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
        ),
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("diaId"), Index("clientId")]
)
data class ConcepteEntity(
    @PrimaryKey val id: String,
    val diaId: String,
    val clientId: String? = null,
    val nom: String,
    val preuHora: Double = 0.0,
    val estat: EstatFacturacio = EstatFacturacio.PENDENT,
    val despeses: Double = 0.0,
    val despesesNotes: String = "",
    val preuFix: Boolean = false,
    val importFix: Double = 0.0
)
