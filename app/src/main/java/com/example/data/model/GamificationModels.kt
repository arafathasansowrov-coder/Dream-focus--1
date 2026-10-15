package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Common student task point values.
 * Gives concrete dopamine rewards for productive academic habits.
 */
object StudentTaskPoints {
    const val FOCUS_SPRINT_25_MIN = 50       // 25-minute Pomodoro focus session
    const val FOCUS_SPRINT_50_MIN = 120      // 50-minute Deep Work sprint
    const val DAILY_ROUTINE_DONE = 30        // Complete scheduled routine habit
    const val ALL_ROUTINES_COMPLETED = 100   // 100% daily routine streak
    const val GOAL_MILESTONE_DONE = 100      // Achieve milestone sub-task
    const val GOAL_QUEST_LEVEL_UP = 150      // Advance level in RPG Goal Quest
    const val BOSS_TRIAL_COMPLETED = 300     // Finish Boss Challenge in Quest
    const val JOURNAL_OPINION_LOGGED = 25    // Write reflection or study philosophy
    const val DISTRACTION_SHIELD_DEFENSE = 40 // Resist opening blocked app
    const val STREAK_BONUS_3_DAY = 50        // 3-day consecutive study streak
    const val STREAK_BONUS_7_DAY = 150       // 7-day milestone streak
}

/**
 * Category for unlockable student achievements.
 */
enum class BadgeCategory(val displayName: String, val icon: String) {
    FOCUS("Deep Focus", "🎯"),
    CONSISTENCY("Streak & Grit", "🔥"),
    MASTERY("Goal Conquest", "⚔️"),
    SCHOLAR("Mindset & Logic", "🧠"),
    CHAMPION("Legendary Ranks", "👑")
}

/**
 * Unlockable badge representing milestones achieved in the app.
 */
data class BadgeItem(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val category: BadgeCategory,
    val requiredXp: Int,
    val criteriaDescription: String,
    val isUnlocked: Boolean = false,
    val unlockedDate: String? = null,
    val progressPercent: Int = 0
)

/**
 * Leaderboard entry for peer competition and personal benchmarking.
 */
data class LeaderboardEntry(
    val rank: Int,
    val studentName: String,
    val dreamGoal: String,
    val totalPoints: Int,
    val streakDays: Int,
    val badgesCount: Int,
    val avatarEmoji: String,
    val isCurrentUser: Boolean = false
)

/**
 * Commands that AI can parse and execute across the entire app.
 */
sealed class AiAppAction(val description: String) {
    data class CreateGoal(val title: String, val category: String) : AiAppAction("Created Goal: $title")
    data class CreateRoutine(val title: String, val time: String, val category: String) : AiAppAction("Created Routine: $title at $time")
    data class StartFocusTimer(val minutes: Int) : AiAppAction("Started $minutes-min Focus Session")
    data class StopFocusTimer(val reason: String = "User requested") : AiAppAction("Focus timer stopped")
    data class ToggleFocusShield(val enable: Boolean) : AiAppAction(if (enable) "Distraction Shield Activated 🛡️" else "Distraction Shield Deactivated")
    data class SwitchTheme(val modeName: String) : AiAppAction("Switched Theme to $modeName")
    data class AwardPoints(val points: Int, val reason: String) : AiAppAction("Awarded +$points XP ($reason)")
    data class SetDreamCareer(val dreamTitle: String) : AiAppAction("Dream career updated to $dreamTitle")
    data class ToggleLeaderboard(val enable: Boolean) : AiAppAction(if (enable) "Leaderboard Enabled" else "Leaderboard Muted")
    data class DismissFeature(val featureName: String) : AiAppAction("Dismissed / Reset: $featureName")
    data class GeneralResponse(val message: String) : AiAppAction("Message Processed")
}

/**
 * Initial set of 8 milestone badges defined for student productivity.
 */
object InitialBadgesList {
    fun createDefaultBadges(currentXp: Int, currentStreak: Int, completedMilestones: Int): List<BadgeItem> {
        return listOf(
            BadgeItem(
                id = "first_spark",
                title = "First Spark",
                description = "Ignite your study journey by logging your first completed routine or focus sprint.",
                emoji = "🌟",
                category = BadgeCategory.FOCUS,
                requiredXp = 50,
                criteriaDescription = "Earn 50 XP in study tasks",
                isUnlocked = currentXp >= 50,
                unlockedDate = if (currentXp >= 50) "Achieved" else null,
                progressPercent = (currentXp * 100 / 50).coerceIn(0, 100)
            ),
            BadgeItem(
                id = "deep_focus_novice",
                title = "Deep Focus Novice",
                description = "Enter the flow state by accumulating over 2 hours (120 mins) of focus work.",
                emoji = "⚡",
                category = BadgeCategory.FOCUS,
                requiredXp = 250,
                criteriaDescription = "Earn 250 XP through focus sprints",
                isUnlocked = currentXp >= 250,
                unlockedDate = if (currentXp >= 250) "Achieved" else null,
                progressPercent = (currentXp * 100 / 250).coerceIn(0, 100)
            ),
            BadgeItem(
                id = "zen_shield_master",
                title = "Zen Shield Master",
                description = "Shield your mind against distractions using the background Focus Shield.",
                emoji = "🛡️",
                category = BadgeCategory.FOCUS,
                requiredXp = 400,
                criteriaDescription = "Earn 400 XP while resisting distractions",
                isUnlocked = currentXp >= 400,
                unlockedDate = if (currentXp >= 400) "Achieved" else null,
                progressPercent = (currentXp * 100 / 400).coerceIn(0, 100)
            ),
            BadgeItem(
                id = "streak_flame_7",
                title = "Streak Flame 7",
                description = "Demonstrate unbreakable discipline with a 7-day study streak.",
                emoji = "🔥",
                category = BadgeCategory.CONSISTENCY,
                requiredXp = 500,
                criteriaDescription = "Maintain 7+ day study streak",
                isUnlocked = currentStreak >= 7,
                unlockedDate = if (currentStreak >= 7) "7-Day Streak Achieved" else null,
                progressPercent = (currentStreak * 100 / 7).coerceIn(0, 100)
            ),
            BadgeItem(
                id = "milestone_conqueror",
                title = "Milestone Conqueror",
                description = "Break big dreams into victories by completing 5 major goal milestones.",
                emoji = "🏆",
                category = BadgeCategory.MASTERY,
                requiredXp = 750,
                criteriaDescription = "Complete 5 goal milestones",
                isUnlocked = completedMilestones >= 5 || currentXp >= 750,
                unlockedDate = if (completedMilestones >= 5 || currentXp >= 750) "Milestones Conquered" else null,
                progressPercent = ((completedMilestones * 100 / 5).coerceAtLeast(currentXp * 100 / 750)).coerceIn(0, 100)
            ),
            BadgeItem(
                id = "boss_slayer_rpg",
                title = "Boss Slayer",
                description = "Conquer an AI-generated Boss Trial in the RPG Gamified Goal Completer.",
                emoji = "⚔️",
                category = BadgeCategory.MASTERY,
                requiredXp = 1000,
                criteriaDescription = "Defeat Boss Trial or reach 1000 XP",
                isUnlocked = currentXp >= 1000,
                unlockedDate = if (currentXp >= 1000) "Trial Conquered" else null,
                progressPercent = (currentXp * 100 / 1000).coerceIn(0, 100)
            ),
            BadgeItem(
                id = "scholar_philosopher",
                title = "Scholar Philosopher",
                description = "Reflect deeply on your study habits and opinions in the Journal.",
                emoji = "📖",
                category = BadgeCategory.SCHOLAR,
                requiredXp = 600,
                criteriaDescription = "Write philosophical study reflections",
                isUnlocked = currentXp >= 600,
                unlockedDate = if (currentXp >= 600) "Reflections Logged" else null,
                progressPercent = (currentXp * 100 / 600).coerceIn(0, 100)
            ),
            BadgeItem(
                id = "grandmaster_legend",
                title = "Grandmaster Legend",
                description = "Reach elite student status by accumulating over 2,000 XP across all disciplines.",
                emoji = "👑",
                category = BadgeCategory.CHAMPION,
                requiredXp = 2000,
                criteriaDescription = "Reach 2,000 Total XP",
                isUnlocked = currentXp >= 2000,
                unlockedDate = if (currentXp >= 2000) "Grandmaster Rank" else null,
                progressPercent = (currentXp * 100 / 2000).coerceIn(0, 100)
            )
        )
    }
}
