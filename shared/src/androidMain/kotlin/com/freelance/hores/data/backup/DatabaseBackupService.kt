package com.freelance.hores.data.backup

import android.content.Context
import java.io.File
import java.io.InputStream

class DatabaseBackupService(private val context: Context) {
    fun exportDatabase(): File {
        val dbFile = context.getDatabasePath("hores_database.db")
        val backup = File(context.cacheDir, "hores_backup.db")
        dbFile.inputStream().use { input ->
            backup.outputStream().use { output -> input.copyTo(output) }
        }
        return backup
    }

    fun importDatabase(input: InputStream) {
        val dbFile = context.getDatabasePath("hores_database.db")
        input.use { src ->
            dbFile.outputStream().use { dst -> src.copyTo(dst) }
        }
    }
}
