package org.futo.inputmethod.keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.futo.inputmethod.keyboard.internal.KeyboardIconsSet;
import org.futo.inputmethod.keyboard.internal.MoreKeySpec;
import org.futo.inputmethod.latin.common.Constants;
import org.junit.Test;

import java.util.Locale;

public class KeyHintAndSanityTest {

    private Key createBaseKey(String label, int code) {
        return new Key(label, null, code, null, null, 0, 0, 0, 0, 100, 100, 0, 0);
    }

    @Test
    public void testExclamationMarkKeyHintNotTreatedAsIcon() {
        Key baseKey = createBaseKey("h", 'h');
        MoreKeySpec[] moreKeys = new MoreKeySpec[] {
            new MoreKeySpec("!", false, Locale.US)
        };
        Key key = new Key(baseKey, moreKeys);

        // Exclamation mark hint must be treated as a text label, NOT an icon
        assertEquals("Exclamation mark must be parsed as hint label", "!", key.getHintLabel());
        assertNull("Exclamation mark must not produce an icon ID", key.getHintIconId());
    }

    @Test
    public void testBengaliGraphemeMultiCharKeyHint() {
        Key baseKey = createBaseKey("\u09AF", 0x09AF); // য
        MoreKeySpec[] moreKeys = new MoreKeySpec[] {
            new MoreKeySpec("\u09CD\u09AF", false, new Locale("bn", "BD")) // ্য (2 UTF-16 code units)
        };
        Key key = new Key(baseKey, moreKeys);

        assertEquals("Multi-char Bengali grapheme conjunct must be preserved as hint label",
                "\u09CD\u09AF", key.getHintLabel());
    }

    @Test
    public void testIconSpecKeyHintTreatedAsIcon() {
        Key baseKey = createBaseKey("s", 's');
        MoreKeySpec[] moreKeys = new MoreKeySpec[] {
            new MoreKeySpec(KeyboardIconsSet.PREFIX_ICON + "settings_key|!code/key_settings", false, Locale.US)
        };
        Key key = new Key(baseKey, moreKeys);

        assertNull("Icon spec must not produce a text hint label", key.getHintLabel());
        assertEquals("Icon spec must be resolved to correct icon ID",
                "settings_key", key.getHintIconId());
    }

    @Test
    public void testNullAndEmptyMoreKeysDoNotCrash() {
        Key baseKey = createBaseKey("a", 'a');
        Key keyWithNullMoreKeys = new Key(baseKey, null);
        assertNull(keyWithNullMoreKeys.getHintLabel());

        Key keyWithEmptyMoreKeys = new Key(baseKey, new MoreKeySpec[0]);
        assertNull(keyWithEmptyMoreKeys.getHintLabel());
    }

    @Test
    public void testStandardBackspaceKeyCodeSanity() {
        // Sanity check standard AOSP/LatinIME backspace definition
        assertEquals("Standard backspace key code must be Constants.CODE_DELETE (-5)",
                Constants.CODE_DELETE, -5);
        assertEquals("Standard delete key icon name",
                "delete_key", KeyboardIconsSet.NAME_DELETE_KEY);

        Key backspaceKey = createBaseKey(null, Constants.CODE_DELETE);
        assertEquals("Backspace key code must match Constants.CODE_DELETE",
                Constants.CODE_DELETE, backspaceKey.getCode());
        assertTrue("Backspace key must be enabled", backspaceKey.isEnabled());
    }

    @Test
    public void testAKeyNoHintAndMKeySlashHint() {
        // Key 'a' with moreKeys "!noPanelAutoMoreKey!,!text/morekeys_a" must not show '@' hint
        Key baseKeyA = createBaseKey("a", 'a');
        MoreKeySpec[] moreKeysA = new MoreKeySpec[] {
            new MoreKeySpec("!noPanelAutoMoreKey!", false, Locale.US),
            new MoreKeySpec("à", false, Locale.US)
        };
        Key keyA = new Key(baseKeyA, moreKeysA);
        assertNull("Key 'a' must not have '@' or other hint label when !noPanelAutoMoreKey! is first", keyA.getHintLabel());

        // Key 'm' with moreKeys "/,?,!noPanelAutoMoreKey!,!text/morekeys_m" must show '/' as hint
        Key baseKeyM = createBaseKey("m", 'm');
        MoreKeySpec[] moreKeysM = new MoreKeySpec[] {
            new MoreKeySpec("/", false, Locale.US),
            new MoreKeySpec("?", false, Locale.US)
        };
        Key keyM = new Key(baseKeyM, moreKeysM);
        assertEquals("Key 'm' must show '/' as hint", "/", keyM.getHintLabel());
    }
}
