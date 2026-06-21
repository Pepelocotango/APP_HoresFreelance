package com.freelance.hores.data.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.freelance.hores.domain.model.Dia
import kotlinx.datetime.LocalDate
import java.io.File

class AndroidExportService(private val context: android.content.Context) : com.freelance.hores.data.export.ExportService {

    private val csvExporter = CsvExporter(context)
    private val pdfExporter = PdfExporter(context)

    override fun exportCsv(dias: List<Dia>, startDate: LocalDate, endDate: LocalDate) {
        val filename = buildFilename("hores_freelance", "csv", startDate, endDate)
        val file = csvExporter.exportToCsv(dias, filename)
        shareFile(file, "text/csv")
    }

    override fun exportPdf(dias: List<Dia>, startDate: LocalDate, endDate: LocalDate) {
        val filename = buildFilename("hores_freelance", "pdf", startDate, endDate)
        val file = pdfExporter.exportToPdf(dias, filename, startDate, endDate)
        shareFile(file, "application/pdf")
    }

    private fun buildFilename(
        prefix: String,
        extension: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): String = "${prefix}_${startDate}_${endDate}.$extension"

    private fun shareFile(file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
