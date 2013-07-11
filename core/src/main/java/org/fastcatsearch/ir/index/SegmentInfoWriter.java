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
//
//import org.fastcatsearch.ir.common.IndexFileNames;
//import org.fastcatsearch.ir.io.BufferedFileOutput;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
//public class SegmentInfoWriter {
//	private static Logger logger = LoggerFactory.getLogger(SegmentInfoWriter.class);
//	private BufferedFileOutput segmentInfoOutput;
//	private File segmentDir;
//	
//	public SegmentInfoWriter(File segmentDir) throws IOException {
//		this.segmentDir = segmentDir;
//		segmentInfoOutput = new BufferedFileOutput(segmentDir, IndexFileNames.segmentInfoFile);
//	}
//	
//	public void close() throws IOException{
//		segmentInfoOutput.close();
//	}
//
//	//write all data to recover current state.
//	public void write(int baseDocNo, int docCount, int revision, long currentTimeMillis) throws IOException {
//		segmentInfoOutput.writeInt(baseDocNo);
//		segmentInfoOutput.writeInt(docCount);
//		segmentInfoOutput.writeInt(revision);
//		segmentInfoOutput.writeLong(currentTimeMillis);
//		logger.debug("segment write baseDoc={}, count={}, revision={}, doc={}, store={}, group = {}"
//				, baseDocNo, docCount, revision
//				, new File(segmentDir, IndexFileNames.docPosition).length()
//				, new File(segmentDir, IndexFileNames.docStored).length()
//				, new File(segmentDir, IndexFileNames.groupDataFile).length());
//		segmentInfoOutput.writeLong(new File(segmentDir, IndexFileNames.docPosition).length());
//		segmentInfoOutput.writeLong(new File(segmentDir, IndexFileNames.docStored).length()); //need to modify doc count, when rollback
//		segmentInfoOutput.writeLong(new File(segmentDir, IndexFileNames.groupDataFile).length());
//		//group.key
//		int cnt = 0;
//		for (;;) {
//			File f0 = new File(segmentDir, IndexFileNames.getSuffixFileName(IndexFileNames.groupKeyFile, Integer.toString(cnt)));
//			if(!f0.exists()){
//				break;
//			}else{
//				cnt++;
//			}
//		}
//		segmentInfoOutput.writeInt(cnt);
//		for (int i = 0; i < cnt; i++) {
//			segmentInfoOutput.writeLong(new File(segmentDir, IndexFileNames.getSuffixFileName(IndexFileNames.groupKeyFile, Integer.toString(i))).length());
//		}
//		
//		//필드색인파일.
//		segmentInfoOutput.writeLong(new File(segmentDir, IndexFileNames.fieldIndexFile).length());
//	}
//	
//}
