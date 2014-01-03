package org.fastcatsearch.http.action.management.settings;

import java.io.Writer;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.GroupAccountMapper;
import org.fastcatsearch.db.mapper.UserAccountMapper;
import org.fastcatsearch.db.vo.GroupAccountVO;
import org.fastcatsearch.db.vo.UserAccountVO;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.SessionInfo;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping (value="/settings/authority/get-my-info", authority=ActionAuthority.Settings, authorityLevel=ActionAuthorityLevel.NONE)
public class GetMySettingsAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response)
			throws Exception {
		
		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		
		MapperSession<UserAccountMapper> userSession = null;
		MapperSession<GroupAccountMapper> groupSession = null;
		
		try {
			userSession = DBService.getInstance().getMapperSession(UserAccountMapper.class);
			groupSession = DBService.getInstance().getMapperSession(GroupAccountMapper.class);
			
			UserAccountMapper userMapper = userSession.getMapper();
			GroupAccountMapper groupMapper = groupSession.getMapper();
			
			SessionInfo sessionInfo = (SessionInfo) super.session
					.getAttribute(AuthAction.AUTH_KEY);
			
			if(sessionInfo != null) {
				UserAccountVO userVo = userMapper.getEntryByUserId(sessionInfo.getUserId());
				GroupAccountVO groupVo = groupMapper.getEntry(userVo.groupId);
				
				String groupName = "";
				if(groupVo!=null) {
					groupName = groupVo.groupName;
				}
				
				responseWriter.object()
					.key("id").value(userVo.id)
					.key("groupName").value(groupName)
					.key("userId").value(userVo.userId)
					.key("name").value(userVo.name)
					.key("email").value(userVo.email)
					.key("sms").value(userVo.sms)
					.endObject();
			}
			
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			
			if (userSession != null) {
				userSession.closeSession();
			}
			
			if (groupSession != null) {
				groupSession.closeSession();
			}
		}
		
		responseWriter.done();
	}
}
