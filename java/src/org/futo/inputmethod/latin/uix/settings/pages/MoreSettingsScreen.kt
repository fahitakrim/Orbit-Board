package org.futo.inputmethod.latin.uix.settings.pages

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.futo.inputmethod.announcements.AnnouncementManager
import org.futo.inputmethod.announcements.AnnouncementSheet
import org.futo.inputmethod.latin.AudioAndHapticFeedbackManager
import org.futo.inputmethod.latin.BuildConfig
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.SubtypesSetting
import org.futo.inputmethod.latin.common.Constants
import org.futo.inputmethod.latin.settings.Settings
import org.futo.inputmethod.latin.uix.AUDIO_FOCUS
import org.futo.inputmethod.latin.uix.DISALLOW_SYMBOLS
import org.futo.inputmethod.latin.uix.PREFER_BLUETOOTH
import org.futo.inputmethod.latin.uix.PreferenceUtils
import org.futo.inputmethod.latin.uix.SHOW_EMOJI_SUGGESTIONS
import org.futo.inputmethod.latin.uix.SettingsKey
import org.futo.inputmethod.latin.uix.USE_SYSTEM_VOICE_INPUT
import org.futo.inputmethod.latin.uix.actions.ClipboardHistoryEnabled
import org.futo.inputmethod.latin.uix.settings.DeveloperDetailsCard
import org.futo.inputmethod.latin.uix.settings.ExpandableSettingsCard
import org.futo.inputmethod.latin.uix.settings.NeuCard
import org.futo.inputmethod.latin.uix.settings.OrbitBoardHeader
import org.futo.inputmethod.latin.uix.settings.PillButton
import org.futo.inputmethod.latin.uix.settings.ScreenTitle
import org.futo.inputmethod.latin.uix.settings.SegmentedControl
import org.futo.inputmethod.latin.uix.settings.SettingsDivider
import org.futo.inputmethod.latin.uix.settings.SettingsRow
import org.futo.inputmethod.latin.uix.settings.SettingsSurfaceVariant
import org.futo.inputmethod.latin.uix.settings.useDataStore
import org.futo.inputmethod.latin.uix.settings.useDataStoreValue
import org.futo.inputmethod.latin.uix.settings.useSharedPrefsBool
import org.futo.inputmethod.latin.uix.settings.useSharedPrefsInt
import org.futo.inputmethod.latin.uix.settings.useSharedPrefsString
import org.futo.inputmethod.latin.uix.theme.Doto
import org.futo.inputmethod.latin.uix.theme.Inter
import org.futo.inputmethod.latin.uix.theme.NothingRed
import org.futo.inputmethod.updates.AppUpdate
import org.futo.inputmethod.updates.OrbitUpdateScheduler
import org.futo.inputmethod.updates.UpdateChecker
import org.futo.inputmethod.updates.UpdateSheet
import java.util.Locale
import kotlin.math.roundToInt

val vibrationDurationSetting = SettingsKey(
    intPreferencesKey("vibration_duration"),
    -1
)

val soundVolumeSetting = SettingsKey(
    intPreferencesKey("keypress_sound_volume_percent"),
    -1
)

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
    showBack: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDeveloper = useDataStoreValue(IS_DEVELOPER)

    // Single-expanded accordion index (0 to 6) or null for all collapsed
    var expandedCardIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    fun toggleCard(index: Int) {
        expandedCardIndex = if (expandedCardIndex == index) null else index
    }

    // Card 1: Typing & Input State
    val (autoCorrection, setAutoCorrection) = useSharedPrefsBool(Settings.PREF_AUTO_CORRECTION, false)
    val (autoCap, setAutoCap) = useSharedPrefsBool(Settings.PREF_AUTO_CAP, false)
    val (swipeGesture, setSwipeGesture) = useSharedPrefsBool(Settings.PREF_GESTURE_INPUT, true)
    val (doubleSpacePeriod, setDoubleSpacePeriod) = useSharedPrefsBool(Settings.PREF_KEY_USE_DOUBLE_SPACE_PERIOD, true)
    val (spacebarMode, setSpacebarMode) = useSharedPrefsInt(Settings.PREF_SPACEBAR_MODE, Settings.SPACEBAR_MODE_SWIPE_LANGUAGE)
    val (backspaceMode, setBackspaceMode) = useSharedPrefsInt(Settings.PREF_BACKSPACE_MODE, Settings.BACKSPACE_MODE_CHARACTERS)
    val (avroSpacebarMode, setAvroSpacebarMode) = useSharedPrefsString(Settings.PREF_AVRO_SPACEBAR_MODE, Settings.DEFAULT_AVRO_SPACEBAR_MODE)
    val (volumeKeyCursor, setVolumeKeyCursor) = useSharedPrefsBool(Settings.PREF_VOLUME_KEY_CURSOR, false)

    val spaceModes = listOf(
        Triple(Settings.SPACEBAR_MODE_SWIPE_LANGUAGE, "Language", "Switch layout"),
        Triple(Settings.SPACEBAR_MODE_SWIPE_CURSOR, "Cursor", "Move cursor"),
        Triple(Settings.SPACEBAR_MODE_SWIPE_CURSOR_ONLY, "Cursor Only", "Only cursor")
    )
    val activeSpaceModeName = spaceModes.firstOrNull { it.first == spacebarMode }?.second ?: "Language"

    // Card 2: Suggestions & Action Bar State
    val (actionBarShown, setActionBarShown) = useDataStore(ActionBarDisplayedSetting)
    val (showSuggestions, setShowSuggestions) = useSharedPrefsBool(Settings.PREF_SHOW_SUGGESTIONS, true)
    val (bigramPredictions, setBigramPredictions) = useSharedPrefsBool(Settings.PREF_BIGRAM_PREDICTIONS, true)
    val (showEmojiSuggestions, setShowEmojiSuggestions) = useDataStore(SHOW_EMOJI_SUGGESTIONS)
    val (usePersonalizedDicts, setUsePersonalizedDicts) = useSharedPrefsBool(Settings.PREF_KEY_USE_PERSONALIZED_DICTS, true)

    // Card 3: Haptics & Sound State
    val (vibrateOn, setVibrateOn) = useSharedPrefsBool(Settings.PREF_VIBRATE_ON, true)
    val (vibrationDuration, setVibrationDuration) = useDataStore(vibrationDurationSetting.key, vibrationDurationSetting.default)
    val (soundOn, setSoundOn) = useSharedPrefsBool(Settings.PREF_SOUND_ON, false)
    val (soundVolume, setSoundVolume) = useDataStore(soundVolumeSetting.key, soundVolumeSetting.default)

    var localVibrationDuration by remember(vibrationDuration) {
        mutableFloatStateOf(vibrationDuration.toFloat().coerceIn(-1f, 100f))
    }
    var localSoundVolume by remember(soundVolume) {
        mutableFloatStateOf(soundVolume.toFloat().coerceIn(-1f, 100f))
    }

    // Card 4: Layout & Keys State
    val (isNumberRowActive, setNumberRowActive) = useSharedPrefsBool(Settings.PREF_ENABLE_NUMBER_ROW, false)
    val (isShowEmojiKeyActive, setShowEmojiKeyActive) = useSharedPrefsBool(Settings.PREF_SHOW_EMOJI_KEY, true)
    val (longPressShortcuts, setLongPressShortcuts) = useDataStore(LongPressShortcutsSetting.key, LongPressShortcutsSetting.default)
    val (longPressASelectAll, setLongPressASelectAll) = useSharedPrefsBool(Settings.PREF_LONG_PRESS_A_SELECT_ALL, false)
    val (longPressTimeout, setLongPressTimeout) = useSharedPrefsInt(Settings.PREF_KEY_LONGPRESS_TIMEOUT, 300)
    var localLongPressTimeout by remember(longPressTimeout) {
        mutableFloatStateOf(longPressTimeout.toFloat().coerceIn(100f, 700f))
    }

    // Card 5: Voice & Input Tools State
    val (voiceInputEnabled, setVoiceInputEnabled) = useSharedPrefsBool(Settings.PREF_VOICE_INPUT_KEY, true)
    val (isVoiceSoundActive, setVoiceSoundActive) = useSharedPrefsBool(Settings.PREF_VOICE_INPUT_SOUND, true)
    val (useSystemVoice, setUseSystemVoice) = useDataStore(USE_SYSTEM_VOICE_INPUT.key, USE_SYSTEM_VOICE_INPUT.default)
    val (preferBluetooth, setPreferBluetooth) = useDataStore(PREFER_BLUETOOTH.key, PREFER_BLUETOOTH.default)
    val (audioFocus, setAudioFocus) = useDataStore(AUDIO_FOCUS.key, AUDIO_FOCUS.default)
    val (suppressSymbols, setSuppressSymbols) = useDataStore(DISALLOW_SYMBOLS.key, DISALLOW_SYMBOLS.default)
    val (clipboardHistory, setClipboardHistory) = useDataStore(ClipboardHistoryEnabled)

    // Card 6: Languages & Subtypes State
    val inputMethods = useDataStoreValue(SubtypesSetting)
    val activeLanguagesCount = if (inputMethods.isEmpty()) 1 else inputMethods.size
    val activeLanguageNamesSummary = remember(inputMethods) {
        if (inputMethods.isEmpty()) {
            "English (US)"
        } else {
            inputMethods.mapNotNull {
                try {
                    val localeStr = it.split(":")[0]
                    val parts = localeStr.split("_")
                    val locale = if (parts.size > 1) Locale(parts[0], parts[1]) else Locale(parts[0])
                    locale.getDisplayLanguage(Locale.ENGLISH).ifEmpty { null }
                } catch (e: Exception) {
                    null
                }
            }.distinct().joinToString(", ").ifEmpty { "English" }
        }
    }

    // Card 7: Updates & Announcements State
    val isCheckingUpdate by UpdateChecker.isChecking.collectAsState()
    var showUpdateSheet by remember { mutableStateOf(false) }
    var availableUpdate by remember { mutableStateOf<AppUpdate?>(null) }
    val announcementManager = remember { AnnouncementManager.getInstance(context) }
    val availableAnnouncement by announcementManager.availableAnnouncement.collectAsState()
    var showAnnouncementSheet by remember { mutableStateOf(false) }
    var updateNotificationsEnabled by remember {
        mutableStateOf(OrbitUpdateScheduler.isNotificationsEnabled(context))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .navigationBarsPadding(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 10.dp,
            bottom = 160.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Header
        item(key = "header") {
            if (showBack) {
                ScreenTitle("Settings", showBack = true, navController)
            } else {
                OrbitBoardHeader()
            }
        }

        // ==========================================
        // Card 1: Typing & Input
        // ==========================================
        item(key = "card_typing") {
            ExpandableSettingsCard(
                title = "Typing & Input",
                summary = "Auto-cap: ${if (autoCap) "On" else "Off"} • Glide: ${if (swipeGesture) "On" else "Off"} • $activeSpaceModeName Swipe",
                isExpanded = expandedCardIndex == 0,
                onToggle = { toggleCard(0) },
                icon = Icons.Rounded.Edit,
                accentColor = NothingRed
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp)) {
                    SettingsRow(
                        title = "Auto-correction",
                        subtitle = if (autoCorrection) "Auto-commit top suggestion" else "Commit exact typed text",
                        trailingContent = {
                            SegmentedControl(
                                options = listOf("Off", "On"),
                                selectedIndex = if (autoCorrection) 1 else 0,
                                onSelect = { setAutoCorrection(it == 1) }
                            )
                        }
                    )

                    SettingsDivider()

                    SettingsRow(
                        title = "Auto-capitalization",
                        subtitle = "Capitalize first word of sentences",
                        trailingContent = {
                            SegmentedControl(
                                options = listOf("Off", "On"),
                                selectedIndex = if (autoCap) 1 else 0,
                                onSelect = { setAutoCap(it == 1) }
                            )
                        }
                    )

                    SettingsDivider()

                    SettingsRow(
                        title = "Swipe Typing (Glide)",
                        subtitle = "Glide across keys to type",
                        trailingContent = {
                            SegmentedControl(
                                options = listOf("Off", "On"),
                                selectedIndex = if (swipeGesture) 1 else 0,
                                onSelect = { setSwipeGesture(it == 1) }
                            )
                        }
                    )

                    SettingsDivider()

                    SettingsRow(
                        title = "Double-Tap Space for Period",
                        subtitle = "Insert period on double tap",
                        trailingContent = {
                            SegmentedControl(
                                options = listOf("Off", "On"),
                                selectedIndex = if (doubleSpacePeriod) 1 else 0,
                                onSelect = { setDoubleSpacePeriod(it == 1) }
                            )
                        }
                    )

                    SettingsDivider()

                    SettingsRow(
                        title = "Spacebar Swipe Gesture",
                        subtitle = "Switch language or move cursor",
                        trailingContent = {
                            val selectedIdx = when (spacebarMode) {
                                Settings.SPACEBAR_MODE_SWIPE_CURSOR -> 1
                                Settings.SPACEBAR_MODE_SWIPE_CURSOR_ONLY -> 2
                                else -> 0
                            }
                            SegmentedControl(
                                options = listOf("Lang", "Cursor", "Lock"),
                                selectedIndex = selectedIdx,
                                onSelect = { setSpacebarMode(spaceModes[it].first) }
                            )
                        }
                    )

                    SettingsDivider()

                    SettingsRow(
                        title = "Avro Spacebar Mode",
                        subtitle = if (avroSpacebarMode == Settings.AVRO_SPACEBAR_MODE_SMART_CANDIDATE)
                            "Smart candidate (kmn -> কেমন)"
                        else
                            "Exact phonetic (kmn -> কম্ন)",
                        trailingContent = {
                            SegmentedControl(
                                options = listOf("Exact", "Smart"),
                                selectedIndex = if (avroSpacebarMode == Settings.AVRO_SPACEBAR_MODE_SMART_CANDIDATE) 1 else 0,
                                onSelect = {
                                    setAvroSpacebarMode(
                                        if (it == 1) Settings.AVRO_SPACEBAR_MODE_SMART_CANDIDATE
                                        else Settings.AVRO_SPACEBAR_MODE_EXACT_PHONETIC
                                    )
                                }
                            )
                        }
                    )

                    SettingsDivider()

                    SettingsRow(
                        title = "Backspace Behavior",
                        subtitle = "Delete by character or word",
                        trailingContent = {
                            SegmentedControl(
                                options = listOf("Chars", "Words"),
                                selectedIndex = if (backspaceMode == Settings.BACKSPACE_MODE_WORDS) 1 else 0,
                                onSelect = {
                                    setBackspaceMode(if (it == 1) Settings.BACKSPACE_MODE_WORDS else Settings.BACKSPACE_MODE_CHARACTERS)
                                }
                            )
                        }
                    )

                    SettingsDivider()

                    SettingsRow(
                        title = "Volume Key Cursor Control",
                        subtitle = "Move cursor left or right with volume buttons",
                        trailingContent = {
                            SegmentedControl(
                                options = listOf("Off", "On"),
                                selectedIndex = if (volumeKeyCursor) 1 else 0,
                                onSelect = { setVolumeKeyCursor(it == 1) }
                            )
                        }
                    )
                }
            }
        }

        // ==========================================
        // Card 2: Suggestions & Action Bar
        // ==========================================
        item(key = "card_suggestions") {
            ExpandableSettingsCard(
                title = "Suggestions & Action Bar",
                summary = "${if (actionBarShown) "Bar Active" else "Bar Hidden"} • ${if (bigramPredictions) "Predictions On" else "Basic"}",
                isExpanded = expandedCardIndex == 1,
                onToggle = { toggleCard(1) },
                icon = Icons.Rounded.Dashboard,
                accentColor = NothingRed
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp)) {
                    SettingsRow(
                        title = "Suggestion & Action Bar",
                        subtitle = "Show suggestions and actions row above the keys",
                        trailingContent = {
                            SegmentedControl(
                                options = listOf("Off", "On"),
                                selectedIndex = if (actionBarShown) 1 else 0,
                                onSelect = { setActionBarShown(it == 1) }
                            )
                        }
                    )

                    SettingsDivider()

                    SettingsRow(
                        title = "Show Suggestions",
                        subtitle = "Display word suggestions above the keyboard",
                        trailingContent = {
                            SegmentedControl(
                                options = listOf("Off", "On"),
                                selectedIndex = if (showSuggestions) 1 else 0,
                                onSelect = { setShowSuggestions(it == 1) }
                            )
                        }
                    )

                    SettingsDivider()

                    SettingsRow(
                        title = "Next-Word Predictions",
                        subtitle = "Use previous words to predict the next word",
                        trailingContent = {
                            SegmentedControl(
                                options = listOf("Off", "On"),
                                selectedIndex = if (bigramPredictions) 1 else 0,
                                onSelect = { setBigramPredictions(it == 1) }
                            )
                        }
                    )

                    SettingsDivider()

                    SettingsRow(
                        title = "Emoji Suggestions",
                        subtitle = "Suggest emojis while typing",
                        trailingContent = {
                            SegmentedControl(
                                options = listOf("Off", "On"),
                                selectedIndex = if (showEmojiSuggestions) 1 else 0,
                                onSelect = { setShowEmojiSuggestions(it == 1) }
                            )
                        }
                    )

                    SettingsDivider()

                    SettingsRow(
                        title = "Personalized Dictionary",
                        subtitle = "Learn from what you type to improve suggestions",
                        trailingContent = {
                            SegmentedControl(
                                options = listOf("Off", "On"),
                                selectedIndex = if (usePersonalizedDicts) 1 else 0,
                                onSelect = { setUsePersonalizedDicts(it == 1) }
                            )
                        }
                    )

                    SettingsDivider()

                    SettingsRow(
                        title = "Action Bar Tools",
                        subtitle = "Customize quick actions on the action bar",
                        trailingContent = {
                            PillButton(
                                text = "Edit",
                                onClick = { navController.navigate("actionEdit") },
                                filled = false
                            )
                        }
                    )

                    SettingsDivider()

                    SettingsRow(
                        title = "Blacklisted Words",
                        subtitle = "Manage blocked words from suggestions",
                        trailingContent = {
                            PillButton(
                                text = "Manage",
                                onClick = { navController.navigate("blacklist") },
                                filled = false
                            )
                        }
                    )

                    SettingsDivider()

                    SettingsRow(
                        title = "System User Dictionary",
                        subtitle = "Android system-wide personal word list",
                        trailingContent = {
                            PillButton(
                                text = "Open",
                                onClick = {
                                    val intent = Intent("android.settings.USER_DICTIONARY_SETTINGS").apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                },
                                filled = false
                            )
                        }
                    )
                }
            }
        }

        // ==========================================
        // Card 3: Haptics & Sound
        // ==========================================
        item(key = "card_haptics") {
            ExpandableSettingsCard(
                title = "Haptics & Sound",
                summary = "Vibrate: ${if (vibrateOn) (if (localVibrationDuration.roundToInt() <= -1) "Default" else "${localVibrationDuration.roundToInt()}ms") else "Off"} • Sound: ${if (soundOn) (if (localSoundVolume.roundToInt() <= -1) "Default" else "${localSoundVolume.roundToInt()}%") else "Off"}",
                isExpanded = expandedCardIndex == 2,
                onToggle = { toggleCard(2) },
                icon = Icons.Rounded.VolumeUp,
                accentColor = NothingRed
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp)) {
                    SettingsRow(
                        title = "Keypress Vibration",
                        subtitle = "Haptic feedback on each keystroke",
                        trailingContent = {
                            SegmentedControl(
                                options = listOf("Off", "On"),
                                selectedIndex = if (vibrateOn) 1 else 0,
                                onSelect = { setVibrateOn(it == 1) }
                            )
                        }
                    )

                    if (vibrateOn) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
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
                                    text = if (localVibrationDuration.roundToInt() <= -1) "Default" else "${localVibrationDuration.roundToInt()} ms",
                                    fontFamily = Doto,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = NothingRed
                                )
                            }
                            Slider(
                                value = localVibrationDuration,
                                onValueChange = { localVibrationDuration = it },
                                onValueChangeFinished = {
                                    val duration = localVibrationDuration.roundToInt()
                                    setVibrationDuration(duration)
                                    val sharedPrefs = PreferenceUtils.getDefaultSharedPreferences(context)
                                    sharedPrefs.edit().putInt(Settings.PREF_VIBRATION_DURATION_SETTINGS, duration).apply()
                                    if (duration <= -1) {
                                        AudioAndHapticFeedbackManager.getInstance().performHapticFeedback(null, false)
                                    } else {
                                        AudioAndHapticFeedbackManager.getInstance().vibrate(duration.toLong())
                                    }
                                },
                                valueRange = -1f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = NothingRed,
                                    inactiveTrackColor = SettingsSurfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    SettingsDivider()

                    SettingsRow(
                        title = "Keypress Sound",
                        subtitle = "Auditory click feedback when typing",
                        trailingContent = {
                            SegmentedControl(
                                options = listOf("Off", "On"),
                                selectedIndex = if (soundOn) 1 else 0,
                                onSelect = { setSoundOn(it == 1) }
                            )
                        }
                    )

                    if (soundOn) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
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
                                    text = if (localSoundVolume.roundToInt() <= -1) "Default" else "${localSoundVolume.roundToInt()}%",
                                    fontFamily = Doto,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = NothingRed
                                )
                            }
                            Slider(
                                value = localSoundVolume,
                                onValueChange = { localSoundVolume = it },
                                onValueChangeFinished = {
                                    val volInt = localSoundVolume.roundToInt()
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
                                    inactiveTrackColor = SettingsSurfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // ==========================================
        // Card 4: Layout & Keys
        // ==========================================
        item(key = "card_layout") {
            ExpandableSettingsCard(
                title = "Layout & Keys",
                summary = "Digits: ${if (isNumberRowActive) "On" else "Off"} • Delay: ${localLongPressTimeout.roundToInt()}ms",
                isExpanded = expandedCardIndex == 3,
                onToggle = { toggleCard(3) },
                icon = Icons.Rounded.TouchApp,
                accentColor = NothingRed
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp)) {
                    SettingsRow(
                        title = "Number Row",
                        subtitle = "Dedicated digits 0–9 above the letters",
                        trailingContent = {
                            SegmentedControl(
                                options = listOf("Off", "On"),
                                selectedIndex = if (isNumberRowActive) 1 else 0,
                                onSelect = { setNumberRowActive(it == 1) }
                            )
                        }
                    )

                    SettingsDivider()

                    SettingsRow(
                        title = "Show Emoji Key",
                        subtitle = "Show dedicated emoji key in the bottom row",
                        trailingContent = {
                            SegmentedControl(
                                options = listOf("Off", "On"),
                                selectedIndex = if (isShowEmojiKeyActive) 1 else 0,
                                onSelect = { setShowEmojiKeyActive(it == 1) }
                            )
                        }
                    )

                    SettingsDivider()

                    SettingsRow(
                        title = "Long-Press Shortcuts (C, X, V)",
                        subtitle = "Long press C to copy, X to cut, and V to paste",
                        trailingContent = {
                            SegmentedControl(
                                options = listOf("Off", "On"),
                                selectedIndex = if (longPressShortcuts) 1 else 0,
                                onSelect = { setLongPressShortcuts(it == 1) }
                            )
                        }
                    )

                    SettingsDivider()

                    SettingsRow(
                        title = "Long-Press 'A' Select All",
                        subtitle = "Holding 'a' outputs '@' (Off) or Select All (On)",
                        trailingContent = {
                            SegmentedControl(
                                options = listOf("Off", "On"),
                                selectedIndex = if (longPressASelectAll) 1 else 0,
                                onSelect = { setLongPressASelectAll(it == 1) }
                            )
                        }
                    )

                    SettingsDivider()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = "Long-Press Delay",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Time required to trigger long-press actions",
                                    fontFamily = Inter,
                                    fontSize = 11.5.sp,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = "${localLongPressTimeout.roundToInt()} ms",
                                fontFamily = Doto,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = NothingRed
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = localLongPressTimeout,
                            onValueChange = { localLongPressTimeout = it },
                            onValueChangeFinished = {
                                setLongPressTimeout(localLongPressTimeout.roundToInt())
                            },
                            valueRange = 100f..700f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = NothingRed,
                                inactiveTrackColor = SettingsSurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    SettingsDivider()

                    SettingsRow(
                        title = "Long-Press Key Arrangement",
                        subtitle = "Configure secondary symbol layout and custom keys",
                        trailingContent = {
                            PillButton(
                                text = "Configure",
                                onClick = { navController.navigate("longPress") },
                                filled = false
                            )
                        }
                    )
                }
            }
        }

        // ==========================================
        // Card 5: Voice & Input Tools
        // ==========================================
        item(key = "card_voice") {
            ExpandableSettingsCard(
                title = "Voice & Input Tools",
                summary = "Voice: ${if (voiceInputEnabled) "On" else "Off"} • Clipboard: ${if (clipboardHistory) "On" else "Off"}",
                isExpanded = expandedCardIndex == 4,
                onToggle = { toggleCard(4) },
                icon = Icons.Rounded.Mic,
                accentColor = NothingRed
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp)) {
                    SettingsRow(
                        title = "Voice Input",
                        subtitle = "Show microphone key and enable voice dictation",
                        trailingContent = {
                            SegmentedControl(
                                options = listOf("Off", "On"),
                                selectedIndex = if (voiceInputEnabled) 1 else 0,
                                onSelect = { setVoiceInputEnabled(it == 1) }
                            )
                        }
                    )

                    if (voiceInputEnabled) {
                        SettingsDivider()

                        SettingsRow(
                            title = "Voice Typing Sound",
                            subtitle = "Play audio chime when voice recognition starts",
                            trailingContent = {
                                SegmentedControl(
                                    options = listOf("Off", "On"),
                                    selectedIndex = if (isVoiceSoundActive) 1 else 0,
                                    onSelect = { setVoiceSoundActive(it == 1) }
                                )
                            }
                        )

                        SettingsDivider()

                        SettingsRow(
                            title = "Voice Engine",
                            subtitle = "Built-in offline voice or external system voice",
                            trailingContent = {
                                SegmentedControl(
                                    options = listOf("Built-in", "System"),
                                    selectedIndex = if (useSystemVoice) 1 else 0,
                                    onSelect = { setUseSystemVoice(it == 1) }
                                )
                            }
                        )

                        SettingsDivider()

                        SettingsRow(
                            title = "Prefer Bluetooth Mic",
                            subtitle = "Record audio via Bluetooth SCO if headset is connected",
                            trailingContent = {
                                SegmentedControl(
                                    options = listOf("Off", "On"),
                                    selectedIndex = if (preferBluetooth) 1 else 0,
                                    onSelect = { setPreferBluetooth(it == 1) }
                                )
                            }
                        )

                        SettingsDivider()

                        SettingsRow(
                            title = "Audio Focus",
                            subtitle = "Pause background music/videos while dictating",
                            trailingContent = {
                                SegmentedControl(
                                    options = listOf("Off", "On"),
                                    selectedIndex = if (audioFocus) 1 else 0,
                                    onSelect = { setAudioFocus(it == 1) }
                                )
                            }
                        )

                        SettingsDivider()

                        SettingsRow(
                            title = "Suppress Symbols",
                            subtitle = "Prevent automatic punctuation and symbol insertion",
                            trailingContent = {
                                SegmentedControl(
                                    options = listOf("Off", "On"),
                                    selectedIndex = if (suppressSymbols) 1 else 0,
                                    onSelect = { setSuppressSymbols(it == 1) }
                                )
                            }
                        )
                    }

                    SettingsDivider()

                    SettingsRow(
                        title = "Clipboard History",
                        subtitle = "Save and quickly paste copied text snippets",
                        trailingContent = {
                            SegmentedControl(
                                options = listOf("Off", "On"),
                                selectedIndex = if (clipboardHistory) 1 else 0,
                                onSelect = { setClipboardHistory(it == 1) }
                            )
                        }
                    )
                }
            }
        }

        // ==========================================
        // Card 6: Languages & Subtypes
        // ==========================================
        item(key = "card_languages") {
            ExpandableSettingsCard(
                title = "Languages & Subtypes",
                summary = "$activeLanguagesCount Active • $activeLanguageNamesSummary",
                isExpanded = expandedCardIndex == 5,
                onToggle = { toggleCard(5) },
                icon = Icons.Rounded.Language,
                accentColor = NothingRed
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp)) {
                    SettingsRow(
                        title = "Active Languages",
                        subtitle = activeLanguageNamesSummary,
                        trailingContent = {
                            PillButton(
                                text = "Manage",
                                onClick = { navController.navigate("languages") },
                                filled = false
                            )
                        }
                    )

                    SettingsDivider()

                    SettingsRow(
                        title = "Add Language",
                        subtitle = "Install additional languages, layouts & voice models",
                        trailingContent = {
                            PillButton(
                                text = "Add",
                                onClick = { navController.navigate("addLanguage") },
                                filled = true
                            )
                        }
                    )

                    SettingsDivider()

                    SettingsRow(
                        title = "Bengali Board Formats",
                        subtitle = "Configure Probhat, National, Avro & phonetic styles",
                        trailingContent = {
                            PillButton(
                                text = "My Space",
                                onClick = { navController.navigate("MySpace") },
                                filled = false
                            )
                        }
                    )
                }
            }
        }

        // ==========================================
        // Card 7: Updates & Announcements
        // ==========================================
        item(key = "card_updates") {
            ExpandableSettingsCard(
                title = "Updates & Announcements",
                summary = "v${BuildConfig.VERSION_NAME} • ${if (updateNotificationsEnabled) "Auto-notify On" else "Manual"}",
                isExpanded = expandedCardIndex == 6,
                onToggle = { toggleCard(6) },
                icon = Icons.Rounded.SystemUpdate,
                accentColor = NothingRed
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp)) {
                    // Check for updates row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceVariant)
                            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                            .clickable {
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
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.activity),
                                contentDescription = null,
                                tint = NothingRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    "Check for Updates",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    "Version ${BuildConfig.VERSION_NAME}",
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        if (isCheckingUpdate) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = NothingRed,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "CHECK",
                                fontFamily = Doto,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = NothingRed
                            )
                        }
                    }

                    SettingsDivider()

                    SettingsRow(
                        title = "Update Notifications",
                        subtitle = "Notify in status bar when updates are released",
                        trailingContent = {
                            SegmentedControl(
                                options = listOf("Off", "On"),
                                selectedIndex = if (updateNotificationsEnabled) 1 else 0,
                                onSelect = { isChecked ->
                                    updateNotificationsEnabled = isChecked == 1
                                    OrbitUpdateScheduler.setNotificationsEnabled(context, isChecked == 1)
                                }
                            )
                        }
                    )

                    SettingsDivider()

                    SettingsRow(
                        title = "Announcements & Notes",
                        subtitle = "View developer announcements and update release notes",
                        trailingContent = {
                            PillButton(
                                text = "View",
                                onClick = {
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
                                filled = false
                            )
                        }
                    )
                }
            }
        }

        // ==========================================
        // Card 8: About Orbit Board (Permanently Visible)
        // ==========================================
        item(key = "card_about") {
            NeuCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 14.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceVariant)
                                .border(1.dp, BorderColor, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.code),
                                contentDescription = null,
                                tint = NothingRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "About Orbit Board",
                                fontFamily = Inter,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(
                                text = "v${BuildConfig.VERSION_NAME}",
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
                    Spacer(modifier = Modifier.height(12.dp))

                    DeveloperDetailsCard()

                    if (isDeveloper || LocalInspectionMode.current) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = "Developer Settings",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Debug options & developer flags",
                                    fontFamily = Inter,
                                    fontSize = 11.5.sp,
                                    color = TextSecondary
                                )
                            }
                            PillButton(
                                text = "Open",
                                onClick = { navController.navigate("developer") },
                                filled = false
                            )
                        }
                    }
                }
            }
        }
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
