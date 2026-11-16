package org.futo.inputmethod.latin.uix.theme.presets

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import org.futo.inputmethod.keyboard.Key
import org.futo.inputmethod.latin.common.Constants

/**
 * Retro Blocks Theme Helper (CMF Patchwork):
 * Pure pitch black AMOLED background (#000000) with distinct color blocks:
 * - Cyber Yellow (#FED700): 'o', 'g', 'x'
 * - Light Olive / Mustard (#96A03E): 'q', 't', 'j', 'm', Shift, ?123, Enter
 * - Muted Forest Green (#4A614A): 'w', 'y', 'd', 'k', 'v', 'b', Emoji, '.'
 * - Dark Charcoal / Moss (#1F2420): 'e', 'i', 'p', 'f', 'l', 'z', 'n', ','
 * - Warm Cream / Off-White (#F0EFE6): 'r', 'u', 'a', 'h', 'c'
 * - Crimson Red Backspace (#BA1A1A) with dot-matrix delete icon
 * - Pitch Black Spacebar with Cyber Yellow dashed / dotted border (#FED700)
 */
object RetroBlocksThemeHelper {
    const val COLOR_BLACK = 0xFF000000.toInt()
    const val COLOR_YELLOW = 0xFFFED700.toInt()      // Cyber Yellow
    const val COLOR_OLIVE = 0xFF9EA637.toInt()       // Light Olive
    const val COLOR_FOREST = 0xFF4A644B.toInt()      // Muted Forest
    const val COLOR_CHARCOAL = 0xFF232621.toInt()    // Dark Charcoal
    const val COLOR_CREAM = 0xFFF0EFE6.toInt()       // Warm Cream
    const val COLOR_RED = 0xFFBA1A1A.toInt()         // Crimson Red (Backspace)
    const val COLOR_TEXT_DARK = 0xFF121212.toInt()   // Deep Black text on light keys
    const val COLOR_TEXT_LIGHT = 0xFFFFFFFF.toInt()  // Pure White text on dark keys

    // Spacebar customization properties
    var customSpacebarColor: Int? = null
    var customSpacebarBorderColor: Int? = null
    var customSpacebarBorderWidth: Int = 0
    var customSpacebarTextColor: Int? = null
    var customSpacebarCornerRadius: Float? = null
    var spacebarShape: String = "boxy"

    // Pressed shades
    const val COLOR_YELLOW_PRESSED = 0xFFDBBA00.toInt()
    const val COLOR_OLIVE_PRESSED = 0xFF7D8632.toInt()
    const val COLOR_FOREST_PRESSED = 0xFF3C4F3C.toInt()
    const val COLOR_CHARCOAL_PRESSED = 0xFF2F3630.toInt()
    const val COLOR_CREAM_PRESSED = 0xFFD8D7CE.toInt()
    const val COLOR_RED_PRESSED = 0xFF991515.toInt()

    private val colorByLabel = buildMap<String, Int> {
        val base = mapOf(
            // English Row 1 (q..p)
            "q" to COLOR_OLIVE,
            "w" to COLOR_FOREST,
            "e" to COLOR_CHARCOAL,
            "r" to COLOR_CREAM,
            "t" to COLOR_OLIVE,
            "y" to COLOR_FOREST,
            "u" to COLOR_CREAM,
            "i" to COLOR_CHARCOAL,
            "o" to COLOR_YELLOW,
            "p" to COLOR_CHARCOAL,

            // Number Row (1..0)
            "1" to COLOR_OLIVE, "2" to COLOR_FOREST, "3" to COLOR_CHARCOAL, "4" to COLOR_CREAM, "5" to COLOR_OLIVE,
            "6" to COLOR_FOREST, "7" to COLOR_CREAM, "8" to COLOR_CHARCOAL, "9" to COLOR_YELLOW, "0" to COLOR_CHARCOAL,

            // Bengali Number Row (১..০)
            "১" to COLOR_OLIVE, "২" to COLOR_FOREST, "৩" to COLOR_CHARCOAL, "৪" to COLOR_CREAM, "৫" to COLOR_OLIVE,
            "৬" to COLOR_FOREST, "৭" to COLOR_CREAM, "৮" to COLOR_CHARCOAL, "৯" to COLOR_YELLOW, "০" to COLOR_CHARCOAL,

            // English Row 2 (a..l)
            "a" to COLOR_CREAM,
            "s" to COLOR_OLIVE,
            "d" to COLOR_FOREST,
            "f" to COLOR_CHARCOAL,
            "g" to COLOR_YELLOW,
            "h" to COLOR_CREAM,
            "j" to COLOR_OLIVE,
            "k" to COLOR_FOREST,
            "l" to COLOR_CHARCOAL,

            // English Row 3 (z..m)
            "z" to COLOR_CHARCOAL,
            "x" to COLOR_YELLOW,
            "c" to COLOR_CREAM,
            "v" to COLOR_OLIVE,
            "b" to COLOR_FOREST,
            "n" to COLOR_CHARCOAL,
            "m" to COLOR_OLIVE,
            "," to COLOR_FOREST,
            "." to COLOR_CHARCOAL,

            // Bengali Layouts (Avro, Jatiyo, Probhat, Akkhor)
            "অ" to COLOR_CREAM, "আ" to COLOR_CREAM, "ই" to COLOR_FOREST, "ঈ" to COLOR_FOREST,
            "উ" to COLOR_OLIVE, "ূ" to COLOR_OLIVE, "ঊ" to COLOR_OLIVE,
            "ঋ" to COLOR_CREAM, "ৃ" to COLOR_CREAM, "এ" to COLOR_CREAM, "ঐ" to COLOR_CREAM,
            "ও" to COLOR_YELLOW, "ঔ" to COLOR_YELLOW, "ক্ষ" to COLOR_YELLOW,
            "\u25CC\u09CD\u09AF" to COLOR_CREAM, "◌্য" to COLOR_CREAM,
            "ঙ" to COLOR_YELLOW, "ং" to COLOR_YELLOW, "য" to COLOR_FOREST, "য়" to COLOR_FOREST,
            "ড" to COLOR_CHARCOAL, "ঢ" to COLOR_CHARCOAL, "প" to COLOR_CREAM, "ফ" to COLOR_CREAM,
            "ট" to COLOR_OLIVE, "ঠ" to COLOR_OLIVE, "চ" to COLOR_FOREST, "ছ" to COLOR_FOREST,
            "জ" to COLOR_CREAM, "ঝ" to COLOR_CREAM, "হ" to COLOR_OLIVE, "ঞ" to COLOR_OLIVE,
            "গ" to COLOR_YELLOW, "ঘ" to COLOR_YELLOW, "ড়" to COLOR_CHARCOAL, "ঢ়" to COLOR_CHARCOAL,

            "ৃ" to COLOR_CREAM, "ঋ" to COLOR_CREAM, "ঐ" to COLOR_CREAM,
            "ু" to COLOR_OLIVE, "উ" to COLOR_OLIVE, "ি" to COLOR_FOREST, "ই" to COLOR_FOREST,
            "ব" to COLOR_CHARCOAL, "ভ" to COLOR_CHARCOAL, "্" to COLOR_YELLOW, "়" to COLOR_YELLOW,
            "া" to COLOR_CREAM, "আ" to COLOR_CREAM, "ক" to COLOR_OLIVE, "খ" to COLOR_OLIVE,
            "ত" to COLOR_FOREST, "থ" to COLOR_FOREST, "ৎ" to COLOR_FOREST, "দ" to COLOR_CHARCOAL, "ধ" to COLOR_CHARCOAL,

            "ঁ" to COLOR_CHARCOAL, "ঃ" to COLOR_CHARCOAL, "ো" to COLOR_YELLOW, "ও" to COLOR_YELLOW,
            "ে" to COLOR_CREAM, "এ" to COLOR_CREAM, "র" to COLOR_OLIVE, "ল" to COLOR_OLIVE,
            "ন" to COLOR_FOREST, "ণ" to COLOR_FOREST, "স" to COLOR_CHARCOAL, "ষ" to COLOR_CHARCOAL,
            "ম" to COLOR_OLIVE, "শ" to COLOR_OLIVE, "্য" to COLOR_CREAM, "্র" to COLOR_FOREST,
            "ৌ" to COLOR_YELLOW, "ৈ" to COLOR_CREAM, "ূ" to COLOR_OLIVE, "ী" to COLOR_FOREST,

            // Standard Symbols
            "!" to COLOR_CREAM, "@" to COLOR_OLIVE, "#" to COLOR_FOREST, "$" to COLOR_CHARCOAL, "%" to COLOR_YELLOW,
            "^" to COLOR_CREAM, "&" to COLOR_OLIVE, "*" to COLOR_FOREST, "(" to COLOR_CHARCOAL, ")" to COLOR_YELLOW,
            "~" to COLOR_YELLOW, "`" to COLOR_FOREST, "|" to COLOR_CHARCOAL, "+" to COLOR_YELLOW, "=" to COLOR_FOREST,
            "/" to COLOR_CHARCOAL, "_" to COLOR_CREAM, "<" to COLOR_OLIVE, ">" to COLOR_FOREST,
            "[" to COLOR_CREAM, "]" to COLOR_OLIVE, "{" to COLOR_YELLOW, "}" to COLOR_CHARCOAL,
            "-" to COLOR_CHARCOAL, "\\" to COLOR_YELLOW, ":" to COLOR_CREAM, ";" to COLOR_OLIVE,
            "?" to COLOR_FOREST, "\"" to COLOR_CHARCOAL, "'" to COLOR_YELLOW,
            "।" to COLOR_CHARCOAL, "\u0964" to COLOR_CHARCOAL
        )
        putAll(base)
        base.forEach { (k, v) -> put(k.uppercase(), v) }
    }

    private val row1Colors = intArrayOf(
        COLOR_OLIVE, COLOR_FOREST, COLOR_CHARCOAL, COLOR_CREAM, COLOR_OLIVE,
        COLOR_FOREST, COLOR_CREAM, COLOR_CHARCOAL, COLOR_YELLOW, COLOR_CHARCOAL
    )
    private val row2Colors = intArrayOf(
        COLOR_CREAM, COLOR_OLIVE, COLOR_FOREST, COLOR_CHARCOAL, COLOR_YELLOW,
        COLOR_CREAM, COLOR_OLIVE, COLOR_FOREST, COLOR_CHARCOAL
    )
    private val row3Colors = intArrayOf(
        COLOR_CHARCOAL, COLOR_YELLOW, COLOR_CREAM, COLOR_OLIVE, COLOR_FOREST,
        COLOR_CHARCOAL, COLOR_OLIVE, COLOR_CHARCOAL
    )

    fun getKeyBaseColor(key: Key): Int {
        val code = key.code
        val rawLabel = key.label

        if (code == Constants.CODE_SHIFT) return COLOR_OLIVE
        if (code == Constants.CODE_DELETE) return COLOR_RED
        if (code == Constants.CODE_ENTER) return COLOR_OLIVE
        if (code == Constants.CODE_SWITCH_ALPHA_SYMBOL || code == Constants.CODE_SWITCH_BENGALI_NUM) return COLOR_OLIVE
        if (rawLabel == "১২৩" || rawLabel == "123" || rawLabel == "?123") return COLOR_OLIVE
        if (code == Constants.CODE_EMOJI || code == Constants.CODE_SHORTCUT) return COLOR_FOREST
        if (code == Constants.CODE_SPACE) return customSpacebarColor ?: COLOR_BLACK
        if (rawLabel == "." || rawLabel == ".\n⚙" || rawLabel == ".\n\u2699") return COLOR_CHARCOAL
        if (rawLabel == ",") return COLOR_FOREST

        // Direct label lookup
        if (rawLabel != null) {
            val c = colorByLabel[rawLabel] ?: colorByLabel[rawLabel.lowercase()]
            if (c != null) return c
        }

        // Code-based lookup for letters, digits, symbols
        if (code > 32) {
            val charStr = code.toChar().toString()
            val c = colorByLabel[charStr] ?: colorByLabel[charStr.lowercase()]
            if (c != null) return c
        }

        // Coordinate-based fallback
        val y = key.y
        val x = key.x
        val h = key.height
        val w = key.width
        if (h > 0) {
            val row = y / h
            return when (row) {
                0 -> {
                    val idx = if (w > 0) (x / w).coerceIn(0, row1Colors.size - 1) else 0
                    row1Colors[idx]
                }
                1 -> {
                    val idx = if (w > 0) (x / w).coerceIn(0, row2Colors.size - 1) else 0
                    row2Colors[idx]
                }
                2 -> {
                    if (x < h) COLOR_OLIVE
                    else {
                        val idx = if (w > 0) ((x - h) / w).coerceIn(0, row3Colors.size - 1) else 0
                        row3Colors[idx]
                    }
                }
                else -> COLOR_CHARCOAL
            }
        }
        return COLOR_CHARCOAL
    }

    fun getKeyBackgroundColor(key: Key, isPressed: Boolean): Int {
        val base = getKeyBaseColor(key)
        if (!isPressed) return base
        return when (base) {
            COLOR_YELLOW -> COLOR_YELLOW_PRESSED
            COLOR_OLIVE -> COLOR_OLIVE_PRESSED
            COLOR_FOREST -> COLOR_FOREST_PRESSED
            COLOR_CREAM -> COLOR_CREAM_PRESSED
            COLOR_RED -> COLOR_RED_PRESSED
            else -> COLOR_CHARCOAL_PRESSED
        }
    }

    fun isLightKey(color: Int): Boolean {
        return color == COLOR_CREAM || color == COLOR_YELLOW || color == COLOR_OLIVE || androidx.core.graphics.ColorUtils.calculateLuminance(color) > 0.45f
    }

    fun hasDarkText(key: Key): Boolean {
        if (key.code == Constants.CODE_SPACE) {
            val spaceCol = customSpacebarColor ?: COLOR_BLACK
            return customSpacebarTextColor == null && isLightKey(spaceCol)
        }
        val base = getKeyBaseColor(key)
        return isLightKey(base)
    }

    fun getKeyTextColor(key: Key): Int {
        if (key.code == Constants.CODE_SPACE) {
            return customSpacebarTextColor ?: COLOR_YELLOW
        }
        return if (hasDarkText(key)) COLOR_TEXT_DARK else COLOR_TEXT_LIGHT
    }

    fun getKeyHintColor(key: Key): Int {
        if (key.code == Constants.CODE_SPACE) {
            return customSpacebarTextColor ?: COLOR_YELLOW
        }
        return if (hasDarkText(key)) 0x88000000.toInt() else 0xCCFFFFFF.toInt()
    }

    private val drawableCache = HashMap<Int, GradientDrawable>()

    @Synchronized
    fun getDrawableForColor(color: Int, cornerRadius: Float): Drawable {
        val existing = drawableCache[color]
        if (existing != null) return existing

        val d = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            this.cornerRadius = cornerRadius
            setColor(color)
        }
        drawableCache[color] = d
        return d
    }

    fun getKeyBackground(key: Key, isPressed: Boolean, cornerRadius: Float): Drawable {
        val color = getKeyBackgroundColor(key, isPressed)
        val h = key.height.toFloat()
        val pillRadius = if (h > 0f) h / 2f else cornerRadius * 2.5f

        if (key.code == Constants.CODE_SPACE) {
            val defaultSpaceRadius = if (spacebarShape == "roundy") 48f * (cornerRadius / 8f).coerceAtLeast(1f) else cornerRadius
            val spaceRadius = customSpacebarCornerRadius ?: defaultSpaceRadius
            val baseColor = customSpacebarColor ?: COLOR_BLACK
            val pressedColor = if (isPressed) {
                if (customSpacebarColor != null) 0x66FFFFFF.toInt() else 0x33FED700.toInt()
            } else baseColor
            return GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                this.cornerRadius = spaceRadius
                setColor(pressedColor)
                if (customSpacebarBorderColor != null && customSpacebarBorderWidth > 0) {
                    setStroke(customSpacebarBorderWidth, customSpacebarBorderColor!!)
                } else if (customSpacebarColor == null) {
                    // Dotted / dashed yellow border matching signature reference image
                    setStroke(3, COLOR_YELLOW, 6f, 5f)
                }
            }
        }

        if (key.code == Constants.CODE_EMOJI) {
            // Full oval capsule pill shape for Emoji key matching reference image
            return GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                this.cornerRadius = pillRadius
                setColor(color)
            }
        }

        if (key.code == Constants.CODE_ENTER) {
            // Capsule rounded right edge for Enter key matching reference image
            return GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadii = floatArrayOf(
                    cornerRadius, cornerRadius,
                    pillRadius, pillRadius,
                    pillRadius, pillRadius,
                    cornerRadius, cornerRadius
                )
                setColor(color)
            }
        }

        return getDrawableForColor(color, cornerRadius)
    }

    private val previewDrawableCache = HashMap<Int, ShapeDrawable>()

    @Synchronized
    fun getKeyPreviewBackground(key: Key, cornerRadius: Float, width: Int, height: Int, bottomPadding: Int): Drawable {
        val color = getKeyBaseColor(key)
        val existing = previewDrawableCache[color]
        if (existing != null) return existing

        val d = ShapeDrawable().apply {
            try {
                paint?.color = color
            } catch (_: Throwable) {}
            try {
                shape = RoundRectShape(
                    floatArrayOf(
                        cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                        cornerRadius, cornerRadius, cornerRadius, cornerRadius
                    ), null, null
                )
            } catch (_: Throwable) {}
            intrinsicWidth = width
            intrinsicHeight = height
            try {
                setPadding(0, 0, 0, bottomPadding)
            } catch (_: Throwable) {}
        }
        previewDrawableCache[color] = d
        return d
    }
}
