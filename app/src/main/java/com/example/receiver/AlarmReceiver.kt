package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.util.NotificationHelper

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Scheduled Routine"
        val category = intent.getStringExtra("EXTRA_CATEGORY") ?: "Deep Work"
        val isMotivation = intent.getBooleanExtra("EXTRA_IS_MOTIVATION", false)

        if (isMotivation) {
            val quote = intent.getStringExtra("EXTRA_QUOTE") ?: "Protect your focus and pursue your dreams today."
            NotificationHelper.showMotivationNotification(context, title, quote)
            return
        }

        val reminderType = intent.getStringExtra("EXTRA_REMINDER_TYPE") ?: "DIRECT_ALARM"
        val tone = intent.getStringExtra("EXTRA_TONE") ?: "Growth Mindset & Discipline"
        val customMsg = intent.getStringExtra("EXTRA_CUSTOM_MSG")
        val alarmSound = intent.getStringExtra("EXTRA_ALARM_SOUND") ?: "Chime"
        val snooze = intent.getIntExtra("EXTRA_SNOOZE_MINUTES", 5)

        when (reminderType) {
            "PSYCHOLOGICAL_MESSAGE" -> {
                NotificationHelper.showPsychologicalNudgeNotification(
                    context = context,
                    routineTitle = title,
                    psychologicalTone = tone,
                    customMessage = customMsg
                )
            }
            "DIRECT_ALARM" -> {
                NotificationHelper.showDirectAlarmNotification(
                    context = context,
                    routineTitle = title,
                    category = category,
                    alarmSound = alarmSound,
                    snoozeMinutes = snooze
                )
            }
            "BOTH" -> {
                NotificationHelper.showPsychologicalNudgeNotification(
                    context = context,
                    routineTitle = title,
                    psychologicalTone = tone,
                    customMessage = customMsg
                )
                NotificationHelper.showDirectAlarmNotification(
                    context = context,
                    routineTitle = title,
                    category = category,
                    alarmSound = alarmSound,
                    snoozeMinutes = snooze
                )
            }
            "NONE" -> {
                // Silently skip
            }
            else -> {
                NotificationHelper.showDirectAlarmNotification(context, title, category, alarmSound, snooze)
            }
        }
    }
}
