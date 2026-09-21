package org.trinetra.android.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey
import org.trinetra.android.core.model.AdviceEntry
import org.trinetra.android.core.model.AdviceOrigin
import org.trinetra.android.core.model.ClinicalObservation
import org.trinetra.android.core.model.DoctorCard
import org.trinetra.android.core.model.DoctorVerificationStatus
import org.trinetra.android.core.model.DocumentType
import org.trinetra.android.core.model.HealthDocument
import org.trinetra.android.core.model.PatientProfile
import org.trinetra.android.core.model.ProfileRelationship
import org.trinetra.android.core.model.ReferenceSource

@Entity(tableName = "profiles")
data class PatientProfileEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "owner_user_id") val ownerUserId: String,
    val relationship: String,
    val name: String,
    @ColumnInfo(name = "date_of_birth") val dateOfBirth: String,
    val sex: String,
    @ColumnInfo(name = "blood_group") val bloodGroup: String,
    @ColumnInfo(name = "guardian_consent_id") val guardianConsentId: String? = null
) {
    fun toDomain(): PatientProfile = PatientProfile(
        id = id,
        ownerUserId = ownerUserId,
        relationship = ProfileRelationship.valueOf(relationship),
        name = name,
        dateOfBirth = dateOfBirth,
        sex = sex,
        bloodGroup = bloodGroup,
        guardianConsentId = guardianConsentId
    )

    companion object {
        fun fromDomain(p: PatientProfile): PatientProfileEntity = PatientProfileEntity(
            id = p.id,
            ownerUserId = p.ownerUserId,
            relationship = p.relationship.name,
            name = p.name,
            dateOfBirth = p.dateOfBirth,
            sex = p.sex,
            bloodGroup = p.bloodGroup,
            guardianConsentId = p.guardianConsentId
        )
    }
}

@Entity(tableName = "doctors")
data class DoctorCardEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String? = null,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "registration_no") val registrationNo: String? = null,
    val council: String? = null,
    @ColumnInfo(name = "verification_status") val verificationStatus: String,
    @ColumnInfo(name = "clinic_name") val clinicName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    @ColumnInfo(name = "specialties_delimited") val specialtiesDelimited: String = ""
) {
    fun toDomain(): DoctorCard = DoctorCard(
        id = id,
        userId = userId,
        displayName = displayName,
        registrationNo = registrationNo,
        council = council,
        verificationStatus = DoctorVerificationStatus.valueOf(verificationStatus),
        clinicName = clinicName,
        phone = phone,
        email = email,
        specialties = if (specialtiesDelimited.isBlank()) emptyList() else specialtiesDelimited.split("|")
    )

    companion object {
        fun fromDomain(d: DoctorCard): DoctorCardEntity = DoctorCardEntity(
            id = d.id,
            userId = d.userId,
            displayName = d.displayName,
            registrationNo = d.registrationNo,
            council = d.council,
            verificationStatus = d.verificationStatus.name,
            clinicName = d.clinicName,
            phone = d.phone,
            email = d.email,
            specialtiesDelimited = d.specialties.joinToString("|")
        )
    }
}

@Entity(
    tableName = "documents",
    indices = [
        Index(value = ["profile_id"]),
        Index(value = ["sha256"])
    ]
)
data class HealthDocumentEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "profile_id") val profileId: String,
    @ColumnInfo(name = "encounter_id") val encounterId: String? = null,
    @ColumnInfo(name = "doctor_id") val doctorId: String? = null,
    val type: String,
    val sha256: String,
    val phash: String? = null,
    @ColumnInfo(name = "storage_ref") val storageRef: String,
    @ColumnInfo(name = "ocr_text") val ocrText: String? = null,
    @ColumnInfo(name = "page_count") val pageCount: Int = 1,
    @ColumnInfo(name = "created_at") val createdAt: String,
    val version: Int = 1,
    @ColumnInfo(name = "supersedes_id") val supersedesId: String? = null
) {
    fun toDomain(): HealthDocument = HealthDocument(
        id = id,
        profileId = profileId,
        encounterId = encounterId,
        doctorId = doctorId,
        type = DocumentType.valueOf(type),
        sha256 = sha256,
        phash = phash,
        storageRef = storageRef,
        ocrText = ocrText,
        pageCount = pageCount,
        createdAt = createdAt,
        version = version,
        supersedesId = supersedesId
    )

    companion object {
        fun fromDomain(d: HealthDocument): HealthDocumentEntity = HealthDocumentEntity(
            id = d.id,
            profileId = d.profileId,
            encounterId = d.encounterId,
            doctorId = d.doctorId,
            type = d.type.name,
            sha256 = d.sha256,
            phash = d.phash,
            storageRef = d.storageRef,
            ocrText = d.ocrText,
            pageCount = d.pageCount,
            createdAt = d.createdAt,
            version = d.version,
            supersedesId = d.supersedesId
        )
    }
}

@Entity(
    tableName = "observations",
    indices = [
        Index(value = ["profile_id"]),
        Index(value = ["document_id"]),
        Index(value = ["loinc_code"])
    ]
)
data class ClinicalObservationEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "profile_id") val profileId: String,
    @ColumnInfo(name = "document_id") val documentId: String,
    @ColumnInfo(name = "loinc_code") val loincCode: String,
    @ColumnInfo(name = "name_as_printed") val nameAsPrinted: String,
    @ColumnInfo(name = "value_num") val valueNum: Double? = null,
    @ColumnInfo(name = "value_text") val valueText: String? = null,
    @ColumnInfo(name = "unit_ucum") val unitUcum: String,
    @ColumnInfo(name = "ref_low") val refLow: Double? = null,
    @ColumnInfo(name = "ref_high") val refHigh: Double? = null,
    @ColumnInfo(name = "ref_source") val refSource: String = "REPORT",
    @ColumnInfo(name = "observed_at") val observedAt: String,
    @ColumnInfo(name = "confirmed_by_user_at") val confirmedByUserAt: String? = null
) {
    fun toDomain(): ClinicalObservation = ClinicalObservation(
        id = id,
        profileId = profileId,
        documentId = documentId,
        loincCode = loincCode,
        nameAsPrinted = nameAsPrinted,
        valueNum = valueNum,
        valueText = valueText,
        unitUcum = unitUcum,
        refLow = refLow,
        refHigh = refHigh,
        refSource = ReferenceSource.valueOf(refSource),
        observedAt = observedAt,
        confirmedByUserAt = confirmedByUserAt
    )

    companion object {
        fun fromDomain(o: ClinicalObservation): ClinicalObservationEntity = ClinicalObservationEntity(
            id = o.id,
            profileId = o.profileId,
            documentId = o.documentId,
            loincCode = o.loincCode,
            nameAsPrinted = o.nameAsPrinted,
            valueNum = o.valueNum,
            valueText = o.valueText,
            unitUcum = o.unitUcum,
            refLow = o.refLow,
            refHigh = o.refHigh,
            refSource = o.refSource.name,
            observedAt = o.observedAt,
            confirmedByUserAt = o.confirmedByUserAt
        )
    }
}

@Entity(
    tableName = "advice_entries",
    indices = [
        Index(value = ["profile_id"]),
        Index(value = ["doctor_id"]),
        Index(value = ["hash"], unique = true)
    ]
)
data class AdviceEntryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "profile_id") val profileId: String,
    @ColumnInfo(name = "doctor_id") val doctorId: String,
    val body: String,
    @ColumnInfo(name = "tags_delimited") val tagsDelimited: String = "",
    @ColumnInfo(name = "follow_up_on") val followUpOn: String? = null,
    @ColumnInfo(name = "tests_ordered_delimited") val testsOrderedDelimited: String = "",
    val origin: String,
    val signature: String? = null,
    @ColumnInfo(name = "prev_hash") val prevHash: String,
    val hash: String,
    val version: Int = 1
) {
    fun toDomain(): AdviceEntry = AdviceEntry(
        id = id,
        profileId = profileId,
        doctorId = doctorId,
        body = body,
        tags = if (tagsDelimited.isBlank()) emptyList() else tagsDelimited.split("|"),
        followUpOn = followUpOn,
        testsOrdered = if (testsOrderedDelimited.isBlank()) emptyList() else testsOrderedDelimited.split("|"),
        origin = AdviceOrigin.valueOf(origin),
        signature = signature,
        prevHash = prevHash,
        hash = hash,
        version = version
    )

    companion object {
        fun fromDomain(a: AdviceEntry): AdviceEntryEntity = AdviceEntryEntity(
            id = a.id,
            profileId = a.profileId,
            doctorId = a.doctorId,
            body = a.body,
            tagsDelimited = a.tags.joinToString("|"),
            followUpOn = a.followUpOn,
            testsOrderedDelimited = a.testsOrdered.joinToString("|"),
            origin = a.origin.name,
            signature = a.signature,
            prevHash = a.prevHash,
            hash = a.hash,
            version = a.version
        )
    }
}

/**
 * FTS4 virtual table for fast full-text keyword indexing across records vault.
 */
@Entity(tableName = "documents_fts")
@Fts4
data class DocumentFtsEntity(
    val id: String,
    @ColumnInfo(name = "ocr_text") val ocrText: String?
)
