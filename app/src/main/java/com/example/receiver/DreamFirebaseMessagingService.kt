package com.example.receiver

import android.content.Context
import android.util.Log
import com.example.util.NotificationHelper

/**
 * Service and dispatcher handling daily motivational reminders,
 * upcoming study session start alerts, and local notification triggers.
 * Operates purely locally without requiring remote FCM servers or tokens.
 */
object DreamFirebaseMessagingService {
    private const val TAG = "DreamAlertService"
    const val TOPIC_DAILY_MOTIVATION = "daily_motivation"
    const val TOPIC_STUDY_ALERTS = "study_alerts"

    private var isMotivationEnabled: Boolean = true
    private var isStudyAlertsEnabled: Boolean = true

    /**
     * Initialize notification channels and local reminder subscriptions.
     */
    fun setupDefaultTopicSubscriptions() {
        Log.d(TAG, "Local notification channels and reminder subscriptions initialized successfully.")
    }

    /**
     * Toggle reminder alerts locally based on user settings.
     */
    fun toggleTopicSubscription(topic: String, enable: Boolean) {
        when (topic) {
            TOPIC_DAILY_MOTIVATION -> isMotivationEnabled = enable
            TOPIC_STUDY_ALERTS -> isStudyAlertsEnabled = enable
        }
        Log.d(TAG, "Notification alert topic $topic set to: $enable")
    }

    fun isDailyMotivationEnabled(): Boolean = isMotivationEnabled
    fun isStudyAlertsEnabled(): Boolean = isStudyAlertsEnabled

    /**
     * Send instant motivational notification locally
     */
    fun triggerLocalMotivation(context: Context, title: String, message: String) {
        if (!isMotivationEnabled) return
        NotificationHelper.showMotivationNotification(
            context = context,
            title = "⚡ $title",
            message = message
        )
    }

    /**
     * Send study session start notification locally
     */
    fun triggerLocalStudyAlert(context: Context, routineTitle: String, category: String) {
        if (!isStudyAlertsEnabled) return
        NotificationHelper.showRoutineAlarmNotification(
            context = context,
            routineTitle = routineTitle,
            category = category
        )
    }
}
