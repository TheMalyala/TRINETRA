package org.trinetra.android.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.trinetra.android.core.designsystem.theme.LocalTrinetraColors

enum class ObservationStatus {
    ELEVATED,
    LOW,
    NORMAL,
    UNKNOWN
}

object ObservationLogic {
    fun calculateStatus(
        value: Double?,
        refLow: Double?,
        refHigh: Double?
    ): ObservationStatus {
        if (value == null) return ObservationStatus.UNKNOWN
        return when {
            refHigh != null && value > refHigh -> ObservationStatus.ELEVATED
            refLow != null && value < refLow -> ObservationStatus.LOW
            refLow != null || refHigh != null -> ObservationStatus.NORMAL
            else -> ObservationStatus.UNKNOWN
        }
    }

    fun statusGlyph(status: ObservationStatus): String {
        return when (status) {
            ObservationStatus.ELEVATED -> "▲"
            ObservationStatus.LOW -> "▼"
            ObservationStatus.NORMAL -> "•"
            ObservationStatus.UNKNOWN -> ""
        }
    }

    fun statusLabel(status: ObservationStatus): String {
        return when (status) {
            ObservationStatus.ELEVATED -> "HIGH"
            ObservationStatus.LOW -> "LOW"
            ObservationStatus.NORMAL -> "NORMAL"
            ObservationStatus.UNKNOWN -> ""
        }
    }
}

/**
 * Clinical observation numeric display adhering strictly to monochrome design rules.
 * Never conveys abnormality through red/green; uses bold tabular figures,
 * directional glyphs (▲/▼), and explicit text labels.
 */
@Composable
fun ObservationValueText(
    value: Double?,
    unit: String,
    refLow: Double? = null,
    refHigh: Double? = null,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    val colors = LocalTrinetraColors.current
    val status = ObservationLogic.calculateStatus(value, refLow, refHigh)
    val isAbnormal = status == ObservationStatus.ELEVATED || status == ObservationStatus.LOW
    val glyph = ObservationLogic.statusGlyph(status)
    val statusText = ObservationLogic.statusLabel(status)

    val formattedValue = value?.let {
        if (it % 1.0 == 0.0) it.toLong().toString() else "%.2f".format(it)
    } ?: "--"

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                color = colors.inkMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
        }
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = formattedValue,
                color = colors.ink,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isAbnormal) FontWeight.Bold else FontWeight.Normal,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = unit,
                color = colors.inkMuted,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.alignByBaseline()
            )
            if (glyph.isNotEmpty()) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$glyph $statusText".trim(),
                    color = colors.ink,
                    fontWeight = if (isAbnormal) FontWeight.Bold else FontWeight.Normal,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.alignByBaseline()
                )
            }
        }
        if (refLow != null || refHigh != null) {
            val refText = when {
                refLow != null && refHigh != null -> "Ref: $refLow - $refHigh $unit"
                refLow != null -> "Ref: > $refLow $unit"
                refHigh != null -> "Ref: < $refHigh $unit"
                else -> ""
            }
            Text(
                text = refText,
                color = colors.inkMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
