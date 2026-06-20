package com.freelance.hores.domain.model

import com.freelance.hores.data.db.entity.EstatFacturacio

data class Concepte(
    val id: Long = 0,
    val diaId: Long,
    val nom: String,
    val preuHora: Double = 0.0,
    val clientId: Long? = null,
    val clientNom: String? = null,
    val rangsHoraris: List<RangHorari> = emptyList(),
    val estat: EstatFacturacio = EstatFacturacio.PENDENT,
    val despeses: Double = 0.0,
    val despesesNotes: String = "",
    val esPreuFix: Boolean = false,
    val importPreuFix: Double = 0.0
) {
    fun getTotalHoras(): Double {
        return rangsHoraris.sumOf { it.getDuracionaEnHoras() }
    }

    fun getDinersHores(): Double {
        return if (esPreuFix) 0.0 else getTotalHoras() * preuHora
    }

    fun getDinersDespeses(): Double {
        return despeses
    }

    fun getTotalDiners(): Double {
        val base = if (esPreuFix) importPreuFix else getDinersHores()
        return base + getDinersDespeses()
    }
}
