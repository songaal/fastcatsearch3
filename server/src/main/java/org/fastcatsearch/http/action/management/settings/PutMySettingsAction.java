package org.fastcatsearch.http.action.management.settings;

import java.io.Writer;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.UserAccountMapper;
import org.fastcatsearch.db.vo.UserAccountVO;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.SessionInfo;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/settings/authority/put-my-info", authority = ActionAuthority.Settings, authorityLevel = ActionAuthorityLevel.NONE)
public class PutMySettingsAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);

		MapperSession<UserAccountMapper> session = null;

		String name = request.getParameter("name");
		String email = request.getParameter("email");
		String sms = request.getParameter("sms");
		String password = request.getParameter("password");
		String newPassword = request.getParameter("newPassword");
		String reqPassword = request.getParameter("reqPassword");

		boolean updated = false;

		String message = "";

		try {
			session = DBService.getInstance().getMapperSession(UserAccountMapper.class);

			UserAccountMapper mapper = session.getMapper();

			SessionInfo sessionInfo = (SessionInfo) super.session.getAttribute(AuthAction.AUTH_KEY);

			if (sessionInfo != null) {

				String userId = sessionInfo.getUserId();
				UserAccountVO vo = mapper.getEntryByUserId(userId);

				if (vo != null) {

					vo.name = name;
					vo.email = email;
					vo.sms = sms;

					if (password != null && password.length() > 0) {
						// 패스워드까지 업데이트이다.
						// 패스워드 업데이트시는 현 패스워드가 일치하는지 한번더 확인한다.
						if (vo.isEqualsEncryptedPassword(password)) {
							if (newPassword != null && !"".equals(newPassword)) {

								if (!newPassword.equals(reqPassword)) {
									throw new Exception((message = "password not match"));
								}

								vo.setEncryptedPassword(newPassword);
							}
						} else {
							throw new Exception((message = "old password incorrect"));
						}
					}

					mapper.updateEntry(vo);

					updated = true;
				} else {

					throw new Exception((message = "not authorized password"));
				}
			}

		} catch (Exception e) {
			logger.error("", e);
		} finally {

			if (session != null) {
				session.closeSession();
			}
		}
		if (updated) {
			responseWriter.object().key("success").value("true").key("status").value(1).endObject();
		} else {
			responseWriter.object().key("success").value("false").key("status").value(1).key("message").value(message).endObject();

		}

		responseWriter.done();
	}
}
