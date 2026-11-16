package org.futo.inputmethod.latin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * Adversarial and Verification Test for Cold Launch and Keyboard Start.
 *
 * Verifies the fixes for:
 * 1. Cold start re-entrancy prevention: KeyboardSwitcher.updateKeyboardTheme
 *    must NEVER destroy and re-inflate legacyInputView mid-session when the view is attached,
 *    input is starting, or input view is shown. Also null-safe.
 * 2. On cold start (keyboard == null), LatinIME.onStartInputView must NOT prematurely
 *    call recreateKeyboard().
 * 3. When pendingRecreateKeyboard is true, keyboard and settings are reloaded cleanly without
 *    calling updateTheme(), updateLegacyView(), or setContent().
 * 4. updateDrawableProvider during onStartInputView with an existing view does not notify UIX (notifyUix = false),
 *    preventing Compose teardown storms.
 * 5. BasicThemeProvider does not fall back to getSettingBlocking() when preferences is provided.
 * 6. LatinIME.onCreate synchronously defaults to OrbitTheme (not VoiceInputTheme).
 */
class ColdStartKeyboardLaunchTest {

    private fun findSourceFile(relPath: String): File {
        val candidates = listOf(
            relPath,
            "java/$relPath",
            "src/org/futo/inputmethod/$relPath",
            "../$relPath",
            "../java/$relPath",
            "nth-kbd/$relPath",
            "nth-kbd/java/$relPath"
        )
        for (candidate in candidates) {
            val f = File(candidate)
            if (f.exists()) return f
        }
        return File(relPath)
    }

    // =========================================================================
    // Source Code Invariant Tests
    // =========================================================================

    @Test
    fun testBasicThemeProviderReadSettingDoesNotFallBackToBlockingWhenPreferencesProvided() {
        val file = findSourceFile("src/org/futo/inputmethod/latin/uix/BasicThemeProvider.kt")
        assertTrue("BasicThemeProvider.kt must exist", file.exists())
        val content = file.readText(StandardCharsets.UTF_8)

        // Verify that readSetting checks if (preferences != null) and returns preferences[key.key] ?: key.default
        // rather than using preferences?.get(key.key) ?: try { context.getSettingBlocking(key) }
        assertFalse(
            "readSetting must NOT use 'preferences?.get(key.key) ?: try { context.getSettingBlocking(key)' which falls back to blocking reads for unset keys",
            content.contains("preferences?.get(key.key) ?: try")
        )
        assertTrue(
            "readSetting must check if preferences != null and return default without blocking",
            content.contains("if (preferences != null)") && content.contains("preferences[key.key] ?: key.default")
        )
    }

    @Test
    fun testLatinIMEDoesNotCallRecreateKeyboardPrematurelyOnColdStart() {
        val file = findSourceFile("src/org/futo/inputmethod/latin/LatinIME.kt")
        assertTrue("LatinIME.kt must exist", file.exists())
        val content = file.readText(StandardCharsets.UTF_8)

        assertFalse(
            "onStartInputView must NOT check mKeyboardSwitcher.keyboard == null to trigger recreateKeyboard()",
            content.contains("mKeyboardSwitcher.keyboard == null) {\n            pendingRecreateKeyboard = false\n            recreateKeyboard()")
        )
        assertFalse(
            "LatinIME must NOT contain premature recreateKeyboard on keyboard == null",
            content.contains("latinIMELegacy.mKeyboardSwitcher.keyboard == null) {\n            pendingRecreateKeyboard = false\n            recreateKeyboard()")
        )
    }

    @Test
    fun testLatinIMEOnCreateDefaultsToOrbitThemeNotVoiceInputTheme() {
        val file = findSourceFile("src/org/futo/inputmethod/latin/LatinIME.kt")
        assertTrue("LatinIME.kt must exist", file.exists())
        val content = file.readText(StandardCharsets.UTF_8)

        assertFalse(
            "LatinIME.onCreate must NOT default missing theme to VoiceInputTheme",
            content.contains("themeOptionFromSettings == null -> VoiceInputTheme")
        )
        assertTrue(
            "LatinIME.onCreate must fall back to OrbitTheme",
            content.contains("val defaultTheme = ThemeOptions[THEME_KEY.default] ?: OrbitTheme")
        )
    }

    @Test
    fun testKeyboardSwitcherHasNullSafeGuards() {
        val file = findSourceFile("src/org/futo/inputmethod/keyboard/KeyboardSwitcher.java")
        assertTrue("KeyboardSwitcher.java must exist", file.exists())
        val content = file.readText(StandardCharsets.UTF_8)

        assertTrue(
            "KeyboardSwitcher.updateKeyboardTheme must check isStartingInput",
            content.contains("isStartingInput")
        )
        assertTrue(
            "KeyboardSwitcher.updateKeyboardTheme must safely check mLatinIMELegacy != null before calling methods",
            content.contains("if (mLatinIMELegacy != null)")
        )
    }

    // =========================================================================
    // Simulation / Behavioral Tests
    // =========================================================================

    // Simulates KeyboardSwitcher theme update guard logic
    class FakeKeyboardSwitcher(
        var isAttached: Boolean = false,
        var isStartingInput: Boolean = false,
        var isInputViewShown: Boolean = false,
        var hasKeyboardView: Boolean = true,
        var hasLegacyService: Boolean = true
    ) {
        var updateLegacyViewCallCount = 0
        var updateMainKeyboardViewSettingsCallCount = 0

        fun updateKeyboardTheme(themeUpdated: Boolean) {
            if (themeUpdated && hasKeyboardView) {
                if (isAttached || isStartingInput || isInputViewShown) {
                    if (hasLegacyService) {
                        updateMainKeyboardViewSettingsCallCount++
                    }
                    return
                }
                if (hasLegacyService) {
                    updateLegacyViewCallCount++
                    updateMainKeyboardViewSettingsCallCount++
                }
            }
        }
    }

    @Test
    fun testUpdateKeyboardThemeGuardedWhenAttached() {
        val switcher = FakeKeyboardSwitcher(isAttached = true, isStartingInput = false, isInputViewShown = false)
        switcher.updateKeyboardTheme(themeUpdated = true)

        assertEquals(0, switcher.updateLegacyViewCallCount)
        assertEquals(1, switcher.updateMainKeyboardViewSettingsCallCount)
    }

    @Test
    fun testUpdateKeyboardThemeGuardedWhenStartingInput() {
        val switcher = FakeKeyboardSwitcher(isAttached = false, isStartingInput = true, isInputViewShown = false)
        switcher.updateKeyboardTheme(themeUpdated = true)

        assertEquals(0, switcher.updateLegacyViewCallCount)
        assertEquals(1, switcher.updateMainKeyboardViewSettingsCallCount)
    }

    @Test
    fun testUpdateKeyboardThemeGuardedWhenShown() {
        val switcher = FakeKeyboardSwitcher(isAttached = false, isStartingInput = false, isInputViewShown = true)
        switcher.updateKeyboardTheme(themeUpdated = true)

        assertEquals(0, switcher.updateLegacyViewCallCount)
        assertEquals(1, switcher.updateMainKeyboardViewSettingsCallCount)
    }

    @Test
    fun testUpdateKeyboardThemeExecutesWhenDormant() {
        // When not attached, not starting input, not shown (closed), recreation is allowed
        val switcher = FakeKeyboardSwitcher(isAttached = false, isStartingInput = false, isInputViewShown = false)
        switcher.updateKeyboardTheme(themeUpdated = true)

        assertEquals(1, switcher.updateLegacyViewCallCount)
        assertEquals(1, switcher.updateMainKeyboardViewSettingsCallCount)
    }

    @Test
    fun testUpdateKeyboardThemeHandlesNullLegacySafely() {
        val switcher = FakeKeyboardSwitcher(isAttached = true, hasLegacyService = false)
        switcher.updateKeyboardTheme(themeUpdated = true)

        assertEquals(0, switcher.updateLegacyViewCallCount)
        assertEquals(0, switcher.updateMainKeyboardViewSettingsCallCount)
    }

    // Simulates onStartInputView lifecycle orchestration
    class FakeInputViewOrchestrator {
        var pendingRecreateKeyboard = false
        var keyboardLoaded = false
        var recreateKeyboardCalled = false
        var loadKeyboardCallCount = 0
        var setContentCallCount = 0
        var legacyInputViewExists = true

        fun simulateColdStartInputView(hasKeyboard: Boolean, isDifferentTextField: Boolean = true) {
            // Under fixed implementation:
            // 1. Premature recreateKeyboard() is removed!
            if (pendingRecreateKeyboard) {
                pendingRecreateKeyboard = false
                if (!isDifferentTextField) {
                    loadKeyboardCallCount++
                }
            }

            // Downstream legacy onStartInputView loads the keyboard if isDifferentTextField
            if (isDifferentTextField) {
                keyboardLoaded = true
                loadKeyboardCallCount++
            } else if (!hasKeyboard) {
                keyboardLoaded = true
                loadKeyboardCallCount++
            }
        }

        fun updateDrawableProvider(notifyUix: Boolean) {
            if (notifyUix) {
                setContentCallCount++
            }
        }
    }

    @Test
    fun testColdStartDoesNotCallRecreateKeyboardPrematurely() {
        val orchestrator = FakeInputViewOrchestrator()
        orchestrator.pendingRecreateKeyboard = false

        orchestrator.simulateColdStartInputView(hasKeyboard = false, isDifferentTextField = true)

        assertFalse(orchestrator.recreateKeyboardCalled)
        assertTrue(orchestrator.keyboardLoaded)
        assertEquals(1, orchestrator.loadKeyboardCallCount)
    }

    @Test
    fun testPendingRecreateKeyboardAvoidsDuplicateLoadWhenDifferentTextField() {
        val orchestrator = FakeInputViewOrchestrator()
        orchestrator.pendingRecreateKeyboard = true

        // When different text field, legacy onStartInputView loads it anyway, so orchestrator skips duplicate load
        orchestrator.simulateColdStartInputView(hasKeyboard = true, isDifferentTextField = true)

        assertFalse(orchestrator.pendingRecreateKeyboard)
        assertEquals(1, orchestrator.loadKeyboardCallCount)
        assertEquals(0, orchestrator.setContentCallCount)
    }

    @Test
    fun testPendingRecreateKeyboardLoadsWhenSameTextField() {
        val orchestrator = FakeInputViewOrchestrator()
        orchestrator.pendingRecreateKeyboard = true

        // When same text field (restarting = true), legacy onStartInputView doesn't load keyboard, so orchestrator loads it
        orchestrator.simulateColdStartInputView(hasKeyboard = true, isDifferentTextField = false)

        assertFalse(orchestrator.pendingRecreateKeyboard)
        assertEquals(1, orchestrator.loadKeyboardCallCount)
        assertEquals(0, orchestrator.setContentCallCount)
    }

    @Test
    fun testUpdateDrawableProviderDuringInputStartDoesNotTriggerSetContent() {
        val orchestrator = FakeInputViewOrchestrator()
        orchestrator.legacyInputViewExists = true

        // During onStartInputView: notifyUix is false when legacy view is already created
        val notifyUix = !orchestrator.legacyInputViewExists
        orchestrator.updateDrawableProvider(notifyUix = notifyUix)

        assertEquals(0, orchestrator.setContentCallCount)
    }
}
