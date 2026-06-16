package com.freelance.hores

import com.freelance.hores.domain.model.Concepte
import com.freelance.hores.domain.model.Dia
import com.freelance.hores.domain.model.RangHorari
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class RangHorariTest {
    @Test
    fun testDuracionaEnHoras() {
        val rang = RangHorari(
            concepteId = 1,
            horaInici = LocalTime.of(9, 0),
            horaFi = LocalTime.of(11, 30)
        )
        val duracio = rang.getDuracionaEnHoras()
        assert(duracio in 2.49..2.51)
    }

    @Test
    fun testDuracionaFormatada() {
        val rang = RangHorari(
            concepteId = 1,
            horaInici = LocalTime.of(9, 0),
            horaFi = LocalTime.of(11, 30)
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
                    horaInici = LocalTime.of(9, 0),
                    horaFi = LocalTime.of(11, 0)
                )
            )
        )
        val concepte2 = Concepte(
            diaId = 1,
            nom = "Task 2",
            rangsHoraris = listOf(
                RangHorari(
                    concepteId = 2,
                    horaInici = LocalTime.of(14, 0),
                    horaFi = LocalTime.of(17, 0)
                )
            )
        )

        val dia = Dia(
            data = LocalDate.now(),
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
                    horaInici = LocalTime.of(9, 0),
                    horaFi = LocalTime.of(12, 0)
                ),
                RangHorari(
                    concepteId = 1,
                    horaInici = LocalTime.of(14, 0),
                    horaFi = LocalTime.of(17, 0)
                )
            )
        )

        assert(concepte.getTotalHoras() in 5.99..6.01)
    }
}
