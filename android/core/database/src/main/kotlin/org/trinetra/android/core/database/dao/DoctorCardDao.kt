package org.trinetra.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.trinetra.android.core.database.entity.DoctorCardEntity

@Dao
interface DoctorCardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(doctor: DoctorCardEntity)

    @Query("SELECT * FROM doctors WHERE id = :id")
    suspend fun getDoctorById(id: String): DoctorCardEntity?

    @Query("SELECT * FROM doctors ORDER BY display_name ASC")
    fun getAllDoctors(): Flow<List<DoctorCardEntity>>

    @Query("SELECT * FROM doctors WHERE verification_status = :status")
    fun getDoctorsByStatus(status: String): Flow<List<DoctorCardEntity>>

    @Query("SELECT * FROM doctors WHERE specialties_delimited LIKE '%' || :specialty || '%'")
    fun searchBySpecialty(specialty: String): Flow<List<DoctorCardEntity>>

    @Query("DELETE FROM doctors WHERE id = :id")
    suspend fun deleteDoctor(id: String)
}
