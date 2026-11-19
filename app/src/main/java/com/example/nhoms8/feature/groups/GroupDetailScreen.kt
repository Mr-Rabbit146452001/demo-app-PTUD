package com.example.nhoms8.feature.groups

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nhoms8.algorithm.FintechAlgorithm
import com.example.nhoms8.core.designsystem.BudgetProgressRing
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.GradientCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.core.designsystem.SpendlyButton
import com.example.nhoms8.core.designsystem.SpendlyButtonStyle
import com.example.nhoms8.core.designsystem.budgetProgressColor
import com.example.nhoms8.domain.model.GroupActivity
import com.example.nhoms8.domain.model.GroupRole
import com.example.nhoms8.domain.model.MemberBalance
import com.example.nhoms8.domain.model.RecordStatus
import com.example.nhoms8.domain.model.SpendlyExpense
import com.example.nhoms8.domain.model.SpendlyGroup
import com.example.nhoms8.domain.model.SpendlyGroupMember
import com.example.nhoms8.domain.model.SpendlyIncome
import com.example.nhoms8.domain.model.SpendlyUser
import com.example.nhoms8.domain.usecase.DebtTransfer
import com.example.nhoms8.ui.theme.SpendlyAmber
import com.example.nhoms8.ui.theme.SpendlyCoralDeep
import com.example.nhoms8.ui.theme.SpendlyIndigo
import com.example.nhoms8.ui.theme.SpendlyLavender
import com.example.nhoms8.ui.theme.SpendlyMintDeep
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlySky
import com.example.nhoms8.ui.theme.SpendlyTextDark
import com.example.nhoms8.ui.theme.SpendlyTextStyles
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * 6 tab chức năng bên trong màn hình Chi tiết quỹ nhóm.
 */
enum class GroupDetailTab(val label: String) {
    OVERVIEW("Tổng quan"),
    MEMBERS("Thành viên"),
    TRANSACTIONS("Giao dịch"),
    SETTLEMENT("Đối soát"),
    STATISTICS("Thống kê"),
    SETTINGS("Cài đặt")
}

private enum class TransactionTypeFilter(val label: String) {
    ALL("Tất cả"),
    EXPENSES("Khoản chi"),
    INCOMES("Khoản thu")
}

private sealed class GroupLedgerRow(
    open val id: String,
    open val dateMillis: Long,
    open val createdAtMillis: Long
) {
    data class ExpenseRow(val expense: SpendlyExpense) : GroupLedgerRow(
        id = "exp_${expense.expenseId}",
        dateMillis = expense.expenseDateMillis,
        createdAtMillis = expense.createdAtMillis
    )

    data class IncomeRow(val income: SpendlyIncome) : GroupLedgerRow(
        id = "inc_${income.incomeId}",
        dateMillis = income.incomeDateMillis,
        createdAtMillis = income.createdAtMillis
    )
}

/**
 * Tạo ảnh QR Code sắc nét dưới dạng [ImageBitmap] bằng ZXing [QRCodeWriter] cho mã mời nhóm.
 */
fun generateQrCodeBitmap(
    content: String,
    sizePx: Int = 512,
    foregroundColor: Int = SpendlyTextDark.toArgb(),
    backgroundColor: Int = android.graphics.Color.WHITE
): ImageBitmap? {
    if (content.isBlank() || sizePx <= 0) return null
    return try {
        val hints = mapOf(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val matrix = QRCodeWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            sizePx,
            sizePx,
            hints
        )
        val width = matrix.width
        val height = matrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val rowOffset = y * width
            for (x in 0 until width) {
                pixels[rowOffset + x] = if (matrix.get(x, y)) foregroundColor else backgroundColor
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        bitmap.asImageBitmap()
    } catch (_: Throwable) {
        null
    }
}

/**
 * Màn hình Chi tiết quỹ nhóm với Hero Card, mã QR mời thành viên và 6 tab tương tác.
 */
@Composable
fun GroupDetailScreen(
    groupId: String,
    viewModel: GroupViewModel,
    onBackClick: () -> Unit,
    onAddExpenseClick: (String) -> Unit = {},
    onAddIncomeClick: (String) -> Unit = {},
    onAiChatClick: (String) -> Unit = {},
    onExpenseClicked: (String) -> Unit = {},
    onOpenFullSettlement: () -> Unit = {},
    onOpenFullReport: () -> Unit = {},
    onOpenBudgets: () -> Unit = {},
    onLeftGroup: () -> Unit = onBackClick,
    initialTab: GroupDetailTab = GroupDetailTab.OVERVIEW,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.observeGroupDetail(groupId).collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    GroupDetailContent(
        uiState = uiState,
        currentUser = currentUser,
        initialTab = initialTab,
        onBackClick = {
            viewModel.clearMessages()
            onBackClick()
        },
        onAddExpenseClick = { onAddExpenseClick(groupId) },
        onAddIncomeClick = { onAddIncomeClick(groupId) },
        onAiChatClick = { onAiChatClick(groupId) },
        onExpenseClicked = onExpenseClicked,
        onOpenFullSettlement = onOpenFullSettlement,
        onOpenFullReport = onOpenFullReport,
        onOpenBudgets = onOpenBudgets,
        onAddManualMember = { name, phone, role ->
            viewModel.addManualMember(groupId = groupId, name = name, phone = phone, role = role)
        },
        onUpdateMemberRole = { userId, role, canAddExpense ->
            viewModel.updateMemberRole(
                groupId = groupId,
                userId = userId,
                role = role,
                canAddExpense = canAddExpense
            )
        },
        onRemoveOrLeaveMember = { userId ->
            val isSelf = userId == currentUser.uid
            viewModel.removeOrLeaveGroup(groupId = groupId, userId = userId) {
                if (isSelf) onLeftGroup()
            }
        },
        onUpdateGroupSettings = { name, description, limit ->
            viewModel.updateGroupSettings(
                groupId = groupId,
                name = name,
                description = description,
                monthlyBudgetLimit = limit
            )
        },
        onDismissMessage = viewModel::clearMessages,
        modifier = modifier
    )
}

/**
 * Giao diện thuần trạng thái cho màn hình Chi tiết quỹ nhóm 6 tab.
 */
@Composable
fun GroupDetailContent(
    uiState: GroupDetailUiState,
    currentUser: SpendlyUser,
    initialTab: GroupDetailTab = GroupDetailTab.OVERVIEW,
    onBackClick: () -> Unit = {},
    onAddExpenseClick: () -> Unit = {},
    onAddIncomeClick: () -> Unit = {},
    onAiChatClick: () -> Unit = {},
    onExpenseClicked: (String) -> Unit = {},
    onOpenFullSettlement: () -> Unit = {},
    onOpenFullReport: () -> Unit = {},
    onOpenBudgets: () -> Unit = {},
    onAddManualMember: (name: String, phone: String, role: GroupRole) -> Unit = { _, _, _ -> },
    onUpdateMemberRole: (userId: String, role: GroupRole, canAddExpense: Boolean) -> Unit = { _, _, _ -> },
    onRemoveOrLeaveMember: (userId: String) -> Unit = {},
    onUpdateGroupSettings: (name: String, description: String, monthlyBudgetLimit: Long) -> Unit = { _, _, _ -> },
    onDismissMessage: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()

    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }
    var showQrDialog by rememberSaveable { mutableStateOf(false) }
    var showAddMemberDialog by rememberSaveable { mutableStateOf(false) }
    var editingMember by remember { mutableStateOf<SpendlyGroupMember?>(null) }
    var selectedIncomeDetail by remember { mutableStateOf<SpendlyIncome?>(null) }
    var showLeaveConfirmDialog by rememberSaveable { mutableStateOf(false) }

    val group = uiState.group
    val currentMember = uiState.members.firstOrNull { it.userId == currentUser.uid }
    val canManageGroup = currentMember?.role?.canManageGroup() == true ||
        (group != null && group.ownerId == currentUser.uid)

    OrganicBackground(
        modifier = modifier.fillMaxSize(),
        hazeState = hazeState
    ) {
        if (uiState.isLoading && group == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primaryLavender)
            }
        } else if (group == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailTopBar(title = "Chi tiết quỹ", onBackClick = onBackClick)
                GlassCard(modifier = Modifier.fillMaxWidth(), hazeState = hazeState) {
                    Text(
                        text = "Không tìm thấy thông tin quỹ nhóm",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Quỹ này có thể đã bị lưu trữ hoặc bạn không còn quyền truy cập.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    SpendlyButton(
                        text = "Quay lại danh sách",
                        onClick = onBackClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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
                    DetailTopBar(
                        title = group.name,
                        subtitle = "${uiState.members.size.coerceAtLeast(group.memberCount)} thành viên",
                        onBackClick = onBackClick,
                        onQrClick = { showQrDialog = true }
                    )
                }

                item(key = "hero_card") {
                    GroupHeroGradientCard(
                        group = group,
                        uiState = uiState,
                        onInviteCodeClick = { showQrDialog = true },
                        onAddExpenseClick = onAddExpenseClick,
                        onAddIncomeClick = onAddIncomeClick,
                        onAiChatClick = onAiChatClick
                    )
                }

                val bannerMsg = uiState.errorMessage ?: uiState.successMessage
                if (!bannerMsg.isNullOrBlank()) {
                    item(key = "status_banner") {
                        val isError = uiState.errorMessage != null
                        DetailBanner(
                            message = bannerMsg,
                            isError = isError,
                            onDismiss = onDismissMessage
                        )
                    }
                }

                item(key = "tab_bar") {
                    GroupDetailTabRow(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        hazeState = hazeState
                    )
                }

                when (selectedTab) {
                    GroupDetailTab.OVERVIEW -> {
                        item(key = "overview_tab") {
                            OverviewTabContent(
                                group = group,
                                uiState = uiState,
                                hazeState = hazeState,
                                onOpenBudgets = onOpenBudgets
                            )
                        }
                    }

                    GroupDetailTab.MEMBERS -> {
                        item(key = "members_tab") {
                            MembersTabContent(
                                group = group,
                                members = uiState.members,
                                currentUserId = currentUser.uid,
                                canManageGroup = canManageGroup,
                                hazeState = hazeState,
                                onAddMemberClick = { showAddMemberDialog = true },
                                onMemberClick = { editingMember = it }
                            )
                        }
                    }

                    GroupDetailTab.TRANSACTIONS -> {
                        item(key = "transactions_tab") {
                            TransactionsTabContent(
                                expenses = uiState.expenses,
                                incomes = uiState.incomes,
                                hazeState = hazeState,
                                onAddExpenseClick = onAddExpenseClick,
                                onAddIncomeClick = onAddIncomeClick,
                                onExpenseClicked = onExpenseClicked,
                                onIncomeClicked = { selectedIncomeDetail = it }
                            )
                        }
                    }

                    GroupDetailTab.SETTLEMENT -> {
                        item(key = "settlement_tab") {
                            SettlementTabContent(
                                balances = uiState.balances,
                                simplifiedTransfers = uiState.simplifiedTransfers,
                                memberNamesById = uiState.memberNamesById,
                                hazeState = hazeState,
                                onOpenFullSettlement = onOpenFullSettlement
                            )
                        }
                    }

                    GroupDetailTab.STATISTICS -> {
                        item(key = "statistics_tab") {
                            StatisticsTabContent(
                                totalExpense = uiState.totalExpense,
                                activeExpenseCount = uiState.expenses.count { it.status == RecordStatus.ACTIVE },
                                categoryBreakdown = uiState.categoryBreakdown,
                                hazeState = hazeState,
                                onOpenFullReport = onOpenFullReport
                            )
                        }
                    }

                    GroupDetailTab.SETTINGS -> {
                        item(key = "settings_tab") {
                            SettingsTabContent(
                                group = group,
                                isProcessing = uiState.isProcessing,
                                hazeState = hazeState,
                                onShowQrClick = { showQrDialog = true },
                                onSaveSettings = onUpdateGroupSettings,
                                onOpenBudgets = onOpenBudgets,
                                onLeaveGroupClick = { showLeaveConfirmDialog = true }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showQrDialog && group != null) {
        GroupInviteQrDialog(
            group = group,
            onDismiss = { showQrDialog = false }
        )
    }

    if (showAddMemberDialog && group != null) {
        AddManualMemberDialog(
            isProcessing = uiState.isProcessing,
            onDismiss = { showAddMemberDialog = false },
            onConfirm = { name, phone, role ->
                onAddManualMember(name, phone, role)
                showAddMemberDialog = false
            }
        )
    }

    val targetMember = editingMember
    if (targetMember != null && group != null) {
        MemberActionDialog(
            member = targetMember,
            group = group,
            membersCount = uiState.members.size,
            currentUserId = currentUser.uid,
            canManageGroup = canManageGroup,
            onDismiss = { editingMember = null },
            onSaveRole = { role, canAdd ->
                onUpdateMemberRole(targetMember.userId, role, canAdd)
                editingMember = null
            },
            onRemoveOrLeave = {
                onRemoveOrLeaveMember(targetMember.userId)
                editingMember = null
            }
        )
    }

    val incomeInfo = selectedIncomeDetail
    if (incomeInfo != null) {
        IncomeDetailDialog(
            income = incomeInfo,
            onDismiss = { selectedIncomeDetail = null }
        )
    }

    if (showLeaveConfirmDialog && group != null) {
        val ownerBlocked = currentUser.uid == group.ownerId && uiState.members.size > 1
        AlertDialog(
            onDismissRequest = { showLeaveConfirmDialog = false },
            title = {
                Text(
                    text = "Rời khỏi quỹ \"${group.name}\"?",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = if (ownerBlocked) {
                        "Bạn đang là Trưởng nhóm và vẫn còn thành viên khác trong quỹ. Hãy giữ quyền quản trị hoặc xóa thành viên khác trước khi rời nhóm."
                    } else {
                        "Lịch sử giao dịch và công nợ đã ghi nhận trước đây của bạn vẫn được giữ nguyên trong sổ cái nhóm."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !ownerBlocked,
                    onClick = {
                        showLeaveConfirmDialog = false
                        onRemoveOrLeaveMember(currentUser.uid)
                    }
                ) {
                    Text(
                        text = "Xác nhận rời nhóm",
                        color = if (ownerBlocked) colors.textMuted else colors.coralDeep,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirmDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }
}

@Composable
private fun DetailTopBar(
    title: String,
    subtitle: String? = null,
    onBackClick: () -> Unit,
    onQrClick: (() -> Unit)? = null
) {
    val colors = MaterialTheme.spendlyColors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.glassContainer.copy(alpha = 0.85f))
                .border(1.dp, colors.divider, CircleShape)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Quay lại",
                tint = colors.textPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted
                )
            }
        }

        if (onQrClick != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.lavenderContainer)
                    .clickable(onClick = onQrClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.QrCode2,
                    contentDescription = "Mã QR mời thành viên",
                    tint = colors.deepLavender,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun GroupHeroGradientCard(
    group: SpendlyGroup,
    uiState: GroupDetailUiState,
    onInviteCodeClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onAddIncomeClick: () -> Unit,
    onAiChatClick: () -> Unit
) {
    GradientCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (group.description.isNotBlank()) {
                    Text(
                        text = group.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                modifier = Modifier
                    .clip(SpendlyShapeTokens.Pill)
                    .background(Color.White.copy(alpha = 0.22f))
                    .border(1.dp, Color.White.copy(alpha = 0.35f), SpendlyShapeTokens.Pill)
                    .clickable(onClick = onInviteCodeClick)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.QrCode2,
                    contentDescription = "Mã QR",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = group.inviteCode.ifBlank { "MÃ MỜI" },
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Số dư quỹ hiện tại",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.82f)
        )
        Text(
            text = FintechAlgorithm.formatVnd(uiState.groupBalance),
            style = SpendlyTextStyles.moneyHero
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Tổng thu ${FintechAlgorithm.formatShortVnd(uiState.totalIncome)} · Tổng chi ${FintechAlgorithm.formatShortVnd(uiState.totalExpense)}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.88f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HeroQuickActionButton(
                label = "+ Khoản chi",
                icon = Icons.Rounded.ArrowUpward,
                onClick = onAddExpenseClick,
                modifier = Modifier.weight(1f)
            )
            HeroQuickActionButton(
                label = "+ Khoản thu",
                icon = Icons.Rounded.ArrowDownward,
                onClick = onAddIncomeClick,
                modifier = Modifier.weight(1f)
            )
            HeroQuickActionButton(
                label = "AI Chat",
                icon = Icons.Rounded.AutoAwesome,
                onClick = onAiChatClick,
                modifier = Modifier.weight(0.88f)
            )
        }
    }
}

@Composable
private fun HeroQuickActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(SpendlyShapeTokens.Pill)
            .background(Color.White.copy(alpha = 0.22f))
            .border(1.dp, Color.White.copy(alpha = 0.30f), SpendlyShapeTokens.Pill)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun GroupDetailTabRow(
    selectedTab: GroupDetailTab,
    onTabSelected: (GroupDetailTab) -> Unit,
    hazeState: HazeState
) {
    val colors = MaterialTheme.spendlyColors

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        shape = SpendlyShapeTokens.Pill,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(GroupDetailTab.entries) { tab ->
                val isSelected = tab == selectedTab
                Box(
                    modifier = Modifier
                        .clip(SpendlyShapeTokens.Pill)
                        .background(
                            if (isSelected) colors.deepLavender else Color.Transparent
                        )
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = if (isSelected) Color.White else colors.textMuted,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewTabContent(
    group: SpendlyGroup,
    uiState: GroupDetailUiState,
    hazeState: HazeState,
    onOpenBudgets: () -> Unit = {}
) {
    val colors = MaterialTheme.spendlyColors

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassCard(
                modifier = Modifier.weight(1f),
                hazeState = hazeState,
                contentPadding = PaddingValues(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(colors.mintContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowDownward,
                            contentDescription = null,
                            tint = colors.mintDeep,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Tổng thu / Quỹ",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textMuted
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = FintechAlgorithm.formatVnd(uiState.totalIncome),
                    style = SpendlyTextStyles.moneyMedium,
                    color = colors.mintDeep,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            GlassCard(
                modifier = Modifier.weight(1f),
                hazeState = hazeState,
                contentPadding = PaddingValues(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(colors.coralContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = null,
                            tint = colors.coralDeep,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Tổng chi tiêu",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textMuted
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = FintechAlgorithm.formatVnd(uiState.totalExpense),
                    style = SpendlyTextStyles.moneyMedium,
                    color = colors.coralDeep,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Hạn mức ngân sách & cảnh báo mốc 0 - 50 - 80 - 100%
        BudgetAlertCard(
            spentAmount = uiState.totalExpense,
            monthlyLimit = group.monthlyBudgetLimit,
            ratio = uiState.budgetRatio,
            hazeState = hazeState
        )

        SpendlyButton(
            text = "Quản lý Ngân sách 4 mức",
            onClick = onOpenBudgets,
            modifier = Modifier.fillMaxWidth(),
            style = SpendlyButtonStyle.OUTLINED,
            leadingIcon = Icons.Rounded.Savings
        )

        // Nhật ký hoạt động gần đây
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            hazeState = hazeState
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
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = null,
                        tint = colors.deepLavender,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Hoạt động gần đây",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                }
                Text(
                    text = "${uiState.activities.size} sự kiện",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (uiState.activities.isEmpty()) {
                Text(
                    text = "Chưa có hoạt động nào được ghi nhận trong quỹ này.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    uiState.activities.take(8).forEachIndexed { index, activity ->
                        if (index > 0) {
                            HorizontalDivider(color = colors.divider.copy(alpha = 0.6f))
                        }
                        ActivityTimelineRow(activity = activity)
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetAlertCard(
    spentAmount: Long,
    monthlyLimit: Long,
    ratio: Float,
    hazeState: HazeState
) {
    val colors = MaterialTheme.spendlyColors
    val percentInt = (ratio.coerceAtLeast(0f) * 100f).roundToInt()
    val progressBarColor = budgetProgressColor(ratio)
    val animatedFill by animateFloatAsState(
        targetValue = ratio.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500),
        label = "budget_linear_progress"
    )

    val alertMessage: String
    val alertContainer: Color
    val alertForeground: Color
    val alertIcon: ImageVector
    when {
        ratio >= 1.0f -> {
            alertMessage = "Vượt 100% hạn mức ($percentInt%)! Cần rà soát lại các khoản chi trong tháng."
            alertContainer = colors.coralContainer
            alertForeground = colors.coralDeep
            alertIcon = Icons.Rounded.WarningAmber
        }
        ratio > 0.80f -> {
            alertMessage = "Cảnh báo mốc 80% ($percentInt%) — Ngân sách tháng sắp chạm trần."
            alertContainer = colors.coralContainer
            alertForeground = colors.coralDeep
            alertIcon = Icons.Rounded.WarningAmber
        }
        ratio >= 0.50f -> {
            alertMessage = "Lưu ý mốc 50% ($percentInt%) — Đã chi quá nửa hạn mức tháng."
            alertContainer = colors.amberContainer
            alertForeground = colors.amberDeep
            alertIcon = Icons.Rounded.WarningAmber
        }
        else -> {
            alertMessage = "An toàn dưới 50% ($percentInt%) — Chi tiêu đang trong ngưỡng kiểm soát."
            alertContainer = colors.mintContainer
            alertForeground = colors.mintDeep
            alertIcon = Icons.Rounded.CheckCircle
        }
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Tiến độ ngân sách tháng",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
                Text(
                    text = "${FintechAlgorithm.formatVnd(spentAmount)} / ${FintechAlgorithm.formatVnd(monthlyLimit)}",
                    style = SpendlyTextStyles.moneySmall,
                    color = colors.textMuted
                )

                Spacer(modifier = Modifier.height(2.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(SpendlyShapeTokens.Pill)
                        .background(colors.divider.copy(alpha = 0.65f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedFill)
                            .height(10.dp)
                            .clip(SpendlyShapeTokens.Pill)
                            .background(progressBarColor)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0%", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                    Text("50%", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                    Text("80%", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                    Text("100%", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                }
            }

            BudgetProgressRing(
                progress = ratio,
                size = 64.dp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SpendlyShapeTokens.CardSmall)
                .background(alertContainer)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = alertIcon,
                contentDescription = null,
                tint = alertForeground,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = alertMessage,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = alertForeground,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActivityTimelineRow(activity: GroupActivity) {
    val colors = MaterialTheme.spendlyColors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(colors.lavenderContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = activityIconFor(activity.action),
                contentDescription = null,
                tint = colors.deepLavender,
                modifier = Modifier.size(17.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.description,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = colors.textPrimary
            )
            Text(
                text = "${activity.actorName} · ${formatTimestamp(activity.createdAtMillis)}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted
            )
        }
    }
}

@Composable
private fun MembersTabContent(
    group: SpendlyGroup,
    members: List<SpendlyGroupMember>,
    currentUserId: String,
    canManageGroup: Boolean,
    hazeState: HazeState,
    onAddMemberClick: () -> Unit,
    onMemberClick: (SpendlyGroupMember) -> Unit
) {
    val colors = MaterialTheme.spendlyColors

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Thành viên (${members.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
                Text(
                    text = if (canManageGroup) {
                        "Nhấn vào thành viên để đổi vai trò hoặc xóa"
                    } else {
                        "Nhấn vào thẻ của bạn nếu muốn rời nhóm"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted
                )
            }

            if (canManageGroup) {
                SpendlyButton(
                    text = "+ Thêm thành viên",
                    onClick = onAddMemberClick,
                    height = 40.dp,
                    leadingIcon = Icons.Rounded.PersonAdd
                )
            }
        }

        if (members.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth(), hazeState = hazeState) {
                Text(
                    text = "Chưa có thành viên hoạt động trong quỹ.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted
                )
            }
        } else {
            members.forEach { member ->
                val isSelf = member.userId == currentUserId
                val isOwner = member.userId == group.ownerId || member.role == GroupRole.OWNER
                val (badgeBg, badgeFg) = roleBadgeColors(member.role)

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SpendlyShapeTokens.CardLarge)
                        .clickable { onMemberClick(member) },
                    hazeState = hazeState,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(badgeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = member.initials.ifBlank { computeInitialsFallback(member.displayName) },
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = badgeFg
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = member.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (isSelf) {
                                    Text(
                                        text = "(Bạn)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.deepLavender
                                    )
                                }
                            }

                            val subLine = buildString {
                                if (member.phone.isNotBlank()) {
                                    append(member.phone)
                                    append(" · ")
                                }
                                append(if (member.canAddExpense && member.role != GroupRole.VIEWER) "Được ghi chi tiêu" else "Chỉ xem")
                            }
                            Text(
                                text = subLine,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(SpendlyShapeTokens.Pill)
                                .background(badgeBg)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isOwner) GroupRole.OWNER.label else member.role.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = badgeFg
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionsTabContent(
    expenses: List<SpendlyExpense>,
    incomes: List<SpendlyIncome>,
    hazeState: HazeState,
    onAddExpenseClick: () -> Unit,
    onAddIncomeClick: () -> Unit,
    onExpenseClicked: (String) -> Unit,
    onIncomeClicked: (SpendlyIncome) -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    var filter by rememberSaveable { mutableStateOf(TransactionTypeFilter.ALL) }

    val mergedRows = remember(expenses, incomes, filter) {
        val list = buildList {
            if (filter != TransactionTypeFilter.INCOMES) {
                expenses.forEach { add(GroupLedgerRow.ExpenseRow(it)) }
            }
            if (filter != TransactionTypeFilter.EXPENSES) {
                incomes.forEach { add(GroupLedgerRow.IncomeRow(it)) }
            }
        }
        list.sortedWith(
            compareByDescending<GroupLedgerRow> { it.dateMillis }
                .thenByDescending { it.createdAtMillis }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SpendlyButton(
                text = "+ Chi",
                onClick = onAddExpenseClick,
                modifier = Modifier.weight(1f),
                height = 44.dp,
                leadingIcon = Icons.Rounded.ArrowUpward
            )
            SpendlyButton(
                text = "+ Thu",
                onClick = onAddIncomeClick,
                modifier = Modifier.weight(1f),
                height = 44.dp,
                style = SpendlyButtonStyle.SECONDARY_GLASS,
                hazeState = hazeState,
                leadingIcon = Icons.Rounded.ArrowDownward
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TransactionTypeFilter.entries.forEach { option ->
                val selected = option == filter
                Box(
                    modifier = Modifier
                        .clip(SpendlyShapeTokens.Pill)
                        .background(if (selected) colors.deepLavender else colors.lavenderContainer)
                        .clickable { filter = option }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) Color.White else colors.onLavenderContainer
                    )
                }
            }
        }

        if (mergedRows.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth(), hazeState = hazeState) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Chưa có giao dịch nào",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Nhấn + Chi hoặc + Thu ở trên để bắt đầu ghi nhận sổ cái.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                }
            }
        } else {
            mergedRows.forEach { row ->
                when (row) {
                    is GroupLedgerRow.ExpenseRow -> {
                        val exp = row.expense
                        val isCancelled = exp.status == RecordStatus.CANCELLED
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SpendlyShapeTokens.CardLarge)
                                .clickable { onExpenseClicked(exp.expenseId) }
                                .alpha(if (isCancelled) 0.68f else 1f),
                            hazeState = hazeState,
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(SpendlyShapeTokens.CardSmall)
                                        .background(if (isCancelled) colors.divider else colors.coralContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isCancelled) Icons.Rounded.Cancel else Icons.Rounded.ArrowUpward,
                                        contentDescription = null,
                                        tint = if (isCancelled) colors.textMuted else colors.coralDeep,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = exp.title,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                textDecoration = if (isCancelled) TextDecoration.LineThrough else TextDecoration.None
                                            ),
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (isCancelled) {
                                            CancelledStatusPill()
                                        }
                                    }

                                    Text(
                                        text = "${exp.category.label} · ${exp.payerName.ifBlank { exp.payerId }} đã trả · ${formatTimestamp(exp.expenseDateMillis)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = "-${FintechAlgorithm.formatVnd(exp.amount)}",
                                    style = SpendlyTextStyles.moneySmall.copy(
                                        textDecoration = if (isCancelled) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    color = if (isCancelled) colors.textMuted else colors.coralDeep
                                )
                            }
                        }
                    }

                    is GroupLedgerRow.IncomeRow -> {
                        val inc = row.income
                        val isCancelled = inc.status == RecordStatus.CANCELLED
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SpendlyShapeTokens.CardLarge)
                                .clickable { onIncomeClicked(inc) }
                                .alpha(if (isCancelled) 0.68f else 1f),
                            hazeState = hazeState,
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(SpendlyShapeTokens.CardSmall)
                                        .background(if (isCancelled) colors.divider else colors.mintContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isCancelled) Icons.Rounded.Cancel else Icons.Rounded.Savings,
                                        contentDescription = null,
                                        tint = if (isCancelled) colors.textMuted else colors.mintDeep,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = inc.title,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                textDecoration = if (isCancelled) TextDecoration.LineThrough else TextDecoration.None
                                            ),
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (isCancelled) {
                                            CancelledStatusPill()
                                        }
                                    }

                                    val kindLabel = if (inc.isFundContribution) "Đóng quỹ" else "Khoản thu"
                                    Text(
                                        text = "$kindLabel · ${inc.payerName.ifBlank { inc.payerId }} · ${formatTimestamp(inc.incomeDateMillis)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = "+${FintechAlgorithm.formatVnd(inc.amount)}",
                                    style = SpendlyTextStyles.moneySmall.copy(
                                        textDecoration = if (isCancelled) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    color = if (isCancelled) colors.textMuted else colors.mintDeep
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CancelledStatusPill() {
    val colors = MaterialTheme.spendlyColors
    Box(
        modifier = Modifier
            .clip(SpendlyShapeTokens.Pill)
            .background(colors.coralContainer)
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = "Đã hủy",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = colors.coralDeep
        )
    }
}

@Composable
private fun SettlementTabContent(
    balances: List<MemberBalance>,
    simplifiedTransfers: List<DebtTransfer>,
    memberNamesById: Map<String, String>,
    hazeState: HazeState,
    onOpenFullSettlement: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SpendlyButton(
            text = "Mở màn hình Quyết toán đầy đủ",
            onClick = onOpenFullSettlement,
            modifier = Modifier.fillMaxWidth(),
            style = SpendlyButtonStyle.OUTLINED,
            leadingIcon = Icons.Rounded.SwapHoriz
        )

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            hazeState = hazeState
        ) {
            Text(
                text = "Số dư ròng từng thành viên",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Số dư ròng = Tổng đã trả hộ − Tổng phần phải chịu",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (balances.isEmpty()) {
                Text(
                    text = "Chưa có phát sinh công nợ trong nhóm.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    balances.sortedByDescending { it.netBalance }.forEachIndexed { idx, bal ->
                        if (idx > 0) {
                            HorizontalDivider(color = colors.divider.copy(alpha = 0.6f))
                        }
                        val displayName = memberNamesById[bal.userId] ?: bal.userId
                        val netColor = when {
                            bal.netBalance > 0L -> colors.mintDeep
                            bal.netBalance < 0L -> colors.coralDeep
                            else -> colors.textMuted
                        }
                        val netPrefix = if (bal.netBalance > 0L) "+" else ""

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Trả hộ: ${FintechAlgorithm.formatShortVnd(bal.totalPaid)} · Phần chia: ${FintechAlgorithm.formatShortVnd(bal.totalShare)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textMuted
                                )
                            }
                            Text(
                                text = "$netPrefix${FintechAlgorithm.formatVnd(bal.netBalance)}",
                                style = SpendlyTextStyles.moneyMedium,
                                color = netColor
                            )
                        }
                    }
                }
            }
        }

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            hazeState = hazeState
        ) {
            Text(
                text = "Đề xuất bù trừ tối ưu (${simplifiedTransfers.size} lượt)",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (simplifiedTransfers.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SpendlyShapeTokens.CardSmall)
                        .background(colors.mintContainer)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = colors.mintDeep,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Tất cả thành viên đã cân bằng công nợ 0 đ.",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = colors.mintDeep
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    simplifiedTransfers.forEachIndexed { idx, transfer ->
                        if (idx > 0) {
                            HorizontalDivider(color = colors.divider.copy(alpha = 0.6f))
                        }
                        val fromName = memberNamesById[transfer.fromUserId] ?: transfer.fromUserId
                        val toName = memberNamesById[transfer.toUserId] ?: transfer.toUserId

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = fromName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.coralDeep,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = "trả cho",
                                    tint = colors.textMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = toName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.mintDeep,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = FintechAlgorithm.formatVnd(transfer.amount),
                                style = SpendlyTextStyles.moneySmall,
                                color = colors.deepLavender
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticsTabContent(
    totalExpense: Long,
    activeExpenseCount: Int,
    categoryBreakdown: List<CategoryBreakdownItem>,
    hazeState: HazeState,
    onOpenFullReport: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val barPalette = listOf(
        SpendlyLavender,
        SpendlySky,
        SpendlyMintDeep,
        SpendlyAmber,
        SpendlyCoralDeep,
        SpendlyIndigo
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SpendlyButton(
            text = "Mở Báo cáo & Biểu đồ đầy đủ",
            onClick = onOpenFullReport,
            modifier = Modifier.fillMaxWidth(),
            style = SpendlyButtonStyle.OUTLINED,
            leadingIcon = Icons.Rounded.BarChart
        )

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            hazeState = hazeState
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Phân bổ theo danh mục",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "$activeExpenseCount khoản chi hiệu lực · Tổng ${FintechAlgorithm.formatVnd(totalExpense)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (categoryBreakdown.isEmpty()) {
                Text(
                    text = "Chưa có dữ liệu chi tiêu hiệu lực để thống kê danh mục.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    categoryBreakdown.forEachIndexed { index, item ->
                        val accentColor = barPalette[index % barPalette.size]
                        val pct = (item.ratio * 100f).roundToInt()

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${item.category.label} (${item.transactionCount})",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "${FintechAlgorithm.formatVnd(item.totalAmount)} · $pct%",
                                    style = SpendlyTextStyles.moneySmall,
                                    color = colors.textPrimary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(9.dp)
                                    .clip(SpendlyShapeTokens.Pill)
                                    .background(colors.divider.copy(alpha = 0.65f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(item.ratio.coerceIn(0.02f, 1f))
                                        .height(9.dp)
                                        .clip(SpendlyShapeTokens.Pill)
                                        .background(accentColor)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsTabContent(
    group: SpendlyGroup,
    isProcessing: Boolean,
    hazeState: HazeState,
    onShowQrClick: () -> Unit,
    onSaveSettings: (name: String, description: String, monthlyBudgetLimit: Long) -> Unit,
    onOpenBudgets: () -> Unit = {},
    onLeaveGroupClick: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors

    var name by remember(group.groupId, group.name) { mutableStateOf(group.name) }
    var description by remember(group.groupId, group.description) { mutableStateOf(group.description) }
    var limitDigits by remember(group.groupId, group.monthlyBudgetLimit) {
        mutableStateOf(group.monthlyBudgetLimit.toString())
    }
    var localError by remember { mutableStateOf<String?>(null) }

    val parsedLimit = limitDigits.toLongOrNull() ?: 0L
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.primaryLavender,
        unfocusedBorderColor = colors.divider,
        focusedLabelColor = colors.deepLavender,
        unfocusedLabelColor = colors.textMuted,
        cursorColor = colors.deepLavender,
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            hazeState = hazeState
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = null,
                    tint = colors.deepLavender,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Thông tin & Hạn mức quỹ",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    localError = null
                },
                label = { Text("Tên quỹ nhóm *") },
                singleLine = true,
                shape = SpendlyShapeTokens.CardSmall,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả quỹ") },
                minLines = 2,
                maxLines = 3,
                shape = SpendlyShapeTokens.CardSmall,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Hạn mức ngân sách hàng tháng",
                style = MaterialTheme.typography.labelLarge,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GROUP_BUDGET_PRESETS.forEach { preset ->
                    val selected = parsedLimit == preset.amount
                    Box(
                        modifier = Modifier
                            .clip(SpendlyShapeTokens.Pill)
                            .background(if (selected) colors.deepLavender else colors.lavenderContainer)
                            .clickable {
                                limitDigits = preset.amount.toString()
                                localError = null
                            }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = preset.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) Color.White else colors.onLavenderContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = limitDigits,
                onValueChange = { raw ->
                    limitDigits = raw.filter { it.isDigit() }.take(12)
                    localError = null
                },
                label = { Text("Số tiền hạn mức (VNĐ)") },
                supportingText = {
                    Text(
                        text = FintechAlgorithm.formatVnd(parsedLimit),
                        style = SpendlyTextStyles.moneyCaption,
                        color = colors.deepLavender
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = SpendlyShapeTokens.CardSmall,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            if (!localError.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = localError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.coralDeep
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SpendlyButton(
                text = "Lưu cài đặt quỹ",
                onClick = {
                    val trimmedName = name.trim()
                    when {
                        trimmedName.isEmpty() -> localError = "Tên quỹ nhóm không được để trống"
                        parsedLimit <= 0L -> localError = "Hạn mức ngân sách phải lớn hơn 0 đ"
                        else -> {
                            localError = null
                            onSaveSettings(trimmedName, description.trim(), parsedLimit)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isLoading = isProcessing,
                enabled = !isProcessing,
                leadingIcon = Icons.Rounded.Edit
            )

            Spacer(modifier = Modifier.height(10.dp))

            SpendlyButton(
                text = "Quản lý Ngân sách 4 mức",
                onClick = onOpenBudgets,
                modifier = Modifier.fillMaxWidth(),
                style = SpendlyButtonStyle.OUTLINED,
                leadingIcon = Icons.Rounded.Savings
            )
        }

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            hazeState = hazeState
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mã mời tham gia quỹ",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Mã: ${group.inviteCode}",
                        style = SpendlyTextStyles.moneyMedium,
                        color = colors.deepLavender
                    )
                }
                SpendlyButton(
                    text = "Mã QR",
                    onClick = onShowQrClick,
                    height = 42.dp,
                    style = SpendlyButtonStyle.OUTLINED,
                    leadingIcon = Icons.Rounded.QrCode2
                )
            }
        }

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            hazeState = hazeState
        ) {
            Text(
                text = "Rời khỏi quỹ nhóm",
                style = MaterialTheme.typography.titleSmall,
                color = colors.coralDeep
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Rời khỏi nhóm khi bạn không còn tham gia chi tiêu chung.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(10.dp))
            SpendlyButton(
                text = "Rời khỏi nhóm",
                onClick = onLeaveGroupClick,
                modifier = Modifier.fillMaxWidth(),
                style = SpendlyButtonStyle.OUTLINED,
                leadingIcon = Icons.AutoMirrored.Rounded.ExitToApp
            )
        }
    }
}

@Composable
private fun GroupInviteQrDialog(
    group: SpendlyGroup,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    val qrBitmap = remember(group.inviteCode) {
        generateQrCodeBitmap(
            content = group.inviteCode,
            sizePx = 512
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Mời tham gia ${group.name}",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Quét mã QR hoặc nhập mã 6 ký tự bên dưới",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(216.dp)
                        .clip(SpendlyShapeTokens.CardMedium)
                        .background(Color.White)
                        .border(1.dp, colors.divider, SpendlyShapeTokens.CardMedium)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap,
                            contentDescription = "Mã QR mời vào quỹ ${group.name}",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = "Không thể tạo mã QR",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.coralDeep
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(SpendlyShapeTokens.Pill)
                        .background(colors.lavenderContainer)
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = group.inviteCode,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp
                        ),
                        color = colors.deepLavender
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SpendlyButton(
                        text = if (copied) "Đã chép!" else "Sao chép mã",
                        onClick = {
                            clipboardManager.setText(AnnotatedString(group.inviteCode))
                            copied = true
                        },
                        modifier = Modifier.weight(1f),
                        height = 44.dp,
                        style = SpendlyButtonStyle.OUTLINED,
                        leadingIcon = Icons.Rounded.ContentCopy
                    )
                    SpendlyButton(
                        text = "Chia sẻ",
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Tham gia quỹ \"${group.name}\" trên Spendly bằng mã mời: ${group.inviteCode}"
                                )
                            }
                            try {
                                context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ mã mời"))
                            } catch (_: Throwable) {
                                clipboardManager.setText(AnnotatedString(group.inviteCode))
                                copied = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        height = 44.dp,
                        leadingIcon = Icons.Rounded.Share
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddManualMemberDialog(
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, role: GroupRole) -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(GroupRole.MEMBER) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val selectableRoles = listOf(GroupRole.ADMIN, GroupRole.MEMBER, GroupRole.VIEWER)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Thêm thành viên mới",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorText = null
                    },
                    label = { Text("Họ và tên *") },
                    placeholder = { Text("VD: Lý Việt Linh") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại") },
                    placeholder = { Text("VD: 0908 224 510") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Vai trò trong nhóm",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textPrimary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectableRoles.forEach { role ->
                        val isSelected = role == selectedRole
                        Box(
                            modifier = Modifier
                                .clip(SpendlyShapeTokens.Pill)
                                .background(if (isSelected) colors.deepLavender else colors.lavenderContainer)
                                .clickable { selectedRole = role }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = role.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) Color.White else colors.onLavenderContainer
                            )
                        }
                    }
                }
                if (!errorText.isNullOrBlank()) {
                    Text(
                        text = errorText!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.coralDeep
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isProcessing,
                onClick = {
                    val trimmed = name.trim()
                    if (trimmed.isEmpty()) {
                        errorText = "Vui lòng nhập tên thành viên"
                    } else {
                        onConfirm(trimmed, phone.trim(), selectedRole)
                    }
                }
            ) {
                Text("Thêm ngay", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MemberActionDialog(
    member: SpendlyGroupMember,
    group: SpendlyGroup,
    membersCount: Int,
    currentUserId: String,
    canManageGroup: Boolean,
    onDismiss: () -> Unit,
    onSaveRole: (role: GroupRole, canAddExpense: Boolean) -> Unit,
    onRemoveOrLeave: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val isTargetOwner = member.userId == group.ownerId || member.role == GroupRole.OWNER
    val isSelf = member.userId == currentUserId
    val canEditRole = canManageGroup && !isTargetOwner
    val canRemoveOrLeave = (canManageGroup && !isTargetOwner) || (isSelf && (!isTargetOwner || membersCount <= 1))

    var selectedRole by remember(member.userId) {
        mutableStateOf(if (member.role == GroupRole.OWNER) GroupRole.ADMIN else member.role)
    }
    var canAddExpense by remember(member.userId) { mutableStateOf(member.canAddExpense) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = member.displayName,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Vai trò hiện tại: ${member.role.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )

                if (isTargetOwner) {
                    Text(
                        text = "Trưởng nhóm nắm toàn quyền quản trị quỹ và không thể bị hạ quyền.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.deepLavender
                    )
                }

                if (canEditRole) {
                    Text(
                        text = "Chọn vai trò mới",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.textPrimary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(GroupRole.ADMIN, GroupRole.MEMBER, GroupRole.VIEWER).forEach { role ->
                            val isSelected = role == selectedRole
                            Box(
                                modifier = Modifier
                                    .clip(SpendlyShapeTokens.Pill)
                                    .background(if (isSelected) colors.deepLavender else colors.lavenderContainer)
                                    .clickable {
                                        selectedRole = role
                                        if (role == GroupRole.VIEWER) {
                                            canAddExpense = false
                                        } else if (!canAddExpense) {
                                            canAddExpense = true
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = role.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) Color.White else colors.onLavenderContainer
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cho phép ghi khoản chi/thu",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = selectedRole != GroupRole.VIEWER && canAddExpense,
                            enabled = selectedRole != GroupRole.VIEWER,
                            onCheckedChange = { canAddExpense = it }
                        )
                    }
                }

                if (canRemoveOrLeave) {
                    HorizontalDivider(color = colors.divider)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SpendlyShapeTokens.CardSmall)
                            .background(colors.coralContainer)
                            .clickable(onClick = onRemoveOrLeave)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isSelf) Icons.AutoMirrored.Rounded.ExitToApp else Icons.Rounded.DeleteOutline,
                            contentDescription = null,
                            tint = colors.coralDeep,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isSelf) "Rời khỏi nhóm" else "Xóa thành viên khỏi nhóm",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.coralDeep
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (canEditRole) {
                TextButton(onClick = { onSaveRole(selectedRole, canAddExpense) }) {
                    Text("Lưu quyền", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
private fun IncomeDetailDialog(
    income: SpendlyIncome,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = income.title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "+${FintechAlgorithm.formatVnd(income.amount)}",
                    style = SpendlyTextStyles.moneyLarge,
                    color = if (income.status == RecordStatus.CANCELLED) colors.textMuted else colors.mintDeep
                )
                Text(
                    text = "Người đóng: ${income.payerName.ifBlank { income.payerId }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textPrimary
                )
                Text(
                    text = "Phân loại: ${if (income.isFundContribution) "Đóng quỹ nhóm" else "Khoản thu"} · ${income.category.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
                Text(
                    text = "Ngày ghi nhận: ${formatTimestamp(income.incomeDateMillis)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
                if (income.description.isNotBlank()) {
                    Text(
                        text = "Ghi chú: ${income.description}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textPrimary
                    )
                }
                if (income.status == RecordStatus.CANCELLED) {
                    CancelledStatusPill()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
private fun DetailBanner(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val bg = if (isError) colors.coralContainer else colors.mintContainer
    val fg = if (isError) colors.coralDeep else colors.mintDeep

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SpendlyShapeTokens.CardMedium)
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = fg,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDismiss, modifier = Modifier.size(22.dp)) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Đóng",
                tint = fg,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun roleBadgeColors(role: GroupRole): Pair<Color, Color> {
    val colors = MaterialTheme.spendlyColors
    return when (role) {
        GroupRole.OWNER -> colors.lavenderContainer to colors.deepLavender
        GroupRole.ADMIN -> Color(0xFFD6ECF9) to Color(0xFF1B577B)
        GroupRole.MEMBER -> colors.mintContainer to colors.mintDeep
        GroupRole.VIEWER -> colors.divider.copy(alpha = 0.5f) to colors.textMuted
    }
}

private fun activityIconFor(action: String): ImageVector = when (action) {
    "CREATE_EXPENSE", "UPDATE_EXPENSE" -> Icons.Rounded.ArrowUpward
    "CANCEL_EXPENSE", "CANCEL_INCOME" -> Icons.Rounded.Cancel
    "CREATE_INCOME" -> Icons.Rounded.Savings
    "JOIN_GROUP", "ADD_MEMBER" -> Icons.Rounded.PersonAdd
    "UPDATE_GROUP_SETTINGS" -> Icons.Rounded.Settings
    else -> Icons.Rounded.Group
}

private fun computeInitialsFallback(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return parts.mapNotNull { it.firstOrNull()?.toString() }
        .takeLast(2)
        .joinToString("")
        .uppercase()
        .ifBlank { "TV" }
}

private fun formatTimestamp(millis: Long): String {
    if (millis <= 0L) return "Vừa xong"
    val sdf = SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale.forLanguageTag("vi-VN"))
    return sdf.format(Date(millis))
}

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun GroupDetailScreenPreview() {
    SpendlyTheme {
        val sampleGroup = SpendlyGroup(
            groupId = "grp_1",
            name = "Quỹ Chi Tiêu Nhóm 08",
            description = "Chi tiêu đồ án và sinh hoạt chung",
            ownerId = "user_hung",
            inviteCode = "N08PRO",
            memberCount = 4,
            monthlyBudgetLimit = 10_000_000L,
            memberIds = listOf("user_hung", "u2", "u3", "u4")
        )
        val members = listOf(
            SpendlyGroupMember("user_hung", "Lê Văn Hùng", "VH", "0912345678", GroupRole.OWNER),
            SpendlyGroupMember("u2", "Lý Việt Linh", "VL", "0908224510", GroupRole.ADMIN),
            SpendlyGroupMember("u3", "Nguyễn Phan Tấn Đạt", "TĐ", "0986420115", GroupRole.MEMBER),
            SpendlyGroupMember("u4", "Lê Trung Bình", "TB", "0934881902", GroupRole.VIEWER, canAddExpense = false)
        )
        val state = GroupDetailUiState(
            groupId = "grp_1",
            group = sampleGroup,
            members = members,
            totalIncome = 10_000_000L,
            totalExpense = 6_250_000L,
            groupBalance = 3_750_000L,
            budgetRatio = 0.625f,
            activities = listOf(
                GroupActivity(
                    activityId = "a1",
                    groupId = "grp_1",
                    actorId = "user_hung",
                    actorName = "Lê Văn Hùng",
                    action = "CREATE_EXPENSE",
                    description = "Lê Văn Hùng đã thêm khoản chi Mua linh kiện đồ án",
                    createdAtMillis = 1_730_000_000_000L
                )
            ),
            isLoading = false
        )
        GroupDetailContent(
            uiState = state,
            currentUser = GroupViewModel.DEFAULT_OFFLINE_USER
        )
    }
}
