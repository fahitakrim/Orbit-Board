package org.futo.inputmethod.latin.uix.theme.presets

import androidx.compose.foundation.layout.*
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

// Matte Bauhaus Nothing Theme Palette
val OrbitMatteYellow = Color(0xFFE2B438)
val OrbitMatteBlue = Color(0xFF2A5288)
val OrbitMatteRed = Color(0xFFC23636)
val OrbitMatteWhite = Color(0xFFEAE6E1)
val OrbitMatteDark = Color(0xFF1C1D20)
val OrbitBlack = Color(0xFF000000)

private val orbitColorScheme = darkColorScheme(
    primary = OrbitMatteRed,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = OrbitMatteDark,
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = OrbitMatteBlue,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = OrbitMatteDark,
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = OrbitMatteYellow,
    onTertiary = Color(0xFF121212),
    background = OrbitBlack,
    onBackground = Color(0xFFFFFFFF),
    surface = OrbitBlack,
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = OrbitMatteDark,
    onSurfaceVariant = Color(0xFFFFFFFF),
    outline = OrbitMatteYellow,
    outlineVariant = Color(0xFF2B2D31)
)

val OrbitTheme = ThemeOption(
    dynamic = false,
    key = "OrbitTheme",
    name = R.string.theme_orbit,
    available = { true }
) {
    orbitColorScheme
}

@Composable
fun OrbitPreviewKeyboard(
    modifier: Modifier = Modifier,
    spacebarColor: Color = OrbitMatteDark,
    spacebarBorderColor: Color? = null,
    hasKeyBorders: Boolean = true,
    keyHeight: androidx.compose.ui.unit.Dp = 16.dp
) {
    val yellow = OrbitMatteYellow
    val blue = OrbitMatteBlue
    val dark = OrbitMatteDark
    val white = OrbitMatteWhite
    val red = OrbitMatteRed

    val row1 = listOf(yellow, blue, dark, white, red, blue, white, red, yellow, dark)
    val row2 = listOf(white, red, blue, dark, yellow, white, red, blue, dark)
    val row3 = listOf(yellow, dark, yellow, white, red, blue, dark, yellow, white)
    val keyBorderStroke = if (hasKeyBorders) androidx.compose.foundation.BorderStroke(0.5.dp, Color(0x33FFFFFF)) else null

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
                    border = keyBorderStroke,
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
                    border = keyBorderStroke,
                    shape = RoundedCornerShape(3.dp)
                ) {}
            }
        }
        // Row 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
        ) {
            row3.forEach { color ->
                Surface(
                    modifier = Modifier.weight(1f).height(keyHeight),
                    color = color,
                    border = keyBorderStroke,
                    shape = RoundedCornerShape(3.dp)
                ) {}
            }
        }
        // Row 4
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
        ) {
            Surface(modifier = Modifier.weight(1.2f).height(keyHeight), color = red, border = keyBorderStroke, shape = RoundedCornerShape(3.dp)) {}
            Surface(modifier = Modifier.weight(0.9f).height(keyHeight), color = blue, border = keyBorderStroke, shape = RoundedCornerShape(3.dp)) {}
            Surface(modifier = Modifier.weight(0.9f).height(keyHeight), color = dark, border = keyBorderStroke, shape = RoundedCornerShape(3.dp)) {}
            Surface(
                modifier = Modifier.weight(3.5f).height(keyHeight),
                color = spacebarColor,
                border = if (spacebarBorderColor != null) androidx.compose.foundation.BorderStroke(1.2.dp, spacebarBorderColor) else keyBorderStroke,
                shape = RoundedCornerShape(3.dp)
            ) {}
            Surface(modifier = Modifier.weight(0.9f).height(keyHeight), color = white, border = keyBorderStroke, shape = RoundedCornerShape(3.dp)) {}
            Surface(modifier = Modifier.weight(1.2f).height(keyHeight), color = red, border = keyBorderStroke, shape = RoundedCornerShape(3.dp)) {}
        }
    }
}
