package com.mirandadevsource.laexquisssita.core.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.OutputStream

/**
 * Utilidad para exportar el código QR directamente a la Galería del dispositivo
 * compatible con Android 10+ (Scoped Storage) sin requerir permisos invasivos.
 */
object GallerySaver {

    fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        filename: String = "LaExquisssita_QR_${System.currentTimeMillis()}"
    ): Result<String> {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$filename.png")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LaExquisssita")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return Result.failure(IllegalStateException("No se pudo crear la entrada en la galería"))

            val outputStream: OutputStream? = resolver.openOutputStream(uri)
            outputStream?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            Result.success("Código guardado en tu galería de fotos.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
