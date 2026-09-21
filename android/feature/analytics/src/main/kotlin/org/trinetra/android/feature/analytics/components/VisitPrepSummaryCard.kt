package org.trinetra.android.feature.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.trinetra.android.core.analytics.BiomarkerTrend
import org.trinetra.android.core.analytics.MedicationChangeOverlay
import org.trinetra.android.core.designsystem.theme.LocalTrinetraColors
import org.trinetra.android.core.ui.components.TrinetraOutlinedButton

/**
 * Visit-Prep Consultation Card complying with Hard Rule 1:
 * AI never diagnoses, prescribes, or changes dosage. It explains, summarises, and prepares questions.
 */
@Composable
fun VisitPrepSummaryCard(
    trend: BiomarkerTrend,
    medications: List<MedicationChangeOverlay> = emptyList(),
    onExportClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTrinetraColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colors.line, RoundedCornerShape(8.dp))
            .background(colors.surface, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DOCTOR VISIT-PREP BRIEF",
                    color = colors.ink,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Rule NN-1 Grounded",
                    color = colors.inkMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Summary Text
            Text(
                text = trend.summaryText,
                color = colors.ink,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            // Active Medications Section
            if (medications.isNotEmpty()) {
                Text(
                    text = "CURRENT REGIMEN:",
                    color = colors.inkMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                medications.forEach { med ->
                    Text(
                        text = "• ${med.medicationName} ${med.dose} (${med.action})",
                        color = colors.ink,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Suggested Questions to ask doctor
            Text(
                text = "QUESTIONS TO DISCUSS WITH YOUR DOCTOR:",
                color = colors.inkMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            val questions = listOf(
                "How does this current ${trend.biomarkerName} value align with our target management plan?",
                "Do my current medication dosages require any adjustment based on these timeline readings?",
                "When should I schedule the next follow-up laboratory test?"
            )
            questions.forEach { q ->
                Text(
                    text = "• $q",
                    color = colors.ink,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            TrinetraOutlinedButton(
                text = "Export Visit-Prep Summary (PDF/Text)",
                onClick = onExportClicked
            )
        }
    }
}
