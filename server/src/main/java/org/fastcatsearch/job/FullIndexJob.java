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

public class FullIndexJob extends IndexingJob {
	private static Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");
			
	@Override
	public JobResult doRun() throws FastcatSearchException {
		String[] args = getStringArrayArgs();
		String collectionId = (String)args[0];
		indexingLogger.info("["+collectionId+"] Full Indexing Start!");
		
		long st = System.currentTimeMillis();
		
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

			processLoggerService.log(IndexingProcessLogger.class, new IndexingStartProcessLog(collectionId,
					IndexingResult.TYPE_FULL_INDEXING, startTime(), isScheduled()));
			notificationService.notify(new IndexingStartNotification(collectionId, IndexingResult.TYPE_FULL_INDEXING,
					startTime(), isScheduled()));
			
			CollectionHandler oldCollectionHandler = irService.collectionHandler(collectionId);
			CollectionContext collectionContext = oldCollectionHandler.collectionContext();
			CollectionFilePaths collectionFilePaths = collectionContext.collectionFilePaths();
			DataPlanConfig dataPlanConfig = collectionContext.collectionConfig().getDataPlanConfig();
			
			Schema workSchema = collectionContext.workSchema();
			if(workSchema == null){
				//workschema가 없으면 기존 schema로 색인수행.
				workSchema = collectionContext.schema();
			}
			
			if(workSchema.getFieldSize() == 0){
				indexingLogger.error("["+collectionId+"] Full Indexing Canceled. Schema field is empty. time = "+Strings.getHumanReadableTimeInterval(System.currentTimeMillis() - st));
				throw new FastcatSearchException("["+collectionId+"] Full Indexing Canceled. Schema field is empty. time = "+Strings.getHumanReadableTimeInterval(System.currentTimeMillis() - st));
			}
			
			//주키가 없으면 색인실패
//			if(workSchema.getIndexID() == -1){
////				EventDBLogger.error(EventDBLogger.CATE_INDEX, "컬렉션 스키마에 주키가 없음.");
//				throw new FastcatSearchException("컬렉션 스키마에 주키(Primary Key)를 설정해야합니다.");
//			}
			
			int newDataSequence = collectionContext.getNextDataSequence();
			
			File collectionDataDir = collectionFilePaths.dataFile(newDataSequence);
			FileUtils.deleteDirectory(collectionDataDir);
			
			//Make new CollectionHandler
			//this handler's schema or other setting can be different from working segment handler's one.
			
			int segmentNumber = 0;
			//xml을 unmarshar해서 DataSourceConfig객체로 가지고 있는다.
			DataSourceConfig dataSourceConfig = collectionContext.dataSourceConfig();
			DataSourceReader sourceReader = DataSourceReaderFactory.createSourceReader(collectionFilePaths.file(), workSchema, dataSourceConfig, null, true);
			
			if(sourceReader == null){
				throw new FastcatSearchException("데이터 수집기 생성중 에러발생. sourceType = "+dataSourceConfig);
			}
			
			/*
			 * 색인파일 생성.
			 */
			SegmentInfo segmentInfo = null;
			IndexConfig indexConfig = collectionContext.collectionConfig().getIndexConfig();
			File segmentDir = collectionFilePaths.segmentFile(newDataSequence, segmentNumber);
			indexingLogger.info("Segment Dir = {}", segmentDir.getAbsolutePath());
			SegmentWriter writer = null;
			int count = 0;
			int[] updateAndDeleteSize = {0, 0};
			
			try{
				writer = new SegmentWriter(workSchema, segmentDir, indexConfig);
				
				
				long startTime = System.currentTimeMillis();
				long lapTime = startTime;
				while(sourceReader.hasNext()){
					
//					t = System.currentTimeMillis();
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
//				logger.error("SegmentWriter indexDocument Exception! "+e.getMessage(),e);
				throw e;
			}finally{
				Exception exception = null;
				try{
					segmentInfo = writer.close();
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
				logger.info("["+collectionId+"] Full Indexing Canceled due to no documents. time = "+Strings.getHumanReadableTimeInterval(System.currentTimeMillis() - st));
				result = new IndexingJobResult(collectionId, segmentDir, 0, 0, 0, (int)(System.currentTimeMillis() - st));
				return new JobResult(result);
			}
			
			//apply schema setting
			collectionContext.applyWorkSchema();
			
			
			//전체색인문서에도 중복된 문서들이 존재하면 삭제문서가 발생할수 있다. 
			DeleteIdSet deleteIdSet = sourceReader.getDeleteList();
			
			//TODO 새 컬렉션을 로드하기전에 collectionContext 가 저장되어있어야 로드시 사용한다.
			CollectionHandler newHandler = irService.loadCollectionHandler(collectionId, newDataSequence);
			SegmentInfo segmentInfo2 = newHandler.addSegment(segmentInfo, segmentDir, null);
//			updateAndDeleteSize[1] += writer.getDuplicateDocCount();//중복문서 삭제카운트
			
//			logger.info("== SegmentStatus ==");
//			newHandler.printSegmentStatus();
			
//			newHandler.saveDataSequenceFile();
			collectionContext.updateCollectionStatus(IndexingType.FULL_INDEXING, newDataSequence, count, segmentInfo.getRevisionInfo().getUpdateCount(), segmentInfo.getRevisionInfo().getDeleteCount(), st , endTime);
//			
			
			
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
			
//			SegmentInfo si = newHandler.getLastSegmentReader().segmentInfo();
//			int docSize = si.getDocCount();
			
			/*
			 * DataStatus 결과 파일 업데이트.
			 */
			long endTime = System.currentTimeMillis();
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			String startDt = sdf.format(st);
//			String endDt = sdf.format(new Date());
//			int duration = (int) (System.currentTimeMillis() - st);
//			String durationStr = Formatter.getFormatTime(duration);
//			IRSettings.storeIndextime(collectionId, "FULL", startDt, endDt, durationStr, count);
			/*
			 * 5초후에 캐시 클리어.
			 */
			getJobExecutor().offer(new CacheServiceRestartJob(5000));
			
			indexingLogger.info("["+collectionId+"] Full Indexing Finished! docs = "+count+", update = "+updateAndDeleteSize[0]+", delete = "+updateAndDeleteSize[1]+", time = "+(endTime - st));
			result = new IndexingJobResult(collectionId, segmentDir, count, updateAndDeleteSize[0], updateAndDeleteSize[1], (int)(endTime - st));
			isSuccess = true;
			
			
			//최종 셋팅들을 모두 저장한다.
			CollectionContextUtil.writeStatus(collectionContext);
			
			
			return new JobResult(result);
			
		} catch (Throwable e) {
			indexingLogger.error("["+collectionId+"] Indexing error = "+e.getMessage(),e);
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

			processLoggerService.log(IndexingProcessLogger.class, new IndexingFinishProcessLog(collectionId,
					IndexingResult.TYPE_FULL_INDEXING, isSuccess, startTime(), endTime, isScheduled(), streamableResult));

			notificationService.notify(new IndexingFinishNotification(collectionId, IndexingResult.TYPE_FULL_INDEXING, isSuccess,
					startTime(), endTime, streamableResult));
		}
		
		
	}


}
