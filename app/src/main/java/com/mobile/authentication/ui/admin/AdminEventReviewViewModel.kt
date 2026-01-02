package com.mobile.authentication.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.authentication.domain.model.AppEvent
import com.mobile.authentication.domain.model.EventStatus
import com.mobile.authentication.domain.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewUiState(
    val isLoading: Boolean = false,
    val events: List<AppEvent> = emptyList(),
    val error: String? = null,
    val operationSuccess: String? = null // Mensaje temporal de éxito
)

class AdminEventReviewViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPendingEvents()
    }

    fun loadPendingEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            eventRepository.getPendingEvents()
                .onSuccess { list ->
                    _uiState.update { it.copy(isLoading = false, events = list) }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.message) }
                }
        }
    }

    fun resolveEvent(event: AppEvent, approved: Boolean, feedback: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val newStatus = if (approved) EventStatus.APPROVED else EventStatus.REJECTED

            eventRepository.updateEventStatus(event.id, newStatus, feedback)
                .onSuccess {
                    // Recargamos la lista para quitar el evento procesado
                    loadPendingEvents()
                    _uiState.update { it.copy(operationSuccess = "Event processed successfully") }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.message) }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, operationSuccess = null) }
    }
}