package com.mirandadevsource.laexquisssita.data.remote

import com.mirandadevsource.laexquisssita.domain.model.Promotion

interface PromotionRemote {
    suspend fun activePromotions(): List<Promotion>
}
