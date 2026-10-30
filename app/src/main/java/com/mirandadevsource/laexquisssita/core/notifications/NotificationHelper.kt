package com.mirandadevsource.laexquisssita.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mirandadevsource.laexquisssita.MainActivity
import com.mirandadevsource.laexquisssita.R
import java.util.concurrent.atomic.AtomicInteger

object NotificationHelper {
    const val CHANNEL_ID = "canal_la_exquisssita"
    private const val CHANNEL_NAME = "Club La Exquisssita"
    private const val CHANNEL_DESCRIPTION = "Alertas de puntos acumulados, visitas y novedades del club"
    private val notificationCounter = AtomicInteger(1001)

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        context: Context,
        title: String?,
        body: String?,
        data: Map<String, String> = emptyMap(),
    ) {
        createNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val requestCode = notificationCounter.getAndIncrement()
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val cleanTitle = title?.trim()?.ifEmpty { null } ?: context.getString(R.string.app_name)
        val cleanBody = body?.trim()?.ifEmpty { null } ?: "Tienes una actualización de tu cuenta de socio."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setColor(ContextCompat.getColor(context, R.color.chocolate_fondant))
            .setContentTitle(cleanTitle)
            .setContentText(cleanBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(cleanBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(requestCode, notification)
    }
}
