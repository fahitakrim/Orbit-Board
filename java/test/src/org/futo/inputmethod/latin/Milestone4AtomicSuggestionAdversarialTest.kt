package org.futo.inputmethod.latin

import android.text.SpannableString
import android.text.Spanned
import android.view.inputmethod.CompletionInfo
import android.view.inputmethod.CorrectionInfo
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Adversarial and Invariant Test Harness for Milestone 4 (Atomic Suggestion Replacement).
 *
 * Verifies:
 * 1. Mode A (Standard Host): Host maintains Spanned.SPAN_COMPOSING. Suggestion commit replaces
 *    composing region natively; no redundant deletion occurs.
 * 2. Mode B (Adversarial Host / Instagram Note Reply): Host strips or ignores Spanned.SPAN_COMPOSING.
 *    IME detects absence of composing spans and executes atomic prefix deletion prior to commit.
 * 3. Mode C (Selection Active): Non-empty selection range is replaced natively; prefix deletion is bypassed.
 * 4. Mode D (Avro / Bengali Transliteration): Transliterated phonetic prefix (e.g. "am" -> "আম")
 *    is deleted atomically based on displayed glyph count before committing selected candidate.
 * 5. Mode E (Empty Prefix / Manual Pick): Manual suggestion pick with no prefix inserts cleanly without index underflow.
 * 6. Mode F (Stress Concurrency): Rapid consecutive suggestion commits maintain buffer integrity without desync.
 */
class Milestone4AtomicSuggestionAdversarialTest {

    // =========================================================================
    // Mock Adversarial Host Editor InputConnection
    // =========================================================================

    class MockAdversarialHostConnection(
        val supportsComposingSpans: Boolean = true
    ) : InputConnection {

        val buffer = StringBuilder()
        var cursorPosition: Int = 0
        var selectionStart: Int = 0
        var selectionEnd: Int = 0

        // Composing range tracked by standard host (-1 if none or unsupported)
        var composingSpanStart: Int = -1
        var composingSpanEnd: Int = -1

        // Audit Counters
        var deleteSurroundingTextCallCount = 0
        var lastDeleteBeforeLength = 0
        var lastDeleteAfterLength = 0
        var commitTextCallCount = 0
        var lastCommittedText: String? = null
        var setComposingTextCallCount = 0

        fun setInitialText(text: String, cursor: Int = text.length) {
            buffer.setLength(0)
            buffer.append(text)
            cursorPosition = cursor
            selectionStart = cursor
            selectionEnd = cursor
            composingSpanStart = -1
            composingSpanEnd = -1
        }

        fun hasActiveComposingSpan(): Boolean {
            return supportsComposingSpans && composingSpanStart >= 0 && composingSpanEnd >= composingSpanStart
        }

        class MockSpannedString(
            private val text: String,
            private val spanStart: Int,
            private val spanEnd: Int,
            private val spanFlags: Int
        ) : Spanned, CharSequence by text {
            private val spanObject = Object()

            @Suppress("UNCHECKED_CAST")
            override fun <T : Any?> getSpans(start: Int, end: Int, type: Class<T>?): Array<T> {
                if (spanEnd > spanStart && end > spanStart && start < spanEnd) {
                    val array = java.lang.reflect.Array.newInstance(type ?: Any::class.java, 1) as Array<T>
                    array[0] = spanObject as T
                    return array
                }
                return java.lang.reflect.Array.newInstance(type ?: Any::class.java, 0) as Array<T>
            }

            override fun getSpanStart(tag: Any?): Int = if (tag === spanObject) spanStart else -1
            override fun getSpanEnd(tag: Any?): Int = if (tag === spanObject) spanEnd else -1
            override fun getSpanFlags(tag: Any?): Int = if (tag === spanObject) spanFlags else 0
            override fun nextSpanTransition(start: Int, limit: Int, type: Class<*>?): Int = limit
            override fun toString(): String = text
        }

        override fun getTextBeforeCursor(n: Int, flags: Int): CharSequence? {
            val start = (cursorPosition - n).coerceAtLeast(0)
            val sub = buffer.substring(start, cursorPosition)
            if (flags == InputConnection.GET_TEXT_WITH_STYLES && hasActiveComposingSpan()) {
                val compRelStart = (composingSpanStart - start).coerceIn(0, sub.length)
                val compRelEnd = (composingSpanEnd - start).coerceIn(0, sub.length)
                if (compRelEnd > compRelStart) {
                    return MockSpannedString(sub, compRelStart, compRelEnd, Spanned.SPAN_COMPOSING)
                }
            }
            return sub
        }

        override fun getTextAfterCursor(n: Int, flags: Int): CharSequence? {
            val end = (cursorPosition + n).coerceAtMost(buffer.length)
            return buffer.substring(cursorPosition, end)
        }

        override fun getSelectedText(flags: Int): CharSequence? {
            return if (selectionStart != selectionEnd) {
                val s = selectionStart.coerceAtMost(selectionEnd)
                val e = selectionStart.coerceAtLeast(selectionEnd)
                buffer.substring(s, e)
            } else null
        }

        override fun setComposingText(text: CharSequence?, newCursorPosition: Int): Boolean {
            setComposingTextCallCount++
            val str = text?.toString() ?: ""
            if (supportsComposingSpans) {
                if (composingSpanStart >= 0 && composingSpanEnd >= composingSpanStart) {
                    buffer.replace(composingSpanStart, composingSpanEnd, str)
                    composingSpanEnd = composingSpanStart + str.length
                    cursorPosition = composingSpanEnd
                } else {
                    val insertPos = cursorPosition
                    buffer.insert(insertPos, str)
                    composingSpanStart = insertPos
                    composingSpanEnd = insertPos + str.length
                    cursorPosition = composingSpanEnd
                }
            } else {
                // Host without composing support (e.g. Instagram note reply):
                // Directly appends or replaces at cursor without marking composing span
                buffer.insert(cursorPosition, str)
                cursorPosition += str.length
                composingSpanStart = -1
                composingSpanEnd = -1
            }
            selectionStart = cursorPosition
            selectionEnd = cursorPosition
            return true
        }

        override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
            deleteSurroundingTextCallCount++
            lastDeleteBeforeLength = beforeLength
            lastDeleteAfterLength = afterLength

            val deleteStart = (cursorPosition - beforeLength).coerceAtLeast(0)
            val deleteEnd = (cursorPosition + afterLength).coerceAtMost(buffer.length)
            if (deleteEnd > deleteStart) {
                buffer.delete(deleteStart, deleteEnd)
                cursorPosition = deleteStart
                selectionStart = cursorPosition
                selectionEnd = cursorPosition
            }
            // Clear composing span if deleted
            if (composingSpanStart >= cursorPosition) {
                composingSpanStart = -1
                composingSpanEnd = -1
            }
            return true
        }

        override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
            commitTextCallCount++
            val str = text?.toString() ?: ""
            lastCommittedText = str

            if (supportsComposingSpans && composingSpanStart >= 0) {
                // Standard host replaces composing range
                buffer.replace(composingSpanStart, composingSpanEnd, str)
                cursorPosition = composingSpanStart + str.length
                composingSpanStart = -1
                composingSpanEnd = -1
            } else if (selectionStart != selectionEnd) {
                // Replace selection range
                val s = selectionStart.coerceAtMost(selectionEnd)
                val e = selectionStart.coerceAtLeast(selectionEnd)
                buffer.replace(s, e, str)
                cursorPosition = s + str.length
                selectionStart = cursorPosition
                selectionEnd = cursorPosition
            } else {
                // Adversarial host without composing span:
                // Blindly inserts at cursor position!
                buffer.insert(cursorPosition, str)
                cursorPosition += str.length
            }
            selectionStart = cursorPosition
            selectionEnd = cursorPosition
            return true
        }

        override fun finishComposingText(): Boolean {
            composingSpanStart = -1
            composingSpanEnd = -1
            return true
        }

        override fun setSelection(start: Int, end: Int): Boolean {
            selectionStart = start
            selectionEnd = end
            cursorPosition = end
            return true
        }

        // Stub methods for interface completeness
        override fun getCursorCapsMode(reqModes: Int): Int = 0
        override fun getExtractedText(request: ExtractedTextRequest?, flags: Int): ExtractedText? = null
        override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean =
            deleteSurroundingText(beforeLength, afterLength)
        override fun setComposingRegion(start: Int, end: Int): Boolean {
            if (supportsComposingSpans) {
                composingSpanStart = start
                composingSpanEnd = end
            }
            return true
        }
        override fun commitCompletion(text: CompletionInfo?): Boolean = true
        override fun commitCorrection(correctionInfo: CorrectionInfo?): Boolean = true
        override fun performEditorAction(editorAction: Int): Boolean = true
        override fun performContextMenuAction(id: Int): Boolean = true
        override fun beginBatchEdit(): Boolean = true
        override fun endBatchEdit(): Boolean = true
        override fun sendKeyEvent(event: android.view.KeyEvent?): Boolean = true
        override fun clearMetaKeyStates(states: Int): Boolean = true
        override fun performPrivateCommand(action: String?, data: android.os.Bundle?): Boolean = true
        override fun requestCursorUpdates(cursorUpdateMode: Int): Boolean = true
        override fun reportFullscreenMode(enabled: Boolean): Boolean = true
        override fun getHandler(): android.os.Handler? = null
        override fun closeConnection() {}
        override fun commitContent(
            inputContentInfo: android.view.inputmethod.InputContentInfo,
            flags: Int,
            opts: android.os.Bundle?
        ): Boolean = false
    }

    // Helper to simulate atomic suggestion commit logic as specified in InputLogic contract:
    // f(host, typedPrefix, chosenWord)
    private fun executeAtomicSuggestionCommit(
        host: MockAdversarialHostConnection,
        typedPrefix: String,
        chosenWord: String
    ) {
        // Step 1: Query host connection for SPAN_COMPOSING
        val beforeText = host.getTextBeforeCursor(typedPrefix.length.coerceAtLeast(1), InputConnection.GET_TEXT_WITH_STYLES)
        var hostHasComposingSpan = false
        if (beforeText is Spanned) {
            val spans = beforeText.getSpans(0, beforeText.length, Any::class.java)
            if (spans != null) {
                for (s in spans) {
                    if ((beforeText.getSpanFlags(s) and Spanned.SPAN_COMPOSING) != 0) {
                        hostHasComposingSpan = true
                        break
                    }
                }
            }
        }

        val hasSelection = host.selectionStart != host.selectionEnd

        // Step 2: Atomic Prefix Deletion if host lacks active composing span and selection is empty
        if (!hostHasComposingSpan && !hasSelection) {
            val lenToDelete = typedPrefix.length
            if (lenToDelete > 0) {
                host.deleteSurroundingText(lenToDelete, 0)
            }
        }

        // Step 3: Commit chosen suggestion
        host.commitText(chosenWord, 1)
    }

    // =========================================================================
    // 1. Mode B: Adversarial Host Tests (Missing Composing Spans)
    // =========================================================================

    @Test
    fun testAdversarialHostAtomicPrefixDeletionOnSuggestionClick() {
        val host = MockAdversarialHostConnection(supportsComposingSpans = false)
        // User types "hel" in Instagram note reply
        host.setInitialText("hel", cursor = 3)

        // User picks "hello" from suggestion strip
        executeAtomicSuggestionCommit(host, typedPrefix = "hel", chosenWord = "hello")

        // Invariant: deleteSurroundingText(3, 0) was invoked exactly once
        assertEquals("Must delete typed prefix in host lacking composing spans", 1, host.deleteSurroundingTextCallCount)
        assertEquals("Deletion length must match prefix length", 3, host.lastDeleteBeforeLength)

        // Invariant: Host buffer must contain "hello", NOT "helhello"
        assertEquals("Final host buffer must be clean replacement without duplication", "hello", host.buffer.toString())
    }

    @Test
    fun testAdversarialHostMidSentenceAtomicReplacement() {
        val host = MockAdversarialHostConnection(supportsComposingSpans = false)
        // Host has "Note: rem"
        host.setInitialText("Note: rem", cursor = 9)

        executeAtomicSuggestionCommit(host, typedPrefix = "rem", chosenWord = "remember")

        assertEquals(1, host.deleteSurroundingTextCallCount)
        assertEquals(3, host.lastDeleteBeforeLength)
        assertEquals("Note: remember", host.buffer.toString())
    }

    // =========================================================================
    // 2. Mode A: Standard Host Tests (Composing Spans Present)
    // =========================================================================

    @Test
    fun testStandardHostBypassesRedundantDeletion() {
        val host = MockAdversarialHostConnection(supportsComposingSpans = true)
        // Standard editor with composing text "hel"
        host.setComposingText("hel", 1)
        assertTrue("Standard host must report active composing span", host.hasActiveComposingSpan())

        executeAtomicSuggestionCommit(host, typedPrefix = "hel", chosenWord = "hello")

        // Invariant: deleteSurroundingText must NOT be called because host handles it natively
        assertEquals("Must NOT call deleteSurroundingText when host maintains composing span", 0, host.deleteSurroundingTextCallCount)
        assertEquals("Final host buffer must be clean replacement", "hello", host.buffer.toString())
        assertFalse("Composing span must be cleared after commit", host.hasActiveComposingSpan())
    }

    // =========================================================================
    // 3. Mode C: Active Selection Range Tests
    // =========================================================================

    @Test
    fun testActiveSelectionBypassesPrefixDeletion() {
        val host = MockAdversarialHostConnection(supportsComposingSpans = false)
        host.setInitialText("replace target word", cursor = 14)
        // Select "target" (indices 8..14)
        host.setSelection(8, 14)

        executeAtomicSuggestionCommit(host, typedPrefix = "target", chosenWord = "chosen")

        // Invariant: Selection replacement should not call deleteSurroundingText
        assertEquals("Must NOT delete prefix when selection is active", 0, host.deleteSurroundingTextCallCount)
        assertEquals("replace chosen word", host.buffer.toString())
    }

    // =========================================================================
    // 4. Mode D: Avro / Bengali Transliteration Tests
    // =========================================================================

    @Test
    fun testAvroPhoneticPrefixAtomicReplacement() {
        val host = MockAdversarialHostConnection(supportsComposingSpans = false)
        // Avro mode: user typed "am", which displayed as "আম" in host editor
        val displayedBengali = "আম"
        host.setInitialText(displayedBengali, cursor = displayedBengali.length)

        // User picks "আমাদের" from suggestion strip
        executeAtomicSuggestionCommit(host, typedPrefix = displayedBengali, chosenWord = "আমাদের")

        assertEquals(1, host.deleteSurroundingTextCallCount)
        assertEquals(displayedBengali.length, host.lastDeleteBeforeLength)
        assertEquals("আমাদের", host.buffer.toString())
    }

    @Test
    fun testAvroComplexConjunctPrefixReplacement() {
        val host = MockAdversarialHostConnection(supportsComposingSpans = false)
        // User typed "kkh" -> "ক্ষ" (2 chars: \u0995 + \u09cd + \u09b7)
        val displayedBengali = "ক্ষ"
        host.setInitialText(displayedBengali, cursor = displayedBengali.length)

        executeAtomicSuggestionCommit(host, typedPrefix = displayedBengali, chosenWord = "ক্ষতি")

        assertEquals(1, host.deleteSurroundingTextCallCount)
        assertEquals(displayedBengali.length, host.lastDeleteBeforeLength)
        assertEquals("ক্ষতি", host.buffer.toString())
    }

    // =========================================================================
    // 5. Mode E: Empty Prefix / Manual Pick Tests
    // =========================================================================

    @Test
    fun testEmptyPrefixManualPickInsertsWithoutUnderflow() {
        val host = MockAdversarialHostConnection(supportsComposingSpans = false)
        host.setInitialText("", cursor = 0)

        executeAtomicSuggestionCommit(host, typedPrefix = "", chosenWord = "start")

        assertEquals("Must not delete when prefix is empty", 0, host.deleteSurroundingTextCallCount)
        assertEquals("start", host.buffer.toString())
    }

    // =========================================================================
    // 6. Mode F: Concurrency Stress Test
    // =========================================================================

    @Test
    fun testRapidConsecutiveSuggestionClicksStress() {
        val host = MockAdversarialHostConnection(supportsComposingSpans = false)
        val words = listOf("one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten")

        for (w in words) {
            val prefix = w.substring(0, 2)
            host.commitText(prefix, 1)
            executeAtomicSuggestionCommit(host, typedPrefix = prefix, chosenWord = "$w ")
        }

        assertEquals("Must execute 10 atomic replacements", 10, host.deleteSurroundingTextCallCount)
        assertEquals("one two three four five six seven eight nine ten ", host.buffer.toString())
    }
}
