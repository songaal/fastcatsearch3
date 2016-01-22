package org.fastcatsearch.http.action.management.collections;

import java.io.Writer;
import java.util.List;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.IndexingHistoryMapper;
import org.fastcatsearch.db.mapper.IndexingResultMapper;
import org.fastcatsearch.db.vo.IndexingStatusVO;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/management/collections/indexing-history", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.READABLE)
public class GetIndexingHistoryAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response)
			throws Exception {

		String collectionId = request.getParameter("collectionId");
		int start = request.getIntParameter("start");
		int end = request.getIntParameter("end"); 
		
		DBService dbService = ServiceManager.getInstance().getService(DBService.class);
		MapperSession<IndexingHistoryMapper> mapperSession = dbService
				.getMapperSession(IndexingHistoryMapper.class);
		IndexingHistoryMapper indexingHistoryMapper = mapperSession.getMapper();
		
		try {
			
			int totalSize = indexingHistoryMapper.getCount(collectionId);
			
			List<IndexingStatusVO> indexHistoryList = indexingHistoryMapper.getEntryList(collectionId, start, end);
	
			Writer writer = response.getWriter();
			ResponseWriter resultWriter = getDefaultResponseWriter(writer);
	
			resultWriter.object()
			.key("totalSize").value(totalSize)
			.key("indexingHistory").array();

		
			if (indexHistoryList != null) {
				for (IndexingStatusVO vo : indexHistoryList) {
					resultWriter.object()
						.key("id").value(vo.id)
						.key("collectionId").value(vo.collectionId)
						.key("type").value(vo.type.name())
						.key("step").value(vo.step)
						.key("status").value(vo.status.name())
						.key("docSize").value(vo.docSize)
						.key("deleteSize").value(vo.deleteSize)
						.key("isScheduled").value(vo.isScheduled ? "Scheduled" : "Manual")
						.key("startTime").value(vo.startTime)
						.key("endTime").value(vo.endTime)
						.key("duration").value(Formatter.getFormatTime(vo.duration))
					.endObject();
				}
			}
			
			resultWriter.endArray().endObject();
			
			resultWriter.done();
			
		} finally {
			mapperSession.closeSession();
		}
		

	}

}
