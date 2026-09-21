package org.trinetra.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.trinetra.android.core.database.entity.DocumentFtsEntity
import org.trinetra.android.core.database.entity.HealthDocumentEntity

@Dao
interface HealthDocumentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(document: HealthDocumentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFts(documentFts: DocumentFtsEntity)

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: String): HealthDocumentEntity?

    @Query("SELECT * FROM documents WHERE profile_id = :profileId ORDER BY created_at DESC")
    fun getDocumentsForProfile(profileId: String): Flow<List<HealthDocumentEntity>>

    @Query("SELECT * FROM documents WHERE profile_id = :profileId AND type = :type ORDER BY created_at DESC")
    fun getDocumentsByType(profileId: String, type: String): Flow<List<HealthDocumentEntity>>

    @Query("SELECT * FROM documents WHERE sha256 = :sha256 LIMIT 1")
    suspend fun findBySha256(sha256: String): HealthDocumentEntity?

    /**
     * Local Full-Text Search (FTS4/5) querying document OCR text without unencrypted cloud transmission.
     */
    @Query(
        """
        SELECT documents.* FROM documents
        JOIN documents_fts ON documents.id = documents_fts.id
        WHERE documents_fts MATCH :query
        """
    )
    fun searchByOcrText(query: String): Flow<List<HealthDocumentEntity>>

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocument(id: String)
}
