package com.mobile.authentication.domain.model

import java.util.Date

enum class EventStatus {
    PENDING,
    APPROVED,
    REJECTED
}

data class AppEvent(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val title: String = "",
    val description: String = "",
    val date: Date = Date(),
    val status: EventStatus = EventStatus.PENDING,
    val adminFeedback: String? = null // Mensaje del admin al aprobar/rechazar
)