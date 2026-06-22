package com.freelance.hores.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Concepte(
    val id: String,
    val diaId: String,
    val nom: String,
    val preuHora: Double = 0.0,
    val clientId: String? = null,
    val clientNom: String? = null,
    val rangsHoraris: List<RangHorari> = emptyList(),
    val estat: String = "PENDENT",
    val despeses: Double = 0.0,
    val despesesNotes: String = "",
    val preuFix: Boolean = false,
    val importFix: Double = 0.0
) {
    fun getTotalHoras(): Double {
        return rangsHoraris.sumOf { it.getDuracionaEnHoras() }
    }

    fun getDinersHores(): Double {
        return if (preuFix) importFix else getTotalHoras() * preuHora
    }

    fun getDinersDespeses(): Double {
        return despeses
    }

    fun getTotalDiners(): Double {
        return getDinersHores() + getDinersDespeses()
    }
}
