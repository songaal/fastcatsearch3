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

package org.fastcatsearch.ir.group;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GroupingValue<T> implements Comparable<GroupingValue<T>> {
	protected static Logger logger = LoggerFactory.getLogger(GroupingValue.class);
	protected T value;
	
	public GroupingValue(){
		
	}
	public GroupingValue(T value){
		this.value = value;
	}
	
	public void set(T result) {
		this.value = result;
	}
	
	public T get(){
		return value;
	}
	
	public abstract boolean isEmpty();
	
	public abstract void add(T obj);
	
	public abstract void setIfMax(T obj);
	
	public abstract void increment();
	
	public void reset() {
		value = null;
	}

	public String toString(){
		if(value != null){
			return value.toString();
		}else{
			return "";
		}
	}
	
}
