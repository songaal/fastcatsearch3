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

package org.fastcatsearch.job;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.datasource.reader.DataSourceReader;
import org.fastcatsearch.datasource.reader.DataSourceReaderFactory;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.config.DataPlanConfig;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.SegmentWriter;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.SegmentReader;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.log.EventDBLogger;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.CollectionContextUtil;
import org.fastcatsearch.util.CollectionFilePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionIndexingJob extends IndexingJob {

	private static final long serialVersionUID = 8592440625617733533L;
	protected static final Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		String[] args = getStringArrayArgs();
		String collectionId = args[0];
//		String indexingType = args[1]; //full or append
//		boolean fromScratch = "full".equalsIgnoreCase(indexingType);
		
		indexingLogger.info("[{}] Inc Collection Indexing Start!", collectionId);
		
		long st = System.currentTimeMillis();
		try {
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			
			
			CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
			if(collectionHandler == null){
				indexingLogger.error("컬렉션 [{}] 을 찾을수 없습니다.", collectionId);
				throw new FastcatSearchException("컬렉션 ["+collectionId+"] 을 찾을수 없음.");
			}
			
			if(!collectionHandler.isLoaded()){
				//전체색인이 아닌데, 컬렉션이 로드되어있지 않으면, 색인을 진행하지 않는다.
				indexingLogger.error("컬렉션 [{}]이 동작중이 아니어서 증분색인을 수행할수 없습니다.", collectionId);
				throw new FastcatSearchException("컬렉션 ["+collectionId+"]이 동작중이 아니어서 증분색인을 수행할수 없습니다.");
			}
			
			CollectionContext workingCollectionContext = collectionHandler.collectionContext().copy();
			workingCollectionContext.nextDataSequence();
			
			CollectionFilePaths  collectionFilePaths = workingCollectionContext.collectionFilePaths();
			DataPlanConfig dataPlanConfig = workingCollectionContext.collectionConfig().getDataPlanConfig();
			Schema schema = null;
			SegmentInfo workingSegmentInfo = null;
			
			//증분색인이면 기존스키마그대로 사용. 
			schema = workingCollectionContext.schema();
			
			SegmentReader lastSegmentReader = collectionHandler.getLastSegmentReader();
			
			if(lastSegmentReader != null){
				SegmentInfo segmentInfo = lastSegmentReader.segmentInfo();
				int docCount = segmentInfo.getRevisionInfo().getDocumentCount();
				int segmentDocumentLimit = dataPlanConfig.getSegmentDocumentLimit();
				if(docCount >= segmentDocumentLimit){
					//segment가 생성되는 증분색인.
					workingSegmentInfo = segmentInfo.getNextSegmentInfo();
				}else{
					//기존 segment에 append되는 증분색인.
					workingSegmentInfo = segmentInfo;
				}
			}else{
				//로딩된 세그먼트가 없음.
				//이전 색인정보가 없다. 즉 전체색인이 수행되지 않은 컬렉션.
				//segment가 생성되는 증분색인.
				workingSegmentInfo = new SegmentInfo();
			}
			
			int dataSequence = collectionHandler.getDataSequence();
			logger.debug("workingHandler={}, dataSequence={}", collectionHandler, dataSequence);
			String lastIndexTime = workingCollectionContext.getLastIndexTime();
			DataSourceConfig dataSourceConfig = workingCollectionContext.dataSourceConfig();
			DataSourceReader sourceReader = DataSourceReaderFactory.createSourceReader(collectionFilePaths.file(), schema, dataSourceConfig, lastIndexTime, false);
			
			if(sourceReader == null){
				EventDBLogger.error(EventDBLogger.CATE_INDEX, "데이터수집기를 생성할 수 없습니다.");
				throw new FastcatSearchException("데이터 수집기 생성중 에러발생. sourceType = "+dataSourceConfig);
			}
			
			IndexConfig indexConfig = workingCollectionContext.collectionConfig().getIndexConfig();
			int count = 0;
			logger.debug("WorkingSegmentInfo = {}", workingSegmentInfo);
			File segmentDir = null;
			RevisionInfo revisionInfo = null;
			int revision = workingSegmentInfo.getNextRevision();
			String segmentNumber = workingSegmentInfo.getId();
			
			segmentDir = collectionFilePaths.segmentFile(dataSequence, segmentNumber);
			indexingLogger.info("Segment Dir = {}", segmentDir.getAbsolutePath());
			
			SegmentWriter segmentWriter = null;
			try{
				segmentWriter = new SegmentWriter(schema, segmentDir, revision, indexConfig);
				long startTime = System.currentTimeMillis();
				long lapTime = startTime;
				while(sourceReader.hasNext()){
					
//					t = System.currentTimeMillis();
					Document doc = sourceReader.nextDocument();
					segmentWriter.addDocument(doc);
					count++;
					if(count % 10000 == 0){
						logger.info("{} documents indexed, lap = {} ms, elapsed = {}, mem = {}",
								new Object[]{count, System.currentTimeMillis() - lapTime, Formatter.getFormatTime(System.currentTimeMillis() - startTime), Formatter.getFormatSize(Runtime.getRuntime().totalMemory())});
						lapTime = System.currentTimeMillis();
					}
				}
			}catch(IRException e){
				logger.error("SegmentWriter Index Exception! " + e.getMessage(), e);
				throw e;
			}finally{
				if(segmentWriter != null){
					revisionInfo = segmentWriter.close();
				}
				sourceReader.close();
			}
			
			workingSegmentInfo.updateRevision(revisionInfo);
			
			//collectionHandler에 append
			
			//count가 0일 경우, revision디렉토리는 삭제되었고 segmentInfo파일도 업데이트 되지 않은 상태이다.
			int deleteCount = sourceReader.getDeleteList().size();
			int indexedCount = segmentWriter.getDocumentCount();
			if(indexedCount == 0){
				if(deleteCount == 0){
					indexingLogger.info("[{}] Incremental Indexing Canceled due to no documents. time = {}", collectionId, Formatter.getFormatTime(System.currentTimeMillis() - st));
					return null;
				}else{
					//count가 0이고 삭제문서만 존재할 경우 리비전은 증가하지 않은 상태.
					collectionHandler.updateCollection(workingSegmentInfo, segmentDir, sourceReader.getDeleteList());
				}
			}else{
				collectionHandler.updateCollection(workingSegmentInfo, segmentDir, sourceReader.getDeleteList());
			}
			
			logger.info("== SegmentStatus ==");
			collectionHandler.printSegmentStatus();
			logger.info("===================");
			
			
/////////////////////////////////
			//append segment info
			workingCollectionContext.addSegmentInfo(workingSegmentInfo);
			
			//apply schema setting
			CollectionContextUtil.applyWorkSchema(workingCollectionContext);
			CollectionHandler newHandler = irService.loadCollectionHandler(workingCollectionContext);
			
			/*
			 * 컬렉션 리로드
			 */
			CollectionHandler oldCollectionHandler = irService.putCollectionHandler(collectionId, newHandler);
			if(oldCollectionHandler != null){
				logger.info("## Close Previous Collection Handler");
				oldCollectionHandler.close();
			}
			DataInfo dataInfo = newHandler.collectionContext().dataInfo();
			indexingLogger.info(dataInfo.toString());
			
			
			
			
//			if(forceAppend || (isAppend && !forceSeparate)){
//				String segmentNumber = workingSegmentInfo.getId();
//				//새로운 리비전으로 증가한다.
////				int revision = collectionHandler.getLastSegmentReader().segmentInfo().getNextRevision();
//				
//				segmentDir = collectionFilePaths.segmentFile(dataSequence, segmentNumber);
////				segmentDir = new File(IRSettings.getCollectionSegmentPath(collectionId, dataSequence, segmentNumber));
//				indexingLogger.info("Revision Dir = {}", new File(segmentDir, Integer.toString(revision)).getAbsolutePath());
//				logger.info("Revision Dir = "+new File(segmentDir,revision+"").getAbsolutePath());
//				SegmentWriter appender = null;
//				try{
//					appender = new SegmentWriter(schema, segmentDir, revision, indexConfig);
//					long startTime = System.currentTimeMillis();
//					long lapTime = startTime;
//					while(sourceReader.hasNext()){
//						
////						t = System.currentTimeMillis();
//						Document doc = sourceReader.nextDocument();
//						appender.addDocument(doc);
//						count++;
//						if(count % 10000 == 0){
//							logger.info("{} documents indexed, lap = {} ms, elapsed = {}, mem = {}",
//									new Object[]{count, System.currentTimeMillis() - lapTime, Formatter.getFormatTime(System.currentTimeMillis() - startTime), Formatter.getFormatSize(Runtime.getRuntime().totalMemory())});
//							lapTime = System.currentTimeMillis();
//						}
//					}
//				}catch(IRException e){
//					EventDBLogger.error(EventDBLogger.CATE_INDEX, "세그먼트생성에러발생.", EventDBLogger.getStackTrace(e));
//					logger.error("SegmentAppender indexDocument Exception! "+e.getMessage(),e);
//					throw e;
//				}finally{
//					if(appender != null){
//						revisionInfo = appender.close();
//					}
//					sourceReader.close();
//				}
//				
//				//count가 0일 경우, revision디렉토리는 삭제되었고 segmentInfo파일도 업데이트 되지 않은 상태이다.
//				int deleteCount = sourceReader.getDeleteList().size();
//				int indexedCount = appender.getDocumentCount();
//				
//				SegmentInfo segmentInfo = workingSegmentInfo.copy();
//				
//				segmentInfo.updateRevision(revisionInfo);
//				
//				if(indexedCount == 0){
//					if(deleteCount == 0){
//						indexingLogger.info("[{}] Incremental Indexing Canceled due to no documents. time = {}", collectionId, Formatter.getFormatTime(System.currentTimeMillis() - st));
//						return null;
//					}else{
//						//count가 0이고 삭제문서만 존재할 경우 리비전은 증가하지 않은 상태.
//						
//						//TODO revisionInfo는 업데이트!!
//						
//						collectionHandler.updateSegment(segmentInfo, segmentDir, sourceReader.getDeleteList());
//					}
//				}else{
//					//그외의 경우는 새로운 리비전 디렉토리 생성됨.
//					//updateAndDeleteSize = ;
//					collectionHandler.updateSegment(segmentInfo, segmentDir, sourceReader.getDeleteList());
//				}
////				updateAndDeleteSize[1] += appender.getDuplicateDocCount();//중복문서 삭제카운트. 엄밀하게는 업데이트 카운트. 
//				logger.info("== SegmentStatus ==");
//				collectionHandler.printSegmentStatus();
//				logger.info("===================");
//			}else{
//				
//				//
//				// 새로운 segment를 만들어서 추가할때.
//				//
//				SegmentInfo segmentInfo = workingSegmentInfo.getNextSegmentInfo();
//				
//				String newSegmentId = segmentInfo.getId();
//				segmentDir = collectionFilePaths.segmentFile(dataSequence, newSegmentId);
//				int revision = collectionHandler.getLastSegmentReader().segmentInfo().getRevision();
//				indexingLogger.info("Segment Dir = {}, baseNo = {}", segmentDir.getAbsolutePath(), segmentInfo.getBaseNumber());
//				FileUtils.deleteDirectory(segmentDir);
//				
//				SegmentWriter writer = null;
//				try{
//					writer = new SegmentWriter(schema, segmentDir, revision, indexConfig);
//					long startTime = System.currentTimeMillis();
//					long lapTime = startTime;
//					while(sourceReader.hasNext()){
//						
//						Document doc = sourceReader.nextDocument();
//						writer.addDocument(doc);
//						count++;
//						if(writer.getDocumentCount() % 10000 == 0){
//							logger.info("{} documents indexed, lap = {} ms, elapsed = {}, mem = {}",
//									new Object[]{writer.getDocumentCount(), System.currentTimeMillis() - lapTime, Formatter.getFormatTime(System.currentTimeMillis() - startTime), Formatter.getFormatSize(Runtime.getRuntime().totalMemory())});
//							lapTime = System.currentTimeMillis();
//						}
//					}
//				}catch(IRException e){
//					EventDBLogger.error(EventDBLogger.CATE_INDEX, "세그먼트생성에러발생.", EventDBLogger.getStackTrace(e));
//					logger.error("SegmentWriter indexDocument Exception! "+e.getMessage(),e);
//					throw e;
//				}finally{
//					if(writer != null){
//						revisionInfo = writer.close();
//					}
//					sourceReader.close();
//				}
//				
//				int deleteCount = sourceReader.getDeleteList().size();
//				int indexedCount = writer.getDocumentCount();
//				if(indexedCount == 0){
//					if(deleteCount == 0){
//						indexingLogger.info("[{}] Incremental Indexing Canceled due to no documents. time = {}", collectionId, Formatter.getFormatTime(System.currentTimeMillis() - st));
//						return null;
//					}else{
//						//추가문서는 없고, 삭제문서만 있을때는 append로 처리한다.
////						String segmentId = currentSegmentInfo.getId();
//						
//						//FIXME segmentInfo에도 udpate, workingHandler에도 ???
//						
//						segmentInfo.updateRevision(revisionInfo);
//						collectionHandler.updateSegment(segmentInfo, segmentDir, sourceReader.getDeleteList());
//					}
//				}else{
//					segmentInfo.updateRevision(revisionInfo);
//					collectionHandler.addSegment(workingSegmentInfo, segmentDir, sourceReader.getDeleteList());
//				}
////				updateAndDeleteSize[1] += writer.getDuplicateDocCount();//중복문서 삭제카운트
//				logger.info("== SegmentStatus ==");
//				collectionHandler.printSegmentStatus();
//				logger.info("===================");
//			}
//			irService.putCollectionHandler(collectionId, collectionHandler);
//			SegmentInfo si = workingHandler.getLastSegmentInfo();
//			indexingLogger.info(si.toString());
//			int docSize = si.getDocCount();
			
			
			
			//TODO 
			
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String startDt = sdf.format(st);
			String endDt = sdf.format(new Date());
			int duration = (int) (System.currentTimeMillis() - st);
			String durationStr = Formatter.getFormatTime(duration);
			
//			collectionContext.updateCollectionStatus(IndexingType.ADD_INDEXING, dataSequence, count, st , System.currentTimeMillis());
//			IRSettings.storeIndextime(collectionId, "INC", startDt, endDt, durationStr, count);
			
			//5초후에 캐시 클리어.
			getJobExecutor().offer(new CacheServiceRestartJob());
			
			indexingLogger.info("[{}] Incremental Indexing Finished! docs = {}, update = {}, delete = {}, time = {}", collectionId, count, 0, 0, durationStr);
			
			return new JobResult(new IndexingJobResult(collectionId, segmentDir, count, 0, 0, duration));
			
		} catch (Exception e) {
//			EventDBLogger.error(EventDBLogger.CATE_INDEX, "증분색인에러", EventDBLogger.getStackTrace(e));
			throw new FastcatSearchException("ERR-00501", e);
		}
		
		
		
	}

}
