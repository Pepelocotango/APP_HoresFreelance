package com.freelance.hores

import com.freelance.hores.domain.model.Concepte
import com.freelance.hores.domain.model.Dia
import com.freelance.hores.domain.model.RangHorari
import org.junit.Test
import java.time.LocalTime

class RangHorariTest {
    @Test
    fun testDuracionaEnHoras() {
        val rang = RangHorari(
            id = "1",
            concepteId = "1",
            horaInici = "09:00",
            horaFi = "11:30"
        )
        val inici = LocalTime.parse(rang.horaInici)
        val fi = LocalTime.parse(rang.horaFi)
        var seconds = (fi.toSecondOfDay() - inici.toSecondOfDay()).toLong()
        if (seconds < 0) seconds += 24 * 3600
        val duracio = seconds / 3600.0
        assert(duracio in 2.49..2.51)
    }
}

class DiaTest {
    @Test
    fun testGetTotalHoras() {
        val concepte1 = Concepte(
            id = "1",
            diaId = "1",
            nom = "Task 1",
            rangsHoraris = listOf(
                RangHorari(
                    id = "1",
                    concepteId = "1",
                    horaInici = "09:00",
                    horaFi = "11:00"
                )
            )
        )
        val concepte2 = Concepte(
            id = "2",
            diaId = "1",
            nom = "Task 2",
            rangsHoraris = listOf(
                RangHorari(
                    id = "2",
                    concepteId = "2",
                    horaInici = "14:00",
                    horaFi = "17:00"
                )
            )
        )

        val dia = Dia(
            id = "1",
            data = "2024-01-01",
            conceptes = listOf(concepte1, concepte2)
        )

        val totalHoras = dia.conceptes.sumOf { c ->
            c.rangsHoraris.sumOf { r ->
                val inici = LocalTime.parse(r.horaInici)
                val fi = LocalTime.parse(r.horaFi)
                var seconds = (fi.toSecondOfDay() - inici.toSecondOfDay()).toLong()
                if (seconds < 0) seconds += 24 * 3600
                seconds / 3600.0
            }
        }
        assert(totalHoras in 4.99..5.01)
    }
}
