package com.mobile.authentication.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.authentication.domain.model.AppUser
import com.mobile.authentication.domain.repository.UserRepository
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(
    private val firestore: FirebaseFirestore
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun saveUser(user: AppUser): Result<Unit> = runCatching {
        usersCollection.document(user.id).set(user).await()
    }

    override suspend fun getUser(uid: String): Result<AppUser?> = runCatching {
        val snapshot = usersCollection.document(uid).get().await()
        snapshot.toObject(AppUser::class.java)
    }

    override suspend fun updateFcmToken(uid: String, token: String): Result<Unit> = runCatching {
        usersCollection.document(uid).update("fcmToken", token).await()
    }

    override suspend fun getAllUsers(): Result<List<AppUser>> = runCatching {
        val snapshot = usersCollection.get().await()
        snapshot.toObjects(AppUser::class.java)
    }
}