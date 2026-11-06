package org.futo.inputmethod.latin.uix.theme.presets

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import org.futo.inputmethod.keyboard.Key
import org.futo.inputmethod.latin.common.Constants

object ChaosThemeHelper {
    // Bauhaus Orbit Chaos Color Palette
    const val COLOR_BLACK = 0xFF000000.toInt()
    const val COLOR_YELLOW = 0xFFE2B438.toInt()    // Warm Bauhaus Ochre
    const val COLOR_BLUE = 0xFF3B72BA.toInt()      // Cobalt Slate Blue
    const val COLOR_RED = 0xFFD71921.toInt()       // Bauhaus Carmine / Nothing Red
    const val COLOR_WHITE = 0xFFEAE6E1.toInt()     // Chalk White
    const val COLOR_DARK = 0xFF1C1D20.toInt()      // Matte Graphite Slate
    const val COLOR_PRESSED = 0x2EFFFFFF.toInt()   // Soft touch highlight

    // Hint semi-transparent colors
    const val COLOR_HINT_YELLOW = 0xBBE2B438.toInt()
    const val COLOR_HINT_BLUE = 0xBB3B72BA.toInt()
    const val COLOR_HINT_RED = 0xBBD71921.toInt()
    const val COLOR_HINT_WHITE = 0xBBEAE6E1.toInt()

    private val colorByLabel = buildMap<String, Int> {
        val base = mapOf(
            // English Row 1 (q..p)
            "q" to COLOR_YELLOW, "w" to COLOR_BLUE, "e" to COLOR_WHITE, "r" to COLOR_RED, "t" to COLOR_YELLOW,
            "y" to COLOR_BLUE, "u" to COLOR_WHITE, "i" to COLOR_RED, "o" to COLOR_YELLOW, "p" to COLOR_BLUE,

            // Number Row (1..0)
            "1" to COLOR_WHITE, "2" to COLOR_RED, "3" to COLOR_YELLOW, "4" to COLOR_BLUE, "5" to COLOR_WHITE,
            "6" to COLOR_RED, "7" to COLOR_YELLOW, "8" to COLOR_BLUE, "9" to COLOR_WHITE, "0" to COLOR_RED,

            // Bengali Number Row (১..০)
            "১" to COLOR_WHITE, "২" to COLOR_RED, "৩" to COLOR_BLUE, "৪" to COLOR_YELLOW, "৫" to COLOR_WHITE,
            "৬" to COLOR_RED, "৭" to COLOR_YELLOW, "৮" to COLOR_BLUE, "৯" to COLOR_WHITE, "০" to COLOR_YELLOW,

            // English Row 2 (a..l)
            "a" to COLOR_RED, "s" to COLOR_YELLOW, "d" to COLOR_BLUE, "f" to COLOR_WHITE, "g" to COLOR_RED,
            "h" to COLOR_YELLOW, "j" to COLOR_BLUE, "k" to COLOR_WHITE, "l" to COLOR_RED,

            // English Row 3 (z..m)
            "z" to COLOR_BLUE, "x" to COLOR_WHITE, "c" to COLOR_RED, "v" to COLOR_YELLOW, "b" to COLOR_BLUE,
            "n" to COLOR_WHITE, "m" to COLOR_RED,
            "," to COLOR_YELLOW, "." to COLOR_BLUE,

            // Bengali Layouts (Avro, Jatiyo, Jatiyo Classic, Probhat)
            "ঙ" to COLOR_YELLOW, "ং" to COLOR_YELLOW, "য" to COLOR_BLUE, "য়" to COLOR_BLUE,
            "ড" to COLOR_WHITE, "ঢ" to COLOR_WHITE, "প" to COLOR_WHITE, "ফ" to COLOR_WHITE,
            "ট" to COLOR_RED, "ঠ" to COLOR_RED, "চ" to COLOR_BLUE, "ছ" to COLOR_BLUE,
            "জ" to COLOR_WHITE, "ঝ" to COLOR_WHITE, "হ" to COLOR_RED, "ঞ" to COLOR_RED,
            "গ" to COLOR_YELLOW, "ঘ" to COLOR_YELLOW, "ড়" to COLOR_WHITE, "ঢ়" to COLOR_WHITE,

            "ৃ" to COLOR_WHITE, "ঋ" to COLOR_WHITE, "ঐ" to COLOR_WHITE,
            "ু" to COLOR_RED, "উ" to COLOR_RED, "ি" to COLOR_BLUE, "ই" to COLOR_BLUE,
            "ব" to COLOR_YELLOW, "ভ" to COLOR_YELLOW, "্" to COLOR_YELLOW, "়" to COLOR_YELLOW,
            "া" to COLOR_WHITE, "আ" to COLOR_WHITE, "ক" to COLOR_RED, "খ" to COLOR_RED,
            "ত" to COLOR_BLUE, "থ" to COLOR_BLUE, "ৎ" to COLOR_BLUE, "দ" to COLOR_WHITE, "ধ" to COLOR_WHITE,

            "ঁ" to COLOR_WHITE, "ঃ" to COLOR_WHITE, "ো" to COLOR_YELLOW, "ও" to COLOR_YELLOW,
            "ে" to COLOR_WHITE, "এ" to COLOR_WHITE, "র" to COLOR_RED, "ল" to COLOR_RED,
            "ন" to COLOR_BLUE, "ণ" to COLOR_BLUE, "স" to COLOR_YELLOW, "ষ" to COLOR_YELLOW,
            "ম" to COLOR_YELLOW, "শ" to COLOR_YELLOW, "্য" to COLOR_WHITE, "্র" to COLOR_BLUE,
            "ৌ" to COLOR_YELLOW, "ৈ" to COLOR_WHITE, "ূ" to COLOR_RED, "ী" to COLOR_BLUE,

            // Standard Symbols
            "!" to COLOR_WHITE, "@" to COLOR_RED, "#" to COLOR_BLUE, "$" to COLOR_YELLOW, "%" to COLOR_WHITE,
            "^" to COLOR_RED, "&" to COLOR_BLUE, "*" to COLOR_YELLOW, "(" to COLOR_WHITE, ")" to COLOR_RED,
            "~" to COLOR_YELLOW, "`" to COLOR_BLUE, "|" to COLOR_WHITE, "+" to COLOR_YELLOW, "=" to COLOR_BLUE,
            "/" to COLOR_WHITE, "_" to COLOR_YELLOW, "<" to COLOR_RED, ">" to COLOR_BLUE,
            "[" to COLOR_WHITE, "]" to COLOR_RED, "{" to COLOR_YELLOW, "}" to COLOR_BLUE,
            "-" to COLOR_WHITE, "\\" to COLOR_YELLOW, ":" to COLOR_RED, ";" to COLOR_BLUE,
            "?" to COLOR_YELLOW, "\"" to COLOR_WHITE, "'" to COLOR_RED,
            "।" to COLOR_YELLOW, "\u0964" to COLOR_YELLOW
        )
        putAll(base)
        base.forEach { (k, v) -> put(k.uppercase(), v) }
    }

    private val rotationByLabel = mapOf(
        "q" to -18f,
        "w" to -90f, // lying down
        "e" to 180f, // upside down
        "r" to 25f,
        "t" to -35f,
        "y" to 180f, // upside down
        "u" to 20f,
        "i" to 90f,  // lying down
        "o" to -25f,
        "p" to 180f, // upside down
        "a" to 180f, // upside down (explicit user request)
        "s" to 30f,
        "d" to 90f,  // lying down
        "f" to 180f, // upside down
        "g" to -28f,
        "h" to 35f,
        "j" to 180f, // upside down
        "k" to -22f,
        "l" to -90f, // lying down
        "z" to 25f,
        "x" to -30f,
        "c" to 90f,  // lying down
        "v" to 180f, // upside down
        "b" to -25f,
        "n" to -90f, // lying down
        "m" to 180f, // upside down
        // Number row
        "1" to -15f,
        "2" to 20f,
        "3" to 180f,
        "4" to 25f,
        "5" to -30f,
        "6" to -20f, // displays 9 tilted
        "7" to -25f,
        "8" to 30f,
        "9" to 25f,  // displays 6 tilted
        "0" to 180f
    )

    fun isBengali(label: String?): Boolean {
        if (label == null) return false
        for (i in 0 until label.length) {
            val c = label[i]
            if (c in '\u0980'..'\u09FF') return true
        }
        return false
    }

    fun getKeyTextColor(key: Key): Int {
        val code = key.code
        val rawLabel = key.label

        if (code == Constants.CODE_SHIFT) return COLOR_YELLOW
        if (code == Constants.CODE_DELETE) return COLOR_WHITE
        if (code == Constants.CODE_ENTER) return COLOR_RED
        if (code == Constants.CODE_SWITCH_ALPHA_SYMBOL || code == Constants.CODE_SWITCH_BENGALI_NUM) return COLOR_RED
        if (rawLabel == "১২৩" || rawLabel == "123") return COLOR_RED
        if (code == Constants.CODE_EMOJI || code == Constants.CODE_SHORTCUT) return COLOR_BLUE
        if (code == Constants.CODE_SPACE) return COLOR_YELLOW

        if (rawLabel != null) {
            val c = colorByLabel[rawLabel]
            if (c != null) return c
        }

        // Coordinate-based fallback
        val hash = Math.abs(key.code * 31 + key.x * 17 + key.y * 7)
        return when (hash % 4) {
            0 -> COLOR_YELLOW
            1 -> COLOR_BLUE
            2 -> COLOR_RED
            else -> COLOR_WHITE
        }
    }

    fun getKeyHintColor(key: Key): Int {
        val hash = Math.abs(key.code * 31 + key.x * 17 + key.y * 7)
        return when (hash % 4) {
            0 -> COLOR_HINT_YELLOW
            1 -> COLOR_HINT_BLUE
            2 -> COLOR_HINT_RED
            else -> COLOR_HINT_WHITE
        }
    }

    fun getKeyLabelOverride(key: Key, originalLabel: String): String {
        // Swap 6 and 9 display on Latin alphabet / number keyboards
        if (originalLabel == "6") return "9"
        if (originalLabel == "9") return "6"
        return originalLabel
    }

    fun getKeyRotation(key: Key): Float {
        val rawLabel = key.label ?: return 0f
        // Only English letters and numbers in Latin mode, NOT Bengali!
        if (isBengali(rawLabel)) return 0f

        val lower = rawLabel.lowercase()
        return rotationByLabel[lower] ?: 0f
    }

    fun getKeyHintCoordinates(key: Key, keyWidth: Int, keyHeight: Int, padding: Float): FloatArray {
        // Disperse hint coordinates across key corners & edges
        val hash = Math.abs(key.code * 31 + key.x * 17 + key.y * 7)
        val mode = hash % 6
        val pad = Math.max(padding, 12f)

        return when (mode) {
            0 -> floatArrayOf(keyWidth - pad - 6f, pad + 14f, 0f)               // Top-Right
            1 -> floatArrayOf(pad + 8f, keyHeight - pad - 2f, 0f)                // Bottom-Left
            2 -> floatArrayOf(keyWidth - pad - 6f, keyHeight - pad - 2f, 0f)      // Bottom-Right
            3 -> floatArrayOf(pad + 8f, pad + 14f, 0f)                          // Top-Left
            4 -> floatArrayOf(keyWidth * 0.5f, keyHeight - pad - 2f, 0f)         // Bottom-Center
            else -> floatArrayOf(keyWidth * 0.5f, pad + 14f, 0f)                 // Top-Center
        }
    }

    private val transparentDrawable = ColorDrawable(Color.TRANSPARENT)
    private val pressedCache = HashMap<Float, Drawable>()

    @Synchronized
    fun getKeyBackground(key: Key, isPressed: Boolean, cornerRadius: Float): Drawable {
        if (key.code == Constants.CODE_SPACE) {
            val spaceColor = OrbitThemeHelper.customSpacebarColor ?: 0xFF141416.toInt()
            return GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                this.cornerRadius = cornerRadius
                setColor(spaceColor)
                val borderColor = OrbitThemeHelper.customSpacebarBorderColor
                if (borderColor != null && OrbitThemeHelper.customSpacebarBorderWidth > 0) {
                    setStroke(OrbitThemeHelper.customSpacebarBorderWidth, borderColor)
                } else {
                    setStroke(2, 0x33FFFFFF.toInt())
                }
            }
        }

        if (isPressed) {
            var d = pressedCache[cornerRadius]
            if (d == null) {
                d = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    this.cornerRadius = cornerRadius
                    setColor(COLOR_PRESSED)
                }
                pressedCache[cornerRadius] = d
            }
            return d
        }

        // BORDERLESS: No borders by default
        return transparentDrawable
    }

    fun getKeyPreviewBackground(key: Key, cornerRadius: Float, width: Int, height: Int, bottomPadding: Int): Drawable {
        val safeWidth = if (width > 0) width else 48
        val safeHeight = if (height > 0) height else 24
        val safePadding = if (bottomPadding in 0 until safeHeight) bottomPadding else 0
        return object : ShapeDrawable(RoundRectShape(
            FloatArray(8) { cornerRadius }, null, null
        )) {
            override fun getIntrinsicWidth(): Int = safeWidth
            override fun getIntrinsicHeight(): Int = safeHeight - safePadding
        }.apply {
            paint.color = COLOR_DARK
        }
    }
}
