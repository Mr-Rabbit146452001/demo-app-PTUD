package com.example.nhoms8.feature.groups.qr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.ReaderException
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer

/**
 * Bộ giải mã QR Code độc lập dùng ZXing Core, hỗ trợ cả luồng CameraX (YUV_420_888)
 * lẫn ảnh Bitmap / Uri từ thư viện máy.
 */
object QrCodeDecoder {

    private val decodeHints: Map<DecodeHintType, Any> = mapOf(
        DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
        DecodeHintType.TRY_HARDER to true,
        DecodeHintType.CHARACTER_SET to "UTF-8"
    )

    /**
     * Giải mã QR từ dữ liệu độ sáng Y (YUV plane 0), hỗ trợ `rowStride > width`
     * để tránh lỗi xiên khung hình trên các thiết bị Samsung/Xiaomi.
     */
    fun decodeFromLuminanceBytes(
        yData: ByteArray,
        width: Int,
        height: Int,
        rowStride: Int = width
    ): String? {
        if (width <= 0 || height <= 0 || rowStride < width) return null
        val requiredSize = (height - 1) * rowStride + width
        if (yData.size < requiredSize) return null

        val packed = if (rowStride == width) {
            yData
        } else {
            ByteArray(width * height).also { compact ->
                for (row in 0 until height) {
                    System.arraycopy(
                        yData,
                        row * rowStride,
                        compact,
                        row * width,
                        width
                    )
                }
            }
        }

        // Ưu tiên cắt vùng giữa (70% cạnh nhỏ nhất) để bắt QR nhanh hơn trong khung ngắm
        val cropSize = (minOf(width, height) * 0.7f).toInt().coerceAtLeast(1)
        if (cropSize < minOf(width, height)) {
            val left = (width - cropSize) / 2
            val top = (height - cropSize) / 2
            val roiSource = PlanarYUVLuminanceSource(
                packed,
                width,
                height,
                left,
                top,
                cropSize,
                cropSize,
                false
            )
            decodeWithHybridThenHistogram(roiSource)?.let { return it }
        }

        val fullSource = PlanarYUVLuminanceSource(
            packed,
            width,
            height,
            0,
            0,
            width,
            height,
            false
        )
        return decodeWithHybridThenHistogram(fullSource)
    }

    /**
     * Giải mã QR từ mảng pixel ARGB_8888 (pure JVM, dùng chung cho Android Bitmap & Unit Test).
     */
    fun decodeFromArgbPixels(
        pixels: IntArray,
        width: Int,
        height: Int
    ): String? {
        if (width <= 0 || height <= 0 || pixels.size < width * height) return null
        val source = RGBLuminanceSource(width, height, pixels)
        return decodeWithHybridThenHistogram(source)
    }

    /**
     * Giải mã QR từ [ImageProxy] của CameraX ImageAnalysis (caller tự đóng `imageProxy` trong `finally`).
     */
    fun decodeFromImageProxy(imageProxy: ImageProxy): String? {
        val yPlane = imageProxy.planes.firstOrNull() ?: return null
        val buffer = yPlane.buffer
        buffer.rewind()
        val yBytes = ByteArray(buffer.remaining())
        buffer.get(yBytes)
        return decodeFromLuminanceBytes(
            yData = yBytes,
            width = imageProxy.width,
            height = imageProxy.height,
            rowStride = yPlane.rowStride
        )
    }

    /**
     * Giải mã QR từ [Bitmap].
     */
    fun decodeFromBitmap(bitmap: Bitmap): String? {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= 0 || height <= 0) return null
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        return decodeFromArgbPixels(pixels, width, height)
    }

    /**
     * Giải mã QR từ [Uri] ảnh (Android Photo Picker), tự động thu nhỏ ảnh lớn
     * xuống cạnh tối đa [maxDimensionPx] để tránh tràn bộ nhớ.
     */
    fun decodeFromUri(
        context: Context,
        uri: Uri,
        maxDimensionPx: Int = 1024
    ): String? {
        val resolver = context.contentResolver
        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        } ?: return null

        val srcWidth = boundsOptions.outWidth
        val srcHeight = boundsOptions.outHeight
        if (srcWidth <= 0 || srcHeight <= 0) return null

        val sampleSize = calculateInSampleSize(srcWidth, srcHeight, maxDimensionPx)
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val bitmap = resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: return null

        return try {
            decodeFromBitmap(bitmap)
        } finally {
            bitmap.recycle()
        }
    }

    private fun decodeWithHybridThenHistogram(source: LuminanceSource): String? {
        val reader = MultiFormatReader().apply {
            setHints(decodeHints)
        }
        return try {
            reader.decodeWithState(BinaryBitmap(HybridBinarizer(source))).text
        } catch (_: ReaderException) {
            reader.reset()
            try {
                reader.decodeWithState(BinaryBitmap(GlobalHistogramBinarizer(source))).text
            } catch (_: ReaderException) {
                null
            }
        } finally {
            reader.reset()
        }
    }

    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        maxDimensionPx: Int
    ): Int {
        var inSampleSize = 1
        val maxSide = maxOf(width, height)
        if (maxSide > maxDimensionPx) {
            while (maxSide / (inSampleSize * 2) >= maxDimensionPx) {
                inSampleSize *= 2
            }
            if (maxSide / inSampleSize > maxDimensionPx) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }
}
