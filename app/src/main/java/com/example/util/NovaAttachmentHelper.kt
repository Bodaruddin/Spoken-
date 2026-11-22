package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import java.util.zip.ZipInputStream
import kotlin.math.max
import kotlin.math.roundToInt

data class NovaAttachment(
    val id: String = UUID.randomUUID().toString(),
    val displayName: String,
    val mimeType: String,
    val isImage: Boolean,
    val base64Data: String? = null,
    val extractedText: String? = null,
    val previewBitmap: Bitmap? = null,
    val sizeLabel: String = ""
)

object NovaAttachmentHelper {

    private const val MAX_IMAGE_DIMENSION = 1800
    private const val MAX_THUMBNAIL_DIMENSION = 360
    private const val MAX_PDF_BYTES = 6 * 1024 * 1024
    private const val MAX_TEXT_CHARS = 24000

    fun createCameraImageUri(context: Context): Uri {
        val dir = File(context.cacheDir, "nova_camera").apply { mkdirs() }
        val file = File(dir, "nova_cam_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    suspend fun loadAttachmentFromUri(
        context: Context,
        uri: Uri,
        forceImage: Boolean = false,
        overrideName: String? = null
    ): Result<NovaAttachment> = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val detectedMime = resolver.getType(uri)?.lowercase() ?: ""
            val resolvedName = overrideName ?: queryDisplayName(context, uri) ?: if (forceImage) {
                "Camera_Photo_${System.currentTimeMillis()}.jpg"
            } else {
                "Attachment_${System.currentTimeMillis()}"
            }

            val lowerName = resolvedName.lowercase()
            val isImage = forceImage ||
                detectedMime.startsWith("image/") ||
                lowerName.endsWith(".jpg") ||
                lowerName.endsWith(".jpeg") ||
                lowerName.endsWith(".png") ||
                lowerName.endsWith(".webp")

            if (isImage) {
                val rawBytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@withContext Result.failure(IllegalStateException("Could not read selected image."))
                val rawDecoded = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)
                    ?: return@withContext Result.failure(IllegalStateException("Unsupported image format."))
                val decoded = applyExifOrientation(rawDecoded, rawBytes)
                if (decoded !== rawDecoded) {
                    rawDecoded.recycle()
                }

                val scaled = scaleBitmapIfNeeded(decoded, MAX_IMAGE_DIMENSION)
                val out = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, 92, out)
                val jpegBytes = out.toByteArray()
                val base64 = Base64.encodeToString(jpegBytes, Base64.NO_WRAP)
                val thumb = scaleBitmapIfNeeded(scaled, MAX_THUMBNAIL_DIMENSION)
                if (scaled !== decoded && scaled !== thumb) {
                    decoded.recycle()
                }

                return@withContext Result.success(
                    NovaAttachment(
                        displayName = resolvedName,
                        mimeType = "image/jpeg",
                        isImage = true,
                        base64Data = base64,
                        previewBitmap = thumb,
                        sizeLabel = formatBytes(jpegBytes.size.toLong())
                    )
                )
            }

            val rawBytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext Result.failure(IllegalStateException("Could not open selected file."))
            val sizeLabel = formatBytes(rawBytes.size.toLong())

            // PDF supported natively via Gemini inline_data
            if (detectedMime == "application/pdf" || lowerName.endsWith(".pdf")) {
                if (rawBytes.size > MAX_PDF_BYTES) {
                    return@withContext Result.failure(IllegalArgumentException("PDF is too large (max 6 MB)."))
                }
                val base64 = Base64.encodeToString(rawBytes, Base64.NO_WRAP)
                return@withContext Result.success(
                    NovaAttachment(
                        displayName = resolvedName,
                        mimeType = "application/pdf",
                        isImage = false,
                        base64Data = base64,
                        sizeLabel = sizeLabel
                    )
                )
            }

            // DOCX text extraction
            if (lowerName.endsWith(".docx") || detectedMime.contains("wordprocessingml")) {
                val extracted = extractDocxText(rawBytes)
                if (extracted.isBlank()) {
                    return@withContext Result.failure(IllegalStateException("No readable text found inside $resolvedName."))
                }
                return@withContext Result.success(
                    NovaAttachment(
                        displayName = resolvedName,
                        mimeType = "text/plain",
                        isImage = false,
                        extractedText = extracted.take(MAX_TEXT_CHARS),
                        sizeLabel = sizeLabel
                    )
                )
            }

            // Plain text / CSV / code / markdown / JSON / XML
            val isTextLike = detectedMime.startsWith("text/") ||
                detectedMime.contains("json") ||
                detectedMime.contains("xml") ||
                lowerName.endsWith(".txt") ||
                lowerName.endsWith(".md") ||
                lowerName.endsWith(".csv") ||
                lowerName.endsWith(".json") ||
                lowerName.endsWith(".xml") ||
                lowerName.endsWith(".html") ||
                lowerName.endsWith(".kt") ||
                lowerName.endsWith(".java") ||
                lowerName.endsWith(".py") ||
                lowerName.endsWith(".js")

            if (isTextLike) {
                val content = rawBytes.toString(Charsets.UTF_8).trim()
                if (content.isBlank()) {
                    return@withContext Result.failure(IllegalStateException("Selected file is empty."))
                }
                return@withContext Result.success(
                    NovaAttachment(
                        displayName = resolvedName,
                        mimeType = detectedMime.ifBlank { "text/plain" },
                        isImage = false,
                        extractedText = content.take(MAX_TEXT_CHARS),
                        sizeLabel = sizeLabel
                    )
                )
            }

            Result.failure(
                IllegalArgumentException("Unsupported file type ($resolvedName). Try Image, PDF, TXT, CSV, or DOCX.")
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) cursor.getString(idx) else null
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun applyExifOrientation(bitmap: Bitmap, rawBytes: ByteArray): Bitmap {
        return try {
            val exif = ExifInterface(ByteArrayInputStream(rawBytes))
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
                else -> return bitmap
            }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (_: Exception) {
            bitmap
        }
    }

    private fun scaleBitmapIfNeeded(src: Bitmap, maxDim: Int): Bitmap {
        val longest = max(src.width, src.height)
        if (longest <= maxDim || longest <= 0) return src
        val ratio = maxDim.toFloat() / longest.toFloat()
        val newW = (src.width * ratio).roundToInt().coerceAtLeast(1)
        val newH = (src.height * ratio).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(src, newW, newH, true)
    }

    private fun extractDocxText(bytes: ByteArray): String {
        return try {
            ZipInputStream(bytes.inputStream()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (entry.name == "word/document.xml") {
                        val xml = zip.readBytes().toString(Charsets.UTF_8)
                        return xml
                            .replace(Regex("</w:p>"), "\n")
                            .replace(Regex("<[^>]+>"), " ")
                            .replace(Regex("[ \\t]+"), " ")
                            .replace(Regex("\\n{3,}"), "\n\n")
                            .trim()
                    }
                    entry = zip.nextEntry
                }
                ""
            }
        } catch (_: Exception) {
            ""
        }
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "${bytes} B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format("%.1f MB", bytes / (1024f * 1024f))
        }
    }
}
