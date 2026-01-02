package com.mobile.authentication.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mobile.authentication.BuildConfig
import com.mobile.authentication.domain.model.AppUser
import com.mobile.authentication.domain.model.UserRole
import com.mobile.authentication.domain.repository.AuthService
import com.mobile.authentication.domain.repository.UserRepository
import kotlinx.coroutines.tasks.await

class AuthServiceImpl(
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository
) : AuthService {

    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun register(
        email: String,
        password: String,
        name: String,
        role: UserRole,
        masterPassword: String?
    ): Result<AppUser> = runCatching {

        // 1. Validación de seguridad para Admins
        if (role == UserRole.ADMIN) {
            if (masterPassword != BuildConfig.ADMIN_MASTER_PASSWORD) {
                throw SecurityException("Invalid Master Password")
            }
        }

        // 2. Crear usuario en Firebase Auth
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user ?: throw Exception("User creation failed")

        // 3. Crear modelo de dominio y guardar en Firestore
        val newUser = AppUser(
            id = firebaseUser.uid,
            email = email,
            name = name,
            role = role
        )

        // Si falla guardar en Firestore, idealmente deberíamos borrar el usuario de Auth,
        // pero por simplicidad académica, lanzamos la excepción.
        userRepository.saveUser(newUser).getOrThrow()

        // 4. Enviar correo de verificación automáticamente
        sendEmailVerification()

        newUser
    }

    override suspend fun sendEmailVerification(): Result<Unit> = runCatching {
        firebaseAuth.currentUser?.sendEmailVerification()?.await()
    }

    override suspend fun reloadUser(): Result<Unit> = runCatching {
        firebaseAuth.currentUser?.reload()?.await()
    }

    override fun logout() {
        firebaseAuth.signOut()
    }
}