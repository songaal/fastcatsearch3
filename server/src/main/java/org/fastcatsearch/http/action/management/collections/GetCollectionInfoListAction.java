package org.fastcatsearch.http.action.management.collections;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionsConfig.Collection;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.management.collections.GetIndexingInfoJob;
import org.fastcatsearch.job.management.model.CollectionIndexingInfo;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ActionMapping(value = "/management/collections/collection-info-list", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.NONE)
public class GetCollectionInfoListAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		
		String collectionListStr = request.getParameter("collectionId", "");
		
		List<String> collections = null;
		
		if(!"".equals(collectionListStr)) {
			collections = Arrays.asList(collectionListStr.replaceAll(" ", "").split(","));
		}

		List<Collection> collectionList = irService.getCollectionList();
		List<ResultFuture> resultFutureList = new ArrayList<ResultFuture>();
		for(Collection collection : collectionList) {
			String collectionId = collection.getId();

			//원하는 컬렉션만 골라낼 때
			if (collections != null && !collections.contains(collectionId)) {
				continue;
			}

			CollectionContext collectionContext = irService.collectionContext(collectionId);
			if (collectionContext == null) {
				continue;
			}

			String indexNodeId = collectionContext.collectionConfig().getIndexNode();
			logger.debug("indexNodeId", indexNodeId);
			Job job = new GetIndexingInfoJob(collectionId);

			NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
			Node indexNode = nodeService.getNodeById(indexNodeId);
			//CollectionIndexingInfo Future를 받았다.
			resultFutureList.add(nodeService.sendRequest(indexNode, job));
		}


		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		responseWriter.object().key("collectionInfoList").array("collectionInfo");

		for (ResultFuture future : resultFutureList) {
			Object obj = future.take();
			if (!future.isSuccess()) {
				if (obj instanceof Throwable) {
					throw new FastcatSearchException((Throwable) obj);
				} else {
					throw new FastcatSearchException("Error", obj);
				}
			}

			CollectionIndexingInfo info = (CollectionIndexingInfo) obj;
			responseWriter.object();
			{//simple-info
				responseWriter
						.key("id").value(info.getCollectionId())
						.key("isActive").value(info.getIsActive())
						.key("name").value(info.getName())
						.key("sequence").value(info.getSequence())
						.key("revisionUUID").value(info.getRevisionUUID())
						.key("indexNode").value(info.getIndexNode())
						.key("dataNodeList").value(info.getDataNodeList())
						.key("searchNodeList").value(info.getSearchNodeList());
			}

			{//simple-info
				responseWriter
						.key("documentSize").value(info.getDocumentSize())
						.key("segmentSize").value(info.getSegmentSize())
						.key("diskSize").value(info.getDiskSize())
						.key("dataPath").value(info.getDataPath())
						.key("createTime").value(info.getCreateTime());
			}

			responseWriter.endObject();
		}

		responseWriter.endArray().endObject();
		responseWriter.done();
	}

}
