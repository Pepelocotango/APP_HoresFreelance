package com.freelance.hores.data.backup

import android.content.Context
import com.freelance.hores.data.db.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun exportDatabase(): File {
        val dbFile = context.getDatabasePath("hores_database")
        val backupFile = File(context.cacheDir, "hores_database_backup.db")
        
        dbFile.inputStream().use { input ->
            backupFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return backupFile
    }

    fun importDatabase(inputStream: java.io.InputStream) {
        val dbFile = context.getDatabasePath("hores_database")
        
        // Close DB before restoring
        AppDatabase.getDatabase(context).close()
        AppDatabase.resetInstance()
        
        inputStream.use { input ->
            dbFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}
