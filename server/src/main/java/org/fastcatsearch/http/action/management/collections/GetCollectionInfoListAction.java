package org.fastcatsearch.http.action.management.collections;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.env.Path;
import org.fastcatsearch.error.SearchError;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionConfig;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionsConfig.Collection;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.management.collections.GetIndexingInfoJob;
import org.fastcatsearch.job.management.model.CollectionIndexingInfo;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

import java.io.File;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		responseWriter.object().key("collectionInfoList").array("collectionInfo");
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

			Job job = new GetIndexingInfoJob(collectionId);

			NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
			Node indexNode = nodeService.getNodeById(indexNodeId);
			//CollectionIndexingInfo 를 받았다.
			resultFutureList.add(nodeService.sendRequest(indexNode, job));

		}





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
						.key("id").value(info.getCollectionId());
			}

			{//normal-info
				responseWriter
						.key("isActive").value(info.getActive())
						.key("name").value(info.getName())
						.key("sequence").value(sequence)
						.key("revisionUUID").value(revisionUUID)
						.key("indexNode").value(collectionConfig.getIndexNode())
						.key("dataNodeList").value(join(collectionConfig.getDataNodeList()))
						.key("searchNodeList").value(join(collectionConfig.getSearchNodeList()));
			}

			{//detail-info
				File indexFileDir = collectionContext.dataFilePaths().indexDirFile(sequence);
				int documentSize = collectionContext.dataInfo().getDocuments();
				int segmentSize = dataInfo.getSegmentSize();
				String diskSize = "";
				if(indexFileDir.exists()){
					long byteCount = FileUtils.sizeOfDirectory(indexFileDir);
					diskSize = FileUtils.byteCountToDisplaySize(byteCount);
				}
				String dataPath = new Path(collectionContext.collectionFilePaths().file()).relativise(indexFileDir).getPath();
				SegmentInfo lastSegmentInfo = collectionContext.dataInfo().getLatestSegmentInfo();
				String createTime = null;
				if(lastSegmentInfo != null) {
					createTime = Formatter.formatDate(new Date(lastSegmentInfo.getCreateTime()));
				} else {
					createTime = "";
				}
				responseWriter
						.key("documentSize").value(documentSize)
						.key("segmentSize").value(segmentSize)
						.key("diskSize").value(diskSize)
						.key("dataPath").value(dataPath)
						.key("createTime").value(createTime);
			}
			responseWriter.endObject();
		}


		responseWriter.endArray().endObject();
		responseWriter.done();
	}

	public String join(List<String> list) {
		String joinString = "";
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				if(i > 0){
					joinString += ", ";
				}
				joinString += list.get(i);
			}
		}

		return joinString;
	}
}
