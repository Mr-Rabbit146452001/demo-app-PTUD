package com.example.nhoms8.feature.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.domain.model.GroupActivity
import com.example.nhoms8.domain.model.SpendlyNotification
import com.example.nhoms8.feature.groups.GroupViewModel
import com.example.nhoms8.feature.groups.NotificationActivityItem
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class NotificationFeedFilter(val label: String) {
    ALL("Tất cả"),
    NOTIFICATIONS("Thông báo"),
    ACTIVITIES("Hoạt động quỹ")
}

/**
 * Màn hình Thông báo & Nhật ký hoạt động các quỹ nhóm.
 */
@Composable
fun NotificationsScreen(
    viewModel: GroupViewModel,
    onNavigateToGroup: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val recentActivities by viewModel.recentActivities.collectAsStateWithLifecycle()

    NotificationsContent(
        notifications = notifications,
        recentActivities = recentActivities,
        onNotificationClick = { notification ->
            viewModel.markNotificationRead(notification.id)
            if (notification.groupId.isNotBlank()) {
                viewModel.setActiveGroup(notification.groupId)
                onNavigateToGroup(notification.groupId)
            }
        },
        onActivityClick = { activity ->
            if (activity.groupId.isNotBlank()) {
                viewModel.setActiveGroup(activity.groupId)
                onNavigateToGroup(activity.groupId)
            }
        },
        onMarkAllReadClick = viewModel::markAllNotificationsRead,
        modifier = modifier
    )
}

/**
 * Giao diện thuần trạng thái cho màn hình Thông báo & Hoạt động nhóm.
 */
@Composable
fun NotificationsContent(
    notifications: List<SpendlyNotification>,
    recentActivities: List<NotificationActivityItem>,
    onNotificationClick: (SpendlyNotification) -> Unit = {},
    onActivityClick: (GroupActivity) -> Unit = {},
    onMarkAllReadClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()
    var filter by rememberSaveable { mutableStateOf(NotificationFeedFilter.ALL) }

    val unreadCount = notifications.count { !it.isRead }
    val showNotifications = filter != NotificationFeedFilter.ACTIVITIES
    val showActivities = filter != NotificationFeedFilter.NOTIFICATIONS

    OrganicBackground(
        modifier = modifier.fillMaxSize(),
        hazeState = hazeState
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 108.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Thông báo",
                            style = MaterialTheme.typography.headlineLarge,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (unreadCount > 0) {
                                "$unreadCount thông báo chưa đọc · ${recentActivities.size} hoạt động mới"
                            } else {
                                "Cập nhật thu chi và biến động các quỹ nhóm"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted
                        )
                    }

                    if (unreadCount > 0) {
                        Row(
                            modifier = Modifier
                                .clip(SpendlyShapeTokens.Pill)
                                .background(colors.lavenderContainer)
                                .clickable(onClick = onMarkAllReadClick)
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DoneAll,
                                contentDescription = null,
                                tint = colors.deepLavender,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Đọc hết",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.deepLavender
                            )
                        }
                    }
                }
            }

            item(key = "filter_pills") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NotificationFeedFilter.entries.forEach { option ->
                        val selected = option == filter
                        Box(
                            modifier = Modifier
                                .clip(SpendlyShapeTokens.Pill)
                                .background(
                                    if (selected) colors.deepLavender else colors.lavenderContainer
                                )
                                .clickable { filter = option }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) Color.White else colors.onLavenderContainer
                            )
                        }
                    }
                }
            }

            val isCompletelyEmpty = (!showNotifications || notifications.isEmpty()) &&
                (!showActivities || recentActivities.isEmpty())

            if (isCompletelyEmpty) {
                item(key = "empty_notifications") {
                    EmptyNotificationsCard(hazeState = hazeState)
                }
            } else {
                if (showNotifications && notifications.isNotEmpty()) {
                    item(key = "notif_section_title") {
                        Text(
                            text = "Thông báo cá nhân (${notifications.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    items(
                        items = notifications,
                        key = { "notif_${it.id}" }
                    ) { notif ->
                        NotificationItemCard(
                            notification = notif,
                            hazeState = hazeState,
                            onClick = { onNotificationClick(notif) }
                        )
                    }
                }

                if (showActivities && recentActivities.isNotEmpty()) {
                    item(key = "activity_section_title") {
                        Text(
                            text = "Hoạt động các quỹ gần đây (${recentActivities.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    items(
                        items = recentActivities,
                        key = { "act_${it.activity.groupId}_${it.activity.activityId}" }
                    ) { item ->
                        GroupActivityFeedCard(
                            item = item,
                            hazeState = hazeState,
                            onClick = { onActivityClick(item.activity) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItemCard(
    notification: SpendlyNotification,
    hazeState: HazeState,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val isUnread = !notification.isRead

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SpendlyShapeTokens.CardLarge)
            .then(
                if (isUnread) {
                    Modifier.border(
                        width = 1.4.dp,
                        color = colors.primaryLavender.copy(alpha = 0.72f),
                        shape = SpendlyShapeTokens.CardLarge
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        hazeState = hazeState,
        tintColor = if (isUnread) {
            colors.lavenderContainer.copy(alpha = if (colors.isDark) 0.42f else 0.78f)
        } else {
            colors.glassContainer
        },
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(SpendlyShapeTokens.CardSmall)
                    .background(if (isUnread) colors.deepLavender else colors.lavenderContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = notificationIconFor(notification.type),
                    contentDescription = null,
                    tint = if (isUnread) Color.White else colors.deepLavender,
                    modifier = Modifier.size(21.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold
                        ),
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (isUnread) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(colors.deepLavender)
                        )
                    }
                }

                if (notification.body.isNotBlank()) {
                    Text(
                        text = notification.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatNotificationTime(notification.createdAtMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textMuted
                    )
                    if (notification.groupId.isNotBlank()) {
                        Text(
                            text = "Mở quỹ →",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.deepLavender
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupActivityFeedCard(
    item: NotificationActivityItem,
    hazeState: HazeState,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    val activity = item.activity

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SpendlyShapeTokens.CardLarge)
            .clickable(onClick = onClick),
        hazeState = hazeState,
        contentPadding = PaddingValues(15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.mintContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = activityFeedIcon(activity.action),
                    contentDescription = null,
                    tint = colors.mintDeep,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(SpendlyShapeTokens.Pill)
                        .background(colors.lavenderContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.groupName.ifBlank { "Quỹ nhóm" },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.onLavenderContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${activity.actorName} · ${formatNotificationTime(activity.createdAtMillis)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted
                )
            }
        }
    }
}

@Composable
private fun EmptyNotificationsCard(hazeState: HazeState) {
    val colors = MaterialTheme.spendlyColors

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(SpendlyShapeTokens.CardMedium)
                    .background(colors.lavenderContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.NotificationsNone,
                    contentDescription = null,
                    tint = colors.deepLavender,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = "Chưa có thông báo mới",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary
            )
            Text(
                text = "Mọi cập nhật thành viên tham gia, khoản chi mới hoặc nhắc đối soát sẽ hiển thị tại đây.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted
            )
        }
    }
}

private fun notificationIconFor(type: String): ImageVector = when {
    type.contains("JOIN", ignoreCase = true) -> Icons.Rounded.GroupAdd
    type.contains("EXPENSE", ignoreCase = true) -> Icons.Rounded.ArrowUpward
    type.contains("SETTLE", ignoreCase = true) -> Icons.Rounded.SwapHoriz
    else -> Icons.Rounded.Notifications
}

private fun activityFeedIcon(action: String): ImageVector = when (action) {
    "CREATE_EXPENSE", "UPDATE_EXPENSE" -> Icons.Rounded.ArrowUpward
    "CANCEL_EXPENSE", "CANCEL_INCOME" -> Icons.Rounded.Cancel
    "CREATE_INCOME" -> Icons.Rounded.Savings
    "JOIN_GROUP", "ADD_MEMBER" -> Icons.Rounded.GroupAdd
    "UPDATE_GROUP_SETTINGS" -> Icons.Rounded.Settings
    else -> Icons.Rounded.Group
}

private fun formatNotificationTime(millis: Long): String {
    if (millis <= 0L) return "Vừa xong"
    val sdf = SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale.forLanguageTag("vi-VN"))
    return sdf.format(Date(millis))
}

@Preview(showBackground = true, widthDp = 412, heightDp = 840)
@Composable
private fun NotificationsScreenPreview() {
    SpendlyTheme {
        NotificationsContent(
            notifications = listOf(
                SpendlyNotification(
                    id = "n1",
                    title = "Thành viên mới tham gia Quỹ Chi Tiêu Nhóm 08",
                    body = "Lý Việt Linh vừa tham gia quỹ nhóm bằng mã mời N08PRO",
                    type = "GROUP_JOIN",
                    groupId = "grp_1",
                    isRead = false,
                    createdAtMillis = 1_730_000_000_000L
                ),
                SpendlyNotification(
                    id = "n2",
                    title = "Nhắc nhở đối soát cuối tuần",
                    body = "Bạn có khoản cần đối soát trong Quỹ Đà Lạt",
                    type = "SETTLEMENT",
                    groupId = "grp_2",
                    isRead = true,
                    createdAtMillis = 1_729_950_000_000L
                )
            ),
            recentActivities = listOf(
                NotificationActivityItem(
                    activity = GroupActivity(
                        activityId = "a1",
                        groupId = "grp_1",
                        actorId = "user_hung",
                        actorName = "Lê Văn Hùng",
                        action = "CREATE_EXPENSE",
                        description = "Lê Văn Hùng đã thêm khoản chi Mua linh kiện đồ án",
                        createdAtMillis = 1_730_000_000_000L
                    ),
                    groupName = "Quỹ Chi Tiêu Nhóm 08"
                )
            )
        )
    }
}
