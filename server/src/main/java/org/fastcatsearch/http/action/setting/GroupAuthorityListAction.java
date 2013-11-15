package org.fastcatsearch.http.action.setting;

import java.io.Writer;
import java.util.List;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.mapper.GroupAccountMapper;
import org.fastcatsearch.db.mapper.GroupAuthorityMapper;
import org.fastcatsearch.db.vo.GroupAccountVO;
import org.fastcatsearch.db.vo.GroupAuthorityVO;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.util.ResultWriterException;

@ActionMapping (value="/setting/authority/group-authority-list")
public class GroupAuthorityListAction extends AuthAction {
	
	private ActionAuthority[] authorities;
	private List<GroupAuthorityVO>authorityList;
	private GroupAuthorityMapper groupAuthorityMapper;
	private GroupAccountMapper groupAccountMapper;
	private ResponseWriter resultWriter;

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response)
			throws Exception {
		groupAuthorityMapper = (GroupAuthorityMapper) 
				DBService.getInstance().getMapperSession(GroupAuthorityMapper.class).getMapper();
		
		groupAccountMapper = (GroupAccountMapper)
				DBService.getInstance().getMapperSession(GroupAccountMapper.class).getMapper();
		
		Writer writer = response.getWriter();
		resultWriter = getDefaultResponseWriter(writer);
		
		int groupId = request.getIntParameter("groupId", 0);
		
		String mode = request.getParameter("mode");
		
		authorities = ActionAuthority.values();
		if(groupAuthorityMapper!=null && groupAccountMapper!=null) {
			if(groupId == -1) {
				GroupAccountVO groupAccountVO = new GroupAccountVO();
				groupAccountVO.id = -1;
				resultWriter.object()
					.key("groupCount").value(1)
					.key("groupList").array();
				writeGroup(groupAccountVO);
				resultWriter.endArray().endObject();
				
			} else if("all".equals(mode)) {
				List<GroupAccountVO> groupList = groupAccountMapper.getEntryList();
				
				resultWriter.object()
					.key("groupCount").value(groupList.size())
					.key("groupList").array();
				for(GroupAccountVO groupAccountVO : groupList) {
					writeGroup(groupAccountVO);
				}
				resultWriter.endArray().endObject();
			} else {
				GroupAccountVO groupAccountVO = groupAccountMapper.getEntry(groupId);
				int count = 0;
				if(groupAccountVO!=null) {
					count = 1;
				}
				resultWriter.object()
					.key("groupCount").value(count)
					.key("groupList").array();
				writeGroup(groupAccountVO);
				resultWriter.endArray().endObject();
			}
		}

		resultWriter.done();
	}
	
	public void writeGroup(GroupAccountVO groupAccountVO) throws Exception {
		if(groupAccountVO!=null) {
			authorityList = groupAuthorityMapper.getEntryList(groupAccountVO.id);
			resultWriter.object().key("groupId").value(groupAccountVO.id)
				.key("groupName").value(groupAccountVO.groupName)
				.key("authorities").array();
			writeAuthority(authorityList);
			resultWriter.endArray().endObject();
		}
	}
	
	public void writeAuthority(List<GroupAuthorityVO> authorityList) throws ResultWriterException {
		for(ActionAuthority authority : authorities) {
			
			if(authority == ActionAuthority.NULL) {
				continue;
			}
			
			boolean found = false;
			GroupAuthorityVO authorityVO = null;
			if(authorityList!=null) {
				for(int inx=0;inx<authorityList.size();inx++) {
					authorityVO = authorityList.get(inx);
					if(authority.name().equals(authorityVO.authorityCode)) {
						found = true;
						break;
					}
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
	}
}
