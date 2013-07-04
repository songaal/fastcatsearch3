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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class Tokenizer {
	protected static Logger logger = LoggerFactory.getLogger(Tokenizer.class);
	
	protected CharVector input;
	
	public static void register() { }
	
	public Tokenizer(){
		
	}
	public void setInput(char[] input) {
		if(input == null || input.length == 0){
			return;
		}
		
		this.input = new CharVector(input, 0, input.length).trim();
		
		init();
	}
	

	public void setInput(CharVector input) {
		this.input = (CharVector) input.trim().clone();
		init();
	}
	
	protected abstract void init();
	
	public abstract boolean nextToken(CharVector token);
	
	public abstract boolean nextToken(CharVector token, int[] seq);
		
	
}
