package com.example.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.data.local.InterviewEntity
import com.example.data.model.QuickPracticeDrill
import com.example.data.model.UserProfile
import com.example.data.repository.InterviewRepository
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.practice.PracticeScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.progress.ProgressScreen
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TealAccent

enum class NavTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    PRACTICE("Drills", Icons.Default.RecordVoiceOver),
    PROGRESS("Analytics", Icons.Default.Assessment),
    PROFILE("Profile", Icons.Default.Person)
}

@Composable
fun MainContainerScreen(
    repository: InterviewRepository,
    userProfile: UserProfile,
    recentInterviews: List<InterviewEntity>,
    onStartInterviewClick: () -> Unit,
    onLiveVoiceCallClick: () -> Unit = onStartInterviewClick,
    onAudioTranscriberClick: () -> Unit = {},
    onDrillClick: (QuickPracticeDrill) -> Unit,
    onInterviewDetailsClick: (String) -> Unit,
    onUpdateProfile: (UserProfile) -> Unit,
    onEditProfileClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(NavTab.HOME) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                modifier = Modifier
                    .border(width = 1.dp, color = DarkBorder)
            ) {
                NavTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) TealAccent else Color(0xFF64748B)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) TealAccent else Color(0xFF64748B)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFF1E293B)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                NavTab.HOME -> HomeScreen(
                    userProfile = userProfile,
                    recentInterviews = recentInterviews,
                    onStartInterviewClick = onStartInterviewClick,
                    onLiveVoiceCallClick = onLiveVoiceCallClick,
                    onAudioTranscriberClick = onAudioTranscriberClick,
                    onDrillClick = onDrillClick,
                    onInterviewDetailsClick = onInterviewDetailsClick
                )
                NavTab.PRACTICE -> PracticeScreen(
                    onStartDrill = onDrillClick,
                    onOpenAudioTranscriber = onAudioTranscriberClick
                )
                NavTab.PROGRESS -> ProgressScreen(
                    userProfile = userProfile,
                    interviews = recentInterviews
                )
                NavTab.PROFILE -> ProfileScreen(
                    repository = repository,
                    userProfile = userProfile,
                    onUpdateProfile = onUpdateProfile,
                    onEditProfileClick = onEditProfileClick
                )
            }
        }
    }
}
