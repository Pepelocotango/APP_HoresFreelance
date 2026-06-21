package com.freelance.hores.ui.screen.calendari

import app.cash.turbine.test
import com.freelance.hores.data.repository.RegistreRepository
import com.freelance.hores.domain.model.Dia
import com.freelance.hores.util.MainDispatcherRule
import com.freelance.hores.util.minusMonths
import com.freelance.hores.util.plusMonths
import com.freelance.hores.util.todayYearMonth
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlinx.datetime.LocalDate
import com.freelance.hores.util.YearMonth

class CalendariViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: CalendariViewModel
    private val repository: RegistreRepository = mockk()

    @Before
    fun setup() {
        // Mock repository to return empty list by default
        coEvery {
            repository.getDiasByDateRange(any(), any())
        } returns flowOf(emptyList())
    }

    @Test
    fun `initial state should load current month`() = runTest {
        viewModel = CalendariViewModel(repository)
        
        viewModel.currentMonth.test {
            val emission = awaitItem()
            assertEquals(todayYearMonth(), emission)
        }
    }

    @Test
    fun `loadDias should update dias state`() = runTest {
        val testDias = listOf(
            Dia(id = 1, data = LocalDate(2024, 1, 1), notes = "Test 1", conceptes = emptyList()),
            Dia(id = 2, data = LocalDate(2024, 1, 2), notes = "Test 2", conceptes = emptyList())
        )

        coEvery {
            repository.getDiasByDateRange(any(), any())
        } returns flowOf(testDias)

        viewModel = CalendariViewModel(repository)

        assertEquals(testDias, viewModel.dias.value)
    }

    @Test
    fun `nextMonth should increment current month`() = runTest {
        viewModel = CalendariViewModel(repository)
        
        val initialMonth = todayYearMonth()
        viewModel.nextMonth()
        
        viewModel.currentMonth.test {
            val emission = awaitItem()
            assertEquals(initialMonth.plusMonths(1), emission)
        }
    }

    @Test
    fun `previousMonth should decrement current month`() = runTest {
        viewModel = CalendariViewModel(repository)
        
        val initialMonth = todayYearMonth()
        viewModel.previousMonth()
        
        viewModel.currentMonth.test {
            val emission = awaitItem()
            assertEquals(initialMonth.minusMonths(1), emission)
        }
    }

    @Test
    fun `getDiasByDate should return matching dia or null`() = runTest {
        val targetDate = LocalDate(2024, 1, 15)
        val testDias = listOf(
            Dia(id = 1, data = targetDate, notes = "Target dia", conceptes = emptyList()),
            Dia(id = 2, data = LocalDate(2024, 1, 16), notes = "Other dia", conceptes = emptyList())
        )
        
        coEvery {
            repository.getDiasByDateRange(any(), any())
        } returns flowOf(testDias)
        
        viewModel = CalendariViewModel(repository)
        
        // Wait for loadDias to complete
        viewModel.dias.test {
            awaitItem()
        }
        
        val result = viewModel.getDiasByDate(targetDate)
        assertEquals(targetDate, result?.data)
    }

    @Test
    fun `getDiasByDate should return null when date not found`() = runTest {
        viewModel = CalendariViewModel(repository)
        
        val result = viewModel.getDiasByDate(LocalDate(2025, 12, 31))
        assertNull(result)
    }

    @Test
    fun `getDiasWithRecords should return list of dates`() = runTest {
        val testDias = listOf(
            Dia(id = 1, data = LocalDate(2024, 1, 1), notes = "Test 1", conceptes = emptyList()),
            Dia(id = 2, data = LocalDate(2024, 1, 5), notes = "Test 2", conceptes = emptyList())
        )
        
        coEvery {
            repository.getDiasByDateRange(any(), any())
        } returns flowOf(testDias)
        
        viewModel = CalendariViewModel(repository)
        
        // Wait for loadDias
        viewModel.dias.test {
            awaitItem()
        }
        
        val result = viewModel.getDiasWithRecords()
        assertEquals(2, result.size)
        assertEquals(LocalDate(2024, 1, 1), result[0])
        assertEquals(LocalDate(2024, 1, 5), result[1])
    }

    @Test
    fun `isLoading should be false after initial load`() = runTest {
        coEvery {
            repository.getDiasByDateRange(any(), any())
        } returns flowOf(emptyList())

        viewModel = CalendariViewModel(repository)

        assertEquals(false, viewModel.isLoading.value)
    }
}
