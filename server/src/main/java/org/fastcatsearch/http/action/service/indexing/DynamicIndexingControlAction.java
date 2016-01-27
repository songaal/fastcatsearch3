package org.fastcatsearch.http.action.service.indexing;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.ResultFuture;
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
import org.fastcatsearch.job.indexing.UpdateDynamicIndexingScheduleJob;
import org.fastcatsearch.job.indexing.UpdateIndexingScheduleJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.util.JAXBConfigs;
import org.fastcatsearch.util.ResponseWriter;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import java.io.File;
/**
 * 동적(REST API) 색인을 시작/정지한다
 * @See IndexingScheduleAction
 * */
@ActionMapping(value = "/service/indexing/dynamic", method = { ActionMethod.POST })
public class DynamicIndexingControlAction extends ServiceAction {

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {
        String collectionId = request.getParameter("collectionId");
        String flag = request.getParameter("flag");
        if(flag == null) {
            flag = "";
        }
        doAction0(response, collectionId, flag);
	}

    protected void doAction0(ActionResponse response, String collectionId, String flag) throws Exception {
        NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
        IRService irService = ServiceManager.getInstance().getService(IRService.class);
        CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
        if (collectionHandler != null) {
            UpdateDynamicIndexingScheduleJob job2 = new UpdateDynamicIndexingScheduleJob(collectionId, flag);
            String indexNodeId = collectionHandler.collectionContext().collectionConfig().getIndexNode();
            Node indexNode = nodeService.getNodeById(indexNodeId);
            ResultFuture future2 = nodeService.sendRequest(indexNode, job2);
            //동적색인 스케쥴
            Object r = future2.take();
            boolean result = false;
            if(r instanceof Boolean) {
                result = (Boolean) r;
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


