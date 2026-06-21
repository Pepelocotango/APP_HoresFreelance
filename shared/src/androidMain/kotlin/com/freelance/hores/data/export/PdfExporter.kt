package com.freelance.hores.data.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import com.freelance.hores.domain.model.Dia
import java.io.File
import kotlinx.datetime.LocalDate

class PdfExporter(private val context: Context) {
    fun exportToPdf(
        dias: List<Dia>,
        filename: String = "hores_freelance.pdf",
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): File {
        val file = File(context.cacheDir, filename)

        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        
        val titlePaint = Paint().apply {
            textSize = 20f
            isFakeBoldText = true
        }

        val headerPaint = Paint().apply {
            textSize = 10f // Reduced
            isFakeBoldText = true
        }

        val textPaint = Paint().apply {
            textSize = 9f // Reduced
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
        val margin = 40f
        val lineHeight = 18f // Slightly tighter

        // Title
        val periodTitle = if (startDate != null && endDate != null) {
            "Resum d'hores — $startDate a $endDate"
        } else {
            "Resum d'hores"
        }
        canvas.drawText(periodTitle, margin, y, titlePaint)
        y += lineHeight * 2

        canvas.drawText("Data", margin, y, headerPaint)
        canvas.drawText("Concepte", margin + 80, y, headerPaint)
        canvas.drawText("Preu/h", margin + 220, y, headerPaint)
        canvas.drawText("Inici", margin + 290, y, headerPaint)
        canvas.drawText("Fi", margin + 350, y, headerPaint)
        canvas.drawText("Durada", margin + 410, y, headerPaint)
        canvas.drawText("Total €", margin + 480, y, headerPaint)

        y += lineHeight
        canvas.drawLine(margin, y - 5, pageWidth - margin.toFloat(), y - 5, textPaint)
        y += 10f

        var totalHoras = 0.0
        var totalDiners = 0.0
        for (dia in dias) {
            for (concepte in dia.conceptes) {
                totalDiners += concepte.getTotalDiners()
                for (rang in concepte.rangsHoraris) {
                    if (y > pageHeight - 100) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        pageInfo = PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        y = 50f
                        
                        // Redraw headers on new page
                        canvas.drawText("Data", margin, y, headerPaint)
                        canvas.drawText("Concepte", margin + 80, y, headerPaint)
                        canvas.drawText("Preu/h", margin + 220, y, headerPaint)
                        canvas.drawText("Inici", margin + 290, y, headerPaint)
                        canvas.drawText("Fi", margin + 350, y, headerPaint)
                        canvas.drawText("Durada", margin + 410, y, headerPaint)
                        canvas.drawText("Total €", margin + 480, y, headerPaint)
                        y += lineHeight
                        canvas.drawLine(margin, y - 5, pageWidth - margin.toFloat(), y - 5, textPaint)
                        y += 10f
                    }

                    val duracion = rang.getDuracionaEnHoras()
                    totalHoras += duracion

                    canvas.drawText(dia.data.toString(), margin, y, textPaint)
                    
                    val conceptName = if (concepte.nom.length > 20) concepte.nom.substring(0, 17) + "..." else concepte.nom
                    canvas.drawText(conceptName, margin + 80, y, textPaint)

                    val preuText = if (concepte.esPreuFix) "FIX: %.2f".format(concepte.importPreuFix) else "%.2f".format(concepte.preuHora)
                    canvas.drawText(preuText, margin + 220, y, textPaint)

                    canvas.drawText("${rang.horaInici.hour}:${"%02d".format(rang.horaInici.minute)}", margin + 290, y, textPaint)
                    canvas.drawText("${rang.horaFi.hour}:${"%02d".format(rang.horaFi.minute)}", margin + 350, y, textPaint)
                    canvas.drawText("%.2f".format(duracion), margin + 410, y, textPaint)

                    // Mostrem l'import total del bolo (hores/fix + despeses) només a la primera fila del bolo per claredat
                    if (rang == concepte.rangsHoraris.firstOrNull()) {
                        canvas.drawText("%.2f".format(concepte.getTotalDiners()), margin + 480, y, textPaint)
                    }

                    y += lineHeight
                }
            }
        }

        // Total
        y += lineHeight
        canvas.drawLine(margin, y - 15, pageWidth - margin.toFloat(), y - 15, textPaint)
        canvas.drawText("Total Hores: %.2f | Total Factura: %.2f €".format(totalHoras, totalDiners), margin, y, footerPaint)

        pdfDocument.finishPage(page)
        pdfDocument.writeTo(file.outputStream())
        pdfDocument.close()

        return file
    }
}
