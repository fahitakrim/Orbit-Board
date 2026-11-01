package org.futo.inputmethod.latin.uix.theme.presets

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import org.futo.inputmethod.keyboard.Key
import org.futo.inputmethod.latin.common.Constants

object BakeryThemeHelper {
    // Bakery & Taiyaki Color Palette
    const val COLOR_BG_CREAM = 0xFFFBF4DE.toInt()         // Soft custard cream background
    const val COLOR_TOP_COFFEE = 0xFF7A5B43.toInt()       // Warm coffee header banner
    const val COLOR_CRUST_BORDER = 0xFF683F1D.toInt()     // Roasted toast crust outline
    const val COLOR_BISCUIT_GOLD = 0xFFFEE8C4.toInt()     // Warm golden biscuit
    const val COLOR_BREAD_CREAM = 0xFFFFF3DC.toInt()      // Toasted bread slice
    const val COLOR_SHIFT_CARD = 0xFFFFF9E6.toInt()       // Pale ivory bakery ticket
    const val COLOR_ENTER_TEAL = 0xFFA4DBE8.toInt()       // Pastel cyan bakery card
    const val COLOR_ENTER_BORDER = 0xFF4C7B87.toInt()     // Slate-teal border for enter
    const val COLOR_ENTER_TEXT = 0xFF204953.toInt()       // Deep teal text for enter
    const val COLOR_DELETE_CRUMB = 0xFFFADBB7.toInt()     // Toasted crumb delete key
    const val COLOR_TEXT_CHOCOLATE = 0xFF3C2415.toInt()   // Deep chocolate brown typography
    const val COLOR_HINT_CHOCOLATE = 0x883C2415.toInt()   // Subtle chocolate for hints
    const val COLOR_PRESSED = 0xFFE8B872.toInt()          // Caramelized honey butter pressed state
    const val COLOR_ENTER_PRESSED = 0xFF88C7D6.toInt()    // Pressed enter key
    const val COLOR_POPUP_BG = 0xFFFFF2D8.toInt()         // Warm butter toast popup bubble

    class BakeryKeyDrawable(
        private val fillColor: Int,
        private val strokeColor: Int,
        private val strokeWidth: Float,
        private val cornerRadius: Float,
        private val isCookie: Boolean = false,
        private val isSpacebar: Boolean = false
    ) : Drawable() {
        private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = fillColor
        }
        private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = this@BakeryKeyDrawable.strokeWidth
            color = strokeColor
        }
        private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0x33683F1D // subtle biscuit detail
        }
        private val slashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth * 0.75f
            color = 0x44683F1D // bread slash marks
            strokeCap = Paint.Cap.ROUND
        }
        private val rectF = RectF()

        override fun draw(canvas: Canvas) {
            val b = bounds
            val insetH = 3.5f
            val insetV = 3.5f
            rectF.set(
                b.left + insetH,
                b.top + insetV,
                b.right - insetH,
                b.bottom - insetV
            )

            // 1. Draw rounded biscuit / bread slice fill
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, fillPaint)

            val w = rectF.width()
            val h = rectF.height()

            // 2. Cookie style: 4 subtle cracker corner dots
            if (isCookie && w > 36f && h > 36f) {
                val dotRadius = strokeWidth * 0.7f
                val padX = w * 0.16f
                val padY = h * 0.18f
                canvas.drawCircle(rectF.left + padX, rectF.top + padY, dotRadius, dotPaint)
                canvas.drawCircle(rectF.right - padX, rectF.top + padY, dotRadius, dotPaint)
                canvas.drawCircle(rectF.left + padX, rectF.bottom - padY, dotRadius, dotPaint)
                canvas.drawCircle(rectF.right - padX, rectF.bottom - padY, dotRadius, dotPaint)
            }

            // 3. Spacebar: subtle bread baker score marks near edges
            if (isSpacebar && w > 120f) {
                val slashLen = h * 0.22f
                val leftX = rectF.left + w * 0.12f
                val rightX = rectF.right - w * 0.12f
                val midY = rectF.centerY()

                // Left baker scores
                canvas.drawLine(leftX - 8f, midY - slashLen, leftX - 4f, midY + slashLen, slashPaint)
                canvas.drawLine(leftX + 8f, midY - slashLen, leftX + 12f, midY + slashLen, slashPaint)

                // Right baker scores
                canvas.drawLine(rightX - 12f, midY - slashLen, rightX - 8f, midY + slashLen, slashPaint)
                canvas.drawLine(rightX + 4f, midY - slashLen, rightX + 8f, midY + slashLen, slashPaint)
            }

            // 4. Crust border outline
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, strokePaint)
        }

        override fun setAlpha(alpha: Int) {
            fillPaint.alpha = alpha
            strokePaint.alpha = alpha
            dotPaint.alpha = (alpha * 0.2f).toInt()
            slashPaint.alpha = (alpha * 0.27f).toInt()
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            fillPaint.colorFilter = colorFilter
            strokePaint.colorFilter = colorFilter
        }

        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    }

    private val drawableCache = HashMap<Int, Drawable>()

    private const val STYLE_COOKIE = 1
    private const val STYLE_BREAD = 2
    private const val STYLE_ENTER = 3
    private const val STYLE_SHIFT = 4
    private const val STYLE_DELETE = 5
    private const val STYLE_SPACE = 6
    private const val STYLE_SYMBOL = 7

    private fun getKeyStyle(key: Key): Int {
        val code = key.code
        val rawLabel = key.label

        if (code == Constants.CODE_ENTER) return STYLE_ENTER
        if (code == Constants.CODE_SHIFT) return STYLE_SHIFT
        if (code == Constants.CODE_DELETE) return STYLE_DELETE
        if (code == Constants.CODE_SPACE) return STYLE_SPACE
        if (code == Constants.CODE_SWITCH_ALPHA_SYMBOL || code == Constants.CODE_SWITCH_BENGALI_NUM) return STYLE_SYMBOL
        if (rawLabel == "১২৩" || rawLabel == "123") return STYLE_SYMBOL

        // Normal letter keys: alternate cookie vs bread pattern
        val x = key.x
        val y = key.y
        val w = if (key.width > 0) key.width else 100
        val h = if (key.height > 0) key.height else 100
        val col = x / w
        val row = y / h

        return if ((col + row) % 2 == 0) STYLE_COOKIE else STYLE_BREAD
    }

    @Synchronized
    fun getKeyBackground(key: Key, isPressed: Boolean, cornerRadius: Float): Drawable {
        val style = getKeyStyle(key)
        val cacheKey = (style * 10) + (if (isPressed) 1 else 0)
        val existing = drawableCache[cacheKey]
        if (existing != null) return existing

        val strokeWidth = 4.2f
        val radius = if (style == STYLE_SPACE) cornerRadius * 1.35f else cornerRadius

        val drawable = when (style) {
            STYLE_ENTER -> BakeryKeyDrawable(
                fillColor = if (isPressed) COLOR_ENTER_PRESSED else COLOR_ENTER_TEAL,
                strokeColor = COLOR_ENTER_BORDER,
                strokeWidth = strokeWidth,
                cornerRadius = radius
            )
            STYLE_SHIFT -> BakeryKeyDrawable(
                fillColor = if (isPressed) COLOR_PRESSED else COLOR_SHIFT_CARD,
                strokeColor = COLOR_CRUST_BORDER,
                strokeWidth = strokeWidth,
                cornerRadius = radius
            )
            STYLE_DELETE -> BakeryKeyDrawable(
                fillColor = if (isPressed) COLOR_PRESSED else COLOR_DELETE_CRUMB,
                strokeColor = COLOR_CRUST_BORDER,
                strokeWidth = strokeWidth,
                cornerRadius = radius
            )
            STYLE_SPACE -> BakeryKeyDrawable(
                fillColor = if (isPressed) COLOR_PRESSED else COLOR_BISCUIT_GOLD,
                strokeColor = COLOR_CRUST_BORDER,
                strokeWidth = strokeWidth,
                cornerRadius = radius,
                isSpacebar = true
            )
            STYLE_SYMBOL -> BakeryKeyDrawable(
                fillColor = if (isPressed) COLOR_PRESSED else COLOR_SHIFT_CARD,
                strokeColor = COLOR_CRUST_BORDER,
                strokeWidth = strokeWidth,
                cornerRadius = radius
            )
            STYLE_COOKIE -> BakeryKeyDrawable(
                fillColor = if (isPressed) COLOR_PRESSED else COLOR_BISCUIT_GOLD,
                strokeColor = COLOR_CRUST_BORDER,
                strokeWidth = strokeWidth,
                cornerRadius = radius,
                isCookie = true
            )
            else -> BakeryKeyDrawable(
                fillColor = if (isPressed) COLOR_PRESSED else COLOR_BREAD_CREAM,
                strokeColor = COLOR_CRUST_BORDER,
                strokeWidth = strokeWidth,
                cornerRadius = radius
            )
        }

        drawableCache[cacheKey] = drawable
        return drawable
    }

    fun getKeyTextColor(key: Key): Int {
        if (key.code == Constants.CODE_ENTER) return COLOR_ENTER_TEXT
        return COLOR_TEXT_CHOCOLATE
    }

    fun getKeyHintColor(key: Key): Int {
        return COLOR_HINT_CHOCOLATE
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
            paint.color = COLOR_POPUP_BG
        }
    }
}
