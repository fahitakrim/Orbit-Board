package org.futo.inputmethod.latin.bengali;

/**
 * Ridmik Parser
 * Open-source English-to-Bangla phonetic parser
 * Author: Shamim Hasnath
 */
public class RidmikParser {
	
	private final BanglaUnicode unicode = new BanglaUnicode();

	/**
	 * @param engWord (String) the english word, might contain spaces or any other characters
	 * @return parsed Bangla string in unicode
	 */
	public String toBangla(String engWord) {
		if (engWord == null || engWord.isEmpty()) {
			return "";
		}
		try {
			return AvroRuleEngine.INSTANCE.sanitize(parseInternal(engWord));
		} catch (final Throwable t) {
			return engWord;
		}
	}

	private void deleteTail(StringBuilder sb, int count) {
		if (sb == null || count <= 0) return;
		int len = sb.length();
		int del = Math.min(len, count);
		sb.delete(len - del, len);
	}

	private String parseInternal(String engWord) {
		StringBuilder st = new StringBuilder();
		char carry = 0;
		char secondCarry = 0;
		char thirdCarry = 0;
		boolean tempNoCarry = false;
		boolean jukta = false;
		char[] charArray = engWord.toCharArray();
		boolean prevJukta = false;

		// ======================= The great for loop starts =======================
		for (char now : charArray) {
			
			// we won't parse anything other than english letters & digits
			if (!((now >= 'a' && now <= 'z') || (now >= 'A' && now <= 'Z') || (now >= '0' && now <= '9'))) {
				st.append(now);
				carry = 0;
				continue;
			}

			if (now >= '0' && now <= '9') {
				st.append(now);
				carry = 0;
				secondCarry = 0;
				thirdCarry = 0;
				jukta = false;
				prevJukta = false;
				continue;
			}
			
			if (now == 'A' || now == 'B' || now == 'C' || now == 'E' || now == 'F' || now == 'P' || now == 'X')
				now = Character.toLowerCase(now);
			if (now == 'K' || now == 'L' || now == 'M' || now == 'V' || now == 'Y' || now == 'W' || now == 'Q')
				now = Character.toLowerCase(now);
			
			if (now == 'H' && carry != 'T') // khondiyo to -> TH
				now = 'h';
			
			// 'w' should be 'O' when it's the first one or comes after a vowel
			if ((carry == 0 || isVowel(carry)) && now == 'w')
				now = 'O';

			if (isVowel(now)) {
				// special for wri kar & wri
				if (carry == 'r' && secondCarry == 'r' && now == 'i') {
					if (thirdCarry == 0) {
						deleteTail(st, 2);
						st.append("\u098B"); // wri
					} else {
						deleteTail(st, 3);
						st.append("\u09C3"); // wri kar
					}
					carry = 'i';
					continue;
				}
				
				String dual;
				if (secondCarry != 0)
					dual = unicode.getDualKar(now, carry);
				else dual = unicode.getDual(now, carry); // dual as the first character of st
				
				if (dual != null) {
					if (carry != 'o')
						deleteTail(st, 1);
					if (isVowel(secondCarry)) { // a dual kar does not applied on vowel
						st.append(unicode.get(carry)).append(unicode.get(now));
					} else
						st.append(dual);
				} else if (now == 'o' && carry != 0) {
					if (isVowel(carry))
						st.append(unicode.get('O'));
					else {
						thirdCarry = secondCarry;
						secondCarry = carry;
						carry = now;
						continue;
					}
				} else if (isVowel(carry) || carry == 0) {
					if (now == 'a' && carry != 0) // not first a
						st.append(unicode.get('y')).append(unicode.getKar('a'));
					else
						st.append(unicode.get(now));
				} else {
					st.append(unicode.getKar(now));
				}
			}

			if (now == 'y' || now == 'Z' || now == 'r')
				jukta = false;
			
			// when previous was a jukta and dual of the later two is not available
			// go to the else part of the next if block i.e now is independent
			tempNoCarry = jukta && unicode.getDual(now, carry) == null;
			
			if (isConsonant(now) && isConsonant(carry) && !tempNoCarry) {
				// handle jo fola
				if (now == 'y' || now == 'Z') {
					if (now == 'y' && carry == 'q' && secondCarry == 'q');
					else 
						now = 'z';
				}
				
				// handle gg as in gyan, second carry not n, to skip onushar/unga
				if (carry == 'g' && now == 'g' && secondCarry != 'N' && secondCarry != 'n') {
					deleteTail(st, 1);
					st.append("\u099C\u09CD\u099E");
					prevJukta = jukta;
					jukta = true;
					secondCarry = 'g';
					continue;
				}
				
				// handle kkh = kSh
				if (secondCarry == 'k' && carry == 'k' && now == 'h')
					carry = 'S';
				
				// check if dual
				String dual = unicode.getDual(now, carry);
				
				if (dual != null) {
					// handle kaNgkShito
					if (thirdCarry == 'g' && secondCarry == 'k' && carry == 'S' && now == 'h')
						prevJukta = jukta = false;
					
					boolean firstOrAfterVowelOrJukta = isVowel(secondCarry) || secondCarry == 0 || prevJukta;
					
					if (dualSitsUnder(thirdCarry, secondCarry, carry, now) && !firstOrAfterVowelOrJukta) {
						deleteTail(st, 1);
						if (secondCarry == 'r' && thirdCarry == 'r')
							deleteTail(st, 1);
						if (jukta);
						else if (secondCarry != 0 && !isVowel(secondCarry))
							st.append("\u09CD");
						
						st.append(dual);
						prevJukta = jukta;
						jukta = true;
					} else {
						if (jukta)
							deleteTail(st, 2);
						else deleteTail(st, 1);
						
						if (secondCarry == 'g' && carry == 'g' && now == 'h') {
							deleteTail(st, 1);
							st.append(unicode.get('g'));
						}
						
						st.append(dual);
						if (dual.equals("\u0982")) {
							carry = 0;
							secondCarry = 0;
							thirdCarry = 0;
							jukta = false;
							prevJukta = false;
							continue;
						}
						prevJukta = jukta;
						jukta = false;
					}
				} else {
					prevJukta = jukta;
					jukta = false;
					
					if (secondCarry != 'r' && carry == 'r' && now == 'z') {
						st.append("\u200D\u09CD"); // handle rya as in ransom/rapid
					} else if (carry == 'r' && secondCarry != 'r');
					else if (carry == 'r' && secondCarry == 'r' && isConsonant(thirdCarry));
					else if (carry == 'r' && secondCarry == 'r' && (isVowel(thirdCarry) || thirdCarry == 0)) {
						deleteTail(st, 1);
						st.append("\u09CD");
					} else if (notJukta(thirdCarry, secondCarry, carry, now));
					else {
						st.append("\u09CD");
						jukta = true;
					}
					
					st.append(unicode.get(now));
				}
			} else if (isConsonant(now)) {
				if (isVowel(carry) && now == 'Z')
					st.append("\u09CD");
				
				if (carry == 0 && now == 'x')
					st.append(unicode.get('e'));
				
				prevJukta = jukta;
				jukta = false;
				
				// to write b-fola 
				if (now == 'w' && isConsonant(carry) && isConsonant(secondCarry)) {
					st.append("\u09CD");
					prevJukta = jukta;
					jukta = true;
				}
				// handle lakshmi/ lokhnou
				if (thirdCarry == 'k' && secondCarry == 'S' && carry == 'h' && (now == 'N' || now == 'm')) {
					st.append("\u09CD");
					prevJukta = false;
					jukta = true;
				}
				st.append(unicode.get(now));
			}
			
			thirdCarry = secondCarry;
			secondCarry = carry;
			carry = now;
		} // end of for loop
		
		return st.toString();
	}
	
	boolean isVowel(char now) {
		return "AEIOUaeiou".indexOf(now) != -1;
	}
	
	boolean isConsonant(char now) {
		return !isVowel(now) && Character.isLetter(now);
	}
	
	boolean isCharInString(char now, String foo) {
		return foo != null && foo.indexOf(now) != -1;
	}
	
	boolean dualSitsUnder(char thirdCarry, char secondCarry, char carry, char now) {
		if (secondCarry == 'r' && thirdCarry == 'r')
			return true;
		if (secondCarry == 'r')
			return false;
		
		String djkt = unicode.getDjkt(carry, now);
		if (djkt != null && isCharInString(secondCarry, djkt))
			return true;

		String djktt = unicode.getDjktt(carry, now);
		if (djktt != null)
			return djktt.contains(Character.toString(thirdCarry) + Character.toString(secondCarry));

		return false;
	}
	
	boolean notJukta(char thirdCarry, char secondCarry, char carry, char now) {
		if (now == 'r' || now == 'z' || now == 'w')
			return false;
	
		String foo = unicode.getDualJkt(secondCarry, carry);
		if (foo != null)
			return !isCharInString(now, foo);
		
		foo = unicode.getJkt(carry);
		if (foo != null)
			return !isCharInString(now, foo);
		
		return true;
	}
}
