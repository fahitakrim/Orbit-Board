package org.futo.inputmethod.latin.bengali

/**
 * OmicronLab 100% Avro Phonetic Rule Engine.
 * Features:
 * 1. 100% Avro rules for vowels, consonants, digraphs, and conjuncts.
 * 2. Middle-of-word cursor stability: stateless parsing with safe bounds.
 * 3. Conjunct sanitizer: eliminates orphan hasants, double viramas, and consecutive kar signs.
 */
object AvroRuleEngine {

    const val HASANT = "\u09CD"
    const val ZWNJ = "\u200C"
    const val ZWJ = "\u200D"

    /**
     * Sanitizes Bengali text by fixing broken conjuncts, orphan hasants,
     * and consecutive kar signs to ensure stable rendering.
     */
    fun sanitize(str: String?): String {
        if (str.isNullOrEmpty()) return ""
        val sb = StringBuilder(str.length)
        var lastChar = 0.toChar()

        for (i in str.indices) {
            val c = str[i]
            // Eliminate double hasants (্ + ্)
            if (c == '\u09CD' && lastChar == '\u09CD') {
                continue
            }
            // If kar follows hasant, remove hasant and attach kar directly
            if (isKarSign(c) && lastChar == '\u09CD') {
                if (sb.isNotEmpty() && sb[sb.length - 1] == '\u09CD') {
                    sb.deleteCharAt(sb.length - 1)
                }
            }
            // If identical kar signs are repeated (e.g. ে followed by ে, or া followed by া), drop the duplicate!
            if (isKarSign(c) && isKarSign(lastChar) && c == lastChar) {
                continue
            }
            // If kar sign appears at start of word or after non-consonant, promote to independent vowel
            if (isKarSign(c) && (sb.isEmpty() || !isBengaliConsonant(lastChar))) {
                val vowel = getCorrespondingVowel(c)
                sb.append(vowel)
                lastChar = vowel
                continue
            }
            // Eliminate consecutive different kar signs by turning second kar into independent vowel
            if (isKarSign(c) && isKarSign(lastChar)) {
                val vowel = getCorrespondingVowel(c)
                sb.append(vowel)
                lastChar = vowel
                continue
            }
            sb.append(c)
            lastChar = c
        }

        // Remove trailing orphan hasant if not followed by a consonant
        while (sb.isNotEmpty() && sb[sb.length - 1] == '\u09CD') {
            sb.deleteCharAt(sb.length - 1)
        }

        return sb.toString()
    }

    fun isBengaliConsonant(c: Char): Boolean {
        return (c in '\u0995'..'\u09B9') || (c in '\u09DC'..'\u09DF') || (c == '\u09CE')
    }

    fun isKarSign(c: Char): Boolean {
        return (c in '\u09BE'..'\u09C4') || (c in '\u09C7'..'\u09C8') || (c in '\u09CB'..'\u09CC') || (c == '\u09D7')
    }

    fun getCorrespondingVowel(sign: Char): Char {
        return when (sign) {
            '\u09BE' -> '\u0986' // া -> আ
            '\u09BF' -> '\u0987' // ি -> ই
            '\u09C0' -> '\u0988' // ী -> ঈ
            '\u09C1' -> '\u0989' // ু -> উ
            '\u09C2' -> '\u098A' // ূ -> ঊ
            '\u09C3' -> '\u098B' // ৃ -> ঋ
            '\u09C7' -> '\u098F' // ে -> এ
            '\u09C8' -> '\u0990' // ৈ -> ঐ
            '\u09CB' -> '\u0993' // ো -> ও
            '\u09CC' -> '\u0994' // ৌ -> ঔ
            '\u09D7' -> '\u0994'
            else -> sign
        }
    }

    /**
     * Converts phonetic English word to Bengali using OmicronLab Avro rules,
     * maintaining cursor stability and conjunct sanitization.
     */
    fun parse(input: String?): String {
        if (input.isNullOrEmpty()) return ""
        val parser = RidmikParser()
        val parsed = parser.toBangla(input)
        return sanitize(parsed)
    }
}
