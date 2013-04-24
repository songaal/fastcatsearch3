/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.task;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.DataSourceSetting;
import org.fastcatsearch.ir.config.Schema;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.SegmentWriter;
import org.fastcatsearch.ir.source.SourceReader;
import org.fastcatsearch.ir.util.Formatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MakeIndexFileTask extends Task {
	private static Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");
	
	public int makeIndex(String collection, String collectionHomeDir, Schema workSchema, File collectionDataDir, DataSourceSetting dsSetting
			, SourceReader sourceReader, File segmentDir) throws Exception {
			
		if(workSchema.getFieldSize() == 0){
			throw new TaskException("["+collection+"] Full Indexing Canceled. Schema field is empty.");
		}
		
		//주키가 없으면 색인실패
		if(workSchema.getIndexID() == -1){
			throw new TaskException("컬렉션 스키마에 주키(Primary Key)를 설정해야합니다.");
		}
		
		FileUtils.deleteDirectory(collectionDataDir);
		
		if(sourceReader == null){
			throw new TaskException("데이터 수집기 생성중 에러발생. sourceType = "+dsSetting.sourceType);
		}
		
		indexingLogger.info("Segment Dir = "+segmentDir.getAbsolutePath());
		SegmentWriter writer = null;
		int count = 0;
		
		try{
			writer = new SegmentWriter(workSchema, segmentDir);
			
			long startTime = System.currentTimeMillis();
			long lapTime = startTime;
			while(sourceReader.hasNext()){
				Document doc = sourceReader.next();
				int lastDocNo = writer.addDocument(doc);
				
				if(lastDocNo % 10000 == 0){
					logger.info("{} documents indexed, lap = {} ms, elapsed = {}, mem = {}",
							new Object[]{lastDocNo, System.currentTimeMillis() - lapTime, Formatter.getFormatTime(System.currentTimeMillis() - startTime), Formatter.getFormatSize(Runtime.getRuntime().totalMemory())});
					lapTime = System.currentTimeMillis();
				}
			}
			count = writer.getDocumentCount();
		}catch(IRException e){
			logger.error("SegmentWriter indexDocument Exception! "+e.getMessage(),e);
			throw e;
		}finally{
			try{
				if(writer != null){
					writer.close();
				}
			}catch(Exception e){
				logger.error("Error while close segment writer! "+e.getMessage(),e);
				e.printStackTrace();
			}
		}
		int dupCount =  writer.getDuplicateDocCount();//중복문서 삭제카운트
		if(count == 0){
			throw new TaskException("["+collection+"] Full Indexing Canceled due to no documents.");
		}
		
		return dupCount;
		
	}


}
