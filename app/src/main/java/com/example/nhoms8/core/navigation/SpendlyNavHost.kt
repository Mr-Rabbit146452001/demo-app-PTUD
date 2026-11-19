package com.example.nhoms8.core.navigation

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.nhoms8.BottomNavTab
import com.example.nhoms8.ChatbotScreen
import com.example.nhoms8.DebtItem
import com.example.nhoms8.EditMemberScreen
import com.example.nhoms8.ExpenseCategory
import com.example.nhoms8.FinancialReportScreen
import com.example.nhoms8.GroupMember
import com.example.nhoms8.HistoryItemType
import com.example.nhoms8.HistoryTransactionItem
import com.example.nhoms8.HomeScreen
import com.example.nhoms8.LoginScreen
import com.example.nhoms8.MemberBalanceStatus
import com.example.nhoms8.MembersScreen
import com.example.nhoms8.OnboardingScreen1
import com.example.nhoms8.OnboardingScreen2
import com.example.nhoms8.OnboardingScreen3
import com.example.nhoms8.ProfileScreen
import com.example.nhoms8.RecentTransaction
import com.example.nhoms8.SettlementScreen
import com.example.nhoms8.SignUpScreen
import com.example.nhoms8.TransactionDateGroup
import com.example.nhoms8.TransactionHistoryScreen
import com.example.nhoms8.TransactionIconType
import com.example.nhoms8.WelcomeScreen
import com.example.nhoms8.algorithm.FintechAlgorithm
import com.example.nhoms8.core.designsystem.SpendlyBottomBar
import com.example.nhoms8.core.designsystem.SpendlyTab
import com.example.nhoms8.data.SpendlyServiceLocator
import com.example.nhoms8.domain.model.GroupRole
import com.example.nhoms8.domain.model.RecordStatus
import com.example.nhoms8.domain.model.SpendlyCategory
import com.example.nhoms8.domain.model.SpendlyUser
import com.example.nhoms8.feature.auth.ForgotPasswordScreen
import com.example.nhoms8.feature.groups.CreateGroupScreen
import com.example.nhoms8.feature.groups.GroupDetailScreen
import com.example.nhoms8.feature.groups.GroupDetailUiState
import com.example.nhoms8.feature.groups.GroupListUiState
import com.example.nhoms8.feature.groups.GroupListScreen
import com.example.nhoms8.feature.groups.GroupViewModel
import com.example.nhoms8.feature.groups.JoinGroupScreen
import com.example.nhoms8.feature.notifications.NotificationsScreen
import com.example.nhoms8.feature.transactions.AddEditExpenseScreen
import com.example.nhoms8.feature.transactions.AddIncomeScreen
import com.example.nhoms8.feature.transactions.AddTransactionModalSheet
import com.example.nhoms8.feature.transactions.ExpenseDetailScreen
import com.example.nhoms8.feature.transactions.SplitBillV2Screen
import com.example.nhoms8.feature.transactions.TransactionViewModel
import com.example.nhoms8.model.CategoryReportItem
import com.example.nhoms8.model.MemberReportItem
import com.example.nhoms8.ui.theme.SpendlyAmber
import com.example.nhoms8.ui.theme.SpendlyCoralDeep
import com.example.nhoms8.ui.theme.SpendlyIndigo
import com.example.nhoms8.ui.theme.SpendlyLavender
import com.example.nhoms8.ui.theme.SpendlyLavenderDeep
import com.example.nhoms8.ui.theme.SpendlyMint
import com.example.nhoms8.ui.theme.SpendlyMintDeep
import com.example.nhoms8.ui.theme.SpendlySky
import com.example.nhoms8.viewmodel.SpendTogetherViewModel
import com.google.firebase.auth.FirebaseUser
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Trung tâm điều hướng Navigation Compose của Spendly kết nối Onboarding, Auth,
 * Hub 5 tab kính mờ Haze 2.1.0, Đa quỹ nhóm, Sổ cái Thu/Chi/Chia tiền V2 và FinSprout AI.
 */
@Composable
fun SpendlyNavHost(
    modifier: Modifier = Modifier,
    spendTogetherViewModel: SpendTogetherViewModel = viewModel(),
    groupViewModel: GroupViewModel = viewModel(
        factory = GroupViewModel.factory(
            userRepository = SpendlyServiceLocator.userRepository,
            groupRepository = SpendlyServiceLocator.groupRepository,
            transactionRepository = SpendlyServiceLocator.transactionRepository,
            autoBindDefaultUser = SpendlyServiceLocator.authRepository.isUserLoggedIn()
        )
    ),
    transactionViewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModel.factory(
            groupRepository = SpendlyServiceLocator.groupRepository,
            transactionRepository = SpendlyServiceLocator.transactionRepository
        )
    ),
    navController: NavHostController = rememberNavController(),
    startDestination: String = SpendlyRoutes.HOME
) {
    val context = LocalContext.current
    val spendTogetherUiState by spendTogetherViewModel.uiState.collectAsStateWithLifecycle()
    val groupsUiState by groupViewModel.groupsUiState.collectAsStateWithLifecycle()
    val currentUser by groupViewModel.currentUser.collectAsStateWithLifecycle()
    val notifications by groupViewModel.notifications.collectAsStateWithLifecycle()

    val emptyGroupDetailFlow = remember { MutableStateFlow(GroupDetailUiState(isLoading = false)) }
    val activeGroupId = groupsUiState.activeGroupId
    val activeGroupDetail by remember(activeGroupId, groupViewModel) {
        val validId = activeGroupId?.takeIf { it.isNotBlank() }
        if (validId != null) groupViewModel.observeGroupDetail(validId) else emptyGroupDetailFlow
    }.collectAsStateWithLifecycle()

    val bottomBarHazeState = rememberHazeState()
    var showAddSheet by remember { mutableStateOf(false) }

    // Kết nối SpendTogetherViewModel với repository Firestore dùng chung & đồng bộ phiên Firebase hiện tại
    LaunchedEffect(Unit) {
        spendTogetherViewModel.bindToSpendlyRepository(
            spendlyRepo = SpendlyServiceLocator.spendlyRepository,
            activeGroupIdFlow = SpendlyServiceLocator.activeGroupId
        )
        SpendlyServiceLocator.authRepository.getCurrentUser()?.let { fbUser ->
            syncSignedInUser(
                fbUser = fbUser,
                fallbackEmailOrPhone = fbUser.email.orEmpty(),
                fallbackName = null,
                spendTogetherViewModel = spendTogetherViewModel,
                groupViewModel = groupViewModel
            )
        }
    }

    // Đồng bộ activeGroupId từ GroupViewModel -> ServiceLocator & SpendTogetherViewModel
    LaunchedEffect(groupsUiState.activeGroupId) {
        val selectedGroupId = groupsUiState.activeGroupId
        SpendlyServiceLocator.setActiveGroup(selectedGroupId)
        spendTogetherViewModel.setActiveGroupId(selectedGroupId)
    }

    // Đồng bộ số thông báo chưa đọc theo UID hiện tại
    LaunchedEffect(currentUser.uid) {
        if (currentUser.uid.isNotBlank()) {
            spendTogetherViewModel.observeUnreadNotificationsCount(currentUser.uid)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val selectedTopLevelTab = SpendlyRoutes.tabForRoute(currentRoute)
    val unreadNotificationsCount = notifications.count { !it.isRead }
        .coerceAtLeast(spendTogetherUiState.unreadNotificationsCount)

    fun navigateToTopLevelTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateClearingAuthToHome() {
        navController.navigate(SpendlyRoutes.HOME) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun resolveOrPromptActiveGroupId(): String? {
        val targetGroupId = resolveEffectiveGroupId(groupsUiState)
        if (targetGroupId.isNullOrBlank()) {
            Toast.makeText(context, "Vui lòng tạo hoặc chọn một quỹ nhóm trước", Toast.LENGTH_SHORT).show()
            navigateToTopLevelTab(SpendlyRoutes.GROUPS)
            return null
        }
        activateGroupEverywhere(targetGroupId, groupViewModel, spendTogetherViewModel)
        return targetGroupId
    }

    fun startExpenseFlowForActiveGroup() {
        val targetGroupId = resolveOrPromptActiveGroupId() ?: return
        transactionViewModel.startNewExpense(targetGroupId, currentUser.uid)
        navController.navigate(SpendlyRoutes.ADD_EDIT_EXPENSE)
    }

    fun startIncomeFlowForActiveGroup() {
        val targetGroupId = resolveOrPromptActiveGroupId() ?: return
        navController.navigate(SpendlyRoutes.addIncome(targetGroupId))
    }

    fun handleDemoLogin(displayName: String, toastMessage: String) {
        val demoPhone = "0912 345 678"
        val demoUid = GroupViewModel.DEFAULT_OFFLINE_USER.uid
        spendTogetherViewModel.onLogin(demoPhone, displayName)
        spendTogetherViewModel.observeUnreadNotificationsCount(demoUid)
        groupViewModel.bindUser(
            SpendlyUser(
                uid = demoUid,
                fullName = displayName,
                email = "hung.le@spendly.vn",
                phone = demoPhone,
                isActive = true
            )
        )
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
        navigateClearingAuthToHome()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = bottomBarHazeState)
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                enterTransition = { fadeIn(animationSpec = tween(260)) },
                exitTransition = { fadeOut(animationSpec = tween(220)) },
                modifier = Modifier.fillMaxSize()
            ) {
                // ==================== ONBOARDING & AUTH ====================

                composable(SpendlyRoutes.WELCOME) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                navController.navigate(SpendlyRoutes.ONBOARDING_1)
                            }
                    ) {
                        WelcomeScreen()
                    }
                }

                composable(SpendlyRoutes.ONBOARDING_1) {
                    OnboardingScreen1(
                        onSkipClicked = {
                            navController.navigate(SpendlyRoutes.LOGIN) { launchSingleTop = true }
                        },
                        onContinueClicked = {
                            navController.navigate(SpendlyRoutes.ONBOARDING_2)
                        }
                    )
                }

                composable(SpendlyRoutes.ONBOARDING_2) {
                    OnboardingScreen2(
                        onSkipClicked = {
                            navController.navigate(SpendlyRoutes.LOGIN) { launchSingleTop = true }
                        },
                        onContinueClicked = {
                            navController.navigate(SpendlyRoutes.ONBOARDING_3)
                        }
                    )
                }

                composable(SpendlyRoutes.ONBOARDING_3) {
                    OnboardingScreen3(
                        onSkipClicked = {
                            navController.navigate(SpendlyRoutes.LOGIN) { launchSingleTop = true }
                        },
                        onGetStartedClicked = {
                            navController.navigate(SpendlyRoutes.LOGIN) { launchSingleTop = true }
                        }
                    )
                }

                composable(SpendlyRoutes.LOGIN) {
                    LoginScreen(
                        isLoading = spendTogetherUiState.authLoading,
                        errorMessage = spendTogetherUiState.authErrorMessage,
                        onLoginSubmit = { emailOrPhone, password ->
                            spendTogetherViewModel.signInWithFirebase(emailOrPhone, password) {
                                val fbUser = SpendlyServiceLocator.authRepository.getCurrentUser()
                                if (fbUser != null) {
                                    syncSignedInUser(
                                        fbUser = fbUser,
                                        fallbackEmailOrPhone = emailOrPhone,
                                        fallbackName = null,
                                        spendTogetherViewModel = spendTogetherViewModel,
                                        groupViewModel = groupViewModel
                                    )
                                } else {
                                    val profile = spendTogetherViewModel.uiState.value.userProfile
                                    groupViewModel.bindUser(
                                        SpendlyUser(
                                            uid = GroupViewModel.DEFAULT_OFFLINE_USER.uid,
                                            fullName = profile.name,
                                            email = emailOrPhone,
                                            phone = profile.phoneNumber,
                                            isActive = true
                                        )
                                    )
                                }
                                Toast.makeText(
                                    context,
                                    "Đăng nhập Firebase thành công!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navigateClearingAuthToHome()
                            }
                        },
                        onRegisterClicked = {
                            spendTogetherViewModel.clearAuthError()
                            navController.navigate(SpendlyRoutes.SIGN_UP)
                        },
                        onForgotPasswordClicked = { emailOrPhone ->
                            spendTogetherViewModel.clearAuthError()
                            navController.navigate(SpendlyRoutes.forgotPassword(emailOrPhone))
                        },
                        onGoogleLoginClicked = {
                            handleDemoLogin(
                                displayName = "Lê Văn Hùng (Google)",
                                toastMessage = "Đăng nhập với Google thành công"
                            )
                        },
                        onAppleLoginClicked = {
                            handleDemoLogin(
                                displayName = "Lê Văn Hùng (Apple)",
                                toastMessage = "Đăng nhập với Apple thành công"
                            )
                        }
                    )
                }

                composable(SpendlyRoutes.SIGN_UP) {
                    SignUpScreen(
                        isLoading = spendTogetherUiState.authLoading,
                        errorMessage = spendTogetherUiState.authErrorMessage,
                        onBackClicked = {
                            spendTogetherViewModel.clearAuthError()
                            navController.popBackStack()
                        },
                        onSignUpSubmit = { name, emailOrPhone, password ->
                            spendTogetherViewModel.signUpWithFirebase(name, emailOrPhone, password) {
                                val fbUser = SpendlyServiceLocator.authRepository.getCurrentUser()
                                if (fbUser != null) {
                                    syncSignedInUser(
                                        fbUser = fbUser,
                                        fallbackEmailOrPhone = emailOrPhone,
                                        fallbackName = name,
                                        spendTogetherViewModel = spendTogetherViewModel,
                                        groupViewModel = groupViewModel
                                    )
                                } else {
                                    groupViewModel.bindUser(
                                        SpendlyUser(
                                            uid = GroupViewModel.DEFAULT_OFFLINE_USER.uid,
                                            fullName = name.ifBlank { "Thành viên Spendly" },
                                            email = emailOrPhone,
                                            isActive = true
                                        )
                                    )
                                }
                                Toast.makeText(
                                    context,
                                    "Đăng ký tài khoản Firebase thành công!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navigateClearingAuthToHome()
                            }
                        },
                        onLoginClicked = {
                            spendTogetherViewModel.clearAuthError()
                            navController.popBackStack()
                        },
                        onPickAvatarClicked = {
                            Toast.makeText(
                                context,
                                "Chọn ảnh đại diện từ thư viện",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }

                composable(
                    route = SpendlyRoutes.FORGOT_PASSWORD,
                    arguments = listOf(
                        navArgument(SpendlyRoutes.ARG_EMAIL_OR_PHONE) {
                            type = NavType.StringType
                            defaultValue = ""
                            nullable = true
                        }
                    )
                ) { backStackEntry ->
                    val prefill = backStackEntry.arguments?.getString(SpendlyRoutes.ARG_EMAIL_OR_PHONE).orEmpty()
                    var isResetLoading by remember { mutableStateOf(false) }
                    var statusMessage by remember { mutableStateOf<String?>(null) }
                    var isResetError by remember { mutableStateOf(false) }

                    ForgotPasswordScreen(
                        initialEmailOrPhone = prefill,
                        isLoading = isResetLoading,
                        statusMessage = statusMessage,
                        isError = isResetError,
                        onSendResetClicked = { emailOrPhone ->
                            isResetLoading = true
                            statusMessage = null
                            isResetError = false
                            spendTogetherViewModel.sendPasswordReset(emailOrPhone) { msg ->
                                isResetLoading = false
                                statusMessage = msg
                                isResetError = !msg.startsWith("Đã gửi liên kết khôi phục")
                            }
                        },
                        onBackToLoginClicked = {
                            navController.popBackStack()
                        }
                    )
                }

                // ==================== TOP-LEVEL 5-TAB HUB ====================

                composable(SpendlyRoutes.HOME) {
                    val hasLiveGroup = !activeGroupId.isNullOrBlank() && activeGroupDetail.group != null
                    val displayUserName = currentUser.fullName.ifBlank { spendTogetherUiState.userProfile.name }
                    val displayUserInitials = computeInitials(displayUserName)
                    val displayGroupName = if (hasLiveGroup) {
                        activeGroupDetail.group?.name.orEmpty().ifBlank { spendTogetherUiState.activeGroupName }
                    } else {
                        spendTogetherUiState.activeGroupName
                    }
                    val displayBalance = if (hasLiveGroup) {
                        FintechAlgorithm.formatVnd(activeGroupDetail.groupBalance)
                    } else {
                        spendTogetherUiState.budget.totalBalanceFormatted
                    }
                    val displayDeposited = if (hasLiveGroup) {
                        FintechAlgorithm.formatVnd(activeGroupDetail.totalIncome)
                    } else {
                        spendTogetherUiState.budget.depositedAmountFormatted
                    }
                    val displaySpent = if (hasLiveGroup) {
                        FintechAlgorithm.formatVnd(activeGroupDetail.totalExpense)
                    } else {
                        spendTogetherUiState.budget.spentAmountFormatted
                    }
                    val displayRecentTx = if (hasLiveGroup) {
                        buildHomeRecentTransactions(activeGroupDetail)
                    } else {
                        spendTogetherUiState.recentTransactions
                    }

                    HomeScreen(
                        userName = displayUserName,
                        userInitials = displayUserInitials,
                        groupName = displayGroupName,
                        totalBalance = displayBalance,
                        monthLabel = currentMonthLabel(),
                        depositedAmount = displayDeposited,
                        spentAmount = displaySpent,
                        recentTransactions = displayRecentTx,
                        onNotificationClicked = {
                            navigateToTopLevelTab(SpendlyRoutes.NOTIFICATIONS)
                        },
                        onAddExpenseClicked = {
                            startExpenseFlowForActiveGroup()
                        },
                        onTopUpFundClicked = {
                            startIncomeFlowForActiveGroup()
                        },
                        onSplitBillClicked = {
                            startExpenseFlowForActiveGroup()
                        },
                        onMembersClicked = {
                            val targetGroupId = resolveEffectiveGroupId(groupsUiState)
                            if (targetGroupId.isNullOrBlank()) {
                                navigateToTopLevelTab(SpendlyRoutes.GROUPS)
                            } else {
                                navController.navigate(SpendlyRoutes.groupDetail(targetGroupId))
                            }
                        },
                        onViewAllTransactionsClicked = {
                            navController.navigate(SpendlyRoutes.HISTORY)
                        },
                        onTransactionClicked = { tx ->
                            val liveGroupId = activeGroupDetail.group?.groupId
                            val matchingExpense = if (hasLiveGroup && !liveGroupId.isNullOrBlank()) {
                                activeGroupDetail.expenses.firstOrNull { it.expenseId == tx.id }
                            } else {
                                null
                            }
                            if (matchingExpense != null && !liveGroupId.isNullOrBlank()) {
                                navController.navigate(SpendlyRoutes.expenseDetail(liveGroupId, matchingExpense.expenseId))
                            } else {
                                Toast.makeText(
                                    context,
                                    "Chi tiết: ${tx.title} (${tx.amountFormatted})",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onChatbotClicked = {
                            navController.navigate(SpendlyRoutes.CHATBOT)
                        },
                        showBottomBar = false,
                        onReportsClicked = {
                            navController.navigate(SpendlyRoutes.REPORTS)
                        }
                    )
                }

                composable(SpendlyRoutes.PROFILE) {
                    val displayUserName = currentUser.fullName.ifBlank { spendTogetherUiState.userProfile.name }
                    val displayInitials = computeInitials(displayUserName)
                    val contactInfo = currentUser.phone.ifBlank {
                        currentUser.email.ifBlank { spendTogetherUiState.userProfile.phoneNumber }
                    }
                    val currentMemberRole = activeGroupDetail.members
                        .firstOrNull { it.userId == currentUser.uid }
                        ?.role
                        ?.label
                        ?: if (spendTogetherUiState.userProfile.isLeader) "Trưởng nhóm" else "Thành viên"
                    val userSubtitle = "$contactInfo · $currentMemberRole"

                    val activeGroup = activeGroupDetail.group
                        ?: groupsUiState.groups.firstOrNull { it.groupId == activeGroupId }
                    val displayGroupName = activeGroup?.name ?: spendTogetherUiState.activeGroupName
                    val displayGroupSubtitle = when {
                        activeGroupDetail.group != null -> {
                            val count = activeGroupDetail.members.size
                                .coerceAtLeast(activeGroupDetail.group?.memberCount ?: 0)
                            "$count thành viên · ${FintechAlgorithm.formatVnd(activeGroupDetail.groupBalance)}"
                        }
                        activeGroup != null -> {
                            "${activeGroup.memberCount} thành viên · ${
                                FintechAlgorithm.formatVnd(groupsUiState.statsFor(activeGroup.groupId).balance)
                            }"
                        }
                        else -> {
                            "${spendTogetherUiState.members.size} thành viên · ${spendTogetherUiState.budget.totalBalanceFormatted}"
                        }
                    }

                    ProfileScreen(
                        userName = displayUserName,
                        userInitials = displayInitials,
                        userSubtitle = userSubtitle,
                        groupName = displayGroupName,
                        groupSubtitle = displayGroupSubtitle,
                        onQrCodeClicked = {
                            val invite = activeGroup?.inviteCode.orEmpty()
                            if (invite.isNotBlank() && activeGroup != null) {
                                Toast.makeText(
                                    context,
                                    "Mã mời quỹ ${activeGroup.name}: $invite",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(context, "Mã QR nhận tiền quỹ", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onEditProfileClicked = {
                            val matchedMember = spendTogetherUiState.members.find {
                                it.id == spendTogetherUiState.userProfile.id ||
                                    it.name.equals(displayUserName, ignoreCase = true)
                            } ?: spendTogetherUiState.members.firstOrNull()
                            matchedMember?.let { spendTogetherViewModel.onSelectMemberForEdit(it) }
                            navController.navigate(SpendlyRoutes.EDIT_MEMBER)
                        },
                        onChangeGroupClicked = {
                            navigateToTopLevelTab(SpendlyRoutes.GROUPS)
                        },
                        onThemeClicked = {
                            Toast.makeText(context, "Giao diện Spendly Lavender Pastel", Toast.LENGTH_SHORT).show()
                        },
                        onCurrencyClicked = {
                            Toast.makeText(context, "Đơn vị tiền tệ: Việt Nam Đồng (đ)", Toast.LENGTH_SHORT).show()
                        },
                        onSecurityClicked = {
                            Toast.makeText(context, "Cài đặt bảo mật & sinh trắc học", Toast.LENGTH_SHORT).show()
                        },
                        onHelpClicked = {
                            navController.navigate(SpendlyRoutes.WELCOME)
                        },
                        onLogoutClicked = {
                            spendTogetherViewModel.signOutFromFirebase()
                            SpendlyServiceLocator.setActiveGroup(null)
                            spendTogetherViewModel.setActiveGroupId(null)
                            groupViewModel.resetSession()
                            Toast.makeText(context, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show()
                            navController.navigate(SpendlyRoutes.LOGIN) {
                                popUpTo(navController.graph.id) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        showBottomBar = false
                    )
                }

                composable(SpendlyRoutes.GROUPS) {
                    GroupListScreen(
                        viewModel = groupViewModel,
                        onGroupClick = { groupId ->
                            navController.navigate(SpendlyRoutes.groupDetail(groupId))
                        },
                        onCreateGroupClick = {
                            if (currentUser.uid.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Vui lòng đăng nhập để đồng bộ quỹ trên Cloud Firestore",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigate(SpendlyRoutes.LOGIN)
                            } else {
                                groupViewModel.clearMessages()
                                navController.navigate(SpendlyRoutes.CREATE_GROUP)
                            }
                        },
                        onJoinGroupClick = {
                            if (currentUser.uid.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Vui lòng đăng nhập để tham gia quỹ bằng mã mời",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigate(SpendlyRoutes.LOGIN)
                            } else {
                                groupViewModel.clearMessages()
                                navController.navigate(SpendlyRoutes.JOIN_GROUP)
                            }
                        }
                    )
                }

                composable(SpendlyRoutes.NOTIFICATIONS) {
                    NotificationsScreen(
                        viewModel = groupViewModel,
                        onNavigateToGroup = { groupId ->
                            navController.navigate(SpendlyRoutes.groupDetail(groupId))
                        }
                    )
                }

                // ==================== MULTI-GROUP FLOWS ====================

                composable(SpendlyRoutes.CREATE_GROUP) {
                    CreateGroupScreen(
                        viewModel = groupViewModel,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onGroupCreated = { group ->
                            activateGroupEverywhere(group.groupId, groupViewModel, spendTogetherViewModel)
                            navController.navigate(SpendlyRoutes.groupDetail(group.groupId)) {
                                popUpTo(SpendlyRoutes.CREATE_GROUP) { inclusive = true }
                            }
                        }
                    )
                }

                composable(SpendlyRoutes.JOIN_GROUP) {
                    JoinGroupScreen(
                        viewModel = groupViewModel,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onJoinedGroup = { group ->
                            activateGroupEverywhere(group.groupId, groupViewModel, spendTogetherViewModel)
                            navController.navigate(SpendlyRoutes.groupDetail(group.groupId)) {
                                popUpTo(SpendlyRoutes.JOIN_GROUP) { inclusive = true }
                            }
                        }
                    )
                }

                composable(
                    route = SpendlyRoutes.GROUP_DETAIL,
                    arguments = listOf(
                        navArgument(SpendlyRoutes.ARG_GROUP_ID) { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val groupId = backStackEntry.arguments?.getString(SpendlyRoutes.ARG_GROUP_ID).orEmpty()
                    LaunchedEffect(groupId) {
                        if (groupId.isNotBlank()) {
                            activateGroupEverywhere(groupId, groupViewModel, spendTogetherViewModel)
                        }
                    }

                    GroupDetailScreen(
                        groupId = groupId,
                        viewModel = groupViewModel,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onAddExpenseClick = { gId ->
                            transactionViewModel.startNewExpense(gId, currentUser.uid)
                            navController.navigate(SpendlyRoutes.ADD_EDIT_EXPENSE)
                        },
                        onAddIncomeClick = { gId ->
                            navController.navigate(SpendlyRoutes.addIncome(gId))
                        },
                        onAiChatClick = { gId ->
                            activateGroupEverywhere(gId, groupViewModel, spendTogetherViewModel)
                            navController.navigate(SpendlyRoutes.CHATBOT)
                        },
                        onExpenseClicked = { expId ->
                            navController.navigate(SpendlyRoutes.expenseDetail(groupId, expId))
                        },
                        onOpenFullSettlement = {
                            navController.navigate(SpendlyRoutes.SETTLEMENT)
                        },
                        onOpenFullReport = {
                            navController.navigate(SpendlyRoutes.REPORTS)
                        },
                        onLeftGroup = {
                            navController.popBackStack()
                        }
                    )
                }

                // ==================== TRANSACTIONS & SPLIT BILL V2 ====================

                composable(SpendlyRoutes.ADD_EDIT_EXPENSE) {
                    AddEditExpenseScreen(
                        viewModel = transactionViewModel,
                        onContinueToSplit = {
                            navController.navigate(SpendlyRoutes.SPLIT_BILL_V2)
                        },
                        onBackClicked = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(SpendlyRoutes.SPLIT_BILL_V2) {
                    SplitBillV2Screen(
                        viewModel = transactionViewModel,
                        onBackClicked = {
                            navController.popBackStack()
                        },
                        onSavedSuccess = {
                            Toast.makeText(
                                context,
                                "Đã lưu khoản chi thành công!",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.popBackStack(SpendlyRoutes.ADD_EDIT_EXPENSE, inclusive = true)
                        }
                    )
                }

                composable(
                    route = SpendlyRoutes.ADD_INCOME,
                    arguments = listOf(
                        navArgument(SpendlyRoutes.ARG_GROUP_ID) { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val groupId = backStackEntry.arguments?.getString(SpendlyRoutes.ARG_GROUP_ID).orEmpty()
                    AddIncomeScreen(
                        viewModel = transactionViewModel,
                        groupId = groupId,
                        currentUserId = currentUser.uid,
                        onBackClicked = {
                            navController.popBackStack()
                        },
                        onSavedSuccess = {
                            Toast.makeText(
                                context,
                                "Đã ghi nhận khoản nạp quỹ thành công!",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = SpendlyRoutes.EXPENSE_DETAIL,
                    arguments = listOf(
                        navArgument(SpendlyRoutes.ARG_GROUP_ID) { type = NavType.StringType },
                        navArgument(SpendlyRoutes.ARG_EXPENSE_ID) { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val groupId = backStackEntry.arguments?.getString(SpendlyRoutes.ARG_GROUP_ID).orEmpty()
                    val expenseId = backStackEntry.arguments?.getString(SpendlyRoutes.ARG_EXPENSE_ID).orEmpty()

                    ExpenseDetailScreen(
                        viewModel = transactionViewModel,
                        groupId = groupId,
                        expenseId = expenseId,
                        onBackClicked = {
                            navController.popBackStack()
                        },
                        onEditExpenseClicked = { expId ->
                            transactionViewModel.loadExpenseForEdit(groupId, expId)
                            navController.navigate(SpendlyRoutes.ADD_EDIT_EXPENSE)
                        },
                        currentUserId = currentUser.uid,
                        currentUserName = currentUser.fullName
                    )
                }

                // ==================== FINSPROUT AI CHATBOT ====================

                composable(SpendlyRoutes.CHATBOT) {
                    ChatbotScreen(
                        messages = spendTogetherUiState.chatMessages,
                        isTyping = spendTogetherUiState.isChatbotTyping,
                        onBackClicked = {
                            navController.popBackStack()
                        },
                        onSendMessage = spendTogetherViewModel::sendMessageToChatbot,
                        onConfirmExpense = { messageId ->
                            spendTogetherViewModel.confirmChatExpense(messageId)
                            Toast.makeText(
                                context,
                                "Đã lưu khoản chi vào quỹ nhóm!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onClearChat = spendTogetherViewModel::clearChatHistory
                    )
                }

                // ==================== LEGACY & FULL-VIEW ROUTES ====================

                composable(SpendlyRoutes.HISTORY) {
                    val hasLiveGroup = !activeGroupId.isNullOrBlank() && activeGroupDetail.group != null
                    val displayGroups = if (hasLiveGroup) {
                        buildHistoryDateGroups(activeGroupDetail)
                    } else {
                        spendTogetherUiState.historyGroups
                    }
                    val totalCount = if (hasLiveGroup) {
                        displayGroups.sumOf { it.transactions.size }
                    } else {
                        spendTogetherUiState.recentTransactions.size
                    }

                    TransactionHistoryScreen(
                        totalTransactionsCount = totalCount,
                        monthText = "${currentMonthLabel()}/${currentYear()}",
                        transactionGroups = displayGroups,
                        onSearchClicked = {
                            Toast.makeText(context, "Mở thanh tìm kiếm giao dịch", Toast.LENGTH_SHORT).show()
                        },
                        onFilterMemberClicked = {
                            Toast.makeText(context, "Lọc theo thành viên", Toast.LENGTH_SHORT).show()
                        },
                        onFilterMonthClicked = {
                            Toast.makeText(context, "Chọn tháng lọc", Toast.LENGTH_SHORT).show()
                        },
                        onFilterCategoryClicked = {
                            Toast.makeText(context, "Lọc theo danh mục chi tiêu", Toast.LENGTH_SHORT).show()
                        },
                        onClearFilterClicked = {
                            spendTogetherViewModel.onClearFilter()
                            Toast.makeText(context, "Đã xóa toàn bộ bộ lọc", Toast.LENGTH_SHORT).show()
                        },
                        onTransactionClicked = { tx ->
                            val liveGroupId = activeGroupDetail.group?.groupId
                            val matchedExpense = if (hasLiveGroup && !liveGroupId.isNullOrBlank()) {
                                activeGroupDetail.expenses.firstOrNull { it.expenseId == tx.id }
                            } else {
                                null
                            }
                            if (matchedExpense != null && !liveGroupId.isNullOrBlank()) {
                                navController.navigate(SpendlyRoutes.expenseDetail(liveGroupId, matchedExpense.expenseId))
                            } else {
                                Toast.makeText(
                                    context,
                                    "Giao dịch: ${tx.title} (${tx.amountFormatted})",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onNavTabSelected = { tab ->
                            handleLegacyTabNavigation(tab, navController, ::navigateToTopLevelTab)
                        }
                    )
                }

                composable(SpendlyRoutes.REPORTS) {
                    val hasLiveGroup = !activeGroupId.isNullOrBlank() && activeGroupDetail.group != null
                    val reportGroupName = if (hasLiveGroup) {
                        activeGroupDetail.group?.name.orEmpty()
                    } else {
                        spendTogetherUiState.activeGroupName
                    }
                    val totalIncomeText = if (hasLiveGroup) {
                        FintechAlgorithm.formatShortVnd(activeGroupDetail.totalIncome)
                    } else {
                        spendTogetherUiState.budget.depositedAmountFormatted.replace(" đ", "")
                    }
                    val totalExpenseText = if (hasLiveGroup) {
                        FintechAlgorithm.formatShortVnd(activeGroupDetail.totalExpense)
                    } else {
                        spendTogetherUiState.budget.spentAmountFormatted.replace(" đ", "")
                    }
                    val totalBalanceText = if (hasLiveGroup) {
                        formatCompactSignedVnd(activeGroupDetail.groupBalance)
                    } else {
                        spendTogetherUiState.budget.totalBalanceFormatted.replace(" đ", "")
                    }
                    val totalSpentFormatted = if (hasLiveGroup) {
                        FintechAlgorithm.formatVnd(activeGroupDetail.totalExpense)
                    } else {
                        spendTogetherUiState.totalSpentFormatted
                    }
                    val categoryItems = if (hasLiveGroup) {
                        buildCategoryReportItems(activeGroupDetail)
                    } else {
                        spendTogetherUiState.categoryReportItems
                    }
                    val memberItems = if (hasLiveGroup) {
                        buildMemberReportItems(activeGroupDetail)
                    } else {
                        spendTogetherUiState.memberReportItems
                    }
                    val topSpenderNote = if (hasLiveGroup) {
                        buildHighestSpenderNote(memberItems)
                    } else {
                        spendTogetherUiState.highestSpenderNote
                    }

                    FinancialReportScreen(
                        groupTitle = "Báo cáo",
                        groupSubtitle = "Phân tích $reportGroupName",
                        totalIncomeText = totalIncomeText,
                        totalExpenseText = totalExpenseText,
                        totalBalanceText = totalBalanceText,
                        totalSpentFormatted = totalSpentFormatted,
                        categoryBreakdown = categoryItems,
                        memberBreakdown = memberItems,
                        highestSpenderNote = topSpenderNote,
                        onExportReportClicked = {
                            Toast.makeText(
                                context,
                                "Đang xuất file báo cáo tài chính PDF...",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFilterMonthClicked = {
                            Toast.makeText(context, "Chọn tháng báo cáo", Toast.LENGTH_SHORT).show()
                        },
                        onCompareMonthClicked = {
                            Toast.makeText(context, "So sánh chi tiêu với tháng trước", Toast.LENGTH_SHORT).show()
                        },
                        onNavTabSelected = { tab ->
                            handleLegacyTabNavigation(tab, navController, ::navigateToTopLevelTab)
                        }
                    )
                }

                composable(SpendlyRoutes.SETTLEMENT) {
                    val hasLiveGroup = !activeGroupId.isNullOrBlank() && activeGroupDetail.group != null
                    val settlementGroupName = if (hasLiveGroup) {
                        activeGroupDetail.group?.name.orEmpty()
                    } else {
                        spendTogetherUiState.activeGroupName
                    }
                    val debtsList = if (hasLiveGroup) {
                        buildDebtItems(activeGroupDetail)
                    } else {
                        spendTogetherUiState.debts
                    }

                    SettlementScreen(
                        groupTitle = "Ai nợ ai?",
                        groupSubtitle = "Quyết toán $settlementGroupName",
                        debtsList = debtsList,
                        autoReminder = spendTogetherUiState.autoReminderEnabled,
                        onToggleAutoReminder = spendTogetherViewModel::onToggleAutoReminder,
                        onBackClicked = {
                            navController.popBackStack()
                        },
                        onHistoryClicked = {
                            navController.navigate(SpendlyRoutes.HISTORY)
                        },
                        onSettleDebtClicked = { debt ->
                            if (hasLiveGroup) {
                                Toast.makeText(
                                    context,
                                    "Đã ghi nhận nhắc quyết toán ${debt.amountFormatted} cho ${debt.debtorName}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                spendTogetherViewModel.onSettleDebt(debt.id)
                                Toast.makeText(
                                    context,
                                    "Đã xác nhận thanh toán cho ${debt.debtorName}!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }

                composable(SpendlyRoutes.MEMBERS) {
                    val hasLiveGroup = !activeGroupId.isNullOrBlank() && activeGroupDetail.group != null
                    val membersGroupName = if (hasLiveGroup) {
                        activeGroupDetail.group?.name.orEmpty()
                    } else {
                        spendTogetherUiState.activeGroupName
                    }
                    val membersList = if (hasLiveGroup) {
                        buildGroupMembers(activeGroupDetail)
                    } else {
                        spendTogetherUiState.members
                    }
                    val totalContributed = if (hasLiveGroup) {
                        FintechAlgorithm.formatVnd(activeGroupDetail.totalIncome)
                    } else {
                        spendTogetherUiState.totalContributedAmount
                    }

                    MembersScreen(
                        groupTitle = "Thành viên",
                        groupSubtitle = "${membersList.size} người trong $membersGroupName",
                        totalContributedAmount = totalContributed,
                        membersList = membersList,
                        onBackClicked = {
                            navController.popBackStack()
                        },
                        onAddMemberSubmit = { name, phone, isLeader ->
                            val liveGroupId = activeGroupDetail.group?.groupId
                            if (hasLiveGroup && !liveGroupId.isNullOrBlank()) {
                                groupViewModel.addManualMember(
                                    groupId = liveGroupId,
                                    name = name,
                                    phone = phone,
                                    role = if (isLeader) GroupRole.ADMIN else GroupRole.MEMBER
                                )
                            } else {
                                spendTogetherViewModel.onAddMember(name, phone, isLeader)
                            }
                            Toast.makeText(
                                context,
                                "Đã thêm thành viên $name vào nhóm!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onMemberClicked = { member ->
                            spendTogetherViewModel.onSelectMemberForEdit(member)
                            navController.navigate(SpendlyRoutes.EDIT_MEMBER)
                        }
                    )
                }

                composable(SpendlyRoutes.EDIT_MEMBER) {
                    val activeMember = spendTogetherUiState.selectedMember
                        ?: spendTogetherUiState.members.firstOrNull()
                    val fallbackName = currentUser.fullName.ifBlank { "Nguyễn Phan Tấn Đạt" }
                    EditMemberScreen(
                        initialMemberName = activeMember?.name ?: fallbackName,
                        initialInitials = activeMember?.initials ?: computeInitials(fallbackName),
                        initialPhoneNumber = activeMember?.phoneNumber
                            ?: currentUser.phone.ifBlank { "0986 420 115" },
                        initialIsLeader = activeMember?.isLeader ?: false,
                        initialCanAddExpense = true,
                        onBackClicked = {
                            navController.popBackStack()
                        },
                        onChangeAvatarClicked = {
                            Toast.makeText(context, "Chọn ảnh đại diện mới", Toast.LENGTH_SHORT).show()
                        },
                        onSaveClicked = { name, phone, isLeader, canAddExpense ->
                            spendTogetherViewModel.onSaveMember(name, phone, isLeader, canAddExpense)
                            Toast.makeText(context, "Đã lưu thông tin của $name", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                        onDeleteMemberClicked = {
                            activeMember?.let { spendTogetherViewModel.onDeleteMember(it.id) }
                            Toast.makeText(
                                context,
                                "Đã xóa ${activeMember?.name ?: "thành viên"} khỏi nhóm",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.popBackStack()
                        }
                    )
                }
            }
        }

        if (selectedTopLevelTab != null) {
            SpendlyBottomBar(
                selectedTab = selectedTopLevelTab,
                onTabSelected = { tab ->
                    when (tab) {
                        SpendlyTab.HOME -> navigateToTopLevelTab(SpendlyRoutes.HOME)
                        SpendlyTab.PROFILE -> navigateToTopLevelTab(SpendlyRoutes.PROFILE)
                        SpendlyTab.ADD -> showAddSheet = true
                        SpendlyTab.GROUPS -> navigateToTopLevelTab(SpendlyRoutes.GROUPS)
                        SpendlyTab.NOTIFICATIONS -> navigateToTopLevelTab(SpendlyRoutes.NOTIFICATIONS)
                    }
                },
                onAddClick = { showAddSheet = true },
                unreadNotificationsCount = unreadNotificationsCount,
                hazeState = bottomBarHazeState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    if (showAddSheet) {
        val effectiveGroupId = resolveEffectiveGroupId(groupsUiState)
        val effectiveGroupName = activeGroupDetail.group?.name
            ?: groupsUiState.groups.firstOrNull { it.groupId == effectiveGroupId }?.name
            ?: spendTogetherUiState.activeGroupName

        AddTransactionModalSheet(
            onDismissRequest = { showAddSheet = false },
            activeGroupName = effectiveGroupName,
            hazeState = bottomBarHazeState,
            onAddExpenseClick = {
                startExpenseFlowForActiveGroup()
            },
            onAddIncomeClick = {
                startIncomeFlowForActiveGroup()
            },
            onAiQuickEntryClick = {
                if (!effectiveGroupId.isNullOrBlank()) {
                    activateGroupEverywhere(effectiveGroupId, groupViewModel, spendTogetherViewModel)
                }
                navController.navigate(SpendlyRoutes.CHATBOT)
            }
        )
    }
}

private fun syncSignedInUser(
    fbUser: FirebaseUser,
    fallbackEmailOrPhone: String,
    fallbackName: String?,
    spendTogetherViewModel: SpendTogetherViewModel,
    groupViewModel: GroupViewModel
) {
    val resolvedEmail = fbUser.email?.takeIf { it.isNotBlank() } ?: fallbackEmailOrPhone
    val resolvedName = fallbackName?.takeIf { it.isNotBlank() }
        ?: fbUser.displayName?.takeIf { it.isNotBlank() }
        ?: resolvedEmail.substringBefore('@').takeIf { it.isNotBlank() }
        ?: "Thành viên Spendly"

    spendTogetherViewModel.syncFirebaseUser(
        uid = fbUser.uid,
        name = resolvedName,
        email = resolvedEmail
    )
    groupViewModel.bindUser(
        SpendlyUser(
            uid = fbUser.uid,
            fullName = resolvedName,
            email = resolvedEmail,
            phone = fbUser.phoneNumber.orEmpty(),
            isActive = true
        )
    )
}

private fun resolveEffectiveGroupId(groupsUiState: GroupListUiState): String? {
    return groupsUiState.activeGroupId?.takeIf { it.isNotBlank() }
        ?: groupsUiState.groups.firstOrNull()?.groupId?.takeIf { it.isNotBlank() }
}

private fun activateGroupEverywhere(
    groupId: String,
    groupViewModel: GroupViewModel,
    spendTogetherViewModel: SpendTogetherViewModel
) {
    groupViewModel.setActiveGroup(groupId)
    SpendlyServiceLocator.setActiveGroup(groupId)
    spendTogetherViewModel.setActiveGroupId(groupId)
}

private fun handleLegacyTabNavigation(
    tab: BottomNavTab,
    navController: NavHostController,
    navigateToTopLevelTab: (String) -> Unit
) {
    when (tab) {
        BottomNavTab.HOME -> navigateToTopLevelTab(SpendlyRoutes.HOME)
        BottomNavTab.HISTORY -> navController.navigate(SpendlyRoutes.HISTORY) {
            launchSingleTop = true
        }
        BottomNavTab.REPORTS -> navController.navigate(SpendlyRoutes.REPORTS) {
            launchSingleTop = true
        }
        BottomNavTab.PROFILE -> navigateToTopLevelTab(SpendlyRoutes.PROFILE)
    }
}

private data class HomeLedgerFeedItem(
    val sortTimestamp: Long,
    val recentTransaction: RecentTransaction
)

private data class HistoryLedgerFeedItem(
    val dateHeader: String,
    val sortTimestamp: Long,
    val signedAmount: Long,
    val historyItem: HistoryTransactionItem
)

private fun buildHomeRecentTransactions(detail: GroupDetailUiState): List<RecentTransaction> {
    val expenseItems = detail.expenses
        .filter { it.status == RecordStatus.ACTIVE }
        .map { exp ->
            val ts = exp.expenseDateMillis.takeIf { it > 0L } ?: exp.createdAtMillis
            val payer = exp.payerName.ifBlank {
                detail.memberNamesById[exp.payerId].orEmpty().ifBlank { "Thành viên" }
            }
            HomeLedgerFeedItem(
                sortTimestamp = ts,
                recentTransaction = RecentTransaction(
                    id = exp.expenseId,
                    title = exp.title,
                    subtitle = "${formatShortRelativeDate(ts)} · $payer đã trả",
                    amountFormatted = "-${FintechAlgorithm.formatVnd(exp.amount)}",
                    isIncome = false,
                    iconType = when (exp.category) {
                        SpendlyCategory.HOUSING,
                        SpendlyCategory.UTILITIES -> TransactionIconType.INTERNET
                        else -> TransactionIconType.GROCERY
                    }
                )
            )
        }

    val incomeItems = detail.incomes
        .filter { it.status == RecordStatus.ACTIVE }
        .map { inc ->
            val ts = inc.incomeDateMillis.takeIf { it > 0L } ?: inc.createdAtMillis
            val payer = inc.payerName.ifBlank {
                detail.memberNamesById[inc.payerId].orEmpty().ifBlank { "Thành viên" }
            }
            HomeLedgerFeedItem(
                sortTimestamp = ts,
                recentTransaction = RecentTransaction(
                    id = inc.incomeId,
                    title = inc.title,
                    subtitle = "${formatShortRelativeDate(ts)} · $payer nạp quỹ",
                    amountFormatted = "+${FintechAlgorithm.formatVnd(inc.amount)}",
                    isIncome = true,
                    iconType = TransactionIconType.DEPOSIT
                )
            )
        }

    return (expenseItems + incomeItems)
        .sortedByDescending { it.sortTimestamp }
        .take(10)
        .map { it.recentTransaction }
}

private fun buildHistoryDateGroups(detail: GroupDetailUiState): List<TransactionDateGroup> {
    val expenseItems = detail.expenses
        .filter { it.status == RecordStatus.ACTIVE }
        .map { exp ->
            val ts = exp.expenseDateMillis.takeIf { it > 0L } ?: exp.createdAtMillis
            val payer = exp.payerName.ifBlank {
                detail.memberNamesById[exp.payerId].orEmpty().ifBlank { "Thành viên" }
            }
            val header = formatFullRelativeDate(ts)
            HistoryLedgerFeedItem(
                dateHeader = header,
                sortTimestamp = ts,
                signedAmount = -exp.amount,
                historyItem = HistoryTransactionItem(
                    id = exp.expenseId,
                    title = exp.title,
                    subtitle = "${formatShortRelativeDate(ts)} · $payer đã trả",
                    amountFormatted = "-${FintechAlgorithm.formatVnd(exp.amount)}",
                    isIncome = false,
                    type = when (exp.category) {
                        SpendlyCategory.HOUSING,
                        SpendlyCategory.UTILITIES -> HistoryItemType.INTERNET
                        SpendlyCategory.TRANSPORT -> HistoryItemType.TAXI
                        SpendlyCategory.FOOD -> HistoryItemType.COFFEE
                        else -> HistoryItemType.GROCERY
                    }
                )
            )
        }

    val incomeItems = detail.incomes
        .filter { it.status == RecordStatus.ACTIVE }
        .map { inc ->
            val ts = inc.incomeDateMillis.takeIf { it > 0L } ?: inc.createdAtMillis
            val payer = inc.payerName.ifBlank {
                detail.memberNamesById[inc.payerId].orEmpty().ifBlank { "Thành viên" }
            }
            val header = formatFullRelativeDate(ts)
            HistoryLedgerFeedItem(
                dateHeader = header,
                sortTimestamp = ts,
                signedAmount = inc.amount,
                historyItem = HistoryTransactionItem(
                    id = inc.incomeId,
                    title = inc.title,
                    subtitle = "${formatShortRelativeDate(ts)} · $payer nạp quỹ",
                    amountFormatted = "+${FintechAlgorithm.formatVnd(inc.amount)}",
                    isIncome = true,
                    type = HistoryItemType.DEPOSIT
                )
            )
        }

    return (expenseItems + incomeItems)
        .sortedByDescending { it.sortTimestamp }
        .groupBy { it.dateHeader }
        .map { (header, entries) ->
            val netTotal = entries.sumOf { it.signedAmount }
            val sign = if (netTotal >= 0L) "+" else ""
            TransactionDateGroup(
                dateHeader = header,
                netTotalFormatted = "$sign${FintechAlgorithm.formatVnd(netTotal)}",
                transactions = entries.map { it.historyItem }
            )
        }
}

private fun buildCategoryReportItems(detail: GroupDetailUiState): List<CategoryReportItem> {
    val palette = listOf(
        SpendlyLavenderDeep,
        SpendlyMintDeep,
        SpendlySky,
        SpendlyAmber,
        SpendlyCoralDeep,
        SpendlyIndigo,
        SpendlyLavender,
        SpendlyMint,
        Color(0xFF7B6F99)
    )
    return detail.categoryBreakdown.mapIndexed { index, item ->
        val pct = item.ratio * 100f
        CategoryReportItem(
            category = item.category.toLegacyExpenseCategory(),
            categoryName = item.category.label,
            amount = item.totalAmount,
            amountFormatted = FintechAlgorithm.formatShortVnd(item.totalAmount),
            percentage = pct,
            sweepAngle = pct * 3.6f,
            color = palette[index % palette.size]
        )
    }
}

private fun buildMemberReportItems(detail: GroupDetailUiState): List<MemberReportItem> {
    val activeExpenses = detail.expenses.filter { it.status == RecordStatus.ACTIVE }
    val spendByMember = detail.members.associate { member ->
        member.userId to activeExpenses.filter { it.payerId == member.userId }.sumOf { it.amount }
    }
    val maxSpent = spendByMember.values.maxOrNull()?.takeIf { it > 0L } ?: 1L
    return detail.members.map { member ->
        val spent = spendByMember[member.userId] ?: 0L
        val ratio = spent.toFloat() / maxSpent.toFloat()
        val heightRatio = if (spent == 0L) 0.08f else ratio.coerceIn(0.18f, 1.0f)
        val shortName = member.displayName.trim().split(" ").lastOrNull() ?: member.displayName
        MemberReportItem(
            memberId = member.userId,
            memberName = member.displayName,
            shortName = shortName,
            amount = spent,
            amountFormatted = FintechAlgorithm.formatShortVnd(spent),
            heightRatio = heightRatio
        )
    }
}

private fun buildHighestSpenderNote(memberItems: List<MemberReportItem>): String {
    val top = memberItems.maxByOrNull { it.amount }
    return if (top != null && top.amount > 0L) {
        "${top.memberName} chi nhiều nhất"
    } else {
        "Chưa có khoản chi"
    }
}

private fun buildDebtItems(detail: GroupDetailUiState): List<DebtItem> {
    val avatarColors = listOf(
        Color(0xFFC4E2D3),
        Color(0xFFEDE6FA),
        Color(0xFFDEE7A8),
        Color(0xFFF2D7C8)
    )
    return detail.simplifiedTransfers.mapIndexed { index, transfer ->
        val debtorName = detail.memberNamesById[transfer.fromUserId] ?: transfer.fromUserId
        val creditorName = detail.memberNamesById[transfer.toUserId] ?: transfer.toUserId
        DebtItem(
            id = "${transfer.fromUserId}->${transfer.toUserId}",
            debtorName = debtorName,
            debtorInitials = computeInitials(debtorName),
            debtorAvatarBg = avatarColors[index % avatarColors.size],
            creditorName = creditorName,
            creditorInitials = computeInitials(creditorName),
            creditorAvatarBg = avatarColors[(index + 1) % avatarColors.size],
            rawAmount = transfer.amount,
            amountFormatted = FintechAlgorithm.formatVnd(transfer.amount),
            statusText = "Cần thanh toán",
            isPaid = false
        )
    }
}

private fun buildGroupMembers(detail: GroupDetailUiState): List<GroupMember> {
    val avatarColors = listOf(
        Color(0xFFC4E2D3),
        Color(0xFFDEE7A8),
        Color(0xFFCBE3DC),
        Color(0xFFF2D7C8)
    )
    val balancesByUser = detail.balances.associateBy { it.userId }
    return detail.members.mapIndexed { index, member ->
        val net = balancesByUser[member.userId]?.netBalance ?: 0L
        val status = when {
            net > 0L -> MemberBalanceStatus.Positive("+${FintechAlgorithm.formatVnd(net)}")
            net < 0L -> MemberBalanceStatus.Negative(FintechAlgorithm.formatVnd(net))
            else -> MemberBalanceStatus.Balanced
        }
        GroupMember(
            id = member.userId,
            name = member.displayName,
            initials = member.initials.ifBlank { computeInitials(member.displayName) },
            phoneNumber = member.phone.ifBlank { "Thành viên Spendly" },
            isLeader = member.role == GroupRole.OWNER || member.role == GroupRole.ADMIN,
            avatarBgColor = avatarColors[index % avatarColors.size],
            balanceStatus = status
        )
    }
}

private fun formatCompactSignedVnd(amount: Long): String {
    return if (amount < 0L) "-${FintechAlgorithm.formatShortVnd(-amount)}" else FintechAlgorithm.formatShortVnd(amount)
}

private fun computeInitials(fullName: String): String {
    val parts = fullName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "SP"
        parts.size == 1 -> parts.first().take(2).uppercase()
        else -> {
            val first = parts[parts.size - 2].first()
            val second = parts.last().first()
            "$first$second".uppercase()
        }
    }
}

private fun currentMonthLabel(): String {
    val month = Calendar.getInstance().get(Calendar.MONTH) + 1
    return "Tháng $month"
}

private fun currentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

private fun formatShortRelativeDate(millis: Long): String {
    if (millis <= 0L) return "Hôm nay"
    return when {
        isSameDay(millis, 0) -> "Hôm nay"
        isSameDay(millis, -1) -> "Hôm qua"
        else -> SimpleDateFormat("dd/MM", Locale.forLanguageTag("vi-VN")).format(Date(millis))
    }
}

private fun formatFullRelativeDate(millis: Long): String {
    if (millis <= 0L) return "Hôm nay"
    return when {
        isSameDay(millis, 0) -> "Hôm nay"
        isSameDay(millis, -1) -> "Hôm qua"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("vi-VN")).format(Date(millis))
    }
}

private fun isSameDay(millis: Long, offsetDaysFromToday: Int): Boolean {
    val target = Calendar.getInstance().apply { timeInMillis = millis }
    val reference = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offsetDaysFromToday) }
    return target.get(Calendar.YEAR) == reference.get(Calendar.YEAR) &&
        target.get(Calendar.DAY_OF_YEAR) == reference.get(Calendar.DAY_OF_YEAR)
}
