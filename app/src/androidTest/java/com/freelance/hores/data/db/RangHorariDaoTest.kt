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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.assertEquals

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
        ).build()
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
        val diaId = diaDao.insert(dia).toInt()
        
        val concepte = ConcepteEntity(diaId = diaId.toLong(), nom = "Work")
        val concepteId = concepteDao.insert(concepte).toInt()
        
        val rang = RangHorariEntity(
            concepteId = concepteId.toLong(),
            horaInici = LocalTime.of(9, 0).toString(),
            horaFi = LocalTime.of(17, 0).toString()
        )
        rangHorariDao.insert(rang)

        val retrieved = rangHorariDao.getById(rang.id)
        assertEquals(LocalTime.of(9, 0).toString(), retrieved?.horaInici)
    }

    @Test
    fun getRangsByConcepteId() = runTest {
        val dia = DiaEntity(data = LocalDate.now().toEpochDay(), notes = "Test")
        val diaId = diaDao.insert(dia).toInt()
        
        val concepte = ConcepteEntity(diaId = diaId.toLong(), nom = "Work")
        val concepteId = concepteDao.insert(concepte).toInt()
        
        val rang1 = RangHorariEntity(
            concepteId = concepteId.toLong(),
            horaInici = LocalTime.of(9, 0).toString(),
            horaFi = LocalTime.of(12, 0).toString()
        )
        val rang2 = RangHorariEntity(
            concepteId = concepteId.toLong(),
            horaInici = LocalTime.of(13, 0).toString(),
            horaFi = LocalTime.of(17, 0).toString()
        )
        
        rangHorariDao.insert(rang1)
        rangHorariDao.insert(rang2)

        val rangs = rangHorariDao.getByConcepteIdSync(concepteId.toLong())
        assertEquals(2, rangs.size)
    }

    @Test
    fun updateRangHorari() = runTest {
        val dia = DiaEntity(data = LocalDate.now().toEpochDay(), notes = "Test")
        val diaId = diaDao.insert(dia).toInt()
        
        val concepte = ConcepteEntity(diaId = diaId.toLong(), nom = "Work")
        val concepteId = concepteDao.insert(concepte).toInt()
        
        val rang = RangHorariEntity(
            concepteId = concepteId.toLong(),
            horaInici = LocalTime.of(9, 0).toString(),
            horaFi = LocalTime.of(12, 0).toString()
        )
        rangHorariDao.insert(rang)
        
        val updated = rang.copy(horaFi = LocalTime.of(13, 0).toString())
        rangHorariDao.update(updated)

        val retrieved = rangHorariDao.getById(rang.id)
        assertEquals(LocalTime.of(13, 0).toString(), retrieved?.horaFi)
    }

    @Test
    fun deleteRangHorari() = runTest {
        val dia = DiaEntity(data = LocalDate.now().toEpochDay(), notes = "Test")
        val diaId = diaDao.insert(dia).toInt()
        
        val concepte = ConcepteEntity(diaId = diaId.toLong(), nom = "Work")
        val concepteId = concepteDao.insert(concepte).toInt()
        
        val rang = RangHorariEntity(
            concepteId = concepteId.toLong(),
            horaInici = LocalTime.of(9, 0).toString(),
            horaFi = LocalTime.of(17, 0).toString()
        )
        rangHorariDao.insert(rang)
        rangHorariDao.delete(rang)

        val retrieved = rangHorariDao.getById(rang.id)
        assertEquals(null, retrieved)
    }

    @Test
    fun calculateTotalHoras() = runTest {
        val dia = DiaEntity(data = LocalDate.now().toEpochDay(), notes = "Test")
        val diaId = diaDao.insert(dia).toInt()
        
        val concepte = ConcepteEntity(diaId = diaId.toLong(), nom = "Work")
        val concepteId = concepteDao.insert(concepte).toInt()
        
        val rang1 = RangHorariEntity(
            concepteId = concepteId.toLong(),
            horaInici = LocalTime.of(9, 0).toString(),
            horaFi = LocalTime.of(12, 0).toString()
        )
        val rang2 = RangHorariEntity(
            concepteId = concepteId.toLong(),
            horaInici = LocalTime.of(13, 0).toString(),
            horaFi = LocalTime.of(17, 0).toString()
        )
        
        rangHorariDao.insert(rang1)
        rangHorariDao.insert(rang2)

        val rangs = rangHorariDao.getByConcepteIdSync(concepteId.toLong())
        val totalHoras = rangs.sumOf { 
            val inicio = LocalTime.parse(it.horaInici)
            val fin = LocalTime.parse(it.horaFi)
            val duration = java.time.temporal.ChronoUnit.HOURS.between(inicio, fin)
            duration.toDouble()
        }
        
        assertEquals(7.0, totalHoras, 0.1)
    }
}
