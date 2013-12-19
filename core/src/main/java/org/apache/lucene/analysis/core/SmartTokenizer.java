//package org.apache.lucene.analysis.core;
//
///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//import java.io.Reader;
//
//import org.apache.lucene.analysis.Tokenizer;
//import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
//import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
//import org.apache.lucene.analysis.util.CharTokenizer;
//import org.apache.lucene.util.AttributeSource;
//
///**
// * A WhitespaceTokenizer is a tokenizer that divides text at whitespace. Adjacent sequences of non-Whitespace characters form
// * tokens. <a name="version"/>
// * <p>
// * You must specify the required {@link Version} compatibility when creating {@link SmartTokenizer}:
// * <ul>
// * <li>As of 3.1, {@link CharTokenizer} uses an int based API to normalize and detect token characters. See
// * {@link CharTokenizer#isTokenChar(int)} and {@link CharTokenizer#normalize(int)} for details.</li>
// * </ul>
// */
//@Deprecated
//public final class SmartTokenizer extends TypeTokenizer {
//
//	public final static String WHITESPACE = "<WHITESPACE>";
//	public final static String SYMBOL = "<SYMBOL>";
//	public final static String NUMERIC = "<NUMERIC>";
//	public final static String ALPHA = "<ALPHA>";
//	// public final static String ALPHANUM = "<ALPHANUM>";
//	public final static String HANGUL = "<HANGUL>";
//	public final static String JAPANESE = "<JAPANESE>";
//	public final static String CHINESE = "<CHINESE>";
//	public final static String PARTNO = "<PARTNO>"; // 상품명등 영문숫자-혼합.
//	public final static String UNCATEGORIZED = "<UNCATEGORIZED>"; // 글자에대한 분류없음.
//
//	private final TypeAttribute typeAttribute = addAttribute(TypeAttribute.class);
//
//	/**
//	 * Construct a new WhitespaceTokenizer. * @param matchVersion Lucene version to match See {@link <a href="#version">above</a>}
//	 * 
//	 * @param in
//	 *            the input to split up into tokens
//	 */
//	public SmartTokenizer(Reader in) {
//		super(in);
//	}
//
//	/**
//	 * Construct a new WhitespaceTokenizer using a given {@link AttributeSource}.
//	 * 
//	 * @param matchVersion
//	 *            Lucene version to match See {@link <a href="#version">above</a>}
//	 * @param source
//	 *            the attribute source to use for this {@link Tokenizer}
//	 * @param in
//	 *            the input to split up into tokens
//	 */
//	public SmartTokenizer(AttributeSource source, Reader in) {
//		super(source, in);
//	}
//
//	/**
//	 * Construct a new WhitespaceTokenizer using a given {@link org.apache.lucene.util.AttributeSource.AttributeFactory}.
//	 * 
//	 * @param matchVersion
//	 *            Lucene version to match See {@link <a href="#version">above</a>}
//	 * @param factory
//	 *            the attribute factory to use for this {@link Tokenizer}
//	 * @param in
//	 *            the input to split up into tokens
//	 */
//	public SmartTokenizer(AttributeFactory factory, Reader in) {
//		super(factory, in);
//	}
//
//	@Override
//	protected void setType(String type) {
////		logger.debug("settype {}", type);
//		typeAttribute.setType(type);
//	}
//
////	@Override
////	protected void setType(int c) {
////		String prevType = typeAttribute.type();
////		Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(c);
////		// logger.debug("{}, prev {} pretype >> {} {}", (char)c, (char)prevChar, prevType, prevType.hashCode());
////		if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
////			if (prevType == TypeAttribute.DEFAULT_TYPE) {
////				typeAttribute.setType(ALPHA);
////			} else if (prevType == NUMERIC) {
////				typeAttribute.setType(ALPHANUM);
////			}
////		} else if (c >= '0' && c <= '9') {
////			if (prevType == TypeAttribute.DEFAULT_TYPE) {
////				typeAttribute.setType(NUMERIC);
////			} else if (prevType == ALPHA) {
////				typeAttribute.setType(ALPHANUM);
////			}
////		} else if (c == '-') {
////			if (prevType == ALPHA || prevType == NUMERIC || prevType == ALPHANUM) {
////				typeAttribute.setType(PARTNO);
////			}
////		} else if (c == '+') {
////			if (prevType == ALPHA || prevType == NUMERIC || prevType == ALPHANUM) {
////				typeAttribute.setType(ALPHANUM);
////			}
////		} else if (unicodeBlock == Character.UnicodeBlock.HANGUL_SYLLABLES) {
////			typeAttribute.setType(HANGUL);
////		} else if (unicodeBlock == Character.UnicodeBlock.HIRAGANA || unicodeBlock == Character.UnicodeBlock.KATAKANA) {
////			typeAttribute.setType(JAPANESE);
////		} else if (unicodeBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
////			typeAttribute.setType(CHINESE);
////		} else {
////			typeAttribute.setType(UNCATEGORIZED);
////		}
////	}
//
////	@Override
////	protected boolean isSplit(int c, int prevChar, int nextChar) {
////		return !isContinuosChar(c, prevChar, nextChar);
////	}
//
//	// 연속되어서 사용할 수 있는 패턴인지 확인한다.
//	private boolean isContinuosChar(int c, int prevChar, int nextChar) {
////		if (c == ',' || c == ':') {
////			// ','은 숫자통화형만 살린다.
////			// ':'은 16:9등의 패턴.
////			return (Character.isDigit(prevChar) && Character.isDigit(nextChar));
////		}
////		if (c == '-') {
////			if (((prevChar >= 'a' && prevChar <= 'z') || (prevChar >= 'A' && prevChar <= 'Z') || Character.isDigit(prevChar))
////					&& ((nextChar >= 'a' && nextChar <= 'z') || (nextChar >= 'A' && nextChar <= 'Z') || Character
////							.isDigit(nextChar))) {
////				return true;
////			}
////			return false;
////		}
////		if (c == '+') {
////			if ((prevChar >= 'a' && prevChar <= 'z') || (prevChar >= 'A' && prevChar <= 'Z') || Character.isDigit(prevChar)) {
////				return true;
////			}
////			return false;
////		}
////		if (c == '\'' || c == '’') {
////			return nextChar == 's';
////		}
////		if (c == '(' || c == ')' || c == '[' || c == ']') {
////			return false;
////		}
////		if (c == '.') {
////			// 영문 대문자는 약자이므로 살린다.
////			if (prevChar >= 'A' && prevChar <= 'Z') {
////				return true;
////			}
////			// 소숫점 숫자는 살린다.
////			if (Character.isDigit(prevChar) && Character.isDigit(nextChar)) {
////				return true;
////			}
////			return false;
////		}
//		// logger.debug("check {} >> {}", (char)c, Character.isLetterOrDigit(c));
//		// 나머지에 대해서는 letter나 숫자가 아니면 모두 없앤다.
//		return Character.isLetterOrDigit(c);
//	}
//
//}
