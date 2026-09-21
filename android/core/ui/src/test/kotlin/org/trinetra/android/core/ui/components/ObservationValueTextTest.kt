package org.trinetra.android.core.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class ObservationValueTextTest {

    @Test
    fun calculateStatus_elevated_when_value_exceeds_refHigh() {
        val status = ObservationLogic.calculateStatus(
            value = 7.4,
            refLow = 4.0,
            refHigh = 5.6
        )
        assertEquals(ObservationStatus.ELEVATED, status)
        assertEquals("▲", ObservationLogic.statusGlyph(status))
        assertEquals("HIGH", ObservationLogic.statusLabel(status))
    }

    @Test
    fun calculateStatus_low_when_value_below_refLow() {
        val status = ObservationLogic.calculateStatus(
            value = 3.2,
            refLow = 4.0,
            refHigh = 5.6
        )
        assertEquals(ObservationStatus.LOW, status)
        assertEquals("▼", ObservationLogic.statusGlyph(status))
        assertEquals("LOW", ObservationLogic.statusLabel(status))
    }

    @Test
    fun calculateStatus_normal_when_value_within_interval() {
        val status = ObservationLogic.calculateStatus(
            value = 5.0,
            refLow = 4.0,
            refHigh = 5.6
        )
        assertEquals(ObservationStatus.NORMAL, status)
        assertEquals("•", ObservationLogic.statusGlyph(status))
        assertEquals("NORMAL", ObservationLogic.statusLabel(status))
    }

    @Test
    fun calculateStatus_unknown_when_value_is_null() {
        val status = ObservationLogic.calculateStatus(
            value = null,
            refLow = 4.0,
            refHigh = 5.6
        )
        assertEquals(ObservationStatus.UNKNOWN, status)
        assertEquals("", ObservationLogic.statusGlyph(status))
        assertEquals("", ObservationLogic.statusLabel(status))
    }

    @Test
    fun calculateStatus_normal_with_single_boundary() {
        val statusAboveMin = ObservationLogic.calculateStatus(
            value = 10.0,
            refLow = 5.0,
            refHigh = null
        )
        assertEquals(ObservationStatus.NORMAL, statusAboveMin)

        val statusBelowMin = ObservationLogic.calculateStatus(
            value = 3.0,
            refLow = 5.0,
            refHigh = null
        )
        assertEquals(ObservationStatus.LOW, statusBelowMin)
    }
}
