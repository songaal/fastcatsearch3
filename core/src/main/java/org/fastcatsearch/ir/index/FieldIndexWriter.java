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

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexFileNames;
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
//	private List<RefSetting> refSettingList;
//	private int[] fieldSequenceList;
	private boolean isMultiValue;
//	private int fieldSize;
//	private int[] fieldIndexSizeList;
	private int limitSize;
	private int fieldSequence;
	
	public FieldIndexWriter(FieldIndexSetting fieldIndexSetting, Map<String, FieldSetting> fieldSettingMap, Map<String, Integer> fieldSequenceMap, File dir) throws IOException, IRException {
		this(fieldIndexSetting, fieldSettingMap, fieldSequenceMap, dir, false);
	}
	
	public FieldIndexWriter(FieldIndexSetting fieldIndexSetting, Map<String, FieldSetting> fieldSettingMap, Map<String, Integer> fieldSequenceMap, File dir, boolean isAppend) throws IOException, IRException {
//		refSettingList = fieldIndexSetting.getRef();
//		fieldSize = refSettingList.size();
//		fieldSequenceList = new int[fieldSize];
//		fieldIndexSizeList = new int[fieldSize];
		String id = fieldIndexSetting.getId();
		output = new BufferedFileOutput(dir, IndexFileNames.getSuffixFileName(IndexFileNames.fieldIndexFile, id), isAppend);
		
//		for (int idx = 0; idx < fieldSize; idx++) {
//			RefSetting rs = refSettingList.get(idx);
			
		String fieldId = fieldIndexSetting.getRef();
		fieldSequence = fieldSequenceMap.get(fieldId);
		FieldSetting refFieldSetting = fieldSettingMap.get(fieldId);
		limitSize = fieldIndexSetting.getSize();
		
		isMultiValue = refFieldSetting.isMultiValue();
		if(isMultiValue){
			multiValueOutput = new BufferedFileOutput(dir, IndexFileNames.getMultiValueSuffixFileName(IndexFileNames.fieldIndexFile, id), isAppend);
		}
			
//			fieldIndexSizeList[idx] = fieldIndexSize;
//			if(fieldSetting.isMultiValue()){
//				hasMultiValue = true;
//			}
//			fieldSequenceList[idx] = fieldSequenceMap.get(fieldId);
//		}
		
		
		
	}
	
	public void write(Document document) throws IOException, IRException{
		
//		for (int idx = 0; idx < fieldSize; idx++) {
//			int k = fieldSequenceList[idx];
			Field f = document.get(fieldSequence);
			
			if(f.isMultiValue()){
				long ptr = multiValueOutput.position();
				output.writeLong(ptr);
				if(f.isFixedSize()){
					f.writeFixedDataTo(multiValueOutput);
				}else{
					//정해진 길이가 있다면 해당 길이로 자른다.
//					int limitSize = fieldIndexSizeList[idx];
					Field tmpField = f.clone();
					if(limitSize > 0){
						tmpField.setSize(limitSize);
						tmpField.writeFixedDataTo(multiValueOutput);
					}else{
						throw new IRException("가변길이필드는 필드색인이 불가능합니다. 필드색인SIZE 필요. field-index-size = "+limitSize);
					}
				}
			}else{
				if(f.isFixedSize()){
					f.writeFixedDataTo(output);
				}else{
					//정해진 길이가 있다면 해당 길이로 자른다.
//					int limitSize = fieldIndexSizeList[idx];
					Field tmpField = f.clone();
					if(limitSize > 0){
						tmpField.setSize(limitSize);
						tmpField.writeFixedDataTo(output);
					}else{
						throw new IRException("가변길이필드는 필드색인이 불가능합니다. 필드색인SIZE 필요. field-index-size = "+limitSize);
					}
				}
			}
//		}
	}
	
	public void flush() throws IOException{
		output.flush();
		
		if(isMultiValue){
			multiValueOutput.flush();
		}
	}
	public void close() throws IOException{
		output.close();
		
		if(isMultiValue){
			multiValueOutput.close();
		}
	}
}
