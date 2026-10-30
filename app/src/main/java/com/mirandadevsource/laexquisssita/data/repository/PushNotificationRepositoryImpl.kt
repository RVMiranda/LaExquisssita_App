package com.mirandadevsource.laexquisssita.data.repository

import android.os.Build
import com.google.firebase.messaging.FirebaseMessaging
import com.mirandadevsource.laexquisssita.core.security.PushTokenStorage
import com.mirandadevsource.laexquisssita.data.remote.PushNotificationRemote
import com.mirandadevsource.laexquisssita.domain.model.AuthState
import com.mirandadevsource.laexquisssita.domain.repository.AuthRepository
import com.mirandadevsource.laexquisssita.domain.repository.PushNotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class PushNotificationRepositoryImpl(
    private val remote: PushNotificationRemote,
    private val storage: PushTokenStorage,
    private val authRepository: AuthRepository,
    scope: CoroutineScope,
    private val tokenFetcher: suspend () -> String? = ::defaultFirebaseTokenFetcher,
) : PushNotificationRepository {

    init {
        scope.launch {
            authRepository.state
                .map { it is AuthState.Authenticated }
                .distinctUntilChanged()
                .collectLatest { isAuthenticated ->
                    if (isAuthenticated) {
                        syncToken()
                    }
                }
        }
    }

    override suspend fun syncToken(token: String?): Boolean {
        val effectiveToken = token?.trim()?.ifEmpty { null }
            ?: storage.getFcmToken()
            ?: tokenFetcher()

        if (effectiveToken.isNullOrBlank()) return false
        storage.setFcmToken(effectiveToken)

        val isAuthenticated = authRepository.state.value is AuthState.Authenticated
        if (!isAuthenticated) return true

        val model = try {
            "${Build.MANUFACTURER} ${Build.MODEL}".trim().take(160)
        } catch (_: Throwable) {
            "Android"
        }
        return remote.registerDeviceToken(effectiveToken, model)
    }

    override suspend fun unregisterCurrentToken(): Boolean {
        val token = storage.getFcmToken() ?: return true
        return remote.unregisterDeviceToken(token)
    }

    companion object {
        suspend fun defaultFirebaseTokenFetcher(): String? = suspendCancellableCoroutine { continuation ->
            try {
                FirebaseMessaging.getInstance().token
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            continuation.resume(task.result)
                        } else {
                            continuation.resume(null)
                        }
                    }
            } catch (_: Throwable) {
                continuation.resume(null)
            }
        }
    }
}
