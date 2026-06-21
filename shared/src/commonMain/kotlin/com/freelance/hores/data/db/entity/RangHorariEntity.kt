package com.freelance.hores.data.db.entity


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
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val concepteId: Long,
    val horaInici: Long,  // Stored as seconds of day
    val horaFi: Long      // Stored as seconds of day
)
