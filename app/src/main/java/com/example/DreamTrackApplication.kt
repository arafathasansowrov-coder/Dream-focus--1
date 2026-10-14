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

        // 2. Initialize Firebase safely with fallback options if google-services.json is absent
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:408576089626:android:dreamtrack")
                    .setProjectId("dreamtrack-study-hub")
                    .setApiKey("AIzaSyFallbackKeyForLocalOfflineUse12345")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d(TAG, "FirebaseApp initialized with fallback options")
            }
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseApp initialization handled: ${e.message}")
        }

        // 3. Setup default FCM topics safely
        try {
            DreamFirebaseMessagingService.setupDefaultTopicSubscriptions()
        } catch (e: Exception) {
            Log.w(TAG, "FCM Topic setup handled: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "DreamTrackApp"
    }
}
