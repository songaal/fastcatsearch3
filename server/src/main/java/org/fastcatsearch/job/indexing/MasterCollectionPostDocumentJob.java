package org.fastcatsearch.job.indexing;

import org.fastcatsearch.cluster.ClusterUtils;
import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeJobResult;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.db.mapper.IndexingResultMapper;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.DynamicIndexModule;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.index.DynamicIndexer;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.job.MasterNodeJob;
import org.fastcatsearch.service.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * json 색인 작업을 모든 노드에 전송한다.
 * */
public class MasterCollectionPostDocumentJob extends MasterNodeJob {
	protected static Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");
	private static final long serialVersionUID = -9030366773507675894L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		long indexingStartTime = System.currentTimeMillis();
		String collectionId = getStringArgs(0);
        String documents = getStringArgs(1);

		Throwable throwable = null;
		IndexingResultMapper.ResultStatus resultStatus = IndexingResultMapper.ResultStatus.RUNNING;
		Object result = null;
		long startTime = System.currentTimeMillis();
		try {
			IRService irService = ServiceManager.getInstance().getService(IRService.class);

			//find index node
			CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
			CollectionContext collectionContext = irService.collectionContext(collectionId);
			if (collectionContext == null) {
				throw new FastcatSearchException("Collection [" + collectionId + "] is not exist.");
			}
			NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);

			Set<String> nodeSet = new HashSet<String>();
			nodeSet.addAll(collectionContext.collectionConfig().getDataNodeList());
			nodeSet.add(collectionContext.collectionConfig().getIndexNode());
			nodeSet.add(nodeService.getMyNode().id());
			List<String> nodeIdList = new ArrayList<String>(nodeSet);
			List<Node> nodeList = new ArrayList<Node>(nodeService.getNodeById(nodeIdList));

			CollectionPostDocumentJob postDocumentJob = new CollectionPostDocumentJob();
			NodeJobResult[] nodeResultList = ClusterUtils.sendJobToNodeList(postDocumentJob, nodeService, nodeList, true);


		} catch (Throwable e) {
			indexingLogger.error("[" + collectionId + "] Add document", e);
			throwable = e;
			resultStatus = IndexingResultMapper.ResultStatus.FAIL;
		}

		return new JobResult();
	}

}
