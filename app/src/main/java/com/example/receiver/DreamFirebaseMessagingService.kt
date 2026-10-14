package com.example.receiver

import android.util.Log
import com.example.data.db.AppDatabase
import com.example.util.NotificationHelper
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Firebase Cloud Messaging Service handling daily motivational reminders,
 * upcoming study session start alerts, and real-time remote cloud pings.
 */
class DreamFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New Firebase Cloud Messaging Token generated: $token")
        
        // Save FCM token in local database for cloud sync
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val profile = db.profileDao()
                // Update profile token if exists
            } catch (e: Exception) {
                Log.e(TAG, "Error caching FCM token: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val type = data["type"] ?: "motivation"
        val title = notification?.title ?: data["title"] ?: "DreamTrack Daily Alert"
        val body = notification?.body ?: data["body"] ?: data["quote"] ?: "Time to focus on your dream goals!"
        val category = data["category"] ?: "Deep Work"

        when (type) {
            "study_alert", "session_start" -> {
                NotificationHelper.showRoutineAlarmNotification(
                    context = applicationContext,
                    routineTitle = title,
                    category = category
                )
            }
            "daily_motivation", "motivation" -> {
                NotificationHelper.showMotivationNotification(
                    context = applicationContext,
                    title = "⚡ $title",
                    message = body
                )
            }
            else -> {
                NotificationHelper.showMotivationNotification(
                    context = applicationContext,
                    title = title,
                    message = body
                )
            }
        }
    }

    companion object {
        private const val TAG = "DreamFCMService"
        const val TOPIC_DAILY_MOTIVATION = "daily_motivation"
        const val TOPIC_STUDY_ALERTS = "study_alerts"

        /**
         * Initialize FCM subscriptions for motivation and study session alerts
         */
        fun setupDefaultTopicSubscriptions() {
            try {
                FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_DAILY_MOTIVATION)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Successfully subscribed to $TOPIC_DAILY_MOTIVATION")
                        }
                    }

                FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_STUDY_ALERTS)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Successfully subscribed to $TOPIC_STUDY_ALERTS")
                        }
                    }
            } catch (e: Exception) {
                Log.w(TAG, "FCM subscription initialization notice: ${e.message}")
            }
        }

        /**
         * Subscribe or unsubscribe dynamically based on user toggle
         */
        fun toggleTopicSubscription(topic: String, enable: Boolean) {
            try {
                if (enable) {
                    FirebaseMessaging.getInstance().subscribeToTopic(topic)
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed toggling topic $topic: ${e.message}")
            }
        }
    }
}
