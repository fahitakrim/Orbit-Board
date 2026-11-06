package org.futo.inputmethod.latin.uix

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.StateListDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.Log
import android.util.TypedValue
import androidx.annotation.ColorInt
import org.futo.inputmethod.keyboard.Key
import org.futo.inputmethod.latin.common.Constants
import org.futo.inputmethod.latin.uix.theme.presets.OrbitThemeHelper
import org.futo.inputmethod.latin.uix.theme.presets.ChaosThemeHelper
import org.futo.inputmethod.keyboard.internal.TapAnimationType
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.android.material.color.DynamicColors
import org.futo.inputmethod.keyboard.internal.KeyPopupDrawable
import org.futo.inputmethod.keyboard.internal.KeyboardIconsSet
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.actions.AllActions
import org.futo.inputmethod.latin.uix.actions.AllActionsMap
import org.futo.inputmethod.latin.uix.theme.DarkColorScheme
import kotlin.math.roundToInt

val KeyBordersSetting = SettingsKey(booleanPreferencesKey("keyBorders"), true)
val HiddenKeysSetting = SettingsKey(booleanPreferencesKey("hiddenKeys"), false)
val KeyHintsSetting   = SettingsKey(booleanPreferencesKey("keyHints"), true)
val KeyPopupStyleSetting = SettingsKey(stringPreferencesKey("key_popup_style"), "orbit_slim")
val KeyboardFontSetting = SettingsKey(stringPreferencesKey("keyboard_font"), "nothing")
val KeyHintColorSetting = SettingsKey(stringPreferencesKey("key_hint_color"), "subtle")
val CustomAccentColorSetting = SettingsKey(stringPreferencesKey("custom_accent_color"), "default")
val SpacebarColorSetting = SettingsKey(stringPreferencesKey("spacebar_color"), "theme")
val SpacebarBorderSetting = SettingsKey(stringPreferencesKey("spacebar_border"), "none")
val SpacebarShapeSetting = SettingsKey(stringPreferencesKey("spacebar_shape"), "boxy")
val SpacebarTextColorSetting = SettingsKey(stringPreferencesKey("pref_spacebar_text_color"), "default")
val KeyboardTypingEffectSetting = SettingsKey(stringPreferencesKey("typing_effect"), "none")

val KeyboardHeightMultiplierSetting = SettingsKey(floatPreferencesKey("keyboardHeightMultiplier"), 1.0f)
val KeyboardBottomOffsetSetting = SettingsKey(floatPreferencesKey("keyboardOffset"), 0.0f)

fun adjustColorBrightnessForContrast(bgColor: Int, fgColor: Int, desiredContrast: Float, adjustSaturation: Boolean = false): Int {
    // Convert RGB colors to HSL
    val bgHSL = FloatArray(3)
    ColorUtils.colorToHSL(bgColor, bgHSL)
    val fgHSL = FloatArray(3)
    ColorUtils.colorToHSL(fgColor, fgHSL)

    // Estimate the adjustment needed in lightness to achieve the desired contrast
    // This is a simplified approach and may not be perfectly accurate
    val lightnessAdjustment = (desiredContrast - 1) / 10.0f // Simplified and heuristic-based adjustment

    // Adjust the background color's lightness
    bgHSL[2] = bgHSL[2] + lightnessAdjustment
    bgHSL[2] = bgHSL[2].coerceIn(0f, 1f) // Ensure the lightness stays within valid range

    if(adjustSaturation) {
        bgHSL[1] = (bgHSL[1] + lightnessAdjustment).coerceIn(0f, 1f)
    }

    // Convert back to RGB and return the adjusted color
    return ColorUtils.HSLToColor(bgHSL)
}

fun<T> Preferences.get(key: SettingsKey<T>): T {
    return this[key.key] ?: key.default
}

class BasicThemeProvider(
    val context: Context,
    val overrideColorScheme: ColorScheme? = null,
    val themeKey: String? = null
) : DynamicThemeProvider {
    val currentThemeKey: String = themeKey ?: context.getSettingBlocking(THEME_KEY)
    val isOrbitTheme: Boolean = currentThemeKey == "OrbitTheme"
    val isChaosTheme: Boolean = currentThemeKey == "OrbitChaos"
    val isRadarTheme: Boolean = currentThemeKey == "NothingRadarWave" || currentThemeKey == "GlyphRadarPulse"
    val isBurstTheme: Boolean = currentThemeKey == "MicroGlyphSpark" || currentThemeKey == "DotParticleBurst"
    val typingEffectPref: String = context.getSettingBlocking(KeyboardTypingEffectSetting)
    override val tapAnimationType: TapAnimationType get() = when (typingEffectPref) {
        "radar_wave" -> TapAnimationType.RADAR_WAVE
        "particle_burst" -> TapAnimationType.PARTICLE_BURST
        "constellation_web" -> TapAnimationType.CONSTELLATION_WEB
        else -> TapAnimationType.NONE
    }
    override val primaryKeyboardColor: Int
    override val keyColor: Int

    override val keyboardBackground: Drawable
    override val keyBackground: Drawable
    override val spaceBarBackground: Drawable

    override val keyFeedback: Drawable

    override val moreKeysTextColor: Int
    override val moreKeysKeyboardBackground: Drawable
    override val popupKey: Drawable
    override val actionPopupKey: Drawable

    private val colors: HashMap<Int, Int> = HashMap()
    override fun getColor(i: Int): Int? {
        return colors[i]
    }


    private val drawables: HashMap<Int, Drawable> = HashMap()
    override fun getDrawable(i: Int): Drawable? {
        return drawables[i]
    }

    val icons: HashMap<String, Drawable?> = hashMapOf()
    override fun getIcon(iconName: String): Drawable? {
        if(iconName == KeyboardIconsSet.ICON_UNDEFINED) return null

        if(!icons.containsKey(iconName)) {
            Log.e("BasicThemeProvider", "Unknown icon $iconName")
        }

        return icons[iconName]
    }

    override fun getKeyboardHeightMultiplier(): Float {
        return keyboardHeight
    }

    private fun dp(dp: Dp): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.value,
            context.resources.displayMetrics
        );
    }

    private fun coloredRectangle(@ColorInt color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(color)
        }
    }

    private fun coloredRoundedRectangle(@ColorInt color: Int, radius: Float, strokeWidth: Int = 0, @ColorInt strokeColor: Int = 0): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(color)
            if (strokeWidth > 0 && strokeColor != 0) {
                setStroke(strokeWidth, strokeColor)
            }
        }
    }

    private fun coloredOval(@ColorInt color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            cornerRadius = Float.MAX_VALUE
            setColor(color)
        }
    }

    private fun StateListDrawable.addStateWithHighlightLayerOnPressed(@ColorInt highlight: Int, stateSet: IntArray, drawable: Drawable, cornerRadius: Dp = 8.dp) {
        addState(intArrayOf(android.R.attr.state_pressed) + stateSet, LayerDrawable(
            arrayOf(
                drawable,
                coloredRoundedRectangle(highlight, dp(cornerRadius))
            )
        )
        )
        addState(stateSet, drawable)
    }

    val expertMode: Boolean
    val keyBorders: Boolean
    val showKeyHints: Boolean
    val keyPopupStyle: String
    val keyHintColor: String
    val keyboardHeight: Float
    val keyboardFont: String
    val spacebarColorPref: String
    val spacebarBorderPref: String
    val spacebarShapePref: String
    val spacebarTextColorPref: String
    val customAccentColorPref: String
    val customAccentColor: Color?
    val isNothingDarkTheme: Boolean = currentThemeKey == "NothingDark" || currentThemeKey == "AMOLEDNothing" || currentThemeKey == "DynamicNothing"
    val popupBgColor: Int
    val popupTextColor: Int
    val isDotFontActive: Boolean get() = (keyboardFont == "nothing" || keyboardFont == "orbit_dot" || keyboardFont.isEmpty())
    override val isDotFont: Boolean get() = isDotFontActive
    override val isQurovaFont: Boolean get() = (keyboardFont == "qurova")
    override val keyboardTypeface: Typeface?
    override val spacebarShape: String get() = spacebarShapePref
    override val spacebarTextColor: Int? get() = colors[R.styleable.MainKeyboardView_languageOnSpacebarTextColor]

    fun hasUpdated(np: Preferences): Boolean {
        return np.get(HiddenKeysSetting) != expertMode
                || np.get(KeyBordersSetting) != keyBorders
                || np.get(KeyHintsSetting) != showKeyHints
                || np.get(KeyPopupStyleSetting) != keyPopupStyle
                || np.get(KeyHintColorSetting) != keyHintColor
                || np.get(KeyboardHeightMultiplierSetting) != keyboardHeight
                || np.get(KeyboardFontSetting) != keyboardFont
                || np.get(SpacebarColorSetting) != spacebarColorPref
                || np.get(SpacebarBorderSetting) != spacebarBorderPref
                || np.get(SpacebarShapeSetting) != spacebarShapePref
                || np.get(SpacebarTextColorSetting) != spacebarTextColorPref
                || np.get(CustomAccentColorSetting) != customAccentColorPref
                || np.get(KeyboardTypingEffectSetting) != typingEffectPref
    }


    private fun addIcon(iconName: String, drawableIntResId: Int, tint: Int) {
        addIcon(iconName, AppCompatResources.getDrawable(
            context,
            drawableIntResId
        ), tint)
    }

    private fun addIcon(iconName: String, drawable: Drawable?, tint: Int) {
        icons[iconName] = drawable?.apply {
            setTint(tint)
        }
    }

    init {
        customAccentColorPref = context.getSettingBlocking(CustomAccentColorSetting)
        customAccentColor = when (customAccentColorPref) {
            "red" -> Color(0xFFD71921)
            "yellow" -> Color(0xFFE2B438)
            "blue" -> Color(0xFF3B72BA)
            "white" -> Color(0xFFEAE6E1)
            "green" -> Color(0xFF67FB59)
            "purple" -> Color(0xFFA855F7)
            "orange" -> Color(0xFFFF7A00)
            else -> null
        }
        val rawColorScheme = if(overrideColorScheme != null) {
            overrideColorScheme
        }else if(!DynamicColors.isDynamicColorAvailable()) {
            DarkColorScheme
        } else {
            val dCtx = DynamicColors.wrapContextIfAvailable(context)

            dynamicLightColorScheme(dCtx)
        }
        val colorScheme = if (customAccentColor != null) {
            rawColorScheme.copy(
                primary = customAccentColor,
                inversePrimary = customAccentColor
            )
        } else {
            rawColorScheme
        }

        expertMode = context.getSettingBlocking(HiddenKeysSetting)
        keyBorders = if (isChaosTheme) false else context.getSettingBlocking(KeyBordersSetting)
        showKeyHints = context.getSettingBlocking(KeyHintsSetting)
        keyPopupStyle = context.getSettingBlocking(KeyPopupStyleSetting)
        keyHintColor = context.getSettingBlocking(KeyHintColorSetting)
        keyboardHeight = context.getSettingBlocking(KeyboardHeightMultiplierSetting)
        keyboardFont = context.getSettingBlocking(KeyboardFontSetting)
        spacebarColorPref = context.getSettingBlocking(SpacebarColorSetting)
        spacebarBorderPref = context.getSettingBlocking(SpacebarBorderSetting)
        spacebarShapePref = context.getSettingBlocking(SpacebarShapeSetting)
        spacebarTextColorPref = context.getSettingBlocking(SpacebarTextColorSetting)
        val primary = colorScheme.primary.toArgb()
        val customSpaceColor = when (spacebarColorPref) {
            "theme" -> if (isChaosTheme) OrbitThemeHelper.COLOR_DARK else primary
            "red" -> OrbitThemeHelper.COLOR_RED
            "blue" -> OrbitThemeHelper.COLOR_BLUE
            "yellow" -> OrbitThemeHelper.COLOR_YELLOW
            "white" -> OrbitThemeHelper.COLOR_WHITE
            "dark" -> OrbitThemeHelper.COLOR_DARK
            "default" -> null
            else -> primary
        }
        OrbitThemeHelper.customSpacebarColor = customSpaceColor
        val customSpaceTextColor: Int? = when (spacebarTextColorPref) {
            "white" -> OrbitThemeHelper.COLOR_WHITE
            "yellow" -> OrbitThemeHelper.COLOR_YELLOW
            "red" -> OrbitThemeHelper.COLOR_RED
            "dark" -> OrbitThemeHelper.COLOR_TEXT_DARK
            "theme" -> primary
            "default" -> null
            else -> null
        }
        OrbitThemeHelper.customSpacebarTextColor = customSpaceTextColor
        keyboardTypeface = when (keyboardFont) {
            "normal" -> Typeface.DEFAULT
            "qurova" -> {
                try {
                    ResourcesCompat.getFont(context, R.font.qurova) ?: Typeface.DEFAULT
                } catch (e: Exception) {
                    Typeface.DEFAULT
                }
            }
            "agency" -> {
                try {
                    ResourcesCompat.getFont(context, R.font.agency) ?: Typeface.DEFAULT
                } catch (e: Exception) {
                    Typeface.DEFAULT
                }
            }
            "orbit_dot" -> {
                ResourcesCompat.getFont(context, R.font.ndot_family) ?: Typeface.DEFAULT
            }
            "nothing" -> {
                ResourcesCompat.getFont(context, R.font.ndot_family) ?: Typeface.DEFAULT
            }
            else -> {
                ResourcesCompat.getFont(context, R.font.ndot_family) ?: Typeface.DEFAULT
            }
        }

        val secondary = colorScheme.secondary.toArgb()
        val highlight = colorScheme.outline.copy(alpha = 0.33f).toArgb()

        val background = colorScheme.surface.toArgb()
        val surface = colorScheme.background.toArgb()
        val outline = colorScheme.outline.toArgb()

        val primaryContainer = colorScheme.primaryContainer.toArgb()
        val onPrimaryContainer = colorScheme.onPrimaryContainer.toArgb()

        val onPrimary = colorScheme.onPrimary.toArgb()
        val onPrimaryThird = colorScheme.onPrimary.copy(alpha = 0.33f).toArgb()
        val onSecondary = colorScheme.onSecondary.toArgb()
        val onBackground = colorScheme.onBackground.toArgb()
        val onBackgroundHalf = colorScheme.onBackground.copy(alpha = 0.5f).toArgb()
        val onBackgroundThird = colorScheme.onBackground.copy(alpha = 0.33f).toArgb()

        val transparent = Color.Transparent.toArgb()
        val yellowAccent = 0xFFFFCC00.toInt()
        primaryKeyboardColor = if(keyBorders) {
            colorScheme.background.toArgb()
        } else {
            colorScheme.surface.toArgb()
        }

        val ratio = 1.5f
        val keyColor = if(keyBorders) {
            when {
                isRadarTheme -> 0xFF1C1D21.toInt()
                isBurstTheme -> 0xFF16171B.toInt()
                else -> {
                    var c = adjustColorBrightnessForContrast(primaryKeyboardColor, primaryKeyboardColor, ratio)
                    if(c == primaryKeyboardColor) {
                        // May happen if the color is already 100% white
                        c = adjustColorBrightnessForContrast(primaryKeyboardColor, primaryKeyboardColor, 1.0f / (ratio / 2.0f + 0.5f))
                    }
                    c
                }
            }
        } else {
            transparent
        }

        this.keyColor = keyColor

        val functionalKeyColor = if(keyBorders) {
            when {
                isRadarTheme || isBurstTheme -> 0xFF23242A.toInt()
                else -> adjustColorBrightnessForContrast(primaryKeyboardColor, primaryKeyboardColor, ratio / 2.0f + 0.5f, adjustSaturation = true)
            }
        } else {
            transparent
        }

        val enterKeyBackground = if(expertMode) { functionalKeyColor } else { primary }
        val enterKeyForeground = if(expertMode) { onBackgroundThird } else { onPrimary }

        val hintColor = when (keyHintColor) {
            "yellow", "ochre" -> 0xFFE2B438.toInt()
            "red", "carmine" -> 0xFFC23636.toInt()
            "blue", "cobalt" -> 0xFF2A5288.toInt()
            "white" -> 0xFFFFFFFF.toInt()
            "orange" -> 0xFFFF7A00.toInt()
            "theme" -> primary
            else -> onBackgroundHalf
        }
        colors[R.styleable.Keyboard_Key_keyTextColor] = onBackground
        colors[R.styleable.Keyboard_Key_keyTextInactivatedColor] = onBackgroundHalf
        colors[R.styleable.Keyboard_Key_keyPressedTextColor] = onPrimary
        colors[R.styleable.Keyboard_Key_keyTextShadowColor] = 0
        colors[R.styleable.Keyboard_Key_actionKeyTextColor] = enterKeyForeground
        colors[R.styleable.Keyboard_Key_functionalTextColor] = onBackground
        colors[R.styleable.Keyboard_Key_keyHintLetterColor] = hintColor
        colors[R.styleable.Keyboard_Key_keyHintLabelColor] = hintColor
        colors[R.styleable.Keyboard_Key_keyShiftedLetterHintInactivatedColor] = hintColor
        popupBgColor = when {
            customAccentColor != null -> customAccentColor.toArgb()
            isChaosTheme -> 0xFF1C1D20.toInt()
            isNothingDarkTheme || isRadarTheme || isBurstTheme -> 0xFFD71921.toInt()
            else -> primary
        }
        popupTextColor = if (isChaosTheme && customAccentColor == null) 0xFFFFFFFF.toInt() else if (ColorUtils.calculateLuminance(popupBgColor) > 0.5) 0xFF1C1D20.toInt() else 0xFFFFFFFF.toInt()
        colors[R.styleable.Keyboard_Key_keyPreviewTextColor] = popupTextColor
        val defaultSpaceTextColor = if (isChaosTheme) 0xFFE2B438.toInt() else yellowAccent
        colors[R.styleable.MainKeyboardView_languageOnSpacebarTextColor] = customSpaceTextColor ?: defaultSpaceTextColor
        colors[R.styleable.MainKeyboardView_gestureTrailColor] = primary
        colors[R.styleable.MainKeyboardView_slidingKeyInputPreviewColor] = primary

        addIcon(KeyboardIconsSet.NAME_SHIFT_KEY, R.drawable.nth_shift, onBackground)
        addIcon(KeyboardIconsSet.NAME_SHIFT_KEY_SHIFTED, R.drawable.nth_shifted, onBackground)
        addIcon(KeyboardIconsSet.NAME_DELETE_KEY, R.drawable.nth_delete, onBackground)
        addIcon(KeyboardIconsSet.NAME_SETTINGS_KEY, R.drawable.settings, onBackground)
        addIcon(KeyboardIconsSet.NAME_SHORTCUT_KEY, R.drawable.mic, onBackground)
        addIcon(KeyboardIconsSet.NAME_SPACE_KEY, null, onBackground)
        addIcon(KeyboardIconsSet.NAME_ENTER_KEY, R.drawable.nth_enter, enterKeyForeground)
        addIcon(KeyboardIconsSet.NAME_GO_KEY, R.drawable.sym_keyboard_go_lxx_light, enterKeyForeground)
        addIcon(KeyboardIconsSet.NAME_SEARCH_KEY, R.drawable.nth_search, enterKeyForeground)
        addIcon(KeyboardIconsSet.NAME_SEND_KEY, R.drawable.sym_keyboard_send_lxx_light, enterKeyForeground)
        addIcon(KeyboardIconsSet.NAME_NEXT_KEY, R.drawable.sym_keyboard_next_lxx_light, enterKeyForeground)
        addIcon(KeyboardIconsSet.NAME_DONE_KEY, R.drawable.sym_keyboard_done_lxx_light, enterKeyForeground)
        addIcon(KeyboardIconsSet.NAME_PREVIOUS_KEY, R.drawable.sym_keyboard_previous_lxx_light, enterKeyForeground)
        addIcon(KeyboardIconsSet.NAME_TAB_KEY, R.drawable.sym_keyboard_tab_holo_dark, onBackground) // TODO: Correct tint
        addIcon(KeyboardIconsSet.NAME_ZWNJ_KEY, R.drawable.sym_keyboard_zwnj_lxx_dark, onBackground)
        addIcon(KeyboardIconsSet.NAME_ZWJ_KEY, R.drawable.sym_keyboard_zwj_lxx_dark, onPrimary)

        addIcon(KeyboardIconsSet.NAME_EMOJI_ACTION_KEY, R.drawable.nth_smile, onPrimary)
        addIcon(KeyboardIconsSet.NAME_EMOJI_NORMAL_KEY, R.drawable.nth_smile, onBackground)

        // Add by name (action_emoji)
        AllActionsMap.forEach { (i, it) ->
            addIcon("action_${i}", it.icon, onBackground)
        }

        // Add by id (action_0)
        AllActions.forEachIndexed { i, it ->
            addIcon("action_${i}", it.icon, onBackground)
        }

        if(!showKeyHints) {
            colors[R.styleable.Keyboard_Key_keyHintLetterColor] = transparent
            colors[R.styleable.Keyboard_Key_keyHintLabelColor] = transparent
            colors[R.styleable.Keyboard_Key_keyShiftedLetterHintInactivatedColor] = transparent
            colors[R.styleable.Keyboard_Key_keyShiftedLetterHintActivatedColor] = transparent
        }

        if(expertMode) {
            colors[R.styleable.Keyboard_Key_keyTextColor] = transparent
            colors[R.styleable.Keyboard_Key_keyTextInactivatedColor] = transparent
            colors[R.styleable.Keyboard_Key_keyHintLetterColor] = transparent
            colors[R.styleable.Keyboard_Key_keyHintLabelColor] = transparent


            // Note: We don't fully hide some things, but fade them away as they may be important landmarks
            colors[R.styleable.Keyboard_Key_functionalTextColor] = onBackgroundThird
        }

        keyboardBackground = when {
            isChaosTheme -> coloredRectangle(0xFF000000.toInt())
            isOrbitTheme -> coloredRectangle(0xFF000000.toInt())
            isRadarTheme -> coloredRectangle(0xFF000000.toInt())
            isBurstTheme -> coloredRectangle(0xFF000000.toInt())
            else -> coloredRectangle(primaryKeyboardColor)
        }

        val keyStrokeWidth = if (keyBorders && (isRadarTheme || isBurstTheme)) dp(1.dp).roundToInt() else 0
        val keyStrokeColor = when {
            !keyBorders -> 0
            isRadarTheme -> 0xFF28282E.toInt()
            isBurstTheme -> 0xFF27282F.toInt()
            else -> 0
        }

        keyBackground = StateListDrawable().apply {
            addStateWithHighlightLayerOnPressed(highlight, intArrayOf(android.R.attr.state_active),
                coloredRoundedRectangle(enterKeyBackground, dp(128.dp)),
                cornerRadius = 128.dp
            )

            addStateWithHighlightLayerOnPressed(highlight, intArrayOf(android.R.attr.state_checkable, android.R.attr.state_checked),
                coloredRoundedRectangle(colorScheme.secondaryContainer.toArgb(), dp(8.dp))
            )

            addStateWithHighlightLayerOnPressed(highlight, intArrayOf(android.R.attr.state_checkable),
                if(keyBorders) {
                    coloredRoundedRectangle(keyColor, dp(8.dp), keyStrokeWidth, keyStrokeColor)
                } else {
                    coloredRectangle(transparent)
                }
            )

            addStateWithHighlightLayerOnPressed(highlight, intArrayOf(android.R.attr.state_first),
                if(keyBorders) {
                    coloredRoundedRectangle(functionalKeyColor, dp(8.dp), keyStrokeWidth, keyStrokeColor)
                } else {
                    coloredRectangle(transparent)
                }
            )

            addStateWithHighlightLayerOnPressed(highlight, intArrayOf(android.R.attr.state_empty),
                if(keyBorders) {
                    coloredRoundedRectangle(keyColor, dp(8.dp), keyStrokeWidth, keyStrokeColor)
                } else {
                    coloredRectangle(transparent)
                }
            )

            addStateWithHighlightLayerOnPressed(highlight, intArrayOf(),
                if(keyBorders) {
                    coloredRoundedRectangle(keyColor, dp(8.dp), keyStrokeWidth, keyStrokeColor)
                } else {
                    coloredRectangle(transparent)
                }
            )
        }

        val spaceCornerRadius = when (spacebarShapePref) {
            "roundy" -> 48.dp
            "boxy" -> 8.dp
            else -> if(keyBorders) 8.dp else 48.dp
        }
        OrbitThemeHelper.customSpacebarCornerRadius = dp(spaceCornerRadius)
        OrbitThemeHelper.spacebarShape = spacebarShapePref

        val strokeColor: Int? = when (spacebarBorderPref) {
            "accent" -> primary
            "yellow" -> yellowAccent
            "red" -> 0xFFD71921.toInt()
            "white" -> 0xFFFFFFFF.toInt()
            "none" -> null
            else -> null
        }
        OrbitThemeHelper.customSpacebarBorderColor = strokeColor
        OrbitThemeHelper.customSpacebarBorderWidth = if (strokeColor != null) dp(1.5.dp).roundToInt() else 0

        val effectiveSpaceColor = customSpaceColor ?: (if(keyBorders) keyColor else highlight)
        val spaceDrawable = if(customSpaceColor != null) {
            coloredRoundedRectangle(effectiveSpaceColor, dp(spaceCornerRadius)).apply {
                if (strokeColor != null) setStroke(dp(1.5.dp).roundToInt(), strokeColor)
            }
        } else if(keyBorders) {
            coloredRoundedRectangle(keyColor, dp(spaceCornerRadius)).apply {
                if (strokeColor != null) setStroke(dp(1.5.dp).roundToInt(), strokeColor)
            }
        } else if(expertMode) {
            coloredRoundedRectangle(colorScheme.outline.copy(alpha = 0.1f).toArgb(), dp(spaceCornerRadius)).apply { if (strokeColor != null) setStroke(dp(1.5.dp).roundToInt(), strokeColor) }
        } else {
            coloredRoundedRectangle(highlight, dp(spaceCornerRadius)).apply {
                if (strokeColor != null) setStroke(dp(1.5.dp).roundToInt(), strokeColor)
            }
        }

        spaceBarBackground = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed),
                LayerDrawable(
                    arrayOf(
                        spaceDrawable,
                        coloredRoundedRectangle(highlight, dp(spaceCornerRadius))
                    )
                )
            )
            addState(intArrayOf(),
                spaceDrawable
            )
        }

        keyFeedback = ShapeDrawable().apply {
            paint.color = popupBgColor
            shape = RoundRectShape(
                floatArrayOf(
                    dp(8.dp), dp(8.dp), dp(8.dp), dp(8.dp),
                    dp(8.dp), dp(8.dp), dp(8.dp), dp(8.dp),
                ), null, null
            )

            intrinsicWidth = dp(48.dp).roundToInt()
            intrinsicHeight = dp(24.dp).roundToInt()

            setPadding(0, 0, 0, dp(50.dp).roundToInt())
        }

        moreKeysTextColor = onPrimaryContainer
        moreKeysKeyboardBackground = coloredRoundedRectangle(primaryContainer, dp(8.dp))
        popupKey = StateListDrawable().apply {
            addStateWithHighlightLayerOnPressed(primary, intArrayOf(),
                coloredRoundedRectangle(primaryContainer, dp(8.dp))
            )
        }
        actionPopupKey = StateListDrawable().apply {
            addStateWithHighlightLayerOnPressed(primary, intArrayOf(),
                coloredRoundedRectangle(primaryContainer, dp(128.dp)),
                128.dp
            )
        }

    }


    private var cachedCornerRadius = -1f
    private var cachedPreviewWidth = 0
    private var cachedPreviewHeight = 0
    private var cachedPreviewPadding = 0

    private fun ensureDimensions() {
        if (cachedCornerRadius < 0f) {
            cachedCornerRadius = dp(8.dp)
            cachedPreviewWidth = dp(48.dp).roundToInt()
            cachedPreviewHeight = dp(24.dp).roundToInt()
            cachedPreviewPadding = dp(50.dp).roundToInt()
        }
    }

    override fun getKeyBackground(key: Key, isPressed: Boolean): Drawable? {
        if (isChaosTheme) {
            ensureDimensions()
            return ChaosThemeHelper.getKeyBackground(key, isPressed, cachedCornerRadius)
        }
        if (isOrbitTheme) {
            ensureDimensions()
            return OrbitThemeHelper.getKeyBackground(key, isPressed, cachedCornerRadius)
        }
        return null
    }

    override fun getKeyTextColor(key: Key): Int? {
        if (isChaosTheme) {
            return ChaosThemeHelper.getKeyTextColor(key)
        }
        if (isOrbitTheme) {
            return OrbitThemeHelper.getKeyTextColor(key)
        }
        if (isRadarTheme) {
            if (key.code == Constants.CODE_SWITCH_ALPHA_SYMBOL) {
                return colors[R.styleable.Keyboard_Key_actionKeyTextColor] ?: 0xFFD71921.toInt()
            }
        }
        if (isBurstTheme) {
            if (key.code == Constants.CODE_SWITCH_ALPHA_SYMBOL) {
                return 0xFFE2B438.toInt()
            }
        }
        return null
    }

    override fun getKeyHintColor(key: Key): Int? {
        if (!showKeyHints) return Color.Transparent.toArgb()
        if (isChaosTheme) {
            return ChaosThemeHelper.getKeyHintColor(key)
        }
        val customColor = when (keyHintColor) {
            "yellow", "ochre" -> 0xFFE2B438.toInt()
            "red", "carmine" -> 0xFFC23636.toInt()
            "blue", "cobalt" -> 0xFF2A5288.toInt()
            "white" -> 0xFFFFFFFF.toInt()
            "orange" -> 0xFFFF7A00.toInt()
            "theme" -> colors[R.styleable.Keyboard_Key_actionKeyTextColor] ?: 0xFFD71921.toInt()
            "subtle", "default" -> null
            else -> null
        }
        if (customColor != null) return customColor
        if (isOrbitTheme) {
            return OrbitThemeHelper.getKeyHintColor(key)
        }
        return colors[R.styleable.Keyboard_Key_keyHintLetterColor]
    }

    override fun getKeyPreviewBackground(key: Key): Drawable? {
        return getKeyPreviewBackground(key, null)
    }

    override fun getKeyPreviewTextColor(key: Key): Int? {
        if (customAccentColor != null) {
            return popupTextColor
        }
        if (isOrbitTheme) {
            return OrbitThemeHelper.getKeyTextColor(key)
        }
        return popupTextColor
    }

    override fun getKeyPreviewBackground(key: Key, existing: org.futo.inputmethod.keyboard.internal.KeyPopupDrawable?): Drawable? {
        ensureDimensions()
        val popupStyle = KeyPopupDrawable.parseStyle(keyPopupStyle)

        // Exact v1.2.3 Orbit Theme popup: Bauhaus color-matched ShapeDrawable when no custom accent override is active
        if (isOrbitTheme && customAccentColor == null && (keyPopupStyle == "default" || keyPopupStyle == "orbit" || popupStyle == KeyPopupDrawable.STYLE_ORBIT_SLIM)) {
            return OrbitThemeHelper.getKeyPreviewBackground(
                key,
                cachedCornerRadius,
                cachedPreviewWidth,
                cachedPreviewHeight,
                cachedPreviewPadding
            )
        }
        if (keyPopupStyle == "default" || keyPopupStyle == "orbit" || popupStyle == KeyPopupDrawable.STYLE_ORBIT_SLIM) {
            val popupColor = when {
                customAccentColor != null -> customAccentColor.toArgb()
                isOrbitTheme -> OrbitThemeHelper.getKeyBaseColor(key)
                isNothingDarkTheme || isRadarTheme || isBurstTheme || currentThemeKey.contains("Nothing") -> Color(0xFFD71921).toArgb()
                else -> popupBgColor
            }
            return ShapeDrawable(RoundRectShape(
                FloatArray(8) { cachedCornerRadius }, null, null
            )).apply {
                paint.color = popupColor
                intrinsicWidth = cachedPreviewWidth
                intrinsicHeight = cachedPreviewHeight
                setPadding(0, 0, 0, cachedPreviewPadding)
            }
        }

        val baseColor = when {
            customAccentColor != null -> customAccentColor.toArgb()
            isOrbitTheme -> OrbitThemeHelper.getKeyBaseColor(key)
            isChaosTheme -> ChaosThemeHelper.COLOR_DARK
            else -> popupBgColor
        }
        val isBaseLight = androidx.core.graphics.ColorUtils.calculateLuminance(baseColor) > 0.5f
        val strokeColor = when {
            isOrbitTheme -> 0x33FFFFFF.toInt()
            isBaseLight -> 0x26000000
            else -> 0x33FFFFFF.toInt()
        }
        val radius = cachedCornerRadius
        val styleEnum = when (popupStyle) {
            KeyPopupDrawable.STYLE_GBOARD_BUBBLE -> org.futo.inputmethod.keyboard.internal.KeyPopupStyle.GBOARD
            KeyPopupDrawable.STYLE_FLUID_CONTOUR -> org.futo.inputmethod.keyboard.internal.KeyPopupStyle.FLUID
            KeyPopupDrawable.STYLE_BOXY -> org.futo.inputmethod.keyboard.internal.KeyPopupStyle.BOXY
            else -> org.futo.inputmethod.keyboard.internal.KeyPopupStyle.SLIM
        }
        val drawable = existing ?: KeyPopupDrawable(context, styleEnum)
        drawable.reconfigure(
            newStyle = popupStyle,
            newFillColor = baseColor,
            newStrokeColor = strokeColor,
            newCornerRadius = radius,
            keyWidthPx = key.drawWidth,
            keyHeightPx = key.height
        )
        return drawable
    }

    override fun getKeyLabelOverride(key: Key, originalLabel: String): String {
        if (isChaosTheme) {
            return ChaosThemeHelper.getKeyLabelOverride(key, originalLabel)
        }
        return originalLabel
    }

    override fun getKeyRotation(key: Key): Float {
        if (isChaosTheme) {
            return ChaosThemeHelper.getKeyRotation(key)
        }
        return 0f
    }

    override fun getKeyHintCoordinates(key: Key, keyWidth: Int, keyHeight: Int, padding: Float): FloatArray? {
        if (isChaosTheme) {
            return ChaosThemeHelper.getKeyHintCoordinates(key, keyWidth, keyHeight, padding)
        }
        return null
    }

    override fun getSpacebarCornerRadius(): Float? = dp(when (spacebarShapePref) {
        "roundy" -> 48.dp
        "boxy" -> 8.dp
        else -> if (keyBorders) 8.dp else 48.dp
    })
}
