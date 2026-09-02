package com.example.data.model

import com.squareup.moshi.JsonClass
import java.util.UUID

enum class ExperienceLevel(val title: String, val description: String) {
    FRESHER("Fresher", "College graduate or looking for first full-time job"),
    JUNIOR("0–2 years", "Early career professional developing core skills"),
    MID("2–5 years", "Experienced specialist with proven execution"),
    SENIOR("5–10 years", "Senior professional handling complex domains"),
    LEADERSHIP("10+ years", "Strategic leader, manager or executive")
}

enum class InterviewType(val title: String, val subtitle: String, val description: String) {
    HR(
        title = "HR Interview",
        subtitle = "Personality & Behavioural",
        description = "Focuses on culture fit, communication, career history, motivation, and behavioural questions."
    ),
    TECHNICAL(
        title = "Technical Interview",
        subtitle = "Domain & Role Knowledge",
        description = "Deep dive into role-specific knowledge, technical decisions, problem-solving, and tooling."
    ),
    HR_TECHNICAL(
        title = "HR + Technical",
        subtitle = "Balanced Realistic Round",
        description = "The industry standard combination covering background, practical skills, scenario handling, and situational judgement."
    ),
    STRESS(
        title = "Stress Interview",
        subtitle = "High-Pressure Follow-ups",
        description = "Simulates demanding interviewers who challenge assumptions, ask tight follow-ups, and test composure."
    ),
    FINAL_ROUND(
        title = "Final Round",
        subtitle = "Hiring Manager & Leadership",
        description = "Senior manager style evaluating strategic alignment, long-term impact, ownership, and value add."
    )
}

enum class InterviewDifficulty(val title: String, val subtitle: String, val description: String) {
    FRIENDLY(
        title = "Friendly",
        subtitle = "Supportive & Patient",
        description = "Encouraging tone with fewer aggressive challenges. Perfect for freshers or building initial confidence."
    ),
    PROFESSIONAL(
        title = "Professional",
        subtitle = "Standard Corporate",
        description = "Realistic corporate hiring standard. Balanced probing and requests for concrete examples."
    ),
    CHALLENGING(
        title = "Challenging",
        subtitle = "Deep Probing & Metrics",
        description = "Frequent follow-ups, demands for exact metrics, tests logical clarity, and questions vague claims."
    ),
    TOUGH(
        title = "Tough",
        subtitle = "Sceptical Executive",
        description = "Demanding senior interviewer who rigorously inspects results, challenges contradictions, and tests resilience."
    )
}

data class Interviewer(
    val id: String,
    val name: String,
    val roleTitle: String,
    val style: String,
    val bio: String,
    val avatarBgColor: Long,
    val initialGreeting: String,
    val voicePitch: Float = 1.0f,
    val voiceSpeed: Float = 1.0f
)

object Interviewers {
    val SARAH = Interviewer(
        id = "sarah",
        name = "Sarah",
        roleTitle = "Senior HR Recruiter",
        style = "Calm • Professional",
        bio = "10+ years recruiting for top MNCs and high-growth firms. Focuses on clarity, poise, and structured answers.",
        avatarBgColor = 0xFF00ADB5,
        initialGreeting = "Hi, thanks for joining today. I'd like to start by getting to know you a little better. Could you briefly introduce yourself and walk me through your recent background?",
        voicePitch = 1.05f,
        voiceSpeed = 0.98f
    )

    val ARJUN = Interviewer(
        id = "arjun",
        name = "Arjun",
        roleTitle = "Hiring Manager",
        style = "Analytical • Direct",
        bio = "Engineering & Product lead known for razor-sharp follow-ups on metrics, architecture, and problem-solving.",
        avatarBgColor = 0xFF2563EB,
        initialGreeting = "Hello. Good to meet you. Let's jump straight in. Walk me through your most impactful project and what your specific contribution was.",
        voicePitch = 0.95f,
        voiceSpeed = 1.02f
    )

    val MAYA = Interviewer(
        id = "maya",
        name = "Maya",
        roleTitle = "Senior Talent Partner",
        style = "Warm • Observant",
        bio = "Specializes in talent strategy across India. Highly observant of situational examples, team dynamics, and conflict resolution.",
        avatarBgColor = 0xFF8B5CF6,
        initialGreeting = "Welcome! I'm excited to speak with you today. To kick things off, tell me what made you apply for this role and how your previous experience aligns with it.",
        voicePitch = 1.1f,
        voiceSpeed = 0.95f
    )

    val VIKRAM = Interviewer(
        id = "vikram",
        name = "Vikram",
        roleTitle = "Department Head",
        style = "Demanding • Strategic",
        bio = "Senior executive who prioritizes business outcome, cost-benefit reasoning, accountability, and clarity under pressure.",
        avatarBgColor = 0xFFDC2626,
        initialGreeting = "Good day. Let's discuss your candidacy. In 60 seconds, summarize why we should hire you over other qualified candidates for this position.",
        voicePitch = 0.88f,
        voiceSpeed = 1.05f
    )

    val ALL = listOf(SARAH, ARJUN, MAYA, VIKRAM)
}

data class ResumeData(
    val candidateName: String = "",
    val rawText: String = "",
    val education: List<String> = emptyList(),
    val companies: List<String> = emptyList(),
    val skills: List<String> = emptyList(),
    val achievements: List<String> = emptyList()
)

data class InterviewConfig(
    val role: String = "Digital Marketing Manager",
    val experienceLevel: String = "2–5 years",
    val interviewType: InterviewType = InterviewType.HR_TECHNICAL,
    val difficulty: InterviewDifficulty = InterviewDifficulty.PROFESSIONAL,
    val resume: ResumeData? = null,
    val jobDescription: String = "",
    val companyName: String = "",
    val interviewer: Interviewer = Interviewers.SARAH,
    val durationMinutes: Int = 15
)

enum class Speaker {
    USER, AI
}

data class InterviewTurn(
    val id: String = UUID.randomUUID().toString(),
    val speaker: Speaker,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class QuestionReview(
    val question: String = "",
    val candidateAnswer: String = "",
    val score: Int = 75,
    val positiveFeedback: String = "",
    val actionableFix: String = "",
    val idealSampleAnswer: String = ""
)

@JsonClass(generateAdapter = true)
data class ImportantMoment(
    val question: String = "",
    val answerExcerpt: String = "",
    val critique: String = "",
    val suggestedImprovement: String = ""
)

@JsonClass(generateAdapter = true)
data class InterviewReport(
    val overallScore: Int = 74,
    val communicationScore: Int = 78,
    val answerQualityScore: Int = 82,
    val clarityScore: Int = 73,
    val englishScore: Int = 69,
    val jobKnowledgeScore: Int = 80,
    val confidenceCommunicationScore: Int = 72,
    val structureScore: Int = 71,
    val strengths: List<String> = emptyList(),
    val weaknesses: List<String> = emptyList(),
    val importantMoments: List<ImportantMoment> = emptyList(),
    val questionReviews: List<QuestionReview> = emptyList(),
    val fillerWords: Map<String, Int> = emptyMap(),
    val summary: String = "",
    val priorityImprovement: String = "",
    val nextPracticeRecommendation: String = "",
    val coachingLanguageExplanation: String? = null
)

data class UserProfile(
    val id: String = "user_default",
    val name: String = "Ritam Gupta",
    val email: String = "ritamgupta.559@gmail.com",
    val experienceLevel: String = "2–5 years",
    val targetRoles: List<String> = listOf("Digital Marketing Manager", "Sales Executive", "Software Engineer"),
    val primaryRole: String = "Digital Marketing Manager",
    val coachingLanguage: String = "English", // "English", "Hindi", "Bengali"
    val biggestChallenge: String = "I struggle with structuring answers under pressure",
    val resumeText: String = "",
    val currentReadinessScore: Int = 74,
    val interviewsCompletedCount: Int = 5,
    val practiceStreakDays: Int = 4,
    val scoreImprovementSinceStart: Int = 9,
    val isGuest: Boolean = false,
    val isOnboarded: Boolean = true
)

data class QuickPracticeDrill(
    val id: String,
    val title: String,
    val category: String,
    val estimatedMinutes: Int,
    val promptQuestion: String,
    val tips: List<String>,
    val bestFramework: String
)

object QuickDrills {
    val ALL = listOf(
        QuickPracticeDrill(
            id = "intro",
            title = "Tell Me About Yourself",
            category = "Opening & Poise",
            estimatedMinutes = 3,
            promptQuestion = "Could you walk me through your background and what brings you to this interview?",
            tips = listOf(
                "Keep it under 90 seconds",
                "Follow Present -> Past -> Future framework",
                "Highlight 1 major achievement"
            ),
            bestFramework = "Present (current role) -> Past (key experience & skills) -> Future (why this role)"
        ),
        QuickPracticeDrill(
            id = "why_hire",
            title = "Why Should We Hire You?",
            category = "Value Proposition",
            estimatedMinutes = 3,
            promptQuestion = "Why should we hire you over other qualified candidates for this position?",
            tips = listOf(
                "Directly match your skills to company pain points",
                "Quantify your past impact (e.g. % growth, ₹ saved)",
                "Show cultural alignment and enthusiasm"
            ),
            bestFramework = "3 Pillars: Unique Skill + Proven Track Record + Hunger to Excel"
        ),
        QuickPracticeDrill(
            id = "weakness",
            title = "Strengths & Weaknesses",
            category = "Self-Awareness",
            estimatedMinutes = 4,
            promptQuestion = "What is your biggest professional weakness, and what steps are you taking to improve it?",
            tips = listOf(
                "Never say 'I am a perfectionist' or 'I work too hard'",
                "Pick a real, non-fatal skill gap",
                "Spend 80% of your answer explaining your active mitigation strategy"
            ),
            bestFramework = "Real Weakness + Specific Past Instance + Action Plan + Current Progress"
        ),
        QuickPracticeDrill(
            id = "salary",
            title = "Salary Expectations",
            category = "Negotiation",
            estimatedMinutes = 3,
            promptQuestion = "What are your salary expectations for this position?",
            tips = listOf(
                "Anchor to industry benchmark for your experience",
                "State a narrow range rather than single number",
                "Express flexibility based on total compensation and growth"
            ),
            bestFramework = "Market Research + Value Justification + Constructive Range"
        ),
        QuickPracticeDrill(
            id = "conflict",
            title = "Handling Conflict / Difficult Situations",
            category = "STAR Method",
            estimatedMinutes = 4,
            promptQuestion = "Tell me about a time you had a significant disagreement with a colleague or manager. How did you resolve it?",
            tips = listOf(
                "Use the STAR method (Situation, Task, Action, Result)",
                "Focus on the business goal, not personal ego",
                "Highlight active listening and empathy"
            ),
            bestFramework = "STAR: Situation (20s) -> Task (10s) -> Action (40s) -> Result (20s)"
        ),
        QuickPracticeDrill(
            id = "tough_gap",
            title = "Explaining Career Gap or Job Switch",
            category = "Career Narrative",
            estimatedMinutes = 3,
            promptQuestion = "Can you explain why you are looking to switch roles at this point in your career?",
            tips = listOf(
                "Never badmouth your current or former employer",
                "Frame the move as running towards growth, not escaping",
                "Connect past learnings to future potential"
            ),
            bestFramework = "Positive Foundation + Career Milestone + Forward Vision"
        )
    )
}
