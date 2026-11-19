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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.core.designsystem.SpendlyButton
import com.example.nhoms8.core.designsystem.spendlyGlassSurface
import com.example.nhoms8.ui.theme.SpendlyIndigo
import com.example.nhoms8.ui.theme.SpendlyLavender
import com.example.nhoms8.ui.theme.SpendlyLavenderDeep
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.rememberHazeState

/**
 * 09 · Màn hình Chỉnh sửa thành viên (Edit Member Screen)
 * Đồng bộ Spendly Lavender / Organic / Haze 2.1.0 Design System.
 */
@Composable
fun EditMemberScreen(
    initialMemberName: String = "Nguyễn Phan Tấn Đạt",
    initialInitials: String = "TĐ",
    initialPhoneNumber: String = "0986 420 115",
    initialIsLeader: Boolean = false,
    initialCanAddExpense: Boolean = true,
    onBackClicked: () -> Unit = {},
    onChangeAvatarClicked: () -> Unit = {},
    onSaveClicked: (name: String, phone: String, isLeader: Boolean, canAddExpense: Boolean) -> Unit = { _, _, _, _ -> },
    onDeleteMemberClicked: () -> Unit = {}
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()

    var memberName by remember(initialMemberName) { mutableStateOf(initialMemberName) }
    var phoneNumber by remember(initialPhoneNumber) { mutableStateOf(initialPhoneNumber) }
    var isLeader by remember(initialIsLeader) { mutableStateOf(initialIsLeader) }
    var canAddExpense by remember(initialCanAddExpense) { mutableStateOf(initialCanAddExpense) }

    val currentInitials = remember(memberName, initialInitials) {
        val parts = memberName.trim().split(" ").filter { it.isNotBlank() }
        parts.mapNotNull { it.firstOrNull()?.toString() }
            .takeLast(2)
            .joinToString("")
            .uppercase()
            .ifEmpty { initialInitials }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.primaryLavender,
        unfocusedBorderColor = colors.divider,
        focusedLabelColor = colors.deepLavender,
        unfocusedLabelColor = colors.textMuted,
        cursorColor = colors.deepLavender,
        focusedContainerColor = colors.surface.copy(alpha = if (colors.isDark) 0.45f else 0.72f),
        unfocusedContainerColor = colors.surface.copy(alpha = if (colors.isDark) 0.30f else 0.55f),
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary
    )

    OrganicBackground(
        modifier = Modifier.fillMaxSize(),
        hazeState = hazeState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // 1. TOP BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
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

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Chỉnh sửa thành viên",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Cập nhật thông tin & phân quyền trong quỹ",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                }
            }

            // 2. AVATAR HEADER
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onChangeAvatarClicked
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(SpendlyLavender, SpendlyLavenderDeep, SpendlyIndigo)
                                )
                            )
                            .border(3.dp, Color.White.copy(alpha = 0.85f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentInitials,
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(colors.surfaceElevated)
                            .border(1.5.dp, colors.primaryLavender, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CameraAlt,
                            contentDescription = "Thay ảnh đại diện",
                            tint = colors.deepLavender,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Chạm để thay ảnh đại diện",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
            }

            // 3. PERSONAL INFO GLASS CARD
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState,
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Thông tin cá nhân",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )

                    OutlinedTextField(
                        value = memberName,
                        onValueChange = { memberName = it },
                        label = { Text("Họ và tên") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = colors.primaryLavender
                            )
                        },
                        singleLine = true,
                        shape = SpendlyShapeTokens.CardMedium,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Số điện thoại") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Phone,
                                contentDescription = null,
                                tint = colors.primaryLavender
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = SpendlyShapeTokens.CardMedium,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Vai trò trong nhóm",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.textPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RolePillOption(
                                label = "Thành viên",
                                selected = !isLeader,
                                onClick = { isLeader = false },
                                modifier = Modifier.weight(1f)
                            )
                            RolePillOption(
                                label = "Trưởng nhóm",
                                selected = isLeader,
                                onClick = { isLeader = true },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 4. PERMISSIONS GLASS CARD
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SpendlyShapeTokens.CardLarge)
                    .clickable { canAddExpense = !canAddExpense },
                hazeState = hazeState,
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
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.lavenderContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                                contentDescription = null,
                                tint = colors.onLavenderContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Quyền thêm khoản chi",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (canAddExpense) "Đang bật · Có thể ghi chi tiêu" else "Đang tắt · Chỉ xem số dư",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted
                            )
                        }
                    }

                    Switch(
                        checked = canAddExpense,
                        onCheckedChange = { canAddExpense = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = colors.primaryLavender,
                            uncheckedThumbColor = colors.textMuted,
                            uncheckedTrackColor = colors.divider
                        )
                    )
                }
            }

            // 5. ACTION BUTTONS
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SpendlyButton(
                    text = "Lưu thay đổi",
                    onClick = { onSaveClicked(memberName, phoneNumber, isLeader, canAddExpense) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = Icons.Rounded.Check,
                    height = 52.dp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(SpendlyShapeTokens.Pill)
                        .background(colors.coralContainer.copy(alpha = if (colors.isDark) 0.55f else 0.85f))
                        .border(
                            width = 1.dp,
                            color = colors.coralDeep.copy(alpha = 0.40f),
                            shape = SpendlyShapeTokens.Pill
                        )
                        .clickable(role = Role.Button, onClick = onDeleteMemberClicked),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = null,
                        tint = colors.coralDeep,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Xóa khỏi nhóm",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.coralDeep
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun RolePillOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    val bgBrush = if (selected) {
        Brush.horizontalGradient(listOf(SpendlyLavender, SpendlyLavenderDeep, SpendlyIndigo))
    } else {
        null
    }

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(SpendlyShapeTokens.Pill)
            .then(
                if (bgBrush != null) {
                    Modifier.background(bgBrush)
                } else {
                    Modifier
                        .background(colors.surface.copy(alpha = if (colors.isDark) 0.25f else 0.5f))
                        .border(1.dp, colors.divider, SpendlyShapeTokens.Pill)
                }
            )
            .clickable(role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (selected) Color.White else colors.textPrimary
        )
    }
}

@Preview(
    name = "09 · Chỉnh sửa thành viên Spendly (Light)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun EditMemberScreenPreview() {
    SpendlyTheme(darkTheme = false) {
        EditMemberScreen()
    }
}

@Preview(
    name = "09 · Chỉnh sửa thành viên Spendly (Dark)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun EditMemberScreenDarkPreview() {
    SpendlyTheme(darkTheme = true) {
        EditMemberScreen(initialIsLeader = true, initialCanAddExpense = false)
    }
}
