package com.freelance.hores.ui.screen.gestio_dades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelance.hores.data.backup.BackupService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GestioDadesViewModel @Inject constructor(
    private val backupService: BackupService
) : ViewModel() {

    fun importarBaseDeDades(jsonString: String) {
        viewModelScope.launch {
            backupService.importFromJson(jsonString)
        }
    }

    suspend fun exportarBaseDeDades(): String {
        return backupService.exportToJson()
    }
}
