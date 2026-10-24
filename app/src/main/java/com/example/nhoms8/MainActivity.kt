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
import com.example.nhoms8.ui.theme.Nhoms8Theme

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
    PROFILE
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

@Composable
fun SpendTogetherApp() {
    val context = LocalContext.current
    // Mặc định hiển thị Màn hình Cá nhân và cài đặt (15 · Profile) để kiểm tra giao diện mới nhất
    var currentScreen by remember { mutableStateOf(AppScreen.PROFILE) }

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
                        onLoginSuccess = {
                            Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                            currentScreen = AppScreen.HOME
                        },
                        onRegisterClicked = {
                            currentScreen = AppScreen.SIGN_UP
                        },
                        onForgotPasswordClicked = {
                            Toast.makeText(context, "Tính năng Quên mật khẩu", Toast.LENGTH_SHORT).show()
                        },
                        onGoogleLoginClicked = {
                            Toast.makeText(context, "Đăng nhập với Google", Toast.LENGTH_SHORT).show()
                            currentScreen = AppScreen.HOME
                        },
                        onAppleLoginClicked = {
                            Toast.makeText(context, "Đăng nhập với Apple", Toast.LENGTH_SHORT).show()
                            currentScreen = AppScreen.HOME
                        }
                    )
                }

                AppScreen.SIGN_UP -> {
                    SignUpScreen(
                        onBackClicked = {
                            currentScreen = AppScreen.LOGIN
                        },
                        onSignUpSuccess = {
                            Toast.makeText(context, "Đăng ký tài khoản thành công!", Toast.LENGTH_SHORT).show()
                            currentScreen = AppScreen.HOME
                        },
                        onLoginClicked = {
                            currentScreen = AppScreen.LOGIN
                        },
                        onPickAvatarClicked = {
                            Toast.makeText(context, "Chọn ảnh đại diện từ thư viện", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                AppScreen.HOME -> {
                    HomeScreen(
                        userName = "Minh Anh",
                        userInitials = "MA",
                        groupName = "Quỹ Nhà Mình",
                        totalBalance = "12.680.000 đ",
                        monthLabel = "Tháng 10",
                        depositedAmount = "8.500.000 đ",
                        spentAmount = "5.820.000 đ",
                        onNotificationClicked = {
                            Toast.makeText(context, "Thông báo mới", Toast.LENGTH_SHORT).show()
                        },
                        onAddExpenseClicked = {
                            currentScreen = AppScreen.CREATE_EXPENSE
                        },
                        onTopUpFundClicked = {
                            Toast.makeText(context, "Nạp tiền vào quỹ", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(context, "Chi tiết: ${tx.title}", Toast.LENGTH_SHORT).show()
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
                        groupSubtitle = "5 người trong quỹ Nhà Mình",
                        totalContributedAmount = "21.500.000 đ",
                        onBackClicked = {
                            currentScreen = AppScreen.HOME
                        },
                        onAddMemberClicked = {
                            Toast.makeText(context, "Mở form thêm thành viên mới", Toast.LENGTH_SHORT).show()
                        },
                        onMemberClicked = { member ->
                            currentScreen = AppScreen.EDIT_MEMBER
                        }
                    )
                }

                AppScreen.EDIT_MEMBER -> {
                    EditMemberScreen(
                        initialMemberName = "Tuấn Nam",
                        initialInitials = "TN",
                        initialPhoneNumber = "0986 420 115",
                        initialIsLeader = false,
                        initialCanAddExpense = true,
                        onBackClicked = {
                            currentScreen = AppScreen.MEMBERS
                        },
                        onChangeAvatarClicked = {
                            Toast.makeText(context, "Chọn ảnh đại diện mới", Toast.LENGTH_SHORT).show()
                        },
                        onSaveClicked = { name, phone, isLeader, canAddExpense ->
                            Toast.makeText(context, "Đã lưu thông tin của $name", Toast.LENGTH_SHORT).show()
                            currentScreen = AppScreen.MEMBERS
                        },
                        onDeleteMemberClicked = {
                            Toast.makeText(context, "Đã xóa Tuấn Nam khỏi nhóm", Toast.LENGTH_SHORT).show()
                            currentScreen = AppScreen.MEMBERS
                        }
                    )
                }

                AppScreen.CREATE_EXPENSE -> {
                    CreateExpenseScreen(
                        groupTitle = "Tạo khoản chi",
                        groupSubtitle = "Ghi vào quỹ Nhà Mình",
                        initialAmount = "850.000",
                        initialAmountInWords = "Tám trăm năm mươi nghìn đồng",
                        onBackClicked = {
                            currentScreen = AppScreen.HOME
                        },
                        onScanReceiptClicked = {
                            Toast.makeText(context, "Tính năng Quét hóa đơn OCR", Toast.LENGTH_SHORT).show()
                        },
                        onSaveAndSplitClicked = { amount, category, note ->
                            Toast.makeText(context, "Đã lưu khoản chi $amount đ [$category]", Toast.LENGTH_SHORT).show()
                            currentScreen = AppScreen.SPLIT_BILL
                        }
                    )
                }

                AppScreen.SPLIT_BILL -> {
                    SplitBillScreen(
                        expenseTitle = "Đi chợ cuối tuần",
                        totalAmount = 850000L,
                        onBackClicked = {
                            currentScreen = AppScreen.HOME
                        },
                        onCalculatorClicked = {
                            Toast.makeText(context, "Mở máy tính tính nhẩm", Toast.LENGTH_SHORT).show()
                        },
                        onConfirmSplitClicked = { count, perPerson ->
                            Toast.makeText(context, "Đã chia $perPerson đ cho $count người!", Toast.LENGTH_SHORT).show()
                            currentScreen = AppScreen.SETTLEMENT
                        }
                    )
                }

                AppScreen.SETTLEMENT -> {
                    SettlementScreen(
                        groupTitle = "Ai nợ ai?",
                        groupSubtitle = "Quyết toán quỹ Nhà Mình",
                        onBackClicked = {
                            currentScreen = AppScreen.HOME
                        },
                        onHistoryClicked = {
                            currentScreen = AppScreen.HISTORY
                        },
                        onSettleDebtClicked = { debt ->
                            Toast.makeText(context, "Đã ghi nhận trả nợ cho ${debt.debtorName}!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                AppScreen.HISTORY -> {
                    TransactionHistoryScreen(
                        totalTransactionsCount = 23,
                        monthText = "Tháng 10/2026",
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
                        groupSubtitle = "Phân tích quỹ Nhà Mình",
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
                        userName = "Nguyễn Minh Anh",
                        userInitials = "MA",
                        userSubtitle = "0912 345 678 · Trưởng nhóm",
                        groupName = "Nhà Mình",
                        groupSubtitle = "5 thành viên · 12.680.000 đ",
                        onQrCodeClicked = {
                            Toast.makeText(context, "Mã QR nhận tiền quỹ", Toast.LENGTH_SHORT).show()
                        },
                        onEditProfileClicked = {
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
            }
        }
    }
}
