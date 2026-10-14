package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppConnectorItem
import com.example.data.model.StudyHistoryItem
import com.example.data.model.ThemeMode
import com.example.data.model.UserAuthState
import com.example.ui.theme.*

enum class SystemHubSubTab(val title: String) {
    ACCOUNT_SYSTEM("Controls & Connectors"),
    HISTORY_STORE("History Store")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemConnectorsHistoryScreen(
    authState: UserAuthState,
    studyHistory: List<StudyHistoryItem>,
    appConnectors: List<AppConnectorItem>,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onSetThemeMode: (ThemeMode) -> Unit = {},
    isDndEnabled: Boolean,
    isKeepScreenOn: Boolean,
    isDailyMotivationalFcmEnabled: Boolean,
    isStudyAlertsFcmEnabled: Boolean,
    onSignInWithGoogle: () -> Unit,
    onSignOut: () -> Unit,
    onToggleDnd: (Boolean) -> Unit,
    onToggleKeepScreenOn: (Boolean) -> Unit,
    onToggleDailyMotivationalFcm: (Boolean) -> Unit,
    onToggleStudyAlertsFcm: (Boolean) -> Unit,
    onTriggerTestMotivation: () -> Unit,
    onTriggerTestStudyAlert: () -> Unit,
    onToggleConnector: (String) -> Unit,
    onDeleteHistoryItem: (StudyHistoryItem) -> Unit,
    onClearAllHistory: () -> Unit,
    onLogManualHistory: (type: String, title: String, duration: Int, notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentSubTab by remember { mutableStateOf(SystemHubSubTab.ACCOUNT_SYSTEM) }
    var selectedHistoryFilter by remember { mutableStateOf("All") }
    var showAddHistoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top Sub-Tabs
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("system_hub_segmented_row")
        ) {
            SystemHubSubTab.entries.forEachIndexed { index, tab ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = SystemHubSubTab.entries.size),
                    onClick = { currentSubTab = tab },
                    selected = currentSubTab == tab,
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = ElectricIndigo.copy(alpha = 0.2f),
                        activeContentColor = ElectricIndigo,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (currentSubTab == tab) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (currentSubTab) {
            SystemHubSubTab.ACCOUNT_SYSTEM -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // Google Sign-In Card
                    item {
                        GoogleAccountCard(
                            authState = authState,
                            onSignIn = onSignInWithGoogle,
                            onSignOut = onSignOut
                        )
                    }

                    // System Controls & FCM Alerts Hub
                    item {
                        SystemControlsCard(
                            themeMode = themeMode,
                            onSetThemeMode = onSetThemeMode,
                            isDndEnabled = isDndEnabled,
                            isKeepScreenOn = isKeepScreenOn,
                            isDailyMotivationalFcmEnabled = isDailyMotivationalFcmEnabled,
                            isStudyAlertsFcmEnabled = isStudyAlertsFcmEnabled,
                            onToggleDnd = onToggleDnd,
                            onToggleKeepScreenOn = onToggleKeepScreenOn,
                            onToggleDailyMotivationalFcm = onToggleDailyMotivationalFcm,
                            onToggleStudyAlertsFcm = onToggleStudyAlertsFcm,
                            onTriggerTestMotivation = onTriggerTestMotivation,
                            onTriggerTestStudyAlert = onTriggerTestStudyAlert
                        )
                    }

                    // App Connectors Section
                    item {
                        Text(
                            text = "Productivity App Connectors",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    items(appConnectors, key = { it.id }) { connector ->
                        AppConnectorCard(
                            connector = connector,
                            onToggle = { onToggleConnector(connector.id) },
                            onLaunch = {
                                try {
                                    val launchIntent = connector.packageName?.let { pkg ->
                                        context.packageManager.getLaunchIntentForPackage(pkg)
                                    }
                                    if (launchIntent != null) {
                                        context.startActivity(launchIntent)
                                    } else {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(connector.actionUri))
                                        context.startActivity(intent)
                                    }
                                } catch (e: Exception) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(connector.actionUri))
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
                }
            }

            SystemHubSubTab.HISTORY_STORE -> {
                val filteredList = remember(studyHistory, selectedHistoryFilter) {
                    if (selectedHistoryFilter == "All") studyHistory
                    else studyHistory.filter { it.sessionType.contains(selectedHistoryFilter, ignoreCase = true) }
                }

                val totalMinutes = remember(studyHistory) {
                    studyHistory.sumOf { it.durationMinutes }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // History Summary Metrics Card
                    item {
                        HistorySummaryCard(
                            totalSessions = studyHistory.size,
                            totalMinutes = totalMinutes,
                            onAddManual = { showAddHistoryDialog = true },
                            onClearAll = onClearAllHistory
                        )
                    }

                    // Filter Chips
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("All", "Focus Sprint", "Routine", "Milestone").forEach { filter ->
                                val isSelected = selectedHistoryFilter == filter
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedHistoryFilter = filter },
                                    label = { Text(filter, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ElectricIndigo.copy(alpha = 0.2f),
                                        selectedLabelColor = ElectricIndigo
                                    )
                                )
                            }
                        }
                    }

                    if (filteredList.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.History,
                                        contentDescription = null,
                                        tint = ElectricIndigo,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "No History Logs Found",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Completed study sessions, routines, and milestones will automatically appear here.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredList, key = { it.id }) { historyItem ->
                            HistoryItemCard(
                                item = historyItem,
                                onDelete = { onDeleteHistoryItem(historyItem) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddHistoryDialog) {
        AddHistoryDialog(
            onDismiss = { showAddHistoryDialog = false },
            onConfirm = { type, title, duration, notes ->
                onLogManualHistory(type, title, duration, notes)
                showAddHistoryDialog = false
            }
        )
    }
}

@Composable
private fun GoogleAccountCard(
    authState: UserAuthState,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (authState.isAuthenticated) ElectricIndigo.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (authState.isAuthenticated) Icons.Default.AccountCircle else Icons.Outlined.Person,
                        contentDescription = "User Avatar",
                        tint = if (authState.isAuthenticated) ElectricIndigo else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (authState.isAuthenticated) (authState.displayName ?: "Student Explorer") else "Sign in with Google",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (authState.isAuthenticated) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Account",
                                tint = CyberCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        text = if (authState.isAuthenticated) (authState.email ?: "Cloud sync active") else "Sync study history, routines & goals across devices",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (authState.isAuthenticated) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Firestore Cloud Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = onSignOut,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_sign_out")
                    ) {
                        Text("Sign Out", fontSize = 12.sp)
                    }
                }
            } else {
                Button(
                    onClick = onSignIn,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_sign_in_google")
                ) {
                    Icon(
                        imageVector = Icons.Default.Login,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connect Google Account", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SystemControlsCard(
    themeMode: ThemeMode,
    onSetThemeMode: (ThemeMode) -> Unit,
    isDndEnabled: Boolean,
    isKeepScreenOn: Boolean,
    isDailyMotivationalFcmEnabled: Boolean,
    isStudyAlertsFcmEnabled: Boolean,
    onToggleDnd: (Boolean) -> Unit,
    onToggleKeepScreenOn: (Boolean) -> Unit,
    onToggleDailyMotivationalFcm: (Boolean) -> Unit,
    onToggleStudyAlertsFcm: (Boolean) -> Unit,
    onTriggerTestMotivation: () -> Unit,
    onTriggerTestStudyAlert: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = ElectricIndigo
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Appearance & Theme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("theme_segmented_button_row")
            ) {
                ThemeMode.entries.forEachIndexed { index, mode ->
                    val isSelected = themeMode == mode
                    val icon = when (mode) {
                        ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                        ThemeMode.LIGHT -> Icons.Default.LightMode
                        ThemeMode.DARK -> Icons.Default.DarkMode
                    }
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = ThemeMode.entries.size),
                        onClick = { onSetThemeMode(mode) },
                        selected = isSelected,
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = mode.title,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = ElectricIndigo.copy(alpha = 0.2f),
                            activeContentColor = ElectricIndigo,
                            inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("theme_btn_${mode.name.lowercase()}")
                    ) {
                        Text(
                            text = when (mode) {
                                ThemeMode.SYSTEM -> "System"
                                ThemeMode.LIGHT -> "Light"
                                ThemeMode.DARK -> "Dark"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Text(
                text = themeMode.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = ElectricIndigo
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "System Controls & FCM Study Alerts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // DND Mode
            ControlSwitchRow(
                icon = Icons.Default.DoNotDisturb,
                title = "Study DND Focus Mode",
                subtitle = "Suppress non-essential alerts during focus sprints",
                checked = isDndEnabled,
                onCheckedChange = onToggleDnd,
                testTag = "switch_dnd"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.surfaceVariant)

            // Keep Screen Awake
            ControlSwitchRow(
                icon = Icons.Default.ScreenLockLandscape,
                title = "Keep Screen Awake",
                subtitle = "Prevent device sleep while focus timer is ticking",
                checked = isKeepScreenOn,
                onCheckedChange = onToggleKeepScreenOn,
                testTag = "switch_keep_awake"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.surfaceVariant)

            // FCM Daily Motivation
            ControlSwitchRow(
                icon = Icons.Default.NotificationsActive,
                title = "FCM Daily Motivational Reminders",
                subtitle = "Receive morning inspiration and ambition fuel via FCM",
                checked = isDailyMotivationalFcmEnabled,
                onCheckedChange = onToggleDailyMotivationalFcm,
                testTag = "switch_fcm_motivation"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.surfaceVariant)

            // FCM Study Alerts
            ControlSwitchRow(
                icon = Icons.Default.Alarm,
                title = "FCM Study Session Planned Alerts",
                subtitle = "High-priority alert when planned study blocks start",
                checked = isStudyAlertsFcmEnabled,
                onCheckedChange = onToggleStudyAlertsFcm,
                testTag = "switch_fcm_study_alerts"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Test FCM Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onTriggerTestMotivation,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("btn_test_fcm_motivation")
                ) {
                    Text("⚡ Test Motivation Alert", fontSize = 11.sp, maxLines = 1)
                }

                OutlinedButton(
                    onClick = onTriggerTestStudyAlert,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("btn_test_fcm_study")
                ) {
                    Text("⏰ Test Study Alert", fontSize = 11.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun ControlSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) ElectricIndigo else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun AppConnectorCard(
    connector: AppConnectorItem,
    onToggle: () -> Unit,
    onLaunch: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().testTag("card_connector_${connector.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(connector.accentColorHex).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = Color(connector.accentColorHex)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = connector.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        SuggestionChip(
                            onClick = { },
                            label = { Text(connector.category, fontSize = 10.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = null
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = connector.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (connector.isConnected) EmeraldGreen else MaterialTheme.colorScheme.outline)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (connector.isConnected) "Connected" else "Not linked",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (connector.isConnected) EmeraldGreen else MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onToggle,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_toggle_connector_${connector.id}")
                    ) {
                        Text(
                            text = if (connector.isConnected) "Disconnect" else "Link App",
                            fontSize = 12.sp,
                            color = if (connector.isConnected) MaterialTheme.colorScheme.outline else ElectricIndigo
                        )
                    }

                    FilledTonalButton(
                        onClick = onLaunch,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ElectricIndigo.copy(alpha = 0.12f),
                            contentColor = ElectricIndigo
                        ),
                        modifier = Modifier.testTag("btn_launch_connector_${connector.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Launch,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open App", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorySummaryCard(
    totalSessions: Int,
    totalMinutes: Int,
    onAddManual: () -> Unit,
    onClearAll: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "History Store Vault",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$totalSessions sessions logged • $totalMinutes total focus minutes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(onClick = onAddManual) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = "Log Session",
                            tint = ElectricIndigo
                        )
                    }
                    IconButton(onClick = onClearAll) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear All",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    item: StudyHistoryItem,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            item.sessionType.contains("Sprint") -> ElectricIndigo.copy(alpha = 0.15f)
                            item.sessionType.contains("Routine") -> EmeraldGreen.copy(alpha = 0.15f)
                            else -> AmberGold.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        item.sessionType.contains("Sprint") -> Icons.Default.Timer
                        item.sessionType.contains("Routine") -> Icons.Default.CheckCircle
                        else -> Icons.Default.EmojiEvents
                    },
                    contentDescription = null,
                    tint = when {
                        item.sessionType.contains("Sprint") -> ElectricIndigo
                        item.sessionType.contains("Routine") -> EmeraldGreen
                        else -> AmberGold
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.sessionType,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ElectricIndigo
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${item.durationMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (item.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun AddHistoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (type: String, title: String, duration: Int, notes: String) -> Unit
) {
    var type by remember { mutableStateOf("Focus Sprint") }
    var title by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("45") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Study Session to History", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Session / Topic Title") },
                    placeholder = { Text("e.g. Backpropagation Proofs") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Session Type") },
                    placeholder = { Text("Focus Sprint, Routine, Milestone") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it.filter { char -> char.isDigit() } },
                    label = { Text("Duration (Minutes)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Key Takeaways") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(type, title, duration.toIntOrNull() ?: 45, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
            ) {
                Text("Log Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
