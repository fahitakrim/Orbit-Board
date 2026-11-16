package org.futo.inputmethod.latin.bengali;

import android.content.Context;
import android.util.Log;

import org.futo.inputmethod.latin.DictionaryFacilitator;
import org.futo.inputmethod.latin.common.FileUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Intelligent Bangla Suggestion & Phonetic Completion Engine.
 * 
 * Enhanced with insights and algorithms from Borno (Jayed Ahsan):
 * 1. Conversational Banglish alias resolution with primary candidate priority.
 * 2. Borno caseHandle: protects phonetic uppercase chars while normalizing accidental auto-caps.
 * 3. Borno RemoveConsecutiveKarSigns: eliminates broken dotted circles by transforming consecutive kar signs into full vowels.
 * 4. High-accuracy phonetic normalization (v <-> bh, s <-> sh, verb inflection trailing o/O).
 * 5. Integration with DictionaryFacilitator (main_bn.dict) for authentic Bengali word ranking.
 * 6. Dynamic Word Learning & Avro Transliteration Memory (like Ridmik & Gboard).
 */
public final class BanglaSuggestionEngine {

    private static final String TAG = "BanglaSuggestionEngine";

    /**
     * Data structure representing a learned transliteration entry.
     */
    public static final class TransliterationEntry {
        public final String chosenBengali;
        public int count;
        public long lastUsedTimestamp;

        public TransliterationEntry(final String chosenBengali, final int count, final long lastUsedTimestamp) {
            this.chosenBengali = chosenBengali;
            this.count = count;
            this.lastUsedTimestamp = lastUsedTimestamp;
        }

        public synchronized int getCount() {
            return count;
        }

        public synchronized long getLastUsedTimestamp() {
            return lastUsedTimestamp;
        }

        public synchronized void incrementCount() {
            count++;
            lastUsedTimestamp = System.currentTimeMillis();
        }
    }

    /**
     * Data structure representing a dynamic learned bigram (next-word) entry.
     */
    public static final class BigramEntry {
        public final String nextWord;
        public int count;
        public long lastUsedTimestamp;

        public BigramEntry(final String nextWord, final int count, final long lastUsedTimestamp) {
            this.nextWord = nextWord;
            this.count = count;
            this.lastUsedTimestamp = lastUsedTimestamp;
        }

        public synchronized int getCount() {
            return count;
        }

        public synchronized long getLastUsedTimestamp() {
            return lastUsedTimestamp;
        }

        public synchronized void incrementCount() {
            count++;
            lastUsedTimestamp = System.currentTimeMillis();
        }
    }

    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, TransliterationEntry>> sLearnedDictionary =
            new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, BigramEntry>> sLearnedBigrams =
            new ConcurrentHashMap<>();
    private static volatile File sStorageFile = null;
    private static volatile File sBigramStorageFile = null;
    private static final Object sFileLock = new Object();
    private static final ExecutorService sIoExecutor = Executors.newSingleThreadExecutor();

    /**
     * Initializes the user transliteration dictionary with application context.
     */
    public static void init(final Context context) {
        if (context == null) return;
        try {
            final Context appCtx = context.getApplicationContext();
            final Context effectiveContext = (appCtx != null) ? appCtx : context;
            final File dir = effectiveContext.getFilesDir();
            if (dir == null) return;
            synchronized (sFileLock) {
                if (sStorageFile == null) {
                    sStorageFile = new File(dir, "user_avro_dictionary.json");
                    sBigramStorageFile = new File(dir, "user_avro_bigrams.json");
                    sIoExecutor.execute(() -> {
                        synchronized (sFileLock) {
                            loadFromFileLocked();
                            loadBigramsFromFileLocked();
                        }
                    });
                }
            }
        } catch (final Throwable ignored) {
        }
    }

    /**
     * Sets custom storage file (used in production or unit tests).
     */
    public static void setStorageFile(final File file) {
        synchronized (sFileLock) {
            sStorageFile = file;
            if (file != null && file.getParentFile() != null) {
                sBigramStorageFile = new File(file.getParentFile(), "user_avro_bigrams.json");
            }
            loadFromFileLocked();
            loadBigramsFromFileLocked();
        }
    }

    public static void setBigramStorageFile(final File file) {
        synchronized (sFileLock) {
            sBigramStorageFile = file;
            loadBigramsFromFileLocked();
        }
    }

    public static File getStorageFile() {
        return sStorageFile;
    }

    public static File getBigramStorageFile() {
        return sBigramStorageFile;
    }

    /**
     * Checks whether the string contains at least one Bengali Unicode character (\u0980-\u09FF).
     */
    public static boolean containsBengaliCharacters(final String s) {
        if (s == null) return false;
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c >= 0x0980 && c <= 0x09FF) {
                return true;
            }
        }
        return false;
    }

    /**
     * Learns an Avro transliteration mapping rawLatin -> chosenBengali (or loanword/English selection).
     * Increments usage count and updates last used timestamp.
     */
    public static void learnWord(final String rawLatin, final String chosenBengali) {
        if (rawLatin == null || chosenBengali == null) return;
        final String trimmedLatin = rawLatin.trim();
        final String trimmedBengali = chosenBengali.trim();
        if (trimmedLatin.isEmpty() || trimmedBengali.isEmpty()) return;

        // Raw input must be phonetic Latin or Banglish input (not raw Bengali text)
        if (containsBengaliCharacters(trimmedLatin)) return;

        final String key = trimmedLatin.toLowerCase(Locale.ROOT);
        final ConcurrentHashMap<String, TransliterationEntry> wordMap =
                sLearnedDictionary.computeIfAbsent(key, k -> new ConcurrentHashMap<>());

        wordMap.compute(trimmedBengali, (bengali, existingEntry) -> {
            if (existingEntry != null) {
                existingEntry.incrementCount();
                return existingEntry;
            } else {
                return new TransliterationEntry(bengali, 1, System.currentTimeMillis());
            }
        });

        scheduleAsyncSave();
    }

    /**
     * Records a dynamic bigram transition (prevWord -> nextWord).
     */
    public static void recordBigram(final String prevWord, final String nextWord) {
        if (prevWord == null || nextWord == null) return;
        final String prev = prevWord.trim();
        final String next = nextWord.trim();
        if (prev.isEmpty() || next.isEmpty()) return;

        // If Latin/loanword, normalize key to lowercase for seamless case-insensitive retrieval
        final String key = containsBengaliCharacters(prev) ? prev : prev.toLowerCase(Locale.ROOT);
        final ConcurrentHashMap<String, BigramEntry> wordMap =
                sLearnedBigrams.computeIfAbsent(key, k -> new ConcurrentHashMap<>());

        wordMap.compute(next, (w, existingEntry) -> {
            if (existingEntry != null) {
                existingEntry.incrementCount();
                return existingEntry;
            } else {
                return new BigramEntry(w, 1, System.currentTimeMillis());
            }
        });

        scheduleAsyncSave();
    }

    /**
     * Returns learned next-word bigram predictions for prevWord, sorted by frequency descending,
     * then by recency descending.
     */
    public static List<String> getLearnedBigramPredictions(final String prevWord) {
        if (prevWord == null || prevWord.isEmpty()) return Collections.emptyList();
        final String prev = prevWord.trim();
        final String key = containsBengaliCharacters(prev) ? prev : prev.toLowerCase(Locale.ROOT);
        ConcurrentHashMap<String, BigramEntry> wordMap = sLearnedBigrams.get(key);
        if ((wordMap == null || wordMap.isEmpty()) && !key.equals(prev)) {
            wordMap = sLearnedBigrams.get(prev);
        }
        if (wordMap == null || wordMap.isEmpty()) return Collections.emptyList();

        final List<BigramEntry> list = new ArrayList<>(wordMap.values());
        Collections.sort(list, (o1, o2) -> {
            if (o1.count != o2.count) {
                return Integer.compare(o2.count, o1.count);
            }
            return Long.compare(o2.lastUsedTimestamp, o1.lastUsedTimestamp);
        });

        final List<String> result = new ArrayList<>(list.size());
        for (BigramEntry e : list) {
            result.add(e.nextWord);
        }
        return result;
    }

    /**
     * Decrements the count of chosenBengali for rawLatin, or forgets it if count reaches 0.
     */
    public static boolean unlearnWord(final String rawLatin, final String chosenBengali) {
        if (rawLatin == null || chosenBengali == null) return false;
        final String key = rawLatin.trim().toLowerCase(Locale.ROOT);
        final ConcurrentHashMap<String, TransliterationEntry> wordMap = sLearnedDictionary.get(key);
        if (wordMap == null) return false;

        final boolean[] updated = new boolean[1];
        wordMap.computeIfPresent(chosenBengali.trim(), (bengali, entry) -> {
            entry.count--;
            updated[0] = true;
            if (entry.count <= 0) {
                return null;
            }
            return entry;
        });

        if (wordMap.isEmpty()) {
            sLearnedDictionary.remove(key, wordMap);
        }
        if (updated[0]) {
            scheduleAsyncSave();
        }
        return updated[0];
    }

    public static boolean unlearnWord(final String rawLatin) {
        return forgetWord(rawLatin);
    }

    /**
     * Completely removes chosenBengali for rawLatin from learned dictionary.
     */
    public static boolean forgetWord(final String rawLatin, final String chosenBengali) {
        if (rawLatin == null || chosenBengali == null) return false;
        final String key = rawLatin.trim().toLowerCase(Locale.ROOT);
        final ConcurrentHashMap<String, TransliterationEntry> wordMap = sLearnedDictionary.get(key);
        if (wordMap == null) return false;

        final boolean removed = wordMap.remove(chosenBengali.trim()) != null;
        if (wordMap.isEmpty()) {
            sLearnedDictionary.remove(key, wordMap);
        }
        if (removed) {
            scheduleAsyncSave();
        }
        return removed;
    }

    /**
     * Completely removes all learned mappings for rawLatin.
     */
    public static boolean forgetWord(final String rawLatin) {
        if (rawLatin == null) return false;
        final String key = rawLatin.trim().toLowerCase(Locale.ROOT);
        final boolean removed = sLearnedDictionary.remove(key) != null;
        if (removed) {
            scheduleAsyncSave();
        }
        return removed;
    }

    /**
     * Clears all learned words and bigrams in memory and storage.
     */
    public static void clearLearnedWords() {
        sLearnedDictionary.clear();
        sLearnedBigrams.clear();
        flushSynchronously();
    }

    /**
     * Clears in-memory dictionary cache without writing to disk (useful for testing reload).
     */
    public static void clearMemoryOnlyForTest() {
        sLearnedDictionary.clear();
        sLearnedBigrams.clear();
    }

    /**
     * Returns the highest frequency learned word entry for rawLatin, or null if none.
     */
    public static TransliterationEntry getTopLearnedWord(final String rawLatin) {
        if (rawLatin == null || rawLatin.isEmpty()) return null;
        final String key = rawLatin.trim().toLowerCase(Locale.ROOT);
        final ConcurrentHashMap<String, TransliterationEntry> wordMap = sLearnedDictionary.get(key);
        if (wordMap == null || wordMap.isEmpty()) return null;

        TransliterationEntry best = null;
        for (TransliterationEntry entry : wordMap.values()) {
            if (best == null) {
                best = entry;
            } else if (entry.count > best.count) {
                best = entry;
            } else if (entry.count == best.count && entry.lastUsedTimestamp > best.lastUsedTimestamp) {
                best = entry;
            }
        }
        return best;
    }

    /**
     * Returns learned Bengali candidates for rawLatin, sorted by frequency count descending,
     * then by recency descending. Supports prefix matching when key length >= 2.
     */
    public static List<String> getLearnedCandidates(final String rawLatin) {
        if (rawLatin == null || rawLatin.isEmpty()) return Collections.emptyList();
        final String key = rawLatin.trim().toLowerCase(Locale.ROOT);
        final LinkedHashSet<String> resultSet = new LinkedHashSet<>();

        // 1. Exact key match
        final ConcurrentHashMap<String, TransliterationEntry> wordMap = sLearnedDictionary.get(key);
        if (wordMap != null && !wordMap.isEmpty()) {
            final List<TransliterationEntry> list = new ArrayList<>(wordMap.values());
            Collections.sort(list, (o1, o2) -> {
                if (o1.count != o2.count) {
                    return Integer.compare(o2.count, o1.count);
                }
                return Long.compare(o2.lastUsedTimestamp, o1.lastUsedTimestamp);
            });
            for (TransliterationEntry e : list) {
                resultSet.add(e.chosenBengali);
            }
        }

        // 2. Prefix matching for keys starting with rawLatin (prefix length >= 2)
        if (key.length() >= 2) {
            final List<TransliterationEntry> prefixMatches = new ArrayList<>();
            for (Map.Entry<String, ConcurrentHashMap<String, TransliterationEntry>> entry : sLearnedDictionary.entrySet()) {
                final String k = entry.getKey();
                if (!k.equals(key) && k.startsWith(key)) {
                    prefixMatches.addAll(entry.getValue().values());
                }
            }
            if (!prefixMatches.isEmpty()) {
                Collections.sort(prefixMatches, (o1, o2) -> {
                    if (o1.count != o2.count) {
                        return Integer.compare(o2.count, o1.count);
                    }
                    return Long.compare(o2.lastUsedTimestamp, o1.lastUsedTimestamp);
                });
                for (TransliterationEntry e : prefixMatches) {
                    resultSet.add(e.chosenBengali);
                }
            }
        }

        return new ArrayList<>(resultSet);
    }

    public static String serializeToJson() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        boolean firstKey = true;
        for (Map.Entry<String, ConcurrentHashMap<String, TransliterationEntry>> e : sLearnedDictionary.entrySet()) {
            if (!firstKey) sb.append(",\n");
            firstKey = false;
            sb.append("  \"").append(escapeJson(e.getKey())).append("\": {\n");
            boolean firstWord = true;
            for (Map.Entry<String, TransliterationEntry> we : e.getValue().entrySet()) {
                if (!firstWord) sb.append(",\n");
                firstWord = false;
                final TransliterationEntry te = we.getValue();
                sb.append("    \"").append(escapeJson(we.getKey())).append("\": {");
                sb.append("\"count\":").append(te.count).append(",");
                sb.append("\"timestamp\":").append(te.lastUsedTimestamp);
                sb.append("}");
            }
            sb.append("\n  }");
        }
        sb.append("\n}\n");
        return sb.toString();
    }

    public static String serializeBigramsToJson() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        boolean firstKey = true;
        for (Map.Entry<String, ConcurrentHashMap<String, BigramEntry>> e : sLearnedBigrams.entrySet()) {
            if (!firstKey) sb.append(",\n");
            firstKey = false;
            sb.append("  \"").append(escapeJson(e.getKey())).append("\": {\n");
            boolean firstWord = true;
            for (Map.Entry<String, BigramEntry> we : e.getValue().entrySet()) {
                if (!firstWord) sb.append(",\n");
                firstWord = false;
                final BigramEntry te = we.getValue();
                sb.append("    \"").append(escapeJson(we.getKey())).append("\": {");
                sb.append("\"count\":").append(te.count).append(",");
                sb.append("\"timestamp\":").append(te.lastUsedTimestamp);
                sb.append("}");
            }
            sb.append("\n  }");
        }
        sb.append("\n}\n");
        return sb.toString();
    }

    private static String escapeJson(final String s) {
        if (s == null) return "";
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') sb.append("\\\"");
            else if (c == '\\') sb.append("\\\\");
            else if (c == '\b') sb.append("\\b");
            else if (c == '\f') sb.append("\\f");
            else if (c == '\n') sb.append("\\n");
            else if (c == '\r') sb.append("\\r");
            else if (c == '\t') sb.append("\\t");
            else sb.append(c);
        }
        return sb.toString();
    }

    public static void deserializeFromJson(final String json) {
        if (json == null || json.trim().isEmpty()) return;
        try {
            int i = 0;
            final int len = json.length();
            String currentLatin = null;
            String currentBengali = null;
            int currentCount = 1;
            long currentTimestamp = 0;
            int depth = 0;

            while (i < len) {
                char c = json.charAt(i);
                if (c == '{') {
                    depth++;
                    i++;
                } else if (c == '}') {
                    if (depth == 3 && currentLatin != null && currentBengali != null) {
                        ConcurrentHashMap<String, TransliterationEntry> wordMap = sLearnedDictionary.get(currentLatin);
                        if (wordMap == null) {
                            wordMap = new ConcurrentHashMap<>();
                            sLearnedDictionary.put(currentLatin, wordMap);
                        }
                        wordMap.put(currentBengali, new TransliterationEntry(currentBengali, currentCount, currentTimestamp));
                        currentBengali = null;
                        currentCount = 1;
                        currentTimestamp = 0;
                    } else if (depth == 2) {
                        currentLatin = null;
                    }
                    depth--;
                    i++;
                } else if (c == '"') {
                    i++;
                    final StringBuilder sb = new StringBuilder();
                    while (i < len) {
                        char sc = json.charAt(i);
                        if (sc == '\\' && i + 1 < len) {
                            i++;
                            char esc = json.charAt(i);
                            if (esc == 'n') sb.append('\n');
                            else if (esc == 't') sb.append('\t');
                            else if (esc == 'r') sb.append('\r');
                            else if (esc == 'u' && i + 4 < len) {
                                String hex = json.substring(i + 1, i + 5);
                                sb.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                            } else sb.append(esc);
                        } else if (sc == '"') {
                            i++;
                            break;
                        } else {
                            sb.append(sc);
                        }
                        i++;
                    }
                    final String str = sb.toString();
                    while (i < len && (Character.isWhitespace(json.charAt(i)) || json.charAt(i) == ':')) {
                        i++;
                    }
                    if (depth == 1) {
                        currentLatin = str;
                    } else if (depth == 2) {
                        currentBengali = str;
                        currentCount = 1;
                        currentTimestamp = System.currentTimeMillis();
                    } else if (depth == 3) {
                        if ("count".equals(str)) {
                            final StringBuilder num = new StringBuilder();
                            while (i < len && (Character.isDigit(json.charAt(i)) || json.charAt(i) == '-')) {
                                num.append(json.charAt(i++));
                            }
                            if (num.length() > 0) currentCount = Integer.parseInt(num.toString());
                        } else if ("timestamp".equals(str)) {
                            final StringBuilder num = new StringBuilder();
                            while (i < len && (Character.isDigit(json.charAt(i)) || json.charAt(i) == '-')) {
                                num.append(json.charAt(i++));
                            }
                            if (num.length() > 0) currentTimestamp = Long.parseLong(num.toString());
                        }
                    }
                } else {
                    i++;
                }
            }
        } catch (final Throwable t) {
            Log.e(TAG, "Failed to parse json", t);
        }
    }

    public static void deserializeBigramsFromJson(final String json) {
        if (json == null || json.trim().isEmpty()) return;
        try {
            int i = 0;
            final int len = json.length();
            String currentPrev = null;
            String currentNext = null;
            int currentCount = 1;
            long currentTimestamp = 0;
            int depth = 0;

            while (i < len) {
                char c = json.charAt(i);
                if (c == '{') {
                    depth++;
                    i++;
                } else if (c == '}') {
                    if (depth == 3 && currentPrev != null && currentNext != null) {
                        ConcurrentHashMap<String, BigramEntry> wordMap = sLearnedBigrams.get(currentPrev);
                        if (wordMap == null) {
                            wordMap = new ConcurrentHashMap<>();
                            sLearnedBigrams.put(currentPrev, wordMap);
                        }
                        wordMap.put(currentNext, new BigramEntry(currentNext, currentCount, currentTimestamp));
                        currentNext = null;
                        currentCount = 1;
                        currentTimestamp = 0;
                    } else if (depth == 2) {
                        currentPrev = null;
                    }
                    depth--;
                    i++;
                } else if (c == '"') {
                    i++;
                    final StringBuilder sb = new StringBuilder();
                    while (i < len) {
                        char sc = json.charAt(i);
                        if (sc == '\\' && i + 1 < len) {
                            i++;
                            char esc = json.charAt(i);
                            if (esc == 'n') sb.append('\n');
                            else if (esc == 't') sb.append('\t');
                            else if (esc == 'r') sb.append('\r');
                            else if (esc == 'u' && i + 4 < len) {
                                String hex = json.substring(i + 1, i + 5);
                                sb.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                            } else sb.append(esc);
                        } else if (sc == '"') {
                            i++;
                            break;
                        } else {
                            sb.append(sc);
                        }
                        i++;
                    }
                    final String str = sb.toString();
                    while (i < len && (Character.isWhitespace(json.charAt(i)) || json.charAt(i) == ':')) {
                        i++;
                    }
                    if (depth == 1) {
                        currentPrev = str;
                    } else if (depth == 2) {
                        currentNext = str;
                        currentCount = 1;
                        currentTimestamp = System.currentTimeMillis();
                    } else if (depth == 3) {
                        if ("count".equals(str)) {
                            final StringBuilder num = new StringBuilder();
                            while (i < len && (Character.isDigit(json.charAt(i)) || json.charAt(i) == '-')) {
                                num.append(json.charAt(i++));
                            }
                            if (num.length() > 0) currentCount = Integer.parseInt(num.toString());
                        } else if ("timestamp".equals(str)) {
                            final StringBuilder num = new StringBuilder();
                            while (i < len && (Character.isDigit(json.charAt(i)) || json.charAt(i) == '-')) {
                                num.append(json.charAt(i++));
                            }
                            if (num.length() > 0) currentTimestamp = Long.parseLong(num.toString());
                        }
                    }
                } else {
                    i++;
                }
            }
        } catch (final Throwable t) {
            Log.e(TAG, "Failed to parse bigram json", t);
        }
    }

    private static void loadFromFileLocked() {
        if (sStorageFile == null || !sStorageFile.exists()) return;
        try (final FileInputStream fis = new FileInputStream(sStorageFile);
             final InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             final BufferedReader reader = new BufferedReader(isr)) {
            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            deserializeFromJson(sb.toString());
        } catch (final Throwable t) {
            Log.e(TAG, "Failed to load user avro dictionary", t);
        }
    }

    private static void loadBigramsFromFileLocked() {
        if (sBigramStorageFile == null || !sBigramStorageFile.exists()) return;
        try (final FileInputStream fis = new FileInputStream(sBigramStorageFile);
             final InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             final BufferedReader reader = new BufferedReader(isr)) {
            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            deserializeBigramsFromJson(sb.toString());
        } catch (final Throwable t) {
            Log.e(TAG, "Failed to load user avro bigrams", t);
        }
    }

    private static final java.util.concurrent.atomic.AtomicBoolean sSaveScheduled =
            new java.util.concurrent.atomic.AtomicBoolean(false);

    /**
     * Synchronously persists learned dictionary and bigrams to storage files with atomic replace.
     */
    public static void flushSynchronously() {
        synchronized (sFileLock) {
            if (sStorageFile != null) {
                try {
                    final String json = serializeToJson();
                    final File tempFile = new File(sStorageFile.getAbsolutePath() + ".tmp");
                    final File parent = tempFile.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    try (final FileOutputStream fos = new FileOutputStream(tempFile);
                         final OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                         final BufferedWriter writer = new BufferedWriter(osw)) {
                        writer.write(json);
                        writer.flush();
                        try {
                            fos.getFD().sync();
                        } catch (final Throwable ignored) {
                        }
                    }
                    if (!tempFile.renameTo(sStorageFile)) {
                        if (sStorageFile.exists()) {
                            sStorageFile.delete();
                        }
                        tempFile.renameTo(sStorageFile);
                    }
                } catch (final Throwable t) {
                    Log.e(TAG, "Failed to flush user avro dictionary", t);
                }
            }

            if (sBigramStorageFile != null) {
                try {
                    final String json = serializeBigramsToJson();
                    final File tempFile = new File(sBigramStorageFile.getAbsolutePath() + ".tmp");
                    final File parent = tempFile.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    try (final FileOutputStream fos = new FileOutputStream(tempFile);
                         final OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                         final BufferedWriter writer = new BufferedWriter(osw)) {
                        writer.write(json);
                        writer.flush();
                        try {
                            fos.getFD().sync();
                        } catch (final Throwable ignored) {
                        }
                    }
                    if (!tempFile.renameTo(sBigramStorageFile)) {
                        if (sBigramStorageFile.exists()) {
                            sBigramStorageFile.delete();
                        }
                        tempFile.renameTo(sBigramStorageFile);
                    }
                } catch (final Throwable t) {
                    Log.e(TAG, "Failed to flush user avro bigrams", t);
                }
            }
        }
    }

    private static void scheduleAsyncSave() {
        if (sSaveScheduled.compareAndSet(false, true)) {
            sIoExecutor.execute(() -> {
                try {
                    Thread.sleep(150);
                } catch (final InterruptedException ignored) {
                } finally {
                    sSaveScheduled.set(false);
                    flushSynchronously();
                }
            });
        }
    }

        private static final Map<String, String> EMOJI_MAP = new HashMap<>();
    public static String getEmojiForWord(final String word) {
        if (word == null) return null;
        return EMOJI_MAP.get(word.trim().toLowerCase(Locale.ROOT));
    }

    private static final Map<String, String[]> BANGLISH_ALIASES = new HashMap<>();
    private static final Set<String> SHORTHAND_ABBREVIATIONS = new HashSet<>();
    private static final Map<String, String[]> CURATED_CHAT_SUGGESTIONS = new HashMap<>(256);

    static {
        // Bengali & Banglish to Emoji
        final String[][] emojiPairs = {
            {"love", "❤️"}, {"valobasha", "❤️"}, {"bhalobasha", "❤️"}, {"valobasi", "❤️"}, {"bhalobasi", "❤️"},
            {"ভালোবাসা", "❤️"}, {"ভালোবাসি", "❤️"}, {"প্রেম", "❤️"}, {"prem", "❤️"}, {"heart", "❤️"}, {"valobasa", "❤️"},
            {"hasi", "😊"}, {"smile", "😊"}, {"happy", "😊"}, {"khushi", "😊"}, {"kushi", "😊"}, {"হাসি", "😊"}, {"খুশি", "😊"},
            {"kanna", "😭"}, {"cry", "😭"}, {"sad", "😭"}, {"dukho", "😭"}, {"dukkho", "😭"}, {"কান্না", "😭"}, {"দুঃখ", "😭"},
            {"fire", "🔥"}, {"hot", "🔥"}, {"agun", "🔥"}, {"আগুন", "🔥"},
            {"cha", "☕"}, {"tea", "☕"}, {"coffee", "☕"}, {"চা", "☕"}, {"কফি", "☕"},
            {"taka", "💰"}, {"money", "💰"}, {"টাকা", "💰"}, {"poisa", "💰"}, {"পয়সা", "💰"},
            {"bd", "🇧🇩"}, {"bangladesh", "🇧🇩"}, {"বাংলাদেশ", "🇧🇩"},
            {"ok", "👍"}, {"thik", "👍"}, {"thikase", "👍"}, {"ঠিক", "👍"}, {"jos", "👍"}, {"like", "👍"},
            {"congrats", "🎉"}, {"party", "🎉"}, {"shuvechha", "🎉"}, {"suvecha", "🎉"}, {"অভিনন্দন", "🎉"}, {"শুভেচ্ছা", "🎉"},
            {"cool", "😎"}, {"smart", "😎"},
            {"flower", "🌸"}, {"ful", "🌸"}, {"ফুল", "🌸"}, {"golap", "🌹"}, {"rose", "🌹"},
            {"star", "⭐"}, {"tara", "⭐"}, {"তারা", "⭐"},
            {"bye", "👋"}, {"tata", "👋"}, {"bidai", "👋"}, {"বিদায়", "👋"}, {"বিদায়", "👋"},
            {"gift", "🎁"}, {"upohar", "🎁"}, {"উপহার", "🎁"},
            {"biral", "🐱"}, {"cat", "🐱"}, {"বিড়াল", "🐱"},
            {"kukur", "🐶"}, {"dog", "🐶"}, {"কুকুর", "🐶"},
            {"mach", "🐟"}, {"fish", "🐟"}, {"মাছ", "🐟"},
            {"food", "🍔"}, {"khabar", "🍔"}, {"খাবার", "🍔"},
            {"sweet", "🍬"}, {"misti", "🍬"}, {"মিষ্টি", "🍬"},
            {"car", "🚗"}, {"gari", "🚗"}, {"গাড়ি", "🚗"},
            {"namaz", "🤲"}, {"dua", "🤲"}, {"দোয়া", "🤲"}, {"দোয়া", "🤲"}
        };
        for (String[] pair : emojiPairs) {
            EMOJI_MAP.put(pair[0].toLowerCase(Locale.ROOT), pair[1]);
        }

        // Avro zero-shift conjuncts (যুক্তবর্ণ) & Visarga (বিসর্গ) words
        addShorthand("dukkho", "দুঃখ", "দুঃখী");
        addShorthand("dukho", "দুঃখ", "দুঃখী");
        addShorthand("dukhi", "দুঃখী", "দুঃখ");
        addShorthand("du:khi", "দুঃখী", "দুঃখ");
        addShorthand("du:kho", "দুঃখ", "দুঃখী");
        addShorthand("dukkhito", "দুঃখিত");
        addShorthand("dukhito", "দুঃখিত");
        addShorthand("prosno", "প্রশ্ন");
        addAlias("proshno", "প্রশ্ন");
        addAlias("proshnotor", "প্রশ্নোত্তর");
        addShorthand("somossa", "সমস্যা");
        addAlias("shomossha", "সমস্যা");
        addAlias("somossha", "সমস্যা");
        addAlias("shonge", "সঙ্গে");
        addAlias("songe", "সঙ্গে");
        addAlias("shongo", "সঙ্গ");
        addAlias("songo", "সঙ্গ");
        addAlias("gyan", "জ্ঞান");
        addAlias("gyani", "জ্ঞানী");
        addAlias("koshto", "কষ্ট");
        addAlias("kosto", "কষ্ট");
        addAlias("koste", "কষ্টে");
        addAlias("koshte", "কষ্টে");
        addAlias("srishti", "সৃষ্টি");
        addAlias("sristi", "সৃষ্টি");
        addAlias("drishti", "দৃষ্টি");
        addAlias("dristi", "দৃষ্টি");
        addAlias("porikkha", "পরীক্ষা");
        addAlias("porikha", "পরীক্ষা");
        addAlias("porikkhar", "পরীক্ষার");
        addAlias("biswas", "বিশ্বাস");
        addAlias("bisshas", "বিশ্বাস");
        addAlias("biswash", "বিশ্বাস");
        addShorthand("bismito", "বিস্মিত");
        addAlias("bho", "ভ");
        addShorthand("kkh", "ক্ষ");
        addShorthand("ksha", "ক্ষ");
        addShorthand("gy", "জ্ঞ");
        addShorthand("gg", "জ্ঞ");
        addShorthand("sht", "ষ্ট", "ষ্ঠ");
        addShorthand("cch", "চ্ছ");
        addShorthand("ngg", "ঙ্গ");
        addShorthand("dhdh", "দ্ধ");
        addShorthand("ddh", "দ্ধ");
        addShorthand("tth", "ঠ");
        addShorthand(":", "ঃ");
        addShorthand("::", "ঃ");
        addShorthand("hs", "\u09CD");
        addAlias("aitasi", "আইতাছি", "আসছি");
        addAlias("aitachi", "আইতাছি");
        addAlias("oitasi", "হইতাছি", "আইতাছি");
        addAlias("jaitasi", "যাইতেছি", "যাচ্ছি");
        addAlias("jaitachi", "যাইতেছি");
        addAlias("kothay", "কোথায়", "কই");
        addAlias("kemon", "কেমন");
        addAlias("achen", "আছেন");
        addAlias("acho", "আছো");
        addAlias("achi", "আছি");
        addAlias("korcho", "করছো");
        addAlias("korchen", "করছেন");
        addAlias("korbo", "করব", "করবো");
        addAlias("korlam", "করলাম");
        addAlias("kortesi", "করতেছি", "করছি");
        addAlias("kortasi", "করতেছি");
        addAlias("valo", "ভালো", "ভাল");
        addAlias("bhalo", "ভালো", "ভাল");
        addAlias("bhabte", "ভাবতে");
        addAlias("bhabto", "ভাবতো");
        addAlias("shobar", "সবার");
        addAlias("sobar", "সবার");
        addAlias("shobai", "সবাই");
        addAlias("sobai", "সবাই");
        addAlias("khub", "খুব");
        addAlias("onek", "অনেক");
        addAlias("thikase", "ঠিক আছে", "ঠিকআছে");
        addAlias("thikache", "ঠিক আছে", "ঠিকআছে");
        addAlias("dorkar", "দরকার");
        addAlias("ghuma", "ঘুমা");
        addAlias("ghumate", "ঘুমাতে");
        addAlias("shune", "শুনে");
        addAlias("sune", "শুনে");
        addShorthand("HH", "ঃ");
        addShorthand("hh", "ঃ");
        addAlias("khoma", "ক্ষমা");
        addAlias("kkhoma", "ক্ষমা");
        addAlias("kkhoti", "ক্ষতি");
        addAlias("khoti", "ক্ষতি");
        addAlias("khuda", "ক্ষুধা");
        addAlias("kkhuda", "ক্ষুধা");
        addAlias("lokkhon", "লক্ষণ", "লক্ষ্য");
        addAlias("lokkho", "লক্ষ", "লক্ষ্য");
        addAlias("bikkhato", "বিখ্যাত");
        addAlias("bikhyato", "বিখ্যাত");
        addAlias("oviggota", "অভিজ্ঞতা");
        addAlias("biggapon", "বিজ্ঞাপন");
        addAlias("bigyan", "বিজ্ঞান");
        addAlias("bijnan", "বিজ্ঞান");
        addAlias("agya", "আজ্ঞা");
        addAlias("obiggo", "অভিজ্ঞ");
        addAlias("obiggyo", "অভিজ্ঞ");
        addAlias("bishes", "বিশেষ");
        addAlias("bishesh", "বিশেষ");
        addAlias("bishesvabe", "বিশেষভাবে");
        addAlias("obostha", "অবস্থা");
        addAlias("byabohar", "ব্যবহার");
        addAlias("babohar", "ব্যবহার");
        addAlias("byartho", "ব্যর্থ");
        addAlias("bartho", "ব্যর্থ");
        addAlias("byatha", "ব্যথা");
        addAlias("batha", "ব্যথা");
        addAlias("byasto", "ব্যস্ত");
        addAlias("basto", "ব্যস্ত");
        addAlias("shanto", "শান্ত");
        addAlias("santo", "শান্ত");
        addAlias("klanto", "ক্লান্ত");
        addAlias("mukto", "মুক্ত");
        addAlias("shokto", "শক্ত");
        addAlias("sokto", "শক্ত");
        addAlias("bhokto", "ভক্ত");
        addAlias("vokto", "ভক্ত");
        addAlias("rokto", "রক্ত");
        addAlias("rokte", "রক্তে");
        addAlias("ukti", "উক্তি");
        addAlias("yukti", "যুক্তি");
        addAlias("jukti", "যুক্তি");
        addAlias("sotto", "সত্য");
        addAlias("shotto", "সত্য");
        addAlias("mitthe", "মিথ্যা");
        addAlias("mittha", "মিথ্যা");
        addAlias("anondo", "আনন্দ");
        addAlias("utsah", "উৎসাহ");
        addAlias("utsob", "উৎসব");
        addAlias("bhabna", "ভাবনা");
        addAlias("chinta", "চিন্তা");

        // Conversational essentials & greetings
        addShorthand("kmn", "কেমন", "কমন");
        addShorthand("kmnacho", "কেমন আছো");
        addShorthand("kmnaso", "কেমন আছো");
        addShorthand("kemonacho", "কেমন আছো");
        addShorthand("valo", "ভালো", "ভাল");
        addShorthand("bhalo", "ভালো", "ভাল");
        addShorthand("valona", "ভালো না", "ভালোনা", "ভাল না");
        addShorthand("bhalona", "ভালো না", "ভালোনা");
        addShorthand("valoacho", "ভালো আছো");
        addShorthand("bhaloacho", "ভালো আছো");
        addShorthand("valoachi", "ভালো আছি");
        addShorthand("bhaloachi", "ভালো আছি");
        addShorthand("thikase", "ঠিক আছে");
        addShorthand("thikache", "ঠিক আছে");
        addShorthand("kisu", "কিছু");
        addShorthand("kichu", "কিছু");
        addShorthand("shob", "সব", "শব");
        addShorthand("sob", "সব");
        addShorthand("dhonn", "ধন্যবাদ", "ধন্য");
        addShorthand("dhonno", "ধন্যবাদ", "ধন্য");
        addShorthand("dhonnobad", "ধন্যবাদ");
        addShorthand("hobena", "হবে না", "হবেনা");
        addShorthand("parbona", "পারব না");
        addShorthand("janina", "জানি না");
        addShorthand("korbona", "করব না");
        addShorthand("pic", "ছবি");
        addShorthand("plz", "দয়া করে");
        addShorthand("please", "দয়া করে");
        addShorthand("thx", "ধন্যবাদ");
        addShorthand("tnx", "ধন্যবাদ");
        addShorthand("inshallah", "ইনশাআল্লাহ");
        addShorthand("inshaallah", "ইনশাআল্লাহ");
        addShorthand("alhamdulillah", "আলহামদুলিল্লাহ");
        addShorthand("mashallah", "মাশাআল্লাহ");
        addShorthand("subhanallah", "সুবহানাল্লাহ");
        addShorthand("bismillah", "বিসমিল্লাহ");
        addShorthand("khodahafez", "খোদা হাফেজ");
        addShorthand("shubhoratri", "শুভরাত্রি");
        addShorthand("shubhoshokal", "শুভসকাল");
        addShorthand("ekhonow", "এখনও", "এখনো");
        addShorthand("ekhono", "এখনো", "এখনও");
        addShorthand("hoynai", "হয়নি", "হয় নাই", "হয়নাই");
        addShorthand("ashtese", "আসতেছে", "আসছে");
        addShorthand("astese", "আসতেছে", "আসছে");
        addShorthand("likhe", "লিখে");
        addShorthand("dile", "দিলে");
        addShorthand("ghumabo", "ঘুমাবো", "ঘুমাব");
        addShorthand("baki", "বাকি");

        // High-frequency Banglish conversational verbs & pronouns (Ridmik & Gboard standard)
        addAlias("jachhi", "যাচ্ছি");
        addAlias("jacchi", "যাচ্ছি");
        addAlias("jachhe", "যাচ্ছে");
        addAlias("jacche", "যাচ্ছে");
        addAlias("jabo", "যাব", "যাবো");
        addAlias("jao", "যাও");
        addAlias("jawa", "যাওয়া");
        addAlias("jaben", "যাবেন");
        addAlias("jabona", "যাব না");
        addAlias("jete", "যেতে");
        addAlias("jemon", "যেমন");
        addAlias("jekhane", "যেখানে");
        addAlias("jeno", "যেন");
        addAlias("jara", "যারা");
        addAlias("jar", "যার");
        addAlias("jate", "যাতে");
        addAlias("jokhon", "যখন");
        addAlias("jeta", "যেটা");

        addAlias("thik", "ঠিক", "ঠিক আছে");
        addAlias("chai", "চাই");
        addAlias("chaibena", "চাইবে না", "চাইবেনা");
        addAlias("chaibo", "চাইব", "চাইবো");
        addAlias("chao", "চাও");
        addAlias("chan", "চান");
        addAlias("chay", "চায়");
        addAlias("chole", "চলে");
        addAlias("chobi", "ছবি");
        addAlias("cholo", "চলো", "চল");

        addAlias("eita", "এইটা", "এটা");
        addAlias("ekta", "একটা", "একটু");
        addAlias("oita", "ওইটা", "ওটা");
        addAlias("sheita", "সেইটা", "সেটা");
        addAlias("konta", "কোনটা");
        addAlias("shobta", "সবটা");
        addAlias("sobta", "সবটা");

        addAlias("koro", "করো", "কর");
        addAlias("bolo", "বলো", "বল");
        addAlias("dekho", "দেখো", "দেখ");
        addAlias("raikho", "রাইখো");
        addAlias("rekho", "রেখো");
        addAlias("kono", "কোনো", "কোন");
        addAlias("shathe", "সাথে");
        addAlias("ashbo", "আসব", "আসবো");
        addAlias("asbo", "আসব", "আসবো");
        addAlias("aschhi", "আসছি");
        addAlias("aschi", "আসছি");
        addAlias("moto", "মতো", "মত");
        addAlias("khabo", "খাবো", "খাব");
        addAlias("avro", "অভ্র");
        addAlias("avra", "অভ্র");
        addAlias("ridmik", "রিদ্মিক", "রিদমিক");
        addAlias("gboard", "জিবোর্ড");
        addAlias("wp", "হোয়াটসঅ্যাপ");
        addAlias("detailed", "ডিটেইলড");
        addAlias("layout", "লেআউট");
        addAlias("lyout", "লেআউট");
        addAlias("permission", "পারমিশন");
        addAlias("allow", "অ্যালাউ");
        addAlias("issue", "ইস্যু");
        addAlias("bareo", "বারেও");
        addAlias("sokaler", "সকালের");
        addAlias("age", "আগে");
        addAlias("valo", "ভালো", "ভাল");
        addAlias("bhalo", "ভালো", "ভাল");
        addAlias("kisu", "কিছু");
        addAlias("kichu", "কিছু");
        addAlias("sobkisu", "সবকিছু");
        addAlias("sobiksu", "সবকিছু");
        addAlias("shobkisu", "সবকিছু");
        addAlias("ghumai", "ঘুমাই");
        addAlias("ghumaite", "ঘুমাতে", "ঘুমাইতে");
        addAlias("korbe", "করবে", "করবে না");

        // Pronoun shorthands & full forms
        addShorthand("amdr", "আমাদের");
        addShorthand("apnr", "আপনার");
        addShorthand("tmdr", "তোমাদের");
        addAlias("ami", "আমি");
        addAlias("amra", "আমরা");
        addAlias("amader", "আমাদের");
        addAlias("amake", "আমাকে");
        addAlias("amar", "আমার");
        addAlias("tumi", "তুমি");
        addAlias("tomra", "তোমরা");
        addAlias("tomader", "তোমাদের");
        addAlias("tomake", "তোমাকে");
        addAlias("tomar", "তোমার");
        addAlias("apni", "আপনি");
        addAlias("apnara", "আপনারা");
        addAlias("apnar", "আপনার");
        addAlias("apnake", "আপনাকে");
        addAlias("she", "সে");
        addAlias("tini", "তিনি");
        addAlias("tara", "তারা");
        addAlias("tader", "তাদের");
        addAlias("take", "তাকে");
        addAlias("tar", "তার");
        addAlias("ora", "ওরা");
        addAlias("oder", "ওদের");
        addAlias("oke", "ওকে");
        addShorthand("vai", "ভাই");
        addShorthand("bhai", "ভাই");
        addShorthand("bhaiya", "ভাইয়া");
        addShorthand("vaiya", "ভাইয়া");
        addAlias("apu", "আপু");
        addAlias("bondu", "বন্ধু");
        addAlias("bondhu", "বন্ধু");
        addAlias("shobai", "সবাই");
        addAlias("sobai", "সবাই");
        addAlias("shobaike", "সবাইকে");
        addAlias("manush", "মানুষ");
        addAlias("lok", "লোক");

        // Questions & conjunctions
        addAlias("ki", "কি", "কী");
        addAlias("kire", "কিরে");
        addAlias("keno", "কেন", "কেনো");
        addAlias("karon", "কারণ");
        addAlias("karom", "কারণ");
        addShorthand("kothay", "কোথায়");
        addAlias("kobe", "কবে");
        addAlias("kokhon", "কখন");
        addShorthand("kivabe", "কিভাবে");
        addShorthand("kemne", "কেমনে");
        addAlias("kar", "কার");
        addAlias("kake", "কাকে");
        addAlias("kiser", "কিসের");
        addAlias("kotodin", "কতদিন");
        addAlias("koto", "কত");
        addAlias("koita", "কয়টা");
        addAlias("ekhon", "এখন");
        addAlias("akhon", "এখন");
        addAlias("tokhon", "তখন");
        addAlias("jokhon", "যখন");
        addAlias("kokhono", "কখনো");
        addAlias("shobshomoy", "সবসময়");
        addAlias("sobshomoy", "সবসময়");
        addAlias("shomoy", "সময়");
        addAlias("somoy", "সময়");
        addAlias("protidin", "প্রতিদিন");
        addAlias("age", "আগে");
        addAlias("pore", "পরে");
        addAlias("por", "পর", "পরে");
        addAlias("kintu", "কিন্তু");
        addAlias("tahole", "তাহলে");
        addAlias("tobe", "তবে");
        addAlias("jodi", "যদি");
        addAlias("tai", "তাই");
        addAlias("ashe", "আসে");
        addShorthand("ashole", "আসলে");
        addShorthand("asole", "আসলে");

        // Quantifiers & affirmations
        addAlias("shobkisu", "সবকিছু");
        addAlias("shobkichu", "সবকিছু");
        addAlias("sobkisu", "সবকিছু");
        addAlias("kisui", "কিছুই");
        addAlias("kichui", "কিছুই");
        addAlias("onek", "অনেক");
        addAlias("ektu", "একটু");
        addAlias("beshi", "বেশি");
        addAlias("kom", "কম");
        addAlias("khub", "খুব");
        addAlias("pura", "পুরা");
        addAlias("shobcheye", "সবচেয়ে");
        addAlias("ha", "হ্যাঁ");
        addAlias("haa", "হ্যাঁ");
        addAlias("na", "না");
        addAlias("naa", "না");
        addAlias("nai", "নাই");
        addAlias("nei", "নেই");
        addAlias("hobe", "হবে");
        addAlias("ase", "আছে");
        addAlias("ache", "আছে");
        addAlias("thakbe", "থাকবে");
        addAlias("hoise", "হয়েছে", "হইছে");
        addAlias("hoye", "হয়ে");
        addAlias("hoyese", "হয়েছে");
        addAlias("geche", "গেছে");
        addAlias("gese", "গেছে");
        addAlias("gelo", "গেল", "গেলো");

        // Verbs & conjugations
        addAlias("kor", "কর");
        addAlias("kora", "করা");
        addAlias("kori", "করি");
        addAlias("kore", "করে");
        addAlias("koro", "করো");
        addAlias("korbo", "করব", "করবো");
        addAlias("korlam", "করলাম");
        addAlias("korchi", "করছি");
        addAlias("korso", "করছ", "করছো");
        addAlias("korchen", "করছেন");
        addAlias("korte", "করতে");
        addAlias("kortesi", "করছি", "করতেছি");
        addAlias("dekh", "দেখ");
        addAlias("dekha", "দেখা");
        addAlias("dekhi", "দেখি");
        addAlias("dekhe", "দেখে");
        addAlias("dekho", "দেখো");
        addAlias("dekhbo", "দেখব", "দেখবো");
        addAlias("dekhlam", "দেখলাম");
        addAlias("dekhchi", "দেখছি");
        addAlias("dekhso", "দেখছ", "দেখছো");
        addAlias("dekhte", "দেখতে");
        addAlias("bol", "বল");
        addAlias("bola", "বলা");
        addAlias("boli", "বলি");
        addAlias("bole", "বলে");
        addAlias("bolo", "বলো");
        addAlias("bolbo", "বলব", "বলবো");
        addAlias("bollam", "বললাম");
        addAlias("bolchi", "বলছি");
        addAlias("bolte", "বলতে");
        addAlias("boltesi", "বলছি", "বলতেছি");
        addAlias("shun", "শুন");
        addAlias("shuna", "শোনা");
        addAlias("shuni", "শুনি");
        addAlias("shunbo", "শুনব", "শুনবো");
        addAlias("shunlam", "শুনলাম");
        addAlias("shunchi", "শুনছি");
        addAlias("shunte", "শুনতে");
        addAlias("ja", "যা");
        addAlias("jawa", "যাওয়া");
        addAlias("jai", "যাই");
        addAlias("jay", "যায়");
        addAlias("jabo", "যাব", "যাবো");
        addAlias("gechi", "গেছি");
        addAlias("jete", "যেতে");
        addAlias("jaitesi", "যাচ্ছি", "যাইতেছি");
        addAlias("ash", "আস");
        addAlias("asha", "আসা");
        addAlias("ashi", "আসি");
        addAlias("asho", "আসো");
        addAlias("ashbo", "আসব", "আসবো");
        addAlias("ashlam", "আসলাম");
        addAlias("ashechi", "এসেছি");
        addAlias("ashte", "আসতে");
        addShorthand("astesi", "আসতেছে", "আসছে");
        addShorthand("ashtesi", "আসতেছে", "আসছে");
        addShorthand("astese", "আসতেছে", "আসছে");
        addShorthand("ashtese", "আসতেছে", "আসছে");
        addAlias("jan", "জান");
        addAlias("jana", "জানা");
        addAlias("jani", "জানি");
        addAlias("janbo", "জানব", "জানবো");
        addAlias("jantam", "জানতাম");
        addAlias("khao", "খাও");
        addAlias("khai", "খাই");
        addAlias("khabo", "খাব", "খাবো");
        addAlias("khawa", "খাওয়া");
        addAlias("khawa-dawa", "খাওয়া-দাওয়া");
        addAlias("ghum", "ঘুম");
        addAlias("ghumao", "ঘুমাও");
        addAlias("ghumai", "ঘুমাই");
        addAlias("por", "পড়", "পড়");
        addAlias("poro", "পড়ো", "পড়ো");
        addAlias("pori", "পড়ি", "পড়ি");
        addAlias("porbo", "পড়ব", "পড়বো");

        // Places & common nouns
        addAlias("sundor", "সুন্দর");
        addAlias("shundor", "সুন্দর");
        addAlias("bhalobasha", "ভালোবাসা", "ভালবাসা");
        addAlias("valobasha", "ভালোবাসা");
        addAlias("bhalobashi", "ভালোবাসি");
        addAlias("valobashi", "ভালোবাসি");
        addAlias("mon", "মন");
        addAlias("mone", "মনে");
        addAlias("bari", "বাড়ি", "বারি");
        addAlias("basha", "বাসা");
        addAlias("ghor", "ঘর");
        addAlias("office", "অফিস");
        addAlias("kaj", "কাজ");
        addAlias("khabar", "খাবার");
        addAlias("pani", "পানি");
        addAlias("cha", "চা");
        addAlias("khobor", "খবর");
        addAlias("bangla", "বাংলা", "বাংলাদেশ");
        addAlias("bangladesh", "বাংলাদেশ", "বাংলা");
        addAlias("dhaka", "ঢাকা");
        addAlias("chittagong", "চট্টগ্রাম");
        addAlias("sylhet", "সিলেট");
        addAlias("rajshahi", "রাজশাহী");
        addAlias("khulna", "খুলনা");
        addAlias("barisal", "বরিশাল");
        addAlias("rangpur", "রংপুর");
        addAlias("mymensingh", "ময়মনসিংহ");
        addAlias("kolkata", "কলকাতা");
        addAlias("desh", "দেশ");
        addAlias("bidesh", "বিদেশ");
        addAlias("rasta", "রাস্তা");
        addAlias("gari", "গাড়ি");
        addAlias("phone", "ফোন");
        addAlias("chobi", "ছবি");
        addAlias("gan", "গান");
        addAlias("gaan", "গান");
        addAlias("boi", "বই");
        addAlias("notun", "নতুন");
        addAlias("puraton", "পুরাতন");
        addAlias("purono", "পুরনো");
        addAlias("boro", "বড়", "বড়");
        addAlias("choto", "ছোট");
        addAlias("shathe", "সাথে");
        addAlias("sathe", "সাথে");
        addAlias("pashe", "পাশে");
        addAlias("samne", "সামনে");
        addAlias("pichone", "পিছনে");
        addAlias("majhe", "মাঝে");
        addAlias("moddhe", "মধ্যে");
        addAlias("bhetore", "ভেতরে");
        addAlias("baire", "বাইরে");
        addAlias("kalke", "কালকে");
        addAlias("agamikal", "আগামীকাল");
        addAlias("gotokal", "গতকাল");
        addAlias("ajke", "আজকে");
        addAlias("aaj", "আজ");
        addAlias("aj", "আজ");
        addAlias("din", "দিন");
        addAlias("soptaho", "সপ্তাহ");
        addAlias("mash", "মাস");
        addAlias("bochor", "বছর");
        addAlias("taka", "টাকা");
        addAlias("poisha", "পয়সা");
        addAlias("shikkha", "শিক্ষা");
        addAlias("porikkha", "পরীক্ষা");
        addShorthand("ingreji", "ইংরেজি");
        addShorthand("ingrezi", "ইংরেজি");
        addShorthand("english", "ইংরেজি");

        // Common English loanwords used in everyday Bangladeshi conversation (Banglish transliteration)
        addShorthand("account", "অ্যাকাউন্ট", "হিসাব", "account");
        addShorthand("use", "ইউজ");
        addShorthand("user", "ইউজার");
        addShorthand("post", "পোস্ট");
        addShorthand("update", "আপডেট");
        addShorthand("share", "শেয়ার", "শেয়ার");
        addShorthand("like", "লাইক");
        addShorthand("comment", "কমেন্ট");
        addShorthand("message", "মেসেজ");
        addShorthand("msg", "মেসেজ");
        addShorthand("call", "কল");
        addShorthand("check", "চেক");
        addShorthand("clear", "ক্লিয়ার", "ক্লিয়ার");
        addShorthand("link", "লিংক");
        addShorthand("video", "ভিডিও");
        addShorthand("photo", "ফটো", "ছবি");
        addShorthand("group", "গ্রুপ");
        addShorthand("page", "পেজ");
        addShorthand("online", "অনলাইন");
        addShorthand("offline", "অফলাইন");
        addShorthand("problem", "প্রবলেম", "সমস্যা");
        addShorthand("help", "হেল্প", "সাহায্য");
        addShorthand("password", "পাসওয়ার্ড", "পাসওয়ার্ড");
        addShorthand("pass", "পাস");
        addShorthand("code", "কোড");
        addShorthand("id", "আইডি");
        addShorthand("app", "অ্যাপ");
        addShorthand("apps", "অ্যাপস");
        addShorthand("test", "টেস্ট");
        addShorthand("work", "ওয়ার্ক", "কাজ");
        addShorthand("job", "জব", "চাকরি");
        addShorthand("setting", "সেটিং");
        addShorthand("settings", "সেটিংস");
        addShorthand("payment", "পেমেন্ট");
        addShorthand("pay", "পে");
        addShorthand("order", "অর্ডার");
        addShorthand("delivery", "ডেলিভারি");
        addShorthand("cancel", "ক্যান্সেল", "বাতিল");
        addShorthand("confirm", "কনফার্ম");
        addShorthand("active", "অ্যাক্টিভ");
        addShorthand("pending", "পেন্ডিং");
        addShorthand("done", "ডান");
        addShorthand("ok", "ওকে", "ঠিক আছে");
        addShorthand("okay", "ওকে", "ঠিক আছে");
        addShorthand("sorry", "সরি", "দুঃখিত");
        addShorthand("welcome", "ওয়েলকাম", "স্বাগতম");
        addShorthand("thanks", "থ্যাংকস", "ধন্যবাদ");
        addShorthand("location", "লোকেশন");
        addShorthand("address", "অ্যাড্রেস", "ঠিকানা");
        addShorthand("number", "নম্বর", "নাম্বার");
        addShorthand("balance", "ব্যালেন্স");
        addShorthand("recharge", "রিচার্জ");
        addShorthand("offer", "অফার");
        addShorthand("discount", "ডিসকাউন্ট");
        addShorthand("bill", "বিল");
        addShorthand("cash", "ক্যাশ");
        addShorthand("money", "মানি", "টাকা");
        addShorthand("bank", "ব্যাংক");
        addShorthand("office", "অফিস");
        addShorthand("school", "স্কুল", "বিদ্যালয়", "school");
        addShorthand("college", "কলেজ");
        addShorthand("varsity", "ভার্সিটি");
        addShorthand("class", "ক্লাস");
        addShorthand("exam", "এক্সাম", "পরীক্ষা");
        addShorthand("result", "রেজাল্ট", "ফলাফল");
        addShorthand("time", "টাইম", "সময়");
        addShorthand("date", "ডেট", "তারিখ");
        addShorthand("meeting", "মিটিং");
        addShorthand("bus", "বাস");
        addShorthand("train", "ট্রেন");
        addShorthand("ticket", "টিকিট");
        addShorthand("hotel", "হোটেল");
        addShorthand("doctor", "ডাক্তার");
        addShorthand("hospital", "হাসপাতাল");
        addShorthand("medicine", "মেডিসিন", "ওষুধ");
        addShorthand("report", "রিপোর্ট");
        addShorthand("mobile", "মোবাইল");
        addShorthand("phone", "ফোন");
        addShorthand("sim", "সিম");
        addShorthand("network", "নেটওয়ার্ক");
        addShorthand("internet", "ইন্টারনেট");
        addShorthand("wifi", "ওয়াইফাই");
        addShorthand("data", "ডেটা");
        addShorthand("screen", "স্ক্রিন");
        addShorthand("display", "ডিসপ্লে");
        addShorthand("battery", "ব্যাটারি");
        addShorthand("charger", "চার্জার");
        addShorthand("charge", "চার্জ");
        addShorthand("sound", "সাউন্ড");
        addShorthand("speaker", "স্পিকার");
        addShorthand("camera", "ক্যামেরা");
        addShorthand("download", "ডাউনলোড");
        addShorthand("install", "ইনস্টল");
        addShorthand("delete", "ডিলিট");
        addShorthand("remove", "রিমুভ");
        addShorthand("copy", "কপি");
        addShorthand("paste", "পেস্ট");
        addShorthand("save", "সেভ");
        addShorthand("search", "সার্চ");
        addShorthand("login", "লগইন");
        addShorthand("logout", "লগআউট");
        addShorthand("signup", "সাইনআপ");
        addShorthand("sign", "সাইন");
        addShorthand("file", "ফাইল");
        addShorthand("folder", "ফোল্ডার");
        addShorthand("system", "সিস্টেম");
        addShorthand("service", "সার্ভিস");
        addShorthand("smart", "স্মার্ট");
    }

    private static void addShorthand(String key, String... values) {
        final String lower = key.toLowerCase(Locale.ROOT);
        BANGLISH_ALIASES.put(lower, values);
        SHORTHAND_ABBREVIATIONS.add(lower);
    }

    private static void addAlias(String key, String... values) {
        BANGLISH_ALIASES.put(key.toLowerCase(Locale.ROOT), values);
    }

    private static final Map<String, String> ENGLISH_CANONICAL_MAP = new HashMap<>();

    private static void addEnglishCanonical(String variant, String canonical) {
        ENGLISH_CANONICAL_MAP.put(variant.toLowerCase(Locale.ROOT), canonical.toLowerCase(Locale.ROOT));
    }

    static {
        // English canonical typo and abbreviation mappings for bilingual Avro suggestions
        addEnglishCanonical("accout", "account");
        addEnglishCanonical("accont", "account");
        addEnglishCanonical("acnt", "account");
        addEnglishCanonical("acct", "account");
        addEnglishCanonical("facebok", "facebook");
        addEnglishCanonical("fb", "facebook");
        addEnglishCanonical("bksh", "bkash");
        addEnglishCanonical("bkas", "bkash");
        addEnglishCanonical("mesage", "message");
        addEnglishCanonical("msg", "message");
        addEnglishCanonical("pasword", "password");
        addEnglishCanonical("pass", "password");
        addEnglishCanonical("pwd", "password");
        addEnglishCanonical("pctr", "picture");
        addEnglishCanonical("pic", "picture");
        addEnglishCanonical("pics", "pictures");
        addEnglishCanonical("plz", "please");
        addEnglishCanonical("pls", "please");
        addEnglishCanonical("thx", "thanks");
        addEnglishCanonical("tnx", "thanks");
        addEnglishCanonical("tq", "thanks");
        addEnglishCanonical("welcom", "welcome");
        addEnglishCanonical("wlc", "welcome");
        addEnglishCanonical("congo", "congratulations");
        addEnglishCanonical("congrts", "congratulations");
        addEnglishCanonical("sry", "sorry");
        addEnglishCanonical("prblm", "problem");
        addEnglishCanonical("prob", "problem");
        addEnglishCanonical("ofc", "office");
        addEnglishCanonical("sch", "school");
        addEnglishCanonical("scl", "school");
        addEnglishCanonical("clg", "college");
        addEnglishCanonical("univ", "university");
        addEnglishCanonical("varsity", "university");
        addEnglishCanonical("nmbr", "number");
        addEnglishCanonical("num", "number");
        addEnglishCanonical("phn", "phone");
        addEnglishCanonical("fone", "phone");
        addEnglishCanonical("mob", "mobile");
        addEnglishCanonical("mbl", "mobile");
        addEnglishCanonical("bal", "balance");
        addEnglishCanonical("rechrg", "recharge");
        addEnglishCanonical("loc", "location");
        addEnglishCanonical("lctn", "location");
        addEnglishCanonical("doc", "document");
        addEnglishCanonical("addr", "address");
        addEnglishCanonical("addrs", "address");
        addEnglishCanonical("cmt", "comment");
        addEnglishCanonical("abt", "about");
        addEnglishCanonical("bcoz", "because");
        addEnglishCanonical("coz", "because");
        addEnglishCanonical("bcz", "because");
        addEnglishCanonical("recive", "receive");
        addEnglishCanonical("recieve", "receive");
        addEnglishCanonical("definately", "definitely");
        addEnglishCanonical("tommorow", "tomorrow");
        addEnglishCanonical("tmrw", "tomorrow");
        addEnglishCanonical("yday", "yesterday");
        addEnglishCanonical("bday", "birthday");
        addEnglishCanonical("brh", "brother");
        addEnglishCanonical("bro", "brother");
        addEnglishCanonical("sis", "sister");
        addEnglishCanonical("frnd", "friend");
        addEnglishCanonical("chk", "check");
        addEnglishCanonical("gud", "good");
        addEnglishCanonical("shud", "should");
        addEnglishCanonical("wud", "would");
        addEnglishCanonical("cud", "could");
        addEnglishCanonical("lyk", "like");
        addEnglishCanonical("dwnld", "download");
        addEnglishCanonical("updt", "update");
        addEnglishCanonical("instal", "install");
        addEnglishCanonical("instl", "install");
        addEnglishCanonical("cncl", "cancel");
        addEnglishCanonical("confrm", "confirm");
        addEnglishCanonical("seting", "settings");
        addEnglishCanonical("stng", "settings");
        addEnglishCanonical("pymt", "payment");
        addEnglishCanonical("pymnt", "payment");
        addEnglishCanonical("btr", "better");
        addEnglishCanonical("snd", "send");
        addEnglishCanonical("hlp", "help");
        addEnglishCanonical("wrk", "work");
        addEnglishCanonical("grp", "group");
        addEnglishCanonical("pg", "page");
        addEnglishCanonical("chnl", "channel");
        addEnglishCanonical("admn", "admin");
        addEnglishCanonical("tm", "time");
        addEnglishCanonical("dt", "date");
        addEnglishCanonical("intrnt", "internet");
        addEnglishCanonical("ntwrk", "network");
        addEnglishCanonical("scrn", "screen");
        addEnglishCanonical("btry", "battery");
        addEnglishCanonical("chrg", "charge");
        addEnglishCanonical("clr", "clear");
        addEnglishCanonical("vid", "video");
        addEnglishCanonical("pht", "photo");
        addEnglishCanonical("cmr", "camera");
        addEnglishCanonical("spkr", "speaker");
        addEnglishCanonical("sys", "system");
        addEnglishCanonical("srvc", "service");
        addEnglishCanonical("ap", "app");
        addEnglishCanonical("apks", "apk");
        initCuratedChatSuggestions();
    }

    private static void addCuratedChat(final String input, final String[] candidates) {
        CURATED_CHAT_SUGGESTIONS.put(input.toLowerCase(Locale.ROOT), candidates);
    }

    public static String[] getCuratedChatSuggestions(final String key) {
        if (key == null || key.isEmpty()) return null;
        final String lower = key.toLowerCase(Locale.ROOT);
        String[] direct = CURATED_CHAT_SUGGESTIONS.get(lower);
        if (direct != null) return direct;
        final String canonical = ENGLISH_CANONICAL_MAP.get(lower);
        if (canonical != null) {
            String[] canon = CURATED_CHAT_SUGGESTIONS.get(canonical);
            if (canon != null) return canon;
        }
        return null;
    }

    private static void initCuratedChatSuggestions() {
        addCuratedChat("ami", new String[]{"আমি", "I"});
        addCuratedChat("amar", new String[]{"আমার", "my"});
        addCuratedChat("tumi", new String[]{"তুমি", "তোমাকে", "you"});
        addCuratedChat("tomar", new String[]{"তোমার", "তোমাকে", "your"});
        addCuratedChat("apni", new String[]{"আপনি", "আপনাকে", "you"});
        addCuratedChat("amra", new String[]{"আমরা", "আমাদের", "we"});
        addCuratedChat("tara", new String[]{"তারা", "তাদের", "they"});
        addCuratedChat("ki", new String[]{"কি", "কী", "what"});
        addCuratedChat("keno", new String[]{"কেন", "কেননা", "why"});
        addCuratedChat("kobe", new String[]{"কবে", "কখন", "when"});
        addCuratedChat("kothay", new String[]{"কোথায়", "where"});
        addCuratedChat("kemon", new String[]{"কেমন", "কী রকম", "how"});
        addCuratedChat("hoy", new String[]{"হয়", "হবে", "hoy"});
        addCuratedChat("ache", new String[]{"আছে", "রয়েছে", "have"});
        addCuratedChat("nei", new String[]{"নেই", "নাই", "not available"});
        addCuratedChat("na", new String[]{"না", "নয়", "no"});
        addCuratedChat("hya", new String[]{"হ্যাঁ", "জি", "yes"});
        addCuratedChat("thik", new String[]{"ঠিক", "আচ্ছা", "okay"});
        addCuratedChat("accha", new String[]{"আচ্ছা", "ঠিক আছে", "okay"});
        addCuratedChat("valo", new String[]{"ভালো", "ভাল", "good"});
        addCuratedChat("bhalo", new String[]{"ভালো", "ভাল", "good"});
        addCuratedChat("kharap", new String[]{"খারাপ", "মন্দ", "bad"});
        addCuratedChat("onek", new String[]{"অনেক", "বেশি", "many"});
        addCuratedChat("ektu", new String[]{"একটু", "কিছুটা", "a little"});
        addCuratedChat("beshi", new String[]{"বেশি", "অনেক", "more"});
        addCuratedChat("kom", new String[]{"কম", "অল্প", "less"});
        addCuratedChat("akhon", new String[]{"এখন", "এই মুহূর্তে", "now"});
        addCuratedChat("aj", new String[]{"আজ", "আজকে", "today"});
        addCuratedChat("kal", new String[]{"কাল", "আগামীকাল", "tomorrow"});
        addCuratedChat("pore", new String[]{"পরে", "তারপর", "later"});
        addCuratedChat("age", new String[]{"আগে", "পূর্বে", "before"});
        addCuratedChat("shokal", new String[]{"সকাল", "সকালে", "morning"});
        addCuratedChat("dupur", new String[]{"দুপুর", "দুপুরে", "afternoon"});
        addCuratedChat("rat", new String[]{"রাত", "রাতে", "night"});
        addCuratedChat("somoy", new String[]{"সময়", "time"});
        addCuratedChat("bondhu", new String[]{"বন্ধু", "ফ্রেন্ড", "friend"});
        addCuratedChat("vai", new String[]{"ভাই", "ভাইয়া", "brother"});
        addCuratedChat("apu", new String[]{"আপু", "বোন", "sister"});
        addCuratedChat("ma", new String[]{"মা", "আম্মু", "mother"});
        addCuratedChat("baba", new String[]{"বাবা", "আব্বু", "father"});
        addCuratedChat("chele", new String[]{"ছেলে", "পুত্র", "boy"});
        addCuratedChat("meye", new String[]{"মেয়ে", "কন্যা", "girl"});
        addCuratedChat("nam", new String[]{"নাম", "নামটা", "name"});
        addCuratedChat("bari", new String[]{"বাড়ি", "বাসা", "home"});
        addCuratedChat("basha", new String[]{"বাসা", "বাড়ি", "home"});
        addCuratedChat("dhaka", new String[]{"ঢাকা", "ঢাকায়", "Dhaka"});
        addCuratedChat("school", new String[]{"স্কুল", "বিদ্যালয়", "school"});
        addCuratedChat("skul", new String[]{"স্কুল", "বিদ্যালয়", "school"});
        addCuratedChat("college", new String[]{"কলেজ", "মহাবিদ্যালয়", "college"});
        addCuratedChat("university", new String[]{"ইউনিভার্সিটি", "বিশ্ববিদ্যালয়", "university"});
        addCuratedChat("varsity", new String[]{"ভার্সিটি", "বিশ্ববিদ্যালয়", "university"});
        addCuratedChat("class", new String[]{"ক্লাস", "শ্রেণি", "class"});
        addCuratedChat("teacher", new String[]{"টিচার", "শিক্ষক", "teacher"});
        addCuratedChat("student", new String[]{"স্টুডেন্ট", "শিক্ষার্থী", "student"});
        addCuratedChat("porashona", new String[]{"পড়াশোনা", "লেখাপড়া", "study"});
        addCuratedChat("boi", new String[]{"বই", "বইটা", "book"});
        addCuratedChat("exam", new String[]{"এক্সাম", "পরীক্ষা", "exam"});
        addCuratedChat("porikkha", new String[]{"পরীক্ষা", "এক্সাম", "exam"});
        addCuratedChat("result", new String[]{"রেজাল্ট", "ফলাফল", "result"});
        addCuratedChat("assignment", new String[]{"অ্যাসাইনমেন্ট", "কাজ", "assignment"});
        addCuratedChat("homework", new String[]{"হোমওয়ার্ক", "বাড়ির কাজ", "homework"});
        addCuratedChat("office", new String[]{"অফিস", "কার্যালয়", "office"});
        addCuratedChat("kaj", new String[]{"কাজ", "কর্ম", "work"});
        addCuratedChat("meeting", new String[]{"মিটিং", "সভা", "meeting"});
        addCuratedChat("client", new String[]{"ক্লায়েন্ট", "গ্রাহক", "client"});
        addCuratedChat("customer", new String[]{"কাস্টমার", "ক্রেতা", "customer"});
        addCuratedChat("project", new String[]{"প্রজেক্ট", "প্রকল্প", "project"});
        addCuratedChat("task", new String[]{"টাস্ক", "কাজ", "task"});
        addCuratedChat("deadline", new String[]{"ডেডলাইন", "শেষ সময়", "deadline"});
        addCuratedChat("update", new String[]{"আপডেট", "হালনাগাদ", "update"});
        addCuratedChat("report", new String[]{"রিপোর্ট", "প্রতিবেদন", "report"});
        addCuratedChat("file", new String[]{"ফাইল", "নথি", "file"});
        addCuratedChat("document", new String[]{"ডকুমেন্ট", "নথি", "document"});
        addCuratedChat("account", new String[]{"অ্যাকাউন্ট", "হিসাব", "account"});
        addCuratedChat("bank", new String[]{"ব্যাংক", "ব্যাংকিং", "bank"});
        addCuratedChat("taka", new String[]{"টাকা", "পয়সা", "money"});
        addCuratedChat("payment", new String[]{"পেমেন্ট", "পরিশোধ", "payment"});
        addCuratedChat("bill", new String[]{"বিল", "হিসাব", "bill"});
        addCuratedChat("price", new String[]{"প্রাইস", "দাম", "price"});
        addCuratedChat("dam", new String[]{"দাম", "মূল্য", "price"});
        addCuratedChat("order", new String[]{"অর্ডার", "ফরমাশ", "order"});
        addCuratedChat("product", new String[]{"প্রোডাক্ট", "পণ্য", "product"});
        addCuratedChat("shop", new String[]{"শপ", "দোকান", "shop"});
        addCuratedChat("market", new String[]{"মার্কেট", "বাজার", "market"});
        addCuratedChat("food", new String[]{"ফুড", "খাবার", "food"});
        addCuratedChat("khabar", new String[]{"খাবার", "খানা", "food"});
        addCuratedChat("pani", new String[]{"পানি", "জল", "water"});
        addCuratedChat("cha", new String[]{"চা", "চায়ের", "tea"});
        addCuratedChat("vat", new String[]{"ভাত", "ভাতটা", "rice"});
        addCuratedChat("bhat", new String[]{"ভাত", "খাবার", "rice"});
        addCuratedChat("khaoa", new String[]{"খাওয়া", "খেতে", "eat"});
        addCuratedChat("ranna", new String[]{"রান্না", "রাঁধা", "cooking"});
        addCuratedChat("bazar", new String[]{"বাজার", "মার্কেট", "market"});
        addCuratedChat("restaurant", new String[]{"রেস্টুরেন্ট", "খাবারের দোকান", "restaurant"});
        addCuratedChat("phone", new String[]{"ফোন", "মোবাইল", "phone"});
        addCuratedChat("mobile", new String[]{"মোবাইল", "ফোন", "mobile"});
        addCuratedChat("computer", new String[]{"কম্পিউটার", "পিসি", "computer"});
        addCuratedChat("laptop", new String[]{"ল্যাপটপ", "কম্পিউটার", "laptop"});
        addCuratedChat("internet", new String[]{"ইন্টারনেট", "নেট", "internet"});
        addCuratedChat("net", new String[]{"নেট", "ইন্টারনেট", "internet"});
        addCuratedChat("wifi", new String[]{"ওয়াইফাই", "ইন্টারনেট", "wifi"});
        addCuratedChat("app", new String[]{"অ্যাপ", "অ্যাপ্লিকেশন", "app"});
        addCuratedChat("software", new String[]{"সফটওয়্যার", "প্রোগ্রাম", "software"});
        addCuratedChat("keyboard", new String[]{"কীবোর্ড", "টাইপ করার বোর্ড", "keyboard"});
        addCuratedChat("message", new String[]{"মেসেজ", "বার্তা", "message"});
        addCuratedChat("msg", new String[]{"মেসেজ", "বার্তা", "message"});
        addCuratedChat("chat", new String[]{"চ্যাট", "কথোপকথন", "chat"});
        addCuratedChat("call", new String[]{"কল", "ফোন করা", "call"});
        addCuratedChat("video", new String[]{"ভিডিও", "চলমান ছবি", "video"});
        addCuratedChat("photo", new String[]{"ফটো", "ছবি", "photo"});
        addCuratedChat("pic", new String[]{"ছবি", "পিক", "picture"});
        addCuratedChat("link", new String[]{"লিংক", "সংযোগ", "link"});
        addCuratedChat("password", new String[]{"পাসওয়ার্ড", "গোপন সংকেত", "password"});
        addCuratedChat("login", new String[]{"লগইন", "প্রবেশ", "login"});
        addCuratedChat("logout", new String[]{"লগআউট", "বের হওয়া", "logout"});
        addCuratedChat("email", new String[]{"ইমেইল", "বার্তা", "email"});
        addCuratedChat("facebook", new String[]{"ফেসবুক", "সোশ্যাল মিডিয়া", "Facebook"});
        addCuratedChat("whatsapp", new String[]{"হোয়াটসঅ্যাপ", "মেসেজিং অ্যাপ", "WhatsApp"});
        addCuratedChat("youtube", new String[]{"ইউটিউব", "ভিডিও প্ল্যাটফর্ম", "YouTube"});
        addCuratedChat("download", new String[]{"ডাউনলোড", "নামিয়ে নেওয়া", "download"});
        addCuratedChat("upload", new String[]{"আপলোড", "জমা দেওয়া", "upload"});
        addCuratedChat("save", new String[]{"সেভ", "সংরক্ষণ", "save"});
        addCuratedChat("share", new String[]{"শেয়ার", "ভাগ করে নেওয়া", "share"});
        addCuratedChat("send", new String[]{"সেন্ড", "পাঠানো", "send"});
        addCuratedChat("reply", new String[]{"রিপ্লাই", "উত্তর", "reply"});
        addCuratedChat("seen", new String[]{"সিন", "দেখেছে", "seen"});
        addCuratedChat("online", new String[]{"অনলাইন", "ইন্টারনেটে সক্রিয়", "online"});
        addCuratedChat("offline", new String[]{"অফলাইন", "ইন্টারনেট ছাড়া", "offline"});
        addCuratedChat("sorry", new String[]{"সরি", "দুঃখিত", "sorry"});
        addCuratedChat("thanks", new String[]{"থ্যাংকস", "ধন্যবাদ", "thanks"});
        addCuratedChat("thankyou", new String[]{"থ্যাংক ইউ", "ধন্যবাদ", "thank you"});
        addCuratedChat("please", new String[]{"প্লিজ", "দয়া করে", "please"});
        addCuratedChat("welcome", new String[]{"ওয়েলকাম", "স্বাগতম", "welcome"});
        addCuratedChat("love", new String[]{"লাভ", "ভালোবাসা", "love"});
        addCuratedChat("miss", new String[]{"মিস", "মনে পড়া", "miss"});
        addCuratedChat("happy", new String[]{"হ্যাপি", "খুশি", "happy"});
        addCuratedChat("sad", new String[]{"স্যাড", "মন খারাপ", "sad"});
        addCuratedChat("problem", new String[]{"প্রবলেম", "সমস্যা", "problem"});
        addCuratedChat("issue", new String[]{"ইস্যু", "সমস্যা", "issue"});
        addCuratedChat("help", new String[]{"হেল্প", "সাহায্য", "help"});
        addCuratedChat("urgent", new String[]{"আর্জেন্ট", "জরুরি", "urgent"});
        addCuratedChat("important", new String[]{"ইম্পর্ট্যান্ট", "গুরুত্বপূর্ণ", "important"});
        addCuratedChat("possible", new String[]{"পসিবল", "সম্ভব", "possible"});
        addCuratedChat("maybe", new String[]{"মেবি", "হয়তো", "maybe"});
        addCuratedChat("sure", new String[]{"শিওর", "নিশ্চিত", "sure"});
        addCuratedChat("confirm", new String[]{"কনফার্ম", "নিশ্চিত করা", "confirm"});
        addCuratedChat("check", new String[]{"চেক", "যাচাই", "check"});
        addCuratedChat("pleasecheck", new String[]{"প্লিজ চেক", "দয়া করে যাচাই করুন", "please check"});
        addCuratedChat("complete", new String[]{"কমপ্লিট", "সম্পন্ন", "complete"});
        addCuratedChat("start", new String[]{"স্টার্ট", "শুরু", "start"});
        addCuratedChat("finish", new String[]{"ফিনিশ", "শেষ", "finish"});
        addCuratedChat("ready", new String[]{"রেডি", "প্রস্তুত", "ready"});
        addCuratedChat("wait", new String[]{"ওয়েট", "অপেক্ষা", "wait"});
        addCuratedChat("aschi", new String[]{"আসছি", "আমি আসছি", "coming"});
        addCuratedChat("jacchi", new String[]{"যাচ্ছি", "আমি যাচ্ছি", "going"});
        addCuratedChat("korbo", new String[]{"করব", "আমি করব", "will do"});
        addCuratedChat("korchi", new String[]{"করছি", "আমি করছি", "doing"});
        addCuratedChat("hobe", new String[]{"হবে", "সম্ভব হবে", "will happen"});
        addCuratedChat("lagbe", new String[]{"লাগবে", "প্রয়োজন হবে", "will need"});
        addCuratedChat("chai", new String[]{"চাই", "ইচ্ছা করি", "want"});
        addCuratedChat("parbo", new String[]{"পারব", "সক্ষম হব", "can"});
        addCuratedChat("bujhlam", new String[]{"বুঝলাম", "বুঝেছি", "understood"});
        addCuratedChat("bujhi", new String[]{"বুঝি", "আমি বুঝি", "understand"});
        addCuratedChat("bolen", new String[]{"বলেন", "বলুন", "please say"});
        addCuratedChat("bolo", new String[]{"বলো", "বল", "say"});
        addCuratedChat("shunen", new String[]{"শুনেন", "শুনুন", "please listen"});
        addCuratedChat("janina", new String[]{"জানিনা", "আমি জানি না", "I don't know"});
        addCuratedChat("jani", new String[]{"জানি", "আমি জানি", "know"});
        addCuratedChat("mone", new String[]{"মনে", "মনে হয়", "mind"});
        addCuratedChat("hoyto", new String[]{"হয়তো", "সম্ভবত", "maybe"});
        addCuratedChat("oboshshoi", new String[]{"অবশ্যই", "নিশ্চয়ই", "of course"});
        addCuratedChat("kintu", new String[]{"কিন্তু", "তবে", "but"});
        addCuratedChat("karon", new String[]{"কারণ", "কেননা", "because"});
        addCuratedChat("tai", new String[]{"তাই", "সেজন্য", "so"});
        addCuratedChat("tahole", new String[]{"তাহলে", "সেক্ষেত্রে", "then"});
        addCuratedChat("jodi", new String[]{"যদি", "যদি কখনো", "if"});
        addCuratedChat("sathe", new String[]{"সাথে", "একসাথে", "with"});
        addCuratedChat("jonno", new String[]{"জন্য", "কারণে", "for"});
        addCuratedChat("shob", new String[]{"সব", "সকল", "all"});
        addCuratedChat("shobai", new String[]{"সবাই", "সকলেই", "everyone"});
        addCuratedChat("ekta", new String[]{"একটা", "একটি", "one"});
        addCuratedChat("duita", new String[]{"দুইটা", "দুটি", "two"});
        addCuratedChat("proyojon", new String[]{"প্রয়োজন", "দরকার", "need"});
        addCuratedChat("dorkar", new String[]{"দরকার", "প্রয়োজন", "need"});
        addCuratedChat("khub", new String[]{"খুব", "অনেক", "very"});
        addCuratedChat("sundor", new String[]{"সুন্দর", "চমৎকার", "beautiful"});
        addCuratedChat("moja", new String[]{"মজা", "আনন্দ", "fun"});
        addCuratedChat("allah", new String[]{"আল্লাহ", "খোদা", "Allah"});
        addCuratedChat("inshallah", new String[]{"ইনশাআল্লাহ", "আল্লাহ চাইলে", "God willing"});
        addCuratedChat("subhanallah", new String[]{"সুবহানাল্লাহ", "মাশাআল্লাহ", "Subhanallah"});
    }

    private BanglaSuggestionEngine() {}

    /**
     * Avro case normalization:
     * In official Avro phonetic input, uppercase is only meaningful for distinct phonemes:
     * O, U, D, G, J, N, R, S, T, Y, Z.
     * Accidental capitalization from Android auto-caps (e.g. A in Ami, K in Kemon, E in Eita, H in Hobe)
     * is normalized to lowercase.
     */
    public static String normalizeCase(final String in) {
        if (in == null || in.isEmpty()) return in;
        final StringBuilder sb = new StringBuilder(in.length());
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (c == 'O' || c == 'U' || c == 'D' || c == 'G' || c == 'J' ||
                c == 'N' || c == 'R' || c == 'S' || c == 'T' || c == 'Y' || c == 'Z' ||
                c == 'o' || c == 'u' || c == 'd' || c == 'g' || c == 'j' ||
                c == 'n' || c == 'r' || c == 's' || c == 't' || c == 'y' || c == 'z') {
                sb.append(c);
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    /**
     * Borno's RemoveConsecutiveKarSigns:
     * When two kar signs occur consecutively (e.g. invalid typing like কা + া),
     * replaces the second kar sign with its independent vowel to prevent broken dotted circles.
     */
    public static String removeConsecutiveKarSigns(final String str) {
        if (str == null || str.length() < 2) return str;
        final StringBuilder sb = new StringBuilder(str);
        for (int i = 1; i < sb.length(); i++) {
            if (isKarSign(sb.charAt(i - 1)) && isKarSign(sb.charAt(i))) {
                sb.setCharAt(i, getCorrespondingVowel(sb.charAt(i)));
            }
        }
        return sb.toString();
    }

    private static boolean isKarSign(char c) {
        return (c >= 0x09BE && c <= 0x09C4) || (c >= 0x09C7 && c <= 0x09C8) || (c >= 0x09CB && c <= 0x09CC) || (c == 0x09D7);
    }

    private static char getCorrespondingVowel(char sign) {
        switch (sign) {
            case 0x09BE: return 0x0986; // া -> আ
            case 0x09BF: return 0x0987; // ি -> ই
            case 0x09C0: return 0x0988; // ী -> ঈ
            case 0x09C1: return 0x0989; // ু -> উ
            case 0x09C2: return 0x098A; // ূ -> ঊ
            case 0x09C3: return 0x098B; // ৃ -> ঋ
            case 0x09C7: return 0x098F; // ে -> এ
            case 0x09C8: return 0x0990; // ৈ -> ঐ
            case 0x09CB: return 0x0993; // ো -> ও
            case 0x09CC: return 0x0994; // ৌ -> ঔ
            case 0x09D7: return 0x0994;
            default: return sign;
        }
    }

    public static boolean containsDigit(final String s) {
        if (s == null) return false;
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                return true;
            }
        }
        return false;
    }

    public static boolean isDigitsOnly(final String s) {
        if (s == null || s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    public static boolean isDigitsOrNumber(final String s) {
        if (s == null || s.isEmpty()) return false;
        boolean hasDigit = false;
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                hasDigit = true;
            } else if (c != '.' && c != ',' && c != '%' && c != '-') {
                return false;
            }
        }
        return hasDigit;
    }

    public static String toBengaliNumerals(final String s) {
        if (s == null) return null;
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                sb.append((char) (c - '0' + 0x09E6));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static boolean isTrailingPunctuation(final char c) {
        return c == '.' || c == ',' || c == '?' || c == '!' || c == ';' || c == ':'
                || c == '\u0964' /* । */ || c == '\u0965' /* ॥ */
                || c == '\'' || c == '"' || c == '-';
    }

    private static final class PhoneticSite {
        final int start;
        final int end;
        final String[] replacements;

        PhoneticSite(final int start, final int end, final String[] replacements) {
            this.start = start;
            this.end = end;
            this.replacements = replacements;
        }
    }

    /**
     * Generates candidate variants where dental/retroflex pairs (t <-> T, th <-> Th,
     * d <-> D, dh <-> Dh, s <-> sh <-> S, n <-> N, r <-> R, v <-> bh) are permuted for
     * initial and medial consonants.
     */
    public static List<String> generatePhoneticVariants(final String raw) {
        final List<String> variants = new ArrayList<>();
        if (raw == null || raw.length() < 2) {
            return variants;
        }

        final List<PhoneticSite> sites = new ArrayList<>();
        int i = 0;
        final int len = raw.length();
        while (i < len) {
            final char c = raw.charAt(i);
            final char next = (i + 1 < len) ? raw.charAt(i + 1) : 0;

            // 1. Two-character clusters first
            if ((c == 't' || c == 'T') && (next == 'h' || next == 'H')) {
                sites.add(new PhoneticSite(i, i + 2, (c == 't') ? new String[]{"Th"} : new String[]{"th"}));
                i += 2;
                continue;
            }
            if ((c == 'd' || c == 'D') && (next == 'h' || next == 'H')) {
                sites.add(new PhoneticSite(i, i + 2, (c == 'd') ? new String[]{"Dh"} : new String[]{"dh"}));
                i += 2;
                continue;
            }
            if ((c == 'c' || c == 'C') && (next == 'h' || next == 'H')) {
                sites.add(new PhoneticSite(i, i + 2, new String[]{"c"}));
                i += 2;
                continue;
            }
            if ((c == 's' || c == 'S') && (next == 'h' || next == 'H')) {
                final String[] reps = (c == 's')
                        ? new String[]{"s", "Sh", "S"}
                        : new String[]{"s", "sh", "S"};
                sites.add(new PhoneticSite(i, i + 2, reps));
                i += 2;
                continue;
            }
            if ((c == 'b' || c == 'B') && (next == 'h' || next == 'H')) {
                sites.add(new PhoneticSite(i, i + 2, new String[]{"v"}));
                i += 2;
                continue;
            }

            // 2. Single-character consonants
            if (c == 't') {
                sites.add(new PhoneticSite(i, i + 1, new String[]{"T", "TH"}));
            } else if (c == 'T') {
                sites.add(new PhoneticSite(i, i + 1, new String[]{"t"}));
            } else if (c == 'd') {
                sites.add(new PhoneticSite(i, i + 1, new String[]{"D"}));
            } else if (c == 'D') {
                sites.add(new PhoneticSite(i, i + 1, new String[]{"d"}));
            } else if (c == 'c' || c == 'C') {
                sites.add(new PhoneticSite(i, i + 1, new String[]{"ch"}));
            } else if (c == 's') {
                sites.add(new PhoneticSite(i, i + 1, new String[]{"sh", "Sh", "S"}));
            } else if (c == 'S') {
                sites.add(new PhoneticSite(i, i + 1, new String[]{"s", "sh", "Sh"}));
            } else if (c == 'n') {
                sites.add(new PhoneticSite(i, i + 1, new String[]{"N"}));
            } else if (c == 'N') {
                sites.add(new PhoneticSite(i, i + 1, new String[]{"n"}));
            } else if (c == 'r') {
                sites.add(new PhoneticSite(i, i + 1, new String[]{"R"}));
            } else if (c == 'R') {
                sites.add(new PhoneticSite(i, i + 1, new String[]{"r"}));
            } else if (c == 'v' || c == 'V') {
                sites.add(new PhoneticSite(i, i + 1, new String[]{"bh"}));
            }
            i++;
        }

        if (sites.isEmpty()) {
            return variants;
        }

        final Set<String> uniqueVariants = new LinkedHashSet<>();

        // Level 1: Single-site substitutions
        for (final PhoneticSite site : sites) {
            for (final String rep : site.replacements) {
                final String variant = raw.substring(0, site.start) + rep + raw.substring(site.end);
                if (!variant.equals(raw)) {
                    uniqueVariants.add(variant);
                }
            }
        }

        // Level 2: Pairwise substitutions
        if (sites.size() >= 2) {
            for (int j = 0; j < sites.size() && uniqueVariants.size() < 32; j++) {
                final PhoneticSite s1 = sites.get(j);
                for (int k = j + 1; k < sites.size() && uniqueVariants.size() < 32; k++) {
                    final PhoneticSite s2 = sites.get(k);
                    for (final String r1 : s1.replacements) {
                        for (final String r2 : s2.replacements) {
                            final String variant = raw.substring(0, s1.start)
                                    + r1
                                    + raw.substring(s1.end, s2.start)
                                    + r2
                                    + raw.substring(s2.end);
                            if (!variant.equals(raw)) {
                                uniqueVariants.add(variant);
                            }
                        }
                    }
                }
            }
        }

        // Level 3: Triple substitutions (for words with >= 3 ambiguous sites)
        if (sites.size() >= 3 && uniqueVariants.size() < 32) {
            for (int j = 0; j < sites.size() && uniqueVariants.size() < 32; j++) {
                final PhoneticSite s1 = sites.get(j);
                for (int k = j + 1; k < sites.size() && uniqueVariants.size() < 32; k++) {
                    final PhoneticSite s2 = sites.get(k);
                    for (int l = k + 1; l < sites.size() && uniqueVariants.size() < 32; l++) {
                        final PhoneticSite s3 = sites.get(l);
                        for (final String r1 : s1.replacements) {
                            for (final String r2 : s2.replacements) {
                                for (final String r3 : s3.replacements) {
                                    final String variant = raw.substring(0, s1.start)
                                            + r1
                                            + raw.substring(s1.end, s2.start)
                                            + r2
                                            + raw.substring(s2.end, s3.start)
                                            + r3
                                            + raw.substring(s3.end);
                                    if (!variant.equals(raw)) {
                                        uniqueVariants.add(variant);
                                    }
                                    if (uniqueVariants.size() >= 32) break;
                                }
                                if (uniqueVariants.size() >= 32) break;
                            }
                            if (uniqueVariants.size() >= 32) break;
                        }
                    }
                }
            }
        }

        variants.addAll(uniqueVariants);
        return variants;
    }

    private static final Map<String, String[]> COMMON_NEXT_WORDS = new HashMap<>();
    static {
        COMMON_NEXT_WORDS.put("আমি", new String[]{"তোমাকে", "ভালো", "যাব"});
        COMMON_NEXT_WORDS.put("তুমি", new String[]{"কেমন", "কোথায়", "কী"});
        COMMON_NEXT_WORDS.put("আপনি", new String[]{"কেমন", "ভালো", "কোথায়"});
        COMMON_NEXT_WORDS.put("আমরা", new String[]{"সবাই", "যাব", "ভালো"});
        COMMON_NEXT_WORDS.put("আমার", new String[]{"মনে", "কাছে", "ভাই"});
        COMMON_NEXT_WORDS.put("তোমার", new String[]{"নাম", "বাসা", "খবর"});
        COMMON_NEXT_WORDS.put("আপনার", new String[]{"নাম", "কাছে", "দোয়া"});
        COMMON_NEXT_WORDS.put("অনেক", new String[]{"ধন্যবাদ", "সুন্দর", "ভালো"});
        COMMON_NEXT_WORDS.put("খুব", new String[]{"ভালো", "সুন্দর", "কষ্ট"});
        COMMON_NEXT_WORDS.put("শুভ", new String[]{"সকাল", "রাত্রি", "কামনা"});
        COMMON_NEXT_WORDS.put("কি", new String[]{"খবর", "করছ", "অবস্থা"});
        COMMON_NEXT_WORDS.put("কী", new String[]{"খবর", "করছ", "অবস্থা"});
        COMMON_NEXT_WORDS.put("ভালো", new String[]{"আছো", "থাকবেন", "বাসি"});
        COMMON_NEXT_WORDS.put("কেমন", new String[]{"আছো", "আছেন", "হবে"});
        COMMON_NEXT_WORDS.put("ধন্যবাদ", new String[]{"ভাই", "আপনাকে", "তোমাকে"});
        COMMON_NEXT_WORDS.put("সে", new String[]{"কী", "কোথায়", "যাবে"});
        COMMON_NEXT_WORDS.put("এটা", new String[]{"খুব", "অনেক", "ভালো"});
        COMMON_NEXT_WORDS.put("হ্যাঁ", new String[]{"ঠিক", "বলুন", "হবে"});
        COMMON_NEXT_WORDS.put("ঠিক", new String[]{"আছে", "হয়েছে", "বলছেন"});
        COMMON_NEXT_WORDS.put("কবে", new String[]{"আসবেন", "হবে", "যাবেন"});
        COMMON_NEXT_WORDS.put("কোথায়", new String[]{"আছো", "আছেন", "যাবেন"});
    }

    public static List<String> getNextWordPredictions(final String prevWord, final DictionaryFacilitator dictFacilitator) {
        final LinkedHashSet<String> predictions = new LinkedHashSet<>();
        if (prevWord == null || prevWord.isEmpty()) {
            return new ArrayList<>();
        }
        String cleanPrev = prevWord.trim();
        while (cleanPrev.length() > 0 && isTrailingPunctuation(cleanPrev.charAt(cleanPrev.length() - 1))) {
            cleanPrev = cleanPrev.substring(0, cleanPrev.length() - 1);
        }

        // 1. Dynamic learned bigram predictions (highest priority)
        final List<String> learned = getLearnedBigramPredictions(cleanPrev);
        if (learned != null) {
            predictions.addAll(learned);
        }

        // 2. Curated conversational next-word associations
        final String[] curated = COMMON_NEXT_WORDS.get(cleanPrev);
        if (curated != null) {
            for (final String word : curated) {
                if (word != null && !word.isEmpty()) {
                    predictions.add(word);
                }
            }
        }

        final List<String> result = new ArrayList<>(predictions);
        while (result.size() > 5) {
            result.remove(result.size() - 1);
        }
        return result;
    }

    /**
     * Backward-compatible candidate generation without DictionaryFacilitator.
     */
    public static List<String> getCandidates(final String raw, final RidmikParser parser) {
        return getCandidates(raw, parser, null);
    }

    /**
     * Generates rich candidates using Banglish alias resolution, phonetic rules,
     * curated chat suggestions, and authentic dictionary validation.
     */
    public static List<String> getCandidates(final String raw, final RidmikParser parser,
                                             final DictionaryFacilitator dictFacilitator) {
        final LinkedHashSet<String> set = new LinkedHashSet<>();
        if (raw == null || raw.isEmpty()) {
            return new ArrayList<>();
        }

        final String lower = raw.toLowerCase(Locale.ROOT);
        final String canonical = ENGLISH_CANONICAL_MAP.get(lower);
        final boolean isShorthand = SHORTHAND_ABBREVIATIONS.contains(lower)
                || (canonical != null && SHORTHAND_ABBREVIATIONS.contains(canonical));

        final String[] curatedChat = getCuratedChatSuggestions(lower);
        final String[] primaryAliases = BANGLISH_ALIASES.get(lower);

        // 0. Curated Chat Suggestions take slot priority (e.g. account -> ["অ্যাকাউন্ট", "হিসাব", "account"])
        if (curatedChat != null) {
            for (String alias : curatedChat) {
                if (alias != null && !alias.isEmpty()) {
                    set.add(removeConsecutiveKarSigns(alias));
                }
            }
        }

        // 1. Explicit Banglish Aliases take slot 0 priority (Ridmik & Gboard standard)
        // Whenever a curated Banglish alias exists (e.g. jabo -> যাব, thik -> ঠিক, chai -> চাই, eita -> এইটা, koro -> করো),
        // the alias MUST take index 0 so Spacebar cleanly commits the expected conversational word.
        if (primaryAliases != null) {
            for (String alias : primaryAliases) {
                if (alias != null && !alias.isEmpty()) {
                    set.add(removeConsecutiveKarSigns(alias));
                }
            }
        }
        if (canonical != null) {
            final String[] canonicalAliases = BANGLISH_ALIASES.get(canonical);
            if (canonicalAliases != null) {
                for (String alias : canonicalAliases) {
                    if (alias != null && !alias.isEmpty()) {
                        set.add(removeConsecutiveKarSigns(alias));
                    }
                }
            }
        }

        // 2. Bengali Numeral Quick-Pick in Avro Mode
        final String bnNumerals = containsDigit(raw) ? toBengaliNumerals(raw) : null;
        if (bnNumerals != null && !bnNumerals.isEmpty()) {
            set.add(bnNumerals);
        }

        // 3. Primary transliteration with Avro case normalization
        final String normalizedRaw = normalizeCase(raw);
        final String primary = removeConsecutiveKarSigns(parser.toBangla(normalizedRaw));
        if (primary != null && !primary.isEmpty()) {
            set.add(primary);
        }
        if (!normalizedRaw.equals(raw)) {
            final String rawPrimary = removeConsecutiveKarSigns(parser.toBangla(raw));
            if (rawPrimary != null && !rawPrimary.isEmpty()) {
                set.add(rawPrimary);
            }
        }
        // Smart Sentence-Start Fix: if raw begins with an uppercase letter, generate lower-first transliteration
        if (raw.length() > 0 && Character.isUpperCase(raw.charAt(0))) {
            final String lowerFirst = Character.toLowerCase(raw.charAt(0)) + (raw.length() > 1 ? raw.substring(1) : "");
            final String lowerFirstPrimary = removeConsecutiveKarSigns(parser.toBangla(normalizeCase(lowerFirst)));
            if (lowerFirstPrimary != null && !lowerFirstPrimary.isEmpty()) {
                set.add(lowerFirstPrimary);
            }
        }
        if (containsDigit(raw) && primary != null) {
            final String bnNumeralsTransliterated = toBengaliNumerals(primary);
            if (!bnNumeralsTransliterated.equals(primary) && !bnNumeralsTransliterated.equals(bnNumerals)) {
                set.add(bnNumeralsTransliterated);
            }
        }

        // 3. Direct phonological variations from raw
        if (raw.contains("o")) {
            final String altO = removeConsecutiveKarSigns(parser.toBangla(raw.replace('o', 'O')));
            if (altO != null && !altO.isEmpty()) set.add(altO);
        } else if (raw.contains("O")) {
            final String alto = removeConsecutiveKarSigns(parser.toBangla(raw.replace('O', 'o')));
            if (alto != null && !alto.isEmpty()) set.add(alto);
        }

        if (raw.contains("sh")) {
            final String sWord = raw.replace("sh", "s");
            final String alt = removeConsecutiveKarSigns(parser.toBangla(sWord));
            if (alt != null && !alt.isEmpty()) set.add(alt);
            if (sWord.contains("o")) {
                final String altSO = removeConsecutiveKarSigns(parser.toBangla(sWord.replace('o', 'O')));
                if (altSO != null && !altSO.isEmpty()) set.add(altSO);
            }
        } else if (raw.contains("s")) {
            final String shWord = raw.replace("s", "sh");
            final String alt = removeConsecutiveKarSigns(parser.toBangla(shWord));
            if (alt != null && !alt.isEmpty()) set.add(alt);
            if (shWord.contains("o")) {
                final String altShO = removeConsecutiveKarSigns(parser.toBangla(shWord.replace('o', 'O')));
                if (altShO != null && !altShO.isEmpty()) set.add(altShO);
            }
        }

        if (raw.contains("t")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('t', 'T')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        } else if (raw.contains("T")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('T', 't')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        }

        if (raw.contains("d")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('d', 'D')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        } else if (raw.contains("D")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('D', 'd')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        }

        if (raw.contains("v")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace("v", "bh")));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        } else if (raw.contains("bh")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace("bh", "v")));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        }

        if (raw.contains("r")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('r', 'R')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        } else if (raw.contains("R")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('R', 'r')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        }

        // 4. Systematic Fuzzy Phonetic Permutations for Soft Dental/Retroflex & Sibilants
        // (t/T, th/Th, d/D, dh/Dh, c/ch, s/sh/S, n/N, r/R, v/bh)
        final List<String> fuzzyVariants = generatePhoneticVariants(raw);
        for (final String variant : fuzzyVariants) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(variant));
            if (alt != null && !alt.isEmpty()) {
                set.add(alt);
            }
            if (variant.contains("o")) {
                final String altO = removeConsecutiveKarSigns(parser.toBangla(variant.replace('o', 'O')));
                if (altO != null && !altO.isEmpty()) set.add(altO);
            }
            if (variant.endsWith("o")) {
                final String altO = removeConsecutiveKarSigns(parser.toBangla(variant.substring(0, variant.length() - 1) + "O"));
                if (altO != null && !altO.isEmpty()) set.add(altO);
            }
        }

        // 5. Trailing 'o' / 'O' variations (verbs & common adjectives, e.g. korbo -> করব, করবো)
        if (raw.endsWith("o")) {
            final String altO = removeConsecutiveKarSigns(parser.toBangla(raw.substring(0, raw.length() - 1) + "O"));
            if (altO != null && !altO.isEmpty()) set.add(altO);
            if (raw.length() > 2) {
                final String stem = removeConsecutiveKarSigns(parser.toBangla(raw.substring(0, raw.length() - 1)));
                if (stem != null && !stem.isEmpty()) set.add(stem);
            }
        } else if (raw.endsWith("O")) {
            final String alto = removeConsecutiveKarSigns(parser.toBangla(raw.substring(0, raw.length() - 1) + "o"));
            if (alto != null && !alto.isEmpty()) set.add(alto);
        } else if (raw.length() > 1 && !isVowel(raw.charAt(raw.length() - 1))) {
            final String altO = removeConsecutiveKarSigns(parser.toBangla(raw + "O"));
            if (altO != null && !altO.isEmpty()) set.add(altO);
            final String alto = removeConsecutiveKarSigns(parser.toBangla(raw + "o"));
            if (alto != null && !alto.isEmpty()) set.add(alto);
        }


        // 6. Additional vowel distinctions (i/I, u/U, z/j)
        if (raw.contains("i")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('i', 'I')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        } else if (raw.contains("I")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('I', 'i')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        }

        if (raw.contains("u")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('u', 'U')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        } else if (raw.contains("U")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('U', 'u')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        }

        if (raw.contains("z")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('z', 'j')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        } else if (raw.contains("j")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('j', 'z')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        }

        // 7. Enrich with Zero-GC flat CSR Trie phonetic completions
        try {
            final List<String> trieMatches = BanglaPhoneticMatcher.Companion.getINSTANCE().findMatches(primary);
            if (trieMatches != null) {
                for (String tm : trieMatches) {
                    if (tm != null && !tm.isEmpty()) {
                        set.add(removeConsecutiveKarSigns(tm));
                    }
                }
            }
        } catch (final Throwable ignored) {}

        // 8. Rank candidates with DictionaryFacilitator (main_bn.dict)
        final ArrayList<String> result = new ArrayList<>();
        String topCuratedAlias = null;
        if (curatedChat != null && curatedChat.length > 0) {
            for (String c : curatedChat) {
                if (c != null && !c.isEmpty()) {
                    final String clean = removeConsecutiveKarSigns(c);
                    if (!result.contains(clean)) {
                        result.add(clean);
                    }
                }
            }
        } else if (isShorthand && primaryAliases != null && primaryAliases.length > 0) {
            topCuratedAlias = removeConsecutiveKarSigns(primaryAliases[0]);
        }
        if (dictFacilitator != null) {
            if (topCuratedAlias != null && set.contains(topCuratedAlias) && !result.contains(topCuratedAlias)) {
                result.add(topCuratedAlias);
            }
            final List<String> validWords = new ArrayList<>();
            final List<String> remainingWords = new ArrayList<>();
            for (final String cand : set) {
                if (result.contains(cand)) continue;
                if (dictFacilitator.isValidSpellingWord(cand)) {
                    validWords.add(cand);
                } else {
                    remainingWords.add(cand);
                }
            }
            // Sort valid words by frequency descending in main_bn.dict
            validWords.sort((a, b) -> Integer.compare(dictFacilitator.getFrequency(b), dictFacilitator.getFrequency(a)));

            result.addAll(validWords);
            result.addAll(remainingWords);
        } else {
            for (final String cand : set) {
                if (!result.contains(cand)) {
                    result.add(cand);
                }
            }
        }

        // Bengali Numeral Priority for pure digits and formatted numbers (e.g. 500 -> 500 at index 0, ৫০০ at index 1)
        final boolean isNumeral = bnNumerals != null && (isDigitsOnly(raw) || isDigitsOrNumber(raw));
        if (isNumeral) {
            result.remove(raw);
            result.remove(bnNumerals);
            result.add(0, bnNumerals);
            result.add(0, raw);
        }

        // 9. Inject learned user transliterations for raw.toLowerCase()
        // Top learned transliterations take index 0 priority (frequently chosen spellings/words)
        // Guard against displacing numbers: learned words must NEVER displace numeral priority
        final List<String> learnedCandidates = getLearnedCandidates(lower);
        if (learnedCandidates != null && !learnedCandidates.isEmpty()) {
            final int countToInject = Math.min(learnedCandidates.size(), 2);
            for (int i = countToInject - 1; i >= 0; i--) {
                final String learned = removeConsecutiveKarSigns(learnedCandidates.get(i));
                if (learned != null && !learned.isEmpty() && !learned.equals(bnNumerals)) {
                    result.remove(learned);
                    if (isNumeral) {
                        int insertIndex = Math.min(2, result.size());
                        result.add(insertIndex, learned);
                    } else {
                        result.add(0, learned);
                    }
                }
            }
        }

        // Keep maximum 5 candidates
        while (result.size() > 5) {
            result.remove(result.size() - 1);
        }

        return result;
    }

    private static boolean isVowel(char c) {
        c = Character.toLowerCase(c);
        return c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u';
    }

    /**
     * Retrieves bilingual English candidates for Avro phonetic typing.
     * Generates canonical spell-corrected word (e.g. accout -> account) and exact typed text.
     */
    public static List<String> getEnglishCandidates(final String raw) {
        final List<String> result = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) {
            return result;
        }
        final String trimmed = raw.trim();
        final String lower = trimmed.toLowerCase(Locale.ROOT);

        final String canonical = ENGLISH_CANONICAL_MAP.get(lower);
        if (canonical != null && !canonical.equalsIgnoreCase(trimmed)) {
            result.add(applyCasing(trimmed, canonical));
        }

        result.add(trimmed);

        return result;
    }

    /**
     * Applies the casing pattern of template string to target string.
     */
    public static String applyCasing(final String template, final String target) {
        if (template == null || target == null || template.isEmpty() || target.isEmpty()) {
            return target;
        }
        boolean allUpper = true;
        for (int i = 0; i < template.length(); i++) {
            if (Character.isLowerCase(template.charAt(i))) {
                allUpper = false;
                break;
            }
        }
        if (allUpper && template.length() > 1) {
            return target.toUpperCase(Locale.ROOT);
        }
        if (Character.isUpperCase(template.charAt(0))) {
            if (target.length() == 1) {
                return target.toUpperCase(Locale.ROOT);
            }
            return Character.toUpperCase(target.charAt(0)) + target.substring(1).toLowerCase(Locale.ROOT);
        }
        return target.toLowerCase(Locale.ROOT);
    }
}
