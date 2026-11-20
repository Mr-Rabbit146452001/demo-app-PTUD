package com.example.nhoms8

import com.example.nhoms8.feature.groups.qr.InviteCodeQrParser
import com.example.nhoms8.feature.groups.qr.QrCodeDecoder
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InviteCodeQrTest {

    @Test
    fun `extractInviteCode handles raw 6-char code and normalizes case and spaces`() {
        assertEquals("SP8K2M", InviteCodeQrParser.extractInviteCode("SP8K2M"))
        assertEquals("N08PRO", InviteCodeQrParser.extractInviteCode("  n08pro \n"))
        assertEquals("DL2026", InviteCodeQrParser.extractInviteCode("\tdl2026 "))
    }

    @Test
    fun `extractInviteCode handles spendly deep link and https urls`() {
        assertEquals(
            "SP8K2M",
            InviteCodeQrParser.extractInviteCode("spendly://join?code=sp8k2m")
        )
        assertEquals(
            "DL2026",
            InviteCodeQrParser.extractInviteCode("https://spendly.app/invite?foo=1&inviteCode=dl2026#top")
        )
        assertEquals(
            "AB12CD",
            InviteCodeQrParser.extractInviteCode("https://spendly.app/join/ab12cd")
        )
        assertEquals(
            "ZX9Q4M",
            InviteCodeQrParser.extractInviteCode("spendly://group/join/zx9q4m")
        )
    }

    @Test
    fun `extractInviteCode handles vietnamese share text and json payloads`() {
        val shareText = "Tham gia quỹ \"Đà Lạt 2026\" trên Spendly bằng mã mời: SP8K2M"
        assertEquals("SP8K2M", InviteCodeQrParser.extractInviteCode(shareText))
        assertEquals("A1B2C3", InviteCodeQrParser.extractInviteCode("Invite Code: a1b2c3"))
        assertEquals("SP8K2M", InviteCodeQrParser.extractInviteCode("""{"inviteCode":"SP8K2M"}"""))
        assertEquals("K9M2P4", InviteCodeQrParser.extractInviteCode("""{ "code": "k9m2p4" }"""))
    }

    @Test
    fun `extractInviteCode rejects blank invalid length wifi vietqr and unrelated urls`() {
        assertNull(InviteCodeQrParser.extractInviteCode(null))
        assertNull(InviteCodeQrParser.extractInviteCode(""))
        assertNull(InviteCodeQrParser.extractInviteCode("   "))
        assertNull(InviteCodeQrParser.extractInviteCode("AB12C"))
        assertNull(InviteCodeQrParser.extractInviteCode("ABCD123"))
        assertNull(InviteCodeQrParser.extractInviteCode("WIFI:S:Home;T:WPA;P:123456;;"))
        assertNull(
            InviteCodeQrParser.extractInviteCode(
                "00020101021138570010A000000727012700069704360113101234567890208QRIBFTTA53037045802VN6304ABCD"
            )
        )
        assertNull(InviteCodeQrParser.extractInviteCode("https://example.com/abcdef"))
        assertNull(InviteCodeQrParser.extractInviteCode("https://bank.vn/pay?id=123456"))
    }

    @Test
    fun `qr encode then decode argb pixels roundtrips invite code and deep link`() {
        val rawPixels = encodeQrToArgbPixels("N08PRO", size = 256)
        val decodedRaw = QrCodeDecoder.decodeFromArgbPixels(rawPixels, 256, 256)
        assertEquals("N08PRO", InviteCodeQrParser.extractInviteCode(decodedRaw))

        val urlPixels = encodeQrToArgbPixels("spendly://join?code=SP8K2M", size = 320)
        val decodedUrl = QrCodeDecoder.decodeFromArgbPixels(urlPixels, 320, 320)
        assertEquals("SP8K2M", InviteCodeQrParser.extractInviteCode(decodedUrl))
    }

    @Test
    fun `qr encode then decode luminance bytes handles exact width and padded rowStride`() {
        val tightY = encodeQrToLuminanceBytes("SP8K2M", width = 320, height = 320, rowStride = 320)
        val decodedTight = QrCodeDecoder.decodeFromLuminanceBytes(
            yData = tightY,
            width = 320,
            height = 320,
            rowStride = 320
        )
        assertEquals("SP8K2M", InviteCodeQrParser.extractInviteCode(decodedTight))

        val paddedY = encodeQrToLuminanceBytes("DL2026", width = 320, height = 320, rowStride = 384)
        val decodedPadded = QrCodeDecoder.decodeFromLuminanceBytes(
            yData = paddedY,
            width = 320,
            height = 320,
            rowStride = 384
        )
        assertEquals("DL2026", InviteCodeQrParser.extractInviteCode(decodedPadded))
    }

    @Test
    fun `decoder returns null safely on invalid dimensions or blank images`() {
        assertNull(QrCodeDecoder.decodeFromArgbPixels(IntArray(0), 0, 0))
        assertNull(QrCodeDecoder.decodeFromArgbPixels(IntArray(100), 20, 20))
        assertNull(QrCodeDecoder.decodeFromLuminanceBytes(ByteArray(100), 320, 320, rowStride = 320))
        assertNull(QrCodeDecoder.decodeFromLuminanceBytes(ByteArray(320 * 320), 320, 320, rowStride = 300))

        val blankWhitePixels = IntArray(128 * 128) { -1 }
        assertNull(QrCodeDecoder.decodeFromArgbPixels(blankWhitePixels, 128, 128))
    }

    private fun encodeQrToArgbPixels(content: String, size: Int): IntArray {
        val matrix = QRCodeWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            size,
            size,
            mapOf(
                EncodeHintType.MARGIN to 2,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )
        )
        val pixels = IntArray(size * size)
        for (y in 0 until size) {
            val rowOffset = y * size
            for (x in 0 until size) {
                pixels[rowOffset + x] = if (matrix.get(x, y)) 0xFF1A1625.toInt() else 0xFFFFFFFF.toInt()
            }
        }
        return pixels
    }

    private fun encodeQrToLuminanceBytes(
        content: String,
        width: Int,
        height: Int,
        rowStride: Int
    ): ByteArray {
        val matrix = QRCodeWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            width,
            height,
            mapOf(
                EncodeHintType.MARGIN to 2,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )
        )
        val buffer = ByteArray(height * rowStride) { 0x7F.toByte() }
        for (y in 0 until height) {
            val rowOffset = y * rowStride
            for (x in 0 until width) {
                buffer[rowOffset + x] = if (matrix.get(x, y)) 0x10.toByte() else 0xF5.toByte()
            }
        }
        return buffer
    }
}
