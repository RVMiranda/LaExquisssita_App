package com.mirandadevsource.laexquisssita.features.home.ui

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mirandadevsource.laexquisssita.core.designsystem.BadgeVariant
import com.mirandadevsource.laexquisssita.core.designsystem.ExquisssitaBadge
import com.mirandadevsource.laexquisssita.features.home.viewmodel.HomeUiState
import com.mirandadevsource.laexquisssita.ui.theme.ApricotIllusion
import com.mirandadevsource.laexquisssita.ui.theme.BrandyBrown
import com.mirandadevsource.laexquisssita.ui.theme.ChocolateFondant
import com.mirandadevsource.laexquisssita.ui.theme.CreamSurface
import com.mirandadevsource.laexquisssita.ui.theme.ErrorRed
import com.mirandadevsource.laexquisssita.ui.theme.OfflineAmber
import com.mirandadevsource.laexquisssita.ui.theme.TextMuted
import com.mirandadevsource.laexquisssita.ui.theme.VanillaCustard
import com.mirandadevsource.laexquisssita.ui.theme.WarmWhite

/**
 * Pantalla de pestaña dedicada para explorar el catálogo completo de promociones vigentes.
 * Permite una visualización amplia y sin recortes de cada oferta disponible.
 */
@Composable
fun PromotionsScreen(
    uiState: HomeUiState,
    onRefresh: () -> Unit,
    onNavigateToLoyalty: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPromotion by remember { mutableStateOf<PromotionItemUi?>(null) }
    val promotionItems = remember(uiState.promotions) {
        uiState.promotions.map { it.toUi() }
    }

    // Modal de Detalle al tocar "Ver términos" o la tarjeta
    selectedPromotion?.let { promo ->
        PromotionDetailSheet(
            title = promo.title,
            description = promo.description,
            validity = promo.validity,
            terms = promo.terms,
            imageUrl = promo.imageUrl,
            onDismiss = { selectedPromotion = null },
            onNavigateToCard = {
                selectedPromotion = null
                onNavigateToLoyalty()
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VanillaCustard),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Modo sin conexión
        if (uiState.isOffline) {
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
                            text = "Modo sin conexión. Mostrando promociones guardadas.",
                            style = MaterialTheme.typography.labelSmall,
                            color = ChocolateFondant
                        )
                    }
                }
            }
        }

        // Encabezado de la pestaña
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ApricotIllusion.copy(alpha = 0.35f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.LocalOffer,
                                contentDescription = null,
                                tint = BrandyBrown,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Catálogo de Promociones",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = ChocolateFondant
                        )
                        Text(
                            text = "${promotionItems.size} promociones vigentes",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }

                if (uiState.isRefreshing) {
                    CircularProgressIndicator(
                        color = BrandyBrown,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Actualizar promociones",
                            tint = BrandyBrown,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Estado: Cargando
        if (uiState.isLoading && !uiState.hasLoaded) {
            item {
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
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = BrandyBrown, modifier = Modifier.size(32.dp))
                        Text(
                            text = "Cargando promociones disponibles...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
            }
        }
        // Estado: Error sin datos
        else if (uiState.errorMessage != null && promotionItems.isEmpty()) {
            item {
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
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "No se pudieron obtener las promociones",
                            style = MaterialTheme.typography.titleMedium,
                            color = ChocolateFondant
                        )
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onRefresh,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandyBrown)
                        ) {
                            Text("Reintentar", color = WarmWhite)
                        }
                    }
                }
            }
        }
        // Estado: Sin promociones
        else if (uiState.hasLoaded && promotionItems.isEmpty()) {
            item {
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
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Campaign,
                            contentDescription = null,
                            tint = BrandyBrown,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "No hay promociones activas por el momento",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ChocolateFondant
                        )
                        Text(
                            text = "Consulta más tarde o visita nuestra sucursal para disfrutar de las mejores carnitas de la región.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        // Lista completa de tarjetas de promociones
        else {
            items(promotionItems) { promo ->
                FullPromotionCard(
                    promotion = promo,
                    onCardClick = { selectedPromotion = promo },
                    onNavigateToLoyalty = onNavigateToLoyalty
                )
            }
        }
    }
}

/**
 * Tarjeta completa y espaciosa de una promoción en el catálogo general.
 */
@Composable
fun FullPromotionCard(
    promotion: PromotionItemUi,
    onCardClick: () -> Unit,
    onNavigateToLoyalty: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WarmWhite),
        border = BorderStroke(1.dp, ApricotIllusion),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Imagen de la promoción si existe
            if (!promotion.imageUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CreamSurface)
                ) {
                    AsyncImage(
                        model = promotion.imageUrl,
                        contentDescription = promotion.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Fila de etiquetas superiores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExquisssitaBadge(
                    text = promotion.tag,
                    variant = BadgeVariant.Brand
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CreamSurface
                ) {
                    Text(
                        text = promotion.validity,
                        style = MaterialTheme.typography.labelSmall,
                        color = ChocolateFondant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Título
            Text(
                text = promotion.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = ChocolateFondant
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Descripción completa
            Text(
                text = promotion.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )

            // Términos y condiciones destacados si existen
            if (!promotion.terms.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ApricotIllusion.copy(alpha = 0.2f),
                    border = BorderStroke(0.5.dp, ApricotIllusion)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Condiciones: ${promotion.terms}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ChocolateFondant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Botón de acción para presentar en caja
            OutlinedButton(
                onClick = onNavigateToLoyalty,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BrandyBrown),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.QrCodeScanner,
                    contentDescription = null,
                    tint = BrandyBrown,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Ver mi Código para Canjear en Caja",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = BrandyBrown
                )
            }
        }
    }
}
