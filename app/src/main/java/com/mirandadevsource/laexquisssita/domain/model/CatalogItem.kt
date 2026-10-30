package com.mirandadevsource.laexquisssita.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Domain shape reserved for a future catalog source; the customer feed currently has no catalog API. */
@Serializable
data class CatalogItem(
    val id: String,
    val nombre: String,
    @SerialName("precio_centavos") val precioCentavos: Long,
    val categoria: String,
    val descripcion: String? = null,
    @SerialName("imagen_url") val imagenUrl: String? = null,
    val activo: Boolean,
)
