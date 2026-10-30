package com.mirandadevsource.laexquisssita.core.config

import com.mirandadevsource.laexquisssita.BuildConfig
import java.net.URI

object SupabaseConfig {
    val SUPABASE_URL = BuildConfig.SUPABASE_URL
    val SUPABASE_ANON_KEY = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
    val normalizedUrl get() = SUPABASE_URL.trim().trimEnd('/')
    val isConfigured: Boolean get() = runCatching {
        val uri = URI(normalizedUrl)
        val key = SUPABASE_ANON_KEY
        uri.scheme == "https" && !uri.host.isNullOrBlank() &&
            (key.startsWith("sb_publishable_") || isAnonJwt(key))
    }.getOrDefault(false)

    private fun isAnonJwt(key: String): Boolean = runCatching {
        val payload = String(android.util.Base64.decode(key.split('.')[1], android.util.Base64.URL_SAFE))
        org.json.JSONObject(payload).optString("role") == "anon"
    }.getOrDefault(false)
}
