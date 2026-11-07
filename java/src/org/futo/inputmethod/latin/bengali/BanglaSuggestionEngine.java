package org.futo.inputmethod.latin.bengali;

import org.futo.inputmethod.latin.DictionaryFacilitator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Intelligent Bangla Suggestion & Phonetic Completion Engine.
 * 
 * Enhanced with insights and algorithms from Borno (Jayed Ahsan):
 * 1. Conversational Banglish alias resolution with primary candidate priority.
 * 2. Borno caseHandle: protects phonetic uppercase chars while normalizing accidental auto-caps.
 * 3. Borno RemoveConsecutiveKarSigns: eliminates broken dotted circles by transforming consecutive kar signs into full vowels.
 * 4. High-accuracy phonetic normalization (v <-> bh, s <-> sh, verb inflection trailing o/O).
 * 5. Integration with DictionaryFacilitator (main_bn.dict) for authentic Bengali word ranking.
 */
public final class BanglaSuggestionEngine {

        private static final Map<String, String> EMOJI_MAP = new HashMap<>();
    public static String getEmojiForWord(final String word) {
        if (word == null) return null;
        return EMOJI_MAP.get(word.trim().toLowerCase(Locale.ROOT));
    }

    private static final Map<String, String[]> BANGLISH_ALIASES = new HashMap<>();
    private static final Set<String> SHORTHAND_ABBREVIATIONS = new HashSet<>();

    static {
        // Bengali & Banglish to Emoji
        final String[][] emojiPairs = {
            {"love", "❤️"}, {"valobasha", "❤️"}, {"bhalobasha", "❤️"}, {"valobasi", "❤️"}, {"bhalobasi", "❤️"},
            {"ভালোবাসা", "❤️"}, {"ভালোবাসি", "❤️"}, {"প্রেম", "❤️"}, {"prem", "❤️"}, {"heart", "❤️"}, {"valobasa", "❤️"},
            {"hasi", "😊"}, {"smile", "😊"}, {"happy", "😊"}, {"khushi", "😊"}, {"kushi", "😊"}, {"হাসি", "😊"}, {"খুশি", "😊"},
            {"kanna", "😭"}, {"cry", "😭"}, {"sad", "😭"}, {"dukho", "😭"}, {"dukkho", "😭"}, {"কান্না", "😭"}, {"দুঃখ", "😭"},
            {"fire", "🔥"}, {"hot", "🔥"}, {"agun", "🔥"}, {"আগুন", "🔥"},
            {"cha", "☕"}, {"tea", "☕"}, {"coffee", "☕"}, {"চা", "☕"}, {"কফি", "☕"},
            {"taka", "💰"}, {"money", "💰"}, {"টাকা", "💰"}, {"poisa", "💰"}, {"পয়সা", "💰"},
            {"bd", "🇧🇩"}, {"bangladesh", "🇧🇩"}, {"বাংলাদেশ", "🇧🇩"},
            {"ok", "👍"}, {"thik", "👍"}, {"thikase", "👍"}, {"ঠিক", "👍"}, {"jos", "👍"}, {"like", "👍"},
            {"congrats", "🎉"}, {"party", "🎉"}, {"shuvechha", "🎉"}, {"suvecha", "🎉"}, {"অভিনন্দন", "🎉"}, {"শুভেচ্ছা", "🎉"},
            {"cool", "😎"}, {"smart", "😎"},
            {"flower", "🌸"}, {"ful", "🌸"}, {"ফুল", "🌸"}, {"golap", "🌹"}, {"rose", "🌹"},
            {"star", "⭐"}, {"tara", "⭐"}, {"তারা", "⭐"},
            {"bye", "👋"}, {"tata", "👋"}, {"bidai", "👋"}, {"বিদায়", "👋"}, {"বিদায়", "👋"},
            {"gift", "🎁"}, {"upohar", "🎁"}, {"উপহার", "🎁"},
            {"biral", "🐱"}, {"cat", "🐱"}, {"বিড়াল", "🐱"},
            {"kukur", "🐶"}, {"dog", "🐶"}, {"কুকুর", "🐶"},
            {"mach", "🐟"}, {"fish", "🐟"}, {"মাছ", "🐟"},
            {"food", "🍔"}, {"khabar", "🍔"}, {"খাবার", "🍔"},
            {"sweet", "🍬"}, {"misti", "🍬"}, {"মিষ্টি", "🍬"},
            {"car", "🚗"}, {"gari", "🚗"}, {"গাড়ি", "🚗"},
            {"namaz", "🤲"}, {"dua", "🤲"}, {"দোয়া", "🤲"}, {"দোয়া", "🤲"}
        };
        for (String[] pair : emojiPairs) {
            EMOJI_MAP.put(pair[0].toLowerCase(Locale.ROOT), pair[1]);
        }

        // Avro zero-shift conjuncts (যুক্তবর্ণ) & Visarga (বিসর্গ) words
        addAlias("dukkho", "দুঃখ", "দুঃখী");
        addAlias("dukho", "দুঃখ", "দুঃখী");
        addAlias("dukhi", "দুঃখী", "দুঃখ");
        addAlias("du:khi", "দুঃখী", "দুঃখ");
        addAlias("du:kho", "দুঃখ", "দুঃখী");
        addAlias("dukkhito", "দুঃখিত");
        addAlias("dukhito", "দুঃখিত");
        addAlias("prosno", "প্রশ্ন");
        addAlias("proshno", "প্রশ্ন");
        addAlias("proshnotor", "প্রশ্নোত্তর");
        addAlias("somossa", "সমস্যা");
        addAlias("shomossha", "সমস্যা");
        addAlias("somossha", "সমস্যা");
        addAlias("shonge", "সঙ্গে");
        addAlias("songe", "সঙ্গে");
        addAlias("shongo", "সঙ্গ");
        addAlias("songo", "সঙ্গ");
        addAlias("gyan", "জ্ঞান");
        addAlias("gyani", "জ্ঞানী");
        addAlias("koshto", "কষ্ট");
        addAlias("kosto", "কষ্ট");
        addAlias("koste", "কষ্টে");
        addAlias("koshte", "কষ্টে");
        addAlias("srishti", "সৃষ্টি");
        addAlias("sristi", "সৃষ্টি");
        addAlias("drishti", "দৃষ্টি");
        addAlias("dristi", "দৃষ্টি");
        addAlias("porikkha", "পরীক্ষা");
        addAlias("porikha", "পরীক্ষা");
        addAlias("porikkhar", "পরীক্ষার");
        addAlias("biswas", "বিশ্বাস");
        addAlias("bisshas", "বিশ্বাস");
        addAlias("biswash", "বিশ্বাস");
        addAlias("kkh", "ক্ষ");
        addAlias("ksha", "ক্ষ");
        addAlias("khoma", "ক্ষমা");
        addAlias("kkhoma", "ক্ষমা");
        addAlias("kkhoti", "ক্ষতি");
        addAlias("khoti", "ক্ষতি");
        addAlias("khuda", "ক্ষুধা");
        addAlias("kkhuda", "ক্ষুধা");
        addAlias("lokkhon", "লক্ষণ", "লক্ষ্য");
        addAlias("lokkho", "লক্ষ", "লক্ষ্য");
        addAlias("bikkhato", "বিখ্যাত");
        addAlias("bikhyato", "বিখ্যাত");
        addAlias("oviggota", "অভিজ্ঞতা");
        addAlias("biggapon", "বিজ্ঞাপন");
        addAlias("bigyan", "বিজ্ঞান");
        addAlias("bijnan", "বিজ্ঞান");
        addAlias("agya", "আজ্ঞা");
        addAlias("obiggo", "অভিজ্ঞ");
        addAlias("obiggyo", "অভিজ্ঞ");
        addAlias("bishes", "বিশেষ");
        addAlias("bishesh", "বিশেষ");
        addAlias("bishesvabe", "বিশেষভাবে");
        addAlias("obostha", "অবস্থা");
        addAlias("byabohar", "ব্যবহার");
        addAlias("babohar", "ব্যবহার");
        addAlias("byartho", "ব্যর্থ");
        addAlias("bartho", "ব্যর্থ");
        addAlias("byatha", "ব্যথা");
        addAlias("batha", "ব্যথা");
        addAlias("byasto", "ব্যস্ত");
        addAlias("basto", "ব্যস্ত");
        addAlias("shanto", "শান্ত");
        addAlias("santo", "শান্ত");
        addAlias("klanto", "ক্লান্ত");
        addAlias("mukto", "মুক্ত");
        addAlias("shokto", "শক্ত");
        addAlias("sokto", "শক্ত");
        addAlias("bhokto", "ভক্ত");
        addAlias("vokto", "ভক্ত");
        addAlias("rokto", "রক্ত");
        addAlias("rokte", "রক্তে");
        addAlias("ukti", "উক্তি");
        addAlias("yukti", "যুক্তি");
        addAlias("jukti", "যুক্তি");
        addAlias("sotto", "সত্য");
        addAlias("shotto", "সত্য");
        addAlias("mitthe", "মিথ্যা");
        addAlias("mittha", "মিথ্যা");
        addAlias("anondo", "আনন্দ");
        addAlias("utsah", "উৎসাহ");
        addAlias("utsob", "উৎসব");
        addAlias("bhabna", "ভাবনা");
        addAlias("chinta", "চিন্তা");

        // Conversational essentials & greetings
        addShorthand("kmn", "কেমন", "কমন");
        addShorthand("kmnacho", "কেমন আছো");
        addShorthand("kmnaso", "কেমন আছো");
        addShorthand("kemonacho", "কেমন আছো");
        addShorthand("valo", "ভালো", "ভাল");
        addShorthand("bhalo", "ভালো", "ভাল");
        addShorthand("valona", "ভালো না", "ভালোনা", "ভাল না");
        addShorthand("bhalona", "ভালো না", "ভালোনা");
        addShorthand("valoacho", "ভালো আছো");
        addShorthand("bhaloacho", "ভালো আছো");
        addShorthand("valoachi", "ভালো আছি");
        addShorthand("bhaloachi", "ভালো আছি");
        addShorthand("thikase", "ঠিক আছে");
        addShorthand("thikache", "ঠিক আছে");
        addShorthand("kisu", "কিছু");
        addShorthand("kichu", "কিছু");
        addShorthand("shob", "সব", "শব");
        addShorthand("sob", "সব");
        addShorthand("dhonn", "ধন্যবাদ", "ধন্য");
        addShorthand("dhonno", "ধন্যবাদ", "ধন্য");
        addShorthand("dhonnobad", "ধন্যবাদ");
        addShorthand("hobena", "হবে না", "হবেনা");
        addShorthand("parbona", "পারব না");
        addShorthand("janina", "জানি না");
        addShorthand("korbona", "করব না");
        addShorthand("pic", "ছবি");
        addShorthand("plz", "দয়া করে");
        addShorthand("please", "দয়া করে");
        addShorthand("thx", "ধন্যবাদ");
        addShorthand("tnx", "ধন্যবাদ");
        addShorthand("inshallah", "ইনশাআল্লাহ");
        addShorthand("inshaallah", "ইনশাআল্লাহ");
        addShorthand("alhamdulillah", "আলহামদুলিল্লাহ");
        addShorthand("mashallah", "মাশাআল্লাহ");
        addShorthand("subhanallah", "সুবহানাল্লাহ");
        addShorthand("bismillah", "বিসমিল্লাহ");
        addShorthand("khodahafez", "খোদা হাফেজ");
        addShorthand("shubhoratri", "শুভরাত্রি");
        addShorthand("shubhoshokal", "শুভসকাল");

        // Pronoun shorthands & full forms
        addShorthand("amdr", "আমাদের");
        addShorthand("apnr", "আপনার");
        addShorthand("tmdr", "তোমাদের");
        addAlias("ami", "আমি");
        addAlias("amra", "আমরা");
        addAlias("amader", "আমাদের");
        addAlias("amake", "আমাকে");
        addAlias("amar", "আমার");
        addAlias("tumi", "তুমি");
        addAlias("tomra", "তোমরা");
        addAlias("tomader", "তোমাদের");
        addAlias("tomake", "তোমাকে");
        addAlias("tomar", "তোমার");
        addAlias("apni", "আপনি");
        addAlias("apnara", "আপনারা");
        addAlias("apnar", "আপনার");
        addAlias("apnake", "আপনাকে");
        addAlias("she", "সে");
        addAlias("tini", "তিনি");
        addAlias("tara", "তারা");
        addAlias("tader", "তাদের");
        addAlias("take", "তাকে");
        addAlias("tar", "তার");
        addAlias("ora", "ওরা");
        addAlias("oder", "ওদের");
        addAlias("oke", "ওকে");
        addShorthand("vai", "ভাই");
        addShorthand("bhai", "ভাই");
        addShorthand("bhaiya", "ভাইয়া");
        addShorthand("vaiya", "ভাইয়া");
        addAlias("apu", "আপু");
        addAlias("bondu", "বন্ধু");
        addAlias("bondhu", "বন্ধু");
        addAlias("shobai", "সবাই");
        addAlias("sobai", "সবাই");
        addAlias("shobaike", "সবাইকে");
        addAlias("manush", "মানুষ");
        addAlias("lok", "লোক");

        // Questions & conjunctions
        addAlias("ki", "কি", "কী");
        addAlias("kire", "কিরে");
        addAlias("keno", "কেন", "কেনো");
        addAlias("karon", "কারণ");
        addAlias("karom", "কারণ");
        addShorthand("kothay", "কোথায়");
        addAlias("kobe", "কবে");
        addAlias("kokhon", "কখন");
        addShorthand("kivabe", "কিভাবে");
        addShorthand("kemne", "কেমনে");
        addAlias("kar", "কার");
        addAlias("kake", "কাকে");
        addAlias("kiser", "কিসের");
        addAlias("kotodin", "কতদিন");
        addAlias("koto", "কত");
        addAlias("koita", "কয়টা");
        addAlias("ekhon", "এখন");
        addAlias("akhon", "এখন");
        addAlias("tokhon", "তখন");
        addAlias("jokhon", "যখন");
        addAlias("kokhono", "কখনো");
        addAlias("shobshomoy", "সবসময়");
        addAlias("sobshomoy", "সবসময়");
        addAlias("shomoy", "সময়");
        addAlias("somoy", "সময়");
        addAlias("protidin", "প্রতিদিন");
        addAlias("age", "আগে");
        addAlias("pore", "পরে");
        addAlias("por", "পর", "পরে");
        addAlias("kintu", "কিন্তু");
        addAlias("tahole", "তাহলে");
        addAlias("tobe", "তবে");
        addAlias("jodi", "যদি");
        addAlias("tai", "তাই");
        addAlias("ashe", "আসে");
        addShorthand("ashole", "আসলে");
        addShorthand("asole", "আসলে");

        // Quantifiers & affirmations
        addAlias("shobkisu", "সবকিছু");
        addAlias("shobkichu", "সবকিছু");
        addAlias("sobkisu", "সবকিছু");
        addAlias("kisui", "কিছুই");
        addAlias("kichui", "কিছুই");
        addAlias("onek", "অনেক");
        addAlias("ektu", "একটু");
        addAlias("beshi", "বেশি");
        addAlias("kom", "কম");
        addAlias("khub", "খুব");
        addAlias("pura", "পুরা");
        addAlias("shobcheye", "সবচেয়ে");
        addAlias("ha", "হ্যাঁ");
        addAlias("haa", "হ্যাঁ");
        addAlias("na", "না");
        addAlias("naa", "না");
        addAlias("nai", "নাই");
        addAlias("nei", "নেই");
        addAlias("hobe", "হবে");
        addAlias("ase", "আছে");
        addAlias("ache", "আছে");
        addAlias("thakbe", "থাকবে");
        addAlias("hoise", "হয়েছে", "হইছে");
        addAlias("hoye", "হয়ে");
        addAlias("hoyese", "হয়েছে");
        addAlias("geche", "গেছে");
        addAlias("gese", "গেছে");
        addAlias("gelo", "গেল", "গেলো");

        // Verbs & conjugations
        addAlias("kor", "কর");
        addAlias("kora", "করা");
        addAlias("kori", "করি");
        addAlias("kore", "করে");
        addAlias("koro", "করো");
        addAlias("korbo", "করব", "করবো");
        addAlias("korlam", "করলাম");
        addAlias("korchi", "করছি");
        addAlias("korso", "করছ", "করছো");
        addAlias("korchen", "করছেন");
        addAlias("korte", "করতে");
        addAlias("kortesi", "করছি", "করতেছি");
        addAlias("dekh", "দেখ");
        addAlias("dekha", "দেখা");
        addAlias("dekhi", "দেখি");
        addAlias("dekhe", "দেখে");
        addAlias("dekho", "দেখো");
        addAlias("dekhbo", "দেখব", "দেখবো");
        addAlias("dekhlam", "দেখলাম");
        addAlias("dekhchi", "দেখছি");
        addAlias("dekhso", "দেখছ", "দেখছো");
        addAlias("dekhte", "দেখতে");
        addAlias("bol", "বল");
        addAlias("bola", "বলা");
        addAlias("boli", "বলি");
        addAlias("bole", "বলে");
        addAlias("bolo", "বলো");
        addAlias("bolbo", "বলব", "বলবো");
        addAlias("bollam", "বললাম");
        addAlias("bolchi", "বলছি");
        addAlias("bolte", "বলতে");
        addAlias("boltesi", "বলছি", "বলতেছি");
        addAlias("shun", "শুন");
        addAlias("shuna", "শোনা");
        addAlias("shuni", "শুনি");
        addAlias("shunbo", "শুনব", "শুনবো");
        addAlias("shunlam", "শুনলাম");
        addAlias("shunchi", "শুনছি");
        addAlias("shunte", "শুনতে");
        addAlias("ja", "যা");
        addAlias("jawa", "যাওয়া");
        addAlias("jai", "যাই");
        addAlias("jay", "যায়");
        addAlias("jabo", "যাব", "যাবো");
        addAlias("gechi", "গেছি");
        addAlias("jete", "যেতে");
        addAlias("jaitesi", "যাচ্ছি", "যাইতেছি");
        addAlias("ash", "আস");
        addAlias("asha", "আসা");
        addAlias("ashi", "আসি");
        addAlias("asho", "আসো");
        addAlias("ashbo", "আসব", "আসবো");
        addAlias("ashlam", "আসলাম");
        addAlias("ashechi", "এসেছি");
        addAlias("ashte", "আসতে");
        addAlias("astesi", "আসছি", "আসতেছি");
        addAlias("jan", "জান");
        addAlias("jana", "জানা");
        addAlias("jani", "জানি");
        addAlias("janbo", "জানব", "জানবো");
        addAlias("jantam", "জানতাম");
        addAlias("khao", "খাও");
        addAlias("khai", "খাই");
        addAlias("khabo", "খাব", "খাবো");
        addAlias("khawa", "খাওয়া");
        addAlias("khawa-dawa", "খাওয়া-দাওয়া");
        addAlias("ghum", "ঘুম");
        addAlias("ghumao", "ঘুমাও");
        addAlias("ghumai", "ঘুমাই");
        addAlias("por", "পড়", "পড়");
        addAlias("poro", "পড়ো", "পড়ো");
        addAlias("pori", "পড়ি", "পড়ি");
        addAlias("porbo", "পড়ব", "পড়বো");

        // Places & common nouns
        addAlias("sundor", "সুন্দর");
        addAlias("shundor", "সুন্দর");
        addAlias("bhalobasha", "ভালোবাসা", "ভালবাসা");
        addAlias("valobasha", "ভালোবাসা");
        addAlias("bhalobashi", "ভালোবাসি");
        addAlias("valobashi", "ভালোবাসি");
        addAlias("mon", "মন");
        addAlias("mone", "মনে");
        addAlias("bari", "বাড়ি", "বারি");
        addAlias("basha", "বাসা");
        addAlias("ghor", "ঘর");
        addAlias("office", "অফিস");
        addAlias("kaj", "কাজ");
        addAlias("khabar", "খাবার");
        addAlias("pani", "পানি");
        addAlias("cha", "চা");
        addAlias("khobor", "খবর");
        addAlias("bangla", "বাংলা", "বাংলাদেশ");
        addAlias("bangladesh", "বাংলাদেশ", "বাংলা");
        addAlias("dhaka", "ঢাকা");
        addAlias("chittagong", "চট্টগ্রাম");
        addAlias("sylhet", "সিলেট");
        addAlias("rajshahi", "রাজশাহী");
        addAlias("khulna", "খুলনা");
        addAlias("barisal", "বরিশাল");
        addAlias("rangpur", "রংপুর");
        addAlias("mymensingh", "ময়মনসিংহ");
        addAlias("kolkata", "কলকাতা");
        addAlias("desh", "দেশ");
        addAlias("bidesh", "বিদেশ");
        addAlias("rasta", "রাস্তা");
        addAlias("gari", "গাড়ি");
        addAlias("phone", "ফোন");
        addAlias("chobi", "ছবি");
        addAlias("gan", "গান");
        addAlias("gaan", "গান");
        addAlias("boi", "বই");
        addAlias("notun", "নতুন");
        addAlias("puraton", "পুরাতন");
        addAlias("purono", "পুরনো");
        addAlias("boro", "বড়", "বড়");
        addAlias("choto", "ছোট");
        addAlias("shathe", "সাথে");
        addAlias("sathe", "সাথে");
        addAlias("pashe", "পাশে");
        addAlias("samne", "সামনে");
        addAlias("pichone", "পিছনে");
        addAlias("majhe", "মাঝে");
        addAlias("moddhe", "মধ্যে");
        addAlias("bhetore", "ভেতরে");
        addAlias("baire", "বাইরে");
        addAlias("kalke", "কালকে");
        addAlias("agamikal", "আগামীকাল");
        addAlias("gotokal", "গতকাল");
        addAlias("ajke", "আজকে");
        addAlias("aaj", "আজ");
        addAlias("aj", "আজ");
        addAlias("din", "দিন");
        addAlias("soptaho", "সপ্তাহ");
        addAlias("mash", "মাস");
        addAlias("bochor", "বছর");
        addAlias("taka", "টাকা");
        addAlias("poisha", "পয়সা");
        addAlias("shikkha", "শিক্ষা");
        addAlias("porikkha", "পরীক্ষা");
        addShorthand("ingreji", "ইংরেজি");
        addShorthand("ingrezi", "ইংরেজি");
        addShorthand("english", "ইংরেজি");

        // Common English loanwords used in everyday Bangladeshi conversation (Banglish transliteration)
        addShorthand("account", "একাউন্ট", "অ্যাকাউন্ট");
        addShorthand("use", "ইউজ");
        addShorthand("user", "ইউজার");
        addShorthand("post", "পোস্ট");
        addShorthand("update", "আপডেট");
        addShorthand("share", "শেয়ার", "শেয়ার");
        addShorthand("like", "লাইক");
        addShorthand("comment", "কমেন্ট");
        addShorthand("message", "মেসেজ");
        addShorthand("msg", "মেসেজ");
        addShorthand("call", "কল");
        addShorthand("check", "চেক");
        addShorthand("clear", "ক্লিয়ার", "ক্লিয়ার");
        addShorthand("link", "লিংক");
        addShorthand("video", "ভিডিও");
        addShorthand("photo", "ফটো", "ছবি");
        addShorthand("group", "গ্রুপ");
        addShorthand("page", "পেজ");
        addShorthand("online", "অনলাইন");
        addShorthand("offline", "অফলাইন");
        addShorthand("problem", "প্রবলেম", "সমস্যা");
        addShorthand("help", "হেল্প", "সাহায্য");
        addShorthand("password", "পাসওয়ার্ড", "পাসওয়ার্ড");
        addShorthand("pass", "পাস");
        addShorthand("code", "কোড");
        addShorthand("id", "আইডি");
        addShorthand("app", "অ্যাপ");
        addShorthand("apps", "অ্যাপস");
        addShorthand("test", "টেস্ট");
        addShorthand("work", "ওয়ার্ক", "কাজ");
        addShorthand("job", "জব", "চাকরি");
        addShorthand("setting", "সেটিং");
        addShorthand("settings", "সেটিংস");
        addShorthand("payment", "পেমেন্ট");
        addShorthand("pay", "পে");
        addShorthand("order", "অর্ডার");
        addShorthand("delivery", "ডেলিভারি");
        addShorthand("cancel", "ক্যান্সেল", "বাতিল");
        addShorthand("confirm", "কনফার্ম");
        addShorthand("active", "অ্যাক্টিভ");
        addShorthand("pending", "পেন্ডিং");
        addShorthand("done", "ডান");
        addShorthand("ok", "ওকে", "ঠিক আছে");
        addShorthand("okay", "ওকে", "ঠিক আছে");
        addShorthand("sorry", "সরি", "দুঃখিত");
        addShorthand("welcome", "ওয়েলকাম", "স্বাগতম");
        addShorthand("thanks", "থ্যাংকস", "ধন্যবাদ");
        addShorthand("location", "লোকেশন");
        addShorthand("address", "অ্যাড্রেস", "ঠিকানা");
        addShorthand("number", "নম্বর", "নাম্বার");
        addShorthand("balance", "ব্যালেন্স");
        addShorthand("recharge", "রিচার্জ");
        addShorthand("offer", "অফার");
        addShorthand("discount", "ডিসকাউন্ট");
        addShorthand("bill", "বিল");
        addShorthand("cash", "ক্যাশ");
        addShorthand("money", "মানি", "টাকা");
        addShorthand("bank", "ব্যাংক");
        addShorthand("office", "অফিস");
        addShorthand("school", "স্কুল");
        addShorthand("college", "কলেজ");
        addShorthand("varsity", "ভার্সিটি");
        addShorthand("class", "ক্লাস");
        addShorthand("exam", "এক্সাম", "পরীক্ষা");
        addShorthand("result", "রেজাল্ট", "ফলাফল");
        addShorthand("time", "টাইম", "সময়");
        addShorthand("date", "ডেট", "তারিখ");
        addShorthand("meeting", "মিটিং");
        addShorthand("bus", "বাস");
        addShorthand("train", "ট্রেন");
        addShorthand("ticket", "টিকিট");
        addShorthand("hotel", "হোটেল");
        addShorthand("doctor", "ডাক্তার");
        addShorthand("hospital", "হাসপাতাল");
        addShorthand("medicine", "মেডিসিন", "ওষুধ");
        addShorthand("report", "রিপোর্ট");
        addShorthand("mobile", "মোবাইল");
        addShorthand("phone", "ফোন");
        addShorthand("sim", "সিম");
        addShorthand("network", "নেটওয়ার্ক");
        addShorthand("internet", "ইন্টারনেট");
        addShorthand("wifi", "ওয়াইফাই");
        addShorthand("data", "ডেটা");
        addShorthand("screen", "স্ক্রিন");
        addShorthand("display", "ডিসপ্লে");
        addShorthand("battery", "ব্যাটারি");
        addShorthand("charger", "চার্জার");
        addShorthand("charge", "চার্জ");
        addShorthand("sound", "সাউন্ড");
        addShorthand("speaker", "স্পিকার");
        addShorthand("camera", "ক্যামেরা");
        addShorthand("download", "ডাউনলোড");
        addShorthand("install", "ইনস্টল");
        addShorthand("delete", "ডিলিট");
        addShorthand("remove", "রিমুভ");
        addShorthand("copy", "কপি");
        addShorthand("paste", "পেস্ট");
        addShorthand("save", "সেভ");
        addShorthand("search", "সার্চ");
        addShorthand("login", "লগইন");
        addShorthand("logout", "লগআউট");
        addShorthand("signup", "সাইনআপ");
        addShorthand("sign", "সাইন");
        addShorthand("file", "ফাইল");
        addShorthand("folder", "ফোল্ডার");
        addShorthand("system", "সিস্টেম");
        addShorthand("service", "সার্ভিস");
        addShorthand("smart", "স্মার্ট");
    }

    private static void addShorthand(String key, String... values) {
        final String lower = key.toLowerCase(Locale.ROOT);
        BANGLISH_ALIASES.put(lower, values);
        SHORTHAND_ABBREVIATIONS.add(lower);
    }

    private static void addAlias(String key, String... values) {
        BANGLISH_ALIASES.put(key.toLowerCase(Locale.ROOT), values);
    }

    private static final Map<String, String> ENGLISH_CANONICAL_MAP = new HashMap<>();

    private static void addEnglishCanonical(String variant, String canonical) {
        ENGLISH_CANONICAL_MAP.put(variant.toLowerCase(Locale.ROOT), canonical.toLowerCase(Locale.ROOT));
    }

    static {
        // English canonical typo and abbreviation mappings for bilingual Avro suggestions
        addEnglishCanonical("accout", "account");
        addEnglishCanonical("accont", "account");
        addEnglishCanonical("acnt", "account");
        addEnglishCanonical("acct", "account");
        addEnglishCanonical("facebok", "facebook");
        addEnglishCanonical("fb", "facebook");
        addEnglishCanonical("bksh", "bkash");
        addEnglishCanonical("bkas", "bkash");
        addEnglishCanonical("mesage", "message");
        addEnglishCanonical("msg", "message");
        addEnglishCanonical("pasword", "password");
        addEnglishCanonical("pass", "password");
        addEnglishCanonical("pwd", "password");
        addEnglishCanonical("pctr", "picture");
        addEnglishCanonical("pic", "picture");
        addEnglishCanonical("pics", "pictures");
        addEnglishCanonical("plz", "please");
        addEnglishCanonical("pls", "please");
        addEnglishCanonical("thx", "thanks");
        addEnglishCanonical("tnx", "thanks");
        addEnglishCanonical("tq", "thanks");
        addEnglishCanonical("welcom", "welcome");
        addEnglishCanonical("wlc", "welcome");
        addEnglishCanonical("congo", "congratulations");
        addEnglishCanonical("congrts", "congratulations");
        addEnglishCanonical("sry", "sorry");
        addEnglishCanonical("prblm", "problem");
        addEnglishCanonical("prob", "problem");
        addEnglishCanonical("ofc", "office");
        addEnglishCanonical("sch", "school");
        addEnglishCanonical("scl", "school");
        addEnglishCanonical("clg", "college");
        addEnglishCanonical("univ", "university");
        addEnglishCanonical("varsity", "university");
        addEnglishCanonical("nmbr", "number");
        addEnglishCanonical("num", "number");
        addEnglishCanonical("phn", "phone");
        addEnglishCanonical("fone", "phone");
        addEnglishCanonical("mob", "mobile");
        addEnglishCanonical("mbl", "mobile");
        addEnglishCanonical("bal", "balance");
        addEnglishCanonical("rechrg", "recharge");
        addEnglishCanonical("loc", "location");
        addEnglishCanonical("lctn", "location");
        addEnglishCanonical("doc", "document");
        addEnglishCanonical("addr", "address");
        addEnglishCanonical("addrs", "address");
        addEnglishCanonical("cmt", "comment");
        addEnglishCanonical("abt", "about");
        addEnglishCanonical("bcoz", "because");
        addEnglishCanonical("coz", "because");
        addEnglishCanonical("bcz", "because");
        addEnglishCanonical("recive", "receive");
        addEnglishCanonical("recieve", "receive");
        addEnglishCanonical("definately", "definitely");
        addEnglishCanonical("tommorow", "tomorrow");
        addEnglishCanonical("tmrw", "tomorrow");
        addEnglishCanonical("yday", "yesterday");
        addEnglishCanonical("bday", "birthday");
        addEnglishCanonical("brh", "brother");
        addEnglishCanonical("bro", "brother");
        addEnglishCanonical("sis", "sister");
        addEnglishCanonical("frnd", "friend");
        addEnglishCanonical("chk", "check");
        addEnglishCanonical("gud", "good");
        addEnglishCanonical("shud", "should");
        addEnglishCanonical("wud", "would");
        addEnglishCanonical("cud", "could");
        addEnglishCanonical("lyk", "like");
        addEnglishCanonical("dwnld", "download");
        addEnglishCanonical("updt", "update");
        addEnglishCanonical("instal", "install");
        addEnglishCanonical("instl", "install");
        addEnglishCanonical("cncl", "cancel");
        addEnglishCanonical("confrm", "confirm");
        addEnglishCanonical("seting", "settings");
        addEnglishCanonical("stng", "settings");
        addEnglishCanonical("pymt", "payment");
        addEnglishCanonical("pymnt", "payment");
        addEnglishCanonical("btr", "better");
        addEnglishCanonical("snd", "send");
        addEnglishCanonical("hlp", "help");
        addEnglishCanonical("wrk", "work");
        addEnglishCanonical("grp", "group");
        addEnglishCanonical("pg", "page");
        addEnglishCanonical("chnl", "channel");
        addEnglishCanonical("admn", "admin");
        addEnglishCanonical("tm", "time");
        addEnglishCanonical("dt", "date");
        addEnglishCanonical("intrnt", "internet");
        addEnglishCanonical("ntwrk", "network");
        addEnglishCanonical("scrn", "screen");
        addEnglishCanonical("btry", "battery");
        addEnglishCanonical("chrg", "charge");
        addEnglishCanonical("clr", "clear");
        addEnglishCanonical("vid", "video");
        addEnglishCanonical("pht", "photo");
        addEnglishCanonical("cmr", "camera");
        addEnglishCanonical("spkr", "speaker");
        addEnglishCanonical("sys", "system");
        addEnglishCanonical("srvc", "service");
        addEnglishCanonical("ap", "app");
        addEnglishCanonical("apks", "apk");
    }

    private BanglaSuggestionEngine() {}

    /**
     * Borno's case normalization:
     * In Bangla phonetic input, uppercase is only meaningful for distinct phonemes:
     * O, I, U, D, G, J, N, R, S, T, Y, Z, H, X.
     * Accidental capitalization from Android auto-caps (e.g. K in Kemon) is normalized to lowercase.
     */
    public static String normalizeCase(final String in) {
        if (in == null || in.isEmpty()) return in;
        final StringBuilder sb = new StringBuilder(in.length());
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (c == 'a' || c == 'A' || c == 'o' || c == 'O' ||
                c == 'i' || c == 'I' || c == 'u' || c == 'U' ||
                c == 'd' || c == 'D' || c == 'g' || c == 'G' ||
                c == 'j' || c == 'J' || c == 'n' || c == 'N' ||
                c == 'r' || c == 'R' || c == 's' || c == 'S' ||
                c == 't' || c == 'T' || c == 'y' || c == 'Y' ||
                c == 'z' || c == 'Z' || c == 'h' || c == 'H' ||
                c == 'x' || c == 'X') {
                sb.append(c);
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    /**
     * Borno's RemoveConsecutiveKarSigns:
     * When two kar signs occur consecutively (e.g. invalid typing like কা + া),
     * replaces the second kar sign with its independent vowel to prevent broken dotted circles.
     */
    public static String removeConsecutiveKarSigns(final String str) {
        if (str == null || str.length() < 2) return str;
        final StringBuilder sb = new StringBuilder(str);
        for (int i = 1; i < sb.length(); i++) {
            if (isKarSign(sb.charAt(i - 1)) && isKarSign(sb.charAt(i))) {
                sb.setCharAt(i, getCorrespondingVowel(sb.charAt(i)));
            }
        }
        return sb.toString();
    }

    private static boolean isKarSign(char c) {
        return (c >= 0x09BE && c <= 0x09C4) || (c >= 0x09C7 && c <= 0x09C8) || (c >= 0x09CB && c <= 0x09CC) || (c == 0x09D7);
    }

    private static char getCorrespondingVowel(char sign) {
        switch (sign) {
            case 0x09BE: return 0x0986; // া -> আ
            case 0x09BF: return 0x0987; // ি -> ই
            case 0x09C0: return 0x0988; // ী -> ঈ
            case 0x09C1: return 0x0989; // ু -> উ
            case 0x09C2: return 0x098A; // ূ -> ঊ
            case 0x09C3: return 0x098B; // ৃ -> ঋ
            case 0x09C7: return 0x098F; // ে -> এ
            case 0x09C8: return 0x0990; // ৈ -> ঐ
            case 0x09CB: return 0x0993; // ো -> ও
            case 0x09CC: return 0x0994; // ৌ -> ঔ
            case 0x09D7: return 0x0994;
            default: return sign;
        }
    }

    /**
     * Backward-compatible candidate generation without DictionaryFacilitator.
     */
    public static List<String> getCandidates(final String raw, final RidmikParser parser) {
        return getCandidates(raw, parser, null);
    }

    /**
     * Generates rich candidates using Banglish alias resolution, phonetic rules,
     * and authentic dictionary validation.
     */
    public static List<String> getCandidates(final String raw, final RidmikParser parser,
                                             final DictionaryFacilitator dictFacilitator) {
        final LinkedHashSet<String> set = new LinkedHashSet<>();
        if (raw == null || raw.isEmpty()) {
            return new ArrayList<>();
        }

        final String lower = raw.toLowerCase(Locale.ROOT);
        final String canonical = ENGLISH_CANONICAL_MAP.get(lower);
        final boolean isShorthand = SHORTHAND_ABBREVIATIONS.contains(lower)
                || (canonical != null && SHORTHAND_ABBREVIATIONS.contains(canonical));

        // 1. Shorthand conversational abbreviations take primary priority
        if (isShorthand) {
            final String[] aliases = BANGLISH_ALIASES.get(lower);
            if (aliases != null) {
                for (String alias : aliases) {
                    if (alias != null && !alias.isEmpty()) {
                        set.add(removeConsecutiveKarSigns(alias));
                    }
                }
            }
            if (canonical != null) {
                final String[] canonicalAliases = BANGLISH_ALIASES.get(canonical);
                if (canonicalAliases != null) {
                    for (String alias : canonicalAliases) {
                        if (alias != null && !alias.isEmpty()) {
                            set.add(removeConsecutiveKarSigns(alias));
                        }
                    }
                }
            }
        }

        // 2. Primary transliteration with Borno case normalization
        final String normalizedRaw = normalizeCase(raw);
        final String primary = removeConsecutiveKarSigns(parser.toBangla(normalizedRaw));
        if (primary != null && !primary.isEmpty()) {
            set.add(primary);
        }
        if (!normalizedRaw.equals(raw)) {
            final String rawPrimary = removeConsecutiveKarSigns(parser.toBangla(raw));
            if (rawPrimary != null && !rawPrimary.isEmpty()) {
                set.add(rawPrimary);
            }
        }

        // 3. General phonetic aliases right after primary
        if (!isShorthand) {
            final String[] aliases = BANGLISH_ALIASES.get(lower);
            if (aliases != null) {
                for (String alias : aliases) {
                    if (alias != null && !alias.isEmpty()) {
                        set.add(removeConsecutiveKarSigns(alias));
                    }
                }
            }
            if (canonical != null) {
                final String[] canonicalAliases = BANGLISH_ALIASES.get(canonical);
                if (canonicalAliases != null) {
                    for (String alias : canonicalAliases) {
                        if (alias != null && !alias.isEmpty()) {
                            set.add(removeConsecutiveKarSigns(alias));
                        }
                    }
                }
            }
        }

        // 3. Trailing 'o' / 'O' variations (verbs & common adjectives, e.g. korbo -> করব, করবো)
        if (raw.endsWith("o")) {
            final String altO = removeConsecutiveKarSigns(parser.toBangla(raw.substring(0, raw.length() - 1) + "O"));
            if (altO != null && !altO.isEmpty()) set.add(altO);
            if (raw.length() > 2) {
                final String stem = removeConsecutiveKarSigns(parser.toBangla(raw.substring(0, raw.length() - 1)));
                if (stem != null && !stem.isEmpty()) set.add(stem);
            }
        } else if (raw.endsWith("O")) {
            final String alto = removeConsecutiveKarSigns(parser.toBangla(raw.substring(0, raw.length() - 1) + "o"));
            if (alto != null && !alto.isEmpty()) set.add(alto);
        } else if (raw.length() > 1 && !isVowel(raw.charAt(raw.length() - 1))) {
            final String altO = removeConsecutiveKarSigns(parser.toBangla(raw + "O"));
            if (altO != null && !altO.isEmpty()) set.add(altO);
            final String alto = removeConsecutiveKarSigns(parser.toBangla(raw + "o"));
            if (alto != null && !alto.isEmpty()) set.add(alto);
        }

        // 4. 'v' vs 'bh' (Bangladeshi typing habit: valo -> bhalo -> ভালো)
        if (raw.contains("v")) {
            final String altBh = removeConsecutiveKarSigns(parser.toBangla(raw.replace("v", "bh")));
            if (altBh != null && !altBh.isEmpty()) set.add(altBh);
            final String altBhO = removeConsecutiveKarSigns(parser.toBangla(raw.replace("v", "bh").replace('o', 'O')));
            if (altBhO != null && !altBhO.isEmpty()) set.add(altBhO);
        } else if (raw.contains("bh")) {
            final String altV = removeConsecutiveKarSigns(parser.toBangla(raw.replace("bh", "v")));
            if (altV != null && !altV.isEmpty()) set.add(altV);
        }

        // 5. 's' vs 'sh' (দাঁত-স vs তালব্য-শ)
        if (raw.contains("sh")) {
            final String sWord = raw.replace("sh", "s");
            final String alt = removeConsecutiveKarSigns(parser.toBangla(sWord));
            if (alt != null && !alt.isEmpty()) set.add(alt);
            if (sWord.contains("o")) {
                final String altSO = removeConsecutiveKarSigns(parser.toBangla(sWord.replace('o', 'O')));
                if (altSO != null && !altSO.isEmpty()) set.add(altSO);
            }
        } else if (raw.contains("s")) {
            final String shWord = raw.replace("s", "sh");
            final String alt = removeConsecutiveKarSigns(parser.toBangla(shWord));
            if (alt != null && !alt.isEmpty()) set.add(alt);
            if (shWord.contains("o")) {
                final String altShO = removeConsecutiveKarSigns(parser.toBangla(shWord.replace('o', 'O')));
                if (altShO != null && !altShO.isEmpty()) set.add(altShO);
            }
        }

        // 6. Other consonant / vowel distinctions
        // 'r' vs 'R' (র vs ড়)
        if (raw.contains("r")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('r', 'R')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        } else if (raw.contains("R")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('R', 'r')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        }

        // 't' vs 'T' (ত vs ট)
        if (raw.contains("t")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('t', 'T')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        } else if (raw.contains("T")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('T', 't')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        }

        // 'd' vs 'D' (দ vs ড)
        if (raw.contains("d")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('d', 'D')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        } else if (raw.contains("D")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('D', 'd')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        }

        // 'z' vs 'j' (জ vs য)
        if (raw.contains("z")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('z', 'j')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        } else if (raw.contains("j")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('j', 'z')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        }

        // 'n' vs 'N' (ন vs ণ)
        if (raw.contains("n")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('n', 'N')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        } else if (raw.contains("N")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('N', 'n')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        }

        // 'i' vs 'I' (ি vs ী)
        if (raw.contains("i")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('i', 'I')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        } else if (raw.contains("I")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('I', 'i')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        }

        // 'u' vs 'U' (ু vs ূ)
        if (raw.contains("u")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('u', 'U')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        } else if (raw.contains("U")) {
            final String alt = removeConsecutiveKarSigns(parser.toBangla(raw.replace('U', 'u')));
            if (alt != null && !alt.isEmpty()) set.add(alt);
        }

        // 7. Enrich with Zero-GC flat CSR Trie phonetic completions
        try {
            final List<String> trieMatches = BanglaPhoneticMatcher.Companion.getINSTANCE().findMatches(primary);
            if (trieMatches != null) {
                for (String tm : trieMatches) {
                    if (tm != null && !tm.isEmpty()) {
                        set.add(removeConsecutiveKarSigns(tm));
                    }
                }
            }
        } catch (final Throwable ignored) {}

        // 8. Rank candidates with DictionaryFacilitator (main_bn.dict)
        final ArrayList<String> result = new ArrayList<>();
        if (dictFacilitator != null) {
            // First pass: add words that are verified authentic words in main_bn.dict
            for (String cand : set) {
                if (dictFacilitator.isValidSpellingWord(cand)) {
                    result.add(cand);
                }
            }
            // Second pass: add remaining phonetic candidates
            for (String cand : set) {
                if (!result.contains(cand)) {
                    result.add(cand);
                }
            }
        } else {
            result.addAll(set);
        }

        // Keep maximum 5 candidates
        while (result.size() > 5) {
            result.remove(result.size() - 1);
        }

        return result;
    }

    private static boolean isVowel(char c) {
        c = Character.toLowerCase(c);
        return c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u';
    }

    /**
     * Retrieves bilingual English candidates for Avro phonetic typing.
     * Generates canonical spell-corrected word (e.g. accout -> account) and exact typed text.
     */
    public static List<String> getEnglishCandidates(final String raw) {
        final List<String> result = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) {
            return result;
        }
        final String trimmed = raw.trim();
        final String lower = trimmed.toLowerCase(Locale.ROOT);

        final String canonical = ENGLISH_CANONICAL_MAP.get(lower);
        if (canonical != null && !canonical.equalsIgnoreCase(trimmed)) {
            result.add(applyCasing(trimmed, canonical));
        }

        result.add(trimmed);

        return result;
    }

    /**
     * Applies the casing pattern of template string to target string.
     */
    public static String applyCasing(final String template, final String target) {
        if (template == null || target == null || template.isEmpty() || target.isEmpty()) {
            return target;
        }
        boolean allUpper = true;
        for (int i = 0; i < template.length(); i++) {
            if (Character.isLowerCase(template.charAt(i))) {
                allUpper = false;
                break;
            }
        }
        if (allUpper && template.length() > 1) {
            return target.toUpperCase(Locale.ROOT);
        }
        if (Character.isUpperCase(template.charAt(0))) {
            if (target.length() == 1) {
                return target.toUpperCase(Locale.ROOT);
            }
            return Character.toUpperCase(target.charAt(0)) + target.substring(1).toLowerCase(Locale.ROOT);
        }
        return target.toLowerCase(Locale.ROOT);
    }
}
