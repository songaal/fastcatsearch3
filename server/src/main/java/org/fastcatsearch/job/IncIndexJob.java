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
import org.fastcatsearch.datasource.reader.SourceReader;
import org.fastcatsearch.datasource.reader.SourceReaderFactory;
import org.fastcatsearch.env.CollectionFilePaths;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionConfig;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataPlanConfig;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.SegmentAppender;
import org.fastcatsearch.ir.index.SegmentWriter;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.SegmentInfo;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.log.EventDBLogger;
import org.fastcatsearch.service.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IncIndexJob extends IndexingJob {

	private static Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		String[] args = getStringArrayArgs();
		String collectionId = args[0];
		boolean forceAppend = false;
		boolean forceSeparate = false;
		if(args.length > 1){
			forceAppend = "append".equalsIgnoreCase(args[1]);
			forceSeparate = "separate".equalsIgnoreCase(args[1]);
		}
		if(forceAppend){
			indexingLogger.info("["+collectionId+"] Add Indexing Start! (forceAppend)");
		}else if(forceSeparate){
			indexingLogger.info("["+collectionId+"] Add Indexing Start! (forceSeparate)");
		}else{
			indexingLogger.info("["+collectionId+"] Add Indexing Start!");
		}
		
		long st = System.currentTimeMillis(); 
		try {
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			
			
			CollectionHandler workingHandler = irService.collectionHandler(collectionId);
			if(workingHandler == null){
				indexingLogger.error("["+collectionId+"] CollectionHandler is not running!");
				EventDBLogger.error(EventDBLogger.CATE_INDEX, "컬렉션 "+collectionId+"가 서비스중이 아님.");
				throw new FastcatSearchException("## ["+collectionId+"] CollectionHandler is not running...");
			}
			
			boolean isAppend = false;
			SegmentInfo currentSegmentInfo = workingHandler.getLastSegmentInfo();
			if(currentSegmentInfo == null){
				indexingLogger.error("["+collectionId+"] has no segment!  Do full-indexing first!!");
				return null;
			}
			
			CollectionContext collectionContext = irService.collectionContext(collectionId);
			CollectionFilePaths  collectionFilePaths = collectionContext.collectionFilePaths();
			DataPlanConfig dataPlanConfig = collectionContext.collectionConfig().getDataPlanConfig();
			int DATA_SEQUENCE_CYCLE = dataPlanConfig.getDataSequenceCycle();
			
			File collectionHomeDir = collectionFilePaths.home().file();
			Schema schema = collectionContext.schema();
			
			int docCount = currentSegmentInfo.getDocCount();
			
			boolean useSeparateAddIndexing = dataPlanConfig.isSeparateIncIndexing();
			int segmentDocumentLimit = dataPlanConfig.getSegmentDocumentLimit(); //irconfig.getInt("segment.document.limit");
		
			if(useSeparateAddIndexing && currentSegmentInfo.getSegmentNumber() == 0){
				//only has full indexing data
				//make separete segment 
				isAppend = false;
			}else{
				//정해진 세그먼트내 문서크기보다 크면, 세그먼트를 분리해서 생성한다. 
				if(docCount >= segmentDocumentLimit){
					isAppend = false;
				}else{
					isAppend = true;
				}
			}
			
			
			int dataSequence = workingHandler.getDataSequence();
			logger.debug("workingHandler={}, dataSequence={}", workingHandler, dataSequence);
			
			DataSourceConfig dsSetting = collectionContext.dataSourceSetting();
			SourceReader sourceReader = SourceReaderFactory.createSourceReader(collectionId, schema, dsSetting, true);
			
			if(sourceReader == null){
				EventDBLogger.error(EventDBLogger.CATE_INDEX, "데이터수집기를 생성할 수 없습니다.");
				throw new FastcatSearchException("데이터 수집기 생성중 에러발생. sourceType = "+dsSetting.sourceType);
			}
			//Check prev doc No.
			//case.1 : forceAppend
			//case.2 : isAppend and not forceSeparate
			//otherwise : separate
			
			IndexConfig indexConfig = collectionContext.collectionConfig().getIndexConfig();
			int count = 0;
			int[] updateAndDeleteSize = {0, 0};
			logger.debug("currentSegmentInfo = {}, isAppend={}",currentSegmentInfo, isAppend);
			File segmentDir = null;
			if(forceAppend || (isAppend && !forceSeparate)){
				int segmentNumber = currentSegmentInfo.getSegmentNumber();
				int revision = workingHandler.getLastSegmentInfo().getLastRevision();
				//
				//새로운 리비전으로 증가한다.
				//
				revision++;
				segmentDir = collectionFilePaths.segmentPath(dataSequence, segmentNumber).file();
//				segmentDir = new File(IRSettings.getCollectionSegmentPath(collectionId, dataSequence, segmentNumber));
				indexingLogger.info("Revision Dir = {}", new File(segmentDir,revision+"").getAbsolutePath());
				logger.info("Revision Dir = "+new File(segmentDir,revision+"").getAbsolutePath());
				SegmentAppender appender = null;
				try{
					appender = new SegmentAppender(schema, segmentDir, revision, indexConfig);
					long startTime = System.currentTimeMillis();
					long lapTime = startTime;
					while(sourceReader.hasNext()){
						
//						t = System.currentTimeMillis();
						Document doc = sourceReader.next();
						appender.addDocument(doc);
						count++;
						if(count % 10000 == 0){
							logger.info("{} documents indexed, lap = {} ms, elapsed = {}, mem = {}",
									new Object[]{count, System.currentTimeMillis() - lapTime, Formatter.getFormatTime(System.currentTimeMillis() - startTime), Formatter.getFormatSize(Runtime.getRuntime().totalMemory())});
							lapTime = System.currentTimeMillis();
						}
					}
//					count = writer.indexDocument(); //index at here
				}catch(IRException e){
					EventDBLogger.error(EventDBLogger.CATE_INDEX, "세그먼트생성에러발생.", EventDBLogger.getStackTrace(e));
					logger.error("SegmentAppender indexDocument Exception! "+e.getMessage(),e);
					throw e;
				}finally{
					if(appender != null){
						appender.close();
					}
					sourceReader.close();
				}
				
				//count가 0일 경우, revision디렉토리는 삭제되었고 segmentInfo파일도 업데이트 되지 않은 상태이다.
				int deleteCount = sourceReader.getDeleteList().size();
				int indexedCount = appender.getDocumentCount();
				if(indexedCount == 0){
					if(deleteCount == 0){
						indexingLogger.info("["+collectionId+"] Incremental Indexing Canceled due to no documents. time = "+Formatter.getFormatTime(System.currentTimeMillis() - st));
						return null;
					}else{
						//count가 0이고 삭제문서만 존재할 경우 리비전은 증가하지 않은 상태.
						updateAndDeleteSize = workingHandler.appendSegment(segmentNumber, segmentDir, sourceReader.getDeleteList(), false);
					}
				}else{
					//그외의 경우는 새로운 리비전 디렉토리 생성됨.
					updateAndDeleteSize = workingHandler.appendSegment(segmentNumber, segmentDir, sourceReader.getDeleteList(), true);
				}
				updateAndDeleteSize[1] += appender.getDuplicateDocCount();//중복문서 삭제카운트
				logger.info("== SegmentStatus ==");
				workingHandler.printSegmentStatus();
				logger.info("===================");
			}else{
				int nextSegmentBaseNumber = currentSegmentInfo.getBaseDocNo() + currentSegmentInfo.getDocCount();
				int newSegmentNumber = workingHandler.getNextSegmentNumber();
				segmentDir = collectionFilePaths.segmentPath(dataSequence, newSegmentNumber).file();
//				segmentDir = new File(IRSettings.getCollectionSegmentPath(collectionId, dataSequence, newSegmentNumber));
				int revision = workingHandler.getLastSegmentInfo().getLastRevision();
				indexingLogger.info("Segment Dir = "+segmentDir.getAbsolutePath()+", baseNo = "+nextSegmentBaseNumber);
				FileUtils.deleteDirectory(segmentDir);
				
				SegmentWriter writer = null;
				try{
					writer = new SegmentWriter(schema, segmentDir, nextSegmentBaseNumber, revision, indexConfig);
//					count = writer.indexDocument();
					long startTime = System.currentTimeMillis();
					long lapTime = startTime;
					while(sourceReader.hasNext()){
						
//						t = System.currentTimeMillis();
						Document doc = sourceReader.next();
						writer.addDocument(doc);
						count++;
						if(count % 10000 == 0){
							logger.info("{} documents indexed, lap = {} ms, elapsed = {}, mem = {}",
									new Object[]{count, System.currentTimeMillis() - lapTime, Formatter.getFormatTime(System.currentTimeMillis() - startTime), Formatter.getFormatSize(Runtime.getRuntime().totalMemory())});
							lapTime = System.currentTimeMillis();
						}
					}
				}catch(IRException e){
					EventDBLogger.error(EventDBLogger.CATE_INDEX, "세그먼트생성에러발생.", EventDBLogger.getStackTrace(e));
					logger.error("SegmentWriter indexDocument Exception! "+e.getMessage(),e);
					throw e;
				}finally{
					if(writer != null){
						writer.close();
					}
					sourceReader.close();
				}
				
				int deleteCount = sourceReader.getDeleteList().size();
				int indexedCount = writer.getDocumentCount();
				if(indexedCount == 0){
					if(deleteCount == 0){
						indexingLogger.info("["+collectionId+"] Incremental Indexing Canceled due to no documents. time = "+Formatter.getFormatTime(System.currentTimeMillis() - st));
						return null;
					}else{
						//추가문서는 없고, 삭제문서만 있을때는 append로 처리한다.
						int segmentNumber = currentSegmentInfo.getSegmentNumber();
						updateAndDeleteSize = workingHandler.appendSegment(segmentNumber, segmentDir, sourceReader.getDeleteList(), false);
					}
				}else{
					updateAndDeleteSize = workingHandler.addSegment(newSegmentNumber, segmentDir, sourceReader.getDeleteList());
				}
				updateAndDeleteSize[1] += writer.getDuplicateDocCount();//중복문서 삭제카운트
				logger.info("== SegmentStatus ==");
				workingHandler.printSegmentStatus();
				logger.info("===================");
			}
			irService.putCollectionHandler(collectionId, workingHandler);
			SegmentInfo si = workingHandler.getLastSegmentInfo();
			indexingLogger.info(si.toString());
			int docSize = si.getDocCount();
			
			
			
			//TODO 
			
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String startDt = sdf.format(st);
			String endDt = sdf.format(new Date());
			int duration = (int) (System.currentTimeMillis() - st);
			String durationStr = Formatter.getFormatTime(duration);
			
			collectionContext.updateCollectionStatus(IndexingType.ADD_INDEXING, dataSequence, count, st , System.currentTimeMillis());
//			IRSettings.storeIndextime(collectionId, "INC", startDt, endDt, durationStr, count);
			
			//5초후에 캐시 클리어.
			getJobExecutor().offer(new CacheServiceRestartJob(5000));
			
			indexingLogger.info("["+collectionId+"] Incremental Indexing Finished! docs = {}, update = {}, delete = {}, time = {}", count, updateAndDeleteSize[0], updateAndDeleteSize[1], durationStr);
			
			return new JobResult(new IndexingJobResult(collectionId, segmentDir, count, updateAndDeleteSize[0], updateAndDeleteSize[1], duration));
			
		} catch (Exception e) {
//			EventDBLogger.error(EventDBLogger.CATE_INDEX, "증분색인에러", EventDBLogger.getStackTrace(e));
			throw new FastcatSearchException("ERR-00501", e);
		}
		
		
		
	}

}
