package com.mirandadevsource.laexquisssita.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabasePushNotificationRemote(
    private val clientProvider: () -> SupabaseClient,
) : PushNotificationRemote {
    private val client get() = clientProvider()

    override suspend fun registerDeviceToken(token: String, deviceModel: String?): Boolean {
        if (token.isBlank()) return false
        val params = buildJsonObject {
            put("p_token", token.trim())
            put("p_modelo", deviceModel?.trim()?.let(::JsonPrimitive) ?: JsonNull)
        }
        return try {
            client.postgrest.rpc("registrar_dispositivo_fcm", params)
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun unregisterDeviceToken(token: String): Boolean {
        if (token.isBlank()) return false
        val params = buildJsonObject {
            put("p_token", token.trim())
        }
        return try {
            client.postgrest.rpc("eliminar_dispositivo_fcm", params)
            true
        } catch (_: Exception) {
            false
        }
    }
}
