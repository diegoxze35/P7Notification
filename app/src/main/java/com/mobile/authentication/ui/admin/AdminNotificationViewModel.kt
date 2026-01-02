package com.mobile.authentication.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.authentication.domain.model.AppUser
import com.mobile.authentication.domain.repository.NotificationRepository
import com.mobile.authentication.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
    val users: List<AppUser> = emptyList(),
    val selectedUsers: Set<String> = emptySet(), // IDs de usuarios seleccionados
    val isLoading: Boolean = false,
    val messageSent: Boolean = false
)

class AdminNotificationViewModel(
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userRepository.getAllUsers()
                .onSuccess { users ->
                    _uiState.update { it.copy(isLoading = false, users = users) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }

    fun toggleUserSelection(userId: String) {
        _uiState.update { state ->
            val newSelection = state.selectedUsers.toMutableSet()
            if (newSelection.contains(userId)) {
                newSelection.remove(userId)
            } else {
                newSelection.add(userId)
            }
            state.copy(selectedUsers = newSelection)
        }
    }

    fun sendNotification(title: String, message: String, sendToAll: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, messageSent = false) }

            val result = if (sendToAll) {
                notificationRepository.sendNotificationToAll(title, message)
            } else {
                // Filtramos los usuarios seleccionados para obtener sus tokens
                val tokens = _uiState.value.users
                    .filter { it.id in _uiState.value.selectedUsers }
                    .mapNotNull { it.fcmToken } // Solo usuarios con token

                if (tokens.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }
                notificationRepository.sendNotificationToUsers(tokens, title, message)
            }

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, messageSent = true, selectedUsers = emptySet()) }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resetMessageSent() {
        _uiState.update { it.copy(messageSent = false) }
    }
}