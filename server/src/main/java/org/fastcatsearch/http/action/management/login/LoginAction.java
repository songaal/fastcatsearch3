package org.fastcatsearch.http.action.management.login;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.SessionInfo;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/management/login")
public class LoginAction extends ServiceAction {

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {

		writeHeader(response);
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		resultWriter.object();
		
		//TODO 1. 
		//db에 id, passwd를 던져서 로그인 성공여부 확인.
		String id = request.getParameter("id");
		String password = request.getParameter("password");
		
		UserInfo userInfo = UserInfoMapper.getUserInfo(id, password);
		if(userInfo != null){
			//TODO 2.
			//db에서 내 그룹의 권한을 가져와서 authorityMap에 채워준다.
			String groupCode = userInfo.getGroupCode();
			Map<ActionAuthority, ActionAuthorityLevel> authorityMap = GroupAuthorityMapper.getAuthority(groupCode);
			
			//임시...
			authorityMap = new HashMap<ActionAuthority, ActionAuthorityLevel>();
			
			//TODO admin 의 경우 모든 권한을 가지는 map을 어떻게 넣어줄까?
			session.setAttribute(AuthAction.AUTH_KEY, new SessionInfo(id, authorityMap));
			
			resultWriter.key("status").value("0");
		}else{
			//로그인 실패.
			resultWriter.key("status").value("1");
		}
	
		resultWriter.key("id").value(id);
		
		resultWriter.endObject();
		resultWriter.done();
		writer.close();
		
		
		
	
		
		
		
		
	}

}
