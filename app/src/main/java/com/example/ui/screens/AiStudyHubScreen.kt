package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.GeneratedStudyPlan
import com.example.data.model.StudentProfile
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.EmeraldGreen

enum class AiHubTab(val title: String) {
    CHAT("Gemini Solver & Chat"),
    PLANNER("AI Study Planner")
}

@Composable
fun AiStudyHubScreen(
    profile: StudentProfile,
    chatMessages: List<ChatMessage>,
    isGeneratingAnswer: Boolean,
    generatedPlan: GeneratedStudyPlan?,
    isGeneratingPlan: Boolean,
    feedbackMessage: String?,
    onSendMessage: (String) -> Unit,
    onSolveProblem: (problem: String, subject: String) -> Unit,
    onClearChat: () -> Unit,
    onGeneratePlan: (subject: String, timeframe: String, dailyHours: Float, level: String) -> Unit,
    onImportPlanToGoals: (GeneratedStudyPlan) -> Unit,
    onImportPlanToRoutines: (GeneratedStudyPlan) -> Unit,
    onDismissFeedback: () -> Unit,
    activeModel: com.example.data.model.AiModelType = com.example.data.model.AiModelType.GEMINI,
    onSelectModel: (com.example.data.model.AiModelType) -> Unit = {},
    initialTab: AiHubTab = AiHubTab.CHAT,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            onDismissFeedback()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("ai_study_hub_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    // Segmented Tabs
                    TabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                                color = ElectricIndigo,
                                height = 3.dp
                            )
                        }
                    ) {
                        AiHubTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            Tab(
                                selected = isSelected,
                                onClick = { selectedTab = tab },
                                text = {
                                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (tab == AiHubTab.CHAT) Icons.Default.ChatBubble else Icons.Default.AutoAwesomeMotion,
                                            contentDescription = null,
                                            tint = if (isSelected) ElectricIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = tab.title,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp,
                                            color = if (isSelected) ElectricIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                modifier = Modifier.testTag("ai_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AiHubTab.CHAT -> {
                    AiChatSection(
                        messages = chatMessages,
                        isLoading = isGeneratingAnswer,
                        profile = profile,
                        onSendMessage = onSendMessage,
                        onSolveProblem = onSolveProblem,
                        onClearChat = onClearChat,
                        onOpenPlanner = { selectedTab = AiHubTab.PLANNER },
                        activeModel = activeModel,
                        onSelectModel = onSelectModel
                    )
                }
                AiHubTab.PLANNER -> {
                    AiPlannerSection(
                        studyPlan = generatedPlan,
                        isLoading = isGeneratingPlan,
                        profile = profile,
                        onGeneratePlan = onGeneratePlan,
                        onImportToGoals = onImportPlanToGoals,
                        onImportToRoutines = onImportPlanToRoutines,
                        onAskInChat = { question ->
                            onSendMessage(question)
                            selectedTab = AiHubTab.CHAT
                        }
                    )
                }
            }
        }
    }
}
