package com.freelance.hores.data.export

import android.content.Context
import com.freelance.hores.domain.model.Dia
import java.io.File
import java.util.Locale

class CsvExporter(private val context: Context) {
    fun exportToCsv(dias: List<Dia>, filename: String = "hores_freelance.csv"): File {
        val file = File(context.cacheDir, filename)
        file.bufferedWriter().use { writer ->
            writer.write(
                "Data,Concepte,Estat,Preu/h,Despeses,Notes despeses,Inici,Fi,Durada (h),Notes dia\n"
            )

            var totalHoras = 0.0
            var totalDiners = 0.0
            var totalDespeses = 0.0
            for (dia in dias) {
                for (concepte in dia.conceptes) {
                    totalDiners += concepte.getTotalDiners()
                    totalDespeses += concepte.despeses
                    for ((index, rang) in concepte.rangsHoraris.withIndex()) {
                        val duracio = rang.getDuracionaEnHoras()
                        totalHoras += duracio
                        writer.write(
                            "${dia.data}," +
                                "${concepte.nom.replace(",", " ")}," +
                                "${concepte.estat.name}," +
                                "${if (concepte.esPreuFix) "FIX: ${String.format(Locale.US, "%.2f", concepte.importPreuFix)}" else String.format(Locale.US, "%.2f", concepte.preuHora)}," +
                                "${if (index == 0) String.format(Locale.US, "%.2f", concepte.despeses) else "0.00"}," +
                                "${concepte.despesesNotes.replace(",", " ")}," +
                                "${rang.horaInici}," +
                                "${rang.horaFi}," +
                                "${String.format(Locale.US, "%.2f", duracio)}," +
                                "${if (index == 0) dia.notes.replace(",", " ").replace("\n", " ") else ""}\n"
                        )
                    }
                }
            }

            writer.write(
                ",,,TOTAL:,${String.format(Locale.US, "%.2f", totalDespeses)},,,,${String.format(Locale.US, "%.2f", totalHoras)},${String.format(Locale.US, "%.2f", totalDiners)}\n"
            )
        }
        return file
    }
}
