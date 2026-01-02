package com.mobile.authentication.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.authentication.domain.model.AppEvent
import com.mobile.authentication.domain.model.EventStatus
import com.mobile.authentication.domain.repository.AuthService
import com.mobile.authentication.domain.repository.EventRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

data class CreateEventUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class CreateEventAction {
    data object NavigateBack : CreateEventAction()
    data class ShowMessage(val message: String) : CreateEventAction()
}

class CreateEventViewModel(
    private val eventRepository: EventRepository,
    private val authService: AuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateEventUiState())
    val uiState = _uiState.asStateFlow()

    private val _actions = Channel<CreateEventAction>()
    val actions = _actions.receiveAsFlow()

    fun submitEvent(title: String, description: String) {
        if (title.isBlank() || description.isBlank()) {
            viewModelScope.launch { _actions.send(CreateEventAction.ShowMessage("Please fill all fields")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val currentUser = authService.currentUser
            if (currentUser == null) {
                _uiState.update { it.copy(isLoading = false) }
                _actions.send(CreateEventAction.ShowMessage("User not authenticated"))
                return@launch
            }

            val newEvent = AppEvent(
                id = UUID.randomUUID().toString(),
                userId = currentUser.uid,
                userEmail = currentUser.email ?: "Unknown",
                title = title,
                description = description,
                date = Date(),
                status = EventStatus.PENDING
            )

            eventRepository.createEvent(newEvent)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _actions.send(CreateEventAction.ShowMessage("Event sent for review!"))
                    _actions.send(CreateEventAction.NavigateBack)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                    _actions.send(CreateEventAction.ShowMessage(error.message ?: "Error creating event"))
                }
        }
    }
}