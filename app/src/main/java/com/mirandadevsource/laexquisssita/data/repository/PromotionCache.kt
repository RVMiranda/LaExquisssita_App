package com.mirandadevsource.laexquisssita.data.repository

import com.mirandadevsource.laexquisssita.domain.model.Promotion

interface PromotionCache {
    fun read(): List<Promotion>?
    fun write(promotions: List<Promotion>)
}
