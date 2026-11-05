package com.example.nhoms8

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nhoms8.ui.theme.Nhoms8Theme
import com.example.nhoms8.viewmodel.SpendTogetherViewModel

/**
 * Các màn hình chính trong chuỗi Onboarding, Auth & Dashboard ứng dụng SpendTogether
 */
enum class AppScreen {
    WELCOME,
    ONBOARDING_1,
    ONBOARDING_2,
    ONBOARDING_3,
    LOGIN,
    SIGN_UP,
    HOME,
    MEMBERS,
    EDIT_MEMBER,
    CREATE_EXPENSE,
    SPLIT_BILL,
    SETTLEMENT,
    HISTORY,
    REPORTS,
    PROFILE,
    CHATBOT
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Nhoms8Theme {
                SpendTogetherApp()
            }
        }
    }
}

/**
 * Ứng dụng SpendTogether kết nối trọn vẹn ViewModel & StateFlow cho 15 màn hình Jetpack Compose
 */
@Composable
fun SpendTogetherApp(
    viewModel: SpendTogetherViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Màn hình bắt đầu: HOME (Tổng quan quỹ nhóm) để trải nghiệm dữ liệu tức thì
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Crossfade(
            targetState = currentScreen,
            animationSpec = tween(durationMillis = 400),
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                AppScreen.WELCOME -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                currentScreen = AppScreen.ONBOARDING_1
                            }
                    ) {
                        WelcomeScreen()
                    }
                }

                AppScreen.ONBOARDING_1 -> {
                    OnboardingScreen1(
                        onSkipClicked = {
                            currentScreen = AppScreen.LOGIN
                        },
                        onContinueClicked = {
                            currentScreen = AppScreen.ONBOARDING_2
                        }
                    )
                }

                AppScreen.ONBOARDING_2 -> {
                    OnboardingScreen2(
                        onSkipClicked = {
                            currentScreen = AppScreen.LOGIN
                        },
                        onContinueClicked = {
                            currentScreen = AppScreen.ONBOARDING_3
                        }
                    )
                }

                AppScreen.ONBOARDING_3 -> {
                    OnboardingScreen3(
                        onSkipClicked = {
                            currentScreen = AppScreen.LOGIN
                        },
                        onGetStartedClicked = {
                            currentScreen = AppScreen.LOGIN
                        }
                    )
                }

                AppScreen.LOGIN -> {
                    LoginScreen(
                        isLoading = uiState.authLoading,
                        errorMessage = uiState.authErrorMessage,
                        onLoginSubmit = { emailOrPhone, password ->
                            viewModel.signInWithFirebase(emailOrPhone, password) {
                                Toast.makeText(context, "Đăng nhập Firebase thành công!", Toast.LENGTH_SHORT).show()
                                currentScreen = AppScreen.HOME
                            }
                        },
                        onRegisterClicked = {
                            viewModel.clearAuthError()
                            currentScreen = AppScreen.SIGN_UP
                        },
                        onForgotPasswordClicked = { emailOrPhone ->
                            viewModel.sendPasswordReset(emailOrPhone) { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        onGoogleLoginClicked = {
                            viewModel.onLogin("0912 345 678", "Nguyễn Minh Anh (Google)")
                            Toast.makeText(context, "Đăng nhập với Google thành công", Toast.LENGTH_SHORT).show()
                            currentScreen = AppScreen.HOME
                        },
                        onAppleLoginClicked = {
                            viewModel.onLogin("0912 345 678", "Nguyễn Minh Anh (Apple)")
                            Toast.makeText(context, "Đăng nhập với Apple thành công", Toast.LENGTH_SHORT).show()
                            currentScreen = AppScreen.HOME
                        }
                    )
                }

                AppScreen.SIGN_UP -> {
                    SignUpScreen(
                        isLoading = uiState.authLoading,
                        errorMessage = uiState.authErrorMessage,
                        onBackClicked = {
                            viewModel.clearAuthError()
                            currentScreen = AppScreen.LOGIN
                        },
                        onSignUpSubmit = { name, emailOrPhone, password ->
                            viewModel.signUpWithFirebase(name, emailOrPhone, password) {
                                Toast.makeText(context, "Đăng ký tài khoản Firebase thành công!", Toast.LENGTH_SHORT).show()
                                currentScreen = AppScreen.HOME
                            }
                        },
                        onLoginClicked = {
                            viewModel.clearAuthError()
                            currentScreen = AppScreen.LOGIN
                        },
                        onPickAvatarClicked = {
                            Toast.makeText(context, "Chọn ảnh đại diện từ thư viện", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                AppScreen.HOME -> {
                    HomeScreen(
                        userName = uiState.userProfile.name,
                        userInitials = uiState.userProfile.initials,
                        groupName = uiState.userProfile.groupName,
                        totalBalance = uiState.budget.totalBalanceFormatted,
                        monthLabel = uiState.budget.monthLabel,
                        depositedAmount = uiState.budget.depositedAmountFormatted,
                        spentAmount = uiState.budget.spentAmountFormatted,
                        recentTransactions = uiState.recentTransactions,
                        onNotificationClicked = {
                            Toast.makeText(context, "Bạn có ${uiState.unpaidDebtsCount} khoản nợ cần quyết toán", Toast.LENGTH_SHORT).show()
                        },
                        onAddExpenseClicked = {
                            currentScreen = AppScreen.CREATE_EXPENSE
                        },
                        onTopUpFundClicked = {
                            viewModel.onTopUpFund(1000000L)
                            Toast.makeText(context, "Đã nạp +1.000.000 đ vào quỹ nhóm!", Toast.LENGTH_SHORT).show()
                        },
                        onSplitBillClicked = {
                            currentScreen = AppScreen.SPLIT_BILL
                        },
                        onMembersClicked = {
                            currentScreen = AppScreen.MEMBERS
                        },
                        onViewAllTransactionsClicked = {
                            currentScreen = AppScreen.HISTORY
                        },
                        onTransactionClicked = { tx ->
                            Toast.makeText(context, "Chi tiết: ${tx.title} (${tx.amountFormatted})", Toast.LENGTH_SHORT).show()
                        },
                        onChatbotClicked = {
                            currentScreen = AppScreen.CHATBOT
                        },
                        onNavTabSelected = { tab ->
                            when (tab) {
                                BottomNavTab.HOME -> currentScreen = AppScreen.HOME
                                BottomNavTab.HISTORY -> currentScreen = AppScreen.HISTORY
                                BottomNavTab.REPORTS -> currentScreen = AppScreen.REPORTS
                                BottomNavTab.PROFILE -> currentScreen = AppScreen.PROFILE
                            }
                        }
                    )
                }

                AppScreen.MEMBERS -> {
                    MembersScreen(
                        groupTitle = "Thành viên",
                        groupSubtitle = "${uiState.members.size} người trong ${uiState.userProfile.groupName}",
                        totalContributedAmount = uiState.totalContributedAmount,
                        membersList = uiState.members,
                        onBackClicked = {
                            currentScreen = AppScreen.HOME
                        },
                        onAddMemberClicked = {
                            val newNumber = (100..999).random()
                            viewModel.onAddMember("Thành viên mới $newNumber", "09$newNumber 888")
                            Toast.makeText(context, "Đã thêm thành viên mới!", Toast.LENGTH_SHORT).show()
                        },
                        onMemberClicked = { member ->
                            viewModel.onSelectMemberForEdit(member)
                            currentScreen = AppScreen.EDIT_MEMBER
                        }
                    )
                }

                AppScreen.EDIT_MEMBER -> {
                    val activeMember = uiState.selectedMember ?: uiState.members.firstOrNull()
                    EditMemberScreen(
                        initialMemberName = activeMember?.name ?: "Tuấn Nam",
                        initialInitials = activeMember?.initials ?: "TN",
                        initialPhoneNumber = activeMember?.phoneNumber ?: "0986 420 115",
                        initialIsLeader = activeMember?.isLeader ?: false,
                        initialCanAddExpense = true,
                        onBackClicked = {
                            currentScreen = AppScreen.MEMBERS
                        },
                        onChangeAvatarClicked = {
                            Toast.makeText(context, "Chọn ảnh đại diện mới", Toast.LENGTH_SHORT).show()
                        },
                        onSaveClicked = { name, phone, isLeader, canAddExpense ->
                            viewModel.onSaveMember(name, phone, isLeader, canAddExpense)
                            Toast.makeText(context, "Đã lưu thông tin của $name", Toast.LENGTH_SHORT).show()
                            currentScreen = AppScreen.MEMBERS
                        },
                        onDeleteMemberClicked = {
                            activeMember?.let { viewModel.onDeleteMember(it.id) }
                            Toast.makeText(context, "Đã xóa ${activeMember?.name} khỏi nhóm", Toast.LENGTH_SHORT).show()
                            currentScreen = AppScreen.MEMBERS
                        }
                    )
                }

                AppScreen.CREATE_EXPENSE -> {
                    CreateExpenseScreen(
                        groupTitle = "Tạo khoản chi",
                        groupSubtitle = "Ghi vào ${uiState.userProfile.groupName}",
                        initialAmount = "850.000",
                        initialAmountInWords = "Tám trăm năm mươi nghìn đồng",
                        onBackClicked = {
                            currentScreen = AppScreen.HOME
                        },
                        onScanReceiptClicked = {
                            Toast.makeText(context, "Tính năng Quét hóa đơn OCR", Toast.LENGTH_SHORT).show()
                        },
                        onSaveAndSplitClicked = { amount, category, note ->
                            viewModel.onCreateExpenseDraft(amount, category, note)
                            Toast.makeText(context, "Đã lưu khoản chi $amount đ [$category]", Toast.LENGTH_SHORT).show()
                            currentScreen = AppScreen.SPLIT_BILL
                        }
                    )
                }

                AppScreen.SPLIT_BILL -> {
                    SplitBillScreen(
                        expenseTitle = uiState.draftExpense.title,
                        totalAmount = uiState.draftExpense.amount,
                        splitMembers = uiState.splitMembers,
                        onToggleMember = { memberId ->
                            viewModel.onToggleSplitMember(memberId)
                        },
                        onBackClicked = {
                            currentScreen = AppScreen.HOME
                        },
                        onCalculatorClicked = {
                            Toast.makeText(context, "Mở máy tính tính nhẩm", Toast.LENGTH_SHORT).show()
                        },
                        onConfirmSplitClicked = { count, perPerson ->
                            viewModel.onConfirmSplitBill(count, perPerson)
                            Toast.makeText(context, "Đã chia $perPerson đ cho $count người!", Toast.LENGTH_SHORT).show()
                            currentScreen = AppScreen.SETTLEMENT
                        }
                    )
                }

                AppScreen.SETTLEMENT -> {
                    SettlementScreen(
                        groupTitle = "Ai nợ ai?",
                        groupSubtitle = "Quyết toán ${uiState.userProfile.groupName}",
                        debtsList = uiState.debts,
                        autoReminder = uiState.autoReminderEnabled,
                        onToggleAutoReminder = {
                            viewModel.onToggleAutoReminder(it)
                        },
                        onBackClicked = {
                            currentScreen = AppScreen.HOME
                        },
                        onHistoryClicked = {
                            currentScreen = AppScreen.HISTORY
                        },
                        onSettleDebtClicked = { debt ->
                            viewModel.onSettleDebt(debt.id)
                            Toast.makeText(context, "Đã xác nhận thanh toán cho ${debt.debtorName}!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                AppScreen.HISTORY -> {
                    TransactionHistoryScreen(
                        totalTransactionsCount = uiState.recentTransactions.size,
                        monthText = "${uiState.budget.monthLabel}/2026",
                        transactionGroups = uiState.historyGroups,
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
                            viewModel.onClearFilter()
                            Toast.makeText(context, "Đã xóa toàn bộ bộ lọc", Toast.LENGTH_SHORT).show()
                        },
                        onTransactionClicked = { tx ->
                            Toast.makeText(context, "Giao dịch: ${tx.title} (${tx.amountFormatted})", Toast.LENGTH_SHORT).show()
                        },
                        onNavTabSelected = { tab ->
                            when (tab) {
                                BottomNavTab.HOME -> currentScreen = AppScreen.HOME
                                BottomNavTab.HISTORY -> currentScreen = AppScreen.HISTORY
                                BottomNavTab.REPORTS -> currentScreen = AppScreen.REPORTS
                                BottomNavTab.PROFILE -> currentScreen = AppScreen.PROFILE
                            }
                        }
                    )
                }

                AppScreen.REPORTS -> {
                    FinancialReportScreen(
                        groupTitle = "Báo cáo",
                        groupSubtitle = "Phân tích ${uiState.userProfile.groupName}",
                        totalIncomeText = uiState.budget.depositedAmountFormatted.replace(" đ", ""),
                        totalExpenseText = uiState.budget.spentAmountFormatted.replace(" đ", ""),
                        totalBalanceText = uiState.budget.totalBalanceFormatted.replace(" đ", ""),
                        onExportReportClicked = {
                            Toast.makeText(context, "Đang xuất file báo cáo tài chính PDF...", Toast.LENGTH_SHORT).show()
                        },
                        onFilterMonthClicked = {
                            Toast.makeText(context, "Chọn tháng báo cáo", Toast.LENGTH_SHORT).show()
                        },
                        onCompareMonthClicked = {
                            Toast.makeText(context, "So sánh chi tiêu với tháng trước", Toast.LENGTH_SHORT).show()
                        },
                        onNavTabSelected = { tab ->
                            when (tab) {
                                BottomNavTab.HOME -> currentScreen = AppScreen.HOME
                                BottomNavTab.HISTORY -> currentScreen = AppScreen.HISTORY
                                BottomNavTab.REPORTS -> currentScreen = AppScreen.REPORTS
                                BottomNavTab.PROFILE -> currentScreen = AppScreen.PROFILE
                            }
                        }
                    )
                }

                AppScreen.PROFILE -> {
                    ProfileScreen(
                        userName = uiState.userProfile.name,
                        userInitials = uiState.userProfile.initials,
                        userSubtitle = "${uiState.userProfile.phoneNumber} · ${if (uiState.userProfile.isLeader) "Trưởng nhóm" else "Thành viên"}",
                        groupName = uiState.userProfile.groupName,
                        groupSubtitle = "${uiState.members.size} thành viên · ${uiState.budget.totalBalanceFormatted}",
                        onQrCodeClicked = {
                            Toast.makeText(context, "Mã QR nhận tiền quỹ", Toast.LENGTH_SHORT).show()
                        },
                        onEditProfileClicked = {
                            uiState.members.find { it.id == uiState.userProfile.id }?.let {
                                viewModel.onSelectMemberForEdit(it)
                            }
                            currentScreen = AppScreen.EDIT_MEMBER
                        },
                        onChangeGroupClicked = {
                            Toast.makeText(context, "Chuyển sang nhóm quỹ khác", Toast.LENGTH_SHORT).show()
                        },
                        onThemeClicked = {
                            Toast.makeText(context, "Tùy chỉnh giao diện", Toast.LENGTH_SHORT).show()
                        },
                        onCurrencyClicked = {
                            Toast.makeText(context, "Chọn đơn vị tiền tệ", Toast.LENGTH_SHORT).show()
                        },
                        onSecurityClicked = {
                            Toast.makeText(context, "Cài đặt bảo mật & sinh trắc học", Toast.LENGTH_SHORT).show()
                        },
                        onHelpClicked = {
                            Toast.makeText(context, "Trung tâm trợ giúp & hướng dẫn", Toast.LENGTH_SHORT).show()
                        },
                        onLogoutClicked = {
                            viewModel.signOutFromFirebase()
                            Toast.makeText(context, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show()
                            currentScreen = AppScreen.LOGIN
                        },
                        onNavTabSelected = { tab ->
                            when (tab) {
                                BottomNavTab.HOME -> currentScreen = AppScreen.HOME
                                BottomNavTab.HISTORY -> currentScreen = AppScreen.HISTORY
                                BottomNavTab.REPORTS -> currentScreen = AppScreen.REPORTS
                                BottomNavTab.PROFILE -> currentScreen = AppScreen.PROFILE
                            }
                        }
                    )
                }

                AppScreen.CHATBOT -> {
                    ChatbotScreen(
                        messages = uiState.chatMessages,
                        isTyping = uiState.isChatbotTyping,
                        onBackClicked = {
                            currentScreen = AppScreen.HOME
                        },
                        onSendMessage = { text ->
                            viewModel.sendMessageToChatbot(text)
                        },
                        onConfirmExpense = { messageId ->
                            viewModel.confirmChatExpense(messageId)
                            Toast.makeText(context, "Đã lưu khoản chi vào quỹ nhóm!", Toast.LENGTH_SHORT).show()
                        },
                        onClearChat = {
                            viewModel.clearChatHistory()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Preview toàn bộ luồng hoạt động ứng dụng SpendTogether trong Android Studio
 */
@Preview(
    name = "SpendTogether · Luồng ứng dụng chính",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun SpendTogetherAppPreview() {
    Nhoms8Theme {
        SpendTogetherApp()
    }
}
