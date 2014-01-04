package org.fastcatsearch.http.action.management.common;

import java.io.Writer;
import java.util.Map.Entry;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.CollectionQueryCountService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

/**
 * {"컬렉션명" : 갯수, "컬렉션명" : 갯수, ... } 로 리턴한다.
 * */
@ActionMapping("/management/common/realtime-query-count")
public class GetRealtimeQueryCountAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		CollectionQueryCountService collectionQueryCountService = ServiceManager.getInstance().getService(CollectionQueryCountService.class);
		
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		resultWriter.object();
		for(Entry<String, Integer> entry : collectionQueryCountService.aggregateCountResult().entrySet()){
			resultWriter.key(entry.getKey()).value(entry.getValue());
		}
		resultWriter.endObject();
		
		resultWriter.done();
		
		
	}

}
