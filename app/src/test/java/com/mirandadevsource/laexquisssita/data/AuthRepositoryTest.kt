package com.mirandadevsource.laexquisssita.data

import com.mirandadevsource.laexquisssita.data.auth.*
import com.mirandadevsource.laexquisssita.data.remote.*
import com.mirandadevsource.laexquisssita.data.repository.*
import com.mirandadevsource.laexquisssita.domain.model.*
import com.mirandadevsource.laexquisssita.domain.repository.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {
    @Test fun `email login returns success and emits authenticated customer profile`() = runTest {
        val gateway = EmailGateway()
        val repository = repository(gateway, TestRemote(), backgroundScope)

        val result = repository.signInWithEmail("  client@example.com ", "secret1")
        runCurrent()

        assertTrue(result.isSuccess)
        assertEquals("client@example.com", gateway.lastEmail)
        assertTrue(repository.state.value is AuthState.Authenticated)
    }

    @Test fun `email registration creates profile with supplied name and optional phone`() = runTest {
        val remote = TestRemote()
        val repository = repository(EmailGateway(), remote, backgroundScope)

        val profile = repository.signUpWithEmail(
            email = "new@example.com", password = "secret1", nombre = " Ana Cliente ", telefono = "5512345678"
        ).getOrThrow()

        assertEquals("Ana Cliente", profile.nombre)
        assertEquals("5512345678", remote.lastPhone)
        assertTrue(repository.state.value is AuthState.Authenticated)
    }

    @Test fun `signup needing email confirmation does not call profile rpc`() = runTest {
        val remote = TestRemote()
        val gateway = EmailGateway(confirmationRequired = true)
        val repository = repository(gateway, remote, backgroundScope)

        val result = repository.signUpWithEmail("new@example.com", "secret1", "Ana", null)

        assertEquals(AuthenticationFailureReason.EMAIL_CONFIRMATION_REQUIRED,
            (result.exceptionOrNull() as AuthenticationFailure).reason)
        assertEquals(0, remote.ensureCalls)

        gateway.status.value = IdentityStatus.SignedIn(CustomerIdentity("auth-new", "Ana", "new@example.com", "5512345678"))
        runCurrent()
        assertEquals("5512345678", remote.lastPhone)
    }

    @Test fun `invalid credentials are returned as a friendly failure`() = runTest {
        val gateway = EmailGateway(loginError = AuthenticationFailure(
            AuthenticationFailureReason.INVALID_CREDENTIALS, "El correo o la contraseña no son correctos."
        ))
        val result = repository(gateway, TestRemote(), backgroundScope)
            .signInWithEmail("client@example.com", "wrongpass")

        assertEquals(AuthenticationFailureReason.INVALID_CREDENTIALS,
            (result.exceptionOrNull() as AuthenticationFailure).reason)
    }

    @Test fun `profile rpc network error preserves signed in retry state`() = runTest {
        val remote = TestRemote(fail = true)
        val repository = repository(EmailGateway(), remote, backgroundScope)

        val result = repository.signUpWithEmail("new@example.com", "secret1", "Ana", null)

        assertTrue(result.isFailure)
        assertTrue(repository.state.value is AuthState.ProfileError)
        assertEquals("auth-new", repository.currentUserId)
    }

    @Test fun `account deletion revokes session and clears customer cache after remote success`() = runTest {
        val gateway = EmailGateway()
        val cache = TestCache().apply {
            saveProfile("auth-login", CustomerProfile("customer-login", "Client", null))
        }
        val repository = repository(gateway, TestRemote(), backgroundScope, cache)
        repository.signInWithEmail("client@example.com", "secret1")
        runCurrent()

        val result = repository.deleteAccount()

        assertTrue(result.isSuccess)
        assertEquals(1, gateway.deleteCalls)
        assertEquals(1, gateway.signOutCalls)
        assertNull(repository.currentUserId)
        assertTrue(repository.state.value is AuthState.Guest)
        assertNull(cache.profile("auth-login"))
    }

    @Test fun `failed remote account deletion keeps the active session`() = runTest {
        val gateway = EmailGateway(deleteError = IllegalStateException("rpc failure"))
        val repository = repository(gateway, TestRemote(), backgroundScope)
        repository.signInWithEmail("client@example.com", "secret1")
        runCurrent()

        val result = repository.deleteAccount()

        assertTrue(result.isFailure)
        assertEquals(1, gateway.deleteCalls)
        assertEquals(0, gateway.signOutCalls)
        assertEquals("auth-login", repository.currentUserId)
        assertTrue(repository.state.value is AuthState.Authenticated)
    }

    private fun repository(
        gateway: EmailGateway,
        remote: TestRemote,
        scope: kotlinx.coroutines.CoroutineScope,
        cache: TestCache = TestCache(),
    ) = AuthRepositoryImpl(gateway, remote, cache, scope)
}

private class EmailGateway(
    private val confirmationRequired: Boolean = false,
    private val loginError: Exception? = null,
    private val deleteError: Exception? = null,
) : AuthGateway {
    override val status = MutableStateFlow<IdentityStatus>(IdentityStatus.Guest)
    var lastEmail: String? = null
    var deleteCalls = 0
    var signOutCalls = 0

    override suspend fun signIn(idToken: String, nonce: String) = Unit
    override suspend fun signInWithEmail(email: String, password: String) {
        loginError?.let { throw it }
        lastEmail = email
        status.value = IdentityStatus.SignedIn(CustomerIdentity("auth-login", "Client", email))
    }
    override suspend fun signUpWithEmail(email: String, password: String, nombre: String, telefono: String?): EmailSignUpResult {
        if (confirmationRequired) return EmailSignUpResult.ConfirmationRequired
        val identity = CustomerIdentity("auth-new", nombre, email, telefono)
        status.value = IdentityStatus.SignedIn(identity)
        return EmailSignUpResult.SignedIn(identity)
    }
    override suspend fun deleteAccount() {
        deleteCalls++
        deleteError?.let { throw it }
    }
    override suspend fun signOut() {
        signOutCalls++
        status.value = IdentityStatus.Guest
    }
}

private class TestCache : CustomerCache {
    private val profiles = mutableMapOf<String, CustomerProfile>()
    override fun profile(userId: String) = profiles[userId]
    override fun saveProfile(userId: String, profile: CustomerProfile) { profiles[userId] = profile }
    override fun qr(userId: String): String? = null
    override fun saveQr(userId: String, token: String) = Unit
    override fun clearCustomerData() = profiles.clear()
}

private class TestRemote(private val fail: Boolean = false) : CustomerRemote {
    var ensureCalls = 0
    var lastPhone: String? = null
    override suspend fun ensureProfile(name: String, phone: String?): CustomerProfile {
        ensureCalls++
        lastPhone = phone
        if (fail) error("network")
        return CustomerProfile("customer-new", name, phone)
    }
    override suspend fun updateProfile(name: String, phone: String?) = ensureProfile(name, phone)
    override suspend fun recoverQr() = CustomerQr("customer-new", "a".repeat(64))
    override suspend fun rewards() = emptyList<Reward>()
    override suspend fun redeemedRewardIds(startInclusive: String, endExclusive: String) = emptySet<String>()
    override suspend fun history(customerId: String) = emptyList<PointMovement>()
}
