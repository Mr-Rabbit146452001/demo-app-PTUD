package com.example.nhoms8.data

import com.example.nhoms8.BuildConfig
import com.example.nhoms8.ExpenseCategory
import com.example.nhoms8.model.ChatExpenseProposal
import com.example.nhoms8.model.ChatReminderProposal
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
 * Kho xử lý Trợ lý ảo Google Gemini 1.5 Flash (GeminiChatRepository)
 * Phụ trách bởi Kỹ sư Thuật toán & Dữ liệu - Nhóm 08 (SpendTogether)
 */
class GeminiChatRepository(
    private val apiKey: String = BuildConfig.GEMINI_API_KEY
) {

    private val systemInstructionText = """
        Bạn là FinSprout 🌱 - Trợ lý AI tài chính cá nhân & nhóm thông minh của ứng dụng SpendTogether (PTIT Nhóm 08).
        Tính cách: Thân thiện, vui vẻ, năng động, am hiểu tài chính, luôn xưng 'FinSprout' hoặc 'mình', gọi người dùng là 'bạn'.
        Luôn sử dụng biểu tượng cảm xúc phù hợp: 🌱, 💰, 📊, 💌, ✨.

        Nhiệm vụ 1: GHI NHẬN KHOẢN CHI TỰ NHIÊN
        Khi người dùng nói về một khoản chi (ví dụ: 'Ăn lẩu 650k Hùng trả', 'Mua trà sữa 90k', 'Tiền mạng 330k Nam trả'):
        Hãy gửi phản hồi vui vẻ xác nhận thông tin và LUÔN đính kèm khối thẻ hành động ở cuối câu trả lời theo đúng định dạng sau:
        <<<ACTION_EXPENSE>>>
        {"title":"Ăn lẩu nhóm","amount":650000,"category":"FOOD","categoryName":"Ăn uống","payerName":"Lê Văn Hùng","splitSummary":"Chia đều cho cả nhóm"}
        <<<END_ACTION_EXPENSE>>>
        (Các danh mục hợp lệ: FOOD - Ăn uống, HOUSING - Nhà ở & Tiện ích, TRANSPORT - Đi lại & Di chuyển, OTHER - Chi tiêu khác).

        Nhiệm vụ 2: SOẠN MẪU TIN NHẮN NHẮC NỢ
        Khi người dùng nhờ nhắc nợ / đòi nợ ai đó (ví dụ: 'Viết tin nhắn nhắc Linh chuyển 120k tiền lẩu'):
        Hãy viết một tin nhắn dễ thương, lịch sự, không làm mất lòng bạn bè và LUÔN đính kèm khối:
        <<<ACTION_REMINDER>>>
        {"recipientName":"Linh","amountFormatted":"120.000 đ","suggestedMessage":"Ting ting! 🧋 Linh ơi, hôm trước nhóm mình ăn lẩu vui quá. Phần của bạn là 120.000đ nè, khi nào tiện bạn chuyển khoản vào quỹ giúp nhóm nhé! Cảm ơn bạn nhiều ❤️"}
        <<<END_ACTION_REMINDER>>>

        Nhiệm vụ 3: PHÂN TÍCH TÀI CHÍNH & MẸO TIẾT KIỆM
        Dựa vào số liệu trong [NGỮ CẢNH QUỸ NHÓM HIỆN TẠI] mà hệ thống cung cấp, trả lời ngắn gọn, rõ ràng, đưa ra đánh giá thực tế và lời khuyên tiết kiệm hữu ích.
    """.trimIndent()

    private val generativeModel: GenerativeModel? by lazy {
        if (isApiKeyConfigured()) {
            try {
                GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = apiKey,
                    systemInstruction = content { text(systemInstructionText) },
                    generationConfig = generationConfig {
                        temperature = 0.7f
                        topK = 40
                        topP = 0.95f
                    }
                )
            } catch (_: Throwable) {
                null
            }
        } else {
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
     * Gửi tin nhắn tới Gemini 1.5 Flash kèm ngữ cảnh quỹ nhóm
     */
    suspend fun sendMessage(
        userMessage: String,
        groupContextPrompt: String
    ): GeminiResult = withContext(Dispatchers.IO) {
        val model = generativeModel
        if (model == null) {
            throw IllegalStateException("Chưa cấu hình GEMINI_API_KEY trong local.properties")
        }

        val fullPrompt = """
            [NGỮ CẢNH QUỸ NHÓM HIỆN TẠI]
            $groupContextPrompt
            
            [TIN NHẮN NGƯỜI DÙNG]
            $userMessage
        """.trimIndent()

        val response = model.generateContent(fullPrompt)
        val rawText = response.text ?: "FinSprout đã lắng nghe bạn 🌱"

        parseGeminiOutput(rawText)
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
