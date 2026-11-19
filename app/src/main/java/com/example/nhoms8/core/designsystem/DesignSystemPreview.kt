package com.example.nhoms8.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.QrCodeScanner
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nhoms8.ui.theme.SpendlyTextStyles
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.rememberHazeState

@Preview(name = "Spendly Theme & Typography (Light)", showBackground = true, widthDp = 412, heightDp = 760)
@Composable
private fun SpendlyThemePaletteLightPreview() {
    SpendlyTheme(darkTheme = false) {
        ThemePaletteShowcase()
    }
}

@Preview(name = "Spendly Theme & Typography (Dark)", showBackground = true, widthDp = 412, heightDp = 760)
@Composable
private fun SpendlyThemePaletteDarkPreview() {
    SpendlyTheme(darkTheme = true) {
        ThemePaletteShowcase()
    }
}

@Preview(name = "OrganicBackground + Haze Glass + BottomBar", showBackground = true, widthDp = 412, heightDp = 860)
@Composable
private fun SpendlyOrganicHazeGlassPreview() {
    SpendlyTheme(darkTheme = false) {
        DashboardComponentShowcase()
    }
}

@Preview(name = "FundCards Mint / Amber / Coral", showBackground = true, widthDp = 412, heightDp = 560)
@Composable
private fun SpendlyFundCardsPreview() {
    SpendlyTheme(darkTheme = false) {
        OrganicBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FundCard(
                    fundName = "Quỹ Chi Tiêu Nhóm 08",
                    subtitle = "An toàn · Dưới 50%",
                    balanceFormatted = "8.490.000 đ",
                    memberCount = 4,
                    transactionCount = 12,
                    budgetRatio = 0.36f
                )
                FundCard(
                    fundName = "Du Lịch Đà Lạt Cuối Tuần",
                    subtitle = "Cân nhắc · 50% - 80%",
                    balanceFormatted = "3.240.000 đ",
                    memberCount = 6,
                    transactionCount = 19,
                    budgetRatio = 0.68f
                )
                FundCard(
                    fundName = "Tiền Nhà & Sinh Hoạt Tháng 11",
                    subtitle = "Cảnh báo · Trên 80%",
                    balanceFormatted = "620.000 đ",
                    memberCount = 4,
                    transactionCount = 27,
                    budgetRatio = 0.92f
                )
            }
        }
    }
}

@Preview(name = "Spendly Buttons Showcase", showBackground = true, widthDp = 412, heightDp = 440)
@Composable
private fun SpendlyButtonsPreview() {
    SpendlyTheme(darkTheme = false) {
        val hazeState = rememberHazeState()
        OrganicBackground(hazeState = hazeState) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpendlyButton(
                    text = "Tạo khoản chi mới",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = Icons.Rounded.Add
                )
                SpendlyButton(
                    text = "Quét mã QR tham gia quỹ",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    style = SpendlyButtonStyle.SECONDARY_GLASS,
                    leadingIcon = Icons.Rounded.QrCodeScanner,
                    hazeState = hazeState
                )
                SpendlyButton(
                    text = "Hỏi trợ lý FinSprout AI",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    style = SpendlyButtonStyle.OUTLINED,
                    leadingIcon = Icons.Rounded.AutoAwesome
                )
                SpendlyButton(
                    text = "Đang đồng bộ Firestore...",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = true
                )
                SpendlyButton(
                    text = "Chưa đủ số dư",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )
            }
        }
    }
}

@Preview(name = "Dark Mode Haze + BottomBar", showBackground = true, widthDp = 412, heightDp = 860)
@Composable
private fun SpendlyBottomBarDarkPreview() {
    SpendlyTheme(darkTheme = true) {
        DashboardComponentShowcase()
    }
}

@Composable
private fun ThemePaletteShowcase() {
    val colors = MaterialTheme.spendlyColors
    OrganicBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Spendly Design System",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.textPrimary
            )
            Text(
                text = "Lavender · Organic · Haze 2.1.0 Glassmorphism",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ColorSwatch("Lavender", colors.primaryLavender)
                ColorSwatch("Deep", colors.deepLavender)
                ColorSwatch("Mint", colors.mint)
                ColorSwatch("Amber", colors.amber)
                ColorSwatch("Coral", colors.coralDeep)
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Tabular Numbers (tnum)",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "12.850.000 đ",
                    style = SpendlyTextStyles.moneyHero,
                    color = colors.deepLavender
                )
                Text(
                    text = "+4.200.000 đ  |  -1.150.000 đ",
                    style = SpendlyTextStyles.moneyMedium,
                    color = colors.mintDeep
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(label: String, color: Color) {
    val colors = MaterialTheme.spendlyColors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textMuted
        )
    }
}

@Composable
private fun DashboardComponentShowcase() {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()
    var selectedTab by remember { mutableStateOf(SpendlyTab.HOME) }

    Box(modifier = Modifier.fillMaxSize()) {
        OrganicBackground(
            modifier = Modifier.fillMaxSize(),
            hazeState = hazeState
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GradientCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Tổng số dư các quỹ",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "12.350.000 đ",
                    style = SpendlyTextStyles.moneyHero
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "3 quỹ hoạt động · 12 thành viên",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState
            ) {
                Text(
                    text = "Thẻ Glassmorphism Haze 2.1.0",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Làm mờ nền Organic pastel phía sau với đường viền sáng tinh tế.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted
                )
            }

            FundCard(
                fundName = "Quỹ Chi Tiêu Nhóm 08",
                subtitle = "Nhóm hoạt động chính",
                balanceFormatted = "8.490.000 đ",
                memberCount = 4,
                transactionCount = 15,
                budgetRatio = 0.42f,
                hazeState = hazeState
            )

            SpendlyButton(
                text = "Thêm khoản chi nhanh",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Rounded.Add
            )
        }

        SpendlyBottomBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            unreadNotificationsCount = 4,
            hazeState = hazeState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
