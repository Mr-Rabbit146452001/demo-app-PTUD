package com.example.nhoms8

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhoms8.algorithm.FintechAlgorithm
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.GradientCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.ui.theme.Nhoms8Theme
import com.example.nhoms8.ui.theme.SpendlyIndigo
import com.example.nhoms8.ui.theme.SpendlyLavender
import com.example.nhoms8.ui.theme.SpendlyLavenderDeep
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlyTextStyles
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState

/**
 * Dữ liệu thể hiện một khoản nợ giữa 2 thành viên
 */
data class DebtItem(
    val id: String,
    val debtorName: String,
    val debtorInitials: String,
    val debtorAvatarBg: Color,
    val creditorName: String,
    val creditorInitials: String,
    val creditorAvatarBg: Color,
    val rawAmount: Long,
    val amountFormatted: String,
    val statusText: String,
    val isPaid: Boolean
)

/**
 * 12 · Màn hình Quyết toán và nhắc nợ (Settle Up & Debt Reminder Screen)
 * Đồng bộ hệ thống thiết kế Spendly Lavender / Organic / Haze 2.1.0:
 * - Nền pastel hữu cơ OrganicBackground(hazeState)
 * - Thanh tiêu đề kính mờ: Nút Back, "Ai nợ ai?", Nút lịch sử quyết toán
 * - Thẻ tổng quan quyết toán GradientCard Lavender-Indigo-Sky
 * - Danh sách thẻ khoản nợ GlassCard với luồng chuyển tiền & Nút Trả nợ / Đã trả
 * - Thẻ cài đặt Nhắc nợ tự động với công tắc Toggle Switch tương tác mượt mà
 */
@Composable
fun SettlementScreen(
    groupTitle: String = "Ai nợ ai?",
    groupSubtitle: String = "Quyết toán Quỹ Chi Tiêu Nhóm 08",
    debtsList: List<DebtItem>? = null,
    autoReminder: Boolean = true,
    onToggleAutoReminder: (Boolean) -> Unit = {},
    onBackClicked: () -> Unit = {},
    onHistoryClicked: () -> Unit = {},
    onSettleDebtClicked: (DebtItem) -> Unit = {}
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()

    // Trạng thái bật/tắt nhắc nợ tự động
    var autoReminderEnabled by remember(autoReminder) { mutableStateOf(autoReminder) }

    // Danh sách khoản nợ thực tế giữa các thành viên Nhóm 08
    val defaultDebtItems = remember {
        mutableStateListOf(
            DebtItem(
                id = "debt_1",
                debtorName = "Nguyễn Phan Tấn Đạt",
                debtorInitials = "TĐ",
                debtorAvatarBg = Color(0xFFEDE6FA),
                creditorName = "Lê Văn Hùng",
                creditorInitials = "VH",
                creditorAvatarBg = Color(0xFFE1F5EE),
                rawAmount = 232500L,
                amountFormatted = "232.500 đ",
                statusText = "Chờ chuyển khoản",
                isPaid = false
            ),
            DebtItem(
                id = "debt_2",
                debtorName = "Lê Trung Bình",
                debtorInitials = "TB",
                debtorAvatarBg = Color(0xFFD6ECF9),
                creditorName = "Lê Văn Hùng",
                creditorInitials = "VH",
                creditorAvatarBg = Color(0xFFE1F5EE),
                rawAmount = 192500L,
                amountFormatted = "192.500 đ",
                statusText = "Chờ chuyển khoản",
                isPaid = false
            ),
            DebtItem(
                id = "debt_3",
                debtorName = "Lý Việt Linh",
                debtorInitials = "VL",
                debtorAvatarBg = Color(0xFFFDF1DC),
                creditorName = "Lê Văn Hùng",
                creditorInitials = "VH",
                creditorAvatarBg = Color(0xFFE1F5EE),
                rawAmount = 47500L,
                amountFormatted = "47.500 đ",
                statusText = "Đã thanh toán",
                isPaid = true
            )
        )
    }

    // Tính toán số lượng và tổng tiền các khoản cần quyết toán
    val currentDebts = debtsList ?: defaultDebtItems
    val pendingDebts = currentDebts.filter { !it.isPaid }
    val pendingCount = pendingDebts.size
    val totalPendingAmount = pendingDebts.sumOf { it.rawAmount }
    val formattedTotalPending = FintechAlgorithm.formatVnd(totalPendingAmount)

    OrganicBackground(
        modifier = Modifier.fillMaxSize(),
        hazeState = hazeState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // ====================== 1. TOP BAR ======================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    GlassCard(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable { onBackClicked() },
                        hazeState = hazeState,
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            BackArrowIcon(
                                color = colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = groupTitle,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = groupSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                GlassCard(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable { onHistoryClicked() },
                    hazeState = hazeState,
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        HistoryClockIcon(
                            color = colors.deepLavender,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ====================== 2. THẺ TỔNG QUAN QUYẾT TOÁN ======================
            GradientCard(
                modifier = Modifier.fillMaxWidth(),
                shape = SpendlyShapeTokens.CardLarge,
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "CẦN QUYẾT TOÁN",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f),
                            letterSpacing = 0.8.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (pendingCount > 0) formattedTotalPending else "0 đ",
                            style = SpendlyTextStyles.moneyHero,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color.White.copy(alpha = 0.18f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = pendingCount.toString(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "khoản",
                                fontSize = 10.5.sp,
                                color = Color.White.copy(alpha = 0.88f),
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ====================== 3. DANH SÁCH THẺ KHOẢN NỢ ======================
            if (currentDebts.isEmpty()) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    hazeState = hazeState,
                    shape = SpendlyShapeTokens.CardLarge,
                    contentPadding = PaddingValues(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tuyệt vời! Không có khoản nợ nào",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tất cả thành viên trong nhóm đều đã sòng phẳng.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    currentDebts.forEachIndexed { index, item ->
                        DebtSettlementCard(
                            item = item,
                            hazeState = hazeState,
                            onSettleClicked = {
                                if (debtsList != null) {
                                    onSettleDebtClicked(item)
                                } else {
                                    defaultDebtItems[index] = item.copy(
                                        isPaid = true,
                                        statusText = "Đã thanh toán vừa xong"
                                    )
                                    onSettleDebtClicked(defaultDebtItems[index])
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ====================== 4. THẺ CÀI ĐẶT NHẮC NỢ TỰ ĐỘNG ======================
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SpendlyShapeTokens.CardLarge)
                    .clickable {
                        autoReminderEnabled = !autoReminderEnabled
                        onToggleAutoReminder(autoReminderEnabled)
                    },
                hazeState = hazeState,
                shape = SpendlyShapeTokens.CardLarge,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
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
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(colors.lavenderContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            BellRingingIcon(
                                color = colors.deepLavender,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Nhắc nợ tự động",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Gửi lời nhắc trước hạn 1 ngày",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    CustomToggleSwitch(
                        checked = autoReminderEnabled,
                        onCheckedChange = {
                            autoReminderEnabled = it
                            onToggleAutoReminder(it)
                        },
                        activeTrackColor = colors.deepLavender,
                        inactiveTrackColor = colors.divider
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

/**
 * Thẻ hiển thị một khoản nợ giữa 2 thành viên theo phong cách Spendly GlassCard.
 */
@Composable
private fun DebtSettlementCard(
    item: DebtItem,
    hazeState: HazeState,
    onSettleClicked: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val avatarTextColor = Color(0xFF302B3D)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        shape = SpendlyShapeTokens.CardLarge,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp)
    ) {
        // ================= HÀNG TRÊN: LUỒNG NỢ (Người nợ -> Số tiền -> Người nhận) =================
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Bên trái: Người nợ
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(80.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(item.debtorAvatarBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.debtorInitials,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = avatarTextColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.debtorName,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Ở giữa: Số tiền và mũi tên luồng nợ
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.amountFormatted,
                    style = SpendlyTextStyles.moneyMedium,
                    color = if (item.isPaid) colors.textMuted else colors.coralDeep
                )

                Spacer(modifier = Modifier.height(4.dp))

                RightDirectionArrowIcon(
                    color = if (item.isPaid) colors.divider else colors.primaryLavender,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Bên phải: Người nhận
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(80.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(item.creditorAvatarBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.creditorInitials,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = avatarTextColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.creditorName,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ================= HÀNG DƯỚI: TRẠNG THÁI & NÚT HÀNH ĐỘNG =================
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (item.isPaid) {
                    CheckCircleSmallIcon(
                        color = colors.mintDeep,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    ClockSmallOutlineIcon(
                        color = colors.amberDeep,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = item.statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (item.isPaid) {
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .clip(CircleShape)
                        .background(colors.mintContainer)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Đã trả",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.mintDeep
                    )
                }
            } else {
                val ctaGradient = Brush.horizontalGradient(
                    colors = listOf(SpendlyLavender, SpendlyLavenderDeep, SpendlyIndigo)
                )
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = CircleShape,
                            ambientColor = SpendlyLavenderDeep.copy(alpha = 0.28f),
                            spotColor = SpendlyLavenderDeep.copy(alpha = 0.28f)
                        )
                        .clip(CircleShape)
                        .background(ctaGradient)
                        .clickable { onSettleClicked() }
                        .padding(horizontal = 22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Trả nợ",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Công tắc Toggle Switch tùy biến phong cách Material 3 / Spendly Lavender.
 */
@Composable
private fun CustomToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    activeTrackColor: Color,
    inactiveTrackColor: Color
) {
    val trackWidth = 50.dp
    val trackHeight = 28.dp
    val thumbSize = 22.dp
    val thumbPadding = 3.dp

    val trackColor by animateColorAsState(
        targetValue = if (checked) activeTrackColor else inactiveTrackColor,
        animationSpec = tween(durationMillis = 200),
        label = "switch_track_color"
    )

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize - thumbPadding else thumbPadding,
        animationSpec = tween(durationMillis = 200),
        label = "switch_thumb_offset"
    )

    Box(
        modifier = Modifier
            .size(width = trackWidth, height = trackHeight)
            .clip(CircleShape)
            .background(trackColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(thumbSize)
                .shadow(elevation = 2.dp, shape = CircleShape)
                .background(Color.White, CircleShape)
        )
    }
}

// =========================================================================
// CUSTOM CANVAS ICONS (Vẽ tự thân 100%, không phụ thuộc tài nguyên XML ngoài)
// =========================================================================

@Composable
private fun BackArrowIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 2.dp.toPx()

        drawLine(
            color = color,
            start = Offset(w * 0.82f, h * 0.5f),
            end = Offset(w * 0.18f, h * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(w * 0.45f, h * 0.24f),
            end = Offset(w * 0.18f, h * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(w * 0.45f, h * 0.76f),
            end = Offset(w * 0.18f, h * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun HistoryClockIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.8.dp.toPx()

        val circlePath = Path().apply {
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    left = w * 0.18f,
                    top = h * 0.18f,
                    right = w * 0.82f,
                    bottom = h * 0.82f
                ),
                startAngleDegrees = -110f,
                sweepAngleDegrees = 310f,
                forceMoveTo = false
            )
        }
        drawPath(
            path = circlePath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        val arrowPath = Path().apply {
            moveTo(w * 0.32f, h * 0.08f)
            lineTo(w * 0.22f, h * 0.22f)
            lineTo(w * 0.38f, h * 0.26f)
        }
        drawPath(
            path = arrowPath,
            color = color,
            style = Stroke(width = strokeWidth * 0.9f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        drawLine(
            color = color,
            start = Offset(w * 0.5f, h * 0.5f),
            end = Offset(w * 0.5f, h * 0.34f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(w * 0.5f, h * 0.5f),
            end = Offset(w * 0.64f, h * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun RightDirectionArrowIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.8.dp.toPx()

        drawLine(
            color = color,
            start = Offset(w * 0.2f, h * 0.5f),
            end = Offset(w * 0.8f, h * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(w * 0.56f, h * 0.28f),
            end = Offset(w * 0.8f, h * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(w * 0.56f, h * 0.72f),
            end = Offset(w * 0.8f, h * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun ClockSmallOutlineIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.5.dp.toPx()

        drawCircle(
            color = color,
            radius = w * 0.42f,
            center = Offset(w * 0.5f, h * 0.5f),
            style = Stroke(width = strokeWidth)
        )

        drawLine(
            color = color,
            start = Offset(w * 0.5f, h * 0.5f),
            end = Offset(w * 0.5f, h * 0.28f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(w * 0.5f, h * 0.5f),
            end = Offset(w * 0.66f, h * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun CheckCircleSmallIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.5.dp.toPx()

        drawCircle(
            color = color,
            radius = w * 0.42f,
            center = Offset(w * 0.5f, h * 0.5f),
            style = Stroke(width = strokeWidth)
        )

        val checkPath = Path().apply {
            moveTo(w * 0.32f, h * 0.50f)
            lineTo(w * 0.45f, h * 0.65f)
            lineTo(w * 0.68f, h * 0.38f)
        }
        drawPath(
            path = checkPath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun BellRingingIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.8.dp.toPx()

        val bellPath = Path().apply {
            moveTo(w * 0.5f, h * 0.16f)
            cubicTo(w * 0.36f, h * 0.16f, w * 0.28f, h * 0.34f, w * 0.28f, h * 0.54f)
            cubicTo(w * 0.28f, h * 0.68f, w * 0.20f, h * 0.74f, w * 0.20f, h * 0.78f)
            lineTo(w * 0.80f, h * 0.78f)
            cubicTo(w * 0.80f, h * 0.74f, w * 0.72f, h * 0.68f, w * 0.72f, h * 0.54f)
            cubicTo(w * 0.72f, h * 0.34f, w * 0.64f, h * 0.16f, w * 0.5f, h * 0.16f)
            close()
        }
        drawPath(
            path = bellPath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        drawCircle(
            color = color,
            radius = w * 0.06f,
            center = Offset(w * 0.5f, h * 0.14f),
            style = Stroke(width = strokeWidth * 0.9f)
        )

        val clapperPath = Path().apply {
            moveTo(w * 0.42f, h * 0.84f)
            cubicTo(w * 0.42f, h * 0.90f, w * 0.58f, h * 0.90f, w * 0.58f, h * 0.84f)
        }
        drawPath(
            path = clapperPath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Preview(
    name = "12 · Quyết toán & Nhắc nợ (SpendTogether)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun SettlementScreenPreview() {
    Nhoms8Theme {
        SettlementScreen()
    }
}
