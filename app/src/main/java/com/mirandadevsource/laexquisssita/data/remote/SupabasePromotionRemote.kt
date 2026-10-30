package com.mirandadevsource.laexquisssita.data.remote

import com.mirandadevsource.laexquisssita.domain.model.Promotion
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class SupabasePromotionRemote(private val clientProvider: () -> SupabaseClient) : PromotionRemote {
    override suspend fun activePromotions(): List<Promotion> = clientProvider().postgrest["promociones"]
        .select(Columns.list(
            "id", "titulo", "descripcion", "terminos_condiciones", "imagen_url",
            "fecha_publicacion", "fecha_vencimiento", "activo"
        )) {
            filter { eq("activo", true) }
            order("fecha_publicacion", Order.DESCENDING)
            limit(MAX_PROMOTIONS)
        }.decodeList()

    private companion object { const val MAX_PROMOTIONS = 100L }
}
