package com.mobile.authentication.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.authentication.domain.model.UserNotification
import com.mobile.authentication.domain.repository.AuthService
import com.mobile.authentication.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val notifications: List<UserNotification> = emptyList(),
    val error: String? = null
)

class UserNotificationsViewModel(
    private val notificationRepository: NotificationRepository,
    private val authService: AuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        val userId = authService.currentUser?.uid ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            notificationRepository.getUserNotifications(userId)
                .onSuccess { list ->
                    _uiState.update { it.copy(isLoading = false, notifications = list) }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.message) }
                }
        }
    }
}