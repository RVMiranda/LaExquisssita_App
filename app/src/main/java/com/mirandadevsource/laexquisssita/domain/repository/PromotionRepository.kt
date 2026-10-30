package com.mirandadevsource.laexquisssita.domain.repository

import com.mirandadevsource.laexquisssita.domain.model.Promotion

data class PromotionFeed(
    val promotions: List<Promotion>,
    val isOffline: Boolean,
)

interface PromotionRepository {
    suspend fun getActivePromotions(): Result<PromotionFeed>
}
