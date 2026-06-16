package com.freelance.hores.ui.screen.registre

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelance.hores.R
import com.freelance.hores.data.repository.RegistreRepository
import com.freelance.hores.domain.model.Concepte
import com.freelance.hores.domain.model.Dia
import com.freelance.hores.domain.model.RangHorari
import com.freelance.hores.ui.util.FormValidator
import com.freelance.hores.ui.util.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class RegistreFormState(
    val diaId: Long = 0,
    val data: LocalDate = LocalDate.now(),
    val notes: String = "",
    val conceptes: List<ConcepteForm> = emptyList(),
    val isSaving: Boolean = false,
    val errorResId: Int? = null,
    val error: String? = null,
    val success: Boolean = false
)

data class ConcepteForm(
    val id: Long = 0,
    val nom: String = "",
    val preuHora: Double = 0.0,
    val rangsHoraris: List<RangHorariForm> = emptyList()
)

data class RangHorariForm(
    val id: Long = 0,
    val horaInici: LocalTime = LocalTime.of(9, 0),
    val horaFi: LocalTime = LocalTime.of(17, 0)
)

@HiltViewModel
class RegistreViewModel @Inject constructor(
    private val repository: RegistreRepository
) : ViewModel() {
    private val _formState = MutableStateFlow(RegistreFormState())
    val formState: StateFlow<RegistreFormState> = _formState.asStateFlow()

    fun setData(data: LocalDate) {
        _formState.value = _formState.value.copy(data = data)
    }

    fun setNotes(notes: String) {
        _formState.value = _formState.value.copy(notes = notes)
    }

    fun addConcepte(concepteName: String = "", preu: Double = 0.0) {
        val currentConceptes = _formState.value.conceptes.toMutableList()
        currentConceptes.add(
            ConcepteForm(
                nom = concepteName,
                preuHora = preu,
                rangsHoraris = listOf(RangHorariForm())
            )
        )
        _formState.value = _formState.value.copy(conceptes = currentConceptes)
    }

    fun updateConcepteName(index: Int, nom: String) {
        val currentConceptes = _formState.value.conceptes.toMutableList()
        if (index >= 0 && index < currentConceptes.size) {
            currentConceptes[index] = currentConceptes[index].copy(nom = nom)
            _formState.value = _formState.value.copy(conceptes = currentConceptes)
        }
    }

    fun updateConceptePreu(index: Int, preu: Double) {
        val currentConceptes = _formState.value.conceptes.toMutableList()
        if (index >= 0 && index < currentConceptes.size) {
            currentConceptes[index] = currentConceptes[index].copy(preuHora = preu)
            _formState.value = _formState.value.copy(conceptes = currentConceptes)
        }
    }

    fun removeConcepte(index: Int) {
        val currentConceptes = _formState.value.conceptes.toMutableList()
        if (index >= 0 && index < currentConceptes.size) {
            currentConceptes.removeAt(index)
            _formState.value = _formState.value.copy(conceptes = currentConceptes)
        }
    }

    fun addRangHorariToConcepte(concepteIndex: Int) {
        val currentConceptes = _formState.value.conceptes.toMutableList()
        if (concepteIndex >= 0 && concepteIndex < currentConceptes.size) {
            val concepte = currentConceptes[concepteIndex]
            val newRangs = concepte.rangsHoraris.toMutableList()
            newRangs.add(RangHorariForm())
            currentConceptes[concepteIndex] = concepte.copy(rangsHoraris = newRangs)
            _formState.value = _formState.value.copy(conceptes = currentConceptes)
        }
    }

    fun updateRangHorariInici(concepteIndex: Int, rangIndex: Int, hora: LocalTime) {
        val currentConceptes = _formState.value.conceptes.toMutableList()
        if (concepteIndex >= 0 && concepteIndex < currentConceptes.size) {
            val concepte = currentConceptes[concepteIndex]
            val newRangs = concepte.rangsHoraris.toMutableList()
            if (rangIndex >= 0 && rangIndex < newRangs.size) {
                newRangs[rangIndex] = newRangs[rangIndex].copy(horaInici = hora)
                currentConceptes[concepteIndex] = concepte.copy(rangsHoraris = newRangs)
                _formState.value = _formState.value.copy(conceptes = currentConceptes)
            }
        }
    }

    fun updateRangHorariFi(concepteIndex: Int, rangIndex: Int, hora: LocalTime) {
        val currentConceptes = _formState.value.conceptes.toMutableList()
        if (concepteIndex >= 0 && concepteIndex < currentConceptes.size) {
            val concepte = currentConceptes[concepteIndex]
            val newRangs = concepte.rangsHoraris.toMutableList()
            if (rangIndex >= 0 && rangIndex < newRangs.size) {
                newRangs[rangIndex] = newRangs[rangIndex].copy(horaFi = hora)
                currentConceptes[concepteIndex] = concepte.copy(rangsHoraris = newRangs)
                _formState.value = _formState.value.copy(conceptes = currentConceptes)
            }
        }
    }

    fun removeRangHorari(concepteIndex: Int, rangIndex: Int) {
        val currentConceptes = _formState.value.conceptes.toMutableList()
        if (concepteIndex >= 0 && concepteIndex < currentConceptes.size) {
            val concepte = currentConceptes[concepteIndex]
            val newRangs = concepte.rangsHoraris.toMutableList()
            if (rangIndex >= 0 && rangIndex < newRangs.size) {
                newRangs.removeAt(rangIndex)
                currentConceptes[concepteIndex] = concepte.copy(rangsHoraris = newRangs)
                _formState.value = _formState.value.copy(conceptes = currentConceptes)
            }
        }
    }

    fun saveDia() {
        viewModelScope.launch {
            val state = _formState.value
            
            // 1. Validació bàsica de conceptes
            val countValidation = FormValidator.validateConceptesCount(state.conceptes.size)
            if (countValidation is ValidationResult.Error) {
                _formState.value = state.copy(errorResId = countValidation.resId)
                return@launch
            }

            for (concepte in state.conceptes) {
                // 2. Validació nom concepte
                val nameValidation = FormValidator.validateConcepteName(concepte.nom)
                if (nameValidation is ValidationResult.Error) {
                    _formState.value = state.copy(errorResId = nameValidation.resId)
                    return@launch
                }

                // 3. Validació rangs horaris
                val rangsCountValidation = FormValidator.validateTimeRangesCount(concepte.rangsHoraris.size)
                if (rangsCountValidation is ValidationResult.Error) {
                    _formState.value = state.copy(errorResId = rangsCountValidation.resId)
                    return@launch
                }
                
                // Sort ranges by start time to check for overlaps
                val sortedRangs = concepte.rangsHoraris.sortedBy { it.horaInici }
                for (i in sortedRangs.indices) {
                    val current = sortedRangs[i]
                    
                    // 4. Validació hora inici < hora fi
                    val rangeValidation = FormValidator.validateTimeRange(current.horaInici, current.horaFi)
                    if (rangeValidation is ValidationResult.Error) {
                        _formState.value = state.copy(errorResId = rangeValidation.resId)
                        return@launch
                    }

                    // 5. Validació solapaments
                    if (i > 0) {
                        val previous = sortedRangs[i - 1]
                        if (current.horaInici < previous.horaFi) {
                            _formState.value = state.copy(errorResId = R.string.registre_error_overlapping_ranges)
                            return@launch
                        }
                    }
                }
            }

            _formState.value = state.copy(isSaving = true, errorResId = null, error = null)
            try {
                val conceptesForSave = state.conceptes.map { concepteForm ->
                    Concepte(
                        id = concepteForm.id,
                        diaId = state.diaId,
                        nom = concepteForm.nom,
                        preuHora = concepteForm.preuHora,
                        rangsHoraris = concepteForm.rangsHoraris.map { rangForm ->
                            RangHorari(
                                id = rangForm.id,
                                concepteId = concepteForm.id,
                                horaInici = rangForm.horaInici,
                                horaFi = rangForm.horaFi
                            )
                        }
                    )
                }

                val diaToSave = Dia(
                    id = state.diaId,
                    data = state.data,
                    notes = state.notes,
                    conceptes = conceptesForSave
                )

                repository.saveDia(diaToSave)
                _formState.value = state.copy(
                    isSaving = false,
                    success = true,
                    errorResId = null,
                    error = null
                )
            } catch (e: Exception) {
                _formState.value = state.copy(
                    isSaving = false,
                    error = e.message ?: "An error occurred"
                )
            }
        }
    }

    fun clearError() {
        _formState.value = _formState.value.copy(errorResId = null, error = null)
    }

    fun resetSuccess() {
        _formState.value = _formState.value.copy(success = false)
    }

    fun loadDiaForEditing(diaId: Long) {
        viewModelScope.launch {
            try {
                val dia = repository.getDiaWithDetails(diaId)
                val concepteForms = dia.conceptes.map { concepte ->
                    ConcepteForm(
                        id = concepte.id,
                        nom = concepte.nom,
                        preuHora = concepte.preuHora,
                        rangsHoraris = concepte.rangsHoraris.map { rang ->
                            RangHorariForm(
                                id = rang.id,
                                horaInici = rang.horaInici,
                                horaFi = rang.horaFi
                            )
                        }
                    )
                }
                _formState.value = RegistreFormState(
                    diaId = dia.id,
                    data = dia.data,
                    notes = dia.notes,
                    conceptes = concepteForms
                )
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(error = e.message)
            }
        }
    }
}
