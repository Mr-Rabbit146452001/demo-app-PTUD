package com.example.nhoms8

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.FilterAltOff
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhoms8.algorithm.FintechAlgorithm
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.core.designsystem.SpendlyButton
import com.example.nhoms8.core.designsystem.SpendlyButtonStyle
import com.example.nhoms8.core.designsystem.spendlyGlassSurface
import com.example.nhoms8.ui.theme.Nhoms8Theme
import com.example.nhoms8.ui.theme.SpendlyIndigo
import com.example.nhoms8.ui.theme.SpendlyLavender
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlyTextStyles
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState
import kotlin.math.abs

/**
 * Loại icon của từng giao dịch trong lịch sử
 */
enum class HistoryItemType {
    GROCERY,
    INTERNET,
    DEPOSIT,
    COFFEE,
    TAXI
}

/**
 * Dữ liệu chi tiết của 1 giao dịch trong lịch sử
 */
data class HistoryTransactionItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val amountFormatted: String,
    val isIncome: Boolean,
    val type: HistoryItemType
)

/**
 * Nhóm giao dịch theo ngày
 */
data class TransactionDateGroup(
    val dateHeader: String,
    val netTotalFormatted: String,
    val transactions: List<HistoryTransactionItem>
)

private val DefaultHistoryBackAction: () -> Unit = {}

private enum class HistoryPickerSheet {
    MEMBER,
    MONTH,
    CATEGORY
}

private val HISTORY_SCOPE_OPTIONS = listOf("Tất cả", "Nhóm", "Cá nhân")

private val DEFAULT_HISTORY_CATEGORIES = listOf(
    "Ăn uống",
    "Di chuyển",
    "Nhà ở",
    "Mua sắm",
    "Giải trí",
    "Y tế",
    "Giáo dục",
    "Sinh hoạt",
    "Thu quỹ",
    "Khác"
)

/**
 * 13 · Màn hình Lịch sử giao dịch và Bộ lọc (Transaction History & Filter Screen)
 * Giao diện Spendly Lavender / Organic / Haze 2.1.0 Glassmorphism với tìm kiếm trực tiếp,
 * bộ lọc phạm vi (Tất cả / Nhóm / Cá nhân) và bộ chọn Thành viên / Tháng / Danh mục.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    totalTransactionsCount: Int = 6,
    monthText: String = "Tháng 11/2026",
    transactionGroups: List<TransactionDateGroup>? = null,
    onSearchClicked: () -> Unit = {},
    onFilterMemberClicked: () -> Unit = {},
    onFilterMonthClicked: () -> Unit = {},
    onFilterCategoryClicked: () -> Unit = {},
    onClearFilterClicked: () -> Unit = {},
    onTransactionClicked: (HistoryTransactionItem) -> Unit = {},
    onNavTabSelected: (BottomNavTab) -> Unit = {},
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    selectedScope: String = "Nhóm",
    onScopeSelected: (String) -> Unit = {},
    availableMembers: List<Pair<String, String>> = emptyList(),
    selectedMemberId: String? = null,
    onMemberSelected: (String?) -> Unit = {},
    availableCategories: List<String> = emptyList(),
    selectedCategoryLabel: String? = null,
    onCategorySelected: (String?) -> Unit = {},
    availableMonths: List<String> = listOf("Tháng 11/2026", "Tháng 10/2026", "Tất cả"),
    selectedMonthLabel: String? = null,
    onMonthSelected: (String?) -> Unit = {},
    onBackClicked: () -> Unit = DefaultHistoryBackAction,
    showBackButton: Boolean = true,
    showBottomBar: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val searchFocusRequester = remember { FocusRequester() }

    var localSearchQuery by rememberSaveable { mutableStateOf(searchQuery) }
    var isSearchExpanded by rememberSaveable { mutableStateOf(searchQuery.isNotBlank()) }
    var localScope by rememberSaveable { mutableStateOf(selectedScope) }
    var localMemberId by rememberSaveable { mutableStateOf(selectedMemberId) }
    var localCategoryLabel by rememberSaveable { mutableStateOf(selectedCategoryLabel) }
    var localMonthLabel by rememberSaveable { mutableStateOf(selectedMonthLabel) }
    var activePickerSheet by remember { mutableStateOf<HistoryPickerSheet?>(null) }
    var selectedNavTab by rememberSaveable { mutableStateOf(BottomNavTab.HISTORY) }

    LaunchedEffect(searchQuery) {
        localSearchQuery = searchQuery
        if (searchQuery.isNotBlank()) {
            isSearchExpanded = true
        }
    }
    LaunchedEffect(selectedScope) { localScope = selectedScope }
    LaunchedEffect(selectedMemberId) { localMemberId = selectedMemberId }
    LaunchedEffect(selectedCategoryLabel) { localCategoryLabel = selectedCategoryLabel }
    LaunchedEffect(selectedMonthLabel) { localMonthLabel = selectedMonthLabel }

    val defaultGroups = remember { buildDefaultHistoryGroups() }
    val baseGroups = transactionGroups ?: defaultGroups

    val resolvedMembers = remember(availableMembers, baseGroups) {
        if (availableMembers.isNotEmpty()) {
            availableMembers
        } else {
            extractMembersFromGroups(baseGroups)
        }
    }

    val resolvedCategories = remember(availableCategories) {
        availableCategories.ifEmpty { DEFAULT_HISTORY_CATEGORIES }
    }

    val resolvedMonths = remember(availableMonths, monthText) {
        buildResolvedMonthOptions(availableMonths, monthText)
    }

    val selectedMemberName = remember(localMemberId, resolvedMembers) {
        localMemberId?.let { id ->
            resolvedMembers.firstOrNull { it.first == id }?.second ?: id
        }
    }

    val filteredGroups = remember(
        baseGroups,
        localSearchQuery,
        localScope,
        localMemberId,
        selectedMemberName,
        localCategoryLabel,
        localMonthLabel,
        monthText
    ) {
        filterTransactionGroups(
            groups = baseGroups,
            query = localSearchQuery,
            scope = localScope,
            selectedMemberName = selectedMemberName,
            selectedCategory = localCategoryLabel,
            selectedMonth = localMonthLabel,
            currentMonthText = monthText
        )
    }

    val rawTransactionCount = remember(baseGroups, totalTransactionsCount, transactionGroups) {
        if (transactionGroups == null) baseGroups.sumOf { it.transactions.size }
        else maxOf(totalTransactionsCount, baseGroups.sumOf { it.transactions.size })
    }
    val visibleTransactionCount = remember(filteredGroups) {
        filteredGroups.sumOf { it.transactions.size }
    }

    val hasActiveFilter = localSearchQuery.isNotBlank() ||
        localScope != "Nhóm" ||
        localMemberId != null ||
        localCategoryLabel != null ||
        (localMonthLabel != null && localMonthLabel != "Tất cả")

    fun resetAllFilters() {
        localSearchQuery = ""
        localScope = "Nhóm"
        localMemberId = null
        localCategoryLabel = null
        localMonthLabel = null
        onSearchQueryChange("")
        onScopeSelected("Nhóm")
        onMemberSelected(null)
        onCategorySelected(null)
        onMonthSelected(null)
        onClearFilterClicked()
    }

    val handleBack: () -> Unit = {
        if (onBackClicked === DefaultHistoryBackAction) {
            backDispatcher?.onBackPressed()
        } else {
            onBackClicked()
        }
    }

    OrganicBackground(
        modifier = modifier.fillMaxSize(),
        hazeState = hazeState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = if (showBottomBar) 24.dp else 36.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "top_bar") {
                    HistoryTopBar(
                        title = "Lịch sử giao dịch",
                        subtitle = if (hasActiveFilter) {
                            "$visibleTransactionCount/$rawTransactionCount giao dịch · ${localMonthLabel ?: monthText}"
                        } else {
                            "$visibleTransactionCount giao dịch · ${localMonthLabel ?: monthText}"
                        },
                        showBackButton = showBackButton,
                        isSearchActive = isSearchExpanded || localSearchQuery.isNotBlank(),
                        hazeState = hazeState,
                        onBackClick = handleBack,
                        onSearchToggleClick = {
                            val expanding = !isSearchExpanded
                            isSearchExpanded = expanding
                            if (expanding) {
                                onSearchClicked()
                            } else if (localSearchQuery.isNotEmpty()) {
                                localSearchQuery = ""
                                onSearchQueryChange("")
                            }
                        }
                    )
                }

                item(key = "collapsible_search") {
                    AnimatedVisibility(
                        visible = isSearchExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        LaunchedEffect(isSearchExpanded) {
                            if (isSearchExpanded) {
                                runCatching { searchFocusRequester.requestFocus() }
                            }
                        }

                        HistorySearchBar(
                            query = localSearchQuery,
                            onQueryChange = { updated ->
                                localSearchQuery = updated
                                onSearchQueryChange(updated)
                            },
                            onClearOrCollapse = {
                                if (localSearchQuery.isNotEmpty()) {
                                    localSearchQuery = ""
                                    onSearchQueryChange("")
                                } else {
                                    isSearchExpanded = false
                                }
                            },
                            focusRequester = searchFocusRequester,
                            hazeState = hazeState
                        )
                    }
                }

                item(key = "scope_toggle") {
                    ScopeSegmentedToggle(
                        options = HISTORY_SCOPE_OPTIONS,
                        selectedOption = localScope,
                        hazeState = hazeState,
                        onOptionSelected = { scope ->
                            localScope = scope
                            onScopeSelected(scope)
                        }
                    )
                }

                item(key = "filter_chips") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val memberLabel = selectedMemberName?.let(::extractShortDisplayName) ?: "Thành viên"
                        HistoryFilterPill(
                            label = memberLabel,
                            icon = Icons.Rounded.PersonOutline,
                            isSelected = localMemberId != null,
                            showDropdownArrow = true,
                            hazeState = hazeState,
                            onClick = {
                                onFilterMemberClicked()
                                activePickerSheet = HistoryPickerSheet.MEMBER
                            }
                        )

                        val monthPillLabel = if (localMonthLabel != null && localMonthLabel != "Tất cả") {
                            localMonthLabel!!
                        } else {
                            "Tháng"
                        }
                        HistoryFilterPill(
                            label = monthPillLabel,
                            icon = Icons.Rounded.CalendarMonth,
                            isSelected = localMonthLabel != null && localMonthLabel != "Tất cả",
                            showDropdownArrow = true,
                            hazeState = hazeState,
                            onClick = {
                                onFilterMonthClicked()
                                activePickerSheet = HistoryPickerSheet.MONTH
                            }
                        )

                        HistoryFilterPill(
                            label = localCategoryLabel ?: "Danh mục",
                            icon = Icons.Rounded.Category,
                            isSelected = localCategoryLabel != null,
                            showDropdownArrow = true,
                            hazeState = hazeState,
                            onClick = {
                                onFilterCategoryClicked()
                                activePickerSheet = HistoryPickerSheet.CATEGORY
                            }
                        )

                        if (hasActiveFilter) {
                            ClearFilterChip(onClick = ::resetAllFilters)
                        }
                    }
                }

                item(key = "filter_status_banner") {
                    FilterStatusSummaryCard(
                        hasActiveFilter = hasActiveFilter,
                        visibleCount = visibleTransactionCount,
                        scope = localScope,
                        selectedMemberName = selectedMemberName,
                        selectedMonthLabel = localMonthLabel,
                        selectedCategoryLabel = localCategoryLabel,
                        searchQuery = localSearchQuery,
                        hazeState = hazeState,
                        onClearClick = ::resetAllFilters
                    )
                }

                if (filteredGroups.isEmpty()) {
                    item(key = "empty_filter_state") {
                        EmptyHistoryStateCard(
                            hasActiveFilter = hasActiveFilter,
                            scope = localScope,
                            hazeState = hazeState,
                            onClearFilters = ::resetAllFilters
                        )
                    }
                } else {
                    itemsIndexed(
                        items = filteredGroups,
                        key = { index, group -> "${index}_${group.dateHeader}" }
                    ) { _, group ->
                        TransactionDateGroupCard(
                            group = group,
                            hazeState = hazeState,
                            onTransactionClicked = onTransactionClicked
                        )
                    }

                    item(key = "touch_hint_footer") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TouchApp,
                                contentDescription = null,
                                tint = colors.textMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Chạm một giao dịch để xem chi tiết",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            if (showBottomBar) {
                HistoryLegacyGlassBottomBar(
                    selectedTab = selectedNavTab,
                    hazeState = hazeState,
                    onTabSelected = { tab ->
                        selectedNavTab = tab
                        onNavTabSelected(tab)
                    }
                )
            }
        }
    }

    activePickerSheet?.let { sheetType ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { activePickerSheet = null },
            sheetState = sheetState,
            shape = SpendlyShapeTokens.BottomSheet,
            containerColor = colors.surfaceElevated,
            contentColor = colors.textPrimary,
            dragHandle = {
                BottomSheetDefaults.DragHandle(color = colors.divider)
            }
        ) {
            when (sheetType) {
                HistoryPickerSheet.MEMBER -> {
                    FilterPickerSheetContent(
                        title = "Lọc theo thành viên",
                        subtitle = "Chọn thành viên đã trả hoặc nạp quỹ",
                        allOptionLabel = "Tất cả thành viên",
                        isAllSelected = localMemberId == null,
                        options = resolvedMembers,
                        selectedId = localMemberId,
                        onSelectAll = {
                            localMemberId = null
                            onMemberSelected(null)
                            activePickerSheet = null
                        },
                        onSelectOption = { memberId ->
                            localMemberId = memberId
                            onMemberSelected(memberId)
                            activePickerSheet = null
                        }
                    )
                }

                HistoryPickerSheet.MONTH -> {
                    val monthOptions = resolvedMonths.map { it to it }
                    FilterPickerSheetContent(
                        title = "Lọc theo tháng",
                        subtitle = "Xem giao dịch theo mốc thời gian",
                        allOptionLabel = null,
                        isAllSelected = localMonthLabel == null,
                        options = monthOptions,
                        selectedId = localMonthLabel ?: monthText,
                        onSelectAll = {
                            localMonthLabel = null
                            onMonthSelected(null)
                            activePickerSheet = null
                        },
                        onSelectOption = { month ->
                            val normalized = if (month == "Tất cả") null else month
                            localMonthLabel = normalized
                            onMonthSelected(normalized)
                            activePickerSheet = null
                        }
                    )
                }

                HistoryPickerSheet.CATEGORY -> {
                    val categoryOptions = resolvedCategories.map { it to it }
                    FilterPickerSheetContent(
                        title = "Lọc theo danh mục",
                        subtitle = "Chọn loại khoản thu hoặc chi tiêu",
                        allOptionLabel = "Tất cả danh mục",
                        isAllSelected = localCategoryLabel == null,
                        options = categoryOptions,
                        selectedId = localCategoryLabel,
                        onSelectAll = {
                            localCategoryLabel = null
                            onCategorySelected(null)
                            activePickerSheet = null
                        },
                        onSelectOption = { category ->
                            localCategoryLabel = category
                            onCategorySelected(category)
                            activePickerSheet = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryTopBar(
    title: String,
    subtitle: String,
    showBackButton: Boolean,
    isSearchActive: Boolean,
    hazeState: HazeState,
    onBackClick: () -> Unit,
    onSearchToggleClick: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (showBackButton) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .spendlyGlassSurface(
                        hazeState = hazeState,
                        shape = CircleShape,
                        tintColor = colors.glassContainer
                    )
                    .clickable(role = Role.Button, onClick = onBackClick),
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
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
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
                .size(42.dp)
                .spendlyGlassSurface(
                    hazeState = hazeState,
                    shape = CircleShape,
                    tintColor = if (isSearchActive) colors.lavenderContainer else colors.glassContainer
                )
                .clickable(role = Role.Button, onClick = onSearchToggleClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSearchActive) Icons.Rounded.Close else Icons.Rounded.Search,
                contentDescription = if (isSearchActive) "Đóng tìm kiếm" else "Tìm kiếm giao dịch",
                tint = if (isSearchActive) colors.deepLavender else colors.textPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun HistorySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearOrCollapse: () -> Unit,
    focusRequester: FocusRequester,
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
                tint = colors.deepLavender,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "Tìm giao dịch, thành viên, danh mục...",
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            }

            IconButton(
                onClick = onClearOrCollapse,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = if (query.isNotEmpty()) "Xóa từ khóa" else "Ẩn thanh tìm kiếm",
                    tint = colors.textMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ScopeSegmentedToggle(
    options: List<String>,
    selectedOption: String,
    hazeState: HazeState,
    onOptionSelected: (String) -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val activeBrush = Brush.horizontalGradient(
        colors = listOf(SpendlyLavender, SpendlyIndigo)
    )

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        shape = SpendlyShapeTokens.Pill,
        contentPadding = PaddingValues(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { option ->
                val isSelected = option.equals(selectedOption, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(SpendlyShapeTokens.Pill)
                        .then(
                            if (isSelected) {
                                Modifier.background(brush = activeBrush, shape = SpendlyShapeTokens.Pill)
                            } else {
                                Modifier.background(Color.Transparent)
                            }
                        )
                        .clickable(role = Role.Tab) { onOptionSelected(option) }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = if (isSelected) Color.White else colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryFilterPill(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    showDropdownArrow: Boolean,
    hazeState: HazeState,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val tintColor = if (isSelected) {
        colors.lavenderContainer
    } else {
        colors.glassContainer
    }
    val contentColor = if (isSelected) colors.deepLavender else colors.textPrimary
    val borderBrush = if (isSelected) {
        Brush.linearGradient(
            listOf(
                colors.primaryLavender.copy(alpha = 0.85f),
                colors.indigo.copy(alpha = 0.65f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                colors.glassBorderStart,
                colors.glassBorderEnd
            )
        )
    }

    Row(
        modifier = Modifier
            .height(38.dp)
            .spendlyGlassSurface(
                hazeState = hazeState,
                shape = SpendlyShapeTokens.Pill,
                tintColor = tintColor,
                borderBrush = borderBrush
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 12.5.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (showDropdownArrow) {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ClearFilterChip(onClick: () -> Unit) {
    val colors = MaterialTheme.spendlyColors

    Row(
        modifier = Modifier
            .height(38.dp)
            .clip(SpendlyShapeTokens.Pill)
            .background(colors.coralContainer)
            .border(
                width = 1.dp,
                color = colors.coral.copy(alpha = 0.65f),
                shape = SpendlyShapeTokens.Pill
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.FilterAltOff,
            contentDescription = null,
            tint = colors.coralDeep,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = "Xóa bộ lọc",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.coralDeep,
            maxLines = 1
        )
    }
}

@Composable
private fun FilterStatusSummaryCard(
    hasActiveFilter: Boolean,
    visibleCount: Int,
    scope: String,
    selectedMemberName: String?,
    selectedMonthLabel: String?,
    selectedCategoryLabel: String?,
    searchQuery: String,
    hazeState: HazeState,
    onClearClick: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val activeSummary = remember(
        hasActiveFilter,
        visibleCount,
        scope,
        selectedMemberName,
        selectedMonthLabel,
        selectedCategoryLabel,
        searchQuery
    ) {
        if (!hasActiveFilter) {
            "Hiển thị tất cả giao dịch ($visibleCount)"
        } else {
            val parts = buildList {
                if (scope != "Nhóm") add(scope)
                if (!selectedMemberName.isNullOrBlank()) add(extractShortDisplayName(selectedMemberName))
                if (!selectedMonthLabel.isNullOrBlank() && selectedMonthLabel != "Tất cả") add(selectedMonthLabel)
                if (!selectedCategoryLabel.isNullOrBlank()) add(selectedCategoryLabel)
                if (searchQuery.isNotBlank()) add("\"${searchQuery.trim()}\"")
            }
            "Bộ lọc: ${parts.joinToString(" · ")} ($visibleCount)"
        }
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        shape = SpendlyShapeTokens.CardSmall,
        tintColor = if (hasActiveFilter) {
            colors.lavenderContainer.copy(alpha = if (colors.isDark) 0.55f else 0.72f)
        } else {
            colors.mintContainer.copy(alpha = if (colors.isDark) 0.45f else 0.55f)
        },
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = activeSummary,
                style = MaterialTheme.typography.labelMedium,
                color = if (hasActiveFilter) colors.onLavenderContainer else colors.mintDeep,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (hasActiveFilter) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Đặt lại",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.deepLavender,
                    modifier = Modifier
                        .clip(SpendlyShapeTokens.Pill)
                        .clickable(role = Role.Button, onClick = onClearClick)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun TransactionDateGroupCard(
    group: TransactionDateGroup,
    hazeState: HazeState,
    onTransactionClicked: (HistoryTransactionItem) -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val isNetPositive = group.netTotalFormatted.startsWith("+")
    val isNetZero = group.netTotalFormatted.startsWith("0")
    val netBg = when {
        isNetPositive -> colors.mintContainer
        isNetZero -> colors.lavenderContainer
        else -> colors.coralContainer
    }
    val netFg = when {
        isNetPositive -> colors.mintDeep
        isNetZero -> colors.onLavenderContainer
        else -> colors.coralDeep
    }

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
            Text(
                text = group.dateHeader,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )

            Box(
                modifier = Modifier
                    .clip(SpendlyShapeTokens.Pill)
                    .background(netBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = group.netTotalFormatted,
                    style = SpendlyTextStyles.moneyCaption.copy(fontWeight = FontWeight.SemiBold),
                    color = netFg
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        group.transactions.forEachIndexed { index, item ->
            if (index > 0) {
                HorizontalDivider(
                    color = colors.divider.copy(alpha = 0.55f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            HistoryTransactionRow(
                item = item,
                onClick = { onTransactionClicked(item) }
            )
        }
    }
}

@Composable
private fun HistoryTransactionRow(
    item: HistoryTransactionItem,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val badgeBg = if (item.isIncome) colors.mintContainer else colors.coralContainer
    val badgeFg = if (item.isIncome) colors.mintDeep else colors.coralDeep
    val amountColor = if (item.isIncome) colors.mintDeep else colors.coralDeep
    val iconVector = when (item.type) {
        HistoryItemType.GROCERY -> Icons.Rounded.ShoppingBag
        HistoryItemType.INTERNET -> Icons.Rounded.Wifi
        HistoryItemType.DEPOSIT -> Icons.Rounded.Savings
        HistoryItemType.COFFEE -> Icons.Rounded.LocalCafe
        HistoryItemType.TAXI -> Icons.Rounded.DirectionsCar
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SpendlyShapeTokens.CardSmall)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(SpendlyShapeTokens.CardSmall)
                .background(badgeBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = badgeFg,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = item.amountFormatted,
                style = SpendlyTextStyles.moneySmall.copy(fontWeight = FontWeight.Bold),
                color = amountColor,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (item.isIncome) "Thu" else "Chi",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = badgeFg
            )
        }
    }
}

@Composable
private fun EmptyHistoryStateCard(
    hasActiveFilter: Boolean,
    scope: String,
    hazeState: HazeState,
    onClearFilters: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(colors.lavenderContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.SearchOff,
                    contentDescription = null,
                    tint = colors.deepLavender,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = if (scope == "Cá nhân" && hasActiveFilter) {
                    "Chưa có giao dịch cá nhân phù hợp"
                } else {
                    "Không tìm thấy giao dịch nào"
                },
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (hasActiveFilter) {
                    "Thử thay đổi bộ lọc thành viên, danh mục, tháng hoặc từ khóa tìm kiếm."
                } else {
                    "Các khoản thu và chi tiêu mới của nhóm sẽ tự động hiển thị tại đây."
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
                textAlign = TextAlign.Center
            )

            if (hasActiveFilter) {
                Spacer(modifier = Modifier.height(4.dp))
                SpendlyButton(
                    text = "Xóa toàn bộ bộ lọc",
                    onClick = onClearFilters,
                    style = SpendlyButtonStyle.OUTLINED,
                    leadingIcon = Icons.Rounded.FilterAltOff,
                    height = 44.dp
                )
            }
        }
    }
}

@Composable
private fun FilterPickerSheetContent(
    title: String,
    subtitle: String,
    allOptionLabel: String?,
    isAllSelected: Boolean,
    options: List<Pair<String, String>>,
    selectedId: String?,
    onSelectAll: () -> Unit,
    onSelectOption: (String) -> Unit
) {
    val colors = MaterialTheme.spendlyColors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
            }
            if (allOptionLabel != null && !isAllSelected) {
                TextButton(onClick = onSelectAll) {
                    Text(
                        text = "Đặt lại",
                        color = colors.deepLavender
                    )
                }
            }
        }

        if (allOptionLabel != null) {
            PickerOptionRow(
                label = allOptionLabel,
                isSelected = isAllSelected,
                onClick = onSelectAll
            )
        }

        if (options.isEmpty()) {
            Text(
                text = "Không có dữ liệu tùy chọn khả dụng.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            options.forEach { (optionId, optionLabel) ->
                PickerOptionRow(
                    label = optionLabel,
                    isSelected = !isAllSelected && selectedId == optionId,
                    onClick = { onSelectOption(optionId) }
                )
            }
        }
    }
}

@Composable
private fun PickerOptionRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val bgColor = if (isSelected) colors.lavenderContainer else colors.surface
    val borderColor = if (isSelected) colors.primaryLavender else colors.divider

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SpendlyShapeTokens.CardMedium)
            .background(bgColor)
            .border(width = 1.dp, color = borderColor, shape = SpendlyShapeTokens.CardMedium)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (isSelected) colors.deepLavender else colors.textPrimary
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(colors.deepLavender),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Đã chọn",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun HistoryLegacyGlassBottomBar(
    selectedTab: BottomNavTab,
    hazeState: HazeState,
    onTabSelected: (BottomNavTab) -> Unit
) {
    val colors = MaterialTheme.spendlyColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .height(64.dp)
            .spendlyGlassSurface(
                hazeState = hazeState,
                shape = SpendlyShapeTokens.BottomBar,
                tintColor = colors.glassContainer
            )
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val tabs = listOf(
            Triple(BottomNavTab.HOME, "Trang chủ", Icons.Rounded.Home),
            Triple(BottomNavTab.HISTORY, "Lịch sử", Icons.Rounded.History),
            Triple(BottomNavTab.REPORTS, "Báo cáo", Icons.Rounded.BarChart),
            Triple(BottomNavTab.PROFILE, "Cá nhân", Icons.Rounded.Person)
        )
        tabs.forEach { (tab, label, icon) ->
            val selected = tab == selectedTab
            val tint = if (selected) colors.deepLavender else colors.textMuted
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(SpendlyShapeTokens.CardMedium)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Tab
                    ) { onTabSelected(tab) }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(SpendlyShapeTokens.Pill)
                        .background(if (selected) colors.lavenderContainer else Color.Transparent)
                        .padding(horizontal = 12.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    color = tint,
                    maxLines = 1
                )
            }
        }
    }
}

private fun filterTransactionGroups(
    groups: List<TransactionDateGroup>,
    query: String,
    scope: String,
    selectedMemberName: String?,
    selectedCategory: String?,
    selectedMonth: String?,
    currentMonthText: String
): List<TransactionDateGroup> {
    val trimmedQuery = query.trim()

    return groups.mapNotNull { group ->
        if (!matchesMonthHeader(group.dateHeader, selectedMonth, currentMonthText)) {
            return@mapNotNull null
        }

        val matchingItems = group.transactions.filter { item ->
            val matchQuery = trimmedQuery.isEmpty() ||
                item.title.contains(trimmedQuery, ignoreCase = true) ||
                item.subtitle.contains(trimmedQuery, ignoreCase = true) ||
                item.amountFormatted.contains(trimmedQuery, ignoreCase = true)

            val matchMember = selectedMemberName.isNullOrBlank() ||
                matchesMemberItem(item, selectedMemberName)

            val matchCategory = selectedCategory.isNullOrBlank() ||
                matchesCategoryItem(item, selectedCategory)

            val matchScope = matchesScopeItem(item, scope)

            matchQuery && matchMember && matchCategory && matchScope
        }

        if (matchingItems.isEmpty()) {
            null
        } else if (matchingItems.size == group.transactions.size) {
            group
        } else {
            val filteredNet = matchingItems.sumOf { parseSignedVnd(it.amountFormatted, it.isIncome) }
            val prefix = if (filteredNet > 0L) "+" else if (filteredNet < 0L) "-" else ""
            TransactionDateGroup(
                dateHeader = group.dateHeader,
                netTotalFormatted = "$prefix${FintechAlgorithm.formatVnd(abs(filteredNet))}",
                transactions = matchingItems
            )
        }
    }
}

private fun matchesMonthHeader(
    dateHeader: String,
    selectedMonth: String?,
    currentMonthText: String
): Boolean {
    if (selectedMonth.isNullOrBlank() || selectedMonth.equals("Tất cả", ignoreCase = true)) {
        return true
    }
    if (dateHeader.equals("Hôm nay", ignoreCase = true) || dateHeader.equals("Hôm qua", ignoreCase = true)) {
        return selectedMonth.equals(currentMonthText, ignoreCase = true) ||
            extractMonthToken(selectedMonth) == extractMonthToken(currentMonthText)
    }
    val monthYearToken = selectedMonth.removePrefix("Tháng ").trim()
    val monthOnlyToken = extractMonthToken(selectedMonth)
    return dateHeader.contains(monthYearToken, ignoreCase = true) ||
        (monthOnlyToken.isNotBlank() && dateHeader.contains("/$monthOnlyToken/", ignoreCase = true)) ||
        (monthOnlyToken.isNotBlank() && dateHeader.endsWith("/$monthOnlyToken", ignoreCase = true))
}

private fun extractMonthToken(monthLabel: String): String {
    val cleaned = monthLabel.removePrefix("Tháng ").trim()
    return cleaned.substringBefore("/").padStart(2, '0')
}

private fun matchesMemberItem(item: HistoryTransactionItem, memberName: String): Boolean {
    if (item.subtitle.contains(memberName, ignoreCase = true) ||
        item.title.contains(memberName, ignoreCase = true)
    ) {
        return true
    }
    val shortName = extractShortDisplayName(memberName)
    return shortName.length >= 2 && item.subtitle.contains(shortName, ignoreCase = true)
}

private fun matchesCategoryItem(item: HistoryTransactionItem, category: String): Boolean {
    if (item.subtitle.contains(category, ignoreCase = true) ||
        item.title.contains(category, ignoreCase = true)
    ) {
        return true
    }
    return when (category.lowercase()) {
        "ăn uống", "cà phê" -> !item.isIncome && (item.type == HistoryItemType.COFFEE || item.type == HistoryItemType.GROCERY)
        "di chuyển", "đi lại" -> !item.isIncome && item.type == HistoryItemType.TAXI
        "nhà ở", "sinh hoạt", "internet" -> !item.isIncome && item.type == HistoryItemType.INTERNET
        "mua sắm", "giải trí", "y tế", "giáo dục", "khác" -> !item.isIncome && item.type == HistoryItemType.GROCERY
        "thu quỹ", "nạp quỹ", "khoản thu" -> item.isIncome || item.type == HistoryItemType.DEPOSIT
        else -> false
    }
}

private fun matchesScopeItem(item: HistoryTransactionItem, scope: String): Boolean {
    return when (scope) {
        "Cá nhân" -> item.subtitle.contains("Cá nhân", ignoreCase = true) ||
            item.subtitle.contains("Riêng", ignoreCase = true) ||
            item.title.contains("Cá nhân", ignoreCase = true)
        "Nhóm" -> !item.subtitle.contains("Cá nhân", ignoreCase = true)
        else -> true
    }
}

private fun extractMembersFromGroups(groups: List<TransactionDateGroup>): List<Pair<String, String>> {
    val nonMemberTokens = setOf(
        "hôm nay", "hôm qua", "ăn uống", "di chuyển", "đi lại", "nhà ở",
        "mua sắm", "giải trí", "y tế", "giáo dục", "sinh hoạt", "thu quỹ", "khác"
    )
    val seen = linkedSetOf<String>()
    groups.forEach { group ->
        group.transactions.forEach { item ->
            val segments = item.subtitle.split("·").map { it.trim() }
            for (segment in segments) {
                val cleaned = segment
                    .removeSuffix("đã trả")
                    .removeSuffix("nạp quỹ")
                    .trim()
                val looksLikeDateOrTime = cleaned.matches(Regex("""\d{1,2}[:/]\d{1,2}(/\d{2,4})?"""))
                if (cleaned.isNotBlank() &&
                    !looksLikeDateOrTime &&
                    cleaned.lowercase() !in nonMemberTokens
                ) {
                    seen.add(cleaned)
                    break
                }
            }
        }
    }
    return seen.map { it to it }
}

private fun buildResolvedMonthOptions(
    availableMonths: List<String>,
    monthText: String
): List<String> {
    val result = mutableListOf<String>()
    if (monthText.isNotBlank() && monthText !in availableMonths) {
        result.add(monthText)
    }
    availableMonths.forEach { option ->
        if (option !in result) {
            result.add(option)
        }
    }
    return result
}

private fun extractShortDisplayName(fullName: String): String {
    return fullName.trim().split(Regex("\\s+")).lastOrNull().orEmpty().ifBlank { fullName }
}

private fun parseSignedVnd(formatted: String, isIncome: Boolean): Long {
    val digits = formatted.filter { it.isDigit() }
    val magnitude = digits.toLongOrNull() ?: 0L
    return if (isIncome) magnitude else -magnitude
}

private fun buildDefaultHistoryGroups(): List<TransactionDateGroup> {
    return listOf(
        TransactionDateGroup(
            dateHeader = "Hôm nay",
            netTotalFormatted = "-850.000 đ",
            transactions = listOf(
                HistoryTransactionItem(
                    id = "1",
                    title = "Mua linh kiện & module đồ án",
                    subtitle = "Lê Văn Hùng · Khác · 09:30",
                    amountFormatted = "-850.000 đ",
                    isIncome = false,
                    type = HistoryItemType.GROCERY
                )
            )
        ),
        TransactionDateGroup(
            dateHeader = "Hôm qua",
            netTotalFormatted = "-330.000 đ",
            transactions = listOf(
                HistoryTransactionItem(
                    id = "2",
                    title = "Tiền phòng thảo luận & Internet",
                    subtitle = "Lý Việt Linh · Nhà ở · 14:15",
                    amountFormatted = "-330.000 đ",
                    isIncome = false,
                    type = HistoryItemType.INTERNET
                )
            )
        ),
        TransactionDateGroup(
            dateHeader = "01/11/2026",
            netTotalFormatted = "+5.000.000 đ",
            transactions = listOf(
                HistoryTransactionItem(
                    id = "3",
                    title = "Đóng quỹ dự án đợt 1",
                    subtitle = "Lê Văn Hùng · Thu quỹ · 10:00",
                    amountFormatted = "+5.000.000 đ",
                    isIncome = true,
                    type = HistoryItemType.DEPOSIT
                )
            )
        ),
        TransactionDateGroup(
            dateHeader = "28/10/2026",
            netTotalFormatted = "-145.000 đ",
            transactions = listOf(
                HistoryTransactionItem(
                    id = "4",
                    title = "Taxi đi in ấn tài liệu đồ án",
                    subtitle = "Nguyễn Phan Tấn Đạt · Đi lại · 16:30",
                    amountFormatted = "-145.000 đ",
                    isIncome = false,
                    type = HistoryItemType.TAXI
                )
            )
        ),
        TransactionDateGroup(
            dateHeader = "25/10/2026",
            netTotalFormatted = "-185.000 đ",
            transactions = listOf(
                HistoryTransactionItem(
                    id = "5",
                    title = "Cà phê & nước họp nhóm đồ án",
                    subtitle = "Lê Trung Bình · Ăn uống · 15:20",
                    amountFormatted = "-185.000 đ",
                    isIncome = false,
                    type = HistoryItemType.COFFEE
                )
            )
        ),
        TransactionDateGroup(
            dateHeader = "24/10/2026",
            netTotalFormatted = "+5.000.000 đ",
            transactions = listOf(
                HistoryTransactionItem(
                    id = "6",
                    title = "Đóng quỹ dự án đợt 1",
                    subtitle = "Lý Việt Linh · Thu quỹ · 09:15",
                    amountFormatted = "+5.000.000 đ",
                    isIncome = true,
                    type = HistoryItemType.DEPOSIT
                )
            )
        )
    )
}

// ====================== PREVIEW ======================

@Preview(
    name = "13 · Lịch sử giao dịch (Spendly Light)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun TransactionHistoryScreenPreview() {
    Nhoms8Theme {
        TransactionHistoryScreen(
            availableMembers = listOf(
                "1" to "Lê Văn Hùng",
                "2" to "Lý Việt Linh",
                "3" to "Nguyễn Phan Tấn Đạt",
                "4" to "Lê Trung Bình"
            )
        )
    }
}

@Preview(
    name = "13b · Lịch sử giao dịch (Dark + Filtered)",
    showBackground = true,
    widthDp = 412,
    heightDp = 860
)
@Composable
private fun TransactionHistoryDarkFilteredPreview() {
    SpendlyTheme(darkTheme = true) {
        TransactionHistoryScreen(
            searchQuery = "đồ án",
            selectedCategoryLabel = "Ăn uống",
            showBottomBar = true
        )
    }
}
