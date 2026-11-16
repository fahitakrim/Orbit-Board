package org.futo.inputmethod.latin.uix.theme.presets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.theme.ThemeOption

val TactileSlateTheme = ThemeOption(
    dynamic = false,
    key = "TactileSlate",
    name = R.string.tactile_slate_theme_name,
    available = { true },
    obtainColors = {
        lightColorScheme(
            primary = Color(0xFFD71921),          // Nothing Red (Backspace & Accents)
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFF404040), // Dark Charcoal Modifiers
            onPrimaryContainer = Color(0xFFFFFFFF),
            secondary = Color(0xFF404040),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFB4B7BD),
            onSecondaryContainer = Color(0xFF1C1D20),
            tertiary = Color(0xFFD71921),
            onTertiary = Color(0xFFFFFFFF),
            background = Color(0xFFD0D2D6),       // Metallic Silver / Light Gray Chassis
            onBackground = Color(0xFF1C1D20),
            surface = Color(0xFFD0D2D6),
            onSurface = Color(0xFF1C1D20),
            outline = Color(0xFFB4B7BD),
            surfaceVariant = Color(0xFFC0C3C8),
            onSurfaceVariant = Color(0xFF404040)
        )
    }
)

@Composable
fun TactileSlatePreviewKeyboard(
    modifier: Modifier = Modifier,
    keyHeight: androidx.compose.ui.unit.Dp = 16.dp
) {
    val chassis = Color(0xFFD0D2D6)
    val whiteKey = Color(0xFFFFFFFF)
    val whiteBevel = Color(0xFFB4B7BD)
    val charcoalKey = Color(0xFF404040)
    val charcoalBevel = Color(0xFF262626)
    val redKey = Color(0xFFD71921)
    val redBevel = Color(0xFF9E1218)

    @Composable
    fun RowScope.TactileKey(
        weight: Float,
        faceColor: Color,
        bevelColor: Color,
        shapeRadius: androidx.compose.ui.unit.Dp = 3.dp
    ) {
        Box(
            modifier = Modifier
                .weight(weight)
                .height(keyHeight)
                .clip(RoundedCornerShape(shapeRadius))
                .background(bevelColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(keyHeight - 2.dp)
                    .clip(RoundedCornerShape(shapeRadius))
                    .background(faceColor)
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(chassis)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Row 1 (Q..P)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
        ) {
            repeat(10) {
                TactileKey(weight = 1f, faceColor = whiteKey, bevelColor = whiteBevel)
            }
        }

        // Row 2 (A..L)
        Row(
            modifier = Modifier.fillMaxWidth(0.92f),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
        ) {
            repeat(9) {
                TactileKey(weight = 1f, faceColor = whiteKey, bevelColor = whiteBevel)
            }
        }

        // Row 3 (Shift, Z..M, Red Backspace)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
        ) {
            // Shift (Dark Charcoal)
            TactileKey(weight = 1.4f, faceColor = charcoalKey, bevelColor = charcoalBevel)

            // Z..M (7 keys)
            repeat(7) {
                TactileKey(weight = 1f, faceColor = whiteKey, bevelColor = whiteBevel)
            }

            // Backspace (Nothing Red - "just backspace ta red hobe")
            TactileKey(weight = 1.4f, faceColor = redKey, bevelColor = redBevel)
        }

        // Row 4 (12!?, Emoji, comma, Spacebar, period, Enter)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
        ) {
            // 12!? (Dark Charcoal)
            TactileKey(weight = 1.2f, faceColor = charcoalKey, bevelColor = charcoalBevel)
            // Emoji (Dark Charcoal)
            TactileKey(weight = 0.9f, faceColor = charcoalKey, bevelColor = charcoalBevel)
            // Comma (White)
            TactileKey(weight = 0.9f, faceColor = whiteKey, bevelColor = whiteBevel)
            // Spacebar (White)
            TactileKey(weight = 3.5f, faceColor = whiteKey, bevelColor = whiteBevel)
            // Period (White)
            TactileKey(weight = 0.9f, faceColor = whiteKey, bevelColor = whiteBevel)
            // Enter (Dark Charcoal)
            TactileKey(weight = 1.2f, faceColor = charcoalKey, bevelColor = charcoalBevel)
        }
    }
}
