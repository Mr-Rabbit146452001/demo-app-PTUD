package com.example.nhoms8

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AddCard
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhoms8.core.designsystem.FundCard
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.GradientCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.core.designsystem.SpendlyButton
import com.example.nhoms8.core.designsystem.budgetHealthZone
import com.example.nhoms8.core.designsystem.BudgetHealthZone
import com.example.nhoms8.core.designsystem.spendlyGlassSurface
import com.example.nhoms8.ui.theme.SpendlyIndigo
import com.example.nhoms8.ui.theme.SpendlyLavender
import com.example.nhoms8.ui.theme.SpendlyLavenderDeep
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlyShapeTokens.CardMedium
import com.example.nhoms8.ui.theme.SpendlyShapeTokens.Pill
import com.example.nhoms8.ui.theme.SpendlyTextStyles
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState
import kotlin.math.roundToInt

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
 * Dữ liệu tóm tắt một quỹ trên màn hình Home V2.
 */
data class HomeFundItem(
    val groupId: String,
    val name: String,
    val balanceFormatted: String,
    val memberCount: Int,
    val transactionCount: Int,
    val budgetRatio: Float
)

/**
 * 07 · Màn hình Tổng quan V2 (Home / Dashboard Screen)
 * Phong cách Spendly Lavender / Organic / Haze 2.1.0 Glassmorphism:
 * 1. Top greeting header + active group pill + glass notification button
 * 2. Hero GradientCard với số dư, Thu/Chi, mini-pill Nợ/Được trả & thanh tiến độ ngân sách
 * 3. Hàng 5 tác vụ nhanh GlassCard (Thêm chi, Nạp thu, Đối soát, Báo cáo, Các quỹ)
 * 4. Khối "Quỹ của tôi" dùng FundCard + BudgetProgressRing
 * 5. Thẻ gợi ý FinSprout AI + nút "Hỏi AI"
 * 6. Danh sách "Giao dịch gần đây" GlassCard + "Xem tất cả"
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
    onNavTabSelected: (BottomNavTab) -> Unit = {},
    showBottomBar: Boolean = true,
    onReportsClicked: () -> Unit = {},
    youOweFormatted: String = "0 đ",
    youAreOwedFormatted: String = "0 đ",
    budgetUsageRatio: Float = 0f,
    fundsList: List<HomeFundItem> = emptyList(),
    aiSuggestionText: String? = null,
    onGroupsClicked: () -> Unit = {},
    onFundClicked: (String) -> Unit = {},
    onSettlementClicked: () -> Unit = {},
    onBudgetsClicked: () -> Unit = {}
) {
    @Suppress("UNUSED_EXPRESSION")
    onSplitBillClicked

    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()
    var selectedTab by remember { mutableStateOf(BottomNavTab.HOME) }

    OrganicBackground(
        modifier = Modifier.fillMaxSize(),
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
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                // 1. TOP GREETING HEADER
                HomeGreetingHeader(
                    userName = userName,
                    userInitials = userInitials,
                    groupName = groupName,
                    hazeState = hazeState,
                    onGroupPillClicked = onMembersClicked,
                    onNotificationClicked = onNotificationClicked
                )

                // 2. HERO GRADIENT BALANCE CARD
                HeroBalanceCardV2(
                    totalBalance = totalBalance,
                    monthLabel = monthLabel,
                    depositedAmount = depositedAmount,
                    spentAmount = spentAmount,
                    youOweFormatted = youOweFormatted,
                    youAreOwedFormatted = youAreOwedFormatted,
                    budgetUsageRatio = budgetUsageRatio,
                    onCardClicked = onReportsClicked,
                    onSettlementClicked = onSettlementClicked,
                    onBudgetsClicked = onBudgetsClicked
                )

                // 3. 5 QUICK ACTIONS ROW
                HomeQuickActionsRow(
                    hazeState = hazeState,
                    onAddExpenseClicked = onAddExpenseClicked,
                    onTopUpFundClicked = onTopUpFundClicked,
                    onSettlementClicked = onSettlementClicked,
                    onReportsClicked = onReportsClicked,
                    onGroupsClicked = onGroupsClicked
                )

                // 4. "QUỸ CỦA TÔI" SECTION
                HomeFundsSection(
                    groupName = groupName,
                    totalBalance = totalBalance,
                    transactionCount = recentTransactions.size,
                    budgetUsageRatio = budgetUsageRatio,
                    fundsList = fundsList,
                    hazeState = hazeState,
                    onGroupsClicked = onGroupsClicked,
                    onFundClicked = onFundClicked,
                    onActiveGroupClicked = onMembersClicked
                )

                // 5. "FINSPROUT AI GỢI Ý" GLASS CARD
                FinSproutSuggestionCard(
                    suggestionText = aiSuggestionText,
                    budgetUsageRatio = budgetUsageRatio,
                    hazeState = hazeState,
                    onChatbotClicked = onChatbotClicked
                )

                // 6. "GIAO DỊCH GẦN ĐÂY" GLASS CARD
                RecentTransactionsSection(
                    transactions = recentTransactions,
                    hazeState = hazeState,
                    onViewAllClicked = onViewAllTransactionsClicked,
                    onTransactionClicked = onTransactionClicked
                )

                // 7. BOTTOM SPACER FOR OVERLAID 5-TAB SPENDLY BOTTOM BAR
                if (showBottomBar) {
                    Spacer(modifier = Modifier.height(14.dp))
                } else {
                    Spacer(modifier = Modifier.height(108.dp))
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }

            if (showBottomBar) {
                HomeLegacyLavenderBottomBar(
                    selectedTab = selectedTab,
                    hazeState = hazeState,
                    onTabSelected = {
                        selectedTab = it
                        onNavTabSelected(it)
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeGreetingHeader(
    userName: String,
    userInitials: String,
    groupName: String,
    hazeState: HazeState,
    onGroupPillClicked: () -> Unit,
    onNotificationClicked: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val avatarGradient = Brush.linearGradient(
        colors = listOf(SpendlyLavender, SpendlyLavenderDeep, SpendlyIndigo)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(avatarGradient)
                    .border(2.dp, Color.White.copy(alpha = 0.75f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userInitials,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Xin chào, $userName",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .clip(Pill)
                        .background(colors.lavenderContainer.copy(alpha = if (colors.isDark) 0.55f else 0.90f))
                        .border(
                            width = 1.dp,
                            color = colors.primaryLavender.copy(alpha = 0.25f),
                            shape = Pill
                        )
                        .clickable(role = Role.Button, onClick = onGroupPillClicked)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountBalanceWallet,
                        contentDescription = null,
                        tint = colors.onLavenderContainer,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = groupName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.onLavenderContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .size(44.dp)
                .spendlyGlassSurface(
                    hazeState = hazeState,
                    shape = CircleShape,
                    tintColor = colors.glassContainer
                )
                .clickable(role = Role.Button, onClick = onNotificationClicked),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Thông báo",
                tint = colors.textPrimary,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

@Composable
private fun HeroBalanceCardV2(
    totalBalance: String,
    monthLabel: String,
    depositedAmount: String,
    spentAmount: String,
    youOweFormatted: String,
    youAreOwedFormatted: String,
    budgetUsageRatio: Float,
    onCardClicked: () -> Unit,
    onSettlementClicked: () -> Unit,
    onBudgetsClicked: () -> Unit
) {
    val clampedRatio = budgetUsageRatio.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = clampedRatio,
        animationSpec = tween(durationMillis = 550),
        label = "home_budget_progress"
    )
    val progressPercent = (budgetUsageRatio.coerceAtLeast(0f) * 100f).roundToInt()
    val barFillColor = when (budgetHealthZone(budgetUsageRatio)) {
        BudgetHealthZone.HEALTHY -> Color(0xFF9DE6CC)
        BudgetHealthZone.CAUTION -> Color(0xFFFAD17C)
        BudgetHealthZone.CRITICAL -> Color(0xFFFFB2B2)
    }

    GradientCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SpendlyShapeTokens.CardLarge)
            .clickable(onClick = onCardClicked),
        contentPadding = PaddingValues(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tổng quỹ hiện tại",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = Color.White.copy(alpha = 0.85f)
            )

            Box(
                modifier = Modifier
                    .clip(Pill)
                    .background(Color.White.copy(alpha = 0.18f))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), Pill)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = monthLabel,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = totalBalance,
            style = SpendlyTextStyles.moneyHero,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Thu / Chi metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HeroCashflowColumn(
                label = "THU VÀO",
                amount = depositedAmount,
                icon = Icons.Rounded.ArrowDownward,
                badgeTint = Color(0xFF9DE6CC),
                modifier = Modifier.weight(1f)
            )
            HeroCashflowColumn(
                label = "CHI RA",
                amount = spentAmount,
                icon = Icons.Rounded.ArrowUpward,
                badgeTint = Color(0xFFFFB8B8),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Two glass mini-pills: Bạn đang nợ & Được trả
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HeroDebtMiniPill(
                label = "Bạn đang nợ: $youOweFormatted",
                icon = Icons.AutoMirrored.Rounded.TrendingDown,
                accentColor = Color(0xFFFFC1C1),
                onClick = onSettlementClicked,
                modifier = Modifier.weight(1f)
            )
            HeroDebtMiniPill(
                label = "Được trả: $youAreOwedFormatted",
                icon = Icons.AutoMirrored.Rounded.TrendingUp,
                accentColor = Color(0xFFA7F0D6),
                onClick = onSettlementClicked,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Subtle budget progress bar (clickable -> onBudgetsClicked)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.12f))
                .clickable(role = Role.Button, onClick = onBudgetsClicked)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ngân sách tháng",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.90f)
                )
                Text(
                    text = "$progressPercent% đã dùng",
                    style = SpendlyTextStyles.moneyCaption.copy(fontWeight = FontWeight.SemiBold),
                    color = barFillColor
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(Pill)
                    .background(Color.White.copy(alpha = 0.22f))
            ) {
                if (animatedProgress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(6.dp)
                            .clip(Pill)
                            .background(barFillColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroCashflowColumn(
    label: String,
    amount: String,
    icon: ImageVector,
    badgeTint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(badgeTint.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = badgeTint,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White.copy(alpha = 0.78f)
            )
            Text(
                text = amount,
                style = SpendlyTextStyles.moneySmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HeroDebtMiniPill(
    label: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(Pill)
            .background(Color.White.copy(alpha = 0.16f))
            .border(1.dp, Color.White.copy(alpha = 0.24f), Pill)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            style = SpendlyTextStyles.moneyCaption.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HomeQuickActionsRow(
    hazeState: HazeState,
    onAddExpenseClicked: () -> Unit,
    onTopUpFundClicked: () -> Unit,
    onSettlementClicked: () -> Unit,
    onReportsClicked: () -> Unit,
    onGroupsClicked: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        QuickActionGlassTile(
            title = "Thêm chi",
            icon = Icons.AutoMirrored.Rounded.ReceiptLong,
            iconTint = colors.coralDeep,
            iconBg = colors.coralContainer,
            hazeState = hazeState,
            onClick = onAddExpenseClicked,
            modifier = Modifier.weight(1f)
        )
        QuickActionGlassTile(
            title = "Nạp thu",
            icon = Icons.Rounded.AddCard,
            iconTint = colors.mintDeep,
            iconBg = colors.mintContainer,
            hazeState = hazeState,
            onClick = onTopUpFundClicked,
            modifier = Modifier.weight(1f)
        )
        QuickActionGlassTile(
            title = "Đối soát",
            icon = Icons.Rounded.SwapHoriz,
            iconTint = colors.deepLavender,
            iconBg = colors.lavenderContainer,
            hazeState = hazeState,
            onClick = onSettlementClicked,
            modifier = Modifier.weight(1f)
        )
        QuickActionGlassTile(
            title = "Báo cáo",
            icon = Icons.Rounded.BarChart,
            iconTint = colors.sky,
            iconBg = colors.sky.copy(alpha = if (colors.isDark) 0.24f else 0.16f),
            hazeState = hazeState,
            onClick = onReportsClicked,
            modifier = Modifier.weight(1f)
        )
        QuickActionGlassTile(
            title = "Các quỹ",
            icon = Icons.Rounded.AccountBalanceWallet,
            iconTint = colors.amberDeep,
            iconBg = colors.amberContainer,
            hazeState = hazeState,
            onClick = onGroupsClicked,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionGlassTile(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    hazeState: HazeState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    GlassCard(
        modifier = modifier
            .clip(CardMedium)
            .clickable(role = Role.Button, onClick = onClick),
        hazeState = hazeState,
        shape = CardMedium,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(19.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HomeFundsSection(
    groupName: String,
    totalBalance: String,
    transactionCount: Int,
    budgetUsageRatio: Float,
    fundsList: List<HomeFundItem>,
    hazeState: HazeState,
    onGroupsClicked: () -> Unit,
    onFundClicked: (String) -> Unit,
    onActiveGroupClicked: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quỹ của tôi",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )

            Row(
                modifier = Modifier
                    .clip(Pill)
                    .clickable(role = Role.Button, onClick = onGroupsClicked)
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Xem tất cả",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.deepLavender
                )
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = colors.deepLavender,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (fundsList.isNotEmpty()) {
            fundsList.take(3).forEach { fund ->
                FundCard(
                    fundName = fund.name,
                    balanceFormatted = fund.balanceFormatted,
                    memberCount = fund.memberCount,
                    transactionCount = fund.transactionCount,
                    budgetRatio = fund.budgetRatio,
                    hazeState = hazeState,
                    onClick = { onFundClicked(fund.groupId) }
                )
            }
        } else {
            FundCard(
                fundName = groupName,
                subtitle = "Quỹ đang hoạt động",
                balanceFormatted = totalBalance,
                memberCount = 4,
                transactionCount = transactionCount,
                budgetRatio = budgetUsageRatio,
                hazeState = hazeState,
                onClick = onActiveGroupClicked
            )
        }
    }
}

@Composable
private fun FinSproutSuggestionCard(
    suggestionText: String?,
    budgetUsageRatio: Float,
    hazeState: HazeState,
    onChatbotClicked: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val resolvedTip = suggestionText?.takeIf { it.isNotBlank() } ?: when {
        budgetUsageRatio >= 0.80f ->
            "Chi tiêu đã vượt ${(budgetUsageRatio * 100f).roundToInt()}% ngân sách. Hãy hỏi FinSprout AI cách cân đối lại nhé!"
        budgetUsageRatio > 0f ->
            "Tiến độ ngân sách tháng đang ở mức ổn định (${(budgetUsageRatio * 100f).roundToInt()}%). Thử nhập nhanh chi tiêu bằng giọng nói/chat cùng FinSprout!"
        else ->
            "Nhập nhanh khoản chi bằng ngôn ngữ tự nhiên hoặc nhờ FinSprout tóm tắt tình hình quỹ chỉ trong 1 chạm."
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        tintColor = colors.lavenderContainer.copy(alpha = if (colors.isDark) 0.36f else 0.72f),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(SpendlyLavender, SpendlyLavenderDeep)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = "FinSprout AI",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "FinSprout AI Gợi ý",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(colors.mintDeep)
                    )
                }
                Text(
                    text = resolvedTip,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            SpendlyButton(
                text = "Hỏi AI",
                onClick = onChatbotClicked,
                height = 38.dp
            )
        }
    }
}

@Composable
private fun RecentTransactionsSection(
    transactions: List<RecentTransaction>,
    hazeState: HazeState,
    onViewAllClicked: () -> Unit,
    onTransactionClicked: (RecentTransaction) -> Unit
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
                text = "Giao dịch gần đây",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )

            Row(
                modifier = Modifier
                    .clip(Pill)
                    .clickable(role = Role.Button, onClick = onViewAllClicked)
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Xem tất cả",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.deepLavender
                )
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = colors.deepLavender,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (transactions.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colors.lavenderContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                        contentDescription = null,
                        tint = colors.onLavenderContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "Chưa có giao dịch nào trong quỹ",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary
                )
                Text(
                    text = "Bấm 'Thêm chi' hoặc 'Nạp thu' để bắt đầu ghi chép",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            transactions.forEachIndexed { index, item ->
                HomeTransactionRow(
                    transaction = item,
                    onClick = { onTransactionClicked(item) }
                )
                if (index < transactions.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        thickness = 0.8.dp,
                        color = colors.divider.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeTransactionRow(
    transaction: RecentTransaction,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors

    val iconVector = when (transaction.iconType) {
        TransactionIconType.GROCERY -> Icons.Rounded.ShoppingBag
        TransactionIconType.INTERNET -> Icons.Rounded.Wifi
        TransactionIconType.DEPOSIT -> Icons.Rounded.ArrowDownward
    }
    val iconBg = when {
        transaction.isIncome -> colors.mintContainer
        transaction.iconType == TransactionIconType.INTERNET -> colors.lavenderContainer
        else -> colors.coralContainer
    }
    val iconTint = when {
        transaction.isIncome -> colors.mintDeep
        transaction.iconType == TransactionIconType.INTERNET -> colors.onLavenderContainer
        else -> colors.coralDeep
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
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
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = transaction.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = transaction.amountFormatted,
            style = SpendlyTextStyles.moneySmall.copy(fontWeight = FontWeight.Bold),
            color = if (transaction.isIncome) colors.mintDeep else colors.coralDeep,
            maxLines = 1
        )
    }
}

@Composable
private fun HomeLegacyLavenderBottomBar(
    selectedTab: BottomNavTab,
    hazeState: HazeState,
    onTabSelected: (BottomNavTab) -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
            LegacyBottomTabItem(
                title = "Trang chủ",
                selected = selectedTab == BottomNavTab.HOME,
                selectedIcon = Icons.Rounded.Home,
                unselectedIcon = Icons.Outlined.Home,
                onClick = { onTabSelected(BottomNavTab.HOME) },
                modifier = Modifier.weight(1f)
            )
            LegacyBottomTabItem(
                title = "Lịch sử",
                selected = selectedTab == BottomNavTab.HISTORY,
                selectedIcon = Icons.AutoMirrored.Rounded.ReceiptLong,
                unselectedIcon = Icons.AutoMirrored.Outlined.ReceiptLong,
                onClick = { onTabSelected(BottomNavTab.HISTORY) },
                modifier = Modifier.weight(1f)
            )
            LegacyBottomTabItem(
                title = "Báo cáo",
                selected = selectedTab == BottomNavTab.REPORTS,
                selectedIcon = Icons.Rounded.BarChart,
                unselectedIcon = Icons.Outlined.BarChart,
                onClick = { onTabSelected(BottomNavTab.REPORTS) },
                modifier = Modifier.weight(1f)
            )
            LegacyBottomTabItem(
                title = "Cá nhân",
                selected = selectedTab == BottomNavTab.PROFILE,
                selectedIcon = Icons.Rounded.Person,
                unselectedIcon = Icons.Outlined.Person,
                onClick = { onTabSelected(BottomNavTab.PROFILE) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LegacyBottomTabItem(
    title: String,
    selected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    val activeColor = if (colors.isDark) colors.primaryLavender else colors.deepLavender
    val tint = if (selected) activeColor else colors.textMuted
    val pillBg = if (selected) {
        colors.lavenderContainer.copy(alpha = if (colors.isDark) 0.42f else 0.85f)
    } else {
        Color.Transparent
    }
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .clip(CardMedium)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(Pill)
                .background(pillBg)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = title,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = tint,
            maxLines = 1
        )
    }
}

@Preview(
    name = "07 · Trang chủ Spendly V2 (Light)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun HomeScreenPreview() {
    SpendlyTheme(darkTheme = false) {
        HomeScreen(
            youOweFormatted = "232.500 đ",
            youAreOwedFormatted = "637.500 đ",
            budgetUsageRatio = 0.42f,
            fundsList = listOf(
                HomeFundItem(
                    groupId = "g1",
                    name = "Quỹ Chi Tiêu Nhóm 08",
                    balanceFormatted = "8.490.000 đ",
                    memberCount = 4,
                    transactionCount = 12,
                    budgetRatio = 0.42f
                ),
                HomeFundItem(
                    groupId = "g2",
                    name = "Du Lịch Đà Lạt Cuối Tuần",
                    balanceFormatted = "3.200.000 đ",
                    memberCount = 6,
                    transactionCount = 9,
                    budgetRatio = 0.68f
                )
            ),
            recentTransactions = listOf(
                RecentTransaction(
                    id = "1",
                    title = "Đi chợ nấu lẩu cuối tuần",
                    subtitle = "Hôm nay · Hùng đã trả",
                    amountFormatted = "-480.000 đ",
                    isIncome = false,
                    iconType = TransactionIconType.GROCERY
                ),
                RecentTransaction(
                    id = "2",
                    title = "Nạp quỹ tháng 11",
                    subtitle = "Hôm qua · Việt Linh nạp quỹ",
                    amountFormatted = "+2.500.000 đ",
                    isIncome = true,
                    iconType = TransactionIconType.DEPOSIT
                ),
                RecentTransaction(
                    id = "3",
                    title = "Tiền Internet FPT",
                    subtitle = "2 ngày trước · Đạt đã trả",
                    amountFormatted = "-280.000 đ",
                    isIncome = false,
                    iconType = TransactionIconType.INTERNET
                )
            )
        )
    }
}

@Preview(
    name = "07 · Trang chủ Spendly V2 (Dark)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun HomeScreenDarkPreview() {
    SpendlyTheme(darkTheme = true) {
        HomeScreen(
            youOweFormatted = "0 đ",
            youAreOwedFormatted = "637.500 đ",
            budgetUsageRatio = 0.84f,
            recentTransactions = listOf(
                RecentTransaction(
                    id = "1",
                    title = "Thanh toán tiền nhà tháng 11",
                    subtitle = "Hôm nay · Hùng đã trả",
                    amountFormatted = "-4.500.000 đ",
                    isIncome = false,
                    iconType = TransactionIconType.INTERNET
                )
            )
        )
    }
}
