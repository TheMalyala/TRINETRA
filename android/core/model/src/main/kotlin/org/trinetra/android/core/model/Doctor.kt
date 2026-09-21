package org.trinetra.android.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class DoctorVerificationStatus {
    UNVERIFIED,
    PENDING,
    VERIFIED,
    REJECTED
}

@Serializable
data class DoctorCard(
    val id: String,
    val userId: String? = null,
    val displayName: String,
    val registrationNo: String? = null,
    val council: String? = null,
    val verificationStatus: DoctorVerificationStatus = DoctorVerificationStatus.UNVERIFIED,
    val clinicName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val specialties: List<String> = emptyList()
)
