package org.futo.inputmethod.latin.uix.settings.pages

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.futo.inputmethod.announcements.AnnouncementManager
import org.futo.inputmethod.announcements.AnnouncementSheet
import org.futo.inputmethod.latin.BuildConfig
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.settings.Settings
import org.futo.inputmethod.latin.AudioAndHapticFeedbackManager
import org.futo.inputmethod.latin.common.Constants
import org.futo.inputmethod.latin.uix.PreferenceUtils
import org.futo.inputmethod.latin.uix.settings.DeveloperDetailsCard
import org.futo.inputmethod.latin.uix.settings.NavigationItem
import org.futo.inputmethod.latin.uix.settings.NavigationItemStyle
import org.futo.inputmethod.latin.uix.settings.NeuCard
import org.futo.inputmethod.latin.uix.settings.OrbitBoardHeader
import org.futo.inputmethod.latin.uix.settings.SectionHeader
import org.futo.inputmethod.latin.uix.settings.useDataStore
import org.futo.inputmethod.latin.uix.settings.useDataStoreValue
import org.futo.inputmethod.latin.uix.settings.useSharedPrefsBool
import org.futo.inputmethod.latin.uix.settings.useSharedPrefsInt
import org.futo.inputmethod.latin.uix.theme.Doto
import org.futo.inputmethod.latin.uix.theme.Inter
import org.futo.inputmethod.latin.uix.theme.NothingRed
import org.futo.inputmethod.updates.AppUpdate
import org.futo.inputmethod.updates.OrbitUpdateScheduler
import org.futo.inputmethod.updates.UpdateChecker
import org.futo.inputmethod.updates.UpdateSheet
import kotlin.math.roundToInt

private val BackgroundColor = Color(0xFF0B0B0C)
private val SurfaceVariant = Color(0xFF1B1B1F)
private val BorderColor = Color(0xFF232326)
private val DividerColor = Color(0xFF1E1E22)
private val TextSecondary = Color(0xFF8E8E93)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun MoreSettingsScreen(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val isDeveloper = useDataStoreValue(IS_DEVELOPER)

    // Gestures & Feedback settings
    val (vibrateOn, setVibrateOn) = useSharedPrefsBool(Settings.PREF_VIBRATE_ON, true)
    val (vibrationDuration, setVibrationDuration) = useDataStore(vibrationDurationSetting.key, vibrationDurationSetting.default)
    val (soundOn, setSoundOn) = useSharedPrefsBool(Settings.PREF_SOUND_ON, false)
    val (soundVolume, setSoundVolume) = useDataStore(soundVolumeSetting.key, soundVolumeSetting.default)
    val (spacebarMode, setSpacebarMode) = useSharedPrefsInt(Settings.PREF_SPACEBAR_MODE, Settings.SPACEBAR_MODE_SWIPE_LANGUAGE)
    val (doubleSpacePeriod, setDoubleSpacePeriod) = useSharedPrefsBool(Settings.PREF_KEY_USE_DOUBLE_SPACE_PERIOD, true)

    val (longPressShortcuts, setLongPressShortcuts) = useDataStore(LongPressShortcutsSetting.key, LongPressShortcutsSetting.default)
    val (longPressASelectAll, setLongPressASelectAll) = useSharedPrefsBool(Settings.PREF_LONG_PRESS_A_SELECT_ALL, false)
    val (swipeGesture, setSwipeGesture) = useSharedPrefsBool(Settings.PREF_GESTURE_INPUT, true)

    // Voice & Preferences
    val (isVoiceSoundActive, setVoiceSoundActive) = useSharedPrefsBool(Settings.PREF_VOICE_INPUT_SOUND, true)

    // Updates & Announcements State
    val isCheckingUpdate by UpdateChecker.isChecking.collectAsState()
    var showUpdateSheet by remember { mutableStateOf(false) }
    var availableUpdate by remember { mutableStateOf<AppUpdate?>(null) }

    val announcementManager = remember { AnnouncementManager.getInstance(context) }
    val availableAnnouncement by announcementManager.availableAnnouncement.collectAsState()
    var showAnnouncementSheet by remember { mutableStateOf(false) }

    var updateNotificationsEnabled by remember {
        mutableStateOf(OrbitUpdateScheduler.isNotificationsEnabled(context))
    }

    val spaceModes = listOf(
        Triple(Settings.SPACEBAR_MODE_SWIPE_LANGUAGE, "Language", "Switch layout"),
        Triple(Settings.SPACEBAR_MODE_SWIPE_CURSOR, "Cursor", "Move cursor"),
        Triple(Settings.SPACEBAR_MODE_SWIPE_CURSOR_ONLY, "Cursor Only", "Only cursor")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .verticalScroll(scrollState)
            .navigationBarsPadding()
            .padding(bottom = 84.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 1. Orbit Board Header: Logo + Title + Version Chip
        OrbitBoardHeader()

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Section: Haptics & Sound Feedback
        SectionHeader(
            title = "Haptics & Sound",
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))

        NeuCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = 14.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Keypress Vibration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Keypress Vibration",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Haptic feedback on each keystroke",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = vibrateOn,
                        onCheckedChange = { setVibrateOn(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NothingRed,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = SurfaceVariant
                        )
                    )
                }

                if (vibrateOn) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Vibration Duration",
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = Color(0xFFD0D0D5)
                        )
                        Text(
                            text = if (vibrationDuration <= -1) "Default" else "${vibrationDuration} ms",
                            fontFamily = Doto,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NothingRed
                        )
                    }
                    Slider(
                        value = vibrationDuration.toFloat().coerceIn(-1f, 100f),
                        onValueChange = {
                            val duration = it.roundToInt()
                            setVibrationDuration(duration)
                            val sharedPrefs = PreferenceUtils.getDefaultSharedPreferences(context)
                            sharedPrefs.edit().putInt(Settings.PREF_VIBRATION_DURATION_SETTINGS, duration).apply()
                            AudioAndHapticFeedbackManager.getInstance().vibrate(if (duration <= -1) 10L else duration.toLong())
                        },
                        valueRange = -1f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = NothingRed,
                            inactiveTrackColor = SurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
                Spacer(modifier = Modifier.height(12.dp))

                // Keypress Sound
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Keypress Sound",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Auditory click feedback when typing",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = soundOn,
                        onCheckedChange = { setSoundOn(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NothingRed,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = SurfaceVariant
                        )
                    )
                }

                if (soundOn) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Sound Volume",
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = Color(0xFFD0D0D5)
                        )
                        Text(
                            text = if (soundVolume <= -1) "Default" else "${soundVolume}%",
                            fontFamily = Doto,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NothingRed
                        )
                    }
                    Slider(
                        value = soundVolume.toFloat().coerceIn(-1f, 100f),
                        onValueChange = {
                            val volInt = it.roundToInt()
                            setSoundVolume(volInt)
                            val sharedPrefs = PreferenceUtils.getDefaultSharedPreferences(context)
                            sharedPrefs.edit().apply {
                                if (volInt <= -1) {
                                    remove(Settings.PREF_KEYPRESS_SOUND_VOLUME)
                                } else {
                                    putFloat(Settings.PREF_KEYPRESS_SOUND_VOLUME, (volInt.coerceIn(0, 100)) / 100.0f)
                                }
                            }.apply()
                            AudioAndHapticFeedbackManager.getInstance().performAudioFeedback(Constants.CODE_SPACE)
                        },
                        valueRange = -1f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = NothingRed,
                            inactiveTrackColor = SurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Section: Spacebar Settings (Unified Card)
        SectionHeader(
            title = "Spacebar Settings",
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))

        NeuCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = 14.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Spacebar Swipe Behavior
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Spacebar Swipe Gesture",
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Slide across spacebar to switch or move",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Text(
                        text = spaceModes.firstOrNull { it.first == spacebarMode }?.second ?: "Language",
                        fontFamily = Doto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = NothingRed
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    spaceModes.forEach { (mode, name, _) ->
                        val isSelected = mode == spacebarMode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SurfaceVariant else Color(0xFF121215))
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) NothingRed else BorderColor,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { setSpacebarMode(mode) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                fontFamily = Inter,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
                Spacer(modifier = Modifier.height(12.dp))

                // Double Space Period
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Double Tap Space for Period (.)",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Double-tapping space inserts a period and space",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = doubleSpacePeriod,
                        onCheckedChange = { setDoubleSpacePeriod(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NothingRed,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = SurfaceVariant
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. Section: Shortcuts & Gestures (Unified Card)
        SectionHeader(
            title = "Shortcuts & Gestures",
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))

        NeuCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = 14.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Long Press Shortcuts (C, X, V)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Long Press Shortcuts (C, X, V)",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Long press C to copy, X to cut, and V to paste",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = longPressShortcuts,
                        onCheckedChange = { setLongPressShortcuts(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NothingRed,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = SurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
                Spacer(modifier = Modifier.height(12.dp))

                // Long-press 'A' to Select All
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Long-Press 'A' Select All",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Default off (holding 'a' outputs '@'). Turn on for Select All",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = longPressASelectAll,
                        onCheckedChange = { setLongPressASelectAll(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NothingRed,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = SurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
                Spacer(modifier = Modifier.height(12.dp))

                // Swipe Typing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Swipe Typing (Glide)",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Continuous glide gesture typing across keys",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = swipeGesture,
                        onCheckedChange = { setSwipeGesture(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NothingRed,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = SurfaceVariant
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Section: Voice & Tools
        SectionHeader(
            title = "Voice & Tools",
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))

        NeuCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = 14.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Voice Chime
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Voice Typing Sound",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Play audio chime when voice recognition starts",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = isVoiceSoundActive,
                        onCheckedChange = { setVoiceSoundActive(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NothingRed,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = SurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
                Spacer(modifier = Modifier.height(6.dp))

                NavigationItem(
                    title = "Themes & Appearance",
                    subtitle = "All themes, color combinations & typing effects",
                    style = NavigationItemStyle.HomeSecondary,
                    navigate = { navController.navigate("themes") },
                    icon = painterResource(id = R.drawable.themes)
                )
                Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
                NavigationItem(
                    title = "Languages & Subtypes",
                    subtitle = "App and typing languages",
                    style = NavigationItemStyle.HomeSecondary,
                    navigate = { navController.navigate("languages") },
                    icon = painterResource(id = R.drawable.globe)
                )
                Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
                NavigationItem(
                    title = "Advanced Typing & Input",
                    subtitle = "Key long-press delays, double-space period, etc.",
                    style = NavigationItemStyle.HomeSecondary,
                    navigate = { navController.navigate("typing") },
                    icon = painterResource(id = R.drawable.keyboard)
                )
                Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
                NavigationItem(
                    title = "Text Prediction & Dictionary",
                    subtitle = "Suggestions, next-word, and custom dictionary",
                    style = NavigationItemStyle.HomeSecondary,
                    navigate = { navController.navigate("predictiveText") },
                    icon = painterResource(id = R.drawable.text_prediction)
                )
                Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
                NavigationItem(
                    title = "Announcements",
                    subtitle = "News and app updates",
                    style = NavigationItemStyle.HomeSecondary,
                    navigate = {
                        announcementManager.checkForAnnouncements(coroutineScope, forceShow = true) { res ->
                            res.fold(
                                onSuccess = { ann ->
                                    if (ann != null) {
                                        showAnnouncementSheet = true
                                    } else {
                                        Toast.makeText(context, "No new announcements", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onFailure = {
                                    Toast.makeText(context, "No new announcements", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    icon = painterResource(id = R.drawable.ic_campaign)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. Section: Updates
        SectionHeader(
            title = "Updates",
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))

        NeuCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = 12.dp,
            onClick = {
                if (!isCheckingUpdate) {
                    coroutineScope.launch {
                        val result = UpdateChecker.checkForUpdate()
                        result.fold(
                            onSuccess = { update ->
                                if (update.isUpdateAvailable) {
                                availableUpdate = update
                                showUpdateSheet = true
                            } else {
                                Toast.makeText(
                                    context,
                                    "Orbit Board is up to date (v${BuildConfig.VERSION_NAME})",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onFailure = {
                            Toast.makeText(
                                context,
                                "Orbit Board is up to date (v${BuildConfig.VERSION_NAME})",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
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
                        painter = painterResource(id = R.drawable.activity),
                        contentDescription = null,
                        tint = NothingRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        "Check for Updates",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Text(
                        "Version ${BuildConfig.VERSION_NAME}",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            if (isCheckingUpdate) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = NothingRed,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "check",
                    fontFamily = Doto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = NothingRed
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    NeuCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Update notifications",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Text(
                    "Notify in status bar when updates are released",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            Switch(
                checked = updateNotificationsEnabled,
                onCheckedChange = { isChecked ->
                    updateNotificationsEnabled = isChecked
                    OrbitUpdateScheduler.setNotificationsEnabled(context, isChecked)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = NothingRed,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = SurfaceVariant
                )
            )
        }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // 5. Section: About Orbit Board
    SectionHeader(
        title = "About Orbit Board",
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    Spacer(modifier = Modifier.height(4.dp))

    DeveloperDetailsCard(modifier = Modifier.padding(horizontal = 16.dp))

    if (isDeveloper || LocalInspectionMode.current) {
        Spacer(modifier = Modifier.height(12.dp))
        NeuCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = 4.dp
        ) {
            NavigationItem(
                title = "Developer Settings",
                subtitle = "Debug options & developer flags",
                style = NavigationItemStyle.HomeTertiary,
                navigate = { navController.navigate("developer") },
                icon = painterResource(id = R.drawable.code)
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
}

if (showUpdateSheet && availableUpdate != null) {
    UpdateSheet(
        update = availableUpdate!!,
        onDismiss = { showUpdateSheet = false }
    )
}

if (showAnnouncementSheet && availableAnnouncement != null) {
    AnnouncementSheet(
        announcement = availableAnnouncement!!,
        onDismiss = {
            showAnnouncementSheet = false
            availableAnnouncement?.let { announcementManager.dismissAnnouncement(it.id) }
        }
    )
}
}
