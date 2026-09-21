package org.trinetra.android.core.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.trinetra.android.core.model.ClinicalObservation
import org.trinetra.android.core.model.ReferenceSource

class TrendEngineTest {

    private fun createObs(
        value: Double,
        date: String,
        loinc: String = "4548-4",
        unit: String = "%",
        refLow: Double? = 4.0,
        refHigh: Double? = 5.6
    ): ClinicalObservation {
        return ClinicalObservation(
            id = "obs-$date",
            profileId = "prof-01",
            documentId = "doc-01",
            loincCode = loinc,
            nameAsPrinted = "Test Observation",
            valueNum = value,
            unitUcum = unit,
            refLow = refLow,
            refHigh = refHigh,
            refSource = ReferenceSource.REPORT,
            observedAt = date
        )
    }

    @Test
    fun hba1c_decreasingTowardsNormal_isImproving() {
        val obs1 = createObs(7.4, "2026-03-01T08:00:00Z")
        val obs2 = createObs(6.8, "2026-06-01T08:00:00Z")

        val trend = TrendEngine.analyzeTimeline(listOf(obs1, obs2), "4548-4", "HbA1c")

        assertEquals(TrendDirection.IMPROVING, trend.direction)
        assertEquals(-0.6, trend.deltaAbsolute!!, 0.001)
        assertEquals(-8.108, trend.deltaPercent!!, 0.01)
        assertEquals("▼", trend.glyph)
        assertTrue(trend.isOutOfRange) // 6.8 is still above 5.6
        assertTrue(trend.summaryText.contains("Decreased by 0.60 %"))
        assertTrue(trend.summaryText.contains("Clinical trajectory: Improving."))
    }

    @Test
    fun hba1c_increasing_isWorsening() {
        val obs1 = createObs(6.8, "2026-06-01T08:00:00Z")
        val obs2 = createObs(7.6, "2026-09-01T08:00:00Z")

        val trend = TrendEngine.analyzeTimeline(listOf(obs1, obs2), "4548-4", "HbA1c")

        assertEquals(TrendDirection.WORSENING, trend.direction)
        assertEquals(0.8, trend.deltaAbsolute!!, 0.001)
        assertEquals(11.764, trend.deltaPercent!!, 0.01)
        assertEquals("▲", trend.glyph)
        assertTrue(trend.isOutOfRange)
        assertTrue(trend.summaryText.contains("Clinical trajectory: Worsening"))
    }

    @Test
    fun hba1c_minimalChangeUnderThreshold_isStable() {
        val obs1 = createObs(6.80, "2026-06-01T08:00:00Z")
        val obs2 = createObs(6.85, "2026-09-01T08:00:00Z") // 0.05 / 6.8 = 0.73% (< 2.5%)

        val trend = TrendEngine.analyzeTimeline(listOf(obs1, obs2), "4548-4", "HbA1c")

        assertEquals(TrendDirection.STABLE, trend.direction)
        assertEquals("→", trend.glyph)
        assertTrue(trend.summaryText.contains("Clinical trajectory: Stable."))
    }

    @Test
    fun hemoglobin_recoveringFromAnemia_isImproving() {
        val obs1 = createObs(10.2, "2026-01-10T08:00:00Z", loinc = "718-7", unit = "g/dL", refLow = 13.0, refHigh = 17.0)
        val obs2 = createObs(13.5, "2026-04-10T08:00:00Z", loinc = "718-7", unit = "g/dL", refLow = 13.0, refHigh = 17.0)

        val trend = TrendEngine.analyzeTimeline(listOf(obs1, obs2), "718-7", "Hemoglobin")

        assertEquals(TrendDirection.IMPROVING, trend.direction)
        assertEquals(3.3, trend.deltaAbsolute!!, 0.001)
        assertEquals("▲", trend.glyph)
        assertFalse(trend.isOutOfRange) // 13.5 is within 13.0 - 17.0
        assertTrue(trend.summaryText.contains("Within normal reference interval."))
    }

    @Test
    fun platelets_droppingBelowNormal_isWorsening() {
        val obs1 = createObs(180000.0, "2026-01-01T08:00:00Z", loinc = "777-3", unit = "/uL", refLow = 150000.0, refHigh = 450000.0)
        val obs2 = createObs(110000.0, "2026-03-01T08:00:00Z", loinc = "777-3", unit = "/uL", refLow = 150000.0, refHigh = 450000.0)

        val trend = TrendEngine.analyzeTimeline(listOf(obs1, obs2), "777-3", "Platelet Count")

        assertEquals(TrendDirection.WORSENING, trend.direction)
        assertEquals(-70000.0, trend.deltaAbsolute!!, 0.001)
        assertEquals("▼", trend.glyph)
        assertTrue(trend.isOutOfRange)
    }

    @Test
    fun singleObservation_isInconclusive() {
        val obs1 = createObs(6.8, "2026-06-01T08:00:00Z")
        val trend = TrendEngine.analyzeTimeline(listOf(obs1), "4548-4", "HbA1c")

        assertEquals(TrendDirection.INCONCLUSIVE, trend.direction)
        assertEquals(1, trend.observationCount)
        assertEquals(null, trend.deltaAbsolute)
        assertEquals(null, trend.deltaPercent)
        assertTrue(trend.summaryText.contains("Baseline reading"))
    }

    @Test
    fun emptyObservations_returnsSafeInconclusive() {
        val trend = TrendEngine.analyzeTimeline(emptyList(), "4548-4", "HbA1c")

        assertEquals(TrendDirection.INCONCLUSIVE, trend.direction)
        assertEquals(0, trend.observationCount)
        assertEquals("No observations available for analysis.", trend.summaryText)
    }
}
