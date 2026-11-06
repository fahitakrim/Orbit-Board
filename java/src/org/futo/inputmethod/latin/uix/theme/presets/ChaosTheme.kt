package org.futo.inputmethod.latin.uix.theme.presets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.theme.ThemeOption

// Bauhaus Nothing Theme Palette for Orbit Chaos
val ChaosYellow = Color(0xFFE2B438)
val ChaosBlue = Color(0xFF3B72BA)
val ChaosRed = Color(0xFFD71921)
val ChaosWhite = Color(0xFFEAE6E1)
val ChaosDark = Color(0xFF1C1D20)
val ChaosBlack = Color(0xFF000000)

private val chaosColorScheme = darkColorScheme(
    primary = ChaosRed,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = ChaosDark,
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = ChaosBlue,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = ChaosDark,
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = ChaosYellow,
    onTertiary = Color(0xFF121212),
    background = ChaosBlack,
    onBackground = Color(0xFFFFFFFF),
    surface = ChaosBlack,
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = ChaosDark,
    onSurfaceVariant = Color(0xFFFFFFFF),
    outline = ChaosRed,
    outlineVariant = Color(0xFF2B2D31)
)

val ChaosTheme = ThemeOption(
    dynamic = false,
    key = "OrbitChaos",
    name = R.string.theme_chaos,
    available = { true }
) {
    chaosColorScheme
}

private val ChaosNdot = FontFamily(Font(R.font.ndot, FontWeight.Normal))

@Composable
fun ChaosPreviewKeyboard(
    modifier: Modifier = Modifier,
    spacebarColor: Color = Color(0xFF1C1D20),
    spacebarBorderColor: Color? = null,
    hasKeyBorders: Boolean = false,
    keyHeight: androidx.compose.ui.unit.Dp = 16.dp
) {
    // 3 rows of chaotic letters
    val row1 = listOf(
        Pair("Q", ChaosYellow) to -18f,
        Pair("W", ChaosBlue) to -90f,
        Pair("E", ChaosWhite) to 180f,
        Pair("R", ChaosRed) to 25f,
        Pair("T", ChaosYellow) to -35f,
        Pair("Y", ChaosBlue) to 180f,
        Pair("U", ChaosWhite) to 20f,
        Pair("I", ChaosRed) to 90f,
        Pair("O", ChaosYellow) to -25f,
        Pair("P", ChaosBlue) to 180f
    )
    val row2 = listOf(
        Pair("A", ChaosRed) to 180f,
        Pair("S", ChaosYellow) to 30f,
        Pair("D", ChaosBlue) to 90f,
        Pair("F", ChaosWhite) to 180f,
        Pair("G", ChaosRed) to -28f,
        Pair("H", ChaosYellow) to 35f,
        Pair("J", ChaosBlue) to 180f,
        Pair("K", ChaosWhite) to -22f,
        Pair("L", ChaosRed) to -90f
    )
    val row3 = listOf(
        Pair("Z", ChaosBlue) to 25f,
        Pair("X", ChaosWhite) to -30f,
        Pair("C", ChaosRed) to 90f,
        Pair("V", ChaosYellow) to 180f,
        Pair("B", ChaosBlue) to -25f,
        Pair("N", ChaosWhite) to -90f,
        Pair("M", ChaosRed) to 180f
    )

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
            row1.forEach { (item, angle) ->
                Box(
                    modifier = Modifier.weight(1f).height(keyHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.first,
                        color = item.second,
                        fontSize = 10.sp,
                        fontFamily = ChaosNdot,
                        modifier = Modifier.graphicsLayer(rotationZ = angle)
                    )
                }
            }
        }
        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(0.92f),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
        ) {
            row2.forEach { (item, angle) ->
                Box(
                    modifier = Modifier.weight(1f).height(keyHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.first,
                        color = item.second,
                        fontSize = 10.sp,
                        fontFamily = ChaosNdot,
                        modifier = Modifier.graphicsLayer(rotationZ = angle)
                    )
                }
            }
        }
        // Row 3
        Row(
            modifier = Modifier.fillMaxWidth(0.80f),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
        ) {
            row3.forEach { (item, angle) ->
                Box(
                    modifier = Modifier.weight(1f).height(keyHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.first,
                        color = item.second,
                        fontSize = 10.sp,
                        fontFamily = ChaosNdot,
                        modifier = Modifier.graphicsLayer(rotationZ = angle)
                    )
                }
            }
        }
        // Row 4 (Spacebar row)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
        ) {
            Box(modifier = Modifier.weight(1.2f).height(keyHeight), contentAlignment = Alignment.Center) {
                Text("?123", color = ChaosRed, fontSize = 8.sp, fontFamily = ChaosNdot)
            }
            Box(modifier = Modifier.weight(0.9f).height(keyHeight), contentAlignment = Alignment.Center) {
                Text("☺", color = ChaosBlue, fontSize = 8.sp)
            }
            Surface(
                modifier = Modifier.weight(3.5f).height(keyHeight),
                color = spacebarColor,
                border = if (spacebarBorderColor != null) androidx.compose.foundation.BorderStroke(1.dp, spacebarBorderColor) else null,
                shape = RoundedCornerShape(3.dp)
            ) {}
            Box(modifier = Modifier.weight(0.9f).height(keyHeight), contentAlignment = Alignment.Center) {
                Text(".", color = ChaosBlue, fontSize = 8.sp, fontFamily = ChaosNdot)
            }
            Box(modifier = Modifier.weight(1.2f).height(keyHeight), contentAlignment = Alignment.Center) {
                Text("↵", color = ChaosRed, fontSize = 10.sp)
            }
        }
    }
}
