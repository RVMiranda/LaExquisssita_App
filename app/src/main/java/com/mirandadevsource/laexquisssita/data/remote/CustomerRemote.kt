package com.mirandadevsource.laexquisssita.data.remote

import com.mirandadevsource.laexquisssita.domain.model.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CustomerQr(
    @SerialName("cliente_id") val customerId: String,
    @SerialName("qr_token") val token: String,
)

interface CustomerRemote {
    suspend fun ensureProfile(name: String, phone: String?): CustomerProfile
    suspend fun updateProfile(name: String, phone: String?): CustomerProfile
    suspend fun recoverQr(): CustomerQr
    suspend fun rewards(): List<Reward>
    suspend fun redeemedRewardIds(startInclusive: String, endExclusive: String): Set<String>
    suspend fun history(customerId: String): List<PointMovement>
}
