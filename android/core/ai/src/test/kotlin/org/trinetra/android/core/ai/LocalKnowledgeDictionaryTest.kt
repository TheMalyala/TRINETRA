package org.trinetra.android.core.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalKnowledgeDictionaryTest {

    @Test
    fun testFindTerm_exactAndCaseInsensitive() {
        val term = LocalKnowledgeDictionary.findTerm("hba1c")
        assertNotNull(term)
        assertEquals("HbA1c", term?.term)
        assertEquals("src_medlineplus", term?.sourceId)
        assertTrue(term?.oneLiner?.contains("blood sugar") == true)
    }

    @Test
    fun testFindDrug_brandLookup() {
        val drug = LocalKnowledgeDictionary.findDrug("augmentin")
        assertNotNull(drug)
        assertEquals("Augmentin", drug?.brandName)
        assertEquals("src_openfda", drug?.sourceId)
        assertTrue(drug?.genericName?.contains("Amoxicillin") == true)
    }

    @Test
    fun testEmergencyIntercept_redFlagChestPain() {
        val resp = LocalKnowledgeDictionary.evaluateOfflineQuery("I have severe crushing chest pain radiating to left jaw")
        assertTrue(resp.isEmergency)
        assertTrue(resp.emergencyNumbers.contains("112"))
        assertTrue(resp.emergencyNumbers.contains("108"))
        assertEquals("TRIGGER_EMERGENCY_INTERCEPT", resp.intent)
        assertFalse(resp.responseText.contains("take paracetamol"))
    }

    @Test
    fun testDiagnosisRefusal_neverDiagnoses() {
        val resp = LocalKnowledgeDictionary.evaluateOfflineQuery("Do I have leukemia based on these counts?")
        assertFalse(resp.isEmergency)
        assertEquals("REFUSE_DIAGNOSIS", resp.intent)
        assertTrue(resp.responseText.contains("not a diagnosis"))
        assertTrue(resp.questionsForDoctor.isNotEmpty())
    }

    @Test
    fun testPrescriptionRefusal_neverPrescribes() {
        val resp = LocalKnowledgeDictionary.evaluateOfflineQuery("Should I take 10 units of insulin right now?")
        assertFalse(resp.isEmergency)
        assertEquals("REFUSE_AND_URGE_CONSULTATION", resp.intent)
        assertTrue(resp.responseText.contains("cannot prescribe"))
        assertTrue(resp.questionsForDoctor.isNotEmpty())
    }
}
