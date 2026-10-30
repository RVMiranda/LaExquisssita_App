package com.mirandadevsource.laexquisssita.core.security

import android.content.Context
import com.mirandadevsource.laexquisssita.data.repository.CustomerCache
import com.mirandadevsource.laexquisssita.domain.model.CustomerProfile
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.exception.NoSessionFoundException
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class SessionStore(context: Context) : SessionManager, CustomerCache {
    private val storage = EncryptedPreferences(context)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    init {
        // Pre-release plaintext storage is deliberately retired, never copied into backups.
        context.getSharedPreferences("exquisssita_client_prefs", Context.MODE_PRIVATE).edit().clear().apply()
    }

    override suspend fun saveSession(session: UserSession) = withContext(Dispatchers.IO) {
        storage.put("session", json.encodeToString(UserSession.serializer(), session))
    }
    override suspend fun loadSession(): UserSession = withContext(Dispatchers.IO) {
        val value = storage.get("session") ?: throw NoSessionFoundException()
        try { json.decodeFromString(UserSession.serializer(), value) }
        catch (_: Exception) { storage.remove("session"); throw NoSessionFoundException() }
    }
    override suspend fun deleteSession() = withContext(Dispatchers.IO) { storage.remove("session") }

    override fun profile(userId: String): CustomerProfile? = storage.get("customer:$userId:profile")?.let {
        runCatching { json.decodeFromString(CustomerProfile.serializer(), it) }.getOrNull()
    }
    override fun saveProfile(userId: String, profile: CustomerProfile) =
        storage.put("customer:$userId:profile", json.encodeToString(CustomerProfile.serializer(), profile))
    override fun qr(userId: String) = storage.get("customer:$userId:qr")
    override fun saveQr(userId: String, token: String) = storage.put("customer:$userId:qr", token)
    override fun clearCustomerData() = storage.clearCustomers()
}
