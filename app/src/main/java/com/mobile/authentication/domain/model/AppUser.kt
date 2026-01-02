package com.mobile.authentication.domain.model

data class AppUser(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: UserRole = UserRole.NORMAL,
    val fcmToken: String? = null // Para las notificaciones
)
