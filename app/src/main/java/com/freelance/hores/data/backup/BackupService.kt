package com.freelance.hores.data.backup

import android.content.Context
import com.freelance.hores.data.db.AppDatabase
import com.freelance.hores.data.db.entity.ClientEntity
import com.freelance.hores.data.db.entity.ConcepteEntity
import com.freelance.hores.data.db.entity.DiaEntity
import com.freelance.hores.data.db.entity.EstatFacturacio
import com.freelance.hores.data.db.entity.RangHorariEntity
import com.freelance.hores.domain.model.AppData
import com.freelance.hores.domain.model.Client
import com.freelance.hores.domain.model.Concepte
import com.freelance.hores.domain.model.Dia
import com.freelance.hores.domain.model.RangHorari
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    private val db = AppDatabase.getDatabase(context)

    suspend fun exportToJson(): String {
        val clients = db.clientDao().getAllClients().first().map {
            Client(it.id, it.nom, it.preuHoraDefecte)
        }

        val diesEntities = db.diaDao().getAllDias().first()
        val dies = diesEntities.map { diaEntity ->
            val conceptes = db.concepteDao().getByDiaIdSync(diaEntity.id).map { concepteEntity ->
                val rangs = db.rangHorariDao().getByConcepteIdSync(concepteEntity.id).map { rangEntity ->
                    RangHorari(
                        id = rangEntity.id,
                        concepteId = rangEntity.concepteId,
                        horaInici = formatSeconds(rangEntity.horaInici),
                        horaFi = formatSeconds(rangEntity.horaFi)
                    )
                }
                Concepte(
                    id = concepteEntity.id,
                    diaId = concepteEntity.diaId,
                    nom = concepteEntity.nom,
                    preuHora = concepteEntity.preuHora,
                    clientId = concepteEntity.clientId,
                    rangsHoraris = rangs,
                    estat = concepteEntity.estat.name,
                    despeses = concepteEntity.despeses,
                    despesesNotes = concepteEntity.despesesNotes,
                    preuFix = concepteEntity.preuFix,
                    importFix = concepteEntity.importFix
                )
            }
            Dia(
                id = diaEntity.id,
                data = LocalDate.ofEpochDay(diaEntity.data).toString(),
                notes = diaEntity.notes,
                conceptes = conceptes
            )
        }

        val appData = AppData(clients, dies)
        return json.encodeToString(appData)
    }

    suspend fun importFromJson(jsonString: String) {
        val appData = json.decodeFromString<AppData>(jsonString)

        db.runInTransaction {
            // Direct SQL for fast clear
            db.compileStatement("DELETE FROM clients").execute()
            db.compileStatement("DELETE FROM dies").execute()
            // conceptes and rangs_horaris are deleted by CASCADE
        }

        appData.clients.forEach { client ->
            db.clientDao().insert(ClientEntity(client.id, client.nom, client.preuHoraDefecte))
        }

        appData.dies.forEach { dia ->
            db.diaDao().insert(DiaEntity(dia.id, LocalDate.parse(dia.data).toEpochDay(), dia.notes))
            dia.conceptes.forEach { concepte ->
                db.concepteDao().insert(ConcepteEntity(
                    id = concepte.id,
                    diaId = concepte.diaId,
                    clientId = concepte.clientId,
                    nom = concepte.nom,
                    preuHora = concepte.preuHora,
                    estat = EstatFacturacio.valueOf(concepte.estat),
                    despeses = concepte.despeses,
                    despesesNotes = concepte.despesesNotes,
                    preuFix = concepte.preuFix,
                    importFix = concepte.importFix
                ))
                concepte.rangsHoraris.forEach { rang ->
                    db.rangHorariDao().insert(RangHorariEntity(
                        id = rang.id,
                        concepteId = rang.concepteId,
                        horaInici = parseTime(rang.horaInici).toSecondOfDay().toLong(),
                        horaFi = parseTime(rang.horaFi).toSecondOfDay().toLong()
                    ))
                }
            }
        }
    }

    private fun formatSeconds(seconds: Long): String {
        return LocalTime.ofSecondOfDay(seconds).format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    private fun parseTime(time: String): LocalTime {
        return LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
    }

    // Legacy backup methods (sqlite .db)
    fun exportDatabase(): File {
        db.close()
        AppDatabase.resetInstance()

        val dbFile = context.getDatabasePath("hores_database")
        val backupFile = File(context.cacheDir, "hores_database_backup.db")
        dbFile.inputStream().use { input ->
            backupFile.outputStream().use { output -> input.copyTo(output) }
        }
        return backupFile
    }

    fun importDatabase(inputStream: java.io.InputStream) {
        val dbFile = context.getDatabasePath("hores_database")
        
        db.close()
        AppDatabase.resetInstance()
        
        val walFile = File(dbFile.path + "-wal")
        val shmFile = File(dbFile.path + "-shm")
        if (walFile.exists()) walFile.delete()
        if (shmFile.exists()) shmFile.delete()
        
        inputStream.use { input ->
            dbFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}
