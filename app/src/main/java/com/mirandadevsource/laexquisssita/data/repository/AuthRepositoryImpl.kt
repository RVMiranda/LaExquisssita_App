package com.mirandadevsource.laexquisssita.data.repository

import com.mirandadevsource.laexquisssita.core.util.customerMessage
import com.mirandadevsource.laexquisssita.core.util.operationResult
import com.mirandadevsource.laexquisssita.data.auth.*
import com.mirandadevsource.laexquisssita.data.remote.CustomerRemote
import com.mirandadevsource.laexquisssita.domain.model.*
import com.mirandadevsource.laexquisssita.domain.repository.AuthRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong
import java.util.Locale

class AuthRepositoryImpl(
    private val gateway: AuthGateway,
    private val remote: CustomerRemote,
    private val cache: CustomerCache,
    scope: CoroutineScope,
) : AuthRepository {
    private val mutableState = MutableStateFlow<AuthState>(AuthState.Initializing)
    override val state = mutableState.asStateFlow()
    @Volatile private var identity: CustomerIdentity? = null
    @Volatile private var signingOut = false
    private val revision = AtomicLong()
    override val sessionVersion get() = revision.get()
    override val currentUserId: String? get() = identity?.id
    private val profileMutex = Mutex()
    private val authenticationMutex = Mutex()

    init {
        scope.launch {
            gateway.status.distinctUntilChanged().collectLatest { status ->
                val shouldRefreshProfile = authenticationMutex.withLock {
                    synchronized(cache) {
                        if (signingOut && status != IdentityStatus.Guest) return@synchronized false
                        var shouldRefresh = false
                        val sameActiveIdentity = status is IdentityStatus.SignedIn &&
                            identity?.id == status.identity.id
                        if (!sameActiveIdentity) revision.incrementAndGet()
                        when (status) {
                            IdentityStatus.Initializing -> mutableState.value = AuthState.Initializing
                            IdentityStatus.Guest -> {
                                identity = null
                                cache.clearCustomerData()
                                mutableState.value = AuthState.Guest
                            }
                            is IdentityStatus.Offline -> {
                                identity = status.identity
                                mutableState.value = status.identity?.let { user ->
                                    cache.profile(user.id)?.let { AuthState.Authenticated(user.id, it, user.email, true) }
                                        ?: AuthState.ProfileError(user.id, "Conéctate para recuperar tu perfil.")
                                } ?: AuthState.Guest
                            }
                            is IdentityStatus.SignedIn -> {
                                val sameIdentity = identity?.id == status.identity.id
                                if (!sameIdentity) retainOnly(status.identity.id)
                                identity = status.identity
                                val alreadyAuthenticated = (mutableState.value as? AuthState.Authenticated)
                                    ?.userId == status.identity.id
                                if (!alreadyAuthenticated) {
                                    mutableState.value = cache.profile(status.identity.id)?.let {
                                        AuthState.Authenticated(status.identity.id, it, status.identity.email, isOffline = true)
                                    } ?: AuthState.LoadingProfile(status.identity.id)
                                }
                                shouldRefresh = !alreadyAuthenticated
                            }
                        }
                        shouldRefresh
                    }
                }
                if (shouldRefreshProfile && status is IdentityStatus.SignedIn && identity?.id == status.identity.id) {
                    refreshProfile()
                }
            }
        }
    }

    private fun retainOnly(userId: String) {
        val profile = cache.profile(userId)
        val qr = cache.qr(userId)
        cache.clearCustomerData()
        profile?.let { cache.saveProfile(userId, it) }
        qr?.let { cache.saveQr(userId, it) }
    }

    override suspend fun signInWithGoogleIdToken(idToken: String, nonce: String) = authenticationMutex.withLock {
        operationResult {
            require(idToken.isNotBlank())
            gateway.signIn(idToken, nonce)
        }
    }

    override suspend fun signInWithEmail(email: String, password: String) = authenticationMutex.withLock {
        operationResult {
            val cleanEmail = normalizeEmail(email)
            require(password.isNotEmpty())
            gateway.signInWithEmail(cleanEmail, password)
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        nombre: String,
        telefono: String?,
    ): Result<CustomerProfile> = authenticationMutex.withLock {
        operationResult {
            val cleanEmail = normalizeEmail(email)
            if (password.length < MIN_PASSWORD_LENGTH) {
                throw AuthenticationFailure(
                    AuthenticationFailureReason.INVALID_INPUT,
                    "La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres.",
                )
            }
            val input = ProfileInput.validated(nombre, telefono)
            val result = gateway.signUpWithEmail(cleanEmail, password, input.name, input.phone)
            val signedIn = (result as? EmailSignUpResult.SignedIn)?.identity
                ?: throw AuthenticationFailure(
                    AuthenticationFailureReason.EMAIL_CONFIRMATION_REQUIRED,
                    "Tu cuenta se creó. Confirma tu correo y después inicia sesión para activar tu tarjeta.",
                )

            synchronized(cache) {
                if (identity?.id != signedIn.id) retainOnly(signedIn.id)
                identity = signedIn
                revision.incrementAndGet()
                mutableState.value = AuthState.LoadingProfile(signedIn.id)
            }

            try {
                val profile = remote.ensureProfile(input.name, input.phone)
                synchronized(cache) {
                    check(identity?.id == signedIn.id) { "Session changed" }
                    cache.saveProfile(signedIn.id, profile)
                    mutableState.value = AuthState.Authenticated(signedIn.id, profile, signedIn.email)
                }
                profile
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                synchronized(cache) {
                    if (identity?.id == signedIn.id) {
                        mutableState.value = AuthState.ProfileError(signedIn.id, error.customerMessage())
                    }
                }
                throw error
            }
        }
    }

    override suspend fun refreshProfile(): Result<CustomerProfile> = profileMutex.withLock {
        val owner = identity ?: return@withLock Result.failure(IllegalStateException("No session"))
        val version = sessionVersion
        val result = operationResult {
            val profile = remote.ensureProfile(owner.name.ifBlank { "Cliente Distinguido" }, owner.phone)
            synchronized(cache) {
                check(identity == owner && sessionVersion == version) { "Session changed" }
                cache.saveProfile(owner.id, profile)
                mutableState.value = AuthState.Authenticated(owner.id, profile, owner.email)
            }
            profile
        }
        synchronized(cache) {
            if (result.isFailure && identity == owner && sessionVersion == version) {
                mutableState.value = cache.profile(owner.id)?.let {
                    AuthState.Authenticated(owner.id, it, owner.email, isOffline = true)
                } ?: AuthState.ProfileError(owner.id, result.exceptionOrNull()!!.customerMessage())
            }
        }
        result
    }

    override suspend fun updateProfile(nombre: String, telefono: String?) = profileMutex.withLock {
        operationResult {
            val input = ProfileInput.validated(nombre, telefono)
            val owner = checkNotNull(identity)
            val version = sessionVersion
            val profile = remote.updateProfile(input.name, input.phone)
            synchronized(cache) {
                check(identity == owner && sessionVersion == version) { "Session changed" }
                cache.saveProfile(owner.id, profile)
                mutableState.value = AuthState.Authenticated(owner.id, profile, owner.email)
            }
            profile
        }
    }

    override suspend fun signOut(): Result<Unit> = authenticationMutex.withLock {
        // Cache mutation and identity changes share one monitor with the loyalty repository.
        // Network requests never run under this monitor.
        synchronized(cache) {
            signingOut = true
            identity = null
            revision.incrementAndGet()
            mutableState.value = AuthState.Guest
            cache.clearCustomerData()
        }
        try { operationResult { gateway.signOut() } }
        finally { signingOut = false }
    }

    override suspend fun deleteAccount(): Result<Unit> = authenticationMutex.withLock {
        if (identity == null) {
            return@withLock Result.failure(IllegalStateException("No session"))
        }

        val deletion = operationResult { gateway.deleteAccount() }
        if (deletion.isFailure) return@withLock deletion

        // The database has committed the irreversible deletion. Clear local identity,
        // caches and refresh credentials even if the caller is cancelled immediately after.
        withContext(NonCancellable) {
            synchronized(cache) {
                signingOut = true
                identity = null
                revision.incrementAndGet()
                mutableState.value = AuthState.Guest
                cache.clearCustomerData()
            }
            try {
                gateway.signOut()
            } catch (_: Exception) {
                // The remote deletion also revoked server-side refresh sessions.
                // SupabaseAuthGateway clears its local session in a finally block.
            } finally {
                signingOut = false
            }
        }
        Result.success(Unit)
    }

    private fun normalizeEmail(email: String): String {
        val clean = email.trim().lowercase(Locale.ROOT)
        if (!EMAIL_REGEX.matches(clean)) {
            throw AuthenticationFailure(
                AuthenticationFailureReason.INVALID_INPUT,
                "Escribe un correo electrónico válido.",
            )
        }
        return clean
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
        val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    }
}
