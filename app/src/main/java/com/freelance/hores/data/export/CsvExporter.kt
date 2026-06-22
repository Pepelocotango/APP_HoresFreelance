package com.freelance.hores.data.export

import android.content.Context
import com.freelance.hores.R
import com.freelance.hores.domain.model.Dia
import java.io.File
import java.time.LocalTime
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
                    for ((index, rang) in concepte.rangsHoraris.withIndex()) {
                        val hInici = LocalTime.parse(rang.horaInici)
                        val hFi = LocalTime.parse(rang.horaFi)
                        var seconds = (hFi.toSecondOfDay() - hInici.toSecondOfDay()).toLong()
                        if (seconds < 0) seconds += 24 * 3600
                        val duracio = seconds / 3600.0

                        totalHoras += duracio
                        // Total diners of concepte is only added once? No, CSV usually expects line by line.
                        // But for total at bottom it might be confusing if we add concepte.getTotalDiners() for every range.
                        // Let's calculate the portion for this range.
                        val dinersRang = duracio * concepte.preuHora + (if (index == 0) concepte.despeses else 0.0)
                        totalDiners += dinersRang
                        totalDespeses += (if (index == 0) concepte.despeses else 0.0)

                        writer.write(
                            "${dia.data}," +
                            "${concepte.nom.replace(",", " ")}," +
                            "${concepte.estat}," +
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
            writer.write(",,,TOTAL:,${String.format(Locale.US, "%.2f", totalDespeses)},,,,${String.format(Locale.US, "%.2f", totalHoras)},${String.format(Locale.US, "%.2f", totalDiners)}\n")
        }
        return file
    }
}
