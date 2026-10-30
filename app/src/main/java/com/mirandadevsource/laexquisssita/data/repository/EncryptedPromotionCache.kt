package com.mirandadevsource.laexquisssita.data.repository

import android.content.Context
import com.mirandadevsource.laexquisssita.core.security.EncryptedPreferences
import com.mirandadevsource.laexquisssita.domain.model.Promotion
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class EncryptedPromotionCache(context: Context) : PromotionCache {
    private val storage = EncryptedPreferences(context.applicationContext)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    override fun read(): List<Promotion>? = storage.get(CACHE_KEY)?.let { value ->
        runCatching { json.decodeFromString(ListSerializer(Promotion.serializer()), value) }
            .getOrElse { storage.remove(CACHE_KEY); null }
    }

    override fun write(promotions: List<Promotion>) {
        storage.put(CACHE_KEY, json.encodeToString(ListSerializer(Promotion.serializer()), promotions))
    }

    private companion object { const val CACHE_KEY = "public:promotions:v1" }
}
