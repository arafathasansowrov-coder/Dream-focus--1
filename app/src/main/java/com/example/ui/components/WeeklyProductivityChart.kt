package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WeeklyProductivityDay
import com.example.data.model.WeeklyProductivitySummary
import com.example.ui.theme.*

/**
 * Recharts-inspired Data Visualization Component:
 * Displays weekly productivity metrics comparing Goal Milestones achieved vs Routine Tasks completed.
 * Features glowing dual column bars, day axes, interactive tap tooltips, and summary analytics.
 */
@Composable
fun WeeklyProductivityChart(
    days: List<WeeklyProductivityDay>,
    modifier: Modifier = Modifier,
    onDaySelected: (WeeklyProductivityDay) -> Unit = {}
) {
    var selectedDayIndex by remember { mutableStateOf(days.indexOfFirst { it.isToday }.let { if (it >= 0) it else 3 }) }
    var selectedWeekTab by remember { mutableStateOf(0) } // 0 = This Week, 1 = Previous Week

    val currentDays = remember(days, selectedWeekTab) {
        if (selectedWeekTab == 0) {
            days
        } else {
            // Simulated previous week comparison data
            days.mapIndexed { idx, d ->
                d.copy(
                    milestonesAchieved = (d.milestonesAchieved * 0.8f).toInt().coerceAtLeast(1),
                    routinesCompleted = (d.routinesCompleted * 0.9f).toInt().coerceAtLeast(2),
                    focusMinutes = (d.focusMinutes * 0.85f).toInt(),
                    isToday = false
                )
            }
        }
    }

    val selectedDay = currentDays.getOrNull(selectedDayIndex) ?: currentDays.firstOrNull()

    val totalMilestones = remember(currentDays) { currentDays.sumOf { it.milestonesAchieved } }
    val totalRoutines = remember(currentDays) { currentDays.sumOf { it.routinesCompleted } }
    val totalFocusHours = remember(currentDays) { currentDays.sumOf { it.focusMinutes } / 60f }
    val peakDay = remember(currentDays) { currentDays.maxByOrNull { it.milestonesAchieved + it.routinesCompleted }?.dayName ?: "Thu" }

    val maxVal = remember(currentDays) {
        val maxMilestone = currentDays.maxOfOrNull { it.milestonesAchieved } ?: 5
        val maxRoutine = currentDays.maxOfOrNull { it.routinesCompleted } ?: 5
        maxOf(maxMilestone, maxRoutine, 6)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_productivity_recharts_container"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header with Recharts title and Week switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = CyberCyan.copy(alpha = 0.2f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WEEKLY PRODUCTIVITY SUMMARY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Milestones vs Routine Discipline",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Week selector pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedWeekTab == 0) ElectricIndigo.copy(alpha = 0.25f) else Color.Transparent)
                            .clickable { selectedWeekTab = 0 }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "This Week",
                            fontSize = 11.sp,
                            fontWeight = if (selectedWeekTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedWeekTab == 0) ElectricIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedWeekTab == 1) ElectricIndigo.copy(alpha = 0.25f) else Color.Transparent)
                            .clickable { selectedWeekTab = 1 }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Last Week",
                            fontSize = 11.sp,
                            fontWeight = if (selectedWeekTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedWeekTab == 1) ElectricIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Summary Metric Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Milestones",
                    value = "$totalMilestones",
                    color = CyberCyan,
                    icon = Icons.Default.Flag
                )
                MetricSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Routines",
                    value = "$totalRoutines",
                    color = ElectricIndigo,
                    icon = Icons.Default.CheckCircle
                )
                MetricSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Focus Time",
                    value = "%.1fh".format(totalFocusHours),
                    color = EmeraldGreen,
                    icon = Icons.Default.Timer
                )
                MetricSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Peak Day",
                    value = peakDay,
                    color = AmberGold,
                    icon = Icons.Default.AutoAwesome
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Recharts-style Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = CyberCyan, label = "Goal Milestones Achieved")
                Spacer(modifier = Modifier.width(20.dp))
                LegendItem(color = ElectricIndigo, label = "Routine Tasks Completed")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // The Chart Canvas / Column Layout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // Background Grid Lines
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(4) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                            thickness = 1.dp
                        )
                    }
                }

                // Bars Row for 7 days
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    currentDays.forEachIndexed { index, day ->
                        val isSelected = (index == selectedDayIndex)
                        DayBarColumn(
                            day = day,
                            maxVal = maxVal,
                            isSelected = isSelected,
                            onSelect = {
                                selectedDayIndex = index
                                onDaySelected(day)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Tooltip Card for the Selected Day
            selectedDay?.let { day ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chart_day_tooltip_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (day.isToday) ElectricIndigo else CyberCyan.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = day.dayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (day.isToday) Color.White else CyberCyan,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${day.dayName} Details (${day.fullDate})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (day.isToday) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = EmeraldGreen.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "TODAY",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = EmeraldGreen,
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Focus Time: ${day.focusMinutes}m • Efficiency: ${if (day.routinesCompleted > 0) "94%" else "Active"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(CyberCyan)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${day.milestonesAchieved} Goals",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberCyan
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(ElectricIndigo)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${day.routinesCompleted} Routines",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricIndigo
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayBarColumn(
    day: WeeklyProductivityDay,
    maxVal: Int,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val maxBarHeight = 125f // dp
    val milestoneRatio = (day.milestonesAchieved.toFloat() / maxVal).coerceIn(0.08f, 1f)
    val routineRatio = (day.routinesCompleted.toFloat() / maxVal).coerceIn(0.08f, 1f)

    val animatedMilestoneHeight by animateFloatAsState(
        targetValue = milestoneRatio * maxBarHeight,
        animationSpec = tween(durationMillis = 600)
    )
    val animatedRoutineHeight by animateFloatAsState(
        targetValue = routineRatio * maxBarHeight,
        animationSpec = tween(durationMillis = 600)
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onSelect() }
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Dual Bar Columns
        Row(
            modifier = Modifier
                .height(135.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            // Milestone Bar (Cyan)
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .height(animatedMilestoneHeight.dp)
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                CyberCyan,
                                CyberCyan.copy(alpha = 0.6f)
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.width(3.dp))

            // Routine Bar (Indigo)
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .height(animatedRoutineHeight.dp)
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                ElectricIndigo,
                                ElectricIndigo.copy(alpha = 0.6f)
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Day Label / Pill
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isSelected) {
                CyberCyan.copy(alpha = 0.25f)
            } else if (day.isToday) {
                ElectricIndigo.copy(alpha = 0.2f)
            } else {
                Color.Transparent
            }
        ) {
            Text(
                text = day.dayName,
                fontSize = 11.sp,
                fontWeight = if (isSelected || day.isToday) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isSelected) {
                    CyberCyan
                } else if (day.isToday) {
                    ElectricIndigo
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MetricSummaryCard(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
