package com.mobile.authentication.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.mobile.authentication.domain.model.AppUser
import com.mobile.authentication.domain.repository.AuthService
import com.mobile.authentication.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val currentUser: AppUser? = null,
    val isEmailVerified: Boolean = false,
    val error: String? = null
)

class HomeViewModel(
    private val authService: AuthService,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val firebaseUser = authService.currentUser
            if (firebaseUser == null) {
                _uiState.update { it.copy(isLoading = false, error = "No user found") }
                return@launch
            }

            // 1. Obtener estado de verificación de Auth
            val isVerified = firebaseUser.isEmailVerified

            // 2. Obtener datos del rol desde Firestore
            userRepository.getUser(firebaseUser.uid)
                .onSuccess { user ->
                    val fcm = FirebaseMessaging.getInstance()
                    fcm.subscribeToTopic("all_users")
                    fcm.token.addOnSuccessListener { token ->
                        viewModelScope.launch {
                            userRepository.updateFcmToken(user!!.id, token)
                        }
                    }

                    if (user?.role == com.mobile.authentication.domain.model.UserRole.ADMIN) {
                        FirebaseMessaging.getInstance().subscribeToTopic("admin_alerts")
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentUser = user,
                            isEmailVerified = isVerified
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun checkEmailVerification() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Es necesario recargar el usuario de Firebase para actualizar el flag isEmailVerified
            authService.reloadUser().onSuccess {
                val isVerified = authService.currentUser?.isEmailVerified == true
                _uiState.update { it.copy(isLoading = false, isEmailVerified = isVerified) }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false) } // Silent fail or show toast
            }
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            authService.sendEmailVerification()
        }
    }

    fun logout() {
        authService.logout()
    }
}