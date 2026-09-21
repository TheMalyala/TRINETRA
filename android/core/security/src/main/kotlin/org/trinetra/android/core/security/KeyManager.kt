package org.trinetra.android.core.security

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

/**
 * Manages the cryptographic hierarchy per docs/security/key-management.md:
 * - Hardware-backed master key inside AndroidKeyStore (AES-256-GCM).
 * - Wraps/encrypts the 256-bit random SQLCipher database passphrase.
 */
class KeyManager(private val context: Context) {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val MASTER_KEY_ALIAS = "TrinetraMasterKey"
        private const val PREFS_NAME = "trinetra_sec_vault"
        private const val KEY_ENCRYPTED_DB_PASSPHRASE = "enc_db_passphrase"
        private const val KEY_DB_PASSPHRASE_IV = "db_passphrase_iv"
        private const val GCM_TAG_LENGTH = 128
        private const val DB_KEY_SIZE_BYTES = 32 // 256 bits
    }

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    /**
     * Retrieves or generates the 256-bit SQLCipher database passphrase.
     * The passphrase is saved locally encrypted by the Android Keystore master key.
     */
    @Synchronized
    fun getOrCreateDatabasePassphrase(): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encryptedBase64 = prefs.getString(KEY_ENCRYPTED_DB_PASSPHRASE, null)
        val ivBase64 = prefs.getString(KEY_DB_PASSPHRASE_IV, null)

        if (encryptedBase64 != null && ivBase64 != null) {
            val encryptedBytes = Base64.decode(encryptedBase64, Base64.DEFAULT)
            val iv = Base64.decode(ivBase64, Base64.DEFAULT)
            return decryptWithMasterKey(encryptedBytes, iv)
        }

        // Generate a new 256-bit cryptographically secure random key
        val secureRandom = SecureRandom()
        val newKey = ByteArray(DB_KEY_SIZE_BYTES)
        secureRandom.nextBytes(newKey)

        // Encrypt with Keystore master key and persist
        val (encryptedBytes, iv) = encryptWithMasterKey(newKey)
        prefs.edit()
            .putString(KEY_ENCRYPTED_DB_PASSPHRASE, Base64.encodeToString(encryptedBytes, Base64.DEFAULT))
            .putString(KEY_DB_PASSPHRASE_IV, Base64.encodeToString(iv, Base64.DEFAULT))
            .apply()

        return newKey
    }

    private fun getOrCreateMasterKey(): SecretKey {
        if (!keyStore.containsAlias(MASTER_KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val spec = KeyGenParameterSpec.Builder(
                MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(spec)
            return keyGenerator.generateKey()
        }
        return (keyStore.getEntry(MASTER_KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    private fun encryptWithMasterKey(plaintext: ByteArray): Pair<ByteArray, ByteArray> {
        val masterKey = getOrCreateMasterKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, masterKey)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext)
        return Pair(ciphertext, iv)
    }

    private fun decryptWithMasterKey(ciphertext: ByteArray, iv: ByteArray): ByteArray {
        val masterKey = getOrCreateMasterKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, masterKey, spec)
        return cipher.doFinal(ciphertext)
    }
}
