package org.fastcatsearch.ir.analysis.general;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.standard.StandardTokenizerInterface;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class GeneralTokenizerImpl implements StandardTokenizerInterface {

	static private final int T_ARA = 1; /* Arabic */
	static private final int T_ARM = 2; /* Armenian */
	static private final int T_BLN = 3; /* Spaces */
	static private final int T_BNG = 4; /* Bengali */
	static private final int T_CAN = 5; /* Canadian Syllabics */
	static private final int T_CHE = 6; /* Cherokee */
	static private final int T_CJK = 7; /* Chinese */
	static private final int T_COP = 8; /* Coptic */
	static private final int T_CTK = 9; /* Control Characters */
	static private final int T_CYR = 10; /* Cyrillic */
	static private final int T_DEV = 11; /* Devanagari */
	static private final int T_DIG = 12; /* Digits */
	static private final int T_ETH = 13; /* Ethiopic */
	static private final int T_GEO = 14; /* Georgian */
	static private final int T_GRE = 15; /* Greek */
	static private final int T_GUJ = 16; /* Gujarati */
	static private final int T_GUR = 17; /* Gurmukhi */
	static private final int T_HAN = 18; /* Hangul */
	static private final int T_HEB = 19; /* Hebrew */
	static private final int T_JPN = 20; /* Japanese */
	static private final int T_KAN = 21; /* Kannada */
	static private final int T_KHM = 22; /* Khmer */
	static private final int T_LAO = 23; /* Lao */
	static private final int T_LAT = 24; /* Latin */
	static private final int T_MAL = 25; /* Malayalam */
	static private final int T_MON = 26; /* Mongolian */
	static private final int T_MYA = 27; /* Myanmar */
	static private final int T_OGH = 28; /* Ogham */
	static private final int T_ORI = 29; /* Oriya */
	static private final int T_RES = 30; /* Reserved */
	static private final int T_RUN = 31; /* Runic */
	static private final int T_SIN = 32; /* Sinhala */
	static private final int T_SPC = 33; /* Special Characters */
	static private final int T_SYM = 34; /* Symbols */
	static private final int T_SYR = 35; /* Syriac */
	static private final int T_TAM = 36; /* Tamil */
	static private final int T_TEL = 37; /* Telugu */
	static private final int T_THI = 38; /* Thai */
	static private final int T_THN = 39; /* Thaana */
	static private final int T_TIB = 40; /* Tibetan */
	static private final int T_YIS = 41; /* Yi Syllables */
	
	public enum AutomataState{
		T_ARA, T_ARM ,T_BLN, T_BNG, T_CAN, T_CHE, T_CJK, T_COP, 
		T_CTK, T_CYR, T_DEV, T_DIG, T_ETH, T_GEO, T_GRE, T_GUJ,
		T_GUR, T_HAN, T_HEB, T_JPN, T_KAN, T_KHM, T_LAO, T_LAT,
		T_MAL, T_MON, T_MYA, T_OGH, T_ORI, T_RES, T_RUN, T_SIN,
		T_SPC, T_SYM, T_SYR, T_TAM, T_TEL, T_THI, T_THN, T_TIB, 
		T_YIS};
	
	private AutomataState[] AutomataTable = new AutomataState[65536]; 
	UnicodeSection section[] = new UnicodeSection[41];
	
	private void fillType(AutomataState type, int start, int end)
	{
		for ( int i = start ; i < end ; i ++ )
			AutomataTable[i] = type;
	}
	
	private void initSection()
	{
		section[0] = new UnicodeSection(T_SYM, 0,65536);
		section[1] = new UnicodeSection(T_ARA, 0,0);
	}
	
	public GeneralTokenizerImpl()
	{
		//basic constructor
		fillType(AutomataState.T_SYM, 0, 65536);
		fillType(AutomataState.T_ARA, 0x0600, 0x06FF);
		fillType(AutomataState.T_ARM, 0x0530, 0x058F);
		fillType(AutomataState.T_BLN, 0,0);
	}
			
	@Override
	public void getText(CharTermAttribute t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	/**
	 * return current position
	 * */
	public int yychar() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	/**
	 * reset Stream
	 * */
	public void yyreset(Reader reader) {
		// TODO Auto-generated method stub
		
	}

	@Override
	/**
	 * Returns the length of the matched text region 
	 */
	public int yylength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	/**
	 * 
	 */
	public int getNextToken() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

}
