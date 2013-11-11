package org.fastcatsearch.http.action.setting;

import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.mapper.GroupAccountMapper;
import org.fastcatsearch.db.mapper.GroupAuthorityMapper;
import org.fastcatsearch.db.vo.GroupAccountVO;
import org.fastcatsearch.db.vo.GroupAuthorityVO;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping (value="/setting/authority/group-authority-update")
public class GroupAuthorityUpdateAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response)
			throws Exception {
		
		GroupAuthorityMapper groupAuthorityMapper = (GroupAuthorityMapper)
				DBService.getInstance().getMapperSession(GroupAuthorityMapper.class).getMapper();
		
		GroupAccountMapper groupAccountMapper = (GroupAccountMapper) 
				DBService.getInstance().getMapperSession(GroupAccountMapper.class).getMapper();
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
		if(groupAccountMapper!=null && groupAuthorityMapper!=null) {
			
			Map<String,String> paramMap = request.getParameterMap();
			
			Pattern keyPtn = Pattern.compile("groupId_([0-9]+)");
			for(String key : paramMap.keySet()) {
				
				Matcher matcher = keyPtn.matcher(key);
				if(matcher.find()) {
					String num = matcher.group(1);
					
					int groupId = request.getIntParameter(key, -1);
					
					String groupName = request.getParameter("groupName_"+num);
					String authorityCode = request.getParameter("authorityCode_"+num);
					String authorityLevel = request.getParameter("authorityLevel_"+num);
					
					GroupAccountVO groupAccountVO = null;
					GroupAuthorityVO groupAuthorityVO = null;
					
					if(groupId != -1) {
						groupAccountVO = groupAccountMapper.getEntry(groupId);
						groupAccountVO.groupName = groupName;
						groupAccountMapper.updateEntry(groupAccountVO);
					} else {
						synchronized(groupAccountMapper) {
							groupId = groupAccountMapper.getMaxId();
							groupAccountVO = new GroupAccountVO();
							groupAccountVO.id = groupId;
							groupAccountVO.groupName = groupName;
							groupAccountMapper.putEntry(groupAccountVO);
						}
					}
					
					if(groupId != -1 && authorityCode!=null && !"".equals(authorityCode)) {
						groupAuthorityVO = groupAuthorityMapper.getEntry(groupId, authorityCode);
						if(groupAuthorityVO!=null) {
							groupAuthorityVO.authorityLevel = authorityLevel;
							groupAuthorityMapper.updateEntry(groupAuthorityVO);
						} else {
							synchronized(groupAuthorityMapper) {
								groupAuthorityVO = new GroupAuthorityVO();
								groupAuthorityVO.groupId = groupId;
								groupAuthorityVO.authorityCode = authorityCode;
								groupAuthorityVO.authorityLevel = authorityLevel;
								groupAuthorityMapper.putEntry(groupAuthorityVO);
							}
						}
					}
				}
			}
			
			int groupId = request.getIntParameter("groupId",-1);
			String authorityCode = request.getParameter("authorityCode");
			String authorityLevel = request.getParameter("authorityLevel");
			
			GroupAuthorityVO vo = null;
			
			synchronized(groupAuthorityMapper) {
				if(groupId != -1 && authorityCode!=null && !"".equals(authorityCode)) {
					vo = groupAuthorityMapper.getEntry(groupId, authorityCode);
					
					if(vo!=null) {
						vo.authorityLevel = authorityLevel;
						groupAuthorityMapper.updateEntry(vo);
					} else {
						vo = new GroupAuthorityVO();
						vo.groupId = groupId;
						vo.authorityCode = authorityCode;
						vo.authorityLevel = authorityLevel;
						groupAuthorityMapper.putEntry(vo);
					}
				}
			}
			resultWriter.object();
			resultWriter.endObject();
		}
		
		resultWriter.done();
	}
}
