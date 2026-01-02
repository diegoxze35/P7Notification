package com.mobile.authentication.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import com.mobile.authentication.data.repository.AuthServiceImpl
import com.mobile.authentication.data.repository.EventRepositoryImpl
import com.mobile.authentication.data.repository.NotificationRepositoryImpl
import com.mobile.authentication.data.repository.UserRepositoryImpl
import com.mobile.authentication.domain.repository.AuthService
import com.mobile.authentication.domain.repository.EventRepository
import com.mobile.authentication.domain.repository.NotificationRepository
import com.mobile.authentication.domain.repository.UserRepository
import com.mobile.authentication.ui.admin.AdminEventReviewViewModel
import com.mobile.authentication.ui.admin.AdminNotificationViewModel
import com.mobile.authentication.ui.auth.AuthViewModel
import com.mobile.authentication.ui.events.CreateEventViewModel
import com.mobile.authentication.ui.home.HomeViewModel
import com.mobile.authentication.ui.user.UserNotificationsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Firebase Instances
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseMessaging.getInstance() }
    single { FirebaseFunctions.getInstance() }

    // --- Repositories ---
    single<NotificationRepository> { NotificationRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<EventRepository> { EventRepositoryImpl(get()) }

    // --- Services ---
    single<AuthService> { AuthServiceImpl(get(), get()) }

    // --- ViewModels ---
    viewModel { AuthViewModel(get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { CreateEventViewModel(get(), get()) }
    viewModel { AdminNotificationViewModel(get(), get()) }
    viewModel { AdminEventReviewViewModel(get()) }
    viewModel { UserNotificationsViewModel(get(), get()) }

}
