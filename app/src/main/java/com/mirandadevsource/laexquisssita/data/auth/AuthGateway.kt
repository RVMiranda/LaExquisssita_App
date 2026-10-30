package com.mirandadevsource.laexquisssita.data.auth

import kotlinx.coroutines.flow.Flow

data class CustomerIdentity(val id: String, val name: String, val email: String?, val phone: String? = null)

sealed interface IdentityStatus {
    data object Initializing : IdentityStatus
    data object Guest : IdentityStatus
    data class Offline(val identity: CustomerIdentity?) : IdentityStatus
    data class SignedIn(val identity: CustomerIdentity) : IdentityStatus
}

interface AuthGateway {
    val status: Flow<IdentityStatus>
    suspend fun signIn(idToken: String, nonce: String)
    suspend fun signInWithEmail(email: String, password: String)
    suspend fun signUpWithEmail(email: String, password: String, nombre: String, telefono: String?): EmailSignUpResult
    /** Permanently anonymizes and disables the authenticated customer's account remotely. */
    suspend fun deleteAccount()
    suspend fun signOut()
}

sealed interface EmailSignUpResult {
    data class SignedIn(val identity: CustomerIdentity) : EmailSignUpResult
    data object ConfirmationRequired : EmailSignUpResult
}
