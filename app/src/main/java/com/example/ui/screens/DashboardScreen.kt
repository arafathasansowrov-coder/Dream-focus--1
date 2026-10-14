package com.example.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GoalItem
import com.example.data.model.MotivationQuote
import com.example.data.model.RoutineItem
import com.example.data.model.StudentProfile
import com.example.data.model.WeeklyProductivityDay
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    profile: StudentProfile,
    goals: List<GoalItem>,
    routines: List<RoutineItem>,
    quotes: List<MotivationQuote>,
    isTimerRunning: Boolean,
    timeRemainingSeconds: Int,
    weeklyDays: List<WeeklyProductivityDay> = emptyList(),
    onNavigateToFocus: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToRoutines: () -> Unit,
    onNavigateToAiChat: () -> Unit,
    onNavigateToAiPlanner: () -> Unit,
    onToggleRoutine: (RoutineItem) -> Unit,
    onToggleAlarm: (RoutineItem) -> Unit,
    onEditProfile: () -> Unit,
    onQuoteClick: (MotivationQuote) -> Unit,
    modifier: Modifier = Modifier
) {
    val todaysQuote = remember(quotes) { quotes.firstOrNull() ?: MotivationQuote(1, "Focus creates reality.", "Anonymous", "Focus", "") }
    val completedRoutinesCount = routines.count { it.isCompletedToday }
    val totalRoutinesCount = routines.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner
        item {
            DreamHeroBanner(
                profile = profile,
                onEditProfileClick = onEditProfile
            )
        }

        // Recharts-inspired Weekly Productivity Summary Data Visualization
        item {
            WeeklyProductivityChart(
                days = if (weeklyDays.isNotEmpty()) weeklyDays else listOf(
                    WeeklyProductivityDay("Mon", 0, "Oct 06", 2, 4, 180),
                    WeeklyProductivityDay("Tue", 1, "Oct 07", 3, 5, 240),
                    WeeklyProductivityDay("Wed", 2, "Oct 08", 1, 4, 150),
                    WeeklyProductivityDay("Thu", 3, "Oct 09", 4, 6, 280),
                    WeeklyProductivityDay("Fri", 4, "Oct 10", 2, 5, 210, isToday = true),
                    WeeklyProductivityDay("Sat", 5, "Oct 11", 3, 3, 160),
                    WeeklyProductivityDay("Sun", 6, "Oct 12", 2, 4, 190)
                )
            )
        }

        // Gemini AI Study Mentor & Problem Solver Banner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_ai_mentor_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.4f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    ElectricIndigo.copy(alpha = 0.12f),
                                    CyberCyan.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(16.dp)
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
                                    color = ElectricIndigo.copy(alpha = 0.25f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = ElectricIndigo,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "GEMINI AI STUDY MENTOR",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberCyan
                                    )
                                    Text(
                                        text = "Problem Solver & Curriculum",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = "3.5 Flash",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricIndigo,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Solve complex academic equations, debug code, or generate a full AI roadmap for ${profile.dreamTitle}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onNavigateToAiChat,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                                modifier = Modifier.weight(1f).testTag("dashboard_ask_gemini_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ask / Solve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            FilledTonalButton(
                                onClick = onNavigateToAiPlanner,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).testTag("dashboard_ai_planner_btn")
                            ) {
                                Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AI Planner", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Live Focus Sprint Quick-Access Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToFocus() }
                    .testTag("quick_focus_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isTimerRunning) CyberCyan.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (isTimerRunning) EmeraldGreen.copy(alpha = 0.2f) else ElectricIndigo.copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isTimerRunning) Icons.Default.PlayArrow else Icons.Default.Timer,
                                    contentDescription = "Timer",
                                    tint = if (isTimerRunning) EmeraldGreen else ElectricIndigo,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = if (isTimerRunning) "FOCUS SPRINT ACTIVE" else "DEEP WORK POMODORO",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isTimerRunning) EmeraldGreen else CyberCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isTimerRunning) {
                                    val mins = timeRemainingSeconds / 60
                                    val secs = timeRemainingSeconds % 60
                                    "%02d:%02d Remaining".format(mins, secs)
                                } else "25m Deep Work Shield",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = onNavigateToFocus,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("open_focus_button")
                    ) {
                        Text(if (isTimerRunning) "View Sprint" else "Start Shield", fontSize = 12.sp)
                    }
                }
            }
        }

        // Daily Routine Progress & Checklist Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Today's Discipline",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "$completedRoutinesCount of $totalRoutinesCount habits accomplished",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TextButton(onClick = onNavigateToRoutines) {
                        Text("View All", color = CyberCyan, fontWeight = FontWeight.Bold)
                    }
                }

                if (routines.isNotEmpty()) {
                    FirestoreRoutineProgressBar(
                        routines = routines,
                        height = 8.dp,
                        showLabel = false
                    )
                }
            }
        }

        // Top Routines
        if (routines.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No routines set yet. Add daily habits to build momentum!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(routines.take(3), key = { it.id }) { routine ->
                RoutineCard(
                    routine = routine,
                    onToggleDone = { onToggleRoutine(routine) },
                    onToggleAlarm = { onToggleAlarm(routine) },
                    onDelete = { /* handled in routines screen */ }
                )
            }
        }

        // Active Goals Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dream Milestones",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                TextButton(onClick = onNavigateToGoals) {
                    Text("Manage", color = CyberCyan, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Top Goals
        if (goals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No goals added yet. Define key milestones for your dream!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(goals.take(2), key = { it.id }) { goal ->
                GoalCard(
                    goal = goal,
                    onProgressChange = { /* handled in goals tab */ },
                    onDelete = { /* handled in goals tab */ }
                )
            }
        }

        // Daily Fuel & Wisdom Card
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onQuoteClick(todaysQuote) }
                    .testTag("quote_of_the_day_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DAILY FUEL FOR ${todaysQuote.category.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AmberGold
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.NotificationsActive,
                            contentDescription = "Send notification",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "\"${todaysQuote.quote}\"",
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "— ${todaysQuote.author}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}
