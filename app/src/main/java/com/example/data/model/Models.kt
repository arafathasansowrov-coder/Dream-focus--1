package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Student profile containing the ultimate dream career, target deadline,
 * streak records, and focus stats.
 */
@Entity(tableName = "student_profile")
data class StudentProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Future Leader",
    val dreamTitle: String = "AI Engineer & Innovator",
    val dreamDescription: String = "Build cutting-edge autonomous agents and neural networks that change how humanity learns.",
    val targetYear: Int = 2028,
    val dailyTargetHours: Float = 4.0f,
    val currentStreak: Int = 12,
    val longestStreak: Int = 28,
    val totalFocusMinutes: Int = 3450,
    val completedMilestonesCount: Int = 19,
    val isCloudSyncEnabled: Boolean = false,
    val fcmToken: String = ""
)

/**
 * Long-term goals & macro milestones for the student's dream.
 */
@Entity(tableName = "goals")
data class GoalItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // "AI & Tech", "Academics", "Content Creation", "Skills", "Health"
    val priority: String = "High", // "High", "Medium", "Low"
    val deadlineDate: String, // e.g. "2026-12-31"
    val progressPercentage: Int = 0, // 0 - 100
    val milestonesJson: String = "", // JSON string list of sub-tasks e.g. "Learn Calculus|Study PyTorch|Build Portfolio"
    val isCompleted: Boolean = false,
    val colorHex: Long = 0xFF6366F1
)

/**
 * Actionable milestone step for breaking down a high-level goal into bite-sized tasks.
 */
@Entity(tableName = "goal_milestones")
data class GoalMilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long,
    val title: String,
    val actionNotes: String = "",
    val targetDate: String = "",
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0
)

/**
 * Daily routine item or scheduled study habit with dual reminder customization.
 */
@Entity(tableName = "routines")
data class RoutineItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val scheduledTime: String = "08:00 AM", // "06:30 AM", "04:00 PM"
    val category: String = "Deep Work", // "Deep Work", "Coding", "Study Block", "Revision", "Morning Ritual"
    val durationMinutes: Int = 45,
    val isCompletedToday: Boolean = false,
    val alarmEnabled: Boolean = true,
    val daysOfWeek: String = "Mon,Tue,Wed,Thu,Fri,Sat,Sun",
    val reminderType: String = "DIRECT_ALARM", // "PSYCHOLOGICAL_MESSAGE", "DIRECT_ALARM", "BOTH", "NONE"
    val psychologicalTone: String = "Growth Mindset & Discipline", // "Growth Mindset & Discipline", "Gentle Encouragement", "High-Energy Hype", "Stoic Resilience", "Focus & Calm"
    val alarmSound: String = "Chime", // "Chime", "Urgent Siren", "Steady Pulse"
    val snoozeMinutes: Int = 5,
    val customPsychologicalMessage: String = ""
)

/**
 * Blocked application configuration for distraction-free focus sessions.
 */
@Entity(tableName = "blocked_apps")
data class BlockedAppRule(
    @PrimaryKey val packageName: String,
    val appName: String,
    val category: String = "Social & Entertainment",
    val dailyLimitMinutes: Int = 30,
    val usedTodayMinutes: Int = 0,
    val isBlockActiveDuringFocus: Boolean = true,
    val isStrictLocked: Boolean = false,
    val timesBlockedToday: Int = 0
)

/**
 * Approved study application allowed during Distraction Shield focus lock.
 * If user navigates away from designated study apps to an unapproved app, Distraction Shield alerts the student.
 */
@Entity(tableName = "designated_study_apps")
data class DesignatedStudyApp(
    @PrimaryKey val packageName: String,
    val appName: String,
    val category: String = "Study Tool", // "Study Tool", "Notes & Docs", "Coding & Tech", "Research", "Math & STEM"
    val isEnabled: Boolean = true,
    val actionUri: String = "",
    val isSystemPreinstalled: Boolean = true,
    val addedTimestamp: Long = System.currentTimeMillis()
)

/**
 * Distraction alert incident event triggered by Distraction Shield foreground service.
 */
data class DistractionAlertEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val appName: String,
    val packageName: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val reason: String = "Navigated away from designated study apps"
)

/**
 * Motivational quote item categorized for different ambitions.
 */
data class MotivationQuote(
    val id: Int,
    val quote: String,
    val author: String,
    val category: String, // "AI & Engineering", "Grit & Discipline", "Content Creation", "Focus"
    val tags: String
)

/**
 * Historical record of completed study sessions, routines, and milestones.
 */
@Entity(tableName = "study_history")
data class StudyHistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionType: String = "Focus Sprint", // "Focus Sprint", "Routine Checkpoint", "Goal Milestone", "AI Study Sprint"
    val title: String,
    val durationMinutes: Int = 25,
    val timestampMillis: Long = System.currentTimeMillis(),
    val dateString: String = "",
    val notes: String = "",
    val isSyncedToCloud: Boolean = false
)

/**
 * Personal opinions, thoughts, and philosophical reflections written by the student.
 */
@Entity(tableName = "journal_opinions")
data class JournalOpinionItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val opinionText: String,
    val category: String = "Personal Opinion", // "Personal Opinion", "Study Philosophy", "Strategy Reflection", "Mindset"
    val mood: String = "Focused", // "Focused", "Determined", "Creative", "Energized", "Calm"
    val dateCreated: String = "",
    val timestampMillis: Long = System.currentTimeMillis()
)

/**
 * Student's custom operating instructions and directives for study habits and AI customization.
 */
@Entity(tableName = "custom_instructions")
data class CustomInstructionItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val instructionText: String,
    val targetScope: String = "AI Tutor Directives", // "AI Tutor Directives", "Study Sprint Rule", "Daily Habit Rule", "Exam Prep"
    val isActive: Boolean = true,
    val priorityLevel: Int = 1
)

/**
 * External app connector link item (Google Calendar, GitHub, Notion, YouTube, Anki, etc.)
 */
data class AppConnectorItem(
    val id: String,
    val name: String,
    val category: String, // "Schedule & Planning", "Development & Portfolio", "Notes & Docs", "Focus & Ambient", "Flashcards"
    val description: String,
    val actionUri: String,
    val packageName: String = "",
    val isConnected: Boolean = true,
    val accentColorHex: Long = 0xFF6366F1
)

/**
 * Firebase user profile state for Google Sign-in.
 */
data class UserAuthState(
    val isAuthenticated: Boolean = false,
    val userId: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false
)
