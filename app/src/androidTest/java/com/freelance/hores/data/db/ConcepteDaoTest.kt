package com.freelance.hores.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.freelance.hores.data.db.dao.ConcepteDao
import com.freelance.hores.data.db.dao.DiaDao
import com.freelance.hores.data.db.entity.ConcepteEntity
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
class ConcepteDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var concepteDao: ConcepteDao
    private lateinit var diaDao: DiaDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()
        concepteDao = database.concepteDao()
        diaDao = database.diaDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndRetrieveConcepte() = runTest {
        val dia = DiaEntity(data = LocalDate.now().toEpochDay(), notes = "Test")
        val diaId = diaDao.insert(dia).toInt()
        
        val concepte = ConcepteEntity(
            id = 1,
            diaId = diaId.toLong(),
            nom = "Project A"
        )
        concepteDao.insert(concepte)

        val retrieved = concepteDao.getById(1)
        assertEquals("Project A", retrieved?.nom)
    }

    @Test
    fun getConceptesByDiaId() = runTest {
        val dia = DiaEntity(data = LocalDate.now().toEpochDay(), notes = "Test")
        val diaId = diaDao.insert(dia).toInt()
        
        val concepte1 = ConcepteEntity(diaId = diaId.toLong(), nom = "Task 1")
        val concepte2 = ConcepteEntity(diaId = diaId.toLong(), nom = "Task 2")
        
        concepteDao.insert(concepte1)
        concepteDao.insert(concepte2)

        val conceptes = concepteDao.getByDiaIdSync(diaId.toLong())
        assertEquals(2, conceptes.size)
    }

    @Test
    fun updateConcepte() = runTest {
        val dia = DiaEntity(data = LocalDate.now().toEpochDay(), notes = "Test")
        val diaId = diaDao.insert(dia).toInt()
        
        val concepte = ConcepteEntity(diaId = diaId.toLong(), nom = "Original")
        concepteDao.insert(concepte)
        
        val updated = concepte.copy(nom = "Updated")
        concepteDao.update(updated)

        val retrieved = concepteDao.getById(concepte.id)
        assertEquals("Updated", retrieved?.nom)
    }

    @Test
    fun deleteConcepte() = runTest {
        val dia = DiaEntity(data = LocalDate.now().toEpochDay(), notes = "Test")
        val diaId = diaDao.insert(dia).toInt()
        
        val concepte = ConcepteEntity(diaId = diaId.toLong(), nom = "To delete")
        val concepteId = concepteDao.insert(concepte).toInt()
        
        concepteDao.delete(concepte)

        val retrieved = concepteDao.getById(concepteId.toLong())
        assertEquals(null, retrieved)
    }

    @Test
    fun emptyConceptesByDiaId() = runTest {
        val dia = DiaEntity(data = LocalDate.now().toEpochDay(), notes = "Test")
        val diaId = diaDao.insert(dia).toInt()

        val conceptes = concepteDao.getByDiaIdSync(diaId.toLong())
        assertTrue(conceptes.isEmpty())
    }
}
