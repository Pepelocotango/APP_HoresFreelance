package com.freelance.hores.ui.screen.resum

import com.freelance.hores.data.export.ExportService
import com.freelance.hores.data.repository.RegistreRepository
import com.freelance.hores.domain.model.Concepte
import com.freelance.hores.domain.model.Dia
import com.freelance.hores.domain.model.RangHorari
import com.freelance.hores.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ResumViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ResumViewModel
    private val repository: RegistreRepository = mockk()
    private val exportService: ExportService = mockk(relaxed = true)

    @Before
    fun setup() {
        coEvery {
            repository.getDiasByDateRange(any(), any())
        } returns flowOf(emptyList())
    }

    @Test
    fun `initial state should load this week`() = runTest {
        viewModel = ResumViewModel(repository, exportService)

        val state = viewModel.resumState.value
        val today = LocalDate.now()
        val expectedMonday = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        val expectedSunday = expectedMonday.plusDays(6)

        assertEquals(expectedMonday, state.startDate)
        assertEquals(expectedSunday, state.endDate)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadThisMonth should set current month range`() = runTest {
        viewModel = ResumViewModel(repository, exportService)
        viewModel.loadThisMonth()

        val today = LocalDate.now()
        val firstDay = today.withDayOfMonth(1)
        val lastDay = today.withDayOfMonth(today.lengthOfMonth())

        assertEquals(firstDay, viewModel.resumState.value.startDate)
        assertEquals(lastDay, viewModel.resumState.value.endDate)
    }

    @Test
    fun `loadCustomPeriod should set custom date range`() = runTest {
        viewModel = ResumViewModel(repository, exportService)
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)

        viewModel.loadCustomPeriod(startDate, endDate)

        assertEquals(startDate, viewModel.resumState.value.startDate)
        assertEquals(endDate, viewModel.resumState.value.endDate)
    }

    @Test
    fun `getTotalHoras should sum all horas from all conceptes`() = runTest {
        val rang1 = RangHorari(concepteId = 1, horaInici = LocalTime.of(9, 0), horaFi = LocalTime.of(12, 0))
        val rang2 = RangHorari(concepteId = 1, horaInici = LocalTime.of(13, 0), horaFi = LocalTime.of(17, 0))
        val concepte = Concepte(diaId = 1, nom = "Work", rangsHoraris = listOf(rang1, rang2))
        val dia = Dia(data = LocalDate.now(), conceptes = listOf(concepte))

        coEvery {
            repository.getDiasByDateRange(any(), any())
        } returns flowOf(listOf(dia))

        viewModel = ResumViewModel(repository, exportService)

        assertEquals(7.0, viewModel.getTotalHoras(), 0.1)
    }

    @Test
    fun `getConceptesSummary should group horas by concepte name`() = runTest {
        val rang1 = RangHorari(concepteId = 1, horaInici = LocalTime.of(9, 0), horaFi = LocalTime.of(12, 0))
        val concepteA = Concepte(diaId = 1, nom = "Project A", rangsHoraris = listOf(rang1))
        val concepteB = Concepte(diaId = 1, nom = "Project B", rangsHoraris = listOf(rang1))
        val dia = Dia(data = LocalDate.now(), conceptes = listOf(concepteA, concepteB))

        coEvery {
            repository.getDiasByDateRange(any(), any())
        } returns flowOf(listOf(dia))

        viewModel = ResumViewModel(repository, exportService)

        val summary = viewModel.getConceptesSummary()
        assertEquals(2, summary.size)
        assertEquals(3.0, summary["Project A"]!!, 0.1)
        assertEquals(3.0, summary["Project B"]!!, 0.1)
    }

    @Test
    fun `isLoading should be false after initial load`() = runTest {
        viewModel = ResumViewModel(repository, exportService)
        assertFalse(viewModel.resumState.value.isLoading)
    }
}
