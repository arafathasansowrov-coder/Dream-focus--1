package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductivityRepository(private val database: AppDatabase) {

    val profile: Flow<StudentProfile> = database.profileDao().getProfile().map {
        it ?: StudentProfile()
    }

    val allGoals: Flow<List<GoalItem>> = database.goalDao().getAllGoals()
    val allRoutines: Flow<List<RoutineItem>> = database.routineDao().getAllRoutines()
    val allBlockedApps: Flow<List<BlockedAppRule>> = database.blockedAppDao().getAllBlockedApps()
    val allStudyApps: Flow<List<DesignatedStudyApp>> = database.studyAppDao().getAllStudyApps()
    val allStudyHistory: Flow<List<StudyHistoryItem>> = database.studyHistoryDao().getAllHistory()
    val allJournalOpinions: Flow<List<JournalOpinionItem>> = database.journalOpinionDao().getAllOpinions()
    val allCustomInstructions: Flow<List<CustomInstructionItem>> = database.customInstructionDao().getAllInstructions()
    val activeCustomInstructions: Flow<List<CustomInstructionItem>> = database.customInstructionDao().getActiveInstructions()

    suspend fun saveProfile(profile: StudentProfile) {
        database.profileDao().saveProfile(profile)
    }

    suspend fun addFocusTime(minutes: Int) {
        database.profileDao().addFocusTime(minutes)
    }

    suspend fun insertGoal(goal: GoalItem): Long = database.goalDao().insertGoal(goal)

    suspend fun updateGoal(goal: GoalItem) = database.goalDao().updateGoal(goal)

    suspend fun deleteGoal(goal: GoalItem) = database.goalDao().deleteGoal(goal)

    suspend fun updateGoalProgress(id: Long, progress: Int, isCompleted: Boolean) =
        database.goalDao().updateGoalProgress(id, progress, isCompleted)

    suspend fun insertRoutine(routine: RoutineItem): Long = database.routineDao().insertRoutine(routine)

    suspend fun updateRoutine(routine: RoutineItem) = database.routineDao().updateRoutine(routine)

    suspend fun deleteRoutine(routine: RoutineItem) = database.routineDao().deleteRoutine(routine)

    suspend fun toggleRoutineCompletion(id: Long, isCompleted: Boolean) =
        database.routineDao().toggleRoutineCompletion(id, isCompleted)

    suspend fun toggleRoutineAlarm(id: Long, enabled: Boolean) =
        database.routineDao().toggleAlarm(id, enabled)

    suspend fun insertOrUpdateBlockedApp(app: BlockedAppRule) =
        database.blockedAppDao().insertOrUpdateBlockedApp(app)

    suspend fun deleteBlockedApp(app: BlockedAppRule) =
        database.blockedAppDao().deleteBlockedApp(app)

    suspend fun toggleAppBlock(packageName: String, isActive: Boolean) =
        database.blockedAppDao().toggleAppBlock(packageName, isActive)

    suspend fun recordBlockAttempt(packageName: String) =
        database.blockedAppDao().recordBlockAttempt(packageName)

    // Designated Study Apps methods
    suspend fun insertOrUpdateStudyApp(app: DesignatedStudyApp) =
        database.studyAppDao().insertOrUpdateStudyApp(app)

    suspend fun toggleStudyApp(packageName: String, isEnabled: Boolean) =
        database.studyAppDao().toggleStudyApp(packageName, isEnabled)

    suspend fun deleteStudyApp(packageName: String) =
        database.studyAppDao().deleteStudyAppByPackage(packageName)

    suspend fun getEnabledStudyApps(): List<DesignatedStudyApp> =
        database.studyAppDao().getEnabledStudyAppsSync()

    // History methods
    suspend fun logStudySession(type: String, title: String, durationMinutes: Int, notes: String): Long {
        return database.studyHistoryDao().insertHistory(
            StudyHistoryItem(
                sessionType = type,
                title = title,
                durationMinutes = durationMinutes,
                timestampMillis = System.currentTimeMillis(),
                dateString = "Today",
                notes = notes,
                isSyncedToCloud = true
            )
        )
    }

    suspend fun deleteHistory(item: StudyHistoryItem) = database.studyHistoryDao().deleteHistory(item)
    suspend fun clearHistory() = database.studyHistoryDao().clearAllHistory()

    // Journal & Opinion methods
    suspend fun insertJournalOpinion(title: String, text: String, category: String, mood: String): Long {
        return database.journalOpinionDao().insertOpinion(
            JournalOpinionItem(
                title = title,
                opinionText = text,
                category = category,
                mood = mood,
                dateCreated = "Today"
            )
        )
    }

    suspend fun updateJournalOpinion(item: JournalOpinionItem) = database.journalOpinionDao().updateOpinion(item)
    suspend fun deleteJournalOpinion(item: JournalOpinionItem) = database.journalOpinionDao().deleteOpinion(item)

    // Custom Instruction methods
    suspend fun insertCustomInstruction(title: String, instructionText: String, targetScope: String): Long {
        return database.customInstructionDao().insertInstruction(
            CustomInstructionItem(
                title = title,
                instructionText = instructionText,
                targetScope = targetScope,
                isActive = true
            )
        )
    }

    suspend fun updateCustomInstruction(item: CustomInstructionItem) = database.customInstructionDao().updateInstruction(item)
    suspend fun deleteCustomInstruction(item: CustomInstructionItem) = database.customInstructionDao().deleteInstruction(item)
    suspend fun toggleCustomInstruction(id: Long, isActive: Boolean) =
        database.customInstructionDao().toggleInstructionActive(id, isActive)

    fun getDefaultAppConnectors(): List<AppConnectorItem> = listOf(
        AppConnectorItem(
            id = "google_calendar",
            name = "Google Calendar",
            category = "Schedule & Planning",
            description = "Sync study routines and exam blocks directly into your daily calendar.",
            actionUri = "https://calendar.google.com",
            packageName = "com.google.android.calendar",
            isConnected = true,
            accentColorHex = 0xFF4285F4
        ),
        AppConnectorItem(
            id = "github",
            name = "GitHub Student Hub",
            category = "Development & Portfolio",
            description = "Connect student repositories, commit streak tracker, and open-source contributions.",
            actionUri = "https://github.com",
            packageName = "com.github.android",
            isConnected = true,
            accentColorHex = 0xFF6366F1
        ),
        AppConnectorItem(
            id = "notion_docs",
            name = "Notion & Docs",
            category = "Notes & Docs",
            description = "Link your study syllabus, research notes, and thesis outlines.",
            actionUri = "https://www.notion.so",
            packageName = "notion.id",
            isConnected = true,
            accentColorHex = 0xFF06B6D4
        ),
        AppConnectorItem(
            id = "youtube_lofi",
            name = "Focus Audio & Lofi",
            category = "Focus & Ambient",
            description = "1-click launch binaural beats, ambient rainfall, and deep work study playlists.",
            actionUri = "https://www.youtube.com/results?search_query=lofi+study+beats",
            packageName = "com.google.android.youtube",
            isConnected = true,
            accentColorHex = 0xFFEF4444
        ),
        AppConnectorItem(
            id = "spotify_focus",
            name = "Spotify Focus Soundscapes",
            category = "Focus & Ambient",
            description = "Binaural alpha waves, instrumental soundscapes, and deep concentration playlists.",
            actionUri = "https://open.spotify.com/genre/focus-page",
            packageName = "com.spotify.music",
            isConnected = true,
            accentColorHex = 0xFF1DB954
        ),
        AppConnectorItem(
            id = "leetcode_hub",
            name = "LeetCode & Problem Solving",
            category = "Algorithms & Coding",
            description = "Solve daily coding challenges, data structures, and algorithmic problems.",
            actionUri = "https://leetcode.com/problemset/all/",
            packageName = "com.leetcode",
            isConnected = true,
            accentColorHex = 0xFFFFA116
        ),
        AppConnectorItem(
            id = "google_drive",
            name = "Google Drive & Docs",
            category = "Cloud Storage & PDF",
            description = "Access textbook PDFs, research papers, lecture slides, and shared study drives.",
            actionUri = "https://drive.google.com",
            packageName = "com.google.android.apps.docs",
            isConnected = true,
            accentColorHex = 0xFF34A853
        ),
        AppConnectorItem(
            id = "discord_study",
            name = "Discord Study Groups",
            category = "Community & Voice",
            description = "Join virtual co-working voice rooms, student hackathons, and AI discussion lounges.",
            actionUri = "https://discord.com/app",
            packageName = "com.discord",
            isConnected = false,
            accentColorHex = 0xFF5865F2
        ),
        AppConnectorItem(
            id = "anki_cards",
            name = "Anki Flashcards",
            category = "Flashcards",
            description = "Spaced repetition flashcard reviews for math formulas and CS theory.",
            actionUri = "https://ankiweb.net",
            packageName = "com.ichi2.anki",
            isConnected = false,
            accentColorHex = 0xFF10B981
        )
    )

    fun getMotivationalQuotes(): List<MotivationQuote> = listOf(
        MotivationQuote(
            id = 1,
            quote = "The future belongs to those who learn more skills and combine them in creative ways.",
            author = "Robert Greene",
            category = "AI & Engineering",
            tags = "Mastery, Skills, Future"
        ),
        MotivationQuote(
            id = 2,
            quote = "We overestimate what we can do in a day and underestimate what we can do in a decade.",
            author = "Bill Gates",
            category = "Grit & Discipline",
            tags = "Vision, Compounding"
        ),
        MotivationQuote(
            id = 3,
            quote = "Discipline is choosing between what you want now and what you want most.",
            author = "Abraham Lincoln",
            category = "Focus",
            tags = "Willpower, Goals"
        ),
        MotivationQuote(
            id = 4,
            quote = "Every line of code you write and every problem you solve is an investment in your freedom.",
            author = "Tech Innovators",
            category = "AI & Engineering",
            tags = "Coding, Freedom"
        ),
        MotivationQuote(
            id = 5,
            quote = "You don't have to be great to start, but you have to start to be great.",
            author = "Zig Ziglar",
            category = "Content Creation",
            tags = "Action, Momentum"
        ),
        MotivationQuote(
            id = 6,
            quote = "Deep Work is the superpower of the 21st century. Those who master distraction rule their domain.",
            author = "Cal Newport",
            category = "Focus",
            tags = "Deep Work, Focus"
        ),
        MotivationQuote(
            id = 7,
            quote = "Small daily improvements over time lead to stunning results.",
            author = "Robin Sharma",
            category = "Grit & Discipline",
            tags = "Consistency, Daily Habit"
        )
    )
}
