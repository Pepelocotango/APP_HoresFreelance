package com.freelance.hores.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Entity for time ranges within a concept
@Entity(
    tableName = "rangs_horaris",
    foreignKeys = [
        ForeignKey(
            entity = ConcepteEntity::class,
            parentColumns = ["id"],
            childColumns = ["concepteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("concepteId")]
)
data class RangHorariEntity(
    @PrimaryKey val id: String,
    val concepteId: String,
    val horaInici: Long,  // Stored as seconds of day
    val horaFi: Long      // Stored as seconds of day
)
