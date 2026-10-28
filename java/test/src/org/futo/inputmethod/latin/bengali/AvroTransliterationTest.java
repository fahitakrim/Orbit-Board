package org.futo.inputmethod.latin.bengali;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.futo.inputmethod.latin.inputlogic.InputLogic;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Comprehensive unit tests for Avro phonetic transliteration and candidate generation.
 */
public class AvroTransliterationTest {

    private RidmikParser mParser;

    @Before
    public void setUp() {
        mParser = new RidmikParser();
    }

    @Test
    public void testBasicPhoneticWords() {
        // Core words required by specification
        assertEquals("আমি", mParser.toBangla("ami"));
        assertEquals("বাংলা", mParser.toBangla("bangla"));
        assertEquals("কেমন", mParser.toBangla("kemon"));
    }

    @Test
    public void testCommonConversationalPhrases() {
        assertEquals("তুমি", mParser.toBangla("tumi"));
        assertEquals("ধন্যবাদ", mParser.toBangla("dhonyobad"));
        assertEquals("ভালো", mParser.toBangla("bhalO"));
        assertEquals("ভাল", mParser.toBangla("bhalo"));
        assertEquals("সুন্দর", mParser.toBangla("sundor"));
        assertEquals("ভারত", mParser.toBangla("bharot"));
        assertEquals("আমার", mParser.toBangla("amar"));
        assertEquals("সোনার", mParser.toBangla("sOnar"));
        assertEquals("গান", mParser.toBangla("gan"));
    }

    @Test
    public void testCaseSensitiveConsonantPairs() {
        // 't' (ত) vs 'T' (ট)
        assertEquals("তাকা", mParser.toBangla("taka"));
        assertEquals("টাকা", mParser.toBangla("Taka"));

        // 'd' (দ) vs 'D' (ড)
        assertEquals("দাল", mParser.toBangla("dal"));
        assertEquals("ডাল", mParser.toBangla("Dal"));

        // 'r' (র) vs 'R' (ড়)
        assertEquals("বাড়ি", mParser.toBangla("baRi"));
        assertEquals("বারি", mParser.toBangla("bari"));
    }

    @Test
    public void testEdgeCasesAndPunctuation() {
        assertEquals("", mParser.toBangla(""));
        assertEquals("", mParser.toBangla(null));
        assertEquals("123", mParser.toBangla("123"));
        assertEquals("আমি!", mParser.toBangla("ami!"));
        assertEquals("কেমন?", mParser.toBangla("kemon?"));
    }

    @Test
    public void testCandidateGenerationForCoreWords() {
        // Test "ami"
        List<String> amiCandidates = InputLogic.getBanglaCandidates("ami", mParser);
        assertNotNull(amiCandidates);
        assertFalse("Candidates for 'ami' must not be empty", amiCandidates.isEmpty());
        assertEquals("Primary candidate for 'ami' must be 'আমি'", "আমি", amiCandidates.get(0));
        assertTrue("Candidates for 'ami' should include 'আমী'", amiCandidates.contains("আমী"));

        // Test "bangla"
        List<String> banglaCandidates = InputLogic.getBanglaCandidates("bangla", mParser);
        assertNotNull(banglaCandidates);
        assertFalse(banglaCandidates.isEmpty());
        assertEquals("Primary candidate for 'bangla' must be 'বাংলা'", "বাংলা", banglaCandidates.get(0));

        // Test "kemon"
        List<String> kemonCandidates = InputLogic.getBanglaCandidates("kemon", mParser);
        assertNotNull(kemonCandidates);
        assertFalse(kemonCandidates.isEmpty());
        assertEquals("Primary candidate for 'kemon' must be 'কেমন'", "কেমন", kemonCandidates.get(0));
    }

    @Test
    public void testCandidateGenerationNoDuplicatesAndBounds() {
        String[] testWords = {"ami", "bangla", "kemon", "tumi", "korbo", "dal", "shonar", "rasta"};
        for (String word : testWords) {
            List<String> candidates = InputLogic.getBanglaCandidates(word, mParser);
            assertNotNull(candidates);
            assertFalse(candidates.isEmpty());
            assertTrue("Candidates list should have <= 5 entries", candidates.size() <= 5);

            // Ensure no duplicate candidates
            Set<String> unique = new HashSet<>(candidates);
            assertEquals("Candidate list must not contain duplicates for: " + word,
                    unique.size(), candidates.size());

            // Primary candidate must equal toBangla(word)
            assertEquals("First candidate must be primary transliteration for: " + word,
                    mParser.toBangla(word), candidates.get(0));
        }
    }

    @Test
    public void testPhonologicalVariations() {
        // Ends with consonant should offer trailing 'o' variation
        List<String> bhalCandidates = InputLogic.getBanglaCandidates("bhal", mParser);
        assertTrue("Candidates for 'bhal' should include 'ভালো'", bhalCandidates.contains("ভালো"));

        // 't' vs 'T' variation
        List<String> takaCandidates = InputLogic.getBanglaCandidates("taka", mParser);
        assertTrue("Candidates for 'taka' should include 'টাকা'", takaCandidates.contains("টাকা"));

        // 'd' vs 'D' variation
        List<String> dalCandidates = InputLogic.getBanglaCandidates("dal", mParser);
        assertTrue("Candidates for 'dal' should include 'ডাল'", dalCandidates.contains("ডাল"));

        // 's' vs 'sh' variation
        List<String> shonarCandidates = InputLogic.getBanglaCandidates("shonar", mParser);
        assertTrue("Candidates for 'shonar' should include 'সোনার'", shonarCandidates.contains("সোনার"));
    }
}
