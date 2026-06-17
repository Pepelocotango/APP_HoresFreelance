package com.freelance.hores.ui.screen.resum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelance.hores.data.export.ExportService
import com.freelance.hores.data.repository.RegistreRepository
import com.freelance.hores.domain.model.Dia
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ResumState(
    val dias: List<Dia> = emptyList(),
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
        loadThisWeek()
    }

    private fun observePeriod() {
        viewModelScope.launch {
            _period
                .flatMapLatest { (start, end) ->
                    repository.getDiasByDateRange(start, end)
                        .onStart { _resumState.value = _resumState.value.copy(isLoading = true, error = null) }
                        .catch { e -> _resumState.value = _resumState.value.copy(isLoading = false, error = e.message ?: "An error occurred") }
                }
                .collect { dias ->
                    _resumState.value = _resumState.value.copy(
                        dias = dias,
                        startDate = _period.value.first,
                        endDate = _period.value.second,
                        isLoading = false
                    )
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
        return _resumState.value.dias.sumOf { it.getTotalHoras() }
    }

    fun getTotalDiners(): Double {
        return _resumState.value.dias.sumOf { dia -> dia.conceptes.sumOf { it.getTotalDiners() } }
    }

    fun getConceptesSummary(): Map<String, Double> {
        val summary = mutableMapOf<String, Double>()
        for (dia in _resumState.value.dias) {
            for (concepte in dia.conceptes) {
                summary[concepte.nom] = (summary[concepte.nom] ?: 0.0) + concepte.getTotalHoras()
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
