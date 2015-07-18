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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.filter.FilterException;
import org.fastcatsearch.ir.filter.FilterFunction;
import org.fastcatsearch.ir.io.DataRef;
import org.fastcatsearch.ir.search.FieldIndexReader;
import org.fastcatsearch.ir.search.FieldIndexesReader;
import org.fastcatsearch.ir.search.IndexRef;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * match, section, suffix, prefix 네가지 기능을 제공하며,
 * 숫자형 필드는 suffix, prefix 매치를 제공하지 않는다.
 * */
public class HitFilter {
	
	private static Logger logger = LoggerFactory.getLogger(HitFilter.class);
	
	private Filter[] filterList;
	private FilterFunction[] filterFunctions;
	
	private IndexRef<FieldIndexReader> fieldIndexRef;
	private List<DataRef> dataRefList;
	
	
	public HitFilter(List<Filter> filterList, Schema schema, FieldIndexesReader fieldIndexesReader, int bulkSize) throws IOException, IRException {
		int size = filterList.size();
		this.filterList = new Filter[size];
		filterFunctions = new FilterFunction[size];
		
		Object[] fieldList = new Object[size];
		int k = 0;
		for (Filter filter : filterList) {
			fieldList[k++] = filter.fieldIndexId();
		}
		//각 필드에대한 indexreader를 clone해서 dataRef와 연결시킨다.
		fieldIndexRef = fieldIndexesReader.selectIndexRef(fieldList);
		
		dataRefList = fieldIndexRef.getDataRefList();
		
		for (int i = 0; i < size; i++) {
			Filter filter = filterList.get(i);
			this.filterList[i] = filter;
			Object fieldIndexIdObject = filter.fieldIndexId();
            FieldIndexSetting fieldIndexSetting = null;
            if(fieldIndexIdObject instanceof String) {
                fieldIndexSetting = schema.getFieldIndexSetting((String)fieldIndexIdObject);
                if(fieldIndexSetting == null){
                    //잘못된 필드명.
                    throw new IRException("\"" + fieldIndexIdObject + "\" is not a field index or not indexed.");
                }

                String fieldId = fieldIndexSetting.getRef();
                FieldSetting fieldSetting = schema.getFieldSetting(fieldId);
                filterFunctions[i] = filter.createFilterFunction(fieldIndexSetting, fieldSetting);
            } else if(fieldIndexIdObject instanceof String[]) {
                for(String fieldIndexId : (String[]) fieldIndexIdObject) {
                    fieldIndexSetting = schema.getFieldIndexSetting(fieldIndexId);
                    if(fieldIndexSetting == null){
                        //잘못된 필드명.
                        throw new IRException("\"" + fieldIndexId + "\" is not a field index or not indexed.");
                    }
                }

                filterFunctions[i] = filter.createFilterFunction(null, null);
            }

			logger.debug("FilterFunction[{}] > {}", i, filterFunctions[i]);
		}
	}

	private void checkFieldIndexId(Schema schema, String fieldIndexId) throws IRException {
        FieldIndexSetting fieldIndexSetting = schema.getFieldIndexSetting(fieldIndexId);
        if(fieldIndexSetting == null){
            //잘못된 필드명.
            throw new IRException("\"" + fieldIndexId + "\" is not a field index or not indexed.");
        }
    }
	public int filtering(RankInfo[] rankInfoList, int nread) throws FilterException, IOException {
		if(nread <= 0){
			return 0;
		}
		
		int count = 0;
		for (int k = 0; k < nread; k++) {
			
			RankInfo rankInfo = rankInfoList[k];
			
			//내부적으로 reader들과 ref를 연결하여 읽어들일수 있도록 한다.
			//검색할 모든 필드에 대해서 read를 수행한다. 
			fieldIndexRef.read(rankInfo.docNo());
			
			boolean isInclude = true;
			
			for (int i = 0; i < filterFunctions.length; i++) {
				DataRef dataRef = dataRefList.get(i);
				//이미 제외된 거라면 더이상 확인하지 않는다.
				if(filterFunctions[i].filtering(rankInfo, dataRef)){
					//부합한다면 다음조건으로 계속진행한다. 
					//필터조건끼리는 AND관계이므로 모든 조건이 부합할때까지는 아직 break하면 안된다.
				}else{
					//부합하지 않는다면 끝낸다.
					isInclude = false;
					break;
				}
				//else 포함된 경우는 남아있는 필터에서 제외될수도 있으므로 끝까지 확인해본다. 
			}

			//엔트리를 추가해준다.
			if(isInclude){
				rankInfoList[count++] = rankInfo;
			}
		}
		
		return count;
	}
}
