package org.fastcatsearch.http.action.management.settings;

import java.io.Writer;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.GroupAccountMapper;
import org.fastcatsearch.db.vo.GroupAccountVO;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping (value="/settings/authority/update-group", authority=ActionAuthority.Settings, authorityLevel=ActionAuthorityLevel.WRITABLE)
public class PutGroupAccountAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response)
			throws Exception {
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
		MapperSession<GroupAccountMapper> groupAccountSession = null;
		
		try {
		
			groupAccountSession = DBService.getInstance().getMapperSession(GroupAccountMapper.class);
			
			GroupAccountMapper groupAccountMapper = (GroupAccountMapper) 
					groupAccountSession.getMapper();
			
			if(groupAccountMapper!=null) {
				
				int groupId = request.getIntParameter("groupId",-1);
				String groupName = request.getParameter("groupName");
				
				GroupAccountVO vo = null;
				
				synchronized(groupAccountMapper) {
					
					if(groupId != -1) {
					
						vo = groupAccountMapper.getEntry(groupId);
					} else {
						
						groupId = groupAccountMapper.getMaxId();
						vo = new GroupAccountVO();
						vo.id = groupId;
					}
					
					vo.groupName = groupName;
				}
				
				resultWriter.object();
				resultWriter.endObject();
			}
		} finally {
			if(groupAccountSession!=null) try {
				groupAccountSession.closeSession();
			} catch (Exception e) { }
		}
		resultWriter.done();
	}
}