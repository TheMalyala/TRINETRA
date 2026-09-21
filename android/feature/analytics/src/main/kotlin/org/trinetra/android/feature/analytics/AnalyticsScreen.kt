package org.trinetra.android.feature.analytics

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import org.trinetra.android.core.analytics.MedicationChangeOverlay
import org.trinetra.android.core.analytics.TrendDirection
import org.trinetra.android.core.analytics.TrendEngine
import org.trinetra.android.core.designsystem.theme.LocalTrinetraColors
import org.trinetra.android.core.model.ClinicalObservation
import org.trinetra.android.core.model.ReferenceSource
import org.trinetra.android.feature.analytics.components.BiomarkerTimelineView
import org.trinetra.android.feature.analytics.components.VisitPrepSummaryCard

data class BiomarkerDefinition(
    val loincCode: String,
    val name: String,
    val unit: String,
    val refLow: Double?,
    val refHigh: Double?
)

private val TRACKED_BIOMARKERS = listOf(
    BiomarkerDefinition("4548-4", "HbA1c", "%", 4.0, 5.6),
    BiomarkerDefinition("1558-6", "Fasting Glucose", "mg/dL", 70.0, 99.0),
    BiomarkerDefinition("718-7", "Hemoglobin", "g/dL", 13.0, 17.0),
    BiomarkerDefinition("777-3", "Platelet Count", "/uL", 150000.0, 450000.0),
    BiomarkerDefinition("2160-0", "Serum Creatinine", "mg/dL", 0.7, 1.3)
)

private val SAMPLE_OBSERVATIONS = listOf(
    // HbA1c history
    ClinicalObservation("h1", "p1", "d1", "4548-4", "HbA1c", 7.6, null, "%", 4.0, 5.6, ReferenceSource.REPORT, "2026-01-10T09:00:00Z"),
    ClinicalObservation("h2", "p1", "d2", "4548-4", "HbA1c", 7.2, null, "%", 4.0, 5.6, ReferenceSource.REPORT, "2026-04-15T09:00:00Z"),
    ClinicalObservation("h3", "p1", "d3", "4548-4", "HbA1c", 6.8, null, "%", 4.0, 5.6, ReferenceSource.REPORT, "2026-08-15T09:00:00Z"),

    // Fasting Glucose history
    ClinicalObservation("g1", "p1", "d1", "1558-6", "Fasting Glucose", 135.0, null, "mg/dL", 70.0, 99.0, ReferenceSource.REPORT, "2026-01-10T09:00:00Z"),
    ClinicalObservation("g2", "p1", "d3", "1558-6", "Fasting Glucose", 104.0, null, "mg/dL", 70.0, 99.0, ReferenceSource.REPORT, "2026-08-15T09:00:00Z"),

    // Hemoglobin history
    ClinicalObservation("hb1", "p1", "d1", "718-7", "Hemoglobin", 11.2, null, "g/dL", 13.0, 17.0, ReferenceSource.REPORT, "2026-01-10T09:00:00Z"),
    ClinicalObservation("hb2", "p1", "d3", "718-7", "Hemoglobin", 13.8, null, "g/dL", 13.0, 17.0, ReferenceSource.REPORT, "2026-08-15T09:00:00Z"),

    // Platelets history
    ClinicalObservation("p1", "p1", "d3", "777-3", "Platelet Count", 240000.0, null, "/uL", 150000.0, 450000.0, ReferenceSource.REPORT, "2026-08-15T09:00:00Z"),

    // Creatinine history
    ClinicalObservation("c1", "p1", "d3", "2160-0", "Serum Creatinine", 0.9, null, "mg/dL", 0.7, 1.3, ReferenceSource.REPORT, "2026-08-15T09:00:00Z")
)

private val SAMPLE_MEDICATIONS = listOf(
    MedicationChangeOverlay("2026-01-15", "Metformin", "Started", "500mg daily"),
    MedicationChangeOverlay("2026-04-20", "Metformin", "Dose Adjusted", "1000mg daily")
)

/**
 * Analytics Dashboard Screen visualizing deterministic biomarker trends,
 * historical timeline charts, and doctor visit-prep consultation summaries.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyticsScreen(
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalTrinetraColors.current
    val scrollState = rememberScrollState()

    var selectedBiomarker by remember { mutableStateOf(TRACKED_BIOMARKERS.first()) }

    val observationsForSelected = SAMPLE_OBSERVATIONS.filter { it.loincCode == selectedBiomarker.loincCode }
    val trend = TrendEngine.analyzeTimeline(
        observations = observationsForSelected,
        loincCode = selectedBiomarker.loincCode,
        biomarkerName = selectedBiomarker.name
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BIOMARKER ANALYTICS",
                    color = colors.ink,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Deterministic trend computation (NN-2)",
                    color = colors.inkMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            if (onBack != null) {
                Box(
                    modifier = Modifier
                        .border(1.dp, colors.line, RoundedCornerShape(4.dp))
                        .clickable(onClick = onBack)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "✕ Close",
                        color = colors.ink,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Biomarker Selector Chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TRACKED_BIOMARKERS.forEach { def ->
                val isSelected = def.loincCode == selectedBiomarker.loincCode
                Box(
                    modifier = Modifier
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) colors.ink else colors.line,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .background(if (isSelected) colors.surface2 else colors.surface, RoundedCornerShape(6.dp))
                        .clickable { selectedBiomarker = def }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = def.name,
                        color = colors.ink,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Current Value & Trend Direction Badge Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.line, RoundedCornerShape(8.dp))
                .background(colors.surface, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedBiomarker.name.uppercase(),
                        color = colors.inkMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    // Trajectory badge
                    val directionLabel = when (trend.direction) {
                        TrendDirection.IMPROVING -> "IMPROVING ${trend.glyph}"
                        TrendDirection.WORSENING -> "WORSENING ${trend.glyph}"
                        TrendDirection.STABLE -> "STABLE ${trend.glyph}"
                        TrendDirection.INCONCLUSIVE -> "BASELINE"
                    }
                    Box(
                        modifier = Modifier
                            .border(1.dp, colors.ink, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = directionLabel,
                            color = colors.ink,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "%.2f".format(trend.currentValue),
                        color = colors.ink,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = trend.unit,
                        color = colors.inkMuted,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.alignByBaseline()
                    )
                    if (trend.deltaAbsolute != null) {
                        Spacer(modifier = Modifier.width(12.dp))
                        val sign = if (trend.deltaAbsolute >= 0) "+" else ""
                        Text(
                            text = "$sign%.2f (%s%.1f%%)".format(trend.deltaAbsolute, sign, trend.deltaPercent),
                            color = colors.ink,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                }

                if (selectedBiomarker.refLow != null || selectedBiomarker.refHigh != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ref: ${selectedBiomarker.refLow ?: ""}-${selectedBiomarker.refHigh ?: ""} ${trend.unit}",
                        color = colors.inkMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Timeline Visualization with Medication-change overlays
        BiomarkerTimelineView(
            observations = observationsForSelected,
            medicationChanges = if (selectedBiomarker.loincCode == "4548-4") SAMPLE_MEDICATIONS else emptyList()
        )

        // Visit-Prep Consultation Brief Card
        VisitPrepSummaryCard(
            trend = trend,
            medications = if (selectedBiomarker.loincCode == "4548-4") SAMPLE_MEDICATIONS else emptyList(),
            onExportClicked = {}
        )
    }
}
