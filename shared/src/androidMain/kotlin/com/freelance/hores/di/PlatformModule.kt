package com.freelance.hores.di

import androidx.room.Room
import com.freelance.hores.data.db.AppDatabase
import com.freelance.hores.data.db.instantiateImpl
import com.freelance.hores.data.db.ANDROID_DATABASE_MIGRATIONS
import com.freelance.hores.data.db.getRoomDatabase
import com.freelance.hores.data.export.AndroidExportService
import com.freelance.hores.data.export.ExportService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual fun platformModule() = module {
    single<AppPrefs> {
        AndroidPrefs(
            androidContext().getSharedPreferences("hores_prefs", android.content.Context.MODE_PRIVATE)
        )
    }
    single {
        val dbFile = androidContext().getDatabasePath("hores_database.db").absolutePath
        getRoomDatabase(
            Room.databaseBuilder<AppDatabase>(
                context = androidContext(),
                name = dbFile,
                factory = { AppDatabase::class.instantiateImpl() }
            ),
            ANDROID_DATABASE_MIGRATIONS
        )
    }
    single { get<AppDatabase>().diaDao() }
    single { get<AppDatabase>().concepteDao() }
    single { get<AppDatabase>().rangHorariDao() }
    single { get<AppDatabase>().clientDao() }
    single<ExportService> { AndroidExportService(androidContext()) }
}
