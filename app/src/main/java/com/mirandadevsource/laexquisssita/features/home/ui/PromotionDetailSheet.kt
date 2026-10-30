package com.mirandadevsource.laexquisssita.features.home.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mirandadevsource.laexquisssita.core.designsystem.BadgeVariant
import com.mirandadevsource.laexquisssita.core.designsystem.ExquisssitaBadge
import com.mirandadevsource.laexquisssita.ui.theme.ApricotIllusion
import com.mirandadevsource.laexquisssita.ui.theme.BrandyBrown
import com.mirandadevsource.laexquisssita.ui.theme.ChocolateFondant
import com.mirandadevsource.laexquisssita.ui.theme.CreamSurface
import com.mirandadevsource.laexquisssita.ui.theme.TextMuted
import com.mirandadevsource.laexquisssita.ui.theme.WarmWhite

/**
 * Hoja modal inferior para ver detalles completos, términos y vigencia de una promoción.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionDetailSheet(
    title: String,
    description: String,
    validity: String?,
    terms: String?,
    imageUrl: String?,
    onDismiss: () -> Unit,
    onNavigateToCard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = WarmWhite,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Imagen si existe
            if (!imageUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CreamSurface)
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Encabezado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Campaign,
                    contentDescription = null,
                    tint = BrandyBrown,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "PROMOCIÓN EXCLUSIVA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = BrandyBrown
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = ChocolateFondant
            )

            // Vigencia
            validity?.takeIf { it.isNotBlank() }?.let { validText ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Vigencia: $validText",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = ChocolateFondant
                    )
                }
            }

            // Descripción
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = ChocolateFondant
            )

            // Términos y condiciones
            terms?.takeIf { it.isNotBlank() }?.let { termsText ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CreamSurface.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, ApricotIllusion),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Términos y Condiciones",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ChocolateFondant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = termsText,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ApricotIllusion),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Cerrar",
                        color = ChocolateFondant,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Button(
                    onClick = {
                        onDismiss()
                        onNavigateToCard()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandyBrown),
                    modifier = Modifier.weight(1.3f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.QrCodeScanner,
                        contentDescription = null,
                        tint = WarmWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Ver Mi Tarjeta",
                        color = WarmWhite,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
