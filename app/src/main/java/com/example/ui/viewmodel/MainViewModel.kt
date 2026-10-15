package com.example.ui.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.ProductivityRepository
import com.example.data.repository.UserRepository
import com.example.receiver.AlarmReceiver
import com.example.receiver.DreamFirebaseMessagingService
import com.example.util.GoogleAuthManager
import com.example.util.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductivityRepository
    val userRepository: UserRepository = UserRepository(
        databaseId = try {
            application.getString(com.example.R.string.firestore_database_id)
        } catch (e: Exception) {
            "ai-studio-android-dreamfoc-9fd8fe1e-12cd-4e02-b9e6-9eab8003a900"
        }
    )
    val authManager: GoogleAuthManager = GoogleAuthManager(application)

    val profile: StateFlow<StudentProfile>
    val goals: StateFlow<List<GoalItem>>
    val routines: StateFlow<List<RoutineItem>>
    val blockedApps: StateFlow<List<BlockedAppRule>>
    val designatedStudyApps: StateFlow<List<DesignatedStudyApp>>
    val quotes: List<MotivationQuote>

    // Distraction Shield Foreground Service States
    val isDistractionShieldRunning: StateFlow<Boolean> = com.example.service.DistractionShieldService.isServiceRunning
    val shieldDurationSeconds: StateFlow<Int> = com.example.service.DistractionShieldService.activeSessionDurationSeconds
    val lastDistractionAlert: StateFlow<DistractionAlertEvent?> = com.example.service.DistractionShieldService.lastDetectedDistraction
    val monitoredPackage: StateFlow<String?> = com.example.service.DistractionShieldService.currentMonitoredPackage

    // Google Sign-In State
    val authState: StateFlow<UserAuthState> = authManager.authState

    // Study History Store
    val studyHistory: StateFlow<List<StudyHistoryItem>>

    // Personal Opinions & Reflections
    val journalOpinions: StateFlow<List<JournalOpinionItem>>

    // Custom Instructions & Directives
    val customInstructions: StateFlow<List<CustomInstructionItem>>

    // Gamification & Badges
    val badgesList: StateFlow<List<BadgeItem>>
    val leaderboard: StateFlow<List<LeaderboardEntry>>

    // App Connectors Hub
    private val _appConnectors = MutableStateFlow<List<AppConnectorItem>>(emptyList())
    val appConnectors: StateFlow<List<AppConnectorItem>> = _appConnectors.asStateFlow()

    // Theme Mode & Visual Preferences
    private val prefs = application.getSharedPreferences("dreamtrack_user_preferences", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        try {
            ThemeMode.valueOf(prefs.getString("PREF_THEME_MODE", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // System Controls & FCM Notification Preferences
    private val _isDndEnabled = MutableStateFlow(false)
    val isDndEnabled: StateFlow<Boolean> = _isDndEnabled.asStateFlow()

    private val _isKeepScreenOn = MutableStateFlow(true)
    val isKeepScreenOn: StateFlow<Boolean> = _isKeepScreenOn.asStateFlow()

    private val _isHapticFeedbackEnabled = MutableStateFlow(true)
    val isHapticFeedbackEnabled: StateFlow<Boolean> = _isHapticFeedbackEnabled.asStateFlow()

    private val _isDailyMotivationalFcmEnabled = MutableStateFlow(true)
    val isDailyMotivationalFcmEnabled: StateFlow<Boolean> = _isDailyMotivationalFcmEnabled.asStateFlow()

    private val _isStudyAlertsFcmEnabled = MutableStateFlow(true)
    val isStudyAlertsFcmEnabled: StateFlow<Boolean> = _isStudyAlertsFcmEnabled.asStateFlow()

    // Firestore routines & cloud state
    private val _currentUserId = MutableStateFlow("student_user_primary")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    private val _cloudSyncError = MutableStateFlow<String?>(null)
    val cloudSyncError: StateFlow<String?> = _cloudSyncError.asStateFlow()

    private val _firestoreRoutines = MutableStateFlow<List<RoutineItem>>(emptyList())
    val firestoreRoutines: StateFlow<List<RoutineItem>> = _firestoreRoutines.asStateFlow()

    // Gemini Chat & Problem Solver States
    private val _activeAiModel = MutableStateFlow(AiModelType.GEMINI)
    val activeAiModel: StateFlow<AiModelType> = _activeAiModel.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isGeneratingAiResponse = MutableStateFlow(false)
    val isGeneratingAiResponse: StateFlow<Boolean> = _isGeneratingAiResponse.asStateFlow()

    // Recharts-Style Weekly Productivity Visualization State
    private val _weeklyProductivityDays = MutableStateFlow(
        listOf(
            WeeklyProductivityDay(dayName = "Mon", dayIndex = 0, fullDate = "Oct 06", milestonesAchieved = 2, routinesCompleted = 4, focusMinutes = 180),
            WeeklyProductivityDay(dayName = "Tue", dayIndex = 1, fullDate = "Oct 07", milestonesAchieved = 3, routinesCompleted = 5, focusMinutes = 240),
            WeeklyProductivityDay(dayName = "Wed", dayIndex = 2, fullDate = "Oct 08", milestonesAchieved = 1, routinesCompleted = 4, focusMinutes = 150),
            WeeklyProductivityDay(dayName = "Thu", dayIndex = 3, fullDate = "Oct 09", milestonesAchieved = 4, routinesCompleted = 6, focusMinutes = 280),
            WeeklyProductivityDay(dayName = "Fri", dayIndex = 4, fullDate = "Oct 10", milestonesAchieved = 2, routinesCompleted = 5, focusMinutes = 210, isToday = true),
            WeeklyProductivityDay(dayName = "Sat", dayIndex = 5, fullDate = "Oct 11", milestonesAchieved = 3, routinesCompleted = 3, focusMinutes = 160),
            WeeklyProductivityDay(dayName = "Sun", dayIndex = 6, fullDate = "Oct 12", milestonesAchieved = 2, routinesCompleted = 4, focusMinutes = 190)
        )
    )
    val weeklyProductivityDays: StateFlow<List<WeeklyProductivityDay>> = _weeklyProductivityDays.asStateFlow()

    // AI Study Planner States
    private val _generatedStudyPlan = MutableStateFlow<GeneratedStudyPlan?>(null)
    val generatedStudyPlan: StateFlow<GeneratedStudyPlan?> = _generatedStudyPlan.asStateFlow()

    private val _isGeneratingPlan = MutableStateFlow(false)
    val isGeneratingPlan: StateFlow<Boolean> = _isGeneratingPlan.asStateFlow()

    private val _aiActionFeedback = MutableStateFlow<String?>(null)
    val aiActionFeedback: StateFlow<String?> = _aiActionFeedback.asStateFlow()

    // Focus Timer States
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timeRemainingSeconds = MutableStateFlow(25 * 60)
    val timeRemainingSeconds: StateFlow<Int> = _timeRemainingSeconds.asStateFlow()

    private val _totalSessionSeconds = MutableStateFlow(25 * 60)
    val totalSessionSeconds: StateFlow<Int> = _totalSessionSeconds.asStateFlow()

    private val _selectedFocusGoal = MutableStateFlow<GoalItem?>(null)
    val selectedFocusGoal: StateFlow<GoalItem?> = _selectedFocusGoal.asStateFlow()

    // Blocker overlay simulation & stats
    private val _simulatedBlockedApp = MutableStateFlow<String?>(null)
    val simulatedBlockedApp: StateFlow<String?> = _simulatedBlockedApp.asStateFlow()

    private val _activeDistractionBlocksCount = MutableStateFlow(3)
    val activeDistractionBlocksCount: StateFlow<Int> = _activeDistractionBlocksCount.asStateFlow()

    // Gamification & Point System
    private val _studentXp = MutableStateFlow(
        prefs.getInt("PREF_STUDENT_XP", 480)
    )
    val studentXp: StateFlow<Int> = _studentXp.asStateFlow()

    private val _isLeaderboardEnabled = MutableStateFlow(
        prefs.getBoolean("PREF_LEADERBOARD_ENABLED", true)
    )
    val isLeaderboardEnabled: StateFlow<Boolean> = _isLeaderboardEnabled.asStateFlow()

    private val _studentAvatarEmoji = MutableStateFlow(
        prefs.getString("PREF_AVATAR_EMOJI", "🚀") ?: "🚀"
    )
    val studentAvatarEmoji: StateFlow<String> = _studentAvatarEmoji.asStateFlow()

    private val _customPlayerTitle = MutableStateFlow(
        prefs.getString("PREF_PLAYER_TITLE", "Visionary Architect") ?: "Visionary Architect"
    )
    val customPlayerTitle: StateFlow<String> = _customPlayerTitle.asStateFlow()

    private val _recentlyUnlockedBadge = MutableStateFlow<BadgeItem?>(null)
    val recentlyUnlockedBadge: StateFlow<BadgeItem?> = _recentlyUnlockedBadge.asStateFlow()

    private var timerJob: Job? = null

    init {
        NotificationHelper.createNotificationChannels(application)
        DreamFirebaseMessagingService.setupDefaultTopicSubscriptions()

        val database = AppDatabase.getDatabase(application)
        repository = ProductivityRepository(database)

        profile = repository.profile.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            StudentProfile()
        )

        goals = repository.allGoals.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        routines = repository.allRoutines.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        blockedApps = repository.allBlockedApps.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        designatedStudyApps = repository.allStudyApps.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        studyHistory = repository.allStudyHistory.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        journalOpinions = repository.allJournalOpinions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        customInstructions = repository.allCustomInstructions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        _appConnectors.value = repository.getDefaultAppConnectors()
        quotes = repository.getMotivationalQuotes()

        badgesList = combine(_studentXp, profile) { xp, currentProfile ->
            InitialBadgesList.createDefaultBadges(xp, currentProfile.currentStreak, currentProfile.completedMilestonesCount)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        leaderboard = combine(
            _studentXp,
            _studentAvatarEmoji,
            profile,
            _customPlayerTitle
        ) { xp, avatar, currentProfile, title ->
            generateLeaderboard(xp, avatar, currentProfile.name, title, currentProfile.currentStreak)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Start observing Firestore routines in real-time
        observeFirestoreRoutines()
    }

    private fun observeFirestoreRoutines() {
        viewModelScope.launch {
            try {
                userRepository.observeUserRoutines(_currentUserId.value).collect { remoteRoutines ->
                    if (remoteRoutines.isNotEmpty()) {
                        _firestoreRoutines.value = remoteRoutines
                    }
                }
            } catch (e: Exception) {
                _cloudSyncError.value = e.localizedMessage
            }
        }
    }

    fun syncLocalRoutinesToFirestore() {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            try {
                val currentRoutines = routines.value
                for (routine in currentRoutines) {
                    userRepository.saveRoutine(_currentUserId.value, routine)
                }
                _cloudSyncError.value = null
            } catch (e: Exception) {
                _cloudSyncError.value = e.localizedMessage
            } finally {
                _isCloudSyncing.value = false
            }
        }
    }

    fun toggleFirestoreRoutineCompletion(routine: RoutineItem, isCompleted: Boolean) {
        viewModelScope.launch {
            // Optimistically update local Room database
            repository.toggleRoutineCompletion(routine.id, isCompleted)
            if (isCompleted) {
                repository.addFocusTime(routine.durationMinutes)
                awardXp(StudentTaskPoints.DAILY_ROUTINE_DONE, "Completed: ${routine.title}")
            }

            // Sync to Firestore document
            userRepository.toggleRoutineCompletion(
                userId = _currentUserId.value,
                routineId = routine.id.toString(),
                isCompleted = isCompleted
            )
        }
    }

    fun saveRoutineToFirestore(
        title: String,
        time: String,
        category: String,
        durationMinutes: Int,
        alarmEnabled: Boolean,
        reminderType: String = "DIRECT_ALARM",
        psychologicalTone: String = "Growth Mindset & Discipline",
        alarmSound: String = "Chime",
        snoozeMinutes: Int = 5,
        customPsychologicalMessage: String = ""
    ) {
        viewModelScope.launch {
            val newRoutine = RoutineItem(
                title = title,
                scheduledTime = time,
                category = category,
                durationMinutes = durationMinutes,
                isCompletedToday = false,
                alarmEnabled = alarmEnabled,
                reminderType = reminderType,
                psychologicalTone = psychologicalTone,
                alarmSound = alarmSound,
                snoozeMinutes = snoozeMinutes,
                customPsychologicalMessage = customPsychologicalMessage
            )
            // Save to Room
            val localId = repository.insertRoutine(newRoutine)
            val savedRoutine = newRoutine.copy(id = localId)

            // Save to Firestore
            userRepository.saveRoutine(_currentUserId.value, savedRoutine)

            if (alarmEnabled) {
                scheduleRoutineAlarm(savedRoutine)
            }
        }
    }

    fun deleteRoutineFromFirestore(routine: RoutineItem) {
        viewModelScope.launch {
            repository.deleteRoutine(routine)
            userRepository.deleteRoutine(_currentUserId.value, routine.id.toString())
        }
    }

    // Profile Actions
    fun updateProfile(
        name: String,
        dreamTitle: String,
        dreamDesc: String,
        targetYear: Int,
        dailyHours: Float
    ) {
        viewModelScope.launch {
            val current = profile.value
            val updated = current.copy(
                name = name,
                dreamTitle = dreamTitle,
                dreamDescription = dreamDesc,
                targetYear = targetYear,
                dailyTargetHours = dailyHours
            )
            repository.saveProfile(updated)
        }
    }

    // Goal Actions
    fun addGoal(
        title: String,
        category: String,
        priority: String,
        deadline: String,
        milestones: List<String>,
        colorHex: Long = 0xFF6366F1
    ) {
        viewModelScope.launch {
            val goal = GoalItem(
                title = title,
                category = category,
                priority = priority,
                deadlineDate = deadline,
                progressPercentage = 0,
                milestonesJson = milestones.joinToString("|"),
                isCompleted = false,
                colorHex = colorHex
            )
            repository.insertGoal(goal)
        }
    }

    fun updateGoalProgress(goalId: Long, progress: Int, isCompleted: Boolean = progress >= 100) {
        viewModelScope.launch {
            repository.updateGoalProgress(goalId, progress, isCompleted)
            if (isCompleted) {
                awardXp(StudentTaskPoints.GOAL_MILESTONE_DONE, "Goal Milestone achieved!")
            }
        }
    }

    fun deleteGoal(goal: GoalItem) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    // Routine Actions
    fun addRoutine(
        title: String,
        scheduledTime: String,
        category: String,
        durationMinutes: Int,
        alarmEnabled: Boolean,
        reminderType: String = "DIRECT_ALARM",
        psychologicalTone: String = "Growth Mindset & Discipline",
        alarmSound: String = "Chime",
        snoozeMinutes: Int = 5,
        customPsychologicalMessage: String = ""
    ) {
        viewModelScope.launch {
            val routine = RoutineItem(
                title = title,
                scheduledTime = scheduledTime,
                category = category,
                durationMinutes = durationMinutes,
                isCompletedToday = false,
                alarmEnabled = alarmEnabled,
                reminderType = reminderType,
                psychologicalTone = psychologicalTone,
                alarmSound = alarmSound,
                snoozeMinutes = snoozeMinutes,
                customPsychologicalMessage = customPsychologicalMessage
            )
            val id = repository.insertRoutine(routine)
            if (alarmEnabled) {
                scheduleRoutineAlarm(routine.copy(id = id))
            }
        }
    }

    fun toggleRoutineCompletion(routine: RoutineItem) {
        viewModelScope.launch {
            val newState = !routine.isCompletedToday
            repository.toggleRoutineCompletion(routine.id, newState)
            if (newState) {
                // Record focus minutes towards streak
                repository.addFocusTime(routine.durationMinutes)
            }
        }
    }

    fun toggleRoutineAlarm(routine: RoutineItem) {
        viewModelScope.launch {
            val newAlarm = !routine.alarmEnabled
            repository.toggleRoutineAlarm(routine.id, newAlarm)
            if (newAlarm) {
                scheduleRoutineAlarm(routine.copy(alarmEnabled = true))
            }
        }
    }

    fun deleteRoutine(routine: RoutineItem) {
        viewModelScope.launch {
            repository.deleteRoutine(routine)
        }
    }

    // Blocker Actions
    fun toggleAppBlock(packageName: String, isActive: Boolean) {
        viewModelScope.launch {
            repository.toggleAppBlock(packageName, isActive)
        }
    }

    fun addBlockedApp(
        packageName: String,
        appName: String,
        category: String,
        dailyLimitMinutes: Int,
        isStrict: Boolean
    ) {
        viewModelScope.launch {
            val rule = BlockedAppRule(
                packageName = packageName,
                appName = appName,
                category = category,
                dailyLimitMinutes = dailyLimitMinutes,
                usedTodayMinutes = 0,
                isBlockActiveDuringFocus = true,
                isStrictLocked = isStrict,
                timesBlockedToday = 0
            )
            repository.insertOrUpdateBlockedApp(rule)
        }
    }

    fun deleteBlockedApp(app: BlockedAppRule) {
        viewModelScope.launch {
            repository.deleteBlockedApp(app)
        }
    }

    // Distraction Shield Foreground Service Controls
    fun startDistractionShield() {
        com.example.service.DistractionShieldService.startShield(getApplication())
    }

    fun stopDistractionShield() {
        com.example.service.DistractionShieldService.stopShield(getApplication())
    }

    fun toggleDistractionShield() {
        if (isDistractionShieldRunning.value) {
            stopDistractionShield()
        } else {
            startDistractionShield()
        }
    }

    fun simulateDistractionAlert(appName: String, packageName: String) {
        _activeDistractionBlocksCount.value += 1
        viewModelScope.launch {
            repository.recordBlockAttempt(packageName)
        }
        com.example.service.DistractionShieldService.simulateDistractionAlert(
            context = getApplication(),
            appName = appName,
            packageName = packageName
        )
    }

    fun dismissDistractionAlert() {
        com.example.service.DistractionShieldService.dismissLastAlert()
    }

    fun allowDistractedAppAsStudyApp(packageName: String, appName: String) {
        viewModelScope.launch {
            val studyApp = DesignatedStudyApp(
                packageName = packageName,
                appName = appName,
                category = "Study Tool",
                isEnabled = true,
                isSystemPreinstalled = false
            )
            repository.insertOrUpdateStudyApp(studyApp)
            dismissDistractionAlert()
        }
    }

    fun toggleStudyApp(packageName: String, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleStudyApp(packageName, isEnabled)
        }
    }

    fun addStudyApp(packageName: String, appName: String, category: String, uri: String = "") {
        viewModelScope.launch {
            val app = DesignatedStudyApp(
                packageName = packageName,
                appName = appName,
                category = category,
                isEnabled = true,
                actionUri = uri,
                isSystemPreinstalled = false
            )
            repository.insertOrUpdateStudyApp(app)
        }
    }

    fun deleteStudyApp(packageName: String) {
        viewModelScope.launch {
            repository.deleteStudyApp(packageName)
        }
    }

    fun hasUsageStatsPermission(): Boolean {
        return com.example.service.DistractionShieldService.hasUsageStatsPermission(getApplication())
    }

    fun openUsageStatsSettings() {
        com.example.service.DistractionShieldService.openUsageStatsSettings(getApplication())
    }

    fun triggerSimulatedDistraction(appName: String, packageName: String) {
        _simulatedBlockedApp.value = appName
        _activeDistractionBlocksCount.value += 1
        viewModelScope.launch {
            repository.recordBlockAttempt(packageName)
        }
        NotificationHelper.showDistractionBlockedNotification(getApplication(), appName)
    }

    fun clearSimulatedDistraction() {
        _simulatedBlockedApp.value = null
    }

    // Focus Timer Controls
    fun setFocusDuration(minutes: Int, goal: GoalItem? = null) {
        _totalSessionSeconds.value = minutes * 60
        _timeRemainingSeconds.value = minutes * 60
        _selectedFocusGoal.value = goal
    }

    fun startFocusTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeRemainingSeconds.value > 0 && _isTimerRunning.value) {
                delay(1000L)
                _timeRemainingSeconds.value -= 1
            }
            if (_timeRemainingSeconds.value <= 0) {
                _isTimerRunning.value = false
                val loggedMinutes = _totalSessionSeconds.value / 60
                repository.addFocusTime(loggedMinutes)
                _selectedFocusGoal.value?.let { goal ->
                    val newProgress = (goal.progressPercentage + 5).coerceAtMost(100)
                    repository.updateGoalProgress(goal.id, newProgress, newProgress >= 100)
                }
                val points = if (loggedMinutes >= 45) StudentTaskPoints.FOCUS_SPRINT_50_MIN else StudentTaskPoints.FOCUS_SPRINT_25_MIN
                awardXp(points, "Completed ${loggedMinutes}m focus sprint")
                NotificationHelper.showMotivationNotification(
                    getApplication(),
                    "🎉 Focus Session Completed! (+$points XP)",
                    "You conquered ${loggedMinutes}m of deep work for your dream: ${profile.value.dreamTitle}!"
                )
            }
        }
    }

    fun pauseFocusTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetFocusTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        _timeRemainingSeconds.value = _totalSessionSeconds.value
    }

    // Alarms & Motivation Reminders
    fun scheduleRoutineAlarm(routine: RoutineItem) {
        try {
            val context = getApplication<Application>()
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("EXTRA_TITLE", routine.title)
                putExtra("EXTRA_CATEGORY", routine.category)
                putExtra("EXTRA_IS_MOTIVATION", false)
                putExtra("EXTRA_REMINDER_TYPE", routine.reminderType)
                putExtra("EXTRA_TONE", routine.psychologicalTone)
                putExtra("EXTRA_ALARM_SOUND", routine.alarmSound)
                putExtra("EXTRA_SNOOZE_MINUTES", routine.snoozeMinutes)
                putExtra("EXTRA_CUSTOM_MSG", routine.customPsychologicalMessage)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                routine.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Parse time e.g. "06:30 AM"
            val parts = routine.scheduledTime.split(" ")
            val timeParts = parts[0].split(":")
            var hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8
            val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
            if (parts.size > 1 && parts[1].equals("PM", ignoreCase = true) && hour < 12) {
                hour += 12
            } else if (parts.size > 1 && parts[1].equals("AM", ignoreCase = true) && hour == 12) {
                hour = 0
            }

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (_: Exception) {
            // Handled gracefully
        }
    }

    fun testPsychologicalNudge(routine: RoutineItem) {
        NotificationHelper.showPsychologicalNudgeNotification(
            context = getApplication(),
            routineTitle = routine.title,
            psychologicalTone = routine.psychologicalTone,
            customMessage = routine.customPsychologicalMessage.ifBlank { null }
        )
    }

    fun testDirectAlarm(routine: RoutineItem) {
        NotificationHelper.showDirectAlarmNotification(
            context = getApplication(),
            routineTitle = routine.title,
            category = routine.category,
            alarmSound = routine.alarmSound,
            snoozeMinutes = routine.snoozeMinutes
        )
    }

    fun triggerMotivationAlert(quote: MotivationQuote) {
        NotificationHelper.showMotivationNotification(
            getApplication(),
            "🚀 Daily Fuel: ${quote.category}",
            "\"${quote.quote}\" — ${quote.author}"
        )
    }

    // ==========================================
    // MULTI-MODEL AI CHAT & PROBLEM SOLVER
    // ==========================================

    fun setAiModel(model: AiModelType) {
        _activeAiModel.value = model
    }

    fun sendChatMessage(prompt: String) {
        if (prompt.isBlank()) return
        val currentModel = _activeAiModel.value
        val userMsg = ChatMessage(text = prompt.trim(), isUser = true, modelType = currentModel)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isGeneratingAiResponse.value = true

            // Check if user prompt is an AI App Command (create, changes, dismiss any app feature)
            val (commandAction, commandResponse) = executeAiAppCommand(prompt)
            if (commandAction != null && commandResponse != null) {
                delay(400L) // natural processing pause
                val aiMsg = ChatMessage(
                    text = commandResponse,
                    isUser = false,
                    modelType = currentModel,
                    aiAction = commandAction
                )
                _chatMessages.value = _chatMessages.value + aiMsg
                _isGeneratingAiResponse.value = false
                return@launch
            }

            val result = GeminiClient.sendChatMessage(
                history = _chatMessages.value.dropLast(1),
                newPrompt = prompt,
                profile = profile.value,
                modelType = currentModel
            )

            result.onSuccess { responseText ->
                val aiMsg = ChatMessage(text = responseText, isUser = false, modelType = currentModel)
                _chatMessages.value = _chatMessages.value + aiMsg
            }.onFailure { error ->
                val errorMsg = ChatMessage(
                    text = "⚠️ ${error.message ?: "Failed to generate answer. Please check your network connection or API Key."}",
                    isUser = false,
                    isError = true,
                    modelType = currentModel
                )
                _chatMessages.value = _chatMessages.value + errorMsg
            }
            _isGeneratingAiResponse.value = false
        }
    }

    fun importAnalyzedRoutines(suggestions: List<AnalyzedRoutineSuggestion>) {
        viewModelScope.launch {
            suggestions.forEach { item ->
                val routine = RoutineItem(
                    title = item.title,
                    scheduledTime = item.time,
                    category = item.category,
                    durationMinutes = item.durationMinutes,
                    isCompletedToday = false,
                    alarmEnabled = (item.reminderType != ReminderType.NONE),
                    reminderType = item.reminderType.name,
                    psychologicalTone = item.psychologicalTone
                )
                val id = repository.insertRoutine(routine)
                val saved = routine.copy(id = id)
                userRepository.saveRoutine(_currentUserId.value, saved)
                if (saved.alarmEnabled) {
                    scheduleRoutineAlarm(saved)
                }
            }
            _aiActionFeedback.value = "✅ Imported ${suggestions.size} routines from image and scheduled reminders!"
        }
    }

    fun solveAcademicProblem(problem: String, subject: String) {
        if (problem.isBlank()) return
        val userPrompt = "Solve this $subject problem:\n$problem"
        val userMsg = ChatMessage(text = userPrompt, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isGeneratingAiResponse.value = true
            val result = GeminiClient.solveSpecificProblem(problem, subject)

            result.onSuccess { responseText ->
                val aiMsg = ChatMessage(text = responseText, isUser = false)
                _chatMessages.value = _chatMessages.value + aiMsg
            }.onFailure { error ->
                val errorMsg = ChatMessage(
                    text = "⚠️ ${error.message ?: "Failed to solve problem."}",
                    isUser = false,
                    isError = true
                )
                _chatMessages.value = _chatMessages.value + errorMsg
            }
            _isGeneratingAiResponse.value = false
        }
    }

    fun clearChat() {
        _chatMessages.value = emptyList()
    }

    // ==========================================
    // AI STUDY PLANNER & ROADMAP ARCHITECT
    // ==========================================

    fun generateAiStudyPlan(
        subject: String,
        timeframe: String,
        dailyHours: Float,
        currentLevel: String
    ) {
        if (subject.isBlank()) return
        viewModelScope.launch {
            _isGeneratingPlan.value = true
            val result = GeminiClient.generateStudyPlan(
                subject = subject,
                timeframe = timeframe,
                dailyHours = dailyHours,
                currentLevel = currentLevel,
                dreamTitle = profile.value.dreamTitle
            )

            result.onSuccess { plan ->
                _generatedStudyPlan.value = plan
                _aiActionFeedback.value = "✨ AI Study Plan created for '$subject'!"
            }.onFailure { error ->
                _aiActionFeedback.value = "⚠️ Error: ${error.message}"
            }
            _isGeneratingPlan.value = false
        }
    }

    fun importPlanToGoals(plan: GeneratedStudyPlan) {
        viewModelScope.launch {
            // Convert modules into GoalItems
            plan.modules.forEachIndexed { index, module ->
                val goal = GoalItem(
                    title = "${plan.targetSubject}: ${module.title}",
                    category = "AI & Tech",
                    priority = if (index == 0) "High" else "Medium",
                    deadlineDate = "Phase ${index + 1} (${plan.timeframe})",
                    progressPercentage = 0,
                    milestonesJson = module.milestones.joinToString("|"),
                    isCompleted = false,
                    colorHex = when (index % 4) {
                        0 -> 0xFF6366F1
                        1 -> 0xFF06B6D4
                        2 -> 0xFF10B981
                        else -> 0xFFF59E0B
                    }
                )
                repository.insertGoal(goal)
            }
            _aiActionFeedback.value = "✅ ${plan.modules.size} milestones added to your Goals!"
        }
    }

    fun importPlanToRoutines(plan: GeneratedStudyPlan) {
        viewModelScope.launch {
            plan.suggestedRoutines.forEach { routineItem ->
                val routine = RoutineItem(
                    title = routineItem.title,
                    scheduledTime = routineItem.suggestedTime,
                    category = routineItem.category,
                    durationMinutes = routineItem.durationMinutes,
                    isCompletedToday = false,
                    alarmEnabled = true
                )
                val id = repository.insertRoutine(routine)
                scheduleRoutineAlarm(routine.copy(id = id))
            }
            _aiActionFeedback.value = "✅ ${plan.suggestedRoutines.size} daily study habits scheduled to your Routines!"
        }
    }

    // ==========================================
    // GOOGLE SIGN-IN & AUTHENTICATION
    // ==========================================

    fun signInWithGoogle(webClientId: String? = null) {
        viewModelScope.launch {
            val result = authManager.signInWithGoogle(webClientId)
            result.onSuccess { state ->
                state.userId?.let { uid ->
                    _currentUserId.value = uid
                }
                _aiActionFeedback.value = "👋 Welcome back, ${state.displayName ?: "Student"}! Google Account connected."
                // Sync profile and routines
                syncLocalRoutinesToFirestore()
            }.onFailure { e ->
                _aiActionFeedback.value = "⚠️ Sign in notice: ${e.localizedMessage ?: "Failed"}"
            }
        }
    }

    fun signOutUser() {
        viewModelScope.launch {
            authManager.signOut()
            _currentUserId.value = "student_user_primary"
            _aiActionFeedback.value = "🔒 Signed out of Google Account."
        }
    }

    // ==========================================
    // HISTORY STORE SYSTEM
    // ==========================================

    fun logStudyHistory(type: String, title: String, durationMinutes: Int, notes: String) {
        viewModelScope.launch {
            repository.logStudySession(type, title, durationMinutes, notes)
            _aiActionFeedback.value = "📜 Activity logged in History Store ($durationMinutes min)"
        }
    }

    fun deleteHistoryItem(item: StudyHistoryItem) {
        viewModelScope.launch {
            repository.deleteHistory(item)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _aiActionFeedback.value = "🗑️ History logs cleared."
        }
    }

    // ==========================================
    // PERSONAL OPINIONS & JOURNAL REFLECTIONS
    // ==========================================

    fun addJournalOpinion(title: String, opinionText: String, category: String, mood: String) {
        if (title.isBlank() || opinionText.isBlank()) return
        viewModelScope.launch {
            repository.insertJournalOpinion(title, opinionText, category, mood)
            awardXp(StudentTaskPoints.JOURNAL_OPINION_LOGGED, "Journal reflection written")
            _aiActionFeedback.value = "✍️ Reflection & Opinion saved to your Journal! (+${StudentTaskPoints.JOURNAL_OPINION_LOGGED} XP)"
        }
    }

    fun deleteJournalOpinion(item: JournalOpinionItem) {
        viewModelScope.launch {
            repository.deleteJournalOpinion(item)
        }
    }

    // ==========================================
    // CUSTOM INSTRUCTIONS & DIRECTIVES
    // ==========================================

    fun addCustomInstruction(title: String, instructionText: String, targetScope: String) {
        if (title.isBlank() || instructionText.isBlank()) return
        viewModelScope.launch {
            repository.insertCustomInstruction(title, instructionText, targetScope)
            _aiActionFeedback.value = "🎯 Custom directive registered!"
        }
    }

    fun toggleCustomInstruction(item: CustomInstructionItem, isActive: Boolean) {
        viewModelScope.launch {
            repository.toggleCustomInstruction(item.id, isActive)
        }
    }

    fun deleteCustomInstruction(item: CustomInstructionItem) {
        viewModelScope.launch {
            repository.deleteCustomInstruction(item)
        }
    }

    // ==========================================
    // APP CONNECTORS & SYSTEM CONTROLS
    // ==========================================

    fun toggleAppConnector(connectorId: String) {
        _appConnectors.value = _appConnectors.value.map { connector ->
            if (connector.id == connectorId) {
                val newStatus = !connector.isConnected
                _aiActionFeedback.value = if (newStatus) "🔗 ${connector.name} connected!" else "🔌 ${connector.name} unlinked."
                connector.copy(isConnected = newStatus)
            } else connector
        }
    }

    fun toggleDnd(enabled: Boolean) {
        _isDndEnabled.value = enabled
        _aiActionFeedback.value = if (enabled) "🔇 Focus DND Mode Activated" else "🔔 Normal Notification Sound Restored"
    }

    fun toggleKeepScreenOn(enabled: Boolean) {
        _isKeepScreenOn.value = enabled
        _aiActionFeedback.value = if (enabled) "💡 Screen will stay awake during Focus Sprints" else "📱 Standard screen sleep enabled"
    }

    fun toggleHapticFeedback(enabled: Boolean) {
        _isHapticFeedbackEnabled.value = enabled
    }

    fun toggleDailyMotivationalFcm(enabled: Boolean) {
        _isDailyMotivationalFcmEnabled.value = enabled
        DreamFirebaseMessagingService.toggleTopicSubscription(
            DreamFirebaseMessagingService.TOPIC_DAILY_MOTIVATION,
            enabled
        )
        _aiActionFeedback.value = if (enabled) "⚡ Daily Motivational Alerts Enabled" else "🔕 Motivational Alerts Muted"
    }

    fun toggleStudyAlertsFcm(enabled: Boolean) {
        _isStudyAlertsFcmEnabled.value = enabled
        DreamFirebaseMessagingService.toggleTopicSubscription(
            DreamFirebaseMessagingService.TOPIC_STUDY_ALERTS,
            enabled
        )
        _aiActionFeedback.value = if (enabled) "⏰ Scheduled Study Start Alerts Enabled" else "🔕 Study Alerts Muted"
    }

    fun triggerTestMotivationNotification() {
        val quote = quotes.randomOrNull()?.quote ?: "The secret of getting ahead is getting started."
        NotificationHelper.showMotivationNotification(
            context = getApplication(),
            title = "🔥 Daily Dream Fuel",
            message = quote
        )
        _aiActionFeedback.value = "🚀 Motivational Study Alert sent!"
    }

    fun triggerTestStudyAlert() {
        val routine = routines.value.firstOrNull()
        val title = routine?.title ?: "Deep Work: Algorithmic Thinking"
        val category = routine?.category ?: "Coding"
        NotificationHelper.showRoutineAlarmNotification(
            context = getApplication(),
            routineTitle = "$title (FCM Alert)",
            category = category
        )
        _aiActionFeedback.value = "⏰ Test Study Start Alert sent!"
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("PREF_THEME_MODE", mode.name).apply()
        _aiActionFeedback.value = "🎨 Theme switched to ${mode.title}"
    }

    fun toggleThemeMode() {
        val nextMode = when (_themeMode.value) {
            ThemeMode.SYSTEM -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
        }
        setThemeMode(nextMode)
    }

    // ==========================================
    // GAMIFICATION & POINT SYSTEM CONTROLS
    // ==========================================

    fun awardXp(points: Int, reason: String) {
        val newXp = _studentXp.value + points
        _studentXp.value = newXp
        prefs.edit().putInt("PREF_STUDENT_XP", newXp).apply()
        _aiActionFeedback.value = "⭐ +$points XP earned: $reason! (Total: $newXp XP)"

        // Check for unlockable badges
        val badges = InitialBadgesList.createDefaultBadges(
            currentXp = newXp,
            currentStreak = profile.value.currentStreak,
            completedMilestones = profile.value.completedMilestonesCount
        )
        val justUnlocked = badges.firstOrNull { it.requiredXp in (_studentXp.value - points + 1)..newXp }
        if (justUnlocked != null) {
            _recentlyUnlockedBadge.value = justUnlocked
            NotificationHelper.showMotivationNotification(
                context = getApplication(),
                title = "🏆 Achievement Unlocked: ${justUnlocked.title}!",
                message = "${justUnlocked.emoji} ${justUnlocked.description}"
            )
        }
    }

    fun toggleLeaderboard(enabled: Boolean) {
        _isLeaderboardEnabled.value = enabled
        prefs.edit().putBoolean("PREF_LEADERBOARD_ENABLED", enabled).apply()
        _aiActionFeedback.value = if (enabled) "🏆 Global Leaderboard Active!" else "🔒 Leaderboard set to Private Solo Focus."
    }

    fun setStudentAvatarEmoji(emoji: String) {
        _studentAvatarEmoji.value = emoji
        prefs.edit().putString("PREF_AVATAR_EMOJI", emoji).apply()
        _aiActionFeedback.value = "Avatar updated to $emoji"
    }

    fun setCustomPlayerTitle(title: String) {
        _customPlayerTitle.value = title
        prefs.edit().putString("PREF_PLAYER_TITLE", title).apply()
        _aiActionFeedback.value = "Player Rank Title set to $title"
    }

    fun dismissUnlockedBadge() {
        _recentlyUnlockedBadge.value = null
    }

    private fun generateLeaderboard(
        userXp: Int,
        userAvatar: String,
        userName: String,
        userTitle: String,
        userStreak: Int
    ): List<LeaderboardEntry> {
        val peers = listOf(
            LeaderboardEntry(1, "Sophia Chen", "Biomedical Neural AI", 2450, 24, 8, "🧬"),
            LeaderboardEntry(2, "Leo Martinez", "Quantum Cryptographer", 1920, 19, 7, "⚡"),
            LeaderboardEntry(3, "Arafat Hasan", "Autonomous Robotics Lead", 1680, 15, 6, "🤖"),
            LeaderboardEntry(4, "Priya Sharma", "Astrophysics Researcher", 1350, 14, 5, "🌌"),
            LeaderboardEntry(5, "Alex Vance", "Full-Stack Neurotechnology", 980, 10, 4, "💻"),
            LeaderboardEntry(6, "Elena Rostova", "Cognitive Neuroscience", 720, 8, 3, "🧠"),
            LeaderboardEntry(7, "Marcus Aurelius", "Philosophical Strategist", 540, 6, 2, "🏛️")
        )

        val currentUserEntry = LeaderboardEntry(
            rank = 0,
            studentName = if (userName.isBlank()) "You (Student Leader)" else "$userName (You)",
            dreamGoal = userTitle,
            totalPoints = userXp,
            streakDays = userStreak,
            badgesCount = InitialBadgesList.createDefaultBadges(userXp, userStreak, 10).count { it.isUnlocked },
            avatarEmoji = userAvatar,
            isCurrentUser = true
        )

        val combined = (peers + currentUserEntry).sortedByDescending { it.totalPoints }
        return combined.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }
    }

    // ==========================================
    // AI APP COMMAND ENGINE & AUTONOMOUS ACCESS
    // ==========================================

    fun executeAiAppCommand(prompt: String): Pair<AiAppAction?, String?> {
        val lower = prompt.trim().lowercase()

        // 1. Goal creation
        if (lower.startsWith("create goal") || lower.startsWith("add goal") || lower.startsWith("new goal") || lower.startsWith("make goal")) {
            val content = prompt.substringAfter("goal", "").trim()
            val category = when {
                lower.contains("tech") || lower.contains("ai") || lower.contains("code") -> "AI & Tech"
                lower.contains("academic") || lower.contains("math") || lower.contains("exam") -> "Academics"
                lower.contains("content") || lower.contains("youtube") -> "Content Creation"
                lower.contains("skill") -> "Skills"
                else -> "AI & Tech"
            }
            val title = content.ifBlank { "Master High-Impact Study Goal" }
            addGoal(
                title = title,
                category = category,
                priority = "High",
                deadline = "2026-12-31",
                milestones = listOf("Phase 1: Foundations & Core Concepts", "Phase 2: Practice & Projects", "Phase 3: Deep Mastery")
            )
            awardXp(StudentTaskPoints.GOAL_QUEST_LEVEL_UP, "Created goal via AI: $title")
            return Pair(
                AiAppAction.CreateGoal(title, category),
                "🎯 **Goal Created Successfully!**\n\nTitle: **$title**\nCategory: **$category**\nReward: **+${StudentTaskPoints.GOAL_QUEST_LEVEL_UP} XP** added to your profile! You can now track its milestone quests in the Dream Goals tab."
            )
        }

        // 2. Routine creation
        if (lower.startsWith("create routine") || lower.startsWith("add routine") || lower.startsWith("new habit") || lower.startsWith("schedule routine") || lower.startsWith("set routine")) {
            val content = prompt.substringAfter("routine", "").ifBlank { prompt.substringAfter("habit", "") }.trim()
            val time = if (lower.contains("at ")) {
                prompt.substringAfter("at ", "08:00 AM").trim()
            } else "08:00 AM"
            val title = if (content.contains("at ")) content.substringBefore("at ").trim() else content.ifBlank { "Focused Morning Study" }
            val category = if (lower.contains("code") || lower.contains("math") || lower.contains("algorithm")) "Coding" else "Deep Work"
            saveRoutineToFirestore(
                title = title,
                time = time,
                category = category,
                durationMinutes = 45,
                alarmEnabled = true
            )
            awardXp(StudentTaskPoints.DAILY_ROUTINE_DONE, "Scheduled routine via AI Command")
            return Pair(
                AiAppAction.CreateRoutine(title, time, category),
                "⏰ **Study Routine Scheduled!**\n\nTitle: **$title**\nTime: **$time**\nCategory: **$category**\nAlarms: **Armed & Active** (+${StudentTaskPoints.DAILY_ROUTINE_DONE} XP)"
            )
        }

        // 3. Focus Timer controls
        if (lower.contains("start focus") || lower.contains("start timer") || lower.contains("pomodoro") || lower.contains("begin focus") || lower.contains("start session")) {
            val minutesRegex = Regex("(\\d+)\\s*(?:min|minute)")
            val match = minutesRegex.find(lower)
            val minutes = match?.groupValues?.get(1)?.toIntOrNull() ?: 25
            setFocusDuration(minutes)
            startFocusTimer()
            return Pair(
                AiAppAction.StartFocusTimer(minutes),
                "⏱️ **Focus Sprint Activated!**\n\nDuration: **$minutes minutes**\nStatus: **Running in Background**\nStay dialed in! Completing this session will earn you **+${StudentTaskPoints.FOCUS_SPRINT_25_MIN} XP**."
            )
        }

        if (lower.contains("stop timer") || lower.contains("pause timer") || lower.contains("stop focus") || lower.contains("cancel timer")) {
            pauseFocusTimer()
            return Pair(
                AiAppAction.StopFocusTimer("Paused via AI Command"),
                "⏸️ **Focus Timer Paused.** Take a breath and resume when ready."
            )
        }

        if (lower.contains("reset timer")) {
            resetFocusTimer()
            return Pair(
                AiAppAction.DismissFeature("Focus Timer Reset"),
                "🔄 **Focus Timer has been reset** to standard 25 minutes."
            )
        }

        // 4. Focus Shield toggle
        if (lower.contains("turn on shield") || lower.contains("enable shield") || lower.contains("activate shield") || lower.contains("shield on")) {
            startDistractionShield()
            awardXp(StudentTaskPoints.DISTRACTION_SHIELD_DEFENSE, "Activated Distraction Shield")
            return Pair(
                AiAppAction.ToggleFocusShield(true),
                "🛡️ **Distraction Shield Activated!**\n\nBackground monitor is now guarding against social media & blacklisted apps. (+${StudentTaskPoints.DISTRACTION_SHIELD_DEFENSE} XP)"
            )
        }

        if (lower.contains("turn off shield") || lower.contains("disable shield") || lower.contains("deactivate shield") || lower.contains("shield off")) {
            stopDistractionShield()
            return Pair(
                AiAppAction.ToggleFocusShield(false),
                "🔓 **Distraction Shield Deactivated.** Monitored background protection is turned off."
            )
        }

        // 5. Theme switching
        if (lower.contains("dark theme") || lower.contains("dark mode")) {
            setThemeMode(ThemeMode.DARK)
            return Pair(
                AiAppAction.SwitchTheme("Dark Mode"),
                "🌙 **Theme switched to Dark Mode.** High-contrast cyberpunk palette applied."
            )
        }
        if (lower.contains("light theme") || lower.contains("light mode")) {
            setThemeMode(ThemeMode.LIGHT)
            return Pair(
                AiAppAction.SwitchTheme("Light Mode"),
                "☀️ **Theme switched to Light Mode.** Clean high-contrast view."
            )
        }
        if (lower.contains("system theme") || lower.contains("auto theme")) {
            setThemeMode(ThemeMode.SYSTEM)
            return Pair(
                AiAppAction.SwitchTheme("System Default"),
                "🌓 **Theme set to System Default.**"
            )
        }

        // 6. Award Points / Add XP
        if (lower.contains("award points") || lower.contains("give me points") || lower.contains("add xp") || lower.contains("reward points") || lower.contains("award xp")) {
            val numRegex = Regex("(\\d+)")
            val points = numRegex.find(lower)?.groupValues?.get(1)?.toIntOrNull() ?: 100
            val reason = if (lower.contains("for ")) lower.substringAfter("for ") else "Task achievement"
            awardXp(points, reason)
            return Pair(
                AiAppAction.AwardPoints(points, reason),
                "⭐ **+$points XP Granted!**\n\nReason: **$reason**\nYour leaderboard standing has been updated."
            )
        }

        // 7. Dream Career update
        if (lower.startsWith("change dream") || lower.startsWith("set dream") || lower.startsWith("my dream is") || lower.startsWith("update dream")) {
            val newTitle = prompt.substringAfter("dream", "").replace("is", "").replace("to", "").trim().ifBlank { "Autonomous Systems Researcher" }
            updateProfile(
                name = profile.value.name,
                dreamTitle = newTitle,
                dreamDesc = profile.value.dreamDescription,
                targetYear = profile.value.targetYear,
                dailyHours = profile.value.dailyTargetHours
            )
            return Pair(
                AiAppAction.SetDreamCareer(newTitle),
                "🚀 **Dream Career Updated!**\n\nYour primary mission is now: **$newTitle**."
            )
        }

        // 8. Leaderboard toggle
        if (lower.contains("enable leaderboard") || lower.contains("show leaderboard") || lower.contains("turn on leaderboard")) {
            toggleLeaderboard(true)
            return Pair(
                AiAppAction.ToggleLeaderboard(true),
                "🏆 **Leaderboard Enabled!** You are now participating in peer rankings."
            )
        }
        if (lower.contains("disable leaderboard") || lower.contains("hide leaderboard") || lower.contains("turn off leaderboard")) {
            toggleLeaderboard(false)
            return Pair(
                AiAppAction.ToggleLeaderboard(false),
                "🔒 **Leaderboard Muted.** App set to private solo focus mode."
            )
        }

        // 9. Clear Chat
        if (lower == "clear chat" || lower == "reset chat" || lower == "dismiss chat") {
            clearChat()
            return Pair(
                AiAppAction.DismissFeature("Chat History"),
                "🧹 **Chat history cleared.** What can I do for you next?"
            )
        }

        return Pair(null, null)
    }

    fun dismissFeedback() {
        _aiActionFeedback.value = null
    }
}


