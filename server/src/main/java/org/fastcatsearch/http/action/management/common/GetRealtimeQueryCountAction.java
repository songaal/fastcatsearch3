package org.fastcatsearch.http.action.management.common;

import java.io.Writer;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/management/common/query-statistics")
public class GetRealtimeQueryCountAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		resultWriter.object().key("").array();
		for(Entry<String, AtomicInteger[]> entry : irService.queryStatistics().statisticsEntrySet()){
			resultWriter.object().key("collectionId").value(entry.getKey()).key("count").value(entry.getValue()[1].get()).endObject();
		}
		resultWriter.endObject()
		.endObject();
		
		resultWriter.done();
		
		
	}

}
