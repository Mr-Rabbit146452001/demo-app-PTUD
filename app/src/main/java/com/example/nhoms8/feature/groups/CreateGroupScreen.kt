package com.example.nhoms8.feature.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nhoms8.algorithm.FintechAlgorithm
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.GradientCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.core.designsystem.SpendlyButton
import com.example.nhoms8.domain.model.SpendlyGroup
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlyTextStyles
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.rememberHazeState

internal data class BudgetPresetOption(
    val label: String,
    val amount: Long
)

internal val GROUP_BUDGET_PRESETS = listOf(
    BudgetPresetOption("5 tr", 5_000_000L),
    BudgetPresetOption("10 tr", 10_000_000L),
    BudgetPresetOption("20 tr", 20_000_000L)
)

/**
 * Màn hình Tạo quỹ nhóm mới.
 */
@Composable
fun CreateGroupScreen(
    viewModel: GroupViewModel,
    onBackClick: () -> Unit,
    onGroupCreated: (SpendlyGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.groupsUiState.collectAsStateWithLifecycle()

    CreateGroupContent(
        isSubmitting = uiState.isSubmitting,
        serverErrorMessage = uiState.errorMessage,
        onBackClick = {
            viewModel.clearMessages()
            onBackClick()
        },
        onSubmit = { name, description, limit ->
            viewModel.createGroup(
                name = name,
                description = description,
                monthlyBudgetLimit = limit,
                onSuccess = onGroupCreated
            )
        },
        modifier = modifier
    )
}

/**
 * Giao diện thuần trạng thái cho màn hình Tạo quỹ nhóm mới.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateGroupContent(
    isSubmitting: Boolean = false,
    serverErrorMessage: String? = null,
    onBackClick: () -> Unit = {},
    onSubmit: (name: String, description: String, monthlyBudgetLimit: Long) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()

    var groupName by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var budgetDigits by rememberSaveable { mutableStateOf("10000000") }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    val parsedBudget = budgetDigits.toLongOrNull() ?: 0L
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.primaryLavender,
        unfocusedBorderColor = colors.divider,
        focusedLabelColor = colors.deepLavender,
        unfocusedLabelColor = colors.textMuted,
        cursorColor = colors.deepLavender,
        focusedContainerColor = colors.surface.copy(alpha = if (colors.isDark) 0.45f else 0.72f),
        unfocusedContainerColor = colors.surface.copy(alpha = if (colors.isDark) 0.30f else 0.55f),
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary
    )

    OrganicBackground(
        modifier = modifier.fillMaxSize(),
        hazeState = hazeState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
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

                Column {
                    Text(
                        text = "Tạo quỹ nhóm mới",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Tự động tạo mã mời QR 6 ký tự cho thành viên",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                }
            }

            GradientCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Ngân sách dự kiến hàng tháng",
                    style = MaterialTheme.typography.labelMedium,
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = FintechAlgorithm.formatVnd(parsedBudget),
                    style = SpendlyTextStyles.moneyHero
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (parsedBudget > 0L) {
                        FintechAlgorithm.convertNumberToWords(parsedBudget)
                    } else {
                        "Vui lòng nhập hạn mức lớn hơn 0 đ"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.88f)
                )
            }

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = {
                            groupName = it
                            localError = null
                        },
                        label = { Text("Tên quỹ nhóm *") },
                        placeholder = { Text("VD: Quỹ Phòng Trọ, Du lịch Đà Lạt...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Groups,
                                contentDescription = null,
                                tint = colors.deepLavender
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        shape = SpendlyShapeTokens.CardSmall,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Mô tả mục đích quỹ") },
                        placeholder = { Text("VD: Chi tiêu ăn uống, điện nước & sinh hoạt chung") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Description,
                                contentDescription = null,
                                tint = colors.deepLavender
                            )
                        },
                        minLines = 2,
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        shape = SpendlyShapeTokens.CardSmall,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Chọn nhanh hạn mức tháng",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GROUP_BUDGET_PRESETS.forEach { preset ->
                                val isSelected = parsedBudget == preset.amount
                                Box(
                                    modifier = Modifier
                                        .clip(SpendlyShapeTokens.Pill)
                                        .background(
                                            if (isSelected) colors.deepLavender else colors.lavenderContainer
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) colors.deepLavender else colors.divider,
                                            shape = SpendlyShapeTokens.Pill
                                        )
                                        .clickable {
                                            budgetDigits = preset.amount.toString()
                                            localError = null
                                        }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = preset.label,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isSelected) {
                                            androidx.compose.ui.graphics.Color.White
                                        } else {
                                            colors.onLavenderContainer
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = budgetDigits,
                        onValueChange = { raw ->
                            budgetDigits = raw.filter { it.isDigit() }.take(12)
                            localError = null
                        },
                        label = { Text("Hoặc nhập hạn mức tùy chỉnh (VNĐ) *") },
                        placeholder = { Text("10000000") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.AccountBalanceWallet,
                                contentDescription = null,
                                tint = colors.deepLavender
                            )
                        },
                        suffix = {
                            Text(
                                text = "VNĐ",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textMuted
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        shape = SpendlyShapeTokens.CardSmall,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val displayError = localError ?: serverErrorMessage
                    if (!displayError.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SpendlyShapeTokens.CardSmall)
                                .background(colors.coralContainer)
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = displayError,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = colors.coralDeep
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    SpendlyButton(
                        text = "Tạo quỹ nhóm",
                        onClick = {
                            val trimmedName = groupName.trim()
                            when {
                                trimmedName.isEmpty() -> {
                                    localError = "Vui lòng nhập tên quỹ nhóm"
                                }
                                parsedBudget <= 0L -> {
                                    localError = "Hạn mức ngân sách hàng tháng phải lớn hơn 0 đ"
                                }
                                else -> {
                                    localError = null
                                    onSubmit(trimmedName, description.trim(), parsedBudget)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSubmitting,
                        isLoading = isSubmitting,
                        leadingIcon = Icons.Rounded.AddCircleOutline
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 820)
@Composable
private fun CreateGroupScreenPreview() {
    SpendlyTheme {
        CreateGroupContent()
    }
}
