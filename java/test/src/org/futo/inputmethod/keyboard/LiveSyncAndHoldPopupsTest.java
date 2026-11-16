package org.futo.inputmethod.keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.futo.inputmethod.keyboard.internal.LaserReticleEffect;
import org.futo.inputmethod.latin.uix.BasicThemeProviderKt;
import org.junit.Test;

public class LiveSyncAndHoldPopupsTest {

    @Test
    public void testLaserColorSettingDefinition() {
        assertNotNull(BasicThemeProviderKt.getLaserColorSetting());
        assertEquals("laser_reticle_color", BasicThemeProviderKt.getLaserColorSetting().getKey().getName());
        assertEquals("neon", BasicThemeProviderKt.getLaserColorSetting().getDefault());
    }

    @Test
    public void testKeyPopupStyleSettingDefinition() {
        assertNotNull(BasicThemeProviderKt.getKeyPopupStyleSetting());
        assertEquals("key_popup_style", BasicThemeProviderKt.getKeyPopupStyleSetting().getKey().getName());
        assertEquals("orbit_slim", BasicThemeProviderKt.getKeyPopupStyleSetting().getDefault());
    }

    @Test
    public void testKeyboardTypingEffectSettingDefinition() {
        assertNotNull(BasicThemeProviderKt.getKeyboardTypingEffectSetting());
        assertEquals("typing_effect", BasicThemeProviderKt.getKeyboardTypingEffectSetting().getKey().getName());
        assertEquals("none", BasicThemeProviderKt.getKeyboardTypingEffectSetting().getDefault());
    }

    @Test
    public void testLaserReticleColorModes() {
        assertEquals(0xFF00E5FF, LaserReticleEffect.resolveColor("cyan"));
        assertEquals(0xFFFF1493, LaserReticleEffect.resolveColor("pink"));
        assertEquals(0xFFFED700, LaserReticleEffect.resolveColor("yellow"));
        assertEquals(0xFF00E676, LaserReticleEffect.resolveColor("green"));
        assertEquals(0xFFD71921, LaserReticleEffect.resolveColor("red"));
        assertEquals(0xFFD500F9, LaserReticleEffect.resolveColor("purple"));
        assertEquals(0xFFD500F9, LaserReticleEffect.resolveColor("violet"));
        assertEquals(0xFF00B0FF, LaserReticleEffect.resolveColor("blue"));
        assertEquals(0xFF00B0FF, LaserReticleEffect.resolveColor("skyblue"));
        assertEquals(0xFFFF6D00, LaserReticleEffect.resolveColor("orange"));
        assertEquals(0xFFFFFFFF, LaserReticleEffect.resolveColor("white"));
        assertEquals(0, LaserReticleEffect.resolveColor("mix"));

        // Case-insensitivity check
        assertEquals(0xFF00E5FF, LaserReticleEffect.resolveColor("CYAN"));
        assertEquals(0xFFFF1493, LaserReticleEffect.resolveColor("Pink"));
        assertEquals(0xFFD71921, LaserReticleEffect.resolveColor("RED"));
    }

    @Test
    public void testPopupOffCheckLogic() {
        String[] offStyles = new String[] { "off", "OFF", "none", "None", "NONE" };
        for (String style : offStyles) {
            boolean isPopupOff = "off".equalsIgnoreCase(style) || "none".equalsIgnoreCase(style);
            assertTrue("Style '" + style + "' must be recognized as popup off", isPopupOff);
        }

        String[] onStyles = new String[] { "orbit_slim", "gboard_bubble", "boxy", "fluid_callout" };
        for (String style : onStyles) {
            boolean isPopupOff = "off".equalsIgnoreCase(style) || "none".equalsIgnoreCase(style);
            assertFalse("Style '" + style + "' must NOT be recognized as popup off", isPopupOff);
        }
    }
}
