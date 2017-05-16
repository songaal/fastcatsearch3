package org.fastcatsearch.http.action.service.indexing;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.job.indexing.IndexDocumentRequestJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import java.io.Writer;

/**
 * Rest API를 통해 문서를 증분색인한다.
 *
 * */
public abstract class IndexDocumentsAction extends ServiceAction {

    public static final String INSERT_TYPE = "I";
    public static final String UPDATE_TYPE = "U";
    public static final String DELETE_TYPE = "D";

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

        if(indexNode == null) {
            throw new IRException("Cannot find index node. Indexing fail! collectionId=" + collectionId);
        }
        IndexDocumentRequestJob indexCollectionDocumentJob = new IndexDocumentRequestJob();
        String indexType = getType();
        indexCollectionDocumentJob.setArgs(new String[]{collectionId, indexType, requestBody});
//        ResultFuture jobResult = nodeService.sendRequest(indexNode, indexCollectionDocumentJob);
        /*
        * 2017.5.16 색인노드의 의존성 제거를 위해 index노드가 아닌 명령을 받은 노드에서 동적색인을 수행한다.
        * */
        ResultFuture jobResult = JobService.getInstance().offer(indexCollectionDocumentJob);
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

    protected abstract String getType();

}
