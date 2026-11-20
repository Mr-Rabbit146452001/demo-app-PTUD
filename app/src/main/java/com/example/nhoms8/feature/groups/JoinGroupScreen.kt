package com.example.nhoms8.feature.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.core.designsystem.SpendlyButton
import com.example.nhoms8.core.designsystem.SpendlyButtonStyle
import com.example.nhoms8.domain.model.SpendlyGroup
import com.example.nhoms8.domain.model.SpendlyJoinRequest
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.rememberHazeState

/**
 * Màn hình Tham gia quỹ nhóm bằng mã mời 6 ký tự.
 */
@Composable
fun JoinGroupScreen(
    viewModel: GroupViewModel,
    initialInviteCode: String = "",
    scannedCodeNonce: Long = 0L,
    onBackClick: () -> Unit,
    onScanQrClick: () -> Unit = {},
    onJoinedGroup: (SpendlyGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.groupsUiState.collectAsStateWithLifecycle()
    var pendingApprovalState by remember { mutableStateOf<Pair<SpendlyJoinRequest, Boolean>?>(null) }

    JoinGroupContent(
        initialInviteCode = initialInviteCode,
        scannedCodeNonce = scannedCodeNonce,
        isSubmitting = uiState.isSubmitting,
        serverErrorMessage = uiState.errorMessage,
        pendingRequest = pendingApprovalState?.first,
        isDuplicatePending = pendingApprovalState?.second == true,
        onBackClick = {
            viewModel.clearMessages()
            onBackClick()
        },
        onScanQrClick = {
            viewModel.clearMessages()
            pendingApprovalState = null
            onScanQrClick()
        },
        onJoinSubmit = { code ->
            pendingApprovalState = null
            viewModel.requestJoinGroup(
                inviteCode = code,
                onPendingApproval = { request, alreadyPending ->
                    pendingApprovalState = request to alreadyPending
                },
                onAlreadyMember = onJoinedGroup
            )
        },
        onClearPendingNotice = { pendingApprovalState = null },
        onReturnAfterRequestClick = {
            viewModel.clearMessages()
            onBackClick()
        },
        modifier = modifier
    )
}

/**
 * Giao diện thuần trạng thái cho màn hình Gửi yêu cầu tham gia quỹ nhóm.
 */
@Composable
fun JoinGroupContent(
    initialInviteCode: String = "",
    scannedCodeNonce: Long = 0L,
    isSubmitting: Boolean = false,
    serverErrorMessage: String? = null,
    pendingRequest: SpendlyJoinRequest? = null,
    isDuplicatePending: Boolean = false,
    onBackClick: () -> Unit = {},
    onScanQrClick: () -> Unit = {},
    onJoinSubmit: (inviteCode: String) -> Unit = {},
    onClearPendingNotice: () -> Unit = {},
    onReturnAfterRequestClick: () -> Unit = onBackClick,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()

    var inviteCode by rememberSaveable(initialInviteCode) {
        mutableStateOf(
            initialInviteCode.uppercase().filter { it.isLetterOrDigit() }.take(6)
        )
    }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }
    var lastAppliedQrNonce by rememberSaveable { mutableStateOf(0L) }
    var showQrPrefillNotice by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(initialInviteCode, scannedCodeNonce) {
        if (scannedCodeNonce > 0L && scannedCodeNonce != lastAppliedQrNonce) {
            val normalized = initialInviteCode.uppercase().filter { it.isLetterOrDigit() }.take(6)
            if (normalized.isNotEmpty()) {
                inviteCode = normalized
                localError = null
                lastAppliedQrNonce = scannedCodeNonce
                showQrPrefillNotice = true
            }
        }
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
        modifier = modifier.fillMaxSize(),
        hazeState = hazeState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(colors.glassContainer.copy(alpha = 0.85f))
                        .border(1.dp, colors.divider, CircleShape)
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "Tham gia quỹ nhóm",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Gửi yêu cầu tham gia quỹ bằng mã mời · Thủ quỹ sẽ duyệt",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                }
            }

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(SpendlyShapeTokens.CardMedium)
                            .background(colors.lavenderContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.GroupAdd,
                            contentDescription = null,
                            tint = colors.deepLavender,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Text(
                        text = "Nhập mã mời 6 ký tự",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary
                    )

                    Text(
                        text = "Mã mời tự động chuyển thành chữ in hoa",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = inviteCode,
                        onValueChange = { raw ->
                            inviteCode = raw
                                .uppercase()
                                .filter { it.isLetterOrDigit() }
                                .take(6)
                            localError = null
                            showQrPrefillNotice = false
                            onClearPendingNotice()
                        },
                        label = { Text("Mã mời quỹ") },
                        placeholder = { Text("VD: SP8K2M") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.QrCode2,
                                contentDescription = null,
                                tint = colors.deepLavender
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = onScanQrClick,
                                enabled = !isSubmitting
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.QrCodeScanner,
                                    contentDescription = "Quét mã QR",
                                    tint = colors.deepLavender
                                )
                            }
                        },
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "6 ký tự chữ cái và số",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textMuted
                                )
                                Text(
                                    text = "${inviteCode.length}/6",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (inviteCode.length == 6) colors.mintDeep else colors.textMuted
                                )
                            }
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 5.sp
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done
                        ),
                        shape = SpendlyShapeTokens.CardSmall,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val displayError = localError ?: serverErrorMessage
                    if (!displayError.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SpendlyShapeTokens.CardSmall)
                                .background(colors.coralContainer)
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = displayError,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = colors.coralDeep
                            )
                        }
                    } else if (showQrPrefillNotice && inviteCode.length == 6 && pendingRequest == null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SpendlyShapeTokens.CardSmall)
                                .background(colors.mintContainer)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = colors.mintDeep,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Đã nhận mã từ QR — kiểm tra và bấm Gửi yêu cầu tham gia",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = colors.mintDeep,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (pendingRequest != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SpendlyShapeTokens.CardSmall)
                                .background(colors.lavenderContainer)
                                .border(
                                    width = 1.dp,
                                    color = colors.primaryLavender.copy(alpha = 0.45f),
                                    shape = SpendlyShapeTokens.CardSmall
                                )
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.HourglassTop,
                                    contentDescription = null,
                                    tint = colors.deepLavender,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (isDuplicatePending) {
                                        "Bạn đã gửi yêu cầu trước đó"
                                    } else {
                                        "Đã gửi yêu cầu tham gia quỹ"
                                    },
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.deepLavender
                                )
                            }
                            Text(
                                text = "Quỹ \"${pendingRequest.groupName}\" (Mã ${pendingRequest.inviteCode}) — Đang chờ Thủ quỹ hoặc Quản trị viên phê duyệt để bạn chính thức vào quỹ.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textPrimary
                            )
                            SpendlyButton(
                                text = "Về danh sách quỹ",
                                onClick = onReturnAfterRequestClick,
                                modifier = Modifier.fillMaxWidth(),
                                height = 44.dp,
                                style = SpendlyButtonStyle.PRIMARY
                            )
                        }
                    }

                    SpendlyButton(
                        text = "Gửi yêu cầu tham gia",
                        onClick = {
                            val normalized = inviteCode.trim().uppercase()
                            if (normalized.length != 6) {
                                localError = "Mã mời phải gồm đúng 6 ký tự chữ hoặc số"
                            } else {
                                localError = null
                                onJoinSubmit(normalized)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSubmitting,
                        isLoading = isSubmitting,
                        leadingIcon = Icons.AutoMirrored.Rounded.Send
                    )

                    SpendlyButton(
                        text = "Quét mã QR",
                        onClick = onScanQrClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSubmitting,
                        style = SpendlyButtonStyle.SECONDARY_GLASS,
                        leadingIcon = Icons.Rounded.QrCodeScanner,
                        hazeState = hazeState
                    )
                }
            }

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colors.mintContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                tint = colors.mintDeep,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Hướng dẫn nhận mã mời & QR",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary
                        )
                    }

                    InviteHintRow(
                        step = "1",
                        text = "Nhờ Trưởng nhóm hoặc Quản trị viên mở màn hình Chi tiết quỹ và nhấn vào thẻ Mã mời."
                    )
                    InviteHintRow(
                        step = "2",
                        text = "Quét mã QR được tạo trên máy Trưởng nhóm hoặc sao chép chuỗi mã 6 ký tự."
                    )
                    InviteHintRow(
                        step = "3",
                        text = "Sau khi Thủ quỹ duyệt yêu cầu, bạn sẽ chính thức vào quỹ để theo dõi số dư, ghi khoản chi và đối soát nợ."
                    )
                }
            }
        }
    }
}

@Composable
private fun InviteHintRow(
    step: String,
    text: String
) {
    val colors = MaterialTheme.spendlyColors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(colors.lavenderContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.deepLavender
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textMuted,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 780)
@Composable
private fun JoinGroupScreenPreview() {
    SpendlyTheme {
        JoinGroupContent(initialInviteCode = "N08PRO")
    }
}
