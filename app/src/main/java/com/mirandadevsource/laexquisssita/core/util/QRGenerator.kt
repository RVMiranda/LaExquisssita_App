package com.mirandadevsource.laexquisssita.core.util

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Generador nativo de código QR de alta resolución usando ZXing Core.
 */
object QRGenerator {

    fun generateQrBitmap(
        content: String,
        size: Int = 512,
        foregroundColor: Color = Color(0xFF3D2723), // ChocolateFondant
        backgroundColor: Color = Color(0xFFFAF3E0)  // VanillaCustard
    ): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                EncodeHintType.MARGIN to 1
            )
            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)
            val fg = foregroundColor.toArgb()
            val bg = backgroundColor.toArgb()

            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) fg else bg
                }
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    fun generateQrImageBitmap(
        content: String,
        size: Int = 512,
        foregroundColor: Color = Color(0xFF3D2723),
        backgroundColor: Color = Color(0xFFFAF3E0)
    ): ImageBitmap? {
        return generateQrBitmap(content, size, foregroundColor, backgroundColor)?.asImageBitmap()
    }
}
