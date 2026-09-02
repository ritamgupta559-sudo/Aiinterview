package com.example.ui.screens.results

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InterviewEntity
import com.example.data.model.QuestionReview
import com.example.ui.components.CategoryScoreRow
import com.example.ui.components.LinearCard
import com.example.ui.components.PrimaryActionButton
import com.example.ui.components.ScoreGauge
import com.example.ui.components.SecondaryActionButton
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ScoreAverage
import com.example.ui.theme.ScoreExcellent
import com.example.ui.theme.ScoreGood
import com.example.ui.theme.ScoreNeedsWork
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    interview: InterviewEntity,
    onBackToHome: () -> Unit,
    onRetryInterview: () -> Unit,
    onRetryQuestion: (QuestionReview) -> Unit
) {
    val report = interview.report ?: return

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Interview Evaluation Report",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackToHome) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Home"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Overall Score Card
            item {
                LinearCard(
                    backgroundColor = Color(0xFF111A2E),
                    borderColor = Color(0xFF233554),
                    cornerRadius = 20.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = interview.role,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Interviewer: ${interview.interviewerName} • ${interview.interviewTypeName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TealAccent
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = report.summary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            ScoreGauge(
                                score = report.overallScore,
                                size = 110.dp,
                                strokeWidth = 10.dp
                            )
                        }
                    }
                }
            }

            // Biggest Improvement Opportunity (Priority Focus)
            if (report.priorityImprovement.isNotBlank()) {
                item {
                    LinearCard(
                        backgroundColor = Color(0xFF1B2032),
                        borderColor = Color(0xFFF59E0B).copy(alpha = 0.5f),
                        cornerRadius = 16.dp
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Your #1 Priority Improvement",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFF59E0B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = report.priorityImprovement,
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Category Competency Breakdown
            item {
                LinearCard(
                    backgroundColor = DarkSurface,
                    borderColor = DarkBorder
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Performance Breakdown",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        CategoryScoreRow(category = "Job Knowledge & Substance", score = report.jobKnowledgeScore)
                        CategoryScoreRow(category = "Answer Quality & Depth", score = report.answerQualityScore)
                        CategoryScoreRow(category = "Communication Poise", score = report.communicationScore)
                        CategoryScoreRow(category = "Clarity & Brevity", score = report.clarityScore)
                        CategoryScoreRow(category = "English Precision & Fluency", score = report.englishScore)
                        CategoryScoreRow(category = "Answer Structure (STAR)", score = report.structureScore)
                    }
                }
            }

            // Strengths and Weaknesses
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Strengths Column
                    Column(modifier = Modifier.weight(1f)) {
                        LinearCard(
                            backgroundColor = DarkSurface,
                            borderColor = DarkBorder
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = ScoreExcellent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Key Strengths",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = ScoreExcellent
                                    )
                                }
                                report.strengths.forEach { s ->
                                    Text(
                                        text = "• $s",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Weaknesses Column
                    Column(modifier = Modifier.weight(1f)) {
                        LinearCard(
                            backgroundColor = DarkSurface,
                            borderColor = DarkBorder
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Improvement Gaps",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFEF4444)
                                    )
                                }
                                report.weaknesses.forEach { w ->
                                    Text(
                                        text = "• $w",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Filler Words Analysis
            if (report.fillerWords.isNotEmpty()) {
                item {
                    LinearCard(
                        backgroundColor = DarkSurface,
                        borderColor = DarkBorder
                    ) {
                        Column {
                            Text(
                                text = "Filler Words Detected",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                report.fillerWords.forEach { (word, count) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF1E293B))
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$count",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (count > 4) Color(0xFFEF4444) else TealAccent
                                            )
                                            Text(
                                                text = "\"$word\"",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Multilingual Regional Coaching Advice
            if (!report.coachingLanguageExplanation.isNullOrBlank()) {
                item {
                    LinearCard(
                        backgroundColor = Color(0xFF161F36),
                        borderColor = Color(0xFF8B5CF6).copy(alpha = 0.5f)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Regional Coaching Summary",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF8B5CF6)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = report.coachingLanguageExplanation,
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Question-by-Question Deep Review
            item {
                Text(
                    text = "Question-by-Question Review",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(report.questionReviews) { qReview ->
                LinearCard(
                    backgroundColor = DarkSurface,
                    borderColor = DarkBorder
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Question",
                                style = MaterialTheme.typography.labelSmall,
                                color = TealAccent
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (qReview.score >= 75) ScoreExcellent.copy(alpha = 0.2f)
                                        else ScoreAverage.copy(alpha = 0.2f)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${qReview.score}/100",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (qReview.score >= 75) ScoreExcellent else ScoreAverage
                                )
                            }
                        }

                        Text(
                            text = qReview.question,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Candidate's Actual Spoken Excerpt
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0E1524))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Your Answer:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "\"${qReview.candidateAnswer}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Positive & Fix
                        if (qReview.positiveFeedback.isNotBlank()) {
                            Text(
                                text = "✓ ${qReview.positiveFeedback}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ScoreExcellent
                            )
                        }

                        if (qReview.actionableFix.isNotBlank()) {
                            Text(
                                text = "→ Actionable Fix: ${qReview.actionableFix}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFF59E0B)
                            )
                        }

                        // Ideal Sample Answer
                        if (qReview.idealSampleAnswer.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF132236))
                                    .border(1.dp, Color(0xFF1E3A5F), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = TealAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Ideal High-Impact Answer:",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = TealAccent
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "\"${qReview.idealSampleAnswer}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Retry Question Button
                        Button(
                            onClick = { onRetryQuestion(qReview) },
                            modifier = Modifier.align(Alignment.End),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E293B),
                                contentColor = TealAccent
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Retry This Question",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Actions
            item {
                Spacer(modifier = Modifier.height(10.dp))
                PrimaryActionButton(
                    text = "Practice Another Mock Interview",
                    onClick = onRetryInterview,
                    testTag = "results_retry_interview_button"
                )
                Spacer(modifier = Modifier.height(8.dp))
                SecondaryActionButton(
                    text = "Back to Home Dashboard",
                    onClick = onBackToHome,
                    testTag = "results_back_home_button"
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
