package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RoutineItem
import com.example.ui.components.AddRoutineDialog
import com.example.ui.components.CategoryBadge
import com.example.ui.components.FirestoreRoutineProgressCard
import com.example.ui.theme.*

/**
 * Screen that displays daily routines fetched from Cloud Firestore in real-time,
 * complete with interactive checkboxes to toggle completion status.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirestoreRoutinesScreen(
    routines: List<RoutineItem>,
    isCloudSyncing: Boolean,
    cloudSyncError: String?,
    onToggleCompletion: (routine: RoutineItem, isCompleted: Boolean) -> Unit,
    onToggleAlarm: (RoutineItem) -> Unit,
    onAddRoutine: (title: String, time: String, category: String, duration: Int, alarm: Boolean) -> Unit,
    onDeleteRoutine: (RoutineItem) -> Unit,
    onManualSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    val completedCount = routines.count { it.isCompletedToday }
    val totalCount = routines.size
    val completionPercentage = if (totalCount > 0) (completedCount.toFloat() / totalCount * 100).toInt() else 0

    // Filter routines based on user selection
    val filteredRoutines = remember(routines, selectedFilter, selectedCategoryFilter) {
        routines.filter { routine ->
            val matchesStatus = when (selectedFilter) {
                "Pending" -> !routine.isCompletedToday
                "Completed" -> routine.isCompletedToday
                else -> true
            }
            val matchesCategory = selectedCategoryFilter == null || routine.category == selectedCategoryFilter
            matchesStatus && matchesCategory
        }
    }

    val categories = remember(routines) {
        routines.map { it.category }.distinct()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("firestore_routines_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ElectricIndigo,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("firestore_add_routine_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Firestore Routine")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title & Cloud Sync Badge
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Habits & Routines",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Synced in real-time with Google Cloud Firestore",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Cloud Sync Indicator Chip
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isCloudSyncing)
                                AmberGold.copy(alpha = 0.15f)
                            else if (cloudSyncError != null)
                                CoralRed.copy(alpha = 0.15f)
                            else
                                CyberCyan.copy(alpha = 0.15f),
                            border = BorderStroke(
                                1.dp,
                                if (isCloudSyncing) AmberGold else if (cloudSyncError != null) CoralRed else CyberCyan.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onManualSync() }
                                .testTag("firestore_sync_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isCloudSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 2.dp,
                                        color = AmberGold
                                    )
                                    Text(
                                        text = "Syncing...",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AmberGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (cloudSyncError != null) Icons.Default.CloudOff else Icons.Default.CloudDone,
                                        contentDescription = "Cloud Sync Status",
                                        tint = if (cloudSyncError != null) CoralRed else CyberCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (cloudSyncError != null) "Offline" else "Firestore Live",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (cloudSyncError != null) CoralRed else CyberCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Error banner if any
            if (cloudSyncError != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CoralRed.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, CoralRed.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = CoralRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sync alert: Local cache active. Tap sync to retry.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CoralRed
                                )
                            }
                            TextButton(onClick = onManualSync) {
                                Text("Retry", color = CoralRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Real-Time Discipline Rate Progress Card using Material3
            item {
                FirestoreRoutineProgressCard(
                    routines = routines,
                    isCloudSynced = (cloudSyncError == null)
                )
            }

            // Filter Chips (Status & Category)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Status filters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Pending", "Completed").forEach { filter ->
                            val isSelected = selectedFilter == filter
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = filter },
                                label = {
                                    Text(
                                        text = when (filter) {
                                            "All" -> "All ($totalCount)"
                                            "Pending" -> "Pending (${totalCount - completedCount})"
                                            else -> "Done ($completedCount)"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricIndigo,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("filter_chip_${filter.lowercase()}")
                            )
                        }
                    }

                    // Category filters if available
                    if (categories.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedCategoryFilter == null,
                                    onClick = { selectedCategoryFilter = null },
                                    label = { Text("All Categories", fontSize = 11.sp) }
                                )
                            }
                            items(categories) { cat ->
                                FilterChip(
                                    selected = selectedCategoryFilter == cat,
                                    onClick = {
                                        selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                                    },
                                    label = { Text(cat, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // List of Routines with Firestore Checkboxes
            if (filteredRoutines.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = ElectricIndigo.copy(alpha = 0.15f),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.TaskAlt,
                                        contentDescription = null,
                                        tint = ElectricIndigo,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (routines.isEmpty()) "No routines in Firestore" else "No routines match filter",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (routines.isEmpty())
                                    "Add daily study blocks, coding sessions, or revision habits to track and sync with Firestore."
                                else
                                    "Try clearing category or status filters to view all routines.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            if (routines.isEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showAddDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add First Routine", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredRoutines, key = { it.id }) { routine ->
                    FirestoreRoutineItemCard(
                        routine = routine,
                        onToggleCompletion = { isChecked ->
                            onToggleCompletion(routine, isChecked)
                        },
                        onToggleAlarm = { onToggleAlarm(routine) },
                        onDelete = { onDeleteRoutine(routine) }
                    )
                }
            }
        }

        // Add Routine Dialog
        if (showAddDialog) {
            AddRoutineDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, time, category, duration, alarm ->
                    onAddRoutine(title, time, category, duration, alarm)
                    showAddDialog = false
                }
            )
        }
    }
}

/**
 * Routine item card featuring an interactive Material 3 Checkbox,
 * time-of-day badges, category pills, duration indicator, and alarm toggles.
 */
@Composable
fun FirestoreRoutineItemCard(
    routine: RoutineItem,
    onToggleCompletion: (Boolean) -> Unit,
    onToggleAlarm: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDone = routine.isCompletedToday

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("firestore_routine_card_${routine.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isDone)
                EmeraldGreen.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Interactive Material 3 Checkbox with custom theme styling
            Checkbox(
                checked = isDone,
                onCheckedChange = { checked ->
                    onToggleCompletion(checked)
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = EmeraldGreen,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    checkmarkColor = Color.White
                ),
                modifier = Modifier
                    .size(36.dp)
                    .testTag("firestore_routine_checkbox_${routine.id}")
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Main Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routine.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isDone) FontWeight.Medium else FontWeight.SemiBold,
                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (isDone)
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        else
                            MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Time Tag
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CyberCyan.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = getTimeIcon(routine.scheduledTime),
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = routine.scheduledTime,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan
                            )
                        }
                    }

                    // Duration Chip
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${routine.durationMinutes}m",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Category Badge
                    CategoryBadge(category = routine.category)
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Alarm Toggle Button
            IconButton(
                onClick = onToggleAlarm,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("firestore_routine_alarm_${routine.id}")
            ) {
                Icon(
                    imageVector = if (routine.alarmEnabled) Icons.Default.NotificationsActive else Icons.Outlined.NotificationsOff,
                    contentDescription = "Toggle Alarm",
                    tint = if (routine.alarmEnabled) AmberGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Delete Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("firestore_routine_delete_${routine.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete Routine",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Returns an appropriate icon based on scheduled time of day.
 */
private fun getTimeIcon(scheduledTime: String): androidx.compose.ui.graphics.vector.ImageVector {
    val upper = scheduledTime.uppercase()
    return when {
        upper.contains("AM") -> Icons.Default.WbSunny
        upper.contains("PM") && (upper.startsWith("12") || upper.startsWith("01") || upper.startsWith("02") || upper.startsWith("03") || upper.startsWith("04") || upper.startsWith("05")) -> Icons.Default.WbTwilight
        else -> Icons.Default.NightsStay
    }
}
