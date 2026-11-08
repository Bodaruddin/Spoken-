package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.ExamSubmission
import java.io.File
import java.io.FileOutputStream

object CertificatePdfGenerator {

    /**
     * Generates a high-resolution, premium landscape A4 PDF Certificate
     * for a student's completed exam and saves it to the device.
     */
    fun generateCertificatePdf(
        context: Context,
        submission: ExamSubmission,
        onSuccess: (File) -> Unit = {},
        onError: (String) -> Unit = {}
    ): File? {
        val document = PdfDocument()

        // High-res standard A4 Landscape: 1122 width x 792 height (approx 72-150 dpi scale)
        val pageWidth = 1122
        val pageHeight = 792
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        try {
            renderCertificateCanvas(canvas, submission, pageWidth, pageHeight)
            document.finishPage(page)

            // Save file in external files / downloads
            val safeFileName = "Certificate_${submission.studentName.replace(" ", "_")}_${submission.certificateId}.pdf"
            val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: File(context.filesDir, "certificates")
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val file = File(downloadsDir, safeFileName)
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            document.close()

            onSuccess(file)
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            try { document.close() } catch (_: Exception) {}
            onError(e.localizedMessage ?: "Failed to generate certificate PDF")
            return null
        }
    }

    private fun renderCertificateCanvas(
        canvas: Canvas,
        submission: ExamSubmission,
        width: Int,
        height: Int
    ) {
        // 1. Background Cream / Parchment Ivory
        val bgPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#FCFBF7")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Soft subtle textured inner glow
        val innerGlowPaint = Paint().apply {
            shader = RadialGradient(
                width / 2f, height / 2f, (width / 1.5f),
                intArrayOf(
                    android.graphics.Color.parseColor("#FFFFFF"),
                    android.graphics.Color.parseColor("#F7F4EA"),
                    android.graphics.Color.parseColor("#EFE9D8")
                ),
                floatArrayOf(0f, 0.7f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(20f, 20f, (width - 20).toFloat(), (height - 20).toFloat(), innerGlowPaint)

        // 2. Ornate Royal Double Borders (Deep Navy & Burnished Gold)
        val goldOuterPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#B8860B") // Dark Goldenrod
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }
        canvas.drawRect(28f, 28f, (width - 28).toFloat(), (height - 28).toFloat(), goldOuterPaint)

        val goldThinPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#DAA520")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        canvas.drawRect(36f, 36f, (width - 36).toFloat(), (height - 36).toFloat(), goldThinPaint)

        val navyMainBorder = Paint().apply {
            color = android.graphics.Color.parseColor("#090D26") // Deep Royal Navy
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawRect(44f, 44f, (width - 44).toFloat(), (height - 44).toFloat(), navyMainBorder)

        // Draw Ornate Corner Brackets / Filigree accents
        drawCornerAccents(canvas, 44f, 44f, (width - 44).toFloat(), (height - 44).toFloat())

        // 3. Institution Branding & Emblem
        val emblemBgPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0F172A")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(width / 2f, 105f, 32f, emblemBgPaint)

        val emblemBorderPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawCircle(width / 2f, 105f, 32f, emblemBorderPaint)

        // Star / Owl in center emblem
        val emblemTextPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#FFD700")
            textSize = 28f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("★", width / 2f, 115f, emblemTextPaint)

        // Academy Name
        val academyTitlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#090D26")
            textSize = 17f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = 0.22f
        }
        canvas.drawText("SPEAKLY ACADEMY OF ENGLISH & LINGUISTICS", width / 2f, 160f, academyTitlePaint)

        val academySubPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#64748B")
            textSize = 10f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
            letterSpacing = 0.15f
        }
        canvas.drawText("DEPARTMENT OF ACADEMIC EVALUATION & CERTIFICATION", width / 2f, 178f, academySubPaint)

        // Decorative Gold Divider
        drawGoldDivider(canvas, width / 2f, 195f, 260f)

        // 4. Certificate Main Title
        val certTitlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0B132B")
            textSize = 34f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = 0.08f
        }
        canvas.drawText("CERTIFICATE OF EXCELLENCE", width / 2f, 240f, certTitlePaint)

        val presentationPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#475569")
            textSize = 13.5f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            isAntiAlias = true
        }
        canvas.drawText("This official credential is proudly conferred upon", width / 2f, 275f, presentationPaint)

        // 5. Student Name (Hero Display)
        val namePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#091030")
            textSize = 38f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(submission.studentName, width / 2f, 328f, namePaint)

        // Underline below student name
        val nameUnderline = Paint().apply {
            color = android.graphics.Color.parseColor("#B8860B")
            strokeWidth = 2f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        canvas.drawLine((width / 2f) - 180f, 342f, (width / 2f) + 180f, 342f, nameUnderline)

        // 6. Evaluation Statement & Exam Title
        val classInfoPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#334155")
            textSize = 14f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText("Student of ${submission.studentClass} • Speakly Digital Campus", width / 2f, 372f, classInfoPaint)

        val achievementDescPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#475569")
            textSize = 13.5f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText(
            "for successfully appearing and passing the formal MCQ Examination in",
            width / 2f,
            406f,
            achievementDescPaint
        )

        val examTitlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1E3A8A") // Deep Blue
            textSize = 21f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("\"${submission.examTitle}\"", width / 2f, 442f, examTitlePaint)

        // 7. Results Score Badge Box
        val boxWidth = 540f
        val boxHeight = 84f
        val boxLeft = (width - boxWidth) / 2f
        val boxTop = 472f

        val boxBgPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(boxLeft, boxTop, boxLeft + boxWidth, boxTop + boxHeight, 14f, 14f, boxBgPaint)

        val boxBorderPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        canvas.drawRoundRect(boxLeft, boxTop, boxLeft + boxWidth, boxTop + boxHeight, 14f, 14f, boxBorderPaint)

        // 3 Column Metrics inside the Score Box: Marks | Percentage | Grade
        val colWidth = boxWidth / 3f

        // Metric 1: Marks Scored
        drawMetricColumn(
            canvas = canvas,
            centerX = boxLeft + colWidth * 0.5f,
            centerY = boxTop + 42f,
            label = "MARKS SCORED",
            value = "${submission.scoreObtained} / ${submission.totalMarks}"
        )

        // Divider 1
        canvas.drawLine(boxLeft + colWidth, boxTop + 14f, boxLeft + colWidth, boxTop + boxHeight - 14f, boxBorderPaint)

        // Metric 2: Percentage
        drawMetricColumn(
            canvas = canvas,
            centerX = boxLeft + colWidth * 1.5f,
            centerY = boxTop + 42f,
            label = "PERCENTAGE",
            value = "${submission.percentage}%"
        )

        // Divider 2
        canvas.drawLine(boxLeft + colWidth * 2, boxTop + 14f, boxLeft + colWidth * 2, boxTop + boxHeight - 14f, boxBorderPaint)

        // Metric 3: Grade
        drawMetricColumn(
            canvas = canvas,
            centerX = boxLeft + colWidth * 2.5f,
            centerY = boxTop + 42f,
            label = "AWARD GRADE",
            value = "Grade ${submission.grade}"
        )

        // 8. Bottom Seal & Signatures
        // Center: Gold Ribbon Seal
        drawOfficialGoldSeal(canvas, width / 2f, 620f)

        // Left Signature: Teacher
        val sigXLeft = 200f
        val sigY = 635f

        val sigLinePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#94A3B8")
            strokeWidth = 1.2f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(sigXLeft - 90f, sigY, sigXLeft + 90f, sigY, sigLinePaint)

        val cursiveSigPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1E293B")
            textSize = 19f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            isAntiAlias = true
        }
        canvas.drawText("Eleanor Vance", sigXLeft, sigY - 10f, cursiveSigPaint)

        val sigTitlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#334155")
            textSize = 11.5f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("Prof. Eleanor Vance", sigXLeft, sigY + 18f, sigTitlePaint)

        val sigSubPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#64748B")
            textSize = 9.5f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText("Head of English Faculty", sigXLeft, sigY + 32f, sigSubPaint)

        // Right Signature: Dean
        val sigXRight = width - 200f
        canvas.drawLine(sigXRight - 90f, sigY, sigXRight + 90f, sigY, sigLinePaint)
        canvas.drawText("Marcus Reed, Ph.D.", sigXRight, sigY - 10f, cursiveSigPaint)
        canvas.drawText("Dr. Marcus Reed", sigXRight, sigY + 18f, sigTitlePaint)
        canvas.drawText("Academic Dean & Director", sigXRight, sigY + 32f, sigSubPaint)

        // 9. Footer: Certificate ID & Issue Date
        val footerPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#64748B")
            textSize = 9.5f
            isAntiAlias = true
        }
        canvas.drawText("Certificate ID: ${submission.certificateId}", 60f, (height - 52).toFloat(), footerPaint)
        canvas.drawText("Completed: ${submission.completedAt}", 60f, (height - 38).toFloat(), footerPaint)

        val verifyPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#15803D") // Emerald
            textSize = 9.5f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("✓ SECURE VERIFIED RECORD • SPEAKLY EDUCATION", (width - 60).toFloat(), (height - 45).toFloat(), verifyPaint)
    }

    private fun drawMetricColumn(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        label: String,
        value: String
    ) {
        val labelPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#64748B")
            textSize = 9f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = 0.12f
            isAntiAlias = true
        }
        canvas.drawText(label, centerX, centerY - 6f, labelPaint)

        val valPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#090D26")
            textSize = 17f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(value, centerX, centerY + 16f, valPaint)
    }

    private fun drawOfficialGoldSeal(canvas: Canvas, cx: Float, cy: Float) {
        // Ribbon Tails
        val ribbonPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#991B1B") // Crimson Red Ribbon
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val path1 = Path().apply {
            moveTo(cx - 16f, cy + 20f)
            lineTo(cx - 28f, cy + 68f)
            lineTo(cx - 14f, cy + 60f)
            lineTo(cx - 4f, cy + 68f)
            lineTo(cx - 4f, cy + 20f)
            close()
        }
        canvas.drawPath(path1, ribbonPaint)

        val path2 = Path().apply {
            moveTo(cx + 4f, cy + 20f)
            lineTo(cx + 4f, cy + 68f)
            lineTo(cx + 14f, cy + 60f)
            lineTo(cx + 28f, cy + 68f)
            lineTo(cx + 16f, cy + 20f)
            close()
        }
        canvas.drawPath(path2, ribbonPaint)

        // Gold Medallion Outer
        val goldSealOuter = Paint().apply {
            color = android.graphics.Color.parseColor("#DAA520")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(cx, cy, 38f, goldSealOuter)

        // Gold Ring Inner
        val goldInner = Paint().apply {
            color = android.graphics.Color.parseColor("#B8860B")
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            isAntiAlias = true
        }
        canvas.drawCircle(cx, cy, 32f, goldInner)

        val goldCenter = Paint().apply {
            color = android.graphics.Color.parseColor("#FFDF00")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(cx, cy, 26f, goldCenter)

        val sealTextPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#78350F")
            textSize = 7f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("OFFICIAL", cx, cy - 8f, sealTextPaint)

        val starPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#78350F")
            textSize = 14f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("★", cx, cy + 5f, starPaint)
        canvas.drawText("SEAL", cx, cy + 16f, sealTextPaint)
    }

    private fun drawCornerAccents(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        val cornerPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            isAntiAlias = true
        }
        val size = 26f

        // Top-Left
        canvas.drawLine(left + 8f, top + 8f, left + 8f + size, top + 8f, cornerPaint)
        canvas.drawLine(left + 8f, top + 8f, left + 8f, top + 8f + size, cornerPaint)
        canvas.drawCircle(left + 8f, top + 8f, 3f, cornerPaint)

        // Top-Right
        canvas.drawLine(right - 8f, top + 8f, right - 8f - size, top + 8f, cornerPaint)
        canvas.drawLine(right - 8f, top + 8f, right - 8f, top + 8f + size, cornerPaint)
        canvas.drawCircle(right - 8f, top + 8f, 3f, cornerPaint)

        // Bottom-Left
        canvas.drawLine(left + 8f, bottom - 8f, left + 8f + size, bottom - 8f, cornerPaint)
        canvas.drawLine(left + 8f, bottom - 8f, left + 8f, bottom - 8f - size, cornerPaint)
        canvas.drawCircle(left + 8f, bottom - 8f, 3f, cornerPaint)

        // Bottom-Right
        canvas.drawLine(right - 8f, bottom - 8f, right - 8f - size, bottom - 8f, cornerPaint)
        canvas.drawLine(right - 8f, bottom - 8f, right - 8f, bottom - 8f - size, cornerPaint)
        canvas.drawCircle(right - 8f, bottom - 8f, 3f, cornerPaint)
    }

    private fun drawGoldDivider(canvas: Canvas, cx: Float, y: Float, halfWidth: Float) {
        val linePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#D4AF37")
            strokeWidth = 1.2f
            isAntiAlias = true
        }
        canvas.drawLine(cx - halfWidth, y, cx - 14f, y, linePaint)
        canvas.drawLine(cx + 14f, y, cx + halfWidth, y, linePaint)

        val diamondPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#B8860B")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val path = Path().apply {
            moveTo(cx, y - 5f)
            lineTo(cx + 6f, y)
            lineTo(cx, y + 5f)
            lineTo(cx - 6f, y)
            close()
        }
        canvas.drawPath(path, diamondPaint)
    }

    /**
     * Opens or shares the generated PDF certificate using standard Android Intent
     */
    fun openOrSharePdf(context: Context, file: File) {
        try {
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Speakly Exam Certificate: ${file.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share or Save Certificate PDF"))
        } catch (e: Exception) {
            // Fallback: Show toast
            Toast.makeText(context, "Certificate saved to: ${file.name}", Toast.LENGTH_LONG).show()
        }
    }
}
