package com.freelance.hores.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

// Entity for workdays
@Entity(tableName = "dies")
data class DiaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val data: Long,  // Stored as epoch day (LocalDate.toEpochDay())
    val notes: String = ""
)
