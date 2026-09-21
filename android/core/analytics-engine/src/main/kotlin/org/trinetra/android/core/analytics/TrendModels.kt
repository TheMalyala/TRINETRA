package org.trinetra.android.core.analytics

enum class TrendDirection {
    IMPROVING,
    STABLE,
    WORSENING,
    INCONCLUSIVE
}

enum class TrendPace {
    RAPID,
    MODERATE,
    STEADY
}

data class MedicationChangeOverlay(
    val date: String,
    val medicationName: String,
    val action: String, // e.g. "Started", "Dose Adjusted", "Discontinued"
    val dose: String
)

data class BiomarkerTrend(
    val loincCode: String,
    val biomarkerName: String,
    val currentValue: Double,
    val previousValue: Double?,
    val unit: String,
    val refLow: Double?,
    val refHigh: Double?,
    val deltaAbsolute: Double?,
    val deltaPercent: Double?,
    val direction: TrendDirection,
    val glyph: String,
    val isOutOfRange: Boolean,
    val summaryText: String,
    val observationCount: Int
)
