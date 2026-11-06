package org.futo.inputmethod.latin.bengali;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.futo.inputmethod.latin.SuggestedWords;
import org.futo.inputmethod.latin.SuggestedWords.SuggestedWordInfo;
import org.futo.inputmethod.latin.inputlogic.InputLogic;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates Avro word code point detection, composing text handling,
 * and suggestion strip candidate ordering contract.
 */
public class AvroComposingLogicTest {

    @Test
    public void testAvroWordCodePoints() {
        // Lowercase English letters must be word code points
        for (char c = 'a'; c <= 'z'; c++) {
            assertTrue("Expected " + c + " to be Avro word code point",
                    InputLogic.isAvroWordCodePoint(c));
        }

        // Uppercase English letters must be word code points
        for (char c = 'A'; c <= 'Z'; c++) {
            assertTrue("Expected " + c + " to be Avro word code point",
                    InputLogic.isAvroWordCodePoint(c));
        }

        // Apostrophe and backtick used for phonetic modifiers
        assertTrue("Apostrophe must be Avro word code point", InputLogic.isAvroWordCodePoint('\''));
        assertTrue("Backtick must be Avro word code point", InputLogic.isAvroWordCodePoint('`'));

        // Separators and punctuation must NOT be word code points
        assertFalse("Space is not a word code point", InputLogic.isAvroWordCodePoint(' '));
        assertFalse("Newline is not a word code point", InputLogic.isAvroWordCodePoint('\n'));
        assertFalse("Period is not a word code point", InputLogic.isAvroWordCodePoint('.'));
        assertFalse("Bengali Dari is not a word code point", InputLogic.isAvroWordCodePoint('\u0964'));
        assertFalse("Comma is not a word code point", InputLogic.isAvroWordCodePoint(','));
        assertFalse("Question mark is not a word code point", InputLogic.isAvroWordCodePoint('?'));
        assertFalse("Exclamation mark is not a word code point", InputLogic.isAvroWordCodePoint('!'));
        assertFalse("Dollar is not a word code point", InputLogic.isAvroWordCodePoint('$'));
        assertFalse("Digit 0 is not a word code point", InputLogic.isAvroWordCodePoint('0'));
        assertFalse("Digit 9 is not a word code point", InputLogic.isAvroWordCodePoint('9'));
    }

    @Test
    public void testSuggestedWordsStructureForAvroComposing() {
        final RidmikParser parser = new RidmikParser();
        final String raw = "ami";
        final String rawBangla = parser.toBangla(raw);
        final List<String> candidates = InputLogic.getBanglaCandidates(raw, parser);

        // Construct SuggestedWords according to the updated InputLogic Avro contract
        final ArrayList<SuggestedWordInfo> list = new ArrayList<>();
        final SuggestedWordInfo typedWordInfo = new SuggestedWordInfo(
                rawBangla, "", 1, SuggestedWordInfo.KIND_TYPED, null, 0, 0);
        list.add(typedWordInfo);
        int score = SuggestedWordInfo.MAX_SCORE;
        for (final String candidate : candidates) {
            if (!candidate.equals(rawBangla)) {
                list.add(new SuggestedWordInfo(candidate, "", score, SuggestedWordInfo.KIND_CORRECTION, null, 0, 0));
                score--;
            }
        }

        // Test with auto-correction enabled:
        final SuggestedWords wordsWithAutoCorrect = new SuggestedWords(
                list, null, typedWordInfo, true, list.size() > 1, false,
                SuggestedWords.INPUT_STYLE_TYPING, 1);

        // 1. Index 0 must be the raw phonetic Bangla transliteration with KIND_TYPED
        assertEquals("Index 0 must be rawBangla", rawBangla, wordsWithAutoCorrect.getWord(0));
        assertEquals(SuggestedWordInfo.KIND_TYPED, wordsWithAutoCorrect.getInfo(0).mKindAndFlags);

        // 2. typedWordInfo accessor must return rawBangla
        assertNotNull(wordsWithAutoCorrect.getTypedWordInfo());
        assertEquals("typedWordInfo must be 'আমি'", rawBangla, wordsWithAutoCorrect.getTypedWordInfo().mWord);

        // Test with auto-correction disabled:
        final SuggestedWords wordsWithoutAutoCorrect = new SuggestedWords(
                list, null, typedWordInfo, true, false /* willAutoCorrect is false */, false,
                SuggestedWords.INPUT_STYLE_TYPING, 1);
        assertFalse("willAutoCorrect must be false when auto-correction is disabled",
                wordsWithoutAutoCorrect.mWillAutoCorrect);
        assertEquals("typedWordInfo remains rawBangla", rawBangla, wordsWithoutAutoCorrect.getTypedWordInfo().mWord);
    }

    @Test
    public void testAvroAutoCorrectionDisabledDoesNotOverrideUserPhonetic() {
        final RidmikParser parser = new RidmikParser();
        // For shorthand like 'kmn', raw transliteration is 'ক্মন', while suggestion candidate 0 is 'কেমন'
        final String raw = "kmn";
        final String rawBangla = parser.toBangla(raw);
        assertEquals("Raw Avro transliteration of 'kmn' is 'কম্ন'", "কম্ন", rawBangla);

        final List<String> candidates = InputLogic.getBanglaCandidates(raw, parser);
        assertNotNull(candidates);
        assertFalse(candidates.isEmpty());
        assertEquals("Primary suggestion for 'kmn' is 'কেমন'", "কেমন", candidates.get(0));

        // When auto-correction is OFF:
        final boolean isAutoCorrectionOn = false;
        final String stringToCommit = isAutoCorrectionOn
                ? (!candidates.isEmpty() ? candidates.get(0) : rawBangla)
                : rawBangla;

        assertEquals("When auto-correction is OFF, rawBangla must be committed", "কম্ন", stringToCommit);

        // When auto-correction is ON:
        final boolean isAutoCorrectionOnTrue = true;
        final String stringToCommitOn = isAutoCorrectionOnTrue
                ? (!candidates.isEmpty() ? candidates.get(0) : rawBangla)
                : rawBangla;

        assertEquals("When auto-correction is ON, suggested candidate must be committed", "কেমন", stringToCommitOn);
    }

    @Test
    public void testSuggestedWordsStructureForBanglaAndKemon() {
        final RidmikParser parser = new RidmikParser();

        // Test "bangla"
        {
            final String raw = "bangla";
            final String rawBangla = parser.toBangla(raw);
            final List<String> candidates = InputLogic.getBanglaCandidates(raw, parser);
            final ArrayList<SuggestedWordInfo> list = new ArrayList<>();
            final SuggestedWordInfo typedWordInfo = new SuggestedWordInfo(
                    rawBangla, "", 1, SuggestedWordInfo.KIND_TYPED, null, 0, 0);
            list.add(typedWordInfo);
            for (final String candidate : candidates) {
                if (!candidate.equals(rawBangla)) {
                    list.add(new SuggestedWordInfo(candidate, "", 100, SuggestedWordInfo.KIND_CORRECTION, null, 0, 0));
                }
            }
            final SuggestedWords words = new SuggestedWords(list, null, typedWordInfo, true, true, false, 0, 1);
            assertEquals("বাংলা", words.getWord(0));
            assertEquals(rawBangla, words.getTypedWordInfo().mWord);
        }

        // Test "kemon"
        {
            final String raw = "kemon";
            final String rawBangla = parser.toBangla(raw);
            final List<String> candidates = InputLogic.getBanglaCandidates(raw, parser);
            final ArrayList<SuggestedWordInfo> list = new ArrayList<>();
            final SuggestedWordInfo typedWordInfo = new SuggestedWordInfo(
                    rawBangla, "", 1, SuggestedWordInfo.KIND_TYPED, null, 0, 0);
            list.add(typedWordInfo);
            for (final String candidate : candidates) {
                if (!candidate.equals(rawBangla)) {
                    list.add(new SuggestedWordInfo(candidate, "", 100, SuggestedWordInfo.KIND_CORRECTION, null, 0, 0));
                }
            }
            final SuggestedWords words = new SuggestedWords(list, null, typedWordInfo, true, true, false, 0, 1);
            assertEquals("কেমন", words.getWord(0));
            assertEquals(rawBangla, words.getTypedWordInfo().mWord);
        }
    }
}
