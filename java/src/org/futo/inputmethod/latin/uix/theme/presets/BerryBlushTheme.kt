package org.futo.inputmethod.latin.uix.theme.presets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.theme.ThemeOption

val BerryBlushTheme = ThemeOption(
    dynamic = false,
    key = "BerryBlush",
    name = R.string.berry_blush_theme_name,
    available = { true },
    obtainColors = {
        darkColorScheme(
            primary = Color(0xFFFAD0DC),          // Soft Blush Pink (#FAD0DC)
            onPrimary = Color(0xFF5E1D35),        // Dark Ink (#5E1D35)
            primaryContainer = Color(0xFFB83362), // Berry Rose (#B83362)
            onPrimaryContainer = Color(0xFFFFF8FA),
            secondary = Color(0xFFB87D8B),        // Rose Mauve (#B87D8B)
            onSecondary = Color(0xFF5E1D35),
            secondaryContainer = Color(0xFFD8C8E8), // Pastel Lavender (#D8C8E8)
            onSecondaryContainer = Color(0xFF5E1D35),
            tertiary = Color(0xFFFF3E8A),         // Hot Pink (#FF3E8A)
            onTertiary = Color(0xFFFFFFFF),
            background = Color(0xFF000000),       // Pure AMOLED Black
            onBackground = Color(0xFFFFF8FA),
            surface = Color(0xFF000000),
            onSurface = Color(0xFFFFF8FA),
            outline = Color(0xFF3A1D2C),
            surfaceVariant = Color(0xFF13090F),
            onSurfaceVariant = Color(0xFFFAD0DC)
        )
    }
)

@Composable
fun BerryBlushPreviewKeyboard(
    modifier: Modifier = Modifier,
    keyHeight: androidx.compose.ui.unit.Dp = 16.dp
) {
    val blushPink = Color(0xFFFAD0DC)
    val bubblegumPink = Color(0xFFF9B4CA)
    val lavender = Color(0xFFD8C8E8)
    val roseMauve = Color(0xFFB87D8B)
    val warmCream = Color(0xFFFAF5EE)
    val hotPink = Color(0xFFFF3E8A)
    val berryRose = Color(0xFFB83362)
    val darkInk = Color(0xFF5E1D35)

    // Row 1: q w e r t y u i o p
    val row1 = listOf(blushPink, lavender, roseMauve, warmCream, bubblegumPink, roseMauve, lavender, warmCream, blushPink, roseMauve)
    // Row 2: a s d f g h j k l
    val row2 = listOf(warmCream, lavender, blushPink, roseMauve, warmCream, lavender, blushPink, roseMauve, warmCream)
    // Row 3: shift (rose), z..m, backspace (hot pink)
    val row3 = listOf(roseMauve, warmCream, bubblegumPink, lavender, roseMauve, warmCream, bubblegumPink, lavender, hotPink)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
        ) {
            row1.forEach { color ->
                Surface(
                    modifier = Modifier.weight(1f).height(keyHeight),
                    color = color,
                    shape = RoundedCornerShape(3.dp)
                ) {}
            }
        }

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(0.92f),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
        ) {
            row2.forEach { color ->
                Surface(
                    modifier = Modifier.weight(1f).height(keyHeight),
                    color = color,
                    shape = RoundedCornerShape(3.dp)
                ) {}
            }
        }

        // Row 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
        ) {
            row3.forEachIndexed { idx, color ->
                val weight = if (idx == 0 || idx == row3.size - 1) 1.4f else 1f
                Surface(
                    modifier = Modifier.weight(weight).height(keyHeight),
                    color = color,
                    shape = RoundedCornerShape(3.dp)
                ) {}
            }
        }

        // Row 4
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
        ) {
            // ?123 (Lavender with cat ears)
            Box(
                modifier = Modifier.weight(1.2f).height(keyHeight),
                contentAlignment = Alignment.Center
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = lavender, shape = RoundedCornerShape(3.dp)) {}
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val earW = size.width * 0.24f
                    val leftPath = Path().apply {
                        moveTo(size.width * 0.12f, size.height * 0.35f)
                        lineTo(size.width * 0.12f + earW / 2f, 0f)
                        lineTo(size.width * 0.12f + earW, size.height * 0.35f)
                        close()
                    }
                    drawPath(leftPath, lavender)
                    val rightPath = Path().apply {
                        moveTo(size.width * 0.88f - earW, size.height * 0.35f)
                        lineTo(size.width * 0.88f - earW / 2f, 0f)
                        lineTo(size.width * 0.88f, size.height * 0.35f)
                        close()
                    }
                    drawPath(rightPath, lavender)
                }
            }

            // Emoji (Cat ears oval)
            Box(
                modifier = Modifier.weight(0.9f).height(keyHeight),
                contentAlignment = Alignment.Center
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = blushPink, shape = RoundedCornerShape(percent = 50)) {}
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val earW = size.width * 0.28f
                    val leftPath = Path().apply {
                        moveTo(size.width * 0.15f, size.height * 0.35f)
                        lineTo(size.width * 0.15f + earW / 2f, 0f)
                        lineTo(size.width * 0.15f + earW, size.height * 0.35f)
                        close()
                    }
                    drawPath(leftPath, blushPink)
                    val rightPath = Path().apply {
                        moveTo(size.width * 0.85f - earW, size.height * 0.35f)
                        lineTo(size.width * 0.85f - earW / 2f, 0f)
                        lineTo(size.width * 0.85f, size.height * 0.35f)
                        close()
                    }
                    drawPath(rightPath, blushPink)
                }
            }

            // Comma
            Surface(modifier = Modifier.weight(0.9f).height(keyHeight), color = warmCream, shape = RoundedCornerShape(3.dp)) {}

            // Spacebar: Blush Pink with dark-berry rim and pink bow
            Box(
                modifier = Modifier
                    .weight(3.5f)
                    .height(keyHeight)
                    .clip(RoundedCornerShape(4.dp))
                    .background(blushPink)
                    .border(0.8.dp, darkInk.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    // Pink bow center knot
                    drawCircle(Color(0xFFFF6B9D), radius = 1.3.dp.toPx(), center = Offset(cx, cy))
                    // Bow loops
                    drawCircle(Color(0xFFFF6B9D), radius = 1.0.dp.toPx(), center = Offset(cx - 3.5.dp.toPx(), cy - 1.2.dp.toPx()))
                    drawCircle(Color(0xFFFF6B9D), radius = 1.0.dp.toPx(), center = Offset(cx + 3.5.dp.toPx(), cy - 1.2.dp.toPx()))
                    // Sparkles
                    drawCircle(Color.White, radius = 0.8.dp.toPx(), center = Offset(cx - 10.dp.toPx(), cy))
                    drawCircle(Color.White, radius = 0.8.dp.toPx(), center = Offset(cx + 10.dp.toPx(), cy))
                }
            }

            // Period
            Surface(modifier = Modifier.weight(0.9f).height(keyHeight), color = roseMauve, shape = RoundedCornerShape(3.dp)) {}

            // Enter (Cat ears oval - Berry Rose)
            Box(
                modifier = Modifier.weight(1.2f).height(keyHeight),
                contentAlignment = Alignment.Center
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = berryRose, shape = RoundedCornerShape(percent = 50)) {}
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val earW = size.width * 0.24f
                    val leftPath = Path().apply {
                        moveTo(size.width * 0.15f, size.height * 0.35f)
                        lineTo(size.width * 0.15f + earW / 2f, 0f)
                        lineTo(size.width * 0.15f + earW, size.height * 0.35f)
                        close()
                    }
                    drawPath(leftPath, berryRose)
                    val rightPath = Path().apply {
                        moveTo(size.width * 0.85f - earW, size.height * 0.35f)
                        lineTo(size.width * 0.85f - earW / 2f, 0f)
                        lineTo(size.width * 0.85f, size.height * 0.35f)
                        close()
                    }
                    drawPath(rightPath, berryRose)
                }
            }
        }
    }
}
