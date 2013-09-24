package org.fastcatsearch.http.action.management;

import java.io.PrintWriter;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.util.DynamicClassLoader;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

@ActionMapping("/management/execute/job")
public class JobExecuteAction extends ServiceAction {

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {
		PrintWriter writer = response.getWriter();
		writeHeader(response);
		response.setStatus(HttpResponseStatus.OK);
		String jobClassName = request.getParameter("job");
		boolean isJoin = request.getBooleanParameter("join", true);
		
		Job job = DynamicClassLoader.loadObject(jobClassName,Job.class);
		job.setArgs(request.getParameterMap());
		ResultFuture resultFuture = JobService.getInstance().offer(job);
		Object result = null;
		if(isJoin){
			result = resultFuture.take();
			writer.write("<h2>Run Job</h2>");
			writer.write("<p>"+jobClassName+"</p><p>Result: <br/>\n");
			writer.write("<pre>"+result.toString()+"</pre></p>");
		}else{
			writer.write("Job offered! " + jobClassName);
		}
		writer.close();
	}

}
