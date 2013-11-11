package org.fastcatsearch.http.action.setting;

import java.io.Writer;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.mapper.UserAccountMapper;
import org.fastcatsearch.db.vo.UserAccountVO;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping (value="/setting/authority/user-update")
public class UserUpdateAction extends AuthAction {
	
	private static final int MODE_INSERT = 1;
	private static final int MODE_UPDATE = 2;

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response)
			throws Exception {
		
		UserAccountMapper userAccountMapper = (UserAccountMapper)
				DBService.getInstance().getMapperSession(UserAccountMapper.class).getMapper();
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
		if(userAccountMapper!=null) {
			int id = request.getIntParameter("userId", -1);
			String userName = request.getParameter("userName");
			String userId = request.getParameter("userId");
			String password = request.getParameter("password");
			String email = request.getParameter("email");
			String sms = request.getParameter("sms");
			int groupId = request.getIntParameter("groupid",-1);
			
			UserAccountVO vo = null;
			
			int mode = 0;
			
			synchronized(userAccountMapper) {
				if(id != -1) {
					vo = userAccountMapper.getEntry(id);
					if(vo!=null) {
						mode = MODE_UPDATE;
						if(password==null || "".equals(password)) {
							password = vo.password;
						}
					}
				} else {
					
					id = userAccountMapper.getMaxId() + 1;
					vo = new UserAccountVO();
					vo.id = id;
					
					mode = MODE_INSERT;
				}
				
				vo.name=userName;
				vo.userId=userId;
				vo.password=password;
				vo.email=email;
				vo.sms=sms;
				vo.groupId=groupId;
				
				if(mode == MODE_UPDATE) {
					userAccountMapper.updateEntry(vo);
				} else if(mode == MODE_INSERT) {
					userAccountMapper.putEntry(vo);
				}
			}
			
			resultWriter.object();
			resultWriter.endObject();
	
		}
		resultWriter.done();
	}
}
