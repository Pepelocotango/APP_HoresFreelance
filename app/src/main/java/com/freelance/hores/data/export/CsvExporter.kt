package com.freelance.hores.data.export

import android.content.Context
import com.freelance.hores.R
import com.freelance.hores.ui.screen.resum.GroupedResumItem
import java.io.File
import java.util.Locale

class CsvExporter(private val context: Context) {
    fun exportToCsv(items: List<GroupedResumItem>, filename: String = "hores_export.csv"): File {
        val file = File(context.cacheDir, filename)
        file.bufferedWriter().use { writer ->
            // Header: Data, Client, Bolo, Rangs Horaris, Hores, Ingressos, Despeses, Estat
            val headers = listOf(
                context.getString(R.string.csv_header_date),
                context.getString(R.string.client_name),
                context.getString(R.string.csv_header_concept),
                context.getString(R.string.registre_add_rang),
                context.getString(R.string.csv_header_duration),
                context.getString(R.string.resum_total_diners_label),
                context.getString(R.string.expenses_title),
                context.getString(R.string.csv_header_status)
            ).joinToString(",")
            writer.write("$headers\n")

            // Rows
            items.forEach { item ->
                val ranges = item.rangsHoraris.joinToString(" | ") { "${it.horaInici}-${it.horaFi}" }
                val row = listOf(
                    item.data,
                    "\"${item.clientNom}\"",
                    "\"${item.conceptesNoms.joinToString(" & ")}\"",
                    "\"$ranges\"",
                    String.format(Locale.US, "%.2f", item.hours),
                    String.format(Locale.US, "%.2f", item.earnings),
                    String.format(Locale.US, "%.2f", item.despeses),
                    item.estat
                ).joinToString(",")
                writer.write("$row\n")
            }

            // Totals
            val totalHours = items.sumOf { it.hours }
            val totalEarnings = items.sumOf { it.earnings }
            val totalExpenses = items.sumOf { it.despeses }

            writer.write("\n,,,,${String.format(Locale.US, "%.2f", totalHours)},${String.format(Locale.US, "%.2f", totalEarnings)},${String.format(Locale.US, "%.2f", totalExpenses)},TOTAL\n")
        }
        return file
    }
}
