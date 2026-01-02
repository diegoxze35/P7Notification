package com.mobile.authentication.domain.model

import java.util.Date

data class UserNotification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val date: Date = Date(),
    val read: Boolean = false,
    val type: String = "INFO" // "SUCCESS", "ERROR", "INFO"
)