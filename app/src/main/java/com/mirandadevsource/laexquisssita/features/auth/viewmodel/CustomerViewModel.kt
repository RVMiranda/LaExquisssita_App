package com.mirandadevsource.laexquisssita.features.auth.viewmodel

import android.app.Activity
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mirandadevsource.laexquisssita.core.config.SupabaseConfig
import com.mirandadevsource.laexquisssita.core.security.CustomerPreferences
import com.mirandadevsource.laexquisssita.core.util.customerMessage
import com.mirandadevsource.laexquisssita.data.auth.GoogleAuthManager
import com.mirandadevsource.laexquisssita.data.auth.GoogleAccountUnavailableException
import com.mirandadevsource.laexquisssita.domain.model.*
import com.mirandadevsource.laexquisssita.domain.repository.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/** One activity-scoped instance shared by shell, card, profile and settings. */
class CustomerViewModel(
    private val auth: AuthRepository,
    private val loyalty: LoyaltyRepository,
    private val google: GoogleAuthManager,
    private val preferences: CustomerPreferences,
    private val pushNotifications: PushNotificationRepository? = null,
) : ViewModel() {
    val authState = auth.state
    val autoMaxBrightness = preferences.autoMaxBrightness
    private val mutableState = MutableStateFlow(CustomerUiState())
    val state = mutableState.asStateFlow()
    private var omittedPhoneFor: String? = null
    private var cardJob: Job? = null
    private var loyaltyDataJob: Job? = null
    private var loyaltyDataOwner: String? = null
    private var loyaltyDataSessionVersion: Long? = null
    private var loyaltyDataPoints: Int? = null
    private var authenticationJob: Job? = null
    private var authenticationRevision = 0L

    init {
        viewModelScope.launch {
            auth.state.collectLatest { session ->
                val signedIn = session as? AuthState.Authenticated
                val owner = signedIn?.userId
                if (owner != mutableState.value.userId) {
                    cardJob?.cancel()
                    loyaltyDataJob?.cancel()
                    mutableState.value = CustomerUiState(
                        userId = owner,
                        currentPeriodLabel = RewardPeriod.current().label,
                    )
                }
                if (signedIn != null) {
                    val isPermanentlyDismissed = owner != null && preferences.isPhonePromptDismissedPermanently(owner)
                    mutableState.update { it.copy(showPhoneOnboarding = signedIn.profile.telefono.isNullOrBlank()
                        && omittedPhoneFor != owner && !isPermanentlyDismissed) }
                    loadCard()
                    loadLoyaltyData(signedIn.profile.puntosLealtad)
                    viewModelScope.launch {
                        pushNotifications?.syncToken()
                    }
                }
            }
        }
    }

    fun signIn(activity: Activity) {
        launchAuthentication {
            if (!SupabaseConfig.isConfigured || SupabaseConfig.webClientId.isBlank()) {
                setAuthError("El acceso con Google aún no está configurado.")
                return@launchAuthentication
            }
            val credential = google.signInWithGoogle(activity, SupabaseConfig.webClientId)
            credential.onSuccess {
                auth.signInWithGoogleIdToken(it.idToken, it.nonce).onFailure { error ->
                    android.util.Log.e("EXQUISSSITA_AUTH", "signInWithGoogleIdToken failed: ${error.message}", error)
                    setAuthError(authErrorMessage(error))
                }
            }.onFailure { error ->
                android.util.Log.e("EXQUISSSITA_AUTH", "signInWithGoogle failure: ${error::class.java.simpleName}: ${error.message}", error)
                val msg = when {
                    error is GoogleAccountUnavailableException ->
                        "Agrega una cuenta de Google al teléfono y vuelve a intentarlo."
                    error is GetCredentialCancellationException ->
                        "Google canceló la autenticación. Si acabas de guardar las credenciales, espera un par de minutos a que Google sincronice con tu dispositivo."
                    else -> error.message ?: error.customerMessage()
                }
                setAuthError(msg)
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        launchAuthentication {
            auth.signInWithEmail(email, password).onFailure { error -> setAuthError(authErrorMessage(error)) }
        }
    }

    fun signUpWithEmail(email: String, password: String, nombre: String, telefono: String?) {
        launchAuthentication {
            auth.signUpWithEmail(email, password, nombre, telefono).onFailure { error ->
                val failure = error as? AuthenticationFailure
                if (failure?.reason == AuthenticationFailureReason.EMAIL_CONFIRMATION_REQUIRED) {
                    mutableState.update { it.copy(message = failure.message) }
                } else setAuthError(authErrorMessage(error))
            }
        }
    }

    private fun launchAuthentication(block: suspend () -> Unit) {
        if (mutableState.value.busy && !mutableState.value.isAuthenticating) return
        authenticationJob?.cancel()
        val operation = ++authenticationRevision
        authenticationJob = viewModelScope.launch {
            mutableState.update { it.copy(busy = true, isAuthenticating = true, authError = null, message = null) }
            try {
                block()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                setAuthError(authErrorMessage(error))
            } finally {
                if (operation == authenticationRevision) {
                    mutableState.update { it.copy(busy = false, isAuthenticating = false) }
                }
            }
        }
    }

    private fun setAuthError(message: String) { mutableState.update { it.copy(authError = message) } }

    private fun authErrorMessage(error: Throwable): String {
        val root = generateSequence(error) { it.cause }.lastOrNull() ?: error
        return when {
            error is AuthenticationFailure -> error.message
            !error.message.isNullOrBlank() -> error.message!!
            !root.message.isNullOrBlank() -> root.message!!
            else -> error.customerMessage()
        }
    }

    fun clearAuthError() { mutableState.update { it.copy(authError = null) } }

    fun refresh() {
        viewModelScope.launch {
            auth.refreshProfile().onFailure { error ->
                mutableState.update { it.copy(message = error.customerMessage()) }
            }
            loadCard()
            (auth.state.value as? AuthState.Authenticated)?.let { loadLoyaltyData(it.profile.puntosLealtad) }
        }
    }

    private fun loadLoyaltyData(customerPoints: Int) {
        if (auth.state.value !is AuthState.Authenticated) return
        val owner = auth.currentUserId ?: return
        val sessionVersion = auth.sessionVersion
        if (loyaltyDataJob?.isActive == true && loyaltyDataOwner == owner
            && loyaltyDataSessionVersion == sessionVersion && loyaltyDataPoints == customerPoints) return
        loyaltyDataJob?.cancel()
        loyaltyDataOwner = owner
        loyaltyDataSessionVersion = sessionVersion
        loyaltyDataPoints = customerPoints
        mutableState.update {
            it.copy(
                currentPeriodLabel = RewardPeriod.current().label,
                loadingRewards = true,
                loadingHistory = true,
                rewardsError = null,
                historyError = null,
            )
        }
        loyaltyDataJob = viewModelScope.launch {
            try {
                coroutineScope {
                    val rewards = async { loyalty.getActiveRewards(customerPoints) }
                    val history = async { loyalty.getPointsHistory() }
                    val rewardsResult = rewards.await()
                    val historyResult = history.await()
                    rewardsResult.exceptionOrNull()?.let {
                        android.util.Log.e("EXQUISSSITA_REWARDS", "Error loading rewards: ${it.message}", it)
                    }
                    if (auth.currentUserId == owner && auth.sessionVersion == sessionVersion) {
                        mutableState.update { current ->
                            current.copy(
                                rewards = rewardsResult.getOrElse { current.rewards },
                                pointsHistory = historyResult.getOrElse { current.pointsHistory },
                                rewardsError = rewardsResult.exceptionOrNull()?.customerMessage(),
                                historyError = historyResult.exceptionOrNull()?.customerMessage(),
                            )
                        }
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (auth.currentUserId == owner && auth.sessionVersion == sessionVersion) {
                    mutableState.update {
                        it.copy(
                            rewardsError = it.rewardsError ?: error.customerMessage(),
                            historyError = it.historyError ?: error.customerMessage(),
                        )
                    }
                }
            } finally {
                if (auth.currentUserId == owner && auth.sessionVersion == sessionVersion) {
                    mutableState.update { it.copy(loadingRewards = false, loadingHistory = false) }
                }
            }
        }
    }

    fun loadCard() {
        if (cardJob?.isActive == true || auth.state.value !is AuthState.Authenticated) return
        val owner = auth.currentUserId
        cardJob = viewModelScope.launch {
            mutableState.update { it.copy(loadingCard = true) }
            try {
                loyalty.getPermanentQrToken().onSuccess { token ->
                    if (auth.currentUserId == owner) mutableState.update { it.copy(qrToken = token) }
                }.onFailure { error ->
                    if (auth.currentUserId == owner) mutableState.update { it.copy(message = error.customerMessage()) }
                }
            } finally {
                if (auth.currentUserId == owner) mutableState.update { it.copy(loadingCard = false) }
            }
        }
    }

    fun saveProfile(name: String, phone: String?) {
        if (mutableState.value.busy || mutableState.value.isAuthenticating) return
        val owner = auth.currentUserId
        viewModelScope.launch {
            mutableState.update { it.copy(busy = true, message = null) }
            try {
                auth.updateProfile(name, phone).onSuccess {
                    if (auth.currentUserId == owner) {
                        omittedPhoneFor = owner
                        mutableState.update { it.copy(showPhoneOnboarding = false, message = "Datos actualizados.") }
                    }
                }.onFailure { error ->
                    val userMessage = if (error.isDuplicatePhone()) {
                        "Este número de teléfono ya está registrado en otra cuenta."
                    } else {
                        error.customerMessage()
                    }
                    if (auth.currentUserId == owner) mutableState.update { it.copy(message = userMessage) }
                }
            } finally { mutableState.update { it.copy(busy = false) } }
        }
    }

    fun skipPhone() {
        omittedPhoneFor = auth.currentUserId
        mutableState.update { it.copy(showPhoneOnboarding = false) }
    }

    fun dismissPhonePromptPermanently() {
        val owner = auth.currentUserId
        if (owner != null) {
            preferences.dismissPhonePromptPermanently(owner)
        }
        omittedPhoneFor = owner
        mutableState.update { it.copy(showPhoneOnboarding = false) }
    }

    fun signOut() {
        authenticationJob?.cancel()
        authenticationRevision++
        cardJob?.cancel()
        loyaltyDataJob?.cancel()
        omittedPhoneFor = null
        mutableState.value = CustomerUiState(busy = true)
        viewModelScope.launch {
            try {
                pushNotifications?.unregisterCurrentToken()
                val result = auth.signOut()
                result.getOrThrow()
                google.clearCredentialState()
            } finally { mutableState.value = CustomerUiState() }
        }
    }

    fun deleteAccount(onComplete: () -> Unit = {}) {
        if (mutableState.value.busy) return
        viewModelScope.launch {
            mutableState.update { it.copy(busy = true, message = null) }
            try {
                authenticationJob?.cancel()
                authenticationRevision++
                cardJob?.cancel()
                loyaltyDataJob?.cancel()
                omittedPhoneFor = null
                val result = auth.deleteAccount()
                result.getOrThrow()
                google.clearCredentialState()
                mutableState.value = CustomerUiState(message = "Tu cuenta ha sido dada de baja exitosamente.")
                onComplete()
            } catch (e: Exception) {
                mutableState.update { it.copy(message = e.customerMessage()) }
            } finally {
                mutableState.update { it.copy(busy = false) }
            }
        }
    }

    fun setAutoMaxBrightness(enabled: Boolean) = preferences.setAutoMaxBrightness(enabled)
    fun clearMessage() { mutableState.update { it.copy(message = null) } }
}

data class CustomerUiState(
    val userId: String? = null,
    val busy: Boolean = false,
    val isAuthenticating: Boolean = false,
    val authError: String? = null,
    val loadingCard: Boolean = false,
    val qrToken: String? = null,
    val rewards: List<CustomerRewardItem> = emptyList(),
    val pointsHistory: List<PointMovement> = emptyList(),
    val loadingRewards: Boolean = false,
    val loadingHistory: Boolean = false,
    val currentPeriodLabel: String = "",
    val rewardsError: String? = null,
    val historyError: String? = null,
    val showPhoneOnboarding: Boolean = false,
    val message: String? = null,
)

private fun Throwable.isDuplicatePhone(): Boolean {
    val msg = message.orEmpty().lowercase()
    return msg.contains("clientes_telefono_key") ||
        msg.contains("23505") ||
        (msg.contains("duplicate key") && msg.contains("telefono")) ||
        (msg.contains("unique constraint") && msg.contains("telefono"))
}
