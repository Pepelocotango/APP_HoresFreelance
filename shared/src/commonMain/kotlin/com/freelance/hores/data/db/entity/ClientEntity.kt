package com.freelance.hores.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nom: String,
    val preuHoraDefecte: Double = 0.0
)
