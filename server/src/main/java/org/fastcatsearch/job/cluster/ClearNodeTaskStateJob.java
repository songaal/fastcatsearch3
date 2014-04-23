package org.fastcatsearch.job.cluster;

import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.MasterNodeJob;
import org.fastcatsearch.job.state.TaskStateService;
import org.fastcatsearch.service.ServiceManager;

/**
 * 원격 노드에서 실행중인 task list를 없애도록 mater에 요청한다.
 * 서버가 재실행되었거나,  
 * */
public class ClearNodeTaskStateJob extends MasterNodeJob implements Streamable {

	private static final long serialVersionUID = -5476946471506917337L;

	private String originNodeId;

	public ClearNodeTaskStateJob() {
	}
	
	public ClearNodeTaskStateJob(String originNodeId) {
		this.originNodeId = originNodeId;
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {

		TaskStateService taskStateService = ServiceManager.getInstance().getService(TaskStateService.class);

		taskStateService.clearTaskMap(originNodeId);

		return new JobResult(true);
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		this.originNodeId = input.readString();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(originNodeId);
	}
}
