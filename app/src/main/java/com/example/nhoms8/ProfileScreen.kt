package com.example.nhoms8

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.GradientCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.core.designsystem.spendlyGlassSurface
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState

/**
 * 15 · Màn hình Cá nhân và cài đặt (Profile & Settings Screen)
 * Đồng bộ hoàn toàn theo hệ thống thiết kế Spendly Lavender / Organic / Haze 2.1.0:
 * - Header "Cá nhân" + nút mã QR kính mờ
 * - Thẻ GradientCard hồ sơ người dùng
 * - Thẻ GlassCard "Tài chính cá nhân" (Lịch sử, Ngân sách, Công nợ)
 * - Thẻ GlassCard "Nhóm đang hoạt động"
 * - Thẻ GlassCard "Tùy chỉnh" & "Bảo mật & Hỗ trợ"
 */
@Composable
fun ProfileScreen(
    userName: String = "Lê Văn Hùng",
    userInitials: String = "VH",
    userSubtitle: String = "0912 345 678 · Trưởng nhóm",
    groupName: String = "Quỹ Chi Tiêu Nhóm 08",
    groupSubtitle: String = "4 thành viên · Quỹ hoạt động",
    onQrCodeClicked: () -> Unit = {},
    onEditProfileClicked: () -> Unit = {},
    onChangeGroupClicked: () -> Unit = {},
    onThemeClicked: () -> Unit = {},
    onCurrencyClicked: () -> Unit = {},
    onSecurityClicked: () -> Unit = {},
    onHelpClicked: () -> Unit = {},
    onLogoutClicked: () -> Unit = {},
    onNavTabSelected: (BottomNavTab) -> Unit = {},
    showBottomBar: Boolean = true,
    onPersonalHistoryClicked: () -> Unit = {},
    onPersonalBudgetClicked: () -> Unit = {},
    onPersonalDebtsClicked: () -> Unit = {},
    personalDebtsSummary: String? = null
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()
    var notificationEnabled by remember { mutableStateOf(true) }

    OrganicBackground(
        modifier = Modifier.fillMaxSize(),
        hazeState = hazeState
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                // 1. HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Cá nhân",
                            style = MaterialTheme.typography.headlineLarge,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tài khoản & tài chính cá nhân",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .spendlyGlassSurface(
                                hazeState = hazeState,
                                shape = CircleShape,
                                tintColor = colors.glassContainer
                            )
                            .clickable(role = Role.Button, onClick = onQrCodeClicked),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.QrCode2,
                            contentDescription = "Mã QR nhận tiền",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }

                // 2. HERO USER PROFILE GRADIENT CARD
                GradientCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(18.dp)
                ) {
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
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.24f))
                                    .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userInitials,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = userName,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = userSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f))
                                .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape)
                                .clickable(role = Role.Button, onClick = onEditProfileClicked),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Chỉnh sửa hồ sơ",
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }

                // 3. TÀI CHÍNH CÁ NHÂN (PERSONAL FINANCE SHORTCUTS)
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    hazeState = hazeState,
                    contentPadding = PaddingValues(18.dp)
                ) {
                    SectionCaption(text = "TÀI CHÍNH CÁ NHÂN")
                    Spacer(modifier = Modifier.height(14.dp))

                    ProfileMenuRow(
                        icon = Icons.Rounded.History,
                        iconBg = colors.lavenderContainer,
                        iconTint = colors.onLavenderContainer,
                        title = "Lịch sử giao dịch",
                        subtitle = "Xem tất cả khoản thu chi cá nhân & nhóm",
                        onClick = onPersonalHistoryClicked
                    )

                    ProfileSectionDivider()

                    ProfileMenuRow(
                        icon = Icons.Rounded.Savings,
                        iconBg = colors.mintContainer,
                        iconTint = colors.mintDeep,
                        title = "Ngân sách & Báo cáo",
                        subtitle = "Theo dõi hạn mức và xu hướng chi tiêu",
                        onClick = onPersonalBudgetClicked
                    )

                    ProfileSectionDivider()

                    ProfileMenuRow(
                        icon = Icons.Rounded.SwapHoriz,
                        iconBg = colors.amberContainer,
                        iconTint = colors.amberDeep,
                        title = "Công nợ & Quyết toán",
                        subtitle = personalDebtsSummary?.takeIf { it.isNotBlank() }
                            ?: "Kiểm tra khoản cần trả hoặc được hoàn lại",
                        onClick = onPersonalDebtsClicked
                    )
                }

                // 4. NHÓM ĐANG HOẠT ĐỘNG
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    hazeState = hazeState,
                    contentPadding = PaddingValues(18.dp)
                ) {
                    SectionCaption(text = "NHÓM ĐANG HOẠT ĐỘNG")
                    Spacer(modifier = Modifier.height(12.dp))

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
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(colors.lavenderContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = colors.onLavenderContainer,
                                    modifier = Modifier.size(21.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = groupName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = groupSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(SpendlyShapeTokens.Pill)
                                .background(colors.lavenderContainer)
                                .clickable(role = Role.Button, onClick = onChangeGroupClicked)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Đổi nhóm",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.onLavenderContainer
                            )
                        }
                    }
                }

                // 5. TÙY CHỈNH
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    hazeState = hazeState,
                    contentPadding = PaddingValues(18.dp)
                ) {
                    SectionCaption(text = "TÙY CHỈNH")
                    Spacer(modifier = Modifier.height(14.dp))

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
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.lavenderContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsNone,
                                    contentDescription = null,
                                    tint = colors.onLavenderContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Thông báo giao dịch",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Báo khi có thu chi mới",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted
                                )
                            }
                        }

                        Switch(
                            checked = notificationEnabled,
                            onCheckedChange = { notificationEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.primaryLavender,
                                uncheckedThumbColor = colors.textMuted,
                                uncheckedTrackColor = colors.divider
                            )
                        )
                    }

                    ProfileSectionDivider()

                    ProfileMenuRow(
                        icon = Icons.Rounded.Palette,
                        iconBg = colors.lavenderContainer,
                        iconTint = colors.onLavenderContainer,
                        title = "Giao diện",
                        subtitle = if (colors.isDark) "Tối · Lavender Organic" else "Sáng · Lavender Pastel",
                        onClick = onThemeClicked
                    )

                    ProfileSectionDivider()

                    ProfileMenuRow(
                        icon = Icons.Rounded.CurrencyExchange,
                        iconBg = colors.mintContainer,
                        iconTint = colors.mintDeep,
                        title = "Đơn vị tiền tệ",
                        subtitle = "Việt Nam Đồng (đ)",
                        onClick = onCurrencyClicked
                    )
                }

                // 6. BẢO MẬT & HỖ TRỢ
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    hazeState = hazeState,
                    contentPadding = PaddingValues(18.dp)
                ) {
                    SectionCaption(text = "BẢO MẬT & HỖ TRỢ")
                    Spacer(modifier = Modifier.height(14.dp))

                    ProfileMenuRow(
                        icon = Icons.Rounded.Shield,
                        iconBg = colors.lavenderContainer,
                        iconTint = colors.onLavenderContainer,
                        title = "Bảo mật tài khoản",
                        subtitle = "Mật khẩu và sinh trắc học",
                        onClick = onSecurityClicked
                    )

                    ProfileSectionDivider()

                    ProfileMenuRow(
                        icon = Icons.AutoMirrored.Rounded.HelpOutline,
                        iconBg = colors.sky.copy(alpha = if (colors.isDark) 0.22f else 0.16f),
                        iconTint = colors.sky,
                        title = "Trung tâm trợ giúp",
                        subtitle = "Hướng dẫn sử dụng Spendly",
                        onClick = onHelpClicked
                    )

                    ProfileSectionDivider()

                    ProfileMenuRow(
                        icon = Icons.AutoMirrored.Rounded.Logout,
                        iconBg = colors.coralContainer,
                        iconTint = colors.coralDeep,
                        title = "Đăng xuất",
                        titleColor = colors.coralDeep,
                        subtitle = null,
                        onClick = onLogoutClicked
                    )
                }

                if (showBottomBar) {
                    Spacer(modifier = Modifier.height(14.dp))
                } else {
                    Spacer(modifier = Modifier.height(108.dp))
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }

            if (showBottomBar) {
                ProfileLavenderBottomNavBar(
                    selectedTab = BottomNavTab.PROFILE,
                    hazeState = hazeState,
                    onTabSelected = onNavTabSelected
                )
            }
        }
    }
}

@Composable
private fun SectionCaption(text: String) {
    val colors = MaterialTheme.spendlyColors
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        ),
        color = colors.deepLavender
    )
}

@Composable
private fun ProfileSectionDivider() {
    val colors = MaterialTheme.spendlyColors
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 12.dp),
        thickness = 0.8.dp,
        color = colors.divider.copy(alpha = 0.7f)
    )
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    titleColor: Color = MaterialTheme.spendlyColors.textPrimary
) {
    val colors = MaterialTheme.spendlyColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 2.dp),
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
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

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ProfileLavenderBottomNavBar(
    selectedTab: BottomNavTab,
    hazeState: HazeState,
    onTabSelected: (BottomNavTab) -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
            ProfileNavItem(
                title = "Trang chủ",
                selected = selectedTab == BottomNavTab.HOME,
                selectedIcon = Icons.Rounded.Home,
                unselectedIcon = Icons.Outlined.Home,
                onClick = { onTabSelected(BottomNavTab.HOME) },
                modifier = Modifier.weight(1f)
            )
            ProfileNavItem(
                title = "Lịch sử",
                selected = selectedTab == BottomNavTab.HISTORY,
                selectedIcon = Icons.AutoMirrored.Rounded.ReceiptLong,
                unselectedIcon = Icons.AutoMirrored.Outlined.ReceiptLong,
                onClick = { onTabSelected(BottomNavTab.HISTORY) },
                modifier = Modifier.weight(1f)
            )
            ProfileNavItem(
                title = "Báo cáo",
                selected = selectedTab == BottomNavTab.REPORTS,
                selectedIcon = Icons.Rounded.BarChart,
                unselectedIcon = Icons.Outlined.BarChart,
                onClick = { onTabSelected(BottomNavTab.REPORTS) },
                modifier = Modifier.weight(1f)
            )
            ProfileNavItem(
                title = "Cá nhân",
                selected = selectedTab == BottomNavTab.PROFILE,
                selectedIcon = Icons.Rounded.Person,
                unselectedIcon = Icons.Outlined.Person,
                onClick = { onTabSelected(BottomNavTab.PROFILE) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ProfileNavItem(
    title: String,
    selected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    val activeColor = if (colors.isDark) colors.primaryLavender else colors.deepLavender
    val tint = if (selected) activeColor else colors.textMuted
    val pillBg = if (selected) {
        colors.lavenderContainer.copy(alpha = if (colors.isDark) 0.42f else 0.85f)
    } else {
        Color.Transparent
    }
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .clip(SpendlyShapeTokens.CardMedium)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(SpendlyShapeTokens.Pill)
                .background(pillBg)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = title,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = tint,
            maxLines = 1
        )
    }
}

@Preview(
    name = "15 · Cá nhân & cài đặt Spendly (Light)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun ProfileScreenPreview() {
    SpendlyTheme(darkTheme = false) {
        ProfileScreen(
            personalDebtsSummary = "Bạn đang được trả 637.500 đ"
        )
    }
}

@Preview(
    name = "15 · Cá nhân & cài đặt Spendly (Dark)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun ProfileScreenDarkPreview() {
    SpendlyTheme(darkTheme = true) {
        ProfileScreen(
            personalDebtsSummary = "Bạn đang nợ 232.500 đ"
        )
    }
}
