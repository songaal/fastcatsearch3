package org.fastcatsearch.http.action.service.indexing;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.ActionMethod;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.http.action.management.indexing.GetAllIndexingTaskStateAction;
import org.fastcatsearch.http.action.management.indexing.GetIndexingTaskStateAction;

/**
 * Created by 전제현 on 2017-04-14.
 */
@ActionMapping(value = "/service/indexing/all-collection/status", method = { ActionMethod.GET })
public class IndexingAllCollectionStatusAction extends ServiceAction {

    @Override
    public void doAction(ActionRequest request, ActionResponse response) throws Exception {

        GetAllIndexingTaskStateAction action = new GetAllIndexingTaskStateAction();
        action.init(this.resultType, request, response, null);
        writeHeader(response);
        action.doAuthAction(request, response);
    }

}