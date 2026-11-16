package org.futo.inputmethod.latin.uix.theme

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import org.futo.inputmethod.keyboard.Key
import org.futo.inputmethod.latin.common.Constants
import org.futo.inputmethod.latin.uix.theme.presets.RetroBlocksTheme
import org.futo.inputmethod.latin.uix.theme.presets.RetroBlocksThemeHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RetroBlocksThemeAndCustomizationTest {

    private fun createTestKey(label: String?, code: Int, x: Int = 0, y: Int = 0, width: Int = 50, height: Int = 50): Key {
        return Key(label, null, code, null, null, 0, 0, x, y, width, height, 0, 0)
    }

    @Test
    fun testRetroBlocksExactColorConstants() {
        assertEquals("Cyber Yellow must be #FED700", 0xFFFED700.toInt(), RetroBlocksThemeHelper.COLOR_YELLOW)
        assertEquals("Light Olive must be #9EA637", 0xFF9EA637.toInt(), RetroBlocksThemeHelper.COLOR_OLIVE)
        assertEquals("Muted Forest must be #4A644B", 0xFF4A644B.toInt(), RetroBlocksThemeHelper.COLOR_FOREST)
        assertEquals("Dark Charcoal must be #232621", 0xFF232621.toInt(), RetroBlocksThemeHelper.COLOR_CHARCOAL)
        assertEquals("Warm Cream must be #F0EFE6", 0xFFF0EFE6.toInt(), RetroBlocksThemeHelper.COLOR_CREAM)
        assertEquals("Crimson Red must be #BA1A1A", 0xFFBA1A1A.toInt(), RetroBlocksThemeHelper.COLOR_RED)
        assertEquals("Pitch Black must be #000000", 0xFF000000.toInt(), RetroBlocksThemeHelper.COLOR_BLACK)
    }

    @Test
    fun testRetroBlocksThemeRegistered() {
        val retro = ThemeOptions[RetroBlocksTheme.key]
        assertNotNull("RetroBlocks must be registered in ThemeOptions", retro)
        assertEquals("RetroBlocks", retro?.key)
    }

    @Test
    fun testKeyBaseColorsMatchPalette() {
        // Yellow keys: o, g, x
        val keyO = createTestKey("o", 'o'.code)
        val keyG = createTestKey("g", 'g'.code)
        val keyX = createTestKey("x", 'x'.code)
        assertEquals(RetroBlocksThemeHelper.COLOR_YELLOW, RetroBlocksThemeHelper.getKeyBaseColor(keyO))
        assertEquals(RetroBlocksThemeHelper.COLOR_YELLOW, RetroBlocksThemeHelper.getKeyBaseColor(keyG))
        assertEquals(RetroBlocksThemeHelper.COLOR_YELLOW, RetroBlocksThemeHelper.getKeyBaseColor(keyX))

        // Olive keys: q, t, j, m, v
        val keyQ = createTestKey("q", 'q'.code)
        val keyT = createTestKey("t", 't'.code)
        val keyJ = createTestKey("j", 'j'.code)
        val keyM = createTestKey("m", 'm'.code)
        val keyV = createTestKey("v", 'v'.code)
        assertEquals(RetroBlocksThemeHelper.COLOR_OLIVE, RetroBlocksThemeHelper.getKeyBaseColor(keyQ))
        assertEquals(RetroBlocksThemeHelper.COLOR_OLIVE, RetroBlocksThemeHelper.getKeyBaseColor(keyT))
        assertEquals(RetroBlocksThemeHelper.COLOR_OLIVE, RetroBlocksThemeHelper.getKeyBaseColor(keyJ))
        assertEquals(RetroBlocksThemeHelper.COLOR_OLIVE, RetroBlocksThemeHelper.getKeyBaseColor(keyM))
        assertEquals(RetroBlocksThemeHelper.COLOR_OLIVE, RetroBlocksThemeHelper.getKeyBaseColor(keyV))

        // Forest keys: w, y, d, k, b
        val keyW = createTestKey("w", 'w'.code)
        val keyY = createTestKey("y", 'y'.code)
        val keyD = createTestKey("d", 'd'.code)
        val keyK = createTestKey("k", 'k'.code)
        val keyB = createTestKey("b", 'b'.code)
        assertEquals(RetroBlocksThemeHelper.COLOR_FOREST, RetroBlocksThemeHelper.getKeyBaseColor(keyW))
        assertEquals(RetroBlocksThemeHelper.COLOR_FOREST, RetroBlocksThemeHelper.getKeyBaseColor(keyY))
        assertEquals(RetroBlocksThemeHelper.COLOR_FOREST, RetroBlocksThemeHelper.getKeyBaseColor(keyD))
        assertEquals(RetroBlocksThemeHelper.COLOR_FOREST, RetroBlocksThemeHelper.getKeyBaseColor(keyK))
        assertEquals(RetroBlocksThemeHelper.COLOR_FOREST, RetroBlocksThemeHelper.getKeyBaseColor(keyB))

        // Charcoal keys: e, i, p, f, l, z, n
        val keyE = createTestKey("e", 'e'.code)
        val keyI = createTestKey("i", 'i'.code)
        val keyP = createTestKey("p", 'p'.code)
        val keyF = createTestKey("f", 'f'.code)
        val keyL = createTestKey("l", 'l'.code)
        val keyZ = createTestKey("z", 'z'.code)
        val keyN = createTestKey("n", 'n'.code)
        assertEquals(RetroBlocksThemeHelper.COLOR_CHARCOAL, RetroBlocksThemeHelper.getKeyBaseColor(keyE))
        assertEquals(RetroBlocksThemeHelper.COLOR_CHARCOAL, RetroBlocksThemeHelper.getKeyBaseColor(keyI))
        assertEquals(RetroBlocksThemeHelper.COLOR_CHARCOAL, RetroBlocksThemeHelper.getKeyBaseColor(keyP))
        assertEquals(RetroBlocksThemeHelper.COLOR_CHARCOAL, RetroBlocksThemeHelper.getKeyBaseColor(keyF))
        assertEquals(RetroBlocksThemeHelper.COLOR_CHARCOAL, RetroBlocksThemeHelper.getKeyBaseColor(keyL))
        assertEquals(RetroBlocksThemeHelper.COLOR_CHARCOAL, RetroBlocksThemeHelper.getKeyBaseColor(keyZ))
        assertEquals(RetroBlocksThemeHelper.COLOR_CHARCOAL, RetroBlocksThemeHelper.getKeyBaseColor(keyN))

        // Cream keys: r, u, a, h, c
        val keyR = createTestKey("r", 'r'.code)
        val keyU = createTestKey("u", 'u'.code)
        val keyA = createTestKey("a", 'a'.code)
        val keyH = createTestKey("h", 'h'.code)
        val keyC = createTestKey("c", 'c'.code)
        assertEquals(RetroBlocksThemeHelper.COLOR_CREAM, RetroBlocksThemeHelper.getKeyBaseColor(keyR))
        assertEquals(RetroBlocksThemeHelper.COLOR_CREAM, RetroBlocksThemeHelper.getKeyBaseColor(keyU))
        assertEquals(RetroBlocksThemeHelper.COLOR_CREAM, RetroBlocksThemeHelper.getKeyBaseColor(keyA))
        assertEquals(RetroBlocksThemeHelper.COLOR_CREAM, RetroBlocksThemeHelper.getKeyBaseColor(keyH))
        assertEquals(RetroBlocksThemeHelper.COLOR_CREAM, RetroBlocksThemeHelper.getKeyBaseColor(keyC))

        // Crimson Red: Backspace
        val keyDel = createTestKey(null, Constants.CODE_DELETE)
        assertEquals(RetroBlocksThemeHelper.COLOR_RED, RetroBlocksThemeHelper.getKeyBaseColor(keyDel))

        // Functional keys
        val keyShift = createTestKey(null, Constants.CODE_SHIFT)
        val keyEnter = createTestKey(null, Constants.CODE_ENTER)
        assertEquals(RetroBlocksThemeHelper.COLOR_OLIVE, RetroBlocksThemeHelper.getKeyBaseColor(keyShift))
        assertEquals(RetroBlocksThemeHelper.COLOR_OLIVE, RetroBlocksThemeHelper.getKeyBaseColor(keyEnter))
    }

    @Test
    fun testKeyCaseInsensitivity() {
        val keyUpperO = createTestKey("O", 'O'.code)
        val keyUpperA = createTestKey("A", 'A'.code)
        val keyUpperW = createTestKey("W", 'W'.code)
        assertEquals(RetroBlocksThemeHelper.COLOR_YELLOW, RetroBlocksThemeHelper.getKeyBaseColor(keyUpperO))
        assertEquals(RetroBlocksThemeHelper.COLOR_CREAM, RetroBlocksThemeHelper.getKeyBaseColor(keyUpperA))
        assertEquals(RetroBlocksThemeHelper.COLOR_FOREST, RetroBlocksThemeHelper.getKeyBaseColor(keyUpperW))
    }

    @Test
    fun testTextContrastPerKey() {
        // Yellow and Cream keys must have dark text
        val keyO = createTestKey("o", 'o'.code)
        val keyA = createTestKey("a", 'a'.code)
        assertEquals(RetroBlocksThemeHelper.COLOR_TEXT_DARK, RetroBlocksThemeHelper.getKeyTextColor(keyO))
        assertEquals(RetroBlocksThemeHelper.COLOR_TEXT_DARK, RetroBlocksThemeHelper.getKeyTextColor(keyA))

        // All Olive keys have dark text for maximum contrast on #9EA637
        val keyQ = createTestKey("q", 'q'.code)
        val keyM = createTestKey("m", 'm'.code)
        val keyT = createTestKey("t", 't'.code)
        val keyJ = createTestKey("j", 'j'.code)
        val keyS = createTestKey("s", 's'.code)
        val keyV = createTestKey("v", 'v'.code)
        val keyShift = createTestKey(null, Constants.CODE_SHIFT)
        val keyEnter = createTestKey(null, Constants.CODE_ENTER)
        assertEquals(RetroBlocksThemeHelper.COLOR_TEXT_DARK, RetroBlocksThemeHelper.getKeyTextColor(keyQ))
        assertEquals(RetroBlocksThemeHelper.COLOR_TEXT_DARK, RetroBlocksThemeHelper.getKeyTextColor(keyM))
        assertEquals(RetroBlocksThemeHelper.COLOR_TEXT_DARK, RetroBlocksThemeHelper.getKeyTextColor(keyT))
        assertEquals(RetroBlocksThemeHelper.COLOR_TEXT_DARK, RetroBlocksThemeHelper.getKeyTextColor(keyJ))
        assertEquals(RetroBlocksThemeHelper.COLOR_TEXT_DARK, RetroBlocksThemeHelper.getKeyTextColor(keyS))
        assertEquals(RetroBlocksThemeHelper.COLOR_TEXT_DARK, RetroBlocksThemeHelper.getKeyTextColor(keyV))
        assertEquals(RetroBlocksThemeHelper.COLOR_TEXT_DARK, RetroBlocksThemeHelper.getKeyTextColor(keyShift))
        assertEquals(RetroBlocksThemeHelper.COLOR_TEXT_DARK, RetroBlocksThemeHelper.getKeyTextColor(keyEnter))

        // Dark keys (Charcoal, Forest, Red) must have light text
        val keyE = createTestKey("e", 'e'.code)
        val keyW = createTestKey("w", 'w'.code)
        val keyDel = createTestKey(null, Constants.CODE_DELETE)
        assertEquals(RetroBlocksThemeHelper.COLOR_TEXT_LIGHT, RetroBlocksThemeHelper.getKeyTextColor(keyE))
        assertEquals(RetroBlocksThemeHelper.COLOR_TEXT_LIGHT, RetroBlocksThemeHelper.getKeyTextColor(keyW))
        assertEquals(RetroBlocksThemeHelper.COLOR_TEXT_LIGHT, RetroBlocksThemeHelper.getKeyTextColor(keyDel))
    }

    @Test
    fun testSpacebarCustomization() {
        try {
            // Default spacebar
            RetroBlocksThemeHelper.customSpacebarColor = null
            RetroBlocksThemeHelper.customSpacebarBorderColor = null
            RetroBlocksThemeHelper.customSpacebarTextColor = null
            RetroBlocksThemeHelper.spacebarShape = "boxy"
            RetroBlocksThemeHelper.customSpacebarCornerRadius = null

            val spaceKey = createTestKey("Space", Constants.CODE_SPACE, width = 200, height = 50)
            assertEquals(RetroBlocksThemeHelper.COLOR_BLACK, RetroBlocksThemeHelper.getKeyBaseColor(spaceKey))
            assertEquals(RetroBlocksThemeHelper.COLOR_YELLOW, RetroBlocksThemeHelper.getKeyTextColor(spaceKey))

            // Custom Spacebar Color: Cyber Yellow
            RetroBlocksThemeHelper.customSpacebarColor = RetroBlocksThemeHelper.COLOR_YELLOW
            assertEquals(RetroBlocksThemeHelper.COLOR_YELLOW, RetroBlocksThemeHelper.getKeyBaseColor(spaceKey))

            // Custom Spacebar Text Color: Crimson Red
            RetroBlocksThemeHelper.customSpacebarTextColor = RetroBlocksThemeHelper.COLOR_RED
            assertEquals(RetroBlocksThemeHelper.COLOR_RED, RetroBlocksThemeHelper.getKeyTextColor(spaceKey))

            // Custom Spacebar Shape: Roundy
            RetroBlocksThemeHelper.spacebarShape = "roundy"
            val bgDrawable = RetroBlocksThemeHelper.getKeyBackground(spaceKey, false, 8f)
            assertTrue("Spacebar background must be GradientDrawable", bgDrawable is GradientDrawable)
            val gd = bgDrawable as GradientDrawable
            assertEquals(GradientDrawable.RECTANGLE, gd.shape)
        } finally {
            // Reset state
            RetroBlocksThemeHelper.customSpacebarColor = null
            RetroBlocksThemeHelper.customSpacebarBorderColor = null
            RetroBlocksThemeHelper.customSpacebarTextColor = null
            RetroBlocksThemeHelper.spacebarShape = "boxy"
            RetroBlocksThemeHelper.customSpacebarCornerRadius = null
        }
    }

    @Test
    fun testKeyPreviewBackgroundDynamicMatching() {
        val keyO = createTestKey("o", 'o'.code)
        val previewO = RetroBlocksThemeHelper.getKeyPreviewBackground(keyO, 8f, 48, 48, 10)
        assertNotNull(previewO)
        assertTrue(previewO is ShapeDrawable)
        assertEquals(RetroBlocksThemeHelper.COLOR_YELLOW, RetroBlocksThemeHelper.getKeyBaseColor(keyO))

        val keyA = createTestKey("a", 'a'.code)
        val previewA = RetroBlocksThemeHelper.getKeyPreviewBackground(keyA, 8f, 48, 48, 10)
        assertNotNull(previewA)
        assertTrue(previewA is ShapeDrawable)
        assertEquals(RetroBlocksThemeHelper.COLOR_CREAM, RetroBlocksThemeHelper.getKeyBaseColor(keyA))

        val keyW = createTestKey("w", 'w'.code)
        val previewW = RetroBlocksThemeHelper.getKeyPreviewBackground(keyW, 8f, 48, 48, 10)
        assertNotNull(previewW)
        assertTrue(previewW is ShapeDrawable)
        assertEquals(RetroBlocksThemeHelper.COLOR_FOREST, RetroBlocksThemeHelper.getKeyBaseColor(keyW))

        // Different colored keys must produce distinct drawables
        assertTrue("Different colors should yield distinct drawables", previewO !== previewA)

        // Same colored keys should reuse cached drawable
        val keyG = createTestKey("g", 'g'.code)
        val previewG = RetroBlocksThemeHelper.getKeyPreviewBackground(keyG, 8f, 48, 48, 10)
        assertEquals("Same color keys must reuse cached drawable", previewO, previewG)
    }

    @Test
    fun testPunctuationColors() {
        val keyComma = createTestKey(",", ','.code)
        val keyPeriod = createTestKey(".", '.'.code)
        val keyPeriodGear = createTestKey(".\n⚙", '.'.code)
        assertEquals("Comma must be Muted Forest in Retro Blocks", RetroBlocksThemeHelper.COLOR_FOREST, RetroBlocksThemeHelper.getKeyBaseColor(keyComma))
        assertEquals("Period must be Dark Charcoal in Retro Blocks", RetroBlocksThemeHelper.COLOR_CHARCOAL, RetroBlocksThemeHelper.getKeyBaseColor(keyPeriod))
        assertEquals("Period with gear icon must be Dark Charcoal in Retro Blocks", RetroBlocksThemeHelper.COLOR_CHARCOAL, RetroBlocksThemeHelper.getKeyBaseColor(keyPeriodGear))
    }

    @Test
    fun testBengaliAvroKeyColors() {
        val keyBanglaA = createTestKey("অ", 'অ'.code)
        assertEquals(RetroBlocksThemeHelper.COLOR_CREAM, RetroBlocksThemeHelper.getKeyBaseColor(keyBanglaA))

        val keyBanglaKkho = createTestKey("ক্ষ", "ক্ষ".codePointAt(0))
        assertEquals(RetroBlocksThemeHelper.COLOR_YELLOW, RetroBlocksThemeHelper.getKeyBaseColor(keyBanglaKkho))

        val keyBanglaGa = createTestKey("গ", 'গ'.code)
        assertEquals(RetroBlocksThemeHelper.COLOR_YELLOW, RetroBlocksThemeHelper.getKeyBaseColor(keyBanglaGa))

        val keyBanglaPa = createTestKey("প", 'প'.code)
        assertEquals(RetroBlocksThemeHelper.COLOR_CREAM, RetroBlocksThemeHelper.getKeyBaseColor(keyBanglaPa))

        val keyBanglaTa = createTestKey("ত", 'ত'.code)
        assertEquals(RetroBlocksThemeHelper.COLOR_FOREST, RetroBlocksThemeHelper.getKeyBaseColor(keyBanglaTa))

        val keyBanglaDa = createTestKey("দ", 'দ'.code)
        assertEquals(RetroBlocksThemeHelper.COLOR_CHARCOAL, RetroBlocksThemeHelper.getKeyBaseColor(keyBanglaDa))
    }

    @Test
    fun testOrbitAndChaosSpacebarCustomization() {
        try {
            // Default Orbit Spacebar
            org.futo.inputmethod.latin.uix.theme.presets.OrbitThemeHelper.customSpacebarColor = null
            org.futo.inputmethod.latin.uix.theme.presets.OrbitThemeHelper.customSpacebarTextColor = null
            org.futo.inputmethod.latin.uix.theme.presets.OrbitThemeHelper.spacebarShape = "boxy"

            val spaceKey = createTestKey("Space", Constants.CODE_SPACE, width = 200, height = 50)
            assertEquals(org.futo.inputmethod.latin.uix.theme.presets.OrbitThemeHelper.COLOR_DARK, org.futo.inputmethod.latin.uix.theme.presets.OrbitThemeHelper.getKeyBaseColor(spaceKey))
            assertEquals(android.graphics.Color.WHITE, org.futo.inputmethod.latin.uix.theme.presets.OrbitThemeHelper.getKeyTextColor(spaceKey))
            assertEquals(org.futo.inputmethod.latin.uix.theme.presets.ChaosThemeHelper.COLOR_YELLOW, org.futo.inputmethod.latin.uix.theme.presets.ChaosThemeHelper.getKeyTextColor(spaceKey))

            // Custom Spacebar Color
            org.futo.inputmethod.latin.uix.theme.presets.OrbitThemeHelper.customSpacebarColor = RetroBlocksThemeHelper.COLOR_YELLOW
            assertEquals(RetroBlocksThemeHelper.COLOR_YELLOW, org.futo.inputmethod.latin.uix.theme.presets.OrbitThemeHelper.getKeyBaseColor(spaceKey))

            // Custom Spacebar Text Color
            org.futo.inputmethod.latin.uix.theme.presets.OrbitThemeHelper.customSpacebarTextColor = RetroBlocksThemeHelper.COLOR_RED
            assertEquals(RetroBlocksThemeHelper.COLOR_RED, org.futo.inputmethod.latin.uix.theme.presets.OrbitThemeHelper.getKeyTextColor(spaceKey))
            assertEquals(RetroBlocksThemeHelper.COLOR_RED, org.futo.inputmethod.latin.uix.theme.presets.ChaosThemeHelper.getKeyTextColor(spaceKey))

            // Custom Spacebar Shape: Roundy
            org.futo.inputmethod.latin.uix.theme.presets.OrbitThemeHelper.spacebarShape = "roundy"
            val chaosBg = org.futo.inputmethod.latin.uix.theme.presets.ChaosThemeHelper.getKeyBackground(spaceKey, false, 8f)
            assertTrue("Chaos spacebar background must be GradientDrawable", chaosBg is GradientDrawable)
        } finally {
            org.futo.inputmethod.latin.uix.theme.presets.OrbitThemeHelper.customSpacebarColor = null
            org.futo.inputmethod.latin.uix.theme.presets.OrbitThemeHelper.customSpacebarTextColor = null
            org.futo.inputmethod.latin.uix.theme.presets.OrbitThemeHelper.spacebarShape = "boxy"
        }
    }
}
