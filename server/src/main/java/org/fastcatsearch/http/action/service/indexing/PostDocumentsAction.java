package org.fastcatsearch.http.action.service.indexing;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.ActionMethod;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.job.indexing.MasterCollectionPostDocumentJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

import java.io.Writer;

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
        JobService jobService = ServiceManager.getInstance().getService(JobService.class);

        MasterCollectionPostDocumentJob masterCollectionPostDocumentJob = new MasterCollectionPostDocumentJob();
        masterCollectionPostDocumentJob.setArgs(new String[]{collectionId, requestBody});

        ResultFuture jobResult = jobService.offer(masterCollectionPostDocumentJob);
        Object result = null;
        if(jobResult != null) {
            result = jobResult.take();
        }

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
