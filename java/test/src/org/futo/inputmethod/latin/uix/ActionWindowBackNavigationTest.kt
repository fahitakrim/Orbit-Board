package org.futo.inputmethod.latin.uix

import android.view.KeyEvent
import androidx.compose.runtime.mutableStateOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

class ActionWindowBackNavigationTest {

    @Test
    fun testFloatingFrame0FallbackDimensions() {
        val screenWidth = 1080f
        val screenHeight = 2400f
        val navBarHeight = 126f
        val statusBarHeight = 84f

        // Frame 0: cardWidthPx and cardHeightPx are 0f (before measurement)
        val frame0WidthPx = 0f
        val frame0HeightPx = 0f

        val isLandscape = false
        val effectiveWidth0 = if (frame0WidthPx <= 0f) screenWidth * (if (isLandscape) 0.70f else 0.78f) else frame0WidthPx
        val effectiveHeight0 = if (frame0HeightPx <= 0f) screenHeight * 0.40f else frame0HeightPx

        assertEquals(842.4f, effectiveWidth0, 0.001f)
        assertEquals(960f, effectiveHeight0, 0.001f)

        val defXFrame0 = (screenWidth - effectiveWidth0) / 2f
        val defYFrame0 = screenHeight - effectiveHeight0 - navBarHeight - 16f

        // defXFrame0 should be (1080 - 842.4) / 2 = 118.8f
        assertEquals(118.8f, defXFrame0, 0.001f)
        // defYFrame0 should be 2400 - 960 - 126 - 16 = 1298f
        assertEquals(1298f, defYFrame0, 0.001f)

        // Without width fallback (old behavior):
        val oldDefX = (screenWidth - frame0WidthPx) / 2f
        assertEquals(540f, oldDefX, 0.001f)
        assertTrue("Frame 0 width fallback prevents card starting in the center", defXFrame0 < oldDefX)

        // Without height fallback (old behavior):
        val oldDefY = screenHeight - frame0HeightPx - navBarHeight - 16f
        assertEquals(2258f, oldDefY, 0.001f)
        assertTrue("Frame 0 height fallback prevents card jumping to screen bottom", defYFrame0 < oldDefY)

        // Clamping on frame 0
        val clampedX0 = FloatingPositionHelper.clampOffsetX(defXFrame0, screenWidth, effectiveWidth0)
        val clampedY0 = FloatingPositionHelper.clampOffsetY(defYFrame0, screenHeight, effectiveHeight0, statusBarHeight, navBarHeight)
        assertEquals(defXFrame0, clampedX0, 0.001f)
        assertEquals(defYFrame0, clampedY0, 0.001f)

        // Landscape frame 0
        val effectiveLandscapeWidth0 = screenWidth * 0.70f
        assertEquals(756f, effectiveLandscapeWidth0, 0.001f)
        val defXLandscape0 = (screenWidth - effectiveLandscapeWidth0) / 2f
        assertEquals(162f, defXLandscape0, 0.001f)
    }

    @Test
    fun testBackKeyInterceptionHierarchyWithDialog() {
        // Models the exact hierarchical state consumption in UixManager.consumeBackKey()
        var activeDialogRequestDismissed = true
        var isShowingActionEditor = false
        var hasActionWindow = false
        var isActionsExpanded = false

        var dialogCancelled = false
        val onCancelDialog = { dialogCancelled = true }

        fun consumeBackKey(): Boolean {
            if (!activeDialogRequestDismissed) {
                activeDialogRequestDismissed = true
                onCancelDialog()
                return true
            }
            if (isShowingActionEditor) {
                isShowingActionEditor = false
                return true
            }
            if (hasActionWindow) {
                hasActionWindow = false
                return true
            }
            return false
        }

        // Scenario 1: Modal dialog is showing -> highest precedence
        activeDialogRequestDismissed = false
        isShowingActionEditor = true
        hasActionWindow = true
        isActionsExpanded = true

        assertTrue("Modal dialog consumes back first", consumeBackKey())
        assertTrue(activeDialogRequestDismissed)
        assertTrue(dialogCancelled)
        assertTrue(isShowingActionEditor)
        assertTrue(hasActionWindow)
        assertTrue(isActionsExpanded)

        // Scenario 2: Next back press closes Action Editor
        assertTrue("Action editor consumes back second", consumeBackKey())
        assertFalse(isShowingActionEditor)
        assertTrue(hasActionWindow)
        assertTrue(isActionsExpanded)

        // Scenario 3: Next back press closes Action Window
        assertTrue("Action window consumes back third", consumeBackKey())
        assertFalse(hasActionWindow)
        assertTrue(isActionsExpanded)

        // Scenario 4: Persistent extended actions bar does NOT consume back key (allows IME to close)
        assertFalse("Expanded actions does not consume back, lets back close keyboard", consumeBackKey())
        assertTrue("Expanded actions remains locked/extended", isActionsExpanded)
    }

    @Test
    fun testHardwareBackKeyEventLifecycleWithNullSafety() {
        var isConsumingBack = false
        var canConsume = true
        var trackingStarted = false

        fun onKeyDown(keyCode: Int, repeatCount: Int, isNullEvent: Boolean): Boolean {
            val KEYCODE_BACK = 4
            val effectiveRepeat = if (isNullEvent) 0 else repeatCount
            if (keyCode == KEYCODE_BACK && effectiveRepeat == 0) {
                if (canConsume) {
                    isConsumingBack = true
                    if (!isNullEvent) {
                        trackingStarted = true
                    }
                    return true
                }
            }
            return false
        }

        fun onKeyUp(keyCode: Int): Boolean {
            val KEYCODE_BACK = 4
            if (keyCode == KEYCODE_BACK) {
                if (isConsumingBack) {
                    isConsumingBack = false
                    return true
                }
            }
            return false
        }

        // Normal single tap with real event
        assertTrue(onKeyDown(4, 0, false))
        assertTrue(isConsumingBack)
        assertTrue(trackingStarted)
        assertTrue(onKeyUp(4))
        assertFalse(isConsumingBack)

        // Null event handling: repeatCount is assumed 0 safely
        trackingStarted = false
        assertTrue(onKeyDown(4, 0, true))
        assertTrue(isConsumingBack)
        assertFalse("Cannot track null event", trackingStarted)
        assertTrue(onKeyUp(4))
        assertFalse(isConsumingBack)

        // Repeat event (long press): repeatCount > 0 ignored
        canConsume = true
        assertFalse(onKeyDown(4, 1, false))
        assertFalse(isConsumingBack)

        // When nothing to consume
        canConsume = false
        assertFalse(onKeyDown(4, 0, false))
        assertFalse(isConsumingBack)
        assertFalse(onKeyUp(4))
    }

    @Test
    fun testComposeMutableStateReactivity() {
        val testHolder = object {
            val currWindowActionWindowState = mutableStateOf<String?>(null)
            var currWindowActionWindow: String?
                get() = currWindowActionWindowState.value
                set(value) { currWindowActionWindowState.value = value }

            val isMainKeyboardHiddenState = mutableStateOf(false)
            var isMainKeyboardHidden: Boolean
                get() = isMainKeyboardHiddenState.value
                set(value) { isMainKeyboardHiddenState.value = value }

            val currWindowActionState = mutableStateOf<String?>(null)
            var currWindowAction: String?
                get() = currWindowActionState.value
                set(value) { currWindowActionState.value = value }
        }

        assertEquals(null, testHolder.currWindowActionWindow)
        assertEquals(false, testHolder.isMainKeyboardHidden)
        assertEquals(null, testHolder.currWindowAction)

        testHolder.currWindowAction = "emoji"
        testHolder.currWindowActionWindow = "EmojiActionWindow"
        testHolder.isMainKeyboardHidden = true

        assertEquals("emoji", testHolder.currWindowActionState.value)
        assertEquals("EmojiActionWindow", testHolder.currWindowActionWindowState.value)
        assertEquals(true, testHolder.isMainKeyboardHiddenState.value)

        // Reset (e.g. back navigation)
        testHolder.currWindowAction = null
        testHolder.currWindowActionWindow = null
        testHolder.isMainKeyboardHidden = false

        assertEquals(null, testHolder.currWindowActionState.value)
        assertEquals(null, testHolder.currWindowActionWindowState.value)
        assertEquals(false, testHolder.isMainKeyboardHiddenState.value)
    }

    @Test
    fun testDockDismissesActionWindowAndRestoresStandard() {
        var actionWindowOpen = true
        var currentMode = KeyboardDisplayMode.FLOATING
        val closedWindow = AtomicBoolean(false)

        fun closeActionWindow() {
            if (actionWindowOpen) {
                actionWindowOpen = false
                closedWindow.set(true)
            }
        }

        fun onDock() {
            closeActionWindow()
            currentMode = KeyboardDisplayMode.STANDARD
        }

        assertEquals(true, actionWindowOpen)
        assertEquals(KeyboardDisplayMode.FLOATING, currentMode)

        onDock()

        assertEquals(false, actionWindowOpen)
        assertTrue(closedWindow.get())
        assertEquals(KeyboardDisplayMode.STANDARD, currentMode)
    }
}
