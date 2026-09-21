package org.trinetra.android.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class ReferenceSource {
    REPORT,
    GENERIC
}

@Serializable
data class ClinicalObservation(
    val id: String,
    val profileId: String,
    val documentId: String,
    val loincCode: String,
    val nameAsPrinted: String,
    val valueNum: Double? = null,
    val valueText: String? = null,
    val unitUcum: String,
    val refLow: Double? = null,
    val refHigh: Double? = null,
    val refSource: ReferenceSource = ReferenceSource.REPORT,
    val observedAt: String,
    val confirmedByUserAt: String? = null
)
