package com.mirandadevsource.laexquisssita.features.auth.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mirandadevsource.laexquisssita.ui.theme.ApricotIllusion
import com.mirandadevsource.laexquisssita.ui.theme.BrandyBrown
import com.mirandadevsource.laexquisssita.ui.theme.ChocolateFondant
import com.mirandadevsource.laexquisssita.ui.theme.ErrorRed
import com.mirandadevsource.laexquisssita.ui.theme.TextMuted
import com.mirandadevsource.laexquisssita.ui.theme.WarmWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneOnboardingSheet(
    isBusy: Boolean,
    onSavePhone: (String) -> Unit,
    onSkip: () -> Unit,
    onDismissPermanently: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var phoneInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onSkip,
        sheetState = sheetState,
        containerColor = WarmWhite,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Phone,
                    contentDescription = null,
                    tint = BrandyBrown,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Completa tu Registro",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = ChocolateFondant
                )
            }

            Text(
                text = "Registra tu número de teléfono celular (10 dígitos) para recibir notificaciones sobre tus visitas, recompensas y promociones exclusivas.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )

            OutlinedTextField(
                value = phoneInput,
                onValueChange = { newValue ->
                    if (newValue.length <= 10 && newValue.all { it.isDigit() }) {
                        phoneInput = newValue
                        errorMessage = null
                    }
                },
                label = { Text("Teléfono celular") },
                placeholder = { Text("Ej. 5512345678") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = errorMessage != null,
                supportingText = errorMessage?.let { { Text(it, color = ErrorRed) } },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandyBrown,
                    focusedLabelColor = BrandyBrown,
                    cursorColor = BrandyBrown
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onSkip,
                    enabled = !isBusy,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ApricotIllusion),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Omitir",
                        color = ChocolateFondant,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Button(
                    onClick = {
                        val trimmed = phoneInput.trim()
                        if (trimmed.length == 10) {
                            onSavePhone(trimmed)
                        } else {
                            errorMessage = "El teléfono debe contener 10 dígitos"
                        }
                    },
                    enabled = !isBusy && phoneInput.length == 10,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandyBrown),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (isBusy) "Guardando..." else "Guardar",
                        color = WarmWhite,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            TextButton(
                onClick = onDismissPermanently,
                enabled = !isBusy,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "No volver a mostrar",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
