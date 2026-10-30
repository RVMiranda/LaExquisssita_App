package com.mirandadevsource.laexquisssita.features.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirandadevsource.laexquisssita.ui.theme.ApricotIllusion
import com.mirandadevsource.laexquisssita.ui.theme.BrandyBrown
import com.mirandadevsource.laexquisssita.ui.theme.ChocolateFondant
import com.mirandadevsource.laexquisssita.ui.theme.CreamSurface
import com.mirandadevsource.laexquisssita.ui.theme.ErrorRed
import com.mirandadevsource.laexquisssita.ui.theme.OfflineAmber
import com.mirandadevsource.laexquisssita.ui.theme.TextMuted
import com.mirandadevsource.laexquisssita.ui.theme.VanillaCustard
import com.mirandadevsource.laexquisssita.ui.theme.WarmWhite
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthBottomSheet(
    isAuthenticating: Boolean,
    authError: String?,
    infoMessage: String?,
    onClearAuthError: () -> Unit,
    onSignInWithEmail: (email: String, password: String) -> Unit,
    onSignUpWithEmail: (email: String, password: String, nombre: String, telefono: String?) -> Unit,
    onSignInWithGoogle: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initialTab: Int = 0,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) } // 0 = Iniciar Sesión, 1 = Registrarse

    // Formulario de Inicio de Sesión
    var loginEmail by rememberSaveable { mutableStateOf("") }
    var loginPassword by rememberSaveable { mutableStateOf("") }
    var loginPasswordVisible by rememberSaveable { mutableStateOf(false) }

    // Formulario de Registro
    var registerName by rememberSaveable { mutableStateOf("") }
    var registerEmail by rememberSaveable { mutableStateOf("") }
    var registerPassword by rememberSaveable { mutableStateOf("") }
    var registerPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var registerPhone by rememberSaveable { mutableStateOf("") }
    var termsAccepted by rememberSaveable { mutableStateOf(false) }

    // Estado para mostrar confirmación pendiente tras registrarse
    var isAwaitingEmailConfirmation by rememberSaveable { mutableStateOf(false) }
    var countdownSeconds by rememberSaveable { mutableIntStateOf(10) }

    // Diálogo de Términos / Privacidad
    var activeLegalDialog by remember { mutableStateOf<LegalDocumentType?>(null) }

    var localValidationError by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Detectar cuando el registro requirió confirmación por correo
    LaunchedEffect(infoMessage) {
        if (!infoMessage.isNullOrBlank() && infoMessage.contains("confirma tu correo", ignoreCase = true)) {
            isAwaitingEmailConfirmation = true
            countdownSeconds = 10
            loginEmail = registerEmail // Pre-llenar correo en la pestaña de login
        }
    }

    // Cuenta regresiva de 10 segundos para redirección suave al login
    LaunchedEffect(isAwaitingEmailConfirmation) {
        if (isAwaitingEmailConfirmation) {
            while (countdownSeconds > 0) {
                delay(1000L)
                countdownSeconds--
            }
            // Al terminar la cuenta regresiva, redirigir a Iniciar Sesión
            isAwaitingEmailConfirmation = false
            selectedTab = 0
            onClearAuthError()
        }
    }

    // Limpiar error al cambiar de pestaña
    LaunchedEffect(selectedTab) {
        localValidationError = null
        onClearAuthError()
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (!isAuthenticating) {
                onClearAuthError()
                onDismiss()
            }
        },
        sheetState = sheetState,
        containerColor = VanillaCustard,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Surface(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                color = ApricotIllusion.copy(alpha = 0.6f),
                shape = RoundedCornerShape(2.dp)
            ) {
                Spacer(modifier = Modifier.size(width = 36.dp, height = 4.dp))
            }
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título de la marca
            Text(
                text = "CLUB LA EXQUISSSITA",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = BrandyBrown,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (isAwaitingEmailConfirmation) {
                // VISTA INTERMEDIA: Confirmación enviada con temporizador
                EmailConfirmationNoticeView(
                    email = registerEmail,
                    countdown = countdownSeconds,
                    onGoToLoginNow = {
                        isAwaitingEmailConfirmation = false
                        selectedTab = 0
                        onClearAuthError()
                    }
                )
            } else {
                Text(
                    text = if (selectedTab == 0) "Bienvenido de nuevo" else "Crea tu cuenta de socio",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = ChocolateFondant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (selectedTab == 0)
                        "Accede a tu tarjeta digital, saldo de puntos y promociones."
                    else
                        "Regístrate para obtener tu código QR permanente y acumular puntos en cada visita.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Selector tipo Tab editorial Flat
                AuthTabSelector(
                    selectedTab = selectedTab,
                    onTabSelected = { newTab ->
                        if (!isAuthenticating) {
                            selectedTab = newTab
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Mensaje informativo genérico si existe y no es el de confirmación
                if (!infoMessage.isNullOrBlank() && !isAwaitingEmailConfirmation) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = OfflineAmber.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, OfflineAmber)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = OfflineAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = infoMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = ChocolateFondant
                            )
                        }
                    }
                }

                // Mensaje de error (de validación local o del backend)
                val displayedError = localValidationError ?: authError
                if (!displayedError.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = ErrorRed.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, ErrorRed)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = displayedError,
                                style = MaterialTheme.typography.bodySmall,
                                color = ErrorRed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Contenido según pestaña seleccionada
                if (selectedTab == 0) {
                    // PESTAÑA: INICIAR SESIÓN
                    OutlinedTextField(
                        value = loginEmail,
                        onValueChange = {
                            loginEmail = it.trim()
                            localValidationError = null
                            onClearAuthError()
                        },
                        label = { Text("Correo electrónico") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = BrandyBrown
                            )
                        },
                        singleLine = true,
                        enabled = !isAuthenticating,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = authTextFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = {
                            loginPassword = it
                            localValidationError = null
                            onClearAuthError()
                        },
                        label = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = BrandyBrown
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { loginPasswordVisible = !loginPasswordVisible }
                            ) {
                                Icon(
                                    imageVector = if (loginPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = if (loginPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                    tint = TextMuted
                                )
                            }
                        },
                        visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = !isAuthenticating,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                validateAndSubmitLogin(
                                    email = loginEmail,
                                    password = loginPassword,
                                    onError = { localValidationError = it },
                                    onSubmit = onSignInWithEmail
                                )
                            }
                        ),
                        colors = authTextFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            validateAndSubmitLogin(
                                email = loginEmail,
                                password = loginPassword,
                                onError = { localValidationError = it },
                                onSubmit = onSignInWithEmail
                            )
                        },
                        enabled = !isAuthenticating,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandyBrown),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (isAuthenticating) {
                            CircularProgressIndicator(
                                color = WarmWhite,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Iniciando sesión...",
                                style = MaterialTheme.typography.labelLarge,
                                color = WarmWhite
                            )
                        } else {
                            Text(
                                text = "Iniciar Sesión",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = WarmWhite
                            )
                        }
                    }
                } else {
                    // PESTAÑA: CREAR CUENTA
                    OutlinedTextField(
                        value = registerName,
                        onValueChange = {
                            registerName = it
                            localValidationError = null
                            onClearAuthError()
                        },
                        label = { Text("Nombre completo") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = BrandyBrown
                            )
                        },
                        singleLine = true,
                        enabled = !isAuthenticating,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = authTextFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = registerEmail,
                        onValueChange = {
                            registerEmail = it.trim()
                            localValidationError = null
                            onClearAuthError()
                        },
                        label = { Text("Correo electrónico") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = BrandyBrown
                            )
                        },
                        singleLine = true,
                        enabled = !isAuthenticating,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = authTextFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = registerPassword,
                        onValueChange = {
                            registerPassword = it
                            localValidationError = null
                            onClearAuthError()
                        },
                        label = { Text("Contraseña (mínimo 6 caracteres)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = BrandyBrown
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { registerPasswordVisible = !registerPasswordVisible }
                            ) {
                                Icon(
                                    imageVector = if (registerPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = if (registerPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                    tint = TextMuted
                                )
                            }
                        },
                        visualTransformation = if (registerPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = !isAuthenticating,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = authTextFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = registerPhone,
                        onValueChange = {
                            if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                                registerPhone = it
                                localValidationError = null
                                onClearAuthError()
                            }
                        },
                        label = { Text("Teléfono celular (opcional, 10 dígitos)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Phone,
                                contentDescription = null,
                                tint = BrandyBrown
                            )
                        },
                        singleLine = true,
                        enabled = !isAuthenticating,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                validateAndSubmitRegister(
                                    name = registerName,
                                    email = registerEmail,
                                    password = registerPassword,
                                    phone = registerPhone,
                                    termsAccepted = termsAccepted,
                                    onError = { localValidationError = it },
                                    onSubmit = onSignUpWithEmail
                                )
                            }
                        ),
                        colors = authTextFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Checkbox de Términos y Privacidad en Registro
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = {
                                termsAccepted = it
                                if (it) localValidationError = null
                            },
                            enabled = !isAuthenticating,
                            colors = CheckboxDefaults.colors(
                                checkedColor = BrandyBrown,
                                uncheckedColor = ApricotIllusion,
                                checkmarkColor = WarmWhite
                            )
                        )
                        TermsAndPrivacyClickableText(
                            prefix = "He leído y acepto los ",
                            onTermsClick = { activeLegalDialog = LegalDocumentType.TERMS_AND_CONDITIONS },
                            onPrivacyClick = { activeLegalDialog = LegalDocumentType.PRIVACY_POLICY },
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            validateAndSubmitRegister(
                                name = registerName,
                                email = registerEmail,
                                password = registerPassword,
                                phone = registerPhone,
                                termsAccepted = termsAccepted,
                                onError = { localValidationError = it },
                                onSubmit = onSignUpWithEmail
                            )
                        },
                        enabled = !isAuthenticating,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandyBrown),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (isAuthenticating) {
                            CircularProgressIndicator(
                                color = WarmWhite,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Creando cuenta...",
                                style = MaterialTheme.typography.labelLarge,
                                color = WarmWhite
                            )
                        } else {
                            Text(
                                text = "Crear Mi Cuenta",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = WarmWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Separador con estilo editorial
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = ApricotIllusion.copy(alpha = 0.7f),
                        thickness = 1.dp
                    )
                    Text(
                        text = "O continúa con",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = ApricotIllusion.copy(alpha = 0.7f),
                        thickness = 1.dp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón Google Sign-In
                OutlinedButton(
                    onClick = onSignInWithGoogle,
                    enabled = !isAuthenticating,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ApricotIllusion),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = WarmWhite,
                        contentColor = ChocolateFondant
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Continuar con Google",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                        color = ChocolateFondant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Leyenda legal general
                TermsAndPrivacyClickableText(
                    prefix = "Al ingresar o crear una cuenta, aceptas nuestros ",
                    onTermsClick = { activeLegalDialog = LegalDocumentType.TERMS_AND_CONDITIONS },
                    onPrivacyClick = { activeLegalDialog = LegalDocumentType.PRIVACY_POLICY },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    }

    // Modal de lectura de Términos o Aviso de Privacidad
    activeLegalDialog?.let { type ->
        LegalDialog(
            type = type,
            onDismiss = { activeLegalDialog = null }
        )
    }
}

@Composable
private fun EmailConfirmationNoticeView(
    email: String,
    countdown: Int,
    onGoToLoginNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(50.dp),
            color = OfflineAmber.copy(alpha = 0.15f),
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.MarkEmailRead,
                    contentDescription = null,
                    tint = OfflineAmber,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¡Cuenta Creada Exitosamente!",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = ChocolateFondant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Hemos enviado un enlace de confirmación a tu correo electrónico:",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = WarmWhite,
            border = BorderStroke(1.dp, ApricotIllusion)
        ) {
            Text(
                text = email.ifBlank { "tu correo registrado" },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = BrandyBrown,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Por favor abre el correo y haz clic en el botón de confirmación para activar tu tarjeta de socio permanente.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Indicador del temporizador de redirección
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CreamSurface,
            border = BorderStroke(1.dp, ApricotIllusion.copy(alpha = 0.6f))
        ) {
            Text(
                text = "Redirigiendo a Iniciar Sesión en $countdown segundos...",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = ChocolateFondant,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onGoToLoginNow,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandyBrown),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Ir a Iniciar Sesión Ahora",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = WarmWhite
            )
        }
    }
}

@Composable
private fun TermsAndPrivacyClickableText(
    prefix: String,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val annotatedString = buildAnnotatedString {
        append(prefix)

        pushStringAnnotation(tag = "TERMS", annotation = "terms")
        withStyle(
            style = SpanStyle(
                color = BrandyBrown,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append("Términos y Condiciones")
        }
        pop()

        append(" y el ")

        pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
        withStyle(
            style = SpanStyle(
                color = BrandyBrown,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append("Aviso de Privacidad")
        }
        pop()

        append(".")
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodySmall.copy(
            color = TextMuted,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            textAlign = TextAlign.Start
        ),
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "TERMS", start = offset, end = offset)
                .firstOrNull()?.let { onTermsClick() }

            annotatedString.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset)
                .firstOrNull()?.let { onPrivacyClick() }
        },
        modifier = modifier
    )
}

@Composable
private fun AuthTabSelector(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = WarmWhite,
        border = BorderStroke(1.dp, ApricotIllusion)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTab == 0) BrandyBrown else WarmWhite)
                    .clickable { onTabSelected(0) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Iniciar Sesión",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (selectedTab == 0) WarmWhite else ChocolateFondant
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTab == 1) BrandyBrown else WarmWhite)
                    .clickable { onTabSelected(1) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Crear Cuenta",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (selectedTab == 1) WarmWhite else ChocolateFondant
                )
            }
        }
    }
}

@Composable
private fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BrandyBrown,
    unfocusedBorderColor = ApricotIllusion,
    focusedLabelColor = BrandyBrown,
    unfocusedLabelColor = TextMuted,
    focusedTextColor = ChocolateFondant,
    unfocusedTextColor = ChocolateFondant,
    cursorColor = BrandyBrown,
    focusedContainerColor = WarmWhite,
    unfocusedContainerColor = WarmWhite
)

private fun validateAndSubmitLogin(
    email: String,
    password: String,
    onError: (String) -> Unit,
    onSubmit: (String, String) -> Unit
) {
    if (email.isBlank()) {
        onError("Ingresa tu correo electrónico.")
        return
    }
    if (!email.contains("@") || !email.contains(".")) {
        onError("Ingresa un correo electrónico válido.")
        return
    }
    if (password.isBlank()) {
        onError("Ingresa tu contraseña.")
        return
    }
    onSubmit(email.trim(), password)
}

private fun validateAndSubmitRegister(
    name: String,
    email: String,
    password: String,
    phone: String,
    termsAccepted: Boolean,
    onError: (String) -> Unit,
    onSubmit: (String, String, String, String?) -> Unit
) {
    if (name.trim().length < 2) {
        onError("Ingresa tu nombre completo.")
        return
    }
    if (email.isBlank()) {
        onError("Ingresa tu correo electrónico.")
        return
    }
    if (!email.contains("@") || !email.contains(".")) {
        onError("Ingresa un correo electrónico válido.")
        return
    }
    if (password.length < 6) {
        onError("La contraseña debe tener al menos 6 caracteres.")
        return
    }
    val cleanPhone = phone.trim().takeIf { it.isNotBlank() }
    if (cleanPhone != null && cleanPhone.length != 10) {
        onError("El teléfono celular debe contener exactamente 10 dígitos o dejarse vacío.")
        return
    }
    if (!termsAccepted) {
        onError("Debes aceptar los Términos y Condiciones y el Aviso de Privacidad para crear tu cuenta.")
        return
    }
    onSubmit(email.trim(), password, name.trim(), cleanPhone)
}
