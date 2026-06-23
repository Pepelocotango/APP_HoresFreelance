package com.freelance.hores.di

import android.content.Context
import com.freelance.hores.data.json.JsonDataSource
import com.freelance.hores.data.repository.RegistreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Singleton
    @Provides
    fun provideJsonDataSource(
        @ApplicationContext context: Context
    ): JsonDataSource {
        return JsonDataSource(context)
    }

    @Singleton
    @Provides
    fun provideRegistreRepository(
        jsonDataSource: JsonDataSource
    ): RegistreRepository {
        return RegistreRepository(jsonDataSource)
    }

    @Singleton
    @Provides
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): android.content.SharedPreferences {
        return context.getSharedPreferences("hores_prefs", Context.MODE_PRIVATE)
    }
}
