package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiClient
import com.example.data.model.GamifiedGoalProgress
import com.example.data.model.GoalItem
import com.example.data.model.GoalQuestLevel
import com.example.data.model.QuestTask
import com.example.ui.theme.*
import kotlinx.coroutines.launch

/**
 * AI-Powered Gamified Goal Completer:
 * Turns student goals into an RPG-style questline with difficulty tiers, levels, tasks,
 * boss challenges, XP rewards, points system, and a rewards perks shop.
 */
@Composable
fun GamifiedGoalCompleter(
    goals: List<GoalItem>,
    modifier: Modifier = Modifier,
    onGoalCompleted: (GoalItem) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedGoalId by remember { mutableStateOf(goals.firstOrNull()?.id ?: 0L) }
    val currentGoal = remember(goals, selectedGoalId) {
        goals.firstOrNull { it.id == selectedGoalId } ?: goals.firstOrNull()
    }

    var isGeneratingQuest by remember { mutableStateOf(false) }
    var showPerksShop by remember { mutableStateOf(false) }
    var levelUpCelebrationTitle by remember { mutableStateOf<String?>(null) }

    // RPG Progression State
    var totalXp by remember { mutableIntStateOf(340) }
    var gemsCount by remember { mutableIntStateOf(95) }
    var activeLevels by remember {
        mutableStateOf<List<GoalQuestLevel>>(
            GeminiClient.getFallbackGamifiedLevels(
                goalTitle = currentGoal?.title ?: "Master Career Goal",
                category = currentGoal?.category ?: "Academics"
            )
        )
    }

    val currentLevelNumber = remember(activeLevels) {
        val firstUnfinished = activeLevels.firstOrNull { !it.isCompleted }?.levelNumber
        firstUnfinished ?: 5
    }

    val playerRank = when {
        totalXp >= 1500 -> "Grandmaster Legend 👑"
        totalXp >= 900 -> "Master Tactician ⚔️"
        totalXp >= 500 -> "Adept Strategist 🛡️"
        totalXp >= 250 -> "Apprentice Scholar 📜"
        else -> "Novice Initiate 🌱"
    }

    val nextLevelTargetXp = when (currentLevelNumber) {
        1 -> 250
        2 -> 500
        3 -> 900
        4 -> 1500
        else -> 3000
    }
    val xpProgress = (totalXp.toFloat() / nextLevelTargetXp).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("gamified_goal_completer")
    ) {
        // Player RPG Profile & Rewards Stats Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = AmberGold.copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MilitaryTech,
                                    contentDescription = null,
                                    tint = AmberGold,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "LEVEL $currentLevelNumber • $playerRank",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = AmberGold
                            )
                            Text(
                                text = "$totalXp / $nextLevelTargetXp XP",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Gems & Perks Shop Button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CyberCyan.copy(alpha = 0.15f),
                            modifier = Modifier.clickable { showPerksShop = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "💎 $gemsCount",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CyberCyan,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = "Shop",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // XP Progress Bar
                LinearProgressIndicator(
                    progress = { xpProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = AmberGold,
                    trackColor = MaterialTheme.colorScheme.surface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🔥 Streak Multiplier: 1.25x XP Bonus",
                        style = MaterialTheme.typography.labelSmall,
                        color = CoralOrange,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Perks Unlocked: Focus Armor, Cheat Day",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Goal Selection & AI Quest Generation Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Active Quest Goal:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentGoal?.title ?: "Select a Goal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Button(
                        onClick = {
                            currentGoal?.let { g ->
                                isGeneratingQuest = true
                                coroutineScope.launch {
                                    val result = GeminiClient.generateGamifiedQuest(
                                        goalTitle = g.title,
                                        category = g.category,
                                        dreamTitle = "Career Dream"
                                    )
                                    isGeneratingQuest = false
                                    result.onSuccess { levels ->
                                        activeLevels = levels
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp).testTag("generate_ai_quest_button")
                    ) {
                        if (isGeneratingQuest) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp))
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Re-Gamify with AI", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Level Quest Ladder
        Text(
            text = "Quest Stages & Boss Trials",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(activeLevels, key = { it.levelNumber }) { level ->
                QuestLevelCard(
                    level = level,
                    onTaskToggle = { task ->
                        // Toggle task
                        val updatedTasks = level.tasks.map {
                            if (it.id == task.id) it.copy(isDone = !it.isDone) else it
                        }
                        if (!task.isDone) {
                            totalXp += (task.xpValue * 1.25f).toInt()
                            gemsCount += 5
                        }
                        activeLevels = activeLevels.map {
                            if (it.levelNumber == level.levelNumber) it.copy(tasks = updatedTasks) else it
                        }
                    },
                    onDefeatBoss = {
                        // Boss defeated
                        totalXp += (level.xpReward * 1.25f).toInt()
                        gemsCount += 25
                        levelUpCelebrationTitle = "Defeated ${level.bossTrialTitle}!"
                        activeLevels = activeLevels.map { lvl ->
                            when (lvl.levelNumber) {
                                level.levelNumber -> lvl.copy(isCompleted = true)
                                level.levelNumber + 1 -> lvl.copy(isUnlocked = true)
                                else -> lvl
                            }
                        }
                    }
                )
            }
        }
    }

    // Level-up celebration dialog
    levelUpCelebrationTitle?.let { title ->
        AlertDialog(
            onDismissRequest = { levelUpCelebrationTitle = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Celebration, contentDescription = null, tint = AmberGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VICTORY! BOSS DEFEATED", fontWeight = FontWeight.Bold, color = AmberGold)
                }
            },
            text = {
                Column {
                    Text(text = title, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "You earned +${(300 * 1.25f).toInt()} XP and +25 Gems! Next stage unlocked. Keep compounding your discipline!")
                }
            },
            confirmButton = {
                Button(
                    onClick = { levelUpCelebrationTitle = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                ) {
                    Text("Continue Quest", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Rewards & Perks Shop Modal
    if (showPerksShop) {
        AlertDialog(
            onDismissRequest = { showPerksShop = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Storefront, contentDescription = null, tint = CyberCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rewards & Study Perks Shop", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Current Balance: 💎 $gemsCount Gems",
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                    PerkShopItem(
                        title = "🛡️ Distraction Shield Armor",
                        cost = 30,
                        description = "Gives cosmetic shield badge and 5% distraction block boost.",
                        canAfford = gemsCount >= 30,
                        onBuy = {
                            if (gemsCount >= 30) gemsCount -= 30
                        }
                    )
                    PerkShopItem(
                        title = "☕ Focus Cheat-Day Pass",
                        cost = 50,
                        description = "Freezes your daily streak if life interrupts your routine.",
                        canAfford = gemsCount >= 50,
                        onBuy = {
                            if (gemsCount >= 50) gemsCount -= 50
                        }
                    )
                    PerkShopItem(
                        title = "⚡ 2X XP Hour Sprint",
                        cost = 75,
                        description = "Doubles all XP earned in focus sprints for 60 minutes.",
                        canAfford = gemsCount >= 75,
                        onBuy = {
                            if (gemsCount >= 75) gemsCount -= 75
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPerksShop = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun QuestLevelCard(
    level: GoalQuestLevel,
    onTaskToggle: (QuestTask) -> Unit,
    onDefeatBoss: () -> Unit
) {
    val allTasksDone = level.tasks.all { it.isDone }
    val isLocked = !level.isUnlocked

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                level.isCompleted -> EmeraldGreen.copy(alpha = 0.08f)
                level.isUnlocked -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                level.isCompleted -> EmeraldGreen.copy(alpha = 0.5f)
                level.isUnlocked -> ElectricIndigo.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            level.isCompleted -> EmeraldGreen.copy(alpha = 0.2f)
                            level.isUnlocked -> ElectricIndigo.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        }
                    ) {
                        Text(
                            text = "LVL ${level.levelNumber}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = when {
                                level.isCompleted -> EmeraldGreen
                                level.isUnlocked -> ElectricIndigo
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = level.levelTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = level.levelRank,
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberGold
                        )
                    }
                }

                if (level.isCompleted) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EmeraldGreen.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "CLEARED ✅",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = level.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!isLocked) {
                Spacer(modifier = Modifier.height(10.dp))

                // Tasks checklist
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    level.tasks.forEach { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { onTaskToggle(task) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.isDone,
                                onCheckedChange = { onTaskToggle(task) },
                                colors = CheckboxDefaults.colors(checkedColor = EmeraldGreen),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = task.taskTitle,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (task.isDone) FontWeight.Normal else FontWeight.Medium,
                                color = if (task.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AmberGold.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "+${task.xpValue} XP",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberGold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Boss Trial Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CoralOrange.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "⚔️ BOSS TRIAL: ${level.bossTrialTitle}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CoralOrange
                                )
                            }
                            Text(
                                text = level.bossTrialDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onDefeatBoss,
                            enabled = allTasksDone && !level.isCompleted,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CoralOrange),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = if (level.isCompleted) "Defeated" else "Fight Boss",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PerkShopItem(
    title: String,
    cost: Int,
    description: String,
    canAfford: Boolean,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = description, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onBuy,
                enabled = canAfford,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text(text = "💎 $cost", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
