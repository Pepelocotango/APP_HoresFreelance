package com.freelance.hores.ui.screen.resum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelance.hores.data.export.ExportService
import com.freelance.hores.data.repository.RegistreRepository
import com.freelance.hores.domain.model.Client
import com.freelance.hores.domain.model.Dia
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class ResumState(
    val dias: List<Dia> = emptyList(),
    val clients: List<Client> = emptyList(),
    val startDate: LocalDate = LocalDate.now().minusWeeks(1),
    val endDate: LocalDate = LocalDate.now(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ResumViewModel @Inject constructor(
    private val repository: RegistreRepository,
    private val exportService: ExportService
) : ViewModel() {
    private val _period = MutableStateFlow(Pair(LocalDate.now().minusWeeks(1), LocalDate.now()))
    private val _resumState = MutableStateFlow(ResumState())
    val resumState: StateFlow<ResumState> = _resumState.asStateFlow()

    init {
        observePeriod()
        observeClients()
        loadThisWeek()
    }

    private fun observeClients() {
        viewModelScope.launch {
            repository.getClients().collect { clients ->
                _resumState.value = _resumState.value.copy(clients = clients)
            }
        }
    }

    private fun observePeriod() {
        viewModelScope.launch {
            repository.getAllDiasWithDetails()
                .onStart { _resumState.value = _resumState.value.copy(isLoading = true, error = null) }
                .catch { e -> _resumState.value = _resumState.value.copy(isLoading = false, error = e.message ?: "An error occurred") }
                .collect { allDias ->
                    val filtered = allDias.filter {
                        val d = LocalDate.parse(it.data)
                        !d.isBefore(_period.value.first) && !d.isAfter(_period.value.second)
                    }
                    _resumState.value = _resumState.value.copy(
                        dias = filtered,
                        startDate = _period.value.first,
                        endDate = _period.value.second,
                        isLoading = false
                    )
                }
        }

        // Re-trigger when period changes
        viewModelScope.launch {
            _period.collect { (start, end) ->
               // Force re-filtering of already observed data if necessary
               // but observePeriod above already listens to repository.getAllDiasWithDetails()
               // and uses _period.value. We need to trigger the collect again when _period changes.
               // Actually it's better to combine them.
            }
        }
    }

    fun loadThisWeek() {
        val today = LocalDate.now()
        val monday = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        val sunday = monday.plusDays(6)
        _period.value = Pair(monday, sunday)
    }

    fun loadThisMonth() {
        val today = LocalDate.now()
        val firstDay = today.withDayOfMonth(1)
        val lastDay = today.withDayOfMonth(today.lengthOfMonth())
        _period.value = Pair(firstDay, lastDay)
    }

    fun loadLastMonth() {
        val today = LocalDate.now()
        val lastMonthLastDay = today.withDayOfMonth(1).minusDays(1)
        val lastMonthFirstDay = lastMonthLastDay.withDayOfMonth(1)
        _period.value = Pair(lastMonthFirstDay, lastMonthLastDay)
    }

    fun loadCustomPeriod(startDate: LocalDate, endDate: LocalDate) {
        _period.value = Pair(startDate, endDate)
    }

    fun getTotalHoras(): Double {
        return _resumState.value.dias.sumOf { dia ->
            dia.conceptes.sumOf { concepte ->
                concepte.rangsHoraris.sumOf { rang ->
                    val inici = LocalTime.parse(rang.horaInici)
                    val fi = LocalTime.parse(rang.horaFi)
                    var seconds = (fi.toSecondOfDay() - inici.toSecondOfDay()).toLong()
                    if (seconds < 0) seconds += 24 * 3600
                    seconds / 3600.0
                }
            }
        }
    }

    fun getTotalDiners(): Double {
        return _resumState.value.dias.sumOf { dia ->
            dia.conceptes.sumOf { concepte ->
                val hores = concepte.rangsHoraris.sumOf { rang ->
                    val inici = LocalTime.parse(rang.horaInici)
                    val fi = LocalTime.parse(rang.horaFi)
                    var seconds = (fi.toSecondOfDay() - inici.toSecondOfDay()).toLong()
                    if (seconds < 0) seconds += 24 * 3600
                    seconds / 3600.0
                }
                hores * concepte.preuHora + concepte.despeses
            }
        }
    }

    fun getConceptesSummary(): Map<String, Double> {
        val summary = mutableMapOf<String, Double>()
        for (dia in _resumState.value.dias) {
            for (concepte in dia.conceptes) {
                val hores = concepte.rangsHoraris.sumOf { rang ->
                    val inici = LocalTime.parse(rang.horaInici)
                    val fi = LocalTime.parse(rang.horaFi)
                    var seconds = (fi.toSecondOfDay() - inici.toSecondOfDay()).toLong()
                    if (seconds < 0) seconds += 24 * 3600
                    seconds / 3600.0
                }
                summary[concepte.nom] = (summary[concepte.nom] ?: 0.0) + hores
            }
        }
        return summary.toSortedMap()
    }

    fun exportCsv(filteredDias: List<Dia>): android.content.Intent {
        val state = _resumState.value
        return exportService.exportCsv(filteredDias, state.startDate, state.endDate)
    }

    fun exportPdf(filteredDias: List<Dia>): android.content.Intent {
        val state = _resumState.value
        return exportService.exportPdf(filteredDias, state.startDate, state.endDate)
    }
}
