package org.fastcatsearch.http.action.service.indexing;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.ActionMethod;
import org.fastcatsearch.http.action.ActionException;
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

@ActionMapping(value = "/service/indexing/schedule", method = { ActionMethod.POST, ActionMethod.GET })
public class IndexingScheduleAction extends ServiceAction {

    private static final String TYPE_FULL = "FULL";
    private static final String TYPE_ADD = "ADD";

    private static final String FLAG_ON = "ON";
    private static final String FLAG_OFF = "OFF";

    /**
     * collectionId : 컬렉션아이디
     * type : 색인타입 ( full | add)
     * flag : 상태플래그 (ON | OFF)
     * */
	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {

        String collectionId = request.getParameter("collectionId");
        String type = request.getParameter("type");
        if(collectionId == null) {
            throw new ActionException("Collection id is empty.");
        }
        if(type == null) {
            throw new ActionException("Type is empty. Choose type in { FULL | ADD }");
        }
        String flag = request.getParameter("flag");

        IRService irService = ServiceManager.getInstance().getService(IRService.class);
        CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
        if (collectionHandler != null) {

            CollectionContext collectionContext = irService.collectionContext(collectionId);
            IndexingScheduleConfig indexingScheduleConfig = collectionContext.indexingScheduleConfig();

            IndexingScheduleConfig.IndexingSchedule indexingSchedule = null;
            if(type.equalsIgnoreCase(TYPE_FULL)) {
                indexingSchedule = indexingScheduleConfig.getFullIndexingSchedule();
            } else if(type.equalsIgnoreCase(TYPE_ADD)) {
                indexingSchedule = indexingScheduleConfig.getAddIndexingSchedule();
            }

            boolean result = false;
            if(flag != null && (flag.equalsIgnoreCase(FLAG_ON) || flag.equalsIgnoreCase(FLAG_OFF))) {
                boolean requestActive = flag.equalsIgnoreCase(FLAG_ON);

                if (indexingSchedule.isActive() != requestActive) {
                    indexingSchedule.setActive(requestActive);
                    File scheduleConfigFile = collectionContext.collectionFilePaths().file(SettingFileNames.scheduleConfig);
                    JAXBConfigs.writeConfig(scheduleConfigFile, indexingScheduleConfig, IndexingScheduleConfig.class);
                    //해당 컬렉션의 스케쥴을 다시 로딩.
                    irService.reloadSchedule(collectionId);
                    result = requestActive;
                }
            } else {
                result = indexingSchedule.isActive();
            }

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
