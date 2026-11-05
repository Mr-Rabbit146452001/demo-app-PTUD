package com.example.nhoms8.ui.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.nhoms8.CreateExpenseScreen
import com.example.nhoms8.EditMemberScreen
import com.example.nhoms8.FinancialReportScreen
import com.example.nhoms8.HomeScreen
import com.example.nhoms8.LoginScreen
import com.example.nhoms8.MembersScreen
import com.example.nhoms8.OnboardingScreen1
import com.example.nhoms8.OnboardingScreen2
import com.example.nhoms8.OnboardingScreen3
import com.example.nhoms8.ProfileScreen
import com.example.nhoms8.SettlementScreen
import com.example.nhoms8.SignUpScreen
import com.example.nhoms8.SplitBillScreen
import com.example.nhoms8.TransactionHistoryScreen
import com.example.nhoms8.WelcomeScreen
import com.example.nhoms8.ui.theme.Nhoms8Theme

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * BỘ SƯU TẬP PREVIEW 15 MÀN HÌNH SPENDTOGETHER (ALL SCREENS PREVIEW GALLERY)
 * Nhóm 08 - Lớp D23CQCI01-N - PTIT
 * ═══════════════════════════════════════════════════════════════════════════════
 * File này tập hợp toàn bộ 15 màn hình trong ứng dụng SpendTogether.
 * Khi mở file này trong Android Studio và chuyển sang chế độ "Split" hoặc "Design",
 * bạn có thể duyệt trực quan toàn bộ 15 màn hình từ Màn hình chào đến Cài đặt.
 */

// ==========================================
// 1. CHUỖI KHỞI ĐỘNG & ONBOARDING (01 -> 04)
// ==========================================

@Preview(
    name = "01 · Màn hình Chào (Welcome)",
    group = "01. Onboarding",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PreviewGallery01Welcome() {
    Nhoms8Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            WelcomeScreen()
        }
    }
}

@Preview(
    name = "02 · Onboarding 1 - Quản lý chi tiêu",
    group = "01. Onboarding",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PreviewGallery02Onboarding1() {
    Nhoms8Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            OnboardingScreen1()
        }
    }
}

@Preview(
    name = "03 · Onboarding 2 - Chia hóa đơn",
    group = "01. Onboarding",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PreviewGallery03Onboarding2() {
    Nhoms8Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            OnboardingScreen2()
        }
    }
}

@Preview(
    name = "04 · Onboarding 3 - Báo cáo trực quan",
    group = "01. Onboarding",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PreviewGallery04Onboarding3() {
    Nhoms8Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            OnboardingScreen3()
        }
    }
}

// ==========================================
// 2. XÁC THỰC TÀI KHOẢN FIREBASE (05 -> 06)
// ==========================================

@Preview(
    name = "05 · Đăng nhập (Firebase Auth)",
    group = "02. Authentication",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PreviewGallery05Login() {
    Nhoms8Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            LoginScreen()
        }
    }
}

@Preview(
    name = "06 · Đăng ký tài khoản (Firebase Auth)",
    group = "02. Authentication",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PreviewGallery06SignUp() {
    Nhoms8Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            SignUpScreen()
        }
    }
}

// ==========================================
// 3. MÀN HÌNH CHÍNH & THÀNH VIÊN (07 -> 09)
// ==========================================

@Preview(
    name = "07 · Tổng quan quỹ nhóm (Dashboard Home)",
    group = "03. Quỹ & Thành viên",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PreviewGallery07Home() {
    Nhoms8Theme {
        HomeScreen()
    }
}

@Preview(
    name = "08 · Danh sách thành viên nhóm (Members)",
    group = "03. Quỹ & Thành viên",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PreviewGallery08Members() {
    Nhoms8Theme {
        MembersScreen()
    }
}

@Preview(
    name = "09 · Chỉnh sửa thành viên (Edit Member)",
    group = "03. Quỹ & Thành viên",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PreviewGallery09EditMember() {
    Nhoms8Theme {
        EditMemberScreen()
    }
}

// ==========================================
// 4. TÍNH NĂNG CHI TIÊU & CHIA TIỀN (10 -> 12)
// ==========================================

@Preview(
    name = "10 · Tạo khoản chi tiêu (Create Expense)",
    group = "04. Chi tiêu & Chia tiền",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PreviewGallery10CreateExpense() {
    Nhoms8Theme {
        CreateExpenseScreen()
    }
}

@Preview(
    name = "11 · Tự tính chia tiền nhóm (Split Bill)",
    group = "04. Chi tiêu & Chia tiền",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PreviewGallery11SplitBill() {
    Nhoms8Theme {
        SplitBillScreen()
    }
}

@Preview(
    name = "12 · Quyết toán & Nhắc nợ (Settlement)",
    group = "04. Chi tiêu & Chia tiền",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PreviewGallery12Settlement() {
    Nhoms8Theme {
        SettlementScreen()
    }
}

// ==========================================
// 5. LỊCH SỬ, THỐNG KÊ & HỒ SƠ (13 -> 15)
// ==========================================

@Preview(
    name = "13 · Lịch sử giao dịch (Transaction History)",
    group = "05. Lịch sử & Báo cáo",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PreviewGallery13History() {
    Nhoms8Theme {
        TransactionHistoryScreen()
    }
}

@Preview(
    name = "14 · Báo cáo & Thống kê tài chính (Financial Report)",
    group = "05. Lịch sử & Báo cáo",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PreviewGallery14Report() {
    Nhoms8Theme {
        FinancialReportScreen()
    }
}

@Preview(
    name = "15 · Hồ sơ cá nhân & Cài đặt (Profile & Settings)",
    group = "05. Lịch sử & Báo cáo",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PreviewGallery15Profile() {
    Nhoms8Theme {
        ProfileScreen()
    }
}
