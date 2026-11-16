package org.futo.inputmethod.latin.uix.theme.presets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.theme.ThemeOption

val RetroBlocksTheme = ThemeOption(
    dynamic = false,
    key = "RetroBlocks",
    name = R.string.retro_blocks_theme_name,
    available = { true },
    obtainColors = {
        darkColorScheme(
            primary = Color(0xFFFED700),          // Cyber Yellow
            onPrimary = Color(0xFF121212),
            primaryContainer = Color(0xFF232621), // Dark Charcoal
            onPrimaryContainer = Color(0xFFFFFFFF),
            secondary = Color(0xFF9EA637),        // Light Olive
            onSecondary = Color(0xFF121212),
            secondaryContainer = Color(0xFF4A644B), // Muted Forest
            onSecondaryContainer = Color(0xFFFFFFFF),
            tertiary = Color(0xFFBA1A1A),         // Crimson Red
            onTertiary = Color(0xFFFFFFFF),
            background = Color(0xFF000000),       // Pure AMOLED Black
            onBackground = Color(0xFFFFFFFF),
            surface = Color(0xFF000000),
            onSurface = Color(0xFFFFFFFF),
            outline = Color(0xFF28282E),
            surfaceVariant = Color(0xFF141416),
            onSurfaceVariant = Color(0xFF8E8E93)
        )
    }
)

@Composable
fun RetroBlocksPreviewKeyboard(
    modifier: Modifier = Modifier,
    keyHeight: androidx.compose.ui.unit.Dp = 16.dp
) {
    val yellow = Color(0xFFFED700)
    val olive = Color(0xFF9EA637)
    val forest = Color(0xFF4A644B)
    val charcoal = Color(0xFF232621)
    val cream = Color(0xFFF0EFE6)
    val red = Color(0xFFBA1A1A)
    val black = Color(0xFF000000)

    val row1 = listOf(olive, forest, charcoal, cream, olive, forest, cream, charcoal, yellow, charcoal)
    val row2 = listOf(cream, olive, forest, charcoal, yellow, cream, olive, forest, charcoal)
    val row3 = listOf(olive, charcoal, yellow, cream, olive, forest, charcoal, olive, red)

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
            Surface(modifier = Modifier.weight(1.2f).height(keyHeight), color = olive, shape = RoundedCornerShape(3.dp)) {}
            Surface(modifier = Modifier.weight(0.9f).height(keyHeight), color = forest, shape = RoundedCornerShape(percent = 50)) {}
            Surface(modifier = Modifier.weight(0.9f).height(keyHeight), color = forest, shape = RoundedCornerShape(3.dp)) {}
            Surface(
                modifier = Modifier.weight(3.5f).height(keyHeight),
                color = black,
                border = BorderStroke(1.2.dp, yellow),
                shape = RoundedCornerShape(3.dp)
            ) {}
            Surface(modifier = Modifier.weight(0.9f).height(keyHeight), color = charcoal, shape = RoundedCornerShape(3.dp)) {}
            Surface(modifier = Modifier.weight(1.2f).height(keyHeight), color = olive, shape = RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp, topEnd = 10.dp, bottomEnd = 10.dp)) {}
        }
    }
}

