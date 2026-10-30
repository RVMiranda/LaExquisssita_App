package com.mirandadevsource.laexquisssita.data.repository

import com.mirandadevsource.laexquisssita.data.auth.CustomerIdentity
import com.mirandadevsource.laexquisssita.data.remote.CustomerQr
import com.mirandadevsource.laexquisssita.data.remote.CustomerRemote
import com.mirandadevsource.laexquisssita.domain.model.*
import com.mirandadevsource.laexquisssita.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class LoyaltyRepositoryTest {
    @Test fun `calendar semester boundaries and labels are stable`() {
        assertEquals(
            RewardPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 7, 1)),
            RewardPeriod.forDate(LocalDate.of(2026, 6, 30)),
        )
        assertEquals("Periodo Ene - Jun 2026", RewardPeriod.forDate(LocalDate.of(2026, 1, 1)).label)
        assertEquals(
            RewardPeriod(LocalDate.of(2026, 7, 1), LocalDate.of(2027, 1, 1)),
            RewardPeriod.forDate(LocalDate.of(2026, 12, 31)),
        )
        assertEquals("Periodo Jul - Dic 2026", RewardPeriod.forDate(LocalDate.of(2026, 7, 1)).label)
        assertEquals("Periodo Ene - Jun 2027", RewardPeriod.forDate(LocalDate.of(2027, 1, 1)).label)
        assertEquals(
            "2026-07-01T06:00:00Z",
            RewardPeriod.forDate(LocalDate.of(2026, 7, 1)).startTimestamp(RewardPeriod.businessZone),
        )
    }

    @Test fun `reward items reflect period redemption and points shortfall`() = runBlocking {
        val remote = FakeCustomerRemote().apply {
            rewards = listOf(
                Reward("redeemed", "Café", costoPuntos = 30),
                Reward("short", "Postre", costoPuntos = 50),
                Reward("available", "Taco", costoPuntos = 20),
            )
            redeemed = setOf("redeemed")
        }
        val result = repository(remote, LocalDate.of(2026, 10, 22)).getActiveRewards(35).getOrThrow()

        assertTrue(result.single { it.reward.id == "redeemed" }.alreadyRedeemedInPeriod)
        assertFalse(result.single { it.reward.id == "redeemed" }.isAvailable)
        assertEquals(15, result.single { it.reward.id == "short" }.pointsMissing)
        assertTrue(result.single { it.reward.id == "available" }.isAvailable)
        assertEquals("2026-07-01T00:00:00Z", remote.periodStart)
        assertEquals("2027-01-01T00:00:00Z", remote.periodEnd)
    }

    @Test fun `points history is newest first and exposes friendly movement labels`() = runBlocking {
        val remote = FakeCustomerRemote().apply {
            movements = listOf(
                PointMovement("1", 10, "visita", "Visita acreditada", "2026-01-01T10:00:00Z"),
                PointMovement("3", -50, "canje", "Canje: Café Americano", "2026-02-01T10:00:00Z"),
                PointMovement("2", 10, "ajuste", null, "2026-01-15T10:00:00Z"),
            )
        }
        val result = repository(remote, LocalDate.of(2026, 2, 1)).getPointsHistory().getOrThrow()

        assertEquals(listOf("3", "2", "1"), result.map { it.id })
        assertEquals("Visita registrada en caja", result.last().displayDescription)
        assertEquals("Canje: Café Americano", result.first().displayDescription)
        assertEquals(PointMovementKind.ADJUSTMENT, result[1].kind)
    }

    private fun repository(remote: FakeCustomerRemote, date: LocalDate) = LoyaltyRepositoryImpl(
        remote = remote,
        cache = FakeCustomerCache(),
        auth = FakeAuthRepository(),
        today = { date },
        zoneId = { ZoneId.of("UTC") },
    )
}

private class FakeCustomerCache : CustomerCache {
    private val profile = CustomerProfile("customer-1", "Test", puntosLealtad = 35)
    override fun profile(userId: String) = profile.takeIf { userId == AUTH_USER }
    override fun saveProfile(userId: String, profile: CustomerProfile) = Unit
    override fun qr(userId: String): String? = null
    override fun saveQr(userId: String, token: String) = Unit
    override fun clearCustomerData() = Unit
}

private class FakeAuthRepository : AuthRepository {
    private val user = CustomerIdentity(AUTH_USER, "Test", "test@example.invalid")
    override val state: StateFlow<AuthState> = MutableStateFlow(
        AuthState.Authenticated(AUTH_USER, CustomerProfile("customer-1", "Test", puntosLealtad = 35)),
    )
    override val currentUserId = AUTH_USER
    override val sessionVersion = 1L
    override suspend fun signInWithGoogleIdToken(idToken: String, nonce: String) = Result.success(Unit)
    override suspend fun signInWithEmail(email: String, password: String) = Result.success(Unit)
    override suspend fun signUpWithEmail(email: String, password: String, nombre: String, telefono: String?) =
        Result.success(CustomerProfile("customer-1", nombre, telefono))
    override suspend fun refreshProfile() = Result.success(CustomerProfile("customer-1", user.name))
    override suspend fun updateProfile(nombre: String, telefono: String?) = Result.success(CustomerProfile("customer-1", nombre, telefono))
    override suspend fun deleteAccount() = Result.success(Unit)
    override suspend fun signOut() = Result.success(Unit)
}

private class FakeCustomerRemote : CustomerRemote {
    var rewards: List<Reward> = emptyList()
    var redeemed: Set<String> = emptySet()
    var movements: List<PointMovement> = emptyList()
    var periodStart = ""
    var periodEnd = ""
    override suspend fun ensureProfile(name: String, phone: String?) = CustomerProfile("customer-1", name, phone)
    override suspend fun updateProfile(name: String, phone: String?) = ensureProfile(name, phone)
    override suspend fun recoverQr() = CustomerQr("customer-1", "a".repeat(64))
    override suspend fun rewards() = rewards
    override suspend fun redeemedRewardIds(startInclusive: String, endExclusive: String): Set<String> {
        periodStart = startInclusive
        periodEnd = endExclusive
        return redeemed
    }
    override suspend fun history(customerId: String) = movements
}

private const val AUTH_USER = "auth-user-1"
