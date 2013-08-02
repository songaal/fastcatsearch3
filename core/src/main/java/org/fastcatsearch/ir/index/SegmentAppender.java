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
//import org.apache.commons.io.FileUtils;
//import org.fastcatsearch.ir.common.IRException;
//import org.fastcatsearch.ir.common.IndexFileNames;
//import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
//import org.fastcatsearch.ir.config.IndexConfig;
//import org.fastcatsearch.ir.document.Document;
//import org.fastcatsearch.ir.document.DocumentWriter;
//import org.fastcatsearch.ir.document.PrimaryKeyIndexesWriter;
//import org.fastcatsearch.ir.settings.Schema;
//import org.fastcatsearch.ir.util.Formatter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class SegmentAppender {
//	private static Logger logger = LoggerFactory.getLogger(SegmentAppender.class);
//	
//	private int lastDocNo;
//	private int count;
//	private boolean requestStop;
//	private long startTime;
//
//	private SegmentInfo segmentInfo;
//	
//	private DocumentWriter documentWriter;
//	private PrimaryKeyIndexesWriter primaryKeyIndexesWriter;
//	private SearchIndexesWriter searchIndexesWriter;
//	private FieldIndexesWriter fieldIndexesWriter;
//	private GroupIndexesWriter groupIndexesWriter;
//	
//	private String segmentId;
//	private File targetDir;
//	private int revision;
//	
//	public SegmentAppender(Schema schema, File targetDir, int baseDocNo, int revision, IndexConfig indexConfig) throws IRException{
//		
//		init(schema, targetDir, baseDocNo, revision, indexConfig);
//	}
//	
//	private void init(Schema schema, File targetDir, int baseDocNo, int revision, IndexConfig indexConfig) throws IRException{
//		try{
//			this.segmentId = targetDir.getName();
//			this.targetDir = targetDir;
//			this.revision = revision;
//			
//			IndexFileNames.getRevisionDir(targetDir, revision).mkdirs();
//			boolean isAppend = false;
//			if (revision > 0) {
//				isAppend = true;
//			}
//			
//			segmentInfo = new SegmentInfo(segmentId, baseDocNo);
//			
//			documentWriter = new DocumentWriter(schema, targetDir, revision, indexConfig);
//			searchIndexesWriter = new SearchIndexesWriter(schema, targetDir, revision, indexConfig);
//			fieldIndexesWriter = new FieldIndexesWriter(schema, targetDir, isAppend);
//			groupIndexesWriter = new GroupIndexesWriter(schema, targetDir, revision, indexConfig);
//		}catch(IOException e){
//			throw new IRException(e);
//		}
//		
//	}
//	
//	public int getDocumentCount(){
//		return count;
//	}
//	
//	public int addDocument(Document doc) throws IRException, IOException{
//		
////		t1 += (System.currentTimeMillis() - t);
////		logger.info("lastDocNo = {}", lastDocNo);
////		t = System.currentTimeMillis();
//		/*
//		 * searchWriter를 거치면서 doc의 영문자들이 모두 대문자로 변환된다.
//		 * 그 결과 filter, sort, group의 데이터는 모두 대문자로 색인된다.
//		 * 차후 대소문자 구별 검색, 필터등을 구현하려면 searchWriter의 해당부분을 수정해야한다. 
//		 * 
//		 * */
//		lastDocNo = documentWriter.write(doc);
//		searchIndexesWriter.write(doc, lastDocNo);
//		fieldIndexesWriter.write(doc);
//		groupIndexesWriter.write(doc);
//		count++;
////		logger.debug("addDocument {}, {}", count, doc);
//		return lastDocNo;
//	}
//	
//	
//	public SegmentInfo close() throws IOException, IRException{
//		boolean errorOccured = false;
//		try{
//			documentWriter.close();
//		}catch(Exception e){
//			logger.error("문서색인에러", e);
//			errorOccured = true;
//		}
//		try{
//			searchIndexesWriter.close();
//		}catch(Exception e){
//			logger.error("검색필드 색인에러", e);
//			errorOccured = true;
//		}
//		try{
//			fieldIndexesWriter.close();
//		}catch(Exception e){
//			logger.error("필드색인필드 색인에러", e);
//			errorOccured = true;
//		}
//		try{
//			groupIndexesWriter.close();
//		}catch(Exception e){
//			logger.error("그룹색인필드 색인에러", e);
//			errorOccured = true;
//		}
//		
//		if(count == 0 || errorOccured){
//			//delete new revisin dir
//			File revisionDir = IndexFileNames.getRevisionDir(targetDir, revision);
//			FileUtils.deleteDirectory(revisionDir);
//			return null;
//		}
//		
//		segmentInfo.updateRevision(revision, lastDocNo + 1, 0, 0, Formatter.formatDate());
//		
////		SegmentInfoWriter segmentInfoWriter = new SegmentInfoWriter(targetDir);
////		segmentInfoWriter.write(segmentInfo.getBaseDocNo(), (lastDocNo + 1), revision, System.currentTimeMillis());
////		segmentInfoWriter.close();
//		
//		//backup a original copy of current revision's segment.info
////		File f = new File(targetDir, IndexFileNames.segmentInfoFile);
////		File revisionDir = IndexFileNames.getRevisionDir(targetDir, revision);
//////		logger.debug("segminfo = "+f.getAbsolutePath());
////		FileUtils.copyFileToDirectory(f, revisionDir);
//		
//		logger.info("{} documents indexed, total = {} docs, elapsed = {}, mem = {}", 
//				new Object[]{count, lastDocNo + 1, Formatter.getFormatTime(System.currentTimeMillis() - startTime), Formatter.getFormatSize(Runtime.getRuntime().totalMemory())});
//		logger.info("doc base number = {}", segmentInfo.getBaseNumber());
//		
//		return segmentInfo;
//	}
//
//}
