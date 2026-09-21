package org.trinetra.android.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class ProfileRelationship {
    SELF,
    CHILD,
    PARENT,
    OTHER
}

@Serializable
data class PatientProfile(
    val id: String,
    val ownerUserId: String,
    val relationship: ProfileRelationship,
    val name: String,
    val dateOfBirth: String,
    val sex: String,
    val bloodGroup: String,
    val guardianConsentId: String? = null
)
