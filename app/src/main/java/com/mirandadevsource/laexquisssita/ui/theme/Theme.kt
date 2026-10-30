package com.mirandadevsource.laexquisssita.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ExquisssitaColorScheme = lightColorScheme(
    primary = BrandyBrown,
    onPrimary = Color.White,
    secondary = ApricotIllusion,
    onSecondary = ChocolateFondant,
    tertiary = OfflineAmber,
    onTertiary = Color.White,
    background = VanillaCustard,
    onBackground = ChocolateFondant,
    surface = VanillaCustard,
    onSurface = ChocolateFondant,
    surfaceVariant = CreamSurface,
    onSurfaceVariant = ChocolateFondant,
    error = ErrorRed,
    onError = Color.White,
    outline = ApricotIllusion
)

@Composable
fun LaExquisssitaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Desactivado para mantener consistencia de marca Pantone
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ExquisssitaColorScheme,
        typography = Typography,
        content = content
    )
}