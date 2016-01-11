package org.fastcatsearch.http.action.service.indexing;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.ActionMethod;
import org.fastcatsearch.http.action.ActionException;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.index.DynamicIndexer;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rest API를 통해 문서를 증분색인한다.
 *
 * */
@ActionMapping(value = "/service/index", method = { ActionMethod.POST })
public class PostDocumentsAction extends ServiceAction {
	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {

        String collectionId = request.getParameter("collectionId");

        String requestBody = request.getRequestBody();

        if(collectionId == null) {
            throw new ActionException("Collection id is empty.");
        }

        IRService irService = ServiceManager.getInstance().getService(IRService.class);
        CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
        if (collectionHandler != null) {
            List<HashMap<String, Object>> jsonList = new JSONRequestReader().readJsonList(requestBody);
            DynamicIndexer dynamicIndexer = collectionHandler.dynamicIndexer();
            try {
                for (Map<String, Object> document : jsonList) {
                    dynamicIndexer.addDocument(document);
                }
            } catch (Exception e) {
                logger.error("", e);
            } finally {
                dynamicIndexer.finish();
            }

            boolean result = false;

            writeHeader(response);
            response.setStatus(HttpResponseStatus.OK);
            ResponseWriter resultWriter = getDefaultResponseWriter(response.getWriter());
            resultWriter.object().key(collectionId).value(result).endObject();
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
