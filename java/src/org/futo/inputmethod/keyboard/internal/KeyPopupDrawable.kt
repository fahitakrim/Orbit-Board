package org.futo.inputmethod.keyboard.internal

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import org.futo.inputmethod.latin.R

/**
 * Defines the visual silhouette and geometry of the LatinIME key preview popup.
 */
enum class KeyPopupStyle(val id: Int) {
    /** Minimal floating squircle with smooth curvature (Default Orbit Style). */
    SLIM(0),

    /** Compact circular floating bubble (Gboard Style). */
    GBOARD(1),

    /** Sleek smooth paddle contour. */
    FLUID(2),

    /** Sharp rectangular Bauhaus preview. */
    BOXY(3);

    companion object {
        @JvmField
        val ORBIT = SLIM
        @JvmField
        val ORBIT_SLIM = SLIM
        @JvmField
        val GBOARD_BUBBLE = GBOARD
        @JvmField
        val FLUID_CONTOUR = FLUID
        @JvmField
        val FLUID_CALLOUT = FLUID
        @JvmField
        val BOXY_POPUP = BOXY
        // Backward compatibility
        @JvmField
        val IOS = FLUID
    }
}

/**
 * Horizontal alignment mode when clamping near screen edges.
 */
enum class KeyPopupEdgeAlignment {
    CENTER,
    LEFT_CLAMPED,
    RIGHT_CLAMPED
}

/**
 * Hardware-accelerated [Drawable] for LatinIME KeyPreviewView.
 *
 * Provides 4 selectable key preview popup styles:
 * 1. STYLE_ORBIT_SLIM (0): Smooth floating squircle directly above key (Default).
 * 2. STYLE_GBOARD_BUBBLE (1): Compact circular floating bubble directly above key, zero stem, soft drop shadow.
 * 3. STYLE_FLUID_CONTOUR (2): Sleek continuous paddle contour merging smoothly into key base.
 * 4. STYLE_BOXY (3): Sharp rectangular Bauhaus boxy preview.
 *
 * Optimized for 0 object allocations during keystroke render loops on 120Hz displays.
 */
class KeyPopupDrawable(
    var style: Int = STYLE_ORBIT_SLIM,
    @ColorInt private var fillColor: Int = 0xFF1C1D20.toInt(),
    @ColorInt private var strokeColor: Int = 0x33FFFFFF.toInt(),
    var cornerRadius: Float = 8f,
    private var intrinsicW: Int = 48,
    private var intrinsicH: Int = 122,
    var bottomPadding: Int = 50
) : Drawable() {

    companion object {
        const val STYLE_ORBIT_SLIM = 0
        const val STYLE_GBOARD_BUBBLE = 1
        const val STYLE_FLUID_CALLOUT = 2
        const val STYLE_FLUID_CONTOUR = 2
        const val STYLE_BOXY = 3

        @JvmStatic
        fun parseStyle(styleName: String?): Int {
            return when (styleName?.lowercase(java.util.Locale.ROOT)) {
                "gboard_bubble", "gboard", "1" -> STYLE_GBOARD_BUBBLE
                "fluid_contour", "fluid_callout", "fluid", "contour", "paddle", "2", "ios", "apple" -> STYLE_FLUID_CONTOUR
                "boxy", "box", "square", "3" -> STYLE_BOXY
                else -> STYLE_ORBIT_SLIM
            }
        }
    }

    private var density: Float = 2.0f
    private var keyWidthPx: Int = 48
    private var keyHeightPx: Int = 54

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.style = Paint.Style.FILL
        this.color = fillColor
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.style = Paint.Style.STROKE
        this.color = strokeColor
        this.strokeWidth = (1.2f * density).coerceAtLeast(1.5f)
    }

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.style = Paint.Style.FILL
        this.color = fillColor
    }

    private val rectF = RectF()
    private val arcRect = RectF()
    private val paddlePath = Path()
    private var position = KeyPreviewView.POSITION_MIDDLE
    private var hasMoreKeys = false

    constructor(
        context: Context? = null,
        popupStyle: KeyPopupStyle = KeyPopupStyle.SLIM
    ) : this(
        style = popupStyle.id,
        fillColor = 0xFF1C1D20.toInt(),
        strokeColor = 0x33FFFFFF.toInt(),
        cornerRadius = 8f,
        intrinsicW = 48,
        intrinsicH = 122,
        bottomPadding = 50
    ) {
        if (context != null) {
            density = context.resources.displayMetrics.density
            cornerRadius = 8f * density
            strokePaint.strokeWidth = (1.2f * density).coerceAtLeast(1.5f)
        }
        updateLuminanceColors()
    }

    private fun updateLuminanceColors() {
        val lum = androidx.core.graphics.ColorUtils.calculateLuminance(fillColor)
        if (lum > 0.5f) {
            shadowPaint.color = fillColor
            shadowPaint.setShadowLayer(8f * density, 0f, 3f * density, 0x22000000)
        } else {
            shadowPaint.color = fillColor
            shadowPaint.setShadowLayer(8f * density, 0f, 3f * density, 0x4D000000)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun configureForKey(
        keyWidthPx: Int,
        keyHeightPx: Int,
        alignment: KeyPopupEdgeAlignment = KeyPopupEdgeAlignment.CENTER,
        stemOffsetPx: Float = 0f
    ) {
        if (keyWidthPx > 0) {
            this.keyWidthPx = keyWidthPx
        }
        if (keyHeightPx > 0) {
            this.keyHeightPx = keyHeightPx
        }
        val kW = this.keyWidthPx.toFloat()
        val kH = this.keyHeightPx.toFloat()

        when (style) {
            STYLE_GBOARD_BUBBLE -> {
                val gap = 6f * density
                val targetSize = (kH * 0.78f).coerceIn(38f * density, 46f * density)
                intrinsicW = targetSize.toInt()
                intrinsicH = (kH + gap + targetSize).toInt()
                bottomPadding = (kH + gap).toInt()
            }
            STYLE_FLUID_CONTOUR -> {
                val flare = (4f * density).coerceAtMost(kW * 0.10f)
                val headW = kW + 2f * flare
                val neckH = (kH * 0.22f).coerceIn(6f * density, 12f * density)
                val headH = (kH * 0.88f).coerceIn(36f * density, 44f * density)
                intrinsicW = headW.toInt()
                intrinsicH = (kH + neckH + headH).toInt()
                bottomPadding = (kH + neckH).toInt()
            }
            STYLE_BOXY -> {
                val gap = 6f * density
                val bubbleH = (kH * 0.90f).coerceIn(36f * density, 48f * density)
                intrinsicW = kW.toInt()
                intrinsicH = (kH + gap + bubbleH).toInt()
                bottomPadding = (kH + gap).toInt()
            }
            else -> {
                val gap = 6f * density
                val bubbleH = (kH * 0.92f).coerceIn(36f * density, 48f * density)
                intrinsicW = kW.toInt()
                intrinsicH = (kH + gap + bubbleH).toInt()
                bottomPadding = (kH + gap).toInt()
            }
        }
        invalidateSelf()
    }

    fun setColors(fill: Int, stroke: Int = 0x33FFFFFF.toInt()) {
        fillColor = fill
        strokeColor = stroke
        fillPaint.color = fill
        strokePaint.color = stroke
        updateLuminanceColors()
        invalidateSelf()
    }

    fun reconfigure(
        newStyle: Int,
        newFillColor: Int,
        newStrokeColor: Int,
        newCornerRadius: Float,
        keyWidthPx: Int,
        keyHeightPx: Int
    ) {
        this.style = newStyle
        this.fillColor = newFillColor
        this.strokeColor = newStrokeColor
        this.fillPaint.color = newFillColor
        this.strokePaint.color = newStrokeColor
        this.cornerRadius = newCornerRadius
        updateLuminanceColors()
        configureForKey(keyWidthPx, keyHeightPx)
    }

    override fun isStateful(): Boolean = true

    override fun onStateChange(state: IntArray): Boolean {
        var newPosition = KeyPreviewView.POSITION_MIDDLE
        var newHasMoreKeys = false
        for (s in state) {
            when (s) {
                R.attr.state_left_edge -> newPosition = KeyPreviewView.POSITION_LEFT
                R.attr.state_right_edge -> newPosition = KeyPreviewView.POSITION_RIGHT
                R.attr.state_has_morekeys -> newHasMoreKeys = true
            }
        }
        val changed = (newPosition != position || newHasMoreKeys != hasMoreKeys)
        if (changed) {
            position = newPosition
            hasMoreKeys = newHasMoreKeys
            invalidateSelf()
        }
        return changed
    }

    private val internalBounds = Rect()

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        try { super.setBounds(left, top, right, bottom) } catch (_: Throwable) {}
        internalBounds.left = left
        internalBounds.top = top
        internalBounds.right = right
        internalBounds.bottom = bottom
    }

    override fun setBounds(bounds: Rect) {
        try { super.setBounds(bounds) } catch (_: Throwable) {}
        internalBounds.left = bounds.left
        internalBounds.top = bounds.top
        internalBounds.right = bounds.right
        internalBounds.bottom = bounds.bottom
    }

    val effectiveBounds: Rect
        get() {
            val b = try { bounds } catch (_: Throwable) { null }
            return if (b != null && (b.left != 0 || b.top != 0 || b.right != 0 || b.bottom != 0)) b else internalBounds
        }

    private val contentRect = RectF()

    private fun setContentRect(l: Float, t: Float, r: Float, b: Float): RectF {
        contentRect.left = l
        contentRect.top = t
        contentRect.right = r
        contentRect.bottom = b
        contentRect.set(l, t, r, b)
        return contentRect
    }

    fun getContentRect(): RectF {
        val b = effectiveBounds
        val bW = b.right - b.left
        val bH = b.bottom - b.top

        val bLeft = if (bW > 0) b.left.toFloat() else 0f
        val bTop = if (bH > 0) b.top.toFloat() else 0f
        val bRight = if (bW > 0) b.right.toFloat() else (if (intrinsicW > 0) intrinsicW.toFloat() else 48f * density)
        val bBottom = if (bH > 0) b.bottom.toFloat() else (if (intrinsicH > 0) intrinsicH.toFloat() else 122f * density)

        val kH = if (keyHeightPx > 0) keyHeightPx.toFloat() else (bBottom - bTop) * 0.45f
        val kW = if (keyWidthPx > 0) keyWidthPx.toFloat() else (bRight - bLeft)

        when (style) {
            STYLE_GBOARD_BUBBLE -> {
                val gap = 6f * density
                val keyTop = bBottom - kH
                val bubbleBottom = (keyTop - gap).coerceAtLeast(bTop + 16f)
                val targetSize = (kH * 0.78f).coerceIn(38f * density, 46f * density)
                val bubbleSize = targetSize.coerceAtMost(bubbleBottom - bTop)
                val bubbleTop = bubbleBottom - bubbleSize
                val cx = (bLeft + bRight) / 2f
                val left = cx - bubbleSize / 2f
                val right = cx + bubbleSize / 2f
                return setContentRect(left, bubbleTop, right, bubbleBottom)
            }
            STYLE_FLUID_CONTOUR -> {
                val stemTop = (bBottom - kH).coerceAtLeast(bTop + 16f)
                val neckH = (kH * 0.22f).coerceIn(6f * density, 12f * density)
                val headBottom = stemTop - neckH
                val headHeight = (kH * 0.88f).coerceIn(36f * density, 44f * density)
                val headTop = (headBottom - headHeight).coerceAtLeast(bTop + 2f)

                val flare = (4f * density).coerceAtMost(kW * 0.10f)
                val headW = kW + 2f * flare

                val headLeft: Float
                val headRight: Float
                when (position) {
                    KeyPreviewView.POSITION_LEFT -> {
                        headLeft = bLeft + 2f
                        headRight = (headLeft + headW).coerceAtMost(bRight - 2f)
                    }
                    KeyPreviewView.POSITION_RIGHT -> {
                        headRight = bRight - 2f
                        headLeft = (headRight - headW).coerceAtLeast(bLeft + 2f)
                    }
                    else -> {
                        val midX = (bLeft + bRight) / 2f
                        headLeft = (midX - headW / 2f).coerceAtLeast(bLeft + 2f)
                        headRight = (headLeft + headW).coerceAtMost(bRight - 2f)
                    }
                }
                return setContentRect(headLeft, headTop, headRight, headBottom)
            }
            STYLE_BOXY -> {
                val gap = 6f * density
                val keyTop = bBottom - kH
                val bubbleBottom = (keyTop - gap).coerceAtLeast(bTop + 16f)
                val bubbleH = (kH * 0.90f).coerceIn(36f * density, 48f * density).coerceAtMost(bubbleBottom - bTop)
                val bubbleTop = bubbleBottom - bubbleH
                return setContentRect(bLeft + 2f, bubbleTop, bRight - 2f, bubbleBottom)
            }
            else -> {
                val gap = 6f * density
                val keyTop = bBottom - kH
                val bubbleBottom = (keyTop - gap).coerceAtLeast(bTop + 16f)
                val bubbleH = (kH * 0.92f).coerceIn(36f * density, 48f * density).coerceAtMost(bubbleBottom - bTop)
                val bubbleTop = bubbleBottom - bubbleH
                return setContentRect(bLeft + 2f, bubbleTop, bRight - 2f, bubbleBottom)
            }
        }
    }

    override fun getPadding(padding: Rect): Boolean {
        val b = effectiveBounds
        val bW = b.right - b.left
        val bH = b.bottom - b.top
        if (bW <= 0 || bH <= 0) {
            val safeBottom = if (bottomPadding > 0) bottomPadding else 0
            padding.left = 0
            padding.top = 0
            padding.right = 0
            padding.bottom = safeBottom
            padding.set(0, 0, 0, safeBottom)
            return true
        }

        val content = getContentRect()
        val bLeft = b.left.toFloat()
        val bRight = b.right.toFloat()
        val bTop = b.top.toFloat()
        val bBottom = b.bottom.toFloat()

        val pLeft = (content.left - bLeft).toInt().coerceAtLeast(0)
        val pTop = (content.top - bTop).toInt().coerceAtLeast(0)
        val pRight = (bRight - content.right).toInt().coerceAtLeast(0)
        val pBottom = (bBottom - content.bottom).toInt().coerceAtLeast(0)
        padding.left = pLeft
        padding.top = pTop
        padding.right = pRight
        padding.bottom = pBottom
        padding.set(pLeft, pTop, pRight, pBottom)
        return true
    }

    override fun getIntrinsicWidth(): Int = if (intrinsicW > 0) intrinsicW else 48

    override fun getIntrinsicHeight(): Int {
        val safeHeight = if (intrinsicH > 0) intrinsicH else 24
        val safePadding = if (bottomPadding in 0 until safeHeight) bottomPadding else 0
        return safeHeight - safePadding
    }

    override fun draw(canvas: Canvas) {
        val b = effectiveBounds
        val bW = b.right - b.left
        val bH = b.bottom - b.top
        if (bW <= 0 || bH <= 0) return

        val bLeft = b.left.toFloat()
        val bRight = b.right.toFloat()
        val bTop = b.top.toFloat()
        val bBottom = b.bottom.toFloat()

        when (style) {
            STYLE_ORBIT_SLIM -> {
                // Classic Orbit floating squircle directly above key with key base Bauhaus color and hairline stroke
                val content = getContentRect()
                val r = cornerRadius.coerceIn(6f * density, content.height() / 2f)
                canvas.drawRoundRect(content, r, r, shadowPaint)
                canvas.drawRoundRect(content, r, r, fillPaint)
                canvas.drawRoundRect(content, r, r, strokePaint)
            }

            STYLE_GBOARD_BUBBLE -> {
                // Compact circular bubble (width ≈ height, authentic Gboard floating bubble)
                val content = getContentRect()
                val pillRadius = content.width() / 2f

                canvas.drawRoundRect(content, pillRadius, pillRadius, shadowPaint)
                canvas.drawRoundRect(content, pillRadius, pillRadius, fillPaint)
                canvas.drawRoundRect(content, pillRadius, pillRadius, strokePaint)
            }

            STYLE_FLUID_CONTOUR -> {
                // Sleek continuous paddle contour
                val kH = if (keyHeightPx > 0) keyHeightPx.toFloat() else (bBottom - bTop) * 0.45f
                val stemW = if (keyWidthPx > 0) keyWidthPx.toFloat() else (bRight - bLeft)
                val stemBottom = bBottom
                val stemTop = (bBottom - kH).coerceAtLeast(bTop + 16f)
                val neckH = (kH * 0.22f).coerceIn(6f * density, 12f * density)

                val content = getContentRect()
                val headLeft = content.left
                val headTop = content.top
                val headRight = content.right
                val headBottom = content.bottom

                val stemLeft: Float
                val stemRight: Float

                when (position) {
                    KeyPreviewView.POSITION_LEFT -> {
                        stemLeft = bLeft + 2f
                        stemRight = stemLeft + stemW
                    }
                    KeyPreviewView.POSITION_RIGHT -> {
                        stemRight = bRight - 2f
                        stemLeft = stemRight - stemW
                    }
                    else -> {
                        val midX = (bLeft + bRight) / 2f
                        stemLeft = (midX - stemW / 2f).coerceAtLeast(bLeft + 2f)
                        stemRight = (stemLeft + stemW).coerceAtMost(bRight - 2f)
                    }
                }

                val headRadius = (10f * density).coerceIn(6f, (headBottom - headTop) / 2f)
                val stemRadius = cornerRadius.coerceIn(4f, 6f * density)

                paddlePath.reset()
                paddlePath.moveTo(headLeft + headRadius, headTop)
                paddlePath.lineTo(headRight - headRadius, headTop)
                arcRect.set(headRight - 2f * headRadius, headTop, headRight, headTop + 2f * headRadius)
                paddlePath.arcTo(arcRect, -90f, 90f, false)
                paddlePath.lineTo(headRight, headBottom)

                if (kotlin.math.abs(headRight - stemRight) < 1f) {
                    paddlePath.lineTo(stemRight, stemTop)
                } else {
                    paddlePath.cubicTo(
                        headRight, headBottom + neckH * 0.5f,
                        stemRight, stemTop - neckH * 0.5f,
                        stemRight, stemTop
                    )
                }

                paddlePath.lineTo(stemRight, stemBottom - stemRadius)
                arcRect.set(stemRight - 2f * stemRadius, stemBottom - 2f * stemRadius, stemRight, stemBottom)
                paddlePath.arcTo(arcRect, 0f, 90f, false)
                paddlePath.lineTo(stemLeft + stemRadius, stemBottom)
                arcRect.set(stemLeft, stemBottom - 2f * stemRadius, stemLeft + 2f * stemRadius, stemBottom)
                paddlePath.arcTo(arcRect, 90f, 90f, false)
                paddlePath.lineTo(stemLeft, stemTop)

                if (kotlin.math.abs(headLeft - stemLeft) < 1f) {
                    paddlePath.lineTo(headLeft, headBottom)
                } else {
                    paddlePath.cubicTo(
                        stemLeft, stemTop - neckH * 0.5f,
                        headLeft, headBottom + neckH * 0.5f,
                        headLeft, headBottom
                    )
                }

                paddlePath.lineTo(headLeft, headTop + headRadius)
                arcRect.set(headLeft, headTop, headLeft + 2f * headRadius, headTop + 2f * headRadius)
                paddlePath.arcTo(arcRect, 180f, 90f, false)
                paddlePath.close()

                canvas.drawPath(paddlePath, shadowPaint)
                canvas.drawPath(paddlePath, fillPaint)
                canvas.drawPath(paddlePath, strokePaint)
            }

            STYLE_BOXY -> {
                // Sharp rectangular Bauhaus boxy preview
                val content = getContentRect()
                val r = (3f * density).coerceAtMost(content.height() / 4f)
                canvas.drawRoundRect(content, r, r, shadowPaint)
                canvas.drawRoundRect(content, r, r, fillPaint)
                canvas.drawRoundRect(content, r, r, strokePaint)
            }

            else -> {
                val content = getContentRect()
                val r = cornerRadius.coerceIn(6f * density, content.height() / 2f)
                canvas.drawRoundRect(content, r, r, shadowPaint)
                canvas.drawRoundRect(content, r, r, fillPaint)
                canvas.drawRoundRect(content, r, r, strokePaint)
            }
        }
    }

    override fun getOutline(outline: Outline) {
        val b = effectiveBounds
        val bW = b.right - b.left
        val bH = b.bottom - b.top
        if (bW <= 0 || bH <= 0) {
            outline.setRect(0, 0, 0, 0)
            return
        }
        val safeBottomPadding = if (bottomPadding in 1 until bH) bottomPadding else 0
        val bubbleBottom = (b.bottom - safeBottomPadding).coerceAtLeast(b.top + 10)
        val r = when (style) {
            STYLE_GBOARD_BUBBLE -> (bubbleBottom - b.top) * 0.48f
            STYLE_FLUID_CONTOUR -> (10f * density).coerceIn(6f, (bubbleBottom - b.top) / 2f)
            STYLE_BOXY -> 3f * density
            else -> (10f * density).coerceIn(4f, (bubbleBottom - b.top) / 2f)
        }
        outline.setRoundRect(b.left, b.top, b.right, bubbleBottom, r)
        outline.alpha = fillPaint.alpha / 255f
    }

    override fun setAlpha(alpha: Int) {
        fillPaint.alpha = alpha
        strokePaint.alpha = alpha
        shadowPaint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        fillPaint.colorFilter = colorFilter
        strokePaint.colorFilter = colorFilter
        shadowPaint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
