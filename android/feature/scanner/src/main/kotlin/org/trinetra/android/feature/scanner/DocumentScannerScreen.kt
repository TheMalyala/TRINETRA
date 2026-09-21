package org.trinetra.android.feature.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.trinetra.android.core.designsystem.theme.LocalTrinetraColors
import org.trinetra.android.core.model.DocumentType
import org.trinetra.android.core.ocr.ClinicalReportParser
import org.trinetra.android.core.ocr.ExtractedObservationCandidate
import org.trinetra.android.core.ui.components.TrinetraButton
import org.trinetra.android.core.ui.components.TrinetraOutlinedButton

private const val SAMPLE_CBC_OCR = """
APEX DIAGNOSTIC CLINICAL LABS
PATIENT REPORT - COMPLETE BLOOD COUNT
=====================================
Test Name              Result      Unit      Biological Reference Interval
Hemoglobin             13.8        g/dL      13.0 - 17.0
Total Leucocyte Count  8400        /uL       4000 - 11000
Platelet Count         240000      /uL       150000 - 450000
Fasting Blood Sugar    104         mg/dL     70.0 - 99.0
HbA1c                  6.8         %         4.0 - 5.6
Serum Creatinine       0.9         mg/dL     0.7 - 1.3
-------------------------------------
End of Report
"""

/**
 * Document Scanner screen providing camera capture and OCR triggering,
 * transitioning into the mandatory NN-4 confirmation review screen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DocumentScannerScreen(
    onSavedSuccessfully: (List<ExtractedObservationCandidate>, DocumentType) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTrinetraColors.current
    var selectedType by remember { mutableStateOf(DocumentType.LAB) }
    var extractedCandidates by remember { mutableStateOf<List<ExtractedObservationCandidate>?>(null) }

    if (extractedCandidates != null) {
        ConfirmObservationScreen(
            candidates = extractedCandidates!!,
            onConfirmAndSave = { approvedList ->
                onSavedSuccessfully(approvedList, selectedType)
            },
            onCancel = {
                extractedCandidates = null
            }
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "SCAN MEDICAL DOCUMENT",
                    color = colors.ink,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Capture prescriptions, lab reports, or discharge summaries with ML Kit on-device scanner.",
                    color = colors.inkMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Document Type Selection
                Text(
                    text = "DOCUMENT CATEGORY",
                    color = colors.inkMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DocumentType.entries.forEach { type ->
                        val isSelected = type == selectedType
                        Box(
                            modifier = Modifier
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) colors.ink else colors.line,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .background(if (isSelected) colors.surface2 else colors.surface, RoundedCornerShape(6.dp))
                                .clickable { selectedType = type }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = type.name,
                                color = colors.ink,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Camera Scanner Viewport Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .border(1.5.dp, colors.line, RoundedCornerShape(12.dp))
                        .background(colors.surface, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📷",
                            fontSize = 36.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ML Kit Document Scanner",
                            color = colors.ink,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Automatic edge detection & perspective warp",
                            color = colors.inkMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Actions
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TrinetraButton(
                    text = "Scan & Extract Report (Sample CBC)",
                    onClick = {
                        val parsed = ClinicalReportParser.parse(SAMPLE_CBC_OCR)
                        extractedCandidates = parsed
                    }
                )

                TrinetraOutlinedButton(
                    text = "Cancel",
                    onClick = onCancel
                )
            }
        }
    }
}
