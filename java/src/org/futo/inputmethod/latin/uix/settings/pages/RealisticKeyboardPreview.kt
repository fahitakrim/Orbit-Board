package org.futo.inputmethod.latin.uix.settings.pages

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import org.futo.inputmethod.keyboard.internal.KeyboardTapAnimationManager
import org.futo.inputmethod.keyboard.internal.TapAnimationType
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import org.futo.inputmethod.latin.uix.adjustColorBrightnessForContrast
import org.futo.inputmethod.latin.uix.theme.Doto
import org.futo.inputmethod.latin.uix.theme.Inter
import org.futo.inputmethod.latin.uix.theme.Qurova
import org.futo.inputmethod.latin.uix.theme.Agency
import org.futo.inputmethod.latin.uix.theme.NothingRed
import org.futo.inputmethod.latin.uix.theme.ThemeOptions
import org.futo.inputmethod.latin.uix.theme.DarkColorScheme
import org.futo.inputmethod.latin.uix.theme.presets.NothingDark

@Composable
fun RealisticKeyboardPreview(
    currentTheme: String,
    spacebarColor: Color,
    spacebarBorder: Color?,
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
    typingEffect: String = "none",
    activeLayoutName: String = "English",
    heightMultiplier: Float = 1.0f,
    bottomOffset: Float = 0f,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tapAnimationManager = remember { KeyboardTapAnimationManager() }
    var animFrame by remember { mutableStateOf(0L) }
    var activeTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(activeTrigger) {
        if (activeTrigger > 0) {
            while (tapAnimationManager.hasActiveEffects()) {
                withFrameNanos { time ->
                    animFrame = time
                }
            }
        }
    }
    val isOrbitTheme = currentTheme == "OrbitTheme"
    val isChaosTheme = currentTheme == "OrbitChaos"
    val isRadarTheme = currentTheme == "NothingRadarWave" || currentTheme == "GlyphRadarPulse"
    val isBurstTheme = currentTheme == "MicroGlyphSpark" || currentTheme == "DotParticleBurst"
    val activeFont: FontFamily = when (keyboardFont) {
        "normal" -> Inter
        "qurova" -> Qurova
        "agency" -> Agency
        else -> Doto
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
        } catch (e: Exception) {
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

    val containerBg = if (isChaosTheme) {
        Color(0xFF000000)
    } else if (isOrbitTheme || isRadarTheme || isBurstTheme) {
        Color(0xFF0C0C0E)
    } else {
        colors.background
    }

    val defaultKeyBg = if (isChaosTheme) {
        Color.Transparent
    } else if (!hasKeyBorders) {
        Color.Transparent
    } else if (isRadarTheme) {
        Color(0xFF1C1D21)
    } else if (isBurstTheme) {
        Color(0xFF16171B)
    } else if (isOrbitTheme) {
        Color(0xFF1C1D20)
    } else if (colors.surface != colors.background) {
        colors.surface
    } else if (colors.surfaceVariant != colors.surface) {
        colors.surfaceVariant
    } else {
        Color(adjustColorBrightnessForContrast(colors.background.toArgb(), colors.background.toArgb(), 1.5f))
    }

    val defaultTextCol = if (isOrbitTheme || isRadarTheme || isBurstTheme) Color.White else colors.onSurface

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

    val subtleKeyBorder = if (isChaosTheme) {
        null
    } else if (hasKeyBorders) {
        if (isRadarTheme) {
            Color(0xFF28282E)
        } else if (isBurstTheme) {
            Color(0xFF27282F)
        } else if (isOrbitTheme) {
            Color(0xFF28282E)
        } else {
            colors.outline.copy(alpha = if (isThemeDark) 0.35f else 0.45f)
        }
    } else null

    fun getThemeKeyColors(keyChar: String, isAction: Boolean = false): Pair<Color, Color> {
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

        // Dynamic theme styling for Cotton Candy, Emerald, Nothing, etc.
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerBg)
            .border(1.dp, Color(0xFF232326), RoundedCornerShape(16.dp))
            .pointerInput(typingEffect) {
                detectTapGestures { offset ->
                    val animType = when (typingEffect) {
                        "radar_wave" -> TapAnimationType.RADAR_WAVE
                        "particle_burst" -> TapAnimationType.PARTICLE_BURST
                        "constellation_web" -> TapAnimationType.CONSTELLATION_WEB
                        else -> TapAnimationType.NONE
                    }
                    if (animType != TapAnimationType.NONE) {
                        tapAnimationManager.spawnAnimation(animType, offset.x, offset.y)
                        activeTrigger++
                    }
                }
            }
    ) {
        // Animation layer rendered UNDER the keys (behind borders & key tops)
        Canvas(
            modifier = Modifier.matchParentSize()
        ) {
            val _f = animFrame
            drawIntoCanvas { composeCanvas ->
                tapAnimationManager.draw(composeCanvas.nativeCanvas)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // 1. Suggestion / Action Bar
            if (showSuggestionRow) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(actionRowHeightDp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back / Collapse button
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(if (isOrbitTheme) Color.White else if (isThemeDark) Color(0xFF282830) else Color(0xFFE0E0E6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = if (isOrbitTheme) Color(0xFF1C1D20) else colors.onBackground,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Suggestions: Suggest | Suggest | Suggest
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val suggestTextCol = if (isOrbitTheme) Color(0xFFE5E5EA) else colors.onBackground
                        val dividerCol = if (isOrbitTheme) Color(0xFF383840) else colors.outline.copy(alpha = 0.33f)
                        Text(
                            text = "Suggest",
                            fontFamily = activeFont,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = suggestTextCol
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(14.dp)
                                .background(dividerCol)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Suggest",
                            fontFamily = activeFont,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = suggestTextCol
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(14.dp)
                                .background(dividerCol)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Suggest",
                            fontFamily = activeFont,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = suggestTextCol
                        )
                    }

                    // Action Icon (Globe)
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = if (isOrbitTheme) Color(0xFF8E8E93) else colors.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
            }

            // 2. Number Row (Optional)
            if (showNumberRow) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    val digits = if (isChaosTheme) listOf("1", "2", "3", "4", "5", "9", "7", "8", "6", "0") else listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
                    digits.forEach { digit ->
                        val (bg, txt) = getThemeKeyColors(digit)
                        KeyTile(
                            text = digit,
                            hint = null,
                            font = Inter,
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
            }

            // 3. Row 1: q w e r t y u i o p
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                val row1Keys = listOf(
                    "q" to "1", "w" to "2", "e" to "3", "r" to "4", "t" to "5",
                    "y" to "6", "u" to "7", "i" to "8", "o" to "9", "p" to "0"
                )
                row1Keys.forEach { (char, digit) ->
                    val (bg, txt) = getThemeKeyColors(char)
                    KeyTile(
                        text = char,
                        hint = if (showKeyHints) digit else null,
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

            // 4. Row 2: a s d f g h j k l (Indented)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Spacer(modifier = Modifier.weight(0.5f))
                val row2Keys = listOf(
                    "a" to "@", "s" to "#", "d" to "&", "f" to "*", "g" to "-",
                    "h" to "!", "j" to "?", "k" to "(", "l" to ")"
                )
                row2Keys.forEach { (char, symbol) ->
                    val (bg, txt) = getThemeKeyColors(char)
                    KeyTile(
                        text = char,
                        hint = if (showKeyHints) symbol else null,
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
                Spacer(modifier = Modifier.weight(0.5f))
            }

            // 5. Row 3: Shift, z x c v b n m, Backspace
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shift key
                val (shiftBg, shiftTxt) = getThemeKeyColors("shift", isAction = true)
                KeyTileIcon(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Shift",
                            tint = shiftTxt,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    bgColor = shiftBg,
                    borderColor = subtleKeyBorder,
                    height = keyHeightDp,
                    modifier = Modifier.weight(1.35f)
                )

                // z x c v b n m
                val row3Keys = listOf(
                    "z" to "\"", "x" to "'", "c" to ":", "v" to ";", "b" to "/",
                    "n" to ",", "m" to "?"
                )
                row3Keys.forEach { (char, symbol) ->
                    val (bg, txt) = getThemeKeyColors(char)
                    KeyTile(
                        text = char,
                        hint = if (showKeyHints) symbol else null,
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

                // Backspace key
                val (backspaceBg, backspaceTxt) = getThemeKeyColors("backspace", isAction = true)
                KeyTileIcon(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Backspace,
                            contentDescription = "Backspace",
                            tint = backspaceTxt,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    bgColor = backspaceBg,
                    borderColor = subtleKeyBorder,
                    height = keyHeightDp,
                    modifier = Modifier.weight(1.35f)
                )
            }

            // 6. Row 4: ?123, emoji, period, Spacebar, period, Enter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ?123 key
                val (symBg, symTxt) = getThemeKeyColors("?123", isAction = true)
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

                // Emoji / Comma key
                if (showEmojiKey) {
                    val (emojiBg, emojiTxt) = getThemeKeyColors("emoji", isAction = true)
                    KeyTileIcon(
                        icon = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SentimentSatisfied,
                                    contentDescription = "Emoji",
                                    tint = if (isOrbitTheme) emojiTxt else colors.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = ",",
                                    fontFamily = activeFont,
                                    fontSize = 11.sp,
                                    color = if (isOrbitTheme) emojiTxt else colors.onSurface,
                                    lineHeight = 11.sp
                                )
                            }
                        },
                        bgColor = emojiBg,
                        borderColor = subtleKeyBorder,
                        height = keyHeightDp,
                        modifier = Modifier.weight(1.0f)
                    )
                }

                // Left Period Key
                val (periodLeftBg, periodLeftTxt) = getThemeKeyColors("period_left")
                KeyTile(
                    text = ".",
                    hint = null,
                    font = activeFont,
                    bgColor = periodLeftBg,
                    textColor = periodLeftTxt,
                    hintColor = hintCol,
                    borderColor = subtleKeyBorder,
                    height = keyHeightDp,
                    modifier = Modifier.weight(0.85f)
                )

                // Spacebar Key
                val spacebarWeight = if (showEmojiKey) 3.8f else 4.8f
                val spaceBorderColor = spacebarBorder ?: subtleKeyBorder
                val spaceRadius = if (spacebarShape == "roundy") (keyHeightDp / 2f) else 7.dp

                Box(
                    modifier = Modifier
                        .weight(spacebarWeight)
                        .height(keyHeightDp)
                        .clip(RoundedCornerShape(spaceRadius))
                        .background(spacebarColor)
                        .then(
                            if (spaceBorderColor != null) {
                                Modifier.border(
                                    if (spacebarBorder != null) 1.5.dp else 1.dp,
                                    spaceBorderColor,
                                    RoundedCornerShape(spaceRadius)
                                )
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (pinLayoutNameOnSpacebar) {
                        val spaceTextColor = spacebarTextColor ?: if (isChaosTheme) {
                            Color(0xFFE2B438)
                        } else if (isOrbitTheme) {
                            Color(0xFFE2B438)
                        } else if (ColorUtils.calculateLuminance(spacebarColor.toArgb()) > 0.5) {
                            Color(0xFF1C1D20)
                        } else {
                            Color.White
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

                // Right Period Key
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
        val isDigit = text.isNotEmpty() && text.all { it in '0'..'9' || it in '\u09E6'..'\u09EF' }
        Text(
            text = text,
            fontFamily = if (isDigit) Inter else font,
            fontWeight = FontWeight.Medium,
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
