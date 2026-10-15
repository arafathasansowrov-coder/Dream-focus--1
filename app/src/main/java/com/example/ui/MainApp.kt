package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.DistractionAlertModal
import com.example.ui.components.DistractionBlockedDialog
import com.example.ui.components.EditProfileDialog
import com.example.ui.components.GamifiedGoalCompleter
import com.example.ui.components.GamificationLeaderboardModal
import com.example.ui.components.CustomizationCenterModal
import com.example.ui.screens.*
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricIndigo
import com.example.ui.viewmodel.MainViewModel

enum class NavigationTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("Mission", Icons.Default.RocketLaunch, Icons.Outlined.RocketLaunch),
    AI_STUDY("AI Mentor", Icons.Default.AutoAwesome, Icons.Outlined.AutoAwesome),
    ROUTINE_JOURNAL("Routines & Journal", Icons.Default.TaskAlt, Icons.Outlined.TaskAlt),
    GOALS_SHIELD("Goals & Focus", Icons.Default.Flag, Icons.Outlined.Flag),
    SYSTEM_CONNECTORS("Connectors & History", Icons.Default.Hub, Icons.Outlined.Hub)
}

enum class GoalsShieldSubTab(val title: String) {
    GOALS("Dream Goals"),
    AI_QUESTS("AI Game Quests 🎮"),
    SHIELD("Focus Shield")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: MainViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
    var initialAiTab by remember { mutableStateOf(AiHubTab.CHAT) }
    var goalsShieldSubTab by remember { mutableStateOf(GoalsShieldSubTab.GOALS) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showGamificationModal by remember { mutableStateOf(false) }
    var showCustomizationModal by remember { mutableStateOf(false) }

    val studentXp by viewModel.studentXp.collectAsStateWithLifecycle()
    val isLeaderboardEnabled by viewModel.isLeaderboardEnabled.collectAsStateWithLifecycle()
    val studentAvatarEmoji by viewModel.studentAvatarEmoji.collectAsStateWithLifecycle()
    val customPlayerTitle by viewModel.customPlayerTitle.collectAsStateWithLifecycle()
    val badgesList by viewModel.badgesList.collectAsStateWithLifecycle()
    val leaderboard by viewModel.leaderboard.collectAsStateWithLifecycle()
    val recentlyUnlockedBadge by viewModel.recentlyUnlockedBadge.collectAsStateWithLifecycle()

    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val routines by viewModel.routines.collectAsStateWithLifecycle()
    val blockedApps by viewModel.blockedApps.collectAsStateWithLifecycle()
    val designatedStudyApps by viewModel.designatedStudyApps.collectAsStateWithLifecycle()
    val isShieldServiceRunning by viewModel.isDistractionShieldRunning.collectAsStateWithLifecycle()
    val shieldDurationSeconds by viewModel.shieldDurationSeconds.collectAsStateWithLifecycle()
    val lastDistractionAlert by viewModel.lastDistractionAlert.collectAsStateWithLifecycle()

    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val timeRemainingSeconds by viewModel.timeRemainingSeconds.collectAsStateWithLifecycle()
    val totalSessionSeconds by viewModel.totalSessionSeconds.collectAsStateWithLifecycle()
    val selectedGoal by viewModel.selectedFocusGoal.collectAsStateWithLifecycle()

    val simulatedBlockedApp by viewModel.simulatedBlockedApp.collectAsStateWithLifecycle()
    val distractionsCount by viewModel.activeDistractionBlocksCount.collectAsStateWithLifecycle()

    // AI States
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isGeneratingAiResponse by viewModel.isGeneratingAiResponse.collectAsStateWithLifecycle()
    val generatedStudyPlan by viewModel.generatedStudyPlan.collectAsStateWithLifecycle()
    val isGeneratingPlan by viewModel.isGeneratingPlan.collectAsStateWithLifecycle()
    val aiFeedback by viewModel.aiActionFeedback.collectAsStateWithLifecycle()
    val activeAiModel by viewModel.activeAiModel.collectAsStateWithLifecycle()
    val weeklyProductivityDays by viewModel.weeklyProductivityDays.collectAsStateWithLifecycle()

    // Firestore routines & sync states
    val firestoreRoutines by viewModel.firestoreRoutines.collectAsStateWithLifecycle()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsStateWithLifecycle()
    val cloudSyncError by viewModel.cloudSyncError.collectAsStateWithLifecycle()

    // Auth & Connectors & History & Journal States
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val studyHistory by viewModel.studyHistory.collectAsStateWithLifecycle()
    val journalOpinions by viewModel.journalOpinions.collectAsStateWithLifecycle()
    val customInstructions by viewModel.customInstructions.collectAsStateWithLifecycle()
    val appConnectors by viewModel.appConnectors.collectAsStateWithLifecycle()

    // System Control States
    val isDndEnabled by viewModel.isDndEnabled.collectAsStateWithLifecycle()
    val isKeepScreenOn by viewModel.isKeepScreenOn.collectAsStateWithLifecycle()
    val isDailyMotivationalFcmEnabled by viewModel.isDailyMotivationalFcmEnabled.collectAsStateWithLifecycle()
    val isStudyAlertsFcmEnabled by viewModel.isStudyAlertsFcmEnabled.collectAsStateWithLifecycle()

    // Combined routines: use Firestore remote items if available, otherwise Room routines
    val activeRoutines = if (firestoreRoutines.isNotEmpty()) firestoreRoutines else routines

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "DreamTrack",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = profile.dreamTitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    // Gamification & Leaderboard Trophy Button
                    FilledTonalButton(
                        onClick = { showGamificationModal = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = AmberGold.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("top_bar_gamification_button")
                    ) {
                        Text(text = "👑 $studentXp", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = AmberGold)
                    }

                    // Customization Center Button
                    IconButton(
                        onClick = { showCustomizationModal = true },
                        modifier = Modifier.testTag("top_bar_customization_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Customization Center",
                            tint = ElectricIndigo
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleThemeMode() },
                        modifier = Modifier.testTag("top_bar_theme_toggle_button")
                    ) {
                        val icon = when (themeMode) {
                            com.example.data.model.ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                            com.example.data.model.ThemeMode.LIGHT -> Icons.Default.DarkMode
                            com.example.data.model.ThemeMode.DARK -> Icons.Default.LightMode
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Toggle Theme (Current: ${themeMode.title})",
                            tint = CyberCyan
                        )
                    }
                    IconButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.testTag("top_bar_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                tint = if (isSelected) ElectricIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ElectricIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        },
                        modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavigationTab.DASHBOARD -> {
                    DashboardScreen(
                        profile = profile,
                        goals = goals,
                        routines = activeRoutines,
                        quotes = viewModel.quotes,
                        isTimerRunning = isTimerRunning,
                        timeRemainingSeconds = timeRemainingSeconds,
                        weeklyDays = weeklyProductivityDays,
                        onNavigateToFocus = {
                            goalsShieldSubTab = GoalsShieldSubTab.SHIELD
                            currentTab = NavigationTab.GOALS_SHIELD
                        },
                        onNavigateToGoals = {
                            goalsShieldSubTab = GoalsShieldSubTab.GOALS
                            currentTab = NavigationTab.GOALS_SHIELD
                        },
                        onNavigateToRoutines = { currentTab = NavigationTab.ROUTINE_JOURNAL },
                        onNavigateToAiChat = {
                            initialAiTab = AiHubTab.CHAT
                            currentTab = NavigationTab.AI_STUDY
                        },
                        onNavigateToAiPlanner = {
                            initialAiTab = AiHubTab.PLANNER
                            currentTab = NavigationTab.AI_STUDY
                        },
                        onToggleRoutine = { viewModel.toggleFirestoreRoutineCompletion(it, !it.isCompletedToday) },
                        onToggleAlarm = { viewModel.toggleRoutineAlarm(it) },
                        onEditProfile = { showEditProfileDialog = true },
                        onQuoteClick = { viewModel.triggerMotivationAlert(it) },
                        studentXp = studentXp,
                        avatarEmoji = studentAvatarEmoji,
                        playerTitle = customPlayerTitle,
                        unlockedBadgesCount = badgesList.count { it.isUnlocked },
                        userRank = leaderboard.firstOrNull { it.isCurrentUser }?.rank ?: 3,
                        onOpenGamification = { showGamificationModal = true }
                    )
                }
                NavigationTab.AI_STUDY -> {
                    AiStudyHubScreen(
                        profile = profile,
                        chatMessages = chatMessages,
                        isGeneratingAnswer = isGeneratingAiResponse,
                        generatedPlan = generatedStudyPlan,
                        isGeneratingPlan = isGeneratingPlan,
                        feedbackMessage = aiFeedback,
                        onSendMessage = { viewModel.sendChatMessage(it) },
                        onSolveProblem = { prob, subj -> viewModel.solveAcademicProblem(prob, subj) },
                        onClearChat = { viewModel.clearChat() },
                        onGeneratePlan = { subj, time, hrs, lvl ->
                            viewModel.generateAiStudyPlan(subj, time, hrs, lvl)
                        },
                        onImportPlanToGoals = { viewModel.importPlanToGoals(it) },
                        onImportPlanToRoutines = { viewModel.importPlanToRoutines(it) },
                        onDismissFeedback = { viewModel.dismissFeedback() },
                        initialTab = initialAiTab,
                        activeModel = activeAiModel,
                        onSelectModel = { viewModel.setAiModel(it) }
                    )
                }
                NavigationTab.ROUTINE_JOURNAL -> {
                    RoutineJournalScreen(
                        routines = activeRoutines,
                        opinions = journalOpinions,
                        instructions = customInstructions,
                        isCloudSyncing = isCloudSyncing,
                        cloudSyncError = cloudSyncError,
                        onToggleRoutineCompletion = { routine, isCompleted ->
                            viewModel.toggleFirestoreRoutineCompletion(routine, isCompleted)
                        },
                        onToggleRoutineAlarm = { viewModel.toggleRoutineAlarm(it) },
                        onAddRoutine = { title, time, category, duration, alarm, reminderType, tone, sound, snooze, customMsg ->
                            viewModel.saveRoutineToFirestore(
                                title = title,
                                time = time,
                                category = category,
                                durationMinutes = duration,
                                alarmEnabled = alarm,
                                reminderType = reminderType,
                                psychologicalTone = tone,
                                alarmSound = sound,
                                snoozeMinutes = snooze,
                                customPsychologicalMessage = customMsg
                            )
                        },
                        onDeleteRoutine = { viewModel.deleteRoutineFromFirestore(it) },
                        onManualSync = { viewModel.syncLocalRoutinesToFirestore() },
                        onAddOpinion = { title, text, category, mood ->
                            viewModel.addJournalOpinion(title, text, category, mood)
                        },
                        onDeleteOpinion = { viewModel.deleteJournalOpinion(it) },
                        onAddInstruction = { title, text, scope ->
                            viewModel.addCustomInstruction(title, text, scope)
                        },
                        onToggleInstruction = { item, isActive ->
                            viewModel.toggleCustomInstruction(item, isActive)
                        },
                        onDeleteInstruction = { viewModel.deleteCustomInstruction(it) },
                        onImportAnalyzedRoutines = { viewModel.importAnalyzedRoutines(it) }
                    )
                }
                NavigationTab.GOALS_SHIELD -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            GoalsShieldSubTab.entries.forEachIndexed { index, tab ->
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = GoalsShieldSubTab.entries.size),
                                    onClick = { goalsShieldSubTab = tab },
                                    selected = goalsShieldSubTab == tab,
                                    colors = SegmentedButtonDefaults.colors(
                                        activeContainerColor = ElectricIndigo.copy(alpha = 0.2f),
                                        activeContentColor = ElectricIndigo
                                    )
                                ) {
                                    Text(
                                        text = tab.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (goalsShieldSubTab == tab) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        when (goalsShieldSubTab) {
                            GoalsShieldSubTab.GOALS -> {
                                GoalsScreen(
                                    goals = goals,
                                    onAddGoal = { title, category, priority, deadline, milestones ->
                                        viewModel.addGoal(title, category, priority, deadline, milestones)
                                    },
                                    onProgressChange = { id, progress ->
                                        viewModel.updateGoalProgress(id, progress)
                                    },
                                    onDeleteGoal = { viewModel.deleteGoal(it) }
                                )
                            }
                            GoalsShieldSubTab.AI_QUESTS -> {
                                GamifiedGoalCompleter(
                                    goals = goals,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    onGoalCompleted = { goal ->
                                        viewModel.updateGoalProgress(goal.id, 100, true)
                                    }
                                )
                            }
                            GoalsShieldSubTab.SHIELD -> {
                                FocusShieldScreen(
                                    isTimerRunning = isTimerRunning,
                                    timeRemainingSeconds = timeRemainingSeconds,
                                    totalSessionSeconds = totalSessionSeconds,
                                    selectedGoal = selectedGoal,
                                    goals = goals,
                                    blockedApps = blockedApps,
                                    designatedStudyApps = designatedStudyApps,
                                    distractionsBlockedCount = distractionsCount,
                                    isShieldServiceRunning = isShieldServiceRunning,
                                    shieldDurationSeconds = shieldDurationSeconds,
                                    hasUsageStatsPermission = viewModel.hasUsageStatsPermission(),
                                    onToggleShieldService = { viewModel.toggleDistractionShield() },
                                    onRequestUsageStatsPermission = { viewModel.openUsageStatsSettings() },
                                    onToggleStudyApp = { pkg, enabled -> viewModel.toggleStudyApp(pkg, enabled) },
                                    onAddStudyApp = { pkg, name, cat, uri -> viewModel.addStudyApp(pkg, name, cat, uri) },
                                    onDeleteStudyApp = { pkg -> viewModel.deleteStudyApp(pkg) },
                                    onSimulateNavigatedAway = { appName, pkg -> viewModel.simulateDistractionAlert(appName, pkg) },
                                    onStartTimer = { viewModel.startFocusTimer() },
                                    onPauseTimer = { viewModel.pauseFocusTimer() },
                                    onResetTimer = { viewModel.resetFocusTimer() },
                                    onSetDuration = { mins, goal -> viewModel.setFocusDuration(mins, goal) },
                                    onToggleBlock = { pkg, active -> viewModel.toggleAppBlock(pkg, active) },
                                    onAddBlockedApp = { pkg, name, cat, limit, strict ->
                                        viewModel.addBlockedApp(pkg, name, cat, limit, strict)
                                    },
                                    onDeleteBlockedApp = { viewModel.deleteBlockedApp(it) },
                                    onSimulateBlock = { appName, pkg -> viewModel.triggerSimulatedDistraction(appName, pkg) }
                                )
                            }
                        }
                    }
                }
                NavigationTab.SYSTEM_CONNECTORS -> {
                    SystemConnectorsHistoryScreen(
                        authState = authState,
                        studyHistory = studyHistory,
                        appConnectors = appConnectors,
                        themeMode = themeMode,
                        onSetThemeMode = { viewModel.setThemeMode(it) },
                        isDndEnabled = isDndEnabled,
                        isKeepScreenOn = isKeepScreenOn,
                        isDailyMotivationalFcmEnabled = isDailyMotivationalFcmEnabled,
                        isStudyAlertsFcmEnabled = isStudyAlertsFcmEnabled,
                        onSignInWithGoogle = { viewModel.signInWithGoogle() },
                        onSignOut = { viewModel.signOutUser() },
                        onToggleDnd = { viewModel.toggleDnd(it) },
                        onToggleKeepScreenOn = { viewModel.toggleKeepScreenOn(it) },
                        onToggleDailyMotivationalFcm = { viewModel.toggleDailyMotivationalFcm(it) },
                        onToggleStudyAlertsFcm = { viewModel.toggleStudyAlertsFcm(it) },
                        onTriggerTestMotivation = { viewModel.triggerTestMotivationNotification() },
                        onTriggerTestStudyAlert = { viewModel.triggerTestStudyAlert() },
                        onToggleConnector = { viewModel.toggleAppConnector(it) },
                        onDeleteHistoryItem = { viewModel.deleteHistoryItem(it) },
                        onClearAllHistory = { viewModel.clearAllHistory() },
                        onLogManualHistory = { type, title, duration, notes ->
                            viewModel.logStudyHistory(type, title, duration, notes)
                        }
                    )
                }
            }

            // Distraction Intercept Dialog Overlay
            simulatedBlockedApp?.let { appName ->
                DistractionBlockedDialog(
                    appName = appName,
                    onDismiss = { viewModel.clearSimulatedDistraction() }
                )
            }

            // Distraction Shield Navigated-Away Alert Modal
            lastDistractionAlert?.let { alertEvent ->
                DistractionAlertModal(
                    event = alertEvent,
                    onDismiss = { viewModel.dismissDistractionAlert() },
                    onAllowAsStudyApp = { pkg, name ->
                        viewModel.allowDistractedAppAsStudyApp(pkg, name)
                    }
                )
            }

            // Edit Profile Dialog
            if (showEditProfileDialog) {
                EditProfileDialog(
                    profile = profile,
                    onDismiss = { showEditProfileDialog = false },
                    onConfirm = { name, dream, desc, year, hours ->
                        viewModel.updateProfile(name, dream, desc, year, hours)
                        showEditProfileDialog = false
                    }
                )
            }

            // Gamification, Badges & Leaderboard Modal
            if (showGamificationModal) {
                GamificationLeaderboardModal(
                    totalXp = studentXp,
                    profile = profile,
                    badges = badgesList,
                    leaderboardEntries = leaderboard,
                    isLeaderboardEnabled = isLeaderboardEnabled,
                    onToggleLeaderboard = { viewModel.toggleLeaderboard(it) },
                    onDismiss = { showGamificationModal = false }
                )
            }

            // Customization Center Modal
            if (showCustomizationModal) {
                CustomizationCenterModal(
                    themeMode = themeMode,
                    onSetThemeMode = { viewModel.setThemeMode(it) },
                    currentAvatarEmoji = studentAvatarEmoji,
                    onSetAvatarEmoji = { viewModel.setStudentAvatarEmoji(it) },
                    currentPlayerTitle = customPlayerTitle,
                    onSetPlayerTitle = { viewModel.setCustomPlayerTitle(it) },
                    isLeaderboardEnabled = isLeaderboardEnabled,
                    onToggleLeaderboard = { viewModel.toggleLeaderboard(it) },
                    onExecuteAiCommand = { viewModel.sendChatMessage(it) },
                    onDismiss = { showCustomizationModal = false }
                )
            }
        }
    }
}

