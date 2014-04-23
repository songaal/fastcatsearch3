package org.fastcatsearch.job.indexing;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.result.BasicStringResult;
import org.fastcatsearch.job.state.IndexingTaskKey;
import org.fastcatsearch.job.state.TaskKey;
import org.fastcatsearch.job.state.TaskState;
import org.fastcatsearch.job.state.TaskStateService;
import org.fastcatsearch.service.ServiceManager;
import org.json.JSONException;
import org.json.JSONStringer;

//삭제예정.
@Deprecated
public class GetIndexingTaskStateJob extends Job implements Streamable {
	
	private static final long serialVersionUID = -1622828261008438014L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		JSONStringer stringer = new JSONStringer();
		try {
			
			TaskStateService taskStateService = ServiceManager.getInstance().getService(TaskStateService.class);
			
			List<Entry<TaskKey, TaskState>> taskEntryList = null;//taskStateService.getTaskEntryList(IndexingTaskKey.class);
			
			stringer.object().key("taskState").array();
			
			if(taskEntryList != null){
				for(Entry<TaskKey, TaskState> entry : taskEntryList){
					TaskKey taskKey = entry.getKey();
					TaskState taskState = entry.getValue();
					stringer.object()
					.key("summary").value(taskKey.getSummary() + " " + taskState.getSummary())
					.key("state").value(taskState.getState())
					.key("step").value(taskState.getStep())
					.key("isScheduled").value(taskState.isScheduled())
					.key("progress").value(taskState.getProgressRate())
					.key("startTime").value(taskState.getStartTime())
					.key("endTime").value(taskState.getStartTime())
					.key("elapsed").value(taskState.getElapsedTime())
					.endObject();
				}
				
			}
			stringer.endArray().endObject();
			
			BasicStringResult result = new BasicStringResult();
			result.setResult(stringer.toString());
			return new JobResult(result);
		
		} catch (JSONException e) {
			logger.debug("exception occurs : {}",e.getMessage());
		} finally {
		}
		return new JobResult(false);
	}

	@Override
	public void readFrom(DataInput input) throws IOException { }

	@Override
	public void writeTo(DataOutput output) throws IOException { }
}
