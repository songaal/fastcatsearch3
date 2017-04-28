package org.fastcatsearch.http.action.management.settings;

import java.io.Writer;
import java.util.List;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.UserAccountMapper;
import org.fastcatsearch.db.vo.UserAccountVO;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping (value="/settings/authority/get-user-list", authority=ActionAuthority.Settings, authorityLevel=ActionAuthorityLevel.READABLE)
public class GetUserListAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response)
			throws Exception {
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
		MapperSession<UserAccountMapper> userAccountSession = null;
		
		try {
			
			userAccountSession = DBService.getInstance().getMapperSession(UserAccountMapper.class);
			
			UserAccountMapper userAccountMapper = (UserAccountMapper) 
					userAccountSession.getMapper();
			
			int id = request.getIntParameter("id", 0);
			
			int totalSize = 0;
			
			if(userAccountMapper!=null) {
				
				if(id==0) {
					
					totalSize = userAccountMapper.getCount();
					List<UserAccountVO> userList = userAccountMapper.getEntryList();
					
					resultWriter.object()
						.key("totalSize").value(totalSize)
						.key("userList").array();
					for(UserAccountVO user : userList) {
						resultWriter.object()
							.key("id").value(user.id)
							.key("name").value(user.name)
							.key("userId").value(user.userId)
							.key("email").value(user.email)
							.key("sms").value(user.sms)
							.key("telegram").value(user.telegram)
							.key("groupId").value(user.groupId)
							.endObject();
					}
					resultWriter.endArray().endObject();
				} else {
					UserAccountVO vo = userAccountMapper.getEntry(id);
					
					if(vo!=null) {
					
						resultWriter.object()
							.key("totalSize").value(1)
							.key("userList").array()
								.object()
									.key("id").value(vo.id)
									.key("name").value(vo.name)
									.key("userId").value(vo.userId)
									.key("email").value(vo.email)
									.key("sms").value(vo.sms)
									.key("telegram").value(vo.telegram)
									.key("groupId").value(vo.groupId)
								.endObject()
							.endArray().endObject();
					} else {
						resultWriter.object()
							.key("totalSize").value(0)
							.key("userList").array().endArray().endObject();
					}
				}
			}
		} finally {
			if(userAccountSession!=null) try {
				userAccountSession.closeSession();
			} catch (Exception e) { }
		}
		resultWriter.done();
	}
}
