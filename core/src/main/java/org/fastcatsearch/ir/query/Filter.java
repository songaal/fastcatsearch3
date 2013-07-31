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

package org.fastcatsearch.ir.query;

import org.fastcatsearch.ir.filter.FilterException;
import org.fastcatsearch.ir.filter.FilterFunction;
import org.fastcatsearch.ir.filter.MatchFilter;
import org.fastcatsearch.ir.filter.NotSupportedFilterFunctionException;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.FieldSetting;

public class Filter {
	
	//filter function for bitwise calculation
	public static final int MATCH = 1 << 0;			//0x0001
	public static final int SECTION = 1 << 1;		//0x0002 pattern is compared as a string. Always inclusive
	public static final int PREFIX = 1 << 2;		//0x0004
	public static final int SUFFIX = 1 << 3;		//0x0008
	public static final int MATCH_BOOST = 1 << 4;	//0x0010
	public static final int SECTION_BOOST = 1 << 5;	//0x0020 pattern is compared as a string. Always inclusive
	public static final int PREFIX_BOOST = 1 << 6;	//0x0040
	public static final int SUFFIX_BOOST = 1 << 7;	//0x0080
	public static final int EXCLUDE = 1 << 8;		//0x0100
	public static final int EXCLUDE_BOOST = 1 << 9;
	
	private String fieldIndexId;
	private int function;
	private String[] patternList;
	private String[] endPatternList;
	private int boostScore;
	
	public Filter(String fieldIndexId, int function, String pattern){
		this.fieldIndexId = fieldIndexId;
		this.function = function;
		//2012-04-19 송상욱
		//필터데이터는 대소문자가 구분되어 저장되어 있으므로, pattern도 강제 대소문자 변환을 하지 않는다.
		//case-insensitive 필터링을 하고 싶으면 filtering메소드 내에서 수행한다.
		
		//2012-10-22 swsong
		//필터데이터는 영문자가 대문자로 변환되어 색인되는 형태로 바뀜. searchFieldWriter에서 Document 필드데이터를 변경.
		//그러므로 여기서도 모두 대문자로 변환한다.
//		this.patternList = new String[]{pattern};
		this.patternList = new String[]{pattern.toUpperCase()};
	}
	public Filter(String fieldIndexId, int function, String pattern, String endPattern){
		this(fieldIndexId, function, pattern);
//		this.endPatternList = new String[]{endPattern};
		this.endPatternList = new String[]{endPattern.toUpperCase()};
	}
	public Filter(String fieldIndexId, int function, String pattern, int boostScore){
		this(fieldIndexId, function, pattern);
		this.boostScore = boostScore;
	}
	public Filter(String fieldIndexId, int function, String pattern, String endPattern, int boostScore){
		this(fieldIndexId, function, pattern, endPattern);
		this.boostScore = boostScore;
	}
	
	//
	//LIST
	//
	public Filter(String fieldIndexId, int function, String[] patternList){
		this.fieldIndexId = fieldIndexId;
		this.function = function;
		this.patternList = patternList;
	}
	public Filter(String fieldIndexId, int function, String[] patternList, String[] endPatternList){
		this(fieldIndexId, function, patternList);
		this.endPatternList = endPatternList;
	}
	public Filter(String fieldIndexId, int function, String[] patternList, int boostScore){
		this(fieldIndexId, function, patternList);
		this.boostScore = boostScore;
	}
	public Filter(String fieldIndexId, int function, String[] patternList, String[] endPatternList, int boostScore){
		this(fieldIndexId, function, patternList, endPatternList);
		this.boostScore = boostScore;
	}
	
	public FilterFunction createFilterFunction(FieldIndexSetting fieldIndexSetting, FieldSetting fieldSetting) throws NotSupportedFilterFunctionException, FilterException{
		switch(function){
			case MATCH:
				return new MatchFilter(this, fieldIndexSetting, fieldSetting);
//			case SECTION:
//				return new SectionFilter(this, fieldSetting);
////				throw new NotSupportedFilterFunctionException(fieldSetting.type+" type section filter is not supported!");
//			case PREFIX:
//				return new PrefixFilter(this, fieldSetting); //char필드만 지원.
//			case SUFFIX:
//				return new SuffixFilter(this, fieldSetting); //char필드만 지원.
//			case EXCLUDE:
//				return new ExcludeFilter(this, fieldSetting);
			case MATCH_BOOST:
				return new MatchFilter(this, fieldIndexSetting, fieldSetting, true);
//			case SECTION_BOOST:
//				return new SectionFilter(this, fieldSetting, true);
//			case PREFIX_BOOST:
//				return new PrefixFilter(this, fieldSetting, true);
//			case SUFFIX_BOOST:
//				return new SuffixFilter(this, fieldSetting, true);
//			case EXCLUDE_BOOST:
//				return new ExcludeFilter(this, fieldSetting, true);
			
		}
		throw new NotSupportedFilterFunctionException("지원하지 않는 필터기능입니다. num="+function);
	}
	
	public String toString(){
		String str = fieldIndexId+":"+function+":";
		if(endPatternList != null){
			for(int i=0; i<patternList.length; i++){
				str += (patternList[i] + "~" + endPatternList[i]);
				if(i < patternList.length - 1)
					str += ";";
			}
		}else{
			for(int i=0; i<patternList.length; i++){
				str += patternList[i];
				if(i < patternList.length - 1)
					str += ";";
			}
		}
		return str+":"+boostScore;
	}
	
	public String fieldIndexId(){
		return fieldIndexId;
	}
	
	public int function(){
		return function;
	}
	
	public String pattern(){
		return patternList[0];
	}
	
	public String pattern(int n){
		return patternList[n];
	}
	
	public int patternLength(){
		return patternList.length;
	}
	
	public boolean isEndPatternExist(){
		return endPatternList != null;
	}
	
	public String endPattern(int n){
		return endPatternList[n];
	}
	
	public int endPatternLength(){
		return endPatternList.length;
	}
	
	public int boostScore(){
		return boostScore;
	}
}
