package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnalyzedRoutineSuggestion
import com.example.data.model.CustomInstructionItem
import com.example.data.model.JournalOpinionItem
import com.example.data.model.ReminderType
import com.example.data.model.RoutineItem
import com.example.ui.components.AiRoutineScannerDialog
import com.example.ui.components.FirestoreRoutineProgressCard
import com.example.ui.theme.*

enum class RoutineJournalSubTab(val title: String) {
    ROUTINES("Daily Routines"),
    OPINIONS("Personal Opinions"),
    INSTRUCTIONS("Custom Instructions")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineJournalScreen(
    routines: List<RoutineItem>,
    opinions: List<JournalOpinionItem>,
    instructions: List<CustomInstructionItem>,
    isCloudSyncing: Boolean,
    cloudSyncError: String?,
    onToggleRoutineCompletion: (RoutineItem, Boolean) -> Unit,
    onToggleRoutineAlarm: (RoutineItem) -> Unit,
    onAddRoutine: (
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
    ) -> Unit,
    onDeleteRoutine: (RoutineItem) -> Unit,
    onManualSync: () -> Unit,
    onAddOpinion: (title: String, text: String, category: String, mood: String) -> Unit,
    onDeleteOpinion: (JournalOpinionItem) -> Unit,
    onAddInstruction: (title: String, text: String, targetScope: String) -> Unit,
    onToggleInstruction: (CustomInstructionItem, Boolean) -> Unit,
    onDeleteInstruction: (CustomInstructionItem) -> Unit,
    onImportAnalyzedRoutines: (List<AnalyzedRoutineSuggestion>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentSubTab by remember { mutableStateOf(RoutineJournalSubTab.ROUTINES) }
    var showAddRoutineDialog by remember { mutableStateOf(false) }
    var showAddOpinionDialog by remember { mutableStateOf(false) }
    var showAddInstructionDialog by remember { mutableStateOf(false) }
    var showAiScannerDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (currentSubTab) {
                        RoutineJournalSubTab.ROUTINES -> showAddRoutineDialog = true
                        RoutineJournalSubTab.OPINIONS -> showAddOpinionDialog = true
                        RoutineJournalSubTab.INSTRUCTIONS -> showAddInstructionDialog = true
                    }
                },
                containerColor = ElectricIndigo,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_item")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Item"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Sub Tab Selector
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("routine_journal_tab_row")
            ) {
                RoutineJournalSubTab.entries.forEachIndexed { index, tab ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = RoutineJournalSubTab.entries.size),
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
                            fontWeight = if (currentSubTab == tab) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (currentSubTab) {
                RoutineJournalSubTab.ROUTINES -> {
                    RoutinesSectionView(
                        routines = routines,
                        isCloudSyncing = isCloudSyncing,
                        cloudSyncError = cloudSyncError,
                        onToggleCompletion = onToggleRoutineCompletion,
                        onToggleAlarm = onToggleRoutineAlarm,
                        onDeleteRoutine = onDeleteRoutine,
                        onManualSync = onManualSync,
                        onOpenAiScanner = { showAiScannerDialog = true }
                    )
                }
                RoutineJournalSubTab.OPINIONS -> {
                    OpinionsSectionView(
                        opinions = opinions,
                        onDeleteOpinion = onDeleteOpinion
                    )
                }
                RoutineJournalSubTab.INSTRUCTIONS -> {
                    InstructionsSectionView(
                        instructions = instructions,
                        onToggleInstruction = onToggleInstruction,
                        onDeleteInstruction = onDeleteInstruction
                    )
                }
            }
        }
    }

    // Dialogs
    if (showAiScannerDialog) {
        AiRoutineScannerDialog(
            onDismiss = { showAiScannerDialog = false },
            onImportRoutines = { suggestions ->
                onImportAnalyzedRoutines(suggestions)
                showAiScannerDialog = false
            }
        )
    }

    if (showAddRoutineDialog) {
        AddRoutineDialog(
            onDismiss = { showAddRoutineDialog = false },
            onConfirm = { title, time, category, duration, alarm, reminderType, tone, sound, snooze, customMsg ->
                onAddRoutine(
                    title,
                    time,
                    category,
                    duration,
                    alarm,
                    reminderType,
                    tone,
                    sound,
                    snooze,
                    customMsg
                )
                showAddRoutineDialog = false
            }
        )
    }

    if (showAddOpinionDialog) {
        AddOpinionDialog(
            onDismiss = { showAddOpinionDialog = false },
            onConfirm = { title, text, category, mood ->
                onAddOpinion(title, text, category, mood)
                showAddOpinionDialog = false
            }
        )
    }

    if (showAddInstructionDialog) {
        AddInstructionDialog(
            onDismiss = { showAddInstructionDialog = false },
            onConfirm = { title, text, scope ->
                onAddInstruction(title, text, scope)
                showAddInstructionDialog = false
            }
        )
    }
}

@Composable
private fun RoutinesSectionView(
    routines: List<RoutineItem>,
    isCloudSyncing: Boolean,
    cloudSyncError: String?,
    onToggleCompletion: (RoutineItem, Boolean) -> Unit,
    onToggleAlarm: (RoutineItem) -> Unit,
    onDeleteRoutine: (RoutineItem) -> Unit,
    onManualSync: () -> Unit,
    onOpenAiScanner: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            FirestoreRoutineProgressCard(
                routines = routines,
                isCloudSyncing = isCloudSyncing,
                onManualSync = onManualSync
            )
        }

        // AI-Powered Routine Manager Image Scanner Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenAiScanner() }
                    .testTag("ai_routine_scanner_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CyberCyan.copy(alpha = 0.2f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Scan Routine",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "AI Routine Photo Scanner",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = CyberCyan.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "AI VISION",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberCyan,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Upload picture of your schedule/timetable to auto-generate routines & reminders",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onOpenAiScanner) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open Scanner",
                            tint = CyberCyan
                        )
                    }
                }
            }
        }

        if (cloudSyncError != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cloud Sync Error: $cloudSyncError",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        if (routines.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Outlined.Checklist,
                    title = "No Daily Routines Yet",
                    subtitle = "Tap '+' to add your morning deep work, coding blocks, or revision habits."
                )
            }
        } else {
            items(routines, key = { it.id }) { routine ->
                RoutineCardItem(
                    routine = routine,
                    onToggleCompletion = { onToggleCompletion(routine, !routine.isCompletedToday) },
                    onToggleAlarm = { onToggleAlarm(routine) },
                    onDelete = { onDeleteRoutine(routine) }
                )
            }
        }
    }
}

@Composable
private fun OpinionsSectionView(
    opinions: List<JournalOpinionItem>,
    onDeleteOpinion: (JournalOpinionItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CyberCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = CyberCyan
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Personal Opinions & Mindset Journal",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Write your engineering philosophies, study epiphanies, and honest perspectives.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (opinions.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Outlined.EditNote,
                    title = "No Personal Reflections Yet",
                    subtitle = "Capture your daily thoughts, academic opinions, and mindset breakthroughs."
                )
            }
        } else {
            items(opinions, key = { it.id }) { opinion ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text(opinion.category, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = ElectricIndigo.copy(alpha = 0.1f),
                                        labelColor = ElectricIndigo
                                    ),
                                    border = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text("Mood: ${opinion.mood}", fontSize = 11.sp) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = EmeraldGreen.copy(alpha = 0.1f),
                                        labelColor = EmeraldGreen
                                    ),
                                    border = null
                                )
                            }

                            IconButton(onClick = { onDeleteOpinion(opinion) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = opinion.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = opinion.opinionText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Recorded: ${opinion.dateCreated}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InstructionsSectionView(
    instructions: List<CustomInstructionItem>,
    onToggleInstruction: (CustomInstructionItem, Boolean) -> Unit,
    onDeleteInstruction: (CustomInstructionItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AmberGold.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = AmberGold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Custom Directives & Operating Rules",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Set non-negotiable rules for your study sprints, AI tutor behaviors, and habit triggers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (instructions.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Outlined.Rule,
                    title = "No Custom Directives",
                    subtitle = "Define operating rules to keep yourself accountable and customize your AI Mentor."
                )
            }
        } else {
            items(instructions, key = { it.id }) { item ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (item.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (item.isActive) 2.dp else 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text(item.targetScope, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = ElectricIndigo.copy(alpha = 0.1f),
                                        labelColor = ElectricIndigo
                                    ),
                                    border = null
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.instructionText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Switch(
                                checked = item.isActive,
                                onCheckedChange = { onToggleInstruction(item, it) },
                                modifier = Modifier.testTag("switch_instruction_${item.id}")
                            )
                            IconButton(
                                onClick = { onDeleteInstruction(item) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.outline
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
private fun RoutineCardItem(
    routine: RoutineItem,
    onToggleCompletion: () -> Unit,
    onToggleAlarm: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (routine.isCompletedToday) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = routine.isCompletedToday,
                onCheckedChange = { onToggleCompletion() },
                colors = CheckboxDefaults.colors(
                    checkedColor = EmeraldGreen,
                    checkmarkColor = Color.White
                ),
                modifier = Modifier.testTag("routine_checkbox_${routine.id}")
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routine.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (routine.isCompletedToday) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⏰ ${routine.scheduledTime}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricIndigo,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${routine.durationMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${routine.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                if (routine.alarmEnabled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (routine.reminderType == "PSYCHOLOGICAL_MESSAGE") CyberCyan.copy(alpha = 0.15f)
                        else if (routine.reminderType == "BOTH") ElectricIndigo.copy(alpha = 0.15f)
                        else AmberGold.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = when (routine.reminderType) {
                                "PSYCHOLOGICAL_MESSAGE" -> "🧠 Mindset Nudge: ${routine.psychologicalTone}"
                                "BOTH" -> "⚡ Dual: Nudge + Alarm (${routine.alarmSound})"
                                else -> "⏰ Alarm: ${routine.alarmSound} (${routine.snoozeMinutes}m)"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = if (routine.reminderType == "PSYCHOLOGICAL_MESSAGE") CyberCyan
                            else if (routine.reminderType == "BOTH") ElectricIndigo
                            else AmberGold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            IconButton(onClick = onToggleAlarm) {
                Icon(
                    imageVector = if (routine.alarmEnabled) Icons.Default.AlarmOn else Icons.Default.AlarmOff,
                    contentDescription = "Toggle Alarm",
                    tint = if (routine.alarmEnabled) AmberGold else MaterialTheme.colorScheme.outline
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Routine",
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ElectricIndigo,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun AddRoutineDialog(
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
    var duration by remember { mutableStateOf("45") }
    var reminderType by remember { mutableStateOf("BOTH") }
    var psychologicalTone by remember { mutableStateOf("Growth Mindset & Discipline") }
    var alarmSound by remember { mutableStateOf("Chime") }
    var snoozeMinutes by remember { mutableIntStateOf(5) }
    var customMessage by remember { mutableStateOf("") }

    val tones = listOf(
        "Growth Mindset & Discipline",
        "Gentle Encouragement",
        "Stoic Challenge",
        "Empathetic Coach",
        "Dream Affirmation"
    )
    val sounds = listOf("Chime", "Radar", "Siren", "Bell")
    val snoozes = listOf(5, 10, 15)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Daily Routine & Reminders", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Routine Name") },
                        placeholder = { Text("e.g., Coding Algorithms & LeetCode") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Scheduled Time") },
                        placeholder = { Text("06:30 AM") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it.filter { char -> char.isDigit() } },
                        label = { Text("Duration (Minutes)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Dual Reminder Mode Selection
                item {
                    Text(
                        text = "🔔 Choose Reminder Type",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            "BOTH" to "⚡ Both: Mindset Nudge + Loud Alarm",
                            "PSYCHOLOGICAL_MESSAGE" to "🧠 Psychological Message (Mindset Nudge)",
                            "DIRECT_ALARM" to "⏰ Direct Alarm (Loud Alert & Snooze)",
                            "NONE" to "🔕 None (Silent Checklist)"
                        ).forEach { (typeKey, label) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { reminderType = typeKey }
                                    .padding(vertical = 2.dp)
                            ) {
                                RadioButton(
                                    selected = (reminderType == typeKey),
                                    onClick = { reminderType = typeKey }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (reminderType == typeKey) ElectricIndigo else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Psychological Customization
                if (reminderType == "PSYCHOLOGICAL_MESSAGE" || reminderType == "BOTH") {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CyberCyan.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "🧠 Psychological Nudge Tone:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberCyan
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                tones.forEach { tone ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { psychologicalTone = tone }
                                            .padding(vertical = 2.dp)
                                    ) {
                                        RadioButton(
                                            selected = (psychologicalTone == tone),
                                            onClick = { psychologicalTone = tone }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(tone, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = customMessage,
                                    onValueChange = { customMessage = it },
                                    label = { Text("Custom Message (optional)") },
                                    placeholder = { Text("e.g. Focus on what matters!") },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                // Direct Alarm Customization
                if (reminderType == "DIRECT_ALARM" || reminderType == "BOTH") {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = AmberGold.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "⏰ Direct Alarm Ringtone:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberGold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    sounds.forEach { snd ->
                                        FilterChip(
                                            selected = (alarmSound == snd),
                                            onClick = { alarmSound = snd },
                                            label = { Text(snd, fontSize = 11.sp) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Snooze Duration: ${snoozeMinutes}m",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    snoozes.forEach { s ->
                                        FilterChip(
                                            selected = (snoozeMinutes == s),
                                            onClick = { snoozeMinutes = s },
                                            label = { Text("${s}m", fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            title,
                            time,
                            category,
                            duration.toIntOrNull() ?: 45,
                            (reminderType != "NONE"),
                            reminderType,
                            psychologicalTone,
                            alarmSound,
                            snoozeMinutes,
                            customMessage
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
            ) {
                Text("Save Routine")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AddOpinionDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, text: String, category: String, mood: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Study Philosophy") }
    var mood by remember { mutableStateOf("Focused") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Personal Opinion / Reflection", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reflection Title / Topic") },
                    placeholder = { Text("e.g. Why Focus Beats Talent") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Opinion & Thought") },
                    placeholder = { Text("Write your thoughts in detail...") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = mood,
                    onValueChange = { mood = it },
                    label = { Text("Current Mood (e.g., Focused, Creative)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && text.isNotBlank()) {
                        onConfirm(title, text, category, mood)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
            ) {
                Text("Save Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AddInstructionDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, text: String, scope: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var scope by remember { mutableStateOf("AI Tutor Directives") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Directive / Rule", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Rule Title") },
                    placeholder = { Text("e.g. Socratic Questioning First") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Instruction Directive") },
                    placeholder = { Text("Specific instructions or strict rules...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = scope,
                    onValueChange = { scope = it },
                    label = { Text("Target Scope (e.g. AI Tutor Directives, Sprint Rule)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && text.isNotBlank()) {
                        onConfirm(title, text, scope)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
            ) {
                Text("Register Rule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
