package org.futo.inputmethod.latin.uix.theme.selector

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import org.futo.inputmethod.latin.uix.KeyBordersSetting
import org.futo.inputmethod.latin.uix.CustomAccentColorSetting
import org.futo.inputmethod.latin.uix.KeyboardBgColorSetting
import org.futo.inputmethod.latin.uix.KeyboardKeyColorSetting
import org.futo.inputmethod.latin.uix.KeyboardTextColorSetting
import org.futo.inputmethod.latin.uix.SpacebarColorSetting
import org.futo.inputmethod.latin.uix.SpacebarBorderSetting
import org.futo.inputmethod.latin.uix.SpacebarShapeSetting
import org.futo.inputmethod.latin.uix.THEME_KEY
import org.futo.inputmethod.latin.uix.settings.useDataStore
import org.futo.inputmethod.latin.uix.settings.SegmentedControl
import org.futo.inputmethod.latin.uix.theme.ThemeOption
import org.futo.inputmethod.latin.uix.theme.ThemeOptionKeys
import org.futo.inputmethod.latin.uix.theme.ThemeOptions
import org.futo.inputmethod.latin.uix.theme.Doto
import org.futo.inputmethod.latin.uix.theme.NothingRed
import org.futo.inputmethod.latin.uix.theme.Typography
import org.futo.inputmethod.latin.uix.theme.UixThemeWrapper
import org.futo.inputmethod.latin.uix.theme.presets.AMOLEDDarkPurple
import org.futo.inputmethod.latin.uix.theme.presets.AMOLEDNothing
import org.futo.inputmethod.latin.uix.theme.presets.OrbitPreviewKeyboard
import org.futo.inputmethod.latin.uix.theme.presets.ChaosPreviewKeyboard
import org.futo.inputmethod.latin.uix.theme.presets.RetroBlocksPreviewKeyboard
import org.futo.inputmethod.latin.uix.theme.presets.BerryBlushPreviewKeyboard
import org.futo.inputmethod.latin.uix.theme.presets.TactileSlatePreviewKeyboard
import org.futo.inputmethod.latin.uix.theme.presets.NothingLight
import org.futo.inputmethod.latin.uix.theme.presets.NothingDark
import org.futo.inputmethod.latin.uix.theme.presets.ClassicMaterialDark
import org.futo.inputmethod.latin.uix.theme.presets.DynamicDarkTheme
import org.futo.inputmethod.latin.uix.theme.presets.DynamicLightTheme
import org.futo.inputmethod.latin.uix.theme.presets.DynamicNothing
import org.futo.inputmethod.latin.uix.theme.presets.VoiceInputTheme
import kotlin.math.roundToInt

@Composable
fun ThemePreview(theme: ThemeOption, isSelected: Boolean = false, overrideName: String? = null, modifier: Modifier = Modifier, onClick: () -> Unit = { }) {

    val context = LocalContext.current
    val colors = remember { theme.obtainColors(context) }

    val currColors = MaterialTheme.colorScheme

    val borderWidth = if (isSelected) {
        4.dp
    } else {
        Dp.Hairline
    }

    val borderColor = if (isSelected) {
        currColors.inversePrimary
    } else {
        currColors.outline
    }

    val textColor = colors.onBackground

    // TODO: These have to be manually kept same as those in BasicThemeProvider
    val spacebarColor = colors.outline.copy(alpha = 0.33f)
    val actionColor = colors.primary

    val keyboardShape = RoundedCornerShape(8.dp)

    val previewModifier = if(LocalInspectionMode.current) {
        modifier.width(172.dp)
    } else {
        modifier
    }

    Surface(
        modifier = previewModifier
            .padding(12.dp)
            .height(128.dp)
            .border(borderWidth, borderColor, keyboardShape)
            .clickable { onClick() },
        color = colors.surface,
        shape = keyboardShape
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Theme name and action bar
            Text(
                text = overrideName ?: stringResource(theme.name),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .background(colors.background)
                    .fillMaxWidth()
                    .padding(4.dp),
                color = textColor,
                style = Typography.labelSmall
            )



            // Keyboard contents
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                val (keyBordersPref, _) = useDataStore(KeyBordersSetting.key, KeyBordersSetting.default)
                if (theme.key == "OrbitTheme") {
                    OrbitPreviewKeyboard()
                } else if (theme.key == "OrbitChaos") {
                    ChaosPreviewKeyboard()
                } else if (theme.key == "RetroBlocks") {
                    RetroBlocksPreviewKeyboard()
                } else if (theme.key == "BerryBlush") {
                    BerryBlushPreviewKeyboard()
                } else if (theme.key == "TactileSlate") {
                    TactileSlatePreviewKeyboard()
                } else {
                    // Spacebar
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(18.dp)
                            .align(Alignment.BottomCenter),
                        color = spacebarColor,
                        shape = RoundedCornerShape(12.dp)
                    ) { }

                    // Enter key
                    Surface(
                        modifier = Modifier
                            .width(24.dp)
                            .height(18.dp)
                            .align(Alignment.BottomEnd)
                            .padding(0.dp, 1.dp),
                        color = actionColor,
                        shape = RoundedCornerShape(4.dp)
                    ) { }
                }
            }
        }
    }
}

@Composable
fun AddCustomThemeButton(onClick: () -> Unit = { }) {
    val context = LocalContext.current
    val currColors = MaterialTheme.colorScheme

    val keyboardShape = RoundedCornerShape(8.dp)

    Surface(
        modifier = Modifier
            .padding(12.dp)
            .width(172.dp)
            .height(128.dp)
            .clickable { onClick() },
        color = currColors.surfaceVariant,
        shape = keyboardShape
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Icon(
                Icons.Default.Add, contentDescription = "", modifier = Modifier
                    .size(48.dp)
                    .align(
                        Alignment.Center
                    )
            )
        }
    }
}

@Composable
fun ThemePicker(onSelected: (ThemeOption) -> Unit) {
    val context = LocalContext.current

    val currentTheme = useDataStore(THEME_KEY.key, "").value
    val (_, setCustomAccent) = useDataStore(CustomAccentColorSetting.key, CustomAccentColorSetting.default)
    val (_, setKeyboardBgColor) = useDataStore(KeyboardBgColorSetting.key, KeyboardBgColorSetting.default)
    val (_, setKeyboardKeyColor) = useDataStore(KeyboardKeyColorSetting.key, KeyboardKeyColorSetting.default)
    val (_, setKeyboardTextColor) = useDataStore(KeyboardTextColorSetting.key, KeyboardTextColorSetting.default)
    val (_, setSpacebarColor) = useDataStore(SpacebarColorSetting.key, SpacebarColorSetting.default)
    val (_, setSpacebarBorder) = useDataStore(SpacebarBorderSetting.key, SpacebarBorderSetting.default)
    val (_, setSpacebarShape) = useDataStore(SpacebarShapeSetting.key, SpacebarShapeSetting.default)

    val isInspecting = LocalInspectionMode.current
    val availableThemeOptions = remember {
        ThemeOptionKeys.mapNotNull { key ->
            ThemeOptions[key]?.let { Pair(key, it) }
        }.filter {
            it.second.available(context)
        }.filter {
            when (isInspecting) {
                true -> !it.second.dynamic
                else -> true
            }
        }
    }


    var selectedTab by remember(currentTheme) {
        mutableStateOf(if (currentTheme in listOf("OrbitTheme", "OrbitChaos", "RetroBlocks", "BerryBlush", "TactileSlate")) "Special" else "Regular")
    }

    val specialThemes = remember(availableThemeOptions) {
        availableThemeOptions.filter { (key, _) -> key in listOf("OrbitTheme", "OrbitChaos", "RetroBlocks", "BerryBlush", "TactileSlate") }
            .sortedBy { (key, _) ->
                when (key) {
                    "OrbitTheme" -> 0
                    "OrbitChaos" -> 1
                    "RetroBlocks" -> 2
                    "BerryBlush" -> 3
                    "TactileSlate" -> 4
                    else -> 5
                }
            }
    }

    val regularThemes = remember(availableThemeOptions) {
        availableThemeOptions.filter { (key, _) -> key !in listOf("OrbitTheme", "OrbitChaos", "RetroBlocks", "BerryBlush", "TactileSlate") }
            .sortedBy { (key, _) ->
                when (key) {
                    "NothingDark" -> 0
                    "DynamicNothing" -> 1
                    "AMOLEDNothing" -> 2
                    "NothingLight" -> 3
                    else -> 100
                }
            }
    }

    val displayedThemes = when (selectedTab) {
        "Special" -> specialThemes
        "Regular" -> regularThemes
        else -> emptyList()
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        SegmentedControl(
            options = listOf("Regular", "Special"),
            selectedIndex = if (selectedTab == "Special") 1 else 0,
            onSelect = {
                selectedTab = if (it == 1) "Special" else "Regular"
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            equalWeight = true
        )

        Spacer(modifier = Modifier.height(4.dp))

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxWidth(),
                columns = GridCells.Adaptive(minSize = 172.dp),
                horizontalArrangement = if (LocalLayoutDirection.current == LayoutDirection.Rtl) {
                    Arrangement.End
                } else {
                    Arrangement.Start
                }
            ) {
                items(displayedThemes.count()) {
                    val themeOption = displayedThemes[it].second

                    ThemePreview(themeOption, isSelected = themeOption.key == currentTheme) {
                        setCustomAccent("default")
                        setKeyboardBgColor("default")
                        setKeyboardKeyColor("default")
                        setKeyboardTextColor("default")
                        setSpacebarColor("theme")
                        setSpacebarBorder("none")
                        setSpacebarShape("boxy")
                        onSelected(themeOption)
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun ThemePickerPreview() {
    Column {
        UixThemeWrapper(VoiceInputTheme.obtainColors(LocalContext.current)) {
            Surface(
                color = MaterialTheme.colorScheme.background
            ) {
                ThemePicker {}
            }
        }
        UixThemeWrapper(ClassicMaterialDark.obtainColors(LocalContext.current)) {
            Surface(
                color = MaterialTheme.colorScheme.background
            ) {
                ThemePicker {}
            }
        }
        UixThemeWrapper(AMOLEDDarkPurple.obtainColors(LocalContext.current)) {
            Surface(
                color = MaterialTheme.colorScheme.background
            ) {
                ThemePicker {}
            }
        }
        UixThemeWrapper(AMOLEDNothing.obtainColors(LocalContext.current)) {
            Surface(
                color = MaterialTheme.colorScheme.background
            ) {
                ThemePicker {}
            }
        }
        UixThemeWrapper(DynamicNothing.obtainColors(LocalContext.current)) {
            Surface(
                color = MaterialTheme.colorScheme.background
            ) {
                ThemePicker {}
            }
        }
        UixThemeWrapper(NothingLight.obtainColors(LocalContext.current)) {
            Surface(
                color = MaterialTheme.colorScheme.background
            ) {
                ThemePicker {}
            }
        }
        UixThemeWrapper(NothingDark.obtainColors(LocalContext.current)) {
            Surface(
                color = MaterialTheme.colorScheme.background
            ) {
                ThemePicker {}
            }
        }
    }
}