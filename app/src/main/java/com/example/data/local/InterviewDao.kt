package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InterviewDao {
    @Query("SELECT * FROM interview_sessions ORDER BY timestamp DESC")
    fun getAllInterviews(): Flow<List<InterviewEntity>>

    @Query("SELECT * FROM interview_sessions ORDER BY timestamp DESC")
    suspend fun getAllInterviewsSync(): List<InterviewEntity>

    @Query("SELECT * FROM interview_sessions WHERE id = :id LIMIT 1")
    suspend fun getInterviewById(id: String): InterviewEntity?

    @Query("SELECT * FROM interview_sessions WHERE isCompleted = 1 ORDER BY timestamp DESC LIMIT 1")
    fun getLatestCompletedInterview(): Flow<InterviewEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterview(entity: InterviewEntity)

    @Update
    suspend fun updateInterview(entity: InterviewEntity)

    @Query("DELETE FROM interview_sessions WHERE id = :id")
    suspend fun deleteInterview(id: String)

    @Query("SELECT AVG(overallScore) FROM interview_sessions WHERE isCompleted = 1")
    fun getAverageScore(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM interview_sessions WHERE isCompleted = 1")
    fun getCompletedCount(): Flow<Int>
}
