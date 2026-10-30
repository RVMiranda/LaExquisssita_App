package com.mirandadevsource.laexquisssita.features.profile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Loyalty
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mirandadevsource.laexquisssita.domain.model.AuthState
import com.mirandadevsource.laexquisssita.domain.model.PointMovement
import com.mirandadevsource.laexquisssita.domain.model.PointMovementKind
import com.mirandadevsource.laexquisssita.domain.model.displayDescription
import com.mirandadevsource.laexquisssita.domain.model.kind
import com.mirandadevsource.laexquisssita.ui.theme.ApricotIllusion
import com.mirandadevsource.laexquisssita.ui.theme.BrandyBrown
import com.mirandadevsource.laexquisssita.ui.theme.ChocolateFondant
import com.mirandadevsource.laexquisssita.ui.theme.CreamSurface
import com.mirandadevsource.laexquisssita.ui.theme.ErrorRed
import com.mirandadevsource.laexquisssita.ui.theme.OfflineAmber
import com.mirandadevsource.laexquisssita.ui.theme.TextMuted
import com.mirandadevsource.laexquisssita.ui.theme.VanillaCustard
import com.mirandadevsource.laexquisssita.ui.theme.WarmWhite

@Composable
fun ProfileScreen(
    authState: AuthState,
    isBusy: Boolean,
    onSaveProfile: (String, String?) -> Unit,
    onSignOut: () -> Unit,
    onSettingsClick: () -> Unit,
    onSignInWithGoogleClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSignInClick: () -> Unit = onSignInWithGoogleClick,
    pointsHistory: List<PointMovement> = emptyList(),
    loadingHistory: Boolean = false,
    historyError: String? = null
) {
    if (authState is AuthState.Initializing || authState is AuthState.LoadingProfile) {
        Box(modifier = modifier.fillMaxSize().background(VanillaCustard), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BrandyBrown)
        }
        return
    }

    if (authState is AuthState.Guest) {
        GuestProfilePrompt(
            modifier = modifier,
            isBusy = isBusy,
            onSignInClick = onSignInClick,
            onSignInWithGoogleClick = onSignInWithGoogleClick
        )
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

    val authenticated = authState as AuthState.Authenticated
    val profile = authenticated.profile

    var isEditing by remember { mutableStateOf(false) }
    var nombreInput by remember { mutableStateOf(profile.nombre) }
    var telefonoInput by remember { mutableStateOf(profile.telefono ?: "") }

    LaunchedEffect(profile) {
        if (!isEditing) {
            nombreInput = profile.nombre
            telefonoInput = profile.telefono ?: ""
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VanillaCustard),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tarjeta Unificada de Perfil e Información Personal
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WarmWhite),
                border = BorderStroke(1.dp, ApricotIllusion),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Cabecera superior con Logo/Avatar + Identidad + Botón Editar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = CreamSurface,
                                border = BorderStroke(1.dp, ApricotIllusion),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = null,
                                        tint = BrandyBrown,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = profile.nombre.ifBlank { "Cliente Distinguido" },
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ChocolateFondant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Socio La Exquisssita",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { isEditing = !isEditing },
                            enabled = !isBusy,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, ApricotIllusion),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                tint = BrandyBrown,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = if (isEditing) "Cancelar" else "Editar",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrandyBrown
                            )
                        }
                    }

                    if (isEditing) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = nombreInput,
                                onValueChange = { nombreInput = it },
                                label = { Text("Nombre completo") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandyBrown,
                                    focusedLabelColor = BrandyBrown
                                )
                            )

                            OutlinedTextField(
                                value = telefonoInput,
                                onValueChange = { newValue ->
                                    if (newValue.length <= 10 && newValue.all { it.isDigit() }) {
                                        telefonoInput = newValue
                                    }
                                },
                                label = { Text("Teléfono celular (10 dígitos)") },
                                placeholder = { Text("Ej. 9191234567") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandyBrown,
                                    focusedLabelColor = BrandyBrown
                                )
                            )

                            Button(
                                onClick = {
                                    val trimmedName = nombreInput.trim()
                                    val trimmedPhone = telefonoInput.trim().takeIf { it.isNotBlank() }
                                    if (trimmedName.isNotEmpty()) {
                                        onSaveProfile(trimmedName, trimmedPhone)
                                        isEditing = false
                                    }
                                },
                                enabled = !isBusy && nombreInput.isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandyBrown),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (isBusy) "Guardando..." else "Guardar Cambios",
                                    color = WarmWhite
                                )
                            }
                        }
                    } else {
                        // Lista limpia de datos personales
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ProfileInfoRow(
                                icon = Icons.Outlined.Person,
                                label = "Nombre",
                                value = profile.nombre
                            )
                            ProfileInfoRow(
                                icon = Icons.Outlined.Email,
                                label = "Correo",
                                value = authenticated.email?.ifBlank { "No disponible" } ?: "No disponible"
                            )
                            ProfileInfoRow(
                                icon = Icons.Outlined.Phone,
                                label = "Teléfono",
                                value = profile.telefono ?: "No registrado"
                            )
                        }
                    }
                }
            }
        }

        // Acceso a Configuración
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSettingsClick() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WarmWhite),
                border = BorderStroke(1.dp, ApricotIllusion),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = null,
                            tint = BrandyBrown,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Configuración",
                                style = MaterialTheme.typography.titleMedium,
                                color = ChocolateFondant
                            )
                            Text(
                                text = "Brillo de pantalla y preferencias",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = "Ir a configuración",
                        tint = TextMuted
                    )
                }
            }
        }

        // Historial Reciente de Puntos y Visitas
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
                            imageVector = Icons.Outlined.History,
                            contentDescription = null,
                            tint = BrandyBrown,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Historial Reciente",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ChocolateFondant
                        )
                    }

                    if (pointsHistory.isNotEmpty()) {
                        Text(
                            text = "${pointsHistory.size} movimientos",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                // Estado de carga de historial
                if (loadingHistory && pointsHistory.isEmpty()) {
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
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = BrandyBrown,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.size(10.dp))
                            Text(
                                text = "Consultando historial de visitas...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }
                // Estado de error en historial
                else if (historyError != null && pointsHistory.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
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
                                text = historyError,
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
                // Historial vacío
                else if (!loadingHistory && pointsHistory.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = WarmWhite),
                        border = BorderStroke(1.dp, ApricotIllusion),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Sin movimientos registrados aún",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = ChocolateFondant
                            )
                            Text(
                                text = "Tus visitas y consumos registrados en caja acumularán puntos automáticamente.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }
                // Lista de movimientos
                else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        pointsHistory.forEach { movement ->
                            PointMovementCard(movement = movement)
                        }
                    }
                }
            }
        }

        // Botón de Cierre de Sesión
        item {
            OutlinedButton(
                onClick = onSignOut,
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Cerrar Sesión",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
fun ProfileErrorView(
    message: String,
    onRetry: () -> Unit,
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
            border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Error al sincronizar perfil",
                    style = MaterialTheme.typography.titleLarge,
                    color = ChocolateFondant
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandyBrown)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Reintentar")
                }
            }
        }
    }
}

@Composable
fun GuestProfilePrompt(
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
                    text = "Tu Perfil",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = ChocolateFondant,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Inicia sesión o crea tu cuenta para consultar tu historial, actualizar tus datos de contacto y gestionar tus preferencias.",
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
fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BrandyBrown,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = ChocolateFondant
            )
        }
    }
}

/**
 * Tarjeta individual para representar un movimiento en el ledger de puntos y visitas.
 */
@Composable
fun PointMovementCard(
    movement: PointMovement,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = WarmWhite),
        border = BorderStroke(1.dp, ApricotIllusion),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (movement.kind) {
                        PointMovementKind.VISIT -> ApricotIllusion.copy(alpha = 0.35f)
                        PointMovementKind.REDEMPTION -> ErrorRed.copy(alpha = 0.15f)
                        PointMovementKind.OPENING -> OfflineAmber.copy(alpha = 0.2f)
                        PointMovementKind.ADJUSTMENT -> CreamSurface
                        PointMovementKind.OTHER -> CreamSurface
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (movement.kind) {
                                PointMovementKind.VISIT -> Icons.Outlined.Storefront
                                PointMovementKind.REDEMPTION -> Icons.Outlined.CardGiftcard
                                PointMovementKind.OPENING -> Icons.Outlined.Loyalty
                                PointMovementKind.ADJUSTMENT -> Icons.Outlined.Tune
                                PointMovementKind.OTHER -> Icons.Outlined.ReceiptLong
                            },
                            contentDescription = null,
                            tint = when (movement.kind) {
                                PointMovementKind.VISIT -> BrandyBrown
                                PointMovementKind.REDEMPTION -> ErrorRed
                                PointMovementKind.OPENING -> OfflineAmber
                                PointMovementKind.ADJUSTMENT -> ChocolateFondant
                                PointMovementKind.OTHER -> ChocolateFondant
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = movement.displayDescription,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = ChocolateFondant
                    )
                    Text(
                        text = movement.createdAt.take(10),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = when {
                    movement.puntosDelta > 0 -> OfflineAmber.copy(alpha = 0.15f)
                    movement.puntosDelta < 0 -> ErrorRed.copy(alpha = 0.15f)
                    else -> CreamSurface
                }
            ) {
                Text(
                    text = when {
                        movement.puntosDelta > 0 -> "+${movement.puntosDelta} pts"
                        movement.puntosDelta < 0 -> "${movement.puntosDelta} pts"
                        else -> "0 pts"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = when {
                        movement.puntosDelta > 0 -> BrandyBrown
                        movement.puntosDelta < 0 -> ErrorRed
                        else -> TextMuted
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
