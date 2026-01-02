package com.mobile.authentication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.mobile.authentication.ui.admin.AdminReviewScreen
import com.mobile.authentication.ui.admin.AdminUsersScreen
import com.mobile.authentication.ui.auth.LoginScreen
import com.mobile.authentication.ui.auth.RegisterScreen
import com.mobile.authentication.ui.events.CreateEventScreen
import com.mobile.authentication.ui.home.HomeScreen
import com.mobile.authentication.ui.theme.AuthenticationTheme
import com.mobile.authentication.ui.user.UserNotificationsScreen
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    // Inyectamos Auth para revisar sesión inicial
    private val firebaseAuth: FirebaseAuth by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AuthenticationTheme {
                val navController = rememberNavController()

                // Determinamos destino inicial
                val startDestination = if (firebaseAuth.currentUser != null) "home" else "login"

                NavHost(navController = navController, startDestination = startDestination) {

                    composable("login") {
                        LoginScreen(
                            onNavigateToRegister = { navController.navigate("register") },
                            onLoginSuccess = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            onNavigateToLogin = { navController.navigate("login") },
                            onRegisterSuccess = {
                                navController.navigate("home") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            onLogout = {
                                firebaseAuth.signOut()
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onNavigateToCreateEvent = { navController.navigate("create_event") },
                            onNavigateToAdminUsers = { navController.navigate("admin_users") },
                            onNavigateToReviewEvents = { navController.navigate("admin_review") },
                            onNavigateToHistory = { navController.navigate("user_history") }
                        )
                    }

                    composable("create_event") {
                        CreateEventScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("admin_users") {
                        AdminUsersScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("admin_review") {
                        AdminReviewScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("user_history") {
                        UserNotificationsScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
