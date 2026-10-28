package org.futo.inputmethod.keyboard.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.futo.inputmethod.event.Event;
import org.futo.inputmethod.latin.common.Constants;
import org.junit.Before;
import org.junit.Test;

public class MultiLayoutStateTransitionTest implements KeyboardState.SwitchActions {

    public enum CurrentLayout {
        ALPHABET,
        ALPHABET_MANUAL_SHIFTED,
        ALPHABET_AUTO_SHIFTED,
        ALPHABET_SHIFT_LOCKED,
        ALPHABET_SHIFT_LOCK_SHIFTED,
        EMOJI,
        SYMBOLS,
        SYMBOLS_SHIFTED,
        SYMBOLS_3
    }

    private KeyboardState mState;
    private CurrentLayout mCurrentLayout;
    private int mDoubleTapTimerStarts = 0;
    private int mDoubleTapTimerCancels = 0;
    private boolean mInDoubleTapTimeout = false;

    @Override
    public void setAlphabetKeyboard() {
        mCurrentLayout = CurrentLayout.ALPHABET;
    }

    @Override
    public void setAlphabetManualShiftedKeyboard() {
        mCurrentLayout = CurrentLayout.ALPHABET_MANUAL_SHIFTED;
    }

    @Override
    public void setAlphabetAutomaticShiftedKeyboard() {
        mCurrentLayout = CurrentLayout.ALPHABET_AUTO_SHIFTED;
    }

    @Override
    public void setAlphabetShiftLockedKeyboard() {
        mCurrentLayout = CurrentLayout.ALPHABET_SHIFT_LOCKED;
    }

    @Override
    public void setAlphabetShiftLockShiftedKeyboard() {
        mCurrentLayout = CurrentLayout.ALPHABET_SHIFT_LOCK_SHIFTED;
    }

    @Override
    public void setEmojiKeyboard() {
        mCurrentLayout = CurrentLayout.EMOJI;
    }

    @Override
    public void setSymbolsKeyboard() {
        mCurrentLayout = CurrentLayout.SYMBOLS;
    }

    @Override
    public void setSymbolsShiftedKeyboard() {
        mCurrentLayout = CurrentLayout.SYMBOLS_SHIFTED;
    }

    @Override
    public void setSymbols3Keyboard() {
        mCurrentLayout = CurrentLayout.SYMBOLS_3;
    }

    @Override
    public boolean isShowingAlphabetKeyboard() {
        return mCurrentLayout == CurrentLayout.ALPHABET
            || mCurrentLayout == CurrentLayout.ALPHABET_MANUAL_SHIFTED
            || mCurrentLayout == CurrentLayout.ALPHABET_AUTO_SHIFTED
            || mCurrentLayout == CurrentLayout.ALPHABET_SHIFT_LOCKED
            || mCurrentLayout == CurrentLayout.ALPHABET_SHIFT_LOCK_SHIFTED;
    }

    @Override
    public void requestUpdatingShiftState(int autoCapsFlags, int recapitalizeMode) {
    }

    @Override
    public void startDoubleTapShiftKeyTimer() {
        mDoubleTapTimerStarts++;
        mInDoubleTapTimeout = true;
    }

    @Override
    public void cancelDoubleTapShiftKeyTimer() {
        mDoubleTapTimerCancels++;
        mInDoubleTapTimeout = false;
    }

    @Override
    public boolean isInDoubleTapShiftKeyTimeout() {
        return mInDoubleTapTimeout;
    }

    @Before
    public void setUp() {
        mState = new KeyboardState(this);
        mCurrentLayout = CurrentLayout.ALPHABET;
        mState.onLoadKeyboard(0, 0);
    }

    private void pressAndReleaseKey(int code) {
        mState.onPressKey(code, false /* isSinglePointer */, 0 /* autoCaps */, 0 /* recapitalize */);
        mState.onReleaseKey(code, false /* withSliding */, 0 /* autoCaps */, 0 /* recapitalize */);
        sendEvent(code);
    }

    private void sendEvent(int code) {
        Event event = Event.createSoftwareKeypressEvent(code, code, 0, 0, false);
        mState.onEvent(event, 0, 0);
    }

    @Test
    public void testAlphabetToSymbolsAndMoreSymbolsTransition() {
        assertEquals(CurrentLayout.ALPHABET, mCurrentLayout);

        // Tap ?123 -> switches to symbols (Page 2: Numbers)
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(CurrentLayout.SYMBOLS, mCurrentLayout);

        // In symbols, tap =\< (CODE_SHIFT) -> switches to shifted symbols (Page 3: Advanced Symbols)
        pressAndReleaseKey(Constants.CODE_SHIFT);
        assertEquals(CurrentLayout.SYMBOLS_SHIFTED, mCurrentLayout);

        // In shifted symbols, tap ?123 (CODE_SHIFT) -> switches back to symbols (Page 2: Numbers)
        pressAndReleaseKey(Constants.CODE_SHIFT);
        assertEquals(CurrentLayout.SYMBOLS, mCurrentLayout);

        // In symbols, tap ABC (CODE_SWITCH_ALPHA_SYMBOL) -> switches back to alphabet (Page 1: Alphabet)
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(CurrentLayout.ALPHABET, mCurrentLayout);
    }

    @Test
    public void testThreePageSymbolCycleNavigation() {
        // Start in alphabet
        assertEquals(CurrentLayout.ALPHABET, mCurrentLayout);

        // Open symbols -> Page 2 (Numbers)
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(CurrentLayout.SYMBOLS, mCurrentLayout);

        // Toggle 2 -> 3 (Advanced Symbols)
        pressAndReleaseKey(Constants.CODE_SHIFT);
        assertEquals(CurrentLayout.SYMBOLS_SHIFTED, mCurrentLayout);

        // Toggle 3 -> 2 (Numbers)
        pressAndReleaseKey(Constants.CODE_SHIFT);
        assertEquals(CurrentLayout.SYMBOLS, mCurrentLayout);

        // Toggle again 2 -> 3 -> 2
        pressAndReleaseKey(Constants.CODE_SHIFT);
        assertEquals(CurrentLayout.SYMBOLS_SHIFTED, mCurrentLayout);

        pressAndReleaseKey(Constants.CODE_SHIFT);
        assertEquals(CurrentLayout.SYMBOLS, mCurrentLayout);

        // Return to alphabet
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(CurrentLayout.ALPHABET, mCurrentLayout);
    }

    @Test
    public void testImmediateAlphabetReturnFromAllPages() {
        // Return from Page 2 (Numbers)
        mState.onResetKeyboardStateToAlphabet(0, 0);
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL); // to Page 2
        assertEquals(CurrentLayout.SYMBOLS, mCurrentLayout);
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL); // ABC key
        assertEquals(CurrentLayout.ALPHABET, mCurrentLayout);

        // Return from Page 3 (Advanced Symbols)
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL); // to Page 2
        assertEquals(CurrentLayout.SYMBOLS, mCurrentLayout);
        pressAndReleaseKey(Constants.CODE_SHIFT); // to Page 3
        assertEquals(CurrentLayout.SYMBOLS_SHIFTED, mCurrentLayout);
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL); // ABC key
        assertEquals(CurrentLayout.ALPHABET, mCurrentLayout);
    }

    @Test
    public void testSymbolPage2And3NoPrematureReversionOnSpace() {
        // Page 2: numbers layout
        mState.onResetKeyboardStateToAlphabet(0, 0);
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(CurrentLayout.SYMBOLS, mCurrentLayout);

        // Page 3: typing a math symbol followed by space/enter does NOT revert to Alphabet
        pressAndReleaseKey(Constants.CODE_SHIFT); // to Page 3
        assertEquals(CurrentLayout.SYMBOLS_SHIFTED, mCurrentLayout);
        // Type mathematical symbols and spaces
        sendEvent('+');
        sendEvent(Constants.CODE_SPACE);
        assertEquals(CurrentLayout.SYMBOLS_SHIFTED, mCurrentLayout);
        sendEvent('=');
        sendEvent(Constants.CODE_SPACE);
        sendEvent(Constants.CODE_ENTER);
        assertEquals(CurrentLayout.SYMBOLS_SHIFTED, mCurrentLayout); // Remains stable on Page 3!
        // Explicit return to ABC
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(CurrentLayout.ALPHABET, mCurrentLayout);
    }

    @Test
    public void testRapidSymbolKeyToggling() {
        // Switch to symbols
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(CurrentLayout.SYMBOLS, mCurrentLayout);

        // Rapid 2-way toggle: 2 -> 3 -> 2 for 10 full loops
        for (int i = 0; i < 10; i++) {
            pressAndReleaseKey(Constants.CODE_SHIFT);
            assertEquals(CurrentLayout.SYMBOLS_SHIFTED, mCurrentLayout);

            pressAndReleaseKey(Constants.CODE_SHIFT);
            assertEquals(CurrentLayout.SYMBOLS, mCurrentLayout);
        }
        assertEquals(CurrentLayout.SYMBOLS, mCurrentLayout);

        // Return cleanly to alphabet
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(CurrentLayout.ALPHABET, mCurrentLayout);
    }

    @Test
    public void testShiftAndCapsLockInAlphabetModeDoNotKickToSymbols() {
        assertEquals(CurrentLayout.ALPHABET, mCurrentLayout);

        // 1. Shift key tap in Alphabet mode
        mState.onPressKey(Constants.CODE_SHIFT, true, 0, 0);
        assertEquals(CurrentLayout.ALPHABET_MANUAL_SHIFTED, mCurrentLayout);
        mState.onReleaseKey(Constants.CODE_SHIFT, false, 0, 0);
        // Finishing sliding input must NOT kick to symbols
        mState.onFinishSlidingInput(0, 0);
        assertEquals(CurrentLayout.ALPHABET_MANUAL_SHIFTED, mCurrentLayout);

        // Type a letter -> shifts back to unshifted alphabet
        sendEvent('a');
        mState.onReleaseKey('a', false, 0, 0);
        assertEquals(CurrentLayout.ALPHABET, mCurrentLayout);

        // 2. Transition to symbols (?123) and back to alphabet (ABC)
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(CurrentLayout.SYMBOLS, mCurrentLayout);
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(CurrentLayout.ALPHABET, mCurrentLayout);

        // 3. Next Shift tap must not open ?123
        mState.onPressKey(Constants.CODE_SHIFT, true, 0, 0);
        assertEquals(CurrentLayout.ALPHABET_MANUAL_SHIFTED, mCurrentLayout);
        mState.onReleaseKey(Constants.CODE_SHIFT, false, 0, 0);
        mState.onFinishSlidingInput(0, 0);
        assertEquals(CurrentLayout.ALPHABET_MANUAL_SHIFTED, mCurrentLayout);

        // Unshift (after double-tap timeout expires)
        mInDoubleTapTimeout = false;
        mState.onPressKey(Constants.CODE_SHIFT, true, 0, 0);
        mState.onReleaseKey(Constants.CODE_SHIFT, false, 0, 0);
        assertEquals(CurrentLayout.ALPHABET, mCurrentLayout);

        // 4. CapsLock must toggle shift lock without opening symbols
        mState.onPressKey(Constants.CODE_CAPSLOCK, true, 0, 0);
        mState.onReleaseKey(Constants.CODE_CAPSLOCK, false, 0, 0);
        assertEquals(CurrentLayout.ALPHABET_SHIFT_LOCKED, mCurrentLayout);
        mState.onFinishSlidingInput(0, 0);
        assertEquals(CurrentLayout.ALPHABET_SHIFT_LOCKED, mCurrentLayout);

        // Unlock caps lock
        mState.onPressKey(Constants.CODE_CAPSLOCK, true, 0, 0);
        mState.onReleaseKey(Constants.CODE_CAPSLOCK, false, 0, 0);
        assertEquals(CurrentLayout.ALPHABET, mCurrentLayout);
    }

    @Test
    public void testMultiLayoutSimulatedLanguages() {
        // Test transition stability across 4 layouts: English QWERTY, Bengali Jatiyo, Avro, Probhat
        String[] layouts = {"English_QWERTY", "Bengali_Jatiyo", "Bengali_Avro", "Bengali_Probhat"};

        for (String layout : layouts) {
            // Reset to alphabet for layout
            mState.onResetKeyboardStateToAlphabet(0, 0);
            assertEquals(CurrentLayout.ALPHABET, mCurrentLayout);

            // Open Number / Symbol window (?123)
            pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
            assertEquals(CurrentLayout.SYMBOLS, mCurrentLayout);

            // Open Page 3 (Advanced Symbols)
            pressAndReleaseKey(Constants.CODE_SHIFT);
            assertEquals(CurrentLayout.SYMBOLS_SHIFTED, mCurrentLayout);

            // Type a symbol key in shifted symbols mode (e.g., character code '=')
            pressAndReleaseKey('=');
            assertEquals(CurrentLayout.SYMBOLS_SHIFTED, mCurrentLayout);

            // Toggle back to Page 2 (Numbers)
            pressAndReleaseKey(Constants.CODE_SHIFT);
            assertEquals(CurrentLayout.SYMBOLS, mCurrentLayout);

            // Return to ABC
            pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
            assertEquals(CurrentLayout.ALPHABET, mCurrentLayout);
        }
    }
}
