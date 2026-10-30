package com.mirandadevsource.laexquisssita.data

import com.mirandadevsource.laexquisssita.core.util.operationResult
import com.mirandadevsource.laexquisssita.data.auth.*
import com.mirandadevsource.laexquisssita.data.remote.*
import com.mirandadevsource.laexquisssita.data.repository.*
import com.mirandadevsource.laexquisssita.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CustomerDataTest {
    private val owner = CustomerIdentity("auth-one", "Cliente", "one@example.invalid")

    @Test fun `restored session exposes cached profile when network fails`() = runTest {
        val cache = MemoryCache().apply { saveProfile(owner.id, CustomerProfile("customer-one", "Cached")) }
        val gateway = FakeAuth(IdentityStatus.SignedIn(owner))
        val remote = FakeRemote().apply { offline = true }
        val auth = AuthRepositoryImpl(gateway, remote, cache, backgroundScope)
        runCurrent()
        val state = auth.state.value as AuthState.Authenticated
        assertTrue(state.isOffline)
        assertEquals("Cached", state.profile.nombre)
    }

    @Test fun `expired offline session restores previously cached card`() = runTest {
        val cache = MemoryCache().apply {
            saveProfile(owner.id, CustomerProfile("customer-one", "Cached"))
            saveQr(owner.id, "a".repeat(64))
        }
        val remote = FakeRemote().apply { offline = true }
        val auth = AuthRepositoryImpl(FakeAuth(IdentityStatus.Offline(owner)), remote, cache, backgroundScope)
        runCurrent()
        assertTrue((auth.state.value as AuthState.Authenticated).isOffline)
        assertEquals("a".repeat(64), LoyaltyRepositoryImpl(remote, cache, auth).getPermanentQrToken().getOrThrow())
        assertEquals(0, remote.qrCalls)
    }

    @Test fun `profile failure allows retry without signing in again`() = runTest {
        val remote = FakeRemote().apply { offline = true }
        val auth = AuthRepositoryImpl(FakeAuth(IdentityStatus.SignedIn(owner)), remote, MemoryCache(), backgroundScope)
        runCurrent()
        assertTrue(auth.state.value is AuthState.ProfileError)
        remote.offline = false
        assertTrue(auth.refreshProfile().isSuccess)
        assertTrue(auth.state.value is AuthState.Authenticated)
    }

    @Test fun `missing cache recovers same server token across installations`() = runTest {
        val remote = FakeRemote()
        val firstCache = MemoryCache()
        val firstAuth = AuthRepositoryImpl(FakeAuth(IdentityStatus.SignedIn(owner)), remote, firstCache, backgroundScope)
        runCurrent()
        val first = LoyaltyRepositoryImpl(remote, firstCache, firstAuth).getPermanentQrToken().getOrThrow()
        val newCache = MemoryCache()
        val newAuth = AuthRepositoryImpl(FakeAuth(IdentityStatus.SignedIn(owner)), remote, newCache, backgroundScope)
        runCurrent()
        assertEquals(first, LoyaltyRepositoryImpl(remote, newCache, newAuth).getPermanentQrToken().getOrThrow())
        assertEquals(2, remote.qrCalls)
    }

    @Test fun `simultaneous requests recover only once`() = runTest {
        val remote = FakeRemote()
        val cache = MemoryCache()
        val auth = AuthRepositoryImpl(FakeAuth(IdentityStatus.SignedIn(owner)), remote, cache, backgroundScope)
        runCurrent()
        val loyalty = LoyaltyRepositoryImpl(remote, cache, auth)
        val values = List(8) { async { loyalty.getPermanentQrToken().getOrThrow() } }.awaitAll()
        assertEquals(1, values.distinct().size)
        assertEquals(1, remote.qrCalls)
    }

    @Test fun `signout clears cache even when remote signout fails`() = runTest {
        val gateway = FakeAuth(IdentityStatus.SignedIn(owner)).apply { failSignOut = true }
        val cache = MemoryCache()
        val auth = AuthRepositoryImpl(gateway, FakeRemote(), cache, backgroundScope)
        runCurrent()
        cache.saveQr(owner.id, "a".repeat(64))
        auth.signOut()
        assertEquals(AuthState.Guest, auth.state.value)
        assertNull(cache.qr(owner.id))
        assertNull(cache.profile(owner.id))
        assertNull(auth.currentUserId)
    }

    @Test fun `response arriving after logout is never cached`() = runTest {
        val remote = FakeRemote().apply { qrGate = CompletableDeferred() }
        val cache = MemoryCache()
        val auth = AuthRepositoryImpl(FakeAuth(IdentityStatus.SignedIn(owner)), remote, cache, backgroundScope)
        runCurrent()
        val request = async { LoyaltyRepositoryImpl(remote, cache, auth).getPermanentQrToken() }
        runCurrent()
        auth.signOut()
        remote.qrGate!!.complete(Unit)
        assertTrue(request.await().isFailure)
        assertNull(cache.qr(owner.id))
    }

    @Test fun `wrong customer response is rejected`() = runTest {
        val remote = FakeRemote().apply { qrCustomer = "different-customer" }
        val cache = MemoryCache()
        val auth = AuthRepositoryImpl(FakeAuth(IdentityStatus.SignedIn(owner)), remote, cache, backgroundScope)
        runCurrent()
        assertTrue(LoyaltyRepositoryImpl(remote, cache, auth).getPermanentQrToken().isFailure)
        assertNull(cache.qr(owner.id))
    }

    @Test fun `account switch never shows previous profile or QR`() = runTest {
        val gateway = FakeAuth(IdentityStatus.SignedIn(owner))
        val cache = MemoryCache()
        val remote = FakeRemote()
        val auth = AuthRepositoryImpl(gateway, remote, cache, backgroundScope)
        runCurrent()
        cache.saveQr(owner.id, "a".repeat(64))
        remote.offline = true
        gateway.status.value = IdentityStatus.SignedIn(CustomerIdentity("auth-two", "Two", null))
        runCurrent()
        assertTrue(auth.state.value is AuthState.ProfileError)
        assertNull(cache.qr(owner.id))
        assertNull(cache.profile(owner.id))
    }

    @Test fun `coroutine cancellation propagates`() = runTest {
        try {
            operationResult<Unit> { throw CancellationException("cancel") }
            fail("Cancellation must not be wrapped in Result")
        } catch (_: CancellationException) { }
    }

    @Test fun `profile input accepts optional phone and rejects invalid values`() {
        assertNull(ProfileInput.validated(" Cliente ", " ").phone)
        assertEquals("Cliente", ProfileInput.validated(" Cliente ", "5512345678").name)
        for (phone in listOf("123", "abcdefghij", "+525512345678")) {
            assertTrue(runCatching { ProfileInput.validated("Cliente", phone) }.isFailure)
        }
        assertTrue(runCatching { ProfileInput.validated(" ", null) }.isFailure)
    }
}

private class MemoryCache : CustomerCache {
    private val profiles = mutableMapOf<String, CustomerProfile>()
    private val tokens = mutableMapOf<String, String>()
    override fun profile(userId: String) = profiles[userId]
    override fun saveProfile(userId: String, profile: CustomerProfile) { profiles[userId] = profile }
    override fun qr(userId: String) = tokens[userId]
    override fun saveQr(userId: String, token: String) { tokens[userId] = token }
    override fun clearCustomerData() { profiles.clear(); tokens.clear() }
}

private class FakeAuth(initial: IdentityStatus) : AuthGateway {
    override val status = MutableStateFlow(initial)
    var failSignOut = false
    override suspend fun signIn(idToken: String, nonce: String) = Unit
    override suspend fun signInWithEmail(email: String, password: String) = Unit
    override suspend fun signUpWithEmail(email: String, password: String, nombre: String, telefono: String?) =
        EmailSignUpResult.SignedIn(CustomerIdentity("auth-new", nombre, email))
    override suspend fun deleteAccount() = Unit
    override suspend fun signOut() {
        if (failSignOut) error("network")
        status.value = IdentityStatus.Guest
    }
}

private class FakeRemote : CustomerRemote {
    var offline = false
    var qrCalls = 0
    var qrGate: CompletableDeferred<Unit>? = null
    var qrCustomer = "customer-one"
    override suspend fun ensureProfile(name: String, phone: String?): CustomerProfile {
        if (offline) error("network")
        return CustomerProfile("customer-one", name, phone)
    }
    override suspend fun updateProfile(name: String, phone: String?) = ensureProfile(name, phone)
    override suspend fun recoverQr(): CustomerQr {
        qrCalls++
        qrGate?.await()
        if (offline) error("network")
        return CustomerQr(qrCustomer, "a".repeat(64))
    }
    override suspend fun rewards() = emptyList<Reward>()
    override suspend fun redeemedRewardIds(startInclusive: String, endExclusive: String) = emptySet<String>()
    override suspend fun history(customerId: String) = emptyList<PointMovement>()
}
