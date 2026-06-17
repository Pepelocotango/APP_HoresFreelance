package com.freelance.hores.di

import android.content.Context
import com.freelance.hores.data.db.AppDatabase
import com.freelance.hores.data.db.dao.ClientDao
import com.freelance.hores.data.db.dao.ConcepteDao
import com.freelance.hores.data.db.dao.DiaDao
import com.freelance.hores.data.db.dao.RangHorariDao
import com.freelance.hores.data.repository.RegistreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Singleton
    @Provides
    fun provideDiaDao(database: AppDatabase): DiaDao {
        return database.diaDao()
    }

    @Singleton
    @Provides
    fun provideConcepteDao(database: AppDatabase): ConcepteDao {
        return database.concepteDao()
    }

    @Singleton
    @Provides
    fun provideRangHorariDao(database: AppDatabase): RangHorariDao {
        return database.rangHorariDao()
    }

    @Singleton
    @Provides
    fun provideClientDao(database: AppDatabase): ClientDao {
        return database.clientDao()
    }
}
