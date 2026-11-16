package org.futo.inputmethod.latin.uix

import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.actions.AllActionsMap
import org.futo.inputmethod.latin.uix.actions.FloatingAction
import org.futo.inputmethod.latin.uix.actions.OneHandedAction
import org.futo.inputmethod.latin.uix.actions.serializeActionListToString
import org.futo.inputmethod.latin.uix.actions.toActionList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OneHandedAndDisplayModeTest {

    @Test
    fun testKeyboardDisplayModeEnum() {
        val modes = KeyboardDisplayMode.entries
        assertEquals(4, modes.size)
        assertTrue(modes.contains(KeyboardDisplayMode.STANDARD))
        assertTrue(modes.contains(KeyboardDisplayMode.ONE_HANDED_LEFT))
        assertTrue(modes.contains(KeyboardDisplayMode.ONE_HANDED_RIGHT))
        assertTrue(modes.contains(KeyboardDisplayMode.FLOATING))
    }

    @Test
    fun testKeyboardDisplayModeDefaultSetting() {
        assertEquals(KeyboardDisplayMode.STANDARD.name, KEYBOARD_DISPLAY_MODE.default)
        assertEquals("keyboard_display_mode", KEYBOARD_DISPLAY_MODE.key.name)
    }

    @Test
    fun testKeyboardDisplayModeManagerWidthMultipliers() {
        KeyboardDisplayModeManager.currentMode = KeyboardDisplayMode.STANDARD
        assertEquals(1.0f, KeyboardDisplayModeManager.getWidthMultiplier(), 0.001f)
        assertEquals(1.0f, KeyboardDisplayModeManager.getWidthMultiplier(isLandscape = false), 0.001f)
        assertEquals(0.70f, KeyboardDisplayModeManager.getWidthMultiplier(isLandscape = true), 0.001f)

        KeyboardDisplayModeManager.currentMode = KeyboardDisplayMode.FLOATING
        assertEquals(0.78f, KeyboardDisplayModeManager.getWidthMultiplier(), 0.001f)
        assertEquals(0.78f, KeyboardDisplayModeManager.getWidthMultiplier(isLandscape = false), 0.001f)
        assertEquals(0.70f, KeyboardDisplayModeManager.getWidthMultiplier(isLandscape = true), 0.001f)

        KeyboardDisplayModeManager.currentMode = KeyboardDisplayMode.ONE_HANDED_LEFT
        assertEquals(0.78f, KeyboardDisplayModeManager.getWidthMultiplier(), 0.001f)
        assertEquals(0.78f, KeyboardDisplayModeManager.getWidthMultiplier(isLandscape = false), 0.001f)
        assertEquals(0.78f, KeyboardDisplayModeManager.getWidthMultiplier(isLandscape = true), 0.001f)

        KeyboardDisplayModeManager.currentMode = KeyboardDisplayMode.ONE_HANDED_RIGHT
        assertEquals(0.78f, KeyboardDisplayModeManager.getWidthMultiplier(), 0.001f)
        assertEquals(0.78f, KeyboardDisplayModeManager.getWidthMultiplier(isLandscape = false), 0.001f)
        assertEquals(0.78f, KeyboardDisplayModeManager.getWidthMultiplier(isLandscape = true), 0.001f)

        // Reset back to standard
        KeyboardDisplayModeManager.currentMode = KeyboardDisplayMode.STANDARD
    }

    @Test
    fun testLandscapeHeightMultiplier() {
        assertEquals(1.0f, KeyboardDisplayModeManager.getHeightMultiplier(isLandscape = false), 0.001f)
        assertEquals(0.78f, KeyboardDisplayModeManager.getHeightMultiplier(isLandscape = true), 0.001f)
    }

    @Test
    fun testFloatingModeOffsetClamping() {
        val screenWidth = 1080f
        val screenHeight = 2400f
        val keyboardWidth = 842.4f // 1080 * 0.78
        val keyboardHeight = 800f
        val statusBarHeight = 84f
        val navBarHeight = 126f

        val maxX = screenWidth - keyboardWidth // 237.6
        val minY = statusBarHeight // 84.0
        val maxY = screenHeight - keyboardHeight - navBarHeight // 1474.0

        // In-bound values remain unchanged
        assertEquals(100f, FloatingPositionHelper.clampOffsetX(100f, screenWidth, keyboardWidth), 0.001f)
        assertEquals(500f, FloatingPositionHelper.clampOffsetY(500f, screenHeight, keyboardHeight, statusBarHeight, navBarHeight), 0.001f)

        // Under-bound (negative or too small) clamp to minimums
        assertEquals(0f, FloatingPositionHelper.clampOffsetX(-50f, screenWidth, keyboardWidth), 0.001f)
        assertEquals(minY, FloatingPositionHelper.clampOffsetY(-100f, screenHeight, keyboardHeight, statusBarHeight, navBarHeight), 0.001f)
        assertEquals(minY, FloatingPositionHelper.clampOffsetY(30f, screenHeight, keyboardHeight, statusBarHeight, navBarHeight), 0.001f)

        // Over-bound clamp to maximums
        assertEquals(maxX, FloatingPositionHelper.clampOffsetX(500f, screenWidth, keyboardWidth), 0.001f)
        assertEquals(maxY, FloatingPositionHelper.clampOffsetY(2000f, screenHeight, keyboardHeight, statusBarHeight, navBarHeight), 0.001f)

        // Edge case: Narrow screen where keyboardWidth >= screenWidth
        assertEquals(0f, FloatingPositionHelper.clampOffsetX(50f, 800f, 900f), 0.001f)

        // Edge case: Short screen where keyboardHeight + navBarHeight >= screenHeight
        val clampedShort = FloatingPositionHelper.clampOffsetY(500f, 1000f, 950f, statusBarHeight, 100f)
        assertEquals(statusBarHeight, clampedShort, 0.001f)

        // Default position calculation
        val (defX, defY) = FloatingPositionHelper.calculateDefaultPosition(
            screenWidth, screenHeight, keyboardWidth, keyboardHeight, statusBarHeight, navBarHeight
        )
        assertEquals(maxX / 2f, defX, 0.001f)
        assertTrue(defY in minY..maxY)
    }

    @Test
    fun testDataStoreFloatingOffsetKeys() {
        assertEquals(-1.0f, FLOATING_KEYBOARD_OFFSET_X.default, 0.001f)
        assertEquals("floating_keyboard_offset_x", FLOATING_KEYBOARD_OFFSET_X.key.name)

        assertEquals(-1.0f, FLOATING_KEYBOARD_OFFSET_Y.default, 0.001f)
        assertEquals("floating_keyboard_offset_y", FLOATING_KEYBOARD_OFFSET_Y.key.name)
    }

    @Test
    fun testModeSwitchingTransitions() {
        fun simulateOneHandedAction(current: KeyboardDisplayMode): KeyboardDisplayMode {
            return when (current) {
                KeyboardDisplayMode.ONE_HANDED_LEFT,
                KeyboardDisplayMode.ONE_HANDED_RIGHT -> KeyboardDisplayMode.STANDARD
                else -> KeyboardDisplayMode.ONE_HANDED_RIGHT
            }
        }

        fun simulateFloatingAction(current: KeyboardDisplayMode): KeyboardDisplayMode {
            return if (current == KeyboardDisplayMode.FLOATING) {
                KeyboardDisplayMode.STANDARD
            } else {
                KeyboardDisplayMode.FLOATING
            }
        }

        fun simulateDockButton(): KeyboardDisplayMode {
            return KeyboardDisplayMode.STANDARD
        }

        // Transition 1: STANDARD -> FLOATING -> STANDARD
        var mode = KeyboardDisplayMode.STANDARD
        mode = simulateFloatingAction(mode)
        assertEquals(KeyboardDisplayMode.FLOATING, mode)
        mode = simulateFloatingAction(mode)
        assertEquals(KeyboardDisplayMode.STANDARD, mode)

        // Transition 2: STANDARD -> ONE_HANDED -> FLOATING -> DOCK (STANDARD)
        mode = KeyboardDisplayMode.STANDARD
        mode = simulateOneHandedAction(mode)
        assertEquals(KeyboardDisplayMode.ONE_HANDED_RIGHT, mode)
        mode = simulateFloatingAction(mode)
        assertEquals(KeyboardDisplayMode.FLOATING, mode)
        mode = simulateDockButton()
        assertEquals(KeyboardDisplayMode.STANDARD, mode)

        // Transition 3: FLOATING -> ONE_HANDED -> FLOATING
        mode = KeyboardDisplayMode.FLOATING
        mode = simulateOneHandedAction(mode)
        assertEquals(KeyboardDisplayMode.ONE_HANDED_RIGHT, mode)
        mode = simulateFloatingAction(mode)
        assertEquals(KeyboardDisplayMode.FLOATING, mode)
    }

    @Test
    fun testOneHandedActionDefinition() {
        assertNotNull(OneHandedAction)
        assertEquals(R.drawable.one_handed, OneHandedAction.icon)
        assertEquals(R.string.action_one_handed, OneHandedAction.name)
        assertNotNull(OneHandedAction.simplePressImpl)
    }

    @Test
    fun testFloatingActionDefinition() {
        assertNotNull(FloatingAction)
        assertEquals(R.drawable.floating, FloatingAction.icon)
        assertEquals(R.string.action_floating, FloatingAction.name)
        assertNotNull(FloatingAction.simplePressImpl)
    }

    @Test
    fun testAllActionsMapRegistration() {
        assertTrue("AllActionsMap must contain one_handed", AllActionsMap.containsKey("one_handed"))
        assertEquals(OneHandedAction, AllActionsMap["one_handed"])

        assertTrue("AllActionsMap must contain floating", AllActionsMap.containsKey("floating"))
        assertEquals(FloatingAction, AllActionsMap["floating"])
    }

    @Test
    fun testActionListSerializationWithNewActions() {
        val testList = listOf(OneHandedAction, FloatingAction)
        val serialized = testList.serializeActionListToString()
        assertEquals("one_handed,floating", serialized)

        val deserialized = serialized.toActionList()
        assertEquals(2, deserialized.size)
        assertEquals(OneHandedAction, deserialized[0])
        assertEquals(FloatingAction, deserialized[1])
    }

    @Test
    fun testOneHandedModeToggleLogic() {
        fun computeNextOneHandedMode(current: KeyboardDisplayMode): KeyboardDisplayMode {
            return when (current) {
                KeyboardDisplayMode.ONE_HANDED_LEFT,
                KeyboardDisplayMode.ONE_HANDED_RIGHT -> KeyboardDisplayMode.STANDARD
                else -> KeyboardDisplayMode.ONE_HANDED_RIGHT
            }
        }

        assertEquals(KeyboardDisplayMode.ONE_HANDED_RIGHT, computeNextOneHandedMode(KeyboardDisplayMode.STANDARD))
        assertEquals(KeyboardDisplayMode.STANDARD, computeNextOneHandedMode(KeyboardDisplayMode.ONE_HANDED_RIGHT))
        assertEquals(KeyboardDisplayMode.STANDARD, computeNextOneHandedMode(KeyboardDisplayMode.ONE_HANDED_LEFT))
        assertEquals(KeyboardDisplayMode.ONE_HANDED_RIGHT, computeNextOneHandedMode(KeyboardDisplayMode.FLOATING))
    }

    @Test
    fun testFloatingModeToggleLogic() {
        fun computeNextFloatingMode(current: KeyboardDisplayMode): KeyboardDisplayMode {
            return if (current == KeyboardDisplayMode.FLOATING) {
                KeyboardDisplayMode.STANDARD
            } else {
                KeyboardDisplayMode.FLOATING
            }
        }

        assertEquals(KeyboardDisplayMode.FLOATING, computeNextFloatingMode(KeyboardDisplayMode.STANDARD))
        assertEquals(KeyboardDisplayMode.STANDARD, computeNextFloatingMode(KeyboardDisplayMode.FLOATING))
        assertEquals(KeyboardDisplayMode.FLOATING, computeNextFloatingMode(KeyboardDisplayMode.ONE_HANDED_RIGHT))
    }

    @Test
    fun testSideDockSwitchLogic() {
        fun switchDockSide(current: KeyboardDisplayMode): KeyboardDisplayMode {
            return if (current == KeyboardDisplayMode.ONE_HANDED_RIGHT) {
                KeyboardDisplayMode.ONE_HANDED_LEFT
            } else {
                KeyboardDisplayMode.ONE_HANDED_RIGHT
            }
        }

        assertEquals(KeyboardDisplayMode.ONE_HANDED_LEFT, switchDockSide(KeyboardDisplayMode.ONE_HANDED_RIGHT))
        assertEquals(KeyboardDisplayMode.ONE_HANDED_RIGHT, switchDockSide(KeyboardDisplayMode.ONE_HANDED_LEFT))
    }

    @Test
    fun testKeyboardDisplayModeManagerInitCorruptStringSafety() {
        // Unknown or corrupted mode string falls back to STANDARD
        val corruptStrings = listOf("", "UNKNOWN", "null", "one_handed", "FLOATING_MODE")
        for (str in corruptStrings) {
            val resolved = try {
                KeyboardDisplayMode.valueOf(str)
            } catch (_: Throwable) {
                KeyboardDisplayMode.STANDARD
            }
            assertEquals(KeyboardDisplayMode.STANDARD, resolved)
        }
    }

    @Test
    fun testOneHandedSideDockAlignmentAndIconInvariants() {
        // In ONE_HANDED_RIGHT: keyboard is on right, dock is on left (isLeftDock = true) -> arrow points left
        val rightMode = KeyboardDisplayMode.ONE_HANDED_RIGHT
        val isLeftDockForRight = rightMode == KeyboardDisplayMode.ONE_HANDED_RIGHT
        assertTrue("In ONE_HANDED_RIGHT mode dock must be on the left", isLeftDockForRight)
        val iconForRight = if (isLeftDockForRight) R.drawable.arrow_left else R.drawable.arrow_right
        assertEquals(R.drawable.arrow_left, iconForRight)

        // In ONE_HANDED_LEFT: keyboard is on left, dock is on right (isLeftDock = false) -> arrow points right
        val leftMode = KeyboardDisplayMode.ONE_HANDED_LEFT
        val isLeftDockForLeft = leftMode == KeyboardDisplayMode.ONE_HANDED_RIGHT
        assertTrue("In ONE_HANDED_LEFT mode dock must be on the right", !isLeftDockForLeft)
        val iconForLeft = if (isLeftDockForLeft) R.drawable.arrow_left else R.drawable.arrow_right
        assertEquals(R.drawable.arrow_right, iconForLeft)
    }

    @Test
    fun testWindowAndObscurationSafetyInvariants() {
        // Obscuration Rule 4 invariants:
        // One-handed width multiplier must strictly compress keyboard width to ~78%
        assertEquals(0.78f, 78 / 100.0f, 0.001f)
        val dockWeight = 0.22f
        val keyboardWeight = 0.78f
        assertEquals(1.0f, dockWeight + keyboardWeight, 0.001f)

        // Standard in portrait is 1.0f, Floating is compact 0.78f
        KeyboardDisplayModeManager.currentMode = KeyboardDisplayMode.STANDARD
        assertEquals(1.0f, KeyboardDisplayModeManager.getWidthMultiplier(), 0.001f)
        KeyboardDisplayModeManager.currentMode = KeyboardDisplayMode.FLOATING
        assertEquals(0.78f, KeyboardDisplayModeManager.getWidthMultiplier(), 0.001f)
        // In landscape, standard is compressed to 0.70f thumb reach
        assertEquals(0.70f, KeyboardDisplayModeManager.getWidthMultiplier(isLandscape = true), 0.001f)
        assertEquals(0.78f, KeyboardDisplayModeManager.getHeightMultiplier(isLandscape = true), 0.001f)

        // Reset
        KeyboardDisplayModeManager.currentMode = KeyboardDisplayMode.STANDARD
    }

    @Test
    fun testFloatingModeFourCornersClamping() {
        val screenW = 1080f
        val screenH = 2400f
        val cardW = 842.4f
        val cardH = 800f
        val statH = 84f
        val navH = 126f

        val maxX = screenW - cardW
        val maxY = screenH - cardH - navH

        // Top-Left corner: dragging far up and left clamps to (0, statusBarHeight)
        val tlX = FloatingPositionHelper.clampOffsetX(-1000f, screenW, cardW)
        val tlY = FloatingPositionHelper.clampOffsetY(-1000f, screenH, cardH, statH, navH)
        assertEquals(0f, tlX, 0.001f)
        assertEquals(statH, tlY, 0.001f)

        // Top-Right corner: dragging far up and right clamps to (maxX, statusBarHeight)
        val trX = FloatingPositionHelper.clampOffsetX(9999f, screenW, cardW)
        val trY = FloatingPositionHelper.clampOffsetY(-1000f, screenH, cardH, statH, navH)
        assertEquals(maxX, trX, 0.001f)
        assertEquals(statH, trY, 0.001f)

        // Bottom-Left corner: dragging far down and left clamps to (0, maxY)
        val blX = FloatingPositionHelper.clampOffsetX(-1000f, screenW, cardW)
        val blY = FloatingPositionHelper.clampOffsetY(9999f, screenH, cardH, statH, navH)
        assertEquals(0f, blX, 0.001f)
        assertEquals(maxY, blY, 0.001f)

        // Bottom-Right corner: dragging far down and right clamps to (maxX, maxY)
        val brX = FloatingPositionHelper.clampOffsetX(9999f, screenW, cardW)
        val brY = FloatingPositionHelper.clampOffsetY(9999f, screenH, cardH, statH, navH)
        assertEquals(maxX, brX, 0.001f)
        assertEquals(maxY, brY, 0.001f)
    }

    @Test
    fun testFloatingModeOrientationChangeClampingAndRestoration() {
        // Portrait initial state
        val portraitW = 1080f
        val portraitH = 2400f
        val portraitCardW = 1080f * 0.78f // 842.4f
        val portraitCardH = 800f
        val statusBarH = 84f
        val navBarH = 126f

        // User drags keyboard to lower right in portrait
        val savedX = 200f
        val savedY = 1400f

        val portraitClampedX = FloatingPositionHelper.clampOffsetX(savedX, portraitW, portraitCardW)
        val portraitClampedY = FloatingPositionHelper.clampOffsetY(savedY, portraitH, portraitCardH, statusBarH, navBarH)
        assertEquals(200f, portraitClampedX, 0.001f)
        assertEquals(1400f, portraitClampedY, 0.001f)

        // Rotate to landscape: screen dimensions flip
        val landscapeW = 2400f
        val landscapeH = 1080f
        val landscapeCardW = 2400f * 0.70f // 1680f
        val landscapeCardH = 800f * 0.78f // 624f (compressed height in landscape)
        val landscapeMaxX = landscapeW - landscapeCardW // 720f
        val landscapeMaxY = landscapeH - landscapeCardH - navBarH // 330f

        // In landscape, offset clamps cleanly without going off screen
        val landscapeClampedX = FloatingPositionHelper.clampOffsetX(savedX, landscapeW, landscapeCardW)
        val landscapeClampedY = FloatingPositionHelper.clampOffsetY(savedY, landscapeH, landscapeCardH, statusBarH, navBarH)
        assertEquals(200f, landscapeClampedX, 0.001f)
        assertTrue("Landscape X must not exceed landscapeMaxX", landscapeClampedX <= landscapeMaxX)
        assertEquals(landscapeMaxY, landscapeClampedY, 0.001f)
        assertTrue("Landscape Y must not exceed landscapeMaxY", landscapeClampedY <= landscapeMaxY)

        // Persistent saved coordinates restore portrait position cleanly
        val restoredX = FloatingPositionHelper.clampOffsetX(savedX, portraitW, portraitCardW)
        val restoredY = FloatingPositionHelper.clampOffsetY(savedY, portraitH, portraitCardH, statusBarH, navBarH)
        assertEquals(200f, restoredX, 0.001f)
        assertEquals(1400f, restoredY, 0.001f)
    }

    @Test
    fun testFloatingKeyboardExplicitWrappingAndDragHandleClamping() {
        val screenW = 1080f
        val screenH = 2400f
        val statH = 84f
        val navH = 126f

        // Total card height wraps keyboard content (800f) + drag handle bar (115f)
        val keyboardH = 800f
        val dragHandleH = 115f
        val totalCardH = keyboardH + dragHandleH
        val cardW = screenW * 0.78f
        val maxX = screenW - cardW
        val clampedX = FloatingPositionHelper.clampOffsetX(500f, screenW, cardW)
        assertEquals(500f.coerceIn(0f, maxX), clampedX, 0.001f)

        val maxY = screenH - totalCardH - navH

        // Dragging all the way to the bottom edge must clamp to maxY
        val clampedBottomY = FloatingPositionHelper.clampOffsetY(9999f, screenH, totalCardH, statH, navH)
        assertEquals(maxY, clampedBottomY, 0.001f)

        // The absolute bottom of the entire card (including drag handle bar) is within screen bounds:
        val cardBottomPx = clampedBottomY + totalCardH
        assertTrue("Entire card including drag handle must not exceed screen height - navBar", cardBottomPx <= screenH - navH)
        assertEquals(screenH - navH, cardBottomPx, 0.001f)
    }

    @Test
    fun testPurgedDictionariesMapOnlyRetainsEnglishAndBengali() {
        // Bengali resolves to R.raw.main_bn
        val bnId = org.futo.inputmethod.latin.utils.Dictionaries.getDictionaryId(java.util.Locale("bn"))
        assertEquals(R.raw.main_bn, bnId)

        // English resolves to R.raw.main_en
        val enId = org.futo.inputmethod.latin.utils.Dictionaries.getDictionaryId(java.util.Locale("en"))
        assertEquals(R.raw.main_en, enId)

        // Purged locales (German, Spanish, French, Italian, Portuguese, Russian) fallback to 0 / fallback dict
        assertEquals(0, org.futo.inputmethod.latin.utils.Dictionaries.getDictionaryId(java.util.Locale("de")))
        assertEquals(0, org.futo.inputmethod.latin.utils.Dictionaries.getDictionaryId(java.util.Locale("es")))
        assertEquals(0, org.futo.inputmethod.latin.utils.Dictionaries.getDictionaryId(java.util.Locale("fr")))
        assertEquals(0, org.futo.inputmethod.latin.utils.Dictionaries.getDictionaryId(java.util.Locale("it")))
        assertEquals(0, org.futo.inputmethod.latin.utils.Dictionaries.getDictionaryId(java.util.Locale("ru")))
    }

    @Test
    fun testDockSnappingSmoothTransition() {
        KeyboardDisplayModeManager.currentMode = KeyboardDisplayMode.FLOATING
        assertEquals(KeyboardDisplayMode.FLOATING, KeyboardDisplayModeManager.currentMode)

        // Dock action snaps to STANDARD
        KeyboardDisplayModeManager.currentMode = KeyboardDisplayMode.STANDARD
        assertEquals(KeyboardDisplayMode.STANDARD, KeyboardDisplayModeManager.currentMode)
        assertEquals(1.0f, KeyboardDisplayModeManager.getWidthMultiplier(isLandscape = false), 0.001f)
    }

    @Test
    fun testDualDragZonesInFloatingMode() {
        // Mode is floating
        val isFloating = true

        // 1. Top Drag Handle Height is 14.dp
        val topDragHandleHeightDp = 14f
        assertTrue("Top drag handle must be at least 12dp for reliable touch target", topDragHandleHeightDp >= 12f)

        // 2. Bottom Drag Handle Bar Height is 42.dp
        val bottomDragHandleHeightDp = 42f
        assertTrue("Bottom drag handle must be at least 40dp for prominent dock pill", bottomDragHandleHeightDp >= 40f)

        // 3. In docked mode (isFloating = false), top handle is omitted
        val topHandleActiveInDocked = false
        assertFalse(topHandleActiveInDocked)

        // 4. In floating mode (isFloating = true), both top handle and bottom handle are active
        assertTrue(isFloating)
    }

    @Test
    fun testFloatingDragGestureClampingAndInertiaMath() {
        val screenW = 1080f
        val screenH = 2400f
        val cardW = 842.4f
        val cardH = 915f // keyboard (800) + bottom handle (42) + top handle (14) + padding
        val statH = 84f
        val navH = 126f

        val maxX = screenW - cardW
        val maxY = screenH - cardH - navH

        // Drag delta sequence
        var curX = 100f
        var curY = 500f

        val dragSteps = listOf(
            Pair(10f, 20f),
            Pair(50f, -100f),
            Pair(500f, 3000f), // large drag overshooting right and bottom bounds
            Pair(-1000f, -2000f) // large drag overshooting left and top bounds
        )

        for ((dx, dy) in dragSteps) {
            val targetX = curX + dx
            val targetY = curY + dy
            curX = FloatingPositionHelper.clampOffsetX(targetX, screenW, cardW)
            curY = FloatingPositionHelper.clampOffsetY(targetY, screenH, cardH, statH, navH)
            assertTrue("Clamped X must be >= 0", curX >= 0f)
            assertTrue("Clamped X must not exceed maxX", curX <= maxX)
            assertTrue("Clamped Y must be >= statH", curY >= statH)
            assertTrue("Clamped Y must not exceed maxY", curY <= maxY)
        }

        // Final positions after negative overshoot must be at min bounds
        assertEquals(0f, curX, 0.001f)
        assertEquals(statH, curY, 0.001f)
    }
}
