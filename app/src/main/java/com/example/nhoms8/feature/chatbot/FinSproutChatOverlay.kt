package com.example.nhoms8.feature.chatbot

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhoms8.FIN_SPROUT_QUICK_PROMPTS
import com.example.nhoms8.FinSproutAvatarBadge
import com.example.nhoms8.FinSproutChatInputBar
import com.example.nhoms8.FinSproutMessageList
import com.example.nhoms8.FinSproutQuickPromptRow
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.core.designsystem.spendlyGlassSurface
import com.example.nhoms8.model.ChatMessage
import com.example.nhoms8.ui.theme.SpendlyIndigo
import com.example.nhoms8.ui.theme.SpendlyLavenderDeep
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.HazeState

/**
 * Bong bóng nổi FinSprout AI hiển thị ở góc dưới-phải trên các màn hình chính & Chi tiết quỹ.
 */
@Composable
fun FinSproutFloatingBubble(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    Box(
        modifier = modifier.size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    ambientColor = SpendlyLavenderDeep.copy(alpha = 0.45f),
                    spotColor = SpendlyIndigo.copy(alpha = 0.45f)
                )
                .clip(CircleShape)
                .border(
                    width = 1.5.dp,
                    color = Color.White.copy(alpha = 0.65f),
                    shape = CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            FinSproutAnimatedAvatar(modifier = Modifier.fillMaxSize())
        }

        // Chấm xanh trực tuyến góc trên-phải của bong bóng
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 3.dp, end = 3.dp)
                .size(12.dp)
                .clip(CircleShape)
                .background(colors.mintDeep)
                .border(2.dp, colors.surface, CircleShape)
        )
    }
}

/**
 * Popup nhỏ FinSprout AI kính mờ Haze 2.1.0 hiển thị ngay trên màn hình hiện tại,
 * có nút Phóng to ra toàn màn hình, Làm mới và Thu gọn.
 */
@Composable
fun FinSproutChatPopup(
    visible: Boolean,
    messages: List<ChatMessage>,
    isTyping: Boolean,
    activeGroupName: String?,
    hazeState: HazeState?,
    onDismiss: () -> Unit,
    onExpandFullScreen: () -> Unit,
    onSendMessage: (String) -> Unit,
    onConfirmExpense: (String) -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = MaterialTheme.spendlyColors
    var inputText by remember { mutableStateOf("") }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = if (colors.isDark) 0.48f else 0.24f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 14.dp, vertical = 18.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + scaleIn(initialScale = 0.92f) + slideInVertically { it / 4 },
                exit = fadeOut() + scaleOut(targetScale = 0.92f) + slideOutVertically { it / 4 }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.96f)
                        .widthIn(max = 410.dp)
                        .fillMaxHeight(0.64f)
                        .shadow(
                            elevation = 20.dp,
                            shape = SpendlyShapeTokens.HeroCard,
                            ambientColor = SpendlyLavenderDeep.copy(alpha = 0.35f),
                            spotColor = SpendlyIndigo.copy(alpha = 0.35f)
                        )
                        .spendlyGlassSurface(
                            hazeState = hazeState,
                            shape = SpendlyShapeTokens.HeroCard,
                            tintColor = colors.surface.copy(alpha = if (colors.isDark) 0.88f else 0.86f)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {} // Chặn click xuyên xuống backdrop
                        )
                ) {
                    // ---------- HEADER POPUP ----------
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 6.dp, top = 12.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            FinSproutAvatarBadge(size = 34.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "FinSprout AI",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(SpendlyShapeTokens.Pill)
                                            .background(colors.lavenderContainer)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "2.5 Flash ✨",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.onLavenderContainer
                                        )
                                    }
                                }
                                Text(
                                    text = activeGroupName?.takeIf { it.isNotBlank() }?.let {
                                        "Quỹ: $it"
                                    } ?: "Trợ lý tài chính Spendly",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    onClearChat()
                                    Toast.makeText(context, "Đã làm mới hội thoại", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Làm mới",
                                    tint = colors.textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = onExpandFullScreen,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.OpenInFull,
                                    contentDescription = "Phóng to toàn màn hình",
                                    tint = colors.deepLavender,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Thu gọn popup",
                                    tint = colors.textMuted,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = colors.divider.copy(alpha = 0.65f))

                    // ---------- DANH SÁCH TIN NHẮN GỌN ----------
                    FinSproutMessageList(
                        messages = messages,
                        isTyping = isTyping,
                        onConfirmExpense = onConfirmExpense,
                        hazeState = hazeState,
                        showIntroCard = messages.size <= 1,
                        compact = true,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )

                    // ---------- GỢI Ý NHANH ----------
                    FinSproutQuickPromptRow(
                        prompts = FIN_SPROUT_QUICK_PROMPTS,
                        hazeState = hazeState,
                        onPromptSelected = { prompt ->
                            onSendMessage(prompt)
                            inputText = ""
                        },
                        contentHorizontalPadding = 12.dp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // ---------- THANH NHẬP TIN NHẮN ----------
                    FinSproutChatInputBar(
                        inputText = inputText,
                        onInputChange = { inputText = it },
                        onSend = {
                            val trimmed = inputText.trim()
                            if (trimmed.isNotEmpty()) {
                                onSendMessage(trimmed)
                                inputText = ""
                            }
                        },
                        hazeState = hazeState,
                        compact = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 820)
@Composable
private fun FinSproutChatPopupLightPreview() {
    SpendlyTheme {
        OrganicBackground(modifier = Modifier.fillMaxSize()) {
            FinSproutChatPopup(
                visible = true,
                messages = listOf(
                    ChatMessage(
                        id = "1",
                        text = "Chào bạn! Mình là FinSprout 🌱. Bạn cần ghi chi tiêu hay kiểm tra quỹ hôm nay?",
                        isFromUser = false,
                        timestampFormatted = "Vừa xong"
                    )
                ),
                isTyping = false,
                activeGroupName = "Quỹ Chi Tiêu Nhóm 08",
                hazeState = null,
                onDismiss = {},
                onExpandFullScreen = {},
                onSendMessage = {},
                onConfirmExpense = {},
                onClearChat = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 820)
@Composable
private fun FinSproutChatPopupDarkPreview() {
    SpendlyTheme(darkTheme = true) {
        OrganicBackground(modifier = Modifier.fillMaxSize()) {
            FinSproutChatPopup(
                visible = true,
                messages = listOf(
                    ChatMessage(
                        id = "1",
                        text = "Chào bạn! Mình là FinSprout 🌱. Bạn cần ghi chi tiêu hay kiểm tra quỹ hôm nay?",
                        isFromUser = false,
                        timestampFormatted = "Vừa xong"
                    )
                ),
                isTyping = false,
                activeGroupName = "Quỹ Chi Tiêu Nhóm 08",
                hazeState = null,
                onDismiss = {},
                onExpandFullScreen = {},
                onSendMessage = {},
                onConfirmExpense = {},
                onClearChat = {}
            )
        }
    }
}
