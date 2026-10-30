package com.mirandadevsource.laexquisssita.core.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mirandadevsource.laexquisssita.LaExquisssitaApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LaExquisssitaFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val app = applicationContext as? LaExquisssitaApp ?: return
        serviceScope.launch {
            app.container.pushNotificationRepository.syncToken(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val notification = remoteMessage.notification
        val data = remoteMessage.data

        val title = notification?.title ?: data["title"]
        val body = notification?.body ?: data["body"]

        NotificationHelper.showNotification(
            context = applicationContext,
            title = title,
            body = body,
            data = data,
        )
    }
}
