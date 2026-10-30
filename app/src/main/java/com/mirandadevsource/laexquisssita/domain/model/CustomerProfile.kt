package com.mirandadevsource.laexquisssita.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Entidad de dominio que representa el perfil del cliente en La Exquisssita.
 */
@Serializable
data class CustomerProfile(
    @SerialName("id")
    val id: String,
    @SerialName("nombre")
    val nombre: String,
    @SerialName("telefono")
    val telefono: String? = null,
    @SerialName("puntos_lealtad")
    val puntosLealtad: Int = 0,
    @SerialName("visitas_totales")
    val visitasTotales: Int = 0
)
