package com.mobile.authentication.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.authentication.domain.model.UserRole
import com.mobile.authentication.domain.repository.AuthService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la UI
data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

// Eventos de una sola vez (Navegación, Toasts)
sealed class AuthEvent {
    data object NavigateToHome : AuthEvent()
    data class ShowError(val message: String) : AuthEvent()
}

class AuthViewModel(
    private val authService: AuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    // Channel para eventos de UI (como navegación)
    private val _authChannel = Channel<AuthEvent>()
    val authEvent = _authChannel.receiveAsFlow()

    // Verifica si ya hay usuario logueado al iniciar
    init {
        if (authService.currentUser != null) {
            _uiState.update { it.copy(isLoggedIn = true) }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            authService.login(email, pass)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                    _authChannel.send(AuthEvent.NavigateToHome)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                    _authChannel.send(AuthEvent.ShowError(error.message ?: "Login failed"))
                }
        }
    }

    fun register(email: String, pass: String, name: String, role: UserRole, masterPass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Si es usuario normal, enviamos null como masterPass, si es Admin, enviamos lo que escribió
            val finalMasterPass = if (role == UserRole.ADMIN) masterPass else null

            authService.register(email, pass, name, role, finalMasterPass)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                    _authChannel.send(AuthEvent.NavigateToHome)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                    _authChannel.send(AuthEvent.ShowError(error.message ?: "Registration failed"))
                }
        }
    }
}