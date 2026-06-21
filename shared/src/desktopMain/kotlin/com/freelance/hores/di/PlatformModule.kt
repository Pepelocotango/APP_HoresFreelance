package com.freelance.hores.di

import androidx.room.Room
import com.freelance.hores.data.db.AppDatabase
import com.freelance.hores.data.db.instantiateImpl
import com.freelance.hores.data.db.getRoomDatabase
import com.freelance.hores.data.export.DesktopExportService
import com.freelance.hores.data.export.ExportService
import org.koin.dsl.module
import java.io.File

actual fun platformModule() = module {
    single<AppPrefs> { DesktopPrefs() }
    single {
        val dbFile = File(System.getProperty("user.home"), ".horesfreelance/hores_database.db")
        dbFile.parentFile?.mkdirs()
        getRoomDatabase(
            Room.databaseBuilder<AppDatabase>(
                name = dbFile.absolutePath,
                factory = { AppDatabase::class.instantiateImpl() }
            )
        )
    }
    single { get<AppDatabase>().diaDao() }
    single { get<AppDatabase>().concepteDao() }
    single { get<AppDatabase>().rangHorariDao() }
    single { get<AppDatabase>().clientDao() }
    single<ExportService> { DesktopExportService() }
}
