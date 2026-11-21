package com.example.nhoms8.feature.chatbot

import android.content.Context
import android.graphics.BitmapFactory
import android.os.SystemClock
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.nhoms8.R
import com.example.nhoms8.SproutIconBot
import com.example.nhoms8.ui.theme.SpendlyIndigo
import com.example.nhoms8.ui.theme.SpendlyLavender
import com.example.nhoms8.ui.theme.SpendlySky
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private const val AVATAR_SPRITE_COLUMNS = 12
private const val AVATAR_SPRITE_ROWS = 10
private const val AVATAR_TOTAL_FRAMES = 120
private const val AVATAR_FRAME_INTERVAL_MS = 83L

private object FinSproutAvatarSpriteCache {
    @Volatile
    private var cachedBitmap: ImageBitmap? = null

    @Synchronized
    fun getOrLoad(context: Context): ImageBitmap? {
        cachedBitmap?.let { return it }
        val decoded = runCatching {
            val options = BitmapFactory.Options().apply {
                inScaled = false
            }
            BitmapFactory.decodeResource(
                context.applicationContext.resources,
                R.drawable.finsprout_avatar_sprite,
                options
            )?.asImageBitmap()
        }.getOrNull()
        if (decoded != null) {
            cachedBitmap = decoded
        }
        return decoded
    }
}

/**
 * Avatar động không âm thanh của Trợ lý AI FinSprout (120 khung hình @ ~12fps trích xuất từ video),
 * dùng chung một sprite sheet WebP cho mọi bong bóng nổi, tiêu đề và tin nhắn bot.
 */
@Composable
fun FinSproutAnimatedAvatar(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isInspectionMode = LocalInspectionMode.current
    val sprite = remember(context) { FinSproutAvatarSpriteCache.getOrLoad(context) }

    if (sprite == null) {
        Box(
            modifier = modifier.background(
                Brush.linearGradient(
                    colors = listOf(SpendlyLavender, SpendlyIndigo, SpendlySky)
                )
            ),
            contentAlignment = Alignment.Center
        ) {
            SproutIconBot(
                color = Color.White,
                modifier = Modifier.fillMaxSize(0.56f)
            )
        }
        return
    }

    val frameIndex = if (isInspectionMode) {
        0
    } else {
        val animatedFrame by produceState(
            initialValue = ((SystemClock.uptimeMillis() / AVATAR_FRAME_INTERVAL_MS) % AVATAR_TOTAL_FRAMES).toInt()
        ) {
            while (true) {
                delay(AVATAR_FRAME_INTERVAL_MS)
                value = ((SystemClock.uptimeMillis() / AVATAR_FRAME_INTERVAL_MS) % AVATAR_TOTAL_FRAMES).toInt()
            }
        }
        animatedFrame
    }

    Canvas(modifier = modifier) {
        val dstWidth = size.width.roundToInt().coerceAtLeast(1)
        val dstHeight = size.height.roundToInt().coerceAtLeast(1)
        val cellW = (sprite.width / AVATAR_SPRITE_COLUMNS).coerceAtLeast(1)
        val cellH = (sprite.height / AVATAR_SPRITE_ROWS).coerceAtLeast(1)
        val safeIndex = frameIndex.coerceIn(0, AVATAR_TOTAL_FRAMES - 1)
        val col = safeIndex % AVATAR_SPRITE_COLUMNS
        val row = safeIndex / AVATAR_SPRITE_COLUMNS

        val dstSize = IntSize(dstWidth, dstHeight)
        val srcSize = IntSize(cellW, cellH)

        drawImage(
            image = sprite,
            srcOffset = IntOffset(col * cellW, row * cellH),
            srcSize = srcSize,
            dstSize = dstSize,
            filterQuality = FilterQuality.Medium
        )

        if (safeIndex >= AVATAR_TOTAL_FRAMES - 4) {
            val crossFadeAlpha = ((safeIndex - (AVATAR_TOTAL_FRAMES - 5)) / 5f).coerceIn(0f, 1f)
            drawImage(
                image = sprite,
                srcOffset = IntOffset.Zero,
                srcSize = srcSize,
                dstSize = dstSize,
                alpha = crossFadeAlpha,
                filterQuality = FilterQuality.Medium
            )
        }
    }
}
