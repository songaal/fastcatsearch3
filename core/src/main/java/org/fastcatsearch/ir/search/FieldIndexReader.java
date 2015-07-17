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



public class FieldIndexReader extends ReferenceableIndexReader {
	
	public FieldIndexReader() { }
	
	
	public FieldIndexReader(FieldIndexSetting fieldIndexSetting, Map<String, FieldSetting> fieldSettingMap, File dir) throws IOException, IRException{
		String id = fieldIndexSetting.getId();
		String refId = fieldIndexSetting.getRef();
		FieldSetting refFieldSetting = fieldSettingMap.get(refId);
		
		File dataFile = new File(dir, IndexFileNames.getFieldIndexFileName(id));
		File multiValueFile = new File(dir, IndexFileNames.getMultiValueFileName(IndexFileNames.getFieldIndexFileName(id)));
    	
		int indexDataSize = fieldIndexSetting.getSize();
		int dataSize = refFieldSetting.getByteSize(indexDataSize);
		if(dataSize <= 0){
			throw new IRException("필드색인은 고정길이필드이거나 field index size를 정해야 합니다.");
		}
			
		init(id, refFieldSetting, dataFile, multiValueFile, dataSize);
		
	}
	

	@Override
	public FieldIndexReader clone(){
		FieldIndexReader reader = new FieldIndexReader();
		reader.indexId = indexId;
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
