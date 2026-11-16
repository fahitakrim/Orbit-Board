package org.futo.inputmethod.latin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Rect
import org.futo.inputmethod.latin.uix.KeyboardDisplayMode

import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.CompletionInfo
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InlineSuggestionsRequest
import android.view.inputmethod.InlineSuggestionsResponse
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodSubtype
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import android.widget.Toast
import androidx.core.content.ContextCompat
import org.futo.inputmethod.latin.SuggestedWords.SuggestedWordInfo
import org.futo.inputmethod.latin.common.Constants
import org.futo.inputmethod.latin.settings.Settings
import org.futo.inputmethod.latin.uix.PreferenceUtils
import org.futo.inputmethod.latin.uix.BasicThemeProvider
import org.futo.inputmethod.latin.uix.DynamicThemeProvider
import org.futo.inputmethod.latin.uix.DynamicThemeProviderOwner
import org.futo.inputmethod.latin.uix.EmojiTracker.unuseEmoji
import org.futo.inputmethod.latin.uix.EmojiTracker.useEmoji
import org.futo.inputmethod.latin.uix.HiddenKeysSetting
import org.futo.inputmethod.latin.uix.KeyboardBottomOffsetSetting
import org.futo.inputmethod.latin.uix.KeyboardDisplayModeManager
import org.futo.inputmethod.latin.uix.KeyboardFontSetting
import org.futo.inputmethod.latin.uix.KeyboardHeightMultiplierSetting
import org.futo.inputmethod.latin.uix.SUGGESTION_BLACKLIST
import org.futo.inputmethod.latin.uix.THEME_KEY
import org.futo.inputmethod.latin.uix.UixManager
import org.futo.inputmethod.latin.uix.createInlineSuggestionsRequest
import org.futo.inputmethod.latin.uix.dataStore
import org.futo.inputmethod.latin.uix.deferGetSetting
import org.futo.inputmethod.latin.uix.deferSetSetting
import org.futo.inputmethod.latin.uix.differsFrom
import org.futo.inputmethod.latin.uix.get
import org.futo.inputmethod.latin.uix.getSetting
import org.futo.inputmethod.latin.uix.getSettingBlocking
import org.futo.inputmethod.latin.uix.getSettingFlow
import org.futo.inputmethod.latin.uix.isDirectBootUnlocked
import org.futo.inputmethod.latin.uix.setSetting
import org.futo.inputmethod.latin.uix.theme.DarkColorScheme
import org.futo.inputmethod.latin.uix.theme.ThemeOption
import org.futo.inputmethod.latin.uix.theme.ThemeOptions
import org.futo.inputmethod.latin.uix.theme.applyWindowColors
import org.futo.inputmethod.latin.uix.theme.presets.OrbitTheme
import org.futo.inputmethod.latin.uix.theme.presets.VoiceInputTheme
import org.futo.inputmethod.latin.xlm.LanguageModelFacilitator
import org.futo.inputmethod.updates.scheduleUpdateCheckingJob

private class UnlockedBroadcastReceiver(val onDeviceUnlocked: () -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        println("Unlocked Broadcast Receiver: ${intent?.action}")
        if (intent?.action == Intent.ACTION_USER_UNLOCKED) {
            onDeviceUnlocked()
        }
    }
}

class LatinIME : InputMethodService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner,
    LatinIMELegacy.SuggestionStripController, DynamicThemeProviderOwner {

    companion object {
        var instance: LatinIME? = null
            private set

        private const val PREFS_INSETS = "latinime_insets"
        private const val PREF_LAST_PORTRAIT_HEIGHT = "last_portrait_touchable_height"
        private const val PREF_LAST_LANDSCAPE_HEIGHT = "last_landscape_touchable_height"
    }

    private lateinit var mLifecycleRegistry: LifecycleRegistry
    private lateinit var mViewModelStore: ViewModelStore
    private lateinit var mSavedStateRegistryController: SavedStateRegistryController



    fun getLatestPreferences(): androidx.datastore.preferences.core.Preferences? {
        return latestPreferences
    }

    fun setOwners(targetView: View? = null) {
        val decorView = window.window?.decorView
        if (decorView?.findViewTreeLifecycleOwner() == null) {
            decorView?.setViewTreeLifecycleOwner(this)
        }
        if (decorView?.findViewTreeViewModelStoreOwner() == null) {
            decorView?.setViewTreeViewModelStoreOwner(this)
        }
        if (decorView?.findViewTreeSavedStateRegistryOwner() == null) {
            decorView?.setViewTreeSavedStateRegistryOwner(this)
        }
        targetView?.let { view ->
            if (view.findViewTreeLifecycleOwner() == null) {
                view.setViewTreeLifecycleOwner(this)
            }
            if (view.findViewTreeViewModelStoreOwner() == null) {
                view.setViewTreeViewModelStoreOwner(this)
            }
            if (view.findViewTreeSavedStateRegistryOwner() == null) {
                view.setViewTreeSavedStateRegistryOwner(this)
            }
        }
    }

    val latinIMELegacy = LatinIMELegacy(
        this as InputMethodService,
        this as LatinIMELegacy.SuggestionStripController
    )

    val inputLogic get() = latinIMELegacy.mInputLogic

    lateinit var languageModelFacilitator: LanguageModelFacilitator

    val uixManager = UixManager(this)
    lateinit var suggestionBlacklist: SuggestionBlacklist

    private var activeThemeOption: ThemeOption? = null
    private var activeColorScheme = DarkColorScheme
    private var pendingRecreateKeyboard: Boolean = false
    var isStartingInputView: Boolean = false
        private set

    val themeOption get() = activeThemeOption
    val colorScheme get() = activeColorScheme
    val keyboardColor get() = drawableProvider?.primaryKeyboardColor?.let { androidx.compose.ui.graphics.Color(it) } ?: colorScheme.surface

    private var drawableProvider: DynamicThemeProvider? = null
    private var latestPreferences: androidx.datastore.preferences.core.Preferences? = null

    private var lastEditorInfo: EditorInfo? = null

    private var settingsRefreshRequired = false
    private fun recreateKeyboard() {
        if (latinIMELegacy.mKeyboardSwitcher.mainKeyboardView == null) {
            pendingRecreateKeyboard = true
            return
        }
        latinIMELegacy.loadSettings()
        latinIMELegacy.updateTheme()

        val editorInfo = currentInputEditorInfo ?: lastEditorInfo ?: EditorInfo()
        latinIMELegacy.mKeyboardSwitcher.mainKeyboardView?.setDrawableProvider(drawableProvider)
        if(settingsRefreshRequired || latinIMELegacy.mKeyboardSwitcher.keyboard == null) {
            latinIMELegacy.mKeyboardSwitcher.loadKeyboard(
                editorInfo,
                latinIMELegacy.mSettings.current,
                latinIMELegacy.currentAutoCapsState,
                latinIMELegacy.currentRecapitalizeState
            )
        } else {
            latinIMELegacy.mKeyboardSwitcher.mState.onLoadKeyboard(
                latinIMELegacy.currentAutoCapsState,
                latinIMELegacy.currentRecapitalizeState
            )
        }

        latinIMELegacy.mKeyboardSwitcher.mainKeyboardView?.setDrawableProvider(drawableProvider)
        settingsRefreshRequired = false
    }

    private var isNavigationBarVisible = false
    fun updateNavigationBarVisibility(visible: Boolean? = null) {
        if(visible != null) isNavigationBarVisible = visible

        val color = drawableProvider?.primaryKeyboardColor

        window.window?.let { window ->
            val shouldShow = if (visible == false) false else (isNavigationBarVisible || isInputViewShown)
            if(color == null || !shouldShow) {
                applyWindowColors(window, Color.TRANSPARENT, statusBar = false)
            } else {
                applyWindowColors(window, color, statusBar = false)
            }
        }
    }

    private fun updateDrawableProvider(
        colorScheme: ColorScheme,
        preferences: androidx.datastore.preferences.core.Preferences? = null,
        notifyUix: Boolean = true
    ) {
        activeColorScheme = colorScheme
        val provider = BasicThemeProvider(
            this,
            overrideColorScheme = colorScheme,
            themeKey = activeThemeOption?.key,
            preferences = preferences ?: latestPreferences
        )
        drawableProvider = provider
        latinIMELegacy.mKeyboardSwitcher.mainKeyboardView?.let {
            it.setDrawableProvider(provider)
            it.claimPointerTracker()
        }

        updateNavigationBarVisibility()
        if (notifyUix) {
            uixManager.onColorSchemeChanged()
        }
    }

    override fun getDrawableProvider(): DynamicThemeProvider {
        return drawableProvider ?: BasicThemeProvider(
            this,
            activeColorScheme,
            themeKey = activeThemeOption?.key,
            preferences = latestPreferences
        ).let {
            drawableProvider = it
            it
        }
    }

    private fun updateColorsIfDynamicChanged() {
        if(activeThemeOption?.dynamic == true) {
            val currColors = activeColorScheme
            val nextColors = activeThemeOption!!.obtainColors(this)

            if(currColors.differsFrom(nextColors)) {
                updateDrawableProvider(nextColors)
                recreateKeyboard()
            }
        }

        deferGetSetting(THEME_KEY) { key ->
            if(key != activeThemeOption?.key) {
                ThemeOptions[key]?.let { if(it.available(this)) updateTheme(it) }
            }
        }
    }

    private fun invalidateKeyboard(refreshSettings: Boolean = false) {
        settingsRefreshRequired = settingsRefreshRequired || refreshSettings

        if(!uixManager.isMainKeyboardHidden && latinIMELegacy.mKeyboardSwitcher.mainKeyboardView != null) {
            println("Recreating keyboard")
            recreateKeyboard()
        } else {
            println("Pend recreate keyboard")
            pendingRecreateKeyboard = true
        }
    }

    fun updateTheme(newTheme: ThemeOption) {
        assert(newTheme.available(this))

        if (activeThemeOption != newTheme) {
            activeThemeOption = newTheme
            updateDrawableProvider(newTheme.obtainColors(this))
            deferSetSetting(THEME_KEY, newTheme.key)
            invalidateKeyboard()
        }
    }

    // Called by UixManager when the intention is to subsequently call LegacyKeyboardView with hidden=false
    // Maybe this can be changed to LaunchedEffect
    fun onKeyboardShown() {
        if(pendingRecreateKeyboard) {
            pendingRecreateKeyboard = false
            recreateKeyboard()
        }
    }

    private var currentSubtype = ""

    val jobs = mutableListOf<Job>()
    private fun launchJob(task: suspend CoroutineScope.() -> Unit) {
        jobs.add(lifecycleScope.launch(block = task))
    }

    private fun stopJobs() {
        jobs.forEach { it.cancel() }
        jobs.clear()
    }

    private var unlockReceiver = UnlockedBroadcastReceiver { onDeviceUnlocked() }

    private fun getDeviceProtectedPrefs(name: String): android.content.SharedPreferences {
        val storageContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            createDeviceProtectedStorageContext()
        } else {
            this
        }
        return storageContext.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    override fun onCreate() {
        instance = this
        super.onCreate()

        val unlockFilter = IntentFilter(Intent.ACTION_USER_UNLOCKED)
        ContextCompat.registerReceiver(this, unlockReceiver, unlockFilter, ContextCompat.RECEIVER_EXPORTED)

        mLifecycleRegistry = LifecycleRegistry(this)
        mLifecycleRegistry.currentState = Lifecycle.State.INITIALIZED

        mViewModelStore = ViewModelStore()

        mSavedStateRegistryController = SavedStateRegistryController.create(this)
        mSavedStateRegistryController.performRestore(null)

        mLifecycleRegistry.currentState = Lifecycle.State.CREATED

        try {
            val insetsPrefs = getDeviceProtectedPrefs(PREFS_INSETS)
            lastPortraitHeight = insetsPrefs.getInt(PREF_LAST_PORTRAIT_HEIGHT, 0).takeIf { it > 0 }
            lastLandscapeHeight = insetsPrefs.getInt(PREF_LAST_LANDSCAPE_HEIGHT, 0).takeIf { it > 0 }
        } catch (t: Throwable) {
            Log.w("LatinIME", "Failed to load cached insets in onCreate", t)
        }
        lastOrientation = resources.configuration.orientation

        suggestionBlacklist = SuggestionBlacklist(latinIMELegacy.mSettings, this, lifecycleScope)

        Subtypes.addDefaultSubtypesIfNecessary(this)
        try {
            val prefs = PreferenceUtils.getDefaultSharedPreferences(this)
            Settings.upgradeAutocorrectionSettings(prefs, resources)
        } catch (t: Throwable) {
            Log.w("LatinIME", "Failed to sanitize autocorrection settings on onCreate", t)
        }
        try {
            if (isDirectBootUnlocked) {
                org.futo.inputmethod.updates.OrbitUpdateScheduler.schedulePeriodicCheck(applicationContext)
            }
        } catch (t: Throwable) {
            android.util.Log.w("LatinIME", "Failed to schedule periodic update check on onCreate", t)
        }

        languageModelFacilitator = LanguageModelFacilitator(
            this,
            latinIMELegacy.mInputLogic,
            latinIMELegacy.mDictionaryFacilitator,
            latinIMELegacy.mSettings,
            latinIMELegacy.mKeyboardSwitcher,
            lifecycleScope,
            suggestionBlacklist
        )

        try {
            val initialPrefs = runBlocking(Dispatchers.IO) {
                withTimeoutOrNull(500L) {
                    dataStore.data.first()
                }
            }
            latestPreferences = initialPrefs
            val themeKey = initialPrefs?.get(THEME_KEY.key) ?: THEME_KEY.default
            val themeOptionFromSettings = ThemeOptions[themeKey]
            val defaultTheme = ThemeOptions[THEME_KEY.default] ?: OrbitTheme
            val themeOption = when {
                themeOptionFromSettings == null -> defaultTheme
                !themeOptionFromSettings.available(this@LatinIME) -> defaultTheme
                else -> themeOptionFromSettings
            }

            activeThemeOption = themeOption
            activeColorScheme = themeOption.obtainColors(this@LatinIME)
            drawableProvider = BasicThemeProvider(
                this,
                overrideColorScheme = activeColorScheme,
                themeKey = themeOption.key,
                preferences = initialPrefs
            )
            KeyboardDisplayModeManager.init(this, initialPrefs)
        } catch (t: Throwable) {
            Log.w("LatinIME", "Failed to load initial preferences synchronously", t)
            val fallbackTheme = ThemeOptions[THEME_KEY.default] ?: OrbitTheme
            activeThemeOption = fallbackTheme
            activeColorScheme = fallbackTheme.obtainColors(this@LatinIME)
            drawableProvider = BasicThemeProvider(
                this,
                overrideColorScheme = activeColorScheme,
                themeKey = fallbackTheme.key,
                preferences = null
            )
            KeyboardDisplayModeManager.init(this)
        }

        latinIMELegacy.onCreate()

        languageModelFacilitator.launchProcessor()

        if(isDirectBootUnlocked) {
            languageModelFacilitator.loadHistoryLog()
        }

        scheduleUpdateCheckingJob(this)
        launchJob { uixManager.showUpdateNoticeIfNeeded() }

        suggestionBlacklist.init()

        launchJob {
            dataStore.data.collect { prefs ->
                latestPreferences = prefs
                withContext(Dispatchers.Main) {
                    val provider = drawableProvider
                    if (provider == null) {
                        val newColors = activeThemeOption?.obtainColors?.invoke(this@LatinIME) ?: activeColorScheme
                        updateDrawableProvider(newColors, preferences = prefs, notifyUix = false)
                    } else if (provider is BasicThemeProvider && provider.hasUpdated(prefs)) {
                        val requiresRecreate = prefs.get(HiddenKeysSetting) != provider.expertMode
                                || prefs.get(KeyboardHeightMultiplierSetting) != provider.keyboardHeight
                                || prefs.get(KeyboardFontSetting) != provider.keyboardFont

                        val newColors = activeThemeOption?.obtainColors?.invoke(this@LatinIME) ?: activeColorScheme
                        val colorSchemeChanged = (newColors != activeColorScheme)
                        updateDrawableProvider(newColors, preferences = prefs, notifyUix = colorSchemeChanged)

                        latinIMELegacy.mKeyboardSwitcher.mainKeyboardView?.let { mainKeyboardView ->
                            mainKeyboardView.setDrawableProvider(drawableProvider)
                            mainKeyboardView.invalidateAllKeys()
                            mainKeyboardView.postInvalidateOnAnimation()
                        }

                        if (requiresRecreate) {
                            latinIMELegacy.loadSettings()
                            invalidateKeyboard(refreshSettings = true)
                        }
                    }
                }
            }
        }

        launchJob {
            val onNewSubtype: suspend (String) -> Unit = {
                val activeSubtype = it.ifEmpty {
                    getSettingBlocking(SubtypesSetting).firstOrNull()
                }

                if(activeSubtype != null && activeSubtype != currentSubtype) {
                    currentSubtype = activeSubtype

                    withContext(Dispatchers.Main) {
                        changeInputMethodSubtype(Subtypes.convertToSubtype(activeSubtype))
                    }
                }
            }

            onNewSubtype(getSetting(ActiveSubtype))

            dataStore.data.collect {
                onNewSubtype(it[ActiveSubtype.key] ?: ActiveSubtype.default)
            }
        }

        launchJob {
            dataStore.data.collect {
                CrashLoggingApplication.logPreferences(it)
            }
        }

        uixManager.onCreate()

        Settings.getInstance().settingsChangedListeners.add { oldSettings, newSettings ->
            val differs = (oldSettings.mActionKeyId != newSettings.mActionKeyId)
                    || (oldSettings.mShowsActionKey != newSettings.mShowsActionKey)

            if (differs) {
                invalidateKeyboard(refreshSettings = true)
            }
        }
    }

    override fun onDestroy() {
        if (instance === this) {
            instance = null
        }
        unregisterReceiver(unlockReceiver)

        stopJobs()
        mLifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        viewModelStore.clear()

        languageModelFacilitator.saveHistoryLog()

        runBlocking {
            languageModelFacilitator.destroyModel()
        }

        Settings.getInstance().settingsChangedListeners.clear()

        latinIMELegacy.onDestroy()
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.w("LatinIME", "Configuration changed")
        latinIMELegacy.onConfigurationChanged(newConfig)
        super.onConfigurationChanged(newConfig)
        val isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
        lastOrientation = newConfig.orientation
        touchableHeight = if (isLandscape) (lastLandscapeHeight ?: 0) else (lastPortraitHeight ?: 0)
        if (KeyboardDisplayModeManager.currentMode == KeyboardDisplayMode.FLOATING) {
            floatingBounds = null
        }
        if (latinIMELegacy.mKeyboardSwitcher.hasThemeContext()) {
            reloadKeyboard()
        }
        window?.window?.decorView?.let { decor ->
            decor.post { decor.requestLayout() }
        }
    }

    override fun onInitializeInterface() {
        latinIMELegacy.onInitializeInterface()
    }

    private var legacyInputView: View? = null
    private var touchableHeight: Int = 0
    private var lastPortraitHeight: Int? = null
    private var lastLandscapeHeight: Int? = null
    private var lastOrientation: Int = Configuration.ORIENTATION_UNDEFINED
    var floatingBounds: Rect? = null

    fun updateFloatingBounds(left: Int, top: Int, right: Int, bottom: Int) {
        val current = floatingBounds
        if (current != null && current.left == left && current.top == top &&
            current.right == right && current.bottom == bottom) {
            return
        }
        floatingBounds = Rect(left, top, right, bottom)
        window?.window?.decorView?.let { decor ->
            decor.post { decor.requestLayout() }
        }
    }

    fun clearFloatingBounds() {
        floatingBounds = null
        window?.window?.decorView?.let { decor ->
            decor.post { decor.requestLayout() }
        }
    }

    override fun onCreateInputView(): View {
        Log.w("LatinIME", "Create input view")
        legacyInputView = latinIMELegacy.onCreateInputView()

        val composeView = uixManager.createComposeView()
        latinIMELegacy.setComposeInputView(composeView)

        return composeView
    }

    fun hasLegacyInputView(): Boolean {
        return legacyInputView != null
    }

    private var inputViewHeight: Int = -1

    // Both called by UixManager
    fun updateTouchableHeight(to: Int) {
        touchableHeight = to
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        lastOrientation = resources.configuration.orientation
        if (to > 0) {
            if (isLandscape) {
                lastLandscapeHeight = to
            } else {
                lastPortraitHeight = to
            }
            try {
                getDeviceProtectedPrefs(PREFS_INSETS)
                    .edit()
                    .putInt(if (isLandscape) PREF_LAST_LANDSCAPE_HEIGHT else PREF_LAST_PORTRAIT_HEIGHT, to)
                    .apply()
            } catch (t: Throwable) {
                Log.w("LatinIME", "Failed to cache insets", t)
            }
        }
        window?.window?.decorView?.let { decor ->
            decor.post { decor.requestLayout() }
        }
    }

    fun updateTouchableHeight() {
        if (touchableHeight > 0) {
            updateTouchableHeight(touchableHeight)
        } else {
            window?.window?.decorView?.let { decor ->
                decor.post { decor.requestLayout() }
            }
        }
    }

    fun getInputViewHeight(): Int {
        if (inputViewHeight > 0) return inputViewHeight
        if (touchableHeight > 0) return touchableHeight
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val cached = if (isLandscape) lastLandscapeHeight else lastPortraitHeight
        if (cached != null && cached > 0) return cached
        val density = resources.displayMetrics.density
        return if (isLandscape) (180f * density).toInt() else (260f * density).toInt()
    }

    fun reloadKeyboard() {
        if (!latinIMELegacy.mKeyboardSwitcher.hasThemeContext()) {
            Log.w("LatinIME", "reloadKeyboard skipped: mThemeContext is null")
            return
        }
        val editorInfo = currentInputEditorInfo ?: lastEditorInfo ?: EditorInfo()
        latinIMELegacy.mKeyboardSwitcher.loadKeyboard(
            editorInfo,
            latinIMELegacy.mSettings.current,
            latinIMELegacy.currentAutoCapsState,
            latinIMELegacy.currentRecapitalizeState
        )
        updateTouchableHeight()
        window?.window?.decorView?.let { decor ->
            decor.post { decor.requestLayout() }
        }
    }

    private var isInputModal = false
    fun setInputModal(to: Boolean) {
        isInputModal = to
        window?.window?.decorView?.let { decor ->
            decor.post { decor.requestLayout() }
        }
    }

    // The keyboard view really doesn't like being detached, so it's always
    // shown, but resized to 0 if an action window is open
    @Composable
    internal fun LegacyKeyboardView(hidden: Boolean) {
        val modifier = if(hidden) {
            Modifier
                .clipToBounds()
                .size(0.dp)
        } else {
            Modifier.onSizeChanged {
                inputViewHeight = it.height
            }
        }

        val padding = getSettingFlow(KeyboardBottomOffsetSetting).collectAsState(initial = 0.0f)

        key(legacyInputView) {
            AndroidView(factory = {
                legacyInputView?.parent?.let { parent ->
                    (parent as? android.view.ViewGroup)?.removeView(legacyInputView)
                }
                legacyInputView!!
            }, modifier = modifier.padding(0.dp, 0.dp, 0.dp, padding.value.dp))
        }
    }

    // necessary for when KeyboardSwitcher updates the theme
    fun updateLegacyView(newView: View) {
        Log.w("LatinIME", "Updating legacy view")
        legacyInputView = newView

        uixManager.setContent()
        uixManager.getComposeView()?.let {
            latinIMELegacy.setComposeInputView(it)
        }

        latinIMELegacy.setInputView(newView)
    }

    override fun setInputView(view: View?) {
        super.setInputView(view)

        uixManager.getComposeView()?.let {
            latinIMELegacy.setComposeInputView(it)
        }

        latinIMELegacy.setInputView(legacyInputView)
    }

    override fun setCandidatesView(view: View?) {
        return latinIMELegacy.setCandidatesView(view)
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        latinIMELegacy.onStartInput(attribute, restarting)
        languageModelFacilitator.onStartInput()
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        isStartingInputView = true
        try {
            mLifecycleRegistry.currentState = Lifecycle.State.STARTED

            lastEditorInfo = info

            val currentProvider = drawableProvider
            val prefs = latestPreferences
            if (currentProvider == null) {
                val newColors = activeThemeOption?.obtainColors?.invoke(this@LatinIME) ?: activeColorScheme
                updateDrawableProvider(newColors, preferences = prefs, notifyUix = (legacyInputView == null))
            } else if (currentProvider is BasicThemeProvider && prefs != null && currentProvider.hasUpdated(prefs)) {
                val newColors = activeThemeOption?.obtainColors?.invoke(this@LatinIME) ?: activeColorScheme
                updateDrawableProvider(newColors, preferences = prefs, notifyUix = false)
            }

            val editorInfo = info ?: currentInputEditorInfo ?: lastEditorInfo ?: EditorInfo()
            val inputTypeChanged = !latinIMELegacy.mSettings.current.isSameInputType(editorInfo)
            val isDifferentTextField = !restarting || inputTypeChanged

            if (pendingRecreateKeyboard) {
                pendingRecreateKeyboard = false
                latinIMELegacy.loadSettings()
                if (!isDifferentTextField) {
                    latinIMELegacy.mKeyboardSwitcher.loadKeyboard(
                        editorInfo,
                        latinIMELegacy.mSettings.current,
                        latinIMELegacy.currentAutoCapsState,
                        latinIMELegacy.currentRecapitalizeState
                    )
                }
                settingsRefreshRequired = false
            }

            super.onStartInputView(info, restarting)
            latinIMELegacy.onStartInputView(info, restarting)

            latinIMELegacy.mKeyboardSwitcher.mainKeyboardView?.let { mainKeyboardView ->
                mainKeyboardView.claimPointerTracker()
                mainKeyboardView.setDrawableProvider(drawableProvider)
                mainKeyboardView.invalidateAllKeys()
                mainKeyboardView.postInvalidateOnAnimation()
            }
            window?.window?.decorView?.let { decor -> decor.post { decor.requestLayout() } }
            lifecycleScope.launch { uixManager.showUpdateNoticeIfNeeded() }
        } finally {
            isStartingInputView = false
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        latinIMELegacy.onFinishInputView(finishingInput)
        uixManager.onInputFinishing()
    }

    override fun onFinishInput() {
        super.onFinishInput()
        latinIMELegacy.onFinishInput()

        uixManager.onInputFinishing()
        languageModelFacilitator.saveHistoryLog()
    }

    private fun changeInputMethodSubtype(newSubtype: InputMethodSubtype?) {
        latinIMELegacy.onCurrentInputMethodSubtypeChanged(newSubtype)
    }

    override fun onWindowShown() {
        super.onWindowShown()
        latinIMELegacy.onWindowShown()
        window?.window?.decorView?.let { decor -> decor.post { decor.requestLayout() } }

        updateColorsIfDynamicChanged()
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        latinIMELegacy.onWindowHidden()

        uixManager.onInputFinishing()
    }

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart,
            oldSelEnd,
            newSelStart,
            newSelEnd,
            candidatesStart,
            candidatesEnd
        )

        latinIMELegacy.onUpdateSelection(
            oldSelStart,
            oldSelEnd,
            newSelStart,
            newSelEnd,
            candidatesStart,
            candidatesEnd
        )
    }

    override fun onExtractedTextClicked() {
        latinIMELegacy.onExtractedTextClicked()
        super.onExtractedTextClicked()
    }

    override fun onExtractedCursorMovement(dx: Int, dy: Int) {
        latinIMELegacy.onExtractedCursorMovement(dx, dy)
        super.onExtractedCursorMovement(dx, dy)
    }

    override fun hideWindow() {
        latinIMELegacy.hideWindow()
        super.hideWindow()
    }

    override fun onDisplayCompletions(completions: Array<out CompletionInfo>?) {
        latinIMELegacy.onDisplayCompletions(completions)
    }

    internal fun getRealWindowHeight(): Int {
        val composeHeight = uixManager.getComposeView()?.takeIf { it.height > 0 }?.height
        if (composeHeight != null && composeHeight > 0) return composeHeight

        val decorHeight = window?.window?.decorView?.height ?: 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wm = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            val insets = wm?.currentWindowMetrics?.windowInsets
            val statusBarTop = insets?.getInsetsIgnoringVisibility(android.view.WindowInsets.Type.statusBars())?.top ?: 0
            if (decorHeight > 0) {
                val totalBoundsHeight = wm?.currentWindowMetrics?.bounds?.height() ?: 0
                return if (decorHeight >= totalBoundsHeight && statusBarTop > 0) {
                    decorHeight - statusBarTop
                } else {
                    decorHeight
                }
            }
            val bounds = wm?.currentWindowMetrics?.bounds
            if (bounds != null && bounds.height() > 0) {
                return (bounds.height() - statusBarTop).coerceAtLeast(0)
            }
        } else {
            if (decorHeight > 0) return decorHeight
            @Suppress("DEPRECATION")
            val wm = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            val display = wm?.defaultDisplay
            if (display != null) {
                val realMetrics = android.util.DisplayMetrics()
                @Suppress("DEPRECATION")
                display.getRealMetrics(realMetrics)
                if (realMetrics.heightPixels > 0) {
                    return realMetrics.heightPixels
                }
            }
        }
        return resources.displayMetrics.heightPixels
    }

    internal fun getRealWindowWidth(): Int {
        val decorWidth = window?.window?.decorView?.width ?: 0
        if (decorWidth > 0) return decorWidth

        val composeWidth = uixManager.getComposeView()?.width ?: 0
        if (composeWidth > 0) return composeWidth

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wm = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            val bounds = wm?.currentWindowMetrics?.bounds
            if (bounds != null && bounds.width() > 0) {
                return bounds.width()
            }
        } else {
            @Suppress("DEPRECATION")
            val wm = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            val display = wm?.defaultDisplay
            if (display != null) {
                val realMetrics = android.util.DisplayMetrics()
                @Suppress("DEPRECATION")
                display.getRealMetrics(realMetrics)
                if (realMetrics.widthPixels > 0) {
                    return realMetrics.widthPixels
                }
            }
        }
        return resources.displayMetrics.widthPixels
    }

    override fun onComputeInsets(outInsets: Insets?) {
        if (outInsets == null) return
        val composeView = uixManager.getComposeView()

        val inputHeight: Int = composeView?.height ?: 0
        if (latinIMELegacy.isImeSuppressedByHardwareKeyboard && legacyInputView != null && !legacyInputView!!.isShown) {
            outInsets.apply {
                contentTopInsets = inputHeight
                visibleTopInsets = inputHeight
            }
            try { latinIMELegacy.setInsets(outInsets) } catch (_: Throwable) {}
            return
        }

        val currentOrientation = resources.configuration.orientation
        val isLandscape = currentOrientation == Configuration.ORIENTATION_LANDSCAPE
        val composeHeight = composeView?.takeIf { it.height > 0 }?.height
        val realWindowHeight = composeHeight ?: getRealWindowHeight()
        val realWindowWidth = getRealWindowWidth()
        val windowHeight = realWindowHeight

        // Check for Floating Mode
        if (KeyboardDisplayModeManager.currentMode == KeyboardDisplayMode.FLOATING && !isInputModal) {
            val bounds = floatingBounds
            if (bounds != null) {
                outInsets.apply {
                    touchableInsets = Insets.TOUCHABLE_INSETS_REGION
                    touchableRegion.set(bounds.left, bounds.top, bounds.right, bounds.bottom)
                    contentTopInsets = windowHeight
                    visibleTopInsets = windowHeight
                }
                try { latinIMELegacy.setInsets(outInsets) } catch (_: Throwable) {}
                return
            } else {
                // Frame 0 cold start fallback for floating mode
                val cardWidth = (realWindowWidth * KeyboardDisplayModeManager.getWidthMultiplier(isLandscape)).toInt()
                val cachedH = if (isLandscape) lastLandscapeHeight else lastPortraitHeight
                val cardHeight = cachedH ?: (windowHeight * if (isLandscape) 0.40f else 0.42f).toInt()
                val defaultX = (realWindowWidth - cardWidth) / 2
                val defaultY = (windowHeight - cardHeight - 48).coerceAtLeast(0)
                outInsets.apply {
                    touchableInsets = Insets.TOUCHABLE_INSETS_REGION
                    touchableRegion.set(defaultX, defaultY, defaultX + cardWidth, defaultY + cardHeight)
                    contentTopInsets = windowHeight
                    visibleTopInsets = windowHeight
                }
                try { latinIMELegacy.setInsets(outInsets) } catch (_: Throwable) {}
                return
            }
        }

        val cachedHeight = if (isLandscape) lastLandscapeHeight else lastPortraitHeight
        val defaultFraction = if (isLandscape) 0.42f else 0.42f
        val fallbackHeight = cachedHeight ?: (windowHeight * defaultFraction).toInt()

        val currentOrientationHeight = if (lastOrientation == currentOrientation && touchableHeight > 0) {
            touchableHeight
        } else {
            cachedHeight ?: 0
        }

        val effectiveTouchableHeight = if (currentOrientationHeight > 0) {
            currentOrientationHeight
        } else {
            fallbackHeight
        }.coerceAtMost(windowHeight)

        val visibleTopY = (windowHeight - effectiveTouchableHeight).coerceAtLeast(0)

        val touchLeft = 0
        val touchTop = if (isInputModal) { 0 } else { visibleTopY }
        val touchRight = window?.window?.decorView?.takeIf { it.width > 0 }?.width ?: composeView?.takeIf { it.width > 0 }?.width ?: realWindowWidth
        val touchBottom = windowHeight
        val contentTop = visibleTopY

        outInsets.apply {
            touchableInsets = Insets.TOUCHABLE_INSETS_CONTENT
            touchableRegion.set(touchLeft, touchTop, touchRight, touchBottom)
            contentTopInsets = contentTop
            visibleTopInsets = contentTop
        }
        try {
            latinIMELegacy.setInsets(outInsets)
        } catch (_: Throwable) {}
    }

    override fun onShowInputRequested(flags: Int, configChange: Boolean): Boolean {
        return true
    }

    override fun onEvaluateInputViewShown(): Boolean {
        return true
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        // TODO: Revisit fullscreen mode
        return false //latinIMELegacy.onEvaluateFullscreenMode(super.onEvaluateFullscreenMode())
    }

    override fun updateFullscreenMode() {
        super.updateFullscreenMode()
        latinIMELegacy.updateFullscreenMode()
    }

    private var isConsumingBack = false

    private fun isVolumeCursorEnabled(): Boolean {
        return latinIMELegacy.mSettings?.current?.mVolumeKeyCursor
            ?: PreferenceUtils.getDefaultSharedPreferences(this).getBoolean(Settings.PREF_VOLUME_KEY_CURSOR, false)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && (event == null || event.repeatCount == 0)) {
            if (uixManager.consumeBackKey()) {
                isConsumingBack = true
                event?.startTracking()
                return true
            }
        }
        if (isInputViewShown && isVolumeCursorEnabled()) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT)
                return true
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT)
                return true
            }
        }
        return latinIMELegacy.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isConsumingBack) {
                isConsumingBack = false
                return true
            }
        }
        if (isInputViewShown && isVolumeCursorEnabled()) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                return true
            }
        }
        return latinIMELegacy.onKeyUp(keyCode, event) || super.onKeyUp(keyCode, event)
    }

    override fun updateVisibility(shouldShowSuggestionsStrip: Boolean, fullscreenMode: Boolean) {
        uixManager.updateVisibility(shouldShowSuggestionsStrip, fullscreenMode)
    }

    override fun setSuggestions(suggestedWords: SuggestedWords?, rtlSubtype: Boolean) {
        uixManager.setSuggestions(suggestedWords, rtlSubtype)
    }

    override fun maybeShowImportantNoticeTitle(): Boolean {
        return false
    }

    override fun onLowMemory() {
        super.onLowMemory()
        uixManager.cleanUpPersistentStates()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        uixManager.cleanUpPersistentStates()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateInlineSuggestionsRequest(uiExtras: Bundle): InlineSuggestionsRequest {
        return createInlineSuggestionsRequest(this, this.activeColorScheme)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onInlineSuggestionsResponse(response: InlineSuggestionsResponse): Boolean {
        return uixManager.onInlineSuggestionsResponse(response)
    }

    fun postUpdateSuggestionStrip(inputStyle: Int): Boolean {
        if (latinIMELegacy.mInputLogic.isAvroActive() || languageModelFacilitator.shouldPassThroughToLegacy()) return false

        languageModelFacilitator.updateSuggestionStripAsync(inputStyle);
        return true
    }

    fun requestForgetWord(suggestedWordInfo: SuggestedWordInfo) {
        uixManager.requestForgetWord(suggestedWordInfo)
    }

    fun refreshSuggestions() {
        latinIMELegacy.mInputLogic.performUpdateSuggestionStripSync(latinIMELegacy.mSettings.current, SuggestedWords.INPUT_STYLE_TYPING)
    }

    fun forceForgetWord(suggestedWordInfo: SuggestedWordInfo) {
        lifecycleScope.launch {
            val existingWords = getSetting(SUGGESTION_BLACKLIST).toMutableSet()
            existingWords.add(suggestedWordInfo.mWord)
            setSetting(SUGGESTION_BLACKLIST, existingWords)
        }

        latinIMELegacy.mDictionaryFacilitator.unlearnFromUserHistory(
            suggestedWordInfo.mWord, NgramContext.EMPTY_PREV_WORDS_INFO,
            -1, Constants.NOT_A_CODE
        )

        refreshSuggestions()
    }

    fun rememberEmojiSuggestion(suggestion: SuggestedWordInfo) {
        if(suggestion.mKindAndFlags == SuggestedWordInfo.KIND_EMOJI_SUGGESTION) {
            lifecycleScope.launch {
                withContext(Dispatchers.Default) {
                    useEmoji(suggestion.mWord)
                }
            }
        }
    }

    fun onEmojiDeleted(emoji: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.Default) {
                unuseEmoji(emoji)
            }
        }
    }

    private var overrideInputConnection: InputConnection? = null
    private var overrideEditorInfo: EditorInfo? = null
    fun overrideInputConnection(to: InputConnection?, editorInfo: EditorInfo?) {
        this.overrideInputConnection = to
        this.overrideEditorInfo = editorInfo

        latinIMELegacy.loadSettings()

        inputLogic.finishInput()
        inputLogic.startInput(RichInputMethodManager.getInstance().combiningRulesExtraValueOfCurrentSubtype, latinIMELegacy.mSettings.current)

        val currentIC = currentInputConnection
        currentIC?.requestCursorUpdates(InputConnection.CURSOR_UPDATE_IMMEDIATE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            super.getCurrentInputConnection()?.setImeConsumesInput(to != null)
        }
    }

    val isInputConnectionOverridden
        get() = overrideInputConnection != null

    override fun getCurrentInputConnection(): InputConnection? {
        return overrideInputConnection ?: super.getCurrentInputConnection()
    }

    override fun getCurrentInputEditorInfo(): EditorInfo? {
        return overrideEditorInfo ?: super.getCurrentInputEditorInfo()
    }

    fun getBaseInputConnection(): InputConnection? {
        return super.getCurrentInputConnection()
    }

    override val lifecycle: Lifecycle
        get() = mLifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry
        get() = mSavedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore
        get() = mViewModelStore


    private fun onDeviceUnlocked() {
        Log.i("LatinIME", "DEVICE has UNLOCKED!!! Reloading settings...")
        // Every place that called getDefaultSharedPreferences now needs to be refreshed or call it again

        // Mainly Settings singleton needs to be refreshed
        Settings.init(applicationContext)
        Settings.getInstance().onSharedPreferenceChanged(null /* unused */, "")
        KeyboardDisplayModeManager.init(this)
        latinIMELegacy.loadSettings()
        recreateKeyboard()

        languageModelFacilitator.loadHistoryLog()

        uixManager.onPersistentStatesUnlocked()
        try {
            org.futo.inputmethod.updates.OrbitUpdateScheduler.schedulePeriodicCheck(applicationContext)
        } catch (t: Throwable) {
            android.util.Log.w("LatinIME", "Failed to schedule periodic update check on unlock", t)
        }

        // TODO: Spell checker service
    }
}
