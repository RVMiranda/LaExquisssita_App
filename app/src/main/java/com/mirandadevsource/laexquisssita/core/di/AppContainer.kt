package com.mirandadevsource.laexquisssita.core.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mirandadevsource.laexquisssita.core.config.SupabaseConfig
import com.mirandadevsource.laexquisssita.core.network.SupabaseClientProvider
import com.mirandadevsource.laexquisssita.core.security.CustomerPreferences
import com.mirandadevsource.laexquisssita.data.auth.*
import com.mirandadevsource.laexquisssita.data.remote.SupabaseCustomerRemote
import com.mirandadevsource.laexquisssita.data.remote.SupabasePromotionRemote
import com.mirandadevsource.laexquisssita.data.remote.SupabasePushNotificationRemote
import com.mirandadevsource.laexquisssita.data.repository.*
import com.mirandadevsource.laexquisssita.domain.repository.*
import com.mirandadevsource.laexquisssita.features.auth.viewmodel.CustomerViewModel
import com.mirandadevsource.laexquisssita.features.home.viewmodel.HomeViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOf

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val sessionStore by lazy { SupabaseClientProvider.getSessionStore(appContext) }
    val supabaseClient by lazy {
        check(SupabaseConfig.isConfigured) { "Missing public Supabase configuration" }
        SupabaseClientProvider.getClient(appContext)
    }
    val googleAuthManager by lazy { GoogleAuthManager(appContext) }
    val preferences by lazy { CustomerPreferences(appContext) }
    private val remote by lazy { SupabaseCustomerRemote { supabaseClient } }
    private val gateway: AuthGateway by lazy {
        if (SupabaseConfig.isConfigured) SupabaseAuthGateway(supabaseClient, sessionStore)
        else object : AuthGateway {
            override val status = flowOf(IdentityStatus.Guest)
            override suspend fun signIn(idToken: String, nonce: String): Unit = error("Missing configuration")
            override suspend fun signInWithEmail(email: String, password: String): Unit = error("Missing configuration")
            override suspend fun signUpWithEmail(email: String, password: String, nombre: String, telefono: String?): EmailSignUpResult = error("Missing configuration")
            override suspend fun deleteAccount(): Unit = error("Missing configuration")
            override suspend fun signOut() = Unit
        }
    }
    val authRepository: AuthRepository by lazy { AuthRepositoryImpl(gateway, remote, sessionStore, scope) }
    val loyaltyRepository: LoyaltyRepository by lazy { LoyaltyRepositoryImpl(remote, sessionStore, authRepository) }
    val pushTokenRemote by lazy { SupabasePushNotificationRemote { supabaseClient } }
    val pushNotificationRepository: PushNotificationRepository by lazy {
        PushNotificationRepositoryImpl(
            remote = pushTokenRemote,
            storage = preferences,
            authRepository = authRepository,
            scope = scope,
        )
    }
    val promotionRepository by lazy {
        PromotionRepositoryImpl(
            remote = SupabasePromotionRemote { supabaseClient },
            cache = EncryptedPromotionCache(appContext),
        )
    }
    val customerViewModelFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == CustomerViewModel::class.java)
            @Suppress("UNCHECKED_CAST")
            return CustomerViewModel(
                auth = authRepository,
                loyalty = loyaltyRepository,
                google = googleAuthManager,
                preferences = preferences,
                pushNotifications = pushNotificationRepository,
            ) as T
        }
    }
    val homeViewModelFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == HomeViewModel::class.java)
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(promotionRepository) as T
        }
    }
}
