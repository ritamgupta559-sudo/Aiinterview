package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.data.local.Converters
import com.example.data.local.InterviewDao
import com.example.data.local.InterviewEntity
import com.example.data.local.UserPreferencesManager
import com.example.data.model.InterviewReport
import com.example.data.model.InterviewTurn
import com.example.data.model.UserProfile
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreRepository(context: Context? = null) {
    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (context != null && FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("FirestoreRepository", "Firestore not available / not configured: ${e.message}")
            null
        }
    }
    private val converters = Converters()

    suspend fun saveUserProfile(userId: String, profile: UserProfile): Boolean = withContext(Dispatchers.IO) {
        if (userId.isEmpty() || userId == "local_user") return@withContext false
        val db = firestore ?: return@withContext false
        try {
            val userMap = hashMapOf(
                "id" to userId,
                "name" to profile.name,
                "email" to profile.email,
                "experienceLevel" to profile.experienceLevel,
                "targetRoles" to profile.targetRoles,
                "primaryRole" to profile.primaryRole,
                "coachingLanguage" to profile.coachingLanguage,
                "biggestChallenge" to profile.biggestChallenge,
                "resumeText" to profile.resumeText,
                "currentReadinessScore" to profile.currentReadinessScore,
                "interviewsCompletedCount" to profile.interviewsCompletedCount,
                "practiceStreakDays" to profile.practiceStreakDays,
                "scoreImprovementSinceStart" to profile.scoreImprovementSinceStart,
                "isOnboarded" to profile.isOnboarded,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("users").document(userId)
                .set(userMap, SetOptions.merge())
                .await()
            Log.d("FirestoreRepository", "Saved user profile to Firestore for user: $userId")
            true
        } catch (e: Exception) {
            Log.w("FirestoreRepository", "Error saving user profile to Firestore: ${e.message}")
            false
        }
    }

    suspend fun getUserProfile(userId: String): UserProfile? = withContext(Dispatchers.IO) {
        if (userId.isEmpty() || userId == "local_user") return@withContext null
        val db = firestore ?: return@withContext null
        try {
            val snapshot = db.collection("users").document(userId).get().await()
            if (snapshot.exists()) {
                val data = snapshot.data ?: return@withContext null
                @Suppress("UNCHECKED_CAST")
                val targetRoles = (data["targetRoles"] as? List<String>) ?: listOf(
                    "Digital Marketing Manager", "Sales Executive", "Software Engineer"
                )
                UserProfile(
                    id = userId,
                    name = data["name"] as? String ?: "Candidate",
                    email = data["email"] as? String ?: "",
                    experienceLevel = data["experienceLevel"] as? String ?: "2–5 years",
                    targetRoles = targetRoles,
                    primaryRole = data["primaryRole"] as? String ?: "Digital Marketing Manager",
                    coachingLanguage = data["coachingLanguage"] as? String ?: "English",
                    biggestChallenge = data["biggestChallenge"] as? String ?: "",
                    resumeText = data["resumeText"] as? String ?: "",
                    currentReadinessScore = (data["currentReadinessScore"] as? Long)?.toInt() ?: 75,
                    interviewsCompletedCount = (data["interviewsCompletedCount"] as? Long)?.toInt() ?: 0,
                    practiceStreakDays = (data["practiceStreakDays"] as? Long)?.toInt() ?: 1,
                    scoreImprovementSinceStart = (data["scoreImprovementSinceStart"] as? Long)?.toInt() ?: 0,
                    isGuest = false,
                    isOnboarded = data["isOnboarded"] as? Boolean ?: true
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w("FirestoreRepository", "Error fetching profile from Firestore: ${e.message}")
            null
        }
    }

    suspend fun saveInterviewSession(userId: String, entity: InterviewEntity): Boolean = withContext(Dispatchers.IO) {
        if (userId.isEmpty() || userId == "local_user") return@withContext false
        val db = firestore ?: return@withContext false
        try {
            val turnsJson = converters.fromTurnsList(entity.turns)
            val reportJson = converters.fromReport(entity.report)

            val sessionMap = hashMapOf(
                "id" to entity.id,
                "userId" to userId,
                "timestamp" to entity.timestamp,
                "role" to entity.role,
                "companyName" to entity.companyName,
                "experienceLevel" to entity.experienceLevel,
                "interviewTypeName" to entity.interviewTypeName,
                "difficultyName" to entity.difficultyName,
                "interviewerId" to entity.interviewerId,
                "interviewerName" to entity.interviewerName,
                "durationMinutes" to entity.durationMinutes,
                "overallScore" to entity.overallScore,
                "isCompleted" to entity.isCompleted,
                "turnsJson" to turnsJson,
                "reportJson" to reportJson,
                "updatedAt" to System.currentTimeMillis()
            )

            db.collection("users").document(userId)
                .collection("interviews").document(entity.id)
                .set(sessionMap, SetOptions.merge())
                .await()
            Log.d("FirestoreRepository", "Saved interview ${entity.id} to Firestore")
            true
        } catch (e: Exception) {
            Log.w("FirestoreRepository", "Error saving interview session to Firestore: ${e.message}")
            false
        }
    }

    suspend fun fetchUserInterviews(userId: String): List<InterviewEntity> = withContext(Dispatchers.IO) {
        if (userId.isEmpty() || userId == "local_user") return@withContext emptyList()
        val db = firestore ?: return@withContext emptyList()
        try {
            val querySnapshot = db.collection("users").document(userId)
                .collection("interviews")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                val turnsJson = data["turnsJson"] as? String ?: "[]"
                val reportJson = data["reportJson"] as? String

                InterviewEntity(
                    id = doc.id,
                    timestamp = (data["timestamp"] as? Long) ?: System.currentTimeMillis(),
                    role = data["role"] as? String ?: "Role",
                    companyName = data["companyName"] as? String ?: "",
                    experienceLevel = data["experienceLevel"] as? String ?: "2–5 years",
                    interviewTypeName = data["interviewTypeName"] as? String ?: "HR_TECHNICAL",
                    difficultyName = data["difficultyName"] as? String ?: "PROFESSIONAL",
                    interviewerId = data["interviewerId"] as? String ?: "sarah",
                    interviewerName = data["interviewerName"] as? String ?: "Sarah",
                    durationMinutes = (data["durationMinutes"] as? Long)?.toInt() ?: 15,
                    turns = converters.toTurnsList(turnsJson) ?: emptyList(),
                    report = converters.toReport(reportJson),
                    overallScore = (data["overallScore"] as? Long)?.toInt() ?: 0,
                    isCompleted = data["isCompleted"] as? Boolean ?: true
                )
            }
        } catch (e: Exception) {
            Log.w("FirestoreRepository", "Error fetching interviews from Firestore: ${e.message}")
            emptyList()
        }
    }

    suspend fun syncCloudAndLocal(
        userId: String,
        localDao: InterviewDao,
        preferences: UserPreferencesManager
    ) = withContext(Dispatchers.IO) {
        if (userId.isEmpty() || userId == "local_user" || firestore == null) return@withContext
        try {
            // 1. Check if user profile exists on cloud
            val cloudProfile = getUserProfile(userId)
            if (cloudProfile != null) {
                preferences.saveProfile(cloudProfile)
            } else {
                // Upload local profile to cloud
                saveUserProfile(userId, preferences.userProfile.value)
            }

            // 2. Fetch interviews from cloud and insert into local Room
            val cloudInterviews = fetchUserInterviews(userId)
            for (interview in cloudInterviews) {
                localDao.insertInterview(interview)
            }

            // 3. Upload any local interviews to cloud
            val localInterviews = localDao.getAllInterviewsSync()
            for (interview in localInterviews) {
                saveInterviewSession(userId, interview)
            }
            Log.d("FirestoreRepository", "Sync completed successfully for user: $userId")
        } catch (e: Exception) {
            Log.w("FirestoreRepository", "Error during Firestore sync: ${e.message}")
        }
    }
}
