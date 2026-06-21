package com.freelance.hores.data.export

import com.freelance.hores.domain.model.Dia
import kotlinx.datetime.LocalDate

interface ExportService {
    fun exportCsv(dias: List<Dia>, startDate: LocalDate, endDate: LocalDate)
    fun exportPdf(dias: List<Dia>, startDate: LocalDate, endDate: LocalDate)
}
