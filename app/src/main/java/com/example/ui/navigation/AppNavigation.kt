package com.example.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.AppDatabase
import com.example.data.local.UserPreferencesManager
import com.example.data.model.InterviewConfig
import com.example.data.model.QuestionReview
import com.example.data.model.QuickPracticeDrill
import com.example.data.repository.InterviewRepository
import com.example.ui.screens.create.CreateInterviewScreen
import com.example.ui.screens.interview.LiveInterviewScreen
import com.example.ui.screens.interview.LiveInterviewViewModel
import com.example.ui.screens.lobby.PreInterviewLobbyScreen
import com.example.ui.screens.main.MainContainerScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.practice.DrillRehearsalScreen
import com.example.ui.screens.results.ResultsScreen
import com.example.ui.screens.setup.ProfileSetupScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.transcribe.AudioTranscriptionScreen
import com.example.ui.screens.voice.LiveVoiceConversationScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object ProfileSetup : Screen("profile_setup")
    object Main : Screen("main")
    object CreateInterview : Screen("create_interview")
    object Lobby : Screen("lobby")
    object LiveInterview : Screen("live_interview")
    object LiveVoiceCall : Screen("live_voice_call")
    object AudioTranscribe : Screen("audio_transcribe")
    object Results : Screen("results/{interviewId}") {
        fun createRoute(interviewId: String) = "results/$interviewId"
    }
    object DrillRehearsal : Screen("drill_rehearsal")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val database = remember { AppDatabase.getDatabase(context) }
    val preferences = remember { UserPreferencesManager(context) }
    val repository = remember { InterviewRepository(database.interviewDao(), preferences) }

    val userProfile by repository.userProfile.collectAsState()
    val allInterviews by repository.allInterviews.collectAsState(initial = emptyList())

    var activeConfig by remember { mutableStateOf<InterviewConfig?>(null) }
    var activeDrill by remember { mutableStateOf<QuickPracticeDrill?>(null) }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Splash
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    if (!userProfile.isOnboarded) {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Onboarding
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinishOnboarding = {
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Profile Setup
        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(
                initialProfile = userProfile,
                onCompleteSetup = { updatedProfile ->
                    repository.updateUserProfile(updatedProfile)
                    repository.setOnboarded(true)
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                    }
                }
            )
        }

        // Main Container Dashboard
        composable(Screen.Main.route) {
            MainContainerScreen(
                repository = repository,
                userProfile = userProfile,
                recentInterviews = allInterviews,
                onStartInterviewClick = {
                    navController.navigate(Screen.CreateInterview.route)
                },
                onLiveVoiceCallClick = {
                    val config = activeConfig ?: InterviewConfig()
                    activeConfig = config
                    navController.navigate(Screen.LiveVoiceCall.route)
                },
                onAudioTranscriberClick = {
                    navController.navigate(Screen.AudioTranscribe.route)
                },
                onDrillClick = { drill ->
                    activeDrill = drill
                    navController.navigate(Screen.DrillRehearsal.route)
                },
                onInterviewDetailsClick = { id ->
                    navController.navigate(Screen.Results.createRoute(id))
                },
                onUpdateProfile = { updated ->
                    repository.updateUserProfile(updated)
                },
                onEditProfileClick = {
                    navController.navigate(Screen.ProfileSetup.route)
                }
            )
        }

        // Create Interview Wizard
        composable(Screen.CreateInterview.route) {
            CreateInterviewScreen(
                userProfile = userProfile,
                onBackClick = { navController.popBackStack() },
                onLaunchLobby = { config ->
                    activeConfig = config
                    navController.navigate(Screen.Lobby.route)
                }
            )
        }

        // Pre-Interview Lobby
        composable(Screen.Lobby.route) {
            val config = activeConfig ?: InterviewConfig()
            PreInterviewLobbyScreen(
                config = config,
                onBackClick = { navController.popBackStack() },
                onJoinInterview = {
                    navController.navigate(Screen.LiveInterview.route)
                },
                onStartLiveVoiceCall = {
                    navController.navigate(Screen.LiveVoiceCall.route)
                }
            )
        }

        // Live Interview Simulation (Standard Screen)
        composable(Screen.LiveInterview.route) {
            val config = activeConfig ?: InterviewConfig()
            val liveViewModel = remember(config) {
                LiveInterviewViewModel(
                    repository = repository,
                    config = config,
                    context = context
                )
            }

            LiveInterviewScreen(
                viewModel = liveViewModel,
                config = config,
                onInterviewCompleted = { sessionId ->
                    navController.navigate(Screen.Results.createRoute(sessionId)) {
                        popUpTo(Screen.Main.route)
                    }
                }
            )
        }

        // Real-Time Live Voice Conversation (gemini-3.1-flash-live-preview)
        composable(Screen.LiveVoiceCall.route) {
            val config = activeConfig ?: InterviewConfig()
            LiveVoiceConversationScreen(
                repository = repository,
                config = config,
                onBackClick = { navController.popBackStack() },
                onCallCompleted = { sessionId ->
                    navController.navigate(Screen.Results.createRoute(sessionId)) {
                        popUpTo(Screen.Main.route)
                    }
                }
            )
        }

        // Audio Transcription Studio (gemini-3.5-transcribe)
        composable(Screen.AudioTranscribe.route) {
            AudioTranscriptionScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Results / Evaluation Screen
        composable(
            route = Screen.Results.route,
            arguments = listOf(navArgument("interviewId") { type = NavType.StringType })
        ) { backStackEntry ->
            val interviewId = backStackEntry.arguments?.getString("interviewId") ?: ""
            val interview = allInterviews.firstOrNull { it.id == interviewId }

            if (interview != null) {
                ResultsScreen(
                    interview = interview,
                    onBackToHome = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Main.route) { inclusive = true }
                        }
                    },
                    onRetryInterview = {
                        navController.navigate(Screen.CreateInterview.route)
                    },
                    onRetryQuestion = { qReview ->
                        activeDrill = QuickPracticeDrill(
                            id = "retry_${System.currentTimeMillis()}",
                            title = "Refine Question",
                            category = "Retry Drill",
                            estimatedMinutes = 2,
                            promptQuestion = qReview.question,
                            tips = listOf("Actionable advice: ${qReview.actionableFix}"),
                            bestFramework = "STAR / Concise Metric First"
                        )
                        navController.navigate(Screen.DrillRehearsal.route)
                    }
                )
            }
        }

        // Drill Rehearsal
        composable(Screen.DrillRehearsal.route) {
            val drill = activeDrill
            if (drill != null) {
                DrillRehearsalScreen(
                    question = drill.promptQuestion,
                    category = drill.category,
                    framework = drill.bestFramework,
                    tips = drill.tips,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
