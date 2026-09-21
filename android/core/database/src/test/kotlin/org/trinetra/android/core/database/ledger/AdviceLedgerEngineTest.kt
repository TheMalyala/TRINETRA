package org.trinetra.android.core.database.ledger

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.trinetra.android.core.model.AdviceOrigin

class AdviceLedgerEngineTest {

    @Test
    fun genesisHash_is64Zeros() {
        assertEquals(64, AdviceLedgerEngine.GENESIS_HASH.length)
        assertEquals("0".repeat(64), AdviceLedgerEngine.GENESIS_HASH)
    }

    @Test
    fun canonicalize_ordersKeysAlphabetically() {
        val map = mapOf(
            "version" to 1,
            "doctor_id" to "doc-01",
            "body" to "Sample advice",
            "id" to "adv-01"
        )
        val canonical = AdviceLedgerEngine.canonicalize(map)
        assertEquals(
            """{"body": "Sample advice", "doctor_id": "doc-01", "id": "adv-01", "version": 1}""",
            canonical
        )
    }

    @Test
    fun chain_maintainsCryptographicIntegrity() {
        // 1. Genesis entry
        val entry1 = AdviceLedgerEngine.buildChainedEntry(
            id = "adv-001",
            profileId = "prof-01",
            doctorId = "doc-01",
            body = "Dietary advice: reduce sodium to < 2g per day.",
            prevHash = AdviceLedgerEngine.GENESIS_HASH,
            origin = AdviceOrigin.DOCTOR_SIGNED,
            signature = "sig_valid_01",
            tags = listOf("diet", "hypertension")
        )
        assertEquals(64, entry1.hash.length)

        // 2. Chained entry 2
        val entry2 = AdviceLedgerEngine.buildChainedEntry(
            id = "adv-002",
            profileId = "prof-01",
            doctorId = "doc-01",
            body = "Prescribed home blood pressure monitoring twice daily.",
            prevHash = entry1.hash,
            origin = AdviceOrigin.DOCTOR_SIGNED,
            signature = "sig_valid_02",
            tags = listOf("monitoring")
        )
        assertEquals(entry1.hash, entry2.prevHash)

        // 3. Chained entry 3 (Self reported)
        val entry3 = AdviceLedgerEngine.buildChainedEntry(
            id = "adv-003",
            profileId = "prof-01",
            doctorId = "doc-02",
            body = "Phone consultation: report BP log after 2 weeks.",
            prevHash = entry2.hash,
            origin = AdviceOrigin.SELF_REPORTED,
            followUpOn = "2026-10-15"
        )
        assertEquals(entry2.hash, entry3.prevHash)

        val chain = listOf(entry1, entry2, entry3)
        val verification = AdviceLedgerEngine.verifyChain(chain)
        assertTrue(verification is ChainVerificationResult.Valid)
        assertEquals(3, (verification as ChainVerificationResult.Valid).totalEntries)
    }

    @Test
    fun chain_detectsTamperedContent() {
        val entry1 = AdviceLedgerEngine.buildChainedEntry(
            id = "adv-001",
            profileId = "prof-01",
            doctorId = "doc-01",
            body = "Original advice text",
            prevHash = AdviceLedgerEngine.GENESIS_HASH
        )

        val entry2 = AdviceLedgerEngine.buildChainedEntry(
            id = "adv-002",
            profileId = "prof-01",
            doctorId = "doc-01",
            body = "Second advice text",
            prevHash = entry1.hash
        )

        // Attacker alters entry1 body
        val tamperedEntry1 = entry1.copy(body = "MALICIOUS ALTERATION")
        val tamperedChain = listOf(tamperedEntry1, entry2)

        val verification = AdviceLedgerEngine.verifyChain(tamperedChain)
        assertTrue(verification is ChainVerificationResult.Corrupted)
        val corrupted = verification as ChainVerificationResult.Corrupted
        assertEquals(0, corrupted.brokenIndex)
        assertTrue(corrupted.reason.contains("Hash mismatch"))
    }

    @Test
    fun chain_detectsBrokenHashLink() {
        val entry1 = AdviceLedgerEngine.buildChainedEntry(
            id = "adv-001",
            profileId = "prof-01",
            doctorId = "doc-01",
            body = "Advice 1",
            prevHash = AdviceLedgerEngine.GENESIS_HASH
        )

        val entry2 = AdviceLedgerEngine.buildChainedEntry(
            id = "adv-002",
            profileId = "prof-01",
            doctorId = "doc-01",
            body = "Advice 2",
            prevHash = "f".repeat(64) // Broken link
        )

        val chain = listOf(entry1, entry2)
        val verification = AdviceLedgerEngine.verifyChain(chain)
        assertTrue(verification is ChainVerificationResult.Corrupted)
        val corrupted = verification as ChainVerificationResult.Corrupted
        assertEquals(1, corrupted.brokenIndex)
        assertTrue(corrupted.reason.contains("Chain broken"))
    }
}
