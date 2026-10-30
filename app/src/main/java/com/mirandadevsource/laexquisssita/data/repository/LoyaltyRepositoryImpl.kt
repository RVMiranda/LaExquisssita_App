package com.mirandadevsource.laexquisssita.data.repository

import com.mirandadevsource.laexquisssita.core.util.operationResult
import com.mirandadevsource.laexquisssita.data.remote.CustomerRemote
import com.mirandadevsource.laexquisssita.domain.model.*
import com.mirandadevsource.laexquisssita.domain.repository.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class LoyaltyRepositoryImpl(
    private val remote: CustomerRemote,
    private val cache: CustomerCache,
    private val auth: AuthRepository,
    private val today: () -> LocalDate = LocalDate::now,
    private val zoneId: () -> ZoneId = { RewardPeriod.businessZone },
) : LoyaltyRepository {
    private val qrMutex = Mutex()

    override suspend fun getPermanentQrToken(): Result<String> = qrMutex.withLock {
        operationResult {
            val owner = checkNotNull(auth.currentUserId)
            val version = auth.sessionVersion
            val profile = checkNotNull(cache.profile(owner))
            cache.qr(owner)?.let { return@operationResult it }
            val qr = remote.recoverQr()
            synchronized(cache) {
                check(auth.currentUserId == owner && auth.sessionVersion == version) { "Session changed" }
                check(qr.customerId == profile.id && qr.token.matches(Regex("[0-9a-f]{64}")))
                cache.saveQr(owner, qr.token)
            }
            qr.token
        }
    }

    override suspend fun getCustomerProfile(): Result<CustomerProfile> = auth.refreshProfile()

    override suspend fun updateCustomerProfile(nombre: String, telefono: String?) =
        auth.updateProfile(nombre, telefono)

    override suspend fun getActiveRewards(customerPoints: Int) = operationResult {
        val owner = checkNotNull(auth.currentUserId)
        val sessionVersion = auth.sessionVersion
        val zone = zoneId()
        val period = RewardPeriod.forDate(today())
        val redeemed = remote.redeemedRewardIds(
            period.startTimestamp(zone),
            period.endTimestamp(zone),
        )
        val rewards = remote.rewards()
        check(auth.currentUserId == owner && auth.sessionVersion == sessionVersion) { "Session changed" }
        rewards.map { reward ->
            CustomerRewardItem(
                reward = reward,
                alreadyRedeemedInPeriod = reward.id in redeemed,
                pointsMissing = (reward.costoPuntos - customerPoints).coerceAtLeast(0),
            )
        }
    }

    override suspend fun getPointsHistory() = operationResult {
        val owner = checkNotNull(auth.currentUserId)
        val sessionVersion = auth.sessionVersion
        val profile = checkNotNull(cache.profile(owner))
        remote.history(profile.id)
            .sortedWith(compareByDescending<PointMovement> { runCatching { Instant.parse(it.createdAt) }.getOrDefault(Instant.MIN) }
                .thenByDescending { it.id })
            .also { check(auth.currentUserId == owner && auth.sessionVersion == sessionVersion) { "Session changed" } }
    }
}

