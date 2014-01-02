package org.fastcatsearch.http.action.management.settings;

import java.io.Writer;
import java.util.List;

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

@ActionMapping (value="/settings/authority/get-group-list", authority=ActionAuthority.Settings, authorityLevel=ActionAuthorityLevel.WRITABLE)
public class GetGroupListAction extends AuthAction {

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
			
			int totalSize = 0;
			
			if(groupAccountMapper!=null) {
				totalSize = groupAccountMapper.getCount();
				List<GroupAccountVO>groupList = groupAccountMapper.getEntryList();
				
				resultWriter.object()
					.key("totalSize").value(totalSize)
					.key("groupList").array();
				for(GroupAccountVO group : groupList) {
					resultWriter.object()
						.key("id").value(group.id)
						.key("groupName").value(group.groupName)
						.endObject();
				}
				resultWriter.endArray().endObject();
			}
		} finally {
			if(groupAccountSession!=null) try {
				groupAccountSession.closeSession();
			} catch (Exception e) { }
		}
		resultWriter.done();
	}
}
