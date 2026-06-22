package com.freelance.hores.ui.screen.dia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelance.hores.data.repository.RegistreRepository
import com.freelance.hores.domain.model.Dia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiaDetallViewModel constructor(
    private val repository: RegistreRepository
) : ViewModel() {
    private val _dia = MutableStateFlow<Dia?>(null)
    val dia: StateFlow<Dia?> = _dia.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadDia(diaId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val diaDetall = repository.getDiaWithDetails(diaId)
                _dia.value = diaDetall
            } catch (e: Exception) {
                _error.value = e.message ?: "Error loading day details"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateDiaNotes(notes: String) {
        val currentDia = _dia.value ?: return
        viewModelScope.launch {
            _error.value = null
            try {
                repository.updateDiaNotes(currentDia.id, notes)
                _dia.value = currentDia.copy(notes = notes)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error updating notes"
            }
        }
    }

    fun deleteDia() {
        val currentDia = _dia.value ?: return
        viewModelScope.launch {
            _error.value = null
            try {
                repository.deleteDia(currentDia)
                _dia.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Error deleting day"
            }
        }
    }

    fun deleteConcepte(concepte: com.freelance.hores.domain.model.Concepte) {
        val currentDia = _dia.value ?: return
        viewModelScope.launch {
            _error.value = null
            try {
                // We'll need a way to delete conceptes in RegistreRepository that handles String IDs
                // For now, let's just use the saveDia with filtered conceptes if we don't want to add a new method
                val updatedConceptes = currentDia.conceptes.filter { it.id != concepte.id }
                repository.saveDia(currentDia.copy(conceptes = updatedConceptes))
                loadDia(currentDia.id)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error deleting bolo"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
