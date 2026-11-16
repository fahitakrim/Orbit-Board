package org.futo.inputmethod.latin.uix.settings.pages

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.core.content.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.settings.LongPressKey
import org.futo.inputmethod.latin.settings.LongPressKeyLayoutSetting
import org.futo.inputmethod.latin.settings.Settings
import org.futo.inputmethod.latin.settings.description
import org.futo.inputmethod.latin.settings.name
import org.futo.inputmethod.latin.settings.toEncodedString
import org.futo.inputmethod.latin.settings.toLongPressKeyLayoutItems
import org.futo.inputmethod.latin.uix.SettingsKey
import org.futo.inputmethod.latin.uix.actions.ActionEditor
import org.futo.inputmethod.latin.uix.actions.ActionsEditor
import org.futo.inputmethod.latin.uix.actions.ClipboardHistoryEnabled
import org.futo.inputmethod.latin.uix.getSettingBlocking
import org.futo.inputmethod.latin.uix.settings.DataStoreItem
import org.futo.inputmethod.latin.uix.settings.NavigationItem
import org.futo.inputmethod.latin.uix.settings.NavigationItemStyle
import org.futo.inputmethod.latin.uix.settings.ScreenTitle
import org.futo.inputmethod.latin.uix.settings.ScrollableList
import org.futo.inputmethod.latin.uix.settings.SettingItem
import org.futo.inputmethod.latin.uix.settings.SettingListLazy
import org.futo.inputmethod.latin.uix.settings.SettingRadio
import org.futo.inputmethod.latin.uix.settings.SettingSlider
import org.futo.inputmethod.latin.uix.settings.SettingSliderSharedPrefsInt
import org.futo.inputmethod.latin.uix.settings.SettingToggleSharedPrefs
import org.futo.inputmethod.latin.uix.settings.useDataStore
import org.futo.inputmethod.latin.uix.settings.useSharedPrefsInt
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.sign


val LongPressShortcutsSetting = SettingsKey(
    booleanPreferencesKey("long_press_cxv_shortcuts"),
    true
)

val ActionBarDisplayedSetting = SettingsKey(
    booleanPreferencesKey("enable_action_bar"),
    true
)

fun NavGraphBuilder.addTypingNavigation(
    navController: NavHostController
) {
    composable("typing") { TypingScreen(navController) }
    composable("resize") { ResizeScreen(navController) }
    composable("longPress") { LongPressScreen(navController) }
    composable("actionEdit") { ActionEditorScreen(navController) }
}

@Preview(showBackground = true)
@Composable
fun ActionEditorScreen(navController: NavHostController = rememberNavController()) {
    Column {
        ScreenTitle("Edit Actions", showBack = true, navController)
        ActionsEditor { }
    }
}

@Preview(showBackground = true)
@Composable
fun ResizeScreen(navController: NavHostController = rememberNavController()) {
    ResizeKeyboardScreen(navController)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.DraggableSettingItem(idx: Int, item: LongPressKey, moveItem: (LongPressKey, Int) -> Unit, disable: (LongPressKey) -> Unit, dragIcon: @Composable () -> Unit) {
    val context = LocalContext.current
    val dragging = remember { mutableStateOf(false) }
    val offset = remember { mutableFloatStateOf(0.0f) }
    val height = remember { mutableIntStateOf(1) }

    SettingItem(
        title = "${idx+1}. " + item.name(context),
        subtitle = item.description(context),
        icon = dragIcon,
        modifier = Modifier
            .onSizeChanged { size -> height.intValue = size.height }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        dragging.value = true
                        offset.floatValue = 0.0f
                    },
                    onDragEnd = {
                        dragging.value = false
                        offset.floatValue = 0.0f
                    },
                    onDragCancel = {
                        dragging.value = false
                        offset.floatValue = 0.0f
                    },
                    onDrag = { change, dragAmount ->
                        offset.floatValue += dragAmount.y
                        if (offset.floatValue.absoluteValue > height.intValue) {
                            val direction = offset.floatValue.sign.toInt()
                            moveItem(
                                item,
                                direction
                            )
                            offset.floatValue -= height.intValue * direction
                        }

                    }
                )
            }
            .let { modifier ->
                if (!dragging.value) {
                    modifier.animateItemPlacement()
                } else {
                    modifier
                        .zIndex(10.0f)
                        .graphicsLayer {
                            clip = false
                            translationX = 0.0f
                            translationY = offset.floatValue
                        }
                }
            }
    ) {
        IconButton(onClick = { disable(item) }) {
            Icon(Icons.Default.Clear, contentDescription = "Remove")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.longPressKeyLayoutEditor(context: Context, setting: DataStoreItem<String>) {
    item {
        ScreenTitle(title = "Layout of long-press keys")
    }

    item {
        Button(onClick = {
            setting.setValue(LongPressKeyLayoutSetting.default)
        }) {
            Text("Reset to default")
        }
    }


    val dragIcon: @Composable () -> Unit = {
        Icon(Icons.Default.Menu, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f))
    }


    val items = setting.value.toLongPressKeyLayoutItems()


    val moveItem: (item: LongPressKey, direction: Int) -> Unit = { item, direction ->
        val oldItems = context.getSettingBlocking(LongPressKeyLayoutSetting).toLongPressKeyLayoutItems()
        val oldIdx = oldItems.indexOf(item)

        val insertIdx = (oldIdx + direction).coerceAtLeast(0).coerceAtMost(oldItems.size - 1)

        val newItems = oldItems.filter { it != item }.toMutableList().apply {
            add(insertIdx, item)
        }.toEncodedString()

        setting.setValue(newItems)
    }

    val disable: (item : LongPressKey) -> Unit = { item ->
        val oldItems = context.getSettingBlocking(LongPressKeyLayoutSetting).toLongPressKeyLayoutItems()

        val newItems = oldItems.filter { it != item }.toEncodedString()

        setting.setValue(newItems)

    }

    val enable: (item : LongPressKey) -> Unit = { item ->
        val oldItems = context.getSettingBlocking(LongPressKeyLayoutSetting).toLongPressKeyLayoutItems()

        val newItems = oldItems.filter { it != item }.toMutableList().apply {
            add(item)
        }.toEncodedString()

        setting.setValue(newItems)
    }

    item {
        Text("Active", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
    itemsIndexed(items, key = { i, v -> v.ordinal }) { i, v ->
        DraggableSettingItem(idx = i, item = v, moveItem = moveItem, disable = disable, dragIcon = dragIcon)
    }

    item {
        Text("Inactive", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
    items(LongPressKey.entries.filter { !items.contains(it) }, key = { it.ordinal }) {
        SettingItem(
            title = it.name(context),
            subtitle = it.description(context),
            modifier = Modifier.animateItemPlacement()
        ) {
            IconButton(onClick = { enable(it) }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LongPressScreen(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val setting = useDataStore(LongPressKeyLayoutSetting, blocking = true)
    SettingListLazy {
        item {
            ScreenTitle("Long-Press Keys", showBack = true, navController)
        }

        longPressKeyLayoutEditor(
            context = context,
            setting = setting,
        )

        item {
            SettingSliderSharedPrefsInt(
                title = "Long Press Duration",
                subtitle = "How long a key needs to be pressed to be considered a long-press",
                key = Settings.PREF_KEY_LONGPRESS_TIMEOUT,
                default = 300,
                range = 100.0f..700.0f,
                hardRange = 25.0f..1200.0f,
                transform = { it.roundToInt() },
                indicator = { "$it ms" },
                steps = 23
            )
        }

        item {
            SettingRadio(
                title = "Backspace Behavior when holding/swiping",
                options = listOf(
                    Settings.BACKSPACE_MODE_CHARACTERS,
                    Settings.BACKSPACE_MODE_WORDS
                ),
                optionNames = listOf(
                    "Delete characters",
                    "Delete entire words"
                ),
                setting = useSharedPrefsInt(
                    key = Settings.PREF_BACKSPACE_MODE,
                    default = Settings.BACKSPACE_MODE_CHARACTERS
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TypingScreen(navController: NavHostController = rememberNavController()) {
    MoreSettingsScreen(navController = navController, showBack = true)
}

