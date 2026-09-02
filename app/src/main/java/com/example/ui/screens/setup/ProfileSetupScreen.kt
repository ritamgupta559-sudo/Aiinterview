package com.example.ui.screens.setup

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.data.model.UserProfile
import com.example.ui.components.BrandHeader
import com.example.ui.components.LinearCard
import com.example.ui.components.PrimaryActionButton
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TealAccent

val POPULAR_ROLES = listOf(
    "Digital Marketing Manager",
    "Sales Executive",
    "Software Engineer",
    "Customer Support / BPO",
    "Data Analyst",
    "Product Manager",
    "Banking & Finance Specialist",
    "HR Associate"
)

val EXPERIENCE_LEVELS = listOf(
    "Fresher" to "College graduate or looking for first full-time role",
    "0–2 years" to "Early career professional developing core execution",
    "2–5 years" to "Experienced specialist with proven domain track record",
    "5–10 years" to "Senior professional handling complex projects",
    "10+ years" to "Strategic leader, executive or department manager"
)

val COACHING_LANGUAGES = listOf(
    "English" to "Coaching and interview purely in professional English",
    "Hindi" to "English interview with key coaching explanations in Hindi",
    "Bengali" to "English interview with key coaching explanations in Bengali"
)

val CHALLENGES = listOf(
    "Structuring answers under pressure (STAR method)",
    "Confidence and overcoming nervous freezes",
    "English vocabulary and conversational fluency",
    "Explaining complex technical or sales metrics",
    "Handling unexpected or tough follow-up questions"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupScreen(
    initialProfile: UserProfile,
    onCompleteSetup: (UserProfile) -> Unit
) {
    var candidateName by remember { mutableStateOf(initialProfile.name) }
    var selectedRole by remember { mutableStateOf(initialProfile.primaryRole) }
    var customRole by remember { mutableStateOf("") }
    var selectedExperience by remember { mutableStateOf(initialProfile.experienceLevel) }
    var selectedLanguage by remember { mutableStateOf(initialProfile.coachingLanguage) }
    var selectedChallenge by remember { mutableStateOf(initialProfile.biggestChallenge) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                BrandHeader(showTagline = true)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Personalize Your Interview Coach",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Tell us about your career goals so the AI recruiter matches your exact hiring standard.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Name
            item {
                Text(
                    text = "Your Full Name",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = candidateName,
                    onValueChange = { candidateName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("name_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    placeholder = { Text("e.g. Ritam Gupta") }
                )
            }

            // Target Role
            item {
                Text(
                    text = "Primary Target Role",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    POPULAR_ROLES.forEach { role ->
                        val isSelected = selectedRole == role
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) TealAccent else Color(0xFF131B2E))
                                .border(
                                    1.dp,
                                    if (isSelected) TealAccent else Color(0xFF283655),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedRole = role }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = role,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Experience Level
            item {
                Text(
                    text = "Experience Level",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EXPERIENCE_LEVELS.forEach { (level, desc) ->
                        val isSelected = selectedExperience == level
                        LinearCard(
                            onClick = { selectedExperience = level },
                            borderColor = if (isSelected) TealAccent else DarkBorder,
                            backgroundColor = if (isSelected) Color(0xFF132236) else DarkSurface
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .border(
                                            2.dp,
                                            if (isSelected) TealAccent else Color(0xFF64748B),
                                            CircleShape
                                        )
                                        .background(if (isSelected) TealAccent else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF0F172A))
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = level,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
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

            // Coaching Language
            item {
                Text(
                    text = "Coaching Language Preference",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "All mock interviews are conducted in professional English. Feedback summaries can include your preferred regional language.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    COACHING_LANGUAGES.forEach { (lang, desc) ->
                        val isSelected = selectedLanguage == lang
                        LinearCard(
                            onClick = { selectedLanguage = lang },
                            borderColor = if (isSelected) TealAccent else DarkBorder,
                            backgroundColor = if (isSelected) Color(0xFF132236) else DarkSurface
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .border(
                                            2.dp,
                                            if (isSelected) TealAccent else Color(0xFF64748B),
                                            CircleShape
                                        )
                                        .background(if (isSelected) TealAccent else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF0F172A))
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = lang,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
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

            // Biggest Challenge
            item {
                Text(
                    text = "Your Biggest Interview Challenge",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CHALLENGES.forEach { challenge ->
                        val isSelected = selectedChallenge == challenge
                        LinearCard(
                            onClick = { selectedChallenge = challenge },
                            borderColor = if (isSelected) TealAccent else DarkBorder,
                            backgroundColor = if (isSelected) Color(0xFF132236) else DarkSurface
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(TealAccent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        androidx.compose.material3.Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF0F172A),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, Color(0xFF64748B), CircleShape)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = challenge,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Submit Button
            item {
                Spacer(modifier = Modifier.height(12.dp))
                PrimaryActionButton(
                    text = "Save Profile & Enter Dashboard",
                    onClick = {
                        val effectiveRole = if (customRole.isNotBlank()) customRole.trim() else selectedRole
                        val updated = initialProfile.copy(
                            name = candidateName.ifBlank { "Ritam Gupta" },
                            primaryRole = effectiveRole,
                            targetRoles = listOf(effectiveRole) + POPULAR_ROLES.filter { it != effectiveRole }.take(2),
                            experienceLevel = selectedExperience,
                            coachingLanguage = selectedLanguage,
                            biggestChallenge = selectedChallenge,
                            isOnboarded = true
                        )
                        onCompleteSetup(updated)
                    },
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    testTag = "save_profile_button"
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
