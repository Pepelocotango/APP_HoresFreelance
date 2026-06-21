package com.freelance.hores.data.db.dao

import com.freelance.hores.data.db.entity.RangHorariEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RangHorariDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rangHorari: RangHorariEntity): Long

    @Update
    suspend fun update(rangHorari: RangHorariEntity)

    @Delete
    suspend fun delete(rangHorari: RangHorariEntity)

    @Query("SELECT * FROM rangs_horaris WHERE id = :id")
    suspend fun getById(id: Long): RangHorariEntity?

    @Query("SELECT * FROM rangs_horaris WHERE concepteId = :concepteId ORDER BY horaInici ASC")
    fun getByConcepteId(concepteId: Long): Flow<List<RangHorariEntity>>

    @Query("SELECT * FROM rangs_horaris WHERE concepteId = :concepteId ORDER BY horaInici ASC")
    suspend fun getByConcepteIdSync(concepteId: Long): List<RangHorariEntity>
}
