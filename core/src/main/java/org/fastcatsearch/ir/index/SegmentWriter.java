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
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.document.DocumentWriter;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Formatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class SegmentWriter {
	
	private static Logger logger = LoggerFactory.getLogger(SegmentWriter.class);
	
	private int baseDocNo;
	private DocumentWriter documentWriter;
	private SearchIndexesWriter searchIndexesWriter;
	private FieldIndexesWriter fieldIndexesWriter;
	private GroupIndexesWriter groupIndexesWriter;
	private int lastDocNo;
	private boolean requestStop;
	private long startTime;
	private File targetDir;
	private final int REVISION = 0;
	
	//usually this constructor is used, except for certain segment rebuild
	//for Full indexing
	public SegmentWriter(Schema schema, File targetDir, IndexConfig indexConfig) throws IRException{
		this(schema, targetDir, 0, 0, indexConfig);
	}
	
	//for Add indexing
	public SegmentWriter(Schema schema, File targetDir, int docBaseNo, int revision, IndexConfig indexConfig) throws IRException{
		init(schema, targetDir, docBaseNo, revision, indexConfig);
	}
	public void init(Schema schema, File targetDir, int docBaseNo, int revision, IndexConfig indexConfig) throws IRException{
		try{
			this.targetDir = targetDir;
			this.baseDocNo = docBaseNo;
			//make a default 0 revision directory
			IndexFileNames.getRevisionDir(targetDir, REVISION).mkdirs();
			boolean isAppend = false;
			if (revision > 0){
				isAppend = true;
			}
			documentWriter = new DocumentWriter(schema, targetDir, revision, indexConfig);
			searchIndexesWriter = new SearchIndexesWriter(schema, targetDir, revision, indexConfig);
			fieldIndexesWriter = new FieldIndexesWriter(schema, targetDir, isAppend);
			groupIndexesWriter = new GroupIndexesWriter(schema, targetDir, revision, indexConfig);
			//TODO writer생성시 에러가 발생하면(ex 토크나이저 발견못함) writer들이 안 닫힌채로 색인이 끝나서 다음번 색인시 파일들을 삭제못하는 에러발생.
			//계속 에러가 발생하므로 매우 치명적인 버그이다.
			//조속한 수정이 필요함.
		}catch(IOException e){
			throw new IRException(e);
		}
		
	}
	public int getDocumentCount(){
		return lastDocNo;
	}
	public int addDocument(Document doc) throws IRException, IOException{
		logger.debug("doc >> {}", doc);
		int docNo = documentWriter.write(doc);
		//ensure lastDocNo == docNo
		if(lastDocNo != docNo)
			throw new IRException("Doc number is strange. docNo="+docNo+", lastDocNo="+lastDocNo);
//		t1 += (System.currentTimeMillis() - t);
//		logger.info("lastDocNo = {}", lastDocNo);
//		t = System.currentTimeMillis();
		/*
		 * searchWriter를 거치면서 doc의 영문자들이 모두 대문자로 변환된다.
		 * 그 결과 filter, sort, group의 데이터는 모두 대문자로 색인된다.
		 * 차후 대소문자 구별 검색, 필터등을 구현하려면 searchWriter의 해당부분을 수정해야한다. 
		 * 
		 * */
		searchIndexesWriter.write(doc);
		fieldIndexesWriter.write(doc);
		groupIndexesWriter.write(doc);
		return lastDocNo++;
	}
	
//	@Deprecated
//	public int indexDocument() throws IRException{
//		try{
////			long t, t1 = 0, t2 = 0, t3 = 0, t4 = 0;
//			startTime = System.currentTimeMillis();
//			
//			long lapTime = startTime;
//			while(!requestStop && sourceReader.hasNext()){
//				
////				t = System.currentTimeMillis();
//				Document doc = sourceReader.next();
////				logger.debug("doc = "+doc);
//				addDocument(doc);
//				
//				if(lastDocNo % 10000 == 0){
//					logger.info("{} documents indexed, lap = {} ms, elapsed = {}, mem = {}",
//							new Object[]{lastDocNo, System.currentTimeMillis() - lapTime, Formatter.getFormatTime(System.currentTimeMillis() - startTime), Formatter.getFormatSize(Runtime.getRuntime().totalMemory())});
//					
//					lapTime = System.currentTimeMillis();
////					logger.debug("DocNo = "+i+", memory = "+Runtime.getRuntime().totalMemory()+", ");
////					logger.debug(">>>" +t1+", "+t2+", "+t3+", "+t4);
//				}
//			}
//			
//			logger.info("{} documents indexed, lap = {} ms, elapsed = {}, mem = {}",
//					new Object[]{lastDocNo, System.currentTimeMillis() - lapTime, Formatter.getFormatTime(System.currentTimeMillis() - startTime), Formatter.getFormatSize(Runtime.getRuntime().totalMemory())});
//			return lastDocNo;
//		}catch(IRException e){
//			logger.error("SegmentWriter write IRException. "+e.getMessage(),e);
//			throw e;
//		}catch(IOException e){
//			logger.error("SegmentWriter write IOException. "+e.getMessage(),e);
//			throw new IRException(e);
//		}
//		
//	}
	//색인중에 문서번호가 같은 데이터가 존재할 경우 내부적으로 삭제처리된다.
	//이 갯수를 색인결과의 삭제문서 갯수에 더해줘야 전체적인 문서수가 일치하게 된다.
	public int getDuplicateDocCount(){
		return documentWriter.getDuplicateDocCount();
	}
	
	public SegmentInfo close() throws IOException, IRException{
		boolean errorOccured = false;
		Exception exception = null;
		try{
			documentWriter.close();
		}catch(Exception e){
			logger.error("문서색인에러", e);
			exception = e;
			errorOccured = true;
		}
		try{
			searchIndexesWriter.close();
		}catch(Exception e){
			logger.error("검색필드 색인에러", e);
			exception = e;
			errorOccured = true;
		}
		try{
			fieldIndexesWriter.close();
		}catch(Exception e){
			logger.error("필드색인필드 색인에러", e);
			exception = e;
			errorOccured = true;
		}
		try{
			groupIndexesWriter.close();
		}catch(Exception e){
			logger.error("그룹색인필드 색인에러", e);
			exception = e;
			errorOccured = true;
		}
		
		SegmentInfo segmentInfo = new SegmentInfo();
		segmentInfo.update(REVISION, lastDocNo, documentWriter.getDuplicateDocCount(), Formatter.formatDate());
		
		
		//
		//문서가 0건일 경우 새로생성한 리비전 디렉토리를 삭제하고
		// SegmentInfo를 업데이트 하지 않는다.
		//
//		if(lastDocNo == 0){
//			File revisionDir = IRFileName.getRevisionDir(targetDir, REVISION);
//			FileUtils.deleteDirectory(revisionDir);
//			return;
//		}
		
		if(errorOccured){
			File revisionDir = IndexFileNames.getRevisionDir(targetDir, REVISION);
			FileUtils.deleteDirectory(revisionDir);
			throw new IRException(exception);
		}
		
		
//		SegmentInfoWriter segmentInfoWriter = new SegmentInfoWriter(targetDir);
//		segmentInfoWriter.write(baseDocNo, lastDocNo, REVISION, System.currentTimeMillis());
//		segmentInfoWriter.close();
//		File f = new File(targetDir, IRFileName.segmentInfoFile);
//		File revisionDir = IRFileName.getRevisionDir(targetDir, REVISION);
//		FileUtils.copyFileToDirectory(f, revisionDir);
		
		
		
		logger.info("Total {} documents indexed, elapsed = {}, mem = {}",
				new Object[]{lastDocNo, Formatter.getFormatTime(System.currentTimeMillis() - startTime), Formatter.getFormatSize(Runtime.getRuntime().totalMemory())});
		
		
		return segmentInfo;
	}
	
}

