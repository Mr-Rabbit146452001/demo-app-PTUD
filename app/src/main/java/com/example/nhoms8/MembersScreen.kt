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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhoms8.ui.theme.Nhoms8Theme

/**
 * Trạng thái số dư tài chính của thành viên trong nhóm
 */
sealed class MemberBalanceStatus {
    data class Positive(val amountFormatted: String) : MemberBalanceStatus()
    data class Negative(val amountFormatted: String) : MemberBalanceStatus()
    data object Balanced : MemberBalanceStatus()
}

/**
 * Dữ liệu đại diện cho 1 thành viên nhóm
 */
data class GroupMember(
    val id: String,
    val name: String,
    val initials: String,
    val phoneNumber: String,
    val isLeader: Boolean = false,
    val avatarBgColor: Color,
    val balanceStatus: MemberBalanceStatus
)

/**
 * 08 · Màn hình Danh sách thành viên (Group Members Screen)
 * Thiết kế chuẩn Figma SpendTogether: Thẻ đóng góp xanh rêu, danh sách thành viên chi tiết kèm số dư & badge vai trò
 */
@Composable
fun MembersScreen(
    groupTitle: String = "Thành viên",
    groupSubtitle: String = "5 người trong quỹ Nhà Mình",
    totalContributedAmount: String = "21.500.000 đ",
    membersList: List<GroupMember>? = null,
    onBackClicked: () -> Unit = {},
    onAddMemberClicked: () -> Unit = {},
    onMemberClicked: (GroupMember) -> Unit = {}
) {
    // Bảng màu thiết kế chuẩn Figma
    val backgroundColor = Color(0xFFF5F3EC)      // Màu kem nhạt ấm áp
    val ambientSage = Color(0xFFDDEAE0)          // Màu xanh xô thơm nhạt họa tiết nền
    val forestGreenDark = Color(0xFF14382C)      // Xanh rêu đậm thương hiệu
    val textMuted = Color(0xFF6E7C74)            // Màu xám xanh phụ đề
    val positiveGreen = Color(0xFF2E8B57)        // Màu xanh biểu thị số dư dương
    val negativeRed = Color(0xFFD34A4A)          // Màu đỏ cam biểu thị số dư âm
    val balancedGreen = Color(0xFF4A725E)        // Màu trạng thái đã cân bằng

    // Danh sách 5 thành viên mẫu theo thiết kế
    val defaultMembers = remember {
        listOf(
            GroupMember(
                id = "1",
                name = "Minh Anh",
                initials = "MA",
                phoneNumber = "0912 345 678",
                isLeader = true,
                avatarBgColor = Color(0xFFC4E2D3),
                balanceStatus = MemberBalanceStatus.Positive("+1.180.000 đ")
            ),
            GroupMember(
                id = "2",
                name = "Lan Anh",
                initials = "LA",
                phoneNumber = "0908 224 510",
                isLeader = false,
                avatarBgColor = Color(0xFFDEE7A8),
                balanceStatus = MemberBalanceStatus.Positive("+425.000 đ")
            ),
            GroupMember(
                id = "3",
                name = "Tuấn Nam",
                initials = "TN",
                phoneNumber = "0986 420 115",
                isLeader = false,
                avatarBgColor = Color(0xFFCBE3DC),
                balanceStatus = MemberBalanceStatus.Negative("-425.000 đ")
            ),
            GroupMember(
                id = "4",
                name = "Hà My",
                initials = "HM",
                phoneNumber = "0935 762 428",
                isLeader = false,
                avatarBgColor = Color(0xFFF2D7C8),
                balanceStatus = MemberBalanceStatus.Balanced
            ),
            GroupMember(
                id = "5",
                name = "Quang Phúc",
                initials = "QP",
                phoneNumber = "0972 108 663",
                isLeader = false,
                avatarBgColor = Color(0xFFD2DEC8),
                balanceStatus = MemberBalanceStatus.Negative("-180.000 đ")
            )
        )
    }

    val members = membersList ?: defaultMembers

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
                radius = w * 0.60f,
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
                // Cụm Nút Back + Tiêu đề
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Nút Back tròn trắng đổ bóng nhẹ
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

                // Nút Thêm nhanh User-Plus (Bên phải)
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
                        .clickable { onAddMemberClicked() },
                    contentAlignment = Alignment.Center
                ) {
                    UserPlusIcon(
                        modifier = Modifier.size(19.dp),
                        color = forestGreenDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Phần cuộn gồm Thẻ đóng góp + Danh sách thành viên + Nút tác vụ
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // ---------- 3. Thẻ tổng quan đóng góp (Contribution Summary Card) ----------
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
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Bên trái: TỔNG ĐÃ GÓP + Số tiền lớn
                        Column {
                            Text(
                                text = "TỔNG ĐÃ GÓP",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9FB8AC),
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = totalContributedAmount,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = (-0.3).sp
                            )
                        }

                        // Bên phải: Badge "Đã xác minh" có icon khiên
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f))
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.22f),
                                    shape = CircleShape
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ShieldCheckIcon(
                                    modifier = Modifier.size(14.dp),
                                    color = Color(0xFFD4E8DC)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Đã xác minh",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFD4E8DC)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ---------- 4. Danh sách thành viên (Members List Card) ----------
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
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        members.forEachIndexed { index, member ->
                            MemberListItemRow(
                                member = member,
                                forestGreenDark = forestGreenDark,
                                positiveGreen = positiveGreen,
                                negativeRed = negativeRed,
                                balancedGreen = balancedGreen,
                                textMuted = textMuted,
                                onClick = { onMemberClicked(member) }
                            )

                            // Đường phân cách mỏng giữa các thành viên
                            if (index < members.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    thickness = 0.8.dp,
                                    color = Color(0xFFEFF2EE)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ---------- 5. Nút hành động chính: Thêm thành viên ----------
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
                        .clickable { onAddMemberClicked() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        UserPlusIcon(
                            modifier = Modifier.size(19.dp),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Thêm thành viên",
                            fontSize = 15.5.sp,
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
 * Hàng hiển thị chi tiết 1 thành viên trong danh sách
 */
@Composable
private fun MemberListItemRow(
    member: GroupMember,
    forestGreenDark: Color,
    positiveGreen: Color,
    negativeRed: Color,
    balancedGreen: Color,
    textMuted: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Cột trái: Avatar + Tên (+ Badge nếu là Leader) + SĐT
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Avatar tròn viết tắt
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(member.avatarBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.initials,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = forestGreenDark
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = member.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = forestGreenDark
                    )

                    // Badge Trưởng nhóm nếu có
                    if (member.isLeader) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFDCEFE4))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Trưởng nhóm",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D523E)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = member.phoneNumber,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = textMuted
                )
            }
        }

        // Cột phải: Số dư tài chính + Mũi tên >
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (val balance = member.balanceStatus) {
                is MemberBalanceStatus.Positive -> {
                    Text(
                        text = balance.amountFormatted,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = positiveGreen
                    )
                }
                is MemberBalanceStatus.Negative -> {
                    Text(
                        text = balance.amountFormatted,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = negativeRed
                    )
                }
                MemberBalanceStatus.Balanced -> {
                    Text(
                        text = "Đã cân bằng",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = balancedGreen
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            ChevronRightIcon(
                modifier = Modifier.size(13.dp),
                color = Color(0xFFA0ACA4)
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

        // Đường thẳng ngang
        drawLine(
            color = color,
            start = Offset(w * 0.82f, h * 0.50f),
            end = Offset(w * 0.18f, h * 0.50f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Đầu mũi tên sang trái
        val arrowHead = Path().apply {
            moveTo(w * 0.44f, h * 0.26f)
            lineTo(w * 0.18f, h * 0.50f)
            lineTo(w * 0.44f, h * 0.74f)
        }
        drawPath(path = arrowHead, color = color, style = stroke)
    }
}

/**
 * Icon Thêm người dùng (User Plus)
 */
@Composable
private fun UserPlusIcon(
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

        // Đầu người (dịch sang trái một chút để chừa chỗ cho dấu +)
        drawCircle(
            color = color,
            radius = w * 0.16f,
            center = Offset(w * 0.38f, h * 0.34f),
            style = stroke
        )

        // Thân người
        val body = Path().apply {
            moveTo(w * 0.12f, h * 0.82f)
            cubicTo(w * 0.12f, h * 0.62f, w * 0.64f, h * 0.62f, w * 0.64f, h * 0.82f)
        }
        drawPath(path = body, color = color, style = stroke)

        // Dấu cộng (+) ở góc trên bên phải
        val plusCenterX = w * 0.78f
        val plusCenterY = h * 0.40f
        val plusRadius = w * 0.11f

        drawLine(
            color = color,
            start = Offset(plusCenterX - plusRadius, plusCenterY),
            end = Offset(plusCenterX + plusRadius, plusCenterY),
            strokeWidth = 1.6.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(plusCenterX, plusCenterY - plusRadius),
            end = Offset(plusCenterX, plusCenterY + plusRadius),
            strokeWidth = 1.6.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * Icon Khiên bảo vệ xác minh (Shield Check)
 */
@Composable
private fun ShieldCheckIcon(
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

        // Đường viền khiên
        val shield = Path().apply {
            moveTo(w * 0.50f, h * 0.12f)
            lineTo(w * 0.84f, h * 0.24f)
            cubicTo(w * 0.84f, h * 0.60f, w * 0.68f, h * 0.82f, w * 0.50f, h * 0.90f)
            cubicTo(w * 0.32f, h * 0.82f, w * 0.16f, h * 0.60f, w * 0.16f, h * 0.24f)
            close()
        }
        drawPath(path = shield, color = color, style = stroke)

        // Dấu tích checkmark bên trong khiên
        val check = Path().apply {
            moveTo(w * 0.36f, h * 0.50f)
            lineTo(w * 0.46f, h * 0.60f)
            lineTo(w * 0.64f, h * 0.40f)
        }
        drawPath(path = check, color = color, style = stroke)
    }
}

/**
 * Icon Mũi tên chỉ sang phải (Chevron Right `>`)
 */
@Composable
private fun ChevronRightIcon(
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

        val chevron = Path().apply {
            moveTo(w * 0.30f, h * 0.20f)
            lineTo(w * 0.70f, h * 0.50f)
            lineTo(w * 0.30f, h * 0.80f)
        }
        drawPath(path = chevron, color = color, style = stroke)
    }
}

// ====================== PREVIEW ======================
@Preview(showBackground = true, device = "id:pixel_7", name = "08 - Group Members Screen")
@Composable
fun MembersScreenPreview() {
    Nhoms8Theme {
        MembersScreen()
    }
}
