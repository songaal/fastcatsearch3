package org.fastcatsearch.http.action.management.logs;

import java.io.Writer;
import java.util.List;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.NotificationConfigMapper;
import org.fastcatsearch.db.vo.NotificationConfigVO;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.notification.message.Notification;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value="/management/logs/notification-alert-setting-list", authority=ActionAuthority.Logs)
public class GetNotificationAlertSettingListAction extends AuthAction {
	
	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		DBService dbService = DBService.getInstance();
		
		MapperSession<NotificationConfigMapper> session = null;
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
		try {
		
			session = dbService.getMapperSession(NotificationConfigMapper.class);
			
			NotificationConfigMapper mapper = session.getMapper();
			
			List<NotificationConfigVO> entryList = null;
			
			entryList = mapper.getEntryList();
			
			resultWriter.object()
				.key("setting-list").array();
			
			
			for(int inx=0; inx < entryList.size(); inx++) {
				
				NotificationConfigVO entry = entryList.get(inx);
				
				String code = entry.getCode();
				String codeType = Notification.getNotificationCodeDefinition().get(code);
				
				resultWriter.object()
					.key("id").value(entry.getId())
					.key("code").value(code)
					.key("codeType").value(codeType)
					.key("alertTo").value(entry.getAlertTo())
					.endObject();
				
			}
			resultWriter.endArray().endObject();
		
		} finally {
			
			if (session != null) {
				session.closeSession();
			}
		}
		
		resultWriter.done();
	}
}
