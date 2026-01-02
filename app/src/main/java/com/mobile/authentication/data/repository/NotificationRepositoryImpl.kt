package com.mobile.authentication.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import com.mobile.authentication.domain.model.UserNotification
import com.mobile.authentication.domain.repository.NotificationRepository
import kotlinx.coroutines.tasks.await

class NotificationRepositoryImpl(
    private val functions: FirebaseFunctions
) : NotificationRepository {

    override suspend fun sendNotificationToUsers(
        tokens: List<String>,
        title: String,
        body: String
    ): Result<Unit> = runCatching {
        val data = hashMapOf(
            "tokens" to tokens,
            "title" to title,
            "body" to body
        )
        // Llamamos a la función "sendPushNotification" que creamos en Node.js
        functions.getHttpsCallable("sendPushNotification").call(data).await()
    }

    override suspend fun sendNotificationToAll(title: String, body: String): Result<Unit> =
        runCatching {
            val data = hashMapOf(
                "topic" to "all_users",
                "title" to title,
                "body" to body
            )
            functions.getHttpsCallable("sendPushNotification").call(data).await()
        }

    override suspend fun getUserNotifications(userId: String): Result<List<UserNotification>> =
        runCatching {
            val snapshot = FirebaseFirestore.getInstance() // O inyéctalo si prefieres
                .collection("users")
                .document(userId)
                .collection("notifications")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.toObjects(UserNotification::class.java)

        }
}
