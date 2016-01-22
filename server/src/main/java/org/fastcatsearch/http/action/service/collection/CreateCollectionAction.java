package org.fastcatsearch.http.action.service.collection;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.ActionMethod;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.indexing.IndexDocumentRequestJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import java.io.Writer;

/**
 * TODO Rest API를 통해 컬렉션을 생성한다.
 *
 * */
@ActionMapping(value = "/service/collection", method = { ActionMethod.POST })
public class CreateCollectionAction extends ServiceAction {

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {

        String collectionId = request.getParameter("collectionId");
        String requestBody = request.getRequestBody();

        NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
        IRService irService = ServiceManager.getInstance().getService(IRService.class);
        CollectionContext collectionContext = irService.collectionContext(collectionId);

        if (collectionContext == null) {
            throw new FastcatSearchException("Collection [" + collectionId + "] is not exist.");
        }

        String indexNodeId = collectionContext.collectionConfig().getIndexNode();
        Node indexNode = nodeService.getNodeById(indexNodeId);

        Job job = null;

        //TODO












        ResultFuture jobResult = nodeService.sendRequest(indexNode, job);
        Object result = null;
        if(jobResult != null) {
            result = jobResult.take();
        }

        writeHeader(response);
        response.setStatus(HttpResponseStatus.OK);
        Writer writer = response.getWriter();
        ResponseWriter resultWriter = getDefaultResponseWriter(writer);
        resultWriter
                .object()
                .key("collectionId").value(collectionId);

        if(result != null){
            resultWriter.key("status").value("0");
        }else{
            resultWriter.key("status").value("1");
        }
        resultWriter.endObject();
        resultWriter.done();
	}

}
