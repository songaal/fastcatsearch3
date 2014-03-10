package org.fastcatsearch.http.action.management;

import java.io.Writer;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.util.DynamicClassLoader;
import org.fastcatsearch.util.ResponseWriter;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

@ActionMapping("/management/execute/job")
public class JobExecuteAction extends ServiceAction {

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {
		writeHeader(response);
		response.setStatus(HttpResponseStatus.OK);
		String jobClassName = request.getParameter("job");
		boolean isJoin = request.getBooleanParameter("join", true);
		
		Job job = DynamicClassLoader.loadObject(jobClassName,Job.class);
		job.setArgs(request.getParameterMap());
		ResultFuture resultFuture = JobService.getInstance().offer(job);
		Object result = null;
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
		if(isJoin){
			result = resultFuture.take();
			writer.write("<h2>Run Job</h2>");
			writer.write("<p>"+jobClassName+"</p><p>Result: <br/>\n");
			writer.write("<pre>"+result.toString()+"</pre></p>");
			
			resultWriter
			.object()
			.key("job").value(jobClassName);
			
			if(result != null && resultFuture.isSuccess()){
				resultWriter.key("status").value("0");
				resultWriter.key("result").value(result.toString());
			}else{
				resultWriter.key("status").value("1");
			}
			resultWriter.endObject();
			
		}else{
			resultWriter
			.object()
			.key("job").value(jobClassName)
			.endObject();
		}
		
		
		resultWriter.done();
		if (writer != null) {
			writer.close();
		}
	}

}
