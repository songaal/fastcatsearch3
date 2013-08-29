package org.fastcatsearch.http.action.index;

import java.io.Writer;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.job.ShardFullIndexingJob;
import org.fastcatsearch.util.ResultWriter;

public class RunIndexingAction extends AuthAction {

	public RunIndexingAction(String type) {
		super(type);
	}

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		String collectionId = request.getParameter("collectionId");
		String shardId = request.getParameter("shardId");
		

		ShardFullIndexingJob shardFullIndexingJob = new ShardFullIndexingJob();
		shardFullIndexingJob.setArgs(new String[]{collectionId, shardId});
		
		ResultFuture jobResult = JobService.getInstance().offer(shardFullIndexingJob);
		
		Writer writer = response.getWriter();
		ResultWriter resultWriter = getDefaultResultWriter(writer);
		resultWriter
		.object()
		.key("collectionId").value("sample")
		.key("shardId").value("sample1");
		
		if(jobResult != null){
			resultWriter.key("status").value("0");
		}else{
			resultWriter.key("status").value("1");
		}
		resultWriter.endObject();
		resultWriter.done();
	}

}
