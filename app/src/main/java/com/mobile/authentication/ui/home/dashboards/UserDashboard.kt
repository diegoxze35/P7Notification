package com.mobile.authentication.ui.home.dashboards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobile.authentication.R
import com.mobile.authentication.domain.model.AppUser

@Composable
fun UserDashboard(
    user: AppUser,
    isEmailVerified: Boolean,
    onRefreshVerification: () -> Unit,
    onCreateEventClick: () -> Unit,
    onResendEmail: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Header ---
        Text(
            text = stringResource(R.string.welcome_message, user.name),
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = stringResource(R.string.role_user),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- Verification Warning ---
        if (!isEmailVerified) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.email_not_verified_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = stringResource(R.string.email_not_verified_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row {
                        Button(onClick = onResendEmail) {
                            Text(stringResource(R.string.resend_verification))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = onRefreshVerification) {
                            Text(stringResource(R.string.refresh_verification))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Feature Buttons (Locked/Unlocked) ---

        // 1. Create Event
        Button(
            onClick = onCreateEventClick,
            enabled = isEmailVerified, // Bloqueado si no verificó
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(if (isEmailVerified) Icons.Default.MarkEmailRead else Icons.Default.Lock, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isEmailVerified) stringResource(R.string.user_create_event) else stringResource(R.string.feature_locked))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Notifications History
        OutlinedButton(
            onClick = onNavigateToHistory,
            enabled = isEmailVerified,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.user_history))
        }
    }
}