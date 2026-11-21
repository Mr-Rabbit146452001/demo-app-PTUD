package com.example.nhoms8.feature.receipt

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
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.nhoms8.algorithm.FintechAlgorithm
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.core.designsystem.SpendlyButton
import com.example.nhoms8.core.designsystem.SpendlyButtonStyle
import com.example.nhoms8.core.designsystem.spendlyGlassSurface
import com.example.nhoms8.domain.model.SpendlyCategory
import com.example.nhoms8.ui.theme.SpendlyCoralDeep
import com.example.nhoms8.ui.theme.SpendlyIndigo
import com.example.nhoms8.ui.theme.SpendlyLavender
import com.example.nhoms8.ui.theme.SpendlyLavenderLight
import com.example.nhoms8.ui.theme.SpendlyMintDeep
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlySky
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

/**
 * Màn hình AI Quét hóa đơn bằng CameraX hoặc Thư viện ảnh, dùng Gemini 2.5 Flash Vision
 * trích xuất thông tin hóa đơn rồi rà soát trước khi thêm vào khoản chi nhóm.
 */
@Composable
fun ReceiptScannerScreen(
    groupId: String,
    currentUserId: String,
    targetMode: String,
    viewModel: ReceiptScannerViewModel,
    onBackClick: () -> Unit,
    onContinueWithReceipt: (ScannedReceiptResult, payerId: String, payerName: String) -> Unit,
    onQuickSaved: (String) -> Unit,
    onManualEntryClick: (savedReceiptPath: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(groupId, currentUserId) {
        viewModel.bindContext(groupId = groupId, currentUserId = currentUserId)
    }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { selectedUri ->
        if (selectedUri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            ReceiptImageProcessor.prepareFromUri(context, selectedUri)
                .onSuccess { prepared ->
                    viewModel.analyzePreparedImage(prepared)
                }
                .onFailure { error ->
                    viewModel.reportImagePreparationError(
                        error.message ?: "Không thể đọc ảnh hóa đơn đã chọn"
                    )
                }
        }
    }

    val openGalleryPicker: () -> Unit = {
        galleryPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    when (uiState.step) {
        ReceiptScanStep.CAMERA -> {
            ReceiptCameraCaptureStep(
                groupName = uiState.groupName,
                onBackClick = onBackClick,
                onPickFromGalleryClick = openGalleryPicker,
                onManualEntryClick = { onManualEntryClick("") },
                onPhotoCaptured = { tempFile ->
                    coroutineScope.launch {
                        ReceiptImageProcessor.prepareFromFile(context, tempFile, deleteSourceAfter = true)
                            .onSuccess { prepared ->
                                viewModel.analyzePreparedImage(prepared)
                            }
                            .onFailure { error ->
                                viewModel.reportImagePreparationError(
                                    error.message ?: "Không thể xử lý ảnh vừa chụp"
                                )
                            }
                    }
                },
                modifier = modifier
            )
        }

        ReceiptScanStep.ANALYZING -> {
            ReceiptAnalyzingStep(
                imagePath = uiState.capturedImagePath,
                groupName = uiState.groupName,
                onCancelClick = { viewModel.resetToCamera() },
                modifier = modifier
            )
        }

        ReceiptScanStep.REVIEW -> {
            ReceiptReviewStep(
                uiState = uiState,
                targetMode = targetMode,
                onBackClick = { viewModel.resetToCamera() },
                onTitleChange = viewModel::updateEditableTitle,
                onAmountChange = viewModel::updateEditableAmount,
                onCategorySelect = viewModel::updateEditableCategory,
                onPayerSelect = viewModel::updateEditablePayer,
                onDescriptionChange = viewModel::updateEditableDescription,
                onRetakeClick = { viewModel.resetToCamera() },
                onContinueClick = {
                    val reviewed = viewModel.buildReviewedResult()
                    val payerId = uiState.editablePayerId
                    val payerName = viewModel.selectedPayerName()
                    onContinueWithReceipt(reviewed, payerId, payerName)
                },
                onQuickSaveClick = {
                    viewModel.saveQuickEqualSplit(
                        currentUserId = currentUserId,
                        onSuccess = onQuickSaved
                    )
                },
                modifier = modifier
            )
        }

        ReceiptScanStep.ERROR -> {
            ReceiptErrorStep(
                imagePath = uiState.capturedImagePath,
                errorMessage = uiState.errorMessage ?: "Không thể phân tích ảnh hóa đơn",
                canRetry = uiState.canRetry,
                onBackClick = onBackClick,
                onRetakeClick = { viewModel.resetToCamera() },
                onRetryClick = { viewModel.retryLastImage(context) },
                onPickFromGalleryClick = openGalleryPicker,
                onManualWithPhotoClick = { onManualEntryClick(uiState.capturedImagePath) },
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ReceiptCameraCaptureStep(
    groupName: String,
    onBackClick: () -> Unit,
    onPickFromGalleryClick: () -> Unit,
    onManualEntryClick: () -> Unit,
    onPhotoCaptured: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current

    var hasCameraPermission by remember { mutableStateOf(context.hasReceiptCameraPermission()) }
    var hasRequestedPermissionOnce by rememberSaveable { mutableStateOf(false) }
    var cameraUnavailableMessage by remember { mutableStateOf<String?>(null) }
    var isTakingPhoto by remember { mutableStateOf(false) }

    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    var hasFlashUnit by remember { mutableStateOf(false) }
    var isTorchEnabled by remember { mutableStateOf(false) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission && !hasRequestedPermissionOnce) {
            hasRequestedPermissionOnce = true
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasCameraPermission = context.hasReceiptCameraPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (!hasCameraPermission) {
        ReceiptCameraPermissionRequiredContent(
            hasRequestedOnce = hasRequestedPermissionOnce,
            onBackClick = onBackClick,
            onRequestPermissionClick = {
                hasRequestedPermissionOnce = true
                permissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onOpenSettingsClick = { context.openReceiptAppPermissionSettings() },
            onPickFromGalleryClick = onPickFromGalleryClick,
            onManualEntryClick = onManualEntryClick,
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
        val cameraExecutor = Executors.newSingleThreadExecutor()
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        var isDisposed = false
        var activeProvider: ProcessCameraProvider? = null

        cameraProviderFuture.addListener(
            {
                if (isDisposed) return@addListener
                runCatching {
                    val provider = cameraProviderFuture.get()
                    activeProvider = provider
                    val previewUseCase = CameraPreview.Builder()
                        .build()
                        .also { it.surfaceProvider = previewView.surfaceProvider }

                    val captureUseCase = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    provider.unbindAll()
                    val camera = provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        previewUseCase,
                        captureUseCase
                    )
                    imageCapture = captureUseCase
                    boundCamera = camera
                    hasFlashUnit = camera.cameraInfo.hasFlashUnit()
                    cameraUnavailableMessage = null
                }.onFailure { error ->
                    cameraUnavailableMessage = error.message
                        ?: "Không thể khởi động camera. Bạn có thể chọn ảnh hóa đơn từ thư viện."
                }
            },
            mainExecutor
        )

        onDispose {
            isDisposed = true
            isTorchEnabled = false
            runCatching {
                boundCamera?.cameraControl?.enableTorch(false)
                activeProvider?.unbindAll()
            }
            cameraExecutor.shutdown()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        ReceiptViewfinderOverlay(modifier = Modifier.fillMaxSize())

        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleOverlayIconButton(
                onClick = onBackClick,
                contentDescription = "Quay lại"
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .clip(SpendlyShapeTokens.Pill)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), SpendlyShapeTokens.Pill)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = SpendlyLavenderLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Quét hóa đơn AI",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                }
                if (groupName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Quỹ: $groupName",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (hasFlashUnit) {
                CircleOverlayIconButton(
                    onClick = {
                        val next = !isTorchEnabled
                        isTorchEnabled = next
                        boundCamera?.cameraControl?.enableTorch(next)
                    },
                    contentDescription = "Bật tắt đèn flash"
                ) {
                    Icon(
                        imageVector = if (isTorchEnabled) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                        contentDescription = "Đèn flash",
                        tint = if (isTorchEnabled) SpendlyLavenderLight else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(44.dp))
            }
        }

        // Hướng dẫn hoặc cảnh báo camera
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (!cameraUnavailableMessage.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .clip(SpendlyShapeTokens.Pill)
                        .background(SpendlyCoralDeep.copy(alpha = 0.90f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = cameraUnavailableMessage.orEmpty(),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    text = "Đưa toàn bộ hóa đơn vào khung chữ nhật & giữ chắc tay",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = Color.White.copy(alpha = 0.92f),
                    textAlign = TextAlign.Center
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thư viện ảnh
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onPickFromGalleryClick)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f))
                            .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhotoLibrary,
                            contentDescription = "Chọn từ thư viện",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Thư viện",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }

                // Nút Chụp lớn
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .border(3.dp, Color.White, CircleShape)
                        .clickable(enabled = !isTakingPhoto && imageCapture != null) {
                            val capture = imageCapture ?: return@clickable
                            isTakingPhoto = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val tempFile = ReceiptImageProcessor.createTempCaptureFile(context)
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()
                            capture.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        isTakingPhoto = false
                                        isTorchEnabled = false
                                        boundCamera?.cameraControl?.enableTorch(false)
                                        onPhotoCaptured(tempFile)
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        isTakingPhoto = false
                                        cameraUnavailableMessage = exception.message
                                            ?: "Không thể chụp ảnh, hãy thử lại hoặc chọn ảnh từ thư viện"
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isTakingPhoto) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(Color.White, SpendlyLavenderLight))
                                )
                        )
                    }
                }

                // Nhập thủ công
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onManualEntryClick)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f))
                            .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Nhập thủ công",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Nhập tay",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiptViewfinderOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "receipt_scan_line")
    val lineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_progress"
    )

    Canvas(
        modifier = modifier.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    ) {
        drawRect(Color.Black.copy(alpha = 0.56f))

        val boxWidth = (size.width * 0.78f).coerceAtMost(size.width - 48.dp.toPx())
        val boxHeight = (boxWidth * 1.28f).coerceAtMost(size.height * 0.62f)
        val left = (size.width - boxWidth) / 2f
        val top = (size.height - boxHeight) / 2f - 24.dp.toPx()
        val cornerRadius = 22.dp.toPx()

        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            blendMode = BlendMode.Clear
        )

        drawRoundRect(
            color = Color.White.copy(alpha = 0.32f),
            topLeft = Offset(left, top),
            size = Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = 1.5.dp.toPx())
        )

        val scanY = top + cornerRadius + (boxHeight - cornerRadius * 2f) * lineProgress
        drawLine(
            brush = Brush.horizontalGradient(
                listOf(Color.Transparent, SpendlyLavender, SpendlySky, Color.Transparent)
            ),
            start = Offset(left + 12.dp.toPx(), scanY),
            end = Offset(left + boxWidth - 12.dp.toPx(), scanY),
            strokeWidth = 2.5.dp.toPx()
        )

        // 4 góc nổi bật
        val cornerLen = 28.dp.toPx()
        val stroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        val right = left + boxWidth
        val bottom = top + boxHeight

        val tl = Path().apply {
            moveTo(left, top + cornerLen)
            lineTo(left, top + cornerRadius)
            arcTo(Rect(left, top, left + cornerRadius * 2, top + cornerRadius * 2), 180f, 90f, false)
            lineTo(left + cornerLen, top)
        }
        val tr = Path().apply {
            moveTo(right - cornerLen, top)
            lineTo(right - cornerRadius, top)
            arcTo(Rect(right - cornerRadius * 2, top, right, top + cornerRadius * 2), 270f, 90f, false)
            lineTo(right, top + cornerLen)
        }
        val bl = Path().apply {
            moveTo(left, bottom - cornerLen)
            lineTo(left, bottom - cornerRadius)
            arcTo(Rect(left, bottom - cornerRadius * 2, left + cornerRadius * 2, bottom), 180f, -90f, false)
            lineTo(left + cornerLen, bottom)
        }
        val br = Path().apply {
            moveTo(right - cornerLen, bottom)
            lineTo(right - cornerRadius, bottom)
            arcTo(Rect(right - cornerRadius * 2, bottom - cornerRadius * 2, right, bottom), 90f, -90f, false)
            lineTo(right, bottom - cornerLen)
        }
        listOf(tl, tr, bl, br).forEach { path ->
            drawPath(path = path, color = SpendlyLavenderLight, style = stroke)
        }
    }
}

@Composable
private fun ReceiptAnalyzingStep(
    imagePath: String,
    groupName: String,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()

    Box(modifier = modifier.fillMaxSize()) {
        if (imagePath.isNotBlank()) {
            AsyncImage(
                model = File(imagePath),
                contentDescription = "Ảnh hóa đơn đang quét",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.32f)
            )
        }

        OrganicBackground(
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (imagePath.isNotBlank()) 0.80f else 1f),
            hazeState = hazeState
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState,
                shape = SpendlyShapeTokens.HeroCard,
                contentPadding = PaddingValues(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(SpendlyLavender, SpendlyIndigo, SpendlySky))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(44.dp),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = "FinSprout AI đang đọc hóa đơn...",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (groupName.isNotBlank()) {
                            "Đang bóc tách tên quán, tổng tiền, ngày chi và các món cho quỹ $groupName"
                        } else {
                            "Đang bóc tách tên quán, tổng tiền, danh mục và chi tiết mặt hàng"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    TextButton(onClick = onCancelClick) {
                        Text(
                            text = "Hủy & chụp ảnh khác",
                            color = colors.deepLavender,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReceiptReviewStep(
    uiState: ReceiptScannerUiState,
    targetMode: String,
    onBackClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCategorySelect: (SpendlyCategory) -> Unit,
    onPayerSelect: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onRetakeClick: () -> Unit,
    onContinueClick: () -> Unit,
    onQuickSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()
    var showFullImagePreview by remember { mutableStateOf(false) }

    val scanned = uiState.scannedResult
    val confidence = scanned?.confidence ?: ReceiptConfidence.MEDIUM
    val amountPreview = if (uiState.parsedAmount > 0L) {
        FintechAlgorithm.formatVnd(uiState.parsedAmount)
    } else {
        "0 đ"
    }

    Box(modifier = modifier.fillMaxSize()) {
        OrganicBackground(
            modifier = Modifier.fillMaxSize(),
            hazeState = hazeState
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = colors.textPrimary
                        )
                    }
                    Column {
                        Text(
                            text = "Kiểm tra hóa đơn AI",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        if (uiState.groupName.isNotBlank()) {
                            Text(
                                text = "Quỹ: ${uiState.groupName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                TextButton(onClick = onRetakeClick) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = null,
                        tint = colors.deepLavender,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Chụp lại", color = colors.deepLavender, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Thẻ ảnh hóa đơn + Độ tin cậy
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    hazeState = hazeState,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (uiState.capturedImagePath.isNotBlank() && File(uiState.capturedImagePath).exists()) {
                            Box(
                                modifier = Modifier
                                    .size(width = 76.dp, height = 96.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, colors.divider, RoundedCornerShape(14.dp))
                                    .clickable { showFullImagePreview = true }
                            ) {
                                AsyncImage(
                                    model = File(uiState.capturedImagePath),
                                    contentDescription = "Ảnh hóa đơn vừa chụp",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.55f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ZoomIn,
                                        contentDescription = "Phóng to",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ConfidenceBadge(confidence = confidence)
                            Text(
                                text = amountPreview,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.deepLavender
                            )
                            Text(
                                text = "Ngày hóa đơn: ${formatReceiptDate(uiState.editableDateMillis)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted
                            )
                        }
                    }

                    val warningText = scanned?.warningMessage
                    if (!warningText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SpendlyShapeTokens.CardSmall)
                                .background(colors.coralContainer.copy(alpha = 0.65f))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WarningAmber,
                                contentDescription = null,
                                tint = SpendlyCoralDeep,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = warningText,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textPrimary
                            )
                        }
                    }
                }

                // Danh sách món AI đọc được (nếu có)
                if (scanned != null && scanned.items.isNotEmpty()) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        hazeState = hazeState,
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Text(
                            text = "Các mục trích xuất (${scanned.items.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        scanned.items.take(8).forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (item.quantity > 1) "${item.quantity}x ${item.name}" else item.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (item.amount > 0L) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = FintechAlgorithm.formatVnd(item.amount),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Form chỉnh sửa nhanh
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    hazeState = hazeState,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Text(
                        text = "Thông tin khoản chi",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = uiState.editableTitle,
                        onValueChange = onTitleChange,
                        label = { Text("Tên cửa hàng / Nội dung chi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = SpendlyShapeTokens.CardSmall,
                        colors = reviewFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = uiState.editableAmountText,
                        onValueChange = onAmountChange,
                        label = { Text("Tổng tiền thanh toán (VNĐ)") },
                        suffix = { Text("đ", color = colors.textMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = SpendlyShapeTokens.CardSmall,
                        colors = reviewFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Danh mục",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SpendlyCategory.entries.forEach { category ->
                            val selected = category == uiState.editableCategory
                            SelectableReviewChip(
                                label = category.label,
                                selected = selected,
                                onClick = { onCategorySelect(category) }
                            )
                        }
                    }

                    if (uiState.members.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Người trả hóa đơn",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.members.forEach { member ->
                                SelectableReviewChip(
                                    label = member.displayName,
                                    selected = member.userId == uiState.editablePayerId,
                                    onClick = { onPayerSelect(member.userId) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.editableDescription,
                        onValueChange = onDescriptionChange,
                        label = { Text("Ghi chú & chi tiết món") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        shape = SpendlyShapeTokens.CardSmall,
                        colors = reviewFieldColors()
                    )
                }

                if (!uiState.errorMessage.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SpendlyShapeTokens.CardSmall)
                            .background(colors.coralContainer)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = SpendlyCoralDeep,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = SpendlyCoralDeep
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2 nút hành động đáy
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpendlyButton(
                    text = if (targetMode == "chat") "Đưa vào FinSprout Chat" else "Tiếp tục chia tiền",
                    onClick = onContinueClick,
                    enabled = !uiState.isSaving,
                    trailingIcon = Icons.AutoMirrored.Rounded.ArrowForward,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = onQuickSaveClick,
                    enabled = !uiState.isSaving && uiState.parsedAmount > 0L && uiState.editableTitle.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = SpendlyShapeTokens.Pill,
                    border = BorderStroke(1.2.dp, colors.deepLavender.copy(alpha = 0.55f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.deepLavender
                    )
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = colors.deepLavender,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (uiState.isSaving) "Đang lưu vào quỹ..." else "Lưu & Chia đều ngay",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }

    if (showFullImagePreview && uiState.capturedImagePath.isNotBlank()) {
        ReceiptFullImageDialog(
            imagePath = uiState.capturedImagePath,
            onDismiss = { showFullImagePreview = false }
        )
    }
}

@Composable
private fun ReceiptErrorStep(
    imagePath: String,
    errorMessage: String,
    canRetry: Boolean,
    onBackClick: () -> Unit,
    onRetakeClick: () -> Unit,
    onRetryClick: () -> Unit,
    onPickFromGalleryClick: () -> Unit,
    onManualWithPhotoClick: () -> Unit,
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
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState,
                shape = SpendlyShapeTokens.HeroCard,
                contentPadding = PaddingValues(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (imagePath.isNotBlank() && File(imagePath).exists()) {
                        AsyncImage(
                            model = File(imagePath),
                            contentDescription = "Ảnh hóa đơn",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 110.dp, height = 135.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, colors.divider, RoundedCornerShape(16.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(colors.coralContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = SpendlyCoralDeep,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Text(
                        text = "Chưa trích xuất được hóa đơn",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    SpendlyButton(
                        text = "Chụp lại hóa đơn",
                        onClick = onRetakeClick,
                        trailingIcon = Icons.Rounded.CameraAlt,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (canRetry) {
                        OutlinedButton(
                            onClick = onRetryClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = SpendlyShapeTokens.Pill,
                            border = BorderStroke(1.2.dp, colors.deepLavender.copy(alpha = 0.55f))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                tint = colors.deepLavender,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Thử phân tích lại với AI",
                                color = colors.deepLavender,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (imagePath.isNotBlank() && File(imagePath).exists()) {
                        SpendlyButton(
                            text = "Tự nhập thông tin kèm ảnh này",
                            onClick = onManualWithPhotoClick,
                            style = SpendlyButtonStyle.SECONDARY_GLASS,
                            trailingIcon = Icons.Rounded.Edit,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = onPickFromGalleryClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Chọn ảnh khác", color = colors.textPrimary)
                        }
                        if (imagePath.isBlank() || !File(imagePath).exists()) {
                            TextButton(
                                onClick = onManualWithPhotoClick,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Nhập thủ công", color = colors.deepLavender, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ReceiptCameraPermissionRequiredContent(
    hasRequestedOnce: Boolean,
    onBackClick: () -> Unit,
    onRequestPermissionClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    onPickFromGalleryClick: () -> Unit,
    onManualEntryClick: () -> Unit,
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
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState,
                shape = SpendlyShapeTokens.HeroCard,
                contentPadding = PaddingValues(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(colors.lavenderContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DocumentScanner,
                            contentDescription = null,
                            tint = colors.deepLavender,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Text(
                        text = "Cần quyền truy cập Camera",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Spendly dùng Camera để chụp ảnh hóa đơn và nhờ FinSprout AI đọc giúp số tiền, quán ăn & danh mục. Bạn vẫn có thể chọn ảnh có sẵn từ thư viện.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted,
                        textAlign = TextAlign.Center
                    )

                    SpendlyButton(
                        text = if (hasRequestedOnce) "Cấp quyền Camera" else "Cho phép sử dụng Camera",
                        onClick = onRequestPermissionClick,
                        trailingIcon = Icons.Rounded.CameraAlt,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (hasRequestedOnce) {
                        SpendlyButton(
                            text = "Mở Cài đặt ứng dụng",
                            onClick = onOpenSettingsClick,
                            style = SpendlyButtonStyle.SECONDARY_GLASS,
                            trailingIcon = Icons.Rounded.Settings,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    SpendlyButton(
                        text = "Chọn ảnh hóa đơn từ Thư viện",
                        onClick = onPickFromGalleryClick,
                        style = SpendlyButtonStyle.SECONDARY_GLASS,
                        trailingIcon = Icons.Rounded.PhotoLibrary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextButton(onClick = onManualEntryClick) {
                        Text(
                            text = "Nhập khoản chi thủ công",
                            color = colors.deepLavender,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ConfidenceBadge(confidence: ReceiptConfidence) {
    val colors = MaterialTheme.spendlyColors
    val (bgColor, textColor) = when (confidence) {
        ReceiptConfidence.HIGH -> colors.mintContainer to colors.mintDeep
        ReceiptConfidence.MEDIUM -> colors.amberContainer to colors.amberDeep
        ReceiptConfidence.LOW -> colors.coralContainer to colors.coralDeep
    }
    Box(
        modifier = Modifier
            .clip(SpendlyShapeTokens.Pill)
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = confidence.label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor
        )
    }
}

@Composable
private fun SelectableReviewChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.spendlyColors
    Row(
        modifier = Modifier
            .clip(SpendlyShapeTokens.Pill)
            .background(if (selected) colors.lavenderContainer else colors.surface.copy(alpha = 0.65f))
            .border(
                width = if (selected) 1.2.dp else 0.8.dp,
                color = if (selected) colors.deepLavender else colors.divider,
                shape = SpendlyShapeTokens.Pill
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = colors.deepLavender,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (selected) colors.onLavenderContainer else colors.textPrimary
        )
    }
}

@Composable
private fun CircleOverlayIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.48f))
            .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun ReceiptFullImageDialog(
    imagePath: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                .clickable(onClick = onDismiss)
        ) {
            AsyncImage(
                model = File(imagePath),
                contentDescription = "Ảnh hóa đơn toàn màn hình",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(16.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Đóng",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun reviewFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.spendlyColors.primaryLavender,
    unfocusedBorderColor = MaterialTheme.spendlyColors.divider,
    focusedLabelColor = MaterialTheme.spendlyColors.deepLavender,
    unfocusedLabelColor = MaterialTheme.spendlyColors.textMuted,
    focusedTextColor = MaterialTheme.spendlyColors.textPrimary,
    unfocusedTextColor = MaterialTheme.spendlyColors.textPrimary
)

private fun formatReceiptDate(millis: Long): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("vi-VN")).format(Date(millis))
}

private fun Context.hasReceiptCameraPermission(): Boolean {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED
}

private fun Context.openReceiptAppPermissionSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}

@Preview(showBackground = true, widthDp = 412, heightDp = 820)
@Composable
private fun ReceiptReviewPreview() {
    SpendlyTheme {
        ReceiptReviewStep(
            uiState = ReceiptScannerUiState(
                step = ReceiptScanStep.REVIEW,
                groupName = "Quỹ Chi Tiêu Nhóm 08",
                editableTitle = "Highlands Coffee - Nguyễn Huệ",
                editableAmountText = "245000",
                editableCategory = SpendlyCategory.FOOD,
                editableDescription = "Chi tiết hóa đơn: 2x Trà sen vàng (98.000 đ), 1x Phin sữa đá (49.000 đ)",
                scannedResult = ScannedReceiptResult(
                    title = "Highlands Coffee - Nguyễn Huệ",
                    totalAmount = 245000L,
                    category = SpendlyCategory.FOOD,
                    confidence = ReceiptConfidence.HIGH,
                    items = listOf(
                        ReceiptLineItem("Trà sen vàng", 2, 98000L),
                        ReceiptLineItem("Phin sữa đá", 1, 49000L)
                    )
                )
            ),
            targetMode = "new_expense",
            onBackClick = {},
            onTitleChange = {},
            onAmountChange = {},
            onCategorySelect = {},
            onPayerSelect = {},
            onDescriptionChange = {},
            onRetakeClick = {},
            onContinueClick = {},
            onQuickSaveClick = {}
        )
    }
}
