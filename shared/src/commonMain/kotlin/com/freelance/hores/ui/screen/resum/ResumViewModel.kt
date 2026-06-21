package com.freelance.hores.ui.screen.resum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelance.hores.data.export.ExportService
import com.freelance.hores.data.repository.RegistreRepository
import com.freelance.hores.domain.model.Client
import com.freelance.hores.domain.model.Dia
import com.freelance.hores.util.lengthOfMonth
import com.freelance.hores.util.minusDays
import com.freelance.hores.util.plusDays
import com.freelance.hores.util.isoDayOfWeek
import com.freelance.hores.util.todayLocalDate
import com.freelance.hores.util.withDayOfMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate

data class ResumState(
    val dias: List<Dia> = emptyList(),
    val clients: List<Client> = emptyList(),
    val startDate: LocalDate = todayLocalDate().minusDays(7),
    val endDate: LocalDate = todayLocalDate(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class ResumViewModel(
    private val repository: RegistreRepository,
    private val exportService: ExportService
) : ViewModel() {
    private val _period = MutableStateFlow(Pair(todayLocalDate().minusDays(7), todayLocalDate()))
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
            _period
                .flatMapLatest { (start, end) ->
                    repository.getDiasByDateRange(start, end)
                        .onStart {
                            _resumState.value = _resumState.value.copy(isLoading = true, error = null)
                        }
                        .catch { e ->
                            _resumState.value = _resumState.value.copy(
                                isLoading = false,
                                error = e.message ?: "An error occurred"
                            )
                        }
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
        val today = todayLocalDate()
        val daysFromMonday = today.isoDayOfWeek() - 1
        val monday = today.minusDays(daysFromMonday.toLong())
        val sunday = monday.plusDays(6)
        _period.value = Pair(monday, sunday)
    }

    fun loadThisMonth() {
        val today = todayLocalDate()
        val firstDay = today.withDayOfMonth(1)
        val lastDay = today.withDayOfMonth(today.lengthOfMonth())
        _period.value = Pair(firstDay, lastDay)
    }

    fun loadLastMonth() {
        val today = todayLocalDate()
        val lastMonthLastDay = today.withDayOfMonth(1).minusDays(1)
        val lastMonthFirstDay = lastMonthLastDay.withDayOfMonth(1)
        _period.value = Pair(lastMonthFirstDay, lastMonthLastDay)
    }

    fun loadCustomPeriod(startDate: LocalDate, endDate: LocalDate) {
        _period.value = Pair(startDate, endDate)
    }

    fun getTotalHoras(): Double = _resumState.value.dias.sumOf { it.getTotalHoras() }

    fun getTotalDiners(): Double =
        _resumState.value.dias.sumOf { dia -> dia.conceptes.sumOf { it.getTotalDiners() } }

    fun getConceptesSummary(): Map<String, Double> {
        val summary = mutableMapOf<String, Double>()
        for (dia in _resumState.value.dias) {
            for (concepte in dia.conceptes) {
                summary[concepte.nom] = (summary[concepte.nom] ?: 0.0) + concepte.getTotalHoras()
            }
        }
        return summary.toSortedMap()
    }

    fun exportCsv(filteredDias: List<Dia>) {
        val state = _resumState.value
        exportService.exportCsv(filteredDias, state.startDate, state.endDate)
    }

    fun exportPdf(filteredDias: List<Dia>) {
        val state = _resumState.value
        exportService.exportPdf(filteredDias, state.startDate, state.endDate)
    }
}
