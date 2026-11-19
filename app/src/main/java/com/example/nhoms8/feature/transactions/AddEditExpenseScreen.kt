package com.example.nhoms8.feature.transactions

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nhoms8.algorithm.FintechAlgorithm
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.GradientCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.core.designsystem.SpendlyButton
import com.example.nhoms8.core.designsystem.spendlyGlassSurface
import com.example.nhoms8.domain.model.GroupRole
import com.example.nhoms8.domain.model.SpendlyCategory
import com.example.nhoms8.domain.model.SpendlyGroupMember
import com.example.nhoms8.ui.theme.SpendlyIndigo
import com.example.nhoms8.ui.theme.SpendlyLavender
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlySky
import com.example.nhoms8.ui.theme.SpendlyTextStyles
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Màn hình Tạo mới hoặc Chỉnh sửa khoản chi (Tạo / Sửa khoản chi -> chuyển sang SplitBillV2).
 */
@Composable
fun AddEditExpenseScreen(
    viewModel: TransactionViewModel,
    onContinueToSplit: () -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val draft by viewModel.draftState.collectAsStateWithLifecycle()

    AddEditExpenseContent(
        draft = draft,
        onUpdateDraft = { title, amount, category, payerId, payerName, description, receiptUrl, dateMillis ->
            viewModel.updateDraft(
                title = title,
                amount = amount,
                category = category,
                payerId = payerId,
                payerName = payerName,
                description = description,
                receiptUrl = receiptUrl,
                expenseDateMillis = dateMillis
            )
        },
        onContinueToSplit = onContinueToSplit,
        onBackClicked = onBackClicked,
        modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseContent(
    draft: ExpenseDraftUiState,
    onUpdateDraft: (
        title: String?,
        amount: Long?,
        category: SpendlyCategory?,
        payerId: String?,
        payerName: String?,
        description: String?,
        receiptUrl: String?,
        expenseDateMillis: Long?
    ) -> Unit,
    onContinueToSplit: () -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()
    var localValidationMessage by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val nowMillis = remember { System.currentTimeMillis() }
    val yesterdayMillis = remember(nowMillis) { nowMillis - 86_400_000L }
    val isTodaySelected = isSameCalendarDay(draft.expenseDateMillis, nowMillis)
    val isYesterdaySelected = !isTodaySelected && isSameCalendarDay(draft.expenseDateMillis, yesterdayMillis)

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
                title = if (draft.isEditMode) "Chỉnh sửa khoản chi" else "Tạo khoản chi",
                subtitle = draft.groupName.ifBlank { "Quỹ chi tiêu Spendly" },
                onBackClicked = onBackClicked,
                hazeState = hazeState
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (draft.isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.primaryLavender)
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AmountEntryHeroCard(
                        label = if (draft.isEditMode) "SỐ TIỀN KHOẢN CHI (CHỈNH SỬA)" else "SỐ TIỀN CHI",
                        amount = draft.amount,
                        onAmountChange = { newAmount ->
                            localValidationMessage = null
                            onUpdateDraft(null, newAmount, null, null, null, null, null, null)
                        },
                        badgeText = draft.category.label
                    )

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        hazeState = hazeState,
                        contentPadding = PaddingValues(18.dp)
                    ) {
                        Text(
                            text = "Tên khoản chi",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = draft.title,
                            onValueChange = { text ->
                                localValidationMessage = null
                                onUpdateDraft(text, null, null, null, null, null, null, null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text("Ví dụ: Lẩu Thái cuối tuần, Tiền điện...")
                            },
                            singleLine = true,
                            shape = SpendlyShapeTokens.CardSmall,
                            colors = spendlyOutlinedFieldColors()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Danh mục chi tiêu",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        CategorySelectorFlow(
                            selectedCategory = draft.category,
                            onCategorySelected = { category ->
                                onUpdateDraft(null, null, category, null, null, null, null, null)
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Người trả tiền",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (draft.members.isEmpty()) {
                            Text(
                                text = "Chưa có thành viên trong quỹ này",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted
                            )
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                draft.members.forEach { member ->
                                    val selected = member.userId == draft.payerId
                                    MemberSelectionChip(
                                        member = member,
                                        selected = selected,
                                        onClick = {
                                            localValidationMessage = null
                                            onUpdateDraft(
                                                null,
                                                null,
                                                null,
                                                member.userId,
                                                member.displayName,
                                                null,
                                                null,
                                                null
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ngày chi",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.textPrimary
                            )
                            Text(
                                text = formatDateVietnamese(draft.expenseDateMillis),
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.deepLavender
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DateOptionChip(
                                label = "Hôm nay",
                                selected = isTodaySelected,
                                onClick = {
                                    onUpdateDraft(
                                        null, null, null, null, null, null, null,
                                        System.currentTimeMillis()
                                    )
                                }
                            )
                            DateOptionChip(
                                label = "Hôm qua",
                                selected = isYesterdaySelected,
                                onClick = {
                                    onUpdateDraft(
                                        null, null, null, null, null, null, null,
                                        System.currentTimeMillis() - 86_400_000L
                                    )
                                }
                            )
                            DateOptionChip(
                                label = if (!isTodaySelected && !isYesterdaySelected) {
                                    formatDateShort(draft.expenseDateMillis)
                                } else {
                                    "Chọn ngày..."
                                },
                                selected = !isTodaySelected && !isYesterdaySelected,
                                leadingIcon = Icons.Rounded.CalendarToday,
                                onClick = { showDatePicker = true }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Ghi chú & hóa đơn (tùy chọn)",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = draft.description,
                            onValueChange = { text ->
                                onUpdateDraft(null, null, null, null, null, text, null, null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Mô tả chi tiết") },
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

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = draft.receiptUrl,
                            onValueChange = { text ->
                                onUpdateDraft(null, null, null, null, null, null, text, null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Link ảnh hóa đơn (Receipt URL)") },
                            placeholder = { Text("https://...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Link,
                                    contentDescription = null,
                                    tint = colors.textMuted
                                )
                            },
                            singleLine = true,
                            shape = SpendlyShapeTokens.CardSmall,
                            colors = spendlyOutlinedFieldColors()
                        )
                    }

                    val displayError = localValidationMessage ?: draft.errorMessage
                    if (!displayError.isNullOrBlank()) {
                        StatusFeedbackBanner(
                            message = displayError,
                            isValid = false
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            SpendlyButton(
                text = "Tiếp tục chia tiền",
                onClick = {
                    when {
                        !draft.isEditable -> {
                            localValidationMessage = "Khoản chi đã bị hủy không thể chỉnh sửa"
                        }
                        draft.amount <= 0L -> {
                            localValidationMessage = "Vui lòng nhập số tiền lớn hơn 0 đ"
                        }
                        draft.title.trim().isEmpty() -> {
                            localValidationMessage = "Vui lòng nhập tên khoản chi"
                        }
                        draft.payerId.isBlank() -> {
                            localValidationMessage = "Vui lòng chọn người trả tiền"
                        }
                        draft.members.isEmpty() -> {
                            localValidationMessage = "Nhóm chưa có thành viên để chia tiền"
                        }
                        else -> {
                            localValidationMessage = null
                            onContinueToSplit()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !draft.isLoading && draft.isEditable,
                trailingIcon = Icons.AutoMirrored.Rounded.ArrowForward
            )

            Spacer(modifier = Modifier.height(14.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = draft.expenseDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { pickedUtc ->
                            onUpdateDraft(
                                null, null, null, null, null, null, null,
                                utcDatePickerMillisToLocalNoon(pickedUtc)
                            )
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Chọn", color = colors.deepLavender, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy", color = colors.textMuted)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// ============================================================================
// SHARED TRANSACTION UI COMPONENTS (internal across feature.transactions)
// ============================================================================

@Composable
internal fun TransactionTopBar(
    title: String,
    subtitle: String?,
    onBackClicked: () -> Unit,
    hazeState: HazeState? = null,
    trailingAction: (@Composable () -> Unit)? = null
) {
    val colors = MaterialTheme.spendlyColors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .spendlyGlassSurface(
                        hazeState = hazeState,
                        shape = CircleShape,
                        tintColor = colors.glassContainer
                    )
                    .clickable(role = Role.Button, onClick = onBackClicked),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

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
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (trailingAction != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingAction()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AmountEntryHeroCard(
    label: String,
    amount: Long,
    onAmountChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
    gradientColors: List<Color> = listOf(SpendlyLavender, SpendlyIndigo, SpendlySky),
    quickPresetAmounts: List<Long> = listOf(50_000L, 100_000L, 200_000L, 500_000L)
) {
    val focusRequester = remember { FocusRequester() }
    val digitsValue = if (amount <= 0L) "" else amount.toString()
    val visualTransformation = remember { ThousandSeparatorVisualTransformation() }

    GradientCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { focusRequester.requestFocus() },
        gradientColors = gradientColors,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White.copy(alpha = 0.85f)
            )
            if (!badgeText.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .clip(SpendlyShapeTokens.Pill)
                        .background(Color.White.copy(alpha = 0.20f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            BasicTextField(
                value = digitsValue,
                onValueChange = { input ->
                    val cleaned = input.filter { it.isDigit() }.take(12)
                    val parsed = cleaned.toLongOrNull() ?: 0L
                    onAmountChange(parsed)
                },
                modifier = Modifier.focusRequester(focusRequester),
                singleLine = true,
                textStyle = SpendlyTextStyles.moneyHero.copy(
                    color = Color.White,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                cursorBrush = SolidColor(Color.White),
                visualTransformation = visualTransformation,
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.Center) {
                        if (digitsValue.isEmpty()) {
                            Text(
                                text = "0",
                                style = SpendlyTextStyles.moneyHero,
                                color = Color.White.copy(alpha = 0.50f)
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "đ",
                style = SpendlyTextStyles.moneyLarge,
                color = Color.White.copy(alpha = 0.85f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = FintechAlgorithm.convertNumberToWords(amount),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.90f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        if (quickPresetAmounts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickPresetAmounts.forEach { preset ->
                    Box(
                        modifier = Modifier
                            .clip(SpendlyShapeTokens.Pill)
                            .background(Color.White.copy(alpha = 0.18f))
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.30f),
                                shape = SpendlyShapeTokens.Pill
                            )
                            .clickable { onAmountChange(preset) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = FintechAlgorithm.formatShortVnd(preset),
                            style = SpendlyTextStyles.moneyCaption.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CategorySelectorFlow(
    selectedCategory: SpendlyCategory,
    onCategorySelected: (SpendlyCategory) -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SpendlyCategory.entries.forEach { category ->
            val selected = category == selectedCategory
            val bg = if (selected) {
                colors.deepLavender
            } else {
                colors.surface.copy(alpha = if (colors.isDark) 0.55f else 0.78f)
            }
            val fg = if (selected) Color.White else colors.textPrimary
            val borderColor = if (selected) colors.deepLavender else colors.divider

            Row(
                modifier = Modifier
                    .clip(SpendlyShapeTokens.Pill)
                    .background(bg)
                    .border(width = 1.dp, color = borderColor, shape = SpendlyShapeTokens.Pill)
                    .clickable(role = Role.RadioButton) { onCategorySelected(category) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = category.icon(),
                    contentDescription = null,
                    tint = fg,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = category.label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    color = fg,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
internal fun MemberSelectionChip(
    member: SpendlyGroupMember,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val bg = if (selected) {
        colors.lavenderContainer
    } else {
        colors.surface.copy(alpha = if (colors.isDark) 0.55f else 0.78f)
    }
    val borderColor = if (selected) colors.primaryLavender else colors.divider

    Row(
        modifier = Modifier
            .clip(SpendlyShapeTokens.Pill)
            .background(bg)
            .border(width = if (selected) 1.5.dp else 1.dp, color = borderColor, shape = SpendlyShapeTokens.Pill)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(start = 6.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MemberInitialsCircle(
            displayName = member.displayName,
            initials = member.initials,
            highlighted = selected,
            size = 26.dp
        )
        Text(
            text = member.displayName,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (selected) colors.onLavenderContainer else colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = colors.deepLavender,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
internal fun MemberInitialsCircle(
    displayName: String,
    initials: String = "",
    highlighted: Boolean = false,
    size: Dp = 36.dp
) {
    val colors = MaterialTheme.spendlyColors
    val bg = if (highlighted) colors.deepLavender else colors.lavenderContainer
    val fg = if (highlighted) Color.White else colors.onLavenderContainer
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initialsFor(displayName, initials),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = fg,
            maxLines = 1
        )
    }
}

@Composable
private fun DateOptionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    leadingIcon: ImageVector? = null
) {
    val colors = MaterialTheme.spendlyColors
    val bg = if (selected) colors.deepLavender else colors.surface.copy(alpha = if (colors.isDark) 0.55f else 0.78f)
    val fg = if (selected) Color.White else colors.textPrimary
    val border = if (selected) colors.deepLavender else colors.divider

    Row(
        modifier = Modifier
            .clip(SpendlyShapeTokens.Pill)
            .background(bg)
            .border(width = 1.dp, color = border, shape = SpendlyShapeTokens.Pill)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = fg
        )
    }
}

@Composable
internal fun StatusFeedbackBanner(
    message: String,
    isValid: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    val bg = if (isValid) colors.mintContainer else colors.coralContainer
    val border = if (isValid) colors.mint else colors.coral
    val fg = if (isValid) colors.mintDeep else colors.coralDeep
    val icon = if (isValid) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(SpendlyShapeTokens.CardMedium)
            .background(bg)
            .border(width = 1.dp, color = border, shape = SpendlyShapeTokens.CardMedium)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = fg,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
internal fun spendlyOutlinedFieldColors(): TextFieldColors {
    val colors = MaterialTheme.spendlyColors
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.primaryLavender,
        unfocusedBorderColor = colors.divider,
        focusedLabelColor = colors.deepLavender,
        unfocusedLabelColor = colors.textMuted,
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary,
        cursorColor = colors.deepLavender,
        focusedContainerColor = colors.surface.copy(alpha = if (colors.isDark) 0.35f else 0.55f),
        unfocusedContainerColor = colors.surface.copy(alpha = if (colors.isDark) 0.25f else 0.40f)
    )
}

internal fun SpendlyCategory.icon(): ImageVector = when (this) {
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

internal fun initialsFor(name: String, explicitInitials: String = ""): String {
    if (explicitInitials.isNotBlank()) return explicitInitials.take(2).uppercase()
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return parts.mapNotNull { it.firstOrNull()?.toString() }
        .takeLast(2)
        .joinToString("")
        .uppercase()
        .ifBlank { "TV" }
}

internal fun formatDateVietnamese(millis: Long): String {
    if (millis <= 0L) return FintechAlgorithm.getTodayFormatted()
    val sdf = SimpleDateFormat("dd 'tháng' MM, yyyy", Locale.forLanguageTag("vi-VN"))
    return sdf.format(Date(millis))
}

internal fun formatDateShort(millis: Long): String {
    if (millis <= 0L) return ""
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

internal fun isSameCalendarDay(millis1: Long, millis2: Long): Boolean {
    if (millis1 <= 0L || millis2 <= 0L) return false
    val c1 = Calendar.getInstance().apply { timeInMillis = millis1 }
    val c2 = Calendar.getInstance().apply { timeInMillis = millis2 }
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
        c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}

private fun utcDatePickerMillisToLocalNoon(utcMillis: Long): Long {
    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = utcMillis
    }
    return Calendar.getInstance().apply {
        set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
        set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

internal fun formatPercentageLabel(percentage: Double): String {
    val bd = BigDecimal.valueOf(percentage)
        .setScale(2, RoundingMode.HALF_UP)
        .stripTrailingZeros()
    return "${bd.toPlainString()}%"
}

private class ThousandSeparatorVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        if (digits.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }
        val len = digits.length
        val sb = StringBuilder()
        val origToTrans = IntArray(len + 1)
        for (i in 0 until len) {
            origToTrans[i] = sb.length
            sb.append(digits[i])
            val remaining = len - 1 - i
            if (remaining > 0 && remaining % 3 == 0) {
                sb.append('.')
            }
        }
        origToTrans[len] = sb.length
        val transformed = sb.toString()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return origToTrans[offset.coerceIn(0, len)]
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, transformed.length)
                var count = 0
                for (idx in 0 until clamped) {
                    if (transformed[idx] != '.') count++
                }
                return count.coerceIn(0, len)
            }
        }
        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}

@Preview(name = "AddEditExpenseScreen Preview", showBackground = true, widthDp = 412, heightDp = 860)
@Composable
private fun AddEditExpenseScreenPreview() {
    val sampleMembers = listOf(
        SpendlyGroupMember("u1", "Lê Văn Hùng", "VH", role = GroupRole.OWNER),
        SpendlyGroupMember("u2", "Lý Việt Linh", "VL", role = GroupRole.ADMIN),
        SpendlyGroupMember("u3", "Nguyễn Phan Tấn Đạt", "TĐ", role = GroupRole.MEMBER),
        SpendlyGroupMember("u4", "Lê Trung Bình", "TB", role = GroupRole.MEMBER)
    )
    SpendlyTheme {
        AddEditExpenseContent(
            draft = ExpenseDraftUiState(
                groupId = "grp_1",
                groupName = "Quỹ Chi Tiêu Nhóm 08",
                title = "Lẩu Thái liên hoan tuần 3",
                amount = 850_000L,
                category = SpendlyCategory.FOOD,
                payerId = "u1",
                payerName = "Lê Văn Hùng",
                description = "Thanh toán tại quán Kichi",
                members = sampleMembers
            ),
            onUpdateDraft = { _, _, _, _, _, _, _, _ -> },
            onContinueToSplit = {},
            onBackClicked = {}
        )
    }
}
