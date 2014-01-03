package org.fastcatsearch.http.action.management.logs;

import java.io.Writer;

import org.fastcatsearch.db.vo.NotificationConfigVO;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.notification.NotificationService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value="/management/logs/update-notification-setting", authority=ActionAuthority.Logs, authorityLevel=ActionAuthorityLevel.WRITABLE)
public class UpdateNotificationAlertSettingAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		boolean isSuccess = false;
		
		String mode = request.getParameter("mode");
		
		int id = request.getIntParameter("id",-1);
		
		String code = request.getParameter("code");

		String alertTo = request.getParameter("alertTo");
		
		ServiceManager serviceManager = ServiceManager.getInstance();
		
		NotificationService service = serviceManager.getService(NotificationService.class);
		
		try {

			NotificationConfigVO vo = new NotificationConfigVO();
				
			
			if(id!=-1) {
				
				if("delete".equals(mode)) {
					
					service.deleteNotificationConfig(code);
				} else {
				
					vo.setId(id);
					vo.setCode(code);
					vo.setAlertTo(alertTo);
					service.updateNotificationConfig(vo);
				}
				
			} else {
				
				vo.setCode(code);
				vo.setAlertTo(alertTo);
				service.putNotificationConfig(vo);
				
			}
			
			isSuccess = true;
			
			
		} catch (Exception e) {
			
			logger.error("",e);
		} finally {
			
			service.close();
		}
		
		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		responseWriter.object();
		responseWriter.key("success").value(isSuccess);
		responseWriter.endObject();
		responseWriter.done();
	}
}
