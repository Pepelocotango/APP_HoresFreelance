package com.freelance.hores.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.freelance.hores.data.db.entity.ConcepteEntity
import com.freelance.hores.data.db.entity.ConcepteWithClient
import kotlinx.coroutines.flow.Flow

@Dao
interface ConcepteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(concepte: ConcepteEntity)

    @Update
    suspend fun update(concepte: ConcepteEntity)

    @Delete
    suspend fun delete(concepte: ConcepteEntity)

    @Query("SELECT * FROM conceptes WHERE id = :id")
    suspend fun getById(id: String): ConcepteEntity?

    @Query("SELECT * FROM conceptes WHERE diaId = :diaId ORDER BY id ASC")
    fun getByDiaId(diaId: String): Flow<List<ConcepteEntity>>

    @Query("SELECT * FROM conceptes WHERE diaId = :diaId ORDER BY id ASC")
    suspend fun getByDiaIdSync(diaId: String): List<ConcepteEntity>

    @Transaction
    @Query("SELECT * FROM conceptes WHERE diaId = :diaId ORDER BY id ASC")
    suspend fun getByDiaIdWithClientSync(diaId: String): List<ConcepteWithClient>
}
