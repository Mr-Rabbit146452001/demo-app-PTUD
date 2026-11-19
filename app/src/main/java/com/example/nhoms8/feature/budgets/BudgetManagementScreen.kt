package com.example.nhoms8.feature.budgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nhoms8.algorithm.FintechAlgorithm
import com.example.nhoms8.core.designsystem.BudgetProgressRing
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.GradientCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.core.designsystem.SpendlyButton
import com.example.nhoms8.domain.model.BudgetHealthLevel
import com.example.nhoms8.domain.model.BudgetStatusSummary
import com.example.nhoms8.domain.model.SpendlyBudget
import com.example.nhoms8.domain.model.SpendlyCategory
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlySkyTint
import com.example.nhoms8.ui.theme.SpendlyTextStyles
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState
import kotlin.math.roundToInt

private data class BudgetLimitPreset(
    val label: String,
    val amount: Long
)

private val BUDGET_LIMIT_PRESETS = listOf(
    BudgetLimitPreset("2 tr", 2_000_000L),
    BudgetLimitPreset("5 tr", 5_000_000L),
    BudgetLimitPreset("10 tr", 10_000_000L),
    BudgetLimitPreset("20 tr", 20_000_000L)
)

private data class BudgetHealthStyle(
    val containerColor: Color,
    val contentColor: Color,
    val accentColor: Color,
    val icon: ImageVector,
    val thresholdLabel: String
)

/**
 * Màn hình Quản lý Ngân sách nhóm & danh mục với cảnh báo sức khỏe chi tiêu 4 cấp độ
 * (Bình thường 0-50%, Theo dõi 50-80%, Cảnh báo 80-100%, Vượt ngân sách >100%).
 */
@Composable
fun BudgetManagementScreen(
    groupId: String,
    currentUserId: String,
    viewModel: BudgetViewModel,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by remember(groupId, viewModel) {
        viewModel.observeGroupBudgets(groupId)
    }.collectAsStateWithLifecycle()

    BudgetManagementContent(
        uiState = uiState,
        onBackClicked = {
            viewModel.clearMessages()
            onBackClicked()
        },
        onUpsertBudget = { budgetId, name, limitAmount, category, onDone ->
            viewModel.upsertBudget(
                groupId = groupId,
                budgetId = budgetId,
                name = name,
                limitAmount = limitAmount,
                category = category,
                createdBy = currentUserId,
                onSuccess = onDone
            )
        },
        onDeleteBudget = { budgetId ->
            viewModel.deleteBudget(groupId = groupId, budgetId = budgetId)
        },
        onDismissMessage = viewModel::clearMessages,
        modifier = modifier
    )
}

/**
 * Giao diện thuần trạng thái cho màn hình Quản lý Ngân sách 4 cấp độ.
 */
@Composable
fun BudgetManagementContent(
    uiState: BudgetUiState,
    onBackClicked: () -> Unit = {},
    onUpsertBudget: (
        budgetId: String?,
        name: String,
        limitAmount: Long,
        category: SpendlyCategory?,
        onSuccess: () -> Unit
    ) -> Unit = { _, _, _, _, _ -> },
    onDeleteBudget: (String) -> Unit = {},
    onDismissMessage: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()

    var showEditorDialog by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<SpendlyBudget?>(null) }
    var deletingBudget by remember { mutableStateOf<SpendlyBudget?>(null) }

    OrganicBackground(
        modifier = modifier.fillMaxSize(),
        hazeState = hazeState
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primaryLavender)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item(key = "top_bar") {
                    BudgetTopBar(
                        title = "Quản lý Ngân sách",
                        subtitle = uiState.groupName.ifBlank { "Quỹ chi tiêu Spendly" },
                        onBackClicked = onBackClicked,
                        onAddClick = {
                            editingBudget = null
                            showEditorDialog = true
                        },
                        hazeState = hazeState
                    )
                }

                item(key = "hero_summary") {
                    OverallBudgetHeroCard(
                        summary = uiState.overallSummary,
                        onEditOverallClick = {
                            editingBudget = uiState.overallSummary.budget
                            showEditorDialog = true
                        }
                    )
                }

                val feedbackMessage = uiState.errorMessage ?: uiState.successMessage
                if (!feedbackMessage.isNullOrBlank()) {
                    item(key = "feedback_banner") {
                        BudgetFeedbackBanner(
                            message = feedbackMessage,
                            isError = uiState.errorMessage != null,
                            onDismiss = onDismissMessage
                        )
                    }
                }

                item(key = "threshold_guide") {
                    BudgetThresholdsExplainerCard(
                        overallLevel = uiState.overallSummary.level,
                        overallPercent = uiState.overallSummary.usagePercent,
                        hazeState = hazeState
                    )
                }

                item(key = "section_header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Ngân sách đã thiết lập",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "${uiState.budgetSummaries.size} cấu hình đang theo dõi",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textMuted
                            )
                        }
                    }
                }

                if (uiState.budgetSummaries.isEmpty()) {
                    item(key = "empty_budgets") {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            hazeState = hazeState,
                            contentPadding = PaddingValues(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = colors.primaryLavender,
                                    modifier = Modifier.size(34.dp)
                                )
                                Text(
                                    text = "Chưa có ngân sách riêng theo danh mục",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Thêm ngân sách cho Toàn nhóm hoặc từng danh mục (Ăn uống, Di chuyển...) để nhận cảnh báo sớm theo 4 cấp độ.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(
                        items = uiState.budgetSummaries,
                        key = { it.budget.budgetId.ifBlank { it.budget.name + "_" + it.budget.category?.name } }
                    ) { summary ->
                        BudgetSummaryCard(
                            summary = summary,
                            hazeState = hazeState,
                            onEditClick = {
                                editingBudget = summary.budget
                                showEditorDialog = true
                            },
                            onDeleteClick = {
                                deletingBudget = summary.budget
                            }
                        )
                    }
                }

                item(key = "add_budget_cta") {
                    SpendlyButton(
                        text = "+ Thêm ngân sách",
                        onClick = {
                            editingBudget = null
                            showEditorDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = Icons.Rounded.Add
                    )
                }
            }
        }
    }

    if (showEditorDialog) {
        UpsertBudgetDialog(
            initialBudget = editingBudget,
            isSaving = uiState.isSaving,
            onDismiss = {
                if (!uiState.isSaving) showEditorDialog = false
            },
            onSave = { budgetId, name, limitAmount, category ->
                onUpsertBudget(budgetId, name, limitAmount, category) {
                    showEditorDialog = false
                    editingBudget = null
                }
            }
        )
    }

    val targetDelete = deletingBudget
    if (targetDelete != null) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isSaving) deletingBudget = null
            },
            containerColor = colors.surfaceElevated,
            title = {
                Text(
                    text = "Xóa ngân sách?",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Bạn muốn xóa cấu hình \"${targetDelete.name}\"? Lịch sử các khoản chi đã ghi nhận sẽ không bị ảnh hưởng.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !uiState.isSaving,
                    onClick = {
                        deletingBudget = null
                        onDeleteBudget(targetDelete.budgetId)
                    }
                ) {
                    Text(
                        text = "Xóa ngân sách",
                        color = colors.coralDeep,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !uiState.isSaving,
                    onClick = { deletingBudget = null }
                ) {
                    Text("Giữ lại", color = colors.textMuted)
                }
            }
        )
    }
}

@Composable
private fun BudgetTopBar(
    title: String,
    subtitle: String,
    onBackClicked: () -> Unit,
    onAddClick: () -> Unit,
    hazeState: HazeState
) {
    val colors = MaterialTheme.spendlyColors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GlassCard(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onBackClicked),
            hazeState = hazeState,
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
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

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.lavenderContainer)
                .clickable(role = Role.Button, onClick = onAddClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Thêm ngân sách",
                tint = colors.deepLavender,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun OverallBudgetHeroCard(
    summary: BudgetStatusSummary,
    onEditOverallClick: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val progressRatio = (summary.usagePercent / 100f).coerceAtLeast(0f)
    val isExceeded = summary.remainingAmount < 0L

    GradientCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NGÂN SÁCH TỔNG NHÓM",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White.copy(alpha = 0.85f)
            )
            BudgetHealthBadge(level = summary.level)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Hạn mức tháng",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.82f)
                )
                Text(
                    text = FintechAlgorithm.formatVnd(summary.budget.limitAmount),
                    style = SpendlyTextStyles.moneyHero,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Đã chi: ${FintechAlgorithm.formatVnd(summary.spentAmount)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = Color.White.copy(alpha = 0.92f)
                )
                Text(
                    text = if (isExceeded) {
                        "Vượt hạn mức: ${FintechAlgorithm.formatVnd(-summary.remainingAmount)}"
                    } else {
                        "Còn lại: ${FintechAlgorithm.formatVnd(summary.remainingAmount)}"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isExceeded) colors.coral else colors.mint
                )
            }

            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center
            ) {
                BudgetProgressRing(
                    progress = progressRatio,
                    size = 70.dp,
                    strokeWidth = 7.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .clip(SpendlyShapeTokens.Pill)
                .background(Color.White.copy(alpha = 0.22f))
                .border(1.dp, Color.White.copy(alpha = 0.30f), SpendlyShapeTokens.Pill)
                .clickable(onClick = onEditOverallClick)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "Điều chỉnh hạn mức tổng",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BudgetThresholdsExplainerCard(
    overallLevel: BudgetHealthLevel,
    overallPercent: Float,
    hazeState: HazeState
) {
    val colors = MaterialTheme.spendlyColors
    val activeStyle = healthStyleFor(overallLevel)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(activeStyle.containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = activeStyle.icon,
                    contentDescription = null,
                    tint = activeStyle.contentColor,
                    modifier = Modifier.size(17.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hệ thống cảnh báo 4 cấp độ (${overallPercent.roundToInt()}%)",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary
                )
                Text(
                    text = healthDescriptionFor(overallLevel),
                    style = MaterialTheme.typography.bodySmall,
                    color = activeStyle.contentColor
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BudgetHealthLevel.entries.forEach { level ->
                val style = healthStyleFor(level)
                val isCurrent = level == overallLevel
                Row(
                    modifier = Modifier
                        .clip(SpendlyShapeTokens.Pill)
                        .background(style.containerColor)
                        .then(
                            if (isCurrent) {
                                Modifier.border(1.2.dp, style.accentColor, SpendlyShapeTokens.Pill)
                            } else {
                                Modifier
                            }
                        )
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(style.accentColor)
                    )
                    Text(
                        text = "${level.label} (${style.thresholdLabel})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = style.contentColor
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetSummaryCard(
    summary: BudgetStatusSummary,
    hazeState: HazeState,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val budget = summary.budget
    val style = healthStyleFor(summary.level)
    val clampedProgress = (summary.usagePercent / 100f).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = tween(durationMillis = 450),
        label = "budget_bar_progress"
    )
    val isExceeded = summary.remainingAmount < 0L

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(SpendlyShapeTokens.CardSmall)
                    .background(colors.lavenderContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = budget.category?.icon() ?: Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    tint = colors.deepLavender,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = budget.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = budget.category?.label ?: "Toàn nhóm",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted
                )
            }

            BudgetHealthBadge(level = summary.level)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Đã chi ${FintechAlgorithm.formatVnd(summary.spentAmount)} / ${FintechAlgorithm.formatVnd(budget.limitAmount)}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = colors.textPrimary
            )
            Text(
                text = "${summary.usagePercent.roundToInt()}%",
                style = SpendlyTextStyles.moneySmall,
                color = style.contentColor
            )
        }

        Spacer(modifier = Modifier.height(7.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(SpendlyShapeTokens.Pill)
                .background(colors.divider.copy(alpha = 0.55f))
        ) {
            if (animatedFraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedFraction)
                        .fillMaxHeight()
                        .clip(SpendlyShapeTokens.Pill)
                        .background(style.accentColor)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isExceeded) {
                    "Vượt ${FintechAlgorithm.formatVnd(-summary.remainingAmount)}"
                } else {
                    "Còn lại ${FintechAlgorithm.formatVnd(summary.remainingAmount)}"
                },
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isExceeded) colors.coralDeep else colors.textMuted
            )

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Sửa ngân sách",
                        tint = colors.deepLavender,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Xóa ngân sách",
                        tint = colors.coralDeep,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetHealthBadge(level: BudgetHealthLevel) {
    val style = healthStyleFor(level)
    Row(
        modifier = Modifier
            .clip(SpendlyShapeTokens.Pill)
            .background(style.containerColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = style.icon,
            contentDescription = null,
            tint = style.contentColor,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = level.label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = style.contentColor
        )
    }
}

@Composable
private fun BudgetFeedbackBanner(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val bg = if (isError) colors.coralContainer else colors.mintContainer
    val border = if (isError) colors.coral else colors.mint
    val fg = if (isError) colors.coralDeep else colors.mintDeep
    val icon = if (isError) Icons.Rounded.ErrorOutline else Icons.Rounded.CheckCircle

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SpendlyShapeTokens.CardMedium)
            .background(bg)
            .border(1.dp, border, SpendlyShapeTokens.CardMedium)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = fg,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Đóng",
                tint = fg,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UpsertBudgetDialog(
    initialBudget: SpendlyBudget?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        budgetId: String?,
        name: String,
        limitAmount: Long,
        category: SpendlyCategory?
    ) -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val isEdit = initialBudget != null && initialBudget.budgetId.isNotBlank()

    var selectedCategory by remember(initialBudget) {
        mutableStateOf(initialBudget?.category)
    }
    var nameText by remember(initialBudget) {
        mutableStateOf(
            initialBudget?.name?.takeIf { it.isNotBlank() }
                ?: initialBudget?.category?.let { "Ngân sách ${it.label}" }
                ?: "Ngân sách tổng tháng"
        )
    }
    var hasCustomName by remember(initialBudget) {
        mutableStateOf(isEdit)
    }
    var limitDigits by remember(initialBudget) {
        mutableStateOf(
            initialBudget?.limitAmount?.takeIf { it > 0L }?.toString() ?: "5000000"
        )
    }
    var localError by remember(initialBudget) { mutableStateOf<String?>(null) }

    val parsedLimit = limitDigits.toLongOrNull() ?: 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceElevated,
        title = {
            Text(
                text = if (isEdit) "Sửa ngân sách" else "+ Thêm / Sửa ngân sách",
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = {
                        nameText = it
                        hasCustomName = true
                        localError = null
                    },
                    label = { Text("Tên ngân sách *") },
                    placeholder = { Text("VD: Ngân sách Ăn uống tháng 11") },
                    singleLine = true,
                    shape = SpendlyShapeTokens.CardSmall,
                    colors = budgetTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Hạn mức nhanh",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textPrimary
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BUDGET_LIMIT_PRESETS.forEach { preset ->
                        val selected = parsedLimit == preset.amount
                        Box(
                            modifier = Modifier
                                .clip(SpendlyShapeTokens.Pill)
                                .background(if (selected) colors.deepLavender else colors.lavenderContainer)
                                .clickable {
                                    limitDigits = preset.amount.toString()
                                    localError = null
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = preset.label,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (selected) Color.White else colors.onLavenderContainer
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = limitDigits,
                    onValueChange = { raw ->
                        limitDigits = raw.filter { it.isDigit() }.take(12)
                        localError = null
                    },
                    label = { Text("Hạn mức tùy chỉnh (đ) *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = SpendlyShapeTokens.CardSmall,
                    colors = budgetTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (parsedLimit > 0L) {
                    Text(
                        text = "${FintechAlgorithm.formatVnd(parsedLimit)} (${FintechAlgorithm.convertNumberToWords(parsedLimit)})",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.deepLavender
                    )
                }

                Text(
                    text = "Phạm vi danh mục",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textPrimary
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryFilterChip(
                        label = "Toàn nhóm",
                        icon = Icons.Rounded.AccountBalanceWallet,
                        selected = selectedCategory == null,
                        onClick = {
                            selectedCategory = null
                            if (!hasCustomName) {
                                nameText = "Ngân sách tổng tháng"
                            }
                        }
                    )
                    SpendlyCategory.entries.forEach { cat ->
                        CategoryFilterChip(
                            label = cat.label,
                            icon = cat.icon(),
                            selected = selectedCategory == cat,
                            onClick = {
                                selectedCategory = cat
                                if (!hasCustomName) {
                                    nameText = "Ngân sách ${cat.label}"
                                }
                            }
                        )
                    }
                }

                if (!localError.isNullOrBlank()) {
                    Text(
                        text = localError.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.coralDeep
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    val trimmedName = nameText.trim()
                    when {
                        trimmedName.isBlank() -> localError = "Vui lòng nhập tên ngân sách"
                        parsedLimit <= 0L -> localError = "Hạn mức ngân sách phải lớn hơn 0 đ"
                        else -> onSave(
                            initialBudget?.budgetId?.takeIf { it.isNotBlank() },
                            trimmedName,
                            parsedLimit,
                            selectedCategory
                        )
                    }
                }
            ) {
                Text(
                    text = if (isSaving) "Đang lưu..." else "Lưu ngân sách",
                    color = colors.deepLavender,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(enabled = !isSaving, onClick = onDismiss) {
                Text("Hủy", color = colors.textMuted)
            }
        }
    )
}

@Composable
private fun CategoryFilterChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val bg = if (selected) colors.deepLavender else colors.surface
    val fg = if (selected) Color.White else colors.textPrimary
    val border = if (selected) colors.deepLavender else colors.divider

    Row(
        modifier = Modifier
            .clip(SpendlyShapeTokens.Pill)
            .background(bg)
            .border(1.dp, border, SpendlyShapeTokens.Pill)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = fg
        )
    }
}

@Composable
private fun healthStyleFor(level: BudgetHealthLevel): BudgetHealthStyle {
    val colors = MaterialTheme.spendlyColors
    return when (level) {
        BudgetHealthLevel.NORMAL -> BudgetHealthStyle(
            containerColor = colors.mintContainer,
            contentColor = colors.mintDeep,
            accentColor = colors.mintDeep,
            icon = Icons.Rounded.CheckCircle,
            thresholdLabel = "0–50%"
        )
        BudgetHealthLevel.WATCH -> BudgetHealthStyle(
            containerColor = if (colors.isDark) Color(0xFF184159) else SpendlySkyTint,
            contentColor = if (colors.isDark) colors.sky else Color(0xFF1B577B),
            accentColor = colors.sky,
            icon = Icons.Rounded.Info,
            thresholdLabel = "50–80%"
        )
        BudgetHealthLevel.WARNING -> BudgetHealthStyle(
            containerColor = colors.amberContainer,
            contentColor = colors.amberDeep,
            accentColor = colors.amberDeep,
            icon = Icons.Rounded.WarningAmber,
            thresholdLabel = "80–100%"
        )
        BudgetHealthLevel.EXCEEDED -> BudgetHealthStyle(
            containerColor = colors.coralContainer,
            contentColor = colors.coralDeep,
            accentColor = colors.coralDeep,
            icon = Icons.Rounded.NotificationsActive,
            thresholdLabel = ">100%"
        )
    }
}

private fun healthDescriptionFor(level: BudgetHealthLevel): String = when (level) {
    BudgetHealthLevel.NORMAL -> "Chi tiêu đang trong vùng an toàn (dưới 50% hạn mức)."
    BudgetHealthLevel.WATCH -> "Đã sử dụng 50–80% ngân sách, cần theo dõi tiến độ chi tiêu."
    BudgetHealthLevel.WARNING -> "Sắp chạm trần ngân sách tháng (80–100%), hãy cân nhắc cắt giảm."
    BudgetHealthLevel.EXCEEDED -> "Chi tiêu đã vượt quá 100% hạn mức ngân sách thiết lập!"
}

private fun SpendlyCategory.icon(): ImageVector = when (this) {
    SpendlyCategory.FOOD -> Icons.Rounded.Restaurant
    SpendlyCategory.TRANSPORT -> Icons.Rounded.DirectionsCar
    SpendlyCategory.HOUSING -> Icons.Rounded.Home
    SpendlyCategory.SHOPPING -> Icons.Rounded.ShoppingBag
    SpendlyCategory.ENTERTAINMENT -> Icons.Rounded.Celebration
    SpendlyCategory.HEALTH -> Icons.Rounded.MedicalServices
    SpendlyCategory.EDUCATION -> Icons.Rounded.School
    SpendlyCategory.UTILITIES -> Icons.Rounded.Bolt
    SpendlyCategory.OTHER -> Icons.Rounded.Category
}

@Composable
private fun budgetTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.spendlyColors.primaryLavender,
    unfocusedBorderColor = MaterialTheme.spendlyColors.divider,
    focusedLabelColor = MaterialTheme.spendlyColors.deepLavender,
    unfocusedLabelColor = MaterialTheme.spendlyColors.textMuted,
    focusedTextColor = MaterialTheme.spendlyColors.textPrimary,
    unfocusedTextColor = MaterialTheme.spendlyColors.textPrimary,
    cursorColor = MaterialTheme.spendlyColors.deepLavender
)

@Preview(name = "BudgetManagementScreen Preview", showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun BudgetManagementScreenPreview() {
    val overall = SpendlyBudget(
        budgetId = "b_all",
        groupId = "grp_1",
        name = "Ngân sách tổng tháng 11",
        limitAmount = 10_000_000L,
        category = null
    )
    val food = SpendlyBudget(
        budgetId = "b_food",
        groupId = "grp_1",
        name = "Ngân sách Ăn uống",
        limitAmount = 3_000_000L,
        category = SpendlyCategory.FOOD
    )
    val transport = SpendlyBudget(
        budgetId = "b_trans",
        groupId = "grp_1",
        name = "Ngân sách Di chuyển",
        limitAmount = 1_000_000L,
        category = SpendlyCategory.TRANSPORT
    )
    val sampleState = BudgetUiState(
        groupId = "grp_1",
        groupName = "Quỹ Chi Tiêu Nhóm 08",
        monthlyBudgetLimit = 10_000_000L,
        overallSummary = BudgetStatusSummary(
            budget = overall,
            spentAmount = 6_450_000L,
            remainingAmount = 3_550_000L,
            usagePercent = 64.5f,
            level = BudgetHealthLevel.WATCH
        ),
        budgetSummaries = listOf(
            BudgetStatusSummary(
                budget = overall,
                spentAmount = 6_450_000L,
                remainingAmount = 3_550_000L,
                usagePercent = 64.5f,
                level = BudgetHealthLevel.WATCH
            ),
            BudgetStatusSummary(
                budget = food,
                spentAmount = 2_700_000L,
                remainingAmount = 300_000L,
                usagePercent = 90f,
                level = BudgetHealthLevel.WARNING
            ),
            BudgetStatusSummary(
                budget = transport,
                spentAmount = 1_150_000L,
                remainingAmount = -150_000L,
                usagePercent = 115f,
                level = BudgetHealthLevel.EXCEEDED
            )
        ),
        isLoading = false
    )

    SpendlyTheme {
        BudgetManagementContent(uiState = sampleState)
    }
}
