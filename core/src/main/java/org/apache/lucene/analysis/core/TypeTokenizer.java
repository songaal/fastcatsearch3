package org.apache.lucene.analysis.core;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.analysis.util.CharacterUtils;
import org.apache.lucene.analysis.util.CharacterUtils.CharacterBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 좌우의 캐릭터를 감안해서 타입별로 토크나이징을 하는 클래스. swsong.
 **/
public class TypeTokenizer extends Tokenizer {
	protected Logger logger = LoggerFactory.getLogger(TypeTokenizer.class);

	private final TypeAttribute typeAttribute = addAttribute(TypeAttribute.class);
	private int positionIncrement;
	// note: bufferIndex is -1 here to best-effort AIOOBE consumers that don't call reset()
	private int offset, bufferIndex, dataLen, finalOffset;
	private static final int MAX_WORD_LEN = 255;
	private static final int IO_BUFFER_SIZE = 4096;

	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private final PositionIncrementAttribute positionIncrementAttribute = addAttribute(PositionIncrementAttribute.class);

	private final CharacterUtils charUtils;
	private final CharacterBuffer ioBuffer = CharacterUtils.newCharacterBuffer(IO_BUFFER_SIZE);

	protected int prevChar = -1;
	protected String prevType;
	protected int nextChar = -1;
	protected String nextType;
	
	public static final String WHITESPACE = "<WHITESPACE>";
	public static final String SYMBOL = "<SYMBOL>";
	public final static String ALPHA = "<ALPHA>";
	public final static String NUMBER = "<NUMBER>";
	public final static String HANGUL = "<HANGUL>";
	public final static String HANGUL_JAMO = "<HANGUL_JAMO>";
	public final static String JAPANESE = "<JAPANESE>";
	public final static String CHINESE = "<CHINESE>";
	public final static String OTHER_LANGUAGE = "<OTHER_LANGUAGE>";
	public final static String UNCATEGORIZED = "<UNCATEGORIZED>"; // 글자에대한 분류없음.
	
	public TypeTokenizer(Reader input) {
		super(input);
		charUtils = CharacterUtils.getInstance();
	}

	public TypeTokenizer(AttributeSource source, Reader input) {
		super(source, input);
		charUtils = CharacterUtils.getInstance();
	}

	public TypeTokenizer(AttributeFactory factory, Reader input) {
		super(factory, input);
		charUtils = CharacterUtils.getInstance();
	}

	public static String getType(int ch) {
		if(Character.isWhitespace(ch)){
			return WHITESPACE;
		}
		
		int type = Character.getType(ch);
		
		switch(type){
		case Character.DASH_PUNCTUATION:
		case Character.START_PUNCTUATION:
		case Character.END_PUNCTUATION:
		case Character.CONNECTOR_PUNCTUATION:
		case Character.OTHER_PUNCTUATION:
		case Character.MATH_SYMBOL:
		case Character.CURRENCY_SYMBOL:
		case Character.MODIFIER_SYMBOL:
		case Character.OTHER_SYMBOL:
		case Character.INITIAL_QUOTE_PUNCTUATION:
		case Character.FINAL_QUOTE_PUNCTUATION:
			return SYMBOL;
		case Character.OTHER_LETTER:
			//외국어.
			Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(ch);
			if (unicodeBlock == Character.UnicodeBlock.HANGUL_SYLLABLES){
				return HANGUL;
			} else if (unicodeBlock == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO) {
				return HANGUL_JAMO;
			} else if (unicodeBlock == Character.UnicodeBlock.HIRAGANA || unicodeBlock == Character.UnicodeBlock.KATAKANA) {
				return JAPANESE;
			} else if (unicodeBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
				return CHINESE;
			}else{
				return OTHER_LANGUAGE;
			}
		case Character.UPPERCASE_LETTER:
		case Character.LOWERCASE_LETTER:
			//영어.
			return ALPHA;
			
		case Character.DECIMAL_DIGIT_NUMBER:
			return NUMBER;
		}
		
		return UNCATEGORIZED;
	}

	//입력한 타입으로 셋팅한다.
	protected void setType(String type) {
//		logger.debug("settype {}", type);
		typeAttribute.setType(type);
	}
	
	/**
	 * Called on each token character to normalize it before it is added to the token. The default implementation does nothing.
	 * Subclasses may use this to, e.g., lowercase tokens.
	 */
	protected int normalize(int c) {
		return c;
	}

	/*
	 * 분리해야하는지 판별한다.
	 * 연결될경우 null 이 아닌 type을 넘겨주며, 변형된 type이 리턴될수 있다.
	 */
	protected String isSplit(int ch, String type){
		
//		String prevType = this.prevType;
//		
//		if (ch == '.') {
//			// 영문 대문자는 약자이므로 살린다.
//			if (prevChar >= 'A' && prevChar <= 'Z') {
//				//허용.
//				return ALPHA;
//			}
//			// 소숫점 숫자는 살린다.
//			if (Character.isDigit(prevChar) && Character.isDigit(nextChar)) {
//				//허용.
//				return NUMBER;
//			}
//		}
//		
////		logger.debug("## {} : {} <- {}", (char) ch, type, prevType);
//		if(Character.isLetterOrDigit(ch)){
//			//타입이 같으면 분리하지 않는다.
//			if(prevType == null || prevType == type){
////				this.prevType = type;
//				return type;
//			}
//			//letter digit이라도 타입이 다르면 분리한다.
//		}
		if(prevType == null || prevType == type){
			prevType = type;
			return type;
		}
		return null;
	}
	
	@Override
	public final boolean incrementToken() throws IOException {
		clearAttributes();
		int length = 0;
		int start = -1; // this variable is always initialized
		int end = -1;
		char[] buffer = termAtt.buffer();
		// boolean hasDelimitChar = false;

		boolean needPositionIncrement = false;
		while (true) {
			if (bufferIndex >= dataLen) {
				offset += dataLen;
				if (!charUtils.fill(ioBuffer, input)) { // read supplementary char aware with CharacterUtils
					dataLen = 0; // so next offset += dataLen won't decrement offset
					if (length > 0) {
						break;
					} else {
						finalOffset = correctOffset(offset);
						return false;
					}
				}
				dataLen = ioBuffer.getLength();
				bufferIndex = 0;
			}
			// use CharacterUtils here to support < 3.1 UTF-16 code unit behavior if the char based methods are gone
			int c = -1;
			String type = null;
			if(nextChar == -1){
				c = charUtils.codePointAt(ioBuffer.getBuffer(), bufferIndex);
				type = getType(c);
			}else{
				c = nextChar;
				type = nextType;
//				logger.debug("use next {}", (char) c);
			}
			
			nextChar = -1;
			if (bufferIndex + 1 < dataLen) {
				nextChar = charUtils.codePointAt(ioBuffer.getBuffer(), bufferIndex + 1);
				nextType = getType(nextChar);
			}
			final int charCount = Character.charCount(c);
//			bufferIndex += charCount;
			
			if(type == WHITESPACE){
				if (length > 0) {
					// 유효토큰을 발견한 상태에서 공백문자등이 나타나면 여기까지 잘라서 리턴한다.
					// Whitespace 도 1을 차지한다.
					needPositionIncrement = true;//일단 token반환후에 position을 증가시킨다.
					prevChar = c;
					// hasDelimitChar = true;
					bufferIndex += charCount;//버린다.
					break;
				}else{
					//연속된 whitespace이거나 이전에 문자가 없었다면, positionIncrement++를 하지 않는다.
					if(prevChar != -1 && prevChar != c){
						positionIncrement++;	//position즉시증가.
					}
					prevChar = c;
					bufferIndex += charCount;//버린다.
				}
			}else{
				String newType = isSplit(c, type);
				prevType = newType;
				if(newType == null){
					//분리한다.
					prevChar = c;
					if (length == 0) { // start of token
						//자기자신을 리턴.
						setType(type);
						start = offset + bufferIndex;
						end = start + charCount;
						length += Character.toChars(normalize(c), buffer, length);
						bufferIndex += charCount;//다음을 위해 증가.
					}
					//else의 경우 bufferIndex를 증가시키면 delimiter를 잃어버리게 되므로 증가시키지 않는다.
					break;
				}else{
					//연결한다.
					setType(newType);
					prevChar = c;
					if (length == 0) { // start of token
						assert start == -1;
						start = offset + bufferIndex;
						end = start;
					} else if (length >= buffer.length - 1) { // check if a supplementary could run out of bounds
						buffer = termAtt.resizeBuffer(2 + length); // make sure a supplementary fits in the buffer
					}
					end += charCount;
					bufferIndex += charCount; //다음을 위해 증가.
					
					length += Character.toChars(normalize(c), buffer, length); // buffer it, normalized
					if (length >= MAX_WORD_LEN) // buffer overflow! make sure to check for >= surrogate pair could break == test
						break;
				}
				
			}
		}
		
		
		termAtt.setLength(length);
		assert start != -1;
		offsetAtt.setOffset(correctOffset(start), finalOffset = correctOffset(end));
		positionIncrementAttribute.setPositionIncrement(positionIncrement++);
		if(needPositionIncrement){
			positionIncrement++;
		}
		//분리했으면 prevType을 없애서 의존관계소멸.
		prevType = null; //whitespace분리시 사용됨.
		//nextchar를 없앤다.
		nextChar = -1;
		return true;
	}

	protected void makeTerm(){
		
	}
	@Override
	public final void end() {
		// set final offset
		offsetAtt.setOffset(finalOffset, finalOffset);
	}

	@Override
	 public void setReader(Reader input) throws IOException {
		super.setReader(input);
		positionIncrement = 0;
		bufferIndex = 0;
		offset = 0;
		dataLen = 0;
		finalOffset = 0;
		ioBuffer.reset(); // make sure to reset the IO buffer!!
		nextChar = -1;
	}
}