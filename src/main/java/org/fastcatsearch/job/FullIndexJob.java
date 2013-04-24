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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.collector.SourceReaderFactory;
import org.fastcatsearch.control.JobController;
import org.fastcatsearch.control.JobException;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.DataSourceSetting;
import org.fastcatsearch.ir.config.IRConfig;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.ir.config.Schema;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.SegmentWriter;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.DataSequenceFile;
import org.fastcatsearch.ir.search.SegmentInfo;
import org.fastcatsearch.ir.source.SourceReader;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.job.result.JobResultIndex;
import org.fastcatsearch.log.EventDBLogger;
import org.fastcatsearch.service.IRService;
import org.fastcatsearch.service.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FullIndexJob extends Job {
	private static Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");
			
	public static void main(String[] args) throws JobException, ServiceException {
		String homePath = args[0];
		String collection = args[1];
		IRSettings.setHome(homePath);
		
		FullIndexJob job = new FullIndexJob();
		job.setArgs(new String[]{collection});
		job.run();
	}
	
	
	@Override
	public JobResultIndex run0() throws JobException, ServiceException {
		String[] args = getStringArrayArgs();
		String collection = (String)args[0];
		indexingLogger.info("["+collection+"] Full Indexing Start!");
		
		long st = System.currentTimeMillis();
		try {
			IRConfig irconfig = IRSettings.getConfig(true);
			int DATA_SEQUENCE_CYCLE = irconfig.getInt("data.sequence.cycle");
			
			String collectionHomeDir = IRSettings.getCollectionHome(collection);
			Schema workSchema = IRSettings.getWorkSchema(collection, true, false);
			if(workSchema == null)
				workSchema = IRSettings.getSchema(collection, false);
			
			if(workSchema.getFieldSize() == 0){
				indexingLogger.error("["+collection+"] Full Indexing Canceled. Schema field is empty. time = "+Formatter.getFormatTime(System.currentTimeMillis() - st));
				return null;
			}
			
			//주키가 없으면 색인실패
			if(workSchema.getIndexID() == -1){
				EventDBLogger.error(EventDBLogger.CATE_INDEX, "컬렉션 스키마에 주키가 없음.");
				throw new JobException("컬렉션 스키마에 주키(Primary Key)를 설정해야합니다.");
			}
			DataSequenceFile dataSequenceFile = new DataSequenceFile(collectionHomeDir, -1); //read sequence
			int	newDataSequence = (dataSequenceFile.getSequence() + 1) % DATA_SEQUENCE_CYCLE;
			
			logger.debug("dataSequence="+newDataSequence+", DATA_SEQUENCE_CYCLE="+DATA_SEQUENCE_CYCLE);
			
			File collectionDataDir = new File(IRSettings.getCollectionDataPath(collection, newDataSequence));
			FileUtils.deleteDirectory(collectionDataDir);
			
			//Make new CollectionHandler
			//this handler's schema or other setting can be different from working segment handler's one.
			
			int segmentNumber = 0;
			
			DataSourceSetting dsSetting = IRSettings.getDatasource(collection, true);
			SourceReader sourceReader = SourceReaderFactory.createSourceReader(collection, workSchema, dsSetting, true);
			
			if(sourceReader == null){
				EventDBLogger.error(EventDBLogger.CATE_INDEX, "데이터수집기를 생성할 수 없습니다.");
				throw new JobException("데이터 수집기 생성중 에러발생. sourceType = "+dsSetting.sourceType);
			}
			
			File segmentDir = new File(IRSettings.getSegmentPath(collection, newDataSequence, segmentNumber));
			indexingLogger.info("Segment Dir = "+segmentDir.getAbsolutePath());
			SegmentWriter writer = null;
			int count = 0;
			int[] updateAndDeleteSize = {0, 0};
			
			try{
//				writer = new SegmentWriter(workSchema, sourceReader, segmentDir);
				writer = new SegmentWriter(workSchema, segmentDir);
				
				
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
				EventDBLogger.error(EventDBLogger.CATE_INDEX, "세그먼트생성에러발생.", EventDBLogger.getStackTrace(e));
				logger.error("SegmentWriter indexDocument Exception! "+e.getMessage(),e);
				throw e;
			}finally{
				try{
					writer.close();
				}catch(Exception e){
					logger.error("Error while close segment writer! "+e.getMessage(),e);
					e.printStackTrace();
				}
				try{
					sourceReader.close();
				}catch(Exception e){
					logger.error("Error while close source reader! "+e.getMessage(),e);
				}
			}
			
			if(count == 0){
				indexingLogger.info("["+collection+"] Full Indexing Canceled due to no documents. time = "+Formatter.getFormatTime(System.currentTimeMillis() - st));
				return null;
			}
			
			//apply schema setting
			IRSettings.applyWorkSchemaFile(collection);
			
			CollectionHandler newHandler = new CollectionHandler(collection, newDataSequence);
			updateAndDeleteSize = newHandler.addSegment(segmentNumber, null);
			updateAndDeleteSize[1] += writer.getDuplicateDocCount();//중복문서 삭제카운트
//			logger.info("== SegmentStatus ==");
//			newHandler.printSegmentStatus();
			
			newHandler.saveDataSequenceFile();
			
			IRService irService = IRService.getInstance();
			CollectionHandler oldCollectionHandler = irService.putCollectionHandler(collection, newHandler);
			if(oldCollectionHandler != null){
				logger.info("## Close Previous Collection Handler");
				oldCollectionHandler.close();
			}
			
			SegmentInfo si = newHandler.getLastSegmentInfo();
			indexingLogger.info(si.toString());
			int docSize = si.getDocCount();
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String startDt = sdf.format(st);
			String endDt = sdf.format(new Date());
			int duration = (int) (System.currentTimeMillis() - st);
			String durationStr = Formatter.getFormatTime(duration);
			IRSettings.storeIndextime(collection, "FULL", startDt, endDt, durationStr, count);
			
			//5초후에 캐시 클리어.
			JobController.getInstance().offer(new CacheServiceRestartJob(5000));
			
			indexingLogger.info("["+collection+"] Full Indexing Finished! docs = "+count+", update = "+updateAndDeleteSize[0]+", delete = "+updateAndDeleteSize[1]+", time = "+durationStr);
			
			return new JobResultIndex(collection, count, updateAndDeleteSize[0], updateAndDeleteSize[1], duration);
			
		} catch (IOException e) {
			EventDBLogger.error(EventDBLogger.CATE_INDEX, "전체색인에러", EventDBLogger.getStackTrace(e));
			indexingLogger.error("["+collection+"] Indexing error = "+e.getMessage(),e);
			throw new JobException(e);
		} catch (SettingException e) {
			EventDBLogger.error(EventDBLogger.CATE_INDEX, "전체색인에러", EventDBLogger.getStackTrace(e));
			indexingLogger.error("["+collection+"] Indexing error = "+e.getMessage(),e);
			throw new JobException(e);
		} catch (IRException e) {
			EventDBLogger.error(EventDBLogger.CATE_INDEX, "전체색인에러", EventDBLogger.getStackTrace(e));
			indexingLogger.error("["+collection+"] Indexing error = "+e.getMessage(),e);
			throw new JobException(e);
		}
		
		
	}


}
