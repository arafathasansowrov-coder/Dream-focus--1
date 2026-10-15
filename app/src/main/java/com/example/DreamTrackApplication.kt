package com.example

import android.app.Application
import android.util.Log
import com.example.receiver.DreamFirebaseMessagingService
import com.example.util.NotificationHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

/**
 * Main Application class for DreamTrack.
 * Initializes notification channels, FirebaseApp with fallback options if needed,
 * and sets up local app services safely.
 */
class DreamTrackApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize Notification Channels
        try {
            NotificationHelper.createNotificationChannels(this)
        } catch (e: Exception) {
            Log.w(TAG, "Notification channel setup handled: ${e.message}")
        }

        // 2. Initialize Firebase safely if needed
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d(TAG, "FirebaseApp initialized successfully")
            }
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseApp initialization handled: ${e.message}")
        }

        // 3. Setup default notification reminder alerts safely
        try {
            DreamFirebaseMessagingService.setupDefaultTopicSubscriptions()
        } catch (e: Exception) {
            Log.w(TAG, "Alert setup handled: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "DreamTrackApp"
    }
}
