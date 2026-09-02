package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.InterviewReport
import com.example.data.model.InterviewTurn

@Entity(tableName = "interview_sessions")
data class InterviewEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val role: String,
    val companyName: String,
    val experienceLevel: String,
    val interviewTypeName: String,
    val difficultyName: String,
    val interviewerId: String,
    val interviewerName: String,
    val durationMinutes: Int,
    val turns: List<InterviewTurn>,
    val report: InterviewReport?,
    val overallScore: Int,
    val isCompleted: Boolean
)
