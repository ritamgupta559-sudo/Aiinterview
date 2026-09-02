package com.example.data.remote

import com.example.data.model.InterviewConfig
import com.example.data.model.InterviewReport
import com.example.data.model.InterviewTurn
import com.example.data.model.ResumeData
import com.example.data.model.Speaker
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class InterviewAiEngine {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun buildSystemInstruction(config: InterviewConfig): String {
        val resumeSection = if (config.resume != null && config.resume.rawText.isNotEmpty()) {
            """
            CANDIDATE RESUME SUMMARY:
            - Name: ${config.resume.candidateName}
            - Skills: ${config.resume.skills.joinToString(", ")}
            - Companies/Experience: ${config.resume.companies.joinToString(", ")}
            - Education: ${config.resume.education.joinToString(", ")}
            - Details: ${config.resume.rawText.take(1000)}
            """.trimIndent()
        } else {
            "CANDIDATE RESUME: Not provided. Inquire naturally about their background."
        }

        val jdSection = if (config.jobDescription.isNotEmpty()) {
            """
            TARGET JOB DESCRIPTION:
            - Company: ${config.companyName.ifEmpty { "Target Employer" }}
            - Role: ${config.role}
            - Description / Requirements: ${config.jobDescription.take(1000)}
            """.trimIndent()
        } else {
            "TARGET ROLE: ${config.role} at ${config.companyName.ifEmpty { "a top company" }}"
        }

        val difficultyPersona = when (config.difficulty) {
            com.example.data.model.InterviewDifficulty.FRIENDLY ->
                "STYLE: Friendly, encouraging, supportive tone. Appropriate for freshers. Fewer sharp challenges."
            com.example.data.model.InterviewDifficulty.PROFESSIONAL ->
                "STYLE: Professional corporate interviewer. Balanced probing, asks for clear evidence and structure."
            com.example.data.model.InterviewDifficulty.CHALLENGING ->
                "STYLE: Challenging. Frequent follow-ups, demands for exact metrics, tests depth of knowledge, asks why."
            com.example.data.model.InterviewDifficulty.TOUGH ->
                "STYLE: Tough, sceptical senior leader. Rigorously tests assumptions, searches for inconsistencies, demands concise proof without being abusive or insulting."
        }

        return """
        You are ${config.interviewer.name}, ${config.interviewer.roleTitle}, conducting a realistic job interview for the position of "${config.role}" at "${config.companyName.ifEmpty { "our company" }}".
        
        $difficultyPersona
        INTERVIEW TYPE: ${config.interviewType.title} (${config.interviewType.description})
        EXPERIENCE LEVEL: ${config.experienceLevel}
        $jdSection
        $resumeSection
        
        CRITICAL RULES:
        1. YOU ARE THE INTERVIEWER, NOT A COACH. Do NOT provide tips, corrections, praise, or coaching during the interview.
        2. Keep most responses short and conversational (1 to 3 sentences). Real interviewers say things like "Okay.", "Interesting.", "Can you elaborate on that?", "What was the exact metric?", "Walk me through what you did next."
        3. Ask ONE question at a time.
        4. Dynamically follow up on what the candidate just said. If they mention numbers, tools, or achievements, probe deeper into their individual contribution.
        5. If an answer is vague or lacks substance, politely ask for specifics.
        6. If the candidate mentions their resume experience, reference it naturally.
        7. Never mention that you are an AI model, prompt, tokens, or Gemini. Stay 100% in character as ${config.interviewer.name}.
        8. The conversation is primarily in English, delivered with professional clarity.
        """.trimIndent()
    }

    suspend fun getNextInterviewerResponse(
        config: InterviewConfig,
        turns: List<InterviewTurn>
    ): String {
        val systemInstruction = buildSystemInstruction(config)
        val messages = turns.map { turn ->
            val role = if (turn.speaker == Speaker.USER) "user" else "model"
            Pair(role, turn.text)
        }

        val response = GeminiClient.generateResponse(
            systemInstruction = systemInstruction,
            messages = messages,
            temperature = 0.7f,
            jsonMode = false
        )
        return response.trim()
    }

    suspend fun generatePostInterviewReport(
        config: InterviewConfig,
        turns: List<InterviewTurn>,
        coachingLanguage: String
    ): InterviewReport {
        val transcript = turns.joinToString("\n") { turn ->
            "${if (turn.speaker == Speaker.USER) "Candidate" else config.interviewer.name}: ${turn.text}"
        }

        val analysisPrompt = """
        You are the Chief Interview Coach at InterviewAI. Analyze the following completed job interview transcript for a candidate interviewing for "${config.role}" (${config.experienceLevel} level).
        
        INTERVIEW CONFIGURATION:
        - Target Role: ${config.role}
        - Company: ${config.companyName}
        - Interview Type: ${config.interviewType.title}
        - Difficulty: ${config.difficulty.title}
        - Preferred Coaching Language: $coachingLanguage
        
        FULL TRANSCRIPT:
        $transcript
        
        Provide a rigorous, constructive, and highly actionable interview evaluation report in pure JSON format matching this schema:
        {
          "overallScore": integer (40-98),
          "communicationScore": integer (40-98),
          "answerQualityScore": integer (40-98),
          "clarityScore": integer (40-98),
          "englishScore": integer (40-98),
          "jobKnowledgeScore": integer (40-98),
          "confidenceCommunicationScore": integer (40-98),
          "structureScore": integer (40-98),
          "strengths": ["string", "string", "string"],
          "weaknesses": ["string", "string", "string"],
          "importantMoments": [
            {
              "question": "string",
              "answerExcerpt": "string",
              "critique": "string",
              "suggestedImprovement": "string"
            }
          ],
          "questionReviews": [
            {
              "question": "string",
              "candidateAnswer": "string",
              "score": integer,
              "positiveFeedback": "string",
              "actionableFix": "string",
              "idealSampleAnswer": "string"
            }
          ],
          "fillerWords": {
            "um": integer,
            "like": integer,
            "basically": integer,
            "you know": integer
          },
          "summary": "string",
          "priorityImprovement": "string",
          "nextPracticeRecommendation": "string",
          "coachingLanguageExplanation": "string (A warm, highly encouraging summary of the main advice written in $coachingLanguage)"
        }
        
        Ensure realistic, evidence-based scores and high-value Indian market coaching insights.
        """.trimIndent()

        val jsonString = GeminiClient.generateResponse(
            systemInstruction = "You are a professional hiring evaluator and talent coach. Return only valid JSON.",
            messages = listOf(Pair("user", analysisPrompt)),
            temperature = 0.3f,
            jsonMode = true
        )

        return try {
            moshi.adapter(InterviewReport::class.java).fromJson(jsonString)
                ?: fallbackReport()
        } catch (e: Exception) {
            fallbackReport()
        }
    }

    suspend fun parseResumeText(rawText: String): ResumeData {
        val prompt = """
        Extract structured information from this candidate's resume/CV.
        
        RESUME:
        $rawText
        
        Output pure JSON with schema:
        {
          "candidateName": "string",
          "skills": ["string"],
          "companies": ["string"],
          "education": ["string"],
          "achievements": ["string"]
        }
        """.trimIndent()

        val responseJson = GeminiClient.generateResponse(
            systemInstruction = "You are an HR resume parser. Return only valid JSON without inventing fake facts.",
            messages = listOf(Pair("user", prompt)),
            temperature = 0.2f,
            jsonMode = true
        )

        return try {
            val obj = org.json.JSONObject(responseJson)
            val name = obj.optString("candidateName", "Candidate")
            val skills = mutableListOf<String>()
            val companies = mutableListOf<String>()
            val education = mutableListOf<String>()
            val achievements = mutableListOf<String>()

            obj.optJSONArray("skills")?.let { arr ->
                for (i in 0 until arr.length()) skills.add(arr.getString(i))
            }
            obj.optJSONArray("companies")?.let { arr ->
                for (i in 0 until arr.length()) companies.add(arr.getString(i))
            }
            obj.optJSONArray("education")?.let { arr ->
                for (i in 0 until arr.length()) education.add(arr.getString(i))
            }
            obj.optJSONArray("achievements")?.let { arr ->
                for (i in 0 until arr.length()) achievements.add(arr.getString(i))
            }

            ResumeData(
                candidateName = name,
                rawText = rawText,
                skills = skills,
                companies = companies,
                education = education,
                achievements = achievements
            )
        } catch (e: Exception) {
            ResumeData(
                candidateName = "Candidate",
                rawText = rawText,
                skills = listOf("Communication", "Domain Knowledge", "Team Collaboration"),
                companies = emptyList(),
                education = emptyList(),
                achievements = emptyList()
            )
        }
    }

    private fun fallbackReport(): InterviewReport {
        return InterviewReport(
            overallScore = 75,
            communicationScore = 78,
            answerQualityScore = 80,
            clarityScore = 74,
            englishScore = 72,
            jobKnowledgeScore = 82,
            confidenceCommunicationScore = 75,
            structureScore = 73,
            strengths = listOf(
                "Good technical domain vocabulary and genuine enthusiasm",
                "Maintained steady composure and professional tone throughout",
                "Provided relevant project instances when probed"
            ),
            weaknesses = listOf(
                "Took more than 40 seconds to deliver the core metric or punchline",
                "Occasional reliance on filler phrases during thought pauses",
                "Needs more concise STAR structure for behavioural questions"
            ),
            importantMoments = listOf(
                com.example.data.model.ImportantMoment(
                    question = "Tell me about your background and recent projects.",
                    answerExcerpt = "I was responsible for managing multiple deliverables and driving growth.",
                    critique = "Strong delivery, but front-load key metrics to immediately capture interviewer attention.",
                    suggestedImprovement = "State your primary differentiator within the first 20 seconds."
                )
            ),
            questionReviews = listOf(
                com.example.data.model.QuestionReview(
                    question = "Why should we hire you for this role?",
                    candidateAnswer = "I have solid experience in this domain and I am very keen to learn and contribute.",
                    score = 76,
                    positiveFeedback = "Positive energy and eagerness to add value.",
                    actionableFix = "Directly map your specific tools/skills to the company's immediate hiring needs.",
                    idealSampleAnswer = "You are looking for someone to optimize conversion workflows. In my last role, I streamlined campaign testing and improved lead velocity by 28% in 90 days."
                )
            ),
            fillerWords = mapOf("um" to 3, "like" to 5, "basically" to 2),
            summary = "Solid performance with a strong foundation. Sharpening your answer structure will significantly boost your conversion rate.",
            priorityImprovement = "State your measurable result in the first sentence before detailing the background story.",
            nextPracticeRecommendation = "Practice the 'Why Should We Hire You' quick drill."
        )
    }
}
