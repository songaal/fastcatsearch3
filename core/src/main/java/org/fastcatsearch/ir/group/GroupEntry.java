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

/*
 * 그룹핑 결과중 한줄 엔트리.
 * group function이 여러개일수 있으므로 value는 []가 된다.
 * **/
public class GroupEntry {
	
	public String key;
	protected GroupingValue[] groupingValue;
	
	public String toString(){
		String valueString = "";
		if(groupingValue != null){
			for (GroupingValue value : groupingValue) {
                valueString += ((value != null ? value.toString() : value) + ",");
            }
		}
		return "[GroupEntry]"+key + " : " + valueString;
	}
	public GroupEntry(String key) {
		this(key, null);
	}
	public GroupEntry(String key, GroupingValue... groupingValue) {
		this.key = key;
		this.groupingValue = groupingValue;
	}

	public int size(){
		if(groupingValue == null){
			return 0;
		}
		return groupingValue.length;
	}
	public GroupingValue[] groupingValues(){
		return groupingValue;
	}
	
	public GroupingValue groupingValue(int i){
		return groupingValue[i];
	}
	
	public int functionSize(){
        if(groupingValue == null){
            return 0;
        }
        return groupingValue.length;
	}
	
	public String getGroupingObjectResultString(int i){
		return groupingValue[i].toString();
	}
	
	public void merge(GroupEntry entry) {
		for (int i = 0; i < entry.functionSize(); i++) {
            if(groupingValue[i] != null) {
                if(entry.groupingValue(i) != null) {
                    groupingValue[i].mergeValue(entry.groupingValue(i).get());
                }
                //우측이 null이면 스킵.
            } else {
                if(entry.groupingValue(i) != null) {
                    groupingValue[i] = entry.groupingValue(i);
                }
                //둘다 null이면 스킵.
            }
		}
		
	}
	
}
