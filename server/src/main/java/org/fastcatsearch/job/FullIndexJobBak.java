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

import org.fastcatsearch.common.Strings;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.datasource.reader.DataSourceReader;
import org.fastcatsearch.datasource.reader.DataSourceReaderFactory;
import org.fastcatsearch.db.dao.IndexingResult;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.config.DataPlanConfig;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.SingleSourceConfig;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.fastcatsearch.ir.index.SegmentWriter;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.notification.NotificationService;
import org.fastcatsearch.notification.message.IndexingFinishNotification;
import org.fastcatsearch.notification.message.IndexingStartNotification;
import org.fastcatsearch.processlogger.IndexingProcessLogger;
import org.fastcatsearch.processlogger.ProcessLoggerService;
import org.fastcatsearch.processlogger.log.IndexingFinishProcessLog;
import org.fastcatsearch.processlogger.log.IndexingStartProcessLog;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableThrowable;
import org.fastcatsearch.util.CollectionContextUtil;
import org.fastcatsearch.util.CollectionFilePaths;
import org.fastcatsearch.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FullIndexJobBak extends IndexingJob {
	private static Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");
			
	@Override
	public JobResult doRun() throws FastcatSearchException {
		String[] args = getStringArrayArgs();
		String collectionId = (String)args[0];
		indexingLogger.info("[{}] Full Indexing Start!", collectionId);
		
		long startTime = System.currentTimeMillis();
		
		ProcessLoggerService processLoggerService = null;
		NotificationService notificationService = null;
		boolean isSuccess = false;
		Object result = null;
		
		Throwable throwable = null;
		
		try {
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			ServiceManager serviceManager = ServiceManager.getInstance();
			processLoggerService = serviceManager.getService(ProcessLoggerService.class);
			notificationService = serviceManager.getService(NotificationService.class);

//			processLoggerService.log(IndexingProcessLogger.class, new IndexingStartProcessLog(collectionId,
//					IndexingResult.TYPE_FULL_INDEXING, jobStartTime(), isScheduled()));
//			notificationService.notify(new IndexingStartNotification(collectionId, IndexingResult.TYPE_FULL_INDEXING,
//					jobStartTime(), isScheduled()));
			
			CollectionHandler oldCollectionHandler = irService.collectionHandler(collectionId);
			CollectionContext collectionContext = oldCollectionHandler.collectionContext().copy();
			CollectionFilePaths collectionFilePaths = collectionContext.collectionFilePaths();
			DataPlanConfig dataPlanConfig = collectionContext.collectionConfig().getDataPlanConfig();
			
			Schema workSchema = collectionContext.workSchema();
			if(workSchema == null){
				//workschema가 없으면 기존 schema로 색인수행.
				workSchema = collectionContext.schema();
			}
			
			if(workSchema.getFieldSize() == 0){
				indexingLogger.error("[{}] Full Indexing Canceled. Schema field is empty. time = {}", collectionId, Strings.getHumanReadableTimeInterval(System.currentTimeMillis() - startTime));
				throw new FastcatSearchException("["+collectionId+"] Full Indexing Canceled. Schema field is empty. time = "+Strings.getHumanReadableTimeInterval(System.currentTimeMillis() - startTime));
			}
			
			collectionContext.clearDataInfoAndStatus();
			int newDataSequence = collectionContext.nextDataSequence();
			
			File collectionDataDir = collectionFilePaths.dataFile(newDataSequence);
			FileUtils.deleteDirectory(collectionDataDir);
			
			//Make new CollectionHandler
			//this handler's schema or other setting can be different from working segment handler's one.
			
			Integer segmentNumber = 0;
			//xml을 unmarshar해서 DataSourceConfig객체로 가지고 있는다.
			DataSourceConfig dataSourceConfig = collectionContext.dataSourceConfig();
			DataSourceReader sourceReader = DataSourceReaderFactory.createSourceReader(collectionFilePaths.file(), workSchema, dataSourceConfig, null, true);
			
			if(sourceReader == null){
				throw new FastcatSearchException("데이터 수집기 생성중 에러발생. sourceType = "+dataSourceConfig);
			}
			
			/*
			 * 색인파일 생성.
			 */
			IndexConfig indexConfig = collectionContext.collectionConfig().getIndexConfig();
			File segmentDir = collectionFilePaths.segmentFile(newDataSequence, segmentNumber);
			indexingLogger.info("Segment Dir = {}", segmentDir.getAbsolutePath());
			SegmentWriter writer = null;
			RevisionInfo revisionInfo = null;
			int count = 0;
			
			try{
				writer = new SegmentWriter(workSchema, segmentDir, indexConfig);
				
				long lapTime = startTime;
				while(sourceReader.hasNext()){
					
					Document doc = sourceReader.nextDocument();
					int lastDocNo = writer.addDocument(doc);
					
					if(lastDocNo % 10000 == 0){
						logger.info("{} documents indexed, lap = {} ms, elapsed = {}, mem = {}",
								new Object[]{lastDocNo, System.currentTimeMillis() - lapTime, Formatter.getFormatTime(System.currentTimeMillis() - startTime), Formatter.getFormatSize(Runtime.getRuntime().totalMemory())});
						lapTime = System.currentTimeMillis();
					}
				}
				count = writer.getDocumentCount();
			}catch(Throwable e){
				throw e;
			}finally{
				Exception exception = null;
				try{
					revisionInfo = writer.close();
					
					
					//전체색인에서는 delete id set을 적용하지 않는다.
//					DeleteIdSet deleteIdSet = sourceReader.getDeleteList();
//					segmentInfo.getRevisionInfo().setDeleteCount(deleteIdSet.size());
					
				}catch(Exception e){
					logger.error("Error while close segment writer!", e);
					exception = e;
				}
				try{
					sourceReader.close();
				}catch(Exception e){
					logger.error("Error while close source reader!", e);
					exception = e;
				}
				
				if(exception != null){
					throw exception;
				}
			}
			
			if(count == 0){
				logger.info("[{}] Full Indexing Canceled due to no documents. time = {}", collectionId, Strings.getHumanReadableTimeInterval(System.currentTimeMillis() - startTime));
				result = null;//new IndexingJobResult(collectionId, segmentDir, 0, 0, 0, (int)(System.currentTimeMillis() - startTime));
				return new JobResult(result);
			}
			
			SegmentInfo segmentInfo = new SegmentInfo(segmentNumber.toString(), 0);
			segmentInfo.updateRevision(revisionInfo);
			
			//append segment info
			collectionContext.addSegmentInfo(segmentInfo);
			
			//apply schema setting
			CollectionContextUtil.applyWorkSchema(collectionContext);
			CollectionHandler newHandler = irService.loadCollectionHandler(collectionContext);
			
			/*
			 * 컬렉션 리로드
			 */
			oldCollectionHandler = irService.putCollectionHandler(collectionId, newHandler);
			if(oldCollectionHandler != null){
				logger.info("## Close Previous Collection Handler");
				oldCollectionHandler.close();
			}
			DataInfo dataInfo = newHandler.collectionContext().dataInfo();
			indexingLogger.info(dataInfo.toString());
			
		
			long endTime = System.currentTimeMillis();

			/*
			 * 5초후에 캐시 클리어.
			 */
			getJobExecutor().offer(new CacheServiceRestartJob());
			
			//컬렉션 status 저장.
			int updateDocumentCount = segmentInfo.getRevisionInfo().getUpdateCount();
			int deleteDocumentCount = segmentInfo.getRevisionInfo().getDeleteCount();
			int duration = (int) (endTime - startTime);
			
			//최종 세그먼트 상태를 저장한다.
			CollectionContextUtil.saveDataInfo(collectionContext);
//			collectionContext.updateCollectionStatus(IndexingType.FULL_INDEXING, count, updateDocumentCount, deleteDocumentCount, startTime , endTime);
			CollectionContextUtil.saveCollectionStatus(collectionContext);
			
			indexingLogger.info("[{}] Full Indexing Finished! docs = {}, update = {}, delete = {}, time = {}"
					, collectionId, count, updateDocumentCount, deleteDocumentCount, duration);
			result = null;//new IndexingJobResult(collectionId, segmentDir, count, updateDocumentCount, deleteDocumentCount, duration);
			isSuccess = true;
			
			return new JobResult(result);
			
		} catch (Throwable e) {
			indexingLogger.error("["+collectionId+"] Indexing", e);
			throwable = e;
			throw new FastcatSearchException("ERR-00500", throwable, collectionId); // 전체색인실패.
		} finally {
			long endTime = System.currentTimeMillis();
			Streamable streamableResult = null;
			if (throwable != null) {
				streamableResult = new StreamableThrowable(throwable);
			} else if (result instanceof IndexingJobResult) {
				streamableResult = (IndexingJobResult) result;
			}

//			processLoggerService.log(IndexingProcessLogger.class, new IndexingFinishProcessLog(collectionId,
//					IndexingResult.TYPE_FULL_INDEXING, isSuccess, jobStartTime(), endTime, isScheduled(), streamableResult));
//
//			notificationService.notify(new IndexingFinishNotification(collectionId, IndexingResult.TYPE_FULL_INDEXING, isSuccess,
//					jobStartTime(), endTime, streamableResult));
		}
		
		
	}


}
