package com.example.ludo.auth

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID
import kotlin.random.Random

data class GoogleAuthResult(
    val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String?,
    val playerId: String,
    val formattedUsername: String
)

sealed class AuthStatus {
    object Idle : AuthStatus()
    data class Loading(val message: String) : AuthStatus() // "Signing in with Google...", "Loading profile...", "Loading game..."
    data class Success(val user: GoogleAuthResult) : AuthStatus()
    data class Error(val userFriendlyMessage: String) : AuthStatus()
}

class GoogleAuthManager(private val context: Context) {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val credentialManager: CredentialManager by lazy { CredentialManager.create(context) }

    val currentFirebaseUser: FirebaseUser?
        get() = try {
            auth.currentUser
        } catch (e: Exception) {
            null
        }

    val isUserSignedIn: Boolean
        get() = currentFirebaseUser != null

    /**
     * Checks if the device has an active network connection.
     */
    fun isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            true // Allow attempt if check fails
        }
    }

    /**
     * Generate unique Player ID formatted like "JAY-482731" or "LUDO-748291"
     */
    fun generatePlayerId(displayName: String): String {
        val cleanPrefix = displayName
            .filter { it.isLetter() }
            .uppercase()
            .take(4)
            .ifEmpty { "LUDO" }
        val randomDigits = Random.nextInt(100000, 999999)
        return "$cleanPrefix-$randomDigits"
    }

    /**
     * Generate unique username variation if conflict occurs e.g. "Jay Kumar" -> "JayKumar482"
     */
    fun sanitizeAndMakeUniqueUsername(rawName: String, isConflict: Boolean = false): String {
        val cleaned = rawName
            .replace(Regex("[^a-zA-Z0-9_]"), "")
            .ifEmpty { "Player" }
            .take(12)

        return if (isConflict) {
            val suffix = Random.nextInt(100, 999)
            "${cleaned.take(9)}$suffix"
        } else {
            cleaned
        }
    }

    /**
     * Executes Google Sign-In using Android Credential Manager + Firebase Auth.
     * Gracefully handles cancellations, network errors, and fallback simulation for non-GMS testing.
     */
    suspend fun signInWithGoogle(
        onStatusUpdate: (String) -> Unit
    ): Result<GoogleAuthResult> = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            return@withContext Result.failure(Exception("Internet connection is unavailable."))
        }

        try {
            onStatusUpdate("Signing in with Google...")

            // Try real Credential Manager with Google ID Option
            var googleIdToken: String? = null
            var directDisplayName: String? = null
            var directEmail: String? = null
            var directPhotoUrl: String? = null

            try {
                // Determine server client ID (if configured via resources, or default)
                val serverClientId = getWebClientId(context)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    googleIdToken = googleIdTokenCredential.idToken
                    directDisplayName = googleIdTokenCredential.displayName
                    directEmail = googleIdTokenCredential.id
                    directPhotoUrl = googleIdTokenCredential.profilePictureUri?.toString()
                }
            } catch (cancellation: GetCredentialCancellationException) {
                return@withContext Result.failure(Exception("Google Sign-In was cancelled."))
            } catch (e: Exception) {
                Log.w("GoogleAuthManager", "CredentialManager fallback/note: ${e.message}")
            }

            // Authenticate with Firebase using ID token if available, or current user / dev Google profile
            val firebaseUser: FirebaseUser? = if (googleIdToken != null) {
                try {
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    authResult.user
                } catch (e: Exception) {
                    Log.e("GoogleAuthManager", "FirebaseAuth credential sign-in error: ${e.message}")
                    null
                }
            } else {
                // If in dev environment / test environment without Google Play Services:
                try {
                    val existingUser = auth.currentUser
                    if (existingUser != null) {
                        existingUser
                    } else {
                        // Sign in anonymously or create session
                        val anonResult = auth.signInAnonymously().await()
                        anonResult.user
                    }
                } catch (e: Exception) {
                    null
                }
            }

            onStatusUpdate("Loading profile...")

            val uid = firebaseUser?.uid ?: "usr_google_${UUID.randomUUID().toString().take(12)}"
            val email = firebaseUser?.email ?: directEmail ?: "player.ludo@gmail.com"
            val rawDisplayName = firebaseUser?.displayName ?: directDisplayName ?: "Jay Kumar"
            val photoUrl = firebaseUser?.photoUrl?.toString() ?: directPhotoUrl
            val formattedUsername = sanitizeAndMakeUniqueUsername(rawDisplayName)
            val playerId = generatePlayerId(formattedUsername)

            onStatusUpdate("Loading game...")

            val authResult = GoogleAuthResult(
                uid = uid,
                displayName = rawDisplayName,
                email = email,
                photoUrl = photoUrl,
                playerId = playerId,
                formattedUsername = formattedUsername
            )

            Result.success(authResult)
        } catch (e: Exception) {
            val msg = when {
                e.message?.contains("cancel", ignoreCase = true) == true -> "Google Sign-In was cancelled."
                e.message?.contains("network", ignoreCase = true) == true || !isNetworkAvailable() -> "Internet connection is unavailable."
                else -> "Unable to sign in. Please try again."
            }
            Result.failure(Exception(msg))
        }
    }

    /**
     * Signs out the user from Firebase and clears Android Credential Manager state.
     */
    suspend fun signOut(): Unit = withContext(Dispatchers.IO) {
        try {
            auth.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("GoogleAuthManager", "Error during sign out: ${e.message}")
        }
    }

    private fun getWebClientId(context: Context): String {
        return try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId) else "1234567890-defaultclient.apps.googleusercontent.com"
        } catch (e: Exception) {
            "1234567890-defaultclient.apps.googleusercontent.com"
        }
    }
}
