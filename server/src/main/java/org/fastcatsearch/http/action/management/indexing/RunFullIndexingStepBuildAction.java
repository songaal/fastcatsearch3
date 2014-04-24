package org.fastcatsearch.http.action.management.indexing;

import java.io.Writer;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.job.indexing.MasterCollectionFullIndexingStepBuildJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/indexing/full/build-indexing/run", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.WRITABLE )
public class RunFullIndexingStepBuildAction extends AuthAction {
	
	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		String collectionId = request.getParameter("collectionId");

		JobService jobService = ServiceManager.getInstance().getService(JobService.class);
		
		MasterCollectionFullIndexingStepBuildJob masterCollectionIndexingJob = new MasterCollectionFullIndexingStepBuildJob();
		masterCollectionIndexingJob.setArgs(collectionId);
		
		ResultFuture jobResult = jobService.offer(masterCollectionIndexingJob);
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		resultWriter
		.object()
		.key("collectionId").value(collectionId);
		
		if(jobResult != null){
			resultWriter.key("status").value("0");
		}else{
			resultWriter.key("status").value("1");
		}
		resultWriter.endObject();
		resultWriter.done();
	}

}
