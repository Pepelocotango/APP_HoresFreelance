package com.freelance.hores.domain.model

import kotlinx.datetime.LocalDate

data class Dia(
    val id: Long = 0,
    val data: LocalDate,
    val notes: String = "",
    val conceptes: List<Concepte> = emptyList()
) {
    fun getTotalHoras(): Double = conceptes.sumOf { it.getTotalHoras() }

    fun getTotalDiners(): Double = conceptes.sumOf { it.getTotalDiners() }

    fun getTotalDinersHores(): Double = conceptes.sumOf { it.getDinersHores() }

    fun getTotalDinersDespeses(): Double = conceptes.sumOf { it.getDinersDespeses() }
}
