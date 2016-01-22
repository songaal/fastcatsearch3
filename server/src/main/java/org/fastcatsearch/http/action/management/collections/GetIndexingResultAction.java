package org.fastcatsearch.http.action.management.collections;

import java.io.Writer;
import java.util.Date;
import java.util.List;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
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

@ActionMapping(value = "/management/collections/indexing-result", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.READABLE)
public class GetIndexingResultAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		String collectionId = request.getParameter("collectionId");

		// TODO collection의 index node인지 확인하고 해당 노드가 아니면 전달하여 받아온다.
		// 해당노드이면 그대로 수행한다.

		DBService dbService = ServiceManager.getInstance().getService(DBService.class);
		MapperSession<IndexingResultMapper> mapperSession = dbService.getMapperSession(IndexingResultMapper.class);
		IndexingResultMapper indexingResultMapper = mapperSession.getMapper();
		List<IndexingStatusVO> indexingResultList = indexingResultMapper.getEntryList(collectionId);

		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);

		resultWriter.object().key("indexingResult").object();

		try {
			if (indexingResultList != null) {
				for (IndexingStatusVO vo : indexingResultList) {
					resultWriter.key(vo.type.name()).object()
						.key("collectionId").value(vo.collectionId)
						.key("status").value(vo.status.name())
						.key("docSize").value(vo.docSize)
						.key("deleteSize").value(vo.deleteSize)
						.key("isScheduled").value(vo.isScheduled ? "Scheduled" : "Manual")
						.key("startTime").value(Formatter.formatDate(new Date(vo.startTime.getTime())))
						.key("endTime").value(Formatter.formatDate(new Date(vo.endTime.getTime())))
						.key("duration").value(Formatter.getFormatTime(vo.duration))
					.endObject();
				}
			}
		} finally {
			mapperSession.closeSession();
		}
		
		resultWriter.endObject().endObject();

		resultWriter.done();

	}

}
