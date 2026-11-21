package com.example.nhoms8.feature.receipt

import com.example.nhoms8.domain.model.SpendlyCategory

/**
 * Mức độ tin cậy của kết quả trích xuất hóa đơn từ AI Vision.
 */
enum class ReceiptConfidence(val label: String) {
    HIGH("Độ tin cậy cao"),
    MEDIUM("Cần kiểm tra nhẹ"),
    LOW("Ảnh mờ — vui lòng kiểm tra kỹ");

    companion object {
        fun fromRaw(raw: String?): ReceiptConfidence {
            val normalized = raw?.trim()?.uppercase().orEmpty()
            return when {
                normalized.contains("HIGH") || normalized.contains("CAO") -> HIGH
                normalized.contains("LOW") || normalized.contains("THẤP") || normalized.contains("THAP") -> LOW
                else -> MEDIUM
            }
        }
    }
}

/**
 * Một dòng món ăn / mặt hàng trích xuất được từ hóa đơn.
 */
data class ReceiptLineItem(
    val name: String,
    val quantity: Int = 1,
    val amount: Long = 0L
)

/**
 * Kết quả phân tích hóa đơn từ Gemini Vision AI kèm đường dẫn ảnh đã nén lưu cục bộ.
 */
data class ScannedReceiptResult(
    val isReceipt: Boolean = true,
    val title: String = "",
    val totalAmount: Long = 0L,
    val category: SpendlyCategory = SpendlyCategory.OTHER,
    val expenseDateMillis: Long? = null,
    val items: List<ReceiptLineItem> = emptyList(),
    val description: String = "",
    val confidence: ReceiptConfidence = ReceiptConfidence.MEDIUM,
    val warningMessage: String? = null,
    val savedReceiptPath: String = "",
    val rawSummary: String = ""
)
