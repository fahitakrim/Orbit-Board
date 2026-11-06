package org.futo.inputmethod.latin.uix.settings.pages

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.futo.inputmethod.latin.ActiveSubtype
import org.futo.inputmethod.latin.settings.Settings
import org.futo.inputmethod.latin.uix.KeyBordersSetting
import org.futo.inputmethod.latin.uix.KeyHintColorSetting
import org.futo.inputmethod.latin.uix.KeyHintsSetting
import org.futo.inputmethod.latin.uix.KeyboardBottomOffsetSetting
import org.futo.inputmethod.latin.uix.KeyboardFontSetting
import org.futo.inputmethod.latin.uix.KeyboardHeightMultiplierSetting
import org.futo.inputmethod.latin.uix.SpacebarBorderSetting
import org.futo.inputmethod.latin.uix.SpacebarColorSetting
import org.futo.inputmethod.latin.uix.THEME_KEY
import org.futo.inputmethod.latin.uix.TextEditPopupActivity
import org.futo.inputmethod.latin.uix.adjustColorBrightnessForContrast
import org.futo.inputmethod.latin.uix.settings.NeuCard
import org.futo.inputmethod.latin.uix.settings.useDataStore
import org.futo.inputmethod.latin.uix.settings.useSharedPrefsBool
import org.futo.inputmethod.latin.uix.theme.DarkColorScheme
import org.futo.inputmethod.latin.uix.theme.Doto
import org.futo.inputmethod.latin.uix.theme.Inter
import org.futo.inputmethod.latin.uix.theme.NothingRed
import org.futo.inputmethod.latin.uix.theme.ThemeOptions
import org.futo.inputmethod.latin.uix.theme.presets.NothingDark
import kotlin.math.roundToInt

private val BackgroundColor = Color(0xFF0B0B0C)
private val SurfaceVariant = Color(0xFF1B1B1F)
private val BorderColor = Color(0xFF232326)
private val DividerColor = Color(0xFF1E1E22)
private val TextSecondary = Color(0xFF8E8E93)

@Preview(showBackground = true)
@Composable
fun ResizeKeyboardScreen(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Height & Offset
    val (heightMultiplier, setHeightMultiplier) = useDataStore(
        KeyboardHeightMultiplierSetting.key,
        KeyboardHeightMultiplierSetting.default
    )
    val (bottomOffset, setBottomOffset) = useDataStore(
        KeyboardBottomOffsetSetting.key,
        KeyboardBottomOffsetSetting.default
    )

    var localHeightMultiplier by remember(heightMultiplier) {
        mutableFloatStateOf(if (heightMultiplier.isNaN() || heightMultiplier <= 0f) 1.0f else heightMultiplier.coerceIn(0.70f, 1.30f))
    }
    var localBottomOffset by remember(bottomOffset) {
        mutableFloatStateOf(if (bottomOffset.isNaN() || bottomOffset < 0f) 0f else bottomOffset.coerceIn(0f, 48f))
    }

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

    // Keyboard Visual Settings for realistic preview
    val (currentTheme, _) = useDataStore(THEME_KEY.key, THEME_KEY.default)
    val (spacebarColorKey, _) = useDataStore(SpacebarColorSetting.key, SpacebarColorSetting.default)
    val (spacebarBorderKey, _) = useDataStore(SpacebarBorderSetting.key, SpacebarBorderSetting.default)
    val (keyBorders, _) = useDataStore(KeyBordersSetting.key, KeyBordersSetting.default)
    val (keyHints, _) = useDataStore(KeyHintsSetting.key, KeyHintsSetting.default)
    val (keyHintColor, _) = useDataStore(KeyHintColorSetting.key, KeyHintColorSetting.default)
    val (keyboardFont, _) = useDataStore(KeyboardFontSetting.key, KeyboardFontSetting.default)
    val (actionBarShown, _) = useDataStore(ActionBarDisplayedSetting)
    val (isNumberRowActive, _) = useSharedPrefsBool(Settings.PREF_ENABLE_NUMBER_ROW, false)
    val (isPinSpacebarActive, _) = useSharedPrefsBool(Settings.PREF_PIN_KEYBOARD_NAME_ON_SPACEBAR, true)
    val (isShowEmojiKeyActive, _) = useSharedPrefsBool(Settings.PREF_SHOW_EMOJI_KEY, true)

    var typingTestText by remember { mutableStateOf("") }

    val currentThemeOption = ThemeOptions[currentTheme] ?: NothingDark
    val currentThemeColors = remember(currentTheme, context) {
        try {
            currentThemeOption.obtainColors(context)
        } catch (e: Exception) {
            DarkColorScheme
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .verticalScroll(scrollState)
            .navigationBarsPadding()
            .padding(bottom = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 1. Navigation Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Column {
                    Text(
                        text = "Resize Keyboard",
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Adjust height & bottom padding",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // Reset Pill Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariant)
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .clickable {
                        localHeightMultiplier = 1.0f
                        localBottomOffset = 0.0f
                        setHeightMultiplier(1.0f)
                        setBottomOffset(0.0f)
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = NothingRed,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Reset",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Sliders Card (Top)
        NeuCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = 14.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Height Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Keyboard Height",
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Vertical scale multiplier",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = "${(localHeightMultiplier * 100).roundToInt()}%",
                        fontFamily = Doto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NothingRed
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Slider(
                    value = localHeightMultiplier,
                    onValueChange = {
                        localHeightMultiplier = it
                        setHeightMultiplier(it)
                    },
                    valueRange = 0.70f..1.30f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = NothingRed,
                        inactiveTrackColor = SurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Offset Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Bottom Offset",
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Extra padding above navigation bar",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = "${localBottomOffset.roundToInt()} dp",
                        fontFamily = Doto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NothingRed
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Slider(
                    value = localBottomOffset,
                    onValueChange = {
                        localBottomOffset = it
                        setBottomOffset(it)
                    },
                    valueRange = 0f..48f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = NothingRed,
                        inactiveTrackColor = SurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Section Title: Live Preview
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Live Preview",
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Updates in real-time as you adjust sliders above.",
                fontFamily = Inter,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Live Keyboard Preview (Dynamically Bound to heightMultiplier & bottomOffset)
        RealisticKeyboardPreview(
            currentTheme = currentTheme,
            spacebarColor = currentSpaceColor,
            spacebarBorder = currentSpaceBorder,
            hasKeyBorders = keyBorders,
            showSuggestionRow = actionBarShown,
            showNumberRow = isNumberRowActive,
            pinLayoutNameOnSpacebar = isPinSpacebarActive,
            showEmojiKey = isShowEmojiKeyActive,
            showKeyHints = keyHints,
            keyHintColor = keyHintColor,
            keyboardFont = keyboardFont,
            activeLayoutName = activeLayoutDisplayName,
            heightMultiplier = localHeightMultiplier,
            bottomOffset = localBottomOffset,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 5. Interactive Typing Test Field
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Test Input",
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Tap here to test typing at this height.",
                fontFamily = Inter,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

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
                            text = "type here to test...",
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
                        try {
                            val intent = Intent().apply {
                                setClass(context, TextEditPopupActivity::class.java)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.util.Log.e("ResizeKeyboardScreen", "Failed to start TextEditPopupActivity", e)
                        }
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
