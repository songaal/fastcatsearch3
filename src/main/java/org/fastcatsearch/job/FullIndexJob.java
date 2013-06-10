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
import org.fastcatsearch.common.Strings;
import org.fastcatsearch.common.io.Streamable;

import org.fastcatsearch.datasource.DataSourceSetting;
import org.fastcatsearch.datasource.reader.SourceReader;
import org.fastcatsearch.datasource.reader.SourceReaderFactory;
import org.fastcatsearch.db.dao.IndexingResult;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.CollectionConfig;
import org.fastcatsearch.ir.config.IRConfig;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.config.Schema;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.SegmentWriter;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.DataSequenceFile;
import org.fastcatsearch.ir.search.SegmentInfo;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.log.EventDBLogger;
import org.fastcatsearch.notification.NotificationService;
import org.fastcatsearch.notification.message.IndexingFinishNotification;
import org.fastcatsearch.notification.message.IndexingStartNotification;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.processlogger.IndexingProcessLogger;
import org.fastcatsearch.processlogger.ProcessLoggerService;
import org.fastcatsearch.processlogger.log.IndexingFinishProcessLog;
import org.fastcatsearch.processlogger.log.IndexingStartProcessLog;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.IRSettings;
import org.fastcatsearch.transport.vo.StreamableThrowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FullIndexJob extends IndexingJob {
	private static Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");
			
	public static void main(String[] args) throws FastcatSearchException {
		String homePath = args[0];
		String collectionId = args[1];
		IRSettings.setHome(homePath);
		
		FullIndexJob job = new FullIndexJob();
		job.setArgs(new String[]{collectionId});
		job.run();
	}
	
	
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
			
			ServiceManager serviceManager = ServiceManager.getInstance();
			processLoggerService = serviceManager.getService(ProcessLoggerService.class);
			notificationService = serviceManager.getService(NotificationService.class);

			processLoggerService.log(IndexingProcessLogger.class, new IndexingStartProcessLog(collectionId,
					IndexingResult.TYPE_FULL_INDEXING, startTime(), isScheduled()));
			notificationService.notify(new IndexingStartNotification(collectionId, IndexingResult.TYPE_FULL_INDEXING,
					startTime(), isScheduled()));
			
			IRConfig irconfig = IRSettings.getConfig(true);
			int DATA_SEQUENCE_CYCLE = irconfig.getInt("data.sequence.cycle");
			
			File collectionHomeDir = new File(IRSettings.getCollectionHome(collectionId));
			Schema workSchema = IRSettings.getWorkSchema(collectionId, true, false);
			if(workSchema == null)
				workSchema = IRSettings.getSchema(collectionId, false);
			
			if(workSchema.getFieldSize() == 0){
				indexingLogger.error("["+collectionId+"] Full Indexing Canceled. Schema field is empty. time = "+Strings.getHumanReadableTimeInterval(System.currentTimeMillis() - st));
				throw new FastcatSearchException("["+collectionId+"] Full Indexing Canceled. Schema field is empty. time = "+Strings.getHumanReadableTimeInterval(System.currentTimeMillis() - st));
			}
			
			//주키가 없으면 색인실패
			if(workSchema.getIndexID() == -1){
//				EventDBLogger.error(EventDBLogger.CATE_INDEX, "컬렉션 스키마에 주키가 없음.");
				throw new FastcatSearchException("컬렉션 스키마에 주키(Primary Key)를 설정해야합니다.");
			}
			DataSequenceFile dataSequenceFile = new DataSequenceFile(collectionHomeDir, -1); //read sequence
			int	newDataSequence = (dataSequenceFile.getSequence() + 1) % DATA_SEQUENCE_CYCLE;
			
			logger.debug("dataSequence="+newDataSequence+", DATA_SEQUENCE_CYCLE="+DATA_SEQUENCE_CYCLE);
			
			File collectionDataDir = new File(IRSettings.getCollectionDataPath(collectionId, newDataSequence));
			FileUtils.deleteDirectory(collectionDataDir);
			
			//Make new CollectionHandler
			//this handler's schema or other setting can be different from working segment handler's one.
			
			int segmentNumber = 0;
			//1.xml을 unmarshar해서 sourceconfig객체로 가지고 있는다.
			//2. 
			DataSourceSetting dsSetting = IRSettings.getDatasource(collectionId, true);
			SourceReader sourceReader = SourceReaderFactory.createSourceReader(collectionId, workSchema, dsSetting, true);
			
			if(sourceReader == null){
//				EventDBLogger.error(EventDBLogger.CATE_INDEX, "데이터수집기를 생성할 수 없습니다.");
				throw new FastcatSearchException("데이터 수집기 생성중 에러발생. sourceType = "+dsSetting.sourceType);
			}
			
			/*
			 * 색인파일 생성.
			 */
			
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			CollectionConfig collectionConfig = irService.getCollectionConfig(collectionId);
			
			IndexConfig indexConfig = collectionConfig.getIndexConfig();
			File segmentDir = new File(IRSettings.getSegmentPath(collectionId, newDataSequence, segmentNumber));
			indexingLogger.info("Segment Dir = "+segmentDir.getAbsolutePath());
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
					writer.close();
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
			IRSettings.applyWorkSchemaFile(collectionId);
			
//			File collectionDir = new File(IRSettings.getCollectionHome(collectionId));
//			Schema newSchema = IRSettings.getSchema(collectionId, false);
//			CollectionHandler newHandler = new CollectionHandler(collectionId, collectionDir, newSchema, indexConfig, newDataSequence);

			CollectionHandler newHandler = irService.newCollectionHandler(collectionId, newDataSequence);
			updateAndDeleteSize = newHandler.addSegment(segmentNumber, segmentDir, null);
			updateAndDeleteSize[1] += writer.getDuplicateDocCount();//중복문서 삭제카운트
//			logger.info("== SegmentStatus ==");
//			newHandler.printSegmentStatus();
			
			newHandler.saveDataSequenceFile();
			
			/*
			 * 컬렉션 리로드
			 */
			CollectionHandler oldCollectionHandler = irService.putCollectionHandler(collectionId, newHandler);
			if(oldCollectionHandler != null){
				logger.info("## Close Previous Collection Handler");
				oldCollectionHandler.close();
			}
			
			SegmentInfo si = newHandler.getLastSegmentInfo();
			indexingLogger.info(si.toString());
//			int docSize = si.getDocCount();
			
			/*
			 * indextime 파일 업데이트.
			 */
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String startDt = sdf.format(st);
			String endDt = sdf.format(new Date());
			int duration = (int) (System.currentTimeMillis() - st);
			String durationStr = Formatter.getFormatTime(duration);
			IRSettings.storeIndextime(collectionId, "FULL", startDt, endDt, durationStr, count);
			
			/*
			 * 5초후에 캐시 클리어.
			 */
			getJobExecutor().offer(new CacheServiceRestartJob(5000));
			
			indexingLogger.info("["+collectionId+"] Full Indexing Finished! docs = "+count+", update = "+updateAndDeleteSize[0]+", delete = "+updateAndDeleteSize[1]+", time = "+durationStr);
			result = new IndexingJobResult(collectionId, segmentDir, count, updateAndDeleteSize[0], updateAndDeleteSize[1], duration);
			isSuccess = true;
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
