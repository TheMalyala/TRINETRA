package org.trinetra.android.core.security

import java.security.MessageDigest

/**
 * Pure cryptographic helper providing deterministic hashing.
 * NN-2: Numbers and cryptographic proofs come from code, never an LLM.
 */
object CryptoHelper {

    fun sha256(text: String): String {
        return sha256(text.toByteArray(Charsets.UTF_8))
    }

    fun sha256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
