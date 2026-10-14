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
                NotificationHelper.showMotivationNotification(
                    getApplication(),
                    "🎉 Focus Session Completed!",
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
            _aiActionFeedback.value = "✍️ Reflection & Opinion saved to your Journal!"
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
        _aiActionFeedback.value = if (enabled) "⚡ Daily Motivational FCM Alerts Enabled" else "🔕 Motivational Alerts Muted"
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
            title = "🔥 Daily Dream Fuel (FCM Test)",
            message = quote
        )
        _aiActionFeedback.value = "🚀 Test FCM Motivation Alert sent!"
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

    fun dismissFeedback() {
        _aiActionFeedback.value = null
    }
}

