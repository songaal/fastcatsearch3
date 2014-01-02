package org.fastcatsearch.http.action.management.settings;

import java.io.Writer;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.GroupAccountMapper;
import org.fastcatsearch.db.mapper.GroupAuthorityMapper;
import org.fastcatsearch.db.vo.GroupAccountVO;
import org.fastcatsearch.db.vo.GroupAuthorityVO;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping (value="/settings/authority/update-group-authority",authority=ActionAuthority.Settings, authorityLevel=ActionAuthorityLevel.WRITABLE)
public class PutGroupAuthorityAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response)
			throws Exception {
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
	
		
		MapperSession<GroupAuthorityMapper> groupAuthoritySession = null;
		MapperSession<GroupAccountMapper> groupAccountSession = null;
		
		try {
		
			groupAuthoritySession = DBService.getInstance().getMapperSession(GroupAuthorityMapper.class);
			
			groupAccountSession = DBService.getInstance().getMapperSession(GroupAccountMapper.class);
			
			GroupAuthorityMapper groupAuthorityMapper = (GroupAuthorityMapper)
					groupAuthoritySession.getMapper();
			
			GroupAccountMapper groupAccountMapper = (GroupAccountMapper) 
					groupAccountSession.getMapper();
			
			if(groupAccountMapper!=null && groupAuthorityMapper!=null) {
				
				String mode = request.getParameter("mode");
				
				if("update".equals(mode)) {

					int groupId = request.getIntParameter("groupId", 0);

					String groupName = request.getParameter("groupName");

					GroupAccountVO groupAccountVO = null;
					GroupAuthorityVO groupAuthorityVO = null;

					if(groupId != -1) {
						groupAccountVO = groupAccountMapper.getEntry(groupId);
						groupAccountVO.groupName = groupName;
						groupAccountMapper.updateEntry(groupAccountVO);
					} else {
						synchronized(groupAccountMapper) {
							groupAccountVO = new GroupAccountVO();
							groupAccountVO.groupName = groupName;
							groupAccountMapper.putEntry(groupAccountVO);
							groupId = groupAccountVO.id;
						}
					}
					
					ActionAuthority[] authorities = ActionAuthority.values();
					for(ActionAuthority authority : authorities) {
						if(authority == ActionAuthority.NULL) {
							continue;
						}
						String authorityCode = authority.name();
						String authorityLevel = request.getParameter("authorityLevel_"+authorityCode);
						if(groupId != -1 && authorityCode!=null && !"".equals(authorityCode)) {
							groupAuthorityVO = groupAuthorityMapper.getEntry(groupId, authorityCode);
							if(groupAuthorityVO!=null) {
								groupAuthorityVO.authorityLevel = authorityLevel;
								groupAuthorityMapper.updateEntry(groupAuthorityVO);
							} else {
								synchronized(groupAuthorityMapper) {
									groupAuthorityVO = new GroupAuthorityVO(groupId, authorityCode, authorityLevel);
									groupAuthorityMapper.putEntry(groupAuthorityVO);
								}
							}
						}
					}
				} else if("delete".equals(mode)) {
					int groupId = request.getIntParameter("groupId", 0);
					groupAuthorityMapper.deleteEntry(groupId);
					groupAccountMapper.deleteEntry(groupId);
				}
				resultWriter.object().key("success").value("true")
						.key("status").value(1).endObject();
			}
		} catch (Exception e) {
			logger.error("",e);
			resultWriter.object().key("success").value("false")
					.key("status").value(1).endObject();
		} finally {
			if(groupAccountSession!=null) try {
				groupAccountSession.commit();
				groupAccountSession.closeSession();
			} catch (Exception e) { }
			
			if(groupAuthoritySession!=null) try {
				groupAuthoritySession.commit();
				groupAuthoritySession.closeSession();
			} catch (Exception e) { }
		}
		
		resultWriter.done();
	}
}
