package com.example.utils

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

object GoogleAuthHelper {

    private const val PREFS_NAME = "google_auth_prefs"
    private const val KEY_IS_SIGNED_IN = "is_google_signed_in"
    private const val KEY_USER_EMAIL = "google_user_email"
    private const val KEY_USER_NAME = "google_user_name"
    private const val KEY_USER_PHOTO = "google_user_photo"

    data class UserAccountInfo(
        val displayName: String,
        val email: String,
        val photoUrl: String? = null,
        val isFirebaseConnected: Boolean = false
    )

    val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }

    val currentUser: FirebaseUser?
        get() = auth?.currentUser

    fun getAccountInfo(context: Context): UserAccountInfo? {
        val fbUser = currentUser
        if (fbUser != null) {
            return UserAccountInfo(
                displayName = fbUser.displayName?.takeIf { it.isNotBlank() } ?: "Google User",
                email = fbUser.email?.takeIf { it.isNotBlank() } ?: "user@gmail.com",
                photoUrl = fbUser.photoUrl?.toString(),
                isFirebaseConnected = true
            )
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_IS_SIGNED_IN, false)) {
            return UserAccountInfo(
                displayName = prefs.getString(KEY_USER_NAME, "Pratik") ?: "Pratik",
                email = prefs.getString(KEY_USER_EMAIL, "pratik989095@gmail.com") ?: "pratik989095@gmail.com",
                photoUrl = prefs.getString(KEY_USER_PHOTO, null),
                isFirebaseConnected = auth != null
            )
        }
        return null
    }

    fun saveLocalAccount(context: Context, email: String, name: String, photoUrl: String? = null) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_IS_SIGNED_IN, true)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_PHOTO, photoUrl)
            .apply()
    }

    /**
     * Signs in with Google using Android Credential Manager and Firebase Auth.
     */
    suspend fun signInWithGoogle(
        context: Context,
        serverClientId: String? = null
    ): Result<FirebaseUser?> {
        return try {
            val credentialManager = CredentialManager.create(context)
            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            // Default Web Client ID (or passed from strings / config)
            val clientId = serverClientId?.takeIf { it.isNotBlank() }
                ?: "948311908685-default.apps.googleusercontent.com"

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(false)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = response.credential
            val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdToken.idToken

            val firebaseAuth = auth
            if (firebaseAuth != null) {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user
                if (user != null) {
                    saveLocalAccount(
                        context = context,
                        email = user.email ?: "user@gmail.com",
                        name = user.displayName ?: "Google User",
                        photoUrl = user.photoUrl?.toString()
                    )
                }
                Result.success(user)
            } else {
                saveLocalAccount(
                    context = context,
                    email = googleIdToken.id,
                    name = googleIdToken.displayName ?: "Google User",
                    photoUrl = googleIdToken.profilePictureUri?.toString()
                )
                Result.success(null)
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception("Sign-in cancelled by user"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut(context: Context) {
        try {
            auth?.signOut()
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
