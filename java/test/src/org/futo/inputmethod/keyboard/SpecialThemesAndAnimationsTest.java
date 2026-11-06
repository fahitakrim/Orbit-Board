package org.futo.inputmethod.keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.futo.inputmethod.keyboard.internal.KeyboardTapAnimationManager;
import org.futo.inputmethod.keyboard.internal.TapAnimationType;
import org.futo.inputmethod.latin.R;
import org.futo.inputmethod.latin.uix.BasicThemeProviderKt;
import org.futo.inputmethod.latin.uix.theme.ThemeOption;
import org.futo.inputmethod.latin.uix.theme.ThemeOptionsKt;
import org.junit.Test;

public class SpecialThemesAndAnimationsTest {

    @Test
    public void testDecoupledEffectsNotInThemeOptions() {
        assertNull("NothingRadarWave must not be a standalone theme", ThemeOptionsKt.getThemeOptions().get("NothingRadarWave"));
        assertNull("MicroGlyphSpark must not be a standalone theme", ThemeOptionsKt.getThemeOptions().get("MicroGlyphSpark"));
        assertFalse(ThemeOptionsKt.getThemeOptionKeys().contains("NothingRadarWave"));
        assertFalse(ThemeOptionsKt.getThemeOptionKeys().contains("MicroGlyphSpark"));
    }

    @Test
    public void testKeyboardTypingEffectSettingKeyAndDefault() {
        assertNotNull(BasicThemeProviderKt.getKeyboardTypingEffectSetting());
        assertEquals("typing_effect", BasicThemeProviderKt.getKeyboardTypingEffectSetting().getKey().getName());
        assertEquals("none", BasicThemeProviderKt.getKeyboardTypingEffectSetting().getDefault());
    }

    @Test
    public void testBakeryThemeCompletelyRemoved() {
        ThemeOption bakery = ThemeOptionsKt.getThemeOptions().get("BakeryTheme");
        assertNull("BakeryTheme must be completely removed from ThemeOptions", bakery);
        assertFalse(ThemeOptionsKt.getThemeOptionKeys().contains("BakeryTheme"));
    }

    @Test
    public void testCustomAccentColorSettingKeyAndDefault() {
        assertNotNull(BasicThemeProviderKt.getCustomAccentColorSetting());
        assertEquals("custom_accent_color", BasicThemeProviderKt.getCustomAccentColorSetting().getKey().getName());
        assertEquals("default", BasicThemeProviderKt.getCustomAccentColorSetting().getDefault());
    }

    @Test
    public void testKeyBordersSettingDefault() {
        assertNotNull(BasicThemeProviderKt.getKeyBordersSetting());
        assertEquals("keyBorders", BasicThemeProviderKt.getKeyBordersSetting().getKey().getName());
        assertTrue(BasicThemeProviderKt.getKeyBordersSetting().getDefault());
    }

    @Test
    public void testTapAnimationTypeEnum() {
        assertEquals(4, TapAnimationType.values().length);
        assertEquals(TapAnimationType.NONE, TapAnimationType.valueOf("NONE"));
        assertEquals(TapAnimationType.RADAR_WAVE, TapAnimationType.valueOf("RADAR_WAVE"));
        assertEquals(TapAnimationType.PARTICLE_BURST, TapAnimationType.valueOf("PARTICLE_BURST"));
        assertEquals(TapAnimationType.CONSTELLATION_WEB, TapAnimationType.valueOf("CONSTELLATION_WEB"));
    }

    @Test
    public void testTapAnimationManagerCreationAndLifecycle() {
        KeyboardTapAnimationManager manager = new KeyboardTapAnimationManager();
        assertFalse(manager.hasActiveEffects());

        // NONE should not create effect
        manager.spawnAnimation(TapAnimationType.NONE, 100f, 100f);
        assertFalse(manager.hasActiveEffects());

        // RADAR_WAVE should create effect
        manager.spawnAnimation(TapAnimationType.RADAR_WAVE, 100f, 100f);
        assertTrue(manager.hasActiveEffects());

        manager.clear();
        assertFalse(manager.hasActiveEffects());

        // PARTICLE_BURST should create effect
        manager.spawnAnimation(TapAnimationType.PARTICLE_BURST, 150f, 150f);
        assertTrue(manager.hasActiveEffects());

        manager.clear();
        assertFalse(manager.hasActiveEffects());

        // CONSTELLATION_WEB should create effect
        manager.spawnAnimation(TapAnimationType.CONSTELLATION_WEB, 120f, 120f);
        assertTrue(manager.hasActiveEffects());

        manager.clear();
        assertFalse(manager.hasActiveEffects());
    }

    @Test
    public void testTapAnimationManagerRapidTypingLimit() {
        KeyboardTapAnimationManager manager = new KeyboardTapAnimationManager();

        // Simulate 25 rapid key presses
        for (int i = 0; i < 25; i++) {
            TapAnimationType type = (i % 2 == 0) ? TapAnimationType.RADAR_WAVE : TapAnimationType.PARTICLE_BURST;
            manager.spawnAnimation(type, 10f * i, 20f * i);
            assertTrue(manager.hasActiveEffects());
        }

        manager.clear();
        assertFalse(manager.hasActiveEffects());
    }

    @Test
    public void testTapEffectsClockSkewHandling() {
        long now = android.os.SystemClock.uptimeMillis();
        org.futo.inputmethod.keyboard.internal.RadarPulseEffect radar =
                new org.futo.inputmethod.keyboard.internal.RadarPulseEffect(100f, 100f, now);
        android.graphics.Canvas dummyCanvas = new android.graphics.Canvas();

        // 1. Clock skew test: time slightly before startTimeMs (e.g. now - 10ms)
        boolean aliveBeforeStart = radar.updateAndDraw(dummyCanvas, now - 10);
        assertTrue("RadarPulseEffect must survive clock jitter before start time", aliveBeforeStart);

        // 2. Normal playback test
        boolean aliveMid = radar.updateAndDraw(dummyCanvas, now + 200);
        assertTrue("RadarPulseEffect must be alive midway", aliveMid);

        // 3. Natural expiration test
        boolean deadAfterEnd = radar.updateAndDraw(dummyCanvas, now + 600);
        assertFalse("RadarPulseEffect must expire after duration", deadAfterEnd);

        // ParticleBurst clock skew test
        org.futo.inputmethod.keyboard.internal.ParticleBurstEffect burst =
                new org.futo.inputmethod.keyboard.internal.ParticleBurstEffect(100f, 100f, now);
        boolean burstAliveBefore = burst.updateAndDraw(dummyCanvas, now - 10);
        assertTrue("ParticleBurstEffect must survive clock jitter before start time", burstAliveBefore);

        // ConstellationWeb clock skew and lifecycle test
        org.futo.inputmethod.keyboard.internal.ConstellationWebEffect constellation =
                new org.futo.inputmethod.keyboard.internal.ConstellationWebEffect(100f, 100f, now);
        boolean constAliveBefore = constellation.updateAndDraw(dummyCanvas, now - 10);
        assertTrue("ConstellationWebEffect must survive clock jitter before start time", constAliveBefore);
        boolean constAliveMid = constellation.updateAndDraw(dummyCanvas, now + 250);
        assertTrue("ConstellationWebEffect must be alive midway", constAliveMid);
        boolean constDeadAfter = constellation.updateAndDraw(dummyCanvas, now + 650);
        assertFalse("ConstellationWebEffect must expire after duration", constDeadAfter);
    }

    @Test
    public void testKeyboardTapAnimationManagerMultiThreadedConcurrency() throws Exception {
        final KeyboardTapAnimationManager manager = new KeyboardTapAnimationManager();
        final int threadCount = 4;
        final int iterationsPerThread = 50;
        Thread[] threads = new Thread[threadCount];

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            threads[t] = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < iterationsPerThread; i++) {
                        TapAnimationType type = (threadId % 2 == 0) ? TapAnimationType.RADAR_WAVE : TapAnimationType.PARTICLE_BURST;
                        manager.spawnAnimation(type, 10f * i, 15f * i);
                        manager.hasActiveEffects();
                    }
                }
            });
            threads[t].start();
        }

        android.graphics.Canvas dummyCanvas = new android.graphics.Canvas();
        for (int i = 0; i < 50; i++) {
            manager.draw(dummyCanvas);
            manager.hasActiveEffects();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        manager.clear();
        assertFalse(manager.hasActiveEffects());
    }

    @Test
    public void testParticleBurstTunedDurationAndLuminance() {
        long now = android.os.SystemClock.uptimeMillis();
        org.futo.inputmethod.keyboard.internal.ParticleBurstEffect burst =
                new org.futo.inputmethod.keyboard.internal.ParticleBurstEffect(200f, 200f, now);
        android.graphics.Canvas dummyCanvas = new android.graphics.Canvas();

        // 1. Alive at start
        assertTrue("Burst must be alive at start", burst.updateAndDraw(dummyCanvas, now));
        // 2. Midway playback (at 500ms should still be active because life is 850-1150ms)
        assertTrue("Burst must still be alive at 500ms due to slower duration", burst.updateAndDraw(dummyCanvas, now + 500));
        // 3. Expiration after 1200ms
        assertFalse("Burst must naturally expire after 1200ms", burst.updateAndDraw(dummyCanvas, now + 1200));
    }

    @Test
    public void testRadarPulseExpandedRadiusAndTiming() {
        long now = android.os.SystemClock.uptimeMillis();
        org.futo.inputmethod.keyboard.internal.RadarPulseEffect radar =
                new org.futo.inputmethod.keyboard.internal.RadarPulseEffect(200f, 200f, now);
        android.graphics.Canvas dummyCanvas = new android.graphics.Canvas();

        assertTrue("Radar must be active at start", radar.updateAndDraw(dummyCanvas, now));
        assertTrue("Radar must still be active midway at 300ms", radar.updateAndDraw(dummyCanvas, now + 300));
        assertFalse("Radar must expire after 560ms (test at 600ms)", radar.updateAndDraw(dummyCanvas, now + 600));
    }
}
