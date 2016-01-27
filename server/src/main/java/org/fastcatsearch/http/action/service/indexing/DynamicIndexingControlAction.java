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
/**
 * 1. 증분색인 스케쥴을 시작/정지한다.(셋팅까지 기록)
 * 2. 동적(REST API) 색인을 시작/정지한다
 * */
@ActionMapping(value = "/service/index/control", method = { ActionMethod.POST })
public class DynamicIndexingControlAction extends ServiceAction {

    private static final String FLAG_ON = "ON";
    private static final String FLAG_OFF = "OFF";

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {

        String collectionId = request.getParameter("collectionId");
        String flag = request.getParameter("flag");

        IRService irService = ServiceManager.getInstance().getService(IRService.class);
        CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
        if (collectionHandler != null) {

            CollectionContext collectionContext = irService.collectionContext(collectionId);
            IndexingScheduleConfig indexingScheduleConfig = collectionContext.indexingScheduleConfig();

            IndexingScheduleConfig.IndexingSchedule indexingSchedule = indexingScheduleConfig.getAddIndexingSchedule();

            boolean result1 = false; //증분색인 스케쥴
            boolean result2 = false; //동적색인 스케쥴
            if(flag != null && (flag.equalsIgnoreCase(FLAG_ON) || flag.equalsIgnoreCase(FLAG_OFF))) {
                boolean requestActive = flag.equalsIgnoreCase(FLAG_ON);

                if (indexingSchedule.isActive() != requestActive) {
                    indexingSchedule.setActive(requestActive);
                    File scheduleConfigFile = collectionContext.collectionFilePaths().file(SettingFileNames.scheduleConfig);
                    JAXBConfigs.writeConfig(scheduleConfigFile, indexingScheduleConfig, IndexingScheduleConfig.class);
                    //해당 컬렉션의 스케쥴을 다시 로딩.
                    irService.reloadSchedule(collectionId);
                }

                if(requestActive) {
                    irService.getDynamicIndexModule(collectionId).startIndexingSchedule();
                } else {
                    irService.getDynamicIndexModule(collectionId).stopIndexingSchedule();
                }
            }
            result1 = indexingSchedule.isActive();
            result2 = irService.getDynamicIndexModule(collectionId).isIndexingScheduled();

            writeHeader(response);
            response.setStatus(HttpResponseStatus.OK);
            ResponseWriter resultWriter = getDefaultResponseWriter(response.getWriter());
            resultWriter.object().key("collectionId").value(collectionId).key("incrementIndexing").value(result1 ? "on" : "off")
                    .key("dynamicIndexing").value(result2 ? "on" : "off").endObject();
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
