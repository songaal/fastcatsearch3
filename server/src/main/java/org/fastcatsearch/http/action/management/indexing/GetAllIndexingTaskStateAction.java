package org.fastcatsearch.http.action.management.indexing;

import java.io.Writer;
import java.util.List;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.job.state.IndexingTaskKey;
import org.fastcatsearch.job.state.IndexingTaskState;
import org.fastcatsearch.job.state.TaskState;
import org.fastcatsearch.job.state.TaskStateService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/indexing/all-state", authority=ActionAuthority.Collections)
public class GetAllIndexingTaskStateAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		//TODO collection의 index node인지 확인하고 해당 노드가 아니면 전달하여 받아온다.
		//해당노드이면 그대로 수행한다.
		TaskStateService taskStateService = ServiceManager.getInstance().getService(TaskStateService.class);
//		IndexingTaskState indexingTaskState = (IndexingTaskState) taskStateService.get(new IndexingTaskKey(collectionId, IndexingType.valueOf(indexingType)));
		
		List<TaskState> taskStateList = taskStateService.getTaskStateList(IndexingTaskState.class);
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		resultWriter.object().key("indexingState").array();
		
		if(taskStateList != null){
			for(TaskState taskState : taskStateList){
				IndexingTaskState indexingTaskState = (IndexingTaskState) taskState;
				IndexingTaskKey indexingTaskKey = (IndexingTaskKey) indexingTaskState.taskKey();
				
				resultWriter.object()
				.key("isScheduled").value(indexingTaskKey.isScheduled())
				.key("collectionId").value(indexingTaskKey.collectionId())
				.key("indexingType").value(indexingTaskKey.indexingType())
				.key("state").value(indexingTaskState.getState()) //색인, 전파등...
				.key("count").value(indexingTaskState.getDocumentCount())
				.key("startTime").value(indexingTaskState.getStartTime())
				.key("elapsed").value(indexingTaskState.getElapsedTime())
				.endObject();
			}
		}
		resultWriter.endArray().endObject();
		
		resultWriter.done();
		
		
	}

}
