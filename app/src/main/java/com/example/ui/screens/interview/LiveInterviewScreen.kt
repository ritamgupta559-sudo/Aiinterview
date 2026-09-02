package com.example.ui.screens.interview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.camera.CameraSelfPreview
import com.example.data.model.InterviewConfig
import com.example.ui.components.InterviewerAvatar
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ScoreExcellent
import com.example.ui.theme.TealAccent

@Composable
fun LiveInterviewScreen(
    viewModel: LiveInterviewViewModel,
    config: InterviewConfig,
    onInterviewCompleted: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEndDialog by remember { mutableStateOf(false) }
    var showTextInput by remember { mutableStateOf(false) }
    var manualText by remember { mutableStateOf("") }

    if (uiState.status == InterviewStatus.COMPLETED && uiState.savedSessionId != null) {
        onInterviewCompleted(uiState.savedSessionId!!)
    }

    if (uiState.status == InterviewStatus.ANALYZING_REPORT) {
        // High-end Analyzing Screen
        AnalyzingInterviewView(interviewerName = config.interviewer.name)
        return
    }

    if (showEndDialog) {
        AlertDialog(
            onDismissRequest = { showEndDialog = false },
            title = {
                Text(
                    text = "End Mock Interview?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to finish the round now? Your performance will be analyzed up to this point.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEndDialog = false
                        viewModel.concludeInterview()
                    }
                ) {
                    Text("End & Get Feedback", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDialog = false }) {
                    Text("Continue Interview")
                }
            },
            containerColor = DarkSurface,
            titleContentColor = Color.White,
            textContentColor = Color(0xFF94A3B8)
        )
    }

    Scaffold(
        containerColor = Color(0xFF0B0F19)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Role & Difficulty Badge
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = config.role,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        Text(
                            text = "${config.interviewType.title} • ${config.difficulty.title}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Timer & End Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val minutes = uiState.timeRemainingSeconds / 60
                        val seconds = uiState.timeRemainingSeconds % 60
                        val timeFormatted = String.format("%02d:%02d", minutes, seconds)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = timeFormatted,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (uiState.timeRemainingSeconds < 120) Color(0xFFEF4444) else Color.White
                            )
                        }

                        IconButton(
                            onClick = { showEndDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                                .testTag("end_interview_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "End Call",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Central Recruiter Avatar & Voice Aura
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    val isAiSpeaking = uiState.status == InterviewStatus.INTERVIEWER_SPEAKING
                    val isCandidateSpeaking = uiState.status == InterviewStatus.USER_SPEAKING || uiState.status == InterviewStatus.LISTENING

                    InterviewerAvatar(
                        interviewer = config.interviewer,
                        size = 110.dp,
                        isSpeaking = isAiSpeaking
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = config.interviewer.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = config.interviewer.roleTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TealAccent
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamic Status Indicator Pill
                    val statusText = when (uiState.status) {
                        InterviewStatus.INITIALIZING -> "Connecting with recruiter..."
                        InterviewStatus.INTERVIEWER_SPEAKING -> "${config.interviewer.name} is speaking..."
                        InterviewStatus.LISTENING -> "Listening to your answer..."
                        InterviewStatus.USER_SPEAKING -> "Candidate speaking..."
                        InterviewStatus.THINKING -> "${config.interviewer.name} is reviewing your answer..."
                        InterviewStatus.ANALYZING_REPORT -> "Generating comprehensive report..."
                        InterviewStatus.COMPLETED -> "Interview complete"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF131B2E))
                            .border(1.dp, Color(0xFF283655), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.status == InterviewStatus.THINKING) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = TealAccent
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = if (isCandidateSpeaking) TealAccent else Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Waveform Equalizer
                    WaveformVisualizer(
                        isAudioActive = isAiSpeaking || isCandidateSpeaking,
                        barCount = 28,
                        activeColor = if (isCandidateSpeaking) TealAccent else Color(config.interviewer.avatarBgColor),
                        amplitude = if (isCandidateSpeaking) uiState.micAmplitude else 0.7f
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live Subtitles / Spoken Transcript Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x99131B2E))
                            .border(1.dp, Color(0x33283655), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        val displayText = when {
                            uiState.currentUserPartialText.isNotBlank() -> "\"${uiState.currentUserPartialText}\""
                            uiState.currentAiSpokenText.isNotBlank() -> "\"${uiState.currentAiSpokenText}\""
                            else -> "\"Let's begin.\""
                        }
                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 22.sp,
                                textAlign = TextAlign.Center
                            ),
                            color = if (uiState.currentUserPartialText.isNotBlank()) TealAccent else Color(0xFFE2E8F0),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Floating Self Video Preview (Candidate PiP Tile)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (uiState.isCameraEnabled) {
                        Box(
                            modifier = Modifier
                                .size(width = 110.dp, height = 80.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
                        ) {
                            CameraSelfPreview(
                                modifier = Modifier.fillMaxSize(),
                                isCameraEnabled = true
                            )
                        }
                    }
                }

                // Text Input Fallback Bar (if open)
                AnimatedVisibility(visible = showTextInput) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualText,
                            onValueChange = { manualText = it },
                            placeholder = { Text("Type your answer if in quiet room...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_answer_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TealAccent,
                                unfocusedBorderColor = DarkBorder,
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (manualText.isNotBlank()) {
                                    viewModel.sendManualTextInput(manualText)
                                    manualText = ""
                                    showTextInput = false
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(TealAccent)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = Color(0xFF0F172A)
                            )
                        }
                    }
                }

                // Bottom Call Control Dock
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF131B2E))
                        .border(1.dp, Color(0xFF283655), RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Camera Toggle
                    IconButton(
                        onClick = { viewModel.toggleCamera() },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isCameraEnabled) Color(0xFF1E293B) else Color(0xFF334155))
                    ) {
                        Icon(
                            imageVector = if (uiState.isCameraEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                            contentDescription = "Toggle Camera",
                            tint = Color.White
                        )
                    }

                    // Main Mic Button
                    val isListening = uiState.status == InterviewStatus.LISTENING || uiState.status == InterviewStatus.USER_SPEAKING
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                if (uiState.isMicMuted) Color(0xFF334155)
                                else if (isListening) TealAccent
                                else Color(0xFF1E293B)
                            )
                            .border(
                                2.dp,
                                if (isListening) Color.White else Color(0xFF334155),
                                CircleShape
                            )
                            .clickable { viewModel.toggleMic() }
                            .testTag("mic_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (uiState.isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mic",
                            tint = if (isListening) Color(0xFF0F172A) else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Speaker Toggle
                    IconButton(
                        onClick = { viewModel.toggleSpeaker() },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isSpeakerMuted) Color(0xFF334155) else Color(0xFF1E293B))
                    ) {
                        Icon(
                            imageVector = if (uiState.isSpeakerMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Toggle Speaker",
                            tint = Color.White
                        )
                    }

                    // Keyboard Fallback Toggle
                    IconButton(
                        onClick = { showTextInput = !showTextInput },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (showTextInput) TealAccent else Color(0xFF1E293B))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "Type Answer",
                            tint = if (showTextInput) Color(0xFF0F172A) else Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyzingInterviewView(interviewerName: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = TealAccent,
                strokeWidth = 5.dp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Analyzing Interview Transcript",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Evaluating communication clarity, answer structure (STAR), filler words, and technical substance...",
                style = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                ),
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF131B2E))
                    .border(1.dp, Color(0xFF283655), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Formulating feedback with recruiter $interviewerName",
                    style = MaterialTheme.typography.labelSmall,
                    color = TealAccent
                )
            }
        }
    }
}
