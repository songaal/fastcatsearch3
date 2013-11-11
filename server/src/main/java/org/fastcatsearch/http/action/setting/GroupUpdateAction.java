package org.fastcatsearch.http.action.setting;

import java.io.Writer;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.mapper.GroupAccountMapper;
import org.fastcatsearch.db.vo.GroupAccountVO;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping (value="/setting/authority/group-update")
public class GroupUpdateAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response)
			throws Exception {
		
		GroupAccountMapper groupAccountMapper = (GroupAccountMapper) 
				DBService.getInstance().getMapperSession(GroupAccountMapper.class).getMapper();
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
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
		resultWriter.done();
	}
}