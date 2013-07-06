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

package org.fastcatsearch.ir.index;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.ir.common.IRFileName;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.io.BufferedFileOutput;
import org.fastcatsearch.ir.io.IndexOutput;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.RefSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * 단일 필드인덱스에 대한 색인클래스. 하위에 여러필드를 가질수 있다.
 * @author sangwook.song
 *
 */
public class FieldIndexWriter {
	private static Logger logger = LoggerFactory.getLogger(FieldIndexWriter.class);
	private IndexOutput output;
	private IndexOutput multiValueOutput;
	private List<RefSetting> refSettingList;
	private int[] fieldSequenceList;
	private boolean hasMultiValue;
	private int fieldSize;
	
	public FieldIndexWriter(FieldIndexSetting fieldIndexSetting, Map<String, FieldSetting> fieldSettingMap, Map<String, Integer> fieldSequenceMap, File dir) throws IOException {
		this(fieldIndexSetting, fieldSettingMap, fieldSequenceMap, dir, false);
	}
	
	public FieldIndexWriter(FieldIndexSetting fieldIndexSetting, Map<String, FieldSetting> fieldSettingMap, Map<String, Integer> fieldSequenceMap, File dir, boolean isAppend) throws IOException {
		refSettingList = fieldIndexSetting.getRefList();
		fieldSize = refSettingList.size();
		fieldSequenceList = new int[fieldSize];
		String id = fieldIndexSetting.getId();
		output = new BufferedFileOutput(dir, IRFileName.getSuffixFileName(IRFileName.fieldIndexFile, id), isAppend);
		
		for (int idx = 0; idx < fieldSize; idx++) {
			RefSetting rs = refSettingList.get(idx);
			
			String fieldId = rs.getRef();
			FieldSetting fieldSetting = fieldSettingMap.get(fieldId);
			if(fieldSetting.isMultiValue()){
				hasMultiValue = true;
			}
			fieldSequenceList[idx] = fieldSequenceMap.get(fieldId);
		}
		if(hasMultiValue){
			multiValueOutput = new BufferedFileOutput(dir, IRFileName.getMultiValueSuffixFileName(IRFileName.fieldIndexFile, id), isAppend);
		}
	}
	
	public void write(Document document) throws IOException{
		
		for (int idx = 0; idx < fieldSize; idx++) {
			int k = fieldSequenceList[idx];
			Field f = document.get(k);
			
			if(f.isMultiValue()){
				long ptr = multiValueOutput.position();
				output.writeLong(ptr);
				f.writeFixedDataTo(multiValueOutput);
			}else{
				f.writeFixedDataTo(output);	
			}
		}
	}
	
	public void flush() throws IOException{
		output.flush();
		
		if(hasMultiValue){
			multiValueOutput.flush();
		}
	}
	public void close() throws IOException{
		output.close();
		
		if(hasMultiValue){
			multiValueOutput.close();
		}
	}
}
