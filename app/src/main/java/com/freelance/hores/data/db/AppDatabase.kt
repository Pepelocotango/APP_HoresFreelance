package com.freelance.hores.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.freelance.hores.data.db.dao.ClientDao
import com.freelance.hores.data.db.dao.ConcepteDao
import com.freelance.hores.data.db.dao.DiaDao
import com.freelance.hores.data.db.dao.RangHorariDao
import com.freelance.hores.data.db.entity.ClientEntity
import com.freelance.hores.data.db.entity.ConcepteEntity
import com.freelance.hores.data.db.entity.DiaEntity
import com.freelance.hores.data.db.entity.RangHorariEntity

@Database(
    entities = [DiaEntity::class, ConcepteEntity::class, RangHorariEntity::class, ClientEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diaDao(): DiaDao
    abstract fun concepteDao(): ConcepteDao
    abstract fun rangHorariDao(): RangHorariDao
    abstract fun clientDao(): ClientDao

    companion object {
        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE conceptes ADD COLUMN estat TEXT NOT NULL DEFAULT 'PENDENT'")
                db.execSQL("ALTER TABLE conceptes ADD COLUMN despeses REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE conceptes ADD COLUMN despesesNotes TEXT NOT NULL DEFAULT ''")
            }
        }

        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hores_database"
                )
                .addMigrations(MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        db.execSQL("PRAGMA foreign_keys=ON;")
                    }
                })
                .build()
                Instance = instance
                instance
            }
        }

        fun resetInstance() {
            Instance = null
        }
    }
}
