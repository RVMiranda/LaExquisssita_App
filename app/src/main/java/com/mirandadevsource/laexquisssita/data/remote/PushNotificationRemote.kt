package com.mirandadevsource.laexquisssita.data.remote

interface PushNotificationRemote {
    suspend fun registerDeviceToken(token: String, deviceModel: String?): Boolean
    suspend fun unregisterDeviceToken(token: String): Boolean
}
