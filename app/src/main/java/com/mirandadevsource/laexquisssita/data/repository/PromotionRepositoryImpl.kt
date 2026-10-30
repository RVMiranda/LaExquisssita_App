package com.mirandadevsource.laexquisssita.data.repository

import com.mirandadevsource.laexquisssita.core.util.operationResult
import com.mirandadevsource.laexquisssita.data.remote.PromotionRemote
import com.mirandadevsource.laexquisssita.domain.model.Promotion
import com.mirandadevsource.laexquisssita.domain.repository.PromotionFeed
import com.mirandadevsource.laexquisssita.domain.repository.PromotionRepository
import java.time.LocalDate
import kotlinx.coroutines.CancellationException

class PromotionRepositoryImpl(
    private val remote: PromotionRemote,
    private val cache: PromotionCache,
    private val today: () -> LocalDate = LocalDate::now,
) : PromotionRepository {
    override suspend fun getActivePromotions(): Result<PromotionFeed> = operationResult {
        val promotions = try {
            remote.activePromotions().filter { it.isValidOn(today()) }
                .sortedByDescending(Promotion::fechaPublicacion)
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            val cached = cache.read() ?: throw error
            cached.filter { it.isValidOn(today()) }
                .sortedByDescending(Promotion::fechaPublicacion)
                .let { return@operationResult PromotionFeed(it, isOffline = true) }
        }
        // Cache failures must not turn a successful server response into an offline/error state.
        runCatching { cache.write(promotions) }
        PromotionFeed(promotions, isOffline = false)
    }
}
