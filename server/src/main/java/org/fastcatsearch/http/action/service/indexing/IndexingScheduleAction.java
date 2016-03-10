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
 * 컬렉션의 전체색인 및 증분색인의 스케쥴을 on/off 할 수 있다.
 * @See DynamicIndexingControlAction
* */
@ActionMapping(value = "/service/indexing/schedule", method = { ActionMethod.POST, ActionMethod.GET })
public class IndexingScheduleAction extends ServiceAction {

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
            throw new ActionException("Type is empty. Choose type in { FULL | ADD | DYNAMIC }");
        }
        String flag = request.getParameter("flag");

        IRService irService = ServiceManager.getInstance().getService(IRService.class);
        CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
        NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
        if (collectionHandler != null) {

            if (UpdateIndexingScheduleJob.TYPE_FULL.equalsIgnoreCase(flag)
                    || UpdateIndexingScheduleJob.TYPE_ADD.equalsIgnoreCase(flag)) {
                UpdateIndexingScheduleJob job = new UpdateIndexingScheduleJob(collectionId, type, flag);
                ResultFuture future = nodeService.sendRequest(nodeService.getMasterNode(), job);
                Object result = future.take();
                writeHeader(response);
                response.setStatus(HttpResponseStatus.OK);
                ResponseWriter resultWriter = getDefaultResponseWriter(response.getWriter());
                resultWriter.object().key(collectionId).value(result).endObject();
                resultWriter.done();
                return;
            } else if (UpdateIndexingScheduleJob.TYPE_DYNAMIC.equalsIgnoreCase(flag)) {
                UpdateDynamicIndexingScheduleJob job = new UpdateDynamicIndexingScheduleJob(collectionId, flag);
                String indexNodeId = collectionHandler.collectionContext().collectionConfig().getIndexNode();
                Node indexNode = nodeService.getNodeById(indexNodeId);
                ResultFuture future2 = nodeService.sendRequest(indexNode, job);
                Object r = future2.take();
                boolean result = false;
                if (r instanceof Boolean) {
                    result = (Boolean) r;
                }

                writeHeader(response);
                response.setStatus(HttpResponseStatus.OK);
                ResponseWriter resultWriter = getDefaultResponseWriter(response.getWriter());
                resultWriter.object().key(collectionId).value(result).endObject();
                resultWriter.done();
                return;
            }
        }

        //컬렉션 없음.
        response.setStatus(HttpResponseStatus.NOT_FOUND);
        writeHeader(response);
        ResponseWriter resultWriter = getDefaultResponseWriter(response.getWriter());
        resultWriter.object().key(collectionId).value(false).endObject();
        resultWriter.done();
	}

}
