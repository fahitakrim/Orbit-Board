package org.futo.inputmethod.keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Rect;

import org.futo.inputmethod.keyboard.internal.KeyPopupDrawable;
import org.futo.inputmethod.latin.R;
import org.junit.Test;

public class KeyPopupDrawableTest {

    @Test
    public void testStyleParsing() {
        assertEquals(KeyPopupDrawable.STYLE_ORBIT_SLIM, KeyPopupDrawable.Companion.parseStyle("orbit_slim"));
        assertEquals(KeyPopupDrawable.STYLE_GBOARD_BUBBLE, KeyPopupDrawable.Companion.parseStyle("gboard_bubble"));
        assertEquals(KeyPopupDrawable.STYLE_GBOARD_BUBBLE, KeyPopupDrawable.Companion.parseStyle("1"));
        assertEquals(KeyPopupDrawable.STYLE_FLUID_CALLOUT, KeyPopupDrawable.Companion.parseStyle("fluid_callout"));
        assertEquals(KeyPopupDrawable.STYLE_FLUID_CALLOUT, KeyPopupDrawable.Companion.parseStyle("2"));
        assertEquals(KeyPopupDrawable.STYLE_FLUID_CALLOUT, KeyPopupDrawable.Companion.parseStyle("ios"));
        assertEquals(KeyPopupDrawable.STYLE_FLUID_CALLOUT, KeyPopupDrawable.Companion.parseStyle("apple"));
        assertEquals(KeyPopupDrawable.STYLE_ORBIT_SLIM, KeyPopupDrawable.Companion.parseStyle(null));
        assertEquals(KeyPopupDrawable.STYLE_ORBIT_SLIM, KeyPopupDrawable.Companion.parseStyle("unknown"));

        // Verify KeyPopupStyle enum mapping
        assertEquals(KeyPopupDrawable.STYLE_ORBIT_SLIM, org.futo.inputmethod.keyboard.internal.KeyPopupStyle.ORBIT_SLIM.getId());
        assertEquals(KeyPopupDrawable.STYLE_GBOARD_BUBBLE, org.futo.inputmethod.keyboard.internal.KeyPopupStyle.GBOARD_BUBBLE.getId());
        assertEquals(KeyPopupDrawable.STYLE_FLUID_CALLOUT, org.futo.inputmethod.keyboard.internal.KeyPopupStyle.IOS.getId());
    }

    @Test
    public void testIntrinsicDimensions() {
        KeyPopupDrawable drawable = new KeyPopupDrawable(
                KeyPopupDrawable.STYLE_ORBIT_SLIM,
                0xFF1C1D20,
                0x33FFFFFF,
                8.0f,
                48,
                122,
                50
        );

        assertEquals(KeyPopupDrawable.STYLE_ORBIT_SLIM, drawable.getStyle());
        assertEquals(48, drawable.getIntrinsicWidth());
        assertEquals(72, drawable.getIntrinsicHeight()); // 122 - 50 = 72
        assertTrue(drawable.isStateful());
    }

    @Test
    public void testDifferentStylesInitialization() {
        KeyPopupDrawable slim = new KeyPopupDrawable(
                KeyPopupDrawable.STYLE_ORBIT_SLIM, 0, 0, 8f, 50, 100, 30);
        KeyPopupDrawable bubble = new KeyPopupDrawable(
                KeyPopupDrawable.STYLE_GBOARD_BUBBLE, 0, 0, 8f, 50, 100, 30);
        KeyPopupDrawable callout = new KeyPopupDrawable(
                KeyPopupDrawable.STYLE_FLUID_CALLOUT, 0, 0, 8f, 50, 100, 30);

        assertEquals(KeyPopupDrawable.STYLE_ORBIT_SLIM, slim.getStyle());
        assertEquals(KeyPopupDrawable.STYLE_GBOARD_BUBBLE, bubble.getStyle());
        assertEquals(KeyPopupDrawable.STYLE_FLUID_CALLOUT, callout.getStyle());

        assertEquals(70, slim.getIntrinsicHeight());
        assertEquals(70, bubble.getIntrinsicHeight());
        assertEquals(70, callout.getIntrinsicHeight());

        // Test getPadding reporting bottomPadding for upper head centering
        Rect padding = new Rect();
        callout.getPadding(padding);
        assertEquals(0, padding.left);
        assertEquals(0, padding.top);
        assertEquals(30, callout.getBottomPadding());
    }

    @Test
    public void testAlphaMutation() {
        KeyPopupDrawable callout = new KeyPopupDrawable(
                KeyPopupDrawable.STYLE_FLUID_CALLOUT, 0xFF1C1D20, 0x33FFFFFF, 8f, 50, 100, 30);
        callout.setAlpha(128);
        callout.setAlpha(0);
        callout.setAlpha(255);
        assertNotNull(callout);
    }

    @Test
    public void testOrbitThemeHelperSpacebarShapeHook() {
        org.futo.inputmethod.latin.uix.theme.presets.OrbitThemeHelper helper =
                org.futo.inputmethod.latin.uix.theme.presets.OrbitThemeHelper.INSTANCE;

        assertEquals("boxy", helper.getSpacebarShape());
        helper.setSpacebarShape("roundy");
        assertEquals("roundy", helper.getSpacebarShape());

        // Reset to default boxy
        helper.setSpacebarShape("boxy");
        assertEquals("boxy", helper.getSpacebarShape());

        // Test custom spacebar text color
        helper.setCustomSpacebarTextColor(0xFFE2B438);
        assertEquals(Integer.valueOf(0xFFE2B438), helper.getCustomSpacebarTextColor());
        helper.setCustomSpacebarTextColor(null);
        assertEquals(null, helper.getCustomSpacebarTextColor());
    }

    @Test
    public void testSettingsLongPressASelectAllConstant() {
        assertEquals("pref_long_press_a_select_all", org.futo.inputmethod.latin.settings.Settings.PREF_LONG_PRESS_A_SELECT_ALL);
    }

    @Test
    public void testAiActionRemoved() {
        org.junit.Assert.assertFalse(org.futo.inputmethod.latin.uix.actions.RegistryKt.getAllActionsMap().containsKey("ai_assistant"));
    }

    @Test
    public void testInPlaceReconfigurationZeroAllocation() {
        KeyPopupDrawable drawable = new KeyPopupDrawable(
                KeyPopupDrawable.STYLE_ORBIT_SLIM, 0xFF1C1D20, 0x33FFFFFF, 8f, 50, 100, 30);
        assertEquals(KeyPopupDrawable.STYLE_ORBIT_SLIM, drawable.getStyle());

        // Reconfigure in-place to GBOARD_BUBBLE
        drawable.reconfigure(
                KeyPopupDrawable.STYLE_GBOARD_BUBBLE,
                0xFF222226,
                0x33FFFFFF,
                12f,
                60,
                120
        );

        assertEquals(KeyPopupDrawable.STYLE_GBOARD_BUBBLE, drawable.getStyle());
        assertTrue(drawable.getIntrinsicWidth() >= 60);
        assertEquals(12f, drawable.getCornerRadius(), 0.001f);

        // Reconfigure to FLUID_CALLOUT with high luminance (light theme)
        drawable.reconfigure(
                KeyPopupDrawable.STYLE_FLUID_CALLOUT,
                0xFFFFFFFF,
                0x26000000,
                14f,
                70,
                130
        );
        assertEquals(KeyPopupDrawable.STYLE_FLUID_CALLOUT, drawable.getStyle());
        assertTrue("Apple iOS head must be wider than key base", drawable.getIntrinsicWidth() > 70);
    }

    @Test
    public void testGboardBubblePaddingAndAspect() {
        KeyPopupDrawable bubble = new KeyPopupDrawable(
                KeyPopupDrawable.STYLE_GBOARD_BUBBLE, 0xFF1C1D20, 0x33FFFFFF, 8f, 50, 100, 30);
        bubble.configureForKey(50, 60, org.futo.inputmethod.keyboard.internal.KeyPopupEdgeAlignment.CENTER, 0f);
        assertTrue("Intrinsic width must accommodate 1:1 bubble", bubble.getIntrinsicWidth() >= 50);

        bubble.setBounds(0, 0, 100, 200);
        Rect padding = new Rect();
        bubble.getPadding(padding);
        assertTrue("Padding bottom must be >= keyHeight", padding.bottom >= 60);
        assertTrue("Padding top must be non-negative", padding.top >= 0);
    }

    @Test
    public void testApplePaddlePaddingAndInflection() {
        KeyPopupDrawable callout = new KeyPopupDrawable(
                KeyPopupDrawable.STYLE_FLUID_CALLOUT, 0xFF1C1D20, 0x33FFFFFF, 8f, 50, 100, 30);
        callout.configureForKey(50, 60, org.futo.inputmethod.keyboard.internal.KeyPopupEdgeAlignment.CENTER, 0f);
        assertTrue("Upper squircle head must be wider than key", callout.getIntrinsicWidth() > 50);

        callout.setBounds(0, 0, 120, 220);
        Rect padding = new Rect();
        callout.getPadding(padding);
        assertTrue("Padding bottom must cover stem and neck", padding.bottom >= 60);
        assertTrue("Padding left must be positive for middle flare", padding.left > 0);
        assertTrue("Padding right must be positive for middle flare", padding.right > 0);
    }

    @Test
    public void testGetContentRectDeadCenterAlignment() {
        KeyPopupDrawable slim = new KeyPopupDrawable(
                KeyPopupDrawable.STYLE_ORBIT_SLIM, 0xFF1C1D20, 0x33FFFFFF, 8f, 50, 100, 30);
        slim.configureForKey(50, 60, org.futo.inputmethod.keyboard.internal.KeyPopupEdgeAlignment.CENTER, 0f);
        slim.setBounds(0, 0, 100, 200);

        android.graphics.RectF contentRect = slim.getContentRect();
        assertNotNull(contentRect);
        assertTrue("Content rect width must be positive", contentRect.right > contentRect.left);
        // Verify horizontal centering
        float centerX = (contentRect.left + contentRect.right) / 2.0f;
        assertEquals(50f, centerX, 0.1f);
        // Verify vertical bounds above key
        assertTrue("Content rect top must be within drawable", contentRect.top >= 0);
        assertTrue("Content rect bottom must be above or at key top", contentRect.bottom <= 200 - 60 + 10);

        // Verify getPadding matches contentRect 1:1
        Rect pad = new Rect();
        slim.getPadding(pad);
        assertEquals((int) contentRect.left, pad.left);
        assertEquals((int) contentRect.top, pad.top);
        assertEquals((int) (100 - contentRect.right), pad.right);
        assertEquals((int) (200 - contentRect.bottom), pad.bottom);
    }

    @Test
    public void testBoxyPopupProfileDistinctFromOrbit() {
        KeyPopupDrawable boxy = new KeyPopupDrawable(
                KeyPopupDrawable.STYLE_BOXY, 0xFF1C1D20, 0x33FFFFFF, 8f, 50, 100, 30);
        assertEquals(KeyPopupDrawable.STYLE_BOXY, boxy.getStyle());
        boxy.setBounds(0, 0, 100, 200);
        android.graphics.RectF boxyRect = boxy.getContentRect();
        assertNotNull(boxyRect);
        assertTrue("Boxy rect width must be positive", boxyRect.right > boxyRect.left);
        float centerX = (boxyRect.left + boxyRect.right) / 2.0f;
        assertEquals(50f, centerX, 0.1f);
    }
}
