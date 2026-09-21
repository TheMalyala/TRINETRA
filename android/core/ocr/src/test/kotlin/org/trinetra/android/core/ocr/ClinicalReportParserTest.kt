package org.trinetra.android.core.ocr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClinicalReportParserTest {

    @Test
    fun parse_syntheticCbcReport_extractsAllKeyBiomarkers() {
        val ocrReportText = """
            APEX DIAGNOSTIC CLINICAL LABS
            PATIENT REPORT - COMPLETE BLOOD COUNT
            =====================================
            Test Name              Result      Unit      Biological Reference Interval
            Hemoglobin             13.8        g/dL      13.0 - 17.0
            Total Leucocyte Count  8400        /uL       4000 - 11000
            Platelet Count         240000      /uL       150000 - 450000
            -------------------------------------
            End of Report
        """.trimIndent()

        val results = ClinicalReportParser.parse(ocrReportText)
        assertEquals(3, results.size)

        // Verify Hemoglobin
        val hb = results.find { it.loincCode == "718-7" }
        assertNotNull(hb)
        assertEquals(13.8, hb!!.valueNum!!, 0.001)
        assertEquals("g/dL", hb.unitUcum)
        assertEquals(13.0, hb.refLow!!, 0.001)
        assertEquals(17.0, hb.refHigh!!, 0.001)
        assertFalse(hb.isConfirmed) // NN-4
        assertTrue(hb.sourceSnippet.contains("13.8"))

        // Verify TLC
        val tlc = results.find { it.loincCode == "6690-2" }
        assertNotNull(tlc)
        assertEquals(8400.0, tlc!!.valueNum!!, 0.001)
        assertEquals("/uL", tlc.unitUcum)
        assertEquals(4000.0, tlc.refLow!!, 0.001)
        assertEquals(11000.0, tlc.refHigh!!, 0.001)
        assertFalse(tlc.isConfirmed)

        // Verify Platelets
        val plt = results.find { it.loincCode == "777-3" }
        assertNotNull(plt)
        assertEquals(240000.0, plt!!.valueNum!!, 0.001)
        assertEquals("/uL", plt.unitUcum)
        assertEquals(150000.0, plt.refLow!!, 0.001)
        assertEquals(450000.0, plt.refHigh!!, 0.001)
        assertFalse(plt.isConfirmed)
    }

    @Test
    fun parse_diabeticPanel_extractsHbA1cAndGlucose() {
        val ocrReportText = """
            METABOLIC PROFILE
            Fasting Blood Sugar: 104 mg/dL (Ref: 70 - 99)
            HbA1c: 6.8 % (Ref: 4.0 - 5.6)
            Serum Creatinine: 0.9 mg/dL (Ref: 0.7 - 1.3)
            Total Cholesterol: 185 mg/dL (Ref: < 200)
        """.trimIndent()

        val results = ClinicalReportParser.parse(ocrReportText)
        assertEquals(4, results.size)

        val hba1c = results.find { it.loincCode == "4548-4" }
        assertNotNull(hba1c)
        assertEquals(6.8, hba1c!!.valueNum!!, 0.001)
        assertEquals("%", hba1c.unitUcum)
        assertEquals(4.0, hba1c.refLow!!, 0.001)
        assertEquals(5.6, hba1c.refHigh!!, 0.001)

        val fbs = results.find { it.loincCode == "1558-6" }
        assertNotNull(fbs)
        assertEquals(104.0, fbs!!.valueNum!!, 0.001)
        assertEquals("mg/dL", fbs.unitUcum)

        val cr = results.find { it.loincCode == "2160-0" }
        assertNotNull(cr)
        assertEquals(0.9, cr!!.valueNum!!, 0.001)

        val chol = results.find { it.loincCode == "2093-3" }
        assertNotNull(chol)
        assertEquals(185.0, chol!!.valueNum!!, 0.001)
        assertEquals(200.0, chol.refHigh!!, 0.001)
    }

    @Test
    fun duplicateDetection_identifiesIdenticalContent() {
        val bytes1 = "Sample medical report".toByteArray()
        val bytes2 = "Sample medical report".toByteArray()
        val bytes3 = "Different report".toByteArray()

        val hash1 = DocumentDuplicateDetector.computeSha256(bytes1)
        val hash2 = DocumentDuplicateDetector.computeSha256(bytes2)
        val hash3 = DocumentDuplicateDetector.computeSha256(bytes3)

        assertEquals(hash1, hash2)
        assertTrue(DocumentDuplicateDetector.isDuplicate(hash2, setOf(hash1)))
        assertFalse(DocumentDuplicateDetector.isDuplicate(hash3, setOf(hash1)))
    }
}
