package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GoalItem
import com.example.ui.components.AddGoalDialog
import com.example.ui.components.GamifiedGoalCompleter
import com.example.ui.components.GoalCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricIndigo

enum class GoalViewMode(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    MILESTONES("Milestone Roadmaps", Icons.Default.Flag),
    AI_QUEST_GAME("AI Quest Game & Perks", Icons.Default.SportsEsports)
}

@Composable
fun GoalsScreen(
    goals: List<GoalItem>,
    onAddGoal: (title: String, category: String, priority: String, deadline: String, milestones: List<String>) -> Unit,
    onProgressChange: (goalId: Long, progress: Int) -> Unit,
    onDeleteGoal: (GoalItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }
    var currentViewMode by remember { mutableStateOf(GoalViewMode.MILESTONES) }

    val categories = listOf("All", "AI & Tech", "Academics", "Content Creation", "Skills")
    val filteredGoals = remember(goals, selectedCategory) {
        if (selectedCategory == "All") goals else goals.filter { it.category == selectedCategory }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("goals_screen"),
        floatingActionButton = {
            if (currentViewMode == GoalViewMode.MILESTONES) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = ElectricIndigo,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("add_goal_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Goal")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Mode Segmented Switcher (Milestone Roadmaps vs AI Quest Game)
            Spacer(modifier = Modifier.height(10.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                GoalViewMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = currentViewMode == mode,
                        onClick = { currentViewMode = mode },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = GoalViewMode.entries.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = if (mode == GoalViewMode.AI_QUEST_GAME) AmberGold.copy(alpha = 0.25f) else ElectricIndigo.copy(alpha = 0.2f),
                            activeContentColor = if (mode == GoalViewMode.AI_QUEST_GAME) AmberGold else ElectricIndigo
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = mode.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(mode.title, fontSize = 12.sp, fontWeight = if (currentViewMode == mode) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (currentViewMode == GoalViewMode.AI_QUEST_GAME) {
                // AI-Powered Gamified Goal Completer with levels and rewards
                GamifiedGoalCompleter(
                    goals = goals,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header summary
                    item {
                        Column {
                            Text(
                                text = "Master Career Goals",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Break down your grand career vision into executable milestones.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Banner encouraging AI Gamification
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { currentViewMode = GoalViewMode.AI_QUEST_GAME }
                                .testTag("banner_gamify_quest_btn"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SportsEsports,
                                        contentDescription = null,
                                        tint = AmberGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "PLAY GOALS AS RPG QUESTS",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = AmberGold
                                        )
                                        Text(
                                            text = "Level up, earn XP & defeat study bosses",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                FilledTonalButton(
                                    onClick = { currentViewMode = GoalViewMode.AI_QUEST_GAME },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Open Quest", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Category Filter Chips
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categories) { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                                        selectedLabelColor = CyberCyan
                                    )
                                )
                            }
                        }
                    }

                    // Goal list
                    if (filteredGoals.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Box(
                                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No goals found in this category. Tap '+' to create one!",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredGoals, key = { it.id }) { goal ->
                            GoalCard(
                                goal = goal,
                                onProgressChange = { progress -> onProgressChange(goal.id, progress) },
                                onDelete = { onDeleteGoal(goal) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddGoalDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, category, priority, deadline, milestones ->
                onAddGoal(title, category, priority, deadline, milestones)
                showAddDialog = false
            }
        )
    }
}
