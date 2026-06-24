package com.freelance.hores.ui.screen.resum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelance.hores.data.export.ExportService
import com.freelance.hores.data.repository.RegistreRepository
import com.freelance.hores.domain.model.Client
import com.freelance.hores.domain.model.Dia
import com.freelance.hores.domain.model.Concepte
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

enum class DateRangeType {
    SETMANA, MES, ANTERIOR, TOTS, LLIURE
}

data class GroupedResumItem(
    val diaId: String,
    val data: String,
    val clientNom: String,
    val conceptesNoms: List<String>,
    val rangsHoraris: List<com.freelance.hores.domain.model.RangHorari>,
    val hours: Double,
    val earnings: Double,
    val despeses: Double,
    val estat: String
)

data class ResumState(
    val dias: List<Dia> = emptyList(),
    val filteredItems: List<GroupedResumItem> = emptyList(),
    val clients: List<Client> = emptyList(),
    val startDate: LocalDate? = LocalDate.now().withDayOfMonth(1),
    val endDate: LocalDate? = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()),
    val rangeType: DateRangeType = DateRangeType.MES,
    val statusFilter: String = "Tots",
    val clientFilter: String = "Tots",
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

    private val _allDias = MutableStateFlow<List<Dia>>(emptyList())

    init {
        loadData()
        observeClients()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getAllDiasWithDetails()
                .onStart { _resumState.update { it.copy(isLoading = true) } }
                .catch { e -> _resumState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { dias ->
                    _allDias.value = dias
                    updateFilteredData()
                }
        }
    }

    private fun observeClients() {
        viewModelScope.launch {
            repository.getClients().collect { clients ->
                _resumState.update { it.copy(clients = clients) }
            }
        }
    }

    fun setRangeType(type: DateRangeType) {
        val now = LocalDate.now()
        var start: LocalDate? = null
        var end: LocalDate? = null

        when (type) {
            DateRangeType.SETMANA -> {
                start = now.minusDays(now.dayOfWeek.value.toLong() - 1)
                end = start.plusDays(6)
            }
            DateRangeType.MES -> {
                start = now.withDayOfMonth(1)
                end = now.withDayOfMonth(now.lengthOfMonth())
            }
            DateRangeType.ANTERIOR -> {
                val lastMonth = now.minusMonths(1)
                start = lastMonth.withDayOfMonth(1)
                end = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth())
            }
            DateRangeType.TOTS -> {
                start = null
                end = null
            }
            DateRangeType.LLIURE -> {
                start = _resumState.value.startDate
                end = _resumState.value.endDate
            }
        }

        _resumState.update { it.copy(rangeType = type, startDate = start, endDate = end) }
        updateFilteredData()
    }

    fun setCustomPeriod(start: LocalDate?, end: LocalDate?) {
        _resumState.update { it.copy(rangeType = DateRangeType.LLIURE, startDate = start, endDate = end) }
        updateFilteredData()
    }

    fun setStatusFilter(status: String) {
        _resumState.update { it.copy(statusFilter = status) }
        updateFilteredData()
    }

    fun setClientFilter(clientId: String) {
        _resumState.update { it.copy(clientFilter = clientId) }
        updateFilteredData()
    }

    private fun updateFilteredData() {
        val state = _resumState.value
        val allDias = _allDias.value
        val start = state.startDate
        val end = state.endDate
        val statusFilter = state.statusFilter
        val clientFilter = state.clientFilter

        val groupedRecords = mutableMapOf<String, GroupedResumItem>()

        allDias.forEach { dia ->
            val dDate = LocalDate.parse(dia.data)

            // Date filtering
            if (start != null && dDate.isBefore(start)) return@forEach
            if (end != null && dDate.isAfter(end)) return@forEach

            dia.conceptes.forEach { concepte ->
                // Status filtering
                if (statusFilter != "Tots" && concepte.estat != statusFilter) return@forEach

                // Client filtering
                if (clientFilter != "Tots" && concepte.clientId != clientFilter) return@forEach

                val client = state.clients.find { it.id == concepte.clientId }
                val hours = concepte.getTotalHoras()
                val earnings = concepte.getDinersHores()
                val despeses = concepte.getDinersDespeses()

                val groupKey = "${dia.id}_${concepte.clientId ?: "no-client"}"

                val existing = groupedRecords[groupKey]
                if (existing == null) {
                    groupedRecords[groupKey] = GroupedResumItem(
                        diaId = dia.id,
                        data = dia.data,
                        clientNom = client?.nom ?: concepte.clientNom ?: "Desconegut",
                        conceptesNoms = listOf(concepte.nom),
                        rangsHoraris = concepte.rangsHoraris,
                        hours = hours,
                        earnings = earnings,
                        despeses = despeses,
                        estat = concepte.estat
                    )
                } else {
                    groupedRecords[groupKey] = existing.copy(
                        conceptesNoms = existing.conceptesNoms + concepte.nom,
                        rangsHoraris = existing.rangsHoraris + concepte.rangsHoraris,
                        hours = existing.hours + hours,
                        earnings = existing.earnings + earnings,
                        despeses = existing.despeses + despeses
                    )
                }
            }
        }

        _resumState.update { it.copy(
            filteredItems = groupedRecords.values.sortedByDescending { item -> item.data },
            isLoading = false
        ) }
    }

    // Adapt for export (might need to map GroupedResumItem back to Dia/Concepte if service expects it)
    fun exportCsv(): android.content.Intent {
        val state = _resumState.value
        // The export service might need a list of Dia. We can reconstruct them or modify the service.
        // For now let's try to pass what it expects.
        // If it expects List<Dia>, we might need to be careful with the grouping.
        return exportService.exportCsv(reconstructDias(state.filteredItems), state.startDate ?: LocalDate.MIN, state.endDate ?: LocalDate.MAX)
    }

    fun exportPdf(): android.content.Intent {
        val state = _resumState.value
        return exportService.exportPdf(reconstructDias(state.filteredItems), state.startDate ?: LocalDate.MIN, state.endDate ?: LocalDate.MAX)
    }

    private fun reconstructDias(items: List<GroupedResumItem>): List<Dia> {
        return items.groupBy { it.diaId }.map { (diaId, itemsInDia) ->
            Dia(
                id = diaId,
                data = itemsInDia.first().data,
                conceptes = itemsInDia.map { item ->
                    Concepte(
                        id = UUID.randomUUID().toString(),
                        diaId = diaId,
                        nom = item.conceptesNoms.joinToString(" & "),
                        clientId = null, // Not strictly needed for export if clientNom is there
                        clientNom = item.clientNom,
                        rangsHoraris = item.rangsHoraris,
                        estat = item.estat,
                        despeses = item.despeses,
                        importFix = item.earnings,
                        preuFix = true // simplify for export
                    )
                }
            )
        }
    }
}
