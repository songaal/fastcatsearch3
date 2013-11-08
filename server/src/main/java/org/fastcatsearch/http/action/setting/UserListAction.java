package org.fastcatsearch.http.action.setting;

import java.io.Writer;
import java.util.List;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.mapper.UserAccountMapper;
import org.fastcatsearch.db.vo.UserAccountVO;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping (value="/setting/authority/user-list")
public class UserListAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response)
			throws Exception {
		
		UserAccountMapper userAccountMapper = (UserAccountMapper) 
				DBService.getInstance().getMapperSession(UserAccountMapper.class);
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
		int totalSize = 0;
		
		if(userAccountMapper!=null) {
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
					.key("groupId").value(user.groupId)
					.endObject();
			}
			resultWriter.endArray().endObject();
		}
		resultWriter.done();
	}
}
