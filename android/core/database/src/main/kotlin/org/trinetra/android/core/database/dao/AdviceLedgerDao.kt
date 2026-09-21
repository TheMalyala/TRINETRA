package org.trinetra.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.trinetra.android.core.database.entity.AdviceEntryEntity

/**
 * Append-only Advice Ledger DAO satisfying NN-7 and ADR 009:
 * Records are immutable once written; silent overwrites and hard deletes are disallowed.
 */
@Dao
interface AdviceLedgerDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun appendEntry(entry: AdviceEntryEntity)

    @Query("SELECT * FROM advice_entries WHERE id = :id")
    suspend fun getEntryById(id: String): AdviceEntryEntity?

    @Query(
        """
        SELECT * FROM advice_entries
        WHERE profile_id = :profileId
        ORDER BY rowid DESC
        LIMIT 1
        """
    )
    suspend fun getLatestEntry(profileId: String): AdviceEntryEntity?

    @Query(
        """
        SELECT * FROM advice_entries
        WHERE profile_id = :profileId
        ORDER BY rowid ASC
        """
    )
    suspend fun getChain(profileId: String): List<AdviceEntryEntity>

    @Query(
        """
        SELECT * FROM advice_entries
        WHERE profile_id = :profileId
        ORDER BY rowid DESC
        """
    )
    fun getAdviceFlow(profileId: String): Flow<List<AdviceEntryEntity>>

    @Query(
        """
        SELECT * FROM advice_entries
        WHERE profile_id = :profileId AND doctor_id = :doctorId
        ORDER BY rowid DESC
        """
    )
    fun getAdviceByDoctor(profileId: String, doctorId: String): Flow<List<AdviceEntryEntity>>
}
