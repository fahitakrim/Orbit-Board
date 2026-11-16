package org.futo.inputmethod.latin.uix.theme.presets

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import org.futo.inputmethod.keyboard.Key
import org.futo.inputmethod.latin.common.Constants

/**
 * Coquette Pink Pookie Theme Helper (Berry Board):
 * - Soft Blush Pink (#FAD0DC), Bubblegum Pink (#F9B4CA), Pastel Lavender (#D8C8E8),
 *   Rose Mauve (#B87D8B), Warm Cream (#FAF5EE), Hot Pink Backspace (#FF3E8A),
 *   and Berry Rose Cat-Head Enter (#B83362).
 * - Unclipped Cat Ears on ?123, Emoji, and Enter keys.
 * - Text-Free Blush Spacebar with Dark-Berry Dotted Rim + Pink Coquette Bow.
 * - Illustrated Kitty-Paw Backspace Key.
 * - Permanently Locked Cute Glyphs: 'q', 'i', 'o', 'g'.
 * - Micro-doodles: w & a (mini bows), y & p (sparkles), c (kitty paw), n & m (double cherries).
 */
object BerryBlushThemeHelper {
    // ------------------------------------------------------------------------
    // 1. Palette Architecture (Exact match to HTML specification)
    // ------------------------------------------------------------------------
    const val BLUSH_PINK = 0xFFFAD0DC.toInt()         // Soft Strawberry Blush (#FAD0DC)
    const val BUBBLEGUM_PINK = 0xFFF9B4CA.toInt()     // Bubblegum Pink (#F9B4CA)
    const val LAVENDER = 0xFFD8C8E8.toInt()           // Pastel Lavender (#D8C8E8)
    const val ROSE_MAUVE = 0xFFB87D8B.toInt()         // Rose Mauve (#B87D8B)
    const val WARM_CREAM = 0xFFFAF5EE.toInt()         // Warm Cream (#FAF5EE)
    const val HOT_PINK = 0xFFFF3E8A.toInt()           // Vibrant Kitty-Paw Backspace Pink (#FF3E8A)
    const val BERRY_ROSE = 0xFFB83362.toInt()         // Berry Rose Cat-Head Enter (#B83362)
    const val DARK_INK = 0xFF5E1D35.toInt()           // Dark Ink for dots, outlines, text (#5E1D35)
    const val LIGHT_INK = 0xFFFFF8FA.toInt()          // White/Light Ink for dots on dark keys (#FFF8FA)
    const val BOW_FILL = 0xFFFF6B9D.toInt()           // Spacebar & Accent Bow Fill (#FF6B9D)
    const val PAW_STRIPE = 0xFFD68A9E.toInt()         // Tabby stripes on Kitty Paw (#D68A9E)
    const val CHERRY_RED = 0xFFD91E48.toInt()         // Double cherries (#D91E48)
    const val COLOR_BACKGROUND = 0xFF000000.toInt()   // Pitch Black chassis default

    // Legacy Aliases for backward compatibility
    const val COLOR_PINK = BLUSH_PINK
    const val COLOR_DUSTY_ROSE = ROSE_MAUVE
    const val COLOR_LAVENDER = LAVENDER
    const val COLOR_CREAM = WARM_CREAM
    const val COLOR_BERRY = BERRY_ROSE
    const val COLOR_WINE_BURGUNDY = BLUSH_PINK
    const val COLOR_RUBY = HOT_PINK
    const val COLOR_RUBY_RED = HOT_PINK
    const val COLOR_TEXT_BURGUNDY = DARK_INK
    const val COLOR_TEXT_LIGHT = LIGHT_INK

    // Spacebar customization properties
    var customSpacebarColor: Int? = null
    var customSpacebarBorderColor: Int? = null
    var customSpacebarBorderWidth: Int = 0
    var customSpacebarTextColor: Int? = null
    var customSpacebarCornerRadius: Float? = null
    var spacebarShape: String = "boxy"

    // Pressed colors
    const val BLUSH_PINK_PRESSED = 0xFFE5B0B0.toInt()
    const val BUBBLEGUM_PINK_PRESSED = 0xFFE29EB3.toInt()
    const val LAVENDER_PRESSED = 0xFFC3B3D3.toInt()
    const val ROSE_MAUVE_PRESSED = 0xFFA06875.toInt()
    const val WARM_CREAM_PRESSED = 0xFFE6E1DA.toInt()
    const val HOT_PINK_PRESSED = 0xFFE02A72.toInt()
    const val BERRY_ROSE_PRESSED = 0xFF9E254F.toInt()

    const val COLOR_PINK_PRESSED = BLUSH_PINK_PRESSED
    const val COLOR_DUSTY_ROSE_PRESSED = ROSE_MAUVE_PRESSED
    const val COLOR_LAVENDER_PRESSED = LAVENDER_PRESSED
    const val COLOR_CREAM_PRESSED = WARM_CREAM_PRESSED
    const val COLOR_BERRY_PRESSED = BERRY_ROSE_PRESSED
    const val COLOR_WINE_BURGUNDY_PRESSED = BLUSH_PINK_PRESSED
    const val COLOR_RUBY_PRESSED = HOT_PINK_PRESSED
    const val COLOR_RUBY_RED_PRESSED = HOT_PINK_PRESSED

    val LOCKED_KAWAII_CHARS = setOf('q', 'i', 'o', 'g')

    private val colorByLabel = buildMap<String, Int> {
        val base = mapOf(
            // English Row 1 (q..p)
            "q" to BLUSH_PINK,
            "w" to LAVENDER,
            "e" to ROSE_MAUVE,
            "r" to WARM_CREAM,
            "t" to BUBBLEGUM_PINK,
            "y" to ROSE_MAUVE,
            "u" to LAVENDER,
            "i" to WARM_CREAM,
            "o" to BLUSH_PINK,
            "p" to ROSE_MAUVE,

            // Number Row (1..0)
            "1" to BLUSH_PINK, "2" to LAVENDER, "3" to ROSE_MAUVE, "4" to WARM_CREAM, "5" to BUBBLEGUM_PINK,
            "6" to ROSE_MAUVE, "7" to LAVENDER, "8" to WARM_CREAM, "9" to BLUSH_PINK, "0" to ROSE_MAUVE,

            // Bengali Number Row
            "১" to BLUSH_PINK, "২" to LAVENDER, "৩" to ROSE_MAUVE, "৪" to WARM_CREAM, "৫" to BUBBLEGUM_PINK,
            "৬" to ROSE_MAUVE, "৭" to LAVENDER, "৮" to WARM_CREAM, "৯" to BLUSH_PINK, "০" to ROSE_MAUVE,

            // English Row 2 (a..l)
            "a" to WARM_CREAM,
            "s" to LAVENDER,
            "d" to BLUSH_PINK,
            "f" to ROSE_MAUVE,
            "g" to WARM_CREAM,
            "h" to LAVENDER,
            "j" to BLUSH_PINK,
            "k" to ROSE_MAUVE,
            "l" to WARM_CREAM,

            // English Row 3 (z..m)
            "z" to WARM_CREAM,
            "x" to BUBBLEGUM_PINK,
            "c" to LAVENDER,
            "v" to ROSE_MAUVE,
            "b" to WARM_CREAM,
            "n" to BUBBLEGUM_PINK,
            "m" to LAVENDER,
            "," to WARM_CREAM,
            "." to ROSE_MAUVE,

            // Switch / Mode Keys
            "?123" to LAVENDER, "abc" to LAVENDER, "123" to LAVENDER, "১২৩" to LAVENDER, "7123" to LAVENDER,

            // Bengali Alphabet Mapping
            "অ" to WARM_CREAM, "আ" to WARM_CREAM, "ই" to LAVENDER, "ঈ" to LAVENDER,
            "উ" to BLUSH_PINK, "ূ" to BLUSH_PINK, "ঊ" to BLUSH_PINK,
            "ঋ" to ROSE_MAUVE, "ৃ" to ROSE_MAUVE, "এ" to WARM_CREAM, "ঐ" to WARM_CREAM,
            "ও" to BLUSH_PINK, "ঔ" to BLUSH_PINK, "ক্ষ" to ROSE_MAUVE,
            "\u25CC\u09CD\u09AF" to WARM_CREAM, "্য" to WARM_CREAM,
            "ঙ" to BLUSH_PINK, "ং" to BLUSH_PINK, "য" to LAVENDER, "য়" to LAVENDER,
            "ড" to ROSE_MAUVE, "ঢ" to ROSE_MAUVE, "প" to WARM_CREAM, "ফ" to WARM_CREAM,
            "ব" to LAVENDER, "ভ" to LAVENDER, "ম" to BLUSH_PINK, "ন" to WARM_CREAM,
            "ণ" to WARM_CREAM, "ত" to ROSE_MAUVE, "থ" to ROSE_MAUVE, "দ" to LAVENDER,
            "ধ" to LAVENDER, "চ" to WARM_CREAM, "ছ" to WARM_CREAM, "জ" to BLUSH_PINK,
            "ঝ" to BLUSH_PINK, "ট" to ROSE_MAUVE, "ঠ" to ROSE_MAUVE, "ক" to WARM_CREAM,
            "খ" to WARM_CREAM, "গ" to LAVENDER, "ঘ" to LAVENDER, "হ" to BLUSH_PINK,
            "স" to ROSE_MAUVE, "শ" to ROSE_MAUVE, "ষ" to WARM_CREAM, "র" to LAVENDER,
            "ল" to BLUSH_PINK, "ড়" to WARM_CREAM, "ঢ়" to WARM_CREAM, "ৎ" to ROSE_MAUVE,
            "ঃ" to BLUSH_PINK, "ঁ" to BLUSH_PINK
        )
        putAll(base)
        base.forEach { (k, v) -> put(k.uppercase(), v) }
    }

    fun isLightKey(color: Int): Boolean {
        return color != BERRY_ROSE && color != HOT_PINK && color != 0xFF000000.toInt()
    }

    fun getKeyBaseColor(key: Key): Int {
        val rawLabel = key.label?.trim()
        val code = key.code

        if (code == Constants.CODE_DELETE) return HOT_PINK
        if (code == Constants.CODE_ENTER) return BERRY_ROSE
        if (code == Constants.CODE_SHIFT) return ROSE_MAUVE
        if (code == Constants.CODE_SWITCH_ALPHA_SYMBOL || code == Constants.CODE_SWITCH_BENGALI_NUM) return LAVENDER
        if (rawLabel == "১২৩" || rawLabel == "123" || rawLabel == "?123" || rawLabel == "7123" || rawLabel?.lowercase() == "abc") return LAVENDER
        if (code == Constants.CODE_EMOJI || code == Constants.CODE_SHORTCUT) return BLUSH_PINK
        if (code == Constants.CODE_SPACE) return customSpacebarColor ?: BLUSH_PINK
        if (rawLabel == "." || rawLabel?.startsWith(".") == true) return ROSE_MAUVE
        if (rawLabel == ",") return WARM_CREAM

        if (rawLabel != null) {
            val c = colorByLabel[rawLabel] ?: colorByLabel[rawLabel.lowercase()]
            if (c != null) return c
        }

        val codeChar = code.toChar().toString()
        val fromChar = colorByLabel[codeChar] ?: colorByLabel[codeChar.lowercase()]
        if (fromChar != null) return fromChar

        val hash = Math.abs(key.code * 31 + key.x * 17 + key.y * 7)
        return when (hash % 5) {
            0 -> BLUSH_PINK
            1 -> BUBBLEGUM_PINK
            2 -> LAVENDER
            3 -> ROSE_MAUVE
            else -> WARM_CREAM
        }
    }

    fun getKeyTextColor(key: Key): Int {
        if (key.code == Constants.CODE_SPACE) {
            return customSpacebarTextColor ?: DARK_INK
        }
        val base = getKeyBaseColor(key)
        return if (isLightKey(base)) DARK_INK else LIGHT_INK
    }

    fun getKeyHintColor(key: Key): Int {
        if (key.code == Constants.CODE_SPACE) {
            return customSpacebarTextColor ?: DARK_INK
        }
        val base = getKeyBaseColor(key)
        return if (isLightKey(base)) 0x885E1D35.toInt() else 0xCCFFF8FA.toInt()
    }

    // ------------------------------------------------------------------------
    // 2. Seamless Cat-Ear Keycap Drawable (?123, Emoji, Enter)
    // ------------------------------------------------------------------------
    class CatEarKeycapDrawable(
        private val baseColor: Int,
        private val pressedColor: Int,
        private val cornerRadius: Float,
        private val isOval: Boolean = false,
        private val innerEarColor: Int = WARM_CREAM
    ) : Drawable() {
        private var isPressedState = false
        private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val earPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val innerEarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val earPath = Path()
        private val bodyRect = RectF()

        fun setPressed(pressed: Boolean) {
            if (isPressedState != pressed) {
                isPressedState = pressed
                invalidateSelf()
            }
        }

        override fun draw(canvas: Canvas) {
            val b = bounds
            val w = b.width().toFloat()
            val h = b.height().toFloat()
            if (w <= 0f || h <= 0f) return

            val currentColor = if (isPressedState) pressedColor else baseColor
            bodyPaint.color = currentColor
            earPaint.color = currentColor
            innerEarPaint.color = innerEarColor

            val earH = (h * 0.16f).coerceIn(6f, 16f)
            val bodyX = b.left.toFloat()
            val bodyY = b.top + earH
            val bodyW = w
            val bodyH = h - earH
            val radius = if (isOval) Math.min(bodyW, bodyH) * 0.48f else cornerRadius

            // Left ear outer path
            earPath.reset()
            earPath.moveTo(bodyX + bodyW * 0.08f, bodyY + bodyH * 0.35f)
            earPath.lineTo(bodyX + bodyW * 0.16f, b.top + 1f)
            earPath.lineTo(bodyX + bodyW * 0.43f, bodyY + bodyH * 0.14f)
            earPath.close()
            canvas.drawPath(earPath, earPaint)

            // Right ear outer path
            earPath.reset()
            earPath.moveTo(bodyX + bodyW * 0.92f, bodyY + bodyH * 0.35f)
            earPath.lineTo(bodyX + bodyW * 0.84f, b.top + 1f)
            earPath.lineTo(bodyX + bodyW * 0.57f, bodyY + bodyH * 0.14f)
            earPath.close()
            canvas.drawPath(earPath, earPaint)

            // Left inner ear
            earPath.reset()
            earPath.moveTo(bodyX + bodyW * 0.14f, bodyY + bodyH * 0.25f)
            earPath.lineTo(bodyX + bodyW * 0.19f, b.top + earH * 0.40f)
            earPath.lineTo(bodyX + bodyW * 0.35f, bodyY + bodyH * 0.12f)
            earPath.close()
            canvas.drawPath(earPath, innerEarPaint)

            // Right inner ear
            earPath.reset()
            earPath.moveTo(bodyX + bodyW * 0.86f, bodyY + bodyH * 0.25f)
            earPath.lineTo(bodyX + bodyW * 0.81f, b.top + earH * 0.40f)
            earPath.lineTo(bodyX + bodyW * 0.65f, bodyY + bodyH * 0.12f)
            earPath.close()
            canvas.drawPath(earPath, innerEarPaint)

            // Body round rect drawn at bodyX, bodyY, bodyX + bodyW, bodyY + bodyH with radius in bodyPaint
            bodyRect.set(bodyX, bodyY, bodyX + bodyW, bodyY + bodyH)
            canvas.drawRoundRect(bodyRect, radius, radius, bodyPaint)
        }

        override fun setAlpha(alpha: Int) {
            bodyPaint.alpha = alpha
            earPaint.alpha = alpha
            innerEarPaint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            bodyPaint.colorFilter = colorFilter
            earPaint.colorFilter = colorFilter
            innerEarPaint.colorFilter = colorFilter
        }

        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    }

    // ------------------------------------------------------------------------
    // 3. Text-Free Coquette Bow Spacebar Drawable (Blush + Dotted Rim + Bow)
    // ------------------------------------------------------------------------
    class CoquetteSpacebarDrawable(
        private val baseColor: Int,
        private val pressedColor: Int,
        private val cornerRadius: Float,
        private val borderColor: Int?,
        private val borderWidth: Int
    ) : Drawable() {
        private var isPressedState = false
        private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = DARK_INK
            strokeWidth = 2.1f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            pathEffect = DashPathEffect(floatArrayOf(2.5f, 6.0f), 0f)
        }
        private val bodyRect = RectF()
        private val rimRect = RectF()

        fun setPressed(pressed: Boolean) {
            if (isPressedState != pressed) {
                isPressedState = pressed
                invalidateSelf()
            }
        }

        override fun draw(canvas: Canvas) {
            val b = bounds
            val w = b.width().toFloat()
            val h = b.height().toFloat()
            if (w <= 0f || h <= 0f) return

            bodyPaint.color = if (isPressedState) pressedColor else baseColor
            bodyRect.set(b)
            canvas.drawRoundRect(bodyRect, cornerRadius, cornerRadius, bodyPaint)

            // Inner Dark-Berry Dotted Rim
            val rimInset = 4.0f
            rimRect.set(b.left + rimInset, b.top + rimInset, b.right - rimInset, b.bottom - rimInset)
            val rimRadius = (h - rimInset * 2f) * 0.32f
            canvas.drawRoundRect(rimRect, rimRadius, rimRadius, rimPaint)

            val cx = b.centerX().toFloat()
            val cy = b.centerY().toFloat()
            val s = (h / 46f).coerceIn(0.75f, 1.4f)
            drawCoquetteSpacebarVisuals(canvas, cx, cy, s)
        }

        override fun setAlpha(alpha: Int) {
            bodyPaint.alpha = alpha
            rimPaint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            bodyPaint.colorFilter = colorFilter
            rimPaint.colorFilter = colorFilter
        }

        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    }

    fun getKeyBackground(key: Key, isPressed: Boolean, cornerRadius: Float): Drawable {
        val code = key.code
        val rawLabel = key.label?.trim()

        // 1. ?123 / ABC / Bengali Numeric Mode: Rectangular Cat-Ear Keycap
        if (code == Constants.CODE_SWITCH_ALPHA_SYMBOL || code == Constants.CODE_SWITCH_BENGALI_NUM ||
            rawLabel == "?123" || rawLabel == "123" || rawLabel == "১২৩" || rawLabel?.lowercase() == "abc") {
            val d = CatEarKeycapDrawable(
                baseColor = LAVENDER,
                pressedColor = LAVENDER_PRESSED,
                cornerRadius = cornerRadius,
                isOval = false,
                innerEarColor = WARM_CREAM
            )
            d.setPressed(isPressed)
            return d
        }

        // 2. Emoji Key: Oval Cat-Ear Pink Cap
        if (code == Constants.CODE_EMOJI || code == Constants.CODE_SHORTCUT) {
            val d = CatEarKeycapDrawable(
                baseColor = BLUSH_PINK,
                pressedColor = BLUSH_PINK_PRESSED,
                cornerRadius = cornerRadius,
                isOval = true,
                innerEarColor = WARM_CREAM
            )
            d.setPressed(isPressed)
            return d
        }

        // 3. Enter Key: Oval Cat-Ear Berry Rose Cap
        if (code == Constants.CODE_ENTER) {
            val d = CatEarKeycapDrawable(
                baseColor = BERRY_ROSE,
                pressedColor = BERRY_ROSE_PRESSED,
                cornerRadius = cornerRadius,
                isOval = true,
                innerEarColor = BUBBLEGUM_PINK
            )
            d.setPressed(isPressed)
            return d
        }

        // 4. Spacebar Key: Blush Pink with Inner Dotted Rim and Coquette Bow (Text-Free)
        if (code == Constants.CODE_SPACE) {
            val defaultSpaceRadius = if (spacebarShape == "roundy") 48f * (cornerRadius / 8f).coerceAtLeast(1f) else cornerRadius
            val spaceRadius = customSpacebarCornerRadius ?: defaultSpaceRadius
            val baseColor = customSpacebarColor ?: BLUSH_PINK
            val pressedColor = if (isPressed) {
                if (customSpacebarColor != null) 0x66FFFFFF.toInt() else BLUSH_PINK_PRESSED
            } else baseColor

            if (customSpacebarColor == null) {
                val d = CoquetteSpacebarDrawable(
                    baseColor = baseColor,
                    pressedColor = pressedColor,
                    cornerRadius = spaceRadius,
                    borderColor = customSpacebarBorderColor,
                    borderWidth = customSpacebarBorderWidth
                )
                d.setPressed(isPressed)
                return d
            } else {
                return GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    this.cornerRadius = spaceRadius
                    setColor(pressedColor)
                    if (customSpacebarBorderColor != null && customSpacebarBorderWidth > 0) {
                        setStroke(customSpacebarBorderWidth, customSpacebarBorderColor!!)
                    }
                }
            }
        }

        // 5. Standard Keys
        val baseColor = getKeyBaseColor(key)
        val pressedColor = when (baseColor) {
            BLUSH_PINK -> BLUSH_PINK_PRESSED
            BUBBLEGUM_PINK -> BUBBLEGUM_PINK_PRESSED
            LAVENDER -> LAVENDER_PRESSED
            ROSE_MAUVE -> ROSE_MAUVE_PRESSED
            WARM_CREAM -> WARM_CREAM_PRESSED
            HOT_PINK -> HOT_PINK_PRESSED
            BERRY_ROSE -> BERRY_ROSE_PRESSED
            else -> 0x44FFFFFF.toInt()
        }
        val currentColor = if (isPressed) pressedColor else baseColor

        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            this.cornerRadius = cornerRadius
            setColor(currentColor)
        }
    }

    fun isAlphabetShift(key: Key): Boolean {
        if (key.code != Constants.CODE_SHIFT) return false
        val label = key.label?.trim() ?: ""
        return label.isEmpty() || label == "⇧" || label == "↑"
    }

    /**
     * Key label override:
     * - Returns empty string for functional keys (Shift, Enter, Emoji, Delete, Space)
     *   and locked cute glyphs ('q', 'i', 'o', 'g') so they are drawn 100% on Canvas.
     * - Letter keys return originalLabel without emoji strings.
     */
    fun getKeyLabelOverride(key: Key, originalLabel: String): String {
        val trimmed = originalLabel.trim()
        val code = key.code

        if (code == Constants.CODE_EMOJI || code == Constants.CODE_SHORTCUT) return ""
        if (code == Constants.CODE_ENTER) return ""
        if (code == Constants.CODE_DELETE) return ""
        if (isAlphabetShift(key)) return ""
        if (code == Constants.CODE_SPACE) return ""

        val lower = trimmed.lowercase(java.util.Locale.ROOT)
        // Permanently locked kawaii glyphs ('q', 'i', 'o', 'g') return empty so they are rendered custom on Canvas
        if (lower == "q" || lower == "i" || lower == "o" || lower == "g") return ""

        // Strip gear icon from period key if present
        if (lower.startsWith(".") && (lower.contains("⚙") || lower.contains("\u2699"))) return "."

        return originalLabel
    }

    /**
     * Handcrafted Canvas vector rendering for Berry Board functional keys & locked glyphs.
     */
    fun onDrawCustomKeyTopVisuals(
        key: Key,
        canvas: Canvas,
        paint: Paint,
        keyWidth: Int,
        keyHeight: Int,
        animAlpha: Int
    ): Boolean {
        val code = key.code
        val rawLabel = key.label?.trim()?.lowercase(java.util.Locale.ROOT) ?: ""

        val cx = keyWidth * 0.5f
        val cy = keyHeight * 0.5f
        val s = (minOf(keyWidth, keyHeight) / 50f).coerceIn(0.6f, 1.4f)

        when {
            isAlphabetShift(key) -> {
                drawBowTie(canvas, cx, cy, s)
                return true
            }
            code == Constants.CODE_DELETE -> {
                drawKittyPawBackspace(canvas, keyWidth, keyHeight)
                return true
            }
            code == Constants.CODE_EMOJI || code == Constants.CODE_SHORTCUT -> {
                val catFaceCy = (keyHeight * 0.16f) + (keyHeight * 0.84f) * 0.54f
                drawSmilingCatFace(canvas, cx, catFaceCy, s)
                return true
            }
            code == Constants.CODE_ENTER -> {
                val catFaceCy = (keyHeight * 0.16f) + (keyHeight * 0.84f) * 0.54f
                drawWinkingCatEnter(canvas, cx, catFaceCy, s)
                return true
            }
            code == Constants.CODE_SPACE -> {
                drawCoquetteSpacebarVisuals(canvas, cx, cy, s)
                return true
            }
            rawLabel == "o" -> {
                val box = RectF(0f, 0f, keyWidth.toFloat(), keyHeight.toFloat())
                BerrySpriteMatrices.drawSpriteCentered(canvas, box, BerrySpriteMatrices.GLYPH_HEART_O, 0.44f, DARK_INK, paint)
                return true
            }
            rawLabel == "i" -> {
                val box = RectF(0f, 0f, keyWidth.toFloat(), keyHeight.toFloat())
                BerrySpriteMatrices.drawSpriteCentered(canvas, box, BerrySpriteMatrices.GLYPH_HEART_I, 0.54f, DARK_INK, paint)
                return true
            }
            rawLabel == "q" -> {
                val box = RectF(0f, 0f, keyWidth.toFloat(), keyHeight.toFloat())
                BerrySpriteMatrices.drawSpriteCentered(canvas, box, BerrySpriteMatrices.GLYPH_CUTE_Q, 0.52f, DARK_INK, paint)
                return true
            }
            rawLabel == "g" -> {
                val box = RectF(0f, 0f, keyWidth.toFloat(), keyHeight.toFloat())
                BerrySpriteMatrices.drawSpriteCentered(canvas, box, BerrySpriteMatrices.GLYPH_CUTE_G, 0.52f, DARK_INK, paint)
                return true
            }
            else -> return false
        }
    }

    // ------------------------------------------------------------------------
    // 4. Illustrated Kitty-Paw Backspace Key
    // ------------------------------------------------------------------------
    private fun drawKittyPawBackspace(canvas: Canvas, keyWidth: Int, keyHeight: Int) {
        val w = keyWidth.toFloat()
        val h = keyHeight.toFloat()
        val cy = h * 0.52f
        val pawLeft = w * 0.22f
        val pawRight = w * 1.04f
        val pawH = h * 0.44f

        // Kitty Arm + Rounded Paw Silhouette
        val armPath = Path().apply {
            moveTo(pawRight, cy - pawH * 0.42f)
            lineTo(pawLeft + pawH * 0.55f, cy - pawH * 0.46f)
            arcTo(
                RectF(pawLeft, cy - pawH * 0.48f, pawLeft + pawH * 1.1f, cy + pawH * 0.48f),
                -90f, -180f, false
            )
            lineTo(pawRight, cy + pawH * 0.42f)
            close()
        }
        val fillArmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = BLUSH_PINK
        }
        val strokeArmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = DARK_INK
            strokeWidth = (h * 0.038f).coerceAtLeast(1.8f)
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        canvas.drawPath(armPath, fillArmPaint)
        canvas.drawPath(armPath, strokeArmPaint)

        // Tabby Stripes on the Arm
        val stripePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = PAW_STRIPE
        }
        val stripeW = w * 0.065f
        canvas.drawRect(w * 0.64f, cy - pawH * 0.40f, w * 0.64f + stripeW, cy - pawH * 0.14f, stripePaint)
        canvas.drawRect(w * 0.78f, cy - pawH * 0.40f, w * 0.78f + stripeW, cy - pawH * 0.10f, stripePaint)

        // Center Heart-Shaped Paw Pad
        val padCx = pawLeft + pawH * 0.62f
        val padPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = BOW_FILL
        }
        drawFilledHeart(canvas, padCx, cy, pawH * 0.30f, padPaint)
        drawHeartOutline(canvas, padCx, cy, pawH * 0.30f, strokeArmPaint, 1f)

        // 4 Little Pink Toe Beans
        val beanOffsets = arrayOf(
            floatArrayOf(-0.26f, -0.24f),
            floatArrayOf(-0.34f, -0.08f),
            floatArrayOf(-0.34f, 0.08f),
            floatArrayOf(-0.26f, 0.24f)
        )
        val beanR = pawH * 0.085f
        for (b in beanOffsets) {
            val bx = padCx + b[0] * pawH * 0.9f
            val by = cy + b[1] * pawH * 0.9f
            canvas.drawCircle(bx, by, beanR, padPaint)
            canvas.drawCircle(bx, by, beanR, strokeArmPaint)
        }
    }

    // ------------------------------------------------------------------------
    // 5. Coquette Bow & Flanking Visuals on Spacebar (Zero allocations in draw loop)
    // ------------------------------------------------------------------------
    private val knotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = 0xFFFFD6E4.toInt()
        strokeCap = Paint.Cap.ROUND
    }
    private val whiteSparklePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }
    private val filledHeartPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = BOW_FILL
    }
    private val strokeHeartPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = DARK_INK
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val miniBowFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val miniBowStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val miniBowLeftLoop = Path()
    private val miniBowRightLoop = Path()
    private val miniBowLeftTail = Path()
    private val miniBowRightTail = Path()
    private val heartOutlinePath = Path()
    private val filledHeartPath = Path()
    private val sparkle4Path = Path()

    fun drawCoquetteSpacebarVisuals(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        val bs = s * 1.45f
        val bowSize = 24f * bs

        // 1. Center Beaded Pink Ribbon Bow
        drawMiniRibbonBow(canvas, cx, cy - bowSize * 0.06f, bowSize, BOW_FILL, DARK_INK)

        // Cute tiny cross '+' knot highlight in #FFD6E4
        knotPaint.strokeWidth = 1.6f * bs
        canvas.drawLine(cx - 3.0f * bs, cy - bowSize * 0.04f, cx + 3.0f * bs, cy - bowSize * 0.04f, knotPaint)
        canvas.drawLine(cx, cy - bowSize * 0.04f - 3.0f * bs, cx, cy - bowSize * 0.04f + 3.0f * bs, knotPaint)

        // 2. Flanking White Sparkles + Filled Pink Hearts + Hollow Hearts
        val spOffset = bowSize * 0.76f
        val heart1Offset = bowSize * 1.20f
        val heart2Offset = bowSize * 1.62f

        strokeHeartPaint.strokeWidth = 1.4f * bs

        // Flanking 4-point white sparkles
        draw4PointSparkle(canvas, cx - spOffset, cy, 5.0f * bs, whiteSparklePaint)
        draw4PointSparkle(canvas, cx + spOffset, cy, 5.0f * bs, whiteSparklePaint)

        // Flanking filled hearts
        drawFilledHeart(canvas, cx - heart1Offset, cy, 4.2f * bs, filledHeartPaint)
        drawHeartOutline(canvas, cx - heart1Offset, cy, 4.2f * bs, strokeHeartPaint, bs)
        drawFilledHeart(canvas, cx + heart1Offset, cy, 4.2f * bs, filledHeartPaint)
        drawHeartOutline(canvas, cx + heart1Offset, cy, 4.2f * bs, strokeHeartPaint, bs)

        // Flanking hollow hearts
        drawHeartOutline(canvas, cx - heart2Offset, cy + 1f * bs, 3.5f * bs, strokeHeartPaint, bs)
        drawHeartOutline(canvas, cx + heart2Offset, cy + 1f * bs, 3.5f * bs, strokeHeartPaint, bs)
    }

    private fun drawMiniRibbonBow(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        size: Float,
        fillColor: Int,
        strokeColor: Int
    ) {
        val s = size / 26f
        miniBowFillPaint.color = fillColor
        miniBowStrokePaint.color = strokeColor
        miniBowStrokePaint.strokeWidth = 1.6f * s

        // Left Loop
        miniBowLeftLoop.reset()
        miniBowLeftLoop.moveTo(cx - 2f * s, cy)
        miniBowLeftLoop.cubicTo(cx - 7f * s, cy - 6f * s, cx - 14f * s, cy - 6f * s, cx - 13f * s, cy)
        miniBowLeftLoop.cubicTo(cx - 12f * s, cy + 6f * s, cx - 7f * s, cy + 5f * s, cx - 2f * s, cy)
        miniBowLeftLoop.close()
        canvas.drawPath(miniBowLeftLoop, miniBowFillPaint)
        canvas.drawPath(miniBowLeftLoop, miniBowStrokePaint)

        // Right Loop
        miniBowRightLoop.reset()
        miniBowRightLoop.moveTo(cx + 2f * s, cy)
        miniBowRightLoop.cubicTo(cx + 7f * s, cy - 6f * s, cx + 14f * s, cy - 6f * s, cx + 13f * s, cy)
        miniBowRightLoop.cubicTo(cx + 12f * s, cy + 6f * s, cx + 7f * s, cy + 5f * s, cx + 2f * s, cy)
        miniBowRightLoop.close()
        canvas.drawPath(miniBowRightLoop, miniBowFillPaint)
        canvas.drawPath(miniBowRightLoop, miniBowStrokePaint)

        // Dangling Tails
        miniBowLeftTail.reset()
        miniBowLeftTail.moveTo(cx - 1f * s, cy + 2f * s)
        miniBowLeftTail.quadTo(cx - 5f * s, cy + 7f * s, cx - 9f * s, cy + 11f * s)

        miniBowRightTail.reset()
        miniBowRightTail.moveTo(cx + 1f * s, cy + 2f * s)
        miniBowRightTail.quadTo(cx + 5f * s, cy + 7f * s, cx + 9f * s, cy + 11f * s)

        canvas.drawPath(miniBowLeftTail, miniBowStrokePaint)
        canvas.drawPath(miniBowRightTail, miniBowStrokePaint)

        // Center Knot
        canvas.drawCircle(cx, cy, 3.2f * s, miniBowFillPaint)
        canvas.drawCircle(cx, cy, 3.2f * s, miniBowStrokePaint)
    }

    private fun drawHeartOutline(canvas: Canvas, cx: Float, cy: Float, size: Float, paint: Paint, s: Float) {
        heartOutlinePath.reset()
        heartOutlinePath.moveTo(cx, cy + size * 0.7f)
        heartOutlinePath.cubicTo(cx - size * 1.15f, cy + size * 0.15f, cx - size * 1.15f, cy - size * 0.75f, cx, cy - size * 0.25f)
        heartOutlinePath.cubicTo(cx + size * 1.15f, cy - size * 0.75f, cx + size * 1.15f, cy + size * 0.15f, cx, cy + size * 0.7f)
        heartOutlinePath.close()
        canvas.drawPath(heartOutlinePath, paint)
    }

    private fun drawBowTie(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        val bs = s * 1.35f
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = LIGHT_INK
        }
        val dotR = 1.6f * bs

        // Center knot (cluster of 5 dots)
        canvas.drawCircle(cx, cy, dotR * 1.5f, dotPaint)
        canvas.drawCircle(cx - 2.5f * bs, cy, dotR, dotPaint)
        canvas.drawCircle(cx + 2.5f * bs, cy, dotR, dotPaint)
        canvas.drawCircle(cx, cy - 2.5f * bs, dotR, dotPaint)
        canvas.drawCircle(cx, cy + 2.5f * bs, dotR, dotPaint)

        // Left loop dots
        val leftLoop = arrayOf(
            floatArrayOf(-5f, -3f), floatArrayOf(-8f, -6f), floatArrayOf(-12f, -8f),
            floatArrayOf(-16f, -7f), floatArrayOf(-19f, -4f), floatArrayOf(-20f, 0f),
            floatArrayOf(-19f, 4f), floatArrayOf(-16f, 7f), floatArrayOf(-12f, 8f),
            floatArrayOf(-8f, 6f), floatArrayOf(-5f, 3f),
            floatArrayOf(-12f, 0f)
        )
        for (pt in leftLoop) {
            canvas.drawCircle(cx + pt[0] * bs, cy + pt[1] * bs, dotR, dotPaint)
        }

        // Right loop dots
        val rightLoop = arrayOf(
            floatArrayOf(5f, -3f), floatArrayOf(8f, -6f), floatArrayOf(12f, -8f),
            floatArrayOf(16f, -7f), floatArrayOf(19f, -4f), floatArrayOf(20f, 0f),
            floatArrayOf(19f, 4f), floatArrayOf(16f, 7f), floatArrayOf(12f, 8f),
            floatArrayOf(8f, 6f), floatArrayOf(5f, 3f),
            floatArrayOf(12f, 0f)
        )
        for (pt in rightLoop) {
            canvas.drawCircle(cx + pt[0] * bs, cy + pt[1] * bs, dotR, dotPaint)
        }

        // Dangling ribbon tails with fishtail notches
        val leftTail = arrayOf(
            floatArrayOf(-4f, 6f), floatArrayOf(-7f, 10f), floatArrayOf(-10f, 14f),
            floatArrayOf(-14f, 18f), floatArrayOf(-18f, 20f),
            floatArrayOf(-14f, 21f), floatArrayOf(-12f, 17f), floatArrayOf(-8f, 13f)
        )
        for (pt in leftTail) {
            canvas.drawCircle(cx + pt[0] * bs, cy + pt[1] * bs, dotR, dotPaint)
        }

        val rightTail = arrayOf(
            floatArrayOf(4f, 6f), floatArrayOf(7f, 10f), floatArrayOf(10f, 14f),
            floatArrayOf(14f, 18f), floatArrayOf(18f, 20f),
            floatArrayOf(14f, 21f), floatArrayOf(12f, 17f), floatArrayOf(8f, 13f)
        )
        for (pt in rightTail) {
            canvas.drawCircle(cx + pt[0] * bs, cy + pt[1] * bs, dotR, dotPaint)
        }
    }

    private fun drawSmilingCatFace(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = DARK_INK
            strokeWidth = 1.9f * s
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = DARK_INK
        }

        // Smiling eyes: two upward curved arcs (^ ^)
        val leftEye = Path().apply {
            moveTo(cx - 10f * s, cy - 2.5f * s)
            quadTo(cx - 7.5f * s, cy - 7f * s, cx - 5f * s, cy - 2.5f * s)
        }
        canvas.drawPath(leftEye, strokePaint)

        val rightEye = Path().apply {
            moveTo(cx + 5f * s, cy - 2.5f * s)
            quadTo(cx + 7.5f * s, cy - 7f * s, cx + 10f * s, cy - 2.5f * s)
        }
        canvas.drawPath(rightEye, strokePaint)

        // Cute mini nose dot
        canvas.drawCircle(cx, cy + 0.5f * s, 1.2f * s, fillPaint)

        // Cute :3 cat mouth
        val mouth = Path().apply {
            moveTo(cx - 4.5f * s, cy + 2.5f * s)
            quadTo(cx - 2.2f * s, cy + 5.5f * s, cx, cy + 3.2f * s)
            quadTo(cx + 2.2f * s, cy + 5.5f * s, cx + 4.5f * s, cy + 2.5f * s)
        }
        canvas.drawPath(mouth, strokePaint)

        // Whiskers on cheeks
        canvas.drawLine(cx - 10.5f * s, cy + 1f * s, cx - 18f * s, cy + 0.5f * s, strokePaint)
        canvas.drawLine(cx - 10.5f * s, cy + 4.5f * s, cx - 17.5f * s, cy + 6f * s, strokePaint)
        canvas.drawLine(cx + 10.5f * s, cy + 1f * s, cx + 18f * s, cy + 0.5f * s, strokePaint)
        canvas.drawLine(cx + 10.5f * s, cy + 4.5f * s, cx + 17.5f * s, cy + 6f * s, strokePaint)
    }

    private fun drawWinkingCatEnter(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = LIGHT_INK
            strokeWidth = 2.0f * s
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = LIGHT_INK
        }

        // Left eye: open round circle
        canvas.drawCircle(cx - 9f * s, cy - 4.5f * s, 2.6f * s, fillPaint)

        // Right eye: playful wink (^)
        val wink = Path().apply {
            moveTo(cx + 5.5f * s, cy - 3f * s)
            quadTo(cx + 8.5f * s, cy - 7f * s, cx + 11.5f * s, cy - 3f * s)
        }
        canvas.drawPath(wink, strokePaint)

        // Whiskers
        canvas.drawLine(cx - 12f * s, cy - 1f * s, cx - 19f * s, cy - 1.5f * s, strokePaint)
        canvas.drawLine(cx - 12f * s, cy + 2f * s, cx - 18.5f * s, cy + 3.5f * s, strokePaint)
        canvas.drawLine(cx + 12f * s, cy - 1f * s, cx + 19f * s, cy - 1.5f * s, strokePaint)
        canvas.drawLine(cx + 12f * s, cy + 2f * s, cx + 18.5f * s, cy + 3.5f * s, strokePaint)

        // Return arrow mouth (↵)
        val mouthArrow = Path().apply {
            moveTo(cx + 8f * s, cy + 3f * s)
            lineTo(cx + 8f * s, cy + 7f * s)
            quadTo(cx + 8f * s, cy + 9.5f * s, cx + 5.5f * s, cy + 9.5f * s)
            lineTo(cx - 7f * s, cy + 9.5f * s)
        }
        canvas.drawPath(mouthArrow, strokePaint)

        val barb = Path().apply {
            moveTo(cx - 2.5f * s, cy + 5f * s)
            lineTo(cx - 7f * s, cy + 9.5f * s)
            lineTo(cx - 2.5f * s, cy + 14f * s)
        }
        canvas.drawPath(barb, strokePaint)
    }

    // ------------------------------------------------------------------------
    // 6. Letter Key Doodles (Post Draw Visuals)
    // ------------------------------------------------------------------------
    fun onPostDrawKeyVisuals(
        key: Key,
        canvas: Canvas,
        paint: Paint,
        keyWidth: Int,
        keyHeight: Int,
        labelX: Float,
        labelBaseline: Float
    ) {
        val label = key.label?.trim()?.lowercase(java.util.Locale.ROOT) ?: return
        val s = (minOf(keyWidth, keyHeight) / 50f).coerceIn(0.6f, 1.4f)
        val doodlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = DARK_INK
        }
        val strokeDoodlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = DARK_INK
            strokeWidth = 1.3f * s
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        when (label) {
            "w" -> {
                drawMiniRibbonBow(canvas, keyWidth * 0.50f, keyHeight * 0.78f, 18f * s, BOW_FILL, DARK_INK)
            }
            "a" -> {
                drawMiniRibbonBow(canvas, keyWidth * 0.30f, keyHeight * 0.76f, 16f * s, BOW_FILL, DARK_INK)
            }
            "c" -> {
                drawCatPaw(canvas, keyWidth * 0.22f, keyHeight * 0.74f, s, doodlePaint)
            }
            "n" -> {
                drawCherries(canvas, keyWidth * 0.76f, keyHeight * 0.22f, s * 0.85f, doodlePaint, strokeDoodlePaint)
            }
            "m" -> {
                drawCherries(canvas, keyWidth * 0.80f, keyHeight * 0.76f, s * 0.75f, doodlePaint, strokeDoodlePaint)
            }
            "y", "p" -> {
                draw4PointSparkle(canvas, keyWidth * 0.80f, keyHeight * 0.74f, 4.5f * s, doodlePaint)
            }
        }
    }

    private fun drawFilledHeart(canvas: Canvas, cx: Float, cy: Float, size: Float, paint: Paint) {
        filledHeartPath.reset()
        filledHeartPath.moveTo(cx, cy + size * 0.7f)
        filledHeartPath.cubicTo(cx - size * 1.1f, cy + size * 0.1f, cx - size * 1.1f, cy - size * 0.7f, cx, cy - size * 0.2f)
        filledHeartPath.cubicTo(cx + size * 1.1f, cy - size * 0.7f, cx + size * 1.1f, cy + size * 0.1f, cx, cy + size * 0.7f)
        filledHeartPath.close()
        canvas.drawPath(filledHeartPath, paint)
    }

    private fun drawCatPaw(canvas: Canvas, cx: Float, cy: Float, s: Float, paint: Paint) {
        val padRect = RectF(cx - 3.8f * s, cy - 2.8f * s, cx + 3.8f * s, cy + 2.8f * s)
        canvas.drawOval(padRect, paint)

        val r = 1.1f * s
        canvas.drawCircle(cx - 4.2f * s, cy - 4.8f * s, r * 0.9f, paint)
        canvas.drawCircle(cx - 1.6f * s, cy - 6.2f * s, r, paint)
        canvas.drawCircle(cx + 1.6f * s, cy - 6.2f * s, r, paint)
        canvas.drawCircle(cx + 4.2f * s, cy - 4.8f * s, r * 0.9f, paint)
    }

    private fun drawCherries(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        s: Float,
        fillPaint: Paint,
        strokePaint: Paint
    ) {
        val cherryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = CHERRY_RED
        }
        val r = 2.4f * s
        canvas.drawCircle(cx, cy, r, cherryPaint)
        canvas.drawCircle(cx + 6f * s, cy - 1f * s, r, cherryPaint)

        val apexX = cx + 3.5f * s
        val apexY = cy - 7f * s

        val stem1 = Path().apply {
            moveTo(cx, cy - r)
            quadTo(cx + 1f * s, cy - 5f * s, apexX, apexY)
        }
        val stem2 = Path().apply {
            moveTo(cx + 6f * s, cy - 1f * s - r)
            quadTo(cx + 5f * s, cy - 5f * s, apexX, apexY)
        }
        canvas.drawPath(stem1, strokePaint)
        canvas.drawPath(stem2, strokePaint)

        val leafPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0xFF5D9655.toInt()
        }
        val leaf = Path().apply {
            moveTo(apexX, apexY)
            quadTo(apexX + 3f * s, apexY - 3f * s, apexX + 5f * s, apexY - 1.5f * s)
            quadTo(apexX + 3f * s, apexY, apexX, apexY)
            close()
        }
        canvas.drawPath(leaf, leafPaint)
    }

    private fun draw4PointSparkle(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint) {
        sparkle4Path.reset()
        val c = radius * 0.22f
        sparkle4Path.moveTo(cx, cy - radius)
        sparkle4Path.quadTo(cx + c, cy - c, cx + radius, cy)
        sparkle4Path.quadTo(cx + c, cy + c, cx, cy + radius)
        sparkle4Path.quadTo(cx - c, cy + c, cx - radius, cy)
        sparkle4Path.quadTo(cx - c, cy - c, cx, cy - radius)
        sparkle4Path.close()
        canvas.drawPath(sparkle4Path, paint)
    }

    fun getKeyHintCoordinates(key: Key, keyWidth: Int, keyHeight: Int, padding: Float): FloatArray? {
        val hintLabel = key.hintLabel
        if (hintLabel.isNullOrEmpty()) return null

        val padX = maxOf(padding, keyWidth * 0.08f)
        val padY = maxOf(padding, keyHeight * 0.08f)
        val hintX = keyWidth - padX
        val hintBaseline = padY + 11f
        return floatArrayOf(hintX, hintBaseline)
    }

    fun getKeyHintCoordinates(key: Key, keyWidth: Int, keyHeight: Int, padding: Float, paint: Paint): FloatArray? {
        val hintLabel = key.hintLabel
        if (hintLabel.isNullOrEmpty()) return null

        val padX = maxOf(padding, keyWidth * 0.10f)
        val padY = maxOf(padding, keyHeight * 0.10f)
        val hintLabelWidth = paint.measureText(hintLabel)
        val hintX = keyWidth - padX - hintLabelWidth / 2.0f
        val hintBaseline = -paint.ascent() + padY
        return floatArrayOf(hintX, hintBaseline)
    }
}
