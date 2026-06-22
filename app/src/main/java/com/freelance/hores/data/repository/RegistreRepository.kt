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
import com.freelance.hores.data.db.entity.EstatFacturacio
import com.freelance.hores.data.db.entity.RangHorariEntity
import com.freelance.hores.domain.model.Client
import com.freelance.hores.domain.model.Concepte
import com.freelance.hores.domain.model.Dia
import com.freelance.hores.domain.model.RangHorari
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

class RegistreRepository @Inject constructor(
    private val database: AppDatabase,
    private val diaDao: DiaDao,
    private val concepteDao: ConcepteDao,
    private val rangHorariDao: RangHorariDao,
    private val clientDao: ClientDao
) {
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // --- Client operations ---
    fun getClients(): Flow<List<Client>> {
        return clientDao.getAllClients().map { entities ->
            entities.map { Client(it.id, it.nom, it.preuHoraDefecte) }
        }
    }

    suspend fun saveClient(client: Client) {
        val id = client.id.ifEmpty { UUID.randomUUID().toString() }
        clientDao.insert(ClientEntity(id = id, nom = client.nom, preuHoraDefecte = client.preuHoraDefecte))
    }

    suspend fun deleteClient(client: Client) {
        clientDao.delete(ClientEntity(id = client.id, nom = client.nom, preuHoraDefecte = client.preuHoraDefecte))
    }

    // --- Registre operations ---
    // Get all dias with details
    fun getAllDiasWithDetails(): Flow<List<Dia>> {
        return diaDao.getAllDias().map { diasEntity ->
            diasEntity.map { entity ->
                Dia(
                    id = entity.id,
                    data = LocalDate.ofEpochDay(entity.data).toString(),
                    notes = entity.notes,
                    conceptes = getConceptesForDia(entity.id).first()
                )
            }
        }
    }

    // Get a specific dia with all its conceptes and rangs horaris
    suspend fun getDiaWithDetails(diaId: String): Dia {
        val diaEntity = diaDao.getById(diaId) ?: return Dia(id = UUID.randomUUID().toString(), data = LocalDate.now().toString())
        val conceptes = getConceptesForDia(diaId).first()
        return Dia(
            id = diaEntity.id,
            data = LocalDate.ofEpochDay(diaEntity.data).toString(),
            notes = diaEntity.notes,
            conceptes = conceptes
        )
    }

    // Get conceptes with their time ranges for a specific dia
    private fun getConceptesForDia(diaId: String): Flow<List<Concepte>> {
        return concepteDao.getByDiaId(diaId).map { entities ->
            entities.map { entity ->
                val rangsHoraris = getRangsForConcepte(entity.id).first()
                Concepte(
                    id = entity.id,
                    diaId = entity.diaId,
                    nom = entity.nom,
                    preuHora = entity.preuHora,
                    clientId = entity.clientId,
                    estat = entity.estat.name,
                    despeses = entity.despeses,
                    despesesNotes = entity.despesesNotes,
                    rangsHoraris = rangsHoraris,
                    preuFix = entity.preuFix,
                    importFix = entity.importFix
                )
            }
        }
    }

    // Get time ranges for a specific concepte
    private fun getRangsForConcepte(concepteId: String): Flow<List<RangHorari>> {
        return rangHorariDao.getByConcepteId(concepteId).map { entities ->
            entities.map { entity ->
                RangHorari(
                    id = entity.id,
                    concepteId = entity.concepteId,
                    horaInici = LocalTime.ofSecondOfDay(entity.horaInici).format(timeFormatter),
                    horaFi = LocalTime.ofSecondOfDay(entity.horaFi).format(timeFormatter)
                )
            }
        }
    }

    // Save or update a complete dia with conceptes and rangs horaris
    suspend fun saveDia(dia: Dia) {
        database.withTransaction {
            val diaId = dia.id.ifEmpty { UUID.randomUUID().toString() }
            val diaEntity = DiaEntity(
                id = diaId,
                data = LocalDate.parse(dia.data).toEpochDay(),
                notes = dia.notes
            )
            diaDao.insert(diaEntity)

            // 2. Clear existing relations to avoid duplicates and handle deletions
            val existingConceptes = concepteDao.getByDiaIdSync(diaId)
            for (concepte in existingConceptes) {
                concepteDao.delete(concepte) // Cascade will delete rangs horaris
            }

            // 3. Save new conceptes and their time ranges
            for (concepte in dia.conceptes) {
                val concepteId = concepte.id.ifEmpty { UUID.randomUUID().toString() }
                val concepteEntity = ConcepteEntity(
                    id = concepteId,
                    diaId = diaId,
                    clientId = concepte.clientId,
                    nom = concepte.nom,
                    preuHora = concepte.preuHora,
                    estat = EstatFacturacio.valueOf(concepte.estat),
                    despeses = concepte.despeses,
                    despesesNotes = concepte.despesesNotes,
                    preuFix = concepte.preuFix,
                    importFix = concepte.importFix
                )
                concepteDao.insert(concepteEntity)

                // Save rangs horaris
                for (rangHorari in concepte.rangsHoraris) {
                    val rangId = rangHorari.id.ifEmpty { UUID.randomUUID().toString() }
                    val rangEntity = RangHorariEntity(
                        id = rangId,
                        concepteId = concepteId,
                        horaInici = LocalTime.parse(rangHorari.horaInici, timeFormatter).toSecondOfDay().toLong(),
                        horaFi = LocalTime.parse(rangHorari.horaFi, timeFormatter).toSecondOfDay().toLong()
                    )
                    rangHorariDao.insert(rangEntity)
                }
            }
        }
    }

    // Delete a dia
    suspend fun deleteDia(dia: Dia) {
        diaDao.delete(DiaEntity(id = dia.id, data = LocalDate.parse(dia.data).toEpochDay(), notes = dia.notes))
    }

    // Get dia by date
    suspend fun getDiaByDate(date: LocalDate): Dia? {
        val diaEntity = diaDao.getByDate(date.toEpochDay()) ?: return null
        return getDiaWithDetails(diaEntity.id)
    }

    // Update dia notes
    suspend fun updateDiaNotes(diaId: String, notes: String) {
        val dia = diaDao.getById(diaId) ?: return
        diaDao.update(dia.copy(notes = notes))
    }
}
