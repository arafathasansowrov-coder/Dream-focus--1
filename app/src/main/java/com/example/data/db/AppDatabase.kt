package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        StudentProfile::class,
        GoalItem::class,
        GoalMilestoneEntity::class,
        RoutineItem::class,
        BlockedAppRule::class,
        DesignatedStudyApp::class,
        StudyHistoryItem::class,
        JournalOpinionItem::class,
        CustomInstructionItem::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun goalDao(): GoalDao
    abstract fun goalMilestoneDao(): GoalMilestoneDao
    abstract fun routineDao(): RoutineDao
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun studyAppDao(): StudyAppDao
    abstract fun studyHistoryDao(): StudyHistoryDao
    abstract fun journalOpinionDao(): JournalOpinionDao
    abstract fun customInstructionDao(): CustomInstructionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dreamtrack_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            // Initial Student Profile
            database.profileDao().saveProfile(
                StudentProfile(
                    id = 1,
                    name = "Alex Vance",
                    dreamTitle = "Lead AI Research Scientist",
                    dreamDescription = "Master deep learning architectures, publish in NeurIPS, and build autonomous AI agents.",
                    targetYear = 2028,
                    dailyTargetHours = 4.5f,
                    currentStreak = 7,
                    longestStreak = 21,
                    totalFocusMinutes = 2540,
                    completedMilestonesCount = 14
                )
            )

            // Initial Goals
            database.goalDao().insertGoal(
                GoalItem(
                    title = "Master Mathematics for Machine Learning",
                    category = "AI & Tech",
                    priority = "High",
                    deadlineDate = "2026-10-30",
                    progressPercentage = 75,
                    milestonesJson = "Linear Algebra (Gilbert Strang)|Multivariable Calculus|Probability & Statistics|PCA & Matrix Factorization",
                    isCompleted = false,
                    colorHex = 0xFF6366F1
                )
            )
            database.goalDao().insertGoal(
                GoalItem(
                    title = "Build & Deploy 3 End-to-End LLM Projects",
                    category = "AI & Tech",
                    priority = "High",
                    deadlineDate = "2026-12-15",
                    progressPercentage = 40,
                    milestonesJson = "RAG with Vector DB|Fine-tune Llama with LoRA|Multi-agent Coding Assistant",
                    isCompleted = false,
                    colorHex = 0xFF06B6D4
                )
            )
            database.goalDao().insertGoal(
                GoalItem(
                    title = "Publish Weekly AI Breakdown & YouTube Video",
                    category = "Content Creation",
                    priority = "Medium",
                    deadlineDate = "2026-11-20",
                    progressPercentage = 30,
                    milestonesJson = "Script Attention Mechanism|Record Voiceover & Diagrams|Edit 4K Video|Create Thumbnail",
                    isCompleted = false,
                    colorHex = 0xFFEC4899
                )
            )

            // Initial Routines
            database.routineDao().insertRoutine(
                RoutineItem(
                    title = "Deep Focus: Neural Networks & Coding",
                    scheduledTime = "06:30 AM",
                    category = "Deep Work",
                    durationMinutes = 90,
                    isCompletedToday = true,
                    alarmEnabled = true
                )
            )
            database.routineDao().insertRoutine(
                RoutineItem(
                    title = "Algorithmic Problem Solving (LeetCode / Math)",
                    scheduledTime = "02:00 PM",
                    category = "Coding",
                    durationMinutes = 60,
                    isCompletedToday = false,
                    alarmEnabled = true
                )
            )
            database.routineDao().insertRoutine(
                RoutineItem(
                    title = "Research Paper Reading & Summarizing",
                    scheduledTime = "05:30 PM",
                    category = "Study Block",
                    durationMinutes = 45,
                    isCompletedToday = false,
                    alarmEnabled = false
                )
            )
            database.routineDao().insertRoutine(
                RoutineItem(
                    title = "Evening Reflection & Next Day Planning",
                    scheduledTime = "09:30 PM",
                    category = "Morning Ritual",
                    durationMinutes = 15,
                    isCompletedToday = false,
                    alarmEnabled = true
                )
            )

            // Initial Blocked Apps
            database.blockedAppDao().insertOrUpdateBlockedApp(
                BlockedAppRule(
                    packageName = "com.google.android.youtube",
                    appName = "YouTube",
                    category = "Video & Streaming",
                    dailyLimitMinutes = 30,
                    usedTodayMinutes = 12,
                    isBlockActiveDuringFocus = true,
                    isStrictLocked = true,
                    timesBlockedToday = 4
                )
            )
            database.blockedAppDao().insertOrUpdateBlockedApp(
                BlockedAppRule(
                    packageName = "com.instagram.android",
                    appName = "Instagram",
                    category = "Social Media",
                    dailyLimitMinutes = 20,
                    usedTodayMinutes = 18,
                    isBlockActiveDuringFocus = true,
                    isStrictLocked = false,
                    timesBlockedToday = 6
                )
            )
            database.blockedAppDao().insertOrUpdateBlockedApp(
                BlockedAppRule(
                    packageName = "com.zhiliaoapp.musically",
                    appName = "TikTok",
                    category = "Short Video",
                    dailyLimitMinutes = 15,
                    usedTodayMinutes = 5,
                    isBlockActiveDuringFocus = true,
                    isStrictLocked = true,
                    timesBlockedToday = 2
                )
            )
            database.blockedAppDao().insertOrUpdateBlockedApp(
                BlockedAppRule(
                    packageName = "com.twitter.android",
                    appName = "X (Twitter)",
                    category = "Social Feeds",
                    dailyLimitMinutes = 25,
                    usedTodayMinutes = 8,
                    isBlockActiveDuringFocus = true,
                    isStrictLocked = false,
                    timesBlockedToday = 1
                )
            )

            // Initial Designated Study Apps (Approved whitelist for Distraction Shield)
            database.studyAppDao().insertStudyApps(
                listOf(
                    DesignatedStudyApp(
                        packageName = "com.aistudio.dreamtrack.stdupk",
                        appName = "DreamTrack (Active App)",
                        category = "Study Tool",
                        isEnabled = true,
                        actionUri = ""
                    ),
                    DesignatedStudyApp(
                        packageName = "com.google.android.apps.docs",
                        appName = "Google Docs & Drive",
                        category = "Notes & Docs",
                        isEnabled = true,
                        actionUri = "https://docs.google.com"
                    ),
                    DesignatedStudyApp(
                        packageName = "com.google.android.apps.classroom",
                        appName = "Google Classroom",
                        category = "Study Tool",
                        isEnabled = true,
                        actionUri = "https://classroom.google.com"
                    ),
                    DesignatedStudyApp(
                        packageName = "com.ichi2.anki",
                        appName = "AnkiDroid Flashcards",
                        category = "Flashcards",
                        isEnabled = true,
                        actionUri = "https://ankiweb.net"
                    ),
                    DesignatedStudyApp(
                        packageName = "com.notion.id",
                        appName = "Notion Notes & Syllabus",
                        category = "Notes & Docs",
                        isEnabled = true,
                        actionUri = "https://notion.so"
                    ),
                    DesignatedStudyApp(
                        packageName = "com.github.android",
                        appName = "GitHub Code & Repos",
                        category = "Coding & Tech",
                        isEnabled = true,
                        actionUri = "https://github.com"
                    ),
                    DesignatedStudyApp(
                        packageName = "com.adobe.reader",
                        appName = "Adobe Acrobat (Textbooks & PDFs)",
                        category = "Research",
                        isEnabled = true,
                        actionUri = ""
                    ),
                    DesignatedStudyApp(
                        packageName = "com.google.android.calculator",
                        appName = "Scientific Calculator",
                        category = "Math & STEM",
                        isEnabled = true,
                        actionUri = ""
                    )
                )
            )

            // Initial Study History Log
            database.studyHistoryDao().insertHistory(
                StudyHistoryItem(
                    sessionType = "Focus Sprint",
                    title = "Attention Mechanisms & Transformer Deep Dive",
                    durationMinutes = 50,
                    timestampMillis = System.currentTimeMillis() - 3600000 * 4,
                    dateString = "Today, 08:30 AM",
                    notes = "Completed Section 3 math breakdown on multi-head scaled dot product.",
                    isSyncedToCloud = true
                )
            )
            database.studyHistoryDao().insertHistory(
                StudyHistoryItem(
                    sessionType = "Routine Checkpoint",
                    title = "Daily Morning Ritual & Mindset Alignment",
                    durationMinutes = 20,
                    timestampMillis = System.currentTimeMillis() - 3600000 * 7,
                    dateString = "Today, 06:30 AM",
                    notes = "Calibrated goals, reviewed daily checklist.",
                    isSyncedToCloud = true
                )
            )
            database.studyHistoryDao().insertHistory(
                StudyHistoryItem(
                    sessionType = "Goal Milestone",
                    title = "Multivariable Calculus Problem Set Completed",
                    durationMinutes = 90,
                    timestampMillis = System.currentTimeMillis() - 86400000,
                    dateString = "Yesterday, 04:15 PM",
                    notes = "Matrix eigenvalues and Jacobians review for ML optimizer proofs.",
                    isSyncedToCloud = true
                )
            )

            // Initial Personal Opinions & Philosophy
            database.journalOpinionDao().insertOpinion(
                JournalOpinionItem(
                    title = "Consistency Over Intensity: The Compound Rule",
                    opinionText = "In engineering and mathematics, one uninterrupted 90-minute block every single morning beats a sporadic 8-hour marathon on weekends. Habit architecture is destiny.",
                    category = "Study Philosophy",
                    mood = "Focused",
                    dateCreated = "2026-08-15"
                )
            )
            database.journalOpinionDao().insertOpinion(
                JournalOpinionItem(
                    title = "Why First Principles Thinking Matters for AI",
                    opinionText = "Don't just copy library APIs. Derive the loss function and backpropagation by hand at least once. Deep understanding is the only real competitive moat.",
                    category = "Personal Opinion",
                    mood = "Determined",
                    dateCreated = "2026-08-14"
                )
            )

            // Initial Custom Instructions & Directives
            database.customInstructionDao().insertInstruction(
                CustomInstructionItem(
                    title = "Strict Sprint Protocol",
                    instructionText = "Keep phone in another room or in airplane mode. Zero notifications allowed during 45-minute focus intervals.",
                    targetScope = "Study Sprint Rule",
                    isActive = true,
                    priorityLevel = 1
                )
            )
            database.customInstructionDao().insertInstruction(
                CustomInstructionItem(
                    title = "Gemini AI Tutor Style: Socratic & Rigorous",
                    instructionText = "When answering my questions, ask clarifying conceptual questions first. Break complex computer science algorithms down mathematically before showing code.",
                    targetScope = "AI Tutor Directives",
                    isActive = true,
                    priorityLevel = 1
                )
            )
            database.customInstructionDao().insertInstruction(
                CustomInstructionItem(
                    title = "Active Recall over Passive Rereading",
                    instructionText = "After studying any topic, spend 5 minutes summarizing key theorems on blank paper before opening notes.",
                    targetScope = "Daily Habit Rule",
                    isActive = true,
                    priorityLevel = 2
                )
            )
        }
    }
}
