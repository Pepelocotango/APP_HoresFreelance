package com.freelance.hores.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Client(
    val id: String,
    val nom: String,
    val preuHoraDefecte: Double
)
