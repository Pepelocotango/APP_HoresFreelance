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
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
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

    suspend fun importFromJson(jsonString: String): Boolean {
        return try {
            // Permet llegir tant el format pla d'Android com el format de la PWA (embolcallat en "state")
            val jsonElement = Json.parseToJsonElement(jsonString)
            val appData = if (jsonElement is kotlinx.serialization.json.JsonObject && jsonElement.containsKey("state")) {
                // El format PWA/Desktop té les dades dins d'un objecte "state"
                val state = jsonElement.jsonObject["state"]?.jsonObject
                if (state != null) {
                     json.decodeFromJsonElement<AppData>(state)
                } else {
                    throw Exception("No s'ha trobat 'state' en el JSON")
                }
            } else {
                // Format Android directe
                json.decodeFromJsonElement<AppData>(jsonElement)
            }

            db.runInTransaction {
                // SQL directe per buidar de manera ràpida i neta
                db.compileStatement("DELETE FROM clients").execute()
                db.compileStatement("DELETE FROM dies").execute()
                // Els conceptes i rangs horaris s'esborren automàticament per CASCADE
            }

            // 1. Insertar clients
            appData.clients.forEach { client ->
                db.clientDao().insert(ClientEntity(client.id, client.nom, client.preuHoraDefecte))
            }

            // 2. Insertar dies i els seus detalls
            appData.dies.forEach { dia ->
                val safeDate = try {
                    LocalDate.parse(dia.data).toEpochDay()
                } catch (e: Exception) {
                    LocalDate.now().toEpochDay()
                }

                db.diaDao().insert(DiaEntity(dia.id, safeDate, dia.notes))

                dia.conceptes.forEach { concepte ->
                    // Seguretat: Evitar fallades si l'estat té problemes de majúscules/espais
                    val safeEstat = try {
                        EstatFacturacio.valueOf(concepte.estat.uppercase().trim())
                    } catch (e: Exception) {
                        EstatFacturacio.PENDENT
                    }

                    // Seguretat: Comprovar si el client existeix per evitar violacions de clau aliena (claus orfes)
                    val clientExists = appData.clients.any { it.id == concepte.clientId }
                    val safeClientId = if (clientExists) concepte.clientId else null

                    db.concepteDao().insert(ConcepteEntity(
                        id = concepte.id,
                        diaId = concepte.diaId,
                        clientId = safeClientId,
                        nom = concepte.nom.ifBlank { "Bolo sense títol" },
                        preuHora = concepte.preuHora,
                        estat = safeEstat,
                        despeses = concepte.despeses,
                        despesesNotes = concepte.despesesNotes,
                        preuFix = concepte.preuFix,
                        importFix = concepte.importFix
                    ))

                    concepte.rangsHoraris.forEach { rang ->
                        val safeHoraInici = try {
                            parseTime(rang.horaInici).toSecondOfDay().toLong()
                        } catch (e: Exception) {
                            0L // Fallback a les 00:00
                        }

                        val safeHoraFi = try {
                            parseTime(rang.horaFi).toSecondOfDay().toLong()
                        } catch (e: Exception) {
                            3600L // Fallback a les 01:00
                        }

                        db.rangHorariDao().insert(RangHorariEntity(
                            id = rang.id,
                            concepteId = rang.concepteId,
                            horaInici = safeHoraInici,
                            horaFi = safeHoraFi
                        ))
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun formatSeconds(seconds: Long): String {
        return LocalTime.ofSecondOfDay(seconds).format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    // Mètode de lectura robust per a diferents formats d'hores
    private fun parseTime(time: String): LocalTime {
        return try {
            LocalTime.parse(time.trim(), DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            try {
                // Intenta parsejar formats tipus H:m (p. ex: 9:00 en lloc de 09:00)
                LocalTime.parse(time.trim(), DateTimeFormatter.ofPattern("H:m"))
            } catch (e2: Exception) {
                LocalTime.of(0, 0)
            }
        }
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
