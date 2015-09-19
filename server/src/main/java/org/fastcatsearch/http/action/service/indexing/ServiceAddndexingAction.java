package org.fastcatsearch.http.action.service.indexing;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.ActionMethod;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.http.action.management.indexing.RunAddIndexingAction;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

@ActionMapping(value = "/service/indexing/add", method = { ActionMethod.POST })
public class ServiceAddndexingAction extends ServiceAction {

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {

        RunAddIndexingAction action = new RunAddIndexingAction();
        action.init(this.resultType, request, response, null);
        writeHeader(response);
        action.doAuthAction(request, response);
	}

}
