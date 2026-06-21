package com.freelance.hores.ui.screen.calendari

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelance.hores.data.repository.RegistreRepository
import com.freelance.hores.domain.model.Dia
import com.freelance.hores.util.atDay
import com.freelance.hores.util.atEndOfMonth
import com.freelance.hores.util.minusMonths
import com.freelance.hores.util.plusMonths
import com.freelance.hores.util.todayYearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import com.freelance.hores.util.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class CalendariViewModel(
    private val repository: RegistreRepository
) : ViewModel() {
    private val _currentMonth = MutableStateFlow(todayYearMonth())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _dias = MutableStateFlow<List<Dia>>(emptyList())
    val dias: StateFlow<List<Dia>> = _dias.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        observeDias()
    }

    private fun observeDias() {
        viewModelScope.launch {
            _currentMonth
                .flatMapLatest { month ->
                    val startDate = month.atDay(1)
                    val endDate = month.atEndOfMonth()
                    repository.getDiasByDateRange(startDate, endDate)
                        .onStart { _isLoading.value = true }
                        .catch { e -> _error.value = e.message ?: "Error loading calendar" }
                }
                .collect { dias ->
                    _dias.value = dias
                    _isLoading.value = false
                }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun setCurrentMonth(yearMonth: YearMonth) {
        _currentMonth.value = yearMonth
    }

    fun loadDias() {
        _currentMonth.value = _currentMonth.value
    }

    fun nextMonth() {
        setCurrentMonth(_currentMonth.value.plusMonths(1))
    }

    fun previousMonth() {
        setCurrentMonth(_currentMonth.value.minusMonths(1))
    }

    fun getDiasByDate(date: LocalDate): Dia? =
        _dias.value.find { it.data == date }

    fun getDiasWithRecords(): List<LocalDate> =
        _dias.value.map { it.data }
}
