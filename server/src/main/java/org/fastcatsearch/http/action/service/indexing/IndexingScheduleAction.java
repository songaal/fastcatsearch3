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

import java.util.ArrayList;
import java.util.List;

@ActionMapping(value = "/service/indexing/schedule", method = { ActionMethod.POST, ActionMethod.GET })
public class IndexingScheduleAction extends ServiceAction {

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {

        String collectionId = request.getParameter("collectionId");
        String flag = request.getParameter("flag");

        IRService irService = ServiceManager.getInstance().getService(IRService.class);

        if(collectionId == null) {
            // 모든 컬렉션 전부적용.
            List<String> idList = new ArrayList<String>();
            List<Boolean> stateList = new ArrayList<Boolean>();
            for(CollectionsConfig.Collection collection : irService.getCollectionList()) {
                String id = collection.getId();
                CollectionHandler collectionHandler = irService.collectionHandler(id);
                if(collectionHandler != null) {
                    if(flag != null) {
                        applyIndexingSchedule(collectionHandler, flag);
                    }
                    boolean isAvailable = collectionHandler.isIndexScheduleAvailable();
                    idList.add(id);
                    stateList.add(isAvailable);
                }
            }
            response.setStatus(HttpResponseStatus.OK);
            writeHeader(response);
            ResponseWriter resultWriter = getDefaultResponseWriter(response.getWriter());

            resultWriter.object().key("indexingSchedule").array();

            for(int i = 0 ;i < idList.size(); i++) {
                String id = idList.get(i);
                boolean state = stateList.get(i);
                resultWriter.object().key(id).value(state).endObject();
            }
            resultWriter.endArray().endObject();
            resultWriter.done();
        } else {
            CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
            if (collectionHandler != null) {
                if(flag != null) {
                    applyIndexingSchedule(collectionHandler, flag);
                }
                boolean isAvailable = collectionHandler.isIndexScheduleAvailable();

                writeHeader(response);
                response.setStatus(HttpResponseStatus.OK);
                ResponseWriter resultWriter = getDefaultResponseWriter(response.getWriter());
                resultWriter.object().key(collectionId).value(isAvailable).endObject();
                resultWriter.done();
            } else {
                //컬렉션 없음.
                response.setStatus(HttpResponseStatus.NOT_FOUND);
                writeHeader(response);
                ResponseWriter resultWriter = getDefaultResponseWriter(response.getWriter());
                resultWriter.object().key(collectionId).value(false).endObject();
                resultWriter.done();
            }
        }
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
