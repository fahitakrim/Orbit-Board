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
        final List<String> candidates = InputLogic.getBanglaCandidates(raw, parser);

        // Construct SuggestedWords according to the InputLogic Avro contract
        final ArrayList<SuggestedWordInfo> list = new ArrayList<>();
        list.add(new SuggestedWordInfo(raw, "", 1, SuggestedWordInfo.KIND_TYPED, null, 0, 0));
        int score = SuggestedWordInfo.MAX_SCORE;
        for (final String candidate : candidates) {
            if (!candidate.equals(raw)) {
                list.add(new SuggestedWordInfo(candidate, "", score, SuggestedWordInfo.KIND_CORRECTION, null, 0, 0));
                score--;
            }
        }
        final SuggestedWords words = new SuggestedWords(
                list, null, list.get(0), true, list.size() > 1, false,
                SuggestedWords.INPUT_STYLE_TYPING, 1);

        // Contract verification:
        // 1. Index 0 must be the typed word with KIND_TYPED
        assertEquals(raw, words.getWord(0));
        assertEquals(SuggestedWordInfo.KIND_TYPED, words.getInfo(0).mKindAndFlags);

        // 2. Index 1 must be the primary transliteration with KIND_CORRECTION
        assertEquals("আমি", words.getWord(1));
        assertEquals(SuggestedWordInfo.KIND_CORRECTION, words.getInfo(1).mKindAndFlags);

        // 3. typedWordInfo accessor must return raw word
        assertNotNull(words.getTypedWordInfo());
        assertEquals(raw, words.getTypedWordInfo().mWord);

        // 4. Verify ActionBar slot ordering emulation:
        // In ActionBar.kt:
        // if (info.kind == KIND_TYPED) { offset = 1 }
        // suggestionOrder = [ORDER_OF_SUGGESTIONS[0] + offset, ORDER_OF_SUGGESTIONS[1] + offset, 0]
        // where ORDER_OF_SUGGESTIONS = [1, 0, 2]
        // => suggestionOrder = [2, 1, 0]
        int offset = (words.getInfo(0).mKindAndFlags == SuggestedWordInfo.KIND_TYPED) ? 1 : 0;
        assertEquals("Offset must be 1 when index 0 is KIND_TYPED", 1, offset);

        int[] orderOfSuggestions = {1, 0, 2};
        int slot0Index = orderOfSuggestions[0] + offset; // 1 + 1 = 2 (left slot)
        int slot1Index = orderOfSuggestions[1] + offset; // 0 + 1 = 1 (center slot)
        int slot2Index = 0;                              // 0 (right slot)

        assertEquals("Center slot (slot 1) must display primary candidate index 1", 1, slot1Index);
        assertEquals("Center slot content must be 'আমি'", "আমি", words.getWord(slot1Index));

        assertEquals("Right slot (slot 2) must display raw typed index 0", 0, slot2Index);
        assertEquals("Right slot content must be 'ami'", "ami", words.getWord(slot2Index));

        if (words.size() > 2) {
            assertEquals("Left slot (slot 0) must display secondary candidate index 2", 2, slot0Index);
            assertEquals("Left slot content should be 'আমী'", "আমী", words.getWord(slot0Index));
        }
    }

    @Test
    public void testSuggestedWordsStructureForBanglaAndKemon() {
        final RidmikParser parser = new RidmikParser();

        // Test "bangla"
        {
            final String raw = "bangla";
            final List<String> candidates = InputLogic.getBanglaCandidates(raw, parser);
            final ArrayList<SuggestedWordInfo> list = new ArrayList<>();
            list.add(new SuggestedWordInfo(raw, "", 1, SuggestedWordInfo.KIND_TYPED, null, 0, 0));
            for (final String candidate : candidates) {
                if (!candidate.equals(raw)) {
                    list.add(new SuggestedWordInfo(candidate, "", 100, SuggestedWordInfo.KIND_CORRECTION, null, 0, 0));
                }
            }
            final SuggestedWords words = new SuggestedWords(list, null, list.get(0), true, true, false, 0, 1);
            assertEquals("bangla", words.getWord(0));
            assertEquals("বাংলা", words.getWord(1));
        }

        // Test "kemon"
        {
            final String raw = "kemon";
            final List<String> candidates = InputLogic.getBanglaCandidates(raw, parser);
            final ArrayList<SuggestedWordInfo> list = new ArrayList<>();
            list.add(new SuggestedWordInfo(raw, "", 1, SuggestedWordInfo.KIND_TYPED, null, 0, 0));
            for (final String candidate : candidates) {
                if (!candidate.equals(raw)) {
                    list.add(new SuggestedWordInfo(candidate, "", 100, SuggestedWordInfo.KIND_CORRECTION, null, 0, 0));
                }
            }
            final SuggestedWords words = new SuggestedWords(list, null, list.get(0), true, true, false, 0, 1);
            assertEquals("kemon", words.getWord(0));
            assertEquals("কেমন", words.getWord(1));
        }
    }
}
