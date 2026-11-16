package org.futo.inputmethod.latin.bengali;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.futo.inputmethod.latin.inputlogic.InputLogic;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests verifying Dynamic Word Learning and Avro Transliteration Memory:
 * 1. A word typed or picked from suggestions is saved in BanglaSuggestionEngine.
 * 2. Subsequent calls to getCandidates return the learned word at the top of the list (index 0).
 * 3. Word learning works when auto-correction is OFF.
 * 4. Thread-safe persistent storage survives reload/reboots.
 * 5. Unlearning and forgetting words.
 * 6. Non-Bengali / English selections are NOT learned into Avro transliteration engine.
 * 7. Multithreaded concurrency stress testing without data loss.
 */
public class DynamicWordLearningTest {

    private RidmikParser mParser;
    private File mTempStorageFile;

    @Before
    public void setUp() throws IOException {
        mParser = new RidmikParser();
        mTempStorageFile = File.createTempFile("user_avro_test", ".json");
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

    @Test
    public void testDirectWordLearningAndCounts() {
        // Initially no learned word for "testkey"
        assertNull(BanglaSuggestionEngine.getTopLearnedWord("testkey"));

        // Learn a word mapping
        BanglaSuggestionEngine.learnWord("testkey", "টেস্টকী");
        BanglaSuggestionEngine.TransliterationEntry entry = BanglaSuggestionEngine.getTopLearnedWord("testkey");
        assertNotNull("Learned entry must not be null", entry);
        assertEquals("টেস্টকী", entry.chosenBengali);
        assertEquals(1, entry.count);
        assertTrue(entry.lastUsedTimestamp > 0);

        // Learn the same word again -> count increments to 2
        BanglaSuggestionEngine.learnWord("testkey", "টেস্টকী");
        entry = BanglaSuggestionEngine.getTopLearnedWord("testkey");
        assertEquals(2, entry.count);

        // Learn an alternative word for the same latin key
        BanglaSuggestionEngine.learnWord("testkey", "টেস্টকি");
        entry = BanglaSuggestionEngine.getTopLearnedWord("testkey");
        // "টেস্টকী" still top because count 2 > count 1
        assertEquals("টেস্টকী", entry.chosenBengali);

        // Boost the alternative word past the first one
        BanglaSuggestionEngine.learnWord("testkey", "টেস্টকি");
        BanglaSuggestionEngine.learnWord("testkey", "টেস্টকি");
        entry = BanglaSuggestionEngine.getTopLearnedWord("testkey");
        assertEquals("টেস্টকি", entry.chosenBengali);
        assertEquals(3, entry.count);
    }

    @Test
    public void testLearnedWordInjectedAtTopOfCandidates() {
        final String rawLatin = "customword";
        // Default candidates before learning
        List<String> initialCandidates = BanglaSuggestionEngine.getCandidates(rawLatin, mParser);
        assertNotNull(initialCandidates);
        assertFalse(initialCandidates.isEmpty());

        // User picks or learns custom spelling
        final String customBengali = "কাস্টমশব্দ";
        BanglaSuggestionEngine.learnWord(rawLatin, customBengali);

        // Subsequent getCandidates call MUST return customBengali at index 0 (top priority)
        List<String> afterLearning = BanglaSuggestionEngine.getCandidates(rawLatin, mParser);
        assertNotNull(afterLearning);
        assertFalse(afterLearning.isEmpty());
        assertEquals("Learned word must be injected at index 0", customBengali, afterLearning.get(0));

        // Must work via InputLogic.getBanglaCandidates contract as well
        List<String> inputLogicCandidates = InputLogic.getBanglaCandidates(rawLatin, mParser);
        assertEquals("InputLogic must return learned word at index 0", customBengali, inputLogicCandidates.get(0));

        // Test case-insensitive querying: uppercase or mixed-case query retrieves learned word
        List<String> mixedCaseCandidates = BanglaSuggestionEngine.getCandidates("CustomWord", mParser);
        assertEquals("Mixed-case query must resolve learned word to index 0", customBengali, mixedCaseCandidates.get(0));

        // Verify standard suggestions are preserved in secondary candidate slots
        assertTrue("Candidate list should preserve standard suggestions", afterLearning.size() > 1);
    }

    @Test
    public void testUnlearningAndForgetting() {
        final String raw = "shobdo";
        BanglaSuggestionEngine.learnWord(raw, "পছন্দেরশব্দ");
        assertEquals("পছন্দেরশব্দ", BanglaSuggestionEngine.getCandidates(raw, mParser).get(0));

        // Unlearn chosen word (decrement count to 0)
        assertTrue(BanglaSuggestionEngine.unlearnWord(raw, "পছন্দেরশব্দ"));
        // Now it is forgotten
        assertNull(BanglaSuggestionEngine.getTopLearnedWord(raw));
        List<String> candidates = BanglaSuggestionEngine.getCandidates(raw, mParser);
        assertFalse("Forgotten word must not be top candidate", "পছন্দেরশব্দ".equals(candidates.get(0)));

        // Learn multiple and forget entire key
        BanglaSuggestionEngine.learnWord("kothay", "কই");
        BanglaSuggestionEngine.learnWord("kothay", "কোথায়");
        assertTrue(BanglaSuggestionEngine.forgetWord("kothay"));
        assertNull(BanglaSuggestionEngine.getTopLearnedWord("kothay"));
    }

    @Test
    public void testPersistenceAcrossKeyboardReloads() {
        // Learn words in current session
        BanglaSuggestionEngine.learnWord("avrokey", "অভ্রকী");
        BanglaSuggestionEngine.learnWord("avrokey", "অভ্রকী");
        BanglaSuggestionEngine.learnWord("ridmik", "রিদ্মিক");
        BanglaSuggestionEngine.flushSynchronously();

        assertTrue("Storage file must exist and have content", mTempStorageFile.length() > 0);

        // Simulate keyboard process reload by wiping in-memory dictionary
        BanglaSuggestionEngine.clearMemoryOnlyForTest();
        assertNull("Memory should be clear before reload", BanglaSuggestionEngine.getTopLearnedWord("avrokey"));

        // Reload from storage file (like on reboot / keyboard restart)
        BanglaSuggestionEngine.setStorageFile(mTempStorageFile);

        BanglaSuggestionEngine.TransliterationEntry entry = BanglaSuggestionEngine.getTopLearnedWord("avrokey");
        assertNotNull("Learned entry must be restored from file", entry);
        assertEquals("অভ্রকী", entry.chosenBengali);
        assertEquals(2, entry.count);

        BanglaSuggestionEngine.TransliterationEntry ridmikEntry = BanglaSuggestionEngine.getTopLearnedWord("ridmik");
        assertNotNull(ridmikEntry);
        assertEquals("রিদ্মিক", ridmikEntry.chosenBengali);

        // Verify candidates top injection still works after reload
        List<String> candidates = BanglaSuggestionEngine.getCandidates("avrokey", mParser);
        assertEquals("অভ্রকী", candidates.get(0));
    }

    @Test
    public void testWordLearningWorksWhenAutoCorrectionIsOff() {
        // 1. In Avro mode, dynamic transliteration learning occurs regardless of auto-correction setting.
        final String rawTyped = "amarshobdo";
        final String chosenBengali = "আমারশব্দ";

        // Learn the transliteration (as performed by InputLogic even when auto-correction is OFF)
        BanglaSuggestionEngine.learnWord(rawTyped, chosenBengali);

        // Verify subsequent candidate queries return the learned word at index 0
        List<String> candidates = BanglaSuggestionEngine.getCandidates(rawTyped, mParser);
        assertNotNull(candidates);
        assertEquals("Learned word must be index 0 even when auto-correction is OFF",
                chosenBengali, candidates.get(0));

        // 2. In InputLogic, verify word learning is permitted when auto-correction is OFF:
        // When autoCorrectionEnabledPerUserSettings is false, learning must still be permitted.
        assertTrue("Learning must be allowed when auto-correction is OFF",
                InputLogic.isLearningAllowed(false /* autoCorrectionEnabled */, false /* noLearning */));
        assertTrue("Learning must be allowed when auto-correction is ON",
                InputLogic.isLearningAllowed(true /* autoCorrectionEnabled */, false /* noLearning */));

        // Only mInputAttributes.mNoLearning (incognito/private mode) should suppress word learning
        assertFalse("Learning must be blocked when noLearning is true even if auto-correction is ON",
                InputLogic.isLearningAllowed(true /* autoCorrectionEnabled */, true /* noLearning */));
        assertFalse("Learning must be blocked when noLearning is true and auto-correction is OFF",
                InputLogic.isLearningAllowed(false /* autoCorrectionEnabled */, true /* noLearning */));
    }

    @Test
    public void testEnglishOrNonBengaliSelectionsNotLearnedIntoAvroDictionary() {
        // In Avro mode, English candidates or non-Bengali words picked from suggestion strip
        // must NOT pollute the Bengali transliteration memory.
        final String raw = "ami";
        BanglaSuggestionEngine.learnWord(raw, "ami"); // User picked English "ami"
        assertNull("English word must not be learned into Avro transliteration engine",
                BanglaSuggestionEngine.getTopLearnedWord(raw));

        // Default Bengali transliteration must remain untouched at index 0
        List<String> candidates = BanglaSuggestionEngine.getCandidates(raw, mParser);
        assertEquals("Default candidate 0 must remain 'আমি'", "আমি", candidates.get(0));

        // Bengali input cannot be passed as Latin key
        BanglaSuggestionEngine.learnWord("আমি", "আমার");
        assertNull(BanglaSuggestionEngine.getTopLearnedWord("আমি"));
    }

    @Test
    public void testMultithreadedConcurrentLearning() throws InterruptedException {
        final int threadCount = 8;
        final int iterationsPerThread = 50;
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            executor.execute(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < iterationsPerThread; i++) {
                        BanglaSuggestionEngine.learnWord("concurrent", "কনকারেন্ট");
                    }
                } catch (Exception ignored) {
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue("Concurrent learning threads must finish in time", endLatch.await(5, TimeUnit.SECONDS));
        executor.shutdown();

        BanglaSuggestionEngine.TransliterationEntry entry = BanglaSuggestionEngine.getTopLearnedWord("concurrent");
        assertNotNull("Concurrent entry must exist", entry);
        assertEquals("Entry count must accurately reflect all concurrent increments",
                threadCount * iterationsPerThread, entry.count);
    }
}
