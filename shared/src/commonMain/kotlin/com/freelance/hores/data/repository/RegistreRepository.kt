package com.freelance.hores.data.repository

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
import com.freelance.hores.util.localTimeFromSecondOfDay
import com.freelance.hores.util.todayLocalDate
import com.freelance.hores.util.totalSecondsFromMidnight
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class RegistreRepository(
    private val database: AppDatabase,
    private val diaDao: DiaDao,
    private val concepteDao: ConcepteDao,
    private val rangHorariDao: RangHorariDao,
    private val clientDao: ClientDao
) {
    fun getClients(): Flow<List<Client>> {
        return clientDao.getAllClients().map { entities ->
            entities.map { Client(it.id, it.nom, it.preuHoraDefecte) }
        }
    }

    suspend fun saveClient(client: Client) {
        clientDao.insert(ClientEntity(id = client.id, nom = client.nom, preuHoraDefecte = client.preuHoraDefecte))
        if (client.id > 0) {
            concepteDao.updatePreuHoraForPendent(client.id, client.preuHoraDefecte)
        }
    }

    suspend fun deleteClient(client: Client) {
        clientDao.delete(ClientEntity(id = client.id, nom = client.nom, preuHoraDefecte = client.preuHoraDefecte))
    }

    fun getAllDiasWithDetails(): Flow<List<Dia>> {
        return diaDao.getAllDias().mapLatest { diasEntity ->
            diasEntity.map { entity ->
                Dia(
                    id = entity.id,
                    data = LocalDate.fromEpochDays(entity.data.toInt()),
                    notes = entity.notes,
                    conceptes = getConceptesForDia(entity.id)
                )
            }
        }
    }

    suspend fun getDiaWithDetails(diaId: Long): Dia {
        val diaEntity = diaDao.getById(diaId) ?: return Dia(data = todayLocalDate())
        val conceptes = getConceptesForDia(diaId)
        return Dia(
            id = diaEntity.id,
            data = LocalDate.fromEpochDays(diaEntity.data.toInt()),
            notes = diaEntity.notes,
            conceptes = conceptes
        )
    }

    private suspend fun getConceptesForDia(diaId: Long): List<Concepte> {
        val concepteEntities = concepteDao.getByDiaIdSync(diaId)
        return concepteEntities.map { concepteEntity ->
            val clientNom = concepteEntity.clientId?.let { clientId ->
                clientDao.getById(clientId)?.nom
            }
            val rangsHoraris = getRangsForConcepte(concepteEntity.id)
            Concepte(
                id = concepteEntity.id,
                diaId = concepteEntity.diaId,
                nom = concepteEntity.nom,
                preuHora = concepteEntity.preuHora,
                clientId = concepteEntity.clientId,
                clientNom = clientNom,
                estat = concepteEntity.estat,
                despeses = concepteEntity.despeses,
                despesesNotes = concepteEntity.despesesNotes,
                esPreuFix = concepteEntity.esPreuFix,
                importPreuFix = concepteEntity.importPreuFix,
                rangsHoraris = rangsHoraris
            )
        }
    }

    private suspend fun getRangsForConcepte(concepteId: Long): List<RangHorari> {
        val rangEntities = rangHorariDao.getByConcepteIdSync(concepteId)
        return rangEntities.map { rangEntity ->
            RangHorari(
                id = rangEntity.id,
                concepteId = rangEntity.concepteId,
                horaInici = localTimeFromSecondOfDay(rangEntity.horaInici),
                horaFi = localTimeFromSecondOfDay(rangEntity.horaFi)
            )
        }
    }

    fun getDiasByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Dia>> {
        return diaDao.getDiasByDateRange(
            startDate.toEpochDays().toLong(),
            endDate.toEpochDays().toLong()
        ).mapLatest { diasEntity ->
            diasEntity.map { diaEntity ->
                Dia(
                    id = diaEntity.id,
                    data = LocalDate.fromEpochDays(diaEntity.data.toInt()),
                    notes = diaEntity.notes,
                    conceptes = getConceptesForDia(diaEntity.id)
                )
            }
        }
    }

    suspend fun saveDia(dia: Dia): Long {
        val diaEntity = DiaEntity(
            id = dia.id,
            data = dia.data.toEpochDays().toLong(),
            notes = dia.notes
        )
        val actualDiaId = if (dia.id > 0) {
            diaDao.update(diaEntity)
            dia.id
        } else {
            diaDao.insert(diaEntity)
        }

        val existingConceptes = concepteDao.getByDiaIdSync(actualDiaId)
        for (concepteEntity in existingConceptes) {
            concepteDao.delete(concepteEntity)
        }

        for (concepte in dia.conceptes) {
            val concepteEntity = ConcepteEntity(
                diaId = actualDiaId,
                clientId = concepte.clientId,
                nom = concepte.nom,
                preuHora = concepte.preuHora,
                estat = concepte.estat,
                despeses = concepte.despeses,
                despesesNotes = concepte.despesesNotes,
                esPreuFix = concepte.esPreuFix,
                importPreuFix = concepte.importPreuFix
            )
            val concepteId = concepteDao.insert(concepteEntity)

            for (rangHorari in concepte.rangsHoraris) {
                val rangEntity = RangHorariEntity(
                    concepteId = concepteId,
                    horaInici = rangHorari.horaInici.totalSecondsFromMidnight(),
                    horaFi = rangHorari.horaFi.totalSecondsFromMidnight()
                )
                rangHorariDao.insert(rangEntity)
            }
        }
        return actualDiaId
    }

    suspend fun deleteDia(dia: Dia) {
        diaDao.delete(DiaEntity(id = dia.id, data = dia.data.toEpochDays().toLong(), notes = dia.notes))
    }

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
                despesesNotes = concepte.despesesNotes,
                esPreuFix = concepte.esPreuFix,
                importPreuFix = concepte.importPreuFix
            )
        )
    }

    suspend fun deleteRangHorari(rangHorari: RangHorari) {
        rangHorariDao.delete(
            RangHorariEntity(
                id = rangHorari.id,
                concepteId = rangHorari.concepteId,
                horaInici = rangHorari.horaInici.totalSecondsFromMidnight(),
                horaFi = rangHorari.horaFi.totalSecondsFromMidnight()
            )
        )
    }

    suspend fun getDiaByDate(date: LocalDate): Dia? {
        val diaEntity = diaDao.getByDate(date.toEpochDays().toLong()) ?: return null
        return getDiaWithDetails(diaEntity.id)
    }

    suspend fun updateDiaNotes(diaId: Long, notes: String) {
        val dia = diaDao.getById(diaId) ?: return
        diaDao.update(dia.copy(notes = notes))
    }
}
