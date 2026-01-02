package com.mobile.authentication.domain.repository

import com.mobile.authentication.domain.model.AppEvent
import com.mobile.authentication.domain.model.EventStatus

interface EventRepository {
    suspend fun createEvent(event: AppEvent): Result<Unit>
    suspend fun getPendingEvents(): Result<List<AppEvent>>
    suspend fun updateEventStatus(
        eventId: String,
        status: EventStatus,
        feedback: String?
    ): Result<Unit>
}