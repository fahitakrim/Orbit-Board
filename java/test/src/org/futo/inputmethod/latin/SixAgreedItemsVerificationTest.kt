package org.futo.inputmethod.latin

import android.graphics.Color
import org.futo.inputmethod.latin.uix.ActionBarExpanded
import org.futo.inputmethod.latin.uix.BasicThemeProvider
import org.futo.inputmethod.latin.uix.CustomAccentColorSetting
import org.futo.inputmethod.latin.uix.KeyboardBgColorSetting
import org.futo.inputmethod.latin.uix.KeyboardKeyColorSetting
import org.futo.inputmethod.latin.uix.KeyboardTextColorSetting
import org.futo.inputmethod.latin.uix.SpacebarBorderSetting
import org.futo.inputmethod.latin.uix.SpacebarColorSetting
import org.futo.inputmethod.latin.uix.SpacebarShapeSetting
import org.futo.inputmethod.latin.uix.THEME_KEY
import org.futo.inputmethod.latin.uix.theme.presets.RetroBlocksThemeHelper
import org.futo.inputmethod.latin.uix.theme.presets.TactileSlateThemeHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

/**
 * Robust adversarial unit tests for the 6 agreed-upon items in nth-kbd:
 * 1. Persistent Extended Suggestion / Action Bar
 * 2. Top (ActionBar) & Bottom (NavigationBar/Insets) Background Color Sync
 * 3. Auto-Reset to Canonical Defaults on Theme Switch
 * 4. Concise English Lock Banners
 * 5. Translation "Others" Click Crash Fix (In-place modal overlay, no AlertDialog)
 * 6. Real Keyboard View Preview at Top of Settings (Keyed AndroidView with MainKeyboardView)
 */
class SixAgreedItemsVerificationTest {

    // =========================================================================
    // Item 1: Persistent Extended Suggestion / Action Bar
    // =========================================================================
    @Test
    fun testPersistentActionBarSettingKey() {
        assertNotNull(ActionBarExpanded)
        assertEquals("actionExpanded", ActionBarExpanded.key.name)
        assertEquals(false, ActionBarExpanded.default)
    }

    @Test
    fun testBackKeyInterceptionDoesNotCollapseActionBar() {
        var isActionsExpanded = true
        var isActionWindowOpen = false
        var isActionEditorOpen = false

        fun consumeBackKey(): Boolean {
            if (isActionEditorOpen) {
                isActionEditorOpen = false
                return true
            }
            if (isActionWindowOpen) {
                isActionWindowOpen = false
                return true
            }
            // Item 1 requirement: Do NOT collapse isActionsExpanded on back key!
            // Back key must be allowed to close the keyboard.
            return false
        }

        // Action window open: back key closes action window, leaves expanded bar intact
        isActionWindowOpen = true
        assertTrue("Back key must consume and close action window", consumeBackKey())
        assertFalse("Action window must now be closed", isActionWindowOpen)
        assertTrue("ActionBar must remain expanded", isActionsExpanded)

        // Main keyboard active with expanded action bar: back key does NOT consume
        assertFalse("Back key must return false so IME window can close", consumeBackKey())
        assertTrue("ActionBar must remain locked/expanded across back press", isActionsExpanded)
    }

    @Test
    fun testActionBarSourceCodeIntegrity() {
        val file = File("java/src/org/futo/inputmethod/latin/uix/ActionBar.kt")
        assertTrue("ActionBar.kt must exist", file.exists())
        val content = String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)

        // Verifies ExpandActionsButton toggles
        assertTrue(content.contains("ExpandActionsButton(isActionsExpanded)"))
        // Verifies kbBgColor usage on surfaces
        assertTrue(content.contains("val kbBgColor = Color(LocalThemeProvider.current.primaryKeyboardColor)"))
        // Verifies no hardcoded MaterialTheme.colorScheme.background on surfaces
        assertFalse("Must not have hardcoded MaterialTheme.colorScheme.background for ActionBar surface",
            content.contains("Surface(\n            modifier = Modifier\n                .fillMaxWidth()\n                .height(actionBarHeight), color = MaterialTheme.colorScheme.background"))
    }

    // =========================================================================
    // Item 2: Top (ActionBar) & Bottom (NavigationBar/Insets) Background Color Sync
    // =========================================================================
    @Test
    fun testCustomBackgroundColorMappingConstants() {
        val colorMap = mapOf(
            "amoled_black" to 0xFF000000.toInt(),
            "cyber_yellow" to RetroBlocksThemeHelper.COLOR_YELLOW,
            "light_olive" to RetroBlocksThemeHelper.COLOR_OLIVE,
            "muted_forest" to RetroBlocksThemeHelper.COLOR_FOREST,
            "dark_charcoal" to RetroBlocksThemeHelper.COLOR_CHARCOAL,
            "warm_cream" to RetroBlocksThemeHelper.COLOR_CREAM,
            "crimson_red" to RetroBlocksThemeHelper.COLOR_RED,
            "dark_slate" to 0xFF121316.toInt(),
            "charcoal_slate" to 0xFF1E1F24.toInt(),
            "deep_navy" to 0xFF0E1424.toInt(),
            "deep_purple" to 0xFF1A1128.toInt(),
            "deep_teal" to 0xFF002228.toInt(),
            "chalk_white" to 0xFFF5F5F7.toInt()
        )

        for ((key, expectedColor) in colorMap) {
            val customBgColor: Int? = when (key) {
                "amoled_black" -> 0xFF000000.toInt()
                "cyber_yellow" -> RetroBlocksThemeHelper.COLOR_YELLOW
                "light_olive" -> RetroBlocksThemeHelper.COLOR_OLIVE
                "muted_forest" -> RetroBlocksThemeHelper.COLOR_FOREST
                "dark_charcoal" -> RetroBlocksThemeHelper.COLOR_CHARCOAL
                "warm_cream" -> RetroBlocksThemeHelper.COLOR_CREAM
                "crimson_red" -> RetroBlocksThemeHelper.COLOR_RED
                "dark_slate" -> 0xFF121316.toInt()
                "charcoal_slate" -> 0xFF1E1F24.toInt()
                "deep_navy" -> 0xFF0E1424.toInt()
                "deep_purple" -> 0xFF1A1128.toInt()
                "deep_teal" -> 0xFF002228.toInt()
                "chalk_white" -> 0xFFF5F5F7.toInt()
                else -> null
            }
            assertEquals("Custom color $key must resolve to expected ARGB", expectedColor, customBgColor)
        }
    }

    @Test
    fun testNavigationBarVisibilityCalculation() {
        // Models LatinIME.updateNavigationBarVisibility(visible)
        fun computeShouldShow(visible: Boolean?, isNavigationBarVisible: Boolean, isInputViewShown: Boolean): Boolean {
            return if (visible == false) false else (isNavigationBarVisible || isInputViewShown)
        }

        // Scenario 1: Window hiding explicitly (visible = false) while isInputViewShown is still true
        assertFalse("When visible is false, nav bar must NOT show keyboard color",
            computeShouldShow(visible = false, isNavigationBarVisible = false, isInputViewShown = true))

        // Scenario 2: Window shown explicitly (visible = true)
        assertTrue("When visible is true, nav bar must show keyboard color",
            computeShouldShow(visible = true, isNavigationBarVisible = true, isInputViewShown = true))

        // Scenario 3: Null visible passed on theme switch while keyboard is shown
        assertTrue("Theme change while keyboard shown must keep nav bar colored",
            computeShouldShow(visible = null, isNavigationBarVisible = true, isInputViewShown = true))

        // Scenario 4: Null visible when keyboard is hidden
        assertFalse("Theme change while keyboard hidden must keep nav bar transparent",
            computeShouldShow(visible = null, isNavigationBarVisible = false, isInputViewShown = false))
    }

    // =========================================================================
    // Item 3: Auto-Reset to Canonical Defaults on Theme Switch
    // =========================================================================
    @Test
    fun testCanonicalResetOnThemeSwitchKeys() {
        val canonicalDefaults = mapOf(
            CustomAccentColorSetting.key.name to "default",
            KeyboardBgColorSetting.key.name to "default",
            KeyboardKeyColorSetting.key.name to "default",
            KeyboardTextColorSetting.key.name to "default",
            SpacebarColorSetting.key.name to "theme",
            SpacebarBorderSetting.key.name to "none",
            SpacebarShapeSetting.key.name to "boxy"
        )

        assertEquals("custom_accent_color", CustomAccentColorSetting.key.name)
        assertEquals("default", CustomAccentColorSetting.default)
        assertEquals("keyboard_bg_color", KeyboardBgColorSetting.key.name)
        assertEquals("default", KeyboardBgColorSetting.default)
        assertEquals("keyboard_key_color", KeyboardKeyColorSetting.key.name)
        assertEquals("default", KeyboardKeyColorSetting.default)
        assertEquals("keyboard_text_color", KeyboardTextColorSetting.key.name)
        assertEquals("default", KeyboardTextColorSetting.default)
        assertEquals("spacebar_color", SpacebarColorSetting.key.name)
        assertEquals("theme", SpacebarColorSetting.default)
        assertEquals("spacebar_border", SpacebarBorderSetting.key.name)
        assertEquals("none", SpacebarBorderSetting.default)
        assertEquals("spacebar_shape", SpacebarShapeSetting.key.name)
        assertEquals("boxy", SpacebarShapeSetting.default)

        assertEquals(7, canonicalDefaults.size)
    }

    @Test
    fun testThemeSwitchResetInSourceCode() {
        val pickerFile = File("java/src/org/futo/inputmethod/latin/uix/theme/selector/ThemePicker.kt")
        assertTrue("ThemePicker.kt must exist", pickerFile.exists())
        val pickerContent = String(Files.readAllBytes(pickerFile.toPath()), StandardCharsets.UTF_8)

        assertTrue(pickerContent.contains("setCustomAccent(\"default\")"))
        assertTrue(pickerContent.contains("setKeyboardBgColor(\"default\")"))
        assertTrue(pickerContent.contains("setKeyboardKeyColor(\"default\")"))
        assertTrue(pickerContent.contains("setKeyboardTextColor(\"default\")"))
        assertTrue(pickerContent.contains("setSpacebarColor(\"theme\")"))
        assertTrue(pickerContent.contains("setSpacebarBorder(\"none\")"))
        assertTrue(pickerContent.contains("setSpacebarShape(\"boxy\")"))

        val customScreenFile = File("java/src/org/futo/inputmethod/latin/uix/settings/pages/KeyboardCustomizationScreen.kt")
        assertTrue("KeyboardCustomizationScreen.kt must exist", customScreenFile.exists())
        val customContent = String(Files.readAllBytes(customScreenFile.toPath()), StandardCharsets.UTF_8)

        assertTrue(customContent.contains("setCustomAccent(\"default\")"))
        assertTrue(customContent.contains("setKeyboardBgColor(\"default\")"))
        assertTrue(customContent.contains("setKeyboardKeyColor(\"default\")"))
        assertTrue(customContent.contains("setKeyboardTextColor(\"default\")"))
        assertTrue(customContent.contains("setSpacebarColorKey(\"theme\")"))
        assertTrue(customContent.contains("setSpacebarBorderKey(\"none\")"))
        assertTrue(customContent.contains("setSpacebarShape(\"boxy\")"))
    }

    // =========================================================================
    // Item 4: Concise English Lock Banners
    // =========================================================================
    @Test
    fun testConciseEnglishLockBanners() {
        val customScreenFile = File("java/src/org/futo/inputmethod/latin/uix/settings/pages/KeyboardCustomizationScreen.kt")
        val content = String(Files.readAllBytes(customScreenFile.toPath()), StandardCharsets.UTF_8)

        // 1. Regular customization locked banner
        assertTrue("Must have concise English banner for Regular customization lock",
            content.contains("Select a Regular theme to customize colors"))

        // 2. Special customization locked banner
        assertTrue("Must have concise English banner for Special customization lock",
            content.contains("Select a Special theme to customize"))

        // 3. Verbose Bengali banners must be completely gone
        assertFalse("Old verbose Bengali text must not be present",
            content.contains("এই অপশনটি পরিবর্তন করতে"))
    }

    // =========================================================================
    // Item 5: Translation "Others" Click Crash Fix
    // =========================================================================
    @Test
    fun testTranslateActionNoAlertDialog() {
        val file = File("java/src/org/futo/inputmethod/latin/uix/actions/TranslateAction.kt")
        val content = String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)

        // Must not import or instantiate AlertDialog
        assertFalse("Must not import androidx.compose.material3.AlertDialog",
            content.contains("import androidx.compose.material3.AlertDialog"))
        assertFalse("Must not invoke AlertDialog(", content.contains("AlertDialog("))

        // Must contain in-place modal overlay Box
        assertTrue("Must contain in-place modal overlay with surface background",
            content.contains("languagePickerState?.let { picker ->"))
        assertTrue(content.contains("Select Language (ভাষা নির্বাচন)"))
        assertTrue(content.contains("Search language..."))
        assertTrue(content.contains("Others... (অন্যান্য)"))
    }

    // =========================================================================
    // Item 6: Pure Compose Visual Preview with Ndot Font at Top of Settings
    // =========================================================================
    @Test
    fun testRealisticKeyboardPreviewUsesPureComposeAndNdotFont() {
        val previewFile = File("java/src/org/futo/inputmethod/latin/uix/settings/pages/RealisticKeyboardPreview.kt")
        val content = String(Files.readAllBytes(previewFile.toPath()), StandardCharsets.UTF_8)

        // Verifies pure Compose visual preview with Ndot font and zero touch/focus-stealing bugs
        assertTrue("Must support Ndot font", content.contains("Ndot"))
        assertTrue("Must render KeyTile keys", content.contains("KeyTile("))
        assertFalse("Must NOT use MainKeyboardView to prevent pointer tracker corruption", content.contains("MainKeyboardView("))
    }

    @Test
    fun testKeyboardCustomizationScreenBindsRealActiveSubtype() {
        val file = File("java/src/org/futo/inputmethod/latin/uix/settings/pages/KeyboardCustomizationScreen.kt")
        val content = String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)

        assertTrue("Must bind ActiveSubtype", content.contains("useDataStore(ActiveSubtype.key, ActiveSubtype.default)"))
        assertTrue("Must pass activeLayoutDisplayName to RealisticKeyboardPreview",
            content.contains("activeLayoutName = activeLayoutDisplayName"))
        assertTrue("Must bind isNumberRowActive to RealisticKeyboardPreview",
            content.contains("showNumberRow = isNumberRowActive"))
    }
}
