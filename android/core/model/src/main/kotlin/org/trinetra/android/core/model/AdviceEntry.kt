package org.trinetra.android.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class AdviceOrigin {
    SELF_REPORTED,
    DOCTOR_SIGNED
}

@Serializable
data class AdviceEntry(
    val id: String,
    val profileId: String,
    val doctorId: String,
    val body: String,
    val tags: List<String> = emptyList(),
    val followUpOn: String? = null,
    val testsOrdered: List<String> = emptyList(),
    val origin: AdviceOrigin = AdviceOrigin.SELF_REPORTED,
    val signature: String? = null,
    val prevHash: String,
    val hash: String,
    val version: Int = 1
)
