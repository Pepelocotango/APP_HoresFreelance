package com.freelance.hores.data.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import com.freelance.hores.R
import com.freelance.hores.ui.screen.resum.GroupedResumItem
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PdfExporter(private val context: Context) {
    fun exportToPdf(
        items: List<GroupedResumItem>,
        filename: String = "hores_export.pdf",
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): File {
        val file = File(context.cacheDir, filename)

        val pdfDocument = PdfDocument()
        val pageWidth = 842 // Landscape A4
        val pageHeight = 595
        
        val titlePaint = Paint().apply {
            textSize = 18f
            isFakeBoldText = true
        }

        val headerPaint = Paint().apply {
            textSize = 10f
            isFakeBoldText = true
        }

        val textPaint = Paint().apply {
            textSize = 9f
        }

        val footerPaint = Paint().apply {
            textSize = 10f
            isFakeBoldText = true
        }

        var pageNumber = 1
        var pageInfo = PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        var y = 50f
        val margin = 30f
        val lineHeight = 20f

        // Title and filters (matching PWA)
        val periodTitle = context.getString(R.string.resum_title)
        canvas.drawText(periodTitle, margin, y, titlePaint)
        y += lineHeight

        val rangeLabel = if (startDate != null && endDate != null) {
            "${startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)} - ${endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
        } else {
            context.getString(R.string.filter_all)
        }
        canvas.drawText("Rang: $rangeLabel", margin, y, textPaint)
        y += lineHeight * 1.5f

        // Column widths for landscape
        val colDate = margin
        val colClient = margin + 70
        val colBolo = margin + 180
        val colRangs = margin + 350
        val colHours = margin + 500
        val colEarn = margin + 570
        val colDesp = margin + 650
        val colEstat = margin + 730

        // Headers
        canvas.drawText(context.getString(R.string.csv_header_date), colDate, y, headerPaint)
        canvas.drawText(context.getString(R.string.client_name), colClient, y, headerPaint)
        canvas.drawText(context.getString(R.string.csv_header_concept), colBolo, y, headerPaint)
        canvas.drawText("Rangs", colRangs, y, headerPaint)
        canvas.drawText("Hores", colHours, y, headerPaint)
        canvas.drawText("Ingressos", colEarn, y, headerPaint)
        canvas.drawText("Despeses", colDesp, y, headerPaint)
        canvas.drawText("Estat", colEstat, y, headerPaint)

        y += 5f
        canvas.drawLine(margin, y, pageWidth - margin, y, textPaint)
        y += 15f

        items.forEach { item ->
            if (y > pageHeight - 60) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
                // Redraw headers...
            }

            canvas.drawText(item.data, colDate, y, textPaint)

            val clientName = if (item.clientNom.length > 20) item.clientNom.substring(0, 18) + ".." else item.clientNom
            canvas.drawText(clientName, colClient, y, textPaint)

            val boloName = item.conceptesNoms.joinToString(" & ")
            val boloShort = if (boloName.length > 30) boloName.substring(0, 28) + ".." else boloName
            canvas.drawText(boloShort, colBolo, y, textPaint)

            val ranges = item.rangsHoraris.joinToString(", ") { "${it.horaInici}-${it.horaFi}" }
            val rangesShort = if (ranges.length > 25) ranges.substring(0, 23) + ".." else ranges
            canvas.drawText(rangesShort, colRangs, y, textPaint)

            canvas.drawText("%.1fh".format(item.hours), colHours, y, textPaint)
            canvas.drawText("%.2f€".format(item.earnings), colEarn, y, textPaint)
            canvas.drawText("%.2f€".format(item.despeses), colDesp, y, textPaint)
            canvas.drawText(item.estat, colEstat, y, textPaint)

            y += lineHeight
        }

        // Totals summary at the end
        val totalHours = items.sumOf { it.hours }
        val totalEarnings = items.sumOf { it.earnings }
        val totalExpenses = items.sumOf { it.despeses }

        y += 10f
        canvas.drawLine(margin, y, pageWidth - margin, y, textPaint)
        y += 20f
        canvas.drawText("Resum: %.1fh totals | Ingressos: %.2f€ | Despeses: %.2f€".format(totalHours, totalEarnings, totalExpenses), margin, y, footerPaint)

        pdfDocument.finishPage(page)
        pdfDocument.writeTo(file.outputStream())
        pdfDocument.close()

        return file
    }
}
