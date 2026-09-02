package com.example.data.repository

import com.example.data.auth.FirebaseAuthManager
import com.example.data.local.InterviewDao
import com.example.data.local.InterviewEntity
import com.example.data.local.UserPreferencesManager
import com.example.data.model.InterviewConfig
import com.example.data.model.InterviewReport
import com.example.data.model.InterviewTurn
import com.example.data.model.ResumeData
import com.example.data.model.UserProfile
import com.example.data.remote.FirestoreRepository
import com.example.data.remote.GeminiTranscriber
import com.example.data.remote.InterviewAiEngine
import com.example.data.remote.TranscriptionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class InterviewRepository(
    private val interviewDao: InterviewDao,
    private val preferencesManager: UserPreferencesManager,
    private val aiEngine: InterviewAiEngine = InterviewAiEngine(),
    val authManager: FirebaseAuthManager = FirebaseAuthManager(),
    val firestoreRepository: FirestoreRepository = FirestoreRepository(),
    val transcriber: GeminiTranscriber = GeminiTranscriber()
) {
    private val repoScope = CoroutineScope(Dispatchers.IO)

    val allInterviews: Flow<List<InterviewEntity>> = interviewDao.getAllInterviews()
    val latestCompletedInterview: Flow<InterviewEntity?> = interviewDao.getLatestCompletedInterview()
    val userProfile: StateFlow<UserProfile> = preferencesManager.userProfile
    val averageScore: Flow<Double?> = interviewDao.getAverageScore()
    val completedCount: Flow<Int> = interviewDao.getCompletedCount()

    init {
        // Automatically sync with cloud if user is logged in
        repoScope.launch {
            authManager.currentUser.collect { user ->
                if (user != null) {
                    firestoreRepository.syncCloudAndLocal(
                        userId = user.uid,
                        localDao = interviewDao,
                        preferences = preferencesManager
                    )
                }
            }
        }
    }

    suspend fun saveInterviewSession(
        id: String = UUID.randomUUID().toString(),
        config: InterviewConfig,
        turns: List<InterviewTurn>,
        report: InterviewReport?,
        isCompleted: Boolean
    ): String {
        val entity = InterviewEntity(
            id = id,
            timestamp = System.currentTimeMillis(),
            role = config.role,
            companyName = config.companyName,
            experienceLevel = config.experienceLevel,
            interviewTypeName = config.interviewType.name,
            difficultyName = config.difficulty.name,
            interviewerId = config.interviewer.id,
            interviewerName = config.interviewer.name,
            durationMinutes = config.durationMinutes,
            turns = turns,
            report = report,
            overallScore = report?.overallScore ?: 0,
            isCompleted = isCompleted
        )
        // 1. Save to Room database locally
        interviewDao.insertInterview(entity)

        if (isCompleted && report != null) {
            preferencesManager.updateReadinessScore(report.overallScore)
        }

        // 2. Persist to Firestore if user is authenticated
        val currentUserId = authManager.getCurrentUserId()
        if (currentUserId.isNotEmpty() && currentUserId != "local_user") {
            firestoreRepository.saveInterviewSession(currentUserId, entity)
            firestoreRepository.saveUserProfile(currentUserId, userProfile.value)
        }

        return id
    }

    suspend fun getInterviewById(id: String): InterviewEntity? {
        return interviewDao.getInterviewById(id)
    }

    suspend fun getNextAiResponse(config: InterviewConfig, turns: List<InterviewTurn>): String {
        return aiEngine.getNextInterviewerResponse(config, turns)
    }

    suspend fun generateAnalysisReport(
        config: InterviewConfig,
        turns: List<InterviewTurn>,
        coachingLanguage: String = userProfile.value.coachingLanguage
    ): InterviewReport {
        return aiEngine.generatePostInterviewReport(config, turns, coachingLanguage)
    }

    suspend fun parseResume(rawText: String): ResumeData {
        return aiEngine.parseResumeText(rawText)
    }

    suspend fun transcribeSpeech(audioData: ByteArray, context: String = ""): TranscriptionResult {
        return transcriber.transcribeAudio(audioData, context)
    }

    fun updateUserProfile(profile: UserProfile) {
        preferencesManager.saveProfile(profile)
        val currentUserId = authManager.getCurrentUserId()
        if (currentUserId.isNotEmpty() && currentUserId != "local_user") {
            repoScope.launch {
                firestoreRepository.saveUserProfile(currentUserId, profile)
            }
        }
    }

    fun updateCoachingLanguage(lang: String) {
        preferencesManager.updateCoachingLanguage(lang)
    }

    fun setOnboarded(completed: Boolean) {
        preferencesManager.setOnboarded(completed)
    }

    suspend fun triggerCloudSync() {
        val currentUserId = authManager.getCurrentUserId()
        if (currentUserId.isNotEmpty() && currentUserId != "local_user") {
            firestoreRepository.syncCloudAndLocal(currentUserId, interviewDao, preferencesManager)
        }
    }
}
