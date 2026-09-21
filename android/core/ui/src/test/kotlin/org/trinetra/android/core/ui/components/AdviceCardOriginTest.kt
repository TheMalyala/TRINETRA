package org.trinetra.android.core.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.trinetra.android.core.model.AdviceEntry
import org.trinetra.android.core.model.AdviceOrigin

class AdviceCardOriginTest {

    @Test
    fun doctorSignedAdvice_maintainsIntegrityMetadata() {
        val entry = AdviceEntry(
            id = "adv-101",
            profileId = "prof-01",
            doctorId = "doc-01",
            body = "Reduce carbohydrate intake and exercise 30 minutes daily.",
            tags = listOf("lifestyle", "exercise"),
            followUpOn = "2026-10-01",
            testsOrdered = listOf("Fasting Blood Sugar"),
            origin = AdviceOrigin.DOCTOR_SIGNED,
            signature = "sig_valid_ecdsa",
            prevHash = "0".repeat(64),
            hash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        )

        assertEquals(AdviceOrigin.DOCTOR_SIGNED, entry.origin)
        assertTrue(entry.signature != null)
        assertEquals(64, entry.hash.length)
        assertEquals(64, entry.prevHash.length)
        assertEquals("e3b0c442", entry.hash.take(8))
    }

    @Test
    fun selfReportedAdvice_hasZeroSignature() {
        val entry = AdviceEntry(
            id = "adv-102",
            profileId = "prof-01",
            doctorId = "doc-02",
            body = "Doctor advised over phone to drink plenty of fluids.",
            origin = AdviceOrigin.SELF_REPORTED,
            signature = null,
            prevHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            hash = "ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb"
        )

        assertEquals(AdviceOrigin.SELF_REPORTED, entry.origin)
        assertEquals(null, entry.signature)
        assertEquals("ca978112", entry.hash.take(8))
    }
}
