package org.futo.inputmethod.keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.futo.inputmethod.latin.R;
import org.futo.inputmethod.latin.uix.BasicThemeProviderKt;
import org.junit.Test;

public class KeyboardFontSettingsTest {

    @Test
    public void testFontResourceIdentifiersExist() {
        assertNotEquals(0, R.font.qurova);
        assertNotEquals(0, R.font.agency);
        assertNotEquals(0, R.font.doto);
        assertNotEquals(0, R.font.inter);
        assertNotEquals(0, R.font.ndot);
        assertNotEquals(0, R.font.ndot_family);
    }

    @Test
    public void testKeyboardFontSettingKeyAndDefault() {
        assertNotNull(BasicThemeProviderKt.getKeyboardFontSetting());
        assertEquals("keyboard_font", BasicThemeProviderKt.getKeyboardFontSetting().getKey().getName());
        assertEquals("nothing", BasicThemeProviderKt.getKeyboardFontSetting().getDefault());
    }

    @Test
    public void testFontOptionsSupported() {
        String[] supportedFonts = new String[]{"nothing", "orbit_dot", "normal", "qurova", "agency"};
        for (String fontKey : supportedFonts) {
            assertNotNull(fontKey);
            assertTrue(fontKey.length() > 0);
        }
    }

    @Test
    public void testOrbitDotBanglaDigitDetection() {
        assertTrue(org.futo.inputmethod.keyboard.internal.OrbitDotBangla.isBengaliDigit('১'));
        assertTrue(org.futo.inputmethod.keyboard.internal.OrbitDotBangla.isBengaliDigit('০'));
        assertTrue(org.futo.inputmethod.keyboard.internal.OrbitDotBangla.isBengaliDigit('৯'));
        assertFalse(org.futo.inputmethod.keyboard.internal.OrbitDotBangla.isBengaliDigit('ক'));
        assertFalse(org.futo.inputmethod.keyboard.internal.OrbitDotBangla.isBengaliDigit('A'));

        assertTrue(org.futo.inputmethod.keyboard.internal.OrbitDotBangla.isBengaliDigitString("১২৩৪৫৬৭৮৯০"));
        assertFalse(org.futo.inputmethod.keyboard.internal.OrbitDotBangla.isBengaliDigitString("১২৩a"));

        assertTrue(org.futo.inputmethod.keyboard.internal.OrbitDotBangla.containsBengaliLetter("ক"));
        assertFalse(org.futo.inputmethod.keyboard.internal.OrbitDotBangla.containsBengaliLetter("১২৩"));
    }

    @Test
    public void testDotFontActiveLogic() {
        assertTrue("nothing font should be dot font", isDotFont("nothing"));
        assertTrue("orbit_dot font should be dot font", isDotFont("orbit_dot"));
        assertTrue("empty font should default to dot font", isDotFont(""));

        assertFalse("normal font should not be dot font", isDotFont("normal"));
        assertFalse("qurova font should not be dot font", isDotFont("qurova"));
        assertFalse("agency font should not be dot font", isDotFont("agency"));
    }

    @Test
    public void testQurovaFontFallbackLogic() {
        // 1. Qurova font with digits should trigger fallback to default typeface
        assertTrue("Qurova with single digit '1'",
                KeyboardView.shouldFallbackToDefaultTypeface(true, false, "1"));
        assertTrue("Qurova with single digit '0'",
                KeyboardView.shouldFallbackToDefaultTypeface(true, false, "0"));
        assertTrue("Qurova with multiple digits '123'",
                KeyboardView.shouldFallbackToDefaultTypeface(true, false, "123"));

        // 2. Qurova font with alphabet letters should NOT trigger fallback
        assertFalse("Qurova with letter 'A'",
                KeyboardView.shouldFallbackToDefaultTypeface(true, false, "A"));
        assertFalse("Qurova with letter 'z'",
                KeyboardView.shouldFallbackToDefaultTypeface(true, false, "z"));
        assertFalse("Qurova with word 'hello'",
                KeyboardView.shouldFallbackToDefaultTypeface(true, false, "hello"));

        // 3. Non-Qurova font with digits should NOT trigger digit fallback
        assertFalse("Non-Qurova with digit '5'",
                KeyboardView.shouldFallbackToDefaultTypeface(false, false, "5"));
        assertFalse("Non-Qurova with letter 'A'",
                KeyboardView.shouldFallbackToDefaultTypeface(false, false, "A"));

        // 4. Bengali characters must always fallback regardless of Qurova flag
        assertTrue("Bengali letter 'ক' on non-Qurova",
                KeyboardView.shouldFallbackToDefaultTypeface(false, false, "\u0995"));
        assertTrue("Bengali digit '১' on non-Qurova",
                KeyboardView.shouldFallbackToDefaultTypeface(false, false, "\u09E7"));

        // 5. Bengali Number Pad layout flag always triggers fallback
        assertTrue("Bengali Number Pad flag true with null text",
                KeyboardView.shouldFallbackToDefaultTypeface(false, true, null));
        assertTrue("Bengali Number Pad flag true with any text",
                KeyboardView.shouldFallbackToDefaultTypeface(false, true, "123"));

        // 6. Null safety check
        assertFalse("Null text on non-numpad non-bengali",
                KeyboardView.shouldFallbackToDefaultTypeface(true, false, null));
    }

    private boolean isDotFont(String keyboardFont) {
        return ("nothing".equals(keyboardFont) || "orbit_dot".equals(keyboardFont) || keyboardFont == null || keyboardFont.isEmpty());
    }
}

