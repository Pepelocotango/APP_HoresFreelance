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
        // Tanquem la base de dades abans de fer la còpia per assegurar-nos que 
        // tot el registre WAL s'ha bolcat (checkpoint) al fitxer principal .db
        AppDatabase.getDatabase(context).close()
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
        
        AppDatabase.getDatabase(context).close()
        AppDatabase.resetInstance()
        
        // Eliminem els fitxers temporals del WAL abans d'escriure el nou fitxer
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
