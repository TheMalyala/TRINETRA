package org.trinetra.android.core.ocr

import java.security.MessageDigest

object DocumentDuplicateDetector {

    fun computeSha256(content: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(content)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun isDuplicate(
        candidateSha256: String,
        existingSha256Hashes: Set<String>
    ): Boolean {
        return existingSha256Hashes.contains(candidateSha256)
    }
}
