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
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.job.indexing.UpdateDynamicIndexingScheduleJob;
import org.fastcatsearch.job.indexing.UpdateIndexingScheduleJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

            if (UpdateIndexingScheduleJob.TYPE_FULL.equalsIgnoreCase(type)
                    || UpdateIndexingScheduleJob.TYPE_ADD.equalsIgnoreCase(type)) {
                UpdateIndexingScheduleJob job = new UpdateIndexingScheduleJob(collectionId, type, flag);
                ResultFuture future = nodeService.sendRequest(nodeService.getMasterNode(), job);
                Object result = future.take();
                writeHeader(response);
                response.setStatus(HttpResponseStatus.OK);
                ResponseWriter resultWriter = getDefaultResponseWriter(response.getWriter());
                resultWriter.object().key(collectionId).value(result).endObject();
                resultWriter.done();
                return;
            } else if (UpdateIndexingScheduleJob.TYPE_DYNAMIC.equalsIgnoreCase(type)) {
                UpdateDynamicIndexingScheduleJob job = new UpdateDynamicIndexingScheduleJob(collectionId, flag);
                Set<String> nodeIdSet = collectionHandler.collectionContext().collectionConfig().getCollectionNodeIDSet();
                //master 노드 추가.
                nodeIdSet.add(nodeService.getMasterNode().id());
                List<ResultFuture> resultList = new ArrayList<ResultFuture>();
                List<Node> nodeList = new ArrayList<Node>();
                for(String nodeId : nodeIdSet) {
                    Node node = nodeService.getNodeById(nodeId);
                    ResultFuture future = nodeService.sendRequest(node, job);
                    nodeList.add(node);
                    resultList.add(future);
                }
                Boolean result = null;
                int i = 0;
                for(ResultFuture future : resultList) {
                    Object r = future.take();
                    Node node = nodeList.get(i++);
                    logger.debug("Update Dynamic Schedule result [{}] => [{}]", node, r);
                    if (r instanceof Boolean) {
                        Boolean tempResult = (Boolean) r;
                        if(result == null) {
                            result = tempResult;
                        } else {
                            //모두 참이어야 결과가 참이 된다.
                            result = result & tempResult;
                        }
                    }
                }
                writeHeader(response);
                response.setStatus(HttpResponseStatus.OK);
                ResponseWriter resultWriter = getDefaultResponseWriter(response.getWriter());
                resultWriter.object().key(collectionId).value(result == null ? false : result.booleanValue()).endObject();
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
