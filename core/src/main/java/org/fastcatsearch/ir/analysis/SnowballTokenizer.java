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

package org.fastcatsearch.ir.analysis;

import org.fastcatsearch.ir.io.CharVector;
import org.tartarus.snowball.SnowballProgram;


public abstract class SnowballTokenizer extends Tokenizer{
	private static final char HANGUL_SYLLABLES_BEGIN = 0xAC00;
	private static final char HANGUL_SYLLABLES_END = 0xD7A3;
	
	private static final char HANGUL_COMPATIBILITY_JAMO_BEGIN = 0x3130;
	private static final char HANGUL_COMPATIBILITY_JAMO_END = 0x318E;
	
	protected SnowballProgram stemmer;
	
	
	private int pos;
	private int limit;
	private int start;
	private int type = -1;
	private int oldType = -3;
	
	public SnowballTokenizer(SnowballProgram stemmer){
		this.stemmer = stemmer;
	}
	
	@Override
	public boolean nextToken(CharVector token, int[] pos) {
		// TODO 어절 위치값이 필요함.
		pos[0] = 0;
		return nextToken(token);
	}
	
	public boolean nextToken(CharVector token) {
		int len = 0;
		start = pos;
		while(pos < limit){
			char ch = input.array[pos];
			
			if(Character.isDigit(ch)){
		    	type = 0;
			}else if(Character.isWhitespace(ch)){
		    	type = -2;
			}else if( (ch >= HANGUL_SYLLABLES_BEGIN && ch <= HANGUL_SYLLABLES_END)
				|| (ch >= HANGUL_COMPATIBILITY_JAMO_BEGIN && ch <= HANGUL_COMPATIBILITY_JAMO_END)){
				//2012-01-03 sang 한글은 따로 처리한다.
				type = 2;
		    }else if(Character.isLetter(ch)){
		    	type = 1;
		    }else{
		    	type = -1;
		    }
//			System.out.println("oldType="+oldType+", type = "+type+" ch="+ch);
			
			if(oldType >= 0 && oldType != type){
				if (oldType == 1) {
					
	    			String current1 = new String(input.array, start, len);
	    			stemmer.setCurrent(current1);
	    			stemmer.stem();
		    		String current = stemmer.getCurrent();
		    		token.init(current.toCharArray(), 0, current.length());
		    		token.toUpperCase();
		    		start = pos;
//		    		System.out.println("oldType = "+oldType+", current="+current+" <= "+current1);
		    		oldType = type;
		    		return true;
		    	}else{
		    		token.init(input.array, start, len);
		    		token.toUpperCase();
//		    		System.out.println("영문이외 oldType = "+oldType+", token="+token);
		    		oldType = type;
		    		return true;	
		    	}
	    	}
			
	    	if(type >= 0){
	    		input.array[pos] = Character.toLowerCase(ch);
	    		//2012-01-03 sang 결과는 대문자로 ...
		    	len++;
//		    	System.out.println("set char = "+ch+", "+pos+", len="+len);
	    	}else{
	    		start++;
	    	}
	    	
	    	pos++;
	    	oldType = type;
			
		}
		
		if (len > 0) {
			String current1 = new String(input.array, start, len);
			stemmer.setCurrent(current1);
			stemmer.stem();
    		String current = stemmer.getCurrent();
    		token.init(current.toCharArray(), 0, current.length());
    		token.toUpperCase();
//    		pos++;
    		start = pos;
    		return true;
    	}
		
		
		
		return false;
		
	}

	protected void init() {
		pos = input.start;
		start = pos;
		limit = pos + input.length;
	}

}
