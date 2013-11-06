package org.fastcatsearch.http.action.service;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.job.plugin.BackupDictionaryJob;
import org.fastcatsearch.util.ResponseWriter;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

@ActionMapping("/service/backup")
public class BackupDictionaryAction extends ServiceAction {

	@Override
	public void doAction(ActionRequest request, ActionResponse response)
			throws Exception {
		BackupDictionaryJob job = new BackupDictionaryJob();
		job.setArgs(request.getParameter("pluginId"));
		ResultFuture jobResult = JobService.getInstance().offer(job);
		Object obj = jobResult.poll(1000);
		
		writeHeader(response);
		response.setStatus(HttpResponseStatus.OK);
		ResponseWriter resultWriter = getDefaultResponseWriter(response.getWriter());
		resultWriter.object().key("status").value("ok").endObject();
		resultWriter.done();
	}
}
