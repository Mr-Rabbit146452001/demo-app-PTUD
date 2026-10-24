package com.example.nhoms8

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhoms8.ui.theme.Nhoms8Theme
import java.text.DecimalFormat

/**
 * Các chế độ chia tiền
 */
enum class SplitMode(val label: String) {
    EQUAL("Chia đều"),
    RATIO("Theo tỷ lệ"),
    EXACT("Số tiền")
}

/**
 * Thành viên tham gia chia tiền
 */
data class SplitMemberItem(
    val id: String,
    val name: String,
    val initials: String,
    val avatarBgColor: Color,
    val isSelected: Boolean = true,
    val assignedAmount: Long = 170000L
)

/**
 * 11 · Màn hình Chia tiền (Split Bill Screen)
 * Thiết kế chuẩn Figma SpendTogether: Segmented Control 3 chế độ chia, Thẻ tóm tắt kết quả, Danh sách thành viên kèm checkbox tròn
 */
@Composable
fun SplitBillScreen(
    expenseTitle: String = "Đi chợ cuối tuần",
    totalAmount: Long = 850000L,
    onBackClicked: () -> Unit = {},
    onCalculatorClicked: () -> Unit = {},
    onConfirmSplitClicked: (selectedCount: Int, perPersonAmount: Long) -> Unit = { _, _ -> }
) {
    var selectedMode by remember { mutableStateOf(SplitMode.EQUAL) }

    // Danh sách 5 thành viên tham gia chia tiền
    var membersList by remember {
        mutableStateOf(
            listOf(
                SplitMemberItem("1", "Minh Anh", "MA", Color(0xFFC4E2D3), true),
                SplitMemberItem("2", "Lan Anh", "LA", Color(0xFFDEE7A8), true),
                SplitMemberItem("3", "Tuấn Nam", "TN", Color(0xFFCBE3DC), true),
                SplitMemberItem("4", "Hà My", "HM", Color(0xFFF2D7C8), true),
                SplitMemberItem("5", "Quang Phúc", "QP", Color(0xFFD2DEC8), true)
            )
        )
    }

    // Tính toán số tiền mỗi người dựa trên số người được chọn
    val selectedMembersCount = membersList.count { it.isSelected }
    val perPersonAmount = if (selectedMembersCount > 0) totalAmount / selectedMembersCount else 0L

    val formatter = remember { DecimalFormat("#,###") }
    val formattedPerPerson = "${formatter.format(perPersonAmount).replace(',', '.')} đ"
    val formattedTotal = "${formatter.format(totalAmount).replace(',', '.')} đ"

    // Bảng màu thiết kế chuẩn Figma
    val backgroundColor = Color(0xFFF5F3EC)      // Màu kem nhạt ấm áp
    val ambientSage = Color(0xFFDDEAE0)          // Màu xanh xô thơm nhạt họa tiết nền
    val forestGreenDark = Color(0xFF14382C)      // Xanh rêu đậm thương hiệu
    val textMuted = Color(0xFF6E7C74)            // Màu xám xanh phụ đề
    val mintBadgeBg = Color(0xFFE2F4E9)          // Màu nền badge xác nhận đủ tiền
    val badgeGreen = Color(0xFF236B46)           // Màu xanh lá đậm chữ trong badge
    val pillBoxBg = Color(0xFFF5F3EC)            // Màu nền ô tiền của mỗi người

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // ====================== 1. HỌA TIẾT NỀN TRANG TRÍ ======================
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Mảng cong xanh xô thơm ở góc trên bên phải
            drawCircle(
                color = ambientSage,
                radius = w * 0.42f,
                center = Offset(x = w * 1.05f, y = h * 0.05f)
            )

            // Mảng cong lớn màu xanh xô thơm ở góc dưới bên trái
            drawCircle(
                color = ambientSage,
                radius = w * 0.58f,
                center = Offset(x = w * 0.04f, y = h * 0.88f)
            )
        }

        // ====================== 2. NỘI DUNG CUỘN CHÍNH ======================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // ---------- Thanh tiêu đề trên cùng (App Bar) ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nút Back + Tiêu đề
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .shadow(
                                elevation = 3.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.04f),
                                spotColor = Color.Black.copy(alpha = 0.08f)
                            )
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { onBackClicked() },
                        contentAlignment = Alignment.Center
                    ) {
                        ArrowBackIcon(
                            modifier = Modifier.size(18.dp),
                            color = forestGreenDark
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Chia tiền",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = forestGreenDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$expenseTitle · $formattedTotal",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Normal,
                            color = textMuted
                        )
                    }
                }

                // Nút Máy tính (Bên phải)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(
                            elevation = 3.dp,
                            shape = CircleShape,
                            ambientColor = Color.Black.copy(alpha = 0.04f),
                            spotColor = Color.Black.copy(alpha = 0.08f)
                        )
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onCalculatorClicked() },
                    contentAlignment = Alignment.Center
                ) {
                    CalculatorIcon(
                        modifier = Modifier.size(19.dp),
                        color = forestGreenDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Phần cuộn nội dung chia tiền
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // ---------- 3. Thanh chuyển đổi chế độ chia (Segmented Control) ----------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 3.dp,
                            shape = CircleShape,
                            ambientColor = Color.Black.copy(alpha = 0.04f),
                            spotColor = Color.Black.copy(alpha = 0.06f)
                        )
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SplitMode.entries.forEach { mode ->
                            val isSelected = selectedMode == mode
                            val tabBg = if (isSelected) forestGreenDark else Color.Transparent
                            val tabTextColor = if (isSelected) Color.White else textMuted

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(CircleShape)
                                    .background(tabBg)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { selectedMode = mode },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.label,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = tabTextColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ---------- 4. Thẻ tóm tắt kết quả chia (Split Summary Card) ----------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(22.dp),
                            ambientColor = Color.Black.copy(alpha = 0.03f),
                            spotColor = Color.Black.copy(alpha = 0.07f)
                        )
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .padding(horizontal = 18.dp, vertical = 18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Hàng trên: "Mỗi người thanh toán" + Số tiền & Badge "Đủ 850.000 đ"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Mỗi người thanh toán",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = textMuted
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formattedPerPerson,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = forestGreenDark,
                                    letterSpacing = (-0.5).sp
                                )
                            }

                            // Badge xanh mint "Đủ 850.000 đ"
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(mintBadgeBg)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CheckCircleSmallIcon(
                                        modifier = Modifier.size(14.dp),
                                        color = badgeGreen
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Đủ $formattedTotal",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeGreen
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Hàng dưới: Badge 5 người + text "Tất cả thành viên được chọn"
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(forestGreenDark)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TwoPeopleIcon(
                                        modifier = Modifier.size(13.dp),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "$selectedMembersCount người",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = if (selectedMembersCount == membersList.size) "Tất cả thành viên được chọn" else "Đã chọn $selectedMembersCount/${membersList.size} thành viên",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Normal,
                                color = textMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ---------- 5. Danh sách thành viên tham gia chia tiền ----------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = Color.Black.copy(alpha = 0.03f),
                            spotColor = Color.Black.copy(alpha = 0.07f)
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        membersList.forEachIndexed { index, member ->
                            SplitMemberRow(
                                member = member,
                                displayAmount = formattedPerPerson,
                                forestGreenDark = forestGreenDark,
                                pillBoxBg = pillBoxBg,
                                onToggleSelect = {
                                    membersList = membersList.map {
                                        if (it.id == member.id) it.copy(isSelected = !it.isSelected) else it
                                    }
                                }
                            )

                            if (index < membersList.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    thickness = 0.8.dp,
                                    color = Color(0xFFEFF2EE)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ---------- 6. Nút hành động chính: Xác nhận chia tiền ----------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = CircleShape,
                            spotColor = forestGreenDark.copy(alpha = 0.35f)
                        )
                        .clip(CircleShape)
                        .background(forestGreenDark)
                        .clickable { onConfirmSplitClicked(selectedMembersCount, perPersonAmount) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CheckmarkWhiteIcon(
                            modifier = Modifier.size(16.dp),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Xác nhận chia tiền",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/**
 * Hàng hiển thị 1 thành viên tham gia chia tiền
 */
@Composable
private fun SplitMemberRow(
    member: SplitMemberItem,
    displayAmount: String,
    forestGreenDark: Color,
    pillBoxBg: Color,
    onToggleSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onToggleSelect() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Cụm trái: Checkbox + Avatar + Tên
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox hình tròn xanh rêu
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (member.isSelected) forestGreenDark else Color(0xFFE2E7E2)),
                contentAlignment = Alignment.Center
            ) {
                if (member.isSelected) {
                    CheckmarkWhiteIcon(
                        modifier = Modifier.size(13.dp),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Avatar tròn viết tắt
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(member.avatarBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.initials,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = forestGreenDark
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = member.name,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = forestGreenDark
            )
        }

        // Cụm phải: Khung viên thuốc hiển thị số tiền
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(pillBoxBg)
                .padding(horizontal = 14.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (member.isSelected) displayAmount else "0 đ",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (member.isSelected) forestGreenDark else Color(0xFF9EAAA2)
            )
        }
    }
}

// ==============================================================================
// CÁC HỌA TIẾT ICON ĐƯỢC VẼ TỈ MỈ BẰNG CANVAS (ZERO-DEPENDENCY, KHÔNG LỖI VERSION)
// ==============================================================================

/**
 * Icon Mũi tên quay lại (Arrow Back `←`)
 */
@Composable
private fun ArrowBackIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = 1.8.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        drawLine(
            color = color,
            start = Offset(w * 0.82f, h * 0.50f),
            end = Offset(w * 0.18f, h * 0.50f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )

        val arrowHead = Path().apply {
            moveTo(w * 0.44f, h * 0.26f)
            lineTo(w * 0.18f, h * 0.50f)
            lineTo(w * 0.44f, h * 0.74f)
        }
        drawPath(path = arrowHead, color = color, style = stroke)
    }
}

/**
 * Icon Máy tính bỏ túi (Calculator Outline)
 */
@Composable
private fun CalculatorIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = 1.6.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        // Thân máy tính
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.20f, h * 0.14f),
            size = Size(w * 0.60f, h * 0.72f),
            cornerRadius = CornerRadius(w * 0.10f, h * 0.10f),
            style = stroke
        )

        // Màn hình hiển thị số ở trên
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.28f, h * 0.24f),
            size = Size(w * 0.44f, h * 0.16f),
            cornerRadius = CornerRadius(w * 0.04f, h * 0.04f),
            style = stroke
        )

        // 4 nút bấm
        drawCircle(color = color, radius = w * 0.04f, center = Offset(w * 0.36f, h * 0.52f))
        drawCircle(color = color, radius = w * 0.04f, center = Offset(w * 0.64f, h * 0.52f))
        drawCircle(color = color, radius = w * 0.04f, center = Offset(w * 0.36f, h * 0.70f))
        drawCircle(color = color, radius = w * 0.04f, center = Offset(w * 0.64f, h * 0.70f))
    }
}

/**
 * Icon Dấu tích tròn nhỏ (Check Circle Small)
 */
@Composable
private fun CheckCircleSmallIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = 1.6.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        drawCircle(
            color = color,
            radius = w * 0.42f,
            center = Offset(w * 0.50f, h * 0.50f),
            style = stroke
        )

        val check = Path().apply {
            moveTo(w * 0.32f, h * 0.50f)
            lineTo(w * 0.46f, h * 0.64f)
            lineTo(w * 0.70f, h * 0.38f)
        }
        drawPath(path = check, color = color, style = stroke)
    }
}

/**
 * Icon 2 Người đại diện nhóm (Two People)
 */
@Composable
private fun TwoPeopleIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = 1.4.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        // Đầu người trước
        drawCircle(
            color = color,
            radius = w * 0.16f,
            center = Offset(w * 0.42f, h * 0.34f),
            style = stroke
        )

        // Thân người trước
        val bodyFront = Path().apply {
            moveTo(w * 0.16f, h * 0.82f)
            cubicTo(w * 0.16f, h * 0.62f, w * 0.68f, h * 0.62f, w * 0.68f, h * 0.82f)
        }
        drawPath(path = bodyFront, color = color, style = stroke)

        // Người phụ phía sau
        drawCircle(
            color = color,
            radius = w * 0.13f,
            center = Offset(w * 0.74f, h * 0.38f),
            style = stroke
        )
        val bodyBack = Path().apply {
            moveTo(w * 0.68f, h * 0.70f)
            cubicTo(w * 0.78f, h * 0.64f, w * 0.90f, h * 0.72f, w * 0.90f, h * 0.82f)
        }
        drawPath(path = bodyBack, color = color, style = stroke)
    }
}

/**
 * Icon Dấu tích trắng (Checkmark White)
 */
@Composable
private fun CheckmarkWhiteIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = 1.9.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        val check = Path().apply {
            moveTo(w * 0.22f, h * 0.52f)
            lineTo(w * 0.44f, h * 0.74f)
            lineTo(w * 0.82f, h * 0.28f)
        }
        drawPath(path = check, color = color, style = stroke)
    }
}

// ====================== PREVIEW ======================
@Preview(showBackground = true, device = "id:pixel_7", name = "11 - Split Bill Screen")
@Composable
fun SplitBillScreenPreview() {
    Nhoms8Theme {
        SplitBillScreen()
    }
}
