package com.example.nhoms8.feature.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.core.designsystem.SpendlyButton
import com.example.nhoms8.core.designsystem.SpendlyButtonStyle
import com.example.nhoms8.core.designsystem.spendlyGlassSurface
import com.example.nhoms8.domain.model.SpendlyUser
import com.example.nhoms8.feature.groups.GroupViewModel
import com.example.nhoms8.ui.theme.SpendlyIndigo
import com.example.nhoms8.ui.theme.SpendlyLavender
import com.example.nhoms8.ui.theme.SpendlyLavenderDeep
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Màn hình Chỉnh sửa thông tin cá nhân dành riêng cho tài khoản chủ đang đăng nhập (`users/{uid}`).
 * Không chứa bất kỳ trường phân quyền hoặc xóa thành viên của nhóm cụ thể nào.
 */
@Composable
fun EditProfileScreen(
    user: SpendlyUser,
    isSaving: Boolean = false,
    errorMessage: String? = null,
    onBackClicked: () -> Unit = {},
    onSaveClicked: (fullName: String, phone: String, avatarUrl: String) -> Unit = { _, _, _ -> },
    onChangePasswordClicked: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val initialName = user.fullName.trim()
    val initialPhone = user.phone.trim()
    val initialAvatar = user.avatarUrl.trim()

    var fullName by rememberSaveable(user.uid, initialName) { mutableStateOf(initialName) }
    var phone by rememberSaveable(user.uid, initialPhone) { mutableStateOf(initialPhone) }
    var avatarUrl by rememberSaveable(user.uid, initialAvatar) { mutableStateOf(initialAvatar) }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }
    var isProcessingAvatar by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { selectedUri: Uri? ->
        if (selectedUri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            isProcessingAvatar = true
            localError = null
            val savedPath = withContext(Dispatchers.IO) {
                persistAvatarToInternalStorage(
                    context = context,
                    sourceUri = selectedUri,
                    uid = user.uid
                )
            }
            if (savedPath != null) {
                avatarUrl = savedPath
            } else {
                localError = "Không thể đọc ảnh đã chọn. Vui lòng thử lại với ảnh khác."
            }
            isProcessingAvatar = false
        }
    }

    val liveInitials = remember(fullName, user.email) {
        computeProfileInitials(
            name = fullName.ifBlank { user.email.substringBefore('@') }
        )
    }

    val hasChanges = remember(fullName, phone, avatarUrl, initialName, initialPhone, initialAvatar) {
        fullName.trim() != initialName ||
            phone.trim() != initialPhone ||
            avatarUrl.trim() != initialAvatar
    }

    val activeError = localError ?: errorMessage?.takeIf { it.isNotBlank() }

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

    val readOnlyEmailColors = OutlinedTextFieldDefaults.colors(
        disabledBorderColor = colors.divider.copy(alpha = 0.65f),
        disabledLabelColor = colors.textMuted,
        disabledTextColor = colors.textPrimary.copy(alpha = 0.85f),
        disabledLeadingIconColor = colors.textMuted,
        disabledTrailingIconColor = colors.textMuted,
        disabledSupportingTextColor = colors.textMuted,
        disabledContainerColor = colors.surface.copy(alpha = if (colors.isDark) 0.22f else 0.38f)
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
                        .clickable(enabled = !isSaving, role = Role.Button, onClick = onBackClicked),
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
                        text = "Chỉnh sửa thông tin cá nhân",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Quản lý thông tin tài khoản chủ Spendly",
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
                val avatarModel = remember(avatarUrl) { resolveProfileAvatarModel(avatarUrl) }
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clickable(
                            enabled = !isSaving && !isProcessingAvatar,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
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
                        if (avatarModel != null) {
                            AsyncImage(
                                model = avatarModel,
                                contentDescription = "Ảnh đại diện tài khoản",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Text(
                                text = liveInitials,
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        if (isProcessingAvatar) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.35f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(26.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                            }
                        }
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
                            contentDescription = "Chọn ảnh đại diện",
                            tint = colors.deepLavender,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Chạm để thay ảnh đại diện",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )

                if (avatarUrl.isNotBlank()) {
                    TextButton(
                        enabled = !isSaving && !isProcessingAvatar,
                        onClick = {
                            avatarUrl = ""
                            localError = null
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            tint = colors.coralDeep,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Gỡ ảnh đại diện",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.coralDeep
                        )
                    }
                }
            }

            // 3. OWNER ACCOUNT DETAILS CARD
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState,
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Thông tin tài khoản",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            localError = null
                        },
                        enabled = !isSaving,
                        label = { Text("Họ và tên *") },
                        placeholder = { Text("Nhập họ và tên hiển thị") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = colors.primaryLavender
                            )
                        },
                        isError = fullName.isNotEmpty() && fullName.trim().length < 2,
                        supportingText = if (fullName.isNotEmpty() && fullName.trim().length < 2) {
                            { Text("Họ và tên cần có ít nhất 2 ký tự") }
                        } else null,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        shape = SpendlyShapeTokens.CardMedium,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            localError = null
                        },
                        enabled = !isSaving,
                        label = { Text("Số điện thoại") },
                        placeholder = { Text("Chưa cập nhật") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Phone,
                                contentDescription = null,
                                tint = colors.primaryLavender
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        shape = SpendlyShapeTokens.CardMedium,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = user.email.ifBlank { "Chưa liên kết email" },
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Email đăng nhập") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.AlternateEmail,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = "Không thể chỉnh sửa",
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        supportingText = {
                            Text("Email dùng để đăng nhập, không thể sửa trực tiếp")
                        },
                        singleLine = true,
                        shape = SpendlyShapeTokens.CardMedium,
                        colors = readOnlyEmailColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (user.createdAtMillis > 0L) {
                        val joinedDateText = remember(user.createdAtMillis) {
                            SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("vi-VN"))
                                .format(Date(user.createdAtMillis))
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.mintContainer.copy(alpha = if (colors.isDark) 0.22f else 0.55f))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.VerifiedUser,
                                contentDescription = null,
                                tint = colors.mintDeep,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Tham gia từ $joinedDateText · Tài khoản đang hoạt động",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 4. SECURITY / RESET PASSWORD CARD
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SpendlyShapeTokens.CardLarge)
                    .clickable(enabled = !isSaving, role = Role.Button, onClick = onChangePasswordClicked),
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
                                imageVector = Icons.Rounded.LockReset,
                                contentDescription = null,
                                tint = colors.onLavenderContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Đặt lại mật khẩu",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Gửi liên kết đổi mật khẩu qua email đăng nhập",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 5. ERROR BANNER
            if (!activeError.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SpendlyShapeTokens.CardMedium)
                        .background(colors.coralContainer)
                        .border(1.dp, colors.coralDeep.copy(alpha = 0.35f), SpendlyShapeTokens.CardMedium)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        tint = colors.coralDeep,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = activeError,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = colors.coralDeep
                    )
                }
            }

            // 6. SAVE BUTTON
            SpendlyButton(
                text = "Lưu thông tin cá nhân",
                onClick = {
                    val validationError = GroupViewModel.validateProfileInput(fullName, phone)
                    if (validationError != null) {
                        localError = validationError
                    } else {
                        localError = null
                        onSaveClicked(fullName.trim(), phone.trim(), avatarUrl.trim())
                    }
                },
                style = SpendlyButtonStyle.PRIMARY,
                enabled = !isSaving && !isProcessingAvatar && hasChanges && fullName.trim().length >= 2,
                isLoading = isSaving || isProcessingAvatar,
                leadingIcon = Icons.Rounded.Check,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun computeProfileInitials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "SP"
        parts.size == 1 -> parts.first().take(2).uppercase()
        else -> "${parts[parts.size - 2].first()}${parts.last().first()}".uppercase()
    }
}

internal fun resolveProfileAvatarModel(avatarUrl: String): Any? {
    val trimmed = avatarUrl.trim()
    if (trimmed.isEmpty()) return null
    if (trimmed.startsWith("http://", ignoreCase = true) ||
        trimmed.startsWith("https://", ignoreCase = true) ||
        trimmed.startsWith("content://", ignoreCase = true) ||
        trimmed.startsWith("file://", ignoreCase = true)
    ) {
        return trimmed
    }
    val localFile = File(trimmed)
    return if (localFile.exists() && localFile.isFile) localFile else null
}

private fun persistAvatarToInternalStorage(
    context: Context,
    sourceUri: Uri,
    uid: String
): String? = runCatching {
    val avatarDir = File(context.filesDir, "avatars").apply { mkdirs() }
    val safeUid = uid.replace(Regex("[^a-zA-Z0-9_-]"), "").ifBlank { "owner" }
    val outFile = File(avatarDir, "avatar_${safeUid}_${System.currentTimeMillis()}.jpg")

    val decodedBitmap = decodeScaledBitmap(context, sourceUri, maxDimensionPx = 720)
    if (decodedBitmap != null) {
        try {
            outFile.outputStream().use { output ->
                decodedBitmap.compress(Bitmap.CompressFormat.JPEG, 88, output)
            }
        } finally {
            decodedBitmap.recycle()
        }
    } else {
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            outFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: return@runCatching null
    }
    outFile.absolutePath
}.getOrNull()

private fun decodeScaledBitmap(
    context: Context,
    uri: Uri,
    maxDimensionPx: Int
): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, bounds)
    } ?: return null

    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while ((bounds.outWidth / sampleSize) > maxDimensionPx * 2 ||
        (bounds.outHeight / sampleSize) > maxDimensionPx * 2
    ) {
        sampleSize *= 2
    }

    val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    val sampled = context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, decodeOptions)
    } ?: return null

    val maxSide = maxOf(sampled.width, sampled.height)
    if (maxSide <= maxDimensionPx) return sampled

    val scale = maxDimensionPx.toFloat() / maxSide.toFloat()
    val targetWidth = (sampled.width * scale).toInt().coerceAtLeast(1)
    val targetHeight = (sampled.height * scale).toInt().coerceAtLeast(1)
    val scaled = Bitmap.createScaledBitmap(sampled, targetWidth, targetHeight, true)
    if (scaled != sampled) {
        sampled.recycle()
    }
    return scaled
}

@Preview(showBackground = true)
@Composable
private fun EditProfileScreenPreview() {
    SpendlyTheme {
        EditProfileScreen(
            user = SpendlyUser(
                uid = "uid_preview",
                fullName = "Nguyễn Minh An",
                email = "minhan@spendly.vn",
                phone = "0912345678",
                createdAtMillis = 1731283200000L
            )
        )
    }
}
