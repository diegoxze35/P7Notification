package com.mobile.authentication.domain.repository

import com.mobile.authentication.domain.model.UserNotification

interface NotificationRepository {
    suspend fun sendNotificationToUsers(tokens: List<String>, title: String, body: String): Result<Unit>
    suspend fun sendNotificationToAll(title: String, body: String): Result<Unit>

    suspend fun getUserNotifications(userId: String): Result<List<UserNotification>>
}