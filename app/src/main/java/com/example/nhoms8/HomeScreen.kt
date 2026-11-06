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
import androidx.compose.material3.Surface
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

/**
 * Các Tab của thanh điều hướng đáy (Bottom Navigation)
 */
enum class BottomNavTab {
    HOME,
    HISTORY,
    REPORTS,
    PROFILE
}

/**
 * Dữ liệu đại diện cho 1 giao dịch thu chi gần đây
 */
data class RecentTransaction(
    val id: String,
    val title: String,
    val subtitle: String,
    val amountFormatted: String,
    val isIncome: Boolean,
    val iconType: TransactionIconType
)

enum class TransactionIconType {
    GROCERY,
    INTERNET,
    DEPOSIT
}

/**
 * 07 · Màn hình Tổng quan quỹ nhóm (Home / Dashboard Screen)
 * Thiết kế chuẩn Figma SpendTogether: Nền kem ấm, Thẻ quỹ xanh rêu đậm, 4 tác vụ nhanh và lịch sử thu chi
 */
@Composable
fun HomeScreen(
    userName: String = "Lê Văn Hùng",
    userInitials: String = "VH",
    groupName: String = "Quỹ Chi Tiêu Nhóm 08",
    totalBalance: String = "8.490.000 đ",
    monthLabel: String = "Tháng 11",
    depositedAmount: String = "10.000.000 đ",
    spentAmount: String = "1.510.000 đ",
    recentTransactions: List<RecentTransaction> = emptyList(),
    onNotificationClicked: () -> Unit = {},
    onAddExpenseClicked: () -> Unit = {},
    onTopUpFundClicked: () -> Unit = {},
    onSplitBillClicked: () -> Unit = {},
    onMembersClicked: () -> Unit = {},
    onViewAllTransactionsClicked: () -> Unit = {},
    onTransactionClicked: (RecentTransaction) -> Unit = {},
    onChatbotClicked: () -> Unit = {},
    onNavTabSelected: (BottomNavTab) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(BottomNavTab.HOME) }

    // Bảng màu thiết kế chuẩn Figma
    val backgroundColor = Color(0xFFF5F3EC)      // Màu kem nhạt ấm áp
    val ambientSage = Color(0xFFDDEAE0)          // Màu xanh xô thơm nhạt họa tiết nền
    val forestGreenDark = Color(0xFF14382C)      // Xanh rêu đậm thương hiệu
    val textMuted = Color(0xFF6E7C74)            // Màu xám xanh phụ đề
    val avatarBgColor = Color(0xFFC4E2D3)        // Màu nền avatar viết tắt
    val actionGreen = Color(0xFF4A725E)          // Màu xanh nút xem tất cả / liên kết

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
                radius = w * 0.40f,
                center = Offset(x = w * 1.05f, y = h * 0.05f)
            )

            // Mảng cong lớn màu xanh xô thơm ở góc dưới bên trái
            drawCircle(
                color = ambientSage,
                radius = w * 0.58f,
                center = Offset(x = w * 0.04f, y = h * 0.84f)
            )
        }

        // ====================== 2. NỘI DUNG CUỘN CHÍNH ======================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                // ---------- Thanh tiêu đề trên cùng (App Header) ----------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cụm thông tin người dùng (Bên trái)
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar viết tắt MA
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(avatarBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userInitials,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = forestGreenDark
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Chào buổi sáng, $userName",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Normal,
                                color = textMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = groupName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = forestGreenDark
                            )
                        }
                    }

                    // Nút thông báo quả chuông (Bên phải)
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
                            .clickable { onNotificationClicked() },
                        contentAlignment = Alignment.Center
                    ) {
                        OutlineBellIcon(
                            modifier = Modifier.size(19.dp),
                            color = forestGreenDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ---------- 3. Thẻ số dư chính (Wallet Summary Card) ----------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = forestGreenDark.copy(alpha = 0.2f),
                            spotColor = forestGreenDark.copy(alpha = 0.35f)
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .background(forestGreenDark)
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Hàng trên: Nhãn tổng quỹ + Badge tháng
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tổng quỹ hiện tại",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFFA5BCB0)
                            )

                            // Badge con nhộng "Tháng 10"
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.14f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = monthLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Số dư tổng nổi bật (Hero Number)
                        Text(
                            text = totalBalance,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        // Hàng thống kê chi tiết: ĐÃ NẠP & ĐÃ CHI
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Cột Đã nạp
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ĐÃ NẠP",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9FB8AC),
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = depositedAmount,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            // Cột Đã chi
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ĐÃ CHI",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9FB8AC),
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = spentAmount,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // ---------- 4. Thanh tác vụ nhanh (Quick Actions Row) ----------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    QuickActionButton(
                        title = "Thêm chi",
                        containerColor = Color(0xFFEFECE4),
                        icon = { InvoiceActionIcon(modifier = Modifier.size(20.dp), color = forestGreenDark) },
                        onClick = onAddExpenseClicked
                    )

                    QuickActionButton(
                        title = "Nạp quỹ",
                        containerColor = Color(0xFFE0EEE5),
                        icon = { WalletActionIcon(modifier = Modifier.size(20.dp), color = forestGreenDark) },
                        onClick = onTopUpFundClicked
                    )

                    QuickActionButton(
                        title = "Chia tiền",
                        containerColor = Color(0xFFDEE7A8),
                        icon = { SplitActionIcon(modifier = Modifier.size(20.dp), color = forestGreenDark) },
                        onClick = onSplitBillClicked
                    )

                    QuickActionButton(
                        title = "Thành viên",
                        containerColor = Color(0xFFDFEDE7),
                        icon = { MembersActionIcon(modifier = Modifier.size(20.dp), color = forestGreenDark) },
                        onClick = onMembersClicked
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // ---------- 5. Khối danh sách "Thu chi gần đây" ----------
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
                        .padding(horizontal = 18.dp, vertical = 18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Tiêu đề khối + nút "Xem tất cả"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Thu chi gần đây",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = forestGreenDark
                            )

                            Text(
                                text = "Xem tất cả",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = actionGreen,
                                modifier = Modifier
                                    .clickable { onViewAllTransactionsClicked() }
                                    .padding(vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val transactionsList = recentTransactions

                        if (transactionsList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Chưa có giao dịch nào trong quỹ",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textMuted
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Bấm 'Thêm chi' hoặc 'Nạp quỹ' để ghi chép",
                                        fontSize = 12.sp,
                                        color = textMuted.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        } else {
                            // Danh sách giao dịch
                            transactionsList.forEachIndexed { index, item ->
                                TransactionRowItem(
                                    transaction = item,
                                    onClick = { onTransactionClicked(item) }
                                )

                                if (index < transactionsList.size - 1) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // ---------- 6. Thanh điều hướng đáy (Bottom Navigation Bar) ----------
            BottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = {
                    selectedTab = it
                    onNavTabSelected(it)
                }
            )
        }

        // ====================== NÚT NỔI FAB TRỢ LÝ AI FINSPROUT ======================
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 76.dp, end = 16.dp)
        ) {
            Surface(
                onClick = onChatbotClicked,
                shape = RoundedCornerShape(24.dp),
                color = forestGreenDark,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E6349))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E6349)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌱", fontSize = 13.sp)
                    }
                    Text(
                        text = "Hỏi FinSprout",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    // Chấm xanh AI trực tuyến
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4ADE80))
                    )
                }
            }
        }
    }
}

/**
 * Nút tác vụ nhanh dạng Squircle bo góc
 */
@Composable
private fun QuickActionButton(
    title: String,
    containerColor: Color,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF14382C),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Hàng hiển thị chi tiết 1 giao dịch thu/chi
 */
@Composable
private fun TransactionRowItem(
    transaction: RecentTransaction,
    onClick: () -> Unit
) {
    val forestGreenDark = Color(0xFF14382C)
    val incomeGreen = Color(0xFF2E8B57)
    val textMuted = Color(0xFF7E8E84)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Icon tròn đại diện danh mục
            when (transaction.iconType) {
                TransactionIconType.GROCERY -> {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5ECE4)),
                        contentAlignment = Alignment.Center
                    ) {
                        GroceryBasketIcon(
                            modifier = Modifier.size(19.dp),
                            color = Color(0xFFB87545)
                        )
                    }
                }
                TransactionIconType.INTERNET -> {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5ECE4)),
                        contentAlignment = Alignment.Center
                    ) {
                        WifiIcon(
                            modifier = Modifier.size(19.dp),
                            color = Color(0xFFB87545)
                        )
                    }
                }
                TransactionIconType.DEPOSIT -> {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2F4E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        CoinDollarIcon(
                            modifier = Modifier.size(19.dp),
                            color = Color(0xFF2E8B57)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = transaction.title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = forestGreenDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = transaction.subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = textMuted
                )
            }
        }

        // Số tiền
        Text(
            text = transaction.amountFormatted,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (transaction.isIncome) incomeGreen else forestGreenDark
        )
    }
}

/**
 * Thanh điều hướng đáy (Bottom Navigation Bar) cố định ở chân màn hình
 */
@Composable
private fun BottomNavBar(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit
) {
    val activeColor = Color(0xFF14382C)
    val inactiveColor = Color(0xFF7F8E85)
    val activePillBg = Color(0xFFCBE7D7)

    Surface(
        color = Color(0xFFF5F3EC),
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Trang chủ
            BottomNavItem(
                title = "Trang chủ",
                isSelected = selectedTab == BottomNavTab.HOME,
                activePillBg = activePillBg,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                icon = { color -> HomeNavIcon(modifier = Modifier.size(20.dp), color = color) },
                onClick = { onTabSelected(BottomNavTab.HOME) }
            )

            // Tab 2: Lịch sử
            BottomNavItem(
                title = "Lịch sử",
                isSelected = selectedTab == BottomNavTab.HISTORY,
                activePillBg = activePillBg,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                icon = { color -> HistoryNavIcon(modifier = Modifier.size(20.dp), color = color) },
                onClick = { onTabSelected(BottomNavTab.HISTORY) }
            )

            // Tab 3: Báo cáo
            BottomNavItem(
                title = "Báo cáo",
                isSelected = selectedTab == BottomNavTab.REPORTS,
                activePillBg = activePillBg,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                icon = { color -> ReportsNavIcon(modifier = Modifier.size(20.dp), color = color) },
                onClick = { onTabSelected(BottomNavTab.REPORTS) }
            )

            // Tab 4: Cá nhân
            BottomNavItem(
                title = "Cá nhân",
                isSelected = selectedTab == BottomNavTab.PROFILE,
                activePillBg = activePillBg,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                icon = { color -> ProfileNavIcon(modifier = Modifier.size(20.dp), color = color) },
                onClick = { onTabSelected(BottomNavTab.PROFILE) }
            )
        }
    }
}

/**
 * Mục tab đơn lẻ trong Bottom Navigation
 */
@Composable
private fun BottomNavItem(
    title: String,
    isSelected: Boolean,
    activePillBg: Color,
    activeColor: Color,
    inactiveColor: Color,
    icon: @Composable (Color) -> Unit,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(activePillBg)
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                icon(activeColor)
            }
        } else {
            Box(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                icon(inactiveColor)
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = title,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) activeColor else inactiveColor
        )
    }
}

// ==============================================================================
// CÁC HỌA TIẾT ICON ĐƯỢC VẼ TỈ MỈ BẰNG CANVAS (ZERO-DEPENDENCY, KHÔNG LỖI VERSION)
// ==============================================================================

/**
 * Icon Quả Chuông thông báo đường nét (Outline Bell)
 */
@Composable
private fun OutlineBellIcon(
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

        // Quả chuông
        val bellPath = Path().apply {
            moveTo(w * 0.50f, h * 0.16f)
            cubicTo(w * 0.34f, h * 0.16f, w * 0.26f, h * 0.32f, w * 0.26f, h * 0.54f)
            cubicTo(w * 0.26f, h * 0.66f, w * 0.16f, h * 0.74f, w * 0.16f, h * 0.78f)
            lineTo(w * 0.84f, h * 0.78f)
            cubicTo(w * 0.84f, h * 0.74f, w * 0.74f, h * 0.66f, w * 0.74f, h * 0.54f)
            cubicTo(w * 0.74f, h * 0.32f, w * 0.66f, h * 0.16f, w * 0.50f, h * 0.16f)
            close()
        }
        drawPath(path = bellPath, color = color, style = stroke)

        // Quả lắc phía dưới
        val clapper = Path().apply {
            moveTo(w * 0.42f, h * 0.84f)
            cubicTo(w * 0.42f, h * 0.92f, w * 0.58f, h * 0.92f, w * 0.58f, h * 0.84f)
        }
        drawPath(path = clapper, color = color, style = stroke)

        // Đỉnh treo chuông
        drawLine(
            color = color,
            start = Offset(w * 0.50f, h * 0.10f),
            end = Offset(w * 0.50f, h * 0.16f),
            strokeWidth = 1.7.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * Icon Hóa đơn ghi chép (Thêm chi)
 */
@Composable
private fun InvoiceActionIcon(
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

        val receipt = Path().apply {
            moveTo(w * 0.24f, h * 0.14f)
            lineTo(w * 0.76f, h * 0.14f)
            lineTo(w * 0.76f, h * 0.86f)
            lineTo(w * 0.67f, h * 0.80f)
            lineTo(w * 0.59f, h * 0.86f)
            lineTo(w * 0.50f, h * 0.80f)
            lineTo(w * 0.41f, h * 0.86f)
            lineTo(w * 0.33f, h * 0.80f)
            lineTo(w * 0.24f, h * 0.86f)
            close()
        }
        drawPath(path = receipt, color = color, style = stroke)

        // 3 dòng kẻ bên trong
        drawLine(
            color = color,
            start = Offset(w * 0.36f, h * 0.32f),
            end = Offset(w * 0.64f, h * 0.32f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.36f, h * 0.46f),
            end = Offset(w * 0.64f, h * 0.46f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.36f, h * 0.60f),
            end = Offset(w * 0.54f, h * 0.60f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * Icon Ví tiền / Nạp quỹ
 */
@Composable
private fun WalletActionIcon(
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

        // Thân ví / phong bì
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.18f, h * 0.22f),
            size = Size(w * 0.64f, h * 0.56f),
            cornerRadius = CornerRadius(w * 0.12f, h * 0.12f),
            style = stroke
        )

        // Nắp gập ví
        val flap = Path().apply {
            moveTo(w * 0.18f, h * 0.34f)
            lineTo(w * 0.50f, h * 0.52f)
            lineTo(w * 0.82f, h * 0.34f)
        }
        drawPath(path = flap, color = color, style = stroke)
    }
}

/**
 * Icon Tách nhánh / Chia tiền (Split Bill)
 */
@Composable
private fun SplitActionIcon(
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

        // Nhánh gốc từ dưới lên
        drawLine(
            color = color,
            start = Offset(w * 0.50f, h * 0.84f),
            end = Offset(w * 0.50f, h * 0.52f),
            strokeWidth = 1.7.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Nhánh rẽ sang trái
        val leftBranch = Path().apply {
            moveTo(w * 0.50f, h * 0.52f)
            lineTo(w * 0.30f, h * 0.24f)
        }
        drawPath(path = leftBranch, color = color, style = stroke)

        // Mũi tên trái
        val leftHead = Path().apply {
            moveTo(w * 0.40f, h * 0.24f)
            lineTo(w * 0.30f, h * 0.24f)
            lineTo(w * 0.30f, h * 0.36f)
        }
        drawPath(path = leftHead, color = color, style = stroke)

        // Nhánh rẽ sang phải
        val rightBranch = Path().apply {
            moveTo(w * 0.50f, h * 0.52f)
            lineTo(w * 0.70f, h * 0.24f)
        }
        drawPath(path = rightBranch, color = color, style = stroke)

        // Mũi tên phải
        val rightHead = Path().apply {
            moveTo(w * 0.60f, h * 0.24f)
            lineTo(w * 0.70f, h * 0.24f)
            lineTo(w * 0.70f, h * 0.36f)
        }
        drawPath(path = rightHead, color = color, style = stroke)
    }
}

/**
 * Icon Nhóm thành viên (Members)
 */
@Composable
private fun MembersActionIcon(
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

        // Người chính giữa
        drawCircle(
            color = color,
            radius = w * 0.14f,
            center = Offset(w * 0.50f, h * 0.32f),
            style = stroke
        )
        val bodyCenter = Path().apply {
            moveTo(w * 0.28f, h * 0.78f)
            cubicTo(w * 0.28f, h * 0.58f, w * 0.72f, h * 0.58f, w * 0.72f, h * 0.78f)
        }
        drawPath(path = bodyCenter, color = color, style = stroke)

        // Người phụ bên cạnh
        val shoulderRight = Path().apply {
            moveTo(w * 0.70f, h * 0.62f)
            cubicTo(w * 0.84f, h * 0.64f, w * 0.90f, h * 0.74f, w * 0.90f, h * 0.78f)
        }
        drawPath(path = shoulderRight, color = color, style = stroke)
    }
}

/**
 * Icon Giỏ đi chợ (Grocery)
 */
@Composable
private fun GroceryBasketIcon(
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

        // Thân giỏ
        val basket = Path().apply {
            moveTo(w * 0.20f, h * 0.44f)
            lineTo(w * 0.80f, h * 0.44f)
            lineTo(w * 0.72f, h * 0.82f)
            lineTo(w * 0.28f, h * 0.82f)
            close()
        }
        drawPath(path = basket, color = color, style = stroke)

        // Quai giỏ
        val handle = Path().apply {
            moveTo(w * 0.32f, h * 0.44f)
            cubicTo(w * 0.32f, h * 0.20f, w * 0.68f, h * 0.20f, w * 0.68f, h * 0.44f)
        }
        drawPath(path = handle, color = color, style = stroke)

        // Các đường nan giỏ
        drawLine(
            color = color,
            start = Offset(w * 0.42f, h * 0.48f),
            end = Offset(w * 0.38f, h * 0.78f),
            strokeWidth = 1.4.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.58f, h * 0.48f),
            end = Offset(w * 0.62f, h * 0.78f),
            strokeWidth = 1.4.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * Icon Sóng Wi-Fi / Internet
 */
@Composable
private fun WifiIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = 1.6.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Vòng sóng lớn
        drawArc(
            color = color,
            startAngle = 215f,
            sweepAngle = 110f,
            useCenter = false,
            topLeft = Offset(w * 0.16f, h * 0.22f),
            size = Size(w * 0.68f, h * 0.68f),
            style = stroke
        )

        // Vòng sóng vừa
        drawArc(
            color = color,
            startAngle = 215f,
            sweepAngle = 110f,
            useCenter = false,
            topLeft = Offset(w * 0.28f, h * 0.36f),
            size = Size(w * 0.44f, h * 0.44f),
            style = stroke
        )

        // Điểm phát sóng trung tâm
        drawCircle(
            color = color,
            radius = w * 0.07f,
            center = Offset(w * 0.50f, h * 0.74f)
        )
    }
}

/**
 * Icon Đồng tiền xu `$`/nạp quỹ
 */
@Composable
private fun CoinDollarIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = 1.7.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Vòng đồng xu ngoài
        drawCircle(
            color = color,
            radius = w * 0.38f,
            center = Offset(w * 0.50f, h * 0.50f),
            style = stroke
        )

        // Ký hiệu $
        drawLine(
            color = color,
            start = Offset(w * 0.50f, h * 0.26f),
            end = Offset(w * 0.50f, h * 0.74f),
            strokeWidth = 1.6.dp.toPx(),
            cap = StrokeCap.Round
        )
        val sCurve = Path().apply {
            moveTo(w * 0.62f, h * 0.38f)
            cubicTo(w * 0.62f, h * 0.32f, w * 0.38f, h * 0.32f, w * 0.38f, h * 0.44f)
            cubicTo(w * 0.38f, h * 0.56f, w * 0.62f, h * 0.56f, w * 0.62f, h * 0.64f)
            cubicTo(w * 0.62f, h * 0.72f, w * 0.38f, h * 0.72f, w * 0.38f, h * 0.64f)
        }
        drawPath(path = sCurve, color = color, style = stroke)
    }
}

/**
 * Icon Trang chủ (Home)
 */
@Composable
private fun HomeNavIcon(
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

        val roof = Path().apply {
            moveTo(w * 0.16f, h * 0.48f)
            lineTo(w * 0.50f, h * 0.18f)
            lineTo(w * 0.84f, h * 0.48f)
        }
        drawPath(path = roof, color = color, style = stroke)

        val walls = Path().apply {
            moveTo(w * 0.24f, h * 0.44f)
            lineTo(w * 0.24f, h * 0.84f)
            lineTo(w * 0.76f, h * 0.84f)
            lineTo(w * 0.76f, h * 0.44f)
        }
        drawPath(path = walls, color = color, style = stroke)

        // Cửa nhà
        val door = Path().apply {
            moveTo(w * 0.42f, h * 0.84f)
            lineTo(w * 0.42f, h * 0.60f)
            lineTo(w * 0.58f, h * 0.60f)
            lineTo(w * 0.58f, h * 0.84f)
        }
        drawPath(path = door, color = color, style = stroke)
    }
}

/**
 * Icon Lịch sử (History)
 */
@Composable
private fun HistoryNavIcon(
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

        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.22f, h * 0.16f),
            size = Size(w * 0.56f, h * 0.68f),
            cornerRadius = CornerRadius(w * 0.08f, h * 0.08f),
            style = stroke
        )

        drawLine(
            color = color,
            start = Offset(w * 0.34f, h * 0.34f),
            end = Offset(w * 0.66f, h * 0.34f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.34f, h * 0.48f),
            end = Offset(w * 0.66f, h * 0.48f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.34f, h * 0.62f),
            end = Offset(w * 0.52f, h * 0.62f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * Icon Báo cáo biểu đồ (Reports)
 */
@Composable
private fun ReportsNavIcon(
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

        // Đồng hồ / Biểu đồ tròn
        drawCircle(
            color = color,
            radius = w * 0.36f,
            center = Offset(w * 0.50f, h * 0.50f),
            style = stroke
        )

        // Kim chỉ / Miếng góc phần tư
        drawLine(
            color = color,
            start = Offset(w * 0.50f, h * 0.50f),
            end = Offset(w * 0.50f, h * 0.24f),
            strokeWidth = 1.6.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.50f, h * 0.50f),
            end = Offset(w * 0.72f, h * 0.50f),
            strokeWidth = 1.6.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * Icon Cá nhân (Profile)
 */
@Composable
private fun ProfileNavIcon(
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

        // Đầu người
        drawCircle(
            color = color,
            radius = w * 0.18f,
            center = Offset(w * 0.50f, h * 0.34f),
            style = stroke
        )

        // Vai & thân người
        val body = Path().apply {
            moveTo(w * 0.22f, h * 0.82f)
            cubicTo(w * 0.22f, h * 0.60f, w * 0.78f, h * 0.60f, w * 0.78f, h * 0.82f)
        }
        drawPath(path = body, color = color, style = stroke)
    }
}

// ====================== PREVIEW ======================
@Preview(
    name = "07 · Tổng quan quỹ nhóm (SpendTogether)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun HomeScreenPreview() {
    Nhoms8Theme {
        HomeScreen()
    }
}
