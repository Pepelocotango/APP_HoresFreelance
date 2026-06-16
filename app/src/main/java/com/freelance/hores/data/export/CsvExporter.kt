package com.freelance.hores.data.export

import android.content.Context
import com.freelance.hores.domain.model.Dia
import java.io.File
import java.time.format.DateTimeFormatter

class CsvExporter(private val context: Context) {
    fun exportToCsv(dias: List<Dia>, filename: String = "hores_freelance.csv"): File {
        val file = File(context.cacheDir, filename)
        file.bufferedWriter().use { writer ->
            // Header
            writer.write("Data,Concepte,Hora inici,Hora fi,Duracio (h),Notes dia\n")

            // Rows
            var totalHoras = 0.0
            for (dia in dias) {
                for (concepte in dia.conceptes) {
                    for ((index, rang) in concepte.rangsHoraris.withIndex()) {
                        val duracio = rang.getDuracionaEnHoras()
                        totalHoras += duracio
                        writer.write(
                            "${dia.data}," +
                            "${concepte.nom}," +
                            "${rang.horaInici}," +
                            "${rang.horaFi}," +
                            "${"%.2f".format(duracio)}," +
                            "${if (index == 0) dia.notes else ""}\n"
                        )
                    }
                }
            }

            // Total
            writer.write(",,,,${"%.2f".format(totalHoras)},\n")
        }
        return file
    }
}
