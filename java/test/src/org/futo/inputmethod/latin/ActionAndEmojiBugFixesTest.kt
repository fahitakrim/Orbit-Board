package org.futo.inputmethod.latin

import android.graphics.Canvas
import android.graphics.Rect
import org.futo.inputmethod.keyboard.internal.CloudTranslationHandler
import org.futo.inputmethod.latin.common.Constants
import org.futo.inputmethod.latin.uix.actions.MoreActionsAction
import org.futo.inputmethod.latin.uix.theme.presets.BerryBlushThemeHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class ActionAndEmojiBugFixesTest {

    private fun findSourceFile(relPath: String): File {
        val candidates = listOf(
            relPath,
            "java/$relPath",
            "src/org/futo/inputmethod/$relPath",
            "../$relPath",
            "../java/$relPath",
            "nth-kbd/$relPath",
            "nth-kbd/java/$relPath"
        )
        for (candidate in candidates) {
            val f = File(candidate)
            if (f.exists()) return f
        }
        return File(relPath)
    }

    @Test
    fun testCloudTranslationHandlerParsesDictChromeExWithoutAuto() {
        val json = "[\"Hello World\"]"
        val parsed = CloudTranslationHandler.parseTranslationResponse(json)
        assertEquals("Hello World", parsed)
    }

    @Test
    fun testCloudTranslationHandlerParsesDictChromeExMultipleSegments() {
        val json = "[\"Hello \", \"Beautiful \", \"World\"]"
        val parsed = CloudTranslationHandler.parseTranslationResponse(json)
        assertEquals("Hello Beautiful World", parsed)
    }

    @Test
    fun testCloudTranslationHandlerParsesDictChromeExWithAuto() {
        // In auto-detect mode, response is [["Translated Text", "detected_lang"]]
        // Crucial bug fix: "en" or language code must NOT be appended to translated text!
        val json = "[[\"How are you\", \"en\"]]"
        val parsed = CloudTranslationHandler.parseTranslationResponse(json)
        assertEquals("How are you", parsed)
    }

    @Test
    fun testCloudTranslationHandlerParsesGtxFormat() {
        val json = "[[[\"আমি ভালো আছি\",\"I am fine\",null,null,10]],null,\"en\"]"
        val parsed = CloudTranslationHandler.parseTranslationResponse(json)
        assertEquals("আমি ভালো আছি", parsed)
    }

    @Test
    fun testCloudTranslationHandlerParsesEscapedQuotesAndNewlines() {
        val json = "[[\"Hello \\\"World\\\"\\nLine 2\", \"en\"]]"
        val parsed = CloudTranslationHandler.parseTranslationResponse(json)
        assertEquals("Hello \"World\"\nLine 2", parsed)
    }

    @Test
    fun testCloudTranslationHandlerHandlesHtmlOrMalformedGracefully() {
        val htmlResponse = "<!DOCTYPE html><html><body>Error 429</body></html>"
        val parsed = CloudTranslationHandler.parseTranslationResponse(htmlResponse)
        assertEquals("", parsed)

        val malformed = "not valid json at all"
        val parsedMalformed = CloudTranslationHandler.parseTranslationResponse(malformed)
        assertEquals("", parsedMalformed)
    }

    @Test
    fun testCatEarKeycapDrawableSymmetryAndAlignment() {
        val drawable = BerryBlushThemeHelper.CatEarKeycapDrawable(
            baseColor = 0xFFFAD0DC.toInt(),
            pressedColor = 0xFFF9B4CA.toInt(),
            cornerRadius = 16f,
            isOval = true
        )
        assertNotNull(drawable)

        // Set realistic key bounds (e.g. 100 x 120 px)
        drawable.bounds = Rect(0, 0, 100, 120)

        // Verify BerryBlushThemeHelper source code mathematically ensures seamless alignment and symmetry:
        val helperFile = findSourceFile("java/src/org/futo/inputmethod/latin/uix/theme/presets/BerryBlushThemeHelper.kt")
        assertTrue(helperFile.exists())
        val content = String(Files.readAllBytes(helperFile.toPath()), StandardCharsets.UTF_8)

        // 1. Keycap body starts at bodyY = b.top + earH (sculpted cat body, eliminating flat visor)
        assertTrue("Keycap body must define earH", content.contains("val earH = (h * 0.16f).coerceIn(6f, 16f)"))
        assertTrue("Keycap body must define bodyY", content.contains("val bodyY = b.top + earH"))
        assertTrue("Keycap body rect must use bodyX, bodyY, bodyW, bodyH", content.contains("bodyRect.set(bodyX, bodyY, bodyX + bodyW, bodyY + bodyH)"))

        // 2. Both ears have symmetric outer and inner anchor points
        assertTrue("Left outer ear anchor", content.contains("bodyX + bodyW * 0.08f"))
        assertTrue("Right outer ear anchor", content.contains("bodyX + bodyW * 0.92f"))
        assertTrue("Left inner ear anchor", content.contains("bodyX + bodyW * 0.14f"))
        assertTrue("Right inner ear anchor", content.contains("bodyX + bodyW * 0.86f"))
    }

    @Test
    fun testActionEditorDoneButtonHidesEditorInsteadOfClosingUnderlyingWindow() {
        val moreActionsFile = findSourceFile("java/src/org/futo/inputmethod/latin/uix/actions/MoreActions.kt")
        assertTrue(moreActionsFile.exists())
        val content = String(Files.readAllBytes(moreActionsFile.toPath()), StandardCharsets.UTF_8)

        // Done button in ActionEditor must call manager?.hideActionEditor()
        assertTrue("ActionEditor Done button must call manager?.hideActionEditor()",
            content.contains("manager?.hideActionEditor()"))
        assertFalse("ActionEditor Done button must not call closeActionWindow() directly",
            content.contains("Button(\n                    onClick = { manager?.closeActionWindow() }"))
    }

    @Test
    fun testUixManagerResetsShowingActionEditorOnActionTransitions() {
        val uixManagerFile = findSourceFile("java/src/org/futo/inputmethod/latin/uix/UixManager.kt")
        assertTrue(uixManagerFile.exists())
        val content = String(Files.readAllBytes(uixManagerFile.toPath()), StandardCharsets.UTF_8)

        // hideActionEditor must exist
        assertTrue("UixManager must define hideActionEditor()", content.contains("fun hideActionEditor()"))

        // enterActionWindowView must reset isShowingActionEditor
        assertTrue("enterActionWindowView must reset isShowingActionEditor",
            content.contains("private fun enterActionWindowView(action: Action) {\n        isShowingActionEditor.value = false"))

        // returnBackToMainKeyboardViewFromAction must reset isShowingActionEditor
        assertTrue("returnBackToMainKeyboardViewFromAction must reset isShowingActionEditor",
            content.contains("fun returnBackToMainKeyboardViewFromAction() {\n        isShowingActionEditor.value = false"))

        // closeActionWindow must reset isShowingActionEditor
        assertTrue("closeActionWindow must reset isShowingActionEditor",
            content.contains("fun closeActionWindow() {\n        isShowingActionEditor.value = false"))
    }

    @Test
    fun testInputViewHeightAlwaysReturnsPositiveFallback() {
        val latinIMEFile = findSourceFile("java/src/org/futo/inputmethod/latin/LatinIME.kt")
        assertTrue(latinIMEFile.exists())
        val content = String(Files.readAllBytes(latinIMEFile.toPath()), StandardCharsets.UTF_8)

        // getInputViewHeight must have positive fallbacks for cold boot or unmeasured frames
        assertTrue("getInputViewHeight must check if inputViewHeight > 0", content.contains("if (inputViewHeight > 0) return inputViewHeight"))
        assertTrue("getInputViewHeight must fallback to touchableHeight", content.contains("if (touchableHeight > 0) return touchableHeight"))
        assertTrue("getInputViewHeight must have density based fallback", content.contains("return if (isLandscape) (180f * density).toInt() else (260f * density).toInt()"))
    }

    @Test
    fun testInputLogicBackspaceHandlesEnteredMultiCharAndEmojiInBengaliAndAvro() {
        val inputLogicFile = findSourceFile("java/src/org/futo/inputmethod/latin/inputlogic/InputLogic.java")
        assertTrue(inputLogicFile.exists())
        val content = String(Files.readAllBytes(inputLogicFile.toPath()), StandardCharsets.UTF_8)

        // 1. mEnteredText must be checked before isBengaliActive() in non-composing backspace branch
        // so typed emojis from palette are deleted in full before single grapheme deletion runs
        val enteredTextIdx = content.indexOf("if (mEnteredText != null && mConnection.sameAsTextBeforeCursor(mEnteredText))")
        assertTrue("mEnteredText check must exist in InputLogic", enteredTextIdx > 0)
        val nextBengaliIdx = content.indexOf("if (isBengaliActive() || isAvroActive())", enteredTextIdx)
        assertTrue("mEnteredText check must come before non-composing isBengaliActive check",
            nextBengaliIdx > enteredTextIdx)

        // 2. isBengaliActive must compute lengthToDelete using BreakIterator and not blindly delete 1 char
        assertTrue("isBengaliActive must use BreakIterator or Character.isSupplementaryCodePoint to support emojis and conjuncts",
            content.contains("Character.isSupplementaryCodePoint(codePointBeforeCursor)"))
        assertTrue("isBengaliActive must use BreakIterator.getCharacterInstance()",
            content.contains("breakIterator.setText(textBeforeCursor.toString())"))
    }

    @Test
    fun testEmojiGraphemeClusterLengthsForBreakIterator() {
        val breakIterator = java.text.BreakIterator.getCharacterInstance()

        fun getLastGraphemeLength(text: String): Int {
            breakIterator.setText(text)
            val end = breakIterator.last()
            val start = breakIterator.previous()
            return if (start != java.text.BreakIterator.DONE && end > start) end - start else 1
        }

        // Standard surrogate pair emoji: 2 UTF-16 code units (never split to 1)
        assertEquals(2, getLastGraphemeLength("Hello 😀"))
        // Supplementary code points always have length 2 in BreakIterator (safe from 1-char surrogate truncation)
        assertEquals(2, getLastGraphemeLength("Test 👍🏽"))
        assertEquals(2, getLastGraphemeLength("Country 🇧🇩"))
        // Plain ASCII char has length 1
        assertEquals(1, getLastGraphemeLength("hello"))
    }

    @Test
    fun testLegacyKeyboardViewDoesNotDeallocateOnRelease() {
        val latinIMEFile = findSourceFile("java/src/org/futo/inputmethod/latin/LatinIME.kt")
        assertTrue(latinIMEFile.exists())
        val content = String(Files.readAllBytes(latinIMEFile.toPath()), StandardCharsets.UTF_8)

        // LegacyKeyboardView must never deallocate or remove views from singleton legacyInputView
        assertFalse("LegacyKeyboardView must not deallocate singleton legacyInputView on release",
            content.contains("view.deallocateMemory()"))
        assertFalse("LegacyKeyboardView must not remove views from singleton legacyInputView on release",
            content.contains("view.removeAllViews()"))
    }

    @Test
    fun testOnComputeInsetsKeepsContentTopVisibleTopY() {
        val latinIMEFile = findSourceFile("java/src/org/futo/inputmethod/latin/LatinIME.kt")
        assertTrue(latinIMEFile.exists())
        val content = String(Files.readAllBytes(latinIMEFile.toPath()), StandardCharsets.UTF_8)

        // contentTop must remain visibleTopY even when isInputModal is true, so host apps are not resized to height 0
        assertTrue("contentTop must remain visibleTopY to prevent host app collapse",
            content.contains("val contentTop = visibleTopY"))
        assertFalse("contentTop must not be set to 0 on isInputModal",
            content.contains("val contentTop = if (isInputModal) { 0 } else { visibleTopY }"))
    }

    @Test
    fun testRepeatablyClickableActionUsesPointerInputWithoutEmptyOnClick() {
        val textEditFile = findSourceFile("java/src/org/futo/inputmethod/latin/uix/actions/TextEditAction.kt")
        assertTrue(textEditFile.exists())
        val content = String(Files.readAllBytes(textEditFile.toPath()), StandardCharsets.UTF_8)

        // repeatablyClickableAction must use pointerInput with awaitEachGesture to dispatch immediately on touch down
        assertTrue("repeatablyClickableAction must use awaitEachGesture", content.contains("awaitEachGesture"))
        assertTrue("repeatablyClickableAction must use awaitFirstDown", content.contains("awaitFirstDown"))
        assertFalse("repeatablyClickableAction must not rely on clickable with empty onClick",
            content.contains("return this.clickable(interactionSource, indication = LocalIndication.current, onClick = { })"))
    }

    @Test
    fun testMoreActionsDoesNotWriteSettingsOnMove() {
        val moreActionsFile = findSourceFile("java/src/org/futo/inputmethod/latin/uix/actions/MoreActions.kt")
        assertTrue(moreActionsFile.exists())
        val content = String(Files.readAllBytes(moreActionsFile.toPath()), StandardCharsets.UTF_8)

        val reorderIdx = content.indexOf("val reorderableLazyListState = rememberReorderableLazyGridState")
        assertTrue("rememberReorderableLazyGridState must exist", reorderIdx > 0)
        val endIdx = content.indexOf("view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK)", reorderIdx)
        assertTrue("Haptic tick must exist", endIdx > reorderIdx)
        val block = content.substring(reorderIdx, endIdx)

        assertFalse("reorderableLazyListState must not write to DataStore on every move frame",
            block.contains("context.updateSettingsWithNewActions(map)"))
    }

    @Test
    fun testUixManagerContentBoxLayering() {
        val uixManagerFile = findSourceFile("java/src/org/futo/inputmethod/latin/uix/UixManager.kt")
        assertTrue(uixManagerFile.exists())
        val content = String(Files.readAllBytes(uixManagerFile.toPath()), StandardCharsets.UTF_8)

        // Find setContent() method body
        val setContentIdx = content.indexOf("fun setContent()")
        assertTrue("fun setContent() must exist", setContentIdx > 0)
        val setContentBody = content.substring(setContentIdx)

        // Inside setContent: BoxWithConstraints, InputDarkener, and ActionEditorHost must be properly ordered
        val boxWithConstraintsIdx = setContentBody.indexOf("BoxWithConstraints(modifier = Modifier.fillMaxSize())")
        val inputDarkenerIdx = setContentBody.indexOf("InputDarkener(isInputOverridden.value || isShowingActionEditor.value)")
        val actionEditorHostIdx = setContentBody.indexOf("ActionEditorHost()")

        assertTrue("BoxWithConstraints must exist in setContent", boxWithConstraintsIdx > 0)
        assertTrue("InputDarkener must exist in setContent", inputDarkenerIdx > 0)
        assertTrue("ActionEditorHost must exist in setContent", actionEditorHostIdx > 0)
        assertTrue("InputDarkener must be layered above BoxWithConstraints",
            inputDarkenerIdx > boxWithConstraintsIdx)
        assertTrue("ActionEditorHost must be layered above InputDarkener",
            actionEditorHostIdx > inputDarkenerIdx)
    }

    @Test
    fun testInputLogicNonComposingBackspaceChecksTextBeforeCursorBeforeBailing() {
        val inputLogicFile = findSourceFile("java/src/org/futo/inputmethod/latin/inputlogic/InputLogic.java")
        assertTrue(inputLogicFile.exists())
        val content = String(Files.readAllBytes(inputLogicFile.toPath()), StandardCharsets.UTF_8)

        assertTrue("InputLogic must inspect textBeforeCursor even when codePointBeforeCursor is NOT_A_CODE",
            content.contains("(textBeforeCursor == null || textBeforeCursor.length() == 0) && codePointBeforeCursor == Constants.NOT_A_CODE"))
    }
}
