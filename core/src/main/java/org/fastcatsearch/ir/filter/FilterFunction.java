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
import org.fastcatsearch.ir.field.FieldDataParseException;
import org.fastcatsearch.ir.io.BytesDataOutput;
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
				BytesDataOutput arrayOutput = new BytesDataOutput(patternByteSize);
				f.writeFixedDataTo(arrayOutput);
				patternList[j] = arrayOutput.bytesRef();
				
				if(filter.isEndPatternExist()){
					f = fieldSetting.createPatternField(filter.endPattern(j));
					patternByteSize = fieldSetting.getByteSize();
					arrayOutput = new BytesDataOutput(patternByteSize);
					f.writeFixedDataTo(arrayOutput);
					endPatternList[j] = arrayOutput.bytesRef();
				}
			}
		} catch (IOException e) {
			throw new FilterException("필터패턴생성중 에러", e);
		} catch (FieldDataParseException e) {
			throw new FilterException("필터패턴을 파싱할 수 없습니다.", e);
		}


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
