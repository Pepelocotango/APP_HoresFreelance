package com.freelance.hores.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Entity for work concepts (e.g., "Client Meeting", "Web Development")
@Entity(
    tableName = "conceptes",
    foreignKeys = [
        ForeignKey(
            entity = DiaEntity::class,
            parentColumns = ["id"],
            childColumns = ["diaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("diaId")]
)
data class ConcepteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val diaId: Long,
    val nom: String,
    val preuHora: Double = 0.0
)
