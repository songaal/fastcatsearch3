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
import org.fastcatsearch.ir.CollectionIndexer;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.config.DataPlanConfig;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.SingleSourceConfig;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.SegmentWriter;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.log.EventDBLogger;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.CollectionFilePaths;
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
			indexingLogger.info("[{}] Add Indexing Start! (forceAppend)", collectionId);
		}else if(forceSeparate){
			indexingLogger.info("[{}] Add Indexing Start! (forceSeparate)", collectionId);
		}else{
			indexingLogger.info("[{}] Add Indexing Start!", collectionId);
		}
		
		long st = System.currentTimeMillis(); 
		try {
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			
			
			CollectionHandler workingHandler = irService.collectionHandler(collectionId);
			if(workingHandler == null){
				indexingLogger.error("[{}] CollectionHandler is not running!", collectionId);
				EventDBLogger.error(EventDBLogger.CATE_INDEX, "컬렉션 "+collectionId+"가 서비스중이 아님.");
				throw new FastcatSearchException("## ["+collectionId+"] CollectionHandler is not running...");
			}
			
			
			
			boolean isAppend = false;
			SegmentInfo currentSegmentInfo = workingHandler.getLastSegmentReader().segmentInfo();
			if(currentSegmentInfo == null){
				indexingLogger.error("[{}] has no segment!  Do full-indexing first!!", collectionId);
				return null;
			}
			
			//copy한다.
			CollectionContext collectionContext = irService.collectionContext(collectionId).copy();
			CollectionFilePaths collectionFilePaths = collectionContext.collectionFilePaths();
			int dataSequence = workingHandler.getDataSequence();
			CollectionIndexer collectionIndexer = new CollectionIndexer(collectionContext);
			SegmentInfo segmentInfo = collectionIndexer.addIndex(workingHandler);
			
			File segmentDir = collectionFilePaths.segmentFile(dataSequence, segmentInfo.getId());
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String startDt = sdf.format(st);
			String endDt = sdf.format(new Date());
			int duration = (int) (System.currentTimeMillis() - st);
			String durationStr = Formatter.getFormatTime(duration);
			
//			collectionContext.updateCollectionStatus(IndexingType.ADD_INDEXING, dataSequence, count, st , System.currentTimeMillis());
//			IRSettings.storeIndextime(collectionId, "INC", startDt, endDt, durationStr, count);
			
			//5초후에 캐시 클리어.
			getJobExecutor().offer(new CacheServiceRestartJob(5000));
			
			indexingLogger.info("[{}] Incremental Indexing Finished! docs = {}, update = {}, delete = {}, time = {}", collectionId, count, 0, 0, durationStr);
			
			return new JobResult(new IndexingJobResult(collectionId, segmentDir, count.intValue(), 0, 0, duration));
			
		} catch (Exception e) {
//			EventDBLogger.error(EventDBLogger.CATE_INDEX, "증분색인에러", EventDBLogger.getStackTrace(e));
			throw new FastcatSearchException("ERR-00501", e, collectionId);
		}
		
		
		
	}

}
