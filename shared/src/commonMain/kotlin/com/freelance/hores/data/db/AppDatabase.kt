package com.freelance.hores.data.db
import com.freelance.hores.data.db.dao.*
import com.freelance.hores.data.db.entity.*
@Database(entities = [DiaEntity::class, ConcepteEntity::class, RangHorariEntity::class, ClientEntity::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
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
        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE conceptes ADD COLUMN esPreuFix INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE conceptes ADD COLUMN importPreuFix REAL NOT NULL DEFAULT 0.0")
            }
        }
    }
}
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
fun getRoomDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder.addMigrations(AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_4_5)
        .setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver())
        .setQueryCoroutineContext(kotlinx.coroutines.Dispatchers.IO).build()
}
