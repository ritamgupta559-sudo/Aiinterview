package com.example.ui.screens.practice

import android.content.Context
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SpeechRecognizerManager
import com.example.data.model.QuestionReview
import com.example.data.remote.GeminiClient
import com.example.ui.components.LinearCard
import com.example.ui.components.PrimaryActionButton
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ScoreExcellent
import com.example.ui.theme.TealAccent
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrillRehearsalScreen(
    question: String,
    category: String,
    framework: String,
    tips: List<String>,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var candidateAnswer by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var micAmplitude by remember { mutableFloatStateOf(0.2f) }
    var speechRecognizer: SpeechRecognizerManager? by remember { mutableStateOf(null) }

    var isEvaluating by remember { mutableStateOf(false) }
    var evaluationResult: QuestionReview? by remember { mutableStateOf(null) }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.destroy()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Practice Drill",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category & Question Card
            item {
                LinearCard(
                    backgroundColor = Color(0xFF111A2E),
                    borderColor = Color(0xFF233554)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(TealAccent.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = category.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = TealAccent
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "\"$question\"",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        if (framework.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Framework: $framework",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            // Pro Tips Card
            if (tips.isNotEmpty()) {
                item {
                    LinearCard(
                        backgroundColor = DarkSurface,
                        borderColor = DarkBorder
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Winning Strategy Tips",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFF59E0B)
                                )
                            }
                            tips.forEach { tip ->
                                Text(
                                    text = "• $tip",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Voice Recording / Answer Box
            item {
                Text(
                    text = "Your Answer",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = candidateAnswer,
                    onValueChange = { candidateAnswer = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("drill_answer_field"),
                    placeholder = {
                        Text("Tap the microphone to speak, or type your answer here...")
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    )
                )
            }

            // Mic Record & Equalizer Bar
            item {
                LinearCard(
                    backgroundColor = DarkSurface,
                    borderColor = DarkBorder
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        WaveformVisualizer(
                            isAudioActive = isListening,
                            barCount = 24,
                            activeColor = TealAccent,
                            amplitude = micAmplitude
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(if (isListening) TealAccent else Color(0xFF1E293B))
                                    .clickable {
                                        if (isListening) {
                                            speechRecognizer?.stopListening()
                                            isListening = false
                                        } else {
                                            speechRecognizer = SpeechRecognizerManager(
                                                context = context,
                                                onResult = { recognized ->
                                                    candidateAnswer = if (candidateAnswer.isBlank()) recognized else "$candidateAnswer $recognized"
                                                    isListening = false
                                                },
                                                onPartialResult = { partial ->
                                                    // Stream live
                                                },
                                                onRmsChanged = { amp -> micAmplitude = amp },
                                                onError = { isListening = false }
                                            )
                                            speechRecognizer?.startListening()
                                            isListening = true
                                        }
                                    }
                                    .testTag("drill_mic_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "Record",
                                    tint = if (isListening) Color(0xFF0F172A) else Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isListening) "Listening... Tap to stop" else "Tap mic to speak your response",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Submit Evaluation CTA
            item {
                PrimaryActionButton(
                    text = if (isEvaluating) "Evaluating Answer..." else "Evaluate My Answer with AI",
                    onClick = {
                        if (candidateAnswer.isNotBlank()) {
                            isEvaluating = true
                            coroutineScope.launch {
                                val evalPrompt = """
                                Evaluate this candidate's answer for the interview question:
                                QUESTION: "$question"
                                CANDIDATE ANSWER: "$candidateAnswer"
                                
                                Return pure JSON:
                                {
                                  "score": integer (50-95),
                                  "positiveFeedback": "string",
                                  "actionableFix": "string",
                                  "idealSampleAnswer": "string"
                                }
                                """.trimIndent()

                                val jsonResp = GeminiClient.generateResponse(
                                    systemInstruction = "You are an expert interview coach.",
                                    messages = listOf(Pair("user", evalPrompt)),
                                    temperature = 0.3f,
                                    jsonMode = true
                                )
                                try {
                                    val obj = JSONObject(jsonResp)
                                    evaluationResult = QuestionReview(
                                        question = question,
                                        candidateAnswer = candidateAnswer,
                                        score = obj.optInt("score", 78),
                                        positiveFeedback = obj.optString("positiveFeedback", "Good direct answer."),
                                        actionableFix = obj.optString("actionableFix", "Add a quantifiable metric."),
                                        idealSampleAnswer = obj.optString("idealSampleAnswer", "In my last role...")
                                    )
                                } catch (e: Exception) {
                                    evaluationResult = QuestionReview(
                                        question = question,
                                        candidateAnswer = candidateAnswer,
                                        score = 80,
                                        positiveFeedback = "Clear delivery and relevant points.",
                                        actionableFix = "Structure your conclusion with a clear forward-looking statement.",
                                        idealSampleAnswer = "I focus on delivering measurable impact through structured execution and proactive stakeholder communication."
                                    )
                                }
                                isEvaluating = false
                            }
                        }
                    },
                    enabled = candidateAnswer.isNotBlank() && !isEvaluating,
                    isLoading = isEvaluating,
                    testTag = "evaluate_drill_button"
                )
            }

            // Evaluation Feedback Reveal Card
            if (evaluationResult != null) {
                val eval = evaluationResult!!
                item {
                    LinearCard(
                        backgroundColor = Color(0xFF131B2E),
                        borderColor = TealAccent,
                        cornerRadius = 16.dp
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AI Coach Evaluation",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ScoreExcellent.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${eval.score}/100",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = ScoreExcellent
                                    )
                                }
                            }

                            if (eval.positiveFeedback.isNotBlank()) {
                                Text(
                                    text = "✓ ${eval.positiveFeedback}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ScoreExcellent
                                )
                            }

                            if (eval.actionableFix.isNotBlank()) {
                                Text(
                                    text = "→ Fix: ${eval.actionableFix}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFF59E0B)
                                )
                            }

                            if (eval.idealSampleAnswer.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0E1524))
                                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                    ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = TealAccent,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Model Response:",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = TealAccent
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "\"${eval.idealSampleAnswer}\"",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
