package com.mirandadevsource.laexquisssita.features.loyalty.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirandadevsource.laexquisssita.core.util.QRGenerator
import com.mirandadevsource.laexquisssita.domain.model.AuthState
import com.mirandadevsource.laexquisssita.domain.model.CustomerProfile
import com.mirandadevsource.laexquisssita.domain.model.CustomerRewardItem
import com.mirandadevsource.laexquisssita.features.profile.ui.ProfileErrorView
import com.mirandadevsource.laexquisssita.ui.theme.ApricotIllusion
import com.mirandadevsource.laexquisssita.ui.theme.BrandyBrown
import com.mirandadevsource.laexquisssita.ui.theme.ChocolateFondant
import com.mirandadevsource.laexquisssita.ui.theme.CreamSurface
import com.mirandadevsource.laexquisssita.ui.theme.ErrorRed
import com.mirandadevsource.laexquisssita.ui.theme.GoldAccent
import com.mirandadevsource.laexquisssita.ui.theme.OfflineAmber
import com.mirandadevsource.laexquisssita.ui.theme.TextMuted
import com.mirandadevsource.laexquisssita.ui.theme.WarmWhite
import com.mirandadevsource.laexquisssita.ui.theme.VanillaCustard
import com.mirandadevsource.laexquisssita.ui.theme.WarmWhite

@Composable
fun LoyaltyScreen(
    authState: AuthState,
    qrToken: String?,
    loadingCard: Boolean,
    isBusy: Boolean,
    autoMaxBrightnessEnabled: Boolean,
    onSignInWithGoogleClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onSaveToGalleryClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSignInClick: () -> Unit = onSignInWithGoogleClick,
    rewards: List<CustomerRewardItem> = emptyList(),
    currentPeriodLabel: String = "",
    loadingRewards: Boolean = false,
    rewardsError: String? = null
) {
    val context = LocalContext.current
    val isAuthenticated = authState is AuthState.Authenticated
    var selectedRewardForInfo by remember { mutableStateOf<CustomerRewardItem?>(null) }

    // Diálogo informativo sobre la mecánica de canje de recompensa
    selectedRewardForInfo?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedRewardForInfo = null },
            containerColor = WarmWhite,
            shape = RoundedCornerShape(18.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Stars,
                        contentDescription = null,
                        tint = BrandyBrown,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = item.reward.titulo,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ChocolateFondant
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (item.reward.descripcion.isNotBlank()) {
                        Text(
                            text = item.reward.descripcion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ChocolateFondant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CreamSurface,
                        border = BorderStroke(1.dp, ApricotIllusion)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Costo de canje:",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                            Text(
                                text = "${item.reward.costoPuntos} puntos",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = BrandyBrown
                            )
                        }
                    }

                    when {
                        item.alreadyRedeemedInPeriod -> {
                            Text(
                                text = "Esta recompensa ya fue canjeada en el semestre activo ($currentPeriodLabel). Podrás volver a canjearla en el siguiente periodo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        item.isAvailable -> {
                            Text(
                                text = "Para hacer válida tu recompensa, presenta tu código QR permanente al ordenar en caja. El personal validará tu tarjeta y descontará los puntos correspondientes.\n\nPolítica: 1 canje por cliente durante el periodo semestral ($currentPeriodLabel).",
                                style = MaterialTheme.typography.bodySmall,
                                color = ChocolateFondant
                            )
                        }
                        else -> {
                            Text(
                                text = "Actualmente te faltan ${item.pointsMissing} puntos para desbloquear esta recompensa durante este semestre.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedRewardForInfo = null },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandyBrown)
                ) {
                    Text("Entendido", color = WarmWhite)
                }
            }
        )
    }

    // Elevación automática de brillo al mostrar la tarjeta de lealtad
    if (isAuthenticated && autoMaxBrightnessEnabled) {
        DisposableEffect(Unit) {
            val activity = context as? Activity
            val window = activity?.window
            val previousBrightness = window?.attributes?.screenBrightness ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE

            window?.let {
                val layoutParams = it.attributes
                layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
                it.attributes = layoutParams
            }

            onDispose {
                window?.let {
                    val layoutParams = it.attributes
                    layoutParams.screenBrightness = previousBrightness
                    it.attributes = layoutParams
                }
            }
        }
    }

    if (authState is AuthState.Initializing || authState is AuthState.LoadingProfile) {
        Box(modifier = modifier.fillMaxSize().background(VanillaCustard), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BrandyBrown)
        }
        return
    }

    if (authState is AuthState.ProfileError) {
        ProfileErrorView(
            message = authState.message,
            onRetry = onRefreshClick,
            modifier = modifier
        )
        return
    }

    if (!isAuthenticated) {
        GuestLoyaltyPrompt(
            modifier = modifier,
            isBusy = isBusy,
            onSignInClick = onSignInClick,
            onSignInWithGoogleClick = onSignInWithGoogleClick
        )
        return
    }

    val authenticatedSession = authState as AuthState.Authenticated
    val profile = authenticatedSession.profile
    val isOffline = authenticatedSession.isOffline

    val qrBitmap = remember(qrToken) {
        qrToken?.takeIf { it.isNotBlank() }?.let {
            QRGenerator.generateQrImageBitmap(
                content = it,
                size = 512,
                foregroundColor = ChocolateFondant,
                backgroundColor = VanillaCustard
            )
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VanillaCustard),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Indicador de modo offline si aplica
        if (isOffline) {
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = OfflineAmber.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, OfflineAmber)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CloudOff,
                            contentDescription = null,
                            tint = OfflineAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Modo sin conexión. Datos guardados localmente.",
                            style = MaterialTheme.typography.labelSmall,
                            color = ChocolateFondant
                        )
                    }
                }
            }
        }

        // Tarjeta Digital de Socio Exquisssita
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ChocolateFondant),
                border = BorderStroke(1.dp, ApricotIllusion.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TARJETA DE SOCIO",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                            color = ApricotIllusion
                        )
                        Icon(
                            imageVector = Icons.Outlined.CreditCard,
                            contentDescription = null,
                            tint = ApricotIllusion,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = profile.nombre.ifBlank { "Cliente Distinguido" },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WarmWhite
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "PUNTOS DISPONIBLES",
                                style = MaterialTheme.typography.labelSmall,
                                color = ApricotIllusion
                            )
                            Text(
                                text = profile.puntosLealtad.toString(),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 36.sp
                                ),
                                color = GoldAccent
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "VISITAS TOTALES",
                                style = MaterialTheme.typography.labelSmall,
                                color = ApricotIllusion
                            )
                            Text(
                                text = profile.visitasTotales.toString(),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 36.sp
                                ),
                                color = WarmWhite
                            )
                        }
                    }
                }
            }
        }

        // Contenedor de Código QR Permanente
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WarmWhite),
                border = BorderStroke(1.dp, ApricotIllusion),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tu Código Permanente",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ChocolateFondant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Escanea en caja al realizar tu compra. Este código no caduca y funciona sin internet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Visor de Código QR Real
                    Surface(
                        modifier = Modifier.size(200.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = VanillaCustard,
                        border = BorderStroke(1.dp, ApricotIllusion)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (loadingCard) {
                                CircularProgressIndicator(
                                    color = BrandyBrown,
                                    modifier = Modifier.size(36.dp)
                                )
                            } else if (qrBitmap != null) {
                                Image(
                                    bitmap = qrBitmap,
                                    contentDescription = "Código QR de Cliente",
                                    modifier = Modifier.size(176.dp)
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.QrCode,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    OutlinedButton(
                                        onClick = onRefreshClick,
                                        border = BorderStroke(1.dp, BrandyBrown)
                                    ) {
                                        Text("Reintentar", color = BrandyBrown)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Acción de respaldo offline: Guardar en Galería
                    OutlinedButton(
                        onClick = onSaveToGalleryClick,
                        enabled = qrToken != null && !loadingCard,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BrandyBrown),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandyBrown),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Guardar en Galería",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }

        // Recompensas Semestrales del Club
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Stars,
                            contentDescription = null,
                            tint = BrandyBrown,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Recompensas del Club",
                            style = MaterialTheme.typography.titleLarge,
                            color = ChocolateFondant
                        )
                    }

                    if (currentPeriodLabel.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ApricotIllusion.copy(alpha = 0.35f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Event,
                                    contentDescription = null,
                                    tint = BrandyBrown,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = currentPeriodLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = BrandyBrown
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Acumula visitas y canjea 1 vez cada recompensa durante el periodo semestral activo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                // Estado de carga de recompensas
                if (loadingRewards && rewards.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = WarmWhite),
                        border = BorderStroke(1.dp, ApricotIllusion),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = BrandyBrown,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.size(12.dp))
                            Text(
                                text = "Consultando catálogo de recompensas...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }
                // Estado de error en recompensas
                else if (rewardsError != null && rewards.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = WarmWhite),
                        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = rewardsError,
                                style = MaterialTheme.typography.bodySmall,
                                color = ErrorRed,
                                textAlign = TextAlign.Center
                            )
                            OutlinedButton(
                                onClick = onRefreshClick,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, ApricotIllusion)
                            ) {
                                Text("Reintentar", color = BrandyBrown)
                            }
                        }
                    }
                }
                // Catálogo vacío
                else if (!loadingRewards && rewards.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = WarmWhite),
                        border = BorderStroke(1.dp, ApricotIllusion),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = "No hay recompensas activas registradas para este periodo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            modifier = Modifier.padding(20.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                // Lista de recompensas dinámicas
                else {
                    rewards.forEach { item ->
                        RewardItem(
                            item = item,
                            onClick = { selectedRewardForInfo = item }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GuestLoyaltyPrompt(
    isBusy: Boolean,
    onSignInClick: () -> Unit,
    onSignInWithGoogleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VanillaCustard)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = WarmWhite),
            border = BorderStroke(1.dp, ApricotIllusion),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = CreamSurface,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = BrandyBrown,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Text(
                    text = "Club La Exquisssita",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = ChocolateFondant,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Inicia sesión o regístrate para obtener tu tarjeta de socio permanente, acumular puntos en cada visita y canjear deliciosas recompensas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onSignInClick,
                    enabled = !isBusy,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandyBrown),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = "Iniciar Sesión o Registrarse",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = WarmWhite
                    )
                }

                OutlinedButton(
                    onClick = onSignInWithGoogleClick,
                    enabled = !isBusy,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ApricotIllusion),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = WarmWhite,
                        contentColor = ChocolateFondant
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = if (isBusy) "Conectando..." else "Continuar con Google",
                        style = MaterialTheme.typography.labelLarge,
                        color = ChocolateFondant
                    )
                }
            }
        }
    }
}

@Composable
fun RewardItem(
    item: CustomerRewardItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = WarmWhite),
        border = BorderStroke(
            1.dp,
            if (item.isAvailable) OfflineAmber.copy(alpha = 0.6f) else ApricotIllusion
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = item.reward.titulo,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ChocolateFondant
                )
                if (item.reward.descripcion.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.reward.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        maxLines = 2
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    when {
                        item.alreadyRedeemedInPeriod -> {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Canjeada este semestre (1 de 1)",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                        item.isAvailable -> {
                            Icon(
                                imageVector = Icons.Outlined.Stars,
                                contentDescription = null,
                                tint = OfflineAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Disponible para canje en caja",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = OfflineAmber
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Te faltan ${item.pointsMissing} pts",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when {
                    item.alreadyRedeemedInPeriod -> CreamSurface
                    item.isAvailable -> OfflineAmber
                    else -> CreamSurface
                }
            ) {
                Text(
                    text = "${item.reward.costoPuntos} pts",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = when {
                        item.alreadyRedeemedInPeriod -> TextMuted
                        item.isAvailable -> WarmWhite
                        else -> ChocolateFondant
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
