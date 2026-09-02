package com.example.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthState
import com.example.data.auth.FirebaseAuthManager
import com.example.data.model.UserProfile
import com.example.data.repository.InterviewRepository
import com.example.ui.components.BrandHeader
import com.example.ui.components.LinearCard
import com.example.ui.components.PrimaryActionButton
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TealPrimary
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    repository: InterviewRepository,
    userProfile: UserProfile,
    onUpdateProfile: (UserProfile) -> Unit,
    onEditProfileClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authManager = repository.authManager
    val currentUser by authManager.currentUser.collectAsState()
    val authState by authManager.authState.collectAsState()

    var resumeInput by remember { mutableStateOf(userProfile.resumeText) }
    var showResumeSavedToast by remember { mutableStateOf(false) }
    var isSyncingCloud by remember { mutableStateOf(false) }

    var showAuthDialog by remember { mutableStateOf(false) }
    var authMode by remember { mutableStateOf(0) } // 0: Login, 1: Register
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var authErrorMessage by remember { mutableStateOf("") }

    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { showAuthDialog = false },
            title = {
                Text(
                    text = if (authMode == 0) "Sign In to InterviewAI" else "Create InterviewAI Account",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Sign in to securely sync your interview scores, transcripts, and custom coach feedback across devices using Cloud Firestore.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (authMode == 1) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (authErrorMessage.isNotEmpty()) {
                        Text(
                            text = authErrorMessage,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFEF4444)
                        )
                    }

                    // Google Sign-In Alternative
                    Button(
                        onClick = {
                            scope.launch {
                                authErrorMessage = ""
                                val res = authManager.signInWithGoogle(context)
                                if (res.isSuccess) {
                                    showAuthDialog = false
                                    Toast.makeText(context, "Google Sign-In Successful!", Toast.LENGTH_SHORT).show()
                                } else {
                                    authErrorMessage = res.exceptionOrNull()?.message ?: "Google Sign-In Failed"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Continue with Google")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            authErrorMessage = ""
                            if (authMode == 0) {
                                val res = authManager.signInWithEmail(emailInput, passwordInput)
                                if (res.isSuccess) {
                                    showAuthDialog = false
                                    Toast.makeText(context, "Signed in successfully!", Toast.LENGTH_SHORT).show()
                                } else {
                                    authErrorMessage = res.exceptionOrNull()?.message ?: "Sign-in failed"
                                }
                            } else {
                                val res = authManager.signUpWithEmail(emailInput, passwordInput, nameInput)
                                if (res.isSuccess) {
                                    showAuthDialog = false
                                    Toast.makeText(context, "Account created & signed in!", Toast.LENGTH_SHORT).show()
                                } else {
                                    authErrorMessage = res.exceptionOrNull()?.message ?: "Sign-up failed"
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text(if (authMode == 0) "Sign In" else "Create Account")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        authMode = if (authMode == 0) 1 else 0
                        authErrorMessage = ""
                    }
                ) {
                    Text(if (authMode == 0) "Create new account" else "Have an account? Sign in")
                }
            }
        )
    }

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
                BrandHeader()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Candidate Profile & Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // User Identity & Firebase Cloud Card
            item {
                LinearCard(
                    backgroundColor = Color(0xFF111A2E),
                    borderColor = Color(0xFF233554),
                    cornerRadius = 18.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(TealAccent)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (currentUser?.displayName?.take(1) ?: userProfile.name.take(1)).uppercase(),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp
                                    ),
                                    color = Color(0xFF0F172A)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentUser?.displayName?.ifEmpty { userProfile.name } ?: userProfile.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = currentUser?.email ?: if (userProfile.email.isNotEmpty()) userProfile.email else "Guest Account",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (currentUser != null) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = if (currentUser != null) Color(0xFF10B981) else Color(0xFFF59E0B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (currentUser != null) "Cloud Firestore Synced" else "Local Storage Only",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (currentUser != null) Color(0xFF10B981) else Color(0xFFF59E0B)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Auth Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (currentUser == null) {
                                Button(
                                    onClick = { showAuthDialog = true },
                                    modifier = Modifier.weight(1f).testTag("sign_in_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Login,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sign In / Google", style = MaterialTheme.typography.labelMedium)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            isSyncingCloud = true
                                            repository.triggerCloudSync()
                                            isSyncingCloud = false
                                            Toast.makeText(context, "Cloud sync complete!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isSyncingCloud) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = TealAccent, strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp), tint = TealAccent)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Sync Now", color = TealAccent)
                                    }
                                }

                                Button(
                                    onClick = {
                                        authManager.signOut()
                                        Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                                ) {
                                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sign Out")
                                }
                            }
                        }
                    }
                }
            }

            // Coaching Language Setting
            item {
                Text(
                    text = "Coaching Language",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("English", "Hindi", "Bengali").forEach { lang ->
                        val isSelected = userProfile.coachingLanguage == lang
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
                                .clickable {
                                    onUpdateProfile(userProfile.copy(coachingLanguage = lang))
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = lang,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Resume & Portfolio Text
            item {
                Text(
                    text = "Your Resume / Work Highlights",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Paste your CV or summary so the AI recruiter can reference your exact projects and metrics.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = resumeInput,
                    onValueChange = { resumeInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    placeholder = {
                        Text(
                            text = "Paste text from your CV (skills, previous companies, major achievements, education)...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                PrimaryActionButton(
                    text = if (showResumeSavedToast) "Resume Saved Successfully!" else "Update Resume Data",
                    onClick = {
                        onUpdateProfile(userProfile.copy(resumeText = resumeInput))
                        showResumeSavedToast = true
                    }
                )
            }

            // Career Questionnaire Revisit
            item {
                LinearCard(
                    onClick = onEditProfileClick,
                    backgroundColor = DarkSurface,
                    borderColor = DarkBorder
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = TealAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Reconfigure Career Focus",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Update target roles, challenges, and experience level.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Privacy & System Standards
            item {
                LinearCard(
                    backgroundColor = DarkSurface,
                    borderColor = DarkBorder
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Interview Data Privacy & Firebase Security",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Data is secured in Firestore rules per user ID and audio is evaluated in-session.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
