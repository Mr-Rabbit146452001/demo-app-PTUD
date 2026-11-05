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
 * Loại icon của từng giao dịch trong lịch sử
 */
enum class HistoryItemType {
    GROCERY,
    INTERNET,
    DEPOSIT,
    COFFEE,
    TAXI
}

/**
 * Dữ liệu chi tiết của 1 giao dịch trong lịch sử
 */
data class HistoryTransactionItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val amountFormatted: String,
    val isIncome: Boolean,
    val type: HistoryItemType
)

/**
 * Nhóm giao dịch theo ngày
 */
data class TransactionDateGroup(
    val dateHeader: String,
    val netTotalFormatted: String,
    val transactions: List<HistoryTransactionItem>
)

/**
 * 13 · Màn hình Lịch sử giao dịch và Bộ lọc (Transaction History & Filter Screen)
 * Thiết kế chuẩn Figma SpendTogether: Nền kem, Filter chips dạng pill, Nhóm giao dịch theo ngày và Bottom Navigation
 */
@Composable
fun TransactionHistoryScreen(
    totalTransactionsCount: Int = 23,
    monthText: String = "Tháng 10/2026",
    transactionGroups: List<TransactionDateGroup>? = null,
    onSearchClicked: () -> Unit = {},
    onFilterMemberClicked: () -> Unit = {},
    onFilterMonthClicked: () -> Unit = {},
    onFilterCategoryClicked: () -> Unit = {},
    onClearFilterClicked: () -> Unit = {},
    onTransactionClicked: (HistoryTransactionItem) -> Unit = {},
    onNavTabSelected: (BottomNavTab) -> Unit = {}
) {
    var selectedFilterMonth by remember { mutableStateOf("Tháng 10") }
    var selectedNavTab by remember { mutableStateOf(BottomNavTab.HISTORY) }

    // Bảng màu thiết kế chuẩn Figma
    val backgroundColor = Color(0xFFF5F3EC)      // Màu kem nhạt ấm áp
    val ambientSage = Color(0xFFDDEAE0)          // Màu xanh xô thơm nhạt họa tiết nền
    val forestGreenDark = Color(0xFF14382C)      // Xanh rêu đậm thương hiệu
    val textMuted = Color(0xFF6E7C74)            // Màu xám xanh phụ đề
    val incomeGreen = Color(0xFF2E8B57)          // Màu xanh thu nhập
    val activePillBg = Color(0xFFCBE7D7)         // Nền pill active bottom nav

    // Dữ liệu mẫu 2 nhóm ngày giao dịch theo thiết kế
    val defaultGroups = remember {
        listOf(
            TransactionDateGroup(
                dateHeader = "Hôm nay, 03/10",
                netTotalFormatted = "-1.170.000 đ",
                transactions = listOf(
                    HistoryTransactionItem(
                        id = "1",
                        title = "Đi chợ cuối tuần",
                        subtitle = "Lan Anh · Ăn uống · 10:30",
                        amountFormatted = "-850.000 đ",
                        isIncome = false,
                        type = HistoryItemType.GROCERY
                    ),
                    HistoryTransactionItem(
                        id = "2",
                        title = "Internet tháng 10",
                        subtitle = "Minh Anh · Nhà ở · 08:15",
                        amountFormatted = "-320.000 đ",
                        isIncome = false,
                        type = HistoryItemType.INTERNET
                    )
                )
            ),
            TransactionDateGroup(
                dateHeader = "Thứ sáu, 02/10",
                netTotalFormatted = "+1.820.000 đ",
                transactions = listOf(
                    HistoryTransactionItem(
                        id = "3",
                        title = "Nam nạp quỹ",
                        subtitle = "Tuấn Nam · Thu quỹ · 19:42",
                        amountFormatted = "+2.000.000 đ",
                        isIncome = true,
                        type = HistoryItemType.DEPOSIT
                    ),
                    HistoryTransactionItem(
                        id = "4",
                        title = "Cà phê họp nhóm",
                        subtitle = "Hà My · Ăn uống · 15:20",
                        amountFormatted = "-180.000 đ",
                        isIncome = false,
                        type = HistoryItemType.COFFEE
                    ),
                    HistoryTransactionItem(
                        id = "5",
                        title = "Taxi về nhà",
                        subtitle = "Quang Phúc · Đi lại · 12:05",
                        amountFormatted = "-210.000 đ",
                        isIncome = false,
                        type = HistoryItemType.TAXI
                    )
                )
            )
        )
    }

    val displayGroups = transactionGroups ?: defaultGroups

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
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                // ---------- Thanh tiêu đề trên cùng (Header) ----------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Lịch sử giao dịch",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = forestGreenDark
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "$totalTransactionsCount giao dịch · $monthText",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Normal,
                            color = textMuted
                        )
                    }

                    // Nút Tìm kiếm kính lúp tròn trắng
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(
                                elevation = 3.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.04f),
                                spotColor = Color.Black.copy(alpha = 0.08f)
                            )
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { onSearchClicked() },
                        contentAlignment = Alignment.Center
                    ) {
                        SearchIcon(
                            modifier = Modifier.size(19.dp),
                            color = forestGreenDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ---------- 3. Hàng bộ lọc (Filter Chips Row) ----------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Chip 1: Thành viên (Inactive)
                    FilterChipButton(
                        label = "Thành viên",
                        isSelected = false,
                        icon = { color -> UserFilterIcon(modifier = Modifier.size(15.dp), color = color) },
                        onClick = onFilterMemberClicked
                    )

                    // Chip 2: Tháng 10 (Active)
                    FilterChipButton(
                        label = selectedFilterMonth,
                        isSelected = true,
                        icon = { color -> CalendarFilterIcon(modifier = Modifier.size(15.dp), color = color) },
                        onClick = onFilterMonthClicked
                    )

                    // Chip 3: Danh mục (Inactive)
                    FilterChipButton(
                        label = "Danh mục",
                        isSelected = false,
                        icon = { color -> CategoryShapesIcon(modifier = Modifier.size(15.dp), color = color) },
                        onClick = onFilterCategoryClicked
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ---------- 4. Dòng trạng thái bộ lọc (Filter Status Bar) ----------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFDDEFE4))
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Đang hiển thị tất cả thành viên",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF264B3B)
                        )

                        Text(
                            text = "Xóa lọc",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E6B4E),
                            modifier = Modifier
                                .clickable { onClearFilterClicked() }
                                .padding(vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ---------- 5. Danh sách các nhóm giao dịch theo ngày ----------
                displayGroups.forEachIndexed { groupIndex, group ->
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
                            .padding(horizontal = 18.dp, vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Tiêu đề nhóm ngày + Tổng thu chi ngày
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = group.dateHeader,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = forestGreenDark
                                )

                                Text(
                                    text = group.netTotalFormatted,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (group.netTotalFormatted.startsWith("+")) incomeGreen else textMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Các giao dịch trong ngày
                            group.transactions.forEachIndexed { itemIndex, item ->
                                HistoryTransactionRow(
                                    item = item,
                                    forestGreenDark = forestGreenDark,
                                    incomeGreen = incomeGreen,
                                    textMuted = textMuted,
                                    onClick = { onTransactionClicked(item) }
                                )

                                if (itemIndex < group.transactions.size - 1) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                }
                            }
                        }
                    }

                    if (groupIndex < displayGroups.size - 1) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ---------- 6. Dòng gợi ý chạm giao dịch ----------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TouchPointerIcon(
                        modifier = Modifier.size(15.dp),
                        color = Color(0xFF7E8E84)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Chạm một giao dịch để xem chi tiết",
                        fontSize = 12.5.sp,
                        color = Color(0xFF7E8E84),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // ---------- 7. Thanh điều hướng đáy (Bottom Navigation Bar) ----------
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
                    HistoryBottomNavItem(
                        title = "Trang chủ",
                        isSelected = selectedNavTab == BottomNavTab.HOME,
                        activePillBg = activePillBg,
                        activeColor = forestGreenDark,
                        inactiveColor = textMuted,
                        icon = { color -> HomeNavSimpleIcon(modifier = Modifier.size(20.dp), color = color) },
                        onClick = {
                            selectedNavTab = BottomNavTab.HOME
                            onNavTabSelected(BottomNavTab.HOME)
                        }
                    )

                    // Tab 2: Lịch sử (Active)
                    HistoryBottomNavItem(
                        title = "Lịch sử",
                        isSelected = selectedNavTab == BottomNavTab.HISTORY,
                        activePillBg = activePillBg,
                        activeColor = forestGreenDark,
                        inactiveColor = textMuted,
                        icon = { color -> HistoryNavSimpleIcon(modifier = Modifier.size(20.dp), color = color) },
                        onClick = {
                            selectedNavTab = BottomNavTab.HISTORY
                            onNavTabSelected(BottomNavTab.HISTORY)
                        }
                    )

                    // Tab 3: Báo cáo
                    HistoryBottomNavItem(
                        title = "Báo cáo",
                        isSelected = selectedNavTab == BottomNavTab.REPORTS,
                        activePillBg = activePillBg,
                        activeColor = forestGreenDark,
                        inactiveColor = textMuted,
                        icon = { color -> ReportsNavSimpleIcon(modifier = Modifier.size(20.dp), color = color) },
                        onClick = {
                            selectedNavTab = BottomNavTab.REPORTS
                            onNavTabSelected(BottomNavTab.REPORTS)
                        }
                    )

                    // Tab 4: Cá nhân
                    HistoryBottomNavItem(
                        title = "Cá nhân",
                        isSelected = selectedNavTab == BottomNavTab.PROFILE,
                        activePillBg = activePillBg,
                        activeColor = forestGreenDark,
                        inactiveColor = textMuted,
                        icon = { color -> ProfileNavSimpleIcon(modifier = Modifier.size(20.dp), color = color) },
                        onClick = {
                            selectedNavTab = BottomNavTab.PROFILE
                            onNavTabSelected(BottomNavTab.PROFILE)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Nút Filter Chip dạng viên thuốc
 */
@Composable
private fun FilterChipButton(
    label: String,
    isSelected: Boolean,
    icon: @Composable (Color) -> Unit,
    onClick: () -> Unit
) {
    val chipBg = if (isSelected) Color(0xFF14382C) else Color.White
    val contentColor = if (isSelected) Color.White else Color(0xFF1E2D25)

    Box(
        modifier = Modifier
            .height(38.dp)
            .shadow(
                elevation = if (isSelected) 3.dp else 2.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.07f)
            )
            .clip(CircleShape)
            .background(chipBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 14.dp),
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

/**
 * Hàng giao dịch trong danh sách lịch sử
 */
@Composable
private fun HistoryTransactionRow(
    item: HistoryTransactionItem,
    forestGreenDark: Color,
    incomeGreen: Color,
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Icon tròn danh mục
            when (item.type) {
                HistoryItemType.GROCERY -> {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5ECE4)),
                        contentAlignment = Alignment.Center
                    ) {
                        GroceryBasketSimpleIcon(modifier = Modifier.size(19.dp), color = Color(0xFFB87545))
                    }
                }
                HistoryItemType.INTERNET -> {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5ECE4)),
                        contentAlignment = Alignment.Center
                    ) {
                        WifiSimpleIcon(modifier = Modifier.size(19.dp), color = Color(0xFFB87545))
                    }
                }
                HistoryItemType.DEPOSIT -> {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2F4E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        CoinDollarSimpleIcon(modifier = Modifier.size(19.dp), color = Color(0xFF2E8B57))
                    }
                }
                HistoryItemType.COFFEE -> {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5ECE4)),
                        contentAlignment = Alignment.Center
                    ) {
                        CoffeeCupIcon(modifier = Modifier.size(19.dp), color = Color(0xFFB87545))
                    }
                }
                HistoryItemType.TAXI -> {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5ECE4)),
                        contentAlignment = Alignment.Center
                    ) {
                        TaxiCarIcon(modifier = Modifier.size(19.dp), color = Color(0xFFB87545))
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = item.title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = forestGreenDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = textMuted
                )
            }
        }

        Text(
            text = item.amountFormatted,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (item.isIncome) incomeGreen else forestGreenDark
        )
    }
}

/**
 * Mục tab đơn lẻ trong Bottom Navigation
 */
@Composable
private fun HistoryBottomNavItem(
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
 * Icon Kính lúp tìm kiếm (Search Icon)
 */
@Composable
private fun SearchIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Vòng tròn kính lúp
        drawCircle(
            color = color,
            radius = w * 0.28f,
            center = Offset(w * 0.44f, h * 0.44f),
            style = stroke
        )

        // Tay cầm kính lúp
        drawLine(
            color = color,
            start = Offset(w * 0.64f, h * 0.64f),
            end = Offset(w * 0.84f, h * 0.84f),
            strokeWidth = 2.0.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * Icon Người dùng cho Filter (User Filter Icon)
 */
@Composable
private fun UserFilterIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawCircle(
            color = color,
            radius = w * 0.20f,
            center = Offset(w * 0.50f, h * 0.35f),
            style = stroke
        )

        val body = Path().apply {
            moveTo(w * 0.22f, h * 0.82f)
            cubicTo(w * 0.22f, h * 0.62f, w * 0.78f, h * 0.62f, w * 0.78f, h * 0.82f)
        }
        drawPath(path = body, color = color, style = stroke)
    }
}

/**
 * Icon Lịch cho Filter (Calendar Filter Icon)
 */
@Composable
private fun CalendarFilterIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.20f, h * 0.22f),
            size = Size(w * 0.60f, h * 0.60f),
            cornerRadius = CornerRadius(w * 0.10f, h * 0.10f),
            style = stroke
        )

        drawLine(
            color = color,
            start = Offset(w * 0.20f, h * 0.40f),
            end = Offset(w * 0.80f, h * 0.40f),
            strokeWidth = 1.3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * Icon Danh mục hình khối cho Filter (Category Shapes Icon)
 */
@Composable
private fun CategoryShapesIcon(
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

        drawCircle(
            color = color,
            radius = w * 0.12f,
            center = Offset(w * 0.50f, h * 0.32f),
            style = stroke
        )

        drawRect(
            color = color,
            topLeft = Offset(w * 0.22f, h * 0.56f),
            size = Size(w * 0.22f, h * 0.22f),
            style = stroke
        )

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
 * Icon Tách Cà phê (Coffee Cup)
 */
@Composable
private fun CoffeeCupIcon(
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

        // Thân tách cà phê
        val cup = Path().apply {
            moveTo(w * 0.22f, h * 0.35f)
            lineTo(w * 0.72f, h * 0.35f)
            lineTo(w * 0.66f, h * 0.72f)
            cubicTo(w * 0.66f, h * 0.80f, w * 0.28f, h * 0.80f, w * 0.28f, h * 0.72f)
            close()
        }
        drawPath(path = cup, color = color, style = stroke)

        // Quai tách
        val handle = Path().apply {
            moveTo(w * 0.72f, h * 0.42f)
            cubicTo(w * 0.86f, h * 0.42f, w * 0.86f, h * 0.62f, w * 0.68f, h * 0.62f)
        }
        drawPath(path = handle, color = color, style = stroke)

        // Khói bốc lên
        drawLine(
            color = color,
            start = Offset(w * 0.40f, h * 0.20f),
            end = Offset(w * 0.40f, h * 0.27f),
            strokeWidth = 1.3.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.54f, h * 0.18f),
            end = Offset(w * 0.54f, h * 0.25f),
            strokeWidth = 1.3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * Icon Xe Taxi (Taxi Car)
 */
@Composable
private fun TaxiCarIcon(
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

        // Thân xe ô tô
        val carBody = Path().apply {
            moveTo(w * 0.18f, h * 0.60f)
            lineTo(w * 0.28f, h * 0.42f)
            lineTo(w * 0.72f, h * 0.42f)
            lineTo(w * 0.82f, h * 0.60f)
            lineTo(w * 0.86f, h * 0.68f)
            lineTo(w * 0.14f, h * 0.68f)
            close()
        }
        drawPath(path = carBody, color = color, style = stroke)

        // Bánh xe
        drawCircle(color = color, radius = w * 0.08f, center = Offset(w * 0.32f, h * 0.72f))
        drawCircle(color = color, radius = w * 0.08f, center = Offset(w * 0.68f, h * 0.72f))

        // Đèn taxi trên mui
        drawLine(
            color = color,
            start = Offset(w * 0.44f, h * 0.34f),
            end = Offset(w * 0.56f, h * 0.34f),
            strokeWidth = 1.6.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * Icon Con trỏ chạm (Touch Pointer)
 */
@Composable
private fun TouchPointerIcon(
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

        // Mũi tên con trỏ / bàn tay
        val pointer = Path().apply {
            moveTo(w * 0.25f, h * 0.20f)
            lineTo(w * 0.25f, h * 0.75f)
            lineTo(w * 0.45f, h * 0.60f)
            lineTo(w * 0.65f, h * 0.80f)
            lineTo(w * 0.75f, h * 0.70f)
            lineTo(w * 0.55f, h * 0.50f)
            lineTo(w * 0.75f, h * 0.50f)
            close()
        }
        drawPath(path = pointer, color = color, style = stroke)
    }
}

/**
 * Icon Giỏ hàng đơn giản
 */
@Composable
private fun GroceryBasketSimpleIcon(
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

        val basket = Path().apply {
            moveTo(w * 0.20f, h * 0.44f)
            lineTo(w * 0.80f, h * 0.44f)
            lineTo(w * 0.72f, h * 0.82f)
            lineTo(w * 0.28f, h * 0.82f)
            close()
        }
        drawPath(path = basket, color = color, style = stroke)

        val handle = Path().apply {
            moveTo(w * 0.34f, h * 0.44f)
            cubicTo(w * 0.34f, h * 0.22f, w * 0.66f, h * 0.22f, w * 0.66f, h * 0.44f)
        }
        drawPath(path = handle, color = color, style = stroke)
    }
}

/**
 * Icon Wi-Fi đơn giản
 */
@Composable
private fun WifiSimpleIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawArc(
            color = color,
            startAngle = 215f,
            sweepAngle = 110f,
            useCenter = false,
            topLeft = Offset(w * 0.18f, h * 0.24f),
            size = Size(w * 0.64f, h * 0.64f),
            style = stroke
        )
        drawArc(
            color = color,
            startAngle = 215f,
            sweepAngle = 110f,
            useCenter = false,
            topLeft = Offset(w * 0.30f, h * 0.38f),
            size = Size(w * 0.40f, h * 0.40f),
            style = stroke
        )
        drawCircle(color = color, radius = w * 0.06f, center = Offset(w * 0.50f, h * 0.74f))
    }
}

/**
 * Icon Đồng xu đơn giản
 */
@Composable
private fun CoinDollarSimpleIcon(
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

        drawCircle(
            color = color,
            radius = w * 0.36f,
            center = Offset(w * 0.50f, h * 0.50f),
            style = stroke
        )

        drawLine(
            color = color,
            start = Offset(w * 0.50f, h * 0.28f),
            end = Offset(w * 0.50f, h * 0.72f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        val sCurve = Path().apply {
            moveTo(w * 0.60f, h * 0.40f)
            cubicTo(w * 0.60f, h * 0.34f, w * 0.40f, h * 0.34f, w * 0.40f, h * 0.44f)
            cubicTo(w * 0.40f, h * 0.56f, w * 0.60f, h * 0.56f, w * 0.60f, h * 0.64f)
            cubicTo(w * 0.60f, h * 0.70f, w * 0.40f, h * 0.70f, w * 0.40f, h * 0.64f)
        }
        drawPath(path = sCurve, color = color, style = stroke)
    }
}

/**
 * Icon Trang chủ cho Bottom Bar
 */
@Composable
private fun HomeNavSimpleIcon(
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

        val roof = Path().apply {
            moveTo(w * 0.18f, h * 0.48f)
            lineTo(w * 0.50f, h * 0.20f)
            lineTo(w * 0.82f, h * 0.48f)
        }
        drawPath(path = roof, color = color, style = stroke)

        val walls = Path().apply {
            moveTo(w * 0.25f, h * 0.45f)
            lineTo(w * 0.25f, h * 0.82f)
            lineTo(w * 0.75f, h * 0.82f)
            lineTo(w * 0.75f, h * 0.45f)
        }
        drawPath(path = walls, color = color, style = stroke)
    }
}

/**
 * Icon Lịch sử cho Bottom Bar
 */
@Composable
private fun HistoryNavSimpleIcon(
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
        drawLine(color = color, start = Offset(w * 0.35f, h * 0.36f), end = Offset(w * 0.65f, h * 0.36f), strokeWidth = 1.4.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.35f, h * 0.50f), end = Offset(w * 0.65f, h * 0.50f), strokeWidth = 1.4.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.35f, h * 0.64f), end = Offset(w * 0.52f, h * 0.64f), strokeWidth = 1.4.dp.toPx(), cap = StrokeCap.Round)
    }
}

/**
 * Icon Báo cáo cho Bottom Bar
 */
@Composable
private fun ReportsNavSimpleIcon(
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

        drawCircle(color = color, radius = w * 0.36f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
        drawLine(color = color, start = Offset(w * 0.50f, h * 0.50f), end = Offset(w * 0.50f, h * 0.25f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.50f, h * 0.50f), end = Offset(w * 0.70f, h * 0.50f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
    }
}

/**
 * Icon Cá nhân cho Bottom Bar
 */
@Composable
private fun ProfileNavSimpleIcon(
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

        drawCircle(color = color, radius = w * 0.18f, center = Offset(w * 0.50f, h * 0.34f), style = stroke)
        val body = Path().apply {
            moveTo(w * 0.22f, h * 0.82f)
            cubicTo(w * 0.22f, h * 0.60f, w * 0.78f, h * 0.60f, w * 0.78f, h * 0.82f)
        }
        drawPath(path = body, color = color, style = stroke)
    }
}

// ====================== PREVIEW ======================
@Preview(showBackground = true, device = "id:pixel_7", name = "13 - Transaction History Screen")
@Composable
fun TransactionHistoryScreenPreview() {
    Nhoms8Theme {
        TransactionHistoryScreen()
    }
}
