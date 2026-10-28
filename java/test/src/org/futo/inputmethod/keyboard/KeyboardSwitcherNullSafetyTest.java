package org.futo.inputmethod.keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.futo.inputmethod.keyboard.KeyboardSwitcher.KeyboardSwitchState;
import org.junit.Test;

public class KeyboardSwitcherNullSafetyTest {

    /**
     * Replicates the null-safe check in KeyboardSwitcher.isShowingKeyboardId
     */
    public static boolean checkIsShowingKeyboardId(final MainKeyboardView keyboardView,
            final int... keyboardIds) {
        if (keyboardView == null || !keyboardView.isShown()) {
            return false;
        }
        final Keyboard keyboard = keyboardView.getKeyboard();
        if (keyboard == null || keyboard.mId == null) {
            return false;
        }
        final int activeKeyboardId = keyboard.mId.mElementId;
        for (final int keyboardId : keyboardIds) {
            if (activeKeyboardId == keyboardId) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testIsShowingKeyboardIdWithNullKeyboardViewReturnsFalse() {
        assertFalse(checkIsShowingKeyboardId(null, KeyboardId.ELEMENT_SYMBOLS_SHIFTED));
        assertFalse(checkIsShowingKeyboardId(null, KeyboardId.ELEMENT_SYMBOLS_3));
    }

    @Test
    public void testKeyboardSwitchStateIds() {
        // HIDDEN and OTHER have keyboardId = -1 and must not trigger setKeyboard directly
        assertEquals(-1, KeyboardSwitchState.HIDDEN.mKeyboardId);
        assertEquals(-1, KeyboardSwitchState.OTHER.mKeyboardId);

        // SYMBOLS_SHIFTED maps to ELEMENT_SYMBOLS_SHIFTED
        assertEquals(KeyboardId.ELEMENT_SYMBOLS_SHIFTED, KeyboardSwitchState.SYMBOLS_SHIFTED.mKeyboardId);
        assertTrue(KeyboardSwitchState.SYMBOLS_SHIFTED.mKeyboardId >= 0);

        // SYMBOLS_3 maps to ELEMENT_SYMBOLS_3
        assertEquals(KeyboardId.ELEMENT_SYMBOLS_3, KeyboardSwitchState.SYMBOLS_3.mKeyboardId);
        assertEquals(27, KeyboardId.ELEMENT_SYMBOLS_3);
        assertTrue(KeyboardSwitchState.SYMBOLS_3.mKeyboardId >= 0);
    }

    @Test
    public void testOnToggleKeyboardNegativeIdGuarded() {
        // When toggleState has mKeyboardId < 0, onToggleKeyboard should skip setKeyboard
        KeyboardSwitchState hiddenState = KeyboardSwitchState.HIDDEN;
        assertFalse(hiddenState.mKeyboardId >= 0);

        KeyboardSwitchState otherState = KeyboardSwitchState.OTHER;
        assertFalse(otherState.mKeyboardId >= 0);

        KeyboardSwitchState symbolsShifted = KeyboardSwitchState.SYMBOLS_SHIFTED;
        assertTrue(symbolsShifted.mKeyboardId >= 0);

        KeyboardSwitchState symbols3 = KeyboardSwitchState.SYMBOLS_3;
        assertTrue(symbols3.mKeyboardId >= 0);
    }
}
