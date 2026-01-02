package com.mobile.authentication

import android.app.Application
import com.mobile.authentication.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AuthenticationApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@AuthenticationApp)
            modules(appModule)
        }
    }
}