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
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import android.view.HapticFeedbackConstants
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
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
    val subtypeString: String,
    val activeBackgroundColor: Color = Color(0xFF221316),
    val activeBorderColor: Color = Color(0xFFE5252A),
    val accentColor: Color = Color(0xFFE5252A)
)

@Composable
fun LayoutGridCard(
    item: LayoutCardItem,
    isActive: Boolean,
    canToggle: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val cardBackground = if (isActive) item.activeBackgroundColor else Color(0xFF161618)
    val cardBorder = if (isActive) item.activeBorderColor.copy(alpha = 0.70f) else item.accentColor.copy(alpha = 0.25f)

    val shapeColor = item.accentColor.copy(alpha = if (isActive) 0.08f else 0.04f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBackground)
            .drawBehind {
                drawCircle(
                    color = shapeColor,
                    radius = size.width * 0.45f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.95f, size.height * 0.95f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f * density)
                )
                drawCircle(
                    color = shapeColor.copy(alpha = shapeColor.alpha * 0.5f),
                    radius = size.width * 0.25f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.95f, size.height * 0.95f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.0f * density)
                )
            }
            .border(
                width = 1.dp,
                color = cardBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                try {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                } catch (_: Exception) {}
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

                // Layout status indicator dot with distinct identity accent
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isActive) item.accentColor else item.accentColor.copy(alpha = 0.25f))
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Layout identity badge chip displaying distinct accent styling
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isActive) item.accentColor.copy(alpha = 0.18f) else item.accentColor.copy(alpha = 0.08f))
                    .border(
                        width = 1.dp,
                        color = if (isActive) item.accentColor.copy(alpha = 0.45f) else item.accentColor.copy(alpha = 0.20f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = item.nativeScript,
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.5.sp,
                    color = if (isActive) item.accentColor else item.accentColor.copy(alpha = 0.85f)
                )
            }

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
    val arabicSubtype = "ar:KeyboardLayoutSet=arabic:"

    var showMaxLimitAlert by remember { mutableStateOf(false) }

    val layoutList = listOf(
        LayoutCardItem(
            id = "akkhor",
            title = "Bangla",
            nativeScript = "বাংলা",
            sampleText = "অ আ ক খ",
            description = "Alphabetical typing",
            subtypeString = akkhorSubtype,
            activeBackgroundColor = Color(0xFF221316),
            activeBorderColor = Color(0xFFE5252A),
            accentColor = Color(0xFFE5252A)
        ),
        LayoutCardItem(
            id = "avro",
            title = "Avro",
            nativeScript = "অভ্র",
            sampleText = "ami • আমি",
            description = "Phonetic typing",
            subtypeString = avroSubtype,
            activeBackgroundColor = Color(0xFF241B12),
            activeBorderColor = Color(0xFFE2B438),
            accentColor = Color(0xFFE2B438)
        ),
        LayoutCardItem(
            id = "jatiyo",
            title = "Jatiyo",
            nativeScript = "জাতীয়",
            sampleText = "অ আ ক খ",
            description = "National standard",
            subtypeString = jatiyoSubtype,
            activeBackgroundColor = Color(0xFF132220),
            activeBorderColor = Color(0xFF38B2AC),
            accentColor = Color(0xFF38B2AC)
        ),
        LayoutCardItem(
            id = "probhat",
            title = "Probhat",
            nativeScript = "প্রভাত",
            sampleText = "ami • আমি",
            description = "Probhat layout",
            subtypeString = probhatSubtype,
            activeBackgroundColor = Color(0xFF1B1626),
            activeBorderColor = Color(0xFFA855F7),
            accentColor = Color(0xFFA855F7)
        ),
        LayoutCardItem(
            id = "jatiyo_classic",
            title = "Jatiyo Classic",
            nativeScript = "জাতীয় ক্লাসিক",
            sampleText = "অ আ ক খ",
            description = "Original traditional national layout",
            subtypeString = jatiyoClassicSubtype,
            activeBackgroundColor = Color(0xFF132220),
            activeBorderColor = Color(0xFF38B2AC),
            accentColor = Color(0xFF38B2AC)
        ),
        LayoutCardItem(
            id = "arabic",
            title = "Arabic",
            nativeScript = "العربية",
            sampleText = "أ ب ت ث",
            description = "Standard Arabic layout",
            subtypeString = arabicSubtype,
            activeBackgroundColor = Color(0xFF132219),
            activeBorderColor = Color(0xFF10B981),
            accentColor = Color(0xFF10B981)
        )
    )

    val supportedSubtypeStrings = remember { layoutList.map { it.subtypeString }.toSet() }
    val activeCount = currentSubtypes.count { it in supportedSubtypeStrings }

    fun toggleSubtype(subtypeStr: String, enabled: Boolean) {
        if (enabled) {
            if (activeCount >= 3) {
                showMaxLimitAlert = true
                return
            }
            val newSubtypes = currentSubtypes + subtypeStr
            context.setSettingBlocking(SubtypesSetting.key, newSubtypes)
        } else {
            // Zero selection allowed: English-only typing works cleanly
            var newSubtypes = currentSubtypes.filter { it != subtypeStr && it != subtypeStr.replace("::", ":") }.toSet()
            if (newSubtypes.isEmpty()) {
                val englishSubtype = "en_US:KeyboardLayoutSet=qwerty:"
                newSubtypes = setOf(englishSubtype)
            }
            context.setSettingBlocking(SubtypesSetting.key, newSubtypes)
            if (context.getSettingBlocking(ActiveSubtype) == subtypeStr) {
                val fallback = newSubtypes.firstOrNull { it in supportedSubtypeStrings }
                    ?: newSubtypes.firstOrNull { it.startsWith("en") }
                    ?: newSubtypes.firstOrNull() ?: "en_US:KeyboardLayoutSet=qwerty:"
                context.setSettingBlocking(ActiveSubtype.key, fallback)
            }
        }
    }

    LaunchedEffect(Unit) {
        Subtypes.addDefaultSubtypesIfNecessary(context)
    }

    if (showMaxLimitAlert) {
        AlertDialog(
            onDismissRequest = { showMaxLimitAlert = false },
            title = {
                Text(
                    text = "LIMIT REACHED",
                    fontFamily = Doto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Maximum 3 active layouts allowed. Please deselect one to enable another.",
                    fontFamily = Inter,
                    fontSize = 13.5.sp,
                    color = Color(0xFFC8C8CC),
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showMaxLimitAlert = false }) {
                    Text("GOT IT", color = NothingRed, fontWeight = FontWeight.Bold, fontFamily = Inter, fontSize = 13.sp)
                }
            },
            containerColor = Color(0xFF18181C),
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .verticalScroll(scrollState)
            .navigationBarsPadding()
            .padding(bottom = 160.dp)
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
            horizontalArrangement = Arrangement.End
        ) {
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

        // 3. Layout Cards Grid (2x3 Grid)
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
                    canToggle = true,
                    onToggle = { enabled -> toggleSubtype(banglaItem.subtypeString, enabled) },
                    modifier = Modifier.weight(1f)
                )

                val avroItem = layoutList[1]
                val isAvroActive = currentSubtypes.contains(avroItem.subtypeString)
                LayoutGridCard(
                    item = avroItem,
                    isActive = isAvroActive,
                    canToggle = true,
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
                    canToggle = true,
                    onToggle = { enabled -> toggleSubtype(jatiyoItem.subtypeString, enabled) },
                    modifier = Modifier.weight(1f)
                )

                val probhatItem = layoutList[3]
                val isProbhatActive = currentSubtypes.contains(probhatItem.subtypeString)
                LayoutGridCard(
                    item = probhatItem,
                    isActive = isProbhatActive,
                    canToggle = true,
                    onToggle = { enabled -> toggleSubtype(probhatItem.subtypeString, enabled) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 3 (Jatiyo Classic & Arabic)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val classicItem = layoutList[4]
                val isClassicActive = currentSubtypes.contains(classicItem.subtypeString)
                LayoutGridCard(
                    item = classicItem,
                    isActive = isClassicActive,
                    canToggle = true,
                    onToggle = { enabled -> toggleSubtype(classicItem.subtypeString, enabled) },
                    modifier = Modifier.weight(1f)
                )

                val arabicItem = layoutList[5]
                val isArabicActive = currentSubtypes.contains(arabicItem.subtypeString)
                LayoutGridCard(
                    item = arabicItem,
                    isActive = isArabicActive,
                    canToggle = true,
                    onToggle = { enabled -> toggleSubtype(arabicItem.subtypeString, enabled) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Languages & Subtypes Navigation Capsule
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF141416))
                    .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                    .clickable { navController.navigate("languages") }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Language,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Languages & Subtypes",
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Manage all installed languages & input methods",
                            fontFamily = Inter,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Try Typing Action Capsule
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

        Spacer(modifier = Modifier.height(110.dp))
    }
}
