package com.freelance.hores.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.freelance.hores.data.db.entity.DiaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dia: DiaEntity): Long

    @Update
    suspend fun update(dia: DiaEntity)

    @Delete
    suspend fun delete(dia: DiaEntity)

    @Query("SELECT * FROM dies WHERE id = :id")
    suspend fun getById(id: Long): DiaEntity?

    @Query("SELECT * FROM dies ORDER BY data DESC")
    fun getAllDias(): Flow<List<DiaEntity>>

    @Query("SELECT * FROM dies WHERE data = :epochDay")
    suspend fun getByDate(epochDay: Long): DiaEntity?

    @Query("SELECT * FROM dies WHERE data BETWEEN :startEpochDay AND :endEpochDay ORDER BY data DESC")
    fun getDiasByDateRange(startEpochDay: Long, endEpochDay: Long): Flow<List<DiaEntity>>
}
