package com.mirandadevsource.laexquisssita.core.network

import android.content.Context
import com.mirandadevsource.laexquisssita.core.config.SupabaseConfig
import com.mirandadevsource.laexquisssita.core.security.SessionStore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Proveedor singleton del cliente Supabase configurado con Auth y PostgREST.
 */
object SupabaseClientProvider {

    @Volatile
    private var instance: SupabaseClient? = null

    @Volatile
    private var sessionStoreInstance: SessionStore? = null

    fun getSessionStore(context: Context): SessionStore {
        return sessionStoreInstance ?: synchronized(this) {
            sessionStoreInstance ?: SessionStore(context.applicationContext).also {
                sessionStoreInstance = it
            }
        }
    }

    fun getClient(context: Context): SupabaseClient {
        return instance ?: synchronized(this) {
            instance ?: buildClient(context.applicationContext).also {
                instance = it
            }
        }
    }

    private fun buildClient(context: Context): SupabaseClient {
        val store = getSessionStore(context)
        return createSupabaseClient(
            supabaseUrl = SupabaseConfig.normalizedUrl,
            supabaseKey = SupabaseConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                sessionManager = store
                autoLoadFromStorage = true
                autoSaveToStorage = true
                alwaysAutoRefresh = true
            }
            install(Postgrest)
        }
    }
}
