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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhoms8.ui.theme.Nhoms8Theme

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
 * Thiết kế chuẩn ứng dụng SpendTogether theo Figma:
 * - Nền be kem ấm áp, mảng cong xanh xô thơm
 * - Thanh tiêu đề: Nút Back, "Ai nợ ai?", Nút lịch sử quyết toán
 * - Thẻ tổng quan quyết toán: Nền xanh rêu sẫm, tổng tiền cần quyết toán & badge số lượng khoản
 * - Danh sách 3 thẻ khoản nợ với luồng chuyển tiền (Người nợ -> Số tiền -> Người nhận) & Nút Trả nợ / Đã trả
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
    // Bảng màu thiết kế chuẩn Figma SpendTogether
    val backgroundColor = Color(0xFFF5F3EC)      // Màu be kem nhạt ấm áp
    val ambientSage = Color(0xFFDDEAE0)          // Màu xanh xô thơm nhạt trang trí
    val forestGreenDark = Color(0xFF14382C)      // Xanh rêu đậm thương hiệu
    val textMuted = Color(0xFF6E7C74)            // Màu xám xanh phụ đề
    val debtCoralRed = Color(0xFFD96B52)         // Màu đỏ cam cho khoản nợ chưa trả
    val paidGreyText = Color(0xFF6E857B)         // Màu xám xanh cho khoản nợ đã trả
    val checkGreen = Color(0xFF387E58)           // Màu xanh lá dấu tích đã thanh toán
    val arrowSage = Color(0xFF8BA99B)            // Màu xanh xô thơm cho mũi tên luồng nợ
    val disabledPillBg = Color(0xFFDCE3DF)       // Nền nút đã thanh toán
    val disabledPillText = Color(0xFF5A6F65)     // Chữ nút đã thanh toán

    // Trạng thái bật/tắt nhắc nợ tự động
    var autoReminderEnabled by remember(autoReminder) { mutableStateOf(autoReminder) }

    // Danh sách khoản nợ thực tế giữa các thành viên Nhóm 08
    val defaultDebtItems = remember {
        mutableStateListOf(
            DebtItem(
                id = "debt_1",
                debtorName = "Nguyễn Phan Tấn Đạt",
                debtorInitials = "TĐ",
                debtorAvatarBg = Color(0xFFCBE3DC),
                creditorName = "Lê Văn Hùng",
                creditorInitials = "VH",
                creditorAvatarBg = Color(0xFFC4E2D3),
                rawAmount = 232500L,
                amountFormatted = "232.500 đ",
                statusText = "Chờ chuyển khoản",
                isPaid = false
            ),
            DebtItem(
                id = "debt_2",
                debtorName = "Lê Trung Bình",
                debtorInitials = "TB",
                debtorAvatarBg = Color(0xFFF2D7C8),
                creditorName = "Lê Văn Hùng",
                creditorInitials = "VH",
                creditorAvatarBg = Color(0xFFC4E2D3),
                rawAmount = 192500L,
                amountFormatted = "192.500 đ",
                statusText = "Chờ chuyển khoản",
                isPaid = false
            ),
            DebtItem(
                id = "debt_3",
                debtorName = "Lý Việt Linh",
                debtorInitials = "VL",
                debtorAvatarBg = Color(0xFFDEE7A8),
                creditorName = "Lê Văn Hùng",
                creditorInitials = "VH",
                creditorAvatarBg = Color(0xFFC4E2D3),
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
    val formattedTotalPending = String.format("%,d đ", totalPendingAmount).replace(',', '.')

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // 1. Họa tiết nền cong mềm mại góc trên bên phải & dưới bên trái
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Mảng cong góc trên bên phải
            drawCircle(
                color = ambientSage,
                radius = w * 0.45f,
                center = Offset(w * 0.95f, h * 0.04f)
            )

            // Mảng cong lớn góc dưới bên trái
            drawCircle(
                color = ambientSage,
                radius = w * 0.55f,
                center = Offset(w * 0.05f, h * 0.86f)
            )
        }

        // 2. Nội dung cuộn dọc toàn màn hình
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
                // Bên trái: Nút quay lại và Cụm tiêu đề
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Nút tròn quay lại (Back Button)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(
                                elevation = 3.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.06f),
                                spotColor = Color.Black.copy(alpha = 0.08f)
                            )
                            .background(Color.White, CircleShape)
                            .clickable { onBackClicked() },
                        contentAlignment = Alignment.Center
                    ) {
                        BackArrowIcon(
                            color = forestGreenDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Cụm tiêu đề màn hình
                    Column {
                        Text(
                            text = groupTitle,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = forestGreenDark,
                            letterSpacing = (-0.3).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = groupSubtitle,
                            fontSize = 13.sp,
                            color = textMuted
                        )
                    }
                }

                // Bên phải: Nút Lịch sử quyết toán
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(
                            elevation = 3.dp,
                            shape = CircleShape,
                            ambientColor = Color.Black.copy(alpha = 0.06f),
                            spotColor = Color.Black.copy(alpha = 0.08f)
                        )
                        .background(Color.White, CircleShape)
                        .clickable { onHistoryClicked() },
                    contentAlignment = Alignment.Center
                ) {
                    HistoryClockIcon(
                        color = forestGreenDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ====================== 2. THẺ TỔNG QUAN QUYẾT TOÁN ======================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = forestGreenDark.copy(alpha = 0.2f),
                        spotColor = forestGreenDark.copy(alpha = 0.25f)
                    )
                    .background(forestGreenDark, RoundedCornerShape(22.dp))
                    .padding(horizontal = 22.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Bên trái: Nhãn và Tổng số tiền cần quyết toán
                    Column {
                        Text(
                            text = "CẦN QUYẾT TOÁN",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFA8C5B8),
                            letterSpacing = 0.8.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (pendingCount > 0) formattedTotalPending else "0 đ",
                                fontSize = 27.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = (-0.5).sp
                            )
                        }
                    }

                    // Bên phải: Huy hiệu tròn thể hiện số lượng khoản nợ
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color.White.copy(alpha = 0.14f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape),
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
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ====================== 3. DANH SÁCH THẺ KHOẢN NỢ ======================
            if (currentDebts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(22.dp),
                            ambientColor = Color.Black.copy(alpha = 0.04f),
                            spotColor = Color.Black.copy(alpha = 0.06f)
                        )
                        .background(Color.White, RoundedCornerShape(22.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Tuyệt vời! Không có khoản nợ nào 🎉",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = forestGreenDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tất cả thành viên trong nhóm đều đã sòng phẳng.",
                            fontSize = 12.5.sp,
                            color = textMuted
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
                            forestGreenDark = forestGreenDark,
                            debtCoralRed = debtCoralRed,
                            paidGreyText = paidGreyText,
                            checkGreen = checkGreen,
                            arrowSage = arrowSage,
                            disabledPillBg = disabledPillBg,
                            disabledPillText = disabledPillText,
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = Color.Black.copy(alpha = 0.04f),
                        spotColor = Color.Black.copy(alpha = 0.06f)
                    )
                    .background(Color.White, RoundedCornerShape(22.dp))
                    .clickable {
                        autoReminderEnabled = !autoReminderEnabled
                        onToggleAutoReminder(autoReminderEnabled)
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Cụm icon và tiêu đề bên trái
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Icon quả chuông trong khung tròn xanh mint
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color(0xFFE2F4E9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            BellRingingIcon(
                                color = forestGreenDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Nhắc nợ tự động",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = forestGreenDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Gửi lời nhắc trước hạn 1 ngày",
                                fontSize = 12.5.sp,
                                color = textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Công tắc Toggle Switch tùy biến
                    CustomToggleSwitch(
                        checked = autoReminderEnabled,
                        onCheckedChange = {
                            autoReminderEnabled = it
                            onToggleAutoReminder(it)
                        },
                        activeTrackColor = forestGreenDark,
                        inactiveTrackColor = Color(0xFFD4DCD7)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

/**
 * Thẻ hiển thị một khoản nợ giữa 2 thành viên
 */
@Composable
private fun DebtSettlementCard(
    item: DebtItem,
    forestGreenDark: Color,
    debtCoralRed: Color,
    paidGreyText: Color,
    checkGreen: Color,
    arrowSage: Color,
    disabledPillBg: Color,
    disabledPillText: Color,
    onSettleClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.06f)
            )
            .background(Color.White, RoundedCornerShape(22.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ================= HÀNG TRÊN: LUỒNG NỢ (Người nợ -> Số tiền -> Người nhận) =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Bên trái: Người nợ
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(76.dp)
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
                            color = forestGreenDark
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.debtorName,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = forestGreenDark
                    )
                }

                // Ở giữa: Số tiền và mũi tên luồng nợ
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.amountFormatted,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isPaid) paidGreyText else debtCoralRed
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    RightDirectionArrowIcon(
                        color = if (item.isPaid) Color(0xFFB4C2BB) else arrowSage,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Bên phải: Người nhận
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(76.dp)
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
                            color = forestGreenDark
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.creditorName,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = forestGreenDark
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
                // Bên trái: Trạng thái hạn trả hoặc đã thanh toán
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.isPaid) {
                        // Icon dấu tích tròn màu xanh lá
                        CheckCircleSmallIcon(
                            color = checkGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        // Icon đồng hồ nhỏ màu cam
                        ClockSmallOutlineIcon(
                            color = Color(0xFFE07A5F),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = item.statusText,
                        fontSize = 13.sp,
                        color = Color(0xFF6E7C74),
                        fontWeight = FontWeight.Normal
                    )
                }

                // Bên phải: Nút "Trả nợ" hoặc "Đã trả"
                if (item.isPaid) {
                    // Badge dạng viên thuốc đã hoàn tất
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(CircleShape)
                            .background(disabledPillBg)
                            .padding(horizontal = 22.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Đã trả",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = disabledPillText
                        )
                    }
                } else {
                    // Nút bấm "Trả nợ" màu xanh rêu sẫm
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .shadow(
                                elevation = 2.dp,
                                shape = CircleShape,
                                ambientColor = forestGreenDark.copy(alpha = 0.15f),
                                spotColor = forestGreenDark.copy(alpha = 0.2f)
                            )
                            .clip(CircleShape)
                            .background(forestGreenDark)
                            .clickable { onSettleClicked() }
                            .padding(horizontal = 22.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Trả nợ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Công tắc Toggle Switch tùy biến phong cách Material 3 / iOS tinh gọn
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

/**
 * Icon mũi tên quay lại sang trái `←`
 */
@Composable
private fun BackArrowIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 2.dp.toPx()

        // Đường thẳng thân mũi tên
        drawLine(
            color = color,
            start = Offset(w * 0.82f, h * 0.5f),
            end = Offset(w * 0.18f, h * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Cánh mũi tên trên
        drawLine(
            color = color,
            start = Offset(w * 0.45f, h * 0.24f),
            end = Offset(w * 0.18f, h * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Cánh mũi tên dưới
        drawLine(
            color = color,
            start = Offset(w * 0.45f, h * 0.76f),
            end = Offset(w * 0.18f, h * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Icon đồng hồ quay ngược lịch sử (History Clock Icon)
 */
@Composable
private fun HistoryClockIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.8.dp.toPx()

        // Vòng tròn đồng hồ mở ở góc trên bên trái
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

        // Mũi tên nhỏ quay ngược ở điểm bắt đầu cung tròn
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

        // Kim giờ và kim phút
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

/**
 * Icon mũi tên hướng sang phải `→` thể hiện luồng nợ
 */
@Composable
private fun RightDirectionArrowIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.8.dp.toPx()

        // Đường thẳng thân
        drawLine(
            color = color,
            start = Offset(w * 0.2f, h * 0.5f),
            end = Offset(w * 0.8f, h * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Cánh mũi tên trên
        drawLine(
            color = color,
            start = Offset(w * 0.56f, h * 0.28f),
            end = Offset(w * 0.8f, h * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Cánh mũi tên dưới
        drawLine(
            color = color,
            start = Offset(w * 0.56f, h * 0.72f),
            end = Offset(w * 0.8f, h * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Icon đồng hồ nhỏ nét mảnh màu cam (Small Clock Outline Icon)
 */
@Composable
private fun ClockSmallOutlineIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.5.dp.toPx()

        // Vòng tròn đồng hồ
        drawCircle(
            color = color,
            radius = w * 0.42f,
            center = Offset(w * 0.5f, h * 0.5f),
            style = Stroke(width = strokeWidth)
        )

        // Kim giờ
        drawLine(
            color = color,
            start = Offset(w * 0.5f, h * 0.5f),
            end = Offset(w * 0.5f, h * 0.28f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Kim phút
        drawLine(
            color = color,
            start = Offset(w * 0.5f, h * 0.5f),
            end = Offset(w * 0.66f, h * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Icon dấu tích tròn màu xanh lá (Check Circle Icon)
 */
@Composable
private fun CheckCircleSmallIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.5.dp.toPx()

        // Vòng tròn bao ngoài
        drawCircle(
            color = color,
            radius = w * 0.42f,
            center = Offset(w * 0.5f, h * 0.5f),
            style = Stroke(width = strokeWidth)
        )

        // Dấu tích bên trong
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

/**
 * Icon quả chuông rung (Bell Ringing Icon)
 */
@Composable
private fun BellRingingIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.8.dp.toPx()

        // Thân quả chuông
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

        // Quai trên của chuông
        drawCircle(
            color = color,
            radius = w * 0.06f,
            center = Offset(w * 0.5f, h * 0.14f),
            style = Stroke(width = strokeWidth * 0.9f)
        )

        // Quả lắc chuông dưới
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
