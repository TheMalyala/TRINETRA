package org.trinetra.android.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.trinetra.android.core.designsystem.theme.LocalTrinetraColors
import org.trinetra.android.core.model.AdviceEntry
import org.trinetra.android.core.model.AdviceOrigin

/**
 * Advice Ledger card implementing strict monochrome semantics:
 * - Solid card border: Doctor-signed consultation advice
 * - Dashed card border: Patient self-reported advice
 * Displays append-only SHA-256 hash provenance and structured clinical metadata.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdviceCard(
    entry: AdviceEntry,
    modifier: Modifier = Modifier,
    doctorName: String? = null
) {
    val colors = LocalTrinetraColors.current
    val isDoctorSigned = entry.origin == AdviceOrigin.DOCTOR_SIGNED
    val cardShape = RoundedCornerShape(8.dp)

    val borderModifier = if (isDoctorSigned) {
        Modifier.border(width = 1.5.dp, color = colors.ink, shape = cardShape)
    } else {
        Modifier.drawWithContent {
            drawContent()
            val strokeWidth = 1.5.dp.toPx()
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            drawRoundRect(
                color = colors.inkMuted,
                style = Stroke(width = strokeWidth, pathEffect = dashEffect)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(borderModifier)
            .background(colors.surface, cardShape)
            .padding(16.dp)
    ) {
        Column {
            // Header: Origin indicator & tamper-evident hash snippet
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val originLabel = if (isDoctorSigned) {
                    "● DOCTOR-SIGNED"
                } else {
                    "○ SELF-REPORTED"
                }
                Text(
                    text = originLabel,
                    color = colors.ink,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                val truncatedHash = if (entry.hash.length >= 8) "#${entry.hash.take(8)}" else "#${entry.hash}"
                Text(
                    text = truncatedHash,
                    color = colors.inkMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (doctorName != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Dr. $doctorName",
                    color = colors.ink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body
            Text(
                text = entry.body,
                color = colors.ink,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            // Diagnostic tests ordered if present
            if (entry.testsOrdered.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Tests Ordered: ${entry.testsOrdered.joinToString(", ")}",
                    color = colors.inkMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Follow up date if present
            if (!entry.followUpOn.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Follow-up: ${entry.followUpOn}",
                    color = colors.inkMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Tags flow row
            if (entry.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    entry.tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .border(1.dp, colors.line, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                color = colors.inkMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
