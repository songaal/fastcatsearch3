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
//import org.fastcatsearch.ir.common.IRException;
//import org.fastcatsearch.ir.common.IRFileName;
//import org.fastcatsearch.ir.config.Schema;
//import org.fastcatsearch.ir.document.Document;
//import org.fastcatsearch.ir.document.DocumentReader;
//import org.fastcatsearch.ir.search.SegmentInfo;
//import org.fastcatsearch.ir.util.Formatter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class SegmentRebuilder {
//	private static Logger logger = LoggerFactory.getLogger(SegmentRebuilder.class);
//	
//	private int count;
//	private boolean requestStop;
//	private long startTime;
//
//	private DocumentReader documentReader;
//
//	private SearchFieldWriter searchWriter;
//
//	private SortFieldWriter sortWriter;
//
//	private GroupFieldWriter groupWriter;
//
//	private int baseDocNo;
//	private int docSize;
//	private SegmentInfo segmentInfo;
//	
//	public SegmentRebuilder(Schema schema, File targetDir) throws IRException{
//		this(schema, targetDir, 0);
//	}
//	public SegmentRebuilder(Schema schema, File targetDir, int revision) throws IRException{
//		init(schema, targetDir, revision);
//	}
//	
//	private void init(Schema schema, File targetDir, int revision) throws IRException{
//		try{
//			segmentInfo = new SegmentInfo(-1, targetDir);
//			baseDocNo = segmentInfo.getBaseDocNo();
//			docSize = segmentInfo.getDocCount();
//			IRFileName.getRevisionDir(targetDir, revision).mkdirs();
//			
//			documentReader = new DocumentReader(schema, targetDir, baseDocNo);
//			searchWriter = new SearchFieldWriter(schema, targetDir, revision);
//			sortWriter = new SortFieldWriter(schema, targetDir, true);
//			groupWriter = new GroupFieldWriter(schema, targetDir, revision);
//		}catch(IOException e){
//			throw new IRException(e);
//		}
//		
//	}
//	
//	
//	public void indexDocument() throws IRException{
//		try{
//			startTime = System.currentTimeMillis();
//			long lapTime = startTime;
//			for(int docNo = baseDocNo ; !requestStop && docNo < docSize ; docNo++) {
//				
//				logger.debug("------------------");
//				Document doc = documentReader.readDocument(docNo);
//				logger.debug("Read doc = "+docNo);
//				searchWriter.write(doc, docNo);
//				sortWriter.write(doc);
//				groupWriter.write(doc);
//				
//				count++;
//				if(count % 10000 == 0){
//					logger.info(count + " documents indexed, lap = "+(System.currentTimeMillis() - lapTime)+" ms, elapsed = " + Formatter.getFormatTime(System.currentTimeMillis() - startTime) + ", mem = "+Formatter.getFormatSize(Runtime.getRuntime().totalMemory()));
//					lapTime = System.currentTimeMillis();
////					logger.debug("DocNo = "+i+", memory = "+Runtime.getRuntime().totalMemory()+", ");
////					logger.debug(">>>" +t1+", "+t2+", "+t3+", "+t4);
//				}
//			}
//			logger.info(count + " documents indexed, lap = "+(System.currentTimeMillis() - lapTime)+" ms, elapsed = " + Formatter.getFormatTime(System.currentTimeMillis() - startTime) + ", mem = "+Formatter.getFormatSize(Runtime.getRuntime().totalMemory()));
//		}catch(IOException e){
//			throw new IRException(e);
//		}
//		
//	}
//	
//	public void close() throws IOException, IRException{
//		documentReader.close();
//		searchWriter.close();
//		sortWriter.close();
//		groupWriter.close();
//		
//		logger.info("Total "+ count + " documents rebuilded, elapsed = " + Formatter.getFormatTime(System.currentTimeMillis() - startTime) + ", mem = "+Formatter.getFormatSize(Runtime.getRuntime().totalMemory()));
//		logger.info("doc count = "+ segmentInfo.getDocCount());
//		logger.info("doc base number = "+segmentInfo.getBaseDocNo());
//	}
//
//}
