package com.mirandadevsource.laexquisssita.data.repository

import com.mirandadevsource.laexquisssita.data.remote.PromotionRemote
import com.mirandadevsource.laexquisssita.domain.model.Promotion
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromotionRepositoryTest {
    private val today = LocalDate.parse("2026-10-19")

    @Test fun `remote feed filters expired and inactive then caches ordered result`() = runBlocking {
        val cache = FakeCache()
        val repository = PromotionRepositoryImpl(
            remote = FakeRemote(listOf(promo("older", "2026-10-01"), promo("expired", "2026-09-01", expiration = "2026-10-18"), promo("inactive", "2026-10-03", active = false), promo("newer", "2026-10-10"))),
            cache = cache,
            today = { today },
        )

        val feed = repository.getActivePromotions().getOrThrow()

        assertFalse(feed.isOffline)
        assertEquals(listOf("newer", "older"), feed.promotions.map(Promotion::id))
        assertEquals(feed.promotions, cache.promotions)
    }

    @Test fun `cached feed is fallback filtered again against today's validity`() = runBlocking {
        val cache = FakeCache(listOf(promo("valid", "2026-10-10"), promo("stale", "2026-10-11", expiration = "2026-10-18")))
        val repository = PromotionRepositoryImpl(FakeRemote(error = IllegalStateException("offline")), cache) { today }

        val feed = repository.getActivePromotions().getOrThrow()

        assertTrue(feed.isOffline)
        assertEquals(listOf("valid"), feed.promotions.map(Promotion::id))
    }

    @Test fun `remote cancellation is not converted to cached success`() = runBlocking {
        val repository = PromotionRepositoryImpl(FakeRemote(error = CancellationException("cancelled")), FakeCache(listOf(promo("cached", "2026-10-10")))) { today }

        try {
            repository.getActivePromotions()
            throw AssertionError("Expected cancellation")
        } catch (_: CancellationException) { }
    }

    @Test fun `promotion serializes using backend column names`() {
        val value = promo("p1", "2026-10-10", expiration = "2026-10-31")
        val serializer = ListSerializer(Promotion.serializer())
        val encoded = Json.encodeToString(serializer, listOf(value))
        val decoded = Json.decodeFromString(serializer, encoded).single()

        assertEquals(value, decoded)
        assertTrue(encoded.contains("\"terminos_condiciones\""))
        assertTrue(encoded.contains("\"fecha_vencimiento\""))
    }

    private fun promo(
        id: String,
        published: String,
        expiration: String? = null,
        active: Boolean = true,
    ) = Promotion(id, id, "Descripción", "Términos", null, "${published}T10:00:00Z", expiration, active)

    private class FakeCache(initial: List<Promotion>? = null) : PromotionCache {
        var promotions: List<Promotion>? = initial
        override fun read() = promotions
        override fun write(promotions: List<Promotion>) { this.promotions = promotions }
    }

    private class FakeRemote(
        private val result: List<Promotion> = emptyList(),
        private val error: Exception? = null,
    ) : PromotionRemote {
        override suspend fun activePromotions(): List<Promotion> = error?.let { throw it } ?: result
    }
}
