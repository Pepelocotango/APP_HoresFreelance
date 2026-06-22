package com.freelance.hores.data.export

import com.freelance.hores.domain.model.Dia
import kotlinx.datetime.LocalDate
import java.awt.Desktop
import java.io.File

class DesktopExportService : ExportService {

    override fun exportCsv(dias: List<Dia>, startDate: LocalDate, endDate: LocalDate) {
        val filename = "hores_freelance_${startDate}_${endDate}.csv"
        val file = File(System.getProperty("user.home"), ".horesfreelance/exports/$filename")
        file.parentFile?.mkdirs()
        file.writeText(buildCsvContent(dias))
        if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file)
    }

    override fun exportPdf(dias: List<Dia>, startDate: LocalDate, endDate: LocalDate) {
        val filename = "hores_freelance_${startDate}_${endDate}.pdf"
        val file = File(System.getProperty("user.home"), ".horesfreelance/exports/$filename")
        file.parentFile?.mkdirs()
        DesktopPdfExporter().exportToPdf(dias, file, startDate, endDate)
        if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file)
    }

    private fun buildCsvContent(dias: List<Dia>): String {
        val sb = StringBuilder()
        sb.appendLine("Data,Concepte,Preu/h,Inici,Fi,Durada (h),Total €,Despeses,Estat")
        for (dia in dias) {
            for (concepte in dia.conceptes) {
                for (rang in concepte.rangsHoraris) {
                    val duracio = rang.getDuracionaEnHoras()
                    val total = duracio * concepte.preuHora + concepte.despeses
                    sb.appendLine(
                        "${dia.data},${concepte.nom},${concepte.preuHora},${rang.horaInici},${rang.horaFi},%.2f,%.2f,${concepte.despeses},${concepte.estat}"
                            .format(duracio, total)
                    )
                }
            }
        }
        return sb.toString()
    }
}
