package com.example.nhoms8.feature.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.ui.theme.SpendlyIndigo
import com.example.nhoms8.ui.theme.SpendlyLavender
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlySky
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.HazeState

/**
 * Bottom Sheet trung tâm được mở từ nút `+` giữa thanh điều hướng `SpendlyBottomBar`,
 * cung cấp 3 lựa chọn nhanh: Thêm khoản chi, Thêm khoản thu / Nạp quỹ và Ghi nhanh bằng FinSprout AI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionModalSheet(
    onDismissRequest: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onAddIncomeClick: () -> Unit,
    onAiQuickEntryClick: () -> Unit,
    modifier: Modifier = Modifier,
    activeGroupName: String? = null,
    @Suppress("UNUSED_PARAMETER")
    hazeState: HazeState? = null
) {
    val colors = MaterialTheme.spendlyColors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        shape = SpendlyShapeTokens.BottomSheet,
        containerColor = colors.surfaceElevated,
        contentColor = colors.textPrimary,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = colors.divider)
        }
    ) {
        AddTransactionSheetContent(
            activeGroupName = activeGroupName,
            onAddExpenseClick = {
                onDismissRequest()
                onAddExpenseClick()
            },
            onAddIncomeClick = {
                onDismissRequest()
                onAddIncomeClick()
            },
            onAiQuickEntryClick = {
                onDismissRequest()
                onAiQuickEntryClick()
            }
        )
    }
}

@Composable
internal fun AddTransactionSheetContent(
    activeGroupName: String?,
    onAddExpenseClick: () -> Unit,
    onAddIncomeClick: () -> Unit,
    onAiQuickEntryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Ghi nhận giao dịch mới",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textPrimary
            )
            if (!activeGroupName.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .clip(SpendlyShapeTokens.Pill)
                        .background(colors.lavenderContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quỹ đang chọn: $activeGroupName",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onLavenderContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Text(
                    text = "Chọn loại giao dịch bạn muốn thêm vào quỹ nhóm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        TransactionSheetOptionCard(
            title = "Thêm khoản chi",
            subtitle = "Ghi nhận chi tiêu và chia bill theo đều, % hoặc số tiền tùy chỉnh",
            icon = Icons.AutoMirrored.Rounded.ReceiptLong,
            iconBackground = Brush.linearGradient(
                colors = listOf(colors.primaryLavender, colors.deepLavender)
            ),
            tintColor = colors.lavenderContainer.copy(alpha = if (colors.isDark) 0.25f else 0.48f),
            onClick = onAddExpenseClick
        )

        TransactionSheetOptionCard(
            title = "Thêm khoản thu / Nạp quỹ",
            subtitle = "Đóng góp tiền vào quỹ chung (tách biệt với trả nợ cá nhân)",
            icon = Icons.Rounded.Savings,
            iconBackground = Brush.linearGradient(
                colors = listOf(colors.mintDeep, colors.mint)
            ),
            tintColor = colors.mintContainer.copy(alpha = if (colors.isDark) 0.28f else 0.55f),
            onClick = onAddIncomeClick
        )

        TransactionSheetOptionCard(
            title = "Ghi nhanh bằng FinSprout AI",
            subtitle = "Nhập câu nói tự nhiên để AI tự bóc tách số tiền & danh mục",
            icon = Icons.Rounded.AutoAwesome,
            iconBackground = Brush.linearGradient(
                colors = listOf(SpendlyLavender, SpendlyIndigo, SpendlySky)
            ),
            tintColor = colors.glassContainer,
            badgeText = "AI",
            onClick = onAiQuickEntryClick
        )
    }
}

@Composable
private fun TransactionSheetOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBackground: Brush,
    tintColor: Color,
    onClick: () -> Unit,
    badgeText: String? = null
) {
    val colors = MaterialTheme.spendlyColors
    val cardShape = SpendlyShapeTokens.CardLarge

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .clickable(role = Role.Button, onClick = onClick),
        hazeState = null,
        shape = cardShape,
        tintColor = tintColor,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!badgeText.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(SpendlyShapeTokens.Pill)
                                .background(colors.lavenderContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.onLavenderContainer
                            )
                        }
                    }
                }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Preview(name = "AddTransactionModalSheet Preview", showBackground = true, widthDp = 412, heightDp = 420)
@Composable
private fun AddTransactionModalSheetPreview() {
    SpendlyTheme {
        OrganicBackground(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .clip(SpendlyShapeTokens.BottomSheet)
                    .background(MaterialTheme.spendlyColors.surfaceElevated)
                    .padding(top = 12.dp)
            ) {
                AddTransactionSheetContent(
                    activeGroupName = "Quỹ Chi Tiêu Nhóm 08",
                    onAddExpenseClick = {},
                    onAddIncomeClick = {},
                    onAiQuickEntryClick = {}
                )
            }
        }
    }
}
