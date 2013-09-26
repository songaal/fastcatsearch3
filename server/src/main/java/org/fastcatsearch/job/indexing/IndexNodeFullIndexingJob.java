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

package org.fastcatsearch.job.indexing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.cluster.ClusterStrategy;
import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.ShardFullIndexer;
import org.fastcatsearch.ir.ShardIndexer;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionConfig.Shard;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.config.ShardConfig;
import org.fastcatsearch.ir.config.ShardContext;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.search.ShardHandler;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.job.CacheServiceRestartJob;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.StreamableJob;
import org.fastcatsearch.job.Job.JobResult;
import org.fastcatsearch.job.cluster.NodeCollectionReloadJob;
import org.fastcatsearch.job.cluster.NodeDirectoryCleanJob;
import org.fastcatsearch.job.cluster.StreamableClusterJob;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.task.IndexFileTransfer;
import org.fastcatsearch.util.CollectionContextUtil;
import org.fastcatsearch.util.IndexFilePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 전체색인을 수행하여 색인파일을 생성하고,
 * 해당하는 data node에 색인파일을 복사한다.
 * 
 * */
public class IndexNodeFullIndexingJob extends StreamableClusterJob {
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
			
			
			//source reader로 데이터를 읽는다.
			
			
			

			//shard 의 filter를 보면서 해당 shard indexer에 데이터를 add 한다.
			

			//shard 정보 저장.
			//collection 색인 마무리.
			
			//cache 리로드.
			
			
			
			//////////////////////////////////////////////////////////////////////////////////////////
			CollectionContext collectionContext = irService.collectionContext(collectionId).copy();
			List<Shard> shardList = collectionContext.collectionConfig().getShardConfigList();
			int shardSize = shardList.size();
			
			for(ShardContext shardContext : collectionContext.getShardContextList()){
				ShardConfig shardConfig =  shardContext.shardConfig();
				String filter = shardConfig.getFilter();
				
				ShardFilter shardFilter = new ShardFilter(filter, shardFullIndexer);
			}
			
			
			// workschema는 shard indexer에서는 보지않는다.
			Schema schema = shardContext.schema();

			if (schema.getFieldSize() == 0) {
				logger.error("[{}] Indexing Canceled. Schema field is empty. time = {}", shardContext.shardId());
				throw new FastcatSearchException("[" + shardContext.shardId() + "] Indexing Canceled. Schema field is empty.");
			}
			
			ShardFullIndexer collectionIndexer = new ShardFullIndexer(collectionContext);
			SegmentInfo segmentInfo = collectionIndexer.fullIndexing();
			RevisionInfo revisionInfo = segmentInfo.getRevisionInfo();
			//////////////////////////////////////////////////////////////////////////////////////////
			
			if(revisionInfo.getInsertCount() == 0){
				int duration = (int) (System.currentTimeMillis() - startTime);
				return new JobResult(new IndexingJobResult(collectionId, revisionInfo, duration, false));
			}
			
			//status를 바꾸고 context를 저장한다.
			collectionContext.updateCollectionStatus(IndexingType.FULL, revisionInfo, startTime, System.currentTimeMillis());
			CollectionContextUtil.saveCollectionAfterIndexing(collectionContext);
			
			/*
			 * 색인파일 원격복사.
			 */
			NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
			ClusterStrategy dataStrategy = irService.getCollectionClusterStrategy(collectionId);
			List<String> nodeIdList = dataStrategy.dataNodes();
			List<Node> nodeList = nodeService.getNodeById(nodeIdList);
			if (nodeList == null || nodeList.size() == 0) {
				throw new FastcatSearchException("색인파일을 복사할 노드가 정의되어있지 않습니다.");
			}

			String segmentId = segmentInfo.getId();
			
			IndexFilePaths collectionFilePaths = collectionContext.indexFilePaths();
			int dataSequence = collectionContext.getIndexSequence();
			File collectionDataDir = collectionFilePaths.indexDirFile(dataSequence);
			File segmentDir = collectionFilePaths.segmentFile(dataSequence, segmentId);

			// 색인전송할디렉토리를 먼저 비우도록 요청.segmentDir
			File relativeDataDir = environment.filePaths().relativise(collectionDataDir);
			NodeDirectoryCleanJob cleanJob = new NodeDirectoryCleanJob(relativeDataDir);
			boolean nodeResult = sendJobToNodeList(cleanJob, nodeService, nodeList, false);
			if(!nodeResult){
				throw new FastcatSearchException("Node Index Directory Clean Failed! Dir=[{}]", segmentDir.getPath());
			}
			
			// 색인된 Segment 파일전송.
			IndexFileTransfer indexFileTransfer = new IndexFileTransfer(environment);
			indexFileTransfer.transferDirectory(segmentDir, nodeService, nodeList);
			
			/*
			 * 데이터노드에 컬렉션 리로드 요청.
			 * 
			 * TODO 일반 노드에도 리로드필요. search노드일수가 있다.
			 * 
			 */
			NodeCollectionReloadJob reloadJob = new NodeCollectionReloadJob(collectionContext);
			nodeResult = sendJobToNodeList(reloadJob, nodeService, nodeList, false);
			if(!nodeResult){
				throw new FastcatSearchException("Node Collection Reload Failed!");
			}
			
			/*
			 * 데이터노드가 리로드 완료되었으면 인덱스노드도 리로드 시작.
			 * */
			ShardHandler collectionHandler = irService.loadCollectionHandler(collectionContext);
			ShardHandler oldCollectionHandler = irService.putCollectionHandler(collectionId, collectionHandler);
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
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		collectionId = input.readString();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(collectionId);
	}

}
