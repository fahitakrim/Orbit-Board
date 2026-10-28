package org.futo.inputmethod.latin.uix.settings.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.runBlocking
import org.futo.inputmethod.latin.BinaryDictionaryGetter
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.Subtypes
import org.futo.inputmethod.latin.SubtypesSetting
import org.futo.inputmethod.latin.common.Constants
import org.futo.inputmethod.latin.uix.FileKind
import org.futo.inputmethod.latin.uix.ResourceHelper
import org.futo.inputmethod.latin.uix.getSetting
import org.futo.inputmethod.latin.uix.icon
import org.futo.inputmethod.latin.uix.kindTitle
import org.futo.inputmethod.latin.uix.namePreferenceKeyFor
import org.futo.inputmethod.latin.uix.settings.NavigationItem
import org.futo.inputmethod.latin.uix.settings.NavigationItemStyle
import org.futo.inputmethod.latin.uix.settings.ScreenTitle
import org.futo.inputmethod.latin.uix.settings.Tip
import org.futo.inputmethod.latin.uix.settings.pages.modelmanager.openModelImporter
import org.futo.inputmethod.latin.uix.settings.useDataStoreValue
import org.futo.inputmethod.latin.uix.theme.Typography
import org.futo.inputmethod.latin.uix.theme.UixThemeWrapper
import org.futo.inputmethod.latin.uix.theme.presets.DynamicDarkTheme
import org.futo.inputmethod.latin.uix.urlEncode
import org.futo.inputmethod.latin.uix.youAreImporting
import org.futo.inputmethod.latin.utils.Dictionaries
import org.futo.inputmethod.latin.xlm.ModelPaths
import org.futo.inputmethod.updates.openURI
import java.util.Locale

data class LayoutToggleItem(
    val layoutKey: String,
    val name: String,
    val active: Boolean
)

data class LanguageItem (
    val languageName: String,
    val options: LanguageOptions,
    val layouts: List<LayoutToggleItem>
)

val TextHeadingMediumMlStyle = Typography.titleLarge.copy(
    fontSize = 20.sp,
    lineHeight = 30.sp,
    fontWeight = FontWeight(500)
)

val TextSmallStyle = Typography.titleSmall.copy(fontWeight = FontWeight.W400)
val TextBodyRegularMlStyle = Typography.titleMedium.copy(fontWeight = FontWeight.W400)

@Composable
fun LanguageConfigurable(
    kind: FileKind,
    selection: String,
    onSelected: () -> Unit
) {
    Row(modifier = Modifier
        .clickable(enabled = true) { onSelected() }
        .padding(start = 16.dp, top = 8.dp, end = 6.dp, bottom = 8.dp)
        .defaultMinSize(0.dp, 50.dp)) {
        Column(modifier = Modifier
            .weight(1.0f)
            .align(Alignment.CenterVertically)) {
            Row(modifier = Modifier.alpha(0.75f)) {
                Icon(
                    painterResource(kind.icon()),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    kind.kindTitle(),
                    modifier = Modifier.align(Alignment.CenterVertically),
                    style = TextSmallStyle
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                selection,
                style = TextBodyRegularMlStyle
            )

        }
        Icon(
            Icons.Default.ArrowForward,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(44.dp)
                .padding(10.dp)
        )
    }
}

@Composable
fun LayoutConfigurable(
    name: String,
    active: Boolean,
    onToggle: (Boolean) -> Unit,
    canToggle: Boolean
) {
    Row(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp)
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            name,
            style = TextBodyRegularMlStyle,
            modifier = Modifier.weight(1.0f)
        )

        Switch(
            checked = active,
            enabled = canToggle,
            onCheckedChange = onToggle
        )
    }
}

@Composable
fun LanguageSurface(
    item: LanguageItem,
    modifier: Modifier = Modifier,
    onConfigurableSelected: (FileKind) -> Unit,
    onLayoutToggled: (String, Boolean) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            modifier
                .padding(start = 32.dp, end = 32.dp)
                .widthIn(296.dp, 400.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(size = 16.dp)
                )
                .padding(top = 14.dp, bottom = 16.dp)
        ) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                Text(
                    item.languageName,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                    style = TextHeadingMediumMlStyle
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    "Keyboard Layouts",
                    modifier = Modifier
                        .alpha(0.75f)
                        .padding(start = 16.dp, end = 16.dp),
                    style = TextSmallStyle
                )

                Spacer(modifier = Modifier.height(8.dp))

                val totalActive = item.layouts.count { it.active }
                item.layouts.forEach { layoutItem ->
                    LayoutConfigurable(
                        name = layoutItem.name,
                        active = layoutItem.active,
                        onToggle = { newState -> onLayoutToggled(layoutItem.layoutKey, newState) },
                        canToggle = !layoutItem.active || totalActive > 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Preview
@Composable
fun LanguageSurfacePreview() {
    UixThemeWrapper(colorScheme = DynamicDarkTheme.obtainColors(LocalContext.current)) {
        LanguageSurface(item = LanguageItem(
            languageName = "Language Name",
            options = LanguageOptions(
                "Model Name",
                "Model Name",
                "Model Name"
            ),
            layouts = listOf(
                LayoutToggleItem("default", "Keyboard Name", true)
            )
        ), onLayoutToggled = { _, _ -> }, onConfigurableSelected = { })
    }
}

data class LanguageOptions(
    val voiceInputModel: String?,
    val dictionary: String?,
    val transformerModel: String?
)

@Composable
fun ConfirmResourceActionDialog(
    onDismissRequest: () -> Unit,

    onExplore: () -> Unit,
    onDelete: () -> Unit,
    onImport: () -> Unit,

    resourceKind: FileKind,
    isCurrentlySet: Boolean,
    locale: Locale
) {
    val hasBuiltInFallback = if(resourceKind == FileKind.VoiceInput) {
        ResourceHelper.BuiltInVoiceInputFallbacks[locale.language] != null
    } else if(resourceKind == FileKind.Dictionary) {
        Dictionaries.getDictionaryId(locale) != 0
    } else {
        true
    }

    AlertDialog(
        icon = {
            Icon(painterResource(id = resourceKind.icon()), contentDescription = "Action")
        },
        title = {
            Text(text = "${locale.displayLanguage} - ${resourceKind.kindTitle()}")
        },
        text = {
            if(isCurrentlySet && hasBuiltInFallback) {
                Text(text = "Would you like to revert ${resourceKind.youAreImporting()} to default for ${locale.displayLanguage}, or replace it with another file?")
            } else if(isCurrentlySet) {
                Text(text = "Would you like to remove the ${resourceKind.youAreImporting()} for ${locale.displayLanguage}, or replace it with another file?\n\nNo built-in ${resourceKind.youAreImporting()} exists for this language, so if you remove it, it will stop working until you import a different one!")
            } else {
                Text(text = "No ${resourceKind.youAreImporting()} override is set for ${locale.displayLanguage}. You can explore downloads online, or import an existing file.")
            }
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(onClick = {
                onImport()
            }) {
                Text(
                    if(isCurrentlySet) {
                        "Replace"
                    } else {
                        "Import"
                    }
                )
            }
        },
        dismissButton = {
            if(isCurrentlySet) {
                TextButton(onClick = { onDelete() }) {
                    if(hasBuiltInFallback) {
                        Text("Revert to Default")
                    } else {
                        Text("Remove")
                    }

                }
            } else {
                TextButton(onClick = { onExplore() }) {
                    Text("Explore")
                }
            }
        }
    )
}


@Composable
fun ConfirmDeleteLanguageDialog(
    onDismissRequest: () -> Unit,
    onDelete: () -> Unit,
    locale: Locale
) {
    AlertDialog(
        icon = {
            Icon(painterResource(id = R.drawable.trash), contentDescription = null)
        },
        title = {
            Text(text = "Delete ${locale.displayLanguage}?")
        },
        text = {
            Text(text = "Remove ${locale.displayLanguage}, all of its associated layouts and custom resources?")
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text("Remove")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}


data class DeleteInfo(
    val locale: Locale,
    val kind: FileKind
)

@Preview(showBackground = true)
@Composable
fun LanguagesScreen(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val deleteDialogInfo: MutableState<DeleteInfo?> = remember { mutableStateOf(null) }
    val languageDeleteInfo: MutableState<Locale?> = remember { mutableStateOf(null) }

    val inputMethods = useDataStoreValue(SubtypesSetting)
    val inputMethodList = remember(inputMethods) {
        Subtypes.layoutsMappedByLanguage(inputMethods)
    }

    val inputMethodKeys = remember(inputMethodList) { inputMethodList.keys.toList().sorted() }

    if(deleteDialogInfo.value != null) {
        val info = deleteDialogInfo.value!!
        ConfirmResourceActionDialog(
            onDismissRequest = { deleteDialogInfo.value = null },
            onExplore = {
                context.openURI(info.kind.getAddonUrlForLocale(info.locale), true)
                deleteDialogInfo.value = null
            },
            onDelete = {
                ResourceHelper.deleteResourceForLanguage(context, info.kind, info.locale)
                deleteDialogInfo.value = null
            },
            onImport = {
                openModelImporter(context)
                deleteDialogInfo.value = null
            },

            resourceKind = info.kind,
            locale = info.locale,
            isCurrentlySet = runBlocking { ResourceHelper.findFileForKind(context, info.locale, info.kind)?.exists() == true }
        )
    }else if(languageDeleteInfo.value != null) {
        val info = languageDeleteInfo.value!!
        ConfirmDeleteLanguageDialog(
            locale = info,
            onDelete = {
                languageDeleteInfo.value = null

                val key = inputMethodKeys.find { localeString ->
                    Subtypes.getLocale(localeString) == info
                }

                if(key != null) {
                    val subtypes = inputMethodList[key]!!
                    subtypes.forEach { Subtypes.removeLanguage(context, it) }
                }
            },
            onDismissRequest = {
                languageDeleteInfo.value = null
            }
        )
    }
    LaunchedEffect(Unit) {
        Subtypes.addDefaultSubtypesIfNecessary(context)
    }

    LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
        item {
            ScreenTitle("Languages", showBack = true, navController)
        }

        item {
            NavigationItem(
                title = "Add language",
                style = NavigationItemStyle.Misc,
                navigate = {
                    navController.navigate("addLanguage")
                },
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(inputMethodKeys) { localeString ->
            val subtypes = inputMethodList[localeString]!!

            val locale = Subtypes.getLocale(localeString)

            val name = Subtypes.getName(subtypes.first())

            val voiceInputModelName = ResourceHelper.tryFindingVoiceInputModelForLocale(context, locale)?.name?.let { stringResource(it) }
            val dictionaryName = runBlocking { ResourceHelper.findKeyForLocaleAndKind(context, locale, FileKind.Dictionary) }?.let {
                runBlocking { context.getSetting(FileKind.Dictionary.namePreferenceKeyFor(it), "Dictionary") } + " (Imported)"
            } ?: if(BinaryDictionaryGetter.getDictionaryFiles(locale, context, false, false).isNotEmpty()) {
                    "Built-in Dictionary"
            } else {
                    null
            }

            val transformerName = runBlocking { ModelPaths.getModelOptions(context) }.get(locale.language)?.let {
                it.loadDetails()?.name
            }

            val options = LanguageOptions(
                voiceInputModel = voiceInputModelName,
                dictionary = dictionaryName,
                transformerModel = transformerName
            )


            val activeLayoutKeys = subtypes.map {
                it.getExtraValueOf(Constants.Subtype.ExtraValue.KEYBOARD_LAYOUT_SET) ?: "default"
            }
            val possibleLayoutKeys = (activeLayoutKeys + Subtypes.findClosestLocaleLayouts(locale)).distinct()
            val layoutItems = possibleLayoutKeys.map { lk ->
                LayoutToggleItem(
                    layoutKey = lk,
                    name = Subtypes.getLayoutName(context, lk),
                    active = activeLayoutKeys.contains(lk)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LanguageSurface(
                LanguageItem(
                    languageName = name,
                    options = options,
                    layouts = layoutItems
                ),
                onConfigurableSelected = { kind ->
                    deleteDialogInfo.value = DeleteInfo(locale, kind)
                },
                onLayoutToggled = { layoutKey, enabled ->
                    if (enabled) {
                        if (inputMethods.size >= Subtypes.MAX_ACTIVE_KEYBOARDS) {
                            android.widget.Toast.makeText(context, "Maximum 3 keyboards can be selected at a time", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            Subtypes.addLanguage(context, locale, layoutKey)
                        }
                    } else {
                        val subtypeToRemove = subtypes.find {
                            (it.getExtraValueOf(Constants.Subtype.ExtraValue.KEYBOARD_LAYOUT_SET) ?: "default") == layoutKey
                        }
                        if (subtypeToRemove != null) {
                            Subtypes.removeLanguage(context, subtypeToRemove)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}