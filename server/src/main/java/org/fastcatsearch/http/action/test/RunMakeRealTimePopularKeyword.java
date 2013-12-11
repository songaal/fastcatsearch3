package org.fastcatsearch.http.action.test;

import java.io.Writer;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.job.statistics.SearchPopularKeywordRealTimeCollectMainJob;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/test/run-make-rt-popular-keyword")
public class RunMakeRealTimePopularKeyword extends ServiceAction {

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {
		SearchPopularKeywordRealTimeCollectMainJob job = new SearchPopularKeywordRealTimeCollectMainJob();
		job.doRun();
		
		Writer writer = response.getWriter();
		
		writeHeader(response);
		
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		responseWriter.object()
		.key("success").value(true)
		.endObject();
		responseWriter.done();
	}

}
