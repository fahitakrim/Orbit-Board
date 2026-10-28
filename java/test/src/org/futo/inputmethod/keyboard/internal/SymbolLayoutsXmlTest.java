package org.futo.inputmethod.keyboard.internal;

import static org.junit.Assert.assertEquals;
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
 * Validates XML configuration and key specifications for 3-Page Symbol Layouts:
 * - English/Generic: rows_symbols.xml (Page 1), rows_symbols_shift.xml (Page 2), rows_symbols_3.xml (Page 3)
 * - Bengali: rows_symbols_bengali.xml (Page 1), rows_symbols_shift_bengali.xml (Page 2), rows_symbols_3_bengali.xml (Page 3)
 * - Layout Sets: keyboard_layout_set_*.xml registering symbols3 element.
 * - Key styles: key_styles_common.xml defining 2/3, 3/3, 1/3 and Bengali equivalents.
 */
public class SymbolLayoutsXmlTest {

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

    @Test
    public void testKeyStylesCommonSymbolToggleStyles() throws Exception {
        Document doc = parseXml("key_styles_common.xml");
        NodeList styles = doc.getElementsByTagName("key-style");

        String p2Spec = null;
        String p3Spec = null;
        String p1Spec = null;
        String bnP2Spec = null;
        String bnP3Spec = null;
        String bnP1Spec = null;

        for (int i = 0; i < styles.getLength(); i++) {
            Element el = (Element) styles.item(i);
            String name = el.getAttribute("latin:styleName");
            if ("toMoreSymbolKeyStyle".equals(name)) p2Spec = el.getAttribute("latin:keySpec");
            if ("backFromMoreSymbolKeyStyle".equals(name)) p3Spec = el.getAttribute("latin:keySpec");
            if ("toSymbolPage1KeyStyle".equals(name)) p1Spec = el.getAttribute("latin:keySpec");
            if ("toMoreSymbolKeyStyle".equals(name)) bnP2Spec = el.getAttribute("latin:keySpec");
            if ("backFromMoreSymbolBengaliKeyStyle".equals(name)) bnP3Spec = el.getAttribute("latin:keySpec");
            if ("toSymbolPage1BengaliKeyStyle".equals(name)) bnP1Spec = el.getAttribute("latin:keySpec");
        }

        assertEquals("!text/keylabel_to_more_symbol|!code/key_shift", p2Spec);
        assertEquals("!text/keylabel_to_symbol|!code/key_shift", p3Spec);
        assertEquals("?\u09E7\u09E8\u09E9|!code/key_shift", bnP3Spec);
    }

    @Test
    public void testPage1And2ToggleStylesEnglishAndBengali() throws Exception {
        // Page 1 English
        Document p1En = parseXml("rows_symbols.xml");
        NodeList keysP1 = p1En.getElementsByTagName("Key");
        boolean foundP1En = false;
        for (int i = 0; i < keysP1.getLength(); i++) {
            Element el = (Element) keysP1.item(i);
            if ("toMoreSymbolKeyStyle".equals(el.getAttribute("latin:keyStyle"))) {
                foundP1En = true;
                break;
            }
        }
        assertTrue("rows_symbols.xml must contain toMoreSymbolKeyStyle", foundP1En);

        // Page 1 Bengali
        Document p1Bn = parseXml("rows_symbols_bengali.xml");
        NodeList keysP1Bn = p1Bn.getElementsByTagName("Key");
        boolean foundP1Bn = false;
        for (int i = 0; i < keysP1Bn.getLength(); i++) {
            Element el = (Element) keysP1Bn.item(i);
            if ("toMoreSymbolKeyStyle".equals(el.getAttribute("latin:keyStyle"))) {
                foundP1Bn = true;
                break;
            }
        }
        assertTrue("rows_symbols_bengali.xml must contain toMoreSymbolKeyStyle", foundP1Bn);

        // Page 3 (Advanced Symbols) English
        Document p2En = parseXml("rows_symbols_shift.xml");
        NodeList keysP2 = p2En.getElementsByTagName("Key");
        boolean foundP2En = false;
        for (int i = 0; i < keysP2.getLength(); i++) {
            Element el = (Element) keysP2.item(i);
            if ("backFromMoreSymbolKeyStyle".equals(el.getAttribute("latin:keyStyle"))) {
                foundP2En = true;
                break;
            }
        }
        assertTrue("rows_symbols_shift.xml must contain backFromMoreSymbolKeyStyle", foundP2En);

        // Page 3 (Advanced Symbols) Bengali
        Document p2Bn = parseXml("rows_symbols_shift_bengali.xml");
        NodeList keysP2Bn = p2Bn.getElementsByTagName("Key");
        boolean foundP2Bn = false;
        for (int i = 0; i < keysP2Bn.getLength(); i++) {
            Element el = (Element) keysP2Bn.item(i);
            if ("backFromMoreSymbolBengaliKeyStyle".equals(el.getAttribute("latin:keyStyle"))) {
                foundP2Bn = true;
                break;
            }
        }
        assertTrue("rows_symbols_shift_bengali.xml must contain backFromMoreSymbolBengaliKeyStyle", foundP2Bn);
    }

    @Test
    public void testPage3EnglishStructureAndEmoticons() throws Exception {
        Document doc = parseXml("rows_symbols_3.xml");
        NodeList rows = doc.getElementsByTagName("Row");
        assertTrue("rows_symbols_3.xml must contain at least 4 rows", rows.getLength() >= 4);

        // Check Row 1 keys: fractions & superscripts
        Element row1 = (Element) rows.item(0);
        NodeList row1Keys = row1.getElementsByTagName("Key");
        assertEquals("Row 1 must have 10 keys", 10, row1Keys.getLength());

        List<String> row1Specs = new ArrayList<>();
        for (int i = 0; i < row1Keys.getLength(); i++) {
            row1Specs.add(((Element) row1Keys.item(i)).getAttribute("latin:keySpec"));
        }
        assertTrue("Row 1 contains ½", row1Specs.contains("\u00BD"));
        assertTrue("Row 1 contains ⅓", row1Specs.contains("\u2153"));
        assertTrue("Row 1 contains ¼", row1Specs.contains("\u00BC"));
        assertTrue("Row 1 contains ¹", row1Specs.contains("\u00B9"));
        assertTrue("Row 1 contains ²", row1Specs.contains("\u00B2"));
        assertTrue("Row 1 contains ³", row1Specs.contains("\u00B3"));
        assertTrue("Row 1 contains ⁿ", row1Specs.contains("\u207F"));

        // Check Row 2 keys: typographic marks and bullets
        Element row2 = (Element) rows.item(1);
        NodeList row2Keys = row2.getElementsByTagName("Key");
        assertEquals("Row 2 must have 10 keys", 10, row2Keys.getLength());

        List<String> row2Specs = new ArrayList<>();
        for (int i = 0; i < row2Keys.getLength(); i++) {
            row2Specs.add(((Element) row2Keys.item(i)).getAttribute("latin:keySpec"));
        }
        assertTrue("Row 2 contains «", row2Specs.contains("\u00AB"));
        assertTrue("Row 2 contains »", row2Specs.contains("\u00BB"));
        assertTrue("Row 2 contains •", row2Specs.contains("\u2022"));
        assertTrue("Row 2 contains ★", row2Specs.contains("\u2605"));

        // Check Row 3 keys: Toggle key (toSymbolPage1KeyStyle), Emoticons, Delete
        Element row3 = (Element) rows.item(2);
        NodeList row3Keys = row3.getElementsByTagName("Key");
        assertTrue("Row 3 must have at least 9 keys", row3Keys.getLength() >= 9);

        boolean hasP1Toggle = false;
        boolean hasDelete = false;
        List<String> emoticons = new ArrayList<>();

        for (int i = 0; i < row3Keys.getLength(); i++) {
            Element keyEl = (Element) row3Keys.item(i);
            String style = keyEl.getAttribute("latin:keyStyle");
            String spec = keyEl.getAttribute("latin:keySpec");
            if ("backFromMoreSymbolKeyStyle".equals(style)) hasP1Toggle = true;
            if ("deleteKeyStyle".equals(style)) hasDelete = true;
            if (!spec.isEmpty()) emoticons.add(spec);
        }

        assertTrue("Row 3 must contain backFromMoreSymbolKeyStyle toggle", hasP1Toggle);
        assertTrue("Row 3 must contain deleteKeyStyle", hasDelete);
        assertTrue("Emoticons contain :)", emoticons.contains(":)"));
        assertTrue("Emoticons contain ;)", emoticons.contains(";)"));
        assertTrue("Emoticons contain :D", emoticons.contains(":D"));
        assertTrue("Emoticons contain :P", emoticons.contains(":P"));
        assertTrue("Emoticons contain -_-", emoticons.contains("-_-"));
        assertTrue("Emoticons contain ^_^", emoticons.contains("^_^"));
        assertTrue("Emoticons contain shrug", emoticons.contains("¯\\\\_(ツ)_/¯"));

        // Check Row 4 has ABC key
        Element row4 = (Element) rows.item(3);
        NodeList row4Keys = row4.getElementsByTagName("Key");
        boolean hasAlphaKey = false;
        for (int i = 0; i < row4Keys.getLength(); i++) {
            Element keyEl = (Element) row4Keys.item(i);
            if ("toAlphaKeyStyle".equals(keyEl.getAttribute("latin:keyStyle"))) {
                hasAlphaKey = true;
            }
        }
        assertTrue("Row 4 must contain toAlphaKeyStyle (ABC)", hasAlphaKey);
    }

    @Test
    public void testPage3BengaliStructureAndEmoticons() throws Exception {
        Document doc = parseXml("rows_symbols_3_bengali.xml");
        NodeList rows = doc.getElementsByTagName("Row");
        assertTrue("rows_symbols_3_bengali.xml must contain at least 4 rows", rows.getLength() >= 4);

        // Check Row 3 toggle key
        Element row3 = (Element) rows.item(2);
        NodeList row3Keys = row3.getElementsByTagName("Key");
        boolean hasBnToggle = false;
        for (int i = 0; i < row3Keys.getLength(); i++) {
            Element keyEl = (Element) row3Keys.item(i);
            if ("backFromMoreSymbolBengaliKeyStyle".equals(keyEl.getAttribute("latin:keyStyle"))) {
                hasBnToggle = true;
            }
        }
        assertTrue("Row 3 must contain backFromMoreSymbolBengaliKeyStyle toggle", hasBnToggle);

        // Check Row 4 has Bengali ABC key
        Element row4 = (Element) rows.item(3);
        NodeList row4Keys = row4.getElementsByTagName("Key");
        boolean hasBnAlphaKey = false;
        for (int i = 0; i < row4Keys.getLength(); i++) {
            Element keyEl = (Element) row4Keys.item(i);
            if ("toAlphaBengaliKeyStyle".equals(keyEl.getAttribute("latin:keyStyle"))) {
                hasBnAlphaKey = true;
            }
        }
        assertTrue("Row 4 must contain toAlphaBengaliKeyStyle", hasBnAlphaKey);
    }

    @Test
    public void testLayoutSetsRegisterSymbols3() throws Exception {
        String[] layoutSets = {
                "keyboard_layout_set_qwerty.xml",
                "keyboard_layout_set_bengali_avro.xml",
                "keyboard_layout_set_bengali_jatiyo.xml",
                "keyboard_layout_set_bengali_probhat.xml",
                "keyboard_layout_set_bengali.xml"
        };

        for (String file : layoutSets) {
            Document doc = parseXml(file);
            NodeList elements = doc.getElementsByTagName("Element");
            boolean foundSymbols3 = false;
            for (int i = 0; i < elements.getLength(); i++) {
                Element el = (Element) elements.item(i);
                if ("symbols3".equals(el.getAttribute("latin:elementName"))) {
                    foundSymbols3 = true;
                    assertNotNull("elementKeyboard attribute must not be empty", el.getAttribute("latin:elementKeyboard"));
                    break;
                }
            }
            assertTrue("Layout set " + file + " must register elementName=symbols3", foundSymbols3);
        }
    }
}
