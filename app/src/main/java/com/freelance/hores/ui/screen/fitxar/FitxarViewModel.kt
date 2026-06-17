package com.freelance.hores.ui.screen.fitxar

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelance.hores.data.db.entity.EstatFacturacio
import com.freelance.hores.data.repository.RegistreRepository
import com.freelance.hores.domain.model.Concepte
import com.freelance.hores.domain.model.Dia
import com.freelance.hores.domain.model.RangHorari
import com.freelance.hores.ui.util.FormValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class FitxarViewModel @Inject constructor(
    private val repository: RegistreRepository,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val _isFitxant = MutableStateFlow(prefs.getBoolean("is_fitxant", false))
    val isFitxant: StateFlow<Boolean> = _isFitxant.asStateFlow()

    private val _horaIniciArrodonida = MutableStateFlow(prefs.getString("hora_inici_arrodonida", "") ?: "")
    val horaIniciArrodonida: StateFlow<String> = _horaIniciArrodonida.asStateFlow()

    private val _dataFitxatge = MutableStateFlow(prefs.getString("data_fitxatge", "") ?: "")
    val dataFitxatge: StateFlow<String> = _dataFitxatge.asStateFlow()

    fun startFitxar() {
        val now = LocalTime.now()
        val rounded = FormValidator.roundToNearest15Minutes(now)
        val today = LocalDate.now()

        val roundedStr = rounded.format(DateTimeFormatter.ofPattern("HH:mm"))
        val todayStr = today.toString()

        prefs.edit().apply {
            putBoolean("is_fitxant", true)
            putString("hora_inici_arrodonida", roundedStr)
            putString("data_fitxatge", todayStr)
            apply()
        }

        _isFitxant.value = true
        _horaIniciArrodonida.value = roundedStr
        _dataFitxatge.value = todayStr
    }

    fun stopFitxar(onSuccess: (Long) -> Unit) {
        val startStr = _horaIniciArrodonida.value
        val dateStr = _dataFitxatge.value

        if (startStr.isEmpty() || dateStr.isEmpty()) return

        viewModelScope.launch {
            val today = LocalDate.parse(dateStr)
            val startLocalTime = LocalTime.parse(startStr)
            val endLocalTime = FormValidator.roundToNearest15Minutes(LocalTime.now())

            // Recupera o crea el Dia per a la data desada
            val existentDia = repository.getDiaByDate(today)
            val diaId = existentDia?.id ?: 0L

            // Comptem quants bolos "Bolo sense títol" ja té aquest dia per posar l'índex correcte
            val count = existentDia?.conceptes?.count { it.nom.startsWith("Bolo sense títol") } ?: 0
            val nouNom = "Bolo sense títol ${count + 1}"

            val nouConcepte = Concepte(
                diaId = diaId,
                nom = nouNom,
                preuHora = 0.0,
                estat = EstatFacturacio.PENDENT,
                rangsHoraris = listOf(
                    RangHorari(
                        concepteId = 0L,
                        horaInici = startLocalTime,
                        horaFi = endLocalTime
                    )
                )
            )

            val diaAGuardar = if (existentDia != null) {
                existentDia.copy(conceptes = existentDia.conceptes + nouConcepte)
            } else {
                Dia(
                    data = today,
                    conceptes = listOf(nouConcepte)
                )
            }

            repository.saveDia(diaAGuardar)

            // Obtenim l'ID definitiu de la DB
            val diaGuardat = repository.getDiaByDate(today)
            val finalDiaId = diaGuardat?.id ?: 0L

            // Esborrem dades temporals
            prefs.edit().apply {
                putBoolean("is_fitxant", false)
                putString("hora_inici_arrodonida", "")
                putString("data_fitxatge", "")
                apply()
            }

            _isFitxant.value = false
            _horaIniciArrodonida.value = ""
            _dataFitxatge.value = ""

            onSuccess(finalDiaId)
        }
    }
}
