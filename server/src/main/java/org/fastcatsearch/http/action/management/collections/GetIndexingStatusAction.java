package org.fastcatsearch.http.action.management.collections;

import java.io.File;
import java.io.Writer;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.config.ShardContext;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.FilePaths;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/management/collections/indexing-status")
public class GetIndexingStatusAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		String collectionId = request.getParameter("collectionId");
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		CollectionContext collectionContext = irService.collectionContext(collectionId);
		

		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		responseWriter.object()
		.key("collectionId").value(collectionId)
		.key("shardStatus").array("shard");
		
		long totalByteCount = 0L; 
		int totalDocumentSize = 0;
		
		for(ShardContext shardContext : collectionContext.getShardContextList()){
			responseWriter.object();
			responseWriter.key("id").value(shardContext.shardId());
			File indexDir = shardContext.indexFilePaths().file();
			int sequence = shardContext.indexStatus().getSequence();
			responseWriter.key("sequence").value(sequence);
			String diskSize = "";
//			logger.debug("shard index dir ={}", indexDir.getAbsolutePath());
			if(indexDir.exists()){
				long byteCount = FileUtils.sizeOfDirectory(indexDir);
				totalByteCount += byteCount;
				diskSize = FileUtils.byteCountToDisplaySize(byteCount);
			}
			responseWriter.key("diskSize").value(diskSize);
			
//			IndexStatus fullIndexStatus = shardContext.indexStatus().getFullIndexStatus();
//			IndexStatus addIndexStatus = shardContext.indexStatus().getAddIndexStatus();
//			int documentSize = fullIndexStatus != null ? fullIndexStatus.getDocumentCount() : 0;
//			documentSize += addIndexStatus != null ? addIndexStatus.getDocumentCount() : 0;
			
			int documentSize = shardContext.dataInfo().getDocuments();
			totalDocumentSize += documentSize;
			responseWriter.key("documentSize").value(documentSize);
			
			String createTime = "";
			SegmentInfo segmentInfo = shardContext.dataInfo().getLastSegmentInfo();
			if(segmentInfo != null){
				RevisionInfo revisionInfo = segmentInfo.getRevisionInfo();
				if(revisionInfo != null){
					createTime = revisionInfo.getCreateTime();
				}
			}
			responseWriter.key("createTime").value(createTime);
			
			responseWriter.endObject();
		}
		
		responseWriter.endArray();
		responseWriter.key("totalDiskSize").value(FileUtils.byteCountToDisplaySize(totalByteCount))
		.key("totalDocumentSize").value(totalDocumentSize);
		responseWriter.endObject();
		responseWriter.done();
	}

}
