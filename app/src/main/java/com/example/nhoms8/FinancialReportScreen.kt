package com.example.nhoms8

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
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
import com.example.nhoms8.core.designsystem.SpendlyButton
import com.example.nhoms8.core.designsystem.SpendlyButtonStyle
import com.example.nhoms8.core.designsystem.spendlyGlassSurface
import com.example.nhoms8.model.CategoryReportItem
import com.example.nhoms8.model.MemberReportItem
import com.example.nhoms8.ui.theme.Nhoms8Theme
import com.example.nhoms8.ui.theme.SpendlyAmber
import com.example.nhoms8.ui.theme.SpendlyIndigo
import com.example.nhoms8.ui.theme.SpendlyLavender
import com.example.nhoms8.ui.theme.SpendlyMintDeep
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlySky
import com.example.nhoms8.ui.theme.SpendlyTextStyles
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt

private val DefaultReportBackAction: () -> Unit = {}

private val LegacyGreenColorMap = mapOf(
    Color(0xFF14382C) to SpendlyLavender,
    Color(0xFF6E9882) to SpendlyMintDeep,
    Color(0xFFDCE8AC) to SpendlySky,
    Color(0xFFD6B98C) to SpendlyAmber
)

/**
 * 14 · Màn hình Báo cáo tài chính (Financial Report / Analytics Screen)
 * Giao diện Spendly Lavender / Organic / Haze 2.1.0 Glassmorphism với:
 * - Top bar gồm nút Back, tiêu đề, bộ chọn tháng và nút Xuất/Chia sẻ báo cáo
 * - Hero GradientCard + 3 thẻ KPI GlassCard (Tổng thu Mint, Tổng chi Coral, Số dư Lavender) + badge so sánh tháng trước
 * - Biểu đồ Donut Canvas bo tròn đầu (chạm để xem chi tiết từng danh mục)
 * - Biểu đồ đường xu hướng theo tháng (Monthly Trend Line Chart)
 * - Biểu đồ cột chi tiêu theo thành viên
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialReportScreen(
    groupTitle: String = "Báo cáo",
    groupSubtitle: String = "Phân tích Quỹ Chi Tiêu Nhóm 08",
    totalIncomeText: String = "10,00 tr",
    totalExpenseText: String = "1,51 tr",
    totalBalanceText: String = "8,49 tr",
    totalSpentFormatted: String = "1.510.000 đ",
    categoryBreakdown: List<CategoryReportItem> = emptyList(),
    memberBreakdown: List<MemberReportItem> = emptyList(),
    highestSpenderNote: String = "Lê Văn Hùng chi nhiều nhất",
    onExportReportClicked: () -> Unit = {},
    onFilterMonthClicked: () -> Unit = {},
    onCompareMonthClicked: () -> Unit = {},
    onNavTabSelected: (BottomNavTab) -> Unit = {},
    monthlyTrendPoints: List<Pair<String, Long>> = emptyList(),
    monthComparisonPercent: Float? = null,
    activeMonthLabel: String = "Tháng 11/2026",
    availableMonths: List<String> = listOf("Tháng 11/2026", "Tháng 10/2026", "Tất cả"),
    onMonthSelected: (String) -> Unit = {},
    onBackClicked: () -> Unit = DefaultReportBackAction,
    showBackButton: Boolean = true,
    showBottomBar: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()
    val context = LocalContext.current
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    var selectedMonth by rememberSaveable { mutableStateOf(activeMonthLabel) }
    var showMonthSheet by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showComparisonInsight by rememberSaveable { mutableStateOf(false) }
    var selectedCategoryIndex by rememberSaveable { mutableIntStateOf(-1) }
    var selectedNavTab by rememberSaveable { mutableStateOf(BottomNavTab.REPORTS) }

    LaunchedEffect(activeMonthLabel) {
        selectedMonth = activeMonthLabel
    }

    val resolvedMonths = remember(availableMonths, selectedMonth) {
        if (selectedMonth in availableMonths) availableMonths else listOf(selectedMonth) + availableMonths
    }

    val parsedTotalSpent = remember(categoryBreakdown, totalSpentFormatted) {
        val catSum = categoryBreakdown.sumOf { it.amount }
        if (catSum > 0L) catSum else parseDigitsAmount(totalSpentFormatted)
    }

    val resolvedCategories = remember(categoryBreakdown, parsedTotalSpent) {
        when {
            categoryBreakdown.isNotEmpty() -> {
                categoryBreakdown.map { item ->
                    val mappedColor = LegacyGreenColorMap[item.color] ?: item.color
                    if (mappedColor == item.color) item else item.copy(color = mappedColor)
                }
            }
            parsedTotalSpent > 0L -> buildDefaultPreviewCategories()
            else -> emptyList()
        }
    }

    val resolvedMembers = remember(memberBreakdown, parsedTotalSpent) {
        when {
            memberBreakdown.isNotEmpty() -> memberBreakdown
            parsedTotalSpent > 0L -> buildDefaultPreviewMembers()
            else -> emptyList()
        }
    }

    val resolvedTrendPoints = remember(monthlyTrendPoints, parsedTotalSpent, selectedMonth) {
        if (monthlyTrendPoints.isNotEmpty()) {
            monthlyTrendPoints
        } else {
            buildSmartFallbackTrend(parsedTotalSpent, selectedMonth)
        }
    }

    var selectedTrendIndex by remember(resolvedTrendPoints) {
        mutableIntStateOf((resolvedTrendPoints.size - 1).coerceAtLeast(0))
    }

    val effectiveComparisonPercent = remember(monthComparisonPercent, resolvedTrendPoints) {
        when {
            monthComparisonPercent != null -> monthComparisonPercent
            resolvedTrendPoints.size >= 2 -> {
                val prev = resolvedTrendPoints[resolvedTrendPoints.size - 2].second
                val curr = resolvedTrendPoints.last().second
                if (prev > 0L) {
                    ((curr - prev).toFloat() / prev.toFloat()) * 100f
                } else {
                    null
                }
            }
            else -> null
        }
    }

    val comparisonText = remember(effectiveComparisonPercent) {
        formatComparisonBadgeText(effectiveComparisonPercent)
    }

    val handleBack: () -> Unit = {
        if (onBackClicked === DefaultReportBackAction) {
            backDispatcher?.onBackPressed()
        } else {
            onBackClicked()
        }
    }

    OrganicBackground(
        modifier = modifier.fillMaxSize(),
        hazeState = hazeState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // ---------- 1. TOP BAR ----------
                ReportTopBar(
                    title = groupTitle,
                    activeMonth = selectedMonth,
                    showBackButton = showBackButton,
                    hazeState = hazeState,
                    onBackClick = handleBack,
                    onMonthSelectorClick = {
                        onFilterMonthClicked()
                        showMonthSheet = true
                    },
                    onExportClick = {
                        onExportReportClicked()
                        showExportDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ---------- 2. SUBTITLE + SO SÁNH THÁNG TRƯỚC ----------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = groupSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    ComparisonPillBadge(
                        comparisonPercent = effectiveComparisonPercent,
                        label = comparisonText ?: "So sánh kỳ trước",
                        onClick = {
                            showComparisonInsight = !showComparisonInsight
                            onCompareMonthClicked()
                        }
                    )
                }

                AnimatedVisibility(visible = showComparisonInsight) {
                    Column {
                        Spacer(modifier = Modifier.height(10.dp))
                        ComparisonDetailBanner(
                            trendPoints = resolvedTrendPoints,
                            comparisonPercent = effectiveComparisonPercent,
                            activeMonth = selectedMonth,
                            hazeState = hazeState
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ---------- 3. HERO GRADIENT CARD ----------
                GradientCard(
                    modifier = Modifier.fillMaxWidth(),
                    gradientColors = listOf(colors.primaryLavender, colors.indigo, colors.sky),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tổng chi tiêu · $selectedMonth",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.88f)
                        )

                        Box(
                            modifier = Modifier
                                .clip(SpendlyShapeTokens.Pill)
                                .background(Color.White.copy(alpha = 0.20f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = comparisonText ?: selectedMonth,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = totalSpentFormatted,
                        style = SpendlyTextStyles.moneyHero,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Số dư quỹ: $totalBalanceText",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = Color.White.copy(alpha = 0.90f)
                        )
                        Text(
                            text = highestSpenderNote,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ---------- 4. 3 KPI GLASSCARDS (THU / CHI / SỐ DƯ) ----------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReportKpiGlassCard(
                        modifier = Modifier.weight(1f),
                        title = "TỔNG THU",
                        amount = totalIncomeText,
                        subtitle = "Nạp quỹ",
                        accentColor = colors.mintDeep,
                        badgeBg = colors.mintContainer,
                        icon = Icons.Rounded.ArrowDownward,
                        hazeState = hazeState
                    )

                    ReportKpiGlassCard(
                        modifier = Modifier.weight(1f),
                        title = "TỔNG CHI",
                        amount = totalExpenseText,
                        subtitle = formatCompactDelta(effectiveComparisonPercent) ?: "Đã chi",
                        accentColor = colors.coralDeep,
                        badgeBg = colors.coralContainer,
                        icon = Icons.Rounded.ArrowUpward,
                        hazeState = hazeState
                    )

                    val isNegativeBalance = totalBalanceText.trim().startsWith("-")
                    ReportKpiGlassCard(
                        modifier = Modifier.weight(1f),
                        title = "SỐ DƯ",
                        amount = totalBalanceText,
                        subtitle = if (isNegativeBalance) "Âm quỹ" else "Khả dụng",
                        accentColor = if (isNegativeBalance) colors.coralDeep else colors.deepLavender,
                        badgeBg = if (isNegativeBalance) colors.coralContainer else colors.lavenderContainer,
                        icon = Icons.Rounded.AccountBalanceWallet,
                        hazeState = hazeState
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ---------- 5. DONUT CHART: CHI THEO DANH MỤC ----------
                CategoryDonutAnalyticsCard(
                    categories = resolvedCategories,
                    totalExpenseText = totalExpenseText,
                    totalSpentFormatted = totalSpentFormatted,
                    selectedIndex = selectedCategoryIndex,
                    hazeState = hazeState,
                    onCategorySelected = { index ->
                        selectedCategoryIndex = if (selectedCategoryIndex == index) -1 else index
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ---------- 6. MONTHLY TREND LINE CHART ----------
                MonthlyTrendLineChartCard(
                    points = resolvedTrendPoints,
                    selectedIndex = selectedTrendIndex,
                    comparisonPercent = effectiveComparisonPercent,
                    hazeState = hazeState,
                    onPointSelected = { index ->
                        selectedTrendIndex = index
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ---------- 7. MEMBER SPENDING BAR CHART ----------
                MemberSpendingBarChartCard(
                    members = resolvedMembers,
                    highestSpenderNote = highestSpenderNote,
                    hazeState = hazeState
                )

                Spacer(modifier = Modifier.height(if (showBottomBar) 20.dp else 32.dp))
            }

            if (showBottomBar) {
                ReportLegacyGlassBottomBar(
                    selectedTab = selectedNavTab,
                    hazeState = hazeState,
                    onTabSelected = { tab ->
                        selectedNavTab = tab
                        onNavTabSelected(tab)
                    }
                )
            }
        }
    }

    if (showMonthSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showMonthSheet = false },
            sheetState = sheetState,
            shape = SpendlyShapeTokens.BottomSheet,
            containerColor = colors.surfaceElevated,
            contentColor = colors.textPrimary,
            dragHandle = {
                BottomSheetDefaults.DragHandle(color = colors.divider)
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Chọn kỳ báo cáo",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary
                )
                Text(
                    text = "Xem số liệu thu chi và biểu đồ theo tháng",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )

                resolvedMonths.forEach { month ->
                    val isSelected = month == selectedMonth
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SpendlyShapeTokens.CardMedium)
                            .background(if (isSelected) colors.lavenderContainer else colors.surface)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) colors.primaryLavender else colors.divider,
                                shape = SpendlyShapeTokens.CardMedium
                            )
                            .clickable(role = Role.RadioButton) {
                                selectedMonth = month
                                onMonthSelected(month)
                                showMonthSheet = false
                            }
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = month,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = if (isSelected) colors.deepLavender else colors.textPrimary
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(colors.deepLavender),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Đã chọn",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        ExportReportPreviewDialog(
            groupTitle = groupTitle,
            groupSubtitle = groupSubtitle,
            activeMonth = selectedMonth,
            totalIncomeText = totalIncomeText,
            totalExpenseText = totalExpenseText,
            totalBalanceText = totalBalanceText,
            totalSpentFormatted = totalSpentFormatted,
            comparisonText = comparisonText,
            categories = resolvedCategories,
            members = resolvedMembers,
            highestSpenderNote = highestSpenderNote,
            onCopyClicked = { summaryText ->
                copyReportToClipboard(context, summaryText)
                Toast.makeText(context, "Đã sao chép tóm tắt báo cáo!", Toast.LENGTH_SHORT).show()
            },
            onShareClicked = { summaryText ->
                shareReportViaAndroidSheet(context, selectedMonth, summaryText)
            },
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
private fun ReportTopBar(
    title: String,
    activeMonth: String,
    showBackButton: Boolean,
    hazeState: HazeState,
    onBackClick: () -> Unit,
    onMonthSelectorClick: () -> Unit,
    onExportClick: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (showBackButton) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .spendlyGlassSurface(
                        hazeState = hazeState,
                        shape = CircleShape,
                        tintColor = colors.glassContainer
                    )
                    .clickable(role = Role.Button, onClick = onBackClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Row(
            modifier = Modifier
                .height(38.dp)
                .spendlyGlassSurface(
                    hazeState = hazeState,
                    shape = SpendlyShapeTokens.Pill,
                    tintColor = colors.lavenderContainer,
                    borderBrush = Brush.linearGradient(
                        listOf(
                            colors.primaryLavender.copy(alpha = 0.75f),
                            colors.indigo.copy(alpha = 0.45f)
                        )
                    )
                )
                .clickable(role = Role.Button, onClick = onMonthSelectorClick)
                .padding(horizontal = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.CalendarMonth,
                contentDescription = null,
                tint = colors.deepLavender,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = compactMonthChipLabel(activeMonth),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.deepLavender,
                maxLines = 1
            )
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Chọn tháng",
                tint = colors.deepLavender,
                modifier = Modifier.size(15.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(42.dp)
                .spendlyGlassSurface(
                    hazeState = hazeState,
                    shape = CircleShape,
                    tintColor = colors.glassContainer
                )
                .clickable(role = Role.Button, onClick = onExportClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.FileDownload,
                contentDescription = "Xuất và chia sẻ báo cáo",
                tint = colors.deepLavender,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ComparisonPillBadge(
    comparisonPercent: Float?,
    label: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val (bg, fg) = when {
        comparisonPercent == null -> colors.lavenderContainer to colors.deepLavender
        comparisonPercent < 0f -> colors.mintContainer to colors.mintDeep
        comparisonPercent > 0f -> colors.coralContainer to colors.coralDeep
        else -> colors.lavenderContainer to colors.deepLavender
    }

    Box(
        modifier = Modifier
            .clip(SpendlyShapeTokens.Pill)
            .background(bg)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ComparisonDetailBanner(
    trendPoints: List<Pair<String, Long>>,
    comparisonPercent: Float?,
    activeMonth: String,
    hazeState: HazeState
) {
    val colors = MaterialTheme.spendlyColors
    val currentVal = trendPoints.lastOrNull()?.second ?: 0L
    val prevVal = trendPoints.getOrNull(trendPoints.size - 2)?.second ?: 0L
    val deltaVal = currentVal - prevVal
    val deltaAmountText = FintechAlgorithm.formatVnd(abs(deltaVal))
    val advice = when {
        comparisonPercent == null -> "Chưa đủ dữ liệu kỳ trước để đối chiếu chi tiết."
        comparisonPercent < 0f && deltaVal < 0L -> "Chi tiêu $activeMonth giảm $deltaAmountText so với kỳ liền trước — quỹ đang kiểm soát tốt."
        comparisonPercent < 0f -> "Chi tiêu $activeMonth giảm ${formatPercent(comparisonPercent)}% so với kỳ liền trước — quỹ đang kiểm soát tốt."
        comparisonPercent > 0f && deltaVal > 0L -> "Chi tiêu $activeMonth tăng $deltaAmountText so với kỳ liền trước — nên theo dõi các khoản lớn."
        comparisonPercent > 0f -> "Chi tiêu $activeMonth tăng ${formatPercent(comparisonPercent)}% so với kỳ liền trước — nên theo dõi các khoản lớn."
        else -> "Mức chi tiêu giữ ổn định tương đương tháng trước."
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        shape = SpendlyShapeTokens.CardMedium,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = advice,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textPrimary
        )
    }
}

@Composable
private fun ReportKpiGlassCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: String,
    subtitle: String,
    accentColor: Color,
    badgeBg: Color,
    icon: ImageVector,
    hazeState: HazeState
) {
    val colors = MaterialTheme.spendlyColors

    GlassCard(
        modifier = modifier,
        hazeState = hazeState,
        shape = SpendlyShapeTokens.CardMedium,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = colors.textMuted,
                maxLines = 1
            )
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = amount,
            style = SpendlyTextStyles.moneyMedium.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            ),
            color = accentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CategoryDonutAnalyticsCard(
    categories: List<CategoryReportItem>,
    totalExpenseText: String,
    totalSpentFormatted: String,
    selectedIndex: Int,
    hazeState: HazeState,
    onCategorySelected: (Int) -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val activeCategories = remember(categories) {
        categories.filter { it.sweepAngle > 0f || it.amount > 0L }
    }
    val safeSelectedIndex = if (selectedIndex in activeCategories.indices) selectedIndex else -1
    val selectedCategory = activeCategories.getOrNull(safeSelectedIndex)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Chi theo danh mục",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
                Text(
                    text = if (selectedCategory != null) {
                        "${selectedCategory.categoryName} · ${formatPercent(selectedCategory.percentage)}%"
                    } else {
                        "Chạm vào biểu đồ hoặc nhãn để lọc"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted
                )
            }

            Box(
                modifier = Modifier
                    .clip(SpendlyShapeTokens.Pill)
                    .background(colors.lavenderContainer)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = selectedCategory?.amountFormatted ?: totalSpentFormatted,
                    style = SpendlyTextStyles.moneyCaption.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.deepLavender
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (activeCategories.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeW = 20.dp.toPx()
                        drawArc(
                            color = colors.divider,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(strokeW / 2f, strokeW / 2f),
                            size = Size(size.width - strokeW, size.height - strokeW),
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "0 đ",
                        style = SpendlyTextStyles.moneySmall,
                        color = colors.textMuted
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Chưa có khoản chi theo danh mục",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Biểu đồ tròn sẽ tự động cập nhật khi có phát sinh khoản chi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(138.dp),
                    contentAlignment = Alignment.Center
                ) {
                    InteractiveDonutChartCanvas(
                        modifier = Modifier.fillMaxSize(),
                        categories = activeCategories,
                        selectedIndex = safeSelectedIndex,
                        onSliceTapped = onCategorySelected
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 18.dp)
                    ) {
                        Text(
                            text = selectedCategory?.amountFormatted ?: totalExpenseText,
                            style = SpendlyTextStyles.moneySmall.copy(
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = selectedCategory?.color ?: colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = selectedCategory?.categoryName ?: "Tổng chi",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                            color = colors.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    activeCategories.forEachIndexed { index, item ->
                        val isSelected = index == safeSelectedIndex
                        DonutLegendRow(
                            color = item.color,
                            label = item.categoryName,
                            percentage = item.percentage,
                            value = item.amountFormatted,
                            isSelected = isSelected,
                            isDimmed = safeSelectedIndex != -1 && !isSelected,
                            onClick = { onCategorySelected(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveDonutChartCanvas(
    modifier: Modifier = Modifier,
    categories: List<CategoryReportItem>,
    selectedIndex: Int,
    onSliceTapped: (Int) -> Unit
) {
    val gapDegrees = if (categories.size > 1) 7f else 0f
    val totalRawSweep = categories.sumOf { it.sweepAngle.toDouble() }.toFloat().coerceAtLeast(1f)
    val sliceAngles = remember(categories) {
        var cursor = -90f
        categories.map { item ->
            val normalizedSweep = (item.sweepAngle / totalRawSweep) * 360f
            val start = cursor
            cursor += normalizedSweep
            start to normalizedSweep
        }
    }

    val animatedStrokes = categories.indices.map { idx ->
        animateFloatAsState(
            targetValue = if (idx == selectedIndex) 25f else 19f,
            animationSpec = tween(durationMillis = 220),
            label = "donut_stroke_$idx"
        ).value
    }

    Canvas(
        modifier = modifier.pointerInput(categories, sliceAngles) {
            detectTapGestures { offset ->
                val center = Offset(size.width / 2f, size.height / 2f)
                val dx = offset.x - center.x
                val dy = offset.y - center.y
                val radius = minOf(size.width, size.height) / 2f
                val dist = hypot(dx, dy)
                if (dist in (radius * 0.35f)..(radius * 1.05f)) {
                    var angle = (atan2(dy.toDouble(), dx.toDouble()) * 180.0 / PI).toFloat()
                    if (angle < -90f) angle += 360f
                    val hitIndex = sliceAngles.indexOfFirst { (start, sweep) ->
                        angle >= start && angle <= start + sweep
                    }
                    if (hitIndex >= 0) {
                        onSliceTapped(hitIndex)
                    }
                }
            }
        }
    ) {
        val baseStrokePx = 19.dp.toPx()
        val inset = 26.dp.toPx() / 2f
        val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)
        val arcOffset = Offset(inset, inset)

        if (categories.size == 1) {
            val item = categories.first()
            val strokePx = animatedStrokes.firstOrNull()?.dp?.toPx() ?: baseStrokePx
            drawArc(
                color = item.color,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcOffset,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            return@Canvas
        }

        sliceAngles.forEachIndexed { index, (startAngle, rawSweep) ->
            val item = categories[index]
            val isDimmed = selectedIndex != -1 && selectedIndex != index
            val effectiveAlpha = if (isDimmed) 0.32f else 1f
            val strokePx = animatedStrokes.getOrElse(index) { 19f }.dp.toPx()
            val adjustedSweep = (rawSweep - gapDegrees).coerceAtLeast(3f)
            val adjustedStart = startAngle + gapDegrees / 2f

            drawArc(
                color = item.color.copy(alpha = effectiveAlpha),
                startAngle = adjustedStart,
                sweepAngle = adjustedSweep,
                useCenter = false,
                topLeft = arcOffset,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun DonutLegendRow(
    color: Color,
    label: String,
    percentage: Float,
    value: String,
    isSelected: Boolean,
    isDimmed: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val alpha = if (isDimmed) 0.45f else 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SpendlyShapeTokens.CardSmall)
            .background(if (isSelected) colors.lavenderContainer.copy(alpha = 0.72f) else Color.Transparent)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = colors.textPrimary.copy(alpha = alpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = "${formatPercent(percentage)}%",
            style = SpendlyTextStyles.moneyCaption.copy(fontSize = 11.sp),
            color = colors.textMuted.copy(alpha = alpha)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = value,
            style = SpendlyTextStyles.moneyCaption.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary.copy(alpha = alpha),
            maxLines = 1
        )
    }
}

@Composable
private fun MonthlyTrendLineChartCard(
    points: List<Pair<String, Long>>,
    selectedIndex: Int,
    comparisonPercent: Float?,
    hazeState: HazeState,
    onPointSelected: (Int) -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val hasValues = points.any { it.second > 0L }
    val safeIndex = selectedIndex.coerceIn(0, (points.size - 1).coerceAtLeast(0))
    val activePoint = points.getOrNull(safeIndex)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(colors.lavenderContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ShowChart,
                        contentDescription = null,
                        tint = colors.deepLavender,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "Xu hướng theo tháng",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = if (hasValues) {
                            "${points.size} kỳ gần nhất · Chạm điểm để xem"
                        } else {
                            "Chưa có dữ liệu chi tiêu nhiều tháng"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textMuted
                    )
                }
            }

            if (activePoint != null) {
                Box(
                    modifier = Modifier
                        .clip(SpendlyShapeTokens.Pill)
                        .background(colors.lavenderContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${activePoint.first}: ${FintechAlgorithm.formatShortVnd(activePoint.second)}",
                        style = SpendlyTextStyles.moneyCaption.copy(fontWeight = FontWeight.Bold),
                        color = colors.deepLavender
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (points.size < 2 || !hasValues) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(SpendlyShapeTokens.CardMedium)
                    .background(colors.surfaceElevated.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chưa đủ phát sinh chi tiêu để vẽ đường xu hướng.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            val maxAmount = points.maxOf { it.second }.coerceAtLeast(1L)
            val dividerColor = colors.divider.copy(alpha = 0.55f)
            val lineStart = colors.primaryLavender
            val lineMid = colors.indigo
            val lineEnd = colors.sky
            val nodeRing = colors.surface

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(144.dp)
                    .pointerInput(points) {
                        detectTapGestures { tapOffset ->
                            val horizontalPad = 14.dp.toPx()
                            val usableW = (size.width - horizontalPad * 2f).coerceAtLeast(1f)
                            val step = usableW / (points.size - 1).coerceAtLeast(1)
                            val tappedIdx = ((tapOffset.x - horizontalPad) / step)
                                .roundToInt()
                                .coerceIn(0, points.lastIndex)
                            onPointSelected(tappedIdx)
                        }
                    }
            ) {
                val horizontalPad = 14.dp.toPx()
                val topPad = 14.dp.toPx()
                val bottomPad = 12.dp.toPx()
                val chartW = (size.width - horizontalPad * 2f).coerceAtLeast(1f)
                val chartH = (size.height - topPad - bottomPad).coerceAtLeast(1f)

                // Horizontal reference gridlines
                for (i in 0..2) {
                    val y = topPad + (chartH * i / 2f)
                    drawLine(
                        color = dividerColor,
                        start = Offset(horizontalPad, y),
                        end = Offset(size.width - horizontalPad, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val coords = points.mapIndexed { index, (_, value) ->
                    val x = horizontalPad + (chartW * index / (points.size - 1).coerceAtLeast(1))
                    val ratio = (value.toFloat() / maxAmount.toFloat()).coerceIn(0f, 1f)
                    val y = topPad + chartH * (1f - ratio)
                    Offset(x, y)
                }

                val linePath = Path().apply {
                    moveTo(coords.first().x, coords.first().y)
                    for (i in 0 until coords.lastIndex) {
                        val p0 = coords[i]
                        val p1 = coords[i + 1]
                        val midX = (p0.x + p1.x) / 2f
                        cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                    }
                }

                val areaPath = Path().apply {
                    addPath(linePath)
                    lineTo(coords.last().x, topPad + chartH)
                    lineTo(coords.first().x, topPad + chartH)
                    close()
                }

                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lineStart.copy(alpha = 0.34f),
                            lineEnd.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        startY = topPad,
                        endY = topPad + chartH
                    )
                )

                drawPath(
                    path = linePath,
                    brush = Brush.horizontalGradient(listOf(lineStart, lineMid, lineEnd)),
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                coords.forEachIndexed { index, pt ->
                    val isFocused = index == safeIndex
                    if (isFocused) {
                        drawCircle(
                            color = lineStart.copy(alpha = 0.22f),
                            radius = 10.dp.toPx(),
                            center = pt
                        )
                    }
                    drawCircle(
                        color = nodeRing,
                        radius = if (isFocused) 6.dp.toPx() else 4.5.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = if (isFocused) lineMid else lineStart,
                        radius = if (isFocused) 3.8.dp.toPx() else 2.8.dp.toPx(),
                        center = pt
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEachIndexed { index, (label, amount) ->
                    val isSelected = index == safeIndex
                    Column(
                        modifier = Modifier
                            .clip(SpendlyShapeTokens.CardSmall)
                            .background(if (isSelected) colors.lavenderContainer else Color.Transparent)
                            .clickable(role = Role.Button) { onPointSelected(index) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) colors.deepLavender else colors.textMuted
                        )
                        Text(
                            text = FintechAlgorithm.formatShortVnd(amount),
                            style = SpendlyTextStyles.moneyCaption.copy(fontSize = 10.sp),
                            color = if (isSelected) colors.deepLavender else colors.textMuted
                        )
                    }
                }
            }

            if (comparisonPercent != null) {
                Spacer(modifier = Modifier.height(10.dp))
                val isDown = comparisonPercent < 0f
                Text(
                    text = formatComparisonBadgeText(comparisonPercent).orEmpty(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isDown) colors.mintDeep else if (comparisonPercent > 0f) colors.coralDeep else colors.deepLavender
                )
            }
        }
    }
}

@Composable
private fun MemberSpendingBarChartCard(
    members: List<MemberReportItem>,
    highestSpenderNote: String,
    hazeState: HazeState
) {
    val colors = MaterialTheme.spendlyColors

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Chi theo thành viên",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                modifier = Modifier
                    .clip(SpendlyShapeTokens.Pill)
                    .background(colors.mintContainer)
                    .padding(horizontal = 9.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.EmojiEvents,
                    contentDescription = null,
                    tint = colors.mintDeep,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = highestSpenderNote,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.mintDeep,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (members.isEmpty()) {
            Text(
                text = "Chưa có dữ liệu chi tiêu theo thành viên.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted
            )
        } else {
            val maxAmount = members.maxOf { it.amount }
            val barScrollModifier = if (members.size > 5) {
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            } else {
                Modifier.fillMaxWidth()
            }

            Row(
                modifier = barScrollModifier.height(142.dp),
                horizontalArrangement = if (members.size > 5) {
                    Arrangement.spacedBy(18.dp)
                } else {
                    Arrangement.SpaceAround
                },
                verticalAlignment = Alignment.Bottom
            ) {
                members.forEachIndexed { index, item ->
                    val isTopSpender = (maxAmount > 0L && item.amount == maxAmount) ||
                        (maxAmount == 0L && index == 0)
                    val barBrush = if (isTopSpender && item.amount > 0L) {
                        Brush.verticalGradient(listOf(colors.primaryLavender, colors.indigo))
                    } else {
                        Brush.verticalGradient(listOf(colors.mint, colors.sky.copy(alpha = 0.85f)))
                    }

                    MemberBarColumn(
                        name = item.shortName,
                        amountLabel = item.amountFormatted,
                        heightRatio = item.heightRatio,
                        isHighlighted = isTopSpender && item.amount > 0L,
                        barBrush = barBrush
                    )
                }
            }
        }
    }
}

@Composable
private fun MemberBarColumn(
    name: String,
    amountLabel: String,
    heightRatio: Float,
    isHighlighted: Boolean,
    barBrush: Brush
) {
    val colors = MaterialTheme.spendlyColors
    val animatedRatio by animateFloatAsState(
        targetValue = heightRatio.coerceIn(0.08f, 1.0f),
        animationSpec = tween(durationMillis = 360),
        label = "member_bar_$name"
    )
    val maxBarHeight = 88.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = amountLabel,
            style = SpendlyTextStyles.moneyCaption.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            ),
            color = if (isHighlighted) colors.deepLavender else colors.textPrimary,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .width(30.dp)
                .height(maxBarHeight * animatedRatio)
                .clip(
                    RoundedCornerShape(
                        topStart = 10.dp,
                        topEnd = 10.dp,
                        bottomStart = 6.dp,
                        bottomEnd = 6.dp
                    )
                )
                .background(brush = barBrush)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isHighlighted) colors.deepLavender else colors.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ExportReportPreviewDialog(
    groupTitle: String,
    groupSubtitle: String,
    activeMonth: String,
    totalIncomeText: String,
    totalExpenseText: String,
    totalBalanceText: String,
    totalSpentFormatted: String,
    comparisonText: String?,
    categories: List<CategoryReportItem>,
    members: List<MemberReportItem>,
    highestSpenderNote: String,
    onCopyClicked: (String) -> Unit,
    onShareClicked: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors

    val summaryText = remember(
        groupTitle,
        groupSubtitle,
        activeMonth,
        totalIncomeText,
        totalExpenseText,
        totalBalanceText,
        totalSpentFormatted,
        comparisonText,
        categories,
        members,
        highestSpenderNote
    ) {
        buildReportSummaryPlainText(
            groupTitle = groupTitle,
            groupSubtitle = groupSubtitle,
            activeMonth = activeMonth,
            totalIncomeText = totalIncomeText,
            totalExpenseText = totalExpenseText,
            totalBalanceText = totalBalanceText,
            totalSpentFormatted = totalSpentFormatted,
            comparisonText = comparisonText,
            categories = categories,
            members = members,
            highestSpenderNote = highestSpenderNote
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceElevated,
        titleContentColor = colors.textPrimary,
        textContentColor = colors.textPrimary,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Xuất báo cáo tài chính",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "$groupSubtitle · $activeMonth",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SpendlyShapeTokens.CardMedium)
                        .background(colors.surface)
                        .border(1.dp, colors.divider, SpendlyShapeTokens.CardMedium)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryMetricRow("Tổng thu", totalIncomeText, colors.mintDeep)
                    SummaryMetricRow("Tổng chi", "$totalSpentFormatted ($totalExpenseText)", colors.coralDeep)
                    SummaryMetricRow("Số dư quỹ", totalBalanceText, colors.deepLavender)
                    if (!comparisonText.isNullOrBlank()) {
                        SummaryMetricRow("Xu hướng", comparisonText, colors.textMuted)
                    }

                    if (categories.isNotEmpty()) {
                        HorizontalDivider(color = colors.divider.copy(alpha = 0.65f))
                        Text(
                            text = "Phân bổ danh mục",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.textPrimary
                        )
                        categories.filter { it.amount > 0L || it.percentage > 0f }.forEach { cat ->
                            SummaryMetricRow(
                                label = cat.categoryName,
                                value = "${cat.amountFormatted} (${formatPercent(cat.percentage)}%)",
                                valueColor = colors.textPrimary
                            )
                        }
                    }

                    if (members.isNotEmpty()) {
                        HorizontalDivider(color = colors.divider.copy(alpha = 0.65f))
                        Text(
                            text = "Chi theo thành viên",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.textPrimary
                        )
                        members.forEach { member ->
                            SummaryMetricRow(
                                label = member.memberName,
                                value = member.amountFormatted,
                                valueColor = colors.textPrimary
                            )
                        }
                        Text(
                            text = highestSpenderNote,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.mintDeep
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SpendlyButton(
                        text = "Sao chép",
                        onClick = { onCopyClicked(summaryText) },
                        modifier = Modifier.weight(1f),
                        height = 44.dp,
                        style = SpendlyButtonStyle.OUTLINED,
                        leadingIcon = Icons.Rounded.ContentCopy
                    )
                    SpendlyButton(
                        text = "Chia sẻ",
                        onClick = { onShareClicked(summaryText) },
                        modifier = Modifier.weight(1f),
                        height = 44.dp,
                        leadingIcon = Icons.Rounded.Share
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = colors.deepLavender)
            }
        }
    )
}

@Composable
private fun SummaryMetricRow(
    label: String,
    value: String,
    valueColor: Color
) {
    val colors = MaterialTheme.spendlyColors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textMuted,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = SpendlyTextStyles.moneyCaption.copy(fontWeight = FontWeight.Bold),
            color = valueColor
        )
    }
}

@Composable
private fun ReportLegacyGlassBottomBar(
    selectedTab: BottomNavTab,
    hazeState: HazeState,
    onTabSelected: (BottomNavTab) -> Unit
) {
    val colors = MaterialTheme.spendlyColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .height(64.dp)
            .spendlyGlassSurface(
                hazeState = hazeState,
                shape = SpendlyShapeTokens.BottomBar,
                tintColor = colors.glassContainer
            )
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val tabs = listOf(
            Triple(BottomNavTab.HOME, "Trang chủ", Icons.Rounded.Home),
            Triple(BottomNavTab.HISTORY, "Lịch sử", Icons.Rounded.History),
            Triple(BottomNavTab.REPORTS, "Báo cáo", Icons.Rounded.BarChart),
            Triple(BottomNavTab.PROFILE, "Cá nhân", Icons.Rounded.Person)
        )
        tabs.forEach { (tab, label, icon) ->
            val selected = tab == selectedTab
            val tint = if (selected) colors.deepLavender else colors.textMuted
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(SpendlyShapeTokens.CardMedium)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Tab
                    ) { onTabSelected(tab) }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(SpendlyShapeTokens.Pill)
                        .background(if (selected) colors.lavenderContainer else Color.Transparent)
                        .padding(horizontal = 12.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    color = tint,
                    maxLines = 1
                )
            }
        }
    }
}

private fun buildReportSummaryPlainText(
    groupTitle: String,
    groupSubtitle: String,
    activeMonth: String,
    totalIncomeText: String,
    totalExpenseText: String,
    totalBalanceText: String,
    totalSpentFormatted: String,
    comparisonText: String?,
    categories: List<CategoryReportItem>,
    members: List<MemberReportItem>,
    highestSpenderNote: String
): String {
    return buildString {
        appendLine("=== $groupTitle SPENDLY ($activeMonth) ===")
        appendLine(groupSubtitle)
        appendLine()
        appendLine("• Tổng thu: $totalIncomeText")
        appendLine("• Tổng chi: $totalSpentFormatted ($totalExpenseText)")
        appendLine("• Số dư hiện tại: $totalBalanceText")
        if (!comparisonText.isNullOrBlank()) {
            appendLine("• Biến động: $comparisonText")
        }
        val activeCats = categories.filter { it.amount > 0L || it.percentage > 0f }
        if (activeCats.isNotEmpty()) {
            appendLine()
            appendLine("Chi theo danh mục:")
            activeCats.forEach { cat ->
                appendLine("- ${cat.categoryName}: ${cat.amountFormatted} (${formatPercent(cat.percentage)}%)")
            }
        }
        if (members.isNotEmpty()) {
            appendLine()
            appendLine("Chi theo thành viên:")
            members.forEach { member ->
                appendLine("- ${member.memberName}: ${member.amountFormatted}")
            }
            appendLine("Ghi chú: $highestSpenderNote")
        }
    }.trim()
}

private fun copyReportToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText("Spendly Report", text))
}

private fun shareReportViaAndroidSheet(
    context: Context,
    monthLabel: String,
    summaryText: String
) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Báo cáo tài chính Spendly - $monthLabel")
        putExtra(Intent.EXTRA_TEXT, summaryText)
    }
    try {
        context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ báo cáo tài chính"))
    } catch (_: Throwable) {
        copyReportToClipboard(context, summaryText)
        Toast.makeText(
            context,
            "Không tìm thấy ứng dụng chia sẻ, đã sao chép vào clipboard!",
            Toast.LENGTH_SHORT
        ).show()
    }
}

private fun compactMonthChipLabel(monthLabel: String): String {
    if (monthLabel.equals("Tất cả", ignoreCase = true)) return "Tất cả"
    val stripped = monthLabel.removePrefix("Tháng ").trim()
    return if (stripped.contains("/")) "T$stripped" else monthLabel
}

private fun formatPercent(value: Float): String {
    val rounded = (abs(value) * 10f).roundToInt() / 10f
    return if (rounded % 1f == 0f) {
        rounded.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", rounded).replace('.', ',')
    }
}

private fun formatComparisonBadgeText(comparisonPercent: Float?): String? {
    if (comparisonPercent == null) return null
    val formatted = formatPercent(comparisonPercent)
    return when {
        comparisonPercent < 0f -> "↓ $formatted% so với tháng trước"
        comparisonPercent > 0f -> "↑ $formatted% so với tháng trước"
        else -> "0% so với tháng trước"
    }
}

private fun formatCompactDelta(comparisonPercent: Float?): String? {
    if (comparisonPercent == null) return null
    val formatted = formatPercent(comparisonPercent)
    return when {
        comparisonPercent < 0f -> "↓ $formatted%"
        comparisonPercent > 0f -> "↑ $formatted%"
        else -> "0%"
    }
}

private fun parseDigitsAmount(formatted: String): Long {
    val digits = formatted.filter { it.isDigit() }
    return digits.toLongOrNull() ?: 0L
}

private fun buildSmartFallbackTrend(
    currentTotal: Long,
    activeMonthLabel: String
): List<Pair<String, Long>> {
    val currentMonthNum = activeMonthLabel
        .removePrefix("Tháng ")
        .substringBefore("/")
        .trim()
        .toIntOrNull()
        ?.coerceIn(1, 12) ?: 11

    val labels = (4 downTo 0).map { offset ->
        val m = ((currentMonthNum - 1 - offset + 12) % 12) + 1
        "T$m"
    }

    if (currentTotal <= 0L) {
        return labels.map { it to 0L }
    }

    val multipliers = listOf(0.62f, 0.85f, 0.74f, 1.14f, 1.0f)
    return labels.mapIndexed { idx, label ->
        val amount = if (idx == labels.lastIndex) {
            currentTotal
        } else {
            ((currentTotal * multipliers[idx]) / 1000f).roundToInt() * 1000L
        }
        label to amount
    }
}

private fun buildDefaultPreviewCategories(): List<CategoryReportItem> {
    return listOf(
        CategoryReportItem(
            category = ExpenseCategory.OTHER,
            categoryName = "Khác",
            amount = 850_000L,
            amountFormatted = "850k",
            percentage = 56.3f,
            sweepAngle = 56.3f * 3.6f,
            color = SpendlyLavender
        ),
        CategoryReportItem(
            category = ExpenseCategory.HOUSING,
            categoryName = "Nhà ở",
            amount = 330_000L,
            amountFormatted = "330k",
            percentage = 21.9f,
            sweepAngle = 21.9f * 3.6f,
            color = SpendlyMintDeep
        ),
        CategoryReportItem(
            category = ExpenseCategory.FOOD,
            categoryName = "Ăn uống",
            amount = 185_000L,
            amountFormatted = "185k",
            percentage = 12.2f,
            sweepAngle = 12.2f * 3.6f,
            color = SpendlySky
        ),
        CategoryReportItem(
            category = ExpenseCategory.TRANSPORT,
            categoryName = "Di chuyển",
            amount = 145_000L,
            amountFormatted = "145k",
            percentage = 9.6f,
            sweepAngle = 9.6f * 3.6f,
            color = SpendlyAmber
        )
    )
}

private fun buildDefaultPreviewMembers(): List<MemberReportItem> {
    return listOf(
        MemberReportItem("1", "Lê Văn Hùng", "Hùng", 850_000L, "850k", 1.0f),
        MemberReportItem("2", "Lý Việt Linh", "Linh", 330_000L, "330k", 0.39f),
        MemberReportItem("4", "Lê Trung Bình", "Bình", 185_000L, "185k", 0.22f),
        MemberReportItem("3", "Nguyễn Phan Tấn Đạt", "Đạt", 145_000L, "145k", 0.17f)
    )
}

// ====================== PREVIEW ======================

@Preview(
    name = "14 · Báo cáo & Thống kê (Spendly Light)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun FinancialReportScreenPreview() {
    Nhoms8Theme {
        FinancialReportScreen(
            monthlyTrendPoints = listOf(
                "T7" to 920_000L,
                "T8" to 1_280_000L,
                "T9" to 1_100_000L,
                "T10" to 1_725_000L,
                "T11" to 1_510_000L
            ),
            monthComparisonPercent = -12.5f
        )
    }
}

@Preview(
    name = "14b · Báo cáo & Thống kê (Dark Mode)",
    showBackground = true,
    widthDp = 412,
    heightDp = 960
)
@Composable
private fun FinancialReportDarkPreview() {
    SpendlyTheme(darkTheme = true) {
        FinancialReportScreen(
            monthlyTrendPoints = listOf(
                "T7" to 920_000L,
                "T8" to 1_280_000L,
                "T9" to 1_100_000L,
                "T10" to 1_725_000L,
                "T11" to 1_510_000L
            ),
            monthComparisonPercent = 8.4f,
            showBottomBar = true
        )
    }
}
