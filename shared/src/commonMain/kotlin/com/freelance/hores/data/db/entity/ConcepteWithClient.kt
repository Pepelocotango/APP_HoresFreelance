package com.freelance.hores.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ConcepteWithClient(
    @Embedded val concepte: ConcepteEntity,
    @Relation(
        parentColumn = "clientId",
        entityColumn = "id"
    )
    val client: ClientEntity?
)
