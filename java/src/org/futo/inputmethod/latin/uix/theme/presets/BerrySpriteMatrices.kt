package org.futo.inputmethod.latin.uix.theme.presets

import android.graphics.*
import kotlin.math.min

/**
 * Pixel & Vector Sprite Matrix Renderer for Berry & Blush (Kawaii Coquette)
 * Converted directly from pixel perfect reference matrices.
 */
object BerrySpriteMatrices {
    val SPRITE_COQUETTE_BOW = arrayOf(
        intArrayOf(0,1,1,1,1,0,0,0,0,0,0,0,1,1,1,1,0),
        intArrayOf(1,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,1),
        intArrayOf(1,0,0,1,0,0,1,0,1,1,1,0,1,0,1,0,1),
        intArrayOf(1,0,1,0,1,0,0,1,0,0,0,1,0,1,0,0,1),
        intArrayOf(1,0,0,0,0,1,0,1,0,0,0,1,0,0,0,0,1),
        intArrayOf(0,1,1,1,1,0,0,0,1,1,1,0,0,1,1,1,0),
        intArrayOf(0,0,0,0,0,1,0,1,0,0,0,1,0,0,0,0,0),
        intArrayOf(0,0,0,0,1,0,1,0,1,0,1,0,1,0,0,0,0),
        intArrayOf(0,0,0,1,0,1,0,0,0,0,0,1,0,1,0,0,0),
        intArrayOf(0,0,1,0,1,0,0,0,0,0,0,0,1,0,1,0,0),
        intArrayOf(0,1,1,1,0,0,0,0,0,0,0,0,0,1,1,1,0)
    )

    val SPRITE_CAT_PAW_BACKSPACE = arrayOf(
        intArrayOf(0,0,1,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0),
        intArrayOf(0,1,0,0,0,1,0,0,0,1,1,1,1,1,1,1,1,0,0),
        intArrayOf(1,1,1,1,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0),
        intArrayOf(0,1,0,0,1,0,1,1,0,1,1,0,0,0,0,0,0,0,0),
        intArrayOf(0,0,1,0,1,0,0,0,0,1,1,0,0,0,0,0,0,0,0),
        intArrayOf(0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0),
        intArrayOf(0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0),
        intArrayOf(0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1)
    )

    val SPRITE_SPACE_CLUSTER = arrayOf(
        intArrayOf(0,1,1,0,1,1,0,0,0,0,0,1,0,0),
        intArrayOf(1,0,0,1,0,0,1,0,0,0,1,0,1,0),
        intArrayOf(1,0,0,0,0,0,1,0,0,1,0,0,0,1),
        intArrayOf(0,1,0,0,0,1,0,0,0,0,1,0,1,0),
        intArrayOf(0,0,1,0,1,0,0,0,0,0,0,1,0,0),
        intArrayOf(0,0,0,1,0,0,0,0,0,0,0,0,0,0)
    )

    val GLYPH_HEART_I = arrayOf(
        intArrayOf(0,1,1,0,1,1,0),
        intArrayOf(1,1,1,1,1,1,1),
        intArrayOf(0,1,1,1,1,1,0),
        intArrayOf(0,0,1,1,1,0,0),
        intArrayOf(0,0,0,1,0,0,0),
        intArrayOf(0,0,0,0,0,0,0),
        intArrayOf(0,0,0,1,0,0,0),
        intArrayOf(0,0,0,1,0,0,0),
        intArrayOf(0,0,0,1,0,0,0),
        intArrayOf(0,0,0,1,0,0,0),
        intArrayOf(0,0,0,1,0,0,0)
    )

    val GLYPH_HEART_O = arrayOf(
        intArrayOf(0,1,1,1,0,1,1,1,0),
        intArrayOf(1,1,0,1,1,1,0,1,1),
        intArrayOf(1,0,0,0,1,0,0,0,1),
        intArrayOf(1,0,0,0,0,0,0,0,1),
        intArrayOf(0,1,0,0,0,0,0,1,0),
        intArrayOf(0,0,1,0,0,0,1,0,0),
        intArrayOf(0,0,0,1,0,1,0,0,0),
        intArrayOf(0,0,0,0,1,0,0,0,0)
    )

    val GLYPH_CUTE_Q = arrayOf(
        intArrayOf(0,1,1,1,1,0,0,0),
        intArrayOf(1,0,0,0,1,0,0,0),
        intArrayOf(1,0,0,0,1,0,0,0),
        intArrayOf(0,1,1,1,1,0,0,0),
        intArrayOf(0,0,0,0,1,0,0,0),
        intArrayOf(0,0,1,0,1,0,1,0),
        intArrayOf(0,0,0,1,1,1,0,1),
        intArrayOf(0,0,1,0,1,0,0,1),
        intArrayOf(0,0,0,0,0,1,1,0)
    )

    val GLYPH_CUTE_G = arrayOf(
        intArrayOf(0,0,1,1,1,1,0),
        intArrayOf(0,1,0,0,0,1,0),
        intArrayOf(0,1,0,0,0,1,0),
        intArrayOf(0,0,1,1,1,1,0),
        intArrayOf(0,0,0,0,0,1,0),
        intArrayOf(0,1,1,0,0,1,0),
        intArrayOf(1,0,1,0,0,1,0),
        intArrayOf(1,0,0,0,0,1,0),
        intArrayOf(0,1,1,1,1,0,0)
    )

    val SPRITE_DOUBLE_HEARTS = arrayOf(
        intArrayOf(1,0,1,0,0,0,0),
        intArrayOf(1,1,1,0,0,0,0),
        intArrayOf(0,1,0,0,1,0,1),
        intArrayOf(0,0,0,1,1,1,1),
        intArrayOf(0,0,0,0,1,1,0)
    )

    val SPRITE_KITTY_PAW = arrayOf(
        intArrayOf(0,1,0,1,0,1,0),
        intArrayOf(1,0,0,0,0,0,1),
        intArrayOf(0,0,1,1,1,0,0),
        intArrayOf(0,1,1,1,1,1,0),
        intArrayOf(0,1,1,0,1,1,0)
    )

    val SPRITE_SPARKLE_CLUSTER = arrayOf(
        intArrayOf(0,0,0,0,0,1,0),
        intArrayOf(0,0,1,0,1,1,1),
        intArrayOf(0,1,1,1,0,1,0),
        intArrayOf(1,1,1,1,1,0,0),
        intArrayOf(0,1,1,1,0,0,0),
        intArrayOf(0,0,1,0,0,0,0)
    )

    fun drawSpriteCentered(
        canvas: Canvas,
        rect: RectF,
        matrix: Array<IntArray>,
        scale: Float,
        color: Int,
        paint: Paint
    ) {
        val rows = matrix.size
        val cols = matrix[0].size
        val targetHeight = rect.height() * scale
        val targetWidth = rect.width() * scale
        val spacing = min(targetWidth / cols, targetHeight / rows)
        val dotRadius = spacing * 0.38f

        val totalW = (cols - 1) * spacing
        val totalH = (rows - 1) * spacing
        val startX = rect.centerX() - totalW / 2f
        val startY = rect.centerY() - totalH / 2f

        paint.color = color
        paint.style = Paint.Style.FILL
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (matrix[r][c] == 1) {
                    canvas.drawCircle(startX + c * spacing, startY + r * spacing, dotRadius, paint)
                }
            }
        }
    }
}
