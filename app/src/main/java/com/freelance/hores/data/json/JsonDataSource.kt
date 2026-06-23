package com.freelance.hores.data.json

import android.content.Context
import com.freelance.hores.domain.model.AppData
import com.freelance.hores.domain.model.Client
import com.freelance.hores.domain.model.Dia
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    private val dataFile: File
        get() = File(context.filesDir, "hores_data.json")
    
    private val _appData = MutableStateFlow<AppData>(AppData(emptyList(), emptyList()))
    val appData: Flow<AppData> = _appData
    
    init {
        loadData()
    }
    
    private fun loadData() {
        try {
            if (dataFile.exists()) {
                val jsonString = dataFile.readText()
                val data = json.decodeFromString<AppData>(jsonString)
                _appData.value = data
            } else {
                // Fitxer no existeix, crear amb dades buides
                saveData()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Si hi ha error, crear fitxer amb dades buides
            _appData.value = AppData(emptyList(), emptyList())
            saveData()
        }
    }
    
    private fun saveData() {
        try {
            val jsonString = json.encodeToString(_appData.value)
            dataFile.writeText(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // --- Client operations ---
    fun getClients(): Flow<List<Client>> {
        return _appData.map { it.clients }
    }
    
    suspend fun saveClient(client: Client) {
        val currentData = _appData.value
        val existingIndex = currentData.clients.indexOfFirst { it.id == client.id }
        
        val updatedClients = if (existingIndex >= 0) {
            currentData.clients.toMutableList().apply { this[existingIndex] = client }
        } else {
            currentData.clients + client
        }
        
        _appData.value = currentData.copy(clients = updatedClients)
        saveData()
    }
    
    suspend fun deleteClient(client: Client) {
        val currentData = _appData.value
        val updatedClients = currentData.clients.filter { it.id != client.id }
        
        // També cal eliminar el clientId dels conceptes que fan referència a aquest client
        val updatedDies = currentData.dies.map { dia ->
            dia.copy(
                conceptes = dia.conceptes.map { concepte ->
                    if (concepte.clientId == client.id) {
                        concepte.copy(clientId = null)
                    } else {
                        concepte
                    }
                }
            )
        }
        
        _appData.value = currentData.copy(clients = updatedClients, dies = updatedDies)
        saveData()
    }
    
    // --- Dia operations ---
    fun getAllDias(): Flow<List<Dia>> {
        return _appData.map { it.dies }
    }
    
    suspend fun getDiaById(diaId: String): Dia? {
        return _appData.value.dies.find { it.id == diaId }
    }
    
    suspend fun saveDia(dia: Dia) {
        val currentData = _appData.value
        val existingIndex = currentData.dies.indexOfFirst { it.id == dia.id }
        
        val updatedDies = if (existingIndex >= 0) {
            currentData.dies.toMutableList().apply { this[existingIndex] = dia }
        } else {
            currentData.dies + dia
        }
        
        _appData.value = currentData.copy(dies = updatedDies)
        saveData()
    }
    
    suspend fun deleteDia(dia: Dia) {
        val currentData = _appData.value
        val updatedDies = currentData.dies.filter { it.id != dia.id }
        
        _appData.value = currentData.copy(dies = updatedDies)
        saveData()
    }
    
    // --- Import/Export ---
    suspend fun exportToJson(): String {
        return json.encodeToString(_appData.value)
    }
    
    suspend fun importFromJson(jsonString: String): Boolean {
        return try {
            val data = json.decodeFromString<AppData>(jsonString)
            _appData.value = data
            saveData()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    // --- Utility ---
    suspend fun clearAllData() {
        _appData.value = AppData(emptyList(), emptyList())
        saveData()
    }
}
