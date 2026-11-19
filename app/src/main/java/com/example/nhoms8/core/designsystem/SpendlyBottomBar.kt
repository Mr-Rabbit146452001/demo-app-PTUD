package com.example.nhoms8.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhoms8.ui.theme.SpendlyIndigo
import com.example.nhoms8.ui.theme.SpendlyLavender
import com.example.nhoms8.ui.theme.SpendlyLavenderDeep
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlyTextStyles
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.HazeState

/**
 * 5-tab navigation model matching the Spendly Sitemap:
 * HOME -> PROFILE -> ADD -> GROUPS -> NOTIFICATIONS.
 */
enum class SpendlyTab(val label: String) {
    HOME("Trang chủ"),
    PROFILE("Cá nhân"),
    ADD("Thêm"),
    GROUPS("Các quỹ"),
    NOTIFICATIONS("Thông báo")
}

/**
 * Spendly frosted-glass 5-tab bottom navigation bar with a centered elevated gradient ADD button
 * and optional unread badge on [SpendlyTab.NOTIFICATIONS].
 */
@Composable
fun SpendlyBottomBar(
    selectedTab: SpendlyTab,
    onTabSelected: (SpendlyTab) -> Unit,
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit = { onTabSelected(SpendlyTab.ADD) },
    unreadNotificationsCount: Int = 0,
    hazeState: HazeState? = null
) {
    val colors = MaterialTheme.spendlyColors
    val barShape = SpendlyShapeTokens.BottomBar

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .height(82.dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = barShape,
                    ambientColor = SpendlyLavenderDeep.copy(alpha = 0.22f),
                    spotColor = SpendlyLavenderDeep.copy(alpha = 0.22f)
                )
                .spendlyGlassSurface(
                    hazeState = hazeState,
                    shape = barShape,
                    tintColor = colors.glassContainer
                )
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarTabItem(
                tab = SpendlyTab.HOME,
                selected = selectedTab == SpendlyTab.HOME,
                selectedIcon = Icons.Rounded.Home,
                unselectedIcon = Icons.Outlined.Home,
                onClick = { onTabSelected(SpendlyTab.HOME) },
                modifier = Modifier.weight(1f)
            )
            BottomBarTabItem(
                tab = SpendlyTab.PROFILE,
                selected = selectedTab == SpendlyTab.PROFILE,
                selectedIcon = Icons.Rounded.Person,
                unselectedIcon = Icons.Outlined.Person,
                onClick = { onTabSelected(SpendlyTab.PROFILE) },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(68.dp))

            BottomBarTabItem(
                tab = SpendlyTab.GROUPS,
                selected = selectedTab == SpendlyTab.GROUPS,
                selectedIcon = Icons.Rounded.AccountBalanceWallet,
                unselectedIcon = Icons.Outlined.AccountBalanceWallet,
                onClick = { onTabSelected(SpendlyTab.GROUPS) },
                modifier = Modifier.weight(1f)
            )
            BottomBarTabItem(
                tab = SpendlyTab.NOTIFICATIONS,
                selected = selectedTab == SpendlyTab.NOTIFICATIONS,
                selectedIcon = Icons.Rounded.Notifications,
                unselectedIcon = Icons.Outlined.Notifications,
                badgeCount = unreadNotificationsCount,
                onClick = { onTabSelected(SpendlyTab.NOTIFICATIONS) },
                modifier = Modifier.weight(1f)
            )
        }

        CenterAddFloatingButton(
            onClick = onAddClick,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun BottomBarTabItem(
    tab: SpendlyTab,
    selected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0
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
                contentDescription = tab.label,
                tint = tint,
                modifier = Modifier.size(21.dp)
            )

            if (badgeCount > 0) {
                val badgeText = if (badgeCount > 9) "9+" else badgeCount.toString()
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-3).dp)
                        .clip(CircleShape)
                        .background(colors.coralDeep)
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeText,
                        style = SpendlyTextStyles.moneyCaption.copy(
                            fontSize = 9.sp,
                            lineHeight = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = tint,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CenterAddFloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    val haloColor = colors.surface.copy(alpha = if (colors.isDark) 0.90f else 0.95f)
    val gradient = Brush.linearGradient(
        colors = listOf(SpendlyLavender, SpendlyLavenderDeep, SpendlyIndigo)
    )

    Box(
        modifier = modifier
            .size(56.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = SpendlyLavenderDeep.copy(alpha = 0.45f),
                spotColor = SpendlyIndigo.copy(alpha = 0.45f)
            )
            .clip(CircleShape)
            .background(brush = gradient, shape = CircleShape)
            .border(width = 3.dp, color = haloColor, shape = CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = SpendlyTab.ADD.label,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}
