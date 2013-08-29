package org.fastcatsearch.http.action.service;

import java.io.PrintWriter;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.util.ResultWriter;
import org.fastcatsearch.util.ResultWriterException;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public abstract class CallableAction extends ServiceAction {

	public CallableAction(String type) {
		super(type);
	}

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {
		PrintWriter writer = response.getWriter();
		writeHeader(response);
		
		response.setStatus(HttpResponseStatus.OK);
		String jsonCallback = request.getParameter("_jsonCallback");
		boolean isJoin = request.getBooleanParameter("_wait", true);
		ResultWriter resultWriter = getResultWriter(writer, "fastcatsearch", true, jsonCallback);
		
		
		Job job = createJob();
		job.setArgs(request.getParameterMap());
		
		long startTime = System.nanoTime();
		ResultFuture resultFuture = JobService.getInstance().offer(job);
		Object result = null;
		int status = 0;
		
		resultWriter.object();
		if(isJoin){
			result = resultFuture.take();
			if(resultFuture.isSuccess()){
				resultWriter.key("result");
				writeResult(resultWriter, result);
				resultWriter.key("error").value("");
			}else{
				resultWriter.key("error");
				writeError(resultWriter, result);
				resultWriter.key("result").value("");
				status = 1;
			}
		}else{
			resultWriter.key("result").value("");
			resultWriter.key("error").value("");
		}
		
		int time = (int) ((System.nanoTime() - startTime) / 1000000L);
		resultWriter.key("time").value(time);
		resultWriter.key("status").value(status);
		resultWriter.endObject();
		resultWriter.done();
		
		writer.close();
	}
	
	protected abstract Job createJob();
		
	protected abstract void writeResult(ResultWriter resultWriter, Object result) throws Exception;
	
	protected void writeError(ResultWriter resultWriter, Object e) throws ResultWriterException {
		logger.error(">> error > {}", e.toString());
		resultWriter.value(e.toString());
	}
		
}
