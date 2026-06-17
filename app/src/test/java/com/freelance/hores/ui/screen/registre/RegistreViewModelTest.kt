package com.freelance.hores.ui.screen.registre

import com.freelance.hores.data.repository.RegistreRepository
import com.freelance.hores.util.MainDispatcherRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class RegistreViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: RegistreViewModel
    private val repository: RegistreRepository = mockk()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { repository.getClients() } returns flowOf(emptyList())
        viewModel = RegistreViewModel(repository)
    }

    @Test
    fun `initial formState should have default values`() = runTest {
        val state = viewModel.formState.value
        assertEquals(LocalDate.now(), state.data)
        assertEquals("", state.notes)
        assertEquals(0, state.conceptes.size)
        assertFalse(state.isSaving)
        assertEquals(null, state.error)
        assertFalse(state.success)
    }

    @Test
    fun `setData should update data field`() = runTest {
        val newDate = LocalDate.of(2024, 6, 15)
        viewModel.setData(newDate)
        assertEquals(newDate, viewModel.formState.value.data)
    }

    @Test
    fun `setNotes should update notes field`() = runTest {
        val notes = "Test notes"
        viewModel.setNotes(notes)
        assertEquals(notes, viewModel.formState.value.notes)
    }

    @Test
    fun `addConcepte should add new concepte to list`() = runTest {
        viewModel.addConcepte()
        val conceptes = viewModel.formState.value.conceptes
        assertEquals(1, conceptes.size)
    }

    @Test
    fun `addConcepte should initialize with one time range`() = runTest {
        viewModel.addConcepte()
        val concepte = viewModel.formState.value.conceptes[0]
        assertEquals(1, concepte.rangsHoraris.size)
    }

    @Test
    fun `updateConcepteName should modify concepte name`() = runTest {
        viewModel.addConcepte()
        viewModel.updateConcepteName(0, "Project B")
        assertEquals("Project B", viewModel.formState.value.conceptes[0].nom)
    }

    @Test
    fun `removeConcepte should delete concepte`() = runTest {
        viewModel.addConcepte()
        viewModel.addConcepte()
        assertEquals(2, viewModel.formState.value.conceptes.size)
        
        viewModel.removeConcepte(0)
        assertEquals(1, viewModel.formState.value.conceptes.size)
    }

    @Test
    fun `addRangHorariToConcepte should add time range`() = runTest {
        viewModel.addConcepte()
        viewModel.addRangHorariToConcepte(0)
        val concepte = viewModel.formState.value.conceptes[0]
        assertEquals(2, concepte.rangsHoraris.size)
    }

    @Test
    fun `updateRangHorariInici should update start time`() = runTest {
        viewModel.addConcepte()
        val newTime = LocalTime.of(10, 0)
        viewModel.updateRangHorariInici(0, 0, newTime)
        
        val rang = viewModel.formState.value.conceptes[0].rangsHoraris[0]
        assertEquals(newTime, rang.horaInici)
    }

    @Test
    fun `multiple operations should maintain state correctly`() = runTest {
        viewModel.setData(LocalDate.of(2024, 6, 15))
        viewModel.setNotes("Important work")
        viewModel.addConcepte()
        viewModel.addConcepte()
        viewModel.updateConcepteName(0, "Task 1 Updated")
        
        val state = viewModel.formState.value
        assertEquals(LocalDate.of(2024, 6, 15), state.data)
        assertEquals("Important work", state.notes)
        assertEquals(2, state.conceptes.size)
        assertEquals("Task 1 Updated", state.conceptes[0].nom)
    }
}
