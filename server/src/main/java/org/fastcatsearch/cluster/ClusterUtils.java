package org.fastcatsearch.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.job.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterUtils {
	
	private static Logger logger = LoggerFactory.getLogger(ClusterUtils.class);
	
	public static NodeJobResult[] sendJobToNodeIdSet(Job job, NodeService nodeService, Set<String> nodeIdSet, boolean includeMyNode) {
		List<String> list = new ArrayList<String>();
		list.addAll(nodeIdSet);
		return sendJobToNodeIdList(job, nodeService, list, includeMyNode);
	}
	public static NodeJobResult[] sendJobToNodeSet(Job job, NodeService nodeService, Set<Node> nodeSet, boolean includeMyNode) {
		List<Node> list = new ArrayList<Node>();
		list.addAll(nodeSet);
		return sendJobToNodeList(job, nodeService, list, includeMyNode);
	}
	
	public static NodeJobResult[] sendJobToNodeIdList(Job job, NodeService nodeService, List<String> nodeIdList, boolean includeMyNode) {
		return ClusterUtils.sendJobToNodeList(job, nodeService, nodeService.getNodeById(nodeIdList), includeMyNode);
	}
	public static NodeJobResult[] sendJobToNodeList(Job job, NodeService nodeService, List<Node> nodeList, boolean includeMyNode) {
		
		NodeJobResult[] resultList = new NodeJobResult[nodeList.size()];
		List<ResultFuture> resultFutureList = new ArrayList<ResultFuture>(nodeList.size());
		for (int i = 0; i < nodeList.size(); i++) {
			Node node = nodeList.get(i);
			if(!includeMyNode && nodeService.isMyNode(node)){
				//자신에게는 실행요청하지 않음.
				resultFutureList.add(null);
				continue;
			}
			ResultFuture resultFuture = nodeService.sendRequest(node, job);
			if(resultFuture == null){
				//네트워크 장애 등으로 전송실패.
				logger.error("{} 으로 전송하지 못했습니다.", nodeList.get(i));
			}
			resultFutureList.add(resultFuture);
		}
		for (int i = 0; i < resultFutureList.size(); i++) {
			Node node = nodeList.get(i);
			ResultFuture resultFuture = resultFutureList.get(i);
			if(resultFuture != null){
				Object obj = resultFuture.take();
				resultList[i] = new NodeJobResult(node, obj, resultFuture.isSuccess());
			}else{
				resultList[i] = new NodeJobResult(node, null, false);
			}
		}
		return resultList;
	}
}
