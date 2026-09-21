package org.trinetra.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.trinetra.android.core.database.entity.ClinicalObservationEntity

@Dao
interface ClinicalObservationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(observation: ClinicalObservationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(observations: List<ClinicalObservationEntity>)

    @Query("SELECT * FROM observations WHERE id = :id")
    suspend fun getObservationById(id: String): ClinicalObservationEntity?

    @Query("SELECT * FROM observations WHERE profile_id = :profileId ORDER BY observed_at DESC")
    fun getObservationsForProfile(profileId: String): Flow<List<ClinicalObservationEntity>>

    @Query(
        """
        SELECT * FROM observations
        WHERE profile_id = :profileId AND loinc_code = :loincCode
        ORDER BY observed_at ASC
        """
    )
    fun getObservationsForBiomarker(profileId: String, loincCode: String): Flow<List<ClinicalObservationEntity>>

    @Query("SELECT DISTINCT loinc_code FROM observations WHERE profile_id = :profileId")
    fun getTrackedLoincCodes(profileId: String): Flow<List<String>>

    @Query("DELETE FROM observations WHERE id = :id")
    suspend fun deleteObservation(id: String)
}
