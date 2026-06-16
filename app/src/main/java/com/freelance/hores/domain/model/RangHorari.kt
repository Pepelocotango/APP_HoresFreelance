package com.freelance.hores.domain.model

import java.time.LocalTime

data class RangHorari(
    val id: Long = 0,
    val concepteId: Long,
    val horaInici: LocalTime,
    val horaFi: LocalTime
) {
    fun getDuracionaEnHoras(): Double {
        val minutsTotal = (horaFi.toSecondOfDay() - horaInici.toSecondOfDay()) / 60
        return minutsTotal / 60.0
    }

    fun getDuracionaFormatada(): String {
        val minutsTotal = (horaFi.toSecondOfDay() - horaInici.toSecondOfDay()) / 60
        val hores = minutsTotal / 60
        val minuts = minutsTotal % 60
        return if (hores > 0) {
            "${hores}h ${minuts}m"
        } else {
            "${minuts}m"
        }
    }
}
