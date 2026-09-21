package org.trinetra.android.feature.analytics.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.trinetra.android.core.analytics.MedicationChangeOverlay
import org.trinetra.android.core.designsystem.theme.LocalTrinetraColors
import org.trinetra.android.core.model.ClinicalObservation

/**
 * High-contrast monochrome clinical timeline chart complying with:
 * - NN-10 & NN-13: Meaning never conveyed by color alone.
 * - In-range points = Filled circles (●)
 * - Out-of-range points = Directional glyphs (▲/▼)
 * - Dashed horizontal lines = Reference interval edges
 * - Vertical dotted lines = Medication change overlays
 */
@Composable
fun BiomarkerTimelineView(
    observations: List<ClinicalObservation>,
    medicationChanges: List<MedicationChangeOverlay> = emptyList(),
    modifier: Modifier = Modifier
) {
    val colors = LocalTrinetraColors.current

    if (observations.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(colors.surface, RoundedCornerShape(8.dp))
                .border(1.dp, colors.line, RoundedCornerShape(8.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No timeline data available for this biomarker.",
                color = colors.inkMuted,
                fontSize = 13.sp
            )
        }
        return
    }

    val sortedObs = observations.sortedBy { it.observedAt }
    val values = sortedObs.mapNotNull { it.valueNum }
    val minVal = (values.minOrNull() ?: 0.0) * 0.9
    val maxVal = (values.maxOrNull() ?: 10.0) * 1.1
    val rangeSpan = if (maxVal - minVal > 0.001) maxVal - minVal else 1.0

    val refLow = sortedObs.firstOrNull()?.refLow
    val refHigh = sortedObs.firstOrNull()?.refHigh
    val unit = sortedObs.firstOrNull()?.unitUcum ?: ""

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colors.line, RoundedCornerShape(8.dp))
            .background(colors.surface, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        // Chart Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HISTORICAL TIMELINE (${sortedObs.size} points)",
                color = colors.inkMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            if (refLow != null || refHigh != null) {
                Text(
                    text = "Ref: ${refLow ?: ""}-${refHigh ?: ""} $unit",
                    color = colors.inkMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Canvas Drawing
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val padding = 20f

                val availableWidth = canvasWidth - (2 * padding)
                val availableHeight = canvasHeight - (2 * padding)

                val stepX = if (sortedObs.size > 1) {
                    availableWidth / (sortedObs.size - 1)
                } else {
                    availableWidth / 2
                }

                // 1. Draw Reference Range Dashed Lines if available
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)

                if (refHigh != null && refHigh in minVal..maxVal) {
                    val yHigh = canvasHeight - padding - (((refHigh - minVal) / rangeSpan) * availableHeight).toFloat()
                    drawLine(
                        color = colors.inkMuted,
                        start = Offset(padding, yHigh),
                        end = Offset(canvasWidth - padding, yHigh),
                        strokeWidth = 1.5f,
                        pathEffect = dashEffect
                    )
                }

                if (refLow != null && refLow in minVal..maxVal) {
                    val yLow = canvasHeight - padding - (((refLow - minVal) / rangeSpan) * availableHeight).toFloat()
                    drawLine(
                        color = colors.inkMuted,
                        start = Offset(padding, yLow),
                        end = Offset(canvasWidth - padding, yLow),
                        strokeWidth = 1.5f,
                        pathEffect = dashEffect
                    )
                }

                // 2. Draw Connecting Lines between points
                val points = sortedObs.mapIndexed { idx, obs ->
                    val x = padding + (idx * stepX)
                    val v = obs.valueNum ?: 0.0
                    val y = canvasHeight - padding - (((v - minVal) / rangeSpan) * availableHeight).toFloat()
                    Offset(x, y)
                }

                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = colors.ink,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 2.5f
                    )
                }

                // 3. Draw Points
                points.forEachIndexed { index, point ->
                    val obs = sortedObs[index]
                    val isAbnormal = (refHigh != null && (obs.valueNum ?: 0.0) > refHigh) ||
                        (refLow != null && (obs.valueNum ?: 0.0) < refLow)

                    if (isAbnormal) {
                        // Square/Diamond marker for out-of-range
                        drawCircle(
                            color = colors.ink,
                            radius = 6f,
                            center = point
                        )
                        drawCircle(
                            color = colors.background,
                            radius = 3f,
                            center = point
                        )
                    } else {
                        // Solid circle marker for normal points
                        drawCircle(
                            color = colors.ink,
                            radius = 5f,
                            center = point
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Data points table summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            sortedObs.takeLast(3).forEach { obs ->
                Column {
                    val dateLabel = obs.observedAt.take(10)
                    Text(
                        text = dateLabel,
                        color = colors.inkMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${obs.valueNum} $unit",
                        color = colors.ink,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Medication Overlays list if available
        if (medicationChanges.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.line, RoundedCornerShape(4.dp))
                    .background(colors.surface2, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "MEDICATION ADJUSTMENT OVERLAYS:",
                        color = colors.inkMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    medicationChanges.forEach { med ->
                        Text(
                            text = "• ${med.date}: ${med.medicationName} ${med.dose} (${med.action})",
                            color = colors.ink,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
