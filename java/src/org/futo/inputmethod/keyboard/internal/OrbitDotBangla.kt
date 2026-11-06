package org.futo.inputmethod.keyboard.internal

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import org.futo.inputmethod.latin.R

/**
 * Handles dot-matrix typography for Bengali numbers (০, ১, ২, ৩, ৪, ৫, ৬, ৭, ৮, ৯).
 * Bengali letters remain in the clean system font; only Bengali numbers render in the
 * dot-matrix style from OrbitDotBangla when dot font is active.
 */
object OrbitDotBangla {
    @Volatile
    private var cachedTypeface: Typeface? = null

    @JvmStatic
    fun getTypeface(context: Context?): Typeface? {
        if (cachedTypeface != null) return cachedTypeface
        if (context == null) return Typeface.DEFAULT
        synchronized(this) {
            if (cachedTypeface == null) {
                cachedTypeface = try {
                    ResourcesCompat.getFont(context, R.font.orbit_dot_bangla)
                        ?: ResourcesCompat.getFont(context, R.font.ndot_family)
                        ?: Typeface.DEFAULT
                } catch (e: Exception) {
                    Typeface.DEFAULT
                }
            }
        }
        return cachedTypeface
    }

    @JvmStatic
    fun isBengaliDigit(c: Char): Boolean = c in '\u09E6'..'\u09EF'

    @JvmStatic
    fun isBengaliDigitString(text: String?): Boolean {
        if (text.isNullOrEmpty()) return false
        for (i in 0 until text.length) {
            if (!isBengaliDigit(text[i])) return false
        }
        return true
    }

    @JvmStatic
    fun containsBengaliDigit(text: String?): Boolean {
        if (text.isNullOrEmpty()) return false
        for (i in 0 until text.length) {
            if (isBengaliDigit(text[i])) return true
        }
        return false
    }

    @JvmStatic
    fun containsBengaliLetter(text: String?): Boolean {
        if (text.isNullOrEmpty()) return false
        for (i in 0 until text.length) {
            val c = text[i]
            if (c in '\u0980'..'\u09FF' && !isBengaliDigit(c)) {
                return true
            }
        }
        return false
    }
}
