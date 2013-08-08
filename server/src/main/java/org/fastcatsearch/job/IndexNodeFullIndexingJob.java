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
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.CollectionIndexer;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.config.DataPlanConfig;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.SingleSourceConfig;
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
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.task.IndexFileTransfer;
import org.fastcatsearch.task.MakeIndexFileTask;
import org.fastcatsearch.transport.common.SendFileResultFuture;
import org.fastcatsearch.util.CollectionContextUtil;
import org.fastcatsearch.util.CollectionFilePaths;
import org.fastcatsearch.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 전체색인을 수행하여 색인파일을 생성하고,
 * 해당하는 data node에 색인파일을 복사한다.
 * 
 * */
public class IndexNodeFullIndexingJob extends StreamableJob {
	private static final long serialVersionUID = -4686760271693082945L;

	private static Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");

	private String collectionId;

	public IndexNodeFullIndexingJob() {
	}

	public IndexNodeFullIndexingJob(String collectionId) {
		this.collectionId = collectionId;
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {

		try {
			
			long startTime = System.currentTimeMillis();
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			
			/*
			 * 색인파일 생성.
			 */
			//////////////////////////////////////////////////////////////////////////////////////////
			CollectionContext collectionContext = irService.collectionContext(collectionId).copy();
			CollectionIndexer collectionIndexer = new CollectionIndexer(collectionContext);
			SegmentInfo segmentInfo = collectionIndexer.fullIndexing();
			RevisionInfo revisionInfo = segmentInfo.getRevisionInfo();
			//////////////////////////////////////////////////////////////////////////////////////////
			
			//status를 바꾸고 context를 저장한다.
			collectionContext.updateCollectionStatus(IndexingType.FULL, revisionInfo, startTime, System.currentTimeMillis());
			CollectionContextUtil.saveAfterIndexing(collectionContext);
			
			/*
			 * 색인파일 원격복사.
			 */
			DataService dataService = ServiceManager.getInstance().getService(DataService.class);
			DataStrategy dataStrategy = dataService.getCollectionDataStrategy(collectionId);
			List<Node> nodeList = dataStrategy.dataNodes();
			if (nodeList == null || nodeList.size() == 0) {
				throw new FastcatSearchException("색인파일을 복사할 노드가 정의되어있지 않습니다.");
			}

			String segmentId = segmentInfo.getId();
			NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
			CollectionFilePaths collectionFilePaths = collectionContext.collectionFilePaths();
			int dataSequence = collectionContext.getDataSequence();
			File collectionDataDir = collectionFilePaths.dataFile(dataSequence);
			File segmentDir = collectionFilePaths.segmentFile(dataSequence, segmentId);

			// 색인전송할디렉토리를 먼저 비우도록 요청.segmentDir
			File relativeDataDir = environment.filePaths().relativise(collectionDataDir);
			NodeDirectoryCleanJob cleanJob = new NodeDirectoryCleanJob(relativeDataDir);
			boolean nodeResult = sendJobToNodeList(cleanJob, nodeService, nodeList);
			if(!nodeResult){
				throw new FastcatSearchException("Node Index Directory Clean Failed! Dir=[{}]", segmentDir.getPath());
			}
			
			// 색인된 Segment 파일전송.
			IndexFileTransfer indexFileTransfer = new IndexFileTransfer(environment);
			indexFileTransfer.tranferSegmentDir(collectionDataDir, segmentDir, nodeService, nodeList);
			
			/*
			 * 데이터노드에 컬렉션 리로드 요청.
			 * 
			 * TODO 일반 노드에도 리로드필요. search노드일수가 있다.
			 * 
			 */
			NodeCollectionReloadJob reloadJob = new NodeCollectionReloadJob(collectionContext);
			nodeResult = sendJobToNodeList(reloadJob, nodeService, nodeList);
			if(!nodeResult){
				throw new FastcatSearchException("Node Collection Reload Failed!");
			}
			
			/*
			 * 데이터노드가 리로드 완료되었으면 인덱스노드도 리로드 시작.
			 * */
			CollectionHandler collectionHandler = irService.loadCollectionHandler(collectionContext);
			CollectionHandler oldCollectionHandler = irService.putCollectionHandler(collectionId, collectionHandler);
			if (oldCollectionHandler != null) {
				logger.info("## [{}] Close Previous Collection Handler", collectionContext.collectionId());
				oldCollectionHandler.close();
			}
			
			int duration = (int) (System.currentTimeMillis() - startTime);
			
			/*
			 * 캐시 클리어.
			 */
			getJobExecutor().offer(new CacheServiceRestartJob());
			
			return new JobResult(new IndexingJobResult(collectionId, revisionInfo, duration));
			
		} catch (Exception e) {
			indexingLogger.error("[" + collectionId + "] Indexing error = " + e.getMessage(), e);
			throw new FastcatSearchException("ERR-00500", e, collectionId);
		}

	}
	
	private boolean sendJobToNodeList(Job job, NodeService nodeService, List<Node> nodeList) throws FastcatSearchException{
		List<ResultFuture> resultFutureList = new ArrayList<ResultFuture>(nodeList.size());
		for (int i = 0; i < nodeList.size(); i++) {
			Node node = nodeList.get(i);
			ResultFuture resultFuture = nodeService.sendRequest(node, job);
			resultFutureList.add(resultFuture);
		}
		for (int i = 0; i < resultFutureList.size(); i++) {
			Node node = nodeList.get(i);
			ResultFuture resultFuture = resultFutureList.get(i);
			Object obj = resultFuture.take();
			if(!resultFuture.isSuccess()){
				logger.debug("[{}] job 결과 : {}", node, obj);
//				throw new FastcatSearchException("작업실패 collection="+collectionId+", "+node);
				return false;
			}
		}
		return true;
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
