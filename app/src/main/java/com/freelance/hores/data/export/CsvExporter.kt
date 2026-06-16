package com.freelance.hores.data.export

import android.content.Context
import com.freelance.hores.R
import com.freelance.hores.domain.model.Dia
import java.io.File
import java.time.format.DateTimeFormatter

class CsvExporter(private val context: Context) {
    fun exportToCsv(dias: List<Dia>, filename: String = "hores_freelance.csv"): File {
        val file = File(context.cacheDir, filename)
        file.bufferedWriter().use { writer ->
            // Header
            val headers = listOf(
                context.getString(R.string.csv_header_date),
                context.getString(R.string.csv_header_concept),
                context.getString(R.string.csv_header_start),
                context.getString(R.string.csv_header_end),
                context.getString(R.string.csv_header_duration),
                context.getString(R.string.csv_header_notes)
            ).joinToString(",")
            writer.write("$headers\n")

            // Rows
            var totalHoras = 0.0
            for (dia in dias) {
                for (concepte in dia.conceptes) {
                    for ((index, rang) in concepte.rangsHoraris.withIndex()) {
                        val duracio = rang.getDuracionaEnHoras()
                        totalHoras += duracio
                        writer.write(
                            "${dia.data}," +
                            "${concepte.nom.replace(",", " ")}," +
                            "${rang.horaInici}," +
                            "${rang.horaFi}," +
                            "${"%.2f".format(duracio)}," +
                            "${if (index == 0) dia.notes.replace(",", " ").replace("\n", " ") else ""}\n"
                        )
                    }
                }
            }

            // Total
            writer.write(",,,,${"%.2f".format(totalHoras)},${context.getString(R.string.csv_header_total)}\n")
        }
        return file
    }
}
