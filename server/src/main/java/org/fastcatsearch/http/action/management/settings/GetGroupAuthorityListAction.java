package org.fastcatsearch.http.action.management.settings;

import java.io.Writer;
import java.util.List;

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
import org.fastcatsearch.util.ResultWriterException;

@ActionMapping (value="/settings/authority/get-group-authority-list", authority=ActionAuthority.Settings, authorityLevel=ActionAuthorityLevel.WRITABLE)
public class GetGroupAuthorityListAction extends AuthAction {
	
	private ActionAuthority[] authorities;
	private ActionAuthorityLevel[] authorityLevels;
	private List<GroupAuthorityVO>authorityList;
	private GroupAuthorityMapper groupAuthorityMapper;
	private GroupAccountMapper groupAccountMapper;
	private ResponseWriter resultWriter;

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response)
			throws Exception {
		
		Writer writer = response.getWriter();
		resultWriter = getDefaultResponseWriter(writer);

		
		MapperSession<GroupAuthorityMapper> groupAuthoritySession = null;
		
		MapperSession<GroupAccountMapper> groupAccountSession = null;
		
		try {
		
			groupAuthoritySession = DBService.getInstance().getMapperSession(GroupAuthorityMapper.class);
			
			groupAccountSession = DBService.getInstance().getMapperSession(GroupAccountMapper.class);
			
			groupAuthorityMapper = (GroupAuthorityMapper) 
					groupAuthoritySession.getMapper();
			
			groupAccountMapper = (GroupAccountMapper)
					groupAccountSession.getMapper();
			
			int groupId = request.getIntParameter("groupId", 0);
			
			String mode = request.getParameter("mode");
			
			authorities = ActionAuthority.values();
			authorityLevels = ActionAuthorityLevel.values();
			
			if(groupAuthorityMapper!=null && groupAccountMapper!=null) {
				if(groupId == -1) {
					GroupAccountVO groupAccountVO = new GroupAccountVO();
					groupAccountVO.id = -1;
					resultWriter.object();
					writeAuthorityLevel();
					writeAuthority();
					resultWriter
						.key("groupCount").value(1)
						.key("groupList").array();
					writeGroup(groupAccountVO);
					resultWriter.endArray().endObject();
					
				} else if("all".equals(mode)) {
					List<GroupAccountVO> groupList = groupAccountMapper.getEntryList();
					
					resultWriter.object();
					writeAuthorityLevel();
					writeAuthority();
					resultWriter
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
					resultWriter.object();
					writeAuthorityLevel();
					writeAuthority();
					resultWriter
						.key("groupCount").value(count)
						.key("groupList").array();
					writeGroup(groupAccountVO);
					resultWriter.endArray().endObject();
				}
			}
		} finally {
			
			if(groupAccountSession!=null) try {
				groupAccountSession.closeSession();
			} catch (Exception e) { }
			
			if(groupAuthoritySession!=null) try {
				groupAuthoritySession.closeSession();
			} catch (Exception e) { }
		}

		resultWriter.done();
	}
	
	public void writeAuthority() throws ResultWriterException {
		resultWriter.key("groupAuthorities").array();
		for(ActionAuthority authority : authorities) {
			if(authority == ActionAuthority.NULL) { continue; }
			resultWriter.object()
				.key("authorityCode").value(authority.name())
				.key("authorityName").value(authority.getName()).endObject();
		}
		resultWriter.endArray();
	}
	
	public void writeAuthorityLevel() throws ResultWriterException {
		resultWriter.key("authorityLevel").array();
		for(ActionAuthorityLevel level : authorityLevels) {
			resultWriter.value(level.name());
		}
		resultWriter.endArray();
		
	}
	
	public void writeGroup(GroupAccountVO groupAccountVO) throws Exception {
		if(groupAccountVO!=null) {
			authorityList = groupAuthorityMapper.getEntryList(groupAccountVO.id);
			resultWriter.object().key("groupId").value(groupAccountVO.id)
				.key("groupName").value(groupAccountVO.groupName)
				.key("authorities").array();
			writeGroupAuthority(authorityList);
			resultWriter.endArray().endObject();
		}
	}
	
	public void writeGroupAuthority(List<GroupAuthorityVO> authorityList) throws ResultWriterException {
		for(ActionAuthority authority : authorities) {
			
			if(authority == ActionAuthority.NULL) { continue; }
			
			ActionAuthorityLevel level = ActionAuthorityLevel.NONE;
			
			GroupAuthorityVO authorityVO = null;
			if(authorityList!=null) {
				for(int inx=0;inx<authorityList.size();inx++) {
					authorityVO = authorityList.get(inx);
					if(authority.name().equals(authorityVO.authorityCode)) {
						try {
							level = ActionAuthorityLevel.valueOf(authorityVO.authorityLevel);
						} catch (IllegalArgumentException e) { }
						break;
					}
				}
			}
			resultWriter.value(level.name());
		}
	}
}
