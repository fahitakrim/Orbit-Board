package org.futo.inputmethod.latin.uix.theme.presets

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import org.futo.inputmethod.keyboard.Key
import org.futo.inputmethod.latin.common.Constants
import java.util.Locale

/**
 * Tactile Slate Theme Helper (BlackBerry / Mechanical):
 * - Metallic silver / light gray chassis (#D0D2D6)
 * - Tactile sculpted crisp white keycaps (#FFFFFF) with 3D drop-shadow bevel on bottom edge
 * - Dark charcoal modifier keys (#404040) for Shift, 12!?, Emoji, Enter
 * - Backspace / Delete key: Nothing Red (#D71921) with white delete icon ("just backspace ta red hobe")
 * - Crisp uppercase sans-serif letters (Q W E R T Y...)
 */
object TactileSlateThemeHelper {
    const val COLOR_CHASSIS = 0xFFD0D2D6.toInt()         // Metallic silver chassis
    const val COLOR_WHITE_KEY = 0xFFFFFFFF.toInt()       // Sculpted white keycap
    const val COLOR_WHITE_BEVEL = 0xFFB4B7BD.toInt()     // 3D bottom drop shadow bevel
    const val COLOR_WHITE_PRESSED = 0xFFE6E7EA.toInt()

    const val COLOR_MODIFIER = 0xFF404040.toInt()        // Dark charcoal modifier
    const val COLOR_MODIFIER_BEVEL = 0xFF262626.toInt()  // 3D bevel for modifier
    const val COLOR_MODIFIER_PRESSED = 0xFF303030.toInt()

    const val COLOR_RED_BACKSPACE = 0xFFD71921.toInt()   // Nothing Red ("just backspace ta red hobe")
    const val COLOR_RED_BEVEL = 0xFF9E1218.toInt()       // 3D bevel for backspace
    const val COLOR_RED_PRESSED = 0xFFBA141B.toInt()

    const val COLOR_TEXT_DARK = 0xFF1C1D20.toInt()       // Dark charcoal text on white keys
    const val COLOR_TEXT_LIGHT = 0xFFFFFFFF.toInt()      // Pure white text on modifiers & backspace

    // Spacebar customization properties
    var customSpacebarColor: Int? = null
    var customSpacebarBorderColor: Int? = null
    var customSpacebarBorderWidth: Int = 0
    var customSpacebarTextColor: Int? = null
    var customSpacebarCornerRadius: Float? = null
    var spacebarShape: String = "boxy"

    fun isModifierKey(key: Key): Boolean {
        val code = key.code
        val label = key.label?.trim()
        return code == Constants.CODE_SHIFT ||
                code == Constants.CODE_ENTER ||
                code == Constants.CODE_EMOJI ||
                code == Constants.CODE_SHORTCUT ||
                code == Constants.CODE_SWITCH_ALPHA_SYMBOL ||
                code == Constants.CODE_SWITCH_BENGALI_NUM ||
                label == "12!?" || label == "?123" || label == "123" || label == "১২৩"
    }

    fun isBackspaceKey(key: Key): Boolean {
        return key.code == Constants.CODE_DELETE
    }

    fun getKeyBaseColor(key: Key): Int {
        val code = key.code
        if (code == Constants.CODE_DELETE) return COLOR_RED_BACKSPACE
        if (code == Constants.CODE_SPACE) return customSpacebarColor ?: COLOR_WHITE_KEY
        if (isModifierKey(key)) return COLOR_MODIFIER
        return COLOR_WHITE_KEY
    }

    fun getKeyTextColor(key: Key): Int {
        if (key.code == Constants.CODE_SPACE) {
            return customSpacebarTextColor ?: COLOR_TEXT_DARK
        }
        val code = key.code
        if (code == Constants.CODE_DELETE || isModifierKey(key)) {
            return COLOR_TEXT_LIGHT
        }
        return COLOR_TEXT_DARK
    }

    fun getKeyHintColor(key: Key): Int {
        if (key.code == Constants.CODE_SPACE) {
            return customSpacebarTextColor ?: COLOR_TEXT_DARK
        }
        if (key.code == Constants.CODE_DELETE || isModifierKey(key)) {
            return 0x99FFFFFF.toInt()
        }
        return 0x88000000.toInt()
    }

    /**
     * Tactile 3D Keycap Drawable:
     * High-precision hardware accelerated 3D drop-shadow bevel effect simulating
     * physical sculpted chiclet / mechanical key switch action.
     */
    class TactileKeycapDrawable(
        private val baseColor: Int,
        private val bevelColor: Int,
        private val pressedColor: Int,
        private val cornerRadius: Float,
        private val strokeColor: Int? = null,
        private val strokeWidth: Int = 0
    ) : Drawable() {
        private var isPressedState = false
        private val bevelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val facePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
        private val bevelRect = RectF()
        private val faceRect = RectF()

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

            val bevelH = (h * 0.08f).coerceIn(2.5f, 5.5f)

            // 1. Draw bottom 3D bevel / shadow foundation
            bevelPaint.color = bevelColor
            bevelRect.set(b)
            canvas.drawRoundRect(bevelRect, cornerRadius, cornerRadius, bevelPaint)

            // 2. Draw sculpted top key face
            facePaint.color = if (isPressedState) pressedColor else baseColor
            val topOffset = if (isPressedState) bevelH * 0.7f else 0f
            val bottomOffset = if (isPressedState) 0f else bevelH

            faceRect.set(b.left.toFloat(), b.top + topOffset, b.right.toFloat(), b.bottom - bottomOffset)
            canvas.drawRoundRect(faceRect, cornerRadius, cornerRadius, facePaint)

            // 3. Optional custom border / accent stroke
            if (strokeColor != null && strokeWidth > 0) {
                strokePaint.color = strokeColor
                strokePaint.strokeWidth = strokeWidth.toFloat()
                canvas.drawRoundRect(faceRect, cornerRadius, cornerRadius, strokePaint)
            }
        }

        override fun setAlpha(alpha: Int) {
            bevelPaint.alpha = alpha
            facePaint.alpha = alpha
            strokePaint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            bevelPaint.colorFilter = colorFilter
            facePaint.colorFilter = colorFilter
            strokePaint.colorFilter = colorFilter
        }

        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    }

    fun getKeyBackground(key: Key, isPressed: Boolean, cornerRadius: Float): Drawable {
        val code = key.code

        // 1. Backspace: Nothing Red with dark red 3D bevel
        if (code == Constants.CODE_DELETE) {
            val d = TactileKeycapDrawable(
                baseColor = COLOR_RED_BACKSPACE,
                bevelColor = COLOR_RED_BEVEL,
                pressedColor = COLOR_RED_PRESSED,
                cornerRadius = cornerRadius
            )
            d.setPressed(isPressed)
            return d
        }

        // 2. Spacebar: Crisp sculpted white with universal customization support
        if (code == Constants.CODE_SPACE) {
            val defaultSpaceRadius = if (spacebarShape == "roundy") 48f * (cornerRadius / 8f).coerceAtLeast(1f) else cornerRadius
            val spaceRadius = customSpacebarCornerRadius ?: defaultSpaceRadius

            val base = customSpacebarColor ?: COLOR_WHITE_KEY
            val bevel = if (customSpacebarColor != null) {
                if (customSpacebarColor == COLOR_WHITE_KEY) COLOR_WHITE_BEVEL else 0xFF18191B.toInt()
            } else COLOR_WHITE_BEVEL

            val pressed = if (isPressed) {
                if (customSpacebarColor != null) 0x66FFFFFF.toInt() else COLOR_WHITE_PRESSED
            } else base

            val d = TactileKeycapDrawable(
                baseColor = base,
                bevelColor = bevel,
                pressedColor = pressed,
                cornerRadius = spaceRadius,
                strokeColor = customSpacebarBorderColor,
                strokeWidth = customSpacebarBorderWidth
            )
            d.setPressed(isPressed)
            return d
        }

        // 3. Modifier Keys: Dark Charcoal with dark bevel
        if (isModifierKey(key)) {
            val d = TactileKeycapDrawable(
                baseColor = COLOR_MODIFIER,
                bevelColor = COLOR_MODIFIER_BEVEL,
                pressedColor = COLOR_MODIFIER_PRESSED,
                cornerRadius = cornerRadius
            )
            d.setPressed(isPressed)
            return d
        }

        // 4. Regular Letter & Number Keys: Sculpted White with 3D bevel
        val d = TactileKeycapDrawable(
            baseColor = COLOR_WHITE_KEY,
            bevelColor = COLOR_WHITE_BEVEL,
            pressedColor = COLOR_WHITE_PRESSED,
            cornerRadius = cornerRadius
        )
        d.setPressed(isPressed)
        return d
    }

    /**
     * Tactile Slate key label override:
     * - Returns originalLabel for letter keys dynamically (lowercase when Shift is unpressed,
     *   uppercase when Shift/Caps is active - FIXING LOWERCASE BUG!).
     * - Returns empty string for functional keys (Shift, Enter, Emoji, Delete) so they are
     *   rendered with handcrafted Canvas vector geometry (ZERO EMOJI CHARACTERS!).
     * - Returns "12!?" for symbol switch keys.
     */
    fun getKeyLabelOverride(key: Key, originalLabel: String): String {
        val code = key.code
        val trimmed = originalLabel.trim()

        if (code == Constants.CODE_SHIFT) return ""
        if (code == Constants.CODE_ENTER) return ""
        if (code == Constants.CODE_EMOJI || code == Constants.CODE_SHORTCUT) return ""
        if (code == Constants.CODE_DELETE) return ""
        if (code == Constants.CODE_SWITCH_ALPHA_SYMBOL) return "12!?"
        if (trimmed == "?123" || trimmed == "123" || trimmed == "১২৩") return "12!?"

        // FIX LOWERCASE BUG: Do NOT unconditionally uppercase!
        // Return originalLabel directly so the keyboard reflects Shift / CapsLock state dynamically.
        return originalLabel
    }

    /**
     * Handcrafted Canvas vector rendering for Tactile Slate functional keys.
     * ZERO EMOJIS! Everything is drawn with mathematical vector Path / primitives.
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
        val cx = keyWidth * 0.5f
        val cy = keyHeight * 0.5f
        val s = (minOf(keyWidth, keyHeight) / 50f).coerceIn(0.6f, 1.4f)

        when (code) {
            Constants.CODE_EMOJI, Constants.CODE_SHORTCUT -> {
                drawSmilingFace(canvas, cx, cy, s)
                return true
            }
            Constants.CODE_SHIFT -> {
                drawShiftArrow(canvas, cx, cy, s)
                return true
            }
            Constants.CODE_ENTER -> {
                drawEnterArrow(canvas, cx, cy, s)
                return true
            }
            else -> return false
        }
    }

    private fun drawSmilingFace(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.WHITE
            strokeWidth = 1.9f * s
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.WHITE
        }

        // Circular face outline
        canvas.drawCircle(cx, cy, 9.5f * s, strokePaint)

        // Two eye dots
        canvas.drawCircle(cx - 3.5f * s, cy - 2.8f * s, 1.3f * s, fillPaint)
        canvas.drawCircle(cx + 3.5f * s, cy - 2.8f * s, 1.3f * s, fillPaint)

        // Smiling mouth
        val mouthOval = RectF(cx - 5.5f * s, cy - 3f * s, cx + 5.5f * s, cy + 5f * s)
        canvas.drawArc(mouthOval, 30f, 120f, false, strokePaint)
    }

    private fun drawShiftArrow(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.WHITE
        }
        val path = Path().apply {
            moveTo(cx, cy - 8.5f * s)
            lineTo(cx + 7f * s, cy - 1.5f * s)
            lineTo(cx + 3.2f * s, cy - 1.5f * s)
            lineTo(cx + 3.2f * s, cy + 7f * s)
            lineTo(cx - 3.2f * s, cy + 7f * s)
            lineTo(cx - 3.2f * s, cy - 1.5f * s)
            lineTo(cx - 7f * s, cy - 1.5f * s)
            close()
        }
        canvas.drawPath(path, fillPaint)
    }

    private fun drawEnterArrow(canvas: Canvas, cx: Float, cy: Float, s: Float) {
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.WHITE
            strokeWidth = 2.2f * s
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val path = Path().apply {
            moveTo(cx + 7f * s, cy - 4f * s)
            lineTo(cx + 7f * s, cy + 2f * s)
            quadTo(cx + 7f * s, cy + 5f * s, cx + 4f * s, cy + 5f * s)
            lineTo(cx - 6f * s, cy + 5f * s)
        }
        canvas.drawPath(path, strokePaint)

        val barb = Path().apply {
            moveTo(cx - 1.5f * s, cy + 0.5f * s)
            lineTo(cx - 6f * s, cy + 5f * s)
            lineTo(cx - 1.5f * s, cy + 9.5f * s)
        }
        canvas.drawPath(barb, strokePaint)
    }

    fun onPostDrawKeyVisuals(
        key: Key,
        canvas: Canvas,
        paint: Paint,
        keyWidth: Int,
        keyHeight: Int,
        labelX: Float,
        labelBaseline: Float
    ) {
        // Standard clean mechanical presentation for Tactile Slate
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

    fun getKeyPreviewBackground(
        key: Key,
        cornerRadius: Float,
        width: Int,
        height: Int,
        padding: Int
    ): Drawable {
        val color = getKeyBaseColor(key)
        return ShapeDrawable(RoundRectShape(FloatArray(8) { cornerRadius }, null, null)).apply {
            paint.color = color
            intrinsicWidth = width
            intrinsicHeight = height
            setPadding(0, 0, 0, padding)
        }
    }
}
