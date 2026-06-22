package com.freelance.hores.data.export

import com.freelance.hores.domain.model.Dia
import kotlinx.datetime.LocalDate
import java.io.File

class DesktopPdfExporter {
    fun exportToPdf(dias: List<Dia>, file: File, startDate: LocalDate, endDate: LocalDate) {
        val sb = StringBuilder()
        sb.appendLine("HoresFreelance — Informe $startDate / $endDate")
        sb.appendLine("=".repeat(60))
        for (dia in dias) {
            sb.appendLine("\n${dia.data}")
            for (concepte in dia.conceptes) {
                for (rang in concepte.rangsHoraris) {
                    val duracio = rang.getDuracionaEnHoras()
                    sb.appendLine(
                        "  ${concepte.nom} | ${rang.horaInici}-${rang.horaFi} | %.2fh | %.2f€"
                            .format(duracio, duracio * concepte.preuHora)
                    )
                }
            }
        }
        file.writeText(sb.toString())
    }
}
