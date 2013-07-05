/*
 * Copyright 2013 Websquared, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fastcatsearch.ir.filter;

import java.io.IOException;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.io.ByteArrayOutput;
import org.fastcatsearch.ir.io.DataRef;
import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.query.Filter;
import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class FilterFunction {
	protected static Logger logger = LoggerFactory.getLogger(FilterFunction.class);
	protected FieldSetting fieldSetting;
	protected int patternCount;
	protected BytesRef[] patternList;
	protected BytesRef[] endPatternList;
	protected int boostScore;
	protected int fieldByteSize;
	protected boolean isBoostFunction;
	
/*	case MATCH:
	case SECTION:
	case PREFIX:
	case SUFFIX:
	case MATCH_BOOST:
	case SECTION_BOOST:
	case PREFIX_BOOST:
	case SUFFIX_BOOST:
	case EXCLUDE:
*/
	public FilterFunction(Filter filter, FieldSetting fieldSetting, boolean isBoostFunction) throws FilterException{
		this.fieldSetting = fieldSetting;
		this.isBoostFunction = isBoostFunction;
		this.fieldByteSize = fieldSetting.getByteSize();
		patternCount = filter.patternLength();
		patternList = new BytesRef[patternCount];
		endPatternList = new BytesRef[patternCount];
		boostScore = filter.boostScore();
		try {
			for (int j = 0; j < patternCount; j++) {
				//패턴의 byte 데이터를 얻기위해 필드객체를 생성한다.
				//패턴과 필드데이터를 같은 길이의 byte[]로 만들어놓고 비교를 한다.
				
				Field f = fieldSetting.createPatternField(filter.pattern(j));
				int patternByteSize = fieldSetting.getByteSize();
				ByteArrayOutput arrayOutput = new ByteArrayOutput(patternByteSize);
				f.writeFixedDataTo(arrayOutput);
				patternList[j] = arrayOutput.bytesRef();
				
				
	//			Field f = fieldSetting.createField(filter.pattern(j));
	//			int patternByteSize = fieldSetting.getByteSize();
	//			patternList[j] = new FastByteBuffer(patternByteSize);
	//			f.getFixedBytes(patternList[j]);
	//			patternList[j].flip();
				if(filter.isEndPatternExist()){
					f = fieldSetting.createPatternField(filter.endPattern(j));
					patternByteSize = fieldSetting.getByteSize();
					arrayOutput = new ByteArrayOutput(patternByteSize);
					f.writeFixedDataTo(arrayOutput);
					endPatternList[j] = arrayOutput.bytesRef();
	//				f = fieldSetting.createField(filter.endPattern(j));
	//				patternByteSize = fieldSetting.getByteSize();
	//				endPatternList[j] = new FastByteBuffer(patternByteSize);
	//				f.getFixedBytes(endPatternList[j]);
	//				endPatternList[j].flip();
				}
			}
		} catch (IOException e) {
			throw new FilterException("필터패턴생성중 에러", e);
		}
//		for (int j = 0; j < patternCount; j++) {
//			//2012-10-22 swsong
//			//패턴의 byte 데이터를 얻기위해 필드객체를 생성한다.
//			//멀티밸류의 경우도 있으므로, createPatternField를 사용하도록 한다.
//			String pat = filter.pattern(j);
//			if(fieldSetting.isNumeric() && "".equals(pat)) {
////				pat = "0"; 
//			}
////			logger.debug("pat-1 = {}", pat);
//			Field f = fieldSetting.createPatternField(pat);
//			int patternByteSize = fieldSetting.getPatternByteSize(pat);
//			patternList[j] = new FastByteBuffer(patternByteSize);
//			f.getRealBytes(patternList[j]);
//			patternList[j].flip();
//			
//			if(filter.isEndPatternExist()){
//				pat = filter.endPattern(j);
////				logger.debug("pat-2 = {}", pat);
//				if("".equals(pat)) {
//					//종료패턴의 경우는 비어있으면 null로 비워둔다. 즉, 종료조건 체크안함. 
//				}else{
//					f = fieldSetting.createPatternField(pat);
//					patternByteSize = fieldSetting.getPatternByteSize(pat);
//					endPatternList[j] = new FastByteBuffer(patternByteSize);
//					f.getRealBytes(endPatternList[j]);
//					endPatternList[j].flip();
//				}
//			}
//			
////			logger.debug("Filter pattern-{} >> {}, {}", new Object[]{j, patternList[j].toAlphaString(), endPatternList[j] != null?endPatternList[j].toAlphaString():"null"});
//		}
		
		//section필터의 경우 입력 패턴과 상관없으므로 에러발생한면 안됨.
//		for (int j = 0; j < patternCount; j++) {
//			int patternLength = filter.pattern(j).length();
//			if(fieldSetting.type == FieldSetting.Type.AChar){
//				//패턴길이가 데이터크기보다 크면 비교가 안되므로 무조건 매칭이 안됨.
//				if(patternLength > fieldByteSize){
//					throw new TooLongFilterPatternException("시작패턴 문자열이 필드길이보다 큽니다. 필드 = "+fieldSetting.name+", 패턴 = "+filter.pattern(j)+", patternSize="+patternLength+", fieldSize="+fieldByteSize);
//				}
//			}else if(fieldSetting.type == FieldSetting.Type.UChar){
//				if(patternLength * 2 > fieldByteSize){
//					throw new TooLongFilterPatternException("시작패턴 문자열이 필드길이보다 큽니다. 필드 = "+fieldSetting.name+", 패턴 = "+filter.pattern(j)+", patternSize="+(patternLength * 2)+", fieldSize="+fieldByteSize);
//				}
//			}
//			
//			if(filter.isEndPatternExist()){
//				int endPatternLength = filter.endPattern(j).length();
//				if(fieldSetting.type == FieldSetting.Type.AChar){
//					//패턴길이가 데이터크기보다 크면 비교가 안되므로 무조건 매칭이 안됨.
//					if(endPatternLength > fieldByteSize){
//						throw new TooLongFilterPatternException("끝패턴 문자열이 필드길이보다 큽니다. 필드 = "+fieldSetting.name+", 패턴 = "+filter.endPattern(j)+", endPatternSize="+endPatternLength+", fieldSize="+fieldByteSize);
//					}
//				}else if(fieldSetting.type == FieldSetting.Type.UChar){
//					if(endPatternLength * 2 > fieldByteSize){
//						throw new TooLongFilterPatternException("끝패턴 문자열이 필드길이보다 큽니다. 필드 = "+fieldSetting.name+", 패턴 = "+filter.endPattern(j)+", endPatternSize="+(endPatternLength * 2)+", fieldSize="+fieldByteSize);
//					}
//				}
//			}
//		}
	}
	
	//나중에 대소문자구분 필터기능을 적용시 사용한다.
//	public boolean isTheCaseInsensitiveSame(char a, char b){
//		//모두 소문자로 바꾸어 연산을 한다.
//		if(a <= 'Z' && a >= 'A' ){
//			a += 32;
//		}
////		
//		if(b <= 'Z' && b >= 'A'){
//			b += 32;
//		}
//		
//		return a == b;
//	}
	
	/**
	 * 필터링에서 buffer의 데이터는 셋팅필드의 바이트길이로 계산되어 입력된다.
	 * 즉, 길이가 10인 셋팅필드에 실제데이터가 1바이트만 들어있든 5바이트만 들어있든 받는 쪽에서는 알수없으므로(나머지는 0으로 채워져있다.), 무조건 패턴길이만큼 비교를 해봐야 한다.   
	 * @param rankInfo
	 * @return true : 포함됨, false : 포함되지 않음.
	 */
	public abstract boolean filtering(RankInfo rankInfo, DataRef dataRef) throws IOException;
//		return filtering(rankInfo, dataRef, false);
//	}
	
//	public abstract boolean filtering(RankInfo rankInfo, DataRef dataRef, boolean skip) throws IOException;
		
	public int getFieldByteSize(){
		return fieldByteSize;
	}
	
	public BytesRef[] getPatternList(){
		return patternList;
	}
}
