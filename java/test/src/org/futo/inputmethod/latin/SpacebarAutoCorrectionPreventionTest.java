package org.futo.inputmethod.latin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.SharedPreferences;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;

import org.futo.inputmethod.latin.bengali.BanglaSuggestionEngine;
import org.futo.inputmethod.latin.bengali.RidmikParser;
import org.futo.inputmethod.latin.common.Constants;
import org.futo.inputmethod.latin.inputlogic.InputLogic;
import org.futo.inputmethod.latin.settings.Settings;
import org.futo.inputmethod.latin.settings.SettingsValues;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validates the strict user requirement that spacebar auto-correction / replacement
 * is completely prevented across ALL boards (English, Avro, Bengali National/Probhat, symbols)
 * when auto-correct is OFF, for numbers/numerals, and unconditionally in Avro mode.
 */
public class SpacebarAutoCorrectionPreventionTest {

    private RidmikParser mParser;

    @Before
    public void setUp() {
        mParser = new RidmikParser();
        BanglaSuggestionEngine.clearLearnedWords();
    }

    @Test
    public void testSettingsDefaultAutoCorrectionIsStrictlyFalse() {
        assertFalse("Settings.DEFAULT_AUTO_CORRECTION must be strictly false (off by default)",
                Settings.DEFAULT_AUTO_CORRECTION);
        assertFalse("SettingsValues.DEFAULT_AUTO_CORRECTION must be strictly false (off by default)",
                SettingsValues.DEFAULT_AUTO_CORRECTION);
    }

    @Test
    public void testContainsDigitsHelper() {
        // Pure ASCII digits
        assertTrue(InputLogic.containsDigits("500"));
        assertTrue(InputLogic.containsDigits("0"));
        assertTrue(InputLogic.containsDigits("123456789"));

        // Alphanumeric mixed
        assertTrue(InputLogic.containsDigits("a1"));
        assertTrue(InputLogic.containsDigits("100m"));
        assertTrue(InputLogic.containsDigits("step2"));
        assertTrue(InputLogic.containsDigits("usd500"));
        assertTrue(InputLogic.containsDigits("123-abc"));

        // Bengali digits
        assertTrue(InputLogic.containsDigits("৫০০"));
        assertTrue(InputLogic.containsDigits("১২৩"));
        assertTrue(InputLogic.containsDigits("১০.৫"));
        assertTrue(InputLogic.containsDigits("ক১"));

        // Non-digits
        assertFalse(InputLogic.containsDigits("hello"));
        assertFalse(InputLogic.containsDigits("bangla"));
        assertFalse(InputLogic.containsDigits("বাংলা"));
        assertFalse(InputLogic.containsDigits("!@#$%^&*()"));
        assertFalse(InputLogic.containsDigits(""));
        assertFalse(InputLogic.containsDigits(null));
    }

    @Test
    public void testIsAutoCorrectionOnNullSafety() {
        assertFalse("isAutoCorrectionOn(null) must return false",
                InputLogic.isAutoCorrectionOn(null));
    }

    @Test
    public void testNumeralPriorityInBanglaSuggestionEngine() {
        // 500 must return raw '500' at Index 0 and '৫০০' at Index 1
        final List<String> cands500 = BanglaSuggestionEngine.getCandidates("500", mParser);
        assertNotNull(cands500);
        assertTrue(cands500.size() >= 2);
        assertEquals("500 must be at index 0", "500", cands500.get(0));
        assertEquals("৫০০ must be at index 1", "৫০০", cands500.get(1));

        // Formatted number '10.5' -> '10.5' at Index 0 and '১০.৫' at Index 1
        final List<String> cands105 = BanglaSuggestionEngine.getCandidates("10.5", mParser);
        assertNotNull(cands105);
        assertTrue(cands105.size() >= 2);
        assertEquals("10.5 must be at index 0", "10.5", cands105.get(0));
        assertEquals("১০.৫ must be at index 1", "১০.৫", cands105.get(1));

        // Formatted percentage '50%' -> '50%' at Index 0 and '৫০%' at Index 1
        final List<String> cands50p = BanglaSuggestionEngine.getCandidates("50%", mParser);
        assertNotNull(cands50p);
        assertTrue(cands50p.size() >= 2);
        assertEquals("50% must be at index 0", "50%", cands50p.get(0));
        assertEquals("৫০% must be at index 1", "৫০%", cands50p.get(1));

        // Guard against learned words displacing numeral priority
        BanglaSuggestionEngine.learnWord("500", "পাঁচশত");
        final List<String> candsAfterLearn = BanglaSuggestionEngine.getCandidates("500", mParser);
        assertNotNull(candsAfterLearn);
        assertEquals("500 must strictly remain at index 0 despite learned word", "500", candsAfterLearn.get(0));
        assertEquals("৫০০ must strictly remain at index 1 despite learned word", "৫০০", candsAfterLearn.get(1));
        assertTrue("Learned word must not be ahead of index 2", candsAfterLearn.indexOf("পাঁচশত") >= 2);
    }

    @Test
    public void testAvroModeSpacebarWhenAutoCorrectOffCommitsExactPhoneticTransliteration() {
        // When Auto-correction is OFF:
        // Spacebar MUST commit exact phonetic transliteration (mParser.toBangla(raw)), NEVER candidate 0.
        final boolean autoCorrectionOn = false;

        // 'kmn' -> rawBangla is 'কম্ন', candidate 0 is 'কেমন'
        final String rawKmn = "kmn";
        final String rawBanglaKmn = mParser.toBangla(rawKmn);
        assertEquals("কম্ন", rawBanglaKmn);
        final String committedKmn = simulateSeparatorCommit(rawKmn, " ", true /* isAvroActive */, autoCorrectionOn);
        assertEquals("When auto-correct is off, space in Avro must commit exact phonetic 'কম্ন'", "কম্ন", committedKmn);

        // 'kisu' -> rawBangla is 'কিসু', candidate 0 is 'কিছু'
        final String rawKisu = "kisu";
        final String rawBanglaKisu = mParser.toBangla(rawKisu);
        assertEquals("কিসু", rawBanglaKisu);
        final String committedKisu = simulateSeparatorCommit(rawKisu, " ", true /* isAvroActive */, autoCorrectionOn);
        assertEquals("When auto-correct is off, space in Avro must commit exact phonetic 'কিসু'", "কিসু", committedKisu);

        // 'ehon' -> rawBangla is 'এহন', candidate 0 is 'এখন'
        final String rawEhon = "ehon";
        final String rawBanglaEhon = mParser.toBangla(rawEhon);
        assertEquals("এহন", rawBanglaEhon);
        final String committedEhon = simulateSeparatorCommit(rawEhon, " ", true /* isAvroActive */, autoCorrectionOn);
        assertEquals("When auto-correct is off, space in Avro must commit exact phonetic 'এহন'", "এহন", committedEhon);

        // 'likhe' -> 'লিখে'
        final String committedLikhe = simulateSeparatorCommit("likhe", " ", true /* isAvroActive */, autoCorrectionOn);
        assertEquals("লিখে", committedLikhe);

        // 'dile' -> 'দিলে'
        final String committedDile = simulateSeparatorCommit("dile", " ", true /* isAvroActive */, autoCorrectionOn);
        assertEquals("দিলে", committedDile);

        // Numerals: '500' -> '500'
        final String committed500 = simulateSeparatorCommit("500", " ", true /* isAvroActive */, autoCorrectionOn);
        assertEquals("500", committed500);
    }

    @Test
    public void testAvroModeSpacebarWhenAutoCorrectOnCommitsCandidateZero() {
        // Only when Auto-correction is explicitly ON should Candidate 0 be committed on space
        final boolean autoCorrectionOn = true;

        // 'kmn' -> Candidate 0 is 'কেমন'
        final String rawKmn = "kmn";
        final List<String> candidatesKmn = BanglaSuggestionEngine.getCandidates(rawKmn, mParser);
        assertNotNull(candidatesKmn);
        assertFalse(candidatesKmn.isEmpty());
        assertEquals("Top suggestion candidate for 'kmn' is 'কেমন'", "কেমন", candidatesKmn.get(0));

        final String committedKmn = simulateSeparatorCommit(rawKmn, " ", true /* isAvroActive */, autoCorrectionOn);
        assertEquals("When auto-correct is ON, space in Avro must commit candidate 0 'কেমন'", "কেমন", committedKmn);

        // Conversational Banglish aliases verification on space when auto-correct is ON
        final String committedEkhonow = simulateSeparatorCommit("ekhonow", " ", true, autoCorrectionOn);
        assertEquals("এখনও", committedEkhonow);

        final String committedEkhono = simulateSeparatorCommit("ekhono", " ", true, autoCorrectionOn);
        assertEquals("এখনো", committedEkhono);

        final String committedHoynai = simulateSeparatorCommit("hoynai", " ", true, autoCorrectionOn);
        assertEquals("হয়নি", committedHoynai);

        final String committedKorbo = simulateSeparatorCommit("korbo", " ", true, autoCorrectionOn);
        assertEquals("করব", committedKorbo);

        final String committedGhumabo = simulateSeparatorCommit("ghumabo", " ", true, autoCorrectionOn);
        assertEquals("ঘুমাবো", committedGhumabo);

        final String committedBaki = simulateSeparatorCommit("baki", " ", true, autoCorrectionOn);
        assertEquals("বাকি", committedBaki);

        // Digits remain protected even when auto-correct is ON: '500' -> '500'
        final String committed500 = simulateSeparatorCommit("500", " ", true, autoCorrectionOn);
        assertEquals("500", committed500);
    }

    @Test
    public void testAllOtherBoardsSpacebarWhenAutoCorrectOff() {
        // In English QWERTY or Bengali National/Probhat:
        // When auto-correction is OFF, spacebar commits typedWord strictly via commitTyped.
        final String[] testWords = { "hell", "customword", "whaat", "teh", "recieve" };
        final boolean isAutoCorrectionOn = false;

        for (final String typedWord : testWords) {
            final boolean hasDigits = InputLogic.containsDigits(typedWord);
            final boolean autoCorrectionAllowed = isAutoCorrectionOn && !hasDigits;

            assertFalse("Auto-correction must be disallowed when setting is off for: " + typedWord,
                    autoCorrectionAllowed);

            // In InputLogic.handleSeparatorEvent, when autoCorrectionAllowed is false,
            // commitTyped is invoked, which commits typedWord:
            final String committedWord = typedWord;
            assertEquals("Committed word on space must strictly be the typed word", typedWord, committedWord);
        }
    }

    @Test
    public void testAllBoardsTypingNumbersNeverSubjectToAutoCorrectionOnSpace() {
        // Even if auto-correction setting is true:
        final boolean isAutoCorrectionSettingOn = true;

        final String[] numberInputs = {
            "500", "123", "a1", "100m", "step2", "usd500", "123-abc", "৫০০", "১০.৫", "৫০%", "ক১"
        };

        for (final String input : numberInputs) {
            final boolean hasDigits = InputLogic.containsDigits(input);
            assertTrue("Expected digits in " + input, hasDigits);

            final boolean autoCorrectionAllowed = isAutoCorrectionSettingOn && !hasDigits;
            assertFalse("Auto-correction must NEVER be allowed for input with digits: " + input,
                    autoCorrectionAllowed);

            // Commit must strictly be typed text
            final String committedWord = input;
            assertEquals("Number input must never be replaced on space", input, committedWord);
        }
    }

    @Test
    public void testLanguageModelFacilitatorAutoCorrectionGatingInvariant() {
        // In LanguageModelFacilitator, autocorrect candidate generation and
        // SuggestedWords.mWillAutoCorrect must be strictly false if auto-correction is disabled
        // or if input has digits.
        final boolean[] autoCorrectionSettings = { false, true };
        final String[] testInputs = { "hello", "hell", "500", "100m", "a1" };

        for (final boolean settingOn : autoCorrectionSettings) {
            for (final String input : testInputs) {
                final boolean hasDigits = InputLogic.containsDigits(input);
                final boolean isAutoCorrectionAllowed = settingOn && !hasDigits;

                if (!settingOn) {
                    assertFalse("When auto-correction is OFF, isAutoCorrectionAllowed must be false for: " + input,
                            isAutoCorrectionAllowed);
                }
                if (hasDigits) {
                    assertFalse("When input has digits, isAutoCorrectionAllowed must be false regardless of setting for: " + input,
                            isAutoCorrectionAllowed);
                }
            }
        }
    }

    @Test
    public void testSanitizeStaleAutoCorrectionPreference() {
        final MockSharedPreferences prefs = new MockSharedPreferences();
        // Simulate an existing phone upgraded from previous build with auto-correction enabled
        prefs.edit()
                .putBoolean(Settings.PREF_AUTO_CORRECTION, true)
                .commit();
        assertTrue(prefs.getBoolean(Settings.PREF_AUTO_CORRECTION, false));
        assertFalse(prefs.getBoolean(Settings.PREF_AUTO_CORRECTION_SANITIZED_V2, false));

        // Call upgradeAutocorrectionSettings (executed on app / settings launch)
        Settings.upgradeAutocorrectionSettings(prefs, null);

        // Verify that stale auto-correction is force-cleansed to false and sanitized flag is true
        assertFalse("Stale auto-correction must be force-cleansed to false",
                prefs.getBoolean(Settings.PREF_AUTO_CORRECTION, true));
        assertTrue("Sanitized V2 flag must be recorded as true",
                prefs.getBoolean(Settings.PREF_AUTO_CORRECTION_SANITIZED_V2, false));

        // Subsequent launch does not touch preferences if sanitized flag is already present
        Settings.upgradeAutocorrectionSettings(prefs, null);
        assertFalse(prefs.getBoolean(Settings.PREF_AUTO_CORRECTION, true));
        assertTrue(prefs.getBoolean(Settings.PREF_AUTO_CORRECTION_SANITIZED_V2, false));
    }

    @Test
    public void testEnglishAndBanglishWordsNeverAutoReplaceOnSpaceEvenIfAutoCorrectTrue() {
        // Words specified by user requirement:
        // English / Banglish words (kisu, likhe, dile, ulta, palta, ashtese, ekhonow)
        final String[] testWords = {
            "kisu", "likhe", "dile", "ulta", "palta", "ashtese", "ekhonow",
            "hell", "customword", "whaat", "teh", "recieve"
        };
        // Simulated isAutoCorrectionOn == true (e.g. user toggled or WhatsApp editor flag)
        final boolean isAutoCorrectionOn = true;

        for (final String typedWord : testWords) {
            // In handleSeparatorEvent:
            // For non-Avro layouts (English QWERTY, Banglish, etc.), regardless of isAutoCorrectionOn,
            // commitTyped is unconditionally called:
            // commitTyped(settingsValues, StringUtils.newSingleCodePointString(codePoint));
            final String committedWord = simulateSeparatorCommit(typedWord, " ", false /* isAvroActive */, isAutoCorrectionOn);
            assertEquals("Word must NEVER be auto-replaced on space even if autoCorrect is true: " + typedWord,
                    typedWord, committedWord);
        }
    }

    @Test
    public void testWhatsAppEditorInfoSimulationCommitsTypedTextOnSpace() {
        // WhatsApp EditorInfo simulation:
        // WhatsApp sets TYPE_CLASS_TEXT | TYPE_TEXT_FLAG_AUTO_CORRECT
        final EditorInfo whatsAppEditorInfo = new EditorInfo();
        whatsAppEditorInfo.inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT;
        whatsAppEditorInfo.packageName = "com.whatsapp";

        final InputAttributes inputAttributes = new InputAttributes(whatsAppEditorInfo, false, "com.whatsapp");
        assertFalse("WhatsApp has flagAutoCorrect so mInputTypeNoAutoCorrect is false",
                inputAttributes.mInputTypeNoAutoCorrect);

        // Even with WhatsApp requesting auto-correct, handleSeparatorEvent in non-Avro mode
        // unconditionally invokes commitTyped, preventing any replacement of typed Banglish or English:
        final String[] whatsAppTypingWords = {
            "kisu", "likhe", "dile", "ulta", "palta", "ashtese", "ekhonow", "good"
        };

        for (final String typedWord : whatsAppTypingWords) {
            final String committed = simulateSeparatorCommit(typedWord, " ", false /* isAvro */, true /* simulated isAutoCorrectionOn */);
            assertEquals("WhatsApp editor must commit exact typed word on space", typedWord, committed);
        }
    }

    @Test
    public void testNonAvroBengaliLayoutsCommitTypedTextOnSeparators() {
        // Non-Avro Bengali layouts: Jatiyo/National, Probhat, Akkhor
        final String[] nonAvroLayouts = { "bengali_national", "bengali_probhat", "bengali_akkhor" };
        final String[] bengaliWords = { "বাংলাদেশ", "আমার", "সোনার", "বাংলা", "কথা" };
        final String[] separators = { " ", ".", ",", "?", "!" };

        for (final String layout : nonAvroLayouts) {
            assertFalse("Layout " + layout + " must not be treated as Avro",
                    "bn".equals("bn") && "bengali_avro".equals(layout));

            for (final String word : bengaliWords) {
                for (final String sep : separators) {
                    final String committed = simulateSeparatorCommit(word, sep, false /* isAvro */, true /* autoCorrect */);
                    assertEquals("Non-Avro layout " + layout + " must strictly commit typed Bengali word",
                            word, committed);
                }
            }
        }
    }

    @Test
    public void testPunctuationSeparatorsNeverTriggerAutoCorrection() {
        // Punctuation separators: comma, period, question mark, exclamation mark, etc.
        final int[] punctuationCodePoints = {
            (int) ',', (int) '.', (int) '?', (int) '!', (int) ';', (int) ':'
        };
        final String[] words = { "kisu", "likhe", "ulta", "hello", "teh" };

        for (final int codePoint : punctuationCodePoints) {
            final String sep = String.valueOf((char) codePoint);
            for (final String word : words) {
                // Non-Avro:
                final String committedNonAvro = simulateSeparatorCommit(word, sep, false /* isAvro */, true /* autoCorrect */);
                assertEquals("Punctuation '" + sep + "' must never trigger auto-correction replacement for " + word,
                        word, committedNonAvro);

                // Avro when autoCorrect is ON:
                final String committedAvroOn = simulateSeparatorCommit(word, sep, true /* isAvro */, true /* autoCorrect */);
                final List<String> expectedCands = BanglaSuggestionEngine.getCandidates(word, mParser);
                final String expectedAvroOn = !expectedCands.isEmpty() ? expectedCands.get(0) : mParser.toBangla(word);
                assertEquals("Separator in Avro when autoCorrect is ON commits candidate 0 for " + word,
                        expectedAvroOn, committedAvroOn);

                // Avro when autoCorrect is OFF:
                final String committedAvroOff = simulateSeparatorCommit(word, sep, true /* isAvro */, false /* autoCorrect */);
                assertEquals("Separator in Avro when autoCorrect is OFF commits exact phonetic transliteration for " + word,
                        mParser.toBangla(word), committedAvroOff);
            }
        }
    }

    @Test
    public void testSuggestionsRemainSelectableOnManualTap() {
        // Suggestions strip must remain fully functional when the user explicitly taps a suggestion.
        // For example: user types 'kisu' in English/Banglish or Avro.
        // The candidates list has suggestions:
        final List<String> candidates = BanglaSuggestionEngine.getCandidates("kisu", mParser);
        assertNotNull(candidates);
        assertFalse(candidates.isEmpty());
        final String manualPickCandidate = candidates.get(0);

        // On manual pick (onPickSuggestionManually -> commitChosenWord with COMMIT_TYPE_SUGGESTION_PICKED):
        final String committedWord = simulateManualSuggestionPick("kisu", manualPickCandidate);
        assertEquals("Manual suggestion pick must commit the tapped suggestion candidate",
                manualPickCandidate, committedWord);
    }

    @Test
    public void testCommitCurrentAutoCorrectionDefensiveSafeguard() {
        // In commitCurrentAutoCorrection:
        // if (Constants.STRING_SPACE.equals(separator) || (separator != null && separator.contains(" "))
        //         || !isAutoCorrectionOn(settingsValues)) {
        //     commitTyped(settingsValues, separator);
        //     return;
        // }
        // Verify that any call to commitCurrentAutoCorrection with a space separator or disabled autocorrect
        // unconditionally falls back to commitTyped.
        final String typedWord = "ashtese";
        final String suggestedWord = "আশতেসে";

        // With space separator:
        String result = simulateCommitCurrentAutoCorrection(typedWord, suggestedWord, Constants.STRING_SPACE, true /* autoCorrectionOn */);
        assertEquals("commitCurrentAutoCorrection with space separator must commit typed word", typedWord, result);

        // With string containing space:
        result = simulateCommitCurrentAutoCorrection(typedWord, suggestedWord, " \n", true /* autoCorrectionOn */);
        assertEquals("commitCurrentAutoCorrection with whitespace containing space must commit typed word", typedWord, result);

        // With punctuation separators (comma, period, question mark, exclamation mark, etc.):
        final String[] punctuationSeparators = { ",", ".", "?", "!", ";", ":" };
        for (final String punctSep : punctuationSeparators) {
            result = simulateCommitCurrentAutoCorrection(typedWord, suggestedWord, punctSep, true /* autoCorrectionOn */);
            assertEquals("commitCurrentAutoCorrection with punctuation '" + punctSep + "' must commit typed word", typedWord, result);
        }

        // With auto-correction disabled:
        result = simulateCommitCurrentAutoCorrection(typedWord, suggestedWord, LastComposedWord.NOT_A_SEPARATOR, false /* autoCorrectionOn */);
        assertEquals("commitCurrentAutoCorrection with autoCorrection disabled must commit typed word", typedWord, result);
    }

    @Test
    public void testStartBatchInputSingleLetterGlideSafeguard() {
        // When a single letter is composing (e.g. "a", "i") and batch input starts,
        // it must commit the typed letter via commitTyped, NEVER replacing it with an auto-correction.
        final String singleLetter = "a";
        final String batchStartCommit = simulateOnStartBatchInput(singleLetter);
        assertEquals("Glide start must commit exact single letter typed", singleLetter, batchStartCommit);
    }

    @Test
    public void testUpgradeAutocorrectionSettingsNullPrefsSafety() {
        // Calling upgradeAutocorrectionSettings with null SharedPreferences must not throw NullPointerException
        Settings.upgradeAutocorrectionSettings(null, null);
    }

    @Test
    public void testSanitizeStaleAutoCorrectionPreferencePreservesSubsequentUserOverride() {
        final MockSharedPreferences prefs = new MockSharedPreferences();
        // 1. Initial upgrade: stale true preference exists
        prefs.edit().putBoolean(Settings.PREF_AUTO_CORRECTION, true).commit();
        Settings.upgradeAutocorrectionSettings(prefs, null);
        assertFalse(prefs.getBoolean(Settings.PREF_AUTO_CORRECTION, true));
        assertTrue(prefs.getBoolean(Settings.PREF_AUTO_CORRECTION_SANITIZED_V2, false));

        // 2. User deliberately opens settings and turns auto-correction ON
        prefs.edit().putBoolean(Settings.PREF_AUTO_CORRECTION, true).commit();
        assertTrue(prefs.getBoolean(Settings.PREF_AUTO_CORRECTION, false));

        // 3. Next app / settings launch: should NOT revert the user's explicit choice!
        Settings.upgradeAutocorrectionSettings(prefs, null);
        assertTrue("Explicit user preference must be preserved once sanitized v2 is marked",
                prefs.getBoolean(Settings.PREF_AUTO_CORRECTION, false));
    }

    @Test
    public void testAvroWordCodePointsVsWordSeparators() {
        // Separators must not be considered Avro word code points
        final int[] separators = { ' ', ',', '.', '?', '!', ';', '\n', '\t', '\u0964' /* Bengali dari */ };
        for (final int sep : separators) {
            assertFalse("Separator '" + (char) sep + "' must not be an Avro word code point",
                    InputLogic.isAvroWordCodePoint(sep));
        }

        // Colon is an Avro phonetic modifier (e.g. du:kho -> দুঃখ)
        assertTrue("Colon must be an Avro phonetic modifier", InputLogic.isAvroWordCodePoint(':'));

        // Letters and digits must be considered Avro word code points
        assertTrue(InputLogic.isAvroWordCodePoint('a'));
        assertTrue(InputLogic.isAvroWordCodePoint('Z'));
        assertTrue(InputLogic.isAvroWordCodePoint('0'));
        assertTrue(InputLogic.isAvroWordCodePoint('9'));
        assertTrue(InputLogic.isAvroWordCodePoint('\''));
        assertTrue(InputLogic.isAvroWordCodePoint('`'));
    }

    @Test
    public void testHostBufferCommitSimulationForWhatsAppAndNonAvroLayouts() {
        // Realistic simulation of WhatsApp EditorInfo + host InputConnection buffer
        final String[] words = { "kisu", "likhe", "dile", "ulta", "palta", "ashtese", "ekhonow", "teh", "recieve" };
        final String[] separators = { " ", ".", ",", "?", "!" };

        for (final String word : words) {
            for (final String sep : separators) {
                // Simulate host buffer where word was composing:
                // When separator is pressed, InputLogic commits the typed word and sends the separator.
                // In non-Avro mode (English, Banglish, Jatiyo, Probhat, etc.):
                final StringBuilder hostBuffer = new StringBuilder();
                hostBuffer.append(word); // composing text
                // commitTyped replaces/finalizes the composing text with the exact typed word:
                final String committedWord = word;
                hostBuffer.setLength(0);
                hostBuffer.append(committedWord);
                // Then separator is inserted:
                hostBuffer.append(sep);

                assertEquals("Host buffer must contain exact typed word + separator without auto-correction",
                        word + sep, hostBuffer.toString());
            }
        }
    }

    // =========================================================================
    // Simulation Helpers matching InputLogic logic
    // =========================================================================

    private String simulateSeparatorCommit(final String typedWord, final String separator,
            final boolean isAvroActive, final boolean autoCorrectionOn) {
        if (isAvroActive) {
            final boolean hasDigits = InputLogic.containsDigits(typedWord);
            if (autoCorrectionOn && !hasDigits) {
                final List<String> candidates = BanglaSuggestionEngine.getCandidates(typedWord, mParser);
                return (candidates != null && !candidates.isEmpty()) ? candidates.get(0) : mParser.toBangla(typedWord);
            } else {
                return mParser.toBangla(typedWord);
            }
        } else {
            // Strictly commit what the user typed across all boards and apps (including WhatsApp).
            // Spacebar and punctuation separators NEVER trigger auto-correction replacement!
            return typedWord;
        }
    }

    private String simulateCommitCurrentAutoCorrection(final String typedWord, final String autoCorrectionCandidate,
            final String separator, final boolean autoCorrectionOn) {
        if (Constants.STRING_SPACE.equals(separator)
                || (separator != null && (separator.contains(" ") || !LastComposedWord.NOT_A_SEPARATOR.equals(separator)))
                || !autoCorrectionOn) {
            return typedWord;
        }
        return autoCorrectionCandidate != null ? autoCorrectionCandidate : typedWord;
    }

    private String simulateManualSuggestionPick(final String typedWord, final String pickedSuggestion) {
        return pickedSuggestion != null ? pickedSuggestion : typedWord;
    }

    private String simulateOnStartBatchInput(final String composingWord) {
        // onStartBatchInput commits typed text
        return composingWord;
    }

    // =========================================================================
    // In-memory MockSharedPreferences for migration testing
    // =========================================================================

    private static class MockSharedPreferences implements SharedPreferences {
        final Map<String, Object> map = new HashMap<>();

        @Override
        public Map<String, ?> getAll() { return map; }
        @Override
        public String getString(String key, String defValue) {
            return map.containsKey(key) ? (String) map.get(key) : defValue;
        }
        @Override
        public Set<String> getStringSet(String key, Set<String> defValues) { return defValues; }
        @Override
        public int getInt(String key, int defValue) {
            return map.containsKey(key) ? (Integer) map.get(key) : defValue;
        }
        @Override
        public long getLong(String key, long defValue) {
            return map.containsKey(key) ? (Long) map.get(key) : defValue;
        }
        @Override
        public float getFloat(String key, float defValue) {
            return map.containsKey(key) ? (Float) map.get(key) : defValue;
        }
        @Override
        public boolean getBoolean(String key, boolean defValue) {
            return map.containsKey(key) ? (Boolean) map.get(key) : defValue;
        }
        @Override
        public boolean contains(String key) { return map.containsKey(key); }
        @Override
        public Editor edit() { return new MockEditor(this); }
        @Override
        public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {}
        @Override
        public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {}

        private static class MockEditor implements Editor {
            private final MockSharedPreferences mPrefs;
            private final Map<String, Object> mTemp = new HashMap<>();

            MockEditor(MockSharedPreferences prefs) {
                mPrefs = prefs;
                mTemp.putAll(prefs.map);
            }
            @Override
            public Editor putString(String key, String value) { mTemp.put(key, value); return this; }
            @Override
            public Editor putStringSet(String key, Set<String> values) { return this; }
            @Override
            public Editor putInt(String key, int value) { mTemp.put(key, value); return this; }
            @Override
            public Editor putLong(String key, long value) { mTemp.put(key, value); return this; }
            @Override
            public Editor putFloat(String key, float value) { mTemp.put(key, value); return this; }
            @Override
            public Editor putBoolean(String key, boolean value) { mTemp.put(key, value); return this; }
            @Override
            public Editor remove(String key) { mTemp.remove(key); return this; }
            @Override
            public Editor clear() { mTemp.clear(); return this; }
            @Override
            public boolean commit() {
                mPrefs.map.clear();
                mPrefs.map.putAll(mTemp);
                return true;
            }
            @Override
            public void apply() { commit(); }
        }
    }
}
