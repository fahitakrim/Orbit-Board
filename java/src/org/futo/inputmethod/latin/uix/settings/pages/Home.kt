package org.futo.inputmethod.latin.uix.settings.pages

import android.content.Intent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.futo.inputmethod.announcements.Announcement
import org.futo.inputmethod.announcements.AnnouncementManager
import org.futo.inputmethod.announcements.AnnouncementSheet
import org.futo.inputmethod.latin.ActiveSubtype
import org.futo.inputmethod.latin.BuildConfig
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.Subtypes
import org.futo.inputmethod.latin.SubtypesSetting
import org.futo.inputmethod.latin.uix.TextEditPopupActivity
import org.futo.inputmethod.latin.uix.getSettingBlocking
import org.futo.inputmethod.latin.uix.setSettingBlocking
import org.futo.inputmethod.latin.uix.settings.DeveloperDetailsCard
import org.futo.inputmethod.latin.uix.settings.NavigationItem
import org.futo.inputmethod.latin.uix.settings.NavigationItemStyle
import org.futo.inputmethod.latin.uix.settings.NeuCard
import org.futo.inputmethod.latin.uix.settings.SectionHeader
import org.futo.inputmethod.latin.settings.Settings
import org.futo.inputmethod.latin.uix.settings.useDataStore
import org.futo.inputmethod.latin.uix.settings.useDataStoreValue
import org.futo.inputmethod.latin.uix.settings.useSharedPrefsBool
import org.futo.inputmethod.latin.uix.theme.Doto
import org.futo.inputmethod.latin.uix.theme.Inter
import org.futo.inputmethod.updates.AppUpdate
import org.futo.inputmethod.updates.UpdateChecker
import org.futo.inputmethod.updates.UpdateSheet

private val NothingRed = Color(0xFFE5252A)
private val CardBackground = Color(0xFF0E0E11)
private val SurfaceVariant = Color(0xFF1B1B1F)
private val BorderColor = Color(0xFF26262B)
private val DividerColor = Color(0xFF1E1E22)
private val TextSecondary = Color(0xFF8E8E93)

@Composable
fun BengaliLayoutToggleRow(
    title: String,
    subtitle: String,
    active: Boolean,
    canToggle: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
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
            checked = active,
            enabled = canToggle,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NothingRed,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = SurfaceVariant
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun HomeScreen(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isDeveloper = useDataStoreValue(IS_DEVELOPER)
    val isCheckingUpdate by UpdateChecker.isChecking.collectAsState()
    var showUpdateSheet by remember { mutableStateOf(false) }
    var availableUpdate by remember { mutableStateOf<AppUpdate?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Announcement manager & state
    val (actionBarShown, setActionBarShown) = useDataStore(ActionBarDisplayedSetting)
    val (isNumberRowActive, setNumberRowActive) = useSharedPrefsBool(Settings.PREF_ENABLE_NUMBER_ROW, false)

    val announcementManager = remember { AnnouncementManager.getInstance(context) }
    val availableAnnouncement by announcementManager.availableAnnouncement.collectAsState()
    val isCheckingAnnouncement by announcementManager.isChecking.collectAsState()
    var showAnnouncementSheet by remember { mutableStateOf(false) }

    val currentSubtypes = useDataStoreValue(SubtypesSetting)
    val akkhorSubtype = "bn_BD:KeyboardLayoutSet=bengali_akkhor:"
    val avroSubtype = "bn_BD:KeyboardLayoutSet=bengali_avro:"
    val jatiyoSubtype = "bn_BD:KeyboardLayoutSet=bengali_jatiyo:"
    val probhatSubtype = "bn_BD:KeyboardLayoutSet=bengali_probhat:"

    val isAkkhorActive = currentSubtypes.contains(akkhorSubtype)
    val isAvroActive = currentSubtypes.contains(avroSubtype)
    val isJatiyoActive = currentSubtypes.contains(jatiyoSubtype)
    val isProbhatActive = currentSubtypes.contains(probhatSubtype)

    fun toggleSubtype(subtypeStr: String, enabled: Boolean) {
        if (enabled && currentSubtypes.size >= 3) {
            Toast.makeText(context, "Maximum 3 keyboards can be selected at a time", Toast.LENGTH_SHORT).show()
            return
        }
        val newSubtypes = if (enabled) {
            currentSubtypes + subtypeStr
        } else {
            if (currentSubtypes.size > 1) currentSubtypes - subtypeStr else currentSubtypes
        }
        context.setSettingBlocking(SubtypesSetting.key, newSubtypes)
        if (!enabled && context.getSettingBlocking(ActiveSubtype) == subtypeStr) {
            context.setSettingBlocking(ActiveSubtype.key, newSubtypes.firstOrNull() ?: "")
        }
    }

    LaunchedEffect(Unit) {
        Subtypes.addDefaultSubtypesIfNecessary(context)

        // Check for updates in background
        val result = UpdateChecker.checkForUpdate()
        val update = result.getOrNull()
        if (update != null && update.isUpdateAvailable) {
            availableUpdate = update
            showUpdateSheet = true
        }

        // Check for announcements in background
        announcementManager.checkForAnnouncements(coroutineScope, forceShow = false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .weight(1.0f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(bottom = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header: ORBIT BOARD & Version Tag
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "ORBIT BOARD",
                        fontFamily = Doto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                    Text(
                        "Crafted for Nothing OS",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(NothingRed.copy(alpha = 0.15f))
                        .border(1.dp, NothingRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "v${BuildConfig.VERSION_NAME}",
                        fontFamily = Doto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = NothingRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. INPUT & LANGUAGES
            SectionHeader(title = "Input & Languages", modifier = Modifier.padding(horizontal = 16.dp))
            NeuCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = 4.dp
            ) {
                NavigationItem(
                    title = "Languages",
                    style = NavigationItemStyle.HomePrimary,
                    navigate = { navController.navigate("languages") },
                    icon = painterResource(id = R.drawable.globe)
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(DividerColor)
                )
                NavigationItem(
                    title = "Keyboard & Typing",
                    style = NavigationItemStyle.HomeSecondary,
                    navigate = { navController.navigate("typing") },
                    icon = painterResource(id = R.drawable.keyboard)
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(DividerColor)
                )
                NavigationItem(
                    title = "Text Prediction",
                    style = NavigationItemStyle.HomeTertiary,
                    navigate = { navController.navigate("predictiveText") },
                    icon = painterResource(id = R.drawable.text_prediction)
                )

            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. BENGALI KEYBOARD LAYOUTS
            SectionHeader(title = "Bengali Layouts (Max 3 Active)", modifier = Modifier.padding(horizontal = 16.dp))
            NeuCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = 4.dp
            ) {
                BengaliLayoutToggleRow(
                    title = "Bangla (বাংলা)",
                    subtitle = "Alphabetical layout (অ আ ক খ)",
                    active = isAkkhorActive,
                    canToggle = !isAkkhorActive || currentSubtypes.size > 1,
                    onToggle = { enabled -> toggleSubtype(akkhorSubtype, enabled) }
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(DividerColor)
                )
                BengaliLayoutToggleRow(
                    title = "Avro (অভ্র)",
                    subtitle = "Phonetic typing (ami -> আমি)",
                    active = isAvroActive,
                    canToggle = !isAvroActive || currentSubtypes.size > 1,
                    onToggle = { enabled -> toggleSubtype(avroSubtype, enabled) }
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(DividerColor)
                )
                BengaliLayoutToggleRow(
                    title = "Jatiyo (জাতীয়)",
                    subtitle = "National standard layout (ক খ গ)",
                    active = isJatiyoActive,
                    canToggle = !isJatiyoActive || currentSubtypes.size > 1,
                    onToggle = { enabled -> toggleSubtype(jatiyoSubtype, enabled) }
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(DividerColor)
                )
                BengaliLayoutToggleRow(
                    title = "Probhat (প্রভাত)",
                    subtitle = "Probhat keyboard layout",
                    active = isProbhatActive,
                    canToggle = !isProbhatActive || currentSubtypes.size > 1,
                    onToggle = { enabled -> toggleSubtype(probhatSubtype, enabled) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. KEYBOARD ROWS
            SectionHeader(title = "Keyboard Rows", modifier = Modifier.padding(horizontal = 16.dp))
            NeuCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = 4.dp
            ) {
                BengaliLayoutToggleRow(
                    title = "Suggestion Row",
                    subtitle = "Show suggestions and actions bar above keyboard",
                    active = actionBarShown,
                    canToggle = true,
                    onToggle = { enabled -> setActionBarShown(enabled) }
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(DividerColor)
                )
                BengaliLayoutToggleRow(
                    title = "Number Row",
                    subtitle = "Show dedicated row of numbers (0-9) on keyboard",
                    active = isNumberRowActive,
                    canToggle = true,
                    onToggle = { enabled -> setNumberRowActive(enabled) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. CUSTOMIZATION & ANNOUNCEMENTS
            SectionHeader(title = "Customization", modifier = Modifier.padding(horizontal = 16.dp))
            NeuCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = 4.dp
            ) {
                NavigationItem(
                    title = "Theme",
                    style = NavigationItemStyle.HomeTertiary,
                    navigate = { navController.navigate("themes") },
                    icon = painterResource(id = R.drawable.themes)
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(DividerColor)
                )
                NavigationItem(
                    title = "Announcements",
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
                if (isDeveloper || LocalInspectionMode.current) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(DividerColor)
                    )
                    NavigationItem(
                        title = "Developer Settings",
                        style = NavigationItemStyle.HomeTertiary,
                        navigate = { navController.navigate("developer") },
                        icon = painterResource(id = R.drawable.code)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. UPDATES
            SectionHeader(title = "Updates", modifier = Modifier.padding(horizontal = 16.dp))
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
                                if (isCheckingUpdate) "Checking releases..." else "Version ${BuildConfig.VERSION_NAME} • Tap to check",
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
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceVariant)
                                .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "Check",
                                fontFamily = Inter,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. ABOUT DEVELOPER
            SectionHeader(title = "About Developer", modifier = Modifier.padding(horizontal = 16.dp))
            DeveloperDetailsCard(modifier = Modifier.padding(horizontal = 16.dp))

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom "Try Typing" capsule
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(Color(0xFF141416))
                    .border(1.dp, BorderColor, RoundedCornerShape(25.dp))
                    .clickable {
                        val intent = Intent().apply {
                            setClass(context, TextEditPopupActivity::class.java)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        context.startActivity(intent)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.try_typing),
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White
                )
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
