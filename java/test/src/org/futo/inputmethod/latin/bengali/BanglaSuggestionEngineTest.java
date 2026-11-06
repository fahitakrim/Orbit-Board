package org.futo.inputmethod.latin.bengali;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BanglaSuggestionEngineTest {

    private RidmikParser mParser;

    @Before
    public void setUp() {
        mParser = new RidmikParser();
    }

    @Test
    public void testConversationalBanglishAliases() {
        // kmn -> কেমন
        List<String> kmn = BanglaSuggestionEngine.getCandidates("kmn", mParser);
        assertNotNull(kmn);
        assertFalse(kmn.isEmpty());
        assertEquals("Primary candidate for 'kmn' must be 'কেমন'", "কেমন", kmn.get(0));

        // amdr -> আমাদের
        List<String> amdr = BanglaSuggestionEngine.getCandidates("amdr", mParser);
        assertEquals("Primary candidate for 'amdr' must be 'আমাদের'", "আমাদের", amdr.get(0));

        // apnr -> আপনার
        List<String> apnr = BanglaSuggestionEngine.getCandidates("apnr", mParser);
        assertEquals("Primary candidate for 'apnr' must be 'আপনার'", "আপনার", apnr.get(0));

        // dhonn -> ধন্যবাদ
        List<String> dhonn = BanglaSuggestionEngine.getCandidates("dhonn", mParser);
        assertEquals("Primary candidate for 'dhonn' must be 'ধন্যবাদ'", "ধন্যবাদ", dhonn.get(0));

        // valo -> ভালো
        List<String> valo = BanglaSuggestionEngine.getCandidates("valo", mParser);
        assertEquals("Primary candidate for 'valo' must be 'ভালো'", "ভালো", valo.get(0));

        // valona -> ভালো না
        List<String> valona = BanglaSuggestionEngine.getCandidates("valona", mParser);
        assertEquals("Primary candidate for 'valona' must be 'ভালো না'", "ভালো না", valona.get(0));

        // thikase -> ঠিক আছে
        List<String> thikase = BanglaSuggestionEngine.getCandidates("thikase", mParser);
        assertEquals("Primary candidate for 'thikase' must be 'ঠিক আছে'", "ঠিক আছে", thikase.get(0));

        // kisu -> কিছু
        List<String> kisu = BanglaSuggestionEngine.getCandidates("kisu", mParser);
        assertEquals("Primary candidate for 'kisu' must be 'কিছু'", "কিছু", kisu.get(0));

        // shob -> সব
        List<String> shob = BanglaSuggestionEngine.getCandidates("shob", mParser);
        assertEquals("Primary candidate for 'shob' must be 'সব'", "সব", shob.get(0));

        // sundor -> সুন্দর
        List<String> sundor = BanglaSuggestionEngine.getCandidates("sundor", mParser);
        assertEquals("Primary candidate for 'sundor' must be 'সুন্দর'", "সুন্দর", sundor.get(0));

        // pic -> ছবি
        List<String> pic = BanglaSuggestionEngine.getCandidates("pic", mParser);
        assertEquals("Primary candidate for 'pic' must be 'ছবি'", "ছবি", pic.get(0));

        // vai -> ভাই
        List<String> vai = BanglaSuggestionEngine.getCandidates("vai", mParser);
        assertEquals("Primary candidate for 'vai' must be 'ভাই'", "ভাই", vai.get(0));

        // ingreji -> ইংরেজি
        List<String> ingreji = BanglaSuggestionEngine.getCandidates("ingreji", mParser);
        assertEquals("Primary candidate for 'ingreji' must be 'ইংরেজি'", "ইংরেজি", ingreji.get(0));

        // ingrezi -> ইংরেজি
        List<String> ingrezi = BanglaSuggestionEngine.getCandidates("ingrezi", mParser);
        assertEquals("Primary candidate for 'ingrezi' must be 'ইংরেজি'", "ইংরেজি", ingrezi.get(0));

        // english -> ইংরেজি
        List<String> english = BanglaSuggestionEngine.getCandidates("english", mParser);
        assertEquals("Primary candidate for 'english' must be 'ইংরেজি'", "ইংরেজি", english.get(0));

        // Loanwords verification
        // account -> একাউন্ট
        List<String> account = BanglaSuggestionEngine.getCandidates("account", mParser);
        assertNotNull(account);
        assertFalse(account.isEmpty());
        assertEquals("Primary candidate for 'account' must be 'একাউন্ট'", "একাউন্ট", account.get(0));

        // Typo resolution: accout -> একাউন্ট
        List<String> accout = BanglaSuggestionEngine.getCandidates("accout", mParser);
        assertNotNull(accout);
        assertFalse(accout.isEmpty());
        assertTrue("Typo 'accout' must resolve to 'একাউন্ট'", accout.contains("একাউন্ট"));

        // use -> ইউজ
        List<String> use = BanglaSuggestionEngine.getCandidates("use", mParser);
        assertNotNull(use);
        assertFalse(use.isEmpty());
        assertEquals("Primary candidate for 'use' must be 'ইউজ'", "ইউজ", use.get(0));

        // post -> পোস্ট
        List<String> post = BanglaSuggestionEngine.getCandidates("post", mParser);
        assertEquals("Primary candidate for 'post' must be 'পোস্ট'", "পোস্ট", post.get(0));

        // update -> আপডেট
        List<String> update = BanglaSuggestionEngine.getCandidates("update", mParser);
        assertEquals("Primary candidate for 'update' must be 'আপডেট'", "আপডেট", update.get(0));
    }

    @Test
    public void testBilingualEnglishCandidates() {
        // accout -> [account, accout]
        List<String> engAccout = BanglaSuggestionEngine.getEnglishCandidates("accout");
        assertNotNull(engAccout);
        assertTrue("Must contain corrected 'account'", engAccout.contains("account"));
        assertTrue("Must contain typed 'accout'", engAccout.contains("accout"));

        // Title case preservation: Accout -> Account
        List<String> engTitle = BanglaSuggestionEngine.getEnglishCandidates("Accout");
        assertTrue("Must preserve title case 'Account'", engTitle.contains("Account"));

        // All upper case preservation: ACCOUT -> ACCOUNT
        List<String> engUpper = BanglaSuggestionEngine.getEnglishCandidates("ACCOUT");
        assertTrue("Must preserve upper case 'ACCOUNT'", engUpper.contains("ACCOUNT"));

        // Common loanwords
        List<String> engFb = BanglaSuggestionEngine.getEnglishCandidates("fb");
        assertTrue("Must contain 'facebook'", engFb.contains("facebook"));

        List<String> engBksh = BanglaSuggestionEngine.getEnglishCandidates("bksh");
        assertTrue("Must contain 'bkash'", engBksh.contains("bkash"));

        List<String> engWifi = BanglaSuggestionEngine.getEnglishCandidates("wifi");
        assertTrue("Must contain 'wifi'", engWifi.contains("wifi"));
    }

    @Test
    public void testVerbConjugationVariations() {
        // korbo must contain both করব and করবো
        List<String> korbo = BanglaSuggestionEngine.getCandidates("korbo", mParser);
        assertTrue("korbo must contain 'করব'", korbo.contains("করব"));
        assertTrue("korbo must contain 'করবো'", korbo.contains("করবো"));

        // dekhbo must contain both দেখব and দেখবো
        List<String> dekhbo = BanglaSuggestionEngine.getCandidates("dekhbo", mParser);
        assertTrue("dekhbo must contain 'দেখব'", dekhbo.contains("দেখব"));
        assertTrue("dekhbo must contain 'দেখবো'", dekhbo.contains("দেখবো"));
    }

    @Test
    public void testInvariantsAndBounds() {
        String[] words = {"kmn", "valo", "ami", "tumi", "amdr", "apnr", "dhonnobad", "thikase", "rasta", "xyz"};
        for (String w : words) {
            List<String> candidates = BanglaSuggestionEngine.getCandidates(w, mParser);
            assertNotNull(candidates);
            assertFalse(candidates.isEmpty());
            assertTrue("Candidates size must be <= 5", candidates.size() <= 5);

            Set<String> unique = new HashSet<>(candidates);
            assertEquals("Candidate list must not have duplicates for " + w, unique.size(), candidates.size());
        }
    }

    @Test
    public void testAvroRuleEngineSanitizer() {
        // Orphan trailing hasants eliminated
        assertEquals("ক", AvroRuleEngine.INSTANCE.sanitize("ক্"));
        assertEquals("কর", AvroRuleEngine.INSTANCE.sanitize("কর্"));
        // Double hasants collapsed
        assertEquals("ক্ক", AvroRuleEngine.INSTANCE.sanitize("ক্" + "্" + "ক"));
        // Consecutive kars transformed
        assertEquals("কাই", AvroRuleEngine.INSTANCE.sanitize("কা" + "ি"));
        // General parse
        assertNotNull(AvroRuleEngine.INSTANCE.parse("ami"));
        assertEquals("আমি", AvroRuleEngine.INSTANCE.parse("ami"));
    }

    @Test
    public void testBanglaPhoneticMatcherCSRTrie() {
        BanglaPhoneticMatcher matcher = BanglaPhoneticMatcher.Companion.getINSTANCE();
        assertNotNull(matcher);
        List<String> matches = matcher.findMatches("আমি");
        assertNotNull(matches);
        assertTrue(matches.contains("আমি"));

        List<String> prefixMatches = matcher.findMatches("বাংলা");
        assertNotNull(prefixMatches);
        assertTrue(prefixMatches.contains("বাংলা") || prefixMatches.contains("বাংলাদেশ"));
    }
}
