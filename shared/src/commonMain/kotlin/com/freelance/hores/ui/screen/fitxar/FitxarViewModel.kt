package com.freelance.hores.ui.screen.fitxar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelance.hores.data.db.entity.EstatFacturacio
import com.freelance.hores.data.repository.RegistreRepository
import com.freelance.hores.di.AppPrefs
import com.freelance.hores.domain.model.Concepte
import com.freelance.hores.domain.model.Dia
import com.freelance.hores.domain.model.RangHorari
import com.freelance.hores.ui.util.FormValidator
import com.freelance.hores.util.nowLocalTime
import com.freelance.hores.util.todayLocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class FitxarViewModel(
    private val repository: RegistreRepository,
    private val prefs: AppPrefs
) : ViewModel() {

    private val _isFitxant = MutableStateFlow(prefs.getBoolean("is_fitxant", false))
    val isFitxant: StateFlow<Boolean> = _isFitxant.asStateFlow()

    private val _horaIniciArrodonida = MutableStateFlow(prefs.getString("hora_inici_arrodonida", ""))
    val horaIniciArrodonida: StateFlow<String> = _horaIniciArrodonida.asStateFlow()

    private val _dataFitxatge = MutableStateFlow(prefs.getString("data_fitxatge", ""))
    val dataFitxatge: StateFlow<String> = _dataFitxatge.asStateFlow()

    fun startFitxar() {
        val now = nowLocalTime()
        val rounded = FormValidator.roundToNearest15Minutes(now)
        val today = todayLocalDate()

        val roundedStr = formatTime(rounded)
        val todayStr = today.toString()

        prefs.putBoolean("is_fitxant", true)
        prefs.putString("hora_inici_arrodonida", roundedStr)
        prefs.putString("data_fitxatge", todayStr)

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
            val startLocalTime = parseTime(startStr)
            val endLocalTime = FormValidator.roundToNearest15Minutes(nowLocalTime())

            val existentDia = repository.getDiaByDate(today)
            val diaId = existentDia?.id ?: 0L

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
                Dia(data = today, conceptes = listOf(nouConcepte))
            }

            repository.saveDia(diaAGuardar)

            val diaGuardat = repository.getDiaByDate(today)
            val finalDiaId = diaGuardat?.id ?: 0L

            prefs.putBoolean("is_fitxant", false)
            prefs.putString("hora_inici_arrodonida", "")
            prefs.putString("data_fitxatge", "")

            _isFitxant.value = false
            _horaIniciArrodonida.value = ""
            _dataFitxatge.value = ""

            onSuccess(finalDiaId)
        }
    }

    private fun formatTime(time: LocalTime): String =
        "%02d:%02d".format(time.hour, time.minute)

    private fun parseTime(value: String): LocalTime {
        val parts = value.split(":")
        return LocalTime(parts[0].toInt(), parts[1].toInt())
    }
}
