package com.freelance.hores.data.repository

import app.cash.turbine.test
import com.freelance.hores.data.db.dao.ConcepteDao
import com.freelance.hores.data.db.dao.DiaDao
import com.freelance.hores.data.db.dao.RangHorariDao
import com.freelance.hores.data.db.entity.ConcepteEntity
import com.freelance.hores.data.db.entity.DiaEntity
import com.freelance.hores.data.db.entity.RangHorariEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class RegistreRepositoryTest {
    private lateinit var repository: RegistreRepository
    private val diaDao: DiaDao = mockk()
    private val concepteDao: ConcepteDao = mockk()
    private val rangHorariDao: RangHorariDao = mockk()

    @Before
    fun setup() {
        repository = RegistreRepository(diaDao, concepteDao, rangHorariDao)
    }

    @Test
    fun `getDiasByDateRange should return dias within range`() = runTest {
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)
        val diasEntity = listOf(
            DiaEntity(id = 1, data = startDate.toEpochDay(), notes = "Day 1"),
            DiaEntity(id = 2, data = startDate.plusDays(1).toEpochDay(), notes = "Day 2")
        )

        coEvery {
            diaDao.getDiasByDateRange(startDate.toEpochDay(), endDate.toEpochDay())
        } returns flowOf(diasEntity)
        coEvery { concepteDao.getByDiaIdSync(1) } returns emptyList()
        coEvery { concepteDao.getByDiaIdSync(2) } returns emptyList()

        repository.getDiasByDateRange(startDate, endDate).test {
            val resultado = awaitItem()
            assertEquals(2, resultado.size)
            awaitComplete()
        }
    }

    @Test
    fun `getDiaWithDetails should map entity to domain model`() = runTest {
        val diaEntity = DiaEntity(id = 1, data = LocalDate.now().toEpochDay(), notes = "Test")
        val concepteEntity = ConcepteEntity(id = 1, diaId = 1, nom = "Project A")
        val rangEntity = RangHorariEntity(
            id = 1,
            concepteId = 1,
            horaInici = LocalTime.of(9, 0).toSecondOfDay().toLong(),
            horaFi = LocalTime.of(17, 0).toSecondOfDay().toLong()
        )

        coEvery { diaDao.getById(1) } returns diaEntity
        coEvery { concepteDao.getByDiaIdSync(1) } returns listOf(concepteEntity)
        coEvery { rangHorariDao.getByConcepteIdSync(1) } returns listOf(rangEntity)

        val result = repository.getDiaWithDetails(1)

        assertEquals(1L, result.id)
        assertEquals("Test", result.notes)
        assertEquals(1, result.conceptes.size)
    }

    @Test
    fun `updateDiaNotes should call dao update`() = runTest {
        val diaEntity = DiaEntity(id = 1, data = LocalDate.now().toEpochDay(), notes = "Old")
        coEvery { diaDao.getById(1) } returns diaEntity
        coEvery { diaDao.update(any()) } returns Unit

        repository.updateDiaNotes(1, "New notes")

        coVerify { diaDao.update(diaEntity.copy(notes = "New notes")) }
    }
}
