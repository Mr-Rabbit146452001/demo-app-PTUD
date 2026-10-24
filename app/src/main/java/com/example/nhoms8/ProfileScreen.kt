package com.example.nhoms8

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhoms8.ui.theme.Nhoms8Theme

/**
 * 15 · Màn hình Cá nhân và cài đặt (Profile & Settings Screen)
 * Thiết kế chuẩn ứng dụng SpendTogether theo Figma:
 * - Nền be kem ấm áp, mảng cong xanh xô thơm
 * - Header: "Cá nhân", dòng phụ "Tài khoản & cài đặt", nút tròn mã QR
 * - Thẻ hồ sơ người dùng xanh rêu sẫm: Avatar "MA", tên "Nguyễn Minh Anh", vai trò, nút chỉnh sửa
 * - Thẻ Nhóm đang hoạt động: Icon nhà tim, "Nhà Mình", "5 thành viên · 12.680.000 đ", nút "Đổi nhóm"
 * - Thẻ Tùy chỉnh: Thông báo giao dịch (Toggle), Giao diện, Đơn vị tiền tệ
 * - Thẻ Bảo mật & Hỗ trợ: Bảo mật tài khoản, Trung tâm trợ giúp, Đăng xuất (màu đỏ)
 * - Thanh điều hướng đáy (Bottom Navigation Bar) cố định với tab Cá nhân đang active
 */
@Composable
fun ProfileScreen(
    userName: String = "Nguyễn Minh Anh",
    userInitials: String = "MA",
    userSubtitle: String = "0912 345 678 · Trưởng nhóm",
    groupName: String = "Nhà Mình",
    groupSubtitle: String = "5 thành viên · 12.680.000 đ",
    onQrCodeClicked: () -> Unit = {},
    onEditProfileClicked: () -> Unit = {},
    onChangeGroupClicked: () -> Unit = {},
    onThemeClicked: () -> Unit = {},
    onCurrencyClicked: () -> Unit = {},
    onSecurityClicked: () -> Unit = {},
    onHelpClicked: () -> Unit = {},
    onLogoutClicked: () -> Unit = {},
    onNavTabSelected: (BottomNavTab) -> Unit = {}
) {
    // Bảng màu thiết kế chuẩn Figma SpendTogether
    val backgroundColor = Color(0xFFF5F3EC)      // Màu be kem nhạt ấm áp
    val ambientSage = Color(0xFFDDEAE0)          // Màu xanh xô thơm nhạt trang trí
    val forestGreenDark = Color(0xFF14382C)      // Xanh rêu sẫm thương hiệu
    val textMuted = Color(0xFF6E7C74)            // Màu xám xanh phụ đề
    val sectionLabelColor = Color(0xFF5E8C76)    // Màu nhãn tiêu đề section
    val iconMintBg = Color(0xFFE2F4E9)           // Nền squircle xanh mint cho icon
    val logoutRedBg = Color(0xFFFDECE9)          // Nền squircle đỏ hồng nhạt cho Đăng xuất
    val logoutRedText = Color(0xFFD9534F)        // Màu chữ & icon Đăng xuất
    val chevronColor = Color(0xFFA0ACA5)         // Màu mũi tên chevron phải

    // Trạng thái bật/tắt thông báo giao dịch
    var notificationEnabled by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // 1. Họa tiết nền cong mềm mại góc trên bên phải & dưới bên trái
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Mảng cong góc trên bên phải
            drawCircle(
                color = ambientSage,
                radius = w * 0.45f,
                center = Offset(w * 0.95f, h * 0.04f)
            )

            // Mảng cong lớn góc dưới bên trái
            drawCircle(
                color = ambientSage,
                radius = w * 0.55f,
                center = Offset(w * 0.05f, h * 0.86f)
            )
        }

        // 2. Nội dung chính toàn màn hình
        Column(modifier = Modifier.fillMaxSize()) {
            // Phần nội dung cuộn dọc
            Column(
                modifier = Modifier
                    .weight(1f)
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // ====================== 1. HEADER ======================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Cụm tiêu đề bên trái
                    Column {
                        Text(
                            text = "Cá nhân",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            color = forestGreenDark,
                            letterSpacing = (-0.4).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tài khoản & cài đặt",
                            fontSize = 13.sp,
                            color = textMuted
                        )
                    }

                    // Nút Mã QR bên phải
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(
                                elevation = 3.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.06f),
                                spotColor = Color.Black.copy(alpha = 0.08f)
                            )
                            .background(Color.White, CircleShape)
                            .clickable { onQrCodeClicked() },
                        contentAlignment = Alignment.Center
                    ) {
                        QrCodeIcon(
                            color = forestGreenDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ====================== 2. THẺ HỒ SƠ NGƯỜI DÙNG ======================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(22.dp),
                            ambientColor = forestGreenDark.copy(alpha = 0.2f),
                            spotColor = forestGreenDark.copy(alpha = 0.25f)
                        )
                        .background(forestGreenDark, RoundedCornerShape(22.dp))
                        .padding(horizontal = 18.dp, vertical = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Cụm Avatar + Tên + Thông tin
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Avatar tròn nền xanh bạc hà nhạt
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(Color(0xFFCBE8D6), CircleShape)
                                    .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userInitials,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = forestGreenDark
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Họ tên và chi tiết
                            Column {
                                Text(
                                    text = userName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = (-0.2).sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = userSubtitle,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFFA8C5B8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Nút chỉnh sửa tròn kính mờ
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color.White.copy(alpha = 0.12f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                            .clickable { onEditProfileClicked() },
                            contentAlignment = Alignment.Center
                        ) {
                            PencilEditIcon(
                                color = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ====================== 3. THẺ NHÓM ĐANG HOẠT ĐỘNG ======================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(22.dp),
                            ambientColor = Color.Black.copy(alpha = 0.04f),
                            spotColor = Color.Black.copy(alpha = 0.06f)
                        )
                        .background(Color.White, RoundedCornerShape(22.dp))
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Nhãn tiêu đề thẻ
                        Text(
                            text = "NHÓM ĐANG HOẠT ĐỘNG",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = sectionLabelColor,
                            letterSpacing = 0.8.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Cụm icon nhà tim + thông tin nhóm
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Vòng tròn nền xanh cốm nhạt chứa icon nhà tim
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color(0xFFDCE8AC), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    HomeHeartIcon(
                                        color = forestGreenDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = groupName,
                                        fontSize = 15.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = forestGreenDark
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = groupSubtitle,
                                        fontSize = 12.5.sp,
                                        color = textMuted
                                    )
                                }
                            }

                            // Nút text "Đổi nhóm"
                            Text(
                                text = "Đổi nhóm",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF387E58),
                                modifier = Modifier
                                    .clickable { onChangeGroupClicked() }
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ====================== 4. THẺ CÀI ĐẶT TÙY CHỈNH ======================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(22.dp),
                            ambientColor = Color.Black.copy(alpha = 0.04f),
                            spotColor = Color.Black.copy(alpha = 0.06f)
                        )
                        .background(Color.White, RoundedCornerShape(22.dp))
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Nhãn tiêu đề thẻ
                        Text(
                            text = "TÙY CHỈNH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = sectionLabelColor,
                            letterSpacing = 0.8.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Mục 1: Thông báo giao dịch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(iconMintBg, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    BellOutlineIcon(
                                        color = forestGreenDark,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "Thông báo giao dịch",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = forestGreenDark
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Báo khi có thu chi mới",
                                        fontSize = 12.sp,
                                        color = textMuted
                                    )
                                }
                            }

                            // Toggle Switch
                            CustomToggleSwitch(
                                checked = notificationEnabled,
                                onCheckedChange = { notificationEnabled = it },
                                activeTrackColor = forestGreenDark,
                                inactiveTrackColor = Color(0xFFD4DCD7)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Mục 2: Giao diện
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onThemeClicked() },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(iconMintBg, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    MoonThemeIcon(
                                        color = forestGreenDark,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "Giao diện",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = forestGreenDark
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Sáng · Màu xanh dịu",
                                        fontSize = 12.sp,
                                        color = textMuted
                                    )
                                }
                            }

                            ChevronRightIcon(
                                color = chevronColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Mục 3: Đơn vị tiền tệ
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCurrencyClicked() },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(iconMintBg, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CashCurrencyIcon(
                                        color = forestGreenDark,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "Đơn vị tiền tệ",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = forestGreenDark
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Việt Nam Đồng (đ)",
                                        fontSize = 12.sp,
                                        color = textMuted
                                    )
                                }
                            }

                            ChevronRightIcon(
                                color = chevronColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ====================== 5. THẺ BẢO MẬT & HỖ TRỢ ======================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(22.dp),
                            ambientColor = Color.Black.copy(alpha = 0.04f),
                            spotColor = Color.Black.copy(alpha = 0.06f)
                        )
                        .background(Color.White, RoundedCornerShape(22.dp))
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Nhãn tiêu đề thẻ
                        Text(
                            text = "BẢO MẬT & HỖ TRỢ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = sectionLabelColor,
                            letterSpacing = 0.8.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Mục 1: Bảo mật tài khoản
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSecurityClicked() },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(iconMintBg, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ShieldCheckIcon(
                                        color = forestGreenDark,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "Bảo mật tài khoản",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = forestGreenDark
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Mật khẩu và sinh trắc học",
                                        fontSize = 12.sp,
                                        color = textMuted
                                    )
                                }
                            }

                            ChevronRightIcon(
                                color = chevronColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Mục 2: Trung tâm trợ giúp
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onHelpClicked() },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(iconMintBg, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    HelpCircleIcon(
                                        color = forestGreenDark,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = "Trung tâm trợ giúp",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = forestGreenDark
                                )
                            }

                            ChevronRightIcon(
                                color = chevronColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Mục 3: Đăng xuất
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLogoutClicked() },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(logoutRedBg, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    LogoutIcon(
                                        color = logoutRedText,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = "Đăng xuất",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = logoutRedText
                                )
                            }

                            ChevronRightIcon(
                                color = chevronColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // ====================== 6. THANH ĐIỀU HƯỚNG ĐÁY ======================
            ProfileBottomNavBar(
                selectedTab = BottomNavTab.PROFILE,
                onTabSelected = onNavTabSelected
            )
        }
    }
}

/**
 * Thanh điều hướng đáy đồng bộ với hệ thống SpendTogether
 */
@Composable
private fun ProfileBottomNavBar(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit
) {
    val activeColor = Color(0xFF14382C)
    val inactiveColor = Color(0xFF7F8E85)
    val activePillBg = Color(0xFFCBE7D7)

    Surface(
        color = Color(0xFFF5F3EC),
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Trang chủ
            ProfileBottomNavItem(
                title = "Trang chủ",
                isSelected = selectedTab == BottomNavTab.HOME,
                activePillBg = activePillBg,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                icon = { color -> HomeNavIcon(modifier = Modifier.size(20.dp), color = color) },
                onClick = { onTabSelected(BottomNavTab.HOME) }
            )

            // Tab 2: Lịch sử
            ProfileBottomNavItem(
                title = "Lịch sử",
                isSelected = selectedTab == BottomNavTab.HISTORY,
                activePillBg = activePillBg,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                icon = { color -> HistoryNavIcon(modifier = Modifier.size(20.dp), color = color) },
                onClick = { onTabSelected(BottomNavTab.HISTORY) }
            )

            // Tab 3: Báo cáo
            ProfileBottomNavItem(
                title = "Báo cáo",
                isSelected = selectedTab == BottomNavTab.REPORTS,
                activePillBg = activePillBg,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                icon = { color -> ReportsNavIcon(modifier = Modifier.size(20.dp), color = color) },
                onClick = { onTabSelected(BottomNavTab.REPORTS) }
            )

            // Tab 4: Cá nhân (Active)
            ProfileBottomNavItem(
                title = "Cá nhân",
                isSelected = selectedTab == BottomNavTab.PROFILE,
                activePillBg = activePillBg,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                icon = { color -> ProfileNavIcon(modifier = Modifier.size(20.dp), color = color) },
                onClick = { onTabSelected(BottomNavTab.PROFILE) }
            )
        }
    }
}

@Composable
private fun ProfileBottomNavItem(
    title: String,
    isSelected: Boolean,
    activePillBg: Color,
    activeColor: Color,
    inactiveColor: Color,
    icon: @Composable (Color) -> Unit,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(activePillBg)
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                icon(activeColor)
            }
        } else {
            Box(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                icon(inactiveColor)
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = title,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) activeColor else inactiveColor
        )
    }
}

/**
 * Công tắc Toggle Switch tùy biến
 */
@Composable
private fun CustomToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    activeTrackColor: Color,
    inactiveTrackColor: Color
) {
    val trackWidth = 50.dp
    val trackHeight = 28.dp
    val thumbSize = 22.dp
    val thumbPadding = 3.dp

    val trackColor by animateColorAsState(
        targetValue = if (checked) activeTrackColor else inactiveTrackColor,
        animationSpec = tween(durationMillis = 200),
        label = "switch_track_color"
    )

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize - thumbPadding else thumbPadding,
        animationSpec = tween(durationMillis = 200),
        label = "switch_thumb_offset"
    )

    Box(
        modifier = Modifier
            .size(width = trackWidth, height = trackHeight)
            .clip(CircleShape)
            .background(trackColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(thumbSize)
                .shadow(elevation = 2.dp, shape = CircleShape)
                .background(Color.White, CircleShape)
        )
    }
}

// =========================================================================
// CUSTOM CANVAS ICONS (Vẽ tự thân 100%, không phụ thuộc tài nguyên XML ngoài)
// =========================================================================

/**
 * Icon mã QR (QR Code Icon)
 */
@Composable
private fun QrCodeIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.6.dp.toPx()

        // Khung góc trên trái
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.14f, h * 0.14f),
            size = Size(w * 0.30f, h * 0.30f),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )
        drawRect(
            color = color,
            topLeft = Offset(w * 0.22f, h * 0.22f),
            size = Size(w * 0.14f, h * 0.14f)
        )

        // Khung góc trên phải
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.56f, h * 0.14f),
            size = Size(w * 0.30f, h * 0.30f),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )
        drawRect(
            color = color,
            topLeft = Offset(w * 0.64f, h * 0.22f),
            size = Size(w * 0.14f, h * 0.14f)
        )

        // Khung góc dưới trái
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.14f, h * 0.56f),
            size = Size(w * 0.30f, h * 0.30f),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )
        drawRect(
            color = color,
            topLeft = Offset(w * 0.22f, h * 0.64f),
            size = Size(w * 0.14f, h * 0.14f)
        )

        // Các khối pixel góc dưới phải
        drawRect(
            color = color,
            topLeft = Offset(w * 0.56f, h * 0.56f),
            size = Size(w * 0.12f, h * 0.12f)
        )
        drawRect(
            color = color,
            topLeft = Offset(w * 0.74f, h * 0.56f),
            size = Size(w * 0.12f, h * 0.12f)
        )
        drawRect(
            color = color,
            topLeft = Offset(w * 0.56f, h * 0.74f),
            size = Size(w * 0.12f, h * 0.12f)
        )
        drawRect(
            color = color,
            topLeft = Offset(w * 0.74f, h * 0.74f),
            size = Size(w * 0.12f, h * 0.12f)
        )
    }
}

/**
 * Icon cây bút chì chỉnh sửa (Pencil Edit Icon)
 */
@Composable
private fun PencilEditIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.6.dp.toPx()

        val pencilPath = Path().apply {
            moveTo(w * 0.22f, h * 0.78f)
            lineTo(w * 0.20f, h * 0.82f)
            lineTo(w * 0.32f, h * 0.80f)
            lineTo(w * 0.78f, h * 0.34f)
            lineTo(w * 0.66f, h * 0.22f)
            close()
        }

        drawPath(
            path = pencilPath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        drawLine(
            color = color,
            start = Offset(w * 0.56f, h * 0.32f),
            end = Offset(w * 0.68f, h * 0.44f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Icon ngôi nhà có trái tim (Home Heart Icon)
 */
@Composable
private fun HomeHeartIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.6.dp.toPx()

        // Khung nhà
        val housePath = Path().apply {
            moveTo(w * 0.5f, h * 0.18f)
            lineTo(w * 0.82f, h * 0.44f)
            lineTo(w * 0.78f, h * 0.82f)
            lineTo(w * 0.22f, h * 0.82f)
            lineTo(w * 0.18f, h * 0.44f)
            close()
        }

        drawPath(
            path = housePath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Trái tim nhỏ ở giữa nhà
        val heartPath = Path().apply {
            moveTo(w * 0.50f, h * 0.68f)
            cubicTo(w * 0.42f, h * 0.62f, w * 0.36f, h * 0.54f, w * 0.36f, h * 0.48f)
            cubicTo(w * 0.36f, h * 0.42f, w * 0.42f, h * 0.40f, w * 0.46f, h * 0.44f)
            lineTo(w * 0.50f, h * 0.48f)
            lineTo(w * 0.54f, h * 0.44f)
            cubicTo(w * 0.58f, h * 0.40f, w * 0.64f, h * 0.42f, w * 0.64f, h * 0.48f)
            cubicTo(w * 0.64f, h * 0.54f, w * 0.58f, h * 0.62f, w * 0.50f, h * 0.68f)
            close()
        }

        drawPath(
            path = heartPath,
            color = color
        )
    }
}

/**
 * Icon quả chuông viền nét (Bell Outline Icon)
 */
@Composable
private fun BellOutlineIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.6.dp.toPx()

        val bellPath = Path().apply {
            moveTo(w * 0.5f, h * 0.18f)
            cubicTo(w * 0.36f, h * 0.18f, w * 0.28f, h * 0.36f, w * 0.28f, h * 0.56f)
            cubicTo(w * 0.28f, h * 0.68f, w * 0.22f, h * 0.74f, w * 0.22f, h * 0.78f)
            lineTo(w * 0.78f, h * 0.78f)
            cubicTo(w * 0.78f, h * 0.74f, w * 0.72f, h * 0.68f, w * 0.72f, h * 0.56f)
            cubicTo(w * 0.72f, h * 0.36f, w * 0.64f, h * 0.18f, w * 0.5f, h * 0.18f)
            close()
        }
        drawPath(
            path = bellPath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Quai trên
        drawCircle(
            color = color,
            radius = w * 0.05f,
            center = Offset(w * 0.5f, h * 0.14f),
            style = Stroke(width = strokeWidth * 0.9f)
        )

        // Quả lắc
        val clapperPath = Path().apply {
            moveTo(w * 0.44f, h * 0.84f)
            cubicTo(w * 0.44f, h * 0.89f, w * 0.56f, h * 0.89f, w * 0.56f, h * 0.84f)
        }
        drawPath(
            path = clapperPath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

/**
 * Icon mặt trăng khuyết kèm sao (Moon Theme Icon)
 */
@Composable
private fun MoonThemeIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.6.dp.toPx()

        // Mặt trăng khuyết
        val moonPath = Path().apply {
            moveTo(w * 0.56f, h * 0.20f)
            cubicTo(w * 0.32f, h * 0.24f, w * 0.22f, h * 0.48f, w * 0.26f, h * 0.68f)
            cubicTo(w * 0.30f, h * 0.84f, w * 0.48f, h * 0.90f, w * 0.66f, h * 0.84f)
            cubicTo(w * 0.54f, h * 0.78f, w * 0.48f, h * 0.64f, w * 0.50f, h * 0.48f)
            cubicTo(w * 0.52f, h * 0.34f, w * 0.62f, h * 0.24f, w * 0.74f, h * 0.22f)
            cubicTo(w * 0.68f, h * 0.20f, w * 0.62f, h * 0.20f, w * 0.56f, h * 0.20f)
            close()
        }

        drawPath(
            path = moonPath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Ngôi sao nhỏ
        val starX = w * 0.76f
        val starY = h * 0.42f
        val starRadius = w * 0.08f
        drawLine(
            color = color,
            start = Offset(starX, starY - starRadius),
            end = Offset(starX, starY + starRadius),
            strokeWidth = strokeWidth * 0.9f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(starX - starRadius, starY),
            end = Offset(starX + starRadius, starY),
            strokeWidth = strokeWidth * 0.9f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Icon đơn vị tiền tệ / tờ tiền (Cash Currency Icon)
 */
@Composable
private fun CashCurrencyIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.6.dp.toPx()

        // Khung tờ tiền bo góc
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.16f, h * 0.28f),
            size = Size(w * 0.68f, h * 0.44f),
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )

        // Vòng tròn trung tâm
        drawCircle(
            color = color,
            radius = w * 0.11f,
            center = Offset(w * 0.5f, h * 0.5f),
            style = Stroke(width = strokeWidth)
        )

        // Hai chấm nhỏ hai bên
        drawCircle(
            color = color,
            radius = 1.4.dp.toPx(),
            center = Offset(w * 0.28f, h * 0.5f)
        )
        drawCircle(
            color = color,
            radius = 1.4.dp.toPx(),
            center = Offset(w * 0.72f, h * 0.5f)
        )
    }
}

/**
 * Icon chiếc khiên bảo mật có dấu tích (Shield Check Icon)
 */
@Composable
private fun ShieldCheckIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.6.dp.toPx()

        // Thân khiên
        val shieldPath = Path().apply {
            moveTo(w * 0.5f, h * 0.18f)
            lineTo(w * 0.78f, h * 0.28f)
            cubicTo(w * 0.78f, h * 0.58f, w * 0.66f, h * 0.76f, w * 0.5f, h * 0.86f)
            cubicTo(w * 0.34f, h * 0.76f, w * 0.22f, h * 0.58f, w * 0.22f, h * 0.28f)
            close()
        }

        drawPath(
            path = shieldPath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Dấu tích bên trong
        val checkPath = Path().apply {
            moveTo(w * 0.36f, h * 0.52f)
            lineTo(w * 0.46f, h * 0.62f)
            lineTo(w * 0.64f, h * 0.42f)
        }
        drawPath(
            path = checkPath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/**
 * Icon trung tâm trợ giúp / Dấu hỏi tròn (Help Circle Icon)
 */
@Composable
private fun HelpCircleIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.6.dp.toPx()

        // Vòng tròn ngoài
        drawCircle(
            color = color,
            radius = w * 0.38f,
            center = Offset(w * 0.5f, h * 0.5f),
            style = Stroke(width = strokeWidth)
        )

        // Dấu hỏi
        val questionPath = Path().apply {
            moveTo(w * 0.42f, h * 0.40f)
            cubicTo(w * 0.42f, h * 0.32f, w * 0.58f, h * 0.32f, w * 0.58f, h * 0.42f)
            cubicTo(w * 0.58f, h * 0.50f, w * 0.50f, h * 0.52f, w * 0.50f, h * 0.60f)
        }
        drawPath(
            path = questionPath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Chấm tròn dấu hỏi
        drawCircle(
            color = color,
            radius = 1.3.dp.toPx(),
            center = Offset(w * 0.5f, h * 0.69f)
        )
    }
}

/**
 * Icon đăng xuất (Logout Icon)
 */
@Composable
private fun LogoutIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.6.dp.toPx()

        // Khung cửa mở (ba cạnh)
        val doorPath = Path().apply {
            moveTo(w * 0.48f, h * 0.22f)
            lineTo(w * 0.24f, h * 0.22f)
            lineTo(w * 0.24f, h * 0.78f)
            lineTo(w * 0.48f, h * 0.78f)
        }
        drawPath(
            path = doorPath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Mũi tên hướng ra ngoài bên phải
        drawLine(
            color = color,
            start = Offset(w * 0.40f, h * 0.50f),
            end = Offset(w * 0.78f, h * 0.50f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(w * 0.64f, h * 0.36f),
            end = Offset(w * 0.78f, h * 0.50f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(w * 0.64f, h * 0.64f),
            end = Offset(w * 0.78f, h * 0.50f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Icon mũi tên chevron phải `>`
 */
@Composable
private fun ChevronRightIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.8.dp.toPx()

        val path = Path().apply {
            moveTo(w * 0.32f, h * 0.22f)
            lineTo(w * 0.68f, h * 0.50f)
            lineTo(w * 0.32f, h * 0.78f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

// Icons dùng chung cho Bottom Navigation Bar
@Composable
private fun HomeNavIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.6.dp.toPx()

        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.18f)
            lineTo(w * 0.84f, h * 0.48f)
            lineTo(w * 0.76f, h * 0.82f)
            lineTo(w * 0.24f, h * 0.82f)
            lineTo(w * 0.16f, h * 0.48f)
            close()
        }
        drawPath(path, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun HistoryNavIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.6.dp.toPx()

        val path = Path().apply {
            moveTo(w * 0.22f, h * 0.2f)
            lineTo(w * 0.78f, h * 0.2f)
            lineTo(w * 0.78f, h * 0.8f)
            lineTo(w * 0.68f, h * 0.74f)
            lineTo(w * 0.58f, h * 0.8f)
            lineTo(w * 0.48f, h * 0.74f)
            lineTo(w * 0.38f, h * 0.8f)
            lineTo(w * 0.22f, h * 0.74f)
            close()
        }
        drawPath(path, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawLine(color = color, start = Offset(w * 0.34f, h * 0.38f), end = Offset(w * 0.66f, h * 0.38f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.34f, h * 0.52f), end = Offset(w * 0.56f, h * 0.52f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
    }
}

@Composable
private fun ReportsNavIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.6.dp.toPx()

        drawCircle(color = color, radius = w * 0.36f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = strokeWidth))
        drawLine(color = color, start = Offset(w * 0.5f, h * 0.5f), end = Offset(w * 0.5f, h * 0.16f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.5f, h * 0.5f), end = Offset(w * 0.78f, h * 0.64f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
    }
}

@Composable
private fun ProfileNavIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.6.dp.toPx()

        drawCircle(color = color, radius = w * 0.2f, center = Offset(w * 0.5f, h * 0.34f), style = Stroke(width = strokeWidth))
        val body = Path().apply {
            moveTo(w * 0.24f, h * 0.80f)
            cubicTo(w * 0.26f, h * 0.60f, w * 0.38f, h * 0.56f, w * 0.50f, h * 0.56f)
            cubicTo(w * 0.62f, h * 0.56f, w * 0.74f, h * 0.60f, w * 0.76f, h * 0.80f)
        }
        drawPath(body, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun ProfileScreenPreview() {
    Nhoms8Theme {
        ProfileScreen()
    }
}
