package org.fastcatsearch.http.action.service.indexing;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.ActionMethod;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.http.action.management.indexing.GetIndexingTaskStateAction;

@ActionMapping(value = "/service/indexing/status", method = { ActionMethod.GET })
public class IndexingStatusAction extends ServiceAction {

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {

        GetIndexingTaskStateAction action = new GetIndexingTaskStateAction();
        action.init(this.resultType, request, response, null);
        writeHeader(response);
        action.doAuthAction(request, response);
	}

}
