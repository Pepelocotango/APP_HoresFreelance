package com.freelance.hores.ui.screen.gestio_dades

import android.content.Context
import androidx.lifecycle.ViewModel
import com.freelance.hores.data.backup.BackupService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class GestioDadesViewModel @Inject constructor(
    private val backupService: BackupService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    fun importarBaseDeDades(inputStream: InputStream) {
        backupService.importDatabase(inputStream)
    }

    fun exportarBaseDeDades(): java.io.File {
        return backupService.exportDatabase()
    }
}
