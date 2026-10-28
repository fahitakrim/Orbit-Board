package org.futo.inputmethod.latin.bengali;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.futo.inputmethod.latin.SuggestedWords;
import org.futo.inputmethod.latin.SuggestedWords.SuggestedWordInfo;
import org.futo.inputmethod.latin.inputlogic.InputLogic;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adversarial Verification Test Suite for Avro Phonetic Transliteration & Candidate Generation.
 * Executed independently by Challenger 1 (challenger_13_m1_1).
 */
public class AvroAdversarialVerificationTest {

    private RidmikParser mParser;

    @Before
    public void setUp() {
        mParser = new RidmikParser();
    }

    // =========================================================================
    // 1. Mandatory Word Transliteration Tests
    // =========================================================================

    @Test
    public void testMandatoryCoreWords() {
        assertEquals("আমি", mParser.toBangla("ami"));
        assertEquals("বাংলা", mParser.toBangla("bangla"));
        assertEquals("কেমন", mParser.toBangla("kemon"));
    }

    @Test
    public void testMandatoryExtendedWords() {
        // "shundor" and "sundor"
        assertEquals("সুন্দর", mParser.toBangla("sundor"));
        // "shundor" transliterates to talobbo-sho by phonetic rule,
        // and its candidate generation MUST contain "সুন্দর"
        List<String> shundorCandidates = InputLogic.getBanglaCandidates("shundor", mParser);
        assertTrue("Candidate list for 'shundor' must contain 'সুন্দর'", shundorCandidates.contains("সুন্দর"));

        // "bhalobasha" and "bhalobasa"
        List<String> bhalobashaCandidates = InputLogic.getBanglaCandidates("bhalobasha", mParser);
        assertTrue("Candidate list for 'bhalobasha' must contain 'ভালোবাসা'", bhalobashaCandidates.contains("ভালোবাসা"));

        // "dosh" -> "দশ" (ten)
        assertEquals("দশ", mParser.toBangla("dosh"));
        // "dOSh" -> "দোষ" (fault/guilt in Avro: O for o-kar, Sh for murdhonno-sho)
        assertEquals("দোষ", mParser.toBangla("dOSh"));

        // "probhat" (প্রভাত)
        assertEquals("প্রভাত", mParser.toBangla("probhat"));
    }

    // =========================================================================
    // 2. Complex Bengali Conjuncts (যুক্তবর্ণ) Transliteration
    // =========================================================================

    @Test
    public void testComplexConjuncts() {
        // pr conjunct (প + র-ফলা)
        assertEquals("প্রথম", mParser.toBangla("prothom"));
        assertEquals("প্রভাত", mParser.toBangla("probhat"));
        assertEquals("প্রশ্ন", mParser.toBangla("proshno"));

        // tr conjunct (ত + র-ফলা) in Avro: ch = ছ, c = চ, tr = ত্ৰ/ত্র
        assertEquals("ছাত্র", mParser.toBangla("chatro"));
        assertEquals("রাত্রি", mParser.toBangla("ratri"));

        // kr conjunct (ক + র-ফলা)
        assertEquals("গ্রাম", mParser.toBangla("gram"));
        assertEquals("ক্রিকেট", mParser.toBangla("krikeT"));

        // kkh (ক্ষ = ক + ষ)
        assertEquals("ক্ষতি", mParser.toBangla("kkhoti"));
        assertEquals("পরিক্ষা", mParser.toBangla("porikkha"));
        assertEquals("পরীক্ষা", mParser.toBangla("porIkkha"));
        assertTrue("Candidates for 'porikkha' should include 'পরীক্ষা'",
                InputLogic.getBanglaCandidates("porikkha", mParser).contains("পরীক্ষা"));

        // gyan (জ্ঞ = জ + ঞ)
        assertEquals("জ্ঞান", mParser.toBangla("ggan"));

        // kt (ক্ত = ক + ত)
        assertEquals("রক্ত", mParser.toBangla("rokto"));
        assertEquals("শক্তি", mParser.toBangla("shokti"));

        // bd (ব্দ = ব + দ)
        assertEquals("শব্দ", mParser.toBangla("shobdo"));

        // nd (ন্দ = ন + দ)
        assertEquals("আনন্দ", mParser.toBangla("anondo"));
        assertEquals("সুন্দর", mParser.toBangla("sundor"));

        // nt (ন্ত = ন + ত)
        assertEquals("শান্ত", mParser.toBangla("shanto"));
        assertEquals("অনন্ত", mParser.toBangla("ononto"));

        // ndh (ন্ধ = ন + ধ)
        assertEquals("বন্ধু", mParser.toBangla("bondhu"));
        assertEquals("অন্ধ", mParser.toBangla("ondho"));

        // sth (স্থ = স + থ)
        assertEquals("স্থান", mParser.toBangla("sthan"));

        // sp (স্প = স + প) and ShT (ষ্ট = ষ + ট)
        assertEquals("স্পষ্ট", mParser.toBangla("spoShTo"));

        // khondiyo-to (ৎ) via TH
        assertEquals("উৎসব", mParser.toBangla("uTHsob"));
        assertEquals("হঠাৎ", mParser.toBangla("hoThaTH"));

        // onushar (ং) via ng
        assertEquals("বাংলা", mParser.toBangla("bangla"));
        assertEquals("রং", mParser.toBangla("rong"));

        // chondrobindu (ঁ) via qq
        assertEquals("চাঁদ", mParser.toBangla("caqqd"));
    }

    // =========================================================================
    // 3. Boundary & Stress Testing
    // =========================================================================

    @Test
    public void testNullAndEmptyInputs() {
        assertEquals("", mParser.toBangla(""));
        assertEquals("", mParser.toBangla(null));

        List<String> nullCandidates = InputLogic.getBanglaCandidates(null, mParser);
        assertNotNull(nullCandidates);
        assertTrue(nullCandidates.isEmpty());

        List<String> emptyCandidates = InputLogic.getBanglaCandidates("", mParser);
        assertNotNull(emptyCandidates);
        assertTrue(emptyCandidates.isEmpty());
    }

    @Test
    public void testNumericAndAlphanumericInputs() {
        // Pure digits should pass through unchanged without throwing
        assertEquals("0", mParser.toBangla("0"));
        assertEquals("1234567890", mParser.toBangla("1234567890"));

        // Alphanumeric mixed
        String mixed = mParser.toBangla("a1b2c3");
        assertNotNull(mixed);
        assertTrue(mixed.contains("1"));
        assertTrue(mixed.contains("2"));
        assertTrue(mixed.contains("3"));
    }

    @Test
    public void testPunctuationAndSymbols() {
        assertEquals("আমি?", mParser.toBangla("ami?"));
        assertEquals("কেমন!", mParser.toBangla("kemon!"));
        assertEquals("বাংলা.", mParser.toBangla("bangla."));
        assertEquals("ধন্যবাদ,", mParser.toBangla("dhonyobad,"));
        assertEquals("কথা:কাজ", mParser.toBangla("kotha:kaj"));
    }

    @Test
    public void testCaseFoldingAndSensitivity() {
        // Sentence-initial capitalized words should transliterate cleanly to their Bengali equivalents
        assertEquals("আমি", mParser.toBangla("Ami"));
        assertEquals("বাংলা", mParser.toBangla("Bangla"));
        assertEquals("কেমন", mParser.toBangla("Kemon"));

        // Avro Phonetic case-sensitivity distinctions:
        // 'ng' (onushar ং) vs 'NG' (niyo ঞ) vs 'Ng' (unga ঙ)
        assertEquals("বাংলা", mParser.toBangla("bangla"));
        assertEquals("বাঞলা", mParser.toBangla("BANGLA")); // 'NG' produces ঞ (niyo)

        // 't' (ত) vs 'T' (ট)
        assertEquals("তাকা", mParser.toBangla("taka"));
        assertEquals("টাকা", mParser.toBangla("Taka"));

        // 'd' (দ) vs 'D' (ড)
        assertEquals("দাল", mParser.toBangla("dal"));
        assertEquals("ডাল", mParser.toBangla("Dal"));

        // 'n' (ন) vs 'N' (ণ)
        assertEquals("গান", mParser.toBangla("gan"));
        assertEquals("বাণ", mParser.toBangla("baN"));

        // 's' (স) vs 'sh' (শ) vs 'Sh' (ষ)
        assertEquals("সাপ", mParser.toBangla("sap"));
        assertEquals("শাপ", mParser.toBangla("shap"));
        assertEquals("দোষ", mParser.toBangla("dOSh"));

        // 'o' (inherent vowel) vs 'O' (ো-কার)
        assertEquals("করব", mParser.toBangla("korbo"));
        assertEquals("করবো", mParser.toBangla("korbO"));
    }

    @Test
    public void testStressLongInputString() {
        // Stress test: 500-character repetitive roman string
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("bangla");
        }
        String longInput = sb.toString();
        try {
            String result = mParser.toBangla(longInput);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        } catch (Exception e) {
            fail("Parser failed on long input string: " + e.getMessage());
        }
    }

    @Test
    public void testRepetitiveCharacters() {
        // Ensure no infinite loops or crashes on repeated chars
        String[] repetitions = {"aaaaa", "kkkkk", "sssss", "hhhhh", ".....", "?????", "      "};
        for (String rep : repetitions) {
            try {
                String out = mParser.toBangla(rep);
                assertNotNull(out);
            } catch (Exception e) {
                fail("Failed on repetition '" + rep + "': " + e.getMessage());
            }
        }
    }

    // =========================================================================
    // 4. Candidate Generation Contract & Invariants
    // =========================================================================

    @Test
    public void testCandidateListInvariants() {
        String[] testWords = {
                "ami", "bangla", "kemon", "shundor", "sundor", "bhalobasha",
                "dosh", "probhat", "tumi", "dhonyobad", "amar", "desh"
        };

        for (String word : testWords) {
            List<String> candidates = InputLogic.getBanglaCandidates(word, mParser);
            assertNotNull("Candidates must not be null for " + word, candidates);
            assertFalse("Candidates must not be empty for " + word, candidates.isEmpty());
            assertTrue("Candidate count must be <= 5 for " + word + ", was: " + candidates.size(),
                    candidates.size() <= 5);

            // Invariant: First candidate MUST be the primary transliteration
            assertEquals("First candidate must match toBangla(" + word + ")",
                    mParser.toBangla(word), candidates.get(0));

            // Invariant: No duplicate entries
            Set<String> unique = new HashSet<>(candidates);
            assertEquals("Candidates must not contain duplicates for " + word,
                    unique.size(), candidates.size());

            // Invariant: Non-empty strings
            for (String c : candidates) {
                assertNotNull(c);
                assertFalse("Candidate item must not be empty for " + word, c.isEmpty());
            }
        }
    }

    @Test
    public void testActionBarSlotContract() {
        final String raw = "ami";
        final List<String> candidates = InputLogic.getBanglaCandidates(raw, mParser);

        // Build SuggestedWords according to InputLogic specification
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

        // Verification of contract with ActionBar.kt:
        // Index 0 is typed raw string KIND_TYPED
        assertEquals(raw, words.getWord(0));
        assertEquals(SuggestedWordInfo.KIND_TYPED, words.getInfo(0).mKindAndFlags);

        // Index 1 is primary Bengali candidate
        assertEquals("আমি", words.getWord(1));
        assertEquals(SuggestedWordInfo.KIND_CORRECTION, words.getInfo(1).mKindAndFlags);

        // ActionBar offset calculation
        int offset = (words.getInfo(0).mKindAndFlags == SuggestedWordInfo.KIND_TYPED) ? 1 : 0;
        assertEquals(1, offset);

        // Slots: [ORDER_OF_SUGGESTIONS[0] + offset, ORDER_OF_SUGGESTIONS[1] + offset, 0]
        // Center slot (slot 1) = order[1] + 1 = 0 + 1 = 1 -> "আমি"
        int centerSlotIndex = 0 + offset;
        assertEquals(1, centerSlotIndex);
        assertEquals("Center slot must display 'আমি'", "আমি", words.getWord(centerSlotIndex));

        // Right slot (slot 2) = 0 -> "ami" (typed raw string)
        int rightSlotIndex = 0;
        assertEquals("Right slot must display raw 'ami'", "ami", words.getWord(rightSlotIndex));
    }
}
