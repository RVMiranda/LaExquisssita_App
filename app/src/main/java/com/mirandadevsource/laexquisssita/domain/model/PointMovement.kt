package com.mirandadevsource.laexquisssita.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Movimiento en el ledger de puntos del cliente.
 */
@Serializable
data class PointMovement(
    @SerialName("id")
    val id: String,
    @SerialName("puntos_delta")
    val puntosDelta: Int,
    @SerialName("tipo")
    val tipo: String, // 'apertura', 'visita', 'canje', 'ajuste'
    @SerialName("motivo")
    val motivo: String? = null,
    @SerialName("created_at")
    val createdAt: String
)

enum class PointMovementKind { OPENING, VISIT, REDEMPTION, ADJUSTMENT, OTHER }

val PointMovement.kind: PointMovementKind
    get() = when (tipo.lowercase()) {
        "apertura" -> PointMovementKind.OPENING
        "visita" -> PointMovementKind.VISIT
        "canje" -> PointMovementKind.REDEMPTION
        "ajuste" -> PointMovementKind.ADJUSTMENT
        else -> PointMovementKind.OTHER
    }

/** Human-readable label for the client activity feed. */
val PointMovement.displayDescription: String
    get() = when (kind) {
        PointMovementKind.OPENING -> "Saldo inicial"
        PointMovementKind.VISIT -> "Visita registrada en caja"
        PointMovementKind.REDEMPTION -> motivo?.takeIf { it.startsWith("Canje:", ignoreCase = true) }
            ?: "Canje de recompensa"
        PointMovementKind.ADJUSTMENT -> motivo?.takeIf(String::isNotBlank)?.let { "Ajuste: $it" }
            ?: "Ajuste de puntos"
        PointMovementKind.OTHER -> "Movimiento de puntos"
    }
