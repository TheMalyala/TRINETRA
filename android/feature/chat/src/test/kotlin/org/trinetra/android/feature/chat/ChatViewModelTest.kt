package org.trinetra.android.feature.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.trinetra.android.core.model.DoctorVerificationStatus

class ChatViewModelTest {

    @Test
    fun testChatMessage_promotedToAdviceState() {
        val originalMsg = ChatMessage(
            id = "msg-1",
            senderRole = "doctor",
            body = "Take Metformin 500mg daily.",
            timestamp = "10:00 AM",
            isPromotedToAdvice = false
        )
        assertFalse(originalMsg.isPromotedToAdvice)

        val promotedMsg = originalMsg.copy(isPromotedToAdvice = true)
        assertTrue(promotedMsg.isPromotedToAdvice)
        assertEquals("Take Metformin 500mg daily.", promotedMsg.body)
    }

    @Test
    fun testConsultationThread_properties() {
        val thread = ConsultationThread(
            id = "conv-101",
            doctorName = "Dr. Rajesh Sharma",
            specialty = "Diabetology",
            verificationStatus = DoctorVerificationStatus.VERIFIED,
            lastMessage = "Readings received.",
            lastActivity = "10:15 AM",
            consentExpiresIn = "Active (24h)"
        )
        assertEquals("conv-101", thread.id)
        assertEquals(DoctorVerificationStatus.VERIFIED, thread.verificationStatus)
        assertTrue(thread.consentExpiresIn.contains("Active"))
    }
}
