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

public abstract class GroupingValue<T extends Comparable> implements Comparable<GroupingValue<T>> {
	protected static Logger logger = LoggerFactory.getLogger(GroupingValue.class);
	protected T value;
    protected GroupFunctionType type;
	protected boolean isSet;

	public GroupingValue(){
		
	}
    public GroupingValue(GroupFunctionType type){
        this.type = type;
    }
	public GroupingValue(T value, GroupFunctionType type){
		this.value = value;
        this.type = type;
	}
	
	public void set(T result) {
		this.value = result;
	}
	
	public T get(){
		return value;
	}
	
	public abstract boolean isEmpty();
	
	public abstract void add(T obj);
	
	public abstract void increment();
	
	public void reset() {
		value = null;
	}

    public GroupFunctionType getType() {
        return type;
    }

    public void setType(GroupFunctionType type) {
        this.type = type;
    }

    //한 세그먼트내에서 그룹결과를 새로 만들때.
    public void addValue(Object v) {
        if (type == GroupFunctionType.COUNT) {
            increment();
        } else {
            mergeValue(v);
        }
    }
    //여러 세그먼트로 부터 만들어진 그룹결과를 합칠때
    public void mergeValue(Object v) {
        //COUNT, SUM, MIN, MAX, FIRST, LAST
        if(value == null) {
            value = (T) v;
        } else {
            if (type == GroupFunctionType.COUNT) {
                add((T) v);
            } else if (type == GroupFunctionType.SUM) {
                add((T) v);
            } else if (type == GroupFunctionType.MIN) {
                if (value.compareTo(v) > 0) {
                    value = (T) v;
                }
            } else if (type == GroupFunctionType.MAX) {
                if (value.compareTo(v) < 0) {
                    value = (T) v;
                }
            } else if (type == GroupFunctionType.FIRST) {
                // 이미 value가 저 위에서 셋팅되었음.
                // do nothing.
            } else if (type == GroupFunctionType.LAST) {
                value = (T) v;
            }
        }
    }
    public String toString(){
		if(value != null){
			return value.toString();
		}else{
			return "";
		}
	}
	
}
