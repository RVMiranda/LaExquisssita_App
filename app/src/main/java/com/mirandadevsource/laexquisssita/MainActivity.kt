package com.mirandadevsource.laexquisssita

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.mirandadevsource.laexquisssita.core.notifications.NotificationHelper
import com.mirandadevsource.laexquisssita.navigation.Screen
import com.mirandadevsource.laexquisssita.ui.MainScreen
import com.mirandadevsource.laexquisssita.ui.theme.LaExquisssitaTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val targetTab = MutableStateFlow<String?>(null)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            lifecycleScope.launch {
                (application as? LaExquisssitaApp)?.container?.pushNotificationRepository?.syncToken()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannel(this)
        requestNotificationPermissionIfNeeded()
        handleIntent(intent)

        lifecycleScope.launch {
            (application as? LaExquisssitaApp)?.container?.pushNotificationRepository?.syncToken()
        }

        setContent {
            val destination by targetTab.collectAsStateWithLifecycle()
            LaExquisssitaTheme {
                MainScreen(
                    targetTab = destination,
                    onTargetTabConsumed = { targetTab.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val extras = intent?.extras ?: return
        val type = extras.getString("type")
        val destination = extras.getString("destination")
        if (type == "promotion" || destination == "promotions") {
            targetTab.value = Screen.Promociones.route
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    LaExquisssitaTheme {
        MainScreen()
    }
}