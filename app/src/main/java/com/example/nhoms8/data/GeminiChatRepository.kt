package com.example.nhoms8.data

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.nhoms8.BuildConfig
import com.example.nhoms8.ExpenseCategory
import com.example.nhoms8.feature.receipt.ReceiptScanParser
import com.example.nhoms8.feature.receipt.ScannedReceiptResult
import com.example.nhoms8.model.ChatExpenseProposal
import com.example.nhoms8.model.ChatReminderProposal
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.SocketTimeoutException
import java.net.URL
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection

/**
 * Kết quả xử lý câu trả lời từ Gemini AI
 */
data class GeminiResult(
    val cleanText: String,
    val expenseProposal: ChatExpenseProposal? = null,
    val reminderProposal: ChatReminderProposal? = null,
    val isFromGeminiCloud: Boolean = true
)

/**
 * Kho xử lý Trợ lý ảo Google Gemini 2.5 Flash (GeminiChatRepository)
 * Phụ trách bởi Kỹ sư Thuật toán & Dữ liệu - Nhóm 08 (Spendly)
 */
class GeminiChatRepository(
    private val apiKey: String = BuildConfig.GEMINI_API_KEY
) {

    private val systemInstructionText = """
        Bạn là FinSprout 🌱 - Trợ lý AI tài chính cá nhân & nhóm thông minh của ứng dụng Spendly (Nhóm 08).
        Tính cách: Thân thiện, rõ ràng, thực tế, am hiểu tài chính, luôn xưng 'FinSprout' hoặc 'mình', gọi người dùng là 'bạn'.
        Luôn sử dụng biểu tượng cảm xúc phù hợp: 🌱, 💰, 📊, 💌, ✨.

        Nhiệm vụ 1: GHI NHẬN KHOẢN CHI TỰ NHIÊN
        Chỉ khi người dùng mô tả một khoản chi mới vừa phát sinh có số tiền cụ thể (ví dụ: 'Ăn lẩu 650k Hùng trả', 'Mua trà sữa 90k', 'Tiền mạng 330k'):
        Hãy gửi phản hồi xác nhận ngắn gọn và LUÔN đính kèm khối thẻ hành động ở cuối câu trả lời theo đúng định dạng sau:
        <<<ACTION_EXPENSE>>>
        {"title":"Ăn lẩu nhóm","amount":650000,"category":"FOOD","categoryName":"Ăn uống","payerName":"Lê Văn Hùng","splitSummary":"Chia đều cho cả nhóm"}
        <<<END_ACTION_EXPENSE>>>
        (Các danh mục hợp lệ: FOOD - Ăn uống, HOUSING - Nhà ở & Tiện ích, TRANSPORT - Đi lại & Di chuyển, OTHER - Chi tiêu khác).

        Nhiệm vụ 2: SOẠN MẪU TIN NHẮN NHẮC NỢ
        Chỉ khi người dùng nhờ soạn tin nhắn nhắc nợ / đòi nợ ai đó (ví dụ: 'Viết tin nhắn nhắc Linh chuyển 120k tiền lẩu', 'Nhắc nợ bạn Đạt'):
        Hãy viết một tin nhắn dễ thương, lịch sự, không làm mất lòng bạn bè và LUÔN đính kèm khối:
        <<<ACTION_REMINDER>>>
        {"recipientName":"Linh","amountFormatted":"120.000 đ","suggestedMessage":"Ting ting! 🧋 Linh ơi, hôm trước nhóm mình ăn lẩu vui quá. Phần của bạn là 120.000đ nè, khi nào tiện bạn chuyển khoản vào quỹ giúp nhóm nhé! Cảm ơn bạn nhiều ❤️"}
        <<<END_ACTION_REMINDER>>>
        (Lưu ý: Nếu người dùng chỉ hỏi "Ai đang nợ ai?" hay "Tôi nợ bao nhiêu?", hãy trả lời số liệu từ ngữ cảnh, KHÔNG xuất <<<ACTION_REMINDER>>>).

        Nhiệm vụ 3: PHÂN TÍCH TÀI CHÍNH, HỎI ĐÁP CÔNG NỢ & MẸO TIẾT KIỆM
        Dựa chính xác vào số liệu trong [NGỮ CẢNH QUỸ NHÓM HIỆN TẠI] (không bịa số liệu), trả lời đúng trọng tâm câu hỏi của người dùng một cách ngắn gọn, dễ hiểu.
    """.trimIndent()

    private data class VisionModelSpec(
        val modelName: String,
        val supportsThinkingBudgetZero: Boolean,
        val readTimeoutMs: Int
    )

    private val primaryModel: GenerativeModel? by lazy {
        createModel("gemini-3.1-flash-lite")
    }

    private val fallbackModel: GenerativeModel? by lazy {
        createModel("gemini-3.6-flash")
    }

    private fun createModel(modelName: String): GenerativeModel? {
        if (!isApiKeyConfigured()) return null
        return try {
            GenerativeModel(
                modelName = modelName,
                apiKey = apiKey,
                systemInstruction = content { text(systemInstructionText) },
                generationConfig = generationConfig {
                    temperature = 0.6f
                    topK = 40
                    topP = 0.95f
                }
            )
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Kiểm tra xem API Key Gemini đã được cấu hình hợp lệ hay chưa
     */
    fun isApiKeyConfigured(): Boolean {
        return apiKey.isNotBlank() && apiKey != "\"\"" && apiKey != "YOUR_GEMINI_API_KEY_HERE"
    }

    /**
     * Gửi tin nhắn tới Gemini 2.5 Flash kèm ngữ cảnh quỹ nhóm và lịch sử hội thoại gần nhất
     */
    suspend fun sendMessage(
        userMessage: String,
        groupContextPrompt: String,
        recentHistory: List<Pair<Boolean, String>> = emptyList()
    ): GeminiResult = withContext(Dispatchers.IO) {
        val model = primaryModel
            ?: throw IllegalStateException("Chưa cấu hình GEMINI_API_KEY trong local.properties")

        val historyBlock = if (recentHistory.isNotEmpty()) {
            val formatted = recentHistory.joinToString("\n") { (isUser, text) ->
                val role = if (isUser) "Người dùng" else "FinSprout"
                "$role: ${text.take(300)}"
            }
            "\n\n[LỊCH SỬ HỘI THOẠI GẦN ĐÂY]\n$formatted"
        } else {
            ""
        }

        val fullPrompt = """
            [NGỮ CẢNH QUỸ NHÓM HIỆN TẠI]
            $groupContextPrompt$historyBlock
            
            [TIN NHẮN NGƯỜI DÙNG]
            $userMessage
        """.trimIndent()

        val response = try {
            model.generateContent(fullPrompt)
        } catch (primaryError: Exception) {
            val backup = fallbackModel ?: throw primaryError
            backup.generateContent(fullPrompt)
        }
        val rawText = response.text ?: "FinSprout đã lắng nghe bạn 🌱"

        parseGeminiOutput(rawText)
    }

    /**
     * Phân tích ảnh hóa đơn bằng Gemini 3.x Vision REST API (tự động fallback qua chuỗi model khả dụng)
     * và trả về dữ liệu có cấu trúc cho khoản chi nhóm.
     */
    suspend fun scanReceipt(
        bitmap: Bitmap,
        savedReceiptPath: String,
        groupName: String = "",
        memberNames: List<String> = emptyList()
    ): Result<ScannedReceiptResult> = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext Result.failure(
                IllegalStateException("Chưa cấu hình GEMINI_API_KEY trong local.properties để quét hóa đơn AI")
            )
        }

        val contextNote = buildString {
            if (groupName.isNotBlank()) append("Quỹ nhóm hiện tại: $groupName. ")
            if (memberNames.isNotEmpty()) append("Thành viên: ${memberNames.joinToString(", ")}.")
        }.trim()

        val receiptPrompt = """
            Bạn là hệ thống OCR phân tích hóa đơn thanh toán Việt Nam cho ứng dụng Spendly.
            Hãy đọc ảnh đính kèm (chấp nhận hóa đơn giấy, phiếu tính tiền quán ăn/cafe, hóa đơn siêu thị, biên lai chuyển khoản ngân hàng/ví điện tử, đơn hàng online) và trả về DUY NHẤT 1 đối tượng JSON (không thêm giải thích ngoài JSON):
            {
              "isReceipt": true,
              "title": "Tên cửa hàng / quán ăn hoặc nội dung chi tiêu ngắn gọn tiếng Việt",
              "totalAmount": 245000,
              "category": "FOOD",
              "date": "21/11/2026",
              "confidence": "HIGH",
              "warning": "",
              "items": [
                {"name": "Tên món / mặt hàng", "quantity": 1, "amount": 49000}
              ]
            }

            Quy tắc bắt buộc:
            1. Chỉ đặt "isReceipt": false khi ảnh hoàn toàn không liên quan tới giao dịch/thanh toán/mua bán (ví dụ ảnh phong cảnh, ảnh chân dung); nếu ảnh là hóa đơn hơi mờ hoặc bị khuất một phần vẫn đặt "isReceipt": true, trích xuất tối đa thông tin đọc được và đặt "confidence": "LOW" kèm "warning".
            2. "totalAmount" là SỐ NGUYÊN VNĐ thể hiện TỔNG TIỀN THANH TOÁN CUỐI CÙNG (sau thuế VAT, phí dịch vụ, giảm giá; tuyệt đối không nhầm với "Tiền khách đưa", "Tiền thừa" hay số lượng). Nếu không đọc được số tiền thì đặt 0.
            3. "category" chỉ được chọn 1 trong 9 mã: FOOD (Ăn uống), TRANSPORT (Di chuyển), HOUSING (Nhà ở), SHOPPING (Mua sắm), ENTERTAINMENT (Giải trí), HEALTH (Y tế), EDUCATION (Giáo dục), UTILITIES (Sinh hoạt/điện/nước/internet), OTHER (Khác).
            4. "date" theo định dạng dd/MM/yyyy nếu thấy trên hóa đơn, nếu không thấy thì để "".
            5. "confidence" chọn 1 trong 3 giá trị: HIGH, MEDIUM, LOW.
            ${if (contextNote.isNotBlank()) "Ngữ cảnh tham khảo: $contextNote" else ""}
        """.trimIndent()

        runCatching {
            val base64Jpeg = encodeBitmapToJpegBase64(bitmap)
            var lastError: Throwable? = null

            for (spec in VISION_MODEL_CHAIN) {
                try {
                    val rawHttpJson = executeVisionRestCall(
                        spec = spec,
                        base64Jpeg = base64Jpeg,
                        prompt = receiptPrompt
                    )
                    val candidateText = ReceiptScanParser.extractCandidateTextFromApiResponse(rawHttpJson)
                        ?: throw IllegalStateException("AI (${spec.modelName}) không trả về văn bản từ ảnh hóa đơn")

                    return@runCatching ReceiptScanParser.parseResponse(
                        rawText = candidateText,
                        savedReceiptPath = savedReceiptPath
                    ).getOrThrow()
                } catch (err: Throwable) {
                    runCatching { Log.w("GeminiVision", "Model ${spec.modelName} failed: ${err.message}") }
                    lastError = err
                    if (err is NonRetryableGeminiApiException) {
                        throw err
                    }
                }
            }

            throw lastError ?: IllegalStateException("Không thể kết nối tới dịch vụ Gemini Vision AI")
        }
    }

    private fun encodeBitmapToJpegBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 78, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }

    private fun executeVisionRestCall(
        spec: VisionModelSpec,
        base64Jpeg: String,
        prompt: String
    ): String {
        val cleanKey = apiKey.trim().removeSurrounding("\"")
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/${spec.modelName}:generateContent?key=$cleanKey"
        val generationConfigJson = JSONObject().apply {
            put("temperature", 0.2)
            put("topK", 32)
            put("topP", 0.95)
            if (spec.supportsThinkingBudgetZero) {
                put("thinkingConfig", JSONObject().put("thinkingBudget", 0))
            }
        }
        val inlineDataJson = JSONObject().apply {
            put("mime_type", "image/jpeg")
            put("data", base64Jpeg)
        }
        val partsArray = JSONArray().apply {
            put(JSONObject().put("inline_data", inlineDataJson))
            put(JSONObject().put("text", prompt))
        }
        val payload = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().put("parts", partsArray)))
            put("generationConfig", generationConfigJson)
        }
        val payloadBytes = payload.toString().toByteArray(StandardCharsets.UTF_8)

        val connection = (URL(endpoint).openConnection() as HttpsURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = spec.readTimeoutMs
            doInput = true
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
        }

        return try {
            connection.outputStream.use { out ->
                out.write(payloadBytes)
                out.flush()
            }
            val statusCode = connection.responseCode
            val responseStream = if (statusCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val bodyText = responseStream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (statusCode !in 200..299) {
                val apiErrorMsg = extractErrorMessage(bodyText).ifBlank { "HTTP $statusCode" }
                if (statusCode in listOf(401, 403) || apiErrorMsg.contains("API key not valid", ignoreCase = true)) {
                    throw NonRetryableGeminiApiException("API Key Gemini không hợp lệ hoặc chưa được cấp quyền Vision ($apiErrorMsg)")
                }
                throw IllegalStateException("Gemini (${spec.modelName}) lỗi $statusCode: $apiErrorMsg")
            }
            bodyText
        } catch (timeout: SocketTimeoutException) {
            throw IllegalStateException("Model ${spec.modelName} phản hồi quá chậm (timeout)", timeout)
        } finally {
            connection.disconnect()
        }
    }

    private fun extractErrorMessage(errorBody: String): String {
        if (errorBody.isBlank()) return ""
        return runCatching {
            JSONObject(errorBody).optJSONObject("error")?.optString("message").orEmpty()
        }.getOrDefault("")
    }

    private class NonRetryableGeminiApiException(message: String) : IllegalStateException(message)

    private companion object {
        val VISION_MODEL_CHAIN = listOf(
            VisionModelSpec("gemini-3.1-flash-lite", supportsThinkingBudgetZero = true, readTimeoutMs = 15_000),
            VisionModelSpec("gemini-3.6-flash", supportsThinkingBudgetZero = true, readTimeoutMs = 22_000),
            VisionModelSpec("gemini-3.5-flash", supportsThinkingBudgetZero = false, readTimeoutMs = 25_000),
            VisionModelSpec("gemini-3.5-flash-lite", supportsThinkingBudgetZero = false, readTimeoutMs = 28_000)
        )
    }

    /**
     * Bóc tách các thẻ Action Card (Expense hoặc Reminder) từ phản hồi của Gemini
     */
    private fun parseGeminiOutput(rawText: String): GeminiResult {
        var cleanText = rawText
        var expenseProposal: ChatExpenseProposal? = null
        var reminderProposal: ChatReminderProposal? = null

        // 1. Tìm và bóc tách thẻ ACTION_EXPENSE
        val expenseRegex = Regex("<<<ACTION_EXPENSE>>>\\s*([\\s\\S]*?)\\s*<<<END_ACTION_EXPENSE>>>")
        val expenseMatch = expenseRegex.find(rawText)
        if (expenseMatch != null) {
            try {
                val jsonString = expenseMatch.groupValues[1]
                val title = extractJsonField(jsonString, "title") ?: "Khoản chi mới"
                val amount = extractJsonField(jsonString, "amount")?.toLongOrNull() ?: 0L
                val categoryStr = extractJsonField(jsonString, "category") ?: "FOOD"
                val categoryName = extractJsonField(jsonString, "categoryName") ?: "Ăn uống"
                val payerName = extractJsonField(jsonString, "payerName") ?: "Lê Văn Hùng"
                val splitSummary = extractJsonField(jsonString, "splitSummary") ?: "Chia đều cho cả nhóm"

                val category = when (categoryStr) {
                    "HOUSING" -> ExpenseCategory.HOUSING
                    "TRANSPORT" -> ExpenseCategory.TRANSPORT
                    "OTHER" -> ExpenseCategory.OTHER
                    else -> ExpenseCategory.FOOD
                }

                if (amount > 0) {
                    val amountFormatted = String.format("%,d đ", amount).replace(",", ".")
                    expenseProposal = ChatExpenseProposal(
                        title = title,
                        amount = amount,
                        amountFormatted = amountFormatted,
                        category = category,
                        categoryName = categoryName,
                        payerName = payerName,
                        splitSummary = splitSummary,
                        isConfirmed = false
                    )
                }
            } catch (_: Exception) {
                // Bỏ qua lỗi parse nếu mô hình trả về cú pháp không chuẩn
            }
            cleanText = cleanText.replace(expenseMatch.value, "").trim()
        }

        // 2. Tìm và bóc tách thẻ ACTION_REMINDER
        val reminderRegex = Regex("<<<ACTION_REMINDER>>>\\s*([\\s\\S]*?)\\s*<<<END_ACTION_REMINDER>>>")
        val reminderMatch = reminderRegex.find(cleanText)
        if (reminderMatch != null) {
            try {
                val jsonString = reminderMatch.groupValues[1]
                val recipientName = extractJsonField(jsonString, "recipientName") ?: "bạn"
                val amountFormatted = extractJsonField(jsonString, "amountFormatted") ?: "tiền quỹ"
                val suggestedMessage = extractJsonField(jsonString, "suggestedMessage") ?: ""

                if (suggestedMessage.isNotBlank()) {
                    reminderProposal = ChatReminderProposal(
                        recipientName = recipientName,
                        amountFormatted = amountFormatted,
                        suggestedMessage = suggestedMessage,
                        isCopied = false
                    )
                }
            } catch (_: Exception) {
                // Bỏ qua nếu lỗi
            }
            cleanText = cleanText.replace(reminderMatch.value, "").trim()
        }

        return GeminiResult(
            cleanText = cleanText,
            expenseProposal = expenseProposal,
            reminderProposal = reminderProposal,
            isFromGeminiCloud = true
        )
    }

    /**
     * Trích xuất giá trị trường trong chuỗi JSON mà không phụ thuộc vào org.json stub của JVM
     */
    private fun extractJsonField(json: String, key: String): String? {
        val stringRegex = Regex("\"$key\"\\s*:\\s*\"([^\"]*)\"")
        val stringMatch = stringRegex.find(json)
        if (stringMatch != null) return stringMatch.groupValues[1]

        val numberRegex = Regex("\"$key\"\\s*:\\s*([0-9]+)")
        val numberMatch = numberRegex.find(json)
        return numberMatch?.groupValues?.get(1)
    }
}
