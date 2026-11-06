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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.futo.inputmethod.latin.ActiveSubtype
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.Subtypes
import org.futo.inputmethod.latin.SubtypesSetting
import org.futo.inputmethod.latin.settings.Settings
import org.futo.inputmethod.latin.uix.TextEditPopupActivity
import org.futo.inputmethod.latin.uix.getSettingBlocking
import org.futo.inputmethod.latin.uix.setSettingBlocking
import org.futo.inputmethod.latin.uix.settings.NeuCard
import org.futo.inputmethod.latin.uix.settings.OrbitBoardHeader
import org.futo.inputmethod.latin.uix.settings.SectionHeader
import org.futo.inputmethod.latin.uix.settings.useDataStore
import org.futo.inputmethod.latin.uix.settings.useDataStoreValue
import org.futo.inputmethod.latin.uix.settings.useSharedPrefsBool
import org.futo.inputmethod.latin.uix.theme.Doto
import org.futo.inputmethod.latin.uix.theme.Inter
import org.futo.inputmethod.latin.uix.theme.NothingRed

private val BackgroundColor = Color(0xFF0B0B0C)
private val CardBackground = Color(0xFF141416)
private val SurfaceVariant = Color(0xFF1B1B1F)
private val BorderColor = Color(0xFF232326)
private val DividerColor = Color(0xFF1E1E22)
private val TextSecondary = Color(0xFF8E8E93)

data class LayoutCardItem(
    val id: String,
    val title: String,
    val nativeScript: String,
    val sampleText: String,
    val description: String,
    val subtypeString: String
)

@Composable
fun LayoutGridCard(
    item: LayoutCardItem,
    isActive: Boolean,
    canToggle: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isActive) Color(0xFF1E1416) else CardBackground)
            .border(
                width = 1.dp,
                color = if (isActive) NothingRed.copy(alpha = 0.5f) else BorderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                onToggle(!isActive)
            }
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.title,
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.White
                )

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(NothingRed)
                    )
                } else {
                    Spacer(modifier = Modifier.size(7.dp))
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.nativeScript,
                fontFamily = Inter,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = if (isActive) NothingRed else TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.sampleText,
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.description,
                fontFamily = Inter,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                color = TextSecondary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MySpaceScreen(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val currentSubtypes = useDataStoreValue(SubtypesSetting)
    val akkhorSubtype = "bn_BD:KeyboardLayoutSet=bengali_akkhor:"
    val avroSubtype = "bn_BD:KeyboardLayoutSet=bengali_avro:"
    val jatiyoSubtype = "bn_BD:KeyboardLayoutSet=bengali_jatiyo:"
    val jatiyoClassicSubtype = "bn_BD:KeyboardLayoutSet=bengali_jatiyo_classic:"
    val probhatSubtype = "bn_BD:KeyboardLayoutSet=bengali_probhat:"

    // Typing & Behavior toggles
    val (autoCorrection, setAutoCorrection) = useSharedPrefsBool(Settings.PREF_AUTO_CORRECTION, false)
    val (actionBarShown, setActionBarShown) = useDataStore(ActionBarDisplayedSetting)
    val (isNumberRowActive, setNumberRowActive) = useSharedPrefsBool(Settings.PREF_ENABLE_NUMBER_ROW, false)
    val (isPinSpacebarActive, setPinSpacebarActive) = useSharedPrefsBool(Settings.PREF_PIN_KEYBOARD_NAME_ON_SPACEBAR, true)
    val (isShowEmojiKeyActive, setShowEmojiKeyActive) = useSharedPrefsBool(Settings.PREF_SHOW_EMOJI_KEY, true)

    val layoutList = listOf(
        LayoutCardItem(
            id = "akkhor",
            title = "Bangla",
            nativeScript = "বাংলা",
            sampleText = "অ আ ক খ",
            description = "Alphabetical typing",
            subtypeString = akkhorSubtype
        ),
        LayoutCardItem(
            id = "avro",
            title = "Avro",
            nativeScript = "অভ্র",
            sampleText = "ami • আমি",
            description = "Phonetic typing",
            subtypeString = avroSubtype
        ),
        LayoutCardItem(
            id = "jatiyo",
            title = "Jatiyo",
            nativeScript = "জাতীয়",
            sampleText = "অ আ ক খ",
            description = "National standard",
            subtypeString = jatiyoSubtype
        ),
        LayoutCardItem(
            id = "probhat",
            title = "Probhat",
            nativeScript = "প্রভাত",
            sampleText = "ami • আমি",
            description = "Probhat layout",
            subtypeString = probhatSubtype
        ),
        LayoutCardItem(
            id = "jatiyo_classic",
            title = "Jatiyo Classic",
            nativeScript = "জাতীয় ক্লাসিক",
            sampleText = "অ আ ক খ",
            description = "Original traditional national layout",
            subtypeString = jatiyoClassicSubtype
        )
    )

    val bengaliSubtypeStrings = remember { layoutList.map { it.subtypeString }.toSet() }
    val activeCount = currentSubtypes.count { it in bengaliSubtypeStrings }

    fun toggleSubtype(subtypeStr: String, enabled: Boolean) {
        if (enabled) {
            if (activeCount >= 3) {
                Toast.makeText(context, "Maximum 3 Bengali keyboards can be selected", Toast.LENGTH_SHORT).show()
                return
            }
            val newSubtypes = currentSubtypes + subtypeStr
            context.setSettingBlocking(SubtypesSetting.key, newSubtypes)
        } else {
            if (activeCount <= 1) {
                Toast.makeText(context, "At least 1 Bengali keyboard must remain active", Toast.LENGTH_SHORT).show()
                return
            }
            val newSubtypes = currentSubtypes - subtypeStr
            context.setSettingBlocking(SubtypesSetting.key, newSubtypes)
            if (context.getSettingBlocking(ActiveSubtype) == subtypeStr) {
                val fallback = newSubtypes.firstOrNull { it in bengaliSubtypeStrings }
                    ?: newSubtypes.firstOrNull() ?: ""
                context.setSettingBlocking(ActiveSubtype.key, fallback)
            }
        }
    }

    LaunchedEffect(Unit) {
        Subtypes.addDefaultSubtypesIfNecessary(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .verticalScroll(scrollState)
            .navigationBarsPadding()
            .padding(bottom = 84.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 1. Unified Orbit Board Header: Title + Subtitle + v1.0.8 | Moon
        OrbitBoardHeader()

        Spacer(modifier = Modifier.height(4.dp))

        // 2. Section Header: Active Layouts
        SectionHeader(
            title = "Active Layouts",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Layout Counter & Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Pick up to 3 to cycle between",
                fontFamily = Inter,
                fontSize = 12.sp,
                color = TextSecondary
            )

            // Layout Counter Pill: [ — — —  X/3 ]
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariant)
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 1..3) {
                        Box(
                            modifier = Modifier
                                .width(10.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(if (i <= activeCount) NothingRed else Color(0xFF3A3A40))
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$activeCount/3",
                        fontFamily = Doto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Layout Cards Grid (2x2 + 5th item)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1 (Bangla & Avro)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val banglaItem = layoutList[0]
                val isBanglaActive = currentSubtypes.contains(banglaItem.subtypeString)
                LayoutGridCard(
                    item = banglaItem,
                    isActive = isBanglaActive,
                    canToggle = if (isBanglaActive) activeCount > 1 else activeCount < 3,
                    onToggle = { enabled -> toggleSubtype(banglaItem.subtypeString, enabled) },
                    modifier = Modifier.weight(1f)
                )

                val avroItem = layoutList[1]
                val isAvroActive = currentSubtypes.contains(avroItem.subtypeString)
                LayoutGridCard(
                    item = avroItem,
                    isActive = isAvroActive,
                    canToggle = if (isAvroActive) activeCount > 1 else activeCount < 3,
                    onToggle = { enabled -> toggleSubtype(avroItem.subtypeString, enabled) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2 (Jatiyo & Probhat)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val jatiyoItem = layoutList[2]
                val isJatiyoActive = currentSubtypes.contains(jatiyoItem.subtypeString)
                LayoutGridCard(
                    item = jatiyoItem,
                    isActive = isJatiyoActive,
                    canToggle = if (isJatiyoActive) activeCount > 1 else activeCount < 3,
                    onToggle = { enabled -> toggleSubtype(jatiyoItem.subtypeString, enabled) },
                    modifier = Modifier.weight(1f)
                )

                val probhatItem = layoutList[3]
                val isProbhatActive = currentSubtypes.contains(probhatItem.subtypeString)
                LayoutGridCard(
                    item = probhatItem,
                    isActive = isProbhatActive,
                    canToggle = if (isProbhatActive) activeCount > 1 else activeCount < 3,
                    onToggle = { enabled -> toggleSubtype(probhatItem.subtypeString, enabled) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 3 (Jatiyo Classic Full Width)
            val classicItem = layoutList[4]
            val isClassicActive = currentSubtypes.contains(classicItem.subtypeString)
            LayoutGridCard(
                item = classicItem,
                isActive = isClassicActive,
                canToggle = if (isClassicActive) activeCount > 1 else activeCount < 3,
                onToggle = { enabled -> toggleSubtype(classicItem.subtypeString, enabled) }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. Section Header: Typing Behavior
        SectionHeader(
            title = "Typing Behavior",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        NeuCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = 4.dp
        ) {
            ToggleSettingRow(
                title = "Auto-correction",
                subtitle = "Spacebar commits suggested corrections (off for raw phonetic)",
                checked = autoCorrection,
                onCheckedChange = { setAutoCorrection(it) }
            )
            Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))

            ToggleSettingRow(
                title = "Suggestion row",
                subtitle = "Suggests and quickly acts above the keys",
                checked = actionBarShown,
                onCheckedChange = { setActionBarShown(it) }
            )
            Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))

            ToggleSettingRow(
                title = "Number row",
                subtitle = "Dedicated digits 0–9 above the letters",
                checked = isNumberRowActive,
                onCheckedChange = { setNumberRowActive(it) }
            )
            Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))

            ToggleSettingRow(
                title = "Layout name on spacebar",
                subtitle = if (isPinSpacebarActive) "Always show active layout name" else "Show briefly when switching",
                checked = isPinSpacebarActive,
                onCheckedChange = { setPinSpacebarActive(it) }
            )
            Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))

            ToggleSettingRow(
                title = "Show emoji key",
                subtitle = "Show dedicated emoji key in the bottom row",
                checked = isShowEmojiKeyActive,
                onCheckedChange = { setShowEmojiKeyActive(it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Quick Tip Box
        NeuCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = 14.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NothingRed)
                )
                Text(
                    text = "Swipe horizontally on spacebar or tap the globe key to quickly cycle through your active keyboards.",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 6. Try Typing Action Capsule
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(Color(0xFF141416))
                    .border(1.dp, BorderColor, RoundedCornerShape(25.dp))
                    .clickable {
                        try {
                            val intent = Intent().apply {
                                setClass(context, TextEditPopupActivity::class.java)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.util.Log.e("MySpaceScreen", "Failed to start TextEditPopupActivity", e)
                        }
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

        Spacer(modifier = Modifier.height(24.dp))
    }
}
