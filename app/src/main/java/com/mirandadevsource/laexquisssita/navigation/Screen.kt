package com.mirandadevsource.laexquisssita.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Destinos principales de navegación de la barra inferior (Bottom Navigation Bar)
 * para la aplicación de clientes La Exquisssita.
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Inicio : Screen(
        route = "inicio",
        title = "Inicio",
        icon = Icons.Outlined.Home
    )

    data object Promociones : Screen(
        route = "promociones",
        title = "Promociones",
        icon = Icons.Outlined.LocalOffer
    )

    data object Lealtad : Screen(
        route = "lealtad",
        title = "Mi Tarjeta",
        icon = Icons.Outlined.QrCodeScanner
    )

    data object Perfil : Screen(
        route = "perfil",
        title = "Perfil",
        icon = Icons.Outlined.AccountCircle
    )

    // Alias para compatibilidad hacia atrás
    val Novedades = Inicio

    companion object {
        val bottomNavItems: List<Screen> by lazy {
            listOf(Inicio, Promociones, Lealtad, Perfil)
        }
    }
}
