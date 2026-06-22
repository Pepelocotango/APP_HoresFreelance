package com.freelance.hores.domain.model

import com.freelance.hores.util.totalSecondsFromMidnight
import kotlinx.datetime.LocalTime

data class RangHorari(
    val id: Long = 0,
    val concepteId: Long,
    val horaInici: LocalTime,
    val horaFi: LocalTime
) {
    fun getDuracionaEnHoras(): Double {
        var seconds = horaFi.totalSecondsFromMidnight() - horaInici.totalSecondsFromMidnight()
        if (seconds < 0) seconds += 24 * 3600
        return seconds / 3600.0
    }

    fun getDuracionaFormatada(): String {
        var seconds = horaFi.totalSecondsFromMidnight() - horaInici.totalSecondsFromMidnight()
        if (seconds < 0) seconds += 24 * 3600
        val minutes = seconds / 60
        val hores = minutes / 60
        val minuts = minutes % 60
        return if (hores > 0) "${hores}h ${minuts}m" else "${minuts}m"
    }
}
