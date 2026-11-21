package com.example.nhoms8

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.core.designsystem.SpendlyButton
import com.example.nhoms8.core.designsystem.spendlyGlassSurface
import com.example.nhoms8.feature.chatbot.FinSproutAnimatedAvatar
import com.example.nhoms8.model.ChatExpenseProposal
import com.example.nhoms8.model.ChatMessage
import com.example.nhoms8.model.ChatReminderProposal
import com.example.nhoms8.ui.theme.Nhoms8Theme
import com.example.nhoms8.ui.theme.SpendlyIndigo
import com.example.nhoms8.ui.theme.SpendlyLavender
import com.example.nhoms8.ui.theme.SpendlyLavenderDeep
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlyTextStyles
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState

internal val FIN_SPROUT_QUICK_PROMPTS = listOf(
    "🍲 Ăn trưa 180k Hùng trả, chia đều",
    "📊 Tình hình quỹ tháng này thế nào?",
    "⚖️ Ai đang nợ ai trong nhóm?",
    "💌 Nhắc nợ bạn Linh tiền đồ án",
    "💡 Mẹo tiết kiệm chi tiêu cho nhóm"
)

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * 16 · MÀN HÌNH TRỢ LÝ ẢO TÀI CHÍNH FINSPROUT (AI CHATBOT SCREEN)
 * Đồng bộ hoàn toàn theo Spendly Lavender / Organic / Haze 2.1.0 Design System.
 * Tái sử dụng các composable chia sẻ với Popup nổi trên các màn hình chính.
 * ═══════════════════════════════════════════════════════════════════════════════
 */
@Composable
fun ChatbotScreen(
    messages: List<ChatMessage> = emptyList(),
    isTyping: Boolean = false,
    onBackClicked: () -> Unit = {},
    onSendMessage: (String) -> Unit = {},
    onConfirmExpense: (String) -> Unit = {},
    onClearChat: () -> Unit = {},
    activeGroupName: String? = null
) {
    val context = LocalContext.current
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()
    var inputText by remember { mutableStateOf("") }

    OrganicBackground(
        modifier = Modifier.fillMaxSize(),
        hazeState = hazeState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // ====================== 1. THANH TIÊU ĐỀ KÍNH MỜ ======================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    hazeState = hazeState,
                    shape = SpendlyShapeTokens.CardLarge,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(onClick = onBackClicked) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Quay lại",
                                    tint = colors.textPrimary
                                )
                            }

                            FinSproutAvatarBadge(size = 40.dp)

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "FinSprout AI",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(SpendlyShapeTokens.Pill)
                                            .background(colors.lavenderContainer)
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Gemini 2.5 Flash ✨",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.onLavenderContainer
                                        )
                                    }
                                }
                                Text(
                                    text = activeGroupName?.takeIf { it.isNotBlank() }?.let {
                                        "Đang kết nối quỹ: $it"
                                    } ?: "Trực tuyến · Ghi chi tiêu & phân tích quỹ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                onClearChat()
                                Toast.makeText(context, "Đã làm mới cuộc trò chuyện", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Làm mới hội thoại",
                                tint = colors.textMuted
                            )
                        }
                    }
                }
            }

            // ====================== 2. DANH SÁCH TIN NHẮN ======================
            FinSproutMessageList(
                messages = messages,
                isTyping = isTyping,
                onConfirmExpense = onConfirmExpense,
                hazeState = hazeState,
                showIntroCard = true,
                compact = false,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            // ====================== 3. GỢI Ý NHANH ======================
            FinSproutQuickPromptRow(
                prompts = FIN_SPROUT_QUICK_PROMPTS,
                hazeState = hazeState,
                onPromptSelected = { prompt ->
                    onSendMessage(prompt)
                    inputText = ""
                },
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // ====================== 4. KHUNG NHẬP TIN NHẮN ======================
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

/**
 * Avatar động trích xuất từ video của Trợ lý FinSprout AI.
 */
@Composable
internal fun FinSproutAvatarBadge(
    size: Dp = 36.dp,
    showOnlineDot: Boolean = true
) {
    val colors = MaterialTheme.spendlyColors
    Box(contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape((size.value * 0.34f).dp))
                .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape((size.value * 0.34f).dp)),
            contentAlignment = Alignment.Center
        ) {
            FinSproutAnimatedAvatar(
                modifier = Modifier.fillMaxSize()
            )
        }
        if (showOnlineDot) {
            Box(
                modifier = Modifier
                    .size((size.value * 0.27f).coerceAtLeast(9f).dp)
                    .clip(CircleShape)
                    .background(colors.mintDeep)
                    .border(1.5.dp, colors.surface, CircleShape)
            )
        }
    }
}

/**
 * Danh sách tin nhắn hội thoại + Action Cards dùng chung cho cả Full Screen và Mini Popup.
 */
@Composable
internal fun FinSproutMessageList(
    messages: List<ChatMessage>,
    isTyping: Boolean,
    onConfirmExpense: (String) -> Unit,
    hazeState: HazeState?,
    modifier: Modifier = Modifier,
    showIntroCard: Boolean = true,
    compact: Boolean = false
) {
    val listState = rememberLazyListState()
    val horizontalPad = if (compact) 12.dp else 16.dp
    val verticalPad = if (compact) 8.dp else 12.dp
    val itemSpacing = if (compact) 10.dp else 14.dp

    LaunchedEffect(messages.size, isTyping) {
        val total = messages.size + (if (showIntroCard) 1 else 0) + (if (isTyping) 1 else 0)
        if (total > 0) {
            listState.animateScrollToItem(total - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = horizontalPad, vertical = verticalPad),
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        if (showIntroCard) {
            item(key = "intro_card") {
                FinSproutIntroCard(hazeState = hazeState)
            }
        }

        items(messages, key = { it.id }) { message ->
            if (message.isFromUser) {
                UserMessageBubble(message = message)
            } else {
                BotMessageBubble(
                    message = message,
                    onConfirmExpense = onConfirmExpense,
                    hazeState = hazeState,
                    compact = compact
                )
            }
        }

        if (isTyping) {
            item(key = "typing_indicator") {
                BotTypingBubble(hazeState = hazeState)
            }
        }
    }
}

/**
 * Thanh chip gợi ý câu hỏi nhanh cuộn ngang.
 */
@Composable
internal fun FinSproutQuickPromptRow(
    prompts: List<String>,
    hazeState: HazeState?,
    onPromptSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentHorizontalPadding: Dp = 14.dp
) {
    val colors = MaterialTheme.spendlyColors
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = contentHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(prompts) { prompt ->
            Box(
                modifier = Modifier
                    .spendlyGlassSurface(
                        hazeState = hazeState,
                        shape = SpendlyShapeTokens.Pill,
                        tintColor = colors.lavenderContainer.copy(alpha = if (colors.isDark) 0.35f else 0.78f)
                    )
                    .clickable { onPromptSelected(prompt) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(
                    text = prompt,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onLavenderContainer,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Thanh nhập tin nhắn + nút Gửi gradient Lavender dùng chung cho Full Screen và Mini Popup.
 */
@Composable
internal fun FinSproutChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    hazeState: HazeState?,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val colors = MaterialTheme.spendlyColors
    val canSend = inputText.isNotBlank()

    GlassCard(
        modifier = modifier,
        hazeState = hazeState,
        shape = SpendlyShapeTokens.CardLarge,
        contentPadding = PaddingValues(
            horizontal = if (compact) 10.dp else 12.dp,
            vertical = if (compact) 6.dp else 8.dp
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                placeholder = {
                    Text(
                        text = "Hỏi FinSprout hoặc nhập khoản chi...",
                        fontSize = if (compact) 12.5.sp else 13.5.sp,
                        color = colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                shape = SpendlyShapeTokens.Pill,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surface.copy(alpha = if (colors.isDark) 0.55f else 0.72f),
                    unfocusedContainerColor = colors.surface.copy(alpha = if (colors.isDark) 0.40f else 0.52f),
                    focusedBorderColor = colors.primaryLavender,
                    unfocusedBorderColor = colors.divider,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    cursorColor = colors.deepLavender
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (canSend) onSend() }),
                singleLine = true
            )

            Box(
                modifier = Modifier
                    .size(if (compact) 42.dp else 46.dp)
                    .clip(CircleShape)
                    .background(
                        brush = if (canSend) {
                            Brush.linearGradient(listOf(SpendlyLavender, SpendlyIndigo))
                        } else {
                            Brush.linearGradient(
                                listOf(colors.divider, colors.divider)
                            )
                        }
                    )
                    .clickable(enabled = canSend, onClick = onSend),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Gửi tin nhắn",
                    tint = Color.White,
                    modifier = Modifier.size(if (compact) 18.dp else 20.dp)
                )
            }
        }
    }
}

@Composable
private fun FinSproutIntroCard(hazeState: HazeState?) {
    val colors = MaterialTheme.spendlyColors
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        shape = SpendlyShapeTokens.CardMedium,
        tintColor = colors.lavenderContainer.copy(alpha = if (colors.isDark) 0.35f else 0.65f),
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = colors.deepLavender,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Mẹo sử dụng FinSprout AI",
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "• Chat \"Ăn lẩu 650k Hùng trả\" để tạo khoản chi 1-chạm\n" +
                "• Hỏi \"Tình hình quỹ\" hoặc \"Ai đang nợ ai?\" để tra số liệu thật\n" +
                "• Nhờ \"Viết tin nhắn nhắc nợ bạn Linh\" để đòi tiền khéo léo",
            style = MaterialTheme.typography.bodySmall,
            lineHeight = 18.sp,
            color = colors.textMuted
        )
    }
}

@Composable
private fun UserMessageBubble(message: ChatMessage) {
    val colors = MaterialTheme.spendlyColors
    val bubbleShape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 4.dp,
        bottomStart = 18.dp,
        bottomEnd = 18.dp
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.fillMaxWidth(0.84f)
        ) {
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 4.dp,
                        shape = bubbleShape,
                        ambientColor = SpendlyLavenderDeep.copy(alpha = 0.25f),
                        spotColor = SpendlyLavenderDeep.copy(alpha = 0.25f)
                    )
                    .clip(bubbleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(SpendlyLavender, SpendlyLavenderDeep)
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.text,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = message.timestampFormatted,
                fontSize = 10.5.sp,
                color = colors.textMuted
            )
        }
    }
}

@Composable
private fun BotMessageBubble(
    message: ChatMessage,
    onConfirmExpense: (String) -> Unit,
    hazeState: HazeState?,
    compact: Boolean
) {
    val context = LocalContext.current
    val colors = MaterialTheme.spendlyColors
    val bubbleShape = RoundedCornerShape(
        topStart = 4.dp,
        topEnd = 18.dp,
        bottomStart = 18.dp,
        bottomEnd = 18.dp
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        FinSproutAvatarBadge(
            size = if (compact) 26.dp else 30.dp,
            showOnlineDot = false
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(0.90f)
        ) {
            Box(
                modifier = Modifier
                    .spendlyGlassSurface(
                        hazeState = hazeState,
                        shape = bubbleShape,
                        tintColor = colors.glassContainer
                    )
                    .padding(horizontal = 13.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.text,
                    fontSize = if (compact) 13.sp else 13.5.sp,
                    lineHeight = 20.sp,
                    color = colors.textPrimary
                )
            }

            message.expenseProposal?.let { proposal ->
                Spacer(modifier = Modifier.height(8.dp))
                ExpenseActionCard(
                    proposal = proposal,
                    onConfirm = { onConfirmExpense(message.id) },
                    hazeState = hazeState
                )
            }

            message.reminderProposal?.let { reminder ->
                Spacer(modifier = Modifier.height(8.dp))
                ReminderActionCard(
                    reminder = reminder,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Nhắc nợ", reminder.suggestedMessage)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(
                            context,
                            "Đã sao chép tin nhắn nhắc nợ vào bộ nhớ tạm!",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    hazeState = hazeState
                )
            }

            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = message.timestampFormatted,
                fontSize = 10.5.sp,
                color = colors.textMuted,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}

@Composable
private fun ExpenseActionCard(
    proposal: ChatExpenseProposal,
    onConfirm: () -> Unit,
    hazeState: HazeState?
) {
    val colors = MaterialTheme.spendlyColors
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        shape = SpendlyShapeTokens.CardSmall,
        tintColor = if (proposal.isConfirmed) {
            colors.mintContainer.copy(alpha = if (colors.isDark) 0.40f else 0.72f)
        } else {
            colors.glassContainer
        },
        contentPadding = PaddingValues(13.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.lavenderContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ReceiptLong,
                        contentDescription = null,
                        tint = colors.onLavenderContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = proposal.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${proposal.categoryName} · ${proposal.payerName} trả",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = proposal.amountFormatted,
                style = SpendlyTextStyles.moneyMedium,
                color = colors.coralDeep
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Chia: ${proposal.splitSummary}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (proposal.isConfirmed) {
                Row(
                    modifier = Modifier
                        .clip(SpendlyShapeTokens.Pill)
                        .background(colors.mintContainer)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = colors.mintDeep,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Đã lưu vào Quỹ",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.mintDeep
                    )
                }
            } else {
                SpendlyButton(
                    text = "Xác nhận lưu",
                    onClick = onConfirm,
                    height = 36.dp
                )
            }
        }
    }
}

@Composable
private fun ReminderActionCard(
    reminder: ChatReminderProposal,
    onCopy: () -> Unit,
    hazeState: HazeState?
) {
    val colors = MaterialTheme.spendlyColors
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        shape = SpendlyShapeTokens.CardSmall,
        contentPadding = PaddingValues(13.dp)
    ) {
        Text(
            text = "💌 Nhắc bạn ${reminder.recipientName} (${reminder.amountFormatted})",
            style = MaterialTheme.typography.titleSmall,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = colors.lavenderContainer.copy(alpha = if (colors.isDark) 0.25f else 0.50f),
            border = BorderStroke(1.dp, colors.divider),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = reminder.suggestedMessage,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                fontStyle = FontStyle.Italic,
                color = colors.textPrimary,
                modifier = Modifier.padding(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            SpendlyButton(
                text = "Sao chép tin nhắn",
                onClick = onCopy,
                leadingIcon = Icons.Rounded.ContentCopy,
                height = 36.dp
            )
        }
    }
}

@Composable
private fun BotTypingBubble(hazeState: HazeState?) {
    val colors = MaterialTheme.spendlyColors
    val transition = rememberInfiniteTransition(label = "dots")
    val dotAlpha1 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dotAlpha2 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dotAlpha3 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .spendlyGlassSurface(
                    hazeState = hazeState,
                    shape = RoundedCornerShape(16.dp),
                    tintColor = colors.glassContainer
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(7.dp).background(colors.primaryLavender.copy(alpha = dotAlpha1), CircleShape))
                Box(modifier = Modifier.size(7.dp).background(colors.primaryLavender.copy(alpha = dotAlpha2), CircleShape))
                Box(modifier = Modifier.size(7.dp).background(colors.primaryLavender.copy(alpha = dotAlpha3), CircleShape))
            }
        }
    }
}

@Composable
internal fun SproutIconBot(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        val stem = Path().apply {
            moveTo(w * 0.50f, h * 0.88f)
            cubicTo(w * 0.50f, h * 0.65f, w * 0.52f, h * 0.45f, w * 0.50f, h * 0.25f)
        }
        drawPath(path = stem, color = color, style = stroke)

        val leftLeaf = Path().apply {
            moveTo(w * 0.50f, h * 0.55f)
            cubicTo(w * 0.35f, h * 0.52f, w * 0.18f, h * 0.35f, w * 0.32f, h * 0.30f)
            cubicTo(w * 0.45f, h * 0.30f, w * 0.48f, h * 0.45f, w * 0.50f, h * 0.55f)
        }
        drawPath(leftLeaf, color = color, style = stroke)

        val rightLeaf = Path().apply {
            moveTo(w * 0.50f, h * 0.42f)
            cubicTo(w * 0.62f, h * 0.38f, w * 0.82f, h * 0.22f, w * 0.70f, h * 0.18f)
            cubicTo(w * 0.56f, h * 0.16f, w * 0.50f, h * 0.30f, w * 0.50f, h * 0.42f)
        }
        drawPath(rightLeaf, color = color, style = stroke)
    }
}

@Preview(
    name = "16 · Trợ lý AI FinSprout (Spendly Lavender)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun ChatbotScreenPreview() {
    val sampleMessages = remember {
        listOf(
            ChatMessage(
                id = "1",
                text = "Chào bạn! Mình là FinSprout 🌱 - Trợ lý AI của Spendly. Mình có thể giúp bạn ghi nhanh chi tiêu, kiểm tra quỹ nhóm hoặc gợi ý tin nhắn đòi nợ khéo léo.",
                isFromUser = false,
                timestampFormatted = "10:30"
            ),
            ChatMessage(
                id = "2",
                text = "Trưa nay ăn bún bò Huế 180k Hùng trả, chia đều cả nhóm nhé",
                isFromUser = true,
                timestampFormatted = "10:31"
            ),
            ChatMessage(
                id = "3",
                text = "FinSprout đã nhận diện thông tin khoản chi! 🌿 Hãy kiểm tra lại và bấm Xác nhận lưu bên dưới nhé:",
                isFromUser = false,
                timestampFormatted = "10:31",
                expenseProposal = ChatExpenseProposal(
                    title = "Bún bò Huế trưa",
                    amount = 180000L,
                    amountFormatted = "180.000 đ",
                    categoryName = "Ăn uống",
                    payerName = "Lê Văn Hùng",
                    splitSummary = "Chia đều cho 4 thành viên",
                    isConfirmed = false
                )
            )
        )
    }

    Nhoms8Theme {
        ChatbotScreen(
            messages = sampleMessages,
            isTyping = false,
            activeGroupName = "Quỹ Chi Tiêu Nhóm 08"
        )
    }
}

@Preview(
    name = "16b · Trợ lý AI FinSprout (Dark Mode)",
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun ChatbotScreenDarkPreview() {
    SpendlyTheme(darkTheme = true) {
        ChatbotScreen(
            messages = listOf(
                ChatMessage(
                    id = "1",
                    text = "Chào bạn! Mình là FinSprout 🌱. Bạn cần hỗ trợ gì hôm nay?",
                    isFromUser = false,
                    timestampFormatted = "10:30"
                )
            ),
            activeGroupName = "Quỹ Chi Tiêu Nhóm 08"
        )
    }
}
