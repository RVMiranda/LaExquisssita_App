package com.mirandadevsource.laexquisssita.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/** Android Keystore key never leaves the device; each value has an independent GCM nonce. */
class EncryptedPreferences(private val context: Context) {
    private val prefs = context.getSharedPreferences("customer_secure_v2", Context.MODE_PRIVATE)

    private val key: SecretKey by lazy { getOrCreateKey() }

    private fun getOrCreateKey(): SecretKey {
        return try {
            val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val existing = runCatching { store.getKey(ALIAS, null) as? SecretKey }.getOrNull()
            if (existing != null) return existing

            if (store.containsAlias(ALIAS)) {
                store.deleteEntry(ALIAS)
            }
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
            )
            keyGenerator.init(
                KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
            keyGenerator.generateKey()
        } catch (e: Exception) {
            android.util.Log.e("EXQUISSSITA_AUTH", "AndroidKeyStore failed, falling back to app-private AES key: ${e.message}", e)
            getOrCreateFallbackKey()
        }
    }

    private fun getOrCreateFallbackKey(): SecretKey {
        val fallbackPrefs = context.getSharedPreferences("customer_fallback_keystore", Context.MODE_PRIVATE)
        val storedKey = fallbackPrefs.getString("fallback_aes_key", null)
        if (storedKey != null) {
            val bytes = Base64.decode(storedKey, Base64.NO_WRAP)
            return SecretKeySpec(bytes, "AES")
        }
        val keyBytes = ByteArray(32).apply { SecureRandom().nextBytes(this) }
        fallbackPrefs.edit().putString("fallback_aes_key", Base64.encodeToString(keyBytes, Base64.NO_WRAP)).apply()
        return SecretKeySpec(keyBytes, "AES")
    }

    @Synchronized fun put(name: String, value: String) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        cipher.updateAAD(name.toByteArray(Charsets.UTF_8))
        val bytes = cipher.iv + cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        check(prefs.edit().putString(name, Base64.encodeToString(bytes, Base64.NO_WRAP)).commit())
    }

    @Synchronized fun get(name: String): String? {
        val encoded = prefs.getString(name, null) ?: return null
        return try {
            val bytes = Base64.decode(encoded, Base64.NO_WRAP)
            require(bytes.size > 28)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
            cipher.updateAAD(name.toByteArray(Charsets.UTF_8))
            String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8)
        } catch (_: Exception) {
            remove(name)
            null
        }
    }

    @Synchronized fun remove(name: String) { check(prefs.edit().remove(name).commit()) }
    @Synchronized fun clearCustomers() {
        val edit = prefs.edit()
        prefs.all.keys.filter { it.startsWith("customer:") }.forEach(edit::remove)
        check(edit.commit())
    }
    companion object { private const val ALIAS = "laexquisssita.customer.v2" }
}
