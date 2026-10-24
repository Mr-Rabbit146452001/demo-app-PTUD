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
 * 14 · Màn hình Báo cáo tài chính (Financial Report / Analytics Screen)
 * Thiết kế chuẩn Figma SpendTogether: 3 thẻ KPI nhanh, Biểu đồ Donut phân bổ danh mục và Biểu đồ cột chi tiêu thành viên
 */
@Composable
fun FinancialReportScreen(
    groupTitle: String = "Báo cáo",
    groupSubtitle: String = "Phân tích quỹ Nhà Mình",
    onExportReportClicked: () -> Unit = {},
    onFilterMonthClicked: () -> Unit = {},
    onCompareMonthClicked: () -> Unit = {},
    onNavTabSelected: (BottomNavTab) -> Unit = {}
) {
    var selectedNavTab by remember { mutableStateOf(BottomNavTab.REPORTS) }

    // Bảng màu thiết kế chuẩn Figma
    val backgroundColor = Color(0xFFF5F3EC)      // Màu kem nhạt ấm áp
    val ambientSage = Color(0xFFDDEAE0)          // Màu xanh xô thơm nhạt họa tiết nền
    val forestGreenDark = Color(0xFF14382C)      // Xanh rêu đậm thương hiệu
    val textMuted = Color(0xFF6E7C74)            // Màu xám xanh phụ đề
    val incomeGreen = Color(0xFF2A7B56)          // Xanh lá cho tổng thu
    val expenseRed = Color(0xFFD96B52)           // Đỏ cam cho tổng chi
    val activePillBg = Color(0xFFCBE8D6)         // Màu nền pill active bottom nav

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
                            text = groupTitle,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            color = forestGreenDark
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = groupSubtitle,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Normal,
                            color = textMuted
                        )
                    }

                    // Nút Xuất báo cáo (Download icon)
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
                            .clickable { onExportReportClicked() },
                        contentAlignment = Alignment.Center
                    ) {
                        DownloadExportIcon(
                            modifier = Modifier.size(19.dp),
                            color = forestGreenDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ---------- 3. Thanh lọc thời gian & Thẻ chỉ số nhanh ----------
                // Hàng nút lọc (2 Pill buttons)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Chip 1 (Active): Tháng 10
                    Box(
                        modifier = Modifier
                            .height(38.dp)
                            .shadow(
                                elevation = 3.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.04f),
                                spotColor = Color.Black.copy(alpha = 0.07f)
                            )
                            .clip(CircleShape)
                            .background(forestGreenDark)
                            .clickable { onFilterMonthClicked() }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CalendarPillIcon(modifier = Modifier.size(14.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tháng 10",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    // Chip 2: So với tháng 9
                    Box(
                        modifier = Modifier
                            .height(38.dp)
                            .shadow(
                                elevation = 2.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.04f),
                                spotColor = Color.Black.copy(alpha = 0.06f)
                            )
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { onCompareMonthClicked() }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CompareSlidersIcon(modifier = Modifier.size(14.dp), color = forestGreenDark)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "So với tháng 9",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = forestGreenDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hàng 3 thẻ tóm tắt chỉ số KPI nhanh
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Thẻ 1: TỔNG THU
                    KpiSummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "TỔNG THU",
                        amount = "8,50 tr",
                        subtitle = "↗ 12%",
                        amountColor = incomeGreen,
                        subtitleColor = incomeGreen,
                        cardBg = Color.White,
                        titleColor = textMuted
                    )

                    // Thẻ 2: TỔNG CHI
                    KpiSummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "TỔNG CHI",
                        amount = "5,82 tr",
                        subtitle = "↗ 6%",
                        amountColor = expenseRed,
                        subtitleColor = expenseRed,
                        cardBg = Color.White,
                        titleColor = textMuted
                    )

                    // Thẻ 3: SỐ DƯ (Xanh rêu đậm)
                    KpiSummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "SỐ DƯ",
                        amount = "12,68 tr",
                        subtitle = "An toàn",
                        amountColor = Color.White,
                        subtitleColor = Color(0xFFA5E2C4),
                        cardBg = forestGreenDark,
                        titleColor = Color(0xFFA5BCB0)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ---------- 4. Thẻ biểu đồ "Chi theo danh mục" (Donut Chart) ----------
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
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Header Card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Chi theo danh mục",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = forestGreenDark
                            )

                            Text(
                                text = "5.820.000 đ",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = textMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Nội dung 2 cột: Donut Chart + Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Biểu đồ Donut bên trái
                            Box(
                                modifier = Modifier.size(130.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                DonutChartCanvas(modifier = Modifier.fillMaxSize())

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "5,82tr",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = forestGreenDark
                                    )
                                    Text(
                                        text = "Tổng chi",
                                        fontSize = 10.5.sp,
                                        color = textMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Chú giải 4 dòng bên phải
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                DonutLegendItem(color = Color(0xFF14382C), label = "Nhà ở", value = "2,91 tr")
                                DonutLegendItem(color = Color(0xFF6E9882), label = "Ăn uống", value = "1,75 tr")
                                DonutLegendItem(color = Color(0xFFDCE8AC), label = "Đi lại", value = "700k")
                                DonutLegendItem(color = Color(0xFFD6B98C), label = "Khác", value = "460k")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ---------- 5. Thẻ biểu đồ "Chi theo thành viên" (Bar Chart) ----------
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
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Header Card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Chi theo thành viên",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = forestGreenDark
                            )

                            Text(
                                text = "Minh Anh cao nhất",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF507865)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Biểu đồ cột dọc (5 cột)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            BarChartColumnItem(name = "M.Anh", amountLabel = "1,8tr", heightRatio = 1.0f, barColor = forestGreenDark)
                            BarChartColumnItem(name = "Lan", amountLabel = "1,4tr", heightRatio = 0.78f, barColor = Color(0xFFBDE2CC))
                            BarChartColumnItem(name = "Nam", amountLabel = "1,1tr", heightRatio = 0.61f, barColor = Color(0xFFBDE2CC))
                            BarChartColumnItem(name = "My", amountLabel = "820k", heightRatio = 0.46f, barColor = Color(0xFFBDE2CC))
                            BarChartColumnItem(name = "Phúc", amountLabel = "700k", heightRatio = 0.39f, barColor = Color(0xFFBDE2CC))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // ---------- 6. Thanh điều hướng đáy (Bottom Navigation Bar) ----------
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
                    ReportBottomNavItem(
                        title = "Trang chủ",
                        isSelected = selectedNavTab == BottomNavTab.HOME,
                        activePillBg = activePillBg,
                        activeColor = forestGreenDark,
                        inactiveColor = textMuted,
                        icon = { color -> HomeReportIcon(modifier = Modifier.size(20.dp), color = color) },
                        onClick = {
                            selectedNavTab = BottomNavTab.HOME
                            onNavTabSelected(BottomNavTab.HOME)
                        }
                    )

                    // Tab 2: Lịch sử
                    ReportBottomNavItem(
                        title = "Lịch sử",
                        isSelected = selectedNavTab == BottomNavTab.HISTORY,
                        activePillBg = activePillBg,
                        activeColor = forestGreenDark,
                        inactiveColor = textMuted,
                        icon = { color -> HistoryReportIcon(modifier = Modifier.size(20.dp), color = color) },
                        onClick = {
                            selectedNavTab = BottomNavTab.HISTORY
                            onNavTabSelected(BottomNavTab.HISTORY)
                        }
                    )

                    // Tab 3: Báo cáo (Active)
                    ReportBottomNavItem(
                        title = "Báo cáo",
                        isSelected = selectedNavTab == BottomNavTab.REPORTS,
                        activePillBg = activePillBg,
                        activeColor = forestGreenDark,
                        inactiveColor = textMuted,
                        icon = { color -> ReportsActiveIcon(modifier = Modifier.size(20.dp), color = color) },
                        onClick = {
                            selectedNavTab = BottomNavTab.REPORTS
                            onNavTabSelected(BottomNavTab.REPORTS)
                        }
                    )

                    // Tab 4: Cá nhân
                    ReportBottomNavItem(
                        title = "Cá nhân",
                        isSelected = selectedNavTab == BottomNavTab.PROFILE,
                        activePillBg = activePillBg,
                        activeColor = forestGreenDark,
                        inactiveColor = textMuted,
                        icon = { color -> ProfileReportIcon(modifier = Modifier.size(20.dp), color = color) },
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
 * Thẻ tóm tắt chỉ số KPI nhanh
 */
@Composable
private fun KpiSummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: String,
    subtitle: String,
    amountColor: Color,
    subtitleColor: Color,
    cardBg: Color,
    titleColor: Color
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color.Black.copy(alpha = 0.03f),
                spotColor = Color.Black.copy(alpha = 0.07f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .padding(vertical = 12.dp, horizontal = 10.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = amount,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor,
                letterSpacing = (-0.3).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = subtitleColor
            )
        }
    }
}

/**
 * Biểu đồ tròn Donut vẽ bằng Canvas
 */
@Composable
private fun DonutChartCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 24.dp.toPx()
        val arcSize = Size(w - strokeWidth, h - strokeWidth)
        val arcOffset = Offset(strokeWidth / 2, strokeWidth / 2)

        val stroke = Stroke(width = strokeWidth)

        // 1. Phân đoạn Nhà ở (50% ~ 180 độ) - Xanh rêu đậm
        drawArc(
            color = Color(0xFF14382C),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = arcOffset,
            size = arcSize,
            style = stroke
        )

        // 2. Phân đoạn Ăn uống (30% ~ 108 độ) - Xanh xô thơm
        drawArc(
            color = Color(0xFF6E9882),
            startAngle = 180f,
            sweepAngle = 108f,
            useCenter = false,
            topLeft = arcOffset,
            size = arcSize,
            style = stroke
        )

        // 3. Phân đoạn Đi lại (12% ~ 43 độ) - Xanh cốm nhạt
        drawArc(
            color = Color(0xFFDCE8AC),
            startAngle = 288f,
            sweepAngle = 43f,
            useCenter = false,
            topLeft = arcOffset,
            size = arcSize,
            style = stroke
        )

        // 4. Phân đoạn Khác (8% ~ 29 độ) - Vàng cát nâu be
        drawArc(
            color = Color(0xFFD6B98C),
            startAngle = 331f,
            sweepAngle = 29f,
            useCenter = false,
            topLeft = arcOffset,
            size = arcSize,
            style = stroke
        )
    }
}

/**
 * Dòng chú giải biểu đồ Donut
 */
@Composable
private fun DonutLegendItem(
    color: Color,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF56655D)
            )
        }

        Text(
            text = value,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF14382C)
        )
    }
}

/**
 * Cột đơn trong biểu đồ cột dọc (Bar Chart)
 */
@Composable
private fun BarChartColumnItem(
    name: String,
    amountLabel: String,
    heightRatio: Float,
    barColor: Color
) {
    val maxHeight = 85.dp
    val barHeight = maxHeight * heightRatio

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Nhãn số tiền trên đỉnh cột
        Text(
            text = amountLabel,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF43534B)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Cột đứng bo tròn 2 đầu
        Box(
            modifier = Modifier
                .width(26.dp)
                .height(barHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(barColor)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tên thành viên dưới chân cột
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF56655D)
        )
    }
}

/**
 * Mục tab đơn lẻ trong Bottom Navigation
 */
@Composable
private fun ReportBottomNavItem(
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
 * Icon Xuất báo cáo (Download / Export Outline)
 */
@Composable
private fun DownloadExportIcon(
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

        // Khay đựng phía dưới
        val tray = Path().apply {
            moveTo(w * 0.22f, h * 0.62f)
            lineTo(w * 0.22f, h * 0.80f)
            lineTo(w * 0.78f, h * 0.80f)
            lineTo(w * 0.78f, h * 0.62f)
        }
        drawPath(path = tray, color = color, style = stroke)

        // Mũi tên chỉ xuống
        drawLine(
            color = color,
            start = Offset(w * 0.50f, h * 0.18f),
            end = Offset(w * 0.50f, h * 0.62f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )
        val arrowHead = Path().apply {
            moveTo(w * 0.36f, h * 0.48f)
            lineTo(w * 0.50f, h * 0.62f)
            lineTo(w * 0.64f, h * 0.48f)
        }
        drawPath(path = arrowHead, color = color, style = stroke)
    }
}

/**
 * Icon Lịch nhỏ trong Pill
 */
@Composable
private fun CalendarPillIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)

        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.18f, h * 0.22f),
            size = Size(w * 0.64f, h * 0.60f),
            cornerRadius = CornerRadius(w * 0.10f, h * 0.10f),
            style = stroke
        )
        drawLine(color = color, start = Offset(w * 0.18f, h * 0.42f), end = Offset(w * 0.82f, h * 0.42f), strokeWidth = 1.3.dp.toPx(), cap = StrokeCap.Round)
    }
}

/**
 * Icon So sánh / Sliders
 */
@Composable
private fun CompareSlidersIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)

        // Đường 1
        drawLine(color = color, start = Offset(w * 0.20f, h * 0.35f), end = Offset(w * 0.80f, h * 0.35f), strokeWidth = 1.4.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color = color, radius = w * 0.08f, center = Offset(w * 0.40f, h * 0.35f), style = stroke)

        // Đường 2
        drawLine(color = color, start = Offset(w * 0.20f, h * 0.65f), end = Offset(w * 0.80f, h * 0.65f), strokeWidth = 1.4.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color = color, radius = w * 0.08f, center = Offset(w * 0.65f, h * 0.65f), style = stroke)
    }
}

/**
 * Icon Trang chủ cho Bottom Bar
 */
@Composable
private fun HomeReportIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

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
private fun HistoryReportIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.22f, h * 0.16f),
            size = Size(w * 0.56f, h * 0.68f),
            cornerRadius = CornerRadius(w * 0.08f, h * 0.08f),
            style = stroke
        )
        drawLine(color = color, start = Offset(w * 0.35f, h * 0.36f), end = Offset(w * 0.65f, h * 0.36f), strokeWidth = 1.4.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.35f, h * 0.50f), end = Offset(w * 0.65f, h * 0.50f), strokeWidth = 1.4.dp.toPx(), cap = StrokeCap.Round)
    }
}

/**
 * Icon Báo cáo biểu đồ đang Active
 */
@Composable
private fun ReportsActiveIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)

        drawCircle(color = color, radius = w * 0.36f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
        drawLine(color = color, start = Offset(w * 0.50f, h * 0.50f), end = Offset(w * 0.50f, h * 0.25f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.50f, h * 0.50f), end = Offset(w * 0.70f, h * 0.50f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
    }
}

/**
 * Icon Cá nhân cho Bottom Bar
 */
@Composable
private fun ProfileReportIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)

        drawCircle(color = color, radius = w * 0.18f, center = Offset(w * 0.50f, h * 0.34f), style = stroke)
        val body = Path().apply {
            moveTo(w * 0.22f, h * 0.82f)
            cubicTo(w * 0.22f, h * 0.60f, w * 0.78f, h * 0.60f, w * 0.78f, h * 0.82f)
        }
        drawPath(path = body, color = color, style = stroke)
    }
}

// ====================== PREVIEW ======================
@Preview(showBackground = true, device = "id:pixel_7", name = "14 - Financial Report Screen")
@Composable
fun FinancialReportScreenPreview() {
    Nhoms8Theme {
        FinancialReportScreen()
    }
}
