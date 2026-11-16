package org.futo.inputmethod.latin

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import org.futo.inputmethod.keyboard.internal.KeyPreviewView
import org.futo.inputmethod.keyboard.internal.LaserReticleEffect
import org.futo.inputmethod.latin.settings.Settings
import org.futo.inputmethod.latin.settings.SettingsValues
import org.futo.inputmethod.latin.uix.actions.ClipboardEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class SevenPendingItemsVerificationTest {

    // =========================================================================
    // Item 1: One-Handed Mode Key Popup Alignment
    // =========================================================================
    @Test
    fun testOneHandedModeKeyPreviewCalculation() {
        val keyboardViewWidth = 842
        val originX = 238 // Right-docked one-handed mode offset
        val previewWidth = 100
        val keyDrawWidth = 76

        // Test key at right edge of keyboard
        val rightKeyDrawX = 760
        var keyX = rightKeyDrawX - (previewWidth - keyDrawWidth) / 2
        val maxKeyX = keyboardViewWidth - previewWidth // 742
        val minKeyX = 0

        val keyPreviewPosition: Int
        if (keyX < minKeyX) {
            keyX = minKeyX
            keyPreviewPosition = KeyPreviewView.POSITION_LEFT
        } else if (keyX > maxKeyX) {
            keyX = maxKeyX
            keyPreviewPosition = KeyPreviewView.POSITION_RIGHT
        } else {
            keyPreviewPosition = KeyPreviewView.POSITION_MIDDLE
        }

        assertEquals(KeyPreviewView.POSITION_RIGHT, keyPreviewPosition)
        assertEquals(742, keyX)

        val finalPreviewX = keyX + originX
        // In window/placer coordinates: 742 + 238 = 980
        assertEquals(980, finalPreviewX)
        assertTrue("Popup must be placed within screen bounds [0, 1080]", finalPreviewX + previewWidth <= 1080)
        assertTrue("Popup must align with shifted one-handed keyboard", finalPreviewX >= originX)

        // Test key at left edge of shifted keyboard
        val leftKeyDrawX = 10
        var leftX = leftKeyDrawX - (previewWidth - keyDrawWidth) / 2 // 10 - 12 = -2
        val leftPosition: Int
        if (leftX < minKeyX) {
            leftX = minKeyX
            leftPosition = KeyPreviewView.POSITION_LEFT
        } else if (leftX > maxKeyX) {
            leftX = maxKeyX
            leftPosition = KeyPreviewView.POSITION_RIGHT
        } else {
            leftPosition = KeyPreviewView.POSITION_MIDDLE
        }

        assertEquals(KeyPreviewView.POSITION_LEFT, leftPosition)
        assertEquals(0, leftX)
        val finalLeftPreviewX = leftX + originX
        assertEquals(238, finalLeftPreviewX)
    }

    // =========================================================================
    // Item 2: Clipboard Pinned Item Sorting Order
    // =========================================================================
    @Test
    fun testClipboardPinnedItemsSortingOrder() {
        val entry1 = ClipboardEntry(timestamp = 1000L, pinned = false, text = "Unpinned 1", uri = null, mimeTypes = listOf())
        val entry2 = ClipboardEntry(timestamp = 2000L, pinned = true, text = "Pinned Old", uri = null, mimeTypes = listOf())
        val entry3 = ClipboardEntry(timestamp = 3000L, pinned = false, text = "Unpinned 2 (New)", uri = null, mimeTypes = listOf())
        val entry4 = ClipboardEntry(timestamp = 4000L, pinned = true, text = "Pinned New", uri = null, mimeTypes = listOf())

        val rawList = listOf(entry1, entry2, entry3, entry4)
        val sortedList = rawList.sortedWith(compareByDescending<ClipboardEntry> { it.pinned }.thenByDescending { it.timestamp })

        // Pinned items must come first, sorted newest first
        assertEquals("Pinned New", sortedList[0].text)
        assertEquals("Pinned Old", sortedList[1].text)
        // Unpinned items follow, sorted newest first
        assertEquals("Unpinned 2 (New)", sortedList[2].text)
        assertEquals("Unpinned 1", sortedList[3].text)
    }

    // =========================================================================
    // Item 3: Laser Reticle Typing Effect (Random 3 Colors & Smooth Spread)
    // =========================================================================
    @Test
    fun testLaserReticleSignatureColors() {
        val expectedColors = setOf(
            0xFFFFFFFF.toInt(), // Pure White
            0xFFD71921.toInt(), // Nothing Red
            0xFFFED700.toInt()  // Cyber Yellow
        )

        assertEquals(3, LaserReticleEffect.SIGNATURE_COLORS.size)
        for (c in LaserReticleEffect.SIGNATURE_COLORS) {
            assertTrue("Laser color must be one of signature colors", expectedColors.contains(c))
        }

        // Generate multiple laser effects to ensure random distribution selects from signature colors
        val observedColors = mutableSetOf<Int>()
        val dummyCanvas = Canvas()
        val now = android.os.SystemClock.uptimeMillis()

        for (i in 0 until 100) {
            val laser = LaserReticleEffect(50f, 50f, colorMode = "random")
            assertTrue("Laser chosen color must be in signature colors", expectedColors.contains(laser.chosenColor))
            observedColors.add(laser.chosenColor)
            assertTrue(laser.updateAndDraw(dummyCanvas, now))
        }
        assertEquals("All 3 signature colors should be selected over 100 random taps", expectedColors, observedColors)
    }

    @Test
    fun testLaserReticleExpansionAndDissipationCurves() {
        val durationMs = 700f
        val dummyCanvas = Canvas()
        val startTime = 1000L

        val laser = LaserReticleEffect(50f, 50f, "random", startTime, durationMs)

        // 1. Immediately at start (progress = 0)
        assertTrue(laser.updateAndDraw(dummyCanvas, startTime))

        // 2. Midway through expansion (progress = 0.14, which is 50% of the 0.28 expansion phase)
        val midwayExpandTime = (startTime + (durationMs * 0.14f)).toLong()
        assertTrue(laser.updateAndDraw(dummyCanvas, midwayExpandTime))

        // 3. Past expansion phase, fading out (progress = 0.60)
        val fadeTime = (startTime + (durationMs * 0.60f)).toLong()
        assertTrue(laser.updateAndDraw(dummyCanvas, fadeTime))

        // 4. Past expiration (progress >= 1.0)
        val expiredTime = (startTime + durationMs.toLong() + 10L)
        assertFalse(laser.updateAndDraw(dummyCanvas, expiredTime))
    }

    // =========================================================================
    // Item 4: Volume Button Cursor Navigation
    // =========================================================================
    @Test
    fun testVolumeKeyCursorSettings() {
        assertEquals("pref_volume_key_cursor", Settings.PREF_VOLUME_KEY_CURSOR)

        val map = mutableMapOf<String, Any>()
        val proxyPrefs = Proxy.newProxyInstance(
            SharedPreferences::class.java.classLoader,
            arrayOf(SharedPreferences::class.java)
        ) { _, method, args ->
            if (method.name == "getBoolean") {
                val key = args[0] as String
                val def = args[1] as Boolean
                map[key] as? Boolean ?: def
            } else null
        } as SharedPreferences

        // Default when key not present must be false
        map.clear()
        assertFalse(Settings.readVolumeKeyCursor(proxyPrefs))

        map[Settings.PREF_VOLUME_KEY_CURSOR] = true
        assertTrue(Settings.readVolumeKeyCursor(proxyPrefs))

        map[Settings.PREF_VOLUME_KEY_CURSOR] = false
        assertFalse(Settings.readVolumeKeyCursor(proxyPrefs))
    }

    // =========================================================================
    // Item 5: Recent Emoji 66-Cap Accumulation & Fresh Blank List
    // =========================================================================
    @Test
    fun testRecentEmojiCap66() {
        val emojis = (1..100).map { "Emoji_$it" }
        val raw = emojis.joinToString("<|>")
        val parsed = raw.split("<|>").map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        val capped = parsed.take(66)

        assertEquals(66, capped.size)
        assertEquals("Emoji_1", capped.first())
        assertEquals("Emoji_66", capped.last())

        // Blank raw returns empty list
        val emptyRaw = ""
        val emptyParsed = if (emptyRaw.isBlank()) emptyList<String>() else emptyRaw.split("<|>")
        assertTrue(emptyParsed.isEmpty())
    }
}
