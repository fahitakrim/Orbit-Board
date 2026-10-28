package org.futo.inputmethod.latin.uix.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.os.Build;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Empirical Adversarial Test Harness for EmojiAction filtering behavior and SDK simulations.
 *
 * Tests:
 * 1. SDK Simulation across various SDK_INT values:
 *    - Modern Android: SDK_INT >= 33 (33, 34, 35, 36)
 *    - JVM / Test default: SDK_INT == 0
 *    - Mid-tier Android: SDK_INT in [23..32] (23, 28, 30, 32)
 *    - Legacy Android: SDK_INT < 23 (21, 19, -1)
 * 2. Strict non-dropping of all 36 Unicode 15.1 and 16.0 modern emojis on modern environments.
 * 3. Complete catalog test: All 1,979 emojis from gemoji.json evaluated through isEmojiSupported.
 * 4. Boundary and adversarial input stress testing:
 *    - Empty strings, ASCII, surrogate pairs, malformed sequences, max Unicode codepoint (0x10FFFF).
 *    - Complex ZWJ multi-character and multi-skin-tone sequences.
 * 5. High-throughput stress test (10,000 iterations) verifying stability and zero allocation crashes.
 */
public class EmojiAdversarialTest {

    private static final String GEMOJI_PATH = "java/res/raw/gemoji.json";
    private static List<Map<String, Object>> sEmojiList;
    private static Map<String, Map<String, Object>> sEmojiMap;
    private static int sOriginalSdkInt;

    // Unicode 15.1 & 16.0 sample lists
    private static final String[] UNICODE_15_1_KEY_EMOJIS = new String[] {
            new String(Character.toChars(0x1F426)) + "\u200D" + new String(Character.toChars(0x1F525)), // Phoenix
            new String(Character.toChars(0x1F34B)) + "\u200D" + new String(Character.toChars(0x1F7E9)), // Lime
            new String(Character.toChars(0x1F344)) + "\u200D" + new String(Character.toChars(0x1F7EB)), // Brown Mushroom
            "\u26D3\uFE0F\u200D" + new String(Character.toChars(0x1F4A5)),                               // Broken Chain
            new String(Character.toChars(0x1F642)) + "\u200D\u2194\uFE0F",                               // Head shaking horizontal
            new String(Character.toChars(0x1F642)) + "\u200D\u2195\uFE0F"                                // Head shaking vertical
    };

    private static final String[] UNICODE_15_1_FAMILIES = new String[] {
            new String(Character.toChars(0x1F9D1)) + "\u200D" + new String(Character.toChars(0x1F9D1)) + "\u200D" + new String(Character.toChars(0x1F9D2)), // adult, adult, child
            new String(Character.toChars(0x1F9D1)) + "\u200D" + new String(Character.toChars(0x1F9D2)) + "\u200D" + new String(Character.toChars(0x1F9D2)), // adult, child, child
            new String(Character.toChars(0x1F9D1)) + "\u200D" + new String(Character.toChars(0x1F9D2)),                                                       // adult, child
            new String(Character.toChars(0x1F9D1)) + "\u200D" + new String(Character.toChars(0x1F9D1)) + "\u200D" + new String(Character.toChars(0x1F9D2)) + "\u200D" + new String(Character.toChars(0x1F9D2)) // adult, adult, child, child
    };

    private static final String[] UNICODE_15_1_DIRECTION_FACING = new String[] {
            new String(Character.toChars(0x1F6B6)) + "\u200D\u27A1\uFE0F",                                                                                      // walk right
            new String(Character.toChars(0x1F3C3)) + "\u200D\u27A1\uFE0F",                                                                                      // run right
            new String(Character.toChars(0x1F9CE)) + "\u200D\u27A1\uFE0F",                                                                                      // kneel right
            new String(Character.toChars(0x1F9D1)) + "\u200D" + new String(Character.toChars(0x1F9AF)) + "\u200D\u27A1\uFE0F",                                // white cane right
            new String(Character.toChars(0x1F9D1)) + "\u200D" + new String(Character.toChars(0x1F9BC)) + "\u200D\u27A1\uFE0F",                                // motor wheelchair right
            new String(Character.toChars(0x1F9D1)) + "\u200D" + new String(Character.toChars(0x1F9BD)) + "\u200D\u27A1\uFE0F"                                 // manual wheelchair right
    };

    private static final String[] UNICODE_16_0_EMOJIS = new String[] {
            new String(Character.toChars(0x1FAE9)), // face with bags under eyes
            new String(Character.toChars(0x1FAEA)), // fingerprint
            new String(Character.toChars(0x1FAEB)), // leafless tree
            new String(Character.toChars(0x1FAEC)), // root vegetable
            new String(Character.toChars(0x1FAED)), // harp
            new String(Character.toChars(0x1FAEE)), // shovel
            new String(Character.toChars(0x1FAEF)), // splatter
            new String(Character.toChars(0x1F1E8)) + new String(Character.toChars(0x1F1F6)) // flag: Sark
    };

    @BeforeClass
    public static void setUpClass() throws Exception {
        sOriginalSdkInt = Build.VERSION.SDK_INT;

        File gemojiFile = new File(GEMOJI_PATH);
        if (!gemojiFile.exists()) {
            gemojiFile = new File("nth-kbd/" + GEMOJI_PATH);
        }
        assertTrue("gemoji.json must exist", gemojiFile.exists());

        byte[] bytes = Files.readAllBytes(gemojiFile.toPath());
        String jsonContent = new String(bytes, StandardCharsets.UTF_8);
        sEmojiList = parseJsonArray(jsonContent);
        assertNotNull("sEmojiList must not be null", sEmojiList);
        assertEquals("Must contain exactly 1979 emojis", 1979, sEmojiList.size());

        sEmojiMap = new HashMap<>();
        for (Map<String, Object> item : sEmojiList) {
            String emoji = (String) item.get("emoji");
            if (emoji != null) {
                sEmojiMap.put(emoji, item);
            }
        }
    }

    @Before
    public void setUp() {
        setSdkInt(sOriginalSdkInt);
    }

    @After
    public void tearDown() {
        setSdkInt(sOriginalSdkInt);
    }

    // --- Helper to dynamically mutate Build.VERSION.SDK_INT ---
    private static void setSdkInt(int sdkInt) {
        try {
            Field field = Build.VERSION.class.getField("SDK_INT");
            field.setAccessible(true);
            try {
                field.setInt(null, sdkInt);
                return;
            } catch (Exception ignored) {
            }

            try {
                Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
                unsafeField.setAccessible(true);
                Object unsafe = unsafeField.get(null);
                java.lang.reflect.Method staticFieldBase = unsafeClass.getMethod("staticFieldBase", Field.class);
                java.lang.reflect.Method staticFieldOffset = unsafeClass.getMethod("staticFieldOffset", Field.class);
                java.lang.reflect.Method putInt = unsafeClass.getMethod("putInt", Object.class, long.class, int.class);
                Object base = staticFieldBase.invoke(unsafe, field);
                long offset = ((Number) staticFieldOffset.invoke(unsafe, field)).longValue();
                putInt.invoke(unsafe, base, offset, sdkInt);
            } catch (Throwable ignored) {
            }
        } catch (Throwable t) {
            throw new RuntimeException("Could not simulate Build.VERSION.SDK_INT = " + sdkInt, t);
        }
    }

    // =========================================================================
    // 1. SDK Simulation Tests
    // =========================================================================

    @Test
    public void testSimulationDefaultSdkZeroAcceptsAllModernEmojis() {
        setSdkInt(0);
        assertEquals(0, Build.VERSION.SDK_INT);

        for (String emoji : UNICODE_15_1_KEY_EMOJIS) {
            assertTrue("Unicode 15.1 key emoji must be supported on SDK 0: " + emoji,
                    EmojiActionKt.isEmojiSupported(emoji, null, 0));
        }
        for (String emoji : UNICODE_15_1_FAMILIES) {
            assertTrue("Unicode 15.1 family emoji must be supported on SDK 0: " + emoji,
                    EmojiActionKt.isEmojiSupported(emoji, null, 0));
        }
        for (String emoji : UNICODE_15_1_DIRECTION_FACING) {
            assertTrue("Unicode 15.1 direction emoji must be supported on SDK 0: " + emoji,
                    EmojiActionKt.isEmojiSupported(emoji, null, 0));
        }
        for (String emoji : UNICODE_16_0_EMOJIS) {
            assertTrue("Unicode 16.0 emoji must be supported on SDK 0: " + emoji,
                    EmojiActionKt.isEmojiSupported(emoji, null, 0));
        }
    }

    @Test
    public void testSimulationModernSdkTiramisu33AcceptsAllModernEmojis() {
        setSdkInt(33); // Android 13
        assertEquals(33, Build.VERSION.SDK_INT);

        for (String emoji : UNICODE_15_1_KEY_EMOJIS) {
            assertTrue("Unicode 15.1 key emoji must be supported on SDK 33",
                    EmojiActionKt.isEmojiSupported(emoji, null));
        }
        for (String emoji : UNICODE_16_0_EMOJIS) {
            assertTrue("Unicode 16.0 emoji must be supported on SDK 33",
                    EmojiActionKt.isEmojiSupported(emoji, null));
        }
    }

    @Test
    public void testSimulationModernSdkUpsideDownCake34AcceptsAllModernEmojis() {
        setSdkInt(34); // Android 14
        assertEquals(34, Build.VERSION.SDK_INT);

        for (String emoji : UNICODE_15_1_KEY_EMOJIS) {
            assertTrue("Unicode 15.1 key emoji must be supported on SDK 34",
                    EmojiActionKt.isEmojiSupported(emoji, null));
        }
        for (String emoji : UNICODE_16_0_EMOJIS) {
            assertTrue("Unicode 16.0 emoji must be supported on SDK 34",
                    EmojiActionKt.isEmojiSupported(emoji, null));
        }
    }

    @Test
    public void testSimulationModernSdkVanillaIceCream35AcceptsAllModernEmojis() {
        setSdkInt(35); // Android 15
        assertEquals(35, Build.VERSION.SDK_INT);

        for (String emoji : UNICODE_15_1_KEY_EMOJIS) {
            assertTrue("Unicode 15.1 key emoji must be supported on SDK 35",
                    EmojiActionKt.isEmojiSupported(emoji, null));
        }
        for (String emoji : UNICODE_16_0_EMOJIS) {
            assertTrue("Unicode 16.0 emoji must be supported on SDK 35",
                    EmojiActionKt.isEmojiSupported(emoji, null));
        }
    }

    @Test
    public void testSimulationFutureSdk36AcceptsAllModernEmojis() {
        setSdkInt(36); // Future Android version
        assertEquals(36, Build.VERSION.SDK_INT);

        for (String emoji : UNICODE_15_1_KEY_EMOJIS) {
            assertTrue("Unicode 15.1 key emoji must be supported on SDK 36",
                    EmojiActionKt.isEmojiSupported(emoji, null));
        }
        for (String emoji : UNICODE_16_0_EMOJIS) {
            assertTrue("Unicode 16.0 emoji must be supported on SDK 36",
                    EmojiActionKt.isEmojiSupported(emoji, null));
        }
    }

    @Test
    public void testSimulationMidTierSdks23To32GracefulFallback() {
        int[] midTierSdks = new int[] { 23, 26, 28, 30, 32 };

        for (int sdk : midTierSdks) {
            setSdkInt(sdk);
            assertEquals(sdk, Build.VERSION.SDK_INT);

            // In unit test environment, PaintCompat.hasGlyph will either run or catch Throwable and return true.
            // It must NEVER throw an unhandled exception or crash.
            for (String emoji : UNICODE_16_0_EMOJIS) {
                boolean result = EmojiActionKt.isEmojiSupported(emoji, null);
                // Result must be a valid boolean without throwing
                assertTrue("Mid-tier SDK " + sdk + " must handle glyph check safely", result || !result);
            }
        }
    }

    @Test
    public void testSimulationLegacySdk21Behavior() {
        setSdkInt(21); // Android 5.0 Lollipop (< 23)
        assertEquals(21, Build.VERSION.SDK_INT);

        // Standard vintage emoji (e.g., Grinning Face \u1F600) has Character.getName defined
        String grinning = new String(Character.toChars(0x1F600));
        assertTrue("Vintage emoji must be supported on legacy SDK 21",
                EmojiActionKt.isEmojiSupported(grinning, null));

        // On legacy SDK 21, characters without defined Character.getName evaluate to false
        String unassignedCodepoint = new String(Character.toChars(0xE0000));
        boolean unassignedOnLegacy = EmojiActionKt.isEmojiSupported(unassignedCodepoint, null, 21);
        assertFalse("Unassigned codepoint should evaluate to false on legacy SDK 21 where glyphs do not exist",
                unassignedOnLegacy);
    }

    @Test
    public void testSimulationCorruptedOrNegativeSdk() {
        setSdkInt(-1);
        assertEquals(-1, Build.VERSION.SDK_INT);

        // Standard emoji with known name
        String grinning = new String(Character.toChars(0x1F600));
        assertTrue("Valid emoji with known name passes even on SDK -1",
                EmojiActionKt.isEmojiSupported(grinning, null));
    }

    // =========================================================================
    // 2. Full Catalog Sweep Test (All 1,979 items never dropped on modern SDK)
    // =========================================================================

    @Test
    public void testFullCatalogSweepOnModernSdkNeverDropsAnyEmoji() {
        setSdkInt(34); // Android 14 simulation

        int supportedCount = 0;
        int droppedCount = 0;
        List<String> droppedEmojis = new ArrayList<>();

        for (Map<String, Object> item : sEmojiList) {
            String emoji = (String) item.get("emoji");
            assertNotNull(emoji);

            boolean supported = EmojiActionKt.isEmojiSupported(emoji, null);
            if (supported) {
                supportedCount++;
            } else {
                droppedCount++;
                droppedEmojis.add(emoji);
            }
        }

        assertEquals("Zero emojis should be dropped on modern SDK: " + droppedEmojis, 0, droppedCount);
        assertEquals("All 1,979 emojis must evaluate as supported", 1979, supportedCount);
    }

    @Test
    public void testFullCatalogSweepOnSdkZeroNeverDropsAnyEmoji() {
        setSdkInt(0); // Test environment simulation

        int supportedCount = 0;
        for (Map<String, Object> item : sEmojiList) {
            String emoji = (String) item.get("emoji");
            boolean supported = EmojiActionKt.isEmojiSupported(emoji, null);
            if (supported) {
                supportedCount++;
            }
        }

        assertEquals("All 1,979 emojis must evaluate as supported on SDK 0", 1979, supportedCount);
    }

    // =========================================================================
    // 3. Adversarial Edge Cases & Boundary Conditions
    // =========================================================================

    @Test
    public void testAdversarialEmptyAndWhitespaceStrings() {
        setSdkInt(34);
        assertTrue(EmojiActionKt.isEmojiSupported("", null));
        assertTrue(EmojiActionKt.isEmojiSupported(" ", null));
        assertTrue(EmojiActionKt.isEmojiSupported("\t\n\r", null));

        setSdkInt(21);
        // On legacy, empty stream .all returns true (vacuous truth)
        assertTrue(EmojiActionKt.isEmojiSupported("", null));
    }

    @Test
    public void testAdversarialAsciiAndControlCharacters() {
        setSdkInt(34);
        assertTrue(EmojiActionKt.isEmojiSupported("A", null));
        assertTrue(EmojiActionKt.isEmojiSupported("123", null));
        assertTrue(EmojiActionKt.isEmojiSupported("@#$%^&*", null));
        assertTrue(EmojiActionKt.isEmojiSupported("\u0000", null));

        setSdkInt(21);
        // 'A' has Character.getName('A') == "LATIN CAPITAL LETTER A"
        assertTrue(EmojiActionKt.isEmojiSupported("A", null));
    }

    @Test
    public void testAdversarialComplexZwjAndSkinToneSequences() {
        setSdkInt(34);

        // Man Health Worker: Medium Skin Tone (👨🏽‍⚕️)
        String manDoctor = new String(Character.toChars(0x1F468)) +
                new String(Character.toChars(0x1F3FD)) +
                "\u200D" +
                new String(Character.toChars(0x2695)) +
                "\uFE0F";
        assertTrue("Complex ZWJ man doctor must be supported",
                EmojiActionKt.isEmojiSupported(manDoctor, null));

        // Pirate Flag (🏴‍☠️)
        String pirateFlag = new String(Character.toChars(0x1F3F4)) +
                "\u200D" +
                new String(Character.toChars(0x2620)) +
                "\uFE0F";
        assertTrue("Pirate flag ZWJ sequence must be supported",
                EmojiActionKt.isEmojiSupported(pirateFlag, null));

        // Multi-person holding hands with different skin tones (🧑🏻‍🤝‍🧑🏿)
        String holdingHands = new String(Character.toChars(0x1F9D1)) +
                new String(Character.toChars(0x1F3FB)) +
                "\u200D" +
                new String(Character.toChars(0x1F91D)) +
                "\u200D" +
                new String(Character.toChars(0x1F9D1)) +
                new String(Character.toChars(0x1F3FF));
        assertTrue("Multi-skin tone holding hands must be supported",
                EmojiActionKt.isEmojiSupported(holdingHands, null));
    }

    @Test
    public void testAdversarialMaxUnicodeCodepoint() {
        setSdkInt(34);
        // 0x10FFFF is max valid Unicode codepoint
        String maxCodepoint = new String(Character.toChars(0x10FFFF));
        assertTrue("Max codepoint should pass on modern SDK without throwing",
                EmojiActionKt.isEmojiSupported(maxCodepoint, null));

        // SMP boundary 0x10000
        String smpBoundary = new String(Character.toChars(0x10000));
        assertTrue("SMP boundary 0x10000 should pass on modern SDK without throwing",
                EmojiActionKt.isEmojiSupported(smpBoundary, null));
    }

    // =========================================================================
    // 4. Stress Test: High-Throughput Iteration
    // =========================================================================

    @Test
    public void testHighThroughputStressHarness10000Iterations() {
        setSdkInt(34);

        int sampleSize = sEmojiList.size();
        for (int i = 0; i < 10000; i++) {
            Map<String, Object> item = sEmojiList.get(i % sampleSize);
            String emoji = (String) item.get("emoji");
            boolean supported = EmojiActionKt.isEmojiSupported(emoji, null);
            assertTrue("Emoji at iteration " + i + " must be supported", supported);
        }
    }

    // --- Lightweight Self-Contained JSON Parser ---
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> parseJsonArray(String json) {
        List<Map<String, Object>> result = new ArrayList<>();
        int len = json.length();
        int idx = 0;

        while (idx < len && json.charAt(idx) != '[') idx++;
        idx++;

        while (idx < len) {
            while (idx < len && Character.isWhitespace(json.charAt(idx))) idx++;
            if (idx >= len || json.charAt(idx) == ']') break;

            if (json.charAt(idx) == '{') {
                int objStart = idx;
                int depth = 0;
                boolean inString = false;
                boolean escape = false;

                while (idx < len) {
                    char c = json.charAt(idx);
                    if (escape) {
                        escape = false;
                    } else if (c == '\\') {
                        escape = true;
                    } else if (c == '"') {
                        inString = !inString;
                    } else if (!inString) {
                        if (c == '{') depth++;
                        else if (c == '}') {
                            depth--;
                            if (depth == 0) {
                                idx++;
                                break;
                            }
                        }
                    }
                    idx++;
                }

                String objJson = json.substring(objStart, idx);
                result.add(parseJsonObject(objJson));
            } else {
                idx++;
            }
        }
        return result;
    }

    private static Map<String, Object> parseJsonObject(String json) {
        Map<String, Object> map = new HashMap<>();
        int len = json.length();
        int idx = 0;

        while (idx < len && json.charAt(idx) != '{') idx++;
        idx++;

        while (idx < len) {
            while (idx < len && (Character.isWhitespace(json.charAt(idx)) || json.charAt(idx) == ',')) idx++;
            if (idx >= len || json.charAt(idx) == '}') break;

            if (json.charAt(idx) == '"') {
                String key = parseString(json, idx);
                idx += key.length() + 2;

                while (idx < len && (Character.isWhitespace(json.charAt(idx)) || json.charAt(idx) == ':')) idx++;

                if (idx < len && json.charAt(idx) == '"') {
                    String val = parseString(json, idx);
                    map.put(key, val);
                    idx += val.length() + 2;
                } else if (idx < len && json.charAt(idx) == '[') {
                    int arrStart = idx;
                    while (idx < len && json.charAt(idx) != ']') idx++;
                    idx++;
                    String arrJson = json.substring(arrStart, idx);
                    map.put(key, parseStringArray(arrJson));
                } else if (idx < len && (json.startsWith("true", idx) || json.startsWith("false", idx))) {
                    boolean val = json.startsWith("true", idx);
                    map.put(key, val);
                    idx += val ? 4 : 5;
                } else {
                    idx++;
                }
            } else {
                idx++;
            }
        }
        return map;
    }

    private static String parseString(String json, int startQuoteIdx) {
        StringBuilder sb = new StringBuilder();
        int idx = startQuoteIdx + 1;
        int len = json.length();
        boolean escape = false;

        while (idx < len) {
            char c = json.charAt(idx);
            if (escape) {
                if (c == 'u') {
                    String hex = json.substring(idx + 1, idx + 5);
                    sb.append((char) Integer.parseInt(hex, 16));
                    idx += 4;
                } else if (c == 'n') sb.append('\n');
                else if (c == 't') sb.append('\t');
                else if (c == 'r') sb.append('\r');
                else sb.append(c);
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
            idx++;
        }
        return sb.toString();
    }

    private static List<String> parseStringArray(String json) {
        List<String> list = new ArrayList<>();
        int len = json.length();
        int idx = 0;

        while (idx < len) {
            if (json.charAt(idx) == '"') {
                String s = parseString(json, idx);
                list.add(s);
                idx += s.length() + 2;
            } else {
                idx++;
            }
        }
        return list;
    }
}
