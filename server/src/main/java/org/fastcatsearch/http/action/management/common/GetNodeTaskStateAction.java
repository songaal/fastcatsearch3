package org.fastcatsearch.http.action.management.common;

import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
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

@ActionMapping("/management/common/node-task-state")
public class GetNodeTaskStateAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		String nodeId = request.getParameter("nodeId");
		
		TaskStateService taskStateService = ServiceManager.getInstance().getService(TaskStateService.class);
		
		Map<TaskKey, TaskState> taskMap = taskStateService.getNodeTaskMap(nodeId);
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		resultWriter.object().key("taskState")
		.array();
		
		if(taskMap != null) {
			Iterator<Map.Entry<TaskKey,TaskState>> iterator = taskMap.entrySet().iterator();
			while(iterator.hasNext()) {
				Entry<TaskKey,TaskState> entry = iterator.next();
				TaskKey taskKey = entry.getKey();
				TaskState taskState = entry.getValue();
				resultWriter.object()
				.key("summary").value(taskKey.getSummary())
				.key("isScheduled").value(taskState.isScheduled())
				.key("state").value(taskState.getState())
				.key("step").value(taskState.getStep())
				.key("startTime").value(taskState.getStartTime())
				.key("endTime").value(taskState.getEndTime())
				.key("elapsed").value(taskState.getElapsedTime());
				resultWriter.endObject();
				
			}
		}
		
		resultWriter.endArray().endObject();
		
		resultWriter.done();
		
		
	}

}
