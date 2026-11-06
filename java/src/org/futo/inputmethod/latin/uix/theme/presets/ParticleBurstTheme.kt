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

// Bauhaus Micro Glyph Spark Particle Palette
val ParticleBurstRed = Color(0xFFD71921)
val ParticleBurstYellow = Color(0xFFE2B438)
val ParticleBurstBlue = Color(0xFF3B72BA)
val ParticleBurstWhite = Color(0xFFFFFFFF)
val ParticleBurstKeyBg = Color(0xFF16171B)
val ParticleBurstBorder = Color(0xFF27282F)
val ParticleBurstDark = Color(0xFF101013)
val ParticleBurstBlack = Color(0xFF000000)

private val particleBurstColorScheme = darkColorScheme(
    primary = ParticleBurstRed,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = ParticleBurstDark,
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = ParticleBurstBlue,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = ParticleBurstDark,
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = ParticleBurstYellow,
    onTertiary = Color(0xFF101013),
    background = ParticleBurstBlack,
    onBackground = ParticleBurstWhite,
    surface = ParticleBurstBlack,
    onSurface = ParticleBurstWhite,
    surfaceVariant = ParticleBurstDark,
    onSurfaceVariant = ParticleBurstWhite,
    outline = ParticleBurstBorder,
    outlineVariant = Color(0xFF2B2D31)
)

val ParticleBurstTheme = ThemeOption(
    dynamic = false,
    key = "MicroGlyphSpark",
    name = R.string.theme_particle_burst,
    available = { true }
) {
    particleBurstColorScheme
}

private val ParticleNdot = FontFamily(Font(R.font.ndot, FontWeight.Normal))

@Composable
fun ParticleBurstPreviewKeyboard(
    modifier: Modifier = Modifier,
    spacebarColor: Color = ParticleBurstDark,
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
                            if (hasKeyBorders) Modifier.border(0.5.dp, ParticleBurstBorder, RoundedCornerShape(3.dp))
                            else Modifier
                        ),
                    color = if (hasKeyBorders) ParticleBurstKeyBg else Color.Transparent,
                    shape = RoundedCornerShape(3.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = char,
                            color = ParticleBurstWhite,
                            fontSize = 9.sp,
                            fontFamily = ParticleNdot
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
                            if (hasKeyBorders) Modifier.border(0.5.dp, ParticleBurstBorder, RoundedCornerShape(3.dp))
                            else Modifier
                        ),
                    color = if (hasKeyBorders) ParticleBurstKeyBg else Color.Transparent,
                    shape = RoundedCornerShape(3.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = char,
                            color = ParticleBurstWhite,
                            fontSize = 9.sp,
                            fontFamily = ParticleNdot
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
                            if (hasKeyBorders) Modifier.border(0.5.dp, ParticleBurstBorder, RoundedCornerShape(3.dp))
                            else Modifier
                        ),
                    color = if (hasKeyBorders) ParticleBurstKeyBg else Color.Transparent,
                    shape = RoundedCornerShape(3.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = char,
                            color = ParticleBurstWhite,
                            fontSize = 9.sp,
                            fontFamily = ParticleNdot
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
                Text("?123", color = ParticleBurstYellow, fontSize = 8.sp, fontFamily = ParticleNdot)
            }
            Box(modifier = Modifier.weight(0.9f).height(keyHeight), contentAlignment = Alignment.Center) {
                Text("☺", color = ParticleBurstBlue, fontSize = 8.sp)
            }
            Surface(
                modifier = Modifier.weight(3.5f).height(keyHeight),
                color = spacebarColor,
                border = if (spacebarBorderColor != null) androidx.compose.foundation.BorderStroke(1.dp, spacebarBorderColor) else null,
                shape = RoundedCornerShape(3.dp)
            ) {}
            Box(modifier = Modifier.weight(0.9f).height(keyHeight), contentAlignment = Alignment.Center) {
                Text(".", color = ParticleBurstWhite, fontSize = 8.sp, fontFamily = ParticleNdot)
            }
            Surface(
                modifier = Modifier.weight(1.2f).height(keyHeight),
                color = ParticleBurstRed,
                shape = RoundedCornerShape(3.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("↵", color = Color.White, fontSize = 10.sp)
                }
            }
        }
    }
}
