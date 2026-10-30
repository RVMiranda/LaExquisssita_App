package com.mirandadevsource.laexquisssita.features.profile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BrightnessHigh
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirandadevsource.laexquisssita.features.auth.ui.LegalDialog
import com.mirandadevsource.laexquisssita.features.auth.ui.LegalDocumentType
import com.mirandadevsource.laexquisssita.ui.theme.ApricotIllusion
import com.mirandadevsource.laexquisssita.ui.theme.BrandyBrown
import com.mirandadevsource.laexquisssita.ui.theme.ChocolateFondant
import com.mirandadevsource.laexquisssita.ui.theme.ErrorRed
import com.mirandadevsource.laexquisssita.ui.theme.TextMuted
import com.mirandadevsource.laexquisssita.ui.theme.VanillaCustard
import com.mirandadevsource.laexquisssita.ui.theme.WarmWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    autoBrightness: Boolean,
    onAutoBrightnessChange: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAuthenticated: Boolean = false,
    onDeleteAccount: () -> Unit = {},
    onSaveQrToGallery: () -> Unit = {},
    hasQrToken: Boolean = false
) {
    var notifyVisits by rememberSaveable { mutableStateOf(true) }
    var notifyPromotions by rememberSaveable { mutableStateOf(true) }
    var activeLegalDocument by remember { mutableStateOf<LegalDocumentType?>(null) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    // Modal de visualización de documentos legales
    activeLegalDocument?.let { docType ->
        LegalDialog(
            type = docType,
            onDismiss = { activeLegalDocument = null }
        )
    }

    // Diálogo de confirmación para eliminación de cuenta
    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = {
                Text(
                    text = "Eliminar Cuenta de Socio",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ErrorRed
                )
            },
            text = {
                Text(
                    text = "Esta acción es definitiva e irreversible. Tus datos personales serán anonimizados conforme al Aviso de Privacidad, tu saldo de puntos acumulados se dará de baja y este correo electrónico no podrá volver a registrarse en La Exquisssita.\n\n¿Estás seguro de que deseas continuar?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ChocolateFondant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmationDialog = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Sí, Eliminar Cuenta", color = WarmWhite)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirmationDialog = false },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ApricotIllusion)
                ) {
                    Text("Cancelar", color = BrandyBrown)
                }
            },
            containerColor = WarmWhite,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VanillaCustard,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Configuración",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BrandyBrown
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Regresar",
                            tint = BrandyBrown
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = VanillaCustard),
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = ApricotIllusion,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección: Visualización
            item {
                Text(
                    text = "VISUALIZACIÓN Y ESCANEO",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                    color = TextMuted,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmWhite),
                    border = BorderStroke(1.dp, ApricotIllusion),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.BrightnessHigh,
                                contentDescription = null,
                                tint = BrandyBrown,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Brillo máximo en Mi Tarjeta",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ChocolateFondant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Aumenta la luz de pantalla automáticamente al mostrar el QR en caja.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                            }
                        }

                        Switch(
                            checked = autoBrightness,
                            onCheckedChange = onAutoBrightnessChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = WarmWhite,
                                checkedTrackColor = BrandyBrown,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = ApricotIllusion.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }

            // Sección: Notificaciones del Club (Sugerencia de implementación)
            item {
                Text(
                    text = "NOTIFICACIONES DEL CLUB",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                    color = TextMuted,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp, top = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmWhite),
                    border = BorderStroke(1.dp, ApricotIllusion),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Toggle Visitas y Puntos
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = null,
                                    tint = BrandyBrown,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Visitas y Puntos",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = ChocolateFondant
                                    )
                                    Text(
                                        text = "Aviso inmediato al escanear tu QR en caja y acumular saldo.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMuted
                                    )
                                }
                            }

                            Switch(
                                checked = notifyVisits,
                                onCheckedChange = { notifyVisits = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = WarmWhite,
                                    checkedTrackColor = BrandyBrown,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = ApricotIllusion.copy(alpha = 0.4f)
                                )
                            )
                        }

                        // Toggle Promociones
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Campaign,
                                    contentDescription = null,
                                    tint = BrandyBrown,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Nuevas Promociones",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = ChocolateFondant
                                    )
                                    Text(
                                        text = "Alertas cuando se publiquen ofertas especiales o combos.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMuted
                                    )
                                }
                            }

                            Switch(
                                checked = notifyPromotions,
                                onCheckedChange = { notifyPromotions = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = WarmWhite,
                                    checkedTrackColor = BrandyBrown,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = ApricotIllusion.copy(alpha = 0.4f)
                                )
                            )
                        }
                    }
                }
            }

            // Sección: Legal y Transparencia
            item {
                Text(
                    text = "LEGAL Y TRANSPARENCIA",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                    color = TextMuted,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp, top = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmWhite),
                    border = BorderStroke(1.dp, ApricotIllusion),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeLegalDocument = LegalDocumentType.PRIVACY_POLICY }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = BrandyBrown,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Aviso de Privacidad",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ChocolateFondant
                                )
                                Text(
                                    text = "Tus datos personales están protegidos y resguardados.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeLegalDocument = LegalDocumentType.TERMS_AND_CONDITIONS }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                tint = BrandyBrown,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Términos del Club de Lealtad",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ChocolateFondant
                                )
                                Text(
                                    text = "Reglas de acumulación de visitas y canje de beneficios semestrales.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            // Sección: Información
            item {
                Text(
                    text = "ACERCA DE LA APLICACIÓN",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                    color = TextMuted,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp, top = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmWhite),
                    border = BorderStroke(1.dp, ApricotIllusion),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = BrandyBrown,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Versión 1.0.0",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ChocolateFondant
                                )
                                Text(
                                    text = "La Exquisssita — Club de Lealtad y Clientes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            // Sección: Gestión de Cuenta y Privacidad (Solo si está autenticado)
            if (isAuthenticated) {
                item {
                    Text(
                        text = "GESTIÓN DE CUENTA Y PRIVACIDAD",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                        color = ErrorRed,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp, top = 8.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = WarmWhite),
                        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.35f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = null,
                                    tint = ErrorRed,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Eliminar Cuenta de Socio",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = ErrorRed
                                    )
                                    Text(
                                        text = "Solicita la baja definitiva de tu perfil y la cancelación irreversible de tu tarjeta y puntos.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMuted
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { showDeleteConfirmationDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, ErrorRed),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = null,
                                    tint = ErrorRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = "Eliminar mi Cuenta Permanentemente",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
