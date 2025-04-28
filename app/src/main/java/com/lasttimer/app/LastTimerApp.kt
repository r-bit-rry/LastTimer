package com.lasttimer.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LastTimerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize components here
    }
}
