package com.freelance.hores.data.backup

import com.freelance.hores.data.json.JsonDataSource
import com.freelance.hores.domain.model.AppData
import com.freelance.hores.domain.model.Client
import com.freelance.hores.domain.model.Concepte
import com.freelance.hores.domain.model.Dia
import com.freelance.hores.domain.model.RangHorari
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Singleton
class BackupService @Inject constructor(
    private val jsonDataSource: JsonDataSource
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun exportToJson(): String {
        return jsonDataSource.exportToJson()
    }

    /**
     * Importa dades de JSON. Accepta dos formats:
     * 1. Format PWA: {"state": {clients: [...], dies: [...]}}
     * 2. Format Android: {clients: [...], dies: [...]}
     * 
     * Retorna: Pair<success: Boolean, message: String>
     */
    suspend fun importFromJson(jsonString: String): Pair<Boolean, String> {
        return try {
            // Parse JSON
            val jsonElement = Json.parseToJsonElement(jsonString)
            val appData = if (jsonElement is kotlinx.serialization.json.JsonObject && jsonElement.containsKey("state")) {
                // Format PWA amb "state" wrapper
                val state = jsonElement.jsonObject["state"]?.jsonObject
                if (state != null) {
                    json.decodeFromJsonElement<AppData>(state)
                } else {
                    return Pair(false, "Error: No s'ha trobat 'state' en el JSON")
                }
            } else {
                // Format Android directe
                json.decodeFromJsonElement<AppData>(jsonElement)
            }

            // ✅ NOVA: Validar coherència de dades
            val validationError = validateAppData(appData)
            if (validationError != null) {
                return Pair(false, validationError)
            }

            // ✅ NOVA: Sanititzar dades
            val sanitizedAppData = sanitizeAppData(appData)

            // Importar a jsonDataSource
            jsonDataSource.importFromJson(json.encodeToString(AppData.serializer(), sanitizedAppData))
            Pair(true, "Dades importades correctament")
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "Error en importar: ${e.message ?: "Unknown error"}")
        }
    }

    /**
     * ✅ NOVA FUNCIÓ: Valida coherència de les dades
     */
    private fun validateAppData(appData: AppData): String? {
        val clientIds = appData.clients.map { it.id }.toSet()
        val diaIds = appData.dies.map { it.id }.toSet()

        // Validar clients
        for (client in appData.clients) {
            if (client.id.isBlank()) {
                return "Error: Client sense ID"
            }
            if (client.nom.isBlank()) {
                return "Error: Client sense nom"
            }
            if (client.preuHoraDefecte < 0) {
                return "Error: Preu per hora negatiu en client '${client.nom}'"
            }
        }

        // Validar dies i conceptes
        for (dia in appData.dies) {
            if (dia.id.isBlank()) {
                return "Error: Dia sense ID"
            }
            
            // Validar format de data (YYYY-MM-DD)
            if (!isValidDate(dia.data)) {
                return "Error: Data inválida '${dia.data}'. Format esperat: YYYY-MM-DD"
            }

            // Validar conceptes dins del dia
            for (concepte in dia.conceptes) {
                if (concepte.id.isBlank()) {
                    return "Error: Concepte sense ID en dia ${dia.data}"
                }
                if (concepte.diaId != dia.id) {
                    return "Error: Concepte '${concepte.nom}' té diaId diferent (${concepte.diaId} vs ${dia.id})"
                }
                if (concepte.clientId != null && !clientIds.contains(concepte.clientId)) {
                    return "Error: Concepte '${concepte.nom}' referencia un client que no existeix: ${concepte.clientId}"
                }
                if (concepte.preuHora < 0) {
                    return "Error: Preu per hora negatiu en '${concepte.nom}'"
                }
                if (concepte.despeses < 0) {
                    return "Error: Despeses negatives en '${concepte.nom}'"
                }
                if (concepte.importFix < 0) {
                    return "Error: Import fix negatiu en '${concepte.nom}'"
                }

                // Validar rangs horaris
                for (rang in concepte.rangsHoraris) {
                    if (rang.id.isBlank()) {
                        return "Error: RangHorari sense ID"
                    }
                    if (rang.concepteId != concepte.id) {
                        return "Error: RangHorari té concepteId diferent"
                    }
                    if (!isValidTime(rang.horaInici)) {
                        return "Error: Hora inválida '${rang.horaInici}' en ${concepte.nom}. Format esperat: HH:mm"
                    }
                    if (!isValidTime(rang.horaFi)) {
                        return "Error: Hora inválida '${rang.horaFi}' en ${concepte.nom}. Format esperat: HH:mm"
                    }
                }
            }
        }

        return null  // Validació OK
    }

    /**
     * ✅ NOVA FUNCIÓ: Sanititza dades (corregeix valors inválids)
     */
    private fun sanitizeAppData(appData: AppData): AppData {
        val sanitizedClients = appData.clients.map { client ->
            client.copy(
                nom = client.nom.trim().ifEmpty { "Client sense nom" },
                preuHoraDefecte = if (client.preuHoraDefecte < 0) 0.0 else client.preuHoraDefecte
            )
        }

        val sanitizedDies = appData.dies.map { dia ->
            val sanitizedConceptes = dia.conceptes.map { concepte ->
                concepte.copy(
                    nom = concepte.nom.trim().ifEmpty { "Bolo sense títol" },
                    preuHora = if (concepte.preuHora < 0) 0.0 else concepte.preuHora,
                    despeses = if (concepte.despeses < 0) 0.0 else concepte.despeses,
                    importFix = if (concepte.importFix < 0) 0.0 else concepte.importFix,
                    despesesNotes = concepte.despesesNotes.trim(),
                    rangsHoraris = concepte.rangsHoraris.filter { 
                        isValidTime(it.horaInici) && isValidTime(it.horaFi)
                    }
                )
            }

            dia.copy(
                notes = dia.notes.trim(),
                conceptes = sanitizedConceptes
            )
        }

        return AppData(
            clients = sanitizedClients,
            dies = sanitizedDies
        )
    }

    private fun isValidDate(dateStr: String): Boolean {
        return try {
            LocalDate.parse(dateStr)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun isValidTime(timeStr: String): Boolean {
        return try {
            val parts = timeStr.split(":")
            if (parts.size != 2) return false
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            hour in 0..23 && minute in 0..59
        } catch (e: Exception) {
            false
        }
    }
}
