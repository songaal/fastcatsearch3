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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.field.FieldDataStringer;
import org.fastcatsearch.ir.group.function.CountGroupFunction;
import org.fastcatsearch.ir.group.function.RangeCountGroupFunction;
import org.fastcatsearch.ir.group.value.DoubleGroupingValue;
import org.fastcatsearch.ir.group.value.FloatGroupingValue;
import org.fastcatsearch.ir.group.value.IntGroupingValue;
import org.fastcatsearch.ir.group.value.LongGroupingValue;
import org.fastcatsearch.ir.io.DataRef;
import org.fastcatsearch.ir.io.NumberDataRef;
import org.fastcatsearch.ir.query.Group;
import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.search.FieldIndexReader;
import org.fastcatsearch.ir.search.FieldIndexesReader;
import org.fastcatsearch.ir.search.GroupIndexReader;
import org.fastcatsearch.ir.search.GroupIndexesReader;
import org.fastcatsearch.ir.search.IndexRef;
import org.fastcatsearch.ir.search.IndexRef.ReaderSequencePair;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.FieldSetting.Type;
import org.fastcatsearch.ir.settings.GroupIndexSetting;
import org.fastcatsearch.ir.settings.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class GroupDataGenerator {
	private static Logger logger = LoggerFactory.getLogger(GroupDataGenerator.class);
	
	private int[] indexSequence;
	private int groupFieldSize;
	private GroupFunction[][] groupFunctionList;
	private int totalSearchCount;
	private IndexRef<GroupIndexReader> indexRef;
	private IndexRef<FieldIndexReader> fieldIndexRef;
	private Set<Integer> redundancyCheck = new HashSet<Integer>();
	private IndexRef<FieldIndexReader>[] fieldIndexRefList;
	private Map<String, NumberDataRef>[] fieldBytesRefMap;
	private int[] groupKeySizeList;
	private String[] groupFieldIdList;
	private FieldSetting[] fieldSettingList;
	
	
	public GroupDataGenerator(List<Group> groupList, Schema schema, GroupIndexesReader groupIndexesReader, FieldIndexesReader fieldIndexesReader) throws IOException {
		
		int groupSize = groupList.size();
		
		this.indexSequence = new int[groupSize];
		groupFieldIdList = new String[groupSize];
		groupFunctionList = new GroupFunction[groupSize][];
		fieldIndexRefList = new IndexRef[groupSize];
		groupKeySizeList = new int[groupSize];
		
		List<String> fieldNameList = new ArrayList<String>(groupSize);
		for (int i = 0; i < groupSize; i++) {
			Group group = groupList.get(i);
			String fieldname = group.fieldname();
			if(!fieldNameList.contains(fieldname)){
				fieldNameList.add(fieldname);
			}
		}
		
		indexRef = groupIndexesReader.selectIndexRef(fieldNameList.toArray(new String[0]));
		
		for (int i = 0; i < groupSize; i++) {
			Group group = groupList.get(i);
			String fieldId = group.fieldname();
			int idx = schema.getGroupIndexSequence(fieldId);
			
			if(idx < 0){
				continue;
			}
			
			groupFieldIdList[groupFieldSize] = fieldId;
			fieldSettingList[groupFieldSize] = schema.fieldSettingMap().get(fieldId);
			
			GroupIndexReader groupIndexReader = groupIndexesReader.getIndexReader(i);
			
			//각 필드의 필드번호를 차례로 저장한다.
			indexSequence[groupFieldSize] = idx;
			
			int groupKeySize = groupIndexReader.getGroupKeySize(idx);
			groupKeySizeList[groupFieldSize] = groupKeySize;
			groupFunctionList[groupFieldSize] = group.function();
			
			//내부 grouping object 배열 초기화.
			List<String> paramFieldNameList = new ArrayList<String>(groupSize);
			for(GroupFunction groupFunction : groupFunctionList[groupFieldSize]){
				
				//범위 그룹핑은 COUNT와 동일하게 int형으로 생성. 
				if(groupFunction instanceof RangeCountGroupFunction){
					groupFunction.init(IntGroupingValue.createList(groupKeySize));
					continue;
				}
				
				
				if(groupFunction.fieldId != null){
					
					if(!paramFieldNameList.contains(groupFunction.fieldId)){
						paramFieldNameList.add(groupFunction.fieldId);
						
						//fieldId 타입에 따라서 value를 만들어준다.
						FieldSetting fieldSetting = schema.fieldSettingMap().get(groupFunction.fieldId);
						if(fieldSetting.getType() == FieldSetting.Type.INT){
							groupFunction.init(IntGroupingValue.createList(groupKeySize));
						}else if(fieldSetting.getType() == FieldSetting.Type.LONG){
							groupFunction.init(LongGroupingValue.createList(groupKeySize));
						}else if(fieldSetting.getType() == FieldSetting.Type.FLOAT){
							groupFunction.init(FloatGroupingValue.createList(groupKeySize));
						}else if(fieldSetting.getType() == FieldSetting.Type.DOUBLE){
							groupFunction.init(DoubleGroupingValue.createList(groupKeySize));
						}
						
					}
				}else{
					//COUNT일 것이므로, int로 만들어준다.
					groupFunction.init(IntGroupingValue.createList(groupKeySize));
				}
			}
			fieldIndexRefList[groupFieldSize] = fieldIndexesReader.selectIndexRef(paramFieldNameList.toArray(new String[0]));
			//동일한 필드가 파라미터로 여러번 들어올경우 한번만 읽기위해서는 동일한 bytesRef 참조를 가지고 있도록 한다. 
			fieldBytesRefMap[groupFieldSize] = new HashMap<String, NumberDataRef>(paramFieldNameList.size());
			int sequence = 0;
			for(String paramFieldId : paramFieldNameList){
				DataRef dataRef = fieldIndexRef.getDataRef(sequence++); 
				FieldSetting fieldSetting = schema.fieldSettingMap().get(paramFieldId);
				NumberDataRef numberDataRef = null;
				if(fieldSetting.getType() == FieldSetting.Type.INT){
					numberDataRef = dataRef.getNumberDataRef(Integer.class);
				}else if(fieldSetting.getType() == FieldSetting.Type.LONG){
					numberDataRef = dataRef.getNumberDataRef(Long.class);
				}else if(fieldSetting.getType() == FieldSetting.Type.FLOAT){
					numberDataRef = dataRef.getNumberDataRef(Float.class);
				}else if(fieldSetting.getType() == FieldSetting.Type.DOUBLE){
					numberDataRef = dataRef.getNumberDataRef(Double.class);
				}
				
				//차후 dataRef.next하면서 데이터를 읽는다. 
				fieldBytesRefMap[groupFieldSize].put(paramFieldId, numberDataRef);
			}
			
			groupFieldSize++;
		}
	}
	
	
	public void insert(RankInfo[] rankInfoList, int n) throws IOException{
		if(n == 0){
			return;
		}
		
		totalSearchCount += n;
		
		for (int k = 0; k < n; k++) {
			RankInfo ri = rankInfoList[k];
			int docNo = ri.docNo();
			indexRef.read(ri.docNo());
			
			for(int i = 0; i < groupFieldSize ;i++){
				fieldIndexRefList[i].read(docNo);
				
				redundancyCheck.clear();
				while(indexRef.getDataRef(i).next()){
					//multi-value는 여러번..
					BytesRef bytesRef = indexRef.getDataRef(i).bytesRef();
					int groupNo = bytesRef.toIntValue();
					for(GroupFunction groupFunction : groupFunctionList[i]){
						Number value = null;
						if(groupFunction.fieldId != null){
							NumberDataRef numberDataRef = fieldBytesRefMap[i].get(groupFunction.fieldId);
							while(numberDataRef.next()){
								value = numberDataRef.getNumber();
								groupFunction.addValue(groupNo, value);
							}
						}else{
							groupFunction.addValue(groupNo, value);
						}
					}
				}
			}
			
		}
		
	}
	
	//make an each group data
	public GroupData generate() throws IOException{
		List<GroupEntryList> result = new ArrayList<GroupEntryList>(groupFieldSize);
		
		BytesRef keyBuffer = null;
		
		for(int i = 0; i < groupFieldSize ;i++){
			
			String fieldId = groupFieldIdList[i];
			ReaderSequencePair<GroupIndexReader> readerSequencePair = indexRef.getReaderSequencePair(fieldId);
			GroupIndexReader groupIndexReader = readerSequencePair.reader();
			int sequence = readerSequencePair.sequence();
			
			Type fieldType = fieldSettingList[i].getType();
			//키는 어떠한 필드타입도 가능하다.
			GroupEntryList groupEntryList = new GroupEntryList();
			
			for(GroupFunction groupFunction : groupFunctionList[i]){
				if(groupFunction instanceof RangeCountGroupFunction){
					((RangeCountGroupFunction) groupFunction).done();
				}
			}
			
			//group function 갯수만큼 []를 만든다.
			int functionSize = groupFunctionList[i].length;
			for (int groupNo = 0; groupNo < groupKeySizeList[i]; groupNo++) {
				
				GroupingValue[] valueList = new GroupingValue[functionSize];
				
				String key = null;
				
				if(groupIndexReader.readKey(sequence, groupNo, keyBuffer)){
					key = FieldDataStringer.parse(fieldType, keyBuffer);
				}
				
				int j = 0;
				boolean hasValue = false;
				for(GroupFunction groupFunction : groupFunctionList[i]){
					GroupingValue groupingValue = groupFunction.value(groupNo);
					if(groupingValue != null){
						valueList[j++] = groupingValue; 
						hasValue = true;
					}
				}
				
				if(hasValue){
					groupEntryList.add(new GroupEntry(key, valueList));
				}
				
			}
			
			//sort by unit name, and make it easy while segment group freq merging.
			Collections.sort(groupEntryList.getEntryList(), GroupEntryList.KeyAscendingComparator);
			
			result.add(groupEntryList);
		}
		
		
		//total frequency is no need now, because when merging total freq will be counted again. 
		return new GroupData(result, totalSearchCount);
	}

}
