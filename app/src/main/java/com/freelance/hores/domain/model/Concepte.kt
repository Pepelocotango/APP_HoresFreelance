package com.freelance.hores.domain.model

import java.time.LocalTime

data class Concepte(
    val id: Long = 0,
    val diaId: Long,
    val nom: String,
    val rangsHoraris: List<RangHorari> = emptyList()
) {
    fun getTotalHoras(): Double {
        return rangsHoraris.sumOf { it.getDuracionaEnHoras() }
    }
}
