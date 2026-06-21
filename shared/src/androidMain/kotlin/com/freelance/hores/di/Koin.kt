package com.freelance.hores.di
import androidx.room.Room
import com.freelance.hores.data.db.*
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
actual fun platformModule() = module {
    single {
        val dbFile = androidContext().getDatabasePath("hores_database.db").absolutePath
        val builder = Room.databaseBuilder<AppDatabase>(context = androidContext(), name = dbFile, factory = { AppDatabase::class.instantiateImpl() })
        getRoomDatabase(builder)
    }
    single { get<AppDatabase>().diaDao() }
    single { get<AppDatabase>().concepteDao() }
    single { get<AppDatabase>().rangHorariDao() }
    single { get<AppDatabase>().clientDao() }
}
