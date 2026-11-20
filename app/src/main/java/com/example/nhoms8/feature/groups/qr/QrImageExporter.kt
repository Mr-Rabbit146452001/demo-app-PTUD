package com.example.nhoms8.feature.groups.qr

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import com.example.nhoms8.ui.theme.SpendlyTextDark
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * Tạo bitmap QR Code sắc nét và hỗ trợ lưu vào thư viện / chia sẻ ảnh qua FileProvider.
 */
object QrImageExporter {

    fun createQrAndroidBitmap(
        content: String,
        sizePx: Int = 768,
        foregroundColor: Int = SpendlyTextDark.toArgb(),
        backgroundColor: Int = Color.WHITE
    ): Bitmap? {
        if (content.isBlank() || sizePx <= 0) return null
        return try {
            val hints = mapOf(
                EncodeHintType.MARGIN to 2,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )
            val matrix = QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                sizePx,
                sizePx,
                hints
            )
            val width = matrix.width
            val height = matrix.height
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                val rowOffset = y * width
                for (x in 0 until width) {
                    pixels[rowOffset + x] = if (matrix.get(x, y)) foregroundColor else backgroundColor
                }
            }
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, width, 0, 0, width, height)
            }
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Lưu ảnh QR mời nhóm vào thư viện ảnh thiết bị (API 29+ qua MediaStore; API 24–28 qua app Pictures).
     */
    fun saveQrToGallery(
        context: Context,
        groupName: String,
        inviteCode: String
    ): Result<Uri> = runCatching {
        val normalizedCode = inviteCode.trim().uppercase(Locale.ROOT)
        require(normalizedCode.isNotEmpty()) { "Mã mời không hợp lệ" }

        val bitmap = createQrAndroidBitmap(content = normalizedCode)
            ?: error("Không thể tạo ảnh mã QR")

        val safeGroupPart = groupName
            .trim()
            .replace(Regex("[^A-Za-z0-9_-]+"), "_")
            .trim('_')
            .take(24)
            .ifEmpty { "Spendly" }
        val fileName = "Spendly_QR_${safeGroupPart}_${normalizedCode}_${System.currentTimeMillis()}.png"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Spendly")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: error("Không thể tạo tệp ảnh trong Thư viện")
                try {
                    resolver.openOutputStream(uri)?.use { out ->
                        if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                            error("Không thể ghi dữ liệu ảnh PNG")
                        }
                    } ?: error("Không thể mở luồng ghi MediaStore")

                    val publishValues = ContentValues().apply {
                        put(MediaStore.Images.Media.IS_PENDING, 0)
                    }
                    resolver.update(uri, publishValues, null, null)
                    uri
                } catch (error: Throwable) {
                    resolver.delete(uri, null, null)
                    throw error
                }
            } else {
                val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    ?: File(context.filesDir, "Pictures")
                if (!picturesDir.exists()) picturesDir.mkdirs()
                val outFile = File(picturesDir, fileName)
                FileOutputStream(outFile).use { out ->
                    if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                        error("Không thể ghi dữ liệu ảnh PNG")
                    }
                }
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    outFile
                )
            }
        } finally {
            bitmap.recycle()
        }
    }

    /**
     * Xuất ảnh PNG tạm trong cache và trả về content URI an toàn để chia sẻ qua Intent.ACTION_SEND.
     */
    fun createShareableQrUri(
        context: Context,
        inviteCode: String
    ): Result<Uri> = runCatching {
        val normalizedCode = inviteCode.trim().uppercase(Locale.ROOT)
        require(normalizedCode.isNotEmpty()) { "Mã mời không hợp lệ" }

        val bitmap = createQrAndroidBitmap(content = normalizedCode)
            ?: error("Không thể tạo ảnh mã QR")

        try {
            val qrCacheDir = File(context.cacheDir, "qr_codes").apply {
                if (!exists()) mkdirs()
            }
            val qrFile = File(qrCacheDir, "spendly_qr_$normalizedCode.png")
            FileOutputStream(qrFile).use { out ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    error("Không thể tạo tệp ảnh QR tạm")
                }
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                qrFile
            )
        } finally {
            bitmap.recycle()
        }
    }
}
