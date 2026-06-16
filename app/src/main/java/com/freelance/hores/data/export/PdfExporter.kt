package com.freelance.hores.data.export

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument.PageInfo
import com.freelance.hores.domain.model.Dia
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PdfExporter(private val context: Context) {
    fun exportToPdf(
        dias: List<Dia>,
        filename: String = "hores_freelance.pdf",
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): File {
        val file = File(context.cacheDir, filename)

        val pdfDocument = PdfDocument()
        val pageInfo = PageInfo.Builder(595, 842, 1).create() // A4 size

        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        drawPdf(canvas, dias, startDate, endDate)

        pdfDocument.finishPage(page)
        pdfDocument.writeTo(file.outputStream())
        pdfDocument.close()

        return file
    }

    private fun drawPdf(
        canvas: Canvas,
        dias: List<Dia>,
        startDate: LocalDate?,
        endDate: LocalDate?
    ) {
        val titlePaint = Paint().apply {
            textSize = 24f
            isFakeBoldText = true
        }

        val headerPaint = Paint().apply {
            textSize = 12f
            isFakeBoldText = true
        }

        val textPaint = Paint().apply {
            textSize = 10f
        }

        var y = 40f
        val margin = 20f
        val lineHeight = 15f

        // Title
        val periodTitle = if (startDate != null && endDate != null) {
            "Resum d'hores — ${startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)} a ${endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
        } else {
            "Hours Summary"
        }
        canvas.drawText(periodTitle, margin, y, titlePaint)
        y += lineHeight * 2

        // Headers
        canvas.drawText("Date", margin, y, headerPaint)
        canvas.drawText("Concept", margin + 100, y, headerPaint)
        canvas.drawText("Start", margin + 250, y, headerPaint)
        canvas.drawText("End", margin + 350, y, headerPaint)
        canvas.drawText("Hours", margin + 450, y, headerPaint)

        y += lineHeight

        // Draw lines
        var totalHoras = 0.0
        for (dia in dias) {
            for (concepte in dia.conceptes) {
                for (rang in concepte.rangsHoraris) {
                    val duracion = rang.getDuracionaEnHoras()
                    totalHoras += duracion

                    if (y > 800) {
                        // New page needed (simplified - just skip)
                        break
                    }

                    canvas.drawText(dia.data.toString(), margin, y, textPaint)
                    canvas.drawText(concepte.nom, margin + 100, y, textPaint)
                    canvas.drawText(rang.horaInici.toString(), margin + 250, y, textPaint)
                    canvas.drawText(rang.horaFi.toString(), margin + 350, y, textPaint)
                    canvas.drawText("%.2f".format(duracion), margin + 450, y, textPaint)

                    y += lineHeight
                }
            }
        }

        // Total
        y += lineHeight
        canvas.drawText("Total: %.2f hours".format(totalHoras), margin, y, headerPaint)
    }
}
