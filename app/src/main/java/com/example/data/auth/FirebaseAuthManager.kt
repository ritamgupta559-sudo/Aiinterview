package com.example.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class FirebaseAuthManager {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        try {
            _currentUser.value = auth.currentUser
            auth.addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                _currentUser.value = user
                _authState.value = if (user != null) AuthState.Authenticated(user) else AuthState.Idle
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error initializing auth state listener", e)
        }
    }

    suspend fun signInWithGoogle(
        context: Context,
        serverClientId: String = ""
    ): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        _authState.value = AuthState.Loading
        try {
            val credentialManager = CredentialManager.create(context)
            
            // Build Google ID option
            val googleIdOptionBuilder = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)

            if (serverClientId.isNotEmpty()) {
                googleIdOptionBuilder.setServerClientId(serverClientId)
            } else {
                // Default fallback client if not explicitly configured
                googleIdOptionBuilder.setServerClientId("dummy-client-id.apps.googleusercontent.com")
            }

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOptionBuilder.build())
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user ?: throw Exception("Firebase user is null after sign in")
                _currentUser.value = user
                _authState.value = AuthState.Authenticated(user)
                Result.success(user)
            } else {
                throw Exception("Received unexpected credential type: ${credential.type}")
            }
        } catch (e: GetCredentialCancellationException) {
            _authState.value = AuthState.Idle
            Result.failure(Exception("Google Sign-In was cancelled"))
        } catch (e: Exception) {
            Log.w("FirebaseAuthManager", "Google sign in error: ${e.message}")
            _authState.value = AuthState.Error(e.message ?: "Google sign in failed")
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        _authState.value = AuthState.Loading
        try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = authResult.user ?: throw Exception("User not found")
            _currentUser.value = user
            _authState.value = AuthState.Authenticated(user)
            Result.success(user)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Sign in failed")
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String, displayName: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        _authState.value = AuthState.Loading
        try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = authResult.user ?: throw Exception("Account creation failed")
            if (displayName.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()
                user.updateProfile(profileUpdates).await()
            }
            _currentUser.value = user
            _authState.value = AuthState.Authenticated(user)
            Result.success(user)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Account creation failed")
            Result.failure(e)
        }
    }

    suspend fun signInAnonymously(): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        _authState.value = AuthState.Loading
        try {
            val authResult = auth.signInAnonymously().await()
            val user = authResult.user ?: throw Exception("Anonymous sign in failed")
            _currentUser.value = user
            _authState.value = AuthState.Authenticated(user)
            Result.success(user)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Anonymous sign in failed")
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            auth.signOut()
            _currentUser.value = null
            _authState.value = AuthState.Idle
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error signing out", e)
        }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: "local_user"
    }

    fun getCurrentUserEmail(): String {
        return auth.currentUser?.email ?: ""
    }

    fun getCurrentUserName(): String {
        return auth.currentUser?.displayName ?: ""
    }
}
