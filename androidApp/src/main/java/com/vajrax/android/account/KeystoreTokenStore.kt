package com.vajrax.android.account

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.vajrax.core.coroutines.AppDispatchers
import com.vajrax.core.log.VxLog
import com.vajrax.data.remote.TokenStore
import kotlinx.coroutines.withContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * The refresh token, encrypted with an AES-GCM key that never leaves the Android Keystore, in a
 * private preferences file kept out of device backups (the key can't move to another phone, so a
 * restored copy would be useless). Nothing secret is ever written in plain text.
 */
class KeystoreTokenStore(context: Context) : TokenStore {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    override suspend fun refreshToken(): String? = read(KEY_REFRESH)

    override suspend fun cachedAccount(): String? = read(KEY_ACCOUNT)

    private suspend fun read(key: String): String? = withContext(AppDispatchers.IO) {
        prefs.getString(key, null)?.let(::decrypt)
    }

    override suspend fun save(refreshToken: String?, accountJson: String) = withContext(AppDispatchers.IO) {
        prefs.edit().apply {
            if (refreshToken != null) putString(KEY_REFRESH, encrypt(refreshToken))
            putString(KEY_ACCOUNT, encrypt(accountJson))
        }.apply()
    }

    override suspend fun clear() = withContext(AppDispatchers.IO) { prefs.edit().clear().apply() }

    private fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, key()) }
        val sealed = cipher.iv + cipher.doFinal(plain.toByteArray())
        return Base64.encodeToString(sealed, Base64.NO_WRAP)
    }

    /** Unreadable (key gone after a reinstall or restore): treated as signed out. */
    private fun decrypt(stored: String): String? = runCatching {
        val bytes = Base64.decode(stored, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, bytes, 0, IV_BYTES))
        String(cipher.doFinal(bytes, IV_BYTES, bytes.size - IV_BYTES))
    }.onFailure {
        VxLog.w("Account", "Stored session unreadable; signing out", it)
        prefs.edit().clear().apply()
    }.getOrNull()

    private fun key(): SecretKey {
        val store = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (store.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE).apply {
            init(
                KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(KEY_BITS)
                    .build()
            )
        }.generateKey()
    }

    private companion object {
        const val PREFS = "vajrax_session"
        const val KEY_REFRESH = "refresh"
        const val KEY_ACCOUNT = "account"
        const val KEYSTORE = "AndroidKeyStore"
        const val ALIAS = "vajrax_session_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_BYTES = 12
        const val TAG_BITS = 128
        const val KEY_BITS = 256
    }
}
