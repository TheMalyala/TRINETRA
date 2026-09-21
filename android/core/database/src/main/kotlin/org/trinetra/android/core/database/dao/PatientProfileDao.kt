package org.trinetra.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.trinetra.android.core.database.entity.PatientProfileEntity

@Dao
interface PatientProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: PatientProfileEntity)

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getProfileById(id: String): PatientProfileEntity?

    @Query("SELECT * FROM profiles WHERE owner_user_id = :ownerUserId")
    fun getProfilesForUser(ownerUserId: String): Flow<List<PatientProfileEntity>>

    @Query("SELECT * FROM profiles")
    fun getAllProfiles(): Flow<List<PatientProfileEntity>>

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteProfile(id: String)
}
