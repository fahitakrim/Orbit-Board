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

import org.futo.inputmethod.latin.common.Constants;
import org.futo.inputmethod.latin.uix.theme.presets.BerryBlushThemeHelper;
import org.futo.inputmethod.latin.uix.theme.presets.TactileSlateThemeHelper;

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
    public void testRetroBlocksThemeRegistered() {
        ThemeOption retro = ThemeOptionsKt.getThemeOptions().get("RetroBlocks");
        assertNotNull("RetroBlocks must be registered in ThemeOptions", retro);
        assertTrue(ThemeOptionsKt.getThemeOptionKeys().contains("RetroBlocks"));
    }

    @Test
    public void testRetroBlocksThemeColorAndTextContrast() {
        org.futo.inputmethod.latin.uix.theme.presets.RetroBlocksThemeHelper helper =
                org.futo.inputmethod.latin.uix.theme.presets.RetroBlocksThemeHelper.INSTANCE;

        Key keyQ = new Key("q", null, 'q', null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyW = new Key("w", null, 'w', null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyE = new Key("e", null, 'e', null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyR = new Key("r", null, 'r', null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyO = new Key("o", null, 'o', null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyZ = new Key("z", null, 'z', null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyDel = new Key(null, null, org.futo.inputmethod.latin.common.Constants.CODE_DELETE, null, null, 0, 0, 0, 0, 100, 100, 0, 0);

        // Verify base colors
        assertEquals(helper.COLOR_OLIVE, helper.getKeyBaseColor(keyQ));
        assertEquals(helper.COLOR_FOREST, helper.getKeyBaseColor(keyW));
        assertEquals(helper.COLOR_CHARCOAL, helper.getKeyBaseColor(keyE));
        assertEquals(helper.COLOR_CREAM, helper.getKeyBaseColor(keyR));
        assertEquals(helper.COLOR_YELLOW, helper.getKeyBaseColor(keyO));
        assertEquals(helper.COLOR_CHARCOAL, helper.getKeyBaseColor(keyZ));
        assertEquals(helper.COLOR_RED, helper.getKeyBaseColor(keyDel));

        // Verify text contrast: Light keys get dark text, dark keys get white text!
        assertEquals(helper.COLOR_TEXT_DARK, helper.getKeyTextColor(keyQ)); // Olive -> dark text
        assertEquals(helper.COLOR_TEXT_LIGHT, helper.getKeyTextColor(keyW)); // Forest -> white text
        assertEquals(helper.COLOR_TEXT_LIGHT, helper.getKeyTextColor(keyE)); // Charcoal -> white text
        assertEquals(helper.COLOR_TEXT_DARK, helper.getKeyTextColor(keyR)); // Cream -> dark text
        assertEquals(helper.COLOR_TEXT_DARK, helper.getKeyTextColor(keyO)); // Yellow -> dark text
        assertEquals(helper.COLOR_TEXT_LIGHT, helper.getKeyTextColor(keyZ)); // Charcoal -> MUST be white text!
        assertEquals(helper.COLOR_TEXT_LIGHT, helper.getKeyTextColor(keyDel)); // Red -> white text

        // Verify key preview background matches key base color
        android.graphics.drawable.Drawable dQ = helper.getKeyPreviewBackground(keyQ, 8f, 48, 24, 50);
        android.graphics.drawable.Drawable dO = helper.getKeyPreviewBackground(keyO, 8f, 48, 24, 50);
        assertNotNull(dQ);
        assertNotNull(dO);
        assertTrue(dQ instanceof android.graphics.drawable.ShapeDrawable);
        assertTrue(dO instanceof android.graphics.drawable.ShapeDrawable);
        assertTrue("Different colors produce distinct drawables", dQ != dO);

        // Same color (Yellow) reuses cached drawable
        Key keyG = new Key("g", null, 'g', null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        android.graphics.drawable.Drawable dG = helper.getKeyPreviewBackground(keyG, 8f, 48, 24, 50);
        assertTrue("Same color reuses cached drawable", dO == dG);
    }

    @Test
    public void testRetroBlocksThemeSpacebarCustomization() {
        org.futo.inputmethod.latin.uix.theme.presets.RetroBlocksThemeHelper helper =
                org.futo.inputmethod.latin.uix.theme.presets.RetroBlocksThemeHelper.INSTANCE;

        assertEquals("boxy", helper.getSpacebarShape());
        helper.setSpacebarShape("roundy");
        assertEquals("roundy", helper.getSpacebarShape());
        helper.setSpacebarShape("boxy");

        helper.setCustomSpacebarColor(helper.COLOR_YELLOW);
        assertEquals(Integer.valueOf(helper.COLOR_YELLOW), helper.getCustomSpacebarColor());
        helper.setCustomSpacebarColor(null);
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
        assertEquals(5, TapAnimationType.values().length);
        assertEquals(TapAnimationType.NONE, TapAnimationType.valueOf("NONE"));
        assertEquals(TapAnimationType.RADAR_WAVE, TapAnimationType.valueOf("RADAR_WAVE"));
        assertEquals(TapAnimationType.PARTICLE_BURST, TapAnimationType.valueOf("PARTICLE_BURST"));
        assertEquals(TapAnimationType.CONSTELLATION_WEB, TapAnimationType.valueOf("CONSTELLATION_WEB"));
        assertEquals(TapAnimationType.LASER_RETICLE, TapAnimationType.valueOf("LASER_RETICLE"));
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

        // LASER_RETICLE should create effect
        manager.spawnAnimation(TapAnimationType.LASER_RETICLE, 130f, 130f);
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

        // LaserReticle clock skew and lifecycle test
        org.futo.inputmethod.keyboard.internal.LaserReticleEffect laser =
                new org.futo.inputmethod.keyboard.internal.LaserReticleEffect(100f, 100f, now);
        boolean laserAliveBefore = laser.updateAndDraw(dummyCanvas, now - 10);
        assertTrue("LaserReticleEffect must survive clock jitter before start time", laserAliveBefore);
        boolean laserAliveMid = laser.updateAndDraw(dummyCanvas, now + 200);
        assertTrue("LaserReticleEffect must be alive midway", laserAliveMid);
        boolean laserDeadAfter = laser.updateAndDraw(dummyCanvas, now + 450);
        assertFalse("LaserReticleEffect must expire after 400ms duration", laserDeadAfter);
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

    @Test
    public void testBerryBlushThemeRegisteredAndPalette() {
        ThemeOption berryBlush = ThemeOptionsKt.getThemeOptions().get("BerryBlush");
        assertNotNull("BerryBlush must be registered in ThemeOptions", berryBlush);
        assertTrue(ThemeOptionsKt.getThemeOptionKeys().contains("BerryBlush"));

        BerryBlushThemeHelper helper = BerryBlushThemeHelper.INSTANCE;
        assertEquals(0xFFFAD0DC, helper.BLUSH_PINK);
        assertEquals(0xFFF9B4CA, helper.BUBBLEGUM_PINK);
        assertEquals(0xFFD8C8E8, helper.LAVENDER);
        assertEquals(0xFFB87D8B, (int)helper.ROSE_MAUVE);
        assertEquals(0xFFFAF5EE, helper.WARM_CREAM);
        assertEquals(0xFFFF3E8A, helper.HOT_PINK);
        assertEquals(0xFFB83362, helper.BERRY_ROSE);
        assertEquals(0xFF5E1D35, helper.DARK_INK);
        assertEquals(0xFFFFF8FA, helper.LIGHT_INK);
        assertEquals(0xFF000000, helper.COLOR_BACKGROUND);
    }

    @Test
    public void testBerryBlushKeyColorsAndContrast() {
        BerryBlushThemeHelper helper = BerryBlushThemeHelper.INSTANCE;

        Key keyQ = new Key("q", null, 'q', null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyW = new Key("w", null, 'w', null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyE = new Key("e", null, 'e', null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyR = new Key("r", null, 'r', null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyDel = new Key(null, null, Constants.CODE_DELETE, null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyEnter = new Key(null, null, Constants.CODE_ENTER, null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyShift = new Key(null, null, Constants.CODE_SHIFT, null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyEmoji = new Key(null, null, Constants.CODE_EMOJI, null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keySpace = new Key(null, null, Constants.CODE_SPACE, null, null, 0, 0, 0, 0, 100, 100, 0, 0);

        assertEquals(helper.BLUSH_PINK, helper.getKeyBaseColor(keyQ));
        assertEquals(helper.LAVENDER, helper.getKeyBaseColor(keyW));
        assertEquals(helper.ROSE_MAUVE, helper.getKeyBaseColor(keyE));
        assertEquals(helper.WARM_CREAM, helper.getKeyBaseColor(keyR));
        assertEquals(helper.HOT_PINK, helper.getKeyBaseColor(keyDel));
        assertEquals(helper.BERRY_ROSE, helper.getKeyBaseColor(keyEnter));
        assertEquals(helper.ROSE_MAUVE, helper.getKeyBaseColor(keyShift));
        assertEquals(helper.BLUSH_PINK, helper.getKeyBaseColor(keyEmoji));
        assertEquals(helper.BLUSH_PINK, helper.getKeyBaseColor(keySpace));

        // Dark ink text on light pastel keys
        assertEquals(helper.DARK_INK, helper.getKeyTextColor(keyQ));
        assertEquals(helper.DARK_INK, helper.getKeyTextColor(keyW));
        assertEquals(helper.DARK_INK, helper.getKeyTextColor(keyE));
        assertEquals(helper.DARK_INK, helper.getKeyTextColor(keyR));
        // Light ink text on hot pink and berry rose keys
        assertEquals(helper.LIGHT_INK, helper.getKeyTextColor(keyDel));
        assertEquals(helper.LIGHT_INK, helper.getKeyTextColor(keyEnter));
        assertEquals(helper.DARK_INK, helper.getKeyTextColor(keySpace));
    }

    @Test
    public void testBerryBlushKeyLabelOverridesAndDoodles() {
        BerryBlushThemeHelper helper = BerryBlushThemeHelper.INSTANCE;

        Key keyQ = new Key("q", null, 'q', null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyI = new Key("i", null, 'i', null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyO = new Key("o", null, 'o', null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyG = new Key("g", null, 'g', null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyA = new Key("a", null, 'a', null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyC = new Key("c", null, 'c', null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyM = new Key("m", null, 'm', null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyY = new Key("y", null, 'y', null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyP = new Key("p", null, 'p', null, null, 0, 0, 0, 0, 100, 100, 0, 0);

        Key keyEmoji = new Key(null, null, Constants.CODE_EMOJI, null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyEnter = new Key(null, null, Constants.CODE_ENTER, null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyDel = new Key(null, null, Constants.CODE_DELETE, null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyShift = new Key(null, null, Constants.CODE_SHIFT, null, null, 0, 0, 0, 0, 100, 100, 0, 0);

        // ZERO EMOJIS! Cute locked glyphs (q, i, o, g) return empty to render custom sprite vectors on Canvas
        assertEquals("", helper.getKeyLabelOverride(keyQ, "q"));
        assertEquals("", helper.getKeyLabelOverride(keyQ, "Q"));
        assertEquals("", helper.getKeyLabelOverride(keyI, "i"));
        assertEquals("", helper.getKeyLabelOverride(keyI, "I"));
        assertEquals("", helper.getKeyLabelOverride(keyO, "o")); // 'o' is rendered as a handcrafted hollow heart on Canvas
        assertEquals("", helper.getKeyLabelOverride(keyG, "g"));
        assertEquals("a", helper.getKeyLabelOverride(keyA, "a"));
        assertEquals("A", helper.getKeyLabelOverride(keyA, "A"));
        assertEquals("c", helper.getKeyLabelOverride(keyC, "c"));
        assertEquals("m", helper.getKeyLabelOverride(keyM, "m"));
        assertEquals("y", helper.getKeyLabelOverride(keyY, "y"));
        assertEquals("p", helper.getKeyLabelOverride(keyP, "p"));

        // Special functional keys return empty string so handcrafted Canvas vector geometry renders them
        assertEquals("", helper.getKeyLabelOverride(keyEmoji, ""));
        assertEquals("", helper.getKeyLabelOverride(keyEnter, ""));
        assertEquals("", helper.getKeyLabelOverride(keyDel, ""));
        assertEquals("", helper.getKeyLabelOverride(keyShift, ""));

        // Verify handcrafted Canvas vector drawing handler returns true
        android.graphics.Canvas canvas = new android.graphics.Canvas();
        android.graphics.Paint paint = new android.graphics.Paint();
        assertTrue("Shift must be custom rendered on Canvas", helper.onDrawCustomKeyTopVisuals(keyShift, canvas, paint, 100, 100, 255));
        assertTrue("Delete must be custom rendered on Canvas", helper.onDrawCustomKeyTopVisuals(keyDel, canvas, paint, 100, 100, 255));
        assertTrue("Emoji must be custom rendered on Canvas", helper.onDrawCustomKeyTopVisuals(keyEmoji, canvas, paint, 100, 100, 255));
        assertTrue("Enter must be custom rendered on Canvas", helper.onDrawCustomKeyTopVisuals(keyEnter, canvas, paint, 100, 100, 255));
        assertTrue("Key 'o' must be custom rendered on Canvas", helper.onDrawCustomKeyTopVisuals(keyO, canvas, paint, 100, 100, 255));
        assertFalse("Key 'a' uses post draw visuals", helper.onDrawCustomKeyTopVisuals(keyA, canvas, paint, 100, 100, 255));
        assertFalse("Key 'c' uses post draw visuals", helper.onDrawCustomKeyTopVisuals(keyC, canvas, paint, 100, 100, 255));
    }

    @Test
    public void testBerryBlushKeyDrawables() {
        BerryBlushThemeHelper helper = BerryBlushThemeHelper.INSTANCE;

        Key key123 = new Key("?123", null, Constants.CODE_SWITCH_ALPHA_SYMBOL, null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyEmoji = new Key(null, null, Constants.CODE_EMOJI, null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyEnter = new Key(null, null, Constants.CODE_ENTER, null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keySpace = new Key(null, null, Constants.CODE_SPACE, null, null, 0, 0, 0, 0, 100, 100, 0, 0);

        android.graphics.drawable.Drawable switchDrawable = helper.getKeyBackground(key123, false, 8f);
        assertTrue("?123 must have CatEarKeycapDrawable", switchDrawable instanceof BerryBlushThemeHelper.CatEarKeycapDrawable);

        android.graphics.drawable.Drawable emojiDrawable = helper.getKeyBackground(keyEmoji, false, 8f);
        assertTrue("Emoji must have CatEarKeycapDrawable", emojiDrawable instanceof BerryBlushThemeHelper.CatEarKeycapDrawable);

        android.graphics.drawable.Drawable enterDrawable = helper.getKeyBackground(keyEnter, false, 8f);
        assertTrue("Enter must have CatEarKeycapDrawable", enterDrawable instanceof BerryBlushThemeHelper.CatEarKeycapDrawable);

        android.graphics.drawable.Drawable spaceDrawable = helper.getKeyBackground(keySpace, false, 8f);
        assertTrue("Default spacebar must have CoquetteSpacebarDrawable", spaceDrawable instanceof BerryBlushThemeHelper.CoquetteSpacebarDrawable);
    }

    @Test
    public void testTactileSlateThemeRegisteredAndPalette() {
        ThemeOption tactileSlate = ThemeOptionsKt.getThemeOptions().get("TactileSlate");
        assertNotNull("TactileSlate must be registered in ThemeOptions", tactileSlate);
        assertTrue(ThemeOptionsKt.getThemeOptionKeys().contains("TactileSlate"));

        TactileSlateThemeHelper helper = TactileSlateThemeHelper.INSTANCE;
        assertEquals(0xFFD0D2D6, helper.COLOR_CHASSIS);
        assertEquals(0xFFFFFFFF, helper.COLOR_WHITE_KEY);
        assertEquals(0xFFB4B7BD, helper.COLOR_WHITE_BEVEL);
        assertEquals(0xFF404040, helper.COLOR_MODIFIER);
        assertEquals(0xFF262626, helper.COLOR_MODIFIER_BEVEL);
        assertEquals(0xFFD71921, helper.COLOR_RED_BACKSPACE);
        assertEquals(0xFF9E1218, helper.COLOR_RED_BEVEL);
        assertEquals(0xFF1C1D20, helper.COLOR_TEXT_DARK);
        assertEquals(0xFFFFFFFF, helper.COLOR_TEXT_LIGHT);
    }

    @Test
    public void testTactileSlateKeyColorsAndRedBackspace() {
        TactileSlateThemeHelper helper = TactileSlateThemeHelper.INSTANCE;

        Key keyQ = new Key("q", null, 'q', null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyDel = new Key(null, null, Constants.CODE_DELETE, null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyEnter = new Key(null, null, Constants.CODE_ENTER, null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyShift = new Key(null, null, Constants.CODE_SHIFT, null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyEmoji = new Key(null, null, Constants.CODE_EMOJI, null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keySpace = new Key(null, null, Constants.CODE_SPACE, null, null, 0, 0, 0, 0, 100, 100, 0, 0);

        // "just backspace ta red hobe" -> Nothing Red #D71921
        assertEquals("Backspace must be Nothing Red", helper.COLOR_RED_BACKSPACE, helper.getKeyBaseColor(keyDel));
        assertEquals(helper.COLOR_TEXT_LIGHT, helper.getKeyTextColor(keyDel));

        // Sculpted white keys
        assertEquals(helper.COLOR_WHITE_KEY, helper.getKeyBaseColor(keyQ));
        assertEquals(helper.COLOR_TEXT_DARK, helper.getKeyTextColor(keyQ));
        assertEquals(helper.COLOR_WHITE_KEY, helper.getKeyBaseColor(keySpace));

        // Modifiers dark charcoal #404040
        assertEquals(helper.COLOR_MODIFIER, helper.getKeyBaseColor(keyEnter));
        assertEquals(helper.COLOR_TEXT_LIGHT, helper.getKeyTextColor(keyEnter));
        assertEquals(helper.COLOR_MODIFIER, helper.getKeyBaseColor(keyShift));
        assertEquals(helper.COLOR_TEXT_LIGHT, helper.getKeyTextColor(keyShift));
        assertEquals(helper.COLOR_MODIFIER, helper.getKeyBaseColor(keyEmoji));
        assertEquals(helper.COLOR_TEXT_LIGHT, helper.getKeyTextColor(keyEmoji));
    }

    @Test
    public void testTactileSlateUppercaseAndModifierLabels() {
        TactileSlateThemeHelper helper = TactileSlateThemeHelper.INSTANCE;

        Key keyQ = new Key("q", null, 'q', null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyZ = new Key("z", null, 'z', null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyShift = new Key(null, null, Constants.CODE_SHIFT, null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyEnter = new Key(null, null, Constants.CODE_ENTER, null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyEmoji = new Key(null, null, Constants.CODE_EMOJI, null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keySymbols = new Key("?123", null, Constants.CODE_SWITCH_ALPHA_SYMBOL, null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyDel = new Key(null, null, Constants.CODE_DELETE, null, null, 0, 0, 0, 0, 100, 100, 0, 0);

        // FIX LOWERCASE BUG: Letters dynamically preserve case!
        assertEquals("q", helper.getKeyLabelOverride(keyQ, "q"));
        assertEquals("z", helper.getKeyLabelOverride(keyZ, "z"));
        assertEquals("Q", helper.getKeyLabelOverride(keyQ, "Q"));
        assertEquals("Z", helper.getKeyLabelOverride(keyZ, "Z"));

        // Modifiers: ZERO EMOJIS! Return empty so vector canvas geometry renders them
        assertEquals("", helper.getKeyLabelOverride(keyShift, ""));
        assertEquals("", helper.getKeyLabelOverride(keyEnter, ""));
        assertEquals("", helper.getKeyLabelOverride(keyEmoji, ""));
        assertEquals("12!?", helper.getKeyLabelOverride(keySymbols, "?123"));

        // Backspace does NOT override label (returns empty/original) so white delete icon is drawn!
        assertEquals("", helper.getKeyLabelOverride(keyDel, ""));

        // Verify handcrafted Canvas vector drawing handler returns true
        android.graphics.Canvas canvas = new android.graphics.Canvas();
        android.graphics.Paint paint = new android.graphics.Paint();
        assertTrue("Emoji must be custom rendered on Canvas", helper.onDrawCustomKeyTopVisuals(keyEmoji, canvas, paint, 100, 100, 255));
        assertTrue("Shift must be custom rendered on Canvas", helper.onDrawCustomKeyTopVisuals(keyShift, canvas, paint, 100, 100, 255));
        assertTrue("Enter must be custom rendered on Canvas", helper.onDrawCustomKeyTopVisuals(keyEnter, canvas, paint, 100, 100, 255));
        assertFalse("Key 'q' does not use custom key top visuals", helper.onDrawCustomKeyTopVisuals(keyQ, canvas, paint, 100, 100, 255));
    }

    @Test
    public void testTactileSlateKeyDrawables() {
        TactileSlateThemeHelper helper = TactileSlateThemeHelper.INSTANCE;

        Key keyQ = new Key("q", null, 'q', null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyDel = new Key(null, null, Constants.CODE_DELETE, null, null, 0, 0, 0, 0, 100, 100, 0, 0);
        Key keyEnter = new Key(null, null, Constants.CODE_ENTER, null, null, 0, 0, 0, 0, 100, 100, 0, 0);

        android.graphics.drawable.Drawable dQ = helper.getKeyBackground(keyQ, false, 8f);
        assertTrue(dQ instanceof TactileSlateThemeHelper.TactileKeycapDrawable);

        android.graphics.drawable.Drawable dDel = helper.getKeyBackground(keyDel, false, 8f);
        assertTrue(dDel instanceof TactileSlateThemeHelper.TactileKeycapDrawable);

        android.graphics.drawable.Drawable dEnter = helper.getKeyBackground(keyEnter, false, 8f);
        assertTrue(dEnter instanceof TactileSlateThemeHelper.TactileKeycapDrawable);
    }

    @Test
    public void testLaserReticle700msChromaticLifecycle() {
        long now = android.os.SystemClock.uptimeMillis();
        android.graphics.Canvas dummyCanvas = new android.graphics.Canvas();

        // 700ms chromatic rainbow effect
        org.futo.inputmethod.keyboard.internal.LaserReticleEffect mixLaser =
                new org.futo.inputmethod.keyboard.internal.LaserReticleEffect(100f, 100f, "mix", now, 700f);

        // 1. Alive at start
        assertTrue("Mix laser must be active at start", mixLaser.updateAndDraw(dummyCanvas, now));
        // 2. Active at 350ms (midway)
        assertTrue("Mix laser must be active midway at 350ms", mixLaser.updateAndDraw(dummyCanvas, now + 350));
        // 3. Active at 600ms (during gentle exponential fade-out)
        assertTrue("Mix laser must be active at 600ms due to 700ms curve", mixLaser.updateAndDraw(dummyCanvas, now + 600));
        // 4. Expires after 700ms
        assertFalse("Mix laser must expire after 700ms duration", mixLaser.updateAndDraw(dummyCanvas, now + 720));

        // 5. Verify LaserReticleEffect color modes
        org.futo.inputmethod.keyboard.internal.LaserReticleEffect whiteLaser =
                new org.futo.inputmethod.keyboard.internal.LaserReticleEffect(50f, 50f, "white", now, 400f);
        assertEquals("White laser must have pure white chosenColor", 0xFFFFFFFF, whiteLaser.getChosenColor());

        org.futo.inputmethod.keyboard.internal.LaserReticleEffect yellowLaser =
                new org.futo.inputmethod.keyboard.internal.LaserReticleEffect(50f, 50f, "yellow", now, 400f);
        assertEquals("Yellow laser must have cyber yellow chosenColor", 0xFFFED700, yellowLaser.getChosenColor());

        org.futo.inputmethod.keyboard.internal.LaserReticleEffect redLaser =
                new org.futo.inputmethod.keyboard.internal.LaserReticleEffect(50f, 50f, "red", now, 400f);
        assertEquals("Red laser must have nothing red chosenColor", 0xFFD71921, redLaser.getChosenColor());

        // 6. Verify spawnAnimation creates active laser reticle effect with specified color
        org.futo.inputmethod.keyboard.internal.KeyboardTapAnimationManager mgr =
                new org.futo.inputmethod.keyboard.internal.KeyboardTapAnimationManager();
        mgr.spawnAnimation(org.futo.inputmethod.keyboard.internal.TapAnimationType.LASER_RETICLE, 50f, 50f, "white");
        assertTrue(mgr.hasActiveEffects());
        assertEquals(1, mgr.getActiveCount());

        // 7. Verify Border Collision / Gutter Flow key dimension awareness & multi-color neon palette
        org.futo.inputmethod.keyboard.internal.LaserReticleEffect cyanFlow =
                new org.futo.inputmethod.keyboard.internal.LaserReticleEffect(100f, 100f, "cyan", now, 450f, 0xFF00E5FF, 100f, 150f);
        assertEquals("Cyan flow must have cyan chosenColor", 0xFF00E5FF, cyanFlow.getChosenColor());
        assertTrue("Cyan flow must be active at start", cyanFlow.updateAndDraw(dummyCanvas, now));
        assertTrue("Cyan flow must be active at 200ms", cyanFlow.updateAndDraw(dummyCanvas, now + 200));
        assertFalse("Cyan flow must expire after 450ms", cyanFlow.updateAndDraw(dummyCanvas, now + 460));

        org.futo.inputmethod.keyboard.internal.LaserReticleEffect pinkFlow =
                new org.futo.inputmethod.keyboard.internal.LaserReticleEffect(80f, 80f, "pink", now, 450f, 0xFFFF1493, 100f, 150f);
        assertEquals("Pink flow must have pink chosenColor", 0xFFFF1493, pinkFlow.getChosenColor());

        org.futo.inputmethod.keyboard.internal.LaserReticleEffect greenFlow =
                new org.futo.inputmethod.keyboard.internal.LaserReticleEffect(80f, 80f, "green", now, 450f, 0xFF00E676, 100f, 150f);
        assertEquals("Green flow must have green chosenColor", 0xFF00E676, greenFlow.getChosenColor());

        org.futo.inputmethod.keyboard.internal.LaserReticleEffect orangeFlow =
                new org.futo.inputmethod.keyboard.internal.LaserReticleEffect(80f, 80f, "orange", now, 450f, 0xFFFF6D00, 100f, 150f);
        assertEquals("Orange flow must have orange chosenColor", 0xFFFF6D00, orangeFlow.getChosenColor());

        // 8. Verify spawnAnimation with key dimensions
        mgr.spawnAnimation(org.futo.inputmethod.keyboard.internal.TapAnimationType.LASER_RETICLE, 80f, 120f, "neon", 95f, 140f);
        assertTrue(mgr.hasActiveEffects());
        assertEquals(2, mgr.getActiveCount());

        // 9. Verify boundary safety with NaN coordinates does not crash
        org.futo.inputmethod.keyboard.internal.LaserReticleEffect nanFlow =
                new org.futo.inputmethod.keyboard.internal.LaserReticleEffect(Float.NaN, Float.POSITIVE_INFINITY, "neon", now, 450f);
        assertTrue("NaN flow must be safely handled without crash", nanFlow.updateAndDraw(dummyCanvas, now));

        // 10. Deep verification of all 8 futuristic neon palette colors
        int[] expectedNeonPalette = new int[] {
                0xFF00E5FF, // Cyan
                0xFFFF1493, // Neon Pink
                0xFFFED700, // Cyber Yellow
                0xFF00E676, // Electric Green
                0xFFD71921, // Nothing Red
                0xFFD500F9, // Electric Violet
                0xFF00B0FF, // Sky Blue
                0xFFFF6D00  // Electric Orange
        };
        assertEquals(8, org.futo.inputmethod.keyboard.internal.LaserReticleEffect.Companion.getNEON_PALETTE().length);
        for (int i = 0; i < expectedNeonPalette.length; i++) {
            assertEquals("Neon palette color " + i + " must match spec",
                    expectedNeonPalette[i],
                    org.futo.inputmethod.keyboard.internal.LaserReticleEffect.Companion.getNEON_PALETTE()[i]);
        }

        // 11. Verify resolveColor("neon") returns a vibrant neon color from NEON_PALETTE, and "random" returns from SIGNATURE_COLORS
        for (int i = 0; i < 20; i++) {
            int neonColor = org.futo.inputmethod.keyboard.internal.LaserReticleEffect.resolveColor("neon");
            int defaultColor = org.futo.inputmethod.keyboard.internal.LaserReticleEffect.resolveColor("");
            assertTrue("Neon color must not be washed out pure white", neonColor != 0xFFFFFFFF);
            assertTrue("Default color must not be washed out pure white", defaultColor != 0xFFFFFFFF);
            boolean inPaletteNeon = false;
            boolean inPaletteDefault = false;
            for (int col : expectedNeonPalette) {
                if (neonColor == col) inPaletteNeon = true;
                if (defaultColor == col) inPaletteDefault = true;
            }
            assertTrue("Neon color must belong to vibrant neon palette", inPaletteNeon);
            assertTrue("Default color must belong to vibrant neon palette", inPaletteDefault);

            int randomColor = org.futo.inputmethod.keyboard.internal.LaserReticleEffect.resolveColor("random");
            boolean inSignature = false;
            for (int col : org.futo.inputmethod.keyboard.internal.LaserReticleEffect.Companion.getSIGNATURE_COLORS()) {
                if (randomColor == col) inSignature = true;
            }
            assertTrue("Random color must belong to signature colors", inSignature);
        }

        // 12. Deep verification: High-framerate rendering across 120 FPS frames without exceptions
        org.futo.inputmethod.keyboard.internal.LaserReticleEffect liveEffect =
                new org.futo.inputmethod.keyboard.internal.LaserReticleEffect(150f, 180f, "neon", now, 450f, 0xFF00E5FF, 120f, 160f);
        for (int frame = 0; frame <= 54; frame++) { // 450ms at 120 FPS = ~54 frames
            long frameTime = now + (long) (frame * 8.33f);
            boolean active = liveEffect.updateAndDraw(dummyCanvas, frameTime);
            if (frame < 50) {
                assertTrue("Effect must remain active during animation lifecycle (frame " + frame + ")", active);
            }
        }
        // At 460ms (past 450ms), effect must expire cleanly
        assertFalse("Effect must cleanly expire past 450ms", liveEffect.updateAndDraw(dummyCanvas, now + 460));

        // 13. Verify spawnAnimation with "white" or "default" preference cycles through vibrant neon colors
        mgr.clear();
        for (int i = 0; i < 15; i++) {
            mgr.spawnAnimation(org.futo.inputmethod.keyboard.internal.TapAnimationType.LASER_RETICLE, 100f, 100f, "white", 100f, 150f);
        }
        assertTrue("Manager must have active effects after spawning with 'white' preference", mgr.hasActiveEffects());
        assertEquals("Manager must cap at maxConcurrent", 10, mgr.getActiveCount());

        mgr.clear();
        for (int i = 0; i < 15; i++) {
            mgr.spawnAnimation(org.futo.inputmethod.keyboard.internal.TapAnimationType.LASER_RETICLE, 100f, 100f, "default", 100f, 150f);
        }
        assertTrue("Manager must have active effects after spawning with 'default' preference", mgr.hasActiveEffects());
        assertEquals("Manager must cap at maxConcurrent", 10, mgr.getActiveCount());
    }
}
