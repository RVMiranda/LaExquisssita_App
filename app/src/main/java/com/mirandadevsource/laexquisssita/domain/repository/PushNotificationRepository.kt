package com.mirandadevsource.laexquisssita.domain.repository

interface PushNotificationRepository {
    suspend fun syncToken(token: String? = null): Boolean
    suspend fun unregisterCurrentToken(): Boolean
}
