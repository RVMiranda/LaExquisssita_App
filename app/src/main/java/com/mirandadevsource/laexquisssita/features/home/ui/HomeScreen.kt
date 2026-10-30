package com.mirandadevsource.laexquisssita.features.home.ui

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Storefront
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.mirandadevsource.laexquisssita.core.designsystem.BadgeVariant
import com.mirandadevsource.laexquisssita.core.designsystem.ExquisssitaBadge
import com.mirandadevsource.laexquisssita.domain.model.Promotion
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
 * Modelo de presentación UI desacoplado para promociones.
 */
data class PromotionItemUi(
    val id: String,
    val title: String,
    val description: String,
    val validity: String,
    val terms: String? = null,
    val imageUrl: String? = null,
    val tag: String = "Promoción"
)

/**
 * Convierte la entidad Promotion del dominio a PromotionItemUi según el contrato de Codex.
 */
fun Promotion.toUi(): PromotionItemUi {
    val formattedValidity = fechaVencimiento?.takeIf { it.isNotBlank() }?.let { raw ->
        "Válido hasta: ${raw.take(10)}"
    } ?: "Vigencia sin fecha límite"

    return PromotionItemUi(
        id = id,
        title = titulo,
        description = descripcion,
        validity = formattedValidity,
        terms = terminosCondiciones,
        imageUrl = imagenUrl,
        tag = "Promoción"
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToLoyalty: () -> Unit = {},
    onNavigateToPromotions: () -> Unit = {}
) {
    var selectedPromotion by remember { mutableStateOf<PromotionItemUi?>(null) }
    val promotionItems = remember(uiState.promotions) {
        uiState.promotions.map { it.toUi() }
    }

    // Modal de Detalle de Promoción
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
        // Indicador de Modo Sin Conexión
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

        // Banner de Bienvenida
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandyBrown),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Campaign,
                            contentDescription = null,
                            tint = ApricotIllusion,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Carnitas al estilo Michoacán",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = ApricotIllusion
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tradición y sabor desde 1998",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WarmWhite
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Consulta las promociones exclusivas y presenta tu código en caja para acumular visitas y recompensas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ApricotIllusion
                    )
                }
            }
        }

        // Banner de Contacto y Pedidos Directos por WhatsApp
        item {
            WhatsAppContactBanner()
        }

        // Mini-sección 1: Información de la Sucursal
        item {
            BusinessInfoCard()
        }

        // Mini-sección 2: Sitio Web Oficial
        item {
            WebsiteBannerCard()
        }
    }
}

/**
 * Banner destacado para ordenar pedidos para llevar o resolver dudas directamente por WhatsApp.
 */
@Composable
fun WhatsAppContactBanner(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WarmWhite),
        border = BorderStroke(1.dp, ApricotIllusion),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ApricotIllusion.copy(alpha = 0.35f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Chat,
                            contentDescription = null,
                            tint = BrandyBrown,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Pedidos y Atención Directa",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ChocolateFondant
                    )
                    Text(
                        text = "Servicio de órdenes para llevar o dudas",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            Text(
                text = "¿Se te antojan unas ricas carnitas? Escríbenos directamente por WhatsApp para hacer tu pedido para llevar o resolver cualquier duda sobre nuestro menú.",
                style = MaterialTheme.typography.bodyMedium,
                color = ChocolateFondant
            )

            Button(
                onClick = {
                    val message = "Hola La Exquisssita, quisiera hacer un pedido para llevar o consultar información."
                    val encodedMessage = Uri.encode(message)
                    val uri = Uri.parse("https://wa.me/529196732035?text=$encodedMessage")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandyBrown),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Chat,
                    contentDescription = null,
                    tint = WarmWhite,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Enviar Mensaje por WhatsApp",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = WarmWhite
                )
            }
        }
    }
}

/**
 * Mini-sección informativa con la ubicación, horarios y contacto de la sucursal.
 */
@Composable
fun BusinessInfoCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WarmWhite),
        border = BorderStroke(1.dp, ApricotIllusion),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                            imageVector = Icons.Outlined.Storefront,
                            contentDescription = null,
                            tint = BrandyBrown,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Nuestra Única Sucursal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ChocolateFondant
                    )
                    Text(
                        text = "Ubicada en: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Place,
                        contentDescription = null,
                        tint = BrandyBrown,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Column {
                        Text(
                            text = "Periferico Oriente Sur s/n, Barrio Aeropuerto",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ChocolateFondant
                        )
                        Text(
                            text = "Ocosingo, Chiapas",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = BrandyBrown,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Column {
                        Text(
                            text = "Horario de Servicio",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ChocolateFondant
                        )
                        Text(
                            text = "Lunes a Sabado: 8:00 hrs – 16:00 hrs \nDomingo: 8:00 hrs - 15:00 hrs",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:9196732035")
                    }
                    try {
                        context.startActivity(dialIntent)
                    } catch (_: Exception) {}
                },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BrandyBrown),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Phone,
                    contentDescription = null,
                    tint = BrandyBrown,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Llamar a Sucursal: (919) 673-2035",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = BrandyBrown
                )
            }
        }
    }
}

/**
 * Mini-sección banner hacia el sitio web oficial del negocio.
 */
@Composable
fun WebsiteBannerCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ChocolateFondant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = VanillaCustard.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Language,
                            contentDescription = null,
                            tint = VanillaCustard,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Sitio Web Oficial",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = VanillaCustard
                    )
                    Text(
                        text = "www.laexquisssita.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = ApricotIllusion
                    )
                }
            }

            Text(
                text = "Conoce más de nuestra historia y tradición del mejor sabor de carnitas en Ocosingo",
                style = MaterialTheme.typography.bodySmall,
                color = VanillaCustard.copy(alpha = 0.85f)
            )

            Button(
                onClick = {
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://laexquisssita.com"))
                    try {
                        context.startActivity(webIntent)
                    } catch (_: Exception) {}
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandyBrown),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Visitar Sitio Web",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = WarmWhite
                )
                Spacer(modifier = Modifier.size(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = null,
                    tint = WarmWhite,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun PromotionCarouselCard(
    promotion: PromotionItemUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasImage = !promotion.imageUrl.isNullOrBlank()
    val cardHeight = if (hasImage) 330.dp else 225.dp

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = WarmWhite),
        border = BorderStroke(1.dp, ApricotIllusion),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .width(280.dp)
            .height(cardHeight)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Imagen si existe
            if (hasImage) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CreamSurface)
                ) {
                    AsyncImage(
                        model = promotion.imageUrl,
                        contentDescription = promotion.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExquisssitaBadge(
                    text = promotion.tag,
                    variant = BadgeVariant.Brand
                )
                Text(
                    text = "Ver detalles",
                    style = MaterialTheme.typography.labelSmall,
                    color = BrandyBrown
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = promotion.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = ChocolateFondant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = promotion.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.weight(1f))

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
    }
}

@Composable
fun PromotionListCard(
    promotion: PromotionItemUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = WarmWhite),
        border = BorderStroke(1.dp, ApricotIllusion),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = promotion.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ChocolateFondant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = promotion.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = promotion.validity,
                    style = MaterialTheme.typography.labelSmall,
                    color = BrandyBrown
                )
            }

            if (!promotion.imageUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CreamSurface)
                ) {
                    AsyncImage(
                        model = promotion.imageUrl,
                        contentDescription = promotion.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
