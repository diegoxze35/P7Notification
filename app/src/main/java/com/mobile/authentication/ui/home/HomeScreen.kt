package com.mobile.authentication.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mobile.authentication.R
import com.mobile.authentication.domain.model.UserRole
import com.mobile.authentication.ui.home.dashboards.AdminDashboard
import com.mobile.authentication.ui.home.dashboards.UserDashboard
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onNavigateToCreateEvent: () -> Unit,
    onNavigateToAdminUsers: () -> Unit,
    onNavigateToReviewEvents: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = stringResource(R.string.logout)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.currentUser != null -> {
                    val user = state.currentUser!!

                    if (user.role == UserRole.ADMIN) {
                        AdminDashboard(
                            user = user,
                            onSendNotificationClick = onNavigateToAdminUsers,
                            onReviewEventsClick = onNavigateToReviewEvents
                        )
                    } else {
                        UserDashboard(
                            user = user,
                            isEmailVerified = state.isEmailVerified,
                            onRefreshVerification = { viewModel.checkEmailVerification() },
                            onResendEmail = { viewModel.resendVerificationEmail() },
                            onCreateEventClick = onNavigateToCreateEvent,
                            onNavigateToHistory = onNavigateToHistory
                        )
                    }
                }

                state.error != null -> {
                    Text(
                        text = state.error ?: "Unknown Error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}