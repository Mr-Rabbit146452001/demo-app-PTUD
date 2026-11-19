package com.example.nhoms8.feature.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nhoms8.algorithm.FintechAlgorithm
import com.example.nhoms8.core.designsystem.FundCard
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.core.designsystem.SpendlyButton
import com.example.nhoms8.core.designsystem.SpendlyButtonStyle
import com.example.nhoms8.domain.model.SpendlyGroup
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState

/**
 * Màn hình Danh sách các quỹ nhóm (Multi-Group Management).
 */
@Composable
fun GroupListScreen(
    viewModel: GroupViewModel,
    onGroupClick: (String) -> Unit,
    onCreateGroupClick: () -> Unit,
    onJoinGroupClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.groupsUiState.collectAsStateWithLifecycle()

    GroupListContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::updateSearchQuery,
        onGroupClick = { groupId ->
            viewModel.setActiveGroup(groupId)
            onGroupClick(groupId)
        },
        onCreateGroupClick = onCreateGroupClick,
        onJoinGroupClick = onJoinGroupClick,
        onSetActiveGroup = viewModel::setActiveGroup,
        onDismissMessage = viewModel::clearMessages,
        modifier = modifier
    )
}

/**
 * Giao diện thuần trạng thái cho màn hình Danh sách quỹ nhóm.
 */
@Composable
fun GroupListContent(
    uiState: GroupListUiState,
    onSearchQueryChange: (String) -> Unit,
    onGroupClick: (String) -> Unit,
    onCreateGroupClick: () -> Unit,
    onJoinGroupClick: () -> Unit,
    onSetActiveGroup: (String) -> Unit = {},
    onDismissMessage: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()
    val displayedGroups = uiState.filteredGroups

    OrganicBackground(
        modifier = modifier.fillMaxSize(),
        hazeState = hazeState
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 108.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Các quỹ của tôi",
                            style = MaterialTheme.typography.headlineLarge,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${uiState.groups.size} quỹ nhóm đang hoạt động",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(SpendlyShapeTokens.Pill)
                            .background(colors.lavenderContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${uiState.groups.size} quỹ",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.onLavenderContainer
                        )
                    }
                }
            }

            item(key = "search_pill") {
                GroupSearchPill(
                    query = uiState.searchQuery,
                    onQueryChange = onSearchQueryChange,
                    hazeState = hazeState
                )
            }

            item(key = "quick_actions") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SpendlyButton(
                        text = "Tạo quỹ mới",
                        onClick = onCreateGroupClick,
                        modifier = Modifier.weight(1f),
                        style = SpendlyButtonStyle.PRIMARY,
                        leadingIcon = Icons.Rounded.Add
                    )
                    SpendlyButton(
                        text = "Tham gia quỹ",
                        onClick = onJoinGroupClick,
                        modifier = Modifier.weight(1f),
                        style = SpendlyButtonStyle.SECONDARY_GLASS,
                        leadingIcon = Icons.Rounded.QrCodeScanner,
                        hazeState = hazeState
                    )
                }
            }

            val bannerText = uiState.errorMessage ?: uiState.successMessage
            if (!bannerText.isNullOrBlank()) {
                item(key = "feedback_banner") {
                    val isError = uiState.errorMessage != null
                    FeedbackBanner(
                        message = bannerText,
                        isError = isError,
                        onDismiss = onDismissMessage
                    )
                }
            }

            if (uiState.isLoading && uiState.groups.isEmpty()) {
                item(key = "loading_state") {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        hazeState = hazeState
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = colors.primaryLavender,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Đang đồng bộ danh sách quỹ...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textMuted
                            )
                        }
                    }
                }
            } else if (displayedGroups.isEmpty()) {
                item(key = "empty_state") {
                    EmptyGroupsCard(
                        hasSearchQuery = uiState.searchQuery.isNotBlank(),
                        hazeState = hazeState,
                        onClearSearch = { onSearchQueryChange("") },
                        onCreateGroupClick = onCreateGroupClick,
                        onJoinGroupClick = onJoinGroupClick
                    )
                }
            } else {
                items(
                    items = displayedGroups,
                    key = { it.groupId }
                ) { group ->
                    val stats = uiState.statsFor(group.groupId)
                    val isActive = group.groupId == uiState.activeGroupId
                    val memberTotal = group.memberCount.coerceAtLeast(group.memberIds.size).coerceAtLeast(1)

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (isActive) {
                            ActiveGroupBadgeRow(
                                inviteCode = group.inviteCode
                            )
                        } else {
                            InactiveGroupQuickSwitchRow(
                                inviteCode = group.inviteCode,
                                onSetActive = { onSetActiveGroup(group.groupId) }
                            )
                        }

                        FundCard(
                            fundName = group.name,
                            subtitle = if (isActive) {
                                "Quỹ hiện tại · Hạn mức ${FintechAlgorithm.formatShortVnd(group.monthlyBudgetLimit)}"
                            } else {
                                group.description.ifBlank {
                                    "Hạn mức ${FintechAlgorithm.formatShortVnd(group.monthlyBudgetLimit)} / tháng"
                                }
                            },
                            balanceFormatted = FintechAlgorithm.formatVnd(stats.balance),
                            memberCount = memberTotal,
                            transactionCount = stats.transactionCount,
                            budgetRatio = stats.budgetRatio,
                            budgetCaption = "Ngân sách",
                            hazeState = hazeState,
                            modifier = if (isActive) {
                                Modifier.border(
                                    width = 1.5.dp,
                                    color = colors.primaryLavender.copy(alpha = 0.72f),
                                    shape = SpendlyShapeTokens.CardLarge
                                )
                            } else {
                                Modifier
                            },
                            onClick = { onGroupClick(group.groupId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupSearchPill(
    query: String,
    onQueryChange: (String) -> Unit,
    hazeState: HazeState
) {
    val colors = MaterialTheme.spendlyColors

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        shape = SpendlyShapeTokens.Pill,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "Tìm quỹ theo tên hoặc mã mời...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.deepLavender),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Xóa tìm kiếm",
                        tint = colors.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveGroupBadgeRow(inviteCode: String) {
    val colors = MaterialTheme.spendlyColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(SpendlyShapeTokens.Pill)
                .background(colors.mintContainer)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = colors.mintDeep,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "Đang hoạt động",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = colors.mintDeep
            )
        }

        if (inviteCode.isNotBlank()) {
            Text(
                text = "Mã mời: $inviteCode",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted
            )
        }
    }
}

@Composable
private fun InactiveGroupQuickSwitchRow(
    inviteCode: String,
    onSetActive: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (inviteCode.isNotBlank()) "Mã mời: $inviteCode" else "Quỹ nhóm",
            style = MaterialTheme.typography.labelSmall,
            color = colors.textMuted
        )

        Text(
            text = "Đặt làm quỹ chính",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = colors.deepLavender,
            modifier = Modifier
                .clip(SpendlyShapeTokens.Pill)
                .clickable(onClick = onSetActive)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun FeedbackBanner(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val container = if (isError) colors.coralContainer else colors.mintContainer
    val content = if (isError) colors.coralDeep else colors.mintDeep

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SpendlyShapeTokens.CardMedium)
            .background(container)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = content,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(22.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Đóng thông báo",
                tint = content,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun EmptyGroupsCard(
    hasSearchQuery: Boolean,
    hazeState: HazeState,
    onClearSearch: () -> Unit,
    onCreateGroupClick: () -> Unit,
    onJoinGroupClick: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(SpendlyShapeTokens.CardMedium)
                    .background(colors.lavenderContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    tint = colors.deepLavender,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = if (hasSearchQuery) "Không tìm thấy quỹ phù hợp" else "Bạn chưa tham gia quỹ nào",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary
            )

            Text(
                text = if (hasSearchQuery) {
                    "Thử tìm theo tên khác hoặc xóa từ khóa tìm kiếm."
                } else {
                    "Tạo quỹ nhóm mới cho phòng trọ, chuyến du lịch hoặc nhập mã mời để tham gia."
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted
            )

            if (hasSearchQuery) {
                SpendlyButton(
                    text = "Xóa bộ lọc tìm kiếm",
                    onClick = onClearSearch,
                    style = SpendlyButtonStyle.OUTLINED
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SpendlyButton(
                        text = "Tạo quỹ mới",
                        onClick = onCreateGroupClick,
                        modifier = Modifier.weight(1f)
                    )
                    SpendlyButton(
                        text = "Nhập mã mời",
                        onClick = onJoinGroupClick,
                        modifier = Modifier.weight(1f),
                        style = SpendlyButtonStyle.OUTLINED
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 840)
@Composable
private fun GroupListScreenPreview() {
    SpendlyTheme {
        val sampleGroups = listOf(
            SpendlyGroup(
                groupId = "grp_1",
                name = "Quỹ Chi Tiêu Nhóm 08",
                description = "Chi tiêu đồ án và sinh hoạt hàng tuần",
                inviteCode = "N08PRO",
                memberCount = 4,
                monthlyBudgetLimit = 10_000_000L,
                memberIds = listOf("user_hung", "u2", "u3", "u4")
            ),
            SpendlyGroup(
                groupId = "grp_2",
                name = "Du lịch Đà Lạt Cuối Tuần",
                description = "Xe khách, homestay, ăn uống",
                inviteCode = "DL2026",
                memberCount = 6,
                monthlyBudgetLimit = 6_000_000L,
                memberIds = listOf("user_hung", "u2")
            )
        )
        GroupListContent(
            uiState = GroupListUiState(
                groups = sampleGroups,
                groupStats = mapOf(
                    "grp_1" to GroupStats(
                        totalIncome = 10_000_000L,
                        totalExpense = 3_600_000L,
                        balance = 6_400_000L,
                        transactionCount = 12,
                        budgetRatio = 0.36f
                    ),
                    "grp_2" to GroupStats(
                        totalIncome = 6_000_000L,
                        totalExpense = 4_950_000L,
                        balance = 1_050_000L,
                        transactionCount = 9,
                        budgetRatio = 0.83f
                    )
                ),
                activeGroupId = "grp_1",
                isLoading = false
            ),
            onSearchQueryChange = {},
            onGroupClick = {},
            onCreateGroupClick = {},
            onJoinGroupClick = {}
        )
    }
}
