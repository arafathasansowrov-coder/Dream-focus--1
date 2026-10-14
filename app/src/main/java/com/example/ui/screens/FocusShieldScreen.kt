package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BlockedAppRule
import com.example.data.model.DesignatedStudyApp
import com.example.data.model.GoalItem
import com.example.ui.components.AddBlockedAppDialog
import com.example.ui.components.AddStudyAppDialog
import com.example.ui.components.BlockedAppItemCard
import com.example.ui.components.DesignatedStudyAppCard
import com.example.ui.theme.*

enum class ShieldMatrixTab(val title: String) {
    STUDY_APPS("Designated Study Apps"),
    BLOCKED_APPS("Blocked Distractions")
}

@Composable
fun FocusShieldScreen(
    isTimerRunning: Boolean,
    timeRemainingSeconds: Int,
    totalSessionSeconds: Int,
    selectedGoal: GoalItem?,
    goals: List<GoalItem>,
    blockedApps: List<BlockedAppRule>,
    designatedStudyApps: List<DesignatedStudyApp>,
    distractionsBlockedCount: Int,
    isShieldServiceRunning: Boolean,
    shieldDurationSeconds: Int,
    hasUsageStatsPermission: Boolean,
    onToggleShieldService: () -> Unit,
    onRequestUsageStatsPermission: () -> Unit,
    onToggleStudyApp: (packageName: String, isEnabled: Boolean) -> Unit,
    onAddStudyApp: (packageName: String, appName: String, category: String, uri: String) -> Unit,
    onDeleteStudyApp: (packageName: String) -> Unit,
    onSimulateNavigatedAway: (appName: String, packageName: String) -> Unit,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onSetDuration: (Int, GoalItem?) -> Unit,
    onToggleBlock: (String, Boolean) -> Unit,
    onAddBlockedApp: (packageName: String, appName: String, category: String, limit: Int, isStrict: Boolean) -> Unit,
    onDeleteBlockedApp: (BlockedAppRule) -> Unit,
    onSimulateBlock: (appName: String, packageName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddAppDialog by remember { mutableStateOf(false) }
    var showAddStudyAppDialog by remember { mutableStateOf(false) }
    var activeMatrixTab by remember { mutableStateOf(ShieldMatrixTab.STUDY_APPS) }

    val progress = if (totalSessionSeconds > 0) {
        (totalSessionSeconds - timeRemainingSeconds).toFloat() / totalSessionSeconds
    } else 0f

    val minutes = timeRemainingSeconds / 60
    val seconds = timeRemainingSeconds % 60

    val serviceMinutes = shieldDurationSeconds / 60
    val serviceSeconds = shieldDurationSeconds % 60

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("focus_shield_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Distraction Shield & Focus Lock",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Monitors app transitions via foreground service to alert when you leave designated study apps.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Foreground Service Distraction Shield Banner & Controls
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("foreground_service_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isShieldServiceRunning)
                        EmeraldGreen.copy(alpha = 0.10f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isShieldServiceRunning) EmeraldGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (isShieldServiceRunning) EmeraldGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isShieldServiceRunning) Icons.Default.Security else Icons.Outlined.Security,
                                        contentDescription = "Shield Service",
                                        tint = if (isShieldServiceRunning) EmeraldGreen else CyberCyan,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Foreground Distraction Shield",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isShieldServiceRunning)
                                        "● MONITORING ACTIVE (%02d:%02d)".format(serviceMinutes, serviceSeconds)
                                    else
                                        "○ STANDBY (Tap Activate to Start)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isShieldServiceRunning) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Button(
                            onClick = onToggleShieldService,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isShieldServiceRunning) CoralRed else EmeraldGreen
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("toggle_shield_service_btn")
                        ) {
                            Icon(
                                imageVector = if (isShieldServiceRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isShieldServiceRunning) "Stop Shield" else "Activate Shield",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isShieldServiceRunning)
                            "DreamTrack's foreground service is actively monitoring app transitions in the background. If you leave designated study apps, you'll receive an instant high-priority alert and haptic feedback."
                        else
                            "Keep your deep work uninterrupted. When enabled, DreamTrack monitors your device to ensure you stay inside designated study tools.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Permission & Quick Test Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!hasUsageStatsPermission) {
                            FilledTonalButton(
                                onClick = onRequestUsageStatsPermission,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = AmberGold.copy(alpha = 0.2f),
                                    contentColor = AmberGold
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("grant_usage_stats_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Grant Usage Access", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldGreen.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Usage Access Granted", fontSize = 11.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { onSimulateNavigatedAway("YouTube", "com.google.android.youtube") },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("simulate_nav_away_btn")
                        ) {
                            Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(14.dp), tint = CoralRed)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Alert", fontSize = 11.sp, color = CoralRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Circular Focus Timer Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("focus_timer_container"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Linked Goal Chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.testTag("linked_goal_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedGoal?.title ?: "General Deep Study Sprint",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Circular Countdown Timer
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(200.dp)
                    ) {
                        val trackColor = MaterialTheme.colorScheme.surface
                        val indicatorColor = if (isTimerRunning) EmeraldGreen else ElectricIndigo

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Background track
                            drawCircle(
                                color = trackColor,
                                style = Stroke(width = 14.dp.toPx())
                            )
                            // Animated active arc
                            drawArc(
                                color = indicatorColor,
                                startAngle = -90f,
                                sweepAngle = (1f - progress) * 360f,
                                useCenter = false,
                                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "%02d:%02d".format(minutes, seconds),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = if (isTimerRunning) "TIMER TICKING" else "READY TO FOCUS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isTimerRunning) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Session Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(25 to "25m", 45 to "45m", 60 to "60m", 90 to "90m").forEach { (min, label) ->
                            val isSelected = totalSessionSeconds == min * 60
                            FilledTonalButton(
                                onClick = { onSetDuration(min, selectedGoal) },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isSelected) ElectricIndigo else MaterialTheme.colorScheme.surface,
                                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Timer Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedIconButton(
                            onClick = onResetTimer,
                            modifier = Modifier.size(48.dp).testTag("reset_timer_button")
                        ) {
                            Icon(imageVector = Icons.Outlined.RestartAlt, contentDescription = "Reset")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = {
                                if (isTimerRunning) onPauseTimer() else onStartTimer()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTimerRunning) AmberGold else EmeraldGreen
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .width(160.dp)
                                .testTag("toggle_timer_button")
                        ) {
                            Icon(
                                imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTimerRunning) "Pause" else "Start Sprint",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // App Matrix Segmented Control: Designated Study Apps Whitelist vs Blocked Distraction Blacklist
        item {
            Column {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        onClick = { activeMatrixTab = ShieldMatrixTab.STUDY_APPS },
                        selected = activeMatrixTab == ShieldMatrixTab.STUDY_APPS,
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = EmeraldGreen.copy(alpha = 0.2f),
                            activeContentColor = EmeraldGreen
                        ),
                        modifier = Modifier.testTag("tab_study_apps")
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Designated Study Apps (${designatedStudyApps.count { it.isEnabled }})",
                            fontSize = 11.sp,
                            fontWeight = if (activeMatrixTab == ShieldMatrixTab.STUDY_APPS) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        onClick = { activeMatrixTab = ShieldMatrixTab.BLOCKED_APPS },
                        selected = activeMatrixTab == ShieldMatrixTab.BLOCKED_APPS,
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = CoralRed.copy(alpha = 0.2f),
                            activeContentColor = CoralRed
                        ),
                        modifier = Modifier.testTag("tab_blocked_apps")
                    ) {
                        Icon(imageVector = Icons.Default.Block, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Blocked Apps (${blockedApps.size})",
                            fontSize = 11.sp,
                            fontWeight = if (activeMatrixTab == ShieldMatrixTab.BLOCKED_APPS) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Content for Designated Study Apps Tab
        if (activeMatrixTab == ShieldMatrixTab.STUDY_APPS) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Approved Study Tools Whitelist",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Navigating away from these apps triggers immediate shield alerts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showAddStudyAppDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_study_app_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Study App", fontSize = 12.sp)
                    }
                }
            }

            if (designatedStudyApps.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No designated study apps configured. Tap Add Study App to permit tools.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(designatedStudyApps, key = { it.packageName }) { app ->
                    DesignatedStudyAppCard(
                        app = app,
                        onToggle = { isEnabled -> onToggleStudyApp(app.packageName, isEnabled) },
                        onLaunch = {
                            try {
                                val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                if (launchIntent != null) {
                                    context.startActivity(launchIntent)
                                } else if (app.actionUri.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(app.actionUri))
                                    context.startActivity(intent)
                                }
                            } catch (e: Exception) {
                                // Fallback
                            }
                        },
                        onDelete = { onDeleteStudyApp(app.packageName) }
                    )
                }
            }
        } else {
            // Content for Blocked Apps Tab
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "App Blocker Shield Matrix",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "$distractionsBlockedCount distraction attempts blocked today",
                            style = MaterialTheme.typography.bodySmall,
                            color = CoralRed
                        )
                    }

                    Button(
                        onClick = { showAddAppDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_blocked_app_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add App", fontSize = 12.sp)
                    }
                }
            }

            if (blockedApps.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No apps in distraction shield. Tap Add App to blacklist time-wasters.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(blockedApps, key = { it.packageName }) { app ->
                    BlockedAppItemCard(
                        app = app,
                        onToggleBlock = { isActive -> onToggleBlock(app.packageName, isActive) },
                        onSimulateBlock = { onSimulateBlock(app.appName, app.packageName) },
                        onDelete = { onDeleteBlockedApp(app) }
                    )
                }
            }
        }

        // Technical Architecture / Setup Guide for Android OS Foreground Service & Usage Stats
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SettingsAccessibility,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "How Distraction Shield Works",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "1. Android Foreground Service: Runs continuously with a persistent notification so focus monitoring stays alive even when you switch between approved study apps.\n2. UsageStatsManager: Real-time event inspection detects when any non-study application enters the foreground.\n3. Haptic & Sound Alerts: Fires immediate vibration waveforms and high-priority heads-up notifications instructing you to return to your study sprint.\n4. Whitelist Architecture: Only apps added to your Designated Study Tools whitelist are permitted during active focus sessions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }

    if (showAddAppDialog) {
        AddBlockedAppDialog(
            onDismiss = { showAddAppDialog = false },
            onConfirm = { pkg, name, cat, limit, strict ->
                onAddBlockedApp(pkg, name, cat, limit, strict)
                showAddAppDialog = false
            }
        )
    }

    if (showAddStudyAppDialog) {
        AddStudyAppDialog(
            onDismiss = { showAddStudyAppDialog = false },
            onConfirm = { pkg, name, cat, uri ->
                onAddStudyApp(pkg, name, cat, uri)
                showAddStudyAppDialog = false
            }
        )
    }
}
