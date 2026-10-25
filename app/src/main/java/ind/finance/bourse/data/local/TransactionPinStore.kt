package ind.finance.bourse.data.local

import android.content.Context
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class TransactionPinStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    init {
        context.getSharedPreferences("bourse_prefs", Context.MODE_PRIVATE)
            .edit()
            .remove("transaction_pin")
            .apply()
    }

    fun hasPin(): Boolean = preferences.contains(PIN_VALUE)

    fun getPin(): String? {
        val encoded = preferences.getString(PIN_VALUE, null) ?: return null
        return try {
            val encrypted = Base64.decode(encoded, Base64.DEFAULT)
            val iv = encrypted.copyOfRange(0, IV_LENGTH)
            val ciphertext = encrypted.copyOfRange(IV_LENGTH, encrypted.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getKey(), GCMParameterSpec(TAG_LENGTH, iv))
            String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    fun savePin(pin: String) {
        val iv = ByteArray(IV_LENGTH)
        java.security.SecureRandom().nextBytes(iv)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getKey(), GCMParameterSpec(TAG_LENGTH, iv))
        val ciphertext = cipher.doFinal(pin.toByteArray(StandardCharsets.UTF_8))
        val encrypted = Base64.encodeToString(iv + ciphertext, Base64.DEFAULT)
        preferences.edit().putString(PIN_VALUE, encrypted).apply()
    }

    private fun getKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing

        val generator = KeyGenerator.getInstance(KEY_ALGORITHM, ANDROID_KEYSTORE)
        generator.init(
            android.security.keystore.KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                    android.security.keystore.KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                .build()
        )
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALGORITHM = "AES"
        const val KEY_ALIAS = "bourse_transaction_pin"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val PREFERENCES = "bourse_secure_pin"
        const val PIN_VALUE = "encrypted_pin"
        const val IV_LENGTH = 12
        const val TAG_LENGTH = 128
    }
}
