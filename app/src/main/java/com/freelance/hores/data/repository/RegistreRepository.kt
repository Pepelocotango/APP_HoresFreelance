package com.freelance.hores.data.repository

import com.freelance.hores.data.json.JsonDataSource
import com.freelance.hores.domain.model.Client
import com.freelance.hores.domain.model.Concepte
import com.freelance.hores.domain.model.Dia
import com.freelance.hores.domain.model.RangHorari
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class RegistreRepository @Inject constructor(
    private val jsonDataSource: JsonDataSource
) {

    // --- Client operations ---
    fun getClients(): Flow<List<Client>> {
        return jsonDataSource.getClients()
    }

    suspend fun saveClient(client: Client) {
        val id = client.id.ifEmpty { UUID.randomUUID().toString() }
        jsonDataSource.saveClient(client.copy(id = id))
    }

    suspend fun deleteClient(client: Client) {
        jsonDataSource.deleteClient(client)
    }

    // --- Registre operations ---
    // Get all dias with details
    fun getAllDiasWithDetails(): Flow<List<Dia>> {
        return jsonDataSource.getAllDias()
    }

    // Get a specific dia with all its conceptes and rangs horaris
    suspend fun getDiaWithDetails(diaId: String): Dia {
        return jsonDataSource.getDiaById(diaId) ?: Dia(id = UUID.randomUUID().toString(), data = LocalDate.now().toString())
    }

    // Save or update a complete dia with conceptes and rangs horaris
    suspend fun saveDia(dia: Dia) {
        val diaId = dia.id.ifEmpty { UUID.randomUUID().toString() }
        jsonDataSource.saveDia(dia.copy(id = diaId))
    }

    // Delete a dia
    suspend fun deleteDia(dia: Dia) {
        jsonDataSource.deleteDia(dia)
    }

    // Get dia by date
    suspend fun getDiaByDate(date: LocalDate): Dia? {
        val dateString = date.toString()
        return jsonDataSource.getAllDias().first().find { it.data == dateString }
    }

    // Update dia notes
    suspend fun updateDiaNotes(diaId: String, notes: String) {
        val dia = jsonDataSource.getDiaById(diaId) ?: return
        jsonDataSource.saveDia(dia.copy(notes = notes))
    }
}
