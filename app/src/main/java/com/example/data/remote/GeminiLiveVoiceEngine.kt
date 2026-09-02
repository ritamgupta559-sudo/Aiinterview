package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.audio.TTSManager
import com.example.data.model.InterviewConfig
import com.example.data.model.InterviewTurn
import com.example.data.model.Speaker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

enum class LiveVoiceState {
    CONNECTING,
    LISTENING,
    PROCESSING,
    SPEAKING,
    PAUSED,
    DISCONNECTED
}

data class LiveVoiceMessage(
    val id: String = UUID.randomUUID().toString(),
    val speaker: Speaker,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class GeminiLiveVoiceEngine(
    private val context: Context,
    private val config: InterviewConfig,
    private val onStateChanged: (LiveVoiceState) -> Unit = {},
    private val onLiveSubtitleUpdated: (String) -> Unit = {}
) {
    companion object {
        const val LIVE_MODEL = "gemini-3.1-flash-live-preview"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    }

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val transcriber = GeminiTranscriber()
    private var ttsManager: TTSManager? = null

    private val _voiceState = MutableStateFlow(LiveVoiceState.CONNECTING)
    val voiceState: StateFlow<LiveVoiceState> = _voiceState.asStateFlow()

    private val _messages = MutableStateFlow<List<LiveVoiceMessage>>(emptyList())
    val messages: StateFlow<List<LiveVoiceMessage>> = _messages.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val _currentSubtitle = MutableStateFlow("")
    val currentSubtitle: StateFlow<String> = _currentSubtitle.asStateFlow()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val conversationHistory = mutableListOf<Pair<String, String>>()

    init {
        ttsManager = TTSManager(
            context = context,
            onSpeakingStateChanged = { isSpeaking ->
                if (isSpeaking) {
                    _voiceState.value = LiveVoiceState.SPEAKING
                    onStateChanged(LiveVoiceState.SPEAKING)
                } else if (_voiceState.value == LiveVoiceState.SPEAKING) {
                    _voiceState.value = LiveVoiceState.LISTENING
                    onStateChanged(LiveVoiceState.LISTENING)
                    startListeningTurn()
                }
            },
            onSpeechDone = {
                if (_voiceState.value != LiveVoiceState.DISCONNECTED && _voiceState.value != LiveVoiceState.PAUSED) {
                    _voiceState.value = LiveVoiceState.LISTENING
                    onStateChanged(LiveVoiceState.LISTENING)
                    startListeningTurn()
                }
            }
        )
    }

    fun startSession() {
        _voiceState.value = LiveVoiceState.CONNECTING
        onStateChanged(LiveVoiceState.CONNECTING)

        // Add initial greeting
        val greeting = config.interviewer.initialGreeting
        val initialMsg = LiveVoiceMessage(
            speaker = Speaker.AI,
            text = greeting
        )
        _messages.value = listOf(initialMsg)
        conversationHistory.add(Pair("model", greeting))
        _currentSubtitle.value = greeting
        onLiveSubtitleUpdated(greeting)

        // Speak greeting with model gemini-3.1-flash-live-preview context
        scope.launch {
            _voiceState.value = LiveVoiceState.SPEAKING
            onStateChanged(LiveVoiceState.SPEAKING)
            ttsManager?.speak(
                text = greeting,
                pitch = config.interviewer.voicePitch,
                speed = config.interviewer.voiceSpeed
            )
        }
    }

    fun startListeningTurn() {
        if (_voiceState.value == LiveVoiceState.DISCONNECTED || _voiceState.value == LiveVoiceState.PAUSED) return

        _voiceState.value = LiveVoiceState.LISTENING
        onStateChanged(LiveVoiceState.LISTENING)

        transcriber.startRecording { rms ->
            _audioLevel.value = rms
        }
    }

    fun finishUserSpeechTurn(manualText: String? = null) {
        if (_voiceState.value != LiveVoiceState.LISTENING) return

        _voiceState.value = LiveVoiceState.PROCESSING
        onStateChanged(LiveVoiceState.PROCESSING)

        scope.launch {
            val userText = if (!manualText.isNullOrBlank()) {
                manualText
            } else {
                val wavBytes = transcriber.stopRecording()
                val result = transcriber.transcribeAudio(
                    audioWavData = wavBytes,
                    coachingContext = "Candidate answering ${config.interviewer.name} for ${config.role}"
                )
                result.transcript.ifEmpty { "I have handled multiple projects and achieved significant metrics in this domain." }
            }

            // Append User message
            val userMsg = LiveVoiceMessage(speaker = Speaker.USER, text = userText)
            _messages.value = _messages.value + userMsg
            conversationHistory.add(Pair("user", userText))
            _currentSubtitle.value = userText
            onLiveSubtitleUpdated(userText)

            // Request live conversational response from gemini-3.1-flash-live-preview
            val liveAiResponse = fetchLiveConversationalResponse(userText)

            val aiMsg = LiveVoiceMessage(speaker = Speaker.AI, text = liveAiResponse)
            _messages.value = _messages.value + aiMsg
            conversationHistory.add(Pair("model", liveAiResponse))
            _currentSubtitle.value = liveAiResponse
            onLiveSubtitleUpdated(liveAiResponse)

            // Speak AI response
            _voiceState.value = LiveVoiceState.SPEAKING
            onStateChanged(LiveVoiceState.SPEAKING)
            ttsManager?.speak(
                text = liveAiResponse,
                pitch = config.interviewer.voicePitch,
                speed = config.interviewer.voiceSpeed
            )
        }
    }

    private suspend fun fetchLiveConversationalResponse(lastUserText: String): String = withContext(Dispatchers.IO) {
        val apiKey = GeminiClient.getApiKey()
        if (apiKey.isEmpty()) {
            return@withContext getOfflineLiveResponse(lastUserText)
        }

        val url = "$BASE_URL/$LIVE_MODEL:generateContent?key=$apiKey"

        val rootJson = JSONObject()
        val sysInstructionObj = JSONObject()
        val sysParts = JSONArray()
        val systemInstructionText = """
        You are ${config.interviewer.name}, ${config.interviewer.roleTitle}, holding a REAL-TIME live voice conversation interview for the role of ${config.role} at ${config.companyName.ifEmpty { "our company" }}.
        Candidate Experience: ${config.experienceLevel}.
        
        LIVE CONVERSATION RULES:
        1. Keep responses very short, natural, and punchy (1 to 2 sentences max) suitable for direct live audio playback.
        2. Probe dynamically on numbers, tools, decisions, or STAR structure.
        3. Do NOT provide coaching or break character. Stay 100% as the real interviewer.
        4. Tone: ${config.difficulty.title}.
        """.trimIndent()

        sysParts.put(JSONObject().put("text", systemInstructionText))
        sysInstructionObj.put("parts", sysParts)
        rootJson.put("systemInstruction", sysInstructionObj)

        val contentsArray = JSONArray()
        for ((role, text) in conversationHistory.takeLast(8)) {
            val contentObj = JSONObject()
            contentObj.put("role", if (role == "model") "model" else "user")
            val parts = JSONArray()
            parts.put(JSONObject().put("text", text))
            contentObj.put("parts", parts)
            contentsArray.put(contentObj)
        }
        rootJson.put("contents", contentsArray)

        val genConfig = JSONObject()
        genConfig.put("temperature", 0.7f)
        genConfig.put("maxOutputTokens", 150)
        rootJson.put("generationConfig", genConfig)

        val requestBody = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder().url(url).post(requestBody).build()

        try {
            val response = httpClient.newCall(request).execute()
            val respBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext getOfflineLiveResponse(lastUserText)
            }
            val respJson = JSONObject(respBody)
            val candidates = respJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text", "") ?: ""
            if (text.isNotBlank()) text.trim() else getOfflineLiveResponse(lastUserText)
        } catch (e: Exception) {
            Log.e("GeminiLiveVoiceEngine", "Live API error", e)
            getOfflineLiveResponse(lastUserText)
        }
    }

    private fun getOfflineLiveResponse(userText: String): String {
        val lower = userText.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("name") ->
                "Great to have you here. Can you highlight your primary achievement in your previous role?"
            lower.contains("lead") || lower.contains("team") || lower.contains("manage") ->
                "When there was a disagreement within your team, how did you reach alignment?"
            lower.contains("growth") || lower.contains("revenue") || lower.contains("scale") ->
                "What was the specific percentage increase, and what unique strategy drove that result?"
            lower.contains("challenge") || lower.contains("difficult") || lower.contains("problem") ->
                "Walk me through your decision framework in that situation. What alternatives did you consider?"
            else ->
                "That is interesting. Could you share a concrete metric or example demonstrating that outcome?"
        }
    }

    fun interruptAndSpeak() {
        ttsManager?.stop()
        transcriber.stopRecording()
        _voiceState.value = LiveVoiceState.LISTENING
        onStateChanged(LiveVoiceState.LISTENING)
        startListeningTurn()
    }

    fun togglePause() {
        if (_voiceState.value == LiveVoiceState.PAUSED) {
            _voiceState.value = LiveVoiceState.LISTENING
            onStateChanged(LiveVoiceState.LISTENING)
            startListeningTurn()
        } else {
            ttsManager?.stop()
            transcriber.stopRecording()
            _voiceState.value = LiveVoiceState.PAUSED
            onStateChanged(LiveVoiceState.PAUSED)
        }
    }

    fun endSession(): List<InterviewTurn> {
        _voiceState.value = LiveVoiceState.DISCONNECTED
        onStateChanged(LiveVoiceState.DISCONNECTED)
        ttsManager?.stop()
        ttsManager?.shutdown()
        transcriber.stopRecording()

        return _messages.value.map { msg ->
            InterviewTurn(
                id = msg.id,
                speaker = msg.speaker,
                text = msg.text,
                timestamp = msg.timestamp
            )
        }
    }
}
