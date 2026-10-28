package org.futo.inputmethod.latin.uix.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Unit tests verifying:
 * 1. gemoji.json parsing and strict schema compliance.
 * 2. Presence and metadata of Unicode 15.1 and 16.0 modern emojis.
 * 3. EmojiAction compatibility refactor ensuring modern emojis are not dropped.
 */
public class EmojiIntegrationTest {

    private static final String GEMOJI_PATH = "java/res/raw/gemoji.json";
    private static File sGemojiFile;
    private static String sJsonContent;
    private static List<Map<String, Object>> sEmojiList;
    private static Map<String, Map<String, Object>> sEmojiMap;

    @BeforeClass
    public static void setUpClass() throws Exception {
        sGemojiFile = new File(GEMOJI_PATH);
        if (!sGemojiFile.exists()) {
            sGemojiFile = new File("nth-kbd/" + GEMOJI_PATH);
        }
        assertTrue("gemoji.json must exist at " + GEMOJI_PATH, sGemojiFile.exists());

        byte[] bytes = Files.readAllBytes(sGemojiFile.toPath());
        sJsonContent = new String(bytes, StandardCharsets.UTF_8);
        assertNotNull("JSON content must not be null", sJsonContent);
        assertFalse("JSON content must not be empty", sJsonContent.trim().isEmpty());

        sEmojiList = parseJsonArray(sJsonContent);
        assertNotNull("Parsed emoji list must not be null", sEmojiList);
        assertTrue("Must contain at least 1950 emojis, found: " + sEmojiList.size(), sEmojiList.size() >= 1950);

        sEmojiMap = new HashMap<>();
        for (Map<String, Object> item : sEmojiList) {
            String emoji = (String) item.get("emoji");
            if (emoji != null) {
                sEmojiMap.put(emoji, item);
            }
        }
    }

    @Test
    public void testFileExistsAndParsesCleanly() {
        assertTrue(sGemojiFile.exists());
        assertTrue(sEmojiList.size() >= 1970);
    }

    @Test
    public void testSchemaComplianceAndCategoryContiguity() {
        Set<String> validCategories = new HashSet<>(Arrays.asList(
                "Smileys & Emotion",
                "People & Body",
                "Animals & Nature",
                "Food & Drink",
                "Travel & Places",
                "Activities",
                "Objects",
                "Symbols",
                "Flags",
                "ASCII"
        ));

        String currentCategory = "";
        Set<String> seenCategories = new HashSet<>();

        for (int i = 0; i < sEmojiList.size(); i++) {
            Map<String, Object> item = sEmojiList.get(i);

            // Mandatory fields
            assertNotNull("Item " + i + " must have 'emoji'", item.get("emoji"));
            String emoji = (String) item.get("emoji");
            assertFalse("Emoji must not be empty at index " + i, emoji.isEmpty());

            assertNotNull("Item " + i + " must have 'description'", item.get("description"));
            String desc = (String) item.get("description");
            assertFalse("Description must not be empty at index " + i, desc.isEmpty());

            assertNotNull("Item " + i + " must have 'category'", item.get("category"));
            String cat = (String) item.get("category");
            assertTrue("Category '" + cat + "' must be valid at index " + i, validCategories.contains(cat));

            assertNotNull("Item " + i + " must have 'aliases'", item.get("aliases"));
            assertTrue("Aliases must be a list at index " + i, item.get("aliases") instanceof List);

            assertNotNull("Item " + i + " must have 'tags'", item.get("tags"));
            assertTrue("Tags must be a list at index " + i, item.get("tags") instanceof List);

            assertNotNull("Item " + i + " must have 'unicode_version'", item.get("unicode_version"));
            assertNotNull("Item " + i + " must have 'ios_version'", item.get("ios_version"));

            // Check contiguity: category transitions must never revert to a previously seen category
            if (!cat.equals(currentCategory)) {
                assertFalse("Category '" + cat + "' is not contiguous (seen previously at index " + i + ")",
                        seenCategories.contains(cat));
                seenCategories.add(cat);
                currentCategory = cat;
            }
        }

        assertEquals("Must see all 10 distinct categories", 10, seenCategories.size());
    }

    @Test
    public void testUnicode151KeyEmojis() {
        // Phoenix: \u1F426\u200D\u1F525
        String phoenix = new String(Character.toChars(0x1F426)) + "\u200D" + new String(Character.toChars(0x1F525));
        Map<String, Object> item = sEmojiMap.get(phoenix);
        assertNotNull("Phoenix emoji must be present", item);
        assertEquals("phoenix", item.get("description"));
        assertEquals("Animals & Nature", item.get("category"));
        assertEquals("15.1", item.get("unicode_version"));

        // Lime: \u1F34B\u200D\u1F7E9
        String lime = new String(Character.toChars(0x1F34B)) + "\u200D" + new String(Character.toChars(0x1F7E9));
        item = sEmojiMap.get(lime);
        assertNotNull("Lime emoji must be present", item);
        assertEquals("lime", item.get("description"));
        assertEquals("Food & Drink", item.get("category"));
        assertEquals("15.1", item.get("unicode_version"));

        // Brown Mushroom: \u1F344\u200D\u1F7EB
        String brownMushroom = new String(Character.toChars(0x1F344)) + "\u200D" + new String(Character.toChars(0x1F7EB));
        item = sEmojiMap.get(brownMushroom);
        assertNotNull("Brown Mushroom emoji must be present", item);
        assertEquals("brown mushroom", item.get("description"));
        assertEquals("Food & Drink", item.get("category"));
        assertEquals("15.1", item.get("unicode_version"));

        // Broken Chain: \u26D3\uFE0F\u200D\u1F4A5
        String brokenChain = "\u26D3\uFE0F\u200D" + new String(Character.toChars(0x1F4A5));
        item = sEmojiMap.get(brokenChain);
        assertNotNull("Broken Chain emoji must be present", item);
        assertEquals("broken chain", item.get("description"));
        assertEquals("Objects", item.get("category"));
        assertEquals("15.1", item.get("unicode_version"));

        // Head Shaking Horizontally: \u1F642\u200D\u2194\uFE0F
        String headHoriz = new String(Character.toChars(0x1F642)) + "\u200D\u2194\uFE0F";
        item = sEmojiMap.get(headHoriz);
        assertNotNull("Head shaking horizontally emoji must be present", item);
        assertEquals("head shaking horizontally", item.get("description"));
        assertEquals("Smileys & Emotion", item.get("category"));
        assertEquals("15.1", item.get("unicode_version"));

        // Head Shaking Vertically: \u1F642\u200D\u2195\uFE0F
        String headVert = new String(Character.toChars(0x1F642)) + "\u200D\u2195\uFE0F";
        item = sEmojiMap.get(headVert);
        assertNotNull("Head shaking vertically emoji must be present", item);
        assertEquals("head shaking vertically", item.get("description"));
        assertEquals("Smileys & Emotion", item.get("category"));
        assertEquals("15.1", item.get("unicode_version"));
    }

    @Test
    public void testUnicode151Families() {
        String adult = new String(Character.toChars(0x1F9D1));
        String child = new String(Character.toChars(0x1F9D2));

        // family: adult, adult, child
        String f1 = adult + "\u200D" + adult + "\u200D" + child;
        Map<String, Object> item = sEmojiMap.get(f1);
        assertNotNull("family adult, adult, child must be present", item);
        assertEquals("family: adult, adult, child", item.get("description"));
        assertEquals("People & Body", item.get("category"));

        // family: adult, child, child
        String f2 = adult + "\u200D" + child + "\u200D" + child;
        item = sEmojiMap.get(f2);
        assertNotNull("family adult, child, child must be present", item);
        assertEquals("family: adult, child, child", item.get("description"));

        // family: adult, child
        String f3 = adult + "\u200D" + child;
        item = sEmojiMap.get(f3);
        assertNotNull("family adult, child must be present", item);
        assertEquals("family: adult, child", item.get("description"));

        // family: adult, adult, child, child
        String f4 = adult + "\u200D" + adult + "\u200D" + child + "\u200D" + child;
        item = sEmojiMap.get(f4);
        assertNotNull("family adult, adult, child, child must be present", item);
        assertEquals("family: adult, adult, child, child", item.get("description"));
    }

    @Test
    public void testUnicode151DirectionFacingPeople() {
        // person walking facing right
        String walkRight = new String(Character.toChars(0x1F6B6)) + "\u200D\u27A1\uFE0F";
        Map<String, Object> item = sEmojiMap.get(walkRight);
        assertNotNull("person walking facing right must be present", item);
        assertEquals("person walking facing right", item.get("description"));
        assertEquals("People & Body", item.get("category"));
        assertEquals(Boolean.TRUE, item.get("skin_tones"));

        // person running facing right
        String runRight = new String(Character.toChars(0x1F3C3)) + "\u200D\u27A1\uFE0F";
        item = sEmojiMap.get(runRight);
        assertNotNull("person running facing right must be present", item);
        assertEquals("person running facing right", item.get("description"));
        assertEquals(Boolean.TRUE, item.get("skin_tones"));

        // person kneeling facing right
        String kneelRight = new String(Character.toChars(0x1F9CE)) + "\u200D\u27A1\uFE0F";
        item = sEmojiMap.get(kneelRight);
        assertNotNull("person kneeling facing right must be present", item);
        assertEquals("person kneeling facing right", item.get("description"));
        assertEquals(Boolean.TRUE, item.get("skin_tones"));

        // person with white cane facing right
        String caneRight = new String(Character.toChars(0x1F9D1)) + "\u200D" + new String(Character.toChars(0x1F9AF)) + "\u200D\u27A1\uFE0F";
        item = sEmojiMap.get(caneRight);
        assertNotNull("person with white cane facing right must be present", item);
        assertEquals("person with white cane facing right", item.get("description"));
        assertEquals(Boolean.TRUE, item.get("skin_tones"));

        // person in motorized wheelchair facing right
        String motorWheelchairRight = new String(Character.toChars(0x1F9D1)) + "\u200D" + new String(Character.toChars(0x1F9BC)) + "\u200D\u27A1\uFE0F";
        item = sEmojiMap.get(motorWheelchairRight);
        assertNotNull("person in motorized wheelchair facing right must be present", item);
        assertEquals("person in motorized wheelchair facing right", item.get("description"));
        assertEquals(Boolean.TRUE, item.get("skin_tones"));

        // person in manual wheelchair facing right
        String manualWheelchairRight = new String(Character.toChars(0x1F9D1)) + "\u200D" + new String(Character.toChars(0x1F9BD)) + "\u200D\u27A1\uFE0F";
        item = sEmojiMap.get(manualWheelchairRight);
        assertNotNull("person in manual wheelchair facing right must be present", item);
        assertEquals("person in manual wheelchair facing right", item.get("description"));
        assertEquals(Boolean.TRUE, item.get("skin_tones"));
    }

    @Test
    public void testUnicode160Emojis() {
        // Face with bags under eyes (0x1FAE9)
        String bagsFace = new String(Character.toChars(0x1FAE9));
        Map<String, Object> item = sEmojiMap.get(bagsFace);
        assertNotNull("face with bags under eyes must be present", item);
        assertEquals("face with bags under eyes", item.get("description"));
        assertEquals("Smileys & Emotion", item.get("category"));
        assertEquals("16.0", item.get("unicode_version"));

        // Fingerprint (0x1FAEA)
        String fingerprint = new String(Character.toChars(0x1FAEA));
        item = sEmojiMap.get(fingerprint);
        assertNotNull("fingerprint must be present", item);
        assertEquals("fingerprint", item.get("description"));
        assertEquals("People & Body", item.get("category"));
        assertEquals("16.0", item.get("unicode_version"));

        // Leafless tree (0x1FAEB)
        String tree = new String(Character.toChars(0x1FAEB));
        item = sEmojiMap.get(tree);
        assertNotNull("leafless tree must be present", item);
        assertEquals("leafless tree", item.get("description"));
        assertEquals("Animals & Nature", item.get("category"));
        assertEquals("16.0", item.get("unicode_version"));

        // Root vegetable (0x1FAEC)
        String rootVeg = new String(Character.toChars(0x1FAEC));
        item = sEmojiMap.get(rootVeg);
        assertNotNull("root vegetable must be present", item);
        assertEquals("root vegetable", item.get("description"));
        assertEquals("Food & Drink", item.get("category"));
        assertEquals("16.0", item.get("unicode_version"));

        // Harp (0x1FAED)
        String harp = new String(Character.toChars(0x1FAED));
        item = sEmojiMap.get(harp);
        assertNotNull("harp must be present", item);
        assertEquals("harp", item.get("description"));
        assertEquals("Objects", item.get("category"));
        assertEquals("16.0", item.get("unicode_version"));

        // Shovel (0x1FAEE)
        String shovel = new String(Character.toChars(0x1FAEE));
        item = sEmojiMap.get(shovel);
        assertNotNull("shovel must be present", item);
        assertEquals("shovel", item.get("description"));
        assertEquals("Objects", item.get("category"));
        assertEquals("16.0", item.get("unicode_version"));

        // Splatter (0x1FAEF)
        String splatter = new String(Character.toChars(0x1FAEF));
        item = sEmojiMap.get(splatter);
        assertNotNull("splatter must be present", item);
        assertEquals("splatter", item.get("description"));
        assertEquals("Activities", item.get("category"));
        assertEquals("16.0", item.get("unicode_version"));

        // Flag of Sark
        String sark = new String(Character.toChars(0x1F1E8)) + new String(Character.toChars(0x1F1F6));
        item = sEmojiMap.get(sark);
        assertNotNull("flag of Sark must be present", item);
        assertEquals("flag: Sark", item.get("description"));
        assertEquals("Flags", item.get("category"));
        assertEquals("16.0", item.get("unicode_version"));
    }

    @Test
    public void testIsEmojiSupportedAcceptsModernEmojis() {
        String bagsFace = new String(Character.toChars(0x1FAE9));
        String phoenix = new String(Character.toChars(0x1F426)) + "\u200D" + new String(Character.toChars(0x1F525));

        // Call the refactored function
        boolean bagsSupported = EmojiActionKt.isEmojiSupported(bagsFace, null);
        assertTrue("Face with bags under eyes (16.0) must be supported by refactored logic", bagsSupported);

        boolean phoenixSupported = EmojiActionKt.isEmojiSupported(phoenix, null);
        assertTrue("Phoenix (15.1) must be supported by refactored logic", phoenixSupported);
    }

    // --- Lightweight Self-Contained JSON Parser ---
    private static List<Map<String, Object>> parseJsonArray(String json) {
        List<Map<String, Object>> result = new ArrayList<>();
        int len = json.length();
        int idx = 0;

        // Skip to '['
        while (idx < len && json.charAt(idx) != '[') idx++;
        idx++; // skip '['

        while (idx < len) {
            // Skip whitespace and commas
            while (idx < len && (Character.isWhitespace(json.charAt(idx)) || json.charAt(idx) == ',')) idx++;
            if (idx >= len || json.charAt(idx) == ']') break;

            if (json.charAt(idx) == '{') {
                int objEnd = findMatchingBrace(json, idx, '{', '}');
                String objStr = json.substring(idx, objEnd + 1);
                Map<String, Object> obj = parseJsonObject(objStr);
                result.add(obj);
                idx = objEnd + 1;
            } else {
                idx++;
            }
        }
        return result;
    }

    private static int findMatchingBrace(String s, int start, char open, char close) {
        int depth = 0;
        boolean inString = false;
        boolean escape = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (escape) {
                escape = false;
                continue;
            }
            if (c == '\\') {
                escape = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (!inString) {
                if (c == open) depth++;
                else if (c == close) {
                    depth--;
                    if (depth == 0) return i;
                }
            }
        }
        return s.length() - 1;
    }

    private static Map<String, Object> parseJsonObject(String s) {
        Map<String, Object> map = new HashMap<>();
        int idx = 0;
        int len = s.length();

        while (idx < len && s.charAt(idx) != '{') idx++;
        idx++;

        while (idx < len) {
            while (idx < len && (Character.isWhitespace(s.charAt(idx)) || s.charAt(idx) == ',')) idx++;
            if (idx >= len || s.charAt(idx) == '}') break;

            if (s.charAt(idx) == '"') {
                int keyEnd = findStringEnd(s, idx);
                String key = unescapeJson(s.substring(idx + 1, keyEnd));
                idx = keyEnd + 1;

                while (idx < len && (Character.isWhitespace(s.charAt(idx)) || s.charAt(idx) == ':')) idx++;

                if (idx < len && s.charAt(idx) == '"') {
                    int valEnd = findStringEnd(s, idx);
                    String val = unescapeJson(s.substring(idx + 1, valEnd));
                    map.put(key, val);
                    idx = valEnd + 1;
                } else if (idx < len && s.charAt(idx) == '[') {
                    int arrEnd = findMatchingBrace(s, idx, '[', ']');
                    List<String> list = parseStringArray(s.substring(idx, arrEnd + 1));
                    map.put(key, list);
                    idx = arrEnd + 1;
                } else if (idx < len && (s.startsWith("true", idx) || s.startsWith("false", idx))) {
                    boolean val = s.startsWith("true", idx);
                    map.put(key, val);
                    idx += (val ? 4 : 5);
                } else {
                    idx++;
                }
            } else {
                idx++;
            }
        }
        return map;
    }

    private static int findStringEnd(String s, int start) {
        boolean escape = false;
        for (int i = start + 1; i < s.length(); i++) {
            char c = s.charAt(i);
            if (escape) {
                escape = false;
                continue;
            }
            if (c == '\\') {
                escape = true;
                continue;
            }
            if (c == '"') {
                return i;
            }
        }
        return s.length() - 1;
    }

    private static List<String> parseStringArray(String s) {
        List<String> list = new ArrayList<>();
        int idx = 0;
        int len = s.length();
        while (idx < len && s.charAt(idx) != '[') idx++;
        idx++;

        while (idx < len) {
            while (idx < len && (Character.isWhitespace(s.charAt(idx)) || s.charAt(idx) == ',')) idx++;
            if (idx >= len || s.charAt(idx) == ']') break;

            if (s.charAt(idx) == '"') {
                int end = findStringEnd(s, idx);
                list.add(unescapeJson(s.substring(idx + 1, end)));
                idx = end + 1;
            } else {
                idx++;
            }
        }
        return list;
    }

    private static String unescapeJson(String s) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int len = s.length();
        while (i < len) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < len) {
                char next = s.charAt(i + 1);
                if (next == 'u' && i + 5 < len) {
                    try {
                        int code = Integer.parseInt(s.substring(i + 2, i + 6), 16);
                        sb.append((char) code);
                        i += 6;
                        continue;
                    } catch (NumberFormatException ignored) {}
                } else if (next == '"' || next == '\\' || next == '/') {
                    sb.append(next);
                    i += 2;
                    continue;
                } else if (next == 'n') {
                    sb.append('\n');
                    i += 2;
                    continue;
                } else if (next == 'r') {
                    sb.append('\r');
                    i += 2;
                    continue;
                } else if (next == 't') {
                    sb.append('\t');
                    i += 2;
                    continue;
                }
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }
}
