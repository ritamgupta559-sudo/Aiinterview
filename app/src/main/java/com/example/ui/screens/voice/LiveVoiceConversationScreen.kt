package com.example.ui.screens.voice

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InterviewConfig
import com.example.data.model.InterviewTurn
import com.example.data.model.Speaker
import com.example.data.remote.GeminiLiveVoiceEngine
import com.example.data.remote.LiveVoiceMessage
import com.example.data.remote.LiveVoiceState
import com.example.data.repository.InterviewRepository
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TealPrimary
import kotlinx.coroutines.launch

@Composable
fun LiveVoiceConversationScreen(
    repository: InterviewRepository,
    config: InterviewConfig,
    onBackClick: () -> Unit,
    onCallCompleted: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isPermissionGranted by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isPermissionGranted = granted
        if (!granted) {
            Toast.makeText(context, "Microphone permission is required for live voice calls", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    val liveEngine = remember(config) {
        GeminiLiveVoiceEngine(
            context = context,
            config = config
        )
    }

    val voiceState by liveEngine.voiceState.collectAsState()
    val messages by liveEngine.messages.collectAsState()
    val audioLevel by liveEngine.audioLevel.collectAsState()
    val currentSubtitle by liveEngine.currentSubtitle.collectAsState()

    var textFallbackInput by remember { mutableStateOf("") }
    var isGeneratingReport by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(isPermissionGranted) {
        if (isPermissionGranted) {
            liveEngine.startSession()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            liveEngine.endSession()
        }
    }

    // Glowing pulsating animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (voiceState == LiveVoiceState.LISTENING || voiceState == LiveVoiceState.SPEAKING) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Scaffold(
        containerColor = Color(0xFF0B1120),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (voiceState != LiveVoiceState.DISCONNECTED) Color(0xFF10B981) else Color(0xFFEF4444))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${config.interviewer.name} (${config.interviewer.roleTitle})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "⚡ gemini-3.1-flash-live-preview (Live API)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                            color = TealAccent
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (voiceState) {
                            LiveVoiceState.CONNECTING -> "Connecting..."
                            LiveVoiceState.LISTENING -> "Listening"
                            LiveVoiceState.PROCESSING -> "Thinking..."
                            LiveVoiceState.SPEAKING -> "Speaking"
                            LiveVoiceState.PAUSED -> "Paused"
                            LiveVoiceState.DISCONNECTED -> "Ended"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = when (voiceState) {
                            LiveVoiceState.LISTENING -> Color(0xFF10B981)
                            LiveVoiceState.SPEAKING -> TealAccent
                            LiveVoiceState.PROCESSING -> Color(0xFFF59E0B)
                            else -> Color(0xFF94A3B8)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Interactive Orb / Wave Visualizer
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .scale(pulseScale + (audioLevel * 0.4f))
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    if (voiceState == LiveVoiceState.SPEAKING) TealAccent.copy(alpha = 0.45f)
                                    else if (voiceState == LiveVoiceState.LISTENING) Color(0xFF10B981).copy(alpha = 0.4f)
                                    else Color(0xFF3B82F6).copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Central interactive sphere
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = when (voiceState) {
                                    LiveVoiceState.SPEAKING -> listOf(Color(0xFF00ADB5), Color(0xFF2563EB))
                                    LiveVoiceState.LISTENING -> listOf(Color(0xFF10B981), Color(0xFF059669))
                                    LiveVoiceState.PROCESSING -> listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                                    else -> listOf(Color(0xFF334155), Color(0xFF1E293B))
                                }
                            )
                        )
                        .border(
                            2.dp,
                            if (voiceState == LiveVoiceState.LISTENING) Color(0xFF34D399) else Color(0x66FFFFFF),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (voiceState) {
                            LiveVoiceState.SPEAKING -> Icons.Default.VolumeUp
                            LiveVoiceState.LISTENING -> Icons.Default.Mic
                            LiveVoiceState.PROCESSING -> Icons.Default.GraphicEq
                            else -> Icons.Default.GraphicEq
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when (voiceState) {
                    LiveVoiceState.LISTENING -> "Speaking is detected • Tap 'Done Answering' when finished"
                    LiveVoiceState.SPEAKING -> "${config.interviewer.name} is speaking live"
                    LiveVoiceState.PROCESSING -> "Generating real-time response with Gemini Live..."
                    LiveVoiceState.PAUSED -> "Call is paused"
                    else -> "Live Conversation active"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Live Transcript Conversation Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(messages) { msg ->
                        val isAI = msg.speaker == Speaker.AI
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isAI) Alignment.Start else Alignment.End
                        ) {
                            Text(
                                text = if (isAI) config.interviewer.name else "You",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (isAI) TealAccent else Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isAI) 2.dp else 12.dp,
                                            bottomEnd = if (isAI) 12.dp else 2.dp
                                        )
                                    )
                                    .background(if (isAI) Color(0xFF1E293B) else Color(0xFF00ADB5).copy(alpha = 0.25f))
                                    .border(
                                        1.dp,
                                        if (isAI) Color(0xFF334155) else TealAccent.copy(alpha = 0.5f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Text Input fallback if user prefers typing
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textFallbackInput,
                    onValueChange = { textFallbackInput = it },
                    placeholder = {
                        Text("Or type your answer here...", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (textFallbackInput.isNotBlank()) {
                            val textToSend = textFallbackInput
                            textFallbackInput = ""
                            liveEngine.finishUserSpeechTurn(textToSend)
                        }
                    },
                    enabled = textFallbackInput.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (textFallbackInput.isNotBlank()) TealAccent else Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Call Action Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause / Resume Call
                IconButton(
                    onClick = { liveEngine.togglePause() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF334155), CircleShape)
                ) {
                    Icon(
                        imageVector = if (voiceState == LiveVoiceState.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "Pause/Resume",
                        tint = Color.White
                    )
                }

                // Primary Turn Action: "Done Speaking / Send Voice Turn"
                if (voiceState == LiveVoiceState.LISTENING) {
                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color(0xFF10B981))
                            .clickable {
                                liveEngine.finishUserSpeechTurn()
                            }
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Done Speaking",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                } else if (voiceState == LiveVoiceState.SPEAKING) {
                    // Interrupt button
                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color(0xFF0284C7))
                            .clickable {
                                liveEngine.interruptAndSpeak()
                            }
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Interrupt & Speak",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                } else if (voiceState == LiveVoiceState.PROCESSING) {
                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = TealAccent,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Thinking...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(TealPrimary)
                            .clickable {
                                liveEngine.startListeningTurn()
                            }
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Speak Answer",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }

                // End Voice Call & Generate Report
                IconButton(
                    onClick = {
                        if (!isGeneratingReport) {
                            isGeneratingReport = true
                            val finalTurns = liveEngine.endSession()
                            scope.launch {
                                val report = repository.generateAnalysisReport(config, finalTurns)
                                val sessionId = repository.saveInterviewSession(
                                    config = config,
                                    turns = finalTurns,
                                    report = report,
                                    isCompleted = true
                                )
                                onCallCompleted(sessionId)
                            }
                        }
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDC2626))
                        .testTag("end_voice_call_button")
                ) {
                    if (isGeneratingReport) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
