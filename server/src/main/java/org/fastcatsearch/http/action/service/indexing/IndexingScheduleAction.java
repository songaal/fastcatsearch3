package org.fastcatsearch.http.action.service.indexing;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.ActionMethod;
import org.fastcatsearch.http.action.ActionException;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionsConfig;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

@ActionMapping(value = "/service/indexing/schedule", method = ActionMethod.POST)
public class IndexingScheduleAction extends ServiceAction {

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {

        String collectionId = request.getParameter("collectionId");
        String flag = request.getParameter("flag");

        IRService irService = ServiceManager.getInstance().getService(IRService.class);

        boolean result = false;
        if(collectionId == null) {
            //TODO 모든 컬렉션 전부적용.
            for(CollectionsConfig.Collection collection : irService.getCollectionList()) {
                String id = collection.getId();
                CollectionHandler collectionHandler = irService.collectionHandler(id);
                if(collectionHandler != null) {
                    applyIndexingSchedule(collectionHandler, flag);
                }
            }
        } else {
            CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
            if(collectionHandler != null) {
                result = applyIndexingSchedule(collectionHandler, flag);
            } else {
               //TODO collection not found.
            }
        }

        writeHeader(response);
		response.setStatus(HttpResponseStatus.OK);
		ResponseWriter resultWriter = getDefaultResponseWriter(response.getWriter());
		resultWriter.object().key("result").value(result).endObject();
		resultWriter.done();
	}

    private boolean applyIndexingSchedule(CollectionHandler collectionHandler, String flag) {
        if("ON".equalsIgnoreCase(flag)) {
            return collectionHandler.resumeIndexingSchedule();
        } else if("OFF".equalsIgnoreCase(flag)) {
            return collectionHandler.pauseIndexingSchedule();
        }
        return false;
    }
}
