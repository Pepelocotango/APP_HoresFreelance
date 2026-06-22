package com.freelance.hores.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Dia(
    val id: String,
    val data: String, // YYYY-MM-DD
    val notes: String = "",
    val conceptes: List<Concepte> = emptyList()
) {
    fun getTotalHoras(): Double {
        return conceptes.sumOf { it.getTotalHoras() }
    }

    fun getTotalDiners(): Double {
        return conceptes.sumOf { it.getTotalDiners() }
    }

    fun getTotalDinersHores(): Double {
        return conceptes.sumOf { it.getDinersHores() }
    }

    fun getTotalDinersDespeses(): Double {
        return conceptes.sumOf { it.getDinersDespeses() }
    }
}
