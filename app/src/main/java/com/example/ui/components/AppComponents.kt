package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun DreamHeroBanner(
    profile: StudentProfile,
    onEditProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dream_hero_banner"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF312E81),
                            Color(0xFF1E1B4B),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = CyberCyan.copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.RocketLaunch,
                                    contentDescription = "Dream",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "STUDENT DREAM MISSION",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = profile.dreamTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = onEditProfileClick,
                        modifier = Modifier.testTag("edit_dream_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit Profile",
                            tint = TextSecondaryDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = profile.dreamDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCBD5E1),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatPill(
                        icon = Icons.Default.LocalFireDepartment,
                        label = "Streak",
                        value = "${profile.currentStreak} Days",
                        tint = AmberGold
                    )
                    StatPill(
                        icon = Icons.Default.HourglassTop,
                        label = "Focus Time",
                        value = "${profile.totalFocusMinutes / 60}h ${profile.totalFocusMinutes % 60}m",
                        tint = CyberCyan
                    )
                    StatPill(
                        icon = Icons.Default.Flag,
                        label = "Target",
                        value = "${profile.targetYear}",
                        tint = ElectricIndigo
                    )
                }
            }
        }
    }
}

@Composable
fun StatPill(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Black.copy(alpha = 0.35f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GoalCard(
    goal: GoalItem,
    onProgressChange: (Int) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("goal_item_${goal.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (goal.isCompleted) EmeraldGreen.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryBadge(category = goal.category)
                    Spacer(modifier = Modifier.width(8.dp))
                    PriorityBadge(priority = goal.priority)
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp).testTag("delete_goal_${goal.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete Goal",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = goal.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-milestones checklist preview
            if (goal.milestonesJson.isNotEmpty()) {
                val milestones = goal.milestonesJson.split("|").filter { it.isNotBlank() }
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    milestones.take(3).forEach { milestone ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = if (goal.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (goal.isCompleted) EmeraldGreen else CyberCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = milestone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar and Quick Increments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Deadline: ${goal.deadlineDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${goal.progressPercentage}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (goal.isCompleted) EmeraldGreen else ElectricIndigo
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { goal.progressPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (goal.isCompleted) EmeraldGreen else ElectricIndigo,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Progress Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FilledTonalButton(
                    onClick = { onProgressChange((goal.progressPercentage - 15).coerceAtLeast(0)) },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Text("-15%", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = { onProgressChange((goal.progressPercentage + 15).coerceAtMost(100)) },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Text("+15%", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onProgressChange(if (goal.isCompleted) 0 else 100) },
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (goal.isCompleted) MaterialTheme.colorScheme.surfaceVariant else EmeraldGreen
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(
                        text = if (goal.isCompleted) "Reopen" else "Complete",
                        fontSize = 11.sp,
                        color = if (goal.isCompleted) MaterialTheme.colorScheme.onSurface else Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun RoutineCard(
    routine: RoutineItem,
    onToggleDone: () -> Unit,
    onToggleAlarm: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("routine_card_${routine.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (routine.isCompletedToday)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (routine.isCompletedToday) EmeraldGreen.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleDone,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("toggle_routine_${routine.id}")
            ) {
                Icon(
                    imageVector = if (routine.isCompletedToday) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = "Toggle Complete",
                    tint = if (routine.isCompletedToday) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routine.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (routine.isCompletedToday)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = routine.scheduledTime,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${routine.durationMinutes} mins",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    CategoryBadge(category = routine.category)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val reminderBadgeText = when (routine.reminderType) {
                        "PSYCHOLOGICAL_MESSAGE" -> "🧠 Nudge"
                        "BOTH" -> "⚡ Dual"
                        "NONE" -> "🔇 Off"
                        else -> "⏰ Alarm"
                    }
                    val reminderBadgeColor = when (routine.reminderType) {
                        "PSYCHOLOGICAL_MESSAGE" -> CyberCyan
                        "BOTH" -> AmberGold
                        "NONE" -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> ElectricIndigo
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = reminderBadgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = reminderBadgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = reminderBadgeColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    if (routine.reminderType == "PSYCHOLOGICAL_MESSAGE" || routine.reminderType == "BOTH") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = routine.psychologicalTone,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            IconButton(
                onClick = onToggleAlarm,
                modifier = Modifier.size(32.dp).testTag("alarm_routine_${routine.id}")
            ) {
                Icon(
                    imageVector = if (routine.alarmEnabled) Icons.Default.NotificationsActive else Icons.Outlined.NotificationsOff,
                    contentDescription = "Alarm Toggle",
                    tint = if (routine.alarmEnabled) AmberGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp).testTag("delete_routine_${routine.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun BlockedAppItemCard(
    app: BlockedAppRule,
    onToggleBlock: (Boolean) -> Unit,
    onSimulateBlock: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("blocked_app_${app.packageName}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (app.isBlockActiveDuringFocus) CoralRed.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (app.isBlockActiveDuringFocus) CoralRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (app.isBlockActiveDuringFocus) Icons.Default.Shield else Icons.Outlined.Shield,
                                contentDescription = "Shield",
                                tint = if (app.isBlockActiveDuringFocus) CoralRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = app.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = app.isBlockActiveDuringFocus,
                        onCheckedChange = onToggleBlock,
                        modifier = Modifier.testTag("switch_block_${app.appName}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Limit: ${app.dailyLimitMinutes}m | Blocked ${app.timesBlockedToday}x",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row {
                    OutlinedButton(
                        onClick = onSimulateBlock,
                        modifier = Modifier.height(30.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Test Intercept", fontSize = 11.sp, color = CyberCyan)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete Rule",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DistractionBlockedDialog(
    appName: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(2.dp, CoralRed),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("distraction_intercept_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = CoralRed.copy(alpha = 0.2f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ShieldMoon,
                            contentDescription = "Shield Locked",
                            tint = CoralRed,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "DISTRACTION INTERCEPTED",
                    style = MaterialTheme.typography.titleSmall,
                    color = CoralRed,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$appName is currently blocked by your Focus Shield.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Every minute spent scrolling is a minute delayed from achieving your dream career. Protect your focus!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondaryDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("return_to_focus_button")
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Return to Deep Focus", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CategoryBadge(category: String) {
    val bg = when (category) {
        "AI & Tech", "Coding" -> CyberCyan.copy(alpha = 0.15f)
        "Academics", "Deep Work" -> ElectricIndigo.copy(alpha = 0.15f)
        "Content Creation" -> NeonPurple.copy(alpha = 0.15f)
        else -> AmberGold.copy(alpha = 0.15f)
    }
    val text = when (category) {
        "AI & Tech", "Coding" -> CyberCyan
        "Academics", "Deep Work" -> ElectricIndigo
        "Content Creation" -> NeonPurple
        else -> AmberGold
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = text,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun PriorityBadge(priority: String) {
    val (bg, text) = when (priority) {
        "High" -> PriorityHighBg to PriorityHighText
        "Medium" -> PriorityMediumBg to PriorityMediumText
        else -> PriorityLowBg to PriorityLowText
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg
    ) {
        Text(
            text = priority,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = text,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// Dialog for Adding a new Student Goal
@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, priority: String, deadline: String, milestones: List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("AI & Tech") }
    var priority by remember { mutableStateOf("High") }
    var deadline by remember { mutableStateOf("2026-12-31") }
    var milestoneText by remember { mutableStateOf("Master Foundations\nBuild Portfolio\nDeploy Application") }

    val categories = listOf("AI & Tech", "Academics", "Content Creation", "Skills", "Health")
    val priorities = listOf("High", "Medium", "Low")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Create Master Goal",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title (e.g. Master PyTorch & LLMs)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Category", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = deadline,
                        onValueChange = { deadline = it },
                        label = { Text("Deadline (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = milestoneText,
                    onValueChange = { milestoneText = it },
                    label = { Text("Milestones (one per line)") },
                    minLines = 3,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val milestones = milestoneText.lines().map { it.trim() }.filter { it.isNotEmpty() }
                        onConfirm(title, category, priority, deadline, milestones)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_goal_button")
            ) {
                Text("Create Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Dialog for Adding a new Daily Routine with dual reminder customization
@Composable
fun AddRoutineDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        time: String,
        category: String,
        duration: Int,
        alarm: Boolean,
        reminderType: String,
        psychologicalTone: String,
        alarmSound: String,
        snoozeMinutes: Int,
        customMessage: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("07:00 AM") }
    var category by remember { mutableStateOf("Deep Work") }
    var durationText by remember { mutableStateOf("45") }
    var alarmEnabled by remember { mutableStateOf(true) }

    // Reminder type and customization states
    var reminderType by remember { mutableStateOf("DIRECT_ALARM") } // "PSYCHOLOGICAL_MESSAGE", "DIRECT_ALARM", "BOTH", "NONE"
    var psychologicalTone by remember { mutableStateOf("Growth Mindset & Discipline") }
    var alarmSound by remember { mutableStateOf("Chime") }
    var snoozeMinutes by remember { mutableIntStateOf(5) }
    var customPsychologicalMessage by remember { mutableStateOf("") }

    val categories = listOf("Deep Work", "Coding", "Study Block", "Revision", "Morning Ritual")
    val psychologicalTones = listOf(
        "Growth Mindset & Discipline",
        "Gentle Encouragement",
        "High-Energy Hype",
        "Stoic Resilience",
        "Focus & Calm"
    )
    val alarmSounds = listOf("Chime", "Urgent Siren", "Steady Pulse")
    val snoozeOptions = listOf(5, 10, 15)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Daily Routine & Reminders",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Routine Name (e.g. Deep Coding Sprint)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("routine_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time (e.g. 07:00 AM)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it },
                        label = { Text("Mins") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.width(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Category", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                // Dual Reminder Mode Selector
                Text(
                    text = "Select Reminder Type:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = reminderType == "PSYCHOLOGICAL_MESSAGE",
                        onClick = {
                            reminderType = "PSYCHOLOGICAL_MESSAGE"
                            alarmEnabled = true
                        },
                        label = { Text("🧠 Mindset Nudge", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                            selectedLabelColor = CyberCyan
                        )
                    )
                    FilterChip(
                        selected = reminderType == "DIRECT_ALARM",
                        onClick = {
                            reminderType = "DIRECT_ALARM"
                            alarmEnabled = true
                        },
                        label = { Text("⏰ Direct Alarm", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricIndigo.copy(alpha = 0.2f),
                            selectedLabelColor = ElectricIndigo
                        )
                    )
                    FilterChip(
                        selected = reminderType == "BOTH",
                        onClick = {
                            reminderType = "BOTH"
                            alarmEnabled = true
                        },
                        label = { Text("⚡ Both", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberGold.copy(alpha = 0.2f),
                            selectedLabelColor = AmberGold
                        )
                    )
                }

                // Customization fields depending on selected reminder type
                if (reminderType == "PSYCHOLOGICAL_MESSAGE" || reminderType == "BOTH") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Psychological Coaching Tone:",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        psychologicalTones.take(2).forEach { tone ->
                            FilterChip(
                                selected = psychologicalTone == tone,
                                onClick = { psychologicalTone = tone },
                                label = { Text(tone, fontSize = 10.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = customPsychologicalMessage,
                        onValueChange = { customPsychologicalMessage = it },
                        label = { Text("Custom Motivation Prompt (Optional)", fontSize = 11.sp) },
                        placeholder = { Text("e.g. Remember why you started this journey...", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (reminderType == "DIRECT_ALARM" || reminderType == "BOTH") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Alarm Sound & Snooze:",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricIndigo
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        alarmSounds.forEach { sound ->
                            FilterChip(
                                selected = alarmSound == sound,
                                onClick = { alarmSound = sound },
                                label = { Text(sound, fontSize = 10.sp) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Snooze: ", style = MaterialTheme.typography.labelSmall)
                        snoozeOptions.forEach { mins ->
                            FilterChip(
                                selected = snoozeMinutes == mins,
                                onClick = { snoozeMinutes = mins },
                                label = { Text("${mins}m", fontSize = 10.sp) },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val duration = durationText.toIntOrNull() ?: 45
                        onConfirm(
                            title,
                            time,
                            category,
                            duration,
                            alarmEnabled,
                            reminderType,
                            psychologicalTone,
                            alarmSound,
                            snoozeMinutes,
                            customPsychologicalMessage
                        )
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_routine_button")
            ) {
                Text("Add Routine")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Backwards-compatible overload for 5-argument callers
@Composable
fun AddRoutineDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, time: String, category: String, duration: Int, alarm: Boolean) -> Unit
) {
    AddRoutineDialog(
        onDismiss = onDismiss,
        onConfirm = { title, time, category, duration, alarm, _, _, _, _, _ ->
            onConfirm(title, time, category, duration, alarm)
        }
    )
}

// Dialog for editing student dream profile
@Composable
fun EditProfileDialog(
    profile: StudentProfile,
    onDismiss: () -> Unit,
    onConfirm: (name: String, dream: String, desc: String, targetYear: Int, hours: Float) -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var dream by remember { mutableStateOf(profile.dreamTitle) }
    var desc by remember { mutableStateOf(profile.dreamDescription) }
    var targetYearText by remember { mutableStateOf(profile.targetYear.toString()) }
    var targetHoursText by remember { mutableStateOf(profile.dailyTargetHours.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Configure Student Dream",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Name / Alias") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dream,
                    onValueChange = { dream = it },
                    label = { Text("Dream Title (e.g. Lead AI Engineer, Top Tech Creator)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("profile_dream_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Dream Purpose / Manifesto") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = targetYearText,
                        onValueChange = { targetYearText = it },
                        label = { Text("Target Year") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetHoursText,
                        onValueChange = { targetHoursText = it },
                        label = { Text("Daily Hours") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val year = targetYearText.toIntOrNull() ?: 2028
                    val hours = targetHoursText.toFloatOrNull() ?: 4.0f
                    onConfirm(name, dream, desc, year, hours)
                },
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Text("Save Dream")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Dialog for adding a blocked app rule
@Composable
fun AddBlockedAppDialog(
    onDismiss: () -> Unit,
    onConfirm: (packageName: String, appName: String, category: String, limit: Int, isStrict: Boolean) -> Unit
) {
    var appName by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Social Media") }
    var limitText by remember { mutableStateOf("20") }
    var isStrict by remember { mutableStateOf(false) }

    val popularDistractions = listOf(
        Triple("YouTube", "com.google.android.youtube", "Video Streaming"),
        Triple("Instagram", "com.instagram.android", "Social Media"),
        Triple("TikTok", "com.zhiliaoapp.musically", "Short Video"),
        Triple("Reddit", "com.reddit.frontpage", "Community Feeds"),
        Triple("Netflix", "com.netflix.mediaclient", "Entertainment")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add App to Distraction Shield",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Quick Select Distraction:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    popularDistractions.take(3).forEach { (name, pkg, cat) ->
                        FilterChip(
                            selected = appName == name,
                            onClick = {
                                appName = name
                                packageName = pkg
                                category = cat
                            },
                            label = { Text(name, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    label = { Text("App Name (e.g. YouTube)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_app_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("Package Name (e.g. com.google.android.youtube)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("Daily Limit (Minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Strict Lock (No bypass)", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isStrict, onCheckedChange = { isStrict = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (appName.isNotBlank() && packageName.isNotBlank()) {
                        val limit = limitText.toIntOrNull() ?: 20
                        onConfirm(packageName, appName, category, limit, isStrict)
                    }
                },
                enabled = appName.isNotBlank() && packageName.isNotBlank(),
                modifier = Modifier.testTag("save_blocked_app_button")
            ) {
                Text("Add Shield Rule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Card for Approved Designated Study Tool in Distraction Shield
@Composable
fun DesignatedStudyAppCard(
    app: DesignatedStudyApp,
    onToggle: (Boolean) -> Unit,
    onLaunch: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("study_app_card_${app.packageName}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (app.isEnabled) EmeraldGreen.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = if (app.isEnabled) EmeraldGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (app.isEnabled) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = if (app.isEnabled) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${app.category} • ${app.packageName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = app.isEnabled,
                        onCheckedChange = onToggle,
                        modifier = Modifier.testTag("switch_study_app_${app.packageName}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (app.isEnabled) EmeraldGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (app.isEnabled) "✓ Approved Study App" else "Paused from Whitelist",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (app.isEnabled) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onLaunch,
                        modifier = Modifier.height(32.dp).testTag("open_study_app_${app.packageName}"),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open", fontSize = 11.sp, color = CyberCyan)
                    }

                    if (!app.isSystemPreinstalled) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp).testTag("delete_study_app_${app.packageName}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Dialog for Adding a new Designated Study App
@Composable
fun AddStudyAppDialog(
    onDismiss: () -> Unit,
    onConfirm: (packageName: String, appName: String, category: String, actionUri: String) -> Unit
) {
    var appName by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Study Tool") }
    var actionUri by remember { mutableStateOf("") }

    val presetStudyApps = listOf(
        Triple("Google Docs", "com.google.android.apps.docs", "Notes & Docs"),
        Triple("Google Classroom", "com.google.android.apps.classroom", "Study Tool"),
        Triple("AnkiDroid Flashcards", "com.ichi2.anki", "Flashcards"),
        Triple("Notion", "com.notion.id", "Notes & Docs"),
        Triple("GitHub", "com.github.android", "Coding & Tech"),
        Triple("Adobe Acrobat PDF", "com.adobe.reader", "Research"),
        Triple("Scientific Calculator", "com.google.android.calculator", "Math & STEM"),
        Triple("Khan Academy", "org.khanacademy.android", "Learning Platform")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Designated Study Tool",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Pick a popular study tool or enter a custom package name:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    presetStudyApps.chunked(2).forEach { rowPresets ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowPresets.forEach { (name, pkg, cat) ->
                                FilterChip(
                                    selected = packageName == pkg,
                                    onClick = {
                                        appName = name
                                        packageName = pkg
                                        category = cat
                                    },
                                    label = { Text(name, fontSize = 11.sp, maxLines = 1) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    label = { Text("App Name (e.g. Notion, Anki)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_study_app_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("Package Name (e.g. com.notion.id)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_study_app_pkg_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. Coding, Math, Reading)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (appName.isNotBlank() && packageName.isNotBlank()) {
                        onConfirm(packageName, appName, category, actionUri)
                    }
                },
                enabled = appName.isNotBlank() && packageName.isNotBlank(),
                modifier = Modifier.testTag("save_study_app_button")
            ) {
                Text("Approve Study App")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Modal Alert Dialog when student navigates away from designated study apps
@Composable
fun DistractionAlertModal(
    event: DistractionAlertEvent,
    onDismiss: () -> Unit,
    onAllowAsStudyApp: (packageName: String, appName: String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("distraction_alert_modal"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, CoralRed)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = CoralRed.copy(alpha = 0.15f),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert",
                            tint = CoralRed,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "DISTRACTION ALERT!",
                    style = MaterialTheme.typography.titleMedium,
                    color = CoralRed,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Left Designated Study Zone",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = CoralRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Detected App: ${event.appName}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Package: ${event.packageName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = event.reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = CoralRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Research shows it takes 23 minutes to refocus after a distraction. Return to deep work now to protect your daily study streak!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("modal_return_to_study_btn")
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Return to Study Mode", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { onAllowAsStudyApp(event.packageName, event.appName) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("modal_allow_app_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Whitelist ${event.appName} as Study App", fontSize = 12.sp)
                }
            }
        }
    }
}
