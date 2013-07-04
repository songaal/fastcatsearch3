package org.fastcatsearch.ir.analysis.general;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizerImpl;
import org.apache.lucene.analysis.standard.StandardTokenizerInterface;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;

public class GeneralTokenizer extends Tokenizer {
	private int maxTokenLength = StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH;
	private StandardTokenizerInterface scanner;

	private final int T_ARA = 1; /* Arabic */
	private final int T_ARM = 2; /* Armenian */
	private final int T_BLN = 3; /* Spaces */
	private final int T_BNG = 4; /* Bengali */
	private final int T_CAN = 5; /* Canadian Syllabics */
	private final int T_CHE = 6; /* Cherokee */
	private final int T_CJK = 7; /* Chinese */
	private final int T_COP = 8; /* Coptic */
	private final int T_CTK = 9; /* Control Characters */
	private final int T_CYR = 10; /* Cyrillic */
	private final int T_DEV = 11; /* Devanagari */
	private final int T_DIG = 12; /* Digits */
	private final int T_ETH = 13; /* Ethiopic */
	private final int T_GEO = 14; /* Georgian */
	private final int T_GRE = 15; /* Greek */
	private final int T_GUJ = 16; /* Gujarati */
	private final int T_GUR = 17; /* Gurmukhi */
	private final int T_HAN = 18; /* Hangul */
	private final int T_HEB = 19; /* Hebrew */
	private final int T_JPN = 20; /* Japanese */
	private final int T_KAN = 21; /* Kannada */
	private final int T_KHM = 22; /* Khmer */
	private final int T_LAO = 23; /* Lao */
	private final int T_LAT = 24; /* Latin */
	private final int T_MAL = 25; /* Malayalam */
	private final int T_MON = 26; /* Mongolian */
	private final int T_MYA = 27; /* Myanmar */
	private final int T_OGH = 28; /* Ogham */
	private final int T_ORI = 29; /* Oriya */
	private final int T_RES = 30; /* Reserved */
	private final int T_RUN = 31; /* Runic */
	private final int T_SIN = 32; /* Sinhala */
	private final int T_SPC = 33; /* Special Characters */
	private final int T_SYM = 34; /* Symbols */
	private final int T_SYR = 35; /* Syriac */
	private final int T_TAM = 36; /* Tamil */
	private final int T_TEL = 37; /* Telugu */
	private final int T_THI = 38; /* Thai */
	private final int T_THN = 39; /* Thaana */
	private final int T_TIB = 40; /* Tibetan */
	private final int T_YIS = 41; /* Yi Syllables */

	public static final String[] TOKEN_TYPES = new String[] { "Arabic", "Armenian", "Spaces", "Bengali",
			"Canadian Syllabics", "Cherokee", "Chinese", "Coptic", "Control Characters", "Cyrillic", "Devanagari",
			"Digits", "Ethiopic", "Georgian", "Greek", "Gujarati", "Gurmukhi", "Hangul", "Hebrew", "Japanese",
			"Kannada", "Khmer", "Lao", "Latin", "Malayalam", "Mongolian", "Myanmar", "Ogham", "Oriya", "Reserved",
			"Runic", "Sinhala", "SC", "Symbols", "Syriac", "Tamil", "Telugu", "Thai", "Thaana", "Tibetan", "Yi" };

	public void setMaxTokenLength(int length) {
		this.maxTokenLength = length;
	}

	public int getMaxTokenLength() {
		return maxTokenLength;
	}

	public GeneralTokenizer(Reader input) {
		super(input);
		init();
	}

	public GeneralTokenizer(AttributeSource source, Reader input) {
		super(source, input);
		init();
	}

	public GeneralTokenizer(AttributeFactory factory, Reader input) {
		super(factory, input);
		init();
	}

	private final void init() {
		this.scanner = new GeneralTokenizerImpl();
	}

	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
	private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

	@Override
	public boolean incrementToken() throws IOException {
		clearAttributes();
		int posIncr = 1;

		while (true) {
			int tokenType = scanner.getNextToken();

			if (tokenType == StandardTokenizerInterface.YYEOF) {
				return false;
			}

			if (scanner.yylength() <= maxTokenLength) {
				posIncrAtt.setPositionIncrement(posIncr);
				scanner.getText(termAtt);
				final int start = scanner.yychar();
				offsetAtt.setOffset(correctOffset(start), correctOffset(start + termAtt.length()));

				if (tokenType == StandardTokenizer.ACRONYM_DEP) {
					typeAtt.setType(StandardTokenizer.TOKEN_TYPES[StandardTokenizer.HOST]);
					termAtt.setLength(termAtt.length() - 1);
				} else
					typeAtt.setType(StandardTokenizer.TOKEN_TYPES[tokenType]);
				return true;
			} else
				posIncr++;
		}
	}

	@Override
	public final void end() {
		int finalOffset = correctOffset(scanner.yychar() + scanner.yylength());
		offsetAtt.setOffset(finalOffset, finalOffset);
	}

	@Override
	public void reset() throws IOException {
		scanner.yyreset(input);
	}
}
