package com.example.ui.screens.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExperienceLevel
import com.example.data.model.InterviewConfig
import com.example.data.model.InterviewDifficulty
import com.example.data.model.InterviewType
import com.example.data.model.Interviewers
import com.example.data.model.ResumeData
import com.example.data.model.UserProfile
import com.example.ui.components.LinearCard
import com.example.ui.components.PersonaSelectCard
import com.example.ui.components.PrimaryActionButton
import com.example.ui.components.SecondaryActionButton
import com.example.ui.screens.setup.EXPERIENCE_LEVELS
import com.example.ui.screens.setup.POPULAR_ROLES
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TealAccent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateInterviewScreen(
    userProfile: UserProfile,
    onBackClick: () -> Unit,
    onLaunchLobby: (InterviewConfig) -> Unit
) {
    var step by remember { mutableStateOf(1) }
    val totalSteps = 4

    var role by remember { mutableStateOf(userProfile.primaryRole) }
    var experienceLevel by remember { mutableStateOf(userProfile.experienceLevel) }
    var interviewType by remember { mutableStateOf(InterviewType.HR_TECHNICAL) }
    var difficulty by remember { mutableStateOf(InterviewDifficulty.PROFESSIONAL) }
    var companyName by remember { mutableStateOf("") }
    var jobDescription by remember { mutableStateOf("") }
    var resumeText by remember { mutableStateOf(userProfile.resumeText) }
    var selectedInterviewer by remember { mutableStateOf(Interviewers.SARAH) }
    var durationMinutes by remember { mutableStateOf(15) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Setup Interview • Step $step of $totalSteps",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (step > 1) step-- else onBackClick()
                        }
                    ) {
                        Icon(
                            imageVector = if (step > 1) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Close,
                            contentDescription = "Back"
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Step Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (s in 1..totalSteps) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (s <= step) TealAccent else Color(0xFF1E293B))
                    )
                }
            }

            // Step Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (step) {
                    1 -> {
                        // Step 1: Role & Experience
                        item {
                            Text(
                                text = "Target Role & Experience",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "What position are you interviewing for today?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = role,
                                onValueChange = { role = it },
                                label = { Text("Target Job Title") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("target_role_field"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TealAccent,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedContainerColor = DarkSurface,
                                    unfocusedContainerColor = DarkSurface
                                )
                            )
                        }

                        item {
                            Text(
                                text = "Popular Suggestions",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                POPULAR_ROLES.forEach { r ->
                                    val isSelected = role == r
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isSelected) TealAccent else DarkSurface)
                                            .border(
                                                1.dp,
                                                if (isSelected) TealAccent else DarkBorder,
                                                RoundedCornerShape(16.dp)
                                            )
                                            .clickable { role = r }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = r,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Target Experience Level",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                EXPERIENCE_LEVELS.forEach { (lvl, desc) ->
                                    val isSelected = experienceLevel == lvl
                                    LinearCard(
                                        onClick = { experienceLevel = lvl },
                                        borderColor = if (isSelected) TealAccent else DarkBorder,
                                        backgroundColor = if (isSelected) Color(0x4D00ADB5) else DarkSurface
                                    ) {
                                        Column {
                                            Text(
                                                text = lvl,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = desc,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // Step 2: Round Type & Rigour / Difficulty
                        item {
                            Text(
                                text = "Interview Round & Difficulty",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Choose what type of interviewer dynamic you want to simulate.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        item {
                            Text(
                                text = "Select Round Type",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                InterviewType.values().forEach { itType ->
                                    val isSelected = interviewType == itType
                                    LinearCard(
                                        onClick = { interviewType = itType },
                                        borderColor = if (isSelected) TealAccent else DarkBorder,
                                        backgroundColor = if (isSelected) Color(0x4D00ADB5) else DarkSurface
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = itType.title,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = itType.subtitle,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TealAccent
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = itType.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Interviewer Rigour / Difficulty",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                InterviewDifficulty.values().forEach { diff ->
                                    val isSelected = difficulty == diff
                                    LinearCard(
                                        onClick = { difficulty = diff },
                                        borderColor = if (isSelected) TealAccent else DarkBorder,
                                        backgroundColor = if (isSelected) Color(0x4D00ADB5) else DarkSurface
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = diff.title,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = diff.subtitle,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TealAccent
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = diff.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // Step 3: Company, Job Description & Resume
                        item {
                            Text(
                                text = "Job Context & Resume",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Add company details or paste a JD so the AI asks pinpointed real-world questions.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = companyName,
                                onValueChange = { companyName = it },
                                label = { Text("Target Company (e.g. Swiggy, TCS, HDFC, Google)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TealAccent,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedContainerColor = DarkSurface,
                                    unfocusedContainerColor = DarkSurface
                                )
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = jobDescription,
                                onValueChange = { jobDescription = it },
                                label = { Text("Paste Job Description (Optional)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TealAccent,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedContainerColor = DarkSurface,
                                    unfocusedContainerColor = DarkSurface
                                ),
                                placeholder = { Text("Paste required skills, responsibilities, or tools...") }
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = resumeText,
                                onValueChange = { resumeText = it },
                                label = { Text("Candidate CV / Work Highlights") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TealAccent,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedContainerColor = DarkSurface,
                                    unfocusedContainerColor = DarkSurface
                                ),
                                placeholder = { Text("Paste summary of your skills, past projects and achievements...") }
                            )
                        }
                    }

                    4 -> {
                        // Step 4: AI Recruiter Persona & Duration
                        item {
                            Text(
                                text = "Select Interviewer & Length",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Each AI recruiter has a distinct tone, background, and evaluation style.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Interviewers.ALL.forEach { interviewer ->
                                    PersonaSelectCard(
                                        interviewer = interviewer,
                                        isSelected = selectedInterviewer.id == interviewer.id,
                                        onSelect = { selectedInterviewer = interviewer }
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Interview Duration",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                listOf(5 to "5 min (Quick)", 15 to "15 min (Standard)", 25 to "25 min (Deep)").forEach { (dur, label) ->
                                    val isSelected = durationMinutes == dur
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) TealAccent else DarkSurface)
                                            .border(
                                                1.dp,
                                                if (isSelected) TealAccent else DarkBorder,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { durationMinutes = dur }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Bottom Action Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                if (step < totalSteps) {
                    PrimaryActionButton(
                        text = "Continue to Step ${step + 1}",
                        onClick = { step++ },
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        testTag = "wizard_next_step"
                    )
                } else {
                    PrimaryActionButton(
                        text = "Enter Pre-Interview Lobby",
                        onClick = {
                            val config = InterviewConfig(
                                role = role.ifBlank { "Digital Marketing Manager" },
                                experienceLevel = experienceLevel,
                                interviewType = interviewType,
                                difficulty = difficulty,
                                resume = if (resumeText.isNotBlank()) ResumeData(
                                    candidateName = userProfile.name,
                                    rawText = resumeText
                                ) else null,
                                jobDescription = jobDescription,
                                companyName = companyName,
                                interviewer = selectedInterviewer,
                                durationMinutes = durationMinutes
                            )
                            onLaunchLobby(config)
                        },
                        icon = Icons.Default.PlayArrow,
                        testTag = "enter_lobby_button"
                    )
                }
            }
        }
    }
}
