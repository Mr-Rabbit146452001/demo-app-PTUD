package com.example.nhoms8.feature.transactions

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.nhoms8.domain.model.GroupRole
import com.example.nhoms8.domain.model.SpendlyCategory
import com.example.nhoms8.domain.model.SpendlyGroupMember
import com.example.nhoms8.domain.usecase.ComputedShare
import com.example.nhoms8.domain.usecase.EngineSplitMode
import com.example.nhoms8.domain.usecase.ParticipantInput
import com.example.nhoms8.domain.usecase.SplitCalculationEngine
import com.example.nhoms8.domain.usecase.SplitValidationResult
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlyTextStyles
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.rememberHazeState
import kotlin.math.roundToLong

/**
 * Màn hình Chia tiền V2 hỗ trợ 3 chế độ (Chia đều, Theo %, Tùy chỉnh),
 * chuyển đổi người trả có tham gia chia tiền hay không và kiểm tra hợp lệ thời gian thực.
 */
@Composable
fun SplitBillV2Screen(
    viewModel: TransactionViewModel,
    onBackClicked: () -> Unit,
    onSavedSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val draft = viewModel.draftState.collectAsStateWithLifecycle().value

    SplitBillV2Content(
        draft = draft,
        onBackClicked = onBackClicked,
        onSelectSplitMode = viewModel::setSplitMode,
        onPayerParticipatesChanged = viewModel::setPayerParticipates,
        onToggleMemberIncluded = viewModel::toggleMemberIncluded,
        onPercentageChanged = viewModel::updateMemberPercentage,
        onCustomAmountChanged = viewModel::updateMemberCustomAmount,
        onDistributeEqualPercentages = viewModel::distributePercentagesEqually,
        onPrefillCustomEqual = viewModel::prefillCustomFromEqualSplit,
        onConfirmSave = { viewModel.commitExpense(onSuccess = onSavedSuccess) },
        modifier = modifier
    )
}

@Composable
fun SplitBillV2Content(
    draft: ExpenseDraftUiState,
    onBackClicked: () -> Unit,
    onSelectSplitMode: (EngineSplitMode) -> Unit,
    onPayerParticipatesChanged: (Boolean) -> Unit,
    onToggleMemberIncluded: (String) -> Unit,
    onPercentageChanged: (String, String) -> Unit,
    onCustomAmountChanged: (String, String) -> Unit,
    onDistributeEqualPercentages: () -> Unit,
    onPrefillCustomEqual: () -> Unit,
    onConfirmSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()

    val validSharesByUserId: Map<String, ComputedShare> = when (val result = draft.validationResult) {
        is SplitValidationResult.Valid -> result.shares.associateBy { it.userId }
        is SplitValidationResult.Invalid -> emptyMap()
    }
    val includedParticipants = draft.participantInputs.filter { it.isIncluded }
    val resolvedPayerLabel = draft.payerName.ifBlank { "Người trả" }
    val isValidationOk = draft.validationResult is SplitValidationResult.Valid

    Box(modifier = modifier.fillMaxSize()) {
        OrganicBackground(
            modifier = Modifier.fillMaxSize(),
            hazeState = hazeState
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            TransactionTopBar(
                title = if (draft.isEditMode) "Chỉnh sửa chia tiền" else "Chia tiền khoản chi",
                subtitle = draft.groupName.ifBlank { "Spendly Split V2" },
                onBackClicked = onBackClicked,
                hazeState = hazeState
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Thẻ tóm tắt khoản chi
                GradientCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = draft.title.ifBlank { "Khoản chi mới" },
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(SpendlyShapeTokens.Pill)
                                .background(Color.White.copy(alpha = 0.20f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = draft.category.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = FintechAlgorithm.formatVnd(draft.amount),
                        style = SpendlyTextStyles.moneyHero,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SummaryPillChip(
                            icon = Icons.Rounded.PersonOutline,
                            text = "Người trả: $resolvedPayerLabel"
                        )
                        SummaryPillChip(
                            icon = Icons.Rounded.Group,
                            text = "${includedParticipants.size}/${draft.participantInputs.size} người chia"
                        )
                    }
                }

                // 2. Segmented Mode Selector (Chia đều / Theo % / Tùy chỉnh)
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    hazeState = hazeState,
                    shape = SpendlyShapeTokens.Pill,
                    contentPadding = PaddingValues(5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SplitModeSegmentItem(
                            label = "Chia đều",
                            selected = draft.splitMode == EngineSplitMode.EQUAL,
                            onClick = { onSelectSplitMode(EngineSplitMode.EQUAL) },
                            modifier = Modifier.weight(1f)
                        )
                        SplitModeSegmentItem(
                            label = "Theo %",
                            selected = draft.splitMode == EngineSplitMode.PERCENTAGE,
                            onClick = { onSelectSplitMode(EngineSplitMode.PERCENTAGE) },
                            modifier = Modifier.weight(1f)
                        )
                        SplitModeSegmentItem(
                            label = "Tùy chỉnh",
                            selected = draft.splitMode == EngineSplitMode.CUSTOM,
                            onClick = { onSelectSplitMode(EngineSplitMode.CUSTOM) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 3. Switch "Người trả có tham gia chia tiền" (PRD Edge Case #1)
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    hazeState = hazeState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "Người trả ($resolvedPayerLabel) có tham gia chia tiền",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.textPrimary
                            )
                            Text(
                                text = if (draft.payerParticipates) {
                                    "$resolvedPayerLabel cùng chịu phần chia trong hóa đơn này"
                                } else {
                                    "$resolvedPayerLabel chỉ trả hộ ${FintechAlgorithm.formatVnd(draft.amount)}, phần chia = 0 đ"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (draft.payerParticipates) colors.textMuted else colors.deepLavender
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Switch(
                            checked = draft.payerParticipates,
                            onCheckedChange = onPayerParticipatesChanged,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.deepLavender,
                                uncheckedThumbColor = colors.textMuted,
                                uncheckedTrackColor = colors.lavenderContainer
                            )
                        )
                    }
                }

                // 4. Danh sách thành viên chia tiền
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    hazeState = hazeState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Phân bổ cho thành viên",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.textPrimary
                            )
                            val modeHint = when (draft.splitMode) {
                                EngineSplitMode.EQUAL -> "Chia đều cho ${includedParticipants.size} người được chọn"
                                EngineSplitMode.PERCENTAGE -> {
                                    val sumPct = includedParticipants.sumOf { it.percentage }
                                    "Tổng % hiện tại: ${formatPercentageLabel(sumPct)}"
                                }
                                EngineSplitMode.CUSTOM -> {
                                    val sumCustom = includedParticipants.sumOf { it.customAmount }
                                    "Đã nhập: ${FintechAlgorithm.formatVnd(sumCustom)} / ${FintechAlgorithm.formatVnd(draft.amount)}"
                                }
                            }
                            Text(
                                text = modeHint,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted
                            )
                        }

                        if (draft.splitMode == EngineSplitMode.PERCENTAGE) {
                            QuickSplitHelperChip(
                                label = "Chia đều 100%",
                                onClick = onDistributeEqualPercentages
                            )
                        } else if (draft.splitMode == EngineSplitMode.CUSTOM) {
                            QuickSplitHelperChip(
                                label = "Điền chia đều",
                                onClick = onPrefillCustomEqual
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (draft.participantInputs.isEmpty()) {
                        Text(
                            text = "Không có thành viên nào trong nhóm hiện tại.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted
                        )
                    } else {
                        draft.participantInputs.forEachIndexed { index, participant ->
                            SplitMemberV2Row(
                                participant = participant,
                                isPayer = participant.userId == draft.payerId,
                                splitMode = draft.splitMode,
                                totalAmount = draft.amount,
                                computedShare = validSharesByUserId[participant.userId],
                                percentageText = draft.percentageTexts[participant.userId] ?: "",
                                customAmountText = draft.customAmountTexts[participant.userId] ?: "",
                                onToggleIncluded = { onToggleMemberIncluded(participant.userId) },
                                onPercentageChanged = { text ->
                                    onPercentageChanged(participant.userId, text)
                                },
                                onCustomAmountChanged = { text ->
                                    onCustomAmountChanged(participant.userId, text)
                                }
                            )
                            if (index < draft.participantInputs.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    thickness = 0.8.dp,
                                    color = colors.divider
                                )
                            }
                        }
                    }
                }

                // 5. Banner kiểm tra hợp lệ trực tiếp (Mint vs Coral)
                when (val result = draft.validationResult) {
                    is SplitValidationResult.Valid -> {
                        StatusFeedbackBanner(
                            message = "Hợp lệ · Tổng phần chia khớp ${FintechAlgorithm.formatVnd(draft.amount)} (${result.shares.size} người)",
                            isValid = true
                        )
                    }
                    is SplitValidationResult.Invalid -> {
                        StatusFeedbackBanner(
                            message = result.errorMessage,
                            isValid = false
                        )
                    }
                }

                if (!draft.errorMessage.isNullOrBlank()) {
                    StatusFeedbackBanner(
                        message = draft.errorMessage,
                        isValid = false
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            SpendlyButton(
                text = if (draft.isEditMode) "Xác nhận & Cập nhật khoản chi" else "Xác nhận & Lưu khoản chi",
                onClick = onConfirmSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = isValidationOk && draft.isEditable && !draft.isSaving,
                isLoading = draft.isSaving,
                leadingIcon = Icons.Rounded.Check
            )

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun SummaryPillChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .clip(SpendlyShapeTokens.Pill)
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SplitModeSegmentItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    val bg = if (selected) colors.deepLavender else Color.Transparent
    val fg = if (selected) Color.White else colors.textMuted

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(SpendlyShapeTokens.Pill)
            .background(bg)
            .clickable(role = Role.Tab, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = fg,
            maxLines = 1
        )
    }
}

@Composable
private fun QuickSplitHelperChip(
    label: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    Box(
        modifier = Modifier
            .clip(SpendlyShapeTokens.Pill)
            .background(colors.lavenderContainer)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = colors.onLavenderContainer
        )
    }
}

@Composable
private fun SplitMemberV2Row(
    participant: ParticipantInput,
    isPayer: Boolean,
    splitMode: EngineSplitMode,
    totalAmount: Long,
    computedShare: ComputedShare?,
    percentageText: String,
    customAmountText: String,
    onToggleIncluded: () -> Unit,
    onPercentageChanged: (String) -> Unit,
    onCustomAmountChanged: (String) -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val rowAlpha = if (participant.isIncluded) 1f else 0.58f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(rowAlpha)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Checkbox tròn + Avatar + Tên thành viên (bấm để bật/tắt)
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(SpendlyShapeTokens.CardSmall)
                .clickable(role = Role.Checkbox, onClick = onToggleIncluded)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (participant.isIncluded) colors.deepLavender else Color.Transparent
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (participant.isIncluded) colors.deepLavender else colors.textMuted,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (participant.isIncluded) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            MemberInitialsCircle(
                displayName = participant.displayName,
                highlighted = participant.isIncluded && isPayer,
                size = 36.dp
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = participant.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isPayer) {
                        Box(
                            modifier = Modifier
                                .clip(SpendlyShapeTokens.Pill)
                                .background(colors.lavenderContainer)
                                .padding(horizontal = 7.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Người trả",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onLavenderContainer
                            )
                        }
                    }
                    if (!participant.isIncluded) {
                        Text(
                            text = "Không chia",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted
                        )
                    }
                }
            }
        }

        // Khu vực hiển thị / nhập số liệu theo chế độ chia
        when (splitMode) {
            EngineSplitMode.EQUAL -> {
                val shareAmount = if (participant.isIncluded) computedShare?.shareAmount ?: 0L else 0L
                val pctLabel = if (participant.isIncluded && computedShare != null) {
                    formatPercentageLabel(computedShare.percentage)
                } else {
                    "0%"
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = FintechAlgorithm.formatVnd(shareAmount),
                        style = SpendlyTextStyles.moneyMedium,
                        color = if (participant.isIncluded) colors.textPrimary else colors.textMuted
                    )
                    Text(
                        text = pctLabel,
                        style = SpendlyTextStyles.moneyCaption,
                        color = colors.textMuted
                    )
                }
            }

            EngineSplitMode.PERCENTAGE -> {
                val liveShareAmount = when {
                    !participant.isIncluded -> 0L
                    computedShare != null -> computedShare.shareAmount
                    totalAmount > 0L && participant.percentage > 0.0 -> {
                        (totalAmount * (participant.percentage / 100.0)).roundToLong()
                    }
                    else -> 0L
                }
                Column(horizontalAlignment = Alignment.End) {
                    OutlinedTextField(
                        value = percentageText,
                        onValueChange = { raw ->
                            val sanitized = sanitizeDecimalText(raw)
                            onPercentageChanged(sanitized)
                        },
                        enabled = participant.isIncluded,
                        modifier = Modifier.width(98.dp),
                        singleLine = true,
                        placeholder = { Text("0") },
                        suffix = { Text("%") },
                        textStyle = SpendlyTextStyles.moneySmall.copy(textAlign = TextAlign.End),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = SpendlyShapeTokens.CardSmall,
                        colors = spendlyOutlinedFieldColors()
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = FintechAlgorithm.formatVnd(liveShareAmount),
                        style = SpendlyTextStyles.moneyCaption,
                        color = if (participant.isIncluded && computedShare != null) {
                            colors.mintDeep
                        } else {
                            colors.textMuted
                        }
                    )
                }
            }

            EngineSplitMode.CUSTOM -> {
                val effectivePct = if (participant.isIncluded && totalAmount > 0L) {
                    (participant.customAmount * 100.0) / totalAmount.toDouble()
                } else {
                    0.0
                }
                Column(horizontalAlignment = Alignment.End) {
                    OutlinedTextField(
                        value = customAmountText,
                        onValueChange = { raw ->
                            val digits = raw.filter { it.isDigit() }.take(12)
                            onCustomAmountChanged(digits)
                        },
                        enabled = participant.isIncluded,
                        modifier = Modifier.width(136.dp),
                        singleLine = true,
                        placeholder = { Text("0") },
                        suffix = { Text("đ") },
                        textStyle = SpendlyTextStyles.moneySmall.copy(textAlign = TextAlign.End),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = SpendlyShapeTokens.CardSmall,
                        colors = spendlyOutlinedFieldColors()
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatPercentageLabel(effectivePct),
                        style = SpendlyTextStyles.moneyCaption,
                        color = colors.textMuted
                    )
                }
            }
        }
    }
}

private fun sanitizeDecimalText(raw: String): String {
    val normalized = raw.replace(',', '.')
    val sb = StringBuilder()
    var hasDot = false
    for (ch in normalized) {
        if (ch.isDigit()) {
            sb.append(ch)
        } else if (ch == '.' && !hasDot) {
            hasDot = true
            sb.append('.')
        }
    }
    return sb.toString().take(7)
}

@Preview(name = "SplitBillV2 Percentage Preview", showBackground = true, widthDp = 412, heightDp = 880)
@Composable
private fun SplitBillV2Preview() {
    val members = listOf(
        SpendlyGroupMember("u1", "Lê Văn Hùng", "VH", role = GroupRole.OWNER),
        SpendlyGroupMember("u2", "Lý Việt Linh", "VL", role = GroupRole.ADMIN),
        SpendlyGroupMember("u3", "Nguyễn Phan Tấn Đạt", "TĐ", role = GroupRole.MEMBER),
        SpendlyGroupMember("u4", "Lê Trung Bình", "TB", role = GroupRole.MEMBER)
    )
    val inputs = listOf(
        ParticipantInput("u1", "Lê Văn Hùng", isIncluded = false, percentage = 0.0),
        ParticipantInput("u2", "Lý Việt Linh", isIncluded = true, percentage = 40.0),
        ParticipantInput("u3", "Nguyễn Phan Tấn Đạt", isIncluded = true, percentage = 30.0),
        ParticipantInput("u4", "Lê Trung Bình", isIncluded = true, percentage = 30.0)
    )
    val validation = SplitCalculationEngine.calculateAndValidateSplit(
        totalAmount = 600_000L,
        mode = EngineSplitMode.PERCENTAGE,
        participants = inputs
    )

    SpendlyTheme {
        SplitBillV2Content(
            draft = ExpenseDraftUiState(
                groupId = "grp_1",
                groupName = "Quỹ Chi Tiêu Nhóm 08",
                title = "Vé xem phim hộ nhóm",
                amount = 600_000L,
                category = SpendlyCategory.ENTERTAINMENT,
                payerId = "u1",
                payerName = "Lê Văn Hùng",
                splitMode = EngineSplitMode.PERCENTAGE,
                payerParticipates = false,
                participantInputs = inputs,
                validationResult = validation,
                members = members,
                percentageTexts = mapOf("u2" to "40", "u3" to "30", "u4" to "30")
            ),
            onBackClicked = {},
            onSelectSplitMode = {},
            onPayerParticipatesChanged = {},
            onToggleMemberIncluded = {},
            onPercentageChanged = { _, _ -> },
            onCustomAmountChanged = { _, _ -> },
            onDistributeEqualPercentages = {},
            onPrefillCustomEqual = {},
            onConfirmSave = {}
        )
    }
}
