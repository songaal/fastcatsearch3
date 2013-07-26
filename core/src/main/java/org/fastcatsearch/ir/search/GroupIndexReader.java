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
import java.util.List;
import java.util.Map;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.io.DataRef;
import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.io.FixedDataInput;
import org.fastcatsearch.ir.io.IOUtil;
import org.fastcatsearch.ir.io.Input;
import org.fastcatsearch.ir.io.SequencialDataInput;
import org.fastcatsearch.ir.io.IndexInput;
import org.fastcatsearch.ir.io.StreamInputRef;
import org.fastcatsearch.ir.io.VariableDataInput;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.GroupIndexSetting;
import org.fastcatsearch.ir.settings.RefSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * 
 * 
 * @author sangwook.song
 *
 */
public class GroupIndexReader extends ReferencableIndexReader {
	
	private IndexInput groupDataInput;
	private SequencialDataInput[] groupKeyInputList;
	private IndexInput multiValueInput;
	private IndexInput[] multiValueInputList;
	private DataRef[] refs;
	private int fieldSize;
	
	protected int dataSize;//색인된 한 문서의 필드데이터의  길이
	protected int[] fieldOffset;
	protected int[] groupKeySize;
	protected boolean[] isMultiValue;
	protected boolean hasMultiValue;
	
	private GroupIndexReader() {}
	
	public GroupIndexReader(GroupIndexSetting groupIndexSetting, Map<String, FieldSetting> fieldSettingMap, File dir, int revision) throws IOException, IRException{
		String id = groupIndexSetting.getId();
		List<RefSetting> refSettingList = groupIndexSetting.getRefList();
		fieldSize = refSettingList.size();
		
		IndexInput groupInfoInput = new BufferedFileInput(IndexFileNames.getRevisionDir(dir, revision), IndexFileNames.groupInfoFile);
    	
		fieldOffset = new int[fieldSize];
		groupKeySize = new int[fieldSize];
		isMultiValue = new boolean[fieldSize];
		//TODO 이게 모지?
    	int fieldCount = groupInfoInput.readInt();
    	
    	for (int idx = 0; idx < fieldSize; idx++) {
			groupKeySize[idx] = groupInfoInput.readInt();
    		long dataBasePosition = groupInfoInput.readLong();
    		long indexBasePosition = groupInfoInput.readLong();
    	}
    	groupInfoInput.close();
    	
    	groupDataInput = new BufferedFileInput(dir, IndexFileNames.getSuffixFileName(IndexFileNames.groupDataFile, id));
    	groupKeyInputList = new SequencialDataInput[fieldSize];
    	
    	//멀티밸류 필드가 존재하는지 확인.
    	for (int idx = 0; idx < fieldSize; idx++) {
    		RefSetting refSetting = refSettingList.get(idx);
			FieldSetting fieldSetting = fieldSettingMap.get(refSetting.getRef());
			if(fieldSetting.isMultiValue()){
				hasMultiValue = true;
				break;
			}
    	}
    	if(hasMultiValue){
    		multiValueInput = new BufferedFileInput(dir, IndexFileNames.getMultiValueSuffixFileName(IndexFileNames.groupDataFile, id));
    		multiValueInputList = new IndexInput[fieldSize];
    	}
   
    	refs = new DataRef[fieldSize];
    	int offset = 0;
    	for (int idx = 0; idx < fieldSize; idx++) {
    		fieldOffset[idx] = dataSize;
    		RefSetting refSetting = refSettingList.get(idx);
			FieldSetting fieldSetting = fieldSettingMap.get(refSetting.getRef());
			
			fieldOffset[idx] = offset;
			
			if(fieldSetting.isMultiValue()){
				isMultiValue[idx] = true;
				//멀티밸류의 경우는 input을 여러개 clone해서 사용해야함.
				//read시 데이터를 읽어서 리턴하는 것이아니라, stream을 전달하기때문에, 동일 input사용시 여러필드에서 position을 각자 움직이면 문제가 발생한다. 
				multiValueInputList[idx] = multiValueInput.clone();
				refs[idx] = new StreamInputRef(multiValueInputList[idx], IOUtil.SIZE_OF_INT); //멀티밸류도 data는 int이다.
				offset += IOUtil.SIZE_OF_LONG;
			}else{
				//싱글밸류의 경우 select시 데이터를 바로 읽어서 byteref를 리터하는게 맞다. 다시 여럭ref에서 read하려면 포지션이문제가된다.
				refs[idx] = new DataRef(IOUtil.SIZE_OF_INT);
				offset += IOUtil.SIZE_OF_INT;
			}
			dataSize = offset;
			
    		if(fieldSetting.isVariableField()){
    			groupKeyInputList[idx] = new VariableDataInput(dir, IndexFileNames.getSuffixFileName(IndexFileNames.groupKeyFile, id, Integer.toString(idx)));
    		}else{
    			int dataSize = fieldSetting.getByteSize();
    			groupKeyInputList[idx] = new FixedDataInput(dir, IndexFileNames.getSuffixFileName(IndexFileNames.groupKeyFile, id, Integer.toString(idx)), dataSize);
    		}
    	}
    	
    	
	}
	
	@Override
	public int getRefCount(){
		return fieldSize;
	}
	@Override
	public DataRef getRef(int sequence) throws IOException{
		return refs[sequence];
	}
	
	@Override
	public DataRef[] getRef() throws IOException{
		return refs;
	}
	
	@Override
	public void read(int docNo) throws IOException{
		for (int sequence = 0; sequence < fieldSize; sequence++) {
			read(docNo, sequence);
		}
	}
	
	@Override
	public void read(int docNo, int sequence) throws IOException{
		int offset = fieldOffset[sequence];
		int pos = dataSize * docNo + offset;
		groupDataInput.seek(pos);
		logger.debug("group data read docNo[{}], dataSize[{}], offset[{}] = pos[{}]", docNo, dataSize, offset, pos);
		if(isMultiValue[sequence]){
			long ptr = groupDataInput.readLong();
			if(ptr != -1){
				multiValueInputList[sequence].seek(ptr);
				int count = multiValueInputList[sequence].readVInt();
				refs[sequence].reset(count);
			}
		}else{
			//이미 input의 position을 움직여 놓았으므로 더이상 아무것도 하지 않는다.
			groupDataInput.readBytes(refs[sequence].bytesRef().bytes, 0, IOUtil.SIZE_OF_INT);
			logger.debug("fill group data to {} as {}", refs[sequence], IOUtil.readInt(refs[sequence].bytesRef().bytes, 0));
			refs[sequence].reset(1); //sigle value는 한개 읽음으로 표시.
		}
	}
	
	//특정 그룹필드의 키값을 읽어온다.
	public boolean readKey(int fieldSequence, int groupNo, BytesRef bytesRef) throws IOException{
		return groupKeyInputList[fieldSequence].read(bytesRef, groupNo);
	}
	
	//각 그룹의 key 갯수
	public int getGroupKeySize(int fieldSequence) {
		return groupKeySize[fieldSequence];
	}
	
	@Override
	public GroupIndexReader clone(){
		DataRef[] refs2 = new DataRef[fieldSize];
		IndexInput[] multiValueInputList2 = null;
		if(hasMultiValue){
			multiValueInputList2 = new IndexInput[fieldSize];
		}
		
		for (int i = 0; i < fieldSize; i++) {
			if(isMultiValue[i]){
				multiValueInputList2[i] = multiValueInput.clone();
				refs2[i] = new StreamInputRef(multiValueInputList2[i], IOUtil.SIZE_OF_INT);
			}else{
				refs2[i] = new DataRef(IOUtil.SIZE_OF_INT);
			}
		}
		
		SequencialDataInput[] newGroupKeyInputList = new  SequencialDataInput[groupKeyInputList.length];
		for (int i=0; i<  groupKeyInputList.length; i++) {
			newGroupKeyInputList[i] = groupKeyInputList[i].clone();
		}
		GroupIndexReader reader = new GroupIndexReader();
		reader.groupDataInput = groupDataInput.clone();
		reader.groupKeyInputList = newGroupKeyInputList;
		if(hasMultiValue){
			reader.multiValueInput = multiValueInput.clone();
		}
		reader.multiValueInputList = multiValueInputList2;
		reader.refs = refs2;
		reader.fieldSize = fieldSize;
		reader.dataSize = dataSize;
		reader.fieldOffset = fieldOffset;
		reader.groupKeySize = groupKeySize;
		reader.isMultiValue = isMultiValue;
		reader.hasMultiValue = hasMultiValue;
		return reader;
	}
	
	@Override
	public void close() throws IOException{
		groupDataInput.close();
		for (int i = 0; i < groupKeyInputList.length; i++) {
			groupKeyInputList[i].close();
		}
		if(hasMultiValue){
			multiValueInput.close();
			for(IndexInput indexInput : multiValueInputList){
				//multi-value가 아닌 필드는 input객체가 null이므로 건너뛴다.
				if(indexInput != null){
					indexInput.close();
				}
			}
		}
	}

	
	
}





