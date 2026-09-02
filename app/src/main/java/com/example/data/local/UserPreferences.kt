package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("interview_ai_prefs", Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private fun loadProfile(): UserProfile {
        return UserProfile(
            id = prefs.getString("user_id", "usr_ritam") ?: "usr_ritam",
            name = prefs.getString("user_name", "Ritam Gupta") ?: "Ritam Gupta",
            email = prefs.getString("user_email", "ritamgupta.559@gmail.com") ?: "ritamgupta.559@gmail.com",
            experienceLevel = prefs.getString("exp_level", "2–5 years") ?: "2–5 years",
            targetRoles = prefs.getStringSet("target_roles", setOf("Digital Marketing Manager", "Sales Executive", "Software Engineer"))?.toList()
                ?: listOf("Digital Marketing Manager", "Sales Executive", "Software Engineer"),
            primaryRole = prefs.getString("primary_role", "Digital Marketing Manager") ?: "Digital Marketing Manager",
            coachingLanguage = prefs.getString("coaching_lang", "English") ?: "English",
            biggestChallenge = prefs.getString("biggest_challenge", "I struggle with structuring answers under pressure")
                ?: "I struggle with structuring answers under pressure",
            resumeText = prefs.getString("resume_text", "") ?: "",
            currentReadinessScore = prefs.getInt("readiness_score", 74),
            interviewsCompletedCount = prefs.getInt("completed_count", 5),
            practiceStreakDays = prefs.getInt("streak_days", 4),
            scoreImprovementSinceStart = prefs.getInt("score_improvement", 9),
            isGuest = prefs.getBoolean("is_guest", false),
            isOnboarded = prefs.getBoolean("is_onboarded", true)
        )
    }

    fun saveProfile(profile: UserProfile) {
        prefs.edit()
            .putString("user_id", profile.id)
            .putString("user_name", profile.name)
            .putString("user_email", profile.email)
            .putString("exp_level", profile.experienceLevel)
            .putStringSet("target_roles", profile.targetRoles.toSet())
            .putString("primary_role", profile.primaryRole)
            .putString("coaching_lang", profile.coachingLanguage)
            .putString("biggest_challenge", profile.biggestChallenge)
            .putString("resume_text", profile.resumeText)
            .putInt("readiness_score", profile.currentReadinessScore)
            .putInt("completed_count", profile.interviewsCompletedCount)
            .putInt("streak_days", profile.practiceStreakDays)
            .putInt("score_improvement", profile.scoreImprovementSinceStart)
            .putBoolean("is_guest", profile.isGuest)
            .putBoolean("is_onboarded", profile.isOnboarded)
            .apply()
        _userProfile.value = profile
    }

    fun updateReadinessScore(newScore: Int) {
        val current = _userProfile.value
        val updatedCompleted = current.interviewsCompletedCount + 1
        val updated = current.copy(
            currentReadinessScore = newScore,
            interviewsCompletedCount = updatedCompleted
        )
        saveProfile(updated)
    }

    fun updateCoachingLanguage(language: String) {
        val current = _userProfile.value
        val updated = current.copy(coachingLanguage = language)
        saveProfile(updated)
    }

    fun setOnboarded(completed: Boolean) {
        val current = _userProfile.value
        val updated = current.copy(isOnboarded = completed)
        saveProfile(updated)
    }
}
