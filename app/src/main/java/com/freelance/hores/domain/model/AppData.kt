package com.freelance.hores.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppData(
    val clients: List<Client>,
    val dies: List<Dia>
)
