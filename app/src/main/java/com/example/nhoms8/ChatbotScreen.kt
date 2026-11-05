package com.example.nhoms8

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhoms8.model.ChatExpenseProposal
import com.example.nhoms8.model.ChatMessage
import com.example.nhoms8.model.ChatReminderProposal
import com.example.nhoms8.ui.theme.Nhoms8Theme

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * 16 · MÀN HÌNH TRỢ LÝ ẢO TÀI CHÍNH FINSPROUT (AI CHATBOT SCREEN)
 * Nhóm 08 - Lớp D23CQCI01-N - PTIT
 * ═══════════════════════════════════════════════════════════════════════════════
 * Tính năng chính:
 * 1. Hội thoại thông minh với trợ lý AI FinSprout
 * 2. Bóc tách ngôn ngữ tự nhiên thành khoản chi với Thẻ Hành Động 1-chạm (Action Card)
 * 3. Báo cáo phân tích quỹ nhóm và cảnh báo hạn mức tức thì
 * 4. Soạn thảo tin nhắn nhắc nợ vui vẻ, khéo léo kèm nút Sao chép nhanh
 * 5. Các gợi ý câu hỏi thông minh (Quick Prompt Chips)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(
    messages: List<ChatMessage> = emptyList(),
    isTyping: Boolean = false,
    onBackClicked: () -> Unit = {},
    onSendMessage: (String) -> Unit = {},
    onConfirmExpense: (String) -> Unit = {},
    onClearChat: () -> Unit = {}
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Bảng màu chuẩn thương hiệu SpendTogether
    val backgroundColor = Color(0xFFF6F4EE)
    val forestGreenDark = Color(0xFF14382C)
    val emeraldGreen = Color(0xFF2E8B57)
    val textMuted = Color(0xFF6E7C74)
    val cardBackground = Color(0xFFFFFFFF)
    val borderLight = Color(0xFFE5E2D8)
    val sageLight = Color(0xFFDDEAE0)

    // Tự động cuộn xuống tin nhắn mới nhất
    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Danh sách gợi ý thao tác nhanh
    val quickPrompts = remember {
        listOf(
            "🍲 Ăn trưa 180k Hùng trả, chia đều",
            "📊 Tình hình quỹ tháng này thế nào?",
            "💌 Nhắc nợ bạn Linh 120k tiền lẩu",
            "☕ Cà phê họp nhóm 85k Nam trả",
            "💡 Mẹo tiết kiệm chi tiêu cho nhóm"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ====================== 1. THANH TIÊU ĐỀ (TOP APP BAR) ======================
            Surface(
                color = cardBackground,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Nút quay lại
                        IconButton(onClick = onBackClicked) {
                            BackArrowIcon(
                                color = forestGreenDark,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Avatar FinSprout với chấm xanh trực tuyến
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFE2F0E7)),
                                contentAlignment = Alignment.Center
                            ) {
                                SproutIconBot(
                                    modifier = Modifier.size(24.dp),
                                    color = emeraldGreen
                                )
                            }
                            // Chấm xanh trạng thái trực tuyến
                            Box(
                                modifier = Modifier
                                    .size(11.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                                    .border(2.dp, Color.White, CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "FinSprout AI",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = forestGreenDark
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE2F4E9), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Gemini 1.5 Flash ✨",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = emeraldGreen
                                    )
                                }
                            }
                            Text(
                                text = "Trực tuyến · Sẵn sàng ghi chi tiêu & phân tích quỹ",
                                fontSize = 12.sp,
                                color = textMuted
                            )
                        }
                    }

                    // Nút làm mới / Xóa cuộc trò chuyện
                    IconButton(
                        onClick = {
                            onClearChat()
                            Toast.makeText(context, "Đã làm mới cuộc trò chuyện", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        RefreshIcon(
                            color = textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // ====================== 2. DANH SÁCH TIN NHẮN (MESSAGE LIST) ======================
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Thẻ thông tin giới thiệu trợ lý AI ở đầu trang
                item {
                    FinSproutIntroCard(sageLight = sageLight, forestGreenDark = forestGreenDark)
                }

                // Danh sách tin nhắn hội thoại
                items(messages, key = { it.id }) { message ->
                    if (message.isFromUser) {
                        UserMessageBubble(
                            message = message,
                            bubbleColor = forestGreenDark
                        )
                    } else {
                        BotMessageBubble(
                            message = message,
                            onConfirmExpense = onConfirmExpense,
                            forestGreenDark = forestGreenDark,
                            emeraldGreen = emeraldGreen,
                            cardBackground = cardBackground,
                            borderLight = borderLight
                        )
                    }
                }

                // Hiệu ứng đang soạn tin nhắn của FinSprout
                if (isTyping) {
                    item {
                        BotTypingBubble(cardBackground = cardBackground, borderLight = borderLight)
                    }
                }
            }

            // ====================== 3. THANH GỢI Ý NHANH (QUICK PROMPTS) ======================
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickPrompts) { prompt ->
                    Surface(
                        onClick = {
                            inputText = prompt
                            onSendMessage(prompt)
                            inputText = ""
                        },
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE2DD)),
                        shadowElevation = 1.dp
                    ) {
                        Text(
                            text = prompt,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = forestGreenDark,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // ====================== 4. KHUNG NHẬP TIN NHẮN (INPUT BAR) ======================
            Surface(
                color = cardBackground,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp),
                        placeholder = {
                            Text(
                                text = "Hỏi FinSprout hoặc nhập khoản chi...",
                                fontSize = 13.5.sp,
                                color = Color(0xFFA0ACA4)
                            )
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF9F8F5),
                            unfocusedContainerColor = Color(0xFFF9F8F5),
                            focusedBorderColor = forestGreenDark,
                            unfocusedBorderColor = Color(0xFFE2DFD6),
                            focusedTextColor = forestGreenDark,
                            unfocusedTextColor = forestGreenDark
                        ),
                        singleLine = true
                    )

                    // Nút gửi tin nhắn
                    val canSend = inputText.isNotBlank()
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (canSend) forestGreenDark else Color(0xFFCBD6CF))
                            .clickable(enabled = canSend) {
                                val text = inputText.trim()
                                if (text.isNotEmpty()) {
                                    onSendMessage(text)
                                    inputText = ""
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        SendIcon(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Thẻ chào mừng & hướng dẫn sử dụng nhanh FinSprout AI
 */
@Composable
private fun FinSproutIntroCard(
    sageLight: Color,
    forestGreenDark: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(sageLight.copy(alpha = 0.5f))
            .border(1.dp, Color(0xFFCADBD0), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🌱",
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mẹo sử dụng FinSprout AI",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = forestGreenDark
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "• Chat 'Ăn lẩu 650k Hùng trả' để tạo khoản chi 1-chạm\n" +
                        "• Hỏi 'Tình hình quỹ' để kiểm tra hạn mức và số dư\n" +
                        "• Nhờ 'Viết tin nhắn đòi tiền bạn Linh' để nhắc nợ khéo léo",
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                color = Color(0xFF3C5346)
            )
        }
    }
}

/**
 * Bong bóng tin nhắn của Người Dùng (User Bubble)
 */
@Composable
private fun UserMessageBubble(
    message: ChatMessage,
    bubbleColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.fillMaxWidth(0.82f)
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 4.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 18.dp
                        )
                    )
                    .background(bubbleColor)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = message.timestampFormatted,
                fontSize = 11.sp,
                color = Color(0xFF94A39B)
            )
        }
    }
}

/**
 * Bong bóng tin nhắn của FinSprout AI (Bot Bubble) kèm Action Card
 */
@Composable
private fun BotMessageBubble(
    message: ChatMessage,
    onConfirmExpense: (String) -> Unit,
    forestGreenDark: Color,
    emeraldGreen: Color,
    cardBackground: Color,
    borderLight: Color
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        // Icon đại diện FinSprout nhỏ gọn
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Color(0xFFE2F0E7)),
            contentAlignment = Alignment.Center
        ) {
            SproutIconBot(
                modifier = Modifier.size(17.dp),
                color = emeraldGreen
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(0.88f)
        ) {
            // Phần nội dung tin nhắn văn bản
            Box(
                modifier = Modifier
                    .shadow(1.dp, RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                    .clip(
                        RoundedCornerShape(
                            topStart = 4.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 18.dp
                        )
                    )
                    .background(cardBackground)
                    .border(1.dp, borderLight, RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = forestGreenDark
                )
            }

            // ==================== THẺ HÀNH ĐỘNG GHI CHI TIÊU (EXPENSE ACTION CARD) ====================
            message.expenseProposal?.let { proposal ->
                Spacer(modifier = Modifier.height(8.dp))
                ExpenseActionCard(
                    proposal = proposal,
                    onConfirm = { onConfirmExpense(message.id) },
                    forestGreenDark = forestGreenDark,
                    emeraldGreen = emeraldGreen,
                    cardBackground = cardBackground
                )
            }

            // ==================== THẺ GỢI Ý MẪU NHẮC NỢ (REMINDER ACTION CARD) ====================
            message.reminderProposal?.let { reminder ->
                Spacer(modifier = Modifier.height(8.dp))
                ReminderActionCard(
                    reminder = reminder,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Nhắc nợ", reminder.suggestedMessage)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Đã sao chép tin nhắn nhắc nợ vào bộ nhớ tạm!", Toast.LENGTH_SHORT).show()
                    },
                    forestGreenDark = forestGreenDark,
                    cardBackground = cardBackground
                )
            }

            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = message.timestampFormatted,
                fontSize = 11.sp,
                color = Color(0xFF94A39B),
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}

/**
 * Thẻ hành động 1-chạm xác nhận tạo chi tiêu (Expense Action Card)
 */
@Composable
private fun ExpenseActionCard(
    proposal: ChatExpenseProposal,
    onConfirm: () -> Unit,
    forestGreenDark: Color,
    emeraldGreen: Color,
    cardBackground: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(cardBackground)
            .border(1.5.dp, if (proposal.isConfirmed) Color(0xFF86EFAC) else Color(0xFFC7D7CE), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🧾", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = proposal.title,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = forestGreenDark
                        )
                        Text(
                            text = "${proposal.categoryName} · ${proposal.payerName} trả",
                            fontSize = 12.sp,
                            color = Color(0xFF6E7C74)
                        )
                    }
                }

                Text(
                    text = proposal.amountFormatted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFDC2626)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quy tắc: ${proposal.splitSummary}",
                    fontSize = 11.5.sp,
                    color = Color(0xFF7E8E84),
                    modifier = Modifier.weight(1f)
                )

                if (proposal.isConfirmed) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE2F4E9), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "✓ Đã lưu vào Quỹ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = emeraldGreen
                        )
                    }
                } else {
                    ElevatedButton(
                        onClick = onConfirm,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = forestGreenDark,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Xác nhận lưu",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Thẻ gợi ý tin nhắn đòi nợ tinh tế (Reminder Action Card)
 */
@Composable
private fun ReminderActionCard(
    reminder: ChatReminderProposal,
    onCopy: () -> Unit,
    forestGreenDark: Color,
    cardBackground: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(cardBackground)
            .border(1.dp, Color(0xFFE5D5C5), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "💌", fontSize = 15.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Mẫu tin nhắn nhắc bạn ${reminder.recipientName} (${reminder.amountFormatted})",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = forestGreenDark
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nội dung tin nhắn đóng khung trích dẫn
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFFDF8), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFFF3ECE0), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = reminder.suggestedMessage,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFF4A5568)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ElevatedButton(
                    onClick = onCopy,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color(0xFF2E6349),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "📋 Sao chép tin nhắn",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Bong bóng hiệu ứng 3 chấm động khi FinSprout đang suy nghĩ (Typing Indicator)
 */
@Composable
private fun BotTypingBubble(
    cardBackground: Color,
    borderLight: Color
) {
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
                .clip(RoundedCornerShape(16.dp))
                .background(cardBackground)
                .border(1.dp, borderLight, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(7.dp).background(Color(0xFF2E8B57).copy(alpha = dotAlpha1), CircleShape))
                Box(modifier = Modifier.size(7.dp).background(Color(0xFF2E8B57).copy(alpha = dotAlpha2), CircleShape))
                Box(modifier = Modifier.size(7.dp).background(Color(0xFF2E8B57).copy(alpha = dotAlpha3), CircleShape))
            }
        }
    }
}

// ====================== CÁC BIỂU TƯỢNG VẼ BẰNG CANVAS (VECTOR ICONS) ======================

@Composable
private fun BackArrowIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 2.2.dp.toPx()

        drawLine(
            color = color,
            start = Offset(w * 0.75f, h * 0.5f),
            end = Offset(w * 0.25f, h * 0.5f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.50f, h * 0.25f),
            end = Offset(w * 0.25f, h * 0.50f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.50f, h * 0.75f),
            end = Offset(w * 0.25f, h * 0.50f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun SproutIconBot(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Thân mầm cây vươn lên
        val stem = Path().apply {
            moveTo(w * 0.50f, h * 0.88f)
            cubicTo(w * 0.50f, h * 0.65f, w * 0.52f, h * 0.45f, w * 0.50f, h * 0.25f)
        }
        drawPath(path = stem, color = color, style = stroke)

        // Nhánh lá trái
        val leftLeaf = Path().apply {
            moveTo(w * 0.50f, h * 0.55f)
            cubicTo(w * 0.35f, h * 0.52f, w * 0.18f, h * 0.35f, w * 0.32f, h * 0.30f)
            cubicTo(w * 0.45f, h * 0.30f, w * 0.48f, h * 0.45f, w * 0.50f, h * 0.55f)
        }
        drawPath(leftLeaf, color = color, style = stroke)

        // Nhánh lá phải
        val rightLeaf = Path().apply {
            moveTo(w * 0.50f, h * 0.42f)
            cubicTo(w * 0.62f, h * 0.38f, w * 0.82f, h * 0.22f, w * 0.70f, h * 0.18f)
            cubicTo(w * 0.56f, h * 0.16f, w * 0.50f, h * 0.30f, w * 0.50f, h * 0.42f)
        }
        drawPath(rightLeaf, color = color, style = stroke)
    }
}

@Composable
private fun RefreshIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 2.dp.toPx()

        drawArc(
            color = color,
            startAngle = 45f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(w * 0.2f, h * 0.2f),
            size = Size(w * 0.6f, h * 0.6f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        // Mũi tên cung tròn
        drawLine(
            color = color,
            start = Offset(w * 0.82f, h * 0.30f),
            end = Offset(w * 0.82f, h * 0.50f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.64f, h * 0.50f),
            end = Offset(w * 0.82f, h * 0.50f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun SendIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.22f, h * 0.20f)
            lineTo(w * 0.85f, h * 0.50f)
            lineTo(w * 0.22f, h * 0.80f)
            lineTo(w * 0.38f, h * 0.50f)
            close()
        }
        drawPath(path = path, color = color)
    }
}

// ====================== PREVIEW HIỂN THỊ TRỰC QUAN TRONG ANDROID STUDIO ======================

@Preview(
    name = "16 · Trợ lý AI FinSprout (SpendTogether)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun ChatbotScreenPreview() {
    val sampleMessages = remember {
        listOf(
            ChatMessage(
                id = "1",
                text = "Chào bạn! Mình là FinSprout 🌱 - Trợ lý AI của SpendTogether. Mình có thể giúp bạn ghi nhanh chi tiêu, kiểm tra quỹ nhóm hoặc gợi ý tin nhắn đòi nợ khéo léo. Bạn cần hỗ trợ gì hôm nay?",
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
                    splitSummary = "Chia đều cho 5 thành viên",
                    isConfirmed = false
                )
            ),
            ChatMessage(
                id = "4",
                text = "Viết giúp mình tin nhắn vui vẻ nhắc bạn Linh chuyển 120k tiền lẩu hôm qua nha",
                isFromUser = true,
                timestampFormatted = "10:32"
            ),
            ChatMessage(
                id = "5",
                text = "FinSprout đã soạn sẵn một mẫu nhắc nợ vừa dễ thương vừa tế nhị cho bạn này! Bạn có thể bấm sao chép để gửi ngay qua Zalo/Messenger nhé: 💌",
                isFromUser = false,
                timestampFormatted = "10:32",
                reminderProposal = ChatReminderProposal(
                    recipientName = "Linh",
                    amountFormatted = "120.000 đ",
                    suggestedMessage = "Ting ting! 🧋 Linh ơi, hôm trước nhóm mình ăn lẩu vui ghê. Phần của bạn là 120.000đ nè, khi nào tiện bạn chuyển vào quỹ giúp nhóm chốt sổ nha! Yêu thương ❤️"
                )
            )
        )
    }

    Nhoms8Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ChatbotScreen(
                messages = sampleMessages,
                isTyping = false
            )
        }
    }
}
