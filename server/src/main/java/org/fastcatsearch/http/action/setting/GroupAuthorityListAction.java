package org.fastcatsearch.http.action.setting;

import java.io.Writer;
import java.util.List;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.mapper.GroupAuthorityMapper;
import org.fastcatsearch.db.vo.GroupAuthorityVO;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping (value="/setting/authority/group-authority-list")
public class GroupAuthorityListAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response)
			throws Exception {
		GroupAuthorityMapper groupAuthorityMapper = (GroupAuthorityMapper) 
				DBService.getInstance().getMapperSession(GroupAuthorityMapper.class);
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
		int groupId = request.getIntParameter("groupId", -1);
		
		if(groupAuthorityMapper!=null && groupId!=-1) {
			List<GroupAuthorityVO>authorityList = groupAuthorityMapper.getEntryList(groupId);
			ActionAuthority[] authorities = ActionAuthority.values();

			resultWriter.object()
				.key("totalSize").value(authorities.length)
				.key("groupAuthorityList").array();
			for(ActionAuthority authority : authorities) {
				boolean found = false;
				GroupAuthorityVO authorityVO = null;
				for(int inx=0;inx<authorityList.size();inx++) {
					authorityVO = authorityList.get(inx);
					if(authority.getName().equals(authorityVO.authorityCode)) {
						found = true;
						break;
					}
				}
				
				if(found && authorityVO!=null) {
					resultWriter.object()
						.key("authorityCode").value(authority.name())
						.key("authorityName").value(authority.getName())
						.key("authorityLevel").value(authorityVO.authorityLevel)
						.endObject();
				} else {
					resultWriter.object()
						.key("authorityCode").value(authority.name())
						.key("authorityName").value(authority.getName())
						.key("authorityLevel").value("")
						.endObject();
				}
			}
			
			resultWriter.endArray().endObject();
		}
		resultWriter.done();

	}

}
