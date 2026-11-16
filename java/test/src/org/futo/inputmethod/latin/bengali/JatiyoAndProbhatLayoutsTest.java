package org.futo.inputmethod.latin.bengali;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Validates Jatiyo Modern, Jatiyo Classic, and Probhat layouts against Ridmik specifications.
 */
public class JatiyoAndProbhatLayoutsTest {

    private static final String RES_XML_PATH = "java/res/xml/";

    private Document parseXml(String relativePath) throws Exception {
        File file = new File(RES_XML_PATH + relativePath);
        if (!file.exists()) {
            file = new File("nth-kbd/" + RES_XML_PATH + relativePath);
        }
        assertTrue("XML file must exist: " + relativePath, file.exists());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        try (FileInputStream fis = new FileInputStream(file)) {
            return builder.parse(fis);
        }
    }

    private List<Element> getKeysByLayoutElement(Element row, boolean shifted) {
        List<Element> keys = new ArrayList<>();
        NodeList children = row.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element child = (Element) children.item(i);
                if ("Key".equals(child.getTagName())) {
                    String style = child.getAttribute("latin:keyStyle");
                    if (!"shiftKeyStyle".equals(style) && !"deleteKeyStyle".equals(style)) {
                        keys.add(child);
                    }
                } else if ("switch".equals(child.getTagName())) {
                    NodeList cases = child.getChildNodes();
                    for (int j = 0; j < cases.getLength(); j++) {
                        if (cases.item(j) instanceof Element) {
                            Element caseEl = (Element) cases.item(j);
                            boolean isShiftCase = "case".equals(caseEl.getTagName())
                                    && caseEl.getAttribute("latin:keyboardLayoutSetElement").contains("alphabetManualShifted");
                            if (shifted && isShiftCase) {
                                NodeList caseKeys = caseEl.getElementsByTagName("Key");
                                for (int k = 0; k < caseKeys.getLength(); k++) {
                                    keys.add((Element) caseKeys.item(k));
                                }
                            } else if (!shifted && "default".equals(caseEl.getTagName())) {
                                NodeList defKeys = caseEl.getElementsByTagName("Key");
                                for (int k = 0; k < defKeys.getLength(); k++) {
                                    keys.add((Element) defKeys.item(k));
                                }
                            }
                        }
                    }
                }
            }
        }
        return keys;
    }

    @Test
    public void testJatiyoModernRow2KeySpecs() throws Exception {
        Document doc = parseXml("rows_bengali_jatiyo.xml");
        NodeList rows = doc.getElementsByTagName("Row");
        assertTrue("Must have at least 4 rows", rows.getLength() >= 4);

        Element row2 = (Element) rows.item(1); // Row 2
        List<Element> baseKeys = getKeysByLayoutElement(row2, false);
        List<Element> shiftKeys = getKeysByLayoutElement(row2, true);

        assertEquals("Row 2 must have 9 keys", 9, baseKeys.size());
        assertEquals("Row 2 shift must have 9 keys", 9, shiftKeys.size());

        // f = া / অ
        assertEquals("f key base must be া", "\u09BE", baseKeys.get(3).getAttribute("latin:keySpec"));
        assertEquals("f key shift must be অ", "\u0985", shiftKeys.get(3).getAttribute("latin:keySpec"));

        // g = ্ (hasanta)
        assertEquals("g key base must be ্", "\u09CD", baseKeys.get(4).getAttribute("latin:keySpec"));
        assertTrue("g key moreKeys must have nukta ়", baseKeys.get(4).getAttribute("latin:moreKeys").contains("\u09BC"));

        // h = ব / ভ
        assertEquals("h key base must be ব", "\u09AC", baseKeys.get(5).getAttribute("latin:keySpec"));
        assertEquals("h key shift must be ভ", "\u09AD", shiftKeys.get(5).getAttribute("latin:keySpec"));

        // s = ু / ূ
        assertEquals("s key base must be ু", "\u09C1", baseKeys.get(1).getAttribute("latin:keySpec"));
        assertEquals("s key shift must be ূ", "\u09C2", shiftKeys.get(1).getAttribute("latin:keySpec"));

        // d = ি / ী
        assertEquals("d key base must be ি", "\u09BF", baseKeys.get(2).getAttribute("latin:keySpec"));
        assertEquals("d key shift must be ী", "\u09C0", shiftKeys.get(2).getAttribute("latin:keySpec"));

        // k = ত / থ (long-press ৎ)
        assertEquals("k key base must be ত", "\u09A4", baseKeys.get(7).getAttribute("latin:keySpec"));
        assertEquals("k key shift must be থ", "\u09A5", shiftKeys.get(7).getAttribute("latin:keySpec"));
        assertTrue("k key moreKeys must have ৎ", baseKeys.get(7).getAttribute("latin:moreKeys").contains("\u09CE"));

        // l = দ / ধ (long-press ঃ)
        assertEquals("l key base must be দ", "\u09A6", baseKeys.get(8).getAttribute("latin:keySpec"));
        assertEquals("l key shift must be ধ", "\u09A7", shiftKeys.get(8).getAttribute("latin:keySpec"));
        assertTrue("l key moreKeys must have ঃ", baseKeys.get(8).getAttribute("latin:moreKeys").contains("\u0983"));
    }

    @Test
    public void testJatiyoModernRow3KeySpecs() throws Exception {
        Document doc = parseXml("rows_bengali_jatiyo.xml");
        NodeList rows = doc.getElementsByTagName("Row");
        Element row3 = (Element) rows.item(2); // Row 3

        List<Element> baseKeys = getKeysByLayoutElement(row3, false);
        List<Element> shiftKeys = getKeysByLayoutElement(row3, true);

        // z = ্র / ্য
        assertEquals("z key base must be ্র", "\u09CD\u09B0", baseKeys.get(0).getAttribute("latin:keySpec"));
        assertEquals("z key shift must be ্য", "\u09CD\u09AF", shiftKeys.get(0).getAttribute("latin:keySpec"));

        // v = র / ল
        assertEquals("v key base must be র", "\u09B0", baseKeys.get(3).getAttribute("latin:keySpec"));
        assertEquals("v key shift must be ল", "\u09B2", shiftKeys.get(3).getAttribute("latin:keySpec"));
        assertTrue("v key moreKeys must have ৱ", baseKeys.get(3).getAttribute("latin:moreKeys").contains("\u09F1"));
        assertTrue("v key moreKeys must have ৰ", baseKeys.get(3).getAttribute("latin:moreKeys").contains("\u09F0"));

        // n = স / ষ (long-press শ)
        assertEquals("n key base must be স", "\u09B8", baseKeys.get(5).getAttribute("latin:keySpec"));
        assertEquals("n key shift must be ষ", "\u09B7", shiftKeys.get(5).getAttribute("latin:keySpec"));
        assertTrue("n key moreKeys must have শ", baseKeys.get(5).getAttribute("latin:moreKeys").contains("\u09B6"));
    }

    @Test
    public void testJatiyoClassicKeyDifferences() throws Exception {
        Document doc = parseXml("rows_bengali_jatiyo_classic.xml");
        NodeList rows = doc.getElementsByTagName("Row");

        Element row2 = (Element) rows.item(1);
        List<Element> baseKeysR2 = getKeysByLayoutElement(row2, false);
        List<Element> shiftKeysR2 = getKeysByLayoutElement(row2, true);

        // a = ৃ / ঋ in classic (hold gives ঐ)
        assertEquals("a key base must be ৃ in classic", "\u09C3", baseKeysR2.get(0).getAttribute("latin:keySpec"));
        assertEquals("a key shift must be ঋ in classic", "\u098B", shiftKeysR2.get(0).getAttribute("latin:keySpec"));
        assertTrue("a key moreKeys must have ঐ", baseKeysR2.get(0).getAttribute("latin:moreKeys").contains("\u0990"));

        // f = ব / ভ in classic
        assertEquals("f key base must be ব in classic", "\u09AC", baseKeysR2.get(3).getAttribute("latin:keySpec"));
        assertEquals("f key shift must be ভ in classic", "\u09AD", shiftKeysR2.get(3).getAttribute("latin:keySpec"));

        // h = া / অ in classic (hold gives আ)
        assertEquals("h key base must be া in classic", "\u09BE", baseKeysR2.get(5).getAttribute("latin:keySpec"));
        assertEquals("h key shift must be অ in classic", "\u0985", shiftKeysR2.get(5).getAttribute("latin:keySpec"));
        assertTrue("h key moreKeys must have আ", baseKeysR2.get(5).getAttribute("latin:moreKeys").contains("\u0986"));

        // k = ত / থ in classic (hold gives ৎ)
        assertEquals("k key base must be ত in classic", "\u09A4", baseKeysR2.get(7).getAttribute("latin:keySpec"));
        assertEquals("k key shift must be থ in classic", "\u09A5", shiftKeysR2.get(7).getAttribute("latin:keySpec"));
        assertTrue("k key moreKeys must have ৎ", baseKeysR2.get(7).getAttribute("latin:moreKeys").contains("\u09CE"));

        // Row 3 z = ঁ / ঃ in classic
        Element row3 = (Element) rows.item(2);
        List<Element> baseKeysR3 = getKeysByLayoutElement(row3, false);
        List<Element> shiftKeysR3 = getKeysByLayoutElement(row3, true);

        assertEquals("z key base must be ঁ in classic", "\u0981", baseKeysR3.get(0).getAttribute("latin:keySpec"));
        assertEquals("z key shift must be ঃ in classic", "\u0983", shiftKeysR3.get(0).getAttribute("latin:keySpec"));
    }

    @Test
    public void testProbhatLayoutSpecs() throws Exception {
        Document doc = parseXml("rows_bengali_probhat.xml");
        NodeList rows = doc.getElementsByTagName("Row");

        Element row1 = (Element) rows.item(0);
        List<Element> baseKeysR1 = getKeysByLayoutElement(row1, false);
        assertEquals("Probhat Row 1 must have 12 keys", 12, baseKeysR1.size());

        // r = র (long-press ড় center, ্র right, র্ left, র্য, ৰ, ৱ)
        Element rKey = baseKeysR1.get(3);
        String moreKeysR = rKey.getAttribute("latin:moreKeys");
        assertTrue("Probhat r key must have ্র", moreKeysR.contains("\u09CD\u09B0"));
        assertTrue("Probhat r key must have র্", moreKeysR.contains("\u09B0\u09CD"));
        assertEquals("Probhat r key moreKeys must be ordered center ড়, right ্র, left র্, etc.",
                "\u09DC,\u09CD\u09B0,\u09B0\u09CD,\u09B0\u200D\u09CD\u09AF,\u09F0,\u09F1", moreKeysR);

        List<Element> shiftKeysR1 = getKeysByLayoutElement(row1, true);
        Element shiftRKey = shiftKeysR1.get(3);
        assertEquals("Probhat shifted r key must be ড়", "\u09DC", shiftRKey.getAttribute("latin:keySpec"));
        assertEquals("Probhat shifted r key hint must be র্", "\u09B0\u09CD", shiftRKey.getAttribute("latin:keyHintLabel"));
        assertTrue("Probhat shifted r key moreKeys must contain র্", shiftRKey.getAttribute("latin:moreKeys").contains("\u09B0\u09CD"));

        Element row3 = (Element) rows.item(2);
        List<Element> baseKeysR3 = getKeysByLayoutElement(row3, false);
        List<Element> shiftKeysR3 = getKeysByLayoutElement(row3, true);

        // z = য় / য (hint ্য) in probhat
        assertEquals("Probhat z key base must be য়", "\u09DF", baseKeysR3.get(0).getAttribute("latin:keySpec"));
        assertEquals("Probhat z key shift must be য", "\u09AF", shiftKeysR3.get(0).getAttribute("latin:keySpec"));
        assertEquals("Probhat z key shift hint must be ্য", "\u09CD\u09AF", shiftKeysR3.get(0).getAttribute("latin:keyHintLabel"));

        // Key 8 = ৃ in probhat
        assertEquals("Probhat key 8 must be ৃ", "\u09C3", baseKeysR3.get(7).getAttribute("latin:keySpec"));

        // Key 9 = ্ / ঁ in probhat
        assertEquals("Probhat key 9 must be ্", "\u09CD", baseKeysR3.get(8).getAttribute("latin:keySpec"));
        assertEquals("Probhat key 9 shift must be ঁ", "\u0981", shiftKeysR3.get(8).getAttribute("latin:keySpec"));
    }

    @Test
    public void testSymbolsLayoutHasEqualsAndDari() throws Exception {
        Document doc = parseXml("rows_symbols_bengali.xml");
        NodeList keys = doc.getElementsByTagName("Key");
        boolean hasEqualsInMoreKeys = false;
        boolean hasPeriodOnRow4WithDari = false;

        for (int i = 0; i < keys.getLength(); i++) {
            Element el = (Element) keys.item(i);
            String spec = el.getAttribute("latin:keySpec");
            String moreKeys = el.getAttribute("latin:moreKeys");

            if ("+".equals(spec) && moreKeys != null && moreKeys.contains("=")) {
                hasEqualsInMoreKeys = true;
            }
            if (".".equals(spec) && "functional".equals(el.getAttribute("latin:backgroundType"))
                    && moreKeys != null && (moreKeys.contains("\u0964") || moreKeys.contains("&#x0964;"))) {
                hasPeriodOnRow4WithDari = true;
            }
        }

        assertTrue("Symbols layout + key must have = in moreKeys", hasEqualsInMoreKeys);
        assertTrue("Symbols layout bottom functional key must have Dot (.) with Dari in moreKeys", hasPeriodOnRow4WithDari);
    }

    @Test
    public void testJatiyoClassicHintsAndMoreKeysWithoutNoPanelAutoMoreKey() throws Exception {
        Document doc = parseXml("rows_bengali_jatiyo_classic.xml");
        NodeList keys = doc.getElementsByTagName("Key");
        for (int i = 0; i < keys.getLength(); i++) {
            Element key = (Element) keys.item(i);
            String moreKeys = key.getAttribute("latin:moreKeys");
            assertFalse("rows_bengali_jatiyo_classic.xml must NOT contain !noPanelAutoMoreKey!",
                    moreKeys != null && moreKeys.contains("!noPanelAutoMoreKey!"));
        }

        NodeList rows = doc.getElementsByTagName("Row");
        Element row1 = (Element) rows.item(0);
        List<Element> baseRow1 = getKeysByLayoutElement(row1, false);
        // w = য (hint য়, moreKeys য়)
        assertEquals("w key base must be য", "\u09AF", baseRow1.get(1).getAttribute("latin:keySpec"));
        assertEquals("w key hint must be য়", "\u09DF", baseRow1.get(1).getAttribute("latin:keyHintLabel"));
        assertTrue("w key moreKeys must contain য়", baseRow1.get(1).getAttribute("latin:moreKeys").contains("\u09DF"));

        Element row2 = (Element) rows.item(1);
        List<Element> baseRow2 = getKeysByLayoutElement(row2, false);
        List<Element> shiftRow2 = getKeysByLayoutElement(row2, true);

        // a: ৃ -> hint ঐ, moreKeys ঐ
        assertEquals("\u0990", baseRow2.get(0).getAttribute("latin:keyHintLabel"));
        assertTrue(baseRow2.get(0).getAttribute("latin:moreKeys").contains("\u0990"));
        // s: ু -> hint উ, moreKeys উ
        assertEquals("\u0989", baseRow2.get(1).getAttribute("latin:keyHintLabel"));
        assertTrue(baseRow2.get(1).getAttribute("latin:moreKeys").contains("\u0989"));
        // d: ি -> hint ই, moreKeys ই
        assertEquals("\u0987", baseRow2.get(2).getAttribute("latin:keyHintLabel"));
        assertTrue(baseRow2.get(2).getAttribute("latin:moreKeys").contains("\u0987"));
        // h: া -> hint আ, moreKeys আ
        assertEquals("\u0986", baseRow2.get(5).getAttribute("latin:keyHintLabel"));
        assertTrue(baseRow2.get(5).getAttribute("latin:moreKeys").contains("\u0986"));
        // k: ত -> hint ৎ, moreKeys ৎ
        assertEquals("\u09CE", baseRow2.get(7).getAttribute("latin:keyHintLabel"));
        assertTrue(baseRow2.get(7).getAttribute("latin:moreKeys").contains("\u09CE"));

        // Shifted row 2: s: ূ -> hint ঊ, moreKeys ঊ
        assertEquals("\u098A", shiftRow2.get(1).getAttribute("latin:keyHintLabel"));
        assertTrue(shiftRow2.get(1).getAttribute("latin:moreKeys").contains("\u098A"));
        // Shifted row 2: d: ী -> hint ঈ, moreKeys ঈ
        assertEquals("\u0988", shiftRow2.get(2).getAttribute("latin:keyHintLabel"));
        assertTrue(shiftRow2.get(2).getAttribute("latin:moreKeys").contains("\u0988"));

        Element row3 = (Element) rows.item(2);
        List<Element> baseRow3 = getKeysByLayoutElement(row3, false);
        List<Element> shiftRow3 = getKeysByLayoutElement(row3, true);

        // x: ো -> hint ও, moreKeys ও
        assertEquals("\u0993", baseRow3.get(1).getAttribute("latin:keyHintLabel"));
        assertTrue(baseRow3.get(1).getAttribute("latin:moreKeys").contains("\u0993"));
        // c: ে -> hint এ, moreKeys এ
        assertEquals("\u098F", baseRow3.get(2).getAttribute("latin:keyHintLabel"));
        assertTrue(baseRow3.get(2).getAttribute("latin:moreKeys").contains("\u098F"));

        // Shifted row 3: x: ৌ -> hint ঔ, moreKeys ঔ
        assertEquals("\u0994", shiftRow3.get(1).getAttribute("latin:keyHintLabel"));
        assertTrue(shiftRow3.get(1).getAttribute("latin:moreKeys").contains("\u0994"));
        // Shifted row 3: c: ৈ -> hint ঐ, moreKeys ঐ
        assertEquals("\u0990", shiftRow3.get(2).getAttribute("latin:keyHintLabel"));
        assertTrue(shiftRow3.get(2).getAttribute("latin:moreKeys").contains("\u0990"));
        // Shifted row 3: v: ্র (ro-fola)
        assertEquals("\u09CD\u09B0", shiftRow3.get(3).getAttribute("latin:keySpec"));
    }

    @Test
    public void testProvatHintsAndPopupWithoutNoPanelAutoMoreKey() throws Exception {
        Document doc = parseXml("rows_bengali_probhat.xml");
        NodeList keys = doc.getElementsByTagName("Key");
        for (int i = 0; i < keys.getLength(); i++) {
            Element key = (Element) keys.item(i);
            String moreKeys = key.getAttribute("latin:moreKeys");
            assertFalse("rows_bengali_probhat.xml must NOT contain !noPanelAutoMoreKey!",
                    moreKeys != null && moreKeys.contains("!noPanelAutoMoreKey!"));
        }

        NodeList rows = doc.getElementsByTagName("Row");
        Element row3 = (Element) rows.item(2);
        List<Element> baseRow3 = getKeysByLayoutElement(row3, false);
        Element yKey = baseRow3.get(0);
        assertEquals("Probhat Row 3 key 1 must be য়", "\u09DF", yKey.getAttribute("latin:keySpec"));
        assertEquals("Probhat Row 3 key 1 hint must be ্য", "\u09CD\u09AF", yKey.getAttribute("latin:keyHintLabel"));
        assertEquals("Probhat Row 3 key 1 moreKeys must be ্য,য,র‍্য", "\u09CD\u09AF,\u09AF,\u09B0\u200D\u09CD\u09AF", yKey.getAttribute("latin:moreKeys"));

        // Row 2 key 'ত' (f): hint 'থ', moreKeys 'থ,ৎ'
        Element row2 = (Element) rows.item(1);
        List<Element> baseRow2 = getKeysByLayoutElement(row2, false);
        Element tKey = baseRow2.get(3); // 'f' key is index 3 (a=0, s=1, d=2, f=3)
        assertEquals("Probhat Row 2 key 4 must be ত", "\u09A4", tKey.getAttribute("latin:keySpec"));
        assertEquals("Probhat 'ত' key hint must be থ", "\u09A5", tKey.getAttribute("latin:keyHintLabel"));
        assertEquals("Probhat 'ত' key moreKeys must place থ first then ৎ", "\u09A5,\u09CE", tKey.getAttribute("latin:moreKeys"));
    }

    @Test
    public void testRidmikBinaryDictionaryInstalled() {
        File dictFile = new File("java/res/raw/main_bn.dict");
        if (!dictFile.exists()) {
            dictFile = new File("nth-kbd/java/res/raw/main_bn.dict");
        }
        assertTrue("main_bn.dict must exist", dictFile.exists());
        assertTrue("main_bn.dict must be Ridmik's complete 2.96 MB dictionary (size >= 2900000 bytes), but was " + dictFile.length(),
                dictFile.length() >= 2900000L);
    }

    @Test
    public void testJatiyoShiftedDariToDot() throws Exception {
        for (String file : new String[]{"rows_bengali_jatiyo.xml", "rows_bengali_jatiyo_classic.xml"}) {
            Document doc = parseXml(file);
            NodeList rows = doc.getElementsByTagName("Row");
            Element row4 = (Element) rows.item(3);

            List<Element> unshiftedKeys = getKeysByLayoutElement(row4, false);
            boolean foundUnshiftedDari = false;
            for (Element key : unshiftedKeys) {
                if ("\u0964".equals(key.getAttribute("latin:keySpec"))) {
                    foundUnshiftedDari = true;
                    break;
                }
            }
            assertTrue(file + " Row 4 unshifted must contain Dari (।)", foundUnshiftedDari);

            List<Element> shiftedKeys = getKeysByLayoutElement(row4, true);
            boolean foundShiftedDot = false;
            for (Element key : shiftedKeys) {
                if (".".equals(key.getAttribute("latin:keySpec"))) {
                    foundShiftedDot = true;
                    break;
                }
            }
            assertTrue(file + " Row 4 shifted must contain dot (.) instead of Dari (।)", foundShiftedDot);
        }
    }
}
