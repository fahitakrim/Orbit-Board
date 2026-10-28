package org.futo.inputmethod.latin.uix.theme.presets

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import org.futo.inputmethod.keyboard.Key
import org.futo.inputmethod.latin.common.Constants

object OrbitThemeHelper {
    var customSpacebarColor: Int? = null

    // Matte Bauhaus Nothing OS Color Palette
    const val COLOR_BLACK = 0xFF000000.toInt()
    const val COLOR_DARK = 0xFF1C1D20.toInt()      // Matte Graphite Slate
    const val COLOR_YELLOW = 0xFFE2B438.toInt()    // Matte Warm Ochre / Bauhaus Mustard
    const val COLOR_BLUE = 0xFF2A5288.toInt()      // Matte Bauhaus Cobalt / Navy Slate
    const val COLOR_RED = 0xFFC23636.toInt()       // Matte Nothing Carmine / Brick Red
    const val COLOR_WHITE = 0xFFEAE6E1.toInt()     // Matte Chalk / Soft Alabaster
    const val COLOR_TEXT_DARK = 0xFF121212.toInt() // Matte Black for text on light keys

    // Pressed matte colors
    const val COLOR_DARK_PRESSED = 0xFF2B2D31.toInt()
    const val COLOR_YELLOW_PRESSED = 0xFFC49B28.toInt()
    const val COLOR_BLUE_PRESSED = 0xFF203F68.toInt()
    const val COLOR_RED_PRESSED = 0xFF9E2828.toInt()
    const val COLOR_WHITE_PRESSED = 0xFFC8C4BE.toInt()

    private val colorByLabel = mapOf(
        // Row 1
        "q" to COLOR_YELLOW, "w" to COLOR_BLUE, "e" to COLOR_DARK, "r" to COLOR_WHITE, "t" to COLOR_RED,
        "y" to COLOR_BLUE, "u" to COLOR_WHITE, "i" to COLOR_RED, "o" to COLOR_YELLOW, "p" to COLOR_DARK,
        "1" to COLOR_YELLOW, "2" to COLOR_BLUE, "3" to COLOR_DARK, "4" to COLOR_WHITE, "5" to COLOR_RED,
        "6" to COLOR_BLUE, "7" to COLOR_WHITE, "8" to COLOR_RED, "9" to COLOR_YELLOW, "0" to COLOR_DARK,
        // Row 2
        "a" to COLOR_WHITE, "s" to COLOR_RED, "d" to COLOR_BLUE, "f" to COLOR_DARK, "g" to COLOR_YELLOW,
        "h" to COLOR_WHITE, "j" to COLOR_RED, "k" to COLOR_BLUE, "l" to COLOR_DARK,
        // Row 3
        "z" to COLOR_DARK, "x" to COLOR_YELLOW, "c" to COLOR_WHITE, "v" to COLOR_RED, "b" to COLOR_BLUE,
        "n" to COLOR_DARK, "m" to COLOR_YELLOW,
        // Punctuation
        "," to COLOR_WHITE, "." to COLOR_DARK
    )

    private val row1Colors = intArrayOf(
        COLOR_YELLOW, COLOR_BLUE, COLOR_DARK, COLOR_WHITE, COLOR_RED,
        COLOR_BLUE, COLOR_WHITE, COLOR_RED, COLOR_YELLOW, COLOR_DARK
    )

    private val row2Colors = intArrayOf(
        COLOR_WHITE, COLOR_RED, COLOR_BLUE, COLOR_DARK, COLOR_YELLOW,
        COLOR_WHITE, COLOR_RED, COLOR_BLUE, COLOR_DARK, COLOR_WHITE
    )

    private val row3Colors = intArrayOf(
        COLOR_DARK, COLOR_YELLOW, COLOR_WHITE, COLOR_RED, COLOR_BLUE,
        COLOR_DARK, COLOR_YELLOW, COLOR_DARK
    )

    // Dedicated Bauhaus color palette for 5-row alphabetical Bangla layout (bengali_akkhor)
    private val banglaAkkhorColorByLabel = mapOf(
        // Row 0: Vowels (10 keys)
        "অ" to COLOR_YELLOW,
        "আ" to COLOR_BLUE,
        "ই" to COLOR_DARK,
        "ঈ" to COLOR_WHITE,
        "উ" to COLOR_RED,
        "ঊ" to COLOR_BLUE,
        "এ" to COLOR_WHITE,
        "ঐ" to COLOR_RED,
        "ও" to COLOR_YELLOW,
        "ঔ" to COLOR_DARK,

        // Row 1: Consonants 1 (10 keys)
        "ক" to COLOR_WHITE,
        "খ" to COLOR_RED,
        "গ" to COLOR_BLUE,
        "ঘ" to COLOR_DARK,
        "ঙ" to COLOR_YELLOW,
        "চ" to COLOR_WHITE,
        "ছ" to COLOR_RED,
        "জ" to COLOR_BLUE,
        "ঝ" to COLOR_DARK,
        "ঞ" to COLOR_YELLOW,

        // Row 2: Consonants 2 (10 keys)
        "ট" to COLOR_DARK,
        "ঠ" to COLOR_YELLOW,
        "ড" to COLOR_WHITE,
        "ঢ" to COLOR_RED,
        "ণ" to COLOR_BLUE,
        "ত" to COLOR_DARK,
        "থ" to COLOR_YELLOW,
        "দ" to COLOR_WHITE,
        "ধ" to COLOR_RED,
        "ন" to COLOR_BLUE,

        // Row 3: Consonants 3 (10 keys)
        "প" to COLOR_BLUE,
        "ফ" to COLOR_WHITE,
        "ব" to COLOR_RED,
        "ভ" to COLOR_YELLOW,
        "ম" to COLOR_DARK,
        "য" to COLOR_BLUE,
        "র" to COLOR_WHITE,
        "ল" to COLOR_RED,
        "শ" to COLOR_YELLOW,
        "ষ" to COLOR_DARK,

        // Row 4: Consonants 4 & Signs (10 keys)
        "স" to COLOR_RED,
        "হ" to COLOR_BLUE,
        "ড়" to COLOR_YELLOW,
        "ঢ়" to COLOR_DARK,
        "য়" to COLOR_WHITE,
        "ৎ" to COLOR_RED,
        "\u25CC\u09CD\u09AF" to COLOR_BLUE, // ◌্য
        "◌্য" to COLOR_BLUE,
        "ক্ষ" to COLOR_YELLOW,
        "\u09CD" to COLOR_DARK,             // ্
        "্" to COLOR_DARK,

        // Punctuation
        "।" to COLOR_DARK,
        "\u0964" to COLOR_DARK,
        "," to COLOR_WHITE,
        "." to COLOR_DARK
    )

    private val banglaAkkhorRowColors = arrayOf(
        // Row 0
        intArrayOf(COLOR_YELLOW, COLOR_BLUE, COLOR_DARK, COLOR_WHITE, COLOR_RED, COLOR_BLUE, COLOR_WHITE, COLOR_RED, COLOR_YELLOW, COLOR_DARK),
        // Row 1
        intArrayOf(COLOR_WHITE, COLOR_RED, COLOR_BLUE, COLOR_DARK, COLOR_YELLOW, COLOR_WHITE, COLOR_RED, COLOR_BLUE, COLOR_DARK, COLOR_YELLOW),
        // Row 2
        intArrayOf(COLOR_DARK, COLOR_YELLOW, COLOR_WHITE, COLOR_RED, COLOR_BLUE, COLOR_DARK, COLOR_YELLOW, COLOR_WHITE, COLOR_RED, COLOR_BLUE),
        // Row 3
        intArrayOf(COLOR_BLUE, COLOR_WHITE, COLOR_RED, COLOR_YELLOW, COLOR_DARK, COLOR_BLUE, COLOR_WHITE, COLOR_RED, COLOR_YELLOW, COLOR_DARK),
        // Row 4
        intArrayOf(COLOR_RED, COLOR_BLUE, COLOR_YELLOW, COLOR_DARK, COLOR_WHITE, COLOR_RED, COLOR_BLUE, COLOR_YELLOW, COLOR_DARK, COLOR_WHITE)
    )

    fun getKeyBaseColor(key: Key): Int {
        val code = key.code
        val rawLabel = key.label
        val label = rawLabel?.lowercase()

        if (code == Constants.CODE_SHIFT) return COLOR_YELLOW
        if (code == Constants.CODE_DELETE) return COLOR_WHITE
        if (code == Constants.CODE_ENTER) return COLOR_RED
        if (code == Constants.CODE_SWITCH_ALPHA_SYMBOL) return COLOR_RED
        if (code == Constants.CODE_EMOJI || code == Constants.CODE_SHORTCUT) return COLOR_BLUE
        if (code == Constants.CODE_SPACE) return customSpacebarColor ?: COLOR_DARK
        if (rawLabel == "।" || rawLabel == "\u0964") return COLOR_DARK

        // Dedicated balanced Bauhaus palette for 5-row alphabetical Bangla layout (bengali_akkhor)
        // Scoped strictly so other layouts are never affected
        if (key.keyboardLayoutSetName == "bengali_akkhor" && key.isAlphabetKeyboard) {
            if (rawLabel != null && banglaAkkhorColorByLabel.containsKey(rawLabel)) {
                return banglaAkkhorColorByLabel[rawLabel]!!
            }
            val y = key.y
            val x = key.x
            val h = key.height
            val w = key.width
            if (h > 0) {
                val row = y / h
                if (row in 0..4) {
                    val colors = banglaAkkhorRowColors[row]
                    val idx = if (w > 0) (x / w).coerceIn(0, colors.size - 1) else 0
                    return colors[idx]
                }
                // Bottom row fallback
                return if (w > 0 && x < w * 2) COLOR_RED
                else if (w > 0 && x > w * 6) COLOR_RED
                else COLOR_DARK
            }
            return COLOR_DARK
        }

        if (label != null && colorByLabel.containsKey(label)) {
            return colorByLabel[label]!!
        }

        // Coordinate-based fallback for Bengali / other script layouts
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
                    if (x < h) COLOR_YELLOW
                    else {
                        val idx = if (w > 0) ((x - h) / w).coerceIn(0, row3Colors.size - 1) else 0
                        row3Colors[idx]
                    }
                }
                else -> {
                    if (w > 0 && x < w * 2) COLOR_RED
                    else if (w > 0 && x > w * 6) COLOR_RED
                    else COLOR_DARK
                }
            }
        }

        return COLOR_DARK
    }

    fun getKeyBackgroundColor(key: Key, isPressed: Boolean): Int {
        val base = getKeyBaseColor(key)
        if (!isPressed) return base
        return when (base) {
            COLOR_YELLOW -> COLOR_YELLOW_PRESSED
            COLOR_BLUE -> COLOR_BLUE_PRESSED
            COLOR_RED -> COLOR_RED_PRESSED
            COLOR_WHITE -> COLOR_WHITE_PRESSED
            else -> COLOR_DARK_PRESSED
        }
    }

    fun getKeyTextColor(key: Key): Int {
        val base = getKeyBaseColor(key)
        return if (base == COLOR_WHITE || base == COLOR_YELLOW) COLOR_TEXT_DARK else Color.WHITE
    }

    fun getKeyHintColor(key: Key): Int {
        val base = getKeyBaseColor(key)
        return if (base == COLOR_WHITE || base == COLOR_YELLOW) 0x77000000.toInt() else 0x99FFFFFF.toInt()
    }

    // Cached drawables for smooth 120Hz rendering
    private val drawableCache = HashMap<Int, GradientDrawable>()

    @Synchronized
    fun getDrawableForColor(color: Int, cornerRadius: Float): Drawable {
        val existing = drawableCache[color]
        if (existing != null) {
            return existing
        }
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
        return getDrawableForColor(color, cornerRadius)
    }

    fun getKeyPreviewBackground(key: Key, cornerRadius: Float, width: Int, height: Int, bottomPadding: Int): Drawable {
        val color = getKeyBaseColor(key)
        val safeWidth = if (width > 0) width else 48
        val safeHeight = if (height > 0) height else 24
        val safePadding = if (bottomPadding in 0 until safeHeight) bottomPadding else 0
        return ShapeDrawable().apply {
            paint.color = color
            shape = RoundRectShape(
                floatArrayOf(
                    cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                    cornerRadius, cornerRadius, cornerRadius, cornerRadius
                ), null, null
            )
            intrinsicWidth = safeWidth
            intrinsicHeight = safeHeight
            setPadding(0, 0, 0, safePadding)
        }
    }
}
