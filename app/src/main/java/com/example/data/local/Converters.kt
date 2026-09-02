package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.InterviewReport
import com.example.data.model.InterviewTurn
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @TypeConverter
    fun fromReport(report: InterviewReport?): String? {
        if (report == null) return null
        return moshi.adapter(InterviewReport::class.java).toJson(report)
    }

    @TypeConverter
    fun toReport(json: String?): InterviewReport? {
        if (json.isNullOrEmpty()) return null
        return try {
            moshi.adapter(InterviewReport::class.java).fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromTurnsList(turns: List<InterviewTurn>?): String? {
        if (turns == null) return null
        val type = Types.newParameterizedType(List::class.java, InterviewTurn::class.java)
        return moshi.adapter<List<InterviewTurn>>(type).toJson(turns)
    }

    @TypeConverter
    fun toTurnsList(json: String?): List<InterviewTurn> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, InterviewTurn::class.java)
        return try {
            moshi.adapter<List<InterviewTurn>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromStringList(strings: List<String>?): String? {
        if (strings == null) return null
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return moshi.adapter<List<String>>(type).toJson(strings)
    }

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return try {
            moshi.adapter<List<String>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
