package org.fastcatsearch.http.action.management.logs;

import java.io.Writer;
import java.util.List;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.ExceptionHistoryMapper;
import org.fastcatsearch.db.vo.ExceptionVO;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value="/management/logs/exception-history-list", authority=ActionAuthority.Logs, authorityLevel = ActionAuthorityLevel.READABLE)
public class GetExceptionHistoryListAction extends AuthAction {
	
	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		DBService dbService = DBService.getInstance();
		
		MapperSession<ExceptionHistoryMapper> session = null;
		
		int start = request.getIntParameter("start",0);
		
		int end = request.getIntParameter("end",start);
		
		int totalCount = 0;
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
		try {
		
			session = dbService.getMapperSession(ExceptionHistoryMapper.class);
			
			ExceptionHistoryMapper mapper = session.getMapper();
			
			List<ExceptionVO> entryList = null;
			
			totalCount = mapper.getCount();
			
			entryList = mapper.getEntryList(start, end);
			
			resultWriter.object().key("totalCount").value(totalCount)
				.key("start").value(start)
				.key("end").value(end)
				.key("exceptions").array();
			
			for(int inx=0; inx < entryList.size(); inx++) {
				
				ExceptionVO entry = entryList.get(inx);
				
				resultWriter.object()
					.key("id").value(entry.getId())
					.key("node").value(entry.getNode())
					.key("message").value(entry.getMessage())
					.key("trace").value(entry.getTrace())
					.key("regtime").value(entry.getRegtime())
					.endObject();
				
			}
			resultWriter.endArray().endObject();
		
		} finally {
			
			if(session!=null) {
				session.closeSession();
			}
		}
		
		resultWriter.done();
	}
}
