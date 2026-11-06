package org.futo.inputmethod.latin.uix

import android.content.res.TypedArray
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import org.futo.inputmethod.keyboard.Key
import org.futo.inputmethod.keyboard.internal.TapAnimationType

interface DynamicThemeProvider {
    val primaryKeyboardColor: Int
    val keyColor: Int

    val keyboardBackground: Drawable
    val keyBackground: Drawable
    val spaceBarBackground: Drawable

    val keyFeedback: Drawable

    val moreKeysTextColor: Int
    val moreKeysKeyboardBackground: Drawable
    val popupKey: Drawable
    val actionPopupKey: Drawable

    val tapAnimationType: TapAnimationType
        get() = TapAnimationType.NONE

    val keyboardTypeface: Typeface?
        get() = null

    val isDotFont: Boolean
        get() = true

    val isQurovaFont: Boolean
        get() = false

    @ColorInt
    fun getColor(i: Int): Int?

    fun getDrawable(i: Int): Drawable?

    fun getIcon(iconName: String): Drawable?

    fun getKeyboardHeightMultiplier(): Float

    fun getKeyBackground(key: Key, isPressed: Boolean): Drawable? = null

    fun getKeyTextColor(key: Key): Int? = null

    fun getKeyHintColor(key: Key): Int? = null

    fun getKeyPreviewBackground(key: Key): Drawable? = null
    fun getKeyPreviewBackground(key: Key, existing: org.futo.inputmethod.keyboard.internal.KeyPopupDrawable?): Drawable? = getKeyPreviewBackground(key)

    fun getKeyPreviewTextColor(key: Key): Int? = null

    fun getKeyLabelOverride(key: Key, originalLabel: String): String = originalLabel

    fun getKeyRotation(key: Key): Float = 0f

    fun getKeyHintCoordinates(key: Key, keyWidth: Int, keyHeight: Int, padding: Float): FloatArray? = null

    val spacebarShape: String
        get() = "boxy"

    val spacebarTextColor: Int?
        get() = null

    fun getSpacebarCornerRadius(): Float? = null

    companion object {
        @ColorInt
        fun getColorOrDefault(i: Int, @ColorInt default: Int, keyAttr: TypedArray, provider: DynamicThemeProvider?): Int {
            return (provider?.getColor(i)) ?: keyAttr.getColor(i, default)
        }

        fun getDrawableOrDefault(i: Int, keyAttr: TypedArray, provider: DynamicThemeProvider?): Drawable? {
            return (provider?.getDrawable(i)) ?: keyAttr.getDrawable(i)
        }
    }
}
