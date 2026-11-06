package org.futo.inputmethod.latin.uix.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.futo.inputmethod.keyboard.internal.TranslationHelper;
import org.futo.inputmethod.latin.R;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

/**
 * Unit tests verifying TranslateAction requirements:
 * 1. Compact WRAP_CONTENT height (no fillMaxHeight() on DropdownMenu).
 * 2. Strict 3-option limit for source ("auto", "bn", "en") and valid options for target ("bn", "en").
 * 3. Rounded boxy styling matching NeuCard / Nothing OS (RoundedCornerShape(16.dp), clip, border).
 * 4. Helper and mapping sanity.
 */
public class TranslateActionTest {

    @Test
    public void testTranslationHelperContainsRequiredLanguages() {
        Map<String, String> languages = TranslationHelper.getLanguages();
        assertNotNull(languages);
        assertTrue("Must contain auto", languages.containsKey("auto"));
        assertTrue("Must contain bn", languages.containsKey("bn"));
        assertTrue("Must contain en", languages.containsKey("en"));
        assertEquals("Bengali", TranslationHelper.getLanguageName("bn"));
        assertEquals("English", TranslationHelper.getLanguageName("en"));
    }

    @Test
    public void testTranslateActionMetadata() {
        assertNotNull(TranslateActionKt.getTranslateAction());
        assertEquals(R.drawable.nth_translate, TranslateActionKt.getTranslateAction().getIcon());
        assertEquals(R.string.translate_action_title, TranslateActionKt.getTranslateAction().getName());
        assertTrue(TranslateActionKt.getTranslateAction().getCanShowKeyboard());
    }

    @Test
    public void testTranslateActionSourceCodeConstraints() throws Exception {
        File sourceFile = new File("java/src/org/futo/inputmethod/latin/uix/actions/TranslateAction.kt");
        assertTrue("TranslateAction.kt must exist", sourceFile.exists());

        String content = new String(Files.readAllBytes(sourceFile.toPath()), StandardCharsets.UTF_8);

        // 1. Must NOT contain fillMaxHeight() on DropdownMenu
        assertFalse("DropdownMenu must not have fillMaxHeight (should wrap content height)",
                content.contains("DropdownMenu(\n            modifier = Modifier\n                .fillMaxWidth()\n                .fillMaxHeight()"));

        // 2. Must contain Bauhaus / Nothing OS NeuCard 16.dp rounded corner styling and clipping
        assertTrue("Must use RoundedCornerShape(16.dp)", content.contains("RoundedCornerShape(16.dp)"));
        assertTrue("Must clip to RoundedCornerShape(16.dp)", content.contains(".clip(RoundedCornerShape(16.dp))"));
        assertTrue("Must have 1.dp hairline border", content.contains("1.dp,"));

        // 3. Must restrict source to exactly 3 items: auto, bn, en
        assertTrue("Source pinned codes must be auto, bn, en", content.contains("listOf(\"auto\", \"bn\", \"en\")"));
        assertTrue("Target pinned codes must be bn, en", content.contains("listOf(\"bn\", \"en\")"));

        // 4. Must not have the old expandable 'More languages' list
        assertFalse("Must not have More languages clutter", content.contains("More languages"));

        // 5. Must use PopupProperties(focusable = false) so DropdownMenu doesn't steal IME focus
        assertTrue("Must specify PopupProperties(focusable = false)", content.contains("PopupProperties(focusable = false)"));

        // 6. onDismissRequest must not call onLanguageSelected() to prevent IME teardown
        assertFalse("onDismissRequest must not trigger onLanguageSelected()",
                content.contains("onDismissRequest = {\n                    mExpanded.value = false\n                    onLanguageSelected()\n                }"));
    }

    @Test
    public void testTranslationHelperNameResolution() {
        assertEquals("Auto Detect", TranslationHelper.getLanguageName("auto"));
        assertEquals("Bengali", TranslationHelper.getLanguageName("bn"));
        assertEquals("English", TranslationHelper.getLanguageName("en"));
        // Unmapped code must gracefully fallback to the code itself without throwing
        assertEquals("xyz_custom", TranslationHelper.getLanguageName("xyz_custom"));
        assertEquals("", TranslationHelper.getLanguageName(""));
    }

    @Test
    public void testLanguageCodeLookup() {
        assertEquals("bn", TranslationHelper.getLanguageCode("Bengali"));
        assertEquals("en", TranslationHelper.getLanguageCode("English"));
        assertEquals("auto", TranslationHelper.getLanguageCode("Auto Detect"));
        // Case-insensitivity check
        assertEquals("bn", TranslationHelper.getLanguageCode("bengali"));
        assertEquals("en", TranslationHelper.getLanguageCode("english"));
    }

    @Test
    public void testPinnedLanguagesIntegrity() {
        Map<String, String> languages = TranslationHelper.getLanguages();
        List<String> sourcePinned = List.of("auto", "bn", "en");
        List<String> targetPinned = List.of("bn", "en");

        assertEquals(3, sourcePinned.size());
        assertEquals(2, targetPinned.size());

        for (String code : sourcePinned) {
            assertTrue("Source pinned code must exist in TranslationHelper: " + code, languages.containsKey(code));
        }

        for (String code : targetPinned) {
            assertTrue("Target pinned code must exist in TranslationHelper: " + code, languages.containsKey(code));
            assertFalse("Target language must never allow 'auto'", "auto".equals(code));
        }
    }
}

