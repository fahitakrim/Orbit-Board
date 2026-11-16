package org.futo.inputmethod.latin.uix.settings.pages

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SpaceBar
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.ui.draw.alpha
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Offset
import org.futo.inputmethod.latin.uix.settings.ExpandableSettingsCard
import org.futo.inputmethod.latin.uix.settings.PillButton
import org.futo.inputmethod.latin.uix.settings.SegmentedControl
import org.futo.inputmethod.latin.uix.settings.SettingsDivider
import org.futo.inputmethod.latin.uix.settings.SettingsRow
import org.futo.inputmethod.latin.uix.settings.SettingsSurfaceVariant
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.futo.inputmethod.latin.ActiveSubtype
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.KeyBordersSetting
import org.futo.inputmethod.latin.uix.CustomAccentColorSetting
import org.futo.inputmethod.latin.uix.KeyboardBgColorSetting
import org.futo.inputmethod.latin.uix.KeyboardKeyColorSetting
import org.futo.inputmethod.latin.uix.KeyboardTextColorSetting
import org.futo.inputmethod.latin.uix.KeyHintColorSetting
import org.futo.inputmethod.latin.uix.KeyHintsSetting
import org.futo.inputmethod.latin.uix.KeyPopupStyleSetting
import org.futo.inputmethod.latin.uix.KeyboardBottomOffsetSetting
import org.futo.inputmethod.latin.uix.KeyboardFontSetting
import org.futo.inputmethod.latin.uix.KeyboardHeightMultiplierSetting
import org.futo.inputmethod.latin.settings.Settings
import org.futo.inputmethod.latin.uix.SpacebarBorderSetting
import org.futo.inputmethod.latin.uix.SpacebarColorSetting
import org.futo.inputmethod.latin.uix.SpacebarShapeSetting
import org.futo.inputmethod.latin.uix.SpacebarTextColorSetting
import org.futo.inputmethod.latin.uix.KeyboardTypingEffectSetting
import org.futo.inputmethod.latin.uix.LaserColorSetting
import org.futo.inputmethod.latin.uix.THEME_KEY
import org.futo.inputmethod.latin.uix.TextEditPopupActivity
import org.futo.inputmethod.latin.uix.adjustColorBrightnessForContrast
import org.futo.inputmethod.latin.uix.settings.NeuCard
import org.futo.inputmethod.latin.uix.settings.OrbitBoardHeader
import org.futo.inputmethod.latin.uix.settings.SectionHeader
import org.futo.inputmethod.latin.uix.settings.useDataStore
import org.futo.inputmethod.latin.uix.settings.useSharedPrefsBool
import org.futo.inputmethod.latin.uix.theme.DarkColorScheme
import org.futo.inputmethod.latin.uix.theme.Doto
import org.futo.inputmethod.latin.uix.theme.Inter
import org.futo.inputmethod.latin.uix.theme.Qurova
import org.futo.inputmethod.latin.uix.theme.Agency
import org.futo.inputmethod.latin.uix.theme.NothingRed
import org.futo.inputmethod.latin.uix.theme.ThemeOptions
import org.futo.inputmethod.latin.uix.theme.presets.DynamicDarkTheme
import org.futo.inputmethod.latin.uix.theme.presets.DynamicLightTheme
import org.futo.inputmethod.latin.uix.theme.presets.NothingDark
import org.futo.inputmethod.latin.uix.theme.presets.VoiceInputTheme
import kotlin.math.roundToInt

private val BackgroundColor = Color(0xFF0B0B0C)
private val CardBackground = Color(0xFF141416)
private val SurfaceVariant = Color(0xFF1B1B1F)
private val BorderColor = Color(0xFF232326)
private val DividerColor = Color(0xFF1E1E22)
private val TextSecondary = Color(0xFF8E8E93)

data class ThemePresetItem(
    val key: String,
    val name: String,
    val isDefault: Boolean = false,
    val isOrbit: Boolean = false,
    val colors: List<Color>
)

data class AccentColorOption(
    val key: String,
    val name: String,
    val color: Color?
)

data class SpacebarColorOption(
    val key: String,
    val name: String,
    val color: Color?
)

data class SpacebarBorderOption(
    val key: String,
    val name: String,
    val color: Color?
)

data class SpacebarTextColorOption(
    val key: String,
    val name: String,
    val color: Color?
)

data class HintColorOption(
    val key: String,
    val name: String,
    val color: Color?
)

data class FontOptionItem(
    val key: String,
    val name: String,
    val desc: String,
    val font: androidx.compose.ui.text.font.FontFamily
)

// Top-level hoisted static presets: Special (Orbit & Chaos) vs Regular
private val SPECIAL_THEME_PRESETS = listOf(
    ThemePresetItem(
        key = "OrbitTheme",
        name = "Orbit",
        isDefault = true,
        isOrbit = true,
        colors = listOf(
            Color(0xFFE2B438), // Yellow
            Color(0xFF2A5288), // Blue
            Color(0xFF1C1D20), // Dark
            Color(0xFFC23636), // Red
            Color(0xFFEAE6E1)  // White
        )
    ),
    ThemePresetItem(
        key = "OrbitChaos",
        name = "Chaos",
        isOrbit = true,
        colors = listOf(
            Color(0xFFE2B438),
            Color(0xFF3B72BA),
            Color(0xFF000000),
            Color(0xFFD71921),
            Color(0xFFEAE6E1)
        )
    ),
    ThemePresetItem(
        key = "RetroBlocks",
        name = "Bamboosa",
        isOrbit = true,
        colors = listOf(
            Color(0xFFFED700), // Cyber Yellow
            Color(0xFF96A03E), // Light Olive
            Color(0xFF4A614A), // Muted Forest
            Color(0xFF1F2420), // Dark Charcoal
            Color(0xFFF0EFE6)  // Warm Cream
        )
    ),
    ThemePresetItem(
        key = "BerryBlush",
        name = "Berry & Blush",
        isOrbit = true,
        colors = listOf(
            Color(0xFFF4C2C2), // Soft Pastel Blush Pink
            Color(0xFFB57C82), // Dusty Rose
            Color(0xFFD8BFD8), // Soft Lavender
            Color(0xFF5A192D), // Deep Berry
            Color(0xFFFDFBF7)  // Warm Cream
        )
    ),
    ThemePresetItem(
        key = "TactileSlate",
        name = "Frostbite",
        isOrbit = true,
        colors = listOf(
            Color(0xFFD0D2D6), // Metallic Silver
            Color(0xFFFFFFFF), // Sculpted White
            Color(0xFF404040), // Dark Charcoal Modifiers
            Color(0xFFD71921), // Nothing Red Backspace
            Color(0xFFB4B7BD)  // Drop Shadow Bevel
        )
    )
)

private val REGULAR_THEME_PRESETS = listOf(
    ThemePresetItem(
        key = "NothingDark",
        name = "Classic Dark",
        colors = listOf(
            Color(0xFF0D0C11),
            Color(0xFF1C1D20),
            Color(0xFF282830),
            Color(0xFFF1F1F1),
            Color(0xFFD71921)
        )
    ),
    ThemePresetItem(
        key = "AMOLEDNothing",
        name = "AMOLED",
        colors = listOf(
            Color(0xFF000000),
            Color(0xFF0A0A0C),
            Color(0xFF141418),
            Color(0xFFF1F1F1),
            Color(0xFFD71921)
        )
    ),
    ThemePresetItem(
        key = "NothingLight",
        name = "Classic Light",
        colors = listOf(
            Color(0xFFF5F5F5),
            Color(0xFFD5D5D5),
            Color(0xFF1C1D20),
            Color(0xFFE0E0E0),
            Color(0xFFD71921)
        )
    ),
    ThemePresetItem(
        key = "SteelGray",
        name = "Slate",
        colors = listOf(
            Color(0xFF18191A),
            Color(0xFF242526),
            Color(0xFF9AA0A6),
            Color(0xFFE4E6EB),
            Color(0xFF80868B)
        )
    ),
    ThemePresetItem(
        key = "DynamicNothing",
        name = "Dynamic nothing",
        colors = listOf(
            Color(0xFF1C1D20),
            Color(0xFF282830),
            Color(0xFF303038),
            Color(0xFF282830),
            Color(0xFFF2F2F5)
        )
    ),
    ThemePresetItem(
        key = "AMOLEDDarkPurple",
        name = "AMOLED purple",
        colors = listOf(
            Color(0xFF000000),
            Color(0xFF381E72),
            Color(0xFFD0BCFF),
            Color(0xFF4F378B),
            Color(0xFFE6E1E5)
        )
    ),
    ThemePresetItem(
        key = "ClassicMaterialDark",
        name = "Material dark",
        colors = listOf(
            Color(0xFF121212),
            Color(0xFF2C2C2C),
            Color(0xFFBB86FC),
            Color(0xFF03DAC6),
            Color(0xFFFFFFFF)
        )
    ),
    ThemePresetItem(
        key = "ClassicMaterialLight",
        name = "Material light",
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFF2F2F2),
            Color(0xFF6200EE),
            Color(0xFF03DAC6),
            Color(0xFF000000)
        )
    ),
    ThemePresetItem(
        key = "Emerald",
        name = "Emerald",
        colors = listOf(
            Color(0xFF0D1A0B),
            Color(0xFF3E4A39),
            Color(0xFF67FB59),
            Color(0xFF8CD97C),
            Color(0xFFDCE5D5)
        )
    ),
    ThemePresetItem(
        key = "CottonCandy",
        name = "Cotton candy",
        colors = listOf(
            Color(0xFFFFECF9),
            Color(0xFFF2DDEB),
            Color(0xFFEC5EB4),
            Color(0xFFFFA5F5),
            Color(0xFF21191F)
        )
    ),
    ThemePresetItem(
        key = "Sunflower",
        name = "Sunflower",
        colors = listOf(
            Color(0xFF2E2300),
            Color(0xFF5E4200),
            Color(0xFFFFB951),
            Color(0xFFFFDEAC),
            Color(0xFFFAF0E6)
        )
    ),
    ThemePresetItem(
        key = "Snowfall",
        name = "Snowfall",
        colors = listOf(
            Color(0xFF0F1A24),
            Color(0xFF243B53),
            Color(0xFF90CDF4),
            Color(0xFFDCEEFB),
            Color(0xFFF7FAFC)
        )
    ),
    ThemePresetItem(
        key = "DeepSeaDark",
        name = "Deep sea dark",
        colors = listOf(
            Color(0xFF001F24),
            Color(0xFF00363A),
            Color(0xFF4DD0E1),
            Color(0xFF80DEEA),
            Color(0xFFE0F7FA)
        )
    ),
    ThemePresetItem(
        key = "DeepSeaLight",
        name = "Deep sea light",
        colors = listOf(
            Color(0xFFE0F7FA),
            Color(0xFFB2EBF2),
            Color(0xFF00838F),
            Color(0xFF006064),
            Color(0xFF004D40)
        )
    ),
    ThemePresetItem(
        key = DynamicDarkTheme.key,
        name = "Dynamic dark",
        colors = listOf(
            Color(0xFF1A1C1E),
            Color(0xFF2E3133),
            Color(0xFF8CD0E5),
            Color(0xFFCCE8EE),
            Color(0xFFE2E2E5)
        )
    ),
    ThemePresetItem(
        key = DynamicLightTheme.key,
        name = "Dynamic light",
        colors = listOf(
            Color(0xFFF8F9FA),
            Color(0xFFE9ECEF),
            Color(0xFF006684),
            Color(0xFF202124),
            Color(0xFF5F6368)
        )
    ),
    ThemePresetItem(
        key = VoiceInputTheme.key,
        name = "Voice input",
        colors = listOf(
            Color(0xFF1A1128),
            Color(0xFF2E1B4E),
            Color(0xFFBB86FC),
            Color(0xFFD0BCFF),
            Color(0xFFFFFFFF)
        )
    )
)
val THEME_PRESETS: List<ThemePresetItem> = SPECIAL_THEME_PRESETS + REGULAR_THEME_PRESETS

data class CustomColorOptionItem(
    val key: String,
    val name: String,
    val color: Color?
)

private val CUSTOM_BG_COLOR_OPTIONS = listOf(
    CustomColorOptionItem("default", "Default / Theme", null),
    CustomColorOptionItem("amoled_black", "AMOLED Black", Color(0xFF000000)),
    CustomColorOptionItem("dark_charcoal", "Dark Charcoal", Color(0xFF232621)),
    CustomColorOptionItem("muted_forest", "Muted Forest", Color(0xFF4A644B)),
    CustomColorOptionItem("light_olive", "Light Olive", Color(0xFF9EA637)),
    CustomColorOptionItem("cyber_yellow", "Cyber Yellow", Color(0xFFFED700)),
    CustomColorOptionItem("warm_cream", "Warm Cream", Color(0xFFF0EFE6)),
    CustomColorOptionItem("crimson_red", "Crimson Red", Color(0xFFBA1A1A)),
    CustomColorOptionItem("dark_slate", "Dark Slate", Color(0xFF121316)),
    CustomColorOptionItem("charcoal_slate", "Charcoal Slate", Color(0xFF1E1F24)),
    CustomColorOptionItem("deep_navy", "Deep Navy", Color(0xFF0E1424)),
    CustomColorOptionItem("deep_purple", "Deep Purple", Color(0xFF1A1128)),
    CustomColorOptionItem("deep_teal", "Deep Teal", Color(0xFF002228)),
    CustomColorOptionItem("chalk_white", "Chalk White", Color(0xFFF5F5F7))
)

private val CUSTOM_KEY_COLOR_OPTIONS = listOf(
    CustomColorOptionItem("default", "Default / Theme", null),
    CustomColorOptionItem("dark_charcoal", "Dark Charcoal", Color(0xFF232621)),
    CustomColorOptionItem("muted_forest", "Muted Forest", Color(0xFF4A644B)),
    CustomColorOptionItem("light_olive", "Light Olive", Color(0xFF9EA637)),
    CustomColorOptionItem("cyber_yellow", "Cyber Yellow", Color(0xFFFED700)),
    CustomColorOptionItem("warm_cream", "Warm Cream", Color(0xFFF0EFE6)),
    CustomColorOptionItem("crimson_red", "Crimson Red", Color(0xFFBA1A1A)),
    CustomColorOptionItem("obsidian", "Obsidian", Color(0xFF151518)),
    CustomColorOptionItem("bauhaus_slate", "Bauhaus Slate", Color(0xFF23242A)),
    CustomColorOptionItem("deep_indigo", "Deep Indigo", Color(0xFF1E1E30)),
    CustomColorOptionItem("pure_black", "Pure Black", Color(0xFF000000)),
    CustomColorOptionItem("chalk_cream", "Chalk Cream", Color(0xFFE8E7E3)),
    CustomColorOptionItem("soft_light", "Soft Light", Color(0xFFF0F0F2))
)

private val CUSTOM_TEXT_COLOR_OPTIONS = listOf(
    CustomColorOptionItem("default", "Default / Theme", null),
    CustomColorOptionItem("white", "Crisp White", Color(0xFFFFFFFF)),
    CustomColorOptionItem("cyber_yellow", "Cyber Yellow", Color(0xFFFED700)),
    CustomColorOptionItem("warm_cream", "Warm Cream", Color(0xFFF0EFE6)),
    CustomColorOptionItem("light_olive", "Light Olive", Color(0xFF9EA637)),
    CustomColorOptionItem("muted_forest", "Muted Forest", Color(0xFF4A644B)),
    CustomColorOptionItem("dark_charcoal", "Dark Charcoal", Color(0xFF232621)),
    CustomColorOptionItem("crimson_red", "Crimson Red", Color(0xFFBA1A1A)),
    CustomColorOptionItem("cream", "Chalk Cream", Color(0xFFEAE6E1)),
    CustomColorOptionItem("yellow", "Bauhaus Ochre", Color(0xFFE2B438)),
    CustomColorOptionItem("red", "Nothing Red", Color(0xFFD71921)),
    CustomColorOptionItem("blue", "Cobalt Blue", Color(0xFF3B72BA)),
    CustomColorOptionItem("green", "Emerald Green", Color(0xFF67FB59)),
    CustomColorOptionItem("dark", "Dark Slate", Color(0xFF1C1D20))
)

private val ACCENT_COLOR_OPTIONS = listOf(
    AccentColorOption("default", "Default / Theme Accent", null),
    AccentColorOption("cyber_yellow", "Cyber Yellow", Color(0xFFFED700)),
    AccentColorOption("light_olive", "Light Olive", Color(0xFF9EA637)),
    AccentColorOption("muted_forest", "Muted Forest", Color(0xFF4A644B)),
    AccentColorOption("dark_charcoal", "Dark Charcoal", Color(0xFF232621)),
    AccentColorOption("warm_cream", "Warm Cream", Color(0xFFF0EFE6)),
    AccentColorOption("crimson_red", "Crimson Red", Color(0xFFBA1A1A)),
    AccentColorOption("red", "Nothing Red", Color(0xFFD71921)),
    AccentColorOption("yellow", "Bauhaus Ochre", Color(0xFFE2B438)),
    AccentColorOption("blue", "Cobalt Blue", Color(0xFF3B72BA)),
    AccentColorOption("white", "Chalk White", Color(0xFFEAE6E1)),
    AccentColorOption("green", "Emerald Green", Color(0xFF67FB59)),
    AccentColorOption("purple", "Violet Purple", Color(0xFFA855F7)),
    AccentColorOption("orange", "Warm Orange", Color(0xFFFF7A00))
)

private val FONT_OPTIONS = listOf(
    FontOptionItem(
        key = "nothing",
        name = "Orbit Dot",
        desc = "Classic Nothing dot-matrix typography",
        font = Doto
    ),
    FontOptionItem(
        key = "normal",
        name = "Normal",
        desc = "Inter / clean system font",
        font = Inter
    ),
    FontOptionItem(
        key = "qurova",
        name = "Qurova",
        desc = "Futuristic branding rounded sans",
        font = Qurova
    ),
    FontOptionItem(
        key = "agency",
        name = "Agency",
        desc = "Flared modern geometric",
        font = Agency
    )
)

private val POPUP_STYLE_OPTIONS = listOf(
    Triple("orbit_slim", "Orbit (Default)", "Classic floating squircle with hairline border"),
    Triple("gboard_bubble", "Gboard Bubble", "Compact floating circular bubble"),
    Triple("fluid_contour", "Fluid Contour", "Sleek smooth paddle contour merging into key"),
    Triple("boxy", "Boxy", "Sharp rectangular Bauhaus preview")
)

private val TYPING_EFFECT_OPTIONS = listOf(
    Triple("none", "None", "Off — Clean typing with no touch animation"),
    Triple("radar_wave", "Radar Wave", "Glyph Radar Pulse — 3 expanding dot-matrix rings with red spark core"),
    Triple("particle_burst", "Particle Burst", "Particle Burst — Radiating micro-dots in Bauhaus palette"),
    Triple("constellation_web", "Constellation Web", "Option B — Interconnected geometric constellation lines & glowing nodes"),
    Triple("laser_reticle", "Border Collision", "Gutter Flow — Organic neon electric energy coursing through key borders & crevices")
)

private fun getThemeDisplayName(themeKey: String): String = when (themeKey) {
    "OrbitTheme" -> "Orbit"
    "OrbitChaos" -> "Chaos"
    "RetroBlocks" -> "Bamboosa"
    "BerryBlush" -> "Berry & Blush"
    "TactileSlate" -> "Frostbite"
    "NothingDark" -> "Classic Dark"
    "DynamicNothing" -> "Dynamic"
    "AMOLEDNothing" -> "AMOLED"
    "NothingLight" -> "Classic Light"
    "SteelGray" -> "Slate"
    "AMOLEDDarkPurple" -> "AMOLED Purple"
    "ClassicMaterialDark" -> "Material Dark"
    "ClassicMaterialLight" -> "Material Light"
    "Emerald" -> "Emerald"
    "CottonCandy" -> "Cotton Candy"
    "Sunflower" -> "Sunflower"
    "Snowfall" -> "Snowfall"
    "SteelGray" -> "Steel Gray"
    "DeepSeaDark" -> "Deep Sea Dark"
    "DeepSeaLight" -> "Deep Sea Light"
    "DynamicDark", "DynamicDarkTheme" -> "Dynamic Dark"
    "DynamicLight", "DynamicLightTheme" -> "Dynamic Light"
    "VoiceInputTheme" -> "Voice Input"
    else -> themeKey
}

@Composable
private fun ColorSwatchBox(
    isSelected: Boolean,
    color: Color?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDefault = color == null
    val activeBorderColor = if (isDefault) {
        NothingRed
    } else if (color == NothingRed || color == Color(0xFFBA1A1A) || color == Color(0xFFD71921)) {
        Color.White
    } else {
        NothingRed
    }

    val boxBackground: Color = if (isDefault || color == null) Color.Transparent else color
    val unselectedBorderColor = if (isDefault) Color(0xFF6E6E78) else Color(0x33FFFFFF)
    val borderColor = if (isSelected) activeBorderColor else unselectedBorderColor
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Box(
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(boxBackground)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isDefault) {
            Canvas(modifier = Modifier.fillMaxSize().padding(7.dp)) {
                drawLine(
                    color = if (isSelected) activeBorderColor else Color(0xFF6E6E78),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
        }
    }
}

@Composable
fun ToggleSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = Inter,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontFamily = Inter,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NothingRed,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = SurfaceVariant
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun KeyboardCustomizationScreen(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val (currentThemeStored, setTheme) = useDataStore(THEME_KEY.key, THEME_KEY.default)
    var currentTheme by remember(currentThemeStored) { mutableStateOf(currentThemeStored) }
    val isSpecialTheme = SPECIAL_THEME_PRESETS.any { it.key == currentTheme }
    val (keyBorders, setKeyBorders) = useDataStore(KeyBordersSetting.key, KeyBordersSetting.default)
    val (customAccent, setCustomAccent) = useDataStore(CustomAccentColorSetting.key, CustomAccentColorSetting.default)
    val (keyHints, setKeyHints) = useDataStore(KeyHintsSetting.key, KeyHintsSetting.default)
    val (keyHintColor, setKeyHintColor) = useDataStore(KeyHintColorSetting.key, KeyHintColorSetting.default)
    val (keyboardFont, setKeyboardFont) = useDataStore(KeyboardFontSetting.key, KeyboardFontSetting.default)
    val (popupStyle, setPopupStyle) = useDataStore(KeyPopupStyleSetting.key, KeyPopupStyleSetting.default)
    val (heightMultiplier, setHeightMultiplier) = useDataStore(KeyboardHeightMultiplierSetting.key, KeyboardHeightMultiplierSetting.default)
    val (bottomOffset, setBottomOffset) = useDataStore(KeyboardBottomOffsetSetting.key, KeyboardBottomOffsetSetting.default)

    val (spacebarShape, setSpacebarShape) = useDataStore(SpacebarShapeSetting.key, SpacebarShapeSetting.default)
    val (spacebarColorKey, setSpacebarColorKey) = useDataStore(SpacebarColorSetting.key, SpacebarColorSetting.default)
    val (spacebarBorderKey, setSpacebarBorderKey) = useDataStore(SpacebarBorderSetting.key, SpacebarBorderSetting.default)
    val (spacebarTextColorKey, setSpacebarTextColorKey) = useDataStore(SpacebarTextColorSetting.key, SpacebarTextColorSetting.default)
    val (keyboardBgColor, setKeyboardBgColor) = useDataStore(KeyboardBgColorSetting.key, KeyboardBgColorSetting.default)
    val (keyboardKeyColor, setKeyboardKeyColor) = useDataStore(KeyboardKeyColorSetting.key, KeyboardKeyColorSetting.default)
    val (keyboardTextColor, setKeyboardTextColor) = useDataStore(KeyboardTextColorSetting.key, KeyboardTextColorSetting.default)
    val (typingEffect, setTypingEffect) = useDataStore(KeyboardTypingEffectSetting.key, KeyboardTypingEffectSetting.default)
    val (laserColor, setLaserColor) = useDataStore(LaserColorSetting.key, LaserColorSetting.default)
    val (isPinSpacebarActive, setPinSpacebarActive) = useSharedPrefsBool(Settings.PREF_PIN_KEYBOARD_NAME_ON_SPACEBAR, true)
    val (isNumberRowActive, _) = useSharedPrefsBool(Settings.PREF_ENABLE_NUMBER_ROW, false)
    val (activeSubtype, _) = useDataStore(ActiveSubtype.key, ActiveSubtype.default)
    val activeLayoutDisplayName = remember(activeSubtype) {
        when {
            activeSubtype.contains("bengali_avro") -> "Avro"
            activeSubtype.contains("bengali_probhat") -> "Probhat"
            activeSubtype.contains("bengali_jatiyo_classic") -> "Jatiyo Classic"
            activeSubtype.contains("bengali_jatiyo") -> "Jatiyo"
            activeSubtype.contains("bengali_akkhor") -> "Bangla"
            else -> "English"
        }
    }

    var expandedCardIndex by rememberSaveable { mutableStateOf<Int?>(0) }
    var typingTestText by remember { mutableStateOf("") }

    val customAccentOverride: Color? = remember(customAccent) {
        when (customAccent) {
            "cyber_yellow" -> Color(0xFFFED700)
            "light_olive" -> Color(0xFF9EA637)
            "muted_forest" -> Color(0xFF4A644B)
            "dark_charcoal" -> Color(0xFF232621)
            "warm_cream" -> Color(0xFFF0EFE6)
            "crimson_red" -> Color(0xFFBA1A1A)
            "red" -> Color(0xFFD71921)
            "yellow" -> Color(0xFFE2B438)
            "blue" -> Color(0xFF3B72BA)
            "white" -> Color(0xFFEAE6E1)
            "green" -> Color(0xFF67FB59)
            "purple" -> Color(0xFFA855F7)
            "orange" -> Color(0xFFFF7A00)
            else -> null
        }
    }

    val currentThemeOption = ThemeOptions[currentTheme] ?: NothingDark
    val rawThemeColors = remember(currentTheme, context) {
        try {
            currentThemeOption.obtainColors(context)
        } catch (e: Exception) {
            DarkColorScheme
        }
    }
    val currentThemeColors = remember(rawThemeColors, customAccentOverride) {
        if (customAccentOverride != null) {
            rawThemeColors.copy(
                primary = customAccentOverride,
                inversePrimary = customAccentOverride
            )
        } else {
            rawThemeColors
        }
    }

    val currentSpaceColor = remember(spacebarColorKey, currentThemeColors, keyBorders, currentTheme) {
        when (spacebarColorKey) {
            "theme" -> when (currentTheme) {
                "RetroBlocks" -> Color(0xFF000000)
                "OrbitChaos" -> Color(0xFF141416)
                "OrbitTheme" -> Color(0xFF1C1D20)
                "BerryBlush" -> Color(0xFF5A192D)
                "TactileSlate" -> Color(0xFFFFFFFF)
                else -> currentThemeColors.primary
            }
            "cyber_yellow" -> Color(0xFFFED700)
            "light_olive" -> Color(0xFF9EA637)
            "muted_forest" -> Color(0xFF4A644B)
            "dark_charcoal" -> Color(0xFF232621)
            "warm_cream" -> Color(0xFFF0EFE6)
            "crimson_red" -> Color(0xFFBA1A1A)
            "red" -> Color(0xFFC23636)
            "blue" -> Color(0xFF2A5288)
            "yellow" -> Color(0xFFE2B438)
            "white" -> Color(0xFFEAE6E1)
            "dark" -> Color(0xFF1C1D20)
            "default" -> if (keyBorders) {
                if (currentThemeColors.surfaceVariant != currentThemeColors.surface) currentThemeColors.surfaceVariant
                else Color(adjustColorBrightnessForContrast(currentThemeColors.background.toArgb(), currentThemeColors.background.toArgb(), 1.5f))
            } else currentThemeColors.outline.copy(alpha = 0.33f)
            else -> currentThemeColors.primary
        }
    }

    val currentSpaceBorder = remember(spacebarBorderKey, currentThemeColors.primary, currentTheme) {
        when (spacebarBorderKey) {
            "none" -> when (currentTheme) {
                "RetroBlocks" -> Color(0xFFFED700)
                "OrbitChaos" -> Color(0x33FFFFFF)
                else -> null
            }
            "border_none" -> null
            "accent" -> currentThemeColors.primary
            "yellow", "yellow_dotted" -> Color(0xFFFFCC00)
            "red" -> Color(0xFFD71921)
            "white" -> Color(0xFFFFFFFF)
            "cyber_yellow" -> Color(0xFFFED700)
            "light_olive" -> Color(0xFF9EA637)
            "muted_forest" -> Color(0xFF4A644B)
            "dark_charcoal" -> Color(0xFF232621)
            "warm_cream" -> Color(0xFFF0EFE6)
            "crimson_red" -> Color(0xFFBA1A1A)
            else -> null
        }
    }

    val currentSpaceTextColor = remember(spacebarTextColorKey, currentThemeColors.primary, currentTheme) {
        when (spacebarTextColorKey) {
            "theme", "default" -> when (currentTheme) {
                "RetroBlocks" -> Color(0xFFFED700)
                "OrbitChaos", "OrbitTheme" -> Color(0xFFE2B438)
                "BerryBlush" -> Color(0xFFFFFFFF)
                "TactileSlate" -> Color(0xFF1C1D20)
                else -> null
            }
            "white" -> Color(0xFFFFFFFF)
            "yellow" -> Color(0xFFE2B438)
            "red" -> Color(0xFFD71921)
            "dark" -> Color(0xFF1C1D20)
            "cyber_yellow" -> Color(0xFFFED700)
            "light_olive" -> Color(0xFF9EA637)
            "muted_forest" -> Color(0xFF4A644B)
            "dark_charcoal" -> Color(0xFF232621)
            "warm_cream" -> Color(0xFFF0EFE6)
            "crimson_red" -> Color(0xFFBA1A1A)
            else -> null
        }
    }

    val spaceColorOptions = remember(currentThemeColors) {
        listOf(
            SpacebarColorOption("theme", "Theme Default", null),
            SpacebarColorOption("cyber_yellow", "Cyber Yellow", Color(0xFFFED700)),
            SpacebarColorOption("light_olive", "Olive", Color(0xFF9EA637)),
            SpacebarColorOption("muted_forest", "Forest", Color(0xFF4A644B)),
            SpacebarColorOption("dark_charcoal", "Charcoal", Color(0xFF232621)),
            SpacebarColorOption("warm_cream", "Cream", Color(0xFFF0EFE6)),
            SpacebarColorOption("crimson_red", "Crimson", Color(0xFFBA1A1A)),
            SpacebarColorOption("default", "Classic Outline", currentThemeColors.outline.copy(alpha = 0.33f)),
            SpacebarColorOption("dark", "Dark Slate", Color(0xFF1C1D20)),
            SpacebarColorOption("white", "Chalk White", Color(0xFFEAE6E1)),
            SpacebarColorOption("yellow", "Mustard Yellow", Color(0xFFE2B438)),
            SpacebarColorOption("red", "Crimson Red", Color(0xFFC23636)),
            SpacebarColorOption("blue", "Cobalt Blue", Color(0xFF2A5288))
        )
    }

    val spaceBorderOptions = remember(currentThemeColors.primary) {
        listOf(
            SpacebarBorderOption("none", "Theme Default", null),
            SpacebarBorderOption("border_none", "None", null),
            SpacebarBorderOption("accent", "Accent", currentThemeColors.primary),
            SpacebarBorderOption("yellow_dotted", "Yellow Dotted", Color(0xFFFFCC00)),
            SpacebarBorderOption("red", "Red", Color(0xFFD71921)),
            SpacebarBorderOption("white", "White", Color(0xFFFFFFFF)),
            SpacebarBorderOption("cyber_yellow", "Cyber Yellow", Color(0xFFFED700)),
            SpacebarBorderOption("light_olive", "Light Olive", Color(0xFF9EA637)),
            SpacebarBorderOption("muted_forest", "Muted Forest", Color(0xFF4A644B)),
            SpacebarBorderOption("dark_charcoal", "Dark Charcoal", Color(0xFF232621)),
            SpacebarBorderOption("warm_cream", "Warm Cream", Color(0xFFF0EFE6)),
            SpacebarBorderOption("crimson_red", "Crimson Red", Color(0xFFBA1A1A))
        )
    }

    val spaceTextColorOptions = remember(currentThemeColors.primary) {
        listOf(
            SpacebarTextColorOption("theme", "Theme Default", null),
            SpacebarTextColorOption("white", "Crisp White", Color(0xFFFFFFFF)),
            SpacebarTextColorOption("yellow", "Bauhaus Yellow", Color(0xFFE2B438)),
            SpacebarTextColorOption("red", "Red", Color(0xFFD71921)),
            SpacebarTextColorOption("dark", "Slate", Color(0xFF1C1D20)),
            SpacebarTextColorOption("cyber_yellow", "Cyber Yellow", Color(0xFFFED700)),
            SpacebarTextColorOption("light_olive", "Light Olive", Color(0xFF9EA637)),
            SpacebarTextColorOption("muted_forest", "Muted Forest", Color(0xFF4A644B)),
            SpacebarTextColorOption("dark_charcoal", "Dark Charcoal", Color(0xFF232621)),
            SpacebarTextColorOption("warm_cream", "Warm Cream", Color(0xFFF0EFE6)),
            SpacebarTextColorOption("crimson_red", "Crimson Red", Color(0xFFBA1A1A)),
            SpacebarTextColorOption("default", "Subtle Gray", Color(0xFF8E8E93))
        )
    }

    val hintColorOptions = remember(currentThemeColors.primary) {
        listOf(
            HintColorOption("theme", "Default / Theme Accent", null),
            HintColorOption("cyber_yellow", "Cyber Yellow", Color(0xFFFED700)),
            HintColorOption("light_olive", "Light Olive", Color(0xFF9EA637)),
            HintColorOption("muted_forest", "Muted Forest", Color(0xFF4A644B)),
            HintColorOption("dark_charcoal", "Dark Charcoal", Color(0xFF232621)),
            HintColorOption("warm_cream", "Warm Cream", Color(0xFFF0EFE6)),
            HintColorOption("crimson_red", "Crimson Red", Color(0xFFBA1A1A)),
            HintColorOption("subtle", "Subtle Gray", Color(0xFF8E8E93)),
            HintColorOption("yellow", "Ochre Yellow", Color(0xFFE2B438)),
            HintColorOption("red", "Carmine Red", Color(0xFFC23636)),
            HintColorOption("blue", "Cobalt Blue", Color(0xFF2A5288)),
            HintColorOption("white", "Crisp White", Color(0xFFFFFFFF)),
            HintColorOption("orange", "Warm Orange", Color(0xFFFF7A00))
        )
    }

    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
    val isImeVisible = imeBottom > 0



    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        // Interactive Preview Area (Fixed at Top, Non-Scrolling)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundColor)
        ) {
            Spacer(modifier = Modifier.height(6.dp))
            OrbitBoardHeader()
            Spacer(modifier = Modifier.height(2.dp))
            RealisticKeyboardPreview(
                currentTheme = currentTheme,
                spacebarColor = currentSpaceColor,
                spacebarBorder = currentSpaceBorder,
                spacebarShape = spacebarShape,
                spacebarTextColor = currentSpaceTextColor,
                hasKeyBorders = keyBorders,
                showSuggestionRow = true,
                showNumberRow = isNumberRowActive,
                pinLayoutNameOnSpacebar = isPinSpacebarActive,
                showEmojiKey = true,
                showKeyHints = keyHints,
                keyHintColor = keyHintColor,
                customAccentColor = customAccent,
                keyboardFont = keyboardFont,
                keyboardBgColor = keyboardBgColor,
                keyboardKeyColor = keyboardKeyColor,
                keyboardTextColor = keyboardTextColor,
                typingEffect = typingEffect,
                laserColor = laserColor,
                activeLayoutName = activeLayoutDisplayName,
                heightMultiplier = heightMultiplier,
                bottomOffset = bottomOffset,
                modifier = Modifier.padding(horizontal = 16.dp),
                spacebarColorKey = spacebarColorKey,
                spacebarBorderKey = spacebarBorderKey,
                spacebarTextColorKey = spacebarTextColorKey
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Subordinate Scrollable Customization Content Below
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .then(
                    if (!isImeVisible) {
                        Modifier.navigationBarsPadding().padding(bottom = 160.dp)
                    } else {
                        Modifier.padding(bottom = 12.dp)
                    }
                )
        ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Card 1: Themes & Color Presets
        ExpandableSettingsCard(
            title = "Themes & Color Presets",
            summary = "${getThemeDisplayName(currentTheme)} • Accent: ${customAccent.replaceFirstChar { it.uppercase() }}",
            isExpanded = expandedCardIndex == 0,
            onToggle = { expandedCardIndex = if (expandedCardIndex == 0) null else 0 },
            icon = Icons.Rounded.Palette,
            accentColor = NothingRed,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Base Theme Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Base Theme",
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (currentTheme == "OrbitTheme") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceVariant)
                                    .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("O", fontFamily = Doto, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFE2B438))
                                Text("r", fontFamily = Doto, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NothingRed)
                                Text("bit", fontFamily = Doto, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            }
                        } else {
                            val displayName = getThemeDisplayName(currentTheme)
                            Text(
                                text = displayName,
                                fontFamily = Doto,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceVariant)
                                    .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceVariant)
                                .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                                .clickable { navController.navigate("themes") }
                                .padding(horizontal = 6.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "All Themes",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                var themePresetCategory by remember(currentTheme) {
                    mutableStateOf(if (currentTheme in listOf("OrbitTheme", "OrbitChaos", "RetroBlocks", "BerryBlush", "TactileSlate")) "Special" else "Regular")
                }
                var customStudioExpanded by rememberSaveable { mutableStateOf(false) }

                SegmentedControl(
                    options = listOf("Regular", "Special"),
                    selectedIndex = if (themePresetCategory == "Special") 1 else 0,
                    onSelect = {
                        themePresetCategory = if (it == 1) "Special" else "Regular"
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    equalWeight = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (themePresetCategory == "Regular") {
                    // Regular theme presets
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(REGULAR_THEME_PRESETS) { item ->
                            val isSelected = item.key == currentTheme

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    setCustomAccent("default")
                                    setKeyboardBgColor("default")
                                    setKeyboardKeyColor("default")
                                    setKeyboardTextColor("default")
                                    setSpacebarColorKey("theme")
                                    setSpacebarBorderKey("none")
                                    setSpacebarShape("boxy")
                                    currentTheme = item.key
                                    setTheme(item.key)
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(96.dp)
                                        .height(50.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CardBackground)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) NothingRed else BorderColor,
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        item.colors.forEach { swatchColor ->
                                            Box(
                                                modifier = Modifier
                                                    .width(8.dp)
                                                    .height(26.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(swatchColor)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = item.name,
                                    fontFamily = Inter,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Collapsible Custom Studio
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF141518))
                            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { customStudioExpanded = !customStudioExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Custom Studio",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (customStudioExpanded) "Tap to collapse" else "Borders, Accent & Surface Colors",
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            PillButton(
                                text = if (customStudioExpanded) "Hide" else "Customize",
                                onClick = { customStudioExpanded = !customStudioExpanded },
                                filled = customStudioExpanded
                            )
                        }

                        if (customStudioExpanded) {
                            Spacer(modifier = Modifier.height(10.dp))
                            SettingsDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            if (isSpecialTheme) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF1B1B1F))
                                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Lock,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.85f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Select a Regular theme to customize colors",
                                            fontFamily = Inter,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            Column(modifier = if (isSpecialTheme) Modifier.alpha(0.38f) else Modifier) {
                                // 1. Key borders switch
                                SettingsRow(
                                    horizontalPadding = 0.dp,
                                    title = "Key borders",
                                    subtitle = "",
                                    trailingContent = {
                                        Switch(
                                            checked = keyBorders,
                                            onCheckedChange = { if (!isSpecialTheme) setKeyBorders(it) },
                                            enabled = !isSpecialTheme,
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = NothingRed,
                                                uncheckedThumbColor = TextSecondary,
                                                uncheckedTrackColor = SurfaceVariant
                                            )
                                        )
                                    }
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                SettingsDivider()
                                Spacer(modifier = Modifier.height(8.dp))

                                // 2. Primary Accent Color
                                Text(
                                    text = "Primary Accent",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = Color(0xFFDCDCE0)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(ACCENT_COLOR_OPTIONS) { option ->
                                        val isSelected = customAccent == option.key ||
                                                (option.key == "default" && (customAccent.isEmpty() || customAccent == "default"))
                                        ColorSwatchBox(
                                            isSelected = isSelected,
                                            color = option.color,
                                            onClick = { if (!isSpecialTheme) setCustomAccent(option.key) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // 1. Keyboard Background Color
                                Text(
                                    text = "Keyboard Background",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = Color(0xFFDCDCE0)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(CUSTOM_BG_COLOR_OPTIONS) { opt ->
                                        val isSelected = keyboardBgColor == opt.key ||
                                                (opt.key == "default" && (keyboardBgColor.isEmpty() || keyboardBgColor == "default"))
                                        ColorSwatchBox(
                                            isSelected = isSelected,
                                            color = opt.color,
                                            onClick = { if (!isSpecialTheme) setKeyboardBgColor(opt.key) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // 2. Key Surface Fill Color
                                Text(
                                    text = "Key Background",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = Color(0xFFDCDCE0)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(CUSTOM_KEY_COLOR_OPTIONS) { opt ->
                                        val isSelected = keyboardKeyColor == opt.key ||
                                                (opt.key == "default" && (keyboardKeyColor.isEmpty() || keyboardKeyColor == "default"))
                                        ColorSwatchBox(
                                            isSelected = isSelected,
                                            color = opt.color,
                                            onClick = { if (!isSpecialTheme) setKeyboardKeyColor(opt.key) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // 3. Key Font / Label Color
                                Text(
                                    text = "Key Font Color",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = Color(0xFFDCDCE0)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(CUSTOM_TEXT_COLOR_OPTIONS) { opt ->
                                        val isSelected = keyboardTextColor == opt.key ||
                                                (opt.key == "default" && (keyboardTextColor.isEmpty() || keyboardTextColor == "default"))
                                        ColorSwatchBox(
                                            isSelected = isSelected,
                                            color = opt.color,
                                            onClick = { if (!isSpecialTheme) setKeyboardTextColor(opt.key) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // 6. Reset to Defaults
                                PillButton(
                                    text = "Reset to Defaults",
                                    onClick = {
                                        if (!isSpecialTheme) {
                                            setKeyboardBgColor("default")
                                            setKeyboardKeyColor("default")
                                            setKeyboardTextColor("default")
                                            setCustomAccent("default")
                                            setKeyBorders(true)
                                        }
                                    },
                                    filled = false,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                }
            } else {
                    // Special theme presets
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(SPECIAL_THEME_PRESETS) { item ->
                            val isSelected = item.key == currentTheme

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    setCustomAccent("default")
                                    setKeyboardBgColor("default")
                                    setKeyboardKeyColor("default")
                                    setKeyboardTextColor("default")
                                    setSpacebarColorKey("theme")
                                    setSpacebarBorderKey("none")
                                    setSpacebarShape("boxy")
                                    currentTheme = item.key
                                    setTheme(item.key)
                                    if (item.key == "OrbitChaos") {
                                        setKeyBorders(false)
                                    }
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(96.dp)
                                        .height(50.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CardBackground)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) NothingRed else BorderColor,
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        item.colors.forEach { swatchColor ->
                                            Box(
                                                modifier = Modifier
                                                    .width(8.dp)
                                                    .height(26.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(swatchColor)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = item.name,
                                    fontFamily = Inter,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    SettingsDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    if (!isSpecialTheme) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1B1B1F))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Select a Special theme to customize",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Column(modifier = if (!isSpecialTheme) Modifier.alpha(0.38f) else Modifier) {
                        // Special controls: Background Color Override
                        Text(
                            text = "Background Color Override",
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(CUSTOM_BG_COLOR_OPTIONS) { opt ->
                                val isSelected = keyboardBgColor == opt.key ||
                                        (opt.key == "default" && (keyboardBgColor.isEmpty() || keyboardBgColor == "default"))
                                ColorSwatchBox(
                                    isSelected = isSelected,
                                    color = opt.color,
                                    onClick = { if (isSpecialTheme) setKeyboardBgColor(opt.key) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        SettingsDivider()

                        // Special controls: Key Hints & Hint Color
                        SettingsRow(
                            horizontalPadding = 0.dp,
                            title = "Key hints",
                            subtitle = "",
                            trailingContent = {
                                Switch(
                                    checked = keyHints,
                                    onCheckedChange = { if (isSpecialTheme) setKeyHints(it) },
                                    enabled = isSpecialTheme,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = NothingRed,
                                        uncheckedThumbColor = TextSecondary,
                                        uncheckedTrackColor = SurfaceVariant
                                    )
                                )
                            }
                        )

                        if (keyHints) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Hint Color",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = Color(0xFFDCDCE0)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(hintColorOptions) { option ->
                                    val isSelected = keyHintColor == option.key ||
                                            (option.key == "theme" && (keyHintColor.isEmpty() || keyHintColor == "theme" || keyHintColor == "default"))
                                    ColorSwatchBox(
                                        isSelected = isSelected,
                                        color = option.color,
                                        onClick = { if (isSpecialTheme) setKeyHintColor(option.key) }
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }





        Spacer(modifier = Modifier.height(12.dp))

        // Card 2: Spacebar Styling
        ExpandableSettingsCard(
            title = "Spacebar Styling",
            summary = "${spacebarShape.replaceFirstChar { it.uppercase() }} • Color: ${spacebarColorKey.replaceFirstChar { it.uppercase() }}",
            isExpanded = expandedCardIndex == 1,
            onToggle = { expandedCardIndex = if (expandedCardIndex == 1) null else 1 },
            icon = Icons.Rounded.SpaceBar,
            accentColor = NothingRed,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Spacebar Shape
                SettingsRow(
                    horizontalPadding = 0.dp,
                    title = "Spacebar Shape",
                    subtitle = "",
                    trailingContent = {
                        SegmentedControl(
                            options = listOf("Boxy", "Roundy"),
                            selectedIndex = if (spacebarShape == "roundy") 1 else 0,
                            onSelect = { setSpacebarShape(if (it == 1) "roundy" else "boxy") }
                        )
                    }
                )

                SettingsDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Spacebar Color
                Text(
                    text = "Color",
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(spaceColorOptions) { option ->
                        val isSelected = spacebarColorKey == option.key ||
                                (option.key == "theme" && (spacebarColorKey.isEmpty() || spacebarColorKey == "theme"))
                        ColorSwatchBox(
                            isSelected = isSelected,
                            color = option.color,
                            onClick = { setSpacebarColorKey(option.key) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                SettingsDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Spacebar Border
                Text(
                    text = "Border Color",
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(spaceBorderOptions) { option ->
                        val isSelected = spacebarBorderKey == option.key ||
                                (option.key == "none" && (spacebarBorderKey.isEmpty() || spacebarBorderKey == "none"))
                        ColorSwatchBox(
                            isSelected = isSelected,
                            color = option.color,
                            onClick = { setSpacebarBorderKey(option.key) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                SettingsDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Spacebar Text Color
                Text(
                    text = "Text Color",
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(spaceTextColorOptions) { option ->
                        val isSelected = spacebarTextColorKey == option.key ||
                                (option.key == "theme" && (spacebarTextColorKey.isEmpty() || spacebarTextColorKey == "theme"))
                        ColorSwatchBox(
                            isSelected = isSelected,
                            color = option.color,
                            onClick = { setSpacebarTextColorKey(option.key) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                SettingsDivider()

                // Pin layout name toggle
                SettingsRow(
                    horizontalPadding = 0.dp,
                    title = "Pin layout name",
                    subtitle = "",
                    trailingContent = {
                        Switch(
                            checked = isPinSpacebarActive,
                            onCheckedChange = { setPinSpacebarActive(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NothingRed,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = SurfaceVariant
                            )
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Card 3: Typography
        ExpandableSettingsCard(
            title = "Typography",
            summary = "${FONT_OPTIONS.find { it.key == keyboardFont }?.name ?: "Orbit Dot"} • Hints: ${if (keyHints) "On" else "Off"}",
            isExpanded = expandedCardIndex == 2,
            onToggle = { expandedCardIndex = if (expandedCardIndex == 2) null else 2 },
            icon = Icons.Rounded.TextFields,
            accentColor = NothingRed,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Keyboard Font Selector
                Text(
                    text = "Keyboard font",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.5.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(10.dp))

                FONT_OPTIONS.forEachIndexed { index, option ->
                    val isSelected = (keyboardFont == option.key) ||
                            (option.key == "nothing" && (keyboardFont == "nothing" || keyboardFont == "orbit_dot" || keyboardFont.isEmpty()))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) SurfaceVariant else Color.Transparent)
                            .clickable { setKeyboardFont(option.key) }
                            .padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = option.name,
                                fontFamily = option.font,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                fontSize = 14.sp,
                                color = if (isSelected) Color.White else Color(0xFFD0D0D5)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = option.desc,
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(NothingRed)
                            )
                        }
                    }
                    if (index < FONT_OPTIONS.size - 1) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                SettingsDivider()

                // Key hints toggle
                SettingsRow(
                    horizontalPadding = 0.dp,
                    title = "Key hints",
                    subtitle = "",
                    trailingContent = {
                        Switch(
                            checked = keyHints,
                            onCheckedChange = { setKeyHints(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NothingRed,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = SurfaceVariant
                            )
                        )
                    }
                )

                if (keyHints) {
                    SettingsDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Hint Color",
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(hintColorOptions) { option ->
                            val isSelected = keyHintColor == option.key ||
                                    (option.key == "theme" && (keyHintColor.isEmpty() || keyHintColor == "theme" || keyHintColor == "default"))
                            ColorSwatchBox(
                                isSelected = isSelected,
                                color = option.color,
                                onClick = { setKeyHintColor(option.key) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // CARD 4: KEY POPUPS
        val popupOptions = listOf(
            listOf("orbit_slim" to "Orbit/Matched", "gboard_bubble" to "Slim Pill"),
            listOf("boxy" to "Classic Box", "off" to "Off/Glow")
        )
        val popupSummary = when (popupStyle) {
            "orbit_slim" -> "Orbit/Matched"
            "gboard_bubble" -> "Slim Pill"
            "boxy" -> "Classic Box"
            "off", "none" -> "Off/Glow"
            else -> "Orbit/Matched"
        }

        ExpandableSettingsCard(
            title = "Key Popups",
            summary = popupSummary,
            icon = Icons.Rounded.TouchApp,
            isExpanded = expandedCardIndex == 3,
            onToggle = { expandedCardIndex = if (expandedCardIndex == 3) null else 3 },
            accentColor = NothingRed,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    popupOptions.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { (key, title) ->
                                val isSelected = popupStyle == key || (key == "off" && popupStyle == "none") || (key == "orbit_slim" && popupStyle == "default")
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(72.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Color(0xFF22242C) else Color(0xFF16171B))
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) NothingRed else BorderColor,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { setPopupStyle(key) }
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) Color(0xFF2A2D38) else Color(0xFF1C1D22))
                                                .border(1.dp, if (isSelected) NothingRed.copy(alpha = 0.5f) else BorderColor, RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            when (key) {
                                                "orbit_slim" -> {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color(0xFF2E3138))
                                                            .border(1.dp, Color(0xFF5A5D66), RoundedCornerShape(6.dp)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("A", fontFamily = Doto, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                                    }
                                                }
                                                "gboard_bubble" -> {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFF2E3138))
                                                            .border(1.dp, Color(0xFF5A5D66), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("A", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                                    }
                                                }
                                                "boxy" -> {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .clip(RoundedCornerShape(2.dp))
                                                            .background(Color(0xFF2E3138))
                                                            .border(1.dp, Color(0xFF5A5D66), RoundedCornerShape(2.dp)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("A", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                                    }
                                                }
                                                "off" -> {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color(0xFF1E1F24))
                                                            .border(1.dp, Color(0xFF3E4048), RoundedCornerShape(6.dp)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("—", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF70727A))
                                                    }
                                                }
                                            }
                                        }

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = title,
                                                fontFamily = Inter,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 12.sp,
                                                color = if (isSelected) Color.White else Color(0xFFD0D0D5),
                                                maxLines = 2,
                                                lineHeight = 14.sp
                                            )
                                        }

                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .clip(CircleShape)
                                                    .background(NothingRed)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // CARD 5: TYPING EFFECTS
        val effectRows = listOf(
            listOf("none" to "None", "radar_wave" to "Radar Wave"),
            listOf("particle_burst" to "Particle Burst", "constellation_web" to "Constellation Web"),
            listOf("laser_reticle" to "Border Collision")
        )
        val effectSummary = when (typingEffect) {
            "radar_wave" -> "Radar Wave"
            "particle_burst" -> "Particle Burst"
            "constellation_web" -> "Constellation Web"
            "laser_reticle" -> "Border Collision"
            else -> "None"
        }

        ExpandableSettingsCard(
            title = "Typing Effects",
            summary = effectSummary,
            icon = Icons.Rounded.TouchApp,
            isExpanded = expandedCardIndex == 4,
            onToggle = { expandedCardIndex = if (expandedCardIndex == 4) null else 4 },
            accentColor = NothingRed,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    effectRows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { (key, title) ->
                                val isSelected = typingEffect == key
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(72.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Color(0xFF22242C) else Color(0xFF16171B))
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) NothingRed else BorderColor,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { setTypingEffect(key) }
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) Color(0xFF2A2D38) else Color(0xFF1C1D22))
                                                .border(1.dp, if (isSelected) NothingRed.copy(alpha = 0.5f) else BorderColor, RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            when (key) {
                                                "none" -> {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(10.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFF6E6E78))
                                                    )
                                                }
                                                "radar_wave" -> {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .border(1.dp, NothingRed.copy(alpha = 0.6f), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(14.dp)
                                                                .border(1.dp, NothingRed.copy(alpha = 0.35f), CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(6.dp)
                                                                    .clip(CircleShape)
                                                                    .background(NothingRed)
                                                            )
                                                        }
                                                    }
                                                }
                                                "particle_burst" -> {
                                                    Box(
                                                        modifier = Modifier.size(24.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Box(Modifier.align(Alignment.TopCenter).size(4.dp).clip(CircleShape).background(NothingRed))
                                                        Box(Modifier.align(Alignment.BottomCenter).size(4.dp).clip(CircleShape).background(Color(0xFFE2B438)))
                                                        Box(Modifier.align(Alignment.CenterStart).size(4.dp).clip(CircleShape).background(Color(0xFF3B72BA)))
                                                        Box(Modifier.align(Alignment.CenterEnd).size(4.dp).clip(CircleShape).background(Color(0xFFFFFFFF)))
                                                        Box(Modifier.align(Alignment.Center).size(3.dp).clip(CircleShape).background(NothingRed))
                                                    }
                                                }
                                                "constellation_web" -> {
                                                    Box(
                                                        modifier = Modifier.size(24.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                        ) {
                                                            Box(Modifier.size(4.dp).clip(CircleShape).background(NothingRed))
                                                            Box(Modifier.width(4.dp).height(1.dp).background(Color(0xFF9E9EA8)))
                                                            Box(Modifier.size(5.dp).clip(CircleShape).background(Color.White))
                                                            Box(Modifier.width(4.dp).height(1.dp).background(Color(0xFF9E9EA8)))
                                                            Box(Modifier.size(4.dp).clip(CircleShape).background(Color(0xFFE2B438)))
                                                        }
                                                    }
                                                }
                                                "laser_reticle" -> {
                                                    Box(
                                                        modifier = Modifier.size(24.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Box(
                                                            Modifier.size(16.dp)
                                                                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                                                        )
                                                        Box(
                                                            Modifier.align(Alignment.TopEnd)
                                                                .size(7.dp, 2.dp)
                                                                .background(Color(0xFF00E5FF))
                                                        )
                                                        Box(
                                                            Modifier.align(Alignment.CenterEnd)
                                                                .size(2.dp, 9.dp)
                                                                .background(Color(0xFFFF1493))
                                                        )
                                                        Box(Modifier.size(3.dp).clip(CircleShape).background(Color.White))
                                                    }
                                                }
                                            }
                                        }

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = title,
                                                fontFamily = Inter,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 12.sp,
                                                color = if (isSelected) Color.White else Color(0xFFD0D0D5),
                                                maxLines = 2,
                                                lineHeight = 14.sp
                                            )
                                        }

                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .clip(CircleShape)
                                                    .background(NothingRed)
                                            )
                                        }
                                    }
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }


            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // CARD 6: RESIZE & POSITION
        ExpandableSettingsCard(
            title = "Resize & Position",
            summary = "Height: ${(heightMultiplier * 100).roundToInt()}% • Offset: ${bottomOffset.roundToInt()} dp",
            icon = Icons.Rounded.AspectRatio,
            isExpanded = expandedCardIndex == 5,
            onToggle = { expandedCardIndex = if (expandedCardIndex == 5) null else 5 },
            accentColor = NothingRed,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Keyboard Height",
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceVariant)
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${(heightMultiplier * 100).roundToInt()}%",
                            fontFamily = Doto,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NothingRed
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = heightMultiplier,
                    onValueChange = { setHeightMultiplier(it) },
                    valueRange = 0.70f..1.30f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = NothingRed,
                        inactiveTrackColor = SettingsSurfaceVariant
                    )
                )

                SettingsDivider()
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Bottom Offset",
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceVariant)
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${bottomOffset.roundToInt()} dp",
                            fontFamily = Doto,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NothingRed
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = bottomOffset,
                    onValueChange = { setBottomOffset(it) },
                    valueRange = 0f..48f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = NothingRed,
                        inactiveTrackColor = SettingsSurfaceVariant
                    )
                )

                SettingsDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PillButton(
                        text = "Reset Defaults",
                        onClick = {
                            setHeightMultiplier(1.0f)
                            setBottomOffset(0.0f)
                        },
                        filled = false,
                        modifier = Modifier.weight(1f)
                    )
                    PillButton(
                        text = "Dedicated Screen",
                        onClick = { navController.navigate("resize") },
                        filled = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 6. Interactive Typing Test Field
        SectionHeader(
            title = "Try Typing",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF121215))
                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (typingTestText.isEmpty()) {
                        Text(
                            text = "start typing here...",
                            fontFamily = Inter,
                            fontSize = 14.sp,
                            color = Color(0xFF55555C)
                        )
                    }
                    BasicTextField(
                        value = typingTestText,
                        onValueChange = { typingTestText = it },
                        textStyle = TextStyle(
                            fontFamily = Inter,
                            fontSize = 14.sp,
                            color = Color.White
                        ),
                        cursorBrush = SolidColor(NothingRed),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                if (typingTestText.isNotEmpty()) {
                    IconButton(
                        onClick = { typingTestText = "" },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        val intent = Intent().apply {
                            setClass(context, TextEditPopupActivity::class.java)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.OpenInNew,
                        contentDescription = "Open Full Screen Test",
                        tint = NothingRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(110.dp))
    } // Closes Subordinate Scrollable Column (Tier 2)
    } // Closes Outer Root Column
} // Closes KeyboardCustomizationScreen
