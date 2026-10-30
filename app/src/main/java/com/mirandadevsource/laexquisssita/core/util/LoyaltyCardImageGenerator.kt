package com.mirandadevsource.laexquisssita.core.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.ui.graphics.toArgb
import com.mirandadevsource.laexquisssita.ui.theme.ApricotIllusion
import com.mirandadevsource.laexquisssita.ui.theme.ChocolateFondant
import com.mirandadevsource.laexquisssita.ui.theme.VanillaCustard
import com.mirandadevsource.laexquisssita.ui.theme.WarmWhite

/**
 * Generador nativo de la imagen oficial de la Tarjeta de Socio para respaldo en Galería.
 *
 * Renderiza la identidad visual de la tarjeta (colores Chocolate Fondant, Vanilla Custard y
 * Apricot Illusion) con el nombre del cliente y su código QR permanente.
 * Excluye intencionalmente el saldo de puntos y visitas para evitar confusiones si la
 * imagen guardada en la galería se consulta después de nuevos movimientos en caja.
 */
object LoyaltyCardImageGenerator {

    fun generateCardBitmap(
        customerName: String,
        qrToken: String,
    ): Bitmap? {
        if (qrToken.isBlank()) return null

        val cardWidth = 800
        val cardHeight = 1100
        val cornerRadius = 36f

        val bitmap = Bitmap.createBitmap(cardWidth, cardHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Fondo de la tarjeta (Chocolate Fondant)
        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ChocolateFondant.toArgb()
            style = Paint.Style.FILL
        }
        val cardRect = RectF(0f, 0f, cardWidth.toFloat(), cardHeight.toFloat())
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, backgroundPaint)

        // Borde decorativo
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ApricotIllusion.toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRoundRect(
            RectF(2f, 2f, cardWidth.toFloat() - 2f, cardHeight.toFloat() - 2f),
            cornerRadius,
            cornerRadius,
            borderPaint
        )

        // 2. Encabezado "CLUB LA EXQUISSSITA"
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ApricotIllusion.toArgb()
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.15f
        }
        canvas.drawText("CLUB LA EXQUISSSITA", cardWidth / 2f, 70f, headerPaint)

        // Subtítulo "TARJETA DE SOCIO"
        val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = VanillaCustard.toArgb()
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.2f
        }
        canvas.drawText("TARJETA DE SOCIO", cardWidth / 2f, 105f, subHeaderPaint)

        // Línea divisoria decorativa
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ApricotIllusion.toArgb()
            alpha = 100
            strokeWidth = 2f
        }
        canvas.drawLine(80f, 130f, cardWidth - 80f, 130f, dividerPaint)

        // 3. Nombre del socio
        val cleanName = customerName.trim().ifEmpty { "Cliente Distinguido" }
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = WarmWhite.toArgb()
            textSize = if (cleanName.length > 25) 30f else 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(cleanName, cardWidth / 2f, 185f, namePaint)

        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ApricotIllusion.toArgb()
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Socio Activo", cardWidth / 2f, 215f, badgePaint)

        // 4. Contenedor del Código QR (Vanilla Custard)
        val qrBoxMargin = 70f
        val qrBoxTop = 250f
        val qrBoxBottom = 880f
        val qrBoxRect = RectF(qrBoxMargin, qrBoxTop, cardWidth - qrBoxMargin, qrBoxBottom)
        val qrBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = VanillaCustard.toArgb()
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(qrBoxRect, 24f, 24f, qrBoxPaint)

        val qrBoxBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ApricotIllusion.toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(qrBoxRect, 24f, 24f, qrBoxBorderPaint)

        // 5. Código QR permanente
        val qrSize = 540
        val qrBitmap = QRGenerator.generateQrBitmap(
            content = qrToken,
            size = qrSize,
            foregroundColor = ChocolateFondant,
            backgroundColor = VanillaCustard
        )

        if (qrBitmap != null) {
            val qrLeft = (cardWidth - qrSize) / 2f
            val qrTop = qrBoxTop + (qrBoxBottom - qrBoxTop - qrSize) / 2f
            canvas.drawBitmap(qrBitmap, qrLeft, qrTop, null)
        }

        // 6. Pie de tarjeta
        val footerTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = VanillaCustard.toArgb()
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Código Permanente", cardWidth / 2f, 935f, footerTitlePaint)

        val footerDescPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ApricotIllusion.toArgb()
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Presenta este código en caja al realizar tu compra.", cardWidth / 2f, 970f, footerDescPaint)
        canvas.drawText("Este código no caduca y funciona sin conexión a internet.", cardWidth / 2f, 995f, footerDescPaint)

        val brandingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ApricotIllusion.toArgb()
            alpha = 160
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Carnitas al Estilo Michoacán La Exquisssita", cardWidth / 2f, 1050f, brandingPaint)

        return bitmap
    }
}
