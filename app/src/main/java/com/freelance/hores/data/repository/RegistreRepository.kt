package com.freelance.hores.data.repository

import androidx.room.withTransaction
import com.freelance.hores.data.db.AppDatabase
import com.freelance.hores.data.db.dao.ClientDao
import com.freelance.hores.data.db.dao.ConcepteDao
import com.freelance.hores.data.db.dao.DiaDao
import com.freelance.hores.data.db.dao.RangHorariDao
import com.freelance.hores.data.db.entity.ClientEntity
import com.freelance.hores.data.db.entity.ConcepteEntity
import com.freelance.hores.data.db.entity.DiaEntity
import com.freelance.hores.data.db.entity.RangHorariEntity
import com.freelance.hores.domain.model.Client
import com.freelance.hores.domain.model.Concepte
import com.freelance.hores.domain.model.Dia
import com.freelance.hores.domain.model.RangHorari
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class RegistreRepository @Inject constructor(
    private val database: AppDatabase,
    private val diaDao: DiaDao,
    private val concepteDao: ConcepteDao,
    private val rangHorariDao: RangHorariDao,
    private val clientDao: ClientDao
) {
    // --- Client operations ---
    fun getClients(): Flow<List<Client>> {
        return clientDao.getAllClients().map { entities ->
            entities.map { Client(it.id, it.nom, it.preuHoraDefecte) }
        }
    }

    suspend fun saveClient(client: Client) {
        clientDao.insert(ClientEntity(id = client.id, nom = client.nom, preuHoraDefecte = client.preuHoraDefecte))
    }

    suspend fun deleteClient(client: Client) {
        clientDao.delete(ClientEntity(id = client.id, nom = client.nom, preuHoraDefecte = client.preuHoraDefecte))
    }

    // --- Registre operations ---
    // Get all dias with concepts and time ranges
    fun getAllDiasWithDetails(): Flow<List<Dia>> {
        return diaDao.getAllDias().map { diasEntity ->
            diasEntity.map { entity ->
                Dia(
                    id = entity.id,
                    data = LocalDate.ofEpochDay(entity.data),
                    notes = entity.notes,
                    conceptes = getConceptesForDia(entity.id)
                )
            }
        }
    }

    // Get a specific dia with all its conceptes and rangs horaris
    suspend fun getDiaWithDetails(diaId: Long): Dia {
        val diaEntity = diaDao.getById(diaId) ?: return Dia(data = LocalDate.now())
        val conceptes = getConceptesForDia(diaId)
        return Dia(
            id = diaEntity.id,
            data = LocalDate.ofEpochDay(diaEntity.data),
            notes = diaEntity.notes,
            conceptes = conceptes
        )
    }

    // Get conceptes with their time ranges for a specific dia
    private suspend fun getConceptesForDia(diaId: Long): List<Concepte> {
        val concepteWithClient = concepteDao.getByDiaIdWithClientSync(diaId)
        return concepteWithClient.map { data ->
            val rangsHoraris = getRangsForConcepte(data.concepte.id)
            Concepte(
                id = data.concepte.id,
                diaId = data.concepte.diaId,
                nom = data.concepte.nom,
                preuHora = data.concepte.preuHora,
                clientId = data.concepte.clientId,
                clientNom = data.client?.nom,
                estat = data.concepte.estat,
                despeses = data.concepte.despeses,
                despesesNotes = data.concepte.despesesNotes,
                rangsHoraris = rangsHoraris
            )
        }
    }

    // Get time ranges for a specific concepte
    private suspend fun getRangsForConcepte(concepteId: Long): List<RangHorari> {
        val rangEntities = rangHorariDao.getByConcepteIdSync(concepteId)
        return rangEntities.map { rangEntity ->
            RangHorari(
                id = rangEntity.id,
                concepteId = rangEntity.concepteId,
                horaInici = LocalTime.ofSecondOfDay(rangEntity.horaInici),
                horaFi = LocalTime.ofSecondOfDay(rangEntity.horaFi)
            )
        }
    }

    // Get dias within a date range with full details
    fun getDiasByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Dia>> {
        return diaDao.getDiasByDateRange(startDate.toEpochDay(), endDate.toEpochDay())
            .map { diasEntity ->
                diasEntity.map { diaEntity ->
                    Dia(
                        id = diaEntity.id,
                        data = LocalDate.ofEpochDay(diaEntity.data),
                        notes = diaEntity.notes,
                        conceptes = getConceptesForDia(diaEntity.id)
                    )
                }
            }
    }

    // Save or update a complete dia with conceptes and rangs horaris
    suspend fun saveDia(dia: Dia) {
        database.withTransaction {
            // 1. Insert or update the dia
            val diaEntity = DiaEntity(
                id = dia.id,
                data = dia.data.toEpochDay(),
                notes = dia.notes
            )
            val actualDiaId = if (dia.id > 0) {
                diaDao.update(diaEntity)
                dia.id
            } else {
                diaDao.insert(diaEntity)
            }

            // 2. Clear existing relations to avoid duplicates and handle deletions
            val existingConceptes = concepteDao.getByDiaIdWithClientSync(actualDiaId)
            for (concepteData in existingConceptes) {
                concepteDao.delete(concepteData.concepte) // Cascade will delete rangs horaris
            }

            // 3. Save new conceptes and their time ranges
            for (concepte in dia.conceptes) {
                val concepteEntity = ConcepteEntity(
                    diaId = actualDiaId,
                    clientId = concepte.clientId,
                    nom = concepte.nom,
                    preuHora = concepte.preuHora,
                    estat = concepte.estat,
                    despeses = concepte.despeses,
                    despesesNotes = concepte.despesesNotes
                )
                val concepteId = concepteDao.insert(concepteEntity)

                // Save rangs horaris
                for (rangHorari in concepte.rangsHoraris) {
                    val rangEntity = RangHorariEntity(
                        concepteId = concepteId,
                        horaInici = rangHorari.horaInici.toSecondOfDay().toLong(),
                        horaFi = rangHorari.horaFi.toSecondOfDay().toLong()
                    )
                    rangHorariDao.insert(rangEntity)
                }
            }
        }
    }

    // Delete a dia
    suspend fun deleteDia(dia: Dia) {
        diaDao.delete(DiaEntity(id = dia.id, data = dia.data.toEpochDay(), notes = dia.notes))
    }

    // Delete a concepte
    suspend fun deleteConcepte(concepte: Concepte) {
        concepteDao.delete(
            ConcepteEntity(
                id = concepte.id,
                diaId = concepte.diaId,
                clientId = concepte.clientId,
                nom = concepte.nom,
                preuHora = concepte.preuHora,
                estat = concepte.estat,
                despeses = concepte.despeses,
                despesesNotes = concepte.despesesNotes
            )
        )
    }

    // Delete a rang horari
    suspend fun deleteRangHorari(rangHorari: RangHorari) {
        rangHorariDao.delete(
            RangHorariEntity(
                id = rangHorari.id,
                concepteId = rangHorari.concepteId,
                horaInici = rangHorari.horaInici.toSecondOfDay().toLong(),
                horaFi = rangHorari.horaFi.toSecondOfDay().toLong()
            )
        )
    }

    // Get dia by date
    suspend fun getDiaByDate(date: LocalDate): Dia? {
        val diaEntity = diaDao.getByDate(date.toEpochDay()) ?: return null
        return getDiaWithDetails(diaEntity.id)
    }

    // Update dia notes
    suspend fun updateDiaNotes(diaId: Long, notes: String) {
        val dia = diaDao.getById(diaId) ?: return
        diaDao.update(dia.copy(notes = notes))
    }
}
