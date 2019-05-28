package org.fastcatsearch.http.action.management.collections;

import org.fastcatsearch.util.FileUtils;
import org.fastcatsearch.env.Path;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

import java.io.File;
import java.io.Writer;
import java.util.Date;

@ActionMapping(value = "/management/collections/indexing-status", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.READABLE)
public class GetIndexingStatusAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		String collectionId = request.getParameter("collectionId");
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		CollectionContext collectionContext = irService.collectionContext(collectionId);
		
		
		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		responseWriter.object()
		.key("collectionId").value(collectionId);
		
		DataInfo dataInfo = collectionContext.dataInfo();
		
		int segmentSize = dataInfo.getSegmentSize();
		String revisionUUID = "";

		responseWriter.key("segmentSize").value(segmentSize)
		.key("revisionUUID").value(revisionUUID);
		
		int sequence = collectionContext.indexStatus().getSequence();
		File indexFileDir = collectionContext.dataFilePaths().indexDirFile(sequence);
		String dataPath = new Path(collectionContext.collectionFilePaths().file()).relativise(indexFileDir).getPath();
		responseWriter.key("sequence").value(sequence)
			.key("dataPath").value(dataPath);
		String diskSize = "";
		
		if(indexFileDir.exists()){
			long byteCount = FileUtils.sizeOfDirectorySafe(indexFileDir);
			diskSize = FileUtils.byteCountToDisplaySize(byteCount);
		}
		responseWriter.key("diskSize").value(diskSize);
		
		int documentSize = collectionContext.dataInfo().getDocuments();
		responseWriter.key("documentSize").value(documentSize);

		SegmentInfo lastSegmentInfo = collectionContext.dataInfo().getLatestSegmentInfo();
		String createTime = null;
		if(lastSegmentInfo != null) {
			createTime = Formatter.formatDate(new Date(lastSegmentInfo.getCreateTime()));
		} else {
			createTime = "";
		}
		responseWriter.key("createTime").value(createTime);
		
		responseWriter.endObject();
		
		responseWriter.done();
	}

}
