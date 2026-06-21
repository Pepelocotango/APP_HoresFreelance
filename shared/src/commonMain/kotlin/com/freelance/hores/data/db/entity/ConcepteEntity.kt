package com.freelance.hores.data.db.entity


import com.freelance.hores.data.db.entity.EstatFacturacio

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
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val diaId: Long,
    val clientId: Long? = null,
    val nom: String,
    val preuHora: Double = 0.0,
    val estat: EstatFacturacio = EstatFacturacio.PENDENT,
    val despeses: Double = 0.0,
    val despesesNotes: String = "",
    val esPreuFix: Boolean = false,
    val importPreuFix: Double = 0.0
)
