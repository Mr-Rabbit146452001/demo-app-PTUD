package com.example.nhoms8

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.GradientCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.core.designsystem.SpendlyButton
import com.example.nhoms8.core.designsystem.spendlyGlassSurface
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlyShapeTokens.CardMedium
import com.example.nhoms8.ui.theme.SpendlyTextDark
import com.example.nhoms8.ui.theme.SpendlyTextStyles
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.rememberHazeState

/**
 * Trạng thái số dư tài chính của thành viên trong nhóm
 */
sealed class MemberBalanceStatus {
    data class Positive(val amountFormatted: String) : MemberBalanceStatus()
    data class Negative(val amountFormatted: String) : MemberBalanceStatus()
    data object Balanced : MemberBalanceStatus()
}

/**
 * Dữ liệu đại diện cho 1 thành viên nhóm
 */
data class GroupMember(
    val id: String,
    val name: String,
    val initials: String,
    val phoneNumber: String,
    val isLeader: Boolean = false,
    val avatarBgColor: Color,
    val balanceStatus: MemberBalanceStatus
)

/**
 * 08 · Màn hình Danh sách thành viên (Group Members Screen)
 * Đồng bộ Spendly Lavender / Organic / Haze 2.1.0 Design System.
 */
@Composable
fun MembersScreen(
    groupTitle: String = "Thành viên",
    groupSubtitle: String = "4 người trong Quỹ Chi Tiêu Nhóm 08",
    totalContributedAmount: String = "10.000.000 đ",
    membersList: List<GroupMember>? = null,
    onBackClicked: () -> Unit = {},
    onAddMemberClicked: () -> Unit = {},
    onAddMemberSubmit: ((name: String, phone: String, isLeader: Boolean) -> Unit)? = null,
    onMemberClicked: (GroupMember) -> Unit = {}
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newMemberName by remember { mutableStateOf("") }
    var newMemberPhone by remember { mutableStateOf("") }
    var newMemberIsLeader by remember { mutableStateOf(false) }

    val defaultMembers = remember {
        listOf(
            GroupMember(
                id = "1",
                name = "Lê Văn Hùng",
                initials = "VH",
                phoneNumber = "0912 345 678",
                isLeader = true,
                avatarBgColor = Color(0xFFEDE6FA),
                balanceStatus = MemberBalanceStatus.Positive("+637.500 đ")
            ),
            GroupMember(
                id = "2",
                name = "Lý Việt Linh",
                initials = "VL",
                phoneNumber = "0908 224 510",
                isLeader = false,
                avatarBgColor = Color(0xFFE1F5EE),
                balanceStatus = MemberBalanceStatus.Negative("-47.500 đ")
            ),
            GroupMember(
                id = "3",
                name = "Nguyễn Phan Tấn Đạt",
                initials = "TĐ",
                phoneNumber = "0986 420 115",
                isLeader = false,
                avatarBgColor = Color(0xFFD6ECF9),
                balanceStatus = MemberBalanceStatus.Negative("-232.500 đ")
            ),
            GroupMember(
                id = "4",
                name = "Lê Trung Bình",
                initials = "TB",
                phoneNumber = "0934 881 902",
                isLeader = false,
                avatarBgColor = Color(0xFFFDF1DC),
                balanceStatus = MemberBalanceStatus.Negative("-192.500 đ")
            )
        )
    }

    val members = membersList ?: defaultMembers

    fun triggerAddMember() {
        if (onAddMemberSubmit != null) {
            newMemberName = ""
            newMemberPhone = ""
            newMemberIsLeader = false
            showAddDialog = true
        } else {
            onAddMemberClicked()
        }
    }

    OrganicBackground(
        modifier = Modifier.fillMaxSize(),
        hazeState = hazeState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. TOP APP BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
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
                            text = groupTitle,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
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

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .spendlyGlassSurface(
                            hazeState = hazeState,
                            shape = CircleShape,
                            tintColor = colors.glassContainer
                        )
                        .clickable(role = Role.Button, onClick = ::triggerAddMember),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PersonAdd,
                        contentDescription = "Thêm thành viên",
                        tint = colors.deepLavender,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (showAddDialog) {
                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryLavender,
                    unfocusedBorderColor = colors.divider,
                    focusedLabelColor = colors.deepLavender,
                    unfocusedLabelColor = colors.textMuted,
                    cursorColor = colors.deepLavender
                )
                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    containerColor = colors.surfaceElevated,
                    title = {
                        Text(
                            text = "Thêm thành viên mới",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = newMemberName,
                                onValueChange = { newMemberName = it },
                                label = { Text("Họ và tên") },
                                singleLine = true,
                                shape = CardMedium,
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newMemberPhone,
                                onValueChange = { newMemberPhone = it },
                                label = { Text("Số điện thoại") },
                                singleLine = true,
                                shape = CardMedium,
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Vai trò Trưởng nhóm",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textPrimary
                                )
                                Switch(
                                    checked = newMemberIsLeader,
                                    onCheckedChange = { newMemberIsLeader = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = colors.primaryLavender
                                    )
                                )
                            }
                        }
                    },
                    confirmButton = {
                        SpendlyButton(
                            text = "Thêm",
                            onClick = {
                                if (newMemberName.isNotBlank()) {
                                    val phone = if (newMemberPhone.isNotBlank()) newMemberPhone else "0900 000 000"
                                    onAddMemberSubmit?.invoke(newMemberName.trim(), phone.trim(), newMemberIsLeader)
                                    showAddDialog = false
                                }
                            },
                            height = 42.dp
                        )
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Hủy", color = colors.textMuted)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 2. SUMMARY GRADIENT CARD
                GradientCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TỔNG ĐÃ GÓP",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = Color.White.copy(alpha = 0.80f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = totalContributedAmount,
                                style = SpendlyTextStyles.moneyLarge,
                                color = Color.White
                            )
                        }

                        Row(
                            modifier = Modifier
                                .clip(SpendlyShapeTokens.Pill)
                                .background(Color.White.copy(alpha = 0.18f))
                                .border(1.dp, Color.White.copy(alpha = 0.26f), SpendlyShapeTokens.Pill)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.VerifiedUser,
                                contentDescription = null,
                                tint = Color(0xFFA7F0D6),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Đã xác minh",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }
                }

                // 3. MEMBERS LIST GLASS CARD
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    hazeState = hazeState,
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    members.forEachIndexed { index, member ->
                        MemberListItemRow(
                            member = member,
                            onClick = { onMemberClicked(member) }
                        )

                        if (index < members.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                thickness = 0.8.dp,
                                color = colors.divider.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // 4. ADD MEMBER CTA
                SpendlyButton(
                    text = "Thêm thành viên",
                    onClick = ::triggerAddMember,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = Icons.Rounded.PersonAdd,
                    height = 52.dp
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun MemberListItemRow(
    member: GroupMember,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
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
                    .clip(CircleShape)
                    .background(member.avatarBgColor)
                    .border(1.dp, colors.divider, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.initials,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = SpendlyTextDark
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (member.isLeader) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(SpendlyShapeTokens.Pill)
                                .background(colors.lavenderContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Trưởng nhóm",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.onLavenderContainer,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = member.phoneNumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            when (val balance = member.balanceStatus) {
                is MemberBalanceStatus.Positive -> {
                    Text(
                        text = balance.amountFormatted,
                        style = SpendlyTextStyles.moneySmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.mintDeep
                    )
                }
                is MemberBalanceStatus.Negative -> {
                    Text(
                        text = balance.amountFormatted,
                        style = SpendlyTextStyles.moneySmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.coralDeep
                    )
                }
                MemberBalanceStatus.Balanced -> {
                    Text(
                        text = "Đã cân bằng",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.deepLavender
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Preview(
    name = "08 · Thành viên nhóm Spendly (Light)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun MembersScreenPreview() {
    SpendlyTheme(darkTheme = false) {
        MembersScreen()
    }
}

@Preview(
    name = "08 · Thành viên nhóm Spendly (Dark)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun MembersScreenDarkPreview() {
    SpendlyTheme(darkTheme = true) {
        MembersScreen()
    }
}
