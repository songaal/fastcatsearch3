package org.fastcatsearch.alert;

import java.io.IOException;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableThrowable;

/**
 * slave노드에서 예외발생한 경우, master노드에 예외를 알려주는 job.
 * 반드시 master노드에서 실행되도록 한다.
 * */
public class FastcatSearchAlertJob extends Job implements Streamable {

	private static final long serialVersionUID = 3902372183481674611L;
	
	private Node node;
	private Throwable e;
	
	public FastcatSearchAlertJob(){ }
	
	public FastcatSearchAlertJob(Node node, Throwable e) {
		this.node = node;
		this.e = e;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		node = new Node();
		node.readFrom(input);
		StreamableThrowable streamableThrowable = new StreamableThrowable();
		streamableThrowable.readFrom(input);
		this.e = streamableThrowable.getThrowable();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		node.writeTo(output);
		//output.writeBoolean(false);
		new StreamableThrowable(e).writeTo(output);
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {
		ServiceManager.getInstance().getService(ClusterAlertService.class).handleException(node, e);
		
		return new JobResult();
	}

}
