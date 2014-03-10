package org.fastcatsearch.http.action.management.settings;

import java.io.Writer;
import java.util.Map.Entry;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping (value="/settings/search-config/update", authority=ActionAuthority.Settings, authorityLevel=ActionAuthorityLevel.WRITABLE)
public class UpdateSearchPageConfigAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response)
			throws Exception {
		
		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		
		
		//TODO 
		
		
		int i = 1;
		for(Entry<String, String> e : request.getParameterMap().entrySet()){
			logger.debug("[{}] key {} > {}", i++, e.getKey(), e.getValue());
		}
		responseWriter.object();
		responseWriter.key("success").value(true);
		responseWriter.endObject();
		responseWriter.done();
	}
}
