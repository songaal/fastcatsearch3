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

public class View {
	private String fieldId;
	private int summarySize; //요약길이
	private int fragments; //요약 블럭 갯수
	private boolean highlight; //일치단어 하이라이팅 여부.
	
	public View(String fieldId){
		this(fieldId, 0, 0, false);
	}
	public View(String fieldId, int summary){
		this(fieldId, summary, 0, false);
	}
	public View(String fieldId, int summary, int fragments, boolean highlight){
		this.fieldId = fieldId;
		this.summarySize = summary;
		this.fragments = fragments;
		this.highlight = highlight;
	}
	public String fieldId(){
		return fieldId;
	}
	public int summarySize(){
		return summarySize;
	}
	public int fragments() {
		return fragments;
	}
	public boolean highlight(){
		return highlight;
	}
	public String toString(){
		return fieldId+":"+summarySize+":"+highlight;
	}
}