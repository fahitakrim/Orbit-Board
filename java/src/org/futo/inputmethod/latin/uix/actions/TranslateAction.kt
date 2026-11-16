package org.futo.inputmethod.latin.uix.actions

import android.util.Log
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.futo.inputmethod.keyboard.internal.CloudTranslationHandler
import org.futo.inputmethod.keyboard.internal.TranslationHelper
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.Suggest.TAG
import org.futo.inputmethod.latin.settings.Settings
import org.futo.inputmethod.latin.uix.Action
import org.futo.inputmethod.latin.uix.ActionEditText
import org.futo.inputmethod.latin.uix.ActionWindow
import org.futo.inputmethod.latin.uix.LocalManager
import org.futo.inputmethod.latin.uix.settings.useSharedPrefsString
import org.futo.inputmethod.latin.uix.settings.useUpdateSharedPrefString


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties

@Composable
fun LanguageDropdown(
    languageList: Map<String, String>,
    mExpanded: MutableState<Boolean>,
    mSelectedText: MutableState<String>,
    sourceLang: Boolean,
    prefKey: String,
    onLanguageSelected: () -> Unit = {},
    openLanguagePicker: ((isSource: Boolean, currentCode: String, onSelect: (String) -> Unit) -> Unit)? = null
) {
    val updateSharedPref = useUpdateSharedPrefString()

    Row(modifier = Modifier.fillMaxWidth()) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceBright,
            shape = RoundedCornerShape(24.dp),
            onClick = {
                mExpanded.value = true
            },
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .padding(2.dp)
                .weight(1.0f)
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(TranslationHelper.getLanguageName(mSelectedText.value), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        DropdownMenu(
            modifier = Modifier
                .widthIn(min = 200.dp, max = 260.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    RoundedCornerShape(16.dp)
                ),
            expanded = mExpanded.value,
            onDismissRequest = {
                mExpanded.value = false
            },
            properties = PopupProperties(focusable = false),
        ) {
            // Pinned Top Languages for BD users:
            // 1. Auto Select (only for source)
            // 2. Bangla
            // 3. English
            val pinnedCodes = if (sourceLang) {
                listOf("auto", "bn", "en")
            } else {
                listOf("bn", "en")
            }

            pinnedCodes.forEach { code ->
                val label = when (code) {
                    "auto" -> "Auto Select (স্বয়ংক্রিয়)"
                    "bn" -> "বাংলা (Bengali)"
                    "en" -> "English"
                    else -> languageList[code] ?: code
                }
                val isSelected = mSelectedText.value == code
                DropdownMenuItem(
                    text = {
                        Text(
                            text = label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    },
                    onClick = {
                        mExpanded.value = false
                        mSelectedText.value = code
                        updateSharedPref(prefKey, code)
                        onLanguageSelected()
                    },
                    trailingIcon = if (code == "auto") {
                        {
                            IconWithColor(
                                iconId = R.drawable.auto_detect,
                                iconColor = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else null
                )
            }

            DropdownMenuItem(
                text = {
                    Text(
                        text = "Others... (অন্যান্য)",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                onClick = {
                    mExpanded.value = false
                    openLanguagePicker?.invoke(sourceLang, mSelectedText.value) { code ->
                        mSelectedText.value = code
                        updateSharedPref(prefKey, code)
                        onLanguageSelected()
                    }
                }
            )
        }
    }
}

@Composable
fun LanguageSelector(
    modifier: Modifier,
    isLoading: Boolean,
    mSelectedLanguage1: MutableState<String>,
    mSelectedLanguage2: MutableState<String>,
    onLanguageSelected: () -> Unit = {},
    openLanguagePicker: ((isSource: Boolean, currentCode: String, onSelect: (String) -> Unit) -> Unit)? = null
) {
    val mExpanded1 = remember { mutableStateOf(false) }
    val mExpanded2 = remember { mutableStateOf(false) }
    val mLanguages = TranslationHelper.getLanguages()

    Row(modifier = modifier) {
        Column (modifier = Modifier.weight(5f)) {
            LanguageDropdown(mLanguages, mExpanded1, mSelectedLanguage1, true, Settings.PREF_KEY_LAST_SOURCE_LANG, onLanguageSelected, openLanguagePicker)
        }
        Column (modifier = Modifier
            .weight(1f)
            .align(Alignment.CenterVertically)) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                ) {
                    val icon = painterResource(id = R.drawable.nth_translate)
                    val color = MaterialTheme.colorScheme.onBackground
                    IconButton(onClick = {
                        if (mSelectedLanguage1.value == "auto") {
                            return@IconButton
                        }
                        val temp = mSelectedLanguage2.value
                        mSelectedLanguage2.value = mSelectedLanguage1.value + ""
                        mSelectedLanguage1.value = temp + ""
                        onLanguageSelected()
                    }) {
                        Canvas(modifier = Modifier.size(24.dp)) {
                            translate(
                                left = this.size.width / 2.0f - icon.intrinsicSize.width / 2.0f,
                                top = this.size.height / 2.0f - icon.intrinsicSize.height / 2.0f
                            ) {
                                with(icon) {
                                    draw(
                                        icon.intrinsicSize,
                                        colorFilter = ColorFilter.tint(
                                            color
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Column (modifier = Modifier.weight(5f)) {
            LanguageDropdown(mLanguages, mExpanded2, mSelectedLanguage2, false, Settings.PREF_KEY_LAST_TARGET_LANG, onLanguageSelected, openLanguagePicker)
        }
    }
}


@Composable
fun TranslationInputBox(
    text: MutableState<String>,
    placeHolder: String = "",
    fontSize: Float = 15f,
    editTextRef: MutableState<ActionEditText?>,
    callbackTextChange: (String) -> Int
) {
    val context = LocalContext.current
    val manager = LocalManager.current

    val height = with(LocalDensity.current) {
        48.dp.toPx()
    }

    val inputType = EditorInfo.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS

    val color = LocalContentColor.current
    val hintColor = MaterialTheme.colorScheme.onBackground

    AndroidView(
        factory = {
            ActionEditText(context).apply {
                editTextRef.value = this
                this.inputType = inputType
                isCursorVisible = true
                isFocusable = true
                isFocusableInTouchMode = true
                privateImeOptions = "org.futo.inputmethod.latin.NoSuggestions=1"

                setTextChangeCallback {
                    text.value = it
                    callbackTextChange(it)
                }
                hint = placeHolder
                setHintTextColor(hintColor.toArgb())

                setText(text.value)
                textSize = fontSize

                setTextColor(color.toArgb())

                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                setHeight(height.toInt())

                val editorInfo = EditorInfo().apply {
                    this.inputType = inputType
                    this.privateImeOptions = "org.futo.inputmethod.latin.NoSuggestions=1"
                }
                onCreateInputConnection(editorInfo)

                inputConnection?.let { conn ->
                    manager.overrideInputConnection(conn, editorInfo)
                }

                requestFocus()
            }
        },
        update = { edit ->
            editTextRef.value = edit
        },
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        onRelease = {
            editTextRef.value = null
        }
    )
}


@Composable
fun SourceTextInput(
    modifier: Modifier,
    searchText: MutableState<String>,
    editTextRef: MutableState<ActionEditText?>,
    onTextChange: (String) -> Int
) {
    Row(modifier = modifier) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceBright,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .padding(2.dp)
                .weight(1.0f)
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterVertically),
                contentAlignment = Alignment.CenterStart,
            ) {
                TranslationInputBox(
                    text = searchText,
                    placeHolder = "Type text here to translate.",
                    fontSize = 20f,
                    editTextRef = editTextRef,
                    callbackTextChange = onTextChange
                )
            }
        }
    }
}

data class LanguagePickerState(
    val isSource: Boolean,
    val currentCode: String,
    val onSelect: (String) -> Unit
)

@Composable
fun TranslateScreen(
    isLoading: Boolean,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    onLanguageChanged: () -> Unit = {},
    onTextChange: (String) -> Int,
    languageFrom: MutableState<String>,
    languageTo: MutableState<String>
) {
    val searchText = remember { mutableStateOf("") }
    val editTextRef = remember { mutableStateOf<ActionEditText?>(null) }
    var languagePickerState by remember { mutableStateOf<LanguagePickerState?>(null) }

    val onLanguageSelected: () -> Unit = {
        editTextRef.value?.let { edit ->
            edit.post {
                edit.requestFocus()
            }
        }
        onLanguageChanged()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier
                .fillMaxHeight()
                .weight(1.0f)) {
                LanguageSelector(
                    modifier = Modifier
                        .weight(1.0f)
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    isLoading,
                    mSelectedLanguage1 = languageFrom,
                    mSelectedLanguage2 = languageTo,
                    onLanguageSelected = onLanguageSelected,
                    openLanguagePicker = { isSource, currentCode, onSelect ->
                        languagePickerState = LanguagePickerState(isSource, currentCode, onSelect)
                    }
                )
                if (errorMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = onRetry,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "Retry (পুনরায় চেষ্টা)",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                SourceTextInput(
                    modifier = Modifier
                        .weight(2.0f)
                        .fillMaxWidth(),
                    searchText,
                    editTextRef,
                    onTextChange,
                )
            }
        }

        // In-place modal overlay inside TranslateScreen replacing AlertDialog to prevent WindowManager$BadTokenException
        languagePickerState?.let { picker ->
            var pickerSearchQuery by remember { mutableStateOf("") }
            val languages = remember { TranslationHelper.getLanguages() }
            val filteredLanguages = remember(pickerSearchQuery, picker.isSource) {
                languages.entries
                    .filter { (code, name) ->
                        if (!picker.isSource && code == "auto") false
                        else if (pickerSearchQuery.isBlank()) true
                        else name.contains(pickerSearchQuery, ignoreCase = true) || code.contains(pickerSearchQuery, ignoreCase = true)
                    }
                    .sortedBy { it.value }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Language (ভাষা নির্বাচন)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(
                            onClick = { languagePickerState = null },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Close",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    OutlinedTextField(
                        value = pickerSearchQuery,
                        onValueChange = { pickerSearchQuery = it },
                        placeholder = { Text("Search language...", fontSize = 14.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(filteredLanguages) { (code, name) ->
                            val isSelected = picker.currentCode == code
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        picker.onSelect(code)
                                        languagePickerState = null
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = code,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

val TranslateAction = Action(
    icon = R.drawable.nth_translate,
    name = R.string.translate_action_title,
    simplePressImpl = null,
    persistentState = null,
    canShowKeyboard = true,
    windowImpl = { manager, persistentState ->
        object : ActionWindow {
            @Composable
            override fun windowName(): String {
                return stringResource(R.string.translate_action_title)
            }

            @Composable
            override fun WindowContents(keyboardShown: Boolean) {
                val (sourcePref, _) = useSharedPrefsString(Settings.PREF_KEY_LAST_SOURCE_LANG, "auto")
                val (targetPref, _) = useSharedPrefsString(Settings.PREF_KEY_LAST_TARGET_LANG, "bn")

                val languageFrom = remember { mutableStateOf(sourcePref) }
                val languageTo = remember { mutableStateOf(targetPref) }
                val isLoading = remember { mutableStateOf(false) }
                val error = remember { mutableStateOf<String?>(null) }
                val currentText = remember { mutableStateOf("") }

                val debounceJob = remember { mutableStateOf<Job?>(null) }
                val lastTranslatedLen = remember { mutableStateOf(0) }

                val coroutineScope = rememberCoroutineScope()

                fun performTranslate(textToTranslate: String) {
                    debounceJob.value?.cancel()
                    if (textToTranslate.isNotEmpty()) {
                        debounceJob.value = coroutineScope.launch {
                            isLoading.value = true
                            error.value = null
                            delay(500)
                            if (languageFrom.value == languageTo.value) {
                                manager.replaceTranslationSessionText(textToTranslate, lastTranslatedLen.value)
                                lastTranslatedLen.value = textToTranslate.length
                                isLoading.value = false
                            } else {
                                val result = CloudTranslationHandler.translate(
                                    languageFrom.value,
                                    languageTo.value,
                                    textToTranslate
                                )
                                result.fold(
                                    onSuccess = {
                                        Log.d(
                                            TAG,
                                            "Translation Success"
                                        )
                                        manager.replaceTranslationSessionText(it, lastTranslatedLen.value)
                                        lastTranslatedLen.value = it.length
                                        isLoading.value = false
                                        error.value = null
                                    },
                                    onFailure = {
                                        Log.d(
                                            TAG,
                                            "Translation fail"  
                                        )
                                        error.value = it.message ?: "Translation failed"
                                        isLoading.value = false
                                    }
                                )
                            }
                        }
                    } else {
                        if (lastTranslatedLen.value > 0) {
                            manager.replaceTranslationSessionText("", lastTranslatedLen.value)
                            lastTranslatedLen.value = 0
                        }
                        isLoading.value = false
                        error.value = null
                    }
                }

                TranslateScreen(
                    isLoading = isLoading.value,
                    errorMessage = error.value,
                    onRetry = {
                        performTranslate(currentText.value)
                    },
                    onLanguageChanged = {
                        if (currentText.value.isNotEmpty()) {
                            performTranslate(currentText.value)
                        }
                    },
                    onTextChange = { updatedString ->
                        currentText.value = updatedString
                        performTranslate(updatedString)
                        0
                    },
                    languageFrom = languageFrom,
                    languageTo = languageTo,
                )
            }

            override fun close() {
                manager.unsetInputConnection()
            }
        }
    }
)
