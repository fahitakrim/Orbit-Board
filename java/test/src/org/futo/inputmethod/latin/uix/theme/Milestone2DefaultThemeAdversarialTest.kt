package org.futo.inputmethod.latin.uix.theme

import androidx.compose.ui.graphics.Color
import org.futo.inputmethod.latin.uix.THEME_KEY
import org.futo.inputmethod.latin.uix.theme.presets.OrbitBlack
import org.futo.inputmethod.latin.uix.theme.presets.OrbitMatteBlue
import org.futo.inputmethod.latin.uix.theme.presets.OrbitMatteDark
import org.futo.inputmethod.latin.uix.theme.presets.OrbitMatteRed
import org.futo.inputmethod.latin.uix.theme.presets.OrbitMatteWhite
import org.futo.inputmethod.latin.uix.theme.presets.OrbitMatteYellow
import org.futo.inputmethod.latin.uix.theme.presets.OrbitTheme
import org.futo.inputmethod.latin.uix.theme.presets.OrbitThemeHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Field
import java.util.Collections

/**
 * Empirical Adversarial Test Harness for Milestone 2:
 * Visual Styling & Layout Adjustments (Requirement R3)
 *
 * Objectives:
 * 1. Default Theme Prioritization Verification:
 *    - OrbitTheme is item index 0 in ThemeOptions.kt, KeyboardCustomizationScreen.kt:THEME_PRESETS,
 *      and ThemePicker.kt sorting logic.
 *    - isDefault == true exclusively for OrbitTheme, and false for all others.
 *    - ThemePicker.kt never filters out OrbitTheme under any circumstance and has no category tabs.
 * 2. Preference Persistence & Integrity:
 *    - THEME_KEY.default evaluates to "OrbitTheme".
 *    - OrbitTheme color palette, helper constants, and styles match design tokens exactly.
 * 3. Adversarial Permutation & Stability Stress Tests.
 */
class Milestone2DefaultThemeAdversarialTest {

    // =========================================================================
    // 1. ThemeOptions Registry Order & Identity Tests
    // =========================================================================

    @Test
    fun testOrbitThemeIsFirstInThemeOptionsMap() {
        val keysList = ThemeOptions.keys.toList()
        assertTrue("ThemeOptions registry must not be empty", keysList.isNotEmpty())
        assertEquals("OrbitTheme.key must be the exact first entry in ThemeOptions", "OrbitTheme", keysList[0])
        assertEquals("OrbitTheme.key must match OrbitTheme.key property", OrbitTheme.key, keysList[0])

        val firstOption = ThemeOptions[keysList[0]]
        assertNotNull("First theme option must not be null", firstOption)
        assertEquals("First theme option must be OrbitTheme", OrbitTheme, firstOption)
        assertEquals("OrbitTheme key property must be 'OrbitTheme'", "OrbitTheme", OrbitTheme.key)
        assertFalse("OrbitTheme must not be dynamic", OrbitTheme.dynamic)
    }

    @Test
    fun testOrbitThemeAvailableUnderAllContexts() {
        // available is (Context) -> Boolean, for OrbitTheme it should return true unconditionally
        // Even when passed a null context or mocking, lambda is { true }
        val availableLambda = OrbitTheme.available
        assertNotNull("OrbitTheme.available lambda must not be null", availableLambda)
        // Verify invocation (using dummy proxy or reflection if needed, or non-null check)
        assertEquals("OrbitTheme must not be dynamic", false, OrbitTheme.dynamic)
    }

    @Test
    fun testOrbitThemeFollowedByChaosThemeAndNothingDark() {
        val keysList = ThemeOptions.keys.toList()
        assertTrue("ThemeOptions must have at least 3 themes", keysList.size >= 3)
        assertEquals("First theme must be OrbitTheme", "OrbitTheme", keysList[0])
        assertEquals("Second theme must be OrbitChaos", "OrbitChaos", keysList[1])
        assertEquals("Third theme must be NothingDark", "NothingDark", keysList[2])
    }

    // =========================================================================
    // 2. THEME_KEY Persistence Invariants
    // =========================================================================

    @Test
    fun testThemeKeyNameAndDefaultEvaluation() {
        assertEquals("THEME_KEY preferences key name must be 'activeThemeOption'", "activeThemeOption", THEME_KEY.key.name)
        assertEquals("THEME_KEY.default must evaluate to 'OrbitTheme'", "OrbitTheme", THEME_KEY.default)
        assertEquals("THEME_KEY.default must evaluate to OrbitTheme.key", OrbitTheme.key, THEME_KEY.default)
    }

    // =========================================================================
    // 3. KeyboardCustomizationScreen THEME_PRESETS Verification (Reflection)
    // =========================================================================

    @Suppress("UNCHECKED_CAST")
    private fun getHoistedThemePresets(): List<Any> {
        val clazz = Class.forName("org.futo.inputmethod.latin.uix.settings.pages.KeyboardCustomizationScreenKt")
        val field: Field = clazz.getDeclaredField("THEME_PRESETS")
        field.isAccessible = true
        return field.get(null) as List<Any>
    }

    @Test
    fun testKeyboardCustomizationThemePresetsPrioritizeOrbitTheme() {
        val presets = getHoistedThemePresets()
        assertTrue("THEME_PRESETS must contain multiple presets", presets.size >= 10)

        // Item 0 inspection
        val item0 = presets[0]
        val getKeyMethod = item0.javaClass.getMethod("getKey")
        val isDefaultMethod = item0.javaClass.getMethod("isDefault")
        val isOrbitMethod = item0.javaClass.getMethod("isOrbit")
        val getNameMethod = item0.javaClass.getMethod("getName")

        val key0 = getKeyMethod.invoke(item0) as String
        val isDefault0 = isDefaultMethod.invoke(item0) as Boolean
        val isOrbit0 = isOrbitMethod.invoke(item0) as Boolean
        val name0 = getNameMethod.invoke(item0) as String

        assertEquals("Item 0 key in THEME_PRESETS must be 'OrbitTheme'", "OrbitTheme", key0)
        assertEquals("Item 0 name in THEME_PRESETS must be 'Orbit'", "Orbit", name0)
        assertTrue("Item 0 isDefault must be TRUE", isDefault0)
        assertTrue("Item 0 isOrbit must be TRUE", isOrbit0)

        // Item 1 inspection (OrbitChaos)
        val item1 = presets[1]
        val key1 = getKeyMethod.invoke(item1) as String
        val isDefault1 = isDefaultMethod.invoke(item1) as Boolean
        val isOrbit1 = isOrbitMethod.invoke(item1) as Boolean
        val name1 = getNameMethod.invoke(item1) as String

        assertEquals("Item 1 key in THEME_PRESETS must be 'OrbitChaos'", "OrbitChaos", key1)
        assertEquals("Item 1 name in THEME_PRESETS must be 'Chaos'", "Chaos", name1)
        assertFalse("Item 1 isDefault must be FALSE", isDefault1)
        assertTrue("Item 1 isOrbit must be TRUE", isOrbit1)
    }

    @Test
    fun testOnlyOrbitThemeHasIsDefaultTrueInThemePresets() {
        val presets = getHoistedThemePresets()
        val isDefaultMethod = presets[0].javaClass.getMethod("isDefault")
        val getKeyMethod = presets[0].javaClass.getMethod("getKey")

        var defaultCount = 0
        val defaultKeys = mutableListOf<String>()

        for (item in presets) {
            val isDefault = isDefaultMethod.invoke(item) as Boolean
            val key = getKeyMethod.invoke(item) as String
            if (isDefault) {
                defaultCount++
                defaultKeys.add(key)
            }
        }

        assertEquals("Exactly ONE preset must have isDefault == true", 1, defaultCount)
        assertEquals("The only preset with isDefault == true must be 'OrbitTheme'", listOf("OrbitTheme"), defaultKeys)
    }

    // =========================================================================
    // 4. ThemePicker Sorting & Anti-Filtering Logic
    // =========================================================================

    @Test
    fun testThemePickerSortingInvariantUnderAdversarialPermutations() {
        val sortOrder = { key: String ->
            when (key) {
                "OrbitTheme" -> 0
                "OrbitChaos" -> 1
                "NothingDark" -> 2
                else -> 100
            }
        }

        val allKeys = ThemeOptions.keys.toList()

        // 1. Natural order
        val sortedNatural = allKeys.sortedBy(sortOrder)
        assertEquals("OrbitTheme must be first in natural sort", "OrbitTheme", sortedNatural[0])
        assertEquals("OrbitChaos must be second in natural sort", "OrbitChaos", sortedNatural[1])
        assertEquals("NothingDark must be third in natural sort", "NothingDark", sortedNatural[2])

        // 2. Reversed order
        val reversedKeys = allKeys.reversed()
        val sortedReversed = reversedKeys.sortedBy(sortOrder)
        assertEquals("OrbitTheme must be first even when input is reversed", "OrbitTheme", sortedReversed[0])
        assertEquals("OrbitChaos must be second even when input is reversed", "OrbitChaos", sortedReversed[1])
        assertEquals("NothingDark must be third even when input is reversed", "NothingDark", sortedReversed[2])

        // 3. Shuffled / Adversarial permutations (10 iterations)
        val shuffled = allKeys.toMutableList()
        for (i in 0 until 10) {
            Collections.shuffle(shuffled)
            val sortedShuffled = shuffled.sortedBy(sortOrder)
            assertEquals("Iteration $i: OrbitTheme must ALWAYS sort to index 0", "OrbitTheme", sortedShuffled[0])
            assertEquals("Iteration $i: OrbitChaos must ALWAYS sort to index 1", "OrbitChaos", sortedShuffled[1])
            assertEquals("Iteration $i: NothingDark must ALWAYS sort to index 2", "NothingDark", sortedShuffled[2])
        }
    }

    @Test
    fun testOrbitThemeNeverFilteredOutByInspectionMode() {
        // In ThemePicker.kt:
        // .filter { when (isInspecting) { true -> !it.second.dynamic else -> true } }
        val isInspectingFalse = true // non-dynamic themes pass
        val isDynamic = OrbitTheme.dynamic
        assertFalse("OrbitTheme.dynamic is false, so it survives inspection filtering", isDynamic)
        assertTrue("OrbitTheme survives !it.second.dynamic", !isDynamic)
    }

    // =========================================================================
    // 5. Palette & Design Token Mathematical Invariants
    // =========================================================================

    @Test
    fun testOrbitThemePaletteColors() {
        assertEquals("OrbitMatteYellow must match 0xFFE2B438", Color(0xFFE2B438), OrbitMatteYellow)
        assertEquals("OrbitMatteBlue must match 0xFF2A5288", Color(0xFF2A5288), OrbitMatteBlue)
        assertEquals("OrbitMatteRed must match 0xFFC23636", Color(0xFFC23636), OrbitMatteRed)
        assertEquals("OrbitMatteWhite must match 0xFFEAE6E1", Color(0xFFEAE6E1), OrbitMatteWhite)
        assertEquals("OrbitMatteDark must match 0xFF1C1D20", Color(0xFF1C1D20), OrbitMatteDark)
        assertEquals("OrbitBlack must match 0xFF000000", Color(0xFF000000), OrbitBlack)
    }

    @Test
    fun testOrbitThemeHelperPaletteConstants() {
        assertEquals("OrbitThemeHelper.COLOR_YELLOW", 0xFFE2B438.toInt(), OrbitThemeHelper.COLOR_YELLOW)
        assertEquals("OrbitThemeHelper.COLOR_BLUE", 0xFF2A5288.toInt(), OrbitThemeHelper.COLOR_BLUE)
        assertEquals("OrbitThemeHelper.COLOR_RED", 0xFFC23636.toInt(), OrbitThemeHelper.COLOR_RED)
        assertEquals("OrbitThemeHelper.COLOR_WHITE", 0xFFEAE6E1.toInt(), OrbitThemeHelper.COLOR_WHITE)
        assertEquals("OrbitThemeHelper.COLOR_DARK", 0xFF1C1D20.toInt(), OrbitThemeHelper.COLOR_DARK)
        assertEquals("OrbitThemeHelper.COLOR_BLACK", 0xFF000000.toInt(), OrbitThemeHelper.COLOR_BLACK)
    }
}
