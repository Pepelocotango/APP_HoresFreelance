package com.freelance.hores.data.export

import android.content.Context
import com.freelance.hores.R
import com.freelance.hores.domain.model.Dia
import java.io.File
import java.time.format.DateTimeFormatter
import java.util.Locale

class CsvExporter(private val context: Context) {
    fun exportToCsv(dias: List<Dia>, filename: String = "hores_freelance.csv"): File {
        val file = File(context.cacheDir, filename)
        file.bufferedWriter().use { writer ->
            // Header
            val headers = listOf(
                context.getString(R.string.csv_header_date),
                context.getString(R.string.csv_header_concept),
                "Estat", // TODO: Add to strings.xml
                "Preu/h",
                "Despeses",
                "Notes despeses",
                context.getString(R.string.csv_header_start),
                context.getString(R.string.csv_header_end),
                context.getString(R.string.csv_header_duration),
                context.getString(R.string.csv_header_notes)
            ).joinToString(",")
            writer.write("$headers\n")

            // Rows
            var totalHoras = 0.0
            var totalDiners = 0.0
            var totalDespeses = 0.0
            for (dia in dias) {
                for (concepte in dia.conceptes) {
                    for ((index, rang) in concepte.rangsHoraris.withIndex()) {
                        val duracio = rang.getDuracionaEnHoras()
                        totalHoras += duracio
                        totalDiners += concepte.getTotalDiners()
                        totalDespeses += concepte.despeses
                        writer.write(
                            "${dia.data}," +
                            "${concepte.nom.replace(",", " ")}," +
                            "${concepte.estat.name}," +
                            "${String.format(Locale.US, "%.2f", concepte.preuHora)}," +
                            "${String.format(Locale.US, "%.2f", concepte.despeses)}," +
                            "${concepte.despesesNotes.replace(",", " ")}," +
                            "${rang.horaInici}," +
                            "${rang.horaFi}," +
                            "${String.format(Locale.US, "%.2f", duracio)}," +
                            "${if (index == 0) dia.notes.replace(",", " ").replace("\n", " ") else ""}\n"
                        )
                    }
                }
            }

            // Total
            writer.write(",,,,,,,,${String.format(Locale.US, "%.2f", totalHoras)},${String.format(Locale.US, "%.2f", totalDespeses)},${String.format(Locale.US, "%.2f", totalDiners)},${context.getString(R.string.csv_header_total)}\n")
        }
        return file
    }
}
