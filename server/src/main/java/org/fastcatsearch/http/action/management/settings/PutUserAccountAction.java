package org.fastcatsearch.http.action.management.settings;

import java.io.Writer;

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

@ActionMapping(value = "/settings/authority/update-user", authority = ActionAuthority.Settings, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class PutUserAccountAction extends AuthAction {

	private static final int MODE_INSERT = 1;
	private static final int MODE_UPDATE = 2;

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);

		MapperSession<UserAccountMapper> userAccountSession = null;

		try {

			userAccountSession = DBService.getInstance().getMapperSession(UserAccountMapper.class);

			UserAccountMapper userAccountMapper = (UserAccountMapper) userAccountSession.getMapper();

			if (userAccountMapper != null) {
				String mode = request.getParameter("mode");

				if (("update").equals(mode)) {

					int id = request.getIntParameter("id", -1);
					String userName = request.getParameter("name");
					String userId = request.getParameter("userId");
					String password = request.getParameter("password");
					String confirmPassword = request.getParameter("confirmPassword");
					String email = request.getParameter("email");
					String sms = request.getParameter("sms");
					String telegram = request.getParameter("telegram");
					int groupId = request.getIntParameter("groupId", 0);

					UserAccountVO vo = null;

					int updateMode = 0;

					synchronized (userAccountMapper) {
						boolean doChangePassword = false;
						
						if (id != -1) {
							vo = userAccountMapper.getEntry(id);
							if (vo != null) {
								updateMode = MODE_UPDATE;
								if (password == null || "".equals(password) || !password.equals(confirmPassword)) {
									doChangePassword = true;
								}
							}
						} else {

							vo = new UserAccountVO();
							vo.id = id;

							updateMode = MODE_INSERT;
						}

						vo.name = userName;
						vo.userId = userId;
						if(!doChangePassword){
							vo.setEncryptedPassword(password);
						}
						vo.email = email;
						vo.sms = sms;
						vo.telegram = telegram;
						vo.groupId = groupId;

						if (updateMode == MODE_UPDATE) {
							userAccountMapper.updateEntry(vo);
						} else if (updateMode == MODE_INSERT) {
							userAccountMapper.putEntry(vo);
						}
					}
				} else if ("delete".equals(mode)) {
					int id = request.getIntParameter("id", 0);
					userAccountMapper.deleteEntry(id);
				}

				resultWriter.object().key("success").value("true").key("status").value(1).endObject();

			}
		} catch (Exception e) {
			logger.error("", e);
			resultWriter.object().key("success").value("false").key("status").value(1).endObject();
		} finally {
			if (userAccountSession != null)
				try {
					userAccountSession.commit();
					userAccountSession.closeSession();
				} catch (Exception e) {
				}
		}
		resultWriter.done();
	}
}
