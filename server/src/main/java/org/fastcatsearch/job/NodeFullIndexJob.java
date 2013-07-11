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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.Strings;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.data.DataService;
import org.fastcatsearch.data.DataStrategy;
import org.fastcatsearch.datasource.reader.DataSourceReader;
import org.fastcatsearch.datasource.reader.DataSourceReaderFactory;
import org.fastcatsearch.env.CollectionFilePaths;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.config.DataPlanConfig;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.index.SegmentWriter;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.log.EventDBLogger;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.task.MakeIndexFileTask;
import org.fastcatsearch.transport.common.SendFileResultFuture;
import org.fastcatsearch.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 전체색인을 수행하여 색인파일을 생성하고,
 * 해당하는 data node에 색인파일을 복사한다.
 * 
 * */
public class NodeFullIndexJob extends StreamableJob {
	private static Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");

	String collectionId;

	public NodeFullIndexJob() {
	}

	public NodeFullIndexJob(String collectionId) {
		this.collectionId = collectionId;
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {

		IndexingJobResult indexingJobResult = null;
		try {
			
			long startTime = System.currentTimeMillis();
//			IRConfig irconfig = IRSettings.getConfig(true);
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			CollectionContext collectionContext = irService.collectionContext(collectionId);
			DataPlanConfig dataPlanConfig = collectionContext.collectionConfig().getDataPlanConfig();
//			int DATA_SEQUENCE_CYCLE = dataPlanConfig.getDataSequenceCycle();
//			int DATA_SEQUENCE_CYCLE = irconfig.getInt("data.sequence.cycle");
			CollectionFilePaths collectionFilePaths = collectionContext.collectionFilePaths();
			
//			File collectionHomeDir = new File(IRSettings.getCollectionHome(collectionId));
			Schema workSchema = collectionContext.workSchema();
			if (workSchema == null){
				workSchema = collectionContext.schema();
			}

			if (workSchema.getFieldSize() == 0) {
				indexingLogger.error("[" + collectionId + "] Full Indexing Canceled. Schema field is empty. time = "
						+ Strings.getHumanReadableTimeInterval(System.currentTimeMillis() - startTime));
				throw new FastcatSearchException("[" + collectionId + "] Full Indexing Canceled. Schema field is empty. time = "
						+ Strings.getHumanReadableTimeInterval(System.currentTimeMillis() - startTime));
			}

			// 주키가 없으면 색인실패
//			if (workSchema.getIndexID() == -1) {
//				EventDBLogger.error(EventDBLogger.CATE_INDEX, "컬렉션 스키마에 주키가 없음.");
//				throw new FastcatSearchException("컬렉션 스키마에 주키(Primary Key)를 설정해야합니다.");
//			}
			
//			DataSequenceFile dataSequenceFile = new DataSequenceFile(collectionHomeDir, -1); // read
																								// sequence
//			int newDataSequence = (dataSequenceFile.getSequence() + 1) % DATA_SEQUENCE_CYCLE;

//			logger.debug("dataSequence=" + newDataSequence + ", DATA_SEQUENCE_CYCLE=" + DATA_SEQUENCE_CYCLE);
			int newDataSequence = collectionContext.getNextDataSequence();
			File collectionDataDir = collectionFilePaths.dataPath(newDataSequence).file();
			FileUtils.cleanCollectionDataDirectorys(collectionDataDir);
			
			// Make new CollectionHandler
			// this handler's schema or other setting can be different from
			// working segment handler's one.

			int segmentNumber = 0;

			DataSourceConfig dataSourceConfig = collectionContext.dataSourceConfig();
			DataSourceReader sourceReader = DataSourceReaderFactory.createSourceReader(collectionFilePaths.home(), workSchema, dataSourceConfig, null, true);
			
			if(sourceReader == null){
//				EventDBLogger.error(EventDBLogger.CATE_INDEX, "데이터수집기를 생성할 수 없습니다.");
				throw new FastcatSearchException("데이터 수집기 생성중 에러발생. sourceType = "+dataSourceConfig.getConfigType());
			}

			SegmentInfo segmentInfo = null;
			IndexConfig indexConfig = collectionContext.collectionConfig().getIndexConfig();
			File segmentDir = collectionFilePaths.segmentPath(newDataSequence, segmentNumber).file();
			indexingLogger.info("Segment Dir = {}", segmentDir.getAbsolutePath());
			SegmentWriter writer = null;
			int count = 0;
			int[] updateAndDeleteSize = {0, 0};
			
			/*
			 * 색인파일 생성.
			 */
			MakeIndexFileTask makeIndexFileTask = new MakeIndexFileTask();
			int dupCount = 0;
			try{
				dupCount = makeIndexFileTask.makeIndex(collectionId, collectionFilePaths.home().file(), workSchema, collectionDataDir, sourceReader, segmentDir);
			}finally{
				try{
					sourceReader.close();
				}catch(Exception e){
					logger.error("Error while close source reader! "+e.getMessage(),e);
				}
			}
//			CollectionHandler newHandler = new CollectionHandler(collectionId, collectionHomeDir, workSchema, IRSettings.getIndexConfig());
			CollectionHandler newHandler = irService.loadCollectionHandler(collectionId, newDataSequence);
//			int[] updateAndDeleteSize = 
			newHandler.addSegment(segmentInfo, segmentDir, null); //collection.info 파일저장.
//			newHandler.saveDataSequenceFile(); //data.sequence 파일저장.
			
			
			/*
			 * 색인파일 원격복사.
			 */
			DataService dataService = ServiceManager.getInstance().getService(DataService.class);
			DataStrategy dataStrategy = dataService.getCollectionDataStrategy(collectionId);
			List<Node> nodeList = dataStrategy.dataNodes();
			if (nodeList == null || nodeList.size() == 0) {
				throw new FastcatSearchException("색인파일을 복사할 노드가 정의되어있지 않습니다.");
			}


			NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
			//
			//TODO 색인전송할디렉토리를 먼저 비우도록 요청.segmentDir
			//
			Collection<File> files = FileUtils.listFiles(segmentDir, null, true);
			//add collection.info 파일
//			File collectionInfoFile = new File(collectionDataDir, IndexFileNames.collectionInfoFile);
//			files.add(collectionInfoFile);
			int totalFileCount = files.size();

			//TODO 순차적전송이라서 여러노드전송시 속도가 느림.해결요망.  
			for (int i = 0; i < nodeList.size(); i++) {
				Node node = nodeList.get(i);
				if(nodeService.isMyNode(node)){
					//자신에게는 전송하지 않는다.
					continue;
				}
				logger.debug("Send File Nodes [{} / {}] {}", new Object[]{i+1, nodeList.size(), node});
				Iterator<File> fileIterator = files.iterator();
				int fileCount = 1;
				while (fileIterator.hasNext()) {
					File sourceFile = fileIterator.next();
					File targetFile = environment.filePaths().path(sourceFile.getPath()).file();
					logger.debug("sourceFile >> {}", sourceFile.getPath());
					logger.debug("targetFile >> {}", targetFile.getPath());
					logger.info("[{} / {}]파일 {} 전송시작! ", new Object[] { fileCount, totalFileCount, sourceFile.getPath() });
					
					SendFileResultFuture sendFileResultFuture = nodeService.sendFile(node, sourceFile, targetFile);
					if(sendFileResultFuture != null){
						Object result = sendFileResultFuture.take();
						if (sendFileResultFuture.isSuccess()) {
							logger.info("[{} / {}]파일 {} 전송완료!", new Object[] { fileCount, totalFileCount, sourceFile.getPath() });
						} else {
							throw new FastcatSearchException("파일전송에 실패했습니다.");
						}
					}else{
						//null이라면 전송에러.
						logger.warn("디렉토리는 전송할수 없습니다.");
						break;
					}
					
					fileCount++;
				}

			}

			/*
			 * 데이터노드에 컬렉션 리로드 요청.
			 * 
			 * TODO 일반 노드에도 리로드필요. search노드일수가 있다.
			 * 
			 */
			NodeCollectionReloadJob reloadJob = new NodeCollectionReloadJob(startTime, collectionId, newDataSequence, segmentNumber);
			List<ResultFuture> resultFutureList = new ArrayList<ResultFuture>(nodeList.size());
			for (int i = 0; i < nodeList.size(); i++) {
				Node node = nodeList.get(i);
				ResultFuture resultFuture = nodeService.sendRequest(node, reloadJob);
				resultFutureList.add(resultFuture);
			}
			for (int i = 0; i < resultFutureList.size(); i++) {
				Node node = nodeList.get(i);
				ResultFuture resultFuture = resultFutureList.get(i);
				Object obj = resultFuture.take();
				if(!resultFuture.isSuccess()){
					logger.debug("리로드 결과 : {}", obj);
					throw new FastcatSearchException("컬렉션 리로드 실패. collection="+collectionId+", "+node);
				}
			}
			
			/*
			 * 데이터노드가 리로드 완료되었으면 인덱스노드도 리로드 시작.
			 * */
			CollectionHandler oldCollectionHandler = irService.putCollectionHandler(collectionId, newHandler);
			if(oldCollectionHandler != null){
				logger.info("## Close Previous Collection Handler");
				oldCollectionHandler.close();
			}
			
			SegmentInfo si = newHandler.getLastSegmentReader().segmentInfo();
			logger.info(si.toString());
			int docSize = si.getDocumentCount();
			
			/*
			 * indextime 파일 업데이트.
			 */
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			String startDt = sdf.format(startTime);
//			String endDt = sdf.format(new Date());
			int duration = (int) (System.currentTimeMillis() - startTime);
//			String durationStr = Formatter.getFormatTime(duration);
//			IRSettings.storeIndextime(collectionId, "FULL", startDt, endDt, durationStr, docSize);
			
			/*
			 * 5초후에 캐시 클리어.
			 */
			getJobExecutor().offer(new CacheServiceRestartJob(5000));
			
			int updateSize = updateAndDeleteSize[0];
			int deleteSize = updateAndDeleteSize[1] + dupCount;
			return new JobResult(new IndexingJobResult(collectionId, segmentDir, docSize, updateSize, deleteSize, duration));
			
		} catch (Exception e) {
//			EventDBLogger.error(EventDBLogger.CATE_INDEX, "전체색인에러", EventDBLogger.getStackTrace(e));
			indexingLogger.error("[" + collectionId + "] Indexing error = " + e.getMessage(), e);
			throw new FastcatSearchException("ERR-00500", e, collectionId);
		}

//		return new JobResult(indexingJobResult);
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		collectionId = input.readString();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(collectionId);
	}

}
