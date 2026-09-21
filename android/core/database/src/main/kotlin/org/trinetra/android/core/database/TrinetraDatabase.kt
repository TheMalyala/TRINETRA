package org.trinetra.android.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SupportFactory
import org.trinetra.android.core.database.dao.AdviceLedgerDao
import org.trinetra.android.core.database.dao.ClinicalObservationDao
import org.trinetra.android.core.database.dao.DoctorCardDao
import org.trinetra.android.core.database.dao.HealthDocumentDao
import org.trinetra.android.core.database.dao.PatientProfileDao
import org.trinetra.android.core.database.entity.AdviceEntryEntity
import org.trinetra.android.core.database.entity.ClinicalObservationEntity
import org.trinetra.android.core.database.entity.DoctorCardEntity
import org.trinetra.android.core.database.entity.DocumentFtsEntity
import org.trinetra.android.core.database.entity.HealthDocumentEntity
import org.trinetra.android.core.database.entity.PatientProfileEntity

@Database(
    entities = [
        PatientProfileEntity::class,
        DoctorCardEntity::class,
        HealthDocumentEntity::class,
        ClinicalObservationEntity::class,
        AdviceEntryEntity::class,
        DocumentFtsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TrinetraDatabase : RoomDatabase() {

    abstract fun profileDao(): PatientProfileDao
    abstract fun doctorDao(): DoctorCardDao
    abstract fun documentDao(): HealthDocumentDao
    abstract fun observationDao(): ClinicalObservationDao
    abstract fun adviceLedgerDao(): AdviceLedgerDao

    companion object {
        private const val DB_NAME = "trinetra_vault.db"

        /**
         * Builds an encrypted SQLCipher Room database instance.
         * The database key is unwrapped from Android Keystore.
         */
        fun buildEncryptedDatabase(context: Context, passphrase: ByteArray): TrinetraDatabase {
            val factory = SupportFactory(passphrase)
            return Room.databaseBuilder(
                context.applicationContext,
                TrinetraDatabase::class.java,
                DB_NAME
            )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()
        }

        /**
         * In-memory database builder for local testing.
         */
        fun buildInMemory(context: Context): TrinetraDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                TrinetraDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()
        }
    }
}
