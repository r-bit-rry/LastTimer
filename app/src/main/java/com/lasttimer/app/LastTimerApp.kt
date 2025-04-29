package com.lasttimer.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main application class for LastTimer app.
 * Configured with Hilt for dependency injection.
 */
@HiltAndroidApp
class LastTimerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize components here if needed
    }
}
