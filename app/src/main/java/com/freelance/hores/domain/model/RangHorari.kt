package com.freelance.hores.domain.model

import kotlinx.serialization.Serializable
import java.time.LocalTime

@Serializable
data class RangHorari(
    val id: String,
    val concepteId: String,
    val horaInici: String, // HH:mm
    val horaFi: String     // HH:mm
) {
    fun getDuracionaEnHoras(): Double {
        val inici = LocalTime.parse(horaInici)
        val fi = LocalTime.parse(horaFi)
        var seconds = (fi.toSecondOfDay() - inici.toSecondOfDay()).toLong()
        if (seconds < 0) seconds += 24 * 3600
        return seconds / 3600.0
    }

    fun getDuracionaFormatada(): String {
        val inici = LocalTime.parse(horaInici)
        val fi = LocalTime.parse(horaFi)
        var seconds = (fi.toSecondOfDay() - inici.toSecondOfDay()).toLong()
        if (seconds < 0) seconds += 24 * 3600
        val minutes = seconds / 60
        val hores = minutes / 60
        val minuts = minutes % 60
        return if (hores > 0) "${hores}h ${minuts}m" else "${minuts}m"
    }
}
