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
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Validates Avro keyboard layout XML configuration against user reference requirements.
 */
public class AvroLayoutXmlTest {

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

    private String getFirstMoreKey(String moreKeys) {
        if (moreKeys == null || moreKeys.isEmpty()) return null;
        String[] parts = moreKeys.split(",");
        return parts.length > 0 ? parts[0] : null;
    }

    @Test
    public void testKbdBengaliAvroIncludesDedicatedRows() throws Exception {
        Document doc = parseXml("kbd_bengali_avro.xml");
        NodeList includes = doc.getElementsByTagName("include");
        boolean foundRowsBengaliAvro = false;
        for (int i = 0; i < includes.getLength(); i++) {
            Element el = (Element) includes.item(i);
            String layout = el.getAttribute("latin:keyboardLayout");
            if ("@xml/rows_bengali_avro".equals(layout)) {
                foundRowsBengaliAvro = true;
            }
        }
        assertTrue("kbd_bengali_avro.xml must include @xml/rows_bengali_avro", foundRowsBengaliAvro);
    }

    @Test
    public void testRowsBengaliAvroStructure() throws Exception {
        Document doc = parseXml("rows_bengali_avro.xml");
        NodeList includes = doc.getElementsByTagName("include");
        boolean hasRow1 = false;
        boolean hasRow2 = false;
        boolean hasRow3 = false;
        boolean hasRow4 = false;

        for (int i = 0; i < includes.getLength(); i++) {
            Element el = (Element) includes.item(i);
            String layout = el.getAttribute("latin:keyboardLayout");
            if ("@xml/rowkeys_qwerty1".equals(layout)) hasRow1 = true;
            if ("@xml/rowkeys_bengali_avro2".equals(layout)) hasRow2 = true;
            if ("@xml/rowkeys_qwerty3".equals(layout)) hasRow3 = true;
            if ("@xml/row_bengali_avro4".equals(layout)) hasRow4 = true;
        }

        assertTrue("rows_bengali_avro.xml must include @xml/rowkeys_qwerty1 (Row 1)", hasRow1);
        assertTrue("rows_bengali_avro.xml must include @xml/rowkeys_bengali_avro2 (Row 2)", hasRow2);
        assertTrue("rows_bengali_avro.xml must include @xml/rowkeys_qwerty3 (Row 3)", hasRow3);
        assertTrue("rows_bengali_avro.xml must include @xml/row_bengali_avro4 (Row 4)", hasRow4);
    }

    @Test
    public void testRow1NumberHintsParity() throws Exception {
        // Row 1 contains q, w, e, r, t, y, u, i, o, p with hints 1-0
        Document leftDoc = parseXml("rowkeys_qwerty1_left5.xml");
        Document rightDoc = parseXml("rowkeys_qwerty1_right5.xml");

        Map<String, String> keyHints = new HashMap<>();
        collectKeyHints(leftDoc, keyHints);
        collectKeyHints(rightDoc, keyHints);

        assertEquals("1", keyHints.get("!text/keyspec_q"));
        assertEquals("2", keyHints.get("!text/keyspec_w"));
        assertEquals("3", keyHints.get("e"));
        assertEquals("4", keyHints.get("r"));
        assertEquals("5", keyHints.get("t"));
        assertEquals("6", keyHints.get("!text/keyspec_y"));
        assertEquals("7", keyHints.get("u"));
        assertEquals("8", keyHints.get("i"));
        assertEquals("9", keyHints.get("o"));
        assertEquals("0", keyHints.get("p"));
    }

    @Test
    public void testRow2AvroHintsAndDollarOnD() throws Exception {
        // Row 2: a s d f g h j k l with hints @ # $ * - ! ? ( )
        Document doc = parseXml("rowkeys_bengali_avro2.xml");
        Map<String, String> keyHints = new HashMap<>();
        collectKeyHints(doc, keyHints);

        assertEquals("@", keyHints.get("a"));
        assertEquals("#", keyHints.get("s"));
        assertEquals("$", keyHints.get("d")); // Key 'd' MUST have '$' instead of '&'
        assertEquals("*", keyHints.get("f"));
        assertEquals("-", keyHints.get("g"));
        assertEquals("!", keyHints.get("h"));
        assertEquals("?", keyHints.get("j"));
        assertEquals("(", keyHints.get("k"));
        assertEquals(")", keyHints.get("l"));

        // Crucial requirement: Key 'd' must NOT have '&'
        assertFalse("Key 'd' hint must not be '&'", "&".equals(keyHints.get("d")));
    }

    @Test
    public void testRow3AvroHintsParity() throws Exception {
        // Row 3: z x c v b n m with popup hints " ' : /
        Document leftDoc = parseXml("rowkeys_qwerty3_left4.xml");
        Document rightDoc = parseXml("rowkeys_qwerty3_right3.xml");

        Map<String, String> keyHints = new HashMap<>();
        collectKeyHints(leftDoc, keyHints);
        collectKeyHints(rightDoc, keyHints);

        assertEquals("\"", keyHints.get("z"));
        assertEquals("'", keyHints.get("b"));
        assertEquals(":", keyHints.get("n"));
        assertEquals("/", keyHints.get("m"));
    }

    @Test
    public void testRow4BengaliDariPresence() throws Exception {
        // Period / Danti key: Displays and inputs Bengali Dari (।, \u0964) as default or long-press
        Document doc = parseXml("row_bengali_avro4.xml");
        NodeList keys = doc.getElementsByTagName("Key");
        boolean foundDari = false;

        for (int i = 0; i < keys.getLength(); i++) {
            Element el = (Element) keys.item(i);
            String keySpec = el.getAttribute("latin:keySpec");
            String moreKeys = el.getAttribute("latin:moreKeys");

            if ("\u0964".equals(keySpec) || "&#x0964;".equals(keySpec) ||
                    (moreKeys != null && (moreKeys.contains("\u0964") || moreKeys.contains("&#x0964;")))) {
                foundDari = true;
            }
        }

        assertTrue("Bengali Dari (।) must be present on the period/danti key in row_bengali_avro4.xml", foundDari);
    }

    @Test
    public void testEnglishQwertyNonRegression() throws Exception {
        // Verify English QWERTY key 'd' still has '&' and was not overwritten
        Document doc = parseXml("rowkeys_qwerty2_left5.xml");
        Map<String, String> keyHints = new HashMap<>();
        collectKeyHints(doc, keyHints);

        assertEquals("&", keyHints.get("d"));
    }

    private void collectKeyHints(Document doc, Map<String, String> keyHints) {
        NodeList keys = doc.getElementsByTagName("Key");
        for (int i = 0; i < keys.getLength(); i++) {
            Element el = (Element) keys.item(i);
            String spec = el.getAttribute("latin:keySpec");
            String moreKeys = el.getAttribute("latin:moreKeys");
            String first = getFirstMoreKey(moreKeys);
            if (first != null) {
                boolean isDirective = first.startsWith("!text/") || first.startsWith("!code/") || first.startsWith("!noPanel");
                if (!isDirective) {
                    keyHints.put(spec, first);
                }
            }
        }
    }
}
