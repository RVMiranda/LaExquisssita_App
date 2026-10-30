package com.mirandadevsource.laexquisssita.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mirandadevsource.laexquisssita.ui.theme.ApricotIllusion
import com.mirandadevsource.laexquisssita.ui.theme.BrandyBrown
import com.mirandadevsource.laexquisssita.ui.theme.ChocolateFondant
import com.mirandadevsource.laexquisssita.ui.theme.WarmWhite

/**
 * Chip de selección de categoría plano sin elevación, alineado a la paleta Pantone.
 */
@Composable
fun CategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) BrandyBrown else WarmWhite,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) BrandyBrown else ApricotIllusion
        ),
        shadowElevation = 0.dp,
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) WarmWhite else ChocolateFondant
            )
        }
    }
}
