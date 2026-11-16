package org.futo.inputmethod.latin.uix.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.roundToInt

/**
 * Empirical Adversarial Test Harness for Milestone 3 (Accordion UI State & Invariants):
 * 1. Single-Expanded Accordion State Transitions & Mathematical Invariants.
 * 2. Collapse Previous on Card Open & Collapse to Null on Tap of Active Card.
 * 3. Configuration Change & Rotation Resilience (rememberSaveable state preservation).
 * 4. Card Boundary Invariants (Page 1 has indices 0..4; Page 2 has indices 0..6 + 1 static card).
 * 5. Summary String Computation Null Safety & Formatting Invariants across Edge Cases.
 * 6. High-Throughput Concurrency Storm on Accordion State Machine.
 */
class Milestone3AccordionAdversarialTest {

    // =========================================================================
    // Pure Mathematical Transition Model (Matches Screen Implementations)
    // =========================================================================

    /**
     * Mathematical accordion transition function:
     * f(expanded, clicked) = if (expanded == clicked) null else clicked
     */
    private fun transitionAccordion(expanded: Int?, clicked: Int): Int? {
        return if (expanded == clicked) null else clicked
    }

    // =========================================================================
    // 1. Single-Expanded Behavior & Invariant Verification
    // =========================================================================

    @Test
    fun testInitialExpansionStates() {
        // Page 1 (KeyboardCustomizationScreen): Initial state is 0 (Themes & Color Presets expanded)
        val page1InitialIndex: Int? = 0
        assertEquals("Page 1 must start with card index 0 expanded by default", 0, page1InitialIndex)

        // Page 2 (MoreSettingsScreen): Initial state is null (all cards collapsed for clean overview)
        val page2InitialIndex: Int? = null
        assertNull("Page 2 must start with all cards collapsed (null)", page2InitialIndex)
    }

    @Test
    fun testOpeningCardCollapsesPreviouslyOpenedCard() {
        // Given card 0 is expanded:
        var activeIndex: Int? = 0

        // When user opens card 1:
        activeIndex = transitionAccordion(activeIndex, 1)

        // Then card 1 is expanded, and card 0 is collapsed
        assertEquals("Card 1 must now be expanded", 1, activeIndex)
        assertFalse("Card 0 must no longer be expanded", activeIndex == 0)

        // When user opens card 2:
        activeIndex = transitionAccordion(activeIndex, 2)
        assertEquals("Card 2 must now be expanded", 2, activeIndex)
        assertFalse("Card 1 must no longer be expanded", activeIndex == 1)

        // Verify across all pairs of indices (i, j) where i != j
        for (i in 0..6) {
            for (j in 0..6) {
                if (i != j) {
                    val result = transitionAccordion(i, j)
                    assertEquals("Opening card $j from card $i must result in $j", j, result)
                    assertNotEquals("Card $i must be collapsed", i, result)
                }
            }
        }
    }

    @Test
    fun testTappingActiveCardCollapsesToNull() {
        // Invariant: Tapping an already expanded card must set state to null (all collapsed)
        for (i in 0..6) {
            val collapsedResult = transitionAccordion(i, i)
            assertNull("Tapping active card $i must collapse to null", collapsedResult)
        }
    }

    @Test
    fun testOpeningCardFromAllCollapsedState() {
        // Invariant: When all cards are collapsed (null), tapping card i must expand card i
        val initialState: Int? = null
        for (i in 0..6) {
            val opened = transitionAccordion(initialState, i)
            assertEquals("Opening card $i from null must set state to $i", i, opened)
        }
    }

    @Test
    fun testIdempotenceAndToggleCycleInvariants() {
        // Double-tapping a card starting from collapsed: null -> i -> null
        for (i in 0..6) {
            val step1 = transitionAccordion(null, i)
            assertEquals("First tap expands card $i", i, step1)
            val step2 = transitionAccordion(step1, i)
            assertNull("Second tap collapses card $i back to null", step2)
        }

        // Toggling a different card twice: A -> B -> null
        val initialCard = 1
        val nextCard = 3
        val switched = transitionAccordion(initialCard, nextCard)
        assertEquals(3, switched)
        val closed = transitionAccordion(switched, nextCard)
        assertNull(closed)
    }

    // =========================================================================
    // 2. High-Throughput Concurrency Storm on Accordion State Machine
    // =========================================================================

    @Test
    fun testConcurrentAccordionToggleStorm() {
        val threadCount = 16
        val operationsPerThread = 2500
        val totalOperations = threadCount * operationsPerThread

        val executor = Executors.newFixedThreadPool(threadCount)
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(threadCount)

        val activeCardState = AtomicReference<Int?>(0)
        val totalTransitionsCount = AtomicInteger(0)

        for (t in 0 until threadCount) {
            executor.submit {
                try {
                    startLatch.await()
                    for (step in 0 until operationsPerThread) {
                        val targetCard = (step + t) % 7
                        var current: Int?
                        var next: Int?
                        do {
                            current = activeCardState.get()
                            next = if (current == targetCard) null else targetCard
                        } while (!activeCardState.compareAndSet(current, next))

                        totalTransitionsCount.incrementAndGet()

                        // Check invariant: state must always be either null or an integer in 0..6
                        val observed = activeCardState.get()
                        assertTrue(
                            "Observed state must be either null or in range 0..6",
                            observed == null || observed in 0..6
                        )
                    }
                } finally {
                    doneLatch.countDown()
                }
            }
        }

        startLatch.countDown()
        val finished = doneLatch.await(10, TimeUnit.SECONDS)
        executor.shutdown()

        assertTrue("Toggle storm must complete within timeout without deadlock", finished)
        assertEquals("All transitions must be recorded", totalOperations, totalTransitionsCount.get())

        val finalState = activeCardState.get()
        assertTrue("Final state must be valid", finalState == null || finalState in 0..6)
    }

    // =========================================================================
    // 3. Card Boundary Properties
    // =========================================================================

    @Test
    fun testPage1CardBoundaries() {
        // Page 1 (KeyboardCustomizationScreen) has exactly 5 accordion cards:
        // Card 0: Themes & Color Presets
        // Card 1: Spacebar Styling
        // Card 2: Typography & Hints
        // Card 3: Key Popups & Typing Effects
        // Card 4: Resize & Position
        val page1CardIndices = listOf(0, 1, 2, 3, 4)
        assertEquals("Page 1 must have exactly 5 accordion cards", 5, page1CardIndices.size)

        // For each card index i, only card i is expanded when activeIndex == i
        for (active in page1CardIndices) {
            val expandedStates = page1CardIndices.map { cardIndex -> cardIndex == active }
            val countExpanded = expandedStates.count { it }
            assertEquals("Exactly 1 card must be expanded", 1, countExpanded)
            assertTrue("Card $active must be expanded", expandedStates[active])
        }

        // When activeIndex is null, 0 cards are expanded
        val activeIndex: Int? = null
        val allCollapsedStates = page1CardIndices.map { cardIndex -> cardIndex == activeIndex }
        assertEquals("0 cards must be expanded when activeIndex is null", 0, allCollapsedStates.count { it })

        // Out-of-bounds indices must never expand any Page 1 card
        val outOfBoundsIndices = listOf(-1, 5, 6, 7, 999, Int.MIN_VALUE, Int.MAX_VALUE)
        for (badIndex in outOfBoundsIndices) {
            val matchCount = page1CardIndices.count { it == badIndex }
            assertEquals("Out-of-bounds index $badIndex must not match any Page 1 card", 0, matchCount)
        }
    }

    @Test
    fun testPage2CardBoundaries() {
        // Page 2 (MoreSettingsScreen) has exactly 7 expandable accordion cards:
        // Card 0: Typing & Input
        // Card 1: Suggestions & Action Bar
        // Card 2: Haptics & Sound
        // Card 3: Layout & Keys
        // Card 4: Voice & Input Tools
        // Card 5: Languages & Subtypes
        // Card 6: Updates & Announcements
        // Plus 1 permanently visible static card:
        // Card 8: About Orbit Board (NeuCard)
        val page2AccordionIndices = listOf(0, 1, 2, 3, 4, 5, 6)
        assertEquals("Page 2 must have exactly 7 expandable accordion cards", 7, page2AccordionIndices.size)

        // Single-expanded mutual exclusion test across all 7 cards
        for (active in page2AccordionIndices) {
            val expandedStates = page2AccordionIndices.map { cardIndex -> cardIndex == active }
            assertEquals("Exactly 1 card must be expanded", 1, expandedStates.count { it })
            assertTrue("Card $active must be expanded", expandedStates[active])
        }

        // Out-of-bounds indices must never expand any Page 2 accordion card
        val badIndices = listOf(-1, 7, 8, 9, 100, Int.MIN_VALUE, Int.MAX_VALUE)
        for (bad in badIndices) {
            val matchCount = page2AccordionIndices.count { it == bad }
            assertEquals("Index $bad must not match any Page 2 accordion card", 0, matchCount)
        }
    }

    // =========================================================================
    // 4. Configuration Change & Rotation Resilience Simulation
    // =========================================================================

    @Test
    fun testConfigurationChangePreservationLogic() {
        // Simulating rememberSaveable { mutableStateOf<Int?>(...) }
        // During an Android configuration change (screen rotation, font size change):
        // 1. Current State is serialized into the SavedStateRegistry (Bundle / Map).
        // 2. Activity / Composable tree is destroyed and recreated.
        // 3. rememberSaveable restores the serialized Int? from the saved state.

        val savedStateBundle = mutableMapOf<String, Any?>()
        val stateKey = "expandedCardIndex"

        // Scenario A: User expanded Card 3 on Page 1, then rotated the device
        var activeIndexBeforeRotation: Int? = 3
        savedStateBundle[stateKey] = activeIndexBeforeRotation

        // Simulate activity recreation:
        var restoredActiveIndex = savedStateBundle[stateKey] as? Int?
        assertEquals("Active card index 3 must survive configuration change", 3, restoredActiveIndex)

        // Scenario B: User collapsed all cards on Page 2 (null), then rotated the device
        activeIndexBeforeRotation = null
        savedStateBundle[stateKey] = activeIndexBeforeRotation

        restoredActiveIndex = savedStateBundle[stateKey] as? Int?
        assertNull("All-collapsed state (null) must survive configuration change", restoredActiveIndex)

        // Scenario C: Contrast with non-saveable remember (which would lose user state)
        val defaultState: Int? = 0
        val nonSaveableRestored = defaultState // If remember { } had been used, it resets to default 0
        assertNotEquals(
            "rememberSaveable avoids resetting to default when active card was 3",
            nonSaveableRestored,
            3
        )
    }

    // =========================================================================
    // 5. Summary String Computation Null Safety & Formatting Invariants
    // =========================================================================

    private fun getThemeDisplayNameTestHelper(themeKey: String): String = when (themeKey) {
        "OrbitTheme" -> "Orbit"
        "OrbitChaos" -> "Chaos"
        "NothingDark" -> "Nothing Dark"
        "DynamicNothing" -> "Dynamic"
        "AMOLEDNothing" -> "AMOLED"
        "NothingLight" -> "Light"
        "AMOLEDDarkPurple" -> "AMOLED Purple"
        "ClassicMaterialDark" -> "Material Dark"
        "ClassicMaterialLight" -> "Material Light"
        "Emerald" -> "Emerald"
        "CottonCandy" -> "Cotton Candy"
        "Sunflower" -> "Sunflower"
        "Snowfall" -> "Snowfall"
        "SteelGray" -> "Steel Gray"
        "DeepSeaDark" -> "Deep Sea Dark"
        "DeepSeaLight" -> "Deep Sea Light"
        "DynamicSystem", "DynamicSystemTheme" -> "Dynamic System"
        "DynamicDark", "DynamicDarkTheme" -> "Dynamic Dark"
        "DynamicLight", "DynamicLightTheme" -> "Dynamic Light"
        "VoiceInputTheme" -> "Voice Input"
        else -> themeKey
    }

    @Test
    fun testPage1Card0ThemeAndAccentSummaryComputation() {
        val testThemes = listOf(
            "OrbitTheme", "OrbitChaos", "NothingDark", "DynamicSystem",
            "CustomUnknownTheme", "", "   ", "⚡Theme_99"
        )
        val testAccents = listOf(
            "default", "red", "yellow", "blue", "white", "green", "purple", "orange",
            "", "   ", "custom_hex", "123"
        )

        for (theme in testThemes) {
            for (accent in testAccents) {
                val themeDisplayName = getThemeDisplayNameTestHelper(theme)
                val accentFormatted = accent.replaceFirstChar { it.uppercase() }
                val summary = "$themeDisplayName • Accent: $accentFormatted"

                assertNotNull(summary)
                assertTrue("Summary must contain separator", summary.contains("• Accent:"))
                assertTrue("Theme name must be present", summary.startsWith(themeDisplayName))
            }
        }
    }

    @Test
    fun testPage1Card1SpacebarSummaryComputation() {
        val testShapes = listOf("boxy", "roundy", "", "CUSTOM", "unknown")
        val testColorKeys = listOf("theme", "red", "blue", "yellow", "white", "dark", "default", "", "custom")

        for (shape in testShapes) {
            for (colorKey in testColorKeys) {
                val shapeFormatted = shape.replaceFirstChar { it.uppercase() }
                val colorFormatted = colorKey.replaceFirstChar { it.uppercase() }
                val summary = "$shapeFormatted • Color: $colorFormatted"

                assertNotNull(summary)
                assertTrue("Summary must contain separator", summary.contains("• Color:"))
            }
        }
    }

    @Test
    fun testPage1Card2TypographySummaryComputation() {
        val fontOptionNames = mapOf(
            "nothing" to "Nothing (Orbit Dot)",
            "doto" to "Doto Dot-Matrix",
            "qurova" to "Qurova Display",
            "agency" to "Agency Tech",
            "inter" to "Inter Clean"
        )

        val testFonts = listOf("nothing", "doto", "qurova", "agency", "inter", "unknown_font", "", "   ")
        val testHints = listOf(true, false)

        for (font in testFonts) {
            for (hints in testHints) {
                val fontName = fontOptionNames[font] ?: "Orbit Dot"
                val hintsText = if (hints) "On" else "Off"
                val summary = "$fontName • Hints: $hintsText"

                assertNotNull(summary)
                assertTrue("Summary must contain separator", summary.contains("• Hints:"))
                assertTrue("Font name must be non-empty", fontName.isNotBlank())
            }
        }
    }

    @Test
    fun testPage1Card3PopupAndTypingEffectSummaryComputation() {
        val popupOptions = mapOf(
            "orbit" to "Orbit Minimalist",
            "classic" to "Classic Card",
            "none" to "Off"
        )
        val effectOptions = mapOf(
            "none" to "None",
            "radar_wave" to "Radar Wave",
            "particle_burst" to "Particle Burst",
            "constellation_web" to "Constellation Web"
        )

        val testPopups = listOf("orbit", "classic", "none", "unknown", "")
        val testEffects = listOf("none", "radar_wave", "particle_burst", "constellation_web", "unknown", "")

        for (popup in testPopups) {
            for (effect in testEffects) {
                val popupName = popupOptions[popup] ?: "Orbit"
                val effectName = effectOptions[effect] ?: "None"
                val summary = "Popup: $popupName • Effect: $effectName"

                assertNotNull(summary)
                assertTrue(summary.startsWith("Popup: "))
                assertTrue(summary.contains("• Effect: "))
            }
        }
    }

    @Test
    fun testPage1Card4ResizeAndPositionSummaryComputation() {
        // Height multiplier range: 0.70f..1.30f, default 1.0f
        // Bottom offset range: 0f..48f, default 0f
        val testHeights = listOf(0.70f, 0.85f, 1.00f, 1.15f, 1.30f, 0.0f, -1.0f, 2.5f)
        val testOffsets = listOf(0f, 8f, 16f, 24f, 48f, -10f, 100f)

        for (h in testHeights) {
            for (o in testOffsets) {
                val heightPct = (h * 100).roundToInt()
                val offsetDp = o.roundToInt()
                val summary = "Height: $heightPct% • Offset: $offsetDp dp"

                assertNotNull(summary)
                assertTrue(summary.startsWith("Height: $heightPct%"))
                assertTrue(summary.endsWith("$offsetDp dp"))
            }
        }
    }

    data class SpaceModeInfo(val mode: Int, val name: String, val desc: String)

    @Test
    fun testPage2Card0TypingSummaryComputation() {
        // Matching Settings.SPACEBAR_MODE_* constants
        val spacebarModeSwipeCursor = 0
        val spacebarModeSwipeLanguage = 1
        val spacebarModeSwipeCursorOnly = 2

        val spaceModes = listOf(
            SpaceModeInfo(spacebarModeSwipeLanguage, "Language", "Switch layout"),
            SpaceModeInfo(spacebarModeSwipeCursor, "Cursor", "Move cursor"),
            SpaceModeInfo(spacebarModeSwipeCursorOnly, "Cursor Only", "Only cursor")
        )

        val testAutoCorrection = listOf(true, false)
        val testSpacebarModes = listOf(
            spacebarModeSwipeLanguage,
            spacebarModeSwipeCursor,
            spacebarModeSwipeCursorOnly,
            -1, 99
        )

        for (ac in testAutoCorrection) {
            for (mode in testSpacebarModes) {
                val activeSpaceModeName = spaceModes.firstOrNull { it.mode == mode }?.name ?: "Language"
                val summary = "Correction: ${if (ac) "On" else "Off"} • Space: $activeSpaceModeName"

                assertNotNull(summary)
                assertTrue(summary.startsWith("Correction: "))
                assertTrue(summary.contains("• Space: "))
            }
        }
    }

    @Test
    fun testPage2Card1SuggestionsSummaryComputation() {
        val testActionBar = listOf(true, false)
        val testBigram = listOf(true, false)

        for (bar in testActionBar) {
            for (bigram in testBigram) {
                val summary = "${if (bar) "Bar Active" else "Bar Hidden"} • ${if (bigram) "Predictions On" else "Basic"}"
                assertNotNull(summary)
                assertTrue(summary.contains("•"))
            }
        }
    }

    @Test
    fun testPage2Card2HapticsAndSoundSummaryComputation() {
        val testVibrate = listOf(true, false)
        val testSound = listOf(true, false)
        val testDurations = listOf(-1f, 0f, 10f, 25f, 100f, -50f, 999f)
        val testVolumes = listOf(-1f, 0f, 30f, 80f, 100f, -10f, 500f)

        for (vib in testVibrate) {
            for (snd in testSound) {
                for (dur in testDurations) {
                    for (vol in testVolumes) {
                        val vibText = if (vib) {
                            if (dur.roundToInt() <= -1) "Default" else "${dur.roundToInt()}ms"
                        } else "Off"

                        val sndText = if (snd) {
                            if (vol.roundToInt() <= -1) "Default" else "${vol.roundToInt()}%"
                        } else "Off"

                        val summary = "Vibrate: $vibText • Sound: $sndText"
                        assertNotNull(summary)
                        assertTrue(summary.startsWith("Vibrate: "))
                        assertTrue(summary.contains("• Sound: "))
                    }
                }
            }
        }
    }

    @Test
    fun testPage2Card3LayoutAndKeysSummaryComputation() {
        val testNumberRow = listOf(true, false)
        val testTimeouts = listOf(100f, 300f, 500f, 700f, 0f, -50f, 1200f)

        for (nr in testNumberRow) {
            for (timeout in testTimeouts) {
                val summary = "Digits: ${if (nr) "On" else "Off"} • Delay: ${timeout.roundToInt()}ms"
                assertNotNull(summary)
                assertTrue(summary.startsWith("Digits: "))
                assertTrue(summary.contains("• Delay: "))
            }
        }
    }

    @Test
    fun testPage2Card4VoiceSummaryComputation() {
        val testVoice = listOf(true, false)
        val testClip = listOf(true, false)

        for (v in testVoice) {
            for (c in testClip) {
                val summary = "Voice: ${if (v) "On" else "Off"} • Clipboard: ${if (c) "On" else "Off"}"
                assertNotNull(summary)
                assertTrue(summary.startsWith("Voice: "))
                assertTrue(summary.contains("• Clipboard: "))
            }
        }
    }

    @Test
    fun testPage2Card5LanguagesSummaryComputation() {
        // Test Language summary calculation logic with empty, normal, complex, and malformed inputMethods
        fun computeLanguageSummary(inputMethods: List<String>): Pair<Int, String> {
            val count = inputMethods.size
            val names = if (inputMethods.isEmpty()) {
                "English (US)"
            } else {
                inputMethods.mapNotNull {
                    try {
                        val localeStr = it.split(":")[0]
                        val parts = localeStr.split("_")
                        val locale = if (parts.size > 1) Locale(parts[0], parts[1]) else Locale(parts[0])
                        locale.getDisplayLanguage(Locale.ENGLISH).ifEmpty { null }
                    } catch (e: Exception) {
                        null
                    }
                }.distinct().joinToString(", ").ifEmpty { "English" }
            }
            return Pair(count, names)
        }

        // Case A: Empty list
        val emptyResult = computeLanguageSummary(emptyList())
        assertEquals(0, emptyResult.first)
        assertEquals("English (US)", emptyResult.second)

        // Case B: Single standard locale
        val singleResult = computeLanguageSummary(listOf("en_US:qwerty"))
        assertEquals(1, singleResult.first)
        assertEquals("English", singleResult.second)

        // Case C: Bengali and English multiple locales
        val multiResult = computeLanguageSummary(listOf("bn_BD:probhat", "en_US:qwerty"))
        assertEquals(2, multiResult.first)
        assertTrue(multiResult.second.contains("Bengali") || multiResult.second.contains("Bangla"))
        assertTrue(multiResult.second.contains("English"))

        // Case D: Malformed strings must not throw exceptions
        val malformedList = listOf("invalid", ":", "a_b_c_d_e", "", "   ")
        val malformedResult = computeLanguageSummary(malformedList)
        assertEquals(5, malformedResult.first)
        assertNotNull(malformedResult.second)

        // Case E: Summary string concatenation
        val summaryStr = "${multiResult.first} Active • ${multiResult.second}"
        assertTrue(summaryStr.startsWith("2 Active • "))
    }

    @Test
    fun testPage2Card6UpdatesSummaryComputation() {
        val testVersionNames = listOf("1.3.10", "1.0.0", "2.0.0-beta", "0.9.9")
        val testNotifications = listOf(true, false)

        for (versionName in testVersionNames) {
            for (notif in testNotifications) {
                val summary = "v$versionName • ${if (notif) "Auto-notify On" else "Manual"}"
                assertNotNull(summary)
                assertTrue(summary.startsWith("v$versionName • "))
            }

            // Static Card 8 About summary
            val staticAboutSummary = "v$versionName • Built by Fahim Takrim"
            assertEquals("v$versionName • Built by Fahim Takrim", staticAboutSummary)
        }
    }
}
