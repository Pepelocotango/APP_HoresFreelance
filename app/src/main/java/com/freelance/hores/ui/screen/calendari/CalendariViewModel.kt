package com.freelance.hores.ui.screen.calendari

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelance.hores.data.repository.RegistreRepository
import com.freelance.hores.domain.model.Dia
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendariViewModel @Inject constructor(
    private val repository: RegistreRepository
) : ViewModel() {
    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _dias = MutableStateFlow<List<Dia>>(emptyList())
    val dias: StateFlow<List<Dia>> = _dias.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadDias()
    }

    fun loadDias() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val month = _currentMonth.value
                val startDate = month.atDay(1)
                val endDate = month.atEndOfMonth()
                repository.getDiasByDateRange(startDate, endDate).collect { dias ->
                    _dias.value = dias
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error loading calendar"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun setCurrentMonth(yearMonth: YearMonth) {
        _currentMonth.value = yearMonth
        loadDias()
    }

    fun nextMonth() {
        setCurrentMonth(_currentMonth.value.plusMonths(1))
    }

    fun previousMonth() {
        setCurrentMonth(_currentMonth.value.minusMonths(1))
    }

    fun getDiasByDate(date: LocalDate): Dia? {
        return _dias.value.find { it.data == date }
    }

    fun getDiasWithRecords(): List<LocalDate> {
        return _dias.value.map { it.data }
    }
}
