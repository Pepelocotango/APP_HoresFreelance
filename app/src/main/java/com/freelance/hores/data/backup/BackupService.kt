package com.freelance.hores.data.backup

import com.freelance.hores.data.json.JsonDataSource
import com.freelance.hores.domain.model.AppData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject
import javax.inject.Singleton

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

            jsonDataSource.importFromJson(json.encodeToString(AppData.serializer(), appData))
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
