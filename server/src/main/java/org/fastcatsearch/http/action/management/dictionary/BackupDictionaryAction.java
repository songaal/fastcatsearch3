package org.fastcatsearch.http.action.management.dictionary;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.job.Job.JobResult;
import org.fastcatsearch.job.plugin.BackupDictionaryJob;
import org.fastcatsearch.util.ResponseWriter;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

@ActionMapping(value="/management/dictionary/backup", authority=ActionAuthority.Dictionary, authorityLevel=ActionAuthorityLevel.WRITABLE)
public class BackupDictionaryAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response)
			throws Exception {
		BackupDictionaryJob job = new BackupDictionaryJob();
		job.setArgs(request.getParameter("pluginId"));
		
		writeHeader(response);
		response.setStatus(HttpResponseStatus.OK);
		ResponseWriter resultWriter = getDefaultResponseWriter(response.getWriter());
		resultWriter.object().key("success");
		//job을 바로실행.
		try{
			JobResult jobResult = job.doRun();
			if(jobResult != null){
				resultWriter.value(jobResult.isSuccess());
			}else{
				resultWriter.value(false);
			}
		}catch(Throwable e){
			resultWriter.value(false);
		}
		
		resultWriter.endObject();
		resultWriter.done();
	}

}
