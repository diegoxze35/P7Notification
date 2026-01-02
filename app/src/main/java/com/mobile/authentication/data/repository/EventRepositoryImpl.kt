package com.mobile.authentication.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.authentication.domain.model.AppEvent
import com.mobile.authentication.domain.model.EventStatus
import com.mobile.authentication.domain.repository.EventRepository
import kotlinx.coroutines.tasks.await

class EventRepositoryImpl(firestore: FirebaseFirestore) : EventRepository {

    private val eventsCollection = firestore.collection("events")

    override suspend fun createEvent(event: AppEvent): Result<Unit> = runCatching {
        // Usamos el ID del evento como ID del documento
        eventsCollection.document(event.id).set(event).await()
    }

    override suspend fun getPendingEvents(): Result<List<AppEvent>> = runCatching {
        val snapshot = eventsCollection
            .whereEqualTo("status", "PENDING")
            .get()
            .await()

        // Mapeamos los documentos a objetos AppEvent
        snapshot.toObjects(AppEvent::class.java)
    }

    override suspend fun updateEventStatus(
        eventId: String,
        status: EventStatus,
        feedback: String?
    ): Result<Unit> = runCatching {
        val updates = hashMapOf<String, Any>(
            "status" to status
        )
        if (feedback != null) {
            updates["adminFeedback"] = feedback
        }

        eventsCollection.document(eventId).update(updates).await()
    }
}