package com.mirandadevsource.laexquisssita.domain.model

sealed interface AuthState {
    data object Initializing : AuthState
    data object Guest : AuthState
    data class LoadingProfile(val userId: String) : AuthState
    data class Authenticated(
        val userId: String,
        val profile: CustomerProfile,
        val email: String? = null,
        val isOffline: Boolean = false,
    ) : AuthState
    data class ProfileError(val userId: String, val message: String) : AuthState
}
