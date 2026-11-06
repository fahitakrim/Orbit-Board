package org.futo.inputmethod.latin.uix.theme.presets

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.theme.ThemeOption

// Nothing OS Radar Wave Palette
val RadarWaveRed = Color(0xFFD71921)
val RadarWaveDark = Color(0xFF141417)
val RadarWaveKeyBg = Color(0xFF1C1D21)
val RadarWaveBorder = Color(0xFF28282E)
val RadarWaveWhite = Color(0xFFF2F2F5)
val RadarWaveBlack = Color(0xFF000000)

private val radarWaveColorScheme = darkColorScheme(
    primary = RadarWaveRed,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = RadarWaveDark,
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFF141417),
    secondaryContainer = RadarWaveDark,
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = RadarWaveRed,
    onTertiary = Color(0xFFFFFFFF),
    background = RadarWaveBlack,
    onBackground = RadarWaveWhite,
    surface = RadarWaveBlack,
    onSurface = RadarWaveWhite,
    surfaceVariant = RadarWaveDark,
    onSurfaceVariant = RadarWaveWhite,
    outline = RadarWaveBorder,
    outlineVariant = Color(0xFF2B2D31)
)

val RadarWaveTheme = ThemeOption(
    dynamic = false,
    key = "NothingRadarWave",
    name = R.string.theme_radar_wave,
    available = { true }
) {
    radarWaveColorScheme
}

private val RadarNdot = FontFamily(Font(R.font.ndot, FontWeight.Normal))

@Composable
fun RadarWavePreviewKeyboard(
    modifier: Modifier = Modifier,
    spacebarColor: Color = RadarWaveDark,
    spacebarBorderColor: Color? = null,
    hasKeyBorders: Boolean = true,
    keyHeight: androidx.compose.ui.unit.Dp = 16.dp
) {
    val row1 = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P")
    val row2 = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L")
    val row3 = listOf("Z", "X", "C", "V", "B", "N", "M")

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
            row1.forEach { char ->
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(keyHeight)
                        .then(
                            if (hasKeyBorders) Modifier.border(0.5.dp, RadarWaveBorder, RoundedCornerShape(3.dp))
                            else Modifier
                        ),
                    color = if (hasKeyBorders) RadarWaveKeyBg else Color.Transparent,
                    shape = RoundedCornerShape(3.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = char,
                            color = RadarWaveWhite,
                            fontSize = 9.sp,
                            fontFamily = RadarNdot
                        )
                    }
                }
            }
        }
        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(0.92f),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
        ) {
            row2.forEach { char ->
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(keyHeight)
                        .then(
                            if (hasKeyBorders) Modifier.border(0.5.dp, RadarWaveBorder, RoundedCornerShape(3.dp))
                            else Modifier
                        ),
                    color = if (hasKeyBorders) RadarWaveKeyBg else Color.Transparent,
                    shape = RoundedCornerShape(3.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = char,
                            color = RadarWaveWhite,
                            fontSize = 9.sp,
                            fontFamily = RadarNdot
                        )
                    }
                }
            }
        }
        // Row 3
        Row(
            modifier = Modifier.fillMaxWidth(0.80f),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
        ) {
            row3.forEach { char ->
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(keyHeight)
                        .then(
                            if (hasKeyBorders) Modifier.border(0.5.dp, RadarWaveBorder, RoundedCornerShape(3.dp))
                            else Modifier
                        ),
                    color = if (hasKeyBorders) RadarWaveKeyBg else Color.Transparent,
                    shape = RoundedCornerShape(3.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = char,
                            color = RadarWaveWhite,
                            fontSize = 9.sp,
                            fontFamily = RadarNdot
                        )
                    }
                }
            }
        }
        // Row 4 (Spacebar row)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
        ) {
            Box(modifier = Modifier.weight(1.2f).height(keyHeight), contentAlignment = Alignment.Center) {
                Text("?123", color = RadarWaveRed, fontSize = 8.sp, fontFamily = RadarNdot)
            }
            Box(modifier = Modifier.weight(0.9f).height(keyHeight), contentAlignment = Alignment.Center) {
                Text("☺", color = RadarWaveWhite, fontSize = 8.sp)
            }
            Surface(
                modifier = Modifier.weight(3.5f).height(keyHeight),
                color = spacebarColor,
                border = if (spacebarBorderColor != null) androidx.compose.foundation.BorderStroke(1.dp, spacebarBorderColor) else null,
                shape = RoundedCornerShape(3.dp)
            ) {}
            Box(modifier = Modifier.weight(0.9f).height(keyHeight), contentAlignment = Alignment.Center) {
                Text(".", color = RadarWaveWhite, fontSize = 8.sp, fontFamily = RadarNdot)
            }
            Surface(
                modifier = Modifier.weight(1.2f).height(keyHeight),
                color = RadarWaveRed,
                shape = RoundedCornerShape(3.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("↵", color = Color.White, fontSize = 10.sp)
                }
            }
        }
    }
}
