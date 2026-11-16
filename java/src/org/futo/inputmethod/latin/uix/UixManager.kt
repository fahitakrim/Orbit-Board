package org.futo.inputmethod.latin.uix

import android.app.Activity
import android.app.KeyguardManager
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InlineSuggestionsResponse
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputContentInfo
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import org.futo.inputmethod.latin.uix.theme.NothingDarkBorder
import org.futo.inputmethod.latin.uix.theme.NothingDarkDock
import org.futo.inputmethod.latin.uix.theme.NothingDarkPill
import org.futo.inputmethod.latin.uix.theme.NothingDarkTextPrimary
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.futo.inputmethod.accessibility.AccessibilityUtils
import org.futo.inputmethod.latin.AudioAndHapticFeedbackManager
import org.futo.inputmethod.latin.BuildConfig
import org.futo.inputmethod.latin.LanguageSwitcherDialog
import org.futo.inputmethod.latin.LatinIME
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.SuggestedWords
import org.futo.inputmethod.latin.SuggestedWords.SuggestedWordInfo
import org.futo.inputmethod.latin.common.Constants
import org.futo.inputmethod.latin.inputlogic.InputLogic
import org.futo.inputmethod.latin.suggestions.SuggestionStripView
import org.futo.inputmethod.latin.uix.actions.ActionEditor
import org.futo.inputmethod.latin.uix.actions.ActionRegistry
import org.futo.inputmethod.latin.uix.actions.AllActions
import org.futo.inputmethod.latin.uix.actions.EmojiAction
import org.futo.inputmethod.latin.uix.settings.DataStoreCacheProvider
import org.futo.inputmethod.latin.uix.settings.SettingsActivity
import org.futo.inputmethod.latin.uix.settings.pages.ActionBarDisplayedSetting
import org.futo.inputmethod.latin.uix.settings.useDataStore
import org.futo.inputmethod.latin.uix.theme.ThemeOption
import org.futo.inputmethod.latin.uix.theme.Typography
import org.futo.inputmethod.latin.uix.theme.UixThemeAuto
import org.futo.inputmethod.latin.uix.theme.UixThemeWrapper
import org.futo.inputmethod.updates.DISABLE_UPDATE_REMINDER
import org.futo.inputmethod.updates.autoDeferManualUpdateIfNeeded
import org.futo.inputmethod.updates.deferManualUpdate
import org.futo.inputmethod.updates.isManualUpdateTimeExpired
import org.futo.inputmethod.updates.openManualUpdateCheck
import org.futo.inputmethod.updates.retrieveSavedLastUpdateCheckResult
import java.util.Locale
import org.futo.voiceinput.shared.ui.FakeToast

val LocalManager = staticCompositionLocalOf<KeyboardManagerForAction> {
    error("No LocalManager provided")
}

val LocalThemeProvider = staticCompositionLocalOf<DynamicThemeProvider> {
    error("No LocalThemeProvider provided")
}

val LocalFloatingDragModifier = staticCompositionLocalOf<Modifier> { Modifier }

private class LatinIMEActionInputTransaction(
    private val inputLogic: InputLogic,
    shouldApplySpace: Boolean,
    private val context: Context
): ActionInputTransaction {
    private val isSpaceNecessary: Boolean
    private var isFinished = false

    init {
        val priorText = inputLogic.mConnection.getTextBeforeCursor(1, 0)
        isSpaceNecessary = shouldApplySpace && !priorText.isNullOrEmpty() && !priorText.last().isWhitespace()

        inputLogic.startSuppressingLogic()
    }

    private fun transformText(text: String): String {
        return if(isSpaceNecessary) { " $text" } else { text }
    }

    private var previousText = ""
    override fun updatePartial(text: String) {
        if(isFinished) return
        previousText = text
        inputLogic.mConnection.setComposingText(
            transformText(text),
            1
        )
    }

    override fun commit(text: String) {
        if(isFinished) return
        isFinished = true
        inputLogic.mConnection.commitText(
            transformText(text),
            1
        )
        inputLogic.endSuppressingLogic()
    }

    override fun cancel() {
        commit(previousText)
    }
}

class UixActionKeyboardManager(val uixManager: UixManager, val latinIME: LatinIME) : KeyboardManagerForAction {
    override fun getContext(): Context {
        return latinIME
    }

    override fun getLifecycleScope(): LifecycleCoroutineScope {
        return latinIME.lifecycleScope
    }

    override fun triggerContentUpdate() {
        uixManager.setContent()
    }

    override fun createInputTransaction(applySpaceIfNeeded: Boolean): ActionInputTransaction {
        return LatinIMEActionInputTransaction(latinIME.inputLogic, applySpaceIfNeeded, latinIME)
    }

    override fun typeText(v: String) {
        if(latinIME.isInputConnectionOverridden) {
            latinIME.getBaseInputConnection()?.commitText(v, 1)
        } else {
            latinIME.latinIMELegacy.onTextInput(v)
        }
    }

    override fun replaceAllText(v: String) {
        val ic = latinIME.getBaseInputConnection()
        if (ic != null) {
            ic.setComposingRegion(0, ic.getExtractedText(ExtractedTextRequest(), 0)?.text?.length ?: 0)
            ic.setComposingText("", 1)
            ic.commitText(v, 1)
        }
    }

    override fun replaceTranslationSessionText(newText: String, prevLen: Int) {
        val ic = latinIME.getBaseInputConnection()
        if (ic != null) {
            if (prevLen > 0) {
                ic.deleteSurroundingText(prevLen, 0)
            }
            if (newText.isNotEmpty()) {
                ic.commitText(newText, 1)
            }
        }
    }

    override fun typeUri(uri: Uri, mimeTypes: List<String>): Boolean {
        if(mimeTypes.isEmpty()) {
            Log.w("UixManager", "mimeTypes is empty")
            return false
        }

        val description = ClipDescription("Pasted image", mimeTypes.toTypedArray())

        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            InputContentInfo(uri, description, null)
        } else {
            return false
        }

        return try {
            latinIME.currentInputConnection?.commitContent(info, InputConnection.INPUT_CONTENT_GRANT_READ_URI_PERMISSION, null) ?: run {
                Log.w("UixManager", "Current input connection is null")
                false
            }
        } catch (e: Throwable) {
            Log.e("UixManager", "Failed to commit image content", e)
            false
        }
    }

    override fun backspace(amount: Int) {
        latinIME.latinIMELegacy.onCodeInput(
            Constants.CODE_DELETE,
            Constants.NOT_A_COORDINATE,
            Constants.NOT_A_COORDINATE, false)
    }

    override fun deleteCharacterBeforeCursor() {
        val ic = latinIME.currentInputConnection ?: latinIME.getBaseInputConnection()
        if (ic == null) {
            latinIME.sendDownUpKeyEvents(android.view.KeyEvent.KEYCODE_DEL)
            return
        }

        try {
            val selected = ic.getSelectedText(0)
            if (!selected.isNullOrEmpty()) {
                ic.commitText("", 1)
                return
            }

            // If there's an active composing word in InputLogic, route to LatinIME's composing backspace
            if (!latinIME.isInputConnectionOverridden && latinIME.inputLogic.mWordComposer?.isComposingWord == true) {
                latinIME.latinIMELegacy.onCodeInput(
                    Constants.CODE_DELETE,
                    Constants.NOT_A_COORDINATE,
                    Constants.NOT_A_COORDINATE,
                    false
                )
                return
            }

            val textBefore = ic.getTextBeforeCursor(16, 0)
            if (textBefore.isNullOrEmpty()) {
                if (!ic.deleteSurroundingText(1, 0)) {
                    latinIME.sendDownUpKeyEvents(android.view.KeyEvent.KEYCODE_DEL)
                }
                return
            }

            val breakIterator = java.text.BreakIterator.getCharacterInstance()
            breakIterator.setText(textBefore.toString())
            val end = breakIterator.last()
            val start = breakIterator.previous()

            val lengthToDelete = if (start != java.text.BreakIterator.DONE && end > start) {
                end - start
            } else {
                1
            }

            if (!ic.deleteSurroundingText(lengthToDelete, 0)) {
                latinIME.sendDownUpKeyEvents(android.view.KeyEvent.KEYCODE_DEL)
            }
        } catch (e: Exception) {
            Log.e("UixManager", "Error deleting character before cursor", e)
            latinIME.sendDownUpKeyEvents(android.view.KeyEvent.KEYCODE_DEL)
        }
    }

    override fun closeActionWindow() {
        if(uixManager.currWindowActionWindow == null) return
        uixManager.returnBackToMainKeyboardViewFromAction()
    }

    override fun triggerSystemVoiceInput() {
        uixManager.systemVoiceInputManager.toggle(getActiveLocale())
    }

    override fun updateTheme(newTheme: ThemeOption) {
        latinIME.updateTheme(newTheme)
    }

    override fun getThemeProvider(): DynamicThemeProvider {
        return latinIME.getDrawableProvider()
    }

    override fun sendCodePointEvent(codePoint: Int) {
        latinIME.latinIMELegacy.onCodeInput(codePoint,
            Constants.NOT_A_COORDINATE,
            Constants.NOT_A_COORDINATE, false)
    }

    override fun sendKeyEvent(keyCode: Int, metaState: Int) {
        latinIME.inputLogic.sendDownUpKeyEvent(keyCode, metaState)
    }

    override fun performEditAction(id: Int) {
        val ic = latinIME.getBaseInputConnection()
        var handled = false
        when (id) {
            android.R.id.selectAll -> {
                try {
                    val before = ic?.getTextBeforeCursor(20000, 0) ?: ""
                    val after = ic?.getTextAfterCursor(20000, 0) ?: ""
                    val totalLen = before.length + after.length
                    if (totalLen > 0) {
                        ic?.setSelection(0, totalLen)
                        handled = true
                    }
                } catch (t: Throwable) {
                    Log.w("UixManager", "Select all via setSelection failed", t)
                }
            }
            android.R.id.copy -> {
                try {
                    val selected = ic?.getSelectedText(0)
                    if (!selected.isNullOrEmpty()) {
                        val cm = latinIME.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                        cm?.setPrimaryClip(android.content.ClipData.newPlainText(null, selected))
                        handled = true
                    }
                } catch (t: Throwable) {
                    Log.w("UixManager", "Copy via clipboard failed", t)
                }
            }
            android.R.id.cut -> {
                try {
                    val selected = ic?.getSelectedText(0)
                    if (!selected.isNullOrEmpty()) {
                        val cm = latinIME.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                        cm?.setPrimaryClip(android.content.ClipData.newPlainText(null, selected))
                        ic?.commitText("", 1)
                        handled = true
                    }
                } catch (t: Throwable) {
                    Log.w("UixManager", "Cut via clipboard failed", t)
                }
            }
            android.R.id.paste -> {
                try {
                    val cm = latinIME.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                    if (cm != null && cm.hasPrimaryClip()) {
                        val clip = cm.primaryClip
                        if (clip != null && clip.itemCount > 0) {
                            val text = clip.getItemAt(0).coerceToText(latinIME)
                            if (!text.isNullOrEmpty()) {
                                ic?.commitText(text, 1)
                                handled = true
                            }
                        }
                    }
                } catch (t: Throwable) {
                    Log.w("UixManager", "Clipboard paste failed, trying performContextMenuAction", t)
                }
            }
        }
        if (!handled && ic != null) {
            handled = ic.performContextMenuAction(id)
        }
        if (!handled) {
            val (keyCode, meta) = when (id) {
                android.R.id.copy -> KeyEvent.KEYCODE_C to KeyEvent.META_CTRL_ON
                android.R.id.paste -> KeyEvent.KEYCODE_V to KeyEvent.META_CTRL_ON
                android.R.id.cut -> KeyEvent.KEYCODE_X to KeyEvent.META_CTRL_ON
                android.R.id.selectAll -> KeyEvent.KEYCODE_A to KeyEvent.META_CTRL_ON
                android.R.id.undo -> KeyEvent.KEYCODE_Z to KeyEvent.META_CTRL_ON
                android.R.id.redo -> KeyEvent.KEYCODE_Y to KeyEvent.META_CTRL_ON
                else -> 0 to 0
            }
            if (keyCode != 0) {
                latinIME.inputLogic.sendDownUpKeyEvent(keyCode, meta)
            }
        }
    }

    override fun cursorLeft(steps: Int, stepOverWords: Boolean, select: Boolean) {
        latinIME.inputLogic.cursorLeft(steps, stepOverWords, select)
    }

    override fun cursorRight(steps: Int, stepOverWords: Boolean, select: Boolean) {
        latinIME.inputLogic.cursorRight(steps, stepOverWords, select)
    }

    override fun performHapticAndAudioFeedback(code: Int, view: View) {
        AudioAndHapticFeedbackManager.getInstance().performHapticAndAudioFeedback(code, view)
    }

    override fun getActiveLocale(): Locale {
        return latinIME.latinIMELegacy.locale
    }

    override fun overrideInputConnection(inputConnection: InputConnection, editorInfo: EditorInfo) {
        latinIME.overrideInputConnection(inputConnection, editorInfo)
        uixManager.toggleExpandAction(true)
        uixManager.isInputOverridden.value = true
    }

    override fun unsetInputConnection() {
        latinIME.overrideInputConnection(null, null)
        uixManager.isInputOverridden.value = false
    }

    override fun requestDialog(text: String, options: List<DialogRequestItem>, onCancel: () -> Unit) {
        uixManager.activeDialogRequest.value = ActiveDialogRequest(text, options, onCancel)
        uixManager.activeDialogRequestDismissed.value = false
    }

    override fun openInputMethodPicker() {
        uixManager.showLanguageSwitcher()
    }

    override fun announce(s: String) {
        AccessibilityUtils.init(getContext())
        if(AccessibilityUtils.getInstance().isAccessibilityEnabled) {
            AccessibilityUtils.getInstance().announceForAccessibility(uixManager.getComposeView(), s)
        }
    }

    override fun activateAction(action: Action) {
        uixManager.onActionActivated(action)
    }

    override fun showActionEditor() {
        uixManager.showActionEditor()
    }

    override fun hideActionEditor() {
        uixManager.hideActionEditor()
    }

    override fun isDeviceLocked(): Boolean {
        return getContext().isDeviceLocked
    }

    override fun getLatinIMEForDebug(): LatinIME = latinIME
}

data class ActiveDialogRequest(
    val text: String,
    val options: List<DialogRequestItem>,
    val onCancel: () -> Unit
)

class UixManager(private val latinIME: LatinIME) {
    val shouldShowSuggestionStripState = mutableStateOf(true)
    var shouldShowSuggestionStrip: Boolean
        get() = shouldShowSuggestionStripState.value
        set(value) { shouldShowSuggestionStripState.value = value }

    val suggestedWordsState = mutableStateOf<SuggestedWords?>(null)
    var suggestedWords: SuggestedWords?
        get() = suggestedWordsState.value
        set(value) { suggestedWordsState.value = value }

    private var composeView: ComposeView? = null

    val currWindowActionWindowState = mutableStateOf<ActionWindow?>(null)
    var currWindowActionWindow: ActionWindow?
        get() = currWindowActionWindowState.value
        set(value) { currWindowActionWindowState.value = value }

    val isMainKeyboardHiddenState = mutableStateOf(false)
    var isMainKeyboardHidden: Boolean
        get() = isMainKeyboardHiddenState.value
        set(value) { isMainKeyboardHiddenState.value = value }
    @get:JvmName("getMainKeyboardHiddenLegacy")
    @set:JvmName("setMainKeyboardHiddenLegacy")
    private var mainKeyboardHidden: Boolean
        get() = isMainKeyboardHiddenState.value
        set(value) { isMainKeyboardHiddenState.value = value }

    val currWindowActionState = mutableStateOf<Action?>(null)
    var currWindowAction: Action?
        get() = currWindowActionState.value
        set(value) { currWindowActionState.value = value }

    private var persistentStates: HashMap<Action, PersistentActionState?> = hashMapOf()

    private var inlineSuggestions: List<MutableState<View?>> = listOf()
    private val keyboardManagerForAction = UixActionKeyboardManager(this, latinIME)

    private var numSuggestionsSinceNotice = 0
    private var currentNotice: MutableState<ImportantNotice?> = mutableStateOf(null)

    // Transient status message (shown via FakeToast for voice input etc.)
    val assistantMessage: MutableState<String?> = mutableStateOf(null)

    fun showAssistantMessage(msg: String?) {
        assistantMessage.value = msg
    }

    val systemVoiceInputManager = org.futo.inputmethod.latin.uix.voiceinput.SystemVoiceInputManager(latinIME)

    fun triggerSystemVoiceInput() {
        systemVoiceInputManager.toggle(latinIME.latinIMELegacy.locale ?: java.util.Locale.getDefault())
    }

    private var isActionsExpanded = mutableStateOf(false)
    private fun toggleActionsExpanded() {
        isActionsExpanded.value = !isActionsExpanded.value
        try {
            latinIME.deferSetSetting(ActionBarExpanded, isActionsExpanded.value)
        } catch (_: Throwable) {}
    }

    private var isShowingActionEditor = mutableStateOf(false)
    fun showActionEditor() {
        isShowingActionEditor.value = true
    }

    fun hideActionEditor() {
        isShowingActionEditor.value = false
    }

    var isInputOverridden = mutableStateOf(false)

    fun consumeBackKey(): Boolean {
        if (!activeDialogRequestDismissed.value) {
            activeDialogRequestDismissed.value = true
            activeDialogRequest.value?.onCancel?.invoke()
            return true
        }
        if (isShowingActionEditor.value) {
            isShowingActionEditor.value = false
            return true
        }
        if (currWindowActionWindowState.value != null) {
            closeActionWindow()
            return true
        }
        return false
    }

    fun onActionActivated(rawAction: Action) {
        latinIME.inputLogic.finishInput()

        val action = runBlocking {
            ActionRegistry.getActionOverride(latinIME, rawAction)
        }

        if (action.windowImpl != null) {
            enterActionWindowView(action)
        } else if (action.simplePressImpl != null) {
            action.simplePressImpl.invoke(keyboardManagerForAction, persistentStates[action])
        } else {
            throw IllegalStateException("An action must have either a window implementation or a simple press implementation")
        }
    }

    fun onActionAltActivated(rawAction: Action) {
        latinIME.inputLogic.finishInput()

        val action = runBlocking {
            ActionRegistry.getActionOverride(latinIME, rawAction)
        }

        action.altPressImpl?.invoke(keyboardManagerForAction, persistentStates[action])
    }

    @Composable
    private fun MainKeyboardViewWithActionBar(suggestedWordsOverride: SuggestedWords? = null) {
        val view = LocalView.current

        val actionBarShown = useDataStore(ActionBarDisplayedSetting)

        Column {
            // Don't show suggested words when it's not meant to be shown
            val currentWords = suggestedWordsOverride ?: suggestedWordsState.value
            val suggestedWordsOrNull = if(shouldShowSuggestionStripState.value) {
                currentWords
            } else {
                null
            }

            if(actionBarShown.value) {
                ActionBar(
                    suggestedWordsOrNull,
                    latinIME.latinIMELegacy as SuggestionStripView.Listener,
                    inlineSuggestions = inlineSuggestions,
                    onActionActivated = {
                        keyboardManagerForAction.performHapticAndAudioFeedback(
                            Constants.CODE_TAB,
                            view
                        )
                        onActionActivated(it)
                    },
                    onActionAltActivated = {
                        if (it.altPressImpl != null) {
                            keyboardManagerForAction.performHapticAndAudioFeedback(
                                Constants.CODE_TAB,
                                view
                            )
                        }
                        onActionAltActivated(it)
                    },
                    importantNotice = currentNotice.value,
                    keyboardManagerForAction = keyboardManagerForAction,
                    isActionsExpanded = isActionsExpanded.value,
                    toggleActionsExpanded = { toggleActionsExpanded() },
                )
            }
        }
    }

    private fun enterActionWindowView(action: Action) {
        isShowingActionEditor.value = false
        assert(action.windowImpl != null)

        if (currWindowActionWindow != null) {
            try {
                currWindowActionWindow?.close()
            } catch (_: Throwable) {}
        }

        isMainKeyboardHidden = true

        currWindowAction = action

        if (persistentStates[action] == null) {
            persistentStates[action] = action.persistentState?.let { it(keyboardManagerForAction) }
        }

        currWindowActionWindow = (action.windowImpl!!)(keyboardManagerForAction, persistentStates[action])

        if(action.keepScreenAwake) {
            latinIME.window.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        setContent()

        keyboardManagerForAction.announce("${latinIME.resources.getString(action.name)} mode")
    }

    fun returnBackToMainKeyboardViewFromAction() {
        isShowingActionEditor.value = false
        if(currWindowActionWindow == null) return

        val name = currWindowAction?.let {
            try { latinIME.resources.getString(it.name) } catch (_: Throwable) { "" }
        } ?: ""

        try {
            currWindowActionWindow?.close()
        } catch (_: Throwable) {}

        currWindowAction = null
        currWindowActionWindow = null

        isMainKeyboardHidden = false

        latinIME.onKeyboardShown()

        latinIME.window.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent()

        if (name.isNotEmpty()) {
            keyboardManagerForAction.announce("$name closed")
        }
    }

    fun toggleExpandAction(to: Boolean? = null) {
        isMainKeyboardHidden = !(to ?: isMainKeyboardHidden)
        if(!isMainKeyboardHidden) {
            latinIME.onKeyboardShown()
        }

        setContent()
    }

    @Composable
    private fun ActionViewWithHeader(windowImpl: ActionWindow) {
        val heightDiv = if(isMainKeyboardHidden) {
            1
        } else {
            1.5
        }
        val density = LocalDensity.current
        val targetHeight = with(density) {
            val measuredHeight = latinIME.getInputViewHeight().toFloat()
            val safeHeight = if (measuredHeight > 0f) measuredHeight else (260f * this.density)
            (safeHeight / heightDiv.toFloat()).toDp()
        }
        Column {
            if(isMainKeyboardHidden || latinIME.isInputConnectionOverridden) {
                ActionWindowBar(
                    onBack = { returnBackToMainKeyboardViewFromAction() },
                    canExpand = currWindowAction?.canShowKeyboard == true,
                    onExpand = { toggleExpandAction() },
                    windowTitleBar = { windowImpl.WindowTitleBar(this) }
                )
            }

            Box(modifier = Modifier
                .fillMaxWidth()
                .height(targetHeight)
            ) {
                windowImpl.WindowContents(keyboardShown = !isMainKeyboardHidden)
            }

            if(!isMainKeyboardHidden && !latinIME.isInputConnectionOverridden) {
                val suggestedWordsOrNull = if (shouldShowSuggestionStrip) {
                    suggestedWords
                } else {
                    null
                }

                CollapsibleSuggestionsBar(
                    onCollapse = { toggleExpandAction() },
                    onClose = { returnBackToMainKeyboardViewFromAction() },
                    words = suggestedWordsOrNull,
                    suggestionStripListener = latinIME.latinIMELegacy as SuggestionStripView.Listener
                )
            }
        }
    }

    val activeDialogRequest: MutableState<ActiveDialogRequest?> = mutableStateOf(null)
    val activeDialogRequestDismissed: MutableState<Boolean> = mutableStateOf(true)

    @Composable
    fun BoxScope.ForgetWordDialog() {
        AnimatedVisibility(
            visible = !activeDialogRequestDismissed.value,
            modifier = Modifier.matchParentSize(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (activeDialogRequest.value != null) {
                Box(modifier = Modifier.matchParentSize()) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.66f),
                        modifier = Modifier
                            .matchParentSize()
                            .pointerInput(Unit) {
                                this.detectTapGestures(onPress = {
                                    activeDialogRequestDismissed.value = true
                                    activeDialogRequest.value?.onCancel?.invoke()
                                })
                            }
                    ) { }

                    Box(modifier = Modifier
                        .matchParentSize()
                        .padding(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    activeDialogRequest.value?.text ?: "",
                                    style = Typography.bodyMedium
                                )

                                Row {
                                    activeDialogRequest.value?.options?.forEach {
                                        TextButton(
                                            onClick = {
                                                it.onClick()
                                                activeDialogRequestDismissed.value = true
                                            }
                                        ) {
                                            Text(it.option)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private var languageSwitcherDialog: DialogComposeView? = null
    fun showLanguageSwitcher() {
        // Dismiss old dialog
        languageSwitcherDialog?.dismiss()

        // Create new dialog
        languageSwitcherDialog = createDialogComposeView(latinIME) {
            DataStoreCacheProvider {
                UixThemeAuto {
                    LanguageSwitcherDialog(
                        onDismiss = { it.dismiss() }
                    )
                }
            }
        }

        languageSwitcherDialog?.show()
    }

    @Composable
    fun ActionEditorHost() {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            AnimatedVisibility(
                visible = isShowingActionEditor.value,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
                content = {
                    ActionEditor()
                },
            )
        }
    }

    @Composable
    fun InputDarkener(darken: Boolean, onClose: () -> Unit) {
        val isTypingInAction = isInputOverridden.value && currWindowAction?.canShowKeyboard == true
        val effectiveDarken = darken && !isTypingInAction
        val color by animateColorAsState(
            if (effectiveDarken) Color.Black.copy(alpha = 0.25f) else Color.Transparent
        )

        LaunchedEffect(effectiveDarken) {
            latinIME.setInputModal(effectiveDarken)
        }

        Box(Modifier
            .background(color)
            .fillMaxWidth()
            .fillMaxHeight()
            .then(
                if (effectiveDarken) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures {
                            onClose()
                        }
                    }
                } else {
                    Modifier
                })
        )
    }

    @Composable
    private fun KeyboardContentColumn(
        actionWindow: ActionWindow?,
        isKeyboardHidden: Boolean,
        suggestedWords: SuggestedWords? = null,
        isFloating: Boolean = false,
        modifier: Modifier = Modifier,
        onHeightChanged: (Int) -> Unit = {}
    ) {
        val topDragModifier = LocalFloatingDragModifier.current

        Column(modifier = modifier.onSizeChanged { onHeightChanged(it.height) }) {
            if (isFloating) {
                // Dual-Drag Zone: Top Drag Handle for floating mode
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .background(latinIME.keyboardColor)
                        .then(topDragModifier)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(NothingDarkPill.copy(alpha = 0.6f))
                    )
                }
            }
            when {
                actionWindow != null -> ActionViewWithHeader(actionWindow)
                else -> MainKeyboardViewWithActionBar(suggestedWords)
            }
            latinIME.LegacyKeyboardView(hidden = isKeyboardHidden)
        }
    }

    @Composable
    private fun SideControlDock(
        modifier: Modifier = Modifier,
        displayMode: KeyboardDisplayMode,
        onSwitchSide: () -> Unit,
        onExpand: () -> Unit,
        onReposition: () -> Unit
    ) {
        val isLeftDock = displayMode == KeyboardDisplayMode.ONE_HANDED_RIGHT
        val view = LocalView.current

        val borderModifier = if (isLeftDock) {
            Modifier.drawBehind {
                drawLine(
                    color = NothingDarkBorder,
                    start = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
        } else {
            Modifier.drawBehind {
                drawLine(
                    color = NothingDarkBorder,
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(0f, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        Box(
            modifier = modifier
                .background(NothingDarkDock)
                .then(borderModifier),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                val switchIcon = if (isLeftDock) R.drawable.arrow_left else R.drawable.arrow_right
                SideDockIconButton(
                    icon = switchIcon,
                    contentDescription = stringResource(R.string.one_handed_switch_side),
                    onClick = {
                        AudioAndHapticFeedbackManager.getInstance().performHapticAndAudioFeedback(
                            Constants.CODE_TAB,
                            view
                        )
                        onSwitchSide()
                    }
                )

                SideDockIconButton(
                    icon = R.drawable.maximize,
                    contentDescription = stringResource(R.string.one_handed_expand),
                    onClick = {
                        AudioAndHapticFeedbackManager.getInstance().performHapticAndAudioFeedback(
                            Constants.CODE_TAB,
                            view
                        )
                        onExpand()
                    }
                )

                SideDockIconButton(
                    icon = R.drawable.move,
                    contentDescription = stringResource(R.string.one_handed_reposition),
                    onClick = {
                        AudioAndHapticFeedbackManager.getInstance().performHapticAndAudioFeedback(
                            Constants.CODE_TAB,
                            view
                        )
                        onReposition()
                    }
                )
            }
        }
    }

    @Composable
    private fun SideDockIconButton(
        @androidx.annotation.DrawableRes icon: Int,
        contentDescription: String,
        onClick: () -> Unit
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(NothingDarkPill)
                .border(1.dp, NothingDarkBorder, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = contentDescription,
                tint = NothingDarkTextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
    }

    @Composable
    private fun FloatingDragHandleBar(
        onDock: () -> Unit,
        dragModifier: Modifier = Modifier
    ) {
        val view = LocalView.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(NothingDarkDock)
                .drawBehind {
                    drawLine(
                        color = NothingDarkBorder,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                .then(dragModifier)
                .padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.size(32.dp))

            val dragDesc = stringResource(R.string.floating_drag_handle)
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(NothingDarkPill)
                    .semantics { contentDescription = dragDesc }
            )

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(NothingDarkPill)
                    .border(1.dp, NothingDarkBorder, CircleShape)
                    .clickable {
                        AudioAndHapticFeedbackManager.getInstance().performHapticAndAudioFeedback(
                            Constants.CODE_TAB,
                            view
                        )
                        onDock()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.maximize),
                    contentDescription = stringResource(R.string.floating_dock),
                    tint = NothingDarkTextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    @Composable
    private fun FloatingKeyboardCard(
        modifier: Modifier = Modifier,
        screenWidth: Float,
        screenHeight: Float,
        statusBarHeight: Float,
        navBarHeight: Float,
        savedOffsetX: Float,
        savedOffsetY: Float,
        isLandscape: Boolean,
        onSaveOffset: (Float, Float) -> Unit,
        onDock: () -> Unit,
        keyboardContent: @Composable (Modifier, Boolean) -> Unit
    ) {
        var cardWidthPx by remember { mutableFloatStateOf(0f) }
        var cardHeightPx by remember { mutableFloatStateOf(0f) }

        var offsetX by remember { mutableFloatStateOf(savedOffsetX) }
        var offsetY by remember { mutableFloatStateOf(savedOffsetY) }

        val currentScreenWidth by rememberUpdatedState(screenWidth)
        val currentScreenHeight by rememberUpdatedState(screenHeight)
        val currentStatusBarHeight by rememberUpdatedState(statusBarHeight)
        val currentNavBarHeight by rememberUpdatedState(navBarHeight)

        LaunchedEffect(screenWidth, screenHeight, cardWidthPx, cardHeightPx, savedOffsetX, savedOffsetY) {
            if (cardWidthPx > 0 && cardHeightPx > 0) {
                val (defX, defY) = FloatingPositionHelper.calculateDefaultPosition(
                    screenWidth, screenHeight, cardWidthPx, cardHeightPx, statusBarHeight, navBarHeight
                )
                val targetX = if (savedOffsetX >= 0f) savedOffsetX else defX
                val targetY = if (savedOffsetY >= 0f) savedOffsetY else defY
                offsetX = FloatingPositionHelper.clampOffsetX(targetX, screenWidth, cardWidthPx)
                offsetY = FloatingPositionHelper.clampOffsetY(targetY, screenHeight, cardHeightPx, statusBarHeight, navBarHeight)
                latinIME.updateFloatingBounds(
                    offsetX.toInt(),
                    offsetY.toInt(),
                    (offsetX + cardWidthPx).toInt(),
                    (offsetY + cardHeightPx).toInt()
                )
            }
        }

        val cardShape = RoundedCornerShape(16.dp)

        val dragModifier = Modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragEnd = {
                    onSaveOffset(offsetX, offsetY)
                },
                onDragCancel = {
                    onSaveOffset(offsetX, offsetY)
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    val (defX, defY) = FloatingPositionHelper.calculateDefaultPosition(
                        currentScreenWidth, currentScreenHeight, cardWidthPx, cardHeightPx, currentStatusBarHeight, currentNavBarHeight
                    )
                    val currentX = if (offsetX >= 0f) offsetX else defX
                    val currentY = if (offsetY >= 0f) offsetY else defY
                    val newX = currentX + dragAmount.x
                    val newY = currentY + dragAmount.y
                    offsetX = FloatingPositionHelper.clampOffsetX(newX, currentScreenWidth, cardWidthPx)
                    offsetY = FloatingPositionHelper.clampOffsetY(newY, currentScreenHeight, cardHeightPx, currentStatusBarHeight, currentNavBarHeight)
                    latinIME.updateFloatingBounds(
                        offsetX.toInt(),
                        offsetY.toInt(),
                        (offsetX + cardWidthPx).toInt(),
                        (offsetY + cardHeightPx).toInt()
                    )
                }
            )
        }

        CompositionLocalProvider(LocalFloatingDragModifier provides dragModifier) {
            Surface(
                modifier = modifier
                    .offset {
                        val effectiveCardWidth = if (cardWidthPx <= 0f) currentScreenWidth * (if (isLandscape) 0.70f else 0.78f) else cardWidthPx
                        val defX = (currentScreenWidth - effectiveCardWidth) / 2f
                        val effectiveCardHeight = if (cardHeightPx <= 0f) currentScreenHeight * 0.40f else cardHeightPx
                        val defY = currentScreenHeight - effectiveCardHeight - currentNavBarHeight - 16f
                        val targetX = if (offsetX >= 0f) offsetX else defX
                        val targetY = if (offsetY >= 0f) offsetY else defY
                        val clampedX = FloatingPositionHelper.clampOffsetX(targetX, currentScreenWidth, effectiveCardWidth)
                        val clampedY = FloatingPositionHelper.clampOffsetY(targetY, currentScreenHeight, effectiveCardHeight, currentStatusBarHeight, currentNavBarHeight)
                        IntOffset(clampedX.roundToInt(), clampedY.roundToInt())
                    }
                    .fillMaxWidth(if (isLandscape) 0.70f else 0.78f)
                    .wrapContentHeight()
                    .shadow(elevation = 10.dp, shape = cardShape, clip = false)
                    .border(1.dp, NothingDarkBorder, cardShape)
                    .clip(cardShape)
                    .onSizeChanged { size ->
                        cardWidthPx = size.width.toFloat()
                        cardHeightPx = size.height.toFloat()
                        val (defX, defY) = FloatingPositionHelper.calculateDefaultPosition(
                            currentScreenWidth, currentScreenHeight, cardWidthPx, cardHeightPx, currentStatusBarHeight, currentNavBarHeight
                        )
                        val targetX = if (offsetX >= 0f) offsetX else defX
                        val targetY = if (offsetY >= 0f) offsetY else defY
                        offsetX = FloatingPositionHelper.clampOffsetX(targetX, currentScreenWidth, cardWidthPx)
                        offsetY = FloatingPositionHelper.clampOffsetY(targetY, currentScreenHeight, cardHeightPx, currentStatusBarHeight, currentNavBarHeight)
                        latinIME.updateFloatingBounds(
                            offsetX.toInt(),
                            offsetY.toInt(),
                            (offsetX + cardWidthPx).toInt(),
                            (offsetY + cardHeightPx).toInt()
                        )
                    },
                color = latinIME.keyboardColor,
                shape = cardShape
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    keyboardContent(Modifier.fillMaxWidth().wrapContentHeight(), true)

                    FloatingDragHandleBar(
                        onDock = {
                            closeActionWindow()
                            onDock()
                        },
                        dragModifier = dragModifier
                    )
                }
            }
        }
    }

    fun setContent() {
        composeView?.setContent {
            UixThemeWrapper(latinIME.colorScheme) {
                DataStoreCacheProvider {
                    CompositionLocalProvider(LocalManager provides keyboardManagerForAction) {
                        CompositionLocalProvider(LocalThemeProvider provides latinIME.getDrawableProvider()) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    val (displayModeStr, setDisplayMode) = useDataStore(KEYBOARD_DISPLAY_MODE)
                                val displayMode = try {
                                    KeyboardDisplayMode.valueOf(displayModeStr)
                                } catch (_: Exception) {
                                    KeyboardDisplayMode.STANDARD
                                }

                                val (savedOffsetX, setSavedOffsetX) = useDataStore(FLOATING_KEYBOARD_OFFSET_X)
                                val (savedOffsetY, setSavedOffsetY) = useDataStore(FLOATING_KEYBOARD_OFFSET_Y)

                                LaunchedEffect(displayMode) {
                                    val oldMultiplier = KeyboardDisplayModeManager.getWidthMultiplier(latinIME.resources)
                                    KeyboardDisplayModeManager.currentMode = displayMode
                                    val newMultiplier = KeyboardDisplayModeManager.getWidthMultiplier(latinIME.resources)
                                    if (oldMultiplier != newMultiplier) {
                                        latinIME.reloadKeyboard()
                                    }
                                    if (displayMode != KeyboardDisplayMode.FLOATING) {
                                        latinIME.clearFloatingBounds()
                                        latinIME.updateTouchableHeight()
                                    }
                                    latinIME.window?.window?.decorView?.let { decor ->
                                        decor.post { decor.requestLayout() }
                                    }
                                }

                                var keyboardHeightPx by remember {
                                    mutableIntStateOf(if (latinIME.getInputViewHeight() > 0) latinIME.getInputViewHeight() else 0)
                                }
                                val density = LocalDensity.current
                                val configuration = LocalConfiguration.current
                                val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                                val screenWidthPx = FloatingPositionHelper.getScreenWidth(latinIME)
                                val screenHeightPx = FloatingPositionHelper.getScreenHeight(latinIME)
                                val statusBarHeightPx = FloatingPositionHelper.getStatusBarHeight(latinIME)
                                val navBarHeightPx = FloatingPositionHelper.getNavBarHeight(latinIME)

                                val keyboardContent = remember {
                                    movableContentOf { colModifier: Modifier, isFloating: Boolean ->
                                        KeyboardContentColumn(
                                            actionWindow = currWindowActionWindowState.value,
                                            isKeyboardHidden = isMainKeyboardHiddenState.value,
                                            suggestedWords = suggestedWordsState.value,
                                            isFloating = isFloating,
                                            modifier = colModifier,
                                            onHeightChanged = { if (it > 0) keyboardHeightPx = it }
                                        )
                                    }
                                }

                                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                    val containerWidthPx = constraints.maxWidth.toFloat()
                                    val containerHeightPx = constraints.maxHeight.toFloat()
                                    val effectiveScreenWidth = if (containerWidthPx > 0f) containerWidthPx else screenWidthPx
                                    val effectiveScreenHeight = if (containerHeightPx > 0f) containerHeightPx else screenHeightPx
                                    if (displayMode == KeyboardDisplayMode.FLOATING) {
                                        FloatingKeyboardCard(
                                            modifier = Modifier.wrapContentSize(),
                                            screenWidth = effectiveScreenWidth,
                                            screenHeight = effectiveScreenHeight,
                                            statusBarHeight = statusBarHeightPx,
                                            navBarHeight = navBarHeightPx,
                                            savedOffsetX = savedOffsetX,
                                            savedOffsetY = savedOffsetY,
                                            isLandscape = isLandscape,
                                            onSaveOffset = { x, y ->
                                                setSavedOffsetX(x)
                                                setSavedOffsetY(y)
                                            },
                                            onDock = {
                                                closeActionWindow()
                                                KeyboardDisplayModeManager.currentMode = KeyboardDisplayMode.STANDARD
                                                setDisplayMode(KeyboardDisplayMode.STANDARD.name)
                                                latinIME.clearFloatingBounds()
                                                latinIME.reloadKeyboard()
                                                latinIME.updateTouchableHeight()
                                                latinIME.window?.window?.decorView?.let { decor ->
                                                    decor.post { decor.requestLayout() }
                                                }
                                            },
                                            keyboardContent = keyboardContent
                                        )
                                    } else {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.Bottom
                                        ) {
                                            Surface(modifier = Modifier.onSizeChanged {
                                                latinIME.updateTouchableHeight(it.height)
                                            }, color = latinIME.keyboardColor) {
                                                Box {
                                                    @Composable
                                                    fun RowScope.DockItem(mode: KeyboardDisplayMode, colWeight: Float) {
                                                        SideControlDock(
                                                            modifier = Modifier
                                                                .weight(colWeight)
                                                                .then(
                                                                    if (keyboardHeightPx > 0) Modifier.height(with(density) { keyboardHeightPx.toFloat().toDp() })
                                                                    else Modifier.wrapContentHeight()
                                                                ),
                                                            displayMode = mode,
                                                            onSwitchSide = {
                                                                val targetMode = if (mode == KeyboardDisplayMode.ONE_HANDED_RIGHT) KeyboardDisplayMode.ONE_HANDED_LEFT else KeyboardDisplayMode.ONE_HANDED_RIGHT
                                                                KeyboardDisplayModeManager.currentMode = targetMode
                                                                setDisplayMode(targetMode.name)
                                                                latinIME.updateTouchableHeight()
                                                                latinIME.window?.window?.decorView?.let { decor ->
                                                                    decor.post { decor.requestLayout() }
                                                                }
                                                            },
                                                            onExpand = {
                                                                KeyboardDisplayModeManager.currentMode = KeyboardDisplayMode.STANDARD
                                                                setDisplayMode(KeyboardDisplayMode.STANDARD.name)
                                                                latinIME.reloadKeyboard()
                                                                latinIME.updateTouchableHeight()
                                                                latinIME.window?.window?.decorView?.let { decor ->
                                                                    decor.post { decor.requestLayout() }
                                                                }
                                                            },
                                                            onReposition = {
                                                                try {
                                                                    val intent = Intent(latinIME, SettingsActivity::class.java).apply {
                                                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                                        putExtra("route", "resize")
                                                                        putExtra("navDest", "resize")
                                                                    }
                                                                    latinIME.startActivity(intent)
                                                                } catch (e: Exception) {
                                                                    Log.e("UixManager", "Failed to start resize screen", e)
                                                                }
                                                            }
                                                        )
                                                    }

                                                    val isOneHanded = displayMode == KeyboardDisplayMode.ONE_HANDED_LEFT || displayMode == KeyboardDisplayMode.ONE_HANDED_RIGHT

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = if (isLandscape && !isOneHanded) Arrangement.Center else Arrangement.Start,
                                                        verticalAlignment = Alignment.Bottom
                                                    ) {
                                                        if (displayMode == KeyboardDisplayMode.ONE_HANDED_RIGHT) {
                                                            DockItem(displayMode, 0.22f)
                                                        }

                                                        val contentModifier = when {
                                                            isOneHanded -> Modifier.weight(0.78f)
                                                            isLandscape -> Modifier.fillMaxWidth(0.70f)
                                                            else -> Modifier.fillMaxWidth()
                                                        }

                                                        keyboardContent(contentModifier, false)

                                                        if (displayMode == KeyboardDisplayMode.ONE_HANDED_LEFT) {
                                                            DockItem(displayMode, 0.22f)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    ForgetWordDialog()

                                    // Transient message (FakeToast) shown at bottom center
                                    FakeToast(modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-16).dp), message = assistantMessage.value)
                                }

                                InputDarkener(isInputOverridden.value || isShowingActionEditor.value) {
                                    closeActionWindow()
                                    isShowingActionEditor.value = false
                                }

                                ActionEditorHost()
                            }
                        }
                        }
                    }
                }
            }
        }
    }

    suspend fun showUpdateNoticeIfNeeded() {
        if(!BuildConfig.UPDATE_CHECKING) return

        autoDeferManualUpdateIfNeeded(latinIME)

        val updateInfo = retrieveSavedLastUpdateCheckResult(latinIME)
        if(updateInfo != null && updateInfo.isNewer()) {
            if(!latinIME.getSetting(DISABLE_UPDATE_REMINDER)) {
                numSuggestionsSinceNotice = 0
                currentNotice.value = object : ImportantNotice {
                    @Composable
                    override fun getText(): String {
                        return "Update available: ${updateInfo.nextVersionString}"
                    }

                    override fun onDismiss(context: Context) {
                        currentNotice.value = null
                    }

                    override fun onOpen(context: Context) {
                        currentNotice.value = null

                        val intent = Intent(context, SettingsActivity::class.java)
                        intent.putExtra("navDest", "update")

                        if (context !is Activity) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }

                        context.startActivity(intent)
                    }
                }
            }
        } else {
            if(isManualUpdateTimeExpired(latinIME)) {
                numSuggestionsSinceNotice = 0
                currentNotice.value = object : ImportantNotice {
                    @Composable
                    override fun getText(): String {
                        return "Please tap to check for updates"
                    }

                    override fun onDismiss(context: Context) {
                        currentNotice.value = null
                    }

                    override fun onOpen(context: Context) {
                        currentNotice.value = null
                        context.openManualUpdateCheck()

                        runBlocking {
                            deferManualUpdate(latinIME)
                        }
                    }
                }

            }
        }
    }

    fun createComposeView(): View {
        if(composeView != null) {
            composeView = null
            //throw IllegalStateException("Attempted to create compose view, when one is already created!")
        }

        composeView = ComposeView(latinIME).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setParentCompositionContext(null)

            latinIME.setOwners(this)
        }

        setContent()

        return composeView!!
    }

    fun getComposeView(): View? {
        return composeView
    }

    fun onColorSchemeChanged() {
        setContent()
    }

    fun onInputFinishing() {
        systemVoiceInputManager.stopListening()
        closeActionWindow()
        languageSwitcherDialog?.dismiss()
        isShowingActionEditor.value = false
    }

    fun cleanUpPersistentStates() {
        println("Cleaning up persistent states")
        systemVoiceInputManager.destroy()
        for((key, value) in persistentStates.entries) {
            if(currWindowAction != key) {
                latinIME.lifecycleScope.launch { value?.cleanUp() }
            }
        }
    }

    fun closeActionWindow() {
        isShowingActionEditor.value = false
        if(currWindowActionWindow == null) return
        returnBackToMainKeyboardViewFromAction()
    }


    fun updateVisibility(shouldShowSuggestionsStrip: Boolean, fullscreenMode: Boolean) {
        this.shouldShowSuggestionStrip = shouldShowSuggestionsStrip
        setContent()
    }

    fun setSuggestions(suggestedWords: SuggestedWords?, rtlSubtype: Boolean) {
        this.suggestedWords = suggestedWords
        setContent()

        if(currentNotice.value != null) {
            numSuggestionsSinceNotice += 1
            if(numSuggestionsSinceNotice > 4) {
                currentNotice.value?.onDismiss(latinIME)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun onInlineSuggestionsResponse(response: InlineSuggestionsResponse): Boolean {
        currentNotice.value?.onDismiss(latinIME)

        inlineSuggestions = response.inlineSuggestions.map {
            latinIME.inflateInlineSuggestion(it)
        }
        setContent()

        return true
    }

    fun openEmojiKeyboard() {
        if(currWindowAction == null) {
            onActionActivated(EmojiAction)
        }
    }

    fun triggerAction(id: Int, alt: Boolean) {
        val action = AllActions.getOrNull(id) ?: throw IllegalArgumentException("No such action with ID $id")

        if(alt) {
            onActionAltActivated(action)
        } else {
            if (currWindowAction != null && action.windowImpl != null) {
                closeActionWindow()
            }

            onActionActivated(action)
        }
    }

    fun requestForgetWord(suggestedWordInfo: SuggestedWords.SuggestedWordInfo) {
        keyboardManagerForAction.requestDialog(
            latinIME.getString(R.string.blacklist_from_suggestions, suggestedWordInfo.mWord),
            listOf(
                DialogRequestItem(latinIME.getString(R.string.cancel)) { },
                DialogRequestItem(latinIME.getString(R.string.blacklist)) {
                    latinIME.forceForgetWord(suggestedWordInfo)
                },
            ) + if(suggestedWordInfo.mKindAndFlags == SuggestedWordInfo.KIND_EMOJI_SUGGESTION) {
                listOf(
                    DialogRequestItem(latinIME.getString(R.string.disable_emoji)) {
                        runBlocking { latinIME.setSetting(SHOW_EMOJI_SUGGESTIONS, false) }
                        latinIME.refreshSuggestions()
                    }
                )
            } else {
                listOf()
            },
            { }
        )

        val v = latinIME.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v!!.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            v!!.vibrate(50)
        }
    }

    fun onCreate() {
        AllActions.forEach { action ->
            if(action.persistentStateInitialization == PersistentStateInitialization.OnKeyboardLoad) {
                persistentStates[action] = action.persistentState?.let { it(keyboardManagerForAction) }
            }
        }

        isActionsExpanded.value = try { latinIME.getSettingBlocking(ActionBarExpanded) } catch (_: Throwable) { false }
    }

    fun onPersistentStatesUnlocked() {
        persistentStates.forEach {
            latinIME.lifecycleScope.launch {
                it.value?.onDeviceUnlocked()
            }
        }
    }
}
