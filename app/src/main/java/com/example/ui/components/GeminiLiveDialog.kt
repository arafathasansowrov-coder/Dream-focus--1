package com.example.ui.components

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.api.GeminiClient
import com.example.data.model.StudentProfile
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.sin

/**
 * Gemini Live Conversation Mode:
 * Real-time hands-free voice coach for students.
 * Features animated pulsating cyber audio waves, native TextToSpeech voice synthesis,
 * live transcripts, interruption controls, and rapid study prompts.
 */
@Composable
fun GeminiLiveDialog(
    profile: StudentProfile,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isListening by remember { mutableStateOf(false) }
    var isGeminiSpeaking by remember { mutableStateOf(false) }
    var currentSpeechText by remember { mutableStateOf("Hello! I am your live Gemini study mentor. Tap a topic or start speaking to dive in.") }
    var userSpokenQuery by remember { mutableStateOf("") }
    var conversationHistory by remember {
        mutableStateOf(
            listOf(
                "Gemini" to "Ready to elevate your focus for ${profile.dreamTitle}? Ask me anything, or tap a study booster below!"
            )
        )
    }

    // Android TextToSpeech engine
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
            }
        }
        tts.language = Locale.US
        tts.setPitch(1.05f)
        tts.setSpeechRate(1.05f)
        ttsEngine = tts

        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun speakAloud(text: String) {
        ttsEngine?.let { tts ->
            tts.stop()
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "gemini_live_utterance")
            isGeminiSpeaking = true
        }
    }

    fun stopSpeaking() {
        ttsEngine?.stop()
        isGeminiSpeaking = false
    }

    fun sendLiveQuery(prompt: String) {
        userSpokenQuery = prompt
        conversationHistory = conversationHistory + ("You" to prompt)
        stopSpeaking()
        isListening = false

        coroutineScope.launch {
            val livePrompt = "You are Gemini Live conversational voice study mentor for a student pursuing: ${profile.dreamTitle}. Respond conversationally, succinctly, and warmly in 2-3 spoken sentences. Prompt: $prompt"
            val result = GeminiClient.sendChatMessage(
                history = emptyList(),
                newPrompt = livePrompt,
                profile = profile
            )
            result.onSuccess { response ->
                currentSpeechText = response
                conversationHistory = conversationHistory + ("Gemini" to response)
                speakAloud(response)
            }.onFailure {
                val fallback = "I'm with you! Focus is your superpower right now. Take a deep breath and conquer the next task."
                currentSpeechText = fallback
                conversationHistory = conversationHistory + ("Gemini" to fallback)
                speakAloud(fallback)
            }
        }
    }

    // Pulsing audio animation
    val infiniteTransition = rememberInfiniteTransition(label = "audio_waves")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Dialog(
        onDismissRequest = {
            stopSpeaking()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .testTag("gemini_live_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (isGeminiSpeaking) NeonAmber.copy(alpha = 0.2f) else ElectricIndigo.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = if (isGeminiSpeaking) NeonAmber else ElectricIndigo,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Gemini Live Voice",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isGeminiSpeaking) EmeraldGreen.copy(alpha = 0.2f) else CyberCyan.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = if (isGeminiSpeaking) "SPEAKING" else if (isListening) "LISTENING" else "LIVE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isGeminiSpeaking) EmeraldGreen else CyberCyan,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Hands-free study coach for ${profile.dreamTitle}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            stopSpeaking()
                            onDismiss()
                        },
                        modifier = Modifier.testTag("close_gemini_live_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Gemini Live",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Animated Holographic Waveform Visualizer
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    ElectricIndigo.copy(alpha = 0.25f),
                                    CyberCyan.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = if (isGeminiSpeaking) listOf(NeonAmber, ElectricIndigo) else listOf(CyberCyan, ElectricIndigo)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val midY = height / 2f
                        val waveColor = if (isGeminiSpeaking) NeonAmber else CyberCyan

                        // Draw flowing sine frequency lines
                        val points = 60
                        for (i in 0 until points) {
                            val x = (width / points) * i
                            val amplitude = if (isGeminiSpeaking) 36f else if (isListening) 24f else 10f
                            val y = midY + sin(wavePhase + (i * 0.2f)) * amplitude

                            drawCircle(
                                color = waveColor.copy(alpha = 0.85f),
                                radius = 2.5f,
                                center = Offset(x, y)
                            )
                        }
                    }

                    // Center Microphone or Audio Indicator
                    Surface(
                        shape = CircleShape,
                        color = if (isGeminiSpeaking) NeonAmber else ElectricIndigo,
                        modifier = Modifier.size((64 * pulseScale).dp),
                        tonalElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isGeminiSpeaking) Icons.Default.VolumeUp else Icons.Default.Mic,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Live Spoken Utterance Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(conversationHistory) { (speaker, text) ->
                            val isMe = speaker == "You"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isMe) ElectricIndigo else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.widthIn(max = 280.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = speaker,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isMe) Color.White.copy(alpha = 0.8f) else CyberCyan
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Voice Study Boosters
                Text(
                    text = "Quick Voice Queries",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val topics = listOf(
                        "🧠 Calculus Quiz" to "Quiz me on Calculus fundamentals with one quick question",
                        "⚡ 2-Min Boost" to "Give me a high-intensity 2-minute motivational briefing for my study session",
                        "🎯 What Next?" to "What high-priority task should I focus on right now?",
                        "💡 Explain AI" to "Explain neural network backpropagation in simple terms"
                    )
                    topics.take(2).forEach { (label, prompt) ->
                        OutlinedButton(
                            onClick = { sendLiveQuery(prompt) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val topicsRow2 = listOf(
                        "🎯 What Next?" to "What high-priority task should I focus on right now?",
                        "💡 Explain AI" to "Explain neural network backpropagation in simple terms"
                    )
                    topicsRow2.forEach { (label, prompt) ->
                        OutlinedButton(
                            onClick = { sendLiveQuery(prompt) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live Control Actions Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Interrupt / Stop Voice Button
                    if (isGeminiSpeaking) {
                        Button(
                            onClick = { stopSpeaking() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("gemini_live_interrupt_button")
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tap to Interrupt")
                        }
                    } else {
                        Button(
                            onClick = {
                                if (isListening) {
                                    isListening = false
                                } else {
                                    isListening = true
                                    sendLiveQuery("I'm ready for study advice. Guide my next steps.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("gemini_live_mic_toggle_button")
                        ) {
                            Icon(imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isListening) "Listening..." else "Speak with Gemini")
                        }
                    }
                }
            }
        }
    }
}
