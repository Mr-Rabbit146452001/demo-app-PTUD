package com.example.nhoms8

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhoms8.ui.theme.Nhoms8Theme

/**
 * Các danh mục chi tiêu khả dụng
 */
enum class ExpenseCategory(val label: String) {
    FOOD("Ăn uống"),
    HOUSING("Nhà ở"),
    TRANSPORT("Đi lại"),
    OTHER("Khác")
}

/**
 * 10 · Màn hình Tạo khoản chi (Create Expense Screen)
 * Thiết kế chuẩn Figma SpendTogether: Thẻ nhập tiền lớn màu xanh rêu, Form chọn danh mục dạng pill tag, người trả và ghi chú
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateExpenseScreen(
    groupTitle: String = "Tạo khoản chi",
    groupSubtitle: String = "Ghi vào quỹ Nhà Mình",
    initialAmount: String = "850.000",
    initialAmountInWords: String = "Tám trăm năm mươi nghìn đồng",
    onBackClicked: () -> Unit = {},
    onScanReceiptClicked: () -> Unit = {},
    onSaveAndSplitClicked: (amount: String, category: String, note: String) -> Unit = { _, _, _ -> }
) {
    var amount by remember { mutableStateOf(initialAmount) }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.FOOD) }
    var note by remember { mutableStateOf("Thực phẩm và đồ dùng cuối tuần") }
    val expenseDate by remember { mutableStateOf("03 tháng 10, 2026") }

    // Bảng màu thiết kế chuẩn Figma
    val backgroundColor = Color(0xFFF5F3EC)      // Màu kem nhạt ấm áp
    val ambientSage = Color(0xFFDDEAE0)          // Màu xanh xô thơm nhạt họa tiết nền
    val forestGreenDark = Color(0xFF14382C)      // Xanh rêu đậm thương hiệu
    val textMuted = Color(0xFF6E7C74)            // Màu xám xanh phụ đề
    val labelColor = Color(0xFF43534B)           // Màu nhãn trường
    val inputFieldBg = Color(0xFFF6F5EF)         // Màu nền ô nhập nhẹ nhàng
    val payerCardBg = Color(0xFFE2F4E9)          // Màu nền xanh bạc hà thẻ người trả
    val accentGreen = Color(0xFF3E6754)          // Màu xanh điểm nhấn

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
                .imePadding()
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
                            text = groupTitle,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = forestGreenDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = groupSubtitle,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Normal,
                            color = textMuted
                        )
                    }
                }

                // Nút Quét hóa đơn (Bên phải)
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
                        .clickable { onScanReceiptClicked() },
                    contentAlignment = Alignment.Center
                ) {
                    ScanReceiptIcon(
                        modifier = Modifier.size(19.dp),
                        color = forestGreenDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Phần cuộn nội dung form
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // ---------- 3. Thẻ nhập số tiền (Amount Input Card) ----------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(22.dp),
                            ambientColor = forestGreenDark.copy(alpha = 0.2f),
                            spotColor = forestGreenDark.copy(alpha = 0.35f)
                        )
                        .clip(RoundedCornerShape(22.dp))
                        .background(forestGreenDark)
                        .padding(vertical = 22.dp, horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SỐ TIỀN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9FB8AC),
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Số tiền nổi bật
                        Text(
                            text = "$amount đ",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Đọc số bằng chữ
                        Text(
                            text = initialAmountInWords,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFFA5BCB0)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ---------- 4. Thẻ chi tiết khoản chi (Expense Details Form Card) ----------
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
                        .padding(horizontal = 18.dp, vertical = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // ----- Mục 1: Ngày chi -----
                        Text(
                            text = "Ngày chi",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = labelColor
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(inputFieldBg)
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CalendarOutlineIcon(
                                        modifier = Modifier.size(18.dp),
                                        color = Color(0xFF67786F)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = expenseDate,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1E2D25)
                                    )
                                }

                                Text(
                                    text = "Hôm nay",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // ----- Mục 2: Danh mục (Pill tags) -----
                        Text(
                            text = "Danh mục",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = labelColor
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CategoryPillChip(
                                label = ExpenseCategory.FOOD.label,
                                isSelected = selectedCategory == ExpenseCategory.FOOD,
                                forestGreenDark = forestGreenDark,
                                icon = { color -> CutleryIcon(modifier = Modifier.size(15.dp), color = color) },
                                onClick = { selectedCategory = ExpenseCategory.FOOD }
                            )

                            CategoryPillChip(
                                label = ExpenseCategory.HOUSING.label,
                                isSelected = selectedCategory == ExpenseCategory.HOUSING,
                                forestGreenDark = forestGreenDark,
                                icon = { color -> HouseCategoryIcon(modifier = Modifier.size(15.dp), color = color) },
                                onClick = { selectedCategory = ExpenseCategory.HOUSING }
                            )

                            CategoryPillChip(
                                label = ExpenseCategory.TRANSPORT.label,
                                isSelected = selectedCategory == ExpenseCategory.TRANSPORT,
                                forestGreenDark = forestGreenDark,
                                icon = { color -> CarCategoryIcon(modifier = Modifier.size(15.dp), color = color) },
                                onClick = { selectedCategory = ExpenseCategory.TRANSPORT }
                            )

                            CategoryPillChip(
                                label = ExpenseCategory.OTHER.label,
                                isSelected = selectedCategory == ExpenseCategory.OTHER,
                                forestGreenDark = forestGreenDark,
                                icon = { color -> MoreCategoryIcon(modifier = Modifier.size(15.dp), color = color) },
                                onClick = { selectedCategory = ExpenseCategory.OTHER }
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // ----- Mục 3: Người trả -----
                        Text(
                            text = "Người trả",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = labelColor
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(payerCardBg)
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Avatar tròn LA
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFDEE7A8)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "LA",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = forestGreenDark
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = "Lan Anh",
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = forestGreenDark
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Thanh toán từ tiền cá nhân",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = textMuted
                                        )
                                    }
                                }

                                ChevronDownIcon(
                                    modifier = Modifier.size(14.dp),
                                    color = accentGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // ----- Mục 4: Ghi chú -----
                        Text(
                            text = "Ghi chú",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = labelColor
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(inputFieldBg)
                                .padding(horizontal = 14.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                NotepadIcon(
                                    modifier = Modifier.size(18.dp),
                                    color = Color(0xFF67786F)
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                BasicTextField(
                                    value = note,
                                    onValueChange = { note = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1E2D25),
                                        fontFamily = FontFamily.Default
                                    ),
                                    cursorBrush = SolidColor(forestGreenDark)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // ---------- 5. Chân trang & Nút hành động (Footer) ----------
                // Dòng trạng thái căn giữa: Chia khoản này cho 5 thành viên
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SplitBranchSmallIcon(
                        modifier = Modifier.size(15.dp),
                        color = Color(0xFF7E8E84)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Chia khoản này cho 5 thành viên sau khi lưu",
                        fontSize = 12.5.sp,
                        color = Color(0xFF7E8E84),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Nút "Lưu & chia tiền"
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
                        .clickable { onSaveAndSplitClicked(amount, selectedCategory.label, note) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ArrowForwardActionIcon(
                            modifier = Modifier.size(16.dp),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Lưu & chia tiền",
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
 * Chip lựa chọn danh mục dạng Pill Tag
 */
@Composable
private fun CategoryPillChip(
    label: String,
    isSelected: Boolean,
    forestGreenDark: Color,
    icon: @Composable (Color) -> Unit,
    onClick: () -> Unit
) {
    val chipBg = if (isSelected) forestGreenDark else Color.White
    val contentColor = if (isSelected) Color.White else Color(0xFF1E2D25)
    val borderModifier = if (isSelected) {
        Modifier
    } else {
        Modifier.border(width = 1.dp, color = Color(0xFFE2E7E2), shape = CircleShape)
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .then(borderModifier)
            .background(chipBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon(contentColor)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = contentColor
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
 * Icon Quét hóa đơn (Scan Outline)
 */
@Composable
private fun ScanReceiptIcon(
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

        val cornerLen = w * 0.22f

        // Góc trên trái
        val topLeft = Path().apply {
            moveTo(w * 0.20f, h * 0.20f + cornerLen)
            lineTo(w * 0.20f, h * 0.20f)
            lineTo(w * 0.20f + cornerLen, h * 0.20f)
        }
        drawPath(path = topLeft, color = color, style = stroke)

        // Góc trên phải
        val topRight = Path().apply {
            moveTo(w * 0.80f - cornerLen, h * 0.20f)
            lineTo(w * 0.80f, h * 0.20f)
            lineTo(w * 0.80f, h * 0.20f + cornerLen)
        }
        drawPath(path = topRight, color = color, style = stroke)

        // Góc dưới trái
        val bottomLeft = Path().apply {
            moveTo(w * 0.20f, h * 0.80f - cornerLen)
            lineTo(w * 0.20f, h * 0.80f)
            lineTo(w * 0.20f + cornerLen, h * 0.80f)
        }
        drawPath(path = bottomLeft, color = color, style = stroke)

        // Góc dưới phải
        val bottomRight = Path().apply {
            moveTo(w * 0.80f - cornerLen, h * 0.80f)
            lineTo(w * 0.80f, h * 0.80f)
            lineTo(w * 0.80f, h * 0.80f - cornerLen)
        }
        drawPath(path = bottomRight, color = color, style = stroke)

        // Đường quét ngang ở giữa
        drawLine(
            color = color,
            start = Offset(w * 0.32f, h * 0.50f),
            end = Offset(w * 0.68f, h * 0.50f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * Icon Lịch (Calendar Outline)
 */
@Composable
private fun CalendarOutlineIcon(
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

        // Khung lịch
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.18f, h * 0.22f),
            size = Size(w * 0.64f, h * 0.62f),
            cornerRadius = CornerRadius(w * 0.12f, h * 0.12f),
            style = stroke
        )

        // Thanh ngang tiêu đề lịch
        drawLine(
            color = color,
            start = Offset(w * 0.18f, h * 0.40f),
            end = Offset(w * 0.82f, h * 0.40f),
            strokeWidth = 1.4.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 2 móc treo lịch
        drawLine(
            color = color,
            start = Offset(w * 0.34f, h * 0.14f),
            end = Offset(w * 0.34f, h * 0.26f),
            strokeWidth = 1.6.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.66f, h * 0.14f),
            end = Offset(w * 0.66f, h * 0.26f),
            strokeWidth = 1.6.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * Icon Dao Nĩa (Ăn uống)
 */
@Composable
private fun CutleryIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = 1.5.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        // Nĩa (Fork) bên trái
        drawLine(
            color = color,
            start = Offset(w * 0.35f, h * 0.20f),
            end = Offset(w * 0.35f, h * 0.80f),
            strokeWidth = 1.4.dp.toPx(),
            cap = StrokeCap.Round
        )
        val forkTines = Path().apply {
            moveTo(w * 0.22f, h * 0.20f)
            lineTo(w * 0.22f, h * 0.42f)
            lineTo(w * 0.48f, h * 0.42f)
            lineTo(w * 0.48f, h * 0.20f)
        }
        drawPath(path = forkTines, color = color, style = stroke)

        // Dao (Knife) bên phải
        val knife = Path().apply {
            moveTo(w * 0.72f, h * 0.80f)
            lineTo(w * 0.72f, h * 0.20f)
            cubicTo(w * 0.72f, h * 0.20f, w * 0.86f, h * 0.30f, w * 0.86f, h * 0.46f)
            lineTo(w * 0.72f, h * 0.52f)
        }
        drawPath(path = knife, color = color, style = stroke)
    }
}

/**
 * Icon Ngôi Nhà (Nhà ở)
 */
@Composable
private fun HouseCategoryIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = 1.5.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        val house = Path().apply {
            moveTo(w * 0.20f, h * 0.46f)
            lineTo(w * 0.50f, h * 0.20f)
            lineTo(w * 0.80f, h * 0.46f)
            lineTo(w * 0.80f, h * 0.80f)
            lineTo(w * 0.20f, h * 0.80f)
            close()
        }
        drawPath(path = house, color = color, style = stroke)
    }
}

/**
 * Icon Ô Tô (Đi lại)
 */
@Composable
private fun CarCategoryIcon(
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

        val carBody = Path().apply {
            moveTo(w * 0.16f, h * 0.60f)
            lineTo(w * 0.26f, h * 0.40f)
            lineTo(w * 0.74f, h * 0.40f)
            lineTo(w * 0.84f, h * 0.60f)
            lineTo(w * 0.88f, h * 0.68f)
            lineTo(w * 0.12f, h * 0.68f)
            close()
        }
        drawPath(path = carBody, color = color, style = stroke)

        // 2 bánh xe
        drawCircle(
            color = color,
            radius = w * 0.08f,
            center = Offset(w * 0.30f, h * 0.72f)
        )
        drawCircle(
            color = color,
            radius = w * 0.08f,
            center = Offset(w * 0.70f, h * 0.72f)
        )
    }
}

/**
 * Icon Hình khối / Ba chấm (Khác)
 */
@Composable
private fun MoreCategoryIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = 1.4.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Khối hình tròn trên đỉnh
        drawCircle(
            color = color,
            radius = w * 0.12f,
            center = Offset(w * 0.50f, h * 0.32f),
            style = stroke
        )

        // Khối vuông dưới trái
        drawRect(
            color = color,
            topLeft = Offset(w * 0.22f, h * 0.56f),
            size = Size(w * 0.22f, h * 0.22f),
            style = stroke
        )

        // Khối tam giác dưới phải
        val tri = Path().apply {
            moveTo(w * 0.70f, h * 0.56f)
            lineTo(w * 0.84f, h * 0.78f)
            lineTo(w * 0.56f, h * 0.78f)
            close()
        }
        drawPath(path = tri, color = color, style = stroke)
    }
}

/**
 * Icon Mũi tên mở rộng xuống (Chevron Down `⌄`)
 */
@Composable
private fun ChevronDownIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = 1.7.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        val chevron = Path().apply {
            moveTo(w * 0.20f, h * 0.35f)
            lineTo(w * 0.50f, h * 0.65f)
            lineTo(w * 0.80f, h * 0.35f)
        }
        drawPath(path = chevron, color = color, style = stroke)
    }
}

/**
 * Icon Tờ giấy ghi chép (Notepad Outline)
 */
@Composable
private fun NotepadIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = 1.5.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        // Thân trang giấy
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.20f, h * 0.18f),
            size = Size(w * 0.60f, h * 0.64f),
            cornerRadius = CornerRadius(w * 0.10f, h * 0.10f),
            style = stroke
        )

        // Đường gạch ghi chép
        drawLine(
            color = color,
            start = Offset(w * 0.34f, h * 0.38f),
            end = Offset(w * 0.66f, h * 0.38f),
            strokeWidth = 1.4.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.34f, h * 0.52f),
            end = Offset(w * 0.56f, h * 0.52f),
            strokeWidth = 1.4.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * Icon Nhánh rẽ chia tiền nhỏ
 */
@Composable
private fun SplitBranchSmallIcon(
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

        // Thân chính
        drawLine(
            color = color,
            start = Offset(w * 0.50f, h * 0.84f),
            end = Offset(w * 0.50f, h * 0.50f),
            strokeWidth = 1.6.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Nhánh trái
        val left = Path().apply {
            moveTo(w * 0.50f, h * 0.50f)
            lineTo(w * 0.28f, h * 0.24f)
        }
        drawPath(path = left, color = color, style = stroke)

        // Nhánh phải
        val right = Path().apply {
            moveTo(w * 0.50f, h * 0.50f)
            lineTo(w * 0.72f, h * 0.24f)
        }
        drawPath(path = right, color = color, style = stroke)
    }
}

/**
 * Icon Mũi tên tiến tới `->`
 */
@Composable
private fun ArrowForwardActionIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = 2.0.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        drawLine(
            color = color,
            start = Offset(w * 0.16f, h * 0.50f),
            end = Offset(w * 0.84f, h * 0.50f),
            strokeWidth = 2.0.dp.toPx(),
            cap = StrokeCap.Round
        )

        val head = Path().apply {
            moveTo(w * 0.54f, h * 0.22f)
            lineTo(w * 0.84f, h * 0.50f)
            lineTo(w * 0.54f, h * 0.78f)
        }
        drawPath(path = head, color = color, style = stroke)
    }
}

// ====================== PREVIEW ======================
@Preview(
    name = "10 · Tạo khoản chi tiêu (SpendTogether)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun CreateExpenseScreenPreview() {
    Nhoms8Theme {
        CreateExpenseScreen()
    }
}
