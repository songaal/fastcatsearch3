///*
// * Copyright 2013 Websquared, Inc.
// * 
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * 
// *   http://www.apache.org/licenses/LICENSE-2.0
// * 
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.fastcatsearch.ir.index;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.List;
//
//import org.fastcatsearch.ir.common.IRFileName;
//import org.fastcatsearch.ir.config.ColumnSetting;
//import org.fastcatsearch.ir.config.Field;
//import org.fastcatsearch.ir.config.FieldSetting;
//import org.fastcatsearch.ir.config.IndexConfig;
//import org.fastcatsearch.ir.config.Schema;
//import org.fastcatsearch.ir.document.Document;
//import org.fastcatsearch.ir.io.BufferedFileOutput;
//import org.fastcatsearch.ir.io.FastByteBuffer;
//import org.fastcatsearch.ir.io.IOUtil;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
//
///**
// * 정렬과 그룹핑, 필터등의 작업이 아닌 단순히 컬럼데이터만을 필요로 할때 사용되는 필드.
// *  
// * @author sangwook.song
// *
// */
//public class ColumnFieldWriter {
//	private static Logger logger = LoggerFactory.getLogger(ColumnFieldWriter.class);
//	private BufferedFileOutput output;
//	private List<ColumnSetting> columnSettingList;
//	private FastByteBuffer buffer;
//	private int[] columnSizeList;
//	
//	public ColumnFieldWriter(Schema schema, File dir, IndexConfig indexConfig) throws IOException {
//		this(schema, dir, false, indexConfig);
//	}
//	public ColumnFieldWriter(Schema schema, File dir, boolean isAppend, IndexConfig indexConfig) throws IOException {
//		columnSettingList = schema.getColumnSettingList();
//		columnSizeList = new int[columnSettingList.size()];
//		
//		output = new BufferedFileOutput(dir, IRFileName.columnFieldFile, isAppend);
//		
//		for (int idx = 0; idx < columnSettingList.size(); idx++) {
//			ColumnSetting ss = columnSettingList.get(idx);
//			int columnSize = 0;
//			//write sort data only within dataSize
//			if(ss.field.type == FieldSetting.Type.AChar)
//				columnSize = ss.field.size;
//			else if(ss.field.type == FieldSetting.Type.UChar)
//				columnSize = ss.field.size * 2;
//			else if(ss.field.type == FieldSetting.Type.Int || ss.field.type == FieldSetting.Type.Float)
//				columnSize = IOUtil.SIZE_OF_INT;
//			else if(ss.field.type == FieldSetting.Type.Long || ss.field.type == FieldSetting.Type.Double)
//				columnSize = IOUtil.SIZE_OF_LONG;
//			
//			columnSizeList[idx] = columnSize;
//		}
//		
//		buffer = new FastByteBuffer(8 * 1024);
//		
//	}
//	
//	public void write(Document document) throws IOException{
//		
//		for (int idx = 0; idx < columnSettingList.size(); idx++) {
//			int k = columnSettingList.get(idx).field.sequence;
//			Field f = document.get(k);
//			
//			if(f == null){
//				for (int i = 0; i < columnSizeList[idx]; i++) {
//					output.writeByte(0);
//				}
//				continue;
//			}
//			
//			buffer.clear();
//			f.getFixedBytes(buffer);
//			int dataSize = columnSizeList[idx];
//			
//			if(buffer.limit() < dataSize){
//				output.writeBytes(buffer.array(), 0, buffer.limit());
//				for (int j = 0; j < dataSize - buffer.limit(); j++) {
//					output.writeByte(0);
//				}
//			}else{
//				output.writeBytes(buffer.array(), 0, dataSize);
//			}
//		}
////		logger.debug("InfoOutput.position = "+output.position());
//	}
//	
//	public void flush() throws IOException{
//		output.flush();
//	}
//	public void close() throws IOException{
//		output.close();
//	}
//}
