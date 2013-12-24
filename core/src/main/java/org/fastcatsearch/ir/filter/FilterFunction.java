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
import org.fastcatsearch.ir.settings.FieldIndexSetting;
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
	public FilterFunction(Filter filter, FieldIndexSetting fieldIndexSetting, FieldSetting fieldSetting, boolean isBoostFunction) throws FilterException{
		this.fieldSetting = fieldSetting;
		this.isBoostFunction = isBoostFunction;
		patternCount = filter.patternLength();
		patternList = new BytesRef[patternCount];
		endPatternList = new BytesRef[patternCount];
		boostScore = filter.boostScore();
		boolean isIgnoreCase = fieldIndexSetting.isIgnoreCase();
		
		try {
			for (int j = 0; j < patternCount; j++) {
				//패턴의 byte 데이터를 얻기위해 필드객체를 생성한다.
				//패턴과 필드데이터를 같은 길이의 byte[]로 만들어놓고 비교를 한다.
				String pattern = filter.pattern(j);
				logger.debug("Filter Pattern {} : {} isIgnoreCase={}", fieldIndexSetting.getId(), pattern, isIgnoreCase);
				
				//ignoreCase로 색인되어있다면 패턴도 대문자로 변환한다.
				if(isIgnoreCase){
					pattern = pattern.toUpperCase();
				}
				Field f = fieldSetting.createPatternField(pattern);
				int patternByteSize = fieldSetting.getByteSize();
				BytesDataOutput arrayOutput = new BytesDataOutput(patternByteSize);
				f.writeFixedDataTo(arrayOutput);
				patternList[j] = arrayOutput.bytesRef();
				logger.debug("Filter Pattern>>> {} > {}", pattern, patternList[j]);
				
				if(filter.isEndPatternExist()){
					pattern = filter.endPattern(j);
					if(isIgnoreCase){
						pattern = pattern.toUpperCase();
					}
					f = fieldSetting.createPatternField(pattern);
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
	
	/**
	 * 필터링에서 buffer의 데이터는 셋팅필드의 바이트길이로 계산되어 입력된다.
	 * 즉, 길이가 10인 셋팅필드에 실제데이터가 1바이트만 들어있든 5바이트만 들어있든 받는 쪽에서는 알수없으므로(나머지는 0으로 채워져있다.), 무조건 패턴길이만큼 비교를 해봐야 한다.   
	 * @param rankInfo
	 * @return true : 포함됨, false : 포함되지 않음.
	 */
	public abstract boolean filtering(RankInfo rankInfo, DataRef dataRef) throws IOException;
	
	public BytesRef[] getPatternList(){
		return patternList;
	}
}
