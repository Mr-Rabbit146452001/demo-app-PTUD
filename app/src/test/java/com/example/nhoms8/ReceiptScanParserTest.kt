package com.example.nhoms8

import com.example.nhoms8.domain.model.SpendlyCategory
import com.example.nhoms8.feature.receipt.ReceiptConfidence
import com.example.nhoms8.feature.receipt.ReceiptLineItem
import com.example.nhoms8.feature.receipt.ReceiptScanParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ReceiptScanParserTest {

    @Test
    fun `parseResponse parses standard JSON with items and metadata`() {
        val raw = """
            {
              "isReceipt": true,
              "title": "Highlands Coffee - Nguyễn Huệ",
              "totalAmount": 245000,
              "category": "FOOD",
              "date": "21/11/2026",
              "confidence": "HIGH",
              "warning": "",
              "items": [
                {"name": "Trà sen vàng", "quantity": 2, "amount": 98000},
                {"name": "Phin sữa đá", "quantity": 1, "amount": 49000}
              ]
            }
        """.trimIndent()

        val result = ReceiptScanParser.parseResponse(
            rawText = raw,
            savedReceiptPath = "/data/user/0/com.example.nhoms8/files/receipts/receipt_1.jpg"
        ).getOrThrow()

        assertTrue(result.isReceipt)
        assertEquals("Highlands Coffee - Nguyễn Huệ", result.title)
        assertEquals(245_000L, result.totalAmount)
        assertEquals(SpendlyCategory.FOOD, result.category)
        assertEquals(ReceiptConfidence.HIGH, result.confidence)
        assertNull(result.warningMessage)
        assertEquals("/data/user/0/com.example.nhoms8/files/receipts/receipt_1.jpg", result.savedReceiptPath)
        assertEquals(2, result.items.size)
        assertEquals("Trà sen vàng", result.items[0].name)
        assertEquals(2, result.items[0].quantity)
        assertEquals(98_000L, result.items[0].amount)
        assertTrue(result.description.contains("2x Trà sen vàng"))

        val cal = Calendar.getInstance().apply { timeInMillis = result.expenseDateMillis!! }
        assertEquals(2026, cal.get(Calendar.YEAR))
        assertEquals(Calendar.NOVEMBER, cal.get(Calendar.MONTH))
        assertEquals(21, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `parseResponse handles markdown json fence and formatted VND amounts`() {
        val raw = """
            Đây là dữ liệu hóa đơn của bạn:
            ```json
            {
              "isReceipt": true,
              "title": "WinMart Cộng Hòa",
              "totalAmount": "1.250.000 đ",
              "category": "Mua sắm",
              "date": "2026-10-05",
              "confidence": "MEDIUM",
              "items": [
                {"name": "Nước suối Aquafina", "quantity": 1, "amount": "120k"}
              ]
            }
            ```
        """.trimIndent()

        val result = ReceiptScanParser.parseResponse(raw).getOrThrow()
        assertTrue(result.isReceipt)
        assertEquals("WinMart Cộng Hòa", result.title)
        assertEquals(1_250_000L, result.totalAmount)
        assertEquals(SpendlyCategory.SHOPPING, result.category)
        assertEquals(ReceiptConfidence.MEDIUM, result.confidence)
        assertEquals(120_000L, result.items.first().amount)

        val cal = Calendar.getInstance().apply { timeInMillis = result.expenseDateMillis!! }
        assertEquals(2026, cal.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, cal.get(Calendar.MONTH))
        assertEquals(5, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `parseVndAmount parses various Vietnamese currency formats accurately`() {
        assertEquals(245_000L, ReceiptScanParser.parseVndAmount("245.000 đ"))
        assertEquals(1_250_000L, ReceiptScanParser.parseVndAmount("1,250,000 VND"))
        assertEquals(180_000L, ReceiptScanParser.parseVndAmount("180k"))
        assertEquals(1_500_000L, ReceiptScanParser.parseVndAmount("1.5tr"))
        assertEquals(350_000L, ReceiptScanParser.parseVndAmount("350000.00"))
        assertEquals(0L, ReceiptScanParser.parseVndAmount(""))
    }

    @Test
    fun `parseCategory maps all 9 SpendlyCategory values`() {
        assertEquals(SpendlyCategory.FOOD, ReceiptScanParser.parseCategory("FOOD"))
        assertEquals(SpendlyCategory.TRANSPORT, ReceiptScanParser.parseCategory("TRANSPORT"))
        assertEquals(SpendlyCategory.HOUSING, ReceiptScanParser.parseCategory("HOUSING"))
        assertEquals(SpendlyCategory.SHOPPING, ReceiptScanParser.parseCategory("SHOPPING"))
        assertEquals(SpendlyCategory.ENTERTAINMENT, ReceiptScanParser.parseCategory("ENTERTAINMENT"))
        assertEquals(SpendlyCategory.HEALTH, ReceiptScanParser.parseCategory("HEALTH"))
        assertEquals(SpendlyCategory.EDUCATION, ReceiptScanParser.parseCategory("EDUCATION"))
        assertEquals(SpendlyCategory.UTILITIES, ReceiptScanParser.parseCategory("UTILITIES"))
        assertEquals(SpendlyCategory.OTHER, ReceiptScanParser.parseCategory("OTHER"))
    }

    @Test
    fun `parseResponse identifies non-receipt image with friendly warning`() {
        val raw = """
            {
              "isReceipt": false,
              "warning": "Ảnh chụp phong cảnh, không có thông tin hóa đơn thanh toán."
            }
        """.trimIndent()

        val result = ReceiptScanParser.parseResponse(raw).getOrThrow()
        assertFalse(result.isReceipt)
        assertEquals("Ảnh chụp phong cảnh, không có thông tin hóa đơn thanh toán.", result.warningMessage)
        assertEquals(0L, result.totalAmount)
    }

    @Test
    fun `parseResponse returns failure when response has no JSON object`() {
        val result = ReceiptScanParser.parseResponse("Xin lỗi, tôi không nhìn thấy gì cả.")
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun `formatItemsDescription creates readable multi-item summary`() {
        val desc = ReceiptScanParser.formatItemsDescription(
            items = listOf(
                ReceiptLineItem("Lẩu Thái Hải Sản", 1, 350_000L),
                ReceiptLineItem("Coca Cola", 3, 45_000L)
            )
        )
        assertTrue(desc.startsWith("Chi tiết hóa đơn:"))
        assertTrue(desc.contains("Lẩu Thái Hải Sản"))
        assertTrue(desc.contains("3x Coca Cola"))
    }

    @Test
    fun `extractCandidateTextFromApiResponse unescapes Gemini 3 REST response with thoughtSignature`() {
        val rawHttpJson = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      {
                        "text": "```json\n{\"isReceipt\":true,\"title\":\"Phở Thìn Bờ Hồ\",\"totalAmount\":150000,\"category\":\"FOOD\",\"date\":\"21/11/2026\",\"confidence\":\"HIGH\",\"warning\":\"\",\"items\":[{\"name\":\"Phở tái lăn\",\"quantity\":2,\"amount\":150000}]}\n```",
                        "thoughtSignature": "EqEBCp4BAWkUfRPu8FhcoeCQz4vC1Ucq7Zc0ZqLxcFj2"
                      }
                    ],
                    "role": "model"
                  },
                  "finishReason": "STOP",
                  "index": 0
                }
              ],
              "usageMetadata": {
                "promptTokenCount": 300,
                "candidatesTokenCount": 55,
                "thoughtsTokenCount": 0,
                "totalTokenCount": 355
              },
              "modelVersion": "gemini-3.1-flash-lite"
            }
        """.trimIndent()

        val extracted = ReceiptScanParser.extractCandidateTextFromApiResponse(rawHttpJson)
        assertNotNull(extracted)
        val parsed = ReceiptScanParser.parseResponse(
            rawText = extracted!!,
            savedReceiptPath = "/data/user/0/com.example.nhoms8/files/receipts/test.jpg"
        ).getOrThrow()

        assertTrue(parsed.isReceipt)
        assertEquals("Phở Thìn Bờ Hồ", parsed.title)
        assertEquals(150_000L, parsed.totalAmount)
        assertEquals(SpendlyCategory.FOOD, parsed.category)
        assertEquals(ReceiptConfidence.HIGH, parsed.confidence)
        assertEquals(1, parsed.items.size)
        assertEquals("Phở tái lăn", parsed.items.first().name)
    }

    @Test
    fun `parseResponse allows blurry receipt with zero amount to enter Review with LOW confidence`() {
        val raw = """
            {
              "isReceipt": true,
              "title": "",
              "totalAmount": 0,
              "category": "OTHER",
              "confidence": "LOW"
            }
        """.trimIndent()

        val parsed = ReceiptScanParser.parseResponse(raw).getOrThrow()
        assertTrue(parsed.isReceipt)
        assertEquals("Chi tiêu từ hóa đơn", parsed.title)
        assertEquals(0L, parsed.totalAmount)
        assertEquals(ReceiptConfidence.LOW, parsed.confidence)
        assertNotNull(parsed.warningMessage)
        assertTrue(parsed.warningMessage!!.contains("tổng tiền"))
    }
}
