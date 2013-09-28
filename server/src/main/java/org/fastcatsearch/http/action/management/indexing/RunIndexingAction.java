package org.fastcatsearch.http.action.management.indexing;

import java.io.Writer;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.job.indexing.CollectionFullIndexingJob;
import org.fastcatsearch.util.ResultWriter;

@ActionMapping("/indexing/run")
public class RunIndexingAction extends AuthAction {
	
	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		String collectionId = request.getParameter("collectionId");
		

		CollectionFullIndexingJob collectionFullIndexingJob = new CollectionFullIndexingJob();
		collectionFullIndexingJob.setArgs(new String[] { collectionId });
		
		ResultFuture jobResult = JobService.getInstance().offer(collectionFullIndexingJob);
		
		Writer writer = response.getWriter();
		ResultWriter resultWriter = getDefaultResultWriter(writer);
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
