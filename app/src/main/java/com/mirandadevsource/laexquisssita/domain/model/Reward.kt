package com.mirandadevsource.laexquisssita.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Entidad de dominio que representa una recompensa canjeable por puntos de lealtad.
 */
@Serializable
data class Reward(
    @SerialName("id")
    val id: String,
    @SerialName("titulo")
    val titulo: String,
    @SerialName("descripcion")
    val descripcion: String = "",
    @SerialName("costo_puntos")
    val costoPuntos: Int,
    @SerialName("activo")
    val activo: Boolean = true
)
