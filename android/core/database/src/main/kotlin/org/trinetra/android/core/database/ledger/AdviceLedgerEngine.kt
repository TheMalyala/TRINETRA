package org.trinetra.android.core.database.ledger

import org.trinetra.android.core.model.AdviceEntry
import org.trinetra.android.core.model.AdviceOrigin
import org.trinetra.android.core.security.CryptoHelper
import java.util.TreeMap

sealed interface ChainVerificationResult {
    data class Valid(val totalEntries: Int) : ChainVerificationResult
    data class Corrupted(
        val brokenIndex: Int,
        val expectedHash: String,
        val actualHash: String,
        val reason: String
    ) : ChainVerificationResult
}

/**
 * Append-only Advice Ledger cryptographic engine adhering to:
 * - NN-7: Append-only, hash-chained advice ledger.
 * - ADR 009: Hash-Chained Audit Log & Advice Ledger.
 * - scripts/verify-chain.sh: 100% interoperable with verification script.
 */
object AdviceLedgerEngine {

    val GENESIS_HASH: String = "0".repeat(64)

    /**
     * Produces canonical JSON with sorted keys matching Python's json.dumps(body, sort_keys=True).
     */
    fun canonicalize(map: Map<String, Any?>): String {
        val sortedMap = TreeMap(map)
        val sb = StringBuilder("{")
        var first = true

        for ((key, value) in sortedMap) {
            if (!first) {
                sb.append(", ")
            }
            sb.append("\"").append(key).append("\": ")
            sb.append(formatJsonValue(value))
            first = false
        }
        sb.append("}")
        return sb.toString()
    }

    private fun formatJsonValue(value: Any?): String {
        return when (value) {
            null -> "null"
            is String -> "\"${escapeJson(value)}\""
            is Number, is Boolean -> value.toString()
            is List<*> -> {
                val items = value.joinToString(", ") { formatJsonValue(it) }
                "[$items]"
            }
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                canonicalize(value as Map<String, Any?>)
            }
            else -> "\"${escapeJson(value.toString())}\""
        }
    }

    private fun escapeJson(input: String): String {
        return input
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    /**
     * Extracts the canonical payload map for an AdviceEntry (excluding hash and signature).
     */
    fun entryToCanonicalPayload(entry: AdviceEntry): Map<String, Any?> {
        val payload = mutableMapOf<String, Any?>(
            "id" to entry.id,
            "profile_id" to entry.profileId,
            "doctor_id" to entry.doctorId,
            "body" to entry.body,
            "origin" to entry.origin.name,
            "prev_hash" to entry.prevHash,
            "version" to entry.version
        )
        if (entry.tags.isNotEmpty()) {
            payload["tags"] = entry.tags
        }
        if (!entry.followUpOn.isNullOrBlank()) {
            payload["follow_up_on"] = entry.followUpOn
        }
        if (entry.testsOrdered.isNotEmpty()) {
            payload["tests_ordered"] = entry.testsOrdered
        }
        return payload
    }

    /**
     * Calculates the SHA-256 hash for an entry given its prevHash and body.
     */
    fun computeHash(entry: AdviceEntry): String {
        val payload = entryToCanonicalPayload(entry)
        val canonical = canonicalize(payload)
        return CryptoHelper.sha256(entry.prevHash + canonical)
    }

    /**
     * Creates and signs/chains a new AdviceEntry.
     */
    fun buildChainedEntry(
        id: String,
        profileId: String,
        doctorId: String,
        body: String,
        prevHash: String,
        origin: AdviceOrigin = AdviceOrigin.SELF_REPORTED,
        signature: String? = null,
        tags: List<String> = emptyList(),
        followUpOn: String? = null,
        testsOrdered: List<String> = emptyList(),
        version: Int = 1
    ): AdviceEntry {
        val provisional = AdviceEntry(
            id = id,
            profileId = profileId,
            doctorId = doctorId,
            body = body,
            tags = tags,
            followUpOn = followUpOn,
            testsOrdered = testsOrdered,
            origin = origin,
            signature = signature,
            prevHash = prevHash,
            hash = "",
            version = version
        )
        val computedHash = computeHash(provisional)
        return provisional.copy(hash = computedHash)
    }

    /**
     * Verifies the cryptographic integrity of an entire advice ledger chain.
     */
    fun verifyChain(entries: List<AdviceEntry>): ChainVerificationResult {
        var prevHash = GENESIS_HASH

        for (idx in entries.indices) {
            val entry = entries[idx]
            if (entry.prevHash != prevHash) {
                return ChainVerificationResult.Corrupted(
                    brokenIndex = idx,
                    expectedHash = prevHash,
                    actualHash = entry.prevHash,
                    reason = "Chain broken: entry.prevHash does not match previous entry's hash."
                )
            }

            val expectedHash = computeHash(entry)
            if (entry.hash != expectedHash) {
                return ChainVerificationResult.Corrupted(
                    brokenIndex = idx,
                    expectedHash = expectedHash,
                    actualHash = entry.hash,
                    reason = "Hash mismatch: entry content was tampered or corrupted."
                )
            }
            prevHash = entry.hash
        }

        return ChainVerificationResult.Valid(totalEntries = entries.size)
    }

    /**
     * Exports a list of AdviceEntries to formatted JSON strictly compatible with scripts/verify-chain.sh.
     */
    fun exportToJson(entries: List<AdviceEntry>): String {
        val sb = StringBuilder("[\n")
        for (i in entries.indices) {
            val entry = entries[i]
            val payload = entryToCanonicalPayload(entry).toMutableMap()
            payload["hash"] = entry.hash
            if (entry.signature != null) {
                payload["signature"] = entry.signature
            }
            sb.append("  ").append(canonicalize(payload))
            if (i < entries.size - 1) {
                sb.append(",")
            }
            sb.append("\n")
        }
        sb.append("]\n")
        return sb.toString()
    }
}
