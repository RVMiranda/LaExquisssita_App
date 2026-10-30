package com.mirandadevsource.laexquisssita.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class Promotion(
    val id: String,
    val titulo: String,
    val descripcion: String,
    @SerialName("terminos_condiciones") val terminosCondiciones: String? = null,
    @SerialName("imagen_url") val imagenUrl: String? = null,
    @SerialName("fecha_publicacion") val fechaPublicacion: String,
    @SerialName("fecha_vencimiento") val fechaVencimiento: String? = null,
    val activo: Boolean,
) {
    fun isValidOn(date: LocalDate = LocalDate.now()): Boolean {
        if (!activo) return false
        val rawExpiration = fechaVencimiento?.takeIf(String::isNotBlank) ?: return true
        val expiration = runCatching { LocalDate.parse(rawExpiration.take(10)) }.getOrNull() ?: return false
        return !expiration.isBefore(date)
    }
}
