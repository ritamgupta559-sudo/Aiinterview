package com.example.ui.screens.interview

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SpeechRecognizerManager
import com.example.audio.TTSManager
import com.example.data.model.InterviewConfig
import com.example.data.model.InterviewReport
import com.example.data.model.InterviewTurn
import com.example.data.model.Speaker
import com.example.data.repository.InterviewRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class InterviewStatus {
    INITIALIZING,
    INTERVIEWER_SPEAKING,
    LISTENING,
    USER_SPEAKING,
    THINKING,
    ANALYZING_REPORT,
    COMPLETED
}

data class LiveInterviewUiState(
    val status: InterviewStatus = InterviewStatus.INITIALIZING,
    val turns: List<InterviewTurn> = emptyList(),
    val currentAiSpokenText: String = "",
    val currentUserPartialText: String = "",
    val timeRemainingSeconds: Int = 15 * 60,
    val isMicMuted: Boolean = false,
    val isSpeakerMuted: Boolean = false,
    val isCameraEnabled: Boolean = true,
    val micAmplitude: Float = 0.2f,
    val errorMessage: String? = null,
    val generatedReport: InterviewReport? = null,
    val savedSessionId: String? = null
)

class LiveInterviewViewModel(
    private val repository: InterviewRepository,
    private val config: InterviewConfig,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LiveInterviewUiState(timeRemainingSeconds = config.durationMinutes * 60)
    )
    val uiState: StateFlow<LiveInterviewUiState> = _uiState.asStateFlow()

    private var speechRecognizerManager: SpeechRecognizerManager? = null
    private var ttsManager: TTSManager? = null
    private var timerJob: Job? = null

    init {
        initAudioEngines()
        startTimer()
        startInterview()
    }

    private fun initAudioEngines() {
        ttsManager = TTSManager(
            context = context,
            onSpeakingStateChanged = { isSpeaking ->
                if (isSpeaking) {
                    _uiState.value = _uiState.value.copy(status = InterviewStatus.INTERVIEWER_SPEAKING)
                }
            },
            onSpeechDone = {
                // Once AI recruiter finishes speaking, automatically open candidate mic
                startListeningForCandidate()
            }
        )

        speechRecognizerManager = SpeechRecognizerManager(
            context = context,
            onResult = { recognizedText ->
                if (recognizedText.isNotBlank()) {
                    onCandidateSpoke(recognizedText)
                }
            },
            onPartialResult = { partial ->
                _uiState.value = _uiState.value.copy(
                    currentUserPartialText = partial,
                    status = InterviewStatus.USER_SPEAKING
                )
            },
            onRmsChanged = { amp ->
                _uiState.value = _uiState.value.copy(micAmplitude = amp)
            },
            onError = { err ->
                // Handle speech timeout or silent pause gracefully
                if (_uiState.value.status == InterviewStatus.LISTENING || _uiState.value.status == InterviewStatus.USER_SPEAKING) {
                    val partial = _uiState.value.currentUserPartialText
                    if (partial.isNotBlank()) {
                        onCandidateSpoke(partial)
                    }
                }
            }
        )
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeRemainingSeconds > 0) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    timeRemainingSeconds = _uiState.value.timeRemainingSeconds - 1
                )
            }
            // Auto wrap up on time expiration
            concludeInterview()
        }
    }

    private fun startInterview() {
        val openingGreeting = config.interviewer.initialGreeting
        val initialTurn = InterviewTurn(
            speaker = Speaker.AI,
            text = openingGreeting
        )
        _uiState.value = _uiState.value.copy(
            turns = listOf(initialTurn),
            currentAiSpokenText = openingGreeting,
            status = InterviewStatus.INTERVIEWER_SPEAKING
        )
        speakInterviewer(openingGreeting)
    }

    private fun speakInterviewer(text: String) {
        if (!_uiState.value.isSpeakerMuted) {
            ttsManager?.speak(
                text = text,
                pitch = config.interviewer.voicePitch,
                speed = config.interviewer.voiceSpeed
            )
        } else {
            // If speaker is muted, simulate short read delay then open mic
            viewModelScope.launch {
                delay(3000)
                startListeningForCandidate()
            }
        }
    }

    fun startListeningForCandidate() {
        if (_uiState.value.isMicMuted) return
        _uiState.value = _uiState.value.copy(
            status = InterviewStatus.LISTENING,
            currentUserPartialText = ""
        )
        speechRecognizerManager?.startListening()
    }

    fun onCandidateSpoke(text: String) {
        speechRecognizerManager?.stopListening()
        val userTurn = InterviewTurn(
            speaker = Speaker.USER,
            text = text
        )
        val updatedTurns = _uiState.value.turns + userTurn
        _uiState.value = _uiState.value.copy(
            turns = updatedTurns,
            currentUserPartialText = "",
            status = InterviewStatus.THINKING
        )

        viewModelScope.launch {
            try {
                val nextAiResponse = repository.getNextAiResponse(config, updatedTurns)
                val aiTurn = InterviewTurn(
                    speaker = Speaker.AI,
                    text = nextAiResponse
                )
                val newTurns = updatedTurns + aiTurn
                _uiState.value = _uiState.value.copy(
                    turns = newTurns,
                    currentAiSpokenText = nextAiResponse,
                    status = InterviewStatus.INTERVIEWER_SPEAKING
                )
                speakInterviewer(nextAiResponse)
            } catch (e: Exception) {
                val fallback = "Thank you. Let's move on to discuss how you handle tight project deadlines."
                val aiTurn = InterviewTurn(speaker = Speaker.AI, text = fallback)
                val newTurns = updatedTurns + aiTurn
                _uiState.value = _uiState.value.copy(
                    turns = newTurns,
                    currentAiSpokenText = fallback,
                    status = InterviewStatus.INTERVIEWER_SPEAKING
                )
                speakInterviewer(fallback)
            }
        }
    }

    fun toggleMic() {
        val newMute = !_uiState.value.isMicMuted
        _uiState.value = _uiState.value.copy(isMicMuted = newMute)
        if (newMute) {
            speechRecognizerManager?.stopListening()
        } else if (_uiState.value.status == InterviewStatus.LISTENING) {
            speechRecognizerManager?.startListening()
        }
    }

    fun toggleCamera() {
        _uiState.value = _uiState.value.copy(isCameraEnabled = !_uiState.value.isCameraEnabled)
    }

    fun toggleSpeaker() {
        val newMute = !_uiState.value.isSpeakerMuted
        _uiState.value = _uiState.value.copy(isSpeakerMuted = newMute)
        if (newMute) {
            ttsManager?.stop()
        }
    }

    fun sendManualTextInput(text: String) {
        if (text.isNotBlank()) {
            onCandidateSpoke(text)
        }
    }

    fun concludeInterview() {
        timerJob?.cancel()
        speechRecognizerManager?.stopListening()
        ttsManager?.stop()

        _uiState.value = _uiState.value.copy(status = InterviewStatus.ANALYZING_REPORT)

        viewModelScope.launch {
            val report = repository.generateAnalysisReport(config, _uiState.value.turns)
            val sessionId = repository.saveInterviewSession(
                config = config,
                turns = _uiState.value.turns,
                report = report,
                isCompleted = true
            )
            _uiState.value = _uiState.value.copy(
                status = InterviewStatus.COMPLETED,
                generatedReport = report,
                savedSessionId = sessionId
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizerManager?.destroy()
        ttsManager?.shutdown()
        timerJob?.cancel()
    }
}
