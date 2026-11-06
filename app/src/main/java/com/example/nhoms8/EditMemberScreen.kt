package com.example.nhoms8

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhoms8.ui.theme.Nhoms8Theme

/**
 * 09 · Màn hình Chỉnh sửa thành viên (Edit Member Screen)
 * Thiết kế chuẩn ứng dụng SpendTogether theo Figma:
 * - Nền be kem ấm áp, mảng cong xanh xô thơm
 * - Header với nút quay lại và tiêu đề cập nhật
 * - Avatar tròn viết tắt "TĐ" kèm icon máy ảnh đổi ảnh
 * - Card Thông tin cá nhân: Họ tên, Số điện thoại, Vai trò (Thành viên / Trưởng nhóm)
 * - Card Phân quyền: Có thể thêm khoản chi (Toggle Đang bật / Đang tắt)
 * - Nút Lưu thay đổi & Nút Xóa khỏi nhóm
 */
@Composable
fun EditMemberScreen(
    initialMemberName: String = "Nguyễn Phan Tấn Đạt",
    initialInitials: String = "TĐ",
    initialPhoneNumber: String = "0986 420 115",
    initialIsLeader: Boolean = false,
    initialCanAddExpense: Boolean = true,
    onBackClicked: () -> Unit = {},
    onChangeAvatarClicked: () -> Unit = {},
    onSaveClicked: (name: String, phone: String, isLeader: Boolean, canAddExpense: Boolean) -> Unit = { _, _, _, _ -> },
    onDeleteMemberClicked: () -> Unit = {}
) {
    // Trạng thái các trường thông tin chỉnh sửa
    var memberName by remember { mutableStateOf(initialMemberName) }
    var phoneNumber by remember { mutableStateOf(initialPhoneNumber) }
    var isLeader by remember { mutableStateOf(initialIsLeader) }
    var canAddExpense by remember { mutableStateOf(initialCanAddExpense) }

    val currentInitials = remember(memberName) {
        val parts = memberName.trim().split(" ").filter { it.isNotBlank() }
        parts.mapNotNull { it.firstOrNull()?.toString() }.takeLast(2).joinToString("").uppercase().ifEmpty { initialInitials }
    }

    // Bảng màu thiết kế chuẩn Figma SpendTogether
    val backgroundColor = Color(0xFFF5F3EC)      // Màu be kem nhạt ấm áp
    val ambientSage = Color(0xFFDDEAE0)          // Màu xanh xô thơm nhạt trang trí
    val forestGreenDark = Color(0xFF14382C)      // Xanh rêu đậm thương hiệu
    val textMuted = Color(0xFF6E7C74)            // Màu xám xanh phụ đề
    val labelColor = Color(0xFF4A5F53)           // Màu chữ tiêu đề trường
    val inputBorderColor = Color(0xFFE6E3D8)     // Viền nhẹ cho ô nhập liệu
    val avatarBgColor = Color(0xFFCBE3DC)        // Màu nền ngọc bích avatar
    val activeToggleGreen = Color(0xFF387E58)     // Màu xanh rêu trạng thái "Đang bật"

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
                verticalAlignment = Alignment.CenterVertically
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Chỉnh sửa thành viên",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = forestGreenDark,
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ====================== 2. AVATAR & THAY ẢNH ======================
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onChangeAvatarClicked() },
                    contentAlignment = Alignment.Center
                ) {
                    // Vòng tròn avatar lớn
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.06f),
                                spotColor = Color.Black.copy(alpha = 0.1f)
                            )
                            .background(Color.White, CircleShape)
                            .border(4.dp, Color.White, CircleShape)
                            .clip(CircleShape)
                            .background(avatarBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentInitials,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = forestGreenDark,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Nút camera tròn nhỏ gắn ở góc dưới bên phải avatar
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .align(Alignment.BottomEnd)
                            .shadow(
                                elevation = 3.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.12f),
                                spotColor = Color.Black.copy(alpha = 0.15f)
                            )
                            .background(forestGreenDark, CircleShape)
                            .border(2.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        SmallCameraIcon(
                            color = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Chạm để thay ảnh đại diện",
                    fontSize = 13.sp,
                    color = textMuted,
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ====================== 3. CARD 1: THÔNG TIN CÁ NHÂN ======================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color.Black.copy(alpha = 0.04f),
                        spotColor = Color.Black.copy(alpha = 0.06f)
                    )
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Tiêu đề card
                    Text(
                        text = "Thông tin cá nhân",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = forestGreenDark
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Trường: Họ và tên ---
                    Text(
                        text = "Họ và tên",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = labelColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, inputBorderColor, CircleShape)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            UserOutlineIcon(
                                color = Color(0xFF7A9486),
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            BasicTextField(
                                value = memberName,
                                onValueChange = { memberName = it },
                                textStyle = TextStyle(
                                    fontSize = 15.sp,
                                    color = forestGreenDark,
                                    fontWeight = FontWeight.Normal
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(forestGreenDark),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // --- Trường: Số điện thoại ---
                    Text(
                        text = "Số điện thoại",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = labelColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, inputBorderColor, CircleShape)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            PhoneOutlineIcon(
                                color = Color(0xFF7A9486),
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            BasicTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                textStyle = TextStyle(
                                    fontSize = 15.sp,
                                    color = forestGreenDark,
                                    fontWeight = FontWeight.Normal
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                cursorBrush = SolidColor(forestGreenDark),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Trường: Vai trò trong nhóm ---
                    Text(
                        text = "Vai trò trong nhóm",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = labelColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Nút "Thành viên"
                        val isMemberSelected = !isLeader
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isMemberSelected) forestGreenDark else Color.Transparent
                                )
                                .then(
                                    if (!isMemberSelected) Modifier.border(1.dp, inputBorderColor, CircleShape)
                                    else Modifier
                                )
                                .clickable { isLeader = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Thành viên",
                                fontSize = 14.5.sp,
                                fontWeight = if (isMemberSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isMemberSelected) Color.White else Color(0xFF4A6054)
                            )
                        }

                        // Nút "Trưởng nhóm"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isLeader) forestGreenDark else Color.Transparent
                                )
                                .then(
                                    if (!isLeader) Modifier.border(1.dp, inputBorderColor, CircleShape)
                                    else Modifier
                                )
                                .clickable { isLeader = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Trưởng nhóm",
                                fontSize = 14.5.sp,
                                fontWeight = if (isLeader) FontWeight.Bold else FontWeight.Medium,
                                color = if (isLeader) Color.White else Color(0xFF4A6054)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ====================== 4. CARD 2: PHÂN QUYỀN ======================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color.Black.copy(alpha = 0.04f),
                        spotColor = Color.Black.copy(alpha = 0.06f)
                    )
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .clickable { canAddExpense = !canAddExpense }
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ====================== 5. ACTION BUTTONS ======================
            // Nút Lưu thay đổi (Primary CTA)
            Button(
                onClick = {
                    onSaveClicked(memberName, phoneNumber, isLeader, canAddExpense)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = CircleShape,
                        ambientColor = forestGreenDark.copy(alpha = 0.2f),
                        spotColor = forestGreenDark.copy(alpha = 0.3f)
                    ),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = forestGreenDark,
                    contentColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CheckmarkIcon(
                        color = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lưu thay đổi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nút Xóa khỏi nhóm (Secondary / Destructive Action)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(
                        elevation = 2.dp,
                        shape = CircleShape,
                        ambientColor = Color.Black.copy(alpha = 0.04f),
                        spotColor = Color.Black.copy(alpha = 0.06f)
                    )
                    .background(Color.White, CircleShape)
                    .border(1.dp, inputBorderColor, CircleShape)
                    .clip(CircleShape)
                    .clickable { onDeleteMemberClicked() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    TrashCanIcon(
                        color = forestGreenDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Xóa khỏi nhóm",
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = forestGreenDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// =========================================================================
// CUSTOM CANVAS ICONS (Vẽ tự thân, không phụ thuộc tài nguyên ngoài)
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
 * Icon máy ảnh nhỏ (Small Camera Icon)
 */
@Composable
private fun SmallCameraIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.6.dp.toPx()

        // Thân máy ảnh
        val bodyRect = Path().apply {
            moveTo(w * 0.18f, h * 0.38f)
            lineTo(w * 0.34f, h * 0.38f)
            lineTo(w * 0.42f, h * 0.25f)
            lineTo(w * 0.58f, h * 0.25f)
            lineTo(w * 0.66f, h * 0.38f)
            lineTo(w * 0.82f, h * 0.38f)
            quadraticTo(w * 0.88f, h * 0.38f, w * 0.88f, h * 0.46f)
            lineTo(w * 0.88f, h * 0.78f)
            quadraticTo(w * 0.88f, h * 0.84f, w * 0.82f, h * 0.84f)
            lineTo(w * 0.18f, h * 0.84f)
            quadraticTo(w * 0.12f, h * 0.84f, w * 0.12f, h * 0.78f)
            lineTo(w * 0.12f, h * 0.46f)
            quadraticTo(w * 0.12f, h * 0.38f, w * 0.18f, h * 0.38f)
            close()
        }

        drawPath(
            path = bodyRect,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Ống kính giữa
        drawCircle(
            color = color,
            radius = w * 0.16f,
            center = Offset(w * 0.5f, h * 0.61f),
            style = Stroke(width = strokeWidth)
        )
    }
}

/**
 * Icon ghi chép văn bản (Document Note Icon)
 */
@Composable
private fun DocumentNoteIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.8.dp.toPx()

        // Khung tài liệu bo góc nhẹ
        val docPath = Path().apply {
            moveTo(w * 0.24f, h * 0.14f)
            lineTo(w * 0.62f, h * 0.14f)
            lineTo(w * 0.78f, h * 0.30f)
            lineTo(w * 0.78f, h * 0.86f)
            lineTo(w * 0.24f, h * 0.86f)
            close()
        }

        drawPath(
            path = docPath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Nếp gấp góc phải trên
        val foldPath = Path().apply {
            moveTo(w * 0.62f, h * 0.14f)
            lineTo(w * 0.62f, h * 0.30f)
            lineTo(w * 0.78f, h * 0.30f)
        }
        drawPath(
            path = foldPath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Ba dòng kẻ ngang bên trong
        drawLine(
            color = color,
            start = Offset(w * 0.36f, h * 0.44f),
            end = Offset(w * 0.66f, h * 0.44f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(w * 0.36f, h * 0.58f),
            end = Offset(w * 0.66f, h * 0.58f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(w * 0.36f, h * 0.72f),
            end = Offset(w * 0.54f, h * 0.72f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Icon dấu tích `✓`
 */
@Composable
private fun CheckmarkIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 2.2.dp.toPx()

        val checkPath = Path().apply {
            moveTo(w * 0.18f, h * 0.52f)
            lineTo(w * 0.42f, h * 0.76f)
            lineTo(w * 0.84f, h * 0.26f)
        }

        drawPath(
            path = checkPath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/**
 * Biểu tượng người dùng (User outline)
 */
@Composable
private fun UserOutlineIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Đầu người
        drawCircle(
            color = color,
            radius = w * 0.22f,
            center = Offset(w * 0.50f, h * 0.30f),
            style = stroke
        )

        // Vai áo uốn cong
        val body = Path().apply {
            moveTo(w * 0.15f, h * 0.86f)
            cubicTo(w * 0.18f, h * 0.62f, w * 0.35f, h * 0.56f, w * 0.50f, h * 0.56f)
            cubicTo(w * 0.65f, h * 0.56f, w * 0.82f, h * 0.62f, w * 0.85f, h * 0.86f)
        }
        drawPath(body, color = color, style = stroke)
    }
}

/**
 * Biểu tượng điện thoại (Phone outline)
 */
@Composable
private fun PhoneOutlineIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        val phonePath = Path().apply {
            moveTo(w * 0.28f, h * 0.22f)
            cubicTo(w * 0.22f, h * 0.28f, w * 0.20f, h * 0.36f, w * 0.28f, h * 0.50f)
            cubicTo(w * 0.38f, h * 0.68f, w * 0.54f, h * 0.82f, w * 0.70f, h * 0.86f)
            cubicTo(w * 0.78f, h * 0.88f, w * 0.86f, h * 0.82f, w * 0.88f, h * 0.74f)
            lineTo(w * 0.78f, h * 0.60f)
            cubicTo(w * 0.74f, h * 0.56f, w * 0.68f, h * 0.58f, w * 0.64f, h * 0.62f)
            lineTo(w * 0.60f, h * 0.66f)
            cubicTo(w * 0.48f, h * 0.60f, w * 0.40f, h * 0.52f, w * 0.34f, h * 0.40f)
            lineTo(w * 0.38f, h * 0.36f)
            cubicTo(w * 0.42f, h * 0.32f, w * 0.44f, h * 0.26f, w * 0.40f, h * 0.22f)
            close()
        }
        drawPath(phonePath, color = color, style = stroke)
    }
}

/**
 * Icon thùng rác nét mảnh (Trash Can Icon)
 */
@Composable
private fun TrashCanIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.8.dp.toPx()

        // Nắp thùng rác
        drawLine(
            color = color,
            start = Offset(w * 0.2f, h * 0.26f),
            end = Offset(w * 0.8f, h * 0.26f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Tay cầm nắp trên
        val handlePath = Path().apply {
            moveTo(w * 0.38f, h * 0.26f)
            lineTo(w * 0.38f, h * 0.16f)
            lineTo(w * 0.62f, h * 0.16f)
            lineTo(w * 0.62f, h * 0.26f)
        }
        drawPath(
            path = handlePath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Thân thùng rác
        val bodyPath = Path().apply {
            moveTo(w * 0.28f, h * 0.26f)
            lineTo(w * 0.32f, h * 0.84f)
            quadraticTo(w * 0.33f, h * 0.88f, w * 0.40f, h * 0.88f)
            lineTo(w * 0.60f, h * 0.88f)
            quadraticTo(w * 0.67f, h * 0.88f, w * 0.68f, h * 0.84f)
            lineTo(w * 0.72f, h * 0.26f)
        }
        drawPath(
            path = bodyPath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Hai sọc dọc giữa thân
        drawLine(
            color = color,
            start = Offset(w * 0.43f, h * 0.40f),
            end = Offset(w * 0.43f, h * 0.74f),
            strokeWidth = strokeWidth * 0.9f,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(w * 0.57f, h * 0.40f),
            end = Offset(w * 0.57f, h * 0.74f),
            strokeWidth = strokeWidth * 0.9f,
            cap = StrokeCap.Round
        )
    }
}

@Preview(
    name = "09 · Chỉnh sửa thành viên (SpendTogether)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun EditMemberScreenPreview() {
    Nhoms8Theme {
        EditMemberScreen()
    }
}
