package org.fastcatsearch.http.action.management.common;

import java.io.Writer;
import java.util.List;
import java.util.Map.Entry;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.job.state.TaskKey;
import org.fastcatsearch.job.state.TaskState;
import org.fastcatsearch.job.state.TaskStateService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/management/common/all-task-state")
public class GetAllTaskStateAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		//TODO admin node인지 확인하고 해당 노드가 아니면 전달하여 받아온다.
		//해당노드이면 그대로 수행한다.
		TaskStateService taskStateService = ServiceManager.getInstance().getService(TaskStateService.class);
		
		List<Entry<TaskKey, TaskState>> taskEntryList = taskStateService.getTaskEntryList(null);
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		resultWriter.object().key("taskState").array();
		
		if(taskEntryList != null){
			for(Entry<TaskKey, TaskState> entry : taskEntryList){
				TaskKey taskKey = entry.getKey();
				TaskState taskState = entry.getValue();
				resultWriter.object()
				.key("summary").value(taskKey.getSummary() + " " + taskState.getSummary())
				.key("isScheduled").value(taskState.isScheduled())
				.key("progress").value(taskState.getProgressRate())
				.key("startTime").value(taskState.getStartTime())
				.key("elapsed").value(taskState.getElapsedTime())
				.endObject();
			}
		}
		resultWriter.endArray().endObject();
		
		resultWriter.done();
		
		
	}

}
