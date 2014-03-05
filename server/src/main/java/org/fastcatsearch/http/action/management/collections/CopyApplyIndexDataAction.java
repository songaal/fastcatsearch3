package org.fastcatsearch.http.action.management.collections;

import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.management.CopyApplyIndexDataJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/management/collections/copy-apply-index", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class CopyApplyIndexDataAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		String collectionId = request.getParameter("collectionId");
		String indexNode = request.getParameter("sourceNode");
		String destNodeListString = request.getParameter("destNodeList");
		
		List<String> destNodeIdList = new ArrayList<String>();
		if (destNodeListString != null) {
			for (String nodeStr : destNodeListString.split(",")) {
				nodeStr = nodeStr.trim();
				if (nodeStr.length() > 0) {
					destNodeIdList.add(nodeStr);
				}
			}
		}
		
		boolean isSuccess = false;
		String errorMessage = null;

		try {
			JobService jobService = ServiceManager.getInstance().getService(JobService.class);
			
			Job job = new CopyApplyIndexDataJob(collectionId, indexNode, destNodeIdList);
			ResultFuture resultFuture = jobService.offer(job);
			//결과는 기다리지 않고 실행이 시작되면 리턴.
			isSuccess = resultFuture != null;
			
		} catch (Exception e) {
			isSuccess = false;
			errorMessage = e.getMessage();
		} finally {
			ResponseWriter responseWriter = getDefaultResponseWriter(response.getWriter());
			responseWriter.object();
			responseWriter.key("success").value(isSuccess);
			if (errorMessage != null) {
				responseWriter.key("errorMessage").value(errorMessage);
			}
			responseWriter.endObject();
			responseWriter.done();
		}
	}

}
