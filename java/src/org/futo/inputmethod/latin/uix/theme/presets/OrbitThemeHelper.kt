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
    var customSpacebarBorderColor: Int? = null
    var customSpacebarBorderWidth: Int = 0
    var customSpacebarCornerRadius: Float? = null
    var spacebarShape: String = "boxy"
    var customSpacebarTextColor: Int? = null

    // Matte Bauhaus Nothing OS Color Palette
    const val COLOR_BLACK = 0xFF000000.toInt()
    const val COLOR_DARK = 0xFF1C1D20.toInt()      // Matte Graphite Slate
    const val COLOR_YELLOW = 0xFFE2B438.toInt()    // Matte Warm Ochre / Bauhaus Mustard
    const val COLOR_BLUE = 0xFF2A5288.toInt()      // Matte Bauhaus Cobalt / Navy Slate
    const val COLOR_RED = 0xFFC23636.toInt()       // Matte Nothing Carmine / Brick Red
    const val COLOR_WHITE = 0xFFEAE6E1.toInt()     // Matte Chalk / Soft Alabaster
    const val COLOR_TEXT_DARK = 0xFF121212.toInt() // Matte Black for text on light keys

    // Additional theme-matching spacebar colors
    const val COLOR_EMERALD = 0xFF2ECC71.toInt()   // Vibrant Emerald Green
    const val COLOR_PURPLE = 0xFF7C4DFF.toInt()    // Deep Bauhaus Purple
    const val COLOR_CYAN = 0xFF00BCD4.toInt()      // High-tech Cyan
    const val COLOR_ORANGE = 0xFFFF9800.toInt()    // Electric Warm Orange
    const val COLOR_PINK = 0xFFEC5EB4.toInt()      // Cotton Candy Pink
    const val COLOR_GRAPHITE = 0xFF37474F.toInt()  // Matte Dark Graphite Slate

    // Pressed matte colors
    const val COLOR_DARK_PRESSED = 0xFF2B2D31.toInt()
    const val COLOR_YELLOW_PRESSED = 0xFFC49B28.toInt()
    const val COLOR_BLUE_PRESSED = 0xFF203F68.toInt()
    const val COLOR_RED_PRESSED = 0xFF9E2828.toInt()
    const val COLOR_WHITE_PRESSED = 0xFFC8C4BE.toInt()

    private val colorByLabel = buildMap<String, Int> {
        val base = mapOf(
            // English Row 1 (q..p)
            "q" to COLOR_YELLOW, "w" to COLOR_BLUE, "e" to COLOR_DARK, "r" to COLOR_WHITE, "t" to COLOR_RED,
            "y" to COLOR_BLUE, "u" to COLOR_WHITE, "i" to COLOR_RED, "o" to COLOR_YELLOW, "p" to COLOR_DARK,

            // Number Row (1..0) - Offset palette so it NEVER matches Row 1 column-by-column
            "1" to COLOR_DARK, "2" to COLOR_RED, "3" to COLOR_BLUE, "4" to COLOR_YELLOW, "5" to COLOR_WHITE,
            "6" to COLOR_DARK, "7" to COLOR_RED, "8" to COLOR_BLUE, "9" to COLOR_WHITE, "0" to COLOR_YELLOW,

            // Bengali Number Row (১..০)
            "১" to COLOR_DARK, "২" to COLOR_RED, "৩" to COLOR_BLUE, "৪" to COLOR_YELLOW, "৫" to COLOR_WHITE,
            "৬" to COLOR_DARK, "৭" to COLOR_RED, "৮" to COLOR_BLUE, "৯" to COLOR_WHITE, "০" to COLOR_YELLOW,

            // English Row 2 (a..l)
            "a" to COLOR_WHITE, "s" to COLOR_RED, "d" to COLOR_BLUE, "f" to COLOR_DARK, "g" to COLOR_YELLOW,
            "h" to COLOR_WHITE, "j" to COLOR_RED, "k" to COLOR_BLUE, "l" to COLOR_DARK,

            // English Row 3 (z..m)
            "z" to COLOR_DARK, "x" to COLOR_YELLOW, "c" to COLOR_WHITE, "v" to COLOR_RED, "b" to COLOR_BLUE,
            "n" to COLOR_DARK, "m" to COLOR_YELLOW,
            "," to COLOR_WHITE, "." to COLOR_DARK,

            // Bengali Layouts (Avro, Jatiyo, Jatiyo Classic, Probhat)
            // Row 1
            "ঙ" to COLOR_YELLOW, "ং" to COLOR_YELLOW, "য" to COLOR_BLUE, "য়" to COLOR_BLUE,
            "ড" to COLOR_DARK, "ঢ" to COLOR_DARK, "প" to COLOR_WHITE, "ফ" to COLOR_WHITE,
            "ট" to COLOR_RED, "ঠ" to COLOR_RED, "চ" to COLOR_BLUE, "ছ" to COLOR_BLUE,
            "জ" to COLOR_WHITE, "ঝ" to COLOR_WHITE, "হ" to COLOR_RED, "ঞ" to COLOR_RED,
            "গ" to COLOR_YELLOW, "ঘ" to COLOR_YELLOW, "ড়" to COLOR_DARK, "ঢ়" to COLOR_DARK,

            // Row 2
            "ৃ" to COLOR_WHITE, "ঋ" to COLOR_WHITE, "ঐ" to COLOR_WHITE,
            "ু" to COLOR_RED, "উ" to COLOR_RED, "ি" to COLOR_BLUE, "ই" to COLOR_BLUE,
            "ব" to COLOR_DARK, "ভ" to COLOR_DARK, "্" to COLOR_YELLOW, "়" to COLOR_YELLOW,
            "া" to COLOR_WHITE, "আ" to COLOR_WHITE, "ক" to COLOR_RED, "খ" to COLOR_RED,
            "ত" to COLOR_BLUE, "থ" to COLOR_BLUE, "ৎ" to COLOR_BLUE, "দ" to COLOR_DARK, "ধ" to COLOR_DARK,

            // Row 3
            "ঁ" to COLOR_DARK, "ঃ" to COLOR_DARK, "ো" to COLOR_YELLOW, "ও" to COLOR_YELLOW,
            "ে" to COLOR_WHITE, "এ" to COLOR_WHITE, "র" to COLOR_RED, "ল" to COLOR_RED,
            "ন" to COLOR_BLUE, "ণ" to COLOR_BLUE, "স" to COLOR_DARK, "ষ" to COLOR_DARK,
            "ম" to COLOR_YELLOW, "শ" to COLOR_YELLOW, "্য" to COLOR_WHITE, "্র" to COLOR_BLUE,
            "ৌ" to COLOR_YELLOW, "ৈ" to COLOR_WHITE, "ূ" to COLOR_RED, "ী" to COLOR_BLUE,

            // Standard Symbols
            "!" to COLOR_WHITE, "@" to COLOR_RED, "#" to COLOR_BLUE, "$" to COLOR_DARK, "%" to COLOR_YELLOW,
            "^" to COLOR_WHITE, "&" to COLOR_RED, "*" to COLOR_BLUE, "(" to COLOR_DARK, ")" to COLOR_YELLOW,
            "~" to COLOR_YELLOW, "`" to COLOR_BLUE, "|" to COLOR_DARK, "+" to COLOR_YELLOW, "=" to COLOR_BLUE,
            "/" to COLOR_DARK, "_" to COLOR_WHITE, "<" to COLOR_RED, ">" to COLOR_BLUE,
            "[" to COLOR_WHITE, "]" to COLOR_RED, "{" to COLOR_YELLOW, "}" to COLOR_DARK,
            "-" to COLOR_DARK, "\\" to COLOR_YELLOW, ":" to COLOR_WHITE, ";" to COLOR_RED,
            "?" to COLOR_BLUE, "\"" to COLOR_DARK, "'" to COLOR_YELLOW,
            "।" to COLOR_DARK, "\u0964" to COLOR_DARK
        )
        putAll(base)
        base.forEach { (k, v) -> put(k.uppercase(), v) }
    }

    private val numberRowColors = intArrayOf(
        COLOR_DARK, COLOR_RED, COLOR_BLUE, COLOR_YELLOW, COLOR_WHITE,
        COLOR_DARK, COLOR_RED, COLOR_BLUE, COLOR_WHITE, COLOR_YELLOW
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
        intArrayOf(COLOR_YELLOW, COLOR_BLUE, COLOR_DARK, COLOR_WHITE, COLOR_RED, COLOR_BLUE, COLOR_WHITE, COLOR_RED, COLOR_YELLOW, COLOR_DARK),
        intArrayOf(COLOR_WHITE, COLOR_RED, COLOR_BLUE, COLOR_DARK, COLOR_YELLOW, COLOR_WHITE, COLOR_RED, COLOR_BLUE, COLOR_DARK, COLOR_YELLOW),
        intArrayOf(COLOR_DARK, COLOR_YELLOW, COLOR_WHITE, COLOR_RED, COLOR_BLUE, COLOR_DARK, COLOR_YELLOW, COLOR_WHITE, COLOR_RED, COLOR_BLUE),
        intArrayOf(COLOR_BLUE, COLOR_WHITE, COLOR_RED, COLOR_YELLOW, COLOR_DARK, COLOR_BLUE, COLOR_WHITE, COLOR_RED, COLOR_YELLOW, COLOR_DARK),
        intArrayOf(COLOR_RED, COLOR_BLUE, COLOR_YELLOW, COLOR_DARK, COLOR_WHITE, COLOR_RED, COLOR_BLUE, COLOR_YELLOW, COLOR_DARK, COLOR_WHITE)
    )

    fun getKeyBaseColor(key: Key): Int {
        val code = key.code
        val rawLabel = key.label

        if (code == Constants.CODE_SHIFT) return COLOR_YELLOW
        if (code == Constants.CODE_DELETE) return COLOR_WHITE
        if (code == Constants.CODE_ENTER) return COLOR_RED
        if (code == Constants.CODE_SWITCH_ALPHA_SYMBOL || code == Constants.CODE_SWITCH_BENGALI_NUM) return COLOR_RED
        if (rawLabel == "১২৩" || rawLabel == "123") return COLOR_RED
        if (code == Constants.CODE_EMOJI || code == Constants.CODE_SHORTCUT) return COLOR_BLUE
        if (code == Constants.CODE_SPACE) return customSpacebarColor ?: COLOR_DARK
        if (rawLabel == "।" || rawLabel == "\u0964") return COLOR_DARK

        // Dedicated balanced Bauhaus palette for 5-row alphabetical Bangla layout (bengali_akkhor)
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
                return if (w > 0 && x < w * 2) COLOR_RED
                else if (w > 0 && x > w * 6) COLOR_RED
                else COLOR_DARK
            }
            return COLOR_DARK
        }

        // Direct label lookup (handles English, Bengali, Numbers, and Symbols deterministically)
        if (rawLabel != null) {
            val c = colorByLabel[rawLabel]
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
        if (key.code == Constants.CODE_SPACE) {
            val defaultSpaceRadius = if (spacebarShape == "roundy") 48f * (cornerRadius / 8f).coerceAtLeast(1f) else cornerRadius
            val spaceRadius = customSpacebarCornerRadius ?: defaultSpaceRadius
            return GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                this.cornerRadius = spaceRadius
                setColor(color)
                if (customSpacebarBorderColor != null && customSpacebarBorderWidth > 0) {
                    setStroke(customSpacebarBorderWidth, customSpacebarBorderColor!!)
                }
            }
        }
        return getDrawableForColor(color, cornerRadius)
    }

    // Cached popup preview drawables matching key colors (exact v1.2.3 implementation)
    private val previewDrawableCache = HashMap<Int, ShapeDrawable>()

    @Synchronized
    fun getKeyPreviewBackground(key: Key, cornerRadius: Float, width: Int, height: Int, bottomPadding: Int): Drawable {
        val color = getKeyBaseColor(key)
        val existing = previewDrawableCache[color]
        if (existing != null) {
            return existing
        }
        val d = ShapeDrawable().apply {
            paint.color = color
            shape = RoundRectShape(
                floatArrayOf(
                    cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                    cornerRadius, cornerRadius, cornerRadius, cornerRadius
                ), null, null
            )
            intrinsicWidth = width
            intrinsicHeight = height
            setPadding(0, 0, 0, bottomPadding)
        }
        previewDrawableCache[color] = d
        return d
    }
}
