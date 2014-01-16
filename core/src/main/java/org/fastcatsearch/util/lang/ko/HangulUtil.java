/*
 * Copyright (C) 2011 WebSquared Inc. http://websqrd.com
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.fastcatsearch.util.lang.ko;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HangulUtil {
	private static Logger logger = LoggerFactory.getLogger(HangulUtil.class);

	public static final String CHOSUNG_LIST = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ"; //19
	public static final String JUNGSUNG_LIST = "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ"; //21
	public static final String JONGSUNG_LIST = " ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ"; //28
	private static final char unicodeHangulBase = '\uAC00';
	private static final char unicodeHangulLast = '\uD7A3';

	public static String decomposeHangul(String keyword) {
		StringBuffer decomposed = new StringBuffer();
		for (int i = 0; i < keyword.length(); i++) {
			char ch = keyword.charAt(i);
			if(Character.isWhitespace(ch)){
				continue;
			}else if (ch < unicodeHangulBase || ch > unicodeHangulLast) {
				decomposed.append(ch);
			} else {
				int unicode = ch - unicodeHangulBase;
				int choSung = unicode / (JUNGSUNG_LIST.length() * JONGSUNG_LIST.length());
				unicode = unicode % (JUNGSUNG_LIST.length() * JONGSUNG_LIST.length());
				int jungSung = unicode / JONGSUNG_LIST.length();
				int jongSung = unicode % JONGSUNG_LIST.length();
				decomposed.append(CHOSUNG_LIST.charAt(choSung));
				if(jungSung >= 0){
					decomposed.append(JUNGSUNG_LIST.charAt(jungSung));
				}
				
				//종성이 없는 경우는 무시. 0포함안함.
				if(jongSung > 0){
					decomposed.append(JONGSUNG_LIST.charAt(jongSung));
				}
			}
		}
		
		return decomposed.toString();
	}
	// 초중종성의 조합을 만든다.
	public static String makeHangulPrefix(String keyword, char delimiter) {
		StringBuffer candidate = new StringBuffer();
		StringBuffer prefix = new StringBuffer();
		for (int i = 0; i < keyword.length(); i++) {
			char ch = keyword.charAt(i);
			if (ch < unicodeHangulBase || ch > unicodeHangulLast) {
				prefix.append(ch);
				candidate.append(prefix);
			} else {
				// Character is composed of {Chosung+Jungsung} OR
				// {Chosung+Jungsung+Jongsung}
				int unicode = ch - unicodeHangulBase;
				int choSung = unicode / (JUNGSUNG_LIST.length() * JONGSUNG_LIST.length());
				// 1. add prefix+chosung
				candidate.append(prefix);
				candidate.append(CHOSUNG_LIST.charAt(choSung));
				candidate.append(delimiter);
				// 2. add prefix+chosung+jungsung
				unicode = unicode % (JUNGSUNG_LIST.length() * JONGSUNG_LIST.length());
				int jongSung = unicode % JONGSUNG_LIST.length();
				char choJung = (char) (ch - jongSung);
				candidate.append(prefix);
				candidate.append(choJung);
				// change prefix
				prefix.append(ch);
				if (jongSung > 0) {
					candidate.append(delimiter);
					// 3. add whole character
					candidate.append(prefix);
				}
			}
			if (i < keyword.length() - 1)
				candidate.append(delimiter);
		}
		return candidate.toString();
	}

	// suffix만들기.
	public static String makeHangulSuffix(String keyword, char delimiter) {
		StringBuffer candidate = new StringBuffer();
		for (int i = 1; i < keyword.length(); i++) {
			candidate.append(keyword.substring(i));
			candidate.append(delimiter);
		}
		return candidate.toString();
	}
	
	// 초성검색
	public static String makeHangulChosung(String keyword, char delimiter) {
		StringBuffer candidate = new StringBuffer();
		StringBuffer prefix = new StringBuffer();
		for (int i = 0; i < keyword.length(); i++) {
			char ch = keyword.charAt(i);
			if (ch >= unicodeHangulBase && ch <= unicodeHangulLast) {
				int unicode = ch - unicodeHangulBase;
				int choSung = unicode / (JUNGSUNG_LIST.length() * JONGSUNG_LIST.length());
				candidate.append(prefix);
				candidate.append(CHOSUNG_LIST.charAt(choSung));
				
				if (i < keyword.length() - 1) {
					candidate.append(delimiter);
				}
				
				prefix.append(CHOSUNG_LIST.charAt(choSung));
			}
		}
		return candidate.toString();
	}

	public static char mergeJaso(String choSung, String jungSung, String jongSung) {
		int choSungPos = CHOSUNG_LIST.indexOf(choSung);
		int jungSungPos = JUNGSUNG_LIST.indexOf(jungSung);
		int jongSungPos = JONGSUNG_LIST.indexOf(jongSung);

		int unicode = unicodeHangulBase + (choSungPos * JUNGSUNG_LIST.length() + jungSungPos) * JONGSUNG_LIST.length() + jongSungPos;

		return (char) unicode;
	}

	public static HangulInfo devideJaso(char hanChar) {

		HangulInfo hangulInfo = new HangulInfo();

		// if character is not Hangul
		if (hanChar < unicodeHangulBase || hanChar > unicodeHangulLast) {
			hangulInfo.isHangul = false;
			hangulInfo.hanChar = hanChar;
			hangulInfo.hanParts = null;
		} else {
			int unicode = hanChar - unicodeHangulBase;
			int choSung = unicode / (JUNGSUNG_LIST.length() * JONGSUNG_LIST.length());
			unicode = unicode % (JUNGSUNG_LIST.length() * JONGSUNG_LIST.length());
			int jungSung = unicode / JONGSUNG_LIST.length();
			int jongSung = unicode % JONGSUNG_LIST.length();
			hangulInfo.isHangul = true;
			hangulInfo.hanChar = hanChar;
			hangulInfo.hanParts = new char[] { CHOSUNG_LIST.charAt(choSung), JUNGSUNG_LIST.charAt(jungSung), JONGSUNG_LIST.charAt(jongSung) };
		}

		return hangulInfo;
	}

	public static class HangulInfo {
		public boolean isHangul;
		public char hanChar;
		public char[] hanParts;

		public String toString() {
			String parts = "";
			if (hanParts != null) {
				for (int i = 0; i < hanParts.length; i++) {
					parts += hanParts[i];
					if (i < hanParts.length - 1)
						parts += ",";
				}
			}
			return "[HangulInfo]" + isHangul + ":" + hanChar + ":" + parts;

		}
	}

}
