package com.example.nhoms8.feature.groups.qr

import java.util.Locale

/**
 * Chuẩn hóa và trích xuất mã mời quỹ 6 ký tự từ các định dạng QR Code khác nhau.
 */
object InviteCodeQrParser {
    const val INVITE_CODE_LENGTH = 6

    private val rawCodeRegex = Regex("^[A-Za-z0-9]{$INVITE_CODE_LENGTH}$")
    private val jsonCodeRegex = Regex(
        """["'](?:inviteCode|code)["']\s*:\s*["']([A-Za-z0-9]{$INVITE_CODE_LENGTH})["']""",
        RegexOption.IGNORE_CASE
    )
    private val urlQueryCodeRegex = Regex(
        """[?&](?:inviteCode|code)=([A-Za-z0-9]{$INVITE_CODE_LENGTH})(?:&|#|$)""",
        RegexOption.IGNORE_CASE
    )
    private val urlJoinPathRegex = Regex(
        """(?:^[a-zA-Z][a-zA-Z0-9+.-]*://[^?\s]*/join/|/join/)([A-Za-z0-9]{$INVITE_CODE_LENGTH})(?:[/?#]|$)""",
        RegexOption.IGNORE_CASE
    )
    private val shareTextRegex = Regex(
        """(?:mã\s*mời|ma\s*moi|invite\s*code)\s*[:\-]\s*([A-Za-z0-9]{$INVITE_CODE_LENGTH})\b""",
        RegexOption.IGNORE_CASE
    )

    /**
     * Trả về mã mời 6 ký tự viết hoa nếu [rawContent] khớp định dạng mã mời Spendly,
     * hoặc `null` nếu đây là mã QR không hợp lệ / thuộc ứng dụng khác.
     */
    fun extractInviteCode(rawContent: String?): String? {
        val trimmed = rawContent?.trim().orEmpty()
        if (trimmed.isEmpty()) return null

        if (rawCodeRegex.matches(trimmed)) {
            return trimmed.uppercase(Locale.ROOT)
        }

        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            jsonCodeRegex.find(trimmed)?.groupValues?.getOrNull(1)?.let { matched ->
                return matched.uppercase(Locale.ROOT)
            }
        }

        urlQueryCodeRegex.find(trimmed)?.groupValues?.getOrNull(1)?.let { matched ->
            return matched.uppercase(Locale.ROOT)
        }

        if (trimmed.contains("://") || trimmed.startsWith("/join/", ignoreCase = true)) {
            urlJoinPathRegex.find(trimmed)?.groupValues?.getOrNull(1)?.let { matched ->
                return matched.uppercase(Locale.ROOT)
            }
            return null
        }

        shareTextRegex.find(trimmed)?.groupValues?.getOrNull(1)?.let { matched ->
            return matched.uppercase(Locale.ROOT)
        }

        return null
    }
}
