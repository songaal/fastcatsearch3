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

import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.io.DataRef;
import org.fastcatsearch.ir.io.IOUtil;
import org.fastcatsearch.ir.io.Input;
import org.fastcatsearch.ir.io.IndexInput;
import org.fastcatsearch.ir.io.StreamInputRef;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.RefSetting;



public class FieldIndexReader extends ReferencableIndexReader {
	protected int dataSize;
	protected int[] fieldOffset;
	protected int[] fieldByteSize;
	protected boolean[] isMultiValue;
	protected boolean hasMultiValue;
	
	private IndexInput indexInput;
	private IndexInput multiValueInput;
	private IndexInput[] multiValueInputList;
	
	private int fieldSize;
	private DataRef[] refs;
	
	
	public FieldIndexReader(IndexInput indexInput, IndexInput multiValueInput, IndexInput[] multiValueInputList, DataRef[] refs, int dataSize, int[] fieldOffset, int[] fieldByteSize
			, boolean[] isMultiValue, boolean hasMultiValue, int fieldSize) {
		this.indexInput = indexInput;
		this.multiValueInput = multiValueInput;
		this.multiValueInputList = multiValueInputList;
		this.refs = refs;
		this.dataSize = dataSize;
		this.fieldOffset = fieldOffset;
		this.fieldByteSize = fieldByteSize;
		this.isMultiValue = isMultiValue;
		this.hasMultiValue = hasMultiValue;
		this.fieldSize = fieldSize;
	}
	
	public FieldIndexReader(FieldIndexSetting fieldIndexSetting, Map<String, FieldSetting> fieldSettingMap, File dir) throws IOException{
		String id = fieldIndexSetting.getId();
		List<RefSetting> refList = fieldIndexSetting.getRefList();
		fieldSize = refList.size();
		fieldOffset = new int[fieldSize];
    	fieldByteSize = new int[fieldSize];
    	isMultiValue = new boolean[fieldSize];
    	refs = new DataRef[fieldSize];
    	
    	//multi-value가 존재하는지 찾아본다.
    	for (int i = 0; i < fieldSize; i++) {
			RefSetting rs = refList.get(i);
			FieldSetting fieldSetting = fieldSettingMap.get(rs.getRef());
			if(fieldSetting.isMultiValue()){
				hasMultiValue = true;
				break;
			}
    	}
    	
    	indexInput = new BufferedFileInput(dir, IndexFileNames.getSuffixFileName(IndexFileNames.fieldIndexFile, id));
    	if(hasMultiValue){
    		multiValueInput = new BufferedFileInput(dir, IndexFileNames.getMultiValueSuffixFileName(IndexFileNames.fieldIndexFile, id));
    	}
    	
    	//multivalue용도..
    	multiValueInputList = new IndexInput[fieldSize];
    	
    	int offset = 0;
		for (int i = 0; i < fieldSize; i++) {
			RefSetting rs = refList.get(i);
			FieldSetting fieldSetting = fieldSettingMap.get(rs.getRef());
			
			fieldByteSize[i] = fieldSetting.getByteSize();
			fieldOffset[i] = offset;
			if(fieldSetting.isMultiValue()){
				//멀티밸류의 경우는 input을 여러개 clone해서 사용해야함.
				//read시 데이터를 읽어서 리턴하는 것이아니라, stream을 전달하기때문에, 동일 input사용시 여러필드에서 position을 각자 움직이면 문제가 발생한다. 
				multiValueInputList[i] = multiValueInput.clone();
				
				refs[i] = new StreamInputRef(multiValueInputList[i], fieldByteSize[i]);
				isMultiValue[i] = true;
				offset += IOUtil.SIZE_OF_LONG;
			}else{
				//싱글밸류의 경우 select시 데이터를 바로 읽어서 byteref를 리터하는게 맞다. 다시 여럭ref에서 read하려면 포지션이문제가된다.
				refs[i] = new DataRef(fieldByteSize[i]);
				offset += fieldByteSize[i];
			}
			
		}
		dataSize = offset;
		
		logger.debug("Field Index Width = {}", dataSize);
	}
	
	@Override
	public int getRefCount(){
		return fieldSize;
	}
	
	@Override
	public void close() throws IOException{
		indexInput.close();
		if(hasMultiValue){
			multiValueInput.close();
			for (int i = 0; i < multiValueInputList.length; i++) {
				if(multiValueInputList[i] != null){
					multiValueInputList[i].close();
				}
			}
		}
	}
	
	@Override
	public FieldIndexReader clone(){
		DataRef[] refs2 = new DataRef[fieldSize];
		IndexInput[] multiValueInputList2 = null;
		if(hasMultiValue){
			multiValueInputList2 = new IndexInput[fieldSize];
		}
		
		for (int i = 0; i < fieldSize; i++) {
			if(isMultiValue[i]){
				multiValueInputList2[i] = multiValueInput.clone();
				refs2[i] = new StreamInputRef(multiValueInputList2[i], fieldByteSize[i]);
			}else{
				refs2[i] = new DataRef(fieldByteSize[i]);
			}
		}
		
		FieldIndexReader reader = new FieldIndexReader(indexInput.clone(), multiValueInput.clone(), multiValueInputList2, refs2
				, dataSize, fieldOffset, fieldByteSize, isMultiValue, hasMultiValue, fieldSize);
		return reader;
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
		int length = fieldByteSize[sequence];
		int pos = dataSize * docNo + offset;
		indexInput.seek(pos);
		
		if(isMultiValue[sequence]){
			long ptr = indexInput.readLong();
			if(ptr != -1){
				multiValueInputList[sequence].seek(ptr);
				int count = multiValueInputList[sequence].readVInt();
				refs[sequence].reset(count);
			}
		}else{
			//이미 input의 position을 움직여 놓았으므로 더이상 아무것도 하지 않는다.
			indexInput.readBytes(refs[sequence].bytesRef().bytes, 0, length);
		}
	}
	
	
	
//	public void main(String[] args) {
//		
//		//멀티-싱글밸류 복합인덱스.
//		for (int i = 0; i < args.length; i++) {
//			int docNo = i;
//			BytesRefEnum[] enumList = readData(docNo);
//			//multivalue의 경우 BytesRefEnum가 data길이를 가지고 있어야한다.
//			
//			//싱글밸류를 읽는다.
//			BytesRef data0 = enumList[0].read();
//			
//			int mvCount = enumList[1].size();
//			for (int j = 0; j < mvCount; j++) {
//				//멀티밸류를 읽는다.
//				BytesRef data1 = enumList[1].read();
//			}
//		}
//		
//	}
	
	//멀티밸류.
//	void a(){
//		int fieldCount = getFieldCount();
//		BytesRefEnumReader r = getReader(0);
//		BytesRefEnumReader r1 = getReader(1);
//		for (int docNo = 0; docNo < 10000; docNo++) {
//			BytesRefEnum e = r.read(docNo);
//			//멀티밸류 필드라면 여러번..
//			e.next();
//			BytesRef data = e.bytesRef();
//			e.next();
//		
//		}
//	}
	

	
//	public int readBulk2(RankInfo[] rankInfoList, int n, byte[] fieldData, int[] fieldNumList) throws IOException{
//		for (int i = 0; i < fieldCount; i++) {
////			int fieldNum = fieldNumList[i];
//			int pos = dataSize * docNo;// + fieldOffset[fieldNum];
////			int len = fieldByteSize[fieldNum];
//			indexInput.position(pos);
//			
//			if(isMultiValue[fieldNum]){
//				int count = indexInput.readShort();
//				offset += IOUtil.writeShort(fieldData, offset, count);
//				offset += indexInput.readBytes(fieldData, offset, len);
//				long ptr = indexInput.readLong();
//				if(ptr != -1){
//					valueInput.position(ptr);
//					offset += valueInput.readBytes(fieldData, offset, len * (count - 1));
//				}
//			}else{
//				//single value field
//				offset += indexInput.readBytes(fieldData, offset, len);
//			}
//		}
//		
//		
//		
//	}
//	public int readBulk(RankInfo[] rankInfoList, int n, byte[] fieldData, int[] fieldNumList) throws IOException{
//		
//		int offset = 0;
//		for (int k = 0; k < n; k++) {
//			RankInfo ri = rankInfoList[k];
//			int docNo = ri.docNo();
//			
//			
//			for (int i = 0; i < fieldNumList.length; i++) {
//				int fieldNum = fieldNumList[i];
//				long pos = (long)dataSize * (long)docNo + (long)fieldOffset[fieldNum];
//				int len = fieldByteSize[fieldNum];
//				if(logger.isTraceEnabled()){
//					logger.trace("docNo={}, fieldNum={}, fieldOffset={}, dataSize={}, pos={}, len={}"
//							, new Object[]{docNo, fieldNum, fieldOffset[fieldNum], dataSize, pos, len});
//				}
//				indexInput.position(pos);
//				
//				if(isMultiValue[fieldNum]){
//					int count = indexInput.readShort();
//					offset += IOUtil.writeShort(fieldData, offset, count);
//					offset += indexInput.readBytes(fieldData, offset, len);
//					long ptr = indexInput.readLong();
//					if(ptr != -1){
//						valueInput.position(ptr);
//						offset += valueInput.readBytes(fieldData, offset, len * (count - 1));
//					}
//				}else{
//					//single value field
//					offset += indexInput.readBytes(fieldData, offset, len);
//				}
//			}
//			
//				
//			
//		}
//		return offset;
//		
//	}
}
