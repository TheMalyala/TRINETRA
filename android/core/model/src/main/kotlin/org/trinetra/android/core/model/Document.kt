package org.trinetra.android.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class DocumentType {
    LAB,
    IMAGING,
    PRESCRIPTION,
    DISCHARGE,
    BILL,
    OTHER
}

@Serializable
data class HealthDocument(
    val id: String,
    val profileId: String,
    val encounterId: String? = null,
    val doctorId: String? = null,
    val type: DocumentType,
    val sha256: String,
    val phash: String? = null,
    val storageRef: String,
    val ocrText: String? = null,
    val pageCount: Int = 1,
    val createdAt: String,
    val version: Int = 1,
    val supersedesId: String? = null
)
