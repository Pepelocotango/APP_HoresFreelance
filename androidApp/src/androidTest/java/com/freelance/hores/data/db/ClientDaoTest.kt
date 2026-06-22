package com.freelance.hores.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.freelance.hores.data.db.dao.ClientDao
import com.freelance.hores.data.db.entity.ClientEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClientDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var clientDao: ClientDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        clientDao = database.clientDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndRetrieveClient() = runTest {
        val client = ClientEntity(id = 1, nom = "Client Test", preuHoraDefecte = 25.0)
        clientDao.insert(client)

        val allClients = clientDao.getAllClients().first()
        assertEquals(1, allClients.size)
        assertEquals("Client Test", allClients[0].nom)
    }

    @Test
    fun deleteClient() = runTest {
        val client = ClientEntity(id = 1, nom = "To delete", preuHoraDefecte = 20.0)
        clientDao.insert(client)
        clientDao.delete(client)

        val allClients = clientDao.getAllClients().first()
        assertTrue(allClients.isEmpty())
    }
}
