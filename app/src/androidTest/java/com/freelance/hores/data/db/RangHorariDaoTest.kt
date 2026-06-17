package com.freelance.hores.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.freelance.hores.data.db.dao.ConcepteDao
import com.freelance.hores.data.db.dao.DiaDao
import com.freelance.hores.data.db.dao.RangHorariDao
import com.freelance.hores.data.db.entity.ConcepteEntity
import com.freelance.hores.data.db.entity.DiaEntity
import com.freelance.hores.data.db.entity.RangHorariEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
class RangHorariDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var rangHorariDao: RangHorariDao
    private lateinit var concepteDao: ConcepteDao
    private lateinit var diaDao: DiaDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        rangHorariDao = database.rangHorariDao()
        concepteDao = database.concepteDao()
        diaDao = database.diaDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndRetrieveRangHorari() = runTest {
        val dia = DiaEntity(data = LocalDate.now().toEpochDay(), notes = "Test")
        val diaId = diaDao.insert(dia)
        
        val concepte = ConcepteEntity(diaId = diaId, nom = "Work")
        val concepteId = concepteDao.insert(concepte)
        
        val startTime = LocalTime.of(9, 0).toSecondOfDay().toLong()
        val endTime = LocalTime.of(17, 0).toSecondOfDay().toLong()
        
        val rang = RangHorariEntity(
            concepteId = concepteId,
            horaInici = startTime,
            horaFi = endTime
        )
        val rangId = rangHorariDao.insert(rang)

        val retrieved = rangHorariDao.getById(rangId)
        assertEquals(startTime, retrieved?.horaInici)
    }

    @Test
    fun getRangsByConcepteId() = runTest {
        val dia = DiaEntity(data = LocalDate.now().toEpochDay(), notes = "Test")
        val diaId = diaDao.insert(dia)
        
        val concepte = ConcepteEntity(diaId = diaId, nom = "Work")
        val concepteId = concepteDao.insert(concepte)
        
        val rang1 = RangHorariEntity(
            concepteId = concepteId,
            horaInici = LocalTime.of(9, 0).toSecondOfDay().toLong(),
            horaFi = LocalTime.of(12, 0).toSecondOfDay().toLong()
        )
        val rang2 = RangHorariEntity(
            concepteId = concepteId,
            horaInici = LocalTime.of(13, 0).toSecondOfDay().toLong(),
            horaFi = LocalTime.of(17, 0).toSecondOfDay().toLong()
        )
        
        rangHorariDao.insert(rang1)
        rangHorariDao.insert(rang2)

        val rangs = rangHorariDao.getByConcepteIdSync(concepteId)
        assertEquals(2, rangs.size)
    }

    @Test
    fun updateRangHorari() = runTest {
        val dia = DiaEntity(data = LocalDate.now().toEpochDay(), notes = "Test")
        val diaId = diaDao.insert(dia)
        
        val concepte = ConcepteEntity(diaId = diaId, nom = "Work")
        val concepteId = concepteDao.insert(concepte)
        
        val startTime = LocalTime.of(9, 0).toSecondOfDay().toLong()
        val endTime = LocalTime.of(12, 0).toSecondOfDay().toLong()
        
        val rang = RangHorariEntity(
            concepteId = concepteId,
            horaInici = startTime,
            horaFi = endTime
        )
        val rangId = rangHorariDao.insert(rang)
        
        val newEndTime = LocalTime.of(13, 0).toSecondOfDay().toLong()
        val updated = RangHorariEntity(id = rangId, concepteId = concepteId, horaInici = startTime, horaFi = newEndTime)
        rangHorariDao.update(updated)

        val retrieved = rangHorariDao.getById(rangId)
        assertEquals(newEndTime, retrieved?.horaFi)
    }

    @Test
    fun deleteRangHorari() = runTest {
        val dia = DiaEntity(data = LocalDate.now().toEpochDay(), notes = "Test")
        val diaId = diaDao.insert(dia)
        
        val concepte = ConcepteEntity(diaId = diaId, nom = "Work")
        val concepteId = concepteDao.insert(concepte)
        
        val rang = RangHorariEntity(
            concepteId = concepteId,
            horaInici = LocalTime.of(9, 0).toSecondOfDay().toLong(),
            horaFi = LocalTime.of(17, 0).toSecondOfDay().toLong()
        )
        val rangId = rangHorariDao.insert(rang)
        val insertedRang = rang.copy(id = rangId)
        
        rangHorariDao.delete(insertedRang)

        val retrieved = rangHorariDao.getById(rangId)
        assertEquals(null, retrieved)
    }

    @Test
    fun calculateTotalHoras() = runTest {
        val dia = DiaEntity(data = LocalDate.now().toEpochDay(), notes = "Test")
        val diaId = diaDao.insert(dia)
        
        val concepte = ConcepteEntity(diaId = diaId, nom = "Work")
        val concepteId = concepteDao.insert(concepte)
        
        val rang1 = RangHorariEntity(
            concepteId = concepteId,
            horaInici = LocalTime.of(9, 0).toSecondOfDay().toLong(),
            horaFi = LocalTime.of(12, 0).toSecondOfDay().toLong()
        )
        val rang2 = RangHorariEntity(
            concepteId = concepteId,
            horaInici = LocalTime.of(13, 0).toSecondOfDay().toLong(),
            horaFi = LocalTime.of(17, 0).toSecondOfDay().toLong()
        )
        
        rangHorariDao.insert(rang1)
        rangHorariDao.insert(rang2)

        val rangs = rangHorariDao.getByConcepteIdSync(concepteId)
        val totalSeconds = rangs.sumOf { it.horaFi - it.horaInici }
        val totalHoras = totalSeconds / 3600.0
        
        assertEquals(7.0, totalHoras, 0.1)
    }
}
