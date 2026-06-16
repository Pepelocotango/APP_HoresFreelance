package com.freelance.hores.ui.screen.resum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelance.hores.data.export.ExportService
import com.freelance.hores.data.repository.RegistreRepository
import com.freelance.hores.domain.model.Dia
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class ResumState(
    val dias: List<Dia> = emptyList(),
    val startDate: LocalDate = LocalDate.now().minusWeeks(1),
    val endDate: LocalDate = LocalDate.now(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ResumViewModel @Inject constructor(
    private val repository: RegistreRepository,
    private val exportService: ExportService
) : ViewModel() {
    private val _resumState = MutableStateFlow(ResumState())
    val resumState: StateFlow<ResumState> = _resumState.asStateFlow()

    init {
        loadThisWeek()
    }

    fun loadThisWeek() {
        val today = LocalDate.now()
        val monday = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        val sunday = monday.plusDays(6)
        loadPeriod(monday, sunday)
    }

    fun loadThisMonth() {
        val today = LocalDate.now()
        val firstDay = today.withDayOfMonth(1)
        val lastDay = today.withDayOfMonth(today.lengthOfMonth())
        loadPeriod(firstDay, lastDay)
    }

    fun loadLastMonth() {
        val today = LocalDate.now()
        val lastMonthLastDay = today.withDayOfMonth(1).minusDays(1)
        val lastMonthFirstDay = lastMonthLastDay.withDayOfMonth(1)
        loadPeriod(lastMonthFirstDay, lastMonthLastDay)
    }

    fun loadCustomPeriod(startDate: LocalDate, endDate: LocalDate) {
        loadPeriod(startDate, endDate)
    }

    private fun loadPeriod(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            _resumState.value = _resumState.value.copy(isLoading = true, error = null)
            try {
                repository.getDiasByDateRange(startDate, endDate).collect { dias ->
                    _resumState.value = _resumState.value.copy(
                        dias = dias,
                        startDate = startDate,
                        endDate = endDate,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _resumState.value = _resumState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred"
                )
            }
        }
    }

    fun getTotalHoras(): Double {
        return _resumState.value.dias.sumOf { it.getTotalHoras() }
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

    fun exportCsv() {
        val state = _resumState.value
        exportService.exportCsv(state.dias, state.startDate, state.endDate)
    }

    fun exportPdf() {
        val state = _resumState.value
        exportService.exportPdf(state.dias, state.startDate, state.endDate)
    }
}
