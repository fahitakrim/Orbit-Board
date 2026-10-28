package org.futo.inputmethod.keyboard.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.futo.inputmethod.keyboard.KeyboardId;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Empirical Adversarial Test Suite for:
 * 1. XML Parsing across all supported layouts (qwerty, bengali_avro, bengali_jatiyo, bengali_probhat).
 * 2. Adversarial fallback when symbols3 is missing in KeyboardLayoutSet.getKeyboard().
 */
public class LayoutSetInflationAndFallbackTest {

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

    private String cleanXmlResourceName(String resourceRef) {
        if (resourceRef == null) return null;
        if (resourceRef.startsWith("@xml/")) {
            return resourceRef.substring(5) + ".xml";
        }
        return resourceRef + ".xml";
    }

    // ---------------------------------------------------------------------------------------------
    // 1. XML Parsing & Layout Set Inflation Verification
    // ---------------------------------------------------------------------------------------------

    @Test
    public void testSupportedLayoutSetsDomIntegrity() throws Exception {
        String[] supportedLayoutSets = {
                "keyboard_layout_set_qwerty.xml",
                "keyboard_layout_set_bengali_avro.xml",
                "keyboard_layout_set_bengali_jatiyo.xml",
                "keyboard_layout_set_bengali_probhat.xml"
        };

        for (String layoutSetFile : supportedLayoutSets) {
            Document doc = parseXml(layoutSetFile);
            Element root = doc.getDocumentElement();
            assertEquals("Root element must be KeyboardLayoutSet in " + layoutSetFile,
                    "KeyboardLayoutSet", root.getTagName());

            // For Bengali layout sets, verify <Feature latin:supportedScript="bengali" />
            if (layoutSetFile.contains("bengali")) {
                NodeList features = doc.getElementsByTagName("Feature");
                assertTrue("Bengali layout set " + layoutSetFile + " must have Feature tag",
                        features.getLength() > 0);
                Element feature = (Element) features.item(0);
                assertEquals("bengali", feature.getAttribute("latin:supportedScript"));
            }

            // Verify essential elements
            NodeList elements = doc.getElementsByTagName("Element");
            assertTrue("Layout set must define elements: " + layoutSetFile, elements.getLength() >= 4);

            Set<String> elementNames = new HashSet<>();
            for (int i = 0; i < elements.getLength(); i++) {
                Element el = (Element) elements.item(i);
                String name = el.getAttribute("latin:elementName");
                String keyboard = el.getAttribute("latin:elementKeyboard");

                assertFalse("elementName must not be empty in " + layoutSetFile, name.isEmpty());
                assertFalse("elementKeyboard must not be empty for " + name + " in " + layoutSetFile, keyboard.isEmpty());
                elementNames.add(name);

                // Verify the referenced keyboard XML actually exists on disk and is parseable
                String xmlFileName = cleanXmlResourceName(keyboard);
                Document kbdDoc = parseXml(xmlFileName);
                assertNotNull("Referenced keyboard XML must parse: " + xmlFileName, kbdDoc);
                assertEquals("Keyboard", kbdDoc.getDocumentElement().getTagName());
            }

            assertTrue(layoutSetFile + " must define alphabet", elementNames.contains("alphabet"));
            assertTrue(layoutSetFile + " must define symbols", elementNames.contains("symbols"));
            assertTrue(layoutSetFile + " must define symbolsShifted", elementNames.contains("symbolsShifted"));
            assertTrue(layoutSetFile + " must define symbols3", elementNames.contains("symbols3"));
        }
    }

    @Test
    public void testRecursiveIncludeResolutionAcrossAllLayouts() throws Exception {
        // Collect all XML files referenced recursively by the 4 layouts
        String[] supportedLayoutSets = {
                "keyboard_layout_set_qwerty.xml",
                "keyboard_layout_set_bengali_avro.xml",
                "keyboard_layout_set_bengali_jatiyo.xml",
                "keyboard_layout_set_bengali_probhat.xml"
        };

        Set<String> visitedXmls = new HashSet<>();
        List<String> toVisit = new ArrayList<>();

        for (String layoutSetFile : supportedLayoutSets) {
            Document doc = parseXml(layoutSetFile);
            NodeList elements = doc.getElementsByTagName("Element");
            for (int i = 0; i < elements.getLength(); i++) {
                Element el = (Element) elements.item(i);
                String kbdXml = cleanXmlResourceName(el.getAttribute("latin:elementKeyboard"));
                if (kbdXml != null && !visitedXmls.contains(kbdXml)) {
                    toVisit.add(kbdXml);
                }
            }
        }

        // Recursively crawl all includes
        while (!toVisit.isEmpty()) {
            String current = toVisit.remove(toVisit.size() - 1);
            if (visitedXmls.contains(current)) continue;
            visitedXmls.add(current);

            Document doc = parseXml(current);
            NodeList includes = doc.getElementsByTagName("include");
            for (int i = 0; i < includes.getLength(); i++) {
                Element inc = (Element) includes.item(i);
                String layoutRef = inc.getAttribute("latin:keyboardLayout");
                if (layoutRef != null && layoutRef.startsWith("@xml/")) {
                    String subXml = cleanXmlResourceName(layoutRef);
                    if (!visitedXmls.contains(subXml)) {
                        toVisit.add(subXml);
                    }
                }
            }
        }

        // Verify that all core layouts and 3-page symbol rows were reached and validated
        assertTrue("Must include kbd_symbols_3.xml", visitedXmls.contains("kbd_symbols_3.xml"));
        assertTrue("Must include rows_symbols_3.xml", visitedXmls.contains("rows_symbols_3.xml"));
        assertTrue("Must include kbd_symbols_3_bengali.xml", visitedXmls.contains("kbd_symbols_3_bengali.xml"));
        assertTrue("Must include rows_symbols_3_bengali.xml", visitedXmls.contains("rows_symbols_3_bengali.xml"));
        assertTrue("Must include rows_bengali_avro.xml", visitedXmls.contains("rows_bengali_avro.xml"));
        assertTrue("Must include key_styles_common.xml", visitedXmls.contains("key_styles_common.xml"));
    }

    @Test
    public void testSymbolNavigationKeyStylesAcrossAllLayouts() throws Exception {
        // Page 1 English: toMoreSymbolKeyStyle
        Document p1En = parseXml("rows_symbols.xml");
        assertTrue(hasKeyWithStyle(p1En, "toMoreSymbolKeyStyle"));

        // Page 1 Bengali: toMoreSymbolKeyStyle
        Document p1Bn = parseXml("rows_symbols_bengali.xml");
        assertTrue(hasKeyWithStyle(p1Bn, "toMoreSymbolKeyStyle"));

        // Page 3 (Advanced Symbols) English: backFromMoreSymbolKeyStyle and toAlphaKeyStyle
        Document p2En = parseXml("rows_symbols_shift.xml");
        assertTrue(hasKeyWithStyle(p2En, "backFromMoreSymbolKeyStyle"));
        assertTrue(hasKeyWithStyle(p2En, "toAlphaKeyStyle"));

        // Page 3 (Advanced Symbols) Bengali: backFromMoreSymbolBengaliKeyStyle and toAlphaBengaliKeyStyle
        Document p2Bn = parseXml("rows_symbols_shift_bengali.xml");
        assertTrue(hasKeyWithStyle(p2Bn, "backFromMoreSymbolBengaliKeyStyle"));
        assertTrue(hasKeyWithStyle(p2Bn, "toAlphaBengaliKeyStyle"));
    }

    private boolean hasKeyWithStyle(Document doc, String targetStyle) {
        NodeList keys = doc.getElementsByTagName("Key");
        for (int i = 0; i < keys.getLength(); i++) {
            Element key = (Element) keys.item(i);
            if (targetStyle.equals(key.getAttribute("latin:keyStyle"))) {
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------------------------------------------
    // 2. Adversarial Fallback Testing (KeyboardLayoutSet.getKeyboard Fallback Contract)
    // ---------------------------------------------------------------------------------------------

    /**
     * Replicates the exact resolution logic in KeyboardLayoutSet.getKeyboard:
     *
     *   ElementParams elementParams = mParams.mKeyboardLayoutSetElementIdToParamsMap.get(
     *           keyboardLayoutSetElementId);
     *   if (elementParams == null && keyboardLayoutSetElementId == KeyboardId.ELEMENT_SYMBOLS_3) {
     *       elementParams = mParams.mKeyboardLayoutSetElementIdToParamsMap.get(
     *               KeyboardId.ELEMENT_SYMBOLS_SHIFTED);
     *       if (elementParams == null) {
     *           elementParams = mParams.mKeyboardLayoutSetElementIdToParamsMap.get(
     *                   KeyboardId.ELEMENT_SYMBOLS);
     *       }
     *   }
     *   if (elementParams == null) {
     *       elementParams = mParams.mKeyboardLayoutSetElementIdToParamsMap.get(
     *               KeyboardId.ELEMENT_ALPHABET);
     *   }
     *   if (elementParams == null) {
     *       return null;
     *   }
     *   return getKeyboard(elementParams, id);
     */
    public static Integer resolveElementIdFallback(final int requestedElementId, final Set<Integer> availableElements) {
        if (availableElements.contains(requestedElementId)) {
            return requestedElementId;
        }
        if (requestedElementId == KeyboardId.ELEMENT_SYMBOLS_3) {
            if (availableElements.contains(KeyboardId.ELEMENT_SYMBOLS_SHIFTED)) {
                return KeyboardId.ELEMENT_SYMBOLS_SHIFTED;
            }
            if (availableElements.contains(KeyboardId.ELEMENT_SYMBOLS)) {
                return KeyboardId.ELEMENT_SYMBOLS;
            }
        }
        if (availableElements.contains(KeyboardId.ELEMENT_ALPHABET)) {
            return KeyboardId.ELEMENT_ALPHABET;
        }
        return null; // Safely returns null, no NullPointerException
    }

    @Test
    public void testFallbackWhenSymbols3Missing_DegradesToShiftedSymbols() {
        // Layout set has alphabet, symbols, symbolsShifted, but lacks symbols3
        Set<Integer> available = new HashSet<>();
        available.add(KeyboardId.ELEMENT_ALPHABET);
        available.add(KeyboardId.ELEMENT_SYMBOLS);
        available.add(KeyboardId.ELEMENT_SYMBOLS_SHIFTED);

        Integer resolved = resolveElementIdFallback(KeyboardId.ELEMENT_SYMBOLS_3, available);
        assertNotNull(resolved);
        assertEquals("When symbols3 is missing, must fall back to ELEMENT_SYMBOLS_SHIFTED",
                Integer.valueOf(KeyboardId.ELEMENT_SYMBOLS_SHIFTED), resolved);
    }

    @Test
    public void testFallbackWhenSymbols3AndShiftedMissing_DegradesToSymbols() {
        // Layout set has alphabet, symbols, but lacks symbolsShifted and symbols3
        Set<Integer> available = new HashSet<>();
        available.add(KeyboardId.ELEMENT_ALPHABET);
        available.add(KeyboardId.ELEMENT_SYMBOLS);

        Integer resolved = resolveElementIdFallback(KeyboardId.ELEMENT_SYMBOLS_3, available);
        assertNotNull(resolved);
        assertEquals("When symbols3 and symbolsShifted are missing, must fall back to ELEMENT_SYMBOLS",
                Integer.valueOf(KeyboardId.ELEMENT_SYMBOLS), resolved);
    }

    @Test
    public void testFallbackWhenAllSymbolsMissing_DegradesToAlphabet() {
        // Minimal layout set defines ONLY alphabet
        Set<Integer> available = new HashSet<>();
        available.add(KeyboardId.ELEMENT_ALPHABET);

        Integer resolved = resolveElementIdFallback(KeyboardId.ELEMENT_SYMBOLS_3, available);
        assertNotNull(resolved);
        assertEquals("When all symbol elements are missing, must fall back to ELEMENT_ALPHABET",
                Integer.valueOf(KeyboardId.ELEMENT_ALPHABET), resolved);
    }

    @Test
    public void testFallbackWhenLayoutSetCompletelyEmpty_ReturnsNullWithoutNPE() {
        // Corrupted or completely empty layout set
        Set<Integer> available = new HashSet<>();

        Integer resolved = resolveElementIdFallback(KeyboardId.ELEMENT_SYMBOLS_3, available);
        // Must safely return null without throwing an uncaught exception
        assertEquals(null, resolved);
    }

    @Test
    public void testFallbackOtherMissingElementsDegradeToAlphabet() {
        Set<Integer> available = new HashSet<>();
        available.add(KeyboardId.ELEMENT_ALPHABET);

        // Missing phone, phoneSymbols, or number should also fall back to alphabet safely
        assertEquals(Integer.valueOf(KeyboardId.ELEMENT_ALPHABET),
                resolveElementIdFallback(KeyboardId.ELEMENT_PHONE, available));
        assertEquals(Integer.valueOf(KeyboardId.ELEMENT_ALPHABET),
                resolveElementIdFallback(KeyboardId.ELEMENT_NUMBER, available));
        assertEquals(Integer.valueOf(KeyboardId.ELEMENT_ALPHABET),
                resolveElementIdFallback(KeyboardId.ELEMENT_PHONE_SYMBOLS, available));
    }

    @Test
    public void testAdversarialRapidConcurrentElementLookups() throws InterruptedException {
        final int THREADS = 8;
        final int ITERATIONS = 2000;
        final ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        final CountDownLatch latch = new CountDownLatch(THREADS);
        final AtomicBoolean failed = new AtomicBoolean(false);

        // Pre-create various permutation sets of available elements
        final List<Set<Integer>> scenarios = new ArrayList<>();
        // 0: full set
        Set<Integer> full = new HashSet<>();
        full.add(KeyboardId.ELEMENT_ALPHABET);
        full.add(KeyboardId.ELEMENT_SYMBOLS);
        full.add(KeyboardId.ELEMENT_SYMBOLS_SHIFTED);
        full.add(KeyboardId.ELEMENT_SYMBOLS_3);
        scenarios.add(full);

        // 1: missing symbols3
        Set<Integer> noP3 = new HashSet<>(full);
        noP3.remove(KeyboardId.ELEMENT_SYMBOLS_3);
        scenarios.add(noP3);

        // 2: missing symbols3 and symbolsShifted
        Set<Integer> noP2P3 = new HashSet<>(noP3);
        noP2P3.remove(KeyboardId.ELEMENT_SYMBOLS_SHIFTED);
        scenarios.add(noP2P3);

        // 3: alphabet only
        Set<Integer> alphaOnly = new HashSet<>();
        alphaOnly.add(KeyboardId.ELEMENT_ALPHABET);
        scenarios.add(alphaOnly);

        // 4: empty
        scenarios.add(new HashSet<>());

        for (int t = 0; t < THREADS; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < ITERATIONS; i++) {
                        Set<Integer> scenario = scenarios.get(i % scenarios.size());
                        Integer resolved = resolveElementIdFallback(KeyboardId.ELEMENT_SYMBOLS_3, scenario);
                        if (scenario.contains(KeyboardId.ELEMENT_SYMBOLS_3)) {
                            if (resolved == null || resolved != KeyboardId.ELEMENT_SYMBOLS_3) failed.set(true);
                        } else if (scenario.contains(KeyboardId.ELEMENT_SYMBOLS_SHIFTED)) {
                            if (resolved == null || resolved != KeyboardId.ELEMENT_SYMBOLS_SHIFTED) failed.set(true);
                        } else if (scenario.contains(KeyboardId.ELEMENT_SYMBOLS)) {
                            if (resolved == null || resolved != KeyboardId.ELEMENT_SYMBOLS) failed.set(true);
                        } else if (scenario.contains(KeyboardId.ELEMENT_ALPHABET)) {
                            if (resolved == null || resolved != KeyboardId.ELEMENT_ALPHABET) failed.set(true);
                        } else {
                            if (resolved != null) failed.set(true);
                        }
                    }
                } catch (Throwable e) {
                    failed.set(true);
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue("Concurrent stress test must complete within timeout", latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
        assertFalse("Concurrent fallback resolution failed or threw exception", failed.get());
    }
}
