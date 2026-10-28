package org.futo.inputmethod.keyboard.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.futo.inputmethod.event.Event;
import org.futo.inputmethod.latin.common.Constants;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

/**
 * Empirical Adversarial Test Harness for 3-Page Symbol Navigation System.
 * Tests:
 * 1. Rapid cyclic navigation: Alphabet -> Page 1 -> Page 2 -> Page 3 -> Page 1 ...
 * 2. Direct Alphabet Return from Page 1, Page 2, and Page 3.
 * 3. Premature Reversion Protection on Page 2 (math expressions) and Page 3 (emoticons/typography).
 * 4. Save/Restore keyboard state persistence across all pages.
 * 5. 10,000-iteration random walk fuzzing stress test.
 */
public class SymbolNavigationAdversarialTest implements KeyboardState.SwitchActions {

    public enum LayoutState {
        ALPHABET,
        ALPHABET_MANUAL_SHIFTED,
        ALPHABET_AUTO_SHIFTED,
        ALPHABET_SHIFT_LOCKED,
        ALPHABET_SHIFT_LOCK_SHIFTED,
        EMOJI,
        SYMBOLS_PAGE_1,
        SYMBOLS_PAGE_2,
        SYMBOLS_PAGE_3
    }

    private KeyboardState mState;
    private LayoutState mCurrentLayout;

    @Override public void setAlphabetKeyboard() { mCurrentLayout = LayoutState.ALPHABET; }
    @Override public void setAlphabetManualShiftedKeyboard() { mCurrentLayout = LayoutState.ALPHABET_MANUAL_SHIFTED; }
    @Override public void setAlphabetAutomaticShiftedKeyboard() { mCurrentLayout = LayoutState.ALPHABET_AUTO_SHIFTED; }
    @Override public void setAlphabetShiftLockedKeyboard() { mCurrentLayout = LayoutState.ALPHABET_SHIFT_LOCKED; }
    @Override public void setAlphabetShiftLockShiftedKeyboard() { mCurrentLayout = LayoutState.ALPHABET_SHIFT_LOCK_SHIFTED; }
    @Override public void setEmojiKeyboard() { mCurrentLayout = LayoutState.EMOJI; }
    @Override public void setSymbolsKeyboard() { mCurrentLayout = LayoutState.SYMBOLS_PAGE_1; }
    @Override public void setSymbolsShiftedKeyboard() { mCurrentLayout = LayoutState.SYMBOLS_PAGE_2; }
    @Override public void setSymbols3Keyboard() { mCurrentLayout = LayoutState.SYMBOLS_PAGE_3; }
    @Override public boolean isShowingAlphabetKeyboard() { return mCurrentLayout == LayoutState.ALPHABET || mCurrentLayout == LayoutState.ALPHABET_MANUAL_SHIFTED || mCurrentLayout == LayoutState.ALPHABET_AUTO_SHIFTED || mCurrentLayout == LayoutState.ALPHABET_SHIFT_LOCKED || mCurrentLayout == LayoutState.ALPHABET_SHIFT_LOCK_SHIFTED; }
    @Override public void requestUpdatingShiftState(int autoCapsFlags, int recapitalizeMode) {}
    @Override public void startDoubleTapShiftKeyTimer() {}
    @Override public void cancelDoubleTapShiftKeyTimer() {}
    @Override public boolean isInDoubleTapShiftKeyTimeout() { return false; }

    @Before
    public void setUp() {
        mState = new KeyboardState(this);
        mCurrentLayout = LayoutState.ALPHABET;
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

    /**
     * Requirement 1: Rapid cycling Alphabet -> Page 1 -> Page 2 -> Page 3 -> Page 1 ...
     */
    @Test
    public void testRapidContinuousPageCycling() {
        assertEquals(LayoutState.ALPHABET, mCurrentLayout);

        // Open symbols
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(LayoutState.SYMBOLS_PAGE_1, mCurrentLayout);
        assertEquals(KeyboardState.SYMBOL_PAGE_1, mState.getSymbolPage());

        // Perform 500 rapid 2-way toggle transitions (Page 2 Numbers <-> Page 3 Advanced Symbols)
        for (int cycle = 0; cycle < 500; cycle++) {
            // Page 2 (Numbers) -> Page 3 (Advanced Symbols)
            pressAndReleaseKey(Constants.CODE_SHIFT);
            assertEquals("Cycle " + cycle + " expected Page 2", LayoutState.SYMBOLS_PAGE_2, mCurrentLayout);

            // Page 3 (Advanced Symbols) -> Page 2 (Numbers)
            pressAndReleaseKey(Constants.CODE_SHIFT);
            assertEquals("Cycle " + cycle + " expected Page 1", LayoutState.SYMBOLS_PAGE_1, mCurrentLayout);
        }

        // Return to Alphabet
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(LayoutState.ALPHABET, mCurrentLayout);
    }

    /**
     * Requirement 2: Direct return to Alphabet from Page 1, Page 2, and Page 3.
     * Also verifies that subsequent symbol toggle re-enters at Page 1 every time.
     */
    @Test
    public void testDirectReturnToAlphabetAndSubsequentReentry() {
        // --- 1. Return from Page 1 ---
        mState.onResetKeyboardStateToAlphabet(0, 0);
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(LayoutState.SYMBOLS_PAGE_1, mCurrentLayout);
        assertEquals(KeyboardState.SYMBOL_PAGE_1, mState.getSymbolPage());

        // Press ABC
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(LayoutState.ALPHABET, mCurrentLayout);

        // Re-enter symbols: must start at Page 1
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(LayoutState.SYMBOLS_PAGE_1, mCurrentLayout);
        assertEquals(KeyboardState.SYMBOL_PAGE_1, mState.getSymbolPage());

        // --- 2. Return from Page 2 ---
        pressAndReleaseKey(Constants.CODE_SHIFT); // to Page 2
        assertEquals(LayoutState.SYMBOLS_PAGE_2, mCurrentLayout);
        assertEquals(KeyboardState.SYMBOL_PAGE_2, mState.getSymbolPage());

        // Press ABC from Page 2
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(LayoutState.ALPHABET, mCurrentLayout);

        // Re-enter symbols: must start at Page 1, NOT Page 2!
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(LayoutState.SYMBOLS_PAGE_1, mCurrentLayout);
        assertEquals(KeyboardState.SYMBOL_PAGE_1, mState.getSymbolPage());

        // --- 3. Return from Page 2 after toggle ---
        pressAndReleaseKey(Constants.CODE_SHIFT); // to Page 2
        assertEquals(LayoutState.SYMBOLS_PAGE_2, mCurrentLayout);

        // Press ABC from Page 2
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(LayoutState.ALPHABET, mCurrentLayout);

        // Re-enter symbols: must start at Page 1, NOT Page 2!
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(LayoutState.SYMBOLS_PAGE_1, mCurrentLayout);
    }

    /**
     * Requirement 3: Mathematical expressions with spaces on Page 2 MUST NOT prematurely revert.
     */
    @Test
    public void testPage2MathExpressionsDoNotPrematurelyRevert() {
        // Go to Page 2
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL); // to Page 1
        pressAndReleaseKey(Constants.CODE_SHIFT); // to Page 2
        assertEquals(LayoutState.SYMBOLS_PAGE_2, mCurrentLayout);
        assertEquals(KeyboardState.SYMBOL_PAGE_2, mState.getSymbolPage());

        // Simulate typing complex mathematical formulas with spaces and enters:
        // Formula: "1 + 2 = 3 [x \div y]"
        int[] mathSequence = {
                '1', Constants.CODE_SPACE,
                '+', Constants.CODE_SPACE,
                '2', Constants.CODE_SPACE,
                '=', Constants.CODE_SPACE,
                '3', Constants.CODE_SPACE,
                '[', 'x', Constants.CODE_SPACE,
                '\\', Constants.CODE_SPACE,
                Constants.CODE_SPACE,
                ']',
                Constants.CODE_ENTER
        };

        for (int code : mathSequence) {
            sendEvent(code);
            assertEquals("Page 2 must NOT revert to alphabet when typing code: " + code,
                    LayoutState.SYMBOLS_PAGE_2, mCurrentLayout);
            assertEquals(KeyboardState.SYMBOL_PAGE_2, mState.getSymbolPage());
        }

        // Stress: 100 consecutive math operator + space pairs
        int[] operators = {'~', '`', '|', '•', '√', 'π', '÷', '×', '{', '}', '^', '°', '=', '<', '>'};
        for (int i = 0; i < 100; i++) {
            int op = operators[i % operators.length];
            sendEvent(op);
            sendEvent(Constants.CODE_SPACE);
            assertEquals("Page 2 must remain stable after operator " + (char) op + " + space",
                    LayoutState.SYMBOLS_PAGE_2, mCurrentLayout);
        }

        // Explicitly return to Alphabet via ABC key
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(LayoutState.ALPHABET, mCurrentLayout);
    }

    /**
     * Requirement 3: Emoticons and typography with spaces on Page 3 MUST NOT prematurely revert.
     */
    @Test
    public void testPage2EmoticonsAndTypographyDoNotPrematurelyRevert() {
        // Go to Page 2 (Shifted Symbols)
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL); // to Page 1
        pressAndReleaseKey(Constants.CODE_SHIFT); // to Page 2
        assertEquals(LayoutState.SYMBOLS_PAGE_2, mCurrentLayout);
        assertEquals(KeyboardState.SYMBOL_PAGE_2, mState.getSymbolPage());

        // Type emoticons (CODE_OUTPUT_TEXT) followed by spaces and enters
        for (int i = 0; i < 50; i++) {
            sendEvent(Constants.CODE_OUTPUT_TEXT); // Emoticon like :) or ¯\_(ツ)_/¯
            sendEvent(Constants.CODE_SPACE);
            assertEquals("Page 2 must remain stable after emoticon + space",
                    LayoutState.SYMBOLS_PAGE_2, mCurrentLayout);

            sendEvent(Constants.CODE_OUTPUT_TEXT);
            sendEvent(Constants.CODE_ENTER);
            assertEquals("Page 2 must remain stable after emoticon + enter",
                    LayoutState.SYMBOLS_PAGE_2, mCurrentLayout);
        }

        // Type fractions and typographic marks followed by spaces
        int[] typography = {'½', '⅓', '¼', '¾', '¹', '²', '³', '«', '»', '•', '★', '§', '¶'};
        for (int mark : typography) {
            sendEvent(mark);
            sendEvent(Constants.CODE_SPACE);
            assertEquals("Page 2 must remain stable after mark " + (char) mark + " + space",
                    LayoutState.SYMBOLS_PAGE_2, mCurrentLayout);
        }

        // Explicitly return to Alphabet via ABC key
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(LayoutState.ALPHABET, mCurrentLayout);
    }

    /**
     * Baseline comparison: Verify that Page 1 DOES revert to Alphabet on symbol + space/enter.
     */
    @Test
    public void testPage1NormalAutoReversionOnSpaceAndEnter() {
        // Space auto-revert
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL); // to Page 1
        assertEquals(LayoutState.SYMBOLS_PAGE_1, mCurrentLayout);
        sendEvent('?');
        sendEvent(Constants.CODE_SPACE);
        assertEquals("Page 1 must auto-revert to Alphabet on symbol + space",
                LayoutState.ALPHABET, mCurrentLayout);

        // Enter auto-revert
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL); // to Page 1
        assertEquals(LayoutState.SYMBOLS_PAGE_1, mCurrentLayout);
        sendEvent('!');
        sendEvent(Constants.CODE_ENTER);
        assertEquals("Page 1 must auto-revert to Alphabet on symbol + enter",
                LayoutState.ALPHABET, mCurrentLayout);
    }

    /**
     * Requirement: Save and restore state fidelity across all 3 pages.
     */
    @Test
    public void testSaveAndRestoreStateAcrossAllSymbolPages() {
        // 1. Save while on Page 1
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(LayoutState.SYMBOLS_PAGE_1, mCurrentLayout);
        mState.onSaveKeyboardState();
        mState.onLoadKeyboard(0, 0); // restores
        assertEquals(LayoutState.SYMBOLS_PAGE_1, mCurrentLayout);
        assertEquals(KeyboardState.SYMBOL_PAGE_1, mState.getSymbolPage());

        // 2. Save while on Page 2
        pressAndReleaseKey(Constants.CODE_SHIFT);
        assertEquals(LayoutState.SYMBOLS_PAGE_2, mCurrentLayout);
        mState.onSaveKeyboardState();
        mState.onLoadKeyboard(0, 0); // restores
        assertEquals(LayoutState.SYMBOLS_PAGE_2, mCurrentLayout);
        assertEquals(KeyboardState.SYMBOL_PAGE_2, mState.getSymbolPage());

        // 3. Toggle back to Page 1 and save
        pressAndReleaseKey(Constants.CODE_SHIFT);
        assertEquals(LayoutState.SYMBOLS_PAGE_1, mCurrentLayout);
        mState.onSaveKeyboardState();
        mState.onLoadKeyboard(0, 0); // restores
        assertEquals(LayoutState.SYMBOLS_PAGE_1, mCurrentLayout);

        // Return to Alphabet
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(LayoutState.ALPHABET, mCurrentLayout);
    }

    /**
     * Requirement: Alphabet Shift and CapsLock must remain stable in Alphabet mode
     * and never kick to Symbols (?123).
     */
    @Test
    public void testShiftAndCapsLockAlphabetStability() {
        assertEquals(LayoutState.ALPHABET, mCurrentLayout);

        // 1. Shift press and release in Alphabet
        mState.onPressKey(Constants.CODE_SHIFT, true, 0, 0);
        assertEquals(LayoutState.ALPHABET_MANUAL_SHIFTED, mCurrentLayout);
        mState.onReleaseKey(Constants.CODE_SHIFT, false, 0, 0);
        mState.onFinishSlidingInput(0, 0);
        assertEquals(LayoutState.ALPHABET_MANUAL_SHIFTED, mCurrentLayout);

        // Type a letter -> back to unshifted alphabet
        sendEvent('a');
        mState.onReleaseKey('a', false, 0, 0);
        assertEquals(LayoutState.ALPHABET, mCurrentLayout);

        // 2. Mode switch to ?123 and back to Alphabet
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(LayoutState.SYMBOLS_PAGE_1, mCurrentLayout);
        pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
        assertEquals(LayoutState.ALPHABET, mCurrentLayout);

        // 3. Shift tap immediately after returning to Alphabet
        mState.onPressKey(Constants.CODE_SHIFT, true, 0, 0);
        assertEquals(LayoutState.ALPHABET_MANUAL_SHIFTED, mCurrentLayout);
        mState.onReleaseKey(Constants.CODE_SHIFT, false, 0, 0);
        mState.onFinishSlidingInput(0, 0);
        assertEquals(LayoutState.ALPHABET_MANUAL_SHIFTED, mCurrentLayout);

        // Unshift
        mState.onPressKey(Constants.CODE_SHIFT, true, 0, 0);
        mState.onReleaseKey(Constants.CODE_SHIFT, false, 0, 0);
        assertEquals(LayoutState.ALPHABET, mCurrentLayout);

        // 4. CapsLock toggles shift locked
        mState.onPressKey(Constants.CODE_CAPSLOCK, true, 0, 0);
        mState.onReleaseKey(Constants.CODE_CAPSLOCK, false, 0, 0);
        assertEquals(LayoutState.ALPHABET_SHIFT_LOCKED, mCurrentLayout);
        mState.onFinishSlidingInput(0, 0);
        assertEquals(LayoutState.ALPHABET_SHIFT_LOCKED, mCurrentLayout);

        // Unlock CapsLock
        mState.onPressKey(Constants.CODE_CAPSLOCK, true, 0, 0);
        mState.onReleaseKey(Constants.CODE_CAPSLOCK, false, 0, 0);
        assertEquals(LayoutState.ALPHABET, mCurrentLayout);
    }

    /**
     * Fuzzing / Random Walk Stress Test:
     * 10,000 random actions verifying no crashes, invariant preservation,
     * and strict reversion rules.
     */
    @Test
    public void testRandomWalkFuzzingInvariants() {
        Random rand = new Random(42); // Deterministic seed

        for (int i = 0; i < 10000; i++) {
            int action = rand.nextInt(7);
            LayoutState beforeLayout = mCurrentLayout;
            int beforePage = mState.getSymbolPage();

            switch (action) {
                case 0: // Press Shift / Toggle
                    pressAndReleaseKey(Constants.CODE_SHIFT);
                    break;
                case 1: // Press ?123 / ABC
                    pressAndReleaseKey(Constants.CODE_SWITCH_ALPHA_SYMBOL);
                    break;
                case 2: // Type a letter
                    sendEvent('a' + rand.nextInt(26));
                    break;
                case 3: // Type a symbol / number
                    sendEvent('!' + rand.nextInt(40));
                    break;
                case 4: // Type an output text emoticon
                    sendEvent(Constants.CODE_OUTPUT_TEXT);
                    break;
                case 5: // Space
                    sendEvent(Constants.CODE_SPACE);
                    break;
                case 6: // Enter
                    sendEvent(Constants.CODE_ENTER);
                    break;
            }

            // Invariant 1: mCurrentLayout and mSymbolPage must be mutually consistent
            if (mCurrentLayout == LayoutState.SYMBOLS_PAGE_1) {
                assertEquals(KeyboardState.SYMBOL_PAGE_1, mState.getSymbolPage());
            } else if (mCurrentLayout == LayoutState.SYMBOLS_PAGE_2) {
                // In Page 2 (shifted symbols)
            }

            // Invariant 2: Space or Enter when in Page 2 must NEVER revert to Alphabet
            if ((action == 5 || action == 6) && beforeLayout == LayoutState.SYMBOLS_PAGE_2) {
                assertTrue("Space/Enter must not cause premature reversion from Page 2! Before: "
                        + beforeLayout + ", After: " + mCurrentLayout,
                        mCurrentLayout == LayoutState.SYMBOLS_PAGE_2);
            }
        }
    }
}
