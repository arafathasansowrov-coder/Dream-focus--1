package com.example.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.db.AppDatabase
import com.example.data.model.DistractionAlertEvent
import com.example.util.NotificationHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Foreground Service that continuously guards focus during deep work sprints.
 * Monitors foreground app usage and immediately alerts the student when they
 * navigate away from designated study apps into non-study distractions.
 */
class DistractionShieldService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var isMonitoring = false
    private var lastAlertedPackage: String? = null
    private var lastAlertTimestamp: Long = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_SHIELD

        when (action) {
            ACTION_STOP_SHIELD -> {
                stopShieldMonitoring()
                return START_NOT_STICKY
            }
            ACTION_SIMULATE_NAVIGATE_AWAY -> {
                val appName = intent?.getStringExtra(EXTRA_SIMULATED_NAME) ?: "YouTube"
                val pkgName = intent?.getStringExtra(EXTRA_SIMULATED_PKG) ?: "com.google.android.youtube"
                handleDistractionDetected(pkgName, appName, "Simulated navigation away from study tools")
                return START_STICKY
            }
            ACTION_START_SHIELD -> {
                startForegroundWithNotification()
                startMonitoringLoop()
            }
        }

        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        _isServiceRunning.value = true

        val notification = buildOngoingNotification("Monitoring active study apps...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                if (Build.VERSION.SDK_INT >= 34) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            } catch (e: Exception) {
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildOngoingNotification(statusText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_TO", "shield")
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            101,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, DistractionShieldService::class.java).apply {
            action = ACTION_STOP_SHIELD
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            102,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val durationMinutes = _activeSessionDurationSeconds.value / 60
        val durationFormatted = "%02d:%02d".format(durationMinutes, _activeSessionDurationSeconds.value % 60)

        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_SHIELD_FOREGROUND)
            .setSmallIcon(android.R.drawable.ic_secure)
            .setContentTitle("🛡️ Distraction Shield Active ($durationFormatted)")
            .setContentText(statusText)
            .setSubText("DreamTrack Focus Lock")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop Shield",
                stopPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_view,
                "Open Study Hub",
                openAppPendingIntent
            )
            .build()
    }

    private fun updateOngoingNotification(statusText: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(NOTIFICATION_ID, buildOngoingNotification(statusText))
    }

    private fun startMonitoringLoop() {
        if (isMonitoring) return
        isMonitoring = true

        serviceScope.launch {
            val database = AppDatabase.getDatabase(applicationContext)

            while (isActive && isMonitoring) {
                delay(1500L)
                _activeSessionDurationSeconds.value += 2

                // Update notification text every 30 seconds
                if (_activeSessionDurationSeconds.value % 30 == 0) {
                    val count = _activeStudyAppCount.value
                    updateOngoingNotification("Focus locked • $count designated study apps permitted")
                }

                // Check active foreground package
                val hasPermission = hasUsageStatsPermission(applicationContext)
                if (hasPermission) {
                    try {
                        val enabledApps = database.studyAppDao().getEnabledStudyAppsSync()
                        _activeStudyAppCount.value = enabledApps.size

                        val allowedPackages = enabledApps.map { it.packageName }.toMutableSet()
                        allowedPackages.add(packageName) // Always allow DreamTrack itself

                        // Also allow home launcher so the user can open another allowed app
                        val launcherPackages = getHomeLauncherPackages(applicationContext)
                        allowedPackages.addAll(launcherPackages)

                        val blockedRules = database.blockedAppDao().getAllBlockedApps()
                        // Collect synchronously or read via DB
                        val currentForeground = getCurrentForegroundPackage(applicationContext)

                        if (!currentForeground.isNullOrBlank()) {
                            _currentMonitoredPackage.value = currentForeground

                            val isAllowed = allowedPackages.contains(currentForeground)
                            val isBlockedExplicitly = blockedRules.let {
                                // check if current package is in known distraction apps
                                currentForeground.contains("youtube") ||
                                currentForeground.contains("instagram") ||
                                currentForeground.contains("tiktok") ||
                                currentForeground.contains("twitter") ||
                                currentForeground.contains("netflix") ||
                                currentForeground.contains("facebook")
                            }

                            if (!isAllowed || isBlockedExplicitly) {
                                // User navigated away from designated study apps!
                                val now = System.currentTimeMillis()
                                val isSamePackage = (currentForeground == lastAlertedPackage)
                                val isThrottled = isSamePackage && (now - lastAlertTimestamp < 8000L)

                                if (!isThrottled) {
                                    val appLabel = getAppNameFromPackage(currentForeground)
                                    handleDistractionDetected(
                                        packageName = currentForeground,
                                        appName = appLabel,
                                        reason = "Navigated away from designated study apps into $appLabel"
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Keep monitoring smoothly
                    }
                }
            }
        }
    }

    private fun handleDistractionDetected(packageName: String, appName: String, reason: String) {
        lastAlertedPackage = packageName
        lastAlertTimestamp = System.currentTimeMillis()

        val alertEvent = DistractionAlertEvent(
            appName = appName,
            packageName = packageName,
            timestampMillis = System.currentTimeMillis(),
            reason = reason
        )
        _lastDetectedDistraction.value = alertEvent

        // 1. Fire high-priority alert notification with sound & vibration
        NotificationHelper.showNavigatedAwayDistractionAlert(
            context = applicationContext,
            appName = appName,
            packageName = packageName,
            reason = reason
        )

        // 2. Trigger haptic pattern
        NotificationHelper.triggerHapticAlert(applicationContext)

        // 3. Record in Room database
        serviceScope.launch {
            try {
                val database = AppDatabase.getDatabase(applicationContext)
                database.blockedAppDao().recordBlockAttempt(packageName)
            } catch (e: Exception) {
                // Ignore DB error
            }
        }
    }

    private fun stopShieldMonitoring() {
        isMonitoring = false
        _isServiceRunning.value = false
        serviceJob.cancel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onDestroy() {
        isMonitoring = false
        _isServiceRunning.value = false
        serviceJob.cancel()
        super.onDestroy()
    }

    private fun getAppNameFromPackage(pkg: String): String {
        return try {
            val pm = packageManager
            val info = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            when {
                pkg.contains("youtube") -> "YouTube"
                pkg.contains("instagram") -> "Instagram"
                pkg.contains("tiktok") -> "TikTok"
                pkg.contains("twitter") || pkg.contains("x.android") -> "X (Twitter)"
                pkg.contains("facebook") -> "Facebook"
                pkg.contains("reddit") -> "Reddit"
                pkg.contains("netflix") -> "Netflix"
                else -> pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() }
            }
        }
    }

    companion object {
        const val NOTIFICATION_ID = 2001
        const val ACTION_START_SHIELD = "com.example.action.START_SHIELD"
        const val ACTION_STOP_SHIELD = "com.example.action.STOP_SHIELD"
        const val ACTION_SIMULATE_NAVIGATE_AWAY = "com.example.action.SIMULATE_NAVIGATE_AWAY"
        const val EXTRA_SIMULATED_NAME = "extra_simulated_name"
        const val EXTRA_SIMULATED_PKG = "extra_simulated_pkg"

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        private val _activeSessionDurationSeconds = MutableStateFlow(0)
        val activeSessionDurationSeconds: StateFlow<Int> = _activeSessionDurationSeconds.asStateFlow()

        private val _lastDetectedDistraction = MutableStateFlow<DistractionAlertEvent?>(null)
        val lastDetectedDistraction: StateFlow<DistractionAlertEvent?> = _lastDetectedDistraction.asStateFlow()

        private val _activeStudyAppCount = MutableStateFlow(8)
        val activeStudyAppCount: StateFlow<Int> = _activeStudyAppCount.asStateFlow()

        private val _currentMonitoredPackage = MutableStateFlow<String?>("com.aistudio.dreamtrack.stdupk")
        val currentMonitoredPackage: StateFlow<String?> = _currentMonitoredPackage.asStateFlow()

        fun startShield(context: Context) {
            _activeSessionDurationSeconds.value = 0
            val intent = Intent(context, DistractionShieldService::class.java).apply {
                action = ACTION_START_SHIELD
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopShield(context: Context) {
            val intent = Intent(context, DistractionShieldService::class.java).apply {
                action = ACTION_STOP_SHIELD
            }
            context.startService(intent)
            _isServiceRunning.value = false
        }

        fun simulateDistractionAlert(context: Context, appName: String, packageName: String) {
            val intent = Intent(context, DistractionShieldService::class.java).apply {
                action = ACTION_SIMULATE_NAVIGATE_AWAY
                putExtra(EXTRA_SIMULATED_NAME, appName)
                putExtra(EXTRA_SIMULATED_PKG, packageName)
            }
            if (_isServiceRunning.value) {
                context.startService(intent)
            } else {
                // If service is not running, start it or trigger alert directly
                _lastDetectedDistraction.value = DistractionAlertEvent(
                    appName = appName,
                    packageName = packageName,
                    reason = "Simulated navigation away from study apps into $appName"
                )
                NotificationHelper.showNavigatedAwayDistractionAlert(
                    context,
                    appName,
                    packageName,
                    "Simulated navigation away from study apps"
                )
            }
        }

        fun dismissLastAlert() {
            _lastDetectedDistraction.value = null
        }

        fun hasUsageStatsPermission(context: Context): Boolean {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
                ?: return false
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )
            }
            return mode == AppOpsManager.MODE_ALLOWED
        }

        fun openUsageStatsSettings(context: Context) {
            try {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            }
        }

        fun getCurrentForegroundPackage(context: Context): String? {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? android.app.usage.UsageStatsManager
                ?: return null
            val endTime = System.currentTimeMillis()
            val startTime = endTime - 10000L

            try {
                val events = usageStatsManager.queryEvents(startTime, endTime)
                val event = android.app.usage.UsageEvents.Event()
                var lastPackage: String? = null
                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    if (event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED ||
                        event.eventType == android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND) {
                        lastPackage = event.packageName
                    }
                }
                if (!lastPackage.isNullOrBlank()) {
                    return lastPackage
                }

                val statsList = usageStatsManager.queryUsageStats(
                    android.app.usage.UsageStatsManager.INTERVAL_DAILY,
                    startTime,
                    endTime
                )
                return statsList?.maxByOrNull { it.lastTimeUsed }?.packageName
            } catch (e: Exception) {
                return null
            }
        }

        fun getHomeLauncherPackages(context: Context): Set<String> {
            return try {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                }
                val resolveInfos = context.packageManager.queryIntentActivities(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                val set = mutableSetOf(
                    "com.android.launcher",
                    "com.google.android.apps.nexuslauncher",
                    "com.sec.android.app.launcher",
                    "com.miui.home",
                    "com.oppo.launcher",
                    "com.coloros.launcher"
                )
                for (info in resolveInfos) {
                    info.activityInfo?.packageName?.let { set.add(it) }
                }
                set
            } catch (e: Exception) {
                emptySet()
            }
        }
    }
}
