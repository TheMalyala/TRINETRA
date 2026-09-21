package org.trinetra.android.feature.scanner

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.trinetra.android.core.designsystem.theme.LocalTrinetraColors
import org.trinetra.android.core.ocr.ExtractedObservationCandidate
import org.trinetra.android.core.ui.components.ObservationValueText
import org.trinetra.android.core.ui.components.TrinetraButton
import org.trinetra.android.core.ui.components.TrinetraOutlinedButton

/**
 * Mandatory Confirm-Before-Save Screen satisfying:
 * - NN-4: OCR output is never saved without the user's explicit confirm step.
 * - Source crop/snippet is shown beside each candidate value for visual verification.
 * - Strict monochrome design language.
 */
@Composable
fun ConfirmObservationScreen(
    candidates: List<ExtractedObservationCandidate>,
    onConfirmAndSave: (List<ExtractedObservationCandidate>) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTrinetraColors.current
    val scrollState = rememberScrollState()

    // Track confirmed status per candidate id
    val confirmedMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            candidates.forEach { put(it.id, false) } // Default unconfirmed (NN-4)
        }
    }

    val confirmedCount = confirmedMap.count { it.value }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "VERIFY EXTRACTED VALUES",
            color = colors.ink,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Rule NN-4: Verify each clinical value against the scanned document text before saving to your encrypted vault.",
            color = colors.inkMuted,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Scrollable List of Candidates
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            candidates.forEachIndexed { index, candidate ->
                val isChecked = confirmedMap[candidate.id] == true

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isChecked) 1.5.dp else 1.dp,
                            color = if (isChecked) colors.ink else colors.line,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(colors.surface, RoundedCornerShape(8.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        // Top bar of card: LOINC badge & Confirmation Checkbox
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LOINC: ${candidate.loincCode}",
                                color = colors.inkMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            // Checkbox toggle
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        confirmedMap[candidate.id] = !isChecked
                                    }
                                    .padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .border(1.5.dp, colors.ink, RoundedCornerShape(4.dp))
                                        .background(if (isChecked) colors.ink else colors.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isChecked) {
                                        Text(
                                            text = "✓",
                                            color = colors.inverseInk,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isChecked) "Confirmed" else "Confirm",
                                    color = if (isChecked) colors.ink else colors.inkMuted,
                                    fontSize = 12.sp,
                                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Extracted Value with deterministic out-of-range indicator
                        ObservationValueText(
                            value = candidate.valueNum,
                            unit = candidate.unitUcum,
                            refLow = candidate.refLow,
                            refHigh = candidate.refHigh,
                            label = candidate.nameAsPrinted
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Source Crop / Snippet Box (NN-4 required)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, colors.line, RoundedCornerShape(4.dp))
                                .background(colors.surface2, RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = "RAW SCANNED SOURCE LINE:",
                                    color = colors.inkMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "\"${candidate.sourceSnippet}\"",
                                    color = colors.ink,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TrinetraButton(
                text = if (confirmedCount > 0) "Save $confirmedCount Confirmed to Vault" else "Select Items to Confirm (0)",
                enabled = confirmedCount > 0,
                onClick = {
                    val approved = candidates.filter { confirmedMap[it.id] == true }.map {
                        it.copy(isConfirmed = true)
                    }
                    onConfirmAndSave(approved)
                }
            )

            TrinetraOutlinedButton(
                text = "Cancel & Discard",
                onClick = onCancel
            )
        }
    }
}
