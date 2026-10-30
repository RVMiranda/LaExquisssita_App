package com.mirandadevsource.laexquisssita.core.security

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CustomerPreferences(context: Context) : PushTokenStorage {
    private val prefs = context.getSharedPreferences("customer_settings", Context.MODE_PRIVATE)
    private val brightness = MutableStateFlow(prefs.getBoolean("max_brightness", true))
    val autoMaxBrightness = brightness.asStateFlow()
    fun setAutoMaxBrightness(enabled: Boolean) {
        prefs.edit().putBoolean("max_brightness", enabled).apply()
        brightness.value = enabled
    }

    override fun getFcmToken(): String? = prefs.getString("fcm_token", null)
    override fun setFcmToken(token: String?) {
        prefs.edit().putString("fcm_token", token).apply()
    }

    fun isPhonePromptDismissedPermanently(userId: String): Boolean =
        prefs.getBoolean("dismiss_phone_prompt_$userId", false)

    fun dismissPhonePromptPermanently(userId: String) {
        prefs.edit().putBoolean("dismiss_phone_prompt_$userId", true).apply()
    }
}
