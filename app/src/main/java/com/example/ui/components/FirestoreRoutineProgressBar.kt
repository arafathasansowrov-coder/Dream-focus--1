package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RoutineItem
import com.example.ui.theme.*

/**
 * Core Material 3 Visual Progress Bar component that calculates and displays
 * the completion percentage of daily routines fetched from Firestore.
 *
 * @param routines The list of daily routines (fetched from Firestore).
 * @param modifier Custom modifier.
 * @param height Height of the progress bar track.
 * @param showLabel Whether to display the text percentage label.
 */
@Composable
fun FirestoreRoutineProgressBar(
    routines: List<RoutineItem>,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp,
    showLabel: Boolean = true
) {
    val totalCount = routines.size
    val completedCount = routines.count { it.isCompletedToday }
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val percentage = (progressFraction * 100).toInt()

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "routine_progress_anim"
    )

    val progressColor by animateColorAsState(
        targetValue = when {
            percentage == 100 -> EmeraldGreen
            percentage >= 75 -> CyberCyan
            percentage >= 40 -> ElectricIndigo
            else -> AmberGold
        },
        animationSpec = tween(durationMillis = 400),
        label = "routine_color_anim"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("firestore_routine_progress_bar")
            .semantics {
                contentDescription = "Daily routine progress: $percentage percent complete, $completedCount of $totalCount completed"
            }
    ) {
        if (showLabel) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$completedCount of $totalCount Completed",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = progressColor,
                    modifier = Modifier.testTag("routine_progress_percent_text")
                )
            }
        }

        // Material 3 LinearProgressIndicator with rounded styling
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .testTag("firestore_routine_linear_indicator"),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
    }
}

/**
 * Feature-rich Material 3 Progress Card displaying comprehensive analytics,
 * motivational badges, segmented markers, and live Firestore sync status.
 */
@Composable
fun FirestoreRoutineProgressCard(
    routines: List<RoutineItem>,
    modifier: Modifier = Modifier,
    isCloudSynced: Boolean = true,
    isCloudSyncing: Boolean = false,
    onManualSync: (() -> Unit)? = null
) {
    val totalCount = routines.size
    val completedCount = routines.count { it.isCompletedToday }
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val percentage = (progressFraction * 100).toInt()

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "routine_card_progress_anim"
    )

    val activeColor by animateColorAsState(
        targetValue = when {
            percentage == 100 -> EmeraldGreen
            percentage >= 70 -> CyberCyan
            percentage >= 40 -> ElectricIndigo
            else -> AmberGold
        },
        label = "active_color_anim"
    )

    val (motivationText, motivationIcon) = when {
        percentage == 100 -> Pair("Mastery Achieved! All routines completed", Icons.Default.CheckCircle)
        percentage >= 75 -> Pair("Almost there! Final push for the day", Icons.Default.LocalFireDepartment)
        percentage >= 50 -> Pair("Halfway mark reached! Keep the momentum", Icons.Default.ElectricBolt)
        percentage > 0 -> Pair("Good momentum! Stay committed", Icons.Default.SelfImprovement)
        else -> Pair("Ready to conquer today's schedule", Icons.Default.LocalFireDepartment)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("firestore_routine_progress_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, activeColor.copy(alpha = 0.35f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            activeColor.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "DAILY HABIT DISCIPLINE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan,
                                letterSpacing = 1.2.sp
                            )
                            if (isCloudSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(13.dp),
                                    strokeWidth = 1.5.dp,
                                    color = CyberCyan
                                )
                            } else if (isCloudSynced) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = "Cloud Synced",
                                    tint = CyberCyan.copy(alpha = 0.7f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Text(
                            text = "Firestore Execution Rate",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (onManualSync != null) {
                            IconButton(
                                onClick = onManualSync,
                                modifier = Modifier.size(36.dp).testTag("btn_sync_routines")
                            ) {
                                Icon(
                                    imageVector = if (isCloudSyncing) Icons.Default.CloudSync else Icons.Default.Sync,
                                    contentDescription = "Sync Cloud Routines",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Percentage Badge Pill
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = activeColor.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, activeColor.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "$percentage%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = activeColor,
                                modifier = Modifier
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                    .testTag("routine_card_percentage_badge")
                            )
                        }
                    }
                }

                // Material 3 Linear Progress Bar
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .testTag("firestore_card_linear_progress"),
                    color = activeColor,
                    trackColor = MaterialTheme.colorScheme.surface,
                    strokeCap = StrokeCap.Round
                )

                // Segmented Tick Indicators if 8 or fewer routines
                if (totalCount in 2..8) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in 0 until totalCount) {
                            val isItemDone = i < completedCount
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isItemDone) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            )
                        }
                    }
                }

                // Footer with Motivational Message and Count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = motivationIcon,
                            contentDescription = null,
                            tint = activeColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = motivationText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "$completedCount / $totalCount",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Circular Material 3 progress variant, ideal for compact widgets,
 * headers, or dashboard sidebars.
 */
@Composable
fun FirestoreRoutineCircularProgress(
    routines: List<RoutineItem>,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    strokeWidth: Dp = 6.dp
) {
    val totalCount = routines.size
    val completedCount = routines.count { it.isCompletedToday }
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val percentage = (progressFraction * 100).toInt()

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 600),
        label = "routine_circular_anim"
    )

    val progressColor = if (percentage == 100) EmeraldGreen else ElectricIndigo

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .testTag("firestore_routine_circular_progress")
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            color = progressColor,
            strokeWidth = strokeWidth,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
