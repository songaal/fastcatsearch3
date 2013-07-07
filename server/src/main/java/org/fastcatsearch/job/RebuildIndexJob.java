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

import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.DataPlanConfig;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.DataSequenceFile;
import org.fastcatsearch.ir.search.SegmentInfo;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.IRSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RebuildIndexJob extends Job {
	private static Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");

	public static void main(String[] args) throws FastcatSearchException {
		String homePath = args[0];
		String collection = args[1];
		IRSettings.setHome(homePath);
		
		RebuildIndexJob job = new RebuildIndexJob();
		job.setArgs(new String[]{collection});
		job.run();
	}
	
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		String[] args = getStringArrayArgs();
		String collection = args[0];
		indexingLogger.info("["+collection+"] Rebuild Indexing Start!");
		
		long st = System.currentTimeMillis(); 
		try {
//			IRConfig irconfig = IRSettings.getConfig(true);
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			DataPlanConfig dataPlanConfig = irService.getCollectionConfig(collection).getDataPlanConfig();
			int DATA_SEQUENCE_CYCLE = dataPlanConfig.getDataSequenceCycle();
//			int DATA_SEQUENCE_CYCLE = irconfig.getInt("data.sequence.cycle");
			
			String collectionHomeDir = IRSettings.getCollectionHome(collection);
			Schema workSchema = IRSettings.getWorkSchema(collection);
			if(workSchema == null)
				workSchema = IRSettings.getSchema(collection, false);
			
			DataSequenceFile dataSequenceFile = new DataSequenceFile(new File(collectionHomeDir), -1); //read sequence
			int	newDataSequence = (dataSequenceFile.getSequence() + 1) % DATA_SEQUENCE_CYCLE;
			
			logger.debug("dataSequence="+newDataSequence+", DATA_SEQUENCE_CYCLE="+DATA_SEQUENCE_CYCLE);
			
			//DO NOT DELETE rebuild dir
//			File collectionDataDir = new File(IRSettings.getCollectionDataPath(collection, newDataSequence));
//			FileUtils.deleteDirectory(collectionDataDir);
			
			//Make new CollectionHandler
			//this handler's schema or other setting can be different from working segment handler's one. 
			
			int segmentNumber = 0;
			
			File segmentDir = new File(IRSettings.getCollectionSegmentPath(collection, newDataSequence, segmentNumber));
			indexingLogger.info("Segment Dir = "+segmentDir.getAbsolutePath());
//			SegmentRebuilder writer = new SegmentRebuilder(workSchema, segmentDir);
//			writer.indexDocument();
//			writer.close();
			
			//apply schema setting
			IRSettings.applyWorkSchemaFile(collection);
			
			Schema newSchema = IRSettings.getSchema(collectionHomeDir, false);
//			CollectionHandler newHandler = new CollectionHandler(collection, new File(collectionHomeDir), newSchema, IRSettings.getIndexConfig());
			CollectionHandler newHandler = irService.loadCollectionHandler(collection, -1);
			newHandler.addSegment(segmentNumber, segmentDir, null);
			
			newHandler.saveDataSequenceFile();
			
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
			String durationStr = Formatter.getFormatTime(System.currentTimeMillis() - st);
			IRSettings.storeIndextime(collection, "REBUILD", startDt, endDt, durationStr, docSize);
			
		} catch (Exception e) {
			indexingLogger.error("["+collection+"] Rebuilding error = "+e.getMessage(),e);
			throw new FastcatSearchException("ERR-00502", e, collection);
		}
		
		indexingLogger.info("["+collection+"] Rebuild Indexing Finished! time = "+Formatter.getFormatTime(System.currentTimeMillis() - st));
		
		return new JobResult(true);
	}


}
