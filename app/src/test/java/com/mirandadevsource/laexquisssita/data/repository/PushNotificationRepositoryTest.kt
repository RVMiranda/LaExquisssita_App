package com.mirandadevsource.laexquisssita.data.repository

import com.mirandadevsource.laexquisssita.core.security.PushTokenStorage
import com.mirandadevsource.laexquisssita.data.remote.PushNotificationRemote
import com.mirandadevsource.laexquisssita.domain.model.AuthState
import com.mirandadevsource.laexquisssita.domain.model.CustomerProfile
import com.mirandadevsource.laexquisssita.domain.repository.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PushNotificationRepositoryTest {

    @Test
    fun `syncToken stores token and registers with remote when user is authenticated`() = runTest {
        val remote = FakePushRemote()
        val storage = FakePushStorage()
        val auth = PushTestAuthRepository(initialState = AuthState.Guest)

        val repository = PushNotificationRepositoryImpl(
            remote = remote,
            storage = storage,
            authRepository = auth,
            scope = backgroundScope,
            tokenFetcher = { "fetched-token-abc" },
        )
        runCurrent()

        auth.mutableState.value = AuthState.Authenticated(
            userId = "user-123",
            profile = CustomerProfile("cust-123", "Rafael Miranda", "9191234567"),
            email = "rafael@example.com",
        )

        val success = repository.syncToken("my-token-123")
        runCurrent()

        assertTrue(success)
        assertEquals("my-token-123", storage.getFcmToken())
        assertEquals("my-token-123", remote.lastRegisteredToken)
    }

    @Test
    fun `syncToken stores token but skips remote registration when user is guest`() = runTest {
        val remote = FakePushRemote()
        val storage = FakePushStorage()
        val auth = PushTestAuthRepository(initialState = AuthState.Guest)

        val repository = PushNotificationRepositoryImpl(
            remote = remote,
            storage = storage,
            authRepository = auth,
            scope = backgroundScope,
            tokenFetcher = { "fetched-token-abc" },
        )

        val success = repository.syncToken("guest-token-456")
        runCurrent()

        assertTrue(success)
        assertEquals("guest-token-456", storage.getFcmToken())
        assertEquals(0, remote.registerCalls)
    }

    @Test
    fun `syncToken fetches token from provider when token argument is null`() = runTest {
        val remote = FakePushRemote()
        val storage = FakePushStorage()
        val auth = PushTestAuthRepository(initialState = AuthState.Guest)

        val repository = PushNotificationRepositoryImpl(
            remote = remote,
            storage = storage,
            authRepository = auth,
            scope = backgroundScope,
            tokenFetcher = { "auto-fetched-token-789" },
        )
        runCurrent()

        auth.mutableState.value = AuthState.Authenticated(
            userId = "user-123",
            profile = CustomerProfile("cust-123", "Rafael Miranda", null),
            email = "rafael@example.com",
        )

        val success = repository.syncToken(null)
        runCurrent()

        assertTrue(success)
        assertEquals("auto-fetched-token-789", storage.getFcmToken())
        assertEquals("auto-fetched-token-789", remote.lastRegisteredToken)
    }

    @Test
    fun `unregisterCurrentToken delegates to remote with stored token`() = runTest {
        val remote = FakePushRemote()
        val storage = FakePushStorage().apply { setFcmToken("token-to-delete") }
        val auth = PushTestAuthRepository(initialState = AuthState.Guest)

        val repository = PushNotificationRepositoryImpl(
            remote = remote,
            storage = storage,
            authRepository = auth,
            scope = backgroundScope,
        )

        val success = repository.unregisterCurrentToken()
        runCurrent()

        assertTrue(success)
        assertEquals("token-to-delete", remote.lastUnregisteredToken)
        assertEquals(1, remote.unregisterCalls)
    }

    @Test
    fun `session transition to authenticated triggers automatic token sync`() = runTest {
        val remote = FakePushRemote()
        val storage = FakePushStorage().apply { setFcmToken("persisted-token-555") }
        val auth = PushTestAuthRepository(initialState = AuthState.Guest)

        val repository = PushNotificationRepositoryImpl(
            remote = remote,
            storage = storage,
            authRepository = auth,
            scope = backgroundScope,
        )
        runCurrent()

        assertEquals(0, remote.registerCalls)

        // User logs in
        auth.mutableState.value = AuthState.Authenticated(
            userId = "user-login",
            profile = CustomerProfile("cust-login", "Cliente", null),
            email = "login@example.com",
        )
        runCurrent()

        assertEquals(1, remote.registerCalls)
        assertEquals("persisted-token-555", remote.lastRegisteredToken)
    }
}

private class FakePushStorage : PushTokenStorage {
    private var token: String? = null
    override fun getFcmToken(): String? = token
    override fun setFcmToken(token: String?) {
        this.token = token
    }
}

private class FakePushRemote : PushNotificationRemote {
    var registerCalls = 0
    var unregisterCalls = 0
    var lastRegisteredToken: String? = null
    var lastUnregisteredToken: String? = null
    var shouldFail = false

    override suspend fun registerDeviceToken(token: String, deviceModel: String?): Boolean {
        registerCalls++
        lastRegisteredToken = token
        return !shouldFail
    }

    override suspend fun unregisterDeviceToken(token: String): Boolean {
        unregisterCalls++
        lastUnregisteredToken = token
        return !shouldFail
    }
}

private class PushTestAuthRepository(
    initialState: AuthState = AuthState.Guest,
) : AuthRepository {
    val mutableState = MutableStateFlow(initialState)
    override val state = mutableState.asStateFlow()
    override val sessionVersion: Long = 1L
    override val currentUserId: String?
        get() = (state.value as? AuthState.Authenticated)?.userId

    override suspend fun signInWithGoogleIdToken(idToken: String, nonce: String): Result<Unit> = Result.success(Unit)
    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun signUpWithEmail(email: String, password: String, nombre: String, telefono: String?): Result<CustomerProfile> =
        Result.success(CustomerProfile("id", nombre, telefono))
    override suspend fun refreshProfile(): Result<CustomerProfile> =
        Result.success(CustomerProfile("id", "Name", null))
    override suspend fun updateProfile(name: String, phone: String?): Result<CustomerProfile> =
        Result.success(CustomerProfile("id", name, phone))
    override suspend fun signOut(): Result<Unit> {
        mutableState.value = AuthState.Guest
        return Result.success(Unit)
    }
    override suspend fun deleteAccount(): Result<Unit> {
        mutableState.value = AuthState.Guest
        return Result.success(Unit)
    }
}
