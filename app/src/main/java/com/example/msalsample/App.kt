package com.example.msalsample

import android.app.Application
import com.example.msalsample.msal.AuthHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var authHelper: AuthHelper

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(authHelper)
    }
}
