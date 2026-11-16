package org.futo.inputmethod.latin.uix.theme

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material3.ColorScheme
import org.futo.inputmethod.latin.uix.theme.presets.AMOLEDDarkPurple
import org.futo.inputmethod.latin.uix.theme.presets.AMOLEDNothing
import org.futo.inputmethod.latin.uix.theme.presets.OrbitTheme
import org.futo.inputmethod.latin.uix.theme.presets.ChaosTheme
import org.futo.inputmethod.latin.uix.theme.presets.RetroBlocksTheme
import org.futo.inputmethod.latin.uix.theme.presets.BerryBlushTheme
import org.futo.inputmethod.latin.uix.theme.presets.TactileSlateTheme
import org.futo.inputmethod.latin.uix.theme.presets.NothingLight
import org.futo.inputmethod.latin.uix.theme.presets.NothingDark
import org.futo.inputmethod.latin.uix.theme.presets.ClassicMaterialDark
import org.futo.inputmethod.latin.uix.theme.presets.ClassicMaterialLight
import org.futo.inputmethod.latin.uix.theme.presets.CottonCandy
import org.futo.inputmethod.latin.uix.theme.presets.DeepSeaDark
import org.futo.inputmethod.latin.uix.theme.presets.DeepSeaLight
import org.futo.inputmethod.latin.uix.theme.presets.DynamicDarkTheme
import org.futo.inputmethod.latin.uix.theme.presets.DynamicLightTheme
import org.futo.inputmethod.latin.uix.theme.presets.DynamicNothing
import org.futo.inputmethod.latin.uix.theme.presets.Emerald
import org.futo.inputmethod.latin.uix.theme.presets.Snowfall
import org.futo.inputmethod.latin.uix.theme.presets.SteelGray
import org.futo.inputmethod.latin.uix.theme.presets.Sunflower
import org.futo.inputmethod.latin.uix.theme.presets.VoiceInputTheme

data class ThemeOption(
    val dynamic: Boolean,
    val key: String,
    @StringRes val name: Int,
    val available: (Context) -> Boolean,
    val obtainColors: (Context) -> ColorScheme,
)

val ThemeOptions = mapOf(
    OrbitTheme.key to OrbitTheme,
    ChaosTheme.key to ChaosTheme,
    NothingDark.key to NothingDark,
    RetroBlocksTheme.key to RetroBlocksTheme,
    BerryBlushTheme.key to BerryBlushTheme,
    TactileSlateTheme.key to TactileSlateTheme,
    DynamicNothing.key to DynamicNothing,
    AMOLEDNothing.key to AMOLEDNothing,
    NothingLight.key to NothingLight,
    AMOLEDDarkPurple.key to AMOLEDDarkPurple,

    ClassicMaterialDark.key to ClassicMaterialDark,
    ClassicMaterialLight.key to ClassicMaterialLight,

    Emerald.key to Emerald,
    CottonCandy.key to CottonCandy,
    Sunflower.key to Sunflower,
    Snowfall.key to Snowfall,
    SteelGray.key to SteelGray,

    DeepSeaDark.key to DeepSeaDark,
    DeepSeaLight.key to DeepSeaLight,

    DynamicDarkTheme.key to DynamicDarkTheme,
    DynamicLightTheme.key to DynamicLightTheme,

    "DynamicSystem" to DynamicNothing,
    "DynamicSystemTheme" to DynamicNothing,
    "DynamicDarkTheme" to DynamicDarkTheme,
    "DynamicLightTheme" to DynamicLightTheme,

    VoiceInputTheme.key to VoiceInputTheme
)

val ThemeOptionKeys = ThemeOptions.keys
