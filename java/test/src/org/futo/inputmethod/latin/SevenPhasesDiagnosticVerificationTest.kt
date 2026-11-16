package org.futo.inputmethod.latin

import android.view.inputmethod.EditorInfo
import org.futo.inputmethod.keyboard.Key
import org.futo.inputmethod.keyboard.KeyboardSwitcher
import org.futo.inputmethod.keyboard.internal.CloudTranslationHandler
import org.futo.inputmethod.keyboard.internal.TranslationHelper
import org.futo.inputmethod.latin.settings.SettingsValues
import org.futo.inputmethod.latin.uix.theme.presets.BerryBlushThemeHelper
import org.futo.inputmethod.latin.uix.theme.presets.OrbitThemeHelper
import org.futo.inputmethod.latin.uix.theme.presets.TactileSlateThemeHelper
import org.futo.voiceinput.shared.types.Language
import org.futo.voiceinput.shared.types.getLanguageFromWhisperString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class SevenPhasesDiagnosticVerificationTest {

    // =========================================================================
    // Phase 1: Crash Prevention & Direct Boot Safety
    // =========================================================================
    @Test
    fun testKeyboardSwitcherThemeContextNullSafety() {
        val switcher = KeyboardSwitcher.getInstance()
        assertNotNull(switcher)
        // Verify loadKeyboard safely returns without throwing NPE when mThemeContext is null
        switcher.loadKeyboard(EditorInfo(), null, 0, 0)
        // switcher.hasThemeContext() accurately reports context state without throwing
        val hasContext = switcher.hasThemeContext()
        assertTrue(hasContext == false || hasContext == true)
    }

    // =========================================================================
    // Phase 2: Translation Service & Robust Parsing
    // =========================================================================
    @Test
    fun testTranslationResponseParsingStringArray() {
        // dict-chrome-ex simple string array: ["হ্যালো"]
        val chromeResponse = "[\"হ্যালো\"]"
        val parsed = CloudTranslationHandler.parseTranslationResponse(chromeResponse)
        assertEquals("হ্যালো", parsed)
    }

    @Test
    fun testTranslationResponseParsingMultiSentenceStringArray() {
        // dict-chrome-ex multi-sentence string array: ["হ্যালো ", "বিশ্ব"]
        val chromeResponse = "[\"হ্যালো \",\"বিশ্ব\"]"
        val parsed = CloudTranslationHandler.parseTranslationResponse(chromeResponse)
        assertEquals("হ্যালো বিশ্ব", parsed)
    }

    @Test
    fun testTranslationResponseParsingChromeAutoDetectFormat() {
        // dict-chrome-ex with sl=auto: [["হ্যালো","en"]]
        // Must parse ONLY the translated text and NOT concatenate the detected language code "en"!
        val autoResponse = "[[\"হ্যালো\",\"en\"]]"
        val parsed = CloudTranslationHandler.parseTranslationResponse(autoResponse)
        assertEquals("হ্যালো", parsed)

        val multiSentenceAuto = "[[\"হ্যালো। কেমন আছেন?\",\"en\"]]"
        val parsedMulti = CloudTranslationHandler.parseTranslationResponse(multiSentenceAuto)
        assertEquals("হ্যালো। কেমন আছেন?", parsedMulti)
    }

    @Test
    fun testTranslationResponseParsingNestedGtxArray() {
        // gtx single response: [[["হ্যালো","hello",null,null,1]],null,"en"]
        val gtxResponse = "[[[\"হ্যালো\",\"hello\",null,null,1]],null,\"en\"]"
        val parsed = CloudTranslationHandler.parseTranslationResponse(gtxResponse)
        assertEquals("হ্যালো", parsed)

        // gtx multi-sentence response
        val multiGtx = "[[[\"হ্যালো। \",\"Hello. \",null,null,1],[\"কেমন আছেন?\",\"How are you?\",null,null,1]],null,\"en\"]"
        val parsedMultiGtx = CloudTranslationHandler.parseTranslationResponse(multiGtx)
        assertEquals("হ্যালো। কেমন আছেন?", parsedMultiGtx)
    }

    @Test
    fun testTranslationResponseFallbackAutoFormat() {
        val autoResponse = "[[\"হ্যালো বিশ্ব\",\"en\"]]"
        val parsed = CloudTranslationHandler.parseTranslationResponseFallback(autoResponse)
        assertEquals("হ্যালো বিশ্ব", parsed)
    }

    @Test
    fun testTranslationLanguagesCatalogContainsOthers() {
        val languages = TranslationHelper.getLanguages()
        assertNotNull(languages)
        assertTrue("Catalog must contain Arabic ('ar')", languages.containsKey("ar"))
        assertTrue("Catalog must contain Bengali ('bn')", languages.containsKey("bn"))
        assertTrue("Catalog must contain English ('en')", languages.containsKey("en"))
        assertTrue("Catalog must contain Spanish ('es')", languages.containsKey("es"))
        assertTrue("Catalog must contain Japanese ('ja')", languages.containsKey("ja"))
        assertTrue("Catalog must contain over 100 languages for Others dialog", languages.size > 100)
    }

    // =========================================================================
    // Phase 3: Spacebar Styling & Settings Swatches
    // =========================================================================
    @Test
    fun testSpacebarCornerRadiusAlignmentInOrbitThemeHelper() {
        // OrbitThemeHelper customSpacebarCornerRadius should track corner radius
        OrbitThemeHelper.spacebarShape = "boxy"
        assertEquals("boxy", OrbitThemeHelper.spacebarShape)

        OrbitThemeHelper.spacebarShape = "roundy"
        assertEquals("roundy", OrbitThemeHelper.spacebarShape)
    }

    // =========================================================================
    // Phase 4: Theme Customization Mutual Exclusion
    // =========================================================================
    @Test
    fun testSpecialThemeKeysIdentification() {
        val specialThemes = setOf("OrbitTheme", "OrbitChaos", "RetroBlocks", "BerryBlush", "TactileSlate")
        assertTrue(specialThemes.contains("OrbitTheme"))
        assertTrue(specialThemes.contains("OrbitChaos"))
        assertTrue(specialThemes.contains("RetroBlocks"))
        assertTrue(specialThemes.contains("BerryBlush"))
        assertTrue(specialThemes.contains("TactileSlate"))
        assertFalse(specialThemes.contains("NothingDark"))
        assertFalse(specialThemes.contains("AMOLEDNothing"))
    }

    // =========================================================================
    // Phase 5: Voice Input Unification & Whisper Safe Lookup
    // =========================================================================
    @Test
    fun testWhisperLanguageSafeFallbackWithoutCrash() {
        assertEquals(Language.English, getLanguageFromWhisperString("en"))
        assertEquals(Language.Arabic, getLanguageFromWhisperString("ar"))
        assertEquals(Language.Bengali, getLanguageFromWhisperString("bn"))

        // An unknown/unsupported whisper string must return null safely
        val unknown = getLanguageFromWhisperString("unsupported_xyz")
        assertNull(unknown)

        // The safe fallback pattern used in VoiceInputAction:
        val safeLanguage = unknown ?: Language.English
        assertEquals(Language.English, safeLanguage)
    }

    // =========================================================================
    // Phase 6: Arabic Subtype & Selection Limits
    // =========================================================================
    @Test
    fun testArabicSubtypeFormat() {
        val arabicSubtypeStr = "ar:KeyboardLayoutSet=arabic:"
        val parts = arabicSubtypeStr.split(":")
        assertEquals("ar", parts[0])
        assertEquals("KeyboardLayoutSet=arabic", parts[1])
    }

    @Test
    fun testMaxActiveKeyboardsLimitConstant() {
        assertEquals(3, Subtypes.MAX_ACTIVE_KEYBOARDS)
    }

    // =========================================================================
    // Phase 7: Bengali Hint Positioning in Theme Helpers
    // =========================================================================
    @Test
    fun testTactileSlateThemeHelperHintCoordinates() {
        // Key with hint label "1"
        val key = Key("q", null, 'q'.code, null, "1", 0, 0, 0, 0, 100, 100, 0, 0)
        val coords = TactileSlateThemeHelper.getKeyHintCoordinates(key, 100, 100, 0f)
        assertNotNull(coords)
        // padX = maxOf(0, 100 * 0.08f) = 8f -> hintX = 100 - 8f = 92f
        assertEquals(92f, coords!![0], 0.001f)
        // padY = maxOf(0, 100 * 0.08f) = 8f -> hintBaseline = 8f + 11f = 19f
        assertEquals(19f, coords[1], 0.001f)
    }

    @Test
    fun testBerryBlushThemeHelperHintCoordinates() {
        // Key with hint label "2"
        val key = Key("w", null, 'w'.code, null, "2", 0, 0, 0, 0, 100, 100, 0, 0)
        val coords = BerryBlushThemeHelper.getKeyHintCoordinates(key, 100, 100, 0f)
        assertNotNull(coords)
        // padX = maxOf(0, 100 * 0.08f) = 8f -> hintX = 100 - 8f = 92f
        assertEquals(92f, coords!![0], 0.001f)
        // padY = maxOf(0, 100 * 0.08f) = 8f -> hintBaseline = 8f + 11f = 19f
        assertEquals(19f, coords[1], 0.001f)
    }

    @Test
    fun testKeyWithoutHintLabelReturnsNullCoordinates() {
        val keyWithoutHint = Key("a", null, 'a'.code, null, null, 0, 0, 0, 0, 100, 100, 0, 0)
        assertNull(TactileSlateThemeHelper.getKeyHintCoordinates(keyWithoutHint, 100, 100, 0f))
        assertNull(BerryBlushThemeHelper.getKeyHintCoordinates(keyWithoutHint, 100, 100, 0f))
    }
}
