package com.example.data.model

/**
 * Stages in the Pomodoro technique.
 */
enum class PomodoroStage(
    val title: String,
    val shortLabel: String,
    val defaultMinutes: Int,
    val emoji: String,
    val xpReward: Int,
    val description: String
) {
    WORK(
        title = "Focus Sprint",
        shortLabel = "Focus",
        defaultMinutes = 25,
        emoji = "🎯",
        xpReward = StudentTaskPoints.FOCUS_SPRINT_25_MIN,
        description = "Deep cognitive immersion without interruptions"
    ),
    DEEP_WORK(
        title = "Deep Work Marathon",
        shortLabel = "Deep 50m",
        defaultMinutes = 50,
        emoji = "⚡",
        xpReward = StudentTaskPoints.FOCUS_SPRINT_50_MIN,
        description = "High-intensity problem solving & complex mastery"
    ),
    SHORT_BREAK(
        title = "Short Recharge",
        shortLabel = "Short Break",
        defaultMinutes = 5,
        emoji = "☕",
        xpReward = 10,
        description = "Step away, stretch, hydrate, and rest your eyes"
    ),
    LONG_BREAK(
        title = "Long Recovery",
        shortLabel = "Long Break",
        defaultMinutes = 15,
        emoji = "🌿",
        xpReward = 20,
        description = "Full cognitive reset after completing 4 cycles"
    )
}

/**
 * Ambient focus sound vibes for study immersion.
 */
enum class AmbientSoundVibe(
    val title: String,
    val emoji: String,
    val description: String
) {
    SILENT("Silent Focus", "🔕", "Pure quiet concentration"),
    RAIN("Tokyo Rain", "🌧️", "Gentle continuous soothing rain"),
    WHITE_NOISE("Binaural Waves", "🎧", "40Hz Gamma wave cognitive tuning"),
    COFFEE_SHOP("Library Ambience", "📚", "Subtle campus study hall murmurs"),
    FOREST("Cyber Zen", "🌌", "Ambient synthesizer focus resonance")
}

/**
 * Comprehensive Pomodoro session state.
 */
data class PomodoroSessionState(
    val currentStage: PomodoroStage = PomodoroStage.WORK,
    val currentCycle: Int = 1,
    val totalCyclesInRound: Int = 4,
    val completedSessionsCount: Int = 6,
    val isShieldAutoLockEnabled: Boolean = true,
    val ambientVibe: AmbientSoundVibe = AmbientSoundVibe.SILENT,
    val totalFocusMinutesToday: Int = 125
)
