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

import org.fastcatsearch.common.Strings;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.datasource.reader.SourceReader;
import org.fastcatsearch.db.dao.IndexingResult;
import org.fastcatsearch.env.CollectionFilePaths;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionContextWriter;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.config.DataPlanConfig;
import org.fastcatsearch.ir.config.DataSourceConfig;
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
			
			CollectionContext collectionContext = irService.collectionContext(collectionId);
			CollectionFilePaths  collectionFilePaths = collectionContext.collectionFilePaths();
			DataPlanConfig dataPlanConfig = collectionContext.collectionConfig().getDataPlanConfig();
			int DATA_SEQUENCE_CYCLE = dataPlanConfig.getDataSequenceCycle();
			
			File collectionHomeDir = collectionFilePaths.home().file();
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
			
//			int currentDataSequence = collectionContext.collectionStatus().getDataStatus().getSequence();
////			DataSequenceFile dataSequenceFile = new DataSequenceFile(collectionHomeDir, -1); //read sequence
//			int	newDataSequence = (currentDataSequence + 1) % DATA_SEQUENCE_CYCLE;
			int newDataSequence = collectionContext.getNextDataSequence();
			
//			logger.debug("dataSequence={}, DATA_SEQUENCE_CYCLE={}", newDataSequence, DATA_SEQUENCE_CYCLE);
			
			File collectionDataDir = collectionFilePaths.dataPath(newDataSequence).file();
			FileUtils.cleanCollectionDataDirectorys(collectionDataDir);
			
			//Make new CollectionHandler
			//this handler's schema or other setting can be different from working segment handler's one.
			
			int segmentNumber = 0;
			//1.xml을 unmarshar해서 sourceconfig객체로 가지고 있는다.
			//2. 
			DataSourceConfig dsSetting = collectionContext.dataSourceConfig();
			SourceReader sourceReader = SourceReaderFactory.createSourceReader(collectionId, workSchema, dsSetting, true);
			
			if(sourceReader == null){
//				EventDBLogger.error(EventDBLogger.CATE_INDEX, "데이터수집기를 생성할 수 없습니다.");
				throw new FastcatSearchException("데이터 수집기 생성중 에러발생. sourceType = "+dsSetting.sourceType);
			}
			
			/*
			 * 색인파일 생성.
			 */
			SegmentInfo segmentInfo = null;
			IndexConfig indexConfig = collectionContext.collectionConfig().getIndexConfig();
			File segmentDir = collectionFilePaths.segmentPath(newDataSequence, segmentNumber).file();
			indexingLogger.info("Segment Dir = {}", segmentDir.getAbsolutePath());
			SegmentWriter writer = null;
			int count = 0;
			int[] updateAndDeleteSize = {0, 0};
			
			try{
//				writer = new SegmentWriter(workSchema, sourceReader, segmentDir);
				writer = new SegmentWriter(workSchema, segmentDir, indexConfig);
				
				
				long startTime = System.currentTimeMillis();
				long lapTime = startTime;
				while(sourceReader.hasNext()){
					
//					t = System.currentTimeMillis();
					Document doc = sourceReader.next();
					int lastDocNo = writer.addDocument(doc);
					
					if(lastDocNo % 10000 == 0){
						logger.info("{} documents indexed, lap = {} ms, elapsed = {}, mem = {}",
								new Object[]{lastDocNo, System.currentTimeMillis() - lapTime, Formatter.getFormatTime(System.currentTimeMillis() - startTime), Formatter.getFormatSize(Runtime.getRuntime().totalMemory())});
						lapTime = System.currentTimeMillis();
					}
				}
//				count = writer.indexDocument(); //index at here
				count = writer.getDocumentCount();
			}catch(IRException e){
//				EventDBLogger.error(EventDBLogger.CATE_INDEX, "세그먼트생성에러발생.", EventDBLogger.getStackTrace(e));
				logger.error("SegmentWriter indexDocument Exception! "+e.getMessage(),e);
				throw e;
			}finally{
				try{
					segmentInfo = writer.close();
				}catch(Exception e){
					logger.error("Error while close segment writer!", e);
					throw e;
				}
				try{
					sourceReader.close();
				}catch(Exception e){
					logger.error("Error while close source reader!", e);
					throw e;
				}
			}
			
			if(count == 0){
				indexingLogger.info("["+collectionId+"] Full Indexing Canceled due to no documents. time = "+Strings.getHumanReadableTimeInterval(System.currentTimeMillis() - st));
				throw new FastcatSearchException("["+collectionId+"] Full Indexing Canceled due to no documents. time = "+Strings.getHumanReadableTimeInterval(System.currentTimeMillis() - st));
			}
			
			//apply schema setting
			collectionContext.applyWorkSchema();
			
			
			//전체색인문서에도 중복된 문서들이 존재하면 삭제문서가 발생할수 있다. 
			DeleteIdSet deleteIdSet = sourceReader.getDeleteList();
			
//			File collectionDir = new File(IRSettings.getCollectionHome(collectionId));
//			Schema newSchema = IRSettings.getSchema(collectionId, false);
//			CollectionHandler newHandler = new CollectionHandler(collectionId, collectionDir, newSchema, indexConfig, newDataSequence);

			CollectionHandler newHandler = irService.loadCollectionHandler(collectionId, newDataSequence);
			SegmentInfo segmentInfo2 = newHandler.addSegment(segmentInfo, segmentDir, null);
//			updateAndDeleteSize[1] += writer.getDuplicateDocCount();//중복문서 삭제카운트
			
//			logger.info("== SegmentStatus ==");
//			newHandler.printSegmentStatus();
			
//			newHandler.saveDataSequenceFile();
			
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
			
//			SegmentInfo si = newHandler.getLastSegmentReader().segmentInfo();
//			int docSize = si.getDocCount();
			
			/*
			 * DataStatus 결과 파일 업데이트.
			 */
			long endTime = System.currentTimeMillis();
			collectionContext.updateCollectionStatus(IndexingType.FULL_INDEXING, newDataSequence, count, segmentInfo.getUpdateCount(), segmentInfo.getDeleteCount(), st , endTime);
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
			CollectionContextWriter.write(collectionContext);
			
			
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
