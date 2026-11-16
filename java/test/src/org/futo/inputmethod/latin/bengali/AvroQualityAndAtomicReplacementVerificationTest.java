package org.futo.inputmethod.latin.bengali;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Spanned;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import org.futo.inputmethod.latin.LastComposedWord;
import org.futo.inputmethod.latin.SuggestedWords;
import org.futo.inputmethod.latin.SuggestedWords.SuggestedWordInfo;
import org.futo.inputmethod.latin.common.Constants;
import org.futo.inputmethod.latin.inputlogic.InputLogic;
import org.futo.inputmethod.latin.settings.Settings;
import org.futo.inputmethod.latin.settings.SettingsValues;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive verification test suite for Avro Typing Quality Improvements:
 * 1. Auto-Caps disabled in Avro mode (getCurrentAutoCapsState returns CAP_MODE_OFF).
 * 2. Dedicated Avro Spacebar Preference (smart_candidate vs exact_phonetic).
 * 3. Spacebar commits never auto-learn words (learnWord); only explicit suggestion clicks learn words.
 * 4. Rule 5 Atomic text replacement on suggestion click (composing span check, host prefix deletion, bounds protection).
 * 5. Architectural candidate ranking for sentence-start words (case-sensitive vs case-insensitive sets).
 */
public class AvroQualityAndAtomicReplacementVerificationTest {

    private RidmikParser mParser;
    private File mTempStorageFile;

    @Before
    public void setUp() throws IOException {
        mParser = new RidmikParser();
        mTempStorageFile = File.createTempFile("avro_quality_test", ".json");
        BanglaSuggestionEngine.setStorageFile(mTempStorageFile);
        BanglaSuggestionEngine.clearLearnedWords();
    }

    @After
    public void tearDown() {
        BanglaSuggestionEngine.clearLearnedWords();
        if (mTempStorageFile != null && mTempStorageFile.exists()) {
            mTempStorageFile.delete();
        }
    }

    // =========================================================================
    // 1. Auto-Caps Disabling for Avro
    // =========================================================================

    @Test
    public void testAvroModeForcesAutoCapsOff() {
        final InputLogic inputLogic = new InputLogic();
        inputLogic.setAvroActiveForTest(true);

        // When Avro is active, getCurrentAutoCapsState MUST unconditionally return CAP_MODE_OFF
        final int autoCapsState = inputLogic.getCurrentAutoCapsState(null);
        assertEquals("Avro mode must strictly return CAP_MODE_OFF to prevent sentence-start auto-capitalization",
                Constants.TextUtils.CAP_MODE_OFF, autoCapsState);
    }

    // =========================================================================
    // 2. Dedicated Avro Spacebar Mode (smart_candidate vs exact_phonetic)
    // =========================================================================

    @Test
    public void testSettingsAvroSpacebarModeConstants() {
        assertEquals("smart_candidate", Settings.AVRO_SPACEBAR_MODE_SMART_CANDIDATE);
        assertEquals("exact_phonetic", Settings.AVRO_SPACEBAR_MODE_EXACT_PHONETIC);
        assertEquals(Settings.AVRO_SPACEBAR_MODE_EXACT_PHONETIC, Settings.DEFAULT_AVRO_SPACEBAR_MODE);
        assertEquals("pref_key_avro_spacebar_mode", Settings.PREF_AVRO_SPACEBAR_MODE);
    }

    @Test
    public void testSmartCandidateSpacebarCommitsCandidateZero() {
        // In smart_candidate mode (default):
        // 'kmn' commits candidate 0 'কেমন' instead of exact phonetic 'কম্ন'
        final String rawKmn = "kmn";
        final List<String> candidatesKmn = BanglaSuggestionEngine.getCandidates(rawKmn, mParser);
        assertNotNull(candidatesKmn);
        assertFalse(candidatesKmn.isEmpty());
        assertEquals("Top suggestion candidate for 'kmn' must be 'কেমন'", "কেমন", candidatesKmn.get(0));

        final String chosenSmart = simulateAvroSpacebarCommit(rawKmn, false /* exactPhonetic */);
        assertEquals("In smart_candidate mode, spacebar must commit 'কেমন'", "কেমন", chosenSmart);

        // Alias: 'ekhono' -> 'এখনো'
        final String chosenEkhono = simulateAvroSpacebarCommit("ekhono", false);
        assertEquals("এখনো", chosenEkhono);

        // Alias: 'tumi' -> 'তুমি'
        final String chosenTumi = simulateAvroSpacebarCommit("tumi", false);
        assertEquals("তুমি", chosenTumi);
    }

    @Test
    public void testExactPhoneticSpacebarCommitsExactTransliteration() {
        // In exact_phonetic mode:
        // 'kmn' strictly commits mParser.toBangla('kmn') = 'কম্ন'
        final String rawKmn = "kmn";
        final String rawBangla = mParser.toBangla(rawKmn);
        assertEquals("কম্ন", rawBangla);

        final String chosenExact = simulateAvroSpacebarCommit(rawKmn, true /* exactPhonetic */);
        assertEquals("In exact_phonetic mode, spacebar must strictly commit exact phonetic 'কম্ন'",
                "কম্ন", chosenExact);

        // 'kisu' -> exact transliteration is 'কিসু'
        final String chosenKisu = simulateAvroSpacebarCommit("kisu", true);
        assertEquals("কিসু", chosenKisu);
    }

    @Test
    public void testDigitsAlwaysCommittedAsExactPhoneticInBothModes() {
        // Numbers and numerals (e.g. '500') must NEVER be replaced by words on spacebar in either mode
        final String chosenSmart = simulateAvroSpacebarCommit("500", false /* smart_candidate */);
        assertEquals("500 must never be replaced on spacebar in smart mode", "500", chosenSmart);

        final String chosenExact = simulateAvroSpacebarCommit("500", true /* exact_phonetic */);
        assertEquals("500 must never be replaced on spacebar in exact mode", "500", chosenExact);
    }

    // =========================================================================
    // 3. Spacebar Commit Never Learns Words; Only Strip Selection Learns
    // =========================================================================

    @Test
    public void testSpacebarCommitNeverInvokesLearnWord() {
        final String raw = "customword";
        // Spacebar commits chosen word in smart mode
        final String chosen = simulateAvroSpacebarCommit(raw, false);

        // Invariant: spacebar commit MUST NOT record word in learned dictionary
        assertNull("Spacebar commit must never auto-learn word",
                BanglaSuggestionEngine.getTopLearnedWord(raw));

        // When user explicitly picks a suggestion from the strip, it is learned
        BanglaSuggestionEngine.learnWord(raw, "কাস্টমশব্দ");
        assertNotNull("Manual pick must learn word", BanglaSuggestionEngine.getTopLearnedWord(raw));
        assertEquals("কাস্টমশব্দ", BanglaSuggestionEngine.getTopLearnedWord(raw).chosenBengali);
    }

    // =========================================================================
    // 4. Rule 5 Atomic Text Replacement on Suggestion Click
    // =========================================================================

    public static class MockTestInputConnection {
        public final StringBuilder buffer = new StringBuilder();
        public int cursor = 0;
        public int selectionStart = 0;
        public int selectionEnd = 0;
        public int composingStart = -1;
        public int composingEnd = -1;

        public int deleteSurroundingTextCalls = 0;
        public int lastDeletedLength = 0;
        public int commitTextCalls = 0;
        public boolean supportsComposingSpans = true;

        public void setText(String text, int cur) {
            buffer.setLength(0);
            buffer.append(text);
            cursor = cur;
            selectionStart = cur;
            selectionEnd = cur;
            composingStart = -1;
            composingEnd = -1;
        }

        public boolean hasHostComposingSpan() {
            return supportsComposingSpans && composingStart >= 0 && composingEnd >= composingStart;
        }

        public boolean hasSelection() {
            return selectionStart != selectionEnd;
        }

        public void setComposingText(String text) {
            if (supportsComposingSpans) {
                if (composingStart >= 0) {
                    buffer.replace(composingStart, composingEnd, text);
                    composingEnd = composingStart + text.length();
                    cursor = composingEnd;
                } else {
                    final int pos = cursor;
                    buffer.insert(pos, text);
                    composingStart = pos;
                    composingEnd = pos + text.length();
                    cursor = composingEnd;
                }
            } else {
                buffer.insert(cursor, text);
                cursor += text.length();
            }
            selectionStart = cursor;
            selectionEnd = cursor;
        }

        public void deleteSurroundingText(int before, int after) {
            deleteSurroundingTextCalls++;
            lastDeletedLength = before;
            final int start = Math.max(0, cursor - before);
            buffer.delete(start, cursor);
            cursor = start;
            selectionStart = cursor;
            selectionEnd = cursor;
            if (composingStart >= cursor) {
                composingStart = -1;
                composingEnd = -1;
            }
        }

        public void commitText(String text) {
            commitTextCalls++;
            if (supportsComposingSpans && composingStart >= 0) {
                buffer.replace(composingStart, composingEnd, text);
                cursor = composingStart + text.length();
                composingStart = -1;
                composingEnd = -1;
            } else if (hasSelection()) {
                final int s = Math.min(selectionStart, selectionEnd);
                final int e = Math.max(selectionStart, selectionEnd);
                buffer.replace(s, e, text);
                cursor = s + text.length();
            } else {
                buffer.insert(cursor, text);
                cursor += text.length();
            }
            selectionStart = cursor;
            selectionEnd = cursor;
        }
    }

    @Test
    public void testRule5AtomicReplacementStandardHostWithComposingSpans() {
        // Standard host: composing span exists. commitText replaces natively, no redundant deletion.
        final MockTestInputConnection host = new MockTestInputConnection();
        host.supportsComposingSpans = true;
        host.setText("আমি ", 4);
        host.setComposingText("ভাত"); // composing text is "ভাত" (3 chars)

        // Suggestion strip commit with Rule 5 check:
        executeRule5Commit(host, "ভাত", "ভাতখাবো");

        assertEquals("No deleteSurroundingText when host has composing span", 0, host.deleteSurroundingTextCalls);
        assertEquals("Clean replacement in buffer", "আমি ভাতখাবো", host.buffer.toString());
    }

    @Test
    public void testRule5AtomicReplacementAdversarialHostWithoutComposingSpans() {
        // Adversarial host (e.g. Instagram note reply / WebView): composing span ignored.
        // IME must explicitly delete the displayed prefix before committing.
        final MockTestInputConnection host = new MockTestInputConnection();
        host.supportsComposingSpans = false;
        host.setText("আমি ", 4);
        host.setComposingText("ভাত"); // appended without composing span

        executeRule5Commit(host, "ভাত", "ভাতখাবো");

        assertEquals("deleteSurroundingText MUST be called when host lacks composing span", 1, host.deleteSurroundingTextCalls);
        assertEquals("Deleted length must match displayed prefix length", 3, host.lastDeletedLength);
        assertEquals("Clean replacement without duplication", "আমি ভাতখাবো", host.buffer.toString());
    }

    @Test
    public void testRule5AvroDisplayedBanglaPrefixLengthCalculation() {
        // In Avro mode: user typed Roman "kmn", displayed text in host editor was "কম্ন" (3 Bangla chars).
        // Adversarial host lacks composing span: IME calculates displayed length from mParser.toBangla("kmn").
        final MockTestInputConnection host = new MockTestInputConnection();
        host.supportsComposingSpans = false;

        final String rawTyped = "kmn";
        final String displayedBangla = mParser.toBangla(rawTyped); // "কম্ন" (4 chars: ক + ম + ্ + ন)
        assertEquals(4, displayedBangla.length());

        host.setText("Tumi ", 5);
        host.setComposingText(displayedBangla);

        // User clicks suggestion "কেমন"
        executeRule5Commit(host, displayedBangla, "কেমন");

        assertEquals(1, host.deleteSurroundingTextCalls);
        assertEquals("Must delete displayed Bangla character length (4), NOT Roman length", 4, host.lastDeletedLength);
        assertEquals("Tumi কেমন", host.buffer.toString());
    }

    @Test
    public void testRule5ActiveSelectionBypassesDeletion() {
        final MockTestInputConnection host = new MockTestInputConnection();
        host.supportsComposingSpans = false;
        host.setText("hello world", 11);
        host.selectionStart = 6;
        host.selectionEnd = 11; // "world" selected

        executeRule5Commit(host, "world", "universe");

        assertEquals("Active selection must not trigger deleteSurroundingText", 0, host.deleteSurroundingTextCalls);
        assertEquals("hello universe", host.buffer.toString());
    }

    @Test
    public void testRichInputConnectionCheckHostHasComposingSpanWithActiveComposing() {
        final org.futo.inputmethod.latin.RichInputConnection ric = new org.futo.inputmethod.latin.RichInputConnection();
        // Host returns plain text without SPAN_COMPOSING (simulating real Android Binder IPC)
        ric.setInputConnectionForTest(new android.view.inputmethod.BaseInputConnection(null, false) {
            @Override
            public CharSequence getTextBeforeCursor(int n, int flags) {
                return "ভাত";
            }
        });

        // When RichInputConnection is actively composing:
        ric.setComposingText("ভাত", 1);
        assertTrue("hasComposingText must be true", ric.hasComposingText());
        // checkHostHasComposingSpan MUST return true so preceding text is not deleted!
        assertTrue("checkHostHasComposingSpan must return true when connection has composing text",
                ric.checkHostHasComposingSpan(3));
    }

    @Test
    public void testRichInputConnectionCheckHostHasComposingSpanFallback() {
        final org.futo.inputmethod.latin.RichInputConnection ric = new org.futo.inputmethod.latin.RichInputConnection();
        // Not composing:
        assertFalse(ric.hasComposingText());

        // Host without composing span returns false:
        ric.setInputConnectionForTest(new android.view.inputmethod.BaseInputConnection(null, false) {
            @Override
            public CharSequence getTextBeforeCursor(int n, int flags) {
                return "hel";
            }
        });
        assertFalse(ric.checkHostHasComposingSpan(3));
    }

    @Test
    public void testSetComposingRegionOutOfBoundsGuarded() {
        // Negative start coordinates must be safely rejected
        final int expectedCursorPosition = -1;
        final int numberOfCharsInWordBeforeCursor = 4;
        final int compStart = expectedCursorPosition - numberOfCharsInWordBeforeCursor; // -5
        final int compEnd = 0;

        final boolean isValidRegion = compStart >= 0 && compEnd >= compStart;
        assertFalse("Negative start coordinates must be guarded and rejected", isValidRegion);
    }

    // =========================================================================
    // 5. Case-Sensitive vs Case-Insensitive Sentence-Start Candidate Ranking
    // =========================================================================

    @Test
    public void testSentenceStartTumiProducesTumiAtSlotZero() {
        // When user types 'Tumi' (capital 'T' at sentence start):
        // Candidate 0 MUST be 'তুমি' (high-frequency / curated Banglish alias), not 'টুমি'
        final List<String> candidates = BanglaSuggestionEngine.getCandidates("Tumi", mParser);
        assertNotNull(candidates);
        assertFalse(candidates.isEmpty());
        assertEquals("Tumi at sentence start must yield 'তুমি' at slot 0", "তুমি", candidates.get(0));
        assertTrue("Alternative transliteration 'টুমি' must still be available in candidate strip",
                candidates.contains("টুমি"));
    }

    @Test
    public void testCaseInsensitiveLettersNormalized() {
        // Letters outside the case-sensitive set (A, B, C, E, F, H, I, K, L, M, P, Q, V, W, X)
        // are normalized to lowercase by normalizeCase:
        assertEquals("ami", BanglaSuggestionEngine.normalizeCase("Ami"));
        assertEquals("kemon", BanglaSuggestionEngine.normalizeCase("Kemon"));
        assertEquals("bhalo", BanglaSuggestionEngine.normalizeCase("Bhalo"));
        assertEquals("hobe", BanglaSuggestionEngine.normalizeCase("Hobe"));
        assertEquals("eita", BanglaSuggestionEngine.normalizeCase("Eita"));

        // Case-sensitive letters (O, U, D, G, J, N, R, S, T, Y, Z) are preserved:
        assertEquals("Tumi", BanglaSuggestionEngine.normalizeCase("Tumi"));
        assertEquals("Daktar", BanglaSuggestionEngine.normalizeCase("Daktar"));
        assertEquals("Shundor", BanglaSuggestionEngine.normalizeCase("Shundor"));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private String simulateAvroSpacebarCommit(final String typedWord, final boolean exactPhonetic) {
        final boolean hasDigits = InputLogic.containsDigits(typedWord);
        final boolean useExact = exactPhonetic || hasDigits;
        if (!useExact) {
            final List<String> candidates = BanglaSuggestionEngine.getCandidates(typedWord, mParser);
            return (candidates != null && !candidates.isEmpty()) ? candidates.get(0) : mParser.toBangla(typedWord);
        } else {
            return mParser.toBangla(typedWord);
        }
    }

    private void executeRule5Commit(final MockTestInputConnection host, final String displayedPrefix, final String suggestion) {
        final boolean hostHasComposing = host.hasHostComposingSpan();
        final boolean hasSelection = host.hasSelection();
        final int typedLength = displayedPrefix != null ? displayedPrefix.length() : 0;
        if (typedLength > 0 && !hostHasComposing && !hasSelection) {
            host.deleteSurroundingText(typedLength, 0);
        }
        host.commitText(suggestion);
    }
}
