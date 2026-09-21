package org.trinetra.android.feature.decoder

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.trinetra.android.core.ai.DefaultNetraRepository

class DecoderStateTest {

    private val repository = DefaultNetraRepository()

    @Test
    fun testDecodeKnownTerm_returnsMedlinePlusSource() = runBlocking {
        val term = repository.decodeTerm("HbA1c")
        assertEquals("HbA1c", term.term)
        assertEquals("src_medlineplus", term.sourceId)
        assertTrue(term.plainExplanation.isNotEmpty())
    }

    @Test
    fun testQueryRefusal_doesNotDiagnose() = runBlocking {
        val response = repository.queryNetra("Do I have diabetes?")
        assertFalse(response.isEmergency)
        assertEquals("REFUSE_DIAGNOSIS", response.intent)
        assertTrue(response.disclaimerIncluded)
        assertTrue(response.questionsForDoctor.isNotEmpty())
    }

    @Test
    fun testEmergencyQuery_triggersEmergencyFlag() = runBlocking {
        val response = repository.queryNetra("I have severe crushing chest pain")
        assertTrue(response.isEmergency)
        assertEquals("TRIGGER_EMERGENCY_INTERCEPT", response.intent)
        assertTrue(response.emergencyNumbers.contains("112"))
    }
}
