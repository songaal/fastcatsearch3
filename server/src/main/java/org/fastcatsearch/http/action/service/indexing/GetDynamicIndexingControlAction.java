package org.fastcatsearch.http.action.service.indexing;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.ActionMethod;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.IndexingScheduleConfig;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.util.JAXBConfigs;
import org.fastcatsearch.util.ResponseWriter;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import java.io.File;

/**
 * */
@ActionMapping(value = "/service/index/working", method = { ActionMethod.GET })
public class GetDynamicIndexingControlAction extends ServiceAction {

    private static final String FLAG_ON = "on";
    private static final String FLAG_OFF = "off";

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {

        String collectionId = request.getParameter("collectionId");

        IRService irService = ServiceManager.getInstance().getService(IRService.class);
        CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
        if (collectionHandler != null) {

            CollectionContext collectionContext = irService.collectionContext(collectionId);
            IndexingScheduleConfig indexingScheduleConfig = collectionContext.indexingScheduleConfig();

            IndexingScheduleConfig.IndexingSchedule indexingSchedule = indexingScheduleConfig.getAddIndexingSchedule();

            boolean result1 = indexingSchedule.isActive();
            boolean result2 = irService.getDynamicIndexModule(collectionId).isIndexingScheduled();

            writeHeader(response);
            response.setStatus(HttpResponseStatus.OK);
            ResponseWriter resultWriter = getDefaultResponseWriter(response.getWriter());
            resultWriter.object().key("collectionId").value(collectionId).key("incrementIndexing").value(result1 ? FLAG_ON : FLAG_OFF)
                    .key("dynamicIndexing").value(result2 ? FLAG_ON : FLAG_OFF).endObject();
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
