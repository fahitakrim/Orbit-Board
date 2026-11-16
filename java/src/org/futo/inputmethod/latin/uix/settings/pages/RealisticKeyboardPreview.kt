package org.futo.inputmethod.latin.uix.settings.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import org.futo.inputmethod.latin.uix.adjustColorBrightnessForContrast
import org.futo.inputmethod.latin.uix.theme.Agency
import org.futo.inputmethod.latin.uix.theme.DarkColorScheme
import org.futo.inputmethod.latin.uix.theme.Inter
import org.futo.inputmethod.latin.uix.theme.Ndot
import org.futo.inputmethod.latin.uix.theme.NothingRed
import org.futo.inputmethod.latin.uix.theme.Qurova
import org.futo.inputmethod.latin.uix.theme.ThemeOptions
import org.futo.inputmethod.latin.uix.theme.presets.BerryBlushThemeHelper
import org.futo.inputmethod.latin.uix.theme.presets.NothingDark
import org.futo.inputmethod.latin.uix.theme.presets.RetroBlocksThemeHelper
import org.futo.inputmethod.latin.uix.theme.presets.TactileSlateThemeHelper

@Composable
fun RealisticKeyboardPreview(
    currentTheme: String,
    spacebarColor: Color = Color.Unspecified,
    spacebarBorder: Color? = null,
    spacebarShape: String = "boxy",
    spacebarTextColor: Color? = null,
    hasKeyBorders: Boolean = true,
    showSuggestionRow: Boolean = true,
    showNumberRow: Boolean = false,
    pinLayoutNameOnSpacebar: Boolean = true,
    showEmojiKey: Boolean = true,
    showKeyHints: Boolean = true,
    keyHintColor: String = "subtle",
    customAccentColor: String = "default",
    keyboardFont: String = "nothing",
    keyboardBgColor: String = "default",
    keyboardKeyColor: String = "default",
    keyboardTextColor: String = "default",
    typingEffect: String = "none",
    laserColor: String = "neon",
    activeLayoutName: String = "English",
    heightMultiplier: Float = 1.0f,
    bottomOffset: Float = 0f,
    modifier: Modifier = Modifier,
    spacebarColorKey: String? = null,
    spacebarBorderKey: String? = null,
    spacebarTextColorKey: String? = null
) {
    val context = LocalContext.current

    val isOrbitTheme = currentTheme == "OrbitTheme"
    val isChaosTheme = currentTheme == "OrbitChaos"
    val isRadarTheme = currentTheme == "NothingRadarWave" || currentTheme == "GlyphRadarPulse"
    val isBurstTheme = currentTheme == "MicroGlyphSpark" || currentTheme == "DotParticleBurst"
    val isRetroBlocksTheme = currentTheme == "retro_blocks" || currentTheme == "RetroBlocks"
    val isBerryBlushTheme = currentTheme == "berry_blush" || currentTheme == "BerryBlush"
    val isTactileSlateTheme = currentTheme == "tactile_slate" || currentTheme == "TactileSlate"
    val isBamboosaTheme = currentTheme == "bamboosa" || currentTheme == "Bamboosa"

    val activeFont: FontFamily = when {
        keyboardFont == "normal" -> Inter
        keyboardFont == "qurova" -> Qurova
        keyboardFont == "agency" -> Agency
        else -> Ndot // Authentic Nothing Dot Font!
    }

    val customAccentOverride: Color? = when (customAccentColor) {
        "red" -> Color(0xFFD71921)
        "yellow" -> Color(0xFFE2B438)
        "blue" -> Color(0xFF3B72BA)
        "white" -> Color(0xFFEAE6E1)
        "green" -> Color(0xFF67FB59)
        "purple" -> Color(0xFFA855F7)
        "orange" -> Color(0xFFFF7A00)
        else -> null
    }

    val themeOption = ThemeOptions[currentTheme] ?: ThemeOptions["NothingDark"] ?: NothingDark
    val rawColors = remember(currentTheme, context) {
        try {
            themeOption.obtainColors(context)
        } catch (_: Throwable) {
            DarkColorScheme
        }
    }
    val colors = remember(rawColors, customAccentOverride) {
        if (customAccentOverride != null) {
            rawColors.copy(
                primary = customAccentOverride,
                inversePrimary = customAccentOverride
            )
        } else {
            rawColors
        }
    }

    fun parseColorHex(str: String, fallback: Color): Color {
        return try {
            if (str.startsWith("#")) Color(android.graphics.Color.parseColor(str))
            else fallback
        } catch (_: Throwable) {
            fallback
        }
    }

    val containerBg = when {
        keyboardBgColor != "default" -> parseColorHex(keyboardBgColor, colors.background)
        isChaosTheme -> Color(0xFF000000)
        isRetroBlocksTheme -> Color(RetroBlocksThemeHelper.COLOR_BLACK)
        isBerryBlushTheme -> Color(BerryBlushThemeHelper.COLOR_BACKGROUND)
        isTactileSlateTheme -> Color(TactileSlateThemeHelper.COLOR_CHASSIS)
        isOrbitTheme || isRadarTheme || isBurstTheme -> Color(0xFF141416)
        else -> colors.background
    }

    val defaultKeyBg = when {
        keyboardKeyColor != "default" -> parseColorHex(keyboardKeyColor, colors.surface)
        isTactileSlateTheme -> Color(TactileSlateThemeHelper.COLOR_WHITE_KEY)
        isRetroBlocksTheme -> Color(RetroBlocksThemeHelper.COLOR_CHARCOAL)
        isBerryBlushTheme -> Color(BerryBlushThemeHelper.BLUSH_PINK)
        isChaosTheme -> Color.Transparent
        isRadarTheme -> Color(0xFF1C1D21)
        isBurstTheme -> Color(0xFF16171B)
        isOrbitTheme -> Color(0xFF1C1D20)
        colors.surface != colors.background -> colors.surface
        colors.surfaceVariant != colors.surface -> colors.surfaceVariant
        else -> Color(adjustColorBrightnessForContrast(colors.background.toArgb(), colors.background.toArgb(), 1.5f))
    }

    val defaultTextCol = when {
        keyboardTextColor != "default" -> parseColorHex(keyboardTextColor, Color.White)
        isTactileSlateTheme -> Color(TactileSlateThemeHelper.COLOR_TEXT_DARK)
        isBerryBlushTheme -> Color(BerryBlushThemeHelper.DARK_INK)
        isOrbitTheme || isRadarTheme || isBurstTheme || isRetroBlocksTheme -> Color.White
        else -> colors.onSurface
    }

    val isThemeDark = ColorUtils.calculateLuminance(containerBg.toArgb()) < 0.5
    val hintCol = when (keyHintColor) {
        "yellow", "ochre" -> Color(0xFFE2B438)
        "red", "carmine" -> Color(0xFFC23636)
        "blue", "cobalt" -> Color(0xFF2A5288)
        "white" -> Color(0xFFFFFFFF)
        "orange" -> Color(0xFFFF7A00)
        "theme" -> colors.primary
        else -> if (isChaosTheme) Color(0xBBE2B438) else if (isThemeDark) Color(0xFF8E8E93) else Color(0xFF72727A)
    }

    val subtleKeyBorder = when {
        isChaosTheme -> null
        hasKeyBorders -> {
            when {
                isRadarTheme -> Color(0xFF28282E)
                isBurstTheme -> Color(0xFF27282F)
                isOrbitTheme -> Color(0xFF28282E)
                isTactileSlateTheme -> Color(0x33000000)
                isBerryBlushTheme -> Color(0x225E1D35)
                isRetroBlocksTheme -> Color(0x22FFFFFF)
                else -> colors.outline.copy(alpha = if (isThemeDark) 0.35f else 0.45f)
            }
        }
        else -> null
    }

    fun getThemeKeyColors(keyChar: String, isAction: Boolean = false): Pair<Color, Color> {
        if (isRetroBlocksTheme) {
            val key = keyChar.lowercase()
            return when {
                key == "backspace" -> Pair(Color(RetroBlocksThemeHelper.COLOR_RED), Color.White)
                key == "spacebar" -> Pair(Color(RetroBlocksThemeHelper.COLOR_BLACK), Color.White)
                key in listOf("o", "g", "x") -> Pair(Color(RetroBlocksThemeHelper.COLOR_YELLOW), Color(RetroBlocksThemeHelper.COLOR_TEXT_DARK))
                key in listOf("q", "t", "j", "m", "shift", "?123", "enter") -> Pair(Color(RetroBlocksThemeHelper.COLOR_OLIVE), Color(RetroBlocksThemeHelper.COLOR_TEXT_LIGHT))
                key in listOf("w", "y", "d", "k", "v", "b", "emoji", ".", "period_right") -> Pair(Color(RetroBlocksThemeHelper.COLOR_FOREST), Color(RetroBlocksThemeHelper.COLOR_TEXT_LIGHT))
                key in listOf("e", "i", "p", "f", "l", "z", "n", ",", "period_left") -> Pair(Color(RetroBlocksThemeHelper.COLOR_CHARCOAL), Color(RetroBlocksThemeHelper.COLOR_TEXT_LIGHT))
                key in listOf("r", "u", "a", "h", "c") -> Pair(Color(RetroBlocksThemeHelper.COLOR_CREAM), Color(RetroBlocksThemeHelper.COLOR_TEXT_DARK))
                else -> Pair(Color(RetroBlocksThemeHelper.COLOR_CHARCOAL), Color(RetroBlocksThemeHelper.COLOR_TEXT_LIGHT))
            }
        }

        if (isBerryBlushTheme) {
            val key = keyChar.lowercase()
            return when {
                key == "backspace" -> Pair(Color(BerryBlushThemeHelper.HOT_PINK), Color.White)
                key == "enter" -> Pair(Color(BerryBlushThemeHelper.BERRY_ROSE), Color.White)
                key in listOf("?123", "emoji") -> Pair(Color(BerryBlushThemeHelper.LAVENDER), Color(BerryBlushThemeHelper.DARK_INK))
                key in listOf("shift", "comma", "period_left") -> Pair(Color(BerryBlushThemeHelper.ROSE_MAUVE), Color.White)
                key in listOf("q", "t", "u", "o", "f", "h", "v") -> Pair(Color(BerryBlushThemeHelper.BLUSH_PINK), Color(BerryBlushThemeHelper.DARK_INK))
                key in listOf("w", "r", "i", "p", "d", "j", "l", "b") -> Pair(Color(BerryBlushThemeHelper.BUBBLEGUM_PINK), Color(BerryBlushThemeHelper.DARK_INK))
                key in listOf("e", "y", "s", "g", "k", "c", "m") -> Pair(Color(BerryBlushThemeHelper.LAVENDER), Color(BerryBlushThemeHelper.DARK_INK))
                key in listOf("a", "z", "x", "n") -> Pair(Color(BerryBlushThemeHelper.WARM_CREAM), Color(BerryBlushThemeHelper.DARK_INK))
                else -> Pair(Color(BerryBlushThemeHelper.BLUSH_PINK), Color(BerryBlushThemeHelper.DARK_INK))
            }
        }

        if (isTactileSlateTheme) {
            val key = keyChar.lowercase()
            return when {
                key == "backspace" -> Pair(Color(TactileSlateThemeHelper.COLOR_RED_BACKSPACE), Color.White)
                key in listOf("shift", "?123", "emoji", "enter") -> Pair(Color(TactileSlateThemeHelper.COLOR_MODIFIER), Color.White)
                else -> Pair(Color(TactileSlateThemeHelper.COLOR_WHITE_KEY), Color(TactileSlateThemeHelper.COLOR_TEXT_DARK))
            }
        }

        if (isBamboosaTheme) {
            val yellow = Color(0xFFFED700)
            val olive = Color(0xFF8D9938)
            val darkGreen = Color(0xFF2C3E2D)
            val textCol = Color.White
            val key = keyChar.lowercase()
            return when {
                key == "enter" -> Pair(yellow, Color(0xFF1C1D20))
                key in listOf("shift", "?123", "backspace") -> Pair(olive, textCol)
                else -> Pair(darkGreen, textCol)
            }
        }

        if (isChaosTheme) {
            val yellow = Color(0xFFE2B438)
            val blue = Color(0xFF3B72BA)
            val red = Color(0xFFD71921)
            val white = Color(0xFFEAE6E1)
            val txt = when (keyChar.lowercase()) {
                "q", "t", "o", "s", "h", "v", "3", "7", "shift", "comma", "period_left" -> yellow
                "w", "y", "p", "d", "j", "z", "b", "4", "8", "emoji", "period_right", "." -> blue
                "r", "i", "a", "g", "l", "c", "m", "2", "6", "0", "?123", "enter" -> red
                "e", "u", "f", "k", "x", "n", "1", "5", "9", "backspace" -> white
                else -> white
            }
            return Pair(Color.Transparent, txt)
        }

        if (isBurstTheme) {
            val accent = customAccentOverride ?: NothingRed
            val fnBg = if (hasKeyBorders) Color(0xFF23242A) else Color.Transparent
            return when (keyChar) {
                "enter" -> Pair(accent, Color.White)
                "shift", "?123" -> Pair(fnBg, Color(0xFFE2B438))
                "emoji" -> Pair(fnBg, Color(0xFF3B72BA))
                else -> Pair(defaultKeyBg, defaultTextCol)
            }
        }

        if (isRadarTheme) {
            val accent = customAccentOverride ?: NothingRed
            val fnBg = if (hasKeyBorders) Color(0xFF23242A) else Color.Transparent
            return when (keyChar) {
                "enter" -> Pair(accent, Color.White)
                "?123" -> Pair(fnBg, accent)
                else -> Pair(defaultKeyBg, defaultTextCol)
            }
        }

        if (isOrbitTheme) {
            return when (keyChar) {
                "q" -> Pair(Color(0xFFE2B438), Color(0xFF1C1D20))
                "w" -> Pair(Color(0xFF2A5288), Color.White)
                "e" -> Pair(Color(0xFF1C1D20), Color.White)
                "r" -> Pair(Color(0xFFEAE6E1), Color(0xFF1C1D20))
                "t" -> Pair(Color(0xFFC23636), Color.White)
                "y" -> Pair(Color(0xFF2A5288), Color.White)
                "u" -> Pair(Color(0xFFEAE6E1), Color(0xFF1C1D20))
                "i" -> Pair(Color(0xFFC23636), Color.White)
                "o" -> Pair(Color(0xFFE2B438), Color(0xFF1C1D20))
                "p" -> Pair(Color(0xFF1C1D20), Color.White)

                "a" -> Pair(Color(0xFFEAE6E1), Color(0xFF1C1D20))
                "s" -> Pair(Color(0xFFC23636), Color.White)
                "d" -> Pair(Color(0xFF2A5288), Color.White)
                "f" -> Pair(Color(0xFF1C1D20), Color.White)
                "g" -> Pair(Color(0xFFE2B438), Color(0xFF1C1D20))
                "h" -> Pair(Color(0xFFEAE6E1), Color(0xFF1C1D20))
                "j" -> Pair(Color(0xFFC23636), Color.White)
                "k" -> Pair(Color(0xFF2A5288), Color.White)
                "l" -> Pair(Color(0xFF1C1D20), Color.White)

                "shift" -> Pair(Color(0xFFE2B438), Color(0xFF1C1D20))
                "z" -> Pair(Color(0xFF1C1D20), Color.White)
                "x" -> Pair(Color(0xFFE2B438), Color(0xFF1C1D20))
                "c" -> Pair(Color(0xFFEAE6E1), Color(0xFF1C1D20))
                "v" -> Pair(Color(0xFFC23636), Color.White)
                "b" -> Pair(Color(0xFF2A5288), Color.White)
                "n" -> Pair(Color(0xFF1C1D20), Color.White)
                "m" -> Pair(Color(0xFFE2B438), Color(0xFF1C1D20))
                "backspace" -> Pair(Color(0xFFEAE6E1), Color(0xFF1C1D20))

                "?123" -> Pair(Color(0xFFC23636), Color.White)
                "emoji" -> Pair(Color(0xFF2A5288), Color.White)
                "period_left" -> Pair(Color(0xFFEAE6E1), Color(0xFF1C1D20))
                "period_right" -> Pair(Color(0xFF1C1D20), Color.White)
                "enter" -> Pair(Color(0xFFC23636), Color.White)
                else -> Pair(defaultKeyBg, defaultTextCol)
            }
        }

        if (keyChar == "enter") {
            return Pair(colors.primary, colors.onPrimary)
        }

        return Pair(defaultKeyBg, defaultTextCol)
    }

    fun getRotation(char: String): Float {
        if (!isChaosTheme) return 0f
        return when (char.lowercase()) {
            "a" -> 180f
            "b" -> -25f
            "c" -> 90f
            "d" -> 90f
            "e" -> 180f
            "f" -> 180f
            "g" -> -28f
            "h" -> 35f
            "i" -> 90f
            "j" -> 180f
            "k" -> -22f
            "l" -> -90f
            "m" -> 180f
            "n" -> -90f
            "o" -> -25f
            "p" -> 180f
            "q" -> -18f
            "r" -> 25f
            "s" -> 30f
            "t" -> -35f
            "u" -> 20f
            "v" -> 180f
            "w" -> -90f
            "x" -> -30f
            "y" -> 180f
            "z" -> 25f
            "1" -> -15f
            "2" -> 20f
            "3" -> 180f
            "4" -> 25f
            "5" -> -30f
            "6" -> -20f
            "7" -> -25f
            "8" -> 30f
            "9" -> 25f
            "0" -> 180f
            else -> 0f
        }
    }

    fun getHintAlignment(char: String): Alignment {
        if (!isChaosTheme) return Alignment.TopEnd
        val hash = Math.abs(char.hashCode()) % 6
        return when (hash) {
            0 -> Alignment.TopEnd
            1 -> Alignment.BottomStart
            2 -> Alignment.BottomEnd
            3 -> Alignment.TopStart
            4 -> Alignment.BottomCenter
            else -> Alignment.TopCenter
        }
    }

    val safeHeightMultiplier = if (heightMultiplier.isNaN() || heightMultiplier <= 0f) 1.0f else heightMultiplier.coerceIn(0.5f, 1.5f)
    val safeBottomOffset = if (bottomOffset.isNaN() || bottomOffset < 0f) 0f else bottomOffset.coerceIn(0f, 100f)

    val keyHeightDp = (44 * safeHeightMultiplier).dp
    val numKeyHeightDp = (36 * safeHeightMultiplier).dp
    val actionRowHeightDp = (36 * safeHeightMultiplier).dp

    val shape = RoundedCornerShape(14.dp)

    // Pure Non-Interactive Visual Preview Card (zero touch, zero focus stealing, zero side effects)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerBg)
            .border(1.dp, Color.White.copy(alpha = 0.12f), shape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // SUGGESTION / ACTION ROW
            if (showSuggestionRow) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(actionRowHeightDp)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val actTint = if (isBerryBlushTheme) Color(BerryBlushThemeHelper.DARK_INK) else colors.onSurface.copy(alpha = 0.6f)
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Expand",
                        tint = actTint,
                        modifier = Modifier.size(18.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Orbit",
                            fontFamily = activeFont,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = actTint
                        )
                        Text(
                            text = "Board",
                            fontFamily = activeFont,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                        Text(
                            text = "Preview",
                            fontFamily = activeFont,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = actTint
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        tint = actTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
            }

            // NUMBER ROW (OPTIONAL)
            if (showNumberRow) {
                val numRow = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    numRow.forEach { digit ->
                        val (bg, txt) = getThemeKeyColors(digit)
                        KeyTile(
                            text = digit,
                            hint = null,
                            font = activeFont,
                            bgColor = bg,
                            textColor = txt,
                            hintColor = hintCol,
                            borderColor = subtleKeyBorder,
                            height = numKeyHeightDp,
                            modifier = Modifier.weight(1f),
                            rotation = getRotation(digit),
                            hintAlignment = getHintAlignment(digit)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // ROW 1: Q W E R T Y U I O P
            val row1Keys = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
            val row1Hints = if (showKeyHints && !showNumberRow) listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0") else List(10) { null }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row1Keys.forEachIndexed { index, char ->
                    val (bg, txt) = getThemeKeyColors(char)
                    KeyTile(
                        text = char,
                        hint = row1Hints.getOrNull(index),
                        font = activeFont,
                        bgColor = bg,
                        textColor = txt,
                        hintColor = hintCol,
                        borderColor = subtleKeyBorder,
                        height = keyHeightDp,
                        modifier = Modifier.weight(1f),
                        rotation = getRotation(char),
                        hintAlignment = getHintAlignment(char)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ROW 2: A S D F G H J K L
            val row2Keys = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
            val row2Hints = if (showKeyHints) listOf("@", "#", "$", "%", "&", "-", "+", "(", ")") else List(9) { null }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row2Keys.forEachIndexed { index, char ->
                    val (bg, txt) = getThemeKeyColors(char)
                    KeyTile(
                        text = char,
                        hint = row2Hints.getOrNull(index),
                        font = activeFont,
                        bgColor = bg,
                        textColor = txt,
                        hintColor = hintCol,
                        borderColor = subtleKeyBorder,
                        height = keyHeightDp,
                        modifier = Modifier.weight(1f),
                        rotation = getRotation(char),
                        hintAlignment = getHintAlignment(char)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ROW 3: SHIFT, Z X C V B N M, BACKSPACE
            val row3Keys = listOf("z", "x", "c", "v", "b", "n", "m")
            val row3Hints = if (showKeyHints) listOf("*", "\"", "'", ":", ";", "!", "?") else List(7) { null }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shift Key
                val (shiftBg, shiftTxt) = getThemeKeyColors("shift")
                KeyTileIcon(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Shift",
                            tint = shiftTxt,
                            modifier = Modifier.size(19.dp)
                        )
                    },
                    bgColor = shiftBg,
                    borderColor = subtleKeyBorder,
                    height = keyHeightDp,
                    modifier = Modifier.weight(1.35f)
                )

                row3Keys.forEachIndexed { index, char ->
                    val (bg, txt) = getThemeKeyColors(char)
                    KeyTile(
                        text = char,
                        hint = row3Hints.getOrNull(index),
                        font = activeFont,
                        bgColor = bg,
                        textColor = txt,
                        hintColor = hintCol,
                        borderColor = subtleKeyBorder,
                        height = keyHeightDp,
                        modifier = Modifier.weight(1f),
                        rotation = getRotation(char),
                        hintAlignment = getHintAlignment(char)
                    )
                }

                // Backspace Key
                val (backBg, backTxt) = getThemeKeyColors("backspace")
                KeyTileIcon(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Backspace,
                            contentDescription = "Backspace",
                            tint = backTxt,
                            modifier = Modifier.size(19.dp)
                        )
                    },
                    bgColor = backBg,
                    borderColor = subtleKeyBorder,
                    height = keyHeightDp,
                    modifier = Modifier.weight(1.35f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ROW 4: ?123, EMOJI/COMMA, SPACEBAR, PERIOD, ENTER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ?123 Key
                val (symBg, symTxt) = getThemeKeyColors("?123")
                KeyTile(
                    text = "?123",
                    hint = null,
                    font = activeFont,
                    bgColor = symBg,
                    textColor = symTxt,
                    hintColor = hintCol,
                    borderColor = subtleKeyBorder,
                    height = keyHeightDp,
                    modifier = Modifier.weight(1.35f)
                )

                // Emoji or Comma
                val (fnBg, fnTxt) = getThemeKeyColors(if (showEmojiKey) "emoji" else "comma")
                KeyTileIcon(
                    icon = {
                        if (showEmojiKey) {
                            Icon(
                                imageVector = Icons.Default.SentimentSatisfied,
                                contentDescription = "Emoji",
                                tint = fnTxt,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                text = ",",
                                fontFamily = activeFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = fnTxt
                            )
                        }
                    },
                    bgColor = fnBg,
                    borderColor = subtleKeyBorder,
                    height = keyHeightDp,
                    modifier = Modifier.weight(0.9f)
                )

                // Spacebar
                val defaultSpaceBg = when {
                    isBerryBlushTheme -> Color(BerryBlushThemeHelper.BLUSH_PINK)
                    isRetroBlocksTheme -> Color(RetroBlocksThemeHelper.COLOR_BLACK)
                    isTactileSlateTheme -> Color(TactileSlateThemeHelper.COLOR_WHITE_KEY)
                    isChaosTheme -> Color(0xFF141416)
                    isOrbitTheme -> Color(0xFFE2B438)
                    else -> colors.surface
                }
                val finalSpaceBg = if (spacebarColor != Color.Unspecified) spacebarColor else defaultSpaceBg
                val subtleSpaceBorder = when {
                    isChaosTheme -> Color.White.copy(alpha = 0.2f)
                    spacebarBorder != null -> spacebarBorder
                    isRetroBlocksTheme -> Color(RetroBlocksThemeHelper.COLOR_YELLOW)
                    isTactileSlateTheme -> Color(0x33000000)
                    isBerryBlushTheme -> Color(BerryBlushThemeHelper.BOW_FILL)
                    else -> subtleKeyBorder
                }
                val spaceRadius = if (spacebarShape == "roundy") 24.dp else 7.dp

                Box(
                    modifier = Modifier
                        .weight(4.4f)
                        .height(keyHeightDp)
                        .clip(RoundedCornerShape(spaceRadius))
                        .background(finalSpaceBg)
                        .then(
                            if (subtleSpaceBorder != null) {
                                Modifier.border(1.dp, subtleSpaceBorder, RoundedCornerShape(spaceRadius))
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (pinLayoutNameOnSpacebar) {
                        val spaceTextColor = when {
                            spacebarTextColor != null && spacebarTextColor != Color.Unspecified -> spacebarTextColor
                            isBerryBlushTheme -> Color(BerryBlushThemeHelper.DARK_INK)
                            isRetroBlocksTheme -> Color(RetroBlocksThemeHelper.COLOR_YELLOW)
                            isTactileSlateTheme -> Color(TactileSlateThemeHelper.COLOR_TEXT_DARK)
                            isChaosTheme || isOrbitTheme -> Color(0xFFE2B438)
                            ColorUtils.calculateLuminance(finalSpaceBg.toArgb()) > 0.5 -> Color(0xFF1C1D20)
                            else -> Color.White
                        }
                        Text(
                            text = activeLayoutName,
                            fontFamily = activeFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = spaceTextColor
                        )
                    }
                }

                // Period Key
                val (periodRightBg, periodRightTxt) = getThemeKeyColors("period_right")
                KeyTile(
                    text = ".",
                    hint = null,
                    font = activeFont,
                    bgColor = periodRightBg,
                    textColor = periodRightTxt,
                    hintColor = hintCol,
                    borderColor = subtleKeyBorder,
                    height = keyHeightDp,
                    modifier = Modifier.weight(0.85f)
                )

                // Enter Key
                val (enterBg, enterTxt) = getThemeKeyColors("enter", isAction = true)
                KeyTileIcon(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Enter",
                            tint = enterTxt,
                            modifier = Modifier.size(17.dp)
                        )
                    },
                    bgColor = enterBg,
                    borderColor = subtleKeyBorder,
                    height = keyHeightDp,
                    modifier = Modifier.weight(1.35f)
                )
            }

            if (safeBottomOffset > 0f) {
                Spacer(modifier = Modifier.height(safeBottomOffset.dp))
            }
        }
    }
}

@Composable
private fun KeyTile(
    text: String,
    hint: String?,
    font: FontFamily,
    bgColor: Color,
    textColor: Color,
    hintColor: Color,
    borderColor: Color?,
    height: Dp,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    hintAlignment: Alignment = Alignment.TopEnd
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(7.dp))
            .background(bgColor)
            .then(
                if (borderColor != null) {
                    Modifier.border(1.dp, borderColor, RoundedCornerShape(7.dp))
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (hint != null) {
            val padModifier = when (hintAlignment) {
                Alignment.TopStart -> Modifier.align(Alignment.TopStart).padding(top = 4.5.dp, start = 6.dp)
                Alignment.BottomStart -> Modifier.align(Alignment.BottomStart).padding(bottom = 3.dp, start = 6.dp)
                Alignment.BottomEnd -> Modifier.align(Alignment.BottomEnd).padding(bottom = 3.dp, end = 6.dp)
                Alignment.BottomCenter -> Modifier.align(Alignment.BottomCenter).padding(bottom = 3.dp)
                Alignment.TopCenter -> Modifier.align(Alignment.TopCenter).padding(top = 4.5.dp)
                else -> Modifier.align(Alignment.TopEnd).padding(top = 4.5.dp, end = 6.dp)
            }
            Text(
                text = hint,
                fontFamily = Inter,
                fontSize = 8.5.sp,
                color = hintColor,
                modifier = padModifier
            )
        }
        Text(
            text = text,
            fontFamily = font,
            fontWeight = FontWeight.Normal,
            fontSize = if (text.length > 2) 12.sp else 16.sp,
            color = textColor,
            modifier = if (rotation != 0f) Modifier.graphicsLayer(rotationZ = rotation) else Modifier
        )
    }
}

@Composable
private fun KeyTileIcon(
    icon: @Composable () -> Unit,
    bgColor: Color,
    borderColor: Color?,
    height: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(7.dp))
            .background(bgColor)
            .then(
                if (borderColor != null) {
                    Modifier.border(1.dp, borderColor, RoundedCornerShape(7.dp))
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}
