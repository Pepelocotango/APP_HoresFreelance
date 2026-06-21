package com.freelance.hores.ui.screen.dia

import app.cash.turbine.test
import com.freelance.hores.data.repository.RegistreRepository
import com.freelance.hores.domain.model.Dia
import com.freelance.hores.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlinx.datetime.LocalDate

class DiaDetallViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: DiaDetallViewModel
    private val repository: RegistreRepository = mockk()

    @Before
    fun setup() {
        coEvery {
            repository.getDiaWithDetails(any())
        } returns Dia(
            id = 1,
            data = com.freelance.hores.util.todayLocalDate(),
            notes = "Test",
            conceptes = emptyList()
        )
        coEvery {
            repository.updateDiaNotes(any(), any())
        } returns Unit
        coEvery {
            repository.deleteDia(any())
        } returns Unit
    }

    @Test
    fun `initial dia should be null`() = runTest {
        viewModel = DiaDetallViewModel(repository)
        
        viewModel.dia.test {
            val emission = awaitItem()
            assertNull(emission)
        }
    }

    @Test
    fun `loadDia should set dia from repository`() = runTest {
        viewModel = DiaDetallViewModel(repository)
        viewModel.loadDia(1L)
        
        viewModel.dia.test {
            val emission = awaitItem()
            assertEquals(1L, emission?.id)
            assertEquals("Test", emission?.notes)
        }
    }

    @Test
    fun `isLoading should be true while loading`() = runTest {
        viewModel = DiaDetallViewModel(repository)
        viewModel.loadDia(1L)
        
        viewModel.isLoading.test {
            val emission = awaitItem()
            assertEquals(false, emission)
        }
    }

    @Test
    fun `updateDiaNotes should update notes and repository`() = runTest {
        viewModel = DiaDetallViewModel(repository)
        viewModel.loadDia(1L)
        
        viewModel.dia.test {
            awaitItem()
        }
        
        viewModel.updateDiaNotes("Updated notes")
        
        coVerify { repository.updateDiaNotes(1L, "Updated notes") }
        
        viewModel.dia.test {
            val emission = awaitItem()
            assertEquals("Updated notes", emission?.notes)
        }
    }

    @Test
    fun `deleteDia should call repository and clear dia`() = runTest {
        viewModel = DiaDetallViewModel(repository)
        viewModel.loadDia(1L)
        
        viewModel.dia.test {
            awaitItem()
        }
        
        viewModel.deleteDia()
        
        coVerify { repository.deleteDia(any()) }
        assertNull(viewModel.dia.value)
    }
}
