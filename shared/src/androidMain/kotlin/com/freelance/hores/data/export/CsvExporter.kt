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
                context.getString(R.string.csv_header_status),
                context.getString(R.string.csv_header_price_hour),
                context.getString(R.string.csv_header_expenses),
                context.getString(R.string.csv_header_expenses_notes),
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

            // Total
            writer.write(",,,TOTAL:,${String.format(Locale.US, "%.2f", totalDespeses)},,,,${String.format(Locale.US, "%.2f", totalHoras)},${String.format(Locale.US, "%.2f", totalDiners)}\n")
        }
        return file
    }
}
