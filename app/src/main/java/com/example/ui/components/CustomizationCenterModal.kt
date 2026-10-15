package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ThemeMode
import com.example.ui.theme.*

/**
 * Universal Customization Center:
 * Gives students complete freedom to customize their avatars, titles, themes,
 * focus preferences, and AI operating directives.
 */
@Composable
fun CustomizationCenterModal(
    themeMode: ThemeMode,
    onSetThemeMode: (ThemeMode) -> Unit,
    currentAvatarEmoji: String,
    onSetAvatarEmoji: (String) -> Unit,
    currentPlayerTitle: String,
    onSetPlayerTitle: (String) -> Unit,
    isLeaderboardEnabled: Boolean,
    onToggleLeaderboard: (Boolean) -> Unit,
    onExecuteAiCommand: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var titleInput by remember { mutableStateOf(currentPlayerTitle) }
    var selectedColorIndex by remember { mutableIntStateOf(0) }

    val avatarOptions = listOf("🚀", "🧠", "⚡", "👑", "🛡️", "⚔️", "🎓", "🌌", "💻", "🔥", "🧬", "🎯")
    val titleSuggestions = listOf(
        "Visionary Architect",
        "Autonomous Systems Lead",
        "Quantum Computing Scholar",
        "Deep Work Strategist",
        "Neurotechnology Pioneer",
        "Cognitive Science Researcher"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .testTag("customization_center_modal"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = ElectricIndigo.copy(alpha = 0.2f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = ElectricIndigo,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Customization Center",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Customize theme, avatar, title & AI controls",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_customization_modal_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Theme Mode
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "🎨 Appearance & Theme",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ThemeMode.entries.forEach { mode ->
                                        val isSelected = themeMode == mode
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { onSetThemeMode(mode) },
                                            label = { Text(mode.title, fontSize = 11.sp) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Avatar Emoji Selector
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "🎭 Player Avatar Emoji",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Currently: $currentAvatarEmoji",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(avatarOptions) { emoji ->
                                        val isSelected = emoji == currentAvatarEmoji
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) ElectricIndigo.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
                                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, ElectricIndigo) else null,
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clickable { onSetAvatarEmoji(emoji) }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(text = emoji, fontSize = 22.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Custom Player Rank Title
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "🎖️ Custom Player Rank Title",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = titleInput,
                                    onValueChange = { titleInput = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    placeholder = { Text("e.g. Autonomous AI Researcher") },
                                    trailingIcon = {
                                        IconButton(onClick = { onSetPlayerTitle(titleInput) }) {
                                            Icon(imageVector = Icons.Default.Check, contentDescription = "Save Title", tint = ElectricIndigo)
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Quick Presets:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(titleSuggestions) { preset ->
                                        AssistChip(
                                            onClick = {
                                                titleInput = preset
                                                onSetPlayerTitle(preset)
                                            },
                                            label = { Text(preset, fontSize = 10.sp) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Quick AI Autonomous Commands
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "⚡ Instant AI Action Prompts",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberCyan
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = CyberCyan.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "FULL APP ACCESS",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 8.sp,
                                            color = CyberCyan,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "AI has autonomous permission to create, change, or dismiss any feature for you.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                val aiCommands = listOf(
                                    "🎯 Create goal Master Deep Learning in AI & Tech" to "Create Goal",
                                    "⏰ Create routine Morning Algorithm Sprint at 07:00 AM" to "Add Routine",
                                    "⏱️ Start focus session for 30 minutes" to "Start 30m Timer",
                                    "🛡️ Turn on distraction shield" to "Activate Shield",
                                    "⭐ Award 150 points for reading textbook" to "+150 XP Reward",
                                    "🌙 Switch to dark theme" to "Dark Mode",
                                    "🏆 Enable leaderboard" to "Enable Leaderboard"
                                )
                                aiCommands.forEach { (command, label) ->
                                    OutlinedButton(
                                        onClick = {
                                            onExecuteAiCommand(command)
                                            onDismiss()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
