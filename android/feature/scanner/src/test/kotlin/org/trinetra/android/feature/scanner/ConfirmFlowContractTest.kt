package org.trinetra.android.feature.scanner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.trinetra.android.core.model.ClinicalObservation
import org.trinetra.android.core.model.ReferenceSource
import org.trinetra.android.core.ocr.ExtractedObservationCandidate
import java.time.Instant

class ConfirmFlowContractTest {

    private fun candidateToPersistentObservation(
        candidate: ExtractedObservationCandidate,
        profileId: String,
        documentId: String,
        confirmedAtTimestamp: String?
    ): ClinicalObservation {
        // NN-4 Invariant enforcement: Cannot save without explicit user confirmation timestamp
        if (!candidate.isConfirmed || confirmedAtTimestamp == null) {
            throw IllegalStateException("NN-4 Violation: Attempted to persist unconfirmed OCR observation!")
        }

        return ClinicalObservation(
            id = candidate.id,
            profileId = profileId,
            documentId = documentId,
            loincCode = candidate.loincCode,
            nameAsPrinted = candidate.nameAsPrinted,
            valueNum = candidate.valueNum,
            valueText = candidate.valueText,
            unitUcum = candidate.unitUcum,
            refLow = candidate.refLow,
            refHigh = candidate.refHigh,
            refSource = ReferenceSource.REPORT,
            observedAt = Instant.now().toString(),
            confirmedByUserAt = confirmedAtTimestamp
        )
    }

    @Test
    fun unconfirmedCandidate_throwsIllegalStateExceptionOnSaveAttempt() {
        val candidate = ExtractedObservationCandidate(
            id = "cand-01",
            nameAsPrinted = "Hemoglobin",
            loincCode = "718-7",
            valueNum = 13.8,
            unitUcum = "g/dL",
            sourceSnippet = "Hemoglobin 13.8 g/dL",
            isConfirmed = false
        )

        assertThrows(IllegalStateException::class.java) {
            candidateToPersistentObservation(
                candidate = candidate,
                profileId = "prof-01",
                documentId = "doc-01",
                confirmedAtTimestamp = null
            )
        }
    }

    @Test
    fun confirmedCandidate_persistsWithConfirmationTimestamp() {
        val candidate = ExtractedObservationCandidate(
            id = "cand-02",
            nameAsPrinted = "HbA1c",
            loincCode = "4548-4",
            valueNum = 6.8,
            unitUcum = "%",
            sourceSnippet = "HbA1c 6.8 %",
            isConfirmed = true // Explicit user confirm
        )

        val timestamp = "2026-09-22T00:20:00Z"
        val persisted = candidateToPersistentObservation(
            candidate = candidate,
            profileId = "prof-01",
            documentId = "doc-01",
            confirmedAtTimestamp = timestamp
        )

        assertNotNull(persisted)
        assertEquals(timestamp, persisted.confirmedByUserAt)
        assertEquals(6.8, persisted.valueNum!!, 0.001)
        assertEquals("4548-4", persisted.loincCode)
    }
}
