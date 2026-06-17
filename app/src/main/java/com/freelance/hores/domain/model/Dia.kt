package com.freelance.hores.domain.model

import java.time.LocalDate

data class Dia(
    val id: Long = 0,
    val data: LocalDate,
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
