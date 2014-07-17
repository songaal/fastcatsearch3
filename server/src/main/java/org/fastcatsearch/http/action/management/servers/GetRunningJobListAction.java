package org.fastcatsearch.http.action.management.servers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.fastcatsearch.cluster.ClusterUtils;
import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeJobResult;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.job.management.GetRunningJobListJob;
import org.fastcatsearch.job.management.GetRunningJobListJob.JobInfo;
import org.fastcatsearch.job.management.GetRunningJobListJob.RunningJobListInfo;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.util.ResultWriterException;

@ActionMapping(value = "/management/servers/running-job-list", authority = ActionAuthority.Servers, authorityLevel = ActionAuthorityLevel.NONE)
public class GetRunningJobListAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		String nodeId = request.getParameter("nodeId");
		
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		
		ResponseWriter responseWriter = getDefaultResponseWriter(response.getWriter());
		
		List<Node> nodeList = nodeService.getNodeArrayList();
		if(nodeId != null && nodeId.length() > 0){
			List<Node> list = new ArrayList<Node>();
			for(Node node : nodeList){
				if(node.id().equals(nodeId)){
					list.add(node);
				}
			}
			nodeList = list;
		}
		
		responseWriter.object();
		GetRunningJobListJob job = new GetRunningJobListJob();
		NodeJobResult[] nodeJobResult = ClusterUtils.sendJobToNodeList(job, nodeService, nodeList, true);
		for(NodeJobResult jobResult : nodeJobResult) {
			if(jobResult.isSuccess()){
				Node node = jobResult.node();
				Object result = jobResult.result();
				if(result != null){
					RunningJobListInfo info = (RunningJobListInfo) result;
					writeRunningJobList(info, node, responseWriter);
				}
				
			}
		}
		responseWriter.endObject();
		responseWriter.done();
		
	}

	SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	private void writeRunningJobList(RunningJobListInfo info, Node node,
			ResponseWriter responseWriter) throws ResultWriterException {
		List<JobInfo> list = info.getJobInfoList();
		
		responseWriter.key(node.id()).object()
		.key("size").value(list.size())
		.key("list").array();
		for(JobInfo jobInfo : list) {
			responseWriter.object();
			responseWriter.key("jobId").value(jobInfo.getJobId())
			.key("className").value(jobInfo.getClassName());
			responseWriter.key("args").value(jobInfo.getArgs());
			responseWriter.key("isScheduled").value(jobInfo.isScheduled());
			responseWriter.key("noResult").value(jobInfo.isNoResult());
			responseWriter.key("startTime").value(timeFormat.format(new Date(jobInfo.getStartTime())));
			responseWriter.key("endTime").value(jobInfo.getEndTime() > 0 ? timeFormat.format(new Date(jobInfo.getEndTime())) : "");
			responseWriter.endObject();
		}
		responseWriter.endArray()
		.endObject();
		
	}

}
