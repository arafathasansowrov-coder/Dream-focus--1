package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BadgeItem
import com.example.data.model.LeaderboardEntry
import com.example.data.model.StudentProfile
import com.example.data.model.StudentTaskPoints
import com.example.ui.theme.*

enum class GamificationModalTab(val title: String, val icon: String) {
    LEADERBOARD("Leaderboard", "🏆"),
    BADGES("Badges", "🌟"),
    TASK_POINTS("Task Points", "⭐")
}

/**
 * Complete Gamification, Badges & Leaderboard modal:
 * - Points system with defined XP reward table for common student tasks
 * - Unlockable milestone badges with progress tracking and unlock states
 * - Optional Leaderboard with top 3 podium, live peer rankings, and privacy toggle
 */
@Composable
fun GamificationLeaderboardModal(
    totalXp: Int,
    profile: StudentProfile,
    badges: List<BadgeItem>,
    leaderboardEntries: List<LeaderboardEntry>,
    isLeaderboardEnabled: Boolean,
    onToggleLeaderboard: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(GamificationModalTab.LEADERBOARD) }
    var selectedBadgeDetail by remember { mutableStateOf<BadgeItem?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .testTag("gamification_leaderboard_modal"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Top Title & Dismiss Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = AmberGold.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "👑", fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Gamification & Ranks",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Total Score: $totalXp XP • ${profile.currentStreak}-Day Streak",
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_gamification_modal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Switcher Row
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    GamificationModalTab.entries.forEachIndexed { index, tab ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = GamificationModalTab.entries.size),
                            onClick = { selectedTab = tab },
                            selected = selectedTab == tab,
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = ElectricIndigo.copy(alpha = 0.2f),
                                activeContentColor = ElectricIndigo,
                                inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(
                                text = "${tab.icon} ${tab.title}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Content
                when (selectedTab) {
                    GamificationModalTab.LEADERBOARD -> {
                        LeaderboardTabContent(
                            totalXp = totalXp,
                            entries = leaderboardEntries,
                            isEnabled = isLeaderboardEnabled,
                            onToggleEnabled = onToggleLeaderboard
                        )
                    }
                    GamificationModalTab.BADGES -> {
                        BadgesTabContent(
                            badges = badges,
                            onSelectBadge = { selectedBadgeDetail = it }
                        )
                    }
                    GamificationModalTab.TASK_POINTS -> {
                        TaskPointsTabContent()
                    }
                }
            }
        }
    }

    // Badge Lore Detail Dialog
    selectedBadgeDetail?.let { badge ->
        AlertDialog(
            onDismissRequest = { selectedBadgeDetail = null },
            icon = { Text(text = badge.emoji, fontSize = 42.sp) },
            title = {
                Text(
                    text = badge.title,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = badge.description,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (badge.isUnlocked) EmeraldGreen.copy(alpha = 0.15f) else ElectricIndigo.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Criteria: ${badge.criteriaDescription}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (badge.isUnlocked) EmeraldGreen else ElectricIndigo
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (badge.isUnlocked) "✅ Status: Unlocked (${badge.unlockedDate ?: "Achieved"})" else "🔒 Progress: ${badge.progressPercent}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedBadgeDetail = null }) {
                    Text("Got It")
                }
            }
        )
    }
}

@Composable
private fun LeaderboardTabContent(
    totalXp: Int,
    entries: List<LeaderboardEntry>,
    isEnabled: Boolean,
    onToggleEnabled: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Toggle Competitive / Solo Mode Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Leaderboard Visibility",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isEnabled) "Active in Global Peer Rankings" else "Solo Focus Mode (Rank hidden)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isEnabled) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggleEnabled,
                    modifier = Modifier.testTag("leaderboard_toggle_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (!isEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🛡️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Solo Focus Mode Active",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Leaderboard rankings are hidden for distraction-free studying. Toggle the switch above anytime to view peer rankings.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Top 3 Podium
            val top3 = entries.take(3)
            if (top3.size >= 3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // 2nd Place
                    PodiumPillar(entry = top3[1], medal = "🥈", heightDp = 100, color = Color(0xFF94A3B8))
                    // 1st Place
                    PodiumPillar(entry = top3[0], medal = "🥇", heightDp = 130, color = AmberGold)
                    // 3rd Place
                    PodiumPillar(entry = top3[2], medal = "🥉", heightDp = 85, color = Color(0xFFD97706))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Full Ranks List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries) { entry ->
                    val isUser = entry.isCurrentUser
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("leaderboard_item_${entry.rank}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) ElectricIndigo.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = if (isUser) androidx.compose.foundation.BorderStroke(1.5.dp, ElectricIndigo) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${entry.rank}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isUser) ElectricIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(32.dp)
                            )
                            Text(text = entry.avatarEmoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = entry.studentName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isUser) FontWeight.ExtraBold else FontWeight.Bold,
                                        color = if (isUser) ElectricIndigo else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isUser) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = ElectricIndigo
                                        ) {
                                            Text(
                                                text = "YOU",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = entry.dreamGoal,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${entry.totalPoints} XP",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AmberGold
                                )
                                Text(
                                    text = "🔥 ${entry.streakDays}d • 🏅 ${entry.badgesCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PodiumPillar(
    entry: LeaderboardEntry,
    medal: String,
    heightDp: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(96.dp)
    ) {
        Text(text = entry.avatarEmoji, fontSize = 24.sp)
        Text(
            text = entry.studentName.substringBefore(" "),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${entry.totalPoints} XP",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = AmberGold,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            color = color.copy(alpha = 0.25f),
            border = androidx.compose.foundation.BorderStroke(1.dp, color),
            modifier = Modifier
                .width(80.dp)
                .height(heightDp.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = medal, fontSize = 28.sp)
            }
        }
    }
}

@Composable
private fun BadgesTabContent(
    badges: List<BadgeItem>,
    onSelectBadge: (BadgeItem) -> Unit
) {
    val unlockedCount = badges.count { it.isUnlocked }

    Column(modifier = Modifier.fillMaxSize()) {
        // Summary Header
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = ElectricIndigo.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Milestone Badges",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ElectricIndigo
                    )
                    Text(
                        text = "Unlocked $unlockedCount of ${badges.size} achievement trophies",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LinearProgressIndicator(
                    progress = { unlockedCount.toFloat() / badges.size.coerceAtLeast(1) },
                    modifier = Modifier
                        .width(90.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = ElectricIndigo
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(badges) { badge ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectBadge(badge) }
                        .testTag("badge_item_${badge.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (badge.isUnlocked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    ),
                    border = if (badge.isUnlocked) androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.6f)) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (badge.isUnlocked) AmberGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (badge.isUnlocked) badge.emoji else "🔒",
                                    fontSize = 24.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = badge.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (badge.isUnlocked) EmeraldGreen.copy(alpha = 0.15f) else ElectricIndigo.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (badge.isUnlocked) "UNLOCKED" else "${badge.requiredXp} XP",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (badge.isUnlocked) EmeraldGreen else ElectricIndigo,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = badge.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!badge.isUnlocked) {
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { badge.progressPercent / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = ElectricIndigo
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskPointsTabContent() {
    val pointRules = listOf(
        TaskPointRow("Pomodoro Focus Sprint (25m)", StudentTaskPoints.FOCUS_SPRINT_25_MIN, "⏱️", "Deep focus sprint without distraction interruptions"),
        TaskPointRow("Deep Work Sprint (50m)", StudentTaskPoints.FOCUS_SPRINT_50_MIN, "⚡", "Extended high-intensity cognitive sprint"),
        TaskPointRow("Daily Routine Habit Done", StudentTaskPoints.DAILY_ROUTINE_DONE, "✅", "Completing scheduled routine or study habit on time"),
        TaskPointRow("All Daily Habits Completed", StudentTaskPoints.ALL_ROUTINES_COMPLETED, "🔥", "100% routine completion for the entire day"),
        TaskPointRow("Goal Milestone Conquered", StudentTaskPoints.GOAL_MILESTONE_DONE, "🏆", "Completing major milestone sub-step towards career goal"),
        TaskPointRow("RPG Quest Level-Up", StudentTaskPoints.GOAL_QUEST_LEVEL_UP, "🎮", "Advancing rank stage in AI Gamified Goal Completer"),
        TaskPointRow("Boss Trial Victory", StudentTaskPoints.BOSS_TRIAL_COMPLETED, "⚔️", "Defeating AI Boss Challenge in Gamified Quest"),
        TaskPointRow("Journal Reflection Written", StudentTaskPoints.JOURNAL_OPINION_LOGGED, "✍️", "Writing personal philosophy, opinions, or study review"),
        TaskPointRow("Distraction Shield Defense", StudentTaskPoints.DISTRACTION_SHIELD_DEFENSE, "🛡️", "Refusing to open blocked blacklisted app during study"),
        TaskPointRow("3-Day Study Streak Bonus", StudentTaskPoints.STREAK_BONUS_3_DAY, "🌟", "Maintaining consecutive 3-day study habits"),
        TaskPointRow("7-Day Study Streak Bonus", StudentTaskPoints.STREAK_BONUS_7_DAY, "👑", "Maintaining unbreakable 7-day study consistency")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CyberCyan.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Defined Student Point System",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )
                Text(
                    text = "Points translate directly to player ranks, badge unlocks, and peer leaderboard standings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(pointRules) { rule ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = rule.emoji, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = rule.taskName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = rule.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AmberGold.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "+${rule.points} XP",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = AmberGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class TaskPointRow(
    val taskName: String,
    val points: Int,
    val emoji: String,
    val description: String
)
