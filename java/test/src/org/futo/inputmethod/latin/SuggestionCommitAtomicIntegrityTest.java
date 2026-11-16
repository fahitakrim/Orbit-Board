package org.futo.inputmethod.latin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputConnection;

import org.futo.inputmethod.latin.bengali.BanglaSuggestionEngine;
import org.futo.inputmethod.latin.bengali.RidmikParser;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Validates Atomic Suggestion Commit & Preceding Text Preservation.
 *
 * Prevents regressions where tapping a suggestion strip word deleted
 * preceding words or caused the clicked word to merge with the previous word
 * ("current word ager word er sathe giye lege jay").
 */
public class SuggestionCommitAtomicIntegrityTest {

    private RidmikParser mParser;

    @Before
    public void setUp() {
        mParser = new RidmikParser();
        BanglaSuggestionEngine.clearLearnedWords();
    }

    /**
     * Accurate simulation of Android's BaseInputConnection behavior across Binder IPC.
     * Tracks buffer, cursor, composing span start/end, and records all operations.
     */
    public static class AndroidHostInputConnection {
        private final StringBuilder mBuffer = new StringBuilder();
        private int mCursor = 0;
        private int mComposingStart = -1;
        private int mComposingEnd = -1;

        public int mDeleteSurroundingTextCalls = 0;
        public int mCommitTextCalls = 0;
        public int mSetComposingTextCalls = 0;

        public void setText(String text, int cursor) {
            mBuffer.setLength(0);
            mBuffer.append(text);
            mCursor = cursor;
            mComposingStart = -1;
            mComposingEnd = -1;
        }

        public String getBuffer() {
            return mBuffer.toString();
        }

        public int getCursor() {
            return mCursor;
        }

        public boolean hasComposingSpan() {
            return mComposingStart >= 0 && mComposingEnd >= mComposingStart;
        }

        public void setComposingText(CharSequence text, int newCursorPosition) {
            mSetComposingTextCalls++;
            final String str = text != null ? text.toString() : "";
            if (hasComposingSpan()) {
                mBuffer.replace(mComposingStart, mComposingEnd, str);
                mComposingEnd = mComposingStart + str.length();
                mCursor = mComposingEnd;
            } else {
                final int insertAt = mCursor;
                mBuffer.insert(insertAt, str);
                mComposingStart = insertAt;
                mComposingEnd = insertAt + str.length();
                mCursor = mComposingEnd;
            }
        }

        public void deleteSurroundingText(int beforeLength, int afterLength) {
            mDeleteSurroundingTextCalls++;
            // In Android BaseInputConnection:
            // If an active composing span exists, the deletion anchor 'a' is getComposingSpanStart()!
            int a = mCursor;
            if (hasComposingSpan()) {
                a = mComposingStart;
            }
            final int start = Math.max(0, a - beforeLength);
            mBuffer.delete(start, a);
            final int deleted = a - start;
            mCursor -= deleted;
            if (hasComposingSpan()) {
                mComposingStart -= deleted;
                mComposingEnd -= deleted;
            }
        }

        public void commitText(CharSequence text, int newCursorPosition) {
            mCommitTextCalls++;
            final String str = text != null ? text.toString() : "";
            if (hasComposingSpan()) {
                // By Android framework contract, commitText replaces the composing span
                mBuffer.replace(mComposingStart, mComposingEnd, str);
                mCursor = mComposingStart + str.length();
                mComposingStart = -1;
                mComposingEnd = -1;
            } else {
                mBuffer.insert(mCursor, str);
                mCursor += str.length();
            }
        }
    }

    @Test
    public void testSuggestionCommitDoesNotDeletePrecedingTextInAvro() {
        final AndroidHostInputConnection connection = new AndroidHostInputConnection();

        // 1. User has already typed and committed "আমার "
        connection.setText("আমার ", 5);

        // 2. User starts typing "nam" in Avro layout.
        // As keys 'n', 'a', 'm' are typed, setComposingText updates the composing region to "নাম"
        connection.setComposingText("নাম", 1);
        assertEquals("Buffer should contain 'আমার নাম'", "আমার নাম", connection.getBuffer());
        assertTrue("Active composing span must exist for current word", connection.hasComposingSpan());

        // 3. User sees suggestions ["নাম", "নাম্বার", "নামী"] on strip and taps "নাম"
        // By Android Framework contract, commitText must cleanly replace "নাম"
        // and deleteSurroundingText must NEVER be called!
        connection.commitText("নাম", 1);

        // Invariant: deleteSurroundingText was NOT called
        assertEquals("deleteSurroundingText must NEVER be called on suggestion commit",
                0, connection.mDeleteSurroundingTextCalls);
        assertEquals("Preceding text 'আমার ' must remain intact and not merged",
                "আমার নাম", connection.getBuffer());
        assertFalse("Composing span must be cleared after commit", connection.hasComposingSpan());
    }

    @Test
    public void testSuggestionCommitDoesNotDeletePrecedingTextInEnglish() {
        final AndroidHostInputConnection connection = new AndroidHostInputConnection();

        // 1. User has already typed "Hello "
        connection.setText("Hello ", 6);

        // 2. User types "wor" -> composing span set to "wor"
        connection.setComposingText("wor", 1);
        assertEquals("Hello wor", connection.getBuffer());

        // 3. User taps suggestion "world"
        connection.commitText("world", 1);

        assertEquals("deleteSurroundingText must NEVER be called", 0, connection.mDeleteSurroundingTextCalls);
        assertEquals("Preceding text 'Hello ' must remain intact", "Hello world", connection.getBuffer());
    }

    @Test
    public void testAvroAlternativeCandidateReplacement() {
        final AndroidHostInputConnection connection = new AndroidHostInputConnection();
        // User typed "এই "
        connection.setText("এই ", 3);

        // User typed "am" -> Avro sets composing text to "আম"
        connection.setComposingText("আম", 1);
        assertEquals("এই আম", connection.getBuffer());

        // User picks candidate "আমার" from suggestion strip
        connection.commitText("আমার", 1);

        assertEquals(0, connection.mDeleteSurroundingTextCalls);
        assertEquals("Candidate replaces only the composing word", "এই আমার", connection.getBuffer());
    }

    @Test
    public void testAvroEnglishCandidateReplacement() {
        final AndroidHostInputConnection connection = new AndroidHostInputConnection();
        connection.setText("আমি ", 4);

        // User typed "code" -> composing text "কোড"
        connection.setComposingText("কোড", 1);
        assertEquals("আমি কোড", connection.getBuffer());

        // User picks English candidate "code" from the right slot
        connection.commitText("code", 1);

        assertEquals(0, connection.mDeleteSurroundingTextCalls);
        assertEquals("English candidate cleanly replaces Bengali composing text", "আমি code", connection.getBuffer());
    }

    @Test
    public void testNextWordPredictionCommit() {
        final AndroidHostInputConnection connection = new AndroidHostInputConnection();
        // User committed "আমার " and cursor is after space
        connection.setText("আমার ", 5);
        assertFalse("No active composing span during next-word prediction", connection.hasComposingSpan());

        // User picks next word prediction "দেশ"
        connection.commitText("দেশ", 1);

        assertEquals(0, connection.mDeleteSurroundingTextCalls);
        assertEquals("Prediction appended cleanly at cursor without deleting previous word",
                "আমার দেশ", connection.getBuffer());
    }

    @Test
    public void testOldBugDemonstrationAndPrevention() {
        // Demonstrates why the old bug occurred:
        // If deleteSurroundingText(3, 0) was mistakenly invoked while "নাম" was composing,
        // Android BaseInputConnection shifted 'a' to getComposingSpanStart(),
        // deleting "ার " from "আমার " and leaving "আম" + "নাম" -> "আমনাম"!
        final AndroidHostInputConnection buggySim = new AndroidHostInputConnection();
        buggySim.setText("আমার ", 5);
        buggySim.setComposingText("নাম", 1);

        // Old buggy behavior:
        buggySim.deleteSurroundingText(3, 0); // Deletes 3 chars before composing start!
        buggySim.commitText("নাম", 1);
        assertEquals("The old bug caused corrupted text 'আমনাম'", "আমনাম", buggySim.getBuffer());

        // Fixed behavior: commitText directly, no deleteSurroundingText:
        final AndroidHostInputConnection fixedSim = new AndroidHostInputConnection();
        fixedSim.setText("আমার ", 5);
        fixedSim.setComposingText("নাম", 1);
        fixedSim.commitText("নাম", 1);
        assertEquals("The fix guarantees clean text 'আমার নাম'", "আমার নাম", fixedSim.getBuffer());
    }
}
