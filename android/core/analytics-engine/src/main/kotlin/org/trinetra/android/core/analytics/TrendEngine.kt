package org.trinetra.android.core.analytics

import org.trinetra.android.core.model.ClinicalObservation
import kotlin.math.abs

/**
 * Pure Kotlin Deterministic Trend Engine adhering to:
 * - NN-2: Numbers come from deterministic code, never an LLM.
 * - ADR 005: 100% reproducible, unit-testable clinical metrics.
 * - Meaning never conveyed by color alone.
 */
object TrendEngine {

    private const val STABILITY_THRESHOLD_PERCENT = 2.5

    fun analyzeTimeline(
        observations: List<ClinicalObservation>,
        loincCode: String,
        biomarkerName: String
    ): BiomarkerTrend {
        val validObs = observations
            .filter { it.loincCode == loincCode && it.valueNum != null }
            .sortedBy { it.observedAt }

        if (validObs.isEmpty()) {
            return BiomarkerTrend(
                loincCode = loincCode,
                biomarkerName = biomarkerName,
                currentValue = 0.0,
                previousValue = null,
                unit = "",
                refLow = null,
                refHigh = null,
                deltaAbsolute = null,
                deltaPercent = null,
                direction = TrendDirection.INCONCLUSIVE,
                glyph = "",
                isOutOfRange = false,
                summaryText = "No observations available for analysis.",
                observationCount = 0
            )
        }

        val latest = validObs.last()
        val currentVal = latest.valueNum!!
        val unit = latest.unitUcum
        val refLow = latest.refLow
        val refHigh = latest.refHigh

        val isOutOfRange = (refLow != null && currentVal < refLow) || (refHigh != null && currentVal > refHigh)

        if (validObs.size == 1) {
            val glyph = when {
                refHigh != null && currentVal > refHigh -> "▲"
                refLow != null && currentVal < refLow -> "▼"
                else -> "•"
            }
            val rangeStatus = if (isOutOfRange) "outside normal range" else "within normal range"
            return BiomarkerTrend(
                loincCode = loincCode,
                biomarkerName = biomarkerName,
                currentValue = currentVal,
                previousValue = null,
                unit = unit,
                refLow = refLow,
                refHigh = refHigh,
                deltaAbsolute = null,
                deltaPercent = null,
                direction = TrendDirection.INCONCLUSIVE,
                glyph = glyph,
                isOutOfRange = isOutOfRange,
                summaryText = "Baseline reading: $currentVal $unit ($rangeStatus).",
                observationCount = 1
            )
        }

        val previous = validObs[validObs.size - 2]
        val prevVal = previous.valueNum!!

        val deltaAbs = currentVal - prevVal
        val deltaPct = if (prevVal != 0.0) (deltaAbs / prevVal) * 100.0 else 0.0

        val direction = evaluateDirection(loincCode, currentVal, prevVal, deltaPct, refLow, refHigh)

        val glyph = when {
            abs(deltaPct) < STABILITY_THRESHOLD_PERCENT -> "→"
            deltaAbs > 0 -> "▲"
            else -> "▼"
        }

        val summary = buildSummary(
            biomarkerName = biomarkerName,
            currentVal = currentVal,
            deltaAbs = deltaAbs,
            deltaPct = deltaPct,
            unit = unit,
            direction = direction,
            isOutOfRange = isOutOfRange,
            refLow = refLow,
            refHigh = refHigh
        )

        return BiomarkerTrend(
            loincCode = loincCode,
            biomarkerName = biomarkerName,
            currentValue = currentVal,
            previousValue = prevVal,
            unit = unit,
            refLow = refLow,
            refHigh = refHigh,
            deltaAbsolute = deltaAbs,
            deltaPercent = deltaPct,
            direction = direction,
            glyph = glyph,
            isOutOfRange = isOutOfRange,
            summaryText = summary,
            observationCount = validObs.size
        )
    }

    private fun evaluateDirection(
        loincCode: String,
        current: Double,
        previous: Double,
        deltaPercent: Double,
        refLow: Double?,
        refHigh: Double?
    ): TrendDirection {
        if (abs(deltaPercent) < STABILITY_THRESHOLD_PERCENT) {
            return TrendDirection.STABLE
        }

        return when (loincCode) {
            // Lower-is-better metrics (Diabetes, Lipids, Renal waste)
            "4548-4", // HbA1c
            "1558-6", // Fasting Blood Glucose
            "2093-3", // Cholesterol
            "2160-0"  // Creatinine
            -> {
                if (current < previous) TrendDirection.IMPROVING else TrendDirection.WORSENING
            }

            // Hematology (Hemoglobin, Platelets) where low values are common concerns
            "718-7", // Hemoglobin
            "777-3"  // Platelet Count
            -> {
                val wasLow = refLow != null && previous < refLow
                val isLow = refLow != null && current < refLow
                when {
                    wasLow && current > previous -> TrendDirection.IMPROVING
                    isLow && current < previous -> TrendDirection.WORSENING
                    !wasLow && !isLow -> TrendDirection.STABLE
                    current > previous -> TrendDirection.IMPROVING
                    else -> TrendDirection.WORSENING
                }
            }

            else -> {
                // Default: moving towards normal range is IMPROVING
                val prevDistanceToRange = distanceToRange(previous, refLow, refHigh)
                val currDistanceToRange = distanceToRange(current, refLow, refHigh)
                when {
                    currDistanceToRange < prevDistanceToRange -> TrendDirection.IMPROVING
                    currDistanceToRange > prevDistanceToRange -> TrendDirection.WORSENING
                    else -> TrendDirection.STABLE
                }
            }
        }
    }

    private fun distanceToRange(value: Double, refLow: Double?, refHigh: Double?): Double {
        return when {
            refLow != null && value < refLow -> refLow - value
            refHigh != null && value > refHigh -> value - refHigh
            else -> 0.0
        }
    }

    private fun buildSummary(
        biomarkerName: String,
        currentVal: Double,
        deltaAbs: Double,
        deltaPct: Double,
        unit: String,
        direction: TrendDirection,
        isOutOfRange: Boolean,
        refLow: Double?,
        refHigh: Double?
    ): String {
        val sign = if (deltaAbs >= 0) "+" else ""
        val absFormatted = "%.2f".format(deltaAbs)
        val pctFormatted = "%.1f".format(abs(deltaPct))

        val changeDesc = if (direction == TrendDirection.STABLE) {
            "Stable with slight change of $sign$absFormatted $unit"
        } else if (deltaAbs > 0) {
            "Increased by $sign$absFormatted $unit (+$pctFormatted%)"
        } else {
            "Decreased by $absFormatted $unit (-$pctFormatted%)"
        }

        val rangeDesc = if (isOutOfRange) {
            val refText = when {
                refLow != null && refHigh != null -> "$refLow - $refHigh $unit"
                refLow != null -> "> $refLow $unit"
                refHigh != null -> "< $refHigh $unit"
                else -> ""
            }
            "Remains outside standard reference ($refText)."
        } else {
            "Within normal reference interval."
        }

        val directionDesc = when (direction) {
            TrendDirection.IMPROVING -> "Clinical trajectory: Improving."
            TrendDirection.WORSENING -> "Clinical trajectory: Worsening. Discuss with doctor."
            TrendDirection.STABLE -> "Clinical trajectory: Stable."
            TrendDirection.INCONCLUSIVE -> ""
        }

        return "$changeDesc from previous reading. $rangeDesc $directionDesc".trim()
    }
}
