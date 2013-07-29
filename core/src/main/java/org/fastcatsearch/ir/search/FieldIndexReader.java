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

package org.fastcatsearch.ir.search;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.io.DataRef;
import org.fastcatsearch.ir.io.StreamInputRef;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.FieldSetting;



public class FieldIndexReader extends ReferencableIndexReader {
	
//	public FieldIndexReader(IndexInput indexInput, IndexInput multiValueInput, IndexInput[] multiValueInputList, DataRef[] refs, int dataSize, int[] fieldOffset, int[] fieldByteSize
//			, boolean[] isMultiValue, boolean hasMultiValue, int fieldSize) {
//		this.indexInput = indexInput;
//		this.multiValueInput = multiValueInput;
////		this.multiValueInputList = multiValueInputList;
//		this.dataRef = dataRef;
//		this.dataSize = dataSize;
//		this.fieldOffset = fieldOffset;
//		this.fieldByteSize = fieldByteSize;
//		this.isMultiValue = isMultiValue;
//		this.hasMultiValue = hasMultiValue;
//	}
	
	public FieldIndexReader() { }
	
	
	public FieldIndexReader(FieldIndexSetting fieldIndexSetting, Map<String, FieldSetting> fieldSettingMap, File dir) throws IOException, IRException{
		String id = fieldIndexSetting.getId();
		FieldSetting refFieldSetting = fieldSettingMap.get(id);
		
		File dataFile = new File(dir, IndexFileNames.getSuffixFileName(IndexFileNames.fieldIndexFile, indexId));
		File multiValueFile = new File(dir, IndexFileNames.getMultiValueSuffixFileName(IndexFileNames.fieldIndexFile, indexId));
    	
		int dataSize = refFieldSetting.getByteSize();
		if(dataSize <= 0){
			//field셋팅의 길이가 가변이라면.
			if(fieldIndexSetting.getSize() > 0){
				dataSize = fieldIndexSetting.getSize();
			}else{
				//error 
				throw new IRException("필드색인은 고정길이필드이거나 field index size를 정해야 합니다.");
			}
		}
			
		init(indexId, refFieldSetting, dataFile, multiValueFile, dataSize);
		
	}
	

	@Override
	public FieldIndexReader clone(){
		FieldIndexReader reader = new FieldIndexReader();
		reader.dataInput = dataInput.clone();
		if(isMultiValue){
			reader.multiValueInput = multiValueInput.clone();
			reader.dataRef = new StreamInputRef(reader.multiValueInput, dataSize);
		}else{
			reader.dataRef = new DataRef(dataSize);
		}
		reader.dataSize = dataSize;
		reader.isMultiValue = isMultiValue;
		return reader;
	}
	
	
}
