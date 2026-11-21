package com.example.nhoms8.feature.receipt

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

/**
 * Ảnh hóa đơn đã qua tiền xử lý (xoay đúng chiều EXIF, thu nhỏ <= 1280px, nén JPEG cục bộ).
 */
data class PreparedReceiptImage(
    val bitmap: Bitmap,
    val savedFilePath: String
)

/**
 * Tiện ích xử lý ảnh hóa đơn trước khi gửi Gemini Vision & lưu bản nội bộ cho chi tiết khoản chi.
 */
object ReceiptImageProcessor {

    private const val MAX_IMAGE_DIMENSION = 1280
    private const val JPEG_QUALITY = 80
    private const val RECEIPTS_DIR = "receipts"

    suspend fun prepareFromUri(context: Context, uri: Uri): Result<PreparedReceiptImage> =
        withContext(Dispatchers.IO) {
            runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, bounds)
                } ?: throw IllegalStateException("Không thể đọc ảnh từ đường dẫn đã chọn")

                val sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, MAX_IMAGE_DIMENSION)
                val decodeOpts = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val rawBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOpts)
                } ?: throw IllegalStateException("Không thể giải mã ảnh hóa đơn")

                val orientation = context.contentResolver.openInputStream(uri)?.use { stream ->
                    runCatching {
                        ExifInterface(stream).getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
                } ?: ExifInterface.ORIENTATION_NORMAL

                val oriented = applyExifOrientation(rawBitmap, orientation)
                val scaled = scaleDownIfNeeded(oriented, MAX_IMAGE_DIMENSION)
                val savedFile = saveCompressedJpeg(context, scaled)
                PreparedReceiptImage(bitmap = scaled, savedFilePath = savedFile.absolutePath)
            }
        }

    suspend fun prepareFromFile(
        context: Context,
        sourceFile: File,
        deleteSourceAfter: Boolean = true
    ): Result<PreparedReceiptImage> = withContext(Dispatchers.IO) {
        runCatching {
            if (!sourceFile.exists()) {
                throw IllegalStateException("Không tìm thấy ảnh vừa chụp")
            }
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(sourceFile.absolutePath, bounds)

            val sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, MAX_IMAGE_DIMENSION)
            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val rawBitmap = BitmapFactory.decodeFile(sourceFile.absolutePath, decodeOpts)
                ?: throw IllegalStateException("Không thể giải mã ảnh vừa chụp")

            val orientation = runCatching {
                ExifInterface(sourceFile.absolutePath).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

            val oriented = applyExifOrientation(rawBitmap, orientation)
            val scaled = scaleDownIfNeeded(oriented, MAX_IMAGE_DIMENSION)
            val savedFile = saveCompressedJpeg(context, scaled)
            if (deleteSourceAfter && sourceFile.absolutePath != savedFile.absolutePath) {
                runCatching { sourceFile.delete() }
            }
            PreparedReceiptImage(bitmap = scaled, savedFilePath = savedFile.absolutePath)
        }
    }

    suspend fun loadSavedBitmap(path: String): Bitmap? = withContext(Dispatchers.IO) {
        if (path.isBlank()) return@withContext null
        val file = File(path)
        if (!file.exists()) return@withContext null
        runCatching {
            val opts = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
            BitmapFactory.decodeFile(file.absolutePath, opts)
        }.getOrNull()
    }

    fun createTempCaptureFile(context: Context): File {
        val cacheDir = File(context.cacheDir, RECEIPTS_DIR).apply { mkdirs() }
        return File(cacheDir, "capture_${System.currentTimeMillis()}.jpg")
    }

    private fun saveCompressedJpeg(context: Context, bitmap: Bitmap): File {
        val dir = File(context.filesDir, RECEIPTS_DIR).apply { mkdirs() }
        val target = File(dir, "receipt_${System.currentTimeMillis()}.jpg")
        FileOutputStream(target).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            out.flush()
        }
        return target
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxDim: Int): Int {
        var inSampleSize = 1
        if (width <= 0 || height <= 0) return 1
        val largest = max(width, height)
        while ((largest / inSampleSize) > maxDim * 2) {
            inSampleSize *= 2
        }
        return inSampleSize
    }

    private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.preScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.preScale(-1f, 1f)
            }
            else -> return bitmap
        }
        return runCatching {
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated != bitmap) bitmap.recycle()
            rotated
        }.getOrDefault(bitmap)
    }

    private fun scaleDownIfNeeded(bitmap: Bitmap, maxDim: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val largest = max(width, height)
        if (largest <= maxDim) return bitmap

        val ratio = maxDim.toFloat() / largest.toFloat()
        val targetWidth = (width * ratio).toInt().coerceAtLeast(1)
        val targetHeight = (height * ratio).toInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        if (scaled != bitmap) bitmap.recycle()
        return scaled
    }
}
