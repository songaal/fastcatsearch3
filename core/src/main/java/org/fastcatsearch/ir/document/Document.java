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

package org.fastcatsearch.ir.document;

import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.ir.field.Field;



public class Document {
	
	private List<Field> fields;
	private float score; //after search, this has cetain value.
	
	public Document(int size){
		fields = new ArrayList<Field>(size);
	}

	public int size(){
		return fields.size();
	}
	
	public void add(Field f){
		fields.add(f);
	}
	
	public void set(int index, Field f){
		fields.add(index, f);
	}
	
	public Field get(int index){
		return fields.get(index);
	}
	
	public void setScore(float score){
		this.score = score;
	}
	
	public float getScore(){
		return score;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[Document]");
		for (int index = 0; index < fields.size(); index++) {
			sb.append("\nF_").append(index).append("=").append(fields.get(index).toString());
		}
		return sb.toString();
	}
}
