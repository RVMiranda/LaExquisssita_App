package com.mirandadevsource.laexquisssita.features.auth.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirandadevsource.laexquisssita.ui.theme.ApricotIllusion
import com.mirandadevsource.laexquisssita.ui.theme.BrandyBrown
import com.mirandadevsource.laexquisssita.ui.theme.ChocolateFondant
import com.mirandadevsource.laexquisssita.ui.theme.TextMuted
import com.mirandadevsource.laexquisssita.ui.theme.VanillaCustard
import com.mirandadevsource.laexquisssita.ui.theme.WarmWhite

enum class LegalDocumentType {
    TERMS_AND_CONDITIONS,
    PRIVACY_POLICY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalDialog(
    type: LegalDocumentType,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = when (type) {
        LegalDocumentType.TERMS_AND_CONDITIONS -> "Términos y Condiciones"
        LegalDocumentType.PRIVACY_POLICY -> "Aviso de Privacidad"
    }

    val content = when (type) {
        LegalDocumentType.TERMS_AND_CONDITIONS -> """
            1. Objeto y Alcance
            El presente acuerdo regula el uso de la aplicación móvil y el Programa de Lealtad del Club La Exquisssita, operado por La Exquisssita para sus clientes.
            
            2. Tarjeta Digital y Código QR
            Al registrarse, el cliente recibe una Tarjeta Digital identificada con un código QR único y permanente. Dicho código es personal y está destinado a la acreditación de visitas y recompensas en el establecimiento.
            
            3. Acumulación de Puntos y Visitas
            • Para acreditar una visita o acumular puntos, el cliente deberá presentar su código QR en caja antes del pago.
            • Las visitas y puntos acumulados no tienen valor monetario y no son canjeables por dinero en efectivo.
            • El catálogo de recompensas y promociones está sujeto a disponibilidad y a las vigencias publicadas en la aplicación.
            
            4. Uso Aceptable
            El cliente se compromete a proporcionar información verídica y a mantener la confidencialidad de sus credenciales de acceso. Queda prohibido cualquier intento de falsificación de registros o manipulación del sistema.
            
            5. Modificaciones
            La Exquisssita se reserva el derecho de actualizar los beneficios, promociones y los presentes términos previa publicación en la aplicación.
        """.trimIndent()

        LegalDocumentType.PRIVACY_POLICY -> """
            1. Responsable del Tratamiento
            La Exquisssita es responsable del tratamiento y protección de sus datos personales conforme a la legislación aplicable en materia de protección de datos.
            
            2. Datos Personales Recabados
            Para la operación del Club de Lealtad, recabamos los siguientes datos:
            • Nombre completo
            • Dirección de correo electrónico
            • Número de teléfono celular (opcional)
            • Historial de visitas, puntos acumulados y canjes realizados
            
            3. Finalidad del Tratamiento
            Sus datos personales serán utilizados exclusivamente para:
            • Crear y mantener activa su cuenta de socio y tarjeta digital.
            • Acreditar sus visitas y contabilizar sus puntos de lealtad.
            • Notificarle sobre promociones vigentes, noticias del restaurante y estado de recompensas.
            • Brindarle atención al cliente y soporte sobre su cuenta.
            
            4. Seguridad y No Transferencia
            Sus datos se almacenan mediante mecanismos de cifrado y no serán compartidos, vendidos ni transferidos a terceros con fines comerciales ajenos a La Exquisssita.
            
            5. Ejercicio de Derechos (Acceso, Rectificación y Cancelación)
            Usted puede actualizar sus datos en la sección de Perfil de la aplicación o solicitar la cancelación de su cuenta a través de nuestros canales de contacto.
        """.trimIndent()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = WarmWhite),
            border = BorderStroke(1.dp, ApricotIllusion),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "LA EXQUISSSITA",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    color = BrandyBrown,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = ChocolateFondant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Área de lectura con scroll
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VanillaCustard),
                    border = BorderStroke(1.dp, ApricotIllusion.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                            color = ChocolateFondant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandyBrown),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Entendido",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = WarmWhite
                    )
                }
            }
        }
    }
}
