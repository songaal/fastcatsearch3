package org.fastcatsearch.job.management;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.cluster.ClusterUtils;
import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeJobResult;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.cluster.NodeCollectionReloadJob;
import org.fastcatsearch.job.indexing.TransferIndexFileMultiNodeJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.FilePaths;

/**
 * 지정한 source노드에서 dest 노드 리스트로 색인파일전파 및 리로드수행. 
 * */
public class CopyApplyIndexDataJob extends Job implements Streamable {

	private static final long serialVersionUID = -5275884443430362098L;

	private String collectionId;
	private String sourceNodeId;
	private List<String> destNodeIdList;

	public CopyApplyIndexDataJob() {
	}

	public CopyApplyIndexDataJob(String collectionId, String sourceNodeId, List<String> destNodeIdList) {
		this.collectionId = collectionId;
		this.sourceNodeId = sourceNodeId;
		this.destNodeIdList = destNodeIdList;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		this.collectionId = input.readString();
		this.sourceNodeId = input.readString();
		int nodeSize = input.readInt();
		this.destNodeIdList = new ArrayList<String>(nodeSize);
		for (int i = 0; i < nodeSize; i++) {
			destNodeIdList.add(input.readString());
		}
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(collectionId);
		output.writeString(sourceNodeId);
		output.writeInt(destNodeIdList.size());
		for (int i = 0; i < destNodeIdList.size(); i++) {
			output.writeString(destNodeIdList.get(i));
		}

	}

	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Node sourceNode = nodeService.getNodeById(sourceNodeId);
		if(nodeService.isMyNode(sourceNode)){
			List<Node> destNodeList = nodeService.getNodeById(destNodeIdList);
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			CollectionContext collectionContext = irService.collectionContext(collectionId);
			FilePaths indexFilePaths = collectionContext.indexFilePaths();
			File indexDir = indexFilePaths.file();
			
//			SegmentInfo segmentInfo = collectionContext.dataInfo().getLastSegmentInfo();
//			if (segmentInfo != null) {
				NodeJobResult[] nodeResultList = null;
//				String segmentId = segmentInfo.getId();
				logger.debug("Transfer index data collection[{}] >> {}", collectionId, indexDir.getAbsolutePath());

				// 색인된 Segment 파일전송.
				TransferIndexFileMultiNodeJob transferJob = new TransferIndexFileMultiNodeJob(indexDir, destNodeList);
				ResultFuture resultFuture = JobService.getInstance().offer(transferJob);
				Object obj = resultFuture.take();
				if(resultFuture.isSuccess() && obj != null){
					nodeResultList = (NodeJobResult[]) obj;
				}else{
					
				}
				
				//성공한 node만 전송.
				ArrayList<Node> nodeList = new ArrayList<Node>();
				for (int i = 0; i < nodeResultList.length; i++) {
					NodeJobResult r = nodeResultList[i];
					logger.debug("node#{} >> {}", i, r);
					if (r.isSuccess()) {
						nodeList.add(r.node());
					}else{
						logger.warn("Do not send index file to {}", r.node());
					}
				}
				
				/*
				 * 데이터노드에 컬렉션 리로드 요청.
				 */
				NodeCollectionReloadJob reloadJob = new NodeCollectionReloadJob(collectionContext);
				nodeResultList = ClusterUtils.sendJobToNodeList(reloadJob, nodeService, nodeList, false);
				for (int i = 0; i < nodeResultList.length; i++) {
					NodeJobResult r = nodeResultList[i];
					logger.debug("node#{} >> {}", i, r);
					if (r.isSuccess()) {
						logger.debug("{} Collection reload OK.", r.node());
					}else{
						logger.warn("{} Collection reload Fail.", r.node());
					}
				}
//			}
			
			
		}else{
			nodeService.sendRequest(sourceNode, this);
		}
		return new JobResult(true);
	}

}
