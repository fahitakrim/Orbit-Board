package org.futo.inputmethod.keyboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.view.inputmethod.EditorInfo;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class NumberRowAndSymbolsKeyTest {

    private KeyboardId createKeyboardId(int elementId, boolean numberRow) {
        KeyboardLayoutSet.Params params = new KeyboardLayoutSet.Params();
        params.mEditorInfo = new EditorInfo();
        params.mNumberRow = numberRow;
        return new KeyboardId(elementId, params);
    }

    private Key createKey(String label, int y, int height) {
        return new Key(label, null, label.charAt(0), null, null, 0, 0, 0, y, 100, height, 0, 0);
    }

    @Test
    public void testPinModeNumberPadKeysAreNeverTreatedAsNumberRow() {
        // Bug 1: In PIN / Number mode (ELEMENT_NUMBER), buttons 1, 2, 3 must NOT be treated as square number row
        KeyboardId numberPadId = createKeyboardId(KeyboardId.ELEMENT_NUMBER, false);
        Key key1 = createKey("1", 0, 100);
        Key key2 = createKey("2", 0, 100);
        Key key3 = createKey("3", 0, 100);
        Key key4 = createKey("4", 100, 100);

        assertFalse("Key 1 in PIN pad must NOT be number row key", KeyboardView.isNumberRowKey(numberPadId, key1));
        assertFalse("Key 2 in PIN pad must NOT be number row key", KeyboardView.isNumberRowKey(numberPadId, key2));
        assertFalse("Key 3 in PIN pad must NOT be number row key", KeyboardView.isNumberRowKey(numberPadId, key3));
        assertFalse("Key 4 in PIN pad must NOT be number row key", KeyboardView.isNumberRowKey(numberPadId, key4));
    }

    @Test
    public void testSymbolsPageNumberRowIsNeverTreatedAsNumberRow() {
        // Bug 2: In symbols mode (?123), row 1 keys 1..0 must stay regular rectangular keys, not square
        KeyboardId symbolsId = createKeyboardId(KeyboardId.ELEMENT_SYMBOLS, true);
        for (int i = 0; i <= 9; i++) {
            Key numKey = createKey(String.valueOf(i), 0, 100);
            assertFalse("Symbol page key " + i + " must NOT be square number row key",
                    KeyboardView.isNumberRowKey(symbolsId, numKey));
        }

        KeyboardId symbolsShiftedId = createKeyboardId(KeyboardId.ELEMENT_SYMBOLS_SHIFTED, true);
        Key symbolKey = createKey("~", 0, 100);
        assertFalse("Shifted symbols page key must NOT be number row key",
                KeyboardView.isNumberRowKey(symbolsShiftedId, symbolKey));
    }

    @Test
    public void testAlphabetKeyboardDedicatedNumberRowIsSquare() {
        // Dedicated number row on alphabet keyboard when enabled
        KeyboardId alphaWithNumberRow = createKeyboardId(KeyboardId.ELEMENT_ALPHABET, true);
        Key numKeyRow0 = createKey("1", 0, 100);
        Key bnNumKeyRow0 = createKey("\u09E7", 0, 100); // ১
        Key shiftedSymbolRow0 = createKey("!", 0, 100);
        Key letterKeyRow1 = createKey("q", 100, 100);

        assertTrue("Alphabet dedicated row 0 key 1 MUST be number row key",
                KeyboardView.isNumberRowKey(alphaWithNumberRow, numKeyRow0));
        assertTrue("Alphabet dedicated row 0 Bengali key ১ MUST be number row key",
                KeyboardView.isNumberRowKey(alphaWithNumberRow, bnNumKeyRow0));
        assertTrue("Alphabet dedicated row 0 shifted symbol ! MUST be number row key",
                KeyboardView.isNumberRowKey(alphaWithNumberRow, shiftedSymbolRow0));
        assertFalse("Alphabet row 1 letter key q must NOT be number row key",
                KeyboardView.isNumberRowKey(alphaWithNumberRow, letterKeyRow1));
    }

    @Test
    public void testAlphabetKeyboardWithoutNumberRow() {
        // When number row is disabled, no key should be treated as number row
        KeyboardId alphaNoNumberRow = createKeyboardId(KeyboardId.ELEMENT_ALPHABET, false);
        Key key1 = createKey("1", 0, 100);
        Key keyQ = createKey("q", 0, 100);

        assertFalse(KeyboardView.isNumberRowKey(alphaNoNumberRow, key1));
        assertFalse(KeyboardView.isNumberRowKey(alphaNoNumberRow, keyQ));
    }

    @Test
    public void testKeyboardSetOccupiedHeight() {
        org.futo.inputmethod.keyboard.internal.KeyboardParams params =
                new org.futo.inputmethod.keyboard.internal.KeyboardParams();
        params.mId = createKeyboardId(KeyboardId.ELEMENT_ALPHABET, true);
        params.GRID_WIDTH = 10;
        params.GRID_HEIGHT = 10;
        params.mOccupiedHeight = 580;
        params.mOccupiedWidth = 0; // minWidth == 0 avoids JNI call in host JVM unit test
        Keyboard keyboard = new Keyboard(params);
        org.junit.Assert.assertEquals(580, keyboard.mOccupiedHeight);

        keyboard.setOccupiedHeight(600);
        org.junit.Assert.assertEquals(600, keyboard.mOccupiedHeight);
    }

    private Document parseXml(String relativePath) throws Exception {
        File file = new File("java/res/xml/" + relativePath);
        if (!file.exists()) {
            file = new File("nth-kbd/java/res/xml/" + relativePath);
        }
        assertTrue("XML file must exist: " + relativePath, file.exists());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        try (FileInputStream fis = new FileInputStream(file)) {
            return builder.parse(fis);
        }
    }

    @Test
    public void testXmlSymbolsProportionalRowHeightsWhenNumberRowEnabled() throws Exception {
        String[] xmlFiles = {
            "rows_symbols.xml",
            "rows_symbols_shift.xml",
            "rows_symbols_bengali.xml",
            "rows_symbols_shift_bengali.xml"
        };

        for (String xmlFile : xmlFiles) {
            Document doc = parseXml(xmlFile);
            NodeList cases = doc.getElementsByTagName("case");
            boolean foundNumberRowCase = false;
            boolean has16PercentRowHeight = false;

            for (int i = 0; i < cases.getLength(); i++) {
                Element caseEl = (Element) cases.item(i);
                if ("true".equals(caseEl.getAttribute("latin:numberRow"))) {
                    foundNumberRowCase = true;
                    NodeList rows = caseEl.getElementsByTagName("Row");
                    for (int j = 0; j < rows.getLength(); j++) {
                        Element rowEl = (Element) rows.item(j);
                        if ("16%p".equals(rowEl.getAttribute("latin:rowHeight"))) {
                            has16PercentRowHeight = true;
                            break;
                        }
                    }
                }
            }

            assertTrue(xmlFile + " must have case latin:numberRow=\"true\"", foundNumberRowCase);
            assertTrue(xmlFile + " number row must have latin:rowHeight=\"16%p\" to ensure 116%p total height",
                    has16PercentRowHeight);
        }
    }
}
