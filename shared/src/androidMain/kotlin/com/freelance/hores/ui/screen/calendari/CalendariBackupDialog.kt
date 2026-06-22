package com.freelance.hores.ui.screen.calendari

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.freelance.hores.data.backup.DatabaseBackupService

@Composable
actual fun CalendariBackupDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onImportComplete: () -> Unit
) {
    if (!visible) return

    val context = LocalContext.current
    val backupService = DatabaseBackupService(context)

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { output ->
                backupService.exportDatabase().inputStream().use { input -> input.copyTo(output) }
            }
        }
    }

    val loadLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { input ->
                backupService.importDatabase(input)
                onImportComplete()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Còpia de seguretat") },
        text = { Text("Gestió de la base de dades local.") },
        confirmButton = {
            TextButton(onClick = { saveLauncher.launch("hores_backup.db"); onDismiss() }) {
                Text("Exportar")
            }
            TextButton(onClick = { loadLauncher.launch(arrayOf("*/*")); onDismiss() }) {
                Text("Importar")
            }
        }
    )
}
