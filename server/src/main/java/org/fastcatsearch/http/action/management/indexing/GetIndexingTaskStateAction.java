package org.fastcatsearch.http.action.management.indexing;

import java.io.Writer;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.job.state.IndexingTaskKey;
import org.fastcatsearch.job.state.IndexingTaskState;
import org.fastcatsearch.job.state.TaskStateService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/indexing/task-state")
public class GetIndexingTaskStateAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		String collectionId = request.getParameter("collectionId");
		
		//TODO collection의 index node인지 확인하고 해당 노드가 아니면 전달하여 받아온다.
		//해당노드이면 그대로 수행한다.
		TaskStateService taskStateService = ServiceManager.getInstance().getService(TaskStateService.class);
		IndexingTaskState indexingTaskState = (IndexingTaskState) taskStateService.get(new IndexingTaskKey(collectionId, IndexingType.FULL));
		logger.debug("indexingTaskState1 > {}",indexingTaskState);
		if(indexingTaskState == null){
			indexingTaskState = (IndexingTaskState) taskStateService.get(new IndexingTaskKey(collectionId, IndexingType.ADD));
		}
		logger.debug("indexingTaskState2 > {}",indexingTaskState);
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
		resultWriter.object().key("indexingState")
		.object();
		
		if(indexingTaskState != null){
			IndexingTaskKey indexingTaskKey = (IndexingTaskKey) indexingTaskState.taskKey();
			
			resultWriter.key("isScheduled").value(indexingTaskKey.isScheduled())
			.key("collectionId").value(collectionId)
			.key("indexingType").value(indexingTaskKey.indexingType())
			.key("state").value(indexingTaskState.getState()) //색인, 전파등...
			.key("count").value(indexingTaskState.getDocumentCount())
			.key("startTime").value(indexingTaskState.getStartTime())
			.key("elapsed").value(indexingTaskState.getElapsedTime());
		}
		
		resultWriter.endObject().endObject();
		
		resultWriter.done();
		
		
	}

}
