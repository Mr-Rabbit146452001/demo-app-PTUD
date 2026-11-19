package com.example.nhoms8.feature.settlements

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nhoms8.algorithm.FintechAlgorithm
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.GradientCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.core.designsystem.SpendlyButton
import com.example.nhoms8.core.designsystem.SpendlyButtonStyle
import com.example.nhoms8.domain.model.GroupRole
import com.example.nhoms8.domain.model.SettlementStatus
import com.example.nhoms8.domain.model.SpendlySettlement
import com.example.nhoms8.feature.groups.generateQrCodeBitmap
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlyTextStyles
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class SettlementV2Tab(val label: String) {
    SUGGESTIONS("Gợi ý trả nợ"),
    PENDING("Chờ xác nhận"),
    HISTORY("Lịch sử thanh toán")
}

/**
 * Màn hình Đối soát & Thanh toán V2 hỗ trợ quy trình 2 bước:
 * Gợi ý trả nợ (kèm mã QR ZXing) -> Chờ xác nhận (Xác nhận / Từ chối) -> Lịch sử thanh toán.
 */
@Composable
fun SettlementV2Screen(
    groupId: String,
    currentUserId: String,
    currentUserName: String = "",
    viewModel: SettlementViewModel,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by remember(groupId, currentUserId, viewModel) {
        viewModel.observeGroupSettlement(groupId = groupId, currentUserId = currentUserId)
    }.collectAsStateWithLifecycle()

    val resolvedActorName = remember(currentUserId, currentUserName, uiState.members) {
        currentUserName.trim().ifBlank {
            uiState.members.firstOrNull { it.userId == currentUserId }?.displayName.orEmpty()
        }.ifBlank { currentUserId }
    }

    SettlementV2Content(
        uiState = uiState,
        onBackClicked = {
            viewModel.clearMessages()
            onBackClicked()
        },
        onRequestSettlement = { transfer, note, bankInfo, onDone ->
            viewModel.requestSettlement(
                groupId = groupId,
                fromUserId = transfer.fromUserId,
                fromUserName = transfer.fromUserName,
                toUserId = transfer.toUserId,
                toUserName = transfer.toUserName,
                amount = transfer.amount,
                note = note,
                bankInfo = bankInfo,
                createdBy = currentUserId.ifBlank { transfer.fromUserId },
                onSuccess = onDone
            )
        },
        onConfirmSettlement = { settlement ->
            viewModel.confirmSettlement(
                groupId = groupId,
                settlementId = settlement.settlementId,
                actorId = currentUserId.ifBlank { settlement.toUserId },
                actorName = resolvedActorName.ifBlank { settlement.toUserName }
            )
        },
        onRejectSettlement = { settlement, reason ->
            viewModel.rejectSettlement(
                groupId = groupId,
                settlementId = settlement.settlementId,
                actorId = currentUserId.ifBlank { settlement.toUserId },
                actorName = resolvedActorName.ifBlank { settlement.toUserName },
                reason = reason
            )
        },
        onDismissMessage = viewModel::clearMessages,
        modifier = modifier
    )
}

/**
 * Giao diện thuần trạng thái cho màn hình Đối soát & Thanh toán V2.
 */
@Composable
fun SettlementV2Content(
    uiState: SettlementUiState,
    onBackClicked: () -> Unit = {},
    onRequestSettlement: (
        transfer: SuggestedTransferItem,
        note: String,
        bankInfo: String,
        onSuccess: () -> Unit
    ) -> Unit = { _, _, _, _ -> },
    onConfirmSettlement: (SpendlySettlement) -> Unit = {},
    onRejectSettlement: (SpendlySettlement, String) -> Unit = { _, _ -> },
    onDismissMessage: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()

    var selectedTab by rememberSaveable { mutableStateOf(SettlementV2Tab.SUGGESTIONS) }
    var activePaymentTransfer by remember { mutableStateOf<SuggestedTransferItem?>(null) }
    var rejectingSettlement by remember { mutableStateOf<SpendlySettlement?>(null) }

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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item(key = "top_bar") {
                    SettlementTopBar(
                        title = "Đối soát & Thanh toán",
                        subtitle = uiState.groupName.ifBlank { "Quỹ chi tiêu Spendly" },
                        onBackClicked = onBackClicked,
                        hazeState = hazeState
                    )
                }

                item(key = "hero_card") {
                    SettlementHeroCard(uiState = uiState)
                }

                val feedbackMessage = uiState.errorMessage ?: uiState.successMessage
                if (!feedbackMessage.isNullOrBlank()) {
                    item(key = "feedback_banner") {
                        SettlementBanner(
                            message = feedbackMessage,
                            isError = uiState.errorMessage != null,
                            onDismiss = onDismissMessage
                        )
                    }
                }

                item(key = "tab_pills") {
                    SettlementTabPills(
                        selectedTab = selectedTab,
                        pendingCount = uiState.pendingSettlements.size,
                        onTabSelected = { selectedTab = it },
                        hazeState = hazeState
                    )
                }

                when (selectedTab) {
                    SettlementV2Tab.SUGGESTIONS -> {
                        if (uiState.suggestedTransfers.isEmpty()) {
                            item(key = "empty_suggestions") {
                                SettlementEmptyCard(
                                    icon = Icons.Rounded.CheckCircle,
                                    iconTint = colors.mintDeep,
                                    iconBg = colors.mintContainer,
                                    title = "Tất cả thành viên đã sòng phẳng",
                                    description = "Không có khoản công nợ nào cần bù trừ trong nhóm lúc này.",
                                    hazeState = hazeState
                                )
                            }
                        } else {
                            items(
                                items = uiState.suggestedTransfers,
                                key = { "${it.fromUserId}->${it.toUserId}" }
                            ) { transfer ->
                                val hasPendingMatch = uiState.pendingSettlements.any {
                                    it.fromUserId == transfer.fromUserId && it.toUserId == transfer.toUserId
                                }
                                SuggestedTransferCard(
                                    transfer = transfer,
                                    currentUserId = uiState.currentUserId,
                                    hasPendingRequest = hasPendingMatch,
                                    hazeState = hazeState,
                                    onPayClick = { activePaymentTransfer = transfer }
                                )
                            }
                        }
                    }

                    SettlementV2Tab.PENDING -> {
                        if (uiState.pendingSettlements.isEmpty()) {
                            item(key = "empty_pending") {
                                SettlementEmptyCard(
                                    icon = Icons.Rounded.HourglassTop,
                                    iconTint = colors.deepLavender,
                                    iconBg = colors.lavenderContainer,
                                    title = "Chưa có yêu cầu chờ xác nhận",
                                    description = "Các khoản chuyển khoản được đánh dấu \"Tôi đã chuyển\" sẽ xuất hiện tại đây.",
                                    hazeState = hazeState
                                )
                            }
                        } else {
                            items(
                                items = uiState.pendingSettlements,
                                key = { it.settlementId }
                            ) { settlement ->
                                PendingSettlementCard(
                                    settlement = settlement,
                                    currentUserId = uiState.currentUserId,
                                    canManageSettlements = uiState.canManageSettlements,
                                    isProcessing = uiState.isProcessing,
                                    hazeState = hazeState,
                                    onConfirmClick = { onConfirmSettlement(settlement) },
                                    onRejectClick = { rejectingSettlement = settlement }
                                )
                            }
                        }
                    }

                    SettlementV2Tab.HISTORY -> {
                        if (uiState.historySettlements.isEmpty()) {
                            item(key = "empty_history") {
                                SettlementEmptyCard(
                                    icon = Icons.Rounded.History,
                                    iconTint = colors.sky,
                                    iconBg = colors.lavenderContainer,
                                    title = "Chưa có lịch sử thanh toán",
                                    description = "Giao dịch sau khi được xác nhận hoặc từ chối sẽ được lưu tại đây.",
                                    hazeState = hazeState
                                )
                            }
                        } else {
                            items(
                                items = uiState.historySettlements,
                                key = { it.settlementId }
                            ) { settlement ->
                                HistorySettlementCard(
                                    settlement = settlement,
                                    hazeState = hazeState
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    val paymentTransfer = activePaymentTransfer
    if (paymentTransfer != null) {
        QrPaymentRequestDialog(
            transfer = paymentTransfer,
            groupName = uiState.groupName,
            isProcessing = uiState.isProcessing,
            onDismiss = {
                if (!uiState.isProcessing) activePaymentTransfer = null
            },
            onSubmitPayment = { note, bankInfo ->
                onRequestSettlement(paymentTransfer, note, bankInfo) {
                    activePaymentTransfer = null
                    selectedTab = SettlementV2Tab.PENDING
                }
            }
        )
    }

    val targetReject = rejectingSettlement
    if (targetReject != null) {
        RejectSettlementDialog(
            settlement = targetReject,
            isProcessing = uiState.isProcessing,
            onDismiss = {
                if (!uiState.isProcessing) rejectingSettlement = null
            },
            onConfirmReject = { reason ->
                rejectingSettlement = null
                onRejectSettlement(targetReject, reason)
            }
        )
    }
}

@Composable
private fun SettlementTopBar(
    title: String,
    subtitle: String,
    onBackClicked: () -> Unit,
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
    }
}

@Composable
private fun SettlementHeroCard(uiState: SettlementUiState) {
    val colors = MaterialTheme.spendlyColors
    val netPrefix = when {
        uiState.myBalance > 0L -> "+"
        uiState.myBalance < 0L -> "-"
        else -> ""
    }
    val absMyBalance = kotlin.math.abs(uiState.myBalance)
    val netCaption = when {
        uiState.myBalance > 0L -> "Bạn đang được nhận lại từ nhóm"
        uiState.myBalance < 0L -> "Bạn cần chuyển khoản để tất toán"
        else -> "Số dư cá nhân của bạn đang cân bằng"
    }

    GradientCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TỔNG SỐ DƯ RÒNG CỦA BẠN",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White.copy(alpha = 0.85f)
            )
            Box(
                modifier = Modifier
                    .clip(SpendlyShapeTokens.Pill)
                    .background(Color.White.copy(alpha = 0.20f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${uiState.suggestedTransfers.size} lượt bù trừ",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "$netPrefix${FintechAlgorithm.formatVnd(absMyBalance)}",
            style = SpendlyTextStyles.moneyHero,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = netCaption,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.88f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HeroStatTile(
                label = "Tôi đang nợ",
                value = FintechAlgorithm.formatVnd(uiState.iOweTotal),
                accentTint = if (uiState.iOweTotal > 0L) colors.coral else Color.White,
                modifier = Modifier.weight(1f)
            )
            HeroStatTile(
                label = "Ai đang nợ tôi",
                value = FintechAlgorithm.formatVnd(uiState.owedToMeTotal),
                accentTint = if (uiState.owedToMeTotal > 0L) colors.mint else Color.White,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HeroStatTile(
    label: String,
    value: String,
    accentTint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(SpendlyShapeTokens.CardSmall)
            .background(Color.White.copy(alpha = 0.16f))
            .border(1.dp, Color.White.copy(alpha = 0.24f), SpendlyShapeTokens.CardSmall)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.86f)
        )
        Text(
            text = value,
            style = SpendlyTextStyles.moneyMedium,
            color = accentTint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SettlementTabPills(
    selectedTab: SettlementV2Tab,
    pendingCount: Int,
    onTabSelected: (SettlementV2Tab) -> Unit,
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
            items(SettlementV2Tab.entries) { tab ->
                val selected = tab == selectedTab
                val label = if (tab == SettlementV2Tab.PENDING) {
                    "${tab.label} ($pendingCount)"
                } else {
                    tab.label
                }
                Box(
                    modifier = Modifier
                        .clip(SpendlyShapeTokens.Pill)
                        .background(if (selected) colors.deepLavender else Color.Transparent)
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = if (selected) Color.White else colors.textMuted,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestedTransferCard(
    transfer: SuggestedTransferItem,
    currentUserId: String,
    hasPendingRequest: Boolean,
    hazeState: HazeState,
    onPayClick: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val isFromMe = currentUserId.isNotBlank() && transfer.fromUserId == currentUserId
    val isToMe = currentUserId.isNotBlank() && transfer.toUserId == currentUserId

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MemberNodeColumn(
                name = if (isFromMe) "${transfer.fromUserName} (Bạn)" else transfer.fromUserName,
                initials = transfer.fromInitials,
                avatarBg = colors.coralContainer,
                avatarFg = colors.coralDeep,
                modifier = Modifier.weight(1f)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = FintechAlgorithm.formatVnd(transfer.amount),
                    style = SpendlyTextStyles.moneyMedium,
                    color = colors.deepLavender
                )
                Box(
                    modifier = Modifier
                        .clip(SpendlyShapeTokens.Pill)
                        .background(colors.lavenderContainer)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Chuyển đến",
                        tint = colors.deepLavender,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            MemberNodeColumn(
                name = if (isToMe) "${transfer.toUserName} (Bạn)" else transfer.toUserName,
                initials = transfer.toInitials,
                avatarBg = colors.mintContainer,
                avatarFg = colors.mintDeep,
                modifier = Modifier.weight(1f)
            )
        }

        if (hasPendingRequest) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SpendlyShapeTokens.CardSmall)
                    .background(colors.amberContainer)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.HourglassTop,
                    contentDescription = null,
                    tint = colors.amberDeep,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Đã có yêu cầu đang chờ ${transfer.toUserName} xác nhận",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = colors.amberDeep
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        SpendlyButton(
            text = "Thanh toán / QR",
            onClick = onPayClick,
            modifier = Modifier.fillMaxWidth(),
            height = 44.dp,
            leadingIcon = Icons.Rounded.QrCode2
        )
    }
}

@Composable
private fun PendingSettlementCard(
    settlement: SpendlySettlement,
    currentUserId: String,
    canManageSettlements: Boolean,
    isProcessing: Boolean,
    hazeState: HazeState,
    onConfirmClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val isSender = currentUserId.isNotBlank() && currentUserId == settlement.fromUserId
    val isRecipient = currentUserId.isNotBlank() && currentUserId == settlement.toUserId
    val canAct = isRecipient || (canManageSettlements && !isSender)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        contentPadding = PaddingValues(16.dp)
    ) {
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
                    text = "${settlement.fromUserName} → ${settlement.toUserName}",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary
                )
                Text(
                    text = formatTimestamp(settlement.createdAtMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted
                )
            }
            SettlementStatusPill(status = settlement.status)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = FintechAlgorithm.formatVnd(settlement.amount),
            style = SpendlyTextStyles.moneyLarge,
            color = colors.deepLavender
        )

        if (settlement.note.isNotBlank() || settlement.bankInfo.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SpendlyShapeTokens.CardSmall)
                    .background(colors.surfaceElevated.copy(alpha = 0.72f))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (settlement.note.isNotBlank()) {
                    Text(
                        text = "Nội dung: ${settlement.note}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textPrimary
                    )
                }
                if (settlement.bankInfo.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccountBalance,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = settlement.bankInfo,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (canAct) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SpendlyButton(
                    text = "Từ chối",
                    onClick = onRejectClick,
                    style = SpendlyButtonStyle.OUTLINED,
                    enabled = !isProcessing,
                    height = 44.dp,
                    leadingIcon = Icons.Rounded.Cancel,
                    modifier = Modifier.weight(0.9f)
                )
                SpendlyButton(
                    text = "Xác nhận đã nhận",
                    onClick = onConfirmClick,
                    style = SpendlyButtonStyle.PRIMARY,
                    enabled = !isProcessing,
                    isLoading = isProcessing,
                    height = 44.dp,
                    leadingIcon = Icons.Rounded.CheckCircle,
                    modifier = Modifier.weight(1.3f)
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SpendlyShapeTokens.Pill)
                    .background(colors.amberContainer)
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.HourglassTop,
                    contentDescription = null,
                    tint = colors.amberDeep,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSender) {
                        "Đang chờ ${settlement.toUserName} xác nhận"
                    } else {
                        "Chờ người nhận xác nhận thanh toán"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.amberDeep
                )
            }
        }
    }
}

@Composable
private fun HistorySettlementCard(
    settlement: SpendlySettlement,
    hazeState: HazeState
) {
    val colors = MaterialTheme.spendlyColors
    val displayTime = settlement.confirmedAtMillis ?: settlement.createdAtMillis

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "${settlement.fromUserName} → ${settlement.toUserName}",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary
                )
                Text(
                    text = formatTimestamp(displayTime),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted
                )
            }
            SettlementStatusPill(status = settlement.status)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = FintechAlgorithm.formatVnd(settlement.amount),
                style = SpendlyTextStyles.moneyMedium,
                color = if (settlement.status == SettlementStatus.CONFIRMED) {
                    colors.mintDeep
                } else {
                    colors.coralDeep
                }
            )
            if (settlement.bankInfo.isNotBlank()) {
                Text(
                    text = settlement.bankInfo,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (settlement.note.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = settlement.note,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted
            )
        }
    }
}

@Composable
private fun SettlementStatusPill(status: SettlementStatus) {
    val colors = MaterialTheme.spendlyColors
    val (bg, fg, icon) = when (status) {
        SettlementStatus.PENDING -> Triple(colors.amberContainer, colors.amberDeep, Icons.Rounded.HourglassTop)
        SettlementStatus.CONFIRMED -> Triple(colors.mintContainer, colors.mintDeep, Icons.Rounded.CheckCircle)
        SettlementStatus.REJECTED -> Triple(colors.coralContainer, colors.coralDeep, Icons.Rounded.Cancel)
    }

    Row(
        modifier = Modifier
            .clip(SpendlyShapeTokens.Pill)
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = fg
        )
    }
}

@Composable
private fun MemberNodeColumn(
    name: String,
    initials: String,
    avatarBg: Color,
    avatarFg: Color,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(avatarBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials.ifBlank { "TV" },
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = avatarFg
            )
        }
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SettlementEmptyCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    description: String,
    hazeState: HazeState
) {
    val colors = MaterialTheme.spendlyColors
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        contentPadding = PaddingValues(22.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SettlementBanner(
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
                contentDescription = "Đóng thông báo",
                tint = fg,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun QrPaymentRequestDialog(
    transfer: SuggestedTransferItem,
    groupName: String,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onSubmitPayment: (note: String, bankInfo: String) -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val context = LocalContext.current
    val resolvedGroup = groupName.ifBlank { "Spendly" }
    val amountLabel = FintechAlgorithm.formatVnd(transfer.amount)
    val defaultTransferNote = remember(resolvedGroup, amountLabel) {
        "Spendly - $resolvedGroup - $amountLabel"
    }

    var noteText by remember(transfer) { mutableStateOf(defaultTransferNote) }
    var bankInfoText by remember(transfer) { mutableStateOf("") }

    val qrPayload = remember(transfer, resolvedGroup, noteText, bankInfoText) {
        buildString {
            append("SPENDLY|PAY|group=").append(resolvedGroup)
            append("|from=").append(transfer.fromUserName)
            append("|to=").append(transfer.toUserName)
            append("|amount=").append(transfer.amount)
            if (noteText.isNotBlank()) append("|note=").append(noteText.trim())
            if (bankInfoText.isNotBlank()) append("|bank=").append(bankInfoText.trim())
        }
    }
    val qrBitmap = remember(qrPayload) {
        generateQrCodeBitmap(content = qrPayload, sizePx = 520)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceElevated,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.SwapHoriz,
                    contentDescription = null,
                    tint = colors.deepLavender
                )
                Text(
                    text = "Thanh toán công nợ",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${transfer.fromUserName} → ${transfer.toUserName}",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = amountLabel,
                    style = SpendlyTextStyles.moneyHero,
                    color = colors.deepLavender
                )
                Text(
                    text = FintechAlgorithm.convertNumberToWords(transfer.amount),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .size(196.dp)
                        .clip(SpendlyShapeTokens.CardMedium)
                        .background(Color.White)
                        .border(1.dp, colors.divider, SpendlyShapeTokens.CardMedium)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap,
                            contentDescription = "Mã QR thanh toán",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(
                            text = "Không tạo được mã QR",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted
                        )
                    }
                }

                // Nội dung chuyển khoản gợi ý + nút sao chép
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SpendlyShapeTokens.CardSmall)
                        .background(colors.lavenderContainer.copy(alpha = 0.55f))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nội dung chuyển khoản",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.onLavenderContainer
                        )
                        Row(
                            modifier = Modifier
                                .clip(SpendlyShapeTokens.Pill)
                                .background(colors.surface)
                                .clickable {
                                    copyTextToClipboard(context, "Nội dung chuyển khoản", noteText.ifBlank { defaultTransferNote })
                                    Toast.makeText(context, "Đã sao chép nội dung chuyển khoản", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = "Sao chép",
                                tint = colors.deepLavender,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Sao chép",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.deepLavender
                            )
                        }
                    }
                    Text(
                        text = defaultTransferNote,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary
                    )
                }

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Ghi chú chuyển khoản") },
                    singleLine = true,
                    shape = SpendlyShapeTokens.CardSmall,
                    colors = settlementTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bankInfoText,
                    onValueChange = { bankInfoText = it },
                    label = { Text("Ngân hàng / STK (tùy chọn)") },
                    placeholder = { Text("VD: VCB · 0123456789") },
                    singleLine = true,
                    shape = SpendlyShapeTokens.CardSmall,
                    colors = settlementTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(color = colors.divider.copy(alpha = 0.6f))

                SpendlyButton(
                    text = "Tôi đã chuyển (Gửi xác nhận)",
                    onClick = { onSubmitPayment(noteText, bankInfoText) },
                    enabled = !isProcessing,
                    isLoading = isProcessing,
                    leadingIcon = Icons.Rounded.CheckCircle,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(enabled = !isProcessing, onClick = onDismiss) {
                Text("Đóng", color = colors.textMuted)
            }
        }
    )
}

@Composable
private fun RejectSettlementDialog(
    settlement: SpendlySettlement,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onConfirmReject: (String) -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    var reasonText by remember(settlement.settlementId) { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceElevated,
        title = {
            Text(
                text = "Từ chối xác nhận thanh toán?",
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Từ chối khoản ${FintechAlgorithm.formatVnd(settlement.amount)} từ ${settlement.fromUserName}. Công nợ giữa hai thành viên sẽ giữ nguyên.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
                OutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    label = { Text("Lý do (tùy chọn)") },
                    placeholder = { Text("VD: Chưa nhận được tiền chuyển khoản") },
                    maxLines = 3,
                    shape = SpendlyShapeTokens.CardSmall,
                    colors = settlementTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isProcessing,
                onClick = { onConfirmReject(reasonText) }
            ) {
                Text(
                    text = "Xác nhận từ chối",
                    color = colors.coralDeep,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(enabled = !isProcessing, onClick = onDismiss) {
                Text("Hủy", color = colors.textMuted)
            }
        }
    )
}

@Composable
private fun settlementTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.spendlyColors.primaryLavender,
    unfocusedBorderColor = MaterialTheme.spendlyColors.divider,
    focusedLabelColor = MaterialTheme.spendlyColors.deepLavender,
    unfocusedLabelColor = MaterialTheme.spendlyColors.textMuted,
    focusedTextColor = MaterialTheme.spendlyColors.textPrimary,
    unfocusedTextColor = MaterialTheme.spendlyColors.textPrimary,
    cursorColor = MaterialTheme.spendlyColors.deepLavender
)

private fun copyTextToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
}

private fun formatTimestamp(millis: Long): String {
    if (millis <= 0L) return "Vừa xong"
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}

@Preview(name = "SettlementV2Screen Preview", showBackground = true, widthDp = 412, heightDp = 880)
@Composable
private fun SettlementV2ScreenPreview() {
    val sampleState = SettlementUiState(
        groupId = "grp_1",
        groupName = "Quỹ Chi Tiêu Nhóm 08",
        currentUserId = "u3",
        currentUserRole = GroupRole.MEMBER,
        myBalance = -232_500L,
        iOweTotal = 232_500L,
        owedToMeTotal = 0L,
        suggestedTransfers = listOf(
            SuggestedTransferItem(
                fromUserId = "u3",
                fromUserName = "Nguyễn Phan Tấn Đạt",
                fromInitials = "TĐ",
                toUserId = "u1",
                toUserName = "Lê Văn Hùng",
                toInitials = "VH",
                amount = 232_500L
            ),
            SuggestedTransferItem(
                fromUserId = "u4",
                fromUserName = "Lê Trung Bình",
                fromInitials = "TB",
                toUserId = "u1",
                toUserName = "Lê Văn Hùng",
                toInitials = "VH",
                amount = 192_500L
            )
        ),
        pendingSettlements = listOf(
            SpendlySettlement(
                settlementId = "s1",
                groupId = "grp_1",
                fromUserId = "u2",
                fromUserName = "Lý Việt Linh",
                toUserId = "u1",
                toUserName = "Lê Văn Hùng",
                amount = 47_500L,
                status = SettlementStatus.PENDING,
                note = "Spendly - Quỹ Chi Tiêu Nhóm 08 - 47.500 đ",
                bankInfo = "VCB · 0123456789",
                createdAtMillis = 1_700_000_000_000L
            )
        ),
        isLoading = false
    )
    SpendlyTheme {
        SettlementV2Content(uiState = sampleState)
    }
}
