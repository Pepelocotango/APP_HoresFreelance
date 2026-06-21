package com.freelance.hores

import com.freelance.hores.domain.model.Concepte
import com.freelance.hores.domain.model.Dia
import com.freelance.hores.domain.model.RangHorari
import org.junit.Test
import com.freelance.hores.util.todayLocalDate
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class RangHorariTest {
    @Test
    fun testDuracionaEnHoras() {
        val rang = RangHorari(
            concepteId = 1,
            horaInici = LocalTime(9, 0),
            horaFi = LocalTime(11, 30)
        )
        val duracio = rang.getDuracionaEnHoras()
        assert(duracio in 2.49..2.51)
    }

    @Test
    fun testDuracionaFormatada() {
        val rang = RangHorari(
            concepteId = 1,
            horaInici = LocalTime(9, 0),
            horaFi = LocalTime(11, 30)
        )
        val formatted = rang.getDuracionaFormatada()
        assert(formatted.contains("2h") || formatted.contains("30m"))
    }
}

class DiaTest {
    @Test
    fun testGetTotalHoras() {
        val concepte1 = Concepte(
            diaId = 1,
            nom = "Task 1",
            rangsHoraris = listOf(
                RangHorari(
                    concepteId = 1,
                    horaInici = LocalTime(9, 0),
                    horaFi = LocalTime(11, 0)
                )
            )
        )
        val concepte2 = Concepte(
            diaId = 1,
            nom = "Task 2",
            rangsHoraris = listOf(
                RangHorari(
                    concepteId = 2,
                    horaInici = LocalTime(14, 0),
                    horaFi = LocalTime(17, 0)
                )
            )
        )

        val dia = Dia(
            data = todayLocalDate(),
            conceptes = listOf(concepte1, concepte2)
        )

        assert(dia.getTotalHoras() in 4.99..5.01)
    }
}

class ConcepteTest {
    @Test
    fun testGetTotalHoras() {
        val concepte = Concepte(
            diaId = 1,
            nom = "Task",
            rangsHoraris = listOf(
                RangHorari(
                    concepteId = 1,
                    horaInici = LocalTime(9, 0),
                    horaFi = LocalTime(12, 0)
                ),
                RangHorari(
                    concepteId = 1,
                    horaInici = LocalTime(14, 0),
                    horaFi = LocalTime(17, 0)
                )
            )
        )

        assert(concepte.getTotalHoras() in 5.99..6.01)
    }

    @Test
    fun testPreuFix() {
        val concepte = Concepte(
            diaId = 1,
            nom = "Task Fix",
            esPreuFix = true,
            importPreuFix = 100.0,
            despeses = 20.0,
            rangsHoraris = listOf(
                RangHorari(
                    concepteId = 1,
                    horaInici = LocalTime(9, 0),
                    horaFi = LocalTime(12, 0)
                )
            )
        )

        // Tot i tenir 3 hores, el preu és fix de 100€ + 20€ de despeses
        assert(concepte.getTotalDiners() == 120.0)
        assert(concepte.getDinersHores() == 0.0)
    }
}
