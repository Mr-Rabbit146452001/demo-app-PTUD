package com.example.nhoms8

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.nhoms8.ui.theme.SpendlyTheme
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Màn hình Splash Video khởi động Spendly (4 giây, nền trắng thuần #FFFFFF, logo ví 3D ở giữa).
 */
@Composable
fun SplashVideoScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val latestOnFinished by rememberUpdatedState(onSplashFinished)
    val isInspectionMode = LocalInspectionMode.current

    if (isInspectionMode) {
        SplashFallbackContent(modifier = modifier)
        return
    }

    val context = LocalContext.current
    val finishedOnce = remember { AtomicBoolean(false) }
    var isVideoReady by remember { mutableStateOf(false) }
    var hasPlaybackError by remember { mutableStateOf(false) }

    val mediaPlayerHolder = remember { arrayOfNulls<MediaPlayer>(1) }
    val surfaceHolder = remember { arrayOfNulls<Surface>(1) }

    fun finishOnce() {
        if (finishedOnce.compareAndSet(false, true)) {
            latestOnFinished()
        }
    }

    // Timeout an toàn đảm bảo luôn điều hướng tiếp kể cả khi thiết bị không phát được video
    LaunchedEffect(Unit) {
        delay(4_500L)
        finishOnce()
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching {
                mediaPlayerHolder[0]?.let { mp ->
                    if (mp.isPlaying) {
                        mp.stop()
                    }
                    mp.release()
                }
            }
            mediaPlayerHolder[0] = null
            runCatching { surfaceHolder[0]?.release() }
            surfaceHolder[0] = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (hasPlaybackError) {
            SplashFallbackContent()
        } else {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (isVideoReady) 1f else 0f),
                factory = { ctx ->
                    TextureView(ctx).apply {
                        isOpaque = false
                        var latestVideoWidth = 0
                        var latestVideoHeight = 0

                        fun applyFitCenterTransform(viewWidth: Int, viewHeight: Int) {
                            if (viewWidth <= 0 || viewHeight <= 0 || latestVideoWidth <= 0 || latestVideoHeight <= 0) {
                                return
                            }
                            val viewAspect = viewWidth.toFloat() / viewHeight.toFloat()
                            val videoAspect = latestVideoWidth.toFloat() / latestVideoHeight.toFloat()
                            val (scaleX, scaleY) = if (videoAspect > viewAspect) {
                                1f to (viewAspect / videoAspect)
                            } else {
                                (videoAspect / viewAspect) to 1f
                            }
                            val matrix = Matrix().apply {
                                setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)
                            }
                            setTransform(matrix)
                        }

                        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                            override fun onSurfaceTextureAvailable(
                                surfaceTexture: SurfaceTexture,
                                width: Int,
                                height: Int
                            ) {
                                runCatching { surfaceHolder[0]?.release() }
                                val surface = Surface(surfaceTexture)
                                surfaceHolder[0] = surface

                                runCatching {
                                    mediaPlayerHolder[0]?.release()
                                }
                                val videoUri = Uri.parse("android.resource://${ctx.packageName}/${R.raw.splash_screen}")
                                val player = MediaPlayer()
                                mediaPlayerHolder[0] = player

                                try {
                                    player.setDataSource(ctx, videoUri)
                                    player.setSurface(surface)
                                    player.isLooping = false
                                    player.setVolume(0f, 0f)
                                    player.setOnVideoSizeChangedListener { _, vWidth, vHeight ->
                                        latestVideoWidth = vWidth
                                        latestVideoHeight = vHeight
                                        applyFitCenterTransform(this@apply.width, this@apply.height)
                                    }
                                    player.setOnInfoListener { _, what, _ ->
                                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                                            isVideoReady = true
                                        }
                                        false
                                    }
                                    player.setOnPreparedListener { mp ->
                                        latestVideoWidth = mp.videoWidth
                                        latestVideoHeight = mp.videoHeight
                                        applyFitCenterTransform(this@apply.width, this@apply.height)
                                        mp.start()
                                    }
                                    player.setOnCompletionListener {
                                        finishOnce()
                                    }
                                    player.setOnErrorListener { _, _, _ ->
                                        hasPlaybackError = true
                                        true
                                    }
                                    player.prepareAsync()
                                } catch (_: Throwable) {
                                    hasPlaybackError = true
                                }
                            }

                            override fun onSurfaceTextureSizeChanged(
                                surfaceTexture: SurfaceTexture,
                                width: Int,
                                height: Int
                            ) {
                                applyFitCenterTransform(width, height)
                            }

                            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                                runCatching {
                                    mediaPlayerHolder[0]?.let { mp ->
                                        if (mp.isPlaying) {
                                            mp.stop()
                                        }
                                        mp.release()
                                    }
                                }
                                mediaPlayerHolder[0] = null
                                runCatching { surfaceHolder[0]?.release() }
                                surfaceHolder[0] = null
                                return true
                            }

                            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
                                if (!isVideoReady && mediaPlayerHolder[0]?.isPlaying == true) {
                                    isVideoReady = true
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun SplashFallbackContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.spendly_app_logo),
            contentDescription = "Spendly Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(150.dp)
        )
    }
}

@Preview(
    name = "Splash Video Fallback · White",
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun SplashVideoScreenPreview() {
    SpendlyTheme(darkTheme = false) {
        SplashFallbackContent()
    }
}
