package com.mirandadevsource.laexquisssita.data.auth

import android.app.Activity
import android.content.Context
import androidx.credentials.*
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.mirandadevsource.laexquisssita.core.util.operationResult
import java.security.MessageDigest
import java.util.UUID

class GoogleAuthManager(context: Context) {
    private val manager = CredentialManager.create(context.applicationContext)

    /** Activity is used only during this call and never retained by the singleton. */
    suspend fun signInWithGoogle(activity: Activity, webClientId: String): Result<GoogleSignInResult> = operationResult {
        require(webClientId.endsWith(".apps.googleusercontent.com"))
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .build()
        val response = try {
            manager.getCredential(activity, GetCredentialRequest.Builder().addCredentialOption(option).build())
        } catch (e: NoCredentialException) {
            android.util.Log.e("EXQUISSSITA_AUTH", "NoCredentialException in manager.getCredential", e)
            throw GoogleAccountUnavailableException()
        } catch (e: Exception) {
            android.util.Log.e("EXQUISSSITA_AUTH", "Exception in manager.getCredential: ${e.javaClass.simpleName}: ${e.message}", e)
            throw e
        }
        val credential = response.credential
        check(credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
        val token = GoogleIdTokenCredential.createFrom(credential.data)
        GoogleSignInResult(token.idToken, "")
    }

    suspend fun clearCredentialState() = operationResult {
        manager.clearCredentialState(ClearCredentialStateRequest())
    }
}

// Avoid data-class generated toString exposing either value in logs.
class GoogleSignInResult(val idToken: String, val nonce: String)

class GoogleAccountUnavailableException : Exception("No Google credential available")
