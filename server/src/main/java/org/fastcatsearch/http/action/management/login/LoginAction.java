package org.fastcatsearch.http.action.management.login;

import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.GroupAuthorityMapper;
import org.fastcatsearch.db.mapper.UserAccountMapper;
import org.fastcatsearch.db.vo.GroupAuthorityVO;
import org.fastcatsearch.db.vo.UserAccountVO;
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

		String userId = request.getParameter("id");
		String password = request.getParameter("password");

		MapperSession<UserAccountMapper> userAccountSession = null;

		MapperSession<GroupAuthorityMapper> groupAuthoritySession = null;

		try {

			userAccountSession = DBService.getInstance().getMapperSession(UserAccountMapper.class);

			groupAuthoritySession = DBService.getInstance().getMapperSession(GroupAuthorityMapper.class);

			UserAccountMapper userAccountMapper = (UserAccountMapper) userAccountSession.getMapper();
			GroupAuthorityMapper groupAuthorityMapper = (GroupAuthorityMapper) groupAuthoritySession.getMapper();

			// db에 id, passwd를 던져서 로그인 성공여부 확인.
			UserAccountVO userInfo = userAccountMapper.getEntryByUserId(userId);
			boolean isCorrectPassword = false;
			if (userInfo != null) {
				isCorrectPassword = userInfo.isEqualsEncryptedPassword(password);
			}

            Map<ActionAuthority, ActionAuthorityLevel> authorityMap = new HashMap<ActionAuthority, ActionAuthorityLevel>();
            if (isCorrectPassword) {
				try {

					// db에서 내 그룹의 권한을 가져와서 authorityMap에 채워준다.
					int groupId = userInfo.groupId;
					List<GroupAuthorityVO> authorityList = groupAuthorityMapper.getEntryList(groupId);
					for (GroupAuthorityVO authority : authorityList) {
						authorityMap.put(ActionAuthority.valueOf(authority.authorityCode),
								ActionAuthorityLevel.valueOf(authority.authorityLevel));
					}
					if (authorityMap != null && authorityMap.size() != 0) {
						session.setAttribute(AuthAction.AUTH_KEY, new SessionInfo(userId, authorityMap));
					}
				} catch (Exception e) {
					userInfo = null;
					logger.error("", e);
				} finally {
				}
			}

			if (isCorrectPassword) {
				resultWriter.key("status").value("0");
				resultWriter.key("name").value(userInfo.name);
                resultWriter.key("authority").object();
                for(Map.Entry<ActionAuthority, ActionAuthorityLevel> entry : authorityMap.entrySet()) {
                    resultWriter.key(entry.getKey().name().toLowerCase()).value(entry.getValue().name());
                }
                resultWriter.endObject();
			} else {
				// 로그인 실패.
				resultWriter.key("status").value("1");
			}

			resultWriter.endObject();
		} finally {
			if (userAccountSession != null)
				try {
					userAccountSession.closeSession();
				} catch (Exception e) {
				}

			if (groupAuthoritySession != null)
				try {
					groupAuthoritySession.closeSession();
				} catch (Exception e) {
				}
		}
		resultWriter.done();
		writer.close();
	}
}
