package com.freelance.hores.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
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
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diaDao(): DiaDao
    abstract fun concepteDao(): ConcepteDao
    abstract fun rangHorariDao(): RangHorariDao
    abstract fun clientDao(): ClientDao
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
    migrations: Array<Migration> = emptyArray()
): AppDatabase {
    return builder
        .addMigrations(*migrations)
        .setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver())
        .setQueryCoroutineContext(kotlinx.coroutines.Dispatchers.IO)
        .build()
}
