package com.example.nhoms8.feature.groups.qr

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.core.designsystem.SpendlyButton
import com.example.nhoms8.core.designsystem.SpendlyButtonStyle
import com.example.nhoms8.ui.theme.SpendlyCoral
import com.example.nhoms8.ui.theme.SpendlyCoralDeep
import com.example.nhoms8.ui.theme.SpendlyLavender
import com.example.nhoms8.ui.theme.SpendlyLavenderLight
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlySky
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private const val INVALID_QR_THROTTLE_MS = 1_500L
private const val STATUS_BANNER_DURATION_MS = 2_000L

/**
 * Màn hình Quét mã QR Code để lấy mã mời quỹ nhóm 6 ký tự bằng CameraX hoặc từ ảnh Thư viện.
 */
@Composable
fun QrScannerScreen(
    onBackClick: () -> Unit,
    onInviteCodeScanned: (String) -> Unit,
    onManualInputClick: () -> Unit = onBackClick,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val currentOnInviteCodeScanned by rememberUpdatedState(onInviteCodeScanned)
    val hasDeliveredResult = remember { AtomicBoolean(false) }

    var hasCameraPermission by remember {
        mutableStateOf(context.hasCameraPermission())
    }
    var hasRequestedPermissionOnce by rememberSaveable { mutableStateOf(false) }
    var invalidQrMessage by remember { mutableStateOf<String?>(null) }
    var lastInvalidNoticeAtMs by remember { mutableLongStateOf(0L) }
    var isDecodingGalleryImage by remember { mutableStateOf(false) }
    var cameraUnavailableMessage by remember { mutableStateOf<String?>(null) }

    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    var hasFlashUnit by remember { mutableStateOf(false) }
    var isTorchEnabled by remember { mutableStateOf(false) }

    val deliverInviteCode: (String) -> Unit = { code ->
        if (hasDeliveredResult.compareAndSet(false, true)) {
            isTorchEnabled = false
            boundCamera?.cameraControl?.enableTorch(false)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            currentOnInviteCodeScanned(code)
        }
    }

    val reportInvalidQr: (String) -> Unit = { message ->
        val now = System.currentTimeMillis()
        if (now - lastInvalidNoticeAtMs >= INVALID_QR_THROTTLE_MS) {
            lastInvalidNoticeAtMs = now
            invalidQrMessage = message
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { selectedUri ->
        if (selectedUri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            isDecodingGalleryImage = true
            val rawQrText = withContext(Dispatchers.IO) {
                runCatching { QrCodeDecoder.decodeFromUri(context, selectedUri) }.getOrNull()
            }
            isDecodingGalleryImage = false

            if (rawQrText == null) {
                reportInvalidQr("Không tìm thấy mã QR trong ảnh đã chọn")
                return@launch
            }

            val parsedCode = InviteCodeQrParser.extractInviteCode(rawQrText)
            if (parsedCode != null) {
                deliverInviteCode(parsedCode)
            } else {
                reportInvalidQr("Mã QR không hợp lệ — cần mã mời Spendly 6 ký tự")
            }
        }
    }

    // Tự động xin quyền Camera lần đầu mở màn hình
    LaunchedEffect(Unit) {
        if (!hasCameraPermission && !hasRequestedPermissionOnce) {
            hasRequestedPermissionOnce = true
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Cập nhật quyền nếu người dùng vừa bật quyền từ Cài đặt hệ thống và quay lại
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasCameraPermission = context.hasCameraPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Tự ẩn thông báo lỗi nhẹ sau 2 giây để người dùng tiếp tục quét mượt mà
    LaunchedEffect(invalidQrMessage, lastInvalidNoticeAtMs) {
        if (invalidQrMessage != null) {
            delay(STATUS_BANNER_DURATION_MS)
            invalidQrMessage = null
        }
    }

    val openGalleryPicker = {
        galleryPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    if (!hasCameraPermission) {
        QrCameraPermissionRequiredContent(
            hasRequestedOnce = hasRequestedPermissionOnce,
            invalidMessage = invalidQrMessage,
            isDecodingGalleryImage = isDecodingGalleryImage,
            onBackClick = onBackClick,
            onRequestPermissionClick = {
                hasRequestedPermissionOnce = true
                permissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onOpenSettingsClick = { context.openAppPermissionSettings() },
            onPickFromGalleryClick = openGalleryPicker,
            onManualInputClick = onManualInputClick,
            modifier = modifier
        )
        return
    }

    val previewView = remember(context) {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(lifecycleOwner, previewView, hasCameraPermission) {
        val executor = Executors.newSingleThreadExecutor()
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        var isDisposed = false
        var activeProvider: ProcessCameraProvider? = null

        cameraProviderFuture.addListener(
            {
                if (isDisposed) return@addListener
                try {
                    val provider = cameraProviderFuture.get()
                    activeProvider = provider
                    val selector = when {
                        provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ->
                            CameraSelector.DEFAULT_BACK_CAMERA
                        provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ->
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        else -> null
                    }

                    if (selector == null) {
                        cameraUnavailableMessage = "Thiết bị không có camera khả dụng"
                        return@addListener
                    }

                    val previewUseCase = CameraPreview.Builder().build().also { preview ->
                        preview.surfaceProvider = previewView.surfaceProvider
                    }

                    val analysisUseCase = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(executor) { imageProxy ->
                                try {
                                    if (!hasDeliveredResult.get()) {
                                        val decoded = QrCodeDecoder.decodeFromImageProxy(imageProxy)
                                        if (decoded != null) {
                                            val inviteCode = InviteCodeQrParser.extractInviteCode(decoded)
                                            if (inviteCode != null) {
                                                mainExecutor.execute {
                                                    deliverInviteCode(inviteCode)
                                                }
                                            } else {
                                                mainExecutor.execute {
                                                    reportInvalidQr(
                                                        "Mã QR không hợp lệ — cần mã mời Spendly 6 ký tự"
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } finally {
                                    imageProxy.close()
                                }
                            }
                        }

                    provider.unbindAll()
                    val camera = provider.bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        previewUseCase,
                        analysisUseCase
                    )
                    cameraUnavailableMessage = null
                    boundCamera = camera
                    hasFlashUnit = camera.cameraInfo.hasFlashUnit()
                    isTorchEnabled = false
                } catch (_: Throwable) {
                    cameraUnavailableMessage = "Không thể khởi động camera lúc này"
                }
            },
            mainExecutor
        )

        onDispose {
            isDisposed = true
            runCatching {
                boundCamera?.cameraControl?.enableTorch(false)
                activeProvider?.unbindAll()
            }
            boundCamera = null
            hasFlashUnit = false
            isTorchEnabled = false
            executor.shutdown()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (cameraUnavailableMessage == null) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
        }

        QrViewfinderOverlay(
            isError = invalidQrMessage != null,
            modifier = Modifier.fillMaxSize()
        )

        QrScannerOverlayChrome(
            statusMessage = invalidQrMessage
                ?: cameraUnavailableMessage
                ?: "Đưa mã QR mời quỹ vào giữa khung ngắm",
            isErrorStatus = invalidQrMessage != null || cameraUnavailableMessage != null,
            hasFlashUnit = hasFlashUnit,
            isTorchEnabled = isTorchEnabled,
            isDecodingGalleryImage = isDecodingGalleryImage,
            onBackClick = onBackClick,
            onToggleTorchClick = {
                val next = !isTorchEnabled
                isTorchEnabled = next
                boundCamera?.cameraControl?.enableTorch(next)
            },
            onPickFromGalleryClick = openGalleryPicker,
            onManualInputClick = onManualInputClick,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun QrViewfinderOverlay(
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "qr_laser_scan")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_850, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "qr_laser_progress"
    )

    val cornerColor = if (isError) SpendlyCoral else SpendlyLavenderLight
    val laserColors = if (isError) {
        listOf(Color.Transparent, SpendlyCoral, Color.Transparent)
    } else {
        listOf(Color.Transparent, SpendlyLavender, SpendlySky, Color.Transparent)
    }

    Canvas(
        modifier = modifier.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    ) {
        // Phủ nền tối mờ toàn màn hình
        drawRect(color = Color.Black.copy(alpha = 0.58f))

        val boxSizePx = 256.dp.toPx().coerceAtMost(size.minDimension * 0.76f)
        val left = (size.width - boxSizePx) / 2f
        val top = (size.height - boxSizePx) * 0.44f
        val cornerRadiusPx = 24.dp.toPx()

        // Đục lỗ trong suốt giữa khung ngắm
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(boxSizePx, boxSizePx),
            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
            blendMode = BlendMode.Clear
        )

        // Viền mảnh dẫn hướng quanh khung
        drawRoundRect(
            color = Color.White.copy(alpha = 0.25f),
            topLeft = Offset(left, top),
            size = Size(boxSizePx, boxSizePx),
            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
            style = Stroke(width = 1.dp.toPx())
        )

        // 4 góc bo tròn nổi bật màu SpendlyLavender
        val bracketLength = 30.dp.toPx()
        val strokeWidth = 4.dp.toPx()
        val r = cornerRadiusPx
        val right = left + boxSizePx
        val bottom = top + boxSizePx

        val cornerPath = Path().apply {
            // Góc trên trái
            moveTo(left, top + r + bracketLength)
            lineTo(left, top + r)
            arcTo(Rect(left, top, left + 2 * r, top + 2 * r), 180f, 90f, false)
            lineTo(left + r + bracketLength, top)

            // Góc trên phải
            moveTo(right - r - bracketLength, top)
            lineTo(right - r, top)
            arcTo(Rect(right - 2 * r, top, right, top + 2 * r), 270f, 90f, false)
            lineTo(right, top + r + bracketLength)

            // Góc dưới phải
            moveTo(right, bottom - r - bracketLength)
            lineTo(right, bottom - r)
            arcTo(Rect(right - 2 * r, bottom - 2 * r, right, bottom), 0f, 90f, false)
            lineTo(right - r - bracketLength, bottom)

            // Góc dưới trái
            moveTo(left + r + bracketLength, bottom)
            lineTo(left + r, bottom)
            arcTo(Rect(left, bottom - 2 * r, left + 2 * r, bottom), 90f, 90f, false)
            lineTo(left, bottom - r - bracketLength)
        }

        drawPath(
            path = cornerPath,
            color = cornerColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Tia laser quét mượt
        val inset = 18.dp.toPx()
        val laserY = top + boxSizePx * laserProgress
        drawLine(
            brush = Brush.horizontalGradient(
                colors = laserColors,
                startX = left + inset,
                endX = right - inset
            ),
            start = Offset(left + inset, laserY),
            end = Offset(right - inset, laserY),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun QrScannerOverlayChrome(
    statusMessage: String,
    isErrorStatus: Boolean,
    hasFlashUnit: Boolean,
    isTorchEnabled: Boolean,
    isDecodingGalleryImage: Boolean,
    onBackClick: () -> Unit,
    onToggleTorchClick: () -> Unit,
    onPickFromGalleryClick: () -> Unit,
    onManualInputClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors

    Column(
        modifier = modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Thanh điều khiển trên (Glass Top Bar)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ScannerCircleButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Quay lại",
                onClick = onBackClick
            )

            Box(
                modifier = Modifier
                    .clip(SpendlyShapeTokens.Pill)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .border(1.dp, Color.White.copy(alpha = 0.22f), SpendlyShapeTokens.Pill)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Quét mã QR quỹ",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }

            if (hasFlashUnit) {
                ScannerCircleButton(
                    icon = if (isTorchEnabled) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                    contentDescription = if (isTorchEnabled) "Tắt đèn Flash" else "Bật đèn Flash",
                    isHighlighted = isTorchEnabled,
                    onClick = onToggleTorchClick
                )
            } else {
                Spacer(modifier = Modifier.size(44.dp))
            }
        }

        // Cụm trạng thái & hành động phía dưới
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .clip(SpendlyShapeTokens.Pill)
                        .background(
                            if (isErrorStatus) {
                                SpendlyCoralDeep.copy(alpha = 0.90f)
                            } else {
                                Color.Black.copy(alpha = 0.56f)
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = if (isErrorStatus) {
                                SpendlyCoral.copy(alpha = 0.7f)
                            } else {
                                Color.White.copy(alpha = 0.22f)
                            },
                            shape = SpendlyShapeTokens.Pill
                        )
                        .padding(horizontal = 16.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SpendlyShapeTokens.CardLarge)
                    .background(colors.glassContainer.copy(alpha = if (colors.isDark) 0.88f else 0.92f))
                    .border(1.dp, colors.divider, SpendlyShapeTokens.CardLarge)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SpendlyButton(
                        text = if (isDecodingGalleryImage) "Đang đọc ảnh..." else "Chọn ảnh QR",
                        onClick = onPickFromGalleryClick,
                        modifier = Modifier.weight(1f),
                        height = 46.dp,
                        enabled = !isDecodingGalleryImage,
                        isLoading = isDecodingGalleryImage,
                        style = SpendlyButtonStyle.PRIMARY,
                        leadingIcon = Icons.Rounded.PhotoLibrary
                    )
                    SpendlyButton(
                        text = "Nhập mã tay",
                        onClick = onManualInputClick,
                        modifier = Modifier.weight(1f),
                        height = 46.dp,
                        enabled = !isDecodingGalleryImage,
                        style = SpendlyButtonStyle.OUTLINED,
                        leadingIcon = Icons.Rounded.Keyboard
                    )
                }

                Text(
                    text = "Hỗ trợ mã QR tạo từ Spendly hoặc ảnh chụp mã mời lưu trong máy",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ScannerCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    isHighlighted: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                if (isHighlighted) {
                    SpendlyLavender.copy(alpha = 0.88f)
                } else {
                    Color.Black.copy(alpha = 0.45f)
                }
            )
            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(21.dp)
        )
    }
}

@Composable
private fun QrCameraPermissionRequiredContent(
    hasRequestedOnce: Boolean,
    invalidMessage: String?,
    isDecodingGalleryImage: Boolean,
    onBackClick: () -> Unit,
    onRequestPermissionClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    onPickFromGalleryClick: () -> Unit,
    onManualInputClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()

    OrganicBackground(
        modifier = modifier.fillMaxSize(),
        hazeState = hazeState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
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
                        text = "Quét mã QR quỹ",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Cấp quyền camera hoặc chọn ảnh QR có sẵn",
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
                            .size(64.dp)
                            .clip(SpendlyShapeTokens.CardMedium)
                            .background(colors.lavenderContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.QrCodeScanner,
                            contentDescription = null,
                            tint = colors.deepLavender,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "Cần quyền truy cập Camera",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Spendly cần camera để quét trực tiếp mã QR mời quỹ từ thiết bị của Trưởng nhóm. Nếu chưa cấp quyền, bạn vẫn có thể chọn ảnh mã QR từ thư viện.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted,
                        textAlign = TextAlign.Center
                    )

                    if (!invalidMessage.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SpendlyShapeTokens.CardSmall)
                                .background(colors.coralContainer)
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = invalidMessage,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = colors.coralDeep,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    SpendlyButton(
                        text = "Cấp quyền Camera",
                        onClick = onRequestPermissionClick,
                        modifier = Modifier.fillMaxWidth(),
                        style = SpendlyButtonStyle.PRIMARY,
                        leadingIcon = Icons.Rounded.CameraAlt
                    )

                    if (hasRequestedOnce) {
                        SpendlyButton(
                            text = "Mở Cài đặt ứng dụng",
                            onClick = onOpenSettingsClick,
                            modifier = Modifier.fillMaxWidth(),
                            style = SpendlyButtonStyle.SECONDARY_GLASS,
                            hazeState = hazeState,
                            leadingIcon = Icons.Rounded.Settings
                        )
                    }

                    SpendlyButton(
                        text = if (isDecodingGalleryImage) "Đang đọc ảnh QR..." else "Chọn ảnh QR từ thư viện",
                        onClick = onPickFromGalleryClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isDecodingGalleryImage,
                        isLoading = isDecodingGalleryImage,
                        style = SpendlyButtonStyle.OUTLINED,
                        leadingIcon = Icons.Rounded.PhotoLibrary
                    )

                    TextButton(
                        onClick = onManualInputClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Keyboard,
                            contentDescription = null,
                            tint = colors.deepLavender,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nhập mã thủ công",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.deepLavender
                        )
                    }
                }
            }
        }
    }
}

private fun Context.hasCameraPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}

private fun Context.openAppPermissionSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { startActivity(intent) }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 780)
@Composable
private fun QrPermissionDeniedPreview() {
    SpendlyTheme {
        QrCameraPermissionRequiredContent(
            hasRequestedOnce = true,
            invalidMessage = null,
            isDecodingGalleryImage = false,
            onBackClick = {},
            onRequestPermissionClick = {},
            onOpenSettingsClick = {},
            onPickFromGalleryClick = {},
            onManualInputClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 780)
@Composable
private fun QrScannerOverlayPreview() {
    SpendlyTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1F2333))
        ) {
            QrViewfinderOverlay(
                isError = false,
                modifier = Modifier.fillMaxSize()
            )
            QrScannerOverlayChrome(
                statusMessage = "Đưa mã QR mời quỹ vào giữa khung ngắm",
                isErrorStatus = false,
                hasFlashUnit = true,
                isTorchEnabled = false,
                isDecodingGalleryImage = false,
                onBackClick = {},
                onToggleTorchClick = {},
                onPickFromGalleryClick = {},
                onManualInputClick = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
