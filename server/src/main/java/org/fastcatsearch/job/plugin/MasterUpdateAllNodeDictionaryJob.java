package org.fastcatsearch.job.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.cluster.ClusterUtils;
import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeJobResult;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.job.MasterNodeJob;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.analysis.AnalysisPlugin;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.task.IndexFileTransfer;

public class MasterUpdateAllNodeDictionaryJob extends MasterNodeJob {

	private static final long serialVersionUID = 1289076529665937727L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		String pluginId = getStringArgs();
		PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
		Plugin plugin = pluginService.getPlugin(pluginId);
		AnalysisPlugin analysisPlugin = null;
		if (plugin != null && plugin instanceof AnalysisPlugin) {
			analysisPlugin = (AnalysisPlugin) plugin;
		}else{
			return new JobResult(false);
		}
		
		
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		List<Node> nodeList = nodeService.getNodeArrayList();
		
		/*
		 * 1. 사전파일을 보낸다.
		 * */
		List<Node> transferNodeList = new ArrayList<Node>();
		IndexFileTransfer indexFileTransfer = new IndexFileTransfer(environment);
		
		for(Node node : nodeList){
			if(nodeService.isMyNode(node)){
				continue;
			}
			CheckPluginExistsJob checkPluginExistsJob = new CheckPluginExistsJob();
			checkPluginExistsJob.setArgs(pluginId);
			ResultFuture resultFuture = nodeService.sendRequest(node, checkPluginExistsJob);
			if (resultFuture != null) {
				Object result = resultFuture.take();
				if(resultFuture.isSuccess() && result != null){
					if((Boolean) result){
						transferNodeList.add(node);
					}
				}
			}
		}
		
		//FIXME:살아있는노드가 1개 (자기자신) 인경우 검사를 하지 않는 방향으로..
		//if(transferNodeList.size() == 0){
		//	return new JobResult(false);
		//}
		
		File directory = analysisPlugin.getDictionaryDirectory();
		indexFileTransfer.transferDirectory(directory, nodeService, transferNodeList);
		
		/*
		 * 2. 사전을 업데이트 한다.
		 * */
		UpdateDictionaryJob updateDictionaryJob = new UpdateDictionaryJob();
		updateDictionaryJob.setArgs(pluginId);
		
		NodeJobResult[] resultList = ClusterUtils.sendJobToNodeList(updateDictionaryJob, nodeService, nodeList, true);
		boolean isSuccess = false;
		if(resultList != null){
			for(NodeJobResult result : resultList){
				if(result.isSuccess()){
					if((Boolean) result.result()){
						isSuccess = true;
					}
				}
			}
		}
		return new JobResult(isSuccess);
	}

}
