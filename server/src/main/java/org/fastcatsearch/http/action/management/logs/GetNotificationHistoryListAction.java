package org.fastcatsearch.http.action.management.logs;

import java.io.Writer;
import java.util.List;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.NotificationHistoryMapper;
import org.fastcatsearch.db.vo.NotificationVO;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value="/management/logs/notification-history-list", authority=ActionAuthority.Logs, authorityLevel = ActionAuthorityLevel.READABLE)
public class GetNotificationHistoryListAction extends AuthAction {
	
	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		DBService dbService = DBService.getInstance();
		
		MapperSession<NotificationHistoryMapper> session = null;
		
		int start = request.getIntParameter("start",0);
		
		int end = request.getIntParameter("end",start);
		
		int totalCount = 0;
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
		try {
		
			session = dbService.getMapperSession(NotificationHistoryMapper.class);
			
			NotificationHistoryMapper mapper = session.getMapper();
			
			List<NotificationVO> entryList = null;
			
			totalCount = mapper.getCount();
			
			entryList = mapper.getEntryList(start, end);
			
			resultWriter.object().key("totalCount").value(totalCount)
				.key("start").value(start)
				.key("end").value(end)
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
