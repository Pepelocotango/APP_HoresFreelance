package com.freelance.hores.data.db.entity


data class ConcepteWithClient(
    @Embedded val concepte: ConcepteEntity,
    @Relation(
        parentColumn = "clientId",
        entityColumn = "id"
    )
    val client: ClientEntity?
)
