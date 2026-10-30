package com.mirandadevsource.laexquisssita.data.remote

import com.mirandadevsource.laexquisssita.domain.model.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.*

@kotlinx.serialization.Serializable
private data class RewardRedemption(
    @kotlinx.serialization.SerialName("recompensa_id") val rewardId: String,
)

class SupabaseCustomerRemote(private val clientProvider: () -> SupabaseClient) : CustomerRemote {
    private val client get() = clientProvider()
    private fun parameters(name: String, phone: String?) = buildJsonObject {
        val input = ProfileInput.validated(name, phone)
        put("p_nombre", input.name)
        put("p_telefono", input.phone?.let(::JsonPrimitive) ?: JsonNull)
    }

    override suspend fun ensureProfile(name: String, phone: String?): CustomerProfile =
        client.postgrest.rpc("ensure_customer_profile", parameters(name, phone)).decodeAs()

    override suspend fun updateProfile(name: String, phone: String?): CustomerProfile =
        client.postgrest.rpc("update_customer_profile", parameters(name, phone)).decodeAs()

    override suspend fun recoverQr(): CustomerQr =
        client.postgrest.rpc("get_or_create_customer_qr_token").decodeAs()

    override suspend fun rewards(): List<Reward> = client.postgrest["recompensas"].select(
        Columns.list("id", "titulo", "descripcion", "costo_puntos", "activo")
    ) {
        filter { eq("activo", true) }
        order("costo_puntos", Order.ASCENDING)
        limit(100)
    }.decodeList()

    override suspend fun redeemedRewardIds(startInclusive: String, endExclusive: String): Set<String> =
        client.postgrest["canjes_recompensas"].select(Columns.list("recompensa_id")) {
            filter {
                gte("created_at", startInclusive)
                lt("created_at", endExclusive)
            }
            order("created_at", Order.DESCENDING)
            limit(1000)
        }.decodeList<RewardRedemption>().mapTo(mutableSetOf()) { it.rewardId }

    override suspend fun history(customerId: String): List<PointMovement> =
        client.postgrest["puntos_movimientos"].select(
            Columns.list("id", "puntos_delta", "tipo", "motivo", "created_at")
        ) {
            filter { eq("cliente_id", customerId) }
            order("created_at", Order.DESCENDING)
            order("id", Order.DESCENDING)
            limit(50)
        }.decodeList()
}
