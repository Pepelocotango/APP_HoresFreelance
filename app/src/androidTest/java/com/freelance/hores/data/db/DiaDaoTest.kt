package com.freelance.hores.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.freelance.hores.data.db.dao.DiaDao
import com.freelance.hores.data.db.entity.DiaEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
        ).build()
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

        val allDias = diaDao.getAllDias()
        var count = 0
        allDias.collect { dias ->
            count = dias.size
        }
        assertEquals(2, count)
    }

    @Test
    fun updateDiaNotes() = runTest {
        val dia = DiaEntity(data = LocalDate.of(2024, 6, 15).toEpochDay(), notes = "Original")
        diaDao.insert(dia)
        diaDao.updateNotes(dia.id, "Updated")

        val updated = diaDao.getById(dia.id)
        assertEquals("Updated", updated?.notes)
    }

    @Test
    fun deleteDia() = runTest {
        val dia = DiaEntity(data = LocalDate.of(2024, 6, 15).toEpochDay(), notes = "To delete")
        val insertedId = diaDao.insert(dia).toInt()
        diaDao.deleteById(insertedId.toLong())

        val retrieved = diaDao.getById(insertedId.toLong())
        assertEquals(null, retrieved)
    }

    @Test
    fun getDiasByDateRange() = runTest {
        val startDate = LocalDate.of(2024, 6, 1)
        val endDate = LocalDate.of(2024, 6, 30)
        
        val dia1 = DiaEntity(data = LocalDate.of(2024, 6, 15).toEpochDay(), notes = "In range")
        val dia2 = DiaEntity(data = LocalDate.of(2024, 7, 1).toEpochDay(), notes = "Out of range")
        
        diaDao.insert(dia1)
        diaDao.insert(dia2)

        val result = mutableListOf<DiaEntity>()
        diaDao.getDiasByDateRange(startDate, endDate).collect { dias ->
            result.clear()
            result.addAll(dias)
        }
        
        assertEquals(1, result.size)
        assertEquals("In range", result[0].notes)
    }

    @Test
    fun emptyDatabase() = runTest {
        var result = emptyList<DiaEntity>()
        diaDao.getAllDias().collect { dias ->
            result = dias
        }
        assertTrue(result.isEmpty())
    }
}
