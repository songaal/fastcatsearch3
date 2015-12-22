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
import org.fastcatsearch.ir.group.value.DoubleGroupingValue;
import org.fastcatsearch.ir.group.value.FloatGroupingValue;
import org.fastcatsearch.ir.group.value.IntGroupingValue;
import org.fastcatsearch.ir.group.value.LongGroupingValue;
import org.fastcatsearch.ir.group.value.StringGroupingValue;
import org.fastcatsearch.ir.io.DataRef;
import org.fastcatsearch.ir.query.Group;
import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.search.FieldIndexReader;
import org.fastcatsearch.ir.search.FieldIndexesReader;
import org.fastcatsearch.ir.search.GroupIndexReader;
import org.fastcatsearch.ir.search.GroupIndexesReader;
import org.fastcatsearch.ir.search.IndexRef;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.FieldSetting.Type;
import org.fastcatsearch.ir.settings.GroupIndexSetting;
import org.fastcatsearch.ir.settings.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class GroupDataGenerator {
	private static Logger logger = LoggerFactory.getLogger(GroupDataGenerator.class);
	
//	private int[] indexSequence;
	private int groupSize;
	private GroupFunction[][] groupFunctionList;
	private int totalSearchCount;
	private IndexRef<GroupIndexReader> indexRef;
	private Set<Integer> redundancyCheck;
	private IndexRef<FieldIndexReader>[] fieldIndexRefList;
	private Map<String, DataRef>[] fieldBytesRefMap;
	private int[] groupKeySizeList; //그룹별 키의 총 갯수.
//	private String[] groupFieldIdList;
	private FieldSetting[] fieldSettingList;
	
	
	public GroupDataGenerator(List<Group> groupList, Schema schema, GroupIndexesReader groupIndexesReader, FieldIndexesReader fieldIndexesReader) throws IOException {
		
		this.groupSize = groupList.size();
		
		fieldSettingList = new FieldSetting[groupSize];
		groupFunctionList = new GroupFunction[groupSize][];
		fieldIndexRefList = new IndexRef[groupSize];
		fieldBytesRefMap = new Map[groupSize];
		groupKeySizeList = new int[groupSize];
		redundancyCheck = new HashSet<Integer>();
		
		List<String> indexIdList = new ArrayList<String>(groupSize);
		for (int i = 0; i < groupSize; i++) {
			Group group = groupList.get(i);
			String indexId = group.groupIndexId();
			indexIdList.add(indexId);
			logger.debug(">> group index id >> {}", indexId);
		}
		
		indexRef = groupIndexesReader.selectIndexRef(indexIdList.toArray(new String[0]));
//		logger.debug("group indexref size = {}", indexRef.getSize());
		
		for (int i = 0; i < groupSize; i++) {
			Group group = groupList.get(i);
			String groupIndexId = group.groupIndexId();
			int idx = schema.getGroupIndexSequence(groupIndexId);
			
			if(idx < 0){
				continue;
			}
			
			GroupIndexReader groupIndexReader = indexRef.getReader(i);
			int groupKeySize = groupIndexReader.getGroupKeySize();
			groupKeySizeList[i] = groupKeySize;
			logger.debug("group#{} [{}] groupKeySize[{}]", i, groupIndexId, groupKeySize);
			
			GroupIndexSetting groupIndexSetting = schema.getGroupIndexSetting(groupIndexId);
			String refId = groupIndexSetting.getRef();
			fieldSettingList[i] = schema.fieldSettingMap().get(refId);
			
			groupFunctionList[i] = group.function();
			int functionSize = groupFunctionList[i].length;
			//내부 grouping object 배열 초기화.
			//function별로 결과 객체를 만들어준다.
			//count는 key갯수만큼 생성후 검색결과로 존재하는 키에 대해서만 갯수를 증가시켜주고,
			//range도 count와 동일하게 생성후 done을 통해 범위결과로 재구성한다.
			List<String> paramFieldNameList = new ArrayList<String>(groupSize);
			for (int j = 0; j < functionSize; j++) {
				GroupFunction groupFunction = groupFunctionList[i][j];
				if(groupFunction == null){
					//그룹기능이름이 잘못되어서 null로 들어올수 있다. 
					continue;
				}
				
				if(groupFunction.getType() == GroupFunctionType.COUNT){
					//int로 만들어준다.
					groupFunction.init(IntGroupingValue.createList(groupKeySize, groupFunction.getType()));
//					logger.debug("groupFunction #{} valuelist = {}", i, groupFunction.valueList);
//				}else if(groupFunction instanceof RangeCountGroupFunction){
//					//범위 그룹핑은 COUNT와 동일하게 int형으로 생성.
//					groupFunction.init(IntGroupingValue.createList(groupKeySize));
				}else{
					//
					// sum, min, max 필드에 대한 그룹핑. 연산대상 fieldId가 필요하다.
					//
					if(groupFunction.getFieldId() != null){
						
						//동일한 필드를 여러번 function 수행할때는 함께 사용한다
						
						if(!paramFieldNameList.contains(groupFunction.getFieldId())) {
                            paramFieldNameList.add(groupFunction.getFieldId());
                        }
                        //fieldId 타입에 따라서 value를 만들어준다.
                        FieldSetting fieldSetting = schema.fieldSettingMap().get(groupFunction.getFieldId());
                        if(fieldSetting.getType() == FieldSetting.Type.INT){
                            groupFunction.init(IntGroupingValue.createList(groupKeySize, groupFunction.getType()));
                        }else if(fieldSetting.getType() == FieldSetting.Type.LONG){
                            groupFunction.init(LongGroupingValue.createList(groupKeySize, groupFunction.getType()));
                        }else if(fieldSetting.getType() == FieldSetting.Type.FLOAT){
                            groupFunction.init(FloatGroupingValue.createList(groupKeySize, groupFunction.getType()));
                        }else if(fieldSetting.getType() == FieldSetting.Type.DOUBLE){
                            groupFunction.init(DoubleGroupingValue.createList(groupKeySize, groupFunction.getType()));
                        }else{
                            groupFunction.init(StringGroupingValue.createList(groupKeySize, groupFunction.getType()));
                        }

					}
				}
			}
			
			if(paramFieldNameList.size() > 0){
				fieldIndexRefList[i] = fieldIndexesReader.selectIndexRef(paramFieldNameList.toArray(new String[0]));
				fieldBytesRefMap[i] = new HashMap<String, DataRef>(paramFieldNameList.size());
			
				//동일한 필드가 파라미터로 여러번 들어올경우 한번만 읽기위해서는 동일한 bytesRef 참조를 가지고 있도록 한다. 
				int k = 0;
				
				for(String paramFieldId : paramFieldNameList){
					//stream input ref일수도 있다.
					DataRef dataRef = fieldIndexRefList[i].getDataRef(k++); 
					FieldSetting fieldSetting = schema.fieldSettingMap().get(paramFieldId);
					dataRef.setType(fieldSetting.getType());
					//차후 dataRef.next하면서 데이터를 읽는다.
					fieldBytesRefMap[i].put(paramFieldId, dataRef);
				}
			}
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
			
			for(int i = 0; i < groupSize ;i++){
				if(fieldIndexRefList[i] != null){
					fieldIndexRefList[i].read(docNo);
				}
				redundancyCheck.clear();
				while(indexRef.getDataRef(i).next()){
					//multi-value는 여러번..
					BytesRef bytesRef = indexRef.getDataRef(i).bytesRef();
					int groupNo = bytesRef.toIntValue();
					for(GroupFunction groupFunction : groupFunctionList[i]){
						if(groupFunction == null){
							continue;
						}
						Object value = null;
						if(groupFunction.getFieldId() != null){
							DataRef dataRef = fieldBytesRefMap[i].get(groupFunction.getFieldId());
							dataRef.reset();
							while(dataRef.next()){
								value = dataRef.getValue();
								if(value!=null && value instanceof String) {
									String strValue = (String)value;
									//trim nil character
									int inx = strValue.indexOf('\0');
									if(inx != -1) {
										value = strValue.substring(0, inx);
									}
								}
								groupFunction.addValue(groupNo, value);
							}
						}else{
//							logger.debug("doc {} add group value groupNo={} val={}", docNo, groupNo, value);
							groupFunction.addValue(groupNo, value);
						}
					}
				}
			}
			
		}
		
	}
	
	//make an each group data
	public GroupsData generate() throws IOException{
		List<GroupEntryList> result = new ArrayList<GroupEntryList>(groupSize);
		
		BytesRef keyBuffer = new BytesRef();
		
		for (int i = 0; i < groupSize; i++) {
			
			if(groupKeySizeList[i] == 0){
				//키가 없는 필드는 돌지 않는다. 빈 결과 추가.
				result.add(new GroupEntryList());
				continue;
			}
			
			GroupIndexReader groupIndexReader = indexRef.getReader(i);

			// 키는 어떠한 필드타입도 가능하다.
			GroupEntryList groupEntryList = new GroupEntryList();

			//범위 그룹핑은 후처리를 수행한다.
			for (GroupFunction groupFunction : groupFunctionList[i]) {
				groupFunction.done();
			}
			
			Type fieldType = fieldSettingList[i].getType();
			//group function 갯수만큼 []를 만든다.
			int functionSize = groupFunctionList[i].length;
			//모든 키에대해 검사를 수행한다.
			for (int groupNo = 0; groupNo < groupKeySizeList[i]; groupNo++) {
				
				GroupingValue[] valueList = new GroupingValue[functionSize];
				
				int j = 0;
				boolean hasValue = false;
				for(GroupFunction groupFunction : groupFunctionList[i]){
					if(groupFunction == null){
						continue;
					}
					GroupingValue groupingValue = groupFunction.value(groupNo);
					//null이거나 비어있지 않으면 추가.
					if(groupingValue != null && !groupingValue.isEmpty()){
						valueList[j++] = groupingValue; 
						hasValue = true;
					} else {
						valueList[j++] = null;
					}
				}

				//각 키별 결과를 추가해준다.
				//해당키에 결과가 없으면 추가하지 않으므로, 값이 있는 키만 그룹핑 결과로 나오게 된다.
				if(hasValue){
					String key = null;
					
					if(groupIndexReader.readKey(groupNo, keyBuffer)){
						key = FieldDataStringer.parse(fieldType, keyBuffer);
					}
//					logger.debug("groupEntryList.add {}, {}, {}", groupNo, key, valueList);
					groupEntryList.add(new GroupEntry(key, valueList));
				}
				
			}
			
			//sort by unit name, and make it easy while segment group freq merging.
			if(groupEntryList.size() > 0){
				Collections.sort(groupEntryList.getEntryList(), GroupEntryList.KeyAscendingComparator);
			}
			
			result.add(groupEntryList);
		}
		
		
		//total frequency is no need now, because when merging total freq will be counted again. 
		return new GroupsData(result, totalSearchCount);
	}

}
