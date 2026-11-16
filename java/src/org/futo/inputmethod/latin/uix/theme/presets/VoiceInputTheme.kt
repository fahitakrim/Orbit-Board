package org.futo.inputmethod.latin.uix.theme.presets

import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.theme.ThemeOption
import org.futo.inputmethod.latin.uix.theme.selector.ThemePreview

private val VoicePrimary = Color(0xFFBB86FC)
private val VoiceBackground = Color(0xFF130B1E)
private val VoiceSurface = Color(0xFF1F1230)
private val VoiceSurfaceVariant = Color(0xFF2E1B4E)
private val VoiceOnPrimary = Color(0xFF000000)
private val VoiceOnBackground = Color(0xFFEDE7F6)
private val VoiceOnSurface = Color(0xFFEDE7F6)
private val VoiceSecondary = Color(0xFFD1C4E9)
private val VoiceOutline = Color(0xFF4A3568)

private val VoiceInputColorScheme = darkColorScheme(
    primary = VoicePrimary,
    onPrimary = VoiceOnPrimary,
    primaryContainer = VoiceSurfaceVariant,
    onPrimaryContainer = VoicePrimary,
    secondary = VoiceSecondary,
    onSecondary = VoiceBackground,
    secondaryContainer = VoiceSurfaceVariant,
    onSecondaryContainer = VoiceSecondary,
    tertiary = VoicePrimary,
    onTertiary = VoiceOnPrimary,
    tertiaryContainer = VoiceSurfaceVariant,
    onTertiaryContainer = VoicePrimary,
    background = VoiceBackground,
    onBackground = VoiceOnBackground,
    surface = VoiceSurface,
    onSurface = VoiceOnSurface,
    outline = VoiceOutline,
    surfaceVariant = VoiceSurfaceVariant,
    onSurfaceVariant = VoiceSecondary
)

val VoiceInputTheme = ThemeOption(
    dynamic = false,
    key = "VoiceInputTheme",
    name = R.string.voice_input_theme_name,
    available = { true }
) {
    VoiceInputColorScheme
}

@Composable
@Preview
private fun PreviewTheme() {
    ThemePreview(VoiceInputTheme)
}