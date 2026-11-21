package com.example.nhoms8.feature.receipt

import com.example.nhoms8.algorithm.FintechAlgorithm
import com.example.nhoms8.domain.model.SpendlyCategory
import java.util.Calendar
import java.util.Locale

/**
 * Bộ phân tích phản hồi JSON từ Gemini Vision OCR bằng Kotlin thuần (không dùng org.json stub của JVM),
 * hỗ trợ chuẩn hóa định dạng tiền VNĐ, ngày hóa đơn, 9 danh mục Spendly và danh sách món.
 */
object ReceiptScanParser {

    fun parseResponse(
        rawText: String,
        savedReceiptPath: String = "",
        fallbackNowMillis: Long = System.currentTimeMillis()
    ): Result<ScannedReceiptResult> {
        val jsonObjectText = extractOuterJsonObject(rawText)
            ?: return Result.failure(IllegalArgumentException("AI chưa trả về đúng định dạng dữ liệu hóa đơn"))

        val isReceipt = extractBooleanField(jsonObjectText, "isReceipt") ?: true
        val warning = extractStringField(jsonObjectText, "warning")
            ?.takeIf { it.isNotBlank() }
            ?: extractStringField(jsonObjectText, "reason")?.takeIf { it.isNotBlank() }

        if (!isReceipt) {
            return Result.success(
                ScannedReceiptResult(
                    isReceipt = false,
                    title = "",
                    totalAmount = 0L,
                    category = SpendlyCategory.OTHER,
                    expenseDateMillis = fallbackNowMillis,
                    items = emptyList(),
                    description = "",
                    confidence = ReceiptConfidence.LOW,
                    warningMessage = warning ?: "Ảnh này có vẻ không phải là hóa đơn thanh toán. Vui lòng chụp lại rõ hơn.",
                    savedReceiptPath = savedReceiptPath,
                    rawSummary = rawText
                )
            )
        }

        val merchant = extractStringField(jsonObjectText, "merchant").orEmpty().trim()
        val rawTitle = extractStringField(jsonObjectText, "title").orEmpty().trim()
        val resolvedTitle = when {
            rawTitle.isNotBlank() -> rawTitle
            merchant.isNotBlank() -> merchant
            else -> "Chi tiêu từ hóa đơn"
        }

        val amountRaw = extractRawValue(jsonObjectText, "totalAmount")
            ?: extractRawValue(jsonObjectText, "amount")
            ?: extractRawValue(jsonObjectText, "total")
        val parsedAmount = parseVndAmount(amountRaw)

        val categoryRaw = extractStringField(jsonObjectText, "category")
        val resolvedCategory = parseCategory(categoryRaw)

        val dateRaw = extractStringField(jsonObjectText, "date")
        val resolvedDateMillis = parseReceiptDateMillis(dateRaw) ?: fallbackNowMillis

        val confidence = ReceiptConfidence.fromRaw(extractStringField(jsonObjectText, "confidence"))
        val items = extractItemsArray(jsonObjectText)
        val noteRaw = extractStringField(jsonObjectText, "note").orEmpty().trim()
        val description = formatItemsDescription(items = items, note = noteRaw)

        val effectiveAmount = if (parsedAmount > 0L) {
            parsedAmount
        } else {
            items.sumOf { it.amount.coerceAtLeast(0L) }
        }

        val finalWarning = when {
            warning != null -> warning
            effectiveAmount <= 0L ->
                "Chưa nhìn rõ tổng tiền trên ảnh, bạn hãy nhập hoặc chỉnh lại số tiền bên dưới nhé."
            else -> null
        }

        return Result.success(
            ScannedReceiptResult(
                isReceipt = true,
                title = resolvedTitle,
                totalAmount = effectiveAmount,
                category = resolvedCategory,
                expenseDateMillis = resolvedDateMillis,
                items = items,
                description = description,
                confidence = if (effectiveAmount <= 0L) ReceiptConfidence.LOW else confidence,
                warningMessage = finalWarning,
                savedReceiptPath = savedReceiptPath,
                rawSummary = rawText
            )
        )
    }

    /**
     * Ghép danh sách món đọc được từ hóa đơn thành ghi chú gọn gàng cho khoản chi.
     */
    fun formatItemsDescription(
        items: List<ReceiptLineItem>,
        note: String = ""
    ): String {
        val parts = mutableListOf<String>()
        if (note.isNotBlank()) {
            parts.add(note.trim())
        }
        if (items.isNotEmpty()) {
            val formattedItems = items.take(12).joinToString(", ") { item ->
                val qtyPrefix = if (item.quantity > 1) "${item.quantity}x " else ""
                val priceSuffix = if (item.amount > 0L) " (${FintechAlgorithm.formatVnd(item.amount)})" else ""
                "$qtyPrefix${item.name}$priceSuffix"
            }
            val overflowNote = if (items.size > 12) " (+${items.size - 12} món khác)" else ""
            parts.add("Chi tiết hóa đơn: $formattedItems$overflowNote")
        }
        return parts.joinToString("\n").trim()
    }

    /**
     * Chuẩn hóa số tiền VNĐ từ số nguyên hoặc chuỗi ("245.000 đ", "1,250,000", "180k", "1.5tr").
     */
    fun parseVndAmount(raw: String?): Long {
        if (raw.isNullOrBlank()) return 0L
        val cleaned = raw.trim()
            .removeSurrounding("\"")
            .lowercase(Locale.ROOT)
            .replace("vnđ", "")
            .replace("vnd", "")
            .replace("đ", "")
            .replace("d", "")
            .replace(" ", "")

        if (cleaned.isEmpty()) return 0L

        // Dạng viết tắt "180k" / "1.5tr"
        val multiplierMatch = Regex("^([0-9]+(?:[.,][0-9]+)?)(k|nghin|ngan|tr|trieu|m)$").find(cleaned)
        if (multiplierMatch != null) {
            val numStr = multiplierMatch.groupValues[1].replace(',', '.')
            val unit = multiplierMatch.groupValues[2]
            val value = numStr.toDoubleOrNull() ?: return 0L
            val multiplier = when (unit) {
                "k", "nghin", "ngan" -> 1_000.0
                else -> 1_000_000.0
            }
            return (value * multiplier).toLong().coerceAtLeast(0L)
        }

        // Bỏ phần thập phân .0 / .00 ở cuối nếu có (ví dụ "245000.00")
        val withoutTrailingCents = cleaned.replace(Regex("\\.0{1,2}$"), "")
        val digitsOnly = withoutTrailingCents.replace(Regex("[^0-9]"), "")
        return digitsOnly.toLongOrNull()?.coerceAtLeast(0L) ?: 0L
    }

    /**
     * Ánh xạ chuỗi danh mục từ AI về đúng 9 danh mục SpendlyCategory.
     */
    fun parseCategory(raw: String?): SpendlyCategory {
        if (raw.isNullOrBlank()) return SpendlyCategory.OTHER
        val trimmed = raw.trim()
        SpendlyCategory.entries.firstOrNull {
            it.name.equals(trimmed, ignoreCase = true) ||
                it.label.equals(trimmed, ignoreCase = true)
        }?.let { return it }

        val lower = trimmed.lowercase(Locale.ROOT)
        return when {
            lower.contains("food") || lower.contains("ăn") || lower.contains("uống") ||
                lower.contains("cafe") || lower.contains("coffee") -> SpendlyCategory.FOOD
            lower.contains("transport") || lower.contains("di chuyển") || lower.contains("xe") ||
                lower.contains("xăng") || lower.contains("grab") -> SpendlyCategory.TRANSPORT
            lower.contains("housing") || lower.contains("nhà") || lower.contains("thuê") -> SpendlyCategory.HOUSING
            lower.contains("shopping") || lower.contains("mua sắm") || lower.contains("siêu thị") -> SpendlyCategory.SHOPPING
            lower.contains("entertainment") || lower.contains("giải trí") || lower.contains("vé") -> SpendlyCategory.ENTERTAINMENT
            lower.contains("health") || lower.contains("y tế") || lower.contains("thuốc") -> SpendlyCategory.HEALTH
            lower.contains("education") || lower.contains("giáo dục") || lower.contains("học") -> SpendlyCategory.EDUCATION
            lower.contains("utilities") || lower.contains("sinh hoạt") || lower.contains("điện") ||
                lower.contains("nước") || lower.contains("mạng") -> SpendlyCategory.UTILITIES
            else -> SpendlyCategory.OTHER
        }
    }

    /**
     * Phân tích ngày trên hóa đơn ("21/11/2026", "2026-11-21", "21-11-2026") về 12:00 trưa local time.
     */
    fun parseReceiptDateMillis(raw: String?): Long? {
        if (raw.isNullOrBlank()) return null
        val cleaned = raw.trim().split(" ", "T").firstOrNull().orEmpty()
        if (cleaned.isBlank()) return null

        val ymdMatch = Regex("^(\\d{4})[-/.](\\d{1,2})[-/.](\\d{1,2})$").find(cleaned)
        if (ymdMatch != null) {
            val year = ymdMatch.groupValues[1].toIntOrNull() ?: return null
            val month = ymdMatch.groupValues[2].toIntOrNull() ?: return null
            val day = ymdMatch.groupValues[3].toIntOrNull() ?: return null
            return buildLocalNoonMillis(year, month, day)
        }

        val dmyMatch = Regex("^(\\d{1,2})[-/.](\\d{1,2})[-/.](\\d{2,4})$").find(cleaned)
        if (dmyMatch != null) {
            val day = dmyMatch.groupValues[1].toIntOrNull() ?: return null
            val month = dmyMatch.groupValues[2].toIntOrNull() ?: return null
            val rawYear = dmyMatch.groupValues[3].toIntOrNull() ?: return null
            val year = if (rawYear < 100) 2000 + rawYear else rawYear
            return buildLocalNoonMillis(year, month, day)
        }

        return null
    }

    private fun buildLocalNoonMillis(year: Int, month: Int, day: Int): Long? {
        if (year !in 2000..2100 || month !in 1..12 || day !in 1..31) return null
        val cal = Calendar.getInstance().apply {
            isLenient = false
            set(year, month - 1, day, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return runCatching { cal.timeInMillis }.getOrNull()
    }

    /**
     * Trích xuất nội dung văn bản từ phản hồi JSON REST của Google Gemini (`candidates[0].content.parts[*].text`)
     * bằng Kotlin thuần (không phụ thuộc `org.json` stub trên JVM test).
     */
    fun extractCandidateTextFromApiResponse(rawHttpJson: String): String? {
        if (rawHttpJson.isBlank()) return null
        val candidatesIndex = rawHttpJson.indexOf("\"candidates\"")
        if (candidatesIndex < 0) return null

        val searchScope = rawHttpJson.substring(candidatesIndex)
        val arrayStart = searchScope.indexOf('[')
        if (arrayStart < 0) return null
        val arrayEnd = findMatchingBrace(searchScope, arrayStart, '[', ']')
        val candidatesBlock = if (arrayEnd > arrayStart) {
            searchScope.substring(arrayStart, arrayEnd + 1)
        } else {
            searchScope
        }

        // Lấy các part nằm trong ứng viên đầu tiên (trước phần tử thứ hai nếu có)
        val firstCandidateStart = candidatesBlock.indexOf('{')
        if (firstCandidateStart < 0) return null
        val firstCandidateEnd = findMatchingBrace(candidatesBlock, firstCandidateStart, '{', '}')
        val firstCandidateBlock = if (firstCandidateEnd > firstCandidateStart) {
            candidatesBlock.substring(firstCandidateStart, firstCandidateEnd + 1)
        } else {
            candidatesBlock
        }

        val textRegex = Regex("\"text\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
        val collectedParts = textRegex.findAll(firstCandidateBlock)
            .mapNotNull { match ->
                match.groupValues.getOrNull(1)?.let(::unescapeJsonString)?.takeIf { it.isNotBlank() }
            }
            .toList()

        return if (collectedParts.isEmpty()) null else collectedParts.joinToString("\n")
    }

    internal fun extractOuterJsonObject(rawText: String): String? {
        val fenced = Regex("```(?:json)?\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
            .find(rawText)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
        val candidate = if (!fenced.isNullOrBlank()) fenced else rawText
        val start = candidate.indexOf('{')
        if (start < 0) return null
        val end = findMatchingBrace(candidate, start, '{', '}')
        return if (end > start) candidate.substring(start, end + 1) else null
    }

    private fun findMatchingBrace(text: String, startIndex: Int, openChar: Char, closeChar: Char): Int {
        var depth = 0
        var inString = false
        var escaped = false
        for (i in startIndex until text.length) {
            val c = text[i]
            if (escaped) {
                escaped = false
                continue
            }
            if (c == '\\' && inString) {
                escaped = true
                continue
            }
            if (c == '"') {
                inString = !inString
                continue
            }
            if (!inString) {
                if (c == openChar) {
                    depth++
                } else if (c == closeChar) {
                    depth--
                    if (depth == 0) return i
                }
            }
        }
        return -1
    }

    private fun extractStringField(json: String, key: String): String? {
        val regex = Regex("\"$key\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
        val raw = regex.find(json)?.groupValues?.getOrNull(1) ?: return null
        return unescapeJsonString(raw).trim()
    }

    internal fun unescapeJsonString(raw: String): String {
        val sb = StringBuilder(raw.length)
        var i = 0
        while (i < raw.length) {
            val c = raw[i]
            if (c == '\\' && i + 1 < raw.length) {
                when (val next = raw[i + 1]) {
                    '"' -> sb.append('"')
                    '\\' -> sb.append('\\')
                    '/' -> sb.append('/')
                    'b' -> sb.append('\b')
                    'f' -> sb.append('\u000C')
                    'n' -> sb.append('\n')
                    'r' -> sb.append('\r')
                    't' -> sb.append('\t')
                    'u' -> {
                        if (i + 5 < raw.length) {
                            val hex = raw.substring(i + 2, i + 6)
                            val codePoint = hex.toIntOrNull(16)
                            if (codePoint != null) {
                                sb.append(codePoint.toChar())
                                i += 4
                            } else {
                                sb.append("\\u")
                            }
                        } else {
                            sb.append("\\u")
                        }
                    }
                    else -> sb.append(next)
                }
                i += 2
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }

    private fun extractBooleanField(json: String, key: String): Boolean? {
        val regex = Regex("\"$key\"\\s*:\\s*(true|false|\"true\"|\"false\")", RegexOption.IGNORE_CASE)
        val raw = regex.find(json)?.groupValues?.getOrNull(1)
            ?.removeSurrounding("\"")
            ?.lowercase(Locale.ROOT)
            ?: return null
        return when (raw) {
            "true" -> true
            "false" -> false
            else -> null
        }
    }

    private fun extractRawValue(json: String, key: String): String? {
        val strVal = extractStringField(json, key)
        if (strVal != null) return strVal
        val numRegex = Regex("\"$key\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)")
        return numRegex.find(json)?.groupValues?.getOrNull(1)
    }

    private fun extractItemsArray(json: String): List<ReceiptLineItem> {
        val keyRegex = Regex("\"items\"\\s*:\\s*\\[")
        val match = keyRegex.find(json) ?: return emptyList()
        val arrayStart = match.range.last
        val arrayEnd = findMatchingBrace(json, arrayStart, '[', ']')
        if (arrayEnd <= arrayStart) return emptyList()

        val arrayBody = json.substring(arrayStart + 1, arrayEnd)
        val result = mutableListOf<ReceiptLineItem>()
        var cursor = 0
        while (cursor < arrayBody.length) {
            val objStart = arrayBody.indexOf('{', cursor)
            if (objStart < 0) break
            val objEnd = findMatchingBrace(arrayBody, objStart, '{', '}')
            if (objEnd <= objStart) break
            val objText = arrayBody.substring(objStart, objEnd + 1)
            val name = extractStringField(objText, "name").orEmpty().trim()
            if (name.isNotBlank()) {
                val qty = extractRawValue(objText, "quantity")
                    ?.toDoubleOrNull()
                    ?.toInt()
                    ?.coerceAtLeast(1)
                    ?: 1
                val amt = parseVndAmount(extractRawValue(objText, "amount") ?: extractRawValue(objText, "price"))
                result.add(ReceiptLineItem(name = name, quantity = qty, amount = amt))
            }
            cursor = objEnd + 1
        }
        return result
    }
}
