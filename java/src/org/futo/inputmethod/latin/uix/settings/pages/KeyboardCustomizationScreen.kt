package org.futo.inputmethod.latin.uix.settings.pages

import android.content.Intent
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.KeyBordersSetting
import org.futo.inputmethod.latin.uix.CustomAccentColorSetting
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
import org.futo.inputmethod.latin.uix.theme.presets.NothingDark
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
    val color: Color
)

data class SpacebarBorderOption(
    val key: String,
    val name: String,
    val color: Color?
)

data class SpacebarTextColorOption(
    val key: String,
    val name: String,
    val color: Color
)

data class HintColorOption(
    val key: String,
    val name: String,
    val color: Color
)

data class FontOptionItem(
    val key: String,
    val name: String,
    val desc: String,
    val font: androidx.compose.ui.text.font.FontFamily
)

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

    val (currentTheme, setTheme) = useDataStore(THEME_KEY.key, THEME_KEY.default)
    val (keyBorders, setKeyBorders) = useDataStore(KeyBordersSetting.key, KeyBordersSetting.default)
    val (customAccent, setCustomAccent) = useDataStore(CustomAccentColorSetting.key, CustomAccentColorSetting.default)
    val (keyHints, setKeyHints) = useDataStore(KeyHintsSetting.key, KeyHintsSetting.default)
    val (keyHintColor, setKeyHintColor) = useDataStore(KeyHintColorSetting.key, KeyHintColorSetting.default)
    val (keyboardFont, setKeyboardFont) = useDataStore(KeyboardFontSetting.key, KeyboardFontSetting.default)
    val (popupStyle, setPopupStyle) = useDataStore(KeyPopupStyleSetting.key, KeyPopupStyleSetting.default)
    val (heightMultiplier, _) = useDataStore(KeyboardHeightMultiplierSetting.key, KeyboardHeightMultiplierSetting.default)
    val (bottomOffset, _) = useDataStore(KeyboardBottomOffsetSetting.key, KeyboardBottomOffsetSetting.default)

    val (spacebarShape, setSpacebarShape) = useDataStore(SpacebarShapeSetting.key, SpacebarShapeSetting.default)
    val (spacebarColorKey, setSpacebarColorKey) = useDataStore(SpacebarColorSetting.key, SpacebarColorSetting.default)
    val (spacebarBorderKey, setSpacebarBorderKey) = useDataStore(SpacebarBorderSetting.key, SpacebarBorderSetting.default)
    val (spacebarTextColorKey, setSpacebarTextColorKey) = useDataStore(SpacebarTextColorSetting.key, SpacebarTextColorSetting.default)
    val (typingEffect, setTypingEffect) = useDataStore(KeyboardTypingEffectSetting.key, KeyboardTypingEffectSetting.default)

    var typingTestText by remember { mutableStateOf("") }

    val customAccentOverride: Color? = when (customAccent) {
        "red" -> Color(0xFFD71921)
        "yellow" -> Color(0xFFE2B438)
        "blue" -> Color(0xFF3B72BA)
        "white" -> Color(0xFFEAE6E1)
        "green" -> Color(0xFF67FB59)
        "purple" -> Color(0xFFA855F7)
        "orange" -> Color(0xFFFF7A00)
        else -> null
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

    val currentSpaceColor = when (spacebarColorKey) {
        "theme" -> currentThemeColors.primary
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

    val currentSpaceBorder = when (spacebarBorderKey) {
        "accent" -> currentThemeColors.primary
        "yellow" -> Color(0xFFE2B438)
        "red" -> Color(0xFFC23636)
        "white" -> Color(0xFFFFFFFF)
        "none" -> null
        else -> null
    }

    val currentSpaceTextColor = when (spacebarTextColorKey) {
        "white" -> Color(0xFFFFFFFF)
        "yellow" -> Color(0xFFE2B438)
        "red" -> Color(0xFFC23636)
        "dark" -> Color(0xFF1C1D20)
        "theme" -> currentThemeColors.primary
        else -> null
    }

    val regularThemePresets = listOf(
        ThemePresetItem(
            key = "NothingDark",
            name = "Nothing",
            colors = listOf(
                Color(0xFF0D0C11),
                Color(0xFF1C1D20),
                Color(0xFF282830),
                Color(0xFFF1F1F1),
                Color(0xFFD71921)
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
            name = "Nothing light",
            colors = listOf(
                Color(0xFFF5F5F5),
                Color(0xFFD5D5D5),
                Color(0xFF1C1D20),
                Color(0xFFE0E0E0),
                Color(0xFFD71921)
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
            key = "SteelGray",
            name = "Steel gray",
            colors = listOf(
                Color(0xFF18191A),
                Color(0xFF242526),
                Color(0xFF9AA0A6),
                Color(0xFFE4E6EB),
                Color(0xFF80868B)
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
            key = "DynamicSystemTheme",
            name = "Dynamic system",
            colors = listOf(
                Color(0xFF2D3133),
                Color(0xFF40484C),
                Color(0xFF8CD0E5),
                Color(0xFFCCE8EE),
                Color(0xFFECEEEF)
            )
        ),
        ThemePresetItem(
            key = "DynamicDarkTheme",
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
            key = "DynamicLightTheme",
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
            key = "VoiceInputTheme",
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

    val specialThemePresets = listOf(
        ThemePresetItem(
            key = "OrbitTheme",
            name = "Orbit",
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
            colors = listOf(
                Color(0xFFE2B438),
                Color(0xFF3B72BA),
                Color(0xFF000000),
                Color(0xFFD71921),
                Color(0xFFEAE6E1)
            )
        )
    )

    val specialThemeKeys = remember { setOf("OrbitTheme", "OrbitChaos") }
    var selectedThemeTab by remember(currentTheme) {
        mutableStateOf(if (currentTheme in specialThemeKeys) 1 else 0)
    }

    val accentColorOptions = listOf(
        AccentColorOption("default", "Default", null),
        AccentColorOption("red", "Nothing Red", Color(0xFFD71921)),
        AccentColorOption("yellow", "Bauhaus Ochre", Color(0xFFE2B438)),
        AccentColorOption("blue", "Cobalt Blue", Color(0xFF3B72BA)),
        AccentColorOption("white", "Chalk White", Color(0xFFEAE6E1)),
        AccentColorOption("green", "Emerald Green", Color(0xFF67FB59)),
        AccentColorOption("purple", "Violet Purple", Color(0xFFA855F7)),
        AccentColorOption("orange", "Warm Orange", Color(0xFFFF7A00))
    )

    val spaceColorOptions = listOf(
        SpacebarColorOption("theme", "Theme", currentThemeColors.primary),
        SpacebarColorOption("default", "Classic", currentThemeColors.outline.copy(alpha = 0.33f)),
        SpacebarColorOption("dark", "Slate", Color(0xFF1C1D20)),
        SpacebarColorOption("white", "Chalk", Color(0xFFEAE6E1)),
        SpacebarColorOption("yellow", "Mustard", Color(0xFFE2B438)),
        SpacebarColorOption("red", "Red", Color(0xFFC23636)),
        SpacebarColorOption("blue", "Cobalt", Color(0xFF2A5288))
    )

    val spaceBorderOptions = listOf(
        SpacebarBorderOption("none", "None", null),
        SpacebarBorderOption("accent", "Accent", currentThemeColors.primary),
        SpacebarBorderOption("white", "White", Color(0xFFFFFFFF)),
        SpacebarBorderOption("yellow", "Yellow", Color(0xFFE2B438)),
        SpacebarBorderOption("red", "Red", Color(0xFFC23636))
    )

    val spaceTextColorOptions = listOf(
        SpacebarTextColorOption("default", "Default", Color(0xFF8E8E93)),
        SpacebarTextColorOption("white", "White", Color(0xFFFFFFFF)),
        SpacebarTextColorOption("yellow", "Yellow", Color(0xFFE2B438)),
        SpacebarTextColorOption("red", "Red", Color(0xFFC23636)),
        SpacebarTextColorOption("dark", "Dark", Color(0xFF1C1D20)),
        SpacebarTextColorOption("theme", "Theme", currentThemeColors.primary)
    )

    val hintColorOptions = listOf(
        HintColorOption("subtle", "Subtle Gray", Color(0xFF8E8E93)),
        HintColorOption("yellow", "Ochre", Color(0xFFE2B438)),
        HintColorOption("red", "Carmine", Color(0xFFC23636)),
        HintColorOption("blue", "Cobalt", Color(0xFF2A5288)),
        HintColorOption("white", "White", Color(0xFFFFFFFF)),
        HintColorOption("orange", "Warm Orange", Color(0xFFFF7A00)),
        HintColorOption("theme", "Theme Accent", currentThemeColors.primary)
    )

    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
    val isImeVisible = imeBottom > 0

    LaunchedEffect(isImeVisible) {
        if (isImeVisible) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .verticalScroll(scrollState)
            .then(
                if (!isImeVisible) {
                    Modifier.navigationBarsPadding().padding(bottom = 84.dp)
                } else {
                    Modifier.padding(bottom = 12.dp)
                }
            )
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 1. Unified Orbit Board Header
        OrbitBoardHeader()

        Spacer(modifier = Modifier.height(4.dp))

        // 1. Live Interactive Preview
        SectionHeader(
            title = "Interactive Preview",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Live Keyboard Preview
        RealisticKeyboardPreview(
            currentTheme = currentTheme,
            spacebarColor = currentSpaceColor,
            spacebarBorder = currentSpaceBorder,
            spacebarShape = spacebarShape,
            spacebarTextColor = currentSpaceTextColor,
            hasKeyBorders = keyBorders,
            showSuggestionRow = true,
            showNumberRow = false,
            pinLayoutNameOnSpacebar = true,
            showEmojiKey = true,
            showKeyHints = keyHints,
            keyHintColor = keyHintColor,
            customAccentColor = customAccent,
            keyboardFont = keyboardFont,
            typingEffect = typingEffect,
            activeLayoutName = "English",
            heightMultiplier = heightMultiplier,
            bottomOffset = bottomOffset,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // 2. Section Header: Theme
        SectionHeader(
            title = "Theme",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        NeuCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = 12.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Base Theme",
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Layer typing effects independently on any theme",
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

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
                        val displayName = when (currentTheme) {
                            "NothingDark" -> "Nothing"
                            "DynamicNothing" -> "Dynamic"
                            "AMOLEDNothing" -> "AMOLED"
                            "NothingLight" -> "Light"
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
                            "DynamicSystemTheme" -> "Dynamic System"
                            "DynamicDarkTheme" -> "Dynamic Dark"
                            "DynamicLightTheme" -> "Dynamic Light"
                            "VoiceInputTheme" -> "Voice Input"
                            "OrbitChaos" -> "Chaos"
                            else -> currentTheme
                        }
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

            // Segmented Tab for Regular vs Special
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceVariant)
                    .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Regular", "Special").forEachIndexed { index, tabTitle ->
                    val isSelected = selectedThemeTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) NothingRed else Color.Transparent)
                            .clickable { selectedThemeTab = index }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabTitle,
                            fontFamily = Inter,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val displayedPresets = if (selectedThemeTab == 1) specialThemePresets else regularThemePresets

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayedPresets) { item ->
                    val isSelected = item.key == currentTheme

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
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
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Typing Effect Section
        SectionHeader(
            title = "Typing Effect",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        NeuCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = 8.dp
        ) {
            val effectOptions = listOf(
                Triple("none", "None", "Off — Clean typing with no touch animation"),
                Triple("radar_wave", "Radar Wave", "Glyph Radar Pulse — 3 expanding dot-matrix rings with red spark core"),
                Triple("particle_burst", "Particle Burst", "Particle Burst — Radiating micro-dots in Bauhaus palette"),
                Triple("constellation_web", "Constellation Web", "Option B — Interconnected geometric constellation lines & glowing nodes")
            )
            effectOptions.forEachIndexed { index, (key, title, desc) ->
                val isSelected = (typingEffect == key) || (key == "none" && (typingEffect.isEmpty() || typingEffect == "none"))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) SurfaceVariant else Color.Transparent)
                        .clickable { setTypingEffect(key) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF22242C) else Color(0xFF16171B))
                                .border(1.dp, if (isSelected) NothingRed else BorderColor, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            when (key) {
                                "none" -> {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF55555C))
                                    )
                                }
                                "radar_wave" -> {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .border(1.dp, Color(0xFFD71921).copy(alpha = 0.6f), CircleShape),
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
                                "particle_burst" -> {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(Modifier.size(4.dp).clip(CircleShape).background(NothingRed))
                                        Box(Modifier.size(5.dp).clip(CircleShape).background(Color(0xFFE2B438)))
                                        Box(Modifier.size(4.dp).clip(CircleShape).background(Color(0xFF3B72BA)))
                                    }
                                }
                                "constellation_web" -> {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(Modifier.size(4.5.dp).clip(CircleShape).background(NothingRed))
                                        Box(Modifier.width(6.dp).height(1.dp).background(Color(0xFF888890)))
                                        Box(Modifier.size(4.5.dp).clip(CircleShape).background(Color(0xFFFAFAFA)))
                                    }
                                }
                            }
                        }

                        Column {
                            Text(
                                text = title,
                                fontFamily = Inter,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 14.sp,
                                color = if (isSelected) Color.White else Color(0xFFD0D0D5)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = desc,
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
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

                if (index < effectOptions.size - 1) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(DividerColor)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Custom Accent Color Card
        SectionHeader(
            title = "Accent Color",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        NeuCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = 12.dp
        ) {
            Text(
                text = "Primary Accent",
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Override primary accent color for active keys, cursor, and controls",
                fontFamily = Inter,
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(accentColorOptions) { option ->
                    val isSelected = customAccent == option.key ||
                            (option.key == "default" && (customAccent.isEmpty() || customAccent == "default"))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) SurfaceVariant else Color(0xFF17171A))
                            .border(1.dp, if (isSelected) NothingRed else BorderColor, RoundedCornerShape(8.dp))
                            .clickable { setCustomAccent(option.key) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (option.color != null) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(option.color)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                )
                            }
                            Text(
                                text = option.name,
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Spacebar Styling Card (Shape, Color, Border)
        SectionHeader(
            title = "Spacebar Styling",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        NeuCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = 12.dp
        ) {
            // Spacebar Shape (Boxy vs Roundy)
            Text(
                text = "Shape",
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "Boxy squircle (8dp) vs Roundy stadium capsule (48dp)",
                fontFamily = Inter,
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Segmented control: Boxy vs Roundy
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceVariant)
                    .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isBoxy = spacebarShape != "roundy"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isBoxy) NothingRed else Color.Transparent)
                        .clickable { setSpacebarShape("boxy") }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Boxy (8dp)",
                        fontFamily = Inter,
                        fontWeight = if (isBoxy) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp,
                        color = if (isBoxy) Color.White else TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isBoxy) NothingRed else Color.Transparent)
                        .clickable { setSpacebarShape("roundy") }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Roundy (48dp)",
                        fontFamily = Inter,
                        fontWeight = if (!isBoxy) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp,
                        color = if (!isBoxy) Color.White else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
            Spacer(modifier = Modifier.height(14.dp))

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
                    val isSelected = spacebarColorKey == option.key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) SurfaceVariant else Color(0xFF17171A))
                            .border(1.dp, if (isSelected) NothingRed else BorderColor, RoundedCornerShape(8.dp))
                            .clickable { setSpacebarColorKey(option.key) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(option.color)
                            )
                            Text(
                                text = option.name,
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
            Spacer(modifier = Modifier.height(14.dp))

            // Spacebar Border
            Text(
                text = "Border",
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
                    val isSelected = spacebarBorderKey == option.key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) SurfaceVariant else Color(0xFF17171A))
                            .border(1.dp, if (isSelected) NothingRed else BorderColor, RoundedCornerShape(8.dp))
                            .clickable { setSpacebarBorderKey(option.key) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (option.color != null) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(option.color)
                                )
                            }
                            Text(
                                text = option.name,
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
            Spacer(modifier = Modifier.height(14.dp))

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
                    val isSelected = spacebarTextColorKey == option.key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) SurfaceVariant else Color(0xFF17171A))
                            .border(1.dp, if (isSelected) NothingRed else BorderColor, RoundedCornerShape(8.dp))
                            .clickable { setSpacebarTextColorKey(option.key) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(option.color)
                            )
                            Text(
                                text = option.name,
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. Key Hints & Colors Card (Below Spacebar Settings)
        SectionHeader(
            title = "Key Hints & Color",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        NeuCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = 8.dp
        ) {
            ToggleSettingRow(
                title = "Key hints",
                subtitle = "Show secondary symbol and letter hints on key tops",
                checked = keyHints,
                onCheckedChange = { setKeyHints(it) }
            )

            if (keyHints) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Hint Color",
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Choose accent color for secondary key symbols and hints",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(hintColorOptions) { option ->
                            val isSelected = keyHintColor == option.key ||
                                    (option.key == "subtle" && (keyHintColor == "subtle" || keyHintColor == "default" || keyHintColor.isEmpty()))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) SurfaceVariant else Color(0xFF17171A))
                                    .border(1.dp, if (isSelected) NothingRed else BorderColor, RoundedCornerShape(8.dp))
                                    .clickable { setKeyHintColor(option.key) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(option.color)
                                    )
                                    Text(
                                        text = option.name,
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 5. Keys & Typography Card
        SectionHeader(
            title = "Keys & Typography",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        NeuCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = 4.dp
        ) {
            ToggleSettingRow(
                title = "Key borders",
                subtitle = "Draw subtle rounded borders around individual keys",
                checked = keyBorders,
                onCheckedChange = { setKeyBorders(it) }
            )
            Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))

            // Keyboard Font Selector
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Keyboard font",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Text(
                    text = "Select sleek display font for key labels and live preview",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                val fontOptions = listOf(
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

                fontOptions.forEachIndexed { index, option ->
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
                    if (index < fontOptions.size - 1) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(DividerColor)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 5. Popup Style & Geometry
        SectionHeader(
            title = "Popup & Geometry",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        NeuCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = 8.dp
        ) {
            val popupStyles = listOf(
                Triple("orbit_slim", "Orbit (Default)", "Classic floating squircle with hairline border"),
                Triple("gboard_bubble", "Gboard Bubble", "Compact floating circular bubble"),
                Triple("fluid_contour", "Fluid Contour", "Sleek smooth paddle contour merging into key"),
                Triple("boxy", "Boxy", "Sharp rectangular Bauhaus preview")
            )
            popupStyles.forEachIndexed { index, (key, name, desc) ->
                val isSelected = popupStyle == key
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) SurfaceVariant else Color.Transparent)
                        .clickable { setPopupStyle(key) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            fontFamily = Inter,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (isSelected) Color.White else Color(0xFFD0D0D5)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = desc,
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
                if (index < popupStyles.size - 1) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(DividerColor)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Resize Keyboard Navigation Card
        NeuCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = 14.dp,
            onClick = { navController.navigate("resize") }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceVariant)
                            .border(1.dp, BorderColor, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.maximize),
                            contentDescription = null,
                            tint = NothingRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Resize Keyboard",
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Adjust height & bottom offset with live preview",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
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

        Spacer(modifier = Modifier.height(24.dp))
    }
}
