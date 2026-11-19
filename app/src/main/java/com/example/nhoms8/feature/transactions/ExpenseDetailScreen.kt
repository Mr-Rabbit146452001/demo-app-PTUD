package com.example.nhoms8.feature.transactions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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
import com.example.nhoms8.domain.model.ExpenseParticipant
import com.example.nhoms8.domain.model.RecordStatus
import com.example.nhoms8.domain.model.SpendlyCategory
import com.example.nhoms8.domain.model.SpendlyExpense
import com.example.nhoms8.domain.model.SplitMethod
import com.example.nhoms8.ui.theme.SpendlyCoralDeep
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlyTextStyles
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.rememberHazeState

/**
 * Màn hình Chi tiết khoản chi hiển thị trạng thái sổ cái (`ACTIVE` / `CANCELLED`),
 * hóa đơn đính kèm, chi tiết phần chia từng thành viên và hỗ trợ Sửa / Hủy mềm khoản chi.
 */
@Composable
fun ExpenseDetailScreen(
    viewModel: TransactionViewModel,
    groupId: String,
    expenseId: String,
    onBackClicked: () -> Unit,
    onEditExpenseClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
    currentUserId: String = "",
    currentUserName: String = ""
) {
    val detailState by viewModel.observeExpenseDetail(groupId, expenseId).collectAsStateWithLifecycle()

    LaunchedEffect(groupId, expenseId) {
        viewModel.clearDetailError()
    }

    ExpenseDetailContent(
        detailState = detailState,
        onBackClicked = onBackClicked,
        onEditExpenseClicked = { onEditExpenseClicked(expenseId) },
        onConfirmCancelExpense = {
            val exp = detailState.expense
            val resolvedActorId = currentUserId.ifBlank { exp?.createdBy.orEmpty() }
            val resolvedActorName = currentUserName.ifBlank { exp?.payerName.orEmpty() }
            viewModel.cancelExpense(
                groupId = groupId,
                expenseId = expenseId,
                actorId = resolvedActorId,
                actorName = resolvedActorName
            )
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpenseDetailContent(
    detailState: ExpenseDetailUiState,
    onBackClicked: () -> Unit,
    onEditExpenseClicked: () -> Unit,
    onConfirmCancelExpense: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var showCancelDialog by remember { mutableStateOf(false) }

    val expense = detailState.expense
    val isActive = expense?.status == RecordStatus.ACTIVE

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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            TransactionTopBar(
                title = "Chi tiết khoản chi",
                subtitle = expense?.category?.label ?: "Sổ cái giao dịch",
                onBackClicked = onBackClicked,
                hazeState = hazeState
            )

            Spacer(modifier = Modifier.height(14.dp))

            when {
                detailState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.primaryLavender)
                    }
                }

                expense == null -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            hazeState = hazeState,
                            contentPadding = PaddingValues(24.dp)
                        ) {
                            Text(
                                text = "Không tìm thấy khoản chi",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = detailState.errorMessage
                                    ?: "Khoản chi này không tồn tại hoặc đã bị xóa khỏi nhóm.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textMuted
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            SpendlyButton(
                                text = "Quay lại",
                                onClick = onBackClicked,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. Hero Card + Status Badge
                        GradientCard(
                            modifier = Modifier.fillMaxWidth(),
                            gradientColors = if (isActive) {
                                listOf(colors.primaryLavender, colors.indigo, colors.sky)
                            } else {
                                listOf(Color(0xFF6E657E), Color(0xFF534C63), Color(0xFF423D50))
                            },
                            contentPadding = PaddingValues(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ExpenseStatusBadge(status = expense.status)

                                Row(
                                    modifier = Modifier
                                        .clip(SpendlyShapeTokens.Pill)
                                        .background(Color.White.copy(alpha = 0.20f))
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = expense.category.icon(),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = expense.category.label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = expense.title,
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = FintechAlgorithm.formatVnd(expense.amount),
                                style = SpendlyTextStyles.moneyHero.copy(
                                    textDecoration = if (isActive) TextDecoration.None else TextDecoration.LineThrough
                                ),
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = FintechAlgorithm.convertNumberToWords(expense.amount),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.86f)
                            )
                        }

                        if (!isActive) {
                            StatusFeedbackBanner(
                                message = "Khoản chi đã bị hủy (CANCELLED) theo quy tắc sổ cái. Bản ghi vẫn được lưu lịch sử nhưng không còn tính vào số dư nợ.",
                                isValid = false
                            )
                        }

                        // 2. Thông tin chi tiết (Người trả, Ngày chi, Phương thức chia, Ghi chú)
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            hazeState = hazeState,
                            contentPadding = PaddingValues(18.dp)
                        ) {
                            Text(
                                text = "Thông tin khoản chi",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            DetailMetadataRow(
                                icon = Icons.Rounded.Person,
                                label = "Người trả tiền",
                                value = expense.payerName.ifBlank { expense.payerId }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 10.dp),
                                thickness = 0.8.dp,
                                color = colors.divider
                            )
                            DetailMetadataRow(
                                icon = Icons.Rounded.CalendarToday,
                                label = "Ngày chi",
                                value = formatDateVietnamese(expense.expenseDateMillis)
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 10.dp),
                                thickness = 0.8.dp,
                                color = colors.divider
                            )
                            DetailMetadataRow(
                                icon = Icons.Rounded.PieChart,
                                label = "Phương thức chia",
                                value = expense.splitType.label
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 10.dp),
                                thickness = 0.8.dp,
                                color = colors.divider
                            )
                            DetailMetadataRow(
                                icon = Icons.Rounded.Info,
                                label = "Ghi chú",
                                value = expense.description.ifBlank { "Không có ghi chú" },
                                isMutedValue = expense.description.isBlank()
                            )
                        }

                        // 3. Thẻ hóa đơn đính kèm (chỉ hiển thị nếu receiptUrl không rỗng)
                        if (expense.receiptUrl.isNotBlank()) {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                hazeState = hazeState,
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(SpendlyShapeTokens.CardSmall)
                                            .background(colors.lavenderContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                                            contentDescription = null,
                                            tint = colors.onLavenderContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Hóa đơn đính kèm",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = expense.receiptUrl,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.deepLavender,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ReceiptActionPill(
                                        icon = Icons.Rounded.ContentCopy,
                                        label = "Sao chép link",
                                        onClick = {
                                            copyToClipboard(context, expense.receiptUrl)
                                            Toast.makeText(
                                                context,
                                                "Đã sao chép link hóa đơn",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                    ReceiptActionPill(
                                        icon = Icons.AutoMirrored.Rounded.OpenInNew,
                                        label = "Mở liên kết",
                                        onClick = {
                                            runCatching {
                                                uriHandler.openUri(expense.receiptUrl)
                                            }.onFailure {
                                                Toast.makeText(
                                                    context,
                                                    "Không thể mở đường dẫn hóa đơn này",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // 4. Bảng phân bổ phần chia từng thành viên
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
                                    text = "Chi tiết phần chia (${detailState.participants.size} người)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = FintechAlgorithm.formatVnd(detailState.participants.sumOf { it.shareAmount }),
                                    style = SpendlyTextStyles.moneySmall,
                                    color = colors.deepLavender
                                )
                            }

                            val payerParticipates = detailState.participants.any { it.userId == expense.payerId }
                            if (!payerParticipates && expense.payerName.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(SpendlyShapeTokens.CardSmall)
                                        .background(colors.lavenderContainer.copy(alpha = 0.65f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "${expense.payerName} trả hộ toàn bộ hóa đơn và không tham gia chia phần.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.onLavenderContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (detailState.participants.isEmpty()) {
                                Text(
                                    text = "Chưa có dữ liệu phân bổ thành viên.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted
                                )
                            } else {
                                detailState.participants.forEachIndexed { index, participant ->
                                    val isPayer = participant.userId == expense.payerId
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            MemberInitialsCircle(
                                                displayName = participant.displayName,
                                                highlighted = isPayer,
                                                size = 36.dp
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = participant.displayName.ifBlank { participant.userId },
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = colors.textPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (isPayer) {
                                                    Text(
                                                        text = "Người thanh toán",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = colors.deepLavender
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = FintechAlgorithm.formatVnd(participant.shareAmount),
                                                style = SpendlyTextStyles.moneyMedium,
                                                color = colors.textPrimary
                                            )
                                            Text(
                                                text = formatPercentageLabel(participant.percentage),
                                                style = SpendlyTextStyles.moneyCaption,
                                                color = colors.textMuted
                                            )
                                        }
                                    }

                                    if (index < detailState.participants.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            thickness = 0.8.dp,
                                            color = colors.divider
                                        )
                                    }
                                }
                            }
                        }

                        if (!detailState.errorMessage.isNullOrBlank()) {
                            StatusFeedbackBanner(
                                message = detailState.errorMessage,
                                isValid = false
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // 5. Nút Sửa & Hủy khoản chi (CHỈ hiển thị khi trạng thái đang ACTIVE)
                    if (isActive) {
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showCancelDialog = true },
                                enabled = !detailState.isCancelling,
                                shape = SpendlyShapeTokens.Pill,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = colors.coralDeep,
                                    containerColor = colors.coralContainer.copy(alpha = 0.45f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.2.dp,
                                    color = colors.coralDeep.copy(alpha = 0.55f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Hủy khoản chi",
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1
                                )
                            }

                            SpendlyButton(
                                text = "Chỉnh sửa",
                                onClick = onEditExpenseClicked,
                                modifier = Modifier.weight(1f),
                                style = SpendlyButtonStyle.PRIMARY,
                                enabled = !detailState.isCancelling,
                                leadingIcon = Icons.Rounded.Edit
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    } else {
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }
    }

    if (showCancelDialog && expense != null) {
        AlertDialog(
            onDismissRequest = {
                if (!detailState.isCancelling) showCancelDialog = false
            },
            containerColor = colors.surfaceElevated,
            title = {
                Text(
                    text = "Xác nhận hủy khoản chi?",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Khoản chi \"${expense.title}\" (${FintechAlgorithm.formatVnd(expense.amount)}) sẽ được đánh dấu Đã hủy (CANCELLED) theo nguyên tắc sổ cái và số dư công nợ nhóm sẽ được tính lại tự động.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !detailState.isCancelling,
                    onClick = {
                        showCancelDialog = false
                        onConfirmCancelExpense()
                    }
                ) {
                    Text(
                        text = if (detailState.isCancelling) "Đang hủy..." else "Xác nhận hủy",
                        color = SpendlyCoralDeep,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !detailState.isCancelling,
                    onClick = { showCancelDialog = false }
                ) {
                    Text("Giữ lại", color = colors.textMuted)
                }
            }
        )
    }
}

@Composable
private fun ExpenseStatusBadge(status: RecordStatus) {
    val colors = MaterialTheme.spendlyColors
    val isActive = status == RecordStatus.ACTIVE
    val bg = if (isActive) colors.mintContainer else colors.coralContainer
    val fg = if (isActive) colors.mintDeep else colors.coralDeep
    val icon = if (isActive) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel
    val label = if (isActive) "Đang hiệu lực" else "Đã hủy / CANCELLED"

    Row(
        modifier = Modifier
            .clip(SpendlyShapeTokens.Pill)
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
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
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = fg
        )
    }
}

@Composable
private fun DetailMetadataRow(
    icon: ImageVector,
    label: String,
    value: String,
    isMutedValue: Boolean = false
) {
    val colors = MaterialTheme.spendlyColors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(SpendlyShapeTokens.CardSmall)
                .background(colors.lavenderContainer.copy(alpha = 0.65f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.deepLavender,
                modifier = Modifier.size(17.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = colors.textMuted
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = if (isMutedValue) colors.textMuted else colors.textPrimary
            )
        }
    }
}

@Composable
private fun ReceiptActionPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    Row(
        modifier = Modifier
            .clip(SpendlyShapeTokens.Pill)
            .background(colors.lavenderContainer.copy(alpha = 0.75f))
            .border(1.dp, colors.primaryLavender.copy(alpha = 0.35f), SpendlyShapeTokens.Pill)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.onLavenderContainer,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.onLavenderContainer
        )
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText("Receipt URL", text))
}

@Preview(name = "ExpenseDetailScreen Active Preview", showBackground = true, widthDp = 412, heightDp = 880)
@Composable
private fun ExpenseDetailActivePreview() {
    val expense = SpendlyExpense(
        expenseId = "exp_1",
        groupId = "grp_1",
        title = "Lẩu Thái liên hoan đồ án",
        amount = 850_000L,
        category = SpendlyCategory.FOOD,
        payerId = "u1",
        payerName = "Lê Văn Hùng",
        createdBy = "u1",
        splitType = SplitMethod.PERCENTAGE,
        description = "Bao gồm nước ngọt và tráng miệng",
        receiptUrl = "https://spendly.vn/receipts/bill_850k.jpg",
        expenseDateMillis = 1_700_000_000_000L,
        status = RecordStatus.ACTIVE
    )
    val participants = listOf(
        ExpenseParticipant("u1", "Lê Văn Hùng", 340_000L, 40.0),
        ExpenseParticipant("u2", "Lý Việt Linh", 255_000L, 30.0),
        ExpenseParticipant("u3", "Nguyễn Phan Tấn Đạt", 255_000L, 30.0)
    )
    SpendlyTheme {
        ExpenseDetailContent(
            detailState = ExpenseDetailUiState(
                isLoading = false,
                expense = expense,
                participants = participants
            ),
            onBackClicked = {},
            onEditExpenseClicked = {},
            onConfirmCancelExpense = {}
        )
    }
}

@Preview(name = "ExpenseDetailScreen Cancelled Preview", showBackground = true, widthDp = 412, heightDp = 840)
@Composable
private fun ExpenseDetailCancelledPreview() {
    val expense = SpendlyExpense(
        expenseId = "exp_2",
        groupId = "grp_1",
        title = "Vé xem phim cuối tuần",
        amount = 600_000L,
        category = SpendlyCategory.ENTERTAINMENT,
        payerId = "u1",
        payerName = "Lê Văn Hùng",
        createdBy = "u1",
        splitType = SplitMethod.EQUAL,
        expenseDateMillis = 1_700_000_000_000L,
        status = RecordStatus.CANCELLED
    )
    val participants = listOf(
        ExpenseParticipant("u2", "Lý Việt Linh", 300_000L, 50.0),
        ExpenseParticipant("u3", "Nguyễn Phan Tấn Đạt", 300_000L, 50.0)
    )
    SpendlyTheme {
        ExpenseDetailContent(
            detailState = ExpenseDetailUiState(
                isLoading = false,
                expense = expense,
                participants = participants
            ),
            onBackClicked = {},
            onEditExpenseClicked = {},
            onConfirmCancelExpense = {}
        )
    }
}
