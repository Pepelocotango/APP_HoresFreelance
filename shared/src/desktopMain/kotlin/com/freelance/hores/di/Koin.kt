package com.freelance.hores.di
import androidx.room.Room
import com.freelance.hores.data.db.*
import org.koin.dsl.module
import java.io.File
actual fun platformModule() = module {
    single {
        val dbFile = File(System.getProperty("user.home"), ".horesfreelance/hores_database.db")
        if (!dbFile.parentFile.exists()) dbFile.parentFile.mkdirs()
        val builder = Room.databaseBuilder<AppDatabase>(name = dbFile.absolutePath, factory = { AppDatabase::class.instantiateImpl() })
        getRoomDatabase(builder)
    }
    single { get<AppDatabase>().diaDao() }
    single { get<AppDatabase>().concepteDao() }
    single { get<AppDatabase>().rangHorariDao() }
    single { get<AppDatabase>().clientDao() }
}
