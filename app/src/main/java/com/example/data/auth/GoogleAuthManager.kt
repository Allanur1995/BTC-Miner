package com.example.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed interface GoogleSignInOutcome {
    data class Success(val user: FirebaseUser) : GoogleSignInOutcome
    data object Cancelled : GoogleSignInOutcome
    data class Error(val message: String) : GoogleSignInOutcome
}

class GoogleAuthManager(
    context: Context,
    private val auth: FirebaseAuth = Firebase.auth
) {
    private val appContext = context.applicationContext
    private val credentialManager = CredentialManager.create(appContext)

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser)
        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    suspend fun signInWithGoogle(activityContext: Context): GoogleSignInOutcome {
        return try {
            val webClientId = appContext.getString(R.string.default_web_client_id)
            val option = GetSignInWithGoogleOption.Builder(webClientId).build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user
                    ?: return GoogleSignInOutcome.Error("Google sign-in completed without a user profile.")
                GoogleSignInOutcome.Success(user)
            } else {
                GoogleSignInOutcome.Error("Unsupported credential type returned.")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Interactive sign-in cancelled by user", e)
            GoogleSignInOutcome.Cancelled
        } catch (e: NoCredentialException) {
            Log.w("Auth", "No Google account available for sign-in", e)
            GoogleSignInOutcome.Error("No Google account found on this device. Please add a Google account and try again.")
        } catch (e: Exception) {
            Log.e("Auth", "Google sign-in failed", e)
            GoogleSignInOutcome.Error(e.localizedMessage ?: "Google sign-in failed. Please try again.")
        }
    }

    suspend fun attemptAutoSignIn(activityContext: Context): FirebaseUser? {
        auth.currentUser?.let { return it }
        return try {
            val webClientId = appContext.getString(R.string.default_web_client_id)
            val autoOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(autoOption)
                .build()
            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                auth.signInWithCredential(firebaseCredential).await().user
            } else {
                null
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Silent sign-in cancelled", e)
            null
        } catch (_: NoCredentialException) {
            null
        } catch (e: Exception) {
            Log.w("Auth", "Silent auto sign-in unavailable: ${e.message}")
            null
        }
    }

    suspend fun signOut() {
        auth.signOut()
        withContext(NonCancellable) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w("Auth", "Failed to clear credential state", e)
            }
        }
    }
}
