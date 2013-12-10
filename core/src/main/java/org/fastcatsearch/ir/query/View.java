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
	private int snippetSize; //요약길이
	private int fragmentSize; //요약 블럭 갯수
	private boolean summarized; //요약사용여부
	private boolean highlighted; //일치단어 하이라이팅 여부.
	
	public View(String fieldId){
		this(fieldId, 0, 0, false, false);
	}
	public View(String fieldId, int snippetSize){
		this(fieldId, snippetSize, 1, false, false);
	}
	public View(String fieldId, int snippetSize, int fragmentSize, 
			boolean summarized, boolean highlighted){
		this.fieldId = fieldId.toUpperCase();
		this.snippetSize = snippetSize;
		this.fragmentSize = fragmentSize;
		this.summarized = summarized;
		this.highlighted = highlighted;
	}
	public String fieldId(){
		return fieldId;
	}
	public int snippetSize(){
		return snippetSize;
	}
	public void setSnippetSize(int snippetSize) {
		this.snippetSize = snippetSize;
	}
	public int fragmentSize() {
		return fragmentSize;
	}
	public void setFragmentSize(int fragmentSize) {
		this.fragmentSize = fragmentSize;
	}
	public boolean isSummarized() {
		return summarized;
	}
	public void setSummarized(boolean summarized) {
		this.summarized = summarized;
	}
	public boolean isHighlighted(){
		return highlighted;
	}
	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
	}
	public String toString(){
		return fieldId+":"+snippetSize+":"+fragmentSize+":"+summarized+":"+highlighted;
	}
}
