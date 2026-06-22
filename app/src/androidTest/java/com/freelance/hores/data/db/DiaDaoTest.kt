package com.freelance.hores.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.freelance.hores.data.db.dao.DiaDao
import com.freelance.hores.data.db.entity.DiaEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class DiaDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var diaDao: DiaDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        diaDao = database.diaDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndRetrieveDia() = runTest {
        val diaEntity = DiaEntity(
            id = 1,
            data = LocalDate.of(2024, 6, 15).toEpochDay(),
            notes = "Test day"
        )
        diaDao.insert(diaEntity)

        val retrieved = diaDao.getById(1)
        assertEquals(diaEntity.id, retrieved?.id)
        assertEquals(diaEntity.notes, retrieved?.notes)
    }

    @Test
    fun insertMultipleDias() = runTest {
        val dia1 = DiaEntity(data = LocalDate.of(2024, 6, 15).toEpochDay(), notes = "Day 1")
        val dia2 = DiaEntity(data = LocalDate.of(2024, 6, 16).toEpochDay(), notes = "Day 2")
        
        diaDao.insert(dia1)
        diaDao.insert(dia2)

        val allDias = diaDao.getAllDias().first()
        assertEquals(2, allDias.size)
    }

    @Test
    fun updateDia() = runTest {
        val dia = DiaEntity(id = 1, data = LocalDate.of(2024, 6, 15).toEpochDay(), notes = "Original")
        diaDao.insert(dia)
        diaDao.update(dia.copy(notes = "Updated"))

        val updated = diaDao.getById(1)
        assertEquals("Updated", updated?.notes)
    }

    @Test
    fun deleteDia() = runTest {
        val dia = DiaEntity(id = 1, data = LocalDate.of(2024, 6, 15).toEpochDay(), notes = "To delete")
        diaDao.insert(dia)
        diaDao.delete(dia)

        val retrieved = diaDao.getById(1)
        assertEquals(null, retrieved)
    }

    @Test
    fun getDiasByDateRange() = runTest {
        val startDate = LocalDate.of(2024, 6, 1).toEpochDay()
        val endDate = LocalDate.of(2024, 6, 30).toEpochDay()
        
        val dia1 = DiaEntity(data = LocalDate.of(2024, 6, 15).toEpochDay(), notes = "In range")
        val dia2 = DiaEntity(data = LocalDate.of(2024, 7, 1).toEpochDay(), notes = "Out of range")
        
        diaDao.insert(dia1)
        diaDao.insert(dia2)

        val result = diaDao.getDiasByDateRange(startDate, endDate).first()
        
        assertEquals(1, result.size)
        assertEquals("In range", result[0].notes)
    }

    @Test
    fun emptyDatabase() = runTest {
        val result = diaDao.getAllDias().first()
        assertTrue(result.isEmpty())
    }
}
