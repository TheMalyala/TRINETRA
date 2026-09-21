package org.trinetra.android.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID
import org.trinetra.android.core.designsystem.theme.LocalTrinetraColors
import org.trinetra.android.core.model.DoctorVerificationStatus
import org.trinetra.android.core.ui.components.DoctorVerificationBadge
import org.trinetra.android.core.ui.components.TrinetraPrimaryButton

data class ChatMessage(
    val id: String,
    val senderRole: String, // "patient" or "doctor"
    val body: str_alias,
    val timestamp: String,
    val isPromotedToAdvice: Boolean = false,
    val attachments: List<String> = emptyList()
)

typealias str_alias = String

data class ConsultationThread(
    val id: String,
    val doctorName: String,
    val specialty: String,
    val verificationStatus: DoctorVerificationStatus,
    val lastMessage: String,
    val lastActivity: String,
    val consentExpiresIn: String
)

@Composable
fun ConsultationsChatScreen(
    modifier: Modifier = Modifier,
    onNavigateToAdviceLedger: () -> Unit = {}
) {
    val colors = LocalTrinetraColors.current

    var activeConversationId by remember { mutableStateOf<String?>(null) }
    var inputText by remember { mutableStateOf("") }

    val threads = remember {
        listOf(
            ConsultationThread(
                id = "conv-1",
                doctorName = "Dr. Rajesh Sharma",
                specialty = "Diabetology · Apollo Clinic",
                verificationStatus = DoctorVerificationStatus.VERIFIED,
                lastMessage = "Increase Metformin to 1000mg with breakfast. Log readings daily.",
                lastActivity = "Today, 10:30 AM",
                consentExpiresIn = "Active (22h left)"
            ),
            ConsultationThread(
                id = "conv-2",
                doctorName = "Dr. Sunita Verma",
                specialty = "General Medicine",
                verificationStatus = DoctorVerificationStatus.UNVERIFIED,
                lastMessage = "Your CBC report shows hemoglobin of 13.8 g/dL, which is normal.",
                lastActivity = "Yesterday",
                consentExpiresIn = "Active (5d left)"
            )
        )
    }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                id = "msg-1",
                senderRole = "patient",
                body = "Hello Doctor, my morning fasting blood sugar was 132 mg/dL. Should we adjust anything?",
                timestamp = "10:15 AM"
            ),
            ChatMessage(
                id = "msg-2",
                senderRole = "doctor",
                body = "Increase Metformin to 1000mg with breakfast. Log readings daily for 1 week.",
                timestamp = "10:30 AM",
                isPromotedToAdvice = false,
                attachments = listOf("Prescription Crop")
            )
        )
    }

    if (activeConversationId == null) {
        // Thread List View
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "CONSULTATIONS",
                        color = colors.ink,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Time-boxed, consent-scoped doctor messaging with official Advice Ledger promotion.",
                        color = colors.inkMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            items(threads) { thread ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.line, RoundedCornerShape(8.dp))
                        .background(colors.surface, RoundedCornerShape(8.dp))
                        .clickable { activeConversationId = thread.id }
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = thread.doctorName,
                                color = colors.ink,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            DoctorVerificationBadge(status = thread.verificationStatus)
                        }

                        Text(
                            text = thread.specialty,
                            color = colors.inkMuted,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = thread.lastMessage,
                            color = colors.ink,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .border(1.dp, colors.line, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = thread.consentExpiresIn,
                                    color = colors.inkMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = thread.lastActivity,
                                color = colors.inkMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Active Conversation View
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .border(1.dp, colors.line)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clickable { activeConversationId = null }
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = "← Back",
                        color = colors.ink,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dr. Rajesh Sharma",
                        color = colors.ink,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Active Consent · 22h remaining",
                        color = colors.inkMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                DoctorVerificationBadge(status = DoctorVerificationStatus.VERIFIED)
            }

            // Message Bubble Stream
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    val isPatient = msg.senderRole == "patient"
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isPatient) Alignment.End else Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .border(
                                    width = 1.dp,
                                    color = if (isPatient) colors.ink else colors.line,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(
                                    color = if (isPatient) colors.surface else colors.surface,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isPatient) "YOU" else "DR. RAJESH SHARMA",
                                        color = colors.inkMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = msg.timestamp,
                                        color = colors.inkMuted,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Text(
                                    text = msg.body,
                                    color = colors.ink,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )

                                if (msg.attachments.isNotEmpty()) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        msg.attachments.forEach { att ->
                                            Box(
                                                modifier = Modifier
                                                    .border(1.dp, colors.line, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "📎 $att",
                                                    color = colors.ink,
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                }

                                // Promote to Advice Section (NN-7, ADR 004)
                                if (!isPatient) {
                                    if (msg.isPromotedToAdvice) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, colors.ink, RoundedCornerShape(4.dp))
                                                .padding(6.dp)
                                        ) {
                                            Text(
                                                text = "✦ PROMOTED TO ADVICE LEDGER (HASH-CHAINED)",
                                                color = colors.ink,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .border(1.dp, colors.ink, RoundedCornerShape(4.dp))
                                                .clickable {
                                                    val index = messages.indexOf(msg)
                                                    if (index != -1) {
                                                        messages[index] = msg.copy(isPromotedToAdvice = true)
                                                    }
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "+ PROMOTE TO ADVICE LEDGER",
                                                color = colors.ink,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .border(1.dp, colors.line)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(text = "Message Dr. Sharma...", color = colors.inkMuted, fontSize = 13.sp)
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.ink,
                        unfocusedBorderColor = colors.line,
                        focusedTextColor = colors.ink,
                        unfocusedTextColor = colors.ink
                    ),
                    shape = RoundedCornerShape(6.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                TrinetraPrimaryButton(
                    text = "Send",
                    onClick = {
                        if (inputText.isNotBlank()) {
                            messages.add(
                                ChatMessage(
                                    id = UUID.randomUUID().toString(),
                                    senderRole = "patient",
                                    body = inputText,
                                    timestamp = "Now"
                                )
                            )
                            inputText = ""
                        }
                    },
                    modifier = Modifier.height(48.dp)
                )
            }
        }
    }
}
