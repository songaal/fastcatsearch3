package org.fastcatsearch.job.management;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.result.BasicStringResult;
import org.json.JSONStringer;

public class GetThreadStateJob extends Job implements Streamable {

	private static final long serialVersionUID = -5814628836593851820L;
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		try {
			
			Map<Thread, StackTraceElement[]> stackTraceMap = Thread.getAllStackTraces();
			JSONStringer stringer = new JSONStringer();
			stringer.object()
			.key("count").value(stackTraceMap.size())
			.key("threadList").array();
			
			
			for(Entry<Thread, StackTraceElement[]> e : stackTraceMap.entrySet()){
				Thread thread = e.getKey();
				StackTraceElement[] el = e.getValue();
				
				StringBuffer sb = new StringBuffer();
				for(StackTraceElement st : el){
					if(sb.length() > 0){
						sb.append("\n");
					}
					sb.append(st.toString());
				}
				stringer.object()
				.key("name").value(thread.getName())
				.key("group").value(thread.getThreadGroup().getName())
				.key("priority").value(String.valueOf(thread.getPriority()))
				.key("tid").value(String.valueOf(thread.getId()))
				.key("state").value(String.valueOf(thread.getState()))
				.key("daemon").value(thread.isDaemon())
				.key("alive").value(thread.isAlive())
				.key("interrupt").value(thread.isInterrupted())
				.key("stacktrace").value(sb.toString())
				.endObject();
			}
			
			stringer.endArray().endObject();
			
			BasicStringResult result = new BasicStringResult();
			result.setResult(stringer.toString());
			
			return new JobResult(result);
			
		} catch (Exception e) {
			logger.error("", e);
		}
		return new JobResult(null);
	}

	@Override
	public void readFrom(DataInput input) throws IOException { }

	@Override
	public void writeTo(DataOutput output) throws IOException { }
}
