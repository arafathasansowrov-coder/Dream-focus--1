package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object NotificationHelper {
    const val CHANNEL_ROUTINE_ALARM = "routine_alarms"
    const val CHANNEL_MOTIVATION = "daily_motivation"
    const val CHANNEL_SHIELD_ALERT = "distraction_shield"
    const val CHANNEL_SHIELD_FOREGROUND = "distraction_shield_foreground"
    const val CHANNEL_PSYCHOLOGICAL_NUDGE = "psychological_nudges"

    fun createNotificationChannels(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return

                val routineChannel = NotificationChannel(
                    CHANNEL_ROUTINE_ALARM,
                    "Study Routine Alarms",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "High-priority direct audible alarms for scheduled study sessions and deep work routines."
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 800)
                }

                val psychologicalChannel = NotificationChannel(
                    CHANNEL_PSYCHOLOGICAL_NUDGE,
                    "Psychological Mindset Nudges",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Empowering psychological reminders, cognitive coaching & mindset nudges."
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 200, 100, 200)
                }

                val motivationChannel = NotificationChannel(
                    CHANNEL_MOTIVATION,
                    "Daily Motivation & Dream Fuel",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Daily inspirational reminders and career milestone encouragement."
                }

                val shieldChannel = NotificationChannel(
                    CHANNEL_SHIELD_ALERT,
                    "Distraction Shield Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "High-priority alerts when navigating away from designated study apps during focus sessions."
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 350, 150, 350)
                }

                val foregroundChannel = NotificationChannel(
                    CHANNEL_SHIELD_FOREGROUND,
                    "Distraction Shield Active Service",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Ongoing foreground service notification indicating that study apps are actively monitored."
                    setShowBadge(false)
                }

                manager.createNotificationChannel(routineChannel)
                manager.createNotificationChannel(psychologicalChannel)
                manager.createNotificationChannel(motivationChannel)
                manager.createNotificationChannel(shieldChannel)
                manager.createNotificationChannel(foregroundChannel)
            }
        } catch (e: Exception) {
            // Handled gracefully
        }
    }

    fun showMotivationNotification(context: Context, title: String, message: String) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_MOTIVATION)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.notify((System.currentTimeMillis() % 10000).toInt(), notification)
        } catch (e: Exception) {
            // SecurityException or notification blocked handled gracefully
        }
    }

    fun showRoutineAlarmNotification(context: Context, routineTitle: String, category: String) {
        showDirectAlarmNotification(context, routineTitle, category, "Chime", 5)
    }

    /**
     * Sends an empowering psychological message / mindset nudge.
     * Includes supportive cognitive coaching, breathing reminder, and motivational mindset reframing.
     */
    fun showPsychologicalNudgeNotification(
        context: Context,
        routineTitle: String,
        psychologicalTone: String = "Growth Mindset & Discipline",
        customMessage: String? = null
    ) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("EXTRA_NAVIGATE_TO", "routines")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                101,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val craftedMessage = if (!customMessage.isNullOrBlank()) {
                customMessage
            } else {
                when (psychologicalTone) {
                    "Growth Mindset & Discipline" ->
                        "🧠 Mindset Check: Focus is a craft that compounds daily. Your future self is shaped by what you do right now in $routineTitle."
                    "Gentle Encouragement" ->
                        "🌱 Take a deep breath. Relax your shoulders and release pressure. You are fully capable of doing great work in $routineTitle."
                    "High-Energy Hype" ->
                        "⚡ Unstoppable Drive! Step into the arena with peak energy and dominate this $routineTitle block!"
                    "Stoic Resilience" ->
                        "🏛️ The obstacle in your path becomes the way. Accept no excuses, mute distractions, and conquer $routineTitle."
                    "Focus & Calm" ->
                        "🧘 Enter your flow state. Clear the clutter, center your mind, and immerse yourself in $routineTitle."
                    else ->
                        "💡 Deep Mindset Shift: Dedicate this time to your vision. Step forward and execute $routineTitle."
                }
            }

            val notification = NotificationCompat.Builder(context, CHANNEL_PSYCHOLOGICAL_NUDGE)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("🧠 Mindset Nudge: $routineTitle")
                .setContentText(craftedMessage)
                .setStyle(NotificationCompat.BigTextStyle().bigText(craftedMessage))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .addAction(
                    android.R.drawable.ic_media_play,
                    "Start Session",
                    pendingIntent
                )
                .setAutoCancel(true)
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.notify((System.currentTimeMillis() % 10000).toInt() + 350, notification)
            triggerHapticAlert(context, longArrayOf(0, 180, 100, 220))
        } catch (e: Exception) {
            // Handled gracefully
        }
    }

    /**
     * Direct loud alarm alert with full-screen intent priority and snooze actions.
     */
    fun showDirectAlarmNotification(
        context: Context,
        routineTitle: String,
        category: String,
        alarmSound: String = "Chime",
        snoozeMinutes: Int = 5
    ) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("EXTRA_NAVIGATE_TO", "routines")
                putExtra("EXTRA_DISMISS_ALARM", true)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                102,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val snoozeIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("EXTRA_SNOOZE_ROUTINE", routineTitle)
            }
            val snoozePendingIntent = PendingIntent.getActivity(
                context,
                103,
                snoozeIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ROUTINE_ALARM)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("⏰ DIRECT ALARM: $routineTitle")
                .setContentText("Scheduled study block starting now ($category). Lock in!")
                .setStyle(NotificationCompat.BigTextStyle().bigText("🚨 DIRECT ALARM: Time for $routineTitle ($category). Sound: $alarmSound. Tap to begin your focus session or snooze."))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Alarm", pendingIntent)
                .addAction(android.R.drawable.ic_popup_sync, "Snooze (${snoozeMinutes}m)", snoozePendingIntent)
                .setAutoCancel(true)
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.notify((System.currentTimeMillis() % 10000).toInt() + 100, notification)
            triggerHapticAlert(context, longArrayOf(0, 500, 200, 500, 200, 800))
        } catch (e: Exception) {
            // Handled gracefully
        }
    }

    fun showDistractionBlockedNotification(context: Context, appName: String) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("EXTRA_NAVIGATE_TO", "shield")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                2,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_SHIELD_ALERT)
                .setSmallIcon(android.R.drawable.ic_secure)
                .setContentTitle("🛡️ Distraction Shield Active")
                .setContentText("$appName was blocked. Return to your focus zone to protect your streak!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.notify((System.currentTimeMillis() % 10000).toInt() + 200, notification)
        } catch (e: Exception) {
            // Handled gracefully
        }
    }

    fun showNavigatedAwayDistractionAlert(
        context: Context,
        appName: String,
        packageName: String,
        reason: String = "Navigated away from designated study apps"
    ) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("EXTRA_NAVIGATE_TO", "shield")
                putExtra("EXTRA_ALERT_APP_NAME", appName)
                putExtra("EXTRA_ALERT_PKG", packageName)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                301,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val actionIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("EXTRA_NAVIGATE_TO", "shield")
            }
            val returnActionPendingIntent = PendingIntent.getActivity(
                context,
                302,
                actionIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_SHIELD_ALERT)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("⚠️ Distraction Alert: Left Study Zone!")
                .setContentText("You navigated away into $appName. Return to your designated study tools!")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Distraction Shield detected you navigated away from approved study apps into $appName ($packageName). Your deep focus momentum is at risk. Return now to protect your study streak!")
                )
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntent)
                .addAction(
                    android.R.drawable.ic_menu_revert,
                    "Return to Study",
                    returnActionPendingIntent
                )
                .setVibrate(longArrayOf(0, 350, 150, 350))
                .setAutoCancel(true)
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.notify(9901, notification)
            triggerHapticAlert(context)
        } catch (e: Exception) {
            // Handled gracefully
        }
    }

    fun triggerHapticAlert(context: Context, pattern: LongArray = longArrayOf(0, 300, 150, 300)) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    android.os.VibrationEffect.createWaveform(pattern, -1)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, -1)
            }
        } catch (e: Exception) {
            // Handled gracefully
        }
    }
}
