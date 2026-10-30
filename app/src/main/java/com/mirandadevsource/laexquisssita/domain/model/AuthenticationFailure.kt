package com.mirandadevsource.laexquisssita.domain.model

enum class AuthenticationFailureReason {
    INVALID_INPUT,
    INVALID_CREDENTIALS,
    EMAIL_ALREADY_REGISTERED,
    WEAK_PASSWORD,
    EMAIL_CONFIRMATION_REQUIRED,
    ACCOUNT_DELETED,
    NETWORK,
    UNKNOWN,
}

class AuthenticationFailure(
    val reason: AuthenticationFailureReason,
    override val message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
