package com.mirandadevsource.laexquisssita.data.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import com.mirandadevsource.laexquisssita.domain.model.AuthenticationFailure
import com.mirandadevsource.laexquisssita.domain.model.AuthenticationFailureReason
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

class SupabaseAuthGateway(private val client: SupabaseClient, private val sessions: SessionManager) : AuthGateway {
    override val status = client.auth.sessionStatus.map { status ->
        when (status) {
            SessionStatus.Initializing -> IdentityStatus.Initializing
            is SessionStatus.NotAuthenticated -> IdentityStatus.Guest
            is SessionStatus.RefreshFailure -> IdentityStatus.Offline(sessions.loadSessionOrNull()?.user?.let {
                CustomerIdentity(it.id, it.userMetadata?.get("full_name")?.jsonPrimitive?.contentOrNull
                    ?: "Cliente Distinguido", it.email,
                    it.userMetadata?.get("phone")?.jsonPrimitive?.contentOrNull)
            })
            is SessionStatus.Authenticated -> status.session.user?.let { user ->
                IdentityStatus.SignedIn(CustomerIdentity(
                    user.id,
                    user.userMetadata?.get("full_name")?.jsonPrimitive?.contentOrNull
                        ?: "Cliente Distinguido",
                    user.email,
                    user.userMetadata?.get("phone")?.jsonPrimitive?.contentOrNull,
                ))
            } ?: IdentityStatus.Guest
        }
    }

    override suspend fun signIn(idToken: String, nonce: String) {
        client.auth.awaitInitialization()
        try {
            client.auth.signInWith(IDToken) {
                this.idToken = idToken
                if (nonce.isNotBlank()) {
                    this.nonce = nonce
                }
                provider = Google
            }
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            throw error.toAuthenticationFailure()
        }
    }

    override suspend fun signInWithEmail(email: String, password: String) {
        client.auth.awaitInitialization()
        try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            throw error.toAuthenticationFailure()
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String, nombre: String, telefono: String?): EmailSignUpResult {
        client.auth.awaitInitialization()
        try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("full_name", nombre)
                    telefono?.let { put("phone", it) }
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            throw error.toAuthenticationFailure()
        }

        val user = client.auth.currentUserOrNull()
            ?: return EmailSignUpResult.ConfirmationRequired
        return EmailSignUpResult.SignedIn(
            CustomerIdentity(
                id = user.id,
                name = user.userMetadata?.get("full_name")?.jsonPrimitive?.contentOrNull ?: nombre,
                email = user.email ?: email,
                phone = user.userMetadata?.get("phone")?.jsonPrimitive?.contentOrNull,
            )
        )
    }

    override suspend fun deleteAccount() {
        client.auth.awaitInitialization()
        client.postgrest.rpc("delete_customer_account")
    }

    private fun Throwable.toAuthenticationFailure(): AuthenticationFailure {
        val chain = generateSequence(this) { it.cause }.toList()
        val code = chain.filterIsInstance<AuthRestException>().firstOrNull()?.errorCode
            ?.toString()?.lowercase().orEmpty()
        val details = chain.joinToString(" ") { it.message.orEmpty() }.lowercase()
        val networkFailure = chain.any { cause ->
            cause is IOException || cause::class.simpleName.orEmpty() in setOf(
                "HttpRequestException", "ConnectException", "UnknownHostException", "SocketTimeoutException"
            )
        }
        val restException = chain.filterIsInstance<AuthRestException>().firstOrNull()
        val rawMessage = restException?.description ?: restException?.message ?: this.message
        val reason = when {
            networkFailure -> AuthenticationFailureReason.NETWORK
            code.contains("invalidcredentials") || details.contains("invalid login") ||
                details.contains("invalid_credentials") || details.contains("invalid credentials") ->
                AuthenticationFailureReason.INVALID_CREDENTIALS
            code.contains("useralreadyexists") || details.contains("user already registered") ||
                details.contains("already been registered") -> AuthenticationFailureReason.EMAIL_ALREADY_REGISTERED
            details.contains("email_registration_blocked") || details.contains("account_deleted") ->
                AuthenticationFailureReason.ACCOUNT_DELETED
            code.contains("weakpassword") || details.contains("weak password") ||
                details.contains("password should be at least") -> AuthenticationFailureReason.WEAK_PASSWORD
            else -> AuthenticationFailureReason.UNKNOWN
        }
        val message = when (reason) {
            AuthenticationFailureReason.INVALID_CREDENTIALS -> "El correo o la contraseña no son correctos."
            AuthenticationFailureReason.EMAIL_ALREADY_REGISTERED -> "Este correo ya tiene una cuenta. Inicia sesión."
            AuthenticationFailureReason.ACCOUNT_DELETED ->
                "Esta cuenta fue dada de baja y el correo no puede volver a registrarse."
            AuthenticationFailureReason.WEAK_PASSWORD -> "La contraseña no cumple los requisitos de seguridad."
            AuthenticationFailureReason.NETWORK -> "No pudimos conectar. Revisa tu conexión e inténtalo de nuevo."
            else -> if (!rawMessage.isNullOrBlank()) "No fue posible completar la autenticación ($rawMessage)."
                    else "No fue posible completar la autenticación. Inténtalo de nuevo."
        }
        return AuthenticationFailure(reason, message, this)
    }

    override suspend fun signOut() {
        try {
            client.auth.signOut()
        } finally {
            withContext(NonCancellable) { client.auth.clearSession() }
        }
    }
}
