package com.example.domain.backup

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathEffect
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.R
import com.example.domain.model.Measurement
import com.example.domain.model.MeasurementStats
import com.example.domain.model.PressureClassification
import com.example.domain.model.UserProfile
import com.example.domain.rules.HealthCalculator
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ReportExporter {

    /**
     * Generates CSV report string with profile header, summary statistics, and table rows.
     */
    fun generateCsv(
        profile: UserProfile?,
        measurements: List<Measurement>,
        stats: MeasurementStats
    ): String {
        val sb = StringBuilder()
        sb.append("RELATÓRIO DE CONTROLE DE PRESSÃO ARTERIAL - PRESSÃO & RITMO\n")
        sb.append("Data de Emissão:;${HealthCalculator.formatEpochToDisplay(System.currentTimeMillis())}\n\n")

        sb.append("--- DADOS DO USUÁRIO ---\n")
        if (profile != null) {
            sb.append("Nome:;\"${profile.fullName}\"\n")
            sb.append("Sexo:;${profile.sex}\n")
            sb.append("Data de Nascimento:;${profile.birthDate ?: "Não informada"}\n")
            sb.append("Idade:;${profile.age?.let { "$it anos" } ?: "Não calculada"}\n")
            sb.append("Peso:;${profile.weight} kg\n")
            sb.append("Altura:;${profile.height} m\n")
            sb.append("IMC:;${profile.bmi ?: "-"} (${profile.bmiClassification?.title ?: "-"})\n\n")
        } else {
            sb.append("Perfil não cadastrado\n\n")
        }

        sb.append("--- ESTATÍSTICAS DO PERÍODO ---\n")
        sb.append("Total de Registros:;${stats.totalCount}\n")
        sb.append("Média Sistólica:;${stats.avgSystolic?.let { "$it mmHg" } ?: "-"}\n")
        sb.append("Média Diastólica:;${stats.avgDiastolic?.let { "$it mmHg" } ?: "-"}\n")
        sb.append("Média Frequência Cardíaca:;${stats.avgHeartRate?.let { "$it BPM" } ?: "-"}\n")
        sb.append("Menor Sistólica / Maior Sistólica:;${stats.minSystolic ?: "-"} / ${stats.maxSystolic ?: "-"} mmHg\n")
        sb.append("Menor Diastólica / Maior Diastólica:;${stats.minDiastolic ?: "-"} / ${stats.maxDiastolic ?: "-"} mmHg\n")
        sb.append("Menor FC / Maior FC:;${stats.minHeartRate ?: "-"} / ${stats.maxHeartRate ?: "-"} BPM\n\n")

        sb.append("--- REGISTROS DE AFERIÇÃO ---\n")
        sb.append("Data/Hora;Sistólica (mmHg);Diastólica (mmHg);Pulso (BPM);Classificação;Observações\n")

        for (m in measurements) {
            val obsEscaped = m.observation.replace("\"", "\"\"")
            sb.append("${m.formattedDate};${m.systolic};${m.diastolic};${m.heartRate};\"${m.classification.title}\";\"$obsEscaped\"\n")
        }

        sb.append("\nAVISO LEGAL: ${PressureClassification.GENERAL_DISCLAIMER}\n")
        return sb.toString()
    }

    /**
     * Writes a professional multi-page PDF medical report directly to an output stream.
     */
    fun writePdfReport(
        outputStream: OutputStream,
        profile: UserProfile?,
        measurements: List<Measurement>,
        stats: MeasurementStats,
        context: Context,
        startDate: Long? = null,
        endDate: Long? = null
    ) {
        val pdfDoc = PdfDocument()
        val pageWidth = 595 // A4 standard point width
        val pageHeight = 842 // A4 standard point height
        val margin = 36f

        val logoBitmap = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_fg_img)
        } catch (_: Exception) {
            null
        }

        val primaryColor = Color.rgb(103, 80, 164) // SleekPrimary

        val titlePaint = Paint().apply {
            color = primaryColor
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subTitlePaint = Paint().apply {
            color = Color.rgb(90, 90, 90)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val sectionPaint = Paint().apply {
            color = primaryColor
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val labelPaint = Paint().apply {
            color = Color.rgb(100, 100, 100)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val valuePaint = Paint().apply {
            color = Color.rgb(33, 33, 33)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val boldTextPaint = Paint().apply {
            color = Color.rgb(33, 33, 33)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.rgb(55, 55, 55)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(225, 225, 225)
            strokeWidth = 0.8f
        }

        val headerBgPaint = Paint().apply {
            color = Color.rgb(248, 248, 248)
            style = Paint.Style.FILL
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDoc.startPage(pageInfo)
        var canvas = page.canvas
        var yPos = margin

        fun drawHeader() {
            val logoSize = 42f
            if (logoBitmap != null) {
                val srcRect = Rect(0, 0, logoBitmap.width, logoBitmap.height)
                val destRect = Rect(margin.toInt(), yPos.toInt(), (margin + logoSize).toInt(), (yPos + logoSize).toInt())
                canvas.drawBitmap(logoBitmap, srcRect, destRect, Paint(Paint.FILTER_BITMAP_FLAG))
            }

            val textStartX = if (logoBitmap != null) margin + logoSize + 12f else margin
            
            canvas.drawText("Pressão & Ritmo", textStartX, yPos + 18f, titlePaint)
            canvas.drawText("Relatório de Acompanhamento de Pressão Arterial", textStartX, yPos + 32f, subTitlePaint)
            
            val emissao = "Emitido em: ${HealthCalculator.formatEpochToDisplay(System.currentTimeMillis())}"
            val emissaoWidth = subTitlePaint.measureText(emissao)
            canvas.drawText(emissao, pageWidth - margin - emissaoWidth, yPos + 18f, subTitlePaint)

            // Period Selection Info
            val startStr = startDate?.let { HealthCalculator.formatEpochToDateOnly(it) } ?: "Início"
            val endStr = endDate?.let { HealthCalculator.formatEpochToDateOnly(it) } ?: "Hoje"
            val periodoText = "Período: $startStr a $endStr"
            val periodoWidth = subTitlePaint.measureText(periodoText)
            canvas.drawText(periodoText, pageWidth - margin - periodoWidth, yPos + 32f, subTitlePaint)

            yPos += logoSize + 10f
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
            yPos += 20f
        }

        drawHeader()

        // Patient Profile Info - Modern Card Style
        canvas.drawText("DADOS DO PACIENTE", margin, yPos, sectionPaint)
        yPos += 14f

        if (profile != null) {
            val col1 = margin
            val col2 = margin + 200f
            val col3 = margin + 400f

            canvas.drawText("Nome:", col1, yPos, labelPaint)
            canvas.drawText(profile.fullName, col1, yPos + 12f, valuePaint)

            canvas.drawText("Sexo:", col2, yPos, labelPaint)
            canvas.drawText(profile.sex, col2, yPos + 12f, valuePaint)

            canvas.drawText("Idade:", col3, yPos, labelPaint)
            canvas.drawText(profile.age?.let { "$it anos" } ?: "N/I", col3, yPos + 12f, valuePaint)

            yPos += 30f

            canvas.drawText("Peso:", col1, yPos, labelPaint)
            canvas.drawText("${profile.weight} kg", col1, yPos + 12f, valuePaint)

            canvas.drawText("Altura:", col2, yPos, labelPaint)
            canvas.drawText("${profile.height} m", col2, yPos + 12f, valuePaint)

            canvas.drawText("IMC:", col3, yPos, labelPaint)
            canvas.drawText("${profile.bmi ?: "-"} (${profile.bmiClassification?.title ?: "-"})", col3, yPos + 12f, valuePaint)

            yPos += 24f
        } else {
            canvas.drawText("Perfil não cadastrado", margin, yPos + 12f, textPaint)
            yPos += 24f
        }

        yPos += 10f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
        yPos += 20f

        // Summary Stats Box
        canvas.drawText("RESUMO ESTATÍSTICO NO PERÍODO", margin, yPos, sectionPaint)
        yPos += 16f

        val boxMargin = 6f
        val boxWidth = (pageWidth - 2 * margin - 12f) / 3f
        
        fun drawStatBox(title: String, value: String, x: Float, y: Float) {
            canvas.drawRect(x, y, x + boxWidth, y + 35f, headerBgPaint)
            canvas.drawText(title, x + boxMargin, y + 14f, labelPaint)
            canvas.drawText(value, x + boxMargin, y + 28f, valuePaint)
        }

        drawStatBox("Média Sistólica", stats.avgSystolic?.let { "$it mmHg" } ?: "-", margin, yPos)
        drawStatBox("Média Diastólica", stats.avgDiastolic?.let { "$it mmHg" } ?: "-", margin + boxWidth + 6f, yPos)
        drawStatBox("Média FC", stats.avgHeartRate?.let { "$it BPM" } ?: "-", margin + 2 * (boxWidth + 6f), yPos)

        yPos += 45f
        
        drawStatBox("Sistólica Mín/Máx", "${stats.minSystolic ?: "-"}/${stats.maxSystolic ?: "-"} mmHg", margin, yPos)
        drawStatBox("Diastólica Mín/Máx", "${stats.minDiastolic ?: "-"}/${stats.maxDiastolic ?: "-"} mmHg", margin + boxWidth + 6f, yPos)
        drawStatBox("FC Mín/Máx", "${stats.minHeartRate ?: "-"}/${stats.maxHeartRate ?: "-"} BPM", margin + 2 * (boxWidth + 6f), yPos)

        yPos += 60f

        // Table Header
        fun drawTableHeader() {
            canvas.drawRect(margin, yPos - 15f, pageWidth - margin, yPos + 7f, headerBgPaint)
            canvas.drawText("DATA / HORA", margin + 6f, yPos, sectionPaint.apply { textSize = 8.5f })
            canvas.drawText("PRESSÃO (mmHg)", margin + 110f, yPos, sectionPaint)
            canvas.drawText("FC (BPM)", margin + 210f, yPos, sectionPaint)
            canvas.drawText("CLASSIFICAÇÃO", margin + 280f, yPos, sectionPaint)
            canvas.drawText("OBSERVAÇÕES", margin + 410f, yPos, sectionPaint)
            yPos += 12f
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
            yPos += 16f
        }

        canvas.drawText("HISTÓRICO DE AFERIÇÕES", margin, yPos, sectionPaint.apply { textSize = 11f })
        yPos += 20f
        drawTableHeader()

        for (m in measurements) {
            if (yPos > pageHeight - margin - 45f) {
                pdfDoc.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDoc.startPage(pageInfo)
                canvas = page.canvas
                yPos = margin
                drawHeader()
                yPos += 10f
                drawTableHeader()
            }

            canvas.drawText(m.formattedDate, margin + 6f, yPos, textPaint)
            canvas.drawText("${m.systolic} / ${m.diastolic}", margin + 110f, yPos, boldTextPaint)
            canvas.drawText("${m.heartRate}", margin + 210f, yPos, textPaint)
            
            // Classification text with color indicator if possible (manual RGB logic)
            val classPaint = Paint(textPaint).apply {
                color = when (m.classification) {
                    PressureClassification.NORMAL -> Color.rgb(46, 125, 50)
                    PressureClassification.HIPERTENSAO_ESTAGIO_3 -> Color.rgb(136, 14, 79)
                    else -> Color.rgb(230, 81, 0)
                }
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 8.5f
            }
            canvas.drawText(m.classification.title, margin + 280f, yPos, classPaint)

            val obsText = if (m.observation.length > 35) m.observation.take(32) + "..." else m.observation
            canvas.drawText(obsText, margin + 410f, yPos, textPaint.apply { textSize = 8f })

            yPos += 12f
            // Thin separator line
            canvas.drawLine(margin + 4f, yPos, pageWidth - margin - 4f, yPos, linePaint.apply { alpha = 100 })
            yPos += 14f
        }

        // New Page for Charts
        pdfDoc.finishPage(page)
        pageNumber++
        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        page = pdfDoc.startPage(pageInfo)
        canvas = page.canvas
        yPos = margin
        drawHeader()

        canvas.drawText("GRÁFICOS DE EVOLUÇÃO NO PERÍODO", margin, yPos, sectionPaint.apply { textSize = 11f })
        yPos += 30f

        // 1. Blood Pressure Chart
        drawBloodPressureChart(canvas, measurements, margin, yPos, pageWidth - 2 * margin, 200f, primaryColor)
        yPos += 230f

        // 2. Heart Rate Chart
        drawHeartRateChart(canvas, measurements, margin, yPos, pageWidth - 2 * margin, 180f)
        yPos += 210f

        // Footer disclaimer
        val disclaimerPaint = Paint().apply {
            color = Color.rgb(120, 120, 120)
            textSize = 7.5f
            isAntiAlias = true
        }
        
        if (yPos > pageHeight - margin - 30f) {
            pdfDoc.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = pdfDoc.startPage(pageInfo)
            canvas = page.canvas
            yPos = margin + 20f
        } else {
            yPos += 20f
        }

        canvas.drawText("Aviso: ${PressureClassification.GENERAL_DISCLAIMER}", margin, yPos, disclaimerPaint)
        yPos += 9f
        canvas.drawText("Referência: Diretriz Brasileira de Hipertensão Arterial - Sociedade Brasileira de Cardiologia (SBC)", margin, yPos, disclaimerPaint)

        pdfDoc.finishPage(page)
        pdfDoc.writeTo(outputStream)
        pdfDoc.close()
    }

    private fun drawBloodPressureChart(
        canvas: Canvas,
        measurements: List<Measurement>,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        primaryColorInt: Int
    ) {
        val chronological = measurements.sortedBy { it.measuredAtEpoch }
        if (chronological.isEmpty()) return

        val chartMarginLeft = 30f
        val chartMarginBottom = 20f
        val plotWidth = width - chartMarginLeft
        val plotHeight = height - chartMarginBottom

        val paint = Paint().apply { isAntiAlias = true }
        val axisPaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }
        val labelPaint = Paint().apply {
            color = Color.rgb(100, 100, 100)
            textSize = 7f
        }

        // Title
        paint.color = primaryColorInt
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9f
        canvas.drawText("PRESSÃO ARTERIAL (mmHg)", x, y - 10f, paint)

        // Draw Axes
        canvas.drawLine(x + chartMarginLeft, y, x + chartMarginLeft, y + plotHeight, axisPaint)
        canvas.drawLine(x + chartMarginLeft, y + plotHeight, x + width, y + plotHeight, axisPaint)

        val minY = 40f
        val maxY = 220f
        val rangeY = maxY - minY

        fun getYPos(value: Float) = y + plotHeight - ((value - minY) / rangeY * plotHeight)
        fun getXPos(index: Int) = if (chronological.size > 1) {
            x + chartMarginLeft + (index.toFloat() / (chronological.size - 1) * plotWidth)
        } else x + chartMarginLeft + plotWidth / 2

        // Grid lines & labels
        val gridLevels = listOf(80f, 120f, 140f, 180f)
        val realDashPaint = Paint(axisPaint).apply {
            pathEffect = android.graphics.DashPathEffect(floatArrayOf(5f, 5f), 0f)
            alpha = 100
        }

        gridLevels.forEach { level ->
            val py = getYPos(level)
            canvas.drawLine(x + chartMarginLeft, py, x + width, py, realDashPaint)
            canvas.drawText(level.toInt().toString(), x, py + 3f, labelPaint)
        }

        val dateLabelPaint = Paint(labelPaint).apply { 
            textSize = 6f
            textAlign = Paint.Align.CENTER
        }
        val valueLabelPaint = Paint(labelPaint).apply {
            textSize = 6.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Draw Paths
        val sysPath = Path()
        val diaPath = Path()
        val sysPaint = Paint().apply {
            color = primaryColorInt
            style = Paint.Style.STROKE
            strokeWidth = 2f
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }
        val diaPaint = Paint(sysPaint).apply { color = Color.rgb(98, 91, 113) } // SleekSecondary

        chronological.forEachIndexed { i, m ->
            val px = getXPos(i)
            val pySys = getYPos(m.systolic.toFloat())
            val pyDia = getYPos(m.diastolic.toFloat())

            if (i == 0) {
                sysPath.moveTo(px, pySys)
                diaPath.moveTo(px, pyDia)
            } else {
                sysPath.lineTo(px, pySys)
                diaPath.lineTo(px, pyDia)
            }
            
            // Points
            canvas.drawCircle(px, pySys, 2.5f, Paint().apply { color = sysPaint.color; isAntiAlias = true })
            canvas.drawCircle(px, pyDia, 2.5f, Paint().apply { color = diaPaint.color; isAntiAlias = true })

            // Data Labels (Values)
            canvas.drawText(m.systolic.toString(), px - 6f, pySys - 6f, valueLabelPaint.apply { color = sysPaint.color })
            canvas.drawText(m.diastolic.toString(), px - 6f, pyDia + 10f, valueLabelPaint.apply { color = diaPaint.color })

            // X-Axis Date Labels (skip some if too many to avoid overlap)
            val skip = when {
                chronological.size > 15 -> chronological.size / 5
                chronological.size > 8 -> 2
                else -> 1
            }
            if (i % skip == 0 || i == chronological.size - 1) {
                val dateStr = m.formattedDate.split(" ").firstOrNull() ?: ""
                canvas.drawText(dateStr, px, y + plotHeight + 12f, dateLabelPaint)
            }
        }
        canvas.drawPath(sysPath, sysPaint)
        canvas.drawPath(diaPath, diaPaint)
    }

    private fun drawHeartRateChart(
        canvas: Canvas,
        measurements: List<Measurement>,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {
        val chronological = measurements.sortedBy { it.measuredAtEpoch }
        if (chronological.isEmpty()) return

        val chartMarginLeft = 30f
        val chartMarginBottom = 20f
        val plotWidth = width - chartMarginLeft
        val plotHeight = height - chartMarginBottom

        val paint = Paint().apply { isAntiAlias = true }
        val labelPaint = Paint().apply {
            color = Color.rgb(100, 100, 100)
            textSize = 7f
        }

        paint.color = Color.rgb(125, 82, 96) // SleekTertiary
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9f
        canvas.drawText("FREQUÊNCIA CARDÍACA (BPM)", x, y - 10f, paint)

        val minY = 40f
        val maxY = 160f
        val rangeY = maxY - minY

        fun getYPos(value: Float) = y + plotHeight - ((value - minY) / rangeY * plotHeight)
        fun getXPos(index: Int) = if (chronological.size > 1) {
            x + chartMarginLeft + (index.toFloat() / (chronological.size - 1) * plotWidth)
        } else x + chartMarginLeft + plotWidth / 2

        // Background normal range (60-100)
        val bgPaint = Paint().apply { color = Color.rgb(232, 245, 233); style = Paint.Style.FILL }
        canvas.drawRect(x + chartMarginLeft, getYPos(100f), x + width, getYPos(60f), bgPaint)

        // Grid
        val realDashPaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
            pathEffect = android.graphics.DashPathEffect(floatArrayOf(5f, 5f), 0f)
            alpha = 100
        }
        listOf(60f, 100f, 140f).forEach { level ->
            val py = getYPos(level)
            canvas.drawLine(x + chartMarginLeft, py, x + width, py, realDashPaint)
            canvas.drawText(level.toInt().toString(), x, py + 3f, labelPaint)
        }

        val dateLabelPaint = Paint(labelPaint).apply { 
            textSize = 6f
            textAlign = Paint.Align.CENTER
        }
        val valueLabelPaint = Paint(labelPaint).apply {
            textSize = 6.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.rgb(125, 82, 96)
        }

        val hrPath = Path()
        val hrPaint = Paint().apply {
            color = Color.rgb(125, 82, 96)
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        chronological.forEachIndexed { i, m ->
            val px = getXPos(i)
            val py = getYPos(m.heartRate.toFloat())
            if (i == 0) hrPath.moveTo(px, py) else hrPath.lineTo(px, py)
            canvas.drawCircle(px, py, 2.5f, Paint().apply { color = hrPaint.color; isAntiAlias = true })
            
            // Data Label
            canvas.drawText(m.heartRate.toString(), px - 5f, py - 6f, valueLabelPaint)

            // X-Axis Date Labels
            val skip = if (chronological.size > 10) chronological.size / 5 else 1
            if (i % skip == 0 || i == chronological.size - 1) {
                val dateStr = m.formattedDate.split(" ").firstOrNull() ?: ""
                canvas.drawText(dateStr, px, y + plotHeight + 12f, dateLabelPaint)
            }
        }
        canvas.drawPath(hrPath, hrPaint)
    }

    /**
     * Creates a temporary file in exports directory and returns Share Intent with content URI.
     */
    fun createShareIntent(
        context: Context,
        fileName: String,
        mimeType: String,
        fileWriter: (FileOutputStream) -> Unit
    ): Intent {
        val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportsDir, fileName)
        FileOutputStream(file).use { fos ->
            fileWriter(fos)
        }

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Relatório Pressão & Ritmo")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
