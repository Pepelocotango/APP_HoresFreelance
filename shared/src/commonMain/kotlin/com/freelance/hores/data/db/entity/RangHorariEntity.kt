package com.freelance.hores.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val concepteId: Long,
    val horaInici: Long,
    val horaFi: Long
)
