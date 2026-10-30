package com.mirandadevsource.laexquisssita.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mirandadevsource.laexquisssita.ui.theme.ApricotIllusion
import com.mirandadevsource.laexquisssita.ui.theme.BrandyBrown
import com.mirandadevsource.laexquisssita.ui.theme.ChocolateFondant
import com.mirandadevsource.laexquisssita.ui.theme.CreamSurface
import com.mirandadevsource.laexquisssita.ui.theme.GoldAccent
import com.mirandadevsource.laexquisssita.ui.theme.WarmWhite

enum class BadgeVariant {
    Brand,      // Fondo BrandyBrown, texto blanco
    Warm,       // Fondo CreamSurface, texto ChocolateFondant
    Gold,       // Fondo GoldAccent, texto blanco
    Outline     // Fondo transparente, borde ApricotIllusion
}

@Composable
fun ExquisssitaBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: BadgeVariant = BadgeVariant.Warm,
    icon: ImageVector? = null
) {
    val (bgColor, textColor, borderColor) = when (variant) {
        BadgeVariant.Brand -> Triple(BrandyBrown, WarmWhite, BrandyBrown)
        BadgeVariant.Warm -> Triple(CreamSurface, ChocolateFondant, ApricotIllusion)
        BadgeVariant.Gold -> Triple(GoldAccent, WarmWhite, GoldAccent)
        BadgeVariant.Outline -> Triple(Color.Transparent, ChocolateFondant, ApricotIllusion)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 0.dp,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier
                        .size(12.dp)
                        .padding(end = 4.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = textColor
            )
        }
    }
}
