package com.freelance.hores.domain.model

data class Concepte(
    val id: Long = 0,
    val diaId: Long,
    val nom: String,
    val preuHora: Double = 0.0,
    val rangsHoraris: List<RangHorari> = emptyList()
) {
    fun getTotalHoras(): Double {
        return rangsHoraris.sumOf { it.getDuracionaEnHoras() }
    }

    fun getTotalDiners(): Double {
        return getTotalHoras() * preuHora
    }
}
