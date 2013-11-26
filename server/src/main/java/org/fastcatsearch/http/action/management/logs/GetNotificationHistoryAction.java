package org.fastcatsearch.http.action.management.logs;

import java.io.Writer;
import java.util.List;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.NotificationHistoryMapper;
import org.fastcatsearch.db.vo.NotificationVO;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value="/management/logs/notification-history", authority=ActionAuthority.Logs)
public class GetNotificationHistoryAction extends AuthAction {
	
	private static final int ROW_SIZE = 15;
	private static final int PAGE_SIZE = 10;

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		DBService dbService = DBService.getInstance();
		
		MapperSession<NotificationHistoryMapper> session = null;
		
		int pageNum = request.getIntParameter("pageNum",1);
		
		if(pageNum < 1) { pageNum = 1; }
		
		int rowStarts = (pageNum-1) * ROW_SIZE + 1;
		
		int rowFinish = (rowStarts) + ROW_SIZE - 1;
		
		int totalSize = 0;
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
		try {
		
			session = dbService.getMapperSession(NotificationHistoryMapper.class);
			
			NotificationHistoryMapper mapper = session.getMapper();
			
			List<NotificationVO> entryList = mapper.getEntryList(rowStarts, rowFinish);
			
			totalSize = mapper.getCount();
			
			if(rowFinish > totalSize) {
				rowFinish = totalSize;
			}
			
			resultWriter.object().key("totalSize").value(totalSize)
				.key("pageNum").value(pageNum)
				.key("rowSize").value(ROW_SIZE)
				.key("pageSize").value(PAGE_SIZE)
				.key("rowStarts").value(rowStarts)
				.key("rowFinish").value(rowFinish)
				.key("notifications").array();
			
			for(int inx=0; inx < entryList.size(); inx++) {
				
				NotificationVO entry = entryList.get(inx);
				
				resultWriter.object()
					.key("id").value(entry.id)
					.key("node").value(entry.node)
					.key("messageCode").value(entry.messageCode)
					.key("message").value(entry.message)
					.key("regtime").value(entry.regtime)
					.endObject();
				
			}
			resultWriter.endArray().endObject();
		
		} finally {
			
			if(session!=null) {
				session.closeSession();
			}
		}
		
		resultWriter.done();
	}
}
