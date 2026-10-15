package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AmbientSoundVibe
import com.example.data.model.GoalItem
import com.example.data.model.PomodoroStage
import com.example.ui.theme.*

/**
 * State-of-the-Art Pomodoro-Style Focus Timer UI:
 * Deeply integrates with the Distraction Shield logic, automated foreground monitoring,
 * multi-cycle stage tracking, ambient focus vibes, and linked dream goals.
 */
@Composable
fun PomodoroFocusTimerCard(
    isTimerRunning: Boolean,
    timeRemainingSeconds: Int,
    totalSessionSeconds: Int,
    currentStage: PomodoroStage,
    currentCycle: Int,
    completedPomodorosCount: Int,
    selectedGoal: GoalItem?,
    goals: List<GoalItem>,
    isShieldServiceRunning: Boolean,
    isShieldAutoLockEnabled: Boolean,
    activeDistractionBlocksCount: Int,
    hasUsageStatsPermission: Boolean,
    ambientVibe: AmbientSoundVibe,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onSelectStage: (PomodoroStage) -> Unit,
    onSkipStage: () -> Unit,
    onAdjustMinutes: (Int) -> Unit,
    onToggleShieldAutoLock: (Boolean) -> Unit,
    onToggleShieldService: () -> Unit,
    onRequestUsageStats: () -> Unit,
    onSelectGoal: (GoalItem?) -> Unit,
    onSelectAmbientVibe: (AmbientSoundVibe) -> Unit,
    onTestShieldTrigger: (appName: String, packageName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showGoalPicker by remember { mutableStateOf(false) }
    var showAmbientVibePicker by remember { mutableStateOf(false) }

    val progress = if (totalSessionSeconds > 0) {
        (totalSessionSeconds - timeRemainingSeconds).toFloat() / totalSessionSeconds
    } else 0f

    val minutes = timeRemainingSeconds / 60
    val seconds = timeRemainingSeconds % 60

    // Pulsing animation for active ticking ring
    val infiniteTransition = rememberInfiniteTransition(label = "pomodoro_pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    val isBreakStage = currentStage == PomodoroStage.SHORT_BREAK || currentStage == PomodoroStage.LONG_BREAK
    val primaryColor = when {
        isBreakStage -> CyberCyan
        isTimerRunning -> EmeraldGreen
        else -> ElectricIndigo
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pomodoro_focus_timer_card"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isTimerRunning) primaryColor.copy(alpha = pulseGlow) else primaryColor.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            primaryColor.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Stage Selector Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PomodoroStage.entries.forEach { stage ->
                    val isSelected = currentStage == stage
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(11.dp))
                            .background(
                                if (isSelected) {
                                    if (stage == PomodoroStage.SHORT_BREAK || stage == PomodoroStage.LONG_BREAK)
                                        CyberCyan.copy(alpha = 0.25f)
                                    else
                                        ElectricIndigo.copy(alpha = 0.25f)
                                } else Color.Transparent
                            )
                            .clickable { onSelectStage(stage) }
                            .padding(vertical = 7.dp)
                            .testTag("pomodoro_stage_${stage.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = stage.emoji, fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = stage.shortLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) {
                                    if (stage == PomodoroStage.SHORT_BREAK || stage == PomodoroStage.LONG_BREAK) CyberCyan else ElectricIndigo
                                } else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cycle & Round Indicator (e.g. 🍅 🍅 ⚪ ⚪ Cycle 2/4)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = primaryColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "ROUND $currentCycle/4",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Cycle Dots
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (i in 1..4) {
                            val isCompleted = i < currentCycle
                            val isCurrent = i == currentCycle
                            Surface(
                                shape = CircleShape,
                                color = when {
                                    isCompleted -> EmeraldGreen
                                    isCurrent -> primaryColor
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                },
                                modifier = Modifier.size(if (isCurrent) 10.dp else 8.dp)
                            ) {}
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AmberGold.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "🏆 $completedPomodorosCount Completed",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AmberGold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Holographic Circular Countdown Timer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(220.dp)
                    .testTag("pomodoro_circular_canvas")
            ) {
                val trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Outer glow shadow
                    if (isTimerRunning) {
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.12f * pulseGlow),
                            radius = size.width / 2f
                        )
                    }

                    // Background circular track
                    drawCircle(
                        color = trackColor,
                        style = Stroke(width = 14.dp.toPx())
                    )

                    // Active progress arc
                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = (1f - progress) * 360f,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = primaryColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = currentStage.title.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 9.sp,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "%02d:%02d".format(minutes, seconds),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 44.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Text(
                        text = if (isTimerRunning) "● RUNNING" else "PAUSED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isTimerRunning) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "+${currentStage.xpReward} XP upon completion",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = AmberGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Time Adjusters (-5m / +5m)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onAdjustMinutes(-5) },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("-5m", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    modifier = Modifier
                        .clickable { showAmbientVibePicker = !showAmbientVibePicker }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = ambientVibe.emoji, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = ambientVibe.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }

                OutlinedButton(
                    onClick = { onAdjustMinutes(5) },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("+5m", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Ambient Sound Selector Dropdown Bar
            AnimatedVisibility(visible = showAmbientVibePicker) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "🎧 Ambient Study Atmosphere",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(AmbientSoundVibe.entries) { vibe ->
                                val isVibeSelected = ambientVibe == vibe
                                FilterChip(
                                    selected = isVibeSelected,
                                    onClick = {
                                        onSelectAmbientVibe(vibe)
                                        showAmbientVibePicker = false
                                    },
                                    label = { Text("${vibe.emoji} ${vibe.title}", fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Linked Goal Tag & Selector
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showGoalPicker = !showGoalPicker }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = ElectricIndigo,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Linked Goal Objective",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = selectedGoal?.title ?: "General Deep Work Sprint",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Icon(
                        imageVector = if (showGoalPicker) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Linked Goal Picker Dropdown
            AnimatedVisibility(visible = showGoalPicker) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedGoal == null) ElectricIndigo.copy(alpha = 0.2f) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectGoal(null)
                                    showGoalPicker = false
                                }
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "🎯 General Deep Work (No specific goal)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        goals.forEach { goal ->
                            val isChosen = selectedGoal?.id == goal.id
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isChosen) ElectricIndigo.copy(alpha = 0.2f) else Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectGoal(goal)
                                        showGoalPicker = false
                                    }
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = goal.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${goal.progressPercentage}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // DISTRACTION SHIELD INTEGRATION BANNER
            // ==========================================
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isShieldServiceRunning) EmeraldGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isShieldServiceRunning) EmeraldGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shield_integration_banner")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = CircleShape,
                                color = if (isShieldServiceRunning) EmeraldGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isShieldServiceRunning) Icons.Default.Shield else Icons.Outlined.Shield,
                                        contentDescription = null,
                                        tint = if (isShieldServiceRunning) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isShieldServiceRunning) "SHIELD ACTIVATED" else "DISTRACTION SHIELD",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isShieldServiceRunning) EmeraldGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isShieldServiceRunning) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = EmeraldGreen
                                        ) {
                                            Text(
                                                text = "GUARDING",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 8.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = if (isShieldServiceRunning)
                                        "Guarding deep work ($activeDistractionBlocksCount distractions deflected)"
                                    else
                                        "Auto-locks when you press Start Sprint",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Auto-lock toggle switch
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Auto-Lock",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = isShieldAutoLockEnabled,
                                onCheckedChange = onToggleShieldAutoLock,
                                modifier = Modifier.testTag("shield_auto_lock_switch")
                            )
                        }
                    }

                    // Shield quick actions (Test Shield & Permission)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!hasUsageStatsPermission) {
                            TextButton(
                                onClick = onRequestUsageStats,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(12.dp), tint = AmberGold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Grant Usage Permission", fontSize = 10.sp, color = AmberGold, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(
                                text = "✅ Background monitoring authorized",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = EmeraldGreen
                            )
                        }

                        // Test Distraction Trigger
                        OutlinedButton(
                            onClick = { onTestShieldTrigger("YouTube", "com.google.android.youtube") },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(12.dp), tint = CoralRed)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Shield Alert", fontSize = 10.sp, color = CoralRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // TIMER ACTION BUTTONS (Reset, Start/Pause, Skip)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                OutlinedIconButton(
                    onClick = onResetTimer,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("pomodoro_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RestartAlt,
                        contentDescription = "Reset Timer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Primary Start / Pause Sprint Button
                Button(
                    onClick = {
                        if (isTimerRunning) onPauseTimer() else onStartTimer()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTimerRunning) AmberGold else primaryColor
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .height(54.dp)
                        .width(180.dp)
                        .testTag("pomodoro_toggle_timer_button"),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Icon(
                        imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTimerRunning) "Pause Session" else if (isBreakStage) "Start Break" else "Start Sprint",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Skip to Next Stage Button
                OutlinedIconButton(
                    onClick = onSkipStage,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("pomodoro_skip_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Skip Stage",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
