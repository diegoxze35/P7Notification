package com.mobile.authentication.domain.repository

import com.google.firebase.auth.FirebaseUser
import com.mobile.authentication.domain.model.AppUser
import com.mobile.authentication.domain.model.UserRole

interface AuthService {
    val currentUser: FirebaseUser?

    suspend fun login(email: String, password: String): Result<Unit>

    suspend fun register(
        email: String,
        password: String,
        name: String,
        role: UserRole,
        masterPassword: String? = null
    ): Result<AppUser>

    suspend fun sendEmailVerification(): Result<Unit>
    suspend fun reloadUser(): Result<Unit> // Necesario para refrescar el estado de isEmailVerified
    fun logout()
}