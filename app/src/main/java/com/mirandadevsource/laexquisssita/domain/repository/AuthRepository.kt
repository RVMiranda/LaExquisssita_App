package com.mirandadevsource.laexquisssita.domain.repository

import com.mirandadevsource.laexquisssita.domain.model.AuthState
import com.mirandadevsource.laexquisssita.domain.model.CustomerProfile
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val state: StateFlow<AuthState>
    val currentUserId: String?
    val sessionVersion: Long
    val isLoggedIn: Boolean get() = currentUserId != null
    suspend fun signInWithGoogleIdToken(idToken: String, nonce: String): Result<Unit>
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>
    suspend fun signUpWithEmail(email: String, password: String, nombre: String, telefono: String?): Result<CustomerProfile>
    suspend fun refreshProfile(): Result<CustomerProfile>
    suspend fun updateProfile(nombre: String, telefono: String?): Result<CustomerProfile>
    suspend fun deleteAccount(): Result<Unit>
    suspend fun signOut(): Result<Unit>
}
