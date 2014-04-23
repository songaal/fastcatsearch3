package org.fastcatsearch.job.cluster;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.MasterNodeJob;
import org.fastcatsearch.job.state.TaskKey;
import org.fastcatsearch.job.state.TaskState;
import org.fastcatsearch.job.state.TaskStateService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.DynamicClassLoader;

/**
 * 원격 노드에서 master노드로 실행중인 task의 정보를 업데이트 한다.
 * */
public class UpdateNodeTaskStateJob extends MasterNodeJob implements Streamable {

	private static final long serialVersionUID = -5476946471506917337L;

	private String originNodeId;
	private Map<TaskKey, TaskState> runningTaskMap;

	public UpdateNodeTaskStateJob() {
	}
	
	public UpdateNodeTaskStateJob(String originNodeId) {
		this.originNodeId = originNodeId;
	}

	public void setRunningTaskMap(Map<TaskKey, TaskState> runningTaskMap) {
		this.runningTaskMap = runningTaskMap;
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {

		TaskStateService taskStateService = ServiceManager.getInstance().getService(TaskStateService.class);

		if (runningTaskMap != null && runningTaskMap.size() > 0) {
			taskStateService.putAllTasks(originNodeId, runningTaskMap);
		}

		return new JobResult(true);
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		this.originNodeId = input.readString();
		int size = input.readVInt();
		if (size > 0) {
			runningTaskMap = new HashMap<TaskKey, TaskState>(size);
			for (int i = 0; i < size; i++) {
				String taskKeyClass = input.readString();
				TaskKey taskKey = (TaskKey) DynamicClassLoader.loadObject(taskKeyClass);
				taskKey.readFrom(input);
				String taskStateClass = input.readString();
				TaskState taskState = (TaskState) DynamicClassLoader.loadObject(taskStateClass);
				taskState.readFrom(input);
				runningTaskMap.put(taskKey, taskState);
			}
		}
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(originNodeId);
		
		if (runningTaskMap != null && runningTaskMap.size() > 0) {
			output.writeVInt(runningTaskMap.size());
			for (Map.Entry<TaskKey, TaskState> entry : runningTaskMap.entrySet()) {
				output.writeString(entry.getKey().getClass().getName());
				entry.getKey().writeTo(output);
				output.writeString(entry.getValue().getClass().getName());
				entry.getValue().writeTo(output);
			}
		} else {
			output.writeVInt(0);
		}

	}
}
