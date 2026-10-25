package ind.finance.bourse.data.local

import android.content.Context
import android.util.Base64
import android.util.Log
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

    fun hasPin(): Boolean {
        if (!preferences.contains(PIN_VALUE)) return false
        val pin = getPin()
        return !pin.isNullOrBlank()
    }

    fun getPin(): String? {
        val encoded = preferences.getString(PIN_VALUE, null) ?: return null
        return try {
            val combined = Base64.decode(encoded, Base64.NO_WRAP)
            if (combined.size < 2) return null

            val ivLength = combined[0].toInt()
            if (ivLength <= 0 || combined.size < 1 + ivLength) return null

            val iv = combined.copyOfRange(1, 1 + ivLength)
            val ciphertext = combined.copyOfRange(1 + ivLength, combined.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getKey(), GCMParameterSpec(TAG_LENGTH, iv))
            val decryptedPin = String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8)
            decryptedPin
        } catch (e: Exception) {
            Log.e("TransactionPinStore", "Failed to decrypt PIN", e)
            null
        }
    }

    fun savePin(pin: String) {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getKey())
            val iv = cipher.iv
            val ciphertext = cipher.doFinal(pin.toByteArray(StandardCharsets.UTF_8))
            val combined = byteArrayOf(iv.size.toByte()) + iv + ciphertext
            val encoded = Base64.encodeToString(combined, Base64.NO_WRAP)
            preferences.edit().putString(PIN_VALUE, encoded).commit()
            Log.d("TransactionPinStore", "PIN saved successfully")
        } catch (e: Exception) {
            Log.e("TransactionPinStore", "Failed to save PIN", e)
        }
    }

    fun clearPin() {
        preferences.edit().remove(PIN_VALUE).commit()
    }

    private fun getKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null)
            if (entry is KeyStore.SecretKeyEntry) {
                return entry.secretKey
            }
            val key = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
            if (key != null) {
                return key
            }
        }

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
        const val TAG_LENGTH = 128
    }
}
