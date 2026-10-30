package com.mirandadevsource.laexquisssita.ui

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mirandadevsource.laexquisssita.LaExquisssitaApp
import com.mirandadevsource.laexquisssita.core.util.GallerySaver
import com.mirandadevsource.laexquisssita.core.util.LoyaltyCardImageGenerator
import com.mirandadevsource.laexquisssita.core.util.QRGenerator
import com.mirandadevsource.laexquisssita.domain.model.AuthState
import com.mirandadevsource.laexquisssita.features.auth.ui.AuthBottomSheet
import com.mirandadevsource.laexquisssita.features.auth.ui.PhoneOnboardingSheet
import com.mirandadevsource.laexquisssita.features.auth.viewmodel.CustomerViewModel
import com.mirandadevsource.laexquisssita.features.home.ui.HomeScreen
import com.mirandadevsource.laexquisssita.features.home.ui.PromotionsScreen
import com.mirandadevsource.laexquisssita.features.home.viewmodel.HomeViewModel
import com.mirandadevsource.laexquisssita.features.loyalty.ui.LoyaltyScreen
import com.mirandadevsource.laexquisssita.features.profile.ui.ProfileScreen
import com.mirandadevsource.laexquisssita.features.profile.ui.SettingsScreen
import com.mirandadevsource.laexquisssita.navigation.Screen
import com.mirandadevsource.laexquisssita.ui.theme.ApricotIllusion
import com.mirandadevsource.laexquisssita.ui.theme.BrandyBrown
import com.mirandadevsource.laexquisssita.ui.theme.ChocolateFondant
import com.mirandadevsource.laexquisssita.ui.theme.TextMuted
import com.mirandadevsource.laexquisssita.ui.theme.VanillaCustard
import com.mirandadevsource.laexquisssita.ui.theme.WarmWhite
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    targetTab: String? = null,
    onTargetTabConsumed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val app = context.applicationContext as LaExquisssitaApp
    val customer: CustomerViewModel = viewModel(factory = app.container.customerViewModelFactory)
    val home: HomeViewModel = viewModel(factory = app.container.homeViewModelFactory)

    val session by customer.authState.collectAsStateWithLifecycle()
    val card by customer.state.collectAsStateWithLifecycle()
    val brightness by customer.autoMaxBrightness.collectAsStateWithLifecycle()
    val homeState by home.state.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableStateOf(Screen.Inicio.route) }
    var isSettingsOpen by rememberSaveable { mutableStateOf(false) }
    var isAuthSheetOpen by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(targetTab) {
        if (!targetTab.isNullOrBlank()) {
            selectedTab = targetTab
            isSettingsOpen = false
            onTargetTabConsumed()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Cerrar el sheet de autenticación si se detecta sesión activa o error de sincronización
    LaunchedEffect(session) {
        when (val s = session) {
            is AuthState.Authenticated -> {
                isAuthSheetOpen = false
            }
            is AuthState.ProfileError -> {
                isAuthSheetOpen = false
                snackbarHostState.showSnackbar(s.message)
            }
            else -> {}
        }
    }

    // Mostrar mensajes del ViewModel en Snackbar
    LaunchedEffect(card.message) {
        card.message?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            customer.clearMessage()
        }
    }

    LaunchedEffect(card.authError) {
        card.authError?.let { err ->
            snackbarHostState.showSnackbar(err)
        }
    }

    LaunchedEffect(homeState.errorMessage) {
        homeState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            home.clearError()
        }
    }

    // Modal no intrusivo de Onboarding de teléfono tras Google Sign-In
    if (card.showPhoneOnboarding && session is AuthState.Authenticated) {
        val currentProfile = (session as AuthState.Authenticated).profile
        PhoneOnboardingSheet(
            isBusy = card.busy,
            onSavePhone = { phone -> customer.saveProfile(currentProfile.nombre, phone) },
            onSkip = { customer.skipPhone() },
            onDismissPermanently = { customer.dismissPhonePromptPermanently() }
        )
    }

    // Modal Bottom Sheet de Autenticación Híbrida (Login y Registro Manual + Google)
    if (isAuthSheetOpen) {
        AuthBottomSheet(
            isAuthenticating = card.isAuthenticating || session is AuthState.LoadingProfile,
            authError = card.authError,
            infoMessage = card.message,
            onClearAuthError = { customer.clearAuthError() },
            onSignInWithEmail = { email, password ->
                customer.signInWithEmail(email, password)
            },
            onSignUpWithEmail = { email, password, name, phone ->
                customer.signUpWithEmail(email, password, name, phone)
            },
            onSignInWithGoogle = {
                activity?.let { customer.signIn(it) }
            },
            onDismiss = {
                isAuthSheetOpen = false
                customer.clearAuthError()
            }
        )
    }

    val saveQrToGalleryAction: () -> Unit = {
        card.qrToken?.let { token ->
            val customerName = (session as? AuthState.Authenticated)?.profile?.nombre ?: "Cliente Distinguido"
            val bitmap = LoyaltyCardImageGenerator.generateCardBitmap(
                customerName = customerName,
                qrToken = token,
            )
            if (bitmap != null) {
                val result = GallerySaver.saveBitmapToGallery(
                    context = context,
                    bitmap = bitmap,
                    filename = "LaExquisssita_Tarjeta_${System.currentTimeMillis()}"
                )
                scope.launch {
                    snackbarHostState.showSnackbar(result.getOrDefault("Tarjeta de socio guardada en tu galería de fotos."))
                }
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("No se pudo generar la tarjeta de socio.")
                }
            }
        } ?: run {
            scope.launch {
                snackbarHostState.showSnackbar("El código QR aún no está disponible.")
            }
        }
    }

    if (isSettingsOpen) {
        SettingsScreen(
            autoBrightness = brightness,
            onAutoBrightnessChange = { customer.setAutoMaxBrightness(it) },
            onBackClick = { isSettingsOpen = false },
            isAuthenticated = session is AuthState.Authenticated,
            onDeleteAccount = {
                customer.deleteAccount {
                    isSettingsOpen = false
                }
            }
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VanillaCustard,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "LA EXQUISSSITA",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = BrandyBrown
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VanillaCustard,
                    titleContentColor = BrandyBrown
                ),
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = ApricotIllusion,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = WarmWhite,
                tonalElevation = 0.dp,
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = ApricotIllusion,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            ) {
                Screen.bottomNavItems.forEach { screen ->
                    val isSelected = selectedTab == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = screen.route },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandyBrown,
                            selectedTextColor = BrandyBrown,
                            indicatorColor = ApricotIllusion.copy(alpha = 0.45f),
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            Screen.Inicio.route -> {
                HomeScreen(
                    uiState = homeState,
                    onRefresh = { home.refresh() },
                    onNavigateToLoyalty = { selectedTab = Screen.Lealtad.route },
                    onNavigateToPromotions = { selectedTab = Screen.Promociones.route },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            Screen.Promociones.route -> {
                PromotionsScreen(
                    uiState = homeState,
                    onRefresh = { home.refresh() },
                    onNavigateToLoyalty = { selectedTab = Screen.Lealtad.route },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            Screen.Lealtad.route -> {
                LoyaltyScreen(
                    authState = session,
                    qrToken = card.qrToken,
                    loadingCard = card.loadingCard,
                    isBusy = card.busy,
                    autoMaxBrightnessEnabled = brightness,
                    onSignInClick = { isAuthSheetOpen = true },
                    onSignInWithGoogleClick = {
                        activity?.let { customer.signIn(it) }
                    },
                    onRefreshClick = { customer.refresh() },
                    onSaveToGalleryClick = saveQrToGalleryAction,
                    rewards = card.rewards,
                    currentPeriodLabel = card.currentPeriodLabel,
                    loadingRewards = card.loadingRewards,
                    rewardsError = card.rewardsError,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            Screen.Perfil.route -> {
                ProfileScreen(
                    authState = session,
                    isBusy = card.busy,
                    onSaveProfile = { name, phone -> customer.saveProfile(name, phone) },
                    onSignOut = { customer.signOut() },
                    onSettingsClick = { isSettingsOpen = true },
                    onSignInClick = { isAuthSheetOpen = true },
                    onSignInWithGoogleClick = {
                        activity?.let { customer.signIn(it) }
                    },
                    onRefreshClick = { customer.refresh() },
                    pointsHistory = card.pointsHistory,
                    loadingHistory = card.loadingHistory,
                    historyError = card.historyError,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
