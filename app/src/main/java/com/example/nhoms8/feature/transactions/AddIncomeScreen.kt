package com.example.nhoms8.feature.transactions

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.core.designsystem.SpendlyButton
import com.example.nhoms8.domain.model.GroupRole
import com.example.nhoms8.domain.model.SpendlyCategory
import com.example.nhoms8.domain.model.SpendlyGroupMember
import com.example.nhoms8.ui.theme.SpendlyIndigo
import com.example.nhoms8.ui.theme.SpendlyLavender
import com.example.nhoms8.ui.theme.SpendlyMintDeep
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.rememberHazeState

/**
 * Màn hình Thêm khoản thu / Nạp quỹ nhóm (`SpendlyIncome`, `isFundContribution = true`),
 * tách biệt rõ với quy trình Quyết toán & Trả nợ (Settlement).
 */
@Composable
fun AddIncomeScreen(
    viewModel: TransactionViewModel,
    groupId: String,
    currentUserId: String,
    onBackClicked: () -> Unit,
    onSavedSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val incomeState by viewModel.incomeState.collectAsStateWithLifecycle()

    LaunchedEffect(groupId, currentUserId) {
        viewModel.loadGroupForIncome(groupId = groupId, currentUserId = currentUserId)
    }

    AddIncomeContent(
        incomeState = incomeState,
        currentUserId = currentUserId,
        onBackClicked = onBackClicked,
        onClearError = viewModel::clearIncomeError,
        onSubmitIncome = { title, amount, payerId, category, description ->
            viewModel.createIncome(
                groupId = groupId,
                title = title,
                amount = amount,
                payerId = payerId,
                category = category,
                description = description,
                currentUserId = currentUserId,
                onSuccess = onSavedSuccess
            )
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddIncomeContent(
    incomeState: IncomeUiState,
    currentUserId: String,
    onBackClicked: () -> Unit,
    onClearError: () -> Unit,
    onSubmitIncome: (
        title: String,
        amount: Long,
        payerId: String,
        category: SpendlyCategory,
        description: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableLongStateOf(0L) }
    var selectedPayerId by remember(incomeState.groupId) {
        mutableStateOf(incomeState.defaultPayerId.ifBlank { currentUserId })
    }
    var selectedCategory by remember { mutableStateOf(SpendlyCategory.OTHER) }
    var description by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(incomeState.defaultPayerId, incomeState.members) {
        if (selectedPayerId.isBlank() ||
            (incomeState.members.isNotEmpty() && incomeState.members.none { it.userId == selectedPayerId })
        ) {
            selectedPayerId = incomeState.defaultPayerId.ifBlank {
                incomeState.members.firstOrNull()?.userId.orEmpty()
            }
        }
    }

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
                title = "Thêm khoản thu / Nạp quỹ",
                subtitle = incomeState.groupName.ifBlank { "Ghi nhận đóng góp vào quỹ chung" },
                onBackClicked = onBackClicked,
                hazeState = hazeState
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (incomeState.isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.mintDeep)
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AmountEntryHeroCard(
                        label = "SỐ TIỀN NẠP QUỸ / THU",
                        amount = amount,
                        onAmountChange = { next ->
                            amount = next
                            localError = null
                            onClearError()
                        },
                        badgeText = "Nạp quỹ nhóm",
                        gradientColors = listOf(
                            SpendlyMintDeep,
                            Color(0xFF3E8E89),
                            SpendlyIndigo,
                            SpendlyLavender
                        ),
                        quickPresetAmounts = listOf(100_000L, 200_000L, 500_000L, 1_000_000L, 2_000_000L)
                    )

                    // Thông báo tách biệt giữa Đóng quỹ (Income) và Trả nợ (Settlement)
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        hazeState = hazeState,
                        tintColor = colors.mintContainer.copy(alpha = if (colors.isDark) 0.35f else 0.78f),
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                tint = colors.mintDeep,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = "Tách biệt với Quyết toán nợ (Settlement)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.mintDeep
                                )
                                Text(
                                    text = "Khoản thu / nạp quỹ sẽ cộng vào ngân sách chung của nhóm và không tự động cấn trừ công nợ chia bill giữa các thành viên.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        hazeState = hazeState,
                        contentPadding = PaddingValues(18.dp)
                    ) {
                        Text(
                            text = "Tên khoản thu / Đợt đóng quỹ",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = title,
                            onValueChange = { text ->
                                title = text
                                localError = null
                                onClearError()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Ví dụ: Đóng quỹ tháng 11, Tài trợ team...") },
                            singleLine = true,
                            shape = SpendlyShapeTokens.CardSmall,
                            colors = spendlyOutlinedFieldColors()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Thành viên đóng quỹ",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (incomeState.members.isEmpty()) {
                            Text(
                                text = "Chưa tải được danh sách thành viên của quỹ",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted
                            )
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                incomeState.members.forEach { member ->
                                    MemberSelectionChip(
                                        member = member,
                                        selected = member.userId == selectedPayerId,
                                        onClick = {
                                            selectedPayerId = member.userId
                                            localError = null
                                            onClearError()
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Phân loại khoản thu",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CategorySelectorFlow(
                            selectedCategory = selectedCategory,
                            onCategorySelected = { selectedCategory = it }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Ghi chú (tùy chọn)",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Ghi chú thêm về nguồn tiền hoặc kỳ đóng quỹ...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Notes,
                                    contentDescription = null,
                                    tint = colors.textMuted
                                )
                            },
                            minLines = 2,
                            maxLines = 4,
                            shape = SpendlyShapeTokens.CardSmall,
                            colors = spendlyOutlinedFieldColors()
                        )
                    }

                    val errorText = localError ?: incomeState.errorMessage
                    if (!errorText.isNullOrBlank()) {
                        StatusFeedbackBanner(
                            message = errorText,
                            isValid = false
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            SpendlyButton(
                text = "Xác nhận nạp quỹ",
                onClick = {
                    when {
                        amount <= 0L -> localError = "Vui lòng nhập số tiền nạp quỹ lớn hơn 0 đ"
                        title.trim().isEmpty() -> localError = "Vui lòng nhập tên khoản thu / đóng quỹ"
                        selectedPayerId.isBlank() -> localError = "Vui lòng chọn thành viên đóng quỹ"
                        else -> {
                            localError = null
                            onSubmitIncome(
                                title.trim(),
                                amount,
                                selectedPayerId,
                                selectedCategory,
                                description.trim()
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !incomeState.isLoading && !incomeState.isSaving,
                isLoading = incomeState.isSaving,
                leadingIcon = Icons.Rounded.Check
            )

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Preview(name = "AddIncomeScreen Preview", showBackground = true, widthDp = 412, heightDp = 860)
@Composable
private fun AddIncomeScreenPreview() {
    val members = listOf(
        SpendlyGroupMember("u1", "Lê Văn Hùng", "VH", role = GroupRole.OWNER),
        SpendlyGroupMember("u2", "Lý Việt Linh", "VL", role = GroupRole.ADMIN),
        SpendlyGroupMember("u3", "Nguyễn Phan Tấn Đạt", "TĐ", role = GroupRole.MEMBER),
        SpendlyGroupMember("u4", "Lê Trung Bình", "TB", role = GroupRole.MEMBER)
    )
    SpendlyTheme {
        AddIncomeContent(
            incomeState = IncomeUiState(
                groupId = "grp_1",
                groupName = "Quỹ Chi Tiêu Nhóm 08",
                members = members,
                defaultPayerId = "u1"
            ),
            currentUserId = "u1",
            onBackClicked = {},
            onClearError = {},
            onSubmitIncome = { _, _, _, _, _ -> }
        )
    }
}
