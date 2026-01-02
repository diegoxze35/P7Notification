package com.mobile.authentication.domain.repository

import com.mobile.authentication.domain.model.AppUser

interface UserRepository {
    suspend fun saveUser(user: AppUser): Result<Unit>
    suspend fun getUser(uid: String): Result<AppUser?>
    suspend fun updateFcmToken(uid: String, token: String): Result<Unit>
    suspend fun getAllUsers(): Result<List<AppUser>> // Exclusivo para Admins
}