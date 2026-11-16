package org.futo.inputmethod.latin.bengali;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.futo.inputmethod.latin.DictionaryFacilitator;

public class BanglaSuggestionEngineTest {

    private RidmikParser mParser;

    @Before
    public void setUp() {
        mParser = new RidmikParser();
        BanglaSuggestionEngine.clearLearnedWords();
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
        // account -> ["অ্যাকাউন্ট", "হিসাব", "account"]
        List<String> account = BanglaSuggestionEngine.getCandidates("account", mParser);
        assertNotNull(account);
        assertFalse(account.isEmpty());
        assertEquals("Primary candidate for 'account' must be 'অ্যাকাউন্ট'", "অ্যাকাউন্ট", account.get(0));
        assertEquals("Second candidate for 'account' must be 'হিসাব'", "হিসাব", account.get(1));
        assertEquals("Third candidate for 'account' must be 'account'", "account", account.get(2));

        // Typo resolution: accout -> অ্যাকাউন্ট
        List<String> accout = BanglaSuggestionEngine.getCandidates("accout", mParser);
        assertNotNull(accout);
        assertFalse(accout.isEmpty());
        assertTrue("Typo 'accout' must resolve to 'অ্যাকাউন্ট'", accout.contains("অ্যাকাউন্ট"));

        // school -> ["স্কুল", "বিদ্যালয়", "school"]
        List<String> school = BanglaSuggestionEngine.getCandidates("school", mParser);
        assertNotNull(school);
        assertEquals("Primary candidate for 'school' must be 'স্কুল'", "স্কুল", school.get(0));
        assertEquals("Second candidate for 'school' must be 'বিদ্যালয়'", "বিদ্যালয়", school.get(1));
        assertEquals("Third candidate for 'school' must be 'school'", "school", school.get(2));

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

        // New Conversational Banglish Aliases:
        // ekhonow -> এখনও, এখনো
        List<String> ekhonow = BanglaSuggestionEngine.getCandidates("ekhonow", mParser);
        assertEquals("Primary candidate for 'ekhonow' must be 'এখনও'", "এখনও", ekhonow.get(0));
        assertTrue("ekhonow must contain 'এখনো'", ekhonow.contains("এখনো"));

        // ekhono -> এখনো, এখনও
        List<String> ekhono = BanglaSuggestionEngine.getCandidates("ekhono", mParser);
        assertEquals("Primary candidate for 'ekhono' must be 'এখনো'", "এখনো", ekhono.get(0));
        assertTrue("ekhono must contain 'এখনও'", ekhono.contains("এখনও"));

        // hoynai -> হয়নি, হয় নাই
        List<String> hoynai = BanglaSuggestionEngine.getCandidates("hoynai", mParser);
        assertEquals("Primary candidate for 'hoynai' must be 'হয়নি'", "হয়নি", hoynai.get(0));
        assertTrue("hoynai must contain 'হয় নাই'", hoynai.contains("হয় নাই"));

        // ashtese / astese -> আসতেছে, আসছে
        List<String> ashtese = BanglaSuggestionEngine.getCandidates("ashtese", mParser);
        assertEquals("Primary candidate for 'ashtese' must be 'আসতেছে'", "আসতেছে", ashtese.get(0));
        assertTrue("ashtese must contain 'আসছে'", ashtese.contains("আসছে"));

        List<String> astese = BanglaSuggestionEngine.getCandidates("astese", mParser);
        assertEquals("Primary candidate for 'astese' must be 'আসতেছে'", "আসতেছে", astese.get(0));
        assertTrue("astese must contain 'আসছে'", astese.contains("আসছে"));

        // likhe -> লিখে
        List<String> likhe = BanglaSuggestionEngine.getCandidates("likhe", mParser);
        assertEquals("Primary candidate for 'likhe' must be 'লিখে'", "লিখে", likhe.get(0));

        // dile -> দিলে
        List<String> dile = BanglaSuggestionEngine.getCandidates("dile", mParser);
        assertEquals("Primary candidate for 'dile' must be 'দিলে'", "দিলে", dile.get(0));

        // korbo -> করব, করবো
        List<String> korbo = BanglaSuggestionEngine.getCandidates("korbo", mParser);
        assertEquals("Primary candidate for 'korbo' must be 'করব'", "করব", korbo.get(0));
        assertTrue("korbo must contain 'করবো'", korbo.contains("করবো"));

        // ghumabo -> ঘুমাবো, ঘুমাব
        List<String> ghumabo = BanglaSuggestionEngine.getCandidates("ghumabo", mParser);
        assertEquals("Primary candidate for 'ghumabo' must be 'ঘুমাবো'", "ঘুমাবো", ghumabo.get(0));
        assertTrue("ghumabo must contain 'ঘুমাব'", ghumabo.contains("ঘুমাব"));

        // baki -> বাকি
        List<String> baki = BanglaSuggestionEngine.getCandidates("baki", mParser);
        assertEquals("Primary candidate for 'baki' must be 'বাকি'", "বাকি", baki.get(0));

        // High-frequency Banglish Conversational Aliases (Ridmik & Gboard standard):
        // jachhi / jacchi -> যাচ্ছি
        List<String> jachhi = BanglaSuggestionEngine.getCandidates("jachhi", mParser);
        assertEquals("Primary candidate for 'jachhi' must be 'যাচ্ছি'", "যাচ্ছি", jachhi.get(0));

        List<String> jacchi = BanglaSuggestionEngine.getCandidates("jacchi", mParser);
        assertEquals("Primary candidate for 'jacchi' must be 'যাচ্ছি'", "যাচ্ছি", jacchi.get(0));

        // jabo -> যাব
        List<String> jabo = BanglaSuggestionEngine.getCandidates("jabo", mParser);
        assertEquals("Primary candidate for 'jabo' must be 'যাব'", "যাব", jabo.get(0));

        // thik -> ঠিক
        List<String> thik = BanglaSuggestionEngine.getCandidates("thik", mParser);
        assertEquals("Primary candidate for 'thik' must be 'ঠিক'", "ঠিক", thik.get(0));

        // chai -> চাই
        List<String> chai = BanglaSuggestionEngine.getCandidates("chai", mParser);
        assertEquals("Primary candidate for 'chai' must be 'চাই'", "চাই", chai.get(0));

        // chaibena -> চাইবে না
        List<String> chaibena = BanglaSuggestionEngine.getCandidates("chaibena", mParser);
        assertEquals("Primary candidate for 'chaibena' must be 'চাইবে না'", "চাইবে না", chaibena.get(0));

        // eita -> এইটা
        List<String> eita = BanglaSuggestionEngine.getCandidates("eita", mParser);
        assertEquals("Primary candidate for 'eita' must be 'এইটা'", "এইটা", eita.get(0));

        // ekta -> একটা
        List<String> ekta = BanglaSuggestionEngine.getCandidates("ekta", mParser);
        assertEquals("Primary candidate for 'ekta' must be 'একটা'", "একটা", ekta.get(0));

        // koro -> করো
        List<String> koro = BanglaSuggestionEngine.getCandidates("koro", mParser);
        assertEquals("Primary candidate for 'koro' must be 'করো'", "করো", koro.get(0));

        // bolo -> বলো
        List<String> bolo = BanglaSuggestionEngine.getCandidates("bolo", mParser);
        assertEquals("Primary candidate for 'bolo' must be 'বলো'", "বলো", bolo.get(0));

        // dekho -> দেখো
        List<String> dekho = BanglaSuggestionEngine.getCandidates("dekho", mParser);
        assertEquals("Primary candidate for 'dekho' must be 'দেখো'", "দেখো", dekho.get(0));

        // kono -> কোনো
        List<String> kono = BanglaSuggestionEngine.getCandidates("kono", mParser);
        assertEquals("Primary candidate for 'kono' must be 'কোনো'", "কোনো", kono.get(0));

        // shathe -> সাথে
        List<String> shathe = BanglaSuggestionEngine.getCandidates("shathe", mParser);
        assertEquals("Primary candidate for 'shathe' must be 'সাথে'", "সাথে", shathe.get(0));

        // moto -> মতো
        List<String> moto = BanglaSuggestionEngine.getCandidates("moto", mParser);
        assertEquals("Primary candidate for 'moto' must be 'মতো'", "মতো", moto.get(0));

        // avro / avra -> অভ্র
        List<String> avro = BanglaSuggestionEngine.getCandidates("avro", mParser);
        assertEquals("Primary candidate for 'avro' must be 'অভ্র'", "অভ্র", avro.get(0));

        List<String> avra = BanglaSuggestionEngine.getCandidates("avra", mParser);
        assertEquals("Primary candidate for 'avra' must be 'অভ্র'", "অভ্র", avra.get(0));

        // ridmik -> রিদ্মিক
        List<String> ridmik = BanglaSuggestionEngine.getCandidates("ridmik", mParser);
        assertEquals("Primary candidate for 'ridmik' must be 'রিদ্মিক'", "রিদ্মিক", ridmik.get(0));

        // gboard -> জিবোর্ড
        List<String> gboard = BanglaSuggestionEngine.getCandidates("gboard", mParser);
        assertEquals("Primary candidate for 'gboard' must be 'জিবোর্ড'", "জিবোর্ড", gboard.get(0));
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

    @Test
    public void testZeroShiftConjunctsAndVisarga() {
        // Visarga words: dukkho -> দুঃখ
        List<String> dukkho = BanglaSuggestionEngine.getCandidates("dukkho", mParser);
        assertNotNull(dukkho);
        assertFalse(dukkho.isEmpty());
        assertEquals("দুঃখ", dukkho.get(0));

        // dukhi -> দুঃখী
        List<String> dukhi = BanglaSuggestionEngine.getCandidates("dukhi", mParser);
        assertNotNull(dukhi);
        assertFalse(dukhi.isEmpty());
        assertEquals("দুঃখী", dukhi.get(0));

        // du:kho -> দুঃখ
        List<String> du_kho = BanglaSuggestionEngine.getCandidates("du:kho", mParser);
        assertNotNull(du_kho);
        assertFalse(du_kho.isEmpty());
        assertEquals("দুঃখ", du_kho.get(0));

        // : -> ঃ
        List<String> colon = BanglaSuggestionEngine.getCandidates(":", mParser);
        assertNotNull(colon);
        assertFalse(colon.isEmpty());
        assertEquals("ঃ", colon.get(0));

        // HH -> ঃ
        List<String> doubleH = BanglaSuggestionEngine.getCandidates("HH", mParser);
        assertNotNull(doubleH);
        assertFalse(doubleH.isEmpty());
        assertEquals("ঃ", doubleH.get(0));

        // Zero-shift: gy -> জ্ঞ
        List<String> gy = BanglaSuggestionEngine.getCandidates("gy", mParser);
        assertNotNull(gy);
        assertFalse(gy.isEmpty());
        assertEquals("জ্ঞ", gy.get(0));

        // sht -> ষ্ট
        List<String> sht = BanglaSuggestionEngine.getCandidates("sht", mParser);
        assertNotNull(sht);
        assertFalse(sht.isEmpty());
        assertEquals("ষ্ট", sht.get(0));

        // cch -> চ্ছ
        List<String> cch = BanglaSuggestionEngine.getCandidates("cch", mParser);
        assertNotNull(cch);
        assertFalse(cch.isEmpty());
        assertEquals("চ্ছ", cch.get(0));

        // ngg -> ঙ্গ
        List<String> ngg = BanglaSuggestionEngine.getCandidates("ngg", mParser);
        assertNotNull(ngg);
        assertFalse(ngg.isEmpty());
        assertEquals("ঙ্গ", ngg.get(0));

        // dhdh -> দ্ধ
        List<String> dhdh = BanglaSuggestionEngine.getCandidates("dhdh", mParser);
        assertNotNull(dhdh);
        assertFalse(dhdh.isEmpty());
        assertEquals("দ্ধ", dhdh.get(0));

        // tth -> ঠ
        List<String> tth = BanglaSuggestionEngine.getCandidates("tth", mParser);
        assertNotNull(tth);
        assertFalse(tth.isEmpty());
        assertEquals("ঠ", tth.get(0));

        // kkh -> ক্ষ
        List<String> kkh = BanglaSuggestionEngine.getCandidates("kkh", mParser);
        assertNotNull(kkh);
        assertFalse(kkh.isEmpty());
        assertEquals("ক্ষ", kkh.get(0));

        // prosno -> প্রশ্ন
        List<String> prosno = BanglaSuggestionEngine.getCandidates("prosno", mParser);
        assertNotNull(prosno);
        assertFalse(prosno.isEmpty());
        assertEquals("প্রশ্ন", prosno.get(0));

        // somossa -> সমস্যা
        List<String> somossa = BanglaSuggestionEngine.getCandidates("somossa", mParser);
        assertNotNull(somossa);
        assertFalse(somossa.isEmpty());
        assertEquals("সমস্যা", somossa.get(0));

        // bismito -> বিস্মিত
        List<String> bismito = BanglaSuggestionEngine.getCandidates("bismito", mParser);
        assertNotNull(bismito);
        assertFalse(bismito.isEmpty());
        assertEquals("বিস্মিত", bismito.get(0));

        // Direct parser tests for zero-shift conjuncts and Visarga
        assertEquals("ঃ", mParser.toBangla("HH"));
        assertEquals("ঃ", mParser.toBangla("hh"));
        assertEquals("দুঃখ", mParser.toBangla("duhhkho"));
        assertEquals("ঙ্গ", mParser.toBangla("ngg"));
        assertEquals("চ্ছ", mParser.toBangla("cch"));
        assertEquals("ষ্ট", mParser.toBangla("sht"));
        assertEquals("ঠ", mParser.toBangla("tth"));
        assertEquals("দ্ধ", mParser.toBangla("dhdh"));
        assertEquals("জ্ঞ", mParser.toBangla("gy"));
    }

    @Test
    public void testEmojiSuggestions() {
        assertEquals("😭", BanglaSuggestionEngine.getEmojiForWord("dukkho"));
        assertEquals("😭", BanglaSuggestionEngine.getEmojiForWord("দুঃখ"));
        assertEquals("❤️", BanglaSuggestionEngine.getEmojiForWord("valobasha"));
        assertEquals("❤️", BanglaSuggestionEngine.getEmojiForWord("ভালোবাসা"));
        assertEquals("☕", BanglaSuggestionEngine.getEmojiForWord("cha"));
        assertEquals("💰", BanglaSuggestionEngine.getEmojiForWord("taka"));
    }

    @Test
    public void testFuzzyPhoneticVariants() {
        // daktar -> Daktar -> ডাক্তার
        List<String> daktarVariants = BanglaSuggestionEngine.generatePhoneticVariants("daktar");
        assertTrue("Variants for 'daktar' should include 'Daktar'", daktarVariants.contains("Daktar"));
        assertEquals("ডাক্তার", mParser.toBangla("Daktar"));

        // thikana -> Thikana -> ঠিকানা
        List<String> thikanaVariants = BanglaSuggestionEngine.generatePhoneticVariants("thikana");
        assertTrue("Variants for 'thikana' should include 'Thikana'", thikanaVariants.contains("Thikana"));
        assertEquals("ঠিকানা", mParser.toBangla("Thikana"));

        // chithi -> ciThi -> চিঠি
        List<String> chithiVariants = BanglaSuggestionEngine.generatePhoneticVariants("chithi");
        assertTrue("Variants for 'chithi' should include 'ciThi'", chithiVariants.contains("ciThi"));
        assertEquals("চিঠি", mParser.toBangla("ciThi"));

        // mishti -> mishTi -> মিষ্টি
        List<String> mishtiVariants = BanglaSuggestionEngine.generatePhoneticVariants("mishti");
        assertTrue("Variants for 'mishti' should include 'mishTi'", mishtiVariants.contains("mishTi"));

        // thanda -> ThanDa -> ঠান্ডা
        List<String> thandaVariants = BanglaSuggestionEngine.generatePhoneticVariants("thanda");
        assertTrue("Variants for 'thanda' should include 'ThanDa'", thandaVariants.contains("ThanDa"));
        assertEquals("ঠান্ডা", mParser.toBangla("ThanDa"));

        // dakat -> Dakat -> ডাকাত
        List<String> dakatVariants = BanglaSuggestionEngine.generatePhoneticVariants("dakat");
        assertTrue("Variants for 'dakat' should include 'Dakat'", dakatVariants.contains("Dakat"));
        assertEquals("ডাকাত", mParser.toBangla("Dakat"));

        // hothat -> hoThaTH -> হঠাৎ (th -> Th retroflex + t -> TH khondiyo to)
        List<String> hothatVariants = BanglaSuggestionEngine.generatePhoneticVariants("hothat");
        assertTrue("Variants for 'hothat' should include 'hoThaTH'", hothatVariants.contains("hoThaTH"));
        assertEquals("হঠাৎ", mParser.toBangla("hoThaTH"));

        // 4-site word: sondesh (s, n, d, sh) generates non-empty variants
        List<String> sondeshVariants = BanglaSuggestionEngine.generatePhoneticVariants("sondesh");
        assertNotNull(sondeshVariants);
        assertFalse(sondeshVariants.isEmpty());
    }

    @Test
    public void testFuzzyMatchingRankingWithDictionary() {
        final Map<String, Integer> dictionaryFrequencies = new HashMap<>();
        dictionaryFrequencies.put("ডাক্তার", 180);
        dictionaryFrequencies.put("ঠিকানা", 150);
        dictionaryFrequencies.put("চিঠি", 160);
        dictionaryFrequencies.put("ঠান্ডা", 170);
        dictionaryFrequencies.put("ডাকাত", 140);
        dictionaryFrequencies.put("মিষ্টি", 190);
        dictionaryFrequencies.put("সন্দেশ", 165);
        dictionaryFrequencies.put("শান্তি", 175);
        dictionaryFrequencies.put("হঠাৎ", 185);

        final DictionaryFacilitator fakeDict = (DictionaryFacilitator) Proxy.newProxyInstance(
                DictionaryFacilitator.class.getClassLoader(),
                new Class<?>[]{DictionaryFacilitator.class},
                (proxy, method, args) -> {
                    if ("isValidSpellingWord".equals(method.getName())) {
                        String word = (String) args[0];
                        return dictionaryFrequencies.containsKey(word);
                    }
                    if ("getFrequency".equals(method.getName())) {
                        String word = (String) args[0];
                        return dictionaryFrequencies.getOrDefault(word, 0);
                    }
                    if ("hasAtLeastOneInitializedMainDictionary".equals(method.getName())) {
                        return true;
                    }
                    return null;
                }
        );

        // daktar -> ডাক্তার ranks at top
        List<String> daktarCands = BanglaSuggestionEngine.getCandidates("daktar", mParser, fakeDict);
        assertNotNull(daktarCands);
        assertFalse(daktarCands.isEmpty());
        assertEquals("ডাক্তার", daktarCands.get(0));

        // thikana -> ঠিকানা ranks at top
        List<String> thikanaCands = BanglaSuggestionEngine.getCandidates("thikana", mParser, fakeDict);
        assertNotNull(thikanaCands);
        assertFalse(thikanaCands.isEmpty());
        assertEquals("ঠিকানা", thikanaCands.get(0));

        // chithi -> চিঠি ranks at top
        List<String> chithiCands = BanglaSuggestionEngine.getCandidates("chithi", mParser, fakeDict);
        assertNotNull(chithiCands);
        assertFalse(chithiCands.isEmpty());
        assertEquals("চিঠি", chithiCands.get(0));

        // thanda -> ঠান্ডা ranks at top
        List<String> thandaCands = BanglaSuggestionEngine.getCandidates("thanda", mParser, fakeDict);
        assertNotNull(thandaCands);
        assertFalse(thandaCands.isEmpty());
        assertEquals("ঠান্ডা", thandaCands.get(0));

        // dakat -> ডাকাত ranks at top
        List<String> dakatCands = BanglaSuggestionEngine.getCandidates("dakat", mParser, fakeDict);
        assertNotNull(dakatCands);
        assertFalse(dakatCands.isEmpty());
        assertEquals("ডাকাত", dakatCands.get(0));

        // mishti -> মিষ্টি ranks at top
        List<String> mishtiCands = BanglaSuggestionEngine.getCandidates("mishti", mParser, fakeDict);
        assertNotNull(mishtiCands);
        assertFalse(mishtiCands.isEmpty());
        assertEquals("মিষ্টি", mishtiCands.get(0));

        // sondesh -> সন্দেশ ranks at top
        List<String> sondeshCands = BanglaSuggestionEngine.getCandidates("sondesh", mParser, fakeDict);
        assertNotNull(sondeshCands);
        assertFalse(sondeshCands.isEmpty());
        assertEquals("সন্দেশ", sondeshCands.get(0));

        // shanti -> শান্তি ranks at top
        List<String> shantiCands = BanglaSuggestionEngine.getCandidates("shanti", mParser, fakeDict);
        assertNotNull(shantiCands);
        assertFalse(shantiCands.isEmpty());
        assertEquals("শান্তি", shantiCands.get(0));

        // hothat -> হঠাৎ ranks at top
        List<String> hothatCands = BanglaSuggestionEngine.getCandidates("hothat", mParser, fakeDict);
        assertNotNull(hothatCands);
        assertFalse(hothatCands.isEmpty());
        assertEquals("হঠাৎ", hothatCands.get(0));
    }

    @Test
    public void testBengaliNumeralsQuickPick() {
        // Pure digits: 500 -> 500 at index 0, bnNumerals ৫০০ at index 1
        List<String> cands500 = BanglaSuggestionEngine.getCandidates("500", mParser);
        assertNotNull(cands500);
        assertFalse(cands500.isEmpty());
        assertEquals("500", cands500.get(0));
        assertEquals("৫০০", cands500.get(1));

        // Pure digits: 123 -> 123 at index 0, bnNumerals ১২৩ at index 1
        List<String> cands123 = BanglaSuggestionEngine.getCandidates("123", mParser);
        assertNotNull(cands123);
        assertFalse(cands123.isEmpty());
        assertEquals("123", cands123.get(0));
        assertEquals("১২৩", cands123.get(1));

        // Formatted / decimal numbers: 10.5 -> 10.5 at index 0, ১০.৫ at index 1
        List<String> cands105 = BanglaSuggestionEngine.getCandidates("10.5", mParser);
        assertNotNull(cands105);
        assertFalse(cands105.isEmpty());
        assertEquals("10.5", cands105.get(0));
        assertEquals("১০.৫", cands105.get(1));

        // Percentage: 100% -> 100% at index 0, ১০০% at index 1
        List<String> cands100p = BanglaSuggestionEngine.getCandidates("100%", mParser);
        assertNotNull(cands100p);
        assertFalse(cands100p.isEmpty());
        assertEquals("100%", cands100p.get(0));
        assertEquals("১০০%", cands100p.get(1));

        // Thousands comma: 1,000 -> 1,000 at index 0, ১,০০০ at index 1
        List<String> cands1k = BanglaSuggestionEngine.getCandidates("1,000", mParser);
        assertNotNull(cands1k);
        assertFalse(cands1k.isEmpty());
        assertEquals("1,000", cands1k.get(0));
        assertEquals("১,০০০", cands1k.get(1));

        // Guard learned words against displacing numbers:
        BanglaSuggestionEngine.learnWord("500", "পাঁচশত");
        List<String> candsAfterLearn = BanglaSuggestionEngine.getCandidates("500", mParser);
        assertEquals("500 must remain at index 0", "500", candsAfterLearn.get(0));
        assertEquals("৫০০ must remain at index 1", "৫০০", candsAfterLearn.get(1));
        assertTrue("Learned word should be present in candidates", candsAfterLearn.contains("পাঁচশত"));
        assertTrue("Index of পাঁচশত must be >= 2", candsAfterLearn.indexOf("পাঁচশত") >= 2);

        // Mixed alphanumeric: test123 -> contains test১২৩
        List<String> mixed = BanglaSuggestionEngine.getCandidates("test123", mParser);
        assertNotNull(mixed);
        assertTrue("Mixed alphanumeric candidate should include 'test১২৩'", mixed.contains("test১২৩"));
    }

    @Test
    public void testNextWordPredictions() {
        // আমি -> তোমাকে, ভালো, যাব
        List<String> predictionsAmi = BanglaSuggestionEngine.getNextWordPredictions("আমি", null);
        assertNotNull(predictionsAmi);
        assertTrue(predictionsAmi.contains("তোমাকে"));
        assertTrue(predictionsAmi.contains("ভালো"));
        assertTrue(predictionsAmi.contains("যাব"));

        // তুমি -> কেমন, কোথায়, কী
        List<String> predictionsTumi = BanglaSuggestionEngine.getNextWordPredictions("তুমি", null);
        assertNotNull(predictionsTumi);
        assertTrue(predictionsTumi.contains("কেমন"));
        assertTrue(predictionsTumi.contains("কোথায়"));

        // আমার -> মনে, কাছে, ভাই
        List<String> predictionsAmar = BanglaSuggestionEngine.getNextWordPredictions("আমার", null);
        assertNotNull(predictionsAmar);
        assertTrue(predictionsAmar.contains("মনে"));

        // Punctuation-stripped query: "আমি।" should resolve same as "আমি"
        List<String> predictionsPunct = BanglaSuggestionEngine.getNextWordPredictions("আমি।", null);
        assertNotNull(predictionsPunct);
        assertTrue(predictionsPunct.contains("তোমাকে"));

        List<String> predictionsComma = BanglaSuggestionEngine.getNextWordPredictions("তুমি, ", null);
        assertNotNull(predictionsComma);
        assertTrue(predictionsComma.contains("কেমন"));

        // Empty / null should return empty list
        List<String> empty = BanglaSuggestionEngine.getNextWordPredictions("", null);
        assertNotNull(empty);
        assertTrue(empty.isEmpty());

        List<String> nullWord = BanglaSuggestionEngine.getNextWordPredictions(null, null);
        assertNotNull(nullWord);
        assertTrue(nullWord.isEmpty());
    }

    @Test
    public void testDynamicWordLearningAndBigrams() {
        BanglaSuggestionEngine.clearMemoryOnlyForTest();

        // 1. English words and loanwords can be learned
        BanglaSuggestionEngine.learnWord("school", "school");
        List<String> learnedSchool = BanglaSuggestionEngine.getLearnedCandidates("school");
        assertNotNull(learnedSchool);
        assertTrue("Learned candidates must include English word 'school'", learnedSchool.contains("school"));

        // 2. Prefix matching on learned words (sch -> school)
        List<String> prefixMatch = BanglaSuggestionEngine.getLearnedCandidates("sch");
        assertNotNull(prefixMatch);
        assertTrue("Prefix match for 'sch' must include 'school'", prefixMatch.contains("school"));

        // 3. Dynamic Bigram learning & prediction
        BanglaSuggestionEngine.recordBigram("school", "bus");
        List<String> predictionsSchool = BanglaSuggestionEngine.getNextWordPredictions("school", null);
        assertNotNull(predictionsSchool);
        assertFalse(predictionsSchool.isEmpty());
        assertEquals("Dynamic learned bigram must take top prediction priority", "bus", predictionsSchool.get(0));

        // 4. Multiple bigrams sorted by frequency & recency
        BanglaSuggestionEngine.recordBigram("school", "van");
        BanglaSuggestionEngine.recordBigram("school", "van");
        List<String> updatedPredictions = BanglaSuggestionEngine.getNextWordPredictions("school", null);
        assertNotNull(updatedPredictions);
        assertEquals("'van' with higher count must be primary prediction", "van", updatedPredictions.get(0));
        assertEquals("'bus' must be secondary prediction", "bus", updatedPredictions.get(1));

        // 5. English bigrams case insensitivity (School -> van)
        List<String> capPredictions = BanglaSuggestionEngine.getNextWordPredictions("School", null);
        assertNotNull(capPredictions);
        assertFalse(capPredictions.isEmpty());
        assertEquals("Sentence-case 'School' must resolve bigram predictions", "van", capPredictions.get(0));

        // 6. Learned English word priority in getCandidates
        List<String> candidatesSchool = BanglaSuggestionEngine.getCandidates("school", mParser, null);
        assertNotNull(candidatesSchool);
        assertFalse(candidatesSchool.isEmpty());
        assertEquals("Learned English word 'school' must be prioritized at index 0", "school", candidatesSchool.get(0));

        // 7. Regular Bengali phonetics (e.g. 'pro') must NOT be hijacked by curated chat prefix matching
        List<String> proCandidates = BanglaSuggestionEngine.getCandidates("pro", mParser, null);
        assertNotNull(proCandidates);
        assertFalse(proCandidates.isEmpty());
        assertEquals("Phonetic 'pro' must produce 'প্র', never hijacked by curated chat words like 'problem'",
                "প্র", proCandidates.get(0));
    }
}
