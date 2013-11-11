package org.fastcatsearch.http.action.setting;

import java.io.Writer;
import java.util.List;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.mapper.GroupAccountMapper;
import org.fastcatsearch.db.vo.GroupAccountVO;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping (value="/setting/authority/group-list")
public class GroupListAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response)
			throws Exception {
		GroupAccountMapper groupAccountMapper = (GroupAccountMapper) 
				DBService.getInstance().getMapperSession(GroupAccountMapper.class).getMapper();
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
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
		resultWriter.done();
	}
}
