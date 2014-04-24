package org.fastcatsearch.http.action.management.indexing;

import java.io.Writer;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.job.state.IndexingTaskKey;
import org.fastcatsearch.job.state.IndexingTaskState;
import org.fastcatsearch.job.state.TaskStateService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/indexing/task-state", authority = ActionAuthority.Collections)
public class GetIndexingTaskStateAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		String collectionId = request.getParameter("collectionId");
		
		TaskStateService taskStateService = ServiceManager.getInstance().getService(TaskStateService.class);
		IndexingTaskKey indexingTaskKey = new IndexingTaskKey(collectionId);
		IndexingTaskState indexingTaskState = (IndexingTaskState) taskStateService.getTaskState(indexingTaskKey);
//		logger.debug("indexingTaskState1 > {}",indexingTaskState);
//		logger.debug("indexingTaskState2 > {}",indexingTaskState);
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
		resultWriter.object().key("indexingState")
		.object();
		
		if(indexingTaskState != null){
			resultWriter
			.key("collectionId").value(collectionId)
			.key("indexingType").value(indexingTaskState.getIndexingType().name())
			.key("isScheduled").value(indexingTaskState.isScheduled())
			.key("state").value(indexingTaskState.getState()) //색인, 전파등...
			.key("step").value(indexingTaskState.getStep())
			.key("count").value(indexingTaskState.getDocumentCount())
			.key("startTime").value(indexingTaskState.getStartTime())
			.key("endTime").value(indexingTaskState.getEndTime())
			.key("elapsed").value(indexingTaskState.getElapsedTime());
		}
		
		resultWriter.endObject().endObject();
		
		resultWriter.done();
		
		
	}

}
