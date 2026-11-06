// Note: Mainly lifted from androidx.emoji2.emojipicker
/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.futo.inputmethod.latin.uix.actions.emoji

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import org.futo.inputmethod.latin.R

class EmojiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    init {
        background = AppCompatResources.getDrawable(context, R.drawable.ripple_emoji_view)
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    internal var willDrawVariantIndicator: Boolean = true

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            32f,
            context.resources.displayMetrics
        )
    }

    fun setTextColor(color: Int) {
        textPaint.color = color
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = minOf(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        ) - context.resources.getDimensionPixelSize(R.dimen.emoji_picker_emoji_view_padding)
        setMeasuredDimension(size, size)
    }

    var emoji: EmojiItem? = null
        set(value) {
            field = value
            contentDescription = value?.description
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val currentEmoji = emoji ?: return
        if (width <= 0 || height <= 0) return

        val emojiText = currentEmoji.emoji
        textPaint.textSize = height * 0.62f

        val textWidth = textPaint.measureText(emojiText, 0, emojiText.length)
        canvas.save()
        if (textWidth > width) {
            val scale = width.toFloat() / textWidth
            canvas.scale(scale, 1.0f, width / 2f, height / 2f)
        }
        val fontMetrics = textPaint.fontMetrics
        val x = (width - textWidth) / 2f
        val y = (height - (fontMetrics.descent + fontMetrics.ascent)) / 2f
        canvas.drawText(emojiText, 0, emojiText.length, x, y, textPaint)
        canvas.restore()

        if (willDrawVariantIndicator && currentEmoji.skinTones) {
            AppCompatResources.getDrawable(context, R.drawable.variant_availability_indicator)?.apply {
                val indicatorWidth = context.resources.getDimensionPixelSize(
                    R.dimen.variant_availability_indicator_width
                )
                val indicatorHeight = context.resources.getDimensionPixelSize(
                    R.dimen.variant_availability_indicator_height
                )
                bounds = Rect(
                    width - indicatorWidth,
                    height - indicatorHeight,
                    width,
                    height
                )
                draw(canvas)
            }
        }
    }
}