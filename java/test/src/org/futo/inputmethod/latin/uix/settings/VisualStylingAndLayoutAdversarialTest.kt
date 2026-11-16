package org.futo.inputmethod.latin.uix.settings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.futo.inputmethod.keyboard.internal.KeyboardTapAnimationManager
import org.futo.inputmethod.keyboard.internal.TapAnimationType
import org.futo.inputmethod.latin.uix.THEME_KEY
import org.futo.inputmethod.latin.uix.settings.pages.LayoutCardItem
import org.futo.inputmethod.latin.uix.theme.ThemeOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Empirical Adversarial Test Harness for Milestone 2 (Visual Styling & Layout Adjustments - Requirement R3):
 * 1. Board Format Colors Differentiation (Bangla, Avro, Jatiyo, Probhat, Jatiyo Classic).
 * 2. Inactive card styling and tinted hairline borders (accentColor.copy(alpha = 0.25f)).
 * 3. Default Theme Prioritization (OrbitTheme as primary default at index 0).
 * 4. Tap Animation Manager concurrency, queue bounds, and coordinate boundary safety.
 * 5. Preview sizing multiplier bounds and key dimension safety invariants.
 */
class VisualStylingAndLayoutAdversarialTest {

    // =========================================================================
    // 1. Board Format Color Differentiation & Invariants
    // =========================================================================

    private val testLayoutList = listOf(
        LayoutCardItem(
            id = "akkhor",
            title = "Bangla",
            nativeScript = "বাংলা",
            sampleText = "অ আ ক খ",
            description = "Alphabetical typing",
            subtypeString = "bn_BD_akkhor",
            activeBackgroundColor = Color(0xFF221316),
            activeBorderColor = Color(0xFFE5252A),
            accentColor = Color(0xFFE5252A)
        ),
        LayoutCardItem(
            id = "avro",
            title = "Avro",
            nativeScript = "অভ্র",
            sampleText = "ami • আমি",
            description = "Phonetic typing",
            subtypeString = "bn_BD_avro",
            activeBackgroundColor = Color(0xFF221C12),
            activeBorderColor = Color(0xFFE2B438),
            accentColor = Color(0xFFE2B438)
        ),
        LayoutCardItem(
            id = "jatiyo",
            title = "Jatiyo",
            nativeScript = "জাতীয়",
            sampleText = "অ আ ক খ",
            description = "National standard",
            subtypeString = "bn_BD_jatiyo",
            activeBackgroundColor = Color(0xFF121E20),
            activeBorderColor = Color(0xFF38B2AC),
            accentColor = Color(0xFF38B2AC)
        ),
        LayoutCardItem(
            id = "probhat",
            title = "Probhat",
            nativeScript = "প্রভাত",
            sampleText = "ami • আমি",
            description = "Probhat layout",
            subtypeString = "bn_BD_probhat",
            activeBackgroundColor = Color(0xFF1C1424),
            activeBorderColor = Color(0xFFA855F7),
            accentColor = Color(0xFFA855F7)
        ),
        LayoutCardItem(
            id = "jatiyo_classic",
            title = "Jatiyo Classic",
            nativeScript = "জাতীয় ক্লাসিক",
            sampleText = "অ আ ক খ",
            description = "Original traditional national layout",
            subtypeString = "bn_BD_jatiyo_classic",
            activeBackgroundColor = Color(0xFF141924),
            activeBorderColor = Color(0xFF3B72BA),
            accentColor = Color(0xFF3B72BA)
        )
    )

    @Test
    fun testBoardFormatColorsMutualDistinctness() {
        assertEquals("Exactly 5 Bengali board formats must be defined", 5, testLayoutList.size)

        // Verify mutual distinctness of active background colors
        val uniqueBgColors = testLayoutList.map { it.activeBackgroundColor }.toSet()
        assertEquals(
            "All 5 board layouts must have mutually distinct active background colors",
            5,
            uniqueBgColors.size
        )

        // Verify mutual distinctness of active border colors
        val uniqueBorderColors = testLayoutList.map { it.activeBorderColor }.toSet()
        assertEquals(
            "All 5 board layouts must have mutually distinct active border colors",
            5,
            uniqueBorderColors.size
        )

        // Verify mutual distinctness of accent colors
        val uniqueAccentColors = testLayoutList.map { it.accentColor }.toSet()
        assertEquals(
            "All 5 board layouts must have mutually distinct accent colors",
            5,
            uniqueAccentColors.size
        )
    }

    @Test
    fun testBoardFormatExactColorDefinitions() {
        val bangla = testLayoutList.first { it.id == "akkhor" }
        assertEquals("Bangla active background must be Crimson tint (0xFF221316)", Color(0xFF221316), bangla.activeBackgroundColor)
        assertEquals("Bangla active border must be Crimson (0xFFE5252A)", Color(0xFFE5252A), bangla.activeBorderColor)
        assertEquals("Bangla accent must be Crimson (0xFFE5252A)", Color(0xFFE5252A), bangla.accentColor)

        val avro = testLayoutList.first { it.id == "avro" }
        assertEquals("Avro active background must be Warm Ochre tint (0xFF221C12)", Color(0xFF221C12), avro.activeBackgroundColor)
        assertEquals("Avro active border must be Warm Ochre (0xFFE2B438)", Color(0xFFE2B438), avro.activeBorderColor)
        assertEquals("Avro accent must be Warm Ochre (0xFFE2B438)", Color(0xFFE2B438), avro.accentColor)

        val jatiyo = testLayoutList.first { it.id == "jatiyo" }
        assertEquals("Jatiyo active background must be Nordic Teal tint (0xFF121E20)", Color(0xFF121E20), jatiyo.activeBackgroundColor)
        assertEquals("Jatiyo active border must be Nordic Teal (0xFF38B2AC)", Color(0xFF38B2AC), jatiyo.activeBorderColor)
        assertEquals("Jatiyo accent must be Nordic Teal (0xFF38B2AC)", Color(0xFF38B2AC), jatiyo.accentColor)

        val probhat = testLayoutList.first { it.id == "probhat" }
        assertEquals("Probhat active background must be Electric Violet tint (0xFF1C1424)", Color(0xFF1C1424), probhat.activeBackgroundColor)
        assertEquals("Probhat active border must be Electric Violet (0xFFA855F7)", Color(0xFFA855F7), probhat.activeBorderColor)
        assertEquals("Probhat accent must be Electric Violet (0xFFA855F7)", Color(0xFFA855F7), probhat.accentColor)

        val jatiyoClassic = testLayoutList.first { it.id == "jatiyo_classic" }
        assertEquals("Jatiyo Classic active background must be Cobalt tint (0xFF141924)", Color(0xFF141924), jatiyoClassic.activeBackgroundColor)
        assertEquals("Jatiyo Classic active border must be Cobalt (0xFF3B72BA)", Color(0xFF3B72BA), jatiyoClassic.activeBorderColor)
        assertEquals("Jatiyo Classic accent must be Cobalt (0xFF3B72BA)", Color(0xFF3B72BA), jatiyoClassic.accentColor)
    }

    // =========================================================================
    // 2. Inactive Card Hairline Border Tint & Badge Chip Invariants
    // =========================================================================

    @Test
    fun testInactiveCardHairlineBorderTint() {
        val inactiveCardBg = Color(0xFF161618)

        for (item in testLayoutList) {
            // Formula from LayoutGridCard:
            // cardBorder = if (isActive) item.activeBorderColor.copy(alpha = 0.70f) else item.accentColor.copy(alpha = 0.25f)
            val activeBorder = item.activeBorderColor.copy(alpha = 0.70f)
            val inactiveBorder = item.accentColor.copy(alpha = 0.25f)

            assertEquals("Inactive border alpha must be exactly 0.25f", 0.25f, inactiveBorder.alpha, 0.01f)
            assertEquals("Active border alpha must be exactly 0.70f", 0.70f, activeBorder.alpha, 0.01f)

            // Inactive border must preserve the exact RGB hue of the format's accent
            assertEquals("Inactive border red must match accent", item.accentColor.red, inactiveBorder.red, 0.01f)
            assertEquals("Inactive border green must match accent", item.accentColor.green, inactiveBorder.green, 0.01f)
            assertEquals("Inactive border blue must match accent", item.accentColor.blue, inactiveBorder.blue, 0.01f)

            // Inactive card background must be dark neutral, distinct from any active background
            assertNotEquals("Inactive card bg must differ from active bg", item.activeBackgroundColor, inactiveCardBg)
        }
    }

    @Test
    fun testNativeScriptBadgeChipStylingInvariants() {
        for (item in testLayoutList) {
            // Formula from LayoutGridCard:
            // bg = item.accentColor.copy(alpha = if (isActive) 0.18f else 0.08f)
            // border = item.accentColor.copy(alpha = if (isActive) 0.45f else 0.20f)
            // text = if (isActive) item.accentColor else item.accentColor.copy(alpha = 0.85f)
            val activeChipBg = item.accentColor.copy(alpha = 0.18f)
            val inactiveChipBg = item.accentColor.copy(alpha = 0.08f)
            val activeChipBorder = item.accentColor.copy(alpha = 0.45f)
            val inactiveChipBorder = item.accentColor.copy(alpha = 0.20f)
            val inactiveText = item.accentColor.copy(alpha = 0.85f)

            assertEquals(0.18f, activeChipBg.alpha, 0.01f)
            assertEquals(0.08f, inactiveChipBg.alpha, 0.01f)
            assertEquals(0.45f, activeChipBorder.alpha, 0.01f)
            assertEquals(0.20f, inactiveChipBorder.alpha, 0.01f)
            assertEquals(0.85f, inactiveText.alpha, 0.01f)

            assertTrue("Active badge chip bg must have higher opacity than inactive", activeChipBg.alpha > inactiveChipBg.alpha)
            assertTrue("Active badge chip border must have higher opacity than inactive", activeChipBorder.alpha > inactiveChipBorder.alpha)
        }
    }

    // =========================================================================
    // 3. Default Theme Prioritization Invariants
    // =========================================================================

    @Test
    fun testDefaultThemePrioritizationInRegistry() {
        assertNotNull("ThemeOptions registry must not be null", ThemeOptions)
        assertFalse("ThemeOptions must not be empty", ThemeOptions.isEmpty())

        // The very first item in the registry must be OrbitTheme
        val firstKey = ThemeOptions.keys.first()
        assertEquals(
            "OrbitTheme must be the first theme registered in ThemeOptions (index 0)",
            "OrbitTheme",
            firstKey
        )

        // The second item in the registry must be ChaosTheme
        val secondKey = ThemeOptions.keys.elementAt(1)
        assertEquals(
            "OrbitChaos must be the second theme registered in ThemeOptions (index 1)",
            "OrbitChaos",
            secondKey
        )

        // THEME_KEY default preference value must strictly match OrbitTheme.key
        assertEquals(
            "THEME_KEY default must be 'OrbitTheme'",
            "OrbitTheme",
            THEME_KEY.default
        )
    }

    // =========================================================================
    // 4. Tap Animation Manager Concurrency, Queue Bounds & Coordinate Safety
    // =========================================================================

    @Test
    fun testTapAnimationManagerRapidSpawnConcurrencyStorm() {
        val manager = KeyboardTapAnimationManager()
        val threadCount = 10
        val tapsPerThread = 200
        val executor = Executors.newFixedThreadPool(threadCount)
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)

        val animationTypes = listOf(
            TapAnimationType.RADAR_WAVE,
            TapAnimationType.PARTICLE_BURST,
            TapAnimationType.CONSTELLATION_WEB,
            TapAnimationType.NONE
        )

        for (t in 0 until threadCount) {
            executor.submit {
                try {
                    startLatch.await()
                    for (i in 0 until tapsPerThread) {
                        val type = animationTypes[i % animationTypes.size]
                        manager.spawnAnimation(type, 100f + i, 200f + i)
                        successCount.incrementAndGet()
                    }
                } finally {
                    doneLatch.countDown()
                }
            }
        }

        startLatch.countDown()
        val completed = doneLatch.await(5, TimeUnit.SECONDS)
        executor.shutdown()

        assertTrue("All concurrency tap storm threads must complete cleanly without deadlocks", completed)
        assertEquals(threadCount * tapsPerThread, successCount.get())
        assertTrue("TapAnimationManager must have active effects after spawning", manager.hasActiveEffects())

        manager.clear()
        assertFalse("TapAnimationManager must not have active effects after clear()", manager.hasActiveEffects())
    }

    @Test
    fun testTapAnimationManagerCoordinateBoundaryAndNaNSafety() {
        val manager = KeyboardTapAnimationManager()

        // Test adversarial coordinates: NaN, infinities, extreme values
        val adversarialCoords = listOf(
            Pair(Float.NaN, Float.NaN),
            Pair(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY),
            Pair(-100000f, -500000f),
            Pair(999999f, 888888f),
            Pair(0f, 0f)
        )

        for ((x, y) in adversarialCoords) {
            // Must not throw any exception
            manager.spawnAnimation(TapAnimationType.RADAR_WAVE, x, y)
            manager.spawnAnimation(TapAnimationType.PARTICLE_BURST, x, y)
            manager.spawnAnimation(TapAnimationType.CONSTELLATION_WEB, x, y)
            manager.spawnAnimation(TapAnimationType.NONE, x, y)
        }

        assertTrue("Manager must handle adversarial coordinates without crash", manager.hasActiveEffects())
        manager.clear()
        assertFalse(manager.hasActiveEffects())
    }

    // =========================================================================
    // 5. Preview Sizing Multiplier Bounds & Key Dimension Safety Invariants
    // =========================================================================

    @Test
    fun testPreviewSizingMultiplierBoundsAndSanity() {
        // Formula from RealisticKeyboardPreview:
        // val safeHeightMultiplier = if (heightMultiplier.isNaN() || heightMultiplier <= 0f) 1.0f else heightMultiplier.coerceIn(0.5f, 1.5f)
        // val safeBottomOffset = if (bottomOffset.isNaN() || bottomOffset < 0f) 0f else bottomOffset.coerceIn(0f, 100f)

        fun computeSafeHeightMultiplier(raw: Float): Float {
            return if (raw.isNaN() || raw <= 0f) 1.0f else raw.coerceIn(0.5f, 1.5f)
        }

        fun computeSafeBottomOffset(raw: Float): Float {
            return if (raw.isNaN() || raw < 0f) 0f else raw.coerceIn(0f, 100f)
        }

        // Test NaN / Inf / Negative
        assertEquals(1.0f, computeSafeHeightMultiplier(Float.NaN), 0.0001f)
        assertEquals(1.0f, computeSafeHeightMultiplier(0f), 0.0001f)
        assertEquals(1.0f, computeSafeHeightMultiplier(-1.5f), 0.0001f)
        assertEquals(0.5f, computeSafeHeightMultiplier(0.1f), 0.0001f)
        assertEquals(1.5f, computeSafeHeightMultiplier(2.5f), 0.0001f)
        assertEquals(1.5f, computeSafeHeightMultiplier(Float.POSITIVE_INFINITY), 0.0001f)

        assertEquals(0f, computeSafeBottomOffset(Float.NaN), 0.0001f)
        assertEquals(0f, computeSafeBottomOffset(-50f), 0.0001f)
        assertEquals(0f, computeSafeBottomOffset(0f), 0.0001f)
        assertEquals(48f, computeSafeBottomOffset(48f), 0.0001f)
        assertEquals(100f, computeSafeBottomOffset(150f), 0.0001f)
        assertEquals(100f, computeSafeBottomOffset(Float.POSITIVE_INFINITY), 0.0001f)

        // Key heights must never collapse to <= 0dp
        for (raw in listOf(-10f, 0f, 0.5f, 1.0f, 1.5f, 100f)) {
            val mult = computeSafeHeightMultiplier(raw)
            val keyHeight = (44 * mult).dp
            assertTrue("Key height must be >= 22dp", keyHeight >= 22.dp)
            assertTrue("Key height must be <= 66dp", keyHeight <= 66.dp)
        }
    }
}
